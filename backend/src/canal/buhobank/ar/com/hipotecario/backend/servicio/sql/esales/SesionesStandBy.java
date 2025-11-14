package ar.com.hipotecario.backend.servicio.sql.esales;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy.SesionStandBy;

@SuppressWarnings("serial")
public class SesionesStandBy extends SqlObjetos<SesionStandBy> {

	public static String SESION_CREADA = "SESION_CREADA";
	public static String CONTROLAR = "CONTROLAR";
	public static String FLUJO_VU_OK = "FLUJO_VU_OK";
	public static String CONTROL_OK = "CONTROL_OK";
	public static String CONTROL_ERROR = "CONTROL_ERROR";
	public static String BORRAR_CONTROL = "BORRAR_CONTROL";

	/* ========== ATRIBUTOS ========== */
	public static class SesionStandBy extends SqlObjeto {

		public Integer id;
		public String usuario_admin;
		public String token_sesion;
		public String cuil;
		public String estado;
		public String score;
		public Fecha fecha_ultima_modificacion;
	}

	private static Object[] obtenerParametros(Contexto contexto, SesionStandBy standBy, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(standBy.usuario_admin) ? standBy.usuario_admin : null;
		parametros[1] = !Util.empty(standBy.token_sesion) ? standBy.token_sesion : null;
		parametros[2] = !Util.empty(standBy.cuil) ? standBy.cuil : null;
		parametros[3] = !Util.empty(standBy.estado) ? standBy.estado : null;
		parametros[4] = !Util.empty(standBy.score) ? standBy.score : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static SesionesStandBy get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[SesionesStandBy] WITH (NOLOCK) ";
		sql += "WHERE ? <= fecha_ultima_modificacion AND fecha_ultima_modificacion <= ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, fechaHasta);
		return map(datos, SesionesStandBy.class, SesionStandBy.class);
	}

	public static SesionStandBy sesionByCuil(Contexto contexto, String cuil) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[SesionesStandBy] WITH (NOLOCK) ";
		sql += "WHERE cuil = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuil);
		return map(datos, SesionesStandBy.class, SesionStandBy.class).first();
	}

	public static Boolean crear(Contexto contexto, String cuil) {

		SesionStandBy nuevoStandBy = new SesionStandBy();
		nuevoStandBy.cuil = cuil;
		nuevoStandBy.estado = SESION_CREADA;

		return post(contexto, nuevoStandBy);
	}

	public static Boolean post(Contexto contexto, SesionStandBy standBy) {

		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[SesionesStandBy] ";
		sql += "([usuario_admin], [token_sesion], [cuil], [estado], [score], [fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, GETDATE()) ";

		Object[] parametros = obtenerParametros(contexto, standBy, 5);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean put(Contexto contexto, SesionStandBy sesionStandBy) {

		String sql = "";
		sql += "UPDATE [esales].[dbo].[SesionesStandBy] SET ";
		sql += "[usuario_admin] = ? , ";
		sql += "[token_sesion] = ? , ";
		sql += "[cuil] = ? , ";
		sql += "[estado] = ? , ";
		sql += "[score] = ? , ";
		sql += "[fecha_ultima_modificacion] = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, sesionStandBy, 6);
		parametros[5] = sesionStandBy.id;

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) > 0;
	}

	public static SesionesStandBy sesionesByEstado(Contexto contexto, String estado, Fecha fechaDesde, Fecha fechaHasta) {

		if (Utils.isEmpty(estado)) {
			return get(contexto, fechaDesde, fechaHasta);
		}

		return getEstado(contexto, estado, fechaDesde, fechaHasta);
	}

	public static SesionesStandBy getEstado(Contexto contexto, String estado, Fecha fechaDesde, Fecha fechaHasta) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[SesionesStandBy] WITH (NOLOCK) ";
		sql += "WHERE ? <= fecha_ultima_modificacion AND fecha_ultima_modificacion <= ? ";
		sql += "AND estado = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, fechaHasta, estado);
		return map(datos, SesionesStandBy.class, SesionStandBy.class);
	}

	public static Boolean borrarSesion(Contexto contexto, String cuil) {

		String sql = "";
		sql += "DELETE [esales].[dbo].[SesionesStandBy] ";
		sql += "WHERE cuil = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, cuil) > 0;
	}
}
