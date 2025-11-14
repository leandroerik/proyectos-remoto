package ar.com.hipotecario.backend.servicio.sql.buhobank;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBBeneficiosBuhobank.BBBeneficioBuhobank;

@SuppressWarnings("serial")
public class BBBeneficiosBuhobank extends SqlObjetos<BBBeneficioBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBBeneficioBuhobank extends SqlObjeto {
		public Integer id;
		public Integer post_id;
		public String titulo;
		public String post_status;
		public String legales;
		public String tyc;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBBeneficioBuhobank beneficio, int cantidad) {
		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(beneficio.post_id) ? beneficio.post_id : "";
		parametros[1] = !Util.empty(beneficio.titulo) ? beneficio.titulo : "";
		parametros[2] = !Util.empty(beneficio.post_status) ? beneficio.post_status : "";
		parametros[3] = !Util.empty(beneficio.legales) ? beneficio.legales : "";
		parametros[4] = !Util.empty(beneficio.tyc) ? beneficio.tyc : "";

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBBeneficiosBuhobank get(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[beneficios] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBBeneficiosBuhobank.class, BBBeneficioBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBBeneficioBuhobank nuevasucursal) {
		String sql = "";
		sql += "INSERT INTO [dbo].[beneficios] ";
		sql += "([post_id], [titulo], [post_status], [legales], [tyc]) ";
		sql += "VALUES (?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, nuevasucursal, 5);
		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBBeneficioBuhobank sucursal) {
		String sql = "";
		sql += "UPDATE [dbo].[beneficios] SET ";
		sql += "post_id = ? , ";
		sql += "titulo = ? , ";
		sql += "post_status = ? , ";
		sql += "legales = ? , ";
		sql += "tyc = ? ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, sucursal, 6);
		parametros[5] = sucursal.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {
		String sql = "";
		sql += "DELETE [dbo].[beneficios] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
