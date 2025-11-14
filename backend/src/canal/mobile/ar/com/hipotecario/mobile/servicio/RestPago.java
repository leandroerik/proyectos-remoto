package ar.com.hipotecario.mobile.servicio;

import java.util.Collections;
import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class RestPago {

	/* ========== SERVICIOS ========== */
	public static Boolean migrarDescripcionesAgendaPagos(ContextoMB contexto) {
		if (existenPagoServiciosTDSinMigrar(contexto)) {
			ApiResponseMB tarjetasDebitosConCancelados = TarjetaDebitoService.tarjetaDebito(contexto, false);
			List<Objeto> tarjetasDebito = tarjetasDebitosConCancelados.ordenar("fechaAlta").objetos();
			Collections.reverse(tarjetasDebito);

			for (Objeto tarjetaDebito1 : tarjetasDebito) {
				for (Objeto tarjetaDebito2 : tarjetasDebito) {
					if (!tarjetaDebito1.string("numeroProducto").equals(tarjetaDebito2.string("numeroProducto"))) {
						if (tarjetaDebito1.date("fechaAlta", "yyyy-MM-dd").after(tarjetaDebito2.date("fechaAlta", "yyyy-MM-dd"))) {
							List<Objeto> agenda = agenda(contexto, tarjetaDebito2.string("numeroProducto"));
							for (Objeto item : agenda) {
								agendar(contexto, tarjetaDebito1.string("numeroProducto"), item.string("psp_codigoPago"), item.string("psp_perteneceA"));
							}
						}
					}
				}
				registrarPagoServiciosTDMigrada(contexto, tarjetaDebito1.string("numeroProducto"));
			}
		}
		return true;
	}

	/* ========== UTIL ========== */
	private static List<Objeto> agenda(ContextoMB contexto, String psp_tarjetaDebito) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectAgendaPagosLink", "homebanking");
		sqlRequest.sql = "SELECT [psp_tarjetaDebito], [psp_codigoPago], [psp_perteneceA] ";
		sqlRequest.sql += "FROM [Homebanking].[dbo].[PagoServiciosPerteneceA] ";
		sqlRequest.sql += "WHERE [psp_tarjetaDebito] = ?";
		sqlRequest.add(psp_tarjetaDebito);
		return SqlMB.response(sqlRequest).registros;
	}

	private static Boolean agendar(ContextoMB contexto, String psp_tarjetaDebito, String psp_codigoPago, String psp_perteneceA) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertAgendaPagosLink", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[PagoServiciosPerteneceA] ([psp_tarjetaDebito], [psp_codigoPago], [psp_perteneceA]) ";
			sqlRequest.sql += "VALUES (?, ?, ?)";
			sqlRequest.add(psp_tarjetaDebito);
			sqlRequest.add(psp_codigoPago);
			sqlRequest.add(psp_perteneceA);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static Boolean existenPagoServiciosTDSinMigrar(ContextoMB contexto) {
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			SqlRequestMB sqlRequest = SqlMB.request("SelectPagoServiciosTDMigradas", "homebanking");
			sqlRequest.sql = "SELECT [numeroTarjetaDebito] ";
			sqlRequest.sql += "FROM [homebanking].[dbo].[pagoServiciosTDMigradas] ";
			sqlRequest.sql += "WHERE [numeroTarjetaDebito] = ?";
			sqlRequest.add(tarjetaDebito.numero());
			if (SqlMB.response(sqlRequest).registros.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static Boolean registrarPagoServiciosTDMigrada(ContextoMB contexto, String numeroTarjetaDebito) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertPagoServiciosTDMigradas", "homebanking");
			sqlRequest.sql += "INSERT INTO [homebanking].[dbo].[pagoServiciosTDMigradas] ([numeroTarjetaDebito]) ";
			sqlRequest.sql += "VALUES (?)";
			sqlRequest.add(numeroTarjetaDebito);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
