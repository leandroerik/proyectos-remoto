package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.AcreditacionesConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.AcreditacionesConfigOBRepositorio;

public class ServicioAcreditacionesConfigOB extends ServicioOB {
	private AcreditacionesConfigOBRepositorio repo;

	public ServicioAcreditacionesConfigOB(ContextoOB contexto) {
		super(contexto);
		repo = new AcreditacionesConfigOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<AcreditacionesConfigOB>> findAll() {
		return futuro(() -> repo.findAll());
	}
}
