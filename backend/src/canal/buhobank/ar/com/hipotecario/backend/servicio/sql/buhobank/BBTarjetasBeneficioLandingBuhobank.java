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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioLandingBuhobank.BBTarjetaBeneficioLandingBuhobank;

@SuppressWarnings("serial")
public class BBTarjetasBeneficioLandingBuhobank extends SqlObjetos<BBTarjetaBeneficioLandingBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaBeneficioLandingBuhobank extends SqlObjeto {
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

	public static BBTarjetaBeneficioLandingBuhobank buscarId(BBTarjetasBeneficioLandingBuhobank tarjetasBeneficio, Integer id) {

		if (tarjetasBeneficio == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaBeneficioLandingBuhobank tarjetaBeneficio : tarjetasBeneficio) {

			if (id.equals(tarjetaBeneficio.id)) {
				return tarjetaBeneficio;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaBeneficioLandingBuhobank tarjetaBeneficio, int cantidad) {

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
	public static BBTarjetasBeneficioLandingBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.descripcion, t2.virtual, t3.* ";
		sql += "FROM bb_paquetes AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN bb_tarjetas_landing AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "INNER JOIN bb_tarjetas_beneficio_landing AS t3 WITH (NOLOCK) ";
		sql += "ON t2.id = t3.id_tarjeta ";
		sql += "ORDER BY t3.id_tarjeta, t3.prioridad ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBTarjetasBeneficioLandingBuhobank.class, BBTarjetaBeneficioLandingBuhobank.class);
	}

	public static BBTarjetasBeneficioLandingBuhobank getIdTarjeta(Contexto contexto, Integer idTarjeta) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_tarjetas_beneficio_landing] WITH (NOLOCK) ";
		sql += "WHERE id_tarjeta = ? ";
		sql += "ORDER BY prioridad ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idTarjeta);
		return map(datos, BBTarjetasBeneficioLandingBuhobank.class, BBTarjetaBeneficioLandingBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBTarjetaBeneficioLandingBuhobank nuevaTarjetaBeneficio) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas_beneficio_landing] ";
		sql += "([id_tarjeta], [desc_beneficio], [desc_beneficio_html], [icono_id], [icono_desc], [prioridad], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjetaBeneficio, 6);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaBeneficioLandingBuhobank tarjetaBeneficio) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas_beneficio_landing] SET ";
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
		sql += "DELETE [dbo].[bb_tarjetas_beneficio_landing] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
