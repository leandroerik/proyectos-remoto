package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PermisoOBRepositorio;

public class ServicioPermisoOB extends ServicioOB {

	private PermisoOBRepositorio repo;

	public ServicioPermisoOB(ContextoOB contexto) {
		super(contexto);
		repo = new PermisoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<PermisoOB>> permisos() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<PermisoOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

}