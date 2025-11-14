package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoBandejaOBRepositorio;

public class ServicioEstadoBandejaOB extends ServicioOB {

	private EstadoBandejaOBRepositorio repo;

	public ServicioEstadoBandejaOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoBandejaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoBandejaOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<EstadoBandejaOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

}
