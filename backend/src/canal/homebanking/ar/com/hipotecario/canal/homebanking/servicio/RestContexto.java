package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBAplicacion;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Util;

public class RestContexto {

	public static Boolean primeraTransferencia(ContextoHB contexto, String cbu) {
		SqlRequest sqlRequest = Sql.request("SelectCbuTransferidos", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[cbu_transferidos] WHERE idCobis = ? AND cbu = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(cbu);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return true;
		}
		return sqlResponse.registros.size() == 0;
	}

	public static Boolean registrarTransferencia(ContextoHB contexto, String cbu) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertCbuTransferidos", "homebanking");
			sqlRequest.sql = "INSERT INTO [Hbs].[dbo].[cbu_transferidos] (idCobis, cbu) VALUES (?, ?)";
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(cbu);
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
		return true;
	}

	public static Boolean agendada(ContextoHB contexto, String cbu, String cuenta) {
		SqlRequest sqlRequest = Sql.request("SelectAgendaTransferencias", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR cbu_destino = ? OR nro_cuenta_destino = ? OR nro_cuenta_destino = ?)";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(cbu);
		sqlRequest.parametros.add(cuenta);
		sqlRequest.parametros.add(cbu);
		sqlRequest.parametros.add(cuenta);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return false;
		}
		return sqlResponse.registros.size() > 0;
	}

	public static Boolean agendada(ContextoHB contexto, String cuenta) {
		SqlRequest sqlRequest = Sql.request("SelectAgendaTransferencias", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(cuenta);
		sqlRequest.parametros.add(cuenta);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return false;
		}
		return sqlResponse.registros.size() > 0;
	}

	public static List<Objeto> obtenerContador(String idCobis, String tipo, String inicio) {
		SqlRequest sqlRequest = Sql.request("ObtenerContador", "homebanking");
		StringBuilder str = new StringBuilder();
		str.append("SELECT [id],[idCobis],[tipo],[momento] FROM [homebanking].[dbo].[contador] WHERE idCobis = ? ");
		if (Objects.nonNull(tipo)) {
			str.append("AND tipo in ( " + tipo + " ) ");
		}
		if (Objects.nonNull(inicio)) {
			str.append("AND momento > ? ");
		}
		str.append("ORDER BY momento DESC");
		System.out.println("Query:" + str.toString());
		sqlRequest.sql = str.toString();
		sqlRequest.add(idCobis);
		if (Objects.nonNull(inicio)) {
			sqlRequest.add(inicio);
		}
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return null;
		}
		return sqlResponse.registros;
	}

	public static Boolean cambioDetectadoParaNormativoPPV2(ContextoHB contexto, Boolean eliminarCache) {
		Boolean llamadoInterno = contexto.parametros.bool("llamadoInterno", true);
		// bloquear en determinados dias y horarios la funcionalidad, emula la
		// validacion de un dato sensible
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude") && llamadoInterno)
			if (Util.fueraHorarioFraudes(contexto)) {
				contexto.responseHeader("x-path", "A");
				return true;
			}

		if (contexto.sesion.cambioDetectadoPPChequeado) {
			contexto.responseHeader("x-path", "B");
			return contexto.sesion.cambioDetectadoPP;
		}

		try {
			// chequeo 90 dias
			if (bloqueado90dias(contexto)) {
				contexto.sesion.cambioDetectadoPPChequeado = (true);
				contexto.sesion.cambioDetectadoPP = (true);
				contexto.responseHeader("x-path", "C");
				return true;
			}

			if (eliminarCache) {
				Api.eliminarCache(contexto, "Email", contexto.idCobis());
				Api.eliminarCache(contexto, "Telefono", contexto.idCobis());
			}

			if (!SqlRegistroDispositivo.obtenerRegistroDispositivoUltimas24hsPorCobis(contexto.idCobis()).registros.isEmpty()) {
				contexto.responseHeader("x-path", "D");
				return true;
			}

		} catch (Exception e) {
			//
		}

		contexto.responseHeader("x-path", "L");
		return false;
	}

	public static Boolean bloqueado90dias(ContextoHB contexto) {
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaActual = new SimpleDateFormat("yyyy-MM-dd").parse(sdf.format(new Date()));
			Date fechaFinBloqueo = new SimpleDateFormat("yyyy-MM-dd").parse("2023-06-27");

			Objeto sql = SqlHomebanking.getTipoMuestreo(contexto.idCobis(), "habilitar.normativo.execpcion");
			if (sql == null) {
				return false;
			}

			if (sql.string("m_valor").equals("false")) {
				if (fechaActual.after(fechaFinBloqueo)) {
					return false;
				} else {
					return true;
				}
			}

			if (sql.string("m_valor").equals("true")) {
				return false;
			}

			Date fechaFinBloqueoVariable = new SimpleDateFormat("yyyy-MM-dd").parse(sql.string("m_valor"));

			if (fechaActual.after(fechaFinBloqueoVariable)) {
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			//
		}
		return false;
	}

	public static boolean modificoDatosPrestamoPersonal(ContextoHB contexto) {
		return obtenerContador(contexto.idCobis(), ConfigHB.string("mitigante_no_permitido_pp", ""), LocalDateTime.now().plusHours(-1 * ConfigHB.integer("mitigante_dias_no_permitido_pp", 2) * 24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).size() != 0;
	}
}
