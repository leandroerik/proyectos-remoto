package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoInvitacionOBRepositorio;

public class ServicioEstadoInvitacionOB extends ServicioOB {

	private EstadoInvitacionOBRepositorio repo;

	public ServicioEstadoInvitacionOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoInvitacionOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EnumEstadoInvitacionOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

}