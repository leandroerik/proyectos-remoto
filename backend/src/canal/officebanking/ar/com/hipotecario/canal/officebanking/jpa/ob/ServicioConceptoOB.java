package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.ConceptoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ConceptoOBRepositorio;

public class ServicioConceptoOB extends ServicioOB {

	private ConceptoOBRepositorio repo;

	public ServicioConceptoOB(ContextoOB contexto) {
		super(contexto);
		repo = new ConceptoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<ConceptoOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<ConceptoOB> find(Integer concepto) {
		return futuro(() -> repo.find(concepto));
	}

}