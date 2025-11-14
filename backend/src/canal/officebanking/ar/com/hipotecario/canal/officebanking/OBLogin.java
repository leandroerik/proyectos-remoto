package ar.com.hipotecario.canal.officebanking;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsCrm;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsOfficeBanking;
import ar.com.hipotecario.backend.servicio.sql.hb_be.TokensOB.TokenOB;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior.UsuarioOBAnterior;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.LoginBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeResult;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeResultType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeType;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ReporteCobranzaCSVDTO;
import ar.com.hipotecario.canal.officebanking.dto.logs.ReporteLogsCrmDto;
import ar.com.hipotecario.canal.officebanking.dto.logs.ReporteLogsDto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.transmit.TransmitOB;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import static ar.com.hipotecario.backend.util.LoginLDAP.loginLDAP;

public class OBLogin extends ModuloOB {

	public static Object prelogin(ContextoOB contexto) {
        try {
        	String numeroDocumento = contexto.parametros.string("numeroDocumento");
        	String usuario = contexto.parametros.string("usuario", null);
        	String clave = contexto.parametros.string("clave", null);
        	Boolean noMigrar = contexto.parametros.bool("noMigrar", false);

        	UsuarioOB usuarioOB = usuario(contexto, Long.parseLong(numeroDocumento));
        	if (empty(usuarioOB)) {
        		LogOB.evento(contexto, "NO_EXISTE_USUARIO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
        		return respuesta("NO_EXISTE_USUARIO");
        	}
        	
        	LogOB.evento(contexto, "INICIO_PRELOGIN", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
        	
            if (usuarioOB.migrado==0) {
                try {
                    ApiSeguridad.loginOB(contexto, String.valueOf(numeroDocumento), usuario, clave).get();
                } catch (Exception e) {
                    LogOB.evento(contexto,"ERROR_MIGRACION", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), e.getMessage());
                    return respuesta("NO_MIGRADO");
                }
            }

        	if(usuarioOB.migrado == 2 || (noMigrar && usuarioOB.migrado == 0)) {
        		return respuesta("NO_MIGRADO");
        	}
        	if(usuarioOB.migrado == 1) {
        		return respuesta("MIGRADO");
        	}
        	if(usuarioOB.migrado == 0) {
        		Objeto response = OBTransmit.migrarUsuario(contexto, usuarioOB, usuario, clave);
        		JSONObject jsonObject = new JSONObject(response.toString());
        		String estado = jsonObject.getString("estado");
        		if(estado.equals("0")) {
        			return respuesta("MIGRADO");
        		}else {
        			return respuesta("NO_MIGRADO");
        		}
        	}
        	LogOB.evento(contexto,"FIN_PRELOGIN", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
        	return respuesta("0");
        }catch(Exception ex) {
        	LogOB.evento(contexto, "Exception prelogin", ex.getMessage());
			return respuesta("ERROR");
        }
    }
	
    public static Object login(ContextoOB contexto) {
        String numeroDocumento = contexto.parametros.string("numeroDocumento");
        String usuario = contexto.parametros.string("usuario").toString();
        String clave = contexto.parametros.string("clave");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        Long cuit = contexto.parametros.longer("cuit", null);
        String sessionID = contexto.parametros.string("sessionID",null);
        String empIdCobis = contexto.parametros.string("idCobis", null);
        String responseTransmit = contexto.parametros.string("mensajeTransmit", null);
        Boolean hasSoftToken = contexto.parametros.bool("hasSoftToken", false);
        
        Boolean validarClave = contexto.esProduccion() || !clave.equals("0");
        
        UsuarioOB usuarioOB = usuario(contexto, Long.parseLong(numeroDocumento));
        if (empty(usuarioOB)) {
            LogOB.evento(contexto,"login","NO_EXISTE_USUARIO");
            return respuesta("NO_EXISTE_USUARIO");
        }

        if (usuarioOB.estado.codigo==2) {
            LogOB.evento(contexto,"login","INHABILITADO");
            return respuesta("INHABILITADO");
        }

        JSONObject obj = null;
        if(usuarioOB.migrado == 1) {
        	obj = new JSONObject(responseTransmit);
        }
        
        String tokenReceived = null;
        try {
        	tokenReceived = obj.getString("tokenReceived");
        }catch (Exception ex){
        	tokenReceived = null;
        }
        
        if(usuarioOB.migrado == 1 && tokenReceived == null) {
        	if(empty(responseTransmit)) {
        		LogOB.evento(contexto,"login","RESPUESTA_TRANSMIT_VACIA");
        		return respuesta("ERROR");
        	}
                            
        	String codeError = null;        	
        	String text = obj.getJSONObject("data").getString("text");
        	
            int startIndex = text.indexOf('{');
            int endIndex = text.lastIndexOf('}') + 1;

            try {
            	if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            		String embeddedJsonStr = text.substring(startIndex, endIndex);
            		JSONObject embeddedJson = new JSONObject(embeddedJsonStr);
            		codeError = embeddedJson.getString("code");                
            	}
            }catch(Exception ex) {
            	LogOB.evento(contexto,"Exception JSON", ex.getMessage());
            }
        	            
        	if(!empty(responseTransmit) && codeError.equals("invalid_credentials"))
        	{
        		LogOB.evento(contexto,"login","NO_EXISTE_USUARIO_TRANSMIT");
        		return respuesta("NO_EXISTE_USUARIO");
        	}
        	
        	if(!empty(responseTransmit) && codeError.equals("invalid_password"))
        	{
        		LogOB.evento(contexto,"login","NO_EXISTE_USUARIO_TRANSMIT");
        		return respuesta("NO_EXISTE_USUARIO");
        	}
        	
        	if(!empty(responseTransmit) && codeError.equals("authenticator_locked"))
        	{
        		LogOB.evento(contexto,"login","USUARIO_BLOQUEADO_TRANSMIT");
        		return respuesta("USUARIO_BLOQUEADO");
        	}
        }  
        
        if (validarClave && usuarioOB.migrado !=1) {
            try {
                String claveSesion = "SESION_OB_" + usuarioOB.codigo.toString();
                String jsonSesionOB = contexto.get(claveSesion);
                if (!empty(jsonSesionOB)) {
                    Objeto datos = Objeto.fromJson(jsonSesionOB);
                    Fecha fechaExpiracion = datos.fecha("fechaExpiracion", "yyyy-MM-dd HH:mm:ss");
                    /*if (!fingerprint.equals(datos.string("uuid")) && fechaExpiracion.restarMinutos(10).esPosterior(Fecha.ahora())) {
                        return respuesta("SESION_DUPLICADA");
                    }*/
                }
            } catch (Exception e) {

            }
        }

        contexto.deleteSesion();
        SesionOB sesion = contexto.sesion();
        sesion.usuarioOB = usuarioOB;
        sesion.idCobis = usuarioOB.idCobis;
        sesion.sucursal = null;
        sesion.fechaLogin = Fecha.nunca();
        sesion.ultimoAccesoHoy = usuarioOB.accesoFecha;
        sesion.hasSoftToken = hasSoftToken;
        sesion.sessionId = sessionID;

        if(usuarioOB.accesoFecha == null) {
			usuarioOB.accesoFecha = LocalDateTime.now();
			ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
			servicio.update(usuarioOB);
		}

        sesion.crearSesion();

        if (usuarioOB.migrado == 0 && validarClave && !usuarioClaveValidos(usuario, clave)) {
        	return respuesta("DATOS_INVALIDOS");
        }
        
        if (validarClave && usuarioOB.migrado !=1) {
            try {
                ApiSeguridad.loginOB(contexto, String.valueOf(numeroDocumento), usuario, clave).get();
            } catch (ApiException e) {
                if ("CLAVE_EXPIRADA".equals(e.codigoError)) {
                    sesion.fechaLogin = Fecha.ahora();
                    sesion.save();
                }
                /*if ("USUARIO_BLOQUEADO".equals(e.codigoError) && e.response.toString()!=null && e.response.get("codigo").equals("3")) {
                    sesion.fechaLogin = Fecha.ahora();
                    sesion.save();
                    return respuesta("DATOS_INVALIDOS");
                }*/
                throw e;
            }
        }

        EmpresaOB empresaOB = null;
        if (!empty(cuit)) {
            ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
            empresaOB = servicioEmpresa.findByCuit(cuit, empIdCobis).tryGet();
            if (empty(empresaOB)) {
                return respuesta("NO_EXISTE_EMPRESA");
            }
            EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, empresaOB, usuarioOB);
            if (empty(empresaUsuario)) {
                return respuesta("DATOS_INVALIDOS");
            }
            sesion.empresaOB = empresaUsuario.empresa;
            sesion.rol = empresaUsuario.rol.rol_codigo.toString();
        }

        sesion.fechaLogin = Fecha.ahora();
        sesion.save();

        Objeto datos = new Objeto();
        System.out.println("usuarioOB.migrado: " + usuarioOB.migrado);
        if(usuarioOB != null && usuarioOB.migrado.intValue() != 2) {
        	System.out.println("USUARIO_MIGRADO");
        	System.out.println("usuarioOB.migrado2: " + usuarioOB.migrado);
        	LogOB.evento(contexto,"INGRESO_LIBRERIA");
        	OBTransmit.requestLibreriaRiesgoFraudes(contexto, usuarioOB, usuario, clave, responseTransmit, datos);
        	LogOB.evento(contexto,"FIN_LIBRERIA");
        }
        
        if (empty(empresaOB)) {
            ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
            List<EmpresaUsuarioOB> empresasUsuario = servicioEmpresaUsuario.findByUsuario(usuarioOB).tryGet();
            if (empresasUsuario.size() > 1) {
                Objeto respuesta = respuesta("MULTIPLES_CUITS");
                for (EmpresaUsuarioOB ua : empresasUsuario) {
                    Objeto empresa = respuesta.add("empresas");
                    empresa.set("cuit", ua.empresa.cuit);
                    empresa.set("razonSocial", ua.empresa.razonSocial);
                    empresa.set("idCobis", ua.empresa.idCobis);
                    empresa.set("rol", ua.rol.nombre);
                }
                LogOB.evento(contexto, "MULTIPLES_CUITS");
                return respuesta;
            } else if (empresasUsuario.size() == 1) {
                sesion.empresaOB = empresasUsuario.get(0).empresa;
                sesion.rol = empresasUsuario.get(0).rol.rol_codigo.toString();
                sesion.save();
            }
        }
        String logueo="";
        for (String valor : contexto.request.headers()) {
            logueo+=valor+": "+ contexto.request.headers(valor);
        }
        contexto.registrarSesion(usuarioOB.codigo.toString(), fingerprint);
        LogOB.evento(contexto, "LOGIN");

        datos.set("cuit", sesion.empresaOB.cuit);
        datos.set("idCobis", sesion.empresaOB.idCobis);
        datos.set("rol", sesion.rol());
        datos.set("razonSocial", sesion.empresaOB.razonSocial);
        try {
            if (contexto.esProduccion() || (!contexto.esProduccion() && sessionID != null)) {
                LoginBEBankProcess bankProcess = new LoginBEBankProcess(sesion.empresaOB.idCobis, sessionID);
                TransmitOB.setAuditLogReport(contexto);
               TransmitOB.obtenerRecomendacion(contexto, bankProcess);

            }
        }catch (Exception e){
            LogOB.evento(contexto, "LOGIN DRS", "Error al obtener recomendacion DRS ");
        }
        return respuesta("datos", datos);
    }

    public static Object pseudoLogin(ContextoOB contexto) {
        String numeroDocumento = contexto.parametros.string("numeroDocumento");
        String usuario = contexto.parametros.string("usuario", null);
        String clave = contexto.parametros.string("clave", null);
        String captcha = contexto.parametros.string("captcha");

        SesionOB sesion = contexto.sesion();
        Boolean validarCaptcha = contexto.esProduccion() || !captcha.equals("0");
        if (validarCaptcha) {
            if (sesion.captcha.texto == null) {
                return respuesta("CAPTCHA_NO_GENERADO");
            } else if (!captcha.equals(sesion.captcha.texto)) {
                return respuesta("CAPTCHA_INVALIDO");
            }
        }
        sesion.captcha.fechaValidacion = Fecha.ahora();
        sesion.captcha.texto = null;
        sesion.save();
        Boolean validarUsuario = contexto.esProduccion() || (!empty(usuario) && !usuario.equals("0")) || (!empty(clave) && !clave.equals("0"));
        UsuarioOB usuarioOB = usuario(contexto, Long.parseLong(numeroDocumento));
        
        if (usuarioOB.migrado.toString().equals("0") && validarUsuario && (!empty(usuario) && !usuarioValido(usuario))) {
            return respuesta("DATOS_INVALIDOS");
        }
        
        if (!usuarioOB.migrado.toString().equals("1")){
        	if(!empty(usuario)) {
        		String usuarioEncriptado = Config.encriptarAES(usuario);
        		if (empty(usuarioOB) || !usuarioOB.login.equals(usuarioEncriptado)) {
        			return respuesta("NO_EXISTE_USUARIO");
        		}
        	}
        	
        	if(!empty(clave)) {
        		String claveEncriptada = Encriptador.sha512(clave);        	
        		ServicioClaveUsuarioOB servicioClaveUsuario = new ServicioClaveUsuarioOB(contexto);
        		ClaveUsuarioOB cu = servicioClaveUsuario.findByUsuario(usuarioOB).get().get(0);
        		if(empty(usuarioOB) || !cu.clave.equals(claveEncriptada)) {
        			return respuesta("NO_EXISTE_USUARIO");
        		}
        	}
        }
                
        if(usuarioOB.estado.codigo==2){
            return respuesta("INHABILITADO");
        }

        sesion.usuarioOB = usuarioOB;
        sesion.idCobis = usuarioOB.idCobis;
        sesion.sucursal = null;
        sesion.fechaLogin = Fecha.nunca();
        sesion.crearSesion();
        LogOB.evento(contexto, "PSEUDOLOGIN");
        Objeto datos = new Objeto();
        datos.set("numeroDocumento", sesion.usuarioOB.numeroDocumento.toString());
        datos.set("telefonoMovil", celularEnmascarado(usuarioOB.telefonoMovil.toString()));
        datos.set("migrado", sesion.usuarioOB.migrado);
                
        return respuesta("datos", datos);
    }

    public static Objeto loginViejoOB(ContextoOB contexto) {
        Long cuit = contexto.parametros.longer("cuit", contexto.sesion().empresaOB.cuit);
        String idCobis = contexto.parametros.string("idCobis", null);
        String funcionalidad = contexto.parametros.string("funcionalidad", "");

        SesionOB sesion = contexto.sesion();

        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresa.findByCuit(cuit, idCobis).tryGet();
        if (empty(empresaOB)) {
            return respuesta("NO_EXISTE_EMPRESA");
        }

        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
        UsuarioOB usuarioOB = servicioUsuario.find(sesion.usuarioOB.codigo).tryGet();

        EmpresaUsuarioOB empresasUsuario = empresasUsuario(contexto, empresaOB, usuarioOB);
        if (empty(empresasUsuario)) {
            return respuesta("DATOS_INVALIDOS");
        }

        LogOB.evento(contexto, "SALTO_OB_VIEJO", new Objeto().set("cuit", cuit).set("funcionalidad", funcionalidad));
        sesion.cuil = usuarioOB.cuil.toString();
        String uuid = UUID.randomUUID().toString();
        Fecha fechaExpiracion = Fecha.ahora().sumarSegundos(20);
        SqlHB_BE.eliminarTokenOB(contexto, cuit, sesion.usuarioOB.cuil.toString()).get();
        Futuro<UsuarioOBAnterior> futuroUsuOBViejo = SqlHB_BE.usuarioOBAnteriorPorCuityCuil(contexto, sesion.usuarioOB.cuil.toString(), sesion.empresaOB.cuit);
        UsuarioOBAnterior usuarioViejo = futuroUsuOBViejo.get();
        SqlHB_BE.crearTokenOB(contexto, uuid, cuit, sesion.cuil, fechaExpiracion, usuarioViejo.usu_login).get();
        contexto.deleteSesion();
        String url = format(contexto.config.string("ob_vieja_url") + "/pages/public/saltoCanal.xhtml?uuid=" + uuid);

        if (!contexto.esOpenShift()) {
            System.out.println("SALTO: " + url);
        }
        return respuesta(url);
    }

    public static Object autoLogin(ContextoOB contexto) {
        String token = contexto.parametros.string("token");
        contexto.deleteSesion();

        SesionOB sesion = contexto.sesion();
        TokenOB tokenOB = SqlHB_BE.tokenOB(contexto, token).tryGet();
        if (tokenOB == null) {
            return contexto.esOpenShift() ? redireccion("/") : respuesta("TOKEN_INVALIDO");
        }

        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
        UsuarioOB usuarioOB = servicioUsuario.findByCuil(Long.valueOf(tokenOB.cuil)).tryGet();
        if (empty(usuarioOB)) {
            return respuesta("CUIL_INVALIDO");
        }

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresasUsuario = servicioEmpresaUsuario.findByUsuario(usuarioOB).tryGet();

        if (empty(empresasUsuario)) {
            return respuesta("DATOS_INVALIDOS");
        }

        sesion.usuarioOB = usuarioOB;
        sesion.sucursal = null;
        sesion.fechaLogin = Fecha.ahora();
        sesion.idCobis = usuarioOB.idCobis;

        sesion.crearSesion();

        if (empresasUsuario.size() > 1) {
            Objeto respuesta = respuesta("MULTIPLES_CUITS");
            for (EmpresaUsuarioOB item : empresasUsuario) {
                EmpresaOB emp = item.empresa;
                Objeto empresa = respuesta.add("empresas");
                empresa.set("cuit", emp.cuit);
                empresa.set("razonSocial", emp.razonSocial);
                empresa.set("idCobis", emp.idCobis);
                empresa.set("rol", item.rol.nombre);
            }
            return respuesta;
        } else if (empresasUsuario.size() == 1) {
            EmpresaUsuarioOB usuarioEmpresasOB = empresasUsuario.get(0);
            EmpresaOB emp = usuarioEmpresasOB.empresa;
            sesion.empresaOB = emp;
            sesion.rol = usuarioEmpresasOB.rol.rol_codigo.toString();
        } else
            return "";

        sesion.save();

        LogOB.evento(contexto, "LOGIN");

        Objeto datos = new Objeto();
        datos.set("cuit", sesion.empresaOB.cuit);
        datos.set("rol", sesion.rol());
        datos.set("idCobis", sesion.empresaOB.idCobis);
        datos.set("enMantenimiento", contexto.config.bool("ob_en_mantenimiento", false));

        return respuesta("datos", datos);

    }

    public static Object loginNuevoOB(ContextoOB contexto) {
        String uuid = contexto.parametros.string("uuid");
//		String funcionalidad = contexto.parametros.string("funcionalidad", "");

        contexto.deleteSesion();
        SesionOB sesion = contexto.sesion();

        TokenOB tokenOB = SqlHB_BE.tokenOB(contexto, uuid).tryGet();
        if (tokenOB == null) {
            return contexto.esOpenShift() ? redireccion("/") : respuesta("TOKEN_INVALIDO");
        } else if (tokenOB.expirado()) {
            return contexto.esOpenShift() ? redireccion("/") : respuesta("TOKEN_EXPIRADO");
        }

        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
        UsuarioOB usuarioOB = servicioUsuario.findByCuil(Long.valueOf(tokenOB.cuil)).tryGet();
        if (empty(usuarioOB)) {
            return respuesta("CUIL_INVALIDO");
        }

        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresa.findByCuit(Long.valueOf(tokenOB.cuit), null).tryGet(); // TODO: ver que pasa con empresas con n id cobis
        if (empty(empresaOB)) {
            return respuesta("NO_EXISTE_EMPRESA");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, empresaOB, usuarioOB);
        if (empty(empresaUsuario)) {
            return respuesta("DATOS_INVALIDOS");
        }

        sesion.usuarioOB = usuarioOB;
        sesion.sucursal = null;
        sesion.fechaLogin = Fecha.ahora();
        sesion.rol = empresaUsuario.rol.rol_codigo.toString();
        sesion.crearSesion();

        // LogOB.evento(contexto, "SALTO_OB_NUEVO", new Objeto().set("funcionalidad",
        // funcionalidad));

        String url = format(contexto.config.string("ob_vieja_url") + uuid);

        return redireccion(url);
    }

    public static Object logout(ContextoOB contexto) {
        if (!empty(contexto.sesion().usuarioOB.codigo)) {
            String key = "SESION_OB_" + contexto.sesion().usuarioOB.codigo;
            contexto.del(key);
        }
        LogOB.evento(contexto,"login","LOGOUT_EJECUTADO");

        contexto.deleteSesion();
        return respuesta();
    }

    //logs de front
    public static String log(ContextoOB contexto) {
        String datos = contexto.parametros.string("datos");
        LogOB.evento(contexto, "FRONT", datos);
        return "0";
    }

    public static Object getLog(ContextoOB contexto) {
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");
        String fecha1 = contexto.parametros.string("desde", null);
        String fecha2 = contexto.parametros.string("hasta", null);
        String cuitEmpresa = contexto.parametros.string("cuitEmpresa", null);
        String tabla = contexto.parametros.string("tabla", "ob");

        if(usuario==null || clave==null || usuario.equals("") || clave.equals("")){
            return respuesta("Error","usuario y clave son obligatorios");
        }
        if (loginLDAP(usuario, clave)) {
            if (fecha1 == null || fecha1.equals("")) {
                fecha1 = Fecha.hoy().toString();
            }
            if (fecha2 == null || fecha2.equals("")) {
                fecha2 = Fecha.hoy().toString();
            }

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date fechaI = formato.parse(fecha1);
                Date fechaF = formato.parse(fecha2);
                long diferenciaEnMilisegundos = Math.abs(fechaI.getTime() - fechaF.getTime());
                long diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24);
                if (diferenciaEnDias > 7) {
                    return respuesta("Error","rango de fechas mayor  a una semana");
                }
            } catch (Exception e) {
            }



                try {
                    Object resultado;
                    if(tabla.equals("ob")){
                        resultado = LogOB.getLogs(contexto, fecha1, fecha2, cuitEmpresa);
                    }else {
                        resultado=  LogsCrm.selectPorFecha(contexto, fecha1, fecha2, cuitEmpresa);
                    }


                    if(resultado==null){
                        return respuesta("Error","resutado null");
                    }else {
                        ArrayList<ReporteLogsDto> listReportes = new ArrayList<>();
                        ArrayList<ReporteLogsCrmDto> listReportesCrm = new ArrayList<>();
                        if(tabla.equals("crm")){
                            LogsCrm logsCrm= (LogsCrm) resultado;
                            for (LogsCrm.LogCrm log : logsCrm) {
                                listReportesCrm.add(new ReporteLogsCrmDto(log.operacion, log.momento.toString(),String.valueOf(log.empresa),String.valueOf(log.usuario),log.usuario_crm));

                            }
                        }else{
                            LogsOfficeBanking logsOfficeBanking= (LogsOfficeBanking) resultado;
                        for (LogsOfficeBanking.LogOfficeBanking log : logsOfficeBanking) {
                            listReportes.add(new ReporteLogsDto(log.momento.toString(), log.empresa, log.usuario, log.endpoint, log.evento, log.error,log.datos,log.idProceso, log.ip));

                        }
                        }
                        if(listReportes.size() > 0 || listReportesCrm.size()>0){
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
                            CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                            if(listReportes.size() > 0 ){
                                var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<ReporteLogsDto>();
                                mappingStrategy.setType(ReporteLogsDto.class);
                                StatefulBeanToCsv<ReporteLogsDto> builder = new StatefulBeanToCsvBuilder<ReporteLogsDto>(writer)
                                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                                        .withSeparator(';')
                                        .withMappingStrategy(mappingStrategy)
                                        .build();
                                builder.write(listReportes);
                            }else {
                                var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<ReporteLogsCrmDto>();
                                mappingStrategy.setType(ReporteLogsCrmDto.class);
                                StatefulBeanToCsv<ReporteLogsCrmDto> builder = new StatefulBeanToCsvBuilder<ReporteLogsCrmDto>(writer)
                                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                                        .withSeparator(';')
                                        .withMappingStrategy(mappingStrategy)
                                        .build();
                                builder.write(listReportesCrm);
                            }

                        streamWriter.flush();
                        writer.close();

                        byte[] file = stream.toByteArray();
                        contexto.response.header("Content-Disposition", "attachment; filename=logs-" + Fecha.hoy() + ".csv");
                        contexto.response.type("application/csv");
                        return file;
                        }else {
                            return respuesta("ERROR", "no hay resultados");
                        }
                    }

                } catch (Exception e) {
                    return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
                }


        }
        return respuesta("Error","usuario y contraseña invalido");
    }


}
