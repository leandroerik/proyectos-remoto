package ar.com.hipotecario.backend.servicio.sql.buhobank;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank.LogBuhoBank;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.buhobank.LogBB;
import ar.com.hipotecario.canal.buhobank.SesionBB;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class LogsBuhoBank extends SqlObjetos<LogBuhoBank> {

	/* ========== ATRIBUTOS ========== */
	public static class LogBuhoBank extends SqlObjeto {
		public String id;
		public Fecha momento;
		public String cuit;
		public String endpoint;
		public String evento;
		public String datos;
		public String error;
		public Integer idProceso;
		public String ip;
	}

	/* ========== SERVICIO ========== */
	public static LogsBuhoBank get(Contexto contexto) {
		return get(contexto, Fecha.hoy());
	}

	public static LogsBuhoBank getReporteRegistroByFecha(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT endpoint, evento, COUNT(*) AS datos ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ? ";
		sql += "AND endpoint NOT LIKE '%Fijo%' ";
		sql += "AND evento NOT LIKE '%FRONT%' ";
		sql += "GROUP BY endpoint, evento ";
		sql += "ORDER BY datos DESC ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getRegistroByFecha(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ? ";
		sql += "ORDER BY momento DESC ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getFinalizadosByFecha(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, Boolean esBatch) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ? ";
		sql += "AND evento = 'BB_FINALIZAR_OK' ";

		if(esBatch){
			sql += "AND endpoint != '/bb/api/finalizar' ";
		}
		else {
			sql += "AND endpoint = '/bb/api/finalizar' ";
		}

		sql += "ORDER BY momento DESC ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getCasosVuByFecha(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ? ";
		sql += "AND endpoint = '/bb/api/guardarvucompleto' ";
		sql += "AND evento IN ('BB_VU_PERSON_OK', 'BB_VU_TOTAL_OK') ";
		sql += "ORDER BY momento DESC ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getCasosErrorByFecha(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT *";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE ? <= momento AND momento <= ? ";
		sql += "AND cuit NOT IN (SELECT cuit FROM [buhobank].[dbo].[log] WITH (NOLOCK) WHERE ? <= momento AND evento = 'BB_FINALIZAR_OK') ";
		sql += "AND id NOT IN (SELECT id FROM log WITH (NOLOCK) WHERE ? <= momento AND datos LIKE '%lista de informados%') ";
		sql += "AND (evento IN ('BB_ERROR_CRITICO', 'BB_ERROR_SQL') ";
		sql += "OR (evento IN ('BB_ERROR') ";
		sql += "AND endpoint IN ('', '/bb/api/batch', '/bb/api/finalizar'))) ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta, fechaDesde, fechaDesde);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getRegistroByCuil(Contexto contexto, String cuil, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE [cuit]= ? ";
		sql += "AND momento BETWEEN ? AND ? ";
		sql += "ORDER BY momento DESC ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, cuil, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getRegistro(Contexto contexto, String cuil, Fecha fechaDesde, Fecha fechaHasta) {

		if (Utils.isEmpty(cuil)) {
			return getRegistroByFecha(contexto, fechaDesde, fechaHasta);
		}

		return getRegistroByCuil(contexto, cuil, fechaDesde, fechaHasta);
	}

	public static LogsBuhoBank get(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE [cuit]= ?";
		Objeto datos = Sql.select(contexto, "buhobank", sql, cuil);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank get(Contexto contexto, Fecha fecha) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ?";
		Objeto datos = Sql.select(contexto, "buhobank", sql, fecha, fecha.sumarDias(1));
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank get(Contexto contexto, String cuil, Fecha fecha) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE momento BETWEEN ? AND ? AND cuit = ?";
		Objeto datos = Sql.select(contexto, "buhobank", sql, fecha, fecha.sumarDias(1), cuil);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static Boolean post(Contexto contexto, String cuit, String endpoint, String evento, String datos, String error, String idProceso) {
		String sql = "";
		sql += "INSERT INTO [buhobank].[dbo].[log] ";
		sql += "([momento],[cuit],[endpoint],[evento],[datos],[error],[idProceso],[ip]) ";
		sql += "VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?) ";
		Object[] parametros = new Object[7];
		parametros[0] = cuit;
		parametros[1] = endpoint;
		parametros[2] = evento;
		parametros[3] = datos;
		parametros[4] = error;
		parametros[5] = idProceso;
		parametros[6] = contexto.ip();
		return Sql.update(contexto, "buhobank", sql, parametros) == 1;
	}

	public static LogsBuhoBank getAbandonos(Contexto contexto, String cuil, String evento, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM log a WITH (NOLOCK) ";
		sql += "WHERE [cuit]= ? ";
		sql += "AND [evento]= ? ";
		sql += "AND [momento] > ? ";
		sql += "AND [id] = (SELECT MAX(id) FROM log b WITH (NOLOCK) WHERE a.cuit = b.cuit AND a.evento = b.evento GROUP BY cuit)";
		Objeto datos = Sql.select(contexto, "buhobank", sql, cuil, evento, fechaDesde);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static void actualizarCuilSesion(ContextoBB contexto, SesionBB sesion, String nuevoCuil) {
		LogBB.evento(contexto, GeneralBB.REEMPLAZAR_CUIL);
		Fecha fechaDesde = sesion.fechaLogin.restarMinutos(3);
		actualizarCuil(contexto, sesion.cuil, nuevoCuil, fechaDesde);
	}

	public static Boolean actualizarCuil(Contexto contexto, String cuil, String nuevoCuil, Fecha fechaDesde) {

		String sql = "";
		sql += "UPDATE [buhobank].[dbo].[log] ";
		sql += "SET cuit = ? ";
		sql += "WHERE cuit = ? ";
		sql += "AND momento > ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, nuevoCuil, cuil, fechaDesde) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "getAbandonos";
		if ("get".equals(test)) {
			Contexto contexto = contexto("BB", "homologacion");
			LogsBuhoBank datos = get(contexto);
			imprimirResultado(contexto, datos);
		}
		if ("post".equals(test)) {
			Contexto contexto = contexto("BB", "desarrollo");
			String cuit = "201234567891";
			String endpoint = "/test";
			String evento = "test";
			String datos = "{}";
			String error = null;
			String idProceso = "1";
			Boolean exito = post(contexto, cuit, endpoint, evento, datos, error, idProceso);
			imprimirResultado(contexto, exito);
		}
		if ("getAbandonos".equals(test)) {
			Contexto contexto = contexto("BB", "desarrollo");
			String cuit = "23347485314";
			String evento = "BB_VALIDAR_DATOS_PERSONALES_OK";
			LogsBuhoBank datos = getAbandonos(contexto, cuit, evento, Fecha.hoy().restarDias(2));
			imprimirResultado(contexto, datos);
		}
	}

	public static LogsBuhoBank obtenerRegistros(Contexto contexto, String cuit, Fecha fechadesde) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE cuit = ? ";
		sql += "AND momento >= ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, cuit, fechadesde);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getReporteErrorByPeriodo(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT t2.evento, t2.fecha AS momento, COUNT(*) AS datos ";
		sql += "FROM ( ";
		sql += "SELECT t1.* ";
		sql += "FROM ( ";
		sql += "SELECT cuit, 'BB_ERROR' AS evento, error, endpoint, CONVERT(DATE, momento) AS fecha ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE ? <= momento AND momento <= ? ";
		sql += "AND cuit NOT IN (SELECT cuit FROM [buhobank].[dbo].[log] WITH (NOLOCK) WHERE ? <= momento AND evento = 'BB_FINALIZAR_OK') ";
		sql += "AND id NOT IN (SELECT id FROM log WITH (NOLOCK) WHERE ? <= momento AND datos LIKE '%lista de informados%') ";
		sql += "AND (evento IN ('BB_ERROR_CRITICO', 'BB_ERROR_SQL') ";
		sql += "OR (evento IN ('BB_ERROR') ";
		sql += "AND endpoint IN ('', '/bb/api/batch', '/bb/api/finalizar'))) ";
		sql += ") AS t1 ";
		sql += "GROUP BY t1.cuit, t1.evento, t1.error, t1.endpoint, t1.fecha ";
		sql += ") AS t2 ";
		sql += "GROUP BY t2.evento, t2.fecha ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta, fechaDesde, fechaDesde);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getReporteEventoByPeriodo(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String evento) {
		String sql = "";
		sql += "SELECT t2.evento, t2.fecha AS momento, COUNT(*) AS datos ";
		sql += "FROM ( ";
		sql += "SELECT t1.* ";
		sql += "FROM ( ";
		sql += "SELECT cuit, endpoint, evento, CONVERT(DATE, momento) AS fecha ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE ? <= momento AND momento <= ? ";
		sql += "AND evento = ? ";
		sql += ") AS t1 ";
		sql += "GROUP BY t1.cuit, t1.endpoint, t1.evento, t1.fecha ";
		sql += ") AS t2 ";
		sql += "GROUP BY t2.evento, t2.fecha ";

		Objeto datos = Sql.select(contexto, "buhobank", sql, fechaDesde, fechaHasta, evento);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static Boolean ejecutarHistorico(Contexto contexto, Fecha fechaDesde) {

		String sql = "EXEC [dbo].[sp_bb_actualizar_historico_push] ";
		sql += "? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, fechaDesde) > 0;
	}

	public static LogsBuhoBank obtenerSesiones(Contexto contexto, Fecha fechadesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT id, fecha_ultima_modificacion AS momento, cuil AS cuit, estado AS evento, tipo_standalone AS datos, CONVERT(VARCHAR, fecha_inicio, 120) AS error, id_solicitud_duenios AS idProceso ";
		sql += "FROM [esales].[dbo].[Sesion] WITH (NOLOCK) ";
		sql += "WHERE fecha_ultima_modificacion >= ? ";
		sql += "AND fecha_ultima_modificacion <= ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechadesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerSesionesEstado(Contexto contexto, String estado, Fecha fechadesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT id, fecha_ultima_modificacion AS momento, cuil AS cuit, tipo_standalone AS evento, CONVERT(VARCHAR, fecha_inicio, 120) AS error, id_solicitud_duenios AS idProceso, ? AS datos ";
		sql += "FROM [esales].[dbo].[Sesion] WITH (NOLOCK) ";
		sql += "WHERE fecha_ultima_modificacion >= ? ";
		sql += "AND fecha_ultima_modificacion <= ? ";
		sql += "AND estado = ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, estado, fechadesde, fechaHasta, estado);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerAuditoriaSP(Contexto contexto, Fecha fechadesde, Fecha fechaHasta, String evento) {
		String sql = "";
		sql += "SELECT Id as id, Fecha AS momento, Paso AS cuit, Detalle AS evento, Evento AS datos, Detalle AS error, Detalle AS idProceso ";
		sql += "FROM [esales].[dbo].[Auditoria_SP] WITH (NOLOCK) ";
		sql += "WHERE Fecha >= ? ";
		sql += "AND Fecha <= ? ";
		sql += "AND Evento = ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechadesde, fechaHasta, evento);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank getSolicitudesSD(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		String sql = "";
		sql += "SELECT cobis as id, fecha as momento, nro_solicitud as cuit, '' as datos, 1 as idProceso, ";
		sql += "'Estado: ' + CONVERT(VARCHAR, estado_solicitud) + ' Aceptado: ' + CONVERT(VARCHAR, aceptado) + ' Resolucion: ' + IIF(resolucion_id is null, '', resolucion_id) + ' AceptaNuevaPropuesta: ' + CONVERT(VARCHAR, IIF(acepta_nueva_propuesta is null, 0, acepta_nueva_propuesta)) AS evento ";
		sql += "FROM [homebanking].[dbo].[aceptacion_digital] WITH (NOLOCK) ";
		sql += "WHERE Fecha >= ? ";
		sql += "AND Fecha <= ? ";
		sql += "ORDER by cuit DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechaDesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerAnonimizarVU(Contexto contexto) {
		String sql = "";
		sql += "SELECT TOP(1000) Id as id, username_cuil AS momento, username_cuil AS cuit, respuestaVu AS evento, operation_vu AS datos, username_cuil AS error, username_cuil AS idProceso ";
		sql += "FROM [esales].[dbo].[IDs_anonimizar_vu] WITH (NOLOCK) ";
		sql += "WHERE borrado = 0";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerCantidadErrorAnonimizarVU(Contexto contexto) {
		String sql = "";
		sql += "SELECT 1 as id, GETDATE() as momento, 'Cantidad: ' + CONVERT(VARCHAR, COUNT(*)) AS cuit, 'Procesados con error' as evento, '' as datos, '' as error, 1 as idProceso ";
		sql += "FROM [esales].[dbo].[IDs_anonimizar_vu] WITH (NOLOCK) ";
		sql += "WHERE borrado = 0 ";
		sql += "AND respuestaVu is not null ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerCantidadProcesadosAnonimizarVU(Contexto contexto) {
		String sql = "";
		sql += "SELECT 1 as id, GETDATE() as momento, 'Cantidad: ' + CONVERT(VARCHAR, COUNT(*)) AS cuit, 'Procesados correctamente' as evento, '' as datos, '' as error, 1 as idProceso ";
		sql += "FROM [esales].[dbo].[IDs_anonimizar_vu] WITH (NOLOCK) ";
		sql += "WHERE borrado = 1 ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	public static LogsBuhoBank obtenerCantidadNoProcesadosAnonimizarVU(Contexto contexto) {
		String sql = "";
		sql += "SELECT 1 as id, GETDATE() as momento, 'Cantidad: ' + CONVERT(VARCHAR, COUNT(*)) AS cuit, 'Sin procesar' as evento, '' as datos, '' as error, 1 as idProceso ";
		sql += "FROM [esales].[dbo].[IDs_anonimizar_vu] WITH (NOLOCK) ";
		sql += "WHERE borrado = 0 ";
		sql += "AND respuestaVu is null ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}

	// borrado de sesiones no finalizadas, solo se conservan los ultimos 30 dias
	public static Boolean ejecutarBorradoDeSesiones(Contexto contexto, Fecha fechaCorte) {

		String sql = "EXEC [dbo].[sp_LimpiarSesionesAntiguas] ";
		sql += "? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, fechaCorte) > 0;
	}

	public static Objeto obtenerClientesAnonimizarVu(ContextoBB contexto) {

		String sql = "SELECT TOP(1000) * FROM [esales].[dbo].[IDs_anonimizar_vu] WITH (NOLOCK) ";
		sql += "WHERE borrado = 0 AND respuestaVu IS NULL AND LEN(operation_vu) > 5 ";
		sql += "ORDER BY Id";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);

		return datos;
	}

	public static Boolean actualizarClientesAnonimizarVu(ContextoBB contexto, Integer id, Boolean borrado, String respuestaVu) {

		String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String respuestaAndDateVu = String.format("(%s) %s", fecha, respuestaVu);

		if (respuestaAndDateVu.length() > 255) {
			respuestaAndDateVu = respuestaAndDateVu.substring(0, 255);
		}

		String sql =
				"UPDATE [esales].[dbo].[IDs_anonimizar_vu] " +
						"SET borrado = ?, respuestaVu = ? " +
						"WHERE id = ?";

		return Sql.update(contexto, SqlEsales.SQL, sql, borrado, respuestaAndDateVu, id) == 1;
	}

	public static Boolean armarLock(ContextoBB contexto, String proceso) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[lockProceso] (proceso, fecha) ";
		sql += "VALUES (?, GETDATE()) ";

		return Sql.update(contexto, SqlEsales.SQL, sql, proceso) > 0;
	}

	public static Boolean desarmarLock(ContextoBB contexto, String proceso) {
		try {
			String sql = "";
			sql += "DELETE FROM [esales].[dbo].[lockProceso] ";
			sql += "WHERE proceso = ? ";

			Sql.update(contexto, SqlEsales.SQL, sql, proceso);
		} catch (Exception ignored) {
		}
		return true;
	}

	public static Boolean logCron(ContextoBB contexto, String evento, String paso, String detalle) {
		try {
			String sql = "";
			sql += "INSERT INTO [esales].[dbo].[Auditoria_SP] (Fecha, Evento, Paso, Detalle) ";
			sql += "VALUES (GETDATE(), ?, ?, ?) ";

			return Sql.update(contexto, SqlEsales.SQL, sql, evento, paso, detalle) > 0;
		} catch (Exception ignored) {
		}
		return true;
	}

	public static LogsBuhoBank obtenerSesionesFinalizadas(Contexto contexto, Fecha fechadesde, Fecha fechaHasta, String flujo) {
		String sql = "";
		sql += "SELECT t1.id, fecha_ultima_modificacion, cuil AS cuit, 'datos de tabla sesion' AS endpoint, t1.is_standalone, ";
		sql += "CONCAT(SUBSTRING(tipo_standalone, 5, LEN(tipo_standalone)-9), ";
		sql += "' - soli: ', id_solicitud_duenios, ";
		sql += "CASE WHEN (t1.is_standalone = 0 AND t2.tc_virtual = 1 AND t2.sucursal_onboarding LIKE '%FLUJO_TCV%') THEN ' - es TCV' ELSE '' END, ";
		sql += "CASE WHEN t2.requiereEmbozado = 'S' THEN ' + fisica' ELSE '' END) AS evento,";
		sql += "CONCAT(sucursal_onboarding, ' -', t2.plataforma) AS datos, NULL AS error, id_solicitud_duenios AS idProceso, ip ";
		sql += "FROM [esales].[dbo].[Sesion] t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.sesion_id ";
		sql += "WHERE t1.fecha_ultima_modificacion BETWEEN ? AND ? ";
		sql += "AND t1.estado = 'FINALIZAR_OK' ";

		if(!Utils.isEmpty(flujo)){
			sql += "AND t2.sucursal_onboarding LIKE '%" + flujo + "%'";
		}

		sql += "ORDER BY t1.id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechadesde, fechaHasta);
		return map(datos, LogsBuhoBank.class, LogBuhoBank.class);
	}
}
