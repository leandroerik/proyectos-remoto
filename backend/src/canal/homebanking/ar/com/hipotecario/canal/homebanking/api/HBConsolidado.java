package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Momento;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijo;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijoLogro;
import ar.com.hipotecario.canal.homebanking.negocio.Prestamo;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestContexto;
import ar.com.hipotecario.canal.homebanking.servicio.SqlPrestamos;

public class HBConsolidado {

	public static final int TOPE_MOVIMIENTOS = 5;
	public static final String OPERACION_MOVIMIENTOS_TC = "2500,2510,3189,1200";
	private static final String TEXTO_HOY_FECHA_PROX_VENCIMIENTO = "Hoy ";
	private static final String TEXTO_DIA_FECHA_PROX_VENCIMIENTO = "Este ";
	private static final String TEXTO_SEMANA_FECHA_PROX_VENCIMIENTO = "Próximo ";

	@SuppressWarnings("unchecked")
	public static Respuesta consolidadoMovimientos(ContextoHB contexto) {
		String tipoMoneda = contexto.parametros.string("tipo");
		Boolean topeMovimientos = contexto.parametros.bool("topeMovimientos");
		String fechaDesde = Fecha.restarDias(new Date(), 30L, "yyyy-MM-dd");
		String fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();
		Respuesta respuesta = new Respuesta();
		Respuesta respuestaMovimientos = new Respuesta();

		List<Cuenta> cuentas = contexto.cuentas();
		filtrarTipoCuentas(tipoMoneda, cuentas);
		if (!cuentas.isEmpty()) {
			if (!topeMovimientos) {
				respuestaMovimientos = HBCuenta.consolidadoMovimientosCuentas(fechaDesde, fechaHasta, cuentas, contexto);
			} else {
				respuestaMovimientos = HBCuenta.consolidado5MovimientosCuentas(cuentas, contexto);
			}

			if (!respuestaMovimientos.hayError()) {
				ultimosMovimientos.addAll(((List<Objeto>) respuestaMovimientos.get("ultimosMovimientos")));
			}
		}

		List<TarjetaCredito> tarjetas = contexto.tarjetasCredito();
		if (!tarjetas.isEmpty()) {
			respuestaMovimientos = HBTarjetas.consolidadoMovimientosTarjeta(contexto, tarjetas, tipoMoneda, topeMovimientos, fechaDesde, fechaHasta);
			if (!respuestaMovimientos.hayError()) {
				ultimosMovimientos.addAll(((List<Objeto>) respuestaMovimientos.get("ultimosMovimientosTC")));
			}
		}

		if (!ultimosMovimientos.isEmpty()) {
			ordenarPorFecha(ultimosMovimientos);
			if (topeMovimientos) {
				ultimosMovimientos = tomaNMovimientosFiltradoXFecha(ultimosMovimientos, TOPE_MOVIMIENTOS, fechaDesde);
			}
			respuesta.set("ultimosMovimientos", ultimosMovimientos);
		} else if (respuestaMovimientos.hayError()) {
			return respuestaMovimientos;
		}
		return respuesta;
	}

	private static List<Cuenta> filtrarTipoCuentas(String tipo, List<Cuenta> cuentas) {
		if (tipo == null || tipo.isEmpty()) {
			return cuentas;
		}
		cuentas.removeIf(cuenta -> (!tipo.equalsIgnoreCase(cuenta.simboloMoneda())));
		return cuentas;
	}

	private static void ordenarPorFecha(List<Objeto> ultimosMovimientos) {
		Collections.sort(ultimosMovimientos, Collections.reverseOrder(new Comparator<Objeto>() {
			public int compare(Objeto o1, Objeto o2) {
				Date a = o1.date("fecha", "dd/MM/yyyy");
				Date b = o2.date("fecha", "dd/MM/yyyy");
				return a.compareTo(b);
			}
		}));
	}

	public static List<Objeto> tomaNMovimientosFiltradoXFecha(List<Objeto> ultimosMovimientos, int tope, String fechaDesde) {
		LocalDate fechaDesdeDate = LocalDate.parse(fechaDesde);
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/MM/yyyy");

		for (Iterator<Objeto> iterator = ultimosMovimientos.iterator(); iterator.hasNext();) {
			Objeto objeto = (Objeto) iterator.next();
			LocalDate fechaDate = LocalDate.parse(objeto.string("fecha"), formato);
			if (!(fechaDate.isEqual(fechaDesdeDate) || fechaDate.isAfter(fechaDesdeDate))) {
				iterator.remove();
			}
		}

		if (ultimosMovimientos.size() > tope) {
			return ultimosMovimientos.subList(0, tope);
		}
		return ultimosMovimientos;
	}

	public static Respuesta consolidadoProximosVencimientos(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		Objeto proximosVencimientos = new Objeto();
		
		if (contexto.idCobis() == null || contexto.idCobis().isEmpty()) {
			return Respuesta.estado("SIN_SESION");
		}
		
// TODO: esto esta tirando errores en PROD
//		List<Objeto> objetos = proximoVencimientoPlazoFijo(contexto);
//		for (Objeto item : objetos) {
//			proximosVencimientos.add("plazoFijo", item);
//		}

		respuesta.set("proximosVencimientos", proximosVencimientos);

		return respuesta;
	}

	protected static List<Objeto> proximoVencimientoPlazoFijo(ContextoHB contexto) {
		Objeto objetosPlazosFijos = new Objeto();
		List<PlazoFijo> plazosFijos = new ArrayList<>();
		List<PlazoFijo> plazosFijosLogros = new ArrayList<>();

		for (PlazoFijo plazoFijo : contexto.getPlazosFijos()) {
			if (verificarProximoVencimiento(plazoFijo.fechaVencimiento("yyyy-MM-dd"))) {
				if (plazoFijo.validarPFLogros()) {
					plazosFijosLogros.add(plazoFijo);
				} else {
					plazosFijos.add(plazoFijo);
				}
			}
		}

		iteraPlazoFijo(objetosPlazosFijos, contexto, plazosFijos, null);
		iteraPlazoFijoLogros(objetosPlazosFijos, contexto, plazosFijosLogros);
		iteraPlazoFijoLogrosCuotas(objetosPlazosFijos, contexto);

		if (!objetosPlazosFijos.esLista()) {
			return null;
		}

		return Fecha.ordenarPorFechaAsc(objetosPlazosFijos, "fechaVencimiento", "dd/MM/yyyy");
	}

	private static void iteraPlazoFijoLogrosCuotas(Objeto objetosPlazosFijos, ContextoHB contexto) {
		List<PlazoFijoLogro> listaPlazoFijoLogros = contexto.plazosFijosLogros();
		Map<PlazoFijoLogro, List<ApiResponse>> detalle = PlazoFijoLogro.plazosFijosLogroDetalle(contexto);

		if (listaPlazoFijoLogros != null && !listaPlazoFijoLogros.isEmpty()) {
			for (PlazoFijoLogro plazoFijoLogro : listaPlazoFijoLogros) {
				Objeto item = new Objeto();
				Boolean encontro = false;

				for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {

					if ("A".equalsIgnoreCase(plazoFijoLogro.idEstado()) && plazoFijoLogro.itemFechaPagoCuota(id, "dd/MM/yyyy", detalle) == null && verificarProximoVencimiento(plazoFijoLogro.itemFechaVencimientoCuota(id, "yyyy-MM-dd", detalle))) {

						String tipoimporte = Formateador.simboloMoneda(plazoFijoLogro.idMoneda());
						item.set("id", plazoFijoLogro.id());
						item.set("descripcion", plazoFijoLogro.descripcionPFLogros(id, detalle));
						item.set("tipo", "Logros " + tipoimporte);
						item.set("moneda", Formateador.moneda(plazoFijoLogro.idMoneda()));
						item.set("esLogroCuotas", true);
						item.set("numeroTotalCuota", plazoFijoLogro.cantidadPlazosFijos());
						item.set("fechaVencimiento", plazoFijoLogro.itemFechaVencimientoCuota(id, "dd/MM/yyyy", detalle));
						item.set("fechaTextoCierre", textoFechaVencimiento(plazoFijoLogro.itemFechaVencimientoCuota(id, "dd/MM/yyyy", detalle)));
						item.set("esUVA", plazoFijoLogro.esUva(detalle));
						item.set("importeInicial", plazoFijoLogro.itemMontoInicial(id, detalle));
						item.set("importeInicialFormateado", "-" + tipoimporte + " " + Formateador.importe(plazoFijoLogro.itemMontoInicial(id, detalle)));

						Cuenta cuenta = contexto.cuenta(plazoFijoLogro.numeroCuenta());
						String cuentaDescripcion = "";
						if (cuenta != null) {
							cuentaDescripcion = cuenta.descripcionCorta() + " " + cuenta.simboloMonedaActual() + cuenta.numeroEnmascaradoAsteriscos();
							item.set("bancaCuentaNumero", cuentaDescripcion);
							item.set("formaAcredita", "Se debitará de tu");
						}
						encontro = true;
						break;
					}
				}
				if (encontro) {
					objetosPlazosFijos.add(item);
				}
			}
		}
	}

	protected static List<Objeto> proximoVencimientoPrestamos(ContextoHB contexto) {
		Objeto objetosPrestamos = new Objeto();

		for (Prestamo prestamo : contexto.prestamos()) {
			if (verificarProximoVencimiento(prestamo.fechaProximoVencimiento("yyyy-MM-dd"))) {
				if (!"C".equals(prestamo.idEstado())) {
					Objeto item = new Objeto();
					item.set("id", prestamo.id());
					item.set("numero", prestamo.numero());
					item.set("descripcion", prestamo.descripcionPrestamo());
					item.set("tipo", prestamo.tipo());
					item.set("moneda", prestamo.descripcionMoneda());
					item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
					item.set("montoCuotaActual", prestamo.montoUltimaCuota());
					item.set("montoCuotaActualFormateado", "-" + prestamo.simboloMonedaActual() + " " + prestamo.montoUltimaCuotaFormateado());
					item.set("estado", prestamo.consolidada().string("descEstado"));
					item.set("esProcrear", prestamo.consolidada().string("esProCrear").equals("S"));
					item.set("fechaTextoCierre", textoFechaVencimiento(prestamo.fechaProximoVencimiento("dd/MM/yyyy")));
					formaPagoPrestamo(prestamo, item);
					objetosPrestamos.add(item);
				}
			}
		}

		if (!objetosPrestamos.esLista()) {
			return null;
		}
		return Fecha.ordenarPorFechaAsc(objetosPrestamos, "fechaVencimiento", "dd/MM/yyyy");
	}

	private static String operadorImporte(BigDecimal importe) {
		BigDecimal operador = importe.multiply(new BigDecimal(-1));
		return operador.signum() == -1 ? "-" : "+";
	}

	protected static void setearImporteAlVencimientoTC(TarjetaCredito tarjetaCredito, Objeto item) {
		BigDecimal pesosPositivo = tarjetaCredito.debitosPesos().signum() == -1 ? Formateador.importeNegativo(tarjetaCredito.debitosPesos()) : tarjetaCredito.debitosPesos();
		BigDecimal dolaresPositivo = tarjetaCredito.debitosDolares().signum() == -1 ? Formateador.importeNegativo(tarjetaCredito.debitosDolares()) : tarjetaCredito.debitosDolares();

		item.set("debitosPesos", pesosPositivo);
		String operador = operadorImporte(tarjetaCredito.debitosPesos());
		item.set("debitosPesosFormateado", operador + " $ " + Formateador.importe(pesosPositivo));

		item.set("debitosDolares", dolaresPositivo);
		operador = operadorImporte(tarjetaCredito.debitosDolares());
		item.set("debitosDolaresFormateado", operador + " U$S " + Formateador.importe(dolaresPositivo));

	}

	protected static String caso(TarjetaCredito tarjetaCredito, boolean stopDebit) {
		String formaPagoEdb = "stop_debit";
		if (!stopDebit) {
			formaPagoEdb = "pago_total";
			if (tarjetaCredito.esPagoMinimo()) {
				formaPagoEdb = "pago_minimo";
			}
			if (tarjetaCredito.formaPago().equals("Efectivo")) {
				formaPagoEdb = "pago_efectivo";
			}
		}
		return formaPagoEdb;
	}

	private static Objeto iteraPlazoFijo(Objeto objetosPlazosFijos, ContextoHB contexto, List<PlazoFijo> listaPlazoFijo, Map<Date, Map<String, BigDecimal>> importesLogros) {

		for (PlazoFijo plazoFijo : listaPlazoFijo) {
			String tipoimporte = "+" + plazoFijo.monedaActual() + " ";
			BigDecimal totalAlVencimiento = plazoFijo.importeInicial().add(plazoFijo.intereses()).subtract(plazoFijo.impuestos());
			BigDecimal importeTotal = importesLogros == null ? totalAlVencimiento : importesLogros.get(plazoFijo.fechaVencimiento()).get(plazoFijo.monedaActual());

			Objeto item = new Objeto();
			item.set("id", plazoFijo.id());
			item.set("descripcion", plazoFijo.descripcionPlazoFijo());
			item.set("tipo", plazoFijo.descripcion());
			item.set("numero", plazoFijo.numero());
			item.set("moneda", plazoFijo.descripcionMoneda());
			item.set("importeInicial", importeTotal);
			item.set("importeInicialFormateado", tipoimporte + Formateador.importe(importeTotal));
			item.set("fechaAlta", plazoFijo.fechaAlta("dd/MM/yyyy"));
			item.set("fechaTextoCierre", textoFechaVencimiento(plazoFijo.fechaVencimiento("dd/MM/yyyy")));
			item.set("fechaVencimiento", plazoFijo.fechaVencimiento("dd/MM/yyyy"));
			Cuenta cuenta = contexto.cuenta(plazoFijo.cuentaAcredita());
			String cuentaDescripcion = "";
			if (cuenta != null) {
				cuentaDescripcion = cuenta.descripcionCorta() + " " + cuenta.simboloMonedaActual() + cuenta.numeroEnmascaradoAsteriscos();
			}
			item.set("bancaCuentaNumero", cuentaDescripcion);
			formaAcreditaMoneda(plazoFijo, item);
			objetosPlazosFijos.add(item);
		}

		return objetosPlazosFijos;
	}

	private static void formaPagoPrestamo(Prestamo prestamo, Objeto item) {
		item.set("formaPago", prestamo.formaPago());
		if (!"Efectivo".equals(prestamo.formaPago()) && prestamo.debitoAutomatico()) {
			item.set("esDebitoAutomatico", prestamo.debitoAutomatico());
			Cuenta cuenta = prestamo.cuentaPago();
			item.set("bancaCuentaNumero", cuenta.descripcionCorta() + " " + cuenta.simboloMonedaActual() + cuenta.numeroEnmascaradoAsteriscos());
		}
	}

	private static Objeto iteraPlazoFijoLogros(Objeto objetosPlazosFijos, ContextoHB contexto, List<PlazoFijo> listaPlazoFijoLogros) {

		if (!listaPlazoFijoLogros.isEmpty()) {
			Map<Date, Map<String, List<PlazoFijo>>> uniquePF = listaPlazoFijoLogros.stream().collect(Collectors.groupingBy(w -> w.fechaVencimiento(), Collectors.groupingBy(w -> w.moneda())));

			Map<Date, Map<String, BigDecimal>> totalImporteLogros = totalImporteLogros(uniquePF);
			List<PlazoFijo> listaOriginal = sinDuplicados(uniquePF);
			iteraPlazoFijo(objetosPlazosFijos, contexto, listaOriginal, totalImporteLogros);
		}
		return objetosPlazosFijos;
	}

	private static Map<Date, Map<String, BigDecimal>> totalImporteLogros(Map<Date, Map<String, List<PlazoFijo>>> listaAgrupadaPorFecha) {
		Map<Date, Map<String, BigDecimal>> mapTotalImporteXfecha = new HashMap<>();
		listaAgrupadaPorFecha.forEach((fecha, listaPlazoFijoXMoneda) -> {
			Map<String, BigDecimal> map = new HashMap<>();
			totalImporteXMoneda(listaPlazoFijoXMoneda, map);
			mapTotalImporteXfecha.put(fecha, map);
		});
		return mapTotalImporteXfecha;
	}

	private static Map<String, BigDecimal> totalImporteXMoneda(Map<String, List<PlazoFijo>> listaPlazoFijoXMoneda, Map<String, BigDecimal> map) {

		listaPlazoFijoXMoneda.forEach((moneda, listaPlazoFijo) -> {
			BigDecimal sum = listaPlazoFijo.stream().map(plazofijo -> plazofijo.importeInicial().add(plazofijo.intereses().subtract(plazofijo.impuestos()))).reduce(BigDecimal.ZERO, BigDecimal::add);
			map.put(moneda, sum);
		});
		return map;
	}

	private static List<PlazoFijo> sinDuplicados(Map<Date, Map<String, List<PlazoFijo>>> uniquePF) {
		List<PlazoFijo> nuevaLista = new ArrayList<>();

		uniquePF.forEach((fecha, listaPalzoFijoXMoneda) -> {
			listaPalzoFijoXMoneda.forEach((moneda, listaPalzoFijo) -> {
				nuevaLista.add(listaPalzoFijo.get(0));
			});
		});
		return nuevaLista;
	}

	private static void formaAcreditaMoneda(PlazoFijo plazoFijo, Objeto item) {
		item.set("cancelacionAnticipada", plazoFijo.tieneCancelacionAnticipada());

		if (plazoFijo.descripcion().contains("UVA") && plazoFijo.tieneCancelacionAnticipada()) {
			item.set("formaAcredita", "Cancelado anticipadamente.");
			item.set("formaIntereses", "Se acreditará en tu");
		} else {
			formaAcredita(plazoFijo, item);
		}
	}

	private static void formaAcredita(PlazoFijo plazoFijo, Objeto item) {
		item.set("renueva", plazoFijo.esRenueva());
		item.set("renuevaIntereses", plazoFijo.esRenuevaIntereses());

		if (plazoFijo.esRenueva() && plazoFijo.esRenuevaIntereses()) {
			item.set("formaAcredita", "Se renovará automáticamente el monto más los intereses.");

		} else if (plazoFijo.esRenueva() && !plazoFijo.esRenuevaIntereses()) {
			item.set("formaAcredita", "Se renovará automáticamente el monto.");
			item.set("formaIntereses", "Los intereses se acreditarán en tu");

		} else {
			item.set("formaAcredita", "Se acreditará en tu");
		}
	}

	private static boolean verificarProximoVencimiento(String fechaCierre) {
		try {
			LocalDate fechaHoy = LocalDate.now();
			LocalDate fechaCierreAviso = LocalDate.parse(fechaCierre);
			LocalDate fechaInicioAviso = LocalDate.parse(fechaCierre).minusDays(7L);
			if (fechaHoy.isEqual(fechaInicioAviso) || fechaHoy.isEqual(fechaCierreAviso) || (fechaHoy.isAfter(fechaInicioAviso) && fechaHoy.isBefore(fechaCierreAviso))) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return false;
	}

	private static String textoFechaVencimiento(String fechaVencimiento) {
		DayOfWeek fechaHoy = LocalDate.now().getDayOfWeek();
		LocalDate fechaHoyDate = LocalDate.now();
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate fechaVencimientoAviso = LocalDate.parse(fechaVencimiento, formato);
		String fechaTexto = StringUtils.substringBeforeLast(fechaVencimiento, "/");

		if (fechaHoyDate.isEqual(fechaVencimientoAviso)) {
			return TEXTO_HOY_FECHA_PROX_VENCIMIENTO + fechaTexto;

		} else if (fechaVencimientoAviso.getDayOfWeek().getValue() > fechaHoy.getValue()) {
			return TEXTO_DIA_FECHA_PROX_VENCIMIENTO + Fecha.diaES(fechaVencimientoAviso.getDayOfWeek()) + " " + fechaTexto;

		} else if (fechaVencimientoAviso.getDayOfWeek().getValue() <= fechaHoy.getValue()) {
			return TEXTO_SEMANA_FECHA_PROX_VENCIMIENTO + Fecha.diaES(fechaVencimientoAviso.getDayOfWeek()) + " " + fechaTexto;
		}

		return "";
	}

	public static Respuesta ofertaPreAprobada(ContextoHB contexto) {
		Boolean enHorario = Momento.enHorario(true);
		Boolean tieneAdelanto = false;

		// LLAMADAS EN PARALELO
		Futuro<ApiResponse> futuroGetCampania = new Futuro<>(() -> ProductosService.getCampania(contexto));
		Futuro<Objeto> futuroOfertaPP = new Futuro<>(() -> validaMostrarOfertaPP(futuroGetCampania.get(), contexto));
		Futuro<Objeto> futuroOfertaA = new Futuro<>(() -> validaMostrarOfertaA(futuroGetCampania.get(), contexto));
		Futuro<Respuesta> futuroDesembolsoOnline = null;
		if (ConfigHB.bool("prendido_desembolso_online")) {
			futuroDesembolsoOnline = new Futuro<>(() -> HBOmnicanalidad.solicitudesDesembolsoOnline(contexto));
		}
		// FIN LLAMADAS EN PARALELO

		if (ConfigHB.bool("prendido_desembolso_online")) {
			Respuesta respuesta = new Respuesta();
			Respuesta desembolsoOnline = futuroDesembolsoOnline.get();

			if (desembolsoOnline != null) {
				if (desembolsoOnline.existe("solicitudes")) {
					respuesta.set("desembolso", desembolsoOnline);
					contexto.parametros.set("nemonico", "ALERTA_DESEMBOLSO_SEGMENTO");
					new Futuro<>(() -> Util.contador(contexto));
					contexto.parametros.set("nemonico", "ALERTA_DESEMBOLSO_MODAL");
					new Futuro<>(() -> Util.contador(contexto));
					return respuesta;
				}
			}
		}

		ApiResponse response = futuroGetCampania.get();
		if (response.hayError()) {
			return Respuesta.estado("NO_POSEE_CAMPANA_PREAPROBADA");
		}

		Respuesta respuesta = new Respuesta();
		Objeto salidaPrestamo = futuroOfertaPP.get();
		Objeto salidaAdelanto = futuroOfertaA.get();

		if (salidaAdelanto != null) {
			if (salidaAdelanto.bool("mostrar") && enHorario) {
				contexto.parametros.set("nemonico", "ALERTA_ADELANTO_MODAL");
				new Futuro<>(() -> Util.contador(contexto));
			}
			tieneAdelanto = salidaAdelanto.bool("mostrarModal");
			if (contexto.tienePPProcrearDesembolsado()) {
				salidaAdelanto.set("mostrarModal", false);
			}
			if (contexto.esJubilado() && contexto.tieneCuentaCategoriaB()) {
				respuesta.set("jubiladoAptoParaAdelanto", true);
			}
			respuesta.set("adelantoBH", salidaAdelanto);
		}

		if (salidaPrestamo != null) {
			contexto.sesion.montoMaximoPrestamo = (salidaPrestamo.bigDecimal("montoPP"));
			if (salidaPrestamo.bool("mostrarModal") && enHorario) {
				contexto.parametros.set("nemonico", "ALERTA_PP");
				new Futuro<>(() -> Util.contador(contexto));
			}
			if (tieneAdelanto) {
				salidaPrestamo.set("mostrarModal", false);
			}
			respuesta.set("prestamoPersonal", salidaPrestamo);
		}

		return respuesta;
	}

	/** Valida si el cliente tiene preaprobado de prestamo personal **/
	private static Objeto validaMostrarOfertaPP(ApiResponse response, ContextoHB contexto) {
		Boolean mostrarOfertaPP = false;
		Boolean mostrarModalOfertaPP = false;
		Boolean tnaAplicado = false;
		String tna = "";
		Objeto salidaPP = new Objeto();

		if (!ConfigHB.bool("prendido_alta_prestamos")) {
			return null;
		}

		Objeto preAprobado = HBPrestamo.preAprobado(response, contexto);

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

		Boolean enHorario = Momento.enHorario(true);
		if (RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false)) {
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

	/** Valida si el cliente tiene preaprobado de adelanto BH **/
	private static Objeto validaMostrarOfertaA(ApiResponse response, ContextoHB contexto) {
		boolean mostrarSegmento = false;
		BigDecimal montoPreAprobado = BigDecimal.ZERO;
		boolean mostrarModal = false;
		BigDecimal remanente = BigDecimal.ZERO;

		if (!ConfigHB.bool("prendido_adelanto_bh") || contexto.persona().esEmpleado()) {
			return null;
		}

		Objeto preAprobado = HBPrestamo.preAprobado(response, contexto);

		if (preAprobado.existe("saldoDisponibleAdelantoBH") && preAprobado.bigDecimal("saldoDisponibleAdelantoBH") != null && preAprobado.bigDecimal("saldoDisponibleAdelantoBH", "0.0").compareTo(BigDecimal.ZERO) > 0) {
			montoPreAprobado = preAprobado.bigDecimal("saldoDisponibleAdelantoBH");

			if (!RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false)) {
				remanente = ConfigHB.bool("prendido_remanente_adelanto") ? HBPrestamo.tieneRemanenteAdelantoBh(contexto, montoPreAprobado) : BigDecimal.ZERO;
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

}
