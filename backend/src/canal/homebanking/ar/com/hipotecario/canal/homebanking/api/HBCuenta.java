package ar.com.hipotecario.canal.homebanking.api;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import ar.com.hipotecario.canal.homebanking.servicio.*;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.SesionHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaComitente;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.negocio.TipoSigno;

public class HBCuenta {

    public static Respuesta consolidadaCuentas(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        Boolean buscarEstadoVendedorCoelsa = contexto.parametros.bool("buscarEstadoVendedorCoelsa", false);

        Futuro<List<Cuenta>> cuentasFuture = new Futuro<>(contexto::cuentas);
        //Futuro<ApiResponse> productosResponse = new Futuro<>(() -> ProductosService.productos(contexto));

//        for (Objeto err : productosResponse.get().objetos("errores")) {
//            if ("cuentas".equals(err.string("codigo"))) {
//                return Respuesta.estado("ERROR_CONSOLIDADA");
//            }
//        }

        if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
            List<Cuenta> cuentasFiltradas = cuentasFuture.get()
                    .stream()
                    .filter(c -> idCuenta == null || idCuenta.isEmpty() || c.string("id", "").equals(idCuenta))
                    .toList();

            Map<String, Futuro<Boolean>> mapaActiva = new HashMap<>();
            Map<String, Futuro<Respuesta>> mapaBloqueos = new HashMap<>();
            Map<String, Futuro<String>> mapaCBUs = new HashMap<>();
            Map<String, Futuro<String>> mapaAlias = new HashMap<>();

            for (Cuenta c : cuentasFiltradas) {
                if (buscarEstadoVendedorCoelsa) {
                    mapaActiva.put(c.string("id", ""), new Futuro<>(() -> RestDebin.cuentaActivaVendedor(contexto, c)));
                }
                mapaBloqueos.put(c.string("id", ""), new Futuro<>(() -> bloqueosCuenta(contexto, c.string("numero", ""), "19")));
                mapaCBUs.put(c.string("id", ""), new Futuro<>(() -> c.cbuV4(c.string("numero", ""))));
                mapaAlias.put(c.string("id", ""), new Futuro<>(() -> c.aliasV4(c.string("numero", ""))));
            }

            for (Cuenta c : cuentasFiltradas) {
                if (buscarEstadoVendedorCoelsa) {
                    try { mapaActiva.get(c.string("numero", "")).get(); } catch (Exception ignored) {}
                }
                try { mapaBloqueos.get(c.string("numero", "")).get(); } catch (Exception ignored) {}
                try { mapaCBUs.get(c.string("numero", "")).get(); } catch (Exception ignored) {}
                try { mapaAlias.get(c.string("numero", "")).get(); } catch (Exception ignored) {}
            }

            Respuesta respuesta = new Respuesta();
            Objeto productos = new Objeto();
            Objeto adelantoExistente = new Objeto();

            for (Cuenta cuenta : cuentasFiltradas) {
                String estadoTd = "";
                if (ConfigHB.bool("prendido_link_td_habilitada", false)) {
                    try {
                        estadoTd = contexto.tarjetaDebitoAsociada(cuenta).estadoLink();
                    } catch (Exception ignored) {
                    }
                }

                Objeto item = new Objeto();
                //item.set("id", cuenta.string("id", ""));
                item.set("id", cuenta.idEncriptado());
                item.set("descripcion", cuenta.string("descripcion", ""));
                item.set("descripcionCorta", cuenta.string("descripcionCorta", ""));
                item.set("numeroFormateado", cuenta.string("numeroFormateado", ""));
                item.set("numeroEnmascarado", cuenta.string("numeroEnmascarado", ""));
                //item.set("ultimos4digitos", cuenta.ultimos4digitos());
                item.set("titularidad", cuenta.string("titularidad", ""));
                //item.set("idMoneda", cuenta.string("moneda", ""));
                item.set("moneda", cuenta.string("moneda", ""));
                item.set("simboloMoneda", cuenta.string("simboloMoneda", ""));
                item.set("estado", cuenta.string("estado", ""));
                item.set("saldo", cuenta.string("saldo", ""));
                item.set("saldoFormateado", cuenta.string("saldoFormateado", ""));
                //item.set("acuerdo", cuenta.acuerdo());
                //item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
                item.set("cbu", cuenta.cbu());
                item.set("alias", cuenta.alias());
                item.set("esCvu", cuenta.esCvu());
                //item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
                item.set("disponibleFormateado", cuenta.string("disponibleFormateado", ""));
                //item.set("esMAU", cuenta.categoria().equals("MAU"));
                item.set("fechaAlta", cuenta.string("fechaAlta", ""));
//				if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
//					item.set("adelantoDisponible", cuenta.adelantoDisponible());
//					item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
//					item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
//					item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
//					item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
//					item.set("adelantoInteresesDevengadosFormateado",
//							Formateador.importe(cuenta.adelantoInteresesDevengados()));
//					adelantoExistente = item;
//				}
                item.set("permiteCuotificacion", false);

                if (buscarEstadoVendedorCoelsa) {
                    try {
                        Boolean activa = mapaActiva.get( cuenta.string("id", "")).get();
                        item.set("estadoVendedorCoelsa", activa ? "ACTIVA" : "INACTIVA");
                    } catch (Exception e) {
                        item.set("estadoVendedorCoelsa", "DESCONOCIDO");
                    }
                }

                Respuesta bloqueos = mapaBloqueos.get( cuenta.string("id", "")).get();
                if ("0".equals(bloqueos.string("estado")) && !bloqueos.bool("consultaConErrores")) {
                    item.set("montoBloqueoProcrear", bloqueos.bigDecimal("montoTotalBloqueado"));
                    item.set("montoBloqueoProcrearFormateado", bloqueos.string("montoTotalBloqueadoFormateado"));
                    item.set("disponibleSinBloqueo", bloqueos.bigDecimal("disponibleSinBloqueo"));
                    item.set("disponibleSinBloqueoFormateado", bloqueos.string("disponibleSinBloqueoFormateado"));
                    item.set("mostrarMontoProcrear", bloqueos.bigDecimal("montoTotalBloqueado").compareTo(new BigDecimal(0)) > 0);
                }
                if ((idCuenta != null && !idCuenta.isEmpty()) || !item.equals(adelantoExistente)) {
                    productos.add("cuentas", item);
                }
            }

            if ((idCuenta != null && !idCuenta.isEmpty()) && productos.objetos("cuentas").size() == 1) {
                respuesta.set("cuentas", productos.objetos("cuentas"));
            } else {
                respuesta.set("cuentas", HBProducto.reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente));
            }

            Objeto datosExtras = new Objeto();
            datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
            datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebitoActivas().size() == 1);
            respuesta.set("datosExtras", datosExtras);

            respuesta.objeto("cuentas").ordenar("descripcionCorta", "simboloMoneda", "id");
            return respuesta;
        } else {
            List<Cuenta> cuentasFiltradas = cuentasFuture.get()
                    .stream()
                    .filter(c -> idCuenta == null || idCuenta.isEmpty() || c.id().equals(idCuenta))
                    .toList();

            Map<String, Futuro<Boolean>> mapaActiva = new HashMap<>();
            Map<String, Futuro<Respuesta>> mapaBloqueos = new HashMap<>();
            Map<String, Futuro<String>> mapaCBUs = new HashMap<>();
            Map<String, Futuro<String>> mapaAlias = new HashMap<>();

            for (Cuenta c : cuentasFiltradas) {
                if (buscarEstadoVendedorCoelsa) {
                    mapaActiva.put(c.id(), new Futuro<>(() -> RestDebin.cuentaActivaVendedor(contexto, c)));
                }
                mapaBloqueos.put(c.id(), new Futuro<>(() -> bloqueosCuenta(contexto, c.numero(), "19")));
                mapaCBUs.put(c.id(), new Futuro<>(() -> c.cbu()));
                mapaAlias.put(c.id(), new Futuro<>(() -> c.alias()));
            }

            for (Cuenta c : cuentasFiltradas) {
                if (buscarEstadoVendedorCoelsa) {
                    try { mapaActiva.get(c.id()).get(); } catch (Exception ignored) {}
                }
                try { mapaBloqueos.get(c.id()).get(); } catch (Exception ignored) {}
                try { mapaCBUs.get(c.id()).get(); } catch (Exception ignored) {}
                try { mapaAlias.get(c.id()).get(); } catch (Exception ignored) {}
            }

            Respuesta respuesta = new Respuesta();
            Objeto productos = new Objeto();
            Objeto adelantoExistente = new Objeto();

            for (Cuenta cuenta : cuentasFiltradas) {
                String estadoTd = "";
                if (ConfigHB.bool("prendido_link_td_habilitada", false)) {
                    try {
                        estadoTd = contexto.tarjetaDebitoAsociada(cuenta).estadoLink();
                    } catch (Exception ignored) {
                    }
                }

                Objeto item = new Objeto();
                //item.set("id", cuenta.id());
                item.set("id", cuenta.idEncriptado());
                item.set("tdInactiva", estadoTd.equalsIgnoreCase("INACTIVA"));
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
                    item.set("adelantoCuentaAsociada", cuenta.adelantoCuentaAsociada());
                    adelantoExistente = item;
                }
                item.set("permiteCuotificacion", false);

                if (buscarEstadoVendedorCoelsa) {
                    try {
                        Boolean activa = mapaActiva.get(cuenta.id()).get();
                        item.set("estadoVendedorCoelsa", activa ? "ACTIVA" : "INACTIVA");
                    } catch (Exception e) {
                        item.set("estadoVendedorCoelsa", "DESCONOCIDO");
                    }
                }

                Respuesta bloqueos = mapaBloqueos.get(cuenta.id()).get();
                if ("0".equals(bloqueos.string("estado")) && !bloqueos.bool("consultaConErrores")) {
                    item.set("montoBloqueoProcrear", bloqueos.bigDecimal("montoTotalBloqueado"));
                    item.set("montoBloqueoProcrearFormateado", bloqueos.string("montoTotalBloqueadoFormateado"));
                    item.set("disponibleSinBloqueo", bloqueos.bigDecimal("disponibleSinBloqueo"));
                    item.set("disponibleSinBloqueoFormateado", bloqueos.string("disponibleSinBloqueoFormateado"));
                    item.set("mostrarMontoProcrear", bloqueos.bigDecimal("montoTotalBloqueado").compareTo(new BigDecimal(0)) > 0);
                }
                if ((idCuenta != null && !idCuenta.isEmpty()) || !item.equals(adelantoExistente)) {
                    productos.add("cuentas", item);
                }
            }

            if ((idCuenta != null && !idCuenta.isEmpty()) && productos.objetos("cuentas").size() == 1) {
                respuesta.set("cuentas", productos.objetos("cuentas"));
            } else {
                respuesta.set("cuentas", HBProducto.reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente));
            }

            Objeto datosExtras = new Objeto();
            datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
            datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
            datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebitoActivas().size() == 1);
            respuesta.set("datosExtras", datosExtras);

            respuesta.objeto("cuentas").ordenar("descripcionCorta", "simboloMoneda", "id");
            return respuesta;
        }
    }

    public static ApiRequest consolidadoMovimientosCuentasRequest(ContextoHB contexto, Cuenta cuenta, String fechaDesde, String fechaHasta, Integer numeroPagina) {
        String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
        ApiRequest request = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
        request.path("idcuenta", cuenta.numero());
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("pendientes", "2");
        request.query("validactaempleado", "false");
        request.query("numeropagina", numeroPagina.toString());
        request.query("tipomovimiento", "T");
        request.query("orden", "D");
        return request;
    }

    public static Respuesta consolidadoMovimientosCuentas(String fechaDesde, String fechaHasta, List<Cuenta> cuentas, ContextoHB contexto) {

        Integer cantidadPaginas = 0;
        Respuesta respuesta = new Respuesta();
        List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();

        Map<String, Futuro<ApiResponse>> mapaRespuestas = new HashMap<>();
        for (Cuenta cuenta : cuentas) {
            ApiRequest request = consolidadoMovimientosCuentasRequest(contexto, cuenta, fechaDesde, fechaHasta, 1);
            Futuro<ApiResponse> futuro = new Futuro<>(() -> Api.response(request, cuenta.numero()));
            mapaRespuestas.put(cuenta.numero() + ":" + "1", futuro);
        }

        Map<String, Integer> mapaCantidadPaginas = new HashMap<>();
        for (Cuenta cuenta : cuentas) {
            ApiResponse response = mapaRespuestas.get(cuenta.numero() + ":" + "1").get();
            for (Objeto item : response.objetos()) {
                if (item.existe("cantPaginas")) {
                    mapaCantidadPaginas.put(cuenta.numero(), item.integer("cantPaginas"));
                    break;
                }
            }
        }

        Integer maximaCantidadPaginas = ConfigHB.integer("hb_movimientos_cuentas_max_paginas", 10);
        Boolean superaLimitePaginas = false;

        for (Cuenta cuenta : cuentas) {
            Integer numeroPagina = 0;
            do {
                ++numeroPagina;

                if (numeroPagina > maximaCantidadPaginas) {
                    superaLimitePaginas = true;
                    break;
                }

                cantidadPaginas = mapaCantidadPaginas.get(cuenta.numero());
                if (cantidadPaginas != null && cantidadPaginas > 1 && numeroPagina > 1) {
                    ApiRequest request = consolidadoMovimientosCuentasRequest(contexto, cuenta, fechaDesde, fechaHasta, numeroPagina);
                    mapaRespuestas.put(cuenta.numero() + ":" + numeroPagina, new Futuro<>(() -> Api.response(request, cuenta.numero())));
                }
            } while (cantidadPaginas != null && numeroPagina < cantidadPaginas);
        }

        for (Cuenta cuenta : cuentas) {
            Integer numeroPagina = 0;
            do {
                ++numeroPagina;

                if (numeroPagina > maximaCantidadPaginas) {
                    superaLimitePaginas = true;
                    break;
                }

                cantidadPaginas = mapaCantidadPaginas.get(cuenta.numero());
                if (cantidadPaginas != null) {
                    ApiResponse response = mapaRespuestas.get(cuenta.numero() + ":" + numeroPagina).get();
                    if (!response.hayError() && !response.objetos().isEmpty()) {
                        for (Objeto item : response.objetos()) {
                            if (!item.existe("cantPaginas")) {
                                ultimosMovimientos.add(movimientosXCuenta(item, cuenta));
                            }
                        }
                    }
                }
            } while (cantidadPaginas != null && numeroPagina < cantidadPaginas);
        }

        if (ultimosMovimientos.isEmpty()) {
            return Respuesta.estado("SIN_MOVIMIENTOS");
        }

        respuesta.set("ultimosMovimientos", ultimosMovimientos);
        respuesta.set("superaLimitePaginas", superaLimitePaginas);

        return respuesta;
    }

    public static Respuesta consolidado5MovimientosCuentas(List<Cuenta> cuentas, ContextoHB contexto) {

        Respuesta respuesta = new Respuesta();
        List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();
        String cuentasAConsultar = cuentas.stream().map(Cuenta::numero).collect(Collectors.joining(","));
        String url = "/v1/cuentas/movimientos";

        ApiRequest request = Api.request("CuentasGetMovimientosConsolidados", "cuentas", "GET", url, contexto);
        request.query("cuentas", cuentasAConsultar);
        request.query("validaCuenta", "N");
        request.query("file", "N");
        request.query("from", "N");

        ApiResponse response = Api.response(request, cuentasAConsultar);
        if (!response.hayError() && !response.objetos().isEmpty()) {
            ultimosMovimientos.addAll(ultimosMovimientos(response.objetos(), cuentas));
        }

        if (ultimosMovimientos.isEmpty()) {
            return Respuesta.estado("SIN_MOVIMIENTOS");
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
        datosAdicionales.set("nroOperacion", movimiento.string("secuencial"));
        datosAdicionales.set("hora", movimiento.string("hora"));
        datosAdicionales.set("fechaMovimiento", movimiento.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
        datosAdicionales.set("categoria", movimiento.string("categoria"));
        datosAdicionales.set("subCategoria", movimiento.string("subCategoria"));
        datosAdicionales.set("tipoMsg", movimiento.string("tipoMsg"));
        datosAdicionales.set("cuitCP", movimiento.string("cuitCP"));
        datosAdicionales.set("idEstablecimiento", movimiento.string("codComercio"));
        datosAdicionales.set("rubro", movimiento.string("codRubro"));
        checkDataComprobante(movimiento, datosAdicionales);
        return setearMovimiento(movimiento, cuenta, "descripcionMovimiento", "valor", fecha, datosAdicionales);
    }

    private static Objeto ultimosMovimientosXCuenta(Objeto movimiento, Cuenta cuenta) {
        String fecha = movimiento.string("fecha");
        return setearMovimiento(movimiento, cuenta, "descripcion", "monto", fecha, null);
    }

    private static Objeto setearMovimiento(Objeto movimiento, Cuenta cuenta, String campoDescripcion, String campoImporte, String fecha, Objeto datosAdicionales) {

        Objeto itemCuenta = new Objeto();
        //itemCuenta.set("id", cuenta.id());
        itemCuenta.set("id", cuenta.idEncriptado());
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

    public static Respuesta modificarAliasCuenta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String alias = contexto.parametros.string("alias");

        if (Objeto.anyEmpty(idCuenta, alias)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        ApiResponse responseCoelsa = CuentasService.cuentaCoelsa(contexto, cuenta.cbu());
        Boolean tieneAlias = true;
        if (responseCoelsa.hayError()) {
            if (responseCoelsa.string("mensajeAlUsuario").contains("NO TIENE ALIAS ASIGNADO")) {
                tieneAlias = false;
            }
        }
        ApiRequest request;
        if (tieneAlias) {
            request = Api.request("CuentasPatchAlias", "cuentas", "PATCH", "/v1/alias", contexto);
            request.body("cuit", contexto.persona().cuit());
            request.body("nro_bco", "044");
            request.body("nro_cbu", cuenta.cbu());
            request.body("reasigna", null);
            request.body("valor", alias);
            request.body("valor_original", responseCoelsa.string("nuevoAlias"));
        } else {
            request = Api.request("CuentasPutAlias", "cuentas", "PUT", "/v1/alias", contexto);
            request.body("cuit", contexto.persona().cuit());
            request.body("nro_bco", "044");
            request.body("nro_cbu", cuenta.cbu());
            request.body("reasigna", null);
            request.body("valor", alias);
            request.body("valor_original", responseCoelsa.string("nuevoAlias"));
        }

        ApiResponse response = Api.response(request, cuenta.numero());
        if (response.hayError()) {
            if ("0380".equals(response.string("codigo"))) {
                return Respuesta.estado("ERROR_TIEMPO_PERMITIDO");
            }
            if ("0470".equals(response.string("codigo"))) {
                return Respuesta.estado("ERROR_ALIAS_IDENTICOS_O_CAMPOS_INVERTIDOS");
            }
            if ("0420".equals(response.string("codigo"))) {
                return Respuesta.estado("ERROR_ALIAS_YA_CREADO_CON_OTRO_CUIT");
            }
            if ("9904".equals(response.string("codigo")) && !tieneAlias) {
                // Esto lo comento, capaz que si no anda de una el post lo puedo llegar a meter
                // acá
                // emm: dio ALIAS MAL FORMADO, y no tiene alias
                // intento mandarlo de nuevo, pero en el alias original le pongo como nuevo
                // alias el que
                // acaba de poner porque me dicen de MW que es dato obligatorio (es más una
                // prueba esto)
                /*
                 * ApiRequest request2 = Api.request("CuentasPatchAlias", "cuentas", "PATCH",
                 * "/v1/alias", contexto); request2.body("cuit", contexto.persona().cuit());
                 * request2.body("nro_bco", "044"); request2.body("nro_cbu", cuenta.cbu());
                 * request2.body("reasigna", null); request2.body("valor", alias); //
                 * request2.body("valor_original", alias); ApiResponse response2 =
                 * Api.response(request2, cuenta.numero()); if (response2.hayError()) { return
                 * Respuesta.error(); } else {
                 * CuentasService.eliminarCacheCuentaCoelsa(contexto, alias);
                 * CuentasService.eliminarCacheCuentaCoelsa(contexto,
                 * responseCoelsa.string("nuevoAlias")); return Respuesta.exito(); }
                 */
                return Respuesta.error();
            }

            return Respuesta.error();
        }

        CuentasService.eliminarCacheCuentaCoelsa(contexto, alias);
        CuentasService.eliminarCacheCuentaCoelsa(contexto, responseCoelsa.string("nuevoAlias"));
        return Respuesta.exito();
    }

    public static Respuesta modificarComentarioCuenta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String comentario = contexto.parametros.string("comentario");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (comentario.length() > 25) {
            return Respuesta.estado("SUPERA_LONGITUD_MAXIMA");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        SqlRequest sqlRequest = Sql.request("InsertOrUpdateComentarioCuenta", "homebanking");
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

        SqlResponse sqlResponse = Sql.response(sqlRequest);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }

//		cuenta.eliminarCacheSelectComentarioCuenta();
        return Respuesta.exito();
    }

    public static Respuesta movimientosCuenta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd");
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd");
        Boolean soloCompra = contexto.parametros.bool("soloCompra", false);

        if (Objeto.anyEmpty(idCuenta, fechaDesde, fechaHasta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        // verifico si es la primera llamada, en caso de ser asi solo muestro la primera pagina
        SesionHB sesion = contexto.sesion();
        Boolean primeraLlamada = sesion.ultimaCuentaConsultadaMovimientos == null;
        primeraLlamada |= !idCuenta.equals(sesion.ultimaCuentaConsultadaMovimientos);
        sesion.ultimaCuentaConsultadaMovimientos = idCuenta;
        if (ConfigHB.bool("hb_apagar_parche_movimientos", false)) {
            primeraLlamada = false;
        }
        // fin de la comprobacion de si es la primera llamada

        Integer cantidadPaginas = 0;
        List<Objeto> movimientos = new CopyOnWriteArrayList<>();
        Integer maximaCantidadPaginas = ConfigHB.integer("hb_movimientos_cuentas_max_paginas", 10);

        String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
        ApiRequest requestPrimero = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
        requestPrimero.path("idcuenta", cuenta.numero());
        requestPrimero.query("fechadesde", fechaDesde);
        requestPrimero.query("fechahasta", fechaHasta);
        requestPrimero.query("pendientes", "2");
        requestPrimero.query("validactaempleado", "false");
        requestPrimero.query("numeropagina", "1");
        requestPrimero.query("tipomovimiento", "T");
        requestPrimero.query("orden", "D");

        ApiResponse responsePrimero = Api.response(requestPrimero, cuenta.id());
        if (responsePrimero.hayError()) {
            if (responsePrimero.string("mensajeAlUsuario").contains("fecha comprendida entre la fecha actual y 12 meses anteriores")) {
                return Respuesta.estado("FECHA_DESDE_ANTERIOR_12_MESES");
            }
            if (responsePrimero.string("codigo").equals("100")) {
                return Respuesta.estado("SIN_MOVIMIENTOS");
            }

            return Respuesta.error();
        }

        if (responsePrimero.codigo == 204) {
            return Respuesta.estado("SIN_MOVIMIENTOS");
        }

        for (Objeto item : responsePrimero.objetos()) {
            if (!item.existe("cantPaginas")) {

                if (soloCompra && !item.string("causa").equals("4202")) {
                    continue;
                }

                Objeto movimiento = new Objeto();
                movimiento.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                String descripcion = validarSignoParametria(item.string("descripcionMovimiento").toUpperCase());
                movimiento.set("descripcion", descripcion);
                movimiento.set("importe", item.bigDecimal("valor"));
                movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
                movimiento.set("saldo", item.bigDecimal("saldo"));
                movimiento.set("saldoFormateado", Formateador.importe(item.bigDecimal("saldo")));
                movimiento.set("simboloMoneda", cuenta.simboloMoneda());
                movimiento.set("nroOperacion", item.string("secuencial"));
                movimiento.set("hora", item.string("hora"));
                movimiento.set("fechaMovimiento", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                movimiento.set("categoria", item.string("categoria"));
                movimiento.set("subCategoria", item.string("subCategoria"));
                movimiento.set("tipoMsg", item.string("tipoMsg"));
                movimiento.set("cuitCP", item.string("cuitCP"));
                movimiento.set("idEstablecimiento", item.string("codComercio"));
                movimiento.set("rubro", item.string("codRubro"));
                movimiento.set("referencia", item.string("referencia"));
                checkDataComprobante(item, movimiento);
                movimientos.add(movimiento);
            } else {
                cantidadPaginas = item.integer("cantPaginas");
                if (primeraLlamada) {
                    cantidadPaginas = 1;
                }
            }
        }

        Boolean superaLimitePaginas = false;
        if (cantidadPaginas > 1) {

            try {

                ExecutorService executorService = Executors.newCachedThreadPool();
                for (Integer i = 2; i < cantidadPaginas + 1; i++) {
                    Integer numeroPagina = i;

                    if (numeroPagina > maximaCantidadPaginas) {
                        superaLimitePaginas = true;
                        break;
                    }

                    executorService.submit(() -> {

                        ApiRequest request = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
                        request.path("idcuenta", cuenta.numero());
                        request.query("fechadesde", fechaDesde);
                        request.query("fechahasta", fechaHasta);
                        request.query("pendientes", "2");
                        request.query("validactaempleado", "false");
                        request.query("numeropagina", numeroPagina.toString());
                        request.query("tipomovimiento", "T");
                        request.query("orden", "D");

                        ApiResponse response = Api.response(request, cuenta.id());
                        if (response.hayError()) {
                            throw new RejectedExecutionException();
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
                                movimiento.set("importe", item.bigDecimal("valor"));
                                movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
                                movimiento.set("saldo", item.bigDecimal("saldo"));
                                movimiento.set("saldoFormateado", Formateador.importe(item.bigDecimal("saldo")));
                                movimiento.set("simboloMoneda", cuenta.simboloMoneda());
                                movimiento.set("nroOperacion", item.string("secuencial"));
                                movimiento.set("hora", item.string("hora"));
                                movimiento.set("fechaMovimiento", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                                movimiento.set("categoria", item.string("categoria"));
                                movimiento.set("subCategoria", item.string("subCategoria"));
                                movimiento.set("tipoMsg", item.string("tipoMsg"));
                                movimiento.set("cuitCP", item.string("cuitCP"));
                                movimiento.set("idEstablecimiento", item.string("codComercio"));
                                movimiento.set("rubro", item.string("codRubro"));
                                movimiento.set("referencia", item.string("referencia"));
                                checkDataComprobante(item, movimiento);
                                movimientos.add(movimiento);
                            }
                        }
                    });
                }

                Concurrencia.esperar(executorService, null, 60);
            } catch (RejectedExecutionException e) {
                return Respuesta.error();
            }
        }

        Respuesta respuesta = new Respuesta();
        // TODO: si estamos buscando soloCompras
        if (soloCompra) {
            // verificar por segmento si es buho
            respuesta.set("mercadoPagoHabilitado", verificarBuhoOne(contexto));
            // verificar si pasaron 10 dias habiles
            respuesta.set("fechaHastaReclamo", Util.calcularFechaNdiasHabilesPasado(contexto, Fecha.fechaActual(), -10));
        }

        respuesta.set("movimientos", movimientos);
        respuesta.set("superaLimitePaginas", superaLimitePaginas);
        return respuesta;
    }

    public static Respuesta historicoMovimientosCuenta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String categoria = contexto.parametros.string("categoria");
        String subCategoria = contexto.parametros.string("subCategoria");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        String cuitCP = contexto.parametros.string("cuitCP");
        String idEstablecimiento = contexto.parametros.string("idEstablecimiento");

        String fechaDesde = contexto.parametros.date("fechaMovimiento", "d/M/yyyy", "yyyy-MM-01");
        String fechaHasta = obtenerFechaHastaHistoricoMovimientos(contexto.parametros.date("fechaMovimiento", "d/M/yyyy", "yyyy-MM-dd"));

        if (Objeto.anyEmpty(idCuenta, categoria, subCategoria, importe)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        if (!mostrarHistoricoMovimientos(categoria, subCategoria)) {
            return Respuesta.estado("COMBINACION_NO_DISPONIBLE");
        }

        Integer numeroPagina = 0;
        Integer cantidadPaginas = 0;
        Respuesta respuesta = new Respuesta();
        List<Objeto> movimientos = new ArrayList<>();

        Integer maximaCantidadPaginas = ConfigHB.integer("hb_movimientos_cuentas_max_paginas", 10);
        Boolean superaLimitePaginas = false;

        do {
            ++numeroPagina;

            if (numeroPagina > maximaCantidadPaginas) {
                superaLimitePaginas = true;
                break;
            }

            String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
            ApiRequest request = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
            request.path("idcuenta", cuenta.numero());
            request.query("fechadesde", fechaDesde);
            request.query("fechahasta", fechaHasta);
            request.query("pendientes", "2");
            request.query("validactaempleado", "false");
            request.query("numeropagina", numeroPagina.toString());
            request.query("tipomovimiento", "T");
            request.query("orden", "D");

            ApiResponse response = Api.response(request, cuenta.id());
            if (response.hayError()) {
                if (response.string("mensajeAlUsuario").contains("fecha comprendida entre la fecha actual y 12 meses anteriores")) {
                    return Respuesta.estado("FECHA_DESDE_ANTERIOR_12_MESES");
                }
                if (response.string("codigo").equals("100")) {
                    return Respuesta.estado("SIN_MOVIMIENTOS");
                }
                return Respuesta.error();
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
            return Respuesta.estado("MOVIMIENTO_NO_EXISTE");
        }

        if ("TRANSFERENCIAS".equalsIgnoreCase(categoria) && "ENTRANTE".equalsIgnoreCase(subCategoria) || "SALIENTE".equalsIgnoreCase(subCategoria)) {

            if (StringUtils.isEmpty(cuitCP)) {
                return Respuesta.parametrosIncorrectos();
            }
            filtrarTransferencias(movimientos, cuitCP);
        }

        if ("COMPRAS".equalsIgnoreCase(categoria) && "TARJETA DE DEBITO".equalsIgnoreCase(subCategoria)) {
            if (StringUtils.isEmpty(idEstablecimiento)) {
                return Respuesta.parametrosIncorrectos();
            }
            filtrarComerciosComprasTD(movimientos, idEstablecimiento);
        }

        if (movimientos.size() == 1) {
            return Respuesta.estado("UNICO_MOVIMIENTO");
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
        respuesta.set("superaLimitePaginas", superaLimitePaginas);
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

        boolean tieneComprobante = ConfigHB.getCausales().containsKey(causa) && importe.compareTo(new BigDecimal(0)) < 0;

        movimiento.set("tieneComprobante", tieneComprobante);
        movimiento.set("causa", causa);

        if (tieneComprobante) {
            movimiento.set("tipo", ConfigHB.getCausales().get(causa));
        }
    }

    public static Respuesta consolidadoImpuestos(ContextoHB contexto) {
        String año = contexto.parametros.string("anio");

        if (Objeto.anyEmpty(año)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiRequest request = Api.request("ProductosGetImpuestos", "productos", "GET", "/v1/clientes/{id}/impuestos", contexto);
        request.path("id", contexto.idCobis());
        request.query("anio", año);

        ApiResponse response = Api.response(request, contexto.idCobis(), año);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
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

    public static Respuesta cajaAhorroBloqueos(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        String numeroCorto = cuenta.numeroCorto();
        if ("".equals(numeroCorto)) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("consultaConErrores", false);
        ApiResponse responseBloqueos = CuentasService.cajaAhorroBloqueos(contexto, numeroCorto);
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

    public static Respuesta bloqueosCuenta(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String numeroCausal = contexto.parametros.string("numeroCausal");
        if (Objeto.anyEmpty(idCuenta)) {
            Respuesta respuesta = new Respuesta();
            respuesta.set("tieneBloqueos", false);
            for (Cuenta cuenta : contexto.cuentas()) {
                ApiResponse responseBloqueos = CuentasService.cuentaBloqueos(contexto, cuenta.numero(), cuenta.idTipo());
                if (responseBloqueos.hayError()) {
                    return Respuesta.error();
                }
                if (responseBloqueos.codigo != 204) {
                    respuesta.set("tieneBloqueos", true);
                }
            }
            return respuesta;
        }

        return bloqueosCuenta(contexto, idCuenta, numeroCausal);
    }

    public static Respuesta bloqueosCuenta(ContextoHB contexto, String idCuenta, String numeroCausal) {
        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (numeroCausal == null)
            numeroCausal = "";

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("consultaConErrores", false);

        ApiResponse responseBloqueos = CuentasService.cuentaBloqueos(contexto, cuenta.numero(), cuenta.idTipo());

        boolean tieneBloqueos = false;
        BigDecimal montoBloqueo = new BigDecimal(0);
        if (responseBloqueos.hayError()) {
            respuesta.set("consultaConErrores", true);
            return respuesta;
        }
        for (Objeto item : responseBloqueos.objetos()) {
            if ("".equals(numeroCausal) || numeroCausal.equals(item.string("numero_causal"))) {
                Objeto bloqueo = new Objeto();
                bloqueo.set("numeroCausal", item.string("numero_causal"));
                bloqueo.set("descripcionCausal", item.string("desc_causal"));
                bloqueo.set("montoBloqueo", item.bigDecimal("monto_bloqueo"));
                bloqueo.set("montoBloqueoFormateado", Formateador.importe(item.bigDecimal("monto_bloqueo")));
                montoBloqueo = montoBloqueo.add(item.bigDecimal("monto_bloqueo"));
                tieneBloqueos = true;
                respuesta.add("bloqueos", bloqueo);
            }
        }
        respuesta.set("montoTotalBloqueado", montoBloqueo);
        respuesta.set("montoTotalBloqueadoFormateado", Formateador.importe(montoBloqueo));

        BigDecimal disponible = cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0"));
        respuesta.set("disponibleSinBloqueo", disponible.subtract(montoBloqueo));
        respuesta.set("disponibleSinBloqueoFormateado", Formateador.importe(disponible.subtract(montoBloqueo)));
        respuesta.set("tieneBloqueos", tieneBloqueos);
        return respuesta;
    }

    public static Respuesta cuentasComitentesAsociadas(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }
        Respuesta respuesta = new Respuesta();
        respuesta.set("tieneCuentasComitentesAsociadas", false);
        for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
            ApiResponse responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto, itemCuentaComitente.numero());
            if (responseCuentasComitentes.hayError()) {
                return Respuesta.estado("ERROR_CONSULTANDO_COMITENTES");
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
        return respuesta;
    }

    public static Respuesta compartirCBU(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String email = contexto.parametros.string("email");
        String mensaje = contexto.parametros.string("mensaje");

        if (Objeto.anyEmpty(idCuenta, email)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
        requestMail.body("para", email);
        requestMail.body("plantilla", ConfigHB.string("doppler_cbu"));
        Objeto parametros = requestMail.body("parametros");
        parametros.set("Subject", "Información Datos Cuenta Home Banking");
        parametros.set("CBU", "<b>CBU: </b>" + cuenta.cbuFormateado() + ". <b>Alias: </b>" + cuenta.alias());
        parametros.set("CUENTA", "<b>Número de cuenta: </b>" + cuenta.numeroFormateado());
        parametros.set("TITULAR", "<b>Titular de la cuenta: </b>" + contexto.persona().apellidos() + " " + contexto.persona().nombres());
        parametros.set("CUIL_CUIT_CDI", "<b>CUIL/CUIT/CDI: </b>" + contexto.persona().cuitFormateado());
        parametros.set("TIPO_CUENTA", "<b>Tipo de cuenta: </b>" + cuenta.producto());
        parametros.set("COMENTARIO", "<b>Mensaje: </b>" + mensaje);

        ApiResponse responseMail = Api.response(requestMail);
        if (responseMail.hayError()) {
            return Respuesta.error();
        }

        return Respuesta.exito();
    }

    public static Respuesta valoresSuspenso(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.error();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        ApiRequest request = null;
        request = Api.request("CuentasGetValorSuspenso", "cuentas", "GET", "/v1/cuentas/{idCuenta}/valoresensuspenso", contexto);
        request.path("idCuenta", cuenta.numero());
        request.query("operacion", "P");
        request.query("secuencial", "0");
        request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
        request.cacheSesion = true;
        request.cache204 = true;

        ApiResponse response = Api.response(request, cuenta.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }
        if (response.codigo == 204) {
            return Respuesta.estado("SIN_VALORES_SUSPENSO");
        }

        Respuesta respuesta = new Respuesta();
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

    public static Respuesta consolidadaResumenCuenta(ContextoHB contexto) {
        if (contexto.cuentas().isEmpty()) {
            return Respuesta.estado("SIN_CUENTAS");
        }

        Respuesta respuesta = new Respuesta();
        for (Cuenta cuenta : contexto.cuentas()) {
            Objeto item = new Objeto();
            //item.set("id", cuenta.id());
            item.set("id", cuenta.idEncriptado());
            item.set("descripcionCorta", cuenta.descripcionCorta());
            item.set("simboloMoneda", cuenta.simboloMoneda());
            item.set("ultimos4digitos", cuenta.ultimos4digitos());
            respuesta.add("cuentas", item);

            int añoActual = Calendar.getInstance().get(Calendar.YEAR);
            int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1;

            int añoCuenta = Integer.valueOf(cuenta.fechaAlta("yyyy"));

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

    public static Respuesta periodosResumenCuenta(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
        Integer año = contexto.parametros.integer("año");

        if (Objeto.anyEmpty(id, año)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(id);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_ENCONTRADA");
        }

        Respuesta respuesta = new Respuesta();
        int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int añoActual = Calendar.getInstance().get(Calendar.YEAR);

        if (año == añoActual && mesActual == 1) {
            return Respuesta.estado("AÑO_INCORRECTO");
        }
        if (año > añoActual) {
            return Respuesta.estado("AÑO_INCORRECTO");
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

    public static Object resumenPlazoFijo(ContextoHB contexto) {
        contexto.parametros.set("plazoFijo", true);
        return resumenCuenta(contexto);
    }

    public static Object rentaFinanciera(ContextoHB contexto) {
        String anio = contexto.parametros.string("anio");

        ApiRequest request = Api.request("RentaFinanciera", "inversiones", "GET", "/v1/rentafinanciera", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("idCobis", contexto.idCobis());
        request.query("periodo", anio);

        ApiResponse response = Api.response(request, contexto.idCobis(), anio);
        if (response.hayError()) {
            contexto.responseHeader("estado", "ERROR");
            return Respuesta.error();
        }

        if (response.codigo == 204) {
            contexto.responseHeader("estado", "SIN_MOVIMIENTOS");
            return Respuesta.error();
        }

        byte[] archivo = Base64.getDecoder().decode(response.string("file"));
        contexto.responseHeader("Content-Type", "application/pdf; name=" + anio + ".pdf");
        contexto.responseHeader("estado", "0");
        return archivo;
    }

    public static Object resumenCuenta(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
        Boolean plazoFijo = contexto.parametros.bool("plazoFijo", false);

        if (Objeto.anyEmpty(id)) {
            contexto.responseHeader("estado", "ERROR");
            return Respuesta.parametrosIncorrectos();
        }

        String idCuenta = id.split("_")[0];
        String periodo = id.split("_")[1];
        String año = id.split("_")[2];

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            contexto.responseHeader("estado", "CUENTA_NO_EXISTE");
            return Respuesta.estado("CUENTA_NO_EXISTE");
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
                fechaDesde = año + "-" + "0" + mesDesde + "-" + "01";
                fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
            } catch (Exception e) {
                contexto.responseHeader("estado", "ERROR");
                return Respuesta.error();
            }
        }

        if (cuenta.esCuentaCorriente()) {
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + periodo + "/" + año);
                fecha = Fecha.sumarMeses(fecha, 1);
                fecha = Fecha.restarDias(fecha, 1L);
                fechaDesde = año + "-" + periodo + "-" + "01";
                fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
            } catch (Exception e) {
                contexto.responseHeader("estado", "ERROR");
                return Respuesta.error();
            }
        }

        ApiRequest request = Api.request("ResumenCuenta", "cuentas", "GET", "/v1/cuentas/{numero}/resumen", contexto);
        request.path("numero", cuenta.numero());
        request.query("desde", fechaDesde);
        request.query("hasta", fechaHasta);
        request.query("producto", cuenta.descripcionCorta());

        if (plazoFijo) {
            request = Api.request("ResumenPlazoFijo", "cuentas", "GET", "/v1/cuentas/{numero}/resumen", contexto);
            request.path("numero", cuenta.numero());
            request.query("desde", fechaDesde);
            request.query("hasta", fechaHasta);
            request.query("producto", "PF");
        }

        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            contexto.responseHeader("estado", "ERROR");
            return Respuesta.error();
        }
        if (response.codigo == 204) {
            contexto.responseHeader("estado", "SIN_RESUMEN");
            return Respuesta.estado("SIN_RESUMEN");
        }

        byte[] archivo = Base64.getDecoder().decode(response.string("file"));
        contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
        contexto.responseHeader("estado", "0");
        return archivo;
    }

    public static Respuesta actualizarMarcaResumen(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        Boolean resumenDigital = contexto.parametros.bool("resumenDigital");

        if (Objeto.anyEmpty(idCuenta, resumenDigital)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_INEXISTENTE");
        }

        ApiResponse response = CuentasService.actualizarMarcaResumen(contexto, cuenta, resumenDigital);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        return respuesta;
    }

    public static Respuesta consolidadaCambioCuentaPrincipal(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            Objeto item = new Objeto();
            //item.set("id", tarjetaDebito.id());
            item.set("id", tarjetaDebito.idEncriptado());
            item.set("descripcion", tarjetaDebito.producto());
            item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
            item.set("titularidad", tarjetaDebito.titularidad());
            for (Cuenta cuenta : tarjetaDebito.cuentasAsociadas()) {
                Objeto itemCuenta = new Objeto();
                //itemCuenta.set("id", cuenta.id());
                itemCuenta.set("id", cuenta.idEncriptado());
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
        datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebitoActivas().size() == 1);
        respuesta.set("datosExtras", datosExtras);

        return respuesta;
    }

    public static Respuesta cambiarCuentaPrincipal(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String idCuenta = contexto.parametros.string("idCuenta");
        Boolean exterior = contexto.parametros.bool("exterior", null);

        if (Objeto.anyEmpty(idTarjetaDebito, idCuenta, exterior)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return Respuesta.estado("TARJETA_DEBITO_NO_ENCONTRADA");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_ENCONTRADA");
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

            ApiRequest request = Api.request("ModificarCuentaPrincipalPais", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}", contexto);
            request.path("nrotarjeta", tarjetaDebito.numero());
            request.query("nrotarjeta", tarjetaDebito.numero());
            request.body("codigoCliente", contexto.idCobis());
            request.body("tipoTarjeta", tarjetaDebito.idTipoTarjeta());
            request.body("limiteRetiro", limiteRetiro);
            request.add("cuentaOperativas", cuentaOperativa);
            ApiResponse response = Api.response(request, contexto.idCobis(), tarjetaDebito.numero(), cuenta.numero());
            if (response.hayError()) {
                if (response.string("codigo").equals("1831602")) {
                    return Respuesta.estado("SOLICITUD_EN_CURSO");
                }
                if (response.string("codigo").equals("40003")) {
                    return Respuesta.estado("FUERA_HORARIO");
                }
                return Respuesta.error();
            }
        }

        if (exterior) {
            ApiRequest request = Api.request("ModificarCuentaPrincipalExterior", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}/cuentaexterior", contexto);
            request.path("nrotarjeta", tarjetaDebito.numero());
            request.body("id_app", Integer.valueOf(Util.idProceso()));
            request.body("reverso", "N");
            request.body("servicio", 200);
            request.body("cuenta_oper1", cuenta.numero());
            request.body("asoc_exterior", "S");
            ApiResponse response = Api.response(request, contexto.idCobis(), tarjetaDebito.numero(), cuenta.numero());
            if (response.hayError()) {
                if (response.string("codigo").equals("1831602")) {
                    return Respuesta.estado("SOLICITUD_EN_CURSO");
                }
                if (response.string("codigo").equals("40003")) {
                    return Respuesta.estado("FUERA_HORARIO");
                }
                return Respuesta.error();
            }
        }
        return Respuesta.exito();
    }

    public static Respuesta consultaCuenta(ContextoHB contexto) {
        String cbu = contexto.parametros.string("cbu");
        String banco = "";

        if (Objeto.empty(cbu)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiResponse responseCoelsa = CuentasService.cuentaCoelsa(contexto, cbu);

        if (responseCoelsa.hayError()) {
            contexto.responseHeader("estado", "ERROR");
            return Respuesta.error();
        }

        if (StringUtils.isNotBlank(responseCoelsa.string("nroBco"))) {
            ApiRequest request = Api.request("CodigoBancos", "catalogo", "GET", "/v1/codigosBancos	", contexto);
            request.query("codigoBanco", responseCoelsa.string("nroBco"));
            ApiResponse response = Api.response(request);
            if (!response.hayError()) {
                banco = response.objetos().get(0).string("Descripcion");
            }
        }

        Respuesta respuesta = new Respuesta();
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
                subCategorias = ConfigHB.string("subcategorias_historico_deposito");
                break;
            case "EXTRACCIONES":
                subCategorias = ConfigHB.string("subcategorias_historico_extracciones");
                break;
            case "HABERES":
                subCategorias = ConfigHB.string("subcategorias_historico_haberes");
                break;
            case "INVERSIONES":
                subCategorias = ConfigHB.string("subcategorias_historico_inversiones");
                break;
            case "PAGOS":
                subCategorias = ConfigHB.string("subcategorias_historico_pagos");
                break;
            case "PROMOCIONES Y BENEFICIOS":
                subCategorias = ConfigHB.string("subcategorias_historico_promociones_y_beneficios");
                break;
            case "TRANSFERENCIAS":
                subCategorias = ConfigHB.string("subcategorias_historico_transferencias");
                break;
            case "COMPRAS":
                subCategorias = ConfigHB.string("subcategorias_historico_compras");
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

    public static Respuesta estadosCuentaAsync(ContextoHB contexto) {
        if (!ConfigHB.esProduccion() && !ConfigHB.esOpenShift()) {
            return estadosCuenta(contexto);
        }

        Respuesta respuesta = new Respuesta();
        try {
            ExecutorService pool = Executors.newCachedThreadPool();
            Future<Respuesta> future = pool.submit(() -> estadosCuenta(contexto));
            respuesta = future.get(ConfigHB.integer("apiestadoscuenta", 3), TimeUnit.SECONDS);
        } catch (Exception e) {
        }
        return respuesta;
    }

    public static Respuesta estadosCuenta(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        ApiResponse responseTarjeta = new ApiResponse();
        if(!"".equals(tarjetaCredito.numero())) {
            responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        }

        if (responseTarjeta.hayError()) {
            return Respuesta.error();
        }

        String vencimiento;
        String cierre;
        Objeto datos = new Objeto();
        ApiResponse responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(), tarjetaCredito.numero());

        if (responseResumen.hayError()) {
            return Respuesta.error();
        }

        if (responseResumen.codigo == 204) {
            return Respuesta.estado("SIN_RESUMEN");
        }

        // TODO: validar si voy a parametria o sigo con datos de visa
        try {
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
                Date fechaCierreProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaVencimientoProximo = Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
                Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                if ((fechaActual.compareTo(fechaCierreProximo) > 0 || fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
                    Objeto fechas = HBTarjetas.fechaCierreParametrica(Fecha.stringToDate(responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy"), tarjetaCredito.grupoCarteraTc());
                    if (fechas != null && !fechas.string("cierre").isEmpty()) {
                        responseResumen.set("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", fechas.string("cierre"));
                        responseResumen.set("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", fechas.string("vencimiento"));
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", "***");
        cierre = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", "***");
        if (!vencimiento.isEmpty() && !vencimiento.contains("***")) {
            try {
                Objeto fechaTC = HBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, responseResumen);
                vencimiento = fechaTC.string("vencimiento");
                cierre = fechaTC.string("cierre");
            } catch (Exception e) { // evitamos excepcion y pones fecha por defecto del servicio tarjeta credito
                cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
                vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
            }
        }

        if (!cierre.isEmpty() && !cierre.equals("***")) {
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
//				if (!ConfigHB.esProduccion()) {
//					estadoTarjetaPostVto(vtoUltima, datos, contexto);
//				}

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoEstadosDeuda")) {
                    Date fechaVencimiento = Fecha.stringToDate(vtoUltima, "dd/MM/yyyy");
                    Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
                    Respuesta respuestaMora = HBTarjetas.estadoDeuda(contexto);
                    if (respuestaMora.string("datos.estadoDeuda").equals("CON_DEUDA")) {
                        if (respuestaMora.string("datos.tipoMora").equals("ONE")) {
                            onboardingMostrado = false;
                            enMora = true;
                            String formaPago = tarjetaCredito.formaPago().equalsIgnoreCase("Efectivo") ? "EFECTIVO" : "AUTODEBITO";
                            String tipoMora = respuestaMora.string("datos.tipoMora").equals("ONE") || respuestaMora.string("datos.tipoMora").equals("MT") ? "T" : "";
                            if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
                                datos.set("estadoTarjeta", ("EN_MORA" + tipoMora + "_" + formaPago));
                            }
                            ApiResponse detalleMora = RestMora.getProductosEnMoraDetalles(contexto, respuestaMora.string("datos.cta_id"));
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
                        Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
                        datos.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

                        try {
                            if (!muestreoOnboarding && onboardingMostrado && enMora) {
                                contexto.parametros.set("nemonico", "ONBOARDING");
                                Util.contador(contexto);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
                // Error mora service
            }
        }

        if (!vencimiento.isEmpty() && !vencimiento.equals("***")) {
            estadoVencimiento(vencimiento, datos);
            try {
                if (Objeto.setOf("TRES", "DOS", "UNO", "HOY").contains(datos.get("estadoVencimiento"))) {
                    datos.set("estadoTarjeta", "RESUMEN_LISTO");
                }
            } catch (Exception e) {
                // TODO: handle exception
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
            //
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

    // TODO AUT-183 mock para estados de pagos de TC, falta definicion
//	private static void estadoTarjetaPostVto(String vencimiento, Objeto datos, ContextoHB contexto) throws ParseException {
//		Date fechaVto = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
//		Date fechaVtoPasado4dias = Fecha.sumarDias(fechaVto, 4L);
//		Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
//
//		if (fechaActual.before(fechaVtoPasado4dias) && fechaActual.after(fechaVto)) {
//			datos.set("estadoTarjeta", "VERIFICANDO_PAGO");
//		}
//
//		Boolean isPagoParcial = false || contexto.idCobis().equals("3763892");
//		Boolean isPagoTotal = false || contexto.idCobis().equals("5182519");
//		Date fechaPasado7Dias = Fecha.sumarDias(fechaVto, 7L);
//
//		if (isPagoParcial && !esPasadoDDiasFecha(fechaPasado7Dias, fechaActual)) {
//			datos.set("estadoTarjeta", "PAGO_PARCIAL");
//		}
//
//		if (isPagoTotal && !esPasadoDDiasFecha(fechaPasado7Dias, fechaActual)) {
//			datos.set("estadoTarjeta", "AL_DIA");
//		}
//	}

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

//	private static boolean esPasadoDDiasFecha(Date fechaFutura, Date fechaActual) {
//		return fechaActual.compareTo(fechaFutura) == 0 || fechaActual.compareTo(fechaFutura) > 0;
//	}

    private static Boolean verificarBuhoOne(ContextoHB contexto) {
        try {
            Respuesta respuesta = HBProducto.productos(contexto);
            String codigoPaquete = respuesta.string("");
            return "37".equals(codigoPaquete) || "38".equals(codigoPaquete) || "42".equals(codigoPaquete) || "43".equals(codigoPaquete) || "53".equals(codigoPaquete);
        } catch (Exception e) {
            return false;
        }
    }

    public static Respuesta getCuentaTD(ContextoHB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        try {
            Respuesta respuesta = new Respuesta();
            String idCuenta = "";
            List<Cuenta> cuentas = contexto.cuentas();
            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);
            for (Cuenta cuenta : cuentas) {
                if (!idCuenta.isEmpty()) {
                    continue;
                }
                ApiResponse response = CuentasService.getTarjetaDebito(contexto, cuenta);
                for (Objeto tarjeta : response.objetos()) {
                    if (idTarjetaDebito.equals(tarjeta.string("numeroTarjeta")) || td.numero().equals(tarjeta.string("numeroTarjeta"))) {
                        idCuenta = cuenta.id();
                        continue;
                    }
                }
            }
            if (idCuenta.isEmpty()) {
                return Respuesta.estado("CUENTA_NO_ASOCIADA");
            }
            respuesta.set("idCuenta", idCuenta);
            return respuesta;
        } catch (Exception e) {
            return Respuesta.estado("CUENTA_NO_ASOCIADA");
        }
    }

    public static Respuesta altaCuentaEspecial(ContextoHB contexto) {
        try {
            HBProducto.AltaCuentaEspecial(contexto);
        } catch (Exception e) {
            return Respuesta.error();
        }
        return Respuesta.exito();
    }

    public static byte[] tycCuentaEspecial(ContextoHB contexto) {
        contexto.parametros.set("idMoneda", "80");
        String nemonico = "CASOLIC";

        Respuesta respuetaIdSolicitud = HBOmnicanalidad.crearSolicitudCajaAhorro(contexto);
        String idSolicitud = respuetaIdSolicitud.string("idSolicitud");

        ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
        request.query("solicitudid", idSolicitud);
        request.query("grupocodigo", nemonico);
        request.query("canal", "HB");

        if (nemonico.equalsIgnoreCase("PPADELANTO")) {
            request.header("x-cuil", contexto.persona().cuit());
            try {
                request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
            } catch (Exception e) {
                request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
            }
            request.header("x-dni", contexto.persona().numeroDocumento());
            request.header("x-producto", "AdelantoBH");
        }

        ApiResponse response = Api.response(request, idSolicitud, nemonico, contexto.idCobis());
        if (response.hayError()) {
            throw new RuntimeException();
        }

        String base64 = response.string("Data");
        byte[] archivo = Base64.getDecoder().decode(base64);
        try {
            archivo = Base64.getDecoder().decode(new String(archivo));
        } catch (Exception e) {
        }
        contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");
        try {
            new Futuro<>(() -> RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud));
        } catch (Exception e) {
        }
        return archivo;
    }

    public static Respuesta movimientosCuentaV2(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd");
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd");
        Boolean soloCompra = contexto.parametros.bool("soloCompra", false);
        Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);

        if (Objeto.anyEmpty(idCuenta, fechaDesde, fechaHasta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        List<Objeto> movimientos = new ArrayList<>();

        String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos"
                : "/v1/cuentascorrientes/{idcuenta}/movimientos";

        ApiRequest request = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
        request.path("idcuenta", cuenta.numero());
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("pendientes", "2");
        request.query("validactaempleado", "false");
        request.query("numeropagina", numeroPagina.toString());
        request.query("cantidad", "15");
        request.query("tipomovimiento", "T");
        request.query("orden", "D");

        ApiResponse response = Api.response(request, cuenta.id());
        if (response.hayError()) {
            if (response.string("mensajeAlUsuario").contains("fecha comprendida entre la fecha actual y 12 meses anteriores")) {
                return Respuesta.estado("FECHA_DESDE_ANTERIOR_12_MESES");
            }
            if (response.string("codigo").equals("100")) {
                return Respuesta.estado("SIN_MOVIMIENTOS");
            }
            return Respuesta.error();
        }

        if (response.codigo == 204) {
            return Respuesta.estado("SIN_MOVIMIENTOS");
        }

        Integer totalPaginas = 0;
        for (Objeto item : response.objetos()) {
            if (item.existe("cantPaginas")) {
                totalPaginas = item.integer("cantPaginas");
            } else {
                if (soloCompra && !item.string("causa").equals("4202")) {
                    continue;
                }

                Objeto movimiento = new Objeto();
                movimiento.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                String descripcion = validarSignoParametria(item.string("descripcionMovimiento").toUpperCase());
                movimiento.set("descripcion", descripcion);
                movimiento.set("importe", item.bigDecimal("valor"));
                movimiento.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
                movimiento.set("saldo", item.bigDecimal("saldo"));
                movimiento.set("saldoFormateado", Formateador.importe(item.bigDecimal("saldo")));
                movimiento.set("simboloMoneda", cuenta.simboloMoneda());
                movimiento.set("nroOperacion", item.string("secuencial"));
                movimiento.set("hora", item.string("hora"));
                movimiento.set("fechaMovimiento", item.date("hora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                movimiento.set("categoria", item.string("categoria"));
                movimiento.set("subCategoria", item.string("subCategoria"));
                movimiento.set("tipoMsg", item.string("tipoMsg"));
                movimiento.set("cuitCP", item.string("cuitCP"));
                movimiento.set("idEstablecimiento", item.string("codComercio"));
                movimiento.set("rubro", item.string("codRubro"));
                movimiento.set("referencia", item.string("referencia"));
                checkDataComprobante(item, movimiento);
                movimientos.add(movimiento);
            }
        }

        Respuesta respuesta = new Respuesta();
        if (soloCompra) {
            respuesta.set("mercadoPagoHabilitado", verificarBuhoOne(contexto));
            respuesta.set("fechaHastaReclamo", Util.calcularFechaNdiasHabilesPasado(contexto, Fecha.fechaActual(), -10));
        }
        respuesta.set("movimientos", movimientos);
        respuesta.set("totalPaginas", totalPaginas);
        return respuesta;
    }

    public static Object ofertaCajaAhorroDolar(ContextoHB ctx) {
        Respuesta respuesta = new Respuesta();
        boolean mostrarModal = false;

        try {
            // Verifica si el usuario tiene cuentas en dólares
            if (!ctx.cuentasDolares().isEmpty()) {
                return respuesta.set("mostrarModal", false);
            }

            // Verifica si la funcionalidad está habilitada por configuración
            if (!ConfigHB.bool("prendido_oferta_caja_ahorro_dolar")) {
                return respuesta.set("mostrarModal", false);
            }

            // Llamadas asincrónicas
            Futuro<Boolean> futuroPermitido = new Futuro<>(() -> SqlHomebanking.getCobisOfertaCADolar(ctx.idCobis()));
            Futuro<SqlResponse> futuroContador = new Futuro<>(() -> Util.getContador(ctx, "ALERTA_CA_DOLARES"));

            boolean permitido = futuroPermitido.get();
            SqlResponse contadorResponse = futuroContador.get();

            if (!permitido || contadorResponse.hayError) {
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
                ctx.parametros.set("nemonico", "ALERTA_CA_DOLARES");
                new Futuro<>(() -> Util.contador(ctx));
            }

        } catch (Exception ignored) {
        }

        return respuesta.set("mostrarModal", mostrarModal);
    }

    public static Respuesta certificadoCuenta(ContextoHB ctx) {
        if(!HBAplicacion.funcionalidadPrendida(ctx.idCobis(), "prendido_certificado_cuenta")){
            return Respuesta.estado("FUNCIONALIDAD_NO_HABILITADA");
        }

        String idCuenta = ctx.parametros.string("idCuenta");
        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = ctx.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        Objeto contadorCertificado = contadorCertificadoCA(ctx);
        if(contadorCertificado != null){
            String tipo = contadorCertificado.string("tipo");
            String email = tipo.contains("|") ? tipo.split("\\|")[1] : null;
            return Respuesta.estado("CERTIFICADO_YA_SOLICITADO").set("email", email);
        }

        ApiResponse respuestaCaso = RestPostventa.certificadoCuenta(ctx, cuenta);
        if(!ctx.esDesarrollo()){
            if (respuestaCaso != null && respuestaCaso.hayError()) {
                return Respuesta.error();
            }
        }

        ctx.insertarContador("CERTIFICADO_CUENTA|" + ctx.persona().email());
        return Respuesta.exito();
    }

    private static Objeto contadorCertificadoCA(ContextoHB ctx){
        try{
            String fechaDesde = Fecha.restarDias(new Date(), 30L, "yyyy-MM-dd");
            SqlRequest sqlRequest = Sql.request("ContadorCertificadoCA", "homebanking");
            sqlRequest.sql = "SELECT TOP 1 * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo LIKE 'CERTIFICADO_CUENTA_%' AND momento > ? ORDER BY momento DESC";
            sqlRequest.add(ctx.idCobis());
            sqlRequest.add(fechaDesde);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            if(sqlResponse.hayError || sqlResponse.registros.size() == 0){
                return null;
            }

            return sqlResponse.registros.get(0);
        }
        catch(Exception e){}
        return null;
    }

}
