package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupoAfinidad.TarjetaCreditoGrupoAfinidad;

@SuppressWarnings("serial")
public class TarjetasCreditoGrupoAfinidad extends SqlObjetos<TarjetaCreditoGrupoAfinidad> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoGrupoAfinidad extends SqlObjeto {
		public Integer TCGA_Id;
		public Integer TCGA_Marca;
		public Integer TCGA_Producto;
		public Integer TCGA_Afinidad;
		public String TCGA_Descripcion;
	}

	/* ========== SERVICIO ========== */
	public static TarjetaCreditoGrupoAfinidad get(Contexto contexto, Integer marca, Integer producto, String afinidadDesc) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[TarjetaCreditoGrupoAfinidad] ";
		sql += "WHERE TCGA_Marca = ? ";
		sql += "AND TCGA_Producto = ? ";
		sql += "AND TCGA_Descripcion LIKE '%' + ? + '%'";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, marca, producto, afinidadDesc);
		return map(datos, TarjetasCreditoGrupoAfinidad.class, TarjetaCreditoGrupoAfinidad.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		TarjetaCreditoGrupoAfinidad datos = get(contexto, 2, 1, "5663");
		imprimirResultado(contexto, datos);
	}
}
