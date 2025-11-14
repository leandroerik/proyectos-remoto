package ar.com.hipotecario.backend;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Redis;
import ar.com.hipotecario.backend.base.Util;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

public class Contexto {

    /* ========== ATRIBUTOS ESTATICOS ========== */
    private static Map<String, DataSource> dataSourcesMap = new HashMap<>();
    private static Map<String, Redis> redisMap = new HashMap<>();
    private static Map<String, Object> redisMock = new ConcurrentHashMap<>();
    private static Gson gson = Util.gson(false);

    /* ========== ATRIBUTOS ========== */
    public Request request;
    public Response response;
    public Parametros parametros;
    public Config config;
    public String canal;
    public Map<String, String> headers = new HashMap<>();
    public Long momentoCreacion = new Date().getTime();
    private Sesion sesion;
    public String ambiente;
    public String csmIdAuth;
    public Boolean usuarioMigrado;
    public Boolean usuarioMigrarBiometria;
    public Boolean usuarioAmigrar;

    /* ========== LOG ========== */
    public String hiloPrincipal;
    public Map<String, String> mapaInvocaciones = new ConcurrentSkipListMap<>();

    /* ========== CONSTRUCTORES ========== */
    public Contexto() {
        this.hiloPrincipal = Thread.currentThread().getName();
        this.request = null;
        this.response = null;
        this.parametros = new Parametros();
        this.config = new Config();
        Sesion sesion = sesion();
        sesion.save();
        this.sesion = sesion;
    }

    /* ========== CONSTRUCTOR PARA TEST ========== */
    public Contexto(String idCobis) {
        this.hiloPrincipal = Thread.currentThread().getName();
        this.request = null;
        this.response = null;
        this.parametros = new Parametros();
        this.config = new Config();
        Sesion sesion = sesion(Sesion.class);
        sesion.save();
        this.sesion = sesion;
    }

    public Contexto(String canal, String ambiente, String idCobis) {
        this.hiloPrincipal = Thread.currentThread().getName();
        this.request = null;
        this.response = null;
        this.parametros = new Parametros();
        this.config = new Config(ambiente);
        this.canal = canal;
        Sesion sesion = sesion();
        sesion.idCobis = idCobis;
        sesion.save();
        this.sesion = sesion;
    }

    public Contexto(Request request, Response response, String canal, String ambiente) {
        this.hiloPrincipal = Thread.currentThread().getName();
        this.request = request;
        this.response = response;
        this.parametros = parametros(request);
        this.config = new Config(ambiente);
        this.ambiente = ambiente;
        this.canal = canal;
    }

    /* ========== INICIALIZAR ========== */
    private Parametros parametros(Request request) {
        String contentType = request.contentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart")) {
            return parametrosMultipart(request);
        }
        return parametrosJson(request);
    }

    private Parametros parametrosMultipart(Request request) {
        Parametros parametros = new Parametros();
        try {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
            for (Part part : request.raw().getParts()) {
                ByteArrayOutputStream valor = new ByteArrayOutputStream();
                try (InputStream input = part.getInputStream()) {
                    byte[] buf = new byte[1024];
                    for (int n = input.read(buf); n != -1; n = input.read(buf)) {
                        valor.write(buf, 0, n);
                    }
                }
                String clave = part.getName();
                String nombre = part.getSubmittedFileName();
                String contentType = part.getContentType();
                if (nombre != null && !"application/json".equalsIgnoreCase(contentType)) {
                    Archivo archivo = new Archivo(nombre, valor.toByteArray());
                    parametros.setArchivo(clave, archivo);
                } else if ("application/json".equalsIgnoreCase(contentType)) {
                    parametros.loadJson(valor.toString());
                } else {
                    parametros.set(clave, valor.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return parametros;
    }

    private Parametros parametrosJson(Request request) {
        String parametrosBody = request.body();
        Parametros parametros = Parametros.fromJson(parametrosBody);

        Map<String, String> parametrosPath = request.params();
        for (String parametro : parametrosPath.keySet()) {
            String valor = parametrosPath.get(parametro);
            parametros.set(parametro, valor);
        }

        Map<String, String[]> parametrosQuery = request.queryMap().toMap();
        for (String parametro : parametrosQuery.keySet()) {
            for (String valor : parametrosQuery.get(parametro)) {
                parametros.set(parametro, valor);
            }
        }

        return parametros;
    }

    /* ========== HEADER ========== */
    public String requestHeader(String clave) {
        String header = null;
        if (clave != null) {
            header = headers.get(clave);
            header = (header == null) ? headers.get(clave.toLowerCase()) : header;
            header = (header == null && request != null) ? request.headers(clave) : header;
            header = (header == null && request != null) ? request.headers(clave.toLowerCase()) : header;
        }
        header = (header != null) ? header : "";
        return header;
    }

    public void responseHeader(String clave, String valor) {
        response.header(clave, valor);
    }

    public void requestHeader(String clave, String valor) {
        headers.put(clave, valor);
    }

    /* ========== MODO ========== */
    public Boolean modoOffline() {
        String parametro = "offline";
        if (!esProduccion()) {
            Boolean modo = false;
            modo |= requestHeader("offline").toLowerCase().contains(simplePath().toLowerCase());
            modo |= requestHeader("modo").toLowerCase().contains(parametro.toLowerCase());
            modo |= requestHeader(simplePath()).toLowerCase().contains(parametro.toLowerCase());
            return modo;
        }
        return false;
    }

    public Boolean modo(String parametro) {
        if (!esProduccion()) {
            Boolean modo = false;
            modo |= requestHeader("modo").toLowerCase().contains(parametro.toLowerCase());
            modo |= requestHeader(simplePath()).toLowerCase().contains(parametro.toLowerCase());
            return modo;
        }
        return false;
    }

    /* ========== REDIS ========== */
    public Boolean usarRedis() {
        return config.bool("backend_redis", false);
    }

    public Redis redis() {
        Boolean usarRedis = config.bool("backend_redis", false);
        if (usarRedis) {
            String servidor = config.string("backend_redis_servidor");
            Integer puerto = config.integer("backend_redis_puerto");
            String usuario = config.string("backend_redis_usuario");
            String clave = config.string("backend_redis_clave");

            String key = servidor + puerto + usuario + clave;
            if (!redisMap.containsKey(key)) {
                Redis redis = new Redis(servidor, puerto, usuario, clave);
                redisMap.put(key, redis);
            }
            return redisMap.get(key);
        }
        return null;
    }

    /* ========== SESION ========== */
    public String get(String clave) {
        Redis redis = redis();
        if (redis != null) {
            return redis.get(clave);
        } else {
            return (String) redisMock.get(clave);
        }
    }

    public void set(String clave, String valor) {
        set(clave, valor, 30 * 60);
    }

    public void set(String clave, String valor, Integer expiracion) {
        Redis redis = redis();
        if (redis != null) {
            redis.set(clave, valor, expiracion);
        } else {
            redisMock.put(clave, valor);
        }
    }

    public void del(String clave) {
        Redis redis = redis();
        if (redis != null) {
            redis.del(clave);
        } else {
            redisMock.remove(clave);
        }
    }

    public byte[] getBinary(String clave) {
        Redis redis = redis();
        if (redis != null) {
            return redis.getBinary(clave);
        } else {
            return (byte[]) redisMock.get(clave);
        }
    }

    public void setBinary(String clave, byte[] valor) {
        setBinary(clave, valor, 30 * 60);
    }

    public void setBinary(String clave, byte[] valor, Integer expiracion) {
        Redis redis = redis();
        if (redis != null) {
            redis.setBinary(clave, valor, expiracion);
        } else {
            redisMock.put(clave, valor);
        }
    }

    public void delBinary(String clave) {
        Redis redis = redis();
        if (redis != null) {
            redis.delBinary(clave);
        } else {
            redisMock.remove(clave);
        }
    }

    public byte[] getBytes(String clave) {
        return getBinary(idSesion() + "." + clave);
    }

    public void setBytes(String clave, byte[] valor) {
        setBinary(idSesion() + "." + clave, valor);
    }

    public void delBytes(String clave) {
        delBinary(idSesion() + "." + clave);
    }

    public String idSesion() {
        if (request != null) {
            String uuid = request.headers("uuid");
            return (uuid != null && !uuid.isEmpty()) ? uuid : request.session().id();
        }
        return "1";
    }

    public Sesion sesion() {
        if (sesion == null) {
            sesion = sesion(Sesion.class);
        }
        return sesion;
    }

    public <T extends Sesion> T sesion(Class<T> tipo) {
        try {
            String clave = idSesion() + "_" + tipo.getName();
            String json = get(clave);
            T valor = json != null && !json.isEmpty() ? gson.fromJson(json, tipo) : null;
            valor = valor != null ? valor : tipo.getDeclaredConstructor().newInstance();
            ((Sesion) valor).contexto = this;
            return valor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveSesion(Sesion valor) {
        String clave = idSesion() + "_" + valor.getClass().getName();
        String json = gson.toJson(valor);
        set(clave, json);
    }

    public void deleteSesion(Sesion valor) {
        String clave = idSesion() + "_" + valor.getClass().getName();
        del(clave);
    }

    public void deleteSesion() {
        if (request != null) {
            sesion().delete();
            request.session().invalidate();
        }
    }

    /* ========== SQL ========== */
    public DataSource dataSource(String baseDatos) {

        Boolean es_hb_be_homo = (esHomologacion() || esDesarrollo()) && "hb_be".equals(baseDatos) && esOpenShift();
        String urlSql = "backend_sql_" + baseDatos + "_url";
        if (!this.config.string(urlSql).isEmpty()) {
            if (!es_hb_be_homo) {
                return dataSourceCamposSeparados(baseDatos);
            }
        }

        String clave = "backend_sql_" + baseDatos;
        if (!dataSourcesMap.containsKey(clave)) {
            String valor = this.config.string(clave);

            String url = valor.split(" ")[0];
            String user = valor.split(" ")[1];
            String password = valor.split(" ")[2];

            HikariConfig config = new HikariConfig();
            if (url.startsWith("jdbc:sqlserver")) {
                config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }
            if (url.startsWith("jdbc:teradata")) {
                config.setDriverClassName("com.teradata.jdbc.TeraDriver");
            }
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            DataSource dataSource = new HikariDataSource(config);
            dataSourcesMap.put(clave, dataSource);
            return dataSource;
        }
        return dataSourcesMap.get(clave);
    }

    public DataSource dataSourceCamposSeparados(String baseDatos) {
        String clave = "backend_sql_" + baseDatos + "_url";
        if (!dataSourcesMap.containsKey(clave)) {
            String url = this.config.string("backend_sql_" + baseDatos + "_url");
            String user = this.config.string("backend_sql_" + baseDatos + "_usuario");
            String password = this.config.string("backend_sql_" + baseDatos + "_clave");

            HikariConfig config = new HikariConfig();
            if (url.startsWith("jdbc:sqlserver")) {
                config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }
            if (url.startsWith("jdbc:teradata")) {
                config.setDriverClassName("com.teradata.jdbc.TeraDriver");
            }
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            DataSource dataSource = new HikariDataSource(config);
            dataSourcesMap.put(clave, dataSource);
            return dataSource;
        }
        return dataSourcesMap.get(clave);
    }

    /* ========== URL ========== */
    public String url() {
        return request != null ? request.servletPath() : "";
    }

    public String path() {
        return request != null && request.matchedPath() != null ? request.matchedPath() : "";
    }

    public String simplePath() {
        String simplePath = path();
        Integer lastIndexOf = simplePath.lastIndexOf('/');
        if (lastIndexOf > 0) {
            try {
                simplePath = simplePath.substring(lastIndexOf + 1);
                if (simplePath.startsWith(":")) {
                    simplePath = simplePath.substring(1);
                }
            } catch (Exception e) {
                return "";
            }
        }
        return simplePath;
    }

    public String basePath() {
        return request.raw().getRequestURL().toString().replace(request.raw().getRequestURI(), "");
    }

    /* ========== AMBIENTE ========== */
    public Boolean esOpenShift() {
        return Config.esOpenShift();
    }

    public Boolean esDesarrollo() {
        return config.esDesarrollo();
    }

    public Boolean esIntegracion() {
        return config.esIntegracion();
    }

    public Boolean esHomologacion() {
        return config.esHomologacion();
    }

    public Boolean esProduccion() {
        return config.esProduccion();
    }

    /* ========== CANALES ========== */
    protected Boolean esAdminCanales() {
        return path().contains("/admin/api/") || "AC".equals(canal);
    }

    protected Boolean esBuhoBank() {
        return path().contains("/bb/api/") || "BB".equals(canal);
    }

    protected Boolean esHomeBanking() {
        return path().contains("/hb/api/") || "HB".equals(canal);
    }

    protected Boolean esOfficeBanking() {
        return path().contains("/ob/api/") || path().contains("/oba/api/") || "OB".equals(canal);
    }

    protected Boolean esTas() {
        return path().contains("/tas/api/") || "TAS".equals(canal);
    }

    protected Boolean esRewards() {
        return path().contains("/rewards/api/") || "RW".equals(canal);
    }

    /* ========== CANAL ========== */
    public String nombreCanal() {
        String nombreCanal = "";
        nombreCanal = esAdminCanales() ? "admin" : nombreCanal;
        nombreCanal = esBuhoBank() ? "buhobank" : nombreCanal;
        nombreCanal = esHomeBanking() ? "homebanking" : nombreCanal;
        nombreCanal = esOfficeBanking() ? "officebanking" : nombreCanal;
        nombreCanal = esTas() ? "tas" : nombreCanal;
        nombreCanal = esRewards() ? "rw" : nombreCanal;
        return nombreCanal;
    }

    public String canal() {
        if(esAdminCanales()){
            return "AC";
        }
        else if(esBuhoBank()){
            return "BB";
        }
        else if(esHomeBanking()){
            return "HB";
        }
        else if(esOfficeBanking()){
            return "OB";
        }
        else if(esTas()){
            return "TAS";
        }
        else if(esRewards()){
            return "RW";
        }
        else if(esMobile()){
            return "MB";
        }
        return "";
    }

    public String subCanal() {
        if (esRewards()) {
            String subcanal = "REWARDS_FE";
            return subcanal;
        }

        String userAgent = request != null && request.userAgent() != null ? request.userAgent().toLowerCase() : "";
        Boolean esMobile = Arrays.stream("android|blackberry|iphone|ipad|ipod".split("\\|"))
                .anyMatch(x -> userAgent.contains(x));
        String subcanal = "WEB";
        subcanal = esMobile ? "MOBILE" : subcanal;
        subcanal = request == null ? "TEST" : subcanal;
        return subcanal;
    }

    public Boolean esMobile() {
        String userAgent = request != null && request.userAgent() != null ? request.userAgent().toLowerCase() : "";
        Boolean esMobile = Arrays.stream("android|blackberry|iphone|ipad|ipod".split("\\|")).anyMatch(x -> userAgent.contains(x));
        return esMobile;
    }

    public Boolean esAndroid() {
        String userAgent = request != null && request.userAgent() != null ? request.userAgent().toLowerCase() : "";
        Boolean esMobile = Arrays.stream("android".split("\\|")).anyMatch(x -> userAgent.contains(x));
        return esMobile;
    }

    public Boolean esIOS() {
        String userAgent = request != null && request.userAgent() != null ? request.userAgent().toLowerCase() : "";
        Boolean esMobile = Arrays.stream("iphone|ipad|ipod".split("\\|")).anyMatch(x -> userAgent.contains(x));
        return esMobile;
    }

    public String canalVenta() {
        return config.string(nombreCanal() + "_canalventa");
    }

    public String subCanalVenta() {
        return config.string(nombreCanal() + "_subcanalventa");
    }

    public String usuarioCanal() {
        return config.string(nombreCanal() + "_usuario");
    }

    /* ========== USUARIO ========== */
    public String ip() {
        try {
            String ip = request != null ? request.ip() : InetAddress.getLocalHost().getHostAddress();
            ip = "0:0:0:0:0:0:0:1".equals(ip) ? InetAddress.getLocalHost().getHostAddress() : ip;
            String xForwardedFor = request != null ? request.headers("X-FORWARDED-FOR") : null;
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /* ========== RESPONSE ========== */
    public Objeto response(Integer httpCode, Objeto objeto) {
        response.status(httpCode);
        response.type("application/json; charset=utf-8");
        return objeto;
    }

    public Object response(Integer httpCode, String objeto) {
        response.status(httpCode);
        response.type("application/json; charset=utf-8");
        return objeto;
    }

    public String response(Integer httpCode) {
        response.status(httpCode);
        return "";
    }

    public String idSesionTransmit() {
        return "";
    }

    public void limpiarCsmIdAuth() {
        this.csmIdAuth = "";
    }

    public boolean tieneCsmIdAuth() {
        return StringUtils.isNotBlank(this.csmIdAuth);
    }
}


