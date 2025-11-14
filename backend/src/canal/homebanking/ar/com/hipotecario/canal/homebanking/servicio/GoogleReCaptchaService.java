package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.negocio.recaptcha.RecaptchaErrorResponse;
import ar.com.hipotecario.canal.homebanking.negocio.recaptcha.RecaptchaEventRequest;
import ar.com.hipotecario.canal.homebanking.negocio.recaptcha.RecaptchaRequest;
import ar.com.hipotecario.canal.homebanking.negocio.recaptcha.RecaptchaResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleReCaptchaService {

    private static final String PROJECT_ID = "{PROJECT_ID}";
    private static final String API_KEY = "{API_KEY}";
    private static final String HTTP_METHOD = "POST";

    private static final String VE_RECAPTCHA_KEY_PRIVATE = "hb_recaptcha_key_public";
    private static final String VE_RECAPTCHA_GOOGLE_URL = "recaptcha_google_url";
    private static final String VE_RECAPTCHA_SCORE = "recaptcha_score";
    private static final String VE_RECAPTCHA_PROJECT_ID = "recaptcha_project_id";
    private static final String VE_RECAPTCHA_KEY_ID = "recaptcha_key_id";

    private static final String ACTION = "LOGIN";

    private static final String ERROR_RECAPTCHA = "ERROR_RECAPTCHA";

    private static final String SP_EXEC = "EXEC [homebanking].[dbo].[insertarErrorRecaptcha] @documento = ?, @descripcion = ?, @action = ?, @score = ?";
    private static final String INSERT_NAME = "InsertErrorRecaptcha";

    private static final String SP_EXEC_WHITE_LIST = "EXEC [homebanking].[dbo].[insertarRegistroListaBlancaRecaptcha] @dni = ?, @ip = ?";
    private static final String INSERT_WHITE_LIST = "InsertListaBlancaRecaptcha";
    private static final String DB_HOMEBANKING = "homebanking";

    private static final String ERROR_GENERICO = "No se pudo obtener una validación reCaptcha. ";
    private static final String ERROR_FALLO_EVALUACION = "Falló la evaluación ";
    private static final String ERROR_ACTION = "El Action no es el correspondiente ";
    private static final String ERROR_SCORE = "El Score no superó el mínimo ";

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static Respuesta verificarRecaptchaLogin(String documento, String token, String userAgent, String ip) {
        return verificiarReCaptcha(documento, token, ACTION, userAgent, ip);
    }

    public static void registrarIpListaBlancaRecaptcha(String documento, String ip) {
        registrarIpListaBlanca(documento, ip);
    }

    private static Respuesta verificiarReCaptcha(String documento, String token, String action, String userAgent, String ip) {
        try {
            HttpRequest request = new HttpRequest(HTTP_METHOD,
                    ConfigHB.string(VE_RECAPTCHA_GOOGLE_URL)
                            .replace(PROJECT_ID, ConfigHB.string(VE_RECAPTCHA_PROJECT_ID))
                            .replace(API_KEY, ConfigHB.string(VE_RECAPTCHA_KEY_ID))
            );

            request.body(crearBody(token, userAgent, ip, action));

            HttpResponse response = request.run();
            if (response.code == 200 && isValidReCatpcha(castearBody(response.jsonBody().toJson()), documento))
                return Respuesta.exito();

            loguearError(documento, ERROR_GENERICO.concat(casterError(response.jsonBody())), ACTION, null);
            return Respuesta.estado(ERROR_RECAPTCHA);
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    private static RecaptchaResponse castearBody(String jsonBody) {
        try {
            return objectMapper.readValue(jsonBody, RecaptchaResponse.class);
        } catch (Exception e) {
            return new RecaptchaResponse();
        }
    }

    private static String casterError(Objeto jsonBody) {
        try {
            return objectMapper.readValue(jsonBody.get("error").toString(), RecaptchaErrorResponse.class).getMessage();
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private static Objeto crearBody(String token, String userAgent, String ip, String expectedAction) {
        return convertirRequest(new RecaptchaEventRequest(new RecaptchaRequest(token, ConfigHB.string(VE_RECAPTCHA_KEY_PRIVATE), userAgent, ip, expectedAction)));
    }

    private static Objeto convertirRequest(RecaptchaEventRequest request) {
        Objeto objetoRetorno = new Objeto();
        if (request != null) {
            Objeto event = new Objeto();

            event.set("token", request.getEvent().getToken());
            event.set("siteKey", request.getEvent().getSiteKey());
            event.set("userAgent", request.getEvent().getUserAgent());
            event.set("userIpAddress", request.getEvent().getUserIpAddress());
            event.set("expectedAction", request.getEvent().getExpectedAction());

            objetoRetorno.set("event", event);
        }
        return objetoRetorno;
    }

    private static boolean isValidReCatpcha(RecaptchaResponse response, String documento) {
        if (!response.getTokenProperties().isValid()) {
            loguearError(documento, ERROR_FALLO_EVALUACION.concat(response.getTokenProperties().getInvalidReason()), ACTION, null);
            return false;
        }

        if (!response.getTokenProperties().getAction().equals(ACTION)) {
            loguearError(documento, ERROR_ACTION.concat(response.getTokenProperties().getInvalidReason()), ACTION, null);
            return false;
        }

        if (!compareScore(response.getRiskAnalysis().getScore())) {
            loguearError(documento, ERROR_SCORE.concat(response.getTokenProperties().getInvalidReason()), ACTION, response.getRiskAnalysis().getScore());
            return false;
        }

        return true;
    }

    private static void insertarLog(String documento, String error, String action, String score) {
        try {
            SqlRequest sqlRequest = Sql.request(INSERT_NAME, DB_HOMEBANKING);

            sqlRequest.sql = SP_EXEC;
            sqlRequest.parametros.add(documento);
            sqlRequest.parametros.add(error);
            sqlRequest.parametros.add(action);
            sqlRequest.parametros.add(score);

            new Futuro<>(() -> Sql.response(sqlRequest));

        } catch (Exception e) {
        }
    }

    private static void registrarIpListaBlanca(String documento, String ip) {
        try {
            SqlRequest sqlRequest = Sql.request(INSERT_WHITE_LIST, DB_HOMEBANKING);

            sqlRequest.sql = SP_EXEC_WHITE_LIST;
            sqlRequest.parametros.add(documento);
            sqlRequest.parametros.add(ip);

            Sql.response(sqlRequest);

        } catch (Exception e) {
        }
    }

    private static void loguearError(String documento, String mensaje, String action, Float score) {
        insertarLog(documento, mensaje, action, score != null ? String.valueOf(score) : "");
    }

    private static boolean compareScore(float score) {
        return score >= ConfigHB.bigDecimal(VE_RECAPTCHA_SCORE).floatValue();
    }

}
