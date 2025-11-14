package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.servicio.api.productos.TarjetasCreditoV4;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cotizacion;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.negocio.Telefono;
import ar.com.hipotecario.canal.homebanking.negocio.TipoOperacionPausado;
import ar.com.hipotecario.canal.homebanking.negocio.TipoOperacionPausadoCredito;
import ar.com.hipotecario.canal.homebanking.servicio.InversionesService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestDelivery;
import ar.com.hipotecario.canal.homebanking.servicio.RestMora;
import ar.com.hipotecario.canal.homebanking.servicio.RestOmnicanalidad;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.RestScheduler;
import ar.com.hipotecario.canal.homebanking.servicio.RestVenta;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaDebitoService;
import ar.gabrielsuarez.glib.G;

public class HBTarjetas {
    public static final String REGEX_ESPACIOS = "\\s{2,}";
    private static final String REGEX_CUOTAS = "(\\s*(\\d{1,2}/\\d{1,2})\\s*)";
    public static final String[] CODIGOS_FINALIZADO = {"F", "D", "R"};
    private static final String ERROR_PAUSADO = "ERROR_PAUSADO";
    private static final String TIPO_OPERACION_INVALIDA = "TIPO_OPERACION_INVALIDA";
    private static final String NO_EXISTE_TARJETA_DEBITO = "NO_EXISTE_TARJETA_DEBITO";
    private static final String MENSAJE_DESARROLLADOR = "mensajeAlDesarrollador";
    private static final String URL_PAUSADO_DEBITO = "/v1/servicios/tarjetas/tarjetas-debito/estados";
    private static final String X_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String X_TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NO_EXISTE_TARJETA_CREDITO = "NO_EXISTE_TARJETA_CREDITO";
    private static final String ERROR_ESTADO_TARJETA_PAUSADO = "ERROR_ESTADO_TARJETA_PAUSADO";

    public static Respuesta consolidadaTarjetas(ContextoHB contexto) {

        try {
            if (!ConfigHB.esProduccion() && contexto.idCobis().equals("8649931")) {
                return Respuesta.error();
            }

            Respuesta respuesta = new Respuesta();

            // LLAMADAS EN PARALELO
            Map<String, Futuro<ApiResponse>> futurosTarjetaDebitoGetEstado = new HashMap<>();
            Map<String, Futuro<List<Cuenta>>> futurosTarjetaDebitoCuentasAsociadas = new HashMap<>();
            for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                String id = tarjetaDebito.numero();
                Futuro<ApiResponse> futuro = new Futuro<>(
                        () -> TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, id));
                futurosTarjetaDebitoGetEstado.put(id, futuro);

                Futuro<List<Cuenta>> futuroCuentasAsociadas = new Futuro<>(
                        () -> tarjetaDebito.cuentasAsociadasPrincipales());
                futurosTarjetaDebitoCuentasAsociadas.put(id, futuroCuentasAsociadas);
            }

            Map<String, Futuro<Boolean>> futurosTarjetaCreditoAdheridoResumenElectronico = new HashMap<>();
            Map<String, Futuro<Objeto>> futurosTarjetaCreditoFechasCierreVto = new HashMap<>();
            for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
                String id = tarjetaCredito.numero();
                Futuro<Boolean> futuroAdheridoResumenElectronico = new Futuro<>(
                        () -> tarjetaCredito.adheridoResumenElectronico());
                futurosTarjetaCreditoAdheridoResumenElectronico.put(id, futuroAdheridoResumenElectronico);

                Futuro<Objeto> futuroFechasCierreVto = new Futuro<>(
                        () -> HBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null));
                futurosTarjetaCreditoFechasCierreVto.put(id, futuroFechasCierreVto);
            }
            // FIN LLAMADAS EN PARALELO

            for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                Objeto item = new Objeto();
                //item.set("id", tarjetaDebito.id());
                item.set("id", tarjetaDebito.idEncriptado());
                item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
                item.set("tipoTarjeta", tarjetaDebito.idTipoTarjeta());
                item.set("limiteCompraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
                item.set("limiteExtraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));
                item.set("virtual", tarjetaDebito.virtual());

                String estadoTarjeta = "";
                // ApiResponse response = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto,
                // tarjetaDebito.numero());
                ApiResponse response = futurosTarjetaDebitoGetEstado.get(tarjetaDebito.numero()).get();
                if (response.hayError()) {
                    if (response.codigo == 500) {
                        estadoTarjeta = "ERROR_LINK";
                    } else {
                        estadoTarjeta = "NO_DETERMINADO";
                    }
                } else {
                    estadoTarjeta = response.string("estadoTarjeta");
                }

                if (estadoTarjeta.toUpperCase().equals("CERRADA"))
                    estadoTarjeta = "HABILITADA";

                if (estadoTarjeta == null || (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
                        && !"INACTIVA".equals(estadoTarjeta.toUpperCase()))) {
                    estadoTarjeta = "NO_DETERMINADO";
                }
                item.set("estado", estadoTarjeta);
                if (tarjetaDebito.cuentasAsociadasPrincipales() != null) {
                    item.set("estadoCuentasAsociadas", "ok");
                    // for (Cuenta cuenta : tarjetaDebito.cuentasAsociadasPrincipales()) {
                    for (Cuenta cuenta : futurosTarjetaDebitoCuentasAsociadas.get(tarjetaDebito.numero()).get()) {
                        Objeto subitem = new Objeto();
                        //subitem.set("id", cuenta.id());
                        subitem.set("id", cuenta.idEncriptado());
                        subitem.set("descripcion", cuenta.producto());
                        subitem.set("numero", cuenta.numero());
                        subitem.set("ultimos4digitos", Formateador.ultimos4digitos(cuenta.numero()));
                        item.add("cuentasAsociadas", subitem);
                    }
                } else {
                    item.set("estadoCuentasAsociadas", "error");
                }

                ApiResponse apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
                if (!apiResponseTitularidad.hayError()
                        && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                        && apiResponseTitularidad.objetos("collection1").size() != 0)
                    item.set("pausada", apiResponseTitularidad.objetos("collection1").get(0).get("Pausada"));

                respuesta.add("tarjetasDebito", item);
            }

            Boolean permitirCambioFormaPago = false;
            for (Cuenta cuenta : contexto.cuentas()) {
                if (cuenta.esPesos()) {
                    permitirCambioFormaPago = true;
                }
            }

            List<TarjetaCredito> tarjetasCredito = contexto.tarjetasCreditoTitularConAdicionalesTercero();

            Map<String, Futuro<ApiResponse>> futurosTarjetaCreditoPausado = null;
            if (prendidoPausadoTarjetaCredito(contexto)) {
                futurosTarjetaCreditoPausado = new HashMap<>();
                for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
                    String id = tarjetaCredito.numero();
                    Futuro<ApiResponse> futuro = new Futuro<>(() -> TarjetaCreditoService.detallePausadoTarjeta(contexto, tarjetaCredito));
                    futurosTarjetaCreditoPausado.put(id, futuro);
                }
            }

            for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
                Objeto item = new Objeto();
                item.set("id", tarjetaCredito.idEncriptado());
                item.set("tipo", tarjetaCredito.tipo());
                item.set("idTipo", tarjetaCredito.idTipo());
                item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
                item.set("esTitular", tarjetaCredito.esTitular());
                item.set("titularidad", tarjetaCredito.titularidad());
                item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
                item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
                item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
                // item.set("adheridoResumenElectronico",
                // tarjetaCredito.adheridoResumenElectronico());
                item.set("adheridoResumenElectronico",
                        futurosTarjetaCreditoAdheridoResumenElectronico.get(tarjetaCredito.numero()).get());
                item.set("cuenta", tarjetaCredito.cuenta());

                String vencimiento;
                String cierre;
                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
                        "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
                    // Objeto fechaTC = HBTarjetas.fechasCierreVtoTarjetaCredito(contexto,
                    // tarjetaCredito, null);
                    Objeto fechaTC = futurosTarjetaCreditoFechasCierreVto.get(tarjetaCredito.numero()).get();
                    vencimiento = fechaTC.string("vencimiento");
                    cierre = fechaTC.string("cierre");
                } else {
                    cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                    vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
                }
                item.set("fechaCierre", cierre);
                item.set("fechaVencimiento", vencimiento);
                // item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
                // item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));

                item.set("formaPago", tarjetaCredito.formaPago());
                item.set("idPaquete", tarjetaCredito.idPaquete());
                item.set("esHml", tarjetaCredito.esHML());
                item.set("permitirCambioFormaPago", permitirCambioFormaPago && tarjetaCredito.esTitular());

                if (futurosTarjetaCreditoPausado != null) {
                    ApiResponse apiResponse = futurosTarjetaCreditoPausado.get(tarjetaCredito.numero()).get();
                    item.set("pausada", !apiResponse.hayError() && !apiResponse.objetos().isEmpty()
                            ? apiResponse.objetos().get(0).string("value").equals("DISABLED")
                            : "");
                }

                respuesta.add("tarjetasCredito", item);
            }

            LocalTime target = LocalTime.now();
            Boolean enHorarioCambioFormaPago = (target.isAfter(LocalTime.parse("06:00:00"))
                    && target.isBefore(LocalTime.parse("21:00:00")));

            Objeto datosExtras = new Objeto();
            datosExtras.set("fueraHorarioCambioFormaPago", !enHorarioCambioFormaPago);
            datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
            datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebitoActivas().size() == 1);
            respuesta.set("datosExtras", datosExtras);

            return respuesta;

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta buscarTooltipConfiguracionTarjeta(ContextoHB contexto) {
        String tipoTarjeta = contexto.parametros.string("tipoTarjeta");
        Respuesta respuesta = new Respuesta();
        Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(),
                "TOOLTIP" + "_" + tipoTarjeta);
        respuesta.set("mostrarTooltipTC", configuracionTarjeta == null);
        return respuesta;
    }

    public static Respuesta agregarTooltipConfiguracionTarjeta(ContextoHB contexto) {
        String tipoTarjeta = contexto.parametros.string("tipoTarjeta");

        Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(),
                "TOOLTIP" + "_" + tipoTarjeta);
        if (configuracionTarjeta == null) {
            SqlHomebanking.saveConfiguracionTarjeta(contexto.idCobis(), "TOOLTIP" + "_" + tipoTarjeta);
        }

        return new Respuesta();
    }

    public static Respuesta consolidadaTarjetasCredito(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        try {

            if (!ConfigHB.esProduccion() && contexto.idCobis().equals("8649931")) {
                return Respuesta.error();
            }

            Boolean permitirCambioFormaPago = false;
            for (Cuenta cuenta : contexto.cuentas()) {
                if (cuenta.esPesos()) {
                    permitirCambioFormaPago = true;
                }
            }

            Map<String, Futuro<ApiResponse>> futurosTarjetaCreditoPausado = null;
            if (prendidoPausadoTarjetaCredito(contexto)) {
                futurosTarjetaCreditoPausado = new HashMap<>();
                for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
                    String id = tarjetaCredito.numero();
                    Futuro<ApiResponse> futuro = new Futuro<>(() -> TarjetaCreditoService.detallePausadoTarjeta(contexto, tarjetaCredito));
                    futurosTarjetaCreditoPausado.put(id, futuro);
                }
            }


            for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
                Objeto item = new Objeto();
                item.set("id", tarjetaCredito.idEncriptado());
                item.set("tipo", tarjetaCredito.tipo());
                item.set("idTipo", tarjetaCredito.idTipo());
                item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
                item.set("esTitular", tarjetaCredito.esTitular());
                item.set("titularidad", tarjetaCredito.titularidad());
                item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
                item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
                item.set("stopDebit", tarjetaCredito.stopDebit());
                item.set("cuenta", tarjetaCredito.cuenta());
                item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
                item.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico());
                item.set("bin", tarjetaCredito.numeroBin());

                String vencimiento;
                String cierre;
                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
                        "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
                    Objeto fechaTC = HBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null);
                    vencimiento = fechaTC.string("vencimiento");
                    cierre = fechaTC.string("cierre");
                } else {
                    cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                    vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
                }
                item.set("fechaCierre", cierre);
                item.set("fechaVencimiento", vencimiento);
                // item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
                // item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));

                item.set("formaPago", tarjetaCredito.formaPago());
                item.set("idPaquete", tarjetaCredito.idPaquete());
                item.set("esHml", tarjetaCredito.esHML());
                item.set("permitirCambioFormaPago", permitirCambioFormaPago && tarjetaCredito.esTitular());
                item.set("descripcionTarjeta", tarjetaCredito.producto());

                if (futurosTarjetaCreditoPausado != null) {
                    ApiResponse apiResponse = futurosTarjetaCreditoPausado.get(tarjetaCredito.numero()).get();
                    item.set("pausada", !apiResponse.hayError() && !apiResponse.objetos().isEmpty()
                            ? apiResponse.objetos().get(0).string("value").equals("DISABLED")
                            : "");
                }

                respuesta.add("tarjetasCredito", item);
            }

            LocalTime target = LocalTime.now();
            Boolean enHorarioCambioFormaPago = (target.isAfter(LocalTime.parse("06:00:00"))
                    && target.isBefore(LocalTime.parse("21:00:00")));

            Objeto datosExtras = new Objeto();
            datosExtras.set("fueraHorarioCambioFormaPago", !enHorarioCambioFormaPago);
            respuesta.set("datosExtras", datosExtras);

            return respuesta;

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta consolidadaTarjetasDebito(ContextoHB contexto) {
        try {

            if (!ConfigHB.esProduccion() && contexto.idCobis().equals("8649931")) {
                return Respuesta.error();
            }

            // LLAMADAS EN PARALELO
            Map<String, Futuro<ApiResponse>> futurosTarjetaDebitoGetEstado = new HashMap<>();
            Map<String, Futuro<List<Cuenta>>> futurosTarjetaDebitoCuentasAsociadas = new HashMap<>();
            for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                String id = tarjetaDebito.numero();
                Futuro<ApiResponse> futuro = new Futuro<>(
                        () -> TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, id));
                futurosTarjetaDebitoGetEstado.put(id, futuro);

                Futuro<List<Cuenta>> futuroCuentasAsociadas = new Futuro<>(
                        () -> tarjetaDebito.cuentasAsociadasPrincipales());
                futurosTarjetaDebitoCuentasAsociadas.put(id, futuroCuentasAsociadas);
            }
            // FIN LLAMADAS EN PARALELO

            Respuesta respuesta = new Respuesta();
            for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                Objeto item = new Objeto();
                //item.set("id", tarjetaDebito.id());
                item.set("id", tarjetaDebito.idEncriptado());
                item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
                item.set("tipoTarjeta", tarjetaDebito.idTipoTarjeta());
                item.set("limiteCompraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
                item.set("limiteExtraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));
                item.set("virtual", tarjetaDebito.virtual());
                item.set("descripcionTarjeta", tarjetaDebito.producto());

                String estadoTarjeta = "";
                // ApiResponse response = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto,
                // tarjetaDebito.numero());
                ApiResponse response = futurosTarjetaDebitoGetEstado.get(tarjetaDebito.numero()).get();
                if (response.hayError()) {
                    if (response.codigo == 500) {
                        estadoTarjeta = "ERROR_LINK";
                    } else {
                        estadoTarjeta = "NO_DETERMINADO";
                    }
                } else {
                    estadoTarjeta = response.string("estadoTarjeta");
                }

                if (estadoTarjeta.toUpperCase().equals("CERRADA"))
                    estadoTarjeta = "HABILITADA";

                if (estadoTarjeta == null || ((!"ERROR_LINK".equals(estadoTarjeta.toUpperCase()))
                        && (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
                        && !"INACTIVA".equals(estadoTarjeta.toUpperCase())))) {
                    estadoTarjeta = "NO_DETERMINADO";
                }

                item.set("estado", estadoTarjeta);
                if (tarjetaDebito.cuentasAsociadasPrincipales() != null) {
                    item.set("estadoCuentasAsociadas", "ok");
                    // for (Cuenta cuenta : tarjetaDebito.cuentasAsociadasPrincipales()) {
                    for (Cuenta cuenta : futurosTarjetaDebitoCuentasAsociadas.get(tarjetaDebito.numero()).get()) {
                        Objeto subitem = new Objeto();
                        //subitem.set("id", cuenta.id());
                        subitem.set("id", cuenta.idEncriptado());
                        subitem.set("descripcion", cuenta.producto());
                        subitem.set("numero", cuenta.numero());
                        subitem.set("ultimos4digitos", Formateador.ultimos4digitos(cuenta.numero()));
                        subitem.set("simboloMoneda", cuenta.simboloMoneda());
                        item.add("cuentasAsociadas", subitem);
                    }
                } else {
                    item.set("estadoCuentasAsociadas", "error");
                }

                ApiResponse apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
                if (!apiResponseTitularidad.hayError()
                        && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                        && apiResponseTitularidad.objetos("collection1").size() != 0)
                    item.set("pausada", apiResponseTitularidad.objetos("collection1").get(0).get("Pausada"));

                respuesta.add("tarjetasDebito", item);
            }

            Objeto datosExtras = new Objeto();
            datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
            datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebitoActivas().size() == 1);
            respuesta.set("datosExtras", datosExtras);

            return respuesta;

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta consultaAdicionalesPropias(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        boolean mostrarAdicionales = false;
        for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
            if (!tarjetaCredito.esTitular()) {
                Objeto item = new Objeto();
                item.set("id", tarjetaCredito.idEncriptado());
                item.set("tipo", tarjetaCredito.tipo());
                item.set("idTipo", tarjetaCredito.idTipo());
                item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
                item.set("esTitular", tarjetaCredito.esTitular());
                item.set("titularidad", tarjetaCredito.titularidad());
                item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
                item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
                item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
                item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
                item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
                item.set("formaPago", tarjetaCredito.formaPago());
                item.set("idPaquete", tarjetaCredito.idPaquete());
                item.set("nombre", tarjetaCredito.denominacionTarjeta());
                respuesta.add("tarjetasCredito", item);
                mostrarAdicionales = true;
            }
        }

        respuesta.set("mostrarAdicionales", mostrarAdicionales);

        return respuesta;
    }

    public static Respuesta consolidadoMovimientosTarjeta(ContextoHB contexto,
                                                          List<TarjetaCredito> listaTarjetasCredito, String tipoMoneda, boolean topeMovimientos, String fechaDesde,
                                                          String fechaHasta) {

        int topeMov = topeMovimientos ? HBConsolidado.TOPE_MOVIMIENTOS : 0;
        Respuesta respuesta = new Respuesta();
        List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
        TarjetaCredito tarjetaCredito = listaTarjetasCredito.get(0);

        ApiResponse response = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (!response.hayError() && !response.objetos("ultimosMovimientos.tarjetas").isEmpty()) {
            ultimosMovimientosTC.addAll(
                    tomarNMovimientosXTarjeta(contexto, topeMov, response.objetos("ultimosMovimientos.tarjetas"),
                            tarjetaCredito.cuenta(), tipoMoneda, tarjetaCredito.fechaCierre("yyyy-MM-dd")));
        }

        if (tarjetaCredito.esTitular()) {
            ultimosMovimientosTC.addAll(movimientosLiquidados(contexto, tarjetaCredito, tipoMoneda, topeMov));
        }
        if (ultimosMovimientosTC.isEmpty()) {
            return Respuesta.estado("SIN_MOVIMIENTOS");
        }

        respuesta.set("ultimosMovimientosTC", ultimosMovimientosTC);

        return respuesta;
    }

    private static List<Objeto> movimientosLiquidados(ContextoHB contexto, TarjetaCredito tarjetaCredito,
                                                      String tipoMoneda, int topeMovimientos) {
        List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
        LocalDate fechaHoy = LocalDate.now();
        String fechaCierre = tarjetaCredito.fechaCierre("yyyy-MM-dd");
        LocalDate fechaCierreDate;
        if (fechaCierre == null || fechaCierre.isEmpty()) {
            fechaCierre = fechaHoy.toString();
            fechaCierreDate = fechaHoy;
        } else {
            fechaCierreDate = LocalDate.parse(fechaCierre);
            if (fechaCierreDate != null && fechaCierreDate.isAfter(fechaHoy)) {
                fechaCierre = fechaHoy.toString();
                fechaCierreDate = fechaHoy;
            }
        }

        long dias = (30L - diasDespuesAFechaCierre(fechaCierre, fechaHoy));
        String fechaDesde = fechaCierreDate != null ? fechaCierreDate.minusDays(dias).toString() : null;
        ApiResponse response = TarjetaCreditoService.movimientosLiquidados(contexto, tarjetaCredito.cuenta(),
                fechaDesde, fechaCierre);
        if (!response.hayError()) {
            ultimosMovimientosTC.addAll(tomarNMovimientosXTarjetaLiquidados(contexto, tarjetaCredito,
                    response.objetos(), topeMovimientos, tipoMoneda));
        }

        return ultimosMovimientosTC;
    }

    private static Long diasDespuesAFechaCierre(String fechaCierre, LocalDate fechaHoy) {
        return ChronoUnit.DAYS.between(LocalDate.parse(fechaCierre), fechaHoy);
    }

    private static List<Objeto> tomarNMovimientosXTarjetaLiquidados(ContextoHB contexto, TarjetaCredito tarjeta,
                                                                    List<Objeto> movimientos, int tope, String tipoMoneda) {
        List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
        Iterator<Objeto> itera = movimientos.iterator();

        int mov = 1;
        while (itera.hasNext()) {
            Objeto movimiento = itera.next();
            Objeto item = new Objeto();
            String simboloMoneda = "1".equals(movimiento.string("moneda")) ? "$" : "USD";
            String descripcion = movimiento.string("descripcionMovimiento").replaceAll(REGEX_ESPACIOS, " ");
            String descripcionSinCuotas = descripcion.replaceFirst(REGEX_CUOTAS, "");
            String moneda = "1".equals(movimiento.string("moneda")) ? "Pesos" : "Dólares";
            String numeroTarjeta = movimiento.string("tarjeta");

            if (consolidaXDescripcion(descripcion) || consolidaXOperacion(movimiento.string("codigoOperacion"))) {
                item.set("id", numeroTarjeta.replaceAll(REGEX_ESPACIOS, ""));
                item.set("tipo", "VISA");
                item.set("descripcionCorta", "Visa");
                item.set("numeroEnmascarado",
                        "****" + StringUtils.substring(numeroTarjeta, numeroTarjeta.length() - 4));
                item.set("numero", verificaTarjeta(tarjeta.numero(), numeroTarjeta));
                item.set("fecha", Fecha.formato(movimiento.string("fechaMovimiento"), "yyyy-MM-dd", "dd/MM/yyyy"));
                item.set("descripcion", formatearDescripcion(descripcionSinCuotas.toLowerCase()));
                item.set("cuotas", Texto.subCadenaDesdeRegex(descripcion, REGEX_CUOTAS));
                BigDecimal importe = Formateador.importeNegativo(movimiento.bigDecimal("importe"));
                item.set("importeFormateado", Formateador.importe(importe));
                item.set("importe", importe);
                item.set("simboloMoneda", simboloMoneda);
                item.set("moneda", moneda);

                ultimosMovimientosTC = guardarItem(ultimosMovimientosTC, item, simboloMoneda, tipoMoneda);
            }

            mov++;
            if (tope != 0 && mov > tope) {
                break;
            }
        }
        return ultimosMovimientosTC;
    }

    private static List<Objeto> tomarNMovimientosXTarjeta(ContextoHB contexto, int tope, List<Objeto> datosTarjeta,
                                                          String cuenta, String tipoMoneda, String fechaCierre) {
        List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();

        for (Objeto tarjeta : datosTarjeta) {
            int mov = 1;
            Iterator<Objeto> itera = tarjeta.objetos("movimientos").iterator();

            while (itera.hasNext()) {
                Objeto movimiento = itera.next();
                Objeto item = new Objeto();
                String monedaMovimiento = "pesos".equals(movimiento.string("descMoneda")) ? "$"
                        : "dolares".equals(movimiento.string("descMoneda")) ? "USD" : null;
                String descripcion = movimiento.string("establecimiento.nombre").replaceAll(REGEX_ESPACIOS, " ");
                String descripcionSinCuotas = descripcion.replaceFirst(REGEX_CUOTAS, "");
                String numeroTarjeta = tarjeta.string("codigoTarjeta");

                if (consolidaXDescripcion(descripcion)) {
                    item.set("id", numeroTarjeta);
                    item.set("idCuenta", cuenta);
                    item.set("tipo", "VISA");
                    item.set("descripcionCorta", "Visa");
                    item.set("numeroEnmascarado",
                            "****" + StringUtils.substring(numeroTarjeta, numeroTarjeta.length() - 4));
                    item.set("numero", numeroTarjeta);
                    item.set("fecha", validaFechaMovACuotas(movimiento.string("fecha"), fechaCierre, item));
                    item.set("descripcion", formatearDescripcion(descripcionSinCuotas.toLowerCase()));
                    item.set("cuotas", Texto.subCadenaDesdeRegex(descripcion, REGEX_CUOTAS));
                    BigDecimal importe = Formateador.importeNegativo(movimiento.bigDecimal("importe.pesos"));
                    item.set("importeFormateado", Formateador.importe(importe));
                    item.set("importe", importe);
                    if ("dolares".equals(movimiento.string("descMoneda"))) {
                        importe = Formateador.importeNegativo(movimiento.bigDecimal("importe.dolares"));
                        item.set("importeFormateado", Formateador.importe(importe));
                        item.set("importe", importe);
                    }
                    item.set("simboloMoneda", monedaMovimiento);
                    item.set("moneda", StringUtils.capitalize(movimiento.string("descMoneda")));

                    Objeto datosAdicionales = new Objeto();
                    datosAdicionales.set("establecimiento", movimiento.string("establecimiento.codigo"));
                    datosAdicionales.set("ticket", movimiento.string("ticket"));
                    if (tarjeta.string("codigoTarjeta").equals("0000000000000000")) {
                        datosAdicionales.set("esCompra", false);
                    } else {
                        datosAdicionales.set("esCompra", true);
                    }
                    item.set("datosAdicionales", datosAdicionales);

                    ultimosMovimientosTC = guardarItem(ultimosMovimientosTC, item, monedaMovimiento, tipoMoneda);
                }

                mov++;
                if (tope != 0 && mov > tope) {
                    break;
                }
            }
        }

        return ultimosMovimientosTC;
    }

    private static boolean consolidaXDescripcion(String descripcion) {
        return descripcion.contains("SU PAGO") ? false : true;
    }

    private static boolean consolidaXOperacion(String operacion) {
        return StringUtils.contains(HBConsolidado.OPERACION_MOVIMIENTOS_TC, operacion) ? false : true;
    }

    private static String verificaTarjeta(String tarjetaTitular, String tarjetaMov) {
        return StringUtils.contains(tarjetaMov, "000000      0000") ? tarjetaTitular
                : tarjetaMov.replaceAll(REGEX_ESPACIOS, " ");
    }

    private static String validaFechaMovACuotas(String fechaMov, String fechaCierreTarjeta, Objeto item) {
        try {
            Date fechaCierreBase = new SimpleDateFormat("yyyy-MM-dd").parse(fechaCierreTarjeta);
            Date fechaMovBase = new SimpleDateFormat("dd/MM/yyyy").parse(fechaMov);
            if (fechaMovBase != null && fechaCierreBase != null
                    && (fechaMovBase.equals(fechaCierreBase) || fechaMovBase.before(fechaCierreBase))) {
                item.set("fechaModificada", true);
                return Fecha.sumarDias(fechaCierreBase, 1L, "dd/MM/yyyy");
            }
        } catch (ParseException e) {
            return fechaMov;
        }

        return fechaMov;
    }

    private static List<Objeto> guardarItem(List<Objeto> lista, Objeto item, String simboloMoneda, String tipoMoneda) {
        if (conFiltroMoneda(simboloMoneda, tipoMoneda)) {
            lista.add(item);
        } else if (sinFiltroMoneda(tipoMoneda)) {
            lista.add(item);
        }
        return lista;
    }

    private static boolean conFiltroMoneda(String simboloMoneda, String tipoMoneda) {
        return !tipoMoneda.isEmpty() && simboloMoneda.equalsIgnoreCase(tipoMoneda);
    }

    private static String formatearDescripcion(String descripcion) {
        return descripcion.startsWith("www.") ? descripcion : StringUtils.capitalize(descripcion);
    }

    private static boolean sinFiltroMoneda(String tipoMoneda) {
        return tipoMoneda == null || tipoMoneda.isEmpty();
    }

    public static Respuesta resumenCuenta(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
        TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
                : contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
        }

        ApiResponse response = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        Objeto resumen = respuesta.set("resumen");

        Objeto itemResumenCuentaProxima = resumen.set("proximo");

        itemResumenCuentaProxima.set("fechaVencimiento", response.date(
                "resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
        itemResumenCuentaProxima.set("fechaCierre", response
                .date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
        itemResumenCuentaProxima.set("saldoPesosFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.saldo.pesos"), ""));
        itemResumenCuentaProxima.set("saldoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.saldo.dolares"), ""));
        itemResumenCuentaProxima.set("pagoMinimoPesosFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.pagoMinimo.pesos"), ""));
        itemResumenCuentaProxima.set("pagoMinimoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.pagoMinimo.dolares"), ""));

        // TODO: validar si voy a parametria o sigo con datos de visa
        Objeto fechas = null;
        try {
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
                Date fechaCierreProximo = Fecha
                        .stringToDate(response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
                                "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha.stringToDate(
                        response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                                "yyyy-MM-dd", "dd/MM/yyyy", ""),
                        "dd/MM/yyyy");
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                if ((fechaActual.compareTo(fechaCierreProximo) > 0
                        || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    fechas = fechaCierreParametrica(
                            Fecha.stringToDate(
                                    response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
                                            "yyyy-MM-dd", "dd/MM/yyyy", ""),
                                    "dd/MM/yyyy"),
                            tarjetaCredito.grupoCarteraTc());
                    if (fechas != null && !fechas.string("cierre").isEmpty()) {
                        itemResumenCuentaProxima.set("fechaCierre",
                                fechas.date("cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
                        itemResumenCuentaProxima.set("fechaVencimiento",
                                fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        Objeto itemResumenCuentaUltima = resumen.set("ultimo");

        try {

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
                Date fechaCierreProximo = Fecha.stringToDate(fechas.date("cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""),
                        "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha
                        .stringToDate(fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaHoy = dateStringToDate(new Date(), "dd/MM/yyyy");

                if ((fechaHoy.compareTo(fechaCierreProximo) > 0
                        || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    itemResumenCuentaUltima.set("fechaVencimiento",
                            fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
                } else {
                    itemResumenCuentaUltima.set("fechaVencimiento",
                            response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
                                    "yyyy-MM-dd", "dd/MM/yyyy", ""));
                }
            } else {
                Date fechaCierreProximo = Fecha
                        .stringToDate(response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
                                "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha.stringToDate(
                        response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                                "yyyy-MM-dd", "dd/MM/yyyy", ""),
                        "dd/MM/yyyy");

                Date fechaHoy = dateStringToDate(new Date(), "dd/MM/yyyy");

                if ((fechaHoy.compareTo(fechaCierreProximo) > 0
                        || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    itemResumenCuentaUltima.set("fechaVencimiento",
                            response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                                    "yyyy-MM-dd", "dd/MM/yyyy", ""));
                } else {
                    itemResumenCuentaUltima.set("fechaVencimiento",
                            response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
                                    "yyyy-MM-dd", "dd/MM/yyyy", ""));
                }
            }

        } catch (Exception e) {
            itemResumenCuentaUltima.set("fechaVencimiento",
                    response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd",
                            "dd/MM/yyyy", ""));
        }

        itemResumenCuentaUltima.set("fechaCierre", response
                .date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
        itemResumenCuentaUltima.set("saldoPesosFormateado", Formateador
                .importe(response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.pesos"), ""));
        itemResumenCuentaUltima.set("saldoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.dolares"), ""));
        itemResumenCuentaUltima.set("pagoMinimoPesosFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos"), ""));
        itemResumenCuentaUltima.set("pagoMinimoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares"), ""));

        Objeto itemResumenCuentaAnterior = resumen.set("anterior");
        itemResumenCuentaAnterior.set("fechaVencimiento", response.date(
                "resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
        itemResumenCuentaAnterior.set("fechaCierre", response.date(
                "resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
        itemResumenCuentaAnterior.set("saldoPesosFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.saldo.pesos"), ""));
        itemResumenCuentaAnterior.set("saldoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.saldo.dolares"), ""));
        itemResumenCuentaAnterior.set("pagoMinimoPesosFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.pagoMinimo.pesos"), ""));
        itemResumenCuentaAnterior.set("pagoMinimoDolaresFormateado", Formateador.importe(
                response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.pagoMinimo.dolares"), ""));

        Objeto limites = respuesta.set("limites");
        for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.limites")) {
            limites.set("compra",
                    objeto.string("descripcion").equals("compra") ? Formateador.importe(objeto.bigDecimal("total"))
                            : limites.get("compra", ""));
            limites.set("compraCuotas",
                    objeto.string("descripcion").equals("compracuotas")
                            ? Formateador.importe(objeto.bigDecimal("total"))
                            : limites.get("compraCuotas", ""));
            limites.set("compraDisp",
                    objeto.string("descripcion").equals("compradisp") ? Formateador.importe(objeto.bigDecimal("total"))
                            : limites.get("compraDisp", ""));
            limites.set("compraCuotasDisp",
                    objeto.string("descripcion").equals("compracuotasdisp")
                            ? Formateador.importe(objeto.bigDecimal("total"))
                            : limites.get("compraCuotasDisp", ""));

            if (tarjetaCredito.tipo().equalsIgnoreCase("Signature")) {
                limites.set("financiacion",
                        objeto.string("descripcion").equals("financiacion")
                                ? Formateador.importe(objeto.bigDecimal("total"))
                                : limites.get("financiacion", ""));
            } else {
                limites.set("financiacion",
                        objeto.string("descripcion").equals("financiacion")
                                ? Formateador.importe(objeto.bigDecimal("total"))
                                : limites.get("compra", ""));
            }

        }

        respuesta.set("limitesUnificados",
                response.string("resumenCuenta.saldoenCuenta.limitesUnificados").equalsIgnoreCase("S") ? "true"
                        : "false");

        Objeto tasas = respuesta.set("tasas");
        for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.tasas")) {
            tasas.set("tnaPesos",
                    objeto.string("descripcion").equals("anual") ? Formateador.importe(objeto.bigDecimal("pesos"))
                            : tasas.get("tnaPesos", ""));
            tasas.set("tnaDolares",
                    objeto.string("descripcion").equals("anual") ? Formateador.importe(objeto.bigDecimal("dolares"))
                            : tasas.get("tnaDolares", ""));
            tasas.set("teaPesos",
                    objeto.string("descripcion").equals("mensual") ? Formateador.importe(objeto.bigDecimal("pesos"))
                            : tasas.get("teaPesos", ""));
            tasas.set("teaDolares",
                    objeto.string("descripcion").equals("mensual") ? Formateador.importe(objeto.bigDecimal("dolares"))
                            : tasas.get("teaDolares", ""));
        }
        Objeto mora = new Objeto();
        Boolean onboardingMostrado = true;
        Boolean enMora = false;
        try {
            // verificar mora
            String vtoUltima = response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
                    "yyyy-MM-dd", "dd/MM/yyyy", "");
            Date fechaVencimiento = Fecha.stringToDate(vtoUltima, "dd/MM/yyyy");
            Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
            if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                Respuesta respuestaMora = HBTarjetas.estadoDeuda(contexto);
                if (respuestaMora.string("datos.estadoDeuda").equals("CON_DEUDA")) {
                    if (respuestaMora.string("datos.tipoMora").equals("ONE")) {
                        onboardingMostrado = false;
                        enMora = true;
                        String formaPago = tarjetaCredito.formaPago().equalsIgnoreCase("Efectivo") ? "EFECTIVO"
                                : "AUTODEBITO";
                        String tipoMora = respuestaMora.string("datos.tipoMora").equals("ONE")
                                || respuestaMora.string("datos.tipoMora").equals("MT") ? "T" : "";
                        mora.set("estadoTarjeta", ("EN_MORA" + tipoMora + "_" + formaPago));
                        ApiResponse responseMoraDetalle = RestMora.getProductosEnMoraDetallesCache(contexto,
                                respuestaMora.string("datos.cta_id"));
                        if (!responseMoraDetalle.hayError()) {
                            mora.set("saldoCubrirMinimo", Formateador
                                    .importe(responseMoraDetalle.objetos().get(0).bigDecimal("Deuda Vencida")));
                            // mora.set("saldoCubrirTotal", Formateador.importe(xx.bigDecimal("total"));
                            onboardingMostrado = !responseMoraDetalle.objetos().get(0).string("PromesaVigente")
                                    .isEmpty() || !responseMoraDetalle.objetos().get(0).string("IndicaYPAG").isEmpty();
                        } else {
                            mora.set("estadoTarjeta", "ERROR_MORA");
                        }
                    }
                } else {
                    mora.set("estadoTarjeta", "SIN_MORA");
                }
                Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
                mora.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

                try {
                    if (!muestreoOnboarding && onboardingMostrado && enMora) {
                        contexto.parametros.set("nemonico", "ONBOARDING");
                        Util.contador(contexto);
                    }
                } catch (Exception e) {
                }

            }
            respuesta.set("mora", mora);
        } catch (Exception e) {
        }

        return respuesta;
    }

    public static Respuesta autorizaciones(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
                : contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
        }
        List<TarjetaCredito> tarjetasComparar = contexto.tarjetasCredito();

        ApiResponse response = TarjetaCreditoService.autorizaciones(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (response.hayError()) {
            if (response.string("codigo").equals("404")) {
                return Respuesta.estado("SIN_AUTORIZACIONES");
            }
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        Objeto autorizaciones = respuesta.set("autorizaciones");

        autorizaciones.set("totalPesos", Formateador.importe(response.bigDecimal("autorizaciones.totales.pesos")));
        autorizaciones.set("totalDolares", Formateador.importe(response.bigDecimal("autorizaciones.totales.dolares")));

        for (Objeto tarj : response.objetos("autorizaciones.tarjeta")) {
            Objeto tarjR = new Objeto();

            boolean esTitular = false;
            String idTipo = "";
            String tipo = "";

            for (TarjetaCredito tarjetaC : tarjetasComparar) {
                if (Formateador.ultimos4digitos(tarjetaC.numero())
                        .equalsIgnoreCase(Formateador.ultimos4digitos(tarj.string("codigoTarjeta")))) {
                    esTitular = tarjetaC.esTitular();
                    idTipo = tarjetaC.idTipo();
                    tipo = tarjetaC.tipo();
                }
            }
            tarjR.set("idTipo", idTipo);
            tarjR.set("tipo", tipo);
            tarjR.set("esTitular", esTitular);
            tarjR.set("ultimos4digitos", Formateador.ultimos4digitos(tarj.string("codigoTarjeta")));
            tarjR.set("proveedor", "VISA");

            for (Objeto objeto : tarj.objetos("autorizaciones")) {
                Objeto subItem = new Objeto();
                // subItem.set("fecha", objeto.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                subItem.set("fecha", objeto.string("fecha"));
                subItem.set("establecimiento", objeto.string("establecimiento.nombre"));
                subItem.set("tipoMovimiento", objeto.string("tipo"));
                subItem.set("tipoMovimientoDescripcion", objeto.string("descripcion"));
                subItem.set("importeFormateado", Formateador.importe(objeto.bigDecimal("importe")));
                subItem.set("idMoneda", "pesos".equals(objeto.string("descMoneda")) ? "80"
                        : "dolares".equals(objeto.string("descMoneda")) ? "2" : null);
                subItem.set("simboloMoneda", "pesos".equals(objeto.string("descMoneda")) ? "$"
                        : "dolares".equals(objeto.string("descMoneda")) ? "USD" : null);
                tarjR.add("movimientos", subItem);
            }

            autorizaciones.add("tarjetas", tarjR);
        }

        return respuesta;
    }

    public static Respuesta cuotasPendientes(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
        BigDecimal proximoResumen = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalTitular = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalAdicional = new BigDecimal(BigInteger.ZERO);

        TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
                : contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
        }

        List<TarjetaCredito> tarjetasComparar = contexto.tarjetasCredito();

        ApiResponse response = TarjetaCreditoService.cuotasPendientes(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (response.hayError()) {
            if (response.string("codigo").equals("404")) {
                return Respuesta.estado("SIN_CUOTAS_PENDIENTES");
            }
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        Objeto cuotasPendientes = respuesta.set("cuotasPendientes");

        cuotasPendientes.set("totalPesos", Formateador.importe(response.bigDecimal("cuotasPendientes.totales.pesos")));
        cuotasPendientes.set("totalDolares",
                Formateador.importe(response.bigDecimal("cuotasPendientes.totales.dolares")));
        cuotasPendientes.set("apellido", response.string("datos.apellido"));
        cuotasPendientes.set("nombre", response.string("datos.nombre"));

        for (Objeto tarj : response.objetos("cuotasPendientes.tarjeta")) {

            Objeto tarjR = new Objeto();

            boolean esTitular = false;
            String idTipo = "";
            String tipo = "";

            for (TarjetaCredito tarjetaC : tarjetasComparar) {
                if (Formateador.ultimos4digitos(tarjetaC.numero())
                        .equalsIgnoreCase(Formateador.ultimos4digitos(tarj.string("codigoTarjeta")))) {
                    esTitular = tarjetaC.esTitular();
                    idTipo = tarjetaC.idTipo();
                    tipo = tarjetaC.tipo();
                }
            }
            tarjR.set("idTipo", idTipo);
            tarjR.set("tipo", tipo);
            tarjR.set("esTitular", esTitular);
            tarjR.set("ultimos4digitos", Formateador.ultimos4digitos(tarj.string("codigoTarjeta")));
            tarjR.set("proveedor", "VISA");

            for (Objeto objeto : tarj.objetos("cuota")) {
                Objeto subItem = new Objeto();
                subItem.set("fecha", objeto.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                subItem.set("cuotaActual", objeto.integer("cuotas") - objeto.integer("cantCuotasPendientes") + 1);
                subItem.set("cuotaMaxima", objeto.integer("cuotas"));
                Integer cuotasRestantes = objeto.integer("cantCuotasPendientes");
                subItem.set("cuotasPendientes", cuotasRestantes);
                subItem.set("importeFormateado", Formateador.importe(objeto.bigDecimal("importe")));
                BigDecimal importeCuota = objeto.bigDecimal("importe").divide(new BigDecimal(cuotasRestantes), 2,
                        RoundingMode.HALF_EVEN);
                subItem.set("importeCuotaFormateado", Formateador.importe(importeCuota));
                subItem.set("idMoneda", "pesos".equals(objeto.string("descMoneda")) ? "80"
                        : "dolares".equals(objeto.string("descMoneda")) ? "2" : null);
                subItem.set("simboloMoneda", "pesos".equals(objeto.string("descMoneda")) ? "$"
                        : "dolares".equals(objeto.string("descMoneda")) ? "USD" : null);

                subItem.set("descripcion", objeto.string("establecimiento.nombre"));

                subItem.set("importeCuotaFormateado", Formateador.importe(objeto.bigDecimal("importe")
                        .divide(objeto.bigDecimal("cantCuotasPendientes"), 2, RoundingMode.HALF_UP)));

                proximoResumen = proximoResumen.add(objeto.bigDecimal("importe")
                        .divide(objeto.bigDecimal("cantCuotasPendientes"), 2, RoundingMode.HALF_UP));

                if (tarjR.bool("esTitular")) {
                    totalTitular = totalTitular.add(objeto.bigDecimal("importe"));
                } else {
                    totalAdicional = totalAdicional.add(objeto.bigDecimal("importe"));
                }

                tarjR.add("cuotas", subItem);
            }
            cuotasPendientes.set("proximoResumen", Formateador.importe(proximoResumen));
            cuotasPendientes.set("totalTitular", Formateador.importe(totalTitular));
            cuotasPendientes.set("totalAdicional", Formateador.importe(totalAdicional));
            cuotasPendientes.add("tarjetas", tarjR);
        }

        return respuesta;
    }

    public static Respuesta movimientosTC(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            tarjetaCredito = contexto.tarjetaCreditoTitular();
            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }
        }

        ApiResponse response = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());

        if (response.hayError() && response.codigo != 404) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();

        if (contexto.tarjetaCreditoTitular() != null) {
            TarjetaCredito tarjeta = contexto.tarjetaCreditoTitular();
            Objeto item = new Objeto();
            item.set("id", "0");
            item.set("proveedor", "VISA");
            item.set("idTipo", tarjeta.idTipo());
            item.set("tipo", tarjeta.tipo());
            item.set("ultimos4digitos", tarjeta.ultimos4digitos());
            item.set("esTitular", tarjeta.esTitular());
            item.set("esTitularMasAdicionales", true);
            respuesta.add("tarjetas", item);
        }

        Set<String> ultimos4digitos = new HashSet<>();
        for (TarjetaCredito tarjeta : contexto.tarjetasCredito()) {
            Objeto item = new Objeto();
            item.set("id", tarjeta.idEncriptado());
            item.set("proveedor", "VISA");
            item.set("idTipo", tarjeta.idTipo());
            item.set("tipo", tarjeta.tipo());
            item.set("ultimos4digitos", tarjeta.ultimos4digitos());
            item.set("esTitular", tarjeta.esTitular());
            item.set("esAdicional", !tarjeta.esTitular());
            item.set("esTitularMasAdicionales", false);
            respuesta.add("tarjetas", item);
            ultimos4digitos.add(tarjeta.ultimos4digitos());
        }

        Boolean esTitularMasAdicionales = idTarjetaCredito.equals("0");

        Objeto cabecera = new Objeto();
        cabecera.set("id", !esTitularMasAdicionales ? tarjetaCredito.id() : "0");
        cabecera.set("cuenta", tarjetaCredito.cuenta());
        cabecera.set("tipoTarjeta", "VISA");
        cabecera.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
        cabecera.set("esTitular", tarjetaCredito.esTitular());
        cabecera.set("esTitularMasAdicionales", esTitularMasAdicionales);
        cabecera.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
        cabecera.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
        cabecera.set("formaPago", tarjetaCredito.formaPago());
        cabecera.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
        cabecera.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));

        if (response.codigo == 404) {
            cabecera.set("pesosFormateado", Formateador.importe(new BigDecimal(0)));
            cabecera.set("dolaresFormateado", Formateador.importe(new BigDecimal(0)));
        } else if (response != null && response.objetos("ultimosMovimientos").isEmpty()) {
            cabecera.set("pesosFormateado",
                    Formateador.importe(response.bigDecimal("totalesUltimosMovimientos.pesos")));
            cabecera.set("dolaresFormateado",
                    Formateador.importe(response.bigDecimal("totalesUltimosMovimientos.dolares")));
        } else {
            if (esTitularMasAdicionales) {
                cabecera.set("pesosFormateado",
                        Formateador.importe(response.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.pesos")));
                cabecera.set("dolaresFormateado", Formateador
                        .importe(response.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.dolares")));
            } else {
                for (Objeto subitem : response.objetos("ultimosMovimientos.tarjetas")) {
                    if (!idTarjetaCredito.equals("0") && idTarjetaCredito.equals(subitem.string("codigoTarjeta"))) {
                        cabecera.set("pesosFormateado", Formateador.importe(subitem.bigDecimal("pesos")));
                        cabecera.set("dolaresFormateado", Formateador.importe(subitem.bigDecimal("dolares")));
                    }
                }
            }
        }

        TarjetaCredito tarjetaCreditoTitularAux = contexto.tarjetaCreditoTitular();
        String vencimientoAux = "";
        String cierreAux = "";
        if (tarjetaCreditoTitularAux != null) {
            ApiResponse responseResumen = TarjetaCreditoService.resumenCuenta(contexto,
                    tarjetaCreditoTitularAux.cuenta(), tarjetaCreditoTitularAux.numero());
            vencimientoAux = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                    "yyyy-MM-dd", "dd/MM", "***");
            cierreAux = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
                    "yyyy-MM-dd", "dd/MM", "***");
        }
        cabecera.set("vencimientoResumenFormateado", vencimientoAux);
        cabecera.set("cierreResumenFormateado", cierreAux);
        respuesta.set("cabecera", cabecera);

        if ((response != null && response.objetos("ultimosMovimientos").isEmpty()) || response.codigo == 404
                || response.objetos("ultimosMovimientos.tarjetas").isEmpty()) {
            respuesta.set("movimientos", new ArrayList<Objeto>());
        } else {
            Objeto datos = response.objeto("ultimosMovimientos.datos");
            for (Objeto tarjeta : response.objetos("ultimosMovimientos.tarjetas")) {

                if (tarjeta.objetos("movimientos").isEmpty()) {
                    if (!ultimos4digitos.contains(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")))) {
                        ultimos4digitos.add(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
                        Objeto itemAux = new Objeto();
                        itemAux.set("id", tarjeta.string("codigoTarjeta"));
                        itemAux.set("proveedor", "VISA");
                        itemAux.set("idTipo", "");
                        itemAux.set("tipo", "");
                        itemAux.set("ultimos4digitos", Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
                        itemAux.set("esTitular", false);
                        itemAux.set("esTitularMasAdicionales", false);
                        respuesta.add("tarjetas", itemAux);
                    }
                }

                for (Objeto movimiento : tarjeta.objetos("movimientos")) {
                    String establecimiento = movimiento.string("establecimiento.codigo");
                    String descripcion = movimiento.string("establecimiento.nombre").replaceAll(REGEX_ESPACIOS, " ");
                    String descripcionSinCuotas = descripcion.replaceFirst(REGEX_CUOTAS, "");
                    Objeto item = new Objeto();
                    item.set("fecha", movimiento.string("fecha"));
                    item.set("establecimiento", establecimiento);
                    item.set("descripcion", formatearDescripcion(descripcionSinCuotas));
                    item.set("cuotas", Texto.subCadenaDesdeRegex(descripcion, REGEX_CUOTAS));
                    item.set("ticket", movimiento.string("ticket"));

                    if (!ultimos4digitos.contains(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")))) {
                        ultimos4digitos.add(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
                        Objeto itemAux = new Objeto();
                        itemAux.set("id", tarjeta.string("codigoTarjeta"));
                        itemAux.set("proveedor", "VISA");
                        itemAux.set("idTipo", "");
                        itemAux.set("tipo", "");
                        itemAux.set("ultimos4digitos", Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
                        itemAux.set("esTitular", false);
                        itemAux.set("esTitularMasAdicionales", false);
                        item.set("esAdicional", false);
                        respuesta.add("tarjetas", itemAux);
                    }

                    item.set("id", tarjeta.string("codigoTarjeta")); // emm
                    item.set("esTitular", contexto.tarjetaCreditoTitular() != null
                            && contexto.tarjetaCreditoTitular().numero().equals(tarjeta.string("codigoTarjeta")));
                    item.set("nombreCompleto", datos.string("nombre") + " " + datos.string("apellido"));
                    item.set("ultimos4digitos", Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
                    item.set("esCompra", !tarjeta.string("codigoTarjeta").equals("0000000000000000"));
                    item.set("simboloMoneda", "pesos".equals(movimiento.string("descMoneda")) ? "$"
                            : "dolares".equals(movimiento.string("descMoneda")) ? "USD" : null);
                    item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("importe.pesos")));
                    item.set("importe", movimiento.bigDecimal("importe.pesos"));
                    if ("dolares".equals(movimiento.string("descMoneda"))) {
                        item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("importe.dolares")));
                        item.set("importe", movimiento.bigDecimal("importe.dolares"));
                    }
                    item.set("orden", Long.MAX_VALUE - movimiento.date("fecha", "dd/MM/yyyy").getTime());

                    if (idTarjetaCredito.equals("0") || idTarjetaCredito.equals(tarjeta.string("codigoTarjeta"))) {
                        respuesta.add("movimientos", item);
                    }
                }
            }
        }

        respuesta.objeto("movimientos").ordenar("orden");
        return respuesta.ordenar("estado", "cabecera", "tarjetas", "movimientos");
    }

    public static Respuesta detalleMovimientoComercio(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEstablecimiento = contexto.parametros.string("idEstablecimiento");

        // TODO REMOVER desde linea 638 a 652
        // Implementacion hardcore parcial hasta que la info de establecimientos este
        // disponible sin prisma
        ApiResponse response = null;
        if ("0".equals(idTarjetaCredito)) {
            idTarjetaCredito = "4304960036889822";
            idCuenta = "0522993383";
            if (filtrarEstablecimiento(idEstablecimiento)) {
                return Respuesta.estado("COMERCIO_NO_DISPONIBLE");
            }

            response = TarjetaCreditoService.detalleMovimientoComercio(contexto, idCuenta, idTarjetaCredito,
                    idEstablecimiento);
            if (response.hayError()) {
                return Respuesta.error();
            }
        } else {

            if (Objeto.anyEmpty(idTarjetaCredito, idCuenta, idEstablecimiento)) {
                return Respuesta.parametrosIncorrectos();
            }

            TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }

            String cuenta = tarjetaCredito.cuenta();
            if (tarjetaCredito == null || !cuenta.equalsIgnoreCase(idCuenta)) {
                return Respuesta.estado("CUENTA_NO_EXISTE");
            }

            if (filtrarEstablecimiento(idEstablecimiento)) {
                return Respuesta.estado("COMERCIO_NO_DISPONIBLE");
            }

            response = TarjetaCreditoService.detalleMovimientoComercio(contexto, tarjetaCredito.cuenta(),
                    tarjetaCredito.numero(), idEstablecimiento);
            if (response.hayError()) {
                return Respuesta.error();
            }
        }

        Respuesta respuesta = new Respuesta();
        Objeto comercio = response.objetos().get(0);
        if (comercio != null) {
            Objeto item = new Objeto();
            item.set("rubro", comercio.string("rubro"));
            String domicilio = comercio.string("domicilio").replaceAll(REGEX_ESPACIOS, " ");
            item.set("domicilio", domicilio);
            item.set("codigoPostal", comercio.string("codigoPostal"));
            item.set("localidad", comercio.string("localidad"));
            item.set("provincia", comercio.string("provincia"));
            item.set("telefono", comercio.string("telefono"));
            item.set("nombre", comercio.string("nombre"));
            item.set("cuit", comercio.string("cuit"));
            item.set("googleMapsDireccion", domicilio + "," + comercio.string("codigoPostal") + ","
                    + comercio.string("provincia") + "," + comercio.string("nombre"));
            return respuesta.set("comercio", item);
        }

        return Respuesta.estado("NO_EXISTE_COMERCIO");
    }

    /* ========== transaccional ========== */
    public static Respuesta infoActualizada(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        BigDecimal montoTotalDolares = null;
        BigDecimal montoTotalPesos = null;

        ApiResponse responseMovimientos = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (!responseMovimientos.hayError()) {
            montoTotalDolares = responseMovimientos.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.dolares");
            montoTotalPesos = responseMovimientos.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.pesos");
            if (montoTotalDolares == null)
                montoTotalDolares = new BigDecimal(0);
            if (montoTotalPesos == null)
                montoTotalPesos = new BigDecimal(0);
        }
        String vencimiento;
        String cierre;
        ApiResponse responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());

        if (responseResumen.hayError()) {
            return Respuesta.error();
        }

        if (responseResumen.codigo == 204) {
            return Respuesta.estado("SIN_RESUMEN");
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
                "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
            Objeto fechaTC = HBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null);
            vencimiento = fechaTC.string("vencimiento");
            cierre = fechaTC.string("cierre");
        } else {
            vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                    "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierre = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                    "dd/MM/yyyy", "***");
        }

        String totalDolaresFormateado = Formateador.importe(montoTotalDolares);
        String totalPesosFormateado = Formateador.importe(montoTotalPesos);
        if (montoTotalDolares == null) {
            totalDolaresFormateado = "***";
        }
        if (montoTotalPesos == null) {
            totalPesosFormateado = "***";
        }

        Respuesta respuesta = new Respuesta(); // si es 200 mandar un cero en consumos.

        if (!respuesta.hayError()) {
            if (totalPesosFormateado == null || totalPesosFormateado.isEmpty()) {
                totalPesosFormateado = "0,00";
            }
            if (totalDolaresFormateado == null || totalDolaresFormateado.isEmpty()) {
                totalDolaresFormateado = "0,00";
            }
        }
        respuesta.set("totalPesos", totalPesosFormateado); // emm-20190417
        respuesta.set("totalDolares", totalDolaresFormateado); // emm-20190417
        respuesta.set("cierre", cierre);
        respuesta.set("vencimiento", vencimiento);

        // EMM. Este comentario no me lo borren. Es para las tareas programadas
        /*
         * try { Respuesta consultaPagos =
         * consultarPagosProgramadosTarjetaCredito(contexto); if
         * (!consultaPagos.hayError()) { respuesta.set("mostrarAvisoPagoProgramado",
         * consultaPagos.bool("mostrarAvisoPagoProgramado", false));
         * respuesta.set("alertaHoy", consultaPagos.bool("alertaHoy", false));
         * respuesta.set("horaAlertaHoy", consultaPagos.string("horaAlertaHoy"));
         * respuesta.set("alertaManana", consultaPagos.bool("alertaManana", false));
         * respuesta.set("horaAlertaManana", consultaPagos.string("horaAlertaManana"));
         * if (!consultaPagos.objetos("tareas").isEmpty()) {
         * respuesta.set("fechaProgramada",
         * consultaPagos.objetos("tareas").get(0).string("fechaProgramada"));
         * respuesta.set("horaProgramada",
         * consultaPagos.objetos("tareas").get(0).string("fechaProgramadaHora")); }
         *
         * } } catch (Exception e) { }
         */

        return respuesta;
    }

    private static Date dateStringToDate(Date fecha, String formato) throws ParseException {
        String fechaActualString = new SimpleDateFormat(formato).format(fecha);
        return new SimpleDateFormat(formato).parse(fechaActualString);
    }

    protected static Boolean validarResumenCuenta(ApiResponse responseResumen) {

        BigDecimal montoPagoMinimoPesos = responseResumen
                .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos");
        BigDecimal montoPagoMinimoDolares = responseResumen
                .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares");

        if (montoPagoMinimoPesos == null || montoPagoMinimoDolares == null) {
            return false;
        }

        return true;
    }

    public static Respuesta datosParaPagar(ContextoHB contexto) {

        // https://api-tarjetascredito-microservicios-desa.appd.bh.com.ar/v1/tarjetascredito/4304970008692328

		ApiResponse responseCotizacion = InversionesService.inversionesGetCotizaciones(contexto);

        Respuesta respuesta = new Respuesta();
        BigDecimal dolarVenta = Cotizacion.dolarVenta(contexto, responseCotizacion);
        BigDecimal dolarCompra = Cotizacion.dolarCompra(contexto, responseCotizacion); // emm-20201229
        if (dolarVenta == null) {
            respuesta.setEstado("NO EXISTE MONEDA");
            return respuesta;
        }
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        BigDecimal montoPagoTotalPesos = new BigDecimal(0);
        BigDecimal montoPagoTotalDolares = new BigDecimal(0);
        BigDecimal montoPagoMinimoPesos = new BigDecimal(0);
        BigDecimal montoPagoMinimoDolares = new BigDecimal(0);
        BigDecimal sumaTotalDolaresMasPesosEnPesos = new BigDecimal(0);
        BigDecimal sumaTotalDolaresMasPesosEnDolar = new BigDecimal(0);
        boolean esPagableUS = false;
        String vencimiento = "";
        String cierre = "";

        /* Primero tomo los datos de la consulta de tarjeta */

        ApiResponse responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return Respuesta.error();
        }
        montoPagoTotalPesos = new BigDecimal(0);
        montoPagoTotalDolares = new BigDecimal(0);
        montoPagoMinimoPesos = new BigDecimal(0);
        vencimiento = "";
        cierre = "";
        for (Objeto item : responseTarjeta.objetos()) {
            montoPagoTotalPesos = item.bigDecimal("debitosEnCursoPesos");
            montoPagoTotalDolares = item.bigDecimal("debitosEnCursoDolares");
            montoPagoMinimoPesos = item.bigDecimal("pagoMinimoActual");
            vencimiento = item.date("fechaVencActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierre = item.date("cierreActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
        }
        esPagableUS = tarjetaCredito.esPagableUS();

        if (montoPagoTotalPesos == null)
            montoPagoTotalPesos = new BigDecimal(0);
        if (montoPagoTotalDolares == null)
            montoPagoTotalDolares = new BigDecimal(0);
        if (montoPagoMinimoPesos == null)
            montoPagoMinimoPesos = new BigDecimal(0);

        montoPagoMinimoDolares = montoPagoMinimoPesos.divide(dolarCompra, 2, RoundingMode.HALF_UP);
        sumaTotalDolaresMasPesosEnPesos = montoPagoTotalPesos.add(montoPagoTotalDolares
                .multiply(montoPagoTotalDolares.compareTo(new BigDecimal(0)) >= 0 ? dolarVenta : dolarCompra));
        sumaTotalDolaresMasPesosEnDolar = montoPagoTotalPesos
                .divide(montoPagoTotalPesos.compareTo(new BigDecimal(0)) >= 0 ? dolarCompra : dolarVenta, 2,
                        RoundingMode.HALF_UP)
                .add(montoPagoTotalDolares);

        sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.setScale(2, RoundingMode.HALF_UP);
        sumaTotalDolaresMasPesosEnDolar = sumaTotalDolaresMasPesosEnDolar.setScale(2, RoundingMode.HALF_UP);

        /*
         * Me fijo, si funciona correctamente el servicio de resumenCuenta, tomo los
         * datos de ahí
         */
        ApiResponse responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return Respuesta.error();
        }

        if (validarResumenCuenta(responseResumen)) {
            montoPagoTotalPesos = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.pesos");
            montoPagoTotalDolares = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.dolares");
            montoPagoMinimoPesos = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos");
            montoPagoMinimoDolares = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares");

            if (montoPagoTotalPesos == null)
                montoPagoTotalPesos = new BigDecimal(0);
            if (montoPagoTotalDolares == null)
                montoPagoTotalDolares = new BigDecimal(0);
            if (montoPagoMinimoPesos == null)
                montoPagoMinimoPesos = new BigDecimal(0);
            if (montoPagoMinimoDolares == null)
                montoPagoMinimoDolares = new BigDecimal(0);

            sumaTotalDolaresMasPesosEnPesos = montoPagoTotalPesos.add(montoPagoTotalDolares
                    .multiply(montoPagoTotalDolares.compareTo(new BigDecimal(0)) >= 0 ? dolarVenta : dolarCompra));
            sumaTotalDolaresMasPesosEnDolar = montoPagoTotalPesos
                    .divide(montoPagoTotalPesos.compareTo(new BigDecimal(0)) >= 0 ? dolarCompra : dolarVenta, 2,
                            RoundingMode.HALF_UP)
                    .add(montoPagoTotalDolares);

            sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.setScale(2, RoundingMode.HALF_UP);
            sumaTotalDolaresMasPesosEnDolar = sumaTotalDolaresMasPesosEnDolar.setScale(2, RoundingMode.HALF_UP);

            // vencimiento =
            // responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
            // "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierre = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                    "dd/MM/yyyy", "***");
        }

        Objeto datos = new Objeto();
        datos.set("cierre", cierre);
        datos.set("vencimiento", vencimiento);
        datos.set("totalPesos", montoPagoTotalPesos);
        datos.set("totalPesosFormateado", Formateador.importe(montoPagoTotalPesos));
        datos.set("totalDolares", montoPagoTotalDolares);
        datos.set("totalDolaresFormateado", Formateador.importe(montoPagoTotalDolares));
        datos.set("minimoPesos", montoPagoMinimoPesos);
        datos.set("minimoPesosFormateado", Formateador.importe(montoPagoMinimoPesos));
        datos.set("minimoDolares", montoPagoMinimoDolares);
        datos.set("minimoDolaresFormateado", Formateador.importe(montoPagoMinimoDolares));
        datos.set("sumaTotalDolaresMasPesosEnPesos", sumaTotalDolaresMasPesosEnPesos); // totalDolaresMasPesos
        datos.set("sumaTotalDolaresMasPesosEnPesosFormateado", Formateador.importe(sumaTotalDolaresMasPesosEnPesos));
        datos.set("sumaTotalDolaresMasPesosEnDolar", sumaTotalDolaresMasPesosEnDolar); // totalDolaresMasPesos
        datos.set("sumaTotalDolaresMasPesosEnDolarFormateado", Formateador.importe(sumaTotalDolaresMasPesosEnDolar));
        datos.set("cotizacionActual", dolarVenta);
        datos.set("cotizacionActualFormateado", Formateador.importe(dolarVenta));
        datos.set("cotizacionActualCompra", dolarCompra); // emm - 20201229
        datos.set("cotizacionActualCompraFormateado", Formateador.importe(dolarCompra)); // emm - 20201229
        datos.set("esPagableUS", esPagableUS);

        datos.set("totalDolaresEnPesos", montoPagoTotalDolares
                .multiply(montoPagoTotalDolares.compareTo(new BigDecimal(0)) >= 0 ? dolarVenta : dolarCompra));
        datos.set("totalDolaresEnPesosFormateado", Formateador.importe(montoPagoTotalDolares
                .multiply(montoPagoTotalDolares.compareTo(new BigDecimal(0)) >= 0 ? dolarVenta : dolarCompra)));
        datos.set("totalPesosEnDolar",
                montoPagoTotalPesos.divide(
                        (montoPagoTotalPesos.compareTo(new BigDecimal(0)) >= 0 ? dolarCompra : dolarVenta), 2,
                        RoundingMode.HALF_UP));
        datos.set("totalPesosEnDolarFormateado",
                Formateador.importe(montoPagoTotalPesos.divide(
                        (montoPagoTotalPesos.compareTo(new BigDecimal(0)) >= 0 ? dolarCompra : dolarVenta), 2,
                        RoundingMode.HALF_UP)));

        boolean mostrarOpcionTotalPesos = sumaTotalDolaresMasPesosEnPesos.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotalDolares = sumaTotalDolaresMasPesosEnDolar.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotalPesosMasDolares = (montoPagoTotalPesos.compareTo(BigDecimal.ZERO) >= 0
                && montoPagoTotalDolares.compareTo(BigDecimal.ZERO) >= 0);
        boolean mostrarOpcionMinimo = montoPagoMinimoPesos.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotal = (mostrarOpcionTotalPesos || mostrarOpcionTotalDolares);

        datos.set("mostrarOpcionTotalPesos", mostrarOpcionTotalPesos);
        datos.set("mostrarOpcionTotalDolares", mostrarOpcionTotalDolares);
        datos.set("mostrarOpcionMinimo", mostrarOpcionMinimo);
        datos.set("mostrarOpcionTotal", mostrarOpcionTotal);
        datos.set("mostrarOpcionTotalPesosMasDolares", mostrarOpcionTotalPesosMasDolares);

        ApiResponse response = ProductosService.productos(contexto);
        if (response.hayError()) {
            return Respuesta.error();
        }
        Boolean tieneCuentasPesos = false;
        Boolean tieneCuentasDolares = false;

        for (Cuenta cuenta : contexto.cuentas()) {
            if (cuenta.esPesos()) {
                tieneCuentasPesos = true;
            }

            if (cuenta.esDolares()) {
                tieneCuentasDolares = true;
            }
        }
        datos.set("tieneCuentasPesos", tieneCuentasPesos);
        datos.set("tieneCuentasDolares", tieneCuentasDolares);
        respuesta.set("datos", datos);

        return respuesta;
    }

    public static Respuesta obtenerFechasTC(ContextoHB contexto) {

        Respuesta respuesta = new Respuesta();

        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        String vencimientoActual = "";
        String cierreActual = "";
        String vencimientoProximo = "";
        String cierreProximo = "";
        String vencimientoAnterior = "";
        String cierreAnterior = "";

        /* Primero tomo los datos de la consulta de tarjeta */

        ApiResponse responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return Respuesta.error();
        }
        for (Objeto item : responseTarjeta.objetos()) {
            vencimientoActual = item.date("fechaVencActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreActual = item.date("cierreActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            vencimientoProximo = item.date("proxVenc", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreProximo = item.date("proxCierre", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            vencimientoAnterior = item.date("vencAnterior", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreAnterior = item.date("cierreAnterior", "yyyy-MM-dd", "dd/MM/yyyy", "***");

        }

        Objeto datos = new Objeto();
        datos.set("vencimientoActual", vencimientoActual);
        datos.set("cierreActual", cierreActual);
        datos.set("vencimientoProximo", vencimientoProximo);
        datos.set("cierreProximo", cierreProximo);
        datos.set("vencimientoAnterior", vencimientoAnterior);
        datos.set("cierreAnterior", cierreAnterior);

        respuesta.set("datos", datos);

        return respuesta;
    }

    public static Respuesta pagarTarjeta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        if (contexto.idCobis() == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }
        if (contexto.persona().esMenor()) {
            return Respuesta.estado("MENOR_NO_AUTORIZADO");
        }

        if (importe.compareTo(BigDecimal.ZERO) < 0) {
            return Respuesta.estado("PAGO_MENOR_A_CERO");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.error();
        }
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return Respuesta.error();
        }
        if (!tarjetaCredito.esTitular()) {
            return Respuesta.estado("PAGO_TARJETA_ADICIONAL_NO_PERMITIDO");
        }

        Respuesta respuesta;
        try {
            respuesta = TarjetaCreditoService.pagarTarjetaCredito(contexto, cuenta, tarjetaCredito, importe);
            return respuesta;
        } catch (Exception e) {
            throw G.runtimeException(e);
        }
    }
    // emm-20190423-hasta

    public static Respuesta programarPagoTarjeta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");

        if (contexto.persona().esMenor()) {
            return Respuesta.estado("MENOR_NO_AUTORIZADO");
        }

        if (importe.compareTo(BigDecimal.ZERO) < 0) {
            return Respuesta.estado("PAGO_MENOR_A_CERO");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.error();
        }
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return Respuesta.error();
        }
        if (!tarjetaCredito.esTitular()) {
            return Respuesta.estado("PAGO_TARJETA_ADICIONAL_NO_PERMITIDO");
        }
        ApiResponse response = TarjetaCreditoService.programarPagoTarjetaCredito(contexto, cuenta, tarjetaCredito,
                importe);
        if (response == null || response.hayError()) {
            return Respuesta.error();
        }

        return Respuesta.exito();

    }

    public static Respuesta consultarPagosProgramadosTarjetaCredito(ContextoHB contexto) {
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");

        if (Objeto.anyEmpty(idTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return Respuesta.error();
        }

        ApiResponse responseCatalogo = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCatalogo.hayError()) { // si dio error le devuelvo que está en horario para que desde front funcione
            // como siempre
            return Respuesta.error();
        }
        Date diaHabilPosterior = responseCatalogo.objetos().get(0).date("diaHabilPosterior", "yyyy-MM-dd");
        Calendar fechaProximoDiaHabil = Calendar.getInstance();
        Calendar fechaProximoDiaHabilSinHora = Calendar.getInstance();
        fechaProximoDiaHabil.setTime(diaHabilPosterior);
        fechaProximoDiaHabilSinHora.setTime(diaHabilPosterior);
        fechaProximoDiaHabilSinHora.set(Calendar.HOUR_OF_DAY, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.MINUTE, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.SECOND, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.MILLISECOND, 0);

        ApiResponse response = RestScheduler.consultarTareas(contexto, "Por Procesar");
        if (response.hayError()) {
            return Respuesta.error();
        }

        Calendar hoy = Calendar.getInstance();
        Calendar hoySinHora = Calendar.getInstance();
        hoySinHora.set(Calendar.HOUR_OF_DAY, 0);
        hoySinHora.set(Calendar.MINUTE, 0);
        hoySinHora.set(Calendar.SECOND, 0);
        hoySinHora.set(Calendar.MILLISECOND, 0);

        Calendar manana = Calendar.getInstance();
        manana.add(Calendar.DAY_OF_YEAR, 1);

        Respuesta respuesta = new Respuesta();
        respuesta.set("mostrarAvisoPagoProgramado", false);

        Objeto tareas = new Objeto();
        String horaAlertaHoy = "";
        String horaAlertaManana = "";
        Boolean alertaHoy = false;
        Boolean alertaManana = false;

        respuesta.set("existePagoProgramado", false);
        for (Objeto item : response.objetos("schedulers")) {
            if (item.string("tipoRequest").equals("json")) {
                ApiResponse responseItem = RestScheduler.consultarTareaEspecifica(contexto, item.string("id"));
                if (responseItem.string("url")
                        .equals(ConfigHB.string("api_url_orquestados") + "/api/tarjeta/pagoTarjeta")) {
                    // me fijo si es la cuenta y la tarjeta correspondiente
                    Objeto tarea = new Objeto();
                    Objeto request = Objeto.fromJson(responseItem.string("request"));
                    if (!request.string("numeroTarjeta").equals(tarjetaCredito.numero())) {
                        continue;
                    }
                    Calendar fechaProximaEjecucion = Calendar.getInstance();
                    Calendar fechaProximaEjecucionSinHora = Calendar.getInstance();

                    // me fijo si la fecha corresponde como para mostrar la leyenda
                    fechaProximaEjecucion
                            .setTime(responseItem.date("fechaHoraProximaEjecucion", "yyyy-MM-dd'T'HH:mm:ss"));
                    fechaProximaEjecucionSinHora
                            .setTime(responseItem.date("fechaHoraProximaEjecucion", "yyyy-MM-dd'T'HH:mm:ss"));
                    fechaProximaEjecucionSinHora.set(Calendar.HOUR_OF_DAY, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.MINUTE, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.SECOND, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.MILLISECOND, 0);

                    if (/*
                     * fechaProgramada.get(Calendar.HOUR) != 7 ||
                     */fechaProximaEjecucion.get(Calendar.MINUTE) != 0
                            || fechaProximaEjecucion.get(Calendar.AM_PM) != Calendar.AM) {
                        continue;
                    }

                    if (fechaProximaEjecucion.compareTo(hoy) < 0)
                        continue;

                    Boolean agregarArray = false;

                    int diasDiferenciaHoy = (int) (fechaProximaEjecucionSinHora.getTime().getTime()
                            - hoySinHora.getTime().getTime()) / 86400000;
                    int diasDiferenciaDiaHabil = (int) (fechaProximoDiaHabilSinHora.getTime().getTime()
                            - fechaProximaEjecucionSinHora.getTime().getTime()) / 86400000;

                    if (diasDiferenciaHoy == 0) { // esta programada para hoy
                        respuesta.set("mostrarAvisoPagoProgramado", true);
                        alertaHoy = true;
                        agregarArray = true;
                        horaAlertaHoy = new SimpleDateFormat("hh:mm").format(fechaProximaEjecucion.getTime());
                        if (horaAlertaHoy.substring(3, 5).equals("00")) {
                            horaAlertaHoy = horaAlertaHoy.substring(0, 2);
                        }
                    } else {
                        if (diasDiferenciaHoy == 1) { // está programado para mañana
                            respuesta.set("mostrarAvisoPagoProgramado", true);
                            agregarArray = true;
                            alertaManana = true;
                            horaAlertaManana = new SimpleDateFormat("hh:mm").format(fechaProximaEjecucion.getTime());
                            if (horaAlertaManana.substring(3, 5).equals("00")) {
                                horaAlertaManana = horaAlertaManana.substring(0, 2);
                            }
                        } else {
                            // me tengo que fijar si está programada para el día habil, sino la ignoro
                            if (diasDiferenciaDiaHabil == 0) {
                                respuesta.set("mostrarAvisoPagoProgramado", true);
                                agregarArray = true;
                            } else {// no está programado para el día hábil, así que lo ignoro
                                continue;
                            }
                        }
                    }

                    if (agregarArray) {
                        tarea.set("numeroCuenta", request.string("cuenta"));
                        Cuenta cuenta = contexto.cuenta(request.string("cuenta"));
                        if (cuenta != null) {
                            Objeto cuentaTarea = new Objeto();
                            cuentaTarea.set("idCuenta", cuenta.id());
                            cuentaTarea.set("numeroCuenta", request.string("cuenta"));
                            cuentaTarea.set("descripcionCorta", cuenta.descripcionCorta());
                            cuentaTarea.set("simboloMoneda", cuenta.simboloMoneda());
                            cuentaTarea.set("numeroFormateado", cuenta.numeroFormateado());
                            cuentaTarea.set("saldo", cuenta.saldo());
                            cuentaTarea.set("saldoFormateado", cuenta.saldoFormateado());

                            tarea.set("cuenta", cuentaTarea);
                        }
                        tarea.set("importe", request.bigDecimal("importe"));
                        tarea.set("importeFormateado", Formateador.importe(request.bigDecimal("importe")));
                        tarea.set("idMoneda", request.bigDecimal("moneda"));
                        tarea.set("tipoTarjeta", request.string("tipoTarjeta"));
                        tarea.set("fechaProgramada",
                                new SimpleDateFormat("dd/MM/yyyy").format(fechaProximaEjecucion.getTime()));
                        String sHoraFechaProximaEjecucion = new SimpleDateFormat("hh:mm")
                                .format(fechaProximaEjecucion.getTime());

                        if (sHoraFechaProximaEjecucion.substring(3, 5).equals("00")) {
                            sHoraFechaProximaEjecucion = sHoraFechaProximaEjecucion.substring(0, 2);
                        }
                        tarea.set("fechaProgramadaHora", sHoraFechaProximaEjecucion);

                        tareas.add(tarea);
                        respuesta.set("existePagoProgramado", true);
                        respuesta.set("tareas", tareas);
                    }
                }
            }
        }
        respuesta.set("alertaHoy", alertaHoy);
        respuesta.set("horaAlertaHoy", horaAlertaHoy);
        respuesta.set("alertaManana", alertaManana);
        respuesta.set("horaAlertaManana", horaAlertaManana);

        return respuesta;

    }


    // emm-20190514-desde
    public static byte[] ultimaLiquidacion(ContextoHB contexto) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return null;// Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
        }
        // String numeroDocumento = contexto.sesion().numeroDocumento();
        String cuit = contexto.persona().cuit();

        if (contexto.persona().esMenor()) {
            return null;// return Respuesta.estado("MENOR_NO_AUTORIZADO");
        }
        ApiResponse response = TarjetaCreditoService.ultimaLiquidacion(contexto, tarjetaCredito.numero(), cuit);
        if (response.hayError()) {
            String htmlError = "";
            contexto.responseHeader("Location", "/hb/#/errorArchivoGenerado");
            contexto.httpCode(302);
            return htmlError.getBytes();
        }

        byte[] decodedString = Base64.getDecoder().decode(response.string("file").getBytes());
        contexto.responseHeader("Content-Type",
                "application/pdf; name=ultima_liquidacion_" + tarjetaCredito.ultimos4digitos() + ".pdf");

        return decodedString;

    }
    // emm-20190514-hasta

    public static Respuesta blanquearPil(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.error();
        }

        Respuesta respuestaPausado = verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausado != null)
            return respuestaPausado;

        ApiResponse response = TarjetaDebitoService.tarjetaDebitoBlanquearPil(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }
        Api.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return Respuesta.exito();

    }

    public static Respuesta blanquearPin(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.error();
        }

        Respuesta respuestaPausado = verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausado != null)
            return respuestaPausado;

        ApiResponse response = TarjetaDebitoService.tarjetaDebitoBlanquearPin(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }
        Api.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return Respuesta.exito();

    }

    public static Respuesta habilitarTarjetaDebito(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.error();
        }

        ApiResponse response = TarjetaDebitoService.habilitarTarjetaDebito(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }

        Api.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());

        return Respuesta.exito();
    }

    public static Respuesta limitesTarjetaDebito(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.estado("TARJETA_NO_ENCONTRADA");
        }

        Objeto limites = new Objeto();
        limites.set("habilitadaLink", tarjetaDebito.habilitadaLink());
        limites.set("simboloMoneda", "$");
        limites.set("compra", tarjetaDebito.limiteCompra());
        limites.set("compraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
        limites.set("extraccion", tarjetaDebito.limiteExtraccion());
        limites.set("extraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));

        if(HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_limite_extraccion_v2")){
            return opcionesExtraccionV2(tarjetaDebito, limites);
        }

        if (tarjetaDebito.limiteExtraccion2().intValue() != 10000) {
            limites.add("opciones", new Objeto().set("id", "12").set("compraFormateado", "60.000")
                    .set("extraccionFormateado", "10.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 60000) {
            limites.add("opciones", new Objeto().set("id", "13").set("compraFormateado", "360.000")
                    .set("extraccionFormateado", "60.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 150000) {
            limites.add("opciones", new Objeto().set("id", "14").set("compraFormateado", "900.000")
                    .set("extraccionFormateado", "150.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 300000) {
            limites.add("opciones", new Objeto().set("id", "15").set("compraFormateado", "1.800.000")
                    .set("extraccionFormateado", "300.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 400000) {
            limites.add("opciones", new Objeto().set("id", "07").set("compraFormateado", "2.400.000")
                    .set("extraccionFormateado", "400.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 600000) {
            limites.add("opciones", new Objeto().set("id", "16").set("compraFormateado", "3.600.000")
                    .set("extraccionFormateado", "600.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 800000) {
            limites.add("opciones", new Objeto().set("id", "17").set("compraFormateado", "4.800.000")
                    .set("extraccionFormateado", "800.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 1000000) {
            limites.add("opciones", new Objeto().set("id", "11").set("compraFormateado", "6.000.000")
                    .set("extraccionFormateado", "1.000.000"));
        }

        return Respuesta.exito("limites", limites);
    }

    private static Respuesta opcionesExtraccionV2(TarjetaDebito tarjetaDebito, Objeto limites) {
        if (tarjetaDebito.limiteExtraccion2().intValue() != 300000) {
            limites.add("opciones", new Objeto().set("id", "14").set("compraFormateado", "1.800.000")
                    .set("extraccionFormateado", "300.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 600000) {
            limites.add("opciones", new Objeto().set("id", "15").set("compraFormateado", "3.600.000")
                    .set("extraccionFormateado", "600.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 800000) {
            limites.add("opciones", new Objeto().set("id", "16").set("compraFormateado", "4.800.000")
                    .set("extraccionFormateado", "800.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 1000000) {
            limites.add("opciones", new Objeto().set("id", "17").set("compraFormateado", "6.000.000")
                    .set("extraccionFormateado", "1.000.000"));
        }
        if (tarjetaDebito.limiteExtraccion2().intValue() != 1200000) {
            limites.add("opciones", new Objeto().set("id", "11").set("compraFormateado", "7.200.000")
                    .set("extraccionFormateado", "1.200.000"));
        }

        return Respuesta.exito("limites", limites);
    }

    public static Respuesta modificarLimiteTarjetaDebito(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String limiteExtraccion = contexto.parametros.string("limiteExtraccion");

        if (Objeto.anyEmpty(idTarjetaDebito, limiteExtraccion)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.estado("TARJETA_NO_ENCONTRADA");
        }

        if (!tarjetaDebito.habilitadaLink()) {
            return Respuesta.estado("TARJETA_NO_HABILITADA");
        }

        ApiRequest request = Api.request("ModificarLimiteTarjetaDebito", "tarjetasdebito", "PUT",
                "/v1/tarjetasdebito/{nrotarjeta}", contexto);
        request.path("nrotarjeta", tarjetaDebito.numero());
        request.query("idcliente", contexto.idCobis());
        request.query("limiteretiro", limiteExtraccion);
        request.query("tipotarjeta", tarjetaDebito.idTipoTarjeta());

        ApiResponse response = Api.response(request, contexto.idCobis(), tarjetaDebito.numero());
        if (response.hayError()) {
            if (response.string("codigo").equals("1831602")) {
                return Respuesta.estado("EXISTE_SOLICITUD");
            }
            if (response.string("codigo").equals("40003")) {
                return Respuesta.estado("FUERA_HORARIO");
            }
            return Respuesta.error();
        }
        Api.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return Respuesta.exito();
    }

    public static Respuesta consolidadaFormaPagoTarjetaCredito(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return Respuesta.estado("SIN_TARJETA_CREDITO_TITULAR");
        }

        List<Cuenta> cuentas = contexto.cuentas();
//		if (cuentas.isEmpty()) {
//			return Respuesta.estado("SIN_CUENTAS");
//		}

        // Forma Pago
        String descripcion = "";
        String idFormaPago = tarjetaCredito.idFormaPago();
        descripcion = "01".equals(idFormaPago) ? "Efectivo por ventanilla" : descripcion;
        descripcion = "02".equals(idFormaPago) ? "Débito automatico del pago mínimo" : descripcion;
        descripcion = "03".equals(idFormaPago) ? "Débito automatico del pago total" : descripcion;
        descripcion = "04".equals(idFormaPago) ? "Débito automatico del pago mínimo" : descripcion;
        descripcion = "05".equals(idFormaPago) ? "Débito automatico del pago total" : descripcion;
        if (idFormaPago.equals("01")) {
            respuesta.set("idFormaPago", "3");
        }
        if (idFormaPago.equals("02") || idFormaPago.equals("04")) {
            respuesta.set("idFormaPago", "2");
        }
        if (idFormaPago.equals("03") || idFormaPago.equals("05")) {
            respuesta.set("idFormaPago", "1");
        }
        respuesta.set("descripcionFormaPago", descripcion);

        // Fuera Horario
        LocalTime target = LocalTime.now();
        Boolean enHorario = (target.isAfter(LocalTime.parse("06:00:00"))
                && target.isBefore(LocalTime.parse("21:00:00")));
        respuesta.set("fueraHorario", !enHorario);

        // Formas de pago
        if (!(idFormaPago.equals("03") || idFormaPago.equals("05"))) {
            respuesta.add("formasPago",
                    new Objeto().set("id", "1").set("descripcion", "Débito automatico del pago total"));
        }
        if (!(idFormaPago.equals("02") || idFormaPago.equals("04"))) {
            respuesta.add("formasPago",
                    new Objeto().set("id", "2").set("descripcion", "Débito automatico del pago mínimo"));
        }
        if (!(idFormaPago.equals("01"))) {
            respuesta.add("formasPago", new Objeto().set("id", "3").set("descripcion", "Efectivo por ventanilla"));
        }

        // Cuentas
        for (Cuenta cuenta : cuentas) {
            if (cuenta.esDolares()) {
                continue;
            }
            Objeto item = new Objeto();
            //item.set("id", cuenta.id());
            item.set("id", cuenta.idEncriptado());
            item.set("descripcionCorta", cuenta.descripcionCorta());
            item.set("idMoneda", cuenta.idMoneda());
            item.set("simboloMoneda", cuenta.simboloMoneda());
            item.set("ultimos4digitos", cuenta.ultimos4digitos());
            item.set("saldo", cuenta.saldo());
            item.set("saldoFormateado", cuenta.saldoFormateado());
            respuesta.add("cuentas", item);
        }

        return respuesta;
    }

    public static Respuesta cambiarFormaPagoTarjetaCredito(ContextoHB contexto) {
        String idFormaPago = contexto.parametros.string("idFormaPago");
        String idCuenta = contexto.parametros.string("idCuenta");

        try {
            if (Objeto.anyEmpty(idFormaPago, idCuenta)) {
                return Respuesta.parametrosIncorrectos();
            }

            Cuenta cuenta = contexto.cuenta(idCuenta);
            if (cuenta == null) {
                return Respuesta.estado("CUENTA_INEXISTENTE");
            }

            TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
            if (tarjetaCredito == null) {
                return Respuesta.estado("SIN_TARJETA_CREDITO_TITULAR");
            }

            String formaPago = idFormaPago.equals("3") ? "01" : "";
            if (formaPago.isEmpty()) {
                formaPago = idFormaPago.equals("1") && cuenta.esCajaAhorro() ? "05" : formaPago;
                formaPago = idFormaPago.equals("1") && cuenta.esCuentaCorriente() ? "03" : formaPago;
                formaPago = idFormaPago.equals("2") && cuenta.esCajaAhorro() ? "04" : formaPago;
                formaPago = idFormaPago.equals("2") && cuenta.esCuentaCorriente() ? "02" : formaPago;
            }

            String tipificacion = "SAV-TC";

            if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, null, true)) {

                return Respuesta.estado("SOLICITUD_EN_CURSO");
            }

            ApiResponse responseReclamo = RestPostventa.cambioFormaPagoTC(contexto, tipificacion, formaPago,
                    tarjetaCredito, cuenta);

            if (responseReclamo == null || responseReclamo.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(responseReclamo);

            if (numeroCaso.isEmpty()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            // No funciona el insert
//			SqlResponse sqlResponse = HBProducto.insertarReclamo(contexto, "CFP_TC", cuenta.numero(), tarjetaCredito.numero(), "", "");
//			if (sqlResponse.hayError) {
//				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
//			}

            ProductosService.eliminarCacheProductos(contexto);
            return Respuesta.exito().set("ticket", numeroCaso);

        } catch (Exception e) {
            return Respuesta.error();
        }

    }

    public static Respuesta tarjetasCreditoPropias(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            List<String> docs = new ArrayList<>();

            // LLAMADAS A SERVICIOS EN PARALELO
            Futuro<Respuesta> futuroSolicitudesEstado = new Futuro<>(() -> HBProcesos.estadoSolicitudes(contexto));

            Map<String, Futuro<ApiResponse>> futurosResponsePorId = new HashMap<>();
            for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
                futurosResponsePorId.put(tarjetaCredito.numero(), new Futuro<>(
                        () -> TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.id())));
            }

            Map<String, Futuro<ApiResponse>> futurosResponseResumen = new HashMap<>();
            for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
                ApiResponse responsePorId = futurosResponsePorId.get(tarjetaCredito.numero()).get();
                futurosResponseResumen.put(tarjetaCredito.numero(),
                        new Futuro<>(() -> TarjetaCreditoService.resumenCuenta(contexto,
                                responsePorId.objetos().get(0).string("cuenta"), tarjetaCredito.id())));
            }
            // FIN DE LLAMADAS A SERVICIOS

            for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
                Objeto item = new Objeto();
                item.set("id", tarjetaCredito.idEncriptado());
                item.set("tipo", tarjetaCredito.tipo());
                item.set("idTipo", tarjetaCredito.idTipo());
                item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
                item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
                item.set("esTitular", tarjetaCredito.esTitular());
                item.set("titularidad", tarjetaCredito.titularidad());
                item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
                item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
                item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
                item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
                item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
                item.set("formaPago", tarjetaCredito.formaPago());
                item.set("idPaquete", tarjetaCredito.idPaquete());
                item.set("nombre", tarjetaCredito.denominacionTarjeta().trim());
                item.set("limite", tarjetaCredito.limiteCompra());
                item.set("limiteFormateado", Formateador.importe(tarjetaCredito.limiteCompra()));

                item.set("descripcionTarjeta", tarjetaCredito.producto());

                ApiResponse responsePorId = futurosResponsePorId.get(tarjetaCredito.numero()).get();
                ApiResponse responseResumen = futurosResponseResumen.get(tarjetaCredito.numero()).get();

                item.set("documento", responseResumen.string("resumenCuenta.datos.documento"));
                docs.add(responseResumen.string("resumenCuenta.datos.documento"));

                if (tarjetaCredito.esTitular()) {
                    respuesta.set("tarjetaCreditoTitular", item);
                } else {
                    if (!responsePorId.objetos().get(0).bool("tarjetaHabilitada")
                            && responsePorId.objetos().get(0).string("tarjetaEstado").equals("20")) {
                        item.set("nombre", tarjetaCredito.denominacionTarjeta().trim());
                        item.set("documento", responseResumen.string("resumenCuenta.datos.documento"));
                        item.set("fechaSolicitud", tarjetaCredito.fechaAlta("dd/MM/yyyy"));
                        respuesta.add("tarjetasCreditoAdicionalesEnCurso", item);
                    } else {
                        respuesta.add("tarjetasCreditoAdicionales", item);
                    }
                }
            }

            try {
                Respuesta solicitudesEstado = futuroSolicitudesEstado.get();
                if (!solicitudesEstado.hayError()) {
                    List<Objeto> solicitudes = (List<Objeto>) solicitudesEstado.objetos("solicitudes");
                    if (solicitudes == null || solicitudes.isEmpty()) {
                        solicitudes = (List<Objeto>) solicitudesEstado.objetos("solicitudes");
                    }
                    Objeto relacion = new Objeto();
                    for (Objeto soli : solicitudes) {
                        if ("ADICIONALES DE TARJETA DE CRÉDITO VISA".equalsIgnoreCase(soli.string("producto"))) {
                            if (!Arrays.asList(CODIGOS_FINALIZADO).contains(soli.string("estado"))
                                    || chequeoNuevaSolicitud(contexto, soli)) {
                                ApiResponse response = RestVenta.consultarSolicitudes(contexto,
                                        soli.string("idSolicitud"));
                                for (Objeto integrante : response.objetos("Datos").get(0).objetos("Integrantes")) {
                                    if (response.objetos("Datos").get(0).objetos("Integrantes").size() >= 3) {
                                        relacion = RestPersona.getTipoRelacionPersona(contexto,
                                                integrante.string("NumeroDocumentoTributario"));
                                        if ("2".equals(relacion.string("idTipoRelacion"))) {
                                            docs.add(integrante.string("NumeroDocumento"));
                                        }
                                    }

                                    if (!docs.contains(integrante.string("NumeroDocumento"))) {
                                        Objeto item = new Objeto();
                                        item.set("nombre",
                                                integrante.string("Apellido") + " " + integrante.string("Nombres"));
                                        item.set("documento", integrante.string("NumeroDocumento"));
                                        item.set("fechaSolicitud", soli.get("dateDesc"));
                                        respuesta.add("tarjetasCreditoAdicionalesEnCurso", item);
                                        docs.add(integrante.string("NumeroDocumento"));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta ofertaSolicitudTarjetaCreditoAdicional(ContextoHB contexto) {
        String idTarjetaCreditoAdicional = contexto.parametros.string("idTarjetaCreditoAdicional");

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return Respuesta.estado("SIN_TARJETA_CREDITO_TITULAR");
        }

        TarjetaCredito tarjetaCreditoAdicional = contexto.tarjetaCredito(idTarjetaCreditoAdicional);

        Objeto titular = new Objeto();
        titular.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
        titular.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
        titular.set("limiteCompraUnPagoFormateado", Formateador.importe(tarjetaCredito.limiteCompra()));
        titular.set("limiteCompraCuotasFormateado", Formateador.importe(tarjetaCredito.limiteCompraCuotas()));

        Objeto ofertas = new Objeto();
        for (Integer valor : Objeto.listOf(100, 75, 50, 25, 15, 10)) {
            BigDecimal porcentaje = new BigDecimal(valor).divide(new BigDecimal(100));
            Objeto oferta = new Objeto();
            oferta.set("id", valor);
            oferta.set("limiteCompraUnPagoFormateado",
                    Formateador.importe(tarjetaCredito.limiteCompra().multiply(porcentaje)));
            oferta.set("limiteCompraCuotasFormateado",
                    Formateador.importe(tarjetaCredito.limiteCompraCuotas().multiply(porcentaje)));
            oferta.set("ofertaActual", false);

            if (tarjetaCreditoAdicional != null) {
                BigDecimal limiteTitular = tarjetaCredito.limiteCompra();
                BigDecimal limiteAdicional = tarjetaCreditoAdicional.limiteCompra();
                BigDecimal ratio = limiteAdicional.divide(limiteTitular, 2, RoundingMode.HALF_UP);

                BigDecimal distanciaAl100 = new BigDecimal("1.00").subtract(ratio).abs();
                BigDecimal distanciaAl75 = new BigDecimal("0.75").subtract(ratio).abs();
                BigDecimal distanciaAl50 = new BigDecimal("0.50").subtract(ratio).abs();
                BigDecimal distanciaAl25 = new BigDecimal("0.25").subtract(ratio).abs();
                BigDecimal distanciaAl15 = new BigDecimal("0.15").subtract(ratio).abs();
                BigDecimal distanciaAl10 = new BigDecimal("0.10").subtract(ratio).abs();

                Integer porcentajeInteger = 100;
                BigDecimal menorDistancia = distanciaAl100.min(distanciaAl75).min(distanciaAl50).min(distanciaAl25)
                        .min(distanciaAl15).min(distanciaAl10);
                if (menorDistancia.equals(distanciaAl100)) {
                    porcentajeInteger = 100;
                } else if (menorDistancia.equals(distanciaAl75)) {
                    porcentajeInteger = 75;
                } else if (menorDistancia.equals(distanciaAl50)) {
                    porcentajeInteger = 50;
                } else if (menorDistancia.equals(distanciaAl25)) {
                    porcentajeInteger = 25;
                } else if (menorDistancia.equals(distanciaAl15)) {
                    porcentajeInteger = 15;
                } else if (menorDistancia.equals(distanciaAl10)) {
                    porcentajeInteger = 10;
                }

                if (porcentajeInteger.equals(valor)) {
                    oferta.set("ofertaActual", true);
                }
            }
            ofertas.add(oferta);
        }

        return Respuesta.exito("titular", titular).set("ofertas", ofertas);
    }

    public static Respuesta crearSolicitudTarjetaCreditoAdicional(ContextoHB contexto) {
        Integer porcentaje = contexto.parametros.integer("porcentaje");
        String cuit = contexto.parametros.string("cuil", "");
        String idSexo = contexto.parametros.string("idSexo", "");
        String nombre = contexto.parametros.string("nombre", "");
        String apellido = contexto.parametros.string("apellido", "");
        String tipoRelacion = contexto.parametros.string("tipoRelacion", "18");
        String email = contexto.parametros.string("email", "");
        String fechaNacimiento = contexto.parametros.string("fechaNacimiento", "");
        String codigoArea = contexto.parametros.string("codigoArea", "");
        String caracteristica = contexto.parametros.string("caracteristica", "");
        String numero = contexto.parametros.string("numero", "");

        if (Objeto.anyEmpty(porcentaje)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (!ConfigHB.esProduccion() && contexto.idCobis().equals("772245")) {
            return Respuesta.error();
        }

        TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();
        if (!tarjetaCreditoTitular.idEstado().equals("20")) {
            return Respuesta.estado("TARJETA_CON_PROBLEMAS");
        }

        if (tarjetaCreditoTitular.esHML()) {
            return Respuesta.estado("TARJETA_HML");
        }

        try {

            if (cuit.isEmpty()) {
                contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA_CUIT");
                return Respuesta.estado("PERSONA_NO_ENCONTRADA");
            }

            String cobisRelacion = "";
            Api.eliminarCache(contexto, "PersonasRelacionadas", cuit);
            ApiResponse responsePersonaEspecifica = RestPersona.consultarPersonaEspecifica(contexto, cuit);
            cobisRelacion = responsePersonaEspecifica.string("idCliente");

            if (cobisRelacion.isEmpty()) {
                return Respuesta.estado("PERSONA_NO_ENCONTRADA");
            }

            if (!ConfigHB.esDesarrollo()) {

                if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
                        || responsePersonaEspecifica.string("idVersionDocumento").isEmpty()
                        || responsePersonaEspecifica.string("idEstadoCivil").isEmpty()
                        || responsePersonaEspecifica.string("fechaNacimiento").isEmpty()
                        || responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()
                        || responsePersonaEspecifica.string("idSexo").isEmpty()) {

                    Objeto datos = new Objeto();

                    if (responsePersonaEspecifica.string("idSexo").isEmpty()) {
                        datos.set("idSexo", idSexo);
                    }

                    if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
                            || responsePersonaEspecifica.string("idNacionalidad").isEmpty()) {

                        datos.set("idPaisNacimiento", "80");
                        datos.set("idNacionalidad", "80");
                        datos.set("idPaisResidencia", "");
                    }

                    if (responsePersonaEspecifica.string("idVersionDocumento").isEmpty()) {
                        datos.set("idVersionDocumento", "A");
                    }

                    if (responsePersonaEspecifica.string("idEstadoCivil").isEmpty()) {
                        datos.set("idEstadoCivil", "S");
                    }

                    if (responsePersonaEspecifica.string("fechaNacimiento").isEmpty()) {
                        fechaNacimiento = fechaNacimiento.replace("/", "-");
                        datos.set("fechaNacimiento", fechaNacimiento + "T00:00:00");
                    }

                    if (responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {
                        datos.set("idSituacionImpositiva", "CONF");
                    }
                    RestPersona.actualizarPersona(contexto, datos, cuit);
                }

                //TODO: Tomo los datos del telefono del Titular si vienen vacios
                // porque esto responsePersonaEspecifica.string("etagTelefonos") a veces viene distinto a -1
                // y el telefono esta mal cargado y falla en API-Ventas cuando va a buscar datos del telefono del Adicional
                if(codigoArea.isEmpty() && caracteristica.isEmpty() && numero.isEmpty()){
                    Objeto telefonoTitular = RestPersona.celular(contexto, contexto.persona().cuit());
                    codigoArea = telefonoTitular.string("codigoArea","");
                    caracteristica = telefonoTitular.string("caracteristica","");
                    numero = telefonoTitular.string("numero","");
                    try{
                        RestPersona.actualizarCelular(contexto, cuit, codigoArea, caracteristica, numero);
                    }catch (Exception ignored){}
                }

                //TODO: Tomo el mail del Titular si viene vacio el mail, porque pasa lo mismo que con el telefono
                if(email.isEmpty()){
                    email = contexto.persona().email();
                    try{
                        RestPersona.actualizarEmail(contexto, cuit, email);
                    }catch (Exception ignored){}
                }

                if (responsePersonaEspecifica.string("etagDomicilios").equalsIgnoreCase("-1")) {
                    Objeto domicilioTitularLegal = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
                    RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularLegal, "LE");
                    Objeto domicilioTitularPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
                    RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularPostal, "DP");
                }
            }

            String idEstadoCivil = contexto.persona().idEstadoCivil();
            Objeto relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);

            if (idEstadoCivil.equalsIgnoreCase("C") && !tipoRelacion.equals("2")) {
                if (relacion.string("idTipoRelacion").equals("2")) {
                    tipoRelacion = "2";
                }
            }

            if (relacion.string("idTipoRelacion").isEmpty()) {
                try {
                    ApiResponse responseGenerarRelacionPersona = RestPersona.generarRelacionPersona(contexto,
                            tipoRelacion, cuit, cobisRelacion);
                    if (responseGenerarRelacionPersona.hayError()) {
                        return Respuesta.estado("ERROR_GENERAR_RELACION");
                    }
                    try {
                        Api.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
                        relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
            } else if (!relacion.string("idTipoRelacion").equals(tipoRelacion)) {
                try {
                    Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                    SimpleDateFormat destinoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String fechaModificacion = destinoSDF.format(fechaActual);
                    ApiResponse responseActualizarRelacionPersona = RestPersona.actualizarRelacionPersona(contexto,
                            relacion.string("id"), tipoRelacion, cuit, cobisRelacion, null, null, fechaModificacion);

                    if (responseActualizarRelacionPersona.hayError()) {
                        return Respuesta.estado("ERROR_GENERAR_RELACION");
                    }

                    try {
                        Api.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
                        relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
                    } catch (Exception e) {
                        //
                    }
                } catch (Exception e) {
                    //
                }
            }

            // Caso al adicional con el titular
            if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
                ApiRequest request = Api.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
                request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
                request.path("id", cuit);
                request.body("idEstadoCivil", "C");
                request.body("idSubtipoEstadoCivil", "Y");

                ApiResponse response = Api.response(request, contexto.idCobis());
                if (response.hayError()) {
                    return Respuesta.error();
                }
            }

            // Caso al titular con el adicional
            if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
                ApiRequest request = Api.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
                request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
                request.path("id", contexto.persona().cuit());
                request.body("idEstadoCivil", "C");
                request.body("idSubtipoEstadoCivil", "Y");

                ApiResponse response = Api.response(request, contexto.idCobis());
                if (response.hayError()) {
                    return Respuesta.error();
                }
            }
        } catch (Exception e) {
        }

        // Generar Solicitud
        ApiResponse generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
        if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
            return new Respuesta().setEstado("ERROR").set("error",
                    !generarSolicitud.objetos("Errores").isEmpty()
                            ? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }
        String idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

        // Generar Integrante
        ApiResponse generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
        if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
            contador(contexto, "ADCIONAL_TC_INTE1_" + idSolicitud);
            return new Respuesta().setEstado("ERROR").set("error",
                    !generarIntegrante.objetos("Errores").isEmpty()
                            ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }
        generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud, cuit);
        if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
            contador(contexto, "ADCIONAL_TC_INTE2_" + idSolicitud);
            return new Respuesta().setEstado("ERROR").set("error",
                    !generarIntegrante.objetos("Errores").isEmpty()
                            ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }

        // Generar Tarjeta Credito Adicional
        ApiResponse tarjetaCreditoAdicional = RestOmnicanalidad.generarTarjetaCreditoAdicional(contexto, idSolicitud,
                cuit);
        if (tarjetaCreditoAdicional.hayError() || !tarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
            contador(contexto, "ADCIONAL_TC_GENADIC_" + idSolicitud);
            return new Respuesta().setEstado("ERROR").set("error",
                    !tarjetaCreditoAdicional.objetos("Errores").isEmpty()
                            ? tarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }
        String idTarjetaCreditoAdicional = tarjetaCreditoAdicional.objetos("Datos").get(0).string("Id");

        // Motor
        ApiResponse responseMotor = RestOmnicanalidad.evaluarSolicitud(contexto, idSolicitud);
        if (responseMotor.hayError() || !responseMotor.objetos("Errores").isEmpty()) {
            try {
                RestVenta.desistirSolicitud(contexto, idSolicitud);
                contador(contexto, "ADCIONAL_TC_ER_MOTOR_" + idSolicitud);
            } catch (Exception e) {
            }
            return Respuesta.error().set("error", responseMotor.objetos("Errores").get(0).string("MensajeCliente"));
        }
        String resolucionId = responseMotor.objetos("Datos").get(0).string("ResolucionId");
        if (!resolucionId.equals("AV")) {
            String explicacion = responseMotor.objetos("Datos").get(0).string("Explicacion");

            String estado = switch (resolucionId) {
                case "AA" -> "APROBADO_AMARILLO";
                case "CT", "RE" -> "ROJO";
                default -> "ERROR";
            };

            if (explicacion.contains("La edad es inferior a la mínima requerida")) {
                switch (tipoRelacion) {
                    case "2": {
                        estado = "EDAD_16";
                        break;
                    }
                    case "18": {
                        estado = "EDAD_18";
                        break;
                    }
                    case "1", "4", "15": {
                        estado = "EDAD_13";
                        break;
                    }
                    default:
                        break;
                }
            }
            try {
                RestVenta.desistirSolicitud(contexto, idSolicitud);
            } catch (Exception e) {
            }
            contador(contexto, "ADCIONAL_TC_" + estado);
            contador(contexto, "ADCIONAL_TC_SOL_" + idSolicitud);
            return new Respuesta().setEstado(estado).set("error", explicacion);
        }

        ApiResponse responsePersona = RestPersona.consultarPersonaEspecifica(contexto, cuit);
        if (responsePersona.hayError()) {
            return new Respuesta().setEstado("ERROR");
        }

        // Actualizar Tarjeta Credito Adicional
        ApiResponse actualizarTarjetaCreditoAdicional = RestOmnicanalidad.actualizarTarjetaCreditoAdicional(contexto,
                idSolicitud, idTarjetaCreditoAdicional, cuit, ContextoHB.embozado(responsePersona.string("nombres"), responsePersona.string("apellidos")), porcentaje);
        if (actualizarTarjetaCreditoAdicional.hayError()
                || !actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
            contador(contexto, "ADCIONAL_TC_ACT_SOL_" + idSolicitud);
            return new Respuesta().setEstado("ERROR").set("error",
                    !actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()
                            ? actualizarTarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }

        // Finalizar
        ApiResponse response = RestOmnicanalidad.finalizarSolicitud(contexto, idSolicitud);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            String estado = "ERROR";
            if (!response.objetos("Errores").isEmpty()
                    && response.objetos("Errores").get(0).string("Codigo").equals("1831609")) {
                estado = "IR_A_SUCURSAL";
            }
            if (!response.objetos("Errores").isEmpty()
                    && response.objetos("Errores").get(0).string("Codigo").equals("1831602")) {
                estado = "EN_PROCESO_ACTUALIZACION";
            }

            contador(contexto, "ADCIONAL_TC_" + estado + idSolicitud);
            return new Respuesta().setEstado(estado).set("error",
                    !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente")
                            : null);
        }

        try {
            Api.eliminarCache(contexto, "EstadoSolicitudBPM", contexto.idCobis(), contexto.persona().numeroDocumento());
        } catch (Exception e) {
            //
        }

        contador(contexto, "ADCIONAL_TC_OK");
        return Respuesta.exito();
    }

    public static Respuesta cambioLimiteTarjetaCreditoAdicional(ContextoHB contexto) {
        String idTarjetaCreditoAdicional = contexto.parametros.string("idTarjetaCreditoAdicional");
        Integer porcentaje = contexto.parametros.integer("porcentaje");

        if (Objeto.anyEmpty(idTarjetaCreditoAdicional, porcentaje)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();
        if (tarjetaCreditoTitular == null) {
            return Respuesta.estado("SIN_TARJETA_TITULAR");
        }
        if (!tarjetaCreditoTitular.idEstado().equals("20")) {
            return Respuesta.estado("TARJETA_CON_PROBLEMAS");
        }

        TarjetaCredito tarjetaCreditoAdicional = contexto.tarjetaCredito(idTarjetaCreditoAdicional);
        if (tarjetaCreditoAdicional == null) {
            return Respuesta.estado("TARJETA_NO_ENCONTRADA");
        }

        ApiRequest request = Api.request("ModificarLimiteTarjetaCreditoAdicional", "tarjetascredito", "POST",
                "/v1/modificacionPorcentajeAlta", contexto);
        request.body.set("adeLimiPorcen", porcentaje);
        request.body.set("compraLimiPorcen", porcentaje);
        request.body.set("cuenta", tarjetaCreditoTitular.cuenta());
        request.body.set("cuotasLimiPorcen", porcentaje);
        request.body.set("marcaCodi", "2");
        request.body.set("tarjeNume", tarjetaCreditoAdicional.numero());

        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            return Respuesta.error();
        }
        if (response.string("codigo").equals("50021")) {
            return Respuesta.estado("SOLICITUD_EN_CURSO");
        }
        if (!response.string("codigo").equals("0")) {
            return Respuesta.error();
        }

        return Respuesta.exito();
    }

    public static Respuesta tarjetaDebitoHabilitadaRedLink(ContextoHB contexto) {
        boolean traerTarjetasVirtuales = contexto.parametros.bool("traerTarjetasVirtuales", false);

        if (contexto.idCobis() == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }
        List<TarjetaDebito> tarjetasDebitoRedLinkActivo = traerTarjetasVirtuales
                ? contexto.tarjetasDebitoRedLinkActivoConVirtuales()
                : contexto.tarjetasDebitoRedLinkActivo();

        Respuesta respuesta = new Respuesta();

        for (TarjetaDebito tarjetaDebito : tarjetasDebitoRedLinkActivo) {
            Objeto item = new Objeto();
            //item.set("idTarjetaDebito", tarjetaDebito.id());
            item.set("idTarjetaDebito", tarjetaDebito.idEncriptado());
            item.set("ultimosDigitos", tarjetaDebito.ultimos4digitos());
            respuesta.add("tarjetasDebito", item);
        }
        return respuesta;
    }

    public static Respuesta horarioPagoTarjeta(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_pago_programado",
                "prendido_pago_programado_cobis")) {
            respuesta.set("enHorario", true);
            respuesta.set("alertaSeHaceHoy", false);
            return respuesta;
        }

        LocalTime target = LocalTime.now();
        Boolean enHorario = (target.isAfter(LocalTime.parse("06:00:00"))
                && target.isBefore(LocalTime.parse("21:00:00")));
        respuesta.set("enHorario", enHorario);
        if (enHorario) {
            return respuesta;
        }

        ApiResponse response = RestCatalogo.calendarioFechaActual(contexto);
        if (response.hayError()) { // si dio error le devuelvo que está en horario para que desde front funcione
            // como siempre
            respuesta.set("enHorario", true);
            return respuesta;
        }
        boolean esDiaHabil = response.objetos().get(0).string("esDiaHabil") == "1" ? true : false;

        // Este es un horario puesto a mano.
        respuesta.set("horaAlertaHoy", "07:00");
        respuesta.set("horaAlertaManana", "07:00");

        if (target.isBefore(LocalTime.parse("06:00:00")) && esDiaHabil) {
            respuesta.set("alertaSeHaceHoy", true);

        } else {
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            Date diaHabilPosterior = response.objetos().get(0).date("diaHabilPosterior", "yyyy-MM-dd");

            int dias = (int) (diaHabilPosterior.getTime() - hoy.getTime().getTime()) / 86400000;
            if (dias <= 1) {
                respuesta.set("alertaSeHaceManana", true);
            } else {
                respuesta.set("alertaSeHaceManana", false);
                respuesta.set("diaHabilPosterior", new SimpleDateFormat("dd/MM/yyyy").format(diaHabilPosterior));
            }

            respuesta.set("alertaSeHaceHoy", false);
        }

        return respuesta;
    }

    public static Respuesta categoriaMovimientoTarjeta(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        String fechaDesde = LocalDate.parse(fecha).minusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fechaHasta = LocalDate.parse(fecha).plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        int numeroEstablecimiento = contexto.parametros.integer("numeroEstablecimiento");

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            tarjetaCredito = contexto.tarjetaCreditoTitular();
            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }
        }

        ApiResponse response = TarjetaCreditoService.consultaMovimientosTarjeta(contexto, fechaDesde, fechaHasta,
                tarjetaCredito.numero(), "999", "PRI", "99");
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();

        List<Objeto> movimientos = new ArrayList<>();
        for (Objeto movimiento : response.objetos()) {
            Objeto item = new Objeto();
            item.set("fecha", movimiento.string("fechaMovimiento"));
            item.set("monto", movimiento.string("montoMovimiento"));
            item.set("tipoMovimiento", movimiento.string("tipoMovimiento"));
            item.set("numeroEstablecimiento", movimiento.string("numeroEstablecimiento"));
            item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("monto")));
            movimientos.add(item);
        }

        Objeto resultado = filtrarMovimiento(movimientos, monto, numeroEstablecimiento);
        if (resultado != null) {
            switch (resultado.integer("tipoMovimiento")) {
                case 1:
                    respuesta.set("categoria", "COMPRAS");
                    respuesta.set("subcategoria", "TARJETA DE CREDITO");
                    break;
                case 2:
                    respuesta.set("categoria", "PAGOS");
                    respuesta.set("subcategoria", "TARJETA DE CREDITO");
                    break;
                case 3:
                    respuesta.set("categoria", "TARJETA DE CREDITO");
                    respuesta.set("subcategoria", "AJUSTE");
                    break;
                case 4:
                    respuesta.set("categoria", "PAGOS ");
                    respuesta.set("subcategoria", "DEBITOS AUTOMATICOS");
                    break;
            }
        }

        return respuesta;
    }

    private static Objeto filtrarMovimiento(List<Objeto> movimientos, BigDecimal monto, int numeroEstablecimiento) {
        Predicate<Objeto> establecimientoPredicate = movimiento -> movimiento.integer("numeroEstablecimiento")
                .equals(numeroEstablecimiento);
        Predicate<Objeto> montoPredicate = movimiento -> movimiento.bigDecimal("monto").equals(monto);

        List<Objeto> resultado = movimientos.stream().filter(montoPredicate).filter(establecimientoPredicate)
                .collect(Collectors.toList());

        if (resultado.isEmpty()) {
            resultado = movimientos.stream().filter(establecimientoPredicate).collect(Collectors.toList());
        }

        if (resultado.size() == 1 || (resultado.size() > 1 && (validarMismoTipoMovimiento(resultado)))) {
            return resultado.get(0);
        }

        return null;
    }

    private static boolean validarMismoTipoMovimiento(List<Objeto> resultado) {
        int tipoMovimiento = resultado.get(0).integer("tipoMovimiento");
        for (Objeto movimiento : resultado) {
            if (movimiento.integer("tipoMovimiento") != tipoMovimiento) {
                return false;
            }
        }
        return true;
    }

    private static boolean filtrarEstablecimiento(String idEstablecimiento) {
        for (String comercio : ConfigHB.string("comercios_no_disponibles").split("_")) {
            if (comercio.equals(idEstablecimiento.replaceFirst("^0+(?!$)", ""))) {
                return true;
            }
        }
        return false;
    }

    public static Respuesta convertirTarjetaDebitoVirtualToFisica(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            String idTarjeta = contexto.parametros.string("idTarjeta");

            if (idTarjeta.isEmpty()) {
                return Respuesta.parametrosIncorrectos();
            }

            TarjetaDebito td = contexto.tarjetaDebito(idTarjeta);
            ApiResponse response = TarjetaDebitoService.tarjetaDebitoVirtualToFisica(contexto, td);

            if (response.hayError()) {
                respuesta.setEstado("ERROR");
                respuesta.set("mensaje", response.get("mensajeAlUsuario"));
                return respuesta;
            }

            respuesta.set("numeroSolicitud", response.get("numeroSolicitud"));

            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta reposicionTD(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
            String motivo = contexto.parametros.string("motivo", "PSREP001");
            String estadoPieza = "";
            String codigoDistribucion = "-99";

            if (Objeto.anyEmpty("idTarjetaDebito")) {
                return Respuesta.parametrosIncorrectos();
            }

            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);

            if (RestPostventa.tieneSolicitudEnCurso(contexto, "7", new Objeto().set("idTarjetaDebito", td.numero()),
                    true)) {
                return Respuesta.estado("SOLICITUD_EN_CURSO").set("message", ConfigHB.string("message_en_curso"));
            }

            ApiResponse pieza = RestDelivery.deliveryPiezas(contexto, td.numero());
            if (!pieza.hayError() && pieza != null) {
                codigoDistribucion = ((String) pieza.get("Estado")).isEmpty() ? "-10" : (String) pieza.get("Estado");
                estadoPieza = ((String) pieza.get("DescripcionEstado")).isEmpty() ? ""
                        : (String) pieza.get("DescripcionEstado");
            }

            ApiResponse responseReclamo = RestPostventa.reposicionTD(contexto, td.numero(), motivo, estadoPieza,
                    codigoDistribucion);
            if (responseReclamo == null || responseReclamo.hayError()) {
                if (responseReclamo.objetos("Errores").get(0).string("MensajeCliente")
                        .contains("HAY UNA SOLICITUD DE RENOVACION DE TARJETA TRAMITANDOSE DE ESTA MISMA TARJETA")) {
                    return Respuesta.estado("SOLICITUD_EN_CURSO");
                }
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta modalTC(ContextoHB contexto, Objeto campania) {
        Boolean mostrarModalTC = false;
        Boolean enHorario = true;
        String monto = "";
        String tipoTC = "";
        String tipoPack = "";

        Respuesta respuesta = new Respuesta();

        try {
            if (campania.bigDecimal("linTc", "0.0").compareTo(new BigDecimal(0)) == 1) {
                mostrarModalTC = true;

                Respuesta oferta = HBOmnicanalidad.ofertasPaqueteModal(contexto,
                        campania.bigDecimal("linTc").intValue());
                if (oferta.hayError()) {
                    return Respuesta.exito("mostrarModal", false).set("mostrarModalTC", false);
                }

                if (oferta.get("paquetes") == null) {
                    return Respuesta.exito("mostrarModal", false).set("mostrarModalTC", false);
                }
                Objeto paqOferta = oferta.objetos("paquetes").get(0);
                tipoTC = paqOferta.string("tipoTarjetaCredito");
                tipoPack = paqOferta.string("descripcion");
                monto = Formateador.importeCantDecimales(campania.bigDecimal("linTc"), 2);
            }

            respuesta.set("monto", monto);
            respuesta.set("tipoTC", tipoTC);
            respuesta.set("tipoPack", tipoPack);
            respuesta.set("enHorario", enHorario);
            respuesta.set("mostrarModalTC", mostrarModalTC);
            respuesta.set("mostrarModal", false);

            if (mostrarModalTC && enHorario) {
                contexto.parametros.set("nemonico", "ALERTA_TC");
                Util.contador(contexto);
            }

            return respuesta;

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta estadoDeuda(ContextoHB contexto) {

        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return Respuesta.parametrosIncorrectos();
        }
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (tarjetaCredito == null) {
            return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
        }

        Objeto datos = new Objeto();
        Respuesta respuesta = new Respuesta();
        ApiResponse responseMora = RestMora.consultarProductosMoraCache(contexto);
        if (responseMora.hayError()) {
            return Respuesta.estado("ERROR_MORA");
        }

        if (responseMora.codigo == 204) {
            return Respuesta.exito();
        }

        boolean respuestaDeuda = tieneDeuda(responseMora.objetos(),
                tarjetaCredito.esPrefijoVisa() + tarjetaCredito.cuenta());
        Objeto productoEnMora = responseMora.objetos().stream()
                .filter(prod -> prod.string("NumeroProducto").trim().startsWith("2" + tarjetaCredito.cuenta()))
                .findFirst().orElse(null);

        datos.set("estadoDeuda", estadoDeudaTC(respuestaDeuda));
        if (productoEnMora != null) {
            datos.set("tipoMora", productoEnMora.get("Tipo Mora"));
            datos.set("cta_id", productoEnMora.get("cta_id"));
            datos.set("numeroProducto", productoEnMora.get("NumeroProducto"));
        }
        respuesta.set("datos", datos);
        return respuesta;
    }

    private static boolean tieneDeuda(List<Objeto> objectos, String cuentaCliente) {
        for (Objeto item : objectos) {
            if (esTarjetaCredito(item.string("pro_cod").trim())
                    && esNumeroCuentaCliente(item.string("NumeroProducto").trim(), cuentaCliente)) {
                return true;
            }
        }
        return false;
    }

    private static String estadoDeudaTC(boolean tieneDeuda) {
        return tieneDeuda ? "CON_DEUDA" : "SIN_DEUDA";
    }

    private static boolean esTarjetaCredito(String codigoProducto) {
        return "203".equals(codigoProducto);
    }

    private static boolean esNumeroCuentaCliente(String numeroProducto, String numeroCuenta) {
        return numeroProducto.trim().equals(numeroCuenta);
    }

    public static Objeto fechasCierreVtoTarjetaCredito(ContextoHB contexto, TarjetaCredito tarjetaCredito,
                                                       ApiResponse resumenTC) {

        if(ConfigHB.bool("hb_prendido_proximo_cierre_consolidada")){
            return fechasCierreVtoTarjetaCreditoV2(tarjetaCredito);
        }

        Objeto fechas = new Objeto();
        String cierre = "";
        String vencimiento = "";
        try {

            ApiResponse responseResumen;
            if (resumenTC == null) {
                responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                        tarjetaCredito.numero());
            } else {
                responseResumen = resumenTC;
            }

            if (responseResumen.hayError()) { // si hay error le pongo las fechas que obtiene por defecto
                cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
            } else { // si no hay error obtengo las fechas de ultima de resumen cuenta
                vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
                        "yyyy-MM-dd", "dd/MM/yyyy", "");
                cierre = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre",
                        "yyyy-MM-dd", "dd/MM/yyyy", "");
                if (!vencimiento.isEmpty()) {
                    try {
                        Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
                        Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                        if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                            // si fecha actual es superior fechavencimiento ultima, se manda fecha proxima
                            vencimiento = responseResumen.date(
                                    "resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd",
                                    "dd/MM/yyyy", "");
                            cierre = responseResumen.date(
                                    "resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                                    "dd/MM/yyyy", "");
                        }
                    } catch (Exception e) { // evitamos excepcion y pones fecha por defecto del servicio tarjeta credito
                        cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                        vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
                    }
                }
            }
            return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
        } catch (Exception e) {
            return fechas.set("cierre", tarjetaCredito.fechaCierre("dd/MM/yyyy")).set("vencimiento",
                    tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));
        }
    }

    public static Respuesta stopDebit(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            String cuenta = contexto.parametros.string("cuenta");
            String codigo = "";

            if (cuenta.isEmpty()) {
                return Respuesta.parametrosIncorrectos();
            }

            TarjetaCredito tc = contexto.tarjetaCreditoTitular();
            contexto.parametros.set("idTarjetaCredito", tc.id());
            Respuesta respuestaEstadosCuenta = HBCuenta.estadosCuenta(contexto);
            String estadoTarjeta = respuestaEstadosCuenta.string("datos.estadoTarjeta");
            String estadoVencimiento = respuestaEstadosCuenta.string("datos.estadoVencimiento");
            contexto.parametros.set("idTarjetaCredito", null);

            if (!estadoTarjeta.equals("RESUMEN_LISTO") || estadoVencimiento.equals("HOY")) {
                respuesta.setEstado("50030");
                respuesta.set("descripcion", mensajesValidosStopDebitTC(codigo));
                return respuesta;
            }

            ApiResponse response = TarjetaCreditoService.stopDebit(contexto, cuenta);

            try {
                if (!response.hayError() && response.codigo == 200) {
                    if (response.string("retcode").equals("0")) {
                        codigo = "0";
                        enviarCorreoStopDebit(contexto);
                    }
                }

                codigo = !codigo.isEmpty() ? codigo : response.string("codigo");

            } catch (Exception e) {
            }

            respuesta.setEstado(codigo);
            respuesta.set("descripcion", mensajesValidosStopDebitTC(codigo));
            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta puedeStopDebit(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            String cuenta = contexto.parametros.string("cuenta");
            Boolean puede = true;

            if (cuenta.isEmpty()) {
                return Respuesta.parametrosIncorrectos();
            }

            TarjetaCredito tc = contexto.tarjetaCreditoTitular();
            contexto.parametros.set("idTarjetaCredito", tc.id());
            Respuesta respuestaEstadosCuenta = HBCuenta.estadosCuenta(contexto);
            String estadoTarjeta = respuestaEstadosCuenta.string("datos.estadoTarjeta");
            String estadoVencimiento = respuestaEstadosCuenta.string("datos.estadoVencimiento");
            contexto.parametros.set("idTarjetaCredito", null);

            if (!estadoTarjeta.equals("RESUMEN_LISTO") || estadoVencimiento.equals("HOY")) {
                puede = false;
            }

            respuesta.set("puede", puede);
            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    private static String mensajesValidosStopDebitTC(String codigo) {
        String operacion_ok = ConfigHB.string("stop_debit_operacion_ok");
        String fuera_de_termino = ConfigHB.string("stop_debit_tc_fuera_de_termino");
        String ya_detenido = ConfigHB.string("stop_debit_tc_ya_detenido");
        String algun_error = ConfigHB.string("stop_debit_tc_error");

        switch (codigo) {
            case "0":
                return operacion_ok;
            case "50001":
                return algun_error;
            case "50005":
                return algun_error;
            case "50010":
                return algun_error;
            case "50020":
                return algun_error;
            case "50030":
                return fuera_de_termino;
            case "50040":
                return algun_error;
            case "50050":
                return algun_error;
            case "50060":
                return ya_detenido;
            case "50070":
                return fuera_de_termino;
            case "50080":
                return fuera_de_termino;
            default:
                return algun_error;
        }
    }

    public static void enviarCorreoStopDebit(ContextoHB contexto) {
        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST",
                "/v1/correoelectronico", contexto);
        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
        requestMail.body("para", contexto.persona().email());
        requestMail.body("plantilla", ConfigHB.string("doppler_stopDebit"));
        Objeto parametros = requestMail.body("parametros");
        parametros.set("Subject", "Detener Débito Automático");
        parametros.set("NOMBRE_CLIENTE", contexto.persona().nombre());
        Api.response(requestMail, new Date().getTime());
    }

    @SuppressWarnings("unchecked")
    public static Respuesta reposicionTDEnCurso(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        try {
            String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

            if (Objeto.anyEmpty(idTarjetaDebito)) {
                return Respuesta.parametrosIncorrectos();
            }

            if (RestPostventa.tieneSolicitudEnCurso(contexto, "7", new Objeto().set("idTarjetaDebito", idTarjetaDebito),
                    true)) {
                return Respuesta.estado("SOLICITUD_EN_CURSO").set("message", ConfigHB.string("message_en_curso"));
            }

            if (RestPostventa.tieneSolicitudEnCurso(contexto, "7",
                    new Objeto().set("idTarjetaDebito", contexto.tarjetaDebito(idTarjetaDebito).numero()), true)) {
                return Respuesta.estado("SOLICITUD_EN_CURSO").set("message", ConfigHB.string("message_en_curso"));
            }

            try {
                Respuesta trackeo = HBTrackeo.agregarTrackeoTarjetaDebito(contexto);
                if (!trackeo.hayError()) {

                    List<Objeto> piezasList = (List<Objeto>) trackeo.get("productsDelivery");

                    if (piezasList.isEmpty()) {
                        piezasList = (List<Objeto>) trackeo.objetos("productsDelivery");
                    }

                    for (int i = 0; i < piezasList.size(); ++i) {
                        Map<String, Object> pieza = (Map<String, Object>) piezasList.get(i);
                        if (pieza.get("pieceNumber").equals(contexto.tarjetaDebito(idTarjetaDebito).numero())) {
                            return Respuesta.estado("SOLICITUD_EN_CURSO").set("message",
                                    ConfigHB.string("message_en_curso"));
                        }
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
        }

        return respuesta;
    }

    public static Respuesta puedePedirCambioCartera(ContextoHB contexto) {
        try {
            String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
            TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
            Objeto obj = new Objeto();
            obj.set("tcNumero", tarjetaCredito.numero());

            if (RestPostventa.tieneSolicitudEnCurso(contexto, "137", obj, true)) {
                return Respuesta.estado("SOLICITUD_EN_CURSO");
            }

            return RestPostventa.tieneMaximoCambiosCartera(contexto);

        } catch (Exception e) {
            return Respuesta.exito();
        }
    }

    public static Respuesta getCarterasTC(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        try {
            TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(contexto.parametros.string("idTarjetaCredito"));

            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }
            respuesta.set("carteras", carteras(tarjetaCredito.esHML(), tarjetaCredito.grupoCarteraTc()));

        } catch (Exception e) {
            return Respuesta.error();
        }
        return respuesta;
    }

    private static List<Objeto> carteras(Boolean esHml, String grupoCarteraTc) {
        List<Objeto> carteras = new ArrayList<>();
        carteras.add(crearCarteraItem("Entre el 1 y el 5", "2"));
        carteras.add(crearCarteraItem("Entre el 3 y el 9", "1"));

        if (!esHml) {
            carteras.add(crearCarteraItem("Entre el 13 y el 19", "4"));
            carteras.add(crearCarteraItem("Entre el 20 y el 26", "3"));
        }

        for (Objeto cartera : carteras) {
            if (cartera.get("opcion").equals(grupoCarteraTc)) {
                cartera.set("esActual", true);
                break;
            }
        }
        return carteras;
    }

    private static Objeto crearCarteraItem(String descripcion, String opcion) {
        Objeto item = new Objeto();
        item.set("descripcion", descripcion);
        item.set("opcion", opcion);
        item.set("esActual", false);
        return item;
    }

    public static Respuesta crearCasoCambioCartera(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(contexto.parametros.string("idTarjetaCredito"));
        String valorCarteraNueva = contexto.parametros.string("carteraNueva");
        try {
            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }

            if (valorCarteraNueva == null) {
                return Respuesta.estado("VALOR_CARTERA_REQUERIDO");
            }

            List<Objeto> carteras = carteras(tarjetaCredito.esHML(), tarjetaCredito.grupoCarteraTc());

            Optional<Objeto> carteraValida = carteras.stream()
                    .filter(cartera -> cartera.get("opcion").equals(valorCarteraNueva)
                            && Boolean.FALSE.equals(cartera.get("esActual")))
                    .findFirst();

            if (!carteraValida.isPresent()) {
                return Respuesta.estado("VALOR_CARTERA_NO_VALIDO");
            }

            ApiResponse response = RestPostventa.cambioCarteraTC(contexto, valorCarteraNueva, tarjetaCredito);

            if (response == null || response.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(response);

            if (numeroCaso.isEmpty()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }
            return respuesta.set("ticket", numeroCaso);
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta mostarOpcionCambioCartera(ContextoHB contexto) {
        try {
            String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
            TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

            if (Objeto.anyEmpty(idTarjetaCredito)) {
                return Respuesta.parametrosIncorrectos();
            }

            if (tarjetaCredito == null) {
                return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
            }

            if (!tarjetaCredito.idEstado().equals("20")) {
                return Respuesta.estado("OPCION_NO_HABILITADA");
            }

            if (!tarjetaCredito.esTitular()) {
                return Respuesta.estado("OPCION_NO_HABILITADA");
            }

            ApiResponse responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                    tarjetaCredito.numero());

            if (responseResumen.hayError()) {
                return Respuesta.exito();
            }

            if (responseResumen.codigo == 204) {
                return Respuesta.exito();
            }

//			Date vencimiento = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
//			Date cierre = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
//			Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
//
//			if (fechaActual.compareTo(vencimiento) < 0 && fechaActual.compareTo(cierre) > 0) {
//				return Respuesta.estado("OPCION_NO_HABILITADA");
//			}

            try {
                Respuesta respuestaEstado = HBCuenta.estadosCuenta(contexto);
                if (respuestaEstado.get("datos.estadoTarjeta") != null) {
                    return Respuesta.estado("OPCION_NO_HABILITADA");
                }
            } catch (Exception e) {
            }

            return Respuesta.exito();
        } catch (Exception e) {
            return Respuesta.error();
        }

    }

    public static Respuesta puedeCrearSolicitudTarjetaCreditoAdicional(ContextoHB contexto) {
        try {
            TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();

            // LLAMADAS A SERVICIOS EN PARALELO
            Futuro<Respuesta> futuroEstadosCuenta = new Futuro<>(() -> HBCuenta.estadosCuenta(contexto));
            Futuro<Objeto> futuroDomicilioPostal = new Futuro<>(
                    () -> RestPersona.domicilioPostal(contexto, contexto.persona().cuit()));

            Objeto domicilioAnterior = futuroDomicilioPostal.get();
            Date fechaModificacionDomicilioAnterior = domicilioAnterior.date("fechaModificacion", "yyyy-MM-dd");
            Boolean comprobar = fechaModificacionDomicilioAnterior != null
                    && new Date().getTime() - fechaModificacionDomicilioAnterior.getTime() < (10 * 24 * 60 * 60 * 1000);

            Futuro<String> futuroNDiasHabiles = new Futuro<>(() -> null);
            if (comprobar) {
                String fechaModificacion = domicilioAnterior.string("fechaModificacion");
                futuroNDiasHabiles = new Futuro<>(() -> Util.calcularFechaNdiasHabiles(contexto, fechaModificacion, 3));
            }

            Respuesta respuesta = futuroEstadosCuenta.get();
            String fechacambioPrisma = futuroNDiasHabiles.get();
            // FIN LLAMADAS A SERVICIOS

            if (Util.isfueraHorario(ConfigHB.integer("procesos_horaInicio", 7),
                    ConfigHB.integer("procesos_horaFin", 22))) {
                return Respuesta.estado("FUERA_HORARIO");
            }

            if (!tarjetaCreditoTitular.idEstado().equals("20")) {
                return Respuesta.estado("TARJETA_CON_PROBLEMAS");
            }

            try {
                contexto.parametros.set("idTarjetaCredito", tarjetaCreditoTitular.id());
                if (!respuesta.hayError() && respuesta.objeto("datos").get("estadoMora") != null) {
                    return Respuesta.estado("TARJETA_CON_PROBLEMAS");
                }
            } catch (Exception e) {
            }

            if (tarjetaCreditoTitular.esHML()) {
                return Respuesta.estado("TARJETA_HML");
            }

//			if (!tarjetaCreditoTitular.firmaContrato()) {
//				return Respuesta.estado("FALTA_FIRMA_CONTRATO");
//			}

            if (comprobar) {
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                Date fechacambioPrismaDate = Fecha.stringToDate(fechacambioPrisma, "yyyy-MM-dd");
                if (fechaActual.compareTo(fechacambioPrismaDate) <= 0) {
                    return Respuesta.estado("CAMBIO_DOMICILIO_PENDIENTE");
                }
            }

            return Respuesta.exito();
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta AdicionalEnCurso(ContextoHB contexto) {
        try {
            String documento = contexto.parametros.string("documento");
            String fechaNacimiento = contexto.parametros.string("fechaNacimiento", "");
            String idNacionalidad = contexto.parametros.string("idNacionalidad", "80");
            String idTramite = contexto.parametros.string("idTramite", "");

            if (Objeto.anyEmpty(documento)) {
                return Respuesta.parametrosIncorrectos();
            }

            Respuesta tarjetas = tarjetasCreditoPropias(contexto);

            if (tarjetas.hayError()) {
                return Respuesta.error();
            }

            for (Objeto tca : tarjetas.objetos("tarjetasCreditoAdicionales")) {
                if (tca.string("documento").equals(documento)) {
                    return Respuesta.estado("ADICIONAL_VIGENTE");
                }
            }

            for (Objeto tca : tarjetas.objetos("tarjetasCreditoAdicionalesEnCurso")) {
                if (tca.string("documento").equals(documento)) {
                    return Respuesta.estado("EN_CURSO");
                }
            }

            if (ConfigHB.esDesarrollo()) {

                String cuit = "";

                Respuesta personaRespuesta = HBPersona.persona(contexto);

                if (personaRespuesta.hayError() || personaRespuesta.objeto("persona").string("cuit").isEmpty()) {
                    cuit = HBPersona.obtenerCuitDeDocumento(contexto);
                } else {
                    cuit = personaRespuesta.objeto("persona").string("cuit");
                }

                if (cuit.isEmpty()) {
                    contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
                    return Respuesta.estado("PERSONA_NO_ENCONTRADA");
                }

                String cobisRelacion = "";
                Api.eliminarCache(contexto, "PersonasRelacionadas", cuit);
                ApiResponse responsePersonaEspecifica = RestPersona.consultarPersonaEspecifica(contexto, cuit);
                cobisRelacion = responsePersonaEspecifica.string("idCliente");

                if (!responsePersonaEspecifica.string("idSexo").equalsIgnoreCase(contexto.parametros.string("idSexo"))
                        || cobisRelacion.isEmpty()) {
                    contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
                    return Respuesta.estado("PERSONA_NO_ENCONTRADA");
                }

                if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
                        || responsePersonaEspecifica.string("idVersionDocumento").isEmpty()
                        || responsePersonaEspecifica.string("idEstadoCivil").isEmpty()
                        || responsePersonaEspecifica.string("fechaNacimiento").isEmpty()
                        || responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {

                    Respuesta respuestaRenaper = new Respuesta();

                    if (idTramite.isEmpty() || idTramite == null) {
                        contador(contexto, "ADCIONAL_TC_PERS_SIN_DATO");
                        return Respuesta.estado("PERSONA_REQUIERE_DATO");
                    }

                    respuestaRenaper = HBPersona.buscarPersonaRenaper(contexto);

                    Objeto datos = new Objeto();

                    if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
                            || responsePersonaEspecifica.string("idNacionalidad").isEmpty()) {
                        datos.set("idPaisNacimiento", idNacionalidad);
                        datos.set("idNacionalidad", idNacionalidad);
                        datos.set("idPaisResidencia", respuestaRenaper.string("idPaisResidencia"));
                    }

                    if (responsePersonaEspecifica.string("idVersionDocumento").isEmpty()) {
                        datos.set("idVersionDocumento", respuestaRenaper.string("ejemplar").toUpperCase());
                    }

                    if (responsePersonaEspecifica.string("idEstadoCivil").isEmpty()) {
                        datos.set("idEstadoCivil", "S");
                    }

                    if (responsePersonaEspecifica.string("fechaNacimiento").isEmpty()) {
                        fechaNacimiento = respuestaRenaper.string("fechaNacimiento");
                        datos.set("fechaNacimiento", fechaNacimiento + "T00:00:00");
                    }

                    if (responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {
                        datos.set("idSituacionImpositiva", "CONF");
                    }
                    RestPersona.actualizarPersona(contexto, datos, cuit);
                }

                if (responsePersonaEspecifica.string("etagDomicilios").equalsIgnoreCase("-1")) {
                    Objeto domicilioTitularLegal = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
                    RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularLegal, "LE");
                    Objeto domicilioTitularPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
                    RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularPostal, "DP");
                }
            }

            return Respuesta.exito();
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    private static Boolean chequeoNuevaSolicitud(ContextoHB contexto, Objeto solicitud) {
        Boolean enCurso = false;
        try {
            if ("F".equalsIgnoreCase(solicitud.string("estado"))) {
                Date fechaActual = dateStringToDate(new Date(), "yyyy-MM-dd");
                String fechaTopeProcesosBatch = Util.calcularFechaNdiasHabiles(contexto,
                        solicitud.string("fechaDeAlta"), 3);
                Date fechaTopeProcesosBatchDate = Fecha.stringToDate(fechaTopeProcesosBatch, "yyyy-MM-dd");
                if (fechaActual.compareTo(fechaTopeProcesosBatchDate) <= 0) {
                    enCurso = true;
                }
            }
            return enCurso;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Respuesta promoNoImpactadaTD(ContextoHB contexto) {
        try {
            String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
            String comentario = contexto.parametros.string("comentario");
            String idCuenta = contexto.parametros.string("idCuenta");
            String rubro = contexto.parametros.string("rubro");
            String nombreComercio = contexto.parametros.string("nombreComercio");
            String fecha = contexto.parametros.string("fecha");
            String monto = contexto.parametros.string("monto");
            String moneda = contexto.parametros.string("moneda");
            Objeto comprobante = contexto.parametros.objeto("comprobante", null);

            if (Objeto.anyEmpty(idTarjetaDebito)) {
                return Respuesta.parametrosIncorrectos();
            }

            Respuesta respuesta = new Respuesta();
            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);
//			String idCuenta = "";
//			List<Cuenta> cuentas = contexto.cuentas();
//			for (Cuenta cuenta : cuentas) {
//				if (!idCuenta.isEmpty()) {
//					continue;
//				}
//				ApiResponse response = CuentasService.getTarjetaDebito(contexto, cuenta);
//				for (Objeto tarjeta : response.objetos()) {
//					if (idTarjetaDebito.equals(tarjeta.string("numeroTarjeta"))) {
//						idCuenta = cuenta.id();
//						continue;
//					}
//				}
//			}

            if (idCuenta.isEmpty()) {
                return Respuesta.estado("CUENTA_NO_ASOCIADA");
            }
            Cuenta cuenta = contexto.cuenta(idCuenta);

            switch (rubro) {
                case "Otros": {
                    rubro = "N/A";
                    break;
                }
                case "Delivery": {
                    rubro = "Rappi";
                    break;
                }
                case "Farmacias y perfumerías": {
                    rubro = "Farmacias";
                    break;
                }
                default:
                    break;
            }

            Objeto movimiento = new Objeto();
            movimiento.set("nombreComercio", nombreComercio);
            movimiento.set("fecha", fecha);
            movimiento.set("monto", monto);
            movimiento.set("moneda", moneda);
            movimiento.set("rubro", rubro);
            movimiento.set("comentario", comentario);

            ApiResponse response = RestPostventa.promoNoImpactadaTD(contexto, td, cuenta, movimiento);

            if (response == null || response.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(response);

            if (numeroCaso.isEmpty()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            if (comprobante != null) {

                if (comprobante.string("archivo").isEmpty()) {
                    respuesta.set("adjunto", "ERROR_COMPROBANTE_VACIO");
                } else {

                    ApiResponse resCrearNota = RestPostventa.crearNota(contexto, numeroCaso, comprobante);
                    if (resCrearNota == null) {
                        respuesta.set("adjunto", "ERROR_CREAR_NOTA");
                    } else {

                        if (resCrearNota.hayError()) {
                            respuesta.set("adjunto", resCrearNota.get("Errores"));
                        } else {
                            respuesta.set("adjunto", "OK");
                        }
                    }
                }
            }

            return respuesta.set("ticket", numeroCaso);

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta crearCasoDesconomientoConsumo(ContextoHB contexto) {
        try {
            String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
            String idCuenta = contexto.parametros.string("idCuenta");
            String fecha = contexto.parametros.string("fechaReclamo");
            List<Objeto> movimientos = contexto.parametros.objetos("movimientos");
            Objeto comprobante = contexto.parametros.objeto("comprobante", null);
            String comentario = contexto.parametros.string("comentario");

            if (Objeto.anyEmpty(idTarjetaDebito)) {
                return Respuesta.parametrosIncorrectos();
            }

            Respuesta respuesta = new Respuesta();
            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);

            if (idCuenta.isEmpty()) {
                return Respuesta.estado("CUENTA_NO_ASOCIADA");
            }

            Cuenta cuenta = contexto.cuenta(idCuenta);

            ApiResponse response = RestPostventa.desconocimientoConsumo(contexto, td, cuenta, fecha, movimientos,
                    comentario);

            if (response == null || response.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(response);

            if (numeroCaso.isEmpty()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            if (comprobante != null) {

                if (comprobante.string("archivo").isEmpty()) {
                    respuesta.set("adjunto", "ERROR_COMPROBANTE_VACIO");
                } else {

                    ApiResponse resCrearNota = RestPostventa.crearNota(contexto, numeroCaso, comprobante);
                    if (resCrearNota == null) {
                        respuesta.set("adjunto", "ERROR_CREAR_NOTA");
                    } else {

                        if (resCrearNota.hayError()) {
                            respuesta.set("adjunto", resCrearNota.get("Errores"));
                        } else {
                            respuesta.set("adjunto", "OK");
                        }
                    }
                }
            }

            return respuesta.set("ticket", numeroCaso);

        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    private static void contador(ContextoHB contexto, String nemonico) {
        try {
            contexto.parametros.set("nemonico", nemonico);
            Util.contador(contexto);
        } catch (Exception e) {
        }
    }

    public static Objeto fechaCierreParametrica(Date fechaProximoCierre, String carteraTC) {
        String cierre = "";
        String vencimiento = "";
        Objeto fechas = new Objeto();
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaProximoCierre);
            Integer periodo = calendar.get(Calendar.MONTH) + 1;
            Integer anio = calendar.get(Calendar.YEAR);
            SqlResponse sqlCartera = TarjetaCreditoService.obtenerFechaCierreVtoCartera(carteraTC, periodo, anio);

            if (sqlCartera.registros.size() > 0) {
                cierre = Fecha.formato(sqlCartera.registros.get(0).string("cierre"), "dd/MM/yyyy", "yyyy-MM-dd");
                vencimiento = Fecha.formato(sqlCartera.registros.get(0).string("vencimiento"), "dd/MM/yyyy",
                        "yyyy-MM-dd");
            }
        } catch (Exception e) {
        }
        return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea).
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static Respuesta pausarTarjetaDebito(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return Respuesta.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return Respuesta.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return Respuesta.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponse apiResponse = pausarTarjetaDebitoLink(contexto,
                !ConfigHB.esDesarrollo() ? tarjetaDebito.numero() : "4998590328911801", tipoOperacion);

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        ApiResponse apiResponseCore = pausarTarjetaDebito(contexto, tarjetaDebito,
                tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()));

        if (apiResponseCore.hayError()) {
            apiResponse = pausarTarjetaDebitoLink(contexto,
                    !ConfigHB.esDesarrollo() ? tarjetaDebito.numero() : "4998590328911801",
                    tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()) ? TipoOperacionPausado.HABILITAR.name()
                            : TipoOperacionPausado.BLOQUEAR.name());

            if (apiResponse.hayError())
                return castearRespuestaPausado(apiResponse);

            return castearRespuestaPausado(apiResponseCore);
        }

        return Respuesta.exito();
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea) por
     * contingencia con Link.
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static Respuesta pausarTarjetaDebitoLinkContingencia(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return Respuesta.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return Respuesta.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return Respuesta.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponse apiResponse = pausarTarjetaDebitoLink(contexto, tarjetaDebito.numero(), tipoOperacion);

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        return Respuesta.exito();
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea) por
     * contingencia con Core.
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static Respuesta pausarTarjetaDebitoCoreContingencia(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return Respuesta.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return Respuesta.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return Respuesta.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponse apiResponse = pausarTarjetaDebito(contexto, tarjetaDebito,
                tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()));

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        return Respuesta.exito();
    }

    /**
     * Determino el error que devolvió la api
     */
    private static Respuesta errorDatosIncorrectos(String mensaje) {
        String estado = ERROR_PAUSADO;
        try {
            JsonObject errorJson = new Gson().fromJson(mensaje, JsonObject.class);
            if (errorJson != null && errorJson.has("codigo"))
                estado = errorJson.get("codigo").getAsString();
        } catch (JsonParseException e) {
        }
        return Respuesta.estado(estado);

    }

    private static ApiResponse pausarTarjetaDebito(ContextoHB contexto, TarjetaDebito tarjetaDebito, boolean pausar) {
        return TarjetaDebitoService.pausadoTarjetaDebito(contexto, tarjetaDebito, pausar);
    }

    private static ApiResponse pausarTarjetaDebitoLink(ContextoHB contexto, String numeroTarjetaDebito,
                                                       String tipoOperacion) {

        ApiRequest request = Api.request("LinkPausadoTarjetaDebito", "link", "POST", URL_PAUSADO_DEBITO, contexto);
        request.body("numeroTarjeta", numeroTarjetaDebito);
        request.body("tipoOperacion", tipoOperacion);
        request.headers.put(X_TIMESTAMP_HEADER, new SimpleDateFormat(X_TIMESTAMP_FORMAT).format(new Date()));
        request.permitirSinLogin = true;

        return Api.response(request);
    }

    private static Respuesta castearRespuestaPausado(ApiResponse apiResponse) {
        return apiResponse.codigo == 400 ? errorDatosIncorrectos(apiResponse.string(MENSAJE_DESARROLLADOR))
                : Respuesta.estado(ERROR_PAUSADO);
    }

    public static ApiResponse obtenerTitularidadTd(ContextoHB contexto, String numeroTarjeta, String extendido) {
        ApiRequest apiRequest = Api.request("Consulta_TitularidadTd", "tarjetasdebito", "GET",
                "/v1/tarjetaDebitoTitular", contexto);

        apiRequest.query("tarjeta", numeroTarjeta);
        apiRequest.query("extendido", extendido);

        return Api.response(apiRequest);

    }

    public static Object estadoBajaTarjeta(ContextoHB ctx) {

        if (HBAplicacion.funcionalidadPrendida(ctx.idCobis(), "prendido_baja_tc_con_oferta_crm")) {
            return estadoBajaTarjetaV2(ctx);
        }

        String idTarjeta = ctx.parametros.string("idTarjeta");

        if (Objeto.anyEmpty(idTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = ctx.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return Respuesta.estado("ERROR");
        }

        if (tarjetaCredito.esHML() && !tarjetaCredito.esTitular()) {
            return Respuesta.estado("ERROR");
        }

        Objeto tcObj = new Objeto();
        tcObj.set("tcNumero", tarjetaCredito.numero());
        tcObj.set("idPaquete", tarjetaCredito.idPaquete());

        String estado = tarjetaCredito.idEstado();
        Boolean tienePaquete = !"".equals(tarjetaCredito.idPaquete());

        if (tienePaquete && estado.equals("20")) {
            if (RestPostventa.tieneSolicitudEnCurso(ctx, "BAJA_PAQUETES", tcObj, true)) {
                return Respuesta.estado("ESPERAR_ASESOR");
            }
            return Respuesta.exito();
        }

        Boolean esRetencion = HBProducto.esTarjetaRetension(ctx);
        String tipiTcRetencion = "";

        if (!tarjetaCredito.esTitular()) {
            tipiTcRetencion = "BAJA_TC_ADICIONAL_PEDIDO";
            if (RestPostventa.tieneSolicitudEnCurso(ctx, tipiTcRetencion, tcObj, true)) {
                return Respuesta.estado("ESPERAR_ASESOR");
            }
        }

        if(estado.equals("25")){
            //TODO: CodigoTipificacion 9017P = BAJA TC HML - CANAL AUTOGESTIVO (Baja Directa)
            if (tarjetaCredito.esHML()) tipiTcRetencion = "9017P";

            //TODO: BAJA TC - CANAL AUTOGESTIVO (Baja Directa)
            if (!tarjetaCredito.esHML()) tipiTcRetencion = "BAJATCAUTOGESTIVO";
        }

        if(estado.equals("20")){
            if (tarjetaCredito.esHML() && esRetencion) tipiTcRetencion = "BAJATCHML_PEDIDO";
            //TODO: CodigoTipificacion 9017P = BAJA TC HML - CANAL AUTOGESTIVO (Baja Directa)
            if (tarjetaCredito.esHML() && !esRetencion) tipiTcRetencion = "9017P";

            if (!tarjetaCredito.esHML() && esRetencion) tipiTcRetencion = "BAJATC_PEDIDO";
            //TODO: BAJA TC - CANAL AUTOGESTIVO (Baja Directa)
            if (!tarjetaCredito.esHML() && !esRetencion) tipiTcRetencion = "BAJATCAUTOGESTIVO";
        }

        Boolean solicitudPendiente = RestPostventa.tieneSolicitudEnCurso(ctx, tipiTcRetencion, tcObj, true);
        if ((estado.equals("20") || estado.equals("25")) && solicitudPendiente) {
            return Respuesta.estado("EN_CURSO");
        }

        return Respuesta.exito();
    }

    public static Object estadoBajaTarjetaV2(ContextoHB contexto) {

        String idTarjeta = contexto.parametros.string("idTarjeta");
        String tipificacion =  "";
        Respuesta respuestaEstadoTC = new Respuesta();

        if (Objeto.anyEmpty(idTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Futuro<Respuesta> respuestaFuturoCRM = new Futuro<>(() -> HBProducto.campaniaCRM(contexto));
        Futuro<TarjetaCredito> tarjetaCreditoFuturo = new Futuro<>(() -> contexto.tarjetaCredito(idTarjeta));
        Futuro<Telefono> telefonoFuturo = new Futuro<>(() -> new Telefono(contexto, "E"));

        String numeroCelular = "("+telefonoFuturo.get().string("codigoArea","")+")"
                +" "+telefonoFuturo.get().string("prefijo","")
                +" "+telefonoFuturo.get().string("caracteristica","")
                +" "+telefonoFuturo.get().string("numero","");

        if (respuestaFuturoCRM.get() == null) {
            return Respuesta.estado("ERROR");
        }

        Boolean tieneCampaniaCRM = respuestaFuturoCRM.get().bool("tieneCampania", false);

        if (tarjetaCreditoFuturo.get() == null) {
            return Respuesta.estado("ERROR");
        }

        String estado = tarjetaCreditoFuturo.get().idEstado();
        Boolean esHML = tarjetaCreditoFuturo.get().esHML();
        Boolean esTitular =  tarjetaCreditoFuturo.get().esTitular();
        String idPaquete =  tarjetaCreditoFuturo.get().idPaquete();

        Objeto tcObj = new Objeto();
        tcObj.set("tcNumero", tarjetaCreditoFuturo.get().numero()); //TODO: tarjetaCredito.numero() ver si es el idTarjeta que viene por parametro
        tcObj.set("idPaquete", idPaquete);

        if(estado.equals("25")){
            if(esHML) tipificacion = "9017P"; //TODO: BAJA TC HML - CANAL AUTOGESTIVO
            if(!esHML) tipificacion = "BAJATCAUTOGESTIVO";
        }

        if(!idPaquete.isEmpty() && estado.equals("20")) tipificacion = "BAJA_PAQUETES";

        if(estado.equals("20")) {
            if (idPaquete.isEmpty() && esTitular) {

                if (esHML && tieneCampaniaCRM) tipificacion = "BAJATCHML_PEDIDO";

                if (esHML && !tieneCampaniaCRM) tipificacion = "9017P"; //TODO: BAJA TC HML - CANAL AUTOGESTIVO

                if (!esHML && tieneCampaniaCRM) tipificacion = "BAJATC_PEDIDO";

                if (!esHML && !tieneCampaniaCRM) tipificacion = "BAJATCAUTOGESTIVO";

            }
        }

        if(idPaquete.isEmpty() && !esTitular) tipificacion = "BAJA_TC_ADICIONAL_PEDIDO";

        Boolean solicitudPendiente = RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, tcObj, true);

        if ((estado.equals("20") || estado.equals("25")) && solicitudPendiente) {
            return Respuesta.estado("EN_CURSO");
        }

        respuestaEstadoTC.set("numeroCelular", numeroCelular);
        respuestaEstadoTC.set("esHML", tarjetaCreditoFuturo.get().esHML());
        respuestaEstadoTC.set("tieneCampaniaCRM", tieneCampaniaCRM);
        respuestaEstadoTC.set("idEstadoTarjeta", estado);
        respuestaEstadoTC.set("tipificacion", tipificacion);
        if(tieneCampaniaCRM && estado.equals("20")) respuestaEstadoTC.set("ofertas", respuestaFuturoCRM.get().objetos("ofertas"));

        return respuestaEstadoTC;
    }

    public static Object retensionBajaTarjeta(ContextoHB ctx) {

        String idTarjeta = ctx.parametros.string("idTarjeta");

        if (Objeto.anyEmpty(idTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = ctx.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return Respuesta.estado("ERROR");
        }

        if (tarjetaCredito.esHML() && !tarjetaCredito.esTitular()) {
            return Respuesta.estado("ERROR");
        }

        String estado = tarjetaCredito.idEstado();
        if (estado.equals("25")) {
            return Respuesta.estado("SIN_RETENSION");
        }

        Boolean tienePaquete = !"".equals(tarjetaCredito.idPaquete());
        if (tienePaquete) {
            return Respuesta.exito();
        }

        Boolean esRetension = HBProducto.esTarjetaRetension(ctx);
        if (esRetension) {
            return Respuesta.exito();
        }

        if (!esRetension) {
            return Respuesta.estado("SIN_RETENSION");
        }

        return Respuesta.exito();
    }

    public static Object bajaDirectaTarjetaCredito(ContextoHB ctx) {

        String idTarjeta = ctx.parametros.string("idTarjeta");

        if (Objeto.anyEmpty(idTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = ctx.tarjetaCredito(idTarjeta);
        
        if (tarjetaCredito == null) {
            return Respuesta.error();
        }

        if (!tarjetaCredito.esTitular()) {
            return HBProducto.bajaTarjetaCredito(ctx);
        }

        Boolean esRetencion = HBProducto.esTarjetaRetension(ctx);
        String estado = tarjetaCredito.idEstado();
        if (esRetencion && estado.equals("20")) {
            if (tarjetaCredito.esHML() && tarjetaCredito.esTitular()) {
                return HBProducto.bajaTarjetaHML(ctx);
            }
            return HBProducto.bajaTarjetaCredito(ctx);
        }

        Boolean tienePaquete = !"".equals(tarjetaCredito.idPaquete());
        if (tienePaquete && estado.equals("20")) {
            return Respuesta.error();
        }

        if (tarjetaCredito.esHML() && !tarjetaCredito.esTitular()) {
            return Respuesta.error();
        }

        ApiResponse resReclamo = null;

        try {
            if (tarjetaCredito.esHML()) {
                //TODO: CodigoTipificacion 9017P = BAJA TC HML - CANAL AUTOGESTIVO
                resReclamo = RestPostventa.bajaTarjetaHML_AUTOGESTIVO(ctx, "9017P", tarjetaCredito, "");
            }

            if (!tarjetaCredito.esHML()) {
                resReclamo = RestPostventa.bajaDirectaTarjetaCredito(ctx, "BAJATCAUTOGESTIVO", tarjetaCredito, "");
            }

            if (resReclamo == null || resReclamo.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(resReclamo);
            if (numeroCaso.isEmpty()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            SqlResponse sqlResponse = HBProducto.insertarReclamo(ctx, "BAJA_RECL", tarjetaCredito.numero(), "ATC",
                    tarjetaCredito.sucursal(), "");
            if (sqlResponse.hayError) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            return Respuesta.exito().set("numeroCaso", numeroCaso);

        } catch (Exception e) {

            return Respuesta.error();
        }
    }

    public static Respuesta verificarTarjetaDebitoPausadaEnCuenta(Cuenta cuenta, ContextoHB contexto) {
        boolean pausada = false;
        for (TarjetaDebito td : contexto.tarjetasDebito().stream()
                .filter(t -> t.cuentasAsociadas().stream().filter(c -> c.numero().equals(cuenta.numero())).count() != 0)
                .collect(Collectors.toList())) {
            ApiResponse apiResponseTitularidad = null;
            if (!pausada) {
                apiResponseTitularidad = HBTarjetas.obtenerTitularidadTd(contexto, td.numero(), "N");
                if (!apiResponseTitularidad.hayError()
                        && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                        && apiResponseTitularidad.objetos("collection1").size() != 0
                        && apiResponseTitularidad.objetos("collection1").get(0).get("Pausada").equals("S"))
                    pausada = true;
            }
        }
        return pausada ? Respuesta.estado("TARJETA_PAUSADA") : null;
    }

    public static Respuesta verificarTarjetaDebitoPausada(TarjetaDebito tarjetaDebito, ContextoHB contexto) {
        ApiResponse apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
        if (!apiResponseTitularidad.hayError() && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                && apiResponseTitularidad.objetos("collection1").size() != 0
                && apiResponseTitularidad.objetos("collection1").get(0).get("Pausada").equals("S"))
            return Respuesta.estado("TARJETA_PAUSADA");

        return null;
    }

    /**
     * Realiza el "pausado" de Tarjeta de Crédito (Habilita o Bloquea).
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static Respuesta pausarTarjetaCredito(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaCredito, tipoOperacion))
            return Respuesta.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausadoCredito.class, tipoOperacion))
            return Respuesta.estado(TIPO_OPERACION_INVALIDA);

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (Objeto.anyEmpty(tarjetaCredito))
            return Respuesta.estado(NO_EXISTE_TARJETA_CREDITO);

        ApiResponse apiResponse = TarjetaCreditoService.pausarTarjeta(contexto, tarjetaCredito, EnumUtils.getEnum(TipoOperacionPausadoCredito.class, tipoOperacion));

        if(apiResponse.codigo == 204)
            return Respuesta.exito();

        Respuesta respuesta = manejoErrorPausado(apiResponse);
        if(!Objeto.empty(respuesta))
            return respuesta;

        return Respuesta.exito();
    }

    /**
     * Retorna si está Pausada la Tarjeta de Crédito
     *
     * @param contexto
     * @return respuesta true si está pausada.
     */
    public static Respuesta estadoTarjetaCredito(ContextoHB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaCredito))
            return Respuesta.parametrosIncorrectos();

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (Objeto.anyEmpty(tarjetaCredito))
            return Respuesta.estado(NO_EXISTE_TARJETA_CREDITO);

        ApiResponse apiResponse = TarjetaCreditoService.detallePausadoTarjeta(contexto, tarjetaCredito);

        Respuesta respuesta = manejoErrorPausado(apiResponse);
        if(!Objeto.empty(respuesta))
            return respuesta;

        return Respuesta.exito("pausada", apiResponse.objetos().get(0).string("value").equals("DISABLED"));
    }

    private static Respuesta manejoErrorPausado(ApiResponse apiResponse){
        if (apiResponse.hayError()) {
            if (apiResponse.codigo == 400 || apiResponse.codigo == 504)
                return Respuesta.estado(ERROR_ESTADO_TARJETA_PAUSADO);
            return Respuesta.estado(ERROR_PAUSADO);
        }

        if (apiResponse.objetos().isEmpty())
            return Respuesta.estado(ERROR_ESTADO_TARJETA_PAUSADO);

        return null;
    }

    public static boolean prendidoPausadoTarjetaCredito(ContextoHB contexto) {
        return HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_pausado_tc");
    }

    public static Objeto fechasCierreVtoTarjetaCreditoV2(TarjetaCredito tarjetaCredito) {
        Objeto fechas = new Objeto();
        String cierre = "";
        String vencimiento = "";
        try {

            vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
            cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");

            if (!vencimiento.isEmpty()) {
                Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                    vencimiento = "";
                    cierre = tarjetaCredito.fechaProximoCierre("dd/MM/yyyy");
                }
            }

            return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
        } catch (Exception e) {
            return fechas.set("cierre", tarjetaCredito.fechaCierre("dd/MM/yyyy")).set("vencimiento",
                    tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));
        }
    }
    
    public static Respuesta solicitarImpresion(ContextoHB contexto) {
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjeta);
		String numeroCuenta = tarjetaCredito.cuenta();
		String idTarjeta = tarjetaCredito.id();
		
		if (Objeto.anyEmpty(numeroCuenta, numeroTarjeta)) {
			return Respuesta.parametrosIncorrectos();
		}
       
		ApiResponse apiResponse = TarjetaCreditoService.solicitarReimpresion(contexto,numeroCuenta,numeroTarjeta, idTarjeta);
		if (apiResponse.hayError()) {
			if(apiResponse.get("codigo").equals("50050")) {
				return Respuesta.estado("POSEE_SOLICITUD");
			}
			return Respuesta.error();
		}
		
			try {
				
				Objeto domicilio = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
				
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
//				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				parametros.set("HORA", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
				parametros.set("CANAL", "Home Banking");
				parametros.set("CALLE", domicilio.string("calle"));
				parametros.set("NUMERO",domicilio.string("numero"));
				parametros.set("PISO", domicilio.string("piso"));
				parametros.set("DEPARTAMENTO", domicilio.string("departamento"));
				parametros.set("PROVINCIA", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));

				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_imprimir_tc"), parametros));
			}
			catch(Exception e) {
				
			}


        return Respuesta.exito();
    }

    public static Respuesta errorGenerandoResumen(ContextoHB contexto) {
        try {
            TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
            if (tarjetaCredito == null || contexto.persona().esMenor()) {
                return Respuesta.error();
            }

            String cuit = contexto.persona().cuit();
            ApiResponse response = TarjetaCreditoService.ultimaLiquidacion(contexto, tarjetaCredito.numero(), cuit);

            if (response.hayError()) {
                return Respuesta.error().set("estado", "ERROR_RESUMEN");
            }

            return Respuesta.exito();
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta avisarViajeExterior(ContextoHB contexto) {
        String nroTarjeta = contexto.parametros.string("nroTarjeta", null);

        if (Objeto.anyEmpty(nroTarjeta)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiResponse apiResponse = TarjetaDebitoService.avisarViajeExterior(contexto,nroTarjeta);

        return Respuesta.exito();
    }

    public static Objeto fechasCierreVtoTarjetaCreditoV4(ContextoHB contexto, TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito,
                                                         ApiResponse resumenTC) {

        if(ConfigHB.bool("hb_prendido_proximo_cierre_consolidada")){
            return fechasCierreVtoTarjetaCreditoV4(tarjetaCredito);
        }

        Objeto fechas = new Objeto();
        String cierre = "";
        String vencimiento = "";
        try {

            ApiResponse responseResumen;
            if (resumenTC == null) {
                responseResumen = TarjetaCreditoService.resumenCuentaV2(contexto, tarjetaCredito.cuenta,
                        tarjetaCredito.numero);
            } else {
                responseResumen = resumenTC;
            }

            if (responseResumen.hayError()) { // si hay error le pongo las fechas que obtiene por defecto
                cierre = tarjetaCredito.cierreActual;
                vencimiento = tarjetaCredito.fechaVencActual;
            } else { // si no hay error obtengo las fechas de ultima de resumen cuenta
                vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
                        "yyyy-MM-dd", "dd/MM/yyyy", "");
                cierre = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre",
                        "yyyy-MM-dd", "dd/MM/yyyy", "");
                if (!vencimiento.isEmpty()) {
                    try {
                        Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
                        Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                        if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                            // si fecha actual es superior fechavencimiento ultima, se manda fecha proxima
                            vencimiento = responseResumen.date(
                                    "resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd",
                                    "dd/MM/yyyy", "");
                            cierre = responseResumen.date(
                                    "resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                                    "dd/MM/yyyy", "");
                        }
                    } catch (Exception e) { // evitamos excepcion y pones fecha por defecto del servicio tarjeta credito
                        cierre = tarjetaCredito.cierreActual;
                        vencimiento = tarjetaCredito.fechaVencActual;
                    }
                }
            }
            return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
        } catch (Exception e) {
            return fechas.set("cierre", tarjetaCredito.cierreActual).set("vencimiento",
                    tarjetaCredito.fechaVencActual);
        }
    }

    public static Objeto fechasCierreVtoTarjetaCreditoV4(TarjetasCreditoV4.TarjetaCreditoV4 tarjetaCredito) {
        Objeto fechas = new Objeto();
        String cierre = "";
        String vencimiento = "";
        try {

            vencimiento = tarjetaCredito.fechaVencActual;
            cierre = tarjetaCredito.cierreActual;

            if (!vencimiento.isEmpty()) {
                Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                    vencimiento = "";
                    cierre = tarjetaCredito.fechaProximoCierre;
                }
            }

            return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
        } catch (Exception e) {
            return fechas.set("cierre", tarjetaCredito.cierreActual).set("vencimiento",
                    tarjetaCredito.fechaVencActual);
        }
    }

}
