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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank.BBContenidoDinamicoBuhobank;

@SuppressWarnings("serial")
public class BBContenidosDinamicoBuhobank extends SqlObjetos<BBContenidoDinamicoBuhobank> {

	// tipos
	public static String TIPO_FINALIZAR_LOGO_PRINCIPAL = "finalizar_logo_principal";
	public static String TIPO_FINALIZAR_CLAVE_ACTIVA = "finalizar_clave_activa";
	public static String TIPO_FINALIZAR_SIN_CLAVE_ACTIVA = "finalizar_sin_clave_activa";
	public static String TIPO_FINALIZAR_BATCH_TEXT_BOX = "finalizar_batch_text_box";
	public static String TIPO_FINALIZAR_BATCH_TEXTO_BOTON = "finalizar_batch_texto_boton";
	public static String TIPO_LANDING_PROMOCIONES = "landing_promociones";
	public static String TIPO_LANDING_PROMOCIONES_TYC = "landing_promociones_tyc";

	/* ========== ATRIBUTOS ========== */
	public static class BBContenidoDinamicoBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_plantilla_flujo;
		public String tipo;
		public String imagen;
		public String titulo;
		public String texto;
		public String texto_legales;
		public String texto_tyc;
		public Boolean habilitado;
	}

	public static BBContenidoDinamicoBuhobank buscarId(BBContenidosDinamicoBuhobank contenidos, Integer id) {

		if (contenidos == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBContenidoDinamicoBuhobank contenido : contenidos) {

			if (id.equals(contenido.id)) {
				return contenido;
			}
		}

		return null;
	}

	public static BBContenidosDinamicoBuhobank filtrarPlantillasHabilitadas(BBContenidosDinamicoBuhobank contenidos) {

		if (contenidos == null) {
			return null;
		}

		BBContenidosDinamicoBuhobank contenidosHabilitados = new BBContenidosDinamicoBuhobank();

		for (BBContenidoDinamicoBuhobank contenido : contenidos) {

			if (contenido.habilitado) {
				contenidosHabilitados.add(contenido);
			}
		}

		return contenidosHabilitados;
	}

	public static String obtenerPrimerTitulo(BBContenidosDinamicoBuhobank contenidosDinamico) {

		if (contenidosDinamico != null && contenidosDinamico.size() > 0) {
			return contenidosDinamico.first().titulo;
		}

		return null;
	}

	public static String obtenerPrimerImagen(BBContenidosDinamicoBuhobank contenidosDinamico) {

		if (contenidosDinamico != null && contenidosDinamico.size() > 0) {
			return contenidosDinamico.first().imagen;
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBContenidoDinamicoBuhobank contenidoDinamico, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(contenidoDinamico.id_plantilla_flujo) ? contenidoDinamico.id_plantilla_flujo : null;
		parametros[1] = !Util.empty(contenidoDinamico.tipo) ? contenidoDinamico.tipo : null;
		parametros[2] = !Util.empty(contenidoDinamico.imagen) ? contenidoDinamico.imagen : null;
		parametros[3] = !Util.empty(contenidoDinamico.titulo) ? contenidoDinamico.titulo : null;
		parametros[4] = !Util.empty(contenidoDinamico.texto) ? contenidoDinamico.texto : null;
		parametros[5] = !Util.empty(contenidoDinamico.texto_legales) ? contenidoDinamico.texto_legales : null;
		parametros[6] = !Util.empty(contenidoDinamico.texto_tyc) ? contenidoDinamico.texto_tyc : null;
		parametros[7] = !Util.empty(contenidoDinamico.habilitado) ? contenidoDinamico.habilitado : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBContenidosDinamicoBuhobank getByTipo(Contexto contexto, String flujo, String tipo) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_contenidos_dinamico] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";
		sql += "AND t2.tipo = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo, tipo);
		return map(datos, BBContenidosDinamicoBuhobank.class, BBContenidoDinamicoBuhobank.class);
	}

	public static BBContenidosDinamicoBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_contenidos_dinamico] WITH (NOLOCK) ";
		sql += "ORDER BY id_plantilla_flujo ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBContenidosDinamicoBuhobank.class, BBContenidoDinamicoBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBContenidoDinamicoBuhobank contenidoDinamico) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_contenidos_dinamico] ";
		sql += "([id_plantilla_flujo], [tipo], [imagen], [titulo], [texto], [texto_legales], [texto_tyc], [habilitado], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, contenidoDinamico, 8);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBContenidoDinamicoBuhobank contenidoDinamico) {

		String sql = "";
		sql += "UPDATE[dbo].[bb_contenidos_dinamico] SET ";
		sql += "id_plantilla_flujo = ? ,";
		sql += "tipo = ? ,";
		sql += "imagen = ? ,";
		sql += "titulo = ? ,";
		sql += "texto = ? ,";
		sql += "texto_legales = ? ,";
		sql += "texto_tyc = ? ,";
		sql += "habilitado = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, contenidoDinamico, 9);
		parametros[8] = contenidoDinamico.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_contenidos_dinamico] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_contenidos_dinamico] (id_plantilla_flujo, tipo, imagen, titulo, texto, texto_legales, texto_tyc, habilitado, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_plantilla_flujo, tipo, imagen, titulo, texto, texto_legales, texto_tyc, habilitado, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_contenidos_dinamico] WITH (NOLOCK) ";
		sql += "WHERE id_plantilla_flujo = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPlantillaBase) > 0;
	}
}
