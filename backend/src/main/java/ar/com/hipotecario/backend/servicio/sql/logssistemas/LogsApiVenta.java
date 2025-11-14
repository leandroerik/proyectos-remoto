package ar.com.hipotecario.backend.servicio.sql.logssistemas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.logssistemas.LogsApiVenta.LogApiVenta;

@SuppressWarnings("serial")
public class LogsApiVenta extends SqlObjetos<LogApiVenta> {

	/* ========== ATRIBUTOS ========== */
	public static class LogApiVenta extends SqlObjeto {
		public String ID;
		public Fecha FechaInicio;
		public Fecha FechaFin;
		public String Clase;
		public String Metodo;
		public String Handle;
		public String RequestSource;
		public Integer ResultCode;
		public Integer FaultCode;
		public String Server;
	}

	/* ========== SERVICIO ========== */
	public static LogApiVenta get(Contexto contexto, String idProceso) {
		String sql = "";
		sql += "SELECT TOP 1 ID, FechaInicio, FechaFin, Clase, Metodo, Handle, RequestSource, ResultCode, FaultCode, Server ";
		sql += "FROM [LogsSistemas].[dbo].[LogWSAPIVentas] ";
		sql += "WHERE FechaInicio > ? ";
		sql += "AND XmlIn LIKE ?";
		Objeto datos = Sql.select(contexto, "logssistemas", sql, Fecha.ayer(), "%\"" + idProceso + "\"%");
		return map(datos, LogsApiVenta.class, LogApiVenta.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		LogApiVenta datos = get(contexto, "1579211738");
		imprimirResultado(contexto, datos);
	}
}
