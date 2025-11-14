package ar.com.hipotecario.backend.servicio.sql.buhobank;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasGeneralBuhobank.BBParametriaGeneralBuhobank;

@SuppressWarnings("serial")
public class BBParametriasGeneralBuhobank extends SqlObjetos<BBParametriaGeneralBuhobank> {
	public static String PRENDIDO = "1";
	public static String CHATBOT_FECHA_ULTIMA_CONSULTA = "CHATBOT_FECHA_ULTIMA_CONSULTA";
	public static String CHATBOT_FILTRO_ESTADOS = "CHATBOT_FILTRO_ESTADOS";
	public static String CHATBOT_FILTRO_EVENTOS = "CHATBOT_FILTRO_EVENTOS";

	/* ========== ATRIBUTOS ========== */
	public static class BBParametriaGeneralBuhobank extends SqlObjeto {
		public Integer id;
		public String nombre;
		public String valor;
	}

	public static String buscarValor(BBParametriasGeneralBuhobank parametrias, String nombre) {

		BBParametriaGeneralBuhobank parametria = buscarNombre(parametrias, nombre);
		if (parametria == null) {
			return null;
		}

		return parametria.valor;
	}

	public static BBParametriaGeneralBuhobank buscarNombre(BBParametriasGeneralBuhobank parametrias, String nombre) {

		if (parametrias == null || Utils.isEmpty(nombre)) {
			return null;
		}

		for (BBParametriaGeneralBuhobank parametria : parametrias) {

			if (nombre.equals(parametria.nombre)) {
				return parametria;
			}
		}

		return null;
	}

	public static BBParametriaGeneralBuhobank buscarId(BBParametriasGeneralBuhobank parametrias, Integer id) {

		if (parametrias == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBParametriaGeneralBuhobank parametria : parametrias) {

			if (id.equals(parametria.id)) {
				return parametria;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBParametriaGeneralBuhobank parametria, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(parametria.nombre) ? parametria.nombre : null;
		parametros[1] = !Util.empty(parametria.valor) ? parametria.valor : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBParametriasGeneralBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_parametrias_general] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBParametriasGeneralBuhobank.class, BBParametriaGeneralBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBParametriaGeneralBuhobank nuevaParametria) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_parametrias_general] ";
		sql += "([nombre], [valor], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaParametria, 2);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBParametriaGeneralBuhobank parametria) {

		String sql = "";
		sql += "UPDATE[dbo].[bb_parametrias_general] SET ";
		sql += "nombre = ? , ";
		sql += "valor = ? , ";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, parametria, 3);
		parametros[2] = parametria.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_parametrias_general] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean putByNombre(Contexto contexto, String nombre, String valor) {

		String sql = "";
		sql += "UPDATE[dbo].[bb_parametrias_general] SET ";
		sql += "valor = ? , ";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE nombre = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, valor, nombre) > 0;
	}

}
