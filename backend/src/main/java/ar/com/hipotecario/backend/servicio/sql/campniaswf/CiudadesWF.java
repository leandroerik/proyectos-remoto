package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;

@SuppressWarnings("serial")
public class CiudadesWF extends SqlObjetos<CiudadWF> {

	/* ========== ATRIBUTOS ========== */
	public static class CiudadWF extends SqlObjeto {
		public String CIU_Id;
		public String CIU_Descripcion;
		public String CIU_Estado;
		public String CIU_PRV_Id;
		public String CIU_CodRemesas;
		public String CIU_PAI_Id;
		public String CIU_Distrito;
		public String POS_Codigo;
		public String PRV_Descripcion;
	}

	/* ========== SERVICIO ========== */
	public static CiudadesWF get(Contexto contexto, String codigoPostal) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[PostalFrontEnd] p ";
		sql += "JOIN [CampaniasWF].[dbo].[CiudadFrontEnd] c ON c.CIU_Id = p.POS_CIU_Id ";
		sql += "JOIN [CampaniasWF].[dbo].[ProvinciaFrontEnd] pf ON pf.PRV_Id = c.CIU_PRV_Id ";
		sql += "WHERE POS_Estado = 'V' AND CIU_Estado = 'V' AND PRV_Estado = 'V' ";
		sql += "AND POS_Codigo = ? ";
		sql += "ORDER BY CIU_Descripcion";

		Objeto datos = Sql.select(contexto, "campaniaswf", sql, codigoPostal);
		return map(datos, CiudadesWF.class, CiudadWF.class);
	}

	public static CiudadWF getPorId(Contexto contexto, String id) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[CiudadFrontEnd] ";
		sql += "WHERE CIU_Estado = 'V' ";
		sql += "AND CIU_Id = ? ";
		sql += "ORDER BY CIU_Descripcion";

		Objeto datos = Sql.select(contexto, "campaniaswf", sql, id);
		SqlException.throwIf("CIUDAD_NO_ENCONTRADA", datos.isEmpty());
		return map(datos, CiudadesWF.class, CiudadWF.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String test = "get";

		if ("get".equals(test)) {
			CiudadesWF datos = get(contexto, "1712");
			imprimirResultado(contexto, datos);

			CiudadesWF datos2 = get(contexto, "1875");
			imprimirResultado(contexto, datos2);

			CiudadesWF datos3 = get(contexto, "5000");
			imprimirResultado(contexto, datos3);

			CiudadesWF datos4 = get(contexto, "1415");
			imprimirResultado(contexto, datos4);

			CiudadesWF datos5 = get(contexto, "1416");
			imprimirResultado(contexto, datos5);
		}

		if ("getId".equals(test)) {
			CiudadWF dato = getPorId(contexto, "195");
			imprimirResultado(contexto, dato);
		}
	}
}
