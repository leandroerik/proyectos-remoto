package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.UnauthorizedException;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.SqlTransferencia;
import ar.com.hipotecario.canal.homebanking.servicio.TransferenciaService;
import org.apache.commons.lang3.StringUtils;

public class HBComprobantes {
    /* ========== COMPROBANTES ========== */
    public static Respuesta comprobantes(ContextoHB contexto) {
        String tipoComprobante = contexto.parametros.string("tipoComprobante");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);
        BigDecimal montoDesde = contexto.parametros.bigDecimal("montoDesde");
        BigDecimal montoHasta = contexto.parametros.bigDecimal("montoHasta");
        String idMoneda = contexto.parametros.string("idMoneda");
        String beneficiario = contexto.parametros.string("beneficiario");
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
        String descripcion = contexto.parametros.string("descripcion");
        String nroReclamo = contexto.parametros.string("nroReclamo");
        String orden = contexto.parametros.string("orden");

        /*
         * Posibles valores: debitosAutomaticos tarjetasCredito transferencias
         * compraVentaDolar PagoPrestamos BajaTarjetaCredito vepsPagados
         * serviciosPagados
         */

        if (Objeto.anyEmpty(fechaDesde, fechaHasta, tipoComprobante)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (!"debitosAutomaticos".equals(tipoComprobante) && !"pagoTarjetasCredito".equals(tipoComprobante) && !"transferencias".equals(tipoComprobante) && !"compraVentaDolar".equals(tipoComprobante) && !"pagoPrestamos".equals(tipoComprobante) && !"bajaTarjetaCredito".equals(tipoComprobante) && !"vepsPagados".equals(tipoComprobante) && !"serviciosPagados".equals(tipoComprobante)) {
            return Respuesta.error();
        }

        Boolean prendidoTransferenciasHaberes = true;

        TarjetaCredito tarjetaCredito = null;
        if (!"".equals(idTarjetaCredito))
            tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        if ("transferencias".equals(tipoComprobante))
            return comprobantesTransferencias(contexto, fechaDesde, fechaHasta, montoDesde, montoHasta, prendidoTransferenciasHaberes, idMoneda, beneficiario);

        ApiRequest request = null;
        ApiResponse response = null;

        String tipoComprobanteRequest = "";
        if ("debitosAutomaticos".equals(tipoComprobante))
            tipoComprobanteRequest = "debitosautomaticos";
        if ("pagoTarjetasCredito".equals(tipoComprobante))
            tipoComprobanteRequest = "tarjetascredito";
//        if ("transferencias".equals(tipoComprobante))
//            tipoComprobanteRequest = "transferencias";
        if ("compraVentaDolar".equals(tipoComprobante))
            tipoComprobanteRequest = "transferencias";
        if ("pagoPrestamos".equals(tipoComprobante))
            tipoComprobanteRequest = "prestamos";


        Respuesta respuesta = new Respuesta();
        request = Api.request("ComprobantesConsulta", "comprobantes", "GET", "/v1/comprobante", contexto);
        request.query("idusuario", contexto.idCobis());
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("tipocomprobante", tipoComprobanteRequest);

        if (!"vepsPagados".equals(tipoComprobante) && !"serviciosPagados".equals(tipoComprobante) && !"bajaTarjetaCredito".equals(tipoComprobante)) {
            response = Api.response(request, contexto.idCobis());
            if (response.hayError()) {
                return Respuesta.error();
            }
        }

        // BAJA TARJETA DE CREDITOS
        if ("bajaTarjetaCredito".equals(tipoComprobante)) {
            ApiRequest requestBajaTarjetaCredito = Api.request("ComprobantesReclamosContainer", "prisma", "GET", "/v1/reclamosContainer", contexto);
            requestBajaTarjetaCredito.query("fechaAperturaDesde", contexto.parametros.date("fechaDesde", "d/M/yyyy", "M/d/yyyy", null));
            requestBajaTarjetaCredito.query("fechaAperturaHasta", contexto.parametros.date("fechaHasta", "d/M/yyyy", "M/d/yyyy", null));
            requestBajaTarjetaCredito.query("idCobis", contexto.idCobis());

            if ("".equals(ConfigHB.string("reclamo_solicitud_baja_tc"))) {
                return Respuesta.estado("FALTA_CONFIGURAR_RECLAMO_SOLICITUD_BAJA_TC");
            }
            requestBajaTarjetaCredito.query("idTema", ConfigHB.string("reclamo_solicitud_baja_tc").split("_")[0]);

            ApiResponse responseBajaTarjetaCredito = Api.response(requestBajaTarjetaCredito, contexto.idCobis());
            if (responseBajaTarjetaCredito.hayError()) {
                return Respuesta.error();
            }

            for (Objeto item : responseBajaTarjetaCredito.objetos()) {
                Objeto comprobante = new Objeto();
                String idComprobante = "reclamo" + "_" + item.string("idReclamo");
                String idTema = item.objetos("items").get(0).string("idTema");
                String descripcionTema = item.objetos("items").get(0).string("descripcionTema");
                String tipoProducto = item.objetos("items").get(0).string("idProducto");
                String reclamoBajaTcAdicional = "";
                if ("".equals(ConfigHB.string("reclamo_solicitud_baja_tc_adicional"))) {
                    return Respuesta.estado("FALTA_CONFIGURAR_RECLAMO_SOLICITUD_BAJA_TC_ADICIONAL");
                }
                reclamoBajaTcAdicional = ConfigHB.string("reclamo_solicitud_baja_tc_adicional").split("_")[0];

                if (idTema.equals(reclamoBajaTcAdicional))
                    descripcionTema = "BAJA DE TARJETA ADICIONAL";
                comprobante.set("idComprobante", idComprobante);
                comprobante.set("idReclamo", item.string("idReclamo"));
                comprobante.set("fecha", item.date("fechaApertura", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                String fechaApertura = "";
                if ("".equals(item.date("fechaApertura", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm")))
                    fechaApertura = item.date("fechaApertura", "yyyy-MM-dd", "dd/MM/yyyy");
                comprobante.set("fecha", fechaApertura);
                comprobante.set("descripcion", descripcionTema);

                if (nroReclamo.equals("") || nroReclamo.equals(item.string("descripcionTema"))) {
                    respuesta.add("comprobantes", comprobante);
                }
                Map<String, String> comprobantePdf = new HashMap<>();
                comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                if ("".equals(item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm")))
                    comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                comprobantePdf.put("ID", item.string("idReclamo"));
                comprobantePdf.put("DESCRIPCION", descripcionTema);

                if ("ATC".equals(tipoProducto)) {
                    comprobantePdf.put("LABEL_TARJETA", "Número de Tarjeta");
                    comprobantePdf.put("NUMERO_TARJETA", " XXXX-" + Formateador.ultimos4digitos(item.objetos("items").get(0).string("numeroProducto")));
                    comprobantePdf.put("FECHA_RECEPCION", fechaApertura);
                } else {
                    comprobantePdf.put("LABEL_TARJETA", "");
                    comprobantePdf.put("NUMERO_TARJETA", "");
                    comprobantePdf.put("FECHA_RECEPCION", fechaApertura);
                }
                contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
            }

        }

        // COMPRA-VENTA-DOLAR
        if ("compraVentaDolar".equals(tipoComprobante)) {
            for (Objeto item : response.objetos("comprobantes")) {
                Objeto comprobante = new Objeto();
                comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                Objeto xml = Objeto.fromXml(item.string("mensaje").replace("\r\n", "").replace("\n", ""));
                Objeto transferencia = xml;
                if ("S".equals(transferencia.string("esCuentaPropia")) && !transferencia.string("idMoneda").equals(transferencia.string("idMonedaDestino"))) {
                    String idComprobante = "compra-venta-dolares" + "_" + transferencia.string("idOperacion");
                    if (transferencia.string("idOperacion").equals("null")) {
                        idComprobante = "compra-venta-dolares" + "_" + UUID.randomUUID().toString().replaceAll("-", "");
                    }
                    comprobante.set("idComprobante", idComprobante);
                    comprobante.set("descripcion", "80".equals(transferencia.string("idMoneda")) ? "Compra de dólares" : "Venta de dólares");
                    comprobante.set("cuentaOrigen", Formateador.tipoCuenta(transferencia.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("cuenta")));
                    comprobante.set("cuentaDestino", Formateador.tipoCuenta(transferencia.string("tipoProducto")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("numeroProducto")));
                    BigDecimal importe = "80".equals(transferencia.string("idMoneda")) ? transferencia.bigDecimal("importeDestino") : transferencia.bigDecimal("importe");
                    comprobante.set("importe", importe);
                    comprobante.set("importeFormateado", Formateador.importe(importe));
                    if ((montoDesde == null || montoDesde.compareTo(transferencia.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(transferencia.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(transferencia.string("idMonedaDestino")))) {
                        respuesta.add("comprobantes", comprobante);
                    }

                    String tipoCuentaOrigenDescripcion = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoCuenta"), transferencia.string("idMoneda"), transferencia.string("cuenta"));
                    String tipoCuentaDestinoDescripcion = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoProducto"), transferencia.string("idMonedaDestino"), transferencia.string("numeroProducto"));

                    Boolean es30porciento = !transferencia.string("percepcion").isEmpty() && !transferencia.string("percepcion").contains("self");

                    Map<String, String> comprobantePdf = new HashMap<>();
                    comprobantePdf.put("OPERACION_A", (transferencia.string("idMoneda").equals("80") ? "Compra" : "Venta") + " USD");
                    comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    comprobantePdf.put("ID", transferencia.string("idOperacion").replace("null", ""));
                    comprobantePdf.put("OPERACION_B", (transferencia.string("idMoneda").equals("80") ? "COMPRA" : "VENTA") + " USD");
                    comprobantePdf.put("IMPORTE", "USD " + Formateador.importe(importe));
                    comprobantePdf.put("TIPO_TRANSFERENCIA", "A cuenta propia");
                    comprobantePdf.put("CUENTA_ORIGEN", tipoCuentaOrigenDescripcion);
                    comprobantePdf.put("CUENTA_DESTINO", tipoCuentaDestinoDescripcion);
                    comprobantePdf.put("OPERACION_C", (transferencia.string("idMoneda").equals("80") ? "Debitar" : "Acreditar"));
                    if (es30porciento) {
                        comprobantePdf.put("MONTO", "$ " + Formateador.importe(transferencia.bigDecimal("importeTotal")));
                    } else {
                        comprobantePdf.put("MONTO", "$ " + Formateador.importe(transferencia.bigDecimal("importePesos")));
                    }
                    comprobantePdf.put("COTIZACION", "USD 1 = $ " + Formateador.importe(transferencia.bigDecimal("cotizacion")));
                    if (transferencia.bigDecimal("impRg4815") != null && transferencia.bigDecimal("impRg4815").compareTo(new BigDecimal(0)) != 0) {
                        comprobantePdf.put("TEXTO_IMPUESTO_4815", "Percepción ARCA RG 5617/2024:");
                        comprobantePdf.put("VALOR_IMPUESTO_4815", "$ " + Formateador.importe(transferencia.bigDecimal("impRg4815")));

                    } else {
                        comprobantePdf.put("TEXTO_IMPUESTO_4815", "");
                        comprobantePdf.put("VALOR_IMPUESTO_4815", "");
                    }

                    try {
                        if (transferencia.string("idMoneda").equals("80") && transferencia.bigDecimal("impRg5430") != null && transferencia.bigDecimal("impRg5430").compareTo(new BigDecimal(0)) != 0) {
                            comprobantePdf.put("TEXTO_IMPUESTO_5430", "Percepción AFIP RG 5430/2023:");
                            comprobantePdf.put("VALOR_IMPUESTO_5430", "$ " + Formateador.importe(response.bigDecimal("impRg5430")));
                            if (es30porciento) {
                                comprobantePdf.put("MONTO", "$ " + Formateador.importe(transferencia.bigDecimal("importeTotal").add(response.bigDecimal("impRg5430"))));

                            }
                        } else {
                            comprobantePdf.put("TEXTO_IMPUESTO_5430", "");
                            comprobantePdf.put("VALOR_IMPUESTO_5430", "");
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    if (transferencia.bigDecimal("percepcion") != null && transferencia.bigDecimal("percepcion").compareTo(new BigDecimal(0)) != 0) {
                        comprobantePdf.put("TEXTO_PERCEPCION", "Impuesto País:");
                        comprobantePdf.put("VALOR_PERCEPCION", "$ " + Formateador.importe(transferencia.bigDecimal("percepcion")));
                    } else {
                        comprobantePdf.put("TEXTO_PERCEPCION", "");
                        comprobantePdf.put("VALOR_PERCEPCION", "");
                    }
                    String leyendaCompraVenta = Texto.htmlToText(ConfigHB.string("leyenda_compra_venta_dolares"));
                    leyendaCompraVenta = leyendaCompraVenta.replace("MONTO_DOLARES", Formateador.importe(importe));
                    comprobantePdf.put("LEYENDA_COMPRA_VENTA_DOLARES", "");
                    contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
                }
            }
        }

        // DEBITOS-AUTOMATICOS
        if ("debitosAutomaticos".equals(tipoComprobante)) {
            for (Objeto item : response.objetos("comprobantes")) {
                Objeto comprobante = new Objeto();
                comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                String idComprobante = "debito-automatico" + "_" + item.string("recibo") + "_" + item.string("secFacturas") + "_" + item.string("nroAbonado");
                comprobante.set("idComprobante", idComprobante);
                comprobante.set("idMoneda", item.string("idMoneda"));
                if (comprobante.string("idMoneda").equals("")) {
                    comprobante.set("idMoneda", Cuenta.idMoneda(item.string("nroCuenta")));
                }

                comprobante.set("descripcionMoneda", Formateador.moneda(comprobante.string("idMoneda")));
                comprobante.set("simboloMoneda", Formateador.simboloMoneda(comprobante.string("idMoneda")));
                comprobante.set("cuenta", item.string("tipoCuenta") + " XXXX-" + Formateador.ultimos4digitos(item.string("nroCuenta")));
                comprobante.set("importe", item.bigDecimal("importe"));
                comprobante.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
                comprobante.set("descServicio", item.string("descProducto"));
                if ("".equals(item.string("descProducto"))) {
                    comprobante.set("descServicio", item.string("convenio") + ("".equals(item.string("servicio")) ? "" : " - " + item.string("servicio")));
                }
                if ((montoDesde == null || montoDesde.compareTo(item.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(item.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(item.string("idMoneda")))) {
                    respuesta.add("comprobantes", comprobante);
                }
                comprobante.set("periodo", item.string("periodo"));
                comprobante.set("cuitContribuyente", contexto.persona().cuit());
                String descripcionCuenta = Cuenta.descripcionCuentaComprobante(item.string("tipoCuenta"), comprobante.string("idMoneda"), item.string("nroCuenta"));
                Map<String, String> comprobantePdf = new HashMap<>();
                comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                if (!"".equals(item.string("recibo")))
                    comprobantePdf.put("ID", item.string("recibo"));
                else
                    comprobantePdf.put("ID", item.string("secFacturas"));
                String monedaDescripcion = comprobante.string("idMoneda").equals("80") ? "$ " : (comprobante.string("idMoneda").equals("2") ? "USD" : "");
                comprobantePdf.put("IMPORTE", monedaDescripcion + Formateador.importe(item.bigDecimal("importe")));
                comprobantePdf.put("DESCRIPCION", item.string("descProducto"));
                if ("".equals(item.string("descProducto"))) {
                    comprobantePdf.put("DESCRIPCION", item.string("convenio") + ("".equals(item.string("servicio")) ? "" : " - " + item.string("servicio")));
                }
                comprobantePdf.put("PERIODO", item.string("periodo"));
                comprobantePdf.put("CUIT_CONTRIBUYENTE", contexto.persona().cuit());
                comprobantePdf.put("CUENTA", descripcionCuenta);

                contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);

            }

        }

        // PAGO-DE-TARJETAS
        if ("pagoTarjetasCredito".equals(tipoComprobante)) {
            if ("".equals(idTarjetaCredito) || (!"".equals(idTarjetaCredito) && tarjetaCredito != null)) {
                for (Objeto item : response.objetos("comprobantes")) {
                    Objeto comprobante = new Objeto();
                    comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                    comprobante.set("fecha24hs", item.string("fecha"));

                    Objeto xml = Objeto.fromXml(item.string("mensaje").replace("\r\n", "").replace("\n", ""));
                    Objeto pagoTarjeta = xml;
                    String idComprobante = "tarjeta-credito" + "_" + pagoTarjeta.string("nroTicket");
                    comprobante.set("idComprobante", idComprobante);
                    comprobante.set("idMoneda", pagoTarjeta.string("idMoneda"));
                    comprobante.set("descripcionMoneda", Formateador.moneda(pagoTarjeta.string("idMoneda")));
                    comprobante.set("simboloMoneda", Formateador.simboloMoneda(pagoTarjeta.string("idMoneda")));
                    comprobante.set("cuenta", Formateador.tipoCuenta(pagoTarjeta.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(pagoTarjeta.string("cuenta")));
                    comprobante.set("importe", pagoTarjeta.bigDecimal("importe"));
                    comprobante.set("importeFormateado", Formateador.importe(pagoTarjeta.bigDecimal("importe")));
                    if ((montoDesde == null || montoDesde.compareTo(pagoTarjeta.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(pagoTarjeta.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(pagoTarjeta.string("idMoneda"))) && (tarjetaCredito == null || tarjetaCredito.numero().equals(pagoTarjeta.string("numeroProducto")))) {
                        respuesta.add("comprobantes", comprobante);
                    }
                    Map<String, String> comprobantePdf = new HashMap<>();
                    comprobantePdf.put("ID", pagoTarjeta.string("nroTicket"));
                    comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                    String descripcionCuenta = Cuenta.descripcionCuentaComprobante(pagoTarjeta.string("tipoCuenta"), pagoTarjeta.string("idMoneda"), pagoTarjeta.string("cuenta"));
                    comprobantePdf.put("CUENTA_ORIGEN", descripcionCuenta);
                    comprobantePdf.put("IMPORTE", Formateador.simboloMoneda(pagoTarjeta.string("idMoneda")) + " " + Formateador.importe(comprobante.bigDecimal("importe")));

                    String tipoTarjetaDescripcion = "";
                    tipoTarjetaDescripcion = "I".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Internacional" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "N".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Nacional" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "M".equals(pagoTarjeta.string("tipoProducto")) ? "Mastercard" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "B".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Business" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "O".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Corporate" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "P".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Gold" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "R".equals(pagoTarjeta.string("tipoProducto")) ? "Visa Purchasing" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "L".equals(pagoTarjeta.string("tipoProducto")) ? "Platinum" : tipoTarjetaDescripcion;
                    tipoTarjetaDescripcion = "S".equals(pagoTarjeta.string("tipoProducto")) ? "Signature" : tipoTarjetaDescripcion;
                    comprobantePdf.put("TIPO_Y_NUMERO_TARJETA", tipoTarjetaDescripcion + " XXXX-" + Formateador.ultimos4digitos(pagoTarjeta.string("numeroProducto")));
                    contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);

                }
            }
        }

        // PRESTAMOS
        if ("pagoPrestamos".equals(tipoComprobante)) {
            for (Objeto item : response.objetos("comprobantes")) {
                Objeto comprobante = new Objeto();
                comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                Objeto xml = Objeto.fromXml(item.string("mensaje").replace("\r\n", "").replace("\n", ""));
                Objeto pagoPrestamo = xml;
                String idComprobante = "prestamo" + "_" + pagoPrestamo.string("idOperacion");
                comprobante.set("idComprobante", idComprobante);
                comprobante.set("idMoneda", pagoPrestamo.string("idMoneda"));
                comprobante.set("descripcionMoneda", Formateador.moneda(pagoPrestamo.string("idMoneda")));
                comprobante.set("simboloMoneda", Formateador.simboloMoneda(pagoPrestamo.string("idMoneda")));
                comprobante.set("cuenta", Formateador.tipoCuenta(pagoPrestamo.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(pagoPrestamo.string("cuenta")));
                comprobante.set("importe", pagoPrestamo.bigDecimal("importe"));
                comprobante.set("importeFormateado", Formateador.importe(pagoPrestamo.bigDecimal("importe")));
                String tipoPrestamo = "";
                if ("CCA".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Personal";
                if ("NSP".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Hipotecario";
                if ("PPN".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Prendario";
                if ("MPR".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Prendario";
                if ("MP".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Personal";
                if ("PRE".equals(pagoPrestamo.string("tipoProducto")))
                    tipoPrestamo = "Preventa";
                comprobante.set("tipoPrestamo", tipoPrestamo);
                if ((montoDesde == null || montoDesde.compareTo(pagoPrestamo.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(pagoPrestamo.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(pagoPrestamo.string("idMoneda")))) {
                    respuesta.add("comprobantes", comprobante);
                }

                Map<String, String> comprobantePdf = new HashMap<>();
                comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                comprobantePdf.put("ID", pagoPrestamo.string("idOperacion"));
                comprobantePdf.put("DESCRIPCION", "PRÉSTAMO " + tipoPrestamo.toUpperCase());
                comprobantePdf.put("IMPORTE", Formateador.simboloMoneda(pagoPrestamo.string("idMoneda")) + " " + Formateador.importe(pagoPrestamo.bigDecimal("importe")));
                String descripcionCuenta = Cuenta.descripcionCuentaComprobante(pagoPrestamo.string("tipoCuenta"), pagoPrestamo.string("idMoneda"), pagoPrestamo.string("cuenta"));
                comprobantePdf.put("CUENTA_ORIGEN", descripcionCuenta);
                comprobantePdf.put("NUMERO_PRESTAMO", pagoPrestamo.string("numeroProducto"));
                contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);

            }
        }

        // TRANSFERENCIAS
        if ("transferencias".equals(tipoComprobante)) {

        }

        // PAGOS DE SERVICIOS
        if ("serviciosPagados".

                equals(tipoComprobante)) {
            Respuesta serviciosPagados = HBPago.comprobantesPorEntePagosLink(contexto);

            for (Objeto item : serviciosPagados.objetos("historicoPago")) {
                Objeto comprobante = new Objeto();
                comprobante.set("idComprobante", item.string("idComprobante"));
                comprobante.set("fecha", item.string("fecha"));
                comprobante.set("descripcion", item.string("descripcion"));
                comprobante.set("importe", item.bigDecimal("importe"));
                comprobante.set("importeFormateado", item.string("importeFormateado"));
                comprobante.set("servicio", item.string("servicio"));
                comprobante.set("pagoNumero", item.string("idDeuda"));
                comprobante.set("cuenta", item.string("pagoTipoCuenta") + " " + item.string("pagoNroCuenta"));

                if ((montoDesde == null || montoDesde.compareTo(comprobante.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(comprobante.bigDecimal("importe")) >= 0)) {
                    respuesta.add("comprobantes", comprobante);
                }
            }
        }

        // VEPS
        if ("vepsPagados".

                equals(tipoComprobante)) {
            Respuesta vepsPagados = HBPago.comprobantesVep(contexto);
            if (vepsPagados.hayError()) {
                return Respuesta.error();
            }

            for (Objeto item : vepsPagados.objetos("comprobantesVeps")) {
                Objeto comprobante = new Objeto();
                comprobante.set("idComprobante", item.string("idComprobante"));
                comprobante.set("fecha", item.string("fecha"));
                comprobante.set("descripcion", item.string("descripcion"));
                comprobante.set("importe", item.bigDecimal("importe"));
                comprobante.set("importeFormateado", item.string("importeFormateado"));
                comprobante.set("servicio", item.string("servicio"));
                comprobante.set("pagoNumero", item.string("pagoNumero"));
                comprobante.set("cuenta", item.string("pagoTipoCuenta") + " " + item.string("pagoNroCuenta"));

                if ((montoDesde == null || montoDesde.compareTo(comprobante.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(comprobante.bigDecimal("importe")) >= 0) && ("".equals(descripcion) || item.string("descripcion").toLowerCase().contains(descripcion.toLowerCase()))) {
                    respuesta.add("comprobantes", comprobante);
                }
            }

        }

        if ("FECHA_ASCENDENTE".

                equals(orden)) {
            try {
                Objeto datos = (Objeto) respuesta.get("comprobantes");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", dato.date("fecha", "dd/MM/yyyy").getTime());
                }
                datos.ordenar("orden", "idComprobante");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", null);
                }
            } catch (Exception e) {
            }
        }

        if ("FECHA_DESCENDENTE".

                equals(orden)) {
            try {
                Objeto datos = (Objeto) respuesta.get("comprobantes");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", Long.MAX_VALUE - dato.date("fecha", "dd/MM/yyyy").getTime());
                }
                datos.ordenar("orden", "idComprobante");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", null);
                }
            } catch (Exception e) {
            }
        }

        if ("IMPORTE_ASCENDENTE".

                equals(orden)) {
            try {
                Objeto datos = (Objeto) respuesta.get("comprobantes");
                for (Objeto dato : datos.objetos()) {
                    String valor = Long.valueOf(dato.bigDecimal("importe").multiply(new BigDecimal("100")).longValue()).toString();
                    while (valor.length() < 15) {
                        valor = "0" + valor;
                    }
                    dato.set("orden", valor);
                }
                datos.ordenar("orden", "idComprobante");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", null);
                }
            } catch (Exception e) {
            }
        }

        if ("IMPORTE_DESCENDENTE".

                equals(orden)) {
            try {
                Objeto datos = (Objeto) respuesta.get("comprobantes");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", Long.MAX_VALUE - dato.bigDecimal("importe").multiply(new BigDecimal("100")).longValue());
                }
                datos.ordenar("orden", "idComprobante");
                for (Objeto dato : datos.objetos()) {
                    dato.set("orden", null);
                }
            } catch (Exception e) {
            }
        }

        return respuesta;
    }

    public static String eliminarTextoSelfClosing(String texto) {
        if (texto == null)
            return "";
        return texto.contains("-self-closing") ? "" : texto;
    }

    public static byte[] comprobante(ContextoHB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");
        
        if ("VEP".equals(idComprobante.split("_")[0]))
            return HBPago.comprobantePagoVep(contexto);
        else if ("N".equals(idComprobante.split("_")[0]))
            return HBPago.comprobantePagoServicio(contexto);
        else if ("transferencia-cvu".equals(idComprobante.split("_")[0])) {
            Futuro<SqlResponse> futuroDatosComprobante = new Futuro<>(() ->
                    new SqlTransferencia().obtenerDatosComprobanteTransferencia(contexto, idComprobante.split("_")[1]));

            ApiRequest request = Api.request("ComprobantesTransferenciasCvu", "debin", "GET", "/v1/debin/{id}", contexto);
            request.path("id", idComprobante.split("_")[1]);
            ApiResponse response = Api.response(request, contexto.idCobis());
            if (response.hayError())
                return null;

            Map<String, String> comprobantePdf = new HashMap<>();
            comprobantePdf.put("FECHA_HORA", response.date("detalle.fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
            comprobantePdf.put("ID_COMPROBANTE", response.string("id"));
            comprobantePdf.put("NOMBRE_BENEFICIARIO", response.string("vendedor.cliente.nombreCompleto"));
            comprobantePdf.put("IMPORTE", "$ " + Formateador.importe(response.bigDecimal("detalle.importe")));
            comprobantePdf.put("TIPO_TRANSFERENCIA", "A cuenta virtual");
            comprobantePdf.put("CUENTA_ORIGEN", response.string("comprador.cliente.cuenta.cbu"));

            comprobantePdf.put("CUIT_DESTINO", response.string("vendedor.cliente.idTributario"));
            comprobantePdf.put("CONCEPTO", TransferenciaService.conceptos().get(response.string("detalle.concepto")));
            comprobantePdf.put("ESTADO", response.string("estado.descripcion"));

            comprobantePdf.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());

            SqlResponse sqlResponse = new SqlTransferencia().obtenerContactoAgendadoById(contexto.idCobis(),
                    response.string("vendedor.cliente.cuenta.cbu"), "");

            comprobantePdf.put("MENSAJE", !sqlResponse.hayError && !sqlResponse.registros.isEmpty() ?
                    sqlResponse.registros.get(0).string("comentario") : "");

            comprobantePdf.put("CONCEPTO",
                    !sqlResponse.hayError && !sqlResponse.registros.isEmpty() ?
                            TransferenciaService.conceptos().get(sqlResponse.registros.get(0).string("concepto")) : "");

            comprobantePdf.put("IMPUESTOS", "***");

            sqlResponse = futuroDatosComprobante.tryGet();
            
            String idCobis = (String) sqlResponse.registros.get(0).get("id_cobis");
            if(!idCobis.equals(contexto.sesion.idCobis)) {
            	return null;
            }

            if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
                comprobantePdf.put("NOMBRE_BANCO", sqlResponse.registros.get(0).string("nombre_banco"));
                comprobantePdf.put("CUENTA_DESTINO", StringUtils.isNotBlank(response.string("vendedor.cliente.cuenta.cbu")) ? response.string("vendedor.cliente.cuenta.cbu") : sqlResponse.registros.get(0).string("cuenta_numero"));
            } else {
                comprobantePdf.put("CUENTA_DESTINO", response.string("vendedor.cliente.cuenta.cbu"));
                comprobantePdf.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, response.string("vendedor.cliente.cuenta.cbu"), ""));
            }

            comprobantePdf.put("CUENTA_ORIGEN", response.string("comprador.cliente.cuenta.cbu"));

            contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
            contexto.parametros.set("id", idComprobante);
            return HBArchivo.comprobante(contexto);
        }
        if ("transferencia".equals(idComprobante.split("_")[0])) {



            Map<String, String> comprobantePdf = contexto.sesion.comprobantes.get(idComprobante);

            try {
                if (comprobantePdf.get("CUIT_DESTINO") == null || comprobantePdf.get("CUIT_DESTINO").isEmpty()) {
                    CuentaTercero cuentaTercero = new CuentaTercero(contexto, comprobantePdf.get("ID_CUENTA_DESTINO"));
                    comprobantePdf.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
                    comprobantePdf.put("CUIT_DESTINO", cuentaTercero.cuit());
                    comprobantePdf.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                    comprobantePdf.put("NOMBRE_BANCO", cuentaTercero.banco());

                    SqlResponse sqlResponse = new SqlTransferencia().obtenerContactoAgendadoById(contexto.idCobis(),
                            cuentaTercero.cbu(), cuentaTercero.numero() == null ? "" : cuentaTercero.numero());

                    comprobantePdf.put("MENSAJE", !sqlResponse.hayError && !sqlResponse.registros.isEmpty() ?
                            sqlResponse.registros.get(0).string("comentario") : "");

                    comprobantePdf.put("CONCEPTO",
                            !sqlResponse.hayError && !sqlResponse.registros.isEmpty() ?
                                    TransferenciaService.conceptos().get(sqlResponse.registros.get(0).string("concepto")) : "");

                    comprobantePdf.put("CUENTA_DESTINO", cuentaTercero.cbu());
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);

            contexto.parametros.set("id", idComprobante);
            return HBArchivo.comprobante(contexto);
        } else {
            contexto.parametros.set("id", idComprobante);
            return HBArchivo.comprobante(contexto);

        }
    }

    public static byte[] comprobanteAuditoriaCompraVentaDolar(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String tipoComprobante = "compraVentaDolar";
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);
        String cobisDestino = contexto.parametros.string("cobisDestino");
        String recibo = contexto.parametros.string("id");

        contexto.parametros.set("tipoComprobante", tipoComprobante);
        contexto.parametros.set("id", "compra-venta-dolares_" + contexto.parametros.string("id"));

        BigDecimal montoDesde = contexto.parametros.bigDecimal("montoDesde");
        BigDecimal montoHasta = contexto.parametros.bigDecimal("montoHasta");
        String idMoneda = contexto.parametros.string("idMoneda");

        if (Objeto.anyEmpty(fechaDesde, fechaHasta, tipoComprobante, cobisDestino)) {
            return null;
        }

        String tipoComprobanteRequest = "transferencias";

        Respuesta respuesta = new Respuesta();
        ApiRequest request = Api.request("ComprobantesConsulta", "comprobantes", "GET", "/v1/comprobante", contexto);
        request.query("idusuario", cobisDestino);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("tipocomprobante", tipoComprobanteRequest);

        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            return null;
        }

        for (Objeto item : response.objetos("comprobantes")) {

            try {
                Objeto comprobante = new Objeto();
                comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                Objeto xml = Objeto.fromXml(item.string("mensaje").replace("\r\n", "").replace("\n", ""));
                Objeto transferencia = xml;
                if ("S".equals(transferencia.string("esCuentaPropia")) && !transferencia.string("idMoneda").equals(transferencia.string("idMonedaDestino"))) {
                    String idComprobante = "compra-venta-dolares" + "_" + transferencia.string("idOperacion");
                    if (transferencia.string("idOperacion").equals("null")) {
                        idComprobante = "compra-venta-dolares" + "_" + UUID.randomUUID().toString().replaceAll("-", "");
                    }
                    comprobante.set("idComprobante", idComprobante);
                    comprobante.set("descripcion", "80".equals(transferencia.string("idMoneda")) ? "Compra de dólares" : "Venta de dólares");
                    comprobante.set("cuentaOrigen", Formateador.tipoCuenta(transferencia.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("cuenta")));
                    comprobante.set("cuentaDestino", Formateador.tipoCuenta(transferencia.string("tipoProducto")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("numeroProducto")));
                    BigDecimal importe = "80".equals(transferencia.string("idMoneda")) ? transferencia.bigDecimal("importeDestino") : transferencia.bigDecimal("importe");
                    comprobante.set("importe", importe);
                    comprobante.set("importeFormateado", Formateador.importe(importe));
                    if ((montoDesde == null || montoDesde.compareTo(transferencia.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(transferencia.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(transferencia.string("idMonedaDestino")))) {
                        respuesta.add("comprobantes", comprobante);
                    }

                    String tipoCuentaOrigenDescripcion = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoCuenta"), transferencia.string("idMoneda"), transferencia.string("cuenta"));
                    String tipoCuentaDestinoDescripcion = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoProducto"), transferencia.string("idMonedaDestino"), transferencia.string("numeroProducto"));

                    Boolean es30porciento = !transferencia.string("percepcion").isEmpty() && !transferencia.string("percepcion").contains("self");

                    Map<String, String> comprobantePdf = new HashMap<>();
                    comprobantePdf.put("OPERACION_A", (transferencia.string("idMoneda").equals("80") ? "Compra" : "Venta") + " USD");
                    comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    comprobantePdf.put("ID", transferencia.string("idOperacion").replace("null", ""));
                    comprobantePdf.put("OPERACION_B", (transferencia.string("idMoneda").equals("80") ? "COMPRA" : "VENTA") + " USD");
                    comprobantePdf.put("IMPORTE", "USD " + Formateador.importe(importe));
                    comprobantePdf.put("TIPO_TRANSFERENCIA", "A cuenta propia");
                    comprobantePdf.put("CUENTA_ORIGEN", tipoCuentaOrigenDescripcion);
                    comprobantePdf.put("CUENTA_DESTINO", tipoCuentaDestinoDescripcion);
                    comprobantePdf.put("OPERACION_C", (transferencia.string("idMoneda").equals("80") ? "Debitar" : "Acreditar"));
                    if (es30porciento) {
                        comprobantePdf.put("MONTO", "$ " + Formateador.importe(transferencia.bigDecimal("importeTotal")));
                    } else {
                        comprobantePdf.put("MONTO", "$ " + Formateador.importe(transferencia.bigDecimal("importePesos")));
                    }
                    comprobantePdf.put("COTIZACION", "USD 1 = $ " + Formateador.importe(transferencia.bigDecimal("cotizacion")));
                    if (transferencia.bigDecimal("impRg4815") != null && transferencia.bigDecimal("impRg4815").compareTo(new BigDecimal(0)) != 0) {
                        comprobantePdf.put("TEXTO_IMPUESTO_4815", "Percepción ARCA RG 5617/2024:");
                        comprobantePdf.put("VALOR_IMPUESTO_4815", "$ " + Formateador.importe(transferencia.bigDecimal("impRg4815")));

                    } else {
                        comprobantePdf.put("TEXTO_IMPUESTO_4815", "");
                        comprobantePdf.put("VALOR_IMPUESTO_4815", "");
                    }

                    try {
                        if (transferencia.string("idMoneda").equals("80") && transferencia.bigDecimal("impRg5430") != null && transferencia.bigDecimal("impRg5430").compareTo(new BigDecimal(0)) != 0) {
                            comprobantePdf.put("TEXTO_IMPUESTO_5430", "Percepción AFIP RG 5430/2023:");
                            comprobantePdf.put("VALOR_IMPUESTO_5430", "$ " + Formateador.importe(transferencia.bigDecimal("impRg5430")));
                        } else {
                            comprobantePdf.put("TEXTO_IMPUESTO_5430", "");
                            comprobantePdf.put("VALOR_IMPUESTO_5430", "");
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    if (idComprobante.equals(contexto.parametros.string("id"))) {
                        String leyenda = "";

                        SqlRequest sqlRequest = Sql.request("SelectComprobanteAuditorME", "hbs");
                        sqlRequest.sql = "select * from [hbs].[dbo].[auditor_compra_venta_dolares] where cobis = ? and recibo = ?";
                        sqlRequest.parametros.add(cobisDestino);
                        sqlRequest.parametros.add(recibo);

                        SqlResponse sqlResponse = Sql.response(sqlRequest);
                        if (!sqlResponse.hayError && sqlResponse.registros.get(0).get("leyenda") != null) {
                            leyenda = (String) sqlResponse.registros.get(0).get("leyenda");
                        }
                        comprobantePdf.put("LEYENDA_COMPRA_VENTA_DOLARES", leyenda);
                    } else {
                        comprobantePdf.put("LEYENDA_COMPRA_VENTA_DOLARES", "");
                    }
                    contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
        return HBArchivo.comprobante(contexto);
    }

    private static Respuesta comprobantesTransferencias(ContextoHB contexto, String fechaDesde, String fechaHasta, BigDecimal montoDesde, BigDecimal montoHasta, boolean prendidoTransferenciasHaberes, String idMoneda, String beneficiario) {
        Futuro<ApiResponse> responseConsultaDebinFuturo = null;
        Futuro<ApiResponse> responseConsulta = null;
        Objeto comprobantes = new Objeto();
        try {
            ApiRequest requestConsultaDebin = Api.request("ComprobantesListadoTransferenciasCvu", "debin", "POST", "/v1/debin/listas", contexto);
            requestConsultaDebin.body.set("listado", new Objeto().set("tamano", 100).set("pagina", 1));
            requestConsultaDebin.body.set("vendedor", new Objeto().set("cliente", new Objeto().set("cuenta", new Objeto().set("", ""))));
            Objeto comprador = new Objeto();
            Objeto clienteComprador = new Objeto();
            clienteComprador.set("idTributario", contexto.persona().cuit());
            clienteComprador.set("cuenta", new Objeto().set("banco", "044"));
            comprador.set("cliente", clienteComprador);
            requestConsultaDebin.body.set("comprador", comprador);
            Objeto debin = new Objeto();
            Objeto creacion = new Objeto();
            creacion.set("fechaDesde", fechaDesde);
            creacion.set("fechaHasta", fechaHasta + "T23:59:59");
            debin.set("creacion", creacion);
            debin.set("estado", new Objeto().set("codigo", "ACREDITADO"));
            requestConsultaDebin.body.set("debin", debin);
            requestConsultaDebin.body.set("tipo", "TRANSFERENCIA");

            responseConsultaDebinFuturo = new Futuro<>(() -> Api.response(requestConsultaDebin, contexto.idCobis()));

        } catch (Exception e) {
        }

        try {
            ApiRequest request = Api.request("ComprobantesConsulta", "comprobantes", "GET", "/v1/comprobante", contexto);
            request.query("idusuario", contexto.idCobis());
            request.query("fechadesde", fechaDesde);
            request.query("fechahasta", fechaHasta);
            request.query("tipocomprobante", "transferencias");

            responseConsulta = new Futuro<>(() -> Api.response(request, contexto.idCobis()));
        } catch (Exception e) {
        }

        try {
            if (responseConsulta != null) {
                ApiResponse responseComprobantes = responseConsulta.tryGet();
                if (responseComprobantes != null && !responseComprobantes.hayError()) {
                    for (Objeto item : responseConsulta.get().objetos("comprobantes")) {
                        Objeto comprobante = new Objeto();
                        comprobante.set("fecha", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                        comprobante.set("fecha24hs", item.string("fecha"));
                        Objeto xml = Objeto.fromXml(item.string("mensaje").replace("\r\n", "").replace("\n", ""));
                        Objeto transferencia = xml;
                        if (!transferencia.existe("user"))
                            transferencia = xml.objeto("TransferenciaOnline");
                        String idComprobante = "transferencia" + "_" + transferencia.string("recibo");
                        if ("S".equals(transferencia.string("esCuentaPropia")) && transferencia.string("idMoneda").equals(transferencia.string("idMonedaDestino"))) {
                            comprobante.set("idComprobante", idComprobante);
                            comprobante.set("idOperacion", transferencia.longer("idOperacion"));
                            if (item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm") != null)
                                comprobante.set("orden", Long.MAX_VALUE - item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss").getTime());
                            else
                                comprobante.set("orden", Long.MAX_VALUE);
                            comprobante.set("cuentaOrigen", Formateador.tipoCuenta(transferencia.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("cuenta")));
                            comprobante.set("cuentaDestino", Formateador.tipoCuenta(transferencia.string("tipoProducto")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("numeroProducto")));
                            comprobante.set("cbuDestino", transferencia.string("numeroProducto"));
                            comprobante.set("beneficiario", contexto.persona().nombreCompleto());
                            comprobante.set("idMoneda", transferencia.string("idMoneda"));
                            comprobante.set("descripcionMoneda", Formateador.moneda(transferencia.string("idMoneda")));
                            comprobante.set("simboloMoneda", Formateador.simboloMoneda(transferencia.string("idMoneda")));
                            comprobante.set("importe", transferencia.bigDecimal("importe"));
                            comprobante.set("importeFormateado", Formateador.importe(transferencia.bigDecimal("importe")));
                            comprobante.set("esCuentaPropia", true);
                            if ((montoDesde == null || montoDesde.compareTo(transferencia.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(transferencia.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(transferencia.string("idMoneda"))))
                                comprobantes.add(comprobante);

                            Map<String, String> comprobantePdf = new HashMap<>();
                            comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                            comprobantePdf.put("ID_COMPROBANTE", transferencia.string("recibo"));
                            comprobantePdf.put("NOMBRE_BENEFICIARIO", contexto.persona().nombreCompleto());
                            comprobantePdf.put("IMPORTE", Formateador.simboloMoneda(transferencia.string("idMoneda")) + " " + Formateador.importe(transferencia.bigDecimal("importe")));
                            comprobantePdf.put("TIPO_TRANSFERENCIA", "A cuenta propia");
                            String descripcionCuentaOrigen = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoCuenta"), transferencia.string("idMoneda"), transferencia.string("cuenta"));
                            comprobantePdf.put("CUENTA_ORIGEN", descripcionCuentaOrigen);
                            comprobantePdf.put("CUENTA_DESTINO", StringUtils.isNotBlank(transferencia.string("cbu")) ? transferencia.string("cbu") : transferencia.string("numeroProducto"));
                            comprobantePdf.put("CUIT_DESTINO", contexto.persona().cuit());
                            String concepto = "";
                            if (transferencia.string("concepto").contains("}"))
                                concepto = "";
                            else
                                concepto = transferencia.string("concepto");
                            comprobantePdf.put("CONCEPTO", concepto);
                            comprobantePdf.put("COMISION", Formateador.simboloMoneda(transferencia.string("idMoneda")) + " 0,00");
                            comprobantePdf.put("IMPUESTOS", Formateador.simboloMoneda(transferencia.string("idMoneda")) + " 0,00");
                            comprobantePdf.put("CUENTA_PROPIA", "S");

                            comprobantePdf.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                            comprobantePdf.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, transferencia.string("cbu"), transferencia.string("numeroProducto")));

                            contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
                        }

                        if ("N".equals(transferencia.string("esCuentaPropia"))) {
                            comprobante.set("idComprobante", idComprobante);
                            comprobante.set("idOperacion", transferencia.longer("idOperacion"));
                            if (item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm") != null) {
                                comprobante.set("orden", Long.MAX_VALUE - item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss").getTime());
                            } else {
                                comprobante.set("orden", Long.MAX_VALUE);
                            }
                            comprobante.set("cuentaOrigen", Formateador.tipoCuenta(transferencia.string("tipoCuenta")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("cuenta")));
                            if (Formateador.ultimos4digitos(transferencia.string("numeroProducto")).contains("}"))
                                comprobante.set("cuentaDestino", "");
                            else
                                comprobante.set("cuentaDestino", Formateador.tipoCuenta(transferencia.string("tipoProducto")) + " XXXX-" + Formateador.ultimos4digitos(transferencia.string("numeroProducto")));
                            comprobante.set("cbu", transferencia.string("cbu"));
                            comprobante.set("beneficiario", transferencia.string("tipoTransferencia").equals("OTROS_CLIENTES") ? "Banco Hipotecario" : eliminarTextoSelfClosing(transferencia.string("beneficiario")));

                            if (transferencia.string("codigoBanco").contains("}"))
                                comprobante.set("codigoBanco", "");
                            else
                                comprobante.set("codigoBanco", transferencia.string("codigoBanco"));
                            comprobante.set("idMoneda", transferencia.string("idMoneda"));
                            comprobante.set("descripcionMoneda", Formateador.moneda(transferencia.string("idMoneda")));
                            comprobante.set("simboloMoneda", Formateador.simboloMoneda(transferencia.string("idMoneda")));
                            comprobante.set("importe", transferencia.bigDecimal("importe"));
                            comprobante.set("importeFormateado", Formateador.importe(transferencia.bigDecimal("importe")));
                            comprobante.set("esCuentaPropia", false);
                            if ((montoDesde == null || montoDesde.compareTo(transferencia.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(transferencia.bigDecimal("importe")) >= 0) && (beneficiario.equals("") || beneficiario.equals(eliminarTextoSelfClosing(transferencia.string("beneficiario")))) && (idMoneda.equals("") || idMoneda.equals(transferencia.string("idMoneda")))) {
                                comprobantes.add(comprobante);
                            }

                            String tipoTransferencia = transferencia.string("tipoTransferencia");

                            String comision = "***";
                            String impuestos = "***";

                            if (tipoTransferencia.equals("OTROS_CLIENTES")) {
                                tipoTransferencia = "A cuenta tercero";
                                comision = Formateador.simboloMoneda(transferencia.string("idMoneda")) + " 0,00";
                                impuestos = Formateador.simboloMoneda(transferencia.string("idMoneda")) + " 0,00";
                            }

                            String concepto = "";
                            if (transferencia.string("concepto").contains("}"))
                                concepto = "";
                            else
                                concepto = transferencia.string("concepto");
                            if (tipoTransferencia.equals("OTROS_BANCOS_INMEDIATA")) {
                                tipoTransferencia = "A otro banco";
                            }
                            if (tipoTransferencia.equals("OTROS_BANCOS_INMEDIATA")) {
                                tipoTransferencia = "A otro banco";
                            }
                            if (prendidoTransferenciasHaberes) {
                                if (tipoTransferencia.equals("SERVICIO_DOMESTICO")) {
                                    tipoTransferencia = "Sueldos – Serv Dom.";
                                    concepto = "Sueldos – Serv Dom.";
                                }
                                if (tipoTransferencia.contains("SERVICIO_HABERES")) {
                                    tipoTransferencia = "Sueldos - Haberes";
                                    concepto = "Sueldos - Haberes";
                                }
                            }

                            Map<String, String> comprobantePdf = new HashMap<>();
                            comprobantePdf.put("FECHA_HORA", item.date("fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                            comprobantePdf.put("ID_COMPROBANTE", transferencia.string("idOperacion"));
                            comprobantePdf.put("NOMBRE_BENEFICIARIO", eliminarTextoSelfClosing(transferencia.string("beneficiario")));
                            comprobantePdf.put("IMPORTE", Formateador.simboloMoneda(transferencia.string("idMoneda")) + " " + Formateador.importe(transferencia.bigDecimal("importe")));
                            comprobantePdf.put("TIPO_TRANSFERENCIA", tipoTransferencia);
                            String descripcionCuentaOrigen = Cuenta.descripcionCuentaComprobante(transferencia.string("tipoCuenta"), transferencia.string("idMoneda"), transferencia.string("cuenta"));
                            comprobantePdf.put("CUENTA_ORIGEN", descripcionCuentaOrigen);
                            comprobantePdf.put("CBU_DESTINO", transferencia.string("cbu"));
                            comprobantePdf.put("CUIT_DESTINO", transferencia.string("cuit"));
                            comprobantePdf.put("CONCEPTO", concepto);
                            comprobantePdf.put("COMISION", comision);
                            comprobantePdf.put("IMPUESTOS", impuestos);
                            comprobantePdf.put("CUENTA_PROPIA", "N");
                            comprobantePdf.put("ID_CUENTA_DESTINO", transferencia.string("numeroProducto"));

                            comprobantePdf.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                            comprobantePdf.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, transferencia.string("cbu"), transferencia.string("numeroProducto")));

                            comprobantePdf.put("CUENTA_DESTINO", StringUtils.isNotBlank(transferencia.string("cbu")) ? transferencia.string("cbu") : Cuenta.descripcionCuentaComprobante(transferencia.string("tipoProducto"), transferencia.string("idMonedaDestino"), transferencia.string("numeroProducto")));

                            SqlResponse sqlResponse = new SqlTransferencia().obtenerContactoAgendadoById(contexto.idCobis(),
                                    transferencia.string("cbu"), !StringUtils.isNotBlank(transferencia.string("numeroProducto")) ? "" : transferencia.string("numeroProducto"));

                            comprobantePdf.put("MENSAJE", !sqlResponse.hayError && !sqlResponse.registros.isEmpty() ?
                                    sqlResponse.registros.get(0).string("comentario") : " ");

                            contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        try {
            if (responseConsultaDebinFuturo != null) {
                ApiResponse responseConsultaDebin = responseConsultaDebinFuturo.tryGet();
                if (responseConsultaDebin != null && !responseConsultaDebin.hayError()) {
                    for (Objeto item : responseConsultaDebin.objetos("debins")) {
                        Objeto comprobante = new Objeto();
                        String idComprobante = "transferencia-cvu_" + item.string("id");
                        comprobante.set("fecha", item.date("fechaAlta", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                        comprobante.set("fecha24hs", item.string("fechaAlta"));
                        if (item.date("fechaAlta", "yyyy-MM-dd'T'HH:mm:ss") != null)
                            comprobante.set("orden", Long.MAX_VALUE - item.date("fechaAlta", "yyyy-MM-dd'T'HH:mm:ss").getTime());
                        else
                            comprobante.set("orden", Long.MAX_VALUE);

                        comprobante.set("idComprobante", idComprobante);
                        comprobante.set("idOperacion", item.string("id"));
                        comprobante.set("cuentaOrigen", "-");
                        comprobante.set("cuentaDestino", "-");
                        comprobante.set("cbu", "");
                        comprobante.set("beneficiario", item.objeto("vendedor").objeto("cliente").string("nombreCompleto"));
                        comprobante.set("codigoBanco", "");
                        comprobante.set("idMoneda", item.objeto("moneda").string("id"));
                        comprobante.set("descripcionMoneda", Formateador.moneda(item.objeto("moneda").string("id")));
                        comprobante.set("simboloMoneda", Formateador.simboloMoneda(item.objeto("moneda").string("id")));
                        comprobante.set("importe", item.bigDecimal("importe"));
                        comprobante.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));

                        SqlResponse sqlResponse = new SqlTransferencia().obtenerDatosComprobanteTransferencia(contexto, idComprobante.split("_")[1]);

                        if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
                            comprobante.set("nombreBanco", sqlResponse.registros.get(0).string("nombre_banco"));
                            comprobante.set("cuentaDestino", sqlResponse.registros.get(0).string("cuenta_numero"));
                        } else {
                            comprobante.set("nombreBanco", "-");
                            comprobante.set("cuentaDestino", "-");
                        }

                        if ((montoDesde == null || montoDesde.compareTo(item.bigDecimal("importe")) <= 0) && (montoHasta == null || montoHasta.compareTo(item.bigDecimal("importe")) >= 0) && (idMoneda.equals("") || idMoneda.equals(item.objeto("moneda").string("id"))))
                            comprobantes.add(comprobante);
                    }
                }
            }
        } catch (Exception e) {
        }
        return Respuesta.exito().set("comprobantes", comprobantes.ordenar("orden"));
    }

}