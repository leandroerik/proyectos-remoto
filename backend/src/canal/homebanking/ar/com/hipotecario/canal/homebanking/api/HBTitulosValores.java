package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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
import ar.com.hipotecario.canal.homebanking.lib.ExtractoComitenteDatosPDF;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Pdf;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaComitente;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestInversiones;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.TransmitHB;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.RescateHBBMBankProcess;

import ar.com.hipotecario.mobile.api.MBAplicacion;
import org.apache.commons.lang3.StringUtils;


public class HBTitulosValores {
	private static final String C_FORMATO_FECHA_1_DMMYYYY = "d/M/yyyy";
	private static final String C_FORMATO_FECHA_2_DDMMYYYY = "dd/MM/yyyy";
	private static final String C_FORMATO_FECHA_3_DDMMYY = "dd/MM/yy";	
	private static final String C_FORMATO_FECHA_1_YYYYMMDD = "yyyy-MM-dd";
		

	public static Respuesta tenenciaPosicionNegociable(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Objeto posicionesNegociables = buscarPosicionesNegociables(contexto, cuentaComitente.numero(), "");
		if (posicionesNegociables instanceof Respuesta) {
			return (Respuesta) posicionesNegociables;
		}

		Respuesta respuesta = new Respuesta();

		Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByProducto(contexto, "");

		for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
			Objeto itemRespuesta = new Objeto();
			String codigo = posicionNegociable.string("codigo");
			BigDecimal saldoNominal = posicionNegociable.bigDecimal("saldoDisponible");

			Objeto productoOperable = productosOperables.get(codigo);
			if (productoOperable == null) {
				itemRespuesta.set("id", codigo);
				itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
				itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
				itemRespuesta.set("cantidadNominal", saldoNominal);
				itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
				itemRespuesta.set("fecha", "");
				itemRespuesta.set("valorPesos", 0);
				itemRespuesta.set("valorPesosFormateado", "0");
				itemRespuesta.set("saldoValuadoPesos", 0);
				itemRespuesta.set("saldoValuadoPesosFormateado", "0");
				itemRespuesta.set("variacion", 0);
				itemRespuesta.set("tipoCotizacion", "SC");
				respuesta.add("tenencia", itemRespuesta);
				continue;
			}

			Objeto cotization = buscarPrecioCotizacion(contexto, productoOperable, "3");
			BigDecimal precio = cotization.bigDecimal("precio");

			BigDecimal saldoValuado = precio.multiply(saldoNominal);
			itemRespuesta.set("id", codigo);
			itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
			itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
			itemRespuesta.set("cantidadNominal", saldoNominal);
			itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
			itemRespuesta.set("fecha", cotization.string("fecha"));
			itemRespuesta.set("valorPesos", precio);
			itemRespuesta.set("valorPesosFormateado", Formateador.importeCantDecimales(precio, 4));
			itemRespuesta.set("saldoValuadoPesos", saldoValuado);
			itemRespuesta.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado));
			itemRespuesta.set("variacion", Formateador.importe(cotization.bigDecimal("variacion")));
			itemRespuesta.set("tipoCotizacion", precio.signum() != 0 ? cotization.string("tipoCotizacion") : "SC");
			respuesta.add("tenencia", itemRespuesta);
		}
		if (respuesta.get("tenencia") == null) {
			return Respuesta.estado("SIN_TENENCIA");
		}
		return respuesta;
	}

	private static Objeto buscarPosicionesNegociables(ContextoHB contexto, String cuentaComitente, String fecha) {
		String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if (!fecha.isEmpty()) {
			fechaActual = fecha;
		}

		ApiResponse posicionesNegociables = RestInversiones.obtenerPosicionesNegociables(contexto, cuentaComitente, "1000", fechaActual, 1);

//		//TODO: prueba TIMEOUT
//		if (ConfigHB.esHomologacion() && contexto.idCobis().equals("395778")) {
//			try {
//				Thread.sleep(20000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		if (!posicionesNegociables.hayError()) {
			if (posicionesNegociables.codigo == 204 || posicionesNegociables.codigo == 404) {
				return Respuesta.estado("SIN_TENENCIA");
			}
		} else {
			if (posicionesNegociables.codigo == 504 || posicionesNegociables.codigo == 500) {
				return Respuesta.estado("OPERA_MANUAL");
			}

			return Respuesta.estado("SIN_TENENCIA");
		}
		return posicionesNegociables;
	}

	private static Objeto buscarPosicionesNegociablesV3(ContextoHB contexto, String cuentaComitente, String fecha) {
		String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if (fecha != null && !fecha.isEmpty()) {
			fechaActual = fecha;
		}

		ApiResponse resp = RestInversiones.obtenerPosicionesNegociablesV2(contexto, cuentaComitente, "1000", fechaActual, 1);

		if (!resp.hayError()) {
			if (resp.codigo == 204 || resp.codigo == 404) {
				return Respuesta.estado("SIN_TENENCIA");
			}
		} else {
			if (resp.codigo == 504 || resp.codigo == 500) {
				return Respuesta.estado("OPERA_MANUAL");
			}
			return Respuesta.estado("SIN_TENENCIA");
		}
		return resp;
	}


	// Deprecada
	public static Respuesta posicionesNegociables(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		Date fechaParametro = contexto.parametros.date("fecha", C_FORMATO_FECHA_1_DMMYYYY);

		if (fechaParametro == null) {
			fechaParametro = new Date();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		ApiResponse tenenciaTitulosValores = RestInversiones.tenenciaTitulosValores(contexto, cuentaComitente.numero());
		if (tenenciaTitulosValores.hayError()) {
			if (tenenciaTitulosValores.string("codigo").equals("500") && tenenciaTitulosValores.codigo == 404) {
				// return Respuesta.estado("SIN_TENENCIA");
				// En caso de que no tenga tenencia no tendría problema en seguir.
			} else {
				return Respuesta.error();
			}
		}

		String fechaTexto = new SimpleDateFormat("yyyy-MM-dd").format(fechaParametro);
		boolean continuar = true;
		Integer secuencial = 0;
		Respuesta respuestaPosicionesNegociables = new Respuesta();
		while (continuar) {
			ApiResponse responsePosicionesNegociables = RestInversiones.tenenciaPosicionesNegociables(contexto, fechaTexto, secuencial + 1);
			if (responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas") == null || responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas").isEmpty()) {
				break;
			}
			for (Objeto item : responsePosicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
				Objeto tituloValor = RestInversiones.tituloValor(contexto, item.string("codigo"), "");

				ApiResponse responseIndicesRealTime = RestInversiones.indicesRealTime(contexto, item.string("codigo"), "", "3");

				BigDecimal valor = null;
				BigDecimal saldoValuado = null;
				BigDecimal variacion = null;
				String fecha = "";
				BigDecimal cantidadNominal = item.bigDecimal("saldoNominal");
//				String tipo = "";
//				String tipoCotizacion = "";

				if (!responseIndicesRealTime.hayError()) {
					for (Objeto itemRealTime : responseIndicesRealTime.objetos()) {
						valor = itemRealTime.bigDecimal("trade");
						variacion = itemRealTime.bigDecimal("imbalance");

						if (valor != null) {
							saldoValuado = valor.multiply(item.bigDecimal("saldoNominal"));
//							tipoCotizacion = "BYMA";
							fecha = item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
						}
					}
				}

				if (saldoValuado == null) { // en este caso tengo que buscar el saldo valuado del día anterior. Me veo
											// forzado a buscarlo en el servicio que usabamos antes
					for (Objeto tenencia : tenenciaTitulosValores.objetos()) {
						if (tenencia.string("codigoEspecie").split("-")[0].trim().equals("")) {
							valor = tenencia.bigDecimal("cotizacionSistemaNoticias");
							saldoValuado = valor.multiply(item.bigDecimal("saldoNominal")); // tenencia.bigDecimal("valorizacion");
							variacion = null;
							fecha = tenencia.date("fechaCotizacion", "yyyy-MM-dd", "dd/MM/yy");
//							tipo = tenencia.string("tipoProducto");
//							tipoCotizacion = tenencia.string("tipoCotizacion");
						}
					}
				}
				Objeto objeto = new Objeto();
				objeto.set("id", item.string("codigo"));
				objeto.set("descripcion", item.string("codigo") + " - " + item.string("descripcionTenencia"));
				objeto.set("tipoActivo", tituloValor.string("clasificacion"));
				objeto.set("tipo", ""); // TODO: tengo que pedir este campo en el servicio de posiciones negociables
				objeto.set("fecha", fecha);
				objeto.set("cantidadNominal", cantidadNominal);
				objeto.set("cantidadNominalFormateada", Formateador.importe(cantidadNominal).replace(",00", ""));
				objeto.set("valorPesos", valor);
				objeto.set("valorPesosFormateado", Formateador.importe(valor).replace(",00", ""));
				objeto.set("saldoValuadoPesos", saldoValuado);
				objeto.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado).replace(",00", ""));
				objeto.set("tipoCotizacion", ""); // TODO: tengo que pedir este campo en el servicio de posiciones negociables
				objeto.set("variacion", variacion);
				objeto.set("variacion", variacion != null && !variacion.equals(new BigDecimal("0")) ? Formateador.importe(variacion) : "-");

				objeto.set("ordenSecuencial", item.integer("ordenSecuencial"));
				objeto.set("cuentaCustodia", item.string("cuentaCustodia"));
				objeto.set("numero", item.string("numero"));

				respuestaPosicionesNegociables.add("posiciones", objeto);
				secuencial = item.integer("ordenSecuencial");
			}
		}
		return respuestaPosicionesNegociables;
	}

	public static Respuesta tenenciaVentaPosicionNegociable(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Boolean poseeCuentaAsociadaPesos = false;
		Boolean poseeCuentaAsociadaDolares = false;
		List<String> response = RestInversiones.cuentasLiquidacionMonetaria(contexto, cuentaComitente.numero());
		for (String numeroCuenta : response) {
			poseeCuentaAsociadaPesos |= numeroCuenta.startsWith("4");
			poseeCuentaAsociadaDolares |= numeroCuenta.startsWith("2");
		}

		Objeto posicionesNegociables = buscarPosicionesNegociables(contexto, cuentaComitente.numero(), "");
		if (posicionesNegociables instanceof Respuesta) {
			return (Respuesta) posicionesNegociables;
		}
		Respuesta respuesta = new Respuesta();

		List<Objeto> productosOperables = RestInversiones.obtenerProductosOperables(contexto, "");

		for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
			List<Objeto> posicionesNegociablesPorCuentasAsociadas = 
					filtrarPosicionesNegociablesPorCuentasAsociadas(
					posicionNegociable, productosOperables, poseeCuentaAsociadaPesos, poseeCuentaAsociadaDolares);
			for (Objeto posicionNegociablePorCuentasAsociadas : posicionesNegociablesPorCuentasAsociadas) {
				respuesta.add("tenencia", posicionNegociablePorCuentasAsociadas);
			}
		}
		if (respuesta.get("tenencia") == null) {
			return Respuesta.estado("SIN_TENENCIA");
		}
		return respuesta;
	}

	private static List<Objeto> filtrarPosicionesNegociablesPorCuentasAsociadas(Objeto posicionNegociable, List<Objeto> productosOperables, boolean poseeCuentaAsociadaPesos, boolean poseeCuentaAsociadaDolares) {
		List<Objeto> posicionesNegociablesPorCuentasAsociadas = new ArrayList<>();

		List<Objeto> productosOperablesPorPosicionNegociable = filtrarProductosOperablesPorPosicionNegociable(posicionNegociable.string("codigo"), productosOperables);

		for (Objeto productoOperable : productosOperablesPorPosicionNegociable) {

			if (("PESOS".equals(productoOperable.string("descMoneda")) && poseeCuentaAsociadaPesos) || ("USD".equals(productoOperable.string("descMoneda")) && poseeCuentaAsociadaDolares)) {

				String codigoProductoOperable = productoOperable.string("codigo");
				BigDecimal saldoNominal = posicionNegociable.bigDecimal("saldoDisponible");
				Objeto tenencia = new Objeto();
				tenencia.set("id", codigoProductoOperable);
				tenencia.set("descripcion", codigoProductoOperable + " - " + productoOperable.string("descripcion"));
				tenencia.set("tipoActivo", productoOperable.string("clasificacion"));
				tenencia.set("moneda", productoOperable.string("descMoneda").equals("PESOS") ? 80 : 2);
				tenencia.set("cantidadNominal", saldoNominal);
				tenencia.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
				posicionesNegociablesPorCuentasAsociadas.add(tenencia);
			}
		}
		return posicionesNegociablesPorCuentasAsociadas;
	}

	private static List<Objeto> filtrarProductosOperablesPorPosicionNegociable(String codigoPosicionNegociable, List<Objeto> productosOperables) {
		List<Objeto> productosOperablesPorCodigo = new ArrayList<>();
		for (Objeto productoOperable : productosOperables) {
			if (codigoPosicionNegociable.equals(productoOperable.string("producto"))) {
				productosOperablesPorCodigo.add(productoOperable);
			}
		}
		return productosOperablesPorCodigo;
	}


	public static Respuesta movimientos(ContextoHB contexto) {

		Respuesta respuesta = new Respuesta();

		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		Date fechaDesde = contexto.parametros.date("fechaDesde", C_FORMATO_FECHA_1_DMMYYYY);
		Date fechaHasta = contexto.parametros.date("fechaHasta", C_FORMATO_FECHA_1_DMMYYYY);
		Boolean ajustarMesCompleto = contexto.parametros.bool("ajustarMesCompleto", false);

		if (Objeto.anyEmpty(idCuentaComitente, fechaDesde, fechaHasta)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		if (ajustarMesCompleto) {
			LocalDate primerDiaDelMes = LocalDate.parse(contexto.parametros.string("fechaDesde"),
					DateTimeFormatter.ofPattern(C_FORMATO_FECHA_1_DMMYYYY));
			fechaDesde = java.sql.Date.valueOf(primerDiaDelMes.with(TemporalAdjusters.firstDayOfMonth()));

			LocalDate ultimoDiaDelMes = LocalDate.parse(contexto.parametros.string("fechaHasta"),
					DateTimeFormatter.ofPattern(C_FORMATO_FECHA_1_DMMYYYY));
			fechaHasta = java.sql.Date.valueOf(ultimoDiaDelMes.with(TemporalAdjusters.lastDayOfMonth()));
		}

		Api.eliminarCache(contexto, "MovimientosTitulosValores", contexto.idCobis(), cuentaComitente.numero());

		boolean usarV2 = "true".equals(ConfigHB.string("extracto_comitente_V2"))
				&& !HBAplicacion.esClienteExcluido(contexto.idCobis(), "extracto_comitente_V2");

		ApiResponse movimientos = usarV2
				? RestInversiones.movimientosV2(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta)
				: RestInversiones.movimientos(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);

		Objeto root = movimientos;
		if (usarV2) {
			root = movimientos.objeto("extractoComitente");
			if (root == null) {
				return Respuesta.error(); // defensivo
			}
		}

		if (movimientos.hayError()) {
			if (movimientos.string("mensajeAlUsuario").startsWith("2100-")) {
				return Respuesta.estado("SIN_MOVIMIENTOS");
			}
			return Respuesta.error();
		}

		PlazoLiquidacion plazo = null;

		List<Objeto> listMovimientos = new ArrayList<>();
		for (Objeto item : root.objetos("operaciones")) {
			String tipo = item.string("tipo").toLowerCase();

			String tipoOperacion = "";
			tipoOperacion = tipo.contains("liquidacion de cupon") ? "LICU" : tipoOperacion;
			tipoOperacion = tipo.contains("venta de especie") ? "V" : tipoOperacion;
			tipoOperacion = tipo.contains("compra de especies") ? "C" : tipoOperacion;
			tipoOperacion = tipo.contains("ividendo") && tipo.contains("fectivo") ? "DIVE" : tipoOperacion;
			tipoOperacion = tipo.contains("ividendo") && tipo.contains("ccion") ? "DIVA" : tipoOperacion;
			tipoOperacion = tipo.contains("ingreso") ? "INGR" : tipoOperacion;
			tipoOperacion = tipo.contains("egreso") ? "EGR" : tipoOperacion;

			Objeto movimiento = new Objeto();

			movimiento.set("id", item.string("idOperacion"));
			movimiento.set("id_comprobante", item.string("idOperacion") + "_" + idCuentaComitente + "_"
					+ contexto.parametros.string("fechaDesde") + "_" + contexto.parametros.string("fechaHasta"));
			movimiento.set("fechaOperacion",
					item.date("fechaConcertacion", C_FORMATO_FECHA_2_DDMMYYYY, C_FORMATO_FECHA_2_DDMMYYYY));
			movimiento.set("tipo", item.string("tipo").split(" ")[0]);
			movimiento.set("especie", item.string("especie.codigo") + " - " + item.string("especie.descripcion"));
			movimiento.set("simboloMoneda", item.string("cuentaLiquidacionME").contains("USD") ? "USD" : "$");

			Boolean esDolares = false;
			if (!item.bigDecimal("totalME", "0.0").equals(new BigDecimal("0.0"))) {
				esDolares = true;
				movimiento.set("monto", item.bigDecimal("totalME"));
				movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("totalME")));
			}

			Boolean esPesos = false;
			if (!item.bigDecimal("totalML", "0.0").equals(new BigDecimal("0.0"))) {
				esPesos = true;
				movimiento.set("monto", item.bigDecimal("totalML"));
				movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("totalML")));
			}

			if (esPesos && esDolares) {
				movimiento.set("monto", item.bigDecimal("bruto"));
				movimiento.set("montoFormateado", Formateador.importe(item.bigDecimal("bruto")));
			}

			movimiento.set("tipoOperacion", tipoOperacion);
			movimiento.set("cantidadResidual", item.string("cantidadResidualActual"));
			movimiento.set("cantidadNominal", item.string("cantidadNominal"));
			movimiento.set("cantidadNominalFormateada", Formateador.importe(item.bigDecimal("cantidadNominal", "0")));
			movimiento.set("fechaPago", item.date("fechaPago", C_FORMATO_FECHA_2_DDMMYYYY, C_FORMATO_FECHA_2_DDMMYYYY));
			
			plazo = null;
			try {
				plazo = PlazoLiquidacion.codigo(item.string("plazo"));
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if (plazo == null) {
				plazo = PlazoLiquidacion.NULO;
			}
			
			movimiento.set("plazoDeLiquidacion", plazo.toObjeto());
			movimiento.set("cantidadMinutas", item.integer("cantidadMinutas"));
			movimiento.set("fechaLiquidacion", calcularFechaLiquidacionActivo(contexto,
							movimiento.date("fechaOperacion", C_FORMATO_FECHA_2_DDMMYYYY), plazo));
			movimiento.set("numeroBoleta", item.string("numeroBoleto"));
			movimiento.set("numeroMinuta", item.string("numeroMinuta"));
			movimiento.set("porcentajeValorResidual", item.string("valorResidual"));
			movimiento.set("comision", item.bigDecimal("comision"));
			movimiento.set("comisionFormateada", Formateador.importeCantDecimales(item.bigDecimal("comision"), 2));
			movimiento.set("derechos", item.bigDecimal("derechos"));
			movimiento.set("derechosFormateado", Formateador.importeCantDecimales(item.bigDecimal("derechos"), 2));
			movimiento.set("precio", item.bigDecimal("precio"));
			movimiento.set("precioFormateado", Formateador.importeCantDecimales(item.bigDecimal("precio"), 4));

			listMovimientos.add(movimiento);
		}

		listMovimientos.sort((o1, o2) -> {
			if (o1.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY) == null
					|| o2.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY) == null)
				return 0;
			return o1.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY)
					.compareTo(o2.date("fechaOperacion", C_FORMATO_FECHA_3_DDMMYY));
		});
		Collections.reverse(listMovimientos);
		listMovimientos.forEach(o -> respuesta.add("movimientos", o));

		respuesta.set("versionVisual", usarV2 ? "v2" : "v1");
		return respuesta;
	}



	private static PlazoLiquidacion safePlazo(String p) {
		try {
			PlazoLiquidacion pl = PlazoLiquidacion.codigo(p);
			return (pl != null) ? pl : PlazoLiquidacion.NULO;
		} catch (Exception e) {
			return PlazoLiquidacion.NULO;
		}
	}

	private static String normalizarTipo(String tipo) {
		String t = tipo == null ? "" : tipo.toLowerCase();
		if (t.contains("liquidacion de cupon")) return "LICU";
		if (t.contains("venta")) return "V";
		if (t.contains("compra")) return "C";
		if (t.contains("ividendo") && t.contains("fectivo")) return "DIVE";
		if (t.contains("ividendo") && t.contains("ccion")) return "DIVA";
		if (t.contains("ingreso")) return "INGR";
		if (t.contains("egreso")) return "EGR";
		return (tipo == null) ? "" : tipo;
	}

	private static boolean esCompraOVenta(String tipo) {
		String t = tipo == null ? "" : tipo.toUpperCase();
		return "C".equals(t) || "V".equals(t) || t.contains("COMPRA") || t.contains("VENTA");
	}

	private static Respuesta mapearErroresExtracto(ApiResponse r) {
		final String code = r.string("codigo");
		if ("2034".equals(code)) return Respuesta.estado("PARAMETRO_AGRUPADO_INVALIDO");
		if ("2033".equals(code)) return Respuesta.estado("TIPO_OPERACION_NO_HABILITADA");
		if ("2001".equals(code)) return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		if ("2110".equals(code)) return Respuesta.estado("SIN_POSICION_NEGOCIABLE");
		if ("1002".equals(code) || "1006".equals(code) || "2003".equals(code)) return Respuesta.parametrosIncorrectos();
		if ("10099".equals(code)) return Respuesta.error();
		if (r.string("mensajeAlUsuario", "").startsWith("2100-")) return Respuesta.estado("SIN_MOVIMIENTOS");
		return Respuesta.error();
	}


	public static Respuesta seguimientoOperaciones(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		Date fechaDesde = contexto.parametros.date("fechaDesde", C_FORMATO_FECHA_1_DMMYYYY
								, new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L));
		Date fechaHasta = contexto.parametros.date("fechaHasta"
								, C_FORMATO_FECHA_1_DMMYYYY, new Date());

		DateFormat dateFormat = new SimpleDateFormat(C_FORMATO_FECHA_2_DDMMYYYY);
		String strFechaDesde = dateFormat.format(fechaDesde);
		String strFechaHasta = dateFormat.format(fechaHasta);

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Api.eliminarCache(contexto, "SeguimientoOperacionesTitulosValores"
				, contexto.idCobis(), cuentaComitente.numero());
		ApiResponse seguimientoOperaciones 
			= RestInversiones.seguimientoOperaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
		if (seguimientoOperaciones.hayError()) {
			if (seguimientoOperaciones.string("codigo").startsWith("2100")) {
				return Respuesta.estado("SIN_OPERACIONES");
			}
			return Respuesta.error();
		}
				
		List<Objeto> ordenes = seguimientoOperaciones.objetos("ordenesOrdenadas");
		ordenes.sort((o1, o2) -> {
			if (o1.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD) == null 
					|| o2.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD) == null)
				return 0;
			return o1.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD)
					 .compareTo(o2.date("fecha", C_FORMATO_FECHA_1_YYYYMMDD));
		});
		Collections.reverse(ordenes);
		PlazoLiquidacion plazo = null;
		List<Objeto> operaciones = new ArrayList<>();
		for (Objeto operacion : seguimientoOperaciones.objetos("ordenesOrdenadas")) {
			Objeto item = new Objeto();
			item.set("idComprobante", operacion.string("ordenSecuencial") + "_" + idCuentaComitente + "_" + operacion.string("numero") + "_" + strFechaDesde + "_" + strFechaHasta);
			item.set("fecha", operacion.date("fecha", "yyyy-MM-dd", C_FORMATO_FECHA_2_DDMMYYYY));
			item.set("tipo", operacion.string("tipo").split(" ")[0]);
			item.set("especie", operacion.string("tipoEspecie") + " - " + operacion.string("descEspecie"));
			item.set("cantidadNominalFormateada", Formateador.importe(operacion.bigDecimal("cantidadNominal"), 0));
			item.set("montoFormateado", Formateador.importe(operacion.bigDecimal("monto")));

			BigDecimal precio = Util.dividir(operacion.bigDecimal("monto"), operacion.bigDecimal("cantidadNominal"));
			item.set("precioFormateado", precio != null ? Formateador.importe(precio, 4) : "");

			item.set("estado", operacion.string("estado").split(" ")[0]);
			item.set("tipoExtendido", operacion.string("tipo"));
			item.set("estadoExtendido", operacion.string("estado"));
			// Precio
			item.set("codigoEspecie", operacion.string("tipoEspecie"));
			item.set("precioLimiteFormateado", Formateador.importeCantDecimales(operacion.bigDecimal("precioLimite"), 4));
			// Numero minuta
			item.set("numeroOrden", operacion.string("numero"));
			if (operacion.string("moneda").equals("PESOS")) {
				item.set("idMoneda", 80);
				item.set("simboloMoneda", "$");
			}
			if (operacion.string("moneda").equals("USD")) {
				item.set("idMoneda", 2);
				item.set("simboloMoneda", "USD");
			}
			
			item.set("tipoDePrecio", 
					operacion.string("precioLimite") == null || operacion.string("precioLimite").isBlank() 
							? "Precio Mercado" : "Precio Límite");
			
			plazo = null;
			try {
				plazo = PlazoLiquidacion.codigo(operacion.string("plazo"));
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if (plazo == null) {
				plazo = PlazoLiquidacion.NULO;
			}			
			item.set("plazoDeLiquidacion", plazo.toObjeto());

			item.set("fechaLiquidacion", calcularFechaLiquidacionActivo(contexto, 
					item.date("fecha", C_FORMATO_FECHA_2_DDMMYYYY), plazo));

			operaciones.add(item);
		}

		operaciones.sort((o1, o2) -> {
			if (o1.date("fecha", C_FORMATO_FECHA_3_DDMMYY) == null || o2.date("fecha", C_FORMATO_FECHA_3_DDMMYY) == null)
				return 0;
			return o1.date("fecha", C_FORMATO_FECHA_3_DDMMYY).compareTo(o2.date("fecha", C_FORMATO_FECHA_3_DDMMYY));
		});
		Collections.reverse(operaciones);
		operaciones.forEach(o -> respuesta.add("operaciones", o));

		return respuesta;
	}
	
	public static Respuesta seguimientoLicitaciones(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		Date fechaDesde = contexto.parametros.date("fechaDesde", C_FORMATO_FECHA_1_DMMYYYY, new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L));
		Date fechaHasta = contexto.parametros.date("fechaHasta", C_FORMATO_FECHA_1_DMMYYYY, new Date());

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Api.eliminarCache(contexto, "SeguimientoLicitacionesTitulosValores", contexto.idCobis(), cuentaComitente.numero()); // elimino cache ya que lo uso cada vez que consulto un comprobante
		ApiResponse seguimientoLicitaciones = RestInversiones.seguimientoLicitaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
		if (seguimientoLicitaciones.hayError() || seguimientoLicitaciones.codigo == 204) {
			if (seguimientoLicitaciones.codigo == 204) {
				return Respuesta.estado("SIN_LICITACIONES");
			}
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
		for (Objeto licitacion : seguimientoLicitaciones.objetos()) {
			Objeto item = new Objeto();

			item.set("idComprobante", licitacion.string("Licitacion") + "_" + licitacion.string("Especie") + "_" + licitacion.string("Numero") + "_" + idCuentaComitente + "_" + new SimpleDateFormat(C_FORMATO_FECHA_1_DMMYYYY).format(fechaDesde) + "_" + new SimpleDateFormat(C_FORMATO_FECHA_1_DMMYYYY).format(fechaHasta));
			item.set("licitacion", licitacion.string("LicitacionDescripcion"));
			item.set("especie", licitacion.string("EspecieDescripcion"));
			item.set("tramo", licitacion.string("Tramo"));
			item.set("numero", licitacion.string("Numero"));
			item.set("estado", licitacion.string("Estado"));
			item.set("fecha", licitacion.date("FechaConcertacion", "dd/MM/yyyy", "dd/MM/yy"));
			item.set("cuentaComitente", licitacion.string("Comitente"));
			item.set("cuentaLiquidacion", licitacion.string("Cuenta"));
			item.set("cantidadLicitada", licitacion.string("CantidadLicitada"));
			item.set("cantidadAdjudicada", licitacion.string("CantidadAdjudicada"));
			item.set("precioAdjudicadoFormateado", Formateador.importe(licitacion.bigDecimal("PrecioAdjudicado")));
			respuesta.add("licitaciones", item);
		}

		return respuesta;
	}

	public static byte[] comprobanteLicitaciones(ContextoHB contexto) {
		String idComprobante = contexto.parametros.string("idComprobante");

		// VEP_2019-05-25_2019-06-25_20105176512_034000038777
		String codigoLicitacion = idComprobante.split("_")[0];
		String codigoEspecie = idComprobante.split("_")[1];
		String numero = idComprobante.split("_")[2];
		String idCuentaComitente = idComprobante.split("_")[3];
		Date fechaDesde;
		Date fechaHasta;
		try {
			fechaDesde = new SimpleDateFormat(C_FORMATO_FECHA_1_DMMYYYY).parse(idComprobante.split("_")[4]);
			fechaHasta = new SimpleDateFormat(C_FORMATO_FECHA_1_DMMYYYY).parse(idComprobante.split("_")[5]);
		} catch (ParseException e) {
			return null;
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return null;
		}

		Api.eliminarCache(contexto, "SeguimientoLicitacionesTitulosValores", contexto.idCobis(), cuentaComitente.numero()); // elimino cache ya que lo uso cada vez que consulto un comprobante
		ApiResponse seguimientoLicitaciones = RestInversiones.seguimientoLicitaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
		if (seguimientoLicitaciones.hayError()) {
			return null;
		}

		String descripcionLicitacion = "";
		String descripcionEspecie = "";
		Integer cantidad = Integer.valueOf(0);
		BigDecimal valor = new BigDecimal(0);
		String tramo = "";
		String cuentaComitenteNumero = "";
		String cuentaNumero = "";
		String fecha = "";

		boolean encontro = false;

		for (Objeto item : seguimientoLicitaciones.objetos()) {
			if (item.string("Licitacion").equals(codigoLicitacion) && item.string("Especie").equals(codigoEspecie) && item.string("Numero").equals(numero)) {
				fecha = item.string("FechaConcertacion") + " " + item.string("HoraConcertacion");
				descripcionLicitacion = item.string("LicitacionDescripcion");
				descripcionEspecie = item.string("EspecieDescripcion");
				cantidad = item.integer("CantidadAdjudicada");
				valor = item.bigDecimal("PrecioAdjudicado");
				tramo = item.string("Tramo");
				cuentaComitenteNumero = cuentaComitente.numero();
				cuentaNumero = item.string("Cuenta");

				encontro = true;
				break;
			}
		}

		if (!encontro) {
			return null;
		}

		String idComprobanteResultado = HBInversion.generarComprobanteLicitacion(contexto, numero, fecha, codigoLicitacion, descripcionLicitacion, codigoEspecie, descripcionEspecie, cantidad, valor, tramo, cuentaComitenteNumero, cuentaNumero);
		contexto.parametros.set("id", idComprobanteResultado);
		return HBArchivo.comprobante(contexto);
	}

	/* ========== MERCADO SECUNDARIO ========== */
	public static Respuesta tiposActivo(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		respuesta.add("tipos", new Objeto().set("id", "Accion").set("descripcion", "Acciones"));
		respuesta.add("tipos", new Objeto().set("id", "CHA").set("descripcion", "Cédula Hipotecaria"));
		respuesta.add("tipos", new Objeto().set("id", "Cedear").set("descripcion", "Cedears"));
		respuesta.add("tipos", new Objeto().set("id", "Titulo Publico").set("descripcion", "Bonos y Obligaciones Negociables"));

		Objeto tiposActivos = new Objeto();
		for (Objeto tipo : respuesta.objetos("tipos")) {
			tiposActivos.add(tipo);
		}
		tiposActivos.ordenar("descripcion");
		respuesta.set("tipos", tiposActivos);

		return respuesta;
	}

	public static Respuesta especies(ContextoHB contexto) {
		String idTipoActivo = contexto.parametros.string("idTipoActivo");
		
		ApiResponse response = RestInversiones.titulosValores(contexto, idTipoActivo);
		if (response.hayError()) {
			return Respuesta.error();
		}

		List<Objeto> resultado = filtrarEspecies(contexto, response.objetos("productosOperablesOrdenados"));
		
		Objeto especies = new Objeto();
		if (resultado.isEmpty()) {
			return Respuesta.exito("especies", new ArrayList<>());
		} 

		for (Objeto item : resultado) {
			Objeto especie = new Objeto();
			especie.set("id", item.string("codigo"));
			especie.set("idTipoActivo", item.string("clasificacion"));
			especie.set("descripcion", item.string("codigo") + " - " + item.string("descripcion"));
			especie.set("idMoneda", item.string("descMoneda").equals("PESOS") ? 80 : 2);
			especies.add(especie);
		}
		especies.ordenar("descripcion");
		
		return Respuesta.exito("especies", especies);
	}
	
	public static Respuesta detalleEspecieProductosOperables(ContextoHB contexto) {
		String idEspecie = contexto.parametros.string("idEspecie");
		String idPlazo = contexto.parametros.string("idPlazo");

		if (Objeto.anyEmpty(idEspecie)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (!idPlazo.isEmpty()) {
			switch (idPlazo) {
				case "0":
					idPlazo = "1";
					break;
				case "24":
					idPlazo = "2";
					break;
				case "48":
					idPlazo = "3";
					break;
				default:
					break;
			}
		}

		Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto, "");
		Objeto productoOperable = productosOperables.get(idEspecie);

		boolean esAccion = "Accion".equals(productoOperable.string("clasificacion"));

		Objeto cotization = buscarPrecioCotizacion(contexto, productoOperable, idPlazo.isEmpty() ? "1" : idPlazo);
		BigDecimal precio = cotization.bigDecimal("precio");

		BigDecimal precioMinimo = precio.multiply(esAccion ? new BigDecimal("0.9") : new BigDecimal("0.9")).setScale(4, RoundingMode.UP);
		BigDecimal precioMaximo = precio.multiply(esAccion ? new BigDecimal("1.1") : new BigDecimal("1.1")).setScale(4, RoundingMode.DOWN);
		BigDecimal montoMinimo = productoOperable.bigDecimal("montoMinimo");
		BigDecimal montoMaximo = productoOperable.bigDecimal("montoMaximo");
		BigDecimal variacion = cotization.bigDecimal("variacion", "0");

		Long cantidadMinima = precioMinimo.signum() != 0 ? montoMinimo.divide(precioMinimo, RoundingMode.UP).longValue() : null;
		Long cantidadMaxima = precioMaximo.signum() != 0 ? montoMaximo.divide(precioMaximo, RoundingMode.UP).longValue() - 1 : null;

		Objeto datos = new Objeto();
		datos.set("precio", precio);
		datos.set("precioFormateado", Formateador.importeCantDecimales(precio, 4));
		datos.set("precioMinimo", precioMinimo);
		datos.set("precioMinimoFormateado", Formateador.importeCantDecimales(precioMinimo, 4));
		datos.set("precioMaximo", precioMaximo);
		datos.set("precioMaximoFormateado", Formateador.importeCantDecimales(precioMaximo, 4));
		datos.set("montoMinimo", montoMinimo);
		datos.set("montoMinimoFormateado", Formateador.importeCantDecimales(montoMinimo, 4));
		datos.set("montoMaximo", montoMaximo);
		datos.set("montoMaximoFormateado", Formateador.importeCantDecimales(montoMaximo, 4));
		datos.set("cantidadMinima", cantidadMinima);
		datos.set("cantidadMinimaFormateada", Formateador.entero(cantidadMinima));
		datos.set("cantidadMaxima", cantidadMaxima);
		datos.set("cantidadMaximaFormateada", Formateador.entero(cantidadMaxima));
		datos.set("fecha", cotization.string("fecha"));
		datos.set("esPrecioRealTime", cotization.bool("esByma"));
		datos.set("variacion", variacion);
		datos.set("variacionFormateada", Formateador.importeCantDecimales(variacion, 2));

		return Respuesta.exito("datos", datos);
	}

	public static Respuesta plazosValidos(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		Boolean habilitado = enHorarioDolarMep(contexto).get("estado").equals("FUERA_HORARIO") ? false : true;

		Set<String> plazosHabilitados = Objeto.setOf(ConfigHB.string("plazos_liquidacion_habilitados").split("_"));
		// TODO GB Usar enum PlazoLiquidacion
		Boolean defaultSeteado = false;
		
		if (plazosHabilitados.contains("48")) {			
			respuesta.add("tipos",
					new Objeto().set("id", "48").set("descripcion", "48hs").set("habilitado", true).set("default", defaultSeteado ? false : true));
			defaultSeteado = true;
		}
		
		if (plazosHabilitados.contains("24")) {
			respuesta.add("tipos",
					new Objeto().set("id", "24").set("descripcion", "24hs").set("habilitado", true).set("default", defaultSeteado ? false : true));
			defaultSeteado = true;
		}
		
		if (plazosHabilitados.contains("0")) {
			respuesta.add("tipos", new Objeto().set("id", "0").set("descripcion", "Contado Inmediato (CI)")
					.set("habilitado", habilitado).set("default", defaultSeteado ? false : true));
			defaultSeteado = true;
		}

		return respuesta;
	}

	public static Respuesta cuentasAsociadasComitente(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idMoneda = contexto.parametros.string("idMoneda", null);

		if (Objeto.anyEmpty(idCuentaComitente)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
//		ApiResponse response = InversionesService.inversionesGetCuentasPorComitente(contexto, cuentaComitente.numero());
//		if (response.hayError()) {
//			return Respuesta.error();
//		}

		List<String> response = RestInversiones.cuentasLiquidacionMonetaria(contexto, cuentaComitente.numero());
		if (response == null) {
			return Respuesta.error();
		}

		Boolean tieneCuentaAsociada = false;
		Respuesta respuesta = new Respuesta();
		for (String numero : response) {
			Cuenta cuenta = contexto.cuenta(numero);
			if (cuenta != null && (idMoneda == null || idMoneda.equals(cuenta.idMoneda()))) {
				Objeto item = new Objeto();
				//item.set("id", cuenta.id());
				item.set("id", cuenta.idEncriptado());
				item.set("descripcion", cuenta.producto());
				item.set("descripcionCorta", cuenta.descripcionCorta());
				item.set("numeroFormateado", cuenta.numeroFormateado());
				item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
				item.set("ultimos4digitos", cuenta.ultimos4digitos());
				item.set("titularidad", cuenta.titularidad());
				item.set("idMoneda", cuenta.idMoneda());
				item.set("moneda", cuenta.moneda());
				item.set("simboloMoneda", cuenta.simboloMoneda());
				item.set("estado", cuenta.descripcionEstado());
				item.set("saldo", cuenta.saldo());
				item.set("saldoFormateado", cuenta.saldoFormateado());
				item.set("acuerdo", cuenta.acuerdo());
				item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
				item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
				item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
				respuesta.add("cuentas", item);
				tieneCuentaAsociada = true;
			}
		}

		if (!tieneCuentaAsociada) {
			return Respuesta.estado("SIN_CUENTA_ASOCIADA");
		}

		return respuesta;
	}

	public static Respuesta simularCompra(ContextoHB contexto) {
		String version = contexto.parametros.string("version");
		if (version == null || version.isBlank() || version.equals("1")) {
			return simularCompraV1(contexto);
		} 
		// INV-692
		return simularCompraV2(contexto);
	}
		
	public static Respuesta simularCompraV2(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
		String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
		Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
		if (tipoPrecio.isPrecioLimite()) {
			if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
				return Respuesta.estado("PRECIO_LIMITE_INVALIDO");
			}

			precioLimiteOperacion = precioLimite;
		} else if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacionFuturo = new Futuro<>(
					() -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA));
		}

		List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto, "");
		Objeto productoOperable = productosOperables.get(idEspecie);

		ApiRequest request = Api.request("SimularCompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", productoOperable.string("descMoneda"));
		request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
		request.body("plazo", plazo.codigo());
		if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacion = precioLimiteOperacionFuturo.get();
			if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
				return Respuesta.estado("PRECIO_MERCADO_INVALIDO");
			}
		}
		request.body("precioLimite", precioLimiteOperacion);
		request.body("tipo", "Compra");
		request.body("tipoServicio", "Consulta");
		request.body("vigencia", 0);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2122")) {
				Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			if (response.string("codigo").equals("2020")) {
				Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}

			return Respuesta.error();
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("comisiones", response.bigDecimal("comisiones"));
		orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
		orden.set("vigencia", "por el día");
		orden.set("precioMercado", precioMercado);
		orden.set("cantidadNominal", cantidadNominal);
		return Respuesta.exito("orden", orden);
	}

	public static Respuesta calcularCantidadNominalCompra(ContextoHB contexto) {

		BigDecimal comisionCompra = new BigDecimal(BigInteger.ZERO);
		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		BigDecimal importeIngresadoPorUsuario = contexto.parametros.bigDecimal("importeIngresadoPorUsuario");
		if (importeIngresadoPorUsuario == null || importeIngresadoPorUsuario.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("IMPORTE_INVALIDO");
		}

		String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));

		Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie, fecha);
		String tipoInstrumento = datosTituloValor.string("clasificacion");
		comisionCompra = calcularComisionPorTipo(tipoInstrumento);

		BigDecimal montoComision = importeIngresadoPorUsuario
				.multiply(comisionCompra)
				.setScale(2, RoundingMode.HALF_DOWN);

		importeIngresadoPorUsuario = importeIngresadoPorUsuario
				.subtract(montoComision);


		BigDecimal precioMercado = calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA);
		if (precioMercado.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("PRECIO_MERCADO_NO_DISPONIBLE");
		}

		BigDecimal cantidadNominal = importeIngresadoPorUsuario.divide(precioMercado, 0, RoundingMode.FLOOR);
		if (cantidadNominal.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("CANTIDAD_NOMINAL_INVALIDA");
		}

		contexto.parametros.set("cantidadNominal", cantidadNominal);


		return simularCompraV2(contexto);
	}

	public static Respuesta calcularCantidadNominalCompraMep(ContextoHB contexto) {

		BigDecimal comisionCompra = new BigDecimal(BigInteger.ZERO);
		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		BigDecimal importeIngresadoPorUsuario = contexto.parametros.bigDecimal("importeIngresadoPorUsuario");
		if (importeIngresadoPorUsuario == null || importeIngresadoPorUsuario.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("IMPORTE_INVALIDO");
		}

		String idEspecie = StringUtils.substringBefore(contexto.parametros.string("idEspecie"), "_");
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));

		Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie, fecha);
		String tipoInstrumento = datosTituloValor.string("clasificacion");
		comisionCompra = calcularComisionPorTipo(tipoInstrumento);

		BigDecimal montoComision = importeIngresadoPorUsuario
				.multiply(comisionCompra)
				.setScale(2, RoundingMode.HALF_DOWN);

		importeIngresadoPorUsuario = importeIngresadoPorUsuario
				.subtract(montoComision);

		BigDecimal precioMercado = calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA);
		if (precioMercado.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("PRECIO_MERCADO_NO_DISPONIBLE");
		}

		BigDecimal cantidadNominal = importeIngresadoPorUsuario.divide(precioMercado, 0, RoundingMode.FLOOR);
		if (cantidadNominal.compareTo(BigDecimal.ZERO) <= 0) {
			return Respuesta.estado("CANTIDAD_NOMINAL_INVALIDA");
		}

		contexto.parametros.set("cantidadNominal", cantidadNominal);


		return comprarV2(contexto);
	}

	public static BigDecimal calcularComisionPorTipo(String tipoInstrumento) {

		BigDecimal comision_general_acciones_cedears = ConfigHB.bigDecimal("inv_comision_general_acciones_cedears");
		BigDecimal comision_general_bonos = ConfigHB.bigDecimal("inv_comision_general_bonos");

		switch (tipoInstrumento) {
			case "Accion", "Cedear":
				return comision_general_acciones_cedears;
			case "CHA", "Titulo Publico":
				return comision_general_bonos;
			default:
				throw new IllegalArgumentException("Tipo de instrumento inválido: " + tipoInstrumento);
		}
	}

	
	public static Respuesta simularVenta(ContextoHB contexto) {
		String version = contexto.parametros.string("version");

		contexto.sesion.setChallengeOtp(false);

		if (version == null || version.isBlank() || version.equals("1")) {
			return simularVentaV1(contexto);
		}
		// INV-692
		return simularVentaV2(contexto);
	}
		
	public static Respuesta simularVentaV2(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
		Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
		Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
		if (tipoPrecio.isPrecioLimite()) {
			if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
				return Respuesta.estado("PRECIO_LIMITE_INVALIDO");
			}

			precioLimiteOperacion = precioLimite;
		} else if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacionFuturo = new Futuro<>(
					() -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.VENTA));
		}
		
		List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		ApiRequest request = Api.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
		request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
		request.body("plazo", plazo.codigo());
		request.body("tipo", "Venta");
		request.body("tipoServicio", "Consulta");
		request.body("vigencia", 0);
		if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacion = precioLimiteOperacionFuturo.get();
			if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
				return Respuesta.estado("PRECIO_MERCADO_INVALIDO");
			}
		}
		request.body("precioLimite", precioLimiteOperacion);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2122")) {
				Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			if (response.string("codigo").equals("2020")) {
				Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			if (response.string("codigo").equals("2014")) {
				if (response.string("mensajeAlUsuario").contains("no es habil")) {
					Respuesta respuesta = Respuesta.estado("DIA_NO_HABIL");
					respuesta.set("mensaje", response.string("mensajeAlUsuario"));
					return respuesta;
				}
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensaje", response.string("mensajeAlUsuario"));
				return respuesta;
			}

			return Respuesta.error();
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("comisiones", response.bigDecimal("comisiones"));
		orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
		orden.set("vigencia", "por el día");
		orden.set("precioMercado", precioMercado);
		return Respuesta.exito("orden", orden);
	}
	
	
	public static Respuesta comprar(ContextoHB contexto) {
		String version = contexto.parametros.string("version");
		if (version == null || version.isBlank() || version.equals("1")) {
			return comprarV1(contexto);
		}
		// INV-692
		return comprarV2(contexto);
	}
	
	public static Respuesta comprarV2(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
		final String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);
		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Boolean mostrarLeyendaTituloPublico = contexto.parametros.bool("mostrarLeyendaTituloPublico", false);

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		Cuenta cuenta = contexto.cuenta(idCuenta);

		Futuro<List<String>> cuentasLiquidacionTituloFuturo = new Futuro<>(
				() -> RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda()));

		Futuro<Map<String, Objeto>> productosOperablesFuturo = new Futuro<>(
				() -> RestInversiones.obtenerProductosOperablesMap(contexto, ""));

		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
		Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
		if (tipoPrecio.isPrecioLimite()) {
			if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
				return Respuesta.estado("PRECIO_LIMITE_INVALIDO");
			}

			precioLimiteOperacion = precioLimite;
		} else if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacionFuturo = new Futuro<>(
					() -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.COMPRA));
		}

		List<String> cuentasLiquidacionTitulo = cuentasLiquidacionTituloFuturo.get();
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error()
					: Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		Map<String, Objeto> productosOperables = productosOperablesFuturo.get();
		Objeto productoOperable = productosOperables.get(idEspecie);

		Respuesta resultado = HBInversion.validarPerfilInversor(contexto,
				Optional.of(HBInversion.EnumPerfilInversor.ARRIESGADO), operaFueraPerfil);
		/*
		 * if (resultado.hayError()) { Respuesta respuesta =
		 * Respuesta.estado("ERROR_FUNCIONAL"); respuesta.set("mensaje",
		 * "Perfil inversor incorrecto para operar"); return respuesta; }
		 */
		Boolean operarFueraPerfilEstaTransaccion = resultado.bool("operaBajoPropioRiesgo");

		ApiRequest request = Api.request("CompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", productoOperable.string("descMoneda"));
		request.body("operaFueraDePerfil", operarFueraPerfilEstaTransaccion ? "SI" : "NO");
		request.body("plazo", plazo.codigo());
		request.body("tipo", "Compra");
		request.body("tipoServicio", "Operacion");
		request.body("vigencia", 0);
		if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacion = precioLimiteOperacionFuturo.get();
			if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
				return Respuesta.estado("PRECIO_MERCADO_INVALIDO");
			}
		}
		request.body("precioLimite", precioLimiteOperacion);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2009")) {
				Respuesta respuestaError = new Respuesta();
				respuestaError.set("estado", "FUERA_HORARIO");
				respuestaError.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuestaError;
			}
			if (response.string("codigo").equals("5000")) {
				return Respuesta.estado("FONDOS_INSUFICIENTES");
			}
			if (response.string("codigo").equals("2013")) {
				return Respuesta.estado("PLAZO_INVALIDO");
			}
			if (response.string("codigo").equals("2025")) {
				return Respuesta.estado("SIN_PERFIL_INVERSOR");
			}
			if (response.string("codigo").equals("2023")) {
				return Respuesta.estado("OPERACION_ARRIESGADA");
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			return Respuesta.error();
		}

		try {
			String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

			String descripcionError = "";
			if (response != null && !codigoError.equals("0")) {
				descripcionError += response.string("codigo") + ".";
				descripcionError += response.string("mensajeAlUsuario") + ".";
			}
			descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

			SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
			sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
			sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.add(new Date()); // momento
			sqlRequest.add(contexto.idCobis()); // cobis
			sqlRequest.add(request.idProceso()); // idProceso
			sqlRequest.add(request.ip()); // ip
			sqlRequest.add("HB"); // canal
			sqlRequest.add(codigoError); // codigoError
			sqlRequest.add(descripcionError); // descripcionError

			sqlRequest.add("Compra"); // operacion
			sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
			sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
			sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
			sqlRequest.add(idEspecie); // especie
			sqlRequest.add(productoOperable.string("descMoneda")); // moneda
			sqlRequest.add(operarFueraPerfilEstaTransaccion ? "SI" : "NO"); // operaFueraDePerfil
			sqlRequest.add(plazo.codigo()); // plazo
			sqlRequest.add(precioLimiteOperacion); // precioLimite
			sqlRequest.add(cantidadNominal); // cantidadNominal
			sqlRequest.add("0"); // vigencia
			sqlRequest.add(response.string("idOrden")); // idOrden
			sqlRequest.add(response.string("numeroOrden")); // numeroOrden
			sqlRequest.add(response.string("comisiones")); // comisiones

			// EMM: para algunos casos desde el front cuando es un título público
			// y a parte es en pesos se muestra una leyenda específica.
			if (mostrarLeyendaTituloPublico) {
				sqlRequest.add(cuenta.esDolares() ? "6" : "12"); // versionDDJJ
			} else {
				sqlRequest.add(cuenta.esDolares() ? "6" : ""); // versionDDJJ
			}

			Sql.response(sqlRequest);
		} catch (Exception e) {
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("numero", response.string("numeroOrden"));

		// emm-20190613-desde--> Comprobante
		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("COMPROBANTE", response.string("numeroOrden"));
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
		comprobante.put("ESPECIE", idEspecie + " - " + productoOperable.string("descripcion"));
		String moneda = "$";
		if (!"PESOS".equals(productoOperable.string("descMoneda"))) {
			moneda = "USD";
		}
		comprobante.put("IMPORTE",
				moneda + " " + Formateador.importe(precioLimiteOperacion.multiply(new BigDecimal(cantidadNominal))));
		comprobante.put("TIPO_OPERACION", "Compra");
		comprobante.put("TIPO_ACTIVO", productoOperable.string("clasificacion")); // todo necesito la accion que eligió
																					// el cliente
		comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimiteOperacion));
		comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
		comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
		comprobante.put("CUENTA", cuenta.numero());
		comprobante.put("PLAZO", plazo.descripcion());
		comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
		comprobante.put("VIGENCIA", "0");
		String idComprobante = "titulo-valor-compra_" + response.string("idOrden");
		contexto.sesion.comprobantes.put(idComprobante, comprobante);
		// emm-20190613-hasta--> Comprobante

		try {
			for (String email : ConfigHB.string("mercadosecundario_email").split(";")) {
				if (!email.trim().isEmpty()) {
					String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
					String mensaje = "<html><head></head><body>";
					mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
					mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
					mensaje += "<b>Operación:</b> " + "Compra" + "<br/>";
					mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
					mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
					mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
					mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
					mensaje += "</body></html>";

					// AGREGAR SALESFORCE - COMPRA VENTA DOLAR MEP
					if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
						Objeto parametros = new Objeto();
						parametros.set("IDCOBIS", contexto.idCobis());
						parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
						parametros.set("APELLIDO", contexto.persona().apellido());
						parametros.set("NOMBRE", contexto.persona().nombre());
						parametros.set("CANAL", "Home Banking");
						parametros.set("ASUNTO", asunto);
						parametros.set("MENSAJE", mensaje);
						parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
						parametros.set("EMAIL_DESTINO", email);
						parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
						parametros.set("TIPO_OPERACION", "Compra");

						if (idEspecie.equals("AL30")) {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_dolar_mep"), parametros));
						} else {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_bonos_acciones"), parametros));
						}
					} else {
						ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
						requestMail.body("de", "aviso@mail-hipotecario.com.ar");
						requestMail.body("para", email.trim());
						requestMail.body("plantilla", ConfigHB.string("doppler_generico"));
						Objeto parametros = requestMail.body("parametros");
						parametros.set("ASUNTO", asunto);
						parametros.set("BODY", mensaje);
						Api.response(requestMail);
					}
				}
			}
		} catch (Exception e) {
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("idComprobante", "titulo-valor-compra_" + response.string("idOrden"));
		respuesta.set("orden", orden);
		return respuesta;
	}
	
	public static Respuesta venderV2(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		final String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		TipoPrecioOperacion tipoPrecio = TipoPrecioOperacion.id(contexto.parametros.integer("idTipoPrecio"));
		PlazoLiquidacion plazo = PlazoLiquidacion.codigo(contexto.parametros.string("plazo"));
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}
		
		BigDecimal precioLimiteOperacion = BigDecimal.ZERO;
		Futuro<BigDecimal> precioLimiteOperacionFuturo = null;
		if (tipoPrecio.isPrecioLimite()) {
			if (precioLimite == null || precioLimite.compareTo(BigDecimal.ZERO) <= 0) {
				return Respuesta.estado("PRECIO_LIMITE_INVALIDO");
			}

			precioLimiteOperacion = precioLimite;
		} else if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacionFuturo = new Futuro<>(
					() -> calcularPrecioMercadoTope(contexto, idEspecie, plazo, TipoOperacionInversion.VENTA));
		}

		List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		ApiRequest request = Api.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
		request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
		request.body("plazo", plazo.codigo());
		request.body("tipo", "Venta");
		request.body("tipoServicio", "Operacion");
		request.body("vigencia", 0);		
		if (tipoPrecio.isPrecioMercado()) {
			precioLimiteOperacion = precioLimiteOperacionFuturo.get();
			if (precioLimiteOperacion.equals(BigDecimal.ZERO)) {
				return Respuesta.estado("PRECIO_MERCADO_INVALIDO");
			}
		}
		request.body("precioLimite", precioLimiteOperacion);

		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_rescate_fondos",
				"prendido_modo_transaccional_rescate_fondos_cobis") && !TransmitHB.isChallengeOtp(contexto, "venta-acciones-bonos")) {
			try {
				String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
				if (Objeto.empty(sessionToken))
					return Respuesta.parametrosIncorrectos();

				boolean esBono = false;
				Respuesta respuestaEspecies;
				contexto.parametros.set("idTipoActivo", "Titulo Publico");
				respuestaEspecies = especies(contexto);
				if (!respuestaEspecies.hayError() && !Objeto.empty(respuestaEspecies.objetos("especies").stream().filter(e -> e.string("id").split("_")[0].equals(idEspecie)).findAny().orElse(null)))
					esBono = true;

				RescateHBBMBankProcess rescateHBBMBankProcess = new RescateHBBMBankProcess(contexto.idCobis(),
						sessionToken,
						precioLimiteOperacion,
						Util.obtenerDescripcionMonedaTransmit(cuenta.idMoneda()),
						esBono ? TransmitHB.REASON_VENTA_BONOS : TransmitHB.REASON_VENTA_ACCIONES,
						new RescateHBBMBankProcess.Payer(contexto.persona().cuit(), cuenta.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
						new RescateHBBMBankProcess.Payee(contexto.persona().cuit(), "", ""));

				Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, rescateHBBMBankProcess, "venta-acciones-bonos");
				if (respuesta.hayError())
					return respuesta;

			} catch (Exception e) {
			}
		}

		if (TransmitHB.isChallengeOtp(contexto, "venta-acciones-bonos") && !contexto.validaSegundoFactor("venta-acciones-bonos"))
			return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2009")) {
				Respuesta respuestaError = new Respuesta();
				respuestaError.set("estado", "FUERA_HORARIO");
				respuestaError.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuestaError;
			}
			if (response.string("codigo").equals("5000")) {
				return Respuesta.estado("FONDOS_INSUFICIENTES");
			}
			if (response.string("codigo").equals("2013")) {
				return Respuesta.estado("PLAZO_INVALIDO");
			}
			if (response.string("codigo").equals("2025")) {
				return Respuesta.estado("SIN_PERFIL_INVERSOR");
			}
			if (response.string("codigo").equals("2023")) {
				return Respuesta.estado("OPERACION_ARRIESGADA");
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			return Respuesta.error();
		}

		try {
			String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

			String descripcionError = "";
			if (response != null && !codigoError.equals("0")) {
				descripcionError += response.string("codigo") + ".";
				descripcionError += response.string("mensajeAlUsuario") + ".";
			}
			descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

			SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
			sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
			sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.add(new Date()); // momento
			sqlRequest.add(contexto.idCobis()); // cobis
			sqlRequest.add(request.idProceso()); // idProceso
			sqlRequest.add(request.ip()); // ip
			sqlRequest.add("HB"); // canal
			sqlRequest.add(codigoError); // codigoError
			sqlRequest.add(descripcionError); // descripcionError

			sqlRequest.add("Venta"); // operacion
			sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
			sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
			sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
			sqlRequest.add(idEspecie); // especie
			sqlRequest.add(cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null); // moneda
			sqlRequest.add(operaFueraPerfil ? "SI" : "NO"); // operaFueraDePerfil
			sqlRequest.add(plazo.codigo()); // plazo
			sqlRequest.add(precioLimiteOperacion); // precioLimite
			sqlRequest.add(cantidadNominal); // cantidadNominal
			sqlRequest.add("0"); // vigencia
			sqlRequest.add(response.string("idOrden")); // idOrden
			sqlRequest.add(response.string("numeroOrden")); // numeroOrden
			sqlRequest.add(response.string("comisiones")); // comisiones
			sqlRequest.add(cuenta.esDolares() ? "11" : "9"); // versionDDJJ

			Sql.response(sqlRequest);
		} catch (Exception e) {
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("numero", response.string("numeroOrden"));

		// emm-20190613-desde--> Comprobante
		Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie, "");

		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("COMPROBANTE", response.string("numeroOrden"));
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
		comprobante.put("ESPECIE", idEspecie + " - " + datosTituloValor.string("descripcion"));
		String moneda = cuenta.esPesos() ? "$" : cuenta.esDolares() ? "USD" : "";
		comprobante.put("IMPORTE", moneda + " " + Formateador.importe(precioLimiteOperacion.multiply(new BigDecimal(cantidadNominal))));
		comprobante.put("TIPO_OPERACION", "Venta");
		comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion")); // todo necesito la accion que eligió el cliente
		comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimiteOperacion));
		comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
		comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
		comprobante.put("CUENTA", cuenta.numero());
		comprobante.put("PLAZO", plazo.descripcion());
		comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
		comprobante.put("VIGENCIA", "0");
		String idComprobante = "titulo-valor-venta_" + response.string("idOrden");
		contexto.sesion.comprobantes.put(idComprobante, comprobante);
		// emm-20190613-hasta--> Comprobante

		try {
			for (String email : ConfigHB.string("mercadosecundario_email").split(";")) {
				if (!email.trim().isEmpty()) {
					String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
					String mensaje = "<html><head></head><body>";
					mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
					mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
					mensaje += "<b>Operación:</b> " + "Compra" + "<br/>";
					mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
					mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
					mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
					mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
					mensaje += "</body></html>";


					// AGREGAR SALESFORCE - COMPRA VENTA DOLAR MEP
					if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
						Objeto parametros = new Objeto();
						parametros.set("IDCOBIS", contexto.idCobis());
						parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
						parametros.set("APELLIDO", contexto.persona().apellido());
						parametros.set("NOMBRE", contexto.persona().nombre());
						parametros.set("CANAL", "Home Banking");
						parametros.set("ASUNTO", asunto);
						parametros.set("MENSAJE", mensaje);
						parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
						parametros.set("EMAIL_DESTINO", email);
						parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
						parametros.set("TIPO_OPERACION", "Compra");

						if (idEspecie.equals("AL30")) {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_dolar_mep"), parametros));
						} else {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_bonos_acciones"), parametros));
						}
					} else {
						ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
						requestMail.body("de", "aviso@mail-hipotecario.com.ar");
						requestMail.body("para", email.trim());
						requestMail.body("plantilla", ConfigHB.string("doppler_generico"));
						Objeto parametros = requestMail.body("parametros");
						parametros.set("ASUNTO", asunto);
						parametros.set("BODY", mensaje);
						Api.response(requestMail);
					}
				}
			}		
		}
		catch (Exception e) {
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("idComprobante", "titulo-valor-venta_" + response.string("idOrden"));
		respuesta.set("orden", orden);
		return respuesta;
	}
	
	public static Respuesta vender(ContextoHB contexto) {
		String version = contexto.parametros.string("version");
		if (version == null || version.isBlank() || version.equals("1")) {
			return venderV1(contexto);
		} 
		// INV-692
		return venderV2(contexto);
	}
	
	public static byte[] comprobanteCuentaComitente(ContextoHB contexto) {
		String idComprobante = contexto.parametros.string("idComprobante");
		String idOperacion = idComprobante.split("_")[0];
		String idCuentaComitente = idComprobante.split("_")[1];

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date fechaDesde = new Date();
		Date fechaHasta = new Date();
		try {
			fechaDesde = sdf.parse(idComprobante.split("_")[2]);
			fechaHasta = sdf2.parse(idComprobante.split("_")[3]);
		} catch (ParseException e) {
			return null;
		}

		ApiResponse movimientos = RestInversiones.movimientos(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
		if (movimientos.hayError()) {
			return null;
		}
		Objeto operacion = null;
		for (Objeto item : movimientos.objetos("operaciones")) {
			String id = item.string("idOperacion");
			if (idOperacion.equals(id)) {
				operacion = item;
				break;
			}
		}
		if (operacion == null) {
			// return Respuesta.error();
			return null;
		}

		String cuil = "";
		String docString = "";
		String nombresRazon = "";
		String telefonos = "";
		String domicilio = "";
		String localidad = "";
		String codigoPostal = "";
		String direccionPostalCobis = "";
		String telefonoDireccionPostalCobis = "";
		String domicilioIDTelefonoCobis = "";

		int x = 0;
		Boolean errorEnElPrimero = false;
		Objeto titularCobis = null;
		for (Objeto item : movimientos.objetos("intervinientesCobis")) {
			ApiResponse clienteResponse = RestPersona.consultarClienteEspecifico(contexto, item.string("idCobis"));
			if (clienteResponse.hayError()) {
				errorEnElPrimero = true;
			} else {
				Objeto cliente = clienteResponse.objetos().get(0);
				if (x == 0) {
					titularCobis = cliente;
					direccionPostalCobis = item.string("direccionPostalCobis");
					telefonoDireccionPostalCobis = item.string("telefonoDireccionPostalCobis");
					domicilioIDTelefonoCobis = item.string("domicilioIDTelefonoCobis");
				}
				// docString = docString + personaAux.getDocumento().getTipo().getDesc() + " - "
				// + personaAux.getDocumento().getNumero() + "\n";
				docString = docString + cliente.string("numeroDocumento") + "\n";
				nombresRazon = nombresRazon + cliente.string("apellidos") + ", " + cliente.string("nombres") + "\n";
			}
			x++;
		}
		if ("".equals(docString))
			docString = " - \n";
		if ("".equals(nombresRazon))
			nombresRazon = " - \n";

		if (!errorEnElPrimero) {
			// titular
			Objeto direccion = null;
			Objeto telefono = null;

			titularCobis.string("id");
			cuil = titularCobis.string("cuit");

			ApiResponse responseDomicilios = RestPersona.domicilios(contexto, titularCobis.string("cuit"));
			if (!"".equals(direccionPostalCobis)) {
				for (Objeto item : responseDomicilios.objetos()) {
					if (item.string("idCore").equals(direccionPostalCobis)) {
						direccion = item;
					}
				}
			}

			ApiResponse responseTelefonos = RestPersona.telefonos(contexto, titularCobis.string("cuit"));
			if (!"".equals(telefonoDireccionPostalCobis)) {
				for (Objeto item : responseTelefonos.objetos()) {
					if (item.string("idCore").equals(telefonoDireccionPostalCobis) && item.string("idDireccion").equals(domicilioIDTelefonoCobis)) {
						telefono = item;
					}
				}
			}
			if (direccion != null) {
				domicilio = direccion.string("calle") + "-" + direccion.string("numero");
				localidad = ""; // tengo el id de ciudad, pero cómo lo mapeo?
				codigoPostal = direccion.string("idCodigoPostal");
			}
			if (telefono != null) {
				// telefonos = telefonoTitular.getDdn() != null &&
				// telefonoTitular.getDdn().equalsIgnoreCase("")
				// ?"("+telefonoTitular.getDdn()+")":""+ telefonoTitular.getPrefijo() +
				// telefonoTitular.getCodCaracteristica() + telefonoTitular.getNumero();
				telefonos = telefono.string("prefijo") + telefono.string("caracteristica") + telefono.string("numero");
				// ¿que es el ddn?
			}
		}

		ExtractoComitenteDatosPDF datos = new ExtractoComitenteDatosPDF();
		/*
		 * Long valor = new Long(10005000); respuesta.set("valor", valor);
		 * respuesta.set("valorFormateado", Formateador.entero(valor));
		 */
		datos.setFechaPago(operacion.string("fechaPago"));
		datos.setFechaHora((operacion.string("fechaOrden")) + " " + (operacion.string("HoraOrden")));
		datos.setNombreRazon(nombresRazon);
		datos.setDocumento(docString);
		datos.setContraparte(operacion.string("contraparte"));
		datos.setCuil(cuil);
		datos.setDomicilio(domicilio);
		datos.setLocalidad(localidad);
		datos.setCodPostal(codigoPostal);
		datos.setTelefonos(telefonos);
		datos.setFechaOrden(operacion.string("fechaOrden"));
		datos.setHoraOrden(operacion.string("HoraOrden"));
		datos.setBruto(Formateador.importe(operacion.bigDecimal("bruto")));
		datos.setFechaConcertacion(operacion.string("fechaConcertacion"));
		datos.setSucursal(operacion.string("sucursal"));
		datos.setNumerosOrden(operacion.string("numeroOrdenInterno") + " - " + operacion.string("numeroOrdenMercado")); // numeros orden, van los dos asi?
		datos.setCuentaNro("");
		// datos.setCuentaNro(frm.getExtractoComitente().getCuenta().getNumero());
		// //numero de cuenta?????
		datos.setCuentaNro(cuentaComitente.numero());
		datos.setNroMinuta(operacion.string("numeroMinuta"));
		datos.setNroBoleto(operacion.string("numeroBoleto"));
		datos.setRefMercado(operacion.string("numeroRefMercado"));
		datos.setEspecie(operacion.string("especie.codigo") + " - " + operacion.string("especie.descripcion"));
		datos.setMercado(operacion.string("mercado"));
		datos.setCantidadValorNominal(operacion.string("cantidadNominal"));
		datos.setCantidadValorResidual(operacion.string("cantidadResidual"));
		datos.setPrecio(Formateador.importe(operacion.bigDecimal("precio")));
		if (!"".equals(operacion.string("derechos")))
			datos.setDerechos(Formateador.importe(operacion.bigDecimal("derechos", "")));
		else
			datos.setDerechos("");
		if (!"".equals(operacion.string("comision")))
			datos.setComision(Formateador.importe(operacion.bigDecimal("comision", "")));
		else
			datos.setComision("");
		if (!"".equals(operacion.string("totalML")))
			datos.setTotalML(Formateador.importe(operacion.bigDecimal("totalML", "")));
		else
			datos.setTotalML("");
		if (!"".equals(operacion.string("totalME")))
			datos.setTotalME(Formateador.importe(operacion.bigDecimal("totalME", "")));
		else
			datos.setTotalME("");
		datos.setTotalLetrasML(operacion.string("totalLetrasML"));
		datos.setTotalLetrasME(operacion.string("totalLetrasME"));
		if (!"".equals(operacion.string("ivaRI")))
			datos.setIvaRI(Formateador.importe(operacion.bigDecimal("ivaRI", "")));
		else
			datos.setIvaRI("");
		if (!"".equals(operacion.string("ivaRNI")))
			datos.setIvaRNI(Formateador.importe(operacion.bigDecimal("ivaRNI", "")));
		else
			datos.setIvaRNI("");
		if (!"".equals(operacion.string("aranceles")))
			datos.setAranceles(Formateador.importe(operacion.bigDecimal("aranceles", "")));
		else
			datos.setAranceles("");
		datos.setCuentaLiquidacionML(operacion.string("cuentaLiquidacionML"));
		datos.setCuentaLiquidacionME(operacion.string("cuentaLiquidacionME"));
		datos.setFechaVencimiento(operacion.string("fechaVencimiento"));
		datos.setPlazo(operacion.string("plazo") + " " + (operacion.string("unidadPlazo")));
		datos.setDividendoEnAcciones(operacion.string("dividendoEnAcciones"));
		datos.setDividendoEnEfectivo(operacion.string("dividendoEnEfectivo"));
		datos.setMoneda(operacion.string("moneda"));

		String tipoComprobante = "";
		if ("Compra de Especies".equalsIgnoreCase(operacion.string("tipo"))) {
			tipoComprobante = "Comprobante de Boleto de compra";
			datos.setTipoBoleto("COMPRA");
			datos.setTipoAccion("COMPRADO");
			if ("98".equalsIgnoreCase(operacion.string("mercado"))) {

				if (operacion.bigDecimal("totalME", "") != null && operacion.bigDecimal("totalME", "").compareTo(new BigDecimal("0")) == 1) {
					if (!"".equals(operacion.string("totalME")))
						datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalME", "")));
					else
						datos.setTotalMonto("");
					datos.setTotalLetra(operacion.string("totalLetrasME"));
					if (!"".equals(operacion.string("amortizacionME")))
						datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionME", "")));
					else
						datos.setAmortizacionMonto("");

					if (!"".equals(operacion.string("rentaME")))
						datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaME", "")));
					else
						datos.setRentaMonto("");
					datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionME"));
					datos.setMoneda("DÓLARES, DÓLARES");
				} else if (operacion.bigDecimal("totalML", "") != null && operacion.bigDecimal("totalML", "").compareTo(new BigDecimal("0")) == 1) {
					if (!"".equals(operacion.string("totalML")))
						datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalML", "")));
					else
						datos.setTotalMonto("");
					datos.setTotalLetra(operacion.string("totalLetrasML"));
					if (!"".equals(operacion.string("amortizacionML")))
						datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionML", "")));
					else
						datos.setAmortizacionMonto("");
					if (!"".equals(operacion.string("rentaML")))
						datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaML", "")));
					else
						datos.setRentaMonto("");
					datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionML"));
					datos.setMoneda("PESOS, PESOS");
				} else {
					datos.setTotalMonto("");
					datos.setTotalLetra("");
					datos.setAmortizacionMonto("");
					datos.setRentaMonto("");
					datos.setCuentaLiquidacion("");
					datos.setMoneda("");
				}
			}
		}
		if ("Venta de Especie".equalsIgnoreCase(operacion.string("tipo"))) {
			tipoComprobante = "Comprobante de Boleto de venta";
			datos.setTipoBoleto("VENTA");
			datos.setTipoAccion("VENDIDO");
		}
		if ("Liquidacion de Cupon".equalsIgnoreCase(operacion.string("tipo"))) {
			tipoComprobante = "Liquidación de renta y/o amortización";
			datos.setAmortizacion(operacion.string("amortizacion"));
			datos.setRenta(operacion.string("renta"));
			datos.setCantidadResidualActual(operacion.string("cantidadResidualActual"));
			if (operacion.bigDecimal("totalME", "") != null && operacion.bigDecimal("totalME", "").compareTo(new BigDecimal("0")) == 1) {
				datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalME", "")));
				datos.setTotalLetra(operacion.string("totalLetrasME"));
				datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionME", "")));
				datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaME", "")));
				datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionME"));
			} else if (operacion.bigDecimal("totalML", "") != null && operacion.bigDecimal("totalML", "").compareTo(new BigDecimal("0")) == 1) {
				datos.setTotalMonto(Formateador.importe(operacion.bigDecimal("totalML", "")));
				datos.setTotalLetra(operacion.string("totalLetrasML"));
				datos.setAmortizacionMonto(Formateador.importe(operacion.bigDecimal("amortizacionML", "")));
				datos.setRentaMonto(Formateador.importe(operacion.bigDecimal("rentaML", "")));
				datos.setCuentaLiquidacion(operacion.string("cuentaLiquidacionML"));

			} else {
				datos.setTotalMonto("");
				datos.setTotalLetra("");
				datos.setAmortizacionMonto("");
				datos.setRentaMonto("");
				datos.setCuentaLiquidacion("");
			}
		}
		if (operacion.string("tipo").toUpperCase().contains("DIVIDENDO")) {
			tipoComprobante = "Liquidación de dividendos";
		}

		Map<String, String> comprobante = new HashMap<>();

		comprobante.put("TIPO_COMPROBANTE", tipoComprobante);

		comprobante.put("FECHA_HORA", datos.getFechaHora());
		comprobante.put("FECHA_ORDEN", datos.getFechaOrden());
		comprobante.put("HORA_ORDEN", datos.getHoraOrden());
		comprobante.put("NUMERO_ORDEN", datos.getNumerosOrden());
		comprobante.put("NUMERO_MINUTA", datos.getNroMinuta());
		comprobante.put("NUMERO_BOLETO", datos.getNroBoleto());
		comprobante.put("SUCURSAL", datos.getSucursal());
		comprobante.put("CONTRAPARTE", datos.getContraparte());
		comprobante.put("CUENTA_NRO", datos.getCuentaNro());
		comprobante.put("REFERENCIA_MERCAD", datos.getRefMercado());
		comprobante.put("APELLIDO_NOMBRE", datos.getNombreRazon());
		comprobante.put("DATO_CUIL", datos.getCuil());
		comprobante.put("DOMICILIO", datos.getDomicilio());
		comprobante.put("LOCALIDAD", datos.getLocalidad());
		comprobante.put("CODIGO_POSTAL", datos.getCodPostal());
		comprobante.put("TELEFONO", datos.getTelefonos());
		comprobante.put("DOCUMENTO", datos.getDocumento());
		comprobante.put("ESPECIE", datos.getEspecie());
		comprobante.put("VALOR_NOMINAL", datos.getCantidadValorNominal());
		comprobante.put("VALOR_RESIDUAL", datos.getCantidadValorResidual());
		comprobante.put("MERCADO", datos.getMercado());
		comprobante.put("PRECIO", datos.getPrecio());
		comprobante.put("SUBTOTAL", datos.getBruto());
		comprobante.put("ARANCELES", datos.getAranceles());
		comprobante.put("IMPUESTOS", datos.getDerechos());
		comprobante.put("IVA_RESPONSABLE_INSCRIPTO", datos.getIvaRI());
		comprobante.put("COMISIONES", datos.getComision());
		comprobante.put("TOTAL_MONEDA_EXTRANJERA", datos.getTotalME());
		comprobante.put("TOTAL_LETRAS_MONEDA_EXTRANJERA", datos.getTotalLetrasME());
		comprobante.put("TOTAL_LETRAS_PESOS", datos.getTotalLetrasML());
		comprobante.put("MONTO_TOTAL", datos.getTotalML());
		comprobante.put("CUENTA_PESOS", datos.getCuentaLiquidacionML());
		comprobante.put("CUENTA_MONEDA_EXTRANJERA", datos.getCuentaLiquidacionME());

		comprobante.put("FECHAPAGO", datos.getFechaPago());
		comprobante.put("FECHA_CORTE", datos.getFechaConcertacion());
		comprobante.put("FECHA_LIQUIDACION", datos.getFechaVencimiento());
		comprobante.put("AMORTIZACION_MONTO", datos.getAmortizacionMonto());
		comprobante.put("AMORTIZACION_PORCENTAJE", datos.getAmortizacion());
		comprobante.put("RENTA_MONTO", datos.getRentaMonto());
		comprobante.put("RENTA_PORCENTAJE", datos.getRenta());
		comprobante.put("TENENCIA_RESIDUAL_ACTUAL", datos.getCantidadResidualActual());
		comprobante.put("TENENCIA_RESIDUAL_ANTERIOR", datos.getCantidadValorResidual());
		comprobante.put("TENENCIA_NOMINAL", datos.getCantidadValorNominal());
		comprobante.put("IVA_NO_INSCRIPTO", datos.getIvaRNI());
		comprobante.put("FORMA_DE_PAGO", datos.getCuentaLiquidacion());

		comprobante.put("FECHA_MINUTA", datos.getFechaConcertacion());
		comprobante.put("PORC_DIVIDENDO_EFECTIVO", datos.getDividendoEnEfectivo());
		comprobante.put("PORC_DIVIDENDO_ACCIONES", datos.getDividendoEnAcciones());
		comprobante.put("DATO_MONEDA", datos.getMoneda());

		String idComprobanteImpresion = "";
		if ("Compra de Especies".equalsIgnoreCase(operacion.string("tipo"))) {
			idComprobanteImpresion = "licitaciones-compra-venta-especie_" + idOperacion;
		}
		if ("Venta de Especie".equalsIgnoreCase(operacion.string("tipo"))) {
			idComprobanteImpresion = "licitaciones-compra-venta-especie_" + idOperacion;
		}
		if ("Liquidacion de Cupon".equalsIgnoreCase(operacion.string("tipo"))) {
			idComprobanteImpresion = "licitaciones-liquidacion-cupon_" + idOperacion;
		}
		if (operacion.string("tipo").toUpperCase().contains("DIVIDENDO")) {
			idComprobanteImpresion = "licitaciones-dividendo_" + idOperacion;
		}

		contexto.sesion.comprobantes.put(idComprobanteImpresion, comprobante);
		String template = idComprobanteImpresion.split("_")[0];
		Map<String, String> parametros = contexto.sesion.comprobantes.get(idComprobanteImpresion);
		contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
		return Pdf.generar(template, parametros);

	}

	public static byte[] comprobanteSeguimientoOperaciones(ContextoHB contexto) {
		String idComprobante = contexto.parametros.string("idComprobante");
		String ordenSecuencial = idComprobante.split("_")[0];
		String idCuentaComitente = idComprobante.split("_")[1];
		String idNumero = idComprobante.split("_")[2];

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date fechaDesde = new Date();
		Date fechaHasta = new Date();
		try {
			fechaDesde = sdf.parse(idComprobante.split("_")[3]);
			fechaHasta = sdf.parse(idComprobante.split("_")[4]);
		} catch (ParseException e) {
			return null;
		}

		ApiResponse seguimientoOperaciones = RestInversiones.seguimientoOperaciones(contexto, cuentaComitente.numero(), fechaDesde, fechaHasta);
		if (seguimientoOperaciones.hayError()) {
			return null;
		}
		Objeto operacion = null;
		for (Objeto item : seguimientoOperaciones.objetos("ordenesOrdenadas")) {
			if (ordenSecuencial.equals(item.string("ordenSecuencial")) && idNumero.equals(item.string("numero"))) {
				operacion = item;
				break;
			}
		}

		if (operacion == null) {
			return null;
		}

		Objeto datosTituloValor = RestInversiones.tituloValor(contexto, operacion.string("tipoEspecie"), "");

		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("COMPROBANTE", operacion.string("numero")); // ...
		comprobante.put("FECHA_HORA", operacion.string("fecha"));
		comprobante.put("ESPECIE", operacion.string("tipoEspecie") + " - " + datosTituloValor.string("descripcion"));
		String moneda = "PESOS".equals(operacion.string("moneda")) ? "$" : "USD".equals(operacion.string("moneda")) ? "USD" : "";
		comprobante.put("IMPORTE", moneda + " " + Formateador.importe(operacion.bigDecimal("monto")));
		String tipoOperacion = "";
		if (operacion.string("tipo").toLowerCase().contains("venta"))
			tipoOperacion = "Venta";
		if (operacion.string("tipo").toLowerCase().contains("compra"))
			tipoOperacion = "Compra";
		comprobante.put("TIPO_OPERACION", tipoOperacion);
		comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion"));
		comprobante.put("PRECIO", moneda + " " + Formateador.importeCantDecimales(operacion.bigDecimal("precioLimite"), 4));
		comprobante.put("VALOR_NOMINAL", operacion.string("cantidadNominal"));
		comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
		comprobante.put("CUENTA", operacion.string("cuentaLiquidacionMonetaria"));
		comprobante.put("PLAZO", operacion.string("plazo") + " hs");
		String comisiones = Formateador.importe(operacion.bigDecimal("comisiones"));
		if (comisiones == null || "".equals(comisiones)) {
			comisiones = Formateador.importe(new BigDecimal(0));
		}
		comprobante.put("COMISION", "$" + " " + comisiones);
		comprobante.put("VIGENCIA", operacion.string("vigencia"));

		String idComprobanteImpresion = "";
		if ("Venta".equals(tipoOperacion)) {
			idComprobanteImpresion = "titulo-valor-venta_" + operacion.string("orderId");
		} else {
			idComprobanteImpresion = "titulo-valor-compra_" + operacion.string("orderId");
		}
		contexto.sesion.comprobantes.put(idComprobanteImpresion, comprobante);
		String template = idComprobanteImpresion.split("_")[0];
		Map<String, String> parametros = contexto.sesion.comprobantes.get(idComprobanteImpresion);
		contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
		return Pdf.generar(template, parametros);
	}

	public static Respuesta indicesBursatilesDelay(ContextoHB contexto) {
		ApiResponse response = RestInversiones.indicesBursatilesDelay(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto indices = new Objeto();
		for (Objeto item : response.objetos("indices")) {
			Objeto indice = new Objeto();
			indice.set("nombre", item.string("nombre"));
			indice.set("variacion", item.bigDecimal("variacion"));
			indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("variacion")));
			indice.set("precioMaximo", item.bigDecimal("maximoValor"));
			indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("maximoValor")));
			indice.set("precioMinimo", item.bigDecimal("minimoValor"));
			indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("minimoValor")));
			indice.set("precioApertura", item.bigDecimal("apertura"));
			indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("apertura")));
			indice.set("cierreAnterior", item.bigDecimal("cierre"));
			indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierre")));
			indice.set("ultimoCierre", item.bigDecimal("ultimo"));
			indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("ultimo")));
			indices.add(indice);
		}

		return Respuesta.exito("indices", indices);
	}

	public static Respuesta indicesSectorialesDelay(ContextoHB contexto) {

		ApiResponse response = RestInversiones.indicesSectorialesDelay(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto indices = new Objeto();
		for (Objeto item : response.objetos("indices")) {
			Objeto indice = new Objeto();
			indice.set("nombre", item.string("nombre"));
			indice.set("variacion", item.bigDecimal("variacion"));
			indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("variacion")));
			indice.set("precioMaximo", item.bigDecimal("maximoValor"));
			indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("maximoValor")));
			indice.set("precioMinimo", item.bigDecimal("minimoValor"));
			indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("minimoValor")));
			indice.set("precioApertura", item.bigDecimal("apertura"));
			indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("apertura")));
			indice.set("cierreAnterior", item.bigDecimal("cierreAnterior"));
			indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierreAnterior")));
			indice.set("ultimoCierre", item.bigDecimal("cierreDia"));
			indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("cierreDia")));
			indices.add(indice);
		}

		return Respuesta.exito("indices", indices);
	}

	public static Respuesta panelesEspecies(ContextoHB contexto) {
		ApiResponse response = RestInversiones.panelesEspecies(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto paneles = new Objeto();
		for (Objeto item : response.objetos()) {
			Objeto panel = new Objeto();
			if (item.string("idPanel").equals("6") || item.string("idPanel").equals("8") || item.string("idPanel").equals("10")) {
				continue;
			}
			panel.set("idPanel", item.string("idPanel"));
			panel.set("descripcion", item.string("descripcion"));
			paneles.add(panel);
		}

		return Respuesta.exito("paneles", paneles);
	}

	public static Respuesta panelesCotizacionesDelay(ContextoHB contexto) {
		String idPanel = contexto.parametros.string("idPanel");

		ApiResponse response = RestInversiones.panelesCotizacionesDelay(contexto, idPanel);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto cotizaciones = new Objeto();
		for (Objeto item : response.objetos("cotizaciones")) {
			Objeto cotizacion = new Objeto();
			cotizacion.set("cantidadCompra", item.integer("cantidadNominalCompra"));
			cotizacion.set("precioCompra", item.bigDecimal("precioCompra"));
			cotizacion.set("precioCompraFormateado", Formateador.importe(item.bigDecimal("precioCompra")));
			cotizacion.set("cantidadVenta", item.integer("cantidadNominalVenta"));
			cotizacion.set("precioVenta", item.bigDecimal("precioVenta"));
			cotizacion.set("precioVentaFormateado", Formateador.importe(item.bigDecimal("precioVenta")));
			cotizacion.set("apertura", item.bigDecimal("apertura"));
			cotizacion.set("aperturaFormateada", Formateador.importe(item.bigDecimal("apertura")));
			cotizacion.set("maximo", item.bigDecimal("maximo"));
			cotizacion.set("maximoFormateado", Formateador.importe(item.bigDecimal("maximo")));
			cotizacion.set("minimo", item.bigDecimal("minimo"));
			cotizacion.set("minimoFormateado", Formateador.importe(item.bigDecimal("minimo")));
			cotizacion.set("cierreAnterior", item.bigDecimal("cierreAnterior"));
			cotizacion.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("cierreAnterior")));
			cotizacion.set("volumenNominal", item.bigDecimal("volumenNominal"));
			cotizacion.set("montoOperadoPesos", item.bigDecimal("montoOperadoPesos"));
			cotizacion.set("montoOperadoPesosFormateado", Formateador.importe(item.bigDecimal("montoOperadoPesos")));
			cotizacion.set("cantidadOperada", item.bigDecimal("cantidadOperada"));
			cotizaciones.add(cotizacion);
		}
		return Respuesta.exito("cotizaciones", cotizaciones);
	}

	public static Respuesta caucionesDelay(ContextoHB contexto) {

		ApiResponse response = RestInversiones.caucionesDelay(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto cauciones = new Objeto();
		for (Objeto item : response.objetos("cauciones")) {
			Objeto caucion = new Objeto();
			caucion.set("fecha", item.string("fecha"));
			caucion.set("montoContado", item.bigDecimal("montoContado"));
			caucion.set("montoContadoFormateado", Formateador.importe(item.bigDecimal("montoContado")));
			caucion.set("montoFuturo", item.bigDecimal("montoFuturo"));
			caucion.set("montoFuturoFormateado", Formateador.importe(item.bigDecimal("montoFuturo")));
			caucion.set("tasaPromedio", item.bigDecimal("tasaPromedio"));
			caucion.set("tasaPromedioFormateado", Formateador.importe(item.bigDecimal("tasaPromedio")));
			caucion.set("moneda", item.string("tipoLiquidacion").toUpperCase().equals("PESOS") ? 80 : 2);
			cauciones.add(caucion);
		}

		return Respuesta.exito("cauciones", cauciones);
	}

	public static Respuesta indicesBursatilesRealTime(ContextoHB contexto) {
		Boolean sectorial = contexto.parametros.bool("sectorial");
		Boolean soloOperados = contexto.parametros.bool("soloOperados");

		Objeto indices = new Objeto();

		ApiResponse responseIndices = null;
		if (sectorial) {
			responseIndices = RestInversiones.indicesSectoriales(contexto);
		} else {
			responseIndices = RestInversiones.indicesBursatiles(contexto);
		}
		if (responseIndices.hayError()) {
			return Respuesta.error();
		}

		ApiResponse response = RestInversiones.indicesRealTime(contexto, "", "-1", "");
		if (response.hayError()) {
			return Respuesta.error();
		}

		for (Objeto item : response.objetos()) {
			if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
				continue;
			}

			boolean encontroIndice = false;
			String descripcionIndice = "";
			for (Objeto indiceBursatil : responseIndices.objetos()) {
				if (item.string("symbol").equals(indiceBursatil.string("codigo"))) {
					encontroIndice = true;
					descripcionIndice = indiceBursatil.string("indice");
					break;
				}
			}
			if (!encontroIndice)
				continue;

			Objeto indice = new Objeto();
			indice.set("nombre", item.string("symbol"));
			indice.set("indice", descripcionIndice);
			indice.set("idIntradiaria", item.string("idIntradiaria"));
			indice.set("variacion", item.bigDecimal("imbalance"));
			indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
			indice.set("precioMaximo", item.bigDecimal("tradingSessionHighPrice"));
			indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
			indice.set("precioMinimo", item.bigDecimal("tradingSessionLowPrice"));
			indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
			indice.set("precioApertura", item.bigDecimal("openingPrice"));
			indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("openingPrice")));
			indice.set("cierreAnterior", item.bigDecimal("previousClose"));
			indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
			indice.set("ultimoCierre", item.bigDecimal("previousClose"));
			indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("previousClose")));
			indice.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));

			indices.add(indice);
		}

		return Respuesta.exito("indices", indices);

	}

	public static Respuesta indicesRealTime(ContextoHB contexto) {
		Boolean soloOperados = contexto.parametros.bool("soloOperados");

		Objeto indices = new Objeto();

		ApiResponse responseIndicesSectoriales = null;
		ApiResponse responseIndicesBursatiles = null;
		responseIndicesSectoriales = RestInversiones.indicesSectoriales(contexto);
		responseIndicesBursatiles = RestInversiones.indicesBursatiles(contexto);

		if (responseIndicesBursatiles.hayError()) {
			return Respuesta.error();
		}

		ApiResponse response = RestInversiones.indicesRealTime(contexto, "", "-1", "");
		if (response.hayError()) {
			return Respuesta.error();
		}

		for (Objeto item : response.objetos()) {
			if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
				continue;
			}
			Objeto indice = new Objeto();
			boolean encontroIndice = false;
			String descripcionIndice = "";

			for (Objeto indiceSectorial : responseIndicesSectoriales.objetos()) {
				if (item.string("symbol").equals(indiceSectorial.string("codigo"))) {
					encontroIndice = true;
					descripcionIndice = indiceSectorial.string("indice");
					indice.set("tipo", "sectorial");
					break;
				}
			}
			for (Objeto indiceBursatil : responseIndicesBursatiles.objetos()) {
				if (item.string("symbol").equals(indiceBursatil.string("codigo"))) {
					encontroIndice = true;
					descripcionIndice = indiceBursatil.string("indice");
					indice.set("tipo", "bursatil");
					break;
				}
			}

			if (!encontroIndice)
				continue;

			indice.set("nombre", item.string("symbol"));
			indice.set("indice", descripcionIndice);
			indice.set("idIntradiaria", item.string("idIntradiaria"));
			indice.set("variacion", item.bigDecimal("imbalance"));
			indice.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
			indice.set("precioMaximo", item.bigDecimal("tradingSessionHighPrice"));
			indice.set("precioMaximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
			indice.set("precioMinimo", item.bigDecimal("tradingSessionLowPrice"));
			indice.set("precioMinimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
			indice.set("precioApertura", item.bigDecimal("openingPrice"));
			indice.set("precioAperturaFormateado", Formateador.importe(item.bigDecimal("openingPrice")));
			indice.set("cierreAnterior", item.bigDecimal("previousClose"));
			indice.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
			indice.set("ultimoCierre", item.bigDecimal("previousClose"));
			indice.set("ultimoCierreFormateado", Formateador.importe(item.bigDecimal("previousClose")));
			indice.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));

			indices.add(indice);
		}

		return Respuesta.exito("indices", indices);

	}

	public static Respuesta indicesSectorialesRealTime(ContextoHB contexto) {
		/*
		 * Boolean sectorial = contexto.parametros.bool("sectorial"); Boolean
		 * soloOperados = contexto.parametros.bool("soloOperados");
		 */
		return indicesBursatilesRealTime(contexto);

	}

	public static Respuesta panelesCotizacionesRealTime(ContextoHB contexto) {
		String idPanel = contexto.parametros.string("idPanel");
		String idPlazo = contexto.parametros.string("idPlazo");
		Boolean soloOperados = contexto.parametros.bool("soloOperados");
		String idMoneda = contexto.parametros.string("idMoneda");

		if (idMoneda.equals("80")) {
			idMoneda = "1";
		}

		ApiResponse response = RestInversiones.indicesRealTime(contexto, "", idPanel, idPlazo);
		if (response.hayError()) {
			return Respuesta.error();
		}
		ApiResponse responseOferta = null;

		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mejora_intradiarias_byma")) {
			try {
				responseOferta = RestInversiones.intradiariasOferta(contexto, "", idPanel);
				if (responseOferta.hayError()) {
					// return Respuesta.error();
				}
			} catch (Exception e) {

			}
		}

		ApiResponse responseEspecies = RestInversiones.especiesPorPanel(contexto, idPanel);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto cotizaciones = new Objeto();
		for (Objeto item : response.objetos()) {
			Objeto cotizacion = new Objeto();

			if (soloOperados && (item.bigDecimal("trade") == null || item.bigDecimal("trade").compareTo(new BigDecimal(0)) == 0)) {
				continue;
			}

			String idMonedaAux = "";
			for (Objeto itemEspecie : responseEspecies.objetos()) {
				if (itemEspecie.string("simbolo").equals(item.string("symbol")))
					idMonedaAux = itemEspecie.string("idMoneda");
			}
			if (!idMoneda.equals("") && !idMonedaAux.equals(idMoneda)) {
				continue;
			}

			cotizacion.set("idIntradiaria", item.string("idIntradiaria"));
			cotizacion.set("codigo", item.string("symbol"));
			cotizacion.set("apertura", item.bigDecimal("openingPrice"));
			cotizacion.set("aperturaFormateada", Formateador.importe(item.bigDecimal("openingPrice")));
			cotizacion.set("maximo", item.bigDecimal("tradingSessionHighPrice"));
			cotizacion.set("maximoFormateado", Formateador.importe(item.bigDecimal("tradingSessionHighPrice")));
			cotizacion.set("minimo", item.bigDecimal("tradingSessionLowPrice"));
			cotizacion.set("minimoFormateado", Formateador.importe(item.bigDecimal("tradingSessionLowPrice")));
			cotizacion.set("cierreAnterior", item.bigDecimal("previousClose"));
			cotizacion.set("cierreAnteriorFormateado", Formateador.importe(item.bigDecimal("previousClose")));
			cotizacion.set("volumenNominal", item.bigDecimal("tradeVolumeQty"));
			cotizacion.set("ultimoOperado", item.bigDecimal("trade"));

			cotizacion.set("montoOperadoPesos", item.bigDecimal("tradeVolume"));
			cotizacion.set("montoOperadoPesosFormateado", Formateador.importe(item.bigDecimal("tradeVolume")));
			cotizacion.set("cantidadOperada", item.bigDecimal("tradeQty"));
			cotizacion.set("cantidadOperadaFormateada", Formateador.importe(item.bigDecimal("tradeQty")));

			cotizacion.set("variacion", item.bigDecimal("imbalance"));
			cotizacion.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));

			cotizacion.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));
			cotizacion.set("idMoneda", idMonedaAux.equals("1") ? "80" : idMonedaAux.equals("2") ? "2" : "");

			// Conseguir precio de compra y de venta
			if (responseOferta != null) {
				Integer cantidadCompra = null;
				BigDecimal precioCompra = null;
				Integer cantidadVenta = null;
				BigDecimal precioVenta = null;

				// Tengan en cuenta que se debe tomar el precio mas alto de BIDs y el mas bajo
				// de OFFERs,
				// ya que el recurso devuelve 5 registros para cada tipo.
				for (Objeto itemOferta : responseOferta.objetos()) {
					if (item.string("symbol").equals(itemOferta.string("symbol"))) {
						if (itemOferta.string("tipo").equals("Bid") && (precioCompra == null || precioCompra.compareTo(itemOferta.bigDecimal("price")) < 0)) {
							cantidadCompra = itemOferta.integer("quantity");
							precioCompra = itemOferta.bigDecimal("price");
						}
						if (itemOferta.string("tipo").equals("Offer") && (precioVenta == null || precioVenta.compareTo(itemOferta.bigDecimal("price")) > 0)) {

							cantidadVenta = itemOferta.integer("quantity");
							precioVenta = itemOferta.bigDecimal("price");
						}
					}
				}

				if (precioCompra != null && precioCompra.equals(new BigDecimal(0))) {
					precioCompra = null;
				}

				if (precioVenta != null && precioVenta.equals(new BigDecimal(0))) {
					precioVenta = null;
				}

				cotizacion.set("cantidadCompra", cantidadCompra);
				cotizacion.set("precioCompra", precioCompra);
				cotizacion.set("precioCompraFormateado", Formateador.importe(precioCompra));

				cotizacion.set("cantidadVenta", cantidadVenta);
				cotizacion.set("precioVenta", precioVenta);
				cotizacion.set("precioVentaFormateado", Formateador.importe(precioVenta));
			}

			cotizaciones.add(cotizacion);
		}
		return Respuesta.exito("cotizaciones", cotizaciones);

	}

	public static Respuesta panelesCotizacionesRealTimeEspecie(ContextoHB contexto) {
		String idIntradiaria = contexto.parametros.string("idIntradiaria");
		ApiResponse response = RestInversiones.intradiariasOferta(contexto, idIntradiaria, "");
		if (response.hayError()) {
			return Respuesta.error();
		}

		Integer cantidadCompra = null;
		BigDecimal precioCompra = null;
		Integer cantidadVenta = null;
		BigDecimal precioVenta = null;

		// Tengan en cuenta que se debe tomar el precio mas alta de BIDs y el mas bajo
		// de OFFERs,
		// ya que el recurso devuelve 5 registros para cada tipo.
		for (Objeto item : response.objetos()) {
			if (item.string("tipo").equals("Bid") && (precioCompra == null || precioCompra.compareTo(item.bigDecimal("price")) < 0)) {
				cantidadCompra = item.integer("quantity");
				precioCompra = item.bigDecimal("price");
			}
			if (item.string("tipo").equals("Offer") && (precioVenta == null || precioVenta.compareTo(item.bigDecimal("price")) > 0)) {
				cantidadVenta = item.integer("quantity");
				precioVenta = item.bigDecimal("price");
			}
		}

		Respuesta respuesta = new Respuesta();
		Objeto cotizaciones = new Objeto();

		if (precioCompra != null && precioCompra.equals(new BigDecimal(0))) {
			precioCompra = null;
		}

		if (precioVenta != null && precioVenta.equals(new BigDecimal(0))) {
			precioVenta = null;
		}

		cotizaciones.set("cantidadCompra", cantidadCompra);
		cotizaciones.set("precioCompra", precioCompra);
		cotizaciones.set("precioCompraFormateado", Formateador.importe(precioCompra));

		cotizaciones.set("cantidadVenta", cantidadVenta);
		cotizaciones.set("precioVenta", precioVenta);
		cotizaciones.set("precioVentaFormateado", Formateador.importe(precioVenta));

		respuesta.set("cotizacion", cotizaciones);
			
		return respuesta;
	}

	private static BigDecimal calcularPrecioMercadoTope(ContextoHB contexto, String idEspecie, PlazoLiquidacion plazo,
														TipoOperacionInversion tipoOperacion) {


		boolean criterioSeleccionPuntas = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "criterio_seleccion_puntas", "criterio_seleccion_puntas_cobis");


		// 1) Obtenemos la lista de puntas de mercado
		Objeto puntas = buscarPuntasMercado(contexto, idEspecie, plazo);
		if (puntas.string("estado").equals("ERROR")
				|| puntas.objetos("puntas") == null) {
			return BigDecimal.ZERO;
		}

		// 2) Validamos que sea Compra o Venta
		if (!tipoOperacion.isCompra() && !tipoOperacion.isVenta()) {
			throw new IllegalArgumentException(tipoOperacion.name());
		}

		// 3) Obtenemos el factor de escalado configurado
		BigDecimal escalarPrecio = ConfigHB.bigDecimal(
				tipoOperacion.isCompra()
						? "escalar_precio_mercado_compra_acciones_bonos"
						: "escalar_precio_mercado_venta_acciones_bonos",
				BigDecimal.ZERO
		);


		BigDecimal precio = null;

		if (criterioSeleccionPuntas) {
			// NUEVO CRITERIO (Toma la 3ra posición o la última disponible)
			if (tipoOperacion.isCompra()) {
				List<BigDecimal> preciosVenta = puntas.objetos("puntas").stream()
						.map(p -> p.bigDecimal("precioVenta"))
						.filter(Objects::nonNull)
						.sorted()
						.collect(Collectors.toList());

				if (preciosVenta.size() >= 3) {
					precio = preciosVenta.get(2); // índice 2 => tercera posición
				} else if (!preciosVenta.isEmpty()) {
					precio = preciosVenta.get(preciosVenta.size() - 1); // última disponible
				}

			} else {
				List<BigDecimal> preciosCompra = puntas.objetos("puntas").stream()
						.map(p -> p.bigDecimal("precioCompra"))
						.filter(Objects::nonNull)
						.sorted(Comparator.reverseOrder()) // de mayor a menor
						.collect(Collectors.toList());

				if (preciosCompra.size() >= 3) {
					precio = preciosCompra.get(2);
				} else if (!preciosCompra.isEmpty()) {
					precio = preciosCompra.get(preciosCompra.size() - 1);
				}
			}
		} else {
			// CRITERIO ANTERIOR (Tomar mejor punta)
			if (tipoOperacion.isCompra()) {
				// La punta de venta más baja
				precio = puntas.objetos("puntas").stream()
						.map(p -> p.bigDecimal("precioVenta"))
						.filter(Objects::nonNull)
						.min(Comparator.naturalOrder()) // Tomamos el menor precio disponible
						.orElse(null);
			} else {
				// La punta de compra más alta
				precio = puntas.objetos("puntas").stream()
						.map(p -> p.bigDecimal("precioCompra"))
						.filter(Objects::nonNull)
						.max(Comparator.naturalOrder()) // Tomamos el mayor precio disponible
						.orElse(null);
			}
		}

		if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 || escalarPrecio.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}


		// 5) Ajuste si es Título Público => se divide por 100
		Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto, "");
		Objeto productoOperable = productosOperables.get(idEspecie);
		if ("Titulo Publico".equals(productoOperable.string("clasificacion"))) {
			precio = precio.divide(BigDecimal.valueOf(100));
		}

		// 6) Escalamos el precio según la configuración
		//precio = precio.multiply(BigDecimal.ONE.add(escalarPrecio));

		// 7) Ajustamos escala final según la clasificación
		switch (productoOperable.string("clasificacion")) {
			case "Titulo Publico":
				precio = precio.setScale(4, RoundingMode.HALF_UP);
				break;
			default:
				precio = precio.setScale(2, RoundingMode.HALF_UP);
				break;
		}

		return precio;
	}

	public static Respuesta profundidadMercado(ContextoHB contexto) {		
		Respuesta respuesta = new Respuesta();
		Objeto profundidadMercado = buscarPuntasMercado(contexto
				, contexto.parametros.string("idEspecie")
				, PlazoLiquidacion.codigo(contexto.parametros.string("plazo")));
		respuesta.set("profundidadMercado", profundidadMercado);
			
		return respuesta;
	}

	private static Objeto buscarPuntasMercado(ContextoHB contexto, String idEspecie, PlazoLiquidacion plazo) {
		String idEspecieAux = idEspecie.split("_")[0];

		ApiResponse responseEspecie = RestInversiones.indicesRealTime(contexto, idEspecieAux, "", String.valueOf(plazo.idVencimiento()));
		if (responseEspecie.hayError()) {
			return Respuesta.error();
		}

		String idIntradiaria = "";
		Objeto profundidadMercado = new Objeto();

		for (Objeto item : responseEspecie.objetos()) {
			idIntradiaria = item.string("idIntradiaria");

			BigDecimal precio = item.bigDecimal("trade").signum() != 0 ? item.bigDecimal("trade") : item.bigDecimal("previousClose");
			profundidadMercado.set("ultimoPrecio", precio);
			profundidadMercado.set("ultimoPrecioFormateado", Formateador.importe(precio));
			profundidadMercado.set("fecha", item.date("fechaModificacion", "yyyy-MM-dd hh:mm", "dd/MM/yyyy hh:mm"));
			profundidadMercado.set("variacion", item.bigDecimal("imbalance"));
			profundidadMercado.set("variacionFormateada", Formateador.importe(item.bigDecimal("imbalance")));
			profundidadMercado.set("variacionAbsoluta", item.bigDecimal("trade", "0").subtract(item.bigDecimal("closingPrice", "0")));
			profundidadMercado.set("variacionAbsolutaFormateada", Formateador.importe(item.bigDecimal("trade", "0").subtract(item.bigDecimal("closingPrice", "0"))));

			profundidadMercado.set("precio", item.bigDecimal("trade"));
			profundidadMercado.set("precioFormateado", Formateador.importe(item.bigDecimal("trade")));
			profundidadMercado.set("hora", item.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "HH:mm"));
		}

		ApiResponse response = RestInversiones.intradiariasProfundidad(contexto, idIntradiaria, "");
		if (response.hayError()) {
			return Respuesta.error();
		}

		List<Objeto> itemsCompra = new ArrayList<>();
		List<Objeto> itemsVenta = new ArrayList<>();
		for (Objeto item : response.objetos()) {
			Objeto dato = new Objeto();
			dato.set("cantidad", item.integer("quantity"));
			dato.set("precio", item.bigDecimal("price"));
			dato.set("precioFormateado", Formateador.importe(item.bigDecimal("price")));
			if (item.string("tipo").equals("Bid")) {
				itemsCompra.add(dato);
			}
			if (item.string("tipo").equals("Offer")) {
				itemsVenta.add(dato);
			}
		}

		itemsCompra.sort(Comparator.comparing((Objeto o) -> o.bigDecimal("precio")).reversed());
		itemsVenta.sort(Comparator.comparing((Objeto o) -> o.bigDecimal("precio")));

		List<Objeto> puntas = new ArrayList<>();

		for (int x = 0; x < 5; x++) {
			boolean existeCompra = false;
			boolean existeVenta = false;
			Objeto dato = new Objeto();
			if (itemsCompra.size() >= x + 1) {
				dato.set("cantidadCompra", itemsCompra.get(x).integer("cantidad"));
				dato.set("precioCompra", itemsCompra.get(x).bigDecimal("precio"));
				dato.set("precioCompraFormateado", itemsCompra.get(x).string("precioFormateado"));
				existeCompra = true;
			}
			if (itemsVenta.size() >= x + 1) {
				dato.set("cantidadVenta", itemsVenta.get(x).integer("cantidad"));
				dato.set("precioVenta", itemsVenta.get(x).bigDecimal("precio"));
				dato.set("precioVentaFormateado", itemsVenta.get(x).string("precioFormateado"));
				existeVenta = true;
			}
			if (existeCompra || existeVenta) {
				puntas.add(dato);
			}
		}
		puntas.forEach(p -> profundidadMercado.add("puntas", p));
		
		return profundidadMercado;
	}
	
	public static Respuesta caucionesRealTime(ContextoHB contexto) {
		String moneda = contexto.parametros.string("moneda");

		ApiResponse response = RestInversiones.indicesRealTime(contexto, "", "98", null);
		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto cauciones = new Objeto();
		for (Objeto item : response.objetos()) {
			Objeto caucion = new Objeto();
			String monedaCaucion = item.string("symbol").substring(item.string("symbol").length() - 1).equals("M") ? "2" : "80";
			if (!moneda.equals("") && !moneda.equals(monedaCaucion)) {
				continue;
			}
			String fechaVencimiento = item.string("symbol").substring(1, 9);
			item.set("fechaVencimiento", fechaVencimiento);
			Date fechaVencimientoDate = item.date("fechaVencimiento", "yyyyMMdd");
			caucion.set("fecha", item.date("fechaVencimiento", "yyyyMMdd", "dd/MM/yyyy"));
			caucion.set("montoContado", item.bigDecimal("tradeVolumeQty"));
			caucion.set("montoContadoFormateado", Formateador.importe(item.bigDecimal("tradeVolumeQty")));
			caucion.set("montoFuturo", item.bigDecimal("tradeVolume"));
			caucion.set("montoFuturoFormateado", Formateador.importe(item.bigDecimal("tradeVolume")));
			caucion.set("tasaPromedio", item.bigDecimal("trade"));
			caucion.set("tasaPromedioFormateado", Formateador.importe(item.bigDecimal("trade")));
			Integer cantidadDias = Fecha.cantidadDias(new Date(), fechaVencimientoDate);
			caucion.set("plazo", cantidadDias);
			caucion.set("moneda", monedaCaucion);

			cauciones.add(caucion);
		}

		return Respuesta.exito("cauciones", cauciones);
	}

	public static Respuesta inversionesPlazos(ContextoHB contexto) {
		ApiResponse response = RestInversiones.vencimientosEspecies(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}
		
		Objeto vencimientos = new Objeto();
		PlazoLiquidacion idVencimientoDefault;
		try {
			idVencimientoDefault = PlazoLiquidacion.plazoDefault();
			for (Objeto item : response.objetos()) {
				Objeto vencimiento = new Objeto();
				PlazoLiquidacion plazo = PlazoLiquidacion.vencimiento(item.integer("idVencimiento"));
				if (plazo.isHabilitado()) {
					vencimiento.set("idVencimiento", item.string("idVencimiento"));
					vencimiento.set("descripcion", item.string("descripcion"));
					if (plazo.equals(idVencimientoDefault)) {
						vencimiento.set("default", true);
					}
					vencimientos.add(vencimiento);
				}
			}

			return Respuesta.exito("vencimientos", vencimientos);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Respuesta.estado("ERROR_PLAZO");
	}

	public static Objeto cotizacionDolaMepV2(ContextoHB contexto) {
		try {
			String coefPlazoCalculo = ConfigHB.string("dolarMep_plazo_coeficiente_calculo");

			// TitulosValores
			Futuro<ApiResponse> futuroTitulosValores = new Futuro<>(() -> RestInversiones.titulosValores(contexto, null, ""));
			
			// InversionesIndicesRealTime
			Futuro<ApiResponse> futuroIndicesRealTime = new Futuro<>(() -> RestInversiones.indicesRealTimeV2(contexto, "AL30,AL30D", "", null));
			
			// CatalogoCalendario
			Futuro<ApiResponse> futuroCatalogoCalendario = new Futuro<>(() -> RestCatalogo.calendarioFechaActual(contexto));
			
			// TenenciaPosicionNegociable2
			String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			Futuro<ApiResponse> futuroPosicionesNegociables = new Futuro<>(() -> RestInversiones.obtenerPosicionesNegociablesCache(contexto, "1000", fechaActual, 1));
			
			Map<String, String> plazosLiquidacion = new HashMap<>();
			plazosLiquidacion.put("CI", "1");
			plazosLiquidacion.put("24H", "2");
			plazosLiquidacion.put("48H", "3");

			ApiResponse realTime = futuroIndicesRealTime.get();
			ApiResponse realTimeAL30 = null;
			ApiResponse realTimeAL30D = null;
			for (Objeto item : realTime.objetos()) {
				if (item.string("symbol").equals("AL30") && item.string("settlType").equals("1")) {
					realTimeAL30 = new ApiResponse();
					realTimeAL30.add(item);
				}
				if (item.string("symbol").equals("AL30D") && item.string("settlType").equals(plazosLiquidacion.get(coefPlazoCalculo))) {
					realTimeAL30D = new ApiResponse();
					realTimeAL30D.add(item);
				}
			}
			Respuesta cotizacionAL30CI = obtenerCotizacionPorPlazoLiquidacion(contexto, "CI", "AL30", realTimeAL30);
			Respuesta cotizacionAL30D = obtenerCotizacionPorPlazoLiquidacion(contexto, coefPlazoCalculo, "AL30D", realTimeAL30D);

			// ESPERA
			futuroTitulosValores.get();
			futuroCatalogoCalendario.get();
			futuroPosicionesNegociables.get();

			BigDecimal cotizacionDolarMep = null;
			String[] codigosProducto = new String[] { "AL30", "AL30D" };
			if (!enHorarioDolarMep(contexto).hayError()) {
				cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precio")
						.divide(cotizacionAL30D.bigDecimal("precioCotizacion.precio"), 2, RoundingMode.CEILING);
			} else {
				cotizacionDolarMep = cotizacionAL30CI.bigDecimal("precioCotizacion.precioAnterior")
						.divide(cotizacionAL30D.bigDecimal("precioCotizacion.precioAnterior"), 2, RoundingMode.CEILING);
			}

			Objeto objeto = new Objeto();
			objeto.set("cotizacion", cotizacionDolarMep);
			objeto.set("cotizacionFormateada", cotizacionDolarMep != null ? Formateador.importe(cotizacionDolarMep) : null);
			objeto.set("tieneTenencia", tieneTenenciasEnCuentasComitentesCache(contexto, codigosProducto));
			objeto.set("leyendaCotizacion", ConfigHB.string("leyenda_dolar_mep_1"));
			objeto.set("leyendaOrdenes", ConfigHB.string("leyenda_dolar_mep_2"));
			objeto.set("leyendaCompraMepExitosa", ConfigHB.string("leyenda_dolar_mep_3"));
			
			Respuesta respuesta = Respuesta.exito("cotizacionDolarMep", objeto);
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	// no se puede cachear la cotizacion ya que cambia constantemente
//	@Deprecated
//	public static class CacheDolarMep {
//		private static Long expiracion = null;
//		private static BigDecimal cotizacion = null;
//		
//		public static BigDecimal cotizacion(ContextoHB contexto) {
//			if (cotizacion == null || new Date().getTime() > expiracion) {
//				contexto.parametros.set("plazoLiquidacion", "CI");
//				contexto.parametros.set("codigoProducto", "AL30");
//				Respuesta cotizacionAL30CI = HBTitulosValores.obtenerCotizacionPorPlazoLiquidacion(contexto);
//
//				contexto.parametros.set("plazoLiquidacion", "48H");
//				contexto.parametros.set("codigoProducto", "AL30D");
//				Respuesta cotizacionAL30D48H = obtenerCotizacionPorPlazoLiquidacion(contexto);
//
//				if (!enHorarioDolarMep(contexto).hayError()) {
//					cotizacion = cotizacionAL30CI.bigDecimal("precioCotizacion.precio").divide(cotizacionAL30D48H.bigDecimal("precioCotizacion.precio"), 2, RoundingMode.CEILING);
//				} else {
//					cotizacion = cotizacionAL30CI.bigDecimal("precioCotizacion.precioAnterior").divide(cotizacionAL30D48H.bigDecimal("precioCotizacion.precioAnterior"), 2, RoundingMode.CEILING);
//				}
//				expiracion = new Date().getTime() + (10 * 60 * 1000);
//			}
//			return cotizacion;
//		}
//	}

//	private static BigDecimal[] buscarCotizacion(ContextoHB contexto, String codigoProducto) {
//		ApiResponse indicesRealTime = RestInversiones.indicesRealTime(contexto, codigoProducto, "", "1");
//		BigDecimal[] cotizacion = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
//		if (!indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
//			Objeto indiceRealTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
//			if (indiceRealTime != null) {
//				cotizacion[0] = indiceRealTime.bigDecimal("trade");
//				cotizacion[1] = indiceRealTime.bigDecimal("previousClose");
//			}
//		}
//		return cotizacion;
//
//	}

//	private static boolean tieneTenenciasEnCuentasComitentes(ContextoHB contexto, String[] codigosProducto) {
//		boolean tieneTenencia = false;
//		ApiResponse posicionesNegociables = RestInversiones.obtenerPosicionesNegociables(contexto, "1000", new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 1);
//		for (Objeto pNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
//			String codigoProducto = pNegociable.string("codigo").toUpperCase();
//			if (Arrays.asList(codigosProducto).contains(codigoProducto)) {
//				tieneTenencia = true;
//				break;
//			}
//		}
//		return tieneTenencia;
//	}

	private static boolean tieneTenenciasEnCuentasComitentesCache(ContextoHB contexto, String[] codigosProducto) {
		boolean tieneTenencia = false;
		ApiResponse posicionesNegociables = RestInversiones.obtenerPosicionesNegociablesCache(contexto, "1000", new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 1);
		for (Objeto pNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
			String codigoProducto = pNegociable.string("codigo").toUpperCase();
			if (Arrays.asList(codigosProducto).contains(codigoProducto)) {
				tieneTenencia = true;
				break;
			}
		}
		return tieneTenencia;
	}

	public static Respuesta obtenerCotizacionPorPlazoLiquidacion(ContextoHB contexto) {
		String plazoLiquidacion = contexto.parametros.string("plazoLiquidacion"); // CI, 24H, 48H
		String codigoProducto = contexto.parametros.string("codigoProducto");

		Objeto productoOperable = RestInversiones.obtenerProductosOperablesMap(contexto, "").get(codigoProducto);

		Map<String, String> plazosLiquidacion = new HashMap<>();
		plazosLiquidacion.put("CI", "1");
		plazosLiquidacion.put("24H", "2");
		plazosLiquidacion.put("48H", "3");

		boolean esByma;

		Objeto cotizacion = buscarPrecioCotizacion(contexto, productoOperable, plazosLiquidacion.get(plazoLiquidacion));
		esByma = cotizacion.bool("esByma");
		if (!plazoLiquidacion.equals("48H") && !esByma) {
			cotizacion = buscarPrecioCotizacion(contexto, productoOperable, plazosLiquidacion.get("48H"));
			esByma = cotizacion.bool("esByma");
		}
		Objeto respuesta = new Objeto();
		respuesta.set("esPrecioRealTime", esByma);
		respuesta.set("precio", cotizacion.bigDecimal("precio"));
		respuesta.set("precioAnterior", cotizacion.bigDecimal("precioAnterior"));
		respuesta.set("precioFormateado", Formateador.importeCantDecimales(cotizacion.bigDecimal("precio"), 4));
		return Respuesta.exito("precioCotizacion", respuesta);
	}

	public static Respuesta obtenerCotizacionPorPlazoLiquidacion(ContextoHB contexto, String plazoLiquidacion, String codigoProducto, ApiResponse indicesRealTime) {
		Objeto productoOperable = RestInversiones.obtenerProductosOperablesMap(contexto, "").get(codigoProducto);

		Map<String, String> plazosLiquidacion = new HashMap<>();
		plazosLiquidacion.put("CI", "1");
		plazosLiquidacion.put("24H", "2");
		plazosLiquidacion.put("48H", "3");

		boolean esByma;

		Objeto cotizacion = buscarPrecioCotizacion(contexto, productoOperable, indicesRealTime);
		esByma = cotizacion.bool("esByma");
		if (!plazoLiquidacion.equals("48H") && !esByma) {
			cotizacion = buscarPrecioCotizacion(contexto, productoOperable, plazosLiquidacion.get("48H"));
			esByma = cotizacion.bool("esByma");
		}
		Objeto respuesta = new Objeto();
		respuesta.set("esPrecioRealTime", esByma);
		respuesta.set("precio", cotizacion.bigDecimal("precio"));
		respuesta.set("precioAnterior", cotizacion.bigDecimal("precioAnterior"));
		respuesta.set("precioFormateado", Formateador.importeCantDecimales(cotizacion.bigDecimal("precio"), 4));
		return Respuesta.exito("precioCotizacion", respuesta);
	}

	private static Objeto buscarPrecioCotizacion(ContextoHB contexto, Objeto productoOperable, ApiResponse indicesRealTime) {
		BigDecimal precio = BigDecimal.ZERO;
		BigDecimal precioAnterior = BigDecimal.ZERO;
		BigDecimal variacion = BigDecimal.ZERO;
		boolean esByma = false;
		boolean buscarPrecioReferencia = true;
		String fecha = null;
		String tipoCotizacion = null;

		//ApiResponse indicesRealTime = RestInversiones.indicesRealTime(contexto, productoOperable.string("codigo"), "", idVencimiento);
		if (indicesRealTime.objetos().size() > 0 && !indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
			Objeto realTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
			if (realTime != null) {
				precio = realTime.bigDecimal("trade").signum() != 0 ? realTime.bigDecimal("trade") : realTime.bigDecimal("previousClose");
				precioAnterior = realTime.bigDecimal("previousClose");
				if (precio.signum() != 0) {
					if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.string("clasificacion"))) {
						precio = precio.divide(new BigDecimal(100));
					}
					variacion = realTime.bigDecimal("imbalance");
					fecha = realTime.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
					tipoCotizacion = "BYMA";
					esByma = true;
					buscarPrecioReferencia = false;
				}
			}
		}
		if (buscarPrecioReferencia) {
			ApiResponse precioReferencia = RestInversiones.precioTituloValor(contexto, productoOperable.string("codigo"), productoOperable.string("descMoneda"));
			if (!precioReferencia.hayError() && precioReferencia.codigo.intValue() != 204 && !precioReferencia.claves().isEmpty()) {
				precio = precioReferencia.bigDecimal("precioReferencia");
				fecha = precioReferencia.date("fechaPrecio", "yy-MM-dd", "dd/MM/yy HH:mm");
			}
		}
		Objeto objeto = new Objeto();
		objeto.set("esByma", esByma);
		objeto.set("precio", precio);
		objeto.set("precioAnterior", precioAnterior);
		objeto.set("variacion", variacion);
		objeto.set("fecha", fecha);
		objeto.set("tipoCotizacion", tipoCotizacion);
		return objeto;
	}

	private static Objeto buscarPrecioCotizacion(ContextoHB contexto, Objeto productoOperable, String idVencimiento) {
		BigDecimal precio = BigDecimal.ZERO;
		BigDecimal precioAnterior = BigDecimal.ZERO;
		BigDecimal variacion = BigDecimal.ZERO;
		boolean esByma = false;
		boolean buscarPrecioReferencia = true;
		String fecha = null;
		String tipoCotizacion = null;

		ApiResponse indicesRealTime = RestInversiones.indicesRealTime(contexto, productoOperable.string("codigo"), "", idVencimiento);
		if (indicesRealTime.objetos().size() > 0 && !indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
			Objeto realTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
			if (realTime != null) {
				precio = realTime.bigDecimal("trade").signum() != 0 ? realTime.bigDecimal("trade") : realTime.bigDecimal("previousClose");
				precioAnterior = realTime.bigDecimal("previousClose");
				if (precio.signum() != 0) {
					if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.string("clasificacion"))) {
						precio = precio.divide(new BigDecimal(100));
					}
					variacion = realTime.bigDecimal("imbalance");
					fecha = realTime.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
					tipoCotizacion = "BYMA";
					esByma = true;
					buscarPrecioReferencia = false;
				}
			}
		}
		if (buscarPrecioReferencia) {
			ApiResponse precioReferencia = RestInversiones.precioTituloValor(contexto, productoOperable.string("codigo"), productoOperable.string("descMoneda"));
			if (!precioReferencia.hayError() && precioReferencia.codigo.intValue() != 204 && !precioReferencia.claves().isEmpty()) {
				precio = precioReferencia.bigDecimal("precioReferencia");
				fecha = precioReferencia.date("fechaPrecio", "yy-MM-dd", "dd/MM/yy HH:mm");
			}
		}
		Objeto objeto = new Objeto();
		objeto.set("esByma", esByma);
		objeto.set("precio", precio);
		objeto.set("precioAnterior", precioAnterior);
		objeto.set("variacion", variacion);
		objeto.set("fecha", fecha);
		objeto.set("tipoCotizacion", tipoCotizacion);
		return objeto;
	}

	private static Objeto buscarPrecioCotizacion(ContextoHB contexto, Objeto productoOperable, String idVencimiento, ApiResponse indicesRealTime) {
		BigDecimal precio = BigDecimal.ZERO;
		BigDecimal precioAnterior = BigDecimal.ZERO;
		BigDecimal variacion = BigDecimal.ZERO;
		boolean esByma = false;
		boolean buscarPrecioReferencia = true;
		String fecha = null;
		String tipoCotizacion = null;

		if (indicesRealTime.objetos().size() > 0 && !indicesRealTime.hayError() && indicesRealTime.codigo != 204) {
			Objeto realTime = indicesRealTime.objetos().get(indicesRealTime.objetos().size() - 1);
			if (realTime != null) {
				precio = realTime.bigDecimal("trade").signum() != 0 ? realTime.bigDecimal("trade") : realTime.bigDecimal("previousClose");
				precioAnterior = realTime.bigDecimal("previousClose");
				if (precio.signum() != 0) {
					if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.string("clasificacion"))) {
						precio = precio.divide(new BigDecimal(100));
					}
					variacion = realTime.bigDecimal("imbalance");
					fecha = realTime.date("fechaModificacion", "yyyy-MM-dd hh:mm:ss", "dd/MM/yy HH:mm");
					tipoCotizacion = "BYMA";
					esByma = true;
					buscarPrecioReferencia = false;
				}
			}
		}
		if (buscarPrecioReferencia) {
			ApiResponse precioReferencia = RestInversiones.precioTituloValor(contexto, productoOperable.string("codigo"), productoOperable.string("descMoneda"));
			if (!precioReferencia.hayError() && precioReferencia.codigo.intValue() != 204 && !precioReferencia.claves().isEmpty()) {
				precio = precioReferencia.bigDecimal("precioReferencia");
				fecha = precioReferencia.date("fechaPrecio", "yy-MM-dd", "dd/MM/yy HH:mm");
			}
		}
		Objeto objeto = new Objeto();
		objeto.set("esByma", esByma);
		objeto.set("precio", precio);
		objeto.set("precioAnterior", precioAnterior);
		objeto.set("variacion", variacion);
		objeto.set("fecha", fecha);
		objeto.set("tipoCotizacion", tipoCotizacion);
		return objeto;
	}

	public static Respuesta tenenciaPosicionNegociableV2(ContextoHB contexto) {

		if (HBAplicacion.funcionalidadPrendida("tenencia_posicion_negociable_V3")) {
			return tenenciaPosicionNegociableV3(contexto);
		}

		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String fecha = contexto.parametros.string("fecha");
		BigDecimal totalFondosPesos = new BigDecimal(BigInteger.ZERO);
		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		Integer cantidadTitulosValoresPesos = 0;

		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Set<String> plazosHabilitados = Objeto.setOf(ConfigHB.string("plazos_liquidacion_habilitados")
											  .split("_"));
		
		String idVencimientoValorizacion = "2";
		if (plazosHabilitados.contains("48")) {			
			idVencimientoValorizacion = "3";
		}
		
		Futuro<Objeto> posicionesNegociablesFuturo = new Futuro<>(() -> buscarPosicionesNegociables(contexto, cuentaComitente.numero(), fecha));
		Objeto posicionesNegociables = posicionesNegociablesFuturo.get();

		// TODO: Optimizar (?) Es necesario esto? No es suficiente una sola llamada a titulosValores(contexto, null, fecha)?
		Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMapByCodigo(contexto, fecha);
		Map<String, Objeto> productosOperablesbyProducto = RestInversiones.obtenerProductosOperablesMapByProducto(contexto, fecha);
		Map<String, Objeto> productosOperablesbyProductoPesos = RestInversiones.obtenerProductosOperablesMapByProductoPesos(contexto, fecha);
		
		if (posicionesNegociables instanceof Respuesta) {
			return (Respuesta) posicionesNegociables;
		}

		Respuesta respuesta = new Respuesta();
		
		// TODO: TIMEOUT
		if (ConfigHB.esHomologacion() && contexto.idCobis().equals("395778")) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		String codigos = "";
		for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
			String codigo = posicionNegociable.string("codigo");
			Objeto productoOperable = productosOperables.get(codigo);
			if (productoOperable == null) {
				productoOperable = productosOperablesbyProducto.get(codigo);
				if (productoOperable == null) {
					continue;
				}
			}
			String descMoneda = productoOperable.string("descMoneda"); 
			if (!descMoneda.equalsIgnoreCase("PESOS")) {
				productoOperable = productosOperablesbyProductoPesos.get(codigo);
			}
			if (productoOperable != null) {
				String codigoActual = productoOperable.string("codigo");
				codigos += codigos.isEmpty() ? codigoActual : "," + codigoActual;
			}
		}
		ApiResponse indicesRealTime = RestInversiones.indicesRealTimeV2(contexto, codigos, "", idVencimientoValorizacion);
		
		for (Objeto posicionNegociable : posicionesNegociables.objetos("posicionesNegociablesOrdenadas")) {
			Objeto erroresTenencia = new Objeto();	
			Objeto itemRespuesta = new Objeto();
			String codigo = posicionNegociable.string("codigo");
			BigDecimal saldoNominal = posicionNegociable.bigDecimal("saldoDisponible");
			Objeto productoOperable = productosOperables.get(codigo);
			itemRespuesta.set("id", codigo);
			itemRespuesta.set("producto", codigo);
			if (productoOperable == null) {
				productoOperable = productosOperablesbyProducto.get(codigo);
				if (productoOperable == null) {
					erroresTenencia.add("ERR_PROD_DATA");
					itemRespuesta.set("errores", erroresTenencia);
					itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
					itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
					itemRespuesta.set("cantidadNominal", saldoNominal);
					itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
					itemRespuesta.set("fecha", "");
					itemRespuesta.set("valorPesos", 0);
					itemRespuesta.set("valorPesosFormateado", "0");
					itemRespuesta.set("saldoValuadoPesos", 0);
					itemRespuesta.set("saldoValuadoPesosFormateado", "0");
					itemRespuesta.set("variacion", Formateador.importe(new BigDecimal(0)));
					itemRespuesta.set("tipoCotizacion", "SC");
					itemRespuesta.set("monedaDescripcion", "");
					itemRespuesta.set("monedaSimbolo", "");
					itemRespuesta.set("color", "#9b9b9b");
					respuesta.add("tenencia", itemRespuesta);
					cantidadTitulosValoresPesos++;
					continue;
				} else {
					// FIXME: INV-564 - API Inversiones - Retorna en "codigo" lo que parece ser "producto"
					// en recurso /v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}
					// los cuales difieren en algunos CEDEARS ("CDR AMZN" / "AMZN"). Hacemos la corrección acá.
					itemRespuesta.set("id", codigo.equals("CDR AMZN") ? "AMZN" : productoOperable.get("codigo"));
					itemRespuesta.set("producto", productoOperable.get("producto"));
				}
			}

			String descMoneda = productoOperable.string("descMoneda"); 
			if (!descMoneda.equalsIgnoreCase("PESOS")) {
				productoOperable = productosOperablesbyProductoPesos.get(codigo);
			}
			
			Objeto cotization = null;
			if (productoOperable != null) {
				try {
					if (!indicesRealTime.hayError()) {
						ApiResponse apiResponse = new ApiResponse();
						for(Objeto subitem : indicesRealTime.objetos()) {
							if (subitem.string("symbol").equals(productoOperable.string("codigo"))) {
								apiResponse.add(subitem);
								break;
							}
						}
						cotization = buscarPrecioCotizacion(contexto, productoOperable, idVencimientoValorizacion, apiResponse);
					} else {
						cotization = buscarPrecioCotizacion(contexto, productoOperable, idVencimientoValorizacion);
					}
					if (cotization == null 
							|| cotization.bigDecimal("precio").compareTo(BigDecimal.ZERO)==0) {
						erroresTenencia.add("ERR_COT");
					}
				} catch (Exception e) {
					e.printStackTrace();
					erroresTenencia.add("ERR_COT");
				}
			} else {
				// BUG INV-628: faltan datos del producto (i.e. falta AL30 -en pesos- para AL30D).
				erroresTenencia.add("ERR_PROD_DATA");
			}
		
			// Si hubo errores, inicializamos y continuamos con el calculo.	
			if (erroresTenencia.toList().size() > 0) {
				// respuesta.setEstadoExistenErrores();
				itemRespuesta.set("errores", erroresTenencia);

				cotization = (new Objeto()).set("precio", "0")
										   .set("variacion", "0")
										   .set("tipoCotizacion", "")
										   .set("fecha", "");
				productoOperable = new Objeto().set("descMoneda", "PESOS");
			}
			
			BigDecimal precio = cotization.bigDecimal("precio");
			String monedaDescripcion = productoOperable.string("descMoneda");
			String monedaSimbolo = monedaDescripcion.equals("PESOS") ? "$" : monedaDescripcion;
			BigDecimal saldoValuado = precio.multiply(saldoNominal);

			itemRespuesta.set("descripcion", codigo + " - " + posicionNegociable.string("descripcionTenencia"));
			itemRespuesta.set("tipoActivo", posicionNegociable.string("clasificacion"));
			itemRespuesta.set("cantidadNominal", saldoNominal);
			itemRespuesta.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00", ""));
			itemRespuesta.set("fecha", cotization.string("fecha"));
			itemRespuesta.set("valorPesos", precio);
			itemRespuesta.set("valorPesosFormateado", Formateador.importeCantDecimales(precio, 4));
			itemRespuesta.set("saldoValuadoPesos", saldoValuado);
			itemRespuesta.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado));
			itemRespuesta.set("variacion", Formateador.importe(cotization.bigDecimal("variacion")));
			itemRespuesta.set("tipoCotizacion", precio.signum() != 0 ? cotization.string("tipoCotizacion") : "SC");
			itemRespuesta.set("monedaDescripcion", monedaDescripcion);
			itemRespuesta.set("monedaSimbolo", monedaSimbolo);
			int nextInt = Util.secureRandom.nextInt(0xffffff + 1);
			itemRespuesta.set("color", String.format("#%06x", nextInt).toUpperCase());
			respuesta.add("tenencia", itemRespuesta);
			cantidadTitulosValoresPesos++;
			totalFondosPesos = totalFondosPesos.add(saldoValuado);
		}
		for (Objeto item : respuesta.objetos("tenencia")) {

			if (item.string("monedaDescripcion").isEmpty()) {
				item.set("porcentaje", 0.0);
			}

			item.set("porcentaje", Util.porcentaje(item.bigDecimal("saldoValuadoPesos"), totalFondosPesos));
		}
		respuesta.set("totalFondosPesos", totalFondosPesos);
		respuesta.set("totalFondosPesosFormateado", Formateador.importe(totalFondosPesos));
		respuesta.set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);

		if (respuesta.get("tenencia") == null) {
			return Respuesta.estado("SIN_TENENCIA");
		}

		return respuesta;
	}

	public static Respuesta tenenciaPosicionNegociableV3(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String fecha             = contexto.parametros.string("fecha");
		CuentaComitente cuenta   = contexto.cuentaComitente(idCuentaComitente);

		if (cuenta == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Objeto pos = buscarPosicionesNegociablesV3(contexto, cuenta.numero(), fecha);
		if (pos instanceof Respuesta) {
			return (Respuesta) pos;
		}

		Respuesta r                 = new Respuesta();
		BigDecimal totalPesos       = BigDecimal.ZERO;
		int        cantTitPesos     = 0;

		// MISMO CONTRATO QUE HB V2 (campos y formatos)
		for (Objeto it : pos.objetos("posicionesNegociablesOrdenadas")) {
			String     codigo         = it.string("codigo");
			String     descTenencia   = it.string("descripcionTenencia");
			String     clasif         = it.string("clasificacion");
			BigDecimal saldoNominal   = it.bigDecimal("saldoDisponible");

			// campos nuevos de la v2 del servicio (BYMA)
			String     fechaPx        = it.string("fechaPrecioConsulta");
			BigDecimal precio         = BigDecimal.ZERO;
			try { precio = new BigDecimal(it.string("precioConsulta")); } catch (Exception ignored) {}

			BigDecimal variacionBD    = BigDecimal.ZERO;
			try { variacionBD = new BigDecimal(it.string("variacion")); } catch (Exception ignored) {}

			// si viene código BYMA lo usamos como id/producto
			String codigoByma = it.string("codigoByma");
			String idFinal    = (codigoByma != null && !codigoByma.trim().isEmpty()) ? codigoByma : codigo;

			BigDecimal saldoValuado = precio.multiply(saldoNominal);

			Objeto fila = new Objeto()
					.set("id", idFinal)
					.set("producto", idFinal)
					.set("descripcion", codigo + " - " + descTenencia)
					.set("tipoActivo", clasif)
					.set("cantidadNominal", saldoNominal)
					.set("cantidadNominalFormateada", Formateador.importe(saldoNominal).replace(",00",""))
					.set("fecha", fechaPx)
					.set("valorPesos", precio)
					.set("valorPesosFormateado", Formateador.importeCantDecimales(precio, 4))
					.set("saldoValuadoPesos", saldoValuado)
					.set("saldoValuadoPesosFormateado", Formateador.importe(saldoValuado))
					.set("variacion", Formateador.importe(variacionBD))
					.set("tipoCotizacion", precio.signum()!=0 ? "BYMA" : "SC")
					.set("monedaDescripcion", "PESOS")
					.set("monedaSimbolo", "$")
					.set("color", String.format("#%06x", Util.secureRandom.nextInt(0xffffff+1)).toUpperCase());

			r.add("tenencia", fila);
			totalPesos   = totalPesos.add(saldoValuado);
			cantTitPesos++;
		}

		// porcentajes (mismo criterio que V2 MB/HB)
		for (Objeto fila : r.objetos("tenencia")) {
			fila.set("porcentaje", Util.porcentaje(fila.bigDecimal("saldoValuadoPesos"), totalPesos));
		}

		r.set("totalFondosPesos", totalPesos)
				.set("totalFondosPesosFormateado", Formateador.importe(totalPesos))
				.set("cantidadTitulosValoresPesos", cantTitPesos);

		if (r.get("tenencia") == null) {
			return Respuesta.estado("SIN_TENENCIA");
		}
		return r;
	}

	public static Respuesta enHorarioDolarMep(ContextoHB contexto) {
		try {
			String cobisId = contexto.idCobis();

			// 1) flag para forzar siempre fuera de horario
			boolean fueraHorario = HBAplicacion.funcionalidadPrendida(
					cobisId, "dolarMepFueraHorario"
			);

			// 2) parámetros de configuración
			String horaInicio = ConfigHB.string("dolarMep_horaInicio", "10:30");
			String horaFin    = ConfigHB.string("dolarMep_horaFin",    "17:00");
			String leyenda    = ConfigHB.string(
					"leyendaMepFueraHorario",
					"Sólo podés operar los <strong>días hábiles de 10:30 a 17:00hs.</strong>"
			);

			// 3) comprobación real de día hábil y horario
			boolean enHorario = Util.isDiaHabil(contexto)
					&& !Util.isfueraHorarioV2(horaInicio, horaFin);

			// 4) si debe forzar o no estamos en horario, devolvemos FUERA_HORARIO + leyenda
			if (fueraHorario || !enHorario) {
				return Respuesta
						.estado("FUERA_HORARIO")
						.set("fueraDeHorarioDescripcion", leyenda);
			}

		} catch (Exception e) {
			return Respuesta.error();
		}
		return Respuesta.exito();
	}





	/***************************** AUXILIARES ********************************/
	
	private static String calcularFechaLiquidacionActivo(ContextoHB contexto, Date fechaOperacion, PlazoLiquidacion plazo) {
		String fechaLiquidacion = "";
		
		if (plazo.isNulo()) {
			return fechaLiquidacion;
		}
		
		try {
			DateFormat dateFormatYYYYMMDD = new SimpleDateFormat(C_FORMATO_FECHA_1_YYYYMMDD);
			DateFormat dateFormatDDMMYYY = new SimpleDateFormat(C_FORMATO_FECHA_2_DDMMYYYY);			
			
			if (plazo.isCI()) {
				fechaLiquidacion = dateFormatDDMMYYY.format(fechaOperacion);
			} else {
				fechaLiquidacion = Util.calcularFechaNdiasHabiles(contexto
						, dateFormatYYYYMMDD.format(fechaOperacion)
						, C_FORMATO_FECHA_1_YYYYMMDD
						, plazo.diasHabilesLiquidacion());
				
				fechaLiquidacion = dateFormatDDMMYYY.format(dateFormatYYYYMMDD.parse(fechaLiquidacion));
			}
		} catch (ParseException e) {
			e.printStackTrace(); 					
		}
		return fechaLiquidacion;
	}
	
	/**
	 * 
	 * @param contexto
	 * 		  parametros:
	 * 			- especie: especie
	 * 		    - monedas: si "especie" es enviado
	 * 					- 0: incluir todas las subespecies asociadas
	 * @param especies especies
	 * @return resultados filtrados
	 */
	private static List<Objeto> filtrarEspecies(ContextoHB contexto, List<Objeto> especies) {
		if (contexto.parametros.existe("especie")) {
			List<Objeto> resultadosFiltro 
					= especies
							.stream()
							.filter(e -> 
								e.get("codigo").equals(contexto.parametros.get("especie")))
					.collect(Collectors.toList());
			if (!resultadosFiltro.isEmpty() 
						&& contexto.parametros.existe("monedas")
						&& contexto.parametros.get("monedas").equals("0")) {
				resultadosFiltro.addAll(especies
							.stream()
							.filter(e -> 
										e.get("producto")
										 .equals(resultadosFiltro.get(0).get("producto")) 
										 && !e.get("codigo").equals(contexto.parametros.get("especie"))
										 )
								.collect(Collectors.toList()));
			}
			return resultadosFiltro;
		}
		return especies;
	}

	
	/************************** INNER PUBLIC STRUCTS *************************/
	
	public enum TipoOperacionInversion {
		COMPRA, VENTA;
		
		public boolean isCompra() {
			return this.equals(COMPRA);
		}
		public boolean isVenta() {
			return this.equals(VENTA);
		}
	}
		
	public enum PlazoLiquidacion {
		// Poner plazo en valor negativo si es un plazo no válido.
		NULO("", "", -1, "-", -1), // e.g. Pago de dividendos
		PL_CI("CI", "0", 0, "Contado Inmediato", 1),
		PL_24HS("24H", "24", 24, "24 hs.", 2),
		PL_48HS("48H", "48", 48, "48 hs.", 3);
		
		private String id;
		private String codigo;
		private Integer plazo;
		private String descripcion;
		private Integer idVencimiento;
		private static Set<String> idPlazosHabilitados = 
				Objeto.setOf(ConfigHB.string("plazos_liquidacion_habilitados")
					.split("_"))
					.stream()
	                .collect(Collectors.toSet());
		
		PlazoLiquidacion(String id, String codigo, Integer plazo, String descripcion, Integer idVencimiento) {
			this.id = id;
			this.codigo = codigo;
			this.plazo = plazo;
			this.descripcion = descripcion;
			this.idVencimiento = idVencimiento;
		}
		
		public static PlazoLiquidacion codigo(String codigo) {
			for (PlazoLiquidacion e : values()) {
				if (e.codigo.equals(codigo)) {
					return e;
				}
			}
			throw new IllegalArgumentException(String.valueOf(codigo));
		}
		
		
		public static PlazoLiquidacion vencimiento(Integer idVencimiento) {
			if (idVencimiento == null) {
				return NULO;
			}
			for (PlazoLiquidacion e : values()) {
				if (e.idVencimiento.equals(idVencimiento)) {
					return e;
				}
			}
			throw new IllegalArgumentException(String.valueOf(idVencimiento));
		}
		
		public static PlazoLiquidacion id(String id) {
			if (id == null || id.equals("")) {
				return NULO;
			}
			for (PlazoLiquidacion e : values()) {
				if (e.id.equals(id)) {
					return e;
				}
			}
			throw new IllegalArgumentException(String.valueOf(id));
		}
		
		public Objeto toObjeto() {
			return new Objeto().set("plazo", this.plazo)
							   .set("descripcion", this.descripcion);
		}
		
		public Integer plazo() {
			return this.plazo;
		}
		
		public String descripcion() {
			return this.descripcion;
		}
		
		public String codigo() {
			return codigo;
		}
		
		public Integer idVencimiento() {
			return idVencimiento;
		}
		
		public boolean isCI() {
			return this.equals(PL_CI);
		}
		
		public boolean is24() {
			return this.equals(PL_24HS);
		}
		
		public boolean is48() {
			return this.equals(PL_48HS);
		}
		
		public boolean isNulo() {
			return this.equals(NULO);
		}
		
		public boolean isHabilitado() {
			return this.plazo < 0 ? false : PlazoLiquidacion.idPlazosHabilitados.contains(this.plazo.toString());
		}
		
		/*
		 * Devuelve el mayor plazo habilitado por VE.
		 */
		public static PlazoLiquidacion plazoDefault() {
			String maxPlazo = null;
			for (String plazo : idPlazosHabilitados) {
			   if (maxPlazo == null || Integer.parseInt(plazo) > Integer.parseInt(maxPlazo)) {
		          maxPlazo = plazo;
		       }
	        }
			return PlazoLiquidacion.codigo(maxPlazo);
		}
		
		public Integer diasHabilesLiquidacion() {
			return (this.plazo <= 0) ? 0 : this.plazo() / 24;
		}

	}	
	
	public enum TipoPrecioOperacion {
		PRECIO_LIMITE(1, "Precio Limite"),
		PRECIO_MERCADO(2, "Precio Mercado");
		
		private Integer id;
		private String descripcion;
		
		TipoPrecioOperacion(Integer id, String descripcion) {
			this.id = id;
			this.descripcion = descripcion;
		}
		
		public static TipoPrecioOperacion id(Integer id) {
			for (TipoPrecioOperacion e : values()) {
				if (e.id.equals(id)) {
					return e;
				}
			}
			throw new IllegalArgumentException(String.valueOf(id));
		}
		
		public Objeto toObjeto() {
			return new Objeto().set("id", this.id)
							   .set("descripcion", this.descripcion);
		}
		
		public Integer id() {
			return this.id;
		}
		
		public String descripcion() {
			return this.descripcion;
		}
		
		public boolean isPrecioLimite() {
			return this.equals(PRECIO_LIMITE);
		}
				
		public boolean isPrecioMercado() {
			return this.equals(PRECIO_MERCADO);
		}
	}
	
	/************************ FIN: INNER PUBLIC STRUCTS **********************/
	
	
	
	/************************* INICIO: CODIGO A DEPRECAR *********************/
	
	@Deprecated
	// INV-692
	// TODO GB - Deprecar codigo / Mover
	public static Respuesta simularCompraV1(ContextoHB contexto) {
	      String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
	      String idCuenta = contexto.parametros.string("idCuenta");
	      String idEspecie = contexto.parametros.string("idEspecie");
	      Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
	      BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
	      Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
	      Integer plazo = contexto.parametros.integer("plazo");
	      Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

	      String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

	      if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
	         return Respuesta.parametrosIncorrectos();
	      }

	      CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
	      if (cuentaComitente == null) {
	         return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
	      }

	      Cuenta cuenta = contexto.cuenta(idCuenta);
	      if (cuenta == null) {
	         return Respuesta.estado("CUENTA_NO_EXISTE");
	      }

	      List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
	      if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
	         return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
	      }

	      Map<String, Objeto> productosOperables = RestInversiones.obtenerProductosOperablesMap(contexto, "");
	      Objeto productoOperable = productosOperables.get(idEspecie);

	      ApiRequest request = Api.request("SimularCompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
	      request.query("idcobis", contexto.idCobis());
	      request.body("cantidadNominal", cantidadNominal);
	      request.body("cuentaComitente", cuentaComitente.numero());
	      request.body("cuentaLiquidacionMonetaria", cuenta.numero());
	      request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
	      request.body("especie", idEspecie);
	      request.body("fecha", fecha);
	      request.body("moneda", productoOperable.string("descMoneda"));
	      request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
	      request.body("plazo", plazo);
	      request.body("precioLimite", precioLimite);
	      request.body("tipo", "Compra");
	      request.body("tipoServicio", "Consulta");
	      request.body("vigencia", 0);

	      ApiResponse response = Api.response(request);
	      if (response.hayError()) {
	         if (response.string("codigo").equals("2122")) {
	            Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
	            respuesta.set("mensaje", response.string("mensajeAlUsuario"));
	            return respuesta;
	         }
	         if (response.string("codigo").equals("2020")) {
	            Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
	            respuesta.set("mensaje", response.string("mensajeAlUsuario"));
	            return respuesta;
	         }
	         if (!response.string("mensajeAlUsuario").isEmpty()) {
	            Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
	            respuesta.set("mensaje", response.string("mensajeAlUsuario"));
	            return respuesta;
	         }

	         return Respuesta.error();
	      }

	      Objeto orden = new Objeto();
	      orden.set("id", response.string("idOrden"));
	      orden.set("comisiones", response.bigDecimal("comisiones"));
	      orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
	      orden.set("vigencia", "por el día");
	      orden.set("precioMercado", precioMercado);
	      return Respuesta.exito("orden", orden);
	}
	
	@Deprecated
	// INV-692
    public static Respuesta simularVentaV1(ContextoHB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idCuenta = contexto.parametros.string("idCuenta");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
        BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
        Boolean precioMercado = contexto.parametros.bool("precioMercado", false);
        Integer plazo = contexto.parametros.integer("plazo");
        Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
           return Respuesta.parametrosIncorrectos();
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
           return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
           return Respuesta.estado("CUENTA_NO_EXISTE");
        }

        List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
        if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
           return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
        }

        ApiRequest request = Api.request("SimularVentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
        request.query("idcobis", contexto.idCobis());
        request.body("cantidadNominal", cantidadNominal);
        request.body("cuentaComitente", cuentaComitente.numero());
        request.body("cuentaLiquidacionMonetaria", cuenta.numero());
        request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
        request.body("especie", idEspecie);
        request.body("fecha", fecha);
        request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
        request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
        request.body("plazo", plazo);
        request.body("precioLimite", precioLimite);
        request.body("tipo", "Venta");
        request.body("tipoServicio", "Consulta");
        request.body("vigencia", 0);

        ApiResponse response = Api.response(request);
        if (response.hayError()) {
           if (response.string("codigo").equals("2122")) {
              Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
              respuesta.set("mensaje", response.string("mensajeAlUsuario"));
              return respuesta;
           }
           if (response.string("codigo").equals("2020")) {
              Respuesta respuesta = Respuesta.estado("PRECIO_INCORRECTO");
              respuesta.set("mensaje", response.string("mensajeAlUsuario"));
              return respuesta;
           }
           if (response.string("codigo").equals("2014")) {
              if (response.string("mensajeAlUsuario").contains("no es habil")) {
                 Respuesta respuesta = Respuesta.estado("DIA_NO_HABIL");
                 respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                 return respuesta;
              }
           }
           if (!response.string("mensajeAlUsuario").isEmpty()) {
              Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
              respuesta.set("mensaje", response.string("mensajeAlUsuario"));
              return respuesta;
           }

           return Respuesta.error();
        }

        Objeto orden = new Objeto();
        orden.set("id", response.string("idOrden"));
        orden.set("comisiones", response.bigDecimal("comisiones"));
        orden.set("comisionesFormateada", Formateador.importe(response.bigDecimal("comisiones")));
        orden.set("vigencia", "por el día");
        orden.set("precioMercado", precioMercado);
        return Respuesta.exito("orden", orden);
     }
	
	@Deprecated
	// INV-692
	public static Respuesta comprarV1(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		Integer plazo = contexto.parametros.integer("plazo");
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);
		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Boolean mostrarLeyendaTituloPublico = contexto.parametros.bool("mostrarLeyendaTituloPublico", false);

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		Cuenta cuenta = contexto.cuenta(idCuenta);

		Futuro<List<String>> cuentasLiquidacionTituloFuturo = new Futuro<>(
				() -> RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda()));
		Futuro<Map<String, Objeto>> productosOperablesFuturo = new Futuro<>(
				() -> RestInversiones.obtenerProductosOperablesMap(contexto, ""));

		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		List<String> cuentasLiquidacionTitulo = cuentasLiquidacionTituloFuturo.get();
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error()
					: Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		Map<String, Objeto> productosOperables = productosOperablesFuturo.get();
		Objeto productoOperable = productosOperables.get(idEspecie);

		Respuesta resultado = HBInversion.validarPerfilInversor(contexto,
				Optional.of(HBInversion.EnumPerfilInversor.ARRIESGADO), operaFueraPerfil);
		/*
		 * if (resultado.hayError()) { Respuesta respuesta =
		 * Respuesta.estado("ERROR_FUNCIONAL"); respuesta.set("mensaje",
		 * "Perfil inversor incorrecto para operar"); return respuesta; }
		 */
		Boolean operarFueraPerfilEstaTransaccion = resultado.bool("operaBajoPropioRiesgo");

		ApiRequest request = Api.request("CompraTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", productoOperable.string("descMoneda"));
		request.body("operaFueraDePerfil", operarFueraPerfilEstaTransaccion ? "SI" : "NO");
		request.body("plazo", plazo);
		request.body("precioLimite", precioLimite);
		request.body("tipo", "Compra");
		request.body("tipoServicio", "Operacion");
		request.body("vigencia", 0);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2009")) {
				Respuesta respuestaError = new Respuesta();
				respuestaError.set("estado", "FUERA_HORARIO");
				respuestaError.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuestaError;
			}
			if (response.string("codigo").equals("5000")) {
				return Respuesta.estado("FONDOS_INSUFICIENTES");
			}
			if (response.string("codigo").equals("2013")) {
				return Respuesta.estado("PLAZO_INVALIDO");
			}
			if (response.string("codigo").equals("2025")) {
				return Respuesta.estado("SIN_PERFIL_INVERSOR");
			}
			if (response.string("codigo").equals("2023")) {
				return Respuesta.estado("OPERACION_ARRIESGADA");
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			return Respuesta.error();
		}

		try {
			String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

			String descripcionError = "";
			if (response != null && !codigoError.equals("0")) {
				descripcionError += response.string("codigo") + ".";
				descripcionError += response.string("mensajeAlUsuario") + ".";
			}
			descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

			SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
			sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
			sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.add(new Date()); // momento
			sqlRequest.add(contexto.idCobis()); // cobis
			sqlRequest.add(request.idProceso()); // idProceso
			sqlRequest.add(request.ip()); // ip
			sqlRequest.add("HB"); // canal
			sqlRequest.add(codigoError); // codigoError
			sqlRequest.add(descripcionError); // descripcionError

			sqlRequest.add("Compra"); // operacion
			sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
			sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
			sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
			sqlRequest.add(idEspecie); // especie
			sqlRequest.add(productoOperable.string("descMoneda")); // moneda
			sqlRequest.add(operarFueraPerfilEstaTransaccion ? "SI" : "NO"); // operaFueraDePerfil
			sqlRequest.add(plazo); // plazo
			sqlRequest.add(precioLimite); // precioLimite
			sqlRequest.add(cantidadNominal); // cantidadNominal
			sqlRequest.add("0"); // vigencia
			sqlRequest.add(response.string("idOrden")); // idOrden
			sqlRequest.add(response.string("numeroOrden")); // numeroOrden
			sqlRequest.add(response.string("comisiones")); // comisiones

			// EMM: para algunos casos desde el front cuando es un título público
			// y a parte es en pesos se muestra una leyenda específica.
			if (mostrarLeyendaTituloPublico) {
				sqlRequest.add(cuenta.esDolares() ? "6" : "12"); // versionDDJJ
			} else {
				sqlRequest.add(cuenta.esDolares() ? "6" : ""); // versionDDJJ
			}

			Sql.response(sqlRequest);
		} catch (Exception e) {
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("numero", response.string("numeroOrden"));

		// emm-20190613-desde--> Comprobante
		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("COMPROBANTE", response.string("numeroOrden"));
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
		comprobante.put("ESPECIE", idEspecie + " - " + productoOperable.string("descripcion"));
		String moneda = "$";
		if (!"PESOS".equals(productoOperable.string("descMoneda"))) {
			moneda = "USD";
		}
		comprobante.put("IMPORTE",
				moneda + " " + Formateador.importe(precioLimite.multiply(new BigDecimal(cantidadNominal))));
		comprobante.put("TIPO_OPERACION", "Compra");
		comprobante.put("TIPO_ACTIVO", productoOperable.string("clasificacion")); // todo necesito la accion que eligió
																					// el cliente
		comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimite));
		comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
		comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
		comprobante.put("CUENTA", cuenta.numero());
		comprobante.put("PLAZO", plazo.toString() + " hs");
		comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
		comprobante.put("VIGENCIA", "0");
		String idComprobante = "titulo-valor-compra_" + response.string("idOrden");
		contexto.sesion.comprobantes.put(idComprobante, comprobante);
		// emm-20190613-hasta--> Comprobante

		try {
			for (String email : ConfigHB.string("mercadosecundario_email").split(";")) {
				if (!email.trim().isEmpty()) {
					String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
					String mensaje = "<html><head></head><body>";
					mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
					mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
					mensaje += "<b>Operación:</b> " + "Compra" + "<br/>";
					mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
					mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
					mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
					mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
					mensaje += "</body></html>";

					// AGREGAR SALESFORCE - COMPRA VENTA DOLAR MEP
					if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
						Objeto parametros = new Objeto();
						parametros.set("IDCOBIS", contexto.idCobis());
						parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
						parametros.set("APELLIDO", contexto.persona().apellido());
						parametros.set("NOMBRE", contexto.persona().nombre());
						parametros.set("CANAL", "Home Banking");
						parametros.set("ASUNTO", asunto);
						parametros.set("MENSAJE", mensaje);
						parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
						parametros.set("EMAIL_DESTINO", email);
						parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
						parametros.set("TIPO_OPERACION", "Compra");

						if (idEspecie.equals("AL30")) {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_dolar_mep"), parametros));
						} else {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_bonos_acciones"), parametros));
						}
					} else {
						ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
						requestMail.body("de", "aviso@mail-hipotecario.com.ar");
						requestMail.body("para", email.trim());
						requestMail.body("plantilla", ConfigHB.string("doppler_generico"));
						Objeto parametros = requestMail.body("parametros");
						parametros.set("ASUNTO", asunto);
						parametros.set("BODY", mensaje);
						Api.response(requestMail);
					}
				}
			}
		} catch (Exception e) {
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("idComprobante", "titulo-valor-compra_" + response.string("idOrden"));
		respuesta.set("orden", orden);
		return respuesta;
	}
	
	@Deprecated
	// INV-692
	public static Respuesta venderV1(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
		String idCuenta = contexto.parametros.string("idCuenta");
		String idEspecie = contexto.parametros.string("idEspecie");
		Integer cantidadNominal = contexto.parametros.integer("cantidadNominal");
		BigDecimal precioLimite = contexto.parametros.bigDecimal("precioLimite");
		Integer plazo = contexto.parametros.integer("plazo");
		Boolean operaFueraPerfil = contexto.parametros.bool("operaFueraPerfil", false);

		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		if (Objeto.anyEmpty(idCuentaComitente, idCuenta, idEspecie, cantidadNominal, precioLimite, plazo)) {
			return Respuesta.parametrosIncorrectos();
		}

		CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
		if (cuentaComitente == null) {
			return Respuesta.estado("CUENTA_COMITENTE_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		List<String> cuentasLiquidacionTitulo = RestInversiones.cuentasLiquidacionTitulo(contexto, cuentaComitente.numero(), cuenta.idMoneda());
		if (cuentasLiquidacionTitulo == null || cuentasLiquidacionTitulo.isEmpty()) {
			return cuentasLiquidacionTitulo == null ? Respuesta.error() : Respuesta.estado("SIN_CUENTA_LIQUIDACION_TITULO");
		}

		ApiRequest request = Api.request("VentaTitulosValores", "inversiones", "POST", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.body("cantidadNominal", cantidadNominal);
		request.body("cuentaComitente", cuentaComitente.numero());
		request.body("cuentaLiquidacionMonetaria", cuenta.numero());
		request.body("cuentaLiquidacionTitulos", cuentasLiquidacionTitulo.get(0));
		request.body("especie", idEspecie);
		request.body("fecha", fecha);
		request.body("moneda", cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null);
		request.body("operaFueraDePerfil", operaFueraPerfil ? "SI" : "NO");
		request.body("plazo", plazo);
		request.body("precioLimite", precioLimite);
		request.body("tipo", "Venta");
		request.body("tipoServicio", "Operacion");
		request.body("vigencia", 0);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			if (response.string("codigo").equals("2009")) {
				Respuesta respuestaError = new Respuesta();
				respuestaError.set("estado", "FUERA_HORARIO");
				respuestaError.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuestaError;
			}
			if (response.string("codigo").equals("5000")) {
				return Respuesta.estado("FONDOS_INSUFICIENTES");
			}
			if (response.string("codigo").equals("2013")) {
				return Respuesta.estado("PLAZO_INVALIDO");
			}
			if (response.string("codigo").equals("2025")) {
				return Respuesta.estado("SIN_PERFIL_INVERSOR");
			}
			if (response.string("codigo").equals("2023")) {
				return Respuesta.estado("OPERACION_ARRIESGADA");
			}
			if (!response.string("mensajeAlUsuario").isEmpty()) {
				Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
				respuesta.set("mensajeError", response.string("mensajeAlUsuario"));
				return respuesta;
			}
			return Respuesta.error();
		}

		try {
			String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

			String descripcionError = "";
			if (response != null && !codigoError.equals("0")) {
				descripcionError += response.string("codigo") + ".";
				descripcionError += response.string("mensajeAlUsuario") + ".";
			}
			descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

			SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
			sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_titulos_valores] ";
			sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[operacion],[cuentaComitente],[cuentaLiquidacionMonetaria],[cuentaLiquidacionTitulos],[especie],[moneda],[operaFueraDePerfil],[plazo],[precioLimite],[cantidadNominal],[vigencia],[idOrden],[numeroOrden],[comisiones],[versionDDJJ]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.add(new Date()); // momento
			sqlRequest.add(contexto.idCobis()); // cobis
			sqlRequest.add(request.idProceso()); // idProceso
			sqlRequest.add(request.ip()); // ip
			sqlRequest.add("HB"); // canal
			sqlRequest.add(codigoError); // codigoError
			sqlRequest.add(descripcionError); // descripcionError

			sqlRequest.add("Venta"); // operacion
			sqlRequest.add(cuentaComitente.numero()); // cuentaComitente
			sqlRequest.add(cuenta.numero()); // cuentaLiquidacionMonetaria
			sqlRequest.add(cuentasLiquidacionTitulo.get(0)); // cuentaLiquidacionTitulos
			sqlRequest.add(idEspecie); // especie
			sqlRequest.add(cuenta.esPesos() ? "PESOS" : cuenta.esDolares() ? "USD" : null); // moneda
			sqlRequest.add(operaFueraPerfil ? "SI" : "NO"); // operaFueraDePerfil
			sqlRequest.add(plazo); // plazo
			sqlRequest.add(precioLimite); // precioLimite
			sqlRequest.add(cantidadNominal); // cantidadNominal
			sqlRequest.add("0"); // vigencia
			sqlRequest.add(response.string("idOrden")); // idOrden
			sqlRequest.add(response.string("numeroOrden")); // numeroOrden
			sqlRequest.add(response.string("comisiones")); // comisiones
			sqlRequest.add(cuenta.esDolares() ? "11" : "9"); // versionDDJJ

			Sql.response(sqlRequest);
		} catch (Exception e) {
		}

		Objeto orden = new Objeto();
		orden.set("id", response.string("idOrden"));
		orden.set("numero", response.string("numeroOrden"));

		// emm-20190613-desde--> Comprobante
		Objeto datosTituloValor = RestInversiones.tituloValor(contexto, idEspecie, "");

		Map<String, String> comprobante = new HashMap<>();
		comprobante.put("COMPROBANTE", response.string("numeroOrden"));
		comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
		comprobante.put("ESPECIE", idEspecie + " - " + datosTituloValor.string("descripcion"));
		String moneda = cuenta.esPesos() ? "$" : cuenta.esDolares() ? "USD" : "";
		comprobante.put("IMPORTE", moneda + " " + Formateador.importe(precioLimite.multiply(new BigDecimal(cantidadNominal))));
		comprobante.put("TIPO_OPERACION", "Venta");
		comprobante.put("TIPO_ACTIVO", datosTituloValor.string("clasificacion")); // todo necesito la accion que eligió el cliente
		comprobante.put("PRECIO", moneda + " " + Formateador.importe(precioLimite));
		comprobante.put("VALOR_NOMINAL", cantidadNominal.toString());
		comprobante.put("CUENTA_COMITENTE", cuentaComitente.numero());
		comprobante.put("CUENTA", cuenta.numero());
		comprobante.put("PLAZO", plazo.toString() + " hs");
		comprobante.put("COMISION", "$" + " " + Formateador.importe(response.bigDecimal("comisiones")));
		comprobante.put("VIGENCIA", "0");
		String idComprobante = "titulo-valor-venta_" + response.string("idOrden");
		contexto.sesion.comprobantes.put(idComprobante, comprobante);
		// emm-20190613-hasta--> Comprobante

		try {
			for (String email : ConfigHB.string("mercadosecundario_email").split(";")) {
				if (!email.trim().isEmpty()) {
					String asunto = "Home Banking - Alta de orden Nro " + response.string("numeroOrden");
					String mensaje = "<html><head></head><body>";
					mensaje += "<b>Orden:</b> " + response.string("numeroOrden") + "<br/>";
					mensaje += "<b>Especie:</b> " + idEspecie + "<br/>";
					mensaje += "<b>Operación:</b> " + "Venta" + "<br/>";
					mensaje += "<b>Precio:</b> " + precioLimite + "<br/>";
					mensaje += "<b>Cantidad Nominal:</b> " + cantidadNominal + "<br/>";
					mensaje += "<b>Plazo:</b> " + plazo + "<br/>";
					mensaje += "<b>Comitente:</b> " + cuentaComitente.numero() + "<br/>";
					mensaje += "</body></html>";

					// AGREGAR SALESFORCE - COMPRA VENTA DOLAR MEP
					if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
						Objeto parametros = new Objeto();
						parametros.set("IDCOBIS", contexto.idCobis());
						parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
						parametros.set("NOMBRE", contexto.persona().nombre());
						parametros.set("APELLIDO", contexto.persona().apellido());
						parametros.set("CANAL", "Home Banking");
						parametros.set("ASUNTO", asunto);
						parametros.set("MENSAJE", mensaje);
						parametros.set("EMAIL_ORIGEN", "aviso@mail-hipotecario.com.ar");
						parametros.set("EMAIL_DESTINO", email);
						parametros.set("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
						parametros.set("TIPO_OPERACION", "Venta");

						if (idEspecie.equals("AL30")) {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_dolar_mep"), parametros));
						} else {
							new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_compra_venta_bonos_acciones"), parametros));
						}
					} else {
						ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
						requestMail.body("de", "aviso@mail-hipotecario.com.ar");
						requestMail.body("para", email.trim());
						requestMail.body("plantilla", ConfigHB.string("doppler_generico"));
						Objeto parametros = requestMail.body("parametros");
						parametros.set("ASUNTO", asunto);
						parametros.set("BODY", mensaje);
						Api.response(requestMail);
					}
				}
			}
		} catch (Exception e) {
		}
		Respuesta respuesta = new Respuesta();
		respuesta.set("idComprobante", "titulo-valor-venta_" + response.string("idOrden"));
		respuesta.set("orden", orden);
		return respuesta;
	}
	/************************* FIN: CODIGO A DEPRECAR ************************/
	
}
