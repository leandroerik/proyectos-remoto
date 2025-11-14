package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoPagoDeHaberesOBRepositorio;

public class ServicioEstadoPagosHaberesOB extends ServicioOB {
	private EstadoPagoDeHaberesOBRepositorio repo;

	public ServicioEstadoPagosHaberesOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoPagoDeHaberesOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoPagosHaberesOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<EstadoPagosHaberesOB>> findAll() {
		return futuro(() -> repo.findAll());
	}
}
