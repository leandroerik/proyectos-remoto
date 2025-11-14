package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlBiometriaService {

	public SqlResponse selectAccesos(ContextoHB contexto, String dispositivo) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectAccesoBiometria", "homebanking");
		sqlRequest.sql = "SELECT [id_cobis],[fecha_disp_registrado],[disp_registrado],[fecha_biometria_activa],[biometria_activa],[id_dispositivo]," + "[fecha_buhoFacil_activo], [buhoFacil_activo], [access_token], [refresh_token], [fecha_token]" + " FROM [Homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		if (dispositivo != null) {
			sqlRequest.sql += " and [id_dispositivo] = ?";
			sqlRequest.parametros.add(dispositivo);
		} else {
			sqlRequest.sql += " order by [fecha_disp_registrado] desc";
		}
		response = Sql.response(sqlRequest);
		return response;
	}

	public SqlResponse selectCountAccesos(ContextoHB contexto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectCountAccesoBiometria", "homebanking");
		sqlRequest.sql = "SELECT count(*) as dispositivos FROM [homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ? and [fecha_biometria_activa] is not NULL and [biometria_activa] = 1";
		sqlRequest.parametros.add(contexto.idCobis());
		response = Sql.response(sqlRequest);
		return response;
	}

}
