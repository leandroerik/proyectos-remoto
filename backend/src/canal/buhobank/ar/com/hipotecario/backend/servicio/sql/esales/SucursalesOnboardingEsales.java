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
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales.SucursalOnboardingEsales;

public class SucursalesOnboardingEsales extends SqlObjetos<SucursalOnboardingEsales> {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public static class SucursalOnboardingEsales extends SqlObjeto {

		public Integer id;
		public String nombre;
		public String url_qr;
		public String flujo;
		public Boolean habilitado;
		public Fecha fecha_creacion;
		public Fecha fecha_ultima_modificacion;
	}

	public SucursalOnboardingEsales buscarSucursalById(Integer id) {

		if (id == null)
			return null;

		for (SucursalOnboardingEsales sucursal : this) {
			if (sucursal.id.equals(id)) {
				return sucursal;
			}
		}

		return null;
	}

	public SucursalOnboardingEsales buscarSucursalByQr(String urlQr) {

		if (Utils.isEmpty(urlQr)) {
			return null;
		}

		for (SucursalOnboardingEsales sucursal : this) {
			if (sucursal.url_qr.equals(urlQr)) {
				return sucursal;
			}
		}

		return null;
	}

	private static Object[] obtenerParametros(Contexto contexto, SucursalOnboardingEsales sucursal, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(sucursal.nombre) ? sucursal.nombre : null;
		parametros[1] = !Util.empty(sucursal.url_qr) ? sucursal.url_qr : null;
		parametros[2] = !Util.empty(sucursal.flujo) ? sucursal.flujo : null;
		parametros[3] = !Util.empty(sucursal.habilitado) ? sucursal.habilitado : null;

		return parametros;
	}

	public static SucursalesOnboardingEsales get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[sucursales_onboarding] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, SucursalesOnboardingEsales.class, SucursalOnboardingEsales.class);
	}

	public static SucursalOnboardingEsales getByQr(Contexto contexto, String urlQr) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[sucursales_onboarding] WITH (NOLOCK) ";
		sql += "WHERE url_qr = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, Utils.isEmpty(urlQr) ? "default" : urlQr);
		return map(datos, SucursalesOnboardingEsales.class, SucursalOnboardingEsales.class).first();
	}

	public static Boolean put(Contexto contexto, SucursalOnboardingEsales sucursal) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[sucursales_onboarding] SET ";
		sql += "[nombre] = ?, ";
		sql += "[url_qr] = ?, ";
		sql += "[flujo] = ?, ";
		sql += "[habilitado] = ?, ";
		sql += "[fecha_ultima_modificacion] = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, sucursal, 5);
		parametros[4] = sucursal.id;

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) > 0;
	}

	public static Boolean post(Contexto contexto, SucursalOnboardingEsales sucursal) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[sucursales_onboarding] ";
		sql += "([nombre], [url_qr], [flujo], [habilitado], [fecha_creacion], [fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";

		Object[] parametros = obtenerParametros(contexto, sucursal, 4);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) > 0;
	}

}
