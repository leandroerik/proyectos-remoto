package ar.com.hipotecario.backend.servicio.sql.buhobank;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBSucursalesBuhobank.BBSucursalBuhobank;

@SuppressWarnings("serial")
public class BBSucursalesBuhobank extends SqlObjetos<BBSucursalBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBSucursalBuhobank extends SqlObjeto {
		public Integer id;
		public Integer post_id;
		public String titulo;
		public String post_status;
		public BigDecimal lat;
		public BigDecimal lng;
		public String direccion;
		public String localidad;
		public String provincia;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBSucursalBuhobank sucursal, int cantidad) {
		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(sucursal.post_id) ? sucursal.post_id : "";
		parametros[1] = !Util.empty(sucursal.titulo) ? sucursal.titulo : "";
		parametros[2] = !Util.empty(sucursal.post_status) ? sucursal.post_status : "";
		parametros[3] = !Util.empty(sucursal.lat) ? sucursal.lat : "";
		parametros[4] = !Util.empty(sucursal.lng) ? sucursal.lng : "";
		parametros[5] = !Util.empty(sucursal.direccion) ? sucursal.direccion : "";
		parametros[6] = !Util.empty(sucursal.localidad) ? sucursal.localidad : "";
		parametros[7] = !Util.empty(sucursal.provincia) ? sucursal.provincia : "";

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBSucursalesBuhobank get(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[sucursales] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBSucursalesBuhobank.class, BBSucursalBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBSucursalBuhobank nuevasucursal) {
		String sql = "";
		sql += "INSERT INTO [dbo].[sucursales] ";
		sql += "([post_id], [titulo], [post_status], [lat], [lng], [direccion], [localidad], [provincia]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, nuevasucursal, 8);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBSucursalBuhobank sucursal) {
		String sql = "";
		sql += "UPDATE [dbo].[sucursales] SET ";
		sql += "post_id = ? , ";
		sql += "titulo = ? , ";
		sql += "post_status = ? , ";
		sql += "lat = ? , ";
		sql += "lng = ? , ";
		sql += "direccion = ? , ";
		sql += "localidad = ? , ";
		sql += "provincia = ? ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, sucursal, 9);
		parametros[8] = sucursal.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {
		String sql = "";
		sql += "DELETE [dbo].[sucursales] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
