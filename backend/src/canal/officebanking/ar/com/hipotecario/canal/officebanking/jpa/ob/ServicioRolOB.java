package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.RolOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.RolOBRepositorio;

public class ServicioRolOB extends ServicioOB {

	private RolOBRepositorio repo;

	public ServicioRolOB(ContextoOB contexto) {
		super(contexto);
		repo = new RolOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<RolOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<RolOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}