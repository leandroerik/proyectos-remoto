package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.DebitoTranfOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebitoTranfOBRepositorio;

public class ServicioDebitoTranfOB extends ServicioOB {

	private DebitoTranfOBRepositorio repo;

	public ServicioDebitoTranfOB(ContextoOB contexto) {
		super(contexto);
		repo = new DebitoTranfOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<DebitoTranfOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}