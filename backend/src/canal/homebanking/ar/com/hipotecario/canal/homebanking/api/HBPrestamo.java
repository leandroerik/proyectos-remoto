package ar.com.hipotecario.canal.homebanking.api;

import static ar.com.hipotecario.canal.homebanking.api.HBProducto.getProductosEnMora;
import static ar.com.hipotecario.canal.homebanking.api.HBProducto.getProductosEnMoraDetalles;
import static ar.com.hipotecario.canal.homebanking.api.HBProducto.verificarEstadoMoraTemprana;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.productos.PrestamosV4;
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
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Momento;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuotaPrestamo;
import ar.com.hipotecario.canal.homebanking.negocio.Prestamo;
import ar.com.hipotecario.canal.homebanking.negocio.ProductoMora;
import ar.com.hipotecario.canal.homebanking.negocio.ProductoMoraDetalles;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.homebanking.ventas.ApiVentaException;
import ar.com.hipotecario.canal.homebanking.ventas.ResolucionMotor;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud.DesembolsoPropuesto;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud.SolicitudProducto;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudPrestamo;

public class HBPrestamo {

	public static final String ESTADO_CUOTA_NORMAL = "Normal";
	public static final String ESTADO_CUOTA_CANCELADO = "Cancelada";

	public static Respuesta consolidada(ContextoHB contexto) {
		Boolean buscarBaseNegativa = contexto.parametros.bool("buscarBaseNegativa", false);
		Boolean buscarPosiblidadPrestamoTasaCero = contexto.parametros.bool("buscarPosiblidadPrestamoTasaCero", false);
//		Boolean buscarBeneficiarioArgentinaConstruye = contexto.parametros.bool("buscarBeneficiarioArgentinaConstruye", false);
//		Boolean buscarBeneficiarioProcrearRefaccion = contexto.parametros.bool("buscarBeneficiarioProcrearRefaccion", false);

		ApiRequest requestBaseNegativa = Api.request("BasesNegativas", "personas", "GET", "/basesNegativas/personas", contexto);
		requestBaseNegativa.query("idSolicitante", contexto.idCobis());
		requestBaseNegativa.query("tipoDocumento", contexto.persona().idTipoDocumentoString());
		requestBaseNegativa.query("nroDocumento", contexto.persona().numeroDocumento());
		requestBaseNegativa.query("sexo", contexto.persona().idSexo());
		requestBaseNegativa.query("idTributario", contexto.persona().tipoTributario());
		requestBaseNegativa.query("nroTributario", contexto.persona().cuit());

		// llamadas a servicios en paralelo
		Futuro<Boolean> futuroMostrarBoton = new Futuro<>(() -> contexto.persona().esEmpleado() ? false : ConfigHB.bool("prendido_alta_prestamos") && !RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false));
		Futuro<Boolean> futuroMostrarBotonAdelanto = new Futuro<>(() -> HBPersona.tieneOpcionAdelanto(contexto));
		Futuro<Respuesta> futuroOfertaPreaprobada = new Futuro<>(() -> HBConsolidado.ofertaPreAprobada(contexto));
		Futuro<Boolean> futuroPreaprobadoAdelanto = new Futuro<>(() -> futuroOfertaPreaprobada.get().existe("adelantoBH"));
		Futuro<Boolean> futuroTienePlanSueldo = new Futuro<>(() -> contexto.esJubilado() && contexto.tieneCuentaCategoriaB() && futuroPreaprobadoAdelanto.get() || contexto.esPlanSueldo());
		Futuro<ApiResponse> futuroResponseBaseNegativa = new Futuro<>(() -> Api.response(requestBaseNegativa, contexto.idCobis()));
		Futuro<Objeto> futuroSolicitudPendiente = new Futuro<>(() -> solicitudPendiente(contexto));

		Respuesta respuesta = new Respuesta();
		Boolean mostrarBoton = futuroMostrarBoton.get();
		Boolean mostrarBotonAdelanto = futuroMostrarBotonAdelanto.get();
		Respuesta ofertaPreAprobada = futuroOfertaPreaprobada.get();
		Boolean preaprobadoAdelanto = futuroPreaprobadoAdelanto.get();
		Boolean tienePlanSueldo = futuroTienePlanSueldo.get();

		respuesta.set("tienePlanSueldo", tienePlanSueldo);

		if (ConfigHB.bool("prendido_remanente_adelanto")) {
			respuesta.set("totalPreaprobadoAdelanto", tienePlanSueldo && ofertaPreAprobada.existe("adelantoBH") ? ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado") : null);
			respuesta.set("totalPreaprobadoAdelantoFormateado", tienePlanSueldo && ofertaPreAprobada.existe("adelantoBH") ? Formateador.importe(ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado")) : null);
		}
		if (contexto.esJubilado()) {
			respuesta.set("jubiladoAptoParaAdelanto", preaprobadoAdelanto && contexto.tieneCuentaCategoriaB());
		}

		List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
		List<ProductoMoraDetalles> productosEnMoraDetalles = getProductoMoraDetalles(contexto, productosEnMora);

		for (Prestamo prestamo : contexto.prestamos()) {
			if (prestamo.detalle().hayError()) {
				return Respuesta.error();
			}

			List<CuotaPrestamo> cuotas = prestamo.cuotas();

			Objeto item = new Objeto();
			item.set("id", prestamo.id());
			item.set("idTipoProducto", prestamo.idTipo());
			item.set("descripcion", descripcionPrestamo(prestamo, contexto.esJubilado()));
			item.set("idMoneda", prestamo.idMoneda());
			item.set("estado", prestamo.estado());
			item.set("simboloMoneda", prestamo.simboloMoneda());
			item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
			item.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
			item.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
			item.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
			item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas());
			item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
			item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
			item.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
			item.set("pagable", prestamo.codigo().equals("PPADELANTO"));
			item.set("enConstruccion", prestamo.enConstruccion());
			item.set("codigo", prestamo.codigo());
			item.set("urlDecreto767", ConfigHB.string("url_decreto_767", ""));
			item.set("mostrarLinkDecreto767", false);
			item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
			item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
			item.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
			item.set("numeroProducto", prestamo.numero());
			item.set("tipoPrestamo", prestamo.tipo());
			item.set("ultimos4digitosCuenta", prestamo.ultimos4digitos());

			verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item);
			Integer cantidadCuotasPagadas = cuotas.stream().filter(cuota -> ESTADO_CUOTA_CANCELADO.equals(cuota.estado())).collect(Collectors.toList()).size();

			try {
				item.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? cantidadCuotasPagadas : 0);
			} catch (Exception e) {
				item.set("cantidadCuotasPagadas", 0);
			}

			if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
				try {
					obtenerProximaCuota(contexto, prestamo, item);
				} catch (Exception e) {
				}
			}

			boolean ocultarMontoAdeudado = esProcrear(contexto, prestamo.codigo());

			item.set("ocultarMontoAdeudado", ocultarMontoAdeudado);

			if (!ConfigHB.string("uvas_decreto_767", "").equals("") && prestamo.idMoneda().equals("88")) {
				Set<String> habilitadoMostrar = Objeto.setOf(ConfigHB.string("uvas_decreto_767").split("_"));
				item.set("mostrarLinkDecreto767", habilitadoMostrar.contains(prestamo.codigo()));
			}

			if (!prestamo.categoria().equals("HIPOTECARIO")) {
				if (prestamo.esProcrear() && prestamo.codigo().equals("PPPROMATE2")) {
					item.set("pagable", false);
					respuesta.add("procrearMateriales", item);
				} else if (prestamo.esProcrear() && prestamo.codigo().equals("PROCREFAC1") || prestamo.esProcrear() && prestamo.codigo().equals("PROCREFAC2") || prestamo.esProcrear() && prestamo.codigo().equals("PROREFHOG1") || prestamo.esProcrear() && prestamo.codigo().equals("PROREFHOG2")) {
					respuesta.add("procrearRefaccionPrestamos", item);
				} else {
					respuesta.add("personales", item);
				}
			}
			if (prestamo.categoria().equals("HIPOTECARIO")) {
				respuesta.add("hipotecarios", item);
			}
		}
		remanenteOfertaPreAprobada(contexto, respuesta, ofertaPreAprobada);

		if (contexto.tienePrestamosNsp()) {
			ApiRequest request = Api.request("PrestamosNSP", "prestamos", "GET", "/v1/prestamos", contexto);
			request.query("buscansp", "true");
			request.query("estado", "true");
			request.query("idcliente", contexto.idCobis());

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError()) {
				return Respuesta.error();
			}

			for (Objeto item : response.objetos()) {
				if (item.string("tipoProducto").equals("NSP")) {
					Objeto nsp = new Objeto();
					nsp.set("id", item.string("idProducto"));
					nsp.set("numero", item.string("numeroProducto"));
					nsp.set("idEstado", item.string("estado"));
					nsp.set("estado", item.string("descEstado"));
					nsp.set("fechaAlta", item.date("fechaAlta", "yyyy-MM-dd", "dd/MM/yyyy"));
					nsp.set("idTitularidad", item.string("tipoTitularidad"));
					nsp.set("titularidad", item.string("descTipoTitularidad"));
					respuesta.add("nsp", nsp);
				}
			}
		}

		Objeto baseNegativa = new Objeto();
		baseNegativa.set("COBUVACVS", false);

		if (buscarBaseNegativa) {
			try {
				ApiResponse responseBaseNegativa = futuroResponseBaseNegativa.get();
				if (!responseBaseNegativa.hayError()) {
					for (Objeto item : responseBaseNegativa.objetos()) {
						if (item.string("referencia").equals("COBUVACVS")) {
							baseNegativa.set("COBUVACVS", true);
						}
					}
				}
			} catch (Exception e) {
			}
			respuesta.set("baseNegativa", baseNegativa);
		}

//		if (buscarBeneficiarioArgentinaConstruye) {
//			LocalTime localTime = LocalTime.now();
//			Boolean enHorario = (localTime.isAfter(LocalTime.parse("06:00:00")) && localTime.isBefore(LocalTime.parse("21:00:00")));
//
//			Boolean permitirSolicitar = HBArgentinaConstruye.ofertaArgentinaConstruye(contexto).string("estado").equals("SO");
//			respuesta.set("argentinaConstruye", new Objeto().set("permitirSolicitar", permitirSolicitar).set("enHorario", enHorario));
//		}
		respuesta.set("argentinaConstruye", false);

//		if (buscarBeneficiarioProcrearRefaccion) {
//			LocalTime localTime = LocalTime.now();
//			Boolean enHorario = (localTime.isAfter(LocalTime.parse("06:00:00")) && localTime.isBefore(LocalTime.parse("21:00:00")));
//
//			Boolean permitirSolicitar = HBProcrearRefaccion.valorOferta(contexto, "PROCREFAC1") != null;
//			respuesta.set("procrearRefaccion", new Objeto().set("permitirSolicitar", permitirSolicitar).set("enHorario", enHorario));
//		}
		respuesta.set("procrearRefaccion", false);

		if (buscarPosiblidadPrestamoTasaCero) {

			TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
			Objeto ama = respuesta.set("prestamoAMA");

			ama.set("permitirSolicitarPrestamoTasaCero", false);
			ama.set("permitirSolicitarTarjetaTasaCero", false);
			ama.set("mostrarMensaje", false);

			String mensajeTasaCero = ConfigHB.string("mensaje_prestamo_tasa_cero_alta_tarjeta");

			if (mensajeTasaCero == null)
				mensajeTasaCero = "";
			// NOMBRE_PERSONA
			mensajeTasaCero = mensajeTasaCero.replace("NOMBRE_PERSONA", contexto.persona().nombre());
			ama.set("mensajePrestamoTasaCeroAltaTarjeta", mensajeTasaCero);

			ama.set("solicitudExistente", existePrestamoTasaCero(contexto));
			String cuit = contexto.persona().cuit();
			try {
				if (!ConfigHB.esProduccion() && !contexto.requestHeader("cuit").isEmpty()) {
					cuit = contexto.requestHeader("cuit");
				}
			} catch (Exception e) {
			}

			ApiRequest request = Api.request("BeneficiarioPrestamoTasaCero", "prestamos", "GET", "/v1/prestamos/creditos/{cuil}", contexto);
			request.path("cuil", cuit);
			request.cacheSesion = true;

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (!response.hayError()) {

				Date fechaTope = null;
				boolean prender = false;
				try {
					fechaTope = new SimpleDateFormat("yyyyMMdd").parse(ConfigHB.string("tasa_cero_fecha_tope", "20220826"));
					prender = !ConfigHB.esProduccion() || response.date("fechaSolicitud", "yyyy-MM-dd'T'hh:mm:ss").after(fechaTope);
				} catch (Exception e) {
				}

				if (!prender) {
					ama.set("permitirSolicitarPrestamoTasaCero", false);
					ama.set("permitirSolicitarTarjetaTasaCero", false);
					ama.set("mostrarMensaje", false);
				} else {
					if (tarjetaCredito != null) {
						// caso 1: está en el banco central + tiene TC titular -> solicita prestamo
						ama.set("permitirSolicitarPrestamoTasaCero", true);
						ama.set("permitirSolicitarTarjetaTasaCero", false);
						ama.set("mostrarMensaje", false);
					} else {
						Boolean tieneAdicionalesTercero = !contexto.tarjetasCreditoAdicionalesTercero().isEmpty();
						if (tieneAdicionalesTercero) {
							// caso 2: está en el banco central + no tiene TC titular pero si una adicional
							// -> muestra mensaje a definir
							ama.set("permitirSolicitarPrestamoTasaCero", false);
							ama.set("permitirSolicitarTarjetaTasaCero", false);
							ama.set("mostrarMensaje", true);
						} else {
							// caso 3: está en el banco central + no tiene TC -> solicita tarjeta
							ama.set("permitirSolicitarPrestamoTasaCero", false);
							ama.set("permitirSolicitarTarjetaTasaCero", true);
							ama.set("mostrarMensaje", false);
						}
					}
				}
			}
		}

//		cargaSolicitudesPendientes(contexto, respuesta);
		respuesta.set("desembolsoPendiente", futuroSolicitudPendiente.get());
		respuesta.set("mostrarBotonSolicitudPrestamoPersonal", mostrarBoton && !respuesta.existe("desembolsoPendiente"));
		respuesta.set("mostrarBotonSolicitudAdelanto", mostrarBotonAdelanto && !respuesta.existe("desembolsoPendiente"));

		return respuesta;
	}

	protected static void cargaSolicitudesPendientes(ContextoHB contexto, Respuesta respuesta) {
		Objeto solicitudes = HBOmnicanalidad.detalleSolicitudesPrestamos(contexto);

		if (solicitudes != null && solicitudes.objetos().size() > 0) {
			for (Objeto solicitud : solicitudes.objetos()) {
				if (solicitud.existe("id")) {
					if (solicitud.bool("desembolsoOnline") && ConfigHB.bool("prendido_desembolso_online")) {
						if (solicitud.integer("horasRestantes") > 0) {
							respuesta.set("desembolsoPendiente", solicitud);
						} else {
							RestOmnicanalidad.desistirSolicitud(contexto, solicitud.string("id"));
						}
					}
				}
			}
		}
	}

	private static Objeto solicitudPendiente(ContextoHB contexto) {
		Objeto solicitudes = HBOmnicanalidad.detalleSolicitudesPrestamos(contexto);

		Objeto solicitudPendiente = null;
		if (solicitudes != null && solicitudes.objetos().size() > 0) {
			for (Objeto solicitud : solicitudes.objetos()) {
				if (solicitud.existe("id")) {
					if (solicitud.bool("desembolsoOnline") && ConfigHB.bool("prendido_desembolso_online")) {
						if (solicitud.integer("horasRestantes") > 0) {
							solicitudPendiente = solicitud;
						} else {
							RestOmnicanalidad.desistirSolicitud(contexto, solicitud.string("id"));
						}
					}
				}
			}
		}
		return solicitudPendiente;
	}

	public static void obtenerProximaCuota(ContextoHB contexto, Prestamo prestamo, Objeto item) {
		List<CuotaPrestamo> cuotas = contexto.prestamo(prestamo.id()).cuotas();
		CuotaPrestamo prestamoNormal = null;

		if (!cuotas.isEmpty()) {
			Optional<CuotaPrestamo> result = cuotas.stream().filter(cuota -> cuota.estado().equals(ESTADO_CUOTA_NORMAL)).findFirst();
			if (result.isPresent()) {
				prestamoNormal = (CuotaPrestamo) result.get();
			}
		}

		if (Objects.nonNull(prestamoNormal)) {
			item.set("proximaCuota", prestamoNormal.importeCuota());
		}

		Integer diasVencimientoCuota = Momento.diferenciaEntreFechas(prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
		if (diasVencimientoCuota >= 0 && diasVencimientoCuota <= 3) {
			item.set("diasVencimientoCuota", diasVencimientoCuota);
		}
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

	public static Respuesta detalle(ContextoHB contexto) {
		String idPrestamo = contexto.parametros.string("idPrestamo");
		Boolean buscarCuotas = contexto.parametros.bool("buscarCuotas", false);
		Boolean buscarDatosCancelacionTotal = contexto.parametros.bool("buscarDatosCancelacionTotal", false);
		Boolean buscarSegundoDesembolso = contexto.parametros.bool("buscarSegundoDesembolso", false);
		Boolean buscarDatosAnticipo = contexto.parametros.bool("buscarDatosAnticipo", false);

		Prestamo prestamo = contexto.prestamo(idPrestamo);
		if (Objeto.anyEmpty(prestamo)) {
			return Respuesta.parametrosIncorrectos();
		}

		List<CuotaPrestamo> cuotas = prestamo.cuotas();

		boolean esProcrear = esProcrear(contexto, prestamo.codigo());

		List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
		List<ProductoMoraDetalles> productosEnMoraDetalles = getProductoMoraDetalles(contexto, productosEnMora);

		Respuesta respuesta = new Respuesta();

		Objeto detalle = new Objeto();
		detalle.set("id", prestamo.id());
		detalle.set("idTipoProducto", prestamo.idTipo());
		detalle.set("idMoneda", prestamo.idMoneda());
		detalle.set("numero", prestamo.numero());
		detalle.set("estado", prestamo.estado());
		detalle.set("fechaAlta", prestamo.fechaAlta("dd/MM/yyyy"));
		detalle.set("simboloMoneda", prestamo.simboloMoneda());
		detalle.set("esProcrear", esProcrear);
		detalle.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
		detalle.set("montoSolicitado", prestamo.montoAprobadoFormateado());

		detalle.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
		detalle.set("valorSaldoActual", prestamo.montoUltimaCuota());
		detalle.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
		detalle.set("formaPago", prestamo.descripcionFormaPago());
		detalle.set("cuenta", prestamo.cuentaPago() != null ? prestamo.cuentaPago().descripcionCorta() + " " + prestamo.cuentaPago().simboloMoneda() + " " + prestamo.cuentaPago().numeroEnmascarado() : "");
		detalle.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
		detalle.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
		detalle.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
		detalle.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
		detalle.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
		detalle.set("numeroProducto", prestamo.numero());

		verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, detalle);
		Integer cantidadCuotasPagadas = cuotas.stream().filter(cuota -> ESTADO_CUOTA_CANCELADO.equals(cuota.estado())).collect(Collectors.toList()).size();

		if(prestamo.idMoneda().equals("88")){
			try{
				ApiResponse responseUva = InversionesService.inversionesGetCotizaciones(contexto, "88");
				detalle.set("cotizacionUva", Formateador.importe(Objeto.fromJson(responseUva.json).objetos().get(0).bigDecimal("valorCotizacion")));
			}catch (Exception e){ }

		}

		try {
			detalle.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? cantidadCuotasPagadas : 0);
		} catch (Exception e) {
			detalle.set("cantidadCuotasPagadas", 0);
		}
		detalle.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
		detalle.set("cantidadCuotasVencidas", !prestamo.enConstruccion() ? prestamo.cuotasVencidas() : 0);
		detalle.set("tipoTasa", prestamo.descripcionTipoTasa());
		detalle.set("tasa", prestamo.tasaFormateada());
		detalle.set("pagable", prestamo.enConstruccion() ? false : prestamo.pagable());
		detalle.set("debitoAutomatico", prestamo.debitoAutomatico());
		detalle.set("categoria", prestamo.categoria());
		detalle.set("enConstruccion", prestamo.enConstruccion());
		detalle.set("esProcrear", prestamo.esProcrear());
		detalle.set("codigo", prestamo.codigo());
		detalle.set("descripcionTipoPrestamo", prestamo.descripcionTipoPrestamo().equalsIgnoreCase("PPADELANTO") ? "ADELANTO DE SUELDO" : prestamo.descripcionTipoPrestamo());
		if (buscarDatosCancelacionTotal) {
			ApiResponse response = RestPrestamo.simluarCancelacionTotal(contexto, prestamo.numero());
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			if (response.hayError()) {
				return Respuesta.error();
			}

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

			Integer montoMaximo = ConfigHB.integer("monto_maximo_cancelacion_total", 2000000);
			Boolean permitirCancelacionTotal = true; // monto.intValue() <= montoMaximo;
			item.set("permitirCancelacionTotal", permitirCancelacionTotal);
			item.set("mensajeSuperaMaximoCancelacionTotal", ConfigHB.string("mensaje_monto_maximo_cancelacion_total").replace("MONTO", Formateador.importe(new BigDecimal(montoMaximo))));

			detalle.set("cancelacionTotal", item);
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

		detalle.set("ocultarMontoAdeudado", esProcrear);

		if (buscarSegundoDesembolso) {
			Objeto item = new Objeto();
			item.set("permitirSegundoDesembolso", false);
			if ("HIPOTECARIO".equals(prestamo.categoria())) {
				if (true) {
					String codigoPrestamo = prestamo.codigo();
					if (codigoPrestamo.equalsIgnoreCase("PROCCONHOG") || codigoPrestamo.equalsIgnoreCase("PROLOCOHOG"))
						item.set("ocultarDesembolso", true);
					Solicitud solicitud = Solicitud.solicitudSegundoDesembolsoPrestamoHipotecario(contexto, contexto.persona().cuit(), "");
					if (solicitud != null) {
						int numeroDesembolso = 0;
						int nroDesembolsosPropuestos = 0;
						for (SolicitudProducto producto : solicitud.Productos) {
							if (producto.tipoProducto.equals("1") && producto.DesembolsosPropuestos != null) {
								nroDesembolsosPropuestos = producto.NroDesembolsosPropuestos == null ? 0 : producto.NroDesembolsosPropuestos;
								numeroDesembolso = producto.NroUltimoDesembolsoLiquidado == null ? 1 : producto.NroUltimoDesembolsoLiquidado;
							}
						}
						if (nroDesembolsosPropuestos > numeroDesembolso && numeroDesembolso + 1 > 1) {
							Solicitud solicitud2 = Solicitud.solicitudSegundoDesembolsoPrestamoHipotecario(contexto, contexto.persona().cuit(), Integer.valueOf(numeroDesembolso + 1).toString());
							if (solicitud2 != null) {
								for (SolicitudProducto producto : solicitud2.Productos) {
									if (producto.tipoProducto.equals("1") && producto.DesembolsosPropuestos != null) {
										item.set("nroDesembolsosPropuestos", producto.NroDesembolsosPropuestos);
										item.set("nroUltimoDesembolsoLiquidado", numeroDesembolso);
										for (DesembolsoPropuesto desembolso : producto.DesembolsosPropuestos) {
											if (desembolso.NroDesembolso != null) {
												if (desembolso.NroDesembolso.equals(numeroDesembolso + 1)) {
													item.set("permitirSegundoDesembolso", true);
													item.set("monto", desembolso.Monto);
													item.set("montoFormateado", Formateador.importe(desembolso.Monto));
													item.set("esHipotecario", true);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} else {
				Solicitud solicitud = Solicitud.solicitudSegundoDesembolsoProcrearRefaccion(contexto, contexto.persona().cuit());
				if (solicitud != null) {
					for (SolicitudProducto producto : solicitud.Productos) {
						if (producto.tipoProducto.equals("2") && producto.DesembolsosPropuestos != null) {
							for (DesembolsoPropuesto desembolso : producto.DesembolsosPropuestos) {
								if (desembolso.NroDesembolso.equals(2)) {
									item.set("permitirSegundoDesembolso", true);
									item.set("monto", desembolso.Monto);
									item.set("montoFormateado", Formateador.importe(desembolso.Monto));
								}
							}
						}
					}
				}
			}
			detalle.set("segundoDesembolso", item);
		}
		respuesta.set("detalle", detalle);

		if (buscarDatosAnticipo) {
			ApiResponse response = RestPrestamo.detalleAnticipo(contexto, prestamo.numero());
			if (response.hayError() || response.objetos() == null) {
				RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
				if (response.string("codigo").equals("770321")) {
					return Respuesta.estado("SUPERO_MAXIMO_ANTICIPOS");
				}

				return Respuesta.error();
			}

			BigDecimal montoMinAnticipo = response.objetos().get(0).bigDecimal("VALOR_MIN_ANTICIPO");
			BigDecimal montoMaxAnticipo;
			try {
				montoMaxAnticipo = response.objetos().get(0).bigDecimal("VALOR_MAX_ANTICIPO","1");
			}catch (Exception ex){
				montoMaxAnticipo = new BigDecimal(1);
			}
			BigDecimal cotizacion = response.objetos().get(0).bigDecimal("COTIZACION", "1");
			contexto.sesion.cotizacionAnticipoPrestamo = cotizacion;

			Objeto item = new Objeto();
			item.set("montoMinAnticipo", montoMinAnticipo);
			item.set("montoMinAnticipoPesos", montoMinAnticipo.multiply(cotizacion));
			item.set("montoMinAnticipoFormateado", Formateador.importe(montoMinAnticipo));
			item.set("montoMinAnticipoPesosFormateado", Formateador.importe(montoMinAnticipo.multiply(cotizacion)));
			item.set("montoMaxAnticipo", montoMaxAnticipo);
			item.set("montoMaxAnticipoPesos", montoMaxAnticipo.multiply(cotizacion));
			item.set("montoMaxAnticipoFormateado", Formateador.importe(montoMaxAnticipo));
			item.set("montoMaxAnticipoPesosFormateado", Formateador.importe(montoMaxAnticipo.multiply(cotizacion)));
			item.set("cotizacion", cotizacion);
			item.set("cotizacionFormateada", Formateador.importe(cotizacion));
			detalle.set("cancelacionAnticipada", item);
		}

		return respuesta;
	}

	private static List<ProductoMoraDetalles> getProductoMoraDetalles(ContextoHB contexto, List<ProductoMora> productosEnMora) {
		List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
		productosEnMora.forEach(productoMora -> {
			ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
			if (Objects.nonNull(item)) {
				productosEnMoraDetalles.add(item);
			}
		});
		return productosEnMoraDetalles;
	}

	public static boolean esProcrear(ContextoHB contexto, String nemonico) {
		boolean nemonicoPrestamo = false;
		SqlResponse sqlResponse;
		try {
			SqlRequest sqlRequest = Sql.request("Nemónicos", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[NemonicosProcrear] WHERE NemonicoProcrear = ?";
			sqlRequest.add(nemonico);
			sqlResponse = Sql.response(sqlRequest);
			nemonicoPrestamo = sqlResponse.registros.size() > 0;
		} catch (Exception e) {

		}
		return nemonicoPrestamo;
	}

	public static Respuesta pagar(ContextoHB contexto) {
		String idPrestamo = contexto.parametros.string("idPrestamo");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal importe = contexto.parametros.bigDecimal("importe");

		if (Objeto.anyEmpty(idPrestamo, idCuenta, importe)) {
			return Respuesta.parametrosIncorrectos();
		}

		Prestamo prestamo = contexto.prestamo(idPrestamo);
		if (prestamo == null) {
			return Respuesta.estado("PRESTAMO_NO_EXISTE");
		}
		if (prestamo.detalle().hayError()) {
			return Respuesta.error();
		}

		if ("88".equals(prestamo.idMoneda())) {
			return new Respuesta().setEstado("PRESTAMO_UVA");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		// ApiRequest request = Api.request("PagarPrestamo", "prestamos", "POST",
		// "/v2/prestamos/{id}/electronicos", contexto);
		ApiRequest request = Api.request("PagarPrestamo", "orquestados", "POST", "/v1/prestamos/{id}/electronicos", contexto);
		request.path("id", prestamo.numero());
		request.body("cuenta", cuenta.numero());
		request.body("idMoneda", prestamo.idMoneda());
		request.body("importe", importe);
		request.body("tipoCuenta", cuenta.idTipo());
		request.body("tipoPrestamo", prestamo.idTipo());
		request.body("reverso", "false");
		request.body("idProducto", prestamo.id()); // emm

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (403 == response.codigo) {
				return Respuesta.estado("FUERA_HORARIO");
			}
			return Respuesta.error();
		}
		String nroTicket = response.string("nroTicket");

		try {
			ProductosService.eliminarCacheProductos(contexto);
			prestamo.eliminarCacheDetalle();
			prestamo.eliminarCacheCuotas();
		} catch (Exception e) {
		}

		// En HB viejo lo hacía similar a esta forma
		String tipoPrestamo = "";
		if ("CCA".equals(prestamo.idTipo()))
			tipoPrestamo = "Personal";
		if ("NSP".equals(prestamo.idTipo()))
			tipoPrestamo = "Hipotecario";
		if ("PPN".equals(prestamo.idTipo()))
			tipoPrestamo = "Prendario";
		if ("MPR".equals(prestamo.idTipo()))
			tipoPrestamo = "Prendario";
		if ("MP".equals(prestamo.idTipo()))
			tipoPrestamo = "Personal";
		if ("PRE".equals(prestamo.idTipo()))
			tipoPrestamo = "Preventa";

		Date hoy = new Date();
		Map<String, String> comprobantePdf = new HashMap<>();
		comprobantePdf.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(hoy));
		comprobantePdf.put("ID", nroTicket);
		comprobantePdf.put("DESCRIPCION", "PRÉSTAMO " + tipoPrestamo.toUpperCase());
		comprobantePdf.put("IMPORTE", prestamo.simboloMoneda() + " " + Formateador.importe(importe));
		String tipoCuentaOrigenDescripcion = "AHO".equals(cuenta.idTipo()) ? "CA" : ("CTE".equals(cuenta.idTipo()) ? "CC" : "");
		tipoCuentaOrigenDescripcion = tipoCuentaOrigenDescripcion + " " + prestamo.simboloMoneda() + " " + cuenta.numero();
		comprobantePdf.put("CUENTA_ORIGEN", tipoCuentaOrigenDescripcion);
		comprobantePdf.put("NUMERO_PRESTAMO", prestamo.numero());

		String idComprobante = "prestamo" + "_" + nroTicket.trim();
		contexto.sesion.comprobantes.put(idComprobante, comprobantePdf);
		return Respuesta.exito("idComprobante", idComprobante);

	}

	public static Respuesta precancelacionTotal(ContextoHB contexto) {
		String idPrestamo = contexto.parametros.string("idPrestamo");
		String idCuenta = contexto.parametros.string("idCuenta");
		Boolean cancelacionAnticipada = contexto.parametros.bool("cancelacionAnticipada", false);
		BigDecimal montoAnticipo = contexto.parametros.bigDecimal("montoAnticipo", "0");
		Boolean aplicarPago = contexto.parametros.bool("aplicarPago", false);

		if (Objeto.anyEmpty(idPrestamo, idCuenta)) {
			return Respuesta.parametrosIncorrectos();
		}

		Prestamo prestamo = contexto.prestamo(idPrestamo);
		if (prestamo == null) {
			return Respuesta.estado("PRESTAMO_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		if (cancelacionAnticipada && montoAnticipo.equals(0)) {
			return Respuesta.estado("MONTO_ANTICIPO_INVALIDO");
		}

		ApiResponse simulacion = RestPrestamo.simluarCancelacionTotal(contexto, prestamo.numero());
		if (simulacion.hayError()) {
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			return Respuesta.error();
		}
		BigDecimal monto = simulacion.bigDecimal("MONTO_A_PAGAR");
		BigDecimal cotizacion = simulacion.bigDecimal("COTIZACION");

		Integer montoMaximo = ConfigHB.integer("monto_maximo_cancelacion_total", 2000000);
		Boolean permitirCancelacionTotal = true;// monto.intValue() <= montoMaximo;
		if (!permitirCancelacionTotal) {
			return Respuesta.estado("DERIVIAR_A_SUCURSAL").set("mensajeSuperaMaximoCancelacionTotal", ConfigHB.string("mensaje_monto_maximo_cancelacion_total").replace("MONTO", Formateador.importe(new BigDecimal(montoMaximo))));
		}

		if (cancelacionAnticipada && !aplicarPago){
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			return Respuesta.exito("montoNegociado", Formateador.importe(cotizacion == null ? monto : monto.multiply(cotizacion)));
		}

		ApiRequest request = Api.request("PrecancelacionTotalPrestamo", "prestamos", "POST", "/v1/prestamos/aplicacion", contexto);
		request.body("cuenta_cobro", cuenta.numero());
		request.body("forma_cobro", cuenta.esCajaAhorro() ? "NDMNCA" : cuenta.esCuentaCorriente() ? "NDMNCC" : null);
		request.body("num_operacion", prestamo.numero());
		if (!cancelacionAnticipada)
			request.body("tipo_cancelacion", "CANCTOTAL");
		else {
			request.body("tipo_cancelacion", "ANTCAPITAL");
			request.body("monto", monto);
		}

		ApiResponse response = null;
		try {
			response = Api.response(request);
		} catch (RuntimeException e) {
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			throw e;
		} finally {
			try {
				ProductosService.eliminarCacheProductos(contexto);
				prestamo.eliminarCacheDetalle();
			} catch (Exception e) {
			}
		}

		if (response.hayError()) {
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			if (response.string("codigo").equals("208003")) {
				return Respuesta.estado("SIN_SALDO");
			}
			if (response.string("codigo").equals("201249")) {
				return Respuesta.estado("VALORES_SUSPENSO");
			}
			if (response.string("codigo").equals("701326")) {
				return Respuesta.estado("SOLO_POR_SUCURSAL");
			}
			if (response.string("codigo").equals("714409")) {
				return Respuesta.estado("SOLO_POR_SUCURSAL");
			}
			return Respuesta.error();
		}

		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
		comprobante.put("ID", response.string("NRO_COMPROBANTE"));
		comprobante.put("NUMERO_PRESTAMO", prestamo.numero());
		comprobante.put("IMPORTE", prestamo.simboloMoneda() + " " + Formateador.importe(simulacion.bigDecimal("MONTO_A_PAGAR")));
		comprobante.put("CUENTA", cuenta.descripcionCorta() + " " + cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());

		comprobante.put("TITULAR", contexto.persona().nombreCompleto());
		comprobante.put("CUIT_CUIL", contexto.persona().cuit());
		if (cotizacion != null) {
			comprobante.put("VALOR_PESOS", "$ " + Formateador.importe(monto.multiply(cotizacion)));
			comprobante.put("DESCRIPCION_COTIZACION", "Cotizacion UVA");
			comprobante.put("COTIZACION", "$ " + Formateador.importe(cotizacion));
		} else {
			comprobante.put("VALOR_PESOS", "$ " + Formateador.importe(monto));
			comprobante.put("DESCRIPCION_COTIZACION", "");
			comprobante.put("COTIZACION", "");
		}

		String idComprobante = !cancelacionAnticipada ? "precancelacion-total-prestamo" + "_" + response.string("NRO_COMPROBANTE")
                                : "precancelacion-anticipada-prestamo" + "_" + response.string("NRO_COMPROBANTE");;
		contexto.sesion.comprobantes.put(idComprobante, comprobante);

		return Respuesta.exito("idComprobante", idComprobante);
	}

	public static Object ultimaLiquidacion(ContextoHB contexto) {
		String idPrestamo = contexto.parametros.string("idPrestamo");
		String cuota = contexto.parametros.string("cuota");

		if (Objeto.anyEmpty(idPrestamo, cuota)) {
			contexto.responseHeader("estado", "PARAMETROS_INCORRECTOS");
			return Respuesta.parametrosIncorrectos();
		}

		Prestamo prestamo = contexto.prestamo(idPrestamo);
		if (prestamo == null) {
			contexto.responseHeader("estado", "PRESTAMO_NO_EXISTE");
			return Respuesta.estado("PRESTAMO_NO_EXISTE");
		}

		ApiResponse response = RestPrestamo.ultimaLiquidacion(contexto, prestamo.numero(), cuota);
		if (response.hayError()) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.error();
		}
		if (response.codigo == 204) {
			contexto.responseHeader("estado", "SIN_RESUMEN");
			return Respuesta.estado("SIN_RESUMEN");
		}

		String base64 = response.string("pdf");
		byte[] archivo = Base64.getDecoder().decode(base64);
		try {
			archivo = Base64.getDecoder().decode(new String(archivo));
		} catch (Exception e) {
		}
		contexto.responseHeader("estado", "0");
		contexto.responseHeader("Content-Type", "application/pdf; name=Prestamo-" + cuota + ".pdf");
		return archivo;
	}

	public static Object ultimaLiquidacionNsp(ContextoHB contexto) {
		String numero = contexto.parametros.string("numero");

		if (Objeto.anyEmpty(numero)) {
			contexto.responseHeader("estado", "PARAMETROS_INCORRECTOS");
			return Respuesta.parametrosIncorrectos();
		}

		ApiResponse response = RestPrestamo.ultimaLiquidacionNsp(contexto, numero);
		if (response.hayError()) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.error();
		}
		if (response.codigo == 204) {
			contexto.responseHeader("estado", "SIN_RESUMEN");
			return Respuesta.estado("SIN_RESUMEN");
		}
		if (response.string("file").isEmpty()) {
			contexto.responseHeader("estado", "SIN_RESUMEN");
			return Respuesta.estado("SIN_RESUMEN");
		}

		String base64 = response.string("file");
		byte[] archivo = Base64.getDecoder().decode(base64);
		try {
			archivo = Base64.getDecoder().decode(new String(archivo));
		} catch (Exception e) {
		}
		contexto.responseHeader("estado", "0");
		contexto.responseHeader("Content-Type", "application/pdf; name=Prestamo-nsp.pdf");
		return archivo;
	}

	public static Respuesta consolidadaPrestamoTasaCero(ContextoHB contexto) {
		String cuit = contexto.persona().cuit();
		try {
			if (!ConfigHB.esProduccion() && !contexto.requestHeader("cuit").isEmpty()) {
				cuit = contexto.requestHeader("cuit");
			}
		} catch (Exception e) {
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

		Respuesta respuesta = new Respuesta();
		Objeto datos = respuesta.set("datos");

		ApiRequest request = Api.request("BeneficiarioPrestamoTasaCero", "prestamos", "GET", "/v1/prestamos/creditos/{cuil}", contexto);
		request.path("cuil", cuit);
		request.cacheSesion = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (!response.hayError()) {
			BigDecimal monto = response.bigDecimal("monto");/*
															 * if (response.bigDecimal("importePeriodo1") != null) { monto =
															 * monto.add(response.bigDecimal("importePeriodo1")); } if
															 * (response.bigDecimal("importePeriodo2") != null) { monto =
															 * monto.add(response.bigDecimal("importePeriodo2")); } if
															 * (response.bigDecimal("importePeriodo3") != null) { monto =
															 * monto.add(response.bigDecimal("importePeriodo3")); }
															 */
			BigDecimal cuota = monto != null ? monto.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP) : null;
			datos.set("montoFormateado", Formateador.importe(monto));
			datos.set("importeCuotaFormateada", Formateador.importe(cuota));
			datos.set("tarjetaCreditoFormateada", tarjetaCredito != null ? tarjetaCredito.tipo() + " XXXX-" + tarjetaCredito.ultimos4digitos() : "");
			datos.set("tnaFormateado", "0%");
			datos.set("plazoFormateado", "12");
			datos.set("tipoCredito", response.string("tipoCredito"));

			String mensajeTasaCeroConfirmacion = ConfigHB.string("mensaje_prestamo_tasa_cero_confirmacion");
			if (mensajeTasaCeroConfirmacion == null)
				mensajeTasaCeroConfirmacion = "";
			datos.set("mensajeTasaCeroConfirmacion", mensajeTasaCeroConfirmacion);
		} else {
//			BigDecimal monto = new BigDecimal("163500.00");
//			BigDecimal cuota = monto != null ? monto.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP) : null;
//			datos.set("montoFormateado", Formateador.importe(monto));
//			datos.set("importeCuotaFormateada", Formateador.importe(cuota));
//			datos.set("tarjetaCreditoFormateada", tarjetaCredito != null ? tarjetaCredito.tipo() + " XXXX-" + tarjetaCredito.ultimos4digitos() : "");
//			datos.set("tnaFormateado", "0%");
//			datos.set("plazoFormateado", "12");
			return Respuesta.error();
		}
		return respuesta;
	}

	public static Respuesta altaPrestamoTasaCero(ContextoHB contexto) {

		if (existePrestamoTasaCero(contexto)) {
			return Respuesta.estado("SOLICITUD_EXISTENTE");
		}

		// Tarjeta Credito
		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
		ApiResponse detalleTarjetaCredito = null;
		if (tarjetaCredito != null) {
			ApiRequest request = Api.request("DetalleTarjetaCredito", "tarjetascredito", "GET", "/v1/tarjetascredito/{idtarjeta}", contexto);
			request.path("idtarjeta", tarjetaCredito.numero());
			request.cacheSesion = true;

			detalleTarjetaCredito = Api.response(request, tarjetaCredito.numero());
			if (detalleTarjetaCredito.hayError()) {
				return Respuesta.error();
			}
		}
		Boolean tieneAdicionalesTercero = !contexto.tarjetasCreditoAdicionalesTercero().isEmpty();

		String cuit = contexto.persona().cuit();
		try {
			if (!ConfigHB.esProduccion() && contexto.requestHeader("cuit") != null && !contexto.requestHeader("cuit").isEmpty()) {
				cuit = contexto.requestHeader("cuit");
			}
		} catch (Exception e) {
		}

		// Prestamo
		ApiRequest requestPrestamo = Api.request("BeneficiarioPrestamoTasaCero", "prestamos", "GET", "/v1/prestamos/creditos/{cuil}", contexto);
		requestPrestamo.path("cuil", cuit);
		requestPrestamo.cacheSesion = true;

		ApiResponse responsePrestamo = Api.response(requestPrestamo, contexto.idCobis());
		if (responsePrestamo.hayError()) {
			return Respuesta.error();
		}

		// INSERT
		SqlRequest request = Sql.request("SelectAuditorPagoTarjeta", "homebanking");
		request.sql = "INSERT INTO [homebanking].[dbo].[solicitudPrestamoTasaCero_2021] VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		request.add(contexto.idCobis());
		request.add(contexto.persona().cuit());
		request.add(contexto.persona().nombreCompleto());
		request.add(tarjetaCredito != null ? tarjetaCredito.numero() : null);
		request.add(detalleTarjetaCredito != null ? detalleTarjetaCredito.objetos().get(0).date("vigenciaHasta", "yyyy-MM-dd", "MM-yy") : null);
		request.add(tarjetaCredito != null ? tarjetaCredito.cuenta() : null);
		request.add(tarjetaCredito != null ? tarjetaCredito.tipo() : null);
		request.add(tieneAdicionalesTercero);
		request.add(responsePrestamo.string("email"));
		request.add(responsePrestamo.string("fechaSolicitud"));
		request.add(responsePrestamo.string("nroTarjeta"));
		request.add(responsePrestamo.string("monto"));
		request.add(responsePrestamo.string("importePeriodo1"));
		request.add(responsePrestamo.string("importePeriodo2"));
		request.add(responsePrestamo.string("importePeriodo3"));
		request.add(null);
		request.add(null);
		request.add(responsePrestamo.string("tipoCredito").equals("ORIGINAL") ? null : responsePrestamo.string("tipoCredito"));
		request.add(null);
		request.add(null);
		request.add(null);
		request.add(null);
		request.add(null);
		request.add(null);
		request.add(null);
		request.add(null);

		SqlResponse response = Sql.response(request);
		if (response.hayError) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Respuesta parametrosSegundoDesembolso(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		respuesta.add("tiposInmuebles", new Objeto().set("id", "01").set("descripcion", "CASA UNIFAMILIAR"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "02").set("descripcion", "CASA EN PH"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "03").set("descripcion", "DEPARTAMENTO EN PH"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "04").set("descripcion", "DUPLEX/TRIPLEX EN PH"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "05").set("descripcion", "LOCAL COMERCIAL"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "06").set("descripcion", "OFICINAS / CONSULTORIOS"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "07").set("descripcion", "DEPÓSITO / GALPON"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "08").set("descripcion", "INDUSTRIA"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "09").set("descripcion", "LOTE TERRENO"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "10").set("descripcion", "CHACRA / QUINTA"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "11").set("descripcion", "CAMPO"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "12").set("descripcion", "BARRIO DE CASAS UNIF"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "13").set("descripcion", "EDIFICIO / TORRE"));
		respuesta.add("tiposInmuebles", new Objeto().set("id", "14").set("descripcion", "OTRO"));

		return respuesta;
	}

	public static Respuesta solicitarSegundoDesembolso(ContextoHB contexto) {
		String tipoInmueble = contexto.parametros.string("tipoInmueble");
		String nombreContacto = contexto.parametros.string("nombreContacto", null);
		String horarioContacto = contexto.parametros.string("horarioContacto", null);
		String telefonoCodigoPais = contexto.parametros.string("telefonoCodigoPais", null);
		String telefonoCodigoArea = contexto.parametros.string("telefonoCodigoArea", null);
		String telefonoPrefijo = contexto.parametros.string("telefonoPrefijo", null);
		String telefonoCaracteristica = contexto.parametros.string("telefonoCaracteristica", null);
		String telefonoNumero = contexto.parametros.string("telefonoNumero", null);
		String direccionCalle = contexto.parametros.string("direccionCalle", null);
		Integer direccionNumero = contexto.parametros.integer("direccionNumero", null);
		String direccionPiso = contexto.parametros.string("direccionPiso", null);
		String direccionDepartamento = contexto.parametros.string("direccionDepartamento", null);
		Integer direccionCodigoPostal = contexto.parametros.integer("direccionCodigoPostal", null);
		Integer direccionCiudad = contexto.parametros.integer("direccionCiudad", null);
		Integer direccionProvincia = contexto.parametros.integer("direccionProvincia", null);
		String direccionPartido = contexto.parametros.string("direccionPartido", null);
		String entreCalle1 = contexto.parametros.string("entreCalle1", null);
		String entreCalle2 = contexto.parametros.string("entreCalle2", null);

		BigDecimal valor = null;
		String nemonicoSolicitud = "";
		Solicitud solicitud = Solicitud.solicitudSegundoDesembolsoProcrearRefaccion(contexto, contexto.persona().cuit());
		for (SolicitudProducto producto : solicitud.Productos) {
			if (producto.tipoProducto.equals("2")) {
				for (DesembolsoPropuesto desembolso : producto.DesembolsosPropuestos) {
					if (desembolso.NroDesembolso != null) {
						if (desembolso.NroDesembolso.equals(2)) {
							valor = desembolso.Monto;
							nemonicoSolicitud = producto.Nemonico;
						}
					}
				}
			}
		}

		Cuenta cuenta = null;
		for (Cuenta cuentaActual : contexto.cuentas()) {
			if ("PCA".equals(cuentaActual.categoria()) && cuentaActual.esPesos()) {
				cuenta = cuentaActual;
			}
		}

		// emm: si la cuenta sigue siendo nula me fijo entre los créditos que tiene si
		// alguno es procrear, y le asigno el préstamo ahí
		if (cuenta == null) {
			for (Prestamo prestamo : contexto.prestamos()) {
				if (prestamo.codigo().equals(nemonicoSolicitud)) {
					cuenta = prestamo.cuentaPago();
				}
			}
		}

//		if (cuenta == null) {
//			for (Cuenta cuentaActual : contexto.cuentas()) {
//				if (cuentaActual.esPesos()) {
//					cuenta = cuentaActual;
//				}
//			}
//		}

		ApiRequest request = Api.request("SolicitarSegundoDesembolso", "ventas_windows", "PUT", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{numeroPrestamo}/desembolso", contexto);
		request.path("numeroSolicitud", solicitud.Id);
		request.path("numeroPrestamo", solicitud.idPrestamo());

		Objeto desembolsos = request.body("Desembolsos");
		Objeto formasDesembolsos = desembolsos.set("FormasDesembolso");
		Objeto formaDesembolso = new Objeto();
		formaDesembolso.set("NroDesembolso", 2);
		formaDesembolso.set("Forma", cuenta.esCajaAhorro() ? "NCMNCA" : "NCMNCC");
		formaDesembolso.set("Referencia", cuenta.numero());
		formaDesembolso.set("Beneficiario", Texto.substring(contexto.persona().nombreCompleto(), 40));
		formaDesembolso.set("Valor", valor);
		formasDesembolsos.add(formaDesembolso);

		Objeto tasacion = request.body("Tasacion");
		tasacion.set("TipoInmueble", tipoInmueble);
		tasacion.set("NroUF", 0);
		tasacion.set("NombreContacto", nombreContacto);
		tasacion.set("HorarioContacto", horarioContacto);
		tasacion.set("TelContacto1CodigoPais", telefonoCodigoPais);
		tasacion.set("TelContacto1CodigoArea", telefonoCodigoArea);
		tasacion.set("TelContacto1Prefijo", telefonoPrefijo);
		tasacion.set("TelContacto1Caracteristica", telefonoCaracteristica);
		tasacion.set("TelContacto1Numero", telefonoNumero);
		tasacion.set("TelContacto1Interno", null);
		tasacion.set("Calle", direccionCalle);
		tasacion.set("Numero", direccionNumero);
		tasacion.set("Piso", direccionPiso);
		tasacion.set("Depto", direccionDepartamento);
		tasacion.set("EntreCalle1", entreCalle1);
		tasacion.set("EntreCalle2", entreCalle2);
		tasacion.set("CodigoPostal", direccionCodigoPostal);
		tasacion.set("Ciudad", direccionCiudad);
		tasacion.set("Provincia", direccionProvincia);
		tasacion.set("Partido", direccionPartido);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			if (!response.objetos("Errores").isEmpty()) {
				for (Objeto item : response.objetos("Errores")) {
					if (item.string("MensajeCliente").contains("Tasación: El código postal no coincide con la ciudad")) {
						item.set("MensajeCliente", "Ups! El código postal ingresado para inspeccionar el avance de tu obra no coincide con la ciudad. Por favor, volvé a la pantalla anterior y modificá el código postal para completar correctamente la solicitud.");
					}
					if (item.string("MensajeCliente").contains("Tasación: La ciudad es nula o no es valida")) {
						item.set("MensajeCliente", "La ciudad que ingresaste no es válida. Por favor, volvé a la pantalla anterior y corroborá que los datos estén bien cargados.");
					}
					if (item.string("MensajeCliente").contains("Tasación: Código de área inválido para el teléfono")) {
						item.set("MensajeCliente", "El código de área de télefono que ingresaste no es válido. Por favor, volvé a la pantalla anterior y modificá el teléfono para completar correctamente la solicitud.");
					}
				}
			}
			throw new ApiVentaException(response);
		}

		return Respuesta.exito();
	}

	public static Respuesta solicitarSegundoDesembolsoPrestamoHipotecario(ContextoHB contexto) {
		String tipoInmueble = contexto.parametros.string("tipoInmueble");
		String nombreContacto = contexto.parametros.string("nombreContacto", null);
		String horarioContacto = contexto.parametros.string("horarioContacto", null);
		String telefonoCodigoPais = contexto.parametros.string("telefonoCodigoPais", null);
		String telefonoCodigoArea = contexto.parametros.string("telefonoCodigoArea", null);
		String telefonoPrefijo = contexto.parametros.string("telefonoPrefijo", null);
		String telefonoCaracteristica = contexto.parametros.string("telefonoCaracteristica", null);
		String telefonoNumero = contexto.parametros.string("telefonoNumero", null);
		String direccionCalle = contexto.parametros.string("direccionCalle", null);
		Integer direccionNumero = contexto.parametros.integer("direccionNumero", null);
		String direccionPiso = contexto.parametros.string("direccionPiso", null);
		String direccionDepartamento = contexto.parametros.string("direccionDepartamento", null);
		Integer direccionCodigoPostal = contexto.parametros.integer("direccionCodigoPostal", null);
		Integer direccionCiudad = contexto.parametros.integer("direccionCiudad", null);
		Integer direccionProvincia = contexto.parametros.integer("direccionProvincia", null);
		String direccionPartido = contexto.parametros.string("direccionPartido", null);
		String entreCalle1 = contexto.parametros.string("entreCalle1", null);
		String entreCalle2 = contexto.parametros.string("entreCalle2", null);
		Integer nroUltimoDesembolsoLiquidado = contexto.parametros.integer("nroUltimoDesembolsoLiquidado", null);

		if (Objeto.anyEmpty(nroUltimoDesembolsoLiquidado)) {
			return Respuesta.parametrosIncorrectos();
		}

		Integer nroProximoDesembolso = nroUltimoDesembolsoLiquidado + 1;

		String idPrestamo = "";

		BigDecimal valor = null;
		String nemonicoSolicitud = "";
		Solicitud solicitud = Solicitud.solicitudSegundoDesembolsoPrestamoHipotecario(contexto, contexto.persona().cuit(), nroProximoDesembolso.toString());
		for (SolicitudProducto producto : solicitud.Productos) {
			if (producto.tipoProducto.equals("1")) {
				for (DesembolsoPropuesto desembolso : producto.DesembolsosPropuestos) {
					if (desembolso.NroDesembolso != null) {
						if (desembolso.NroDesembolso.equals(nroProximoDesembolso)) {
							valor = desembolso.Monto;
							nemonicoSolicitud = producto.Nemonico;
						}
					}
				}
				idPrestamo = producto.Id;
			}
		}

		Cuenta cuenta = null;
		for (Cuenta cuentaActual : contexto.cuentas()) {
			if ("PCA".equals(cuentaActual.categoria()) && cuentaActual.esPesos()) {
				cuenta = cuentaActual;
			}
		}

		// emm: si la cuenta sigue siendo nula me fijo entre los créditos que tiene si
		// alguno es procrear, y le asigno el préstamo ahí
		if (cuenta == null) {
			for (Prestamo prestamo : contexto.prestamos()) {
				if (prestamo.codigo().equals(nemonicoSolicitud)) {
					cuenta = prestamo.cuentaPago();
				}
			}
		}

		ApiRequest request = Api.request("SolicitarSegundoDesembolsoPrestamoHipotecario", "ventas_windows", "PUT", "/solicitudes/{numeroSolicitud}/prestamoHipotecario/{numeroPrestamo}/desembolso", contexto);
		request.path("numeroSolicitud", solicitud.Id);
		request.path("numeroPrestamo", idPrestamo);
		request.query("nro", nroProximoDesembolso.toString());

		Objeto desembolsos = request.body("Desembolsos");
		Objeto formasDesembolsos = desembolsos.set("FormasDesembolso");
		Objeto formaDesembolso = new Objeto();
		formaDesembolso.set("NroDesembolso", nroProximoDesembolso);
		formaDesembolso.set("Forma", cuenta.esCajaAhorro() ? "NCMNCA" : "NCMNCC");
		formaDesembolso.set("Referencia", cuenta.numero());
		formaDesembolso.set("Beneficiario", Texto.substring(contexto.persona().nombreCompleto(), 40));
		formaDesembolso.set("Valor", valor);
		formasDesembolsos.add(formaDesembolso);

		Objeto tasacion = request.body("Tasacion");
		tasacion.set("TipoInmueble", tipoInmueble);
		tasacion.set("NroUF", 0);
		tasacion.set("NombreContacto", nombreContacto);
		tasacion.set("HorarioContacto", horarioContacto);
		tasacion.set("TelContacto1CodigoPais", telefonoCodigoPais);
		tasacion.set("TelContacto1CodigoArea", telefonoCodigoArea);
		tasacion.set("TelContacto1Prefijo", telefonoPrefijo);
		tasacion.set("TelContacto1Caracteristica", telefonoCaracteristica);
		tasacion.set("TelContacto1Numero", telefonoNumero);
		tasacion.set("TelContacto1Interno", null);
		tasacion.set("Calle", direccionCalle);
		tasacion.set("Numero", direccionNumero);
		tasacion.set("Piso", direccionPiso);
		tasacion.set("Depto", direccionDepartamento);
		tasacion.set("EntreCalle1", entreCalle1);
		tasacion.set("EntreCalle2", entreCalle2);
		tasacion.set("CodigoPostal", direccionCodigoPostal);
		tasacion.set("Ciudad", direccionCiudad);
		tasacion.set("Provincia", direccionProvincia);
		tasacion.set("Partido", direccionPartido);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			if (!response.objetos("Errores").isEmpty()) {
				for (Objeto item : response.objetos("Errores")) {
					if (item.string("MensajeCliente").contains("Tasación: El código postal no coincide con la ciudad")) {
						item.set("MensajeCliente", "Ups! El código postal ingresado para inspeccionar el avance de tu obra no coincide con la ciudad. Por favor, volvé a la pantalla anterior y modificá el código postal para completar correctamente la solicitud.");
					}
					if (item.string("MensajeCliente").contains("Tasación: La ciudad es nula o no es valida")) {
						item.set("MensajeCliente", "La ciudad que ingresaste no es válida. Por favor, volvé a la pantalla anterior y corroborá que los datos estén bien cargados.");
					}
					if (item.string("MensajeCliente").contains("Tasación: Código de área inválido para el teléfono")) {
						item.set("MensajeCliente", "El código de área de télefono que ingresaste no es válido. Por favor, volvé a la pantalla anterior y modificá el teléfono para completar correctamente la solicitud.");
					}
				}
			}
			throw new ApiVentaException(response);
		}

		return Respuesta.exito();
	}

	public static Respuesta alertasPrestamosComplementarios(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		Integer cantidadSolicitadoPrestamo = cantidadSolicitadoPrestamoComplementario(contexto, "PROCOMPHOG");

//		String fechaTope = Config.string("fecha_tope_prestamo_complemetario", "09/04/2022");

		// && Momento.hoy().esAnterior(new Momento(fechaTope, "dd/MM/yyyy"))

		if (cantidadSolicitadoPrestamo == 0) {
			try {
				for (Prestamo prestamo : contexto.prestamos()) {
					if ("PROCCONHOG".equals(prestamo.codigo()) || "PROLOCOHOG".equals(prestamo.codigo())) {
						ApiRequest requestDetallePrestamo = Api.request("PagarPrestamo", "prestamos", "GET", "/v1/prestamos/{id}", contexto);
						requestDetallePrestamo.path("id", prestamo.numero());
						requestDetallePrestamo.query("detalle", "true");

						ApiResponse response = Api.response(requestDetallePrestamo);
						if (response.hayError()) {
							return Respuesta.error();
						}
						if (("3".equals(response.get("desembolsosRealizados")) || "4".equals(response.get("desembolsosRealizados"))) && "REEMBOLSO".equals(response.get("etapaConstruccion"))) {
							respuesta.set("alertaPPComplementario", true);
							break;
						}
					}
				}
				if (Objects.isNull(respuesta.get("alertaPPComplementario"))) {
					respuesta.set("alertaPPComplementario", false);
				}

			} catch (Exception ex) {
				respuesta.set("alertaPPComplementario", false);
			}
		} else {
			respuesta.set("alertaPPComplementario", false);
		}

		return respuesta;
	}

	public static Respuesta generarPrestamoComplementario(ContextoHB contexto) {
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Boolean solicitaComprobarIngresos = contexto.parametros.bool("solicitaComprobarIngresos", null);
		Integer plazo = contexto.parametros.integer("plazo");

		String cuit = contexto.persona().cuit();
		String cuitConyuge = RestPersona.cuitConyugeComplementario(contexto);
		Solicitud solicitud = Solicitud.solicitudPrestamoComplementario(contexto, cuit);

		if (Objects.isNull(solicitud)) {
			solicitud = Solicitud.generarSolicitud(contexto);
			solicitud.generarIntegrantes(contexto, cuit, cuitConyuge);
			solicitud.generarPrestamoComplementario(contexto, monto, plazo, cuit, cuitConyuge);

			ResolucionMotor resolucion = solicitud.ejecutarMotor(contexto, solicitaComprobarIngresos);
			HBOmnicanalidad.insertarLogMotor(contexto, solicitud.IdSolicitud, resolucion.ResolucionId, "Resolucion Generar Prestamo Complementario");
			if (!resolucion.esVerde() && !resolucion.esAprobadoAmarillo()) {
				Respuesta respuesta = Respuesta.estado(resolucion.esAmarillo() ? "RECHAZO_MOTOR_AMARILLO" : "RECHAZO_MOTOR");
				Objeto oferta = new Objeto();
				oferta.set("esOfertaMejorableComprobandoIngresos", resolucion.EsOfertaMejorableComprobandoIngresos);
				respuesta.set("oferta", oferta);
				return respuesta;
			}

			solicitud = Solicitud.solicitudPrestamoComplementario(contexto, cuit);
		}

		SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
		if (!monto.equals(prestamo.MontoSolicitado)) {
			solicitud = solicitud.actualizarPrestamoComplementario(contexto, monto, plazo, cuit, cuitConyuge);
			ResolucionMotor resolucion = solicitud.ejecutarMotor(contexto, solicitaComprobarIngresos);
			HBOmnicanalidad.insertarLogMotor(contexto, solicitud.IdSolicitud, resolucion.ResolucionId, "Resolucion Generar Prestamo Complementario");
			if (!resolucion.esVerde() && !resolucion.esAprobadoAmarillo()) {
				Respuesta respuesta = Respuesta.estado(resolucion.esAmarillo() ? "RECHAZO_MOTOR_AMARILLO" : "RECHAZO_MOTOR");
				Objeto oferta = new Objeto();
				oferta.set("esOfertaMejorableComprobandoIngresos", resolucion.EsOfertaMejorableComprobandoIngresos);
				respuesta.set("oferta", oferta);
				return respuesta;
			}
			prestamo = solicitud.prestamo(contexto);
		}
		ResolucionMotor resolucion = solicitud.consultarMotor(contexto, solicitaComprobarIngresos);
		HBOmnicanalidad.insertarLogMotor(contexto, solicitud.IdSolicitud, resolucion.ResolucionId, "Resolucion Consulta Generar Prestamo Complementario");
		Respuesta respuesta = new Respuesta();
		Objeto oferta = new Objeto();
		oferta.set("idSolicitud", solicitud.IdSolicitud);
		oferta.set("idPrestamo", solicitud.idPrestamo());
		oferta.set("monto", prestamo.MontoAprobado);
		oferta.set("valorOferta", prestamo.MontoAprobado);
		oferta.set("plazo", prestamo.Plazo);
		oferta.set("tasa", prestamo.Tasa);
		oferta.set("cft", cft(contexto, contexto.sesion.nuevoNemonico));
		oferta.set("esOfertaMejorableComprobandoIngresos", resolucion.EsOfertaMejorableComprobandoIngresos);

		oferta.set("tipo", "Refacción");
		oferta.set("moneda", "Pesos");
		oferta.set("simboloMoneda", "$");
		oferta.set("montoFormateado", Formateador.importe(prestamo.MontoAprobado));
		oferta.set("plazoFormateado", prestamo.Plazo + " meses");
		oferta.set("tnaFormateada", Formateador.importe(prestamo.Tasa));
		oferta.set("cftFormateado", Formateador.importe(prestamo.CFT));
		oferta.set("cuota", prestamo.importeCuota);
		oferta.set("cuotaFormateada", Formateador.importe(prestamo.importeCuota));
		oferta.set("formaPago", "Débito automatico");
		oferta.set("destinoManoObra", prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.UP));
		oferta.set("destinoMateriales", prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.DOWN));
		oferta.set("destinoManoObraFormateado", Formateador.importe(prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.UP)));
		oferta.set("destinoMaterialesFormateado", Formateador.importe(prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.DOWN)));
		respuesta.set("oferta", oferta);

		Boolean generaCuenta = true;
		for (Cuenta cuenta : contexto.cuentas()) {
			if (cuenta.esCajaAhorro() && cuenta.esPesos() && cuenta.esTitular()) {
				if (!cuenta.idEstado().equals("I")) {
					String tarjetaDebitoAsociada = null;
					TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociada(cuenta);
					if (tarjetaDebito != null) {
						tarjetaDebitoAsociada = "VISA xxxx-" + tarjetaDebito.ultimos4digitos();
					} else {
						if (contexto.tarjetaDebitoPorDefecto() != null) {
							tarjetaDebitoAsociada = "VISA xxxx-" + contexto.tarjetaDebitoPorDefecto().ultimos4digitos();
						}
					}

					Objeto item = new Objeto();
					//item.set("id", cuenta.id());
					item.set("id", cuenta.idEncriptado());
					item.set("ultimos4digitos", cuenta.ultimos4digitos());
					item.set("descripcion", cuenta.producto());
					item.set("numero", cuenta.numero());
					item.set("numeroFormateado", cuenta.numeroFormateado());
					item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
					item.set("titularidad", cuenta.titularidad());
					item.set("moneda", cuenta.moneda());
					item.set("simboloMoneda", cuenta.simboloMoneda());
					item.set("estado", cuenta.descripcionEstado());
					item.set("saldo", cuenta.saldo());
					item.set("saldoFormateado", cuenta.saldoFormateado());
					item.set("acuerdo", cuenta.acuerdo());
					item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
					item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
					item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
					item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
					item.set("tarjetaDebitoAsociada", tarjetaDebitoAsociada);
					item.set("debeReponerTarjeta", tarjetaDebitoAsociada != null);

					respuesta.add("cuentas", item);
					generaCuenta = false;
				}
			}
		}

		Objeto originacion = new Objeto();
		originacion.set("generaCuenta", generaCuenta);
		originacion.set("generaTarjetaDebito", generaCuenta);
		originacion.set("debeSubirDocumentacion", contexto.esProspecto());
		respuesta.set("originacion", originacion);

		return respuesta;
	}

	public static Respuesta actualizarPrestamoComplementario(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Integer plazo = contexto.parametros.integer("plazo");
		String seguroVida = contexto.parametros.string("seguroVida");
		Boolean solicitaComprobarIngresos = contexto.parametros.bool("solicitaComprobarIngresos", null);

		if (Objeto.anyEmpty(idCuenta, monto, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

//		String cuitConyugue = RestPersona.cuitConyugeComplementario(contexto);

		Solicitud solicitud = Solicitud.solicitudPrestamoComplementario(contexto, contexto.persona().cuit());
		solicitud.actualizarPrestamo(contexto, idCuenta, seguroVida, monto, null, HBArgentinaConstruye.esBatch());
		ResolucionMotor resolucion = solicitud.ejecutarMotor(contexto, solicitaComprobarIngresos);
		HBOmnicanalidad.insertarLogMotor(contexto, solicitud.IdSolicitud, resolucion.ResolucionId, "Resolucion Actualizar Prestamo Complementario");

		if (!resolucion.esVerde() && !resolucion.esAprobadoAmarillo()) {
			Respuesta respuesta = Respuesta.estado(resolucion.esAmarillo() ? "RECHAZO_MOTOR_AMARILLO" : "RECHAZO_MOTOR");
			Objeto oferta = new Objeto();
			oferta.set("esOfertaMejorableComprobandoIngresos", resolucion.EsOfertaMejorableComprobandoIngresos);
			respuesta.set("oferta", oferta);
			return respuesta;
		}

		Objeto nemonicos = new Objeto();

		nemonicos.add("PROCOMPHOG");

		if (idCuenta.equals("0")) {
			nemonicos.add("CASOLIC");
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("nemonicos", nemonicos);
		return respuesta;
	}

	public static Respuesta finalizarPrestamoComplementario(ContextoHB contexto) {
		Solicitud solicitud = Solicitud.solicitudPrestamoComplementario(contexto, contexto.persona().cuit());

		if (ConfigHB.esOpenShift()) {
			solicitud.finalizar(contexto);
		} else {
			solicitud.simularFinalizar(contexto);
			return Respuesta.exito();
		}

		try {
			ApiRequest request = Api.request("ActualizarEstadoOfertaPrestamo", "prestamos", "PATCH", "/v1/prestamos/aviso", contexto);
			request.body("idCobis", contexto.idCobis());
			request.body("nemonico", contexto.sesion.nuevoNemonico);
			ApiResponse response = Api.response(request, contexto.idCobis());
			HBOmnicanalidad.insertarLogApiVentas(contexto, solicitud.IdSolicitud, "ActualizarEstadoOfertaPrestamo", response.objetos("Errores").get(0).string("MensajeCliente"), response.objetos("Errores").get(0).string("MensajeDesarrollador"), response.objetos("Errores").get(0).string("Codigo"));
		} catch (Exception e) {
		}

		String rutaOrigen = rutaTemporal(contexto, solicitud.Id).replace("/1/documentacionAdjunta/", "").replace("/1/documentacionAdjunta", "");
		String rutaDestino = rutaFinal(contexto, solicitud.Id).replace("/1/documentacionAdjunta/", "").replace("/1/documentacionAdjunta", "");
		File origen = new File(rutaOrigen);
		if (origen.isDirectory()) {
			File destino = new File(rutaDestino);
			origen.renameTo(destino);
		}

		ProductosService.eliminarCacheProductos(contexto);

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", "Solicitaste el préstamo");
			parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
			Date hoy = new Date();
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
			parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
			parametros.set("CANAL", "Home Banking");
			parametros.set("TITULAR_CANAL", contexto.persona().apellido());
			parametros.set("NOMBRE_PRESTAMO", "Prestamo Complementario");

			if ("true".equals(ConfigHB.string("salesforce_prendido_alta_prestamo")) && HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				var parametrosSf = Objeto.fromJson(contexto.sesion.cache.get(ConfigHB.string("salesforce_alta_prestamo")));

				String salesforce_alta_prestamo = ConfigHB.string("salesforce_alta_prestamo");
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("ISMOBILE", contexto.esMobile());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("MONTO_PRESTAMO", parametrosSf.string("MONTO_PRESTAMO"));
				if(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA") != null) {
			        LocalDate today = LocalDate.now();
			        LocalDate fechaVencimiento = LocalDate.of(today.getYear(), today.getMonth().plus(1), Integer.parseInt(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA")));
			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					parametros.set("FECHA_VENCIMIENTO_CUOTA", fechaVencimiento.format(formatter).toString());
				}
				parametros.set("MONTO_CUOTA", parametrosSf.string("MONTO_CUOTA"));
				parametros.set("NUMERO_CUOTA", "1");
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_alta_prestamo, parametros));
			}
			else
				RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_alta_prestamo"), parametros);

		} catch (Exception e) {
		}

		incrementarCantidadSolicitadoPrestamoComplementario(contexto, "PROCOMPHOG");

		contexto.sesion.cache.remove(ConfigHB.string("salesforce_alta_prestamo"));
		return Respuesta.exito();
	}

	public static BigDecimal cft(ContextoHB contexto, String nemonico) {
		ApiResponse response = ofertasProcrear(contexto);
		if (response.hayError()) {
			return null;
		}

		for (Objeto item : response.objetos()) {
			if (item.string("estado").equals("SO")) {
				String nemonicoActual = item.string("nemonico");

				if ("PROCOMPHOG".equals(nemonicoActual)) {
					return item.bigDecimal("valorCFT");
				}
			}
		}

		return null;
	}

	public static ApiResponse ofertasProcrear(ContextoHB contexto) {
		ApiRequest request = Api.request("OfertasProcrear", "prestamos", "GET", "/v1/prestamos/{id}/beneficiario", contexto);
		request.path("id", contexto.idCobis());
		request.query("Tipo", "C");
		request.cacheSesion = true;
		request.permitirSinLogin = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
		return response;
	}

	public static String rutaTemporal(ContextoHB contexto, String idSolicitud) {
		String path = ConfigHB.string("path_documentacion_bpm").replace("SUCURSAL", "temp_SUCURSAL").replace("{NUMERO_SOLICITUD}", idSolicitud);
		new File(path).mkdirs();
		return path;
	}

	public static String rutaFinal(ContextoHB contexto, String idSolicitud) {
		String path = ConfigHB.string("path_documentacion_bpm").replace("{NUMERO_SOLICITUD}", idSolicitud);
		return path;
	}

	public static Integer cantidadSolicitadoPrestamoComplementario(ContextoHB contexto, String nemonico) {
		try {
			SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ?";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			Integer cantidad = Sql.response(sqlRequest).registros.size();
			return cantidad;
		} catch (Exception e) {
		}
		return 0;
	}

	public static void incrementarCantidadSolicitadoPrestamoComplementario(ContextoHB contexto, String nemonico) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertContador", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static Respuesta alertasProcrear(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		ApiRequest request = Api.request("EstadoSolicitudBPM", "procesos", "GET", "/procesos-de-negocio/v1/solicitud/estado", contexto);
		request.query("pNroDoc", contexto.persona().numeroDocumento());
		request.query("pDias", ConfigHB.string("dias_consulta_estado_solicitud_bpm", "90"));
		Futuro<ApiResponse> futuroResponse = new Futuro<>(() -> Api.response(request));

		Futuro<Solicitud> futuroSolicitud = new Futuro<>(() -> Solicitud.solicitudSegundoDesembolsoProcrearRefaccion(contexto, contexto.persona().cuit()));

		try {
			// Segundo Desembolso
			Solicitud solicitud = futuroSolicitud.get();
			respuesta.set("alertaSegundoDesembolso", solicitud != null);

			// Procrear
			ApiResponse response = futuroResponse.get();
			if (response.hayError()) {
				return Respuesta.error();
			}

			String descripcion = response.string("estado");
			String idSolicitud = response.string("idSolicitud");
			Boolean esSolicitudPrestamo = false;
			if (idSolicitud.isEmpty()) {
				respuesta.set("alertaFinalizarProcrear", false);
				respuesta.set("alertaSubirDocumentacionProcrear", false);
				return respuesta;
			}
			ApiResponse responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
			if (responseSolicitud.hayError()) {
				respuesta.set("alertaFinalizarProcrear", false);
				respuesta.set("alertaSubirDocumentacionProcrear", false);
				return respuesta;
			}
			Objeto datos = responseSolicitud.objetos("Datos").get(0);
			for (Objeto producto : datos.objetos("Productos")) {
				String tipoProducto = producto.string("IdProductoFrontEnd");
				if (tipoProducto.equals("2")) {
					esSolicitudPrestamo = true;
				}
			}
			if (esSolicitudPrestamo) {
				respuesta.set("alertaFinalizarProcrear", Objeto.setOf("PENDIENTE CONFIRMACION OPERACIONES").contains(descripcion));
				respuesta.set("alertaSubirDocumentacionProcrear", Objeto.setOf("PENDIENTE DE DOCUMENTACION ADICIONAL", "RECLAMO DE DOCUMENTACION").contains(descripcion));
			}
			// respuesta.set("alertaSubirDocumentacionProcrear", false);

		} catch (Exception e) {
			respuesta.set("alertaSegundoDesembolso", false);
			respuesta.set("alertaFinalizarProcrear", false);
			respuesta.set("alertaSubirDocumentacionProcrear", false);
		}
		return respuesta;
	}

	public static Respuesta movimientosPrestamo(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("cuenta");
		String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
		String secuencial = contexto.parametros.string("secuencial");
		String productoCobis = null;

		if (Objeto.anyEmpty(idCuenta, fecha, secuencial)) {
			return Respuesta.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		if ("Caja de Ahorro".equalsIgnoreCase(cuenta.producto())) {
			productoCobis = "4";
		}
		if ("Cuenta Corriente".equalsIgnoreCase(cuenta.producto())) {
			productoCobis = "3";
		}

		Respuesta respuesta = new Respuesta();
		ApiResponse response = RestPrestamo.movimientos(contexto, cuenta.numero(), fecha, productoCobis, secuencial);
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

	public static Respuesta movimientosDesembolso(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("cuenta");
		String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
		String secuencial = contexto.parametros.string("secuencial");
		String productoCobis = null;

		if (Objeto.anyEmpty(idCuenta, fecha, secuencial)) {
			return Respuesta.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		if ("Caja de Ahorro".equalsIgnoreCase(cuenta.producto())) {
			productoCobis = "4";
		}
		if ("Cuenta Corriente".equalsIgnoreCase(cuenta.producto())) {
			productoCobis = "3";
		}

		Respuesta respuesta = new Respuesta();
		ApiResponse response = RestPrestamo.movimientos(contexto, cuenta.numero(), fecha, productoCobis, secuencial);
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

	public static Boolean tienePrestamoTasaCero(ContextoHB contexto) {
		return existePrestamoTasaCero(contexto) || existePrestamoTasaCeroBefore2021(contexto);
	}

	/* ========== METODOS PRIVADOS ========== */
	private static Boolean existePrestamoTasaCero(ContextoHB contexto) {
		SqlRequest request = Sql.request("SelectSolicitudPrestamoTasaCero", "homebanking");
		request.sql = "SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero_2021] WHERE idCobis = ?";
		request.add(contexto.idCobis());

		SqlResponse response = Sql.response(request);
		return !response.registros.isEmpty();
	}

	private static Boolean existePrestamoTasaCeroBefore2021(ContextoHB contexto) {
		SqlRequest request = Sql.request("SelectSolicitudPrestamoTasaCero", "homebanking");
		request.sql = "SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero] WHERE idCobis = ?";
		request.add(contexto.idCobis());

		SqlResponse response = Sql.response(request);
		return !response.registros.isEmpty();
	}

	/* ========== METODOS PRIVADOS ========== */

	public static Respuesta modal(ContextoHB contexto) {
		try {

			if (contexto.sesion.ofertaPp) {
				return Respuesta.exito("mostrarModal", false).set("mostrarModalTC", false);
			} else {
				contexto.sesion.ofertaPp = (true);
			}

			Boolean enHorario = true;
			Respuesta response = getCampana(contexto);
			if (response.hayError()) {
				return Respuesta.exito("mostrarModal", false).set("mostrarModalTC", false);
			}

			Boolean mostrarModal = response.bool("mostrarModal");
			Objeto pp = response.objeto("pp");
			Boolean tnaAplicado = response.bool("tnaAplicado");
			String tna = response.string("tna");
			String monto = response.string("monto");

			// activar y acomodar horario en caso de necesitarlo
			enHorario = Momento.enHorario(false);

			// si se cambio usuario, clave, celular o mail hace menos de 48 hs no tiene que
			// mostrar el modal
			if (mostrarModal && RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false)) {
				mostrarModal = false;
			}

			if (!mostrarModal) {
				return HBTarjetas.modalTC(contexto, pp);
			}

			Respuesta respuesta = new Respuesta();
			respuesta.set("enHorario", enHorario);
			respuesta.set("mostrarModal", mostrarModal);
			respuesta.set("mostrarModalTC", false);
			respuesta.set("tnaAplicado", tnaAplicado);
			respuesta.set("tna", tna);
			respuesta.set("monto", monto);

			if (mostrarModal && enHorario) {
				contexto.parametros.set("nemonico", "ALERTA_PP");
				Util.contador(contexto);
			}

			return respuesta;

		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static Respuesta formasDePago(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		respuesta.set("AUTOMATIC_DEBIT", "Débito automático").set("CASH", "Efectivo por ventanilla");
		return respuesta;
	}

	public static Respuesta cambiarFormaPago(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		try {
			String idPrestamo = contexto.parametros.string("idPrestamo");
			String formaPago = contexto.parametros.string("formaPago");
			String idCuenta = contexto.parametros.string("idCuenta", "");
			String numeroProducto = contexto.parametros.string("numeroProducto", "");

			if (Objeto.anyEmpty(idPrestamo, formaPago, numeroProducto)) {
				return Respuesta.parametrosIncorrectos();
			}

			if (!formaPago.equals("CASH") && idCuenta.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
			}

			// tipo prestamo
			Prestamo prestamo = contexto.prestamo(idPrestamo);
			String tipi = prestamo.categoria().equals("HIPOTECARIO") ? "FP-PH" : "FP-PP";

			// forma pago
			String formaPagoNemonico = "CASH".equals(formaPago) ? "EFMN" : idCuenta.startsWith("3") ? "NDMNCC" : "NDMNCA";
			Objeto formaPagoDetalle = RestPrestamo.detalleFormaPago(contexto, numeroProducto, formaPagoNemonico);

			// cuentaPago
			Cuenta cuentaPago = contexto.cuenta(idCuenta);

			Objeto tcObj = new Objeto();
			tcObj.set("prestamoNumero", prestamo.numero());

			if (RestPostventa.tieneSolicitudEnCurso(contexto, tipi, tcObj, true)) {
				return Respuesta.estado("SOLICITUD_EN_CURSO");
			}

			// api cambiar forma pago
			ApiResponse caso = RestPostventa.cambioFormaPagoPrestamo(contexto, tipi, prestamo, formaPagoDetalle, cuentaPago);

			if (caso == null || caso.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			Objeto reclamo = (Objeto) caso.get("Datos");
			String numeroCaso = reclamo.objetos().get(0).string("NumeracionCRM");

			if (numeroCaso.isEmpty()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static boolean getEsPreaprobadoPP(ContextoHB contexto) {
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		Boolean esPreaprobado = false;

		ApiResponse response = ProductosService.getCampania(contexto);
		if (response.hayError()) {
			return false;
		}

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

		if (pp.bigDecimal("mtoPp", "0.0").compareTo(new BigDecimal(0)) == 1) {
			esPreaprobado = true;
		}

		return esPreaprobado;
	}

	public static Objeto preAprobado(ApiResponse response, ContextoHB contexto) {
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

	private static int getTopeMaximoDias() {
		// Fecha tope de XX dias default
		int topeDefault = 30;
		try {
			SqlRequest sqlRequest = Sql.request("Cuotificacion", "homebanking");

			sqlRequest.sql += "SELECT * FROM [Homebanking].[dbo].[parametros] WHERE nombre_parametro = 'cuotificacion.topemaximodias';";

			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (sqlResponse.hayError) {
				System.err.println("Error al recuperar el tope maximo dias de cuotificacion.");
				return topeDefault;
			} else if (sqlResponse.registros.isEmpty()) {
				return topeDefault;
			}

			return sqlResponse.registros.get(0).integer("valor");
		} catch (Exception err) {
			return topeDefault;
		}
	}

	public static List<Cuenta> getCuentasCuotificacion(ContextoHB contexto) {

		Respuesta tarjetaDebitoRespuesta = HBTarjetas.consolidadaTarjetasDebito(contexto);
		List<Objeto> listaTarjetaDebito = tarjetaDebitoRespuesta.objetos("tarjetasDebito");
		List<Cuenta> cajasDeAhorros = new ArrayList<Cuenta>();

		// Chequea si el usuario es empleado
		boolean esEmpleado = contexto.persona().esEmpleado();

		// Chequea si el usuario es preaprobado
		boolean esPreAprobado = getEsPreaprobadoPP(contexto);
		if (esEmpleado || !esPreAprobado)
			return cajasDeAhorros;

		for (Objeto tarjetaDebito : listaTarjetaDebito) {

			if (tarjetaDebito.string("estado").equalsIgnoreCase("HABILITADA")) {

				for (Objeto cuentaAsociada : tarjetaDebito.objetos("cuentasAsociadas")) {

					String numeroCuenta = (String) cuentaAsociada.get("id");
					Cuenta cuenta = contexto.cuenta(numeroCuenta);

					if (cuenta != null && cuenta.esTitular() && cuenta.esCajaAhorro() && cuenta.estaActiva() && cuenta.esPesos()) {

						cajasDeAhorros.add(cuenta);

						/*
						 * // Filtro de movimientos de los ultimos XX dias String idCaja = cuenta.id();
						 * String fechaDesde = Momento.hoy().restarDias(topeMaximo).string("d/M/yyyy");
						 * String fechaHasta = Momento.hoy().string("d/M/yyyy");
						 * contexto.parametros.set("idCuenta", idCaja);
						 * contexto.parametros.set("fechaDesde", fechaDesde);
						 * contexto.parametros.set("fechaHasta", fechaHasta);
						 * 
						 * // Se obtienen movimientos de ultimos XX dias Respuesta respuestaCuenta =
						 * ApiCuenta.movimientosCuenta(contexto); List<Objeto> movimientosCuenta =
						 * respuestaCuenta.objetos("movimientos");
						 * 
						 * if (!movimientosCuenta.isEmpty()) {
						 * 
						 * for (Objeto movimiento:movimientosCuenta) {
						 * 
						 * BigDecimal importe = movimiento.bigDecimal("importe"); // Movimientos de
						 * importe negativo if (importe.compareTo(BigDecimal.ZERO) < 0) {
						 * cajasDeAhorros.add(cuenta); break; }
						 * 
						 * }
						 * 
						 * }
						 * 
						 */
					}

				}

			}

		}

		return cajasDeAhorros;
	}

	public static Respuesta alertaPrestamoCuotificacion(ContextoHB contexto) {

		Respuesta respuesta = new Respuesta();

		// Se muestra una vez por sesion iniciada
		if (contexto.sesion.ofertaPpCuotificacionMostrada)
			return respuesta.set("alertaPPCuotificacion", false);

		if (RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false))
			return respuesta.set("alertaPPCuotificacion", false);

		// int topeMaximo = getTopeMaximoDias();

		// Fecha tope de muestra de alerta cuotificacion
		String fechaTope = ConfigHB.string("fecha_tope_cuotificacion", "20220826");
		Momento momentoHoy = Momento.hoy();
		Momento momentoTope = new Momento(fechaTope, "yyyyMMdd");
		boolean esAnterior = momentoHoy.esAnterior(momentoTope);

		// Chequea si el usuario no es empleado
		boolean noEsEmpleado = !contexto.persona().esEmpleado();

		// Chequea si el usuario es preaprobado
		boolean esPreAprobado = getEsPreaprobadoPP(contexto);

		if (noEsEmpleado && esAnterior && esPreAprobado) {

			Respuesta tarjetaDebitoRespuesta = HBTarjetas.consolidadaTarjetasDebito(contexto);
			List<Objeto> listaTarjetaDebito = tarjetaDebitoRespuesta.objetos("tarjetasDebito");
			List<Cuenta> cajasDeAhorros = new ArrayList<Cuenta>();

			// Falta probar el caso de que tenga dos tarjetas con cuentas
			// Falta probar el caso de que no tenga tarjetas

			// Chequea si el usuario tiene tarjetas de debito
			if (!listaTarjetaDebito.isEmpty()) {

				for (Objeto tarjetaDebito : listaTarjetaDebito) {

					if (tarjetaDebito.string("estado").equalsIgnoreCase("HABILITADA")) {

						for (Objeto cuentaAsociada : tarjetaDebito.objetos("cuentasAsociadas")) {

							String numeroCuenta = (String) cuentaAsociada.get("id");
							Cuenta cuenta = contexto.cuenta(numeroCuenta);

							if (cuenta != null && cuenta.esTitular() && cuenta.esCajaAhorro() && cuenta.estaActiva() && cuenta.esPesos())
								cajasDeAhorros.add(cuenta);
						}

					}

				}

				// boolean tieneMovimientosDebito = false;

				// Chequea si las tarjetas de debito tienen cuentas asociadas que cumplan
				// esTitular, esCajaAhorro, estaActiva y esPesos
				if (!cajasDeAhorros.isEmpty()) {
					/*
					 * for (Cuenta cajaAhorro: cajasDeAhorros) { // Filtro de movimientos de los
					 * ultimos XX dias String idCaja = cajaAhorro.id(); String fechaDesde =
					 * Momento.hoy().restarDias(topeMaximo).string("d/M/yyyy"); String fechaHasta =
					 * Momento.hoy().string("d/M/yyyy"); contexto.parametros.set("idCuenta",
					 * idCaja); contexto.parametros.set("fechaDesde", fechaDesde);
					 * contexto.parametros.set("fechaHasta", fechaHasta);
					 * 
					 * // Se obtienen movimientos de ultimos XX dias Respuesta respuestaCuenta =
					 * ApiCuenta.movimientosCuenta(contexto); List<Objeto> movimientosCuenta =
					 * respuestaCuenta.objetos("movimientos"); List<Objeto> movimientosCuentaDebito
					 * = new ArrayList<Objeto>();
					 * 
					 * if (!movimientosCuenta.isEmpty()) {
					 * 
					 * Predicate<Objeto> porImporte = movimiento ->
					 * movimiento.bigDecimal("importe").compareTo(BigDecimal.ZERO) < 0;
					 * 
					 * movimientosCuentaDebito = movimientosCuenta.stream() .filter(porImporte)
					 * .collect(Collectors.toList());
					 * 
					 * if (!movimientosCuentaDebito.isEmpty()) {
					 * 
					 * tieneMovimientosDebito = true; break;
					 * 
					 * } }
					 * 
					 * }
					 * 
					 * if (tieneMovimientosDebito) contexto.sesion.setPPCuotificacionMostrada(true);
					 * 
					 * respuesta.set("alertaPPCuotificacion", tieneMovimientosDebito);
					 */

					contexto.sesion.ofertaPpCuotificacionMostrada = (true);
					respuesta.set("alertaPPCuotificacion", true);

				} else
					respuesta.set("alertaPPCuotificacion", false);
			} else
				respuesta.set("alertaPPCuotificacion", false);
		} else
			respuesta.set("alertaPPCuotificacion", false);

		return respuesta;
	}

	public static Respuesta movimientosCuotificacion(ContextoHB contexto) {

		Respuesta respuestaMovimientos = new Respuesta();

		boolean movimientosDebug = false;
		int MAX_MOV = 5;

		List<Objeto> listaMovimientos = new ArrayList<Objeto>();
		respuestaMovimientos.set("movimientos", listaMovimientos);

		int topeMaximo = getTopeMaximoDias();

		// Fecha tope de muestra de alerta cuotificacion
		String fechaTope = ConfigHB.string("fecha_tope_cuotificacion", "20220826");
		Momento momentoHoy = Momento.hoy();
		Momento momentoTope = new Momento(fechaTope, "yyyyMMdd");
		boolean esAnterior = momentoHoy.esAnterior(momentoTope);

		// Chequea si el usuario no es empleado
		boolean noEsEmpleado = !contexto.persona().esEmpleado();

		// Chequea si el usuario es preaprobado
		boolean esPreAprobado = getEsPreaprobadoPP(contexto);

		Respuesta tarjetaDebitoRespuesta = HBTarjetas.consolidadaTarjetasDebito(contexto);
		List<Objeto> listaTarjetaDebito = tarjetaDebitoRespuesta.objetos("tarjetasDebito");
		List<Cuenta> cajasDeAhorros = new ArrayList<Cuenta>();

		// Chequeo de si el usuario tiene tarjetas de debito y condiciones anteriores
		if (!listaTarjetaDebito.isEmpty() && noEsEmpleado && esAnterior && esPreAprobado) {

			for (Objeto tarjetaDebito : listaTarjetaDebito) {

				if (tarjetaDebito.string("estado").equalsIgnoreCase("HABILITADA")) {

					for (Objeto cuentaAsociada : tarjetaDebito.objetos("cuentasAsociadas")) {

						String numeroCuenta = (String) cuentaAsociada.get("id");
						Cuenta cuenta = contexto.cuenta(numeroCuenta);

						if (cuenta != null && cuenta.esTitular() && cuenta.esCajaAhorro() && cuenta.estaActiva() && cuenta.esPesos())
							cajasDeAhorros.add(cuenta);

					}

				}

			}

			// Chequeo de si las tarjetas de debito tienen cuentas asociadas que cumplan
			// esTitular, esCajaAhorro, estaActiva y esPesos
			if (!cajasDeAhorros.isEmpty()) {

				for (Cuenta cajaAhorro : cajasDeAhorros) {
					// Filtro de movimientos de los ultimos XX dias
					String idCaja = cajaAhorro.id();
					String fechaDesde = Momento.hoy().restarDias(topeMaximo).string("d/M/yyyy");
					String fechaHasta = Momento.hoy().string("d/M/yyyy");
					contexto.parametros.set("idCuenta", idCaja);
					contexto.parametros.set("fechaDesde", fechaDesde);
					contexto.parametros.set("fechaHasta", fechaHasta);

					// Se obtienen movimientos de ultimos XX dias
					Respuesta respuestaCuenta = HBCuenta.movimientosCuenta(contexto);
					List<Objeto> movimientosCuenta = respuestaCuenta.objetos("movimientos");

					if (!movimientosCuenta.isEmpty()) {

						Predicate<Objeto> porImporte = movimiento -> movimiento.bigDecimal("importe").compareTo(BigDecimal.ZERO) < 0;

						movimientosCuenta = movimientosCuenta.stream().filter(porImporte).collect(Collectors.toList());

						listaMovimientos.addAll(movimientosCuenta);

					}

				}

				boolean tieneMovimientosCuentas = !listaMovimientos.isEmpty();

				if (movimientosDebug) {

					for (int i = 0; i < MAX_MOV; i++) {
						Objeto prueba1 = new Objeto();
						int fecha = (i + 1) % 30 + 1;

						String fechaFormato = Integer.toString(fecha).length() == 1 ? "0" + fecha : Integer.toString(fecha);
						BigDecimal monto = BigDecimal.valueOf(-12200 * (i + 1) / 0.76).setScale(2, RoundingMode.DOWN);
						;

						prueba1.set("fecha", fechaFormato + "/11/2021");
						if (i % 2 == 0) {
							prueba1.set("descripcion", "CONFITERIA LAS VE - BARRIO MAR #" + i);
						} else {
							prueba1.set("descripcion", "PERTUTTI - LOMAS DE ZA #" + i);
						}

						prueba1.set("importe", monto);
						prueba1.set("importeFormateado", Formateador.importe(monto));

						prueba1.set("saldo", 558712.77);
						prueba1.set("saldoFormateado", "558.712,77");
						prueba1.set("simboloMoneda", "$");
						prueba1.set("nroOperacion", "33642917");
						prueba1.set("hora", "2021-11-" + fechaFormato + "T18:34:00.000");
						prueba1.set("fechaMovimiento", fechaFormato + "/11/2021");
						prueba1.set("categoria", "PAGOS");
						prueba1.set("subCategoria", "CONSUMOS");
						prueba1.set("tipoMsg", "Pago_consumos");
						prueba1.set("cuitCP", "");
						prueba1.set("idEstablecimiento", "");
						prueba1.set("rubro", "");
						prueba1.set("tieneComprobante", true);
						prueba1.set("causa", "361");
						prueba1.set("tipo", "pagoConsumos");

						listaMovimientos.add(prueba1);
					}

				}

				if (tieneMovimientosCuentas) {
					// Se ordenan movimientos por fecha y hora, del mas nuevo al mas antiguo

					Collections.sort(listaMovimientos, (Objeto o1, Objeto o2) -> {

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

				// Se devuelven movimientos y flag de alerta
				respuestaMovimientos.set("alertaPPCuotificacionMovimientos", tieneMovimientosCuentas);
				respuestaMovimientos.set("movimientos", listaMovimientos);

			} else
				respuestaMovimientos.set("alertaPPCuotificacionMovimientos", false);
		} else
			respuestaMovimientos.set("alertaPPCuotificacionMovimientos", false);

		return respuestaMovimientos;
	}

	public static void insertarLogCuotificacion(ContextoHB contexto, Integer idSolicitud) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertarSolicitudCuotificacion", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_pp_cuotificacion] (IdSolicitud, IdCliente, Fecha) VALUES (?, ?, GETDATE())";
			sqlRequest.add(idSolicitud);
			sqlRequest.add(contexto.idCobis());
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static Respuesta solicitarCuotificacion(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		respuesta = HBOriginacion.solicitarPrimerOfertaPrestamo(contexto);
		boolean primeraOferta = contexto.parametros.bool("primeraOferta");
		if (!respuesta.hayError() && primeraOferta) {
			insertarLogCuotificacion(contexto, respuesta.integer("idSolicitud"));
		}

		return respuesta;
	}

	public static Respuesta aceptaTerminosCondiciones(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String funcionalidad = contexto.parametros.string("funcionalidad");
		Boolean aceptaTyC = contexto.parametros.bool("aceptaTyC", false);
		String estado = "NO_APLICA";

		if (contexto.idCobis() == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		if (Objeto.anyEmpty(idSolicitud, funcionalidad)) {
			return Respuesta.parametrosIncorrectos();
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
		return Respuesta.exito("estado", estado);
	}

	/*** se consulta la campaña que pueda aplicar para el cliente en sesion **/
	public static Respuesta getCampana(ContextoHB contexto) {
		Boolean mostrarModal = false;
		Boolean tnaAplicado = false;
		String tna = "";
		String monto = "";

		ApiResponse response = ProductosService.getCampania(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto pp = preAprobado(response, contexto);
		Respuesta respuesta = new Respuesta();
		respuesta.set("mostrarModal", mostrarModal);

		if (pp.bigDecimal("mtoPp", "0.0").compareTo(new BigDecimal(0)) == 1) {
			mostrarModal = true;
			if (pp.get("tasaAplicada") != null && (pp.bigDecimal("tasaAplicada", "0.0").compareTo(new BigDecimal(0)) == 1)) {
				tnaAplicado = true;
				if (pp.bigDecimal("tasaAplicada").compareTo(ConfigHB.bigDecimal("tasa_atractiva_pp")) == 0) {
					tna = Formateador.importeCantDecimales(pp.bigDecimal("tasaAplicada"), 2);
				}
			}

			// monto = Formateador.importeCantDecimales(pp.bigDecimal("mtoPp"), 2);
			// DLV-45313 se desea en el monto no mostrar mas los decimales, carecen de
			// sentido
			monto = Formateador.entero(pp.bigDecimal("mtoPp").longValue());
		}

		respuesta.set("mostrarModal", mostrarModal);
		respuesta.set("tnaAplicado", tnaAplicado);
		respuesta.set("tna", tna);
		respuesta.set("monto", monto);
		respuesta.set("pp", pp);
		return respuesta;
	}

	private static void remanenteOfertaPreAprobada(ContextoHB contexto, Respuesta respuesta, Respuesta ofertaPreAprobada) {

		if (ConfigHB.bool("prendido_remanente_adelanto") && contexto.tieneAdelantoActivo() && ofertaPreAprobada.existe("adelantoBH") && (contexto.esPlanSueldo() || (contexto.esJubilado() && contexto.tieneCuentaCategoriaB()))) {
			BigDecimal totalPreaprobado = ofertaPreAprobada.objeto("adelantoBH").bigDecimal("aplicado");
			BigDecimal remanente = tieneRemanenteAdelantoBh(contexto, totalPreaprobado);

			if (remanente != BigDecimal.ZERO && remanente.compareTo(ConfigHB.bigDecimal("monto_minimo_PP_adelanto")) > 0) {
				BigDecimal porcentajeRemanente = remanente.multiply(new BigDecimal(100)).divide(totalPreaprobado, 0, RoundingMode.HALF_UP);
				BigDecimal acumuladoAdelantoBh = acumuladoAdelantoBh(contexto, totalPreaprobado);
				respuesta.set("utilizadoPreaprobadoAdelanto", acumuladoAdelantoBh);
				respuesta.set("utilizadoPreaprobadoAdelantoFormateado", Formateador.importe(acumuladoAdelantoBh));
				respuesta.set("remanentePreaprobadoAdelanto", remanente);
				respuesta.set("remanentePreaprobadoAdelantoFormateado", Formateador.importe(remanente));
				respuesta.set("porcentajeRemanente", porcentajeRemanente.doubleValue());

			} else {
				respuesta.set("mostrarBotonSolicitudAdelanto", false);
			}
		}
	}

	public static BigDecimal tieneRemanenteAdelantoBh(ContextoHB contexto, BigDecimal montoPreAprobado) {

		BigDecimal remanente = BigDecimal.ZERO;
		BigDecimal montosAprobados = BigDecimal.ZERO;
		montosAprobados = acumuladoAdelantoBh(contexto, montoPreAprobado);

		remanente = montosAprobados != BigDecimal.ZERO ? montoPreAprobado.subtract(montosAprobados) : BigDecimal.ZERO;
		if (remanente != BigDecimal.ZERO && (remanente.compareTo(ConfigHB.bigDecimal("monto_minimo_PP_adelanto")) > 0)) {
			return remanente;
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal acumuladoAdelantoBh(ContextoHB contexto, BigDecimal montoPreAprobado) {

		BigDecimal montosAprobados = BigDecimal.ZERO;
		for (Prestamo prestamo : contexto.prestamos()) {
			if (prestamo.codigo().equalsIgnoreCase("PPADELANTO")) {
				montosAprobados = montosAprobados.add(prestamo.montoAprobado());
			}
		}
		return montosAprobados;
	}

	public static Respuesta obtenerHipotecasPrestamo(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		String cuit = contexto.persona().cuit();
		try {
			if (!ConfigHB.esProduccion() && !contexto.requestHeader("cuit").isEmpty()) {
				cuit = contexto.requestHeader("cuit");
			}
		} catch (Exception e) {
		}

		if (Objeto.anyEmpty(cuit)) {
			return Respuesta.parametrosIncorrectos();
		}

		ApiResponse response = RestPrestamo.hipotecas(contexto, cuit);
		if (response.hayError()) {
			if (response.codigo == 500) {
				return Respuesta.estado("ERROR_API");
			} else {
				return Respuesta.estado("NO_DETERMINADO");
			}
		} else if (!response.hayError() && (response.lista == null) ) {
			return Respuesta.estado("NO_TIENE_HIPOTECAS");
		}

		List<Object> datos = response.lista;
		List<Objeto> prestamos = datos.stream().map(obj -> (Objeto) obj).filter(prestamo -> { Object enCurso = prestamo.get("inicioTramite");
															return enCurso != null && "NO".equalsIgnoreCase(enCurso.toString());
												}).collect(Collectors.toList());
		if (prestamos == null || (prestamos != null && prestamos.size() == 0 )) {
			return Respuesta.estado("HIPOTECA_INICIADA");
		}

		for (Object prestamoObject : prestamos) {
			Objeto prestamo = ((Objeto) prestamoObject);

			Map<String, Object> hipotecaPrestamo = new HashMap<String, Object>();
			hipotecaPrestamo.put("nroPrestamo", prestamo.get("nroPrestamo"));
			hipotecaPrestamo.put("estadoPrestamo", prestamo.get("estadoPrestamo"));
			hipotecaPrestamo.put("garantia", prestamo.get("garantia"));
			hipotecaPrestamo.put("codCiudadGarantia", prestamo.get("codigoCiudad"));
			hipotecaPrestamo.put("ciudadGarantia", prestamo.get("ciudad"));
			hipotecaPrestamo.put("nroPrestamoMig", prestamo.get("nroPrestamoMigrado"));
			respuesta.add("hipotecas", hipotecaPrestamo);
		}

		return respuesta;
	}

	public static void obtenerProximaCuotaV4(ContextoHB contexto, PrestamosV4.PrestamoV4 prestamo, Objeto item) {
		List<CuotaPrestamo> cuotas = contexto.prestamo(prestamo.codigoProducto).cuotas();
		CuotaPrestamo prestamoNormal = null;

		if (!cuotas.isEmpty()) {
			Optional<CuotaPrestamo> result = cuotas.stream().filter(cuota -> cuota.estado().equals(ESTADO_CUOTA_NORMAL)).findFirst();
			if (result.isPresent()) {
				prestamoNormal = (CuotaPrestamo) result.get();
			}
		}

		if (Objects.nonNull(prestamoNormal)) {
			item.set("proximaCuota", prestamoNormal.importeCuota());
		}

//		Integer diasVencimientoCuota = Momento.diferenciaEntreFechas(prestamo.fechaProximoVenc);
//		if (diasVencimientoCuota >= 0 && diasVencimientoCuota <= 3) {
//			item.set("diasVencimientoCuota", diasVencimientoCuota);
//		}
	}

}
