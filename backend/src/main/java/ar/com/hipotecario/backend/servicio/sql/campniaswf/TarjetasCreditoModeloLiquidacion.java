package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoModeloLiquidacion.TarjetaCreditoModeloLiquidacion;

@SuppressWarnings("serial")
public class TarjetasCreditoModeloLiquidacion extends SqlObjetos<TarjetaCreditoModeloLiquidacion> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoModeloLiquidacion extends SqlObjeto {
		public Integer TCML_Id;
		public Integer TCML_Marca;
		public Integer TCML_Producto;
		public Integer TCML_ModLiq;
		public String TCML_Descripcion;
		public String TCML_Estado;
		public BigDecimal TCML_CFT_BHSeguro;
		public BigDecimal TCML_CFT_Caruso;
		public BigDecimal TCML_CFT_Mafre;
	}

	/* ========== SERVICIO ========== */
	public static TarjetaCreditoModeloLiquidacion get(Contexto contexto, Integer marca, Integer producto, Integer modLiq) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [CampaniasWF].[dbo].[TarjetaCreditoModeloLiquidacion] ";
		sql += "WHERE TCML_Marca = ? ";
		sql += "AND TCML_Producto = ? ";
		sql += "AND TCML_ModLiq = ? ";
		sql += "AND TCML_Estado = 'V' ";
		Objeto datos = Sql.select(contexto, "campaniaswf", sql, marca, producto, modLiq);
		return map(datos, TarjetasCreditoModeloLiquidacion.class, TarjetaCreditoModeloLiquidacion.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		TarjetaCreditoModeloLiquidacion datos = get(contexto, 2, 1, 3);
		imprimirResultado(contexto, datos);
	}
}
