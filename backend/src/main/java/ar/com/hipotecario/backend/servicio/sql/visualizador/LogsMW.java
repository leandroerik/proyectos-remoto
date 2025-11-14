package ar.com.hipotecario.backend.servicio.sql.visualizador;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.visualizador.LogsMW.LogMW;

@SuppressWarnings("serial")
public class LogsMW extends SqlObjetos<LogMW> {

	/* ========== ATRIBUTOS ========== */
	public static class LogMW extends SqlObjeto {
		public String ba_id_basic;
		public String ba_eventId;
		public String ba_channelId;
		public String ba_subChannelId;
		public String ba_userId;
		public String ba_userIP;
		public String ba_sessionId;
		public String ba_operationId;
		public String ba_serviceId;
		public String ba_sessionType;
		public String ba_resultCode;
		public String ba_eventType;
		public Fecha ba_startTime;
		public Fecha ba_endTime;
		public String ba_branch;
		public String ba_variantId;
		public Long ba_elapsedTime;
		public Long ba_orden;
		public String ba_targetSystem;
		public String ba_targetObjectType;
		public String ba_targetObjectName;
		public String ba_integrationId;
		public Fecha ba_providerStartTime;
		public Fecha ba_providerEndTime;
		public Long ba_providerElapsedTime;
	}

	/* ========== SERVICIO ========== */
	public static LogMW get(Contexto contexto, String idProceso) {
		String sql = "";
		sql += "SELECT TOP 1 * ";
		sql += "FROM [Visualizador].[dbo].[log_basic] ";
		sql += "WHERE ba_operationId = ? ";
		sql += "AND ba_startTime > ? ";
		sql += "ORDER BY ba_startTime";
		Objeto datos = Sql.select(contexto, "visualizador", sql, idProceso, Fecha.hoy());
		return map(datos, LogsMW.class, LogMW.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		LogMW datos = get(contexto, "1652683313");
		imprimirResultado(contexto, datos);
	}
}
