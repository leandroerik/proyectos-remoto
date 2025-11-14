package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoComexOBRepositorio;


public class ServicioEstadoComexOB extends ServicioOB {

	private EstadoComexOBRepositorio repo;

	public ServicioEstadoComexOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoComexOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoOPComexOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

	public Futuro<List<EstadoOPComexOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

}

