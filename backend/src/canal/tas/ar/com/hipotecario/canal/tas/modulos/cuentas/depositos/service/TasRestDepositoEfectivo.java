package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasRestDepositos;

public class TasRestDepositoEfectivo {

	public static Objeto postDepositosEfectivo(ContextoTAS contexto, TasDepositoEfectivo deposito) {
		return TasRestDepositos.postDepositosEfectivo(contexto, deposito);
	}

	public static Objeto patchDepositosEfectivoReversa(ContextoTAS contexto, TasDepositoEfectivo deposito,
			String idReversa) {
		return TasRestDepositos.patchDepositosEfectivoReversa(contexto, deposito, idReversa);
	}

}
