package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.CreditoTranfOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CreditoTranfOBRepositorio;

public class ServicioCreditoTranfOB extends ServicioOB {

	private CreditoTranfOBRepositorio repo;

	public ServicioCreditoTranfOB(ContextoOB contexto) {
		super(contexto);
		repo = new CreditoTranfOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<CreditoTranfOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}