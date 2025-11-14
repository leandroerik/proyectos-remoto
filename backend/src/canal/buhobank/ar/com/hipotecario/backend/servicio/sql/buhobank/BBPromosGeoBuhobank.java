package ar.com.hipotecario.backend.servicio.sql.buhobank;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPromosGeoBuhobank.BBPromoGeoBuhobank;

@SuppressWarnings("serial")
public class BBPromosGeoBuhobank extends SqlObjetos<BBPromoGeoBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBPromoGeoBuhobank extends SqlObjeto {

		public String logo;
		public String titulo;
		public String highlight;
		public String categoria;
		public String beneficio;
		public String DistanciaKm;
		public String legales;
		public String tyc;
		public Integer id_plantilla_flujo;
	}

	/* ========== SERVICIO ========== */
	public static BBPromosGeoBuhobank get(Contexto contexto, String latitud, String longitud, String radio) {

		String sql = "EXEC [dbo].[SP_BuscarPorLatitudLongitud] ";
		sql += " ? , ? , ?";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, latitud, longitud, radio);
		return map(datos, BBPromosGeoBuhobank.class, BBPromoGeoBuhobank.class);
	}
}
