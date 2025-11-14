package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.negocio.EnumErrores;

public class BiometriaService {

	public static ApiResponse generaRefreshTokens(ContextoHB contexto, String refreshToken) {

		ApiRequest request = Api.request("GeneraAccessTokens", "seguridad", "GET", "/v1/refress", contexto);
		request.permitirSinLogin = true;

		request.headers.put("refresh_token", refreshToken);
		request.headers.put("grant_type", "refresh_token");

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse revocaBiometria(ContextoHB contexto, String metodo, String publicKey) {
		String token = contexto.sesion.cache.get("token_biometria");
		String dispositivo = contexto.sesion.cache.get("id_Dispositivo");
		ApiRequest request = Api.request("RevocaBiometria", "seguridad", "PATCH", "/v1/autenticacion/revocar", contexto);
		if (metodo.equalsIgnoreCase("todo")) {
			request = Api.request("RevocaBiometria", "seguridad", "PATCH", "/v1/tokenautenticador/revocar", contexto);
			dispositivo = "";
		}

		request.query("metodo", metodo);
		Objeto objAutenticador = new Objeto();
		objAutenticador.set("algorithm", "SHA512withRSA");
		objAutenticador.set("enabled", true);
		objAutenticador.set("keyHandle", dispositivo);
		objAutenticador.set("publicKey", publicKey);
		request.body(objAutenticador);

		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis(), dispositivo);
	}

	public static ApiResponse consultaUsuario(ContextoHB contexto, String token) {
		ApiRequest request = Api.request("ConsultarUsuarioIsva", "seguridad", "GET", "/v1/autenticacion", contexto);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static Boolean deleteAccesos(ContextoHB contexto) {
		try {
			SqlRequest sqlRequest = Sql.request("RevocaAccesosBiometria", "homebanking");
			sqlRequest.sql = "UPDATE [homebanking].[dbo].[usuarios_biometria_b] SET [fecha_disp_registrado] =  GETDATE(), [disp_registrado] = 0, [biometria_activa] = 0, [fecha_biometria_activa] = GETDATE(), [buhoFacil_activo] = 0, [fecha_buhoFacil_activo] = GETDATE()" + " WHERE [id_cobis] = ?";
			sqlRequest.parametros.add(contexto.idCobis());
			Sql.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static SqlResponse selectAccesos(ContextoHB contexto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectAccesoBiometria", "homebanking");
		sqlRequest.sql = "SELECT [id_cobis],[fecha_disp_registrado],[disp_registrado],[fecha_biometria_activa],[biometria_activa],[id_dispositivo]," + "[fecha_buhoFacil_activo], [buhoFacil_activo], [access_token], [refresh_token], [fecha_token]" + " FROM [Homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ? order by [fecha_disp_registrado] desc";
		sqlRequest.parametros.add(contexto.idCobis());
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse selectLogsBiometria(ContextoHB contexto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectLogsBiometria", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[logs_biometria_b] where id_cobis = ? and id_dispositivo = ? and codigo_mensaje = 'E001' order by fecha_token asc ";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(contexto.parametros.string("dispositivo"));
		response = Sql.response(sqlRequest);
		return response;
	}

	public static Boolean updateToken(ContextoHB contexto, String token, String refressToken, String dispositivo) {
		SqlRequest sqlRequest = Sql.request("UpdateRefreshToken", "homebanking");
		SqlResponse sqlResponse = null;
		try {
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [access_token] = ?, [refresh_token] = ?, [fecha_token] = GETDATE()  WHERE [id_cobis] = ? and [id_dispositivo] = ? ";
			sqlRequest.parametros.add(token);
			sqlRequest.parametros.add(refressToken);
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(dispositivo);
			sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[logs_biometria_b] ([id_cobis], [access_token], [refresh_token], [fecha_token], [id_dispositivo], [codigo_mensaje], [mensaje]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, GETDATE(), ?, ?, ?) ";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(token);
			sqlRequest.add(refressToken);
			sqlRequest.add(dispositivo);
			sqlRequest.add(EnumErrores.ERROR_REFRESS_TOKEN.getCodigo());
			sqlRequest.add(EnumErrores.ERROR_REFRESS_TOKEN.getValor());
			sqlResponse = Sql.response(sqlRequest);

			if (sqlResponse.hayError) {
				biometriaServiceLog(contexto, "HayError", sqlRequest.sql, token + "," + refressToken + "," + contexto.idCobis() + "," + dispositivo);
				return false;
			}
		} catch (Exception e) {
			biometriaServiceLog(contexto, "Exception", sqlRequest.sql, token + "," + refressToken + "," + contexto.idCobis() + "," + dispositivo);
			return false;
		}
		biometriaServiceLog(contexto, "Exito", sqlRequest.sql, token + "," + refressToken + "," + contexto.idCobis() + "," + dispositivo);
		return true;
	}

	public static void biometriaServiceLog(ContextoHB contexto, String salida, String mensaje, String valores) {
		Objeto detalle = new Objeto();
		detalle.set("peticion", "UpdateToken en BD");
		detalle.set("respuesta", salida);
		detalle.set("mensaje", mensaje);
		detalle.set("valores", valores);
		AuditorLogService.biometriaLogVisualizador(contexto, "Api-Biometria_UpdateToken", detalle);
	}

	public static Boolean updateAccesoBiometria(ContextoHB contexto, Boolean biometriaActiva, String dispositivo) {
		try {

			SqlRequest sqlRequest = Sql.request("UpdateAccesoBiometria", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [fecha_biometria_activa] = GETDATE(), [biometria_activa] = ? WHERE [id_cobis] = ? and [id_dispositivo] = ?";

			if (biometriaActiva) {
				sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [fecha_biometria_activa] = GETDATE(), [biometria_activa] = ?," + " [fecha_buhoFacil_activo] = GETDATE(), [buhoFacil_activo] = 1 WHERE [id_cobis] = ? and [id_dispositivo] = ?";
			}

			sqlRequest.parametros.add(biometriaActiva ? 1 : 0);
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(dispositivo);
			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
