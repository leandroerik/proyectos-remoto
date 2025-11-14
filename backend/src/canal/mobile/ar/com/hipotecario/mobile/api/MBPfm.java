package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.PfmService;

public class MBPfm {

	public static RespuestaMB consultarMovimientos(ContextoMB contexto) {
		String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
		String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB datos = PfmService.consultarMovimientos(contexto, fechaDesde, fechaHasta);
		if (datos.hayError()) {
			return RespuestaMB.error();
		}
		for (Objeto dato : datos.objetos()) {
			Objeto item = new Objeto();
			item.set("id", dato.string("id"));
			item.set("fechaMovimiento", dato.date("fechaMovimiento", "yyyy-MM-dd", "dd/MM/yyyy"));
			item.set("monto", Formateador.importe(dato.bigDecimal("monto")));
			item.set("moneda", dato.string("moneda"));
			item.set("idProducto", dato.string("idProducto"));
			item.set("tipoProducto", dato.string("tipoProducto"));
			item.set("signo", dato.string("signo"));
			item.set("idTipoMovimiento", dato.string("idTipoMovimiento"));
			item.set("idSubTipoMovimiento", dato.string("idSubTipoMovimiento"));
			item.set("idComercio", dato.string("idComercio"));
			item.set("idRubroComercio", dato.string("idRubroComercio"));
			item.set("codigoOperacion", dato.string("codigoOperacion"));
			item.set("descripcionProducto", dato.string("descripcionProducto"));
			item.set("descripcionMoneda", dato.string("descripcionMoneda"));
			item.set("tipoMovimiento", dato.string("tipoMovimiento"));
			item.set("subTipoMovimiento", dato.string("subTipoMovimiento"));
			item.set("descripcionRubroComercio", dato.string("descripcionRubroComercio"));
			item.set("rolClienteProducto", dato.string("rolClienteProducto"));
			item.set("idRubroPFM", dato.string("idRubroPFM"));
			item.set("descripcionMovimiento", dato.string("descripcionMovimiento"));
			respuesta.add("movimientos", item);
		}
		return respuesta;
	}

	public static RespuestaMB consultarMovimientosMensuales(ContextoMB contexto) {
		String fechaDesde = contexto.parametros.date("fechaDesde", "M/yyyy", "yyyyMM", null);
		String fechaHasta = contexto.parametros.date("fechaHasta", "M/yyyy", "yyyyMM", null);
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB datos = PfmService.consultarMovimientosMensuales(contexto, fechaDesde, fechaHasta);
		if (datos.hayError()) {
			return RespuestaMB.error();
		}
		for (Objeto dato : datos.objetos()) {
			Objeto item = new Objeto();
			item.set("fechaMovimientos", dato.date("fechaMovimientos", "yyyyMM", "MM/yyyy"));
			item.set("moneda", dato.string("moneda"));
			item.set("descripcionMoneda", dato.string("descripcionMoneda"));
			item.set("codigoPfm", dato.string("codigoPfm"));
			item.set("descripcionRubroComercio", dato.string("descripcionRubroComercio"));
			item.set("signo", dato.string("signo"));
			item.set("monto", dato.bigDecimal("monto"));
			item.set("montoFormateado", Formateador.importe(dato.bigDecimal("monto")));
			item.set("cantidad", dato.string("cantidad"));
			respuesta.add("movimientosMensuales", item);
		}
		calcularPorcentajesMensuales(fechaDesde, fechaHasta, datos, respuesta);
		return respuesta;
	}

	private static void calcularPorcentajesMensuales(String fechaDesde, String fechaHasta, ApiResponseMB datos, RespuestaMB respuesta) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
		YearMonth desde = YearMonth.parse(fechaDesde, formatter);
		YearMonth hasta = YearMonth.parse(fechaHasta, formatter);

		for (YearMonth fecha = desde; fecha.isBefore(hasta.plusMonths(1)); fecha = fecha.plusMonths(1)) {
			HashMap<BigDecimal, BigDecimal> debitoMap = new HashMap<>();
			HashMap<BigDecimal, BigDecimal> debitoUsdMap = new HashMap<>();
			HashMap<BigDecimal, BigDecimal> creditoMap = new HashMap<>();
			HashMap<BigDecimal, BigDecimal> creditoUsdMap = new HashMap<>();

			YearMonth finalFecha = fecha;

			List<Objeto> mensuales = datos.objetos().stream().filter(d -> d.date("fechaMovimientos", "yyyyMM", "yyyy-MM").equals(finalFecha.toString())).collect(Collectors.toList());
			List<Objeto> items = respuesta.objetos("movimientosMensuales").stream().filter(r -> r.date("fechaMovimientos", "MM/yyyy", "yyyy-MM").equals(finalFecha.toString())).collect(Collectors.toList());

			for (Objeto dato : mensuales) {
				if ("D".equalsIgnoreCase(dato.string("signo"))) {
					if ("2".equals(dato.string("moneda"))) {
						debitoUsdMap.put(dato.bigDecimal("monto"), dato.bigDecimal("monto"));
					} else {
						debitoMap.put(dato.bigDecimal("monto"), dato.bigDecimal("monto"));
					}
				} else {
					if ("2".equals(dato.string("moneda"))) {
						creditoUsdMap.put(dato.bigDecimal("monto"), dato.bigDecimal("monto"));
					} else {
						creditoMap.put(dato.bigDecimal("monto"), dato.bigDecimal("monto"));
					}
				}
			}
			ajustarPorcentajes(debitoUsdMap, items.stream().filter(i -> i.string("signo").equals("D") && i.string("moneda").equals("2")).collect(Collectors.toList()));
			ajustarPorcentajes(debitoMap, items.stream().filter(i -> i.string("signo").equals("D") && i.string("moneda").equals("80")).collect(Collectors.toList()));
			ajustarPorcentajes(creditoUsdMap, items.stream().filter(i -> i.string("signo").equals("C") && i.string("moneda").equals("2")).collect(Collectors.toList()));
			ajustarPorcentajes(creditoMap, items.stream().filter(i -> i.string("signo").equals("C") && i.string("moneda").equals("80")).collect(Collectors.toList()));
		}
	}

	private static void ajustarPorcentajes(HashMap<BigDecimal, BigDecimal> mapa, List<Objeto> items) {
		BigDecimal total = mapa.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

		// Ajustar porcentajes usando Largest Remainder Method
		mapa.replaceAll((k, v) -> v.divide(total, 4, RoundingMode.DOWN));

		Map<BigDecimal, BigDecimal> result = mapa.entrySet().stream().sorted(Map.Entry.comparingByValue(new Comparator<BigDecimal>() {
			@Override
			public int compare(BigDecimal o1, BigDecimal o2) {
				return (o2.subtract(o2.setScale(2, RoundingMode.DOWN)).compareTo(o1.subtract(o1.setScale(2, RoundingMode.DOWN))));
			}
		})).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		result.replaceAll((k, v) -> v.setScale(2, RoundingMode.DOWN));
		Set<BigDecimal> keys = result.keySet();
		for (BigDecimal key : keys) {
			BigDecimal roundedSum = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
			if (roundedSum.compareTo(BigDecimal.ONE) != 0) {
				result.put(key, result.get(key).add(BigDecimal.valueOf(0.01)));
			}
		}

		for (Objeto item : items) {
			item.set("porcentaje", result.get(item.bigDecimal("monto")));
		}
	}
}
