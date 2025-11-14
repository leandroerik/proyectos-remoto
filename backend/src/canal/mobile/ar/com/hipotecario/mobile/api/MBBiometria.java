package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.AuditorLogService;
import ar.com.hipotecario.mobile.servicio.BiometriaService;
import ar.com.hipotecario.mobile.servicio.RestNotificaciones;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.RestSeguridad;
import ar.com.hipotecario.mobile.servicio.SqlBiometriaService;

public class MBBiometria {

    public static final int VALIDES_TOKEN_SECONDS = 3599;
    public static final int VALIDES_TOKEN_INTERVALO = 20;
    public static final long BLOQUEO_MODAL_BIOMETRIA_EN_DIAS = 7L;
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAymtSO8ucuAOmzWNajXXx6IGYF//GcPWYS3puUTxbn5n+Kp7n/Sl+dx/m5nC6LlTb9C59sNleAK02idlXTuWT2avGN4/+YWTnB1n/EPJSktd11Ir/wmhXfcIAI8Q2FXC6np7Ro2SGpdALjr4u+U0ERYs0yZRj0jkqX5GkxMnLv8SDcxHbAF8+hlhTwyvAPNhrg6tVR9p26Ljb/V6VhjO7q/Qkd5DOGqS8Krwgb/rrAbnqnx2GEEgkFAMlM6zZ8OFmnYeVGN3bN7lHZijQ4m7YJzS1Ck8RDpOPMmicGcJNM0DVjGxq8J0oPCQrQ5M7J8yAw3x8MrV8bt1iz9Jdgt1NqwIDAQAB";

    public RespuestaMB accessTokensAutenticador(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String deviceName = contexto.parametros.string("device_name");
        String deviceType = contexto.parametros.string("device_type");
        Boolean fingerprintSupport = contexto.parametros.bool("fingerprint_support");
        String osVersion = contexto.parametros.string("os_version");
        String pushToken = contexto.parametros.string("push_token");
        Boolean notificar = contexto.parametros.bool("notificar", false);
        RespuestaMB respuesta = new RespuestaMB();

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(deviceName, deviceType, fingerprintSupport, osVersion, pushToken)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (!contexto.validaSegundoFactor("biometria")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        ApiResponseMB response = BiometriaService.generaTokensAutenticador(contexto);
        if (response.hayError()) {
            return RespuestaMB.estado("ERROR_TOKEN_BIOMETRIA");
        }

        contexto.sesion().setCache("token_biometria", response.string("access_token"));
        contexto.sesion().setCache("dispositivo", response.string("authenticator_id"));
        contexto.sesion().setCache("refresh_token_biometria", response.string("refresh_token"));
        contexto.sesion().setCache("fecha_token_biometria", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ss").format(new Date()));
        contexto.sesion().setCache("expires_in_biometria", response.string("expires_in"));

        contexto.parametros.set("registraDispositivo", true);
        RespuestaMB respuestaInsert = seteaRegistroDispositivo(contexto);
        if (respuestaInsert.hayError()) {
            return respuestaInsert;
        }

        respuesta.set("access_token", response.string("access_token"));
        respuesta.set("refresh_token", response.string("refresh_token"));
        respuesta.set("scope", response.string("scope"));
        respuesta.set("authenticator_id", response.string("authenticator_id"));
        respuesta.set("token_type", response.string("token_type"));
        respuesta.set("display_name", response.string("display_name"));
        respuesta.set("expires_in", response.integer("expires_in"));

        boolean isSalesforce = MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto);
        if (isSalesforce) {
            if (sqlBiometriaService.selectCountAccesos(contexto).registros.get(0).integer("dispositivos") > 1) {
                Objeto parametros = new Objeto();
                String salesforce_email_doble_biometria = ConfigMB.string("salesforce_email_doble_biometria");
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
                parametros.set("ENVIA_MAIL", notificar);
                new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_email_doble_biometria, parametros));
            } else {
                Objeto parametros = new Objeto();
                String salesforce_email_biometria = ConfigMB.string("salesforce_email_biometria");
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
                parametros.set("ENVIA_MAIL", notificar);
                new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_email_biometria, parametros));
            }
        }

        if (notificar && !isSalesforce) {
            if (sqlBiometriaService.selectCountAccesos(contexto).registros.get(0).integer("dispositivos") > 1) {
                envioEmailBiometria(contexto, ConfigMB.string("doppler_email_doble_biometria"), "Alerta de Seguridad. Nuevo dispositivo validado");
            } else {
                envioEmailBiometria(contexto, ConfigMB.string("doppler_email_biometria"), "Activaste tu inicio de sesión rápido");
            }
        }

        return respuesta;
    }

    public static RespuestaMB refreshTokens(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String refreshToken = contexto.sesion().cache("refresh_token_biometria") == null ? contexto.parametros.string("refresh_token") : contexto.sesion().cache("refresh_token_biometria");

        if (Objeto.anyEmpty(refreshToken)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = null;
        RespuestaMB respuesta = new RespuestaMB();
        try {
            response = BiometriaService.generaRefreshTokens(contexto, refreshToken);
            if (response.hayError()) {
                response = verificaValidezToken(contexto);
                if (response == null || response.hayError()) {
                    return RespuestaMB.estado("ERROR_REFRESS_TOKEN");
                }
            }

            Boolean resp = sqlBiometriaService.updateToken(contexto, response.string("access_token").trim(), response.string("refresh_token").trim(), response.string("authenticator_id").trim());
            if (!resp) {
                updateBiometriaLog(contexto, response, "ERROR_REFRESS_TOKEN_BD");
                return RespuestaMB.estado("ERROR_REFRESS_TOKEN_BD");
            }

            updateBiometriaLog(contexto, response, "UpdateRealizado");
            respuesta.set("access_token", response.string("access_token"));
            respuesta.set("refresh_token", response.string("refresh_token"));
            respuesta.set("scope", response.string("scope"));
            respuesta.set("authenticator_id", response.string("authenticator_id"));
            respuesta.set("token_type", response.string("token_type"));
            respuesta.set("display_name", response.string("display_name"));
            respuesta.set("expires_in", response.integer("expires_in"));

        } catch (Exception e) {
            updateBiometriaLog(contexto, response, "ExceptionGeneradaRefrehToken: " + e);
        }
        return respuesta;
    }

    public RespuestaMB enrolaAutenticador(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String token = contexto.sesion().cache("token_biometria") == null ? contexto.parametros.string("token", null) : contexto.sesion().cache("token_biometria");
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo", null) : contexto.sesion().cache("dispositivo");
        String publicKey = contexto.parametros.string("publicKey");
        String metodo = contexto.parametros.string("metodo");
        Boolean otp = contexto.parametros.bool("otp", false);

        if (Objeto.anyEmpty(dispositivo, publicKey, metodo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (otp && !contexto.validaSegundoFactor("biometria")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        RespuestaMB accesos = verificaAccesosRefresh(contexto, dispositivo);
        if (accesos.hayError()) {
            return accesos;
        }

        Objeto usuarioIsva = consultarUsuarioIsva(contexto);
        if (usuarioIsva == null || usuarioIsva.objetos("authenticators").isEmpty()) {
            return RespuestaMB.estado("ERROR_USUARIO_INVALIDO");
        }

        if (!validaEnrolamiento(metodo, usuarioIsva)) {
            token = contexto.sesion().cache("token_biometria");
            ApiResponseMB response = BiometriaService.enrolaBiometria(contexto, metodo, dispositivo, token, publicKey);
            if (response.hayError()) {
                return RespuestaMB.estado("ERROR_ENROLAR_AUTENTICADOR");
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        contexto.parametros.set("biometria", true);
        seteaAccesoBiometria(contexto);
        if (sqlBiometriaService.selectCountAccesos(contexto).registros.get(0).integer("dispositivos") > 1) {
            envioEmailBiometria(contexto, "huella".equalsIgnoreCase(metodo) ? ConfigMB.string("doppler_email_doble_biometria_huella") : ConfigMB.string("doppler_email_doble_biometria_rostro"), "Alerta de Seguridad. Nuevo dispositivo validado");
        } else {
            envioEmailBiometria(contexto, "huella".equalsIgnoreCase(metodo) ? ConfigMB.string("doppler_email_biometria_huella") : ConfigMB.string("doppler_email_biometria_rostro"), "Activaste tu inicio de sesión rápido");
        }
        respuesta.set("activaModalBio", mostrarModalBiometria(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ss").format(new Date())));

        return respuesta;
    }

    private RespuestaMB revocadorValidate(ContextoMB contexto) {
        Boolean isRevokedEnable = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_restringe_biometria");
        String publicKey = contexto.parametros.string("publicKey");
        if (StringUtils.isEmpty(contexto.idCobis())) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (StringUtils.isEmpty(publicKey)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (isRevokedEnable && !contexto.validaSegundoFactor("biometria-revocar")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }
        return null;
    }

    public RespuestaMB revocaAutenticador(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String publicKey = contexto.parametros.string("publicKey");
        String dispositivo = contexto.sesion().cache("dispositivo");
        String metodo = "todo";
        RespuestaMB validated = this.revocadorValidate(contexto);
        if (Objects.nonNull(validated)) {
            return validated;
        }

        RespuestaMB accesos = verificaAccesosRefresh(contexto, dispositivo);
        if (accesos.hayError()) {
            Boolean deleted = sqlBiometriaService.deleteAccesos(contexto);
            if (deleted) {
                RespuestaMB.exito();
            }
            return accesos;
        }

        RespuestaMB usuarioIsva = consultarUsuarioIsva(contexto);
        if (usuarioIsva.hayError() || !usuarioIsva.existe("authenticators")) {
            return RespuestaMB.estado("ERROR_USUARIO_SIN_REGISTROS_ISVA");
        }

        String token = contexto.sesion().cache("token_biometria");
        dispositivo = contexto.sesion().cache("dispositivo");
        ApiResponseMB response = BiometriaService.revocaBiometria(contexto, metodo, token, dispositivo, publicKey);
        if (response.hayError()) {
            sqlBiometriaService.deleteAccesos(contexto);
            return RespuestaMB.estado("ERROR_REVOCA_AUTENTICADOR");
        }
        Boolean deleted = sqlBiometriaService.deleteAccesos(contexto);
        if (!deleted) {
            RespuestaMB.estado("SIN_EFECTO");
        }
        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public RespuestaMB otpBiometria(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
//		String token = contexto.sesion().cache("token_biometria") == null ? contexto.parametros.string("token") : contexto.sesion().cache("token_biometria");
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion().cache("dispositivo");
        String metodo = contexto.parametros.string("metodo");
        String documento = contexto.parametros.string("documento");
        String idCobis = contexto.idCobis();
        String ip = contexto.ip();

        String acceso = "A002";

        // TODO fingerprint setear la llegada desde front
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_dobleFactor_biometria")) {
            return RespuestaMB.estado("ERROR_TRANSACCION_NO_INICIADA");
        }

        if (Objeto.anyEmpty(dispositivo, documento)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB accesos = verificaAccesosRefresh(contexto, dispositivo);
        if (accesos.hayError()) {
            return accesos;
        }

        List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, documento, null, null);
        if (listaIdCobis == null) {
            return RespuestaMB.error();
        } else if (listaIdCobis.isEmpty()) {
            return RespuestaMB.estado("USUARIO_INVALIDO");
        } else if (listaIdCobis.size() > 1) {
            return RespuestaMB.estado("USUARIO_INVALIDO");
            // return Respuesta.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        String idCobisXDocumento = listaIdCobis.get(0);
        RespuestaMB respuesta = new RespuestaMB();
        if (documento != null && contexto.idCobis().equalsIgnoreCase(idCobisXDocumento)) {
            IsvaDTO isvaDTO = new IsvaDTO();
            String refreshToken = contexto.sesion().cache("refresh_token_biometria");
            respuesta = setearTransaccionDispositivo(contexto, contexto.sesion().cache("token_biometria"), dispositivo, fingerprint, isvaDTO);
            if (respuesta.string("estado").contains("ERROR")) {
                sqlBiometriaService.biometriaInsertLog(acceso, metodo, respuesta.string("estado"),
                        idCobis, dispositivo, refreshToken, ip);
                return respuesta;
            }
            contexto.sesion().setValidaSegundoFactorBiometria(true);
            contexto.sesion().setExpiracionOtp(null);
            // logs asociados a esta interaccion
            sqlBiometriaService.biometriaInsertLog(acceso, metodo, respuesta.string("estado"), idCobis, dispositivo, refreshToken, ip);
        } else {
            respuesta = RespuestaMB.estado("USUARIO_INVALIDO");
        }

        return respuesta;
    }

    public static RespuestaMB otpBuhoFacil(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion().cache("dispositivo");
        String clave = contexto.parametros.string("clave");
        Integer intentos = contexto.sesion().cache("intentosBuhoFacil") == null ? 0 : Integer.parseInt(contexto.sesion().cache("intentosBuhoFacil"));
        String acceso = "A002";
        String idCobis = contexto.idCobis();
        String ip = contexto.ip();

        // TODO el front deberia de enviarme el fingerprint
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_dobleFactor_buhofacil")) {
            return RespuestaMB.estado("ERROR_DOBLEFACTOR_INVALIDO");
        }

        if (Objeto.anyEmpty(dispositivo, clave)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (contexto.sesion().cache("dispositivo") == null || !contexto.sesion().cache("dispositivo").equalsIgnoreCase(contexto.parametros.string("dispositivo"))) {
            return RespuestaMB.estado("ERROR_USUARIO_INVALIDO");
        }

        String refreshToken = contexto.sesion().cache("refresh_token_biometria");
        if (intentos == 2) {
            sqlBiometriaService.biometriaInsertLog(acceso, "", "ERROR_MAXIMO_INTENTOS", idCobis,
                    dispositivo, refreshToken, ip);
            return RespuestaMB.estado("ERROR_MAXIMO_INTENTOS");
        }

        ApiResponseMB validarClave = RestSeguridad.validarClave(contexto, clave, fingerprint);

        if (validarClave.hayError()) {
            String error = "ERROR";
            intentos += 1;
            contexto.sesion().setCache("intentosBuhoFacil", String.valueOf(intentos));
            error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

            error = validarClave.string("mensajeAlUsuario").contains("The password authentication failed") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA" : error;


            sqlBiometriaService.biometriaInsertLog(acceso, "", error, idCobis, dispositivo, refreshToken, ip);

            return RespuestaMB.estado(error);
        }

        contexto.sesion().delCache("intentosBuhoFacil");
        contexto.sesion().setValidaSegundoFactorBuhoFacil(true);
        contexto.sesion().setExpiracionOtp(null);

        sqlBiometriaService.biometriaInsertLog(acceso, "", "0", idCobis, dispositivo, refreshToken, ip);
        return RespuestaMB.exito();
    }

    public static RespuestaMB consultarUsuarioIsva(ContextoMB contexto) {
        String token = contexto.sesion().cache("token_biometria") == null ? contexto.parametros.string("token") : contexto.sesion().cache("token_biometria");
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion().cache("dispositivo");

        if (Objeto.anyEmpty(token, dispositivo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = BiometriaService.consultaUsuario(contexto, token);
        if (response.hayError()) {
            return RespuestaMB.estado("ERROR_CONSULTA_USUARIO_ISVA");
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("authenticators", respuestaUsuarioIsva(contexto, response, "authenticators", dispositivo));
        respuesta.set("userPresenceMethods", respuestaUsuarioIsva(contexto, response, "userPresenceMethods", dispositivo));
        respuesta.set("fingerprintMethods", respuestaUsuarioIsva(contexto, response, "fingerprintMethods", dispositivo));

        return respuesta;
    }

    private static String iniciarTransaccion(ContextoMB contexto, String dispositivo, String fingerprint, IsvaDTO isvaDTO) {
        String metodo = contexto.parametros.string("metodo");

        ApiResponseMB response = BiometriaService.inciaTransaccion(contexto, metodo, fingerprint);
        if (response.hayError()) {
            return "ERROR_TRANSACCION_NO_INICIADA";
        }

        if (!ConfigMB.esProduccion()) {
            contexto.responseHeader("stateId-a", response.string("stateId"));
        }

        contexto.sesion().setCache("stateId", response.string("stateId"));
        contexto.sesion().setCache("dispositivo", dispositivo);
        contexto.sesion().setCookieOtp(response.headers.get("set-cookie"));

        isvaDTO.stateId = response.string("stateId");
        isvaDTO.dispositivo = dispositivo;
        isvaDTO.cookie = response.headers.get("set-cookie");

        return "";
    }

    private static RespuestaMB setearTransaccion(ContextoMB contexto, IsvaDTO isvaDTO) {
        String stateId = isvaDTO.stateId;
        String dispositivo = isvaDTO.dispositivo;

        if (!ConfigMB.esProduccion()) {
            contexto.responseHeader("stateId-c", stateId);
        }

        ApiResponseMB response = BiometriaService.seteaTransaccion(contexto, dispositivo, stateId, isvaDTO);
        if (response.hayError() || !"".equals(response.string("errorMessage"))) {
            return null;
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("stateId_transaccion", response.string("action"));
        respuesta.set("dispositivo", response.string("mmfa_transaction_id"));
        respuesta.set("estado", response.string("mmfa_transaction_status"));
        return respuesta;
    }

    private static String validaTransaccionPendiente(ContextoMB contexto, String token) {

        ApiResponseMB response = BiometriaService.trasaccionPendiente(contexto, token);
        if (response.hayError() || response.codigo > 400) {
            return "ERROR_TRANSACCION_INVALIDA";
        }

        String transactionId = "";
        Objeto listaTransacciones = response.objetos("urn:ietf:params:scim:schemas:extension:isam:1.0:MMFA:Transaction").get(0);
        if (!listaTransacciones.objetos("transactionsPending").isEmpty()) {
            List<Objeto> objeto = listaTransacciones.objetos("transactionsPending");
            transactionId = objeto.get(0).string("transactionId");
        }

        return transactionId;
    }

    private static String transaccionCompletada(ContextoMB contexto, String transactionId, IsvaDTO isvaDTO) {
        String dispositivo = isvaDTO.dispositivo;
        String token = contexto.sesion().cache("token_biometria");
        String pushToken = contexto.parametros.string("push_token");

        if (Objeto.empty(transactionId) || transactionId.contains("ERROR")) {
            return "ERROR_TRANSACCION_INVALIDA";
        }

        ApiResponseMB response = BiometriaService.seteaTransaccionCompletada(contexto, dispositivo, token, transactionId, pushToken);
        if (response.hayError()) {
            return "ERROR_TRANSACCION_NO_COMPLETADA";
        }

        return "TRANSACCION_COMPLETADA";
    }

    public static class IsvaDTO {
        public String stateId;
        public String dispositivo;
        public String cookie;
    }

    public static RespuestaMB setearTransaccionDispositivo(ContextoMB contexto, String token, String dispositivo, String fingerprint, IsvaDTO isvaDTO) {
        if (!"".equals(iniciarTransaccion(contexto, dispositivo, fingerprint, isvaDTO))) {
            return RespuestaMB.estado("ERROR_TRANSACCION_NO_INICIADA");
        }

        if (!ConfigMB.esProduccion()) {
            contexto.responseHeader("stateId-b", contexto.sesion().cache("stateId"));
        }

        setearTransaccion(contexto, isvaDTO);
        return RespuestaMB.estado(transaccionCompletada(contexto, validaTransaccionPendiente(contexto, token), isvaDTO));
    }

    private static Objeto respuestaUsuarioIsva(ContextoMB contexto, ApiResponseMB response, String clave, String dispositivo) {
        Objeto objeto = response.objeto("urn:ietf:params:scim:schemas:extension:isam:1.0:MMFA:Authenticator");
        List<Objeto> lista = objeto.objeto(clave).objetos();
        if (lista.isEmpty()) {
            return null;
        }

        for (Objeto obj : lista) {
            if (clave.equalsIgnoreCase("authenticators") && obj.string("id").equals(dispositivo)) {
                return obj;
            }

            if ((clave.equalsIgnoreCase("userPresenceMethods") || clave.equalsIgnoreCase("fingerprintMethods")) && dispositivo.equals(obj.string("authenticator"))) {
                return obj;
            }

        }
        return null;
    }

    public static RespuestaMB verificaAccesosCompletos(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo", null) : contexto.sesion().cache("dispositivo");

        RespuestaMB respuesta = new RespuestaMB();
        SqlResponseMB response = sqlBiometriaService.selectAccesos(contexto, dispositivo);

        if (response.hayError) {
            return respuesta.setEstado("ERROR_ACCESO_BIOMETRIA");
        }

        if (response.registros.isEmpty()) {
            return respuesta.setEstado("NO_TIENE_ACCESOS_BIOMETRIA");
        }

        Objeto registro = response.registros.get(0);
        if ((registro.integer("disp_registrado") == 0 && registro.integer("biometria_activa") == 0) || (Objeto.anyEmpty(registro.string("access_token").trim(), registro.string("refresh_token").trim(), registro.date("fecha_token")))) {
            return respuesta.setEstado("NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA");
        }

        respuesta.set("fechaRegistro", registro.string("fecha_disp_registrado"));
        respuesta.set("dispositivoRegistrado", registro.string("disp_registrado"));
        respuesta.set("biometriaActiva", registro.string("biometria_activa"));
        respuesta.set("fechaBiometriaActiva", registro.string("fecha_biometria_activa"));
        respuesta.set("buhoFacilActivo", registro.string("buhoFacil_activo"));
        respuesta.set("fechaBuhoFacilActivo", registro.string("fecha_buhoFacil_activo"));
        respuesta.set("idDispositivo", registro.string("id_dispositivo").trim());
        respuesta.set("accessToken", registro.string("access_token").trim());
        respuesta.set("refreshToken", registro.string("refresh_token").trim());
        respuesta.set("fechaToken", registro.date("fecha_token"));

        return respuesta;
    }

    public static RespuestaMB verificaAccesos(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.parametros.string("dispositivo", null);

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        RespuestaMB respuesta = new RespuestaMB();
        SqlResponseMB response = sqlBiometriaService.selectAccesos(contexto, dispositivo);

        if (response.hayError) {
            return respuesta.setEstado("ERROR_ACCESO_BIOMETRIA");
        }

        if (response.registros.isEmpty()) {
            if (sqlBiometriaService.selectCountAccesos(contexto).registros.get(0).integer("dispositivos") >= 1) {
                return respuesta.setEstado("ERROR_TIENE_DISP_REGISTRADOS");
            }
            return respuesta.setEstado("NO_TIENE_ACCESOS_BIOMETRIA");
        }

        Objeto registro = response.registros.get(0);
        respuesta.set("fechaRegistro", registro.string("fecha_disp_registrado"));
        respuesta.set("dispositivoRegistrado", registro.string("disp_registrado"));
        respuesta.set("biometriaActiva", registro.string("biometria_activa"));
        respuesta.set("fechaBiometriaActiva", registro.string("fecha_biometria_activa"));
        respuesta.set("biometriaActivaISVA", registro.string("fecha_biometria_activa") != null && "1".equalsIgnoreCase(registro.string("biometria_activa")));
        respuesta.set("buhoFacilActivo", registro.string("buhoFacil_activo"));
        respuesta.set("fechaBuhoFacilActivo", registro.string("fecha_buhoFacil_activo"));
        respuesta.set("activaModalBio", mostrarModalBiometria(registro.string("fecha_biometria_activa")));

        return respuesta;
    }

    public RespuestaMB seteaAccesoBuhoFacil(ContextoMB contexto) {
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion().cache("dispositivo");
        Boolean buhoFacilActivo = contexto.parametros.bool("buhoFacil", null);

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(buhoFacilActivo, dispositivo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Boolean response = new SqlBiometriaService().updateAccesoBuhoFacil(contexto, buhoFacilActivo, dispositivo);
        if (!response) {
            return respuesta.setEstado("ERROR_BD_ACCESO_BUHO_FACIL");
        }

        return RespuestaMB.exito();
    }

    public RespuestaMB seteaAccesoBiometria(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.sesion().cache("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion().cache("dispositivo");
        Boolean biometriaActiva = contexto.parametros.bool("biometria", null);

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(biometriaActiva, dispositivo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Boolean response = sqlBiometriaService.updateAccesoBiometria(contexto, biometriaActiva, dispositivo);
        if (!response) {
            return respuesta.setEstado("ERROR_BD_ACCESO_BIOMETRIA");
        }

        return RespuestaMB.exito();
    }

    public RespuestaMB seteaRegistroDispositivo(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        Boolean dispositivoRegistrado = contexto.parametros.bool("registraDispositivo", false);
        Boolean biometriaActiva = contexto.parametros.bool("registraBiometria", false);
        Boolean buhoFacilActivo = contexto.parametros.bool("registraBuhoFacil", true);
        String dispositivo = Objeto.empty(contexto.parametros.string("dispositivo")) ? contexto.sesion().cache("dispositivo") : contexto.parametros.string("dispositivo");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        RespuestaMB respuesta = new RespuestaMB();
        Boolean response = sqlBiometriaService.insertRegistroDispositivo(contexto, dispositivoRegistrado, biometriaActiva, buhoFacilActivo, dispositivo);
        if (!response) {
            return respuesta.setEstado("ERROR_BD_REGISTRO_DISPOSITIVO");
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB eliminaAccesoBiometria(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        sqlBiometriaService.deleteAccesos(contexto);
        return RespuestaMB.exito();
    }

    public static RespuestaMB envioEmailBiometria(ContextoMB contexto, String plantilla, String titulo) {

        try {
            Objeto parametros = new Objeto();
            parametros.set("Subject", titulo);
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));

            RestNotificaciones.envioMail(contexto, plantilla, parametros);
        } catch (Exception e) {
        }

        return RespuestaMB.exito();
    }

    public static Boolean validaEnrolamiento(String metodo, Objeto usuarioIsva) {
        return ((metodo.equalsIgnoreCase("rostro") && !usuarioIsva.objetos("userPresenceMethods").isEmpty()) || (metodo.equalsIgnoreCase("huella") && !usuarioIsva.objetos("fingerprintMethods").isEmpty()));
    }

    public static boolean verificaAccessToken(ContextoMB contexto, Date fechaToken) {
        LocalDateTime fechaTokenCreado = null;
        Integer expires_in = contexto.sesion().cache("expires_in_biometria") == null ? VALIDES_TOKEN_SECONDS : Integer.parseInt(contexto.sesion().cache("expires_in_biometria"));

        try {
            fechaTokenCreado = Fecha.convertToLocalDateTime(fechaToken);

            if (LocalDateTime.now().isAfter(fechaTokenCreado.plusSeconds(Math.subtractExact(expires_in, VALIDES_TOKEN_INTERVALO)))) {
                MBBiometria.refreshLog(contexto, fechaTokenCreado, LocalDateTime.now(), contexto.sesion().cache("dispositivo"), expires_in, "");
                return true;
            }
        } catch (DateTimeParseException e) {
            MBBiometria.refreshLog(contexto, fechaTokenCreado, LocalDateTime.now(), contexto.sesion().cache("dispositivo"), expires_in, e.getMessage());
            return true;
        }

        return false;
    }

    public static boolean loginLog(ContextoMB contexto, String tipoMetodo, String metodo, String dispositivo, String token) {
        try {
            Objeto detalle = new Objeto();
            detalle.set("peticion", "api/loginB");
            if (metodo.equalsIgnoreCase("biometriaActiva")) {
                detalle.set("Tipo Autenticación", tipoMetodo);
            }
            detalle.set("Autenticación", metodo);
            detalle.set("Dispositivo", dispositivo);
            detalle.set("Token", token);
            AuditorLogService.biometriaLogVisualizador(contexto, "Api-seguridad_loginB", detalle);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void refreshLog(ContextoMB contexto, LocalDateTime horaBD, LocalDateTime horaSistema, String dispositivo, int expires_in, String mensaje) {
        Objeto detalle = new Objeto();
        detalle.set("Petición", "api/refressTokenBiometria");
        detalle.set("Hora BD", horaBD.toString());
        detalle.set("Hora Sistema", horaSistema.toString());
        detalle.set("Dispositivo", dispositivo);
        detalle.set("Expira", String.valueOf(expires_in));
        detalle.set("Mensaje", mensaje);
        AuditorLogService.biometriaLogVisualizador(contexto, "Api-seguridad_LogRefreshToken", detalle);
    }

    private static RespuestaMB verificaAccesosRefresh(ContextoMB contexto, String dispositivo) {
        Date fechaToken = null;
        if (Objeto.empty(contexto.sesion().cache("token_biometria"))) {
            RespuestaMB respAccesos = MBBiometria.verificaAccesosCompletos(contexto);
            if (respAccesos.hayError()) {
                return respAccesos;
            }

            contexto.sesion().setCache("token_biometria", respAccesos.string("accessToken"));
            contexto.sesion().setCache("refresh_token_biometria", respAccesos.string("refreshToken"));
            contexto.sesion().setCache("fecha_token_biometria", respAccesos.string("fechaToken"));
            contexto.sesion().setCache("dispositivo", dispositivo == null ? respAccesos.string("idDispositivo") : dispositivo);

        }

        fechaToken = Fecha.stringToDate(contexto.sesion().cache("fecha_token_biometria"), "yyyy-MM-dd HH:mm:ss.ss");

        if (MBBiometria.verificaAccessToken(contexto, fechaToken)) {
            contexto.parametros.set("refresh_token", contexto.sesion().cache("refresh_token_biometria"));
            RespuestaMB respToken = MBBiometria.refreshTokens(contexto);
            if (respToken.hayError()) {
                return respToken;
            }
            contexto.sesion().setCache("token_biometria", respToken.string("access_token"));
            contexto.sesion().setCache("refresh_token_biometria", respToken.string("refresh_token"));
            contexto.sesion().setCache("fecha_token_biometria", contexto.sesion().cache("fecha_token_biometria"));
            contexto.sesion().setCache("expires_in_biometria", respToken.string("expires_in"));
            contexto.sesion().setCache("dispositivo", dispositivo == null ? respToken.string("authenticator_id") : dispositivo);
        }
        return RespuestaMB.exito();
    }

    private static void updateBiometriaLog(ContextoMB contexto, ApiResponseMB response, String updateRealizado) {
        Objeto detalle = new Objeto();
        detalle.set("peticion", "/v1/refress");
        if (response.hayError()) {
            detalle.set("error", response.json);
        } else {
            detalle.set("access_token", response.string("access_token"));
            detalle.set("refresh_token", response.string("refresh_token"));
            detalle.set("authenticator_id", response.string("authenticator_id"));
        }
        detalle.set("updateRealizado", updateRealizado);

        AuditorLogService.biometriaLogVisualizador(contexto, "Api-Biometria_UpdateBiometriaLog", detalle);
    }

    private static ApiResponseMB verificaValidezToken(ContextoMB contexto) {
        SqlResponseMB response = new SqlBiometriaService().selectLogsBiometria(contexto);

        if (!response.registros.isEmpty()) {
            Objeto objeto = response.registros.get(0);
            String refreshTokenLogs = objeto.string("refresh_token").trim();
            return BiometriaService.generaRefreshTokens(contexto, refreshTokenLogs);
        }
        return null;
    }

    private static Boolean mostrarModalBiometria(String fechaRegistracionBiometria) {
        if (!ConfigMB.bool("deshabilitar_isva", false)) {
            if (fechaRegistracionBiometria == "" || fechaRegistracionBiometria == null) {
                return false;
            }
            Date fechaRegistro = Fecha.stringToDate(fechaRegistracionBiometria, "yyyy-MM-dd HH:mm:ss.ss");
            Date fechaEstimada = Fecha.sumarDias(fechaRegistro, BLOQUEO_MODAL_BIOMETRIA_EN_DIAS);

            if (new Date().after(fechaEstimada)) {
                return true;
            }
        }
        return false;
    }

    // Revocar por problemas en el token durante el login
    public static RespuestaMB revocarBiometria(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.parametros.string("dispositivo", null);

        try {

            verificaAccesosRefresh(contexto, dispositivo);
            String token = contexto.sesion().cache("token_biometria");

            if (token == null || token.isEmpty()) {
                SqlResponseMB response = sqlBiometriaService.selectAccesos(contexto, dispositivo);

                if (response.hayError) {
                    return RespuestaMB.estado("ERROR_ACCESO_BIOMETRIA");
                }
                if (response.registros != null && response.registros.get(0) != null) {
                    token = response.registros.get(0).string("accessToken").trim();
                }
            }

            ApiResponseMB response = BiometriaService.revocaBiometriaLogin(contexto, "todo", PUBLIC_KEY, token, dispositivo != null ? dispositivo : "");

            Boolean deleted = sqlBiometriaService.deleteAccesos(contexto);

            if (response.hayError()) {
                return RespuestaMB.estado("ERROR_REVOCA_AUTENTICADOR");
            }
            if (!deleted) {
                RespuestaMB.estado("SIN_EFECTO");
            }

        } catch (Exception e) {
            sqlBiometriaService.deleteAccesos(contexto);
            return RespuestaMB.estado("ERROR_REVOCA_AUTENTICADOR");
        }
        return RespuestaMB.exito();

    }

    /* Inicio Refactor */

    /* Inicio Constantes */

    private static final String ERROR_ACCESO_BIOMETRIA = "ERROR_ACCESO_BIOMETRIA";
    private static final String ERROR_REVOCA_AUTENTICADOR = "ERROR_REVOCA_AUTENTICADOR";
    private static final String METODO_REVOCAR = "todo";
    private static final String ERROR_REFRESS_TOKEN = "ERROR_REFRESS_TOKEN";
    private static final String ERROR_REFRESS_TOKEN_BD = "ERROR_REFRESS_TOKEN_BD";


    /* Fin Constantes */

    /* Inicio Métodos Publicos */

    public static RespuestaMB revocarBiometriaNew(ContextoMB contexto) {
        SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
        String dispositivo = contexto.parametros.string("dispositivo", "");

        try {
            String token = contexto.sesion().cache("token_biometria");
            if (Objeto.empty(token)) {
                SqlResponseMB response = sqlBiometriaService.selectAccesos(contexto, dispositivo);
                if (response.hayError)
                    return RespuestaMB.estado(ERROR_ACCESO_BIOMETRIA);
                if (response.registros != null && !response.registros.isEmpty())
                    token = response.registros.get(0).string("access_token").trim();
            }

            ApiResponseMB response = BiometriaService.revocaBiometriaLogin(contexto, METODO_REVOCAR, PUBLIC_KEY, token, dispositivo);
            sqlBiometriaService.deleteAccesos(contexto);

            if (response.hayError())
                return RespuestaMB.estado(ERROR_REVOCA_AUTENTICADOR);

        } catch (Exception e) {
            sqlBiometriaService.deleteAccesos(contexto);
            return RespuestaMB.estado(ERROR_REVOCA_AUTENTICADOR);
        }
        return RespuestaMB.exito();
    }

    public static RespuestaMB refreshTokensNew(ContextoMB contexto, String refreshToken) {
        if (Objeto.anyEmpty(refreshToken))
            return RespuestaMB.parametrosIncorrectos();

        ApiResponseMB response = null;
        try {
            response = BiometriaService.generaRefreshTokens(contexto, refreshToken);
            if (response.hayError())
                return RespuestaMB.estado(ERROR_REFRESS_TOKEN);

            if (!new SqlBiometriaService().updateToken(contexto, response.string("access_token").trim(), response.string("refresh_token").trim(), response.string("authenticator_id").trim())) {
                updateBiometriaLog(contexto, response, ERROR_REFRESS_TOKEN_BD);
                return RespuestaMB.estado(ERROR_REFRESS_TOKEN_BD);
            }

            updateBiometriaLog(contexto, response, "UpdateRealizado");

            return new RespuestaMB().exito().set("access_token", response.string("access_token"))
                    .set("refresh_token", response.string("refresh_token"))
                    .set("scope", response.string("scope"))
                    .set("authenticator_id", response.string("authenticator_id"))
                    .set("token_type", response.string("token_type"))
                    .set("display_name", response.string("display_name"))
                    .set("expires_in", response.integer("expires_in"));
        } catch (Exception e) {
            updateBiometriaLog(contexto, response, "ExceptionGeneradaRefrehToken: " + e);
            return RespuestaMB.estado(ERROR_REFRESS_TOKEN);
        }
    }

    /* Fin Métodos Publicos */

    /* Inicio Métodos Privados */
    /* Fin Métodos Privados */

    /* Fin Refactor */

}
