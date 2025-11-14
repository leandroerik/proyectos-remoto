package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.HistorialBatchTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialBatchTrnOBRepositorio;

public class ServicioHistorialBatchTrnOB extends ServicioOB {

	private HistorialBatchTrnOBRepositorio repo;

	public ServicioHistorialBatchTrnOB(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialBatchTrnOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialBatchTrnOB> crear(TransferenciaOB transferencia, EstadoTRNOB estadoInicial, EstadoTRNOB estadoFinal) {
		HistorialBatchTrnOB historial = new HistorialBatchTrnOB();
		historial.transferencia = transferencia;
		historial.cuentaDestino = transferencia.credito.nroCuenta;
		historial.cuentaOrigen = transferencia.debito.nroCuenta;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;
		historial.moneda = transferencia.moneda;
		historial.monto = transferencia.monto;
		historial.tipoCuentaOrigen = transferencia.debito.tipoCuenta;
		historial.tipoCuentaDestino = transferencia.credito.tipoCuenta;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<List<HistorialBatchTrnOB>> buscar(TransferenciaOB transferencia) {
		return futuro(() -> repo.buscar(transferencia));
	}

}