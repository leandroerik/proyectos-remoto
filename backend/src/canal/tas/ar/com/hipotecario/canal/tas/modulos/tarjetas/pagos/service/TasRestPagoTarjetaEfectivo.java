package ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.service;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models.TasPagoTarjetaEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasRestDepositos;

public class TasRestPagoTarjetaEfectivo {

	public static Objeto postPagoTarjetaEfectivo(ContextoTAS contexto, TasPagoTarjetaEfectivo deposito) {
		return TasRestDepositos.postPagoTarjetaEfectivo(contexto, deposito);
	}

	public static Objeto patchPagoTarjetaEfectivoReversa(ContextoTAS contexto, TasPagoTarjetaEfectivo deposito,
			String idReversa) {
		return TasRestDepositos.patchPagoTarjetaEfectivoReversa(contexto, deposito, idReversa);
	}

}
