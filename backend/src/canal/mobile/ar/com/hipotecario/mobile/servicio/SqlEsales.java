package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlEsales {

	private static final String CONSULTAR_ONBOARDING_POR_CUIL = "SELECT * FROM [esales].[dbo].[Sesion] WHERE [cuil] = ? AND [estado] = 'FINALIZAR_OK'";

	public static SqlResponseMB esOnboarding(String cuil) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarEsOnboarding", "esales");
		sqlRequest.sql = CONSULTAR_ONBOARDING_POR_CUIL;
		sqlRequest.add(cuil);
		return SqlMB.response(sqlRequest);
	}
}
