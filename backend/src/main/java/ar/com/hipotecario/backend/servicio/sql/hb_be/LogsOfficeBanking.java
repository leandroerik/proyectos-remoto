package ar.com.hipotecario.backend.servicio.sql.hb_be;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsOfficeBanking.LogOfficeBanking;

@SuppressWarnings("serial")
public class LogsOfficeBanking extends SqlObjetos<LogOfficeBanking> {

	/* ========== ATRIBUTOS ========== */
	public static class LogOfficeBanking extends SqlObjeto {
		public String id;
		public Fecha momento;
		public String empresa;
		public String usuario;
		public String endpoint;
		public String evento;
		public String datos;
		public String error;
		public Integer idProceso;
		public String ip;
	}

	/* ========== SERVICIO ========== */
	public static LogsOfficeBanking selectPorFecha(Contexto contexto, Fecha fecha) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [HB_BE].[dbo].[log] ";
		sql += "WHERE momento BETWEEN ? AND ?";
		Objeto datos = Sql.select(contexto, "hb_be", sql, fecha, fecha.sumarDias(1));
		return map(datos, LogsOfficeBanking.class, LogOfficeBanking.class);
	}
	public static LogsOfficeBanking selectPorFecha(Contexto contexto, String fecha1,String fecha2, String cuitEmpresa ) {
		String datosRecibidos = fecha1 +" " + fecha2+" "+cuitEmpresa;
		post(contexto,"0","0","LogsOfficeBanking","reporte",datosRecibidos,"","");
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [HB_BE].[dbo].[log] WHERE ";
		sql += "momento BETWEEN ? AND ? AND empresa != 0 ";
		Objeto datos = new Objeto();
		if(cuitEmpresa!=null){
			sql += "AND empresa= ? ";
			datos = Sql.select(contexto, "hb_be", sql, fecha1, fecha2,cuitEmpresa);
		}else{
			datos = Sql.select(contexto, "hb_be", sql, fecha1, fecha2);
		}


		return map(datos, LogsOfficeBanking.class, LogOfficeBanking.class);
	}
	public static LogsOfficeBanking selectPorUsuarioOrEmpresa(Contexto contexto, String usuario, String empresa) {
		String sql = "SELECT * FROM [hb_be].[dbo].[log] ";
		sql += "WHERE [usuario]= ? OR [empresa]= ? ";
		Objeto datos = Sql.select(contexto, "hb_be", sql, usuario, empresa);
		return map(datos, LogsOfficeBanking.class, LogOfficeBanking.class);
	}

	public static Boolean post(Contexto contexto, String empresa, String usuario, String endpoint, String evento, String datos, String error, String idProceso) {
		String sql = "";
		sql += "INSERT INTO [hb_be].[dbo].[log] ";
		sql += "([momento],[empresa],[usuario],[endpoint],[evento],[datos],[error],[idProceso],[ip]) ";
		sql += "VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?) ";
		Object[] parametros = new Object[8];
		parametros[0] = empresa;
		parametros[1] = usuario;
		parametros[2] = endpoint;
		parametros[3] = evento;
		parametros[4] = datos;
		parametros[5] = error;
		parametros[6] = idProceso;
		parametros[7] = contexto.ip();
		return Sql.update(contexto, "hb_be", sql, parametros) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("OB", "desarrollo");
		String empresa = "301234567891";
		String usuario = "208765432101";
		String endpoint = "/test";
		String evento = "test";
		String datos = "{}";
		String error = null;
		String idProceso = "1";
		Boolean exito = post(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
		imprimirResultado(contexto, exito);
	}
}
