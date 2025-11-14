package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlNotificaciones {

	public static SqlResponseMB getNotificacionSolicitud(ContextoMB contexto, String solicitud) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB getNotificacionProducto(ContextoMB contexto, String producto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [producto] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = SqlMB.response(sqlRequest);
		return response;
	}

		public static SqlResponseMB getNotificacionProductoNoBorrado(ContextoMB contexto, String producto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [producto] = ? AND [borrado] = 0";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB insertNotificacionSolicitud(ContextoMB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("InsertNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[notificacion_estado_solicitud] ([id_cobis], [solicitud], [estado], [momento], [leido], [borrado], [producto]) VALUES (?, ?, ?, GETDATE(), ?, ?, ?) ";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(producto);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB updateNotificacionSolicitud(ContextoMB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [estado] = ?, [momento] = GETDATE(), [leido] = ?, [borrado] = ?, [producto] = ? WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(producto);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB updateNotificacionSolicitudLeidoBorrado(ContextoMB contexto, String solicitud, Boolean leido, Boolean borrar) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [momento] = GETDATE(), [leido] = ?, [borrado] = ? WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB updateNotificacionesPorIdCobisLeidoBorrado(ContextoMB contexto, Boolean leido, Boolean borrar) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [momento] = GETDATE(), [leido] = ?, [borrado] = ? WHERE [id_cobis] = ?";
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(contexto.idCobis());
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static SqlResponseMB updateNotificacionProducto(ContextoMB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponseMB response = null;
		SqlRequestMB sqlRequest = SqlMB.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [estado] = ?, [momento] = GETDATE(), [leido] = ?, [borrado] = ?, [solicitud] = ? WHERE [id_cobis] = ? AND [producto] = ?";
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(solicitud);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = SqlMB.response(sqlRequest);
		return response;
	}

}
