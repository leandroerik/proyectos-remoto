package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.ConceptoDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ConceptoDebinOBRepositorio;

public class ServicioConceptoDebinOB extends ServicioOB{

	private ConceptoDebinOBRepositorio repo;

	public ServicioConceptoDebinOB(ContextoOB contexto) {
		super(contexto);
		repo = new ConceptoDebinOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<ConceptoDebinOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<ConceptoDebinOB> find(Integer concepto) {
		return futuro(() -> repo.find(concepto));
	}
}
