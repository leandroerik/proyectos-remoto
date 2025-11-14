package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupo.TarjetaCreditoGrupo;

@SuppressWarnings("serial")
public class TarjetasCreditoGrupo extends SqlObjetos<TarjetaCreditoGrupo> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoGrupo extends SqlObjeto {
		public Integer TCGR_Id;
		public Integer TCGR_Marca;
		public Integer TCGR_Producto;
		public Integer TCGR_Grupo;
		public String TCGR_Descripcion;
		public String TCGR_Estado;
	}

	/* ========== SERVICIO ========== */
	public static TarjetaCreditoGrupo get(Contexto contexto, Integer marca, Integer producto, Integer grupo) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[TarjetaCreditoGrupo] ";
		sql += "WHERE TCGR_Marca = ? ";
		sql += "AND TCGR_Producto = ? ";
		sql += "AND TCGR_Grupo = ? ";
		sql += "AND TCGR_Estado = 'V' ";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, marca, producto, grupo);
		return map(datos, TarjetasCreditoGrupo.class, TarjetaCreditoGrupo.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		TarjetaCreditoGrupo datos = get(contexto, 2, 1, 2);
		imprimirResultado(contexto, datos);
	}
}
