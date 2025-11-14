package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BancoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.BancoOBRepositorio;

public class ServicioBancoOB extends ServicioOB {

	private BancoOBRepositorio repo;

	public ServicioBancoOB(ContextoOB contexto) {
		super(contexto);
		repo = new BancoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<BancoOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<BancoOB> create(BancoOB banco) {
		return futuro(() -> repo.create(banco));
	}

}