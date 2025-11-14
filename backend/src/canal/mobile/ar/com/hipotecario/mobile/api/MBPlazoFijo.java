package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.PlazoFijo;
import ar.com.hipotecario.mobile.servicio.PlazoFijoService;
import ar.com.hipotecario.mobile.servicio.ProductosService;

public class MBPlazoFijo {

	public static RespuestaMB consolidadaPlazosFijo(ContextoMB contexto) {

		Boolean buscarTasaPreferencial = contexto.parametros.bool("buscarTasaPreferencial");
		String leyendaGarantia = ConfigMB.string("leyenda_garantia_plazo_fijo", "");
		String leyendaRenovacion = ConfigMB.string("leyenda_renovacion_plazo_fijo", "");
		
		// TODO: Se retira por Normativo “A” 7849
		/*
		 * if (contexto.persona().esMenor()) { return
		 * Respuesta.estado("MENOR_NO_AUTORIZADO"); }
		 */

		RespuestaMB respuesta = new RespuestaMB();

		for (Objeto item : ProductosService.productos(contexto).objetos("errores")) {
			if ("plazosFijos".equals(item.string("codigo"))) {
				return RespuestaMB.estado("ERROR_CONSOLIDADA");
			}
		}
		if (buscarTasaPreferencial) {
			RespuestaMB respuestaTasaPreferencial = tasaPreferencial(contexto);
			if (!"ERROR".equals(respuestaTasaPreferencial.string("estado"))) {
				respuesta.unir(respuestaTasaPreferencial);
			}
		}

		List<Objeto> plazosFijos = new CopyOnWriteArrayList<>();
		try {

			ExecutorService executorService = Executors.newCachedThreadPool();
			for (PlazoFijo item : contexto.plazosFijos()) {
				executorService.submit(() -> {

					if (item.esValido() && !item.esPlazoFijoLogros() && !item.esUva()) {

						Objeto plazoFijo = new Objeto();
						plazoFijo.set("id", item.id());
						plazoFijo.set("tipo", item.tipo());

						Integer codigoInt = Integer.valueOf(item.tipo());
						plazoFijo.set("esUva", false);
						plazoFijo.set("esUvaPrecancelable", false);
						if (PlazoFijo.esUvaNoCancelable(codigoInt.toString())) {
							plazoFijo.set("esUva", true);
						} else {
							if (PlazoFijo.esUvaPrecancelable(codigoInt.toString())) {
								plazoFijo.set("esUva", true);
								plazoFijo.set("esUvaPrecancelable", true);
							}
						}
						plazoFijo.set("fechaAlta", item.fechaAlta("dd/MM/yyyy"));
						plazoFijo.set("fechaVencimiento", item.fechaVencimiento("dd/MM/yyyy"));
						plazoFijo.set("moneda", item.moneda());
						plazoFijo.set("monto", item.importeInicial());
						plazoFijo.set("montoFormateado", item.importeInicialFormateado());
						plazoFijo.set("tna", item.tnaFormateada());
						plazoFijo.set("interesesCobrar", item.interesesFormateado());
						plazoFijo.set("porcentajeTiempo", item.porcentajeDiasTranscurridos());
						plazoFijo.set("diasFaltantes", item.diasFaltantes());
						plazoFijo.set("renovacionAutomatica", item.tieneRenovacionAutomatica());
						plazoFijo.set("garantiaDeposito", item.tieneGarantiaDeposito());
						plazoFijo.set("descripcion", PlazoFijo.tipo(item.tipo()));
						plazoFijo.set("numero", item.numero());
						plazoFijo.set("plazo", item.plazo());
						plazoFijo.set("orden", item.fechaVencimiento().getTime());
						plazoFijo.set("tnaCancelacionAntFormateada", item.tnaCancelacionAntFormateada());
						plazoFijo.set("teaCancelacionAntFormateada", item.teaCancelacionAntFormateada());
						plazoFijo.set("fechaDesdeCancelacionAnt", item.fechaDesdeCancelacionAnt("dd/MM/yyyy"));
						plazoFijo.set("fechaHastaCancelacionAnt", item.fechaHastaCancelacionAnt("dd/MM/yyyy"));

						// TODO DLV-50929 nueva tasa 120 dias
						// String leyendaUvaPrecancelable =
						// devolverLeyendaUvaCancelacion(item.fechaDesdeCancelacionAnt("dd/MM/yyyy"),
						// item.fechaHastaCancelacionAnt("dd/MM/yyyy"),
						// item.tnaCancelacionAntFormateada(), item.teaCancelacionAntFormateada());
						String leyendaUvaPrecancelable = devolverLeyendaUvaCancelacion(item.fechaDesdeCancelacionAnt("dd/MM/yyyy"), item.fechaHastaCancelacionAnt("dd/MM/yyyy"), item.tnaCancelacionAntFormateada(), item.teaCancelacionAntFormateada(), item.plazo(), item.fechaCancelacionAnt120("dd/MM/yyyy"));
						if (PlazoFijo.esUvaPrecancelable(codigoInt.toString())) {
							leyendaUvaPrecancelable = leyendaUvaPrecancelable + leyendaGarantia;
						}else{
							leyendaUvaPrecancelable = leyendaRenovacion + leyendaGarantia;
						}
						plazoFijo.set("leyendaUvaPrecancelable", leyendaUvaPrecancelable);

						try {
							if (item.numero() != null && !item.numero().isEmpty()) {
								contexto.parametros.set("nroCertificado", item.numero());
							}

							RespuestaMB detalle = detallePlazoFijo(contexto);
							if (!detalle.hayError()) {
								// importe final
								BigDecimal impuesto = item.importeInicial().add(item.intereses()).subtract(new BigDecimal(Formateador.importe(detalle.string("impuesto"))));
								detalle.set("montoAlVencimiento", Formateador.importe(impuesto));
								plazoFijo.set("montoAlVencimiento", Formateador.importe(impuesto));
								plazoFijo.set("detalle", detalle);
							} else {
								plazoFijo.set("detalle", new HashMap<>());
							}
						} catch (Exception e) {
							plazoFijo.set("detalle", new HashMap<>());
						}

						plazosFijos.add(plazoFijo);
					}
				});
			}
			Concurrencia.esperar(executorService, null, 60);
		} catch (Exception e) {
			// return Respuesta.error();
		}

		Collections.sort(plazosFijos, (o1, o2) -> o1.string("orden").compareTo(o2.string("orden")));

		BigDecimal totalPesos = new BigDecimal("0");
		BigDecimal totalDolares = new BigDecimal("0");
		BigDecimal totalAhorroJoven = new BigDecimal("0");
		for (Objeto plazoFijo : plazosFijos) {
			PlazoFijo item = contexto.plazoFijo(plazoFijo.string("id"));
			if (item.esPesos() && !item.esProcrearJoven()) {
				totalPesos = totalPesos.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosPesos", plazoFijo);
			}
			if (item.esDolares() && !item.esProcrearJoven()) {
				totalDolares = totalDolares.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosDolares", plazoFijo);
			}
			if (item.esProcrearJoven()) {
				totalAhorroJoven = totalAhorroJoven.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosAhorroJoven", plazoFijo);
			}
		}

		respuesta.set("configuracion", obtenerConfiguracionPlazoFijo());
		respuesta.set("totalPesosFormateado", Formateador.importe(totalPesos));
		respuesta.set("totalDolaresFormateado", Formateador.importe(totalDolares));
		respuesta.set("totalProcrearJovenFormateado", Formateador.importe(totalAhorroJoven));

        return respuesta.ordenar("estado", "totalPesosFormateado", "totalDolaresFormateado", "totalProcrearJovenFormateado");
	}

	public static RespuestaMB consolidadaPlazosFijoUva(ContextoMB contexto) {

		Boolean buscarTasaPreferencial = contexto.parametros.bool("buscarTasaPreferencial"); // default false

		RespuestaMB respuesta = new RespuestaMB();

		for (Objeto item : ProductosService.productos(contexto).objetos("errores")) {
			if ("plazosFijos".equals(item.string("codigo"))) {
				return RespuestaMB.estado("ERROR_CONSOLIDADA");
			}
		}

		// === SOLO PRICING (sin plataNueva) ===
		if (Boolean.TRUE.equals(buscarTasaPreferencial)) {
			RespuestaMB respTP = tasaPreferencial(contexto);
			if (!"ERROR".equalsIgnoreCase(respTP.string("estado"))) {
				Object nodoPricing = respTP.get("pricing"); // viene solo si NO hay PN y SÍ hay datos
				if (nodoPricing != null) {
					respuesta.set("pricing", nodoPricing); // NO unimos toda la resp — evitamos "plataNueva"
				}
			}
		}

		List<Objeto> plazosFijos = new CopyOnWriteArrayList<>();
		try {
			ExecutorService executorService = Executors.newCachedThreadPool();
			for (PlazoFijo item : contexto.plazosFijos()) {
				executorService.submit(() -> {
					if (item.esValido() && item.esUva()) {

						Objeto plazoFijo = new Objeto();
						plazoFijo.set("id", item.id());
						plazoFijo.set("tipo", item.tipo());

						Integer codigoInt = Integer.valueOf(item.tipo());
						plazoFijo.set("esUva", false);
						plazoFijo.set("esUvaPrecancelable", false);
						if (PlazoFijo.esUvaNoCancelable(codigoInt.toString())) {
							plazoFijo.set("esUva", true);
						} else if (PlazoFijo.esUvaPrecancelable(codigoInt.toString())) {
							plazoFijo.set("esUva", true);
							plazoFijo.set("esUvaPrecancelable", true);
						}

						plazoFijo.set("fechaAlta", item.fechaAlta("dd/MM/yyyy"));
						plazoFijo.set("fechaVencimiento", item.fechaVencimiento("dd/MM/yyyy"));
						plazoFijo.set("moneda", item.moneda());
						plazoFijo.set("monto", item.importeInicial());
						plazoFijo.set("montoFormateado", item.importeInicialFormateado());
						plazoFijo.set("tna", item.tnaFormateada());
						plazoFijo.set("interesesCobrar", item.interesesFormateado());
						plazoFijo.set("porcentajeTiempo", item.porcentajeDiasTranscurridos());
						plazoFijo.set("diasFaltantes", item.diasFaltantes());
						plazoFijo.set("renovacionAutomatica", item.tieneRenovacionAutomatica());
						plazoFijo.set("garantiaDeposito", item.tieneGarantiaDeposito());
						plazoFijo.set("descripcion", PlazoFijo.tipo(item.tipo()));
						plazoFijo.set("numero", item.numero());
						plazoFijo.set("plazo", item.plazo());
						plazoFijo.set("orden", item.fechaVencimiento().getTime());
						plazoFijo.set("tnaCancelacionAntFormateada", item.tnaCancelacionAntFormateada());
						plazoFijo.set("teaCancelacionAntFormateada", item.teaCancelacionAntFormateada());
						plazoFijo.set("fechaDesdeCancelacionAnt", item.fechaDesdeCancelacionAnt("dd/MM/yyyy"));
						plazoFijo.set("fechaHastaCancelacionAnt", item.fechaHastaCancelacionAnt("dd/MM/yyyy"));
						String leyendaUvaPrecancelable = devolverLeyendaUvaCancelacion(
								item.fechaDesdeCancelacionAnt("dd/MM/yyyy"),
								item.fechaHastaCancelacionAnt("dd/MM/yyyy"),
								item.tnaCancelacionAntFormateada(),
								item.teaCancelacionAntFormateada(),
								item.plazo(),
								item.fechaCancelacionAnt120("dd/MM/yyyy"));
						plazoFijo.set("leyendaUvaPrecancelable", leyendaUvaPrecancelable);

						try {
							if (item.numero() != null && !item.numero().isEmpty()) {
								contexto.parametros.set("nroCertificado", item.numero());
							}
							RespuestaMB detalle = detallePlazoFijo(contexto);
							if (!detalle.hayError()) {
								BigDecimal impuesto = item.importeInicial().add(item.intereses())
										.subtract(new BigDecimal(Formateador.importe(detalle.string("impuesto"))));
								detalle.set("montoAlVencimiento", Formateador.importe(impuesto));
								plazoFijo.set("montoAlVencimiento", Formateador.importe(impuesto));
								plazoFijo.set("detalle", detalle);
							} else {
								plazoFijo.set("detalle", new HashMap<>());
							}
						} catch (Exception e) {
							plazoFijo.set("detalle", new HashMap<>());
						}

						plazosFijos.add(plazoFijo);
					}
				});
			}
			Concurrencia.esperar(executorService, null, 60);
		} catch (Exception e) {
			// return Respuesta.error();
		}

		Collections.sort(plazosFijos, (o1, o2) -> o1.string("orden").compareTo(o2.string("orden")));

		BigDecimal totalPesos = new BigDecimal("0");
		BigDecimal totalDolares = new BigDecimal("0");
		BigDecimal totalAhorroJoven = new BigDecimal("0");
		for (Objeto plazoFijo : plazosFijos) {
			PlazoFijo item = contexto.plazoFijo(plazoFijo.string("id"));
			if (item.esPesos() && !item.esProcrearJoven()) {
				totalPesos = totalPesos.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosPesos", plazoFijo);
			}
			if (item.esDolares() && !item.esProcrearJoven()) {
				totalDolares = totalDolares.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosDolares", plazoFijo);
			}
			if (item.esProcrearJoven()) {
				totalAhorroJoven = totalAhorroJoven.add(plazoFijo.bigDecimal("monto"));
				respuesta.add("plazosFijosAhorroJoven", plazoFijo);
			}
		}

		respuesta.set("configuracion", obtenerConfiguracionPlazoFijo());
		respuesta.set("totalPesosFormateado", Formateador.importe(totalPesos));
		respuesta.set("totalDolaresFormateado", Formateador.importe(totalDolares));
		respuesta.set("totalProcrearJovenFormateado", Formateador.importe(totalAhorroJoven));

		return respuesta.ordenar("estado", "totalPesosFormateado", "totalDolaresFormateado", "totalProcrearJovenFormateado");
	}



	public static Objeto obtenerConfiguracionPlazoFijo(){
		Objeto configuracion = new Objeto();
		configuracion.set("montoMinimoTradicional", new BigDecimal("1000.00"));
		configuracion.set("montoMinimoTradicionalDolares", new BigDecimal("100.00"));
		configuracion.set("montoMinimoUVA", new BigDecimal("1000.00"));
		configuracion.set("montoMaximoUVA", new BigDecimal("100000000000.00"));
		configuracion.set("diasMinimoTradicional", 30);
		configuracion.set("diasMinimoUVA", 90);
		configuracion.set("diasMaximoUVA", 729);
		configuracion.set("montoPorDefecto", "1.000,00");
		configuracion.set("fechaEstandarTradicional", 36);

		return configuracion;
	}


	public static RespuestaMB detallePlazoFijo(ContextoMB contexto) {
		String nroCertificado = contexto.parametros.string("nroCertificado");

		if (Objeto.anyEmpty(nroCertificado)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		boolean precancelable = false;
		PlazoFijo plazoFijo = contexto.plazoFijo(nroCertificado);
		if (plazoFijo == null || plazoFijo.esUvaPrecancelable()) {
			// si viene nulo el plazo fijo de todas formas voy a permitir consultar el
			// detalle
			// si en un futuro agregamos los históricos, esto me puede servir.
			precancelable = true;
		}

		RespuestaMB respuesta = new RespuestaMB();
		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosGetDetalle", "plazosfijos", "GET", "/v1/plazosfijos/{nropf}", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
			request.path("nropf", nroCertificado);
			request.cacheSesion = true;
		} else {
			respuesta.set("apiWindows", true);
			return respuesta;
		}

		ApiResponseMB response = ApiMB.response(request, nroCertificado);
		for (Objeto item : response.objetos()) {
			respuesta.set("montoUva", item.string("montoUva"));
			respuesta.set("montoUvaFormateado", Formateador.importe(item.bigDecimal("montoUva")));
			respuesta.set("valorIndice", item.string("valorIndice"));
			respuesta.set("valorIndiceFormateado", Formateador.importe(item.bigDecimal("valorIndice")));
			respuesta.set("impuesto", Formateador.importe(item.bigDecimal("impuesto")));
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_sellos")) {
				respuesta.set("sellos", Formateador.importe(item.bigDecimal("sellos")));
			}
			
			//Se piden para el el envío de info a salesforce
			respuesta.set("renovacion", item.string("renovacion"));
			respuesta.set("renueva", item.string("renueva"));
			respuesta.set("renuevaInteres", item.string("renuevaInteres"));
			respuesta.set("nroRenovacion", item.integer("nroRenovacion"));

			Cuenta cuenta = contexto.cuenta(item.string("cuenta"));
			respuesta.set("cuenta", "");
			if (cuenta == null) {
				respuesta.set("cuenta", "CA XXXX-" + Formateador.ultimos4digitos(item.string("cuenta")));
			} else {
				respuesta.set("cuenta", Formateador.tipoCuenta(cuenta.idTipo()) + " XXXX-" + Formateador.ultimos4digitos(cuenta.numero()));
			}

			respuesta.set("permiteSolicitudCancelacionAnticipada", false);
			respuesta.set("solicitudCancelacionAnticipadaCargada", false);

			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_precancelar_plazo_fijo_cer", "prendido_precancelar_plazo_fijo_cer_cobis") && precancelable) {

				ApiResponseMB responseCancelacion = PlazoFijoService.consultarInformacionCancelacionAnticipadaCER(contexto, nroCertificado);

				if (!responseCancelacion.hayError()) { // si hay error prefiero hacer como que no se puede cancelar
					if ("S".equals(responseCancelacion.string("permiteSolicitud")) && "S".equals(responseCancelacion.string("enfechaDeSolicitud"))) {
						respuesta.set("permiteSolicitudCancelacionAnticipada", true);
					}

					ApiResponseMB responseEstado = PlazoFijoService.consultarEstadoCancelacionAnticipadaCER(contexto, nroCertificado);
					if (responseEstado.hayError()) {
						respuesta.set("estadoSolicitudCancelacionAnticipada", "");
					} else {
						if (!"".equals(responseEstado.string("estado"))) {
							respuesta.set("solicitudCancelacionAnticipadaCargada", true);
						}
						respuesta.set("estadoSolicitudCancelacionAnticipada", PlazoFijo.estadoSolicitudCancelacionAnticipada(responseEstado.string("estado")));
					}

					respuesta.set("interesCancelacionAnticipada", responseCancelacion.bigDecimal("interesCancelacionAnt"));
					respuesta.set("interesCancelacionAnticipadaFormateada", Formateador.importe(responseCancelacion.bigDecimal("interesCancelacionAnt")));
					respuesta.set("montoFinalCancelacionAnticipada", responseCancelacion.bigDecimal("monto"));
					respuesta.set("montoFinalCancelacionAnticipadaFormateada", Formateador.importe(responseCancelacion.bigDecimal("monto")));
					respuesta.set("fechaCancelacionAnticipada", responseCancelacion.string("fechaCancelacion"));
					respuesta.set("tasaCancelacionAnticipadaModal", Formateador.importe(responseCancelacion.bigDecimal("tasaCancelacionAnt")));
					respuesta.set("teaCancelacionAnticipadaModal", Formateador.importe(responseCancelacion.bigDecimal("teaCancelacionAnt")));
				}
				/*
				 * respuesta.set("permiteSolicitudCancelacionAnticipada", true); //esto es para
				 * probar, se tiene que comentar
				 * respuesta.set("solicitudCancelacionAnticipadaCargada", false); //esto es para
				 * probar, se tiene que comentar
				 */
			}

		}

		return respuesta;
	}

	public static RespuestaMB precancelarPlazoFijoUvaCer(ContextoMB contexto) {

		String nroCertificado = contexto.parametros.string("nroCertificado");

		if (Objeto.anyEmpty(nroCertificado)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		ApiResponseMB response = PlazoFijoService.precancelarPlazoFijoUvaCer(contexto, nroCertificado);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		//AGREGAR SALESFORCE
		if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				Objeto parametros = new Objeto();
				contexto.parametros.set("nroCertificado", nroCertificado);
				RespuestaMB pf = detallePlazoFijo(contexto);
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
				parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				parametros.set("MONEDA", "$");
				parametros.set("CAPITAL", Formateador.importe(response.bigDecimal("monto")));
				parametros.set("NUMERO_PLAZO_FIJO",  nroCertificado);
				parametros.set("TNA", response.string("tasaCancelacionAnt"));
				parametros.set("PLAZO_CONSTITUCION_DIAS", cantidadDiasPreCan(response.string("fechaInicioCanAnt"), response.string("fechaFinCanAnt")));
				parametros.set("FECHA_VENCIMIENTO", response.string("fechaFinCanAnt"));
				parametros.set("RENOVACION_AUTOMATICA", pf.string("renueva"));
				parametros.set("GARANTIA_DEPOSITOS", null);
				parametros.set("TIPO_PLAZO_FIJO", null);
				parametros.set("FECHA_CONSTITUCION", response.string("fechaInicioCanAnt"));
				parametros.set("MONTO_ESTIMADO_VENCIMIENTO", Formateador.importe(response.bigDecimal("monto")));
				parametros.set("INTERESES_ESTIMADOS_COBRAR",  Formateador.importe(response.bigDecimal("interesCancelacionAnt")));
				parametros.set("COTIZACION_UVA", pf.string("valorIndiceFormateado"));
				parametros.set("MONTO_INICIAL_UVA", pf.get("montoUvaFormateado"));
				parametros.set("TNA_CANCELACION_ANTICIPADA", response.bigDecimal("tasaCancelacionAnt").toString());
				parametros.set("TEA_CANCELACION_ANTICIPADA",  response.bigDecimal("teaCancelacionAnt").toString());
				parametros.set("FECHA_CANCELACION_BAJA",  new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
				parametros.set("RENOVAR_MONTO_MAS_INTERESES", pf.string("renuevaInteres"));
				parametros.set("RENOVAR_MONTO", pf.string("renueva"));
				parametros.set("CANTIDAD_RENOVACIONES", null);
				BigDecimal tea = calcularTEA(response.bigDecimal("monto"), response.bigDecimal("interesCancelacionAnt"), 
						cantidadDiasPreCan(response.string("fechaInicioCanAnt"), response.string("fechaFinCanAnt")));
				BigDecimal tem = calcularTEM(tea);
				parametros.set("TEA",  tea);
				parametros.set("TEM", tem);
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_solicitud_cancelacion_anticipada_pf_uva"), parametros));
				}
			catch(Exception e) {
			}
		}
		
		ProductosService.eliminarCacheProductos(contexto);

		return RespuestaMB.exito();
	}

	public static Map<String, String> armarDatosComprobantePlazoFijo(ContextoMB contexto, PlazoFijo plazoFijo, boolean cancelado) {

		String interesesFormateado = "";
		String tasaNominalFormateada = "";
		Integer plazo = 0;
		Boolean tieneRenovacionAutomatica = false;
		Boolean tieneGarantiaDeposito = false;

		String cuentaString = "";
		String montoUvaFormateado = "";
		String valorIndiceFormateado = "";
		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		if (cancelado) {
			ApiRequestMB request = null;
			if (habilitarPlazosFijosApi) {
				request = ApiMB.request("PlazosFijosGet", "plazosfijos", "GET", "/v1/{idCobis}", contexto);
				request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
			} else {
				request = ApiMB.request("PlazosFijosWindowsGet", "plazosfijos_windows", "GET", "/v1/{idCobis}", contexto);
			}

			request.path("idCobis", contexto.idCobis());
			request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
			request.query("fechaFin", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
			request.query("certificado", plazoFijo.numero());
			request.cacheSesion = true;
			ApiResponseMB response = ApiMB.response(request, plazoFijo.numero());
			if (!response.hayError()) {
				for (Objeto objeto : response.objetos()) {
					interesesFormateado = Formateador.importe(objeto.bigDecimal("montoEstimado"));
					plazo = objeto.integer("plazo");
					tasaNominalFormateada = Formateador.importe(objeto.bigDecimal("tasaNominal"));
					tieneRenovacionAutomatica = "S".equals(objeto.string("renovacion"));
					tieneGarantiaDeposito = "S".equals(objeto.string("garantizado"));
				}
			} else {
				plazoFijo.existenErrores = true;
			}
		} else {
			interesesFormateado = plazoFijo.interesesFormateado();
			plazo = plazoFijo.plazo();
			tasaNominalFormateada = plazoFijo.tnaFormateada();
			tieneRenovacionAutomatica = plazoFijo.tieneRenovacionAutomatica();
			tieneGarantiaDeposito = plazoFijo.tieneGarantiaDeposito();
		}

		ApiRequestMB requestDetalle = null;
		if (habilitarPlazosFijosApi) {
			requestDetalle = ApiMB.request("PlazosFijosGetDetalle", "plazosfijos", "GET", "/v1/plazosfijos/{nropf}", contexto);
			requestDetalle.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
			requestDetalle.path("nropf", plazoFijo.numero());

			ApiResponseMB responseDetalle = ApiMB.response(requestDetalle, plazoFijo.numero());
			for (Objeto item : responseDetalle.objetos()) {

				montoUvaFormateado = Formateador.importe(item.bigDecimal("montoUva"));
				// respuesta.set("montoUvaFormateado",
				// Formateador.importe(item.bigDecimal("montoUva")));
				valorIndiceFormateado = Formateador.importe(item.bigDecimal("valorIndice"));
				// respuesta.set("valorIndiceFormateado",
				// Formateador.importe(item.bigDecimal("valorIndice")));

				Cuenta cuenta = contexto.cuentaExtendido(item.string("cuenta"));
				if (cuenta != null) {
					cuentaString = Formateador.tipoCuenta(cuenta.idTipo()) + " XXXX-" + Formateador.ultimos4digitos(cuenta.numero());
				}
			}
		} else {

		}

		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("ID_PLAZO_FIJO", plazoFijo.numero());
		comprobante.put("CUENTA_PLAZO_FIJO", cuentaString);
		comprobante.put("FECHA_HORA", plazoFijo.fechaAlta("dd/MM/yyyy"));
		comprobante.put("TIPO_PLAZO", PlazoFijo.tipo(plazoFijo.tipo()));
		comprobante.put("MONTO_INICIAL", plazoFijo.moneda() + " " + plazoFijo.importeInicialFormateado());
		comprobante.put("MONTO_AL_VENCIMIENTO", plazoFijo.moneda() + " " + plazoFijo.importeFinalFormateado());
		comprobante.put("INTERES_A_COBRAR", plazoFijo.moneda() + " " + interesesFormateado);
		comprobante.put("FECHA_VENCIMIENTO", plazoFijo.fechaVencimiento("dd/MM/yyyy"));
		comprobante.put("NRO_REFERENCIA", plazoFijo.numero());
		comprobante.put("FECHA_CONSTITUCION", plazoFijo.fechaAlta("dd/MM/yyyy"));
		if (plazo == null) {
			comprobante.put("PLAZO_EN_DIAS", "");
		} else {
			comprobante.put("PLAZO_EN_DIAS", plazo.toString());
		}
		comprobante.put("TASA_NOMINAL_ANUAL", tasaNominalFormateada + "%");
		comprobante.put("RENOVACION_AUTOMATICA", tieneRenovacionAutomatica ? "SI" : "NO");
		comprobante.put("GARANTIA_DEPOSITO", tieneGarantiaDeposito ? "SI" : "NO");

		comprobante.put("LABEL_CANC_INTERES_FINAL", "");
		comprobante.put("LABEL_CANC_MONTO", "");
		comprobante.put("LABEL_CANC_FECHA", "");
		comprobante.put("LABEL_CANC_ESTADO", "");

		comprobante.put("TEXT_CANC_INTERES_FINAL", "");
		comprobante.put("TEXT_CANC_MONTO", "");
		comprobante.put("TEXT_CANC_FECHA", "");
		comprobante.put("TEXT_CANC_ESTADO", "");

		if (plazoFijo.esUva()) {
			comprobante.put("LABEL_MONTO_VENC", "Monto estimado al vencimiento:");
			comprobante.put("LABEL_INTERES_COBRAR", "Intereses estimados a cobrar:");
			comprobante.put("LABEL_MONTO_UVA", "Monto Inicial en UVA:");
			comprobante.put("LABEL_COTIZACION_UVA", "Cotización UVA al día de la constitución:");
			comprobante.put("UVA_MONTO", montoUvaFormateado);
			comprobante.put("COTIZACION_CONSTITUCION_UVA", "$ " + valorIndiceFormateado);
			String legalUva = "AL VENCIMIENTO EL MONTO INICIAL SERÁ ACTUALIZADO A LA COTIZACIÓN UVA VIGENTE SOBRE EL QUE SE CALCULARÁN LOS INTERESES A COBRAR.";
			if (plazoFijo.esUvaPrecancelable()) {
				// TODO DLV-50929 nueva tasa 120 dias
				// String leyendaUvaCancelacion =
				// devolverLeyendaUvaCancelacion(plazoFijo.fechaDesdeCancelacionAnt("dd/MM/yyyy"),
				// plazoFijo.fechaHastaCancelacionAnt("dd/MM/yyyy"),
				// plazoFijo.tnaCancelacionAntFormateada(),
				// plazoFijo.teaCancelacionAntFormateada());
				String leyendaUvaCancelacion = devolverLeyendaUvaCancelacion(plazoFijo.fechaDesdeCancelacionAnt("dd/MM/yyyy"), plazoFijo.fechaHastaCancelacionAnt("dd/MM/yyyy"), plazoFijo.tnaCancelacionAntFormateada(), plazoFijo.teaCancelacionAntFormateada(), plazoFijo.plazo(), plazoFijo.fechaCancelacionAnt120("dd/MM/yyyy"));

				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&aacute;", "á");
				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&eacute;", "é");
				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&iacute;", "í");
				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&oacute;", "ó");
				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&uacute;", "ú");
				leyendaUvaCancelacion = leyendaUvaCancelacion.replace("&quot;", "\"");
				legalUva = legalUva + "\n" + leyendaUvaCancelacion;
			}

			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_precancelar_plazo_fijo_cer", "prendido_precancelar_plazo_fijo_cer_cobis") && plazoFijo.esUvaPrecancelable()) {
				ApiResponseMB responseCancelacion = PlazoFijoService.consultarInformacionCancelacionAnticipadaCER(contexto, plazoFijo.numero());
				String estadoSolicitud = "";
				if (!responseCancelacion.hayError()) { // si hay error prefiero hacer como que no se puede cancelar
					ApiResponseMB responseEstado = PlazoFijoService.consultarEstadoCancelacionAnticipadaCER(contexto, plazoFijo.numero());
					if (!responseEstado.hayError()) {
						estadoSolicitud = PlazoFijo.estadoSolicitudCancelacionAnticipada(responseEstado.string("estado"));
					}
					if (!"".equals(estadoSolicitud)) {
						comprobante.put("LABEL_CANC_INTERES_FINAL", "Intereses finales a cobrar:");
						comprobante.put("LABEL_CANC_MONTO", "Monto final al vencimiento:");
						comprobante.put("LABEL_CANC_FECHA", "Fecha de cancelación:");
						comprobante.put("LABEL_CANC_ESTADO", "Estado:");

						comprobante.put("TEXT_CANC_INTERES_FINAL", "$ " + Formateador.importe(responseCancelacion.bigDecimal("interesCancelacionAnt")));
						comprobante.put("TEXT_CANC_MONTO", "$ " + Formateador.importe(responseCancelacion.bigDecimal("monto")));
						comprobante.put("TEXT_CANC_FECHA", responseCancelacion.string("fechaCancelacion"));
						comprobante.put("TEXT_CANC_ESTADO", estadoSolicitud);
					}
				}
			}

			comprobante.put("LEGAL_UVA", legalUva);
		} else {
			comprobante.put("LABEL_MONTO_VENC", "Monto al vencimiento:");
			comprobante.put("LABEL_INTERES_COBRAR", "Intereses a cobrar:");
			comprobante.put("LABEL_MONTO_UVA", "");
			comprobante.put("LABEL_COTIZACION_UVA", "");
			comprobante.put("UVA_MONTO", "");
			comprobante.put("COTIZACION_CONSTITUCION_UVA", "");
			comprobante.put("LEGAL_UVA", "");
		}

		if (Objeto.setOf("0003", "0004", "0005", "0010", "0011", "0012", "0013").contains(plazoFijo.tipo())) {
			comprobante.put("LEGAL_RENOVACION_AUTOMATICA", "En caso de renovación automática resultará de aplicación la tasa de interés máxima vigente el día de la efectiva renovación, conforme el respectivo canal de originación.");
		} else {
			comprobante.put("LEGAL_RENOVACION_AUTOMATICA", "");
		}
		return comprobante;
	}

	public static byte[] comprobantePlazosFijo(ContextoMB contexto) {
		String idPlazoFijo = contexto.parametros.string("idPlazoFijo");

		PlazoFijo plazoFijo = contexto.plazoFijo(idPlazoFijo);
		boolean cancelado = false;

		if (plazoFijo == null) { // no lo encontró porque probablemente ya no venga en la consolidada porque está
									// finalizado
									// esto puede pasar con un plazo fijo de logros, por ahora lo busco entre los
									// cancelados.
									// Igual hay que tener en cuenta que lo ideal es buscarlo en el detalle

			plazoFijo = contexto.plazoFijoBuscadoEntreCancelados(idPlazoFijo);
			cancelado = true;

		}

		Map<String, String> comprobante = armarDatosComprobantePlazoFijo(contexto, plazoFijo, cancelado);

		String idComprobante = "plazo-fijo" + "_" + plazoFijo.id();
		contexto.sesion().setComprobante(idComprobante, comprobante);
		contexto.parametros.set("id", idComprobante);

		return MBArchivo.comprobante(contexto);
	}

	public static RespuestaMB tiposPlazosFijos(ContextoMB contexto) {

		// TODO: Se retira por Normativo “A” 7849
		/*
		 * if (contexto.persona().esMenor()) { return
		 * RespuestaMB.estado("MENOR_NO_AUTORIZADO"); }
		 */

		List<ApiResponseMB> listResponse = PlazoFijoService.tasas(contexto);
		for (ApiResponseMB response : listResponse) {
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		}

		Objeto tiposPlazoFijo = new Objeto();
		Set<String> idsTipoPlazoFijos = new TreeSet<>();
		for (ApiResponseMB response : listResponse) {
			for (Objeto item : response.objetos("tasas")) {
				String codigo = item.string("idTipoDeposito");
				if (Integer.valueOf(codigo) < 25 || Integer.valueOf(codigo).equals(42) || Integer.valueOf(codigo).equals(43)) {
					String id = codigo + "_" + item.string("idMoneda");
					if (PlazoFijo.esUvaNoCancelable(codigo)) {
						id += "_uva";
					} else {
						if (PlazoFijo.esUvaPrecancelable(codigo)) {
							id += "_uvaprecancelable";
						} else {
							id += "_noesuva";
						}
					}

					if (!idsTipoPlazoFijos.contains(id) && !PlazoFijo.tipo(codigo).isEmpty()) {
						Objeto tipoPlazoFijo = new Objeto();
						tipoPlazoFijo.set("id", id);
						tipoPlazoFijo.set("descripcion", PlazoFijo.tipo(codigo) + (Integer.valueOf(codigo) <= 13 ? " en " + Formateador.moneda(item.string("idMoneda")) : ""));
						tipoPlazoFijo.set("idMoneda", item.integer("idMoneda"));
						tipoPlazoFijo.set("moneda", Formateador.simboloMoneda(item.string("idMoneda")));
						tipoPlazoFijo.set("orden", id.replace("_2", "_92"));

						tipoPlazoFijo.set("montoMinimo", ConfigMB.bigDecimal("monto_minimo_plazo_fijo_" + id));
						BigDecimal montoMaximo = ConfigMB.bigDecimal("monto_maximo_plazo_fijo_" + id);
						if(montoMaximo != null){
							tipoPlazoFijo.set("montoMaximo", montoMaximo);
						}

						tipoPlazoFijo.set("diasMinimo", ConfigMB.integer("dias_minimo_plazo_fijo_" + id));
						int diasMaximo = ConfigMB.integer("dias_maximo_plazo_fijo_" + id, 0);
						if(diasMaximo > 0){
							tipoPlazoFijo.set("diasMaximo", diasMaximo);
						}

						tiposPlazoFijo.add(tipoPlazoFijo);
						idsTipoPlazoFijos.add(id);
					}
				}
			}
		}

		tiposPlazoFijo.ordenar("orden");
		for (Objeto item : tiposPlazoFijo.objetos()) {
			item.set("orden", null);
		}

		return RespuestaMB.exito("tiposPlazosFijos", tiposPlazoFijo);
	}

	public static RespuestaMB simularPlazoFijo(ContextoMB contexto) {
		String idTipoPlazoFijo = contexto.parametros.string("idTipoPlazoFijo");
		Integer plazo = contexto.parametros.integer("plazo");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idTipoPlazoFijo, plazo, monto)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idTipoPlazoFijo.split("_")[1];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
		}

		Objeto montoPlazoValido = montoPlazoValido(contexto, idTipoPlazoFijo, plazo, monto);
		if (montoPlazoValido.bool("error")) {
			return RespuestaMB.error();
		}
		if (!montoPlazoValido.bool("valido")) {
			return RespuestaMB.estado("MONTO_PLAZO_INVALIDO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);

		Boolean habilitarSimuladorApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_simulador_api");
		ApiRequestMB request = null;
		if (habilitarSimuladorApi) {
			request = ApiMB.request("SimuladoresGetPlazoFijos", "simuladores", "GET", "/v1/plazoFijos", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("SimuladoresWindowsGetPlazoFijos", "simuladores_windows", "GET", "/v1/plazoFijos", contexto);
			request.query("canal", "26");
		}

		request.query("idcliente", contexto.idCobis());
		request.query("tipoOperacion", idTipoPlazoFijo.split("_")[0]);
		request.query("plazo", plazo.toString());
		request.query("monto", monto.toString());
		request.query("moneda", idTipoPlazoFijo.split("_")[1]);
		if (cuenta != null) {
			request.query("cuenta", cuenta.numero());
			request.query("tipoCuenta", cuenta.idTipo());
		}

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {

			if ("123008".equals(response.string("codigo")) && contexto.esProcrear(contexto)) {
				return RespuestaMB.estado("OPERACION_NO_POSIBLE_PROCREAR");
			}

			if ("123008".equals(response.string("codigo"))) {
				return RespuestaMB.estado("OPERACION_NO_POSIBLE");
			}

			if (contexto.esProcrear(contexto)) {
				return RespuestaMB.estado("OPERACION_NO_POSIBLE_PROCREAR");
			}

			return RespuestaMB.error();
		}

		for (Objeto item : response.objetos()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date fechaVencimiento = item.date("fechaVencimiento", "yyyy-MM-dd'T'HH:mm:ss");

			Objeto simulacion = new Objeto();
			simulacion.set("interesesCobrar", Formateador.importe(item.bigDecimal("totalInteresEstimado")));
			simulacion.set("montoVencimiento", Formateador.importe(item.bigDecimal("montoTotal")));
			simulacion.set("tna", Formateador.importe(item.bigDecimal("tasa")));
			simulacion.set("fechaVencimientoReal", sdf.format(fechaVencimiento));
			simulacion.set("fechaVencimientoTeorica", sdf.format(Fecha.sumarDias(new Date(), Long.valueOf(plazo))));
			simulacion.set("cantidadDiasReal", Fecha.cantidadDias(new Date(), fechaVencimiento) /* + 1 */); // TODO: cuidado que en desa modificaron esto
			simulacion.set("cantidadDiasTeorica", Fecha.cantidadDias(new Date(), Fecha.sumarDias(new Date(), Long.valueOf(plazo))));
			String fechaDesdeCancelacionAnt = "";
			String fechaHastaCancelacionAnt = "";

			if (!"".equals(item.string("fechaDesdeCancelacionAnt")) && item.date("fechaDesdeCancelacionAnt", "yyyy-MM-dd") != null) {
				try {
					fechaDesdeCancelacionAnt = sdf.format(item.date("fechaDesdeCancelacionAnt", "yyyy-MM-dd"));
					simulacion.set("fechaDesdeCancelacionAnt", fechaDesdeCancelacionAnt);
				} catch (Exception e) {

				}
			}

			if (!"".equals(item.string("fechaHastaCancelacionAnt")) && item.date("fechaHastaCancelacionAnt", "yyyy-MM-dd") != null) {
				try {
					fechaHastaCancelacionAnt = sdf.format(item.date("fechaHastaCancelacionAnt", "yyyy-MM-dd"));
					simulacion.set("fechaHastaCancelacionAnt", fechaHastaCancelacionAnt);
				} catch (Exception e) {

				}
			}

			// TODO DLV-50929 nueva tasa 120 dias
			String fechaCancelacionAnt120 = "";
			if (!"".equals(item.string("fechaCancelacionLeliq120")) && item.string("fechaCancelacionLeliq120") != null) {
				try {
					fechaCancelacionAnt120 = sdf.format(item.date("fechaCancelacionLeliq120", "yyyy/MM/dd"));
					simulacion.set("fechaCancelacionLeliq120", fechaCancelacionAnt120);
				} catch (Exception e) {

				}
			}

			String leyendaUva = ConfigMB.string("leyenda_uva_precancelable_solo_simulacion", "");
			leyendaUva = leyendaUva.replace("FECHA_DESDE", fechaDesdeCancelacionAnt);
			leyendaUva = leyendaUva.replace("FECHA_HASTA", fechaHastaCancelacionAnt);
			leyendaUva = leyendaUva.replace("TNA_CANCELACION_ANT_FORMATEADO", Formateador.importe(item.bigDecimal("tnaCancelacionAnt")));
			leyendaUva = leyendaUva.replace("TEACANCELACIONANTFORMATEADO", Formateador.importe(item.bigDecimal("teaCancelacionAnt")));
			String leyendaMontoSuperiores = ConfigMB.string("leyenda_montos_superiores_plazofijo", "");
			// String leyendaUvaPrecancelable = leyendaUva +
			// devolverLeyendaUvaCancelacion(fechaDesdeCancelacionAnt,
			// fechaHastaCancelacionAnt,
			// Formateador.importe(item.bigDecimal("tnaCancelacionAnt")),
			// Formateador.importe(item.bigDecimal("teaCancelacionAnt")));
			// String leyendaUvaPrecancelable = leyendaUva +
			// devolverLeyendaUvaCancelacion(fechaDesdeCancelacionAnt,
			// fechaHastaCancelacionAnt,
			// Formateador.importe(item.bigDecimal("tnaCancelacionAnt")),
			// Formateador.importe(item.bigDecimal("teaCancelacionAnt")), plazo,
			// Formateador.importe(item.bigDecimal("tnaCancelacionLeliq120")),
			// Formateador.importe(item.bigDecimal("teaCancelacionLeliq120")),
			// fechaCancelacionAnt120);

			simulacion.set("tnaCancelacionAntFormateado", Formateador.importe(item.bigDecimal("tnaCancelacionAnt")));
			simulacion.set("teaCancelacionAntFormateado", Formateador.importe(item.bigDecimal("teaCancelacionAnt")));
			simulacion.set("montoUvaFormateado", Formateador.importe(item.bigDecimal("montoUVA")));
			simulacion.set("cotizacionUvaFormateado", Formateador.importe(item.bigDecimal("cotizacionUVA")));
			simulacion.set("garantiaDeDepositos", item.string("gtiaDeDepositos").equals("S") ? true : false);
			simulacion.set("leyendaUvaPrecancelable", leyendaUva);
			simulacion.set("leyendaMontoSuperiores", leyendaMontoSuperiores);
			simulacion.set("plazoIngresado", plazo);
			simulacion.set("plazoSimulado", item.integer("plazo"));

			try {
				if (item.get("impuestosAPagar") != null) {
					simulacion.set("impuestosAPagar", Formateador.importe(item.bigDecimal("impuestosAPagar")));
				} else {
					simulacion.set("impuestosAPagar", "0");
				}
				if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_sellos")) {
					if (item.get("sellos") != null) {
						simulacion.set("sellos", Formateador.importe(item.bigDecimal("sellos")));
					} else {
						simulacion.set("sellos", "0");
					}
				}

			} catch (Exception e) {
				simulacion.set("impuestosAPagar", "0");
				simulacion.set("sellos", "0");
			}

			return RespuestaMB.exito("simulacion", simulacion);
		}

		return RespuestaMB.error();
	}

	// TODO DLV-50929 nueva tasa 120 dias
	private static String devolverLeyendaUvaCancelacion(String fechaDesdeCancelacionAnt, String fechaHastaCancelacionAnt, String tnaCancelacionAnt, String teaCancelacionAnt, Integer cantidadDias, String fechaCancelacionAnt120) {
		// private static String devolverLeyendaUvaCancelacion(String
		// fechaDesdeCancelacionAnt, String fechaHastaCancelacionAnt, String
		// tnaCancelacionAnt, String teaCancelacionAnt) {
		String leyendaUvaPrecancelable = ConfigMB.string("leyenda_uva_precancelable", "");
		String leyendaUvaPrecancelable180Dias = "";
		String fechasLeyenda = ". Record&aacute; que pod&eacute;s solicitar la cancelaci&oacute;n anticipada con 5 d&iacute;as h&aacute;biles de antelaci&oacute;n";
		if (!"".equals(fechaDesdeCancelacionAnt)) {
			fechasLeyenda = " (podes solicitarlo desde el " + fechaDesdeCancelacionAnt + " hasta el " + fechaHastaCancelacionAnt + ")";
		}
		leyendaUvaPrecancelable = leyendaUvaPrecancelable.replace("FECHAS_CANCELACION_ANT", fechasLeyenda);
		leyendaUvaPrecancelable = leyendaUvaPrecancelable.replace("TNA_CANCELACION_ANT_FORMATEADO", tnaCancelacionAnt);
		leyendaUvaPrecancelable = leyendaUvaPrecancelable.replace("TEACANCELACIONANTFORMATEADO", teaCancelacionAnt);


		return leyendaUvaPrecancelable + leyendaUvaPrecancelable180Dias;
	}

	@SuppressWarnings("null")
	public static RespuestaMB altaPlazoFijo(ContextoMB contexto) {
		String idTipoPlazoFijo = contexto.parametros.string("idTipoPlazoFijo");
		Integer plazo = contexto.parametros.integer("plazo");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String idCuenta = contexto.parametros.string("idCuenta");
		Integer cantidadRenovaciones = contexto.parametros.integer("cantidadRenovaciones", 0);
		Boolean renovarInteres = contexto.parametros.bool("renovarInteres", false);

		if (Objeto.anyEmpty(idTipoPlazoFijo, plazo, monto, idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idTipoPlazoFijo.split("_")[1];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
		}

		Objeto montoPlazoValido = montoPlazoValido(contexto, idTipoPlazoFijo, plazo, monto);
		if (montoPlazoValido.bool("error")) {
			return RespuestaMB.error();
		}
		if (!montoPlazoValido.bool("valido")) {
			return RespuestaMB.estado("MONTO_PLAZO_INVALIDO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("CUENTA_NO_EXISTE");
		}

		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsPost", "plazosfijos_windows", "POST", "/v1/plazoFijos", contexto);
		}
		request.body("canal", 26);
		request.body("capInteres", renovarInteres ? "S" : "N");
		request.body("cuenta", cuenta.numero());
		request.body("idPlanAhorro", null);
		request.body("idcliente", contexto.idCobis());
		request.body("moneda", Integer.parseInt(idTipoPlazoFijo.split("_")[1]));
		request.body("monto", monto);
		request.body("nroOperacion", 1);
		request.body("periodo", cantidadRenovaciones);
		request.body("plazo", plazo);
		request.body("renova", cantidadRenovaciones > 0 ? "S" : "N");
		request.body("reverso", null);
		request.body("tipoCuenta", cuenta.idTipo());
		request.body("tipoOperacion", idTipoPlazoFijo.split("_")[0]);
		request.body("usuarioAlta", null);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("258402".equals(response.string("codigo"))) {
				return RespuestaMB.estado("SIN_PERFIL_PATRIMONIAL");
			}
			if ("141144".equals(response.string("codigo"))) {
				return RespuestaMB.estado("SALDO_INSUFICIENTE");
			}
			return RespuestaMB.error();
		}

		ProductosService.eliminarCacheProductos(contexto);

		String nroPlazoFijo = response.string("nroPlazoFijo");
		RespuestaMB respuesta = new RespuestaMB();

		PlazoFijo plazoFijo = contexto.plazoFijo(nroPlazoFijo);
		if (plazoFijo != null) {

			Map<String, String> comprobante = armarDatosComprobantePlazoFijo(contexto, plazoFijo, false);

			String idComprobante = "plazo-fijo" + "_" + plazoFijo.id();
			contexto.sesion().setComprobante(idComprobante, comprobante);
			contexto.parametros.set("id", idComprobante);
			respuesta.set("idComprobante", idComprobante);

		}

		//AGREGAR SALESFORCE
		if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				Objeto parametros = new Objeto();
				contexto.parametros.set("nroCertificado", nroPlazoFijo);
				RespuestaMB pf = detallePlazoFijo(contexto);
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				parametros.set("MONEDA", Formateador.simboloMoneda(response.string("moneda")));
				parametros.set("CAPITAL", Formateador.importe(response.bigDecimal("monto")));
				parametros.set("NUMERO_PLAZO_FIJO",  response.string("nroPlazoFijo"));
				BigDecimal TNA = response.bigDecimal("tasa");
                BigDecimal TEA = calcularTEA(response.bigDecimal("capital"), response.bigDecimal("totalInteresEstimado"), response.integer("plazo"));
                BigDecimal TEM = calcularTEM(TEA);
                parametros.set("TNA", TNA);
                parametros.set("TEA", TEA);
                parametros.set("TEM", TEM);
				parametros.set("PLAZO_CONSTITUCION_DIAS", response.string("plazo"));
				parametros.set("FECHA_VENCIMIENTO", response.string("fechaVencimiento"));
				parametros.set("RENOVACION_AUTOMATICA", cantidadRenovaciones > 0 ? "S" : "N");
				parametros.set("GARANTIA_DEPOSITOS", response.string("cubiertoPorGarantia"));
				parametros.set("TIPO_PLAZO_FIJO", response.string("tipoOperacion"));
				parametros.set("FECHA_CONSTITUCION", response.date("fechaActual", "yyyy-MM-dd", "dd/MM/yyyy"));
				parametros.set("RENOVAR_MONTO_MAS_INTERESES", cantidadRenovaciones > 0 && renovarInteres ? "S" : "N");
				parametros.set("RENOVAR_MONTO", cantidadRenovaciones > 0 && !renovarInteres ? "S" : "N");
				parametros.set("CANTIDAD_RENOVACIONES", cantidadRenovaciones);
				if(plazoFijo.esUva()) {
					parametros.set("MONTO_ESTIMADO_VENCIMIENTO", Formateador.importe(response.bigDecimal("monto")));
					parametros.set("INTERESES_ESTIMADOS_COBRAR",  Formateador.importe(response.bigDecimal("totalInteresEstimado")));
					parametros.set("COTIZACION_UVA", pf.get("valorIndiceFormateado"));
					parametros.set("MONTO_INICIAL_UVA", pf.get("montoUvaFormateado"));
					if(plazoFijo.esUvaPrecancelable()) {
						parametros.set("TNA_CANCELACION_ANTICIPADA", response.bigDecimal("tasaCancelacionAnt").toString());
						parametros.set("TEA_CANCELACION_ANTICIPADA",  response.bigDecimal("tnaCancelacionAnt").toString());
						parametros.set("FECHA_CANCELACION_BAJA",  null);
						MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_alta_plazo_fijo_uva_precancelable"), parametros);
					}
					else {
						parametros.set("INTERES_COBRAR", Formateador.importe(response.bigDecimal("interesEstimado")));
						MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_alta_plazo_fijo_uva"), parametros);
					}

				}
				else {
					parametros.set("INTERES_COBRAR", Formateador.importe(response.bigDecimal("interesEstimado")));
					parametros.set("MONTO_VENCIMIENTO", Formateador.importe(response.bigDecimal("monto")));
					if(esTasaPreferencial(contexto, Integer.parseInt(idTipoPlazoFijo.split("_")[1]), monto, plazo))
						MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_alta_plazo_fijo_tasa_preferencial"), parametros);
					else
						MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_alta_plazo_fijo"), parametros);
				}
			}
			catch(Exception e) {
				
			}
		}

		return respuesta;

		// return Respuesta.exito();
	}

	public static RespuestaMB bajaRenovacionPlazoFijo(ContextoMB contexto) {
		String idPlazoFijo = contexto.parametros.string("idPlazoFijo");

		if (Objeto.anyEmpty(idPlazoFijo)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		PlazoFijo plazoFijo = contexto.plazoFijo(idPlazoFijo);
		if (plazoFijo == null) {
			return RespuestaMB.estado("PLAZO_FIJO_NO_EXISTE");
		}

		ApiRequestMB request = ApiMB.request("PlazosFijosPostStopRenovacion", "plazosfijos", "PATCH", "/v1/plazosfijos/{idplazofijo}", contexto);
		request.path("idplazofijo", plazoFijo.numero());

		ApiResponseMB response = ApiMB.response(request, plazoFijo.numero());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		//AGREGAR SALESFORCE
		if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				ApiRequestMB requestPfGet = null;
				requestPfGet = ApiMB.request("PlazosFijosGetDetalle", "plazosfijos", "GET", "/v1/plazosfijos/{nropf}", contexto);
				requestPfGet.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
				requestPfGet.path("nropf", plazoFijo.numero());

				Objeto respuesta = Optional.ofNullable(ApiMB.response(requestPfGet, plazoFijo.numero()).objetos())
						.filter(obj -> !obj.isEmpty()).map(obj -> obj.get(0)).orElse(null);
				Objeto parametros = new Objeto();
				contexto.parametros.set("nroCertificado", idPlazoFijo);
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				parametros.set("MONEDA", Formateador.simboloMoneda(respuesta.string("idMoneda")));
				parametros.set("CAPITAL", Formateador.importe(respuesta.bigDecimal("capital")));
				parametros.set("NUMERO_PLAZO_FIJO",  plazoFijo.numero());
				BigDecimal TNA = respuesta.bigDecimal("tasa");
				BigDecimal TEA = calcularTEA(TNA, respuesta.bigDecimal("interesEstimado"), respuesta.integer("cantidadDias"));
				BigDecimal TEM = calcularTEM(TEA);
				parametros.set("TNA", TNA);
				parametros.set("TEA", TEA);
				parametros.set("TEM", TEM);
				parametros.set("PLAZO_CONSTITUCION_DIAS", respuesta.string("cantidadDias"));
				parametros.set("FECHA_VENCIMIENTO", respuesta.string("fechaVencimiento"));
				parametros.set("RENOVACION_AUTOMATICA", "N");
				parametros.set("GARANTIA_DEPOSITOS", respuesta.string("cubiertoPorGarantia"));
				parametros.set("TIPO_PLAZO_FIJO", respuesta.string("tipoOperacion"));
				parametros.set("FECHA_CONSTITUCION", respuesta.date("fechaActivacion", "yyyy-MM-dd", "dd/MM/yyyy"));
				parametros.set("RENOVAR_MONTO_MAS_INTERESES", respuesta.integer("renovaciones") > 0 && respuesta.string("renuevaIntereses").equals("S") ? "S" : "N");
				parametros.set("RENOVAR_MONTO", respuesta.integer("renovaciones") > 0 && !respuesta.string("renuevaIntereses").equals("S") ? "S" : "N");
				parametros.set("CANTIDAD_RENOVACIONES", respuesta.integer("renovaciones"));
				parametros.set("INTERES_COBRAR", Formateador.importe(respuesta.bigDecimal("interesEstimado")));
				if(plazoFijo.esUva()) {
					parametros.set("MONTO_ESTIMADO_VENCIMIENTO",  Formateador.importe(respuesta.bigDecimal("importe").add(respuesta.bigDecimal("interesEstimado"))));
					parametros.set("INTERESES_ESTIMADOS_COBRAR",  Formateador.importe(respuesta.bigDecimal("interesEstimado")));
					parametros.set("COTIZACION_UVA", respuesta.bigDecimal("valorIndice"));
					parametros.set("MONTO_INICIAL_UVA", respuesta.bigDecimal("montoUva"));
					MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_baja_renovacion_plazo_fijo_uva"), parametros);
				}
				else {
					parametros.set("MONTO_VENCIMIENTO", Formateador.importe(respuesta.bigDecimal("importe").add(respuesta.bigDecimal("interesEstimado"))));
					MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_baja_renovacion_plazo_fijo"), parametros);
				}
			}
			catch(Exception e) {
				
			}
		}


		
		plazoFijo.eliminarCachePlazosFijosWindowsGet();

		return RespuestaMB.exito();
	}

	public static RespuestaMB precancelarProcrearJoven(ContextoMB contexto) {
		ApiResponseMB response = PlazoFijoService.precancelarProcrearJoven(contexto);
		if (response.hayError()) {
			if ("143111".equals(response.string("codigo"))) {
				return RespuestaMB.estado("EXISTE_SOLICITUD");
			}
			return RespuestaMB.error();
		}
		return RespuestaMB.exito();
	}

	public static RespuestaMB tasaPreferencial(ContextoMB contexto) {

		Objeto tp = PlazoFijoService.tasaPreferencial(contexto);
		if (tp == null) return RespuestaMB.error();

		boolean ofertaCliente    = "S".equalsIgnoreCase(tp.string("OfertaCliente"));
		boolean ofertaPlataNueva = "S".equalsIgnoreCase(tp.string("OfertaPlataNueva"));
		String  montoVencStr     = tp.string("MontoVencimientoPFTradicional");
		boolean montoVencEsCero  = Objeto.anyEmpty(montoVencStr)
				|| "0".equals(montoVencStr) || "0.0".equals(montoVencStr) || "0.00".equals(montoVencStr);

		boolean renovacionesPendientes = ofertaPlataNueva && !montoVencEsCero;
		boolean esTPLegacy = ofertaCliente || (!ofertaCliente && ofertaPlataNueva && montoVencEsCero);

		// ===== NODO PLATA NUEVA =====
		Objeto nodoPN = new Objeto();
		nodoPN.set("habilitado", ofertaPlataNueva);
		nodoPN.set("renovacionesPendiente", renovacionesPendientes);
		nodoPN.set("tasaPreferencial", esTPLegacy);

		if (ofertaPlataNueva) {
			nodoPN.set("tasa", tp.bigDecimal("Tasa"));
			nodoPN.set("tasaFormateada", Formateador.importe(tp.bigDecimal("Tasa")));
			nodoPN.set("tasaEfectivaAnual", tp.bigDecimal("TEA"));
			nodoPN.set("tasaEfectivaAnualFormateada", Formateador.importe(tp.bigDecimal("TEA")));
			nodoPN.set("montoDesde", tp.bigDecimal("MontoDesde"));
			nodoPN.set("montoDesdeFormateado", Formateador.importe(tp.bigDecimal("MontoDesde")));
			nodoPN.set("montoHasta", tp.bigDecimal("MontoHasta"));
			nodoPN.set("montoHastaFormateado", Formateador.importe(tp.bigDecimal("MontoHasta")));
			nodoPN.set("plazo", tp.integer("PlazoMaximo"));

			Integer idMonedaPN = tp.integer("Moneda") != null ? tp.integer("Moneda") : 80;
			nodoPN.set("idMoneda", idMonedaPN);
			nodoPN.set("monedaSimbolo", Formateador.simboloMoneda(String.valueOf(idMonedaPN)));
			nodoPN.set("monedaDescripcion", Formateador.moneda(String.valueOf(idMonedaPN)));
			nodoPN.set("fechaVigencia", tp.date("FechaVigencia", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));

			if (renovacionesPendientes) {
				nodoPN.set("montoRenovacion", tp.bigDecimal("MontoVencimientoPFTradicional"));
				nodoPN.set("montoRenovacionFormateado", Formateador.importe(tp.bigDecimal("MontoVencimientoPFTradicional")));
			}
			nodoPN.set("tyc", tycTasaPreferencial(nodoPN));
		}

		// ===== PRICING SOLO SI NO HAY PN =====
		@SuppressWarnings("unchecked")
		List<Objeto> ofertaPricingRaw = (List<Objeto>) tp.get("OfertaPricing");
		if (ofertaPricingRaw == null) ofertaPricingRaw = Collections.emptyList();

		boolean pricingHabilitado = "S".equalsIgnoreCase(tp.string("Pricing"));
		boolean tienePlataNueva   = ofertaPlataNueva;
		boolean tienePricingData  = pricingHabilitado && !ofertaPricingRaw.isEmpty();

		// Respuesta base con PN
		RespuestaMB r = RespuestaMB.exito("plataNueva", nodoPN);

		if (!tienePlataNueva && tienePricingData) {
			// ===== PRICING (ofertas planas + agrupación por (moneda|codigoProducto)) =====
			List<Map<String, Object>> ofertas = new ArrayList<>();
			Map<String, Map<String, Object>> agregaPorGrupo = new LinkedHashMap<>();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			for (Objeto p : ofertaPricingRaw) {
				Integer idMoneda       = p.integer("moneda");
				String  codigoProducto = p.string("codigoProducto");
				String  simbolo        = Formateador.simboloMoneda(String.valueOf(idMoneda));
				String  descripcion    = Formateador.moneda(String.valueOf(idMoneda));

				// fila plana para el front
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("codigoProducto", codigoProducto);
				row.put("moneda", idMoneda);
				row.put("monedaSimbolo", simbolo);
				row.put("monedaDescripcion", descripcion);
				row.put("tasa", p.bigDecimal("tasa"));
				row.put("tasaFormateada", Formateador.importe(p.bigDecimal("tasa")));
				row.put("fechaInicio", p.string("fechaInicio"));
				row.put("fechaFin", p.string("fechaFin"));
				row.put("plazoMinimo", p.integer("plazoMinimo"));
				row.put("plazoMaximo", p.integer("plazoMaximo"));
				row.put("plazoMinimoFormateado", (p.integer("plazoMinimo") != null ? p.integer("plazoMinimo") : 0) + " días");
				row.put("plazoMaximoFormateado", (p.integer("plazoMaximo") != null ? p.integer("plazoMaximo") : 0) + " días");
				row.put("montoMinimo", p.bigDecimal("montoMinimo"));
				row.put("montoMaximo", p.bigDecimal("montoMaximo"));
				row.put("montoMinimoFormateado", p.bigDecimal("montoMinimo") != null ? Formateador.importe(p.bigDecimal("montoMinimo")) : null);
				row.put("montoMaximoFormateado", p.bigDecimal("montoMaximo") != null ? Formateador.importe(p.bigDecimal("montoMaximo")) : null);
				row.put("rangoMontoFormateado",
						(p.bigDecimal("montoMinimo") != null ? Formateador.importe(p.bigDecimal("montoMinimo")) : "-")
								+ " - " +
								(p.bigDecimal("montoMaximo") != null ? Formateador.importe(p.bigDecimal("montoMaximo")) : "-"));
				row.put("rangoPlazoFormateado",
						(p.integer("plazoMinimo") != null ? p.integer("plazoMinimo") : 0)
								+ " - " +
								(p.integer("plazoMaximo") != null ? p.integer("plazoMaximo") : 0) + " días");
				row.put("campania", p.string("campania"));
				row.put("mensaje", p.string("mensaje"));
				ofertas.add(row);

				// agregación por (moneda|codigoProducto) para T&C
				String key = (idMoneda != null ? idMoneda : -1) + "|" + (codigoProducto != null ? codigoProducto : "");
				Map<String, Object> g = agregaPorGrupo.get(key);

				Date fi = null, ff = null;
				try { fi = sdf.parse(p.string("fechaInicio")); } catch (Exception ignored) {}
				try { ff = sdf.parse(p.string("fechaFin")); }    catch (Exception ignored) {}

				BigDecimal montoMin = p.bigDecimal("montoMinimo");
				BigDecimal montoMax = p.bigDecimal("montoMaximo");
				Integer    plazoMin = p.integer("plazoMinimo");
				Integer    plazoMax = p.integer("plazoMaximo");

				if (g == null) {
					g = new LinkedHashMap<>();
					g.put("idMoneda", idMoneda);
					g.put("codigoProducto", codigoProducto);
					g.put("simbolo", simbolo);
					g.put("descripcion", descripcion);
					g.put("minMonto", montoMin);
					g.put("maxMonto", montoMax);
					g.put("minPlazo", plazoMin);
					g.put("maxPlazo", plazoMax);
					g.put("minFecha", fi);
					g.put("maxFecha", ff);
					agregaPorGrupo.put(key, g);
				} else {
					BigDecimal curMinMonto = (BigDecimal) g.get("minMonto");
					BigDecimal curMaxMonto = (BigDecimal) g.get("maxMonto");
					if (montoMin != null && (curMinMonto == null || montoMin.compareTo(curMinMonto) < 0)) g.put("minMonto", montoMin);
					if (montoMax != null && (curMaxMonto == null || montoMax.compareTo(curMaxMonto) > 0)) g.put("maxMonto", montoMax);

					Integer curMinPlazo = (Integer) g.get("minPlazo");
					Integer curMaxPlazo = (Integer) g.get("maxPlazo");
					if (plazoMin != null && (curMinPlazo == null || plazoMin < curMinPlazo)) g.put("minPlazo", plazoMin);
					if (plazoMax != null && (curMaxPlazo == null || plazoMax > curMaxPlazo)) g.put("maxPlazo", plazoMax);

					Date curMinF = (Date) g.get("minFecha");
					Date curMaxF = (Date) g.get("maxFecha");
					if (fi != null && (curMinF == null || fi.before(curMinF))) g.put("minFecha", fi);
					if (ff != null && (curMaxF == null || ff.after(curMaxF))) g.put("maxFecha", ff);
				}
			}

			// === tycItems (List<Map>) igual que HB; SIN tycTexto ===
			List<Map<String, Object>> tycItems = new ArrayList<>();
			for (Map<String, Object> g : agregaPorGrupo.values()) {
				Integer     idMoneda  = (Integer) g.get("idMoneda");
				String      codigo    = (String)  g.get("codigoProducto");
				String      desc      = (String)  g.get("descripcion");
				String      simbolo   = (String)  g.get("simbolo");
				BigDecimal  minMonto  = (BigDecimal) g.get("minMonto");
				BigDecimal  maxMonto  = (BigDecimal) g.get("maxMonto");
				Integer     minPlazo  = (Integer)   g.get("minPlazo");
				Integer     maxPlazo  = (Integer)   g.get("maxPlazo");

				Map<String, Object> item = new LinkedHashMap<>();
				item.put("codigoProducto", codigo);
				item.put("idMoneda", idMoneda);
				item.put("monedaDescripcion", desc);
				item.put("monedaSimbolo", simbolo);
				item.put("montoMinimoFormateado", minMonto != null ? Formateador.importe(minMonto) : null);
				item.put("montoMaximoFormateado", maxMonto != null ? Formateador.importe(maxMonto) : null);
				item.put("plazoMinimo", minPlazo);
				item.put("plazoMaximo", maxPlazo);
				tycItems.add(item);
			}

			Objeto nodoPricing = new Objeto();
			nodoPricing.set("habilitado", true); // solo llegamos acá si está habilitado y hay data
			nodoPricing.set("ofertas", ofertas);
			nodoPricing.set("tycItems", tycItems);

			// IMPORTANTE: no enviamos tycTexto
			r.set("pricing", nodoPricing);
		}

		return r;
	}



	private static List<Objeto> buildPricingTycItems(List<Map<String, Object>> grupos, SimpleDateFormat sdf) {
		List<Objeto> items = new ArrayList<>();
		for (Map<String, Object> g : grupos) {
			Objeto t = new Objeto();
			t.set("idMoneda", (Integer) g.get("moneda"));
			t.set("monedaDescripcion", (String) g.get("descripcion"));
			t.set("monedaSimbolo", (String) g.get("simbolo"));
			t.set("montoMinimoFormateado", Formateador.importe((BigDecimal) g.get("minMonto")));
			t.set("montoMaximoFormateado", Formateador.importe((BigDecimal) g.get("maxMonto")));
			t.set("plazoMinimo", g.get("minPlazo"));
			t.set("plazoMaximo", g.get("maxPlazo"));
			Date minF = (Date) g.get("minFecha");
			Date maxF = (Date) g.get("maxFecha");
			String vig = (minF != null && maxF != null) ? "del " + sdf.format(minF) + " al " + sdf.format(maxF) : "";
			t.set("vigenciaTexto", vig);
			items.add(t);
		}
		return items;
	}

	private static String buildPricingTycTexto(List<Objeto> tycItems) {
		StringBuilder out = new StringBuilder();
		if (tycItems == null || tycItems.isEmpty()) return "";

		if (tycItems.size() == 1) {
			Objeto t = tycItems.get(0);
			out.append("• Montos: Desde ").append(t.string("monedaSimbolo")).append(" ").append(t.string("montoMinimoFormateado"))
					.append(" y hasta ").append(t.string("monedaSimbolo")).append(" ").append(t.string("montoMaximoFormateado")).append('\n')
					.append("• Plazos: Desde ").append(t.integer("plazoMinimo")).append(" y ").append(t.integer("plazoMaximo")).append(" días");
			if (!t.string("vigenciaTexto").isEmpty()) {
				out.append('\n').append("• Vigencia: ").append(t.string("vigenciaTexto"));
			}
			return out.toString();
		}

		for (int i = 0; i < tycItems.size(); i++) {
			Objeto t = tycItems.get(i);
			out.append("Plazo Fijo en ").append(t.string("monedaDescripcion")).append('\n')
					.append("• Montos: Desde ").append(t.string("monedaSimbolo")).append(" ").append(t.string("montoMinimoFormateado"))
					.append(" y hasta ").append(t.string("monedaSimbolo")).append(" ").append(t.string("montoMaximoFormateado")).append('\n')
					.append("• Plazos: Desde ").append(t.integer("plazoMinimo")).append(" y ").append(t.integer("plazoMaximo")).append(" días");
			if (!t.string("vigenciaTexto").isEmpty()) {
				out.append('\n').append("• Vigencia: ").append(t.string("vigenciaTexto"));
			}
			if (i < tycItems.size() - 1) out.append("\n\n");
		}
		return out.toString();
	}



	private static boolean esTasaPreferencial(ContextoMB contexto, Integer idMoneda, BigDecimal monto, Integer plazo) {
		RespuestaMB respuesta = tasaPreferencial(contexto);
		Objeto plataNueva = (Objeto) respuesta.get("plataNueva");

		if((Boolean) plataNueva.get("tasaPreferencial")) {
			BigDecimal montoDesde = plataNueva.bigDecimal("montoDesde");
			BigDecimal montoHasta = plataNueva.bigDecimal("montoHasta");
			Integer plazoPreferencial = plataNueva.integer("plazo");
			Integer idMonedaPreferencial = plataNueva.integer("idMoneda");
			
			if(( (monto.compareTo(montoDesde) == 0 || monto.compareTo(montoDesde) > 0) 
					&& (monto.compareTo(montoHasta) == 0 || monto.compareTo(montoHasta) < 0)) 
					&& plazo <= plazoPreferencial && idMoneda == idMonedaPreferencial) {
				return true;
			}
			
		}
		return false;
	}
	
	/* ========== METODOS PRIVADOS ========== */
	private static Objeto montoPlazoValido(ContextoMB contexto, String codigo, Integer plazo, BigDecimal monto) {
		List<ApiResponseMB> listResponse = PlazoFijoService.tasas(contexto);
		for (ApiResponseMB response : listResponse) {
			if (response.hayError()) {
				return new Objeto().set("error", true);
			}
		}
		for (ApiResponseMB response : listResponse) {
			for (Objeto item : response.objetos("tasas")) {
				if (codigo.split("_")[0].equals(item.string("idTipoDeposito"))) {
					if (plazo >= item.integer("plazoMinimo") && plazo <= item.integer("plazoMaximo")) {
//						if (monto >= item.bigDecimal("montoMinimo").intValue() && monto <= item.bigDecimal("montoMaximo").intValue()) {
						if (monto.compareTo(item.bigDecimal("montoMinimo")) >= 0 && monto.compareTo(item.bigDecimal("montoMaximo")) <= 0) {
							return new Objeto().set("valido", true).set("moneda", codigo.split("_")[1]);
						}
					}
				}
			}
		}
		return new Objeto().set("valido", false);
	}

	private static String tycTasaPreferencial(Objeto objTasaPref) {
		String tyc = ConfigMB.string("tyc_tasa_preferencial");
		try {
			tyc = tyc.replace("$FECHA_VIGENCIA", objTasaPref.string("fechaVigencia"));
			tyc = tyc.replace("$PLAZO", objTasaPref.string("plazo"));
			tyc = tyc.replace("$TASA_FORMATEADA", objTasaPref.string("tasaFormateada"));
			tyc = tyc.replace("$TASA_EFECTIVA_ANUAL_FORMATEADA", objTasaPref.string("tasaEfectivaAnualFormateada"));
			tyc = tyc.replace("$MONTO_DESDE_FORMATEADO", objTasaPref.string("montoDesdeFormateado"));
			tyc = tyc.replace("$MONTO_HASTA_FORMATEADO", objTasaPref.string("montoHastaFormateado"));
		} catch (Exception e) {
			//
		}
		return tyc;
	}

	public static RespuestaMB simularPlazosFijoPorTipo(ContextoMB contexto) {
		Integer plazo = contexto.parametros.integer("plazo");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String idCuenta = contexto.parametros.string("idCuenta");
		String idMoneda = contexto.parametros.string("idMoneda");
		boolean buscarUva = contexto.parametros.bool("buscarUva", true);
		if (Objeto.anyEmpty(idCuenta, plazo, monto, idMoneda)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		RespuestaMB tiposPlazoFijo = tiposPlazosFijos(contexto);
		List<Objeto> listadoTiposPlazoFijo = tiposPlazoFijo.objetos("tiposPlazosFijos").stream().filter(item -> item.string("idMoneda").equals(idMoneda)).collect(Collectors.toList());
		listadoTiposPlazoFijo = listadoTiposPlazoFijo.stream().filter(dato -> buscarUva ? dato.string("id").contains("_uva") : !dato.string("id").contains("_uva")).collect(Collectors.toList());
		
		if(buscarUva && (monto.compareTo(new BigDecimal("1000")) < 0 || plazo < 90)) {
			listadoTiposPlazoFijo = listadoTiposPlazoFijo.stream().filter(dato -> !dato.string("id").contains("precancelable")).collect(Collectors.toList());
		}
		
		List<Objeto> listadoSimulaciones = new ArrayList<>();
		for (Objeto tipoPlazoFijo : listadoTiposPlazoFijo) {
			contexto.parametros.set("idTipoPlazoFijo", tipoPlazoFijo.string("id"));
			if (!buscarUva) {
				String[] descPF = tipoPlazoFijo.string("id").split("_");
				Boolean esUva = !"noesuva".contains(descPF[2]);
				if (esUva) {
					continue;
				}
			}

			RespuestaMB respuestaSimulacion = simularPlazoFijo(contexto);

			if (!respuestaSimulacion.hayError().booleanValue()) {
				Objeto simulacion = respuestaSimulacion.objetos("simulacion").get(0);
				datosTipoPlazoFijo(simulacion, tipoPlazoFijo);
				listadoSimulaciones.add(simulacion);
			}

		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("simulaciones", listadoSimulaciones);

		return respuesta;
	}

	private static void datosTipoPlazoFijo(Objeto simulacion, Objeto tipoPlazoFijo) {
		simulacion.set("idTipoPlazoFijo", tipoPlazoFijo.string("id"));
		simulacion.set("descripcionTipoPlazoFijo", tipoPlazoFijo.string("descripcion"));

	}

	public static boolean esClienteTasaPreferencial(String idCobs, boolean activo) {
		try {
			int activoParam = activo ? 1 : 0;

			SqlRequestMB sqlRequest = SqlMB.request("SelectClienteTasaPreferencial", "homebanking");
			sqlRequest.sql = "SELECT Activo " +
					"FROM [homebanking].[dbo].[clientesTasaPreferencialPF] " +
					"WHERE idCobis = ? AND Activo = ?";

			sqlRequest.parametros.add(idCobs);
			sqlRequest.parametros.add(activoParam);

			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public class PlazoFijoConfig {

		public static final BigDecimal MIN_AMOUNT_TRADITIONAL = new BigDecimal("1000.00");
		public static final BigDecimal MIN_AMOUNT_TRADITIONAL_IN_DOLLARS = new BigDecimal("1000.00");
		public static final BigDecimal MIN_AMOUNT_UVA = new BigDecimal("1000.00");
		public static final BigDecimal MAX_AMOUNT_UVA = new BigDecimal("5000000.00");
		public static final int MIN_DAYS_TRADITIONAL = 30;
		public static final int MIN_DAYS_UVA = 90;
		public static final int MAX_DAYS_UVA = 729;
		public static final String DEFAULT_AMOUNT = "1.000,00";
		public static final int DEFAULT_DATE_TRADITIONAL = 36;
	}
	
	//calcularTEA y calcularTEM apartir de los valores recibidos de api plazosfijos
	private static Integer cantidadDiasPreCan(String inicio, String fin) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		LocalDate fechaInicio = LocalDate.parse(inicio, formatter);
		LocalDate fechaFin = LocalDate.parse(fin, formatter);

		long diasEntre = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
		
		return (int) diasEntre;
	}

	
	private static BigDecimal calcularTEA(BigDecimal capital, BigDecimal interes, int plazoDias) {
        BigDecimal uno = BigDecimal.ONE;
        MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
        BigDecimal ratio = interes.divide(capital, mc).add(uno);
        BigDecimal exponente = BigDecimal.valueOf(365.0).divide(BigDecimal.valueOf(plazoDias), mc);
        double pow = Math.pow(ratio.doubleValue(), exponente.doubleValue());
        BigDecimal tea = BigDecimal.valueOf(pow).subtract(uno).multiply(BigDecimal.valueOf(100));
        return tea.setScale(2, RoundingMode.HALF_UP);
    
    }
    
	private static BigDecimal calcularTEM(BigDecimal teaPorcentaje) {
	    MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
	    BigDecimal uno = BigDecimal.ONE;
	    BigDecimal teaDecimal = teaPorcentaje.divide(BigDecimal.valueOf(100), mc);
	    double pow = Math.pow(teaDecimal.add(uno).doubleValue(), 1.0 / 12.0);
	    BigDecimal temDecimal = BigDecimal.valueOf(pow).subtract(uno);
	    return temDecimal.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
	}

}
