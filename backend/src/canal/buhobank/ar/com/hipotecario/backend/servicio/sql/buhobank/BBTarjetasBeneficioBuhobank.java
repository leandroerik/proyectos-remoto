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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioBuhobank.BBTarjetaBeneficioBuhobank;

@SuppressWarnings("serial")
public class BBTarjetasBeneficioBuhobank extends SqlObjetos<BBTarjetaBeneficioBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaBeneficioBuhobank extends SqlObjeto {
		public Integer numero_paquete;
		public String descripcion;
		public Boolean virtual;

		public Integer id;
		public Integer id_tarjeta;
		public String desc_beneficio;
		public String desc_beneficio_html;
		public String icono_id;
		public String icono_desc;
		public String prioridad;
	}

	public static BBTarjetaBeneficioBuhobank buscarId(BBTarjetasBeneficioBuhobank tarjetasBeneficio, Integer id) {

		if (tarjetasBeneficio == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaBeneficioBuhobank tarjetaBeneficio : tarjetasBeneficio) {

			if (id.equals(tarjetaBeneficio.id)) {
				return tarjetaBeneficio;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaBeneficioBuhobank tarjetaBeneficio, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(tarjetaBeneficio.id_tarjeta) ? tarjetaBeneficio.id_tarjeta : null;
		parametros[1] = !Util.empty(tarjetaBeneficio.desc_beneficio) ? tarjetaBeneficio.desc_beneficio : null;
		parametros[2] = !Util.empty(tarjetaBeneficio.desc_beneficio_html) ? tarjetaBeneficio.desc_beneficio_html : null;
		parametros[3] = !Util.empty(tarjetaBeneficio.icono_id) ? tarjetaBeneficio.icono_id : null;
		parametros[4] = !Util.empty(tarjetaBeneficio.icono_desc) ? tarjetaBeneficio.icono_desc : null;
		parametros[5] = !Util.empty(tarjetaBeneficio.prioridad) ? tarjetaBeneficio.prioridad : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBTarjetasBeneficioBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.descripcion, t2.virtual, t3.* ";
		sql += "FROM bb_paquetes AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN bb_tarjetas AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "INNER JOIN bb_tarjetas_beneficio AS t3 WITH (NOLOCK) ";
		sql += "ON t2.id = t3.id_tarjeta ";
		sql += "ORDER BY t3.id_tarjeta, t3.prioridad ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBTarjetasBeneficioBuhobank.class, BBTarjetaBeneficioBuhobank.class);
	}

	public static BBTarjetasBeneficioBuhobank getIdTarjeta(Contexto contexto, Integer idTarjeta) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_tarjetas_beneficio] WITH (NOLOCK) ";
		sql += "WHERE id_tarjeta = ? ";
		sql += "ORDER BY prioridad ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idTarjeta);
		return map(datos, BBTarjetasBeneficioBuhobank.class, BBTarjetaBeneficioBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBTarjetaBeneficioBuhobank nuevaTarjetaBeneficio) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas_beneficio] ";
		sql += "([id_tarjeta], [desc_beneficio], [desc_beneficio_html], [icono_id], [icono_desc], [prioridad], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjetaBeneficio, 6);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaBeneficioBuhobank tarjetaBeneficio) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas_beneficio] SET ";
		sql += "id_tarjeta = ? ,";
		sql += "desc_beneficio = ? ,";
		sql += "desc_beneficio_html = ? ,";
		sql += "icono_id = ? ,";
		sql += "icono_desc = ? ,";
		sql += "prioridad = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, tarjetaBeneficio, 7);
		parametros[6] = tarjetaBeneficio.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_tarjetas_beneficio] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idTarjetaBase, Integer idTarjeta) {

		String sql = "";
		sql += "INSERT [dbo].[bb_tarjetas_beneficio] (id_tarjeta, desc_beneficio, desc_beneficio_html, icono_id, icono_desc, prioridad, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_tarjeta, desc_beneficio, desc_beneficio_html, icono_id, icono_desc, prioridad, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_tarjetas_beneficio] WITH (NOLOCK) ";
		sql += "WHERE id_tarjeta = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idTarjeta, idTarjetaBase) > 0;
	}

	public static BBTarjetasBeneficioBuhobank getAdicionalesIdTarjeta(Contexto contexto, Integer idTarjeta) {
		try {
			String sql = "";
			sql += "SELECT * ";
			sql += "FROM [dbo].[bb_tarjetas_beneficio_adicionales] WITH (NOLOCK) ";
			sql += "WHERE id_tarjeta = ? ";
			sql += "ORDER BY prioridad ASC ";

			Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idTarjeta);
			return map(datos, BBTarjetasBeneficioBuhobank.class, BBTarjetaBeneficioBuhobank.class);
		}catch(Exception e) {
			return null;
		}
	}

	public static Boolean clonarAdicionales(Contexto contexto, Integer idTarjetaBase, Integer idTarjeta) {
		try {
			String sql = "";
			sql += "INSERT [dbo].[bb_tarjetas_beneficio_adicionales] (id_tarjeta, desc_beneficio, desc_beneficio_html, icono_id, icono_desc, prioridad, fecha_ultima_modificacion) ";
			sql += "SELECT ? AS id_tarjeta, desc_beneficio, desc_beneficio_html, icono_id, icono_desc, prioridad, GETDATE() AS fecha_ultima_modificacion ";
			sql += "FROM [dbo].[bb_tarjetas_beneficio_adicionales] WITH (NOLOCK) ";
			sql += "WHERE id_tarjeta = ? ";

			return Sql.update(contexto, SqlBuhoBank.SQL, sql, idTarjeta, idTarjetaBase) > 0;
		}catch(Exception e) {
			return null;
		}
	}

}
