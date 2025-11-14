package ar.com.hipotecario.canal.buhobank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank.LogBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.esales.BBAdjustsEsales;

public class BBReporte extends Modulo {

	public static Object registrarEvento(ContextoBB contexto) {
		String evento = contexto.parametros.string("evento");
		String datos = contexto.parametros.string("datos");
		LogBB.evento(contexto, "FRONT_" + evento, datos);
		return respuesta();
	}

	public static Object registrarEventoExterno(ContextoBB contexto) {
		return respuesta();
	}

	public static Object obtenerRegistros(ContextoBB contexto) {
		String evento = contexto.parametros.string("evento", null);
		String cuil = contexto.parametros.string("cuil", "");
		String fechaDesdeStr = contexto.parametros.string("fechaDesde");
		String fechaHastaStr = contexto.parametros.string("fechaHasta");
		Fecha fechaDesde = BBAlta.getFechaDesde(fechaDesdeStr, fechaHastaStr);
		Fecha fechaHasta = BBAlta.getFechaHasta(fechaDesdeStr, fechaHastaStr);

		if (empty(fechaDesde) || empty(fechaHasta) || fechaHasta.esAnterior(fechaDesde)) {
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		LogsBuhoBank registros;
		if (cuil.length() != 11) {
			registros = getRegistroByCodigo(contexto, cuil, fechaDesde, fechaHasta);
			if(!registros.isEmpty()){
				registros.get(0).cuit += " (" + descCodigo(cuil) + " - total " + registros.size() + ")";
			}
		} else {
			registros = LogsBuhoBank.getRegistro(contexto, cuil, fechaDesde, fechaHasta);
		}

		Objeto respuesta = respuesta();
		respuesta.set("fechaDesde", fechaDesde);
		respuesta.set("fechahasta", fechaHasta);

		if (!empty(evento)) {
			LogsBuhoBank registrosAux = new LogsBuhoBank();
			for (LogBuhoBank registro : registros) {
				if (evento.equals(registro.evento)) {
					registrosAux.add(registro);
				}
			}

			return respuesta.set("registros", registrosAux);
		}

		return respuesta.set("registros", registros);
	}

	private static String descCodigo(String codigo) {
		return switch (codigo) {
			case "01" -> "Sesiones";
			case "02" -> "finalizados";
			case "03" -> "En batch";
			case "11" -> "Logs";
			case "12" -> "Altas batch";
			case "13" -> "Altas app";
			case "14" -> "Flujo vu ok";
			case "15" -> "Errores";
			default -> codigo;
		};
	}

	private static LogsBuhoBank getRegistroByCodigo(ContextoBB contexto, String codigo, Fecha fechaDesde, Fecha fechaHasta) {
		return switch (codigo) {
			case "21" -> SqlBuhoBank.getSesiones(contexto, fechaDesde, fechaHasta).tryGet();
			case "22" -> SqlBuhoBank.getSesionesEstado(contexto, "FINALIZAR_OK", fechaDesde, fechaHasta).tryGet();
			case "23" -> SqlBuhoBank.getSesionesEstado(contexto, "BATCH_CORRIENDO", fechaDesde, fechaHasta).tryGet();
			case "24" -> SqlBuhoBank.getAuditoriaSP(contexto, fechaDesde, fechaHasta, "sp_LimpiarSesionesAntiguas").tryGet();
			case "25" -> SqlBuhoBank.getAnonimizarVU(contexto).tryGet();
			case "26" -> SqlBuhoBank.getCantidadErrorAnonimizarVU(contexto).tryGet();
			case "27" -> SqlBuhoBank.getCantidadProcesadosAnonimizarVU(contexto).tryGet();
			case "28" -> SqlBuhoBank.getCantidadNoProcesadosAnonimizarVU(contexto).tryGet();
			case "29" -> SqlBuhoBank.getAuditoriaSP(contexto, fechaDesde, fechaHasta, "cron_prismaNotificacionesNFC").tryGet();
			case "30" -> SqlBuhoBank.getSolicitudesSD(contexto, fechaDesde, fechaHasta).tryGet();
			case "11" -> LogsBuhoBank.getRegistroByFecha(contexto, fechaDesde, fechaHasta);
			case "12" -> SqlBuhoBank.logFinalizados(contexto, fechaDesde, fechaHasta, true).tryGet();
			case "13" -> SqlBuhoBank.logFinalizados(contexto, fechaDesde, fechaHasta, false).tryGet();
			case "14" -> SqlBuhoBank.logCasosVu(contexto, fechaDesde, fechaHasta).tryGet();
			case "15" -> SqlBuhoBank.logCasosError(contexto, fechaDesde, fechaHasta).tryGet();
			case "16" -> SqlBuhoBank.logSesionesFinalizadas(contexto, fechaDesde, fechaHasta, "").tryGet(); // todos
			case "17" -> SqlBuhoBank.logSesionesFinalizadas(contexto, fechaDesde, fechaHasta, "FLUJO_TCV").tryGet();
			case "18" -> SqlBuhoBank.logSesionesFinalizadas(contexto, fechaDesde, fechaHasta, "FLUJO_INVERSIONES").tryGet();
			case "19" -> SqlBuhoBank.logSesionesFinalizadas(contexto, fechaDesde, fechaHasta, "FLUJO_ONBOARDING").tryGet();
			default -> new LogsBuhoBank();
		};
	}

	public static Object obtenerReporteRegistros(ContextoBB contexto) {

		String fechaDesdeStr = contexto.parametros.string("fechaDesde", null);
		String fechaHastaStr = contexto.parametros.string("fechaHasta", null);

		Fecha fechaDesde = BBAlta.getFechaDesde(fechaDesdeStr, fechaHastaStr);
		Fecha fechaHasta = BBAlta.getFechaHasta(fechaDesdeStr, fechaHastaStr);

		if (empty(fechaDesde) || empty(fechaHasta) || fechaHasta.esAnterior(fechaDesde)) {
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		Objeto respuesta = respuesta();
		respuesta.set("fechaDesde", fechaDesde);
		respuesta.set("fechahasta", fechaHasta);
		respuesta.set("reporte", new LogsBuhoBank());

		LogsBuhoBank registros = LogsBuhoBank.getReporteRegistroByFecha(contexto, fechaDesde, fechaHasta);
		if (registros == null || registros.size() == 0) {
			return respuesta;
		}

		List<Objeto> registrosAux = new ArrayList<Objeto>();

		for (LogBuhoBank registro : registros) {

			Objeto reporteObj = new Objeto();
			reporteObj.set("endpoint", registro.endpoint);
			reporteObj.set("evento", registro.evento);
			reporteObj.set("total", registro.datos);
			registrosAux.add(reporteObj);
		}

		respuesta.set("reporte", registrosAux);

		return respuesta;
	}

	public static Object obtenerReportePeriodicoRegistros(ContextoBB contexto) {
		String evento = contexto.parametros.string("evento");
		String fechaDesdeStr = contexto.parametros.string("fechaDesde", null);
		String fechaHastaStr = contexto.parametros.string("fechaHasta", null);

		Fecha fechaDesde = BBAlta.getFechaDesde(fechaDesdeStr, fechaHastaStr);
		Fecha fechaHasta = BBAlta.getFechaHasta(fechaDesdeStr, fechaHastaStr);
		if (empty(fechaDesde) || empty(fechaHasta) || fechaHasta.esAnterior(fechaDesde)) {
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		Objeto respuesta = respuesta();
		respuesta.set("fechaDesde", fechaDesde);
		respuesta.set("fechahasta", fechaHasta);
		respuesta.set("labels", new LogsBuhoBank());
		respuesta.set("data", new LogsBuhoBank());

		LogsBuhoBank registros;
		if ("BB_ERROR".equals(evento)) {
			registros = LogsBuhoBank.getReporteErrorByPeriodo(contexto, fechaDesde, fechaHasta);
		} else {
			registros = LogsBuhoBank.getReporteEventoByPeriodo(contexto, fechaDesde, fechaHasta, evento);
		}

		List<String> labels = new ArrayList<>();
		List<Integer> data = new ArrayList<>();

		for (LogBuhoBank registro : registros) {

			labels.add(registro.momento.toString());
			data.add(Integer.parseInt(registro.datos));
		}

		respuesta.set("labels", labels);
		respuesta.set("data", data);

		return respuesta;
	}

	public static Object registrarEventoAdjust(ContextoBB contexto) {

		Map<String, Object> data = contexto.parametros.toMap();

		if (data.size() > 1) {
			String paramToken = contexto.parametros.string("token", "");
			if (paramToken.equals("") || !esAutorizado(paramToken))
				return respuesta("No autorizado");

			String adid = contexto.parametros.string("adid", "");
			if (adid.equals(""))
				return respuesta("Request inválida");

			String claves = "";
			String valores = "";

			for (Map.Entry<String, Object> entry : data.entrySet()) {
				String key = entry.getKey();
				if (key.equals("token") || key.equals("adid"))
					continue;

				String value = entry.getValue().toString();
				claves += "|" + key;
				valores += "|" + value;
			}

			claves = claves.substring(1);
			valores = valores.substring(1);

			BBAdjustsEsales result = SqlEsales.registrarEventoAdjust(contexto, adid, claves, valores).tryGet();

			return result == null ? respuesta("ERROR") : respuesta("OK");
		}

		return respuesta("BAD_REQUEST");
	}

	private static final String tokenAdjustSecurityMax = "e5b2eb63414f7ce4556d0f2b04199bb0e33cd0fe3fb78265c4b85e3af323ae55";

	private static Boolean esAutorizado(String token) {
		return token.equals(tokenAdjustSecurityMax);
	}

}
