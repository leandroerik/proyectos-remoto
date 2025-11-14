package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.DepositosDestinoCuenta;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.DepositosDestinoPrestamo;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models.DepositosDestinoTarjeta;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.ResponseInsertValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasSqlDepositos;

public class TasSqlDepositoEfectivo {

	public static Objeto getPrecintosLotes(Contexto contexto, String idTas) {
		return TasSqlDepositos.getPrecintosLotes(contexto, idTas);
	}

	// 1- Ejecutar SP de inicializacion de orden deposito: [altaDeposito]
	public static Map<String, Object> saveOrdenDeposito(Contexto contexto, DepositoCuentaDTO deposito) {
		return TasSqlDepositos.saveOrdenDeposito(contexto, deposito);
	}

	public static boolean deleteOrdenDeposito(Contexto contexto, Long depositoId, String numeroTicket) {
		return TasSqlDepositos.deleteOrdenDeposito(contexto, depositoId, numeroTicket);
	}

	// 2- Guardar en TASKioscosDepositosDestinoCuenta
	public static boolean saveTASKioscosDepositosDestinoCuenta(Contexto contexto,
			DepositosDestinoCuenta depoDestinoCuenta) {

		try {
			String sql = "";
			sql += "INSERT INTO [dbo].[TASKioscosDepositosDestinoCuenta]";

			Integer result = Sql.insertGenerico(contexto, "hipotecariotas", sql, depoDestinoCuenta);
			return result == 1 || result.equals(1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	// 2- Guardar en TASKioscosDepositosDestinoCuenta
	public static boolean saveTASKioscosDepositosDestinoTarjeta(Contexto contexto,
			DepositosDestinoTarjeta depoDestinoTarjeta) {

		try {
			String sql = "";
			sql += "INSERT INTO [dbo].[TASKioscosDepositosDestinoTarjeta]";

			Integer result = Sql.insertGenerico(contexto, "hipotecariotas", sql, depoDestinoTarjeta);
			return result == 1 || result.equals(1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	// 2- Guardar en TASKioscosDepositosDestinoCuenta
		public static boolean saveTASKioscosDepositosDestinoPrestamo(Contexto contexto,
				DepositosDestinoPrestamo depoDestinoPrest) {

			try {
				String sql = "";
				sql += "INSERT INTO [dbo].[TASKioscosDepositosDestinoPrestamo]";

				Integer result = Sql.insertGenerico(contexto, "hipotecariotas", sql, depoDestinoPrest);
				return result == 1 || result.equals(1);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return false;
			}
		}

	// 3- Recuperar deposito por num tkt
	public static DepositoCuentaDTO getDepositoByNumeroTicket(Contexto contexto, String numeroTicket) {
		return TasSqlDepositos.getDepositoByNumeroTicket(contexto, numeroTicket);
	}

	// 4- Insertar en la tabla [TASKioscosDepositosValores] los valores que recibo
	public static ResponseInsertValores insertValores(Contexto contexto, List<DepositoValores> list) {
		return TasSqlDepositos.insertValores(contexto, list);
	}

	// 3- Update del deposito [TASKioscosDepositos]
	public static boolean updateImporteTotalEfectivo(Contexto contexto, String numeroTicket, BigDecimal total) {
		return TasSqlDepositos.updateImporteTotalEfectivo(contexto, numeroTicket, total);
	}

	// Auditoria - Update depositos a reversado
	public static Objeto updateDepositoToReversado(ContextoTAS contexto, int kioscoId){
		return TasSqlDepositos.updateDepositoToReversado(contexto, kioscoId);
	}

}