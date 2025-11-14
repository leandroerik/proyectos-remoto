package ar.com.hipotecario.mobile.api;

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
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.PlazoFijo;
import ar.com.hipotecario.mobile.negocio.PlazoFijoLogro;
import ar.com.hipotecario.mobile.negocio.Prestamo;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;

public class MBConsolidado {

	public static final int TOPE_MOVIMIENTOS = 5;
	public static final String OPERACION_MOVIMIENTOS_TC = "2500,2510,3189,1200";
	private static final String TEXTO_HOY_FECHA_PROX_VENCIMIENTO = "Hoy ";
	private static final String TEXTO_DIA_FECHA_PROX_VENCIMIENTO = "Este ";
	private static final String TEXTO_SEMANA_FECHA_PROX_VENCIMIENTO = "Próximo ";

	@SuppressWarnings("unchecked")
	public static RespuestaMB consolidadoMovimientos(ContextoMB contexto) {
		String tipoMoneda = contexto.parametros.string("tipo");
		Boolean topeMovimientos = contexto.parametros.bool("topeMovimientos");
		String fechaDesde = Fecha.restarDias(new Date(), 30L, "yyyy-MM-dd");
		String fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		List<Objeto> ultimosMovimientos = new ArrayList<Objeto>();
		RespuestaMB respuesta = new RespuestaMB();
		RespuestaMB respuestaMovimientos = new RespuestaMB();

		List<Cuenta> cuentas = contexto.cuentas();
		List<TarjetaCredito> tarjetas = contexto.tarjetasCredito();
		
		filtrarTipoCuentas(tipoMoneda, cuentas);
		
		// LLAMADAS EN PARALELO
		Futuro<RespuestaMB> futuroRespuestaMovimientos = null;
		Futuro<RespuestaMB> futuroRespuestaMovimientosTC = null;
		if (!cuentas.isEmpty()) {
			if (!topeMovimientos) {
				futuroRespuestaMovimientos = new Futuro<>(() -> MBCuenta.consolidadoMovimientosCuentas(fechaDesde, fechaHasta, cuentas, contexto));
			} else {
				futuroRespuestaMovimientos = new Futuro<>(() -> MBCuenta.consolidado5MovimientosCuentas(cuentas, contexto));
			}
		}

		if (!tarjetas.isEmpty()) {
			futuroRespuestaMovimientosTC = new Futuro<>(() -> MBTarjetas.consolidadoMovimientosTarjeta(contexto, tarjetas, tipoMoneda, topeMovimientos));
		}
		
		// FIN LLAMADAS EN PARALELO
		
		if (!cuentas.isEmpty()) {
			respuestaMovimientos = futuroRespuestaMovimientos.get();
			if (!respuestaMovimientos.hayError()) {
				ultimosMovimientos.addAll(((List<Objeto>) respuestaMovimientos.get("ultimosMovimientos")));
			}
		}

		if (!tarjetas.isEmpty()) {
			respuestaMovimientos = futuroRespuestaMovimientosTC.get();
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
		if (tipo.isEmpty() || tipo == null) {
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

	public static RespuestaMB consolidadoProximosVencimientos(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		Objeto proximosVencimientos = new Objeto();

		proximosVencimientos.set("tarjetaCredito", proximoVencimientoTarjeta(contexto));
		proximosVencimientos.set("plazoFijo", proximoVencimientoPlazoFijo(contexto));
		proximosVencimientos.set("prestamos", proximoVencimientoPrestamos(contexto));

		respuesta.set("proximosVencimientos", proximosVencimientos);

		return respuesta;
	}

	private static List<Objeto> proximoVencimientoTarjeta(ContextoMB contexto) {
		Objeto proximoVencimientoTarjeta = new Objeto();
		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
		boolean stopDebit = false;
		if (tarjetaCredito == null) {
			return null;
		}

		Objeto item = new Objeto();
		String cuentaDebito = "";
		String tipoCuentaDebito = "";

		if (verificarProximoVencimiento(tarjetaCredito.fechaVencimiento("yyyy-MM-dd"))) {
			item.set("tipo", tarjetaCredito.tipo());
			item.set("descripcionTipo", "VISA");
			item.set("numero", tarjetaCredito.idEncriptado());
			item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascaradoAsteriscos());
			setearImporteAlVencimientoTC(tarjetaCredito, item);
			item.set("pagoMinimoActual", tarjetaCredito.pagoMinimo());
			item.set("pagoMinimoActualFormateado", "$ " + Formateador.importe(tarjetaCredito.pagoMinimo()));
			item.set("fechaTextoCierre", textoFechaVencimiento(tarjetaCredito.fechaVencimiento("dd/MM/yyyy")));
			item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM/yyyy"));
			item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));

			boolean tieneCuenta = false;
			if (!tarjetaCredito.formaPago().equals("Efectivo")) {
				cuentaDebito = tarjetaCredito.bancaCuentaNumero();
				if (!cuentaDebito.isEmpty()) {
					String moneda = tarjetaCredito.debitosPesos().compareTo(new BigDecimal("0.0")) == 0 ? "U$S" : "$";
					cuentaDebito = "****" + Formateador.ultimos4digitos(cuentaDebito);
					tipoCuentaDebito = tarjetaCredito.bancaCuentaTipo();
					item.set("bancaCuentaNumero", tipoCuentaDebito + " " + moneda + cuentaDebito);
					tieneCuenta = true;
				}
				stopDebit = "NO".equals(tarjetaCredito.stopDebit()) ? false : true;
			}
			item.set("formaPago", tarjetaCredito.formaPagoPV(tieneCuenta));
			item.set("esPagoMinimo", tarjetaCredito.esPagoMinimo());
			item.set("stopDebit", stopDebit);
			item.set("formaPagoEdb", caso(tarjetaCredito, stopDebit));

			proximoVencimientoTarjeta.add(item);
		}

		if (!proximoVencimientoTarjeta.esLista()) {
			return null;
		}

		return Fecha.ordenarPorFechaAsc(proximoVencimientoTarjeta, "fechaVencimiento", "dd/MM/yyyy");
	}

	private static List<Objeto> proximoVencimientoPlazoFijo(ContextoMB contexto) {
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

	private static List<Objeto> proximoVencimientoPrestamos(ContextoMB contexto) {
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
					item.set("fechaVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
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

	private static String caso(TarjetaCredito tarjetaCredito, boolean stopDebit) {
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

	private static String operadorImporte(BigDecimal importe) {
		BigDecimal operador = importe.multiply(new BigDecimal(-1));
		return operador.signum() == -1 ? "-" : "+";
	}

	private static void setearImporteAlVencimientoTC(TarjetaCredito tarjetaCredito, Objeto item) {
		BigDecimal pesosPositivo = tarjetaCredito.debitosPesos().signum() == -1 ? Formateador.importeNegativo(tarjetaCredito.debitosPesos()) : tarjetaCredito.debitosPesos();
		BigDecimal dolaresPositivo = tarjetaCredito.debitosDolares().signum() == -1 ? Formateador.importeNegativo(tarjetaCredito.debitosDolares()) : tarjetaCredito.debitosDolares();

		item.set("debitosPesos", pesosPositivo);
		String operador = operadorImporte(tarjetaCredito.debitosPesos());
		item.set("debitosPesosFormateado", operador + " $ " + Formateador.importe(pesosPositivo));

		item.set("debitosDolares", dolaresPositivo);
		operador = operadorImporte(tarjetaCredito.debitosDolares());
		item.set("debitosDolaresFormateado", operador + " U$S " + Formateador.importe(dolaresPositivo));

	}

	private static void formaPagoPrestamo(Prestamo prestamo, Objeto item) {
		item.set("formaPago", prestamo.formaPago());
		if (!"Efectivo".equals(prestamo.formaPago()) && prestamo.debitoAutomatico()) {
			item.set("esDebitoAutomatico", prestamo.debitoAutomatico());
			Cuenta cuenta = prestamo.cuentaPago();
			item.set("bancaCuentaNumero", cuenta.descripcionCorta() + " " + cuenta.simboloMonedaActual() + " " + cuenta.numeroEnmascaradoAsteriscos());
		}
	}

	private static boolean verificarProximoVencimiento(String fechaCierre) {
		LocalDate fechaHoy = LocalDate.now();
		LocalDate fechaCierreAviso = LocalDate.parse(fechaCierre);
		LocalDate fechaInicioAviso = LocalDate.parse(fechaCierre).minusDays(7L);
		if (fechaHoy.isEqual(fechaInicioAviso) || fechaHoy.isEqual(fechaCierreAviso) || (fechaHoy.isAfter(fechaInicioAviso) && fechaHoy.isBefore(fechaCierreAviso))) {
			return true;
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
			return TEXTO_DIA_FECHA_PROX_VENCIMIENTO + Fecha.diaCapitalize(fechaVencimientoAviso.getDayOfWeek()) + " " + fechaTexto;

		} else if (fechaVencimientoAviso.getDayOfWeek().getValue() <= fechaHoy.getValue()) {
			return TEXTO_SEMANA_FECHA_PROX_VENCIMIENTO + Fecha.diaCapitalize(fechaVencimientoAviso.getDayOfWeek()) + " " + fechaTexto;
		}

		return "";
	}

	private static void iteraPlazoFijo(Objeto objetosPlazosFijos, ContextoMB contexto, List<PlazoFijo> listaPlazoFijo, Map<Date, Map<String, BigDecimal>> importesLogros) {

		for (PlazoFijo plazoFijo : listaPlazoFijo) {
			plazoFijo.consideraTodos(true);
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
	}

	private static void iteraPlazoFijoLogrosCuotas(Objeto objetosPlazosFijos, ContextoMB contexto) {
		List<PlazoFijoLogro> listaPlazoFijoLogros = contexto.plazosFijosLogros();

		if (listaPlazoFijoLogros != null && !listaPlazoFijoLogros.isEmpty()) {
			for (PlazoFijoLogro plazoFijoLogro : listaPlazoFijoLogros) {
				Objeto item = new Objeto();
				Boolean encontro = false;

				for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {

					if ("A".equalsIgnoreCase(plazoFijoLogro.idEstado()) && plazoFijoLogro.itemFechaPagoCuota(id, "dd/MM/yyyy") == null && verificarProximoVencimiento(plazoFijoLogro.itemFechaVencimientoCuota(id, "yyyy-MM-dd"))) {

						String tipoimporte = Formateador.simboloMoneda(plazoFijoLogro.idMoneda());
						item.set("id", plazoFijoLogro.id());
						item.set("descripcion", plazoFijoLogro.descripcionPFLogros(id));
						item.set("tipo", "Logros " + tipoimporte);
						item.set("moneda", Formateador.moneda(plazoFijoLogro.idMoneda()));
						item.set("esLogroCuotas", true);
						item.set("numeroTotalCuota", plazoFijoLogro.cantidadPlazosFijos());
						item.set("fechaVencimiento", plazoFijoLogro.itemFechaVencimientoCuota(id, "dd/MM/yyyy"));
						item.set("fechaTextoCierre", textoFechaVencimiento(plazoFijoLogro.itemFechaVencimientoCuota(id, "dd/MM/yyyy")));
						item.set("esUVA", plazoFijoLogro.esUva());
						item.set("importeInicial", plazoFijoLogro.itemMontoInicial(id));
						item.set("importeInicialFormateado", "-" + tipoimporte + " " + Formateador.importe(plazoFijoLogro.itemMontoInicial(id)));

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

	private static void iteraPlazoFijoLogros(Objeto objetosPlazosFijos, ContextoMB contexto, List<PlazoFijo> listaPlazoFijoLogros) {

		if (!listaPlazoFijoLogros.isEmpty()) {
			Map<Date, Map<String, List<PlazoFijo>>> uniquePF = listaPlazoFijoLogros.stream().collect(Collectors.groupingBy(w -> w.fechaVencimiento(), Collectors.groupingBy(w -> w.moneda())));

			Map<Date, Map<String, BigDecimal>> totalImporteLogros = totalImporteLogros(uniquePF);
			List<PlazoFijo> listaOriginal = sinDuplicados(uniquePF);
			iteraPlazoFijo(objetosPlazosFijos, contexto, listaOriginal, totalImporteLogros);
		}
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

		if (plazoFijo.descripcion().toUpperCase().contains("UVA") && plazoFijo.tieneCancelacionAnticipada()) {
			item.set("formaAcredita", "Cancelado anticipadamente.");
			item.set("formaIntereses", "Se acreditará en tu");
		} else {
			formaAcredita(plazoFijo, item);
		}
	}

	private static void formaAcredita(PlazoFijo plazoFijo, Objeto item) {
		item.set("renueva", plazoFijo.tieneRenovacionAutomatica());
		item.set("renuevaIntereses", plazoFijo.esRenuevaIntereses());

		if (plazoFijo.tieneRenovacionAutomatica() && plazoFijo.esRenuevaIntereses()) {
			item.set("formaAcredita", "Se renovará automáticamente el monto más los intereses.");

		} else if (plazoFijo.tieneRenovacionAutomatica() && !plazoFijo.esRenuevaIntereses()) {
			item.set("formaAcredita", "Se renovará automáticamente el monto.");
			item.set("formaIntereses", "Los intereses se acreditarán en tu");

		} else {
			item.set("formaAcredita", "Se acreditará en tu");
		}
	}
}
