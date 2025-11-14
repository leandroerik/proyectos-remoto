package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;

public class SqlPrestamos {

	public static Boolean permitidoModal(ContextoHB contexto, String nemonico) {

		Integer omiteModalPPDias = ConfigHB.integer("omite_modal_PP_dias");
		SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");

		sqlRequest.sql = "SELECT top 1  DATEDIFF(day, momento, GETDATE()) AS 'dias' " + "    FROM [Homebanking].[dbo].[contador] WHERE idCobis = ? AND canal = 'HB' " + "	AND tipo in (?) order by momento desc";

		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(nemonico);
		List<Objeto> listaOmitidos = Sql.response(sqlRequest).registros;
		if (!listaOmitidos.isEmpty()) {
			Integer fechaOmitido = listaOmitidos.get(0).integer("dias");
			if (fechaOmitido >= omiteModalPPDias) {
				return true;
			}
			return false;
		}
		return true;
	}

	public static Objeto finalizaSolicitudCanalAmarillo(ContextoHB contexto, String idSolicitud) {
		Respuesta respuesta = new Respuesta();
		SqlRequest sqlRequest = Sql.request("finalizaCanalAmarillo", "homebanking");
		sqlRequest.sql = "UPDATE [homebanking].[dbo].[solicitud_canal_amarillo_PP] ";
		sqlRequest.sql += "SET [momento] = GETDATE(), estado = 0";
		sqlRequest.sql += "WHERE [id_solicitud] = ? and [id_cobis] = ? ";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponse response = Sql.response(sqlRequest);
		return respuesta.set("solicitudCanalAmarilloPendiente", response.registros);
	}

	public static Objeto solicitudCanalAmarillo(ContextoHB contexto, String idSolicitud) {
		Respuesta respuesta = new Respuesta();
		SqlRequest sqlRequest = Sql.request("SolicitudCanalAmarillo", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[solicitud_canal_amarillo_PP] WHERE [id_solicitud] = ? and [id_cobis] = ? and estado = 1 ";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponse response = Sql.response(sqlRequest);
		if (response.registros.size() >= 1) {
			return respuesta.set("solicitud", response.registros.get(0));
		}
		return Respuesta.exito();
	}

	public static Boolean guardaSolicitudCanalAmarillo(ContextoHB contexto, String idSolicitud, Integer idActividad, String idSituacionLaboral, String categoriaMonotributo, BigDecimal ingresosNetos, Date fechaInicio, String cuitEmpleador) {
		String fecha = fechaInicio != null ? new SimpleDateFormat("dd/MM/yyyy").format(fechaInicio) : null;
		SqlRequest sqlRequest = Sql.request("InsertSolicitudCanalAmarillo", "homebanking");

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
			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (sqlResponse.hayError) {
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	public static Boolean insertEliminaSolicitud(ContextoHB contexto, String idSolicitud) {
		if (!idSolicitud.equals("") && idSolicitud != null) {
			SqlRequest sqlRequest = Sql.request("EliminaSolicitud", "homebanking");
			sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log_elimina_solicitud] ([id_solicitud], [id_cobis], [canal], [fecha]) VALUES (?,?,?,GETDATE())";
			sqlRequest.add(idSolicitud);
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add("MB");

			try {
				SqlResponse sqlResponse = Sql.response(sqlRequest);
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

	public static Objeto selectEliminaSolicitud(ContextoHB contexto, String idSolicitud) {
		Respuesta respuesta = new Respuesta();
		SqlRequest sqlRequest = Sql.request("EliminaSolicitud", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[log_elimina_solicitud] WHERE [id_solicitud] = ? AND [id_cobis] = ?";
		sqlRequest.add(idSolicitud);
		sqlRequest.add(contexto.idCobis());

		SqlResponse response = Sql.response(sqlRequest);
		if (response.registros.size() >= 1) {
			return respuesta.set("solicitud", response.registros.get(0));
		}
		return null;
	}
}
