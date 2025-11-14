package ar.com.hipotecario.mobile.api;

import static ar.com.hipotecario.mobile.api.MBProducto.getProductosEnMora;
import static ar.com.hipotecario.mobile.api.MBProducto.getProductosEnMoraDetalles;
import static ar.com.hipotecario.mobile.api.MBProducto.verificarEstadoMoraTemprana;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.*;
import ar.com.hipotecario.mobile.negocio.*;
import ar.com.hipotecario.mobile.servicio.*;

public class MBPrestamo {

    public static final String FORMA_PAGO_EFECTIVO = "Efectivo";
    public static final String[] TIPOS_MORA_TEMPRANA = {"ONE", "MT"};
    public static final String CODIGO_PRODUCTO_PRESTAMO = "7";
    public static final String[] BUCKETS = {"B2", "B3"};
    public static final String CUOTA_VENCIDA = "Vencida";

    public static RespuestaMB consolidada(ContextoMB contexto) {
        Boolean buscarBaseNegativa = contexto.parametros.bool("buscarBaseNegativa", false);
        RespuestaMB respuesta = new RespuestaMB();
        Boolean mostrarBoton = contexto.persona().esEmpleado() ? false : ConfigMB.bool("prendido_alta_prestamos") && !ContextoMB.cambioDetectadoParaNormativoPP(contexto, false);
        Boolean mostrarBotonAdelanto = MBPersona.tieneOpcionAdelanto(contexto);
        RespuestaMB ofertaPreAprobada = ofertaPreAprobada(contexto);
        Boolean preaprobadoAdelanto = ofertaPreAprobada.existe("adelantoBH");
        Boolean tienePlanSueldo = contexto.esJubilado() && contexto.tieneCuentaCategoriaB() && preaprobadoAdelanto || contexto.esPlanSueldo();

        respuesta.set("tienePlanSueldo", tienePlanSueldo);
        if (contexto.esJubilado()) {
            respuesta.set("jubiladoAptoParaAdelanto", ConfigMB.bool("prendido_adelanto_jubilados", false) && preaprobadoAdelanto && contexto.tieneCuentaCategoriaB());
        }

        List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
        List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
        productosEnMora.forEach(productoMora -> {
            ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
            if (Objects.nonNull(item)) {
                productosEnMoraDetalles.add(item);
            }
        });
        for (Prestamo prestamo : contexto.prestamos()) {
            if (prestamo.detalle().hayError()) {
                AuditorLogService.prestamosLogVisualizador(contexto, "API-Ventas_ConsolidadaPrestamos", null, prestamo.detalle().json);
                return RespuestaMB.error();
            }

            Objeto item = new Objeto();
            item.set("id", prestamo.id());
            item.set("idTipoProducto", prestamo.idTipo());
            item.set("descripcion", descripcionPrestamo(prestamo, contexto.esJubilado()));
            item.set("nroPrestamo", prestamo.numero());
            item.set("idMoneda", prestamo.idMoneda());
            item.set("estado", prestamo.estado());
            item.set("simboloMoneda", prestamo.simboloMoneda());
            item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
            item.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
            item.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
            item.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
            item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas());

            try {
                item.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? prestamo.cantidadCuotas() - prestamo.cuotasPendientes() - prestamo.cuotasVencidas() : 0);
            } catch (Exception e) {
                item.set("cantidadCuotasPagadas", 0);
            }

            item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
            item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
            item.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
            item.set("codigo", prestamo.codigo());
            item.set("pagable", prestamo.codigo().equals("PPADELANTO"));
            item.set("enConstruccion", prestamo.enConstruccion());
            if (!prestamo.categoria().equals("HIPOTECARIO")) {
                respuesta.add("personales", item);
            }
            if (prestamo.categoria().equals("HIPOTECARIO")) {
                respuesta.add("hipotecarios", item);
            }
            item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
            item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
            item.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
            item.set("numeroProducto", prestamo.numero());
            verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item, true);
        }

        datosPreaprobado(contexto, ofertaPreAprobada, respuesta);
        remanenteOfertaPreAprobada(contexto, respuesta, ofertaPreAprobada);
        cargaSolicitudesPendientes(contexto, respuesta);
        respuesta.set("mostrarBotonSolicitudPrestamoPersonal", mostrarBoton && !respuesta.existe("desembolsoPendiente"));
        respuesta.set("mostrarBotonSolicitudAdelanto", mostrarBotonAdelanto && !respuesta.existe("desembolsoPendiente"));
        RestPrestamo.tienePrestamosNsp(contexto, respuesta);
        respuesta.set("baseNegativa", RestPersona.buscarBaseNegativa(buscarBaseNegativa, contexto));
        AuditorLogService.prestamosLogVisualizador(contexto, "API-Ventas_ConsolidadaPrestamos", null, respuesta.toJson());
        return respuesta;
    }

    private static String descripcionPrestamo(Prestamo prestamo, Boolean esJubilado) {
        String descripcion = tieneCategoria(prestamo);

        if (prestamo.descripcionPrestamo().contains("Crédito Refacción")) {
            descripcion = prestamo.tipo();
        } else {
            if ("Personal".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "Préstamo " + prestamo.tipo();
            }
            if ("Hipotecario".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "Crédito " + prestamo.tipo();
            }
            if ("Personal".equalsIgnoreCase(prestamo.categoria()) && "Adelanto".equalsIgnoreCase(prestamo.tipo())) {
                descripcion = "Adelanto de Sueldo";
            }
            if ("Personal".equalsIgnoreCase(prestamo.categoria()) && "Adelanto".equalsIgnoreCase(prestamo.tipo()) && esJubilado) {
                descripcion = "Adelanto de Jubilación";
            }
        }

        return descripcion;
    }

    private static String tieneCategoria(Prestamo prestamo) {
        if (prestamo.categoria().trim().isEmpty()) {
            return prestamo.tipo();
        }
        return prestamo.categoria();
    }

    private static void remanenteOfertaPreAprobada(ContextoMB contexto, RespuestaMB respuesta, RespuestaMB ofertaPreAprobada) {
        Boolean clienteValido = (contexto.esJubilado() && contexto.tieneCuentaCategoriaB()) ? true : contexto.esPlanSueldo();

        if (ConfigMB.bool("prendido_remanente_adelanto") && contexto.tieneAdelantoActivo() && ofertaPreAprobada.existe("adelantoBH") && clienteValido) {
            BigDecimal totalPreaprobado = ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado");
            BigDecimal remanente = tieneRemanenteAdelantoBh(contexto, totalPreaprobado);

            // cliente con remanente valido, mayor al minimo de pp adelanto y puede
            // solicitar otro adelanto
            if (remanente != BigDecimal.ZERO && remanente.compareTo(ConfigMB.bigDecimal("monto_minimo_PP_adelanto")) > 0) {
                BigDecimal porcentajeRemanente = remanente.multiply(new BigDecimal(100)).divide(totalPreaprobado, 2, RoundingMode.HALF_UP);
                BigDecimal acumuladoAdelantoBh = acumuladoAdelantoBh(contexto, totalPreaprobado);
                respuesta.set("utilizadoPreaprobadoAdelanto", acumuladoAdelantoBh);
                respuesta.set("utilizadoPreaprobadoAdelantoFormateado", Formateador.importe(acumuladoAdelantoBh));
                respuesta.set("remanentePreaprobadoAdelanto", remanente);
                respuesta.set("remanentePreaprobadoAdelantoFormateado", Formateador.importe(remanente));
                respuesta.set("porcentajeRemanente", porcentajeRemanente.doubleValue());
            } else {
                // cliente con remanente menor al minimo de pp adelanto NO puede solicitar otro
                // adelanto
                respuesta.set("mostrarBotonSolicitudAdelanto", false);
            }
        }

    }

    private static void cargaSolicitudesPendientes(ContextoMB contexto, RespuestaMB respuesta) {
        Objeto solicitudes = MBOmnicanalidad.detalleSolicitudesPrestamos(contexto);

        if (solicitudes != null && solicitudes.objetos().size() > 0) {
            for (Objeto solicitud : solicitudes.objetos()) {
                if (solicitud.existe("id")) {
                    if (solicitud.bool("desembolsoOnline") && ConfigMB.bool("prendido_desembolso_online")) {
                        if (solicitud.integer("horasRestantes") > 0) {
                            respuesta.set("desembolsoPendiente", solicitud);
                        } else {
                            contexto.parametros.set("idSolicitud", solicitud.string("id"));
                            MBOmnicanalidad.desistirSolicitud(contexto);
                        }
                    } else {
                        respuesta.add("personalesPendientes", new Objeto().set("solicitud", solicitud));
                    }
                }
                if (solicitud.bool("resolucionCanalAmarillo")) {
                    respuesta.set("mostrarBotonSolicitudPrestamoPersonal", false);
                }
            }
        }
    }

    public static RespuestaMB detalle(ContextoMB contexto) {
        String idPrestamo = contexto.parametros.string("idPrestamo");
        Boolean buscarCuotas = contexto.parametros.bool("buscarCuotas", false);
        Boolean buscarDatosCancelacionTotal = contexto.parametros.bool("buscarDatosCancelacionTotal", false);

        Prestamo prestamo = contexto.prestamo(idPrestamo);
        if (Objeto.anyEmpty(prestamo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        List<CuotaPrestamo> cuotas = prestamo.cuotas();
        boolean esProcrear = esProcrear(contexto, prestamo.codigo());

        RespuestaMB respuesta = new RespuestaMB();

        Objeto detalle = new Objeto();
        detalle.set("id", prestamo.id());
        detalle.set("idTipoProducto", prestamo.idTipo());
        detalle.set("idMoneda", prestamo.idMoneda());
        if (prestamo.idMoneda().equals("88")) {
            detalle.set("cotizacionUva", Formateador.importe(Cotizacion.uvaCompra(contexto)));
        }
        detalle.set("numero", prestamo.numero());
        detalle.set("estado", prestamo.estado());
        detalle.set("fechaAlta", prestamo.fechaAlta("dd/MM/yyyy"));
        detalle.set("simboloMoneda", prestamo.simboloMoneda());
        detalle.set("esProcrear", esProcrear);
        detalle.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
        detalle.set("montoSolicitado", prestamo.montoAprobadoFormateado());

        detalle.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
        detalle.set("valorSaldoActual", prestamo.montoUltimaCuota());
        detalle.set("ultimoPagoFormateado", prestamo.montoUltimoPagoFormateado());
        detalle.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
        detalle.set("formaPago", Texto.primeraMayuscula(prestamo.descripcionFormaPago()));
        if (prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN")) {
            detalle.set("formaPago", "Débito automático");
        }
        detalle.set("cuenta", prestamo.cuentaPago() != null ? prestamo.cuentaPago().descripcionCorta() + " " + prestamo.cuentaPago().simboloMoneda() + " " + prestamo.cuentaPago().numeroEnmascarado() : "");
        detalle.set("cuentaCompleta", prestamo.cuentaPago() == null ? "" : prestamo.cuentaPago().numero());
        detalle.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
        detalle.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
        try {
            detalle.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? prestamo.cantidadCuotas() - prestamo.cuotasPendientes() - prestamo.cuotasVencidas() : 0);
        } catch (Exception e) {
            detalle.set("cantidadCuotasPagadas", 0);
        }
        detalle.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
        detalle.set("cantidadCuotasVencidas", !prestamo.enConstruccion() ? prestamo.cuotasVencidas() : 0);
        detalle.set("tipoTasa", Texto.primeraMayuscula(prestamo.descripcionTipoTasa()));
        detalle.set("tasa", prestamo.tasaFormateada());
        detalle.set("pagable", prestamo.enConstruccion() ? false : prestamo.pagable());
        detalle.set("debitoAutomatico", prestamo.debitoAutomatico());
        detalle.set("categoria", Texto.primeraMayuscula(prestamo.categoria()));
        detalle.set("enConstruccion", prestamo.enConstruccion());
        detalle.set("descripcionTipoPrestamo", prestamo.descripcionTipoPrestamo().equalsIgnoreCase("PPADELANTO") ? "Adelanto de sueldo" : Texto.primeraMayuscula(prestamo.descripcionTipoPrestamo()));
        detalle.set("codigo", prestamo.codigo());
        if (!ConfigMB.string("codigos_prestamos_refinanciables", "").equals("")) {
            Set<String> habilitadoRefinanciar = Objeto.setOf(ConfigMB.string("codigos_prestamos_refinanciables").split("_"));
            detalle.set("esRefinanciable", habilitadoRefinanciar.contains(prestamo.codigo()));
        }
        detalle.set("descuentoTasaInteresReferencial", Texto.primeraMayuscula(prestamo.detalle().string("descuentoTasaInteresReferencial")));
        detalle.set("cuotaAnterior", prestamo.montoAdeudadoFormateado());
        detalle.set("fechaUltimoPago", prestamo.fechaUltimoPago("dd/MM/yyyy"));
        detalle.set("fechaVencimientoActual", prestamo.fechaVencimientoActual("dd/MM/yyyy"));
        detalle.set("cantCuotasMora", prestamo.detalle().get("cantCuotasMora"));
        detalle.set("fechaAcuerdo", prestamo.fechaAcuerdo("dd/MM/yyyy"));
        detalle.set("plazoRestante", prestamo.cuotasPendientes());

        detalle.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
        detalle.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
        detalle.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
        detalle.set("numeroProducto", prestamo.numero());
        detalle.set("proximaCuota", cuotas.size());

        try {

            detalle.set("estadoMora", "SIN_MORA");
            Boolean muestreoOnboarding = true;
            boolean enMora = false;
            detalle.set("onboardingMostrado", muestreoOnboarding);

            List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
            List<ProductoMoraDetalles> productosEnMoraDetalles = getProductoMoraDetalles(contexto, productosEnMora);

            if (productosEnMora.size() > 0 && productosEnMora.stream().anyMatch(prod -> prod.numeroProducto().equals(prestamo.numero()) && Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora()) && CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim()))) {
                ProductoMora productoMora = productosEnMora.stream().filter(prod -> prod.numeroProducto().equals(prestamo.numero())).findFirst().get();
                detalle.set("estadoMora", "EN_MORAT");
                enMora = true;
                muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
                if (productosEnMoraDetalles.size() > 0) {
                    ProductoMoraDetalles detalleMora = productosEnMoraDetalles.stream().filter(prod -> prod.ctaId().equals(productoMora.ctaId())).findFirst().get();
                    detalle.set("inicioMora", detalleMora.inicioMora());
                    detalle.set("deudaVencida", detalleMora.deudaVencida());
                    detalle.set("diasEnMora", detalleMora.diasEnMora());
                    detalle.set("ctaId", detalleMora.ctaId());
                    detalle.set("promesaVigente", detalleMora.promesaVigente());
                    detalle.set("AvisoPago", detalleMora.yaPague());
                    detalle.set("deudaAVencer", detalleMora.deudaAVencer());
                    detalle.set("montoMinPromesa", detalleMora.montoMinPromesa());
                    detalle.set("montoMaxPromesa", detalleMora.deudaVencida());
                    Boolean onboardingMostrado = !detalleMora.promesaVigente().isEmpty() || !detalleMora.yaPague().isEmpty();
                    detalle.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

                    try {
                        if (!muestreoOnboarding && onboardingMostrado && enMora) {
                            contexto.parametros.set("nemonico", "ONBOARDING");
                            Util.contador(contexto);
                        }
                    } catch (Exception e) {
                    }

                }

                for (ProductoMora prod : productosEnMora) {
                    if (prod.numeroProducto().equals(prestamo.numero()) && Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora()) && CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim())) {
                        if (Arrays.asList(BUCKETS).contains(Util.bucketMora(prod.diasMora(), prestamo.categoria()))) {
                            detalle.set("estadoMora", "EN_MORAT" + "_" + Util.bucketMora(prod.diasMora(), prestamo.categoria()));
                        }
                    }
                }
            } else if (!FORMA_PAGO_EFECTIVO.equals(prestamo.formaPago()) && prestamo.cuotas().stream().anyMatch(cuota -> CUOTA_VENCIDA.equals(cuota.estado()))) {
                detalle.set("estadoMora", "EN_MORAT");
            }

        } catch (Exception e) {
            detalle.set("estadoMora", "ERROR_MORA");
            detalle.set("onboardingMostrado", true);
        }

        if (buscarDatosCancelacionTotal) {
            ApiResponseMB response = RestPrestamo.simluarCancelacionTotal(contexto, prestamo.numero());
            RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
            if (!response.hayError()) {
                BigDecimal monto = response.bigDecimal("MONTO_A_PAGAR");
                BigDecimal comision = response.bigDecimal("COMISION");
                BigDecimal hipoteca = response.bigDecimal("LIB_HIPOTECA");
                BigDecimal cotizacion = response.bigDecimal("COTIZACION", "1");
                BigDecimal montoPuro = monto.subtract(comision).subtract(hipoteca);

                Objeto item = new Objeto();
                item.set("monto", monto);
                item.set("montoPesos", monto.multiply(cotizacion));
                item.set("montoFormateado", Formateador.importe(monto));
                item.set("montoPesosFormateado", Formateador.importe(monto.multiply(cotizacion)));

                item.set("comision", comision);
                item.set("comisionPesos", comision.multiply(cotizacion));
                item.set("comisionFormateada", Formateador.importe(comision));
                item.set("comisionPesosFormateada", Formateador.importe(comision.multiply(cotizacion)));

                item.set("hipoteca", hipoteca);
                item.set("hipotecaPesos", hipoteca.multiply(cotizacion));
                item.set("hipotecaFormateada", Formateador.importe(hipoteca));
                item.set("hipotecaPesosFormateada", Formateador.importe(hipoteca.multiply(cotizacion)));

                item.set("montoPuro", montoPuro);
                item.set("montoPuroPesos", montoPuro.multiply(cotizacion));
                item.set("montoPuroFormateado", Formateador.importe(montoPuro));
                item.set("montoPuroPesosFormateado", Formateador.importe(montoPuro.multiply(cotizacion)));

                item.set("cotizacion", cotizacion);
                item.set("cotizacionFormateada", Formateador.importe(cotizacion));

                detalle.set("cancelacionTotal", item);
            }


        }

        if (buscarCuotas) {
            for (CuotaPrestamo cuota : cuotas) {
                if (!cuota.idEstado().equals("NO VIGENTE")) {
                    Objeto item = new Objeto();
                    item.set("id", cuota.id());
                    item.set("numero", cuota.numero());
                    item.set("simboloMoneda", prestamo.simboloMoneda());
                    item.set("saldoPrestamo", cuota.saldoPrestamoFormateado());
                    item.set("vencimiento", cuota.fechaVencimiento("dd/MM/yyyy"));
                    item.set("importe", cuota.importeCuotaFormateado());
                    item.set("estado", cuota.estado());
                    item.set("tipoPrestamo", prestamo.tipo());
                    item.set("numeroPrestamo", prestamo.numero());
                    item.set("interes", cuota.interesFormateado());
                    item.set("cuotaPura", cuota.cuotaPuraFormateada());
                    item.set("otrosRubros", cuota.otrosRubrosFormateado());
                    item.set("impuestos", cuota.impuestosFormateado());
                    detalle.add("cuotas", item);
                }
            }
        }

        respuesta.set("detalle", detalle);
        return respuesta;

    }

    public static RespuestaMB pagar(ContextoMB contexto) {
        String idPrestamo = contexto.parametros.string("idPrestamo");
        String idCuenta = contexto.parametros.string("idCuenta");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");

        if (!ConfigMB.bool("prendido_pago_prestamos")) {
            return RespuestaMB.estado("OPERACION_INHABILITADA");
        }

        if (Objeto.anyEmpty(idPrestamo, idCuenta, importe)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Prestamo prestamo = contexto.prestamo(idPrestamo);
        if (prestamo == null) {
            return RespuestaMB.estado("PRESTAMO_NO_EXISTE");
        }
        if (prestamo.detalle().hayError()) {
            return RespuestaMB.error();
        }

        if ("88".equals(prestamo.idMoneda())) {
            return new RespuestaMB().setEstado("PRESTAMO_UVA");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        ApiResponseMB response = RestPrestamo.pagarPrestamoElectronico(contexto, prestamo, cuenta, importe);
        if (response.hayError()) {
            if (403 == response.codigo) {
                return RespuestaMB.estado("FUERA_HORARIO");
            }
            return RespuestaMB.error();
        }
        String nroTicket = response.string("nroTicket");

        try {
            ProductosService.eliminarCacheProductos(contexto);
            prestamo.eliminarCacheDetalle();
            prestamo.eliminarCacheCuotas();
        } catch (Exception e) {
        }

        String idComprobante = "prestamo" + "_" + nroTicket.trim();
        contexto.sesion().setComprobante(idComprobante, MBComprobantes.comprobantePagoPrestamo(contexto, nroTicket, prestamo, cuenta, importe, "", null));
        return RespuestaMB.exito("idComprobante", idComprobante);
    }

    public static RespuestaMB precancelacionTotal(ContextoMB contexto) {
        String idPrestamo = contexto.parametros.string("idPrestamo");
        String idCuenta = contexto.parametros.string("idCuenta");

        if (!ConfigMB.bool("prendido_pago_prestamos")) {
            return RespuestaMB.estado("OPERACION_INHABILITADA");
        }

        if (Objeto.anyEmpty(idPrestamo, idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Prestamo prestamo = contexto.prestamo(idPrestamo);
        if (prestamo == null) {
            return RespuestaMB.estado("PRESTAMO_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        ApiResponseMB simulacion = RestPrestamo.simluarCancelacionTotal(contexto, prestamo.numero());
        if (simulacion.hayError()) {
            RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
            return RespuestaMB.error();
        }
        BigDecimal monto = simulacion.bigDecimal("MONTO_A_PAGAR");
        BigDecimal cotizacion = simulacion.bigDecimal("COTIZACION");

        ApiResponseMB response = RestPrestamo.pagarTotalPrestamo(contexto, prestamo, cuenta);
        if (response.hayError()) {
            RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
            if (response.string("codigo").equals("208003")) {
                return RespuestaMB.estado("SIN_SALDO");
            }
            if (response.string("codigo").equals("201249")) {
                return RespuestaMB.estado("VALORES_SUSPENSO");
            }
            if (response.string("codigo").equals("701326")) {
                return RespuestaMB.estado("SOLO_POR_SUCURSAL");
            }
            if (response.string("codigo").equals("714409")) {
                return RespuestaMB.estado("SOLO_POR_SUCURSAL");
            }
            return RespuestaMB.error();
        }

        Map<String, String> comprobante = MBComprobantes.comprobantePagoPrestamo(contexto, response.string("NRO_COMPROBANTE"), prestamo, cuenta, monto, "pagoTotal", cotizacion);
        String idComprobante = "precancelacion-total-prestamo" + "_" + response.string("NRO_COMPROBANTE");
        contexto.sesion().setComprobante(idComprobante, comprobante);

        return RespuestaMB.exito("idComprobante", idComprobante);
    }

    public static Object ultimaLiquidacion(ContextoMB contexto) {
        String idPrestamo = contexto.parametros.string("idPrestamo");
        String cuota = contexto.parametros.string("cuota");

        if (Objeto.anyEmpty(idPrestamo, cuota)) {
            contexto.setHeader("estado", "PARAMETROS_INCORRECTOS");
            return RespuestaMB.parametrosIncorrectos();
        }

        Prestamo prestamo = contexto.prestamo(idPrestamo);
        if (prestamo == null) {
            contexto.setHeader("estado", "PRESTAMO_NO_EXISTE");
            return RespuestaMB.estado("PRESTAMO_NO_EXISTE");
        }

        ApiResponseMB response = RestPrestamo.ultimaLiquidacion(contexto, prestamo.numero(), cuota);
        if (response.hayError()) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.error();
        }
        if (response.codigo == 204) {
            contexto.setHeader("estado", "SIN_RESUMEN");
            return RespuestaMB.estado("SIN_RESUMEN");
        }

        String base64 = response.string("pdf");
        byte[] archivo = Base64.getDecoder().decode(base64);
        try {
            archivo = Base64.getDecoder().decode(new String(archivo));
        } catch (Exception e) {
        }
        contexto.setHeader("estado", "0");
        contexto.setHeader("Content-Type", "application/pdf; name=Prestamo-" + cuota + ".pdf");
        return archivo;
    }

    public static Object ultimaLiquidacionNsp(ContextoMB contexto) {
        String numero = contexto.parametros.string("numero");

        if (Objeto.anyEmpty(numero)) {
            contexto.setHeader("estado", "PARAMETROS_INCORRECTOS");
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = RestPrestamo.ultimaLiquidacionNsp(contexto, numero);
        if (response.hayError()) {
            contexto.setHeader("estado", "ERROR");
            return RespuestaMB.error();
        }
        if (response.codigo == 204) {
            contexto.setHeader("estado", "SIN_RESUMEN");
            return RespuestaMB.estado("SIN_RESUMEN");
        }
        if (response.string("file").isEmpty()) {
            contexto.setHeader("estado", "SIN_RESUMEN");
            return RespuestaMB.estado("SIN_RESUMEN");
        }

        String base64 = response.string("file");
        byte[] archivo = Base64.getDecoder().decode(base64);
        try {
            archivo = Base64.getDecoder().decode(new String(archivo));
        } catch (Exception e) {
        }
        contexto.setHeader("estado", "0");
        contexto.setHeader("Content-Type", "application/pdf; name=Prestamo-nsp.pdf");
        return archivo;
    }

    public static RespuestaMB movimientosPrestamo(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("cuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        String secuencial = contexto.parametros.string("secuencial");
        String productoCobis = null;

        if (Objeto.anyEmpty(idCuenta, fecha, secuencial)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        if ("Caja de Ahorro".equalsIgnoreCase(cuenta.producto())) {
            productoCobis = "4";
        }
        if ("Cuenta Corriente".equalsIgnoreCase(cuenta.producto())) {
            productoCobis = "3";
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = RestPrestamo.movimientos(contexto, cuenta.numero(), fecha, productoCobis, secuencial);
        respuesta.set("tipo", response.string("tipo"));
        for (Objeto item : response.objetos("pagos")) {
            Objeto pago = new Objeto();
            pago.set("nroPrestamo", item.string("nroPrestamo"));
            pago.set("nroCuotaPago", item.string("nroCuotaPago"));
            pago.set("montoCuotaPago", item.string("montoCuotaPago"));
            pago.set("cuotasRestantes", item.string("cuotasRestantes"));
            pago.set("tasa", item.string("tasa"));
            respuesta.add("pagos", pago);
        }

        return respuesta;
    }

    public static RespuestaMB movimientosDesembolso(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("cuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        String secuencial = contexto.parametros.string("secuencial");
        String productoCobis = null;

        if (Objeto.anyEmpty(idCuenta, fecha, secuencial)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }

        if ("Caja de Ahorro".equalsIgnoreCase(cuenta.producto())) {
            productoCobis = "4";
        }
        if ("Cuenta Corriente".equalsIgnoreCase(cuenta.producto())) {
            productoCobis = "3";
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = RestPrestamo.movimientos(contexto, cuenta.numero(), fecha, productoCobis, secuencial);
        respuesta.set("tipo", response.string("tipo"));
        for (Objeto item : response.objetos("desembolo")) {
            Objeto desembolso = new Objeto();
            desembolso.set("nroPrestamo", item.string("nroPrestamo"));
            desembolso.set("cuotas", item.string("cuotas"));
            desembolso.set("tipoOperacion", item.string("tipoOperacion"));
            desembolso.set("montoAprobado", item.string("montoAprobado"));
            desembolso.set("fechaLiquidacion", item.string("fechaLiquidacion"));
            desembolso.set("formaPago", item.string("formaPago"));
            desembolso.set("cuenta", item.string("cuenta"));
            respuesta.add("desembolsos", desembolso);
        }

        return respuesta;
    }

    public static RespuestaMB formasDePago(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Objeto formas = new Objeto();
        formas.set("AUTOMATIC_DEBIT", "Débito automático");
        formas.set("CASH", "Pago manual");
        respuesta.set("methods", formas);
        /*
         * respuesta.set("AUTOMATIC_DEBIT", "Débito automático") .set("CASH",
         * "Efectivo por ventanilla");
         */
        return respuesta;
    }

    public static RespuestaMB cambiarFormaPago(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        try {
            // String idPrestamo = contexto.parametros.string("idPrestamo");
            String formaPago = contexto.parametros.string("formaPago");
            String idCuenta = contexto.parametros.string("idCuenta", "");
            String numeroProducto = contexto.parametros.string("numeroProducto", "");

            if (Objeto.anyEmpty(formaPago, numeroProducto)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            if (!formaPago.equals("CASH") && idCuenta.isEmpty()) {
                return RespuestaMB.parametrosIncorrectos();
            }

            // tipo prestamo
            Prestamo prestamo = contexto.prestamo(numeroProducto);
            String tipi = prestamo.categoria().equals("HIPOTECARIO") ? "FP-PH" : "FP-PP";

            // forma pago
            String formaPagoNemonico = "CASH".equals(formaPago) ? "EFMN" : idCuenta.startsWith("3") ? "NDMNCC" : "NDMNCA";
            Objeto formaPagoDetalle = RestPrestamo.detalleFormaPago(contexto, numeroProducto, formaPagoNemonico);

            // cuentaPago
            Cuenta cuentaPago = contexto.cuenta(idCuenta);

            if (RestPostventa.tieneSolicitudEnCurso(contexto, tipi, new Objeto().set("prestamoNumero", prestamo.numero()), true)) {
                return RespuestaMB.estado("SOLICITUD_EN_CURSO");
            }

            // api cambiar forma pago
            ApiResponseMB caso = RestPostventa.cambioFormaPagoPrestamo(contexto, tipi, prestamo, formaPagoDetalle, cuentaPago);

            if (caso == null || caso.hayError()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }

            Objeto reclamo = (Objeto) caso.get("Datos");
            String numeroCaso = reclamo.objetos().get(0).string("NumeracionCRM");

            if (numeroCaso.isEmpty()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }
            return respuesta;
        } catch (Exception e) {
            return RespuestaMB.error();
        }
    }

    public static boolean buscarSolicitudPrestamoTasaCero(String cobisId) {
        SqlRequestMB request = SqlMB.request("SelectSolicitudPrestamoTasaCero", "homebanking");
        request.sql = "SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero_2021] WHERE idCobis = ? ";
        request.sql += "UNION ";
        request.sql += "SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero] WHERE idCobis = ?";
        request.add(cobisId);
        request.add(cobisId);

        SqlResponseMB response = SqlMB.response(request);
        return !response.registros.isEmpty();
    }

    public static boolean validaModfEmailCel(ContextoMB contexto) {
        contexto.persona().email();
        contexto.persona().celular();

        Long cantidadDiasNormativoPrestamo = Long.valueOf(ConfigMB.string("cantidad_dias_normativo_prestamo", "10"));
        Date fechaTope = Fecha.restarDias(new Date(), cantidadDiasNormativoPrestamo);
        Date fechaTopeNormativo = Fecha.stringToDate(ConfigMB.string("fecha_normativo_prestamo_modif_datos"), "yyyy-MM-dd");

        if (!"CRM".equals(contexto.sesion().modificacionMailCanal()) && !"CORE".equals(contexto.sesion().modificacionMailCanal()) && contexto.sesion().modificacionMail() != null && contexto.sesion().modificacionMail().compareTo(fechaTopeNormativo) >= 0 && contexto.sesion().modificacionMail().compareTo(fechaTope) >= 0) {
            return true;
        }
        if (!"CRM".equals(contexto.sesion().modificacionCelularCanal()) && !"CORE".equals(contexto.sesion().modificacionCelularCanal()) && contexto.sesion().modificacionCelular() != null && contexto.sesion().modificacionCelular().compareTo(fechaTopeNormativo) >= 0 && contexto.sesion().modificacionCelular().compareTo(fechaTope) >= 0) {
            return true;
        }
        return false;
    }

    public static RespuestaMB ofertaPreAprobadaPP(ContextoMB contexto) {

        RespuestaMB ofertaPP = ofertaPreAprobada(contexto);
        if (ofertaPP.hayError()) {
            return RespuestaMB.exito("mostrarSegmentoPP", false);
        }
        if (ofertaPP.existe("prestamoPersonal")) {
            Objeto obj = ofertaPP.objeto("prestamoPersonal");
            return new RespuestaMB().set("enHorario", obj.bool("enHorario")).set("mostrarSegmento", obj.bool("mostrarSegmento")).set("mostrarModal", obj.bool("mostrarModal")).set("tna", obj.string("tna")).set("montoPPString", obj.string("montoPPString")).set("montoPP", obj.bigDecimal("montoPP", "0.0"));
        } else {
            return new RespuestaMB();
        }
    }

    public static RespuestaMB ofertaPreAprobada(ContextoMB contexto) {
        Boolean enHorario = enHorario(true);
        Boolean tieneAdelanto = false;
        Futuro<ApiResponseMB> responseFuturo = new Futuro<>(() -> ProductosService.getCampania(contexto));

        if (ConfigMB.bool("prendido_desembolso_online")) {
            RespuestaMB respuesta = new RespuestaMB();
            RespuestaMB desembolsoOnline = MBOmnicanalidad.solicitudesDesembolsoOnline(contexto);

            if (desembolsoOnline != null) {
                if (desembolsoOnline.existe("solicitudes")) {
                    respuesta.set("desembolso", desembolsoOnline);
                    contexto.parametros.set("nemonico", "ALERTA_DESEMBOLSO_SEGMENTO");
                    Util.contador(contexto);
                    contexto.parametros.set("nemonico", "ALERTA_DESEMBOLSO_MODAL");
                    Util.contador(contexto);
                    return respuesta;
                }
            }
        }

        ApiResponseMB response = responseFuturo.get();
        if (response.hayError()) {
            return RespuestaMB.estado("NO_POSEE_CAMPANA_PREAPROBADA");
        }

        RespuestaMB respuesta = new RespuestaMB();
        Futuro<Objeto> salidaPrestamoFuturo = new Futuro<>(() -> validaMostrarOfertaPP(response, contexto));
        Futuro<Objeto> salidaAdelantoFuturo = new Futuro<>(() -> validaMostrarOfertaA(response, contexto));

        Objeto salidaPrestamo = salidaPrestamoFuturo.get();
        Objeto salidaAdelanto = salidaAdelantoFuturo.get();

        if (salidaAdelanto != null) {
            if (salidaAdelanto.bool("mostrar") && enHorario) {
                contexto.parametros.set("nemonico", "ALERTA_ADELANTO_MODAL");
                Util.contador(contexto);
            }
            tieneAdelanto = salidaAdelanto.bool("mostrarModal");
            if (contexto.tienePPProcrearDesembolsado()) {
                salidaAdelanto.set("mostrarModal", false);
            }
            if (ConfigMB.bool("prendido_adelanto_jubilados", false) && contexto.esJubilado() && contexto.tieneCuentaCategoriaB()) {
                salidaAdelanto.set("jubiladoAptoParaAdelanto", true);
            }
            respuesta.set("adelantoBH", salidaAdelanto);
        }

        if (salidaPrestamo != null) {
            contexto.sesion().setMontoMaximoPrestamo(salidaPrestamo.bigDecimal("montoPP"));
            if (salidaPrestamo.bool("mostrarSegmento") && enHorario) {
                contexto.parametros.set("nemonico", "ALERTA_PP_SEGMENTO");
                Util.contador(contexto);
            }
            if (salidaPrestamo.bool("mostrarModal") && enHorario) {
                contexto.parametros.set("nemonico", "ALERTA_PP_MODAL");
                Util.contador(contexto);
            }
            if (tieneAdelanto) {
                salidaPrestamo.set("mostrarModal", false);
            }
            respuesta.set("prestamoPersonal", salidaPrestamo);
        }
        return respuesta;
    }

    private static Boolean enHorario(Boolean tenerEnCuentaElHorario) {
        if (tenerEnCuentaElHorario) {
            LocalTime localTime = LocalTime.now();
            return (localTime.isAfter(LocalTime.parse("06:00:00")) && localTime.isBefore(LocalTime.parse("21:00:00")));
        }
        return true;
    }

    /**
     * Valida si el cliente tiene preaprobado de prestamo personal
     **/
    private static Objeto validaMostrarOfertaPP(ApiResponseMB response, ContextoMB contexto) {
//		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Boolean mostrarOfertaPP = false;
        Boolean mostrarModalOfertaPP = false;
        Boolean tnaAplicado = false;
        String tna = "";
        Objeto salidaPP = new Objeto();

        if (!ConfigMB.bool("prendido_alta_prestamos")) {
            return null;
        }

        Objeto preAprobado = preAprobado(response, contexto);

        if (preAprobado.bigDecimal("mtoPp", "0.0").compareTo(new BigDecimal(0)) == 1) {
            mostrarOfertaPP = true;
            if (preAprobado.get("tasaAplicada") != null && (preAprobado.bigDecimal("tasaAplicada", "0.0").compareTo(new BigDecimal(0)) == 1)) {
                tnaAplicado = true;
                tna = Formateador.importeCantDecimales(preAprobado.bigDecimal("tasaAplicada"), 2);
            }
        } else {
            return salidaPP;
        }

        if (tnaAplicado && SqlPrestamos.permitidoModal(contexto, "ALERTA_OMITE_PP")) {
            mostrarModalOfertaPP = true;
        }

        Boolean enHorario = enHorario(true);
        if (ContextoMB.cambioDetectadoParaNormativoPP(contexto, false)) {
            mostrarOfertaPP = false;
            mostrarModalOfertaPP = false;
        }

        salidaPP.set("enHorario", enHorario);
        salidaPP.set("mostrarSegmento", mostrarOfertaPP);
        salidaPP.set("mostrarModal", mostrarModalOfertaPP);
        salidaPP.set("tna", tna);
        salidaPP.set("montoPPString", Formateador.importeCantDecimales(preAprobado.bigDecimal("mtoPp", "0.0"), 2));
        salidaPP.set("montoPP", preAprobado.bigDecimal("mtoPp", "0.0"));

        return salidaPP;
    }

    /**
     * Valida si el cliente tiene preaprobado de adelanto BH
     **/
    private static Objeto validaMostrarOfertaA(ApiResponseMB response, ContextoMB contexto) {
        boolean mostrarSegmento = false;
        BigDecimal montoPreAprobado = BigDecimal.ZERO;
        boolean mostrarModal = false;
        BigDecimal remanente = BigDecimal.ZERO;

        if (!ConfigMB.bool("prendido_adelanto_bh") || contexto.persona().esEmpleado()) {
            return null;
        }

        Objeto preAprobado = preAprobado(response, contexto);

        if (preAprobado.existe("saldoDisponibleAdelantoBH") && preAprobado.bigDecimal("saldoDisponibleAdelantoBH") != null && preAprobado.bigDecimal("saldoDisponibleAdelantoBH", "0.0").compareTo(BigDecimal.ZERO) > 0) {
            montoPreAprobado = preAprobado.bigDecimal("saldoDisponibleAdelantoBH");

            if (!ContextoMB.cambioDetectadoParaNormativoPP(contexto, false)) {
                remanente = ConfigMB.bool("prendido_remanente_adelanto") ? tieneRemanenteAdelantoBh(contexto, montoPreAprobado) : BigDecimal.ZERO;
                mostrarSegmento = true;
                if (SqlPrestamos.permitidoModal(contexto, "ALERTA_OMITE_ADELANTO")) {
                    mostrarModal = true;
                }
                if (contexto.tieneAdelantoActivo() && remanente == BigDecimal.ZERO) { // tiene un adelanto tomado por completo
                    mostrarModal = false;
                    mostrarSegmento = false;
                }
            }
            return new Objeto().set("mostrar", mostrarSegmento).set("mostrarModal", mostrarModal).set("aplicado", montoPreAprobado).set("aplicadoFormateado", Formateador.importeCantDecimales(montoPreAprobado, 2)).set("tieneRemanente", remanente != BigDecimal.ZERO);
        }
        return null;
    }

    private static BigDecimal tieneRemanenteAdelantoBh(ContextoMB contexto, BigDecimal montoPreAprobado) {

        BigDecimal remanente = BigDecimal.ZERO;
        BigDecimal montosAprobados = acumuladoAdelantoBh(contexto, montoPreAprobado);

        remanente = montosAprobados != BigDecimal.ZERO ? montoPreAprobado.subtract(montosAprobados) : BigDecimal.ZERO;
        if (remanente != BigDecimal.ZERO && (remanente.compareTo(ConfigMB.bigDecimal("monto_minimo_PP_adelanto")) > 0)) {
            return remanente;
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal acumuladoAdelantoBh(ContextoMB contexto, BigDecimal montoPreAprobado) {

        BigDecimal montosAprobados = BigDecimal.ZERO;
        for (Prestamo prestamo : contexto.prestamos()) {
            if (prestamo.codigo().equalsIgnoreCase("PPADELANTO")) {
                montosAprobados = montosAprobados.add(prestamo.montoAprobado());
            }
        }
        return montosAprobados;
    }

    public static RespuestaMB aceptaTerminosCondiciones(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String funcionalidad = contexto.parametros.string("funcionalidad");
        Boolean aceptaTyC = contexto.parametros.bool("aceptaTyC", false);
        String estado = "NO_APLICA";

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(idSolicitud, funcionalidad)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        contexto.sesion().setAceptaTyC(aceptaTyC);
        if (aceptaTyC && funcionalidad.equalsIgnoreCase("prestamos-personales")) {
            Solicitud.logOriginacion(contexto, idSolicitud, "TerminosYCondiciones", null, "ACEPTA_TyC_PRESTAMOS");
            estado = "0";
        }
        if (aceptaTyC && funcionalidad.equalsIgnoreCase("adelanto")) {
            Solicitud.logOriginacion(contexto, idSolicitud, "TerminosYCondiciones", null, "ACEPTA_TyC_ADELANTO");
            estado = "0";
        }
        return RespuestaMB.exito("estado", estado);
    }

    public static Objeto preAprobado(ApiResponseMB response, ContextoMB contexto) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        Objeto pp = new Objeto();
        for (Objeto obj : response.objetos()) {
            if (pp.get("fecFin") == null) {
                pp = obj;
            } else {
                try {
                    if (formato.parse(obj.string("fecFin")).after(formato.parse(pp.string("fecFin")))) {
                        pp = obj;
                    }
                } catch (Exception e) {
                    // continue;
                }
            }
        }
        return pp;
    }

    public static boolean esProcrear(ContextoMB contexto, String nemonico) {
        boolean nemonicoPrestamo = false;
        SqlResponseMB sqlResponse;
        try {
            SqlRequestMB sqlRequest = SqlMB.request("Nemónicos", "homebanking");
            sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[NemonicosProcrear] WHERE NemonicoProcrear = ?";
            sqlRequest.add(nemonico);
            sqlResponse = SqlMB.response(sqlRequest);
            nemonicoPrestamo = sqlResponse.registros.size() > 0;
        } catch (Exception e) {

        }
        return nemonicoPrestamo;
    }

    private static void datosPreaprobado(ContextoMB contexto, RespuestaMB ofertaPreAprobada, RespuestaMB respuesta) {
        Boolean tieneAdelanto = ofertaPreAprobada.existe("adelantoBH");
        Boolean tienePlanSueldo = contexto.esPlanSueldo();

        // condicion que aplica cuando es un jubilado con preaprobado de adelanto
        if (ConfigMB.bool("prendido_remanente_adelanto") && contexto.esJubilado()) {
            respuesta.set("totalPreaprobadoAdelanto", tieneAdelanto ? ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado") : null);
            respuesta.set("totalPreaprobadoAdelantoFormateado", tieneAdelanto ? Formateador.importe(ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado")) : null);

        } else if (ConfigMB.bool("prendido_remanente_adelanto")) {
            // condicion que aplica a aquellos que tienen preaprobado de adelanto
            respuesta.set("totalPreaprobadoAdelanto", tienePlanSueldo && tieneAdelanto ? ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado") : null);
            respuesta.set("totalPreaprobadoAdelantoFormateado", tienePlanSueldo && tieneAdelanto ? Formateador.importe(ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado")) : null);
        }
    }

    private static List<ProductoMoraDetalles> getProductoMoraDetalles(ContextoMB contexto, List<ProductoMora> productosEnMora) {
        List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
        productosEnMora.forEach(productoMora -> {
            ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
            if (Objects.nonNull(item)) {
                productosEnMoraDetalles.add(item);
            }
        });
        return productosEnMoraDetalles;
    }

    public static RespuestaMB consolidadaLite(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
        List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
        productosEnMora.forEach(productoMora -> {
            ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
            if (Objects.nonNull(item)) {
                productosEnMoraDetalles.add(item);
            }
        });
        for (Prestamo prestamo : contexto.prestamos()) {
            if (prestamo.detalle().hayError()) {
                AuditorLogService.prestamosLogVisualizador(contexto, "API-Ventas_ConsolidadaPrestamos", null, prestamo.detalle().json);
                return RespuestaMB.error();
            }

            Objeto item = new Objeto();
            item.set("id", prestamo.id());
            item.set("idTipoProducto", prestamo.idTipo());
            item.set("descripcion", descripcionPrestamo(prestamo, contexto.esJubilado()));
            item.set("descripcionCorta", descripcionCortaPrestamo(prestamo, contexto.esJubilado()));
            item.set("nroPrestamo", prestamo.numero());
            item.set("idMoneda", prestamo.idMoneda());
            item.set("estado", prestamo.estado());
            item.set("simboloMoneda", prestamo.simboloMoneda());
            item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
            item.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
            item.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
            item.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
            item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas());

            try {
                item.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? prestamo.cantidadCuotas() - prestamo.cuotasPendientes() - prestamo.cuotasVencidas() : 0);
            } catch (Exception e) {
                item.set("cantidadCuotasPagadas", 0);
            }

            item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
            item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
            item.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
            item.set("codigo", prestamo.codigo());
            item.set("pagable", prestamo.codigo().equals("PPADELANTO"));
            item.set("enConstruccion", prestamo.enConstruccion());
            item.set("categoria", !prestamo.categoria().equals("HIPOTECARIO") ? "personales" : "hipotecarios");
            if (!prestamo.categoria().equals("HIPOTECARIO")) {
                respuesta.add("personales", item);
            }
            if (prestamo.categoria().equals("HIPOTECARIO")) {
                respuesta.add("hipotecarios", item);
            }
            item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
            item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
            item.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
            item.set("numeroProducto", prestamo.numero());
            verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item, true);
        }

        RestPrestamo.tienePrestamosNsp(contexto, respuesta);
        return respuesta;
    }

    private static String descripcionCortaPrestamo(Prestamo prestamo, Boolean esJubilado) {
        String descripcion = tieneCategoria(prestamo);

        if (prestamo.descripcionPrestamo().contains("Crédito Refacción")) {
            descripcion = "P. Refa.";
        } else {
            if ("Personal".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "P. Pers.";
            }
            if ("Hipotecario".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "P. Hipo.";
            }
            if ("Personal".equalsIgnoreCase(prestamo.categoria()) && "Adelanto".equalsIgnoreCase(prestamo.tipo())) {
                descripcion = "A. Sueldo";
            }
            if ("Personal".equalsIgnoreCase(prestamo.categoria()) && "Adelanto".equalsIgnoreCase(prestamo.tipo()) && esJubilado) {
                descripcion = "A. Jubil";
            }
        }

        return descripcion;
    }

    public static boolean getEsPreaprobadoPP(ContextoMB contexto) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        boolean esPreaprobado = false;

        ApiResponseMB response = ProductosService.getCampania(contexto);
        if (response.hayError())
            return false;

        Objeto pp = new Objeto();
        for (Objeto obj : response.objetos()) {
            if (pp.get("fecFin") == null)
                pp = obj;
            else {
                try {
                    if (formato.parse(obj.string("fecFin")).after(formato.parse(pp.string("fecFin"))))
                        pp = obj;
                } catch (Exception e) {
                }
            }
        }

        if (pp.bigDecimal("mtoPp", "0.0").compareTo(new BigDecimal(0)) == 1)
            esPreaprobado = true;

        return esPreaprobado;
    }

    public static RespuestaMB alertaPrestamoCuotificacion(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        if (contexto.sesion().ofertaPpCuotificacionMostrada)
            return respuesta.set("alertaPPCuotificacion", false);

        if (RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false))
            return respuesta.set("alertaPPCuotificacion", false);


        Momento momentoTope = new Momento(ConfigMB.string("fecha_tope_cuotificacion", "20301231"), "yyyyMMdd");

        if (!contexto.persona().esEmpleado() && Momento.hoy().esAnterior(momentoTope) && getEsPreaprobadoPP(contexto)) {
            RespuestaMB tarjetaDebitoRespuesta = MBTarjetas.consolidadaTarjetasDebito(contexto);
            List<Cuenta> cajasDeAhorros = new ArrayList<>();

            if (tarjetaDebitoRespuesta.hayError())
                return RespuestaMB.estado("ERROR_CONSOLIDADA_TARJETAS");

            for (Objeto tarjetaDebito : tarjetaDebitoRespuesta.objetos("tarjetasDebito")) {
                if (tarjetaDebito.string("estado").equalsIgnoreCase("HABILITADA")) {
                    for (Objeto cuentaAsociada : tarjetaDebito.objetos("cuentasAsociadas")) {
                        String numeroCuenta = (String) cuentaAsociada.get("id");
                        Cuenta cuenta = contexto.cuenta(numeroCuenta);
                        if (cuenta != null && cuenta.esTitular() && cuenta.esCajaAhorro() && cuenta.estaActiva() && cuenta.esPesos())
                            cajasDeAhorros.add(cuenta);
                    }
                }

                if (!cajasDeAhorros.isEmpty()) {
                    contexto.sesion().ofertaPpCuotificacionMostrada = true;
                    respuesta.set("alertaPPCuotificacion", true);
                } else
                    respuesta.set("alertaPPCuotificacion", false);
            }
        } else
            respuesta.set("alertaPPCuotificacion", false);

        return respuesta;
    }

    public static RespuestaMB movimientosCuotificacion(ContextoMB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        boolean tieneMovimientosCuentas = false;
        BigDecimal importeMinimo = ConfigMB.bigDecimal("cuotificacion_importe_minimo", BigDecimal.ZERO);
        BigDecimal importeMaximo = ConfigMB.bigDecimal("cuotificacion_importe_maximo", BigDecimal.ZERO);
        List<String> categorias = Arrays.asList(ConfigMB.string("cuotificacion_categorias", "").split("_"));
        List<String> subCategorias = Arrays.asList(ConfigMB.string("cuotificacion_subcategorias", "").split("_"));
        List<Objeto> listaMovimientos = new ArrayList<>();

        RespuestaMB respuestaMovimientos = new RespuestaMB();
        Futuro<Boolean> futuroPreaprobado = new Futuro<>(() -> getEsPreaprobadoPP(contexto));
        Futuro<Boolean> futuroEnMora = new Futuro<>(() -> RestContexto.enMora(contexto));
        Futuro<Integer> futurotopeMaximo = new Futuro<>(
                MBPrestamo::getTopeMaximoDias);

        Futuro<RespuestaMB> futurotarjetaDebitoRespuesta = new Futuro<>(
                () -> MBTarjetas.consolidadaTarjetasDebito(contexto));

        Futuro<SqlResponseMB> fututoSqlResponse = new Futuro<>(
                () -> SqlMovimientosCuotificacion.get(contexto.idCobis(), true));

        RespuestaMB tarjetaDebitoRespuesta = futurotarjetaDebitoRespuesta.get();
        if (tarjetaDebitoRespuesta.hayError())
            return RespuestaMB.estado("ERROR_CONSOLIDADA_TARJETAS_DEBITO");

        List<Objeto> listaTarjetaDebito = tarjetaDebitoRespuesta.objetos("tarjetasDebito").stream().filter(t -> t.string("estado").equalsIgnoreCase("HABILITADA")).collect(Collectors.toList());
        List<Cuenta> cajasDeAhorros = new ArrayList<>();

        if (!listaTarjetaDebito.isEmpty() && !contexto.persona().esEmpleado() && Momento.hoy().esAnterior(new Momento(ConfigMB.string("fecha_tope_cuotificacion", "20501231"), "yyyyMMdd")) && futuroPreaprobado.get() && !futuroEnMora.get()) {
            for (Objeto tarjetaDebito : listaTarjetaDebito) {
                for (Objeto cuentaAsociada : tarjetaDebito.objetos("cuentasAsociadas")) {
                    Cuenta cuenta = contexto.cuenta(cuentaAsociada.string("id"));
                    if (cuenta != null && cuenta.esTitular() && cuenta.esCajaAhorro() && cuenta.estaActiva() && cuenta.esPesos())
                        cajasDeAhorros.add(cuenta);
                }
            }

            int topeMaximo = futurotopeMaximo.get();
            Map<String, Futuro<List<Objeto>>> futurosMovimientosCuenta = new HashMap<>();

            if (!cajasDeAhorros.isEmpty()) {
                for (Cuenta cajaAhorro : cajasDeAhorros) {
                    Futuro<List<Objeto>> futuro = new Futuro<>(
                            () -> MBCuenta.movimientosCuentaCuotificacion(contexto, cajaAhorro, Momento.hoy().restarDias(topeMaximo).string("yyyy-MM-dd"), Momento.hoy().string("yyyy-MM-dd")));
                    futurosMovimientosCuenta.put(cajaAhorro.id(), futuro);
                }

                for (Cuenta cajaAhorro : cajasDeAhorros) {
                    List<Objeto> movimientosCuenta = futurosMovimientosCuenta.get(cajaAhorro.id()).get();

                    listaMovimientos.addAll(movimientosCuenta.stream().filter(p ->
                                    categorias.contains(p.string("categoria").trim())
                                            && subCategorias.contains(p.string("subCategoria").trim())
                                            && (p.bigDecimal("importe").compareTo(importeMinimo) > 0 || p.bigDecimal("importe").equals(importeMinimo))
                                            && (p.bigDecimal("importe").compareTo(importeMaximo) < 0 || p.bigDecimal("importe").equals(importeMaximo)))
                            .toList());

                    SqlResponseMB sqlResponse = fututoSqlResponse.tryGet();
                    List<String> idsMovimientos = new ArrayList<>();
                    if (sqlResponse != null && !sqlResponse.hayError && !sqlResponse.registros.isEmpty())
                        idsMovimientos.addAll(sqlResponse.registros.stream().map(m -> m.string("id_movimiento")).toList());

                    if (!idsMovimientos.isEmpty())
                        listaMovimientos = new ArrayList<>(listaMovimientos.stream().filter(m -> !idsMovimientos.contains(m.string("numeroOperacion").trim())).toList());
                }

                tieneMovimientosCuentas = !listaMovimientos.isEmpty();

                if (tieneMovimientosCuentas) {
                    listaMovimientos.sort((Objeto o1, Objeto o2) -> {
                        String hora1 = o1.get("hora").toString();
                        String hora2 = o2.get("hora").toString();
                        DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        Date dt1;
                        Date dt2;

                        try {
                            dt1 = dFormat.parse(hora1);
                            dt2 = dFormat.parse(hora2);
                        } catch (ParseException e) {
                            return 0;
                        }
                        return dt2.compareTo(dt1);
                    });
                }
            }
        }
        return respuestaMovimientos.set("alertaPPCuotificacionMovimientos", tieneMovimientosCuentas).set("movimientos", listaMovimientos);
    }

    public static void insertarLogCuotificacion(ContextoMB contexto, Integer idSolicitud) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertarSolicitudCuotificacion", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_pp_cuotificacion] (IdSolicitud, IdCliente, Fecha) VALUES (?, ?, GETDATE())";
            sqlRequest.add(idSolicitud);
            sqlRequest.add(contexto.idCobis());
            new Futuro<>(() -> SqlMB.response(sqlRequest));
        } catch (Exception e) {
        }
    }

    public static RespuestaMB solicitarCuotificacion(ContextoMB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.empty(contexto.parametros.bigDecimal("montoCuotificacion")))
            return RespuestaMB.parametrosIncorrectos();

        if (Objeto.empty(contexto.parametros.objeto("idsMovimientos")))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuesta = MBOriginacion.solicitarPrimerOfertaPrestamo(contexto);
        if (!respuesta.hayError() && contexto.parametros.bool("primeraOferta"))
            insertarLogCuotificacion(contexto, respuesta.integer("idSolicitud"));
        return respuesta;
    }

    private static int getTopeMaximoDias() {
        int topeDefault = 30;
        try {
            SqlRequestMB sqlRequest = SqlMB.request("Cuotificacion", "homebanking");
            sqlRequest.sql += "SELECT * FROM [Homebanking].[dbo].[parametros] WHERE nombre_parametro = 'cuotificacion.topemaximodias';";
            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            if (sqlResponse.hayError || sqlResponse.registros.isEmpty())
                return topeDefault;
            return sqlResponse.registros.get(0).integer("valor");
        } catch (Exception err) {
            return topeDefault;
        }
    }

}
