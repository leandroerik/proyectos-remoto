package ar.com.hipotecario.backend;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.adapter.input.RecommendationService;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.config.TransmitGatewayAdapters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.CronLocal.CronJobLocal;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.buhobank.ApiBuhoBank;
import ar.com.hipotecario.canal.homebanking.ApiHomeBanking;
import ar.com.hipotecario.canal.officebanking.ApiOfficeBanking;
import ar.com.hipotecario.canal.rewards.ApiRewards;
import ar.com.hipotecario.canal.tas.ApiTAS;
import ar.com.hipotecario.mobile.ApiMobile;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Servidor {

    private static Logger log = LoggerFactory.getLogger(Servidor.class);
    public static String tiempoInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

    /* ========== ATRIBUTOS ESTATICOS ========== */
    private static Map<String, String> endpointsCanales = new HashMap<>();
    public static RecommendationService recommendationService = null;

    /* ========== MAIN ========== */
    public static void main(String[] args) throws Exception {
        CronLocal.registrar("*/10 * * * *", new GarbageCollector());
        String canal = System.getenv("canal");
        if(!StringUtils.isNotBlank(canal) && args.length > 0)
            canal = args[0];
        Spark.port(Config.puerto());
        Spark.staticFiles.location("/web");
        if (canal == null || canal.isEmpty() || canal.equals("officebanking") || canal.equals("tas")) {
            Servidor.habilitarCORS();
        } else if (canal.equals("buhobank")) {
            Servidor.habilitarCORSBuhobank();
        }

        if (canal == null || canal.isEmpty() || canal.equals("buhobank")) {
            ApiBuhoBank.iniciar();
            try {
                Transmit.iniciarTransmit(new Contexto(), "buhobank");
            } catch (Exception e) {
            }
        }
        if (canal == null || canal.isEmpty() || canal.equals("officebanking")) {
            ApiOfficeBanking.iniciar();
        }
        if (canal == null || canal.isEmpty() || canal.equals("homebanking")) {
            ApiHomeBanking.iniciar();
            try {
                Transmit.iniciarTransmit(new Contexto(), "homebanking");
            } catch (Exception e) {
            }
        }
        if (canal == null || canal.isEmpty() || canal.equals("mobile")) {
            ApiMobile.iniciar();
            try {
                Transmit.iniciarTransmit(new Contexto(), "mobile");
            } catch (Exception e) {
                log.info("Error al iniciar Transmit ".concat(e.getMessage()));
            }
        }
        if (canal == null || canal.isEmpty() || canal.equals("tas")) {
            ApiTAS.iniciar();
            ApiRewards.iniciar();
        }

        Spark.init();
        Spark.awaitInitialization();
        log.info("");

        try {
            if (canal == null || canal.equals("") || canal.equals("buhobank"))
                Transmit.iniciarTransmit(new Contexto(), "buhobank");
        } catch (Exception e) {
        }
        try {
            if (canal == null || canal.equals("") || canal.equals("officebanking"))
                Transmit.iniciarTransmit(new Contexto(), "OB");
        } catch (Exception e) {
        }

//        try {
//            if (canal == null || canal.equals("") || canal.equals("homebanking"))
//                Transmit.iniciarTransmit(new ContextoHB("HB", Config.ambiente(), "1"), "homebanking");
//        } catch (Exception e) {
//        }
//
//        try {
//            if (canal == null || canal.equals("") || canal.equals("mobile"))
//                Transmit.iniciarTransmit(new ContextoMB("MB", Config.ambiente(), "1"), "mobile");
//        } catch (Exception e) {
//        }

        try {
            recommendationService = TransmitGatewayAdapters.getRecommendationService();
        } catch (Exception e) {
            log.info("Error al iniciar recommendationService ".concat(e.getMessage()));
        }
    }

    /* ========== METODOS PRIVADOS ========== */
    private static <T extends Contexto> T contexto(Request request, Response response, Class<T> clase) {
        try {
            String canal = endpointsCanales.get(request.requestMethod().toLowerCase() + ":" + request.matchedPath());
            String ambiente = request.headers("ambiente");
            Constructor<T> constructor = clase.getDeclaredConstructor(Request.class, Response.class, String.class, String.class);
            T contexto = constructor.newInstance(request, response, canal, ambiente);
            return contexto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Contexto> Route route(Function<T, Object> funcion, Class<T> clase) {
        Route route = new Route() {
            public Object handle(Request request, Response response) throws Exception {
                if ("prueba".equals(System.getenv("variable"))) {
                    log.info(request.uri());
                }
                T contexto = contexto(request, response, clase);
                Object object = funcion.apply(contexto);
                if (object instanceof ar.com.hipotecario.canal.homebanking.base.Objeto) {
                    ar.com.hipotecario.canal.homebanking.base.Objeto hb = (ar.com.hipotecario.canal.homebanking.base.Objeto) object;
                    if (hb.esLista()) {
                        object = Objeto.fromList(hb.lista);
                    } else {
                        object = Objeto.fromRawMap(hb.mapa);
                    }
                }
                if (object instanceof Objeto) {
                    response.type("application/json; charset=utf-8");
                    return object;
                }
                if (object instanceof Archivo) {
                    Archivo archivo = (Archivo) object;
                    response.type(archivo.mime() + "; name=" + archivo.nombre);
                    return archivo.bytes;
                }
                if (object instanceof ServerSentEvents<?>) {
                    response.type("text/event-stream");
                    @SuppressWarnings("unchecked")
                    ServerSentEvents<T> sse = (ServerSentEvents<T>) object;
                    try {
                        sse.ejecutar();
                    } catch (ApiException e) {
                        return contexto.response(500, new Objeto().set("ERROR", e.codigoError));
                    }
                    return "";
                }
                if (object instanceof Redireccion) {
                    String url = ((Redireccion) object).url;
                    response.redirect(url);
                    return "";
                }
                return object;
            }
        };
        return route;
    }

    /* ========== CORS ========== */
    private static void habilitarCORS() {
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            response.header("Access-Control-Expose-Headers", "uuid");
            return "OK";
        });

        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Allow-Origin", request.headers("Origin"));
            response.header("Access-Control-Expose-Headers", "uuid");
        });
    }

    private static void habilitarCORSBuhobank() {
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        Spark.before((request, response) -> {
            String origin = request.headers("Origin");
            // TODO poner las urls de marketing en todos los ambientes o leerlo desde VE
            if (origin != null && (origin.contains("buhobank") || origin.contains("bh.com.ar") || origin.contains("backoffice") || origin.contains("hipotecario"))) {
                response.header("Access-Control-Allow-Credentials", "true");
                response.header("Access-Control-Allow-Origin", origin);
            } else {
                response.header("Access-Control-Allow-Origin", "null");
            }
        });
    }

    /* ========== HTTP ========== */
    protected static <T extends Contexto> void get(String canal, String url, Function<T, Object> funcion, Class<T> clase) {
        if (endpointsCanales.put("get:" + url, canal) != null) {
            throw new RuntimeException("Ya existe el endpoint get:" + url);
        }
        Spark.get(url, route(funcion, clase));
    }

    protected static <T extends Contexto> void post(String canal, String url, Function<T, Object> funcion, Class<T> clase) {
        if (endpointsCanales.put("post:" + url, canal) != null) {
            throw new RuntimeException("Ya existe el endpoint post:" + url);
        }
        Spark.post(url, route(funcion, clase));
    }

    protected static <T extends Contexto> void put(String canal, String url, Function<T, Object> funcion, Class<T> clase) {
        if (endpointsCanales.put("put" + url, canal) != null) {
            throw new RuntimeException("Ya existe el endpoint put:" + url);
        }
        Spark.put(url, route(funcion, clase));
    }

    protected static <T extends Contexto> void patch(String canal, String url, Function<T, Object> funcion, Class<T> clase) {
        if (endpointsCanales.put("patch:" + url, canal) != null) {
            throw new RuntimeException("Ya existe el endpoint patch:" + url);
        }
        Spark.patch(url, route(funcion, clase));
    }

    protected static <T extends Contexto> void delete(String canal, String url, Function<T, Object> funcion, Class<T> clase) {
        if (endpointsCanales.put("delete:" + url, canal) != null) {
            throw new RuntimeException("Ya existe el endpoint delete:" + url);
        }
        Spark.delete(url, route(funcion, clase));
    }

    /* ========== CRON ========== */
    public static void cron(String cron, CronJob cronJob) {
        Cron.registrar(cron, cronJob);
    }

    public static void cron(String cron, CronJob cronJob, Boolean prendido) {
        if (prendido) {
            Cron.registrar(cron, cronJob);
        }
    }

    /* ========== LOG ========== */
    protected static class Log {
        private static Gson gson = new Gson();

        public String canal;
        public String cuit;
        public String idCobis;
        public String tipo;
        public String stackTrace;

        public String toString() {
            return gson.toJson(this);
        }
    }

    protected static void log(Contexto contexto, Exception e) {
        if (contexto.config.bool("kibana")) {
            Log log = new Log();
            log.canal = contexto.canal();
            log.cuit = contexto.sesion() != null ? contexto.sesion().cuil : "";
            log.idCobis = contexto.sesion() != null ? contexto.sesion().idCobis : "";
            log.tipo = "error";
            log.stackTrace = Texto.stackTrace(e);
            Servidor.log.error(log.toString());
        } else {
            Servidor.log.error(Fecha.ahora().string("[HH:mm:ss] ") + Texto.stackTrace(e));
        }
    }

    /* ========== CRON ========== */
    public static class GarbageCollector extends CronJobLocal {
        public void run() {
            System.gc();
        }
    }
}
