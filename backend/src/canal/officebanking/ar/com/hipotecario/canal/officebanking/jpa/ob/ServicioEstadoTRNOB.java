package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoTransferenciaOBRepositorio;

public class ServicioEstadoTRNOB extends ServicioOB {

	private EstadoTransferenciaOBRepositorio repo;

	public ServicioEstadoTRNOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoTransferenciaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<EstadoTRNOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<EstadoTRNOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

}