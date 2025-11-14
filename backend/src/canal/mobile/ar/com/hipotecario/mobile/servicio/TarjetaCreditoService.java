package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Mock;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TipoOperacionPausadoCredito;

public class TarjetaCreditoService {
    //un comentario de emm que hay que borrar
    /* ========== SERVICIOS ========== */
    public static ApiResponseMB resumenCuenta(ContextoMB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoGetResumenCuenta", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/resumencuenta", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;

        if (!ConfigMB.esProduccion()) {
            ApiResponseMB responseResumen = ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
            return responseResumen = Mock.resumenCuentaTCMock(responseResumen, contexto.idCobis());
        }

        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB autorizaciones(ContextoMB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoGetAutorizaciones", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/autorizaciones", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB cuotasPendientes(ContextoMB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoGetCuotasPendientes", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/cuotasPendientes", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB movimientos(ContextoMB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoGetMovimientos", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/movimientostarjeta", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB movimientosLiquidados(ContextoMB contexto, String numeroCuenta, String fechadesde,
                                                      String fechahasta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoGetMovimientosLiquidados", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{nroProducto}/movimientosLiquidados", contexto);
        request.path("nroProducto", numeroCuenta);
        request.query("fechadesde", fechadesde);
        request.query("fechahasta", fechahasta);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroCuenta);
    }

    public static ApiResponseMB detalleMovimientoComercio(ContextoMB contexto, String numeroCuenta,
                                                          String numeroTarjeta, String codigo) {
        ApiRequestMB request = ApiMB.request("ConsultaComerciosGetMovimientos", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/comercios", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.query("codigo", codigo);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), codigo);
    }

    // emm-20190423-desde
    public static ApiResponseMB consultaTarjetaCredito(ContextoMB contexto, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB consultaTarjetaCreditoCache(ContextoMB contexto, String numeroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        request.permitirSinLogin = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB consultaEResumenDigital(ContextoMB contexto, String idCuenta) {
        ApiRequestMB request = ApiMB.request("V1CuentasGetByIdcuenta", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/debitosAdheridos", contexto);
        request.path("idcuenta", idCuenta);

        return ApiMB.response(request, contexto.idCobis(), idCuenta);
    }

    public static ApiResponseMB altaEResumenDigital(ContextoMB contexto, String idCuenta) {
        ApiRequestMB request = ApiMB.request("V1CuentasPatchByIdcuenta", "tarjetascredito", "PATCH",
                "/v1/cuentas/{idcuenta}", contexto);
        Objeto body = new Objeto();
        if (idCuenta != null && !idCuenta.isEmpty()) {
            request.path("idcuenta", idCuenta);
            body.set("cuenta", idCuenta);
        }
        body.set("email", contexto.parametros.string("email"));
        body.set("modoEresumen", contexto.parametros.string("modoResumenDigital"));
        request.body(body);
        return ApiMB.response(request, contexto.idCobis(), idCuenta);
    }

    public static RespuestaMB pagarTarjetaCredito(ContextoMB contexto, Cuenta cuenta, TarjetaCredito tarjetaCredito,
                                                  BigDecimal importe) {

        String moneda = cuenta.idMoneda();
        String cuentaTarjeta = tarjetaCredito.cuenta();
        String tipoTarjeta = tarjetaCredito.idTipo();

        ApiRequestMB request = ApiMB.request("pagoTarjetaPost", "orquestados", "POST", "/api/tarjeta/pagoTarjeta",
                contexto);

        request.body("cuenta", cuenta.numero());
        request.body("cuentaTarjeta", cuentaTarjeta);
        request.body("importe", importe);
        request.body("moneda", moneda);
        request.body("tipoTarjeta", tipoTarjeta);
        request.body("tipoCuenta", cuenta.idTipo());
        request.body("numeroTarjeta", tarjetaCredito.numero());

        ApiResponseMB response = null;
        try {
            response = ApiMB.response(request, contexto.idCobis());
        } finally {
            if (ConfigMB.bool("log_transaccional", false)) {
                try {
                    String codigoError = response == null ? "ERROR"
                            : response.hayError() ? response.string("codigo") : "0";

                    String descripcion = "";
                    if (response != null && !codigoError.equals("0")) {
                        descripcion += response.string("codigo") + ".";
                        descripcion += response.string("mensajeAlDesarrollador") + ".";
                    }
                    descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

                    SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorPagoTarjeta", "hbs");
                    sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_tarjeta] ";
                    sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[cuentaTarjeta],[tarjeta],[importe],[ticket]) ";
                    sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    sqlRequest.add(new Date()); // momento
                    sqlRequest.add(contexto.idCobis()); // cobis
                    sqlRequest.add(request.idProceso()); // idProceso
                    sqlRequest.add(request.ip()); // ip
                    sqlRequest.add("MB"); // canal
                    sqlRequest.add(codigoError); // codigoError
                    sqlRequest.add(descripcion); // descripcionError
                    sqlRequest.add(cuenta.numero()); // cuenta
                    sqlRequest.add(cuentaTarjeta); // cuentaTarjeta
                    sqlRequest.add(tarjetaCredito.numero()); // tarjeta
                    sqlRequest.add(importe.toString()); // importe
                    sqlRequest.add(codigoError.equals("0") ? response.string("nroTicket") : null); // ticket

                    SqlMB.response(sqlRequest);
                } catch (Exception e) {
                }
            }
        }
        ProductosService.eliminarCacheProductos(contexto);
        if (response.hayError()) {
            if (403 == response.codigo) {
                return RespuestaMB.estado("FUERA_DE_HORARIO");
            }
            return RespuestaMB.error();
        }

        Date hoy = new Date();
        String sImporte = request.body().string("importe");
        String numeroTarjeta = tarjetaCredito.ultimos4digitos();
        if (numeroTarjeta == null) {
            numeroTarjeta = "";
        }
        String tipoTarjetaDescripcion = tarjetaCredito.tipo();
        if (tipoTarjetaDescripcion == null) {
            tipoTarjetaDescripcion = "";
        }
        tipoTarjetaDescripcion = tipoTarjetaDescripcion.toUpperCase();

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("ID", response.string("nroTicket"));
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(hoy));
        comprobante.put("CUENTA_ORIGEN", request.body().string("cuenta"));
        comprobante.put("IMPORTE", (request.body().string("moneda").equals("80") ? "$" : "USD") + " "
                + Formateador.importe(new BigDecimal(sImporte)));
        comprobante.put("TIPO_Y_NUMERO_TARJETA", tipoTarjetaDescripcion + " XXXX-" + numeroTarjeta);

        String idComprobante = "tarjeta-credito" + "_" + response.string("nroTicket");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        return RespuestaMB.exito("idComprobante", idComprobante);
    }
    // emm-20190423-hasta

    public static ApiResponseMB programarPagoTarjetaCredito(ContextoMB contexto, Cuenta cuenta,
                                                            TarjetaCredito tarjetaCredito, BigDecimal importe) {

        String moneda = cuenta.idMoneda();
        String cuentaTarjeta = tarjetaCredito.cuenta();
        String tipoTarjeta = tarjetaCredito.idTipo();

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#####0.00", decimalFormatSymbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        String sImporte = decimalFormat.format(importe);
        String sRequest = "{\"cuenta\":\"" + cuenta.numero() + "\",\"cuentaTarjeta\":\"" + cuentaTarjeta
                + "\",\"importe\":" + sImporte + ",\"moneda\":\"" + moneda + "\",\"tipoTarjeta\":\"" + tipoTarjeta
                + "\",\"tipoCuenta\":\"" + cuenta.idTipo() + "\",\"numeroTarjeta\":\"" + tarjetaCredito.numero()
                + "\"}";

        Calendar fechaAuxiliar = Calendar.getInstance();

        ApiResponseMB responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCalendario.hayError()) { // si dio error le devuelvo que está en horario para que desde front
            // funcione
            // como siempre
            return null;
        }
        Date diaHabilPosterior = responseCalendario.objetos().get(0).date("diaHabilPosterior", "yyyy-MM-dd");
        boolean esDiaHabil = responseCalendario.objetos().get(0).string("esDiaHabil") == "1" ? true : false;
        Integer year = fechaAuxiliar.get(Calendar.YEAR);
        Integer date = fechaAuxiliar.get(Calendar.DATE);
        Integer hour = fechaAuxiliar.get(Calendar.HOUR_OF_DAY);
        Integer month = fechaAuxiliar.get(Calendar.MONTH) + 1;

        if (hour > 7 || !esDiaHabil) {
            fechaAuxiliar.setTime(diaHabilPosterior);

            year = fechaAuxiliar.get(Calendar.YEAR);
            date = fechaAuxiliar.get(Calendar.DATE);
            month = fechaAuxiliar.get(Calendar.MONTH) + 1;
        }

        ApiResponseMB response = null;

        response = RestScheduler.crearTarea(contexto, year.toString(), date.toString(), "07", month.toString(), "0",
                "*", sRequest);

        return response;
    }

    // emm-20190514-desde

    public static ApiResponseMB ultimaLiquidacion(ContextoMB contexto, String numeroTarjetaCredito, String cuit) {
        Boolean habilitarV2 = false;
        habilitarV2 |= ConfigMB.string("prendido_ultimaliquidacionv2", "").equals("true");
        habilitarV2 |= Objeto.setOf(ConfigMB.string("prendido_ultimaliquidacionv2_cobis", "").split("_"))
                .contains(contexto.idCobis());
        if (!habilitarV2) {
            ApiRequestMB request = ApiMB.request("TarjetaUltimaLiquidacionGET", "orquestados", "GET",
                    "/api/tarjeta/ultimaLiquidacion", contexto);
            request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
            request.query("NroTarjeta", numeroTarjetaCredito);
            request.query("cuil", cuit);
            return ApiMB.response(request, contexto.idCobis(), numeroTarjetaCredito);
        } else {
            TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjetaCredito);
            ApiRequestMB request = ApiMB.request("TarjetaUltimaLiquidacionGETv2", "orquestados", "GET",
                    "/api/tarjeta/ultimaLiquidacionv2", contexto);
            request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
            request.query("cuenta", tarjetaCredito.cuenta());
            return ApiMB.response(request, contexto.idCobis(), numeroTarjetaCredito);
        }
    }
    // emm-20190514-hasta

    public static ApiResponseMB consultaTiposTarjeta(ContextoMB contexto, Integer limiteCompra, String segmento) {
        ApiRequestMB request = ApiMB.request("TiposTarjetaCredito", "tarjetascredito", "GET",
                "/v1/tarjetascredito/tipoTarjeta", contexto);
        request.query("limiteCompra", limiteCompra.toString());
        request.query("segmento", segmento);
        return ApiMB.response(request, contexto.idCobis());
    }

    // OJO que no consulta sino que da la baja de las tarjetas
    public static ApiResponseMB consultaTarjetasDadasBaja(ContextoMB contexto, String cuentaAsociada,
                                                          String numeroTarjetaCredito) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoBaja", "tarjetascredito", "GET",
                "/v1/bajaTarjetaCredito/{marcaCodi}/cuenta/{cuenta}/{tarjeNume}", contexto);
        request.path("marcaCodi", "2");
        request.path("cuenta", cuentaAsociada);
        request.path("tarjeNume", numeroTarjetaCredito);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultaMovimientosTarjeta(ContextoMB contexto, String fechaDesde, String fechaHasta,
                                                           String numeroTarjeta, String cantMovimientos, String origen, String tipoMovimiento) {
        ApiRequestMB request = ApiMB.request("MovimientosTarjetaCredito", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}/movimientos", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.query("cantmovimientos", cantMovimientos);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("idtarjeta", numeroTarjeta);
        request.query("origen", origen);
        request.query("tipomovimiento", tipoMovimiento);
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponseMB stopDebit(ContextoMB contexto, String numeroCuenta) {
        ApiRequestMB request = ApiMB.request("V1TarjetascreditoStopDebitByIdCuenta", "tarjetascredito", "POST",
                "/v1/cuentas/{idcuenta}/stopdebit", contexto);
        request.path("idcuenta", numeroCuenta);
        request.body("cuenta", numeroCuenta);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB altaEResumenStopDebit(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("V1CuentasPatchByIdcuenta", "tarjetascredito", "POST",
                "/v1/cuentas/{idcuenta}/stopdebit", contexto);
        Objeto body = new Objeto();
        request.path("cuenta", contexto.parametros.string("idCuenta"));
        body.set("cuenta", contexto.parametros.string("idCuenta"));
        body.set("email", contexto.parametros.string("email"));
        body.set("modoEresumen", contexto.parametros.string("modoResumenDigital"));
        request.body(body);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consolidada(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("V1TarjetascreditoConsolidadaGetByIdtarjeta", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}/consolidada", contexto);
        request.path("idtarjeta", contexto.parametros.string("idTarjeta"));
        request.query("idcuenta", contexto.parametros.string("idCuenta"));
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultaCvvPrisma(ContextoMB contexto, TarjetaCredito tarjetaCredito) {
        ApiRequestMB request = ApiMB.request("TarjetascreditoCVV", "tarjetascredito", "POST", "/v1/tarjetascredito/cvv",
                contexto);

        request.body(armarBodyPausado(contexto, tarjetaCredito, null, true));

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB tarjetaCreditoVirtualToFisica(ContextoMB contexto, TarjetaCredito tarjetaCredito) {
        ApiRequestMB request = ApiMB.request("solicitudPlasticoTCV", "tarjetascredito", "POST",
                "/v1/tarjetascredito/virtual/solicitudPlastico", contexto);

        Objeto tdModel = new Objeto();
        tdModel.set("cuenta", tarjetaCredito.numeroCuenta());
        tdModel.set("ticket", tarjetaCredito.numeroCuenta());
        request.body(tdModel);

        return ApiMB.response(request, contexto.idCobis());
    }

    public static SqlResponseMB obtenerFechaCierreVtoCartera(String cartera, Integer periodo, Integer anio) {
        SqlRequestMB sqlRequest = SqlMB.request("ObteneFechaCierreCartera", "homebanking");
        sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[fecha_cierre_tc] WHERE anio = ? and periodo = ? and cartera = ?";
        sqlRequest.add(anio);
        sqlRequest.add(periodo);
        sqlRequest.add(cartera);
        return SqlMB.response(sqlRequest);
    }

    public static ApiResponseMB pausarTarjeta(ContextoMB contexto, TarjetaCredito tarjetaCredito,
                                              TipoOperacionPausadoCredito tipoOperacion) {
        ApiRequestMB request = ApiMB.request("PausadoTarjetaCredito", "tarjetascredito", "POST",
                "/v1/tarjetascredito/pausado", contexto);

        if ((contexto.esDesarrollo() || contexto.esHomologacion()) && (contexto.idCobis().equals("5841097") || contexto.idCobis().equals("6318754"))) {
            Objeto body = new Objeto();
            Objeto client = new Objeto();
            Objeto card = new Objeto();
            Objeto rule = new Objeto();
            // Usuario OK
            if (contexto.idCobis().equals("5841097")) {
                if (tarjetaCredito.numero().equals("4304970016546037")) {
                    // TARJETA NO PAUSADA
                    request.headers.put("codigoBanco", "890");
                    client.set("document_type", "DNI")
                            .set("document_number", "99999999")
                            .set("gender", "F");
                    card.set("bin", "444444")
                            .set("last_four_digits", "4000");
                    rule.set("rule", "status")
                            .set("value", "DISABLED");
                    body.set("client", client).set("card", card).set("rule_information", rule);
                } else {
                    // TARJETA PAUSADA
                    request.headers.put("codigoBanco", "900");
                    client.set("document_type", "DNI")
                            .set("document_number", "12345678")
                            .set("gender", "F");
                    card.set("bin", "123456")
                            .set("last_four_digits", "1234");
                    rule.set("rule", "status")
                            .set("value", "ENABLED");
                    body.set("client", client).set("card", card).set("rule_information", rule);
                }
            }
            // USUARIO ERROR
            if (contexto.idCobis().equals("6318754")) {
                request.headers.put("codigoBanco", "900");
                client.set("document_type", "DNI")
                        .set("document_number", "99999999")
                        .set("gender", "M");
                card.set("bin", "444444")
                        .set("last_four_digits", "4321");
                rule.set("rule", "status")
                        .set("value", "DISABLED");
                body.set("client", client).set("card", card).set("rule_information", rule);
            }
            request.body(body);
        } else
            request.body(armarBodyPausado(contexto, tarjetaCredito, tipoOperacion, false));

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB detallePausadoTarjeta(ContextoMB contexto, TarjetaCredito tarjetaCredito) {
        ApiRequestMB request = ApiMB.request("DetallePausadoTarjetaCredito", "tarjetascredito", "POST",
                "/v1/tarjetascredito/pausado/detalle", contexto);

        if ((contexto.esDesarrollo() || contexto.esHomologacion()) && (contexto.idCobis().equals("5841097") || contexto.idCobis().equals("6318754"))) {
            Objeto body = new Objeto();
            Objeto client = new Objeto();
            Objeto card = new Objeto();
            request.headers.put("codigoBanco", "352");
            // Usuario OK
            if (contexto.idCobis().equals("5841097")) {
                if (tarjetaCredito.numero().equals("4304970016546037")) {
                    // TARJETA NO PAUSADA
                    client.set("document_type", "DNI")
                            .set("document_number", "12345678")
                            .set("gender", "F");
                    card.set("bin", "123456")
                            .set("last_four_digits", "1234");
                    body.set("client", client).set("card", card);
                } else {
                    // TARJETA PAUSADA
                    client.set("document_type", "DNI")
                            .set("document_number", "99999999")
                            .set("gender", "F");
                    card.set("bin", "444444")
                            .set("last_four_digits", "6000");
                    body.set("client", client).set("card", card);
                }
            }
            // USUARIO ERROR
            if (contexto.idCobis().equals("6318754")) {
                client.set("document_type", "DNI")
                        .set("document_number", "99999999")
                        .set("gender", "F");
                card.set("bin", "444444")
                        .set("last_four_digits", "6000");
                body.set("client", client).set("card", card);
            }
            request.body(body);
        } else
            request.body(armarBodyPausado(contexto, tarjetaCredito, null, false));

        return ApiMB.response(request, contexto.idCobis());
    }

    private static Objeto armarClient(ContextoMB contexto) {
        return new Objeto().set("document_type", contexto.persona().tipoDocumento())
                .set("document_number", contexto.persona().numeroDocumento())
                .set("gender", contexto.persona().idSexo());
    }

    private static Objeto armarCard(TarjetaCredito tarjetaCredito) {
        return new Objeto().set("bin", tarjetaCredito.numeroBin()).set("last_four_digits",
                tarjetaCredito.ultimos4digitos());
    }

    private static Objeto armarCardCvv(TarjetaCredito tarjetaCredito) {
        return new Objeto().set("bin", tarjetaCredito.numeroBin()).set("last_four_digits",
                tarjetaCredito.ultimos4digitos()).set("expiration_date", tarjetaCredito.fechaVigenciaHasta("MMyy"));
    }

    private static Objeto armarRule(TipoOperacionPausadoCredito tipoOperacion) {
        return new Objeto().set("value", tipoOperacion.getValue());
    }

    private static Objeto armarBodyPausado(ContextoMB contexto, TarjetaCredito tarjetaCredito,
                                           TipoOperacionPausadoCredito tipoOperacion, boolean esCvv) {
        Objeto body = new Objeto().set("client", armarClient(contexto));
        body.set("card", esCvv ? armarCardCvv(tarjetaCredito) : armarCard(tarjetaCredito));
        if (!Objeto.empty(tipoOperacion))
            body.set("rule_information", armarRule(tipoOperacion));
        return body;
    }
    
    public static ApiResponseMB obtenerInformacionFinanciamiento(ContextoMB contexto, TarjetaCredito tarjetaCredito,
    		String tipoDocumento, String genero, boolean tipoResumen) {
    	ApiRequestMB request = null;
    	if(!tipoResumen) {
    		request = ApiMB.request("TCInformacionConsumo", "tarjetascredito", "GET",
                "/v1/consumos", contexto);
    	}else {
    		request = ApiMB.request("TCFinanciarResumen", "tarjetascredito", "GET",
                    "/v1/balances", contexto);
    	}
        request.query("bin", tarjetaCredito.numeroBin());
	request.query("genero", genero);
	request.query("numeroDocumento", contexto.persona().numeroDocumento());
	request.query("tipoDocumento", tipoDocumento);
	request.query("ultimosCuatroDigitos", tarjetaCredito.ultimos4digitos());
        request.cacheSesion = true;

        return ApiMB.response(request, contexto.idCobis(), tarjetaCredito.numeroCuenta());
    }
    
    public static ApiResponseMB simularFinanciamiento(ContextoMB contexto, Objeto cliente,Objeto tarjeta,
    		String cuotas, String moneda, String monto, boolean tipoResumen) {
    	
    	ApiRequestMB request = null;
    	if(!tipoResumen) {
    		request = ApiMB.request("TCSimularConsumo", "tarjetascredito", "POST",
                "/v1/consumo-simulaciones", contexto);
    	}else {
    		request = ApiMB.request("TCSimularResumen", "tarjetascredito", "POST",
                    "/v1/balance-simulaciones", contexto);
    	}
        request.body("cliente", cliente);
        request.body("cuotas", cuotas);
        request.body("moneda", moneda);
        request.body("monto", monto);
        request.body("tarjeta", tarjeta);
        request.cacheSesion = true;

        return ApiMB.response(request, contexto.idCobis(), tarjeta.get("binTarjeta"));
    }
    
    public static ApiResponseMB confirmarFinanciamiento(ContextoMB contexto, Objeto cliente,Objeto tarjeta,
    		String cuotas, String moneda, String monto, boolean tipoResumen) {
    	ApiRequestMB request = null;
    	if(!tipoResumen) {
    		request = ApiMB.request("TCConfirmarConsumo", "tarjetascredito", "POST",
                "/v1/consumos", contexto);
    	}else {
    		request = ApiMB.request("TCConfirmarResumen", "tarjetascredito", "POST",
                    "/v1/balances", contexto);
    	}
        request.body("cliente", cliente);
        request.body("cuotas", cuotas);
        request.body("moneda", moneda);
        request.body("monto", monto);
        request.body("tarjeta", tarjeta);
        request.cacheSesion = true;

        return ApiMB.response(request, contexto.idCobis(), tarjeta.get("binTarjeta"));
    }
    
    public static ApiResponseMB obtenerFinanciamientosAprobados(ContextoMB contexto, String nroCuenta,
    		String tipoFinanza) {
    	ApiRequestMB request = ApiMB.request("TCFinanciamientoAprobado", "tarjetascredito", "GET",
                "/v1/aprobadas", contexto);

		request.query("numeroCuenta", nroCuenta);
		request.query("tipoFinanza", tipoFinanza.toUpperCase());
        request.cacheSesion = true;

        return ApiMB.response(request, contexto.idCobis(), nroCuenta);
    }
    
    public static ApiResponseMB solicitarReimpresion(ContextoMB contexto, String numeroCuenta, String numeroTarjeta, String idTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasCreditoReimpresion", "tarjetascredito", "POST",
                "/v1/tarjetascredito/{idtarjeta}/reimpresion", contexto);
        request.path("idtarjeta", idTarjeta);
        request.body("numeroTarjeta", numeroTarjeta);
        request.body("cuentaTarjeta", numeroCuenta);
        request.body("ticket", Util.idProceso());
        request.header("x-canal", "MB");
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
    }
    
}
