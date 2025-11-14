package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDate;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EjecucionBatchOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EjecucionBatchOBRepositorio;

public class ServicioEjecucionBatchOB extends ServicioOB {

	private EjecucionBatchOBRepositorio repo;

	public ServicioEjecucionBatchOB(ContextoOB contexto) {
		super(contexto);
		repo = new EjecucionBatchOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EjecucionBatchOB> create(EjecucionBatchOB batch) {
		return futuro(() -> repo.create(batch));
	}

	public Futuro<EjecucionBatchOB> update(EjecucionBatchOB batch) {
		return futuro(() -> repo.update(batch));
	}

	public Futuro<EjecucionBatchOB> buscarPorCron(String nombre, LocalDate fecha) {
		return futuro(() -> repo.findByNombreyFecha(nombre, fecha));

	}

}