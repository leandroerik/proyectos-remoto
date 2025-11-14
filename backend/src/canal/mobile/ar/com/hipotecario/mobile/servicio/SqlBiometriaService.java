package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Enumlog;

public class SqlBiometriaService {

	public Boolean insertRegistroDispositivo(ContextoMB contexto, Boolean dispositivoRegistrado, Boolean biometriaActiva, Boolean buhoFacilActivo, String idDispositivo) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertRegistroDispositivo", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[usuarios_biometria_b] ([id_cobis],[fecha_disp_registrado],[disp_registrado]," + "[fecha_biometria_activa],[biometria_activa],[id_dispositivo],[fecha_buhoFacil_activo],[buhoFacil_activo],[access_token],[refresh_token],[fecha_token])" + " VALUES (?, GETDATE(), ?, NULL, ?, ?, GETDATE(), ?, ?, ?, GETDATE())";
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(dispositivoRegistrado ? 1 : 0);
			sqlRequest.parametros.add(biometriaActiva ? 1 : 0);
			sqlRequest.parametros.add(idDispositivo);
			sqlRequest.parametros.add(buhoFacilActivo ? 1 : 0);
			sqlRequest.parametros.add(contexto.sesion().cache("token_biometria"));
			sqlRequest.parametros.add(contexto.sesion().cache("refresh_token_biometria"));
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public Boolean updateAccesoBiometria(ContextoMB contexto, Boolean biometriaActiva, String dispositivo) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("UpdateAccesoBiometria", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [fecha_biometria_activa] = GETDATE(), [biometria_activa] = ? WHERE [id_cobis] = ? and [id_dispositivo] = ?";

			if (biometriaActiva) {
				sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [fecha_biometria_activa] = GETDATE(), [biometria_activa] = ?," + " [fecha_buhoFacil_activo] = GETDATE(), [buhoFacil_activo] = 1 WHERE [id_cobis] = ? and [id_dispositivo] = ?";
			}
			sqlRequest.parametros.add(biometriaActiva ? 1 : 0);
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(dispositivo);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public Boolean updateAccesoBuhoFacil(ContextoMB contexto, Boolean buhoFacilActivo, String dispositivo) {
		try {

			SqlRequestMB sqlRequest = SqlMB.request("UpdateAccesoBuhoFacil", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[usuarios_biometria_b] SET [fecha_buhoFacil_activo] = GETDATE(), [buhoFacil_activo] = ? WHERE [id_cobis] = ? and [id_dispositivo] = ? ";
			sqlRequest.parametros.add(buhoFacilActivo ? 1 : 0);
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(dispositivo);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public SqlResponseMB selectAccesos(ContextoMB contexto, String dispositivo) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectAccesoBiometria", "homebanking");
		sqlRequest.sql = "SELECT [id_cobis],[fecha_disp_registrado],[disp_registrado],[fecha_biometria_activa],[biometria_activa],[id_dispositivo]," + "[fecha_buhoFacil_activo], [buhoFacil_activo], [access_token], [refresh_token], [fecha_token]" + " FROM [Homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		if (dispositivo != null) {
			sqlRequest.sql += " and [id_dispositivo] = ?";
			sqlRequest.parametros.add(dispositivo);
		} else {
			sqlRequest.sql += " order by [fecha_disp_registrado] desc";
		}
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public SqlResponseMB selectCountAccesos(ContextoMB contexto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectCountAccesoBiometria", "homebanking");
		sqlRequest.sql = "SELECT count(*) as dispositivos FROM [homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ? and [fecha_biometria_activa] is not NULL and [biometria_activa] = 1";
		sqlRequest.parametros.add(contexto.idCobis());
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public Boolean deleteAccesos(ContextoMB contexto) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("RevocaAccesosBiometria", "homebanking");
			sqlRequest.sql = "UPDATE [homebanking].[dbo].[usuarios_biometria_b] SET [fecha_disp_registrado] =  GETDATE(), [disp_registrado] = 0, [biometria_activa] = 0, [fecha_biometria_activa] = GETDATE(), [buhoFacil_activo] = 0, [fecha_buhoFacil_activo] = GETDATE()" + " WHERE [id_cobis] = ?";
			sqlRequest.parametros.add(contexto.idCobis());
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean biometriaInsertLog(String acceso, String metodo, String estado, String idCobis,
																		String dispositivo, String refreshToken, String ip) {
		String tipoLog = Enumlog.LOG_BUHOFACIL.valor;
		if ("huella".equalsIgnoreCase(metodo)) {
			tipoLog = Enumlog.LOG_BIOMETRIA_HUELLA.valor;
		}
		if ("rostro".equalsIgnoreCase(metodo)) {
			tipoLog = Enumlog.LOG_BIOMETRIA_ROSTRO.valor;
		}

		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertLogBiometria", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_biometria] (idCobis, canal, fecha, metodo, tipo_metodo, acceso, id_dispositivo, refres_token, estado_acceso, direccionIp) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.add(idCobis);
			sqlRequest.add("MB");
			sqlRequest.add(tipoLog);
			sqlRequest.add(metodo.toUpperCase());
			sqlRequest.add(Enumlog.getValor(acceso));
			sqlRequest.add(dispositivo);
			sqlRequest.add(refreshToken);
			sqlRequest.add(estado);
			sqlRequest.add(ip);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public SqlResponseMB selectLogsBiometria(ContextoMB contexto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectLogsBiometria", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[logs_biometria_b] where id_cobis = ? and id_dispositivo = ? and codigo_mensaje = 'E001' order by fecha_token desc ";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(contexto.parametros.string("dispositivo"));
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public Boolean updateToken(ContextoMB contexto, String token, String refressToken, String dispositivo) {
		SqlRequestMB sqlRequest = null;
		SqlResponseMB sqlResponse = null;
		try {
			sqlRequest = SqlMB.request("UpdateRefreshToken", "homebanking");
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
			sqlRequest.add(Enumlog.ERROR_REFRESS_TOKEN.codigo);
			sqlRequest.add(Enumlog.ERROR_REFRESS_TOKEN.valor);
			sqlResponse = SqlMB.response(sqlRequest);

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

	public void biometriaServiceLog(ContextoMB contexto, String salida, String mensaje, String valores) {
		Objeto detalle = new Objeto();
		detalle.set("peticion", "UpdateToken en BD");
		detalle.set("respuesta", salida);
		detalle.set("mensaje", mensaje);
		detalle.set("valores", valores);
		AuditorLogService.biometriaLogVisualizador(contexto, "Api-Biometria_UpdateToken", detalle);
	}

}
