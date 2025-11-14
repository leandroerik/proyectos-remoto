package ar.com.hipotecario.backend.util;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.RequestMigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.ResponseMigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.*;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaRequest;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaResponse;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.DatosAdicionalesCsm;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.config.TransmitGatewayAdapters;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.config.db.SQLServerConnection;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.BankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.LoginBEBankProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.util.JSONObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class Transmit {

    public static final String DENY = "DENY";
    public static final String CHALLENGE = "CHALLENGE";
    public static final String ALLOW = "ALLOW";

    private static Logger log = LoggerFactory.getLogger(Transmit.class);
    public static final String BH_COM_AR = "@bh.com.ar";

    private static final String ERROR = "ERROR_";

    private static final String SESSION_TOKEN = "sessionToken";

    public static String getSessionToken() {
        return SESSION_TOKEN;
    }

    public static String getErrorDeny() {
        return ERROR.concat(DENY);
    }

    public static String getErrorChallenge() {
        return ERROR.concat(CHALLENGE);
    }

    public static void iniciarTransmit(Contexto contexto, String canal) {
        try {
            SQLServerConnection.init(contexto.config.string("drs_bd_url"), contexto.config.string("drs_bd_usr").concat(BH_COM_AR), contexto.config.string("drs_bd_pass"));
            TransmitGatewayAdapters.init(contexto.config.string("url_transmit"), getUsuarioTransmit(contexto, canal), getClaveTransmit(contexto, canal));
        } catch (Exception e) {
            log.info("Error al iniciar Transmit en canal ".concat(canal).concat(" - ").concat(e.getMessage()));
        }
    }

    private static String getUsuarioTransmit(Contexto contexto, String canal) {
        return switch (canal) {
            case "buhobank" -> contexto.config.string("bb_app_drs_client_id");
            case "homebanking" -> contexto.config.string("hb_app_drs_client_id");
            case "mobile" -> contexto.config.string("mb_app_drs_client_id");
            case "OB" -> contexto.config.string("REACT_APP_DRS_CLIENT_ID");
            default -> "";
        };
    }

    private static String getClaveTransmit(Contexto contexto, String canal) {
        return switch (canal) {
            case "buhobank" -> contexto.config.string("bb_app_drs_secret_id");
            case "homebanking" -> contexto.config.string("hb_app_drs_secret_id");
            case "mobile" -> contexto.config.string("mb_app_drs_secret_id");
            case "OB" -> contexto.config.string("REACT_APP_DRS_SECRET_ID");
            default -> "";
        };
    }

    protected static Futuro<RecommendationDTO> recomendacion(BankProcess objeto, AuditLogReport auditLogReport) {
        ObjectMapper m = new ObjectMapper();
        try {
            String datos = m.writeValueAsString(objeto);
            loguearInfo("recomendacion", datos);
            return new Futuro<>(() -> Servidor.recommendationService.getRecommendation(objeto, auditLogReport));
        } catch (Exception e) {
            loguearError("recomendacion", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static Futuro<RecommendationDTO> recomendacion(Contexto contexto,BankProcess objeto, AuditLogReport auditLogReport) {
        ObjectMapper m = new ObjectMapper();
        try {
            String datos = m.writeValueAsString(objeto);
            loguearInfo("recomendacion", datos);
            return new Futuro<>(() -> Servidor.recommendationService.getRecommendation(objeto, auditLogReport));
        } catch (Exception e) {
            loguearError("recomendacion", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static Futuro<RecommendationDTO> recomendacion(Contexto contexto, BankProcess objeto) {
        ObjectMapper m = new ObjectMapper();
        try {

            AuditLogReport auditLogReport = new AuditLogReport(
                    contexto.canal == null ? "" : contexto.canal,
                    contexto.subCanal() == null ? "" : contexto.subCanal(),
                    contexto.idSesion() == null ? "" : contexto.idSesion(),
                    contexto.idSesion() == null ? "" : contexto.idSesion(),
                    contexto.idSesion() == null ? "" : contexto.idSesion(),
                    contexto.config.string("backend_api_auditor", ""),
                    contexto.sesion().ip == null ? "" : contexto.sesion().ip);

            String datos = m.writeValueAsString(objeto);
            loguearInfo("recomendacion", datos);
            return new Futuro<>(() -> Servidor.recommendationService.getRecommendation(objeto, auditLogReport));
        } catch (Exception e) {
            loguearError("recomendacion", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static Futuro<Boolean> isErrorRecommendation(RecommendationDTO recommendationDTO) {
        try {
            return new Futuro<>(() -> Servidor.recommendationService.isErrorRecommendation(recommendationDTO));
        } catch (Exception e) {
            loguearError("recomendacion", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static void loguearError(String metodo, String error) {
        log.error("Error ejecutando ".concat(metodo).concat(" - ").concat(error));
    }

    protected static void loguearInfo(String metodo, String texto) {
        log.info("Ejecutando ".concat(metodo).concat(" - ").concat(texto));
    }

    public static boolean esUsuarioMigrado(Contexto contexto, String idCobis, int documento) {
        if (documento <= 0)
            return false;

        ResponseMigracionUsuario response = ApiSeguridad.gestionarMigracion(contexto, new RequestMigracionUsuario(documento, 0, OpcionMigradoEnum.CONSULTAR, idCobis, "", "")).tryGet();
        if (response == null || (response.codigoHttp() != 200 && response.codigoHttp() != 204) || (response.codigoHttp() == 200 && response.codRet != 0))
            return false;

        return EstadoMigradoEnum.codigo(response.migrado).esMigrado();
    }

    protected static boolean validarCsm(Contexto contexto, UsuarioLibreriaRequest request) {
        UsuarioLibreriaResponse response = ApiTransmit.validarCsmHB(contexto, request).tryGet();
        if (response == null || response.esError())
            return false;

        DatosAdicionalesCsm datosAdicionalesCsm = MapperUtil.mapToObject(response.addicionalData, LibreriaFraudes.DatosAdicionalesCsm.class);
        if (datosAdicionalesCsm == null || !StringUtils.isNotBlank(datosAdicionalesCsm.csmIdAuth))
            return false;

        contexto.csmIdAuth = datosAdicionalesCsm.csmIdAuth;
        return true;
    }

    public static String generarTipoClienteEncriptado(String clavePublica, boolean esClientExterior, boolean esTdv, boolean esMono) {
        try {

            String jsonInput = String.format("{\"tipo_cliente\": \"%s\"}", obtenerTipoCliente(esClientExterior, esTdv, esMono, false));
            return obtenerEncriptado(jsonInput, clavePublica);

        } catch (Exception e) {
            log.info("Error al generar el tipo de cliente encriptado: {}", e.getMessage());
            return "";
        }
    }

    public static String generarRespuestaLink(String clavePublica, boolean esValida) {
        try {
            String jsonInput = String.format("{\"response\" : \"%s\"}", esValida);
            return obtenerEncriptado(jsonInput, clavePublica);
        } catch (Exception e) {
            log.info("Error al generar el valido de clave link encriptado: {}", e.getMessage());
            return "";
        }
    }

    public static String generarDatosClienteEncriptado(String clavePublica, String documento, String telefono, String fechaNacimiento, String email, boolean esClientExterior, boolean esTdv, boolean esMono) {
        try {
            String tipoCliente = obtenerTipoCliente(esClientExterior, esTdv, esMono, true);

            String jsonInput = String.format("{\n" +
                    "\"dni\": \"%s\",\n" +
                    "\"phone\": \"%s\",\n" +
                    "\"fecha_nacimiento\": \"%s\",\n" +
                    "\"mail\": \"%s\",\n" +
                    "\"tipo_cliente\":\"%s\"\n" +
                    "}", documento, telefono, fechaNacimiento, email, tipoCliente);

            return obtenerEncriptado(jsonInput, clavePublica);

        } catch (Exception e) {
            log.info("Error al generar el tipo de cliente encriptado: {}", e.getMessage());
            return "";
        }
    }

    private static String obtenerEncriptado(String jsonInput, String clavePublica) throws JOSEException {
        RSAPublicKey publicKey = PemUtils.loadPublicKey(clavePublica);
        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)
                .contentType("text/plain")
                .build();
        Payload payload = new Payload(jsonInput);
        JWEObject jweObject = new JWEObject(header, payload);
        jweObject.encrypt(new RSAEncrypter(publicKey));
        return jweObject.serialize();
    }

    private static String obtenerTipoCliente(boolean esClientExterior, boolean esTdv, boolean esMono, boolean esNuevo) {
        StringBuilder nemonicos = new StringBuilder();
        if (esClientExterior)
            nemonicos.append(NemonicosUsuarioTrasnmitEnum.CLIENTEEXTERIOR.getDescripcion());
        else if (esMono)
            nemonicos.append(NemonicosUsuarioTrasnmitEnum.MONOPRODUCTO.getDescripcion());
        else if (esTdv)
            nemonicos.append(NemonicosUsuarioTrasnmitEnum.TARJETADEBITOVIRTUAL.getDescripcion());

        if (nemonicos.isEmpty())
            nemonicos.append(esNuevo ? NemonicosUsuarioTrasnmitEnum.NUEVO.getDescripcion() : NemonicosUsuarioTrasnmitEnum.NORMAL.getDescripcion());

        return nemonicos.toString();

    }

}
