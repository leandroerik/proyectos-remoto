package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlPrestamos {

	public static Boolean consultaModfDatosSensibles(ContextoMB contexto) {

		Integer cantidadDiasNormativoPrestamo = ConfigMB.integer("cantidad_dias_normativo_prestamo", 10) * (-1);
		SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");

		sqlRequest.sql = "SELECT top 1 momento FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND momento > DATEADD(day, " + cantidadDiasNormativoPrestamo.toString() + ", GETDATE()) AND tipo in (?,?,?,?) order by momento desc";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add("CAMBIO_CLAVE");
		sqlRequest.add("CAMBIO_USUARIO");
		sqlRequest.add("CAMBIO_TELEFONO");
		sqlRequest.add("CAMBIO_MAIL");
		Integer cantidad = SqlMB.response(sqlRequest).registros.size();
		if (cantidad >= 1) {
			return true;
		}
		return false;
	}

	public static Objeto consultaModfDatosSensiblesObj(ContextoMB contexto) {

		Integer cantidadDiasNormativoPrestamo = ConfigMB.integer("cantidad_dias_normativo_prestamo", 10) * (-1);
		SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");

		sqlRequest.sql = "SELECT top 1 momento FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND momento > DATEADD(day, " + cantidadDiasNormativoPrestamo.toString() + ", GETDATE()) AND tipo in (?,?,?,?) order by momento desc";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add("CAMBIO_CLAVE");
		sqlRequest.add("CAMBIO_USUARIO");
		sqlRequest.add("CAMBIO_TELEFONO");
		sqlRequest.add("CAMBIO_MAIL");
		Integer cantidad = SqlMB.response(sqlRequest).registros.size();
		if (cantidad >= 1) {
			return new Objeto().set("modificado", true).set("momento", SqlMB.response(sqlRequest).registros.get(0).string("momento"));
		}
		return null;
	}

	public static Boolean permitidoModal(ContextoMB contexto, String nemonico) {

		Integer omiteModalPPDias = ConfigMB.integer("omite_modal_PP_dias");
		SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");

		sqlRequest.sql = "SELECT top 1  DATEDIFF(day, momento, GETDATE()) AS 'dias' " + "    FROM [Homebanking].[dbo].[contador] WHERE idCobis = ? AND canal = 'MB' " + "	AND tipo in (?) order by momento desc";

		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(nemonico);
		List<Objeto> listaOmitidos = SqlMB.response(sqlRequest).registros;
		if (!listaOmitidos.isEmpty()) {
			Integer fechaOmitido = listaOmitidos.get(0).integer("dias");
			if (fechaOmitido >= omiteModalPPDias) {
				return true;
			}
			return false;
		}
		return true;
	}

	public static Boolean guardaSolicitudCanalAmarillo(ContextoMB contexto, String idSolicitud, Integer idActividad, String idSituacionLaboral, String categoriaMonotributo, BigDecimal ingresosNetos, Date fechaInicio, String cuitEmpleador) {
		String fecha = fechaInicio != null ? new SimpleDateFormat("dd/MM/yyyy").format(fechaInicio) : null;
		SqlRequestMB sqlRequest = SqlMB.request("InsertSolicitudCanalAmarillo", "homebanking");

		sqlRequest.sql = "UPDATE [homebanking].[dbo].[solicitud_canal_amarillo_PP]";
		sqlRequest.sql += " SET [momento] = GETDATE(), [modifica_actividad] = ?, [situacion_labora_nueva] = ?, ";
		sqlRequest.sql += " [categoria_monotributo_nueva] = ?, [ingresos_nuevos] = ?, [fecha_ingreso_nuevo] = ?, [cuit_empleador_nuevo] = ?, [estado] = ?";
		sqlRequest.sql += " WHERE [id_solicitud] = ? and [id_cobis] = ? ";
		sqlRequest.parametros.add(1);
		sqlRequest.parametros.add(idSituacionLaboral);
		sqlRequest.parametros.add(categoriaMonotributo);
		sqlRequest.parametros.add(Formateador.importe(ingresosNetos));
		sqlRequest.parametros.add(fecha);
		sqlRequest.parametros.add(cuitEmpleador);
		sqlRequest.parametros.add(1);
		sqlRequest.parametros.add(idSolicitud);
		sqlRequest.parametros.add(contexto.idCobis());

		sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
		sqlRequest.sql += "INSERT INTO [homebanking].[dbo].[solicitud_canal_amarillo_PP]  ([id_solicitud], [id_cobis], [id_actividad], [momento], [modifica_actividad], [situacion_labora_nueva], [categoria_monotributo_nueva], [ingresos_nuevos], [fecha_ingreso_nuevo], [cuit_empleador_nuevo], [estado]) VALUES (?,?,?,GETDATE(),?,?,?,?,?,?,?)";
		sqlRequest.parametros.add(idSolicitud);
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(idActividad);
		sqlRequest.parametros.add(1);
		sqlRequest.parametros.add(idSituacionLaboral);
		sqlRequest.parametros.add(categoriaMonotributo);
		sqlRequest.parametros.add(Formateador.importe(ingresosNetos));
		sqlRequest.parametros.add(fecha);
		sqlRequest.parametros.add(cuitEmpleador);
		sqlRequest.parametros.add(1);

		try {
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	public static Objeto solicitudCanalAmarillo(ContextoMB contexto, String idSolicitud) {
		RespuestaMB respuesta = new RespuestaMB();
		SqlRequestMB sqlRequest = SqlMB.request("SolicitudCanalAmarillo", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[solicitud_canal_amarillo_PP] WHERE [id_solicitud] = ? and [id_cobis] = ? and estado = 1 ";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponseMB response = SqlMB.response(sqlRequest);
		if (response.registros.size() >= 1) {
			return respuesta.set("solicitud", response.registros.get(0));
		}
		return RespuestaMB.exito();
	}

	public static Objeto finalizaSolicitudCanalAmarillo(ContextoMB contexto, String idSolicitud) {
		RespuestaMB respuesta = new RespuestaMB();
		SqlRequestMB sqlRequest = SqlMB.request("finalizaCanalAmarillo", "homebanking");
		sqlRequest.sql = "UPDATE [homebanking].[dbo].[solicitud_canal_amarillo_PP] ";
		sqlRequest.sql += "SET [momento] = GETDATE(), estado = 0";
		sqlRequest.sql += "WHERE [id_solicitud] = ? and [id_cobis] = ? ";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponseMB response = SqlMB.response(sqlRequest);
		return respuesta.set("solicitudCanalAmarilloPendiente", response.registros);
	}

	public static Boolean insertEliminaSolicitud(ContextoMB contexto, String idSolicitud) {
		if (!idSolicitud.equals("") && idSolicitud != null) {
			SqlRequestMB sqlRequest = SqlMB.request("EliminaSolicitud", "homebanking");
			sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log_elimina_solicitud] ([id_solicitud], [id_cobis], [canal], [fecha]) VALUES (?,?,?,GETDATE())";
			sqlRequest.add(idSolicitud);
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add("MB");

			try {
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (sqlResponse.hayError) {
					return false;
				}
			} catch (Exception e) {
				System.out.println(e);
				return false;
			}
			return true;
		}
		return false;
	}

	public static Objeto selectEliminaSolicitud(ContextoMB contexto, String idSolicitud) {
		RespuestaMB respuesta = new RespuestaMB();
		SqlRequestMB sqlRequest = SqlMB.request("EliminaSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[log_elimina_solicitud] WHERE [id_solicitud] = ? AND [id_cobis] = ?";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponseMB response = SqlMB.response(sqlRequest);
		if (response.registros.size() >= 1) {
			return respuesta.set("solicitud", response.registros.get(0));
		}
		return null;
	}

	public static void eliminarCacheActividades(ContextoMB contexto) {
		ApiMB.eliminarCache(contexto, "Actividades", contexto.idCobis());
	}

}
