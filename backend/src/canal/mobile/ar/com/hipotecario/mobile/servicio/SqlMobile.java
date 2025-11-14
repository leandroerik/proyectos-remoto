package ar.com.hipotecario.mobile.servicio;

import java.util.Objects;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.LoginToken;

public class SqlMobile {

	public static boolean persistirToken(LoginToken token) {
		try {
            SqlRequestMB request = SqlMB.request("InsertLoginToken", "homebanking");
			request.sql = "INSERT INTO [Mobile].[dbo].[token_login] ([token], [id_cobis], [fecha]) VALUES (?, ?, ?)";
			request.add(token.getUuid());
			request.add(token.getIdCobis());
			request.add(token.getFecha());
			SqlResponseMB response = SqlMB.response(request);
			if (response.hayError) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean persistirTokenSdk(RespuestaMB respuesta, ContextoMB contexto) {
		try {
			SqlRequestMB request = SqlMB.request("InsertTokenSdk", "hbs");
			request.sql = "INSERT INTO  [Mobile].[dbo].[MODO_Token_Sdk] ([id_cobis], [user_id], [access_token],"
					+ "[refresh_token],[secure_pym_secret] , [fecha_actualizacion]) VALUES (?, ?, ?, ?, ?, GETDATE())";
			request.add(contexto.sesion().idCobis());
			request.add(respuesta.get("userId"));
			request.add(respuesta.get("token"));
			request.add(respuesta.get("refresh_token"));
			request.add(respuesta.get("securePaymentSecret"));
			SqlResponseMB response = SqlMB.response(request);
			if (response.hayError) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean actualizarToken(ContextoMB contexto, RespuestaMB respuesta) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("ActualizarTokenSdk", "hbs");
			sqlRequest.sql = "UPDATE [Mobile].[dbo].[MODO_Token_Sdk] SET [access_token] = ?,"
					+ "[refresh_token] = ?, [fecha_actualizacion] = GETDATE()  WHERE [id_cobis] = ?";
			sqlRequest.parametros.add(respuesta.get("token"));
			sqlRequest.parametros.add(respuesta.get("refresh_token"));
			sqlRequest.parametros.add(contexto.idCobis());
			SqlResponseMB response = SqlMB.response(sqlRequest);
			if (response.hayError) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public SqlResponseMB consultarToken(ContextoMB contexto) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("ConsultarTokenSdk", "hbs");
			sqlRequest.sql = "SELECT * FROM [Mobile].[dbo].[MODO_Token_Sdk] WHERE id_cobis = ? ";
			sqlRequest.add(contexto.sesion().idCobis());
			SqlResponseMB response = SqlMB.response(sqlRequest);
			return response;
		} catch (Exception error) {
			return null;
		}
	}

	public static boolean persistirUsuario(Objeto usuario) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectUsuarioMobile", "homebanking");
			sqlRequest.sql = "SELECT * FROM[Mobile].[dbo].[usuarios] WHERE id_cobis =" + usuario.get("idCobis");
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (sqlResponse.registros.isEmpty()) {
				SqlRequestMB request = SqlMB.request("InsertUsuario", "hbs");
				request.sql = "INSERT INTO [Mobile].[dbo].[usuarios] ([token],[email],[nombre_usuario]," + "[id_cobis],[dispositivo],[version_os],[version_app],[ubi_latitud],[ubi_longitud],[fecha_ult_login]," + "[fecha_creacion]) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				request.add(usuario.get("token"));
				request.add(usuario.get("email"));
				request.add(usuario.get("nombreUsuario"));
				request.add(usuario.get("idCobis"));
				request.add(usuario.get("dispositivo"));
				request.add(usuario.get("versionOS"));
				request.add(usuario.get("versionApp"));
				request.add(usuario.get("ubiLatitud"));
				request.add(usuario.get("ubiLongitud"));
				request.add(usuario.get("fechaUltLogin"));
				request.add(usuario.get("fechaCreacion"));
				SqlResponseMB response = SqlMB.response(request);
				if (response.hayError) {
					return false;
				}
				return true;
			} else {
				SqlRequestMB request = SqlMB.request("InsertUsuario", "hbs");
				request.sql = "UPDATE [Mobile].[dbo].[usuarios]  " + "SET [token] = ?";
				request.parametros.add(usuario.get("token"));
				if (Objects.nonNull(usuario.get("email")) && !usuario.string("email").isEmpty()) {
					request.sql += ", [email] = ?";
					request.parametros.add(usuario.get("email"));
				}
				if (Objects.nonNull(usuario.get("nombreUsuario")) && !usuario.string("nombreUsuario").isEmpty()) {
					request.sql += ", [nombre_usuario] = ?";
					request.parametros.add(usuario.get("nombreUsuario"));
				}
				if (Objects.nonNull(usuario.get("dispositivo")) && !usuario.string("dispositivo").isEmpty()) {
					request.sql += ", [dispositivo] = ?";
					request.parametros.add(usuario.get("dispositivo"));
				}
				if (Objects.nonNull(usuario.get("versionOS")) && !usuario.string("versionOS").isEmpty()) {
					request.sql += ", [version_os] = ?";
					request.parametros.add(usuario.get("versionOS"));
				}
				if (Objects.nonNull(usuario.get("versionApp")) && !usuario.string("versionApp").isEmpty()) {
					request.sql += ", [version_app] = ?";
					request.parametros.add(usuario.get("versionApp"));
				}
				if (Objects.nonNull(usuario.get("ubiLatitud"))) {
					request.sql += ", [ubi_latitud] = ?";
					request.parametros.add(usuario.get("ubiLatitud"));
				}
				if (Objects.nonNull(usuario.get("ubiLongitud"))) {
					request.sql += ", [ubi_longitud] = ?";
					request.parametros.add(usuario.get("ubiLongitud"));
				}
				if (Objects.nonNull(usuario.get("fechaUltLogin"))) {
					request.sql += ", [fecha_ult_login] = ?";
					request.parametros.add(usuario.get("fechaUltLogin"));
				}
				if (Objects.nonNull(usuario.get("fechaCreacion"))) {
					request.sql += ", [fecha_creacion] = ?";
					request.parametros.add(usuario.get("fechaCreacion"));
				}
				request.sql += " WHERE id_cobis =" + usuario.get("idCobis");

				SqlResponseMB response = SqlMB.response(request);
				if (response.hayError) {
					return false;
				}
				return true;
			}

		} catch (Exception e) {
			return false;
		}
	}
}
