package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlConsentimiento {

	public SqlResponse consultarPorState(ContextoHB contexto, String state) {
		try {
			SqlRequest sqlRequest = Sql.request("consultarDatosConsentimiento", "hbs");
			sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[MODO_consentimiento] WITH (NOLOCK) WHERE [state] = ? order by id desc";
			sqlRequest.add(state);

			SqlResponse response = Sql.response(sqlRequest);
			return response;
		} catch (Exception error) {
			error.printStackTrace();
			return null;
		}
	}

}
