package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TipoTransferenciaOBRepositorio;

public class ServicioTipoTransferenciaOB extends ServicioOB {

	private TipoTransferenciaOBRepositorio repo;

	public ServicioTipoTransferenciaOB(ContextoOB contexto) {
		super(contexto);
		repo = new TipoTransferenciaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<TipoTransferenciaOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<TipoTransferenciaOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}