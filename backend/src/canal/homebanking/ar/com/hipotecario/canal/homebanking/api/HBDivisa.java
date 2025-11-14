package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cotizacion;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.InversionesService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;
import ar.com.hipotecario.canal.homebanking.servicio.TransmitHB;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.VentaUsdHBBMBankProcess;
import ar.gabrielsuarez.glib.G;

public class HBDivisa {

	public static Respuesta cotizaciones(ContextoHB contexto) {
		Objeto cotizaciones = new Objeto();

		Respuesta horarioDolarResp = HBTransferencia.compraVentaDolarFueraDeHorario(contexto);
		boolean enHorarioDolar = !horarioDolarResp.hayError();
		boolean cobisFueraHorario = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "dolarFueraHorario");
		boolean sinCotizaciones = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "dolarSinCotizaciones");

		Futuro<ApiResponse> futuroResponse = new Futuro<>(() -> InversionesService.inversionesGetCotizaciones(contexto));
		Futuro<ApiResponse> futuroResponseUva = new Futuro<>(() -> InversionesService.inversionesGetCotizaciones(contexto, "88"));

		ApiResponse response = futuroResponse.get();
		ApiResponse responseUva = futuroResponseUva.get();

		Objeto dolar = new Objeto();
		dolar.set("moneda", "Dolares");

		if (sinCotizaciones) {
			// 2) Para este usuario, fuerza null y “-”
			dolar.set("compra", null);
			dolar.set("compraFormateado", "-");
			dolar.set("venta",  null);
			dolar.set("ventaFormateado", "-");
		} else {
			// 3) Para el resto, comportamiento normal
			BigDecimal compra = Cotizacion.dolarCompra(contexto, response);
			BigDecimal venta  = Cotizacion.dolarVenta (contexto, response);
			dolar.set("compra", compra);
			dolar.set("compraFormateado",
					compra  != null ? Formateador.importe(compra) : "-");
			dolar.set("venta",  venta);
			dolar.set("ventaFormateado",
					venta   != null ? Formateador.importe(venta)  : "-");
		}

		boolean esDiaHabil = Util.isDiaHabil(contexto);
		boolean mostrarEnHorario = !cobisFueraHorario && enHorarioDolar && esDiaHabil;


		dolar.set("enHorario", mostrarEnHorario);
		dolar.set("fueraDeHorarioDescripcion", "Te recordamos que el horario para operar es de lunes a viernes (excepto feriados) de 07:00 a 21:00 hs.");

		cotizaciones.set("dolar", dolar);

		Objeto euro = new Objeto();
		euro.set("moneda", "Euros");
		euro.set("compra", Cotizacion.euroCompra(contexto, response));
		euro.set("compraFormateado", Formateador.importe(Cotizacion.euroCompra(contexto, response)));
		euro.set("venta", Cotizacion.euroVenta(contexto, response));
		euro.set("ventaFormateado", Formateador.importe(Cotizacion.euroVenta(contexto, response)));
		cotizaciones.set("euro", euro);

		Objeto uva = new Objeto();
		uva.set("moneda", "UVAs");
		uva.set("compra", Cotizacion.uvaCompra(contexto, responseUva));
		uva.set("compraFormateado", Formateador.importe(Cotizacion.uvaCompra(contexto, responseUva)));
		uva.set("venta", Cotizacion.uvaVenta(contexto, responseUva));
		uva.set("ventaFormateado", Formateador.importe(Cotizacion.uvaVenta(contexto, responseUva)));
		cotizaciones.set("uva", uva);

//		List<Objeto> otrosImpuestos = new ArrayList<>();
//		Objeto impuestoPais = new Objeto();
//		impuestoPais.set("nombre","IMPUESTO PAÍS");
//		impuestoPais.set("valor",30.00);
//		impuestoPais.set("valorFormateado",Formateador.importe(new BigDecimal(30)));
//		otrosImpuestos.add(impuestoPais);
//		Objeto impuestoRg4815 = new Objeto();
//		impuestoRg4815.set("nombre","Percepción RG4815");
//		impuestoRg4815.set("valor",30.00);
//		impuestoRg4815.set("valorFormateado",Formateador.importe(new BigDecimal(30)));
//		otrosImpuestos.add(impuestoRg4815);
//		cotizaciones.set("otrosImpuestos",otrosImpuestos);

		Objeto otrosImpuestos = cotizaciones.set("otrosImpuestos");

		Objeto impuestoPais = new Objeto();
		impuestoPais.set("nombre", "IMPUESTO PAÍS");
		BigDecimal impuestoCompraVentaDolar = ConfigHB.bigDecimal("porcentaje_impuesto_compra_venta_dolares", new BigDecimal(0));
		impuestoPais.set("valor", impuestoCompraVentaDolar);
		impuestoPais.set("valorFormateado", Formateador.importe(impuestoCompraVentaDolar));
		otrosImpuestos.add(impuestoPais);

		Objeto impuestoRg4815 = new Objeto();
		impuestoRg4815.set("nombre", "Percepción RG 5617/2024 (ARCA)");
		impuestoRg4815.set("valor", 0.00);
		impuestoRg4815.set("valorFormateado", Formateador.importe(new BigDecimal(0)));
		otrosImpuestos.add(impuestoRg4815);

		return Respuesta.exito("cotizaciones", cotizaciones);
	}

	public static Respuesta simularCompraVentaDolares(ContextoHB contexto) {
		String idCuentaOrigen = contexto.parametros.string("idCuentaOrigen");
		String idCuentaDestino = contexto.parametros.string("idCuentaDestino");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");

		contexto.sesion.setChallengeOtp(false);

		if (Objeto.anyEmpty(idCuentaOrigen, idCuentaDestino, monto)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuentaOrigen = contexto.cuenta(idCuentaOrigen);
		Cuenta cuentaDestino = contexto.cuenta(idCuentaDestino);

		if (cuentaOrigen == null || cuentaDestino == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}
		if (cuentaOrigen.idMoneda().equals(cuentaDestino.idMoneda())) {
			return Respuesta.estado("MISMA_MONEDA");
		}

		Api.eliminarCache(contexto, "SimularCompraVentaDolares", idCuentaOrigen, idCuentaDestino, monto);
		ApiRequest requestSimulacion = null;
		// CuentasPostTransferenciasSimularDolares
		requestSimulacion = Api.request("SimularCompraVentaDolares", "cuentas", "POST", "/v2/cuentas/{idcuenta}/transferencias", contexto);
		requestSimulacion.path("idcuenta", cuentaOrigen.numero());
		requestSimulacion.query("cuentapropia", "true");
		requestSimulacion.query("inmediata", "false");
		requestSimulacion.query("especial", "false");

		requestSimulacion.body("cotizacion", "0");
		requestSimulacion.body("importe", monto);
		requestSimulacion.body("importeDivisa", "0");
		requestSimulacion.body("importePesos", "0");
		requestSimulacion.body("modoSimulacion", "true");
		requestSimulacion.body("reverso", false);
		requestSimulacion.body("cuentaOrigen", cuentaOrigen.numero());
		requestSimulacion.body("cuentaDestino", cuentaDestino.numero());
		requestSimulacion.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
		requestSimulacion.body("tipoCuentaDestino", cuentaDestino.idTipo());
		requestSimulacion.body("idMoneda", cuentaOrigen.idMoneda());
		requestSimulacion.body("idMonedaDestino", cuentaDestino.idMoneda());
		requestSimulacion.body("descripcionConcepto", "VAR");
		requestSimulacion.body("idCliente", contexto.idCobis());
		requestSimulacion.cacheSesion = false;

		ApiResponse response = Api.response(requestSimulacion, idCuentaOrigen, idCuentaDestino, monto);

//		if (!Config.esProduccion() && contexto.idCobis().equals("1475715")) {
//			response = TestApiDivisa.mockTopeSubsidio();
//		}

		if (response.hayError()) {
			if ("127012".equals(response.string("codigo"))) {
				return Respuesta.estado("OPERACION_INHABILITADA");
			}
			if ("127013".equals(response.string("codigo"))) {
				return Respuesta.estado("OPERACION_INHABILITADA");
			}
			if ("128027".equals(response.string("codigo"))) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if ("141018".equals(response.string("codigo"))){
				return estadoConCustomError("MESA_DE_CAMBIOS_DESHABILITADA");
			}
			if ("123008".equals(response.string("codigo"))) {

				if ("CLIENTE POSEE REFERENCIAS EXTERNAS, CONSULTAR CON UPC".contains(response.string("mensajeAlUsuario"))) {
					return Respuesta.estado("NO_OPERAR_CA_USD");
				}

				if ("CLIENTE POSEE REFERENCIAS EXTERNAS".contains(response.string("mensajeAlUsuario")) || "Cuenta bloqueada".contains(response.string("mensajeAlDesarrollador"))) {
					return Respuesta.estado("NO_OPERAR_CA_USD");
				}
				return Respuesta.estado("OPERACION_NO_POSIBLE");
			}
			if ("123015".equals(response.string("codigo"))) {
				return Respuesta.estado("OPERACION_NO_POSIBLE_A_7001_BCRA");
			}
			if ("128029".equals(response.string("codigo"))) {
				return Respuesta.estado("SOLO_TITULAR");
			}
			if ("201008".equals(response.string("codigo"))) {
				return Respuesta.estado("NO_OPERAR_CA_USD");
			}
			if ("400".equals(response.string("codigo"))) {
				if (response.string("mensajeAlUsuario").toLowerCase().contains("3 - excedido otra causal")) {
					return Respuesta.estado("TOPE_SUBSIDIO");
				}
				return Respuesta.estado("OPERACION_NO_POSIBLE_400");
			}

			return Respuesta.error();
		}

		Objeto simulacion = new Objeto();
		simulacion.set("cotizacion", response.bigDecimal("cotizacion"));
		simulacion.set("cotizacionFormateada", Formateador.importe(response.bigDecimal("cotizacion")));
		simulacion.set("importe", response.bigDecimal("importe"));
		simulacion.set("importeFormateado", Formateador.importe(response.bigDecimal("importe")));
		simulacion.set("importePesos", response.bigDecimal("importePesos"));
		simulacion.set("importePesosFormateado", Formateador.importe(response.bigDecimal("importePesos")));
		simulacion.set("importeDolares", response.bigDecimal("importeDivisa"));
		simulacion.set("importeDolaresFormateado", Formateador.importe(response.bigDecimal("importeDivisa")));
		simulacion.set("importePesosImpuesto", response.bigDecimal("importeTotal")); // emm: Agrego el importe con
		// impuesto
		simulacion.set("importePesosImpuestoFormateado", Formateador.importe(response.bigDecimal("importeTotal"))); // emm:
		// Agrego
		// el
		// importe
		// con
		// impuesto
		simulacion.set("importeImpRg4815", response.bigDecimal("impRg4815")); // emm: Agrego el importe con impuesto
		simulacion.set("importeImpRg4815Formateado", Formateador.importe(response.bigDecimal("impRg4815"))); // emm:
		// Agrego
		// el
		// importe
		// con
		// impuesto

		simulacion.set("importeImpRg5430", response.bigDecimal("impRg5430")); // emm: Agrego el importe con impuesto
		simulacion.set("importeImpRg5430Formateado", Formateador.importe(response.bigDecimal("impRg5430")));

		// emm-20190912-leyenda: Sólo si es venta de dólares
		if ("80".equals(cuentaOrigen.idMoneda())) {
			String leyenda = ConfigHB.string("leyenda_compra_venta_dolares", "");
			leyenda = leyenda.replace("MONTO_DOLARES", Formateador.importe(response.bigDecimal("importeDivisa")));
			simulacion.set("leyenda", leyenda);
		} else {
			simulacion.set("leyenda", "");
		}

		return Respuesta.exito("simulacion", simulacion);

//		{
//		  "operacion" : "307051000050124",
//		  "transaccion" : "4780709",
//		  "cotizacion" : "15.2",
//		  "importe" : 1520.0,
//		  "fecha" : "2019/04/02",
//		  "servicio" : "TRANSF.TERCEROS",
//		  "referencia" : "307051000050124",
//		  "idMonedaDestino" : "2",
//		  "cuentaDestino" : "205100013927615",
//		  "cuentaOrigen" : "405100012723564",
//		  "tipoCuentaDestino" : "AHO",
//		  "tipoCuentaOrigen" : "AHO",
//		  "importeDivisa" : 100.0,
//		  "importePesos" : 1520.0,
//		  "monedaOrigen" : "80",
//		  "estado" : "P",
//		  "idCobis" : "32595319",
//		  "idProceso" : "9151240",
//		  "monto" : "1520.00",
//		  "numeroError" : "0",
//		  "recibo" : "307051000050124",
//		  "impuestos" : "0.00",
//		  "comision" : "0.00",
//		  "totalTransferencia" : "1520.00"
//		}
	}

	public static Respuesta compraVentaDolares(ContextoHB contexto) {
		String idCuentaOrigen = contexto.parametros.string("idCuentaOrigen");
		String idCuentaDestino = contexto.parametros.string("idCuentaDestino");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		BigDecimal cotizacion = contexto.parametros.bigDecimal("cotizacion", "0");

		if (Objeto.anyEmpty(idCuentaOrigen, idCuentaDestino, monto)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuentaOrigen = contexto.cuenta(idCuentaOrigen);
		Cuenta cuentaDestino = contexto.cuenta(idCuentaDestino);

		if (cuentaOrigen == null || cuentaDestino == null) {
			// return Respuesta.estado("CUENTA_NO_EXISTE"); //se saca por motivos de
			// seguridad
			return Respuesta.error();
		}
		if (cuentaOrigen.idMoneda().equals(cuentaDestino.idMoneda())) {
			return Respuesta.estado("MISMA_MONEDA");
		}

		if (cuentaDestino.esPesos() && HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_venta_dolares",
				"prendido_modo_transaccional_venta_dolares_cobis") && !TransmitHB.isChallengeOtp(contexto, "venta-dolares")) {
			try {
				String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
				if (Objeto.empty(sessionToken))
					return Respuesta.parametrosIncorrectos();

				VentaUsdHBBMBankProcess ventaUsdHBBMBankProcess = new VentaUsdHBBMBankProcess(contexto.idCobis(),
						sessionToken,
						monto,
						Util.obtenerDescripcionMonedaTransmit(cuentaDestino.idMoneda()),
						TransmitHB.REASON_VENTA_DOLARES,
						new VentaUsdHBBMBankProcess.Payer(contexto.persona().cuit(), cuentaOrigen.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
						new VentaUsdHBBMBankProcess.Payee(contexto.persona().cuit(), cuentaDestino.numero(), Util.BH_CODIGO));

				Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, ventaUsdHBBMBankProcess, "venta-dolares");
				if (respuesta.hayError())
					return respuesta;
			} catch (Exception e) {
			}
		}

		if (TransmitHB.isChallengeOtp(contexto, "venta-dolares") && !contexto.validaSegundoFactor("venta-dolares"))
			return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");

		final String operacionIdem = cuentaDestino.esDolares() ? "COMPRA_USD" : "VENTA_USD";
		final int cobisInt = Integer.parseInt(contexto.idCobis());


		if (!SqlHomebanking.tryLockCVUSD(cobisInt, operacionIdem)) {
			return Respuesta.estado("OPERACION_EN_CURSO");
		}

		boolean __idemSuccess = false;
		try {

		ApiRequest requestSimulacion = null;
		// CuentasPostTransferenciasOperarDolares
		requestSimulacion = Api.request("SimularCompraVentaDolares", "cuentas", "POST", "/v2/cuentas/{idcuenta}/transferencias", contexto);
		requestSimulacion.path("idcuenta", cuentaOrigen.numero());
		requestSimulacion.query("cuentapropia", "true");
		requestSimulacion.query("inmediata", "false");
		requestSimulacion.query("especial", "false");

		requestSimulacion.body("cotizacion", "0");
		requestSimulacion.body("importe", monto);
		requestSimulacion.body("importeDivisa", "0");
		requestSimulacion.body("importePesos", "0");
		requestSimulacion.body("modoSimulacion", true);
		requestSimulacion.body("reverso", false);
		requestSimulacion.body("cuentaOrigen", cuentaOrigen.numero());
		requestSimulacion.body("cuentaDestino", cuentaDestino.numero());
		requestSimulacion.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
		requestSimulacion.body("tipoCuentaDestino", cuentaDestino.idTipo());
		requestSimulacion.body("idMoneda", cuentaOrigen.idMoneda());
		requestSimulacion.body("idMonedaDestino", cuentaDestino.idMoneda());
		requestSimulacion.body("descripcionConcepto", "VAR");
		requestSimulacion.body("idCliente", contexto.idCobis());
		requestSimulacion.cacheSesion = false;

		ApiResponse responseSimulacion = Api.response(requestSimulacion, idCuentaOrigen, idCuentaDestino, monto);
		if (responseSimulacion.hayError()) {
			if ("90005".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("SALDO_INSUFICIENTE");
			}
			if ("127012".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("OPERACION_INHABILITADA");
			}
			if ("127013".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("OPERACION_INHABILITADA");
			}
			if ("128027".equals(responseSimulacion.string("codigo"))) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if ("123008".equals(responseSimulacion.string("codigo"))) {
				if ("CLIENTE POSEE REFERENCIAS EXTERNAS".contains(responseSimulacion.string("mensajeAlUsuario")) || "Cuenta bloqueada".contains(responseSimulacion.string("mensajeAlDesarrollador"))) {
					return Respuesta.estado("NO_OPERAR_CA_USD");
				}
				return Respuesta.estado("OPERACION_NO_POSIBLE");
			}
			if ("123015".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("OPERACION_NO_POSIBLE_A_7001_BCRA");
			}
			if ("128029".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("SOLO_TITULAR");
			}
			if ("201008".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("NO_OPERAR_CA_USD");
			}
			if ("400".equals(responseSimulacion.string("codigo"))) {
				if (responseSimulacion.string("mensajeAlUsuario").toLowerCase().contains("3 - excedido otra causal")) {
					return Respuesta.estado("TOPE_SUBSIDIO");
				}
				return Respuesta.estado("OPERACION_NO_POSIBLE_400");
			}
			return Respuesta.error();
		}

		if (cotizacion.compareTo(responseSimulacion.bigDecimal("cotizacion")) != 0) {
			contexto.sesion.cache.put("InversionesGetCotizaciones-" + contexto.idCobis(), "");
			return Respuesta.estado("CAMBIO_COTIZACION");
		}

		Api.eliminarCache(contexto, "SimularCompraVentaDolares", idCuentaOrigen, idCuentaDestino, monto);
		ApiRequest request = null;
		// CuentasPostTransferenciasOperarDolares
		request = Api.request("CompraVentaDolares", "cuentas", "POST", "/v2/cuentas/{idcuenta}/transferencias", contexto);
		request.path("idcuenta", cuentaOrigen.numero());
		request.query("cuentapropia", "true");
		request.query("inmediata", "false");
		request.query("especial", "false");

		request.body("cotizacion", responseSimulacion.bigDecimal("cotizacion"));
		request.body("importe", monto);
		request.body("importeDivisa", responseSimulacion.bigDecimal("importeDivisa"));
		request.body("importePesos", responseSimulacion.bigDecimal("importePesos"));
		request.body("modoSimulacion", false);
		request.body("reverso", false);
		request.body("cuentaOrigen", cuentaOrigen.numero());
		request.body("cuentaDestino", cuentaDestino.numero());
		request.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
		request.body("tipoCuentaDestino", cuentaDestino.idTipo());
		request.body("idMoneda", cuentaOrigen.idMoneda());
		request.body("idMonedaDestino", cuentaDestino.idMoneda());
		request.body("descripcionConcepto", "VAR");
		request.body("transaccion", responseSimulacion.string("transaccion"));
		request.body("idCliente", contexto.idCobis());

		request.body("efectivo", false);
		request.body("identificacionPersona", contexto.persona().cuit());
		request.body("montoEnDivisa", responseSimulacion.bigDecimal("importeDivisa"));
		if (contexto.persona().idTipoDocumento() != null && contexto.persona().idTipoDocumento().equals(134)) {
			request.body("paisDocumento", 80);
		} else {
			request.body("paisDocumento", contexto.persona().idNacionalidad());
		}

		if (cuentaDestino.esDolares()) {
			request.body("ddjjCompraventa", true);
		}

		ApiResponse response = null;

		Boolean mostrarLeyenda = request.body().string("idMoneda").equals("80");
		String leyendaCompraVenta = Texto.htmlToText(ConfigHB.string("leyenda_compra_venta_dolares"));

		try {
			response = Api.response(request, contexto.idCobis(), new Date().getTime());
			leyendaCompraVenta = leyendaCompraVenta.replace("MONTO_DOLARES", Formateador.importe(response.bigDecimal("importeDivisa")));

		} finally {
			try {
				String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";
				String transaccion = codigoError.equals("0") ? response.string("transaccion") : null;
				String recibo = codigoError.equals("0") ? response.string("recibo") : null;

				String descripcion = "";
				if (response != null && !codigoError.equals("0")) {
					descripcion += response.string("codigo") + ".";
					descripcion += response.string("mensajeAlDesarrollador") + ".";
					descripcion += response.string("detalle") + ".";
				}
				descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

				SqlRequest sqlRequest = Sql.request("InsertAuditorCompraVentaDolares", "hbs");
				sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_compra_venta_dolares] ";
				sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cotizacion],[pesos],[dolares],[total],[cuentaOrigen],[cuentaDestino],[transaccion],[recibo],[leyenda]) ";
				sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.add(new Date()); // momento
				sqlRequest.add(contexto.idCobis()); // cobis
				sqlRequest.add(request.idProceso()); // idProceso
				sqlRequest.add(request.ip()); // ip
				sqlRequest.add("HB"); // canal
				sqlRequest.add(codigoError); // codigoError
				sqlRequest.add(descripcion); // descripcionError
				sqlRequest.add(request.body().string("idMoneda").equals("80") ? "compra" : "venta"); // operacion
				sqlRequest.add(responseSimulacion.bigDecimal("cotizacion")); // cotizacion
				sqlRequest.add(responseSimulacion.bigDecimal("importePesos")); // pesos
				sqlRequest.add(responseSimulacion.bigDecimal("importeDivisa")); // dolares
				sqlRequest.add(responseSimulacion.bigDecimal("importeTotal")); // total
				sqlRequest.add(cuentaOrigen.numero()); // cuentaOrigen
				sqlRequest.add(cuentaDestino.numero()); // cuentaDestino
				sqlRequest.add(transaccion); // transaccion
				sqlRequest.add(recibo); // recibo
				sqlRequest.add(mostrarLeyenda ? leyendaCompraVenta : ""); // leyenda
				Sql.response(sqlRequest);
			} catch (Exception e) {
			}
		}
		if (response.hayError()) {
			if ("90005".equals(response.string("codigo"))) {
				return Respuesta.estado("SALDO_INSUFICIENTE");
			}
			if ("127013".equals(response.string("codigo"))) {
				return Respuesta.estado("OPERACION_INHABILITADA");
			}
			if ("128027".equals(response.string("codigo"))) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if ("141018".equals(response.string("codigo"))){
				return estadoConCustomError("MESA_DE_CAMBIOS_DESHABILITADA");
			}
			if ("123008".equals(response.string("codigo"))) {
				if ("CLIENTE POSEE REFERENCIAS EXTERNAS".contains(response.string("mensajeAlUsuario")) || "Cuenta bloqueada".contains(response.string("mensajeAlDesarrollador"))) {
					return Respuesta.estado("NO_OPERAR_CA_USD");
				}
				return Respuesta.estado("OPERACION_NO_POSIBLE");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("3 - excedido otra causal")) {
				return Respuesta.estado("TOPE_SUBSIDIO");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("monto supera")) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("excede cupo")) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("excedido")) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("excede")) {
				return estadoConCustomError("TOPE_SUPERADO");
			}
			if (response.string("mensajeAlUsuario").toLowerCase().contains("venta previa inexistente")) {
				return Respuesta.estado("VENTA_PREVIA_INEXISTENTE");
			}
			if ("128029".equals(responseSimulacion.string("codigo"))) {
				return Respuesta.estado("SOLO_TITULAR");
			}
			if ("201008".equals(response.string("codigo"))) {
				return Respuesta.estado("NO_OPERAR_CA_USD");
			}
			// agrego esto para el caso que me devuelva un 504 el servicio. En ese caso no
			// sé si terminó o no la compra/venta
			// asi que por las dudas emito un socket time out. Del lado de front, ya tengo
			// el manejo para esto.
			if ("504".equals(response.string("codigo")) || "302".equals(response.string("codigo"))) {
				throw G.runtimeException(new java.net.SocketTimeoutException());
			}
			if ("400".equals(response.string("codigo"))) {
				return Respuesta.estado("OPERACION_NO_POSIBLE_400");
			}

			return Respuesta.error();
		}

		ProductosService.eliminarCacheProductos(contexto);

		String fechaHora = response.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy");
		if (fechaHora.isEmpty()) {
			fechaHora = response.date("fecha", "yyyy/MM/dd", "dd/MM/yyyy");
		}
		if (fechaHora.isEmpty()) {
			fechaHora = response.date("fecha", "yyyy/MM/dd HH:mm:ss.sss", "dd/MM/yyyy");
		}
		if (fechaHora.isEmpty()) {
			fechaHora = response.date("fecha", "yyyy-MM-dd HH:mm:ss.sss", "dd/MM/yyyy");
		}
		if (fechaHora.isEmpty()) {
			fechaHora = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		}

		String tipoCuentaOrigenDescripcion = Cuenta.descripcionCuentaComprobante(request.body().string("tipoCuentaOrigen"), request.body().string("idMoneda"), request.body().string("cuentaOrigen"));
		String tipoCuentaDestinoDescripcion = Cuenta.descripcionCuentaComprobante(request.body().string("tipoCuentaDestino"), request.body().string("idMonedaDestino"), request.body().string("cuentaDestino"));
		Boolean es30porciento = response.bigDecimal("percepcion") != null && response.bigDecimal("percepcion").compareTo(new BigDecimal(0)) != 0;
		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("OPERACION_A", (request.body().string("idMoneda").equals("80") ? "Compra" : "Venta") + " USD");
		comprobante.put("FECHA_HORA", fechaHora);
		comprobante.put("ID", response.string("recibo", response.string("operacion")));
		comprobante.put("OPERACION_B", (request.body().string("idMoneda").equals("80") ? "COMPRA" : "VENTA") + " USD");
		comprobante.put("IMPORTE", (request.body().string("idMoneda").equals("80") ? "USD " + Formateador.importe(response.bigDecimal("importeDivisa")) : "USD " + Formateador.importe(response.bigDecimal("importeDivisa"))));
		comprobante.put("TIPO_TRANSFERENCIA", "A cuenta propia");
		comprobante.put("CUENTA_ORIGEN", tipoCuentaOrigenDescripcion);
		comprobante.put("CUENTA_DESTINO", tipoCuentaDestinoDescripcion);
		comprobante.put("OPERACION_C", (request.body().string("idMoneda").equals("80") ? "Debitar" : "Acreditar"));

		// comprobante.put("MONTO", "$ " +
		// Formateador.importe(responseSimulacion.bigDecimal("importeTotal")));
		if (es30porciento) {
			comprobante.put("MONTO", "$ " + Formateador.importe(responseSimulacion.bigDecimal("importeTotal")));
		} else {
			comprobante.put("MONTO", "$ " + Formateador.importe(responseSimulacion.bigDecimal("importePesos")));
		}
		comprobante.put("COTIZACION", "USD 1 = $ " + Formateador.importe(response.bigDecimal("cotizacion")));
		if (response.bigDecimal("impRg4815") != null && response.bigDecimal("impRg4815").compareTo(new BigDecimal(0)) != 0) {
			comprobante.put("TEXTO_IMPUESTO_4815", "Percepción ARCA RG 5617/2024:");
			comprobante.put("VALOR_IMPUESTO_4815", "$ " + Formateador.importe(response.bigDecimal("impRg4815")));
		} else {
			comprobante.put("TEXTO_IMPUESTO_4815", "");
			comprobante.put("VALOR_IMPUESTO_4815", "");
		}

		try {
			if (request.body().string("idMoneda").equals("80") && responseSimulacion.bigDecimal("impRg5430") != null && responseSimulacion.bigDecimal("impRg5430").compareTo(new BigDecimal(0)) != 0) {
				comprobante.put("TEXTO_IMPUESTO_5430", "Percepción AFIP RG 5430/2023:");
				comprobante.put("VALOR_IMPUESTO_5430", "$ " + Formateador.importe(responseSimulacion.bigDecimal("impRg5430")));

			} else {
				comprobante.put("TEXTO_IMPUESTO_5430", "");
				comprobante.put("VALOR_IMPUESTO_5430", "");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (response.bigDecimal("percepcion") != null && response.bigDecimal("percepcion").compareTo(new BigDecimal(0)) != 0) {
			comprobante.put("TEXTO_PERCEPCION", "Impuesto País:");
			comprobante.put("VALOR_PERCEPCION", "$ " + Formateador.importe(response.bigDecimal("percepcion")));
		} else {
			comprobante.put("TEXTO_PERCEPCION", "");
			comprobante.put("VALOR_PERCEPCION", "");
		}
		comprobante.put("LEYENDA_COMPRA_VENTA_DOLARES", mostrarLeyenda ? leyendaCompraVenta : "");

		String idComprobante = "compra-venta-dolares" + "_" + response.string("transaccion");
		contexto.sesion.comprobantes.put(idComprobante, comprobante);

		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional_3") && cuentaOrigen.esDolares() && cuentaDestino.esPesos()) {
			String codigoTransaccion = cuentaOrigen.esCajaAhorro() ? cuentaDestino.esCajaAhorro() ? "401511" : "401501" : cuentaDestino.esCajaAhorro() ? "400711" : "400701";

			TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuentaDestino);

			new HBMonitoring().sendMonitoringVentaDolares(contexto, tarjetaDebitoAsociada == null ? contexto.persona().cuit() : tarjetaDebitoAsociada.numero(), codigoTransaccion, Formateador.importeTlf(response.bigDecimal("importeDivisa"), 12), Formateador.importeTlf(BigDecimal.valueOf(Double.valueOf(response.string("cotizacion"))), 8), "840", "00", "0210", cuentaOrigen.numero(), cuentaDestino.numero(), request.idProceso());

		}

		if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			Objeto parametros = new Objeto();
			parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("APELLIDO", contexto.persona().apellido());
			Date hoy = new Date();
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
			parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
			parametros.set("CANAL", "Home Banking");
			parametros.set("OPERACION_A", (request.body().string("idMoneda").equals("80") ? "Compra" : "Venta") + " USD");
			parametros.set("OPERACION_B", (request.body().string("idMoneda").equals("80") ? "COMPRA" : "VENTA") + " USD");
			parametros.set("IMPORTE", (request.body().string("idMoneda").equals("80") ? "USD " + Formateador.importe(response.bigDecimal("importeDivisa")) : "USD " + Formateador.importe(response.bigDecimal("importeDivisa"))));
			parametros.set("TIPO_TRANSFERENCIA", "A cuenta propia");
			parametros.set("CUENTA_ORIGEN", tipoCuentaOrigenDescripcion);
			parametros.set("CUENTA_DESTINO", tipoCuentaDestinoDescripcion);
			parametros.set("OPERACION_C", (request.body().string("idMoneda").equals("80") ? "Debitar" : "Acreditar"));


			String salesforce_compraventa_dolar_ahorro = ConfigHB.string("salesforce_compraventa_dolar_ahorro");
			parametros.set("IDCOBIS", contexto.idCobis());
			parametros.set("ISMOBILE", contexto.esMobile());
			new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_compraventa_dolar_ahorro, parametros));
		}

		__idemSuccess = true;
		return Respuesta.exito("idComprobante", idComprobante);
		} finally {
			// Libera siempre; si __idemSuccess=true, se activa cooldown (last_ok_at=now)
			try {
				SqlHomebanking.releaseCVUSD(cobisInt, operacionIdem, __idemSuccess);
			} catch (Exception ignore) {}
		}
	}
//	RESPUESTA DE LA SIMULACION
//		{
//		  "cotizacion": 36.0,
//		  "importe": 45,
//		  "importeDivisa": 45.0,
//		  "importePesos": 1620.0,
//		  "modoSimulacion": false,
//		  "reverso": false,
//		  "cuentaOrigen": "205100013927615",
//		  "cuentaDestino": "408100024711714",
//		  "tipoCuentaOrigen": "AHO",
//		  "tipoCuentaDestino": "AHO",
//		  "idMoneda": "2",
//		  "idMonedaDestino": "80",
//		  "descripcionConcepto": "VAR",
//		  "transaccion": "5669452",
//		  "idCliente": "4594725"
//		}

//	RESPUESTA DE LA OPERACION
//		{
//		  "operacion" : "307051000080608",
//		  "transaccion" : "5669452",
//		  "cotizacion" : "36.0",
//		  "importe" : 45.0,
//		  "servicio" : "TRANSF.TERCEROS",
//		  "idMonedaDestino" : "80",
//		  "cuentaDestino" : "408100024711714",
//		  "cuentaOrigen" : "205100013927615",
//		  "tipoCuentaDestino" : "AHO",
//		  "tipoCuentaOrigen" : "AHO",
//		  "monedaOrigen" : "2",
//		  "estado" : "P",
//		  "idCobis" : "30852062",
//		  "importePesos" : 1620.0,
//		  "referencia" : "307051000080608",
//		  "importeDivisa" : 45.0,
//		  "impuestos" : "0.00",
//		  "comision" : "0.00",
//		  "totalTransferencia" : "45.00",
//		  "idProceso" : "9201821",
//		  "fecha" : "2019/04/11",
//		  "monto" : "45.00",
//		  "numeroError" : "0",
//		  "recibo" : "307051000080608"
//		}

	public static Respuesta refreshCotizacion(ContextoHB contexto) {
		contexto.sesion.cache.put("InversionesGetCotizaciones-" + contexto.idCobis(), "");
		return Respuesta.exito();
	}

	private static Respuesta estadoConCustomError(String estado) {
		Objeto customError = new Objeto();

		switch (estado) {
			case "TOPE_SUPERADO":
				customError.set("titulo", "Estás intentando comprar más de USD 99.999");
				customError.set("descripcion", "Según la Com. “A” 8085, para avanzar con la compra podés <strong>ingresar un monto menor o autorizar la operación en una sucursal (esta última opción demora 48hs hábiles).</strong>");
				break;

			case "MESA_DE_CAMBIOS_DESHABILITADA":
				customError.set("titulo", "Esta operación no está disponible en este momento");
				customError.set("descripcion", "Volvé a intentar más tarde.");
				break;

			case "VENTA_PREVIA_INEXISTENTE":
				customError.set("titulo", "Venta previa no registrada");
				customError.set("descripcion", "No se encontró una venta previa de dólares requerida para esta operación.");
				break;

			case "SOLO_TITULAR":
				customError.set("titulo", "Sólo para titulares");
				customError.set("descripcion", "Esta operación está habilitada únicamente para titulares de la cuenta.");
				break;

			case "CTA_BLOQUEADA_CONTRADEPOSITO":
				customError.set("titulo", "Cuenta bloqueada");
				customError.set("descripcion", "Tu cuenta se encuentra bloqueada por contradepósito. Contactanos para más información.");
				break;

			default:
				return Respuesta.estado(estado); // fallback sin customError
		}

		return Respuesta.estado(estado).set("customError", customError);
	}


}