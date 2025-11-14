package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Mock;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TipoOperacionPausadoCredito;

public class TarjetaCreditoService {
    //un comentario de emm que hay que borrar
    /* ========== SERVICIOS ========== */
    public static ApiResponse resumenCuenta(ContextoHB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoGetResumenCuenta", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/resumencuenta", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        request.permitirSinLogin = true;

        if (!ConfigHB.esProduccion()) {
            ApiResponse responseResumen = Api.response(request, contexto.idCobis(), numeroTarjeta);
            return responseResumen = Mock.resumenCuentaTCMock(responseResumen, contexto.idCobis());
        }

        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse autorizaciones(ContextoHB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoGetAutorizaciones", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/autorizaciones", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse cuotasPendientes(ContextoHB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoGetCuotasPendientes", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/cuotasPendientes", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse movimientos(ContextoHB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoGetMovimientos", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/movimientostarjeta", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse detalleMovimientoComercio(ContextoHB contexto, String numeroCuenta, String numeroTarjeta,
                                                        String codigo) {
        ApiRequest request = Api.request("ConsultaComerciosGetMovimientos", "tarjetascredito", "GET",
                "/v1/cuentas/{idcuenta}/comercios", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.query("codigo", codigo);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), codigo);
    }

    public static ApiResponse movimientosLiquidados(ContextoHB contexto, String numeroCuenta, String fechadesde,
                                                    String fechahasta) {
        ApiRequest request = Api.request("TarjetasCreditoGetMovimientosLiquidados", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{nroProducto}/movimientosLiquidados", contexto);
        request.path("nroProducto", numeroCuenta);
        request.query("fechadesde", fechadesde);
        request.query("fechahasta", fechahasta);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), numeroCuenta);
    }

    // emm-20190423-desde
    public static ApiResponse consultaTarjetaCredito(ContextoHB contexto, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse consultaTarjetaCreditoCache(ContextoHB contexto, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        request.permitirSinLogin = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static Respuesta pagarTarjetaCredito(ContextoHB contexto, Cuenta cuenta, TarjetaCredito tarjetaCredito,
                                                BigDecimal importe) throws Exception {
        String moneda = cuenta.idMoneda();
        String cuentaTarjeta = tarjetaCredito.cuenta();
        String tipoTarjeta = tarjetaCredito.idTipo();

        ApiRequest request = Api.request("pagoTarjetaPost", "orquestados", "POST", "/api/tarjeta/pagoTarjeta",
                contexto);

        request.body("cuenta", cuenta.numero());
        request.body("cuentaTarjeta", cuentaTarjeta);
        request.body("importe", importe);
        request.body("moneda", moneda);
        request.body("tipoTarjeta", tipoTarjeta);
        request.body("tipoCuenta", cuenta.idTipo());
        request.body("numeroTarjeta", tarjetaCredito.numero());

        ApiResponse response = null;
        try {
            response = Api.response(request, contexto.idCobis());
        } finally {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcion = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcion += response.string("codigo") + ".";
                    descripcion += response.string("mensajeAlDesarrollador") + ".";
                }
                descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

                SqlRequest sqlRequest = Sql.request("InsertAuditorPagoTarjeta", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_tarjeta] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[cuentaTarjeta],[tarjeta],[importe],[ticket]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("HB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcion); // descripcionError
                sqlRequest.add(cuenta.numero()); // cuenta
                sqlRequest.add(cuentaTarjeta); // cuentaTarjeta
                sqlRequest.add(tarjetaCredito.numero()); // tarjeta
                sqlRequest.add(importe.toString()); // importe
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("nroTicket") : null); // ticket

                Sql.response(sqlRequest);
            } catch (Exception e) {
            }
        }
        ProductosService.eliminarCacheProductos(contexto);
        if (response.hayError()) {
            if (403 == response.codigo) {
                return Respuesta.estado("FUERA_DE_HORARIO");
            }
            if (response.string("mensajeAlDesarrollador").contains("SocketTimeoutException")) {
                throw new Exception("SocketTimeoutException");
            }
            return Respuesta.error();
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
        contexto.sesion.comprobantes.put(idComprobante, comprobante);
        return Respuesta.exito("idComprobante", idComprobante);
    }
    // emm-20190423-hasta

    public static ApiResponse programarPagoTarjetaCredito(ContextoHB contexto, Cuenta cuenta,
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

        ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
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

        ApiResponse response = null;

        response = RestScheduler.crearTarea(contexto, year.toString(), date.toString(), "07", month.toString(), "0",
                "*", sRequest);

        return response;
    }

    public static ApiResponse ultimaLiquidacion(ContextoHB contexto, String numeroTarjetaCredito, String cuit) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjetaCredito);
        ApiRequest request = Api.request("TarjetaUltimaLiquidacionGETv2", "orquestados", "GET",
                "/api/tarjeta/ultimaLiquidacionv2", contexto);
        request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
        request.query("cuenta", tarjetaCredito.cuenta());
        return Api.response(request, contexto.idCobis(), numeroTarjetaCredito);
    }

    public static ApiResponse consultaTiposTarjeta(ContextoHB contexto, Integer limiteCompra, String segmento) {
        ApiRequest request = Api.request("TiposTarjetaCredito", "tarjetascredito", "GET",
                "/v1/tarjetascredito/tipoTarjeta", contexto);
        request.query("limiteCompra", limiteCompra.toString());
        request.query("segmento", segmento);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse consultaMovimientosTarjeta(ContextoHB contexto, String fechaDesde, String fechaHasta,
                                                         String numeroTarjeta, String cantMovimientos, String origen, String tipoMovimiento) {
        ApiRequest request = Api.request("MovimientosTarjetaCredito", "tarjetascredito", "GET",
                "/v1/tarjetascredito/{idtarjeta}/movimientos", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.query("cantmovimientos", cantMovimientos);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("idtarjeta", numeroTarjeta);
        request.query("origen", origen);
        request.query("tipomovimiento", tipoMovimiento);
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static SqlResponse obtenerFechaCierreVtoCartera(String cartera, Integer periodo, Integer anio) {
        SqlRequest sqlRequest = Sql.request("ObteneFechaCierreCartera", "homebanking");
        sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[fecha_cierre_tc] WHERE anio = ? and periodo = ? and cartera = ?";
        sqlRequest.add(anio);
        sqlRequest.add(periodo);
        sqlRequest.add(cartera);
        return Sql.response(sqlRequest);
    }

    public static ApiResponse stopDebit(ContextoHB contexto, String numeroCuenta) {
        ApiRequest request = Api.request("V1TarjetascreditoStopDebitByIdCuenta", "tarjetascredito", "POST",
                "/v1/cuentas/{idcuenta}/stopdebit", contexto);
        request.path("idcuenta", numeroCuenta);
        request.body("cuenta", numeroCuenta);
        request.header("x-canal", "HOMEBANKING");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse pausarTarjeta(ContextoHB contexto, TarjetaCredito tarjetaCredito,
                                            TipoOperacionPausadoCredito tipoOperacion) {
        ApiRequest request = Api.request("PausadoTarjetaCredito", "tarjetascredito", "POST",
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
            request.body(armarBodyPausado(contexto, tarjetaCredito, tipoOperacion));

        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse detallePausadoTarjeta(ContextoHB contexto, TarjetaCredito tarjetaCredito) {
        ApiRequest request = Api.request("DetallePausadoTarjetaCredito", "tarjetascredito", "POST",
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
            request.body(armarBodyPausado(contexto, tarjetaCredito, null));

        return Api.response(request, contexto.idCobis());
    }

    private static Objeto armarClient(ContextoHB contexto) {
        return new Objeto().set("document_type", contexto.persona().tipoDocumento())
                .set("document_number", contexto.persona().numeroDocumento())
                .set("gender", contexto.persona().idSexo());
    }

    private static Objeto armarCard(TarjetaCredito tarjetaCredito) {
        return new Objeto().set("bin", tarjetaCredito.numeroBin()).set("last_four_digits",
                tarjetaCredito.ultimos4digitos());
    }

    private static Objeto armarRule(TipoOperacionPausadoCredito tipoOperacion) {
        return new Objeto().set("value", tipoOperacion.getValue());
    }

    private static Objeto armarBodyPausado(ContextoHB contexto, TarjetaCredito tarjetaCredito,
                                           TipoOperacionPausadoCredito tipoOperacion) {
        Objeto body = new Objeto().set("client", armarClient(contexto)).set("card", armarCard(tarjetaCredito));
        if (!Objeto.empty(tipoOperacion))
            body.set("rule_information", armarRule(tipoOperacion));
        return body;
    }

    public static ApiResponse solicitarReimpresion(ContextoHB contexto,  String numeroCuenta, String numeroTarjeta, String idtarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoReimpresion", "tarjetascredito", "POST",
                "/v1/tarjetascredito/{idtarjeta}/reimpresion", contexto);
        request.path("idtarjeta", idtarjeta);
        request.body("numeroTarjeta", numeroTarjeta);
        request.body("cuentaTarjeta", numeroCuenta);
        request.body("ticket", Util.idProceso());
        request.header("x-canal", "HB");
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse obtenerInformacionFinanciamiento(ContextoHB contexto, TarjetaCredito tarjetaCredito,
    		String tipoDocumento, String genero, boolean tipoResumen) {
		ApiRequest request = null;
		if(!tipoResumen) {
	        	request = Api.request("TCInformacionConsumo", "tarjetascredito", "GET",
	                "/v1/consumos", contexto);
		}else{
	        	request = Api.request("TCFinanciarResumen", "tarjetascredito", "GET",
	                    "/v1/balances", contexto);
		}
		request.query("bin", tarjetaCredito.numeroBin());
		request.query("genero", genero);
		request.query("numeroDocumento", contexto.persona().numeroDocumento());
		request.query("tipoDocumento", tipoDocumento);
		request.query("ultimosCuatroDigitos", tarjetaCredito.ultimos4digitos());
	    request.cacheSesion = true;
	
		return Api.response(request, contexto.idCobis(), tarjetaCredito.numeroCuenta());
	
    }

    public static ApiResponse simularFinanciamiento(ContextoHB contexto,Objeto cliente,Objeto tarjeta,
    		String cuotas, String moneda, String monto, boolean tipoResumen){
		ApiRequest request = null;
		if(!tipoResumen) {
	        	request = Api.request("TCSimularConsumo", "tarjetascredito", "POST",
	                    "/v1/consumo-simulaciones", contexto);
		}else{
	        	request = Api.request("TCSimularResumen", "tarjetascredito", "POST",
	                    "/v1/balance-simulaciones", contexto);
		}
	    request.body("cliente", cliente);
	    request.body("cuotas", cuotas);
	    request.body("moneda", moneda);
	    request.body("monto", monto);
	    request.body("tarjeta", tarjeta);
	    request.cacheSesion = true;
	
	
		return Api.response(request, contexto.idCobis(), tarjeta.get("binTarjeta"));
    }
    
    public static ApiResponse confirmarFinanciamiento(ContextoHB contexto,Objeto cliente,Objeto tarjeta,
    		String cuotas, String moneda, String monto, boolean tipoResumen){
		ApiRequest request = null;
		if(!tipoResumen) {
	        	request = Api.request("TCConfirmarConsumo", "tarjetascredito", "POST",
	                    "/v1/consumos", contexto);
		}else{
	        	request = Api.request("TCConfirmarResumen", "tarjetascredito", "POST",
	                    "/v1/balances", contexto);
		}
	    request.body("cliente", cliente);
	    request.body("cuotas", cuotas);
	    request.body("moneda", moneda);
	    request.body("monto", monto);
	    request.body("tarjeta", tarjeta);
	    request.cacheSesion = true;
	
	
		return Api.response(request, contexto.idCobis(), tarjeta.get("binTarjeta"));
    }
    
    public static ApiResponse obtenerFinanciamientosAprobados(ContextoHB contexto, String cuenta,
    		String tipoFinanza) {
		ApiRequest request = Api.request("TCFinanciamientoAprobado", "tarjetascredito", "GET",
                "/v1/aprobadas", contexto);
	
		request.query("numeroCuenta", cuenta);
		request.query("tipoFinanza", tipoFinanza.toUpperCase());
		
		return Api.response(request, contexto.idCobis(), cuenta);
    }

    public static ApiResponse resumenCuentaV2(ContextoHB contexto, String numeroCuenta, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetasCreditoGetResumenCuenta", "tarjetascredito", "GET",
                "/v2/cuentas/{idcuenta}/resumencuenta", contexto);
        request.path("idcuenta", numeroCuenta);
        request.query("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        request.permitirSinLogin = true;

        if (!ConfigHB.esProduccion()) {
            ApiResponse responseResumen = Api.response(request, contexto.idCobis(), numeroTarjeta);
            return responseResumen = Mock.resumenCuentaTCMock(responseResumen, contexto.idCobis());
        }

        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }

    public static ApiResponse consultaTarjetaCreditoCacheV4(ContextoHB contexto, String numeroTarjeta) {
        ApiRequest request = Api.request("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v2/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.cacheSesion = true;
        request.permitirSinLogin = true;
        return Api.response(request, contexto.idCobis(), numeroTarjeta);
    }
}
