package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.AccionesOBRepositorio;

public class ServicioAccionesOB extends ServicioOB {

	private AccionesOBRepositorio repo;

	public ServicioAccionesOB(ContextoOB contexto) {
		super(contexto);
		repo = new AccionesOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<AccionesOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}
