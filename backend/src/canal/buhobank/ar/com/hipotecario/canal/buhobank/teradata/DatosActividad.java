package ar.com.hipotecario.canal.buhobank.teradata;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.canal.buhobank.teradata.DatosActividad.DatoActividad;

@SuppressWarnings("serial")
public class DatosActividad extends SqlObjetos<DatoActividad> {

	/* ========== ATRIBUTOS ========== */
	public static class DatoActividad extends SqlObjeto {
		public String cuit;
		public String imp_ganancias;
		public String imp_iva;
		public String monotributo;
		public String integrante_sociedad;
		public String empleador;
	}

	/* ========== SERVICIO ========== */
	public static DatoActividad get(Contexto contexto, String cuit) {

		String esquema = getEsquema(contexto);
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM " + esquema;
		sql += "ff_afip_condicion_tributaria ";
		sql += "where cuit = ? ";

		Objeto datos = Sql.select(contexto, "teradata", sql, cuit);
		return map(datos, DatosActividad.class, DatoActividad.class).first();
	}

	protected static String getEsquema(Contexto contexto) {
		if (!contexto.esProduccion()) {
			return "d_lnd.";
		} else {
			return "p_lnd.";
		}
	}

	/* ========== TEST ========== */

	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		imprimirResultado(contexto, get(contexto, "20044067480"));
	}

}
