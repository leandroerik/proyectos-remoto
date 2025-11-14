package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoUsuarioOBRepositorio;

public class ServicioEstadoUsuarioOB extends ServicioOB {

	private EstadoUsuarioOBRepositorio repo;

	public ServicioEstadoUsuarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoUsuarioOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoUsuarioOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

}