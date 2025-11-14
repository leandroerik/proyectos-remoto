package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoCamaraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TipoCamaraOBRepositorio;

public class ServicioTipoCamaraOB extends ServicioOB {

	private TipoCamaraOBRepositorio repo;

	public ServicioTipoCamaraOB(ContextoOB contexto) {
		super(contexto);
		repo = new TipoCamaraOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<TipoCamaraOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<TipoCamaraOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

}