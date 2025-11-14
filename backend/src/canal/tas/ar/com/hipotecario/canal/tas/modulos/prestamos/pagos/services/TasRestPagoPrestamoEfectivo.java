package ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.TasPagoPrestamoEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasRestDepositos;

public class TasRestPagoPrestamoEfectivo {

	public static Objeto postPagoPrestamoEfectivo(ContextoTAS contexto, TasPagoPrestamoEfectivo deposito) {
		return TasRestDepositos.postPagoPrestamoEfectivo(contexto, deposito);
	}

	public static Objeto patchPagoPrestamoEfectivoReversa(ContextoTAS contexto, TasPagoPrestamoEfectivo deposito,
			String idReversa) {
		return TasRestDepositos.patchPagoPrestamoEfectivoReversa(contexto, deposito, idReversa);
	}

}
