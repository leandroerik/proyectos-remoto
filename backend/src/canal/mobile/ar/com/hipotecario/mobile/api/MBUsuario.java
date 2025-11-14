package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.EstadoMigradoEnum;
import ar.com.hipotecario.backend.util.Errores;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.servicio.TransmitMB;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestSeguridad;
import ar.com.hipotecario.mobile.servicio.SqlHomebanking;

public class MBUsuario {

    public static RespuestaMB nuevoUsuario(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");

        if (Objeto.anyEmpty(usuario)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (documento != null) {
            RespuestaMB pseudoLogin = MBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        // SeguridadGetUsuario
        ApiRequestMB requestSeguridadGetUsuario = ApiMB.request("SeguridadGetUsuario", "seguridad", "GET", "/v1/usuario", contexto);
        requestSeguridadGetUsuario.query("grupo", "ClientesBH");
        requestSeguridadGetUsuario.query("idcliente", idCobis);
        requestSeguridadGetUsuario.permitirSinLogin = true;

        ApiResponseMB responseSeguridadGetUsuario = ApiMB.response(requestSeguridadGetUsuario, idCobis);
        if (responseSeguridadGetUsuario.hayError() && responseSeguridadGetUsuario.codigo != 404) {
            return RespuestaMB.error();
        }

        if (!contexto.validaSegundoFactor("adhesion-canal")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        // SeguridadPostClaveUsuario
        if (!responseSeguridadGetUsuario.bool("tieneClaveDefault")) {
            ApiRequestMB requestSeguridadPostClaveUsuario = ApiMB.request("CrearUsuario", "seguridad", "POST", "/v1/clave", contexto);
            requestSeguridadPostClaveUsuario.body("grupo", "ClientesBH");
            requestSeguridadPostClaveUsuario.body("idUsuario", idCobis);
            requestSeguridadPostClaveUsuario.body("parametros").set("clave", usuario);
            requestSeguridadPostClaveUsuario.permitirSinLogin = true;

            ApiResponseMB responseSeguridadPostUsuario = ApiMB.response(requestSeguridadPostClaveUsuario, idCobis);
            if (responseSeguridadPostUsuario.hayError()) {
                if (responseSeguridadPostUsuario.string("detalle").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_USUARIO_INVALIDO");
                }
                if (responseSeguridadPostUsuario.string("mensajeAlUsuario").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_USUARIO_INVALIDO");
                }
                return RespuestaMB.error();
            }
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_USUARIO");
        contexto.insertarLogCambioUsuario(contexto);

        // SeguridadPostClaveClave
        if (!responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
            if (Objeto.anyEmpty(clave)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            ApiRequestMB requestSeguridadPostClaveClave = ApiMB.request("CrearClave", "seguridad", "POST", "/v1/clave", contexto);
            requestSeguridadPostClaveClave.body("grupo", "ClientesBH");
            requestSeguridadPostClaveClave.body("idUsuario", idCobis);
            requestSeguridadPostClaveClave.body("parametros").set("clave", clave);
            requestSeguridadPostClaveClave.body("nombreClave", "numerica");
            requestSeguridadPostClaveClave.permitirSinLogin = true;

            ApiResponseMB responseSeguridadPostClaveClave = ApiMB.response(requestSeguridadPostClaveClave, idCobis);
            if (responseSeguridadPostClaveClave.hayError()) {
                if (responseSeguridadPostClaveClave.string("detalle").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_CLAVE_INVALIDA");
                }
                if (responseSeguridadPostClaveClave.string("mensajeAlUsuario").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_CLAVE_INVALIDA");
                }
                return RespuestaMB.error();
            }

            // TODO: guardar cambios de datos del usuario
            contexto.insertarContador("CAMBIO_CLAVE");
            contexto.insertarLogCambioClave(contexto);
        }

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarUsuario(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario");

        if (Objeto.anyEmpty(usuario)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (documento != null) {
            RespuestaMB pseudoLogin = MBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (!contexto.validaSegundoFactor("adhesion-canal")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        // SeguridadPostCambiarUsuario
        ApiResponseMB response = RestSeguridad.cambiarUsuario(contexto, usuario, true);
        if (response.hayError()) {
            if (response.string("detalle").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            return RespuestaMB.error();
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_USUARIO");
        contexto.insertarLogCambioUsuario(contexto);
        mailCambioUsuario(contexto);

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarUsuarioLogueado(ContextoMB contexto) {
        String usuarioAnterior = contexto.parametros.string("usuarioAnterior");
        String usuarioNuevo = contexto.parametros.string("usuarioNuevo");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(usuarioAnterior, usuarioNuevo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // Validar Usuario
        ApiResponseMB validarUsuario = RestSeguridad.validarUsuario(contexto, usuarioAnterior, fingerprint);
        if (validarUsuario.hayError()) {
            String error = "ERROR";
            error = validarUsuario.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarUsuario.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("detalle").contains("password has expired") ? "USUARIO_EXPIRADO" : error;
            return RespuestaMB.estado(error);
        }

        // SeguridadPostCambiarUsuario
        ApiResponseMB response = RestSeguridad.cambiarUsuario(contexto, usuarioNuevo, false);
        if (response.hayError()) {
            if (response.string("detalle").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            return RespuestaMB.error();
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_USUARIO");
        contexto.insertarLogCambioUsuario(contexto);

        mailCambioUsuario(contexto);

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarClave(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String clave = contexto.parametros.string("clave");

        if (Objeto.anyEmpty(clave)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (documento != null) {
            RespuestaMB pseudoLogin = MBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (!contexto.validaSegundoFactor("adhesion-canal")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }
        // CambiarClave
        ApiResponseMB response = RestSeguridad.cambiarClave(contexto, clave, true);
        if (response.hayError()) {
            if (response.string("detalle").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            return RespuestaMB.error();
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_CLAVE");
        contexto.insertarLogCambioClave(contexto);

        mailCambioClave(contexto);

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarClaveLogueado(ContextoMB contexto) {
        String claveAnterior = contexto.parametros.string("claveAnterior");
        String claveNueva = contexto.parametros.string("claveNueva");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(claveAnterior, claveNueva)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB validarClave = RestSeguridad.validarClave(contexto, claveAnterior, fingerprint);

        if (validarClave.hayError()) {
            String error = "ERROR";
            error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

            return RespuestaMB.estado(error);
        }

        // CambiarClave
        ApiResponseMB response = RestSeguridad.cambiarClave(contexto, claveNueva, false);
        if (response.hayError()) {
            if (response.string("detalle").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                return RespuestaMB.estado("CLAVE_REPETIDA");
            }
            return RespuestaMB.error();
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_CLAVE");
        contexto.insertarLogCambioClave(contexto);

        mailCambioClave(contexto);

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static void mailCambioUsuario(ContextoMB contexto) {
        String emailDestino = contexto.persona().email();
        // emailDestino = "semachado@hipotecario.com.ar";
        if (emailDestino != null && !emailDestino.isEmpty()) {
            ApiRequestMB requestMail = ApiMB.request("CambiarUsuarioCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", ConfigMB.string("doppler_cambio_usuario"));
            requestMail.permitirSinLogin = true;
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Cambio de usuario BH");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
            parametros.set("CANAL", "Home Banking");

            ApiMB.response(requestMail, new Date().getTime());
        }
    }

    public static void mailCambioClave(ContextoMB contexto) {
        try {
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones", "prendido_alerta_notificaciones_cobis")) {
                if (!contexto.persona().alertaCambioClave())
                    return;
            }
        } catch (Exception e) {

        }

        String emailDestino = contexto.persona().email();
        // emailDestino = "semachado@hipotecario.com.ar";
        if (emailDestino != null && !emailDestino.isEmpty()) {
            ApiRequestMB requestMail = ApiMB.request("CambiarClaveCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", ConfigMB.string("doppler_cambio_clave"));
            requestMail.permitirSinLogin = true;
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Cambio de usuario BH");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
            parametros.set("CANAL", "Home Banking");
            ApiMB.response(requestMail, new Date().getTime());
        }
    }

    public static RespuestaMB encuestaUsuario(ContextoMB contexto) {
        String canal = contexto.parametros.string("canal");
        String funcionalidad = contexto.parametros.string("funcionalidad");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(canal, funcionalidad)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> preguntas = SqlHomebanking.encuestaPreguntas(canal, funcionalidad);
        if (preguntas.isEmpty()) {
            return RespuestaMB.error();
        }
        preguntas = SqlHomebanking.mostrarEncuestaUsuario(idCobis, preguntas, funcionalidad);
        if (preguntas.isEmpty()) {
            return RespuestaMB.exito();
        }

        return respuesta.set("preguntas", preguntas);
    }

    public static RespuestaMB guardarEncuesta(ContextoMB contexto) {
        String opinion = contexto.parametros.string("opinion");
        Integer puntuacion = contexto.parametros.integer("puntuacion");
        Integer idPregunta = contexto.parametros.integer("pregunta");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(idPregunta, puntuacion)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        boolean response = SqlHomebanking.guardaEncuesta(idCobis, idPregunta, opinion, puntuacion);
        if (!response) {
            return RespuestaMB.estado("ENCUESTA_NO_GUARDADA");
        }
        return RespuestaMB.exito();
    }

    public static RespuestaMB guardarAuditorConsumoSugerido(ContextoMB contexto) {
        Integer recomendedMerchant = contexto.parametros.integer("idEstablecimiento");
        String nombreComercioVisualizacion = contexto.parametros.string("nombreComercioVisualizacion");
        boolean vioDetalle = contexto.parametros.bool("vioDetalle");
        String canal = contexto.parametros.string("canal");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        boolean response = SqlHomebanking.guardarAuditorConsumoSugerido(idCobis, recomendedMerchant, nombreComercioVisualizacion, canal, vioDetalle ? 1 : 0);
        if (!response) {
            return RespuestaMB.estado("CONSUMO_NO_GUARDADO");
        }
        return RespuestaMB.exito();
    }

    public static RespuestaMB obtenerConfiguracionVariable(ContextoMB contexto) {
        String llave = contexto.parametros.string("llave");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(llave)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        RespuestaMB respuesta = new RespuestaMB();
        Objeto mensaje = SqlHomebanking.obtenerConfiguracionVariable(llave);
        if (mensaje == null) {
            return RespuestaMB.error();
        }
        return respuesta.set("mensaje", mensaje);
    }

    public static RespuestaMB nuevoUsuarioSS(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");

        if (Objeto.anyEmpty(usuario)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (documento != null) {
            RespuestaMB pseudoLogin = MBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        // SeguridadGetUsuario
        ApiRequestMB requestSeguridadGetUsuario = ApiMB.request("SeguridadGetUsuario", "seguridad", "GET", "/v1/usuario", contexto);
        requestSeguridadGetUsuario.query("grupo", "ClientesBH");
        requestSeguridadGetUsuario.query("idcliente", idCobis);
        requestSeguridadGetUsuario.permitirSinLogin = true;

        ApiResponseMB responseSeguridadGetUsuario = ApiMB.response(requestSeguridadGetUsuario, idCobis);
        if (responseSeguridadGetUsuario.hayError() && responseSeguridadGetUsuario.codigo != 404) {
            return RespuestaMB.error();
        }

        // SeguridadPostClaveUsuario
        if (!responseSeguridadGetUsuario.bool("tieneClaveDefault")) {
            ApiRequestMB requestSeguridadPostClaveUsuario = ApiMB.request("CrearUsuario", "seguridad", "POST", "/v1/clave", contexto);
            requestSeguridadPostClaveUsuario.body("grupo", "ClientesBH");
            requestSeguridadPostClaveUsuario.body("idUsuario", idCobis);
            requestSeguridadPostClaveUsuario.body("parametros").set("clave", usuario);
            requestSeguridadPostClaveUsuario.permitirSinLogin = true;

            ApiResponseMB responseSeguridadPostUsuario = ApiMB.response(requestSeguridadPostClaveUsuario, idCobis);
            if (responseSeguridadPostUsuario.hayError()) {
                if (responseSeguridadPostUsuario.string("detalle").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_USUARIO_INVALIDO");
                }
                if (responseSeguridadPostUsuario.string("mensajeAlUsuario").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_USUARIO_INVALIDO");
                }
                return RespuestaMB.error();
            }
        }

        // TODO: guardar cambios de datos del usuario
        contexto.insertarContador("CAMBIO_USUARIO");
        contexto.insertarLogCambioUsuario(contexto);

        // SeguridadPostClaveClave
        if (!responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
            if (Objeto.anyEmpty(clave)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            ApiRequestMB requestSeguridadPostClaveClave = ApiMB.request("CrearClave", "seguridad", "POST", "/v1/clave", contexto);
            requestSeguridadPostClaveClave.body("grupo", "ClientesBH");
            requestSeguridadPostClaveClave.body("idUsuario", idCobis);
            requestSeguridadPostClaveClave.body("parametros").set("clave", clave);
            requestSeguridadPostClaveClave.body("nombreClave", "numerica");
            requestSeguridadPostClaveClave.permitirSinLogin = true;

            ApiResponseMB responseSeguridadPostClaveClave = ApiMB.response(requestSeguridadPostClaveClave, idCobis);
            if (responseSeguridadPostClaveClave.hayError()) {
                if (responseSeguridadPostClaveClave.string("detalle").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_CLAVE_INVALIDA");
                }
                if (responseSeguridadPostClaveClave.string("mensajeAlUsuario").contains("The password does not meet the following password rules")) {
                    return RespuestaMB.estado("FORMATO_CLAVE_INVALIDA");
                }
                return RespuestaMB.error();
            }

            // TODO: guardar cambios de datos del usuario
            contexto.insertarContador("CAMBIO_CLAVE");
            contexto.insertarLogCambioClave(contexto);
        }

        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarUsuarioTrasmit(ContextoMB contexto) {
        if (Util.migracionCompleta())
            return RespuestaMB.estado(Errores.ERROR_GENERICO);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario", null);

        if (Objeto.anyEmpty(documento, usuario))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuesta = TransmitMB.usuarioParaMigrar(contexto, documento);
        if (respuesta != null)
            return respuesta;

        if (!contexto.validaSegundoFactor("migracion-transmit"))
            return RespuestaMB.requiereSegundoFactor();

        ApiResponseMB apiResponseCambioUsuario = RestSeguridad.cambiarUsuario(contexto, usuario, false);
        if (apiResponseCambioUsuario.hayError()) {
            if (chequearErroresCambioClaveUsuario(apiResponseCambioUsuario))
                return RespuestaMB.estado(Errores.ERROR_USUARIO_REPETIDO);
            return RespuestaMB.estado(Errores.ERROR_CAMBIO_USUARIO);
        }

        contexto.sesion().setCambioUsuarioTransmit(true);
        return RespuestaMB.exito();
    }

    public static RespuestaMB cambiarClaveTrasmit(ContextoMB contexto) {
        if (Util.migracionCompleta())
            return RespuestaMB.estado(Errores.ERROR_GENERICO);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        String documento = contexto.parametros.string("documento", null);
        String clave = contexto.parametros.string("clave", null);

        if (Objeto.anyEmpty(documento, clave))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuesta = TransmitMB.usuarioParaMigrar(contexto, documento);
        if (respuesta != null)
            return respuesta;

        if (!contexto.validaSegundoFactor("migracion-transmit"))
            return RespuestaMB.requiereSegundoFactor();

        ApiResponseMB apiResponseCambioClave = RestSeguridad.cambiarClave(contexto, clave, false);
        if (apiResponseCambioClave.hayError()) {
            if (chequearErroresCambioClaveUsuario(apiResponseCambioClave))
                return RespuestaMB.estado(Errores.ERROR_CLAVE_REPETIDA);
            return RespuestaMB.estado(Errores.ERROR_CAMBIO_CLAVE);
        }

        contexto.sesion().setCambioClaveTransmit(true);
        return RespuestaMB.exito();
    }

    private static boolean chequearErroresCambioClaveUsuario(ApiResponseMB apiResponseMB) {
        return apiResponseMB.string("detalle").contains("Cannot Repeat History Value") ||
                apiResponseMB.string("mensajeAlUsuario").contains("Cannot Repeat History Value");
    }

}
