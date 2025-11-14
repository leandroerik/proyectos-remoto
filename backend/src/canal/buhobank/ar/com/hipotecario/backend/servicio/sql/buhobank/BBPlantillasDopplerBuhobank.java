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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank.BBPlantillaDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;

@SuppressWarnings("serial")
public class BBPlantillasDopplerBuhobank extends SqlObjetos<BBPlantillaDopplerBuhobank> {

	public static String TIPO_ALTA_TD_OK = "alta_td_ok";
	public static String TIPO_ALTA_TC_OK = "alta_tc_ok";
	public static String TIPO_ALTA_TD_VIRTUAL_OK = "alta_td_virtual_ok";
	public static String TIPO_ALTA_TC_VIRTUAL_OK = "alta_tc_virtual_ok";
	public static String TIPO_ALTA_ERROR = "alta_error";
	public static String TIPO_AVISO_ALTA_BATCH = "aviso_alta_batch";
	public static String TIPO_ALTA_BATCH_OK = "alta_batch_ok";
	public static String TIPO_ALTA_BATCH_ERROR = "alta_batch_error";
	
	//contenido	inversiones
	public static String ITEM_INVERSOR_TD_VIRTUAL_EMAIL_ALTA_OK = "<div style=\"display: flex;\"><div style=\"margin-block-start: 15px; width: 40px;text-align: center;\"><img src=\"https://www.hipotecario.com.ar/media/buhobank/tm-tick-negro.png\" height=\"25px\" width=\"25px\"></div><div><p style=\"color: #001517;font-size: 18px;\"><b>Tarjeta de Débito Virtual</b></p><p>¡Tu tarjeta ya está lista para usar!<br>Para ver los datos, seguí estos pasos:</p><ol><li>Ingresá a la App BH</li><li>Tocá la opción “Más” en la barra inferior</li><li>Presioná en “Tus Tarjetas” y elegí la opción “Débito”</li><li>¡Listo! Vas a ver los datos de tu tarjeta para empezar a operar</li></ol><p>Comprá online, asociala al pago de tus membresías digitales o agregala a tus billeteras virtuales.</p></div></div>";
	public static String ITEM_INVERSOR_EN_CAMINO_EMAIL_ALTA_OK = "<div style=\"display: flex;\"><div style=\"margin-block-start: 15px; width: 40px;text-align: center;\"><img src=\"https://www.hipotecario.com.ar/media/buhobank/tm-tick-negro.png\" height=\"25px\" width=\"25px\"></div><div><p style=\"color: #001517;font-size: 18px;\"><b>Tarjeta de Débito Física</b></p><p>Te avisaremos por mail cuando esté en camino para que puedas hacer el seguimiento. Recordá que el envío se hará a la dirección que nos indicaste.</p></div></div>";	
	public static String ITEM_CUENTA_INVERSOR_EMAIL_ALTA_OK = "<div style=\"display: flex;\"><div style=\"margin-block-start: 15px; width: 40px;text-align: center;\"><img src=\"https://www.hipotecario.com.ar/media/buhobank/tm-tick-negro.png\" height=\"25px\" width=\"25px\"></div><div><p style=\"color: #001517;font-size: 18px;\"><b>Cuenta Inversor</b></p><p>¡Tu cuenta Comitente y Cuotapartista ya están listas!<br>Empezá a invertir en Fondos Comunes de Inversión, Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.</p></div></div>";
	public static String ITEM_CUENTA_INVERSOR_FUERA_DE_HORARIO_EMAIL_ALTA_OK = "<div style=\"display: flex;\"><div style=\"margin-block-start: 15px; width: 40px;text-align: center;\"><img src=\"https://www.hipotecario.com.ar/media/buhobank/tm-tick-negro.png\" height=\"25px\" width=\"25px\"></div><div><p style=\"color: #001517;font-size: 18px;\"><b>Cuenta Inversor</b></p><p>¡Tu cuenta Comitente y Cuotapartista está en proceso!<br>Finaliza el alta de tu cuenta en la sección Inversiones dentro del<br>Home Banking o la App BH cualquier día hábil de 8 a 19hs.</p></div></div>";
	
	public static String[] TIPOS_VALIDOS = { TIPO_ALTA_TD_OK, TIPO_ALTA_TC_OK, TIPO_ALTA_TD_VIRTUAL_OK, TIPO_ALTA_TC_VIRTUAL_OK, TIPO_ALTA_ERROR, TIPO_AVISO_ALTA_BATCH, TIPO_ALTA_BATCH_OK, TIPO_ALTA_BATCH_ERROR };

	/* ========== ATRIBUTOS ========== */
	public static class BBPlantillaDopplerBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_plantilla_flujo;
		public String tipo;
		public String codigo;
		public String asunto;
	}

	public static BBPlantillasDopplerBuhobank buscarIdPlantilla(BBPlantillasDopplerBuhobank plantillasDoppler, Integer idPlantilla) {

		if (plantillasDoppler == null || Utils.isEmpty(idPlantilla)) {
			return null;
		}

		BBPlantillasDopplerBuhobank plantillasDopplerById = new BBPlantillasDopplerBuhobank();

		for (BBPlantillaDopplerBuhobank plantilla : plantillasDoppler) {

			if (idPlantilla.equals(plantilla.id_plantilla_flujo)) {
				plantillasDopplerById.add(plantilla);
			}
		}

		return plantillasDopplerById;
	}

	public static BBPlantillaDopplerBuhobank buscarTipo(BBPlantillasDopplerBuhobank plantillas, String tipo) {

		if (plantillas == null || Utils.isEmpty(tipo)) {
			return null;
		}

		for (BBPlantillaDopplerBuhobank plantilla : plantillas) {

			if (plantilla.tipo.equals(tipo)) {
				return plantilla;
			}
		}

		return null;
	}

	public static Boolean validate(Contexto contexto) {

		Boolean plantillasMinimas = validateTipos(contexto, TIPOS_VALIDOS);
		Boolean plantillasBuhobank = validateTipos(contexto, getTipos(contexto));

		return plantillasMinimas || plantillasBuhobank;

	}

	public static Boolean validateTipos(Contexto contexto, String[] tiposValidos) {

		if (tiposValidos == null) {
			return false;
		}

		BBPlantillasDopplerBuhobank plantillasDoppler = get(contexto);

		BBPlantillasFlujoBuhobank plantillasFlujo = BBPlantillasFlujoBuhobank.get(contexto);
		if (plantillasFlujo == null) {
			return false;
		}

		Boolean respuesta = false;
		for (BBPlantillaFlujoBuhobank plantillaFlujo : plantillasFlujo) {

			for (String tipoPlantilla : tiposValidos) {

				BBPlantillasDopplerBuhobank plantillasDopplerById = BBPlantillasDopplerBuhobank.buscarIdPlantilla(plantillasDoppler, plantillaFlujo.id);
				if (BBPlantillasDopplerBuhobank.buscarTipo(plantillasDopplerById, tipoPlantilla) == null) {

					BBPlantillaDopplerBuhobank nuevaPlantillaDoppler = new BBPlantillaDopplerBuhobank();
					nuevaPlantillaDoppler.id_plantilla_flujo = plantillaFlujo.id;
					nuevaPlantillaDoppler.tipo = tipoPlantilla;
					nuevaPlantillaDoppler.codigo = "";
					nuevaPlantillaDoppler.asunto = "";
					post(contexto, nuevaPlantillaDoppler);
					respuesta = true;
				}
			}
		}

		return respuesta;
	}

	public static BBPlantillaDopplerBuhobank buscarId(BBPlantillasDopplerBuhobank plantillasDoppler, Integer id) {

		if (plantillasDoppler == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBPlantillaDopplerBuhobank plantillaDoppler : plantillasDoppler) {

			if (id.equals(plantillaDoppler.id)) {
				return plantillaDoppler;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBPlantillaDopplerBuhobank plantillaDoppler, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(plantillaDoppler.id_plantilla_flujo) ? plantillaDoppler.id_plantilla_flujo : null;
		parametros[1] = !Util.empty(plantillaDoppler.tipo) ? plantillaDoppler.tipo : null;
		parametros[2] = !Util.empty(plantillaDoppler.codigo) ? plantillaDoppler.codigo : "";
		parametros[3] = !Util.empty(plantillaDoppler.asunto) ? plantillaDoppler.asunto : "";

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBPlantillasDopplerBuhobank getByFlujo(Contexto contexto, String flujo) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_plantillas_doppler] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo);
		return map(datos, BBPlantillasDopplerBuhobank.class, BBPlantillaDopplerBuhobank.class);
	}

	public static BBPlantillasDopplerBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_plantillas_doppler] WITH (NOLOCK) ";
		sql += "ORDER BY id_plantilla_flujo ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBPlantillasDopplerBuhobank.class, BBPlantillaDopplerBuhobank.class);
	}

	public static String[] getTipos(Contexto contexto) {

		try {

			String sql = "";
			sql += "SELECT tipo ";
			sql += "FROM [dbo].[bb_plantillas_doppler] WITH (NOLOCK) ";
			sql += "GROUP BY tipo ";

			Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
			BBPlantillasDopplerBuhobank plantillas = map(datos, BBPlantillasDopplerBuhobank.class, BBPlantillaDopplerBuhobank.class);
			if (plantillas == null) {
				return null;
			}

			String[] tipos = new String[plantillas.size()];
			for (int i = 0; i < plantillas.size(); i++) {
				tipos[i] = plantillas.get(i).tipo;
			}

			return tipos;

		} catch (Exception e) {
			return null;
		}
	}

	public static Boolean post(Contexto contexto, BBPlantillaDopplerBuhobank plantillaDoppler) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_plantillas_doppler] ";
		sql += "([id_plantilla_flujo], [tipo], [codigo], [asunto], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, GETDATE()) ";

		Object[] parametros = obtenerParametros(contexto, plantillaDoppler, 4);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_plantillas_doppler] (id_plantilla_flujo, tipo, codigo, asunto, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_plantilla_flujo, tipo, codigo, asunto, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_plantillas_doppler] WITH (NOLOCK) ";
		sql += "WHERE id_plantilla_flujo = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPlantillaBase) > 0;
	}

	public static Boolean put(Contexto contexto, BBPlantillaDopplerBuhobank plantillaDoppler) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_plantillas_doppler] SET ";
		sql += "id_plantilla_flujo = ? ,";
		sql += "tipo = ? ,";
		sql += "codigo = ? ,";
		sql += "asunto = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, plantillaDoppler, 5);
		parametros[4] = plantillaDoppler.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_plantillas_doppler] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}