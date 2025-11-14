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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesSubproductoBuhobank.BBPaqueteSubproductoBuhobank;

@SuppressWarnings("serial")
public class BBPaquetesSubproductoBuhobank extends SqlObjetos<BBPaqueteSubproductoBuhobank> {

	public static String TIPO_PROGRAMA_FIDELIZACION = "programa_fidelizacion";

	/* ========== ATRIBUTOS ========== */
	public static class BBPaqueteSubproductoBuhobank extends SqlObjeto {
		public Integer numero_paquete;

		public Integer id;
		public Integer id_paquete;
		public String tipo;
		public String codigo;
		public String titulo;
		public String descripcion;
		public String url_legales;
		public String imagen;
		public Boolean habilitado;
	}

	private static BBPaquetesSubproductoBuhobank buscarIdPaquete(BBPaquetesSubproductoBuhobank paquetesSubproducto, Integer idPaquete) {

		if (paquetesSubproducto == null || Utils.isEmpty(idPaquete)) {
			return null;
		}

		BBPaquetesSubproductoBuhobank paquetesSubproductoById = new BBPaquetesSubproductoBuhobank();

		for (BBPaqueteSubproductoBuhobank tarjeta : paquetesSubproducto) {

			if (idPaquete.equals(tarjeta.id_paquete)) {
				paquetesSubproductoById.add(tarjeta);
			}
		}

		return paquetesSubproductoById;
	}

	public static BBPaquetesSubproductoBuhobank buscarTipo(BBPaquetesSubproductoBuhobank subproductosBuhobank, String tipo) {

		if (subproductosBuhobank == null || Utils.isEmpty(tipo)) {
			return null;
		}

		BBPaquetesSubproductoBuhobank subproductoByTipo = new BBPaquetesSubproductoBuhobank();

		for (BBPaqueteSubproductoBuhobank subproducto : subproductosBuhobank) {

			if (tipo.equals(subproducto.tipo)) {
				subproductoByTipo.add(subproducto);
			}
		}

		return subproductoByTipo;
	}

	public static BBPaquetesSubproductoBuhobank filtrarHabilitados(BBPaquetesSubproductoBuhobank subproductosBuhobank) {

		if (subproductosBuhobank == null) {
			return null;
		}

		BBPaquetesSubproductoBuhobank subproductoHabilitados = new BBPaquetesSubproductoBuhobank();

		for (BBPaqueteSubproductoBuhobank subproducto : subproductosBuhobank) {

			if (subproducto.habilitado) {
				subproductoHabilitados.add(subproducto);
			}
		}

		return subproductoHabilitados;
	}

	public static BBPaqueteSubproductoBuhobank buscarId(BBPaquetesSubproductoBuhobank paquetesSubproducto, Integer id) {

		if (paquetesSubproducto == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBPaqueteSubproductoBuhobank paqueteSubproducto : paquetesSubproducto) {

			if (id.equals(paqueteSubproducto.id)) {
				return paqueteSubproducto;
			}
		}

		return null;
	}

	public static void clonarTotal(Contexto contexto, Integer idPaqueteBase, Integer idPaquete) {

		BBPaquetesSubproductoBuhobank paquetesSubproductoBuhobank = get(contexto);
		BBPaquetesSubproductoBuhobank paquetesSubproductoBase = buscarIdPaquete(paquetesSubproductoBuhobank, idPaqueteBase);
		if (paquetesSubproductoBase == null || paquetesSubproductoBase.size() == 0) {
			return;
		}

		for (BBPaqueteSubproductoBuhobank paqueteSubproductoBase : paquetesSubproductoBase) {
			clonarSubproducto(contexto, paqueteSubproductoBase.id, idPaquete);
		}
	}

	private static Object[] obtenerParametros(Contexto contexto, BBPaqueteSubproductoBuhobank paqueteSubproducto, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(paqueteSubproducto.id_paquete) ? paqueteSubproducto.id_paquete : null;
		parametros[1] = !Util.empty(paqueteSubproducto.tipo) ? paqueteSubproducto.tipo : null;
		parametros[2] = !Util.empty(paqueteSubproducto.codigo) ? paqueteSubproducto.codigo : null;
		parametros[3] = !Util.empty(paqueteSubproducto.titulo) ? paqueteSubproducto.titulo : null;
		parametros[4] = !Util.empty(paqueteSubproducto.descripcion) ? paqueteSubproducto.descripcion : null;
		parametros[5] = !Util.empty(paqueteSubproducto.url_legales) ? paqueteSubproducto.url_legales : null;
		parametros[6] = !Util.empty(paqueteSubproducto.imagen) ? paqueteSubproducto.imagen : null;
		parametros[7] = !Util.empty(paqueteSubproducto.habilitado) ? paqueteSubproducto.habilitado : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBPaquetesSubproductoBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 ";
		sql += "INNER JOIN [dbo].[bb_paquetes_subproducto] AS t2 ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBPaquetesSubproductoBuhobank.class, BBPaqueteSubproductoBuhobank.class);
	}

	public static BBPaquetesSubproductoBuhobank getByPaquete(Contexto contexto, Integer idPaquete) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_paquetes_subproducto] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "WHERE t2.id_paquete = ? ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete);
		return map(datos, BBPaquetesSubproductoBuhobank.class, BBPaqueteSubproductoBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBPaqueteSubproductoBuhobank nuevoPaqueteSubproducto) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_paquetes_subproducto] ";
		sql += "([id_paquete], [tipo], [codigo], [titulo], [descripcion], [url_legales], [imagen], [habilitado], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevoPaqueteSubproducto, 8);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBPaqueteSubproductoBuhobank paqueteSubproducto) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_paquetes_subproducto] SET ";
		sql += "id_paquete = ? ,";
		sql += "tipo = ? ,";
		sql += "codigo = ? ,";
		sql += "titulo = ? ,";
		sql += "descripcion = ? ,";
		sql += "url_legales = ? ,";
		sql += "imagen = ? ,";
		sql += "habilitado = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, paqueteSubproducto, 9);
		parametros[8] = paqueteSubproducto.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_paquetes_subproducto] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	private static BBPaqueteSubproductoBuhobank clonarSubproducto(Contexto contexto, Integer idSubproducto, Integer idPaquete) {

		String sql = "";
		sql += "INSERT [dbo].[bb_paquetes_subproducto] (id_paquete, tipo, codigo, titulo, descripcion, url_legales, imagen, habilitado, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_paquete, tipo, codigo, titulo, descripcion, url_legales, imagen, habilitado, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_paquetes_subproducto] WITH (NOLOCK) ";
		sql += "WHERE id = ? ;";
		sql += "SELECT SCOPE_IDENTITY() AS id";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete, idSubproducto);
		return map(datos, BBPaquetesSubproductoBuhobank.class, BBPaqueteSubproductoBuhobank.class).first();
	}

}
