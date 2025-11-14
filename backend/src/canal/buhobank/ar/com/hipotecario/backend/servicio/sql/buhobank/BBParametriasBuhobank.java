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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank.BBParametriaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

@SuppressWarnings("serial")
public class BBParametriasBuhobank extends SqlObjetos<BBParametriaBuhobank> {
	public static String PRENDIDO = "1";
	public static String CUENTA_SUELDO = "CUENTA_SUELDO";
	public static String URL_BACKOFFICE_VU = "URL_BACKOFFICE_VU";
	public static String KEY_BACKOFFICE_VU = "KEY_BACKOFFICE_VU";
	public static String KEY_PRIVADA_BACKOFFICE_VU = "KEY_PRIVADA_BACKOFFICE_VU";
	public static String HORARIO_BATCH = "HORARIO_BATCH";
	public static String ENVIO_ANDRIANI = "ENVIO_ANDRIANI";
	public static String PREVENCION_STANDALONE = "PREVENCION_STANDALONE";
	public static String TC_ONLINE = "TC_ONLINE";
	public static String OTP_V2 = "OTP_V2";

	/* ========== ATRIBUTOS ========== */
	public static class BBParametriaBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_plantilla_flujo;
		public String nombre;
		public String valor_android;
		public String valor_ios;
	}

	public static Boolean estaPrendido(BBParametriasBuhobank parametrias, String nombreParametria, String plataforma) {

		BBParametriaBuhobank parametria = buscarNombre(parametrias, nombreParametria);
		if (parametria == null) {
			return false;
		}

		return PRENDIDO.equals(GeneralBB.PLATAFORMA_IOS.equals(plataforma) ? parametria.valor_ios : parametria.valor_android);
	}

	public static String obtenerValor(BBParametriasBuhobank parametrias, String nombre, String plataforma) {

		BBParametriaBuhobank parametria = buscarNombre(parametrias, nombre);
		if (parametria == null) {
			return "";
		}

		return GeneralBB.PLATAFORMA_IOS.equals(plataforma) ? parametria.valor_ios : parametria.valor_android;
	}

	public static BBParametriaBuhobank buscarNombre(BBParametriasBuhobank parametrias, String nombre) {

		if (parametrias == null || Utils.isEmpty(nombre)) {
			return null;
		}

		for (BBParametriaBuhobank parametria : parametrias) {

			if (nombre.equals(parametria.nombre)) {
				return parametria;
			}
		}

		return null;
	}

	public static BBParametriaBuhobank buscarId(BBParametriasBuhobank parametrias, Integer id) {

		if (parametrias == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBParametriaBuhobank parametria : parametrias) {

			if (id.equals(parametria.id)) {
				return parametria;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBParametriaBuhobank parametria, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(parametria.id_plantilla_flujo) ? parametria.id_plantilla_flujo : null;
		parametros[1] = !Util.empty(parametria.nombre) ? parametria.nombre : null;
		parametros[2] = !Util.empty(parametria.valor_android) ? parametria.valor_android : "";
		parametros[3] = !Util.empty(parametria.valor_ios) ? parametria.valor_ios : "";

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBParametriasBuhobank getByFlujo(Contexto contexto, String flujo) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_parametrias] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo);
		return map(datos, BBParametriasBuhobank.class, BBParametriaBuhobank.class);
	}

	public static BBParametriasBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_parametrias] WITH (NOLOCK) ";
		sql += "ORDER BY id_plantilla_flujo ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBParametriasBuhobank.class, BBParametriaBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBParametriaBuhobank nuevaParametria) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_parametrias] ";
		sql += "([id_plantilla_flujo], [nombre], [valor_android], [valor_ios], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaParametria, 4);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_parametrias] (id_plantilla_flujo, nombre, valor_android, valor_ios, fecha_ultima_modificacion)";
		sql += "SELECT ? AS id_plantilla_flujo, nombre, valor_android, valor_ios, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_parametrias] WITH (NOLOCK) ";
		sql += "WHERE id_plantilla_flujo = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPlantillaBase) > 0;
	}

	public static Boolean put(Contexto contexto, BBParametriaBuhobank parametria) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_parametrias] SET ";
		sql += "id_plantilla_flujo = ? , ";
		sql += "nombre = ? , ";
		sql += "valor_android = ? , ";
		sql += "valor_ios = ? , ";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, parametria, 5);
		parametros[4] = parametria.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_parametrias] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean putFullNombre(Contexto contexto, String nombre, String valor) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_parametrias] SET ";
		sql += "valor_android = ? , ";
		sql += "valor_ios = ? , ";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE nombre = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, valor, valor, nombre) > 0;
	}

}
