package ar.com.hipotecario.backend.conector.api;

import com.google.gson.Gson;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;

public class Api extends Base {

	/* ========== ATRIBUTOS ESTATICOS ========== */
	public static Boolean habilitarLog = true;

	/* ========== LOG ========== */
	public static class Log {
		private static Gson gson = new Gson();

		public String canal;
		public String cuit;
		public String idCobis;
		public String servicio;
		public String tipo;
		public String idProceso;
		public String http;
		public String request;
		public String response;

		public String toString() {
			return gson.toJson(this);
		}
	}

	/* ========== CACHE ========== */
	public static String claveCache(Contexto contexto, String servicio, Object... parametros) {
		String clave = servicio + "_" + Fecha.hoy().string("yyyyMMdd");
		for (int i = 0; i < parametros.length; ++i) {
			clave += "_" + parametros[i];
		}
		return clave;
	}

	public static String getCache(Contexto contexto, String servicio, Object... parametros) {
		return contexto.get(claveCache(contexto, servicio, parametros));
	}

	public static void setCache(Contexto contexto, String servicio, String json, Object... parametros) {
		contexto.set(claveCache(contexto, servicio, parametros), json);
	}

	public static void eliminarCache(Contexto contexto, String servicio, Object... parametros) {
		contexto.del(claveCache(contexto, servicio, parametros));
	}
}
