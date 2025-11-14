package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.SesionMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.*;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class MBCuenta {

    public static RespuestaMB consolidadaCuentas(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        Boolean filtrarSoloPesos = contexto.parametros.bool("filtrarSoloPesos", false);
        Boolean buscarEstadoVendedorCoelsa = contexto.parametros.bool("buscarEstadoVendedorCoelsa", false);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        Futuro<Boolean> cambioDetectadoNormativo = new Futuro<>(() -> RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false));
        Futuro<Boolean> futuroEnMora = new Futuro<>(() -> RestContexto.enMora(contexto));

        Futuro<List<Cuenta>> cuentasFuture = new Futuro<>(contexto::cuentas);
        Futuro<ApiResponseMB> productosResponse = new Futuro<>(() -> ProductosService.productos(contexto));

        for (Objeto err : productosResponse.get().objetos("errores")) {
            if ("cuentas".equals(err.string("codigo"))) {
                return RespuestaMB.estado("ERROR_CONSOLIDADA");
            }
        }

        List<Cuenta> cuentasFiltradas = cuentasFuture.get()
                .stream()
                .filter(c -> idCuenta == null || idCuenta.isEmpty() || c.id().equals(idCuenta))
                .toList();

        Map<String, Futuro<Boolean>> mapaActiva = new HashMap<>();
        Map<String, Futuro<String>> mapaCBUs = new HashMap<>();
        Map<String, Futuro<String>> mapaAlias = new HashMap<>();

        for (Cuenta c : cuentasFiltradas) {
            if (buscarEstadoVendedorCoelsa) {
                mapaActiva.put(c.id(), new Futuro<>(() -> RestDebin.cuentaActivaVendedor(contexto, c)));
            }
            mapaCBUs.put(c.id(), new Futuro<>(() -> c.cbu()));
            mapaAlias.put(c.id(), new Futuro<>(() -> c.alias()));
        }

        for (Cuenta c : cuentasFiltradas) {
            if (buscarEstadoVendedorCoelsa) {
                try { mapaActiva.get(c.id()).get(); } catch (Exception ignored) {}
            }
            try { mapaCBUs.get(c.id()).get(); } catch (Exception ignored) {}
            try { mapaAlias.get(c.id()).get(); } catch (Exception ignored) {}
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto productos = new Objeto();
        Objeto adelantoExistente = new Objeto();

        for (Cuenta cuenta : cuentasFiltradas) {
            if (idCuenta == null || idCuenta.isEmpty() || cuenta.id().equals(idCuenta)) {
                Objeto item = new Objeto();
                item.set("id", cuenta.id());
                item.set("descripcionCorta", cuenta.descripcionCorta());
                item.set("ultimos4digitos", cuenta.ultimos4digitos());
                item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
                item.set("cbu", cuenta.cbu());
                item.set("cbuFormateado", cuenta.cbuFormateado());
                item.set("comentario", cuenta.comentario());
                item.set("alias", cuenta.alias());
                item.set("idMoneda", cuenta.idMoneda());
                item.set("simboloMoneda", cuenta.simboloMoneda());
                item.set("numero", cuenta.numero());
                item.set("saldo", cuenta.saldo());
                item.set("saldoFormateado", cuenta.saldoFormateado());
                item.set("acuerdo", cuenta.acuerdo());
                item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
                item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
                item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
                item.set("unipersonal", cuenta.unipersonal());
                item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
                item.set("paquetizada", !"".equals(cuenta.idPaquete()));
                if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
                    item.set("adelantoDisponible", cuenta.adelantoDisponible());
                    item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
                    item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
                    item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
                    item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
                    item.set("adelantoInteresesDevengadosFormateado", Formateador.importe(cuenta.adelantoInteresesDevengados()));
                    adelantoExistente = item;
                }
                item.set("permiteCuotificacion", !contexto.persona().esEmpleado() && !cambioDetectadoNormativo.get() && !futuroEnMora.get());
                if (buscarEstadoVendedorCoelsa) {
                    try {
                        Boolean activa = mapaActiva.get(cuenta.id()).get();
                        item.set("estadoVendedorCoelsa", activa ? "ACTIVA" : "INACTIVA");
                    } catch (Exception e) {
                        item.set("estadoVendedorCoelsa", "DESCONOCIDO");
                    }
                }

                if (filtrarSoloPesos) {
                    if (cuenta.simboloMoneda().equals("$") && !item.equals(adelantoExistente))
                        productos.add("cuentas", item);
                } else if (!item.equals(adelantoExistente)) {
                    productos.add("cuentas", item);
                }
            }
        }
        respuesta.set("cuentas", ConfigMB.bool("prendido_adelanto_bh") ? MBProducto.reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente) : productos.objetos("cuentas"));

        Objeto datosExtras = new Objeto();
        datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
        datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
        datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
        datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebito().size() == 1);
        respuesta.set("datosExtras", datosExtras);

        respuesta.objeto("cuentas").ordenar("descripcionCorta", "simboloMoneda", "id");
        return respuesta;
    }

    public static ApiResponseMB consolidadoMovimientosCuenta(String fechaDesde, String fechaHasta, Cuenta cuenta, ContextoMB contexto, Integer numeroPagina) {
        String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
        ApiRequestMB request = ApiMB.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
        request.path("idcuenta", cuenta.numero());
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("pendientes", "2");
        request.query("validactaempleado", "false");
        request.query("numeropagina", numeroPagina.toString());
        request.query("tipomovimiento", "T");
        request.query("orden", "D");

        ApiResponseMB response = ApiMB.response(request, cuenta.id());
        return response;
    }

    public static RespuestaMB consolidadoMovimientosCuentas(String fechaDesde, String fechaHasta, List<Cuenta> cuentas, ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();

        Boolean superaLimitePaginas = false;

        for (Cuenta cuenta : cuentas) {
            Integer numeroPagina = 1;
            Integer cantidadPaginas = 0;
            ApiResponseMB response = consolidadoMovimientosCuenta(fechaDesde, fechaHasta, cuenta, contexto, numeroPagina);
            if (!response.hayError() && !response.objetos().isEmpty()) {
                for (Objeto item : response.objetos()) {
                    if (!item.existe("cantPaginas")) {
                        ultimosMovimientos.add(movimientosXCuenta(item, cuenta));
                    } else {
                        cantidadPaginas = item.integer("cantPaginas");
                    }
                }
            }

            Integer maximaCantidadPaginas = ConfigMB.integer("mb_movimientos_cuentas_max_paginas", 15);

            List<Futuro<ApiResponseMB>> futuros = new ArrayList<>();
            while (numeroPagina < cantidadPaginas) {
                Integer numeroPaginaActual = ++numeroPagina;

                if (numeroPaginaActual > maximaCantidadPaginas) {
                    superaLimitePaginas = true;
                    break;
                }

                Futuro<ApiResponseMB> futuro = new Futuro<>(() -> consolidadoMovimientosCuenta(fechaDesde, fechaHasta, cuenta, contexto, numeroPaginaActual));
                futuros.add(futuro);
            }

            for (Futuro<ApiResponseMB> futuro : futuros) {
                ApiResponseMB responseFuturo = futuro.get();
                if (!responseFuturo.hayError() && !responseFuturo.objetos().isEmpty()) {
                    for (Objeto item : responseFuturo.objetos()) {
                        if (!item.existe("cantPaginas")) {
                            ultimosMovimientos.add(movimientosXCuenta(item, cuenta));
                        }
                    }
                }
            }
        }

        if (ultimosMovimientos.isEmpty()) {
            return RespuestaMB.estado("SIN_MOVIMIENTOS");
        }

        respuesta.set("ultimosMovimientos", ultimosMovimientos);
        respuesta.set("superaLimitePaginas", superaLimitePaginas);

        return respuesta;
    }

    public static RespuestaMB consolidado5MovimientosCuentas(List<Cuenta> cuentas, ContextoMB contexto) {

        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();
        String cuentasAConsultar = cuentas.stream().map(Cuenta::numero).collect(Collectors.joining(","));
        String url = "/v1/cuentas/movimientos";

        ApiRequestMB request = ApiMB.request("CuentasGetMovimientosConsolidados", "cuentas", "GET", url, contexto);
        request.query("cuentas", cuentasAConsultar);
        request.query("validaCuenta", "N");
        request.query("file", "N");
        request.query("from", "N");

        ApiResponseMB response = ApiMB.response(request, cuentasAConsultar);
        if (!response.hayError() && !response.objetos().isEmpty()) {
            ultimosMovimientos.addAll(ultimosMovimientos(response.objetos(), cuentas));
        }

        if (ultimosMovimientos.isEmpty()) {
            return RespuestaMB.estado("SIN_MOVIMIENTOS");
        }

        respuesta.set("ultimosMovimientos", ultimosMovimientos);

        return respuesta;
    }

    private static List<Objeto> ultimosMovimientos(List<Objeto> objetos, List<Cuenta> cuentas) {
        List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();
        Iterator<Objeto> itera = objetos.iterator();

        while (itera.hasNext()) {
            Objeto movimiento = itera.next();
            String numeroCuenta = movimiento.string("cuenta");
            Cuenta cuenta = cuentas.stream().filter(p -> numeroCuenta.equals(p.numero())).findFirst().get();
            ultimosMovimientos.add(ultimosMovimientosXCuenta(movimiento, cuenta));
        }

        return ultimosMovimientos;
    }

    private static Objeto movimientosXCuenta(Objeto movimiento, Cuenta cuenta) {
        String fecha = movimiento.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy");
        Objeto datosAdicionales = new Objeto();
        datosAdicionales.set("numeroOperacion", movimiento.string("secuencial"));
        datosAdicionales.set("hora", movimiento.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss"));
        datosAdicionales.set("fechaMovimiento", movimiento.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
        datosAdicionales.set("saldo", movimiento.bigDecimal("saldo"));
        datosAdicionales.set("saldoFormateado", Formateador.importe(movimiento.bigDecimal("saldo")));
        datosAdicionales.set("descripcionCausa", movimiento.string("descCausa"));
        datosAdicionales.set("categoria", movimiento.string("categoria"));
        datosAdicionales.set("subCategoria", movimiento.string("subCategoria"));
        datosAdicionales.set("tipoMsg", movimiento.string("tipoMsg"));
        datosAdicionales.set("cuitCP", movimiento.string("cuitCP"));
        datosAdicionales.set("idEstablecimiento", movimiento.string("codComercio"));
        datosAdicionales.set("rubro", movimiento.string("codRubro"));
        datosAdicionales.set("referencia", movimiento.string("referencia"));

        checkDataComprobante(movimiento, datosAdicionales);

        return setearMovimiento(movimiento, cuenta, "descripcionMovimiento", "valor", fecha, datosAdicionales);
    }

    private static Objeto ultimosMovimientosXCuenta(Objeto movimiento, Cuenta cuenta) {
        String fecha = movimiento.string("fecha");
        return setearMovimiento(movimiento, cuenta, "descripcion", "monto", fecha, null);
    }

    private static Objeto setearMovimiento(Objeto movimiento, Cuenta cuenta, String campoDescripcion, String campoImporte, String fecha, Objeto datosAdicionales) {

        Objeto itemCuenta = new Objeto();
        itemCuenta.set("id", cuenta.id());
        itemCuenta.set("tipo", "Cuenta");
        itemCuenta.set("descripcionCorta", cuenta.descripcionCorta());
        itemCuenta.set("numeroEnmascarado", "****" + StringUtils.substring(cuenta.numero(), cuenta.numero().length() - 4));
        itemCuenta.set("numero", cuenta.numero());
        itemCuenta.set("fecha", fecha);
        String descripcion = validarSignoParametria(movimiento.string(campoDescripcion).toUpperCase());
        itemCuenta.set("descripcion", descripcion);
        itemCuenta.set("importeFormateado", Formateador.importe(movimiento.bigDecimal(campoImporte)));
        itemCuenta.set("importe", movimiento.bigDecimal(campoImporte));
        itemCuenta.set("simboloMoneda", cuenta.simboloMoneda());
        itemCuenta.set("moneda", cuenta.moneda());
        if (datosAdicionales != null)
            itemCuenta.set("datosAdicionales", datosAdicionales);

        return itemCuenta;
    }

    public static RespuestaMB modificarAliasCuenta(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String alias = contexto.parametros.string("nuevoAlias");

        if (Objeto.anyEmpty(idCuenta, alias)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        ApiResponseMB responseCoelsa = CuentasService.cuentaCoelsa(contexto, cuenta.cbu());

        ApiRequestMB request = ApiMB.request("CuentasPatchAlias", "cuentas", "PATCH", "/v1/alias", contexto);
        request.body("cuit", contexto.persona().cuit());
        request.body("nro_bco", "044");
        request.body("nro_cbu", cuenta.cbu());
        request.body("reasigna", null);
        request.body("valor", alias);
        request.body("valor_original", responseCoelsa.string("nuevoAlias"));

        ApiResponseMB response = ApiMB.response(request, cuenta.numero());
        if (response.hayError()) {
            // emm-20190424-desde
            if ("0380".equals(response.string("codigo"))) {
                return RespuestaMB.estado("ERROR_TIEMPO_PERMITIDO");
            }
            if ("0470".equals(response.string("codigo"))) {
                return RespuestaMB.estado("ERROR_ALIAS_IDENTICOS_O_CAMPOS_INVERTIDOS");
            }
            // emm-20190424-hasta
            if ("0420".equals(response.string("codigo"))) {
                return RespuestaMB.estado("ERROR_ALIAS_YA_CREADO_CON_OTRO_CUIT");
            }
            return RespuestaMB.error();
        }

        CuentasService.eliminarCacheCuentaCoelsa(contexto, alias);
        CuentasService.eliminarCacheCuentaCoelsa(contexto, responseCoelsa.string("nuevoAlias"));
        return RespuestaMB.exito();
    }

    public static RespuestaMB modificarComentarioCuenta(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String comentario = contexto.parametros.string("comentario");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (comentario.length() > 25) {
            return RespuestaMB.estado("SUPERA_LONGITUD_MAXIMA");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdateComentarioCuenta", "homebanking");
        sqlRequest.sql = "UPDATE [Hbs].[dbo].[comentarios_cuentas] ";
        sqlRequest.sql += "SET [comentario] = ? ";
        sqlRequest.sql += "WHERE [cuenta] = ? ";
        sqlRequest.add(comentario);
        sqlRequest.add(cuenta.numero());

        sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
        sqlRequest.sql += "INSERT INTO [Hbs].[dbo].[comentarios_cuentas] ([idCobis] ,[cuenta] ,[comentario]) ";
        sqlRequest.sql += "VALUES (?, ?, ?) ";
        sqlRequest.add(contexto.idCobis());
        sqlRequest.add(cuenta.numero());
        sqlRequest.add(comentario);

        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

//		cuenta.eliminarCacheSelectComentarioCuenta();
        return RespuestaMB.exito();
    }

    public static RespuestaMB movimientosCuenta(ContextoMB contexto) {

        String idCuenta = contexto.parametros.string("idCuenta");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd");
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd");
        Boolean soloCompra = contexto.parametros.bool("soloCompra", false);

        if (Objeto.anyEmpty(idCuenta, fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        // verifico si es la primera llamada, en caso de ser asi solo muestro la primera pagina
        SesionMB sesion = contexto.sesion();
        Boolean primeraLlamada = sesion.getUltimaCuentaConsultadaMovimientos() == null;
        primeraLlamada |= !idCuenta.equals(sesion.getUltimaCuentaConsultadaMovimientos());
        sesion.setUltimaCuentaConsultadaMovimientos(idCuenta);
        if (ConfigMB.bool("mb_apagar_parche_movimientos", false)) {
            primeraLlamada = false;
        }
        // fin de la comprobacion de si es la primera llamada

        Integer numeroPagina = 0;
        Integer cantidadPaginas = 0;
        Integer maximaCantidadPaginas = ConfigMB.integer("mb_movimientos_cuentas_max_paginas", 15);
        RespuestaMB respuesta = new RespuestaMB();
        do {
            ++numeroPagina;
            if (numeroPagina > maximaCantidadPaginas) {
                break;
            }

            String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
            ApiRequestMB request = ApiMB.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
            request.path("idcuenta", cuenta.numero());
            request.query("fechadesde", fechaDesde);
            request.query("fechahasta", fechaHasta);
            request.query("pendientes", "2");
            request.query("validactaempleado", "false");
            request.query("numeropagina", numeroPagina.toString());
            request.query("tipomovimiento", "T");
            request.query("orden", "D");

            ApiResponseMB response = ApiMB.response(request, cuenta.id());
            if (response.hayError()) {
                if (response.string("mensajeAlUsuario").contains("fecha comprendida entre la fecha actual y 12 meses anteriores")) {
                    return RespuestaMB.estado("FECHA_DESDE_ANTERIOR_12_MESES");
                }
                if (response.string("codigo").equals("100")) {
                    return RespuestaMB.estado("SIN_MOVIMIENTOS");
                }
                return RespuestaMB.error();
            }

            if (response.codigo == 204) {
                return RespuestaMB.estado("SIN_MOVIMIENTOS");
            }

            for (Objeto item : response.objetos()) {
                if (!item.existe("cantPaginas")) {

                    if (soloCompra && !item.string("causa").equals("4202")) {
                        continue;
                    }

                    Objeto movimiento = new Objeto();
                    movimiento.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                    String descripcion = validarSignoParametria(item.string("descripcionMovimiento").toUpperCase());
                    movimiento.set("descripcion", descripcion);
                    movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
                    movimiento.set("saldo", item.bigDecimal("saldo"));
                    movimiento.set("saldoFormateado", Formateador.importe(item.bigDecimal("saldo")));
                    movimiento.set("simboloMoneda", cuenta.simboloMoneda());
                    movimiento.set("moneda", cuenta.moneda());
                    movimiento.set("hora", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss"));
                    movimiento.set("fechaMovimiento", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    movimiento.set("descripcionCausa", item.string("descCausa"));
                    movimiento.set("referencia", item.string("referencia"));
                    movimiento.set("numeroOperacion", item.string("secuencial"));
                    movimiento.set("categoria", item.string("categoria"));
                    movimiento.set("subCategoria", item.string("subCategoria"));
                    movimiento.set("tipoMsg", item.string("tipoMsg"));
                    movimiento.set("importe", item.bigDecimal("valor"));
                    movimiento.set("cuitCP", item.string("cuitCP"));
                    movimiento.set("idEstablecimiento", item.string("codComercio"));
                    movimiento.set("rubro", item.string("codRubro"));

                    checkDataComprobante(item, movimiento);

                    respuesta.add("movimientos", movimiento);
                } else {
                    cantidadPaginas = item.integer("cantPaginas");
                    if (primeraLlamada) {
                        cantidadPaginas = 1;
                    }
                }
            }
        } while (numeroPagina < cantidadPaginas);

        if (soloCompra) {
            // verificar por segmento si es buho
            respuesta.set("mercadoPagoHabilitado", verificarBuhoOne(contexto));
            // verificar si pasaron 10 dias habiles
            respuesta.set("fechaHastaReclamo", Util.calcularFechaNdiasHabilesPasado(contexto, Fecha.fechaActual(), -10));
        }

        respuesta.set("superaLimitePaginas", numeroPagina > maximaCantidadPaginas);

        return respuesta;
    }

    public static RespuestaMB historicoMovimientosCuenta(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String categoria = contexto.parametros.string("categoria");
        String subCategoria = contexto.parametros.string("subCategoria");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        String cuitCP = contexto.parametros.string("cuitCP");
        String idEstablecimiento = contexto.parametros.string("idEstablecimiento");

        String fechaDesde = contexto.parametros.date("fechaMovimiento", "d/M/yyyy", "yyyy-MM-01");
        String fechaHasta = obtenerFechaHastaHistoricoMovimientos(contexto.parametros.date("fechaMovimiento", "d/M/yyyy", "yyyy-MM-dd"));

        if (Objeto.anyEmpty(idCuenta, categoria, subCategoria, importe)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        if (!mostrarHistoricoMovimientos(categoria, subCategoria)) {
            return RespuestaMB.estado("COMBINACION_NO_DISPONIBLE");
        }

        Integer numeroPagina = 0;
        Integer cantidadPaginas = 0;

        Integer maximaCantidadPaginas = ConfigMB.integer("mb_movimientos_cuentas_max_paginas", 15);

        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> movimientos = new ArrayList<>();
        do {
            ++numeroPagina;
            if (numeroPagina > maximaCantidadPaginas) {
                break;
            }

            String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
            ApiRequestMB request = ApiMB.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
            request.path("idcuenta", cuenta.numero());
            request.query("fechadesde", fechaDesde);
            request.query("fechahasta", fechaHasta);
            request.query("pendientes", "2");
            request.query("validactaempleado", "false");
            request.query("numeropagina", numeroPagina.toString());
            request.query("tipomovimiento", "T");
            request.query("orden", "D");

            ApiResponseMB response = ApiMB.response(request, cuenta.id());
            if (response.hayError()) {
                if (response.string("mensajeAlUsuario").contains("fecha comprendida entre la fecha actual y 12 meses anteriores")) {
                    return RespuestaMB.estado("FECHA_DESDE_ANTERIOR_12_MESES");
                }
                if (response.string("codigo").equals("100")) {
                    return RespuestaMB.estado("SIN_MOVIMIENTOS");
                }
                return RespuestaMB.error();
            }

            for (Objeto item : response.objetos()) {
                if (!item.existe("cantPaginas")) {
                    if (categoria.equalsIgnoreCase(item.string("categoria")) && subCategoria.equalsIgnoreCase(item.string("subCategoria"))) {
                        Objeto movimiento = new Objeto();
                        String descripcion = validarSignoParametria(item.string("descripcionMovimiento").toUpperCase());
                        movimiento.set("descripcion", descripcion);
                        movimiento.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                        movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
                        movimiento.set("simboloMoneda", cuenta.simboloMoneda());
                        movimiento.set("moneda", cuenta.moneda());
                        movimiento.set("fechaMovimiento", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                        movimiento.set("numeroOperacion", item.string("secuencial"));
                        movimiento.set("categoria", item.string("categoria"));
                        movimiento.set("subCategoria", item.string("subCategoria"));
                        movimiento.set("importe", item.bigDecimal("valor"));
                        movimiento.set("cuitCP", item.string("cuitCP"));
                        movimiento.set("idEstablecimiento", item.string("codComercio"));

                        movimientos.add(movimiento);
                    }
                } else {
                    cantidadPaginas = item.integer("cantPaginas");
                }
            }
        } while (numeroPagina < cantidadPaginas);

        if (movimientos.isEmpty()) {
            return RespuestaMB.estado("MOVIMIENTO_NO_EXISTE");
        }

        if ("TRANSFERENCIAS".equalsIgnoreCase(categoria) && ("ENTRANTE".equalsIgnoreCase(subCategoria) || "SALIENTE".equalsIgnoreCase(subCategoria))) {
            if (StringUtils.isEmpty(cuitCP)) {
                return RespuestaMB.parametrosIncorrectos();
            }
            filtrarTransferencias(movimientos, cuitCP);
        }

        if ("COMPRAS".equalsIgnoreCase(categoria) && "TARJETA DE DEBITO".equalsIgnoreCase(subCategoria)) {
            if (StringUtils.isEmpty(idEstablecimiento)) {
                return RespuestaMB.parametrosIncorrectos();
            }
            filtrarComerciosComprasTD(movimientos, idEstablecimiento);
        }

        if (movimientos.size() == 1) {
            return RespuestaMB.estado("UNICO_MOVIMIENTO");
        }

        BigDecimal totalImportes = BigDecimal.ZERO;
        for (Objeto movimiento : movimientos) {
            if (importe.compareTo(BigDecimal.ZERO) == movimiento.bigDecimal("importe").compareTo(BigDecimal.ZERO)) {
                totalImportes = totalImportes.add(movimiento.bigDecimal("importe"));
            }
        }

        BigDecimal promedio = totalImportes.divide(new BigDecimal(movimientos.size()), RoundingMode.CEILING);
        respuesta.set("cantidadMovimientos", movimientos.size());
        respuesta.set("totalImportes", totalImportes);
        respuesta.set("promedioImportes", promedio);
        respuesta.set("mes", Fecha.mes(contexto.parametros.date("fechaMovimiento", "d/M/yyyy", "MM")));
        respuesta.set("movimientos", movimientos);
        respuesta.set("superaLimitePaginas", numeroPagina > maximaCantidadPaginas);
        return respuesta;
    }

    public static String validarSignoParametria(String descripcion) {
        String descripcionFiltrada = descripcion;
        if (descripcion != null && !descripcion.isEmpty()) {
            for (TipoSigno signo : TipoSigno.values()) {
                if (descripcion.contains(signo.valor()) && StringUtils.indexOf(descripcion, signo.valor() + " - ") != -1) {
                    String cadenaSigno = signo.valor() + " - ";
                    descripcionFiltrada = Formateador.eliminarTextoEnDescripcion(descripcion, cadenaSigno);
                    break;
                }
            }
            descripcionFiltrada = validarParametria(descripcionFiltrada);
        }

        return descripcionFiltrada;
    }

    private static String validarParametria(String descripcion) {
        if (descripcion.contains("RETIRO CAJ.AH.")) {
            descripcion = "EXTRACCION POR CAJA SUCURSAL";
        }
        return descripcion;
    }

    private static void checkDataComprobante(Objeto item, Objeto movimiento) {
        BigDecimal importe = item.bigDecimal("valor");
        String causa = item.string("causa");

        boolean tieneComprobante = ConfigMB.getCausales().containsKey(causa) && importe.compareTo(new BigDecimal(0)) < 0;

        movimiento.set("tieneComprobante", tieneComprobante);
        movimiento.set("causa", causa);

        if (tieneComprobante) {
            movimiento.set("tipo", ConfigMB.getCausales().get(causa));
        }
    }

    public static RespuestaMB consolidadoImpuestos(ContextoMB contexto) {
        String año = contexto.parametros.string("anio");

        if (Objeto.anyEmpty(año)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("ProductosGetImpuestos", "productos", "GET", "/v1/clientes/{id}/impuestos", contexto);
        request.path("id", contexto.idCobis());
        request.query("anio", año);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), año);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto item : response.objetos()) {
            Map<String, String> mapa = new LinkedHashMap<>();
            mapa.put("AHO", "Caja de Ahorro");
            mapa.put("CTE", "Cuenta Corriente");
            mapa.put("PFI", "Plazo Fijo");

            Objeto registro = new Objeto();
            registro.set("producto", mapa.get(item.string("tipoProducto")));
            registro.set("numero", item.string("numero").trim());
            registro.set("simboloMoneda", Formateador.simboloMoneda(item.string("moneda")));
            registro.set("saldo", item.bigDecimal("saldo"));
            registro.set("saldoFormateado", Formateador.importe(item.bigDecimal("saldo")));
            registro.set("debitos", item.bigDecimal("impuestoDebito"));
            registro.set("debitosFormateado", Formateador.importe(item.bigDecimal("impuestoDebito")));
            registro.set("creditos", item.bigDecimal("impuestoCredito"));
            registro.set("creditosFormateado", Formateador.importe(item.bigDecimal("impuestoCredito")));
            respuesta.add("consolidadaImpuestos", registro);
        }

        return respuesta;
    }

    public static RespuestaMB cajaAhorroBloqueos(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        String numeroCorto = cuenta.numeroCorto();
        if ("".equals(numeroCorto)) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("consultaConErrores", false);
        ApiResponseMB responseBloqueos = CuentasService.cajaAhorroBloqueos(contexto, numeroCorto);
        if (responseBloqueos.hayError()) {
            // return Respuesta.estado("CONSULTA_BLOQUEOS");3
            respuesta.set("consultaConErrores", true);
            respuesta.set("tieneBloqueos", false);
        } else {
            respuesta.set("tieneBloqueos", !responseBloqueos.objetos().isEmpty());
        }

        // TODO: para hacer completo este servicio, habría que devolver los bloqueos. De
        // todas formas desde front esto no lo muestra, asi que lo dejaria asi
        return respuesta;
    }

    public static RespuestaMB cuentasComitentesAsociadas(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("tieneCuentasComitentesAsociadas", false);
        // CHEQUEO SI ESTA ASOCIADA A UNA CUENTA COMITENTE
        for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
            ApiResponseMB responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto, itemCuentaComitente.numero());
            if (responseCuentasComitentes.hayError()) {
                return RespuestaMB.estado("ERROR_CONSULTANDO_COMITENTES");
            }
            for (Objeto cuentaItem : responseCuentasComitentes.objetos()) {
                if (cuentaItem.string("NUMERO").trim().equals(cuenta.numero()) || cuentaItem.string("NUMERO").trim().equals(cuenta.id())) {
                    respuesta.set("tieneCuentasComitentesAsociadas", true);
                    String ult4 = cuenta.ultimos4digitos();
                    respuesta.set("titulo", "No podemos dar de baja la Caja de Ahorro xxxx-" + ult4);
                    respuesta.set("descripcion", "<strong>Para realizarlo debés dar de baja tu Cuenta Inversor llamando al 0810-222-7777</strong><br>Luego continuá la solicitud.");
                }
            }
        }

        // TODO: para hacer completo este servicio, habría que devolver las cuentas
        // comitentes asociadas. De todas formas desde front esto no lo muestra, asi que
        // lo dejaria asi
        return respuesta;
    }

    public static RespuestaMB compartirCBU(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String email = contexto.parametros.string("email");
        String mensaje = contexto.parametros.string("mensaje");

        if (Objeto.anyEmpty(idCuenta, email)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
        requestMail.body("para", email);
        requestMail.body("plantilla", ConfigMB.string("doppler_cbu"));
        Objeto parametros = requestMail.body("parametros");
        parametros.set("Subject", "Información Datos Cuenta Home Banking");
        parametros.set("CBU", "<b>CBU: </b>" + cuenta.cbuFormateado());
        parametros.set("CUENTA", "<b>Número de cuenta: </b>" + cuenta.numeroFormateado());
        parametros.set("TITULAR", "<b>Titular de la cuenta: </b>" + contexto.persona().apellidos() + " " + contexto.persona().nombres());
        parametros.set("CUIL_CUIT_CDI", "<b>CUIL/CUIT/CDI: </b>" + contexto.persona().cuitFormateado());
        parametros.set("TIPO_CUENTA", "<b>Tipo de cuenta: </b>" + cuenta.producto());
        parametros.set("COMENTARIO", "<b>Mensaje: </b>" + mensaje);

        ApiResponseMB responseMail = ApiMB.response(requestMail);
        if (responseMail.hayError()) {
            return RespuestaMB.error();
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB valoresSuspenso(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.error();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        Boolean habilitarCuentasApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cuentas_api");

        ApiRequestMB request = null;
        if (habilitarCuentasApi) {
            request = ApiMB.request("CuentasGetValorSuspenso", "cuentas", "GET", "/v1/cuentas/{idCuenta}/valoresensuspenso", contexto);
            request.path("idCuenta", cuenta.numero());
            request.query("operacion", "P");
            request.query("secuencial", "0");
            request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        } else {
            request = ApiMB.request("CuentasWindowsGetValorSuspenso", "cuentas_windows", "GET", "/v1/{numeroCuenta}/valorSuspenso", contexto);
            request.path("numeroCuenta", cuenta.numero());
            request.query("secuencial", "0");
        }

        ApiResponseMB response = ApiMB.response(request, cuenta.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        if (response.codigo == 204) {
            return RespuestaMB.estado("SIN_VALORES_SUSPENSO");
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("total", response.bigDecimal("totalValoresSuspenso"));
        respuesta.set("totalFormateado", Formateador.importe(response.bigDecimal("totalValoresSuspenso")));
        for (Objeto item : response.objetos("valoresSuspenso")) {
            Objeto movimiento = new Objeto();
            movimiento.set("fecha", item.string("fecha"));
            movimiento.set("descripcion", item.string("descripcion"));
            movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
            respuesta.add("movimientos", movimiento);
        }

        return respuesta;
    }

    public static RespuestaMB consolidadaResumenCuenta(ContextoMB contexto) {
        if (contexto.cuentas().isEmpty()) {
            return RespuestaMB.estado("SIN_CUENTAS");
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Cuenta cuenta : contexto.cuentas()) {
            Objeto item = new Objeto();
            item.set("id", cuenta.id());
            item.set("descripcionCorta", cuenta.descripcionCorta());
            item.set("simboloMoneda", cuenta.simboloMoneda());
            item.set("ultimos4digitos", cuenta.ultimos4digitos());
            respuesta.add("cuentas", item);

            int añoActual = Calendar.getInstance().get(Calendar.YEAR);
            int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1;

            int añoCuenta = Integer.valueOf(cuenta.fechaAlta("yyyy"));
//			int mesCuenta = new Integer(cuenta.fechaAlta("MM"));

            for (int año = añoActual; año > añoActual - 3; --año) {
                if (añoCuenta > año) {
                    continue;
                }
                if (año == añoActual && mesActual == 1) {
                    continue;
                }
                item.add("años", año);
            }
        }

        return respuesta;
    }

    public static RespuestaMB periodosResumenCuenta(ContextoMB contexto) {
        String id = contexto.parametros.string("id");
        Integer año = contexto.parametros.integer("año");

        if (Objeto.anyEmpty(id, año)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(id);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
        }

        RespuestaMB respuesta = new RespuestaMB();
        int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int añoActual = Calendar.getInstance().get(Calendar.YEAR);

//		int añoCuenta = new Integer(cuenta.fechaAlta("yyyy"));
//		int mesCuenta = new Integer(cuenta.fechaAlta("MM"));

        if (año == añoActual && mesActual == 1) {
            return RespuestaMB.estado("AÑO_INCORRECTO");
        }
        if (año > añoActual) {
            return RespuestaMB.estado("AÑO_INCORRECTO");
        }

        if (cuenta.esCajaAhorro()) {
            if (año < añoActual || mesActual > 12) {
                Objeto item3 = new Objeto();
                item3.set("id", id + "_" + "03" + "_" + año);
                item3.set("numero", "3");
                item3.set("descripcion", "Septiembre - Diciembre");
                respuesta.add("meses", item3);
            }

            if (año < añoActual || mesActual > 8) {
                Objeto item2 = new Objeto();
                item2.set("id", id + "_" + "02" + "_" + año);
                item2.set("numero", "2");
                item2.set("descripcion", "Mayo - Agosto");
                respuesta.add("meses", item2);
            }

            if (año < añoActual || mesActual > 4) {
                Objeto item1 = new Objeto();
                item1.set("id", id + "_" + "01" + "_" + año);
                item1.set("numero", "1");
                item1.set("descripcion", "Enero - Abril");
                respuesta.add("meses", item1);
            }
        }

        if (cuenta.esCuentaCorriente()) {
            Integer tope = año == añoActual ? mesActual - 1 : 12;
            for (Integer mes = tope; mes >= 1; --mes) {
                Objeto item = new Objeto();
                if (mes < 10) {
                    item.set("id", id + "_0" + mes + "_" + año);
                } else {
                    item.set("id", id + "_" + mes + "_" + año);
                }
                item.set("numero", mes);
                item.set("descripcion", RestCatalogo.nombreMes(mes));
                respuesta.add("meses", item);
            }
        }

        return respuesta;
    }

    public static Object resumenPlazoFijo(ContextoMB contexto) {
        contexto.parametros.set("plazoFijo", true);
        return resumenCuenta(contexto);
    }

    public static Object rentaFinanciera(ContextoMB contexto) {
        String anio = contexto.parametros.string("anio");

        ApiRequestMB request = ApiMB.request("RentaFinanciera", "inversiones", "GET", "/v1/rentafinanciera", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("idCobis", contexto.idCobis());
        request.query("periodo", anio);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), anio);
        if (response.hayError()) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.error();
        }

        if (response.codigo == 204) {
            contexto.setHeader("estado", "SIN_MOVIMIENTOS");
            return RespuestaMB.error();
        }

        byte[] archivo = Base64.getDecoder().decode(response.string("file"));
        contexto.setHeader("Content-Type", "application/pdf; name=" + anio + ".pdf");
        contexto.setHeader("estado", "0");
        return archivo;
    }

    public static Object resumenCuenta(ContextoMB contexto) {
        String id = contexto.parametros.string("id");
        Boolean plazoFijo = contexto.parametros.bool("plazoFijo", false);

        if (Objeto.anyEmpty(id)) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.parametrosIncorrectos();
        }

        String idCuenta = id.split("_")[0];
        String periodo = id.split("_")[1];
        String año = id.split("_")[2];

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            contexto.setHeader("estado", "CUENTA_NO_EXISTE");
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        String fechaDesde = null;
        String fechaHasta = null;

        if (cuenta.esCajaAhorro()) {
            try {
                Integer periodoEntero = Integer.valueOf(periodo);
                Integer mesDesde = periodoEntero * 4 - 3;
                Integer mesHasta = periodoEntero * 4;

                Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("01/0" + mesHasta + "/" + año);
                fecha = Fecha.sumarMeses(fecha, 1);
                fecha = Fecha.restarDias(fecha, 1L);

//				fechaDesde = "01/0" + mesDesde + "/" + año;
//				fechaHasta = new SimpleDateFormat("dd/MM/yyyy").format(fecha);

                fechaDesde = año + "-" + "0" + mesDesde + "-" + "01";
                fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
            } catch (Exception e) {
                contexto.setHeader("estado", "ERROR");
                return RespuestaMB.error();
            }
        }

        if (cuenta.esCuentaCorriente()) {
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + periodo + "/" + año);
                fecha = Fecha.sumarMeses(fecha, 1);
                fecha = Fecha.restarDias(fecha, 1L);

//				fechaDesde = "01/" + periodo + "/" + año;
//				fechaHasta = new SimpleDateFormat("dd/MM/yyyy").format(fecha);

                fechaDesde = año + "-" + periodo + "-" + "01";
                fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
            } catch (Exception e) {
                contexto.setHeader("estado", "ERROR");
                return RespuestaMB.error();
            }
        }

        ApiRequestMB request = ApiMB.request("ResumenCuenta", "cuentas", "GET", "/v1/cuentas/{numero}/resumen", contexto);
        request.path("numero", cuenta.numero());
        request.query("desde", fechaDesde);
        request.query("hasta", fechaHasta);
        request.query("producto", cuenta.descripcionCorta());

        if (plazoFijo) {
            request = ApiMB.request("ResumenPlazoFijo", "cuentas", "GET", "/v1/cuentas/{numero}/resumen", contexto);
            request.path("numero", cuenta.numero());
            request.query("desde", fechaDesde);
            request.query("hasta", fechaHasta);
            request.query("producto", "PF");
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.error();
        }
        if (response.codigo == 204) {
            contexto.setHeader("estado", "SIN_RESUMEN");
            return RespuestaMB.estado("SIN_RESUMEN");
        }

        byte[] archivo = Base64.getDecoder().decode(response.string("file"));
        contexto.setHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
        contexto.setHeader("estado", "0");
        return archivo;
    }

    public static RespuestaMB consolidadaCambioCuentaPrincipal(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

//		for (Cuenta cuenta : contexto.cuentas()) {
//			Objeto item = new Objeto();
//			item.set("id", cuenta.id());
//			item.set("descripcion", cuenta.producto());
//			item.set("numero", cuenta.numero());
//			item.set("numeroFormateado", cuenta.numeroFormateado());
//			item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
//			item.set("titularidad", cuenta.titularidad());
//			item.set("moneda", cuenta.moneda());
//			item.set("simboloMoneda", cuenta.simboloMoneda());
//			item.set("estado", cuenta.descripcionEstado());
//			item.set("saldo", cuenta.saldo());
//			item.set("saldoFormateado", cuenta.saldoFormateado());
//			item.set("acuerdo", cuenta.acuerdo());
//			item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
//			item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
//			item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
//			item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
//			respuesta.add("cuentas", item);
//		}

        for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {

            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            Objeto item = new Objeto();
            item.set("id", tarjetaDebito.id());
            item.set("descripcion", tarjetaDebito.producto());
            item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
            item.set("titularidad", tarjetaDebito.titularidad());
            for (Cuenta cuenta : tarjetaDebito.cuentasAsociadas()) {
                Objeto itemCuenta = new Objeto();
                itemCuenta.set("id", cuenta.id());
                itemCuenta.set("descripcion", cuenta.producto());
                itemCuenta.set("descripcionCorta", cuenta.descripcionCorta());
                itemCuenta.set("numero", cuenta.numero());
                itemCuenta.set("numeroFormateado", cuenta.numeroFormateado());
                itemCuenta.set("numeroEnmascarado", cuenta.numeroEnmascarado());
                itemCuenta.set("titularidad", cuenta.titularidad());
                itemCuenta.set("idMoneda", cuenta.idMoneda());
                itemCuenta.set("moneda", cuenta.moneda());
                itemCuenta.set("simboloMoneda", cuenta.simboloMoneda());
                itemCuenta.set("estado", cuenta.descripcionEstado());
                itemCuenta.set("saldo", cuenta.saldo());
                itemCuenta.set("saldoFormateado", cuenta.saldoFormateado());
                itemCuenta.set("acuerdo", cuenta.acuerdo());
                itemCuenta.set("acuerdoFormateado", cuenta.acuerdoFormateado());
                itemCuenta.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
                itemCuenta.set("disponibleFormateado", Formateador.importe(itemCuenta.bigDecimal("disponible")));
                itemCuenta.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
                itemCuenta.set("esPrincipalPais", false);
                itemCuenta.set("esPrincipalExterior", false);
                for (Cuenta cuentaPrincipal : tarjetaDebito.cuentasAsociadasPrincipales()) {
                    if (cuentaPrincipal.id().equals(cuenta.id())) {
                        itemCuenta.set("esPrincipalPais", true);
                    }
                }
                for (Cuenta cuentaPrincipal : tarjetaDebito.cuentasAsociadasPrincipalesExterior()) {
                    if (cuentaPrincipal.id().equals(cuenta.id())) {
                        itemCuenta.set("esPrincipalExterior", true);
                    }
                }
                item.add("cuentasAsociadas", itemCuenta);
            }
            respuesta.add("tarjetasDebito", item);
        }

        Objeto datosExtras = new Objeto();
        datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
        datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
        datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
        datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebito().size() == 1);
        respuesta.set("datosExtras", datosExtras);

        return respuesta;
    }

    public static RespuestaMB cambiarCuentaPrincipal(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String idCuenta = contexto.parametros.string("idCuenta");
        Boolean exterior = contexto.parametros.bool("exterior", null);

        if (Objeto.anyEmpty(idTarjetaDebito, idCuenta, exterior)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("TARJETA_DEBITO_NO_ENCONTRADA");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
        }

        if (!exterior) {
            Map<Integer, String> mapa = new HashMap<Integer, String>();
            mapa.put(15000, "01");
            mapa.put(25000, "03");
            mapa.put(20000, "04");
            mapa.put(4000, "05");
            mapa.put(30000, "07");
            mapa.put(8000, "08");
            mapa.put(10000, "10");

            String limiteRetiro = mapa.get(tarjetaDebito.limiteExtraccion().intValue());
            if (limiteRetiro == null || limiteRetiro.isEmpty()) {
                limiteRetiro = "00";
            }

            Objeto cuentaOperativa = new Objeto();
            cuentaOperativa.set("accion", "M");
            cuentaOperativa.set("cuenta", cuenta.numero());
            cuentaOperativa.set("esCuentaPrincipal", "S");
            cuentaOperativa.set("idMoneda", cuenta.idMoneda());
            cuentaOperativa.set("tipoProducto", cuenta.idTipo());

            ApiRequestMB request = ApiMB.request("ModificarCuentaPrincipalPais", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}", contexto);
            request.path("nrotarjeta", tarjetaDebito.numero());
            request.query("nrotarjeta", tarjetaDebito.numero());
            request.body("codigoCliente", contexto.idCobis());
            request.body("tipoTarjeta", tarjetaDebito.idTipoTarjeta());
            request.body("limiteRetiro", limiteRetiro);
            request.add("cuentaOperativas", cuentaOperativa);
            ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), tarjetaDebito.numero(), cuenta.numero());
            if (response.hayError()) {
                if (response.string("codigo").equals("1831602")) {
                    return RespuestaMB.estado("SOLICITUD_EN_CURSO");
                }
                if (response.string("codigo").equals("40003")) {
                    return RespuestaMB.estado("FUERA_HORARIO");
                }
                return RespuestaMB.error();
            }
        }

        if (exterior) {
            ApiRequestMB request = ApiMB.request("ModificarCuentaPrincipalExterior", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}/cuentaexterior", contexto);
            request.path("nrotarjeta", tarjetaDebito.numero());
            request.body("id_app", Integer.valueOf(Util.idProceso()));
            request.body("reverso", "N");
            request.body("servicio", 200);
            request.body("cuenta_oper1", cuenta.numero());
            request.body("asoc_exterior", "S");
            ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), tarjetaDebito.numero(), cuenta.numero());
            if (response.hayError()) {
                if (response.string("codigo").equals("1831602")) {
                    return RespuestaMB.estado("SOLICITUD_EN_CURSO");
                }
                if (response.string("codigo").equals("40003")) {
                    return RespuestaMB.estado("FUERA_HORARIO");
                }
                return RespuestaMB.error();
            }
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB consultaCuenta(ContextoMB contexto) {
        String cbu = contexto.parametros.string("cbu");
        String banco = "";

        if (Objeto.empty(cbu)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB responseCoelsa = CuentasService.cuentaCoelsa(contexto, cbu);

        if (responseCoelsa.hayError()) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.error();
        }

        if (StringUtils.isNotBlank(responseCoelsa.string("nroBco"))) {
            ApiRequestMB request = ApiMB.request("CodigoBancos", "catalogo", "GET", "/v1/codigosBancos	", contexto);
            request.query("codigoBanco", responseCoelsa.string("nroBco"));
            ApiResponseMB response = ApiMB.response(request);
            if (!response.hayError()) {
                banco = response.objetos().get(0).string("Descripcion");
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("nombreTitular", responseCoelsa.string("nombreTitular"));
        respuesta.set("cuit", responseCoelsa.string("cuit"));
        respuesta.set("alias", responseCoelsa.string("nuevoAlias"));
        respuesta.set("banco", banco);

        return respuesta;
    }

    private static String obtenerFechaHastaHistoricoMovimientos(String fechaHasta) {
        LocalDate hasta = LocalDate.parse(fechaHasta);
        hasta = hasta.with(lastDayOfMonth());
        return hasta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static boolean mostrarHistoricoMovimientos(String categoria, String subCategoria) {
        String subCategorias;

        switch (categoria) {
            case "DEPOSITO":
                subCategorias = ConfigMB.string("subcategorias_historico_deposito");
                break;
            case "EXTRACCIONES":
                subCategorias = ConfigMB.string("subcategorias_historico_extracciones");
                break;
            case "HABERES":
                subCategorias = ConfigMB.string("subcategorias_historico_haberes");
                break;
            case "INVERSIONES":
                subCategorias = ConfigMB.string("subcategorias_historico_inversiones");
                break;
            case "PAGOS":
                subCategorias = ConfigMB.string("subcategorias_historico_pagos");
                break;
            case "PROMOCIONES Y BENEFICIOS":
                subCategorias = ConfigMB.string("subcategorias_historico_promociones_y_beneficios");
                break;
            case "TRANSFERENCIAS":
                subCategorias = ConfigMB.string("subcategorias_historico_transferencias");
                break;
            case "COMPRAS":
                subCategorias = ConfigMB.string("subcategorias_historico_compras");
                break;
            default:
                return false;
        }

        for (String subCat : subCategorias.split("_")) {
            if (subCategoria.equalsIgnoreCase(subCat)) {
                return true;
            }
        }

        return false;
    }

    private static void filtrarTransferencias(List<Objeto> movimientos, String cuitCP) {
        movimientos.removeIf(item -> !cuitCP.equalsIgnoreCase(item.string("cuitCP")));
    }

    private static void filtrarComerciosComprasTD(List<Objeto> movimientos, String idEstablecimiento) {
        movimientos.removeIf(item -> !idEstablecimiento.equalsIgnoreCase(item.string("idEstablecimiento")));
    }

    public static RespuestaMB validarCuentaAsociadaVirtual(ContextoMB contexto) {
        String numeroCuenta = contexto.parametros.string("numero", "");

        if (numeroCuenta.isEmpty()) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        try {
            RespuestaMB tarjetas = MBTarjetas.consolidadaTarjetasFull(contexto);
            for (Objeto td : tarjetas.objetos("tarjetasDebito")) {
                for (Objeto cuenta : td.objetos("cuentasAsociadas")) {
                    if (numeroCuenta.equals(cuenta.get("numero"))) {
                        respuesta.set("virtual", td.bool("virtual"));
                        return respuesta;
                    }
                }
            }
        } catch (Exception e) {
            return RespuestaMB.error();
        }
        return respuesta.set("virtual", false);
    }

    public static RespuestaMB bloqueosCuenta(ContextoMB contexto, String idCuenta, String numeroCausal) {
        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (numeroCausal == null)
            numeroCausal = "";

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB responseBloqueos = CuentasService.cuentaBloqueos(contexto, cuenta.numero(), cuenta.idTipo());

        boolean tieneBloqueos = false;
        if (responseBloqueos.hayError()) {
            return RespuestaMB.error();
        }
        for (Objeto item : responseBloqueos.objetos()) {
            if ("".equals(numeroCausal) || numeroCausal.equals(item.string("numero_causal"))) {
                Objeto bloqueo = new Objeto();
                bloqueo.set("numeroCausal", item.string("numero_causal"));
                bloqueo.set("descripcionCausal", item.string("desc_causal"));
                bloqueo.set("montoBloqueo", item.bigDecimal("monto_bloqueo"));
                bloqueo.set("montoBloqueoFormateado", Formateador.importe(item.bigDecimal("monto_bloqueo")));
                tieneBloqueos = true;
                respuesta.set("tieneBloqueos", tieneBloqueos);
                respuesta.add("bloqueos", bloqueo);
            }
        }
        return respuesta;
    }

    public static RespuestaMB estadosCuentaAsync(ContextoMB contexto) {
        if (!ConfigMB.esProduccion() && !ConfigMB.esOpenShift()) {
            return estadosCuenta(contexto);
        }

        RespuestaMB respuesta = new RespuestaMB();
        try {
            ExecutorService pool = Executors.newCachedThreadPool();
            Future<RespuestaMB> future = pool.submit(() -> estadosCuenta(contexto));
            respuesta = future.get(ConfigMB.integer("apiestadoscuenta", 3), TimeUnit.SECONDS);
        } catch (Exception e) {
        }
        return respuesta;
    }

    public static RespuestaMB estadosCuenta(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
            if (tarjetaCredito != null) {
                idTarjetaCredito = tarjetaCredito.id();
            }
        }

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        ApiResponseMB responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return RespuestaMB.error();
        }

        String vencimiento;
        String cierre;
        Objeto datos = new Objeto();

        ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(), tarjetaCredito.numero());

        if (responseResumen.hayError()) {
            return RespuestaMB.error();
        }

        if (responseResumen.codigo == 204) {
            return RespuestaMB.estado("SIN_RESUMEN");
        }

        // TODO: validar si voy a parametria o sigo con datos de visa
        try {
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
                Date fechaCierreProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                if ((fechaActual.compareTo(fechaCierreProximo) > 0 || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    Objeto fechas = MBTarjetas.fechaCierreParametrica(Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy"), tarjetaCredito.grupoCarteraTc());
                    if (fechas != null && !fechas.string("cierre").isEmpty()) {
                        responseResumen.set("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", fechas.string("cierre"));
                        responseResumen.set("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", fechas.string("vencimiento"));
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", "");
        cierre = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", "");

        if (!vencimiento.isEmpty()) {
            try {
                Objeto fechaTC = MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, responseResumen);
                vencimiento = fechaTC.string("vencimiento");
                cierre = fechaTC.string("cierre");
            } catch (Exception e) { // evitamos excepcion y pones fecha por defecto del servicio tarjeta credito
                cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
            }
        }

        if (!cierre.isEmpty()) {
            try {
                estadoTarjeta(cierre, datos);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        String vtoUltima = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", "");
        Boolean onboardingMostrado = true;
        Boolean enMora = false;
        if (!vtoUltima.isEmpty()) {
            try {
                // TODO AUT-183 mock para estados de pagos de TC, falta definicion
//				if (!ConfigMB.esProduccion()) {
//					estadoTarjetaPostVto(vtoUltima, datos, contexto);
//				}

                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoEstadosDeuda")) {
                    Date fechaVencimiento = Fecha.stringToDate(vtoUltima, "dd/MM/yyyy");
                    Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                    RespuestaMB respuestaMora = MBTarjetas.estadoDeuda(contexto);
                    if (respuestaMora.string("datos.estadoDeuda").equals("CON_DEUDA")) {
                        if (respuestaMora.string("datos.tipoMora").equals("ONE")) {
                            enMora = true;
                            onboardingMostrado = false;
                            String formaPago = tarjetaCredito.formaPago().equalsIgnoreCase("Efectivo") ? "EFECTIVO" : "AUTODEBITO";
                            String tipoMora = respuestaMora.string("datos.tipoMora").equals("ONE") ? "T" : "";
                            if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                                datos.set("estadoTarjeta", "EN_MORA" + tipoMora + "_" + formaPago);
                            }
                            ApiResponseMB detalleMora = RestMora.getProductosEnMoraDetallesCache(contexto, respuestaMora.string("datos.cta_id"));
                            datos.set("estadoMora", Util.bucketMora(detalleMora.objetos().get(0).integer("DiasMora"), "TC"));
                            datos.set("ctaId", detalleMora.objetos().get(0).integer("cta_id"));
                            datos.set("promesaVigente", detalleMora.objetos().get(0).string("PromesaVigente"));
                            datos.set("AvisoPago", detalleMora.objetos().get(0).string("IndicaYPAG"));
                            datos.set("montoMinPromesa", detalleMora.objetos().get(0).string("montoMinPromesa"));
                            datos.set("montoMaxPromesa", detalleMora.objetos().get(0).bigDecimal("deudaAVencer").add(detalleMora.objetos().get(0).bigDecimal("Deuda Vencida")));
                            datos.set("deudaAVencer", detalleMora.objetos().get(0).string("deudaAVencer"));
                            datos.set("deudaVencida", detalleMora.objetos().get(0).string("Deuda Vencida"));
                            datos.set("numeroProducto", respuestaMora.string("datos.numeroProducto").trim());
                            onboardingMostrado = !detalleMora.objetos().get(0).string("PromesaVigente").isEmpty() || !detalleMora.objetos().get(0).string("IndicaYPAG").isEmpty();
                        }
                    }
                }

                Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
                datos.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

                try {
                    if (!muestreoOnboarding && onboardingMostrado && enMora) {
                        contexto.parametros.set("nemonico", "ONBOARDING");
                        Util.contador(contexto);
                    }
                } catch (Exception e) {
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!vencimiento.isEmpty()) {
            estadoVencimiento(vencimiento, datos);
            try {
                if (Objeto.setOf("TRES", "DOS", "UNO", "HOY").contains(datos.get("estadoVencimiento"))) {
                    datos.set("estadoTarjeta", "RESUMEN_LISTO");
                }
            } catch (Exception e) {
            }
        }

        try {
            if (datos.string("estadoTarjeta").isEmpty() || datos.string("estadoTarjeta").contains("EN_MORA")) {
                Date fechaCierreProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");

                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");

                if ((fechaActual.compareTo(fechaCierreProximo) > 0 || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    datos.set("estadoTarjeta", "PREPARANDO_RESUMEN_2");
                }
            }
        } catch (Exception e) {
        }

        datos.set("cierre", cierre);
        datos.set("vencimiento", vencimiento);
        respuesta.set("datos", datos);

        return respuesta;
    }

    private static void estadoTarjeta(String cierre, Objeto datos) throws ParseException {
        Date fechaCierre = Fecha.stringToDate(cierre, "dd/MM/yyyy");
        Date fechaPasado72h = Fecha.sumarDias(fechaCierre, 3L);
        Date fechaPosteriorCierre = Fecha.sumarDias(fechaCierre, 1L);
        Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
        if (esDiaPosteriorFechaCierre(fechaPosteriorCierre, fechaActual)) {
            datos.set("estadoTarjeta", "PREPARANDO_RESUMEN");
        }
        if (esPasado72HFechaCierre(fechaPasado72h, fechaActual)) {
            datos.set("estadoTarjeta", "RESUMEN_LISTO");
        }
    }

    // TODO AUT-183 mock para estados de pagos de TC
    protected static void estadoTarjetaPostVto(String vencimiento, Objeto datos, ContextoMB contexto) throws ParseException {
        Date fechaVto = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
        Date fechaVtoPasado4dias = Fecha.sumarDias(fechaVto, 4L);
        Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");

        if (fechaActual.before(fechaVtoPasado4dias) && fechaActual.after(fechaVto)) {
            datos.set("estadoTarjeta", "VERIFICANDO_PAGO");
        }

        // TODO obtener pago y su fecha
        Boolean isPagoParcial = false || contexto.idCobis().equals("3763892");
        Boolean isPagoTotal = false || contexto.idCobis().equals("5182519");
        Date fechaPasado7Dias = Fecha.sumarDias(fechaVto, 7L); // TODO ver que fecha se tiene que usar

        if (isPagoParcial && !esPasadoDDiasFecha(fechaPasado7Dias, fechaActual)) {
            datos.set("estadoTarjeta", "PAGO_PARCIAL");
        }

        if (isPagoTotal && !esPasadoDDiasFecha(fechaPasado7Dias, fechaActual)) {
            datos.set("estadoTarjeta", "AL_DIA");
        }

    }

    private static void estadoVencimiento(String vencimiento, Objeto datos) {
        Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
        LocalDate fechaHoy = LocalDate.now();
        LocalDate vencimientoLocalDate = Fecha.convertToLocalDateTime(fechaVencimiento).toLocalDate();
        long dias = calcularDias(vencimientoLocalDate, fechaHoy);
        if (dias == 3) {
            datos.set("estadoVencimiento", "TRES");
        } else if (dias == 2) {
            datos.set("estadoVencimiento", "DOS");
        } else if (dias == 1) {
            datos.set("estadoVencimiento", "UNO");
        } else if (dias == 0) {
            datos.set("estadoVencimiento", "HOY");
        }
    }

    private static long calcularDias(LocalDate fecha, LocalDate fechaHoy) {
        return diasDespuesAFecha(fechaHoy.toString(), fecha);
    }

    private static Long diasDespuesAFecha(String fechaCierre, LocalDate fechaHoy) {
        return ChronoUnit.DAYS.between(LocalDate.parse(fechaCierre), fechaHoy);
    }

    private static Date dateStringToDate(Date fecha, String formato) throws ParseException {
        String fechaActualString = new SimpleDateFormat(formato).format(fecha);
        return new SimpleDateFormat(formato).parse(fechaActualString);
    }

    private static boolean esDiaPosteriorFechaCierre(Date fechaPosteriorCierre, Date fechaActual) {
        return fechaActual.compareTo(fechaPosteriorCierre) == 0 || fechaActual.compareTo(fechaPosteriorCierre) > 0;
    }

    private static boolean esPasado72HFechaCierre(Date fechaPasado72h, Date fechaActual) {
        return fechaActual.compareTo(fechaPasado72h) == 0 || fechaActual.compareTo(fechaPasado72h) > 0;
    }

    private static boolean esPasadoDDiasFecha(Date fechaFutura, Date fechaActual) {
        return fechaActual.compareTo(fechaFutura) == 0 || fechaActual.compareTo(fechaFutura) > 0;
    }

    private static Boolean verificarBuhoOne(ContextoMB contexto) {
        try {
            RespuestaMB respuesta = MBProducto.productos(contexto);
            String codigoPaquete = respuesta.string("");
            return "37".equals(codigoPaquete) || "38".equals(codigoPaquete) || "42".equals(codigoPaquete) || "43".equals(codigoPaquete) || "53".equals(codigoPaquete);
        } catch (Exception e) {
            return false;
        }
    }

    public static RespuestaMB getCuentaTD(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        try {
            RespuestaMB respuesta = new RespuestaMB();
            String idCuenta = "";
            List<Cuenta> cuentas = contexto.cuentas();
            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);
            for (Cuenta cuenta : cuentas) {
                if (!idCuenta.isEmpty()) {
                    continue;
                }
                ApiResponseMB response = CuentasService.getTarjetaDebito(contexto, cuenta);
                for (Objeto tarjeta : response.objetos()) {
                    if (idTarjetaDebito.equals(tarjeta.string("numeroTarjeta")) || td.numero().equals(tarjeta.string("numeroTarjeta"))) {
                        idCuenta = cuenta.id();
                        continue;
                    }
                }
            }
            if (idCuenta.isEmpty()) {
                return RespuestaMB.estado("CUENTA_NO_ASOCIADA");
            }
            respuesta.set("idCuenta", idCuenta);
            return respuesta;
        } catch (Exception e) {
            return RespuestaMB.estado("CUENTA_NO_ASOCIADA");
        }
    }

    public static RespuestaMB altaCuentaEspecial(ContextoMB contexto) {
        try {
            MBProducto.AltaCuentaEspecial(contexto);
        } catch (Exception e) {
            return RespuestaMB.error();
        }
        return RespuestaMB.exito();
    }

    public static List<Objeto> movimientosCuentaCuotificacion(ContextoMB contexto, Cuenta cuenta, String fechaDesde, String fechaHasta) {
        int cantidadPaginas = 0;
        int maximaCantidadPaginas = ConfigMB.integer("movimientos_cuentas_max_paginas", 15);
        List<Objeto> movimientos = new ArrayList<>();

        for (Integer numeroPagina = 0; numeroPagina <= cantidadPaginas && numeroPagina <= maximaCantidadPaginas; numeroPagina++) {
            ApiRequestMB request = ApiMB.request("CuentasGetMovimientos", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}/movimientos", contexto);
            request.path("idcuenta", cuenta.numero());
            request.query("fechadesde", fechaDesde);
            request.query("fechahasta", fechaHasta);
            request.query("pendientes", "2");
            request.query("validactaempleado", "false");
            request.query("numeropagina", numeroPagina.toString());
            request.query("tipomovimiento", "T");
            request.query("orden", "D");

            ApiResponseMB response = ApiMB.response(request, cuenta.id());
            if (response.hayError())
                return movimientos;

            for (Objeto item : response.objetos()) {
                if (!item.existe("cantPaginas")) {
                    if (!item.string("causa").equals("4202"))
                        continue;

                    movimientos.add(new Objeto()
                            .set("numeroOperacion", item.string("secuencial"))
                            .set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"))
                            .set("descripcion", validarSignoParametria(item.string("descripcionMovimiento").toUpperCase()))
                            .set("importe", item.bigDecimal("valor").multiply(BigDecimal.valueOf(-1)))
                            .set("descripcionCausa", item.string("descCausa"))
                            .set("categoria", item.string("categoria"))
                            .set("subCategoria", item.string("subCategoria"))
                            .set("numeroCuenta", cuenta.numeroEnmascarado())
                            .set("hora", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss")));
                } else
                    cantidadPaginas = item.integer("cantPaginas");
            }
        }

        return movimientos;
    }

    public static Object ofertaCajaAhorroDolar(ContextoMB contextoMB) {
        RespuestaMB respuesta = new RespuestaMB();
        boolean mostrarModal = false;

        try {
            // Verifica si el usuario tiene cuentas en dólares
            if (!contextoMB.cuentasDolares().isEmpty()) {
                return respuesta.set("mostrarModal", false);
            }

            // Verifica si la funcionalidad está habilitada por configuración
            if (!ConfigMB.bool("prendido_oferta_caja_ahorro_dolar")) {
                return respuesta.set("mostrarModal", false);
            }

            // Llamadas asincrónicas
            Futuro<SqlResponseMB> futuroCobis = new Futuro<>(() -> getCobisOfertaCADolar(contextoMB.idCobis()));
            Futuro<SqlResponseMB> futuroContador = new Futuro<>(() -> Util.getContador(contextoMB, "ALERTA_CA_DOLARES"));

            SqlResponseMB cobisResponse = futuroCobis.get();
            SqlResponseMB contadorResponse = futuroContador.get();

            boolean existeCobis = !cobisResponse.hayError && !cobisResponse.registros.isEmpty();

            if (!existeCobis || contadorResponse.hayError) {
                return respuesta.set("mostrarModal", false);
            }

            List<Objeto> registros = contadorResponse.registros;

            if (registros.isEmpty()) {
                mostrarModal = true;
            } else if (registros.size() < 2) {
                for (Objeto registro : registros) {
                    long dias = Util.diferenciaDias(registro.date("momento"));
                    if (dias >= 7) {
                        mostrarModal = true;
                        break;
                    }
                }
            }

            if (mostrarModal) {
                contextoMB.parametros.set("nemonico", "ALERTA_CA_DOLARES");
                new Futuro<>(() -> Util.contador(contextoMB));
            }

        } catch (Exception ignored) {
        }

        return respuesta.set("mostrarModal", mostrarModal);
    }

    public static SqlResponseMB getCobisOfertaCADolar(String idCobis) {
        String DB_HBS = "hbs";
        /* ========== GET ========== */
        String SP_EXEC_GET = "EXEC [hbs].[dbo].[sp_VerificarExistenciaIdCobis] @idCobis = ?";
        String GET = "GetCobisOfertaCADolar";

        SqlRequestMB sqlRequest = SqlMB.request(GET, DB_HBS);

        sqlRequest.sql = SP_EXEC_GET;
        sqlRequest.parametros.add(idCobis);

        return SqlMB.response(sqlRequest);
    }
}
