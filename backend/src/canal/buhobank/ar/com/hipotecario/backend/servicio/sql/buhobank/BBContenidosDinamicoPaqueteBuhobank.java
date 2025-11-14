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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoPaqueteBuhobank.BBContenidoDinamicoPaqueteBuhobank;

@SuppressWarnings("serial")
public class BBContenidosDinamicoPaqueteBuhobank extends SqlObjetos<BBContenidoDinamicoPaqueteBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBContenidoDinamicoPaqueteBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_paquete;
		public String tipo;
		public String imagen;
		public String titulo;
		public String descripcion;
		public String texto;
		public String texto_legales;
		public Boolean habilitado;
	}

	private static BBContenidosDinamicoPaqueteBuhobank buscarIdPaquete(BBContenidosDinamicoPaqueteBuhobank contenidos, Integer idPaquete) {

		if (contenidos == null || Utils.isEmpty(idPaquete)) {
			return null;
		}

		BBContenidosDinamicoPaqueteBuhobank contenidosById = new BBContenidosDinamicoPaqueteBuhobank();

		for (BBContenidoDinamicoPaqueteBuhobank contenido : contenidos) {

			if (idPaquete.equals(contenido.id_paquete)) {
				contenidosById.add(contenido);
			}
		}

		return contenidosById;
	}

	public static BBContenidoDinamicoPaqueteBuhobank buscarId(BBContenidosDinamicoPaqueteBuhobank contenidos, Integer id) {
		if (contenidos == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBContenidoDinamicoPaqueteBuhobank contenido : contenidos) {

			if (id.equals(contenido.id)) {
				return contenido;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBContenidoDinamicoPaqueteBuhobank contenido, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(contenido.id_paquete) ? contenido.id_paquete : "";
		parametros[1] = !Util.empty(contenido.tipo) ? contenido.tipo : "";
		parametros[2] = !Util.empty(contenido.imagen) ? contenido.imagen : "";
		parametros[3] = !Util.empty(contenido.titulo) ? contenido.titulo : "";
		parametros[4] = !Util.empty(contenido.descripcion) ? contenido.descripcion : "";
		parametros[5] = !Util.empty(contenido.texto) ? contenido.texto : "";
		parametros[6] = !Util.empty(contenido.texto_legales) ? contenido.texto_legales : "";
		parametros[7] = !Util.empty(contenido.habilitado) ? contenido.habilitado : "";

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBContenidosDinamicoPaqueteBuhobank get(Contexto contexto) {
		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_contenidos_dinamico_paquete] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBContenidosDinamicoPaqueteBuhobank.class, BBContenidoDinamicoPaqueteBuhobank.class);
	}

	public static BBContenidosDinamicoPaqueteBuhobank getByTipo(Contexto contexto, String idPaquete, String tipo) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_contenidos_dinamico_paquete] WITH (NOLOCK) ";
		sql += "WHERE id_paquete = ? ";
		sql += "AND tipo = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete, tipo);
		return map(datos, BBContenidosDinamicoPaqueteBuhobank.class, BBContenidoDinamicoPaqueteBuhobank.class);
	}

	public static void clonarTotal(Contexto contexto, Integer idPaqueteBase, Integer idPaquete) {

		BBContenidosDinamicoPaqueteBuhobank contenidosBuhobank = get(contexto);
		BBContenidosDinamicoPaqueteBuhobank contenidosBase = buscarIdPaquete(contenidosBuhobank, idPaqueteBase);
		if (contenidosBase == null || contenidosBase.size() == 0) {
			return;
		}

		for (BBContenidoDinamicoPaqueteBuhobank contenidoBase : contenidosBase) {

			clonarContenido(contexto, contenidoBase.id, idPaquete);
		}
	}

	private static BBContenidoDinamicoPaqueteBuhobank clonarContenido(Contexto contexto, Integer idContenido, Integer idPaquete) {
		String sql = "";
		sql += "INSERT [dbo].[bb_contenidos_dinamico_paquete] (id_paquete, tipo, imagen, titulo, descripcion, texto, texto_legales, habilitado, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_paquete, tipo, imagen, titulo, descripcion, texto, texto_legales, habilitado, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_contenidos_dinamico_paquete] WITH (NOLOCK) ";
		sql += "WHERE id = ? ;";
		sql += "SELECT SCOPE_IDENTITY() AS id";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete, idContenido);
		return map(datos, BBContenidosDinamicoPaqueteBuhobank.class, BBContenidoDinamicoPaqueteBuhobank.class).first();
	}

	public static Boolean post(Contexto contexto, BBContenidoDinamicoPaqueteBuhobank nuevoContenido) {
		String sql = "";
		sql += "INSERT INTO [dbo].[bb_contenidos_dinamico_paquete] ";
		sql += "([id_paquete], [tipo], [imagen], [titulo], [descripcion], [texto], [texto_legales], [habilitado], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevoContenido, 8);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBContenidoDinamicoPaqueteBuhobank contenido) {
		String sql = "";
		sql += "UPDATE [dbo].[bb_contenidos_dinamico_paquete] SET ";
		sql += "id_paquete = ? , ";
		sql += "tipo = ? , ";
		sql += "imagen = ? , ";
		sql += "titulo = ? , ";
		sql += "descripcion = ? , ";
		sql += "texto = ? , ";
		sql += "texto_legales = ? , ";
		sql += "habilitado = ? , ";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, contenido, 9);
		parametros[8] = contenido.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {
		String sql = "";
		sql += "DELETE [dbo].[bb_contenidos_dinamico_paquete] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
