package ar.com.hipotecario.canal.homebanking.api;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.servicio.TransmitHB;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.domain.enums.BankProcessChangeDataType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.ChangeDataHBBMBankProcess;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.endpoints.EHBLogin;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestSeguridad;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;

public class HBUsuario {

    public static Respuesta nuevoUsuario(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");

        if (contexto.esProduccion() && HBAplicacion.funcionalidadPrendida("alta_usuario_transmit"))
            return Respuesta.funcionalidadNoHabilitada();

        if (Objeto.anyEmpty(usuario)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (documento != null) {
            Respuesta pseudoLogin = HBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        // SeguridadGetUsuario
        ApiRequest requestSeguridadGetUsuario = Api.request("SeguridadGetUsuario", "seguridad", "GET", "/v1/usuario",
                contexto);
        requestSeguridadGetUsuario.query("grupo", "ClientesBH");
        requestSeguridadGetUsuario.query("idcliente", idCobis);
        requestSeguridadGetUsuario.permitirSinLogin = true;

        ApiResponse responseSeguridadGetUsuario = Api.response(requestSeguridadGetUsuario, idCobis);
        if (responseSeguridadGetUsuario.hayError() && responseSeguridadGetUsuario.codigo != 404) {
            return Respuesta.error();
        }

        Boolean esProspecto = contexto.validaPorRiesgoNet(contexto);
        if (esProspecto) {
            if (!contexto.sesion.validaRiesgoNet) {
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
            }
        } else {
            String segundoFactor = "adhesion-canal";
            if (responseSeguridadGetUsuario.bool("tieneClaveDefault")
                    && responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
                segundoFactor = "cambio-clave-canal";
            }
            if (!contexto.validaSegundoFactor(segundoFactor)) {
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
            }
        }

        // SeguridadPostClaveUsuario
        if (!responseSeguridadGetUsuario.bool("tieneClaveDefault")) {
            ApiRequest requestSeguridadPostClaveUsuario = Api.request("CrearUsuario", "seguridad", "POST", "/v1/clave",
                    contexto);
            requestSeguridadPostClaveUsuario.body("grupo", "ClientesBH");
            requestSeguridadPostClaveUsuario.body("idUsuario", idCobis);
            requestSeguridadPostClaveUsuario.body("parametros").set("clave", usuario);
            requestSeguridadPostClaveUsuario.permitirSinLogin = true;

            ApiResponse responseSeguridadPostUsuario = Api.response(requestSeguridadPostClaveUsuario, idCobis);
            if (responseSeguridadPostUsuario.hayError()) {
                if (responseSeguridadPostUsuario.string("detalle")
                        .contains("The password does not meet the following password rules")) {
                    return Respuesta.estado("FORMATO_USUARIO_INVALIDO");
                }
                if (responseSeguridadPostUsuario.string("mensajeAlUsuario")
                        .contains("The password does not meet the following password rules")) {
                    return Respuesta.estado("FORMATO_USUARIO_INVALIDO");
                }
                return Respuesta.error();
            }

            contexto.insertarContador("CAMBIO_USUARIO");
            contexto.insertarLogCambioUsuario(contexto);
        }

        // SeguridadPostClaveClave
        if (!responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
            if (Objeto.anyEmpty(clave)) {
                return Respuesta.parametrosIncorrectos();
            }

            ApiRequest requestSeguridadPostClaveClave = Api.request("CrearClave", "seguridad", "POST", "/v1/clave",
                    contexto);
            requestSeguridadPostClaveClave.body("grupo", "ClientesBH");
            requestSeguridadPostClaveClave.body("idUsuario", idCobis);
            requestSeguridadPostClaveClave.body("parametros").set("clave", clave);
            requestSeguridadPostClaveClave.body("nombreClave", "numerica");
            requestSeguridadPostClaveClave.permitirSinLogin = true;

            ApiResponse responseSeguridadPostClaveClave = Api.response(requestSeguridadPostClaveClave, idCobis);
            if (responseSeguridadPostClaveClave.hayError()) {
                if (responseSeguridadPostClaveClave.string("detalle")
                        .contains("The password does not meet the following password rules")) {
                    return Respuesta.estado("FORMATO_CLAVE_INVALIDA");
                }
                if (responseSeguridadPostClaveClave.string("mensajeAlUsuario")
                        .contains("The password does not meet the following password rules")) {
                    return Respuesta.estado("FORMATO_CLAVE_INVALIDA");
                }
                return Respuesta.error();
            }

            contexto.insertarContador("CAMBIO_CLAVE");
            contexto.insertarLogCambioClave(contexto);
        }

        if (contexto.persona().email() != null && !contexto.persona().email().isEmpty()) {
            contexto.parametros.set("ingresos", true);
            try {
                HBNotificaciones.modificarConfiguracionAlertas(contexto);
            } catch (Exception e) {
            }
        }

        contexto.limpiarSegundoFactor();
        return Respuesta.exito();
    }

    public static Respuesta nuevoUsuarioTransmit(ContextoHB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");
        String documento = contexto.parametros.string("documento");

        if (Objeto.anyEmpty(csmId, checksum, documento))
            return Respuesta.parametrosIncorrectos();

        if (!TransmitHB.validarCsmTransaccion(contexto, JourneyTransmitEnum.HB_INICIO_SESION, ""))
            return Respuesta.requiereSegundoFactor();

        Respuesta pseudoLogin = HBLogin.pseudoLogin(contexto);
        if (pseudoLogin.hayError())
            return pseudoLogin.set("csmIdAuth", contexto.csmIdAuth);

        Futuro<Boolean> futuroNuevoUsuario = new Futuro<>(() -> contexto.insertarNuevoUsuario(documento));
        Futuro<Persona> futuroPersona = new Futuro<>(contexto::persona);
        Futuro<Boolean> futuroContadorCambioUsuario = new Futuro<>(() -> contexto.insertarContador("CAMBIO_USUARIO"));
        Futuro<Boolean> futuroContadorCambioClave = new Futuro<>(() -> contexto.insertarContador("CAMBIO_CLAVE"));
        new Futuro<>(() -> contexto.insertarLogCambioClave(contexto));
        new Futuro<>(() -> contexto.insertarLogCambioUsuario(contexto));

        Persona persona = futuroPersona.tryGet();
        if (persona != null && StringUtils.isNotBlank(persona.email())) {
            contexto.parametros.set("ingresos", true);
            try {
                new Futuro<>(() -> HBNotificaciones.modificarConfiguracionAlertas(contexto));
            } catch (Exception e) {
            }
        }

        futuroContadorCambioClave.tryGet();
        futuroContadorCambioUsuario.tryGet();
        futuroNuevoUsuario.tryGet();

        return Respuesta.exito("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta cambiarUsuario(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String usuario = contexto.parametros.string("usuario");
        Boolean cambioDoble = contexto.parametros.bool("cambioDoble", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(usuario)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (documento != null) {
            Respuesta pseudoLogin = HBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        Objeto consultaForzar = EHBLogin.datosCambioUsuario(idCobis);
        Boolean esProspecto = contexto.validaPorRiesgoNet(contexto);
        Boolean realizado = consultaForzar != null && consultaForzar.bool("realizado");

        Respuesta respuestaTransmit = recomendacionTransmit(contexto, "recupero-usuario-clave");
        if (respuestaTransmit.hayError())
            return respuestaTransmit;

        // SeguridadPostCambiarUsuario
        ApiResponse response = RestSeguridad.cambiarUsuario(contexto, usuario, true);
        if (response.hayError()) {
            if (response.string("detalle").contains("Cannot Repeat History Value")) {
                return Respuesta.estado("CLAVE_REPETIDA");
            }
            if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                return Respuesta.estado("CLAVE_REPETIDA");
            }
            return Respuesta.error();
        }

        boolean esMigrado = Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitHB.esUsuarioMigrado(contexto,
                contexto.idCobis(),
                ar.com.hipotecario.backend.base.Util.documento(contexto.persona().numeroDocumento())));

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        if (consultaForzar != null && !realizado) {
            Boolean token = consultaForzar.bool("token");
            if (token) {
                if (esProspecto) {
                    if (!contexto.sesion.validaRiesgoNet) {
                        return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
                    }
                } else {
                    Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "recupero-usuario-clave", JourneyTransmitEnum.HB_INICIO_SESION);
                    if (respuestaValidaTransaccion.hayError())
                        return respuestaValidaTransaccion;
                }
            }
        } else {
            if (esProspecto) {
                if (!contexto.sesion.validaRiesgoNet) {
                    return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
                }
            } else {
                Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "recupero-usuario-clave", JourneyTransmitEnum.HB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;
            }
        }
        if (!cambioDoble)
            contexto.limpiarSegundoFactorPreCondicionLink();

        new Futuro<>(() -> contexto.insertarContador("CAMBIO_USUARIO"));
        new Futuro<>(() -> contexto.insertarLogCambioUsuario(contexto));

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
            HBMonitoring monitoringApi = new HBMonitoring();
            String codigoTransaccion = "YD0000";
            new Futuro<>(
                    () -> monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccion, null, null, null));
        }

        new Futuro<>(() -> mailCambioUsuario(contexto));

        if (!cambioDoble)
            contexto.limpiarSegundoFactor();

        //Actualizo a realizado para q no vuelva a forzar el cambio
        if (consultaForzar != null && !consultaForzar.bool("realizado"))
            updateForzarUsuario(contexto.idCobis());

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta updateForzarUsuario(String idCobis) {
        SqlRequest sqlRequest = Sql.request("updateForzarUsuario", "homebanking");
        sqlRequest.sql = "UPDATE [Homebanking].[dbo].[forzar_cambio_usuario] SET realizado = ?, fecha = ? WHERE Cobis = ?";
        sqlRequest.add("1");
        sqlRequest.add(LocalDateTime.now());
        sqlRequest.add(idCobis);
        Sql.response(sqlRequest);
        return Respuesta.exito();
    }

    public static Respuesta cambiarUsuarioLogueado(ContextoHB contexto) {
        String usuarioAnterior = contexto.parametros.string("usuarioAnterior");
        String usuarioNuevo = contexto.parametros.string("usuarioNuevo");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(usuarioAnterior, usuarioNuevo)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "recupero-usuario-clave", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (!esMigrado) {
            contexto.limpiarSegundoFactorPreCondicionLink();

            Respuesta respuestaTransmit = recomendacionTransmit(contexto, "recupero-usuario-clave");
            if (respuestaTransmit.hayError())
                return respuestaTransmit;

            // SeguridadPostCambiarUsuario
            ApiResponse response = RestSeguridad.cambiarUsuario(contexto, usuarioNuevo, false);
            if (response.hayError()) {
                if (response.string("detalle").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                return Respuesta.error();
            }

            new Futuro<>(() -> contexto.insertarContador("CAMBIO_USUARIO"));
            new Futuro<>(() -> contexto.insertarLogCambioUsuario(contexto));

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
                HBMonitoring monitoringApi = new HBMonitoring();
                String codigoTransaccion = "ZQ0000";
                monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccion, null, null, null);
            }
            mailCambioUsuario(contexto);
            contexto.limpiarSegundoFactor();
        } else {
            new Futuro<>(() -> contexto.insertarContador("CAMBIO_USUARIO"));
            new Futuro<>(() -> contexto.insertarLogCambioUsuario(contexto));
        }

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta cambiarClave(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento", null);
        String clave = contexto.parametros.string("clave");
        Boolean isForcedChange = contexto.parametros.bool("isForced");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

//        contexto.limpiarSegundoFactorPreCondicionLink();

        Respuesta respuestaTransmit = recomendacionTransmit(contexto, "cambio-clave-logeado");
        if (respuestaTransmit.hayError())
            return respuestaTransmit;

        if (Objeto.anyEmpty(clave)) {
            return Respuesta.parametrosIncorrectos();
        }

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.sinPseudoSesion();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        if (documento != null && !esMigrado) {
            Respuesta pseudoLogin = HBLogin.pseudoLogin(contexto);
            if (pseudoLogin.hayError()) {
                return pseudoLogin;
            }
        }

        Boolean esProspecto = contexto.validaPorRiesgoNet(contexto);
        if (esProspecto && !esMigrado) {
            if (!contexto.sesion.validaRiesgoNet) {
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
            }
        } else {
            Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "recupero-usuario-clave", JourneyTransmitEnum.HB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        String typeChange = isForcedChange ? "CAMBIO_CLAVE_EXPIRACION" : "CAMBIO_CLAVE";

        if (!esMigrado) {
            // CambiarClave
            ApiResponse response = RestSeguridad.cambiarClave(contexto, clave, true);
            if (response.hayError()) {
                if (response.string("detalle").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                return Respuesta.error();
            }

            new Futuro<>(() -> contexto.insertarContador(typeChange));
            new Futuro<>(() -> contexto.insertarLogCambioClave(contexto));
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
                HBMonitoring monitoringApi = new HBMonitoring();
                String codigoTransaccion = "YD0000";
                new Futuro<>(
                        () -> monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccion, null, null, null));
            }
            new Futuro<>(() -> mailCambioClave(contexto));

            contexto.limpiarSegundoFactor();
        } else {
            new Futuro<>(() -> contexto.insertarContador(typeChange));
            new Futuro<>(() -> contexto.insertarLogCambioClave(contexto));
        }
        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta cambiarClaveLogueado(ContextoHB contexto) {
        String claveAnterior = contexto.parametros.string("claveAnterior");
        String claveNueva = contexto.parametros.string("claveNueva");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(claveAnterior, claveNueva)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "cambio-clave-logeado", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (!esMigrado) {
            contexto.limpiarSegundoFactorPreCondicionLink();

            Respuesta respuestaTransmit = recomendacionTransmit(contexto, "cambio-clave-logeado");
            if (respuestaTransmit.hayError())
                return respuestaTransmit;

            ApiResponse validarClave = RestSeguridad.validarClave(contexto, claveAnterior, "");

            if (validarClave.hayError()) {
                String error = "ERROR";
                error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO"
                        : error;
                error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
                error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded")
                        ? "USUARIO_BLOQUEADO"
                        : error;
                error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

                return Respuesta.estado(error);
            }

            // CambiarClave
            ApiResponse response = RestSeguridad.cambiarClave(contexto, claveNueva, false);
            if (response.hayError()) {
                if (response.string("detalle").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                if (response.string("mensajeAlUsuario").contains("Cannot Repeat History Value")) {
                    return Respuesta.estado("CLAVE_REPETIDA");
                }
                return Respuesta.error();
            }

            new Futuro<>(() -> contexto.insertarContador("CAMBIO_CLAVE"));
            new Futuro<>(() -> contexto.insertarLogCambioClave(contexto));

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
                HBMonitoring monitoringApi = new HBMonitoring();
                String codigoTransaccion = "ZR0000";
                monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccion, null, null, null);
            }

            new Futuro<>(() -> mailCambioClave(contexto));

            contexto.limpiarSegundoFactor();
        } else {
            new Futuro<>(() -> contexto.insertarContador("CAMBIO_CLAVE"));
            new Futuro<>(() -> contexto.insertarLogCambioClave(contexto));
        }
        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Boolean mailCambioUsuario(ContextoHB contexto) {
        String emailDestino = contexto.persona().email();
        String celularDestino = contexto.persona().celular();
        // emailDestino = "semachado@hipotecario.com.ar";
        if (emailDestino != null && !emailDestino.isEmpty()) {
            ApiRequest requestMail = Api.request("CambiarUsuarioCorreoElectronico", "notificaciones", "POST",
                    "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", ConfigHB.string("doppler_cambio_usuario"));
            requestMail.permitirSinLogin = true;
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Cambio de usuario BH");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
            parametros.set("CANAL", "Home Banking");

            Api.response(requestMail, new Date().getTime());
        }

        RestNotificaciones.sendSms(contexto, celularDestino, ConfigHB.string("mensaje_sms_cambio_usuario",
                        "Modificaste tu usuario de acceso a Home Banking de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."),
                "");
        return true;
    }

    public static Boolean mailCambioClave(ContextoHB contexto) {

        String emailDestino = contexto.persona().email();
        String celularDestino = contexto.persona().celular();
        if (emailDestino != null && !emailDestino.isEmpty()) {
            ApiRequest requestMail = Api.request("CambiarClaveCorreoElectronico", "notificaciones", "POST",
                    "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", ConfigHB.string("doppler_cambio_clave"));
            requestMail.permitirSinLogin = true;
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Cambio de Clave Búho Fácil");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
            parametros.set("CANAL", "Home Banking");
            Api.response(requestMail, new Date().getTime());
        }
        RestNotificaciones.sendSms(contexto, celularDestino, ConfigHB.string("mensaje_sms_cambio_clave",
                        "Modificaste tu clave de acceso a Home Banking de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."),
                "");
        return true;
    }

    public static Respuesta encuestaUsuario(ContextoHB contexto) {
        String canal = contexto.parametros.string("canal");
        String funcionalidad = contexto.parametros.string("funcionalidad");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(canal, funcionalidad)) {
            return Respuesta.parametrosIncorrectos();
        }
        Respuesta respuesta = new Respuesta();
        List<Objeto> preguntas = SqlHomebanking.encuestaPreguntas(canal, funcionalidad);
        if (preguntas.isEmpty()) {
            return Respuesta.error();
        }
        preguntas = SqlHomebanking.mostrarEncuestaUsuario(idCobis, preguntas);
        if (preguntas.isEmpty()) {
            return Respuesta.exito();
        }

        return respuesta.set("preguntas", preguntas);
    }

    public static Respuesta guardarEncuesta(ContextoHB contexto) {
        String opinion = contexto.parametros.string("opinion");
        Integer puntuacion = contexto.parametros.integer("puntuacion");
        Integer idPregunta = contexto.parametros.integer("pregunta");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(idPregunta, puntuacion)) {
            return Respuesta.parametrosIncorrectos();
        }
        boolean response = SqlHomebanking.guardaEncuesta(idCobis, idPregunta, opinion, puntuacion);
        if (!response) {
            return Respuesta.estado("ENCUESTA_NO_GUARDADA");
        }

        return Respuesta.exito();
    }

    public static Respuesta obtenerConfiguracionVariable(ContextoHB contexto) {
        String llave = contexto.parametros.string("llave");
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(llave)) {
            return Respuesta.parametrosIncorrectos();
        }
        Respuesta respuesta = new Respuesta();
        Objeto mensaje = SqlHomebanking.obtenerConfiguracionVariable(llave);
        if (mensaje == null) {
            return Respuesta.error();
        }
        return respuesta.set("mensaje", mensaje);
    }

    /**
     * Valida contra Transmit
     *
     * @param contexto
     * @param funcionalidad
     * @return respuesta.
     */
    private static Respuesta recomendacionTransmit(ContextoHB contexto, String funcionalidad) {
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_cambio_usuario_clave",
                "prendido_modo_transaccional_cambio_usuario_clave_cobis") && !TransmitHB.isChallengeOtp(contexto, funcionalidad)) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return Respuesta.parametrosIncorrectos();

                ChangeDataHBBMBankProcess changeDataHBBMBankProcess = new ChangeDataHBBMBankProcess(contexto.idCobis(), sessionToken, BankProcessChangeDataType.ACCOUNT_DETAILS_CHANGE);

                return TransmitHB.recomendacionTransmit(contexto, changeDataHBBMBankProcess, funcionalidad);
            } catch (Exception e) {
                return Respuesta.exito();
            }
        }
        return Respuesta.exito();
    }
}
