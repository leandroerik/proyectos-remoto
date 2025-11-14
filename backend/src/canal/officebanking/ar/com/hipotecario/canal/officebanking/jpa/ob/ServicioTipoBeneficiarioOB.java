package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TipoBeneficiarioOBRepositorio;

public class ServicioTipoBeneficiarioOB extends ServicioOB {

	private TipoBeneficiarioOBRepositorio repo;

	public ServicioTipoBeneficiarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new TipoBeneficiarioOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<TipoBeneficiarioOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<TipoBeneficiarioOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}