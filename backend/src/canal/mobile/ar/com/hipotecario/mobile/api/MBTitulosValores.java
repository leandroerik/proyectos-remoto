package ar.com.hipotecario.mobile.api;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.RescateMBBMBankProcess;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.lib.ExtractoComitenteDatosPDF;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Pdf;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaComitente;
import ar.com.hipotecario.mobile.servicio.RestInversiones;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.TransmitMB;

import javax.naming.Context;

public class MBTitulosValores {
    private static final String C_FORMATO_FECHA_1_DMMYYYY = "d/M/yyyy";
    private static final String C_FORMATO_FECHA_2_DDMMYYYY = "dd/MM/yyyy";
    private static final String C_FORMATO_FECHA_3_DDMMYY = "dd/MM/yy";
    private static final String C_FORMATO_FECHA_1_YYYYMMDD = "yyyy-MM-dd";


    public static RespuestaMB tenencia(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        Date fechaParametro = contexto.parametros.date("fecha", "d/M/yyyy");
        // String plazo = contexto.parametros.string("plazo");

        // emm: en caso que desde el front le pasen una fecha menor a la de hoy, va por
        // el historico
        if (fechaParametro != null) {
            Date hoy = new Date();
            if (Fecha.cantidadDias(fechaParametro, hoy) != 0) {
                contexto.parametros.set("fechaDesde", fechaParametro);
                contexto.parametros.set("fechaHasta", fechaParametro);
                return tenenciaHistorico(contexto);
            }
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        ApiResponseMB tenenciaTitulosValores = RestInversiones.tenenciaTitulosValores(contexto, cuentaComitente.numero());
        if (tenenciaTitulosValores.hayError()) {
            if (tenenciaTitulosValores.string("codigo").equals("500") && tenenciaTitulosValores.codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
            return RespuestaMB.error();
        }

        ApiResponseMB responseIndicesRealTime = null;

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto tenencia : tenenciaTitulosValores.objetos()) {
            String codigo = tenencia.string("codigoEspecie").split("-")[0].trim();
            if (codigo.isEmpty()) {
                continue;
            }

            Objeto tituloValor = RestInversiones.tituloValor(contexto, codigo);

            BigDecimal cantidadNominal = tenencia.bigDecimal("saldoDisponibleNominal");

            BigDecimal valor = tenencia.bigDecimal("cotizacionSistemaNoticias");
            BigDecimal saldoValuado = tenencia.bigDecimal("valorizacion");
            BigDecimal variacion = null;
            String fecha = tenencia.date("fechaCotizacion", "yyyy-MM-dd", "dd/MM/yy");
            String tipoCotizacion = tenencia.string("tipoCotizacion");
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_licitaciones_byma")) {
                responseIndicesRealTime = RestInversiones.indicesRealTime(contexto, codigo, "", "3");
                if (!responseIndicesRealTime.hayError()) {
                    for (Objeto item : responseIndicesRealTime.objetos()) {
                        if (item.bigDecimal("trade") != null && item.bigDecimal("trade").compareTo(new BigDecimal(0)) != 0) {
                            valor = item.bigDecimal("trade");
                            if ("Titulo Publico".equals(tituloValor.string("clasificacion")) || "CHA".equals(tituloValor.string("clasificacion"))) {
                                valor = valor.divide(new BigDecimal(100));
                            }
                            variacion = item.bigDecimal("imbalance");
                            saldoValuado = valor.multiply(tenencia.bigDecimal("saldoDisponibleNominal"));
                            tipoCotizacion = "BYMA";
                            fecha = item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
                        }
                    }
                }
            }

            Objeto item = new Objeto();
            item.set("id", codigo);
            item.set("descripcion", tenencia.string("codigoEspecie"));
            item.set("monedaEspecie", tenencia.string("monedaEspecie"));
            item.set("tipoActivo", tituloValor.string("clasificacion"));
            item.set("tipo", tenencia.string("tipoProducto"));
            item.set("fecha", fecha);
            item.set("cantidadNominal", cantidadNominal);
            item.set("cantidadNominalFormateada", Formateador.importe(cantidadNominal).replace(",00", ""));
            item.set("valorPesos", valor);
            item.set("valorPesosFormateado", valor != null && !valor.equals(new BigDecimal("0")) ? Formateador.importeCantDecimales(valor, 4) : "-");
            item.set("saldoValuadoPesos", saldoValuado);
            item.set("saldoValuadoPesosFormateado", valor != null && !valor.equals(new BigDecimal("0")) ? Formateador.importe(saldoValuado) : "-");
            item.set("tipoCotizacion", tipoCotizacion);
            item.set("variacion", variacion);
            item.set("variacion", variacion != null && !variacion.equals(new BigDecimal("0")) ? Formateador.importe(variacion) : "-");
            respuesta.add("tenencia", item);
        }
        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return respuesta;
    }

    public static RespuestaMB tenenciaPosicionNegociable(ContextoMB contexto) {

        if (MBAplicacion.funcionalidadPrendida("tenencia_posicion_negociable_V2")){
            return tenenciaPosicionNegociableV2(contexto);
        }

        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String fecha = contexto.parametros.string("fecha");
        BigDecimal totalFondosPesos = new BigDecimal(BigInteger.ZERO);
        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        Integer cantidadTitulosValoresPesos = 0;

        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Set<String> plazosHabilitados = Objeto.setOf(ConfigMB.string("plazos_liquidacion_habilitados")
                .split("_"));

        String idVencimientoValorizacion = "2";
        if (plazosHabilitados.contains("48")) {
            idVencimientoValorizacion = "3";
        }

        Long inicio = System.currentTimeMillis();

        Futuro<Objeto> futuroPosicionesNegociables = new Futuro<>(() -> buscarPosicionesNegociablesv2(contexto, cuentaComitente.numero(), fecha));
        Futuro<Map<String, Objeto>> productosOperablesFuturo = new Futuro<>(() -> RestInversiones.obtenerProductosOperablesMapByCodigo(contexto, fecha));

        Objeto posicionesNegociables = futuroPosicionesNegociables.get();
        Map<String, Objeto> productosOperables = productosOperablesFuturo.get();

        contexto.setHeader("B1", String.valueOf(System.currentTimeMillis() - inicio));

        Futuro<Map<String, Objeto>> futuroProductosOperablesbyProducto = new Futuro<>(() -> null);
        Futuro<Map<String, Objeto>> futuroProductosOperablesbyProductoPesos = new Futuro<>(() -> null);
        Map<String, Futuro<Objeto>> futurosCotizaciones = new HashMap<>();
        for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String codigo = posicionNegociable.string("codigo");
            Objeto productoOperable = productosOperables.get(codigo);
            if (productoOperable == null) {
                futuroProductosOperablesbyProducto = new Futuro<>(() -> RestInversiones.obtenerProductosOperablesMapByProductoFecha(contexto, fecha));
            }
        }

        contexto.setHeader("B2", String.valueOf(System.currentTimeMillis() - inicio));

        for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String codigo = posicionNegociable.string("codigo");
            Objeto productoOperable = productosOperables.get(codigo);
            if (productoOperable == null) {
                productoOperable = futuroProductosOperablesbyProducto.get().get(codigo);
            }
            if (productoOperable == null) {
                continue;
            }
            String descMoneda = productoOperable.string("descMoneda");
            if (!descMoneda.equalsIgnoreCase("PESOS")) {
                futuroProductosOperablesbyProductoPesos = new Futuro<>(() -> RestInversiones.obtenerProductosOperablesMapByProductoPesos(contexto, fecha));
            }
        }

        contexto.setHeader("B3", String.valueOf(System.currentTimeMillis() - inicio));

        for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String codigo = posicionNegociable.string("codigo");
            Objeto productoOperable = productosOperables.get(codigo);
            if (productoOperable == null){
                productoOperable = futuroProductosOperablesbyProducto.get().get(codigo);
            }
            if (productoOperable == null) {
                continue;
            }
            String descMoneda = productoOperable.string("descMoneda");
            if (!descMoneda.equalsIgnoreCase("PESOS")) {
                productoOperable = futuroProductosOperablesbyProductoPesos.get().get(codigo);
            }
            if (productoOperable != null) {
                //cotization = buscarPrecioCotizacion(contexto, productoOperable, idVencimientoValorizacion);
                String idVencimientoValorizacionFinal = idVencimientoValorizacion;
                Objeto productoOperableFinal = Objeto.fromJson(productoOperable.toJson());
                futurosCotizaciones.put(codigo, new Futuro<>(() -> buscarPrecioCotizacion(contexto, productoOperableFinal, idVencimientoValorizacionFinal)));
            }
        }

        contexto.setHeader("B4", String.valueOf(System.currentTimeMillis() - inicio));

        // TODO: TIMEOUT
        if (ConfigMB.esHomologacion() && contexto.idCobis().equals("395778")) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (posicionesNegociables instanceof RespuestaMB) {
            return (RespuestaMB) posicionesNegociables;
        }

        RespuestaMB respuesta = new RespuestaMB();

        // TODO: TIMEOUT
        if (ConfigMB.esHomologacion() && contexto.idCobis().equals("395778")) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Map<String, Objeto> productosOperablesbyProducto = null;
        Map<String, Objeto> productosOperablesbyProductoPesos = null;
        for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            Objeto erroresTenencia = new Objeto();
            Objeto itemRespuesta = new Objeto();
            String codigo = posicionNegociable.string("codigo");
            BigDecimal saldoNominal = posicionNegociable.bigDecimal("saldoDisponible");
            Objeto productoOperable = productosOperables.get(codigo);
            itemRespuesta.set("id", codigo);
            itemRespuesta.set("producto", codigo);

            if (productoOperable == null) {
                if (productosOperablesbyProducto == null) {
                    //productosOperablesbyProducto = RestInversiones.obtenerProductosOperablesMapByProductoFecha(contexto, fecha);
                    productosOperablesbyProducto = futuroProductosOperablesbyProducto.get();
                }

                productoOperable = productosOperablesbyProducto.get(codigo);
                if (productoOperable == null) {
                    erroresTenencia.add("ERR_PROD_DATA");
                    itemRespuesta.set("errores", erroresTenencia);
                    itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
                    itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
                    itemRespuesta.set("cantidadNominal", saldoNominal);
                    itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
                    itemRespuesta.set("fecha", "");
                    itemRespuesta.set("valorPesos", 0);
                    itemRespuesta.set("valorPesosFormateado", "0");
                    itemRespuesta.set("saldoValuadoPesos", 0);
                    itemRespuesta.set("saldoValuadoPesosFormateado", "0");
                    itemRespuesta.set("variacion", Formateador.importe(new BigDecimal(0)));
                    itemRespuesta.set("tipoCotizacion", "SC");
                    itemRespuesta.set("monedaDescripcion", "");
                    itemRespuesta.set("monedaSimbolo", "");
                    itemRespuesta.set("color", "#9b9b9b");
                    respuesta.add("tenencia", itemRespuesta);
                    cantidadTitulosValoresPesos++;
                    continue;
                } else {
                    // FIXME: INV-564 - API Inversiones - Retorna en "codigo" lo que parece ser "producto"
                    // en recurso /v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}
                    // los cuales difieren en algunos CEDEARS ("CDR AMZN" / "AMZN"). Hacemos la corrección acá.
                    itemRespuesta.set("id", codigo.equals("CDR AMZN") ? "AMZN" : productoOperable.get("codigo"));
                    itemRespuesta.set("producto", productoOperable.get("producto"));
                }
            }

            String descMoneda = productoOperable.string("descMoneda");
            if (!descMoneda.equalsIgnoreCase("PESOS")) {
                if (productosOperablesbyProductoPesos == null) {
                    //productosOperablesbyProductoPesos = RestInversiones.obtenerProductosOperablesMapByProductoPesos(contexto, fecha);
                    productosOperablesbyProductoPesos = futuroProductosOperablesbyProductoPesos.get();
                }
                productoOperable = productosOperablesbyProductoPesos.get(codigo);
            }

            Objeto cotization = null;
            if (productoOperable != null) {
                try {
                    //cotization = buscarPrecioCotizacion(contexto, productoOperable, idVencimientoValorizacion);
                    cotization = futurosCotizaciones.get(codigo).get();
                    if (cotization == null
                            || cotization.bigDecimal("precio").compareTo(BigDecimal.ZERO)==0) {
                        erroresTenencia.add("ERR_COT");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    erroresTenencia.add("ERR_COT");
                }
            } else {
                // BUG INV-628: faltan datos del producto (i.e. falta AL30 -en pesos- para AL30D).
                erroresTenencia.add("ERR_PROD_DATA");
            }

            // Si hubo errores, inicializamos y continuamos con el calculo.
            if (erroresTenencia.toList().size() > 0) {
                // respuesta.setEstadoExistenErrores();
                itemRespuesta.set("errores", erroresTenencia);
                cotization = (new Objeto()).set("precio", "0")
                        .set("variacion", "0")
                        .set("tipoCotizacion", "")
                        .set("fecha", "");
                productoOperable = new Objeto().set("descMoneda", "PESOS");
            }

            contexto.setHeader("B5", String.valueOf(System.currentTimeMillis() - inicio));

            BigDecimal precio = cotization.bigDecimal("precio");
            String monedaDescripcion = productoOperable.string("descMoneda");
            String monedaSimbolo = monedaDescripcion.equals("PESOS") ? "$" : monedaDescripcion;
            BigDecimal saldoValuado = precio.multiply(saldoNominal);

            itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
            itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
            itemRespuesta.set("cantidadNominal", saldoNominal);
            itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
            itemRespuesta.set("fecha", cotization.string("fecha"));
            itemRespuesta.set("valorPesos", precio);
            itemRespuesta.set("valorPesosFormateado", Formateador.importeCantDecimales(precio, 4));
            itemRespuesta.set("saldoValuadoPesos", saldoValuado);
            itemRespuesta.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado));
            itemRespuesta.set("variacion", Formateador.importe(cotization.bigDecimal("variacion")));
            itemRespuesta.set("tipoCotizacion", precio.signum() != 0 ? cotization.string("tipoCotizacion") : "SC");
            itemRespuesta.set("monedaDescripcion", monedaDescripcion);
            itemRespuesta.set("monedaSimbolo", monedaSimbolo);
            int nextInt = Util.secureRandom.nextInt(0xffffff + 1);
            itemRespuesta.set("color", String.format("#%06x", nextInt).toUpperCase());
            respuesta.add("tenencia", itemRespuesta);
            cantidadTitulosValoresPesos++;
            totalFondosPesos = totalFondosPesos.add(saldoValuado);
        }
        for (Objeto item : respuesta.objetos("tenencia")) {

            if (item.string("monedaDescripcion").isEmpty()) {
                item.set("porcentaje", 0.0);
            }

            item.set("porcentaje", Util.porcentaje(item.bigDecimal("saldoValuadoPesos"), totalFondosPesos));
        }
        respuesta.set("totalFondosPesos", totalFondosPesos);
        respuesta.set("totalFondosPesosFormateado", Formateador.importe(totalFondosPesos));
        respuesta.set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);

        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }

        contexto.setHeader("B6", String.valueOf(System.currentTimeMillis() - inicio));

        return respuesta;
    }

    public static RespuestaMB tenenciaPosicionNegociableV2(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String fecha             = contexto.parametros.string("fecha");
        CuentaComitente cuenta   = contexto.cuentaComitente(idCuentaComitente);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Futuro<Objeto> futuroPos = new Futuro<>(
                () -> buscarPosicionesNegociablesV3(contexto, cuenta.numero(), fecha)
        );
        Objeto posicionesNegociables = futuroPos.get();
        if (posicionesNegociables instanceof RespuestaMB) {
            return (RespuestaMB) posicionesNegociables;
        }

        RespuestaMB respuesta           = new RespuestaMB();
        BigDecimal totalFondosPesos     = BigDecimal.ZERO;
        int       cantidadTitulosValoresPesos = 0;

        if (ConfigMB.esHomologacion() && "395778".equals(contexto.idCobis())) {
            try { Thread.sleep(20_000); } catch (InterruptedException ignored) {}
        }

        for (Objeto pos : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String       codigo       = pos.string("codigo");
            BigDecimal   saldoNominal = pos.bigDecimal("saldoDisponible");
            String       descTenencia = pos.string("descripcionTenencia");
            String       clasif       = pos.string("clasificacion");
            String       fechaV2      = pos.string("fechaPrecioConsulta");
            BigDecimal   precioV2     = new BigDecimal(pos.string("precioConsulta"));
            BigDecimal   variacionBD  = BigDecimal.ZERO;
            try {
                variacionBD = new BigDecimal(pos.string("variacion"));
            } catch (Exception ignored) {}

            // código BYMA, si existe
            String codigoByma = pos.string("codigoByma");
            String idFinal    = (codigoByma != null && !codigoByma.trim().isEmpty())
                    ? codigoByma
                    : codigo;

            BigDecimal saldoValuado = precioV2.multiply(saldoNominal);

            Objeto item = new Objeto()
                    .set("id", idFinal)
                    .set("producto", idFinal)
                    .set("descripcion", codigo + " - " + descTenencia)
                    .set("tipoActivo", clasif)
                    .set("cantidadNominal", saldoNominal)
                    .set("cantidadNominalFormateada",
                            Formateador.importe(saldoNominal).replace(",00",""))
                    .set("fecha", fechaV2)
                    .set("valorPesos", precioV2)
                    .set("valorPesosFormateado",
                            Formateador.importeCantDecimales(precioV2, 4))
                    .set("saldoValuadoPesos", saldoValuado)
                    .set("saldoValuadoPesosFormateado",
                            Formateador.importe(saldoValuado))
                    .set("variacion", Formateador.importe(variacionBD))
                    .set("tipoCotizacion", "BYMA")
                    .set("monedaDescripcion", "PESOS")
                    .set("monedaSimbolo", "$")
                    .set("color",
                            String.format("#%06x",
                                            Util.secureRandom.nextInt(0xffffff+1))
                                    .toUpperCase());

            respuesta.add("tenencia", item);
            totalFondosPesos = totalFondosPesos.add(saldoValuado);
            cantidadTitulosValoresPesos++;
        }

        for (Objeto it : respuesta.objetos("tenencia")) {
            it.set("porcentaje",
                    Util.porcentaje(it.bigDecimal("saldoValuadoPesos"), totalFondosPesos));
        }

        respuesta
                .set("totalFondosPesos", totalFondosPesos)
                .set("totalFondosPesosFormateado", Formateador.importe(totalFondosPesos))
                .set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);

        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return respuesta;
    }



    private static Objeto buscarPosicionesNegociables(ContextoMB contexto, String cuentaComitente) {
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Futuro<ApiResponseMB> posicionesNegociables = new Futuro<>(() -> RestInversiones.obtenerPosicionesNegociables(contexto, cuentaComitente, "1000", fechaActual, 1));
        if (!posicionesNegociables.get().hayError()) {
            if (posicionesNegociables.get().codigo == 204 || posicionesNegociables.get().codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
        } else {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return posicionesNegociables.get();
    }

    private static Objeto buscarPosicionesNegociablesv2(ContextoMB contexto, String cuentaComitente, String fecha) {
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (!fecha.isEmpty()) {
            fechaActual = fecha;
        }

        ApiResponseMB posicionesNegociables = RestInversiones.obtenerPosicionesNegociables(contexto, cuentaComitente, "1000", fechaActual, 1);

        // TODO: TIMEOUT
        if (ConfigMB.esHomologacion() && contexto.idCobis().equals("395778")) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!posicionesNegociables.hayError()) {
            if (posicionesNegociables.codigo == 204 || posicionesNegociables.codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
        } else {
            if (posicionesNegociables.codigo == 504 || posicionesNegociables.codigo == 500) {
                return RespuestaMB.estado("OPERA_MANUAL");
            }

            return RespuestaMB.estado("SIN_TENENCIA");
        }

        return posicionesNegociables;
    }

    private static Objeto buscarPosicionesNegociablesV3(ContextoMB contexto, String cuentaComitente, String fecha) {
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (!fecha.isEmpty()) {
            fechaActual = fecha;
        }

        ApiResponseMB posicionesNegociables = RestInversiones.obtenerPosicionesNegociablesV2(contexto, cuentaComitente, "1000", fechaActual, 1);

        // TODO: TIMEOUT
        if (ConfigMB.esHomologacion() && contexto.idCobis().equals("395778")) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!posicionesNegociables.hayError()) {
            if (posicionesNegociables.codigo == 204 || posicionesNegociables.codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
        } else {
            if (posicionesNegociables.codigo == 504 || posicionesNegociables.codigo == 500) {
                return RespuestaMB.estado("OPERA_MANUAL");
            }

            return RespuestaMB.estado("SIN_TENENCIA");
        }

        return posicionesNegociables;
    }

    public static RespuestaMB posicionesNegociables(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        Date fechaParametro = contexto.parametros.date("fecha", "d/M/yyyy");

        if (fechaParametro == null) {
            fechaParametro = new Date();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Futuro<ApiResponseMB> tenenciaTitulosValoresFuturo = new Futuro<>(() -> RestInversiones.tenenciaTitulosValores(contexto, cuentaComitente.numero()));
        ApiResponseMB tenenciaTitulosValores = tenenciaTitulosValoresFuturo.get();

        if (tenenciaTitulosValores.hayError()) {
            if (tenenciaTitulosValores.string("codigo").equals("500") && tenenciaTitulosValores.codigo == 404) {
                // return Respuesta.estado("SIN_TENENCIA");
                // En caso de que no tenga tenencia no tendría problema en seguir.
            } else {
                return RespuestaMB.error();
            }
        }

        String fechaTexto = new SimpleDateFormat("yyyy-MM-dd").format(fechaParametro);
        boolean continuar = true;
        final int[] secuencial = {0};
        RespuestaMB respuestaPosicionesNegociables = new RespuestaMB();
        while (continuar) {
            Futuro<ApiResponseMB> responsePosicionesNegociablesFuturo =  new Futuro<>(() -> RestInversiones.tenenciaPosicionesNegociables(contexto, fechaTexto, secuencial[0] + 1));
            ApiResponseMB responsePosicionesNegociables = responsePosicionesNegociablesFuturo.get();
            if (responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas") == null || responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas").isEmpty()) {
                break;
            }
            for (Objeto item : responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
                Objeto tituloValor = RestInversiones.tituloValor(contexto, item.string("codigo"));
                Futuro<ApiResponseMB> responseIndicesRealTimeFuturo = new Futuro<>(() -> RestInversiones.indicesRealTime(contexto, item.string("codigo"), "", "3"));

                BigDecimal valor = null;
                BigDecimal saldoValuado = null;
                BigDecimal variacion = null;
                String fecha = "";
                BigDecimal cantidadNominal = item.bigDecimal("saldoNominal");
//				String tipo = "";
//				String tipoCotizacion = "";

                ApiResponseMB responseIndicesRealTime = responseIndicesRealTimeFuturo.get();
                if (!responseIndicesRealTime.hayError()) {
                    for (Objeto itemRealTime : responseIndicesRealTime.objetos()) {
                        valor = itemRealTime.bigDecimal("trade");
                        variacion = itemRealTime.bigDecimal("imbalance");

                        if (valor != null) {
                            saldoValuado = valor.multiply(item.bigDecimal("saldoNominal"));
//							tipoCotizacion = "BYMA";
                            fecha = item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
                        }
                    }
                }

                if (saldoValuado == null) { // en este caso tengo que buscar el saldo valuado del día anterior. Me veo
                    // forzado a buscarlo en el servicio que usabamos antes
                    for (Objeto tenencia : tenenciaTitulosValores.objetos()) {
                        if (tenencia.string("codigoEspecie").split("-")[0].trim().equals("")) {
                            valor = tenencia.bigDecimal("cotizacionSistemaNoticias");
                            saldoValuado = valor.multiply(item.bigDecimal("saldoNominal")); // tenencia.bigDecimal("valorizacion");
                            variacion = null;
                            fecha = tenencia.date("fechaCotizacion", "yyyy-MM-dd", "dd/MM/yy");
//							tipo = tenencia.string("tipoProducto");
//							tipoCotizacion = tenencia.string("tipoCotizacion");
                        }
                    }
                }
                Objeto objeto = new Objeto();
                objeto.set("id", item.string("codigo"));
                objeto.set("descripcion", item.string("codigo") + " - " + item.string("descripcionTenencia"));
                objeto.set("tipoActivo", tituloValor.string("clasificacion"));
                objeto.set("tipo", ""); // TODO: tengo que pedir este campo en el servicio de posiciones negociables
                objeto.set("fecha", fecha);
                objeto.set("cantidadNominal", cantidadNominal);
                objeto.set("cantidadNominalFormateada", Formateador.importe(cantidadNominal).replace(",00", ""));
                objeto.set("valorPesos", valor);
                objeto.set("valorPesosFormateado", Formateador.importe(valor).replace(",00", ""));
                objeto.set("saldoValuadoPesos", saldoValuado);
                objeto.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado).replace(",00", ""));
                objeto.set("tipoCotizacion", ""); // TODO: tengo que pedir este campo en el servicio de posiciones negociables
                objeto.set("variacion", variacion);
                objeto.set("variacion", variacion != null && !variacion.equals(new BigDecimal("0")) ? Formateador.importe(variacion) : "-");

                objeto.set("ordenSecuencial", item.integer("ordenSecuencial"));
                objeto.set("cuentaCustodia", item.string("cuentaCustodia"));
                objeto.set("numero", item.string("numero"));

                respuestaPosicionesNegociables.add("posiciones", objeto);
                secuencial[0] = item.integer("ordenSecuencial");
            }
        }
        return respuestaPosicionesNegociables;
    }

    public static RespuestaMB tenenciaHistorico(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd");
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd");
        String plazo = contexto.parametros.string("plazo");
        if ("0".equals(plazo)) {
            plazo = "1";
        }
        if ("24".equals(plazo)) {
            plazo = "2";
        }
        if ("48".equals(plazo)) {
            plazo = "3";
        }

        if ("".equals(plazo)) {
            plazo = "3";
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        ApiResponseMB tenenciaTitulosValores = RestInversiones.tenenciaTitulosValores(contexto, cuentaComitente.numero());
        if (tenenciaTitulosValores.hayError()) {
            if (tenenciaTitulosValores.string("codigo").equals("500") && tenenciaTitulosValores.codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto tenencia : tenenciaTitulosValores.objetos()) {
            String codigo = tenencia.string("codigoEspecie").split("-")[0].trim();
            if (codigo.isEmpty()) {
                continue;
            }

            ApiResponseMB historico = RestInversiones.tenenciaHistorico(contexto, fechaDesde, fechaHasta, codigo, plazo);
            if (historico.hayError()) {
                return RespuestaMB.error();
            }
            Objeto tituloValor = RestInversiones.tituloValor(contexto, codigo);
            Objeto item = new Objeto();
            Boolean encontroHistorico = false;
            for (Objeto itemHistorico : historico.objetos()) {

                BigDecimal cantidadNominal = tenencia.bigDecimal("saldoDisponibleNominal");

                BigDecimal valor = itemHistorico.bigDecimal("ultimo");
                BigDecimal saldoValuado = null;
                if (valor != null) {
                    saldoValuado = valor.multiply(tenencia.bigDecimal("saldoDisponibleNominal"));
                } else {
                    saldoValuado = null;
                }

                item.set("id", codigo);
                item.set("descripcion", tenencia.string("codigoEspecie"));
                item.set("tipoActivo", tituloValor.string("clasificacion"));
                item.set("tipo", tenencia.string("tipoProducto"));
                item.set("fecha", itemHistorico.date("fecha", "yyyy-MM-dd", "dd/MM/yy"));
                item.set("cantidadNominal", cantidadNominal);
                item.set("cantidadNominalFormateada", Formateador.importe(cantidadNominal).replace(",00", ""));
                item.set("valorPesos", valor);
                item.set("valorPesosFormateado", valor != null && !valor.equals(new BigDecimal("0")) ? Formateador.importeCantDecimales(valor, 4) : "-");
                item.set("saldoValuadoPesos", saldoValuado);
                item.set("saldoValuadoPesosFormateado", valor != null && !valor.equals(new BigDecimal("0")) ? Formateador.importe(saldoValuado) : "-");
                item.set("tipoCotizacion", tenencia.string("tipoCotizacion"));
                BigDecimal variacion = itemHistorico.bigDecimal("variacion");
                item.set("variacion", variacion);
                item.set("variacion", variacion != null && !variacion.equals(new BigDecimal("0")) ? Formateador.importe(variacion) : "-");
                encontroHistorico = true;
            }

            if (!encontroHistorico) {
                continue;
            }

            respuesta.add("tenencia", item);
        }
        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return respuesta;
    }

    public static RespuestaMB tenenciaVenta(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        ApiResponseMB tenenciaTitulosValores = RestInversiones.tenenciaTitulosValores(contexto, cuentaComitente.numero());
        if (tenenciaTitulosValores.hayError()) {
            if (tenenciaTitulosValores.string("codigo").equals("500") && tenenciaTitulosValores.codigo == 404) {
                return RespuestaMB.estado("SIN_TENENCIA");
            }
            return RespuestaMB.error();
        }

        Boolean poseeCuentaAsociadaPesos = false;
        Boolean poseeCuentaAsociadaDolares = false;
        List<String> response = RestInversiones.cuentasLiquidacionMonetaria(contexto, cuentaComitente.numero());
        for (String numeroCuenta : response) {
            poseeCuentaAsociadaPesos |= numeroCuenta.startsWith("4");
            poseeCuentaAsociadaDolares |= numeroCuenta.startsWith("2");
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto tenencia : tenenciaTitulosValores.objetos()) {
            String codigoOriginal = tenencia.string("codigoEspecie").split("-")[0].trim();
            String codigo = tenencia.string("codigoEspecie").split("-")[0].trim();
            for (String subcodigo : Objeto.listOf("", "D")) {
                if (!poseeCuentaAsociadaPesos && subcodigo.equals("")) {
                    continue;
                }
                if (!poseeCuentaAsociadaDolares && subcodigo.equals("D")) {
                    continue;
                }
                if (codigo.isEmpty()) {
                    continue;
                }
                codigo += subcodigo;

                Objeto tituloValor = RestInversiones.tituloValor(contexto, codigo);
                if (tituloValor.string("descMoneda").isEmpty()) {
                    continue;
                }

                ApiResponseMB precioPesos = RestInversiones.precioTituloValor(contexto, codigo, tituloValor.string("descMoneda", "PESOS"));
                if (precioPesos.hayError()) {
                    precioPesos = new ApiResponseMB();
                    precioPesos.set("precioReferencia", new BigDecimal("0"));
                }
                if (precioPesos.codigo.equals(204)) {
                    continue;
                }

                BigDecimal cantidadNominal = tenencia.bigDecimal("saldoNegociableNominal");
                BigDecimal valor = precioPesos.bigDecimal("precioReferencia");
                BigDecimal saldoValuado = precioPesos.bigDecimal("precioReferencia").multiply(tenencia.bigDecimal("saldoNegociableNominal"));

                String descripcion = codigo + tenencia.string("codigoEspecie").substring(codigoOriginal.length());

                Objeto item = new Objeto();
                item.set("id", codigo + "_" + tituloValor.string("descMoneda"));
                item.set("descripcion", descripcion);
                item.set("codigoEspecie", codigo);
                item.set("tipoActivo", tituloValor.string("clasificacion"));
                item.set("tipo", tenencia.string("tipoProducto"));
                item.set("fecha", precioPesos.date("fechaPrecio", "yyyy-MM-dd", "dd/MM/yy"));
                item.set("cantidadNominal", cantidadNominal);
                item.set("cantidadNominalFormateada", Formateador.importe(cantidadNominal));
                item.set("valorPesos", valor);
                item.set("valorPesosFormateado", Formateador.importe(valor));
                item.set("saldoValuadoPesos", saldoValuado);
                item.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado));
                item.set("moneda", tituloValor.string("descMoneda").equals("PESOS") ? 80 : 2);
                respuesta.add("tenencia", item);
            }
        }
        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return respuesta;
    }

    public static RespuestaMB tenenciaVentaPosicionNegociable(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Boolean poseeCuentaAsociadaPesos = false;
        Boolean poseeCuentaAsociadaDolares = false;
        List<String> response = RestInversiones.cuentasLiquidacionMonetaria(contexto, cuentaComitente.numero());
        for (String numeroCuenta : response) {
            poseeCuentaAsociadaPesos |= numeroCuenta.startsWith("4");
            poseeCuentaAsociadaDolares |= numeroCuenta.startsWith("2");
        }

        Objeto posicionesNegociables = buscarPosicionesNegociables(contexto, cuentaComitente.numero());
        if (posicionesNegociables instanceof RespuestaMB) {
            return (RespuestaMB) posicionesNegociables;
        }
        RespuestaMB respuesta = new RespuestaMB();

        List<Objeto> productosOperables = RestInversiones.obtenerProductosOperables(contexto);

        for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            List<Objeto> posicionesNegociablesPorCuentasAsociadas = filtrarPosicionesNegociablesPorCuentasAsociadas(posicionNegociable, productosOperables, poseeCuentaAsociadaPesos, poseeCuentaAsociadaDolares);
            for (Objeto posicionNegociablePorCuentasAsociadas : posicionesNegociablesPorCuentasAsociadas) {
                respuesta.add("tenencia", posicionNegociablePorCuentasAsociadas);
            }
        }
        if (respuesta.get("tenencia") == null) {
            return RespuestaMB.estado("SIN_TENENCIA");
        }
        return respuesta;
    }

    private static List<Objeto> filtrarPosicionesNegociablesPorCuentasAsociadas(Objeto posicionNegociable, List<Objeto> productosOperables, boolean poseeCuentaAsociadaPesos, boolean poseeCuentaAsociadaDolares) {
        List<Objeto> posicionesNegociablesPorCuentasAsociadas = new ArrayList<>();

        List<Objeto> productosOperablesPorPosicionNegociable = filtrarProductosOperablesPorPosicionNegociable(posicionNegociable.string("codigo"), productosOperables);

        for (Objeto productoOperable : productosOperablesPorPosicionNegociable) {

            if (("PESOS".equals(productoOperable.string("descMoneda")) && poseeCuentaAsociadaPesos) || ("USD".equals(productoOperable.string("descMoneda")) && poseeCuentaAsociadaDolares)) {

                String codigoProductoOperable = productoOperable.string("codigo");
                BigDecimal saldoNominal = posicionNegociable.bigDecimal("saldoDisponible");
                Objeto tenencia = new Objeto();
                tenencia.set("id", codigoProductoOperable);
                tenencia.set("descripcion", codigoProductoOperable + " - " + productoOperable.string("descripcion"));
                tenencia.set("tipoActivo", productoOperable.string("clasificacion"));
                tenencia.set("moneda", productoOperable.string("descMoneda").equals("PESOS") ? 80 : 2);
                tenencia.set("cantidadNominal", saldoNominal);
                tenencia.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
                posicionesNegociablesPorCuentasAsociadas.add(tenencia);
            }
        }
        return posicionesNegociablesPorCuentasAsociadas;
    }

    private static List<Objeto> filtrarProductosOperablesPorPosicionNegociable(String codigoPosicionNegociable, List<Objeto> productosOperables) {
        List<Objeto> productosOperablesPorCodigo = new ArrayList<>();
        for (Objeto productoOperable : productosOperables) {
            if (codigoPosicionNegociable.equals(productoOperable.string("producto"))) {
                productosOperablesPorCodigo.add(productoOperable);
            }
        }
        return productosOperablesPorCodigo;
    }

    public static RespuestaMB movimientos(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        Date fechaDesde = contexto.parametros.date("fechaDesde", C_FORMATO_FECHA_1_DMMYYYY);
        Date fechaHasta = contexto.parametros.date("fechaHasta", C_FORMATO_FECHA_1_DMMYYYY);
        Boolean ajustarMesCompleto = contexto.parametros.bool("ajustarMesCompleto", false);

        if (Objeto.anyEmpty(idCuentaComitente, fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        if (ajustarMesCompleto) {
            LocalDate primerDiaDelMes = LocalDate.parse(contexto.parametros.string("fechaDesde"), DateTimeFormatter.ofPattern(C_FORMATO_FECHA_1_DMMYYYY));
            fechaDesde = java.sql.Date.valueOf(primerDiaDelMes.with(TemporalAdjusters.firstDayOfMonth()));

            LocalDate ultimoDiaDelMes = LocalDate.parse(contexto.parametros.string("fechaHasta"), DateTimeFormatter.ofPattern(C_FORMATO_FECHA_1_DMMYYYY));
            fechaHasta = java.sql.Date.valueOf(ultimoDiaDelMes.with(TemporalAdjusters.lastDayOfMonth()));
        }

        ApiMB.eliminarCache(contexto, "MovimientosTitulosValores", contexto.idCobis(), cuentaComitente.numero());
        ApiResponseMB movimientos = RestInversiones.movimientos(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (movimientos.hayError()) {
            if (movimientos.string("mensajeAlUsuario").startsWith("2100-")) {
                return RespuestaMB.estado("SIN_MOVIMIENTOS");
            }
            return RespuestaMB.error();
        }

        PlazoLiquidacion plazo = null;

        List<Objeto> listMovimientos = new ArrayList<>();
        for (Objeto item : movimientos.objetos("operaciones")) {
            String tipo = item.string("tipo").toLowerCase();

            String tipoOperacion = "";
            tipoOperacion = tipo.contains("liquidacion de cupon") ? "LICU" : tipoOperacion;
            tipoOperacion = tipo.contains("venta de especie") ? "V" : tipoOperacion;
            tipoOperacion = tipo.contains("compra de especies") ? "C" : tipoOperacion;
            tipoOperacion = tipo.contains("ividendo") && tipo.contains("fectivo") ? "DIVE" : tipoOperacion;
            tipoOperacion = tipo.contains("ividendo") && tipo.contains("ccion") ? "DIVA" : tipoOperacion;
            tipoOperacion = tipo.contains("ingreso") ? "INGR" : tipoOperacion;
            tipoOperacion = tipo.contains("egreso") ? "EGR" : tipoOperacion;

            Objeto movimiento = new Objeto();

            movimiento.set("id", item.string("idOperacion"));
            movimiento.set("id_comprobante", item.string("idOperacion")
                    + "_" + idCuentaComitente + "_"
                    + contexto.parametros.string("fechaDesde") + "_"
                    + contexto.parametros.string("fechaHasta"));
            movimiento.set("fechaOperacion", item.date("fechaConcertacion"
                    , C_FORMATO_FECHA_2_DDMMYYYY
                    , C_FORMATO_FECHA_2_DDMMYYYY));
            movimiento.set("tipo", item.string("tipo").split(" ")[0]);
            movimiento.set("especie", item.string("especie.codigo") + " - " + item.string("especie.descripcion"));
            movimiento.set("simboloMoneda", item.string("cuentaLiquidacionME").contains("USD") ? "USD" : "$");

            Boolean esDolares = false;
            if (!item.bigDecimal("totalME", "0.0").equals(new BigDecimal("0.0"))) {
                esDolares = true;
                movimiento.set("monto", item.bigDecimal("totalME"));
                movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("totalME")));
            }

            Boolean esPesos = false;
            if (!item.bigDecimal("totalML", "0.0").equals(new BigDecimal("0.0"))) {
                esPesos = true;
                movimiento.set("monto", item.bigDecimal("totalML"));
                movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("totalML")));
            }

            if (esPesos && esDolares) {
                movimiento.set("monto", item.bigDecimal("bruto"));
                movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("bruto")));
            }

            movimiento.set("tipoOperacion", tipoOperacion);
            movimiento.set("cantidadResidual", item.string("cantidadResidualActual"));
            movimiento.set("cantidadNominal", item.string("cantidadNominal"));
            movimiento.set("fechaPago", item.date("fechaPago"
                    , C_FORMATO_FECHA_2_DDMMYYYY
                    , C_FORMATO_FECHA_2_DDMMYYYY));

            plazo = null;
            try {
                plazo = PlazoLiquidacion.codigo(item.string("plazo"));
            } catch(Exception e) {
                e.printStackTrace();
            }

            if (plazo == null) {
                plazo = PlazoLiquidacion.NULO;
            }

            movimiento.set("plazoDeLiquidacion", plazo.toObjeto());

            movimiento.set("fechaLiquidacion", calcularFechaLiquidacionActivo(contexto,
                    movimiento.date("fechaOperacion", C_FORMATO_FECHA_2_DDMMYYYY), plazo));

            movimiento.set("numeroBoleta", item.string("numeroBoleto"));
            movimiento.set("numeroMinuta", item.string("numeroMinuta"));
            movimiento.set("porcentajeValorResidual", item.string("valorResidual"));
            movimiento.set("comision", item.bigDecimal("comision"));
            movimiento.set("comisionFormateada", Formateador.importeCantDecimales(item.bigDecimal("comision"), 2));
            movimiento.set("derechos", item.bigDecimal("derechos"));
            movimiento.set("derechosFormateado", Formateador.importeCantDecimales(item.bigDecimal("derechos"), 2));
            movimiento.set("precio", item.bigDecimal("precio"));
            movimiento.set("precioFormateado", Formateador.importeCantDecimales(item.bigDecimal("precio"), 4));

            listMovimientos.add(movimiento);
        }

        listMovimientos.sort((o1, o2) -> {
            if (o1.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY) == null || o2.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY) == null)
                return 0;
            return o1.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY).compareTo(o2.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY));
        });
        Collections.reverse(listMovimientos);
        listMovimientos.forEach(o -> respuesta.add("movimientos", o));

        return respuesta;
    }

    public static RespuestaMB seguimientoOperaciones(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        Date fechaDesde = contexto.parametros.date("fechaDesde", C_FORMATO_FECHA_1_DMMYYYY
                , new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L));
        Date fechaHasta = contexto.parametros.date("fechaHasta"
                , C_FORMATO_FECHA_1_DMMYYYY, new Date());

        DateFormat dateFormat = new SimpleDateFormat(C_FORMATO_FECHA_2_DDMMYYYY);
        String strFechaDesde = dateFormat.format(fechaDesde);
        String strFechaHasta = dateFormat.format(fechaHasta);

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        ApiMB.eliminarCache(contexto, "SeguimientoOperacionesTitulosValores"
                , contexto.idCobis(), cuentaComitente.numero());
        ApiResponseMB seguimientoOperaciones
                = RestInversiones.seguimientoOperaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (seguimientoOperaciones.hayError()) {
            if (seguimientoOperaciones.string("codigo").startsWith("2100")) {
                return RespuestaMB.estado("SIN_OPERACIONES");
            }
            return RespuestaMB.error();
        }

        List<Objeto> ordenes = seguimientoOperaciones.objetos("ordenesOrdenadas");
        ordenes.sort((o1, o2) -> {
            if (o1.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD) == null
                    || o2.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD) == null)
                return 0;
            return o1.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD)
                    .compareTo(o2.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD));
        });
        Collections.reverse(ordenes);
        PlazoLiquidacion plazo = null;
        List<Objeto> operaciones = new ArrayList<>();
        for (Objeto operacion : seguimientoOperaciones.objetos("ordenesOrdenadas")) {
            Objeto item = new Objeto();
            item.set("idComprobante", operacion.string("ordenSecuencial") + "_" + idCuentaComitente + "_" + operacion.string("numero") + "_" + strFechaDesde + "_" + strFechaHasta);
            item.set("fecha", operacion.date("fecha", "yyyy-MM-dd", C_FORMATO_FECHA_2_DDMMYYYY));
            item.set("tipo", operacion.string("tipo").split(" ")[0]);
            item.set("especie", operacion.string("tipoEspecie") + " - " + operacion.string("descEspecie"));
            item.set("cantidadNominalFormateada", Formateador.importe(operacion.bigDecimal("cantidadNominal"), 0));
            item.set("montoFormateado", Formateador.importe(operacion.bigDecimal("monto")));

            BigDecimal precio = Util.dividir(operacion.bigDecimal("monto"), operacion.bigDecimal("cantidadNominal"));
            item.set("precioFormateado", precio != null ? Formateador.importe(precio, 4) : "");

            item.set("estado", operacion.string("estado").split(" ")[0]);

            item.set("tipoExtendido", operacion.string("tipo"));
            item.set("estadoExtendido", operacion.string("estado"));
            // Precio
            item.set("codigoEspecie", operacion.string("tipoEspecie"));
            item.set("precioLimiteFormateado", Formateador.importeCantDecimales(operacion.bigDecimal("precioLimite"), 4));
            // Numero minuta
            item.set("numeroOrden", operacion.string("numero"));
            if (operacion.string("moneda").equals("PESOS")) {
                item.set("idMoneda", 80);
                item.set("simboloMoneda", "$");
            }
            if (operacion.string("moneda").equals("USD")) {
                item.set("idMoneda", 2);
                item.set("simboloMoneda", "USD");
            }

            item.set("tipoDePrecio",
                    operacion.string("precioLimite") == null || operacion.string("precioLimite").isBlank()
                            ? "Precio Mercado" : "Precio Límite");

            plazo = null;
            try {
                plazo = PlazoLiquidacion.codigo(operacion.string("plazo"));
            } catch(Exception e) {
                e.printStackTrace();
            }

            if (plazo == null) {
                plazo = PlazoLiquidacion.NULO;
            }
            item.set("plazoDeLiquidacion", plazo.toObjeto());

            item.set("fechaLiquidacion", calcularFechaLiquidacionActivo(contexto,
                    item.date("fecha", C_FORMATO_FECHA_2_DDMMYYYY), plazo));

            operaciones.add(item);
        }

        operaciones.sort((o1, o2) -> {
            if (o1.date("fecha", C_FORMATO_FECHA_3_DDMMYY) == null || o2.date("fecha", C_FORMATO_FECHA_3_DDMMYY) == null)
                return 0;
            return o1.date("fecha", C_FORMATO_FECHA_3_DDMMYY).compareTo(o2.date("fecha", C_FORMATO_FECHA_3_DDMMYY));
        });
        Collections.reverse(operaciones);
        operaciones.forEach(o -> respuesta.add("operaciones", o));

        return respuesta;
    }

    public static RespuestaMB seguimientoLicitaciones(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        Date fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L));
        Date fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", new Date());

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        ApiMB.eliminarCache(contexto, "SeguimientoLicitacionesTitulosValores", contexto.idCobis(), cuentaComitente.numero()); // elimino cache ya que lo uso cada vez que consulto un comprobante
        ApiResponseMB seguimientoLicitaciones = RestInversiones.seguimientoLicitaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (seguimientoLicitaciones.hayError() || seguimientoLicitaciones.codigo == 204) {
            if (seguimientoLicitaciones.codigo == 204) {
                return RespuestaMB.estado("SIN_LICITACIONES");
            }
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto licitacion : seguimientoLicitaciones.objetos()) {
            Objeto item = new Objeto();

            item.set("idComprobante", licitacion.string("Licitacion") + "_" + licitacion.string("Especie") + "_" + licitacion.string("Numero") + "_" + idCuentaComitente + "_" + new SimpleDateFormat("d/M/yyyy").format(fechaDesde) + "_" + new SimpleDateFormat("d/M/yyyy").format(fechaHasta));
            item.set("licitacion", licitacion.string("LicitacionDescripcion"));
            item.set("especie", licitacion.string("EspecieDescripcion"));
            item.set("tramo", licitacion.string("Tramo"));
            item.set("numero", licitacion.string("Numero"));
            item.set("estado", licitacion.string("Estado"));
            item.set("fecha", licitacion.date("FechaConcertacion", "yyyy-MM-dd", "dd/MM/yy"));
            item.set("cuentaComitente", licitacion.string("Comitente"));
            item.set("cuentaLiquidacion", licitacion.string("Cuenta"));
            item.set("cantidadLicitada", licitacion.string("CantidadLicitada"));
            item.set("cantidadAdjudicada", licitacion.string("CantidadAdjudicada"));
            item.set("precioAdjudicadoFormateado", Formateador.importe(licitacion.bigDecimal("PrecioAdjudicado")));
            respuesta.add("licitaciones", item);
        }

        return respuesta;
    }

    public static byte[] comprobanteLicitaciones(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");

        // VEP_2019-05-25_2019-06-25_20105176512_034000038777
        String codigoLicitacion = idComprobante.split("_")[0];
        String codigoEspecie = idComprobante.split("_")[1];
        String numero = idComprobante.split("_")[2];
        String idCuentaComitente = idComprobante.split("_")[3];
        Date fechaDesde;
        Date fechaHasta;
        try {
            fechaDesde = new SimpleDateFormat("d/M/yyyy").parse(idComprobante.split("_")[4]);
            fechaHasta = new SimpleDateFormat("d/M/yyyy").parse(idComprobante.split("_")[5]);
        } catch (ParseException e) {
            return null;
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return null;
        }

        ApiMB.eliminarCache(contexto, "SeguimientoLicitacionesTitulosValores", contexto.idCobis(), cuentaComitente.numero()); // elimino cache ya que lo uso cada vez que consulto un comprobante
        ApiResponseMB seguimientoLicitaciones = RestInversiones.seguimientoLicitaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (seguimientoLicitaciones.hayError()) {
            return null;
        }

        String descripcionLicitacion = "";
        String descripcionEspecie = "";
        Integer cantidad = Integer.valueOf(0);
        BigDecimal valor = new BigDecimal(0);
        String tramo = "";
        String cuentaComitenteNumero = "";
        String cuentaNumero = "";
        String fecha = "";

        boolean encontro = false;

        for (Objeto item : seguimientoLicitaciones.objetos()) {
            if (item.string("Licitacion").equals(codigoLicitacion) && item.string("Especie").equals(codigoEspecie) && item.string("Numero").equals(numero)) {
                fecha = item.string("FechaConcertacion") + " " + item.string("HoraConcertacion");
                descripcionLicitacion = item.string("LicitacionDescripcion");
                descripcionEspecie = item.string("EspecieDescripcion");
                cantidad = item.integer("CantidadAdjudicada");
                valor = item.bigDecimal("PrecioAdjudicado");
                tramo = item.string("Tramo");
                cuentaComitenteNumero = cuentaComitente.numero();
                cuentaNumero = item.string("Cuenta");

                encontro = true;
                break;
            }
        }

        if (!encontro) {
            return null;
        }

        String idComprobanteResultado = MBInversion.generarComprobanteLicitacion(contexto, numero, fecha, codigoLicitacion, descripcionLicitacion, codigoEspecie, descripcionEspecie, cantidad, valor, tramo, cuentaComitenteNumero, cuentaNumero);
        contexto.parametros.set("id", idComprobanteResultado);
        return MBArchivo.comprobante(contexto);
    }

    /* ========== MERCADO SECUNDARIO ========== */
    public static RespuestaMB tiposActivo(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add("tipos", new Objeto().set("id", "Accion").set("descripcion", "Acciones"));
        respuesta.add("tipos", new Objeto().set("id", "CHA").set("descripcion", "Cédula Hipotecaria"));
        respuesta.add("tipos", new Objeto().set("id", "Cedear").set("descripcion", "Cedears"));
        respuesta.add("tipos", new Objeto().set("id", "Titulo Publico").set("descripcion", "Bonos y Obligaciones Negociables"));

        Objeto tiposActivos = new Objeto();
        for (Objeto tipo : respuesta.objetos("tipos")) {
            tiposActivos.add(tipo);
        }
        tiposActivos.ordenar("descripcion");
        respuesta.set("tipos", tiposActivos);

        return respuesta;
    }

    private static BigDecimal calcularPrecioMercadoTope(ContextoMB contexto, String idEspecie, PlazoLiquidacion plazo,
                                                        TipoOperacionInversion tipoOperacion) {

        boolean criterioSeleccionPuntas = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),"criterio_seleccion_puntas", "criterio_seleccion_puntas_cobis");

        Objeto puntas = buscarPuntasMercado(contexto, idEspecie, plazo);
        if (puntas.string("estado").equals("ERROR")
                || puntas.objetos("puntas") == null) {
            return BigDecimal.ZERO;
        }

        if (!tipoOperacion.isCompra() && !tipoOperacion.isVenta()) {
            throw new IllegalArgumentException(tipoOperacion.name());
        }


        BigDecimal escalarPrecio = ConfigMB.bigDecimal(
                tipoOperacion.isCompra()
                        ? "escalar_precio_mercado_compra_acciones_bonos"
                        : "escalar_precio_mercado_venta_acciones_bonos",
                BigDecimal.ZERO
        );

        BigDecimal precio = null;

        if (criterioSeleccionPuntas) {
            // NUEVO CRITERIO (Toma la 3ra posición o la última disponible)
            if (tipoOperacion.isCompra()) {
                List<BigDecimal> preciosVenta = puntas.objetos("puntas").stream()
                        .map(p -> p.bigDecimal("precioVenta"))
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());

                if (preciosVenta.size() >= 3) {
                    precio = preciosVenta.get(2); // índice 2 => tercera posición
                } else if (!preciosVenta.isEmpty()) {
                    precio = preciosVenta.get(preciosVenta.size() - 1); // última disponible
                }

            } else {
                List<BigDecimal> preciosCompra = puntas.objetos("puntas").stream()
                        .map(p -> p.bigDecimal("precioCompra"))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.reverseOrder()) // de mayor a menor
                        .collect(Collectors.toList());

                if (preciosCompra.size() >= 3) {
                    precio = preciosCompra.get(2);
                } else if (!preciosCompra.isEmpty()) {
                    precio = preciosCompra.get(preciosCompra.size() - 1);
                }
            }
        } else {
            // CRITERIO ANTERIOR (Tomar mejor punta)
            if (tipoOperacion.isCompra()) {
                // La punta de venta más baja
                precio = puntas.objetos("puntas").stream()
                        .map(p -> p.bigDecimal("precioVenta"))
                        .filter(Objects::nonNull)
                        .min(Comparator.naturalOrder()) // Tomamos el menor precio disponible
                        .orElse(null);
            } else {
                // La punta de compra más alta
                precio = puntas.objetos("puntas").stream()
                        .map(p -> p.bigDecimal("precioCompra"))
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder()) // Tomamos el mayor precio disponible
                        .orElse(null);
            }
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 || escalarPrecio.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);
        Objeto productoOperable = productosOperables.get(idEspecie);
        if ("Titulo Publico".equals(productoOperable.string("clasificacion"))) {
            precio = precio.divide(BigDecimal.valueOf(100));
        }

        //precio = precio.multiply(BigDecimal.ONE.add(escalarPrecio));

        switch (productoOperable.string("clasificacion")) {
            case "Titulo Publico":
                precio = precio.setScale(4, RoundingMode.HALF_UP);
                break;
            default:
                precio = precio.setScale(2, RoundingMode.HALF_UP);
                break;
        }

        return precio;
    }

    public static RespuestaMB simularCompra(ContextoMB contexto) {
        String version = contexto.parametros.string("version");
        if (version == null || version.isBlank() || version.equals("1")) {
            return simularCompraV1(contexto);
        }
        return simularCompraV2(contexto);
    }

    public static RespuestaMB simularCompraV2(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        final String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);
        TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
        PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
        Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
        if (tipoPrecio.isPrecioLimite()) {
            if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaMB.estado("PRECIO_LIMITE_INVALIDO");
            }

            precioLimiteOperacion = precioLimite;
        } else if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacionFuturo = new Futuro<>(
                    () -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA));
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);
        Objeto productoOperable = productosOperables.get(idEspecie);

        ApiRequestMB request = ApiMB.request("SimularCompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", productoOperable.string("descMoneda"));
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo.codigo());
        request.body("tipo", "Compra");
        request.body("tipoServicio", "Consulta");
        request.body("vigencia", 0);
        if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacion = precioLimiteOperacionFuturo.get();
            if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
                return RespuestaMB.estado("PRECIO_MERCADO_INVALIDO");
            }
        }
        request.body("precioLimite", precioLimiteOperacion);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            if (response.string("codigo").equals("2122")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2020")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }

            return RespuestaMB.error();
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("comisiones", response.bigDecimal("comisiones"));
        orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
        orden.set("vigencia", "por el día");
        orden.set("precioMercado", precioMercado);
        orden.set("cantidadNominal", cantidadNominal);
        return RespuestaMB.exito("orden", orden);
    }

    public static RespuestaMB calcularCantidadNominalCompra(ContextoMB contexto) {

        BigDecimal comisionCompra = new BigDecimal(BigInteger.ZERO);

        if (contexto.parametros.existe("precioMercado")) {
            Boolean esPrecioMercado = contexto.parametros.bool("precioMercado");

            if (!esPrecioMercado) {
                return simularCompraV2(contexto);
            }
        }

        BigDecimal importeIngresadoPorUsuario = contexto.parametros.bigDecimal("importeIngresadoPorUsuario");
        if (importeIngresadoPorUsuario == null || importeIngresadoPorUsuario.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaMB.estado("IMPORTE_INVALIDO");
        }

        String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
        PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));


        Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie);
        String tipoInstrumento = datosTituloValor.string("clasificacion");
        comisionCompra = calcularComisionPorTipo(tipoInstrumento);

        BigDecimal montoComision = importeIngresadoPorUsuario
                .multiply(comisionCompra)
                .setScale(2, RoundingMode.HALF_DOWN);

        importeIngresadoPorUsuario = importeIngresadoPorUsuario
                .subtract(montoComision);

        BigDecimal precioMercado = calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA);
        if (precioMercado.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaMB.estado("PRECIO_MERCADO_NO_DISPONIBLE");
        }

        BigDecimal cantidadNominal = importeIngresadoPorUsuario.divide(precioMercado, 0, RoundingMode.FLOOR);
        if (cantidadNominal.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaMB.estado("CANTIDAD_NOMINAL_INVALIDA");
        }

        contexto.parametros.set("cantidadNominal", cantidadNominal);

        return simularCompraV2(contexto);
    }

    public static BigDecimal calcularComisionPorTipo(String tipoInstrumento) {

        BigDecimal comision_general_acciones_cedears = ConfigMB.bigDecimal("inv_comision_general_acciones_cedears");
        BigDecimal comision_general_bonos = ConfigMB.bigDecimal("inv_comision_general_bonos");

        switch (tipoInstrumento) {
            case "Accion", "Cedear":
                return comision_general_acciones_cedears;
            case "CHA", "Titulo Publico":
                return comision_general_bonos;
            default:
                throw new IllegalArgumentException("Tipo de instrumento inválido: " + tipoInstrumento);
        }
    }


    public static RespuestaMB comprar(ContextoMB contexto) {
        String version = contexto.parametros.string("version");
        if (version == null || version.isBlank() || version.equals("1")) {
            return comprarV1(contexto);
        }
        // INV-692
        return comprarV2(contexto);
    }

    public static RespuestaMB comprarV2(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        final String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);
        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Boolean mostrarLeyendaTituloPublico = contexto.parametros.bool("mostrarLeyendaTituloPublico", false);
        TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
        PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
        Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
        if (tipoPrecio.isPrecioLimite()) {
            if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaMB.estado("PRECIO_LIMITE_INVALIDO");
            }

            precioLimiteOperacion = precioLimite;
        } else if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacionFuturo = new Futuro<>(
                    () -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA));
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto,
                cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error()
                    : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        RespuestaMB resultado = MBInversion.validarPerfilInversor(contexto,
                Optional.of(MBInversion.EnumPerfilInversor.ARRIESGADO), operaFueraPerfil);
        if (resultado.hayError()) {
            RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", "Perfil inversor incorrecto para operar");
            return respuesta;
        }

        Boolean operarFueraPerfilEstaTransaccion = resultado.bool("operaBajoPropioRiesgo");

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);
        Objeto productoOperable = productosOperables.get(idEspecie);

        ApiRequestMB request = ApiMB.request("CompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", StringUtils.substringBefore(idEspecie, "_"));
        request.body("fecha", fecha);
        request.body("moneda", productoOperable.string("descMoneda"));
        request.body("operaFueraDePerfil", operarFueraPerfilEstaTransaccion ? "SI" : "NO");
        request.body("plazo", plazo.codigo());
        request.body("tipo", "Compra");
        request.body("tipoServicio", "Operacion");
        request.body("vigencia", 0);
        if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacion = precioLimiteOperacionFuturo.get();
            if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
                return RespuestaMB.estado("PRECIO_MERCADO_INVALIDO");
            }
        }
        request.body("precioLimite", precioLimiteOperacion);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            if (response.string("codigo").equals("2009")) {
                RespuestaMB respuestaError = new RespuestaMB();
                respuestaError.set("estado", "FUERA_HORARIO");
                respuestaError.set("mensaje", response.string("mensajeAlUsuario"));
                return respuestaError;
            }
            if (response.string("codigo").equals("5000")) {
                return RespuestaMB.estado("FONDOS_INSUFICIENTES");
            }
            if (response.string("codigo").equals("2013")) {
                return RespuestaMB.estado("PLAZO_INVALIDO");
            }
            if (response.string("codigo").equals("2025")) {
                return RespuestaMB.estado("SIN_PERFIL_INVERSOR");
            }
            if (response.string("codigo").equals("2023")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return RespuestaMB.error();
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                        : descripcionError;

                SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("MB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError

                sqlRequest.add("Compra"); // operacion
                sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
                sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
                sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
                sqlRequest.add(StringUtils.substringBefore(idEspecie, "_")); // especie
                sqlRequest.add(productoOperable.string("descMoneda")); // moneda
                sqlRequest.add(operarFueraPerfilEstaTransaccion ? "SI" : "NO"); // operaFueraDePerfil
                sqlRequest.add(plazo.codigo()); // plazo
                sqlRequest.add(precioLimiteOperacion); // precioLimite
                sqlRequest.add(cantidadNominal); // cantidadNominal
                sqlRequest.add("0"); // vigencia
                sqlRequest.add(response.string("idOrden")); // idOrden
                sqlRequest.add(response.string("numeroOrden")); // numeroOrden
                sqlRequest.add(response.string("comisiones")); // comisiones

                // EMM: para algunos casos desde el front cuando es un título público
                // y a parte es en pesos se muestra una leyenda específica.
                if (mostrarLeyendaTituloPublico) {
                    sqlRequest.add(cuenta.esDolares() ? "6" : "12"); // versionDDJJ
                } else {
                    sqlRequest.add(cuenta.esDolares() ? "6" : ""); // versionDDJJ
                }

                SqlMB.response(sqlRequest);
            } catch (Exception e) {
            }
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("numero", response.string("numeroOrden"));

        // emm-20190613-desde--> Comprobante
        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", response.string("numeroOrden"));
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
        comprobante.put("ESPECIE", idEspecie + " - " + productoOperable.string("descripcion"));
        String moneda = "$";
        if (!"PESOS".equals(productoOperable.string("descMoneda"))) {
            moneda = "USD";
        }
        comprobante.put("IMPORTE",
                moneda + " " + Formateador.importe(precioLimiteOperacion.multiply(new BigDecimal(cantidadNominal))));
        comprobante.put("TIPO_OPERACION", "Compra");
        comprobante.put("TIPO_ACTIVO", productoOperable.string("clasificacion")); // todo necesito la accion que eligió
        // el cliente
        comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimiteOperacion));
        comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
        comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
        comprobante.put("CUENTA", cuenta.numero());
        comprobante.put("PLAZO", plazo.descripcion());
        comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
        comprobante.put("VIGENCIA", "0");
        String idComprobante = "titulo-valor-compra_" + response.string("idOrden");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        // emm-20190613-hasta--> Comprobante

        try {
            for (String email : ConfigMB.string("mercadosecundario_email").split(";")) {
                if (!email.trim().isEmpty()) {
                    String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
                    String mensaje = "<html><head></head><body>";
                    mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
                    mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
                    mensaje += "<b>Operación:</b> " + "Compra" + "<br/>";
                    mensaje += "<b>Precio:</b> " + precioLimiteOperacion + "<br/>";
                    mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
                    mensaje += "<b>Plazo:</b> " + plazo.descripcion() + "<br/>";
                    mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
                    mensaje += "</body></html>";

                    if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                        String salesforce_compra_venta_dolar_mep = ConfigMB.string("salesforce_compra_venta_dolar_mep");
                        String salesforce_compra_venta_bonos_acciones = ConfigMB.string("salesforce_compra_venta_bonos_acciones");
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        parametros.set("ASUNTO", asunto);
                        parametros.set("MENSAJE", mensaje);
                        parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
                        parametros.set("EMAIL_DESTINO", email);
                        parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                        parametros.set("TIPO_OPERACION", "Compra");

                        if (idEspecie.equals("AL30")) {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_dolar_mep, parametros));
                        } else {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_bonos_acciones, parametros));
                        }
                    }
                    else {
                        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", email.trim());
                        requestMail.body("plantilla", ConfigMB.string("doppler_generico"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("ASUNTO", asunto);
                        parametros.set("BODY", mensaje);
                        ApiMB.response(requestMail);

                    }
                }
            }
        } catch (Exception e) {
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idComprobante", "titulo-valor-compra_" + response.string("idOrden"));
        respuesta.set("orden", orden);
        return respuesta;
    }

    public static RespuestaMB simularVenta(ContextoMB contexto) {
        String version = contexto.parametros.string("version");
        if (version == null || version.isBlank() || version.equals("1")) {
            return simularVentaV1(contexto);
        }
        return simularVentaV2(contexto);
    }

    public static RespuestaMB simularVentaV2(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        // FIXME GB - Estos substringBefore estan en MB pero no en HB (comprar, vender y simulaciones). Son necesarios?
        String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
        Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
        PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
        Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
        if (tipoPrecio.isPrecioLimite()) {
            if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaMB.estado("PRECIO_LIMITE_INVALIDO");
            }

            precioLimiteOperacion = precioLimite;
        } else if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacionFuturo = new Futuro<>(
                    () -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.VENTA));
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        ApiRequestMB request = ApiMB.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo.codigo());
        request.body("tipo", "Venta");
        request.body("tipoServicio", "Consulta");
        request.body("vigencia", 0);
        if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacion = precioLimiteOperacionFuturo.get();
            if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
                return RespuestaMB.estado("PRECIO_MERCADO_INVALIDO");
            }
        }
        request.body("precioLimite", precioLimiteOperacion);

        ApiResponseMB response = ApiMB.response(request);
        String idEspecieProducto = idEspecie;
        if (response.hayError()) {

            if (response.string("codigo").equals("2006") || response.string("mensajeAlUsuario").contains("El producto no existe")) {

                Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto);
                Objeto productoOperable = productosOperables.get(idEspecieProducto);

                if (productoOperable != null) {
                    idEspecieProducto = productoOperable.string("codigo");

                    request = ApiMB.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
                    request.query("idcobis", contexto.idCobis());
                    request.body("cantidadNominal", cantidadNominal);
                    request.body("cuentaComitente", cuentaComitente.numero());
                    request.body("cuentaLiquidacionMonetaria", cuenta.numero());
                    request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
                    request.body("especie", idEspecieProducto);
                    request.body("fecha", fecha);
                    request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
                    request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
                    request.body("plazo", plazo.codigo());
                    request.body("precioLimite", precioLimiteOperacion);
                    request.body("tipo", "Venta");
                    request.body("tipoServicio", "Consulta");
                    request.body("vigencia", 0);

                    response = ApiMB.response(request);
                }
            }
        }

        if (response.hayError()) {
            if (response.string("codigo").equals("2122")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2020")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2014")) {
                if (response.string("mensajeAlUsuario").contains("no es habil")) {
                    RespuestaMB respuesta = RespuestaMB.estado("DIA_NO_HABIL");
                    respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                    return respuesta;
                }
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }

            return RespuestaMB.error();
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("comisiones", response.bigDecimal("comisiones"));
        orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
        orden.set("vigencia", "por el día");
        orden.set("precioMercado", precioMercado);
        return RespuestaMB.exito("orden", orden);
    }

    public static RespuestaMB vender(ContextoMB contexto) {
        String version = contexto.parametros.string("version");
        if (version == null || version.isBlank() || version.equals("1")) {
            return venderV1(contexto);
        }
        return venderV2(contexto);
    }

    public static RespuestaMB venderV2(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        final String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
        PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
        Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
        if (tipoPrecio.isPrecioLimite()) {
            if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
                return RespuestaMB.estado("PRECIO_LIMITE_INVALIDO");
            }

            precioLimiteOperacion = precioLimite;
        } else if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacionFuturo = new Futuro<>(
                    () -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.VENTA));
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        ApiRequestMB request = ApiMB.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo.codigo());
        request.body("tipo", "Venta");
        request.body("tipoServicio", "Operacion");
        request.body("vigencia", 0);

        if (tipoPrecio.isPrecioMercado()) {
            precioLimiteOperacion = precioLimiteOperacionFuturo.get();
            if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
                return RespuestaMB.estado("PRECIO_MERCADO_INVALIDO");
            }
        }

        request.body("precioLimite", precioLimiteOperacion);

if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_rescate_fondos",
                "prendido_modo_transaccional_rescate_fondos_cobis") && !TransmitMB.isChallengeOtp(contexto, "venta-acciones-bonos")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                boolean esBono = false;
                RespuestaMB respuestaEspecies;
                contexto.parametros.set("idTipoActivo", "Titulo Publico");
                respuestaEspecies = especies(contexto, true);
                if (!respuestaEspecies.hayError() && !Objeto.empty(respuestaEspecies.objetos("especies").stream().filter(e -> e.string("id").split("_")[0].equals(idEspecie)).findAny().orElse(null)))
                    esBono = true;

                RescateMBBMBankProcess rescateMBBMBankProcess = new RescateMBBMBankProcess(contexto.idCobis(),
                        sessionToken,
                        precioLimiteOperacion.multiply(BigDecimal.valueOf(cantidadNominal)),
                        Util.obtenerDescripcionMonedaTransmit(cuenta.idMoneda()),
                        esBono ? TransmitMB.REASON_VENTA_BONOS : TransmitMB.REASON_VENTA_ACCIONES,
                        new RescateMBBMBankProcess.Payer(contexto.persona().cuit(), cuenta.numero(), Util.getBhCodigo(), TransmitMB.CANAL),
                        new RescateMBBMBankProcess.Payee(contexto.persona().cuit(), "", ""));

                RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, rescateMBBMBankProcess, "venta-acciones-bonos");
                if (respuesta.hayError())
                    return respuesta;

            } catch (Exception e) {
            }
        }

        if (TransmitMB.isChallengeOtp(contexto, "venta-acciones-bonos") && !contexto.validaSegundoFactor("venta-acciones-bonos"))
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");


        ApiResponseMB response = ApiMB.response(request);
        String idEspecieProducto = idEspecie;
        if (response.hayError()) {

            if (response.string("codigo").equals("2006") || response.string("mensajeAlUsuario").contains("El producto no existe")) {

                Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto);
                Objeto productoOperable = productosOperables.get(idEspecie);

                if (productoOperable != null) {
                    idEspecieProducto = productoOperable.string("codigo");

                    request = ApiMB.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
                    request.query("idcobis", contexto.idCobis());
                    request.body("cantidadNominal", cantidadNominal);
                    request.body("cuentaComitente", cuentaComitente.numero());
                    request.body("cuentaLiquidacionMonetaria", cuenta.numero());
                    request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
                    request.body("especie", idEspecieProducto);
                    request.body("fecha", fecha);
                    request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
                    request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
                    request.body("plazo", plazo.codigo());
                    request.body("tipo", "Venta");
                    request.body("tipoServicio", "Operacion");
                    request.body("vigencia", 0);
                    request.body("precioLimite", precioLimiteOperacion);

                    response = ApiMB.response(request);
                }
            }
        }

        if (response.hayError()) {
            if (response.string("codigo").equals("2009")) {
                RespuestaMB respuestaError = new RespuestaMB();
                respuestaError.set("estado", "FUERA_HORARIO");
                respuestaError.set("mensaje", response.string("mensajeAlUsuario"));
                return respuestaError;
            }
            if (response.string("codigo").equals("5000")) {
                return RespuestaMB.estado("FONDOS_INSUFICIENTES");
            }
            if (response.string("codigo").equals("2013")) {
                return RespuestaMB.estado("PLAZO_INVALIDO");
            }
            if (response.string("codigo").equals("2025")) {
                return RespuestaMB.estado("SIN_PERFIL_INVERSOR");
            }
            if (response.string("codigo").equals("2023")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return RespuestaMB.error();
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

                SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("MB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError

                sqlRequest.add("Venta"); // operacion
                sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
                sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
                sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
                sqlRequest.add(idEspecie); // especie
                sqlRequest.add(cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null); // moneda
                sqlRequest.add(operaFueraPerfil ? "SI" : "NO"); // operaFueraDePerfil
                sqlRequest.add(plazo.codigo()); // plazo
                sqlRequest.add(precioLimiteOperacion); // precioLimite
                sqlRequest.add(cantidadNominal); // cantidadNominal
                sqlRequest.add("0"); // vigencia
                sqlRequest.add(response.string("idOrden")); // idOrden
                sqlRequest.add(response.string("numeroOrden")); // numeroOrden
                sqlRequest.add(response.string("comisiones")); // comisiones
                sqlRequest.add(cuenta.esDolares() ? "11" : "9"); // versionDDJJ

                SqlMB.response(sqlRequest);
            } catch (Exception e) {
            }
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("numero", response.string("numeroOrden"));

        // emm-20190613-desde--> Comprobante
        Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie);

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", response.string("numeroOrden"));
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
        comprobante.put("ESPECIE", idEspecie + " - " + datosTituloValor.string("descripcion"));
        String moneda = cuenta.esPesos() ? "$" : cuenta.esDolares() ? "USD" : "";
        comprobante.put("IMPORTE", moneda + " " + Formateador.importe(precioLimiteOperacion.multiply(new BigDecimal(cantidadNominal))));
        comprobante.put("TIPO_OPERACION", "Venta");
        comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion")); // todo necesito la accion que eligió el cliente
        comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimiteOperacion));
        comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
        comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
        comprobante.put("CUENTA", cuenta.numero());
        comprobante.put("PLAZO", plazo.descripcion());
        comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
        comprobante.put("VIGENCIA", "0");
        String idComprobante = "titulo-valor-venta_" + response.string("idOrden");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        // emm-20190613-hasta--> Comprobante

        try {
            for (String email : ConfigMB.string("mercadosecundario_email").split(";")) {
                if (!email.trim().isEmpty()) {
                    String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
                    String mensaje = "<html><head></head><body>";
                    mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
                    mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
                    mensaje += "<b>Operación:</b> " + "Venta" + "<br/>";
                    mensaje += "<b>Precio:</b> " + precioLimiteOperacion + "<br/>";
                    mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
                    mensaje += "<b>Plazo:</b> " + plazo.descripcion() + "<br/>";
                    mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
                    mensaje += "</body></html>";

                    if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                        String salesforce_compra_venta_dolar_mep = ConfigMB.string("salesforce_compra_venta_dolar_mep");
                        String salesforce_compra_venta_bonos_acciones = ConfigMB.string("salesforce_compra_venta_bonos_acciones");
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        parametros.set("ASUNTO", asunto);
                        parametros.set("MENSAJE", mensaje);
                        parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
                        parametros.set("EMAIL_DESTINO", email);
                        parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                        parametros.set("TIPO_OPERACION", "Venta");

                        if (idEspecie.equals("AL30")) {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_dolar_mep, parametros));
                        } else {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_bonos_acciones, parametros));
                        }
                    }
                    else {

                        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", email.trim());
                        requestMail.body("plantilla", ConfigMB.string("doppler_generico"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("ASUNTO", asunto);
                        parametros.set("BODY", mensaje);
                        ApiMB.response(requestMail);
                    }

                }
            }
        } catch (Exception e) {
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idComprobante", "titulo-valor-venta_" + response.string("idOrden"));
        respuesta.set("orden", orden);
        return respuesta;
    }

    public static RespuestaMB especies(ContextoMB contexto, Boolean modificarId) {
        String idTipoActivo = contexto.parametros.string("idTipoActivo");

        ApiResponseMB response = RestInversiones.titulosValores(contexto, idTipoActivo);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        List<Objeto> resultado = filtrarEspecies(contexto, response.objetos("productosOperablesOrdenados"));

        Objeto especies = new Objeto();
        if (resultado.isEmpty()) {
            return RespuestaMB.exito("especies", new ArrayList<>());
        }

        for (Objeto item : resultado) {
            Objeto especie = new Objeto();
            especie.set("id", !modificarId ? item.string("codigo") : item.string("codigo") + "_" + item.string("descMoneda"));
            especie.set("idTipoActivo", item.string("clasificacion"));
            especie.set("descripcion", item.string("codigo") + " - " + item.string("descripcion"));
            especie.set("idMoneda", item.string("descMoneda").equals("PESOS") ? 80 : 2);
            especies.add(especie);
        }

        especies.ordenar("descripcion");
        return RespuestaMB.exito("especies", especies);
    }

    public static RespuestaMB detalleEspecie(ContextoMB contexto) {
        String idEspecie = contexto.parametros.string("idEspecie");
        String tipoActivo = contexto.parametros.string("tipoActivo");

        if (Objeto.anyEmpty(idEspecie)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        String codigoEspecie = idEspecie.split("_")[0];
        String moneda = idEspecie.split("_")[1];

        ApiResponseMB response = RestInversiones.precioTituloValor(contexto, codigoEspecie, moneda);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto tituloValor = RestInversiones.tituloValor(contexto, codigoEspecie);
        Boolean esAccion = tituloValor.string("clasificacion").equals("Accion");
        boolean esByma = false;
        BigDecimal precio = response.bigDecimal("precioReferencia");

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_licitaciones_byma")) {
            ApiResponseMB responseIndicesRealTime = RestInversiones.indicesRealTime(contexto, codigoEspecie, "", "3");
            if (!responseIndicesRealTime.hayError()) {
                for (Objeto item : responseIndicesRealTime.objetos()) {
                    if (item.bigDecimal("trade") != null && item.bigDecimal("trade").compareTo(new BigDecimal(0)) > 0) {
                        precio = item.bigDecimal("trade");
                        if ("Titulo Publico".equals(tipoActivo) || "CHA".equals(tipoActivo)) {
                            precio = precio.divide(new BigDecimal(100));
                        }
                        esByma = true;
                    }
                }
            }
        }

        BigDecimal precioMinimo = precio.multiply(esAccion ? new BigDecimal("0.9") : new BigDecimal("0.9")).setScale(4, RoundingMode.UP);
        BigDecimal precioMaximo = precio.multiply(esAccion ? new BigDecimal("1.1") : new BigDecimal("1.1")).setScale(4, RoundingMode.DOWN);
        BigDecimal montoMinimo = RestInversiones.tituloValor(contexto, codigoEspecie).bigDecimal("montoMinimo");
        BigDecimal montoMaximo = RestInversiones.tituloValor(contexto, codigoEspecie).bigDecimal("montoMaximo");

        Long cantidadMinima = !Objeto.anyEmpty(montoMinimo, precioMinimo) ? montoMinimo.divide(precioMinimo, RoundingMode.UP).longValue() : null;
        Long cantidadMaxima = !Objeto.anyEmpty(montoMaximo, precioMaximo) ? montoMaximo.divide(precioMaximo, RoundingMode.UP).longValue() - 1 : null;

        Objeto datos = new Objeto();
        datos.set("precio", precio);
        datos.set("precioFormateado", Formateador.importeCantDecimales(precio, 4));
        datos.set("precioMinimo", precioMinimo);
        datos.set("precioMinimoFormateado", Formateador.importeCantDecimales(precioMinimo, 4));
        datos.set("precioMaximo", precioMaximo);
        datos.set("precioMaximoFormateado", Formateador.importeCantDecimales(precioMaximo, 4));
        datos.set("montoMinimo", montoMinimo);
        datos.set("montoMinimoFormateado", Formateador.importeCantDecimales(montoMinimo, 4));
        datos.set("montoMaximo", montoMaximo);
        datos.set("montoMaximoFormateado", Formateador.importeCantDecimales(montoMaximo, 4));
        datos.set("cantidadMinima", cantidadMinima);
        datos.set("cantidadMinimaFormateada", Formateador.entero(cantidadMinima));
        datos.set("cantidadMaxima", cantidadMaxima);
        datos.set("cantidadMaximaFormateada", Formateador.entero(cantidadMaxima));
        datos.set("fecha", response.date("fechaPrecio", "yyyy-MM-dd", "dd/MM/yyyy"));
        datos.set("esPrecioRealTime", esByma);

        return RespuestaMB.exito("datos", datos);
    }

    public static RespuestaMB detalleEspecieProductosOperables(ContextoMB contexto) {
        String idEspecie = contexto.parametros.string("idEspecie");
        String idPlazo = contexto.parametros.string("idPlazo");

        if (Objeto.anyEmpty(idEspecie)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (!idPlazo.isEmpty()) {
            switch (idPlazo) {
                case "0":
                    idPlazo = "1";
                    break;
                case "24":
                    idPlazo = "2";
                    break;
                case "48":
                    idPlazo = "3";
                    break;
                default:
                    break;
            }
        }

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);

        // TODO: TIMEOUT
        if (ConfigMB.esHomologacion() && contexto.idCobis().equals("395778")) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Objeto productoOperable = productosOperables.get(idEspecie);

        if (productoOperable == null) {

            productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto);
            productoOperable = productosOperables.get(idEspecie);

            if (productoOperable == null) {
                return RespuestaMB.error();
            }

        }

        boolean esAccion = "Accion".equals(productoOperable.string("clasificacion"));

        Objeto cotization = buscarPrecioCotizacion(contexto, productoOperable, idPlazo.isEmpty() ? "1" : idPlazo);
        BigDecimal precio = cotization.bigDecimal("precio");

        BigDecimal precioMinimo = precio.multiply(esAccion ? new BigDecimal("0.9") : new BigDecimal("0.9")).setScale(4, RoundingMode.UP);
        BigDecimal precioMaximo = precio.multiply(esAccion ? new BigDecimal("1.1") : new BigDecimal("1.1")).setScale(4, RoundingMode.DOWN);
        BigDecimal montoMinimo = productoOperable.bigDecimal("montoMinimo");
        BigDecimal montoMaximo = productoOperable.bigDecimal("montoMaximo");

        Long cantidadMinima = precioMinimo.signum() != 0 ? montoMinimo.divide(precioMinimo, RoundingMode.UP).longValue() : null;
        Long cantidadMaxima = precioMaximo.signum() != 0 ? montoMaximo.divide(precioMaximo, RoundingMode.UP).longValue() - 1 : null;

        Objeto datos = new Objeto();
        datos.set("precio", precio);
        datos.set("precioFormateado", Formateador.importeCantDecimales(precio, 4));
        datos.set("precioMinimo", precioMinimo);
        datos.set("precioMinimoFormateado", Formateador.importeCantDecimales(precioMinimo, 4));
        datos.set("precioMaximo", precioMaximo);
        datos.set("precioMaximoFormateado", Formateador.importeCantDecimales(precioMaximo, 4));
        datos.set("montoMinimo", montoMinimo);
        datos.set("montoMinimoFormateado", Formateador.importeCantDecimales(montoMinimo, 4));
        datos.set("montoMaximo", montoMaximo);
        datos.set("montoMaximoFormateado", Formateador.importeCantDecimales(montoMaximo, 4));
        datos.set("cantidadMinima", cantidadMinima);
        datos.set("cantidadMinimaFormateada", Formateador.entero(cantidadMinima));
        datos.set("cantidadMaxima", cantidadMaxima);
        datos.set("cantidadMaximaFormateada", Formateador.entero(cantidadMaxima));
        datos.set("fecha", cotization.string("fecha"));
        datos.set("esPrecioRealTime", cotization.bool("esByma"));
        return RespuestaMB.exito("datos", datos);
    }

    public static RespuestaMB plazosValidos(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        Boolean habilitado = enHorarioDolarMep(contexto).get("estado").equals("FUERA_HORARIO") ? false : true;

        Set<String> plazosHabilitados = Objeto.setOf(ConfigMB.string("plazos_liquidacion_habilitados").split("_"));
        // TODO GB Usar enum PlazoLiquidacion
        Boolean defaultSeteado = false;

        if (plazosHabilitados.contains("48")) {
            respuesta.add("tipos",
                    new Objeto().set("id", "48").set("descripcion", "48hs").set("habilitado", true).set("default", defaultSeteado ? false : true));
            defaultSeteado = true;
        }

        if (plazosHabilitados.contains("24")) {
            respuesta.add("tipos",
                    new Objeto().set("id", "24").set("descripcion", "24hs").set("habilitado", true).set("default", defaultSeteado ? false : true));
            defaultSeteado = true;
        }

        if (plazosHabilitados.contains("0")) {
            respuesta.add("tipos", new Objeto().set("id", "0").set("descripcion", "Contado Inmediato (CI)")
                    .set("habilitado", habilitado).set("default", defaultSeteado ? false : true));
            defaultSeteado = true;
        }

        return respuesta;
    }

    public static RespuestaMB cuentasAsociadasComitente(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idMoneda = contexto.parametros.string("idMoneda", null);

        if (Objeto.anyEmpty(idCuentaComitente)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);

        List<String> response = RestInversiones.cuentasLiquidacionMonetaria(contexto, cuentaComitente.numero());
        if (response == null) {
            return RespuestaMB.error();
        }

        Boolean tieneCuentaAsociada = false;
        RespuestaMB respuesta = new RespuestaMB();
        for (String numero : response) {
            Cuenta cuenta = contexto.cuenta(numero);
            if (cuenta != null && (idMoneda == null || idMoneda.equals(cuenta.idMoneda()))) {
                Objeto item = new Objeto();
                item.set("id", cuenta.id());
                item.set("descripcion", cuenta.producto());
                item.set("descripcionCorta", cuenta.descripcionCorta());
                item.set("numeroFormateado", cuenta.numeroFormateado());
                item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
                item.set("ultimos4digitos", cuenta.ultimos4digitos());
                item.set("titularidad", cuenta.titularidad());
                item.set("idMoneda", cuenta.idMoneda());
                item.set("moneda", cuenta.moneda());
                item.set("simboloMoneda", cuenta.simboloMoneda());
                item.set("estado", cuenta.descripcionEstado());
                item.set("saldo", cuenta.saldo());
                item.set("saldoFormateado", cuenta.saldoFormateado());
                item.set("acuerdo", cuenta.acuerdo());
                item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
                item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
                item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
                respuesta.add("cuentas", item);
                tieneCuentaAsociada = true;
            }
        }

        if (!tieneCuentaAsociada) {
            return RespuestaMB.estado("SIN_CUENTA_ASOCIADA");
        }

        return respuesta;
    }

    public static byte[] comprobanteCuentaComitente(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");
        String idOperacion = idComprobante.split("_")[0];
        String idCuentaComitente = idComprobante.split("_")[1];

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaDesde = new Date();
        Date fechaHasta = new Date();
        try {
            fechaDesde = sdf.parse(idComprobante.split("_")[2]);
            fechaHasta = sdf2.parse(idComprobante.split("_")[3]);
        } catch (ParseException e) {
            return null;
        }

        ApiResponseMB movimientos = RestInversiones.movimientos(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (movimientos.hayError()) {
            return null;
        }
        Objeto operacion = null;
        for (Objeto item : movimientos.objetos("operaciones")) {
            String id = item.string("idOperacion");
            if (idOperacion.equals(id)) {
                operacion = item;
                break;
            }
        }
        if (operacion == null) {
            // return Respuesta.error();
            return null;
        }

        String cuil = "";
        String docString = "";
        String nombresRazon = "";
        String telefonos = "";
        String domicilio = "";
        String localidad = "";
        String codigoPostal = "";
        String direccionPostalCobis = "";
        String telefonoDireccionPostalCobis = "";
        String domicilioIDTelefonoCobis = "";

        int x = 0;
        Boolean errorEnElPrimero = false;
        Objeto titularCobis = null;
        for (Objeto item : movimientos.objetos("intervinientesCobis")) {
            ApiResponseMB clienteResponse = RestPersona.consultarClienteEspecifico(contexto, item.string("idCobis"));
            if (clienteResponse.hayError()) {
                errorEnElPrimero = true;
            } else {
                Objeto cliente = clienteResponse.objetos().get(0);
                if (x == 0) {
                    titularCobis = cliente;
                    direccionPostalCobis = item.string("direccionPostalCobis");
                    telefonoDireccionPostalCobis = item.string("telefonoDireccionPostalCobis");
                    domicilioIDTelefonoCobis = item.string("domicilioIDTelefonoCobis");
                }
                // docString = docString + personaAux.getDocumento().getTipo().getDesc() + " - "
                // + personaAux.getDocumento().getNumero() + "\n";
                docString = docString + cliente.string("numeroDocumento") + "\n";
                nombresRazon = nombresRazon + cliente.string("apellidos") + ", " + cliente.string("nombres") + "\n";
            }
            x++;
        }
        if ("".equals(docString))
            docString = " - \n";
        if ("".equals(nombresRazon))
            nombresRazon = " - \n";

        if (!errorEnElPrimero) {
            // titular
            Objeto direccion = null;
            Objeto telefono = null;

            titularCobis.string("id");
            cuil = titularCobis.string("cuit");

            ApiResponseMB responseDomicilios = RestPersona.domicilios(contexto, titularCobis.string("cuit"));
            if (!"".equals(direccionPostalCobis)) {
                for (Objeto item : responseDomicilios.objetos()) {
                    if (item.string("idCore").equals(direccionPostalCobis)) {
                        direccion = item;
                    }
                }
            }

            ApiResponseMB responseTelefonos = RestPersona.telefonos(contexto, titularCobis.string("cuit"));
            if (!"".equals(telefonoDireccionPostalCobis)) {
                for (Objeto item : responseTelefonos.objetos()) {
                    if (item.string("idCore").equals(telefonoDireccionPostalCobis) && item.string("idDireccion").equals(domicilioIDTelefonoCobis)) {
                        telefono = item;
                    }
                }
            }
            if (direccion != null) {
                domicilio = direccion.string("calle") + "-" + direccion.string("numero");
                localidad = ""; // tengo el id de ciudad, pero cómo lo mapeo?
                codigoPostal = direccion.string("idCodigoPostal");
            }
            if (telefono != null) {
                // telefonos = telefonoTitular.getDdn() != null &&
                // telefonoTitular.getDdn().equalsIgnoreCase("")
                // ?"("+telefonoTitular.getDdn()+")":""+ telefonoTitular.getPrefijo() +
                // telefonoTitular.getCodCaracteristica() + telefonoTitular.getNumero();
                telefonos = telefono.string("prefijo") + telefono.string("caracteristica") + telefono.string("numero");
                // ¿que es el ddn?
            }
        }

        ExtractoComitenteDatosPDF datos = new ExtractoComitenteDatosPDF();
        /*
         * Long valor = new Long(10005000); respuesta.set("valor", valor);
         * respuesta.set("valorFormateado", Formateador.entero(valor));
         */
        datos.setFechaPago(operacion.string("fechaPago"));
        datos.setFechaHora((operacion.string("fechaOrden")) + " " + (operacion.string("HoraOrden")));
        datos.setNombreRazon(nombresRazon);
        datos.setDocumento(docString);
        datos.setContraparte(operacion.string("contraparte"));
        datos.setCuil(cuil);
        datos.setDomicilio(domicilio);
        datos.setLocalidad(localidad);
        datos.setCodPostal(codigoPostal);
        datos.setTelefonos(telefonos);
        datos.setFechaOrden(operacion.string("fechaOrden"));
        datos.setHoraOrden(operacion.string("HoraOrden"));
        datos.setBruto(Formateador.importe(operacion.bigDecimal("bruto")));
        datos.setFechaConcertacion(operacion.string("fechaConcertacion"));
        datos.setSucursal(operacion.string("sucursal"));
        datos.setNumerosOrden(operacion.string("numeroOrdenInterno") + " - " + operacion.string("numeroOrdenMercado")); // numeros orden, van los dos asi?
        datos.setCuentaNro("");
        // datos.setCuentaNro(frm.getExtractoComitente().getCuenta().getNumero());
        // //numero de cuenta?????
        datos.setCuentaNro(cuentaComitente.numero());
        datos.setNroMinuta(operacion.string("numeroMinuta"));
        datos.setNroBoleto(operacion.string("numeroBoleto"));
        datos.setRefMercado(operacion.string("numeroRefMercado"));
        datos.setEspecie(operacion.string("especie.codigo") + " - " + operacion.string("especie.descripcion"));
        datos.setMercado(operacion.string("mercado"));
        datos.setCantidadValorNominal(operacion.string("cantidadNominal"));
        datos.setCantidadValorResidual(operacion.string("cantidadResidual"));
        datos.setPrecio(Formateador.importe(operacion.bigDecimal("precio")));
        if (!"".equals(operacion.string("derechos")))
            datos.setDerechos(Formateador.importe(operacion.bigDecimal("derechos", "")));
        else
            datos.setDerechos("");
        if (!"".equals(operacion.string("comision")))
            datos.setComision(Formateador.importe(operacion.bigDecimal("comision", "")));
        else
            datos.setComision("");
        if (!"".equals(operacion.string("totalML")))
            datos.setTotalML(Formateador.importe(operacion.bigDecimal("totalML", "")));
        else
            datos.setTotalML("");
        if (!"".equals(operacion.string("totalME")))
            datos.setTotalME(Formateador.importe(operacion.bigDecimal("totalME", "")));
        else
            datos.setTotalME("");
        datos.setTotalLetrasML(operacion.string("totalLetrasML"));
        datos.setTotalLetrasME(operacion.string("totalLetrasME"));
        if (!"".equals(operacion.string("ivaRI")))
            datos.setIvaRI(Formateador.importe(operacion.bigDecimal("ivaRI", "")));
        else
            datos.setIvaRI("");
        if (!"".equals(operacion.string("ivaRNI")))
            datos.setIvaRNI(Formateador.importe(operacion.bigDecimal("ivaRNI", "")));
        else
            datos.setIvaRNI("");
        if (!"".equals(operacion.string("aranceles")))
            datos.setAranceles(Formateador.importe(operacion.bigDecimal("aranceles", "")));
        else
            datos.setAranceles("");
        datos.setCuentaLiquidacionML(operacion.string("cuentaLiquidacionML"));
        datos.setCuentaLiquidacionME(operacion.string("cuentaLiquidacionME"));
        datos.setFechaVencimiento(operacion.string("fechaVencimiento"));
        datos.setPlazo(operacion.string("plazo") + " " + (operacion.string("unidadPlazo")));
        datos.setDividendoEnAcciones(operacion.string("dividendoEnAcciones"));
        datos.setDividendoEnEfectivo(operacion.string("dividendoEnEfectivo"));
        datos.setMoneda(operacion.string("moneda"));

        String tipoComprobante = "";
        if ("Compra de Especies".equalsIgnoreCase(operacion.string("tipo"))) {
            tipoComprobante = "Comprobante de Boleto de compra";
            datos.setTipoBoleto("COMPRA");
            datos.setTipoAccion("COMPRADO");
            if ("98".equalsIgnoreCase(operacion.string("mercado"))) {

                if (operacion.bigDecimal("totalME", "") != null && operacion.bigDecimal("totalME", "").compareTo(new BigDecimal("0")) == 1) {
                    if (!"".equals(operacion.string("totalME")))
                        datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalME", "")));
                    else
                        datos.setTotalMonto("");
                    datos.setTotalLetra(operacion.string("totalLetrasME"));
                    if (!"".equals(operacion.string("amortizacionME")))
                        datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionME", "")));
                    else
                        datos.setAmortizacionMonto("");

                    if (!"".equals(operacion.string("rentaME")))
                        datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaME", "")));
                    else
                        datos.setRentaMonto("");
                    datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionME"));
                    datos.setMoneda("DÓLARES, DÓLARES");
                } else if (operacion.bigDecimal("totalML", "") != null && operacion.bigDecimal("totalML", "").compareTo(new BigDecimal("0")) == 1) {
                    if (!"".equals(operacion.string("totalML")))
                        datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalML", "")));
                    else
                        datos.setTotalMonto("");
                    datos.setTotalLetra(operacion.string("totalLetrasML"));
                    if (!"".equals(operacion.string("amortizacionML")))
                        datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionML", "")));
                    else
                        datos.setAmortizacionMonto("");
                    if (!"".equals(operacion.string("rentaML")))
                        datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaML", "")));
                    else
                        datos.setRentaMonto("");
                    datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionML"));
                    datos.setMoneda("PESOS, PESOS");
                } else {
                    datos.setTotalMonto("");
                    datos.setTotalLetra("");
                    datos.setAmortizacionMonto("");
                    datos.setRentaMonto("");
                    datos.setCuentaLiquidacion("");
                    datos.setMoneda("");
                }
            }
        }
        if ("Venta de Especie".equalsIgnoreCase(operacion.string("tipo"))) {
            tipoComprobante = "Comprobante de Boleto de venta";
            datos.setTipoBoleto("VENTA");
            datos.setTipoAccion("VENDIDO");
        }
        if ("Liquidacion de Cupon".equalsIgnoreCase(operacion.string("tipo"))) {
            tipoComprobante = "Liquidación de renta y/o amortización";
            datos.setAmortizacion(operacion.string("amortizacion"));
            datos.setRenta(operacion.string("renta"));
            datos.setCantidadResidualActual(operacion.string("cantidadResidualActual"));
            if (operacion.bigDecimal("totalME", "") != null && operacion.bigDecimal("totalME", "").compareTo(new BigDecimal("0")) == 1) {
                datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalME", "")));
                datos.setTotalLetra(operacion.string("totalLetrasME"));
                datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionME", "")));
                datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaME", "")));
                datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionME"));
            } else if (operacion.bigDecimal("totalML", "") != null && operacion.bigDecimal("totalML", "").compareTo(new BigDecimal("0")) == 1) {
                datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalML", "")));
                datos.setTotalLetra(operacion.string("totalLetrasML"));
                datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionML", "")));
                datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaML", "")));
                datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionML"));

            } else {
                datos.setTotalMonto("");
                datos.setTotalLetra("");
                datos.setAmortizacionMonto("");
                datos.setRentaMonto("");
                datos.setCuentaLiquidacion("");
            }
        }
        if (operacion.string("tipo").toUpperCase().contains("DIVIDENDO")) {
            tipoComprobante = "Liquidación de dividendos";
        }

        Map<String, String> comprobante = new HashMap<>();

        comprobante.put("TIPO_COMPROBANTE", tipoComprobante);

        comprobante.put("FECHA_HORA", datos.getFechaHora());
        comprobante.put("FECHA_ORDEN", datos.getFechaOrden());
        comprobante.put("HORA_ORDEN", datos.getHoraOrden());
        comprobante.put("NUMERO_ORDEN", datos.getNumerosOrden());
        comprobante.put("NUMERO_MINUTA", datos.getNroMinuta());
        comprobante.put("NUMERO_BOLETO", datos.getNroBoleto());
        comprobante.put("SUCURSAL", datos.getSucursal());
        comprobante.put("CONTRAPARTE", datos.getContraparte());
        comprobante.put("CUENTA_NRO", datos.getCuentaNro());
        comprobante.put("REFERENCIA_MERCAD", datos.getRefMercado());
        comprobante.put("APELLIDO_NOMBRE", datos.getNombreRazon());
        comprobante.put("DATO_CUIL", datos.getCuil());
        comprobante.put("DOMICILIO", datos.getDomicilio());
        comprobante.put("LOCALIDAD", datos.getLocalidad());
        comprobante.put("CODIGO_POSTAL", datos.getCodPostal());
        comprobante.put("TELEFONO", datos.getTelefonos());
        comprobante.put("DOCUMENTO", datos.getDocumento());
        comprobante.put("ESPECIE", datos.getEspecie());
        comprobante.put("VALOR_NOMINAL", datos.getCantidadValorNominal());
        comprobante.put("VALOR_RESIDUAL", datos.getCantidadValorResidual());
        comprobante.put("MERCADO", datos.getMercado());
        comprobante.put("PRECIO", datos.getPrecio());
        comprobante.put("SUBTOTAL", datos.getBruto());
        comprobante.put("ARANCELES", datos.getAranceles());
        comprobante.put("IMPUESTOS", datos.getDerechos());
        comprobante.put("IVA_RESPONSABLE_INSCRIPTO", datos.getIvaRI());
        comprobante.put("COMISIONES", datos.getComision());
        comprobante.put("TOTAL_MONEDA_EXTRANJERA", datos.getTotalME());
        comprobante.put("TOTAL_LETRAS_MONEDA_EXTRANJERA", datos.getTotalLetrasME());
        comprobante.put("TOTAL_LETRAS_PESOS", datos.getTotalLetrasML());
        comprobante.put("MONTO_TOTAL", datos.getTotalML());
        comprobante.put("CUENTA_PESOS", datos.getCuentaLiquidacionML());
        comprobante.put("CUENTA_MONEDA_EXTRANJERA", datos.getCuentaLiquidacionME());

        comprobante.put("FECHAPAGO", datos.getFechaPago());
        comprobante.put("FECHA_CORTE", datos.getFechaConcertacion());
        comprobante.put("FECHA_LIQUIDACION", datos.getFechaVencimiento());
        comprobante.put("AMORTIZACION_MONTO", datos.getAmortizacionMonto());
        comprobante.put("AMORTIZACION_PORCENTAJE", datos.getAmortizacion());
        comprobante.put("RENTA_MONTO", datos.getRentaMonto());
        comprobante.put("RENTA_PORCENTAJE", datos.getRenta());
        comprobante.put("TENENCIA_RESIDUAL_ACTUAL", datos.getCantidadResidualActual());
        comprobante.put("TENENCIA_RESIDUAL_ANTERIOR", datos.getCantidadValorResidual());
        comprobante.put("TENENCIA_NOMINAL", datos.getCantidadValorNominal());
        comprobante.put("IVA_NO_INSCRIPTO", datos.getIvaRNI());
        comprobante.put("FORMA_DE_PAGO", datos.getCuentaLiquidacion());

        comprobante.put("FECHA_MINUTA", datos.getFechaConcertacion());
        comprobante.put("PORC_DIVIDENDO_EFECTIVO", datos.getDividendoEnEfectivo());
        comprobante.put("PORC_DIVIDENDO_ACCIONES", datos.getDividendoEnAcciones());
        comprobante.put("DATO_MONEDA", datos.getMoneda());

        String idComprobanteImpresion = "";
        if ("Compra de Especies".equalsIgnoreCase(operacion.string("tipo"))) {
            idComprobanteImpresion = "licitaciones-compra-venta-especie_" + idOperacion;
        }
        if ("Venta de Especie".equalsIgnoreCase(operacion.string("tipo"))) {
            idComprobanteImpresion = "licitaciones-compra-venta-especie_" + idOperacion;
        }
        if ("Liquidacion de Cupon".equalsIgnoreCase(operacion.string("tipo"))) {
            idComprobanteImpresion = "licitaciones-liquidacion-cupon_" + idOperacion;
        }
        if (operacion.string("tipo").toUpperCase().contains("DIVIDENDO")) {
            idComprobanteImpresion = "licitaciones-dividendo_" + idOperacion;
        }

        contexto.sesion().setComprobante(idComprobanteImpresion, comprobante);
        String template = idComprobanteImpresion.split("_")[0];
        Map<String, String> parametros = contexto.sesion().comprobante(idComprobanteImpresion);
        contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        return Pdf.generar(template, parametros);

    }

    public static byte[] comprobanteSeguimientoOperaciones(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");
        String ordenSecuencial = idComprobante.split("_")[0];
        String idCuentaComitente = idComprobante.split("_")[1];
        String idNumero = idComprobante.split("_")[2];

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaDesde = new Date();
        Date fechaHasta = new Date();
        try {
            fechaDesde = sdf.parse(idComprobante.split("_")[3]);
            fechaHasta = sdf.parse(idComprobante.split("_")[4]);
        } catch (ParseException e) {
            return null;
        }

        ApiResponseMB seguimientoOperaciones = RestInversiones.seguimientoOperaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
        if (seguimientoOperaciones.hayError()) {
            return null;
        }
        Objeto operacion = null;
        for (Objeto item : seguimientoOperaciones.objetos("ordenesOrdenadas")) {
            if (ordenSecuencial.equals(item.string("ordenSecuencial")) && idNumero.equals(item.string("numero"))) {
                operacion = item;
                break;
            }
        }

        if (operacion == null) {
            return null;
        }

        Objeto datosTituloValor = RestInversiones.tituloValor(contexto, operacion.string("tipoEspecie"));

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", operacion.string("numero")); // ...
        comprobante.put("FECHA_HORA", operacion.string("fecha"));
        comprobante.put("ESPECIE", operacion.string("tipoEspecie") + " - " + datosTituloValor.string("descripcion"));
        String moneda = "PESOS".equals(operacion.string("moneda")) ? "$" : "USD".equals(operacion.string("moneda")) ? "USD" : "";
        comprobante.put("IMPORTE", moneda + " " + Formateador.importe(operacion.bigDecimal("monto")));
        String tipoOperacion = "";
        if (operacion.string("tipo").toLowerCase().contains("venta"))
            tipoOperacion = "Venta";
        if (operacion.string("tipo").toLowerCase().contains("compra"))
            tipoOperacion = "Compra";
        comprobante.put("TIPO_OPERACION", tipoOperacion);
        comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion"));
        comprobante.put("PRECIO", moneda + " " + Formateador.importeCantDecimales(operacion.bigDecimal("precioLimite"), 4));
        comprobante.put("VALOR_NOMINAL", operacion.string("cantidadNominal"));
        comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
        comprobante.put("CUENTA", operacion.string("cuentaLiquidacionMonetaria"));
        comprobante.put("PLAZO", operacion.string("plazo") + " hs");
        String comisiones = Formateador.importe(operacion.bigDecimal("comisiones"));
        if (comisiones == null || "".equals(comisiones)) {
            comisiones = Formateador.importe(new BigDecimal(0));
        }
        comprobante.put("COMISION", "$" + " " + comisiones);
        comprobante.put("VIGENCIA", operacion.string("vigencia"));

        String idComprobanteImpresion = "";
        if ("Venta".equals(tipoOperacion)) {
            idComprobanteImpresion = "titulo-valor-venta_" + operacion.string("orderId");
        } else {
            idComprobanteImpresion = "titulo-valor-compra_" + operacion.string("orderId");
        }
        contexto.sesion().setComprobante(idComprobanteImpresion, comprobante);
        String template = idComprobanteImpresion.split("_")[0];
        Map<String, String> parametros = contexto.sesion().comprobante(idComprobanteImpresion);
        contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        return Pdf.generar(template, parametros);
    }

    public static RespuestaMB indicesBursatilesDelay(ContextoMB contexto) {
        ApiResponseMB response = RestInversiones.indicesBursatilesDelay(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto indices = new Objeto();
        for (Objeto item : response.objetos("indices")) {
            Objeto indice = new Objeto();
            indice.set("nombre", item.string("nombre"));
            indice.set("variacion", item.bigDecimal("variacion"));
            indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("variacion")));
            indice.set("precioMaximo", item.bigDecimal("maximoValor"));
            indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("maximoValor")));
            indice.set("precioMinimo", item.bigDecimal("minimoValor"));
            indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("minimoValor")));
            indice.set("precioApertura", item.bigDecimal("apertura"));
            indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("apertura")));
            indice.set("cierreAnterior", item.bigDecimal("cierre"));
            indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierre")));
            indice.set("ultimoCierre", item.bigDecimal("ultimo"));
            indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("ultimo")));
            indices.add(indice);
        }

        return RespuestaMB.exito("indices", indices);
    }

    public static RespuestaMB indicesSectorialesDelay(ContextoMB contexto) {

        ApiResponseMB response = RestInversiones.indicesSectorialesDelay(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto indices = new Objeto();
        for (Objeto item : response.objetos("indices")) {
            Objeto indice = new Objeto();
            indice.set("nombre", item.string("nombre"));
            indice.set("variacion", item.bigDecimal("variacion"));
            indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("variacion")));
            indice.set("precioMaximo", item.bigDecimal("maximoValor"));
            indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("maximoValor")));
            indice.set("precioMinimo", item.bigDecimal("minimoValor"));
            indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("minimoValor")));
            indice.set("precioApertura", item.bigDecimal("apertura"));
            indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("apertura")));
            indice.set("cierreAnterior", item.bigDecimal("cierreAnterior"));
            indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierreAnterior")));
            indice.set("ultimoCierre", item.bigDecimal("cierreDia"));
            indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("cierreDia")));
            indices.add(indice);
        }

        return RespuestaMB.exito("indices", indices);
    }

    public static RespuestaMB panelesEspecies(ContextoMB contexto) {
        ApiResponseMB response = RestInversiones.panelesEspecies(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto paneles = new Objeto();
        for (Objeto item : response.objetos()) {
            Objeto panel = new Objeto();
            if (item.string("idPanel").equals("6") || item.string("idPanel").equals("8") || item.string("idPanel").equals("10")) {
                continue;
            }
            panel.set("idPanel", item.string("idPanel"));
            panel.set("descripcion", item.string("descripcion"));
            paneles.add(panel);
        }

        return RespuestaMB.exito("paneles", paneles);
    }

    public static RespuestaMB panelesCotizacionesDelay(ContextoMB contexto) {
        String idPanel = contexto.parametros.string("idPanel");

        ApiResponseMB response = RestInversiones.panelesCotizacionesDelay(contexto, idPanel);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cotizaciones = new Objeto();
        for (Objeto item : response.objetos("cotizaciones")) {
            Objeto cotizacion = new Objeto();
            cotizacion.set("cantidadCompra", item.integer("cantidadNominalCompra"));
            cotizacion.set("precioCompra", item.bigDecimal("precioCompra"));
            cotizacion.set("precioCompraFormateado", Formateador.importe(item.bigDecimal("precioCompra")));
            cotizacion.set("cantidadVenta", item.integer("cantidadNominalVenta"));
            cotizacion.set("precioVenta", item.bigDecimal("precioVenta"));
            cotizacion.set("precioVentaFormateado", Formateador.importe(item.bigDecimal("precioVenta")));
            cotizacion.set("apertura", item.bigDecimal("apertura"));
            cotizacion.set("aperturaFormateada", Formateador.importe(item.bigDecimal("apertura")));
            cotizacion.set("maximo", item.bigDecimal("maximo"));
            cotizacion.set("maximoFormateado", Formateador.importe(item.bigDecimal("maximo")));
            cotizacion.set("minimo", item.bigDecimal("minimo"));
            cotizacion.set("minimoFormateado", Formateador.importe(item.bigDecimal("minimo")));
            cotizacion.set("cierreAnterior", item.bigDecimal("cierreAnterior"));
            cotizacion.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierreAnterior")));
            cotizacion.set("volumenNominal", item.bigDecimal("volumenNominal"));
            cotizacion.set("montoOperadoPesos", item.bigDecimal("montoOperadoPesos"));
            cotizacion.set("montoOperadoPesosFormateado", Formateador.importe(item.bigDecimal("montoOperadoPesos")));
            cotizacion.set("cantidadOperada", item.bigDecimal("cantidadOperada"));
            cotizaciones.add(cotizacion);
        }
        return RespuestaMB.exito("cotizaciones", cotizaciones);
    }

    public static RespuestaMB caucionesDelay(ContextoMB contexto) {

        ApiResponseMB response = RestInversiones.caucionesDelay(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cauciones = new Objeto();
        for (Objeto item : response.objetos("cauciones")) {
            Objeto caucion = new Objeto();
            caucion.set("fecha", item.string("fecha"));
            caucion.set("montoContado", item.bigDecimal("montoContado"));
            caucion.set("montoContadoFormateado", Formateador.importe(item.bigDecimal("montoContado")));
            caucion.set("montoFuturo", item.bigDecimal("montoFuturo"));
            caucion.set("montoFuturoFormateado", Formateador.importe(item.bigDecimal("montoFuturo")));
            caucion.set("tasaPromedio", item.bigDecimal("tasaPromedio"));
            caucion.set("tasaPromedioFormateado", Formateador.importe(item.bigDecimal("tasaPromedio")));
            caucion.set("moneda", item.string("tipoLiquidacion").toUpperCase().equals("PESOS") ? 80 : 2);
            cauciones.add(caucion);
        }

        return RespuestaMB.exito("cauciones", cauciones);
    }

    public static RespuestaMB indicesBursatilesRealTime(ContextoMB contexto) {
        Boolean sectorial = contexto.parametros.bool("sectorial");
        Boolean soloOperados = contexto.parametros.bool("soloOperados");

        Objeto indices = new Objeto();

        ApiResponseMB responseIndices = null;
        if (sectorial) {
            responseIndices = RestInversiones.indicesSectoriales(contexto);
        } else {
            responseIndices = RestInversiones.indicesBursatiles(contexto);
        }
        if (responseIndices.hayError()) {
            return RespuestaMB.error();
        }

        ApiResponseMB response = RestInversiones.indicesRealTime(contexto, "", "-1", "");
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        for (Objeto item : response.objetos()) {
            if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
                continue;
            }

            boolean encontroIndice = false;
            String descripcionIndice = "";
            for (Objeto indiceBursatil : responseIndices.objetos()) {
                if (item.string("symbol").equals(indiceBursatil.string("codigo"))) {
                    encontroIndice = true;
                    descripcionIndice = indiceBursatil.string("indice");
                    break;
                }
            }
            if (!encontroIndice)
                continue;

            Objeto indice = new Objeto();
            indice.set("nombre", item.string("symbol"));
            indice.set("indice", descripcionIndice);
            indice.set("idIntradiaria", item.string("idIntradiaria"));
            indice.set("variacion", item.bigDecimal("imbalance"));
            indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
            indice.set("precioMaximo", item.bigDecimal("tradingSessionHighPrice"));
            indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
            indice.set("precioMinimo", item.bigDecimal("tradingSessionLowPrice"));
            indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
            indice.set("precioApertura", item.bigDecimal("openingPrice"));
            indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("openingPrice")));
            indice.set("cierreAnterior", item.bigDecimal("previousClose"));
            indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
            indice.set("ultimoCierre", item.bigDecimal("previousClose"));
            indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("previousClose")));
            indice.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));

            indices.add(indice);
        }

        return RespuestaMB.exito("indices", indices);

    }

    public static RespuestaMB indicesRealTime(ContextoMB contexto) {
        Boolean soloOperados = contexto.parametros.bool("soloOperados");

        Objeto indices = new Objeto();

        ApiResponseMB responseIndicesSectoriales = null;
        ApiResponseMB responseIndicesBursatiles = null;
        responseIndicesSectoriales = RestInversiones.indicesSectoriales(contexto);
        responseIndicesBursatiles = RestInversiones.indicesBursatiles(contexto);

        if (responseIndicesBursatiles.hayError()) {
            return RespuestaMB.error();
        }

        ApiResponseMB response = RestInversiones.indicesRealTime(contexto, "", "-1", "");
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        for (Objeto item : response.objetos()) {
            if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
                continue;
            }
            Objeto indice = new Objeto();
            boolean encontroIndice = false;
            String descripcionIndice = "";

            for (Objeto indiceSectorial : responseIndicesSectoriales.objetos()) {
                if (item.string("symbol").equals(indiceSectorial.string("codigo"))) {
                    encontroIndice = true;
                    descripcionIndice = indiceSectorial.string("indice");
                    indice.set("tipo", "sectorial");
                    break;
                }
            }
            for (Objeto indiceBursatil : responseIndicesBursatiles.objetos()) {
                if (item.string("symbol").equals(indiceBursatil.string("codigo"))) {
                    encontroIndice = true;
                    descripcionIndice = indiceBursatil.string("indice");
                    indice.set("tipo", "bursatil");
                    break;
                }
            }

            if (!encontroIndice)
                continue;

            indice.set("nombre", item.string("symbol"));
            indice.set("indice", descripcionIndice);
            indice.set("idIntradiaria", item.string("idIntradiaria"));
            indice.set("variacion", item.bigDecimal("imbalance"));
            indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
            indice.set("precioMaximo", item.bigDecimal("tradingSessionHighPrice"));
            indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
            indice.set("precioMinimo", item.bigDecimal("tradingSessionLowPrice"));
            indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
            indice.set("precioApertura", item.bigDecimal("openingPrice"));
            indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("openingPrice")));
            indice.set("cierreAnterior", item.bigDecimal("previousClose"));
            indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
            indice.set("ultimoCierre", item.bigDecimal("previousClose"));
            indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("previousClose")));
            indice.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));

            indices.add(indice);
        }

        return RespuestaMB.exito("indices", indices);

    }

    public static RespuestaMB indicesSectorialesRealTime(ContextoMB contexto) {
        /*
         * Boolean sectorial = contexto.parametros.bool("sectorial"); Boolean
         * soloOperados = contexto.parametros.bool("soloOperados");
         */
        return indicesBursatilesRealTime(contexto);

    }

    public static RespuestaMB panelesCotizacionesRealTime(ContextoMB contexto) {
        String idPanel = contexto.parametros.string("idPanel");
        String idPlazo = contexto.parametros.string("idPlazo");
        Boolean soloOperados = contexto.parametros.bool("soloOperados");
        String idMoneda = contexto.parametros.string("idMoneda");

        if (idMoneda.equals("80")) {
            idMoneda = "1";
        }

        ApiResponseMB response = RestInversiones.indicesRealTime(contexto, "", idPanel, idPlazo);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        ApiResponseMB responseOferta = null;

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mejora_intradiarias_byma")) {
            try {
                responseOferta = RestInversiones.intradiariasOferta(contexto, "", idPanel);
                if (responseOferta.hayError()) {
                    // return Respuesta.error();
                }
            } catch (Exception e) {

            }
        }

        ApiResponseMB responseEspecies = RestInversiones.especiesPorPanel(contexto, idPanel);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cotizaciones = new Objeto();
        for (Objeto item : response.objetos()) {
            Objeto cotizacion = new Objeto();

            if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
                continue;
            }

            String idMonedaAux = "";
            for (Objeto itemEspecie : responseEspecies.objetos()) {
                if (itemEspecie.string("simbolo").equals(item.string("symbol")))
                    idMonedaAux = itemEspecie.string("idMoneda");
            }
            if (!idMoneda.equals("") && !idMonedaAux.equals(idMoneda)) {
                continue;
            }

            cotizacion.set("idIntradiaria", item.string("idIntradiaria"));
            cotizacion.set("codigo", item.string("symbol"));
            cotizacion.set("apertura", item.bigDecimal("openingPrice"));
            cotizacion.set("aperturaFormateada", Formateador.importe(item.bigDecimal("openingPrice")));
            cotizacion.set("maximo", item.bigDecimal("tradingSessionHighPrice"));
            cotizacion.set("maximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
            cotizacion.set("minimo", item.bigDecimal("tradingSessionLowPrice"));
            cotizacion.set("minimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
            cotizacion.set("cierreAnterior", item.bigDecimal("previousClose"));
            cotizacion.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
            cotizacion.set("volumenNominal", item.bigDecimal("tradeVolumeQty"));
            cotizacion.set("ultimoOperado", item.bigDecimal("trade"));

            cotizacion.set("montoOperadoPesos", item.bigDecimal("tradeVolume"));
            cotizacion.set("montoOperadoPesosFormateado", Formateador.importe(item.bigDecimal("tradeVolume")));
            cotizacion.set("cantidadOperada", item.bigDecimal("tradeQty"));
            cotizacion.set("cantidadOperadaFormateada", Formateador.importe(item.bigDecimal("tradeQty")));

            cotizacion.set("variacion", item.bigDecimal("imbalance"));
            cotizacion.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));

            cotizacion.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));
            cotizacion.set("idMoneda", idMonedaAux.equals("1") ? "80" : idMonedaAux.equals("2") ? "2" : "");

            // Conseguir precio de compra y de venta
            if (responseOferta != null) {
                Integer cantidadCompra = null;
                BigDecimal precioCompra = null;
                Integer cantidadVenta = null;
                BigDecimal precioVenta = null;

                // Tengan en cuenta que se debe tomar el precio mas alto de BIDs y el mas bajo
                // de OFFERs,
                // ya que el recurso devuelve 5 registros para cada tipo.
                for (Objeto itemOferta : responseOferta.objetos()) {
                    if (item.string("symbol").equals(itemOferta.string("symbol"))) {
                        if (itemOferta.string("tipo").equals("Bid") && (precioCompra == null || precioCompra.compareTo(itemOferta.bigDecimal("price")) < 0)) {
                            cantidadCompra = itemOferta.integer("quantity");
                            precioCompra = itemOferta.bigDecimal("price");
                        }
                        if (itemOferta.string("tipo").equals("Offer") && (precioVenta == null || precioVenta.compareTo(itemOferta.bigDecimal("price")) > 0)) {

                            cantidadVenta = itemOferta.integer("quantity");
                            precioVenta = itemOferta.bigDecimal("price");
                        }
                    }
                }

                if (precioCompra != null && precioCompra.equals(new BigDecimal(0))) {
                    precioCompra = null;
                }

                if (precioVenta != null && precioVenta.equals(new BigDecimal(0))) {
                    precioVenta = null;
                }

                cotizacion.set("cantidadCompra", cantidadCompra);
                cotizacion.set("precioCompra", precioCompra);
                cotizacion.set("precioCompraFormateado", Formateador.importe(precioCompra));

                cotizacion.set("cantidadVenta", cantidadVenta);
                cotizacion.set("precioVenta", precioVenta);
                cotizacion.set("precioVentaFormateado", Formateador.importe(precioVenta));
            }

            cotizaciones.add(cotizacion);
        }
        return RespuestaMB.exito("cotizaciones", cotizaciones);

    }

    public static RespuestaMB panelesCotizacionesRealTimeEspecie(ContextoMB contexto) {
        String idIntradiaria = contexto.parametros.string("idIntradiaria");
        ApiResponseMB response = RestInversiones.intradiariasOferta(contexto, idIntradiaria, "");
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Integer cantidadCompra = null;
        BigDecimal precioCompra = null;
        Integer cantidadVenta = null;
        BigDecimal precioVenta = null;

        // Tengan en cuenta que se debe tomar el precio mas alta de BIDs y el mas bajo
        // de OFFERs,
        // ya que el recurso devuelve 5 registros para cada tipo.
        for (Objeto item : response.objetos()) {
            if (item.string("tipo").equals("Bid") && (precioCompra == null || precioCompra.compareTo(item.bigDecimal("price")) < 0)) {
                cantidadCompra = item.integer("quantity");
                precioCompra = item.bigDecimal("price");
            }
            if (item.string("tipo").equals("Offer") && (precioVenta == null || precioVenta.compareTo(item.bigDecimal("price")) > 0)) {

                cantidadVenta = item.integer("quantity");
                precioVenta = item.bigDecimal("price");
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto cotizaciones = new Objeto();

        if (precioCompra != null && precioCompra.equals(new BigDecimal(0))) {
            precioCompra = null;
        }

        if (precioVenta != null && precioVenta.equals(new BigDecimal(0))) {
            precioVenta = null;
        }

        cotizaciones.set("cantidadCompra", cantidadCompra);
        cotizaciones.set("precioCompra", precioCompra);
        cotizaciones.set("precioCompraFormateado", Formateador.importe(precioCompra));

        cotizaciones.set("cantidadVenta", cantidadVenta);
        cotizaciones.set("precioVenta", precioVenta);
        cotizaciones.set("precioVentaFormateado", Formateador.importe(precioVenta));

        respuesta.set("cotizacion", cotizaciones);

        return respuesta;
    }

    public static RespuestaMB profundidadMercado(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Objeto profundidadMercado = buscarPuntasMercado(contexto
                , contexto.parametros.string("idEspecie")
                , PlazoLiquidacion.codigo(contexto.parametros.string("plazo")));
        respuesta.set("profundidadMercado", profundidadMercado);

        return respuesta;
    }

    private static Objeto buscarPuntasMercado(ContextoMB contexto, String idEspecie, PlazoLiquidacion plazo) {
        String idEspecieAux = idEspecie.split("_")[0];

        ApiResponseMB responseEspecie = RestInversiones.indicesRealTime(contexto, idEspecieAux, "",  String.valueOf(plazo.idVencimiento()));
        if (responseEspecie.hayError()) {
            return RespuestaMB.error();
        }

        String idIntradiaria = "";
        Objeto profundidadMercado = new Objeto();

        for (Objeto item : responseEspecie.objetos()) {
            idIntradiaria = item.string("idIntradiaria");

            BigDecimal precio = item.bigDecimal("trade").signum() != 0 ? item.bigDecimal("trade") : item.bigDecimal("previousClose");

            profundidadMercado.set("ultimoPrecio", precio);
            profundidadMercado.set("ultimoPrecioFormateado", Formateador.importe(precio));
            profundidadMercado.set("fecha", item.date("fechaModificacion", "yyyy-MM-dd hh:mm", "dd/MM/yyyy hh:mm"));
            profundidadMercado.set("variacion", item.bigDecimal("imbalance"));
            profundidadMercado.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
            profundidadMercado.set("variacionAbsoluta", item.bigDecimal("trade", "0").subtract(item.bigDecimal("closingPrice", "0")));
            profundidadMercado.set("variacionAbsolutaFormateada", Formateador.importe(item.bigDecimal("trade", "0").subtract(item.bigDecimal("closingPrice", "0"))));

            profundidadMercado.set("precio", item.bigDecimal("trade"));
            profundidadMercado.set("precioFormateado", Formateador.importe(item.bigDecimal("trade")));
            profundidadMercado.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));
        }

        ApiResponseMB response = RestInversiones.intradiariasProfundidad(contexto, idIntradiaria, "");
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        List<Objeto> itemsCompra = new ArrayList<>();
        List<Objeto> itemsVenta = new ArrayList<>();
        for (Objeto item : response.objetos()) {
            Objeto dato = new Objeto();
            dato.set("cantidad", item.integer("quantity"));
            dato.set("precio", item.bigDecimal("price"));
            dato.set("precioFormateado", Formateador.importe(item.bigDecimal("price")));
            if (item.string("tipo").equals("Bid")) {
                itemsCompra.add(dato);
            }
            if (item.string("tipo").equals("Offer")) {
                itemsVenta.add(dato);
            }
        }

        itemsCompra.sort(Comparator.comparing((Objeto o) -> o.bigDecimal("precio")).reversed());
        itemsVenta.sort(Comparator.comparing((Objeto o) -> o.bigDecimal("precio")));

        List<Objeto> puntas = new ArrayList<>();

        for (int x = 0; x < 5; x++) {
            boolean existeCompra = false;
            boolean existeVenta = false;
            Objeto dato = new Objeto();
            if (itemsCompra.size() >= x + 1) {
                dato.set("cantidadCompra", itemsCompra.get(x).integer("cantidad"));
                dato.set("precioCompra", itemsCompra.get(x).bigDecimal("precio"));
                dato.set("precioCompraFormateado", itemsCompra.get(x).string("precioFormateado"));
                existeCompra = true;
            }
            if (itemsVenta.size() >= x + 1) {
                dato.set("cantidadVenta", itemsVenta.get(x).integer("cantidad"));
                dato.set("precioVenta", itemsVenta.get(x).bigDecimal("precio"));
                dato.set("precioVentaFormateado", itemsVenta.get(x).string("precioFormateado"));
                existeVenta = true;
            }
            if (existeCompra || existeVenta) {
                puntas.add(dato);
            }
        }

        puntas.forEach(p -> profundidadMercado.add("puntas", p));

        return profundidadMercado;
    }

    public static RespuestaMB caucionesRealTime(ContextoMB contexto) {
        String moneda = contexto.parametros.string("moneda");

        ApiResponseMB response = RestInversiones.indicesRealTime(contexto, "", "98", null);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cauciones = new Objeto();
        for (Objeto item : response.objetos()) {
            Objeto caucion = new Objeto();
            String monedaCaucion = item.string("symbol").substring(item.string("symbol").length() - 1).equals("M") ? "2" : "80";
            if (!moneda.equals("") && !moneda.equals(monedaCaucion)) {
                continue;
            }
            String fechaVencimiento = item.string("symbol").substring(1, 9);
            item.set("fechaVencimiento", fechaVencimiento);
            Date fechaVencimientoDate = item.date("fechaVencimiento", "yyyyMMdd");
            caucion.set("fecha", item.date("fechaVencimiento", "yyyyMMdd", "dd/MM/yyyy"));
            caucion.set("montoContado", item.bigDecimal("tradeVolumeQty"));
            caucion.set("montoContadoFormateado", Formateador.importe(item.bigDecimal("tradeVolumeQty")));
            caucion.set("montoFuturo", item.bigDecimal("tradeVolume"));
            caucion.set("montoFuturoFormateado", Formateador.importe(item.bigDecimal("tradeVolume")));
            caucion.set("tasaPromedio", item.bigDecimal("trade"));
            caucion.set("tasaPromedioFormateado", Formateador.importe(item.bigDecimal("trade")));
            Integer cantidadDias = Fecha.cantidadDias(new Date(), fechaVencimientoDate);
            caucion.set("plazo", cantidadDias);
            caucion.set("moneda", monedaCaucion);

            cauciones.add(caucion);
        }

        return RespuestaMB.exito("cauciones", cauciones);
    }

    public static RespuestaMB inversionesPlazos(ContextoMB contexto) {
        ApiResponseMB response = RestInversiones.vencimientosEspecies(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto vencimientos = new Objeto();
        PlazoLiquidacion idVencimientoDefault;
        try {
            idVencimientoDefault = PlazoLiquidacion.plazoDefault();
            for (Objeto item : response.objetos()) {
                Objeto vencimiento = new Objeto();
                PlazoLiquidacion plazo = PlazoLiquidacion.vencimiento(item.integer("idVencimiento"));
                if (plazo.isHabilitado()) {
                    vencimiento.set("idVencimiento", item.string("idVencimiento"));
                    vencimiento.set("descripcion", item.string("descripcion"));
                    if (plazo.equals(idVencimientoDefault)) {
                        vencimiento.set("default", true);
                    }
                    vencimientos.add(vencimiento);
                }
            }

            return RespuestaMB.exito("vencimientos", vencimientos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RespuestaMB.estado("ERROR_PLAZO");
    }

    public static RespuestaMB cotizacionDolaMep(ContextoMB contexto) {
        // LLAMA A ESTE NUEVO METODO
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_fix_cotizacionmep")) {
            String version = contexto.parametros.string("version");
            if (version == null || version.isBlank() || version.equals("2")) {
                return cotizacionDolaMepV2(contexto);
            }
            return cotizacionDolaMepV3(contexto);
        }

        BigDecimal cotizacionDolarMep = null;
        String[] codigosProducto = new String[] { "AL30", "AL30D" };

        BigDecimal[] cotizacionAL30 = buscarCotizacion(contexto, codigosProducto[0]);
        BigDecimal[] cotizacionAL30D = buscarCotizacion(contexto, codigosProducto[1]);

        if (cotizacionAL30[0].signum() != 0 && cotizacionAL30D[0].signum() != 0) {
            cotizacionDolarMep = cotizacionAL30[0].divide(cotizacionAL30D[0], 2, RoundingMode.CEILING);
        } else if (cotizacionAL30[1].signum() != 0 && cotizacionAL30D[1].signum() != 0) {
            cotizacionDolarMep = cotizacionAL30[1].divide(cotizacionAL30D[1], 2, RoundingMode.CEILING);
        }

        Objeto objeto = new Objeto();
        objeto.set("cotizacion", cotizacionDolarMep);
        objeto.set("cotizacionFormateada", cotizacionDolarMep != null ? Formateador.importe(cotizacionDolarMep) : null);
        objeto.set("tieneTenencia", tieneTenenciasEnCuentasComitentes(contexto, codigosProducto));
        return RespuestaMB.exito("cotizacionDolarMep", objeto);
    }

    public static RespuestaMB cotizacionDolaMepV2(ContextoMB contexto) {
        try {
//			BigDecimal cotizacionDolarMep = CacheDolarMep.cotizacion(contexto);

            String coefPlazoCalculo = ConfigMB.string("dolarMep_plazo_coeficiente_calculo");

            BigDecimal cotizacionDolarMep = null;
            String[] codigosProducto = new String[] { "AL30", "AL30D" };


            contexto.parametros.set("plazoLiquidacion", "CI");
            contexto.parametros.set("codigoProducto", "AL30");
            RespuestaMB cotizacionAL30CI = obtenerCotizacionPorPlazoLiquidacion(contexto);

            contexto.parametros.set("plazoLiquidacion", coefPlazoCalculo);
            contexto.parametros.set("codigoProducto", "AL30D");
            RespuestaMB cotizacionAL30D = obtenerCotizacionPorPlazoLiquidacion(contexto);

            if (!enHorarioDolarMep(contexto).hayError()) {
                cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precio")
                        .divide(cotizacionAL30D.bigDecimal("precioCotizacion.precio"), 2, RoundingMode.CEILING);
            } else {
                cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precioAnterior")
                        .divide(cotizacionAL30D.bigDecimal("precioCotizacion.precioAnterior"), 2, RoundingMode.CEILING);
            }

            Objeto objeto = new Objeto();
            objeto.set("cotizacion", cotizacionDolarMep);
            objeto.set("cotizacionFormateada", cotizacionDolarMep != null ? Formateador.importe(cotizacionDolarMep) : null);
            objeto.set("tieneTenencia", tieneTenenciasEnCuentasComitentesCache(contexto, codigosProducto));
            objeto.set("leyendaCotizacion", ConfigMB.string("leyenda_dolar_mep_1"));
            objeto.set("leyendaOrdenes", ConfigMB.string("leyenda_dolar_mep_2"));
            objeto.set("leyendaCompraMepExitosa", ConfigMB.string("leyenda_dolar_mep_3"));

            return RespuestaMB.exito("cotizacionDolarMep", objeto);
        } catch (Exception e) {
            return RespuestaMB.error();
        }
    }

    public static RespuestaMB cotizacionDolaMepV3(ContextoMB contexto) {

        String cobisId = contexto.idCobis();
        Boolean enHorario = enHorarioDolarMep(contexto).get("estado").equals("FUERA_HORARIO") ? false : true;
        Boolean prendidoCotizacionMepV3 = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cotizacion_mep_v3");
        BigDecimal cotizacionDolarMep = null;
        String coefPlazoCalculo = ConfigMB.string("dolarMep_plazo_coeficiente_calculo");
        BigDecimal precioCompra = null;
        BigDecimal precioVenta = null;

        if (!enHorario || !prendidoCotizacionMepV3) {
            contexto.parametros.set("plazoLiquidacion", "CI");
            contexto.parametros.set("codigoProducto", "AL30");
            RespuestaMB cotizacionAL30CI = obtenerCotizacionPorPlazoLiquidacion(contexto);

            contexto.parametros.set("plazoLiquidacion", coefPlazoCalculo);
            contexto.parametros.set("codigoProducto", "AL30D");
            RespuestaMB cotizacionAL30D = obtenerCotizacionPorPlazoLiquidacion(contexto);

            if (enHorario) {
                if (!cotizacionAL30D.bigDecimal("precioCotizacion.precio").equals(BigDecimal.ZERO)) {
                    cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precio")
                            .divide(cotizacionAL30D.bigDecimal("precioCotizacion.precio"), 2, RoundingMode.CEILING);
                }
            } else {
                if (!cotizacionAL30D.bigDecimal("precioCotizacion.precioAnterior").equals(BigDecimal.ZERO)) {
                    cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precioAnterior")
                            .divide(cotizacionAL30D.bigDecimal("precioCotizacion.precioAnterior"), 2, RoundingMode.CEILING);
                }
            }

        } else if (enHorario && prendidoCotizacionMepV3) {
            ApiResponseMB intradiariasV2 = RestInversiones.indicesRealTimeV2(contexto, "AL30,AL30D", "", "1");
            String idAL30 = null;
            String idAL30D = null;
            String[] codigosProducto = new String[]{"AL30", "AL30D"};

            for (Objeto item : intradiariasV2.objetos()) {
                if (item.string("symbol").equals("AL30")) {
                    idAL30 = item.string("idIntradiaria");
                }
                if (item.string("symbol").equals("AL30D")) {
                    idAL30D = item.string("idIntradiaria");
                }
            }

            String idAL30Final = idAL30;
            String idAL30DFinal = idAL30D;

            Futuro<ApiResponseMB> futuroProfundidadAL30 = new Futuro<>(() -> RestInversiones.intradiariasProfundidad(contexto, idAL30Final, null));
            Futuro<ApiResponseMB> futuroProfundidadAL30D = new Futuro<>(() -> RestInversiones.intradiariasProfundidad(contexto, idAL30DFinal, null));

            ApiResponseMB profundidadAL30 = futuroProfundidadAL30.get();
            ApiResponseMB profundidadAL30D = futuroProfundidadAL30D.get();

            if (profundidadAL30.hayError() || profundidadAL30D.hayError()) {
                return RespuestaMB.error().set("mensaje", "error al obtener las siguientes cotizaciones:"
                        + (profundidadAL30.hayError() ? " AL30" : "")
                        + (profundidadAL30D.hayError() ? " AL30D" : ""));
            }

            BigDecimal puntaCompraAL30 = null;
            BigDecimal puntaVentaAL30 = null;
            BigDecimal puntaCompraAL30D = null;
            BigDecimal puntaVentaAL30D = null;

            for (Objeto item : profundidadAL30.objetos()) {
                BigDecimal precio = item.bigDecimal("price");
                if ("Bid".equalsIgnoreCase(item.string("tipo"))) {
                    if (puntaCompraAL30 == null || precio.compareTo(puntaCompraAL30) > 0) {
                        puntaCompraAL30 = precio;
                    }
                }
                if ("Offer".equalsIgnoreCase(item.string("tipo"))) {
                    if (puntaVentaAL30 == null || precio.compareTo(puntaVentaAL30) < 0) {
                        puntaVentaAL30 = precio;
                    }
                }
            }

            for (Objeto item : profundidadAL30D.objetos()) {
                BigDecimal precio = item.bigDecimal("price");
                if ("Bid".equalsIgnoreCase(item.string("tipo"))) {
                    if (puntaCompraAL30D == null || precio.compareTo(puntaCompraAL30D) > 0) {
                        puntaCompraAL30D = precio;
                    }
                }
                if ("Offer".equalsIgnoreCase(item.string("tipo"))) {
                    if (puntaVentaAL30D == null || precio.compareTo(puntaVentaAL30D) < 0) {
                        puntaVentaAL30D = precio;
                    }
                }
            }

            // Mantenemos la misma lógica de cálculo ya existente:
            precioCompra = puntaVentaAL30.divide(puntaCompraAL30D, 2, RoundingMode.UP);
            precioVenta = puntaCompraAL30.divide(puntaVentaAL30D, 2, RoundingMode.DOWN);
        }





        // Ahora formateamos la respuesta de manera similar a la versión anterior:
        Objeto objeto = new Objeto();
        objeto.set("cotizacion", cotizacionDolarMep);
        objeto.set("cotizacionFormateada", cotizacionDolarMep != null ? Formateador.importe(cotizacionDolarMep) : null);
        objeto.set("cotizacionCompra", precioCompra); //dejar el mismo precio de cotizacion
        objeto.set("cotizacionCompraFormateada", Formateador.importe(precioCompra));
        objeto.set("cotizacionVenta", precioVenta);
        objeto.set("cotizacionVentaFormateada", Formateador.importe(precioVenta));
        objeto.set("tieneTenencia", tieneTenenciasEnCuentasComitentesCache(contexto, new String[] { "AL30", "AL30D" }));
        objeto.set("leyendaCotizacion", ConfigMB.string("leyenda_dolar_mep_1"));
        objeto.set("leyendaOrdenes", ConfigMB.string("leyenda_dolar_mep_2"));
        objeto.set("leyendaCompraMepExitosa", ConfigMB.string("leyenda_dolar_mep_3"));
        objeto.set("enHorario", enHorario);
        if (!enHorario) {
            objeto.set("fueraDeHorarioDescripcion", enHorarioDolarMep(contexto).string("fueraDeHorarioDescripcion"));
        }

        return RespuestaMB.exito("cotizacionDolarMep", objeto);
    }


    private static BigDecimal[] buscarCotizacion(ContextoMB contexto, String codigoProducto) {
        ApiResponseMB indicesRealTime = RestInversiones.indicesRealTime(contexto, codigoProducto, "", "1");
        BigDecimal[] cotizacion = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
        if (!indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
            Objeto indiceRealTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
            if (indiceRealTime != null) {
                cotizacion[0] = indiceRealTime.bigDecimal("trade");
                cotizacion[1] = indiceRealTime.bigDecimal("previousClose");
            }
        }
        return cotizacion;

    }

    private static boolean tieneTenenciasEnCuentasComitentes(ContextoMB contexto, String[] codigosProducto) {
        boolean tieneTenencia = false;
        ApiResponseMB posicionesNegociables = RestInversiones.obtenerPosicionesNegociables(contexto, "1000", new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 1);
        for (Objeto pNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String codigoProducto = pNegociable.string("codigo").toUpperCase();
            if (Arrays.asList(codigosProducto).contains(codigoProducto)) {
                tieneTenencia = true;
                break;
            }
        }
        return tieneTenencia;
    }

    private static boolean tieneTenenciasEnCuentasComitentesCache(ContextoMB contexto, String[] codigosProducto) {
        boolean tieneTenencia = false;
        ApiResponseMB posicionesNegociables = RestInversiones.obtenerPosicionesNegociablesCache(contexto, "1000", new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 1);
        for (Objeto pNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
            String codigoProducto = pNegociable.string("codigo").toUpperCase();
            if (Arrays.asList(codigosProducto).contains(codigoProducto)) {
                tieneTenencia = true;
                break;
            }
        }
        return tieneTenencia;
    }

    public static RespuestaMB obtenerCotizacionPorPlazoLiquidacion(ContextoMB contexto) {
        String plazoLiquidacion = contexto.parametros.string("plazoLiquidacion"); // CI, 24H, 48H
        String codigoProducto = contexto.parametros.string("codigoProducto");

        Objeto productoOperable = RestInversiones.obtenerProductosOperablesMap(contexto).get(codigoProducto);

        Map<String, String> plazosLiquidacion = new HashMap<>();
        plazosLiquidacion.put("CI", "1");
        plazosLiquidacion.put("24H", "2");
        plazosLiquidacion.put("48H", "3");

        boolean esByma;

        Objeto cotizacion = buscarPrecioCotizacion(contexto, productoOperable, plazosLiquidacion.get(plazoLiquidacion));
        esByma = cotizacion.bool("esByma");
        if (!plazoLiquidacion.equals("48H") && !esByma) {
            cotizacion = buscarPrecioCotizacion(contexto, productoOperable, plazosLiquidacion.get("48H"));
            esByma = cotizacion.bool("esByma");
        }
        Objeto respuesta = new Objeto();
        respuesta.set("esPrecioRealTime", esByma);
        respuesta.set("precio", cotizacion.bigDecimal("precio"));
        respuesta.set("precioAnterior", cotizacion.bigDecimal("precioAnterior"));
        respuesta.set("precioFormateado", Formateador.importeCantDecimales(cotizacion.bigDecimal("precio"), 4));
        return RespuestaMB.exito("precioCotizacion", respuesta);
    }

    private static Objeto buscarPrecioCotizacion(ContextoMB contexto, Objeto productoOperable, String idVencimiento) {
        BigDecimal precio = BigDecimal.ZERO;
        BigDecimal precioAnterior = BigDecimal.ZERO;
        BigDecimal variacion = BigDecimal.ZERO;
        boolean esByma = false;
        boolean buscarPrecioReferencia = true;
        String fecha = null;
        String tipoCotizacion = null;

        ApiResponseMB indicesRealTime = RestInversiones.indicesRealTime(contexto, productoOperable.string("codigo"), "", idVencimiento);
        if (!indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
            Objeto realTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
            if (realTime != null) {
                precio = realTime.bigDecimal("trade").signum() != 0 ? realTime.bigDecimal("trade") : realTime.bigDecimal("previousClose");
                precioAnterior = realTime.bigDecimal("previousClose");
                if (precio.signum() != 0) {
                    if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.string("clasificacion"))) {
                        precio = precio.divide(new BigDecimal(100));
                    }
                    variacion = realTime.bigDecimal("imbalance");
                    fecha = realTime.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
                    tipoCotizacion = "BYMA";
                    esByma = true;
                    buscarPrecioReferencia = false;
                }
            }
        }
        if (buscarPrecioReferencia) {
            ApiResponseMB precioReferencia = RestInversiones.precioTituloValor(contexto, productoOperable.string("codigo"), productoOperable.string("descMoneda"));
            if (!precioReferencia.hayError() && precioReferencia.codigo.intValue() != 204 && !precioReferencia.claves().isEmpty()) {
                precio = precioReferencia.bigDecimal("precioReferencia");
                fecha = precioReferencia.date("fechaPrecio", "yy-MM-dd", "dd/MM/yy HH:mm");
            }
        }
        Objeto objeto = new Objeto();
        objeto.set("esByma", esByma);
        objeto.set("precio", precio);
        objeto.set("precioAnterior", precioAnterior);
        objeto.set("variacion", variacion);
        objeto.set("fecha", fecha);
        objeto.set("tipoCotizacion", tipoCotizacion);
        return objeto;
    }

    public static RespuestaMB enHorarioDolarMep(ContextoMB contexto) {
        try {
            String cobisId = contexto.idCobis();
            boolean fueraHorario = MBAplicacion.funcionalidadPrendida(cobisId, "dolarMepFueraHorario");
            String horaInicio = ConfigMB.string("dolarMep_horaInicio", "10:30");
            String horaFin = ConfigMB.string("dolarMep_horaFin",    "17:00");
            String leyenda = ConfigMB.string("leyendaMepFueraHorario", "Sólo podés operar los <strong>días hábiles de 10:30 a 17:00hs.</strong>");

            boolean enHorario = Util.isDiaHabil(contexto)
                    && !Util.isfueraHorarioV2(horaInicio, horaFin);

            if (fueraHorario || !enHorario) {
                return RespuestaMB
                        .estado("FUERA_HORARIO")
                        .set("fueraDeHorarioDescripcion", leyenda);
            }
        } catch (Exception e) {
            return RespuestaMB.error();
        }
        return RespuestaMB.exito();
    }










    /***************************** AUXILIARES ********************************/

    private static String calcularFechaLiquidacionActivo(ContextoMB contexto, Date fechaOperacion, PlazoLiquidacion plazo) {
        String fechaLiquidacion = "";

        if (plazo.isNulo()) {
            return fechaLiquidacion;
        }

        try {
            DateFormat dateFormatYYYYMMDD = new SimpleDateFormat(C_FORMATO_FECHA_1_YYYYMMDD);
            DateFormat dateFormatDDMMYYY = new SimpleDateFormat(C_FORMATO_FECHA_2_DDMMYYYY);

            if (plazo.isCI()) {
                fechaLiquidacion = dateFormatDDMMYYY.format(fechaOperacion);
            } else {
                fechaLiquidacion = Util.calcularFechaNdiasHabiles(contexto
                        , dateFormatYYYYMMDD.format(fechaOperacion)
                        , C_FORMATO_FECHA_1_YYYYMMDD
                        , plazo.diasHabilesLiquidacion());

                fechaLiquidacion = dateFormatDDMMYYY.format(dateFormatYYYYMMDD.parse(fechaLiquidacion));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return fechaLiquidacion;
    }

    /**
     *
     * @param contexto
     * 		  parametros:
     * 			- especie: especie
     * 		    - monedas: si "especie" es enviado
     * 					- 0: incluir todas las subespecies asociadas
     * @param especies especies
     * @return resultados filtrados
     */
    private static List<Objeto> filtrarEspecies(ContextoMB contexto, List<Objeto> especies) {
        if (contexto.parametros.existe("especie")) {
            List<Objeto> resultadosFiltro
                    = especies
                    .stream()
                    .filter(e ->
                            e.get("codigo").equals(contexto.parametros.get("especie")))
                    .collect(Collectors.toList());
            if (!resultadosFiltro.isEmpty()
                    && contexto.parametros.existe("monedas")
                    && contexto.parametros.get("monedas").equals("0")) {
                resultadosFiltro.addAll(especies
                        .stream()
                        .filter(e ->
                                e.get("producto")
                                        .equals(resultadosFiltro.get(0).get("producto"))
                                        && !e.get("codigo").equals(contexto.parametros.get("especie"))
                        )
                        .collect(Collectors.toList()));
            }
            return resultadosFiltro;
        }
        return especies;
    }


    /************************** INNER PUBLIC STRUCTS *************************/

    public enum TipoOperacionInversion {
        COMPRA, VENTA;

        public boolean isCompra() {
            return this.equals(COMPRA);
        }
        public boolean isVenta() {
            return this.equals(VENTA);
        }
    }

    public enum PlazoLiquidacion {
        // Poner plazo en valor negativo si es un plazo no válido.
        NULO("", "", -1, "-", -1), // e.g. Pago de dividendos
        PL_CI("CI", "0", 0, "Contado Inmediato", 1),
        PL_24HS("24H", "24", 24, "24 hs.", 2),
        PL_48HS("48H", "48", 48, "48 hs.", 3);

        private String id;
        private String codigo;
        private Integer plazo;
        private String descripcion;
        private Integer idVencimiento;
        private static Set<String> idPlazosHabilitados =
                Objeto.setOf(ConfigMB.string("plazos_liquidacion_habilitados")
                                .split("_"))
                        .stream()
                        .collect(Collectors.toSet());

        PlazoLiquidacion(String id, String codigo, Integer plazo, String descripcion, Integer idVencimiento) {
            this.id = id;
            this.codigo = codigo;
            this.plazo = plazo;
            this.descripcion = descripcion;
            this.idVencimiento = idVencimiento;
        }

        public static PlazoLiquidacion codigo(String codigo) {
            for (PlazoLiquidacion e : values()) {
                if (e.codigo.equals(codigo)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(codigo));
        }


        public static PlazoLiquidacion vencimiento(Integer idVencimiento) {
            if (idVencimiento == null) {
                return NULO;
            }
            for (PlazoLiquidacion e : values()) {
                if (e.idVencimiento.equals(idVencimiento)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(idVencimiento));
        }

        public static PlazoLiquidacion id(String id) {
            if (id == null || id.equals("")) {
                return NULO;
            }
            for (PlazoLiquidacion e : values()) {
                if (e.id.equals(id)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public Objeto toObjeto() {
            return new Objeto().set("plazo", this.plazo)
                    .set("descripcion", this.descripcion);
        }

        public Integer plazo() {
            return this.plazo;
        }

        public String descripcion() {
            return this.descripcion;
        }

        public String codigo() {
            return codigo;
        }

        public boolean isCI() {
            return this.equals(PL_CI);
        }

        public boolean is24() {
            return this.equals(PL_24HS);
        }

        public boolean is48() {
            return this.equals(PL_48HS);
        }

        public boolean isNulo() {
            return this.equals(NULO);
        }

        public boolean isHabilitado() {
            return this.plazo < 0 ? false : PlazoLiquidacion.idPlazosHabilitados.contains(this.plazo.toString());
        }

        /*
         * Devuelve el mayor plazo habilitado por VE.
         */
        public static PlazoLiquidacion plazoDefault() {
            String maxPlazo = null;
            for (String plazo : idPlazosHabilitados) {
                if (maxPlazo == null || Integer.parseInt(plazo) > Integer.parseInt(maxPlazo)) {
                    maxPlazo = plazo;
                }
            }
            return PlazoLiquidacion.codigo(maxPlazo);
        }

        public Integer idVencimiento() {
            return idVencimiento;
        }

        public String getId() {
            return id;
        }

        public Integer diasHabilesLiquidacion() {
            return (this.plazo <= 0) ? 0 : this.plazo() / 24;
        }
    }

    public enum TipoPrecioOperacion {
        PRECIO_LIMITE(1, "Precio Limite"),
        PRECIO_MERCADO(2, "Precio Mercado");

        private Integer id;
        private String descripcion;

        TipoPrecioOperacion(Integer id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public static TipoPrecioOperacion id(Integer id) {
            for (TipoPrecioOperacion e : values()) {
                if (e.id.equals(id)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public Objeto toObjeto() {
            return new Objeto().set("id", this.id)
                    .set("descripcion", this.descripcion);
        }

        public Integer id() {
            return this.id;
        }

        public String descripcion() {
            return this.descripcion;
        }

        public boolean isPrecioLimite() {
            return this.equals(PRECIO_LIMITE);
        }

        public boolean isPrecioMercado() {
            return this.equals(PRECIO_MERCADO);
        }
    }

    /************************ FIN: INNER PUBLIC STRUCTS **********************/



    /************************* INICIO: CODIGO A DEPRECAR *********************/

    @Deprecated
    // INV-692
    // TODO GB - Deprecar codigo / Mover
    public static RespuestaMB comprarV1(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Integer plazo = contexto.parametros.integer("plazo");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);
        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Boolean mostrarLeyendaTituloPublico = contexto.parametros.bool("mostrarLeyendaTituloPublico", false);

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto,
                cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error()
                    : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        RespuestaMB resultado = MBInversion.validarPerfilInversor(contexto,
                Optional.of(MBInversion.EnumPerfilInversor.ARRIESGADO), operaFueraPerfil);
        if (resultado.hayError()) {
            RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", "Perfil inversor incorrecto para operar");
            return respuesta;
        }

        Boolean operarFueraPerfilEstaTransaccion = resultado.bool("operaBajoPropioRiesgo");

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);
        idEspecie = StringUtils.substringBefore(idEspecie, "_");
        Objeto productoOperable = productosOperables.get(idEspecie);

        ApiRequestMB request = ApiMB.request("CompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", productoOperable.string("descMoneda"));
        request.body("operaFueraDePerfil", operarFueraPerfilEstaTransaccion ? "SI" : "NO");
        request.body("plazo", plazo);
        request.body("precioLimite", precioLimite);
        request.body("tipo", "Compra");
        request.body("tipoServicio", "Operacion");
        request.body("vigencia", 0);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            if (response.string("codigo").equals("2009")) {
                RespuestaMB respuestaError = new RespuestaMB();
                respuestaError.set("estado", "FUERA_HORARIO");
                respuestaError.set("mensaje", response.string("mensajeAlUsuario"));
                return respuestaError;
            }
            if (response.string("codigo").equals("5000")) {
                return RespuestaMB.estado("FONDOS_INSUFICIENTES");
            }
            if (response.string("codigo").equals("2013")) {
                return RespuestaMB.estado("PLAZO_INVALIDO");
            }
            if (response.string("codigo").equals("2025")) {
                return RespuestaMB.estado("SIN_PERFIL_INVERSOR");
            }
            if (response.string("codigo").equals("2023")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return RespuestaMB.error();
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                        : descripcionError;

                SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("MB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError

                sqlRequest.add("Compra"); // operacion
                sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
                sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
                sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
                sqlRequest.add(idEspecie); // especie
                sqlRequest.add(productoOperable.string("descMoneda")); // moneda
                sqlRequest.add(operarFueraPerfilEstaTransaccion ? "SI" : "NO"); // operaFueraDePerfil
                sqlRequest.add(plazo); // plazo
                sqlRequest.add(precioLimite); // precioLimite
                sqlRequest.add(cantidadNominal); // cantidadNominal
                sqlRequest.add("0"); // vigencia
                sqlRequest.add(response.string("idOrden")); // idOrden
                sqlRequest.add(response.string("numeroOrden")); // numeroOrden
                sqlRequest.add(response.string("comisiones")); // comisiones

                // EMM: para algunos casos desde el front cuando es un título público
                // y a parte es en pesos se muestra una leyenda específica.
                if (mostrarLeyendaTituloPublico) {
                    sqlRequest.add(cuenta.esDolares() ? "6" : "12"); // versionDDJJ
                } else {
                    sqlRequest.add(cuenta.esDolares() ? "6" : ""); // versionDDJJ
                }

                SqlMB.response(sqlRequest);
            } catch (Exception e) {
            }
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("numero", response.string("numeroOrden"));

        // emm-20190613-desde--> Comprobante
        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", response.string("numeroOrden"));
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
        comprobante.put("ESPECIE", idEspecie + " - " + productoOperable.string("descripcion"));
        String moneda = "$";
        if (!"PESOS".equals(productoOperable.string("descMoneda"))) {
            moneda = "USD";
        }
        comprobante.put("IMPORTE",
                moneda + " " + Formateador.importe(precioLimite.multiply(new BigDecimal(cantidadNominal))));
        comprobante.put("TIPO_OPERACION", "Compra");
        comprobante.put("TIPO_ACTIVO", productoOperable.string("clasificacion")); // todo necesito la accion que eligió
        // el cliente
        comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimite));
        comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
        comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
        comprobante.put("CUENTA", cuenta.numero());
        comprobante.put("PLAZO", plazo.toString() + " hs");
        comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
        comprobante.put("VIGENCIA", "0");
        String idComprobante = "titulo-valor-compra_" + response.string("idOrden");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        // emm-20190613-hasta--> Comprobante

        try {
            for (String email : ConfigMB.string("mercadosecundario_email").split(";")) {
                if (!email.trim().isEmpty()) {
                    String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
                    String mensaje = "<html><head></head><body>";
                    mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
                    mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
                    mensaje += "<b>Operación:</b> " + "Compra" + "<br/>";
                    mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
                    mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
                    mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
                    mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
                    mensaje += "</body></html>";

                    if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                        String salesforce_compra_venta_dolar_mep = ConfigMB.string("salesforce_compra_venta_dolar_mep");
                        String salesforce_compra_venta_bonos_acciones = ConfigMB.string("salesforce_compra_venta_bonos_acciones");
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        parametros.set("ASUNTO", asunto);
                        parametros.set("MENSAJE", mensaje);
                        parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
                        parametros.set("EMAIL_DESTINO", email);
                        parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                        parametros.set("TIPO_OPERACION", "Compra");

                        if (idEspecie.equals("AL30")) {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_dolar_mep, parametros));
                        } else {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_bonos_acciones, parametros));
                        }
                    }
                    else {
                        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", email.trim());
                        requestMail.body("plantilla", ConfigMB.string("doppler_generico"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("ASUNTO", asunto);
                        parametros.set("BODY", mensaje);
                        ApiMB.response(requestMail);

                    }

                }
            }
        } catch (Exception e) {
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idComprobante", "titulo-valor-compra_" + response.string("idOrden"));
        respuesta.set("orden", orden);
        return respuesta;
    }

    @Deprecated
    // INV-692
    public static RespuestaMB simularVentaV1(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
        Integer plazo = contexto.parametros.integer("plazo");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        idEspecie = StringUtils.substringBefore(idEspecie, "_");
        ApiRequestMB request = ApiMB.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo);
        request.body("precioLimite", precioLimite);
        request.body("tipo", "Venta");
        request.body("tipoServicio", "Consulta");
        request.body("vigencia", 0);

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {

            if (response.string("codigo").equals("2006") || response.string("mensajeAlUsuario").contains("El producto no existe")) {

                Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto);
                Objeto productoOperable = productosOperables.get(idEspecie);

                if (productoOperable != null) {
                    idEspecie = productoOperable.string("codigo");

                    request = ApiMB.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
                    request.query("idcobis", contexto.idCobis());
                    request.body("cantidadNominal", cantidadNominal);
                    request.body("cuentaComitente", cuentaComitente.numero());
                    request.body("cuentaLiquidacionMonetaria", cuenta.numero());
                    request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
                    request.body("especie", idEspecie);
                    request.body("fecha", fecha);
                    request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
                    request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
                    request.body("plazo", plazo);
                    request.body("precioLimite", precioLimite);
                    request.body("tipo", "Venta");
                    request.body("tipoServicio", "Consulta");
                    request.body("vigencia", 0);

                    response = ApiMB.response(request);
                }
            }
        }

        if (response.hayError()) {
            if (response.string("codigo").equals("2122")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2020")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2014")) {
                if (response.string("mensajeAlUsuario").contains("no es habil")) {
                    RespuestaMB respuesta = RespuestaMB.estado("DIA_NO_HABIL");
                    respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                    return respuesta;
                }
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }

            return RespuestaMB.error();
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("comisiones", response.bigDecimal("comisiones"));
        orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
        orden.set("vigencia", "por el día");
        orden.set("precioMercado", precioMercado);
        return RespuestaMB.exito("orden", orden);
    }

    @Deprecated
    // INV-692
    public static RespuestaMB venderV1(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Integer plazo = contexto.parametros.integer("plazo");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        idEspecie = StringUtils.substringBefore(idEspecie, "_");

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        ApiRequestMB request = ApiMB.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo);
        request.body("precioLimite", precioLimite);
        request.body("tipo", "Venta");
        request.body("tipoServicio", "Operacion");
        request.body("vigencia", 0);

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {

            if (response.string("codigo").equals("2006") || response.string("mensajeAlUsuario").contains("El producto no existe")) {

                Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto);
                Objeto productoOperable = productosOperables.get(idEspecie);

                if (productoOperable != null) {
                    idEspecie = productoOperable.string("codigo");

                    request = ApiMB.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
                    request.query("idcobis", contexto.idCobis());
                    request.body("cantidadNominal", cantidadNominal);
                    request.body("cuentaComitente", cuentaComitente.numero());
                    request.body("cuentaLiquidacionMonetaria", cuenta.numero());
                    request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
                    request.body("especie", idEspecie);
                    request.body("fecha", fecha);
                    request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
                    request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
                    request.body("plazo", plazo);
                    request.body("precioLimite", precioLimite);
                    request.body("tipo", "Venta");
                    request.body("tipoServicio", "Operacion");
                    request.body("vigencia", 0);
                    response = ApiMB.response(request);
                }
            }
        }

        if (response.hayError()) {
            if (response.string("codigo").equals("2009")) {
                RespuestaMB respuestaError = new RespuestaMB();
                respuestaError.set("estado", "FUERA_HORARIO");
                respuestaError.set("mensaje", response.string("mensajeAlUsuario"));
                return respuestaError;
            }
            if (response.string("codigo").equals("5000")) {
                return RespuestaMB.estado("FONDOS_INSUFICIENTES");
            }
            if (response.string("codigo").equals("2013")) {
                return RespuestaMB.estado("PLAZO_INVALIDO");
            }
            if (response.string("codigo").equals("2025")) {
                return RespuestaMB.estado("SIN_PERFIL_INVERSOR");
            }
            if (response.string("codigo").equals("2023")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return RespuestaMB.error();
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

                SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("MB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError

                sqlRequest.add("Venta"); // operacion
                sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
                sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
                sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
                sqlRequest.add(idEspecie); // especie
                sqlRequest.add(cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null); // moneda
                sqlRequest.add(operaFueraPerfil ? "SI" : "NO"); // operaFueraDePerfil
                sqlRequest.add(plazo); // plazo
                sqlRequest.add(precioLimite); // precioLimite
                sqlRequest.add(cantidadNominal); // cantidadNominal
                sqlRequest.add("0"); // vigencia
                sqlRequest.add(response.string("idOrden")); // idOrden
                sqlRequest.add(response.string("numeroOrden")); // numeroOrden
                sqlRequest.add(response.string("comisiones")); // comisiones
                sqlRequest.add(cuenta.esDolares() ? "11" : "9"); // versionDDJJ

                SqlMB.response(sqlRequest);
            } catch (Exception e) {
            }
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("numero", response.string("numeroOrden"));

        // emm-20190613-desde--> Comprobante
        Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie);

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", response.string("numeroOrden"));
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
        comprobante.put("ESPECIE", idEspecie + " - " + datosTituloValor.string("descripcion"));
        String moneda = cuenta.esPesos() ? "$" : cuenta.esDolares() ? "USD" : "";
        comprobante.put("IMPORTE", moneda + " " + Formateador.importe(precioLimite.multiply(new BigDecimal(cantidadNominal))));
        comprobante.put("TIPO_OPERACION", "Venta");
        comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion")); // todo necesito la accion que eligió el cliente
        comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimite));
        comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
        comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
        comprobante.put("CUENTA", cuenta.numero());
        comprobante.put("PLAZO", plazo.toString() + " hs");
        comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
        comprobante.put("VIGENCIA", "0");
        String idComprobante = "titulo-valor-venta_" + response.string("idOrden");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        // emm-20190613-hasta--> Comprobante

        try {
            for (String email : ConfigMB.string("mercadosecundario_email").split(";")) {
                if (!email.trim().isEmpty()) {
                    String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
                    String mensaje = "<html><head></head><body>";
                    mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
                    mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
                    mensaje += "<b>Operación:</b> " + "Venta" + "<br/>";
                    mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
                    mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
                    mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
                    mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
                    mensaje += "</body></html>";

                    if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                        String salesforce_compra_venta_dolar_mep = ConfigMB.string("salesforce_compra_venta_dolar_mep");
                        String salesforce_compra_venta_bonos_acciones = ConfigMB.string("salesforce_compra_venta_bonos_acciones");
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        parametros.set("ASUNTO", asunto);
                        parametros.set("MENSAJE", mensaje);
                        parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
                        parametros.set("EMAIL_DESTINO", email);
                        parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                        parametros.set("TIPO_OPERACION", "Venta");

                        if (idEspecie.equals("AL30")) {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_dolar_mep, parametros));
                        } else {
                            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_compra_venta_bonos_acciones, parametros));
                        }
                    }
                    else {

                        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", email.trim());
                        requestMail.body("plantilla", ConfigMB.string("doppler_generico"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("ASUNTO", asunto);
                        parametros.set("BODY", mensaje);
                        ApiMB.response(requestMail);
                    }
                }
            }
        } catch (Exception e) {
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idComprobante", "titulo-valor-venta_" + response.string("idOrden"));
        respuesta.set("orden", orden);
        return respuesta;
    }

    @Deprecated
    // INV-692
    public static RespuestaMB simularCompraV1(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
        Integer plazo = contexto.parametros.integer("plazo");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
            return cuentasLiquidacionTitulo == null ? RespuestaMB.error() : RespuestaMB.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto);
        idEspecie = StringUtils.substringBefore(idEspecie, "_");
        Objeto productoOperable = productosOperables.get(idEspecie);

        ApiRequestMB request = ApiMB.request("SimularCompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", productoOperable.string("descMoneda"));
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo);
        request.body("precioLimite", precioLimite);
        request.body("tipo", "Compra");
        request.body("tipoServicio", "Consulta");
        request.body("vigencia", 0);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            if (response.string("codigo").equals("2122")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2020")) {
                RespuestaMB respuesta = RespuestaMB.estado("PRECIO_INCORRECTO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }

            return RespuestaMB.error();
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("comisiones", response.bigDecimal("comisiones"));
        orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
        orden.set("vigencia", "por el día");
        orden.set("precioMercado", precioMercado);
        return RespuestaMB.exito("orden", orden);
    }


    /************************* FIN: CODIGO A DEPRECAR ************************/

}

