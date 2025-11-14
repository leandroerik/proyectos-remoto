package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.BBAdjustsEsales.BBAdjustEsales;

@SuppressWarnings("serial")
public class BBAdjustsEsales extends SqlObjetos<BBAdjustEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class BBAdjustEsales extends SqlObjeto {
		public String Resultado;
		public String Detalle;
	}

	public static BBAdjustsEsales post(Contexto contexto, String adid, String claves, String valores) {

		String sql = "";
		sql += "EXEC [dbo].[BB_InsertarEventoAdjust] ";
		sql += " ? , ? , ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, adid, claves, valores);
		return map(datos, BBAdjustsEsales.class, BBAdjustEsales.class);
	}
}
