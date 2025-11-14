package ar.com.hipotecario.backend.servicio.sql.hb_be;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.hb_be.NovedadesATEHistorico.NovedadATEHistorico;

@SuppressWarnings("serial")
public class NovedadesATEHistorico extends SqlObjetos<NovedadATEHistorico> {

	/* ========== ATRIBUTOS ========== */
	public static class NovedadATEHistorico extends SqlObjeto {
		public Fecha HorSnd;
		public String CmlCanal;
		public Fecha CmlFecSol;
		public String NroCml;
		public String Estado;
		public String CodRet;
		public String CodError;
		public String DesError;
		public String Descripcion;
		public Integer Id;
		public Fecha FecSolicitud;
		public String IdLote;
		public String IdOpe;
		public String IsConsolidado;
		public String CbuOrigen;
		public String DescOrigen;
		public String CuiOrigen;
		public String CtaOrigen;
		public String TipoCtaOrigen;
		public String CbuDest;
		public String DescDestino;
		public String CuiDest;
		public String CtaDestino;
		public String TipoCtaDestino;
		public String CamaDest;
		public String Moneda;
		public BigDecimal Monto;
		public String TipoOp;
		public String FecAplic;
		public Integer LineaLote;
		public Integer TotalLote;
		public String Concepto;
		public String Observaciones;
		public String CbuPozo;
		public String TipCuePozo;
		public String CtaPozo;
		public String Vep;
		public String CanalOrigen;
		public Integer Id_Obe_Operador;
		public Fecha Dt_Obe_Operador;
		public Integer Id_Obe_Supervisor;
		public Fecha Dt_Obe_Supervisor;
		public Integer RefClienteOb_Operacion;
		public String Ref_ClienteOb_Carga;
		public String Id_Cobis_Firmante1;
		public String Id_Cobis_Firmante2;
		public String Id_Cobis_Firmante3;
		public Fecha Dt_Firmante1;
		public Fecha Dt_Firmante2;
		public Fecha Dt_Firmante3;
		public String Tipo_Ident_Benf;
		public Fecha fecha_proceso;
		public Long num_intercambio;
		public Integer RowNum;
	}

	/* ========== SERVICIO ========== */
	// NOTA: esta consulta tarda un rato
	public static NovedadesATEHistorico get(Contexto contexto, String canal) {
		String sql = "";
		sql += " SELECT h.*, ROW_NUMBER() OVER (ORDER BY h.FecAplic) AS RowNum";
		sql += " FROM [HB_BE].[dbo].[st_ate_NovedadesPeticionesHist_HB_BE] h";
		sql += " INNER JOIN (";
		sql += " 	SELECT CmlCanal, CtaOrigen, IdOpe, MAX(num_intercambio) AS num_intercambio, MAX(HorSnd) AS HorSnd";
		sql += " 	FROM [HB_BE].[dbo].[st_ate_NovedadesPeticionesHist_HB_BE]";
		sql += " 	GROUP BY CmlCanal, CtaOrigen, IdOpe";
		sql += " ) a";
		sql += " ON a.CmlCanal = h.CmlCanal";
		sql += " AND a.CtaOrigen = h.CtaOrigen";
		sql += " AND a.IdOpe = h.IdOpe";
		sql += " AND h.num_intercambio = a.num_intercambio";
		sql += " AND h.HorSnd = a.HorSnd";
		sql += " WHERE h.cmlCanal = ?";

		Objeto datos = Sql.select(contexto, "hb_be", sql, canal);
		return map(datos, NovedadesATEHistorico.class, NovedadATEHistorico.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		NovedadesATEHistorico datos = get(contexto, "OB");
		imprimirResultado(contexto, datos);
	}
}
