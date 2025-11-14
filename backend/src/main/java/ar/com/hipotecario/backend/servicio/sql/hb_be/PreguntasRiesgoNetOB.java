package ar.com.hipotecario.backend.servicio.sql.hb_be;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.hb_be.PreguntasRiesgoNetOB.PreguntaRiesgoNet;

@SuppressWarnings("serial")
public class PreguntasRiesgoNetOB extends SqlObjetos<PreguntaRiesgoNet> {

	/* ========== ATRIBUTOS ========== */
	public static class PreguntaRiesgoNet extends SqlObjeto {
		public String emp_cuit;
		public String usu_cuil;
		public Fecha momento;
		public Boolean exitoso;
	}

	/* ========== SERVICIO ========== */
	public static Boolean registrarIntento(Contexto contexto, String emp_cuit, String usu_cuil, int exitoso) {
		Fecha momento = Fecha.ahora();
		String sql = "";
		sql += "INSERT INTO [HB_BE].[dbo].[OB_Riesgonet] ";
		sql += "([emp_cuit], [usu_cuil], [momento], [exitoso]) ";
		sql += "VALUES (?, ?, ?, ?) ";

		Object[] parametros = new Object[4];
		parametros[0] = emp_cuit;
		parametros[1] = usu_cuil;
		parametros[2] = momento;
		parametros[3] = exitoso;

		return Sql.update(contexto, "hb_be", sql, parametros) == 1;
	}

	public static Boolean consultarUsuarioBloqueado(Contexto contexto, String usu_cuil) {
		Integer cantidadDiasBloqueoRiesgonet = contexto.config.integer("cantidad_dias_bloqueo_riesgonet");
		Integer cantidadIntentosBloqueoRiesgonet = contexto.config.integer("cantidad_intentos_bloqueo_riesgonet");

		Fecha momento = Fecha.ahora().restarDias(cantidadDiasBloqueoRiesgonet);

		String sql = "";
		sql += "SELECT count(1) as intentos ";
		sql += "FROM [hb_be].[dbo].[OB_Riesgonet] ";
		sql += "WHERE momento > ? ";
		sql += "AND usu_cuil =  ? ";
		sql += "AND exitoso = 0 ";
		Objeto datos = Sql.select(contexto, "hb_be", sql, momento, usu_cuil);
		Integer intentos = (Integer) datos.objetos(0).get("intentos");
		if (intentos >= cantidadIntentosBloqueoRiesgonet) {
			return true;
		} else {
			return false;
		}
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		imprimirResultado(contexto, registrarIntento(contexto, "20069041486", "20309574592", 0));
		imprimirResultado(contexto, consultarUsuarioBloqueado(contexto, "20069041486"));
	}
}
