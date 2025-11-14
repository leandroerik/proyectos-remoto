package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.CodigosAreasBb.CodigoAreasBb;

@SuppressWarnings("serial")
public class CodigosAreasBb extends SqlObjetos<CodigoAreasBb> {

	/* ========== ATRIBUTOS ========== */
	public static class CodigoAreasBb extends SqlObjeto {
		public String id;
		public String num_intercambio;
		public String tabla;
		public String codigo;
		public String valor;
		public String estado;

	}

	/* ========== SERVICIO ========== */

	public static CodigosAreasBb get(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[codigos_areas_bb] WITH (NOLOCK) ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, CodigosAreasBb.class, CodigoAreasBb.class);
	}

	public static CodigosAreasBb getPorProvincia(Contexto contexto, String provincia) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[codigos_areas_bb] WITH (NOLOCK) ";
		sql += "WHERE valor = ?";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, provincia);
		return map(datos, CodigosAreasBb.class, CodigoAreasBb.class);
	}

	public static CodigosAreasBb getPorCodigo(Contexto contexto, String codigo) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[codigos_areas_bb] WITH (NOLOCK) ";
		sql += "WHERE codigo = ?";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, codigo);
		return map(datos, CodigosAreasBb.class, CodigoAreasBb.class);
	}

}
