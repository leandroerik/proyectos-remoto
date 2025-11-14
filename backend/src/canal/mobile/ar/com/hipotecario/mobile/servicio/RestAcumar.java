package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class RestAcumar {

	/* ========== SERVICIOS ========== */
	public static Boolean esAcumar(String idCobis) {
		Boolean esAcumar = false;
		try {
			String base = ConfigMB.string("sql_esales_base");
			SqlRequestMB sqlRequest = SqlMB.request("SelectAcumar", "esales");
			sqlRequest.sql += "SELECT * ";
			sqlRequest.sql += "FROM [" + base + "].[dbo].[MC_Interesado] ";
			sqlRequest.sql += "WHERE CodTipoInteresado = 12 ";
			sqlRequest.sql += "AND CodClienteCobis = ?";
			sqlRequest.add(idCobis);

			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
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
			String base = ConfigMB.string("sql_esales_base");
			SqlRequestMB sqlRequest = SqlMB.request("SelectEstadoAcumar", "esales");
			sqlRequest.sql += "SELECT a.*";
			sqlRequest.sql += "FROM [" + base + "].[dbo].[MC_Solicitud] a ";
			sqlRequest.sql += "JOIN [" + base + "].[dbo].[MC_Interesado] b ON a.CodInteresado = b.CodInteresado ";
			sqlRequest.sql += "WHERE b.CodTipoInteresado = 12";
			sqlRequest.sql += "AND b.CodClienteCobis = ?";
			sqlRequest.add(idCobis);

			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				estado = sqlResponse.registros.get(0).string("CodEstado");
			}
		} catch (Exception e) {
		}
		return estado;
	}

	public static void actualizarEstado(String idCobis, String estado) {
		try {
			String base = ConfigMB.string("sql_esales_base");
			SqlRequestMB sqlRequest = SqlMB.request("SelectEstadoAcumar", "esales");
			sqlRequest.sql += "UPDATE [" + base + "].[dbo].[MC_Solicitud] ";
			sqlRequest.sql += "SET CodEstado = ? ";
			sqlRequest.sql += "WHERE CodTipoSolicitud = 3 ";
			sqlRequest.sql += "AND CodInteresado IN ( ";
			sqlRequest.sql += "	SELECT CodInteresado ";
			sqlRequest.sql += "	FROM [" + base + "].[dbo].[MC_Interesado] ";
			sqlRequest.sql += "	WHERE CodTipoInteresado = 12 ";
			sqlRequest.sql += "	AND CodClienteCobis = ? ";
			sqlRequest.sql += ")";
			sqlRequest.add(estado);
			sqlRequest.add(idCobis);

			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
	}
}
