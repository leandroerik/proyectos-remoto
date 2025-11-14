package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SucursalesWF.SucursalWF;

@SuppressWarnings("serial")
public class SucursalesWF extends SqlObjetos<SucursalWF> {

	/* ========== ATRIBUTOS ========== */
	public static class SucursalWF extends SqlObjeto {
		public String codigoPostal;
		public String codigoSucursal;
	}

	/* ========== SERVICIO ========== */
	public static SucursalWF get(Contexto contexto, String codigoPostal) {
		String sql = "";
		sql += "SELECT TOP 1 POSU_IdPostal AS codigoPostal, POSU_IdSucursal AS codigoSucursal, ABS(POSU_IdPostal - ?) AS distancia ";
		sql += "FROM [CampaniasWF].[dbo].[PostalSucursal] ";
		sql += "ORDER BY distancia";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, codigoPostal);
		return map(datos, SucursalesWF.class, SucursalWF.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SucursalWF datos = get(contexto, "1712");
		imprimirResultado(contexto, datos);
	}
}
