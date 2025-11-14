package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.NominaConfigOBRepositorio;

public class ServicioNominaConfigOB extends ServicioOB {

	private NominaConfigOBRepositorio repo;

	public ServicioNominaConfigOB(ContextoOB contexto) {
		super(contexto);
		repo = new NominaConfigOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<NominaConfigOB>> findAll() {
		return futuro(() -> repo.findAll());
	}
}
