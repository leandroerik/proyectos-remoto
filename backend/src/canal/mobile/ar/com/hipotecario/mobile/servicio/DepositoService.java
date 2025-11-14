package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class DepositoService {

	public static SqlResponseMB guardarTokenDeposito(ContextoMB contexto, String token, String cuil) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectCuilDeposito", "mobile");
		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[deposito_sesion] ([token],[cuil]) VALUES(?, ?)";
		sqlRequest.add(token);
		sqlRequest.add(cuil);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB selectCuilDeposito(ContextoMB contexto, String token) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectCuilDeposito", "mobile");
		sqlRequest.sql = "SELECT [cuil] FROM [Mobile].[dbo].[deposito_sesion] WHERE token = ?";
		sqlRequest.add(token);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB eliminarTokenDeposito(ContextoMB contexto, String token) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("EliminarTokenDeposito", "mobile");
		sqlRequest.sql = "DELETE FROM [Mobile].[dbo].[deposito_sesion] WHERE token = ?";
		sqlRequest.add(token);
		response = SqlMB.response(sqlRequest);
		return response;
	}

}
