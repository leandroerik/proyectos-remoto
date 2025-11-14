package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ContenidosBBEsales.ContenidoBBEsales;

@SuppressWarnings("serial")
public class ContenidosBBEsales extends SqlObjetos<ContenidoBBEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class ContenidoBBEsales extends SqlObjeto {
		public String id;
		public String tipo;
		public String nombre;
		public String imagen;
		public String titular;
		public String titular_html;
		public String bajada;
		public String bajada_html;
		public String bajada_legales;
		public String bajada_legales_html;

		/* ========== METODOS ========== */

	}

	public static ContenidoBBEsales buscarId(ContenidosBBEsales contenidos, String id) {

		if (contenidos == null || Util.empty(id)) {
			return null;
		}

		for (ContenidoBBEsales contenido : contenidos) {
			if (contenido.id.equals(id)) {
				return contenido;
			}
		}

		return null;
	}

	/* ========== SERVICIO ========== */

	private static Object[] obtenerParametros(Contexto contexto, ContenidoBBEsales contenido, int cantidad) {
		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(contenido.tipo) ? contenido.tipo : null;
		parametros[1] = !Util.empty(contenido.imagen) ? contenido.imagen : null;
		parametros[2] = !Util.empty(contenido.titular) ? contenido.titular : null;
		parametros[3] = !Util.empty(contenido.bajada) ? contenido.bajada : null;
		parametros[4] = !Util.empty(contenido.bajada_legales) ? contenido.bajada_legales : null;
		parametros[5] = !Util.empty(contenido.titular_html) ? contenido.titular_html : null;
		parametros[6] = !Util.empty(contenido.bajada_html) ? contenido.bajada_html : null;
		parametros[7] = !Util.empty(contenido.bajada_legales_html) ? contenido.bajada_legales_html : null;
		parametros[8] = !Util.empty(contenido.nombre) ? contenido.nombre : null;

		return parametros;
	}

	public static ContenidosBBEsales get(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[contenido_onboarding] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, ContenidosBBEsales.class, ContenidoBBEsales.class);
	}

	public static ContenidosBBEsales getByTipo(Contexto contexto, String tipo) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[contenido_onboarding] WITH (NOLOCK) ";
		sql += "WHERE tipo = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, tipo);
		return map(datos, ContenidosBBEsales.class, ContenidoBBEsales.class);
	}

	public static ContenidosBBEsales getPromosLanding(Contexto contexto) {
		return getByTipo(contexto, "promolanding");
	}

	public static Boolean put(Contexto contexto, ContenidoBBEsales contenido) {
		String sql = "";
		sql += "UPDATE [dbo].[contenido_onboarding] SET ";
		sql += "[tipo] = ? , ";
		sql += "[imagen] = ? , ";
		sql += "[titular] = ? , ";
		sql += "[bajada] = ? , ";
		sql += "[bajada_legales] = ? , ";
		sql += "[titular_html] = ? , ";
		sql += "[bajada_html] = ? , ";
		sql += "[bajada_legales_html] = ? , ";
		sql += "[nombre] = ? ";
		sql += "WHERE id = ? ";
		Object[] parametros = obtenerParametros(contexto, contenido, 10);

		parametros[9] = contenido.id;

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean post(Contexto contexto, ContenidoBBEsales contenido) {

		String sql = "";
		sql += "INSERT INTO [dbo].[contenido_onboarding] ";
		sql += "([tipo], [imagen], ";
		sql += "[titular], [bajada], [bajada_legales], ";
		sql += "[titular_html], [bajada_html], [bajada_legales_html], [nombre]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, contenido, 9);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean delete(Contexto contexto, String id) {

		String sql = "";
		sql += "DELETE FROM [dbo].[contenido_onboarding] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, id) == 1;
	}

}
