package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoSolicitudOBRepositorio;

public class ServicioEstadoInversionOB extends ServicioOB {

	private EstadoSolicitudOBRepositorio repo;

	public ServicioEstadoInversionOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoSolicitudOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoSolicitudInversionOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}