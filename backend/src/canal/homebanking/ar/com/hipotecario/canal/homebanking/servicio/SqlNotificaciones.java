package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlNotificaciones {

	public static SqlResponse getNotificacionSolicitud(ContextoHB contexto, String solicitud) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse getNotificacionProducto(ContextoHB contexto, String producto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [producto] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse getNotificacionProductoNoBorrado(ContextoHB contexto, String producto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[notificacion_estado_solicitud] WHERE [id_cobis] = ? AND [producto] = ? AND [borrado] = 0";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse insertNotificacionSolicitud(ContextoHB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("InsertNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[notificacion_estado_solicitud] ([id_cobis], [solicitud], [estado], [momento], [leido], [borrado], [producto]) VALUES (?, ?, ?, GETDATE(), ?, ?, ?) ";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(producto);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse updateNotificacionSolicitud(ContextoHB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [estado] = ?, [momento] = GETDATE(), [leido] = ?, [borrado] = ?, [producto] = ? WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(producto);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse updateNotificacionSolicitudLeidoBorrado(ContextoHB contexto, String solicitud, Boolean leido, Boolean borrar) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [momento] = GETDATE(), [leido] = ?, [borrado] = ? WHERE [id_cobis] = ? AND [solicitud] = ?";
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(solicitud);
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse updateNotificacionesPorIdCobisLeidoBorrado(ContextoHB contexto, Boolean leido, Boolean borrar) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [momento] = GETDATE(), [leido] = ?, [borrado] = ? WHERE [id_cobis] = ?";
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(contexto.idCobis());
		response = Sql.response(sqlRequest);
		return response;
	}

	public static SqlResponse updateNotificacionProducto(ContextoHB contexto, String solicitud, Boolean leido, Boolean borrar, String estado, String producto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("UpdateNotificacionEstadoSolicitud", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_estado_solicitud] SET [estado] = ?, [momento] = GETDATE(), [leido] = ?, [borrado] = ?, [solicitud] = ? WHERE [id_cobis] = ? AND [producto] = ?";
		sqlRequest.parametros.add(estado);
		sqlRequest.parametros.add(leido ? 1 : 0);
		sqlRequest.parametros.add(borrar ? 1 : 0);
		sqlRequest.parametros.add(solicitud);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(producto);
		response = Sql.response(sqlRequest);
		return response;
	}

}
