package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.homebanking.LogsMonitor.LogMonitor;

@SuppressWarnings("serial")
public class LogsMonitor extends SqlObjetos<LogMonitor> {

	/* ========== ATRIBUTOS ========== */
	public static class LogMonitor extends SqlObjeto {
		public Long id;
		public Fecha momento;
		public Integer proceso;
		public String servicio;
		public String cobis;
		public String codigo_respuesta;
		public String request;
		public String response;
		public String ip;
	}

	/* ========== SERVICIO ========== */
	public static Boolean post(Contexto contexto, LogMonitor logMonitor) {
		String sql = "INSERT INTO [homebanking].[dbo].[log] VALUES (getdate(), ?, ?, ?, ?, ?, ?, ?)";
		Object[] parametros = new Object[7];
		parametros[0] = logMonitor.proceso;
		parametros[1] = logMonitor.servicio;
		parametros[2] = logMonitor.cobis;
		parametros[3] = logMonitor.codigo_respuesta;
		parametros[4] = logMonitor.request;
		parametros[5] = logMonitor.response;
		parametros[6] = logMonitor.ip;
		return Sql.update(contexto, "visualizador", sql, parametros) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		LogMonitor logMonitor = new LogMonitor();
		logMonitor.proceso = 1;
		logMonitor.servicio = "2";
		logMonitor.cobis = null;
		logMonitor.codigo_respuesta = "4";
		logMonitor.request = "5";
		logMonitor.response = "6";
		logMonitor.ip = "7";
		Boolean exito = post(contexto, logMonitor);
		imprimirResultado(contexto, exito);
	}
}
