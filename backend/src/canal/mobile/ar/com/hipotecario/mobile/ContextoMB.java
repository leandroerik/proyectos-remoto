package ar.com.hipotecario.mobile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBProcrearRefaccion;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Encriptador;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Redis;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.CajaSeguridad;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaComitente;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.negocio.PlazoFijo;
import ar.com.hipotecario.mobile.negocio.PlazoFijoLogro;
import ar.com.hipotecario.mobile.negocio.Prestamo;
import ar.com.hipotecario.mobile.negocio.SituacionLaboral;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.*;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

public class ContextoMB extends Contexto {

    public String idSesionLocal = "";

    /* ========== CONSTRUCTORES ========== */
    public ContextoMB(Request request, Response response, String canal, String ambiente) {
        super(request, response, canal, ambiente);

        this.request = request.raw();
        this.response = response.raw();
        this.parametros = Objeto.fromJson(super.parametros.toJson());

        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionMB.class);
        } else {
            this.sesion = (SesionMB) request.raw().getSession().getAttribute("sesion");
        }

        if (sesion == null) {
            crearSesion();
        }

        setParametrosSesion();

        Map<String, Archivo> archivos = super.parametros.archivos();
        for (String clave : archivos.keySet()) {
            Archivo archivo = archivos.get(clave);
            this.parametros.set(clave, archivo.nombre);
            this.archivos.put(clave, archivo.bytes);
        }
        this.idSesionLocal = idSesion();
    }

    /* ========== ATRIBUTOS ========== */
    public HttpServletRequest request;
    public HttpServletResponse response;
    private SesionMB sesion;
    public Objeto parametros;
    public Map<String, byte[]> archivos = new ConcurrentHashMap<>();
    public Map<String, String> cachePorRequest = new ConcurrentHashMap<>();
    public Map<String, Integer> cachePorRequestHttp = new ConcurrentHashMap<>();
    public Map<PlazoFijoLogro, List<ApiResponseMB>> cacheDetallePlazoFijo;

    /* ========== ATRIBUTOS PARA TEST ========== */
//	private String idCobis;
    private String idSesion;
    private String ip;

    /* ========== ATRIBUTOS DE SESION ========== */
    public final String CONTEXT_SESSION_ID = "contextSessionId";
    public final String CONTEXT_SIMPLE_PATH = "contextSimplePath";
    public final String CONTEXT_USUARIO_LOGUEADO = "contextUsuarioLogueado";

    /* ========== CONSTRUCTOR PARA TEST ========== */
    public ContextoMB(String idCobis, String idSesion, String ip) {
//		this.idCobis = idCobis;
        this.idSesion = idSesion;
        this.ip = ip;
        this.sesion = new SesionMB(idSesion);
        this.parametros = new Objeto();
        this.sesion.setUsuarioLogueado(true);
    }

    /* ========== CONSTRUCTOR ========== */
    public ContextoMB(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.idSesion = request.getSession().getId();

        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionMB.class);
        } else {
            this.sesion = (SesionMB) request.getSession().getAttribute("sesion");
        }

        if (sesion == null) {
            crearSesion();
        }
        setParametrosSesion();

    }

    public ContextoMB(HttpServletRequest request, HttpServletResponse response, String canal, String ambiente) {
        super(RequestResponseFactory.create(request), RequestResponseFactory.create(response), canal, ambiente);

        this.request = request;
        this.response = response;
        this.idSesion = request.getSession().getId();

        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionMB.class);
        } else {
            this.sesion = (SesionMB) request.getSession().getAttribute("sesion");
        }

        if (sesion == null) {
            crearSesion();
        }

    }

    /* ========== SESION ========== */
    public void crearSesion() {
        sesion = new SesionMB(idSesion());
        if (Redis.habilitado) {
            Redis.set(idSesion(), sesion);
        } else {
            request.getSession().setAttribute("sesion", sesion);
        }
    }

    public void eliminarSesion(Boolean eliminarBaseDatos, String idCobis) {
        Redis.del(idSesion());

        if (ConfigMB.bool("validar_sesion_duplicada", false) && eliminarBaseDatos)
            SqlHomebanking.eliminarSesion(idCobis);

        request.getSession().invalidate();
        crearSesion();
        setParametrosSesion();
    }

    /* ========== UTIL ========== */
    public Map<String, String> headers() {
        Map<String, String> headers = Collections.list(((HttpServletRequest) request).getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, ((HttpServletRequest) request)::getHeader));
        return headers;
    }

    private void setParametrosSesion() {
        this.parametros.set(CONTEXT_SESSION_ID, String.valueOf(idSesion().hashCode()).replace("-", ""));
        this.parametros.set(CONTEXT_SIMPLE_PATH, simplePath());
        this.parametros.set(CONTEXT_USUARIO_LOGUEADO, sesion().usuarioLogueado());
    }

    public Objeto getParametros() {
        return this.parametros;
    }

    public String idCobis() {
        return sesion.idCobis();
    }

    public String idSesion() {
        try {
            String idSesion = this.idSesion == null ? request.getSession().getId() : this.idSesion;
            return idSesion;
        } catch (Exception e) {
            return this.idSesionLocal;
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

    public String ip() {
        if (request != null) {
            String xforwardedfor = request.getHeader("X-FORWARDED-FOR");
            if (xforwardedfor != null && !xforwardedfor.isEmpty()) {
                return xforwardedfor.split(",")[0].trim();
            }
        }
        return ip == null ? request.getRemoteAddr() : ip;
    }

    public SesionMB sesion() {
        if (Redis.habilitado) {
            this.sesion = Redis.get(idSesion(), SesionMB.class);
        } else if (request != null) {
            try {
                this.sesion = (SesionMB) request.getSession().getAttribute("sesion");
            } catch (Exception e) {
            }
        }
        return this.sesion;
    }

    public ContextoMB clonar() {
        ContextoMB clon = new ContextoMB(request, response, canal, ambiente);
        clon.parametros = Objeto.fromJson(parametros.toJson());
        setParametrosSesion();
        return clon;
    }

    /* ========== HTTP ========== */
    public void setStatus(int status) {
        response.setStatus(status);
    }

    public void setHeader(String clave, String valor) {
        response.setHeader(clave, valor);
    }

    public void setContentType(String type) {
        response.setContentType(type);
    }

    /* ========== PERSONA ========== */
    public Persona persona() {
        ApiResponseMB response = RestPersona.clientes(this);
        if (!response.hayError()) {
            return new Persona(this, response.objetos().get(0));
        }
        return null;
    }

    public Boolean esMonoProductoTC() {
        Boolean esMonoProductoTC = true;
        esMonoProductoTC &= cuentas().isEmpty();
        esMonoProductoTC &= tarjetasDebito().isEmpty();
//		esMonoProductoTC &= !tarjetasCredito().isEmpty();
        return esMonoProductoTC;
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

    /* ========== CUENTAS ========== */
    public List<Cuenta> cuentas() {
        List<Cuenta> cuentas = new ArrayList<>();
        ApiResponseMB response = ProductosService.productos(this);
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
        ApiResponseMB response = ProductosService.productos(this);
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
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cuentas")) {
            Cuenta cuenta = new Cuenta(this, item);
            if (cuenta.mostrar() && cuenta.esDolares()) {
                cuentas.add(cuenta);
            }
        }
        return cuentas;
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

    public Cuenta cuentaTitularPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.esTitular() && cuenta.estaActiva()) {
                return cuenta;
            }
        }
        return null;
    }

    public Cuenta cuentaUnipersonalPesos() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esPesos() && cuenta.unipersonal() && cuenta.estaActiva()) {
                return cuenta;
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

    public Cuenta cajaAhorroTitularDolares() {
        for (Cuenta cuenta : cuentas()) {
            if (cuenta.esDolares() && cuenta.esTitular() && cuenta.estaActiva() && cuenta.esCajaAhorro()) {
                return cuenta;
            }
        }
        return null;
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
        ApiResponseMB response = ProductosService.productos(this);
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
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("tarjetasDebito")) {
            if (!"C".equals(item.string("estado"))) {
                tarjetasDebito.add(new TarjetaDebito(this, item));
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
        if (tarjetasDebito().size() == 1) {
            tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
        }
        if (tarjetasDebito().size() >= 2) {
            tarjetaDebitoAsociada = TarjetaDebitoService.tarjetaAsociada(this, cuenta);
            if (tarjetaDebitoAsociada == null) {
                tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
            }
        }
        return tarjetaDebitoAsociada;
    }

    public TarjetaDebito tarjetaDebitoAsociadaHabilitadaLink(Cuenta cuenta) {
        TarjetaDebito tarjetaDebitoAsociada = null;
        if (tarjetasDebito().size() == 1) {
            tarjetaDebitoAsociada = tarjetaDebitoPorDefecto();
        }
        if (tarjetasDebito().size() >= 2) {
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
        ApiResponseMB response = ProductosService.productos(this);
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

    /* ========== CUENTA COMITENTE ========== */
    public List<CuentaComitente> cuentasComitentes() {
        List<CuentaComitente> cuentasComitente = new ArrayList<>();
        ApiResponseMB response = ProductosService.productos(this);
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
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("cajasSeguridad")) {
            if ("CSG".equals(item.string("tipoProducto"))) {
                cajasSeguridad.add(new CajaSeguridad(item));
            }
        }
        return cajasSeguridad;
    }

    public CajaSeguridad cajaSeguridad(String id) {
        for (CajaSeguridad cajaSeguridad : cajasSeguridad()) {
            if (id != null && (id.equals(cajaSeguridad.id()) || id.equals(cajaSeguridad.numero()))) {
                return cajaSeguridad;
            }
        }
        return null;
    }

    /* ========== PLAZO FIJO ========== */
    public List<PlazoFijo> plazosFijos() {
        List<PlazoFijo> plazosFijos = new ArrayList<>();
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("plazosFijos")) {
            PlazoFijo plazoFijo = new PlazoFijo(this, item);
            if (plazoFijo.mostrar()) {
                plazosFijos.add(new PlazoFijo(this, item));
            }
        }
        return plazosFijos;
    }

    public List<PlazoFijo> getPlazosFijos() {
        List<PlazoFijo> plazosFijos = new ArrayList<>();
        ApiResponseMB response = ProductosService.productos(this);
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
        ApiResponseMB response = ProductosService.productosConCancelados(this);
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
        Integer limiteContadorPlazoFijoLogro = ConfigMB.integer("limite_contador_plazo_fijo_logro");
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
            ApiResponseMB response = PlazoFijoLogrosService.cabecera(this, idPlanAhorro);
            if (response.hayError()) {
                if ("710270".equals(response.string("codigo")) || "40003".equals(response.string("codigo"))) {
                    return plazosFijosLogros;
                }
                return null;
            }
            for (Objeto cabecera : response.objetos()) {
                PlazoFijoLogro plazoFijoLogro = new PlazoFijoLogro(this, cabecera);
                plazosFijosLogros.add(plazoFijoLogro);
                idPlanAhorro = cabecera.string("idPlanAhorro");
            }
            continuar = false;
            if (response.objetos() != null && response.objetos().size() == 15
                    && MBAplicacion.funcionalidadPrendida(idCobis(), "prendido_plan_logro_cabecera_secuencial")) {
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
    public Boolean tienePrestamosNsp() {
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("prestamos")) {
            if (item.string("tipoProducto").equals("NSP")) {
                return true;
            }
        }
        return false;
    }

    public Boolean tieneAdelantoActivo() {
        for (Prestamo prestamo : prestamos()) {
            if (prestamo.codigo().equalsIgnoreCase("PPADELANTO")) {
                return true;
            }
        }
        return false;
    }

    public List<Prestamo> prestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto item : response.objetos("prestamos")) {
            Prestamo prestamo = new Prestamo(this, item);
//			try {
//				Boolean ocultarMontoAdeudado = ApiPrestamo.obtenerNemonicos(this, prestamo.codigo());
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

    public Boolean depositaSueldo() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("K", "EV", "M", "L").contains(cuenta.categoria()) && (cuenta.esPesos())) {
                return true;
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

    /* ========== PAQUETE ========== */
    public Boolean tienePaquete() {
        ApiResponseMB response = ProductosService.productos(this);
        for (Objeto objeto : response.objetos("productos")) {
            if ("PAQ".equals(objeto.string("tipo"))) {
                return true;
            }
        }
        return false;
    }

    /* ========== SEGUNDO FACTOR ========== */
    public Boolean validaSegundoFactor(String funcionalidad) {
        Boolean validaSegundoFactor = !ConfigMB.esOpenShift();// asignar false para probar validaSegundoFactor en local
        String validadorUsado = sesion().validadorUsado();
        List<String> validadores = RestPersona.validadoresSegundoFactor(this, funcionalidad, idCobis());
        for (String validador : validadores) {
            validaSegundoFactor |= validador.equals("buhoFacil") && sesion.validaSegundoFactorBuhoFacil();
            validaSegundoFactor |= validador.equals("biometria") && sesion.validaSegundoFactorBiometria();
            validaSegundoFactor |= validador.equals("tco") && sesion.validaSegundoFactorTarjetaCoordenadas();
            validaSegundoFactor |= validador.equals("email") && sesion.validaSegundoFactorOtp()
                    && "email".equals(validadorUsado);
            validaSegundoFactor |= validador.equals("sms") && sesion.validaSegundoFactorOtp()
                    && "sms".equals(validadorUsado);
            validaSegundoFactor |= validador.equals("red-link") && sesion.validaSegundoFactorClaveLink();
            validaSegundoFactor |= validador.equals("preguntas-personales")
                    && sesion.validaSegundoFactorPreguntasPersonales();
            validaSegundoFactor |= validador.equals("riesgo-net") && sesion.validaRiesgoNet();
            validaSegundoFactor |= validador.equals("soft-token") && sesion.validaSegundoFactorSoftToken();
        }

        if ("transferencia".equals(funcionalidad) && this.tarjetaDebitoPorDefecto() != null
                && sesion.validaSegundoFactorClaveLink()) {
            validaSegundoFactor = true;
        }

        return validaSegundoFactor;
    }

    public void limpiarSegundoFactor() {
        sesion.limpiarSegundoFactor(true);
    }

    public void limpiarSegundoFactorTransferencia() {
        sesion.limpiarSegundoFactor(false);
    }

    public Boolean primeraTransferencia(String cbu) {
        SqlRequestMB sqlRequest = SqlMB.request("SelectCbuTransferidos", "homebanking");
        sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[cbu_transferidos] WHERE idCobis = ? AND cbu = ?";
        sqlRequest.parametros.add(idCobis());
        sqlRequest.parametros.add(cbu);

        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError)
            return true;

        return sqlResponse.registros.isEmpty();
    }

    public void registrarTransferencia(String cbu) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertCbuTransferidos", "homebanking");
            sqlRequest.sql = "INSERT INTO [Hbs].[dbo].[cbu_transferidos] (idCobis, cbu) VALUES (?, ?)";
            sqlRequest.parametros.add(idCobis());
            sqlRequest.parametros.add(cbu);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public Boolean agendada(String cuenta) {
        SqlRequestMB sqlRequest = SqlMB.request("SelectAgendaTransferencias", "hbs");
        sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
        sqlRequest.parametros.add(idCobis());
        sqlRequest.parametros.add(cuenta);
        sqlRequest.parametros.add(cuenta);

        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError) {
            return null;
        }
        return !sqlResponse.registros.isEmpty();
    }

    // TODO: guardar cambios de datos del usuario
    public Boolean insertarContador(String tipo) {
        try {

            SqlRequestMB sqlRequest = SqlMB.request("InsertContador", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'MB')";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            SqlMB.response(sqlRequest);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogLogin(String ip) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogLogin", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(ip);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogLogin(ContextoMB contexto) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogLogin", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
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

            SqlRequestMB request = SqlMB.request("logsLoginCliente", "homebanking");
            request.sql = sql;
            request.add(idCobis());
            SqlResponseMB response = SqlMB.response(request);
            return response.registros;
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    // TODO: guardar cambios de datos del usuario
    public Boolean insertarLogCambioClave(ContextoMB contexto) {

        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogCambioClave", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_clave] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // TODO: guardar cambios de datos del usuario
    public Boolean insertarLogCambioUsuario(ContextoMB contexto) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogCambioUsuario", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_usuario] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogCambioMail(ContextoMB contexto, String mailAnterior, String mailNuevo) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogCambioMail", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_mail] (idCobis, canal, momento, direccionIp, mailAnterior, mailNuevo) VALUES (?, ?, GETDATE(), ?, ?, ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            sqlRequest.add(mailAnterior);
            sqlRequest.add(mailNuevo);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogCambioCelular(ContextoMB contexto, String celularAnterior, String celularNuevo) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogCambioCelular", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_cambio_celular] (idCobis, canal, momento, direccionIp, celularAnterior, celularNuevo) VALUES (?, ?, GETDATE(), ?, ?, ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            sqlRequest.add(celularAnterior);
            sqlRequest.add(celularNuevo);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogEnvioOtp(ContextoMB contexto, String celular, String email, Boolean riesgoNet,
                                       Boolean link, String estado) {

        if (riesgoNet == null) {
            riesgoNet = false;
        }
        if (link == null) {
            link = false;
        }

        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogEnvioOtp", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_envios_otp] (idCobis, canal, momento, direccionIp, celular, email, riesgoNet, link, estado) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            sqlRequest.add(celular);
            sqlRequest.add(email);
            sqlRequest.add(riesgoNet ? 1 : 0);
            sqlRequest.add(link ? 1 : 0);
            sqlRequest.add(estado);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogPrestamos(ContextoMB contexto, BigDecimal importe, Integer plazo, String cuenta) {

        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogEnvioOtp", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_prestamos_personales] (idCobis, canal, momento, direccionIp, importe, plazo, cuenta) VALUES (?, ?, GETDATE(), ?, ?, ?, ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            sqlRequest.add(importe);
            sqlRequest.add(plazo);
            sqlRequest.add(cuenta);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean insertarLogBeneficiario(ContextoMB contexto, String cbu, String cuenta, String documento,
                                           String nombre, String accion) {

        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogBeneficiario", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_beneficiarios] (idCobis, canal, momento, direccionIp, cbu, cuenta, documento, nombre, accion) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";
            sqlRequest.add(idCobis());
            sqlRequest.add("MB");
            sqlRequest.add(contexto.ip());
            sqlRequest.add(cbu);
            sqlRequest.add(cuenta);
            sqlRequest.add(documento);
            sqlRequest.add(nombre);
            sqlRequest.add(accion);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean isOnboardingModo() {
        Boolean isOnboarding = true;
        if (String.valueOf(this.sesion().cache("access_token")).equals("")
                || String.valueOf(this.sesion().cache("refresh_token")).equals("")) {
            isOnboarding = false;
        }
        return isOnboarding;
    }

    public static Boolean cambioDetectadoParaNormativoPP(ContextoMB contexto, Boolean eliminaCache) {
        return RestContexto.cambioDetectadoParaNormativoPPV2(contexto, eliminaCache);
    }

    public Boolean insertLogPreguntasRiesgoNet(ContextoMB contexto, String nombreServicio, String idProceso) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertLogPreguntasRiesgoNet", "hbs");
            sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_servicio] (idCobis, momento, canal, nombreServicio, idProceso) VALUES (?, GETDATE(), ?, ?, ?)";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add("HB");
            sqlRequest.add(nombreServicio);
            sqlRequest.add(idProceso);
            SqlMB.response(sqlRequest);
        } catch (Exception error) {
            return false;
        }
        return true;
    }

    public Boolean esProcrear(ContextoMB contexto) {
        ApiResponseMB response = MBProcrearRefaccion.ofertasProcrear(contexto);
        for (Objeto item : response.objetos()) {
            if (item.string("estado").equals("SO")) {
                return true;
            }
        }
        return false;
    }

    public Boolean esProspecto() {
        ApiResponseMB response = ProductosService.productos(this, true);
        return response.codigo == 204 || "{}".equals(response.json);
    }

    public static List<Objeto> obtenerContador(String idCobis, String tipo, String inicio) {
        SqlRequestMB sqlRequest = SqlMB.request("ObtenerContador", "homebanking");
        StringBuilder str = new StringBuilder();
        str.append("SELECT [id],[idCobis],[tipo],[momento] FROM [homebanking].[dbo].[contador] WHERE idCobis = ? ");
        if (Objects.nonNull(tipo)) {
            str.append("AND tipo in ( " + tipo + " ) ");
        }
        if (Objects.nonNull(inicio)) {
            str.append("AND momento > ? ");
        }
        str.append("ORDER BY momento DESC");
        System.out.println("Query:" + str.toString());
        sqlRequest.sql = str.toString();
        sqlRequest.add(idCobis);
        if (Objects.nonNull(inicio)) {
            sqlRequest.add(inicio);
        }
        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError) {
            return null;
        }
        return sqlResponse.registros;
    }

    public Boolean tieneContador(String tipo) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT TOP 1 * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? order by momento desc";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            return sqlResponse.registros.size() > 0;
        } catch (Exception e) {
        }

        return false;
    }

    public List<Objeto> tieneContador(String tipo, String fechaDesde) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? AND momento > ? ORDER BY momento DESC";
            sqlRequest.add(idCobis());
            sqlRequest.add(tipo);
            sqlRequest.add(fechaDesde);
            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            return sqlResponse.registros;
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public Boolean esPlanSueldo() {
        for (Cuenta cuenta : cuentas()) {
            if (Objeto.setOf("K", "EV", "M").contains(cuenta.categoria())) {
                return true;
            }
        }
        return false;
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

    public Boolean tienePPProcrearDesembolsado() {
        Boolean habilitadoBloqueoAdelantoSueldoPP = "true"
                .equals(ConfigMB.string("prendido_bloqueo_adelanto_sueldo_pp"));

        List<Prestamo> filtrado = Collections.emptyList();

        if (ConfigMB.string("prendido_bloqueo_adelanto_sueldo_pp_nemonicos") != null
                && habilitadoBloqueoAdelantoSueldoPP) {
            Set<String> nemonicos = Objeto
                    .setOf(ConfigMB.string("prendido_bloqueo_adelanto_sueldo_pp_nemonicos").split("_"));

            filtrado = Optional.ofNullable(prestamos()).orElse(Collections.emptyList()).stream()
                    .filter(p -> p.codigo() != null && nemonicos.contains(p.codigo())).collect(Collectors.toList());
        }
        return !(filtrado.isEmpty());
    }

    /* ========== CONSENTIMIENTO AGREGADOR ========== */
    private String code;
    private String state;
    private String bcra_id;
    private int wallet_bcra_id;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getBcra_id() {
        return bcra_id;
    }

    public void setBcra_id(String bcra_id) {
        this.bcra_id = bcra_id;
    }

    public int getWallet_bcra_id() {
        return wallet_bcra_id;
    }

    public void setWallet_bcra_id(int wallet_bcra_id) {
        this.wallet_bcra_id = wallet_bcra_id;
    }

    // SUPER CACHE
    public void superCache(String method, String uri) {
        superCache(method, uri, new Objeto());
    }

    public void superCache(String method, String uri, Objeto params) {
        String base = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        base = ConfigMB.string("local_url", "http://localhost:8080");
        HttpRequest request = new HttpRequest(method, base + uri);
        Enumeration<String> headers = this.request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if ("Host".equals(header) || "Content-Length".equals(header) || "Cookie".equals(header)) {
                continue;
            }
            request.header(header, this.request.getHeader(header));
        }
        request.header("interno", "true");
        request.header("Cookie", "JSESSIONID=" + this.request.getSession().getId());
        if ("GET".equals(method)) {
            for (String key : params.toMap().keySet()) {
                request.query(key, params.toMap().get(key).toString());
            }
        } else {
            request.body(ar.com.hipotecario.backend.base.Objeto.fromMap(params.toMap()));
        }
        ar.com.hipotecario.backend.base.Futuro<Object> response = new ar.com.hipotecario.backend.base.Futuro<>(() -> (Object) request.run());
        CanalMobile.superCache.put(idCobis() + ":" + method + ":" + uri, response);
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

    public void call(String method, String uri, Objeto params) {
        String base = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        base = ConfigMB.string("local_url", "http://localhost:8080");
        HttpRequest request = new HttpRequest(method, base + uri);
        Enumeration<String> headers = this.request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if ("Host".equals(header) || "Content-Length".equals(header) || "Cookie".equals(header)) {
                continue;
            }
            request.header(header, this.request.getHeader(header));
        }
        request.header("interno", "true");
        request.header("Cookie", "JSESSIONID=" + this.request.getSession().getId());
        if ("GET".equals(method)) {
            for (String key : params.toMap().keySet()) {
                request.query(key, params.toMap().get(key).toString());
            }
        } else {
            request.body(ar.com.hipotecario.backend.base.Objeto.fromMap(params.toMap()));
        }
        request.run();
    }

    public String idSesionTransmit() {
        return idCobis();
    }

    public boolean esMigrado(ContextoMB contexto) {
        return Util.migracionPrendida() && (Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitMB.esUsuarioMigrado(contexto,
                contexto.idCobis(),
                ar.com.hipotecario.backend.base.Util.documento(contexto.persona().numeroDocumento()))));
    }

    public boolean esMigrado(ContextoMB contexto, String documento) {
        return Util.migracionPrendida() && (Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitMB.esUsuarioMigrado(contexto,
                "",
                ar.com.hipotecario.backend.base.Util.documento(documento))));
    }

    public RespuestaMB validarTransaccion(ContextoMB contexto, boolean migrado, String funcionalidad, JourneyTransmitEnum journeyTransmitEnum) {
        return (migrado ? TransmitMB.validarCsmTransaccion(contexto, journeyTransmitEnum) : contexto.validaSegundoFactor(funcionalidad))
                ? RespuestaMB.exito()
                : RespuestaMB.requiereSegundoFactor();
    }

    public void limpiarMigracion() {
        sesion.limpiarMigracion();
    }

}