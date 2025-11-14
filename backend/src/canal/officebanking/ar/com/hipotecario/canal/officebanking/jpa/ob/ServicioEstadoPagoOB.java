package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoPagoOBRepositorio;

public class ServicioEstadoPagoOB extends ServicioOB {
	private EstadoPagoOBRepositorio repo;

	public ServicioEstadoPagoOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoPagoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoPagoOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<EstadoPagoOB>> findAll() {
		return futuro(() -> repo.findAll());
	}
}
