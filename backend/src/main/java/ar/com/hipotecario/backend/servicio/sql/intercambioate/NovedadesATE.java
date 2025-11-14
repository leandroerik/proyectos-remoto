package ar.com.hipotecario.backend.servicio.sql.intercambioate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.NovedadesATE.NovedadATE;

/////////////// ESTADOS	//////////////////////////////////////////////
//	PENDIENTE 	 	10 - enviada al banco de débito.
//	FALTA FONDOS 	20 - demorada por el banco de débito.                                 (a)
//	PENDIENTE 	 	30 - reverso del débito por exceder el riesgo red.                             (b)
//	ACEPTADA  		40 - valor al cobro (pendiente de tratar. crédito).                             (c)
//	PENDIENTE 		50 - enviada al banco de crédito.
//	ACEPTADA  		60 - ejecutada.
//	RECHAZADA 		70 - rechazo del banco de débito.
//	RECHAZADA 		80 - rechazo del banco de crédito.                                                         
//	NADA 15 – 		TEF CCI pendientes de habilitación de cuenta de crédito         (d)
//	RECAHZADA 		90 – rechazo de TEF CCI,  por inhabilitación de cuenta de crédito   (d)

@SuppressWarnings("serial")
public class NovedadesATE extends SqlObjetos<NovedadATE> {

	/* ========== ATRIBUTOS ========== */
	public static class NovedadATE extends SqlObjeto {
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
		public Integer RowNum;
	}

	/* ========== SERVICIO ========== */

	public static NovedadesATE get(Contexto contexto, String canal, int inicio, int fin, String estadosFinales, String estadoFaltaDeFondos) {

		LocalDate fechaDesde = new java.sql.Date(new Date().getTime()).toLocalDate();
		LocalDate fechaHasta = fechaDesde.plusDays(1);
		
		inicio = 0;

		String sql = "SELECT * ";
		sql += "FROM (SELECT ROW_NUMBER() OVER (ORDER BY FecAplic) AS RowNum , n.* FROM [IntercambioATE].[dbo].[VW_NovedadesPeticiones] n where n.cmlCanal = ?) as nov ";
		sql += "WHERE nov.RowNum BETWEEN ? AND ? ";
		sql += " AND FecSolicitud BETWEEN ? AND ? ";

		if (estadosFinales != null) {
			sql += "AND ((estado in (" + estadosFinales + ")) ";
			// SETBHIN no se usa porque se lee con el estado 70 de rechazo
			// sql += "OR (estado in (" + estadosFinales + ") and coderror = 'SETBHIN') ";
			sql += "OR (estado = '" + estadoFaltaDeFondos + "' and coderror = 'SETBHSF'))";
		}
		Objeto datos = Sql.select(contexto, "intercambioate", sql, canal, inicio, fin, fechaDesde, fechaHasta);
		return map(datos, NovedadesATE.class, NovedadATE.class);
	}

	/* ========== TEST ========== */
//	public static void main(String[] args) {
//		Contexto contexto = contexto("HB", "homologacion");
//
//		ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
//
//		String strEstadosFinales = CronOBLeeNovedadesInterbanking.getEstadosFinalesOB(contexto);
//		String strEstadoFaltaDeFondos = servicioParametros.split("transferencia.estados.faltaDeFondos").get().get(0);
//
//		NovedadesATE datos = get(contexto, PeticionesOB.CANAL, 0, 100, strEstadosFinales, strEstadoFaltaDeFondos);
//
//		imprimirResultado(contexto, datos);
//	}

}
