package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoProducto.TarjetaCreditoProducto;

@SuppressWarnings("serial")
public class TarjetasCreditoProducto extends SqlObjetos<TarjetaCreditoProducto> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoProducto extends SqlObjeto {
		public Integer TCPR_Id;
		public Integer TCPR_Marca;
		public Integer TCPR_Producto;
		public String TCPR_Descripcion;
		public String TCPR_Estado;
		public String TCPR_Letra;
	}

	/* ========== SERVICIO ========== */
	public static TarjetaCreditoProducto get(Contexto contexto, Integer marca, Integer producto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[TarjetaCreditoProducto] ";
		sql += "WHERE TCPR_Marca = ? ";
		sql += "AND TCPR_Producto = ? ";
		sql += "AND TCPR_Estado = 'V' ";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, marca, producto);
		return map(datos, TarjetasCreditoProducto.class, TarjetaCreditoProducto.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		TarjetaCreditoProducto datos = get(contexto, 2, 1);
		imprimirResultado(contexto, datos);
	}
}
