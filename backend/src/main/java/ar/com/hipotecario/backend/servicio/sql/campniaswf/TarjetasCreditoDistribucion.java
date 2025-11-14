package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoDistribucion.TarjetaCreditoDistribucion;

@SuppressWarnings("serial")
public class TarjetasCreditoDistribucion extends SqlObjetos<TarjetaCreditoDistribucion> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoDistribucion extends SqlObjeto {
		public Integer TCDI_Id;
		public Integer TCDI_Marca;
		public Integer TCDI_Producto;
		public Integer TCDI_Distribucion;
		public String TCDI_Descripcion;
		public String TCDI_Estado;
	}

	/* ========== SERVICIO ========== */
	public static TarjetaCreditoDistribucion get(Contexto contexto, Integer marca, Integer producto, String distribucionDesc) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[TarjetaCreditoDistribucion] ";
		sql += "WHERE TCDI_Marca = ? ";
		sql += "AND TCDI_Producto = ? ";
		sql += "AND TCDI_Descripcion LIKE '[[]' + ? + ']%'";
		sql += "AND TCDI_Estado = 'V' ";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, marca, producto, distribucionDesc);
		return map(datos, TarjetasCreditoDistribucion.class, TarjetaCreditoDistribucion.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		TarjetaCreditoDistribucion datos = get(contexto, 2, 541, "k1");
		imprimirResultado(contexto, datos);
	}
}
