package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.ProvinciasWF.ProvinciaWF;

@SuppressWarnings("serial")
public class ProvinciasWF extends SqlObjetos<ProvinciaWF> {

	/* ========== ATRIBUTOS ========== */
	public static class ProvinciaWF extends SqlObjeto {
		public String PRV_Id;
		public String PRV_Descripcion;
		public String PRV_Estado;
		public Integer PRV_PAI_Id;
	}

	/* ========== SERVICIO ========== */
	public static ProvinciasWF get(Contexto contexto, String codigoPostal) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[PostalFrontEnd] p ";
		sql += "JOIN [CampaniasWF].[dbo].[ProvinciaFrontEnd] c ON c.PRV_ID = p.POS_PRV_Id ";
		sql += "WHERE POS_Estado = 'V' AND PRV_Estado = 'V' ";
		sql += "AND POS_Codigo = ? ";
		sql += "ORDER BY PRV_Descripcion";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, codigoPostal);
		return map(datos, ProvinciasWF.class, ProvinciaWF.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		ProvinciasWF datos = get(contexto, "1712");
		imprimirResultado(contexto, datos);
	}
}
