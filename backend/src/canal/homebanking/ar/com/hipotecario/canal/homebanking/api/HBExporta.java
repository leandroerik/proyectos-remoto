package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Excel;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Pdf;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;

public class HBExporta {

	private static final String REGEX = "[+-]";
	private static final String PLANTILLA_CUENTA_XLS = "/comprobantes/xlsx/movimientos-cuenta.xlsx";
	private static final String PLANTILLA_TARJETA_XLS = "/comprobantes/xlsx/movimientos-tarjeta.xlsx";
	private static final String PLANTILLA_CUENTA_PDF = "/comprobantes/movimientos-cuenta.docx";
	private static final String PLANTILLA_TARJETA_PDF = "/comprobantes/movimientos-tarjeta.docx";

	public static Respuesta exportarMovimientosCuenta(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");
		String fechaDesde = contexto.parametros.string("fechaDesde");
		String fechaHasta = contexto.parametros.string("fechaHasta");
		String descripcion = contexto.parametros.string("descripcion");
		String montoDesde = contexto.parametros.string("montoDesde");
		String montoHasta = contexto.parametros.string("montoHasta");
		String ordenar = contexto.parametros.string("ordenar");
		String tipoArchivo = contexto.parametros.string("tipoArchivo");

		Integer numeroPagina = 0;
		Integer cantidadPaginas = 0;
		List<Objeto> listaMovimientos = new ArrayList<Objeto>();
		Integer maximaCantidadPaginas = ConfigHB.integer("hb_movimientos_cuentas_max_paginas", 10);

		Cuenta cuenta = contexto.cuenta(idCuenta);
		Objeto datosExportar = new Objeto();

		Boolean superaLimitePaginas = false;
		do {
			++numeroPagina;

			if (numeroPagina > maximaCantidadPaginas) {
				superaLimitePaginas = true;
				break;
			}

			String url = cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/movimientos" : "/v1/cuentascorrientes/{idcuenta}/movimientos";
			ApiRequest request = Api.request("CuentasGetMovimientos", "cuentas", "GET", url, contexto);
			request.path("idcuenta", cuenta.numero());
			request.query("fechadesde", Fecha.formato(fechaDesde, "dd/MM/yyyy", "yyyy-MM-dd"));
			request.query("fechahasta", Fecha.formato(fechaHasta, "dd/MM/yyyy", "yyyy-MM-dd"));
			request.query("pendientes", "2");
			request.query("validactaempleado", "false");
			request.query("numeropagina", numeroPagina.toString());
			request.query("tipomovimiento", "T");
			request.query("orden", "D");

			ApiResponse response = Api.response(request, cuenta.id());
			if (response.hayError()) {
				return Respuesta.error();
			}

			for (Objeto item : response.objetos()) {
				if (!item.existe("cantPaginas")) {

					Objeto movimiento = new Objeto();
					movimiento.set("FECHA_MOV", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
					String descripcionMovimiento = HBCuenta.validarSignoParametria(item.string("descripcionMovimiento").toUpperCase());
					movimiento.set("DESCRIPCION_MOV", descripcionMovimiento);
					movimiento.set("IMPORTE_MOV", Formateador.importe(item.bigDecimal("valor")));
					movimiento.set("SALDO_MOV", Formateador.importe(item.bigDecimal("saldo")));

					listaMovimientos.add(movimiento);
				} else {
					cantidadPaginas = item.integer("cantPaginas");
				}
			}
		} while (numeroPagina < cantidadPaginas);

		datosExportar.set("MOVIMIENTO_FECHA", "Movimientos del " + fechaDesde + " al " + fechaHasta);
		datosExportar.set("CUENTA", descripcionCuenta(cuenta.descripcionCorta(), cuenta.simboloMoneda(), cuenta.ultimos4digitos()));
		datosExportar.set("IMPORTE_MONEDA", "IMPORTE EN " + cuenta.simboloMoneda());
		datosExportar.set("SALDO_MONEDA", "SALDO EN " + cuenta.simboloMoneda());

		if(superaLimitePaginas) {
			datosExportar.set("MOVIMIENTO_FECHA", "");
		}

		filtraMovimientosXFechasOrdenado(fechaDesde, fechaHasta, listaMovimientos, ordenar);
		aplicarFiltros(descripcion, montoDesde, montoHasta, "", listaMovimientos, datosExportar, ordenar, "cuenta");
		String nombreArchivo = nombreArchivo(cuenta.descripcionCorta(), cuenta.simboloMoneda(), cuenta.ultimos4digitos());

		return generaArchivo(tipoArchivo, listaMovimientos, contexto, datosExportar, nombreArchivo, "cuenta");
	}

	public static Respuesta exportarMovimientosTarjeta(ContextoHB contexto) {
		String fechaDesde = contexto.parametros.string("fechaDesde");
		String fechaHasta = contexto.parametros.string("fechaHasta");
		String descripcion = contexto.parametros.string("descripcion");
		String montoDesde = contexto.parametros.string("montoDesde");
		String montoHasta = contexto.parametros.string("montoHasta");
		String ordenar = contexto.parametros.string("ordenar");
		String tipoMoneda = contexto.parametros.string("tipoMoneda");
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", "0");
		String tipoArchivo = contexto.parametros.string("tipoArchivo");

		List<Objeto> listaMovimientos = new ArrayList<Objeto>();
		Objeto datosExportar = new Objeto();

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			tarjetaCredito = contexto.tarjetaCreditoTitular();
			if (tarjetaCredito == null) {
				return Respuesta.estado("NO_EXISTE_TARJETA_CREDITO");
			}
		}

		String tituloFecha = !fechaDesde.equals(fechaHasta) ? "Movimientos del " + fechaDesde + " al " + fechaHasta : "Movimientos al " + fechaDesde;
		datosExportar.set("MOVIMIENTO_FECHA", tituloFecha);
		datosExportar.set("TARJETA", descripcionCuenta("TC", "", tarjetaCredito.ultimos4digitos()));

		ApiResponse response = TarjetaCreditoService.movimientos(contexto, tarjetaCredito.cuenta(), tarjetaCredito.numero());
		if (response.hayError()) {
			return Respuesta.error();
		}

		for (Objeto tarjeta : response.objetos("ultimosMovimientos.tarjetas")) {
			for (Objeto itemMovimiento : tarjeta.objetos("movimientos")) {
				Objeto movimiento = new Objeto();
				movimiento.set("FECHA_MOV", itemMovimiento.string("fecha"));
				String descripcionMovimiento = itemMovimiento.string("establecimiento.nombre").replaceAll(HBTarjetas.REGEX_ESPACIOS, " ");
				movimiento.set("DESCRIPCION_MOV", descripcionMovimiento);
				if ("dolares".equals(itemMovimiento.string("descMoneda"))) {
					movimiento.set("IMPORTE_FORMAT_USD", Formateador.importe(itemMovimiento.bigDecimal("importe.dolares")));
					movimiento.set("IMPORTE_MOV", Formateador.importe(itemMovimiento.bigDecimal("importe.dolares")));
					movimiento.set("IMPORTE_FORMAT_PESOS", "");
				} else {
					movimiento.set("IMPORTE_FORMAT_PESOS", Formateador.importe(itemMovimiento.bigDecimal("importe.pesos")));
					movimiento.set("IMPORTE_MOV", Formateador.importe(itemMovimiento.bigDecimal("importe.pesos")));
					movimiento.set("IMPORTE_FORMAT_USD", "");
				}
				listaMovimientos.add(movimiento);
			}
			if (esTitularOAdicional(tipoTarjeta, idTarjetaCredito, tarjeta)) {
				break;
			}
		}

		filtraMovimientosXFechasOrdenado(fechaDesde, fechaHasta, listaMovimientos, ordenar);
		aplicarFiltros(descripcion, montoDesde, montoHasta, tipoMoneda, listaMovimientos, datosExportar, ordenar, "tarjeta");

		String nombreArchivo = nombreArchivo("TC", null, tarjetaCredito.ultimos4digitos());
		return generaArchivo(tipoArchivo, listaMovimientos, contexto, datosExportar, nombreArchivo, "tarjeta");

	}

	private static Respuesta generaArchivo(String tipoArchivo, List<Objeto> listaMovimientos, ContextoHB contexto, Objeto datosExportar, String nombreArchivo, String plantilla) {
		Respuesta respuesta = new Respuesta();
		List<List<Objeto>> particionMovimientos = new ArrayList<List<Objeto>>();

		if ("xls".equalsIgnoreCase(tipoArchivo)) {
			particionMovimientos = nParticion(listaMovimientos, listaMovimientos.size());

			XSSFWorkbook workbook = construirNPaginasExcel(contexto, particionMovimientos, datosExportar, plantilla.contains("cuenta") ? PLANTILLA_CUENTA_XLS : PLANTILLA_TARJETA_XLS);
			byte[] dataExcel = Excel.generarExcel(workbook, contexto, nombreArchivo);
			respuesta.set("data", dataExcel);
			respuesta.set("type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		} else {
			setearDatosExporta(datosExportar, "TOTAL_IMPORTE", listaMovimientos, "IMPORTE_MOV");
			if (!plantilla.contains("cuenta")) {
				setearDatosExporta(datosExportar, "TOTAL_IMPORTE_USD", listaMovimientos, "IMPORTE_FORMAT_USD");
				setearDatosExporta(datosExportar, "TOTAL_IMPORTE", listaMovimientos, "IMPORTE_FORMAT_PESOS");
			}
			contexto.responseHeader("Content-Type", "application/pdf; name=" + nombreArchivo + ".pdf");
			byte[] dataPdf = Pdf.generarPdf(datosExportar, listaMovimientos, plantilla.contains("cuenta") ? PLANTILLA_CUENTA_PDF : PLANTILLA_TARJETA_PDF);
			respuesta.set("data", dataPdf);
			respuesta.set("type", "application/pdf");
		}

		respuesta.set("description", nombreArchivo);
		return respuesta;
	}

	private static XSSFWorkbook construirNPaginasExcel(ContextoHB contexto, List<List<Objeto>> particionMovimientos, Objeto datosExportar, String plantilla) {

		XSSFWorkbook workbook = null;
		int num = 1;
		int indiceSheet = 0;
		boolean banderaClonar = true;

		if (plantilla.contains("cuenta")) {
			setearDatosExporta(datosExportar, "TOTAL_IMPORTE", particionMovimientos.get(0), "IMPORTE_MOV");
		} else {
			setearDatosExporta(datosExportar, "TOTAL_IMPORTE", particionMovimientos.get(0), "IMPORTE_FORMAT_PESOS");
			setearDatosExporta(datosExportar, "TOTAL_IMPORTE_USD", particionMovimientos.get(0), "IMPORTE_FORMAT_USD");
		}

		if (particionMovimientos.size() > 1) {
			workbook = Excel.contruyeExcel(null, contexto, particionMovimientos.get(0), datosExportar, banderaClonar, indiceSheet, plantilla);

			while (num < particionMovimientos.size()) {
				List<Objeto> listaMovimientos = (List<Objeto>) particionMovimientos.get(num);
				banderaClonar = num == particionMovimientos.size() - 1 ? false : true;

				if (plantilla.contains("cuenta")) {
					setearDatosExporta(datosExportar, "TOTAL_IMPORTE", listaMovimientos, "IMPORTE_MOV");
				} else {
					setearDatosExporta(datosExportar, "TOTAL_IMPORTE", listaMovimientos, "IMPORTE_FORMAT_PESOS");
					setearDatosExporta(datosExportar, "TOTAL_IMPORTE_USD", listaMovimientos, "IMPORTE_FORMAT_USD");
				}

				workbook = Excel.contruyeExcel(workbook, contexto, listaMovimientos, datosExportar, banderaClonar, num, plantilla);
				++num;
			}

		} else {
			workbook = Excel.contruyeExcel(null, contexto, particionMovimientos.get(0), datosExportar, !banderaClonar, indiceSheet, plantilla);
		}

		return workbook;
	}

	private static void setearDatosExporta(Objeto datosExportar, String datoExporta, List<Objeto> lista, String campo) {
		datosExportar.set(datoExporta, totalImporte(lista, campo));
	}

	private static <T> List<List<T>> nParticion(List<T> objs, final int N) {
		List<List<T>> list = new ArrayList<List<T>>();
		list.add(0, objs);

		if (!objs.isEmpty()) {
			list = new ArrayList<>(IntStream.range(0, objs.size()).boxed().collect(Collectors.groupingBy(e -> e / N, Collectors.mapping(e -> objs.get(e), Collectors.toList()))).values());
		}
		return list;
	}

	private static void ordenarPorMontoDesc(List<Objeto> ultimosMovimientos, String campo) {
		Collections.sort(ultimosMovimientos, Collections.reverseOrder(new Comparator<Objeto>() {
			public int compare(Objeto o1, Objeto o2) {
				Double a = Formateador.importe(o1.get(campo).toString().trim());
				Double b = Formateador.importe(o2.get(campo).toString().trim());
				return a.compareTo(b);
			}
		}));
	}

	private static void ordenarPorMontoAsc(List<Objeto> ultimosMovimientos, String campo) {
		Collections.sort(ultimosMovimientos, new Comparator<Objeto>() {
			public int compare(Objeto o1, Objeto o2) {
				Double a = Formateador.importe(o1.get(campo).toString().trim());
				Double b = Formateador.importe(o2.get(campo).toString().trim());
				return a.compareTo(b);
			}
		});
	}

	private static String nombreArchivo(String tipoCuenta, String simboloMoneda, String ultimos4digitos) {
		if ("USD".equalsIgnoreCase(simboloMoneda)) {
			return tipoCuenta + " " + simboloMoneda + " " + ultimos4digitos;
		}
		return tipoCuenta + " " + ultimos4digitos;
	}

	private static String descripcionCuenta(String tipoCuenta, String simboloMoneda, String ultimos4digitos) {
		ultimos4digitos = "****" + ultimos4digitos;
		if (!simboloMoneda.isEmpty()) {
			return tipoCuenta + " " + simboloMoneda + " " + ultimos4digitos;
		}

		return tipoCuenta + " " + ultimos4digitos;
	}

	private static void filtraMovimientos(String filtro, String campoImporte, String descripcion, String montoDesde, String montoHasta, String moneda, List<Objeto> listaMovimientos, Objeto datosExportar) {

		filtro = filtrarPorDescripcion(filtro, descripcion, listaMovimientos, datosExportar);
		filtro = filtrarRangoMonto(campoImporte, filtro, montoDesde, montoHasta, moneda, listaMovimientos, datosExportar);

		if (!filtro.isEmpty()) {
			datosExportar.set("TITULO_FILTRO", "Filtros Aplicados:");
			datosExportar.set("FILTROS", StringUtils.substringBeforeLast(filtro, " | "));
		} else {
			datosExportar.set("FILTROS", "");
			datosExportar.set("TITULO_FILTRO", "");
		}
	}

	private static void aplicarFiltros(String descripcion, String montoDesde, String montoHasta, String moneda, List<Objeto> listaMovimientos, Objeto datosExportar, String ordenar, String plantilla) {
		String importe = "USD".equalsIgnoreCase(moneda) ? "IMPORTE_FORMAT_USD" : ("$".equalsIgnoreCase(moneda) ? "IMPORTE_FORMAT_PESOS" : "IMPORTE_MOV");
		String tipo = ordenar.contains("importe") ? StringUtils.substringAfter(ordenar, "_") : "";
		String filtro = "";

		if (plantilla.contains("cuenta")) {
			ordenarXMontosAbsolutos(listaMovimientos, tipo, importe);
		} else {
			ordenarXMontos(listaMovimientos, tipo, importe);
		}

		filtro = filtrarMovimientosXMoneda(filtro, importe, moneda, listaMovimientos, datosExportar);
		filtraMovimientos(filtro, importe, descripcion, montoDesde, montoHasta, moneda, listaMovimientos, datosExportar);
	}

	private static void ordenarXMontos(List<Objeto> listaMovimientos, String tipo, String importe) {
		if (!tipo.isEmpty() && "asc".equalsIgnoreCase(tipo)) {
			ordenarPorMontoAsc(listaMovimientos, importe);
		}
		if (!tipo.isEmpty() && "desc".equalsIgnoreCase(tipo)) {
			ordenarPorMontoDesc(listaMovimientos, importe);
		}
	}

	private static void ordenarXMontosAbsolutos(List<Objeto> listaMovimientos, String tipo, String importe) {
		if (!tipo.isEmpty() && "asc".equalsIgnoreCase(tipo)) {
			Collections.sort(listaMovimientos, new Comparator<Objeto>() {
				public int compare(Objeto o1, Objeto o2) {
					Double a = Math.abs(Formateador.importe(o1.get(importe).toString().trim()));
					Double b = Math.abs(Formateador.importe(o2.get(importe).toString().trim()));
					return a.compareTo(b);
				}
			});
		}
		if (!tipo.isEmpty() && "desc".equalsIgnoreCase(tipo)) {
			Collections.sort(listaMovimientos, Collections.reverseOrder(new Comparator<Objeto>() {
				public int compare(Objeto o1, Objeto o2) {
					Double a = Math.abs(Formateador.importe(o1.get(importe).toString().trim()));
					Double b = Math.abs(Formateador.importe(o2.get(importe).toString().trim()));
					return a.compareTo(b);
				}
			}));
		}
	}

	private static void filtraMovimientosXFechasOrdenado(String fechaDesde, String fechaHasta, List<Objeto> listaMovimientos, String ordenar) {
		String tipo = ordenar.contains("fecha") ? StringUtils.substringAfter(ordenar, "_") : "";

		filtrarPorFecha(fechaDesde, fechaHasta, listaMovimientos);
		if (!tipo.isEmpty() && "asc".equalsIgnoreCase(tipo)) {
			Fecha.ordenarPorFechaAsc(listaMovimientos, "FECHA_MOV", "dd/MM/yyyy");
		}
		if (!tipo.isEmpty() && "desc".equalsIgnoreCase(tipo)) {
			Fecha.ordenarPorFechaDesc(listaMovimientos, "FECHA_MOV", "dd/MM/yyyy");
		}
	}

	private static String filtrarPorDescripcion(String filtro, String texto, List<Objeto> listaMovimientos, Objeto datosExportar) {
		if (texto != null && !texto.isEmpty() && !listaMovimientos.isEmpty()) {
			filtro += " '" + texto + "' | ";
			for (Iterator<Objeto> it = listaMovimientos.iterator(); it.hasNext();) {
				Objeto movimiento = it.next();
				String descripcion = movimiento.get("DESCRIPCION_MOV").toString().toLowerCase();
				if (descripcion.indexOf(texto.toLowerCase()) == -1) {
					it.remove();
				}
			}
		}
		return filtro;
	}

	private static String filtrarMovimientosXMoneda(String filtro, String importe, String moneda, List<Objeto> listaMovimientos, Objeto datosExportar) {

		if (!moneda.isEmpty() && !listaMovimientos.isEmpty()) {
			if ("USD".equals(moneda)) {
				filtro += "Dólares  | ";
			} else if ("$".equals(moneda)) {
				filtro += "Pesos  | ";
			}

			for (Iterator<Objeto> it = listaMovimientos.iterator(); it.hasNext();) {
				Objeto movimiento = it.next();
				String monto = movimiento.get(importe).toString();
				if (monto.isEmpty()) {
					it.remove();
				}
			}
		}
		return filtro;
	}

	private static String filtrarRangoMonto(String importe, String filtro, String montoDesde, String montoHasta, String moneda, List<Objeto> listaMovimientos, Objeto datosExportar) {
		String simbolo = "$_USD".equals(moneda) ? "" : moneda;

		if (validarMonto(montoDesde) || validarMonto(montoHasta) && !listaMovimientos.isEmpty()) {
			montoDesde = validaMontoTexto(montoDesde);
			montoHasta = validaMontoTexto(montoHasta);

			filtro += simbolo + " " + montoDesde + " - " + simbolo + " " + montoHasta + " | ";
			for (Iterator<Objeto> it = listaMovimientos.iterator(); it.hasNext();) {
				Objeto movimiento = it.next();
				String monto = movimiento.get(importe).toString().trim().replaceAll(REGEX, "");
				if (montoDesde.equals("0") && !montoHasta.equals("0") && Formateador.importe(monto) <= Formateador.importe(montoHasta)) {
					continue;
				} else if (!montoDesde.equals("0") && montoHasta.equals("0") && Formateador.importe(monto) >= Formateador.importe(montoDesde)) {
					continue;
				} else if (Formateador.importe(monto) >= Formateador.importe(montoDesde) && Formateador.importe(monto) <= Formateador.importe(montoHasta)) {
					continue;
				} else {
					it.remove();
				}
			}
		}

		return filtro;
	}

	private static void filtrarPorFecha(String fechaDesde, String fechaHasta, List<Objeto> listaMovimientos) {

		if (!fechaDesde.isEmpty() && !fechaHasta.isEmpty() && !fechaDesde.equals(fechaHasta) && !listaMovimientos.isEmpty()) {
			for (Iterator<Objeto> it = listaMovimientos.iterator(); it.hasNext();) {
				Objeto movimiento = it.next();
				String fecha = movimiento.get("FECHA_MOV").toString();
				if (Fecha.fechaEnRangoFecha(fecha, fechaDesde, fechaHasta, "dd/MM/yyyy")) {
					continue;
				} else {
					it.remove();
				}
			}
		}
	}

	private static boolean validarMonto(String monto) {
		return (StringUtils.isNotEmpty(monto));
	}

	private static boolean esTitularOAdicional(String tipoTarjeta, String idTarjetaCredito, Objeto tarjeta) {
		return (("T".equalsIgnoreCase(tipoTarjeta) || "A".equalsIgnoreCase(tipoTarjeta)) && idTarjetaCredito.equals(tarjeta.string("codigoTarjeta")));
	}

	private static String validaMontoTexto(String monto) {
		return (validarMonto(monto) ? monto : "0");
	}

	private static String totalImporte(List<Objeto> listaMovimientos, String importe) {
		Double totalImporte = listaMovimientos.stream().mapToDouble(movimiento -> Formateador.importe(movimiento.get(importe).toString().trim())).sum();
		String total = totalImporte == 0.0 ? "" : Formateador.importeSinRedondeo(new BigDecimal(totalImporte));
		return total;
	}

}
