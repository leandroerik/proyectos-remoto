package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class RestAcumar {

	/* ========== SERVICIOS ========== */
	public static Boolean esAcumar(String idCobis) {
		Boolean esAcumar = false;
		try {
			String base = "esales";
			SqlRequest sqlRequest = Sql.request("SelectAcumar", "esales");
			sqlRequest.sql += "SELECT * ";
			sqlRequest.sql += "FROM [" + base + "].[dbo].[MC_Interesado] ";
			sqlRequest.sql += "WHERE CodTipoInteresado = 12 ";
			sqlRequest.sql += "AND CodClienteCobis = ?";
			sqlRequest.add(idCobis);

			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				esAcumar = true;
			}
		} catch (Exception e) {
		}
		return esAcumar;
	}

	public static String estado(String idCobis) {
		String estado = "";
		try {
			String base = "esales";
			SqlRequest sqlRequest = Sql.request("SelectEstadoAcumar", "esales");
			sqlRequest.sql += "SELECT a.*";
			sqlRequest.sql += "FROM [" + base + "].[dbo].[MC_Solicitud] a ";
			sqlRequest.sql += "JOIN [" + base + "].[dbo].[MC_Interesado] b ON a.CodInteresado = b.CodInteresado ";
			sqlRequest.sql += "WHERE b.CodTipoInteresado = 12";
			sqlRequest.sql += "AND b.CodClienteCobis = ?";
			sqlRequest.add(idCobis);

			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				estado = sqlResponse.registros.get(0).string("CodEstado");
			}
		} catch (Exception e) {
		}
		return estado;
	}
}
