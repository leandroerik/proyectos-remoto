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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;

@SuppressWarnings("serial")
public class BBPlantillasFlujoBuhobank extends SqlObjetos<BBPlantillaFlujoBuhobank> {

	public static class BBPlantillaFlujoBuhobank extends SqlObjeto {
		public Integer id;
		public String plantilla;
	}

	public static BBPlantillasFlujoBuhobank buscarIdPlantilla(BBPlantillasFlujoBuhobank plantillas, Integer idPlantilla) {

		if (plantillas == null || Utils.isEmpty(idPlantilla)) {
			return null;
		}

		BBPlantillasFlujoBuhobank plantillasById = new BBPlantillasFlujoBuhobank();

		for (BBPlantillaFlujoBuhobank plantilla : plantillas) {

			if (idPlantilla.equals(plantilla.id)) {
				plantillasById.add(plantilla);
			}
		}

		return plantillasById;
	}

	public static BBPlantillaFlujoBuhobank buscarNombre(BBPlantillasFlujoBuhobank plantillas, String nombre) {

		if (plantillas == null || Utils.isEmpty(nombre)) {
			return null;
		}

		for (BBPlantillaFlujoBuhobank plantilla : plantillas) {

			if (nombre.equals(plantilla.plantilla)) {
				return plantilla;
			}
		}

		return null;
	}

	public static Boolean existeIdPlantilla(BBPlantillasFlujoBuhobank plantillas, Integer idPlantilla) {

		BBPlantillasFlujoBuhobank plantillasById = buscarIdPlantilla(plantillas, idPlantilla);
		if (plantillasById != null && plantillasById.size() > 0) {
			return true;
		}

		return false;
	}

	public static Boolean existeNombre(BBPlantillasFlujoBuhobank plantillas, String nombre) {

		BBPlantillaFlujoBuhobank plantilla = buscarNombre(plantillas, nombre);
		if (plantilla != null) {
			return true;
		}

		return false;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBPlantillaFlujoBuhobank plantillaFlujo, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(plantillaFlujo.plantilla) ? plantillaFlujo.plantilla : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBPlantillasFlujoBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_plantillas_flujo] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBPlantillasFlujoBuhobank.class, BBPlantillaFlujoBuhobank.class);
	}

	public static BBPlantillaFlujoBuhobank getFlujo(Contexto contexto, String flujo) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_plantillas_flujo] WITH (NOLOCK) ";
		sql += "WHERE plantilla = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo);
		return map(datos, BBPlantillasFlujoBuhobank.class, BBPlantillaFlujoBuhobank.class).first();
	}

	public static Boolean post(Contexto contexto, BBPlantillaFlujoBuhobank nuevaPlantillaFlujo) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_plantillas_flujo] ";
		sql += "([plantilla], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaPlantillaFlujo, 1);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBPlantillaFlujoBuhobank plantilla) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_plantillas_flujo] SET ";
		sql += "plantilla = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, plantilla, 2);
		parametros[1] = plantilla.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_plantillas_flujo] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
