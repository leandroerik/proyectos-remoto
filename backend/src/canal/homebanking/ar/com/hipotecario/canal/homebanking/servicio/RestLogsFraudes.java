package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.util.Date;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class RestLogsFraudes {

	public static class Logs extends Objeto {
		private static final long serialVersionUID = 1L;
		public Date momento;
		public String cobis;
		public String idProceso;
		public String ip;
		public String canal;
		public String codigoError;
		public String descripcionError;
	}

	public static class Ode extends Logs {
		private static final long serialVersionUID = 1L;
		public BigDecimal importe;
		public String numeroDocumento;
		public String referencia;
		public String tipoCuenta;
		public String cuentaPBF;
	}

	public static SqlResponse insertLogODE(Ode registroOde, ApiResponse response, ApiRequest request) {
		setInfoLogs(registroOde, response, request);

		registroOde.cuentaPBF = request.body().string("cuentaPBF");
		registroOde.tipoCuenta = request.body().string("tipoCuenta");

		SqlRequest sqlRequest = Sql.request("InsertAuditorOde", "hbs");
		String query = "INSERT INTO [hbs].[dbo].[auditor_ode] ";
		query += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[importe],[numeroDocumento],[referencia],[tipoCuenta],[cuentaPBF]) ";
		query += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		sqlRequest.sql = query;

		sqlRequest.add(registroOde.momento);
		sqlRequest.add(registroOde.cobis != null ? registroOde.cobis : "");
		sqlRequest.add(registroOde.idProceso != null ? registroOde.idProceso : "");
		sqlRequest.add(registroOde.ip != null ? registroOde.ip : "");
		sqlRequest.add(registroOde.canal != null ? registroOde.canal : "");
		sqlRequest.add(registroOde.codigoError != null ? registroOde.codigoError : "");
		sqlRequest.add(registroOde.descripcionError != null ? registroOde.descripcionError : "");
		sqlRequest.add(registroOde.importe);
		sqlRequest.add(registroOde.numeroDocumento != null ? registroOde.numeroDocumento : "");
		sqlRequest.add(registroOde.referencia != null ? registroOde.referencia : "");
		sqlRequest.add(registroOde.tipoCuenta != null ? registroOde.tipoCuenta : "");
		sqlRequest.add(registroOde.cuentaPBF != null ? registroOde.cuentaPBF : "");

		return Sql.response(sqlRequest);
	};

	private static void setInfoLogs(Logs registroLogs, ApiResponse response, ApiRequest request) {
		registroLogs.momento = new Date();
		registroLogs.canal = "HB";
		registroLogs.codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

		if (response != null && !registroLogs.codigoError.equals("0")) {
			String descripcion = response.string("codigo") + ".";
			descripcion += response.string("mensajeAlDesarrollador") + ".";
			descripcion += response.string("detalle") + ".";

			registroLogs.descripcionError = descripcion;
		}

		registroLogs.cobis = request.idCobis();
		registroLogs.idProceso = request.idProceso();
		registroLogs.ip = request.ip();
	}

}
