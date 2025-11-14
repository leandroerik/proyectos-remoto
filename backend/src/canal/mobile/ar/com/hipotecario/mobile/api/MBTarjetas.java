package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.negocio.*;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;

public class MBTarjetas {
	private static final String REGEX_ESPACIOS = "\\s{2,}";

	private static final String flagTarjetaCredito = "prendido_tarjetas_credito_api";

	private static final String REGEX_FECHA_VENCIMIENTO = "\\b(0[1-9]|1[0-2])\\/?([0-9]{4})\\b";
	private static final String REGEX_ANIO = "^[0-9]{4}$";
	private static final String REGEX_MES = "^[0-9]{2}$";
	public static final String[] CODIGOS_FINALIZADO = { "F", "D", "R" };

	private static final String ERROR_VALIDAR_DATOS = "ERROR_VALIDAR_DATOS";
	private static final String ERROR_PAUSADO = "ERROR_PAUSADO";
	private static final String TIPO_OPERACION_INVALIDA = "TIPO_OPERACION_INVALIDA";
	private static final String NO_EXISTE_TARJETA_DEBITO = "NO_EXISTE_TARJETA_DEBITO";
	private static final String MENSAJE_DESARROLLADOR = "mensajeAlDesarrollador";
	private static final String URL_PAUSADO_DEBITO = "/v1/servicios/tarjetas/tarjetas-debito/estados";
	private static final String X_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String X_TIMESTAMP_HEADER = "X-Timestamp";
	private static final String NO_EXISTE_TARJETA_CREDITO = "NO_EXISTE_TARJETA_CREDITO";

	private static final String ERROR_ESTADO_TARJETA_PAUSADO = "ERROR_ESTADO_TARJETA_PAUSADO";
    private static final String ERROR_CVV_TARJETA = "ERROR_CVV_TARJETA";

	public static RespuestaMB consolidadaTarjetas(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		// int x=0;
		try {

			if (!ConfigMB.esProduccion() && contexto.idCobis().equals("8649931")) {
				return RespuestaMB.error();
			}

			Futuro<Boolean> futuroEnMora = new Futuro<>(() -> RestContexto.enMora(contexto));

			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				// x++;
				Objeto item = new Objeto();
				item.set("id", tarjetaDebito.id());
				item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
				item.set("limiteCompraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
				item.set("limiteExtraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));
				// emm-20190712-desde--> Necesito agregar el estado de la tarjeta
				String estadoTarjeta = "";
				ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, tarjetaDebito.numero());
				if (response.hayError()) {
					if (response.codigo == 500) {
						estadoTarjeta = "ERROR_LINK";
					} else {
						estadoTarjeta = "NO_DETERMINADO";
					}
				} else {
					estadoTarjeta = response.string("estadoTarjeta");
				}

				if (estadoTarjeta.toUpperCase().equals("CERRADA"))
					estadoTarjeta = "HABILITADA";

				if (estadoTarjeta == null || ((!"ERROR_LINK".equals(estadoTarjeta.toUpperCase()))
						&& (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
								&& !"INACTIVA".equals(estadoTarjeta.toUpperCase())))) {
					estadoTarjeta = "NO_DETERMINADO";
				}
				item.set("estado", estadoTarjeta);
				// emm-20190712-hasta
				if (tarjetaDebito.cuentasAsociadasPrincipales() != null) {
					item.set("estadoCuentasAsociadas", "ok");
					for (Cuenta cuenta : tarjetaDebito.cuentasAsociadasPrincipales()) {
						Objeto subitem = new Objeto();
						subitem.set("id", cuenta.id());
						subitem.set("descripcion", cuenta.producto());
						subitem.set("numero", cuenta.numero());
						subitem.set("ultimos4digitos", Formateador.ultimos4digitos(cuenta.numero()));
                        subitem.set("permiteCuotificacion", !RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false) && !futuroEnMora.get() && !contexto.persona().esEmpleado());
						item.add("cuentasAsociadas", subitem);
					}
				} else {
					item.set("estadoCuentasAsociadas", "error");
				}

				ApiResponseMB apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
				if (!apiResponseTitularidad.hayError()
						&& !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
						&& apiResponseTitularidad.objetos("collection1").size() != 0)
					item.set("pausada", apiResponseTitularidad.objetos("collection1").get(0).get("Pausada"));

				respuesta.add("tarjetasDebito", item);
			}

			Boolean permitirCambioFormaPago = false;
			for (Cuenta cuenta : contexto.cuentas()) {
				if (cuenta.esPesos()) {
					permitirCambioFormaPago = true;
				}
			}

			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
				item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
				item.set("fechaVencifechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
				item.set("formaPago", tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("permitirCambioFormaPago", permitirCambioFormaPago && tarjetaCredito.esTitular());
				respuesta.add("tarjetasCredito", item);
			}

			LocalTime target = LocalTime.now();
			Boolean enHorarioCambioFormaPago = (target.isAfter(LocalTime.parse("06:00:00"))
					&& target.isBefore(LocalTime.parse("21:00:00")));

			Objeto datosExtras = new Objeto();
			datosExtras.set("fueraHorarioCambioFormaPago", !enHorarioCambioFormaPago);
			datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
			datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
			datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
			datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebito().size() == 1);
			respuesta.set("datosExtras", datosExtras);

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB consolidadoMovimientosTarjeta(ContextoMB contexto,
			List<TarjetaCredito> listaTarjetasCredito, String tipoMoneda, boolean topeMovimientos) {

		int topeMov = topeMovimientos ? MBConsolidado.TOPE_MOVIMIENTOS : 0;
		RespuestaMB respuesta = new RespuestaMB();
		List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
		TarjetaCredito tarjetaCredito = listaTarjetasCredito.get(0);

		ApiResponseMB response = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
				tarjetaCredito.numero());
		if (!response.hayError() && !response.objetos("ultimosMovimientos.tarjetas").isEmpty()) {
			ultimosMovimientosTC.addAll(tomarNMovimientosXTarjeta(contexto, topeMov,
					response.objetos("ultimosMovimientos.tarjetas"), tarjetaCredito.cuenta(), tarjetaCredito.numero(),
					tipoMoneda, tarjetaCredito.fechaCierre("yyyy-MM-dd")));
		}

		if (tarjetaCredito.esTitular()) {
			ultimosMovimientosTC.addAll(movimientosLiquidados(contexto, tarjetaCredito, tipoMoneda, topeMov));
		}

		if (ultimosMovimientosTC.isEmpty()) {
			return RespuestaMB.estado("SIN_MOVIMIENTOS");
		}

		respuesta.set("ultimosMovimientosTC", ultimosMovimientosTC);

		return respuesta;
	}

	private static List<Objeto> movimientosLiquidados(ContextoMB contexto, TarjetaCredito tarjetaCredito,
			String tipoMoneda, int topeMovimientos) {
		List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
		LocalDate fechaHoy = LocalDate.now();
		String fechaCierre = tarjetaCredito.fechaCierre("yyyy-MM-dd");

		LocalDate fechaCierreDate = LocalDate.parse(fechaCierre);
		if (fechaCierreDate != null && fechaCierreDate.isAfter(fechaHoy)) {
			fechaCierre = fechaHoy.toString();
			fechaCierreDate = fechaHoy;
		}

		long dias = (30L - diasDespuesAFechaCierre(fechaCierre, fechaHoy));
		String fechaDesde = fechaCierreDate.minusDays(dias).toString();
		ApiResponseMB response = TarjetaCreditoService.movimientosLiquidados(contexto, tarjetaCredito.cuenta(),
				fechaDesde, fechaCierre);
		if (!response.hayError()) {
			ultimosMovimientosTC.addAll(tomarNMovimientosXTarjetaLiquidados(contexto, tarjetaCredito,
					response.objetos(), topeMovimientos, tipoMoneda));
		}

		return ultimosMovimientosTC;
	}

	private static Long diasDespuesAFechaCierre(String fechaCierre, LocalDate fechaHoy) {
		return ChronoUnit.DAYS.between(LocalDate.parse(fechaCierre), fechaHoy);
	}

	private static List<Objeto> tomarNMovimientosXTarjetaLiquidados(ContextoMB contexto, TarjetaCredito tarjeta,
			List<Objeto> movimientos, int tope, String tipoMoneda) {
		List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();
		Iterator<Objeto> itera = movimientos.iterator();

		int mov = 1;
		while (itera.hasNext()) {
			Objeto movimiento = itera.next();
			Objeto item = new Objeto();
			String simboloMoneda = "1".equals(movimiento.string("moneda")) ? "$" : "USD";
			String descripcion = movimiento.string("descripcionMovimiento").replaceAll(REGEX_ESPACIOS, " ");
			String moneda = "1".equals(movimiento.string("moneda")) ? "Pesos" : "Dólares";
			String numeroTarjeta = movimiento.string("tarjeta");

			if (consolidaXDescripcion(descripcion) || consolidaXOperacion(movimiento.string("codigoOperacion"))) {
				item.set("id", numeroTarjeta.replaceAll(REGEX_ESPACIOS, ""));
				item.set("tipo", "VISA");
				item.set("descripcionCorta", "Visa");
				item.set("numeroEnmascarado",
						"****" + StringUtils.substring(numeroTarjeta, numeroTarjeta.length() - 4));
				item.set("numero", verificaTarjeta(tarjeta.numero(), numeroTarjeta));
				item.set("fecha", Fecha.formato(movimiento.string("fechaMovimiento"), "yyyy-MM-dd", "dd/MM/yyyy"));
				item.set("descripcion", descripcion);
				BigDecimal importe = Formateador.importeNegativo(movimiento.bigDecimal("importe"));
				item.set("importeFormateado", Formateador.importe(importe));
				item.set("importe", importe);
				item.set("simboloMoneda", simboloMoneda);
				item.set("moneda", moneda);

				ultimosMovimientosTC = guardarItem(ultimosMovimientosTC, item, simboloMoneda, tipoMoneda);
			}

			mov++;
			if (tope != 0 && mov > tope) {
				break;
			}
		}
		return ultimosMovimientosTC;
	}

	private static List<Objeto> tomarNMovimientosXTarjeta(ContextoMB contexto, int tope, List<Objeto> datosTarjeta,
			String idCuenta, String idTarjeta, String tipoMoneda, String fechaCierre) {
		List<Objeto> ultimosMovimientosTC = new ArrayList<Objeto>();

		for (Objeto tarjeta : datosTarjeta) {
			int mov = 1;
			Iterator<Objeto> itera = tarjeta.objetos("movimientos").iterator();

			while (itera.hasNext()) {
				Objeto movimiento = itera.next();
				Objeto item = new Objeto();
				String monedaMovimiento = "pesos".equals(movimiento.string("descMoneda")) ? "$"
						: "dolares".equals(movimiento.string("descMoneda")) ? "USD" : null;
				String descripcion = movimiento.string("establecimiento.nombre").replaceAll(REGEX_ESPACIOS, " ");
				String numeroTarjeta = tarjeta.string("codigoTarjeta");

				if (consolidaXDescripcion(descripcion)) {
					item.set("id", numeroTarjeta);
					item.set("tipo", "VISA");
					item.set("idCuenta", idCuenta);
					item.set("descripcionCorta", "Visa");
					item.set("numeroEnmascarado",
							"****" + StringUtils.substring(numeroTarjeta, numeroTarjeta.length() - 4));
					item.set("numero", numeroTarjeta);
					item.set("fecha", validaFechaMovACuotas(movimiento.string("fecha"), fechaCierre, item));
					item.set("descripcion", descripcion);
					BigDecimal importe = Formateador.importeNegativo(movimiento.bigDecimal("importe.pesos"));
					item.set("importeFormateado", Formateador.importe(importe));
					item.set("importe", importe);
					if ("dolares".equals(movimiento.string("descMoneda"))) {
						importe = Formateador.importeNegativo(movimiento.bigDecimal("importe.dolares"));
						item.set("importeFormateado", Formateador.importe(importe));
						item.set("importe", importe);
					}
					item.set("simboloMoneda", monedaMovimiento);
					item.set("moneda", StringUtils.capitalize(movimiento.string("descMoneda")));

					Objeto datosAdicionales = new Objeto();
					datosAdicionales.set("establecimiento", movimiento.string("establecimiento.codigo"));
					datosAdicionales.set("ticket", movimiento.string("ticket"));
					datosAdicionales.set("idTarjeta", idTarjeta);
					if (tarjeta.string("codigoTarjeta").equals("0000000000000000")) {
						datosAdicionales.set("esCompra", false);
					} else {
						datosAdicionales.set("esCompra", true);
					}
					item.set("datosAdicionales", datosAdicionales);

					ultimosMovimientosTC = guardarItem(ultimosMovimientosTC, item, monedaMovimiento, tipoMoneda);
				}

				mov++;
				if (tope != 0 && mov > tope) {
					break;
				}
			}
		}
		return ultimosMovimientosTC;
	}

	private static boolean consolidaXDescripcion(String descripcion) {
		return descripcion.contains("SU PAGO") ? false : true;
	}

	private static boolean consolidaXOperacion(String operacion) {
		return StringUtils.contains(MBConsolidado.OPERACION_MOVIMIENTOS_TC, operacion) ? false : true;
	}

	private static String verificaTarjeta(String tarjetaTitular, String tarjetaMov) {
		return StringUtils.contains(tarjetaMov, "000000      0000") ? tarjetaTitular
				: tarjetaMov.replaceAll(REGEX_ESPACIOS, " ");
	}

	private static String validaFechaMovACuotas(String fechaMov, String fechaCierreTarjeta, Objeto item) {
		try {
			Date fechaCierreBase = new SimpleDateFormat("yyyy-MM-dd").parse(fechaCierreTarjeta);
			Date fechaMovBase = new SimpleDateFormat("dd/MM/yyyy").parse(fechaMov);
			if (fechaMovBase != null && fechaCierreBase != null
					&& (fechaMovBase.equals(fechaCierreBase) || fechaMovBase.before(fechaCierreBase))) {
				item.set("fechaModificada", true);
				return Fecha.sumarDias(fechaCierreBase, 1L, "dd/MM/yyyy");
			}
		} catch (ParseException e) {
			return fechaMov;
		}

		return fechaMov;
	}

	private static List<Objeto> guardarItem(List<Objeto> lista, Objeto item, String simboloMoneda, String tipoMoneda) {
		if (conFiltroMoneda(simboloMoneda, tipoMoneda)) {
			lista.add(item);
		} else if (sinFiltroMoneda(tipoMoneda)) {
			lista.add(item);
		}
		return lista;
	}

	private static boolean conFiltroMoneda(String simboloMoneda, String tipoMoneda) {
		return !tipoMoneda.isEmpty() && simboloMoneda.equalsIgnoreCase(tipoMoneda);
	}

	private static boolean sinFiltroMoneda(String tipoMoneda) {
		return tipoMoneda.isEmpty() || tipoMoneda == null;
	}

	public static RespuestaMB consultaAdicionalesPropias(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		boolean mostrarAdicionales = false;
		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
			if (!tarjetaCredito.esTitular()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
				item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
				item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
				item.set("formaPago", tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("nombre", tarjetaCredito.denominacionTarjeta());
				respuesta.add("tarjetasCredito", item);
				mostrarAdicionales = true;
			}
		}

		respuesta.set("mostrarAdicionales", mostrarAdicionales);

		return respuesta;
	}

	public static RespuestaMB resumenCuenta(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}

		ApiResponseMB response = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
				tarjetaCredito.numero());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		Objeto resumen = respuesta.set("resumen");

		Objeto itemResumenCuentaProxima = resumen.set("proximo");
		itemResumenCuentaProxima.set("fechaVencimiento", response.date(
				"resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
		itemResumenCuentaProxima.set("fechaCierre", response
				.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
		itemResumenCuentaProxima.set("saldoPesosFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.saldo.pesos"), ""));
		itemResumenCuentaProxima.set("saldoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.saldo.dolares"), ""));
		itemResumenCuentaProxima.set("pagoMinimoPesosFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.pagoMinimo.pesos"), ""));
		itemResumenCuentaProxima.set("pagoMinimoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.pagoMinimo.dolares"), ""));

		// TODO: validar si voy a parametria o sigo con datos de visa
		Objeto fechas = null;
		try {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
				Date fechaCierreProximo = Fecha
						.stringToDate(response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
								"yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
				Date fechaVencimientoProximo = Fecha.stringToDate(
						response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
								"yyyy-MM-dd", "dd/MM/yyyy", ""),
						"dd/MM/yyyy");
				Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
				if ((fechaActual.compareTo(fechaCierreProximo) > 0
						|| fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
					fechas = fechaCierreParametrica(
							Fecha.stringToDate(
									response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
											"yyyy-MM-dd", "dd/MM/yyyy", ""),
									"dd/MM/yyyy"),
							tarjetaCredito.grupoCarteraTc());
					if (fechas != null && !fechas.string("cierre").isEmpty()) {
						itemResumenCuentaProxima.set("fechaCierre",
								fechas.date("cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
						itemResumenCuentaProxima.set("fechaVencimiento",
								fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
					}
				}
			}
		} catch (Exception e) {
			//
		}

		Objeto itemResumenCuentaUltima = resumen.set("ultimo");

		try {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoVtoTcParametrica")) {
				Date fechaCierreProximo = Fecha.stringToDate(fechas.date("cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""),
						"dd/MM/yyyy");
				Date fechaVencimientoProximo = Fecha
						.stringToDate(fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
				Date fechaHoy = dateStringToDate(new Date(), "dd/MM/yyyy");

				if ((fechaHoy.compareTo(fechaCierreProximo) > 0
						|| fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
					itemResumenCuentaUltima.set("fechaVencimiento",
							fechas.date("vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
				} else {
					itemResumenCuentaUltima.set("fechaVencimiento",
							response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
									"yyyy-MM-dd", "dd/MM/yyyy", ""));
				}
			} else {
				Date fechaCierreProximo = Fecha
						.stringToDate(response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
								"yyyy-MM-dd", "dd/MM/yyyy", ""), "dd/MM/yyyy");
				Date fechaVencimientoProximo = Fecha.stringToDate(
						response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
								"yyyy-MM-dd", "dd/MM/yyyy", ""),
						"dd/MM/yyyy");
				Date fechaHoy = dateStringToDate(new Date(), "dd/MM/yyyy");
				if ((fechaHoy.compareTo(fechaCierreProximo) > 0
						|| fechaVencimientoProximo.compareTo(fechaCierreProximo) < 0)) {
					itemResumenCuentaUltima.set("fechaVencimiento",
							response.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
									"yyyy-MM-dd", "dd/MM/yyyy", ""));
				} else {
					itemResumenCuentaUltima.set("fechaVencimiento",
							response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
									"yyyy-MM-dd", "dd/MM/yyyy", ""));
				}
			}
		} catch (Exception e) {
			itemResumenCuentaUltima.set("fechaVencimiento",
					response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento", "yyyy-MM-dd",
							"dd/MM/yyyy", ""));
		}

		itemResumenCuentaUltima.set("fechaCierre", response
				.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
		itemResumenCuentaUltima.set("saldoPesosFormateado", Formateador
				.importe(response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.pesos"), ""));
		itemResumenCuentaUltima.set("saldoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.dolares"), ""));
		itemResumenCuentaUltima.set("pagoMinimoPesosFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos"), ""));
		itemResumenCuentaUltima.set("pagoMinimoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares"), ""));

		Objeto itemResumenCuentaAnterior = resumen.set("anterior");
		itemResumenCuentaAnterior.set("fechaVencimiento", response.date(
				"resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.vencimiento", "yyyy-MM-dd", "dd/MM/yyyy", ""));
		itemResumenCuentaAnterior.set("fechaCierre", response.date(
				"resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.cierre", "yyyy-MM-dd", "dd/MM/yyyy", ""));
		itemResumenCuentaAnterior.set("saldoPesosFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.saldo.pesos"), ""));
		itemResumenCuentaAnterior.set("saldoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.saldo.dolares"), ""));
		itemResumenCuentaAnterior.set("pagoMinimoPesosFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.pagoMinimo.pesos"), ""));
		itemResumenCuentaAnterior.set("pagoMinimoDolaresFormateado", Formateador.importe(
				response.bigDecimal("resumenCuenta.saldoenCuenta.anterior.liquidacionResumen.pagoMinimo.dolares"), ""));

		Objeto limites = respuesta.set("limites");
		for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.limites")) {
			limites.set("compra",
					objeto.string("descripcion").equals("compra") ? Formateador.importe(objeto.bigDecimal("total"))
							: limites.get("compra", ""));
			limites.set("compraCuotas",
					objeto.string("descripcion").equals("compracuotas")
							? Formateador.importe(objeto.bigDecimal("total"))
							: limites.get("compraCuotas", ""));
			limites.set("compraDisp",
					objeto.string("descripcion").equals("compradisp") ? Formateador.importe(objeto.bigDecimal("total"))
							: limites.get("compraDisp", ""));
			limites.set("compraCuotasDisp",
					objeto.string("descripcion").equals("compracuotasdisp")
							? Formateador.importe(objeto.bigDecimal("total"))
							: limites.get("compraCuotasDisp", ""));

			if (tarjetaCredito.tipo().equalsIgnoreCase("Signature")) {
				limites.set("financiacion",
						objeto.string("descripcion").equals("financiacion")
								? Formateador.importe(objeto.bigDecimal("total"))
								: limites.get("financiacion", ""));
			} else {
				limites.set("financiacion",
						objeto.string("descripcion").equals("financiacion")
								? Formateador.importe(objeto.bigDecimal("total"))
								: limites.get("compra", ""));
			}

		}

		respuesta.set("limitesUnificados",
				response.string("resumenCuenta.saldoenCuenta.limitesUnificados").equalsIgnoreCase("S") ? "true"
						: "false");

		Objeto tasas = respuesta.set("tasas");
		for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.tasas")) {
			tasas.set("tnaPesos",
					objeto.string("descripcion").equals("anual") ? Formateador.importe(objeto.bigDecimal("pesos"))
							: tasas.get("tnaPesos", ""));
			tasas.set("tnaDolares",
					objeto.string("descripcion").equals("anual") ? Formateador.importe(objeto.bigDecimal("dolares"))
							: tasas.get("tnaDolares", ""));
			tasas.set("teaPesos",
					objeto.string("descripcion").equals("mensual") ? Formateador.importe(objeto.bigDecimal("pesos"))
							: tasas.get("teaPesos", ""));
			tasas.set("teaDolares",
					objeto.string("descripcion").equals("mensual") ? Formateador.importe(objeto.bigDecimal("dolares"))
							: tasas.get("teaDolares", ""));
		}

		respuesta.set("stopDebit", "SI".equalsIgnoreCase(tarjetaCredito.stopDebit()));

		Objeto mora = new Objeto();
		try {

			String vtoUltima = response.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
					"yyyy-MM-dd", "dd/MM/yyyy", "");
			Date fechaVencimiento = Fecha.stringToDate(vtoUltima, "dd/MM/yyyy");
			Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
			RespuestaMB respuestaMora = MBTarjetas.estadoDeuda(contexto);
			Boolean onboardingMostrado = true;
			boolean enMora = false;
			if (respuestaMora.string("datos.estadoDeuda").equals("CON_DEUDA")) {
				if (respuestaMora.string("datos.tipoMora").equals("ONE")) {
					onboardingMostrado = false;
					enMora = true;
					String formaPago = tarjetaCredito.formaPago().equalsIgnoreCase("Efectivo") ? "EFECTIVO"
							: "AUTODEBITO";
					String tipoMora = respuestaMora.string("datos.tipoMora").equals("ONE")
							|| respuestaMora.string("datos.tipoMora").equals("MT") ? "T" : "";
					if (Util.esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
						mora.set("estadoTarjeta", ("EN_MORA" + tipoMora + "_" + formaPago));
					}
					ApiResponseMB responseMoraDetalle = RestMora.getProductosEnMoraDetallesCache(contexto,
							respuestaMora.string("datos.cta_id"));
					if (!responseMoraDetalle.hayError()) {
						ApiResponseMB detalleMora = RestMora.getProductosEnMoraDetalles(contexto,
								respuestaMora.string("datos.cta_id"));
						mora.set("saldoCubrirMinimo", detalleMora.objetos().get(0).string("Deuda Vencida"));
						mora.set("saldoCubrirTotal", detalleMora.objetos().get(0).bigDecimal("deudaAVencer")
								.add(detalleMora.objetos().get(0).bigDecimal("Deuda Vencida")));

						mora.set("estadoMora", Util.bucketMora(detalleMora.objetos().get(0).integer("DiasMora"), "TC"));
						mora.set("ctaId", detalleMora.objetos().get(0).integer("cta_id"));
						mora.set("promesaVigente", detalleMora.objetos().get(0).string("PromesaVigente"));
						mora.set("AvisoPago", detalleMora.objetos().get(0).string("IndicaYPAG"));
						mora.set("montoMinPromesa", detalleMora.objetos().get(0).string("montoMinPromesa"));
						mora.set("montoMaxPromesa", detalleMora.objetos().get(0).bigDecimal("deudaAVencer")
								.add(detalleMora.objetos().get(0).bigDecimal("Deuda Vencida")));
						mora.set("deudaAVencer", detalleMora.objetos().get(0).string("deudaAVencer"));
						mora.set("deudaVencida", detalleMora.objetos().get(0).string("Deuda Vencida"));
						mora.set("numeroProducto", respuestaMora.string("datos.numeroProducto").trim());
						onboardingMostrado = !detalleMora.objetos().get(0).string("PromesaVigente").isEmpty()
								|| !detalleMora.objetos().get(0).string("IndicaYPAG").isEmpty();
					} else {
						mora.set("estadoTarjeta", "ERROR_MORA");
					}
				}
			} else {
				mora.set("estadoTarjeta", "SIN_MORA");
			}
			Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
			mora.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

			try {
				if (!muestreoOnboarding && onboardingMostrado && enMora) {
					contexto.parametros.set("nemonico", "ONBOARDING");
					Util.contador(contexto);
				}
			} catch (Exception e) {
			}

			respuesta.set("mora", mora);
		} catch (Exception e) {
		}

		return respuesta;
	}

	public static RespuestaMB limitesTarjetaCredito(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		final String defaultMonto = "0.00";
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}
		RespuestaMB respuesta = new RespuestaMB();
//		if (ApiAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTarjetaCredito)) {
        try {
            ApiResponseMB response = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                    tarjetaCredito.numero());
            String mostrarTasa = ConfigMB.string("tarjeta_credito_mostrar_tasa");
            if (response.hayError()) {
                respuesta = obtenerRespuestaError(response);
            } else {
                Objeto limites = new Objeto();
                respuesta.set("limites", limites);
                for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.limites")) {
                    limites.set("compraCuotas",
                            objeto.string("descripcion").equals("compracuotas")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("compraCuotas", defaultMonto));
                    limites.set("compraDisp",
                            objeto.string("descripcion").equals("compradisp")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("compraDisp", defaultMonto));
                    limites.set("compraDispSinOverLimit",
                            objeto.string("descripcion").equals("tope")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("compraDispSinOverLimit", defaultMonto));
                    limites.set("compraCuotasDisp",
                            objeto.string("descripcion").equals("compracuotasdisp")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("compraCuotasDisp", defaultMonto));
                    limites.set("compraAcord",
                            objeto.string("descripcion").equals("compra")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("compraAcord", defaultMonto));

                    if (tarjetaCredito.tipo().equalsIgnoreCase("Signature")) {
                        limites.set("financiacionAcord",
                                objeto.string("descripcion").equals("financiacion")
                                        ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                        : limites.get("financiacionAcord", defaultMonto));
                    } else {
                        limites.set("financiacionAcord",
                                objeto.string("descripcion").equals("financiacion")
                                        ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                        : limites.get("compraAcord", defaultMonto));
                    }

                    limites.set("cuotaMaxMensual",
                            objeto.string("descripcion").equals("cuotamaxmensual")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("cuotaMaxMensual", defaultMonto));
                    limites.set("adelantos",
                            objeto.string("descripcion").equals("adelantos")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("adelantos", defaultMonto));
                    limites.set("prestamo",
                            objeto.string("descripcion").equals("prestamo")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("total"))
                                    : limites.get("prestamo", defaultMonto));
                }
                for (Objeto objeto : response.objetos("resumenCuenta.saldoenCuenta.tasas")) {
                    limites.set("tasaAnualPeso",
                            objeto.string("descripcion").equals("anual")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("pesos"))
                                    : limites.get("tasaAnualPeso", defaultMonto));
                    limites.set("tasaAnualDolar",
                            objeto.string("descripcion").equals("anual")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("dolares"))
                                    : limites.get("tasaAnualDolar", defaultMonto));
                    limites.set("tasaMensualPeso",
                            objeto.string("descripcion").equals("mensual")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("pesos"))
                                    : limites.get("tasaMensualPeso", defaultMonto));
                    limites.set("tasaMensualDolar",
                            objeto.string("descripcion").equals("mensual")
                                    ? Formateador.importeUsPeso(objeto.bigDecimal("dolares"))
                                    : limites.get("tasaMensualDolar", defaultMonto));
                }
                limites.set("mostrarTasa", mostrarTasa);
                limites.set("subtipo", response.string("resumenCuenta.datos.subtipo"));
                limites.set("limiteUnificado", response.string("resumenCuenta.saldoenCuenta.limitesUnificados"));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
//		} else {
//			respuesta.set("error", "Funcionalidad no habilitada");
//		}
		return respuesta;
	}

	public static RespuestaMB autorizaciones(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}
		List<TarjetaCredito> tarjetasComparar = contexto.tarjetasCredito();

		ApiResponseMB response = TarjetaCreditoService.autorizaciones(contexto, tarjetaCredito.cuenta(),
				tarjetaCredito.numero());
		if (response.hayError()) {
			if (response.string("codigo").equals("404")) {
				return RespuestaMB.estado("SIN_AUTORIZACIONES");
			}
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		Objeto autorizaciones = respuesta.set("autorizaciones");

		autorizaciones.set("totalPesos", Formateador.importe(response.bigDecimal("autorizaciones.totales.pesos")));
		autorizaciones.set("totalDolares", Formateador.importe(response.bigDecimal("autorizaciones.totales.dolares")));

		for (Objeto tarj : response.objetos("autorizaciones.tarjeta")) {
			Objeto tarjR = new Objeto();

			boolean esTitular = false;
			String idTipo = "";
			String tipo = "";

			for (TarjetaCredito tarjetaC : tarjetasComparar) {
				if (Formateador.ultimos4digitos(tarjetaC.numero())
						.equalsIgnoreCase(Formateador.ultimos4digitos(tarj.string("codigoTarjeta")))) {
					esTitular = tarjetaC.esTitular();
					idTipo = tarjetaC.idTipo();
					tipo = tarjetaC.tipo();
				}
			}
			tarjR.set("idTipo", idTipo);
			tarjR.set("tipo", tipo);
			tarjR.set("esTitular", esTitular);
			tarjR.set("ultimos4digitos", Formateador.ultimos4digitos(tarj.string("codigoTarjeta")));
			tarjR.set("proveedor", "VISA");

			for (Objeto objeto : tarj.objetos("autorizaciones")) {
				Objeto subItem = new Objeto();
				// subItem.set("fecha", objeto.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
				subItem.set("fecha", objeto.string("fecha"));
				subItem.set("establecimiento", objeto.string("establecimiento.nombre"));
				subItem.set("tipoMovimiento", objeto.string("tipo"));
				subItem.set("tipoMovimientoDescripcion", objeto.string("descripcion"));
				subItem.set("importeFormateado", Formateador.importe(objeto.bigDecimal("importe")));
				subItem.set("idMoneda", "pesos".equals(objeto.string("descMoneda")) ? "80"
						: "dolares".equals(objeto.string("descMoneda")) ? "2" : null);
				subItem.set("simboloMoneda", "pesos".equals(objeto.string("descMoneda")) ? "$"
						: "dolares".equals(objeto.string("descMoneda")) ? "USD" : null);
				tarjR.add("movimientos", subItem);
			}

			autorizaciones.add("tarjetas", tarjR);
		}

		return respuesta;
	}

	public static RespuestaMB cuotasPendientes(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}

		List<TarjetaCredito> tarjetasComparar = contexto.tarjetasCredito();

		ApiResponseMB response = TarjetaCreditoService.cuotasPendientes(contexto, tarjetaCredito.cuenta(),
				tarjetaCredito.numero());
		if (response.hayError()) {
			if (response.string("codigo").equals("404")) {
				return RespuestaMB.estado("SIN_CUOTAS_PENDIENTES");
			}
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		Objeto cuotasPendientes = respuesta.set("cuotasPendientes");

		cuotasPendientes.set("totalPesos", Formateador.importe(response.bigDecimal("cuotasPendientes.totales.pesos")));
		cuotasPendientes.set("totalDolares",
				Formateador.importe(response.bigDecimal("cuotasPendientes.totales.dolares")));
		for (Objeto tarj : response.objetos("cuotasPendientes.tarjeta")) {

			Objeto tarjR = new Objeto();

			boolean esTitular = false;
			String idTipo = "";
			String tipo = "";

			for (TarjetaCredito tarjetaC : tarjetasComparar) {
				if (Formateador.ultimos4digitos(tarjetaC.numero())
						.equalsIgnoreCase(Formateador.ultimos4digitos(tarj.string("codigoTarjeta")))) {
					esTitular = tarjetaC.esTitular();
					idTipo = tarjetaC.idTipo();
					tipo = tarjetaC.tipo();
				}
			}
			tarjR.set("idTipo", idTipo);
			tarjR.set("tipo", tipo);
			tarjR.set("esTitular", esTitular);
			tarjR.set("ultimos4digitos", Formateador.ultimos4digitos(tarj.string("codigoTarjeta")));
			tarjR.set("proveedor", "VISA");

			for (Objeto objeto : tarj.objetos("cuota")) {
				Objeto subItem = new Objeto();
				subItem.set("fecha", objeto.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
				subItem.set("cuotaActual", objeto.integer("cuotas") - objeto.integer("cantCuotasPendientes") + 1);
				subItem.set("cuotaMaxima", objeto.integer("cuotas"));

				subItem.set("importeFormateado", Formateador.importe(objeto.bigDecimal("importe")));
				subItem.set("idMoneda", "pesos".equals(objeto.string("descMoneda")) ? "80"
						: "dolares".equals(objeto.string("descMoneda")) ? "2" : null);
				subItem.set("simboloMoneda", "pesos".equals(objeto.string("descMoneda")) ? "$"
						: "dolares".equals(objeto.string("descMoneda")) ? "USD" : null);

				subItem.set("descripcion", objeto.string("establecimiento.nombre"));

				tarjR.add("cuotas", subItem);
			}
			cuotasPendientes.add("tarjetas", tarjR);
		}

		return respuesta;
	}

	public static RespuestaMB movimientos(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			tarjetaCredito = contexto.tarjetaCreditoTitular();
			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}
		}

		ApiResponseMB response = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
				tarjetaCredito.numero());
		if (response.hayError() && response.codigo != 404) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();

		if (contexto.tarjetaCreditoTitular() != null) {
			TarjetaCredito tarjeta = contexto.tarjetaCreditoTitular();
			Objeto item = new Objeto();
			item.set("id", "0");
			item.set("proveedor", "VISA");
			item.set("idTipo", tarjeta.idTipo());
			item.set("tipo", tarjeta.tipo());
			item.set("ultimos4digitos", tarjeta.ultimos4digitos());
			item.set("esTitular", tarjeta.esTitular());
			item.set("esTitularMasAdicionales", true);
			respuesta.add("tarjetas", item);
		}

		Set<String> ultimos4digitos = new HashSet<>();
		for (TarjetaCredito tarjeta : contexto.tarjetasCredito()) {
			Objeto item = new Objeto();
			item.set("id", tarjeta.idEncriptado());
			item.set("proveedor", "VISA");
			item.set("idTipo", tarjeta.idTipo());
			item.set("tipo", tarjeta.tipo());
			item.set("ultimos4digitos", tarjeta.ultimos4digitos());
			item.set("esTitular", tarjeta.esTitular());
			item.set("esAdicional", !tarjeta.esTitular());
			item.set("esTitularMasAdicionales", false);
			respuesta.add("tarjetas", item);
			ultimos4digitos.add(tarjeta.ultimos4digitos());
		}

		Boolean esTitularMasAdicionales = idTarjetaCredito.equals("0");

		// Logica solo para MB, el titular siempre muestra "titular + sus adicionales"
		// en los movimientos
		if (!esTitularMasAdicionales && tarjetaCredito.esTitular()) {
			esTitularMasAdicionales = true;
			idTarjetaCredito = "0";
		}

		Objeto cabecera = new Objeto();
		cabecera.set("id", !esTitularMasAdicionales ? tarjetaCredito.id() : "0");
		cabecera.set("cuenta", tarjetaCredito.cuenta());
		cabecera.set("tipoTarjeta", "VISA");
		cabecera.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
		cabecera.set("esTitular", tarjetaCredito.esTitular());
		cabecera.set("esTitularMasAdicionales", esTitularMasAdicionales);
		cabecera.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
		cabecera.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
		cabecera.set("formaPago",
				tarjetaCredito.formaPago().equalsIgnoreCase("EFECTIVO") ? "Manual" : tarjetaCredito.formaPago());
		cabecera.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
		cabecera.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));

		if (tarjetaCredito.esTitular()) {
			cabecera.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico());
		}

		if (response.codigo == 404) {
			cabecera.set("pesosFormateado", Formateador.importe(new BigDecimal(0)));
			cabecera.set("dolaresFormateado", Formateador.importe(new BigDecimal(0)));
		} else if (response != null && response.objetos("ultimosMovimientos").isEmpty()) {
			cabecera.set("pesosFormateado",
					Formateador.importe(response.bigDecimal("totalesUltimosMovimientos.pesos")));
			cabecera.set("dolaresFormateado",
					Formateador.importe(response.bigDecimal("totalesUltimosMovimientos.dolares")));
		} else {
			if (esTitularMasAdicionales) {
				cabecera.set("pesosFormateado",
						Formateador.importe(response.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.pesos")));
				cabecera.set("dolaresFormateado", Formateador
						.importe(response.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.dolares")));
			} else {
				for (Objeto subitem : response.objetos("ultimosMovimientos.tarjetas")) {
					if (!idTarjetaCredito.equals("0") && idTarjetaCredito.equals(subitem.string("codigoTarjeta"))) {
						cabecera.set("pesosFormateado", Formateador.importe(subitem.bigDecimal("pesos")));
						cabecera.set("dolaresFormateado", Formateador.importe(subitem.bigDecimal("dolares")));
					}
				}
			}
		}

		TarjetaCredito tarjetaCreditoTitularAux = contexto.tarjetaCreditoTitular();
		String vencimientoAux = "";
		String cierreAux = "";
		if (tarjetaCreditoTitularAux != null) {
			ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto,
					tarjetaCreditoTitularAux.cuenta(), tarjetaCreditoTitularAux.numero());
			vencimientoAux = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
					"yyyy-MM-dd", "dd/MM", "***");
			cierreAux = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre",
					"yyyy-MM-dd", "dd/MM", "***");
		}
		cabecera.set("vencimientoResumenFormateado", vencimientoAux);
		cabecera.set("cierreResumenFormateado", cierreAux);
		respuesta.set("cabecera", cabecera);

		if ((response != null && response.objetos("ultimosMovimientos").isEmpty()) || response.codigo == 404
				|| response.objetos("ultimosMovimientos.tarjetas").isEmpty()) {
			respuesta.set("movimientos", new ArrayList<Objeto>());
		} else {
			Objeto datos = response.objeto("ultimosMovimientos.datos");
			for (Objeto tarjeta : response.objetos("ultimosMovimientos.tarjetas")) {
				for (Objeto movimiento : tarjeta.objetos("movimientos")) {
					String establecimiento = movimiento.string("establecimiento.codigo");
					String descripcion = movimiento.string("establecimiento.nombre").replaceAll(REGEX_ESPACIOS, " ");

					Objeto item = new Objeto();
					item.set("fecha", movimiento.string("fecha"));
					item.set("establecimiento", establecimiento);
					item.set("descripcion", descripcion);
					item.set("ticket", movimiento.string("ticket"));

					if (!ultimos4digitos.contains(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")))) {
						ultimos4digitos.add(Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));

						Objeto itemAux = new Objeto();
						itemAux.set("id", tarjeta.string("codigoTarjeta"));

						itemAux.set("proveedor", "VISA");
						itemAux.set("idTipo", "");
						itemAux.set("tipo", "");
						itemAux.set("ultimos4digitos", Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
						itemAux.set("esTitular", false);
						itemAux.set("esTitularMasAdicionales", false);
						item.set("esAdicional", false);
						respuesta.add("tarjetas", itemAux);

					}
					item.set("id", tarjeta.string("codigoTarjeta"));
					item.set("ultimos4digitos", Formateador.ultimos4digitos(tarjeta.string("codigoTarjeta")));
					item.set("esTitular", contexto.tarjetaCreditoTitular() != null
							&& contexto.tarjetaCreditoTitular().numero().equals(tarjeta.string("codigoTarjeta")));
					item.set("nombreCompleto", datos.string("nombre") + " " + datos.string("apellido"));

					// si el codigoTarjeta viene en 0000000000000000 quiere decir q el mov no fue
					// una compra
					if (tarjeta.string("codigoTarjeta").equals("0000000000000000")) {
						item.set("esCompra", false);
					} else {
						item.set("esCompra", true);
					}

					item.set("simboloMoneda", "pesos".equals(movimiento.string("descMoneda")) ? "$"
							: "dolares".equals(movimiento.string("descMoneda")) ? "USD" : null);
					item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("importe.pesos")));
					if ("dolares".equals(movimiento.string("descMoneda"))) {
						item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("importe.dolares")));
					}
					item.set("orden", Long.MAX_VALUE - movimiento.date("fecha", "dd/MM/yyyy").getTime());

					if (idTarjetaCredito.equals("0") || idTarjetaCredito.equals(tarjeta.string("codigoTarjeta"))) {
						respuesta.add("movimientos", item);
					}
				}
			}
		}

		respuesta.objeto("movimientos").ordenar("orden");
		return respuesta.ordenar("estado", "cabecera", "tarjetas", "movimientos");
	}

	public static RespuestaMB detalleMovimientoComercio(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");
		String idCuenta = contexto.parametros.string("idCuenta");
		String idEstablecimiento = contexto.parametros.string("idEstablecimiento");

		// TODO REMOVER desde linea 638 a 652
		// Implementacion hardcore parcial hasta que la info de establecimientos este
		// disponible sin prisma
		ApiResponseMB response = null;
		if ("0".equals(idTarjetaCredito)) {
			idTarjetaCredito = "4304960036889822";
			idCuenta = "0522993383";
			if (filtrarEstablecimiento(idEstablecimiento)) {
				return RespuestaMB.estado("COMERCIO_NO_DISPONIBLE");
			}

			response = TarjetaCreditoService.detalleMovimientoComercio(contexto, idCuenta, idTarjetaCredito,
					idEstablecimiento);
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		} else {

			if (Objeto.anyEmpty(idTarjetaCredito, idCuenta, idEstablecimiento)) {
				return RespuestaMB.parametrosIncorrectos();
			}
			TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}

			String cuenta = tarjetaCredito.cuenta();
			if (tarjetaCredito == null || !cuenta.equalsIgnoreCase(idCuenta)) {
				return RespuestaMB.estado("CUENTA_NO_EXISTE");
			}

			if (filtrarEstablecimiento(idEstablecimiento)) {
				return RespuestaMB.estado("COMERCIO_NO_DISPONIBLE");
			}

			response = TarjetaCreditoService.detalleMovimientoComercio(contexto, tarjetaCredito.cuenta(),
					tarjetaCredito.numero(), idEstablecimiento);
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		}

		RespuestaMB respuesta = new RespuestaMB();
		Objeto comercio = response.objetos().get(0);
		if (comercio != null) {
			Objeto item = new Objeto();
			item.set("rubro", comercio.string("rubro"));
			String domicilio = comercio.string("domicilio").replaceAll(REGEX_ESPACIOS, " ");
			item.set("domicilio", domicilio);
			item.set("codigoPostal", comercio.string("codigoPostal"));
			item.set("localidad", comercio.string("localidad"));
			item.set("provincia", comercio.string("provincia"));
			item.set("telefono", comercio.string("telefono"));
			item.set("nombre", comercio.string("nombre"));
			item.set("cuit", comercio.string("cuit"));
			item.set("googleMapsDireccion", domicilio + "," + comercio.string("codigoPostal") + ","
					+ comercio.string("provincia") + "," + comercio.string("nombre"));
			respuesta.set("comercio", item);
		}

		return respuesta;
	}

	/* ========== transaccional ========== */
	public static RespuestaMB formasPago(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		Map<String, String> mapa = TarjetaCredito.formasPago();
		for (String clave : mapa.keySet()) {
			if (Objeto.setOf("01", "02", "03", "04", "05").contains(clave)) {
				respuesta.add("formasPago", new Objeto().set("id", clave).set("descripcion", mapa.get(clave)));
			}
		}
		return respuesta;
	}

	public static RespuestaMB cambioFormaPago(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
		String idFormaPago = contexto.parametros.string("idFormaPago");
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idTarjetaCredito, idFormaPago, idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		return new RespuestaMB();
	}

	public static RespuestaMB consultaEResumenDigital(ContextoMB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");
		if (Objeto.anyEmpty(idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		RespuestaMB respuesta = new RespuestaMB();

		ApiResponseMB response = TarjetaCreditoService.consultaEResumenDigital(contexto, idCuenta);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		respuesta.set("email", response.string("email"));
		respuesta.set("modeResumen", response.string("modoEresumen"));

		Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
		if (domicilioPostal != null) {
			String calle = domicilioPostal.string("calle");
			String altura = domicilioPostal.string("numero");
			respuesta.set("calle_alura", calle + " " + altura);

			String prov = RestCatalogo.nombreProvincia(contexto, domicilioPostal.integer("idProvincia", 1));
			String cp = domicilioPostal.string("idCodigoPostal");
			String ciudad = RestCatalogo.nombreLocalidad(contexto, domicilioPostal.integer("idProvincia", 1),
					domicilioPostal.integer("idCiudad", 146));
			respuesta.set("prov_cp_ciudad", prov + " (" + cp + ") " + ciudad);
		}

		return respuesta;

	}

	public static RespuestaMB adherirResumenDigital(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idTarjetaCredito)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("TARJETA_NO_ENCONTRADA");
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("success", false);
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTarjetaCredito)) {
			try {
				// contexto.parametros.add("email", contexto.persona().email());
				ApiResponseMB response = TarjetaCreditoService.altaEResumenDigital(contexto, idCuenta);
				if (response.hayError()) {
					respuesta.set("success", true);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			respuesta.set("error", "Funcionalidad no habilitada");
		}
		return respuesta;
	}

	public static RespuestaMB adherirResumenDigitalViejo(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idTarjetaCredito, idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("TARJETA_NO_ENCONTRADA");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("success", false);
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTarjetaCredito)) {
			try {
				// contexto.parametros.add("email", contexto.persona().email());
				ApiResponseMB response = TarjetaCreditoService.altaEResumenStopDebit(contexto);
				if (response.hayError()) {
					respuesta.set("success", true);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			respuesta.set("error", "Funcionalidad no habilitada");
		}
		// http://osbh-vs.bh.com.ar:80/CUE-0014/ActualizaMarcaResumenDigital

		return respuesta;
	}

	public static RespuestaMB solicitarTarjetaAdicional(ContextoMB contexto) {
		String idPaisNacimiento = contexto.parametros.string("idPaisNacimiento");
		String idCiudadNacimiento = contexto.parametros.string("idCiudadNacimiento");
		String idNacionalidad = contexto.parametros.string("idNacionalidad");
		String fechaNacimiento = contexto.parametros.string("fechaNacimiento");
		String porcentajeLimite = contexto.parametros.string("porcentajeLimite");
		String email = contexto.parametros.string("email");
//		String fechaIngresoPais = contexto.parametros.string("fechaIngresoPais", null);

        if (Objeto.anyEmpty(idPaisNacimiento, idCiudadNacimiento, idNacionalidad, fechaNacimiento, porcentajeLimite,
                email)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        return new RespuestaMB();
    }

    public static RespuestaMB infoActualizada(ContextoMB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        BigDecimal montoTotalDolares = null;
        BigDecimal montoTotalPesos = null;

        ApiResponseMB responseMovimientos = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (!responseMovimientos.hayError()) {
            montoTotalDolares = responseMovimientos.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.dolares");
            montoTotalPesos = responseMovimientos.bigDecimal("ultimosMovimientos.totalesUltimosMovimientos.pesos");
            if (montoTotalDolares == null)
                montoTotalDolares = new BigDecimal(0);
            if (montoTotalPesos == null)
                montoTotalPesos = new BigDecimal(0);
        }

        String totalDolaresFormateado = Formateador.importe(montoTotalDolares);
        String totalPesosFormateado = Formateador.importe(montoTotalPesos);
        if (montoTotalDolares == null) {
            totalDolaresFormateado = "***";
        }
        if (montoTotalPesos == null) {
            totalPesosFormateado = "***";
        }

        String vencimiento;
        String cierre;
        ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());

        if (responseResumen.hayError()) {
            return RespuestaMB.error();
        }

        if (responseResumen.codigo == 204) {
            return RespuestaMB.estado("SIN_RESUMEN");
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
                "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas

            Objeto fechaTC = fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, responseResumen);
            vencimiento = fechaTC.string("vencimiento");
            cierre = fechaTC.string("cierre");

        } else {
            vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
                    "yyyy-MM-dd", "dd/MM/yyyy", "");
            cierre = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                    "dd/MM/yyyy", "");
        }
        RespuestaMB respuesta = new RespuestaMB(); // si es 200 mandar un cero en consumos.

        if (!respuesta.hayError()) {
            if (totalPesosFormateado == null || totalPesosFormateado.isEmpty()) {
                totalPesosFormateado = "0,00";
            }
            if (totalDolaresFormateado == null || totalDolaresFormateado.isEmpty()) {
                totalDolaresFormateado = "0,00";
            }
        }
        respuesta.set("totalPesos", totalPesosFormateado); // emm-20190417
        respuesta.set("totalDolares", totalDolaresFormateado); // emm-20190417
        respuesta.set("cierre", cierre);
        respuesta.set("vencimiento", vencimiento);

        return respuesta;
    }

    private static Date dateStringToDate(Date fecha, String formato) throws ParseException {
        String fechaActualString = new SimpleDateFormat(formato).format(fecha);
        return new SimpleDateFormat(formato).parse(fechaActualString);
    }

    private static boolean esFechaActualSuperiorVencimiento(Date fechaPosteriorCierre, Date fechaActual) {
        return fechaActual.compareTo(fechaPosteriorCierre) == 1;
    }

    protected static Boolean validarResumenCuenta(ApiResponseMB responseResumen) {

        BigDecimal montoPagoMinimoPesos = responseResumen
                .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos");
        BigDecimal montoPagoMinimoDolares = responseResumen
                .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares");

        if (montoPagoMinimoPesos == null || montoPagoMinimoDolares == null) {
            return false;
        }

        return true;
    }

    public static RespuestaMB datosParaPagar(ContextoMB contexto) {

        // https://api-tarjetascredito-microservicios-desa.appd.bh.com.ar/v1/tarjetascredito/4304970008692328

        RespuestaMB respuesta = new RespuestaMB();
        BigDecimal dolarVenta = Cotizacion.dolarVenta(contexto);
        if (dolarVenta == null) {
            respuesta.setEstado("NO EXISTE MONEDA");
            return respuesta;
        }
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        BigDecimal montoPagoTotalPesos = new BigDecimal(0);
        BigDecimal montoPagoTotalDolares = new BigDecimal(0);
        BigDecimal montoPagoMinimoPesos = new BigDecimal(0);
        BigDecimal montoPagoMinimoDolares = new BigDecimal(0);
        BigDecimal sumaTotalDolaresMasPesosEnPesos = new BigDecimal(0);
        BigDecimal sumaTotalDolaresMasPesosEnDolar = new BigDecimal(0);
        boolean esPagableUS = false;
        String vencimiento = "";
        String cierre = "";

        /* Primero tomo los datos de la consulta de tarjeta */

        ApiResponseMB responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return RespuestaMB.error();
        }
        montoPagoTotalPesos = new BigDecimal(0);
        montoPagoTotalDolares = new BigDecimal(0);
        montoPagoMinimoPesos = new BigDecimal(0);
        vencimiento = "";
        cierre = "";
        for (Objeto item : responseTarjeta.objetos()) {
            montoPagoTotalPesos = item.bigDecimal("debitosEnCursoPesos");
            montoPagoTotalDolares = item.bigDecimal("debitosEnCursoDolares");
            montoPagoMinimoPesos = item.bigDecimal("pagoMinimoActual");
            vencimiento = item.date("fechaVencActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierre = item.date("cierreActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
        }
        esPagableUS = tarjetaCredito.esPagableUS();

        if (montoPagoTotalPesos == null)
            montoPagoTotalPesos = new BigDecimal(0);
        if (montoPagoTotalDolares == null)
            montoPagoTotalDolares = new BigDecimal(0);
        if (montoPagoMinimoPesos == null)
            montoPagoMinimoPesos = new BigDecimal(0);

        montoPagoMinimoDolares = montoPagoMinimoPesos.divide(dolarVenta, 2, RoundingMode.UP);
        sumaTotalDolaresMasPesosEnPesos = montoPagoTotalPesos.add(montoPagoTotalDolares.multiply(dolarVenta));
        sumaTotalDolaresMasPesosEnDolar = montoPagoTotalPesos.divide(dolarVenta, 2, RoundingMode.UP)
                .add(montoPagoTotalDolares);

        sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.setScale(2, RoundingMode.UP);
        sumaTotalDolaresMasPesosEnDolar = sumaTotalDolaresMasPesosEnDolar.setScale(2, RoundingMode.UP);

        /*
         * Me fijo, si funciona correctamente el servicio de resumenCuenta, tomo los
         * datos de ahí
         */
        ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
                tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return RespuestaMB.error();
        }

        if (validarResumenCuenta(responseResumen)) {
            montoPagoTotalPesos = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.pesos");
            montoPagoTotalDolares = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.saldo.dolares");
            montoPagoMinimoPesos = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.pesos");
            montoPagoMinimoDolares = responseResumen
                    .bigDecimal("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.pagoMinimo.dolares");

            if (montoPagoTotalPesos == null)
                montoPagoTotalPesos = new BigDecimal(0);
            if (montoPagoTotalDolares == null)
                montoPagoTotalDolares = new BigDecimal(0);
            if (montoPagoMinimoPesos == null)
                montoPagoMinimoPesos = new BigDecimal(0);
            if (montoPagoMinimoDolares == null)
                montoPagoMinimoDolares = new BigDecimal(0);

            sumaTotalDolaresMasPesosEnPesos = montoPagoTotalPesos.add(montoPagoTotalDolares.multiply(dolarVenta));
            sumaTotalDolaresMasPesosEnDolar = montoPagoTotalPesos.divide(dolarVenta, 2, RoundingMode.UP)
                    .add(montoPagoTotalDolares);

            sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.setScale(2, RoundingMode.UP);
            sumaTotalDolaresMasPesosEnDolar = sumaTotalDolaresMasPesosEnDolar.setScale(2, RoundingMode.UP);

            // vencimiento =
            // responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento",
            // "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierre = responseResumen.date("resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
                    "dd/MM/yyyy", "***");
        }

        Objeto datos = new Objeto();
        datos.set("cierre", cierre);
        datos.set("vencimiento", vencimiento);
        datos.set("totalPesos", montoPagoTotalPesos);
        datos.set("totalPesosFormateado", Formateador.importe(montoPagoTotalPesos));
        datos.set("totalDolares", montoPagoTotalDolares);
        datos.set("totalDolaresFormateado", Formateador.importe(montoPagoTotalDolares));
        datos.set("minimoPesos", montoPagoMinimoPesos);
        datos.set("minimoPesosFormateado", Formateador.importe(montoPagoMinimoPesos));
        datos.set("minimoDolares", montoPagoMinimoDolares);
        datos.set("minimoDolaresFormateado", Formateador.importe(montoPagoMinimoDolares));
        datos.set("sumaTotalDolaresMasPesosEnPesos", sumaTotalDolaresMasPesosEnPesos); // totalDolaresMasPesos
        datos.set("sumaTotalDolaresMasPesosEnPesosFormateado", Formateador.importe(sumaTotalDolaresMasPesosEnPesos));
        datos.set("sumaTotalDolaresMasPesosEnDolar", sumaTotalDolaresMasPesosEnDolar); // totalDolaresMasPesos
        datos.set("sumaTotalDolaresMasPesosEnDolarFormateado", Formateador.importe(sumaTotalDolaresMasPesosEnDolar));
        datos.set("cotizacionActual", dolarVenta);
        datos.set("cotizacionActualFormateado", Formateador.importe(dolarVenta));
        datos.set("esPagableUS", esPagableUS);

        datos.set("totalDolaresEnPesos", montoPagoTotalDolares.multiply(dolarVenta));
        datos.set("totalDolaresEnPesosFormateado", Formateador.importe(montoPagoTotalDolares.multiply(dolarVenta)));
        datos.set("totalPesosEnDolar", montoPagoTotalPesos.divide(dolarVenta, 2, RoundingMode.UP));
        datos.set("totalPesosEnDolarFormateado",
                Formateador.importe(montoPagoTotalPesos.divide(dolarVenta, 2, RoundingMode.UP)));

        boolean mostrarOpcionTotalPesos = sumaTotalDolaresMasPesosEnPesos.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotalDolares = sumaTotalDolaresMasPesosEnDolar.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotalPesosMasDolares = (montoPagoTotalPesos.compareTo(BigDecimal.ZERO) >= 0
                && montoPagoTotalDolares.compareTo(BigDecimal.ZERO) >= 0);
        boolean mostrarOpcionMinimo = montoPagoMinimoPesos.compareTo(BigDecimal.ZERO) >= 0;
        boolean mostrarOpcionTotal = (mostrarOpcionTotalPesos || mostrarOpcionTotalDolares);

        datos.set("mostrarOpcionTotalPesos", mostrarOpcionTotalPesos);
        datos.set("mostrarOpcionTotalDolares", mostrarOpcionTotalDolares);
        datos.set("mostrarOpcionMinimo", mostrarOpcionMinimo);
        datos.set("mostrarOpcionTotal", mostrarOpcionTotal);
        datos.set("mostrarOpcionTotalPesosMasDolares", mostrarOpcionTotalPesosMasDolares);

        ApiResponseMB response = ProductosService.productos(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        Boolean tieneCuentasPesos = false;
        Boolean tieneCuentasDolares = false;

        for (Cuenta cuenta : contexto.cuentas()) {
            if (cuenta.esPesos()) {
                tieneCuentasPesos = true;
            }

            if (cuenta.esDolares()) {
                tieneCuentasDolares = true;
            }
        }
        datos.set("tieneCuentasPesos", tieneCuentasPesos);
        datos.set("tieneCuentasDolares", tieneCuentasDolares);

        String footer = ConfigMB.string("creditcard_payment_footer", "");
        if (esPagableUS) {
            footer = "\n" + ConfigMB.string("creditcard_payment_dollar_footer", "");
        }
        datos.set("dolaresLegal", footer);

        respuesta.set("datos", datos);

        return respuesta;
    }

    public static RespuestaMB obtenerFechasTC(ContextoMB contexto) {

        RespuestaMB respuesta = new RespuestaMB();

        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(idTarjetaCredito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        String vencimientoActual = "";
        String cierreActual = "";
        String vencimientoProximo = "";
        String cierreProximo = "";
        String vencimientoAnterior = "";
        String cierreAnterior = "";

        /* Primero tomo los datos de la consulta de tarjeta */

        ApiResponseMB responseTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return RespuestaMB.error();
        }
        for (Objeto item : responseTarjeta.objetos()) {
            vencimientoActual = item.date("fechaVencActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreActual = item.date("cierreActual", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            vencimientoProximo = item.date("proxVenc", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreProximo = item.date("proxCierre", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            vencimientoAnterior = item.date("vencAnterior", "yyyy-MM-dd", "dd/MM/yyyy", "***");
            cierreAnterior = item.date("cierreAnterior", "yyyy-MM-dd", "dd/MM/yyyy", "***");

        }

        Objeto datos = new Objeto();
        datos.set("vencimientoActual", vencimientoActual);
        datos.set("cierreActual", cierreActual);
        datos.set("vencimientoProximo", vencimientoProximo);
        datos.set("cierreProximo", cierreProximo);
        datos.set("vencimientoAnterior", vencimientoAnterior);
        datos.set("cierreAnterior", cierreAnterior);

        respuesta.set("datos", datos);

        return respuesta;
    }

    public static RespuestaMB pagarTarjeta(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        importe = importe.setScale(2, RoundingMode.HALF_UP);

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (contexto.persona().esMenor()) {
            return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
        }

        if (importe.compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaMB.estado("PAGO_MENOR_A_CERO");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.error();
        }
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return RespuestaMB.error();
        }
        if (!tarjetaCredito.esTitular()) {
            return RespuestaMB.estado("PAGO_TARJETA_ADICIONAL_NO_PERMITIDO");
        }

        RespuestaMB respuesta = TarjetaCreditoService.pagarTarjetaCredito(contexto, cuenta, tarjetaCredito, importe);

        return respuesta;

    }
    // emm-20190423-hasta

    public static RespuestaMB programarPagoTarjeta(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");

        if (contexto.persona().esMenor()) {
            return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
        }

        if (importe.compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaMB.estado("PAGO_MENOR_A_CERO");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.error();
        }
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return RespuestaMB.error();
        }
        if (!tarjetaCredito.esTitular()) {
            return RespuestaMB.estado("PAGO_TARJETA_ADICIONAL_NO_PERMITIDO");
        }
        ApiResponseMB response = TarjetaCreditoService.programarPagoTarjetaCredito(contexto, cuenta, tarjetaCredito,
                importe);
        if (response == null || response.hayError()) {
            return RespuestaMB.error();
        }

        return RespuestaMB.exito();

    }

    public static RespuestaMB consultarPagosProgramadosTarjetaCredito(ContextoMB contexto) {
        String idTarjeta = contexto.parametros.string("idTarjetaCredito");

        if (Objeto.anyEmpty(idTarjeta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
        if (tarjetaCredito == null) {
            return RespuestaMB.error();
        }

        ApiResponseMB responseCatalogo = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCatalogo.hayError()) { // si dio error le devuelvo que está en horario para que desde front funcione
            // como siempre
            return RespuestaMB.error();
        }
        Date diaHabilPosterior = responseCatalogo.objetos().get(0).date("diaHabilPosterior", "yyyy-MM-dd");
        Calendar fechaProximoDiaHabil = Calendar.getInstance();
        Calendar fechaProximoDiaHabilSinHora = Calendar.getInstance();
        fechaProximoDiaHabil.setTime(diaHabilPosterior);
        fechaProximoDiaHabilSinHora.setTime(diaHabilPosterior);
        fechaProximoDiaHabilSinHora.set(Calendar.HOUR_OF_DAY, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.MINUTE, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.SECOND, 0);
        fechaProximoDiaHabilSinHora.set(Calendar.MILLISECOND, 0);

        ApiResponseMB response = RestScheduler.consultarTareas(contexto, "Por Procesar");
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Calendar hoy = Calendar.getInstance();
        Calendar hoySinHora = Calendar.getInstance();
        hoySinHora.set(Calendar.HOUR_OF_DAY, 0);
        hoySinHora.set(Calendar.MINUTE, 0);
        hoySinHora.set(Calendar.SECOND, 0);
        hoySinHora.set(Calendar.MILLISECOND, 0);

        Calendar manana = Calendar.getInstance();
        manana.add(Calendar.DAY_OF_YEAR, 1);

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("mostrarAvisoPagoProgramado", false);

        Objeto tareas = new Objeto();
        String horaAlertaHoy = "";
        String horaAlertaManana = "";
        Boolean alertaHoy = false;
        Boolean alertaManana = false;

        respuesta.set("existePagoProgramado", false);
        for (Objeto item : response.objetos("schedulers")) {
            if (item.string("tipoRequest").equals("json")) {
                ApiResponseMB responseItem = RestScheduler.consultarTareaEspecifica(contexto, item.string("id"));
                if (responseItem.string("url")
                        .equals(ConfigMB.string("api_url_orquestados") + "/api/tarjeta/pagoTarjeta")) {
                    // me fijo si es la cuenta y la tarjeta correspondiente
                    Objeto tarea = new Objeto();
                    Objeto request = Objeto.fromJson(responseItem.string("request"));
                    if (!request.string("numeroTarjeta").equals(tarjetaCredito.numero())) {
                        continue;
                    }
                    Calendar fechaProximaEjecucion = Calendar.getInstance();
                    Calendar fechaProximaEjecucionSinHora = Calendar.getInstance();

                    // me fijo si la fecha corresponde como para mostrar la leyenda
                    fechaProximaEjecucion
                            .setTime(responseItem.date("fechaHoraProximaEjecucion", "yyyy-MM-dd'T'HH:mm:ss"));
                    fechaProximaEjecucionSinHora
                            .setTime(responseItem.date("fechaHoraProximaEjecucion", "yyyy-MM-dd'T'HH:mm:ss"));
                    fechaProximaEjecucionSinHora.set(Calendar.HOUR_OF_DAY, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.MINUTE, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.SECOND, 0);
                    fechaProximaEjecucionSinHora.set(Calendar.MILLISECOND, 0);

                    if (/*
                     * fechaProgramada.get(Calendar.HOUR) != 7 ||
                     */fechaProximaEjecucion.get(Calendar.MINUTE) != 0
                            || fechaProximaEjecucion.get(Calendar.AM_PM) != Calendar.AM) {
                        continue;
                    }

                    if (fechaProximaEjecucion.compareTo(hoy) < 0)
                        continue;

                    Boolean agregarArray = false;

                    int diasDiferenciaHoy = (int) (fechaProximaEjecucionSinHora.getTime().getTime()
                            - hoySinHora.getTime().getTime()) / 86400000;
                    int diasDiferenciaDiaHabil = (int) (fechaProximoDiaHabilSinHora.getTime().getTime()
                            - fechaProximaEjecucionSinHora.getTime().getTime()) / 86400000;

                    if (diasDiferenciaHoy == 0) { // esta programada para hoy
                        respuesta.set("mostrarAvisoPagoProgramado", true);
                        alertaHoy = true;
                        agregarArray = true;
                        horaAlertaHoy = new SimpleDateFormat("hh:mm").format(fechaProximaEjecucion.getTime());
                        if (horaAlertaHoy.substring(3, 5).equals("00")) {
                            horaAlertaHoy = horaAlertaHoy.substring(0, 2);
                        }
                    } else {
                        if (diasDiferenciaHoy == 1) { // está programado para mañana
                            respuesta.set("mostrarAvisoPagoProgramado", true);
                            agregarArray = true;
                            alertaManana = true;
                            horaAlertaManana = new SimpleDateFormat("hh:mm").format(fechaProximaEjecucion.getTime());
                            if (horaAlertaManana.substring(3, 5).equals("00")) {
                                horaAlertaManana = horaAlertaManana.substring(0, 2);
                            }
                        } else {
                            // me tengo que fijar si está programada para el día habil, sino la ignoro
                            if (diasDiferenciaDiaHabil == 0) {
                                respuesta.set("mostrarAvisoPagoProgramado", true);
                                agregarArray = true;
                            } else {// no está programado para el día hábil, así que lo ignoro
                                continue;
                            }
                        }
                    }

                    if (agregarArray) {
                        tarea.set("numeroCuenta", request.string("cuenta"));
                        Cuenta cuenta = contexto.cuenta(request.string("cuenta"));
                        if (cuenta != null) {
                            Objeto cuentaTarea = new Objeto();
                            cuentaTarea.set("idCuenta", cuenta.id());
                            cuentaTarea.set("numeroCuenta", request.string("cuenta"));
                            cuentaTarea.set("descripcionCorta", cuenta.descripcionCorta());
                            cuentaTarea.set("simboloMoneda", cuenta.simboloMoneda());
                            cuentaTarea.set("numeroFormateado", cuenta.numeroFormateado());
                            cuentaTarea.set("saldo", cuenta.saldo());
                            cuentaTarea.set("saldoFormateado", cuenta.saldoFormateado());

                            tarea.set("cuenta", cuentaTarea);
                        }
                        tarea.set("importe", request.bigDecimal("importe"));
                        tarea.set("importeFormateado", Formateador.importe(request.bigDecimal("importe")));
                        tarea.set("idMoneda", request.bigDecimal("moneda"));
                        tarea.set("tipoTarjeta", request.string("tipoTarjeta"));
                        tarea.set("fechaProgramada",
                                new SimpleDateFormat("dd/MM/yyyy").format(fechaProximaEjecucion.getTime()));
                        String sHoraFechaProximaEjecucion = new SimpleDateFormat("hh:mm")
                                .format(fechaProximaEjecucion.getTime());

                        if (sHoraFechaProximaEjecucion.substring(3, 5).equals("00")) {
                            sHoraFechaProximaEjecucion = sHoraFechaProximaEjecucion.substring(0, 2);
                        }
                        tarea.set("fechaProgramadaHora", sHoraFechaProximaEjecucion);

                        tareas.add(tarea);
                        respuesta.set("existePagoProgramado", true);
                        respuesta.set("tareas", tareas);
                    }
                }
            }
        }
        respuesta.set("alertaHoy", alertaHoy);
        respuesta.set("horaAlertaHoy", horaAlertaHoy);
        respuesta.set("alertaManana", alertaManana);
        respuesta.set("horaAlertaManana", horaAlertaManana);

        return respuesta;

    }

    // emm-20190514-desde
    public static byte[] ultimaLiquidacion(ContextoMB contexto) {
		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return null;
        }

		if (ConfigMB.string("mb_apicanales_ultimaliquidacion", "false").equals("true")){
			ApiRequestMB request = ApiMB.request("CanalesUltimaLiquidacion", "canales", "POST", "/mb/api/ultima-liquidacion", contexto);
			request.body.set("idCobis", contexto.idCobis());

			ApiResponseMB response = ApiMB.response(request);
			if (response.hayError()) {
				String htmlError = "";
				contexto.setHeader("Location", "/hb/#/errorArchivoGenerado");
				contexto.setStatus(302);
				return htmlError.getBytes();
			}

			byte[] decodedString = Base64.getDecoder().decode(response.string("base64").getBytes());
        	contexto.setHeader("Content-Type", "application/pdf; name=ultima_liquidacion_" + tarjetaCredito.ultimos4digitos() + ".pdf");
        	return decodedString;
		}

        String cuit = contexto.persona().cuit();

        if (contexto.persona().esMenor()) {
            return null;
        }
        ApiResponseMB response = TarjetaCreditoService.ultimaLiquidacion(contexto, tarjetaCredito.numero(), cuit);
        if (response.hayError()) {
            String htmlError = "";
            contexto.setHeader("Location", "/hb/#/errorArchivoGenerado");
            contexto.setStatus(302);
            return htmlError.getBytes();
        }

        byte[] decodedString = Base64.getDecoder().decode(response.string("file").getBytes());
        contexto.setHeader("Content-Type", "application/pdf; name=ultima_liquidacion_" + tarjetaCredito.ultimos4digitos() + ".pdf");

        return decodedString;

    }
    // emm-20190514-hasta

    public static RespuestaMB blanquearPil(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.error();
        }

        RespuestaMB respuestaPausado = verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausado != null)
            return respuestaPausado;

        ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoBlanquearPil(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        ApiMB.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return RespuestaMB.exito();

    }

    public static RespuestaMB blanquearPin(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.error();
        }

        RespuestaMB respuestaPausado = verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausado != null)
            return respuestaPausado;

        ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoBlanquearPin(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        ApiMB.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return RespuestaMB.exito();

    }

    public static RespuestaMB tarjetaDebitoGetEstado(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.error();
        }
        String estadoTarjeta = "";
        ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            if (response.codigo == 500) {
                estadoTarjeta = "ERROR_LINK";
            } else {
                estadoTarjeta = "NO_DETERMINADO";
            }
        } else {
            estadoTarjeta = response.string("estadoTarjeta");
        }

        if (estadoTarjeta.toUpperCase().equals("CERRADA"))
            estadoTarjeta = "HABILITADA";

        if (estadoTarjeta == null || ((!"ERROR_LINK".equals(estadoTarjeta.toUpperCase()))
                && (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
                && !"INACTIVA".equals(estadoTarjeta.toUpperCase())))) {
            estadoTarjeta = "NO_DETERMINADO";
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.string("estadoTarjeta", estadoTarjeta);

        return RespuestaMB.exito();
    }

    public static RespuestaMB habilitarTarjetaDebito(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.error();
        }

        ApiResponseMB response = TarjetaDebitoService.habilitarTarjetaDebito(contexto, tarjetaDebito.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        ApiMB.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());

        return RespuestaMB.exito();
    }

    public static RespuestaMB limitesTarjetaDebito(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

        if (Objeto.anyEmpty(idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("TARJETA_NO_ENCONTRADA");
        }

        Objeto limites = new Objeto();
        limites.set("habilitadaLink", tarjetaDebito.habilitadaLink());
        limites.set("simboloMoneda", "$");

		if(MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_limite_extraccion_v2")){
			limites.add("opciones", new Objeto().set("id", "14").set("compraFormateado", "1.800.000")
					.set("extraccionFormateado", "300.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "15").set("compraFormateado", "3.600.000")
					.set("extraccionFormateado", "600.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "16").set("compraFormateado", "4.800.000")
					.set("extraccionFormateado", "800.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "17").set("compraFormateado", "6.000.000")
					.set("extraccionFormateado", "1.000.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "11").set("compraFormateado", "7.200.000")
					.set("extraccionFormateado", "1.200.000").set("actual", false));
		}
		else{
			limites.add("opciones", new Objeto().set("id", "12").set("compraFormateado", "60.000")
					.set("extraccionFormateado", "10.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "13").set("compraFormateado", "360.000")
					.set("extraccionFormateado", "60.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "14").set("compraFormateado", "900.000")
					.set("extraccionFormateado", "150.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "15").set("compraFormateado", "1.800.000")
					.set("extraccionFormateado", "300.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "07").set("compraFormateado", "2.400.000")
					.set("extraccionFormateado", "400.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "16").set("compraFormateado", "3.600.000")
					.set("extraccionFormateado", "600.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "17").set("compraFormateado", "4.800.000")
					.set("extraccionFormateado", "800.000").set("actual", false));
			limites.add("opciones", new Objeto().set("id", "11").set("compraFormateado", "6.000.000")
					.set("extraccionFormateado", "1.000.000").set("actual", false));
		}

        List<Objeto> opcionesList = limites.objetos("opciones");
        String limiteActualFormateado = Formateador.importe(tarjetaDebito.limiteExtraccion2());
        String[] limiteActual = limiteActualFormateado.split(",");
        for (Objeto opcion : opcionesList) {
            if (opcion.get("extraccionFormateado").equals(limiteActual[0])) {
                opcion.set("actual", true);
                break;
            }
        }

        return RespuestaMB.exito("limites", limites);
    }

    public static RespuestaMB modificarLimiteTarjetaDebito(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String limiteExtraccion = contexto.parametros.string("limiteExtraccion");

        if (Objeto.anyEmpty(idTarjetaDebito, limiteExtraccion)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("TARJETA_NO_ENCONTRADA");
        }

        if (!tarjetaDebito.habilitadaLink()) {
            return RespuestaMB.estado("TARJETA_NO_HABILITADA");
        }

        ApiRequestMB request = ApiMB.request("ModificarLimiteTarjetaDebito", "tarjetasdebito", "PUT",
                "/v1/tarjetasdebito/{nrotarjeta}", contexto);
        request.path("nrotarjeta", tarjetaDebito.numero());
        request.query("idcliente", contexto.idCobis());
        request.query("limiteretiro", limiteExtraccion);
        request.query("tipotarjeta", tarjetaDebito.idTipoTarjeta());

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), tarjetaDebito.numero());
        if (response.hayError()) {
            if (response.string("codigo").equals("1831602")) {
                return RespuestaMB.estado("EXISTE_SOLICITUD");
            }
            if (response.string("codigo").equals("40003")) {
                return RespuestaMB.estado("FUERA_HORARIO");
            }
            return RespuestaMB.error();
        }
        ApiMB.eliminarCache(contexto, "TarjetaDebitoGetEstado", contexto.idCobis(), tarjetaDebito.numero());
        return RespuestaMB.exito();
    }

    public static RespuestaMB consolidadaFormaPagoTarjetaCredito(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return RespuestaMB.estado("SIN_TARJETA_CREDITO_TITULAR");
        }

        List<Cuenta> cuentas = contexto.cuentas();
//		if (cuentas.isEmpty()) {
//			return Respuesta.estado("SIN_CUENTAS");
//		}

		// Forma Pago
		String descripcion = "";
		String idFormaPago = tarjetaCredito.idFormaPago();
		descripcion = "01".equals(idFormaPago) ? "Pago manual" : descripcion;
		descripcion = "02".equals(idFormaPago) ? "Débito automatico del pago mínimo" : descripcion;
		descripcion = "03".equals(idFormaPago) ? "Débito automatico del pago total" : descripcion;
		descripcion = "04".equals(idFormaPago) ? "Débito automatico del pago mínimo" : descripcion;
		descripcion = "05".equals(idFormaPago) ? "Débito automatico del pago total" : descripcion;
		if (idFormaPago.equals("01")) {
			respuesta.set("idFormaPago", "3");
		}
		if (idFormaPago.equals("02") || idFormaPago.equals("04")) {
			respuesta.set("idFormaPago", "2");
		}
		if (idFormaPago.equals("03") || idFormaPago.equals("05")) {
			respuesta.set("idFormaPago", "1");
		}
		respuesta.set("descripcionFormaPago", descripcion);

		// Fuera Horario
		LocalTime target = LocalTime.now();
		Boolean enHorario = (target.isAfter(LocalTime.parse("06:00:00"))
				&& target.isBefore(LocalTime.parse("21:00:00")));
		respuesta.set("fueraHorario", !enHorario);

		// Formas de pago
		if (!(idFormaPago.equals("03") || idFormaPago.equals("05"))) {
			respuesta.add("formasPago",
					new Objeto().set("id", "1").set("descripcion", "Débito automatico del pago total"));
		}
		if (!(idFormaPago.equals("02") || idFormaPago.equals("04"))) {
			respuesta.add("formasPago",
					new Objeto().set("id", "2").set("descripcion", "Débito automatico del pago mínimo"));
		}
		if (!(idFormaPago.equals("01"))) {
			respuesta.add("formasPago", new Objeto().set("id", "3").set("descripcion", "Pago manual"));
		}

		// Cuentas
		for (Cuenta cuenta : cuentas) {
			if (cuenta.esDolares()) {
				continue;
			}
			Objeto item = new Objeto();
			item.set("id", cuenta.id());
			item.set("descripcionCorta", cuenta.descripcionCorta());
			item.set("idMoneda", cuenta.idMoneda());
			item.set("simboloMoneda", cuenta.simboloMoneda());
			item.set("ultimos4digitos", cuenta.ultimos4digitos());
			item.set("saldo", cuenta.saldo());
			item.set("saldoFormateado", cuenta.saldoFormateado());
			respuesta.add("cuentas", item);
		}

		return respuesta;
	}

	public static RespuestaMB cambiarFormaPagoTarjetaCredito(ContextoMB contexto) {
		String idFormaPago = contexto.parametros.string("idFormaPago");
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idFormaPago, idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("CUENTA_INEXISTENTE");
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("SIN_TARJETA_CREDITO_TITULAR");
		}

		String formaPago = idFormaPago.equals("3") ? "01" : "";
		if (formaPago.isEmpty()) {
			formaPago = idFormaPago.equals("1") && cuenta.esCajaAhorro() ? "05" : formaPago;
			formaPago = idFormaPago.equals("1") && cuenta.esCuentaCorriente() ? "03" : formaPago;
			formaPago = idFormaPago.equals("2") && cuenta.esCajaAhorro() ? "04" : formaPago;
			formaPago = idFormaPago.equals("2") && cuenta.esCuentaCorriente() ? "02" : formaPago;
		}

		String tipificacion = "SAV-TC";

		if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, null, true)) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}

		ApiResponseMB responseReclamo = RestPostventa.cambioFormaPagoTC(contexto, tipificacion, formaPago,
				tarjetaCredito, cuenta);

		if (responseReclamo == null || responseReclamo.hayError()) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}

		String numeroCaso = Util.getNumeroCaso(responseReclamo);

		if (numeroCaso.isEmpty()) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}

		ProductosService.eliminarCacheProductos(contexto);
		return RespuestaMB.exito().set("ticket", numeroCaso);

		/*
		 * ApiRequest request = Api.request("ModificarFormaPagoTarjetaCredito",
		 * "tarjetascredito", "PATCH", "/v1/cuentas/{cuentaVisa}/formapago", contexto);
		 * request.path("cuentaVisa", tarjetaCredito.cuenta());
		 * request.body.set("cuenta", tarjetaCredito.cuenta());
		 * request.body.set("nombreCliente", contexto.persona().nombres());
		 * request.body.set("apellidoCliente", contexto.persona().apellidos());
		 * request.body.set("codigoCliente", contexto.idCobis());
		 * request.body.set("numeroCuenta", new Long(cuenta.numero()));
		 * request.body.set("tipoCuenta", cuenta.idTipo()); request.body.set("idMoneda",
		 * 80); request.body.set("sucursal", new
		 * Integer(contexto.persona().sucursal())); request.body.set("ticket",
		 * request.idProceso()); request.body.set("formaPago", formaPago); ApiResponse
		 * response = Api.response(request, contexto.idCobis(),
		 * tarjetaCredito.cuenta()); if (response.hayError()) { if
		 * (response.string("codigo").equals("50050")) { return
		 * Respuesta.estado("SOLICITUD_EN_CURSO"); } if
		 * (response.string("codigo").equals("40003")) { return
		 * Respuesta.estado("FUERA_HORARIO"); } return Respuesta.error(); }
		 * 
		 * ProductosService.eliminarCacheProductos(contexto); return
		 * Respuesta.exito().set("ticket", response.string("ticketId"));
		 */

	}

	@SuppressWarnings("unchecked")
	public static RespuestaMB tarjetasCreditoPropias(ContextoMB contexto) {
		try {
			RespuestaMB respuesta = new RespuestaMB();
			List<String> docs = new ArrayList<>();
			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
				item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
				item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
				item.set("formaPago", tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("nombre", tarjetaCredito.denominacionTarjeta().trim());
				item.set("limite", tarjetaCredito.limiteCompra());
				item.set("limiteFormateado", Formateador.importe(tarjetaCredito.limiteCompra()));

				ApiResponseMB responsePorId = TarjetaCreditoService.consultaTarjetaCredito(contexto,
						tarjetaCredito.id());
				ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto,
						responsePorId.objetos().get(0).string("cuenta"), tarjetaCredito.id());

				item.set("documento", responseResumen.string("resumenCuenta.datos.documento"));
				docs.add(responseResumen.string("resumenCuenta.datos.documento"));

				if (tarjetaCredito.esTitular()) {
					respuesta.set("tarjetaCreditoTitular", item);
				} else {
					if (!responsePorId.objetos().get(0).bool("tarjetaHabilitada")
							&& responsePorId.objetos().get(0).string("tarjetaEstado").equals("20")) {
						item.set("nombre", tarjetaCredito.denominacionTarjeta().trim());
						item.set("documento", responseResumen.string("resumenCuenta.datos.documento"));
						item.set("fechaSolicitud", tarjetaCredito.fechaAlta("dd/MM/yyyy"));
						respuesta.add("tarjetasCreditoAdicionalesEnCurso", item);
					} else {
						respuesta.add("tarjetasCreditoAdicionales", item);
					}
				}
			}

			try {
				RespuestaMB solicitudesEstado = MBProcesos.estadoSolicitudes(contexto);
				if (!solicitudesEstado.hayError()) {
					List<Objeto> solicitudes = (List<Objeto>) solicitudesEstado.get("solicitudes");

					if (solicitudes == null || solicitudes.isEmpty()) {
						solicitudes = (List<Objeto>) solicitudesEstado.objetos("solicitudes");
					}
					Objeto relacion = new Objeto();
					for (Objeto soli : solicitudes) {
						if ("ADICIONALES DE TARJETA DE CRÉDITO VISA".equalsIgnoreCase(soli.string("producto"))) {
							if (!Arrays.asList(CODIGOS_FINALIZADO).contains(soli.string("estado"))
									|| chequeoNuevaSolicitud(contexto, soli)) {
								ApiResponseMB response = RestVenta.consultarSolicitudes(contexto,
										soli.string("idSolicitud"));
								for (Objeto integrante : response.objetos("Datos").get(0).objetos("Integrantes")) {
									if (response.objetos("Datos").get(0).objetos("Integrantes").size() >= 3) {
										relacion = RestPersona.getTipoRelacionPersona(contexto,
												integrante.string("NumeroDocumentoTributario"));
										if ("2".equals(relacion.string("idTipoRelacion"))) {
											docs.add(integrante.string("NumeroDocumento"));
										}
									}

									if (!docs.contains(integrante.string("NumeroDocumento"))) {
										Objeto item = new Objeto();
										item.set("nombre",
												integrante.string("Apellido") + " " + integrante.string("Nombres"));
										item.set("documento", integrante.string("NumeroDocumento"));
										item.set("fechaSolicitud", soli.get("dateDesc"));
										respuesta.add("tarjetasCreditoAdicionalesEnCurso", item);
										docs.add(integrante.string("NumeroDocumento"));
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB ofertaSolicitudTarjetaCreditoAdicional(ContextoMB contexto) {
		String idTarjetaCreditoAdicional = contexto.parametros.string("idTarjetaCreditoAdicional");

		TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("SIN_TARJETA_CREDITO_TITULAR");
		}

		TarjetaCredito tarjetaCreditoAdicional = contexto.tarjetaCredito(idTarjetaCreditoAdicional);

		Objeto titular = new Objeto();
		titular.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
		titular.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
		titular.set("limiteCompraUnPagoFormateado", Formateador.importe(tarjetaCredito.limiteCompra()));
		titular.set("limiteCompraCuotasFormateado", Formateador.importe(tarjetaCredito.limiteCompraCuotas()));

		Objeto ofertas = new Objeto();
		for (Integer valor : Objeto.listOf(100, 75, 50, 25, 15, 10)) {
			BigDecimal porcentaje = new BigDecimal(valor).divide(new BigDecimal(100));
			Objeto oferta = new Objeto();
			oferta.set("id", valor);
			oferta.set("limiteCompraUnPagoFormateado",
					Formateador.importe(tarjetaCredito.limiteCompra().multiply(porcentaje)));
			oferta.set("limiteCompraCuotasFormateado",
					Formateador.importe(tarjetaCredito.limiteCompraCuotas().multiply(porcentaje)));
			oferta.set("ofertaActual", false);

			if (tarjetaCreditoAdicional != null) {
				BigDecimal limiteTitular = tarjetaCredito.limiteCompra();
				BigDecimal limiteAdicional = tarjetaCreditoAdicional.limiteCompra();
				BigDecimal ratio = limiteAdicional.divide(limiteTitular, 2, RoundingMode.HALF_UP);

				BigDecimal distanciaAl100 = new BigDecimal("1.00").subtract(ratio).abs();
				BigDecimal distanciaAl75 = new BigDecimal("0.75").subtract(ratio).abs();
				BigDecimal distanciaAl50 = new BigDecimal("0.50").subtract(ratio).abs();
				BigDecimal distanciaAl25 = new BigDecimal("0.25").subtract(ratio).abs();
				BigDecimal distanciaAl15 = new BigDecimal("0.15").subtract(ratio).abs();
				BigDecimal distanciaAl10 = new BigDecimal("0.10").subtract(ratio).abs();

				Integer porcentajeInteger = 100;
				BigDecimal menorDistancia = distanciaAl100.min(distanciaAl75).min(distanciaAl50).min(distanciaAl25)
						.min(distanciaAl15).min(distanciaAl10);
				if (menorDistancia.equals(distanciaAl100)) {
					porcentajeInteger = 100;
				} else if (menorDistancia.equals(distanciaAl75)) {
					porcentajeInteger = 75;
				} else if (menorDistancia.equals(distanciaAl50)) {
					porcentajeInteger = 50;
				} else if (menorDistancia.equals(distanciaAl25)) {
					porcentajeInteger = 25;
				} else if (menorDistancia.equals(distanciaAl15)) {
					porcentajeInteger = 15;
				} else if (menorDistancia.equals(distanciaAl10)) {
					porcentajeInteger = 10;
				}

				if (porcentajeInteger.equals(valor)) {
					oferta.set("ofertaActual", true);
				}
			}
			ofertas.add(oferta);
		}

		return RespuestaMB.exito("titular", titular).set("ofertas", ofertas);
	}

	public static RespuestaMB crearSolicitudTarjetaCreditoAdicional(ContextoMB contexto) {
		Integer porcentaje = contexto.parametros.integer("porcentaje");
		String cuit = contexto.parametros.string("cuit", "");
		if(cuit.isEmpty()){
			return crearSolicitudTarjetaCreditoAdicionalV2(contexto);
		}
		String idSexo = contexto.parametros.string("idSexo", "");
		String nombre = contexto.parametros.string("nombre", "");
		String apellido = contexto.parametros.string("apellido", "");
		String tipoRelacion = contexto.parametros.string("tipoRelacion", "18");
		String email = contexto.parametros.string("email", "");
		String fechaNacimiento = contexto.parametros.string("fechaNacimiento", "");
		String codigoArea = contexto.parametros.string("codigoArea", "");
		String caracteristica = contexto.parametros.string("caracteristica", "");
		String numero = contexto.parametros.string("numero", "");

		if (Objeto.anyEmpty(porcentaje)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		if (!ConfigMB.esProduccion() && contexto.idCobis().equals("772245")) {
			return RespuestaMB.error();
		}

		TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();
		if (!tarjetaCreditoTitular.idEstado().equals("20")) {
			return RespuestaMB.estado("TARJETA_CON_PROBLEMAS");
		}

		if (tarjetaCreditoTitular.esHML()) {
			return RespuestaMB.estado("TARJETA_HML");
		}

		try {

			if (cuit.isEmpty()) {
				contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
				return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
			}

			String cobisRelacion = "";
			ApiMB.eliminarCache(contexto, "PersonasRelacionadas", cuit);
			ApiResponseMB responsePersonaEspecifica = RestPersona.consultarPersonaEspecifica(contexto, cuit);
			cobisRelacion = responsePersonaEspecifica.string("idCliente");

			if (!responsePersonaEspecifica.string("idSexo").equalsIgnoreCase(idSexo)
					|| cobisRelacion.isEmpty()) {
				contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
				return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
			}

			if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
					|| responsePersonaEspecifica.string("idVersionDocumento").isEmpty()
					|| responsePersonaEspecifica.string("idEstadoCivil").isEmpty()
					|| responsePersonaEspecifica.string("fechaNacimiento").isEmpty()
					|| responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {

				Objeto datos = new Objeto();

				if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
						|| responsePersonaEspecifica.string("idNacionalidad").isEmpty()) {
					datos.set("idPaisNacimiento", 80);
					datos.set("idNacionalidad", 80);
					datos.set("idPaisResidencia", "");
				}

				if (responsePersonaEspecifica.string("idVersionDocumento").isEmpty()) {
					datos.set("idVersionDocumento", "A");
				}

				if (responsePersonaEspecifica.string("idEstadoCivil").isEmpty()) {
					datos.set("idEstadoCivil", "S");
				}

				if (responsePersonaEspecifica.string("fechaNacimiento").isEmpty()) {
					fechaNacimiento = fechaNacimiento.replace("\\", "");
					fechaNacimiento = Fecha.formato(fechaNacimiento, "dd/MM/yyyy", "yyyy/MM/dd");
					fechaNacimiento = fechaNacimiento.replace("/", "-");
					datos.set("fechaNacimiento", fechaNacimiento + "T00:00:00");
				}

				if (responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {
					datos.set("idSituacionImpositiva", "CONF");
				}

				RestPersona.actualizarPersona(contexto, datos, cuit);
			}

			//TODO: Tomo los datos del telefono del Titular si vienen vacios
			// porque esto responsePersonaEspecifica.string("etagTelefonos") a veces viene distinto a -1
			// y el telefono esta mal cargado y falla en API-Ventas cuando va a buscar datos del telefono del Adicional
			if(codigoArea.isEmpty() && caracteristica.isEmpty() && numero.isEmpty()){
				Objeto telefonoTitular = RestPersona.celular(contexto, contexto.persona().cuit());
				codigoArea = telefonoTitular.string("codigoArea","");
				caracteristica = telefonoTitular.string("caracteristica","");
				numero = telefonoTitular.string("numero","");
				try{
					RestPersona.actualizarCelular(contexto, cuit, codigoArea, caracteristica, numero);
				}catch (Exception ignored){	}
			}

			//TODO: Tomo el mail del Titular si viene vacio el mail, porque pasa lo mismo que con el telefono
			if(email.isEmpty()){
				email = contexto.persona().email();
				try{
					RestPersona.actualizarEmail(contexto, cuit, email);
				}catch (Exception ignored){ }
			}

			if (responsePersonaEspecifica.string("etagDomicilios").equalsIgnoreCase("-1")) {
				Objeto domicilioTitularLegal = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
				RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularLegal, "LE");
				Objeto domicilioTitularPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
				RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularPostal, "DP");
			}

			String idEstadoCivil = contexto.persona().idEstadoCivil();
			Objeto relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);

			if (idEstadoCivil.equalsIgnoreCase("C") && !tipoRelacion.equals("2")) {
				if (relacion.string("idTipoRelacion").equals("2")) {
					tipoRelacion = "2";
				}
			}

			if (relacion.string("idTipoRelacion").isEmpty()) {
				try {
					ApiResponseMB responseGenerarRelacionPersona = RestPersona.generarRelacionPersona(contexto,
							tipoRelacion, cuit, cobisRelacion);
					if (responseGenerarRelacionPersona.hayError()) {
						return RespuestaMB.estado("ERROR_GENERAR_RELACION");
					}
					try {
						ApiMB.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
						relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
					} catch (Exception e) {
					}
				} catch (Exception e) {
				}
			} else if (!relacion.string("idTipoRelacion").equals(tipoRelacion)) {
				try {
					Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
					SimpleDateFormat destinoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					String fechaModificacion = destinoSDF.format(fechaActual);
					ApiResponseMB responseActualizarRelacionPersona = RestPersona.actualizarRelacionPersona(contexto,
							relacion.string("id"), tipoRelacion, cuit, cobisRelacion, null, null, fechaModificacion);

					if (responseActualizarRelacionPersona.hayError()) {
						return RespuestaMB.estado("ERROR_GENERAR_RELACION");
					}

					try {
						ApiMB.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
						relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
					} catch (Exception e) {
						//
					}
				} catch (Exception e) {
					//
				}
			}

			// Caso al adicional con el titular
			if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
				ApiRequestMB request = ApiMB.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
				request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
				request.path("id", cuit);
				request.body("idEstadoCivil", "C");
				request.body("idSubtipoEstadoCivil", "Y");

				ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
				if (response.hayError()) {
					return RespuestaMB.error();
				}
			}

			// Caso al titular con el adicional
			if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
				ApiRequestMB request = ApiMB.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
				request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
				request.path("id", contexto.persona().cuit());
				request.body("idEstadoCivil", "C");
				request.body("idSubtipoEstadoCivil", "Y");

				ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
				if (response.hayError()) {
					return RespuestaMB.error();
				}
			}
		} catch (Exception e) {
		}

		// Generar Solicitud
		ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
		if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarSolicitud.objetos("Errores").isEmpty()
							? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		String idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

		// Generar Integrante
		ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
		if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarIntegrante.objetos("Errores").isEmpty()
							? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud, cuit);
		if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarIntegrante.objetos("Errores").isEmpty()
							? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		// Generar Tarjeta Credito Adicional
		ApiResponseMB tarjetaCreditoAdicional = RestOmnicanalidad.generarTarjetaCreditoAdicional(contexto, idSolicitud,
				cuit);
		if (tarjetaCreditoAdicional.hayError() || !tarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!tarjetaCreditoAdicional.objetos("Errores").isEmpty()
							? tarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		String idTarjetaCreditoAdicional = tarjetaCreditoAdicional.objetos("Datos").get(0).string("Id");

		// Motor
		ApiResponseMB responseMotor = RestOmnicanalidad.evaluarSolicitud(contexto, idSolicitud);
		if (responseMotor.hayError() || !responseMotor.objetos("Errores").isEmpty()) {
			try {
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			} catch (Exception e) {
			}
			return RespuestaMB.error().set("error", responseMotor.objetos("Errores").get(0).string("MensajeCliente"));
		}

		String resolucionId = responseMotor.objetos("Datos").get(0).string("ResolucionId");
		if (!resolucionId.equals("AV")) {
			String explicacion = responseMotor.objetos("Datos").get(0).string("Explicacion");
			
			String estado = switch (resolucionId) {
                case "AA" -> "APROBADO_AMARILLO";
                case "CT", "RE" -> "ROJO";
                default -> "ERROR";
            };

            if (explicacion.contains("La edad es inferior a la mínima requerida")) {
				switch (tipoRelacion) {
				case "2": {
					estado = "EDAD_16";
					break;
				}
				case "18": {
					estado = "EDAD_18";
					break;
				}
				case "1", "4", "15": {
					estado = "EDAD_13";
					break;
				}
					default:
					break;
				}
			}
			try {
				contador(contexto, "ADCIONAL_TC_" + estado);
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			} catch (Exception ignored) {
			}

			return new RespuestaMB().setEstado(estado).set("error", explicacion);
		}

		// Embozado
		ApiResponseMB responsePersona = RestPersona.consultarPersonaEspecifica(contexto, cuit);
		if (responsePersona.hayError()) {
			return new RespuestaMB().setEstado("ERROR");
		}

		// Actualizar Tarjeta Credito Adicional
		ApiResponseMB actualizarTarjetaCreditoAdicional = RestOmnicanalidad.actualizarTarjetaCreditoAdicional(contexto,
				idSolicitud, idTarjetaCreditoAdicional, cuit, ContextoMB.embozado(responsePersona.string("nombres"), responsePersona.string("apellidos")), porcentaje);
		if (actualizarTarjetaCreditoAdicional.hayError()
				|| !actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()
							? actualizarTarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		// Finalizar
		ApiResponseMB response = RestOmnicanalidad.finalizarSolicitud(contexto, idSolicitud);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			String estado = "ERROR";
			if (!response.objetos("Errores").isEmpty()
					&& response.objetos("Errores").get(0).string("Codigo").equals("1831609")) {
				estado = "IR_A_SUCURSAL";
			}
			if (!response.objetos("Errores").isEmpty()
					&& response.objetos("Errores").get(0).string("Codigo").equals("1831602")) {
				estado = "EN_PROCESO_ACTUALIZACION";
			}
			return new RespuestaMB().setEstado(estado).set("error",
					!response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		try {
			ApiMB.eliminarCache(contexto, "EstadoSolicitudBPM", contexto.idCobis(), contexto.persona().numeroDocumento());
		} catch (Exception e) {
			//
		}
		contador(contexto, "ADCIONAL_TC_OK");
		return RespuestaMB.exito();
	}

	public static RespuestaMB cambioLimiteTarjetaCreditoAdicional(ContextoMB contexto) {
		String idTarjetaCreditoAdicional = contexto.parametros.string("idTarjetaCreditoAdicional");
		Integer porcentaje = contexto.parametros.integer("porcentaje");

		if (Objeto.anyEmpty(idTarjetaCreditoAdicional, porcentaje)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();
		if (tarjetaCreditoTitular == null) {
			return RespuestaMB.estado("SIN_TARJETA_TITULAR");
		}
		if (!tarjetaCreditoTitular.idEstado().equals("20")) {
			return RespuestaMB.estado("TARJETA_CON_PROBLEMAS");
		}

		TarjetaCredito tarjetaCreditoAdicional = contexto.tarjetaCredito(idTarjetaCreditoAdicional);
		if (tarjetaCreditoAdicional == null) {
			return RespuestaMB.estado("TARJETA_NO_ENCONTRADA");
		}

		ApiRequestMB request = ApiMB.request("ModificarLimiteTarjetaCreditoAdicional", "tarjetascredito", "POST",
				"/v1/modificacionPorcentajeAlta", contexto);
		request.body.set("adeLimiPorcen", porcentaje);
		request.body.set("compraLimiPorcen", porcentaje);
		request.body.set("cuenta", tarjetaCreditoTitular.cuenta());
		request.body.set("cuotasLimiPorcen", porcentaje);
		request.body.set("marcaCodi", "2");
		request.body.set("tarjeNume", tarjetaCreditoAdicional.numero());

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		if (response.string("codigo").equals("50021")) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}
		if (!response.string("codigo").equals("0")) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB horarioPagoTarjeta(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();

		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_pago_programado",
				"prendido_pago_programado_cobis")) {
			respuesta.set("enHorario", true);
			respuesta.set("alertaSeHaceHoy", false);
			return respuesta;
		}

		LocalTime target = LocalTime.now();
		Boolean enHorario = (target.isAfter(LocalTime.parse("06:00:00"))
				&& target.isBefore(LocalTime.parse("21:00:00")));
		respuesta.set("enHorario", enHorario);
		if (enHorario) {
			return respuesta;
		}

		// respuesta.set("enHorario", false); //TODO: sacar esto ya que está solo para
		// pruebas

		ApiResponseMB response = RestCatalogo.calendarioFechaActual(contexto);
		if (response.hayError()) { // si dio error le devuelvo que está en horario para que desde front funcione
									// como siempre
			respuesta.set("enHorario", true);
			return respuesta;
		}
		boolean esDiaHabil = response.objetos().get(0).string("esDiaHabil") == "1" ? true : false;

		// Este es un horario puesto a mano.
		// Por defecto la hora impuesta parga genrar la alerta es a las 7.
		respuesta.set("horaAlertaHoy", "07:00");
		respuesta.set("horaAlertaManana", "07:00");

		if (target.isBefore(LocalTime.parse("06:00:00")) && esDiaHabil) {
			respuesta.set("alertaSeHaceHoy", true);

		} else {
			Calendar hoy = Calendar.getInstance();
			hoy.set(Calendar.HOUR_OF_DAY, 0);
			hoy.set(Calendar.MINUTE, 0);
			hoy.set(Calendar.SECOND, 0);
			hoy.set(Calendar.MILLISECOND, 0);

			Date diaHabilPosterior = response.objetos().get(0).date("diaHabilPosterior", "yyyy-MM-dd");

			int dias = (int) (diaHabilPosterior.getTime() - hoy.getTime().getTime()) / 86400000;
			if (dias <= 1) {
				respuesta.set("alertaSeHaceManana", true);
			} else {
				respuesta.set("alertaSeHaceManana", false);
				respuesta.set("diaHabilPosterior", new SimpleDateFormat("dd/MM/yyyy").format(diaHabilPosterior));
			}

			respuesta.set("alertaSeHaceHoy", false);
		}

		return respuesta;
	}

	public static RespuestaMB categoriaMovimientoTarjeta(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");
		String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
		String fechaDesde = LocalDate.parse(fecha).minusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String fechaHasta = LocalDate.parse(fecha).plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		int numeroEstablecimiento = contexto.parametros.integer("numeroEstablecimiento");

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			tarjetaCredito = contexto.tarjetaCreditoTitular();
			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}
		}

		ApiResponseMB response = TarjetaCreditoService.consultaMovimientosTarjeta(contexto, fechaDesde, fechaHasta,
				tarjetaCredito.numero(), "999", "PRI", "99");
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();

		List<Objeto> movimientos = new ArrayList<>();
		for (Objeto movimiento : response.objetos()) {
			Objeto item = new Objeto();
			item.set("fecha", movimiento.string("fechaMovimiento"));
			item.set("monto", movimiento.string("montoMovimiento"));
			item.set("tipoMovimiento", movimiento.string("tipoMovimiento"));
			item.set("numeroEstablecimiento", movimiento.string("numeroEstablecimiento"));
			item.set("importeFormateado", Formateador.importe(movimiento.bigDecimal("monto")));
			movimientos.add(item);
		}

		Objeto resultado = filtrarMovimiento(movimientos, monto, numeroEstablecimiento);
		if (resultado != null) {
			switch (resultado.integer("tipoMovimiento")) {
			case 1:
				respuesta.set("categoria", "COMPRAS");
				respuesta.set("subcategoria", "TARJETA DE CREDITO");
				break;
			case 2:
				respuesta.set("categoria", "PAGOS");
				respuesta.set("subcategoria", "TARJETA DE CREDITO");
				break;
			case 3:
				respuesta.set("categoria", "TARJETA DE CREDITO");
				respuesta.set("subcategoria", "AJUSTE");
				break;
			case 4:
				respuesta.set("categoria", "PAGOS ");
				respuesta.set("subcategoria", "DEBITOS AUTOMATICOS");
				break;
			}
		}

		return respuesta;
	}

	private static Objeto filtrarMovimiento(List<Objeto> movimientos, BigDecimal monto, int numeroEstablecimiento) {
		Predicate<Objeto> establecimientoPredicate = movimiento -> movimiento.integer("numeroEstablecimiento")
				.equals(numeroEstablecimiento);
		Predicate<Objeto> montoPredicate = movimiento -> movimiento.bigDecimal("monto").equals(monto);

		List<Objeto> resultado = movimientos.stream().filter(montoPredicate).filter(establecimientoPredicate)
				.collect(Collectors.toList());

		if (resultado.isEmpty()) {
			resultado = movimientos.stream().filter(establecimientoPredicate).collect(Collectors.toList());
		}

		if (resultado.size() == 1 || (resultado.size() > 1 && (validarMismoTipoMovimiento(resultado)))) {
			return resultado.get(0);
		}

		return null;
	}

	private static boolean validarMismoTipoMovimiento(List<Objeto> resultado) {
		int tipoMovimiento = resultado.get(0).integer("tipoMovimiento");
		for (Objeto movimiento : resultado) {
			if (movimiento.integer("tipoMovimiento") != tipoMovimiento) {
				return false;
			}
		}
		return true;
	}

	private static boolean filtrarEstablecimiento(String idEstablecimiento) {
		for (String comercio : ConfigMB.string("comercios_no_disponibles").split("_")) {
			if (comercio.equals(idEstablecimiento.replaceFirst("^0+(?!$)", ""))) {
				return true;
			}
		}
		return false;
	}

	public static RespuestaMB consolidadaTarjetasFull(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {
			if (!ConfigMB.esProduccion() && contexto.idCobis().equals("8649931")) {
				return RespuestaMB.error();
			}

			Futuro<Boolean> futuroEnMora = new Futuro<>(() -> RestContexto.enMora(contexto));

			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaDebito.id());
				item.set("numero", tarjetaDebito.numero());
				item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
				item.set("limiteCompraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
				item.set("limiteExtraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));
				item.set("tipoTitularidad", tarjetaDebito.titularidad());

				ApiResponseMB detalleTarjeta = TarjetaDebitoService.tarjetaDebitoGet(contexto, tarjetaDebito.numero());
				String tipoTarjeta = detalleTarjeta.get("tipoTarjeta") != null
						? detalleTarjeta.get("tipoTarjeta").toString().trim()
						: "NO_DETERMINADO";

				item.set("tipoTarjeta", tipoTarjeta);
				item.set("fechaVencimiento", tarjetaDebito.fechaVencimiento("MM/yy"));
				item.set("estado", estadoTarjetaDebito(contexto, tarjetaDebito.numero()));
				Boolean variableEntorno = ConfigMB.bool("mb_prendido_evolutivo_titularidad", false);

				item.set("virtual",
						variableEntorno
								? validaVisualizacionTDV(tarjetaDebito.virtual(),
										detalleTarjeta.string("visualizaTdVirtual"))
								: tarjetaDebito.virtual());
				item.set("puedeSolicitarTDFisica",
						variableEntorno
								? puedeSolicitarTDF(tarjetaDebito.virtual(),
										detalleTarjeta.string("solicitudTdProgreso"))
								: null);

				// cuentas asociadas por TD
				List<Cuenta> cuentasDebito = tarjetaDebito.cuentasAsociadas();
				if (cuentasDebito != null && !cuentasDebito.isEmpty()) {
					item.set("estadoCuentasAsociadas", "ok");
					for (Cuenta cuenta : cuentasDebito) {
						Objeto subitem = new Objeto();
						subitem.set("id", cuenta.id());
						subitem.set("descripcion", cuenta.producto());
						subitem.set("numero", cuenta.numero());
						subitem.set("ultimos4digitos", Formateador.ultimos4digitos(cuenta.numero()));
                        subitem.set("permiteCuotificacion", !RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false) && !futuroEnMora.get() && !contexto.persona().esEmpleado());
						item.add("cuentasAsociadas", subitem);
					}
				} else {
					item.set("estadoCuentasAsociadas", "error");
				}

				ApiResponseMB apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
				if (!apiResponseTitularidad.hayError()
						&& !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
						&& apiResponseTitularidad.objetos("collection1").size() != 0)
					item.set("pausada", apiResponseTitularidad.objetos("collection1").get(0).get("Pausada"));

				respuesta.add("tarjetasDebito", item);
			}
			Boolean permitirCambioFormaPago = false;
			for (Cuenta cuenta : contexto.cuentas()) {
				if (cuenta.esPesos()) {
					permitirCambioFormaPago = true;
				}
			}

			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("virtual", tarjetaCredito.esTarjetaVirtual());
				item.set("origenVirtual", tarjetaCredito.esOrigenVirtual());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

				String vencimiento;
				String cierre;
				if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
						"prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
					Objeto fechaTC = MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null);
					vencimiento = fechaTC.string("vencimiento");
					cierre = fechaTC.string("cierre");
				} else {
					cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
					vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
				}
				item.set("fechaCierre", cierre);
				item.set("fechaVencimiento", vencimiento);
				item.set("fechaVigenciaHasta", tarjetaCredito.fechaVigenciaHasta("MM/yy"));
				item.set("denominacionTarjeta", tarjetaCredito.denominacionTarjeta().trim());
				item.set("formaPago", tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("permitirCambioFormaPago", permitirCambioFormaPago && tarjetaCredito.esTitular());

				respuesta.add("tarjetasCredito", item);
			}

			LocalTime target = LocalTime.now();
			Boolean enHorarioCambioFormaPago = (target.isAfter(LocalTime.parse("06:00:00"))
					&& target.isBefore(LocalTime.parse("21:00:00")));

			Objeto datosExtras = new Objeto();
			datosExtras.set("fueraHorarioCambioFormaPago", !enHorarioCambioFormaPago);
			datosExtras.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1);
			datosExtras.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1);
			datosExtras.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1);
			datosExtras.set("tieneSoloUnaTD", contexto.tarjetasDebito().size() == 1);
			respuesta.set("datosExtras", datosExtras);

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB tarjetaDebitoHabilitadaRedLink(ContextoMB contexto) {
		boolean traerTarjetasVirtuales = contexto.parametros.bool("traerTarjetasVirtuales", false);

		if (contexto.idCobis() == null) {
			return RespuestaMB.estado("SIN_PSEUDO_SESION");
		}
		List<TarjetaDebito> tarjetasDebitoRedLinkActivo = traerTarjetasVirtuales
				? contexto.tarjetasDebitoRedLinkActivoConVirtuales()
				: contexto.tarjetasDebitoRedLinkActivo();

		RespuestaMB respuesta = new RespuestaMB();

		for (TarjetaDebito tarjetaDebito : tarjetasDebitoRedLinkActivo) {
			Objeto item = new Objeto();
			item.set("idTarjetaDebito", tarjetaDebito.id());
			item.set("ultimosDigitos", tarjetaDebito.ultimos4digitos());
			respuesta.add("tarjetasDebito", item);
		}
		return respuesta;
	}

	public static RespuestaMB stopDebit(ContextoMB contexto) {
		try {
			RespuestaMB respuesta = new RespuestaMB();
			String cuenta = contexto.parametros.string("cuenta");
			String codigo = "";

			if (cuenta.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			TarjetaCredito tc = contexto.tarjetaCreditoTitular();
			contexto.parametros.set("idTarjetaCredito", tc.id());
			RespuestaMB respuestaEstadosCuenta = MBCuenta.estadosCuenta(contexto);
			String estadoTarjeta = respuestaEstadosCuenta.string("datos.estadoTarjeta");
			String estadoVencimiento = respuestaEstadosCuenta.string("datos.estadoVencimiento");
			contexto.parametros.set("idTarjetaCredito", null);

			if(!estadoTarjeta.equals("RESUMEN_LISTO") || estadoVencimiento.equals("HOY")){
				respuesta.setEstado("50030");
				respuesta.set("descripcion", mensajesValidosStopDebitTC(codigo));
				return respuesta;
			}

			ApiResponseMB response = TarjetaCreditoService.stopDebit(contexto, cuenta);

			try {
				if (!response.hayError() && response.codigo == 200) {
					if (response.string("retcode").equals("0")) {
						codigo = "0";
						//enviarCorreoStopDebit(contexto);
					}
				}

				codigo = !codigo.isEmpty() ? codigo : response.string("codigo");

			} catch (Exception e) {
			}

			respuesta.setEstado(codigo);
			respuesta.set("descripcion", mensajesValidosStopDebitTC(codigo));

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	private static String mensajesValidosStopDebitTC(String codigo) {
		String operacion_ok = ConfigMB.string("stop_debit_operacion_ok");
		String fuera_de_termino = ConfigMB.string("stop_debit_tc_fuera_de_termino");
		String ya_detenido = ConfigMB.string("stop_debit_tc_ya_detenido");
		String algun_error = ConfigMB.string("stop_debit_tc_error");

		switch (codigo) {
		case "0":
			return operacion_ok;
		case "50001":
			return algun_error;
		case "50005":
			return algun_error;
		case "50010":
			return algun_error;
		case "50020":
			return algun_error;
		case "50030":
			return fuera_de_termino;
		case "50040":
			return fuera_de_termino;
		case "50050":
			return algun_error;
		case "50060":
			return ya_detenido;
		case "50070":
			return fuera_de_termino;
		case "50080":
			return fuera_de_termino;
		default:
			return algun_error;
		}
	}

	public static RespuestaMB reposicionTD(ContextoMB contexto) {
		try {
			RespuestaMB respuesta = new RespuestaMB();
			String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
			String motivo = contexto.parametros.string("motivo", "PSREP001");
			String estadoPieza = "";
			String codigoDistribucion = "-99";

			if (Objeto.anyEmpty("idTarjetaDebito")) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "7", new Objeto().set("idTarjetaDebito", idTarjetaDebito),
					true)) {
				return RespuestaMB.estado("SOLICITUD_EN_CURSO").set("message", ConfigMB.string("message_en_curso"));
			}

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "7",
					new Objeto().set("idTarjetaDebito", contexto.tarjetaDebito(idTarjetaDebito).numero()), true)) {
				return RespuestaMB.estado("SOLICITUD_EN_CURSO").set("message", ConfigMB.string("message_en_curso"));
			}

			ApiResponseMB pieza = RestDelivery.deliveryPiezas(contexto, idTarjetaDebito);
			if (!pieza.hayError() && pieza != null) {
				codigoDistribucion = ((String) pieza.get("Estado")).isEmpty() ? "-10" : (String) pieza.get("Estado");
				estadoPieza = ((String) pieza.get("DescripcionEstado")).isEmpty() ? ""
						: (String) pieza.get("DescripcionEstado");
			}

			ApiResponseMB responseReclamo = RestPostventa.reposicionTD(contexto, idTarjetaDebito, motivo, estadoPieza,
					codigoDistribucion);
			if (responseReclamo == null || responseReclamo.hayError()) {

				if (responseReclamo == null) {
					return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
				}

				if (responseReclamo.objetos("Errores").get(0).string("MensajeCliente")
						.contains("HAY UNA SOLICITUD DE RENOVACION DE TARJETA TRAMITANDOSE DE ESTA MISMA TARJETA")) {
					return RespuestaMB.estado("SOLICITUD_EN_CURSO");
				}

				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB buscarTooltipConfiguracionTarjeta(ContextoMB contexto) {
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");
		RespuestaMB respuesta = new RespuestaMB();

		Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(),
				"TOOLTIP" + "_" + tipoTarjeta);
		if (configuracionTarjeta != null) {
			respuesta.set("mostrarTooltipTC", false);
		} else {
			respuesta.set("mostrarTooltipTC", true);
		}

		return respuesta;
	}

	public static RespuestaMB agregarTooltipConfiguracionTarjeta(ContextoMB contexto) {
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");

		Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(),
				"TOOLTIP" + "_" + tipoTarjeta);
		if (configuracionTarjeta == null) {
			SqlHomebanking.saveConfiguracionTarjeta(contexto.idCobis(), "TOOLTIP" + "_" + tipoTarjeta);
		}

		return new RespuestaMB();
	}

	public static RespuestaMB consolidadaTarjetasCredito(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {

			if (!ConfigMB.esProduccion() && contexto.idCobis().equals("8649931")) {
				return RespuestaMB.error();
			}

			Boolean permitirCambioFormaPago = false;
			for (Cuenta cuenta : contexto.cuentas()) {
				if (cuenta.esPesos()) {
					permitirCambioFormaPago = true;
				}
			}

			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
				item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
				item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
				item.set("fechaVencimientoCompleta", tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));
				item.set("fechaVigenciaDesde", tarjetaCredito.fechaVigenciaDesde("MM/yy"));
				item.set("fechaVigenciaHasta", tarjetaCredito.fechaVigenciaHasta("MM/yy"));
				item.set("denominacionTarjeta", tarjetaCredito.denominacionTarjeta().trim());
				item.set("formaPago", tarjetaCredito.formaPago().equalsIgnoreCase("EFECTIVO") ? "Manual"
						: tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("esHml", tarjetaCredito.esHML());
				item.set("cuenta", tarjetaCredito.cuenta());
				item.set("permitirCambioFormaPago", permitirCambioFormaPago && tarjetaCredito.esTitular());

				respuesta.add("tarjetasCredito", item);
			}

			LocalTime target = LocalTime.now();
			Boolean enHorarioCambioFormaPago = (target.isAfter(LocalTime.parse("06:00:00"))
					&& target.isBefore(LocalTime.parse("21:00:00")));

			Objeto datosExtras = new Objeto();
			datosExtras.set("fueraHorarioCambioFormaPago", !enHorarioCambioFormaPago);
			respuesta.set("datosExtras", datosExtras);

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB convertirTarjetaDebitoVirtualToFisica(ContextoMB contexto) {
		try {
			RespuestaMB respuesta = new RespuestaMB();
			String idTarjeta = contexto.parametros.string("idTarjeta");

			if (idTarjeta.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			TarjetaDebito td = contexto.tarjetaDebito(idTarjeta);
			ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoVirtualToFisica(contexto, td);

			if (response.hayError()) {
				if (response.get("codigo").equals("1831602") || response.string("mensajeAlUsuario")
						.contains("HAY UNA SOLICITUD DE ACTUALIZACION DE DATOS TRAMITANDOSE DE ESTA MISMA TARJETA")) {
					return RespuestaMB.estado("SOLICITUD_EN_CURSO");
				}
				if (response.get("codigo").equals("40003")) {
					return RespuestaMB.estado("BATCH_CORE").set("mensaje",
							ConfigMB.string("mb_prendido_tdv_mensaje_batch"));
				}

				return RespuestaMB.error();
			}

			respuesta.set("numeroSolicitud", response.get("numeroSolicitud"));

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB validarEstadosBajaTarjetaCredito(ContextoMB contexto) {
		if (contexto.idCobis() == null) {
			return RespuestaMB.estado("SIN_SESION");
		}
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		boolean esTitular = contexto.parametros.bool("esTitular");
		if (Objeto.anyEmpty(numeroTarjeta, esTitular)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		// TODO: DLV-38668
		// String fechaMinimaSolicitud =
		// LocalDate.now().plusDays(-7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		// boolean tieneSolicitudesEnCurso =
		// ApiProducto.tieneSolicitudesEnCurso(contexto.idCobis(), new String[]{"BAJA",
		// "BAJA_RECL"}, "ATC", numeroTarjeta, fechaMinimaSolicitud);

		TarjetaCredito tc = contexto.tarjetaCredito(numeroTarjeta);
		boolean tieneSolicitudesEnCurso = RestPostventa.tieneSolicitudEnCurso(contexto, "BAJATC",
				new Objeto().set("tcNumero", numeroTarjeta).set("idPaquete", tc.idPaquete()), true);

		if (tieneSolicitudesEnCurso) {
			return RespuestaMB.estado("TIENE_BAJA_TARJETA_CREDITO_EN_CURSO");
		} else if (esTitular) {
			boolean tienePrestamosTasaCero = MBPrestamo.buscarSolicitudPrestamoTasaCero(contexto.idCobis());
			if (tienePrestamosTasaCero) {
				return RespuestaMB.estado("TIENE_PRESTAMOS_TASA_CERO");
			}
		}
		return new RespuestaMB();
	}

	public static RespuestaMB validarCuotasPendientes(ContextoMB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		if (Objeto.anyEmpty(numeroCuenta, numeroTarjeta)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		ApiResponseMB cuotasPendientes = TarjetaCreditoService.cuotasPendientes(contexto, numeroCuenta, numeroTarjeta);
		if (cuotasPendientes.hayError()) {
			if (cuotasPendientes.string("codigo").equals("404") || cuotasPendientes.string("codigo").equals("112107")) {
				return new RespuestaMB();
			} else {
				return RespuestaMB.estado("ERROR_CONSULTA_CUOTAS_PENDIENTES");
			}
		}
		if (cuotasPendientes.objeto("cuotasPendientes") != null
				&& !cuotasPendientes.objetos("cuotasPendientes.tarjeta").isEmpty()) {
			return RespuestaMB.estado("TIENE_CUOTAS_PENDIENTES");
		}
		return new RespuestaMB();
	}

	public static RespuestaMB crearCasoBajaTarjetaCredito(ContextoMB contexto) {
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		RespuestaMB respuesta = new RespuestaMB();

		if (Objeto.anyEmpty(numeroTarjeta)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjeta);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("ERROR_TARJETA_NO_ENCONTRADA");
		}

		String codigoTipificacion = null;
		if (!tarjetaCredito.esTitular()) {
			codigoTipificacion = "BAJA_TC_ADICIONAL_PEDIDO";
		} else {
			String estado = tarjetaCredito.idEstado();
			boolean tienePaquete = !"".equals(tarjetaCredito.idPaquete());
			if ("20".equals(estado)) {
				codigoTipificacion = tienePaquete ? "BAJA_PAQUETES" : "BAJATC_PEDIDO";
			} else if ("25".equals(estado)) {
				codigoTipificacion = "BAJATC_PEDIDO";
			}
		}
		RespuestaMB caso = MBProducto.generarCasoBajaTC(contexto, tarjetaCredito, codigoTipificacion);
		if (caso.hayError()) {
			return caso;
		}
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTarjetaCredito)) {
			try {
				ApiResponseMB response = TarjetaCreditoService.consolidada(contexto);
				if (response.hayError()) {
					respuesta = obtenerRespuestaError(response);
				} else {
					List<Objeto> tarjetasAdicionales = new ArrayList<>();
					List<TarjetaCredito> tarjetasAdicionalesTerceros = contexto
							.tarjetasCreditoTitularConAdicionalesTercero();
					for (TarjetaCredito tarjeta : tarjetasAdicionalesTerceros) {
						if (!tarjeta.esTitular()) {
							Objeto tarjetaObj = new Objeto();
							// tarjetaObj.set("tipo", tarjeta.)
							tarjetasAdicionales.add(tarjetaObj);
						}
					}
					if (tarjetasAdicionales.isEmpty()) {
						respuesta.set("estado", "NO_HAY_ADICIONALES");
						respuesta.set("descripcion", "No se registraron adicionales.");
					}
					respuesta.set("tarjetasAdicionales", tarjetasAdicionales);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			respuesta.set("error", "Funcionalidad no habilitada");
		}
		return respuesta;
	}

	private static RespuestaMB obtenerRespuestaError(ApiResponseMB response) {
		RespuestaMB respuesta = new RespuestaMB();
		if (response.codigo != 306) {
			for (String clave : response.claves()) {
				respuesta.set(clave, response.get(clave));
			}
		} else {
			return RespuestaMB.estado(respuesta.string("error"));
		}
		return respuesta;
	}

	public static RespuestaMB estadoDeuda(ContextoMB contexto) {

		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

		if (Objeto.anyEmpty(idTarjetaCredito)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}

		Objeto datos = new Objeto();
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB responseMora = RestMora.consultarProductosMoraCache(contexto);
		if (responseMora.hayError()) {
			return RespuestaMB.estado("ERROR_MORA");
		}

		if (responseMora.codigo == 204 || responseMora.objetos().isEmpty()) {
			return RespuestaMB.exito();
		}

		boolean respuestaDeuda = tieneDeuda(responseMora.objetos(),
				tarjetaCredito.esPrefijoVisa() + tarjetaCredito.cuenta());
		Objeto productoEnMora = responseMora.objetos().stream()
				.filter(prod -> prod.string("NumeroProducto").trim().startsWith("2" + tarjetaCredito.cuenta()))
				.findFirst().orElse(null);

		datos.set("estadoDeuda", estadoDeudaTC(respuestaDeuda));
		if (productoEnMora != null) {
			datos.set("tipoMora", productoEnMora.get("Tipo Mora"));
			datos.set("cta_id", productoEnMora.get("cta_id"));
			datos.set("numeroProducto", productoEnMora.get("NumeroProducto"));
		}
		respuesta.set("datos", datos);

		return respuesta;
	}

	protected static ApiResponseMB consultarProductosMora(ContextoMB contexto) {
		ApiRequestMB requestMora = ApiMB.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
		requestMora.query("idClienteCobis", contexto.idCobis());
		return ApiMB.response(requestMora, contexto.idCobis());
	}

	private static boolean tieneDeuda(List<Objeto> objectos, String cuentaCliente) {
		for (Objeto item : objectos) {
			if (esTarjetaCredito(item.string("pro_cod").trim())
					&& esNumeroCuentaCliente(item.string("NumeroProducto").trim(), cuentaCliente)) {
				return true;
			}
		}
		return false;
	}

	private static String estadoDeudaTC(boolean tieneDeuda) {
		return tieneDeuda ? "CON_DEUDA" : "SIN_DEUDA";
	}

	private static boolean esTarjetaCredito(String codigoProducto) {
		return "203".equals(codigoProducto);
	}

	private static boolean esNumeroCuentaCliente(String numeroProducto, String numeroCuenta) {
		return numeroProducto.trim().equals(numeroCuenta);
	}

	public static RespuestaMB horarioBancarioPagoTarjeta(ContextoMB contexto) {

		ApiResponseMB response = RestScheduler.consultarHorarioBancario(contexto, "PagoTarjeta");

		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		Calendar calendar = Calendar.getInstance();
		Integer hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

		Integer horaInicio = response.objetos().get(0).integer("horaInicio");
		Integer horaFin = response.objetos().get(0).integer("horaFin");

		Boolean esHorarioBancario = hourOfDay >= horaInicio && hourOfDay < horaFin;

		respuesta.set("habilitado", esHorarioBancario);

		return respuesta;
	}

	public static Objeto fechasCierreVtoTarjetaCredito(ContextoMB contexto, TarjetaCredito tarjetaCredito,
			ApiResponseMB resumenTC) {

		if(ConfigMB.bool("mb_prendido_proximo_cierre_consolidada")){
			return fechasCierreVtoTarjetaCreditoV2(tarjetaCredito);
		}

		Objeto fechas = new Objeto();
		String cierre = "";
		String vencimiento = "";
		try {

			ApiResponseMB responseResumen;
			if (resumenTC == null) {
				responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
						tarjetaCredito.numero());
			} else {
				responseResumen = resumenTC;
			}

			if (responseResumen.hayError()) { // si hay error le pongo las fechas que obtiene por defecto
				cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
				vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
			} else { // si no hay error obtengo las fechas de ultima de resumen cuenta
				vencimiento = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.vencimiento",
						"yyyy-MM-dd", "dd/MM/yyyy", "");
				cierre = responseResumen.date("resumenCuenta.saldoenCuenta.ultima.liquidacionResumen.cierre",
						"yyyy-MM-dd", "dd/MM/yyyy", "");
				if (!vencimiento.isEmpty()) {
					try {
						Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
						Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
						if (esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
							// si fecha actual es superior fechavencimiento ultima, se manda fecha proxima
							vencimiento = responseResumen.date(
									"resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.vencimiento", "yyyy-MM-dd",
									"dd/MM/yyyy", "");
							cierre = responseResumen.date(
									"resumenCuenta.saldoenCuenta.proximo.liquidacionResumen.cierre", "yyyy-MM-dd",
									"dd/MM/yyyy", "");
						}
					} catch (Exception e) { // evitamos excepcion y pones fecha por defecto del servicio tarjeta credito
						cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
						vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
					}
				}
			}
			return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
		} catch (Exception e) {
			return fechas.set("cierre", tarjetaCredito.fechaCierre("dd/MM/yyyy")).set("vencimiento",
					tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));
		}
	}

	public static RespuestaMB obtenerCvvTdLink(ContextoMB contexto) {
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_td_cvv_link")) {

			ApiResponseMB apiResponse = new ApiResponseMB();
			RespuestaMB respuesta = new RespuestaMB();
			ApiRequestMB apiRequest;

			String nroTarjeta = contexto.parametros.string("nroTarjeta", null);
			String fechaVencimiento = contexto.parametros.string("fechaVencimiento", null);

			if (Objeto.anyEmpty(nroTarjeta, fechaVencimiento)) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if(contexto.tarjetaDebito(nroTarjeta) == null){
				return RespuestaMB.error();
			}
			
			apiRequest = ApiMB.request("LinkInternalValidacionCvv", "link", "POST",
					"/v1/servicios/tarjetas/validar-cvv", contexto);
			apiRequest.body("nroTarjeta", nroTarjeta);
			apiRequest.body("fechaVencimiento", formatearFechaVencimiento(fechaVencimiento));

			apiResponse = ApiMB.response(apiRequest);

			if (apiResponse.hayError())
				return apiResponse.codigo == 400 ? errorDatosIncorrectos(apiResponse.string("mensajeAlDesarrollador"))
						: RespuestaMB.estado(ERROR_VALIDAR_DATOS);

			respuesta.set("cvv", apiResponse);
			return respuesta;

		} else {
			return RespuestaMB.estado("NO_DISPONIBLE");
		}

	}

	/**
	 * Formatea la fecha de vencimiento si viene en formato MM/AA(sólamente ese
	 * formato) a MM/AAAA
	 */
	private static String formatearFechaVencimiento(String fechaVencimiento) {
		if (!Pattern.matches(REGEX_FECHA_VENCIMIENTO, fechaVencimiento)) {
			if (fechaVencimiento.contains("/")) {
				String[] valores = fechaVencimiento.split("/");
				if (Pattern.matches(REGEX_MES, valores[0]) && !Pattern.matches(REGEX_ANIO, valores[1])
						&& Pattern.matches(REGEX_MES, valores[1])) {
					return fechaVencimiento.replace("/", "/20");
				}
			}
		}
		return fechaVencimiento;
	}

	protected static RespuestaMB errorDatosIncorrectos(String mensaje) {
		String estado = ERROR_VALIDAR_DATOS;
		try {
			JsonObject errorJson = new Gson().fromJson(mensaje, JsonObject.class);
			if (errorJson != null && errorJson.has("codigo"))
				estado = errorJson.get("codigo").getAsString();
		} catch (JsonParseException e) {
		}
		return RespuestaMB.estado(estado);

	}

	public static RespuestaMB obtenerTitularidadTd(ContextoMB contexto) {
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_td_virtual_titularidad")) {
			ApiResponseMB apiResponse = new ApiResponseMB();
			RespuestaMB respuesta = new RespuestaMB();
			ApiRequestMB apiRequest;

			String numTarjeta = contexto.parametros.string("tarjeta", null);
			String extendido = contexto.parametros.string("extendido", null);

			if (Objeto.anyEmpty(numTarjeta, extendido)) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if (numTarjeta.length() != 16) {
				return RespuestaMB.estado("ERROR_TARJETA_INVALIDA");
			}

			TarjetaDebito tarjetaDetalle = contexto.tarjetaDebito(numTarjeta);
			if (tarjetaDetalle == null) {
				return RespuestaMB.estado("ERROR_TARJETA_NO_EXISTE");
			}

			apiRequest = ApiMB.request("Consulta_TitularidadTd", "tarjetasdebito", "GET", "/v1/tarjetaDebitoTitular",
					contexto);
			apiRequest.query("tarjeta", numTarjeta);
			apiRequest.query("extendido", extendido);

			apiResponse = ApiMB.response(apiRequest);
			if (apiResponse.hayError()) {
				respuesta.setEstado("ERROR");
			}

			Objeto item = new Objeto();
			item.absorber(apiResponse);
			Boolean variableEntorno = ConfigMB.bool("mb_prendido_evolutivo_titularidad", false);
			respuesta.set("datos", variableEntorno ? validarDatosTD(contexto, tarjetaDetalle, item) : item);
			return respuesta;

		} else {
			return RespuestaMB.estado("OPERACION_INHABILITADA");
		}
	}

	public static RespuestaMB puedePedirCambioCartera(ContextoMB contexto) {

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(contexto.parametros.string("idTarjetaCredito"));
		Objeto obj = new Objeto();
		obj.set("tcNumero", tarjetaCredito.numero());

		if (RestPostventa.tieneSolicitudEnCurso(contexto, "137", obj, true)) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}

		return RestPostventa.tieneMaximoCambiosCartera(contexto);
	}

	public static RespuestaMB getCarterasTC(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {
			TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(contexto.parametros.string("idTarjetaCredito"));

			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}
			respuesta.set("carteras", carteras(tarjetaCredito.esHML(), tarjetaCredito.grupoCarteraTc()));

		} catch (Exception e) {
			return RespuestaMB.error();
		}
		return respuesta;
	}

	private static List<Objeto> carteras(Boolean esHml, String grupoCarteraTc) {
		List<Objeto> carteras = new ArrayList<>();
		carteras.add(crearCarteraItem("Entre el 1 y el 5", "2"));
		carteras.add(crearCarteraItem("Entre el 3 y el 9", "1"));

		if (!esHml) {
			carteras.add(crearCarteraItem("Entre el 13 y el 19", "4"));
			carteras.add(crearCarteraItem("Entre el 20 y el 26", "3"));
		}

		for (Objeto cartera : carteras) {
			if (cartera.get("opcion").equals(grupoCarteraTc)) {
				cartera.set("esActual", true);
				break;
			}
		}

		return carteras;
	}

	private static Objeto crearCarteraItem(String descripcion, String opcion) {
		Objeto item = new Objeto();
		item.set("descripcion", descripcion);
		item.set("opcion", opcion);
		item.set("esActual", false);
		return item;
	}

	public static RespuestaMB crearCasoCambioCartera(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(contexto.parametros.string("idTarjetaCredito"));
		String valorCarteraNueva = contexto.parametros.string("carteraNueva");
		try {
			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}

			if (valorCarteraNueva == null) {
				return RespuestaMB.estado("VALOR_CARTERA_REQUERIDO");
			}

			List<Objeto> carteras = carteras(tarjetaCredito.esHML(), tarjetaCredito.grupoCarteraTc());

			Optional<Objeto> carteraValida = carteras.stream()
					.filter(cartera -> cartera.get("opcion").equals(valorCarteraNueva)
							&& Boolean.FALSE.equals(cartera.get("esActual")))
					.findFirst();

			if (!carteraValida.isPresent()) {
				return RespuestaMB.estado("VALOR_CARTERA_NO_VALIDO");
			}

			ApiResponseMB response = RestPostventa.cambioCarteraTC(contexto, valorCarteraNueva, tarjetaCredito);

			if (response == null || response.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(response);

			if (numeroCaso.isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta.set("ticket", numeroCaso);
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB mostarOpcionCambioCartera(ContextoMB contexto) {
		try {
			String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
			TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

			if (Objeto.anyEmpty(idTarjetaCredito)) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if (tarjetaCredito == null) {
				return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
			}

			if (!tarjetaCredito.idEstado().equals("20")) {
				return RespuestaMB.estado("OPCION_NO_HABILITADA");
			}

			if (!tarjetaCredito.esTitular()) {
				return RespuestaMB.estado("OPCION_NO_HABILITADA");
			}

			ApiResponseMB responseResumen = TarjetaCreditoService.resumenCuenta(contexto, tarjetaCredito.cuenta(),
					tarjetaCredito.numero());

			if (responseResumen.hayError()) {
				return RespuestaMB.exito();
			}

			if (responseResumen.codigo == 204) {
				return RespuestaMB.exito();
			}

			try {
				RespuestaMB respuestaEstado = MBCuenta.estadosCuenta(contexto);
				if (respuestaEstado.get("datos.estadoTarjeta") != null) {
					return RespuestaMB.estado("OPCION_NO_HABILITADA");
				}
			} catch (Exception e) {
			}

			return RespuestaMB.exito();
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB obtenerCvvTcPrisma(ContextoMB contexto) {
        String numeroTarjeta = contexto.parametros.string("numeroTarjeta", null);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.empty(numeroTarjeta))
            return RespuestaMB.parametrosIncorrectos();

        TarjetaCredito tarjeta = contexto.tarjetaCredito(numeroTarjeta);
        if (Objeto.empty(tarjeta))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_CREDITO);

        //return RespuestaMB.exito().set("cvv", "123");

        ApiResponseMB apiResponse = TarjetaCreditoService.consultaCvvPrisma(contexto, tarjeta);
        if (apiResponse.hayError())
            return castearRespuestaCvv(apiResponse);

        return RespuestaMB.exito().set("cvv", ((Objeto) apiResponse.objetos().get(0).get("card_information")).string("security_code"));

    }

	public static RespuestaMB solicitudPlasticoTCvirtual(ContextoMB contexto) {
		String nroTarjeta = contexto.parametros.string("nroTarjeta");

		if (nroTarjeta.isEmpty()) {
			return RespuestaMB.parametrosIncorrectos();
		}
		TarjetaCredito tarjeta = contexto.tarjetaCredito(nroTarjeta);
		if (tarjeta == null) {
			return RespuestaMB.estado("ERROR_TC_NO_VALIDA");
		}
		if (!tarjeta.esTarjetaVirtual()) {
			return RespuestaMB.estado("ERROR_TC_NO_VIRTUAL");
		}

		ApiResponseMB response = TarjetaCreditoService.tarjetaCreditoVirtualToFisica(contexto, tarjeta);

		if (response.hayError()) {
			return RespuestaMB.estado("ERROR_SOLICITUD_PLASTICO_TC");
		}
		if (response.objetos().get(0).existe("retCode") && response.objetos().get(0).integer("retCode") > 0) {
			String codigoError = response.objetos().get(0).string("retCode");
			codigoError = codigoError.substring(codigoError.length() - 2, codigoError.length());
			if (codigoError.equals("29")) {
				return RespuestaMB.estado("ERROR_POSEE_SOLICITUD_EN_CURSO");
			}
			if (Set.of("26", "28", "27").contains(codigoError)) { // ERRORES ASOCIADOS A PARAMETRIA Y GAFS
				return RespuestaMB.estado("ERROR_CALL_CENTER");
			}
			return new RespuestaMB().set("estado", "ERROR_SOLICITUD_PLASTICO_TC").set("descripcion",
					response.string("retDescrip"));
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB puedeCrearSolicitudTarjetaCreditoAdicional(ContextoMB contexto) {
		try {
			TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();

			RespuestaMB respuesta = new RespuestaMB();
			respuesta.set("tieneAdicionales", false);
			try {
				RespuestaMB tarjetas = tarjetasCreditoPropias(contexto);
				if (!tarjetas.objetos("tarjetasCreditoAdicionales").isEmpty()
						|| !tarjetas.objetos("tarjetasCreditoAdicionalesEnCurso").isEmpty()) {
					respuesta.set("tieneAdicionales", true);
				}
			} catch (Exception e) {
			}

			if (Util.isfueraHorario(ConfigMB.integer("procesos_horaInicio", 7),
					ConfigMB.integer("procesos_horaFin", 22))) {
				respuesta.setEstado("FUERA_HORARIO");
				return respuesta;
			}

			if (!tarjetaCreditoTitular.idEstado().equals("20")) {
				respuesta.setEstado("TARJETA_CON_PROBLEMAS");
				return respuesta;
			}

			try {
				contexto.parametros.set("idTarjetaCredito", tarjetaCreditoTitular.id());
				RespuestaMB respuestaEstado = MBCuenta.estadosCuenta(contexto);
				if (!respuestaEstado.hayError() && respuestaEstado.objeto("datos").get("estadoMora") != null) {
					respuesta.setEstado("TARJETA_CON_PROBLEMAS");
					return respuesta;
				}
			} catch (Exception e) {
			}

			if (tarjetaCreditoTitular.esHML()) {
				respuesta.setEstado("TARJETA_HML");
				return respuesta;
			}

			Objeto domicilioAnterior = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
			Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
			String fechacambioPrisma = Util.calcularFechaNdiasHabiles(contexto,
					domicilioAnterior.string("fechaModificacion"), 3);
			Date fechacambioPrismaDate = Fecha.stringToDate(fechacambioPrisma, "yyyy-MM-dd");
			if (fechaActual.compareTo(fechacambioPrismaDate) <= 0) {
				respuesta.setEstado("CAMBIO_DOMICILIO_PENDIENTE");
				return respuesta;
			}

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}

	}

	public static RespuestaMB AdicionalEnCurso(ContextoMB contexto) {
		try {
			String documento = contexto.parametros.string("documento");

			if (Objeto.anyEmpty(documento)) {
				return RespuestaMB.parametrosIncorrectos();
			}

			RespuestaMB tarjetas = tarjetasCreditoPropias(contexto);

			if (tarjetas.hayError()) {
				return RespuestaMB.error();
			}

			for (Objeto tca : tarjetas.objetos("tarjetasCreditoAdicionales")) {
				if (tca.string("documento").equals(documento)) {
					return RespuestaMB.estado("ADICIONAL_VIGENTE");
				}
			}

			for (Objeto tca : tarjetas.objetos("tarjetasCreditoAdicionalesEnCurso")) {
				if (tca.string("documento").equals(documento)) {
					return RespuestaMB.estado("EN_CURSO");
				}
			}

			return RespuestaMB.exito();
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	private static Boolean chequeoNuevaSolicitud(ContextoMB contexto, Objeto solicitud) {
		Boolean enCurso = false;
		try {
			if ("F".equalsIgnoreCase(solicitud.string("estado"))) {
				Date fechaActual = dateStringToDate(new Date(), "yyyy-MM-dd");
				String fechaTopeProcesosBatch = Util.calcularFechaNdiasHabiles(contexto,
						solicitud.string("fechaDeAlta"), 3);
				Date fechaTopeProcesosBatchDate = Fecha.stringToDate(fechaTopeProcesosBatch, "yyyy-MM-dd");
				if (fechaActual.compareTo(fechaTopeProcesosBatchDate) <= 0) {
					enCurso = true;
				}
			}
			return enCurso;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static RespuestaMB consultaTDVirtual(ContextoMB contexto) {
		String documento = contexto.parametros.string("documento");
		String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);

		if (Objeto.anyEmpty(documento)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Objeto cobisTdVirtual = SqlHomebanking.buscarDocumentoParaTDVirtual(1, idTipoDocumento, documento);
		if (cobisTdVirtual == null) {
			return RespuestaMB.estado("0");
		} else {
			return RespuestaMB.estado("1");
		}
	}

	private static void contador(ContextoMB contexto, String nemonico) {
		try {
			contexto.parametros.set("nemonico", nemonico);
			Util.contador(contexto);
		} catch (Exception e) {
		}
	}

	public static RespuestaMB promoNoImpactadaTD(ContextoMB contexto) {
		try {
			String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
			String comentario = contexto.parametros.string("comentario");
			String idCuenta = contexto.parametros.string("idCuenta");
			String rubro = contexto.parametros.string("rubro");
			String nombreComercio = contexto.parametros.string("nombreComercio");
			String fecha = contexto.parametros.string("fecha");
			String monto = contexto.parametros.string("monto");
			String moneda = contexto.parametros.string("moneda");

			if (Objeto.anyEmpty(idTarjetaDebito)) {
				return RespuestaMB.parametrosIncorrectos();
			}

			RespuestaMB respuesta = new RespuestaMB();
			TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);
//			String idCuenta = "";
//			List<Cuenta> cuentas = contexto.cuentas();
//			for (Cuenta cuenta : cuentas) {
//				if (!idCuenta.isEmpty()) {
//					continue;
//				}
//				ApiResponseMB response = CuentasService.getTarjetaDebito(contexto, cuenta);
//				for (Objeto tarjeta : response.objetos()) {
//					if (idTarjetaDebito.equals(tarjeta.string("numeroTarjeta"))) {
//						idCuenta = cuenta.id();
//						continue;
//					}
//				}
//			}

            if (idCuenta.isEmpty()) {
                return RespuestaMB.estado("CUENTA_NO_ASOCIADA");
            }
            Cuenta cuenta = contexto.cuenta(idCuenta);

            switch (rubro) {
                case "Otros": {
                    rubro = "N/A";
                    break;
                }
                case "Delivery": {
                    rubro = "Rappi";
                    break;
                }
                case "Farmacias y perfumerías": {
                    rubro = "Farmacias";
                    break;
                }
                default:
                    break;
            }

            Objeto movimiento = new Objeto();
            movimiento.set("nombreComercio", nombreComercio);
            movimiento.set("fecha", fecha);
            movimiento.set("monto", monto);
            movimiento.set("moneda", moneda);
            movimiento.set("rubro", rubro);
            movimiento.set("comentario", comentario);

            ApiResponseMB response = RestPostventa.promoNoImpactadaTD(contexto, td, cuenta, movimiento);

            if (response == null || response.hayError()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(response);

            if (numeroCaso.isEmpty()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }
            return respuesta.set("ticket", numeroCaso);

        } catch (Exception e) {
            return RespuestaMB.error();
        }
    }

    public static boolean validaVisualizacionTDV(Boolean esVirtual, String visualizaTdVirtual) {
        if (!esVirtual && "S".equalsIgnoreCase(visualizaTdVirtual)) {
            return true;
        }

        return esVirtual;
    }

    public static boolean puedeSolicitarTDF(Boolean esVirtual, String solicitudTdProgreso) {
        if (!esVirtual && "N".equalsIgnoreCase(solicitudTdProgreso)) {
            return false;
        }

        return !"S".equalsIgnoreCase(solicitudTdProgreso);
    }

    public static RespuestaMB crearCasoDesconomientoConsumo(ContextoMB contexto) {
        try {
            String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
            String idCuenta = contexto.parametros.string("idCuenta");
            String fecha = contexto.parametros.string("fechaReclamo");
            List<Objeto> movimientos = contexto.parametros.objetos("movimientos");
            // String comprobante = contexto.parametros.string("comprobante");

            if (Objeto.anyEmpty(idTarjetaDebito)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            RespuestaMB respuesta = new RespuestaMB();
            TarjetaDebito td = contexto.tarjetaDebito(idTarjetaDebito);

            if (idCuenta.isEmpty()) {
                return RespuestaMB.estado("CUENTA_NO_ASOCIADA");
            }

            Cuenta cuenta = contexto.cuenta(idCuenta);

            ApiResponseMB response = RestPostventa.desconocimientoConsumo(contexto, td, cuenta, fecha, movimientos);

            if (response == null || response.hayError()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }

            String numeroCaso = Util.getNumeroCaso(response);

            if (numeroCaso.isEmpty()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }
            return respuesta.set("ticket", numeroCaso);

        } catch (Exception e) {
            return RespuestaMB.error();
        }
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea).
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static RespuestaMB pausarTarjetaDebito(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return RespuestaMB.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return RespuestaMB.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponseMB apiResponse = pausarTarjetaDebitoLink(contexto,
                !ConfigMB.esDesarrollo() ? tarjetaDebito.numero() : "4998590351346602", tipoOperacion);

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        ApiResponseMB apiResponseCore = pausarTarjetaDebito(contexto, tarjetaDebito,
                tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()));

        if (apiResponseCore.hayError()) {
            apiResponse = pausarTarjetaDebitoLink(contexto,
                    !ConfigMB.esDesarrollo() ? tarjetaDebito.numero() : "4998590351346602",
                    tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()) ? TipoOperacionPausado.HABILITAR.name()
                            : TipoOperacionPausado.BLOQUEAR.name());
            // VER QUÉ HACER EN ESTE CASO
            if (apiResponse.hayError())
                return castearRespuestaPausado(apiResponse);

            return castearRespuestaPausado(apiResponseCore);
        }

        return RespuestaMB.exito();
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea) por
     * contingencia con Link.
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static RespuestaMB pausarTarjetaDebitoLinkContingencia(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return RespuestaMB.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return RespuestaMB.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponseMB apiResponse = pausarTarjetaDebitoLink(contexto, tarjetaDebito.numero(), tipoOperacion);

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        return RespuestaMB.exito();
    }

    /**
     * Realiza el "pausado" de Tarjeta de Débito (Habilita o Bloquea) por
     * contingencia con Core.
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static RespuestaMB pausarTarjetaDebitoCoreContingencia(ContextoMB contexto) {
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(idTarjetaDebito, tipoOperacion))
            return RespuestaMB.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausado.class, tipoOperacion))
            return RespuestaMB.estado(TIPO_OPERACION_INVALIDA);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        if (Objeto.anyEmpty(tarjetaDebito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);

        ApiResponseMB apiResponse = pausarTarjetaDebito(contexto, tarjetaDebito,
                tipoOperacion.equals(TipoOperacionPausado.BLOQUEAR.name()));

        if (apiResponse.hayError())
            return castearRespuestaPausado(apiResponse);

        return RespuestaMB.exito();
    }

    public static Objeto fechaCierreParametrica(Date fechaProximoCierre, String carteraTC) {
        String cierre = "";
        String vencimiento = "";
        Objeto fechas = new Objeto();
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaProximoCierre);
            Integer periodo = calendar.get(Calendar.MONTH) + 1;
            Integer anio = calendar.get(Calendar.YEAR);
            SqlResponseMB sqlCartera = TarjetaCreditoService.obtenerFechaCierreVtoCartera(carteraTC, periodo, anio);

            if (sqlCartera.registros.size() > 0) {
                cierre = Fecha.formato(sqlCartera.registros.get(0).string("cierre"), "dd/MM/yyyy", "yyyy-MM-dd");
                vencimiento = Fecha.formato(sqlCartera.registros.get(0).string("vencimiento"), "dd/MM/yyyy",
                        "yyyy-MM-dd");
            }
        } catch (Exception e) {
        }
        return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
    }

    private static ApiResponseMB pausarTarjetaDebito(ContextoMB contexto, TarjetaDebito tarjetaDebito, boolean pausar) {
        return TarjetaDebitoService.pausadoTarjetaDebito(contexto, tarjetaDebito, pausar);
    }

    private static ApiResponseMB pausarTarjetaDebitoLink(ContextoMB contexto, String numeroTarjetaDebito,
                                                         String tipoOperacion) {

        ApiRequestMB request = ApiMB.request("LinkPausadoTarjetaDebito", "link", "POST", URL_PAUSADO_DEBITO, contexto);
        request.body("numeroTarjeta", numeroTarjetaDebito);
        request.body("tipoOperacion", tipoOperacion);
        request.headers.put(X_TIMESTAMP_HEADER, new SimpleDateFormat(X_TIMESTAMP_FORMAT).format(new Date()));
        request.permitirSinLogin = true;

        return ApiMB.response(request);
    }

    private static RespuestaMB castearRespuestaPausado(ApiResponseMB apiResponse) {
        return apiResponse.codigo == 400 ? errorDatosIncorrectos(apiResponse.string(MENSAJE_DESARROLLADOR))
                : RespuestaMB.estado(ERROR_PAUSADO);
    }

    public static ApiResponseMB obtenerTitularidadTd(ContextoMB contexto, String numeroTarjeta, String extendido) {
        ApiRequestMB apiRequest = ApiMB.request("Consulta_TitularidadTd", "tarjetasdebito", "GET",
                "/v1/tarjetaDebitoTitular", contexto);

        apiRequest.query("tarjeta", numeroTarjeta);
        apiRequest.query("extendido", extendido);

        return ApiMB.response(apiRequest);
    }

    private static String estadoTarjetaDebito(ContextoMB contexto, String nroTarjeta) {
        String estadoTarjeta = "";
        ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, nroTarjeta);
        if (response.hayError()) {
            if (response.codigo == 500) {
                estadoTarjeta = "ERROR_LINK";
            } else {
                estadoTarjeta = "NO_DETERMINADO";
            }
        } else {
            estadoTarjeta = response.string("estadoTarjeta");
        }

        if (estadoTarjeta.toUpperCase().equals("CERRADA"))
            estadoTarjeta = "HABILITADA";

        if (estadoTarjeta == null || ((!"ERROR_LINK".equals(estadoTarjeta.toUpperCase()))
                && (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
                && !"INACTIVA".equals(estadoTarjeta.toUpperCase())))) {
            estadoTarjeta = "NO_DETERMINADO";
        }
        return estadoTarjeta;
    }

    private static Objeto validarDatosTD(ContextoMB contexto, TarjetaDebito tarjetaDetalle, Objeto item) {

        Objeto tarjeta = item.objetos("collection1").get(0);
        tarjeta.set("visualizaTdVirtual",
                validaVisualizacionTDV(tarjetaDetalle.virtual(), item.string("visualizaTdVirtual")));
        tarjeta.set("puedeSolicitarTDFisica",
                puedeSolicitarTDF(tarjetaDetalle.virtual(), item.string("solicitudTdProgreso")));
        tarjeta.set("solicitudTdProgreso", null);

        return item.set("collection1", tarjeta.objetos());
    }

    public static RespuestaMB verificarTarjetaDebitoPausadaEnCuenta(Cuenta cuenta, ContextoMB contexto) {
        boolean pausada = false;
        for (TarjetaDebito td : contexto.tarjetasDebito().stream()
                .filter(t -> t.cuentasAsociadas().stream().filter(c -> c.numero().equals(cuenta.numero())).count() != 0)
                .collect(Collectors.toList())) {
            ApiResponseMB apiResponseTitularidad = null;
            if (!pausada) {
                apiResponseTitularidad = MBTarjetas.obtenerTitularidadTd(contexto, td.numero(), "N");
                if (!apiResponseTitularidad.hayError()
                        && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                        && apiResponseTitularidad.objetos("collection1").size() != 0
                        && apiResponseTitularidad.objetos("collection1").get(0).get("Pausada").equals("S"))
                    pausada = true;
            }
        }
        return pausada ? RespuestaMB.estado("TARJETA_PAUSADA") : null;
    }

    public static RespuestaMB verificarTarjetaDebitoPausada(TarjetaDebito tarjetaDebito, ContextoMB contexto) {
        ApiResponseMB apiResponseTitularidad = obtenerTitularidadTd(contexto, tarjetaDebito.numero(), "N");
        if (!apiResponseTitularidad.hayError() && !Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
                && apiResponseTitularidad.objetos("collection1").size() != 0
                && apiResponseTitularidad.objetos("collection1").get(0).get("Pausada").equals("S"))
            return RespuestaMB.estado("TARJETA_PAUSADA");

        return null;
    }

    /**
     * Realiza el "pausado" de Tarjeta de crédito (Habilita o Bloquea).
     *
     * @param contexto
     * @return respuesta de si fue ok.
     */
    public static RespuestaMB pausarTarjetaCredito(ContextoMB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
        String tipoOperacion = contexto.parametros.string("tipoOperacion", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaCredito, tipoOperacion))
            return RespuestaMB.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(TipoOperacionPausadoCredito.class, tipoOperacion))
            return RespuestaMB.estado(TIPO_OPERACION_INVALIDA);

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (Objeto.anyEmpty(tarjetaCredito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_CREDITO);

        ApiResponseMB apiResponse = TarjetaCreditoService.pausarTarjeta(contexto, tarjetaCredito,
                EnumUtils.getEnum(TipoOperacionPausadoCredito.class, tipoOperacion));

        if (apiResponse.codigo == 204) {
            return RespuestaMB.exito();
        }

        RespuestaMB respuesta = manejoErrorPausado(apiResponse);
        if (!Objeto.empty(respuesta))
            return respuesta;

        return RespuestaMB.exito();
    }

    /**
     * Devuelve Estado si está pausada o no la tarjeta de crédito
     *
     * @param contexto
     * @return respuesta si está pausada
     */
    public static RespuestaMB estadoTarjetaCredito(ContextoMB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.anyEmpty(idTarjetaCredito))
            return RespuestaMB.parametrosIncorrectos();

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (Objeto.anyEmpty(tarjetaCredito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_CREDITO);

        ApiResponseMB apiResponse = TarjetaCreditoService.detallePausadoTarjeta(contexto, tarjetaCredito);

        RespuestaMB respuesta = manejoErrorPausado(apiResponse);
        if (!Objeto.empty(respuesta))
            return respuesta;

        return RespuestaMB.exito("pausada", apiResponse.objetos().get(0).string("value").equals("DISABLED"));
    }

    private static RespuestaMB manejoErrorPausado(ApiResponseMB apiResponse) {
        if (apiResponse.hayError()) {
            if (apiResponse.codigo == 400 || apiResponse.codigo == 504)
                return RespuestaMB.estado(ERROR_ESTADO_TARJETA_PAUSADO);
            return RespuestaMB.estado(ERROR_PAUSADO);
        }

        if (apiResponse.objetos().isEmpty())
            return RespuestaMB.estado(ERROR_ESTADO_TARJETA_PAUSADO);

        return null;
    }

    private static RespuestaMB castearRespuestaCvv(ApiResponseMB apiResponse) {
        return apiResponse.codigo == 400 ? errorDatosIncorrectos(apiResponse.string(MENSAJE_DESARROLLADOR))
                : RespuestaMB.estado(ERROR_CVV_TARJETA);
    }

    public static RespuestaMB obtenerDatosTarjetaCredito(ContextoMB contexto) {
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.empty(idTarjetaCredito))
            return RespuestaMB.parametrosIncorrectos();

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
        if (Objeto.empty(tarjetaCredito))
            return RespuestaMB.estado(NO_EXISTE_TARJETA_CREDITO);

        return RespuestaMB.exito()
                .set("tarjetaCredito", new Objeto()
                        .set("id", tarjetaCredito.id())
                        .set("numero", tarjetaCredito.numero())
                        .set("nombre", tarjetaCredito.denominacionTarjeta().trim())
                        .set("fechaVigenciaHasta", tarjetaCredito.fechaVigenciaHasta("MM/yy")));
    }
    
    public static RespuestaMB avisarViajeExterior(ContextoMB contexto) {
    	String nroTarjeta = contexto.parametros.string("nroTarjeta", null);

		
		if (Objeto.anyEmpty(nroTarjeta)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		
		ApiResponseMB apiResponse = TarjetaDebitoService.avisarViajeExterior(contexto,nroTarjeta);

		if (apiResponse.hayError()) {
			return RespuestaMB.error();
		}
    	
    	
    	return RespuestaMB.exito();
    }

	// Version V2 con parametro dni (cuit viene vacio)
	public static RespuestaMB crearSolicitudTarjetaCreditoAdicionalV2(ContextoMB contexto) {
		String cuit = contexto.parametros.string("cuit", "");
		Integer porcentaje = contexto.parametros.integer("porcentaje");
		String tipoRelacion = contexto.parametros.string("tipoRelacion", "18");
		String fechaNacimiento = contexto.parametros.string("fechaNacimiento", "");

		if (Objeto.anyEmpty(porcentaje)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		if (!ConfigMB.esProduccion() && contexto.idCobis().equals("772245")) {
			return RespuestaMB.error();
		}

		TarjetaCredito tarjetaCreditoTitular = contexto.tarjetaCreditoTitular();
		if (!tarjetaCreditoTitular.idEstado().equals("20")) {
			return RespuestaMB.estado("TARJETA_CON_PROBLEMAS");
		}

		if (tarjetaCreditoTitular.esHML()) {
			return RespuestaMB.estado("TARJETA_HML");
		}

		try {
			RespuestaMB personaRespuesta = MBPersona.persona(contexto);

			if (personaRespuesta.hayError() || personaRespuesta.objeto("persona").string("cuit").isEmpty()) {
				cuit = MBPersona.obtenerCuitDeDocumento(contexto);
			} else {
				cuit = personaRespuesta.objeto("persona").string("cuit");
			}

			if (cuit.isEmpty()) {
				contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
				return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
			}

			String cobisRelacion = "";
			ApiMB.eliminarCache(contexto, "PersonasRelacionadas", cuit);
			ApiResponseMB responsePersonaEspecifica = RestPersona.consultarPersonaEspecifica(contexto, cuit);
			cobisRelacion = responsePersonaEspecifica.string("idCliente");

			if (!responsePersonaEspecifica.string("idSexo").equalsIgnoreCase(contexto.parametros.string("idSexo"))
					|| cobisRelacion.isEmpty()) {
				contador(contexto, "ADCIONAL_TC_PERS_NO_ENCONTRADA");
				return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
			}

			if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
					|| responsePersonaEspecifica.string("idVersionDocumento").isEmpty()
					|| responsePersonaEspecifica.string("idEstadoCivil").isEmpty()
					|| responsePersonaEspecifica.string("fechaNacimiento").isEmpty()
					|| responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {

				Objeto datos = new Objeto();

				if (responsePersonaEspecifica.string("idPaisNacimiento").isEmpty()
						|| responsePersonaEspecifica.string("idNacionalidad").isEmpty()) {
					datos.set("idPaisNacimiento", 80);
					datos.set("idNacionalidad", 80);
					datos.set("idPaisResidencia", "");
				}

				if (responsePersonaEspecifica.string("idVersionDocumento").isEmpty()) {
					datos.set("idVersionDocumento", "A");
				}

				if (responsePersonaEspecifica.string("idEstadoCivil").isEmpty()) {
					datos.set("idEstadoCivil", "S");
				}

				if (responsePersonaEspecifica.string("fechaNacimiento").isEmpty()) {
					fechaNacimiento = fechaNacimiento.replace("\\", "");
					fechaNacimiento = Fecha.formato(fechaNacimiento, "dd/MM/yyyy", "yyyy/MM/dd");
					fechaNacimiento = fechaNacimiento.replace("/", "-");
					datos.set("fechaNacimiento", fechaNacimiento + "T00:00:00");
				}

				if (responsePersonaEspecifica.string("idSituacionImpositiva").isEmpty()) {
					datos.set("idSituacionImpositiva", "CONF");
				}
				RestPersona.actualizarPersona(contexto, datos, cuit);
			}

			if (responsePersonaEspecifica.string("etagDomicilios").equalsIgnoreCase("-1")) {
				Objeto domicilioTitularLegal = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
				RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularLegal, "LE");
				Objeto domicilioTitularPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
				RestPersona.crearDomicilioProspecto(contexto, cuit, domicilioTitularPostal, "DP");
			}

			String idEstadoCivil = contexto.persona().idEstadoCivil();
			Objeto relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);

			if (idEstadoCivil.equalsIgnoreCase("C") && !tipoRelacion.equals("2")) {
				if (relacion.string("idTipoRelacion").equals("2")) {
					tipoRelacion = "2";
				}
			}

			if (relacion.string("idTipoRelacion").isEmpty()) {
				try {
					ApiResponseMB responseGenerarRelacionPersona = RestPersona.generarRelacionPersona(contexto,
							tipoRelacion, cuit, cobisRelacion);
					if (responseGenerarRelacionPersona.hayError()) {
						return RespuestaMB.estado("ERROR_GENERAR_RELACION");
					}
					try {
						ApiMB.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
						relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
					} catch (Exception e) {
					}
				} catch (Exception e) {
				}
			} else if (!relacion.string("idTipoRelacion").equals(tipoRelacion)) {
				try {
					Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
					SimpleDateFormat destinoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					String fechaModificacion = destinoSDF.format(fechaActual);
					ApiResponseMB responseActualizarRelacionPersona = RestPersona.actualizarRelacionPersona(contexto,
							relacion.string("id"), tipoRelacion, cuit, cobisRelacion, null, null, fechaModificacion);

					if (responseActualizarRelacionPersona.hayError()) {
						return RespuestaMB.estado("ERROR_GENERAR_RELACION");
					}

					try {
						ApiMB.eliminarCache(contexto, "PersonasRelacionadas", contexto.idCobis());
						relacion = RestPersona.getTipoRelacionPersona(contexto, cuit);
					} catch (Exception e) {
						//
					}
				} catch (Exception e) {
					//
				}
			}

			// Caso al adicional con el titular
			if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
				ApiRequestMB request = ApiMB.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
				request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
				request.path("id", cuit);
				request.body("idEstadoCivil", "C");
				request.body("idSubtipoEstadoCivil", "Y");

				ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
				if (response.hayError()) {
					return RespuestaMB.error();
				}
			}

			// Caso al titular con el adicional
			if (relacion.string("idTipoRelacion").equals("2") && !idEstadoCivil.equalsIgnoreCase("C")) {
				ApiRequestMB request = ApiMB.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
				request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
				request.path("id", contexto.persona().cuit());
				request.body("idEstadoCivil", "C");
				request.body("idSubtipoEstadoCivil", "Y");

				ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
				if (response.hayError()) {
					return RespuestaMB.error();
				}
			}
		} catch (Exception e) {
		}

		// Generar Solicitud
		ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
		if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarSolicitud.objetos("Errores").isEmpty()
							? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		String idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

		// Generar Integrante
		ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
		if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarIntegrante.objetos("Errores").isEmpty()
							? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud, cuit);
		if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!generarIntegrante.objetos("Errores").isEmpty()
							? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		// Generar Tarjeta Credito Adicional
		ApiResponseMB tarjetaCreditoAdicional = RestOmnicanalidad.generarTarjetaCreditoAdicional(contexto, idSolicitud,
				cuit);
		if (tarjetaCreditoAdicional.hayError() || !tarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!tarjetaCreditoAdicional.objetos("Errores").isEmpty()
							? tarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}
		String idTarjetaCreditoAdicional = tarjetaCreditoAdicional.objetos("Datos").get(0).string("Id");

		// Motor
		ApiResponseMB responseMotor = RestOmnicanalidad.evaluarSolicitud(contexto, idSolicitud);
		if (responseMotor.hayError() || !responseMotor.objetos("Errores").isEmpty()) {
			try {
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			} catch (Exception e) {
			}
			return RespuestaMB.error().set("error", responseMotor.objetos("Errores").get(0).string("MensajeCliente"));
		}
		if (!responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
			String estado = "ERROR";
			estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "APROBADO_AMARILLO"
					: estado;
			estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
			estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;

			if (responseMotor.objetos("Datos").get(0).string("Explicacion")
					.contains("La edad es inferior a la mínima requerida")) {
				switch (tipoRelacion) {
					case "2": {
						estado = "EDAD_16";
						break;
					}
					case "18": {
						estado = "EDAD_18";
						break;
					}
					case "1", "4", "15": {
						estado = "EDAD_14";
						break;
					}
					default:
						break;
				}
			}
			try {
				contador(contexto, "ADCIONAL_TC_" + estado);
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			} catch (Exception e) {
			}

			return new RespuestaMB().setEstado(estado).set("error",
					responseMotor.objetos("Datos").get(0).string("Explicacion"));
		}

		// Embozado
		String embozado = "";
		ApiResponseMB responsePersona = RestPersona.consultarPersonaEspecifica(contexto, cuit);
		if (responsePersona.hayError()) {
			return new RespuestaMB().setEstado("ERROR");
		}
		embozado = responsePersona.string("apellidos") + "/" + responsePersona.string("nombres");
		embozado = embozado.toUpperCase();
		embozado = embozado.length() > 19 ? embozado.substring(0, 19) : embozado;

		// Actualizar Tarjeta Credito Adicional
		ApiResponseMB actualizarTarjetaCreditoAdicional = RestOmnicanalidad.actualizarTarjetaCreditoAdicional(contexto,
				idSolicitud, idTarjetaCreditoAdicional, cuit, embozado, porcentaje);
		if (actualizarTarjetaCreditoAdicional.hayError()
				|| !actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()) {
			return new RespuestaMB().setEstado("ERROR").set("error",
					!actualizarTarjetaCreditoAdicional.objetos("Errores").isEmpty()
							? actualizarTarjetaCreditoAdicional.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		// Finalizar
		ApiResponseMB response = RestOmnicanalidad.finalizarSolicitud(contexto, idSolicitud);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			String estado = "ERROR";
			if (!response.objetos("Errores").isEmpty()
					&& response.objetos("Errores").get(0).string("Codigo").equals("1831609")) {
				estado = "IR_A_SUCURSAL";
			}
			if (!response.objetos("Errores").isEmpty()
					&& response.objetos("Errores").get(0).string("Codigo").equals("1831602")) {
				estado = "EN_PROCESO_ACTUALIZACION";
			}
			return new RespuestaMB().setEstado(estado).set("error",
					!response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente")
							: null);
		}

		try {
			ApiMB.eliminarCache(contexto, "EstadoSolicitudBPM", contexto.idCobis(), contexto.persona().numeroDocumento());
		} catch (Exception e) {
			//
		}
		contador(contexto, "ADCIONAL_TC_OK");
		return RespuestaMB.exito();
	}

	public static Objeto fechasCierreVtoTarjetaCreditoV2(TarjetaCredito tarjetaCredito) {
		Objeto fechas = new Objeto();
		String cierre = "";
		String vencimiento = "";
		try {
			vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
			cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
			if (!vencimiento.isEmpty()) {
				Date fechaVencimiento = Fecha.stringToDate(vencimiento, "dd/MM/yyyy");
				Date fechaActual = dateStringToDate(new Date(), "dd/MM/yyyy");
				if (esFechaActualSuperiorVencimiento(fechaVencimiento, fechaActual)) {
					vencimiento = "";
					cierre = tarjetaCredito.fechaProximoCierre("dd/MM/yyyy");
				}
			}
			return fechas.set("cierre", cierre).set("vencimiento", vencimiento);
		} catch (Exception e) {
			return fechas.set("cierre", tarjetaCredito.fechaCierre("dd/MM/yyyy")).set("vencimiento",
					tarjetaCredito.fechaVencimiento("dd/MM/yyyy"));
		}
	}

	public static RespuestaMB consolidadaTarjetasDebito(ContextoMB contexto) {
		try {
			// LLAMADAS EN PARALELO
			Map<String, Futuro<ApiResponseMB>> futurosTarjetaDebitoGetEstado = new HashMap<>();
			Map<String, Futuro<List<Cuenta>>> futurosTarjetaDebitoCuentasAsociadas = new HashMap<>();
			Map<String, Futuro<ApiResponseMB>> futurosTarjetaDebitoTitularidad = new HashMap<>();
			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}

				String id = tarjetaDebito.numero();
				Futuro<ApiResponseMB> futuro = new Futuro<>(
						() -> TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, id));
				futurosTarjetaDebitoGetEstado.put(id, futuro);

				Futuro<List<Cuenta>> futuroCuentasAsociadas = new Futuro<>(
						() -> tarjetaDebito.cuentasAsociadasPrincipales());
				futurosTarjetaDebitoCuentasAsociadas.put(id, futuroCuentasAsociadas);

				Futuro<ApiResponseMB> futuroTitularidad = new Futuro<>(
						() -> obtenerTitularidadTd(contexto, id, "N"));
				futurosTarjetaDebitoTitularidad.put(id, futuroTitularidad);

			}
			// FIN LLAMADAS EN PARALELO

			RespuestaMB respuesta = new RespuestaMB();
			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}

				Objeto item = new Objeto();
				item.set("id", tarjetaDebito.id());
				item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
				item.set("tipoTarjeta", tarjetaDebito.idTipoTarjeta());
				item.set("limiteCompraFormateado", Formateador.importe(tarjetaDebito.limiteCompra2()));
				item.set("limiteExtraccionFormateado", Formateador.importe(tarjetaDebito.limiteExtraccion2()));
				item.set("virtual", tarjetaDebito.virtual());
				item.set("descripcionTarjeta", tarjetaDebito.producto());

				ApiResponseMB response = futurosTarjetaDebitoGetEstado.get(tarjetaDebito.numero()).get();

				String estadoTarjeta = response.hayError() ? (response.codigo == 500 ? "ERROR_LINK" : "NO_DETERMINADO") : response.string("estadoTarjeta");

				if (estadoTarjeta.toUpperCase().equals("CERRADA"))
					estadoTarjeta = "HABILITADA";

				if (estadoTarjeta == null || ((!"ERROR_LINK".equals(estadoTarjeta.toUpperCase()))
						&& (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
						&& !"INACTIVA".equals(estadoTarjeta.toUpperCase()))))
					estadoTarjeta = "NO_DETERMINADO";

				item.set("estado", estadoTarjeta);
				if (tarjetaDebito.cuentasAsociadasPrincipales() != null) {
					item.set("estadoCuentasAsociadas", "ok");
					for (Cuenta cuenta : futurosTarjetaDebitoCuentasAsociadas.get(tarjetaDebito.numero()).get()) {
						Objeto subitem = new Objeto();
						subitem.set("id", cuenta.id());
						subitem.set("descripcion", cuenta.producto());
						subitem.set("numero", cuenta.numero());
						subitem.set("ultimos4digitos", Formateador.ultimos4digitos(cuenta.numero()));
						item.add("cuentasAsociadas", subitem);
					}
				} else
					item.set("estadoCuentasAsociadas", "error");

				ApiResponseMB apiResponseTitularidad = futurosTarjetaDebitoTitularidad.get(tarjetaDebito.numero()).get();
				if (!apiResponseTitularidad.hayError()
						&& !ar.com.hipotecario.canal.homebanking.base.Objeto.anyEmpty(apiResponseTitularidad.objetos("collection1"))
						&& apiResponseTitularidad.objetos("collection1").size() != 0)
					item.set("pausada", apiResponseTitularidad.objetos("collection1").get(0).get("Pausada"));

				respuesta.add("tarjetasDebito", item);
			}

			respuesta.set("datosExtras", new Objeto()
					.set("tieneMasDeUnaCuenta", contexto.cuentas().size() > 1)
					.set("tieneMasDeUnaCuentaPesos", contexto.cuentasPesos().size() > 1)
					.set("tieneMasDeUnaCuentaDolares", contexto.cuentasDolares().size() > 1)
					.set("tieneSoloUnaTD", contexto.tarjetasDebitoRedLinkActivo().size() == 1));

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}
	
	public static RespuestaMB solicitarImpresion(ContextoMB contexto) {
		
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjeta);
		String numeroCuenta = tarjetaCredito.cuenta();
		String idTarjeta = tarjetaCredito.id();

		
		if (Objeto.anyEmpty(numeroCuenta, numeroTarjeta)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		
		ApiResponseMB apiResponse = TarjetaCreditoService.solicitarReimpresion(contexto,numeroCuenta,numeroTarjeta, idTarjeta);

		if (apiResponse.hayError()) {
			if(apiResponse.get("codigo").equals("50050")) {
				return RespuestaMB.estado("La Cuenta y tarjeta ya posee una Solicitud");
			}
			return RespuestaMB.error();
		}
		

			try {
				
				Objeto domicilio = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
				
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
//				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				parametros.set("HORA", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
				parametros.set("CANAL", "Home Banking");
				parametros.set("CALLE", domicilio.string("calle"));
				parametros.set("NUMERO",domicilio.string("numero"));
				parametros.set("PISO", domicilio.string("piso"));
				parametros.set("DEPARTAMENTO", domicilio.string("departamento"));
				parametros.set("PROVINCIA", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));

				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_imprimir_tc"), parametros));
			}
			catch(Exception e) {
				
			}
		

        return RespuestaMB.exito();
	}
	

}

