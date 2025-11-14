package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijo;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class HBCedip {
	
	public static List<Objeto> formatearCedips(ContextoHB ctx, List<Objeto> datos) {
		List<Objeto> cedips = new ArrayList<>();
		Map<String, String> cache = new HashMap<>();
		for(Objeto item : datos) {
			Objeto cedip = new Objeto();
			cedip.set("cedipId", item.string("cedipId"));
			cedip.set("fechaEmision", item.string("fechaEmision"));
			cedip.set("fechaVencimiento", item.string("fechaVencimiento"));
			
			String codigoMoneda = "032".equals(item.string("tipoMoneda")) ? "80" : "2";
			cedip.set("moneda", codigoMoneda);
			String simboloMoneda = Formateador.simboloMoneda(codigoMoneda);
			cedip.set("simbolo", simboloMoneda);
			
			cedip.set("montoDepositado", item.bigDecimal("montoDepositado"));			
			cedip.set("interesesCobrar", item.bigDecimal("montoIntereses"));
			cedip.set("montoAlVencimiento", item.bigDecimal("montoCobrar"));
			
			cedip.set("montoDepositadoFormateado", Formateador.importe(item.bigDecimal("montoDepositado")));			
			cedip.set("interesesCobrarFormateado", Formateador.importe(item.bigDecimal("montoIntereses")));
			cedip.set("montoAlVencimientoFormateado", Formateador.importe(item.bigDecimal("montoCobrar")));
			
			cedip.set("tna", item.bigDecimal("tna"));
			cedip.set("plazo",item.integer("plazo"));
			cedip.set("numeroFraccion",item.bigDecimal("fraccionNumero"));
			
			Boolean renovacionAutomatica = !item.string("tipoRenovacion").isEmpty() && !"NO RENUEVA".equals(item.string("tipoRenovacion"));
			cedip.set("renovacionAutomatica", renovacionAutomatica);
			
			Boolean esUva = "UVA".equals(item.string("tipoCertificado")) || "UVI".equals(item.string("tipoCertificado"));
			Boolean esUvaPrecancelable = esUva && false; //?
			String tipo = "Cedip";
			if(esUva) {
				tipo = "Cedip Ajustable UVA";
			}
			
			if(esUva && esUvaPrecancelable) {
				tipo = "Cedip Ajustable UVA Precancelable";
			}
			
			cedip.set("tipo", tipo);
			cedip.set("esUva", esUva);
			cedip.set("esUvaPrecancelable", esUvaPrecancelable);
			cedip.set("cotizacionUva", item.bigDecimal("cotizacionUva"));
			
			Objeto detalle = new Objeto();
			detalle.set("numero", item.string("numeroCertificado"));
			
			Cuenta cuenta = null;
			if("Recibido".equals(item.string("estado"))) {
				String cbuAcreditar = item.string("cbuAcreditar");
				if(cache.containsKey(cbuAcreditar)) {
					detalle.set("cuenta", cache.get(cbuAcreditar));
				}
				else {
					cuenta = ctx.cuentaPorCBU(item.string("cbuAcreditar"));
					String nroCuenta = cuenta != null ? "CA " + cuenta.numeroEnmascarado() : "";
					detalle.set("cuenta", nroCuenta);
					cache.put(cbuAcreditar, nroCuenta);
				}
				
				detalle.set("cbu", item.string("cbuAcreditar"));
				detalle.set("esRechazada", item.bool("esRechazada"));
				
				boolean esPendienteDeposito = false;
				
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					if("PC".equals(item.string("tipoAcreditacion"))) {
						if(!item.string("fechaDeposito").isEmpty()) {
							esPendienteDeposito = true;
						}
						else {
							esPendienteDeposito = LocalDate.now().isAfter(LocalDate.parse(item.string("fechaVencimiento"), formatter)) || LocalDate.now().equals(LocalDate.parse(item.string("fechaVencimiento"), formatter));
						}
					}
				}
				catch(Exception e) {}
				
				detalle.set("esDeposito", esPendienteDeposito);
				detalle.set("fechaDeposito", item.string("fechaDeposito"));
			}

			detalle.set("esModificable", esModificable(ctx, item.string("fechaVencimiento")));
			detalle.set("retensionGanancias", Formateador.importe(item.bigDecimal("montoRetencion")));
			detalle.set("esTransferible", true);
			detalle.set("garantiaDeposito", true); //?
			detalle.set("sellos", "0,00"); //?
			detalle.set("tipoAcreditacion", item.string("tipoAcreditacion"));
			detalle.set("tipoAcreditacionDesc", "PC".equals(item.string("tipoAcreditacion")) ? "Presentación al cobro" : "Acreditación al vencimiento");
			detalle.set("fecha", item.string("fecha"));
			detalle.set("esEmbargado", item.bool("embargo"));
			cedip.set("detalle", detalle);
			
			cedip.set("estado", item.string("estado"));
			
			if("Recibido".equals(item.string("estado")) || "Pendiente".equals(item.string("estado"))) {
				String cuilTercero = item.string("cuilEmisor");
				cedip.set("cuilEmisor", cuilTercero);
				
				if(cache.containsKey(cuilTercero)) {
					cedip.set("nombreEmisor", cache.get(cuilTercero));
				}
				else {
					String nombreTercero = nombreCompletoTercero(ctx, cuilTercero);
					cedip.set("nombreEmisor", nombreTercero);
					cache.put(cuilTercero, nombreTercero);
				}
			}
			
			if("Enviado".equals(item.string("estado")) || "Transferido".equals(item.string("estado"))) {
				String cuilTercero = item.string("cuilDestino");
				cedip.set("cuilDestino", cuilTercero);
				
				if(cache.containsKey(cuilTercero)) {
					cedip.set("nombreDestino", cache.get(cuilTercero));
				}
				else {
					String nombreTercero = nombreCompletoTercero(ctx, cuilTercero);
					cedip.set("nombreDestino", nombreTercero);
					cache.put(cuilTercero, nombreTercero);
				}
			}
			
			cedips.add(cedip);
		}
		
		Collections.sort(cedips, (o1, o2) -> o2.string("fechaEmision").compareTo(o1.string("fechaEmision")));
		
		return cedips;
	}
	
	private static boolean esModificable(ContextoHB ctx, String fecha) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		try {
			
			if(LocalDate.now().isAfter(LocalDate.parse(fecha, formatter))) {
				return false;
			}
			
			if(ChronoUnit.DAYS.between(LocalDate.parse(fecha, formatter), LocalDate.now()) > 10) {
				return true;
			}
			
			ApiResponse responseCatalogo = RestCatalogo.calendarioFecha(ctx, fecha);
			if(responseCatalogo.hayError()) {
				return false;
			}
			
			String fechaHabilAnteriorStr = responseCatalogo.objetos().get(0).string("diaHabilAnterior");
			LocalDate fechaHabilAnterior = LocalDate.parse(fechaHabilAnteriorStr, formatter);
			if(fechaHabilAnterior.isEqual(LocalDate.now()) || fechaHabilAnterior.isBefore(LocalDate.now())) {
				return false;
			}
			
			responseCatalogo = RestCatalogo.calendarioFecha(ctx, fechaHabilAnteriorStr);
			if(responseCatalogo.hayError()) {
				return false;
			}
			
			fechaHabilAnteriorStr = responseCatalogo.objetos().get(0).string("diaHabilAnterior");
			fechaHabilAnterior = LocalDate.parse(fechaHabilAnteriorStr, formatter);
			if(fechaHabilAnterior.isEqual(LocalDate.now()) || fechaHabilAnterior.isBefore(LocalDate.now())) {
				return false;
			}
			
		}
		catch(Exception e) {
			return false;
		}

		return true;
	}

	public static Respuesta cedipsRecibidos(ContextoHB ctx) {
		String cuil = ctx.persona().cuit();
		return Respuesta.exito("cedips",  formatearCedips(ctx, ctx.cedipsPendientes(cuil)));
	}
	
	public static Respuesta consolidadaCedips(ContextoHB ctx) {
		String cuil = ctx.persona().cuit();
		CompletableFuture<List<Objeto>> cedipsActivosFuture = CompletableFuture.supplyAsync(() ->  ctx.cedipsActivos(cuil, "ACTIVO"));
		CompletableFuture<List<Objeto>> cedipsEnviadosFuture = CompletableFuture.supplyAsync(() -> ctx.cedipsEnviados(cuil));
		CompletableFuture<List<Objeto>> cedipsTransferidosFuture = CompletableFuture.supplyAsync(() -> ctx.cedipsTransmitidos(cuil));
		CompletableFuture<List<Objeto>> cedipsFuture = CompletableFuture.allOf(cedipsActivosFuture, cedipsEnviadosFuture, cedipsTransferidosFuture)
				.thenApplyAsync(voidResult -> {
					
					List<Objeto> cedipsList = new ArrayList<>();
					try {
						cedipsList.addAll(cedipsActivosFuture.get());
						cedipsList.addAll(cedipsEnviadosFuture.get());
						cedipsList.addAll(cedipsTransferidosFuture.get());
					}
					catch(Exception e) {}
					return cedipsList;
				});
		
		List<Objeto> cedips;
		try {
			cedips = cedipsFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			return Respuesta.error();
		}
		
		cedips = formatearCedips(ctx, cedips);
		
		BigDecimal totalPesos = new BigDecimal(0);
		BigDecimal totalDolares = new BigDecimal(0);
		BigDecimal totalUVAPesos = new BigDecimal(0);
		
		for(Objeto cedip: cedips) {
			if(!"Recibido".equals(cedip.string("estado"))) {
				continue;
			}
			
			boolean esPesos = "80".equals(cedip.string("moneda"));
			if(esPesos && cedip.bool("esUva")) {
				totalUVAPesos = totalUVAPesos.add(cedip.bigDecimal("montoAlVencimiento"));
			}
			else if(esPesos) {
				totalPesos = totalPesos.add(cedip.bigDecimal("montoAlVencimiento"));
			}
			
			if("2".equals(cedip.string("moneda"))) {
				totalDolares = totalDolares.add(cedip.bigDecimal("montoAlVencimiento"));
			}
		}
		
		Respuesta respuesta = new Respuesta();
		respuesta.set("totalPesos", totalPesos);
		respuesta.set("totalDolares", totalDolares);
		respuesta.set("totalUVAPesos", totalUVAPesos);
		respuesta.set("totalPesosFormateado", Formateador.importe(totalPesos));
		respuesta.set("totalDolaresFormateado", Formateador.importe(totalDolares));
		respuesta.set("totalUVAPesosFormateado", Formateador.importe(totalUVAPesos));
		respuesta.set("leyendaCedip", ConfigHB.string("hb_leyenda_cedip", ""));
		respuesta.set("cedips", cedips);
		return respuesta;
	}

	public static Object altaCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		Boolean aceptarCedip = ctx.parametros.bool("aceptarCedip");
		
		String cbu = ctx.parametros.string("cbu");
		String tipoAcreditacion = ctx.parametros.string("tipoAcreditacion");
		
		if (Objeto.anyEmpty(aceptarCedip, cedipId)) {
			return Respuesta.parametrosIncorrectos();
		}
		
		Persona persona = ctx.persona();
		CompletableFuture<Objeto> domicilioPostalFuture = CompletableFuture.supplyAsync(() -> RestPersona.domicilioPostal(ctx, persona.cuit()));
		CompletableFuture<ApiResponse> cedipFuture = CompletableFuture.supplyAsync(() ->  {
			ApiRequest requestCedip = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", ctx);
			requestCedip.path("cedipId", cedipId);
			requestCedip.path("cuit", persona.cuit());
			
			return Api.response(requestCedip, ctx.idCobis());
		});
		
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(domicilioPostalFuture, cedipFuture);
		combinedFuture.join();
		
		Objeto responseDomicilioPostal = domicilioPostalFuture.join();
		ApiResponse responseCedip = cedipFuture.join();

		if(responseCedip.hayError()) {
			return Respuesta.error();
		}
		
		if(responseCedip.bool("embargo")) {
			return Respuesta.estado("ERROR_EMBARGO");
		}
		
		String tipoDocumento = persona.tieneCuit() ? "CUIT" : "CUIL";
		
		if(aceptarCedip) {
			
			if (Objeto.anyEmpty(cbu, tipoAcreditacion)) {
				return Respuesta.parametrosIncorrectos();
			}
			
			ApiRequest request = Api.request("cedipsAdmitir", "plazosfijos", "POST", "/v1/cedip/admitir", ctx);
			request.body("cbuAcreditar", cbu);
			request.body("cedipId", cedipId);
			request.body("codigoBanco", "044");
			request.body("ejecutorDocumento", persona.cuit());
			request.body("ejecutorTipoDocumento", tipoDocumento);
			request.body("fechaVencimiento", responseCedip.string("fechaVencimiento"));
			request.body("firmantes", getFirmantes(persona));
			request.body("fraccionId", 0);
			request.body("tipoAcreditacion", tipoAcreditacion);
			request.body("tipoCertificado", responseCedip.string("tipoCertificado"));
			
			Objeto montosAdmitirCore = new Objeto();
			montosAdmitirCore.set("montoACobrar", responseCedip.bigDecimal("montoCobrar"));
			montosAdmitirCore.set("montoDepositado", responseCedip.bigDecimal("montoDepositado"));
			montosAdmitirCore.set("montoIntereses", responseCedip.bigDecimal("montoIntereses"));
			montosAdmitirCore.set("montoRetencion", responseCedip.bigDecimal("montoRetencion"));

			request.body("ejecutorDomicilio", responseDomicilioPostal.string("provincia"));
			request.body("montosAdmitirCore", montosAdmitirCore);
			
			ApiResponse response = Api.response(request, ctx.idCobis());
			if(response.hayError()) {
				
				tipoDocumento = "CUIT".equals(tipoDocumento) ? "CUIL" : "CUIT";
				request.body("ejecutorTipoDocumento", tipoDocumento);
				
				List<Objeto> firmantes = new ArrayList<>();
				Objeto firmante = new Objeto();
				firmante.set("documentoFirmante", persona.cuit());
				firmante.set("tipoDocumentoFirmante", tipoDocumento);
				firmantes.add(firmante);
				request.body("firmantes", firmantes);
				
				response = Api.response(request, ctx.idCobis());
				if(response.hayError()) {
					return Respuesta.error();
				}
			}
			
			return Respuesta.exito();
		}
		
		ApiRequest request = Api.request("cedipsRepudiar", "plazosfijos", "POST", "/v1/cedip/repudiar", ctx);
		request.body("cedipId", cedipId);
		request.body("ejecutorDocumento", persona.cuit());
		request.body("ejecutorTipoDocumento", tipoDocumento);
		request.body("firmantes", getFirmantes(persona));
		request.body("fraccionId", 0);
		request.body("fraccionado", false);
		
		ApiResponse response = Api.response(request, ctx.idCobis());
		if(response.hayError()) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Object transferenciaCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		String cuilDestino = ctx.parametros.string("cuilDestino");
		
		if (Objeto.anyEmpty(cedipId, cuilDestino)) {
			return Respuesta.parametrosIncorrectos();
		}
		
		Persona persona = ctx.persona();
		CompletableFuture<ApiResponse> personaDestinoFuture = CompletableFuture.supplyAsync(() ->  RestPersona.consultarPersonaEspecifica(ctx, cuilDestino));
		CompletableFuture<ApiResponse> cedipFuture = CompletableFuture.supplyAsync(() ->  {
			ApiRequest requestCedip = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", ctx);
			requestCedip.path("cedipId", cedipId);
			requestCedip.path("cuit", persona.cuit());
			
			return Api.response(requestCedip, ctx.idCobis());
		});
		
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(personaDestinoFuture, cedipFuture);
		combinedFuture.join();
		
		ApiResponse responseDestino = personaDestinoFuture.join();
		ApiResponse responseCedip = cedipFuture.join();
		
		
		if(responseDestino.hayError() || responseCedip.hayError()) {
			return Respuesta.error();
		}
		
		if(responseCedip.bool("embargo")) {
			return Respuesta.estado("ERROR_EMBARGO");
		}
		
		String nombreDestino = Texto.primerasMayuscula(responseDestino.string("nombres") + " " + responseDestino.string("apellidos"));
		String tipoDocumentoDestino = "11".equals(responseDestino.string("idTipoIDTributario").trim()) ? "CUIT" : "CUIL";
		if("CUIT".equals(tipoDocumentoDestino)) {
			return Respuesta.estado("ERROR_CUIT");
		}
		
		ApiRequest request = Api.request("cedipsTransferir", "plazosfijos", "POST", "/v1/cedip/solicitar", ctx);
		request.body("cedipId", cedipId);
		request.body("codigoBanco", responseCedip.string("codigoBanco"));
		request.body("firmantes", getFirmantes(persona));
		request.body("fraccionId", 0);
		request.body("fraccionado", false);
		request.body("tenedorDocumento", persona.cuit());
		
		String tipoDocumento = persona.tieneCuit() ? "CUIT" : "CUIL";
		request.body("tenedorTipoDocumento", tipoDocumento);
	
		List<Objeto> transmisiones = new ArrayList<>();
		Objeto transmisicion = new Objeto();
		transmisicion.set("beneficiarioDocumento", cuilDestino);
		transmisicion.set("beneficiarioNombre", nombreDestino.toUpperCase());
		transmisicion.set("monto", responseCedip.bigDecimal("montoDepositado"));
		transmisicion.set("beneficiarioTipoDocumento", tipoDocumentoDestino);
		transmisicion.set("tipoTransmision", "NOM");
		transmisiones.add(transmisicion);
		request.body("transmisiones", transmisiones);
		
		
		ApiResponse response = Api.response(request, ctx.idCobis());
		if(response.hayError()) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Object anularTransferenciaCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		
		if (Objeto.anyEmpty(cedipId)) {
			return Respuesta.parametrosIncorrectos();
		}
	
		Persona persona = ctx.persona();
		ApiRequest requestCedip = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", ctx);
		requestCedip.path("cedipId", cedipId);
		requestCedip.path("cuit", persona.cuit());
		
		ApiResponse responseCedip = Api.response(requestCedip, ctx.idCobis());
		if(responseCedip.hayError() || responseCedip.bool("embargo")) {
			return Respuesta.error();
		}
		
		ApiRequest request = Api.request("cedipsAdmitir", "plazosfijos", "POST", "/v1/cedip/anular", ctx);
		request.body("cedipId", cedipId);
		request.body("codigoBanco", responseCedip.string("codigoBanco"));
		request.body("ejecutorDocumento", persona.cuit());
		
		String tipoDocumento = ctx.persona().tieneCuit() ? "CUIT" : "CUIL";
		request.body("ejecutorTipoDocumento", tipoDocumento);
		request.body("firmantes", getFirmantes(persona));
		request.body("fraccionId", 0);
		
		ApiResponse response = Api.response(request, ctx.idCobis());
		if(response.hayError()) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Object acreditacionCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		String tipoAcreditacion = ctx.parametros.string("tipoAcreditacion");
		String cbu = ctx.parametros.string("cbu");
		
		if (Objeto.anyEmpty(cedipId)) {
			return Respuesta.parametrosIncorrectos();
		}
		
		Persona persona = ctx.persona();
		ApiRequest requestCedip = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", ctx);
		requestCedip.path("cedipId", cedipId);
		requestCedip.path("cuit", persona.cuit());
		
		ApiResponse responseCedip = Api.response(requestCedip, ctx.idCobis());
		if(responseCedip.hayError() || responseCedip.bool("embargo")) {
			return Respuesta.error();
		}
		
		ApiRequest request = Api.request("cedipsAdmitir", "plazosfijos", "PUT", "/v1/acreditacion/modificar", ctx);
		request.body("cbuAcreditar", cbu.isEmpty() ? responseCedip.string("cbuAcreditar") : cbu);
		request.body("cedipId", cedipId);
		request.body("codigoBanco", responseCedip.string("codigoBanco"));
		request.body("fechaVencimiento", responseCedip.string("fechaVencimiento"));
		request.body("firmantes", getFirmantes(persona));
		request.body("fraccionId", 0);
		
		Objeto montosAdmitirCore = new Objeto();
		montosAdmitirCore.set("montoDepositado", responseCedip.bigDecimal("montoDepositado"));
		montosAdmitirCore.set("montoRetencion", responseCedip.bigDecimal("montoRetencion"));
		montosAdmitirCore.set("montoACobrar", responseCedip.bigDecimal("montoCobrar"));
		montosAdmitirCore.set("montoIntereses", responseCedip.bigDecimal("montoIntereses"));
		request.body("montosAdmitirCore", montosAdmitirCore);
		request.body("tenedorDocumento", persona.cuit());
		
		String tipoDocumento = persona.tieneCuit() ? "CUIT" : "CUIL";
		request.body("tenedorTipoDocumento", tipoDocumento);
		request.body("tipoAcreditacion", tipoAcreditacion.isEmpty() ? responseCedip.string("tipoAcreditacion") : tipoAcreditacion);
		
		ApiResponse response = Api.response(request, ctx.idCobis());
		if(response.hayError()) {
			return Respuesta.error();
		}
	
		return Respuesta.exito();
	}
	
	public static List<Objeto> getFirmantes(Persona persona){
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documentoFirmante", persona.cuit());
		
		String tipoDocumento = persona.tieneCuit() ? "CUIT" : "CUIL";
		firmante.set("tipoDocumentoFirmante", tipoDocumento);
		firmantes.add(firmante);
		return firmantes;
	}
	
	public static String nombreCompletoTercero(ContextoHB ctx, String cuil) {
		String nombreCompleto = "";
		ApiResponse response = RestPersona.consultarPersonaEspecifica(ctx, cuil);
		if(response != null && !response.hayError()) {
			nombreCompleto = Texto.primerasMayuscula(response.string("nombres") + " " + response.string("apellidos"));
		}
		
		return nombreCompleto;
	}

	public static byte[] comprobanteCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		List<Objeto> cedips = formatearCedips(ctx, ctx.cedip(cedipId));
		Objeto cedip = cedips.get(0);
		
		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("ID_PLAZO_FIJO", cedip.objeto("detalle").string("numero"));
		comprobante.put("ESTADO_CEDIP", cedip.string("estado"));
		comprobante.put("LABEL_CUENTA_TITULO", "");
		comprobante.put("CUENTA_PLAZO_FIJO", "");
		
		String fechaTransferencia = cedip.objeto("detalle").string("fecha");
		comprobante.put("LABEL_FECHA_ESTADO", fechaTransferencia.isEmpty() ? "" : "Fecha " + cedip.string("estado").toLowerCase() + ":");
		comprobante.put("FECHA_TRANSFERENCIA", fechaTransferencia);
		comprobante.put("ESTA_EMBARGADO", cedip.bool("esEmbargado", false) ? "SI" : "NO");
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		comprobante.put("TIPO_PLAZO", PlazoFijo.tipo(cedip.bool("esUva") ? "0018" : "0011"));
		comprobante.put("MONTO_INICIAL", cedip.string("simbolo") + " " + cedip.string("montoDepositadoFormateado"));
		comprobante.put("MONTO_AL_VENCIMIENTO", cedip.string("simbolo") + " " + cedip.string("montoAlVencimientoFormateado"));
		comprobante.put("INTERES_A_COBRAR", cedip.string("simbolo") + " " + cedip.string("interesesCobrarFormateado"));
		comprobante.put("FECHA_VENCIMIENTO", cedip.string("fechaVencimiento"));
		comprobante.put("NRO_REFERENCIA", cedip.objeto("detalle").string("numero"));
		comprobante.put("FECHA_CONSTITUCION",  cedip.string("fechaEmision"));
		comprobante.put("PLAZO_EN_DIAS", cedip.string("plazo"));
		comprobante.put("TASA_NOMINAL_ANUAL", cedip.string("tna") + "%");
		comprobante.put("RENOVACION_AUTOMATICA", cedip.bool("renovacionAutomatica") ? "SI" : "NO");
		comprobante.put("GARANTIA_DEPOSITO", "NO");
		comprobante.put("TIPO_COBRO", cedip.objeto("detalle").string("tipoAcreditacionDesc"));
		comprobante.put("NUMERO_FRACCION", cedip.bigDecimal("numeroFraccion").toString());
		comprobante.put("ID_CEDIP", cedipId);

		comprobante.put("LABEL_CANC_INTERES_FINAL", "");
		comprobante.put("LABEL_CANC_MONTO", "");
		comprobante.put("LABEL_CANC_FECHA", "");
		comprobante.put("LABEL_CANC_ESTADO", "");

		comprobante.put("TEXT_CANC_INTERES_FINAL", "");
		comprobante.put("TEXT_CANC_MONTO", "");
		comprobante.put("TEXT_CANC_FECHA", "");
		comprobante.put("TEXT_CANC_ESTADO", "");
		
		comprobante.put("LABEL_MONTO_VENC", "Monto al vencimiento:");
		comprobante.put("LABEL_INTERES_COBRAR", "Intereses a cobrar:");
		comprobante.put("LABEL_MONTO_UVA", "");
		comprobante.put("LABEL_COTIZACION_UVA", "");
		comprobante.put("UVA_MONTO", "");
		comprobante.put("COTIZACION_CONSTITUCION_UVA", "");
		comprobante.put("LEGAL_UVA", "");
		comprobante.put("LEGAL_RENOVACION_AUTOMATICA", "");
		
		comprobante.put("LABEL_CUIL_EMISOR", "");
		comprobante.put("TEXT_CUIL_EMISOR", "");
		comprobante.put("LABEL_NOMBRE_EMISOR", "");
		comprobante.put("TEXT_NOMBRE_EMISOR", "");
		
		if("Recibido".equals(cedip.string("estado"))) {
			String cuenta = cedip.objeto("detalle").string("cuenta");			
			if(!cuenta.isEmpty()) {
				comprobante.put("LABEL_CUENTA_TITULO", "Cuenta:");
				comprobante.put("CUENTA_PLAZO_FIJO", cuenta);	
			}
			else {
				comprobante.put("LABEL_CUENTA_TITULO", "Cuenta CBU:");
				comprobante.put("CUENTA_PLAZO_FIJO", cedip.objeto("detalle").string("cbu"));
			}
		}
		
		if("Recibido".equals(cedip.string("estado")) || "Pendiente".equals(cedip.string("estado"))) {
			comprobante.put("LABEL_CUIL_EMISOR", cedip.string("cuilEmisor").isEmpty() ? "" : "Cuil emisor:");
			comprobante.put("TEXT_CUIL_EMISOR", cedip.string("cuilEmisor"));
			comprobante.put("LABEL_NOMBRE_EMISOR", cedip.string("nombreEmisor").isEmpty() ? "" : "Nombre emisor:");
			comprobante.put("TEXT_NOMBRE_EMISOR", cedip.string("nombreEmisor"));
		}
		else {
			comprobante.put("LABEL_CUIL_EMISOR", cedip.string("cuilDestino").isEmpty() ? "" : "Cuil destino:");
			comprobante.put("TEXT_CUIL_EMISOR", cedip.string("cuilDestino"));
			comprobante.put("LABEL_NOMBRE_EMISOR", cedip.string("nombreDestino").isEmpty() ? "" :  "Nombre destino:");
			comprobante.put("TEXT_NOMBRE_EMISOR", cedip.string("nombreDestino"));
		}
		
		if (cedip.bool("esUva")) {
			comprobante.put("LABEL_MONTO_VENC", "Monto estimado al vencimiento:");
			comprobante.put("LABEL_INTERES_COBRAR", "Intereses estimados a cobrar:");
			comprobante.put("LABEL_MONTO_UVA", "Monto Inicial en UVA:");
			comprobante.put("LABEL_COTIZACION_UVA", "Cotización UVA al día de la constitución:");
			comprobante.put("UVA_MONTO", cedip.string("simbolo") + " " + cedip.string("montoAlVencimiento"));
			comprobante.put("COTIZACION_CONSTITUCION_UVA", cedip.bigDecimal("cotizacionUva").toString());
			comprobante.put("LEGAL_UVA", "AL VENCIMIENTO EL MONTO INICIAL SERÁ ACTUALIZADO A LA COTIZACIÓN UVA VIGENTE SOBRE EL QUE SE CALCULARÁN LOS INTERESES A COBRAR.");
		}
		
		if(cedip.bool("renovacionAutomatica")) {
			comprobante.put("LEGAL_RENOVACION_AUTOMATICA", "En caso de renovación automática resultará de aplicación la tasa de interés máxima vigente el día de la efectiva renovación, conforme el respectivo canal de originación.");
		}
		
		String idComprobante = "cedip" + "_" + cedipId;
		ctx.sesion.comprobantes.put(idComprobante, comprobante);
		ctx.parametros.set("id", idComprobante);

		return HBArchivo.comprobante(ctx);
	}

	public static Object depositarCedip(ContextoHB ctx) {
		String cedipId = ctx.parametros.string("cedipId");
		String cbu = ctx.parametros.string("cbu");
		
		if (Objeto.anyEmpty(cedipId)) {
			return Respuesta.parametrosIncorrectos();
		}
		
		Persona persona = ctx.persona();
		ApiRequest requestCedip = Api.request("cedipGet", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/0", ctx);
		requestCedip.path("cedipId", cedipId);
		requestCedip.path("cuit", persona.cuit());
		
		ApiResponse responseCedip = Api.response(requestCedip, ctx.idCobis());
		if(responseCedip.hayError() || responseCedip.bool("embargo")) {
			return Respuesta.error();
		}
		
		ApiRequest request = Api.request("cedipsDepositar", "plazosfijos", "POST", "/v1/acreditacion/depositar", ctx);
		request.body("cbuAcreditar", cbu.isEmpty() ? responseCedip.string("cbuAcreditar") : cbu);
		request.body("cedipId", cedipId);
		request.body("codigoBanco", responseCedip.string("codigoBanco"));
		request.body("firmantes", getFirmantes(persona));
		request.body("fraccionId", 0);
		request.body("tenedorDocumento", persona.cuit());
		request.body("tenedorTipoDocumento", persona.tieneCuit() ? "CUIT" : "CUIL");
		
		ApiResponse response = Api.response(request, ctx.idCobis());
		if(response.hayError()) {
			return Respuesta.error();
		}
	
		return Respuesta.exito();
	}
	
}
