package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.response.GeneroResponse;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Emails;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.*;
import ar.com.hipotecario.backend.servicio.sql.SqlHomeBanking;
import ar.com.hipotecario.backend.servicio.sql.homebanking.AccesoBiometriaHB;
import ar.com.hipotecario.backend.servicio.sql.mobile.RegistroDispositivoMobile;
import ar.com.hipotecario.backend.servicio.sql.mobile.SoftTokenMobile;
import ar.com.hipotecario.backend.util.Errores;
import ar.com.hipotecario.backend.util.MapperUtil;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBBiometria.IsvaDTO;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.endpoints.EMBLogin;
import ar.com.hipotecario.mobile.lib.Encriptador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.LoginToken;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBLogin {

    public static void supercache(ContextoMB contexto) {
        if (contexto.esAndroid()) {
            contexto.superCache("GET", "/mb/api/cliente-exterior", new Objeto().set("idCobis", contexto.idCobis()));
            contexto.superCache("GET", "/mb/api/tiene-dispositivos-registrados");
            contexto.superCache("POST", "/mb/api/campanas");
            contexto.superCache("POST", "/mb/api/consulta-aumento-sueldo");
            contexto.superCache("POST", "/mb/api/productos");
            contexto.superCache("POST", "/mb/api/estados-cuenta");
            contexto.superCache("GET", "/mb/api/encuesta-usuario", new Objeto().set("canal", "MB").set("funcionalidad", "PantallaInicio"));
            contexto.superCache("POST", "/mb/api/verificaAccesos", new Objeto().set("canal", "MB").set("funcionalidad", "PantallaInicio"));
            contexto.superCache("GET", "/mb/api/datavalid-vigente");
        }

        if (contexto.esIOS()) {
            contexto.superCache("GET", "/mb/api/tiene-dispositivos-registrados");
            contexto.superCache("GET", "/mb/api/cliente-exterior");
            contexto.superCache("POST", "/mb/api/productos");
            contexto.superCache("GET", "/mb/api/notificaciones-descarga");
            contexto.superCache("POST", "/mb/api/perfil-inversor");
            contexto.superCache("POST", "/mb/api/canales-otp-usuario", new Objeto().set("funcionalidad", "transferencia"));
            contexto.superCache("POST", "/mb/api/consulta-recurrencia");
            contexto.superCache("POST", "/mb/api/consulta-aumento-sueldo");
            contexto.superCache("GET", "/mb/api/ps-enabled-transfer");
            contexto.superCache("POST", "/mb/api/campanas");
            contexto.superCache("POST", "/mb/api/estados-cuenta");
            contexto.superCache("GET", "/mb/api/validar-onboarding-tdv");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MBLogin.class);

    public static Boolean salesforceIngresoUsuario(ContextoMB contexto) throws Exception {
        Persona persona = contexto.persona();
        Objeto parametros = new Objeto();
        parametros.set("NOMBRE", persona.nombre());
        parametros.set("APELLIDO", persona.apellidos());
        parametros.set("ENVIO_MAIL", RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_ACC"));
        Date hoy = new Date();
        parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
        parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
        parametros.set("TITULAR_CANAL", persona.nombreCompleto());

        MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_login"), parametros);

        return true;
    }

    public static Boolean mailIngresoUsuario(ContextoMB contexto, String cuit, String apellido, String nombre)
            throws Exception {

        try {
            if (!RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_ACC"))
                return false;

            if (!StringUtils.isNotBlank(cuit)) {
                Persona persona = contexto.persona();
                cuit = persona.cuit();
                apellido = persona.apellidos();
                nombre = persona.nombres();
            }

            String emailDestino = RestPersona.direccionEmail(contexto, cuit);
            if (!StringUtils.isNotBlank(emailDestino))
                return false;

            if (emailDestino != null && !emailDestino.isEmpty()) {
                ApiRequestMB requestMail = ApiMB.request("LoginCorreoElectronico", "notificaciones", "POST",
                        "/v1/correoelectronico", contexto);
                requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                requestMail.body("para", emailDestino);
                requestMail.body("plantilla", ConfigMB.string("doppler_acceso_usuario"));
                requestMail.permitirSinLogin = true;
                Objeto parametros = requestMail.body("parametros");
                parametros.set("Subject", "Ingreso a BH");
                parametros.set("NOMBRE", nombre);
                parametros.set("APELLIDO", apellido);
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                parametros.set("TITULAR_CANAL", apellido);

                ApiMB.response(requestMail, new Date().getTime());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static RespuestaMB pseudoLogin(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        contexto.eliminarSesion(false, "");

        if (Objeto.anyEmpty(documento)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // idCobis
        List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, documento, idTipoDocumento, idSexo);
        if (listaIdCobis == null) {
            return RespuestaMB.error();
        } else if (listaIdCobis.isEmpty()) {
            // return Respuesta.estado("PERSONA_NO_ENCONTRADA"); //se modifica por bache de
            // seguridad
            return RespuestaMB.estado("USUARIO_INVALIDO");
        } else if (listaIdCobis.size() > 1) {
            return RespuestaMB.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        String idCobis = listaIdCobis.get(0);
        contexto.sesion().setIdCobis(idCobis);
        registrarHeaders(contexto, "pseudoLogin");

        // Validación Claves Generadas
        ApiResponseMB usuarioIDG = RestSeguridad.usuario(contexto);
        if (usuarioIDG.hayError()) {
            return RespuestaMB.error();
        }
        Boolean tieneUsuario = usuarioIDG.bool("tieneClaveDefault", false);
        Boolean tieneClave = usuarioIDG.bool("tieneClaveNumerica", false);

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("claveGenerada", tieneUsuario && tieneClave);
        respuesta.set("tieneClaveUsuario", tieneUsuario);
        respuesta.set("tieneClaveNumerica", tieneClave);

        ApiResponseMB response = RestPersona.clientes(contexto);
        if (response.hayError()) {
            if (response.string("mensajeAlUsuario").contains("no fue encontrada en BUPT")) {
                // return Respuesta.estado("PERSONA_NO_ENCONTRADA"); //se modifica por bache de
                // seguridad
                return RespuestaMB.estado("USUARIO_INVALIDO");
            }
        }
        return respuesta;
    }

    public static RespuestaMB logout(ContextoMB contexto) {
        contexto.eliminarSesion(true, contexto.idCobis());
        return RespuestaMB.exito();
    }

    public static RespuestaMB refrescarSesion() {
        return RespuestaMB.exito();
    }

    public static RespuestaMB tiposDocumento() {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add("tipos", new Objeto().set("id", "01").set("descripcion", "DNI"));
        respuesta.add("tipos", new Objeto().set("id", "02").set("descripcion", "Libreta de Enrolamiento"));
        respuesta.add("tipos", new Objeto().set("id", "03").set("descripcion", "Libreta Civica"));
        respuesta.add("tipos", new Objeto().set("id", "125").set("descripcion", "Pasaporte Extranjero"));
        respuesta.add("tipos", new Objeto().set("id", "134").set("descripcion", "Documento Extranjeros en Argentina"));
        respuesta.add("tipos", new Objeto().set("id", "135").set("descripcion", "Documento Extranjeros"));
        return respuesta;
    }

    public static RespuestaMB tiposSexos() {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add("tipos", new Objeto().set("id", "M").set("descripcion", "Hombre"));
        respuesta.add("tipos", new Objeto().set("id", "F").set("descripcion", "Mujer"));
        return respuesta;
    }

    public static RespuestaMB tokenSesion(ContextoMB contexto) {
        String token = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
        String cuil = contexto.persona().cuit();

        SqlResponseMB sqlTokenDeposito = DepositoService.guardarTokenDeposito(contexto, token, cuil);
        if (sqlTokenDeposito.hayError) {
            return RespuestaMB.error();
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add("token", token);
        return respuesta;
    }

    public static RespuestaMB loginToken(ContextoMB contexto) {
        String token = contexto.parametros.string("token");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        contexto.eliminarSesion(false, "");

        if (Objeto.anyEmpty(token)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // Obtengo credenciales
        SqlResponseMB cuilDeposito = DepositoService.selectCuilDeposito(contexto, token);
        if (cuilDeposito.hayError) {
            return RespuestaMB.error();
        }
        if (cuilDeposito.registros.isEmpty()) {
            return RespuestaMB.estado("TOKEN_INVALIDO");
        }
        String cuil = cuilDeposito.registros.get(0).string("cuil").trim();
        String idTipoDocumento = "08"; // contexto.parametros.string("idTipoDocumento", null);
        String idSexo = null; // contexto.parametros.string("idSexo", null);
        contexto.parametros.set("documento", cuil);
        contexto.parametros.set("idTipoDocumento", idTipoDocumento);

        // Elimino token
        DepositoService.eliminarTokenDeposito(contexto, token);

        List<String> listaIdCobis = RestPersona.listaIdCobisParaCuil(contexto, cuil, idTipoDocumento, idSexo);
        if (listaIdCobis == null) {
            idTipoDocumento = "11";
            contexto.parametros.set("idTipoDocumento", idTipoDocumento);
            listaIdCobis = RestPersona.listaIdCobisParaCuil(contexto, cuil, idTipoDocumento, idSexo);
        }
        if (listaIdCobis == null) {
            idTipoDocumento = "09";
            contexto.parametros.set("idTipoDocumento", idTipoDocumento);
            listaIdCobis = RestPersona.listaIdCobisParaCuil(contexto, cuil, idTipoDocumento, idSexo);
        }
        if (listaIdCobis == null) {
            idTipoDocumento = "12";
            contexto.parametros.set("idTipoDocumento", idTipoDocumento);
            listaIdCobis = RestPersona.listaIdCobisParaCuil(contexto, cuil, idTipoDocumento, idSexo);
        }

        if (listaIdCobis == null) {
            return RespuestaMB.error();
        } else if (listaIdCobis.isEmpty()) {
            return RespuestaMB.estado("USUARIO_INVALIDO");
        } else if (listaIdCobis.size() > 1) {
            return RespuestaMB.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        String idCobis = listaIdCobis.get(0);
        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);
        registrarHeaders(contexto, "loginToken");

        // Verifico sesion duplicada
        if (ConfigMB.bool("validar_sesion_duplicada", false)) {
            if (SqlHomebanking.existeSesion(idCobis, fingerprint)) {
                contexto.eliminarSesion(false, idCobis);
                return RespuestaMB.estado("EXISTE_SESION");
            }
        }

        // Respuesta
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.unir(MBAplicacion.configuracionUsuario(contexto, idCobis));
        Objeto datos = new Objeto();
        datos.set("cuit", cuil);
        respuesta.unir(RespuestaMB.exito("persona", datos));

        // si esto tira un error no debe impedir que el usuario se loguee
        try {
            if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto))
                new Futuro<>(() -> salesforceIngresoUsuario(contexto));
            else if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones",
                    "prendido_alerta_notificaciones_cobis")) {
                new Futuro<>(() -> mailIngresoUsuario(contexto, respuesta.string("persona.cuit"),
                        respuesta.string("persona.apellido"), respuesta.string("persona.nombre")));
            }
        } catch (Exception e) {
        }

        contexto.sesion().setUsuarioLogueado(true);
        respuesta.set("hash", contexto.hashSesion());
        return respuesta;
    }

    public static RespuestaMB preguntasRiesgoNet(ContextoMB contexto) {
        contexto.sesion().clearRespuestasRiesgoNet();
/*
        Boolean bloqueadoRiesgoNet = bloqueadoRiesgoNet(contexto);
        if (bloqueadoRiesgoNet) {
            return RespuestaMB.estado("BLOQUEADO_RIESGONET");
        }

        Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
        if (domicilioPostal == null) {
            domicilioPostal = new Objeto();
        }

        ApiResponseMB response = SqlLogin.preguntasRiesgoNet(contexto, domicilioPostal);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        if (filtraPreguntasRiesgoNet(contexto, response, respuesta) == 0) {
            return RespuestaMB.estado("RECHAZADO_RIESGONET");
        }

        contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "P");
        */

        return RespuestaMB.error();
    }

    private static Integer filtraPreguntasRiesgoNet(ContextoMB contexto, ApiResponseMB response, Objeto respuesta) {
        int numeroPregunta = 0;
        int numeroOpcion = 0;

        for (Objeto item0 : response.objetos()) {
            for (Objeto item1 : item0.objetos("oPreguntas")) {
                for (Objeto item2 : item1.objetos("preguntas")) {
                    for (Objeto item3 : item2.objetos("Pregunta")) {
                        String correcta = item3.objetos("ok").get(0).string("codigo");
                        Objeto pregunta = new Objeto();
                        pregunta.set("id", ++numeroPregunta);
                        pregunta.set("enunciado", item3.string("enunciado"));
                        for (Objeto item4 : item3.objetos("opciones")) {
                            for (Objeto item5 : item4.objetos("opcion")) {
                                Integer id = ++numeroOpcion;
                                Boolean esCorrecta = correcta.equalsIgnoreCase(item5.string("codigo"));
                                Objeto opcion = new Objeto();
                                opcion.set("id", id.toString());
                                opcion.set("enunciado", item5.string("opcion1"));
                                pregunta.add("opciones", opcion);
                                if (esCorrecta) {
                                    contexto.sesion().setRespuestaRiesgoNet(numeroPregunta, id);
                                }
                            }
                        }
                        if (!ConfigMB.esProduccion()) {
                            respuesta.set("respuestas", contexto.sesion().respuestasRiesgoNet());
                        }
                        respuesta.add("preguntas", pregunta);
                        numeroOpcion = 0;
                    }
                }
            }
        }
        return numeroPregunta;

    }

    public static RespuestaMB responderPreguntasRiesgoNet(ContextoMB contexto) {
        Objeto respuestas = (Objeto) contexto.parametros.get("respuestas");

        Boolean bloqueadoRiesgoNet = bloqueadoRiesgoNet(contexto);
        if (bloqueadoRiesgoNet) {
            return RespuestaMB.estado("BLOQUEADO_RIESGONET");
        }

        int cantidadRespuestasCorrectas = 0;
        for (Integer idPregunta : contexto.sesion().respuestasRiesgoNet().keySet()) {
            for (Objeto item : respuestas.objetos()) {
                if (idPregunta.equals(item.integer("idPregunta"))) {
                    Integer idRespuesta = contexto.sesion().respuestasRiesgoNet().get(idPregunta);
                    cantidadRespuestasCorrectas += idRespuesta.equals(item.integer("idRespuesta")) ? 1 : 0;
                    break;
                }
            }
        }

        boolean validaRiesgoNet = false;
        int cantidadRespuestasIncorrectas = 5 - cantidadRespuestasCorrectas;

        if (cantidadRespuestasCorrectas < ConfigMB.integer("cantidad_respuestas_correctas_riesgonet", 4)) {
            SqlLogin.insertRiesgoNet(contexto, cantidadRespuestasIncorrectas);
            bloqueadoRiesgoNet = bloqueadoRiesgoNet(contexto);
            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "R");
            if (bloqueadoRiesgoNet) {
                return RespuestaMB.estado("BLOQUEADO_RIESGONET");
            }
        } else {
            validaRiesgoNet = true;
            contexto.sesion().setValidaRiesgoNet(true);
            SqlLogin.deleteRiesgoNet(contexto);
            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "A");
        }

        return validaRiesgoNet ? RespuestaMB.exito() : RespuestaMB.estado("RESPUESTAS_INCORRECTAS");
    }

    private static String seteaErrorClave(ApiResponseMB validarClave) {
        String error = "ERROR";
        error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO"
                : error;
        error = validarClave.string("detalle").contains("Clave incorrecta") ? "USUARIO_INVALIDO" : error;
        error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
        error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded")
                ? "USUARIO_BLOQUEADO"
                : error;
        error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

        error = validarClave.string("mensajeAlUsuario").contains("The password authentication failed")
                ? "USUARIO_INVALIDO"
                : error;
        error = validarClave.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
        error = validarClave.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded")
                ? "USUARIO_BLOQUEADO"
                : error;
        error = validarClave.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

        return error;
    }

    private static LoginToken crearLoginToken(String idCobis) {
        LoginToken token = new LoginToken();
        token.setUuid(UUID.randomUUID().toString());
        token.setFecha(LocalDateTime.now());
        token.setIdCobis(idCobis);
        return token;
    }

    public static Boolean bloqueadoRiesgoNet(ContextoMB contexto) {
        Integer cantidadIntentosBloqueo = ConfigMB.integer("cantidad_intentos_bloqueo_riesgonet", 3);
        Integer cantidadIntentosFallidos = SqlLogin.selectRiesgoNet(contexto);
        return cantidadIntentosFallidos >= cantidadIntentosBloqueo;
    }

    public static String registrarHeaders(ContextoMB contexto, String origen) {
        if (!ConfigMB.bool("activar_log_header", false))
            return "";
        try {
            Map<String, Object> headers = new LinkedHashMap<>(contexto.headers());

            Objeto extra = new Objeto();
            extra.set("origen", origen);
            extra.set("ip", contexto.ip());

            ApiRequestMB apiRequest = ApiMB.request("auditor", "auditor", "POST", "/v1/reportes", contexto);
            apiRequest.permitirSinLogin = true;
            apiRequest.habilitarLog = false;
            apiRequest.body.set("canal", "MB");
            apiRequest.body.set("subCanal", "BACUNI");
            apiRequest.body.set("usuario", contexto.sesion().idCobis());
            apiRequest.body.set("idProceso", Util.idProceso());
            apiRequest.body.set("sesion", String.valueOf(contexto.sesion().id().hashCode()).replace("-", ""));
            apiRequest.body.set("servicio", "API-MB_headers");
            apiRequest.body.set("resultado", "200");
            apiRequest.body.set("duracion", 1L);
            Objeto mensajes = apiRequest.body.set("mensajes");
            mensajes.set("entrada", Objeto.fromMap(headers).toJson());
            mensajes.set("salida", extra.toJson());
            ApiMB.response(apiRequest);
        } catch (Exception e) {
        }
        return "";
    }

    /* Inicio Refactor */

    /* Inicio Constantes */

    private static final String VE_VALIDAR_SESION_DUPLICADA = "validar_sesion_duplicada";
    private static final String VE_DESHABILITAR_ISVA = "deshabilitar_isva";
    private static final String VE_MANTENIMIENTO = "mantenimiento";
    private static final String VE_PRENDIDO_PRUEBA_CAMBIO_CLAVE = "prendido_prueba_cambio_clave";
    private static final String VE_GUARDAR_ULTIMA_CONEXION = "guardar_ultima_conexion";
    private static final String VE_MODO_ULTIMA_CONEXION = "modo_ultima_conexion";
    private static final String ERROR_CONSULTA_USUARIO_ISVA = "ERROR_CONSULTA_USUARIO_ISVA";
    private static final String EN_MANTENIMIENTO = "EN_MANTENIMIENTO";
    private static final String EXISTE_SESION = "EXISTE_SESION";
    private static final String USUARIO_BLOQUEADO_POR_FRAUDE = "USUARIO_BLOQUEADO_POR_FRAUDE";
    private static final String USUARIO_BLOQUEADO_POR_FRAUDE_LINK = "USUARIO_BLOQUEADO_POR_FRAUDE_LINK";
    private static final String NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA = "NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA";
    private static final String ERROR_REFRESS_TOKEN = "ERROR_REFRESS_TOKEN";
    private static final String ERROR_REFRESS_TOKEN_BD = "ERROR_REFRESS_TOKEN_BD";
    private static final String ERROR_USUARIO_INVALIDO = "ERROR_USUARIO_INVALIDO";
    private static final String ERROR_USUARIO_NO_ENROLADO = "ERROR_USUARIO_NO_ENROLADO";
    private static final String TRANSACCION_COMPLETADA = "TRANSACCION_COMPLETADA";
    private static final String ERROR = "ERROR";
    private static final String ERROR_BIOMETRIA_BUHOFACIL_INACTIVA = "ERROR_BIOMETRIA_BUHOFACIL_INACTIVA";
    private static final String ACCESO = "A001";

    /* Fin Constantes */

    /* Inicio Métodos Públicos */

    public static RespuestaMB loginBiometria(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String dispositivo = contexto.parametros.string("dispositivo");
        String clave = contexto.parametros.string("clave", null);
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        String tipoMetodo = contexto.parametros.string("metodo", "");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        RespuestaMB respuesta;

        eliminarSesion(contexto, false, "");
        contexto.sesion();

        respuesta = validarVariablesEntorno();
        if (!Objeto.empty(respuesta))
            return respuesta;

        if (Objeto.anyEmpty(documento, dispositivo))
            return RespuestaMB.parametrosIncorrectos();


        RespuestaMB respuestaCobis = obtenerIdCobis(contexto, documento, idTipoDocumento, idSexo);
        if (respuestaCobis.hayError())
            return respuestaCobis;

        String idCobis = respuestaCobis.string("cobis");

        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);
        setearUsuarioLogueado(contexto);

        if (ConfigMB.bool("prender_super_cache", ConfigMB.esARO()))
            supercache(contexto);

        Boolean validarSesionDuplicada = ConfigMB.bool("validar_sesion_duplicada_aro", false);

        contexto.requestHeader("interno", "true");
        Futuro<RespuestaMB> futuroPersona = new Futuro<>(() -> obtenerPersona(contexto));
        Futuro<RespuestaMB> futuroValidarSesionDuplicada = validarSesionDuplicada ? new Futuro<>(() -> validarSesionDuplicada(contexto, fingerprint)) : null;
        Futuro<RespuestaMB> futuroVerificarBloqueos = new Futuro<>(() -> verificarBloqueos(contexto));
        Futuro<RespuestaMB> futuroVerificarAccesosNew = new Futuro<>(() -> verificaAccesosNew(contexto, dispositivo));
        Futuro<Boolean> futuroTokenModo = new Futuro<>(() -> generarTokenModo(contexto));
        futuroVerificarAccesosNew.get();
        Futuro<RespuestaMB> futuroConsultarUsuarioIsva = new Futuro<>(() -> MBBiometria.consultarUsuarioIsva(contexto));

        respuesta = futuroPersona.get();
        if (respuesta.hayError())
            return respuesta;

        Objeto persona = respuesta.objeto("persona");
        new Futuro<>(() -> registrarHeaders(contexto, "biometria"));
        Futuro<RespuestaMB> futuroExito = new Futuro<>(() -> exito(contexto, buscarPersona ? persona : null));

        if (validarSesionDuplicada) {
            respuesta = futuroValidarSesionDuplicada.get();
            if (!Objeto.empty(respuesta))
                return respuesta;
        }

        respuesta = futuroVerificarBloqueos.get();
        if (!Objeto.empty(respuesta))
            return respuesta;

        respuesta = futuroVerificarAccesosNew.get();

        if (respuesta.hayError())
            return contingenciaErrorIsvaAccesos(contexto, respuesta);

        boolean biometriaActiva = respuesta.bool("biometriaActiva");
        boolean buhoFacilActivo = respuesta.bool("buhoFacilActivo");

        respuesta = futuroConsultarUsuarioIsva.get();
        if (respuesta.hayError())
            return respuesta;

        if (Objeto.empty(respuesta.objeto("authenticators")))
            return RespuestaMB.estado(ERROR_USUARIO_INVALIDO);

        IsvaDTO isvaDTO = new IsvaDTO();
        respuesta = validarBiometriaClave(contexto, respuesta, clave, biometriaActiva, buhoFacilActivo, fingerprint, dispositivo, tipoMetodo, isvaDTO);
        if (respuesta.hayError())
            return respuesta;

        String metodo = respuesta.string("metodo");

        logBiometria(contexto, tipoMetodo, metodo, dispositivo, contexto.sesion().cache("token_biometria"));
        if (validarSesionDuplicada)
            registrarSesion(contexto, fingerprint);

        registrarUltimaFechaHoraConexion(contexto);
        insertarLogBiometria(contexto, tipoMetodo, respuesta.string("estado"));
        enviarEmailIngresoUsuario(contexto, persona.string("cuit"), persona.string("apellido"), persona.string("nombre"));

        futuroTokenModo.tryGet();

        return exito(contexto, futuroExito.get(), metodo);
    }

    public static RespuestaMB loginNew(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");
        String idSexo = contexto.parametros.string("idSexo", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        Boolean buscarProductos = contexto.parametros.bool("buscarProductos", false);
        Boolean eliminarSesion = contexto.parametros.bool("eliminarSesion", false);

        // TODO quitar del login acumar y consolidada productos
        buscarProductos = false;

        contexto.eliminarSesion(false, "");

        if (Objeto.anyEmpty(documento, usuario, clave)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Boolean falsoLogin = !ConfigMB.esProduccion() && clave.equals("0");

        if (ConfigMB.bool("mantenimiento", false)) {
            return RespuestaMB.estado("EN_MANTENIMIENTO");
        }

        // idCobis
        String idCobis = falsoLogin ? usuario : EMBLogin.clientes.get(documento);
        if (idCobis == null || idCobis.isEmpty()) {
            List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, documento, null, idSexo);
            if (listaIdCobis == null) {
                return RespuestaMB.error();
            }
            if (listaIdCobis.isEmpty()) {
                return RespuestaMB.estado("USUARIO_INVALIDO");
            }
            if (listaIdCobis.size() > 1) {
                return RespuestaMB.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
            }
            idCobis = listaIdCobis.get(0);
        }
        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);
        registrarHeaders(contexto, "loginNew");

        if (eliminarSesion) {
            SqlHomebanking.eliminarSesion(idCobis);
        }

        Boolean superCache = ConfigMB.bool("prender_super_cache", ConfigMB.esARO());
        if (superCache) {
            supercache(contexto);
        }

        Boolean validarSesionDuplicada = ConfigMB.bool("validar_sesion_duplicada_aro", false) && !falsoLogin;

        String idCobisFinal = idCobis;

        contexto.requestHeader("interno", "true");
        Futuro<Boolean> futuroEsUsuarioMarcado = new Futuro<>(() -> EMBLogin.esUsuarioMarcado(idCobisFinal));
        Futuro<Boolean> futuroBloqueadoPorFraude = new Futuro<>(() -> SqlHomebanking.bloqueadoPorFraude(idCobisFinal));
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = new Futuro<>(() -> SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobisFinal));
        Futuro<ApiResponseMB> futuroValidarUsuario = falsoLogin ? null : new Futuro<>(() -> RestSeguridad.validarUsuario(contexto, usuario, fingerprint));
        Futuro<ApiResponseMB> futuroValidarClave = falsoLogin ? null : new Futuro<>(() -> RestSeguridad.validarClavev2(contexto, clave, fingerprint));
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(() -> SqlHomebanking.fechaHoraUltimaConextion(contexto.idCobis()));
        Futuro<RespuestaMB> futuroPersona = buscarPersona && !falsoLogin ? new Futuro<>(() -> MBPersona.persona(contexto)) : null;
        Futuro<RespuestaMB> futuroProductos = buscarProductos ? new Futuro<>(() -> MBProducto.productos(contexto)) : null;
        Futuro<Boolean> futuroConsultaTokenModo = new Futuro<>(() -> MBModo.consultTokensModo(contexto));
        Futuro<Boolean> futuroExisteSesion = validarSesionDuplicada ? new Futuro<>(() -> SqlHomebanking.existeSesion(idCobisFinal, fingerprint)) : null;
        if (buscarPersona && falsoLogin) {
            futuroPersona = new Futuro<>(() -> MBPersona.cliente(contexto));
        }

        // Desarrollo para cambio de usuario forzoso para algunos Cobis seleccionados
        Boolean esUsuarioMarcado = futuroEsUsuarioMarcado.get();
        if (esUsuarioMarcado) {
            Objeto consultaForzar = EMBLogin.datosCambioUsuario(idCobis);
            if (consultaForzar != null && !consultaForzar.bool("realizado")) {
                Boolean consultaST = consultaForzar.bool("token");
                if (consultaST != null && consultaST) {
                    return RespuestaMB.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", true);
                } else {
                    return RespuestaMB.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", false);
                }
            }
        }

        // TODO: BANMOVBACK-226
        if (futuroBloqueadoPorFraude.get()) {
            contexto.eliminarSesion(false, idCobis);
            return RespuestaMB.estado("USUARIO_BLOQUEADO_POR_FRAUDE");
        }

        if (futuroBloqueadoPorTransaccionFraudeLink.get()) {
            contexto.eliminarSesion(false, idCobis);
            return RespuestaMB.estado("USUARIO_BLOQUEADO_POR_FRAUDE_LINK");
        }
        // Verifico sesion duplicada
        if (validarSesionDuplicada) {
            if (futuroExisteSesion.get()) {
                contexto.eliminarSesion(false, idCobis);
                return RespuestaMB.estado("EXISTE_SESION");
            }
        }

        if (!falsoLogin) {
            ApiResponseMB validarUsuario = futuroValidarUsuario.get();
            if (validarUsuario.hayError()) {
                String error = "ERROR";
                error = validarUsuario.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
                error = validarUsuario.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
                error = validarUsuario.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
                error = validarUsuario.string("detalle").contains("password has expired") ? "USUARIO_EXPIRADO" : error;
                error = validarUsuario.string("mensajeAlUsuario").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
                error = validarUsuario.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
                error = validarUsuario.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
                error = validarUsuario.string("mensajeAlUsuario").contains("password has expired") ? "USUARIO_EXPIRADO" : error;
                return RespuestaMB.estado(error);
            }

            ApiResponseMB validarClave = futuroValidarClave.get();
            if (validarClave.hayError()) {
                String error = "ERROR";
                error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
                error = validarClave.string("detalle").contains("Clave incorrecta") ? "USUARIO_INVALIDO" : error;
                error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
                error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
                error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;
                error = validarClave.string("mensajeAlUsuario").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
                error = validarClave.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
                error = validarClave.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
                error = validarClave.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA" : error;
                if (validarClave.string("mensajeAlDesarrollador").contains("Clave incorrecta")) {
                    String mensajeCanal = validarClave.string("mensajeAlDesarrollador").replace("Clave incorrecta. Canal ", "");
                    String canal = mensajeCanal.split("-")[0].trim();
                    String fechaCambio = mensajeCanal.split("-")[1].trim();
                    return RespuestaMB.estado(error).set("canal", canal).set("fechaCambio", fechaCambio);
                }

                return RespuestaMB.estado(error);
            }
        }

        // Fecha hora ultima conexion
        // TODO: BANMOVBACK-226x
        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.get();
        contexto.sesion().setFechaHoraUltimaConexion(fechaHoraUltimaConexion);
        new Futuro<>(() -> SqlHomebanking.registrarFechaHoraUltimaConextion(idCobisFinal, ConfigMB.string("modo_ultima_conexion", "rowlock")));

        // Registrar Sesion BD
        // TODO: BANMOVBACK-226
        if (validarSesionDuplicada) {
            new Futuro<>(() -> SqlHomebanking.registrarSesion(idCobisFinal, fingerprint, contexto.idSesion()));
        }

        // Respuesta
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.unir(MBAplicacion.configuracionUsuario(contexto, idCobis));
        if (buscarPersona) {
            respuesta.unir(futuroPersona.get());
        }
        if (buscarProductos) {
            respuesta.unir(futuroProductos.get());
        }

        // si esto tira un error no debe impedir que el usuario se loguee
        try {
            if (MBSalesforce.prendidoSalesforce(contexto.idCobis()))
                new Futuro<>(() -> salesforceIngresoUsuario(contexto));
            else if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones", "prendido_alerta_notificaciones_cobis")) {
                new Futuro<>(() -> mailIngresoUsuario(contexto, respuesta.string("persona.cuit"), respuesta.string("persona.apellido"), respuesta.string("persona.nombre")));
            }
        } catch (Exception e) {
        }

        // Hago llamada a generador de token
        LoginToken token = crearLoginToken(contexto.idCobis());
        respuesta.set("token", token.getUuid());
        if (!SqlMobile.persistirToken(token)) {
            return RespuestaMB.estado("ERROR_GENERACION_TOKEN");
        }

        contexto.sesion().setUsuarioLogueado(true);
        futuroConsultaTokenModo.get();
        respuesta.set("hash", contexto.hashSesion());

        try {
            String ip = contexto.ip();
            new Futuro<>(() -> contexto.insertarLogLogin(ip));
        } catch (Exception e) {
            //
        }

        respuesta.set("mostrarTcv", false);
        /*
        try {
            respuesta.set("mostrarTcv",
                    contexto.logsLogin().size() <= 1
                            && MBPersona.tieneAltaTcv(contexto)
                            && !contexto.tarjetasCredito().isEmpty()
            );
        } catch (Exception e) {
        }
        */

        return respuesta.set("ID", Encriptador.sha256(contexto.idCobis()));
    }

    public static RespuestaMB biometria(ContextoMB contexto) {
        return loginBiometria(contexto);
    }

    /* Fin Métodos Públicos */

    /* Inicio Métodos Privados */

    private static RespuestaMB obtenerPersona(ContextoMB contexto) {
        return MBPersona.persona(contexto);
    }

    private static RespuestaMB validarSesionDuplicada(ContextoMB contexto, String fingerprint) {
        if (ConfigMB.bool(VE_VALIDAR_SESION_DUPLICADA, false) && !"true".equals(contexto.parametros.string("ignorarSesionDuplicada"))) {
            if (SqlHomebanking.existeSesion(contexto.idCobis(), fingerprint)) {
                contexto.eliminarSesion(false, "");
                return RespuestaMB.estado(EXISTE_SESION);
            }
        }
        return null;
    }

    private static RespuestaMB verificarBloqueos(ContextoMB contexto) {
        Futuro<Boolean> futuroBloqueadoPorFraude = new Futuro<>(
                () -> SqlHomebanking.bloqueadoPorFraude(contexto.idCobis()));
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = new Futuro<>(
                () -> SqlHomebanking.bloqueadoPorTransaccionFraudeLink(contexto.idCobis()));

        if (futuroBloqueadoPorFraude.get()) {
            contexto.eliminarSesion(false, "");
            return RespuestaMB.estado(USUARIO_BLOQUEADO_POR_FRAUDE);
        }

        if (futuroBloqueadoPorTransaccionFraudeLink.get()) {
            contexto.eliminarSesion(false, "");
            return RespuestaMB.estado(USUARIO_BLOQUEADO_POR_FRAUDE_LINK);
        }

        return null;
    }

    private static void registrarSesion(ContextoMB contexto, String fingerprint) {
        try {
            new Futuro<>(() -> SqlHomebanking.registrarSesion(contexto.idCobis(), fingerprint, contexto.idSesion()));
        } catch (Exception e) {
        }
    }

    private static RespuestaMB limpiarCacheErrorIsvaAccesos(ContextoMB contexto) {
        contexto.sesion().delCache("token_biometria");
        contexto.sesion().delCache("dispositivo");

        return RespuestaMB.estado(NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA);
    }

    private static RespuestaMB contingenciaErrorIsvaAccesos(ContextoMB contexto, RespuestaMB respuestaAccesos) {
        try {
            if (respuestaAccesos.string("estado").equals(NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA)
                    || respuestaAccesos.string("estado").equals(ERROR_REFRESS_TOKEN)
                    || respuestaAccesos.string("estado").equals(ERROR_REFRESS_TOKEN_BD)) {
                MBBiometria.revocarBiometriaNew(contexto);
                return limpiarCacheErrorIsvaAccesos(contexto);
            }
        } catch (Exception e) {
            return limpiarCacheErrorIsvaAccesos(contexto);
        }

        return respuestaAccesos;
    }

    private static RespuestaMB validarBiometriaClave(ContextoMB contexto, RespuestaMB respuesta, String clave,
                                                     boolean biometriaActiva, boolean buhoFacilActivo,
                                                     String fingerprint, String dispositivo, String tipoMetodo, IsvaDTO isvaDTO) {
        if (Objeto.empty(clave) && biometriaActiva) {
            return validarBiometria(contexto, respuesta, tipoMetodo, dispositivo, fingerprint, isvaDTO);
        } else if (!Objeto.empty(clave) && buhoFacilActivo) {
            return validarClave(contexto, clave, fingerprint, tipoMetodo);
        } else
            return RespuestaMB.estado(ERROR_BIOMETRIA_BUHOFACIL_INACTIVA);
    }

    private static RespuestaMB validarBiometria(ContextoMB contexto, RespuestaMB respuesta, String tipoMetodo,
                                                String dispositivo, String fingerprint, IsvaDTO isvaDTO) {
        String idCobis = contexto.idCobis();
        String ip = contexto.ip();
        String refreshToken = contexto.sesion().cache("refresh_token_biometria");

        if (MBBiometria.validaEnrolamiento(tipoMetodo, respuesta)) {
            respuesta = MBBiometria.setearTransaccionDispositivo(contexto, contexto.sesion().cache("token_biometria"),
                    dispositivo,
                    fingerprint, isvaDTO);
            if (!respuesta.string("estado").equalsIgnoreCase(TRANSACCION_COMPLETADA)) {
                new SqlBiometriaService().biometriaInsertLog(ACCESO, tipoMetodo,
                        respuesta.string("estado"), idCobis, dispositivo, refreshToken, ip);
                return respuesta;
            }
        } else
            return RespuestaMB.estado(ERROR_USUARIO_NO_ENROLADO);

        return RespuestaMB.exito().set("metodo", "biometriaActiva");
    }

    private static RespuestaMB validarClave(ContextoMB contexto, String clave, String fingerprint, String tipoMetodo) {
        ApiResponseMB apiResponse = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                VE_PRENDIDO_PRUEBA_CAMBIO_CLAVE)
                ? RestSeguridad.validarClavev2(contexto, clave, fingerprint)
                : RestSeguridad.validarClave(contexto, clave, fingerprint);

        if (apiResponse.hayError()) {
            String error = seteaErrorClave(apiResponse);
            if (!ERROR.equalsIgnoreCase(error)) {
                String idCobis = contexto.idCobis();
                String ip = contexto.ip();
                String refreshToken = contexto.sesion().cache("refresh_token_biometria");
                String dispositivo = contexto.sesion().cache("dispositivo");
                new SqlBiometriaService().biometriaInsertLog(ACCESO, tipoMetodo, error, idCobis, dispositivo,
                        refreshToken, ip);
            }
            if (apiResponse.string("mensajeAlDesarrollador").contains("Clave incorrecta")) {
                String mensajeCanal = apiResponse.string("mensajeAlDesarrollador")
                        .replace("Clave incorrecta. Canal ", "");
                return RespuestaMB.estado(error).set("canal", mensajeCanal.split("-")[0].trim()).set("fechaCambio",
                        mensajeCanal.split("-")[1].trim());
            }
            return RespuestaMB.estado(error);
        }

        return RespuestaMB.exito().set("metodo", "buhoFacilActivo");
    }

    private static boolean setearRegistrarUltimaFechaHoraConexion(ContextoMB contexto) {
        try {
            contexto.sesion().setFechaHoraUltimaConexion(SqlHomebanking.fechaHoraUltimaConextion(contexto.idCobis()));
            SqlHomebanking.registrarFechaHoraUltimaConextion(contexto.idCobis(),
                    ConfigMB.string(VE_MODO_ULTIMA_CONEXION, "rowlock"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void registrarUltimaFechaHoraConexion(ContextoMB contexto) {
        if (ConfigMB.bool(VE_GUARDAR_ULTIMA_CONEXION, false)) {
            try {
                new Futuro<>(() -> setearRegistrarUltimaFechaHoraConexion(contexto));
            } catch (Exception e) {
            }
        }
    }

    private static void enviarEmailIngresoUsuario(ContextoMB contexto, String cuit, String apellido, String nombre) {
        try {
            if (MBSalesforce.prendidoSalesforce(contexto.idCobis()))
                new Futuro<>(() -> salesforceIngresoUsuario(contexto));
            else
                new Futuro<>(() -> RestNotificaciones.enviarMailIngreso(contexto, cuit, apellido, nombre));
        } catch (Exception e) {
        }
    }

    private static RespuestaMB exito(ContextoMB contexto, Objeto persona) {
        RespuestaMB respuesta = new RespuestaMB().unir(MBAplicacion.configuracionUsuario(contexto, contexto.idCobis()));
        if (!Objeto.empty(persona))
            respuesta.set("persona", persona);
        return respuesta;
    }

    private static RespuestaMB exito(ContextoMB contexto, RespuestaMB respuesta, String metodo) {
        String expires = contexto.sesion().cache("expires_in_biometria");
        expires = expires == null ? "0" : expires;
        return respuesta.set("accesso", metodo).set("expires_token", Integer.parseInt(expires))
                .set("ID", Encriptador.sha256(contexto.idCobis()))
                .set("usuarioMigrarBiometria", contexto.usuarioMigrarBiometria);
    }

    private static void setearUsuarioLogueado(ContextoMB contexto) {
        contexto.sesion().setUsuarioLogueado(true);
    }

    private static boolean generarTokenModo(ContextoMB contexto) {
        try {
            MBModo.consultTokensModo(contexto);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void insertarLogBiometria(ContextoMB contexto, String tipoMetodo, String estado) {
        try {
            String idCobis = contexto.idCobis();
            String ip = contexto.ip();
            String refreshToken = contexto.sesion().cache("refresh_token_biometria");
            String dispositivo = contexto.sesion().cache("dispositivo");
            new Futuro<>(() -> new SqlBiometriaService().biometriaInsertLog(ACCESO, tipoMetodo, estado, idCobis,
                    dispositivo, refreshToken, ip));
        } catch (Exception e) {
        }
    }

    private static void eliminarSesion(ContextoMB contexto, boolean eliminarBaseDatos, String idCobis) {
        contexto.eliminarSesion(eliminarBaseDatos, idCobis);
    }

    private static void logBiometria(ContextoMB contexto, String tipoMetodo, String metodo, String dispositivo,
                                     String tokenBiometria) {
        try {
            new Futuro<>(() -> MBBiometria.loginLog(contexto, tipoMetodo, metodo, dispositivo, tokenBiometria));
        } catch (Exception e) {
        }
    }

    private static RespuestaMB verificaAccesosNew(ContextoMB contexto, String dispositivo) {
        contexto.parametros.set("dispositivo", dispositivo);
        RespuestaMB respuesta = MBBiometria.verificaAccesosCompletos(contexto);
        if (respuesta.hayError())
            return respuesta;

        Boolean biometriaActiva = respuesta.string("biometriaActiva").equalsIgnoreCase("1");
        Boolean buhoFacilActivo = respuesta.string("buhoFacilActivo").equalsIgnoreCase("1");

        String token = respuesta.string("accessToken");
        String refreshToken = respuesta.string("refreshToken");
        String fechaTokenString = respuesta.string("fechaToken");
        Integer expiresIn = 0;

        if (MBBiometria.verificaAccessToken(contexto, respuesta.date("fechaToken"))) {
            respuesta = MBBiometria.refreshTokensNew(contexto, refreshToken);
            if (respuesta.hayError())
                return respuesta;
            token = respuesta.string("access_token");
            refreshToken = respuesta.string("refresh_token");
            expiresIn = respuesta.integer("expires_in");
            fechaTokenString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ss").format(new Date());
        }

        contexto.sesion().setCache("token_biometria", token);
        contexto.sesion().setCache("refresh_token_biometria", refreshToken);
        contexto.sesion().setCache("fecha_token_biometria", fechaTokenString);
        contexto.sesion().setCache("expires_in_biometria", expiresIn.toString());
        contexto.sesion().setCache("dispositivo", dispositivo);

        return RespuestaMB.exito().set("biometriaActiva", biometriaActiva).set("buhoFacilActivo", buhoFacilActivo);
    }

    private static RespuestaMB validarVariablesEntorno() {
        if (ConfigMB.bool(VE_DESHABILITAR_ISVA, false))
            return RespuestaMB.estado(ERROR_CONSULTA_USUARIO_ISVA);

        if (ConfigMB.bool(VE_MANTENIMIENTO, false))
            return RespuestaMB.estado(EN_MANTENIMIENTO);

        return null;
    }

    /* Fin Métodos Privados */

    /* Fin Refactor */


    /* INICIO TRANSMIT */

    public static RespuestaMB login(ContextoMB contexto) {
        if (Util.migracionCompleta())
            return loginTransmit(contexto);
        return loginIsva(contexto);
    }

    public static RespuestaMB loginBiometriaTransmit(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String dispositivo = contexto.parametros.string("dispositivo");
        String clave = contexto.parametros.string("clave", null);
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        String tipoMetodo = contexto.parametros.string("metodo", "");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        RespuestaMB respuesta;

        eliminarSesion(contexto, false, "");
        contexto.sesion();

        respuesta = validarVariablesEntorno();
        if (!Objeto.empty(respuesta))
            return respuesta;

        if (Objeto.anyEmpty(documento))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaCobis = obtenerIdCobis(contexto, documento, idTipoDocumento, idSexo);
        if (respuestaCobis.hayError())
            return respuestaCobis;

        String idCobis = respuestaCobis.string("cobis");

        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);
        setearUsuarioLogueado(contexto);

        if (Util.migracionPrendida()) {
            RespuestaMB respuestaChequearMigracion = chequearMigracion(contexto, documento, idSexo, fingerprint, true);
            if (respuestaChequearMigracion != null)
                return respuestaChequearMigracion;
        }

        if (Objeto.anyEmpty(dispositivo))
            return RespuestaMB.parametrosIncorrectos();

        if (ConfigMB.bool("prender_super_cache", ConfigMB.esARO()))
            supercache(contexto);

        Boolean validarSesionDuplicada = ConfigMB.bool("validar_sesion_duplicada_aro", false);

        contexto.requestHeader("interno", "true");
        Futuro<RespuestaMB> futuroPersona = new Futuro<>(() -> obtenerPersona(contexto));
        Futuro<RespuestaMB> futuroValidarSesionDuplicada = validarSesionDuplicada ? new Futuro<>(() -> validarSesionDuplicada(contexto, fingerprint)) : null;
        Futuro<RespuestaMB> futuroVerificarBloqueos = new Futuro<>(() -> verificarBloqueos(contexto));
        Futuro<RespuestaMB> futuroVerificarAccesosNew = new Futuro<>(() -> verificaAccesosNew(contexto, dispositivo));
        Futuro<Boolean> futuroTokenModo = new Futuro<>(() -> generarTokenModo(contexto));
        futuroVerificarAccesosNew.get();
        Futuro<RespuestaMB> futuroConsultarUsuarioIsva = new Futuro<>(() -> MBBiometria.consultarUsuarioIsva(contexto));

        respuesta = futuroPersona.get();
        if (respuesta.hayError())
            return respuesta;

        Objeto persona = respuesta.objeto("persona");
        new Futuro<>(() -> registrarHeaders(contexto, "biometria"));
        Futuro<RespuestaMB> futuroExito = new Futuro<>(() -> exito(contexto, buscarPersona ? persona : null));

        if (validarSesionDuplicada) {
            respuesta = futuroValidarSesionDuplicada.get();
            if (!Objeto.empty(respuesta))
                return respuesta;
        }

        respuesta = futuroVerificarBloqueos.get();
        if (!Objeto.empty(respuesta))
            return respuesta;

        respuesta = futuroVerificarAccesosNew.get();

        if (respuesta.hayError())
            return contingenciaErrorIsvaAccesos(contexto, respuesta);

        boolean biometriaActiva = respuesta.bool("biometriaActiva");
        boolean buhoFacilActivo = respuesta.bool("buhoFacilActivo");

        respuesta = futuroConsultarUsuarioIsva.get();
        if (respuesta.hayError())
            return respuesta;

        if (Objeto.empty(respuesta.objeto("authenticators")))
            return RespuestaMB.estado(ERROR_USUARIO_INVALIDO);

        IsvaDTO isvaDTO = new IsvaDTO();
        respuesta = validarBiometriaClave(contexto, respuesta, clave, biometriaActiva, buhoFacilActivo, fingerprint, dispositivo, tipoMetodo, isvaDTO);
        if (respuesta.hayError())
            return respuesta;

        String metodo = respuesta.string("metodo");

        logBiometria(contexto, tipoMetodo, metodo, dispositivo, contexto.sesion().cache("token_biometria"));
        if (validarSesionDuplicada)
            registrarSesion(contexto, fingerprint);

        registrarUltimaFechaHoraConexion(contexto);
        insertarLogBiometria(contexto, tipoMetodo, respuesta.string("estado"));
        enviarEmailIngresoUsuario(contexto, persona.string("cuit"), persona.string("apellido"), persona.string("nombre"));

        futuroTokenModo.tryGet();

        return exito(contexto, futuroExito.get(), metodo);
    }


    public static RespuestaMB loginIsva(ContextoMB contexto) {
        log.info("TRANSMIT - loginIsva");
        String documento = contexto.parametros.string("documento");
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");
        String idSexo = contexto.parametros.string("idSexo", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        Boolean eliminarSesion = contexto.parametros.bool("eliminarSesion", false);

        contexto.eliminarSesion(false, "");

        if (Objeto.anyEmpty(documento, usuario, clave))
            return RespuestaMB.parametrosIncorrectos();

        if (ConfigMB.bool("mantenimiento", false))
            return RespuestaMB.estado(Errores.EN_MANTENIMIENTO);

        RespuestaMB respuestaCobis = obtenerIdCobis(contexto, documento, null, idSexo);
        if (respuestaCobis.hayError())
            return respuestaCobis;

        String idCobis = respuestaCobis.string("cobis");

        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);

        if (Util.migracionPrendida()) {
            log.info("TRANSMIT - chequearMigacion");
            RespuestaMB respuestaChequearMigracion = chequearMigracion(contexto, documento, idSexo, fingerprint, false);
            log.info("TRANSMIT - chequearMigacion response: " + (respuestaChequearMigracion != null));
            if (respuestaChequearMigracion != null)
                return respuestaChequearMigracion;
        }
        log.info("TRANSMIT - login sin migracion");
        registrarHeaders(contexto, "loginNew");

        if (eliminarSesion)
            SqlHomebanking.eliminarSesion(idCobis);

        Boolean superCache = ConfigMB.bool("prender_super_cache", ConfigMB.esARO());
        if (superCache)
            supercache(contexto);

        boolean validarSesionDuplicada = ConfigMB.bool("validar_sesion_duplicada_aro", false);

        contexto.requestHeader("interno", "true");
        Futuro<Boolean> futuroEsUsuarioMarcado = new Futuro<>(() -> EMBLogin.esUsuarioMarcado(idCobis));
        Futuro<Boolean> futuroBloqueadoPorFraude = new Futuro<>(() -> SqlHomebanking.bloqueadoPorFraude(idCobis));
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = new Futuro<>(() -> SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobis));
        Futuro<ApiResponseMB> futuroValidarUsuario = new Futuro<>(() -> RestSeguridad.validarUsuario(contexto, usuario, fingerprint));
        Futuro<ApiResponseMB> futuroValidarClave = new Futuro<>(() -> RestSeguridad.validarClavev2(contexto, clave, fingerprint));
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(() -> SqlHomebanking.fechaHoraUltimaConextion(contexto.idCobis()));
        Futuro<RespuestaMB> futuroPersona = buscarPersona ? new Futuro<>(() -> MBPersona.persona(contexto)) : null;
        Futuro<Boolean> futuroConsultaTokenModo = new Futuro<>(() -> MBModo.consultTokensModo(contexto));
        Futuro<Boolean> futuroExisteSesion = validarSesionDuplicada ? new Futuro<>(() -> SqlHomebanking.existeSesion(idCobis, fingerprint)) : null;
        if (buscarPersona)
            futuroPersona = new Futuro<>(() -> MBPersona.cliente(contexto));

        if (futuroEsUsuarioMarcado.get()) {
            Objeto consultaForzar = EMBLogin.datosCambioUsuario(idCobis);
            if (consultaForzar != null && !consultaForzar.bool("realizado"))
                return RespuestaMB.estado("FORZAR_CAMBIO_USUARIO")
                        .set("conSoftToken", Boolean.TRUE.equals(consultaForzar.bool("token")));
        }

        if (futuroBloqueadoPorFraude.get()) {
            contexto.eliminarSesion(false, idCobis);
            return RespuestaMB.estado("USUARIO_BLOQUEADO_POR_FRAUDE");
        }

        if (futuroBloqueadoPorTransaccionFraudeLink.get()) {
            contexto.eliminarSesion(false, idCobis);
            return RespuestaMB.estado("USUARIO_BLOQUEADO_POR_FRAUDE_LINK");
        }

        if (validarSesionDuplicada) {
            if (futuroExisteSesion.get()) {
                contexto.eliminarSesion(false, idCobis);
                return RespuestaMB.estado("EXISTE_SESION");
            }
        }

        ApiResponseMB validarUsuario = futuroValidarUsuario.get();
        if (validarUsuario.hayError()) {
            String error = "ERROR";
            error = validarUsuario.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarUsuario.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("detalle").contains("password has expired") ? "USUARIO_EXPIRADO" : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("password has expired") ? "USUARIO_EXPIRADO" : error;
            return RespuestaMB.estado(error);
        }

        ApiResponseMB validarClave = futuroValidarClave.get();
        if (validarClave.hayError()) {
            String error = "ERROR";
            error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("detalle").contains("Clave incorrecta") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;
            error = validarClave.string("mensajeAlUsuario").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA" : error;
            if (validarClave.string("mensajeAlDesarrollador").contains("Clave incorrecta")) {
                String mensajeCanal = validarClave.string("mensajeAlDesarrollador").replace("Clave incorrecta. Canal ", "");
                String canal = mensajeCanal.split("-")[0].trim();
                String fechaCambio = mensajeCanal.split("-")[1].trim();
                return RespuestaMB.estado(error).set("canal", canal).set("fechaCambio", fechaCambio);
            }

            return RespuestaMB.estado(error);
        }

        // Fecha hora ultima conexion
        // TODO: BANMOVBACK-226x
        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.get();
        contexto.sesion().setFechaHoraUltimaConexion(fechaHoraUltimaConexion);
        new Futuro<>(() -> SqlHomebanking.registrarFechaHoraUltimaConextion(idCobis, ConfigMB.string("modo_ultima_conexion", "rowlock")));

        // Registrar Sesion BD
        // TODO: BANMOVBACK-226
        if (validarSesionDuplicada) {
            new Futuro<>(() -> SqlHomebanking.registrarSesion(idCobis, fingerprint, contexto.idSesion()));
        }

        // Respuesta
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.unir(MBAplicacion.configuracionUsuario(contexto, idCobis));
        if (buscarPersona)
            respuesta.unir(futuroPersona.get());

        // si esto tira un error no debe impedir que el usuario se loguee
        try {
            if (MBSalesforce.prendidoSalesforce(contexto.idCobis()))
                new Futuro<>(() -> salesforceIngresoUsuario(contexto));
            else if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones", "prendido_alerta_notificaciones_cobis")) {
                new Futuro<>(() -> mailIngresoUsuario(contexto, respuesta.string("persona.cuit"), respuesta.string("persona.apellido"), respuesta.string("persona.nombre")));
            }
        } catch (Exception e) {
        }

        // Hago llamada a generador de token
        LoginToken token = crearLoginToken(contexto.idCobis());
        respuesta.set("token", token.getUuid());
        if (!SqlMobile.persistirToken(token)) {
            return RespuestaMB.estado("ERROR_GENERACION_TOKEN");
        }

        contexto.sesion().setUsuarioLogueado(true);
        futuroConsultaTokenModo.get();
        respuesta.set("hash", contexto.hashSesion());

        try {
            String ip = contexto.ip();
            new Futuro<>(() -> contexto.insertarLogLogin(ip));
        } catch (Exception e) {
            //
        }

        respuesta.set("mostrarTcv", false);
        /*
        try {
            respuesta.set("mostrarTcv",
                    contexto.logsLogin().size() <= 1
                            && MBPersona.tieneAltaTcv(contexto)
                            && !contexto.tarjetasCredito().isEmpty()
            );
        } catch (Exception e) {
        }
         */

        return respuesta.set("ID", Encriptador.sha256(contexto.idCobis()));
    }

    public static RespuestaMB biometriaTransmit(ContextoMB contexto) {
        if (Util.migracionCompleta())
            return loginTransmit(contexto);
        return loginBiometriaTransmit(contexto);
    }

    public static RespuestaMB obtenerGenero(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");

        if (Objeto.empty(documento))
            return RespuestaMB.parametrosIncorrectos();

        if (!Util.migracionPrendida())
            return RespuestaMB.estado(Errores.FUNCIONALIDAD_APAGADA);

        ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersonas datosBasicosPersonas = ApiPersonas.datosBasicos(contexto, documento).tryGet();

        if (datosBasicosPersonas == null || datosBasicosPersonas.isEmpty())
            return RespuestaMB.estado(Errores.NO_ENCUENTRA_PERSONAS);

        if (!contexto.esMigrado(contexto, documento))
            return RespuestaMB.estado(datosBasicosPersonas.size() == 1
                    ? Errores.ERROR_USUARIO_NO_MIGRADO
                    : Errores.ERROR_USUARIO_NO_MIGRADO_MULTIPLES);

        return RespuestaMB.exito("generos", datosBasicosPersonas.stream().map(p -> new GeneroResponse(p.sexo)).toList());
    }

    public static RespuestaMB loginTransmit(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        if (Objeto.empty(documento, csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        return validacionesLogin(contexto);
    }

    private static RespuestaMB migrarTransmit(ContextoMB contexto, String idSexo, String fingerprint, boolean biometria, String ultimaFechaModificacionUsuario, String ultimaFechaModificacionClave) {
        log.info("TRANSMIT - validar usuario y Clave");
        if (!validarUsuarioParaMigracion(contexto, contexto.parametros.string("usuario"), fingerprint) ||
                !validarClaveParaMigracion(contexto, contexto.parametros.string("clave"), fingerprint)) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_CLAVE_USUARIO));
            return null;
        }

        log.info("TRANSMIT - obtener datos basicos");
        ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersonas datosBasicos = ApiPersonas.datosBasicos(contexto, contexto.parametros.string("documento")).tryGet();
        if (datosBasicos == null || datosBasicos.isEmpty()) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_DATOS_BASICOS));
            return null;
        }

        ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersona persona = datosBasicos.size() == 1
                ? datosBasicos.get(0)
                : datosBasicos.stream().filter(db -> db.sexo.equals(idSexo)).findFirst().orElse(null);

        if (persona == null) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_PERSONA_NO_ENCONTRADA));
            return null;
        }

        log.info("TRANSMIT - obtener email y telèfono");

        Futuro<Objeto> futuroEmail = new Futuro<>(() -> RestPersona.email(contexto, persona.numeroIdentificacionTributaria));
        Futuro<Objeto> futuroTelefono = new Futuro<>(() -> RestPersona.celular(contexto, persona.numeroIdentificacionTributaria));

        Objeto email = futuroEmail.tryGet();
        if (email == null) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_EMAIL_NO_ENCONTRADO));
            return null;
        }

        Objeto telefono = futuroTelefono.tryGet();
        if (telefono == null) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_TELEFONO_NO_ENCONTRADO));
            return null;
        }

        log.info("TRANSMIT - armo objeto de migracion a libreria");
        LibreriaFraudes.UsuarioLibreriaRequest usuarioLibreriaRequest = new LibreriaFraudes.UsuarioLibreriaRequest(
                email.string("direccion"),
                contexto.parametros.string("clave"),
                contexto.parametros.string("usuario"),
                String.format("%s%s", persona.sexo, contexto.parametros.string("documento")),
                obtenerTelefono(telefono),
                "MB",
                "MOBILE",
                persona.numeroIdentificacionTributaria,
                contexto.ip(),
                JourneyTransmitEnum.MB_ENROLAR_USUARIO,
                contexto.sesion().id(),
                contexto.idCobis(),
                "",
                "",
                obtenerFechaModificacionUsuario(ultimaFechaModificacionUsuario, biometria),
                obtenerFechaModificacionClave(ultimaFechaModificacionClave, biometria),
                obtenerFechaModificacion(telefono),
                obtenerFechaModificacion(email),
                biometria
        );

        log.info("TRANSMIT - migracion a libreria");
        LibreriaFraudes.UsuarioLibreriaResponse usuarioLibreriaResponse = ApiTransmit.migrarUsuarioMB(contexto, usuarioLibreriaRequest).tryGet();

        if (usuarioLibreriaResponse == null) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_RESPONSE_NULL));
            return null;
        }

        if (usuarioLibreriaResponse.esError()) {
            log.info("TRANSMIT - error de migracion libreria");
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto,
                    contexto.parametros.integer("documento"),
                    !usuarioLibreriaResponse.errorCode.equals(ConstantesTransmit.CODIGO_ERROR_USUARIO_YA_EXISTE)
                            ? ErroresMigracionTransmitEnum.ERROR_LIBRERIA_EMAIL_DUPLICADO
                            : ErroresMigracionTransmitEnum.ERROR_LIBRERIA_RESPONSE_ERROR));
            return null;
//            if (!usuarioLibreriaResponse.errorCode.equals("405")) {
//                new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_RESPONSE_ERROR));
//                return null;
//            }
//            if (!actualizarEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(contexto.parametros.string("documento")))) {
//                new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_RESPONSE_ERROR));
//                return null;
//            }
//            return RespuestaMB.estado(Errores.ERROR_MIGRACION_COMPLETA);
        }

        LibreriaFraudes.DatosAdicionales datosAdicionalesCsm = MapperUtil.mapToObject(usuarioLibreriaResponse.addicionalData, LibreriaFraudes.DatosAdicionales.class);
        if (datosAdicionalesCsm == null || !StringUtils.isNotBlank(datosAdicionalesCsm.csmIdAuth)) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_MAPEO_RESPONSE));
            return null;
        }

        log.info("TRANSMIT - actualizo tabla interna");
        if (!actualizarEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(contexto.parametros.string("documento")))) {
            new Futuro<>(() -> TransmitMB.actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_ACTUALIZACION_ESTADO_MIGRACION));
            return null;
        }

        Futuro<Boolean> futuroModificarAltaSoftToken = new Futuro<>(() -> modificarAltaSoftToken(contexto));
        Futuro<Boolean> futuroAccesoBiometria = new Futuro<>(() -> modificarAccesoBiometria(contexto));

        futuroModificarAltaSoftToken.tryGet();
        futuroAccesoBiometria.tryGet();

        contexto.csmIdAuth = datosAdicionalesCsm.csmIdAuth;
        contexto.usuarioMigrado = true;
        contexto.parametros.set("migrado", true);

        if (biometria) {
            contexto.limpiarSegundoFactor();
            contexto.limpiarMigracion();
            return RespuestaMB.exito();
        }

        return validacionesLogin(contexto);
    }

    private static String obtenerTelefono(Objeto telefono) {
        return String.format("+%s9%s%s%s",
                telefono.string("codigoPais").replaceFirst("^0+(?!$)", ""),
                telefono.string("codigoArea").replaceFirst("^0+(?!$)", ""),
                telefono.string("caracteristica"),
                telefono.string("numero"));
    }

    private static boolean prendidoMonitoreoTransaccional(String idCobis) {
        return MBAplicacion.funcionalidadPrendida(idCobis, "prendido_monitoreo_transaccional");
    }

    private static RespuestaMB chequearMigracion(ContextoMB contexto, String documento, String idSexo, String fingerprint, boolean biometria) {
        log.info("Chequear migracion");
        MigracionUsuario.ResponseMigracionUsuario estadoMigradoResponse = obtenerEstadoMigracion(contexto,
                ar.com.hipotecario.backend.base.Util.documento(documento));
        log.info("estadoMigradoResponse: {}", estadoMigradoResponse);
        if (estadoMigradoResponse != null) {
            EstadoMigradoEnum estadoMigradoEnum = EstadoMigradoEnum.codigo(estadoMigradoResponse.migrado);
            log.info("estadoMigradoEnum: {}", estadoMigradoEnum);
            if (!estadoMigradoEnum.esNoMigra()) {
                if (estadoMigradoEnum.esMigrado())
                    return loginTransmit(contexto);
                if (estadoMigradoEnum.esParaMigrar() || (estadoMigradoEnum.esErrorAlMigrar() &&
                        StringUtils.isNotBlank(estadoMigradoResponse.fechaError) && puedeIntentar(estadoMigradoResponse.fechaError))) {
                    if (!biometria)
                        return migrarTransmit(contexto, idSexo, fingerprint, false, estadoMigradoResponse.ultimaFechaModificacionUsuario, estadoMigradoResponse.ultimaFechaModificacionClave);
                    contexto.usuarioMigrarBiometria = true;
                }
            }
        }
        return null;
    }

    private static RespuestaMB validacionesLogin(ContextoMB contexto) {
        log.info("TRANSMIT - validacion Login transmit");
        String documento = contexto.parametros.string("documento");
        String idSexo = contexto.parametros.string("idSexo", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        Integer numeroDocumento = ar.com.hipotecario.backend.base.Util.documento(documento);
        if (numeroDocumento == 0)
            return RespuestaMB.parametrosIncorrectos();

        eliminarSesion(contexto, false, "");

        RespuestaMB respuestaIdCobis = obtenerIdCobis(contexto, documento, null, idSexo);
        if (respuestaIdCobis.hayError())
            return respuestaIdCobis;

        String idCobis = respuestaIdCobis.string("cobis");

        setearCobisSesion(contexto, idCobis);

        boolean prendidoMonitoreoTransaccional = prendidoMonitoreoTransaccional(idCobis);
        boolean validaSesionDuplicada = validaSesionDuplicada();

        registrarHeaderInterno(contexto);
        registrarHeaders(contexto, "loginTransmit");

        if (ConfigMB.bool("prender_super_cache", ConfigMB.esARO()))
            supercache(contexto);

        Futuro<Boolean> futuroExisteSesion = validaSesionDuplicada ? new Futuro<>(
                () -> SqlHomebanking.existeSesion(idCobis, fingerprint)) : null;
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = prendidoMonitoreoTransaccional ? new Futuro<>(
                () -> SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobis)) : null;
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(
                () -> SqlHomebanking.fechaHoraUltimaConextion(idCobis));
        Futuro<RespuestaMB> futuroConfiguracionUsuario = new Futuro<>(() -> MBAplicacion.configuracionUsuario(contexto, idCobis));
        Futuro<RespuestaMB> futuroPersona = new Futuro<>(() -> MBPersona.persona(contexto));
        Futuro<Boolean> futuroTokenModo = new Futuro<>(() -> generarTokenModo(contexto));

        if (validaSesionDuplicada) {
            Boolean existe = futuroExisteSesion.tryGet();
            if (existe != null && existe) {
                eliminarSesion(contexto, false, idCobis);
                return RespuestaMB.estado(Errores.EXISTE_SESION);
            }
        }

        if (prendidoMonitoreoTransaccional) {
            Boolean bloqueadoFraudeLink = futuroBloqueadoPorTransaccionFraudeLink.tryGet();
            if (bloqueadoFraudeLink != null && bloqueadoFraudeLink) {
                eliminarSesion(contexto, false, idCobis);
                return RespuestaMB.estado(Errores.USUARIO_BLOQUEADO_POR_FRAUDE_LINK);
            }
        }

        log.info("TRANSMIT - validacion de csm");
        if (!contexto.parametros.bool("migrado", false) && !TransmitMB.validarCsmTransaccion(contexto, JourneyTransmitEnum.MB_INICIO_SESION))
            return RespuestaMB.estado(Errores.ERROR_LOGIN_LIBRERIA);

        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.tryGet();
        if (fechaHoraUltimaConexion != null)
            registrarFechaHoraUltimaConexion(contexto, fechaHoraUltimaConexion);

        if (validaSesionDuplicada)
            registrarSesion(contexto, fingerprint);

        Objeto persona = futuroPersona.tryGet();
        if (!Objeto.empty(persona))
            enviarMailIngreso(contexto, persona.string("cuit"), persona.string("apellido"), persona.string("nombre"));

        insertarLog(contexto);

        setearSesionLogueado(contexto);

        futuroTokenModo.tryGet();

        RespuestaMB respuestaCrearLogin = crearLoginToken(contexto);
        if (respuestaCrearLogin.hayError())
            return respuestaCrearLogin;

        log.info("TRANSMIT - fin de login");
        return RespuestaMB.exito()
                .set("hash", contexto.hashSesion())
                .set("ID", Encriptador.sha256(contexto.idCobis()))
                .set("csmIdAuth", contexto.csmIdAuth)
                .set("usuarioMigrado", contexto.usuarioMigrado)
                .unir(futuroConfiguracionUsuario.tryGet())
                .unir(futuroPersona.tryGet())
                .unir(respuestaCrearLogin);
    }

    public static RespuestaMB migrarTransmit(ContextoMB contexto) {
        if (Util.migracionCompleta())
            return RespuestaMB.estado(Errores.ERROR_GENERICO);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario", null);
        String clave = contexto.parametros.string("clave", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        RespuestaMB respuesta;

        if (Objeto.empty(documento, usuario, clave))
            return RespuestaMB.parametrosIncorrectos();

        if (!contexto.sesion().isCambioClaveTransmit() || !contexto.sesion().isCambioUsuarioTransmit() || !contexto.validaSegundoFactor("migracion-transmit"))
            return RespuestaMB.requiereSegundoFactor();

        Futuro<Persona> futuroPersona = new Futuro<>(contexto::persona);

        MigracionUsuario.ResponseMigracionUsuario estadoMigradoResponse = obtenerEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(documento));

        if (estadoMigradoResponse == null || !EstadoMigradoEnum.codigo(estadoMigradoResponse.migrado).esParaMigrar())
            return RespuestaMB.estado(Errores.ERROR_EN_BUSCAR_ESTADO_MIGRACION);

        respuesta = migrarTransmit(contexto, futuroPersona.tryGet().idSexo(), fingerprint, true, estadoMigradoResponse.ultimaFechaModificacionUsuario, estadoMigradoResponse.ultimaFechaModificacionClave);
        if (respuesta == null)
            return RespuestaMB.estado(Errores.ERROR_EN_MIGRACION_LIBRERIA);
        if (respuesta.hayError())
            return respuesta;

        return RespuestaMB.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB renovacionTransmit(ContextoMB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (!contexto.esMigrado(contexto))
            return RespuestaMB.estado(Errores.ERROR_USUARIO_NO_MIGRADO);

        return TransmitMB.validarCsmTransaccion(contexto, JourneyTransmitEnum.HB_INICIO_SESION)
                ? RespuestaMB.exito().set("csmIdAuth", contexto.csmIdAuth)
                : RespuestaMB.estado(Errores.ERROR_LOGIN_LIBRERIA);
    }

    private static MigracionUsuario.ResponseMigracionUsuario obtenerEstadoMigracion(ContextoMB contexto,
                                                                                    int numeroDocumento) {
        MigracionUsuario.ResponseMigracionUsuario response = gestionarMigracion(contexto, new MigracionUsuario.RequestMigracionUsuario(numeroDocumento, 0, OpcionMigradoEnum.CONSULTAR, contexto.idCobis(), "", ""));
        log.info("TRANSMIT - ResponseMigracionUsuario: " + response);
        if (Objeto.empty(response) || (!Objeto.empty(response) && response.codRet == -1))
            return null;
        return response;
    }

    private static boolean actualizarEstadoMigracion(ContextoMB contexto, int numeroDocumento) {
        MigracionUsuario.ResponseMigracionUsuario response = gestionarMigracion(contexto, new MigracionUsuario.RequestMigracionUsuario(numeroDocumento, Integer.parseInt(EstadoMigradoEnum.MIGRADO.getCodigo()), OpcionMigradoEnum.ACTUALIZAR, contexto.idCobis(), "", ""));
        return !Objeto.empty(response) && (Objeto.empty(response) || response.codRet != -1);
    }

    private static MigracionUsuario.ResponseMigracionUsuario gestionarMigracion(ContextoMB contexto, MigracionUsuario.RequestMigracionUsuario request) {
        return ApiSeguridad.gestionarMigracion(contexto, request).tryGet();
    }

    private static boolean validarUsuarioParaMigracion(ContextoMB contexto, String usuario, String fingerprint) {
        ApiResponseMB validarUsuario = new Futuro<>(
                () -> RestSeguridad.validarUsuario(contexto, usuario, fingerprint)).tryGet();
        if (validarUsuario == null)
            return false;
        return !validarUsuario.hayError();
    }

    private static boolean validarClaveParaMigracion(ContextoMB contexto, String clave, String fingerprint) {
        ApiResponseMB validarClave = new Futuro<>(
                () -> RestSeguridad.validarClavev2(contexto, clave, fingerprint)).tryGet();
        if (validarClave == null)
            return false;
        return !validarClave.hayError();
    }

    private static void loguearMensajeError(String accion, String funcionalidad, String idCobis) {
        log.info("Error al intentar ".concat(accion).concat("el ").concat(funcionalidad).concat(" de cliente ").concat(idCobis).concat("al migrar a Transmit"));
    }

    private static Boolean modificarAccesoBiometria(ContextoMB contexto) {
        AccesoBiometriaHB.AccesoBiometria accesoBiometria = SqlHomeBanking.obtenerAccesosBiometria(contexto, contexto.idCobis(), "").tryGet();
        if (accesoBiometria == null) {
            loguearMensajeError("obtener", "Acceso Biometria", contexto.idCobis());
            return false;
        }

        if (SqlHomeBanking.borrarAccesosBiometria(contexto, contexto.idCobis()).tryGet())
            return true;

        loguearMensajeError("modificar", "Acceso Biometria", contexto.idCobis());
        return true;
    }

    private static Boolean modificarAltaSoftToken(ContextoMB contexto) {
        SoftTokenMobile.SoftToken softToken = ar.com.hipotecario.backend.servicio.sql.SqlMobile.obtenerUltimaAltaSoftTokenActiva(contexto, contexto.idCobis()).tryGet();
        if (softToken == null) {
            loguearMensajeError("obtener", "Alta de Soft Token", contexto.idCobis());
            return false;
        }

        if (ar.com.hipotecario.backend.servicio.sql.SqlMobile.deshabilitarSoftToken(contexto, String.valueOf(softToken.Id)).tryGet())
            return true;

        loguearMensajeError("modificar", "Alta de Soft Token", contexto.idCobis());
        return true;
    }

    private static Boolean modificarRegistroDispositivo(ContextoMB contexto) {
        RegistroDispositivoMobile.RegistroDispositivo registroDispositivo = ar.com.hipotecario.backend.servicio.sql.SqlMobile.obtenerUltimoRegistroPorCobis(contexto, contexto.idCobis()).tryGet();
        if (registroDispositivo == null) {
            loguearMensajeError("obtener", "Registro de Dispositivo", contexto.idCobis());
            return false;
        }

        if (ar.com.hipotecario.backend.servicio.sql.SqlMobile.deshabilitarRegistroDispositivo(contexto, registroDispositivo.IdDispositivo.concat(String.valueOf(new Random().nextInt())), String.valueOf(registroDispositivo.Id)).tryGet())
            return true;

        loguearMensajeError("modificar", "Registro de Dispositivo", contexto.idCobis());
        return false;
    }

    private static Emails.Email obtenerEmail(ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        return emails.personal() != null ? emails.personal() : (emails.laboral() != null ? emails.laboral() : null);
    }

    private static String obtenerEmailPersona(ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        Emails.Email email = obtenerEmail(emails);
        return email != null ? email.direccion() : "";
    }

    private static Date obtenerFechaModificacionEmailPersonal
            (ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        Emails.Email email = obtenerEmail(emails);
        return email != null ? email.fechaModificacion.fechaDate() : null;
    }

    private static Telefonos.Telefono obtenerTelefono(ar.com.hipotecario.backend.servicio.api.personas.Telefonos
                                                              telefonos) {
        return telefonos.celular();
    }

    private static String obtenerTelefonoPersona(ar.com.hipotecario.backend.servicio.api.personas.Telefonos
                                                         telefonos) {
        ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono telefono = obtenerTelefono(telefonos);
        return telefono != null ? telefono.numeroMigracion() : "";
    }

    private static Date obtenerFechaModificacionTelefonoPersona
            (ar.com.hipotecario.backend.servicio.api.personas.Telefonos telefonos) {
        ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono telefono = obtenerTelefono(telefonos);
        return telefono != null ? telefono.fechaModificacion.fechaDate() : null;
    }

    private static Date obtenerFechaModificacion(Objeto objeto) {
        LocalDate localDate = LocalDate.parse(objeto.string("fechaModificacion"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date convertirDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date restarHoras(Date date) {
        return Date.from(date.toInstant().minus(49, ChronoUnit.HOURS));
    }

    private static Date obtenerFechaModificacionClave(String ultimaFechaModificacionClave, boolean biometria) {
        Date date = StringUtils.isNotBlank(ultimaFechaModificacionClave)
                ? convertirDate(LocalDate.parse(ultimaFechaModificacionClave, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                : new Date();
        return biometria ? restarHoras(date) : date;
    }

    private static Date obtenerFechaModificacionUsuario(String ultimaFechaModificacionUsuario, boolean biometria) {
        Date date = StringUtils.isNotBlank(ultimaFechaModificacionUsuario)
                ? convertirDate(LocalDate.parse(ultimaFechaModificacionUsuario, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                : new Date();
        return biometria ? restarHoras(date) : date;
    }

    private static void setearCobisSesion(ContextoMB contexto, String idCobis) {
        contexto.sesion().setIdCobis(idCobis);
        contexto.sesion().setIdCobisReal(idCobis);
    }

    private static List<String> obtenerCobis(ContextoMB contexto, String documento, String idTipoDocumento, String
            idSexo) {
        log.info("TRANSMIT - obtener cobis:");
        String idCobis = EMBLogin.clientes.get(documento);
        log.info("TRANSMIT - obtener cobis idCobis: {}", idCobis);
        if (Objeto.empty(idCobis))
            return RestPersona.listaIdCobis(contexto, documento, idTipoDocumento, idSexo);
        return List.of(idCobis);
    }

    private static RespuestaMB obtenerIdCobis(ContextoMB contexto, String documento, String idTipoDocumento, String
            idSexo) {
        log.info("TRANSMIT obtenerIdCobis");
        List<String> listaIdCobis = obtenerCobis(contexto, documento, idTipoDocumento, idSexo);
        log.info("TRANSMIT listaIdCobis {}", listaIdCobis);
        if (listaIdCobis == null)
            return RespuestaMB.estado(Errores.ERROR_GENERICO);
        else if (listaIdCobis.isEmpty())
            return RespuestaMB.estado(Errores.USUARIO_INVALIDO);
        else if (listaIdCobis.size() > 1)
            return RespuestaMB.estado(Errores.MULTIPLES_PERSONAS_ENCONTRADAS);
        log.info("TRANSMIT idCObis {}", listaIdCobis.get(0));
        return RespuestaMB.exito().set("cobis", listaIdCobis.get(0));
    }

    private static boolean validaSesionDuplicada() {
        return MBAplicacion.funcionalidadPrendida("validar_sesion_duplicada_aro");
    }

    private static void enviarMailIngreso(ContextoMB contexto, String apellido, String nombre, String cuit) {
        try {
            if (MBSalesforce.prendidoSalesforce(contexto.idCobis()))
                new Futuro<>(() -> salesforceIngresoUsuario(contexto));
            else if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones", "prendido_alerta_notificaciones_cobis"))
                new Futuro<>(() -> mailIngresoUsuario(contexto, cuit, apellido, nombre));
        } catch (Exception e) {
        }
    }

    private static void insertarLog(ContextoMB contexto) {
        new Futuro<>(() -> contexto.insertarLogLogin(contexto));
    }

    private static void setearSesionLogueado(ContextoMB contexto) {
        contexto.sesion().setUsuarioLogueado(true);
        contexto.sesion().ip = contexto.ip();
    }

    private static void registrarFechaHoraUltimaConexion(ContextoMB contexto, Date fechaHoraUltimaConexion) {
        contexto.sesion().setFechaHoraUltimaConexion(fechaHoraUltimaConexion);
        new Futuro<>(() -> SqlHomebanking.registrarFechaHoraUltimaConextion(contexto.idCobis(),
                ConfigMB.string("modo_ultima_conexion", "rowlock")));
    }

    private static RespuestaMB crearLoginToken(ContextoMB contexto) {
        LoginToken token = crearLoginToken(contexto.idCobis());
        if (!SqlMobile.persistirToken(token))
            return RespuestaMB.estado(Errores.ERROR_GENERACION_TOKEN);
        return RespuestaMB.exito("token", token.getUuid());
    }

    private static void registrarHeaderInterno(ContextoMB contexto) {
        contexto.requestHeader("interno", "true");
    }

    private static boolean puedeIntentar(String fecha) {
        LocalDate fechaError = Util.stringToLocalDate(fecha, "yyyy-MM-dd");

        LocalDate fechaReintenta = new Date()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(ConfigMB.integer("cantidad_dias_reintento_transmit", 0));

        return fechaError.isBefore(fechaReintenta);
    }

    /* FIN TRANSMIT */

}
