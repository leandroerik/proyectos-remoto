package ar.com.hipotecario.canal.homebanking;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.productos.*;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.canal.homebanking.api.*;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.DataFile;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Redis;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.*;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import spark.Request;
import spark.Response;

public class ContextoHB extends Contexto {

    /* ========== ATRIBUTOS ========== */
    public SesionHB sesion;
    public Objeto parametros;
    public String ip;
    public Map<String, DataFile> archivos;
    public Map<String, String> cacheRequest = new ConcurrentHashMap<>();
    public static Respuesta consolidada = new Respuesta();

    /* ========== CONSTRUCTORES ========== */
    public ContextoHB(Request request, Response response, String canal, String ambiente) {
        super(request, response, canal, ambiente);

//		this.request = request.raw();
//		this.response = response.raw();
        this.parametros = Objeto.fromJson(super.parametros.toJson());
        consolidada = new Respuesta();

        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionHB.class);
        } else {
            this.sesion = (SesionHB) request.raw().getSession().getAttribute("sesion");
        }

        if (sesion == null) {
            crearSesion();
        }

        Map<String, Archivo> archivos = super.parametros.archivos();
        for (String clave : archivos.keySet()) {
            Archivo archivo = archivos.get(clave);
            this.parametros.set(clave, archivo.nombre);
            this.archivos.put(clave, new DataFile(clave, archivo.bytes));
        }
    }

    /* ========== CONSTRUCTOR PARA TEST ========== */
    public ContextoHB(String idCobis, String idSesion, String ip) {
        this.sesion = new SesionHB(idSesion);
        this.parametros = new Objeto();
        this.sesion.usuarioLogueado = true;
        this.ip = ip;
        this.sesion.idCobis = idCobis;
        this.sesion.save();
    }

    /* ========== CONSTRUCTOR ========== */
    public ContextoHB() {
    }

    /* ========== SESION ========== */
    public void crearSesion() {
        sesion = new SesionHB(idSesion());
        if (Redis.habilitado) {
            Redis.set(idSesion(), sesion);
        } else {
            if (request.raw() != null) {
                request.raw().getSession().setAttribute("sesion", sesion);
            }
        }
    }

    public void eliminarSesion(Boolean eliminarBaseDatos, String idCobis) {
        this.sesion.idCobis = null;
        this.sesion.idCobisReal = null;
        this.sesion.usuarioLogueado = null;
        this.sesion.ip = null;
        this.sesion.canal = null;
        this.sesion.save();
        consolidada = new Respuesta();

        Redis.del(idSesion());
        if (ConfigHB.bool("validar_sesion_duplicada", false) && eliminarBaseDatos)
            SqlHomebanking.eliminarSesion(idCobis);

        if (request.raw() != null)
            request.raw().getSession().invalidate();

        crearSesion();
    }

    /* ========== UTIL ========== */
    public Boolean esHiloPrincipal() {
        ContextoHB contexto = CanalHomeBanking.threadLocal.get();
        return contexto != null;
    }

    public String idCobis() {
        return sesion.idCobis;
    }

    public String idSesion() {
        if (request.raw() == null) {
            return UUID.randomUUID().toString();
        } else {
            try {
                return request.raw().getSession().getId();
            } catch (Exception e) {
                return sesion.idSesion;
            }
        }
    }

    public String hashSesion() {
        String idSesion = idSesion();
        String idCobis = idCobis();
        String datos = idSesion;
        if (idCobis != null) {
            datos += idCobis;
        }
        return Encriptador.md5(datos);
    }

    public SesionHB sesion() {
        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionHB.class);
        } else if (request.raw() != null) {
            this.sesion = (SesionHB) request.raw().getSession().getAttribute("sesion");
        }
        return this.sesion;
    }

    // a mejorar solo clona los parametros, falta el resto de los objetos
//	public ContextoHB clon() {
//		ContextoHB contexto = new ContextoHB();
//		contexto.parametros = Objeto.fromJson(parametros.toJson());
//		return contexto;
//	}

    /* ========== PERSONA ========== */
    public Persona persona() {
        ApiResponse response = RestPersona.clientes(this);
        if (!response.hayError()) {
            Persona persona = new Persona(this, response.objetos().get(0));
            this.sesion.esEmpleado = persona.esEmpleado();
            return persona;
        }
        return null;
    }

    public Boolean esMonoProductoTC() {
        Boolean esMonoProductoTC = true;
        esMonoProductoTC &= cuentas().isEmpty();
        esMonoProductoTC &= tarjetasDebito().isEmpty();
        return esMonoProductoTC;
    }

    public Boolean tieneTD() {
        return !tarjetasDebito().isEmpty();
    }

    public Boolean poseeCuentasUnipersonales() {
        Boolean unipersonal = false;
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.unipersonal()) {
                unipersonal = true;
            }
        }
        return unipersonal;
    }

    public Boolean depositaSueldo() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("K", "EV", "M", "L").contains(cuenta.categoria())) {
                if (cuenta.esPesos()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean esPlanSueldoExcluyente() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("K", "EV").contains(cuenta.categoria())) {
                return true;
            }
        }
        return false;
    }

    public Boolean esPlanSueldo() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("K", "EV", "M").contains(cuenta.categoria())) {
                return true;
            }
        }
        return false;
    }

    public Boolean esProcrear(ContextoHB contexto) {
        ApiResponse response = HBProcrearRefaccion.ofertasProcrear(contexto);
        for (Objeto item : response.objetos()) {
            if (item.string("estado").equals("SO")) {
                return true;
            }
        }
        return false;
    }

    public Boolean esTasaCero(ContextoHB contexto) {
        ApiRequest request = Api.request("BeneficiarioPrestamoTasaCero", "prestamos", "GET",
                "/v1/prestamos/creditos/{cuil}", contexto);
        request.path("cuil", contexto.persona().cuit());
        request.cacheSesion = true;
        request.permitirSinLogin = true;

        ApiResponse response = Api.response(request, contexto.idCobis());
        if (!response.hayError()) {
            Date fechaTope = null;
            boolean tieneTasaCero = false;
            try {
                fechaTope = new SimpleDateFormat("yyyyMMdd").parse(ConfigHB.string("tasa_cero_fecha_tope", "20220826"));
                tieneTasaCero = !ConfigHB.esProduccion()
                        || response.date("fechaSolicitud", "yyyy-MM-dd'T'hh:mm:ss").after(fechaTope);
            } catch (Exception e) {
            }
            return tieneTasaCero;
        }
        return false;
    }

    /* ========== RIESGONET ========== */
    public Boolean esProspecto() {
        ApiResponse response = ProductosService.productos(this, true);
        return response.codigo == 204 || "{}".equals(response.json);
    }

    public Boolean validaPorRiesgoNet(ContextoHB contexto) {
        ApiResponse response = HBProcrearRefaccion.ofertasProcrear(contexto);
        Boolean validaPorRiesgoNet = false;

        for (Objeto item : response.objetos()) {
            if (item.string("estado").equals("SO")) {
                validaPorRiesgoNet = esProspecto();
            }
        }

        if (validaPorRiesgoNet) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertContador", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
                sqlRequest.add(contexto.idCobis());
                sqlRequest.add("ADHESION_PROCREAR");
                new Futuro<>(() -> Sql.response(sqlRequest));
            } catch (Exception e) {
            }
        }

        return validaPorRiesgoNet;
    }

    public Boolean bloqueadoRiesgoNet() {
        Integer cantidadDiasBloqueo = ConfigHB.integer("cantidad_dias_bloqueo_riesgonet", 1);
        Integer cantidadIntentosBloqueo = ConfigHB.integer("cantidad_intentos_bloqueo_riesgonet", 3);

        String inicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.sql.Date(new java.util.Date().getTime() - cantidadDiasBloqueo * 24 * 60 * 60 * 1000L));
        String fin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.sql.Date(new java.util.Date().getTime() + 60000L));

        SqlRequest sqlRequest = Sql.request("ConsultaRiesgoNet", "homebanking");
        sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[riesgo_net] WHERE idCobis = ? AND momento > ? AND momento < ?";
        sqlRequest.add(this.idCobis());
        sqlRequest.add(inicio);
        sqlRequest.add(fin);
        Integer cantidadIntentosFallidos = Sql.response(sqlRequest).registros.size();

        return cantidadIntentosFallidos >= cantidadIntentosBloqueo;
    }

    public Boolean insertarContador(String tipo) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertContador", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            Sql.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean tieneContador(String tipo) {
        try {
            SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT TOP 1 * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? order by momento desc";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return sqlResponse.registros.size() > 0;
        } catch (Exception e) {
        }

        return false;
    }

    public List<Objeto> tieneContador(String tipo, String fechaDesde) {
        try {
            SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? AND momento > ? ORDER BY momento DESC";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            sqlRequest.add(fechaDesde);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return sqlResponse.registros;
        } catch (Exception e) {
        }

        return new ArrayList<>();
    }


    public Boolean insertarLogLogin(ContextoHB contexto, String fingerprint) {

        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogLogin", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp, fingerprint) VALUES (?, ?, GETDATE(), ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(fingerprint);
                SqlResponse response = Sql.response(sqlRequest);
                if (response.hayError) {
                    try {
                        sqlRequest = Sql.request("InsertLogLogin", "homebanking");
                        sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
                        sqlRequest.add(idCobis());
                        sqlRequest.add("HB");
                        sqlRequest.add(contexto.ip());
                        Sql.response(sqlRequest);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            } catch (Exception e) {
                try {
                    SqlRequest sqlRequest = Sql.request("InsertLogLogin", "homebanking");
                    sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
                    sqlRequest.add(idCobis());
                    sqlRequest.add("HB");
                    sqlRequest.add(contexto.ip());
                    Sql.response(sqlRequest);
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Objeto> logsLogin() {
        try {
            String sql = "";
            sql += "SELECT TOP 5 canal, momento ";
            sql += "FROM [homebanking].[dbo].[logs_login] WITH (NOLOCK) ";
            sql += "WHERE idCobis = ? ";
            sql += "ORDER BY id DESC ";

            SqlRequest request = Sql.request("logsLoginCliente", "homebanking");
            request.sql = sql;
            request.add(idCobis());
            SqlResponse response = Sql.response(request);
            return response.registros;
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public Boolean insertarLogCambioClave(ContextoHB contexto) {

        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogCambioClave", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_clave] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Boolean insertarLogCambioUsuario(ContextoHB contexto) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertLogCambioUsuario", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_usuario] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("HB");
            sqlRequest.add(contexto.ip());
            Sql.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public Boolean insertarLogEnvioOtp(ContextoHB contexto, String celular, String email, Boolean riesgoNet,
                                       Boolean link, String estado) {

        if (riesgoNet == null) {
            riesgoNet = false;
        }
        if (link == null) {
            link = false;
        }

        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogEnvioOtp", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_envios_otp] (idCobis, canal, momento, direccionIp, celular, email, riesgoNet, link, estado) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(celular);
                sqlRequest.add(email);
                sqlRequest.add(riesgoNet ? 1 : 0);
                sqlRequest.add(link ? 1 : 0);
                sqlRequest.add(estado);
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Boolean insertLogPreguntasRiesgoNet(ContextoHB contexto, String nombreServicio, String idProceso) {
        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogPreguntasRiesgoNet", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_servicio] (idCobis, momento, canal, nombreServicio, idProceso) VALUES (?, GETDATE(), ?, ?, ?)";
                sqlRequest.add(contexto.idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(nombreServicio);
                sqlRequest.add(idProceso);
                Sql.response(sqlRequest);
            } catch (Exception error) {
                return false;
            }
        }

        return true;
    }

    public Boolean insertarLogPrestamos(ContextoHB contexto, BigDecimal importe, Integer plazo, String cuenta) {

        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogEnvioOtp", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_prestamos_personales] (idCobis, canal, momento, direccionIp, importe, plazo, cuenta) VALUES (?, ?, GETDATE(), ?, ?, ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(importe);
                sqlRequest.add(plazo);
                sqlRequest.add(cuenta);
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Boolean insertarLogCambioMail(ContextoHB contexto, String mailAnterior, String mailNuevo) {
        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogCambioMail", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_mail] (idCobis, canal, momento, direccionIp, mailAnterior, mailNuevo) VALUES (?, ?, GETDATE(), ?, ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(mailAnterior);
                sqlRequest.add(mailNuevo);
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Boolean insertarLogCambioCelular(ContextoHB contexto, String celularAnterior, String celularNuevo) {
        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogCambioCelular", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_celular] (idCobis, canal, momento, direccionIp, celularAnterior, celularNuevo) VALUES (?, ?, GETDATE(), ?, ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(celularAnterior);
                sqlRequest.add(celularNuevo);
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Boolean insertarLogBeneficiario(ContextoHB contexto, String cbu, String cuenta, String documento,
                                           String nombre, String accion) {
        if (true) {
            try {
                SqlRequest sqlRequest = Sql.request("InsertLogBeneficiario", "homebanking");
                sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_beneficiarios] (idCobis, canal, momento, direccionIp, cbu, cuenta, documento, nombre, accion) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(idCobis());
                sqlRequest.add("HB");
                sqlRequest.add(contexto.ip());
                sqlRequest.add(cbu);
                sqlRequest.add(cuenta);
                sqlRequest.add(documento);
                sqlRequest.add(nombre);
                sqlRequest.add(accion);
                Sql.response(sqlRequest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /* ========== CUENTAS ========== */
    public List<Cuenta> cuentas() {
        List<Cuenta> cuentas = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("cuentas").objetos()) {
                cuentas.add(new Cuenta(this, item));
            }
            return cuentas;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cuentas")) {
            Cuenta cuenta = new Cuenta(this, item);
            if (cuenta.mostrar()) {
                cuentas.add(cuenta);
            }
        }
        return cuentas;
    }

    public List<Cuenta> cuentasPesos() {
        List<Cuenta> cuentas = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("cuentas").objetos()) {
                if(!item.string("codigoTitularidad").equals("F") && item.string("codMoneda").equals("80")) {
                    cuentas.add(new Cuenta(this, item));
                }
            }
            return cuentas;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cuentas")) {
            Cuenta cuenta = new Cuenta(this, item);
            if (cuenta.mostrar() && cuenta.esPesos()) {
                cuentas.add(cuenta);
            }
        }
        return cuentas;
    }

    public List<Cuenta> cuentasDolares() {
        List<Cuenta> cuentas = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("cuentas").objetos()) {
                if(!item.string("codigoTitularidad").equals("F") && item.string("codMoneda").equals("2")) {
                    cuentas.add(new Cuenta(this, item));
                }
            }
            return cuentas;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cuentas")) {
            Cuenta cuenta = new Cuenta(this, item);
            if (cuenta.mostrar() && cuenta.esDolares()) {
                cuentas.add(cuenta);
            }
        }
        return cuentas;
    }

    public boolean tieneCuentaPropia() {
        return cuentasPesos().size() > 1 || cuentasDolares().size() > 1;
    }

    public Cuenta cuentaPorDefecto() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.esTitular()) {
                return cuenta;
            }
        }
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos()) {
                return cuenta;
            }
        }
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esDolares() && cuenta.esTitular()) {
                return cuenta;
            }
        }
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esDolares()) {
                return cuenta;
            }
        }
        return null;
    }

    public Cuenta cuenta(String id) {
        return cuenta(id, null);
    }

    public Cuenta cuentaPorCBU(String cbu) {
        for (Cuenta cuenta : cuentas()) {
            if (cbu != null && (cbu.equals(cuenta.cbu()))) {
                return cuenta;
            }
        }
        return null;
    }

    public Cuenta cuenta(String id, Cuenta cuentaPorDefecto) {
        for (Cuenta cuenta : cuentas()) {
            if (id != null && (id.equals(cuenta.id()) || id.equals(cuenta.numero()))) {
                return cuenta;
            }
        }
        return cuentaPorDefecto;
    }

    public Cuenta cuentaUnipersonalPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.estaActiva()) {
                return cuenta;
            }
        }
        return null;
    }

    public Cuenta cajaAhorroTitularPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.esTitular() && cuenta.estaActiva() && cuenta.esCajaAhorro()) {
                return cuenta;
            }
        }
        return null;
    }

    public Boolean tieneCajaAhorroActivaPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.estaActiva() && cuenta.esCajaAhorro()) {
                return true;
            }
        }
        return false;
    }

    public Cuenta cuentaCorrienteTitular() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.esTitular() && cuenta.estaActiva() && cuenta.esCuentaCorriente()) {
                return cuenta;
            }
        }
        return null;
    }

    public Cuenta AdelantoTitular() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.esTitular() && cuenta.estaActiva() && cuenta.esAdelanto()) {
                return cuenta;
            }
        }
        return null;
    }

    public List<Cuenta> cuentasExtendido() {
        List<Cuenta> cuentas = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("cuentas").objetos()) {
                cuentas.add(new Cuenta(this, item));
            }
            return cuentas;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cuentas")) {
            Cuenta cuenta = new Cuenta(this, item);
            if ("FCL".equalsIgnoreCase(cuenta.categoria()) || "R".equalsIgnoreCase(cuenta.categoria())) {
                continue;
            }
            cuentas.add(cuenta);
        }
        return cuentas;
    }

    public Cuenta cuentaExtendido(String id) {
        return cuentaExtendido(id, null);
    }

    public Cuenta cuentaExtendido(String id, Cuenta cuentaPorDefecto) {
        for (Cuenta cuenta : cuentasExtendido()) {
            if (id != null && (id.equals(cuenta.id()) || id.equals(cuenta.numero()))) {
                return cuenta;
            }
        }
        return cuentaPorDefecto;
    }

    /* ========== TARJETA DEBITO ========== */
    public List<TarjetaDebito> tarjetasDebito() {
        List<TarjetaDebito> tarjetasDebito = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("tarjetasDebito").objetos()) {
                if(!"C".equals(item.string("estado"))) {
                    tarjetasDebito.add(new TarjetaDebito(this, item));
                }
            }
            return tarjetasDebito;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("tarjetasDebito")) {
            if (!"C".equals(item.string("estado"))) {
                tarjetasDebito.add(new TarjetaDebito(this, item));
            }
        }
        return tarjetasDebito;
    }

    public List<TarjetaDebito> tarjetasDebitoActivas() {
        List<TarjetaDebito> tarjetasDebito = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for (Objeto item : consolidadaProductos.objeto("tarjetasDebito").objetos()) {
                if(!"C".equals(item.string("estado"))) {
                    Boolean marca = Boolean.valueOf(item.string("activacionTemprana"));
                    if(!marca) {
                        tarjetasDebito.add(new TarjetaDebito(this, item));
                    }
                }
            }
            return tarjetasDebito;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("tarjetasDebito")) {
            if (!"C".equals(item.string("estado"))) {
                TarjetaDebito td = new TarjetaDebito(this, item);
                if (!td.activacionTemprana()) {
                    tarjetasDebito.add(td);
                }
            }
        }
        return tarjetasDebito;
    }

    public TarjetaDebito tarjetaDebito(String id) {
        return tarjetaDebito(id, null);
    }

    public TarjetaDebito tarjetaDebito(String id, TarjetaDebito tarjetaDebitoPorDefecto) {
        for (TarjetaDebito tarjetaDebito : tarjetasDebito()) {
            if (id != null && (id.equals(tarjetaDebito.id()) || id.equals(tarjetaDebito.numero()))) {
                return tarjetaDebito;
            }
        }
        return tarjetaDebitoPorDefecto;
    }

    public Boolean isEnableToGetRedLinkPassword() {
        return tarjetasDebito().stream().filter(td -> "V".equals(td.getEstado()))
                .anyMatch(td -> !td.activacionTemprana() && !td.virtual());
    }

    public List<TarjetaDebito> tarjetasDebitoRedLinkActivo() {
        return tarjetasDebito().stream().filter(td -> "V".equals(td.getEstado()))
                .filter(td -> !td.activacionTemprana() && !td.virtual()).collect(Collectors.toList());
    }

    public List<TarjetaDebito> tarjetasDebitoRedLinkActivoConVirtuales() {
        return tarjetasDebito().stream().filter(td -> "V".equals(td.getEstado())).filter(td -> !td.activacionTemprana())
                .collect(Collectors.toList());
    }

    public TarjetaDebito tarjetaDebitoPorDefecto() {
        TarjetaDebito tarjetaDebitoPorDefecto = null;
        for (TarjetaDebito tarjetaDebito : tarjetasDebito()) {
            if (!tarjetaDebito.activacionTemprana()) {
                tarjetaDebitoPorDefecto = tarjetaDebito;
            }
        }
        if (tarjetaDebitoPorDefecto != null) {
            return tarjetaDebitoPorDefecto;
        }
        for (TarjetaDebito tarjetaDebito : tarjetasDebito()) {
            return tarjetaDebito;
        }
        return null;
    }

    public TarjetaDebito tarjetaDebitoPorDefecto(List<TarjetaDebito> tarjetasDebito) {
        TarjetaDebito tarjetaDebitoPorDefecto = null;
        for (TarjetaDebito tarjetaDebito : tarjetasDebito) {
            if (!tarjetaDebito.activacionTemprana()) {
                if (tarjetaDebitoPorDefecto == null) {
                    tarjetaDebitoPorDefecto = tarjetaDebito;
                } else if (tarjetaDebito.fechaAlta().getTime() > tarjetaDebitoPorDefecto.fechaAlta().getTime()) {
                    tarjetaDebitoPorDefecto = tarjetaDebito;
                }
            }
        }
        if (tarjetaDebitoPorDefecto != null) {
            return tarjetaDebitoPorDefecto;
        }
        for (TarjetaDebito tarjetaDebito : tarjetasDebito) {
            return tarjetaDebito;
        }
        for (TarjetaDebito tarjetaDebito : tarjetasDebito()) {
            return tarjetaDebito;
        }
        return null;
    }

    public TarjetaDebito tarjetaDebitoHabilitadaLink() {
        for (TarjetaDebito tarjetaDebito : tarjetasDebito()) {
            if (tarjetaDebito.habilitadaLink()) {
                return tarjetaDebito;
            }
        }
        return null;
    }

    public TarjetaDebito tarjetaDebitoAsociada(Cuenta cuenta) {
        TarjetaDebito tarjetaDebitoAsociada = null;
        if (tarjetasDebitoActivas().size() == 1) {
            tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
        }
        if (tarjetasDebitoActivas().size() >= 2) {
            tarjetaDebitoAsociada = TarjetaDebitoService.tarjetaAsociada(this, cuenta);
            if (tarjetaDebitoAsociada == null) {
                tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
            }
        }
        return tarjetaDebitoAsociada;
    }

    public TarjetaDebito tarjetaDebitoAsociadaHabilitadaLink(Cuenta cuenta) {
        TarjetaDebito tarjetaDebitoAsociada = null;
        if (tarjetasDebitoActivas().size() == 1) {
            tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
        }
        if (tarjetasDebitoActivas().size() >= 2) {
            tarjetaDebitoAsociada = TarjetaDebitoService.tarjetaAsociadaHabilitadaLink(this, cuenta, false);
            if (tarjetaDebitoAsociada == null) {
                tarjetaDebitoAsociada = TarjetaDebitoService.tarjetaAsociadaHabilitadaLink(this, cuenta, true);
            }
            if (tarjetaDebitoAsociada == null) {
                tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
            }
        }
        return tarjetaDebitoAsociada;
    }

    /* ========== TARJETA CREDITO ========== */
    public List<TarjetaCredito> tarjetasCredito() {
        List<TarjetaCredito> lista = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("tarjetasCredito").objetos()) {
                lista.add(new TarjetaCredito(this, item));
            }
            List<TarjetaCredito> tarjetasCredito = new ArrayList<>();
            for (TarjetaCredito tarjetaCredito : lista) {
                if (tarjetaCredito.esTitular()) {
                    tarjetasCredito.add(tarjetaCredito);
                }
            }
            for (TarjetaCredito tarjetaCredito : lista) {
                if (!tarjetaCredito.esTitular()) {
                    tarjetasCredito.add(tarjetaCredito);
                }
            }
            return tarjetasCredito;
        }
        ApiResponse response = new ApiResponse();
        response = ProductosService.productos(this);

        for (Objeto item : response.objetos("tarjetasCredito")) {
            lista.add(new TarjetaCredito(this, item));
        }
        List<TarjetaCredito> tarjetasCredito = new ArrayList<>();
        for (TarjetaCredito tarjetaCredito : lista) {
            if (tarjetaCredito.esTitular()) {
                tarjetasCredito.add(tarjetaCredito);
            }
        }
        for (TarjetaCredito tarjetaCredito : lista) {
            if (!tarjetaCredito.esTitular()) {
                tarjetasCredito.add(tarjetaCredito);
            }
        }
        return tarjetasCredito;
    }

    public TarjetaCredito tarjetaCredito(String id) {
        return tarjetaCredito(id, null);
    }

    public TarjetaCredito tarjetaCredito(String id, TarjetaCredito tarjetaCreditoPorDefecto) {
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (id != null && (id.equals(tarjetaCredito.id()) || id.equals(tarjetaCredito.numero()))) {
                return tarjetaCredito;
            }
        }
        return tarjetaCreditoPorDefecto;
    }

    public TarjetaCredito tarjetaCreditoTitular() {
        TarjetaCredito tarjetaCreditoTitular = null;
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (tarjetaCredito.esTitular()) {
                tarjetaCreditoTitular = tarjetaCredito;
            }
        }
        return tarjetaCreditoTitular;
    }

    public List<TarjetaCredito> tarjetasCreditoTitularConAdicionalesPropias() {
        TarjetaCredito tarjetaCreditoTitular = tarjetaCreditoTitular();
        List<TarjetaCredito> lista = new ArrayList<>();
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (tarjetaCredito.esTitular()) {
                lista.add(tarjetaCredito);
            }
        }
        if (tarjetaCreditoTitular != null) {
            for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
                if (!tarjetaCredito.esTitular() && tarjetaCreditoTitular.cuenta().equals(tarjetaCredito.cuenta())) {
                    lista.add(tarjetaCredito);
                }
            }
        }
        return lista;
    }

    public List<TarjetaCredito> tarjetasCreditoTitularConAdicionalesTercero() {
        TarjetaCredito tarjetaCreditoTitular = tarjetaCreditoTitular();
        List<TarjetaCredito> lista = new ArrayList<>();
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (tarjetaCredito.esTitular()) {
                lista.add(tarjetaCredito);
            }
        }
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (!tarjetaCredito.esTitular() && (tarjetaCreditoTitular == null
                    || !tarjetaCreditoTitular.cuenta().equals(tarjetaCredito.cuenta()))) {
                lista.add(tarjetaCredito);
            }
        }
        return lista;
    }

    public List<TarjetaCredito> tarjetasCreditoAdicionalesTercero() {
        TarjetaCredito tarjetaCreditoTitular = tarjetaCreditoTitular();
        List<TarjetaCredito> lista = new ArrayList<>();
        for (TarjetaCredito tarjetaCredito : tarjetasCredito()) {
            if (!tarjetaCredito.esTitular() && (tarjetaCreditoTitular == null
                    || !tarjetaCreditoTitular.cuenta().equals(tarjetaCredito.cuenta()))) {
                lista.add(tarjetaCredito);
            }
        }
        return lista;
    }

    /* ========== CUENTA COMITENTE ========== */
    public List<CuentaComitente> cuentasComitentes() {
        List<CuentaComitente> cuentasComitente = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("cuentasComitentes").objetos()) {
                if(!"C".equals(item.string("estado"))) {
                    cuentasComitente.add(new CuentaComitente(item));
                }
            }
            return cuentasComitente;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("inversiones")) {
            if ("UNI".equals(item.string("tipoProducto"))) {
                if (!"C".equals(item.string("estado"))) {
                    cuentasComitente.add(new CuentaComitente(item));
                }
            }
        }
        return cuentasComitente;
    }

    public CuentaComitente cuentaComitente(String id) {
        for (CuentaComitente cuentaComitente : cuentasComitentes()) {
            if (id != null && (id.equals(cuentaComitente.id()) || id.equals(cuentaComitente.numero()))) {
                return cuentaComitente;
            }
        }
        return null;
    }

    public CuentaComitente cuentaComitentePorDefecto() {
        List<CuentaComitente> cuentasComitentes = cuentasComitentes();
        if (!cuentasComitentes.isEmpty()) {
            return cuentasComitentes.get(0);
        }
        return null;
    }

    /* ========== CAJA SEGURIDAD ========== */
    public List<CajaSeguridad> cajasSeguridad() {
        List<CajaSeguridad> cajasSeguridad = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("cajasSeguridad").objetos()) {
                if("CSG".equals(item.string("codigoProducto"))) {
                    cajasSeguridad.add(new CajaSeguridad(item));
                }
            }
            return cajasSeguridad;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cajasSeguridad")) {
            if ("CSG".equals(item.string("tipoProducto"))) {
                cajasSeguridad.add(new CajaSeguridad(item));
            }
        }
        return cajasSeguridad;
    }

    /* ========== PLAZO FIJO ========== */
    public List<PlazoFijo> plazosFijos() {
        List<PlazoFijo> plazosFijos = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("plazosFijos").objetos()) {
                if(!Objeto.setOf("VEN", "CAN", "ANU").contains(item.string("estadoPF"))) {
                    plazosFijos.add(new PlazoFijo(this, item));
                }
            }
            return plazosFijos;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("plazosFijos")) {
            PlazoFijo plazoFijo = new PlazoFijo(this, item);
            if (plazoFijo.mostrar()) {
                plazosFijos.add(new PlazoFijo(this, item));
            }
        }
        return plazosFijos;
    }

    public List<Objeto> getCedips(String cuil, String filtro) {
        List<Objeto> cedips = new ArrayList<>();
        int pagina = 1;

        while (true) {
            ApiResponse response = PlazoFijoService.cedips(this, cuil, filtro, pagina);
            if (response.hayError()) {
                break;
            }

            cedips.addAll(response.objetos());

            if (response.objetos().size() < 20) {
                break;
            }

            pagina++;
        }

        return cedips;
    }

    public List<Objeto> cedipsEstado(String cuil, String estado) {
        String filtro = "$estado$ [eq] __" + estado + "__";
        return getCedips(cuil, filtro);
    }

    public List<Objeto> cedipsTransmitidoPor(String cuil) {
        String filtro = "$transmitido_por$ [eq] __" + cuil + "__";
        return getCedips(cuil, filtro);
    }

    public List<Objeto> cedipsPendientes(String cuil) {
        List<Objeto> cedips = new ArrayList<>();
        for (Objeto cedip : cedipsEstado(cuil, "ACTIVO-PENDIENTE")) {

            for (Objeto transmision : cedip.objetos("transmisiones")) {
                if ("PENDIENTE".equals(transmision.string("estado"))
                        && cuil.equals(transmision.string("cuitBeneficiario"))) {

                    String cuitTransmisor = transmision.string("cuitTransmisor");
                    cedip.set("cuilEmisor", cuitTransmisor);
                    cedip.set("estado", "Pendiente");
                    String fecha = transmision.string("fecha");
                    cedip.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                    cedip.set("embargo", transmision.bool("embargo"));
                    cedips.add(cedip);
                    break;
                }
            }
        }

        return cedips;
    }

    public List<Objeto> cedipsActivos(String cuil, String estado) {
        List<Objeto> cedips = new ArrayList<>();
        for (Objeto cedip : cedipsEstado(cuil, estado)) {

            if (cedip.string("cbuAcreditar").isEmpty()) {
                continue;
            }

            List<Objeto> transmisiones = cedip.objetos("transmisiones");
            if (transmisiones.size() == 0) {
                cedip.set("estado", "Recibido");
                cedip.set("esRechazada", false);
                cedips.add(cedip);
            }

            if (transmisiones.size() > 0) {
                Collections.reverse(transmisiones);
            }

            boolean esRechazada = false;
            boolean esAceptada = false;

            for (Objeto transmision : transmisiones) {

                if (("ANULADA".equals(transmision.string("estado")) || "REPUDIADA".equals(transmision.string("estado"))) && cuil.equals(transmision.string("cuitTransmisor"))) {
                    esRechazada = true;
                }

                if ("ACEPTADA".equals(transmision.string("estado"))) {

                    if (cuil.equals(transmision.string("cuitBeneficiario"))) {
                        String cuitTransmisor = transmision.string("cuitTransmisor");
                        cedip.set("cuilEmisor", cuitTransmisor);
                        cedip.set("estado", "Recibido");
                        cedip.set("esRechazada", esRechazada);
                        String fecha = transmision.string("fecha");
                        cedip.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                        cedip.set("embargo", transmision.bool("embargo"));
                        cedips.add(cedip);
                    }

                    esAceptada = true;
                    break;
                }
            }

            if (esRechazada && !esAceptada && cuil.equals(cedip.string("tenedorDocumento"))) {
                cedip.set("estado", "Recibido");
                cedip.set("esRechazada", true);
                cedips.add(cedip);
            }
        }

        return cedips;
    }

    public List<Objeto> cedipsEnviados(String cuil) {
        List<Objeto> cedips = new ArrayList<>();
        for (Objeto cedip : cedipsEstado(cuil, "ACTIVO-PENDIENTE")) {

            for (Objeto transmision : cedip.objetos("transmisiones")) {
                if ("PENDIENTE".equals(transmision.string("estado"))
                        && cuil.equals(transmision.string("cuitTransmisor"))) {

                    String cuitBeneficiario = transmision.string("cuitBeneficiario");
                    cedip.set("cuilDestino", cuitBeneficiario);
                    cedip.set("estado", "Enviado");
                    String fecha = transmision.string("fecha");
                    cedip.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                    cedip.set("embargo", transmision.bool("embargo"));
                    cedips.add(cedip);
                    break;
                }
            }
        }

        return cedips;
    }

    public List<Objeto> cedipsTransmitidos(String cuil) {
        List<Objeto> cedips = new ArrayList<>();
        for (Objeto cedip : cedipsTransmitidoPor(cuil)) {

            List<Objeto> transmisiones = cedip.objetos("transmisiones");
            if (transmisiones.size() > 0) {
                Collections.reverse(transmisiones);
            }

            for (Objeto transmision : transmisiones) {
                if ("ACEPTADA".equals(transmision.string("estado"))) {

                    if (cuil.equals(transmision.string("cuitTransmisor"))) {
                        String cuitBeneficiario = transmision.string("cuitBeneficiario");
                        cedip.set("cuilDestino", cuitBeneficiario);
                        cedip.set("estado", "Transferido");
                        String fecha = transmision.string("fecha");
                        cedip.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                        cedip.set("embargo", transmision.bool("embargo"));
                        cedips.add(cedip);
                    }

                    break;
                }
            }
        }

        return cedips;
    }

    public List<Objeto> cedip(String cedipId) {
        String cuil = persona().cuit();
        List<Objeto> cedips = new ArrayList<>();

        ApiRequest request = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", this);
        request.path("cedipId", cedipId);
        request.path("cuit", cuil);

        ApiResponse response = Api.response(request, idCobis());
        if (response.hayError()) {
            return null;
        }

        List<Objeto> transmisiones = response.objetos("transmisiones");
        if ("ACTIVO".equals(response.string("estado")) && transmisiones.size() == 0) {
            response.set("estado", "Recibido");
            response.set("esRechazada", false);
            cedips.add(response);
            return cedips;
        }

        if (transmisiones.size() > 0) {
            Collections.reverse(transmisiones);
        }

        if ("ACTIVO-PENDIENTE".equals(response.string("estado"))) {

            for (Objeto transmision : transmisiones) {
                if (!"PENDIENTE".equals(transmision.string("estado"))) {
                    continue;
                }

                if (cuil.equals(transmision.string("cuitTransmisor"))) {
                    String cuitBeneficiario = transmision.string("cuitBeneficiario");
                    response.set("cuilDestino", cuitBeneficiario);
                    response.set("estado", "Enviado");
                    String fecha = transmision.string("fecha");
                    response.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                    response.set("embargo", transmision.bool("embargo"));
                    cedips.add(response);
                    return cedips;
                }

                if (cuil.equals(transmision.string("cuitBeneficiario"))) {
                    String cuitTransmisor = transmision.string("cuitTransmisor");
                    response.set("cuilEmisor", cuitTransmisor);
                    response.set("estado", "Pendiente");
                    String fecha = transmision.string("fecha");
                    response.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                    response.set("embargo", transmision.bool("embargo"));
                    cedips.add(response);
                    return cedips;
                }
            }
        } else if ("ACTIVO".equals(response.string("estado")) || "DEPOSITADO".equals(response.string("estado"))) {

            boolean esRechazada = false;
            boolean esAceptada = false;

            for (Objeto transmision : transmisiones) {

                if (("ANULADA".equals(transmision.string("estado")) || "REPUDIADA".equals(transmision.string("estado")))
                        && cuil.equals(transmision.string("cuitTransmisor"))) {
                    esRechazada = true;
                }

                if ("ACEPTADA".equals(transmision.string("estado"))) {
                    if (cuil.equals(transmision.string("cuitBeneficiario"))) {
                        String cuitTransmisor = transmision.string("cuitTransmisor");
                        response.set("cuilEmisor", cuitTransmisor);
                        response.set("estado", "Recibido");
                        String fecha = transmision.string("fecha");
                        response.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                        response.set("embargo", transmision.bool("embargo"));
                        response.set("esRechazada", esRechazada);
                        cedips.add(response);
                        return cedips;
                    }

                    esAceptada = true;
                    break;
                }
            }

            if (esRechazada && !esAceptada && cuil.equals(response.string("tenedorDocumento"))) {
                response.set("estado", "Recibido");
                response.set("esRechazada", true);
                cedips.add(response);
                return cedips;
            }
        }

        for (Objeto transmision : transmisiones) {
            if ("ACEPTADA".equals(transmision.string("estado"))
                    && cuil.equals(transmision.string("cuitTransmisor"))) {

                String cuitBeneficiario = transmision.string("cuitBeneficiario");
                response.set("cuilDestino", cuitBeneficiario);
                response.set("estado", "Transferido");
                String fecha = transmision.string("fecha");
                response.set("fecha", fecha.length() > 10 ? fecha.substring(0, 10) : fecha);
                response.set("embargo", transmision.bool("embargo"));
                cedips.add(response);
                return cedips;
            }
        }

        return cedips;
    }

    public List<PlazoFijo> getPlazosFijos() {
        List<PlazoFijo> plazosFijos = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("plazosFijos").objetos()) {
                if(PlazoFijo.esValidoEstado(item.string("estadoPF"))) {
                    plazosFijos.add(new PlazoFijo(this, item));
                }
            }
            return plazosFijos;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("plazosFijos")) {
            if (PlazoFijo.esValidoEstado(item.string("estadoPF"))) {
                plazosFijos.add(new PlazoFijo(this, item));
            }
        }
        return plazosFijos;
    }

    public PlazoFijo plazoFijo(String id) {
        for (PlazoFijo plazoFijo : plazosFijos()) {
            if (id != null && (id.equals(plazoFijo.id()) || id.equals(plazoFijo.numero()))) {
                return plazoFijo;
            }
        }
        return null;
    }

    public List<PlazoFijo> plazosFijosConCancelados() {
        List<PlazoFijo> plazosFijos = new ArrayList<>();
        ApiResponse response = ProductosService.productosConCancelados(this);
        for (Objeto item : response.objetos("plazosFijos")) {
            // PlazoFijo plazoFijo = new PlazoFijo(this, item);
            // if (!"ANU".equals(plazoFijo.estado())) {
            plazosFijos.add(new PlazoFijo(this, item));
            // }
        }
        return plazosFijos;
    }

    public PlazoFijo plazoFijoBuscadoEntreCancelados(String id) {
        for (PlazoFijo plazoFijo : plazosFijosConCancelados()) {
            if (id != null && (id.equals(plazoFijo.id()) || id.equals(plazoFijo.numero()))) {
                return plazoFijo;
            }
        }
        return null;
    }

    /* ========== PLAZO FIJO LOGRO ========== */
    public List<PlazoFijoLogro> plazosFijosLogros() {
        Integer limiteContadorPlazoFijoLogro = ConfigHB.integer("limite_contador_plazo_fijo_logro");
        List<PlazoFijoLogro> plazosFijosLogros = new ArrayList<>();
        boolean continuar = true;
        String idPlanAhorro = "0";
        int contador = 0; // este contador es por si por algun motivo no funciona bien el servicio de MW
        // y me sigue mandando 15 planes logros eternamente.
        // no debería pasar nunca, pero por las dudas yo voy a cortar el loop al septimo
        // llamado
        int contadorPag = 0;

        do {
            contador++;
            String finalIdPlanAhorro = idPlanAhorro;
            Futuro<ApiResponse> response = new Futuro<>(() -> PlazoFijoLogrosService.cabecera(this, finalIdPlanAhorro));
            //ApiResponse response = PlazoFijoLogrosService.cabecera(this, idPlanAhorro);
            if (response.get().hayError()) {
                if ("710270".equals(response.get().string("codigo")) || "40003".equals(response.get().string("codigo"))) {
                    return plazosFijosLogros;
                }
                return null;
            }
            for (Objeto cabecera : response.get().objetos()) {
                PlazoFijoLogro plazoFijoLogro = new PlazoFijoLogro(this, cabecera);
                plazosFijosLogros.add(plazoFijoLogro);
                idPlanAhorro = cabecera.string("idPlanAhorro");
            }
            continuar = false;
            if (response.get().objetos() != null && response.get().objetos().size() == 15) {
                continuar = true;
                contadorPag++;
            }
        } while (continuar && contadorPag <= limiteContadorPlazoFijoLogro && contador <= contadorPag);
        return plazosFijosLogros;
    }

    public PlazoFijoLogro plazoFijoLogro(String id) {
        List<PlazoFijoLogro> plazosFijosLogros = plazosFijosLogros();
        for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros) {
            if (id != null && id.equals(plazoFijoLogro.id())) {
                return plazoFijoLogro;
            }
        }
        return null;
    }

    /* ========== PRESTAMO ========== */
    public Boolean tienePrestamosProcrear(String nemonico) {
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("prestamos").objetos()) {
                if(item.string("codigoProducto").equals(nemonico)) {
                    return true;
                }
            }
            return false;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("prestamos")) {
            if (item.string("codigoProducto").equals(nemonico)) {
                return true;
            }
        }
        return false;
    }

    public Boolean tienePrestamosNsp() {
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("prestamos").objetos()) {
                if(item.string("tipo").equals("NSP")) {
                    return true;
                }
            }
            return false;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("prestamos")) {
            if (item.string("tipoProducto").equals("NSP")) {
                return true;
            }
        }
        return false;
    }

    public Boolean tieneAdelantoActivo() {
        for (Prestamo prestamo : prestamos()) {
            if (prestamo.codigo().equalsIgnoreCase("PPADELANTO")) { // deberia ser Adelanto
                return true;
            }
        }
        return false;
    }

    public List<Prestamo> prestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("prestamos").objetos()) {
                if(!item.string("tipo").equals("NSP")) {
                    prestamos.add(new Prestamo(this, item));
                }
            }
            return prestamos;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto item : response.objetos("prestamos")) {
            Prestamo prestamo = new Prestamo(this, item);
//			try {
//				Boolean ocultarMontoAdeudado = HBPrestamo.obtenerNemonicos(this, prestamo.codigo());
//				if (ocultarMontoAdeudado != null && ocultarMontoAdeudado) {
//					continue;
//				}
//			} catch (Exception e) {
//			}
            if (!item.string("tipoProducto").equals("NSP")) {
                prestamos.add(prestamo);
            }
        }
        return prestamos;
    }

    public Prestamo prestamo(String id) {
        for (Prestamo prestamo : prestamos()) {
            if (id != null && (id.equals(prestamo.id()) || id.equals(prestamo.numero()))) {
                return prestamo;
            }
        }
        return null;
    }

    public Cuenta cuentaUnipersonalCAPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.unipersonal() && cuenta.estaActiva() && cuenta.esCajaAhorro()) {
                return cuenta;
            }
        }
        return null;
    }

    public Boolean tieneCuentaCategoriaB() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("B").contains(cuenta.categoria())) {
                return true;
            }
        }
        return false;
    }

    public Boolean esJubilado() {
        try {
            if ("11".equals(SituacionLaboral.situacionLaboralPrincipal(this).idSituacionLaboral)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /* ========== PAQUETE ========== */
    public Boolean tienePaquete() {
        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            Objeto consolidadaProductos = consolidada.objeto("productos");
            for(Objeto item : consolidadaProductos.objeto("paquetes").objetos()) {
                if("PAQ".equals(item.string("tipo"))) {
                    return true;
                }
            }
            return false;
        }

        ApiResponse response = ProductosService.productos(this);
        for (Objeto objeto : response.objetos("productos")) {
            if ("PAQ".equals(objeto.string("tipo"))) {
                return true;
            }
        }
        return false;
    }

    /* ========== SEGUNDO FACTOR ========== */
    public Boolean validaSegundoFactor(String funcionalidad) {
        Boolean validaSegundoFactor = !ConfigHB.esOpenShift();
        String validadorUsado = sesion.validadorUsado;
        List<String> validadores = RestPersona.validadoresSegundoFactor(this, funcionalidad, idCobis());
        for (String validador : validadores) {
            validaSegundoFactor |= validador.equals("tco") && sesion.validaSegundoFactorTarjetaCoordenadas;
            validaSegundoFactor |= validador.equals("email") && sesion.validaSegundoFactorOtp
                    && "email".equals(validadorUsado);
            validaSegundoFactor |= validador.equals("sms") && sesion.validaSegundoFactorOtp
                    && "sms".equals(validadorUsado);
            validaSegundoFactor |= validador.equals("red-link") && sesion.validaSegundoFactorClaveLink;
            validaSegundoFactor |= validador.equals("preguntas-personales")
                    && sesion.validaSegundoFactorPreguntasPersonales;
            validaSegundoFactor |= validador.equals("riesgo-net") && sesion.validaRiesgoNet;
            validaSegundoFactor |= validador.equals("soft-token") && sesion.validaSegundoFactorSoftToken;
        }

        return validaSegundoFactor;
    }

    public void limpiarSegundoFactor() {
        sesion.limpiarSegundoFactor(false);
    }

    public void limpiarSegundoFactorPreCondicionLink() {
        sesion.limpiarSegundoFactor(true);
    }

    public void limpiarSegundoFactorPreCondicionLink(boolean limpiarSoftoken) {
        sesion.limpiarSegundoFactor(true, limpiarSoftoken);
    }

    public void responseHeader(String header, String value) {
        response.header(header, value);
    }

    public void httpCode(int httpCode) {
        response.status(httpCode);
    }

    public void contentType(String contentType) {
        response.type(contentType);
    }

    public String ip() {
        if (request != null) {
            String xforwardedfor = request.headers("X-FORWARDED-FOR");
            if (xforwardedfor != null && !xforwardedfor.isEmpty()) {
                return xforwardedfor.split(",")[0].trim();
            }
        }
        return ip == null ? request.ip() : ip;
    }

    public ContextoHB clonar() {
        ContextoHB clon = new ContextoHB(request, response, canal, ambiente);
        clon.parametros = Objeto.fromJson(parametros.toJson());
        clon.sesion = sesion;
        clon.ip = ip;
        clon.cacheRequest = cacheRequest;
        return clon;
    }

    public Boolean tienePPProcrearDesembolsado() {
        Boolean habilitadoBloqueoAdelantoSueldoPP = "true"
                .equals(ConfigHB.string("prendido_bloqueo_adelanto_sueldo_pp"));

        List<Prestamo> filtrado = Collections.emptyList();

        if (ConfigHB.string("prendido_bloqueo_adelanto_sueldo_pp_nemonicos") != null
                && habilitadoBloqueoAdelantoSueldoPP) {
            Set<String> nemonicos = Objeto
                    .setOf(ConfigHB.string("prendido_bloqueo_adelanto_sueldo_pp_nemonicos").split("_"));

            filtrado = Optional.ofNullable(prestamos()).orElse(Collections.emptyList()).stream()
                    .filter(p -> p.codigo() != null && nemonicos.contains(p.codigo())).collect(Collectors.toList());
        }
        return !(filtrado.isEmpty());
    }

    public Integer nivelSecuencial() {
        return CanalHomeBanking.cantidadMaxima(this);
    }

    public static String embozado(String nombre, String apellido) {
        if (nombre.isEmpty() || apellido.isEmpty()) {
            return "";

        }

        int longitud = apellido.length() + nombre.length() + 1;
        if (longitud <= 19) {
            return apellido.toUpperCase() + "/" + nombre.toUpperCase();
        }

        if (apellido.contains(" ")) {
            apellido = apellido.split("\\s+")[0];
        }

        if (apellido.length() > 12) {
            apellido = apellido.substring(0, 12);
        }

        String embozado = apellido.toUpperCase() + "/" + nombre.toUpperCase();
        return embozado.length() > 19 ? embozado.substring(0, 19).trim() : embozado;
    }

    public boolean esMigrado(ContextoHB contexto) {
        return Util.migracionPrendida() && (Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitHB.esUsuarioMigrado(contexto,
                contexto.idCobis(),
                ar.com.hipotecario.backend.base.Util.documento(contexto.persona().numeroDocumento()))));
    }

    public boolean esMigrado(ContextoHB contexto, String documento) {
        return Util.migracionPrendida() && (Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitHB.esUsuarioMigrado(contexto,
                "",
                ar.com.hipotecario.backend.base.Util.documento(documento))));
    }

    public Respuesta validarTransaccion(ContextoHB contexto, boolean migrado, String funcionalidad, JourneyTransmitEnum journeyTransmitEnum) {
        return (migrado ? TransmitHB.validarCsmTransaccion(contexto, journeyTransmitEnum, "") : contexto.validaSegundoFactor(funcionalidad))
                ? Respuesta.exito()
                : Respuesta.requiereSegundoFactor();
    }

    public Boolean insertarNuevoUsuario(String documento) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertUsuarioTransmit", "hbs");
            sqlRequest.sql = "INSERT INTO [hbs].[dbo].[BM_Migracion_Usuarios] (dni, id_cobis, migrado) VALUES (?, ?, 1)";
            sqlRequest.add(documento);
            sqlRequest.add(idCobis());
            Sql.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Long logTiempo(Long tiempo, String marca) {
        Boolean activado = false;
        if (activado) {
            System.out.println("[+] " + marca + ": " + (System.currentTimeMillis() - tiempo) + " ms");
        }
        return System.currentTimeMillis();
    }

    public static Respuesta productosV4(ContextoHB contexto) {
        Objeto productos = new Objeto();

        long tiempo = System.currentTimeMillis();

        PosicionConsolidadaV4 posicionConsolidada = PosicionConsolidadaV4.get(contexto, contexto.idCobis(), contexto.persona().cuit());

        tiempo = logTiempo(tiempo, "A");

        List<TarjetaCredito> tarjetasCredito = contexto.tarjetasCreditoTitularConAdicionalesTercero();

        // LLAMADAS EN PARALELO
        Futuro<List<ProductoMora>> futuroProductosEnMora = new Futuro<>(() -> HBProducto.getProductosEnMora(contexto));
        Map<String, Futuro<ApiResponse>> futuroDetalleTC = new HashMap<String, Futuro<ApiResponse>>();
        for (TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito : posicionConsolidada.tarjetasCredito) {
            Futuro<ApiResponse> futuro = new Futuro<>(
                    () -> TarjetaCreditoService.consultaTarjetaCreditoCacheV4(contexto, tarjetaCredito.numero));
            futuroDetalleTC.put(tarjetaCredito.numero, futuro);
        }

        Map<String, Futuro<Objeto>> futuroResumenTC = new HashMap<String, Futuro<Objeto>>();

        for (TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito : posicionConsolidada.tarjetasCredito) {
            Futuro<Objeto> futuro = new Futuro<>(
                    () -> HBTarjetas.fechasCierreVtoTarjetaCreditoV4(contexto, tarjetaCredito, null));
            futuroResumenTC.put(tarjetaCredito.numero, futuro);
        }

        futuroProductosEnMora.tryGet();
        for (TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito : posicionConsolidada.tarjetasCredito) {
            futuroDetalleTC.get(tarjetaCredito.numero).tryGet();
            futuroResumenTC.get(tarjetaCredito.numero).tryGet();
        }

        Futuro<List<ar.com.hipotecario.backend.base.Objeto>> listTarjetaDebito = new Futuro<>(() -> TarjetasDebitoV4.tarjetasDebitoById(contexto));
        Futuro<List<ar.com.hipotecario.backend.base.Objeto>> listPrestamo = new Futuro<>(() -> PrestamosV4.prestamosByIdCliente(contexto));

        listTarjetaDebito.tryGet();
        listPrestamo.tryGet();

        // FIN LLAMADAS EN PARALELO

        tiempo = logTiempo(tiempo, "B");

        BigDecimal cuentasSaldoPesos = new BigDecimal(0.0);
        BigDecimal cuentasSaldoDolares = new BigDecimal(0.0);
        Objeto productosPaquete = new Objeto();
        Boolean mostrarArrepentimiento = false;
        int cantidadDiasArrepentimiento = 10;
        Objeto adelantoExistente = new Objeto();

        tiempo = logTiempo(tiempo, "C");

        for (CuentasV4.CuentaV4 cuenta : posicionConsolidada.cuentas) {

            Objeto item = new Objeto();
            item.set("id", cuenta.detProducto);
            item.set("tdInactiva", false);
            item.set("descripcion", cuenta.descripcion());
            item.set("numero", cuenta.numeroProducto);
            item.set("numeroFormateado", cuenta.numeroFormateado());
            item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
            item.set("descripcionCorta", cuenta.descripcionCorta());
            item.set("titularidad", cuenta.titularidad());
            item.set("moneda", cuenta.moneda());
            item.set("simboloMoneda", cuenta.simboloMoneda());
            item.set("estado", cuenta.descripcionEstado());
            item.set("saldo", cuenta.importe);
            item.set("saldoFormateado", cuenta.saldoFormateado());
            //item.set("acuerdo", cuenta.acuerdo());
            //item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
//			item.set("disponible",
//					cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
            item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
            item.set("fechaAlta", cuenta.fechaAlta);
//			if (cuenta.fechaAlta != null && "AHO".equals(cuenta.tipo)) {
//				item.set("sePuedeArrepentir",
//						Fecha.cantidadDias(cuenta.fechaAlta, new Date()) <= cantidadDiasArrepentimiento);
//				mostrarArrepentimiento = mostrarArrepentimiento
//						|| Fecha.cantidadDias(cuenta.fechaAlta, new Date()) <= cantidadDiasArrepentimiento;
//			}
//			if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
//				item.set("adelantoDisponible", cuenta.adelantoDisponible());
//				item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
//				item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
//				item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
//				item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
//				item.set("adelantoInteresesDevengadosFormateado",
//						Formateador.importe(cuenta.adelantoInteresesDevengados()));
//				adelantoExistente = item;
//			} else {
//				productos.add("cuentas", item);
//			}

            productos.add("cuentas", item);

            cuentasSaldoPesos = "$".equals(cuenta.simboloMoneda()) ? cuentasSaldoPesos.add(cuenta.importe)
                    : cuentasSaldoPesos;
            cuentasSaldoDolares = "USD".equals(cuenta.simboloMoneda()) ? cuentasSaldoDolares.add(cuenta.importe)
                    : cuentasSaldoDolares;

            if (cuenta.codigoPaquete != null && !"".equals(cuenta.codigoPaquete)) {
                if (!"80".equals(cuenta.moneda()) || cuenta.esCuentaCorriente()) {
                    Objeto productoPaquete = new Objeto();
                    productoPaquete.set("id", cuenta.detProducto);
                    productoPaquete.set("titulo", cuenta.descProducto);
                    productoPaquete.set("descripcion", cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());
                    productosPaquete.add(productoPaquete);
                }
            }
        }
        productos.set("cuentas", HBProducto.reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente));

        tiempo = logTiempo(tiempo, "D");

        //Recoro el for para guardar los datos faltantes dentro de la posicion consolidada
        for (TarjetasDebitoV4.TarjetaDebitoV4 tarjetaDebitoSelect : posicionConsolidada.tarjetasDebito) {
            for (ar.com.hipotecario.backend.base.Objeto tDebito : listTarjetaDebito.get()) {
                if (tarjetaDebitoSelect.numeroProducto.equals(tDebito.string("numeroProducto"))) {
                    tarjetaDebitoSelect.activacionTemprana = Boolean.valueOf(tDebito.string("activacionTemprana"));
                    tarjetaDebitoSelect.idPaquete = tDebito.string("idPaquete");
                    tarjetaDebitoSelect.virtual = Boolean.valueOf(tDebito.string("virtual"));
                }
            }
        }

        for (TarjetasDebitoV4.TarjetaDebitoV4 tarjetaDebito : posicionConsolidada.tarjetasDebito) {
            if (tarjetaDebito.activacionTemprana) {
                continue;
            }

            Objeto item = new Objeto();
            item.set("id", tarjetaDebito.detProducto);
            item.set("descripcion", tarjetaDebito.descripcion());
            item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
            item.set("titularidad", tarjetaDebito.titularidad());
            item.set("virtual", tarjetaDebito.virtual);
            if (!ConfigHB.esOpenShift()) {
                item.set("numero", tarjetaDebito.numeroProducto);
            }
            productos.add("tarjetasDebito", item);

            if (tarjetaDebito.idPaquete != null && !"".equals(tarjetaDebito.idPaquete)) {
                Objeto productoPaquete = new Objeto();
                productoPaquete.set("id", tarjetaDebito.detProducto);
                productoPaquete.set("titulo", "Tarjeta de Débito");
                productoPaquete.set("descripcion", "VISA XXXX-" + tarjetaDebito.ultimos4digitos());
                productosPaquete.add(productoPaquete);
            }
        }

        tiempo = logTiempo(tiempo, "E");

        for (TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito : posicionConsolidada.tarjetasCredito) {
            Objeto item = new Objeto();
            item.set("id", tarjetaCredito.numero);
            item.set("descripcion", tarjetaCredito.descTipoTarjeta);
            item.set("tipo", tarjetaCredito.tipo());
            item.set("idTipo", tarjetaCredito.tipoTarjeta);
            item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
            item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
            item.set("estado", tarjetaCredito.estado());
            item.set("titularidad", tarjetaCredito.titularidad());
            item.set("debitosPesos", tarjetaCredito.debitosEnCursoPesos);
            item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
            item.set("debitosDolares", tarjetaCredito.debitosEnCursoDolares);
            item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
            item.set("cuenta", tarjetaCredito.cuenta);

            try {
                futuroDetalleTC.get(tarjetaCredito.numero);
                item.set("stopDebit", tarjetaCredito.stopDebit(contexto, tarjetaCredito.numero));
                item.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico(contexto, tarjetaCredito.numero));
            } catch (Exception e) {
                item.set("stopDebit", "NO");
                item.set("adheridoResumenElectronico", true);
            }

            String vencimiento;
            String cierre;

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
                    "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
                Objeto fechaTC = futuroResumenTC.get(tarjetaCredito.numero).get();
                vencimiento = fechaTC.string("vencimiento");
                cierre = fechaTC.string("cierre");
            } else {
                cierre = tarjetaCredito.cierreActual;
                vencimiento = tarjetaCredito.fechaVencActual;
            }
            item.set("fechaCierre", cierre);
            item.set("fechaVencimiento", vencimiento);
//			item.set("fechaProximoCierre", tarjetaCredito.fechaProximoCierre("dd/MM/yyyy"));
            item.set("fechaAlta", tarjetaCredito.fechaAlta);
            item.set("formaPago", tarjetaCredito.formaPago());
            item.set("esHml", tarjetaCredito.esHML());
            if (!ConfigHB.esOpenShift()) {
                item.set("numero", tarjetaCredito.idEncriptado());
                item.set("cuenta", tarjetaCredito.cuenta);
            }
//			if (tarjetaCredito.fechaAlta != null) {
//				item.set("sePuedeArrepentir",
//						Fecha.cantidadDias(tarjetaCredito.fechaAlta, new Date()) <= cantidadDiasArrepentimiento);
//				mostrarArrepentimiento = mostrarArrepentimiento || Fecha.cantidadDias(tarjetaCredito.fechaAlta,
//						new Date()) <= cantidadDiasArrepentimiento;
//			}

            productos.add("tarjetasCredito", item);

            if (tarjetaCredito.idPaquete != null && tarjetaCredito.esTitular()) {
                Objeto productoPaquete = new Objeto();
                productoPaquete.set("id", tarjetaCredito.idEncriptado());
                productoPaquete.set("titulo", "Tarjeta de Crédito");
                productoPaquete.set("descripcion",
                        tarjetaCredito.tipo() + " " + "XXXX-" + tarjetaCredito.ultimos4digitos());
                productosPaquete.add(productoPaquete);
            }
        }

        tiempo = logTiempo(tiempo, "F");

        Objeto objetosPlazosFijos = new Objeto();
        for (PlazosFijosV4.PlazoFijoV4 plazoFijo : posicionConsolidada.plazosFijos) {
            if(plazoFijo.esCedip()) {
                continue;
            }

            Objeto item = new Objeto();
            item.set("id", plazoFijo.codigoProducto);
            item.set("descripcion", plazoFijo.producto());
            item.set("tipo", plazoFijo.descripcion());
            item.set("numero", plazoFijo.numeroProducto);
            item.set("titularidad", plazoFijo.titularidad());
            item.set("moneda", plazoFijo.descripcionMoneda());
            item.set("simboloMoneda", plazoFijo.moneda());
            item.set("importeInicial", plazoFijo.importe);
            item.set("importeInicialFormateado", plazoFijo.importeInicialFormateado());
            item.set("fechaAlta", plazoFijo.fechaAlta);
            item.set("fechaVencimiento", plazoFijo.pfFechaVencimiento);
            item.set("estado", plazoFijo.estado());
            item.set("orden", plazoFijo.pfFechaVencimiento);
            objetosPlazosFijos.add(item);
        }
        productos.set("plazosFijos", objetosPlazosFijos.ordenar("orden"));

        tiempo = logTiempo(tiempo, "G");

        List<ProductoMora> productosEnMora = futuroProductosEnMora.get();
        List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
        productosEnMora.forEach(productoMora -> {
            ProductoMoraDetalles item = HBProducto.getProductosEnMoraDetalles(contexto, productoMora.ctaId());
            if (Objects.nonNull(item)) {
                productosEnMoraDetalles.add(item);
            }
        });

        tiempo = logTiempo(tiempo, "H");

        Objeto consolidadoOnboardings = new Objeto();
        try {
            consolidadoOnboardings.set("onboardingMoraMostrado",
                    Util.tieneMuestreoNemonico(contexto, "ONBOARDING_MORA"));
            consolidadoOnboardings.set("onboardingPromesaMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING"));
        } catch (Exception e) {
            consolidadoOnboardings.set("onboardingMoraMostrado", true);
            consolidadoOnboardings.set("onboardingPromesaMostrado", true);
        }
        productos.set("consolidadoOnboarding", consolidadoOnboardings);

        //Recoro el for para guardar los datos faltantes dentro de la posicion consolidada
        for (PrestamosV4.PrestamoV4 prestamoSelect : posicionConsolidada.prestamos) {
            for (ar.com.hipotecario.backend.base.Objeto prestamo : listPrestamo.get()) {
                if(prestamoSelect.numeroProducto.equals(prestamo.string("numeroProducto"))) {
                    prestamoSelect.descEstado = prestamo.string("descEstado");
                    prestamoSelect.importe = prestamo.bigDecimal("montoAprobado");
                    prestamoSelect.fechaProximoVenc = prestamo.string("fechaProximoVenc");
                    prestamoSelect.formaPago = prestamo.string("formaPago");
                    prestamoSelect.plazoOriginal = prestamo.string("plazoOriginal");
                    prestamoSelect.esPrecoudeu = prestamo.string("esPrecodeu");
                    prestamoSelect.esProCrear = prestamo.string("esProcrear");
                    prestamoSelect.categoria = prestamo.string("categoria");
                    prestamoSelect.cantCuotasMora = prestamo.integer("cantCuotasMora");
                    prestamoSelect.montoCuotaActual = prestamo.bigDecimal("montoCuotaActual");
                }
            }
        }

        for (PrestamosV4.PrestamoV4 prestamo : posicionConsolidada.prestamos) {
            if (!"C".equals(prestamo.estado)) {
                Objeto item = new Objeto();
                item.set("id", prestamo.codigoProducto);
                item.set("descripcion", prestamo.descripcion());
                item.set("tipo", prestamo.tipo(contexto, prestamo.numeroProducto));
                item.set("numero", prestamo.numeroProducto);
                item.set("titularidad", prestamo.titularidad());
                item.set("moneda", prestamo.descripcionMoneda());
                item.set("simboloMoneda", prestamo.simboloMoneda());
                item.set("formaPago", PrestamosV4.PrestamoV4.formasPago(prestamo.formaPago));
                //item.set("tipoFormaPagoActual", PrestamosV4.PrestamoV4.debitoAutomatico(contexto, prestamo.numeroProducto) || prestamo.formaPago.equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
                item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago(prestamo));
                item.set("habilitaMenuPago", prestamo.habilitadoMenuPago(prestamo));
                item.set("ultimos4digitosCuenta", prestamo.ultimos4digitos(prestamo.cuentaAsociada));
                item.set("fechaAlta", prestamo.fechaAlta);
                item.set("fechaProximoVencimiento", prestamo.fechaProximoVenc);
                item.set("montoAprobado", prestamo.importe);
                item.set("montoAprobadoFormateado", prestamo.montoAprobadoFormateado());

                item.set("idEstado", prestamo.estado);
                item.set("estado", prestamo.descEstado);
                item.set("subtipo", prestamo.detProducto);
                item.set("fechaProximoVencimiento",prestamo.fechaProximoVenc);
                item.set("esProcrear", prestamo.esProCrear);
                item.set("montoCuotaActual", prestamo.montoCuotaActual);
                item.set("cantidadCuotas", prestamo.plazoOriginal);
                item.set("numeroProducto", prestamo.numeroProducto);

                if (prestamo.cuotaActual(contexto, prestamo.numeroProducto) != null && prestamo.cuotaActual(contexto, prestamo.numeroProducto) >= 0) {
                    item.set("cuotaActual", prestamo.cuotaActual(contexto, prestamo.numeroProducto));
                } else {
                    item.set("cuotaActual", "-");
                }

//				if (prestamo.fechaAlta != null) {
//					item.set("sePuedeArrepentir",
//							Fecha.cantidadDias(prestamo.consolidada().date("fechaAlta", "yyyy-MM-dd"),
//									new Date()) <= cantidadDiasArrepentimiento);
//					mostrarArrepentimiento = mostrarArrepentimiento
//							|| Fecha.cantidadDias(prestamo.consolidada().date("fechaAlta", "yyyy-MM-dd"),
//							new Date()) <= cantidadDiasArrepentimiento;
//				}

                HBProducto.verificarEstadoMoraTempranaV4(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item);
                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
                    try {
                        HBPrestamo.obtenerProximaCuotaV4(contexto, prestamo, item);
                    } catch (Exception ignored) {
                    }
                }

                try {
                    Integer cantidadCuotasPagadas = prestamo.cuotas(contexto, prestamo).stream().filter(cuota -> HBPrestamo.ESTADO_CUOTA_CANCELADO.equals(cuota.estado())).collect(Collectors.toList()).size();
                    item.set("cantidadCuotasPagadas", !prestamo.enConstruccion(contexto, prestamo.numeroProducto) ? cantidadCuotasPagadas : 0);
                } catch (Exception e) {
                    item.set("cantidadCuotasPagadas", 0);
                }

                item.set("cantidadCuotasPendientes", !prestamo.enConstruccion(contexto, prestamo.numeroProducto) ? prestamo.cuotasPendientes(contexto, prestamo.numeroProducto) : prestamo.cantidadCuotas(contexto, prestamo.numeroProducto));
                item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas(contexto, prestamo.numeroProducto));
                item.set("codigo", prestamo.codigoProducto);
                item.set("enConstruccion", prestamo.enConstruccion(contexto, prestamo.numeroProducto));
                item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago(prestamo));
                item.set("habilitaMenuPago", prestamo.habilitadoMenuPago(prestamo));
                item.set("idMoneda", prestamo.codMoneda);
                item.set("idTipoProducto", prestamo.tipo);
                //item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
                item.set("mostrarLinkDecreto767", false);
                item.set("ocultarMontoAdeudado", HBPrestamo.esProcrear(contexto, prestamo.codigoProducto));
                item.set("pagable", prestamo.detalle(contexto, prestamo.numeroProducto).string("pagable"));
                //item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
                item.set("saldoActual", prestamo.montoUltimaCuotaFormateado(contexto, prestamo.numeroProducto));
                //item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
                item.set("urlDecreto767", ConfigHB.string("url_decreto_767", ""));

                productos.add("prestamos", item);
            }
        }

        tiempo = logTiempo(tiempo, "I");

        for (CuentasComitentesV4.CuentaComitenteV4 cuentaComitente : posicionConsolidada.cuentasComitentes) {
            Objeto item = new Objeto();
            item.set("id", cuentaComitente.codigoProducto);
            item.set("descripcion", cuentaComitente.descripcion());
            item.set("numero", cuentaComitente.numeroProducto);
            item.set("titularidad", cuentaComitente.titularidad());
//			if (cuentaComitente.fechaAlta != null) {
//				item.set("sePuedeArrepentir",
//						Fecha.cantidadDias(cuentaComitente.fechaAlta(), new Date()) <= cantidadDiasArrepentimiento);
//				mostrarArrepentimiento = mostrarArrepentimiento
//						|| Fecha.cantidadDias(cuentaComitente.fechaAlta(), new Date()) <= cantidadDiasArrepentimiento;
//			}
            productos.add("cuentasComitentes", item);
        }

        tiempo = logTiempo(tiempo, "J");

        for (CajasSeguridadV4.CajaSeguridadV4 cajaSeguridad : posicionConsolidada.cajasSeguridad) {
            Objeto item = new Objeto();
            item.set("id", cajaSeguridad.codigoProducto);
            item.set("descripcion", cajaSeguridad.descripcion());
            item.set("numero", cajaSeguridad.numeroProducto);
            item.set("titularidad", cajaSeguridad.titularidad());
            item.set("estado", cajaSeguridad.estado());
            item.set("fechaVencimiento", cajaSeguridad.pfFechaVencimiento);
//			item.set("sucursal", cajaSeguridad.sucursal());
            productos.add("cajasSeguridad", item);
        }

        tiempo = logTiempo(tiempo, "K");

        tiempo = logTiempo(tiempo, "L");

        Objeto consolidadoSaldos = new Objeto();
        consolidadoSaldos.set("cuentasSaldoPesos", cuentasSaldoPesos);
        consolidadoSaldos.set("cuentasSaldoDolares", cuentasSaldoDolares);
        productos.set("consolidadoSaldos", consolidadoSaldos);
        productos.set("mostrarArrepentimiento", mostrarArrepentimiento);

        Respuesta respuesta = Respuesta.exito("productos", productos);
        if (ProductosService.productos(contexto).objetos("errores").size() > 0) {
            respuesta.setEstadoExistenErrores();
        }

        tiempo = logTiempo(tiempo, "M");

        consolidada = respuesta;

        return respuesta;
    }
}

