package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Documentos;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.canal.officebanking.enums.EnumMigracionTransmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.domain.enums.BankProcessChangeDataType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.ChangeDataBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.LoginBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.TransactionBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.generics.TransactionBankProcess;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.transmit.TransmitOB;

public class OBUsuarios extends ModuloOB {

    public static Object usuario(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        if (empty(sesion.usuarioOB)) {
            return respuesta("USUARIO_INVALIDO");
        }

        UsuarioOB usuarioOB = sesion.usuarioOB;

        Objeto datos = new Objeto();
        datos.set("cuil", usuarioOB.cuil);
        datos.set("nombre", usuarioOB.nombre);

        List<PermisoOB> listaPermisos = new ArrayList<PermisoOB>();
        if (sesion.esOperadorInicial()) {
            ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
            listaPermisos = servicioPermisos.permisos().get();
        } else {
            EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, usuarioOB);
            List<PermisoOperadorOB> permisosOperador = empresaUsuario.permisos.stream().collect(Collectors.toList());
            // se saca el filtro .filter(p -> p.habilitado)
            listaPermisos = permisosOperador.stream().map(p -> p.permiso).collect(Collectors.toList());
        }
        datos.set("menu", listaPermisos.stream().map(p -> p.objeto(false)).collect(Collectors.toList()));

        Objeto lstEmpresas = respuesta("empresas");
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresasUsuario = servicioEmpresaUsuario.findByUsuario(usuarioOB).get();
        for (EmpresaUsuarioOB item : empresasUsuario) {
            Objeto emp = lstEmpresas.add("empresas");
            emp.set("cuit", item.empresa.cuit);
            emp.set("idCobis", item.empresa.idCobis);
            emp.set("razonSocial", item.empresa.razonSocial);
            emp.set("rol", item.rol.nombre);
            emp.set("seleccionada", sesion.empresaOB.cuit.equals(item.empresa.cuit) ? "true" : "false");
            lstEmpresas.add(emp);
        }
        datos.add("empresas", lstEmpresas);

        return respuesta("datos", datos);
    }

    public static Object datosPersonales(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        UsuarioOB usuarioOB = sesion.usuarioOB;
        Objeto datos = new Objeto();
        datos.set("numeroDocumento", usuarioOB.numeroDocumento);
        datos.set("nombre", usuarioOB.nombre);
        datos.set("apellido", usuarioOB.apellido);
        datos.set("usuario", Config.desencriptarAES(usuarioOB.login));
        datos.set("email", usuarioOB.email);
        if (usuarioOB.telefonoLaboral.startsWith("+54")) {
            datos.set("telefonoLaboral", usuarioOB.telefonoLaboral.substring(3).replaceAll("\\D", ""));
        } else {
            datos.set("telefonoLaboral", usuarioOB.telefonoLaboral);
        }
        if (usuarioOB.telefonoMovil.startsWith("+54")) {
            datos.set("telefonoMovil", usuarioOB.telefonoMovil.substring(3).replaceAll("\\D", ""));
        } else {
            datos.set("telefonoMovil", usuarioOB.telefonoMovil);
        }
        datos.set("ultimoAcceso", usuarioOB.accesoFecha.toString());
        datos.set("ultimoAccesoHoy", validarAcceso(contexto));
        datos.set("migrado", usuarioOB.migrado);

        return respuesta("datos", datos);
    }

    public static Object validarAcceso(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        UsuarioOB usuarioOB = sesion.usuarioOB;

        if (usuarioOB.accesoFecha != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate hoy = LocalDate.parse(LocalDate.now().toString(), formatter);
            LocalDate ultimoAcceso = LocalDate.parse(usuarioOB.accesoFecha.toLocalDate().toString(), formatter);

            if (ultimoAcceso.isBefore(hoy)) {
                usuarioOB.accesoFecha = LocalDateTime.now();
                ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
                servicio.update(usuarioOB);
                return respuesta("ACCESO_ANTERIOR");
            }
        } else {
            usuarioOB.accesoFecha = LocalDateTime.now();
            ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
            servicio.update(usuarioOB);
            return respuesta("ACCESO_ANTERIOR");
        }

        return respuesta("0");
    }

    public static Object modificarDatosPersonales(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        LogOB.evento(contexto, "modificarDatosPersonales", "INICIO_MODIFICACION_DATOS_PERSONALES");
        //String email = sesion.usuarioOB.email;
        String email = contexto.parametros.string("email");
        String telefonoLaboral = contexto.parametros.string("telefonoLaboral");
        String telefonoMovil = contexto.parametros.string("telefonoMovil");

        if(contexto.sesion().usuarioOB.migrado !=1){
            if (sesion.token.fechaValidacion.isNull()) {
                return respuesta("FACTOR_NO_VALIDADO");
            }
        }

		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

        if (!celularValido(telefonoLaboral)) {
            return respuesta("TELEFONO_LABORAL_INVALIDO");
        }
        if (!celularValido(telefonoMovil)) {
            return respuesta("TELEFONO_MOVIL_INVALIDO");
        }

        if (!empty(sesion.tokenCorreo.correo)) {
            email = sesion.tokenCorreo.correo;
        }
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().empresaOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"modificarDatosPersonales".equals(contexto.sesion().challenge)){
			if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
				LogOB.evento(contexto, "modificarDatosPersonales", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				contexto.sesion().challenge = "modificarDatosPersonales";
				contexto.sesion().save();
				return respuesta("CHALLENGE");
			}
			if (recommendationDTO.getRecommendationType().equals("DENY")) {
				LogOB.evento(contexto, "modificarDatosPersonales", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				return respuesta("DENY");
			}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}
*/

        UsuarioOB usuarioOB = sesion.usuarioOB;
        usuarioOB.email = email;
        usuarioOB.telefonoLaboral = telefonoLaboral;
        usuarioOB.telefonoMovil = telefonoMovil;

        ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
        servicio.update(usuarioOB);

        sesion.usuarioOB = usuarioOB;
        sesion.save();

        LogOB.evento(contexto, "MODIFICACION_DATOS_PERSONALES", "FIN_MODIFICACION_DATOS_PERSONALES");
        EnvioEmail envioMail = ApiNotificaciones.envioAvisoModificaDatosPersonales(contexto, sesion.usuarioOB.email, sesion.usuarioOB.nombre, sesion.usuarioOB.apellido).tryGet();
        if (envioMail == null) {
            LogOB.error(contexto, "Error enviando Aviso Modificacion Datos Personales: " + sesion.usuarioOB.email + "|" + sesion.usuarioOB.nombre + "|" + sesion.usuarioOB.apellido + "|");
        }

        return respuesta();
    }

    public static Object modificarTelefonoMovil(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        LogOB.evento(contexto, "modificarTelefonoMovil", "INICIO_MODIFICACION_TELEFONO_MOVIL");

        String telefonoMovil = contexto.parametros.string("telefonoMovil");

        if (sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            if (sesion.token.fechaValidacion.isNull()) {
                return respuesta("FACTOR_NO_VALIDADO");
            }
        }
        sesion.token.fechaValidacion = Fecha.nunca();
        sesion.save();

        if (!celularValido(telefonoMovil)) {
            return respuesta("TELEFONO_MOVIL_INVALIDO");
        }
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().empresaOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"modificarTelefonoMovil".equals(contexto.sesion().challenge)){
			if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
				LogOB.evento(contexto, "modificarTelefonoMovil", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				contexto.sesion().challenge = "modificarTelefonoMovil";
				contexto.sesion().save();
				return respuesta("CHALLENGE");
			}
			if (recommendationDTO.getRecommendationType().equals("DENY")) {
				LogOB.evento(contexto, "modificarTelefonoMovil", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				return respuesta("DENY");
			}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}*/


        UsuarioOB usuarioOB = sesion.usuarioOB;
        usuarioOB.telefonoMovil = telefonoMovil;
        usuarioOB.validoOTP = true;

        ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
        servicio.update(usuarioOB);

        sesion.usuarioOB = usuarioOB;
        sesion.save();

        LogOB.evento(contexto, "MODIFICACION_TELEFONO_MOVIL", "FIN_MODIFICACION_TELEFONO_MOVIL");

        return respuesta();
    }

    public static Object modificarNombre(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        LogOB.evento(contexto, "modificarNombre", " INICIO_MODIFICACION_NOMBRE");
        String patron = "[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ]+";

        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");


        if (nombre == null || nombre.trim().isEmpty()) {
            return respuesta("ERROR");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            return respuesta("ERROR");
        }

        if(!nombre.matches(patron) || !apellido.matches(patron)){
            return respuesta("ERROR");
        }

        UsuarioOB usuarioOB = sesion.usuarioOB;
        usuarioOB.nombre = nombre;
        usuarioOB.apellido = apellido;
        ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
        servicio.update(usuarioOB);

        sesion.usuarioOB = usuarioOB;
        sesion.save();

        LogOB.evento(contexto, "modificarNombre", "FIN_MODIFICACION_NOMBRE");
        return respuesta();
    }

    public static Object modificarUsuario(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        LogOB.evento(contexto, "modificarUsuario", "INICIO_MODIFICACION_USUARIO");

        String usuario = contexto.parametros.string("usuario");
        String nuevoUsuario = contexto.parametros.string("nuevoUsuario");
        String usuarioDesencriptado = Config.desencriptarAES(sesion.usuarioOB.login);

        if (sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            if (sesion.token.fechaValidacion.isNull()) {
                return respuesta("FACTOR_NO_VALIDADO");
            }
        }
        sesion.token.fechaValidacion = Fecha.nunca();
        sesion.save();

        if (!usuarioDesencriptado.equals(usuario)) {
            return respuesta("USUARIO_INVALIDO");
        }

        if (!usuarioValido(nuevoUsuario)) {
            return respuesta("NUEVO_USUARIO_INVALIDO");
        }

        if (nuevoUsuario.equals(usuario)) {
            return respuesta("USUARIO_REPETIDO");
        }
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().empresaOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"modificarUsuario".equals(contexto.sesion().challenge)){
			if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
				LogOB.evento(contexto, "modificarUsuario", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				contexto.sesion().challenge = "modificarUsuario";
				contexto.sesion().save();
				return respuesta("CHALLENGE");
			}
			if (recommendationDTO.getRecommendationType().equals("DENY")) {
				LogOB.evento(contexto, "modificarUsuario", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				return respuesta("DENY");
			}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}*/
        UsuarioOB usuarioOB = sesion.usuarioOB;
        usuarioOB.login = Config.encriptarAES(nuevoUsuario);

        ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
        servicio.update(usuarioOB);

        OBLogin.logout(contexto);

        LogOB.evento(contexto, "modificarUsuario", "FIN_MODIFICAR_USUARIO");
        return respuesta();
    }

    public static Object cambiarEmpresa(ContextoOB contexto) {
        Long cuit = contexto.parametros.longer("cuit");
        String empIdCobis = contexto.parametros.string("idCobis", null);
        SesionOB sesion = contexto.sesion();

        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresa.findByCuit(cuit, empIdCobis).tryGet();
        if (empty(empresaOB)) {
            return respuesta("NO_EXISTE_EMPRESA");
        }

        EmpresaUsuarioOB usuarioEmpresa = empresasUsuario(contexto, empresaOB, sesion.usuarioOB);
        if (empty(usuarioEmpresa)) {
            return respuesta("DATOS_INVALIDOS");
        }

        LogOB.evento(contexto, "CAMBIAR_EMPRESA", new Objeto().set("SESION", sesion.esOperador().toString()).set("ROL", sesion.rol()));


        sesion.empresaOB = usuarioEmpresa.empresa;
        sesion.rol = usuarioEmpresa.rol.rol_codigo.toString();
        sesion.tarjetasVirtuales = null;
        sesion.save();
        try {
            if (contexto.esProduccion() || (!contexto.esProduccion() && sesion.sessionId != null)) {
                LoginBEBankProcess bankProcess = new LoginBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId);
                TransmitOB.setAuditLogReport(contexto);
                TransmitOB.obtenerRecomendacion(contexto, bankProcess);

            }
        } catch (Exception e) {
            LogOB.evento(contexto, "cambiarEmpresa", "Error al obtener recomendacion DRS");
        }
        LogOB.evento(contexto, "cambiarEmpresa", "ok");
        return respuesta();
    }

    public static Object cambiarClave(ContextoOB contexto) {
        String clave = contexto.parametros.string("clave");
        String claveAnterior = contexto.parametros.string("claveAnterior");


        SesionOB sesion = contexto.sesion();
        LogOB.evento(contexto, "cambiarClave", "INICIO_CAMBIAR_CLAVE");

        if (!claveValida(clave)) {
            return respuesta("CLAVE_INVALIDA");
        }

        if (sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            if (sesion.token.fechaValidacion.isNull()) {
                return respuesta("FACTOR_NO_VALIDADO");
            }
        }
        sesion.token.fechaValidacion = Fecha.nunca();
        sesion.save();
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().empresaOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"cambiarClave".equals(contexto.sesion().challenge)){
		if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
			LogOB.evento(contexto, "cambiarClave", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
			contexto.sesion().challenge = "cambiarClave";
			contexto.sesion().save();
			return respuesta("CHALLENGE");
		}
		if (recommendationDTO.getRecommendationType().equals("DENY")) {
			LogOB.evento(contexto, "cambiarClave", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
			return respuesta("DENY");
		}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}*/
        ApiSeguridad.cambiarClaveOB(contexto, sesion.usuarioOB.numeroDocumento.toString(), sesion.usuarioOB.login, clave, claveAnterior).get();
        LogOB.evento(contexto, "CAMBIO_CLAVE");

        OBLogin.logout(contexto);

        EnvioEmail envioMail = ApiNotificaciones.envioAvisoCambioClave(contexto, sesion.usuarioOB.email, sesion.usuarioOB.nombre, sesion.usuarioOB.apellido).tryGet();
        if (envioMail == null) {
            LogOB.error(contexto, "Error enviando Aviso Cambio Clave: " + sesion.usuarioOB.email + "|" + sesion.usuarioOB.nombre + "|" + sesion.usuarioOB.apellido + "|");
        }
        LogOB.evento(contexto, "cambiarClave", "FIN_CAMBIAR_CLAVE");

        return respuesta();
    }

    public static Object consultaPersonaFisica(ContextoOB contexto) {
        Long cuil = contexto.parametros.longer("cuil");
        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);

        UsuarioOB usuarioOB = servicioUsuario.findByCuil(cuil).tryGet();

        if (empty(usuarioOB)) {
            return respuesta("NO_EXISTE_USUARIO");
        }

        Objeto datos = new Objeto();
        datos.set("dni", usuarioOB.numeroDocumento);
        datos.set("cuil", usuarioOB.cuil);
        datos.set("nombre", usuarioOB.nombre);
        datos.set("apellido", usuarioOB.apellido);
        datos.set("email", usuarioOB.email);
        datos.set("telefonoLaboral", usuarioOB.telefonoLaboral);
        datos.set("telefonoMovil", usuarioOB.telefonoMovil);
        datos.set("idCobis", usuarioOB.idCobis);

        return respuesta("datos", datos);
    }

    public static Object updateAdheridoGire(ContextoOB contexto) {

        SesionOB sesion = contexto.sesion();
        UsuarioOB usuarioOB = sesion.usuarioOB;
        Boolean adheridoGire = contexto.parametros.bool("adheridoGire");
        if (adheridoGire != null && !usuarioOB.adheridoGire.equals(adheridoGire)) {
            usuarioOB.adheridoGire = adheridoGire;
            try {
                ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
                servicio.update(usuarioOB);
                sesion.save();
            } catch (Exception e) {
                LogOB.evento(contexto, "ERROR_MODIFICAR_ADHERIDO_GIRE");
                return respuesta("ERROR_MODIFICAR_ADHERIDO_GIRE");
            }
        }

        LogOB.evento(contexto, "MODIFICAR_ADHERIDO_GIRE");
        return respuesta();
    }

    public static Object inhabilitar(ContextoOB contexto) {
        String documento = contexto.parametros.string("documento");
        String usuario = contexto.parametros.string("usuario");
        ServicioUsuarioOB servicioUsuarioOB = new ServicioUsuarioOB(contexto);
        UsuarioOB u = servicioUsuarioOB.findByNumeroDocumento(Long.parseLong(documento)).tryGet();
        EstadoUsuarioOB e = new EstadoUsuarioOB();
        e.codigo = 2;
        u.estado = e;
        ServicioParametroOB servicioParametroOB = new ServicioParametroOB(contexto);
        ParametroOB mail = servicioParametroOB.find("mail_fraudes").tryGet();
        try {
            //if(Config.desencriptarAES(u.login).equals(usuario)){
            if (u.numeroDocumento.toString().equals(documento)) {
                servicioUsuarioOB.update(u);
                ApiNotificaciones.postEnvioMailInhabilitadoSeguridad(contexto, mail.valor, u);
                ApiNotificaciones.postEnvioMailInhabilitado(contexto, u);

                return respuesta("0", "datos", "ok");
            } else {
                return respuesta("ERROR", "datos", "error usuario");
            }

        } catch (Exception ex) {
            LogOB.evento(contexto, "inhabilitar", "ERROR");
            throw ex;
        }

    }

    public static Object modificarDatosUsuario(ContextoOB contexto) {
        String usuarioCrm = contexto.request.headers("x-usuario");
        LogCrmOB.evento(contexto, 0, 0, "Comienzo modificacion de datos", usuarioCrm);
        Integer codigo = contexto.parametros.integer(":codigo");
        Long cuitEmpresa = contexto.parametros.longer(":cuitempresa");

        Long numeroDocumento = contexto.parametros.longer("numeroDocumento", null);
        Long cuil = contexto.parametros.longer("cuil", null);
        String nombre = contexto.parametros.string("nombre", null);
        String apellido = contexto.parametros.string("apellido", null);
        String email = contexto.parametros.string("email", null);
        String telefonoMovil = contexto.parametros.string("telefonoMovil", null);
        String telefonoLaboral = contexto.parametros.string("telefonoLaboral", null);
        Integer codigoEstado = contexto.parametros.integer("estado", null);

        boolean banderaCambioDeEstado = false;
        List<String> cambios = new ArrayList<>();

        ServicioEmpresaOB servicioEmpresaOB = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresaOB.findByCuit(cuitEmpresa, null).get();
        if (empty(empresaOB)) {
            return respuesta("EMPRESA_INEXISTENTE");
        }

        ServicioUsuarioOB servicioUsuarioOB = new ServicioUsuarioOB(contexto);
        UsuarioOB usuarioOB = servicioUsuarioOB.find(codigo).tryGet();
        if (usuarioOB == null) {
            return respuesta("USUARIO_INEXISTENTE");
        }

        if (numeroDocumento != null && !usuarioOB.numeroDocumento.equals(numeroDocumento)) {
            cambios.add("Numero documento: de " + usuarioOB.numeroDocumento + " a " + numeroDocumento);
            usuarioOB.numeroDocumento = numeroDocumento;
        }

        if (cuil != null && !usuarioOB.cuil.equals(cuil)) {
            cambios.add("CUIL: de " + usuarioOB.cuil + " a " + cuil);
            usuarioOB.cuil = cuil;
        }

        if (nombre != null && !nombre.equals(usuarioOB.nombre)) {
            cambios.add("Nombre: de " + usuarioOB.nombre + " a " + nombre);
            usuarioOB.nombre = nombre;
        }

        if (apellido != null && !apellido.equals(usuarioOB.apellido)) {
            cambios.add("Apellido: de " + usuarioOB.apellido + " a " + apellido);
            usuarioOB.apellido = apellido;
        }

        if (email != null && !email.equals(usuarioOB.email)) {
            cambios.add("Email: de " + usuarioOB.email + " a " + email);
            usuarioOB.email = email;
        }

        if (telefonoMovil != null && !telefonoMovil.equals(usuarioOB.telefonoMovil)) {
            cambios.add("Telefono movil: de " + usuarioOB.telefonoMovil + " a " + telefonoMovil);
            usuarioOB.telefonoMovil = telefonoMovil;
        }

        if (telefonoLaboral != null && !telefonoLaboral.equals(usuarioOB.telefonoLaboral)) {
            cambios.add("Telefono laboral: de " + usuarioOB.telefonoLaboral + " a " + telefonoLaboral);
            usuarioOB.telefonoLaboral = telefonoLaboral;
        }

        if (codigoEstado != null && !codigoEstado.equals(usuarioOB.estado.codigo)) {
            cambios.add("Estado: de " + usuarioOB.estado.codigo + " a " + codigoEstado + " = " + (codigoEstado == 1 ? "Habilitado" : "Deshabilitado"));
            EstadoUsuarioOB estadoUsuarioOB = new EstadoUsuarioOB();
            estadoUsuarioOB.codigo = codigoEstado;
            usuarioOB.estado = estadoUsuarioOB;
            banderaCambioDeEstado = true;
        }
        try {
            Documentos doc = ApiPersonas.buscarPorDocumento(contexto, usuarioOB.numeroDocumento.toString()).tryGet();
            if (!empty(doc)) {
                if (usuarioOB.idCobis == null) {
                    cambios.add("idCobis: de NULL a  = " + doc.get(0).idCliente);
                    usuarioOB.idCobis = doc.get(0).idCliente;
                } else {
                    cambios.add("idCobis: de " + usuarioOB.estado.codigo + " a " + usuarioOB.idCobis + " = " + doc.get(0).idCliente);
                    usuarioOB.idCobis = doc.get(0).idCliente;
                }
            }
        } catch (Exception e) {

        }

        try {
            String mensaje = "Comienzo modificación de datos. " + "Código: " + codigo + ", " + "CUIT Empresa: " + cuitEmpresa + ", " + "Número Documento: " + numeroDocumento + ", " + "CUIL: " + cuil + ", " + "Nombre: " + nombre + ", " + "Apellido: " + apellido + ", " + "Email: " + email + ", " + "Teléfono Móvil: " + telefonoMovil + ", " + "Teléfono Laboral: " + telefonoLaboral + ", " + "Código Estado: " + codigoEstado; // Llama a la función de logueo con el mensaje construido LogCrmOB.evento(contexto, 0, 0, mensajeLog, usuarioCrm);
            LogCrmOB.evento(contexto, empresaOB.emp_codigo, usuarioOB.codigo, "Datos modificados: " + mensaje, usuarioCrm);
            servicioUsuarioOB.update(usuarioOB).get();
            String respuesta = "";
            //String mensaje = String.join(" | ", cambios);

            if (banderaCambioDeEstado) {
                if (usuarioOB.estado.codigo.equals(1)) {
                    respuesta = "USUARIO_HABILITADO";
                } else {
                    respuesta = "USUARIO_DESHABILITADO";
                }

                if (!cambios.isEmpty()) {
                    respuesta += "_MODIFICADO";
                }
                LogCrmOB.evento(contexto, empresaOB.emp_codigo, usuarioOB.codigo, "Datos modificados: " + mensaje, usuarioCrm);
            } else if (!cambios.isEmpty()) {
                respuesta = "USUARIO_MODIFICADO";
                LogCrmOB.evento(contexto, empresaOB.emp_codigo, usuarioOB.codigo, "Datos modificados: " + mensaje, usuarioCrm);
            } else {
                LogCrmOB.evento(contexto, empresaOB.emp_codigo, usuarioOB.codigo, "Datos sin modificar para el usuario con documento: " + numeroDocumento, usuarioCrm);
                respuesta = "SIN_CAMBIOS";
            }
            return respuesta(respuesta);

        } catch (Exception ex) {
            String mensajeError = Optional.ofNullable(ex.getMessage()).orElse("Sin mensaje de error");
            String mensajeTruncado = mensajeError.length() > 450 ? mensajeError.substring(0, 450) : mensajeError;
            LogCrmOB.evento(contexto, empresaOB.emp_codigo, usuarioOB.codigo, "Error al modificar datos: Exception " + mensajeTruncado, usuarioCrm);
            return respuesta("ERROR_ACTUALIZACION_USUARIO");
        }
    }
}