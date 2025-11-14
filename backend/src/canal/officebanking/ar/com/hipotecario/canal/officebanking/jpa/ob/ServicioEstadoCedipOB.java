package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoCedipOBRepositorio;

public class ServicioEstadoCedipOB extends ServicioOB {

	private EstadoCedipOBRepositorio repo;

	public ServicioEstadoCedipOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoCedipOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoCedipOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<EstadoCedipOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

}