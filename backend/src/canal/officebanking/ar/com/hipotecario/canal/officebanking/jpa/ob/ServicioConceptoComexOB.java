package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.CategoriaComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ConceptoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ConceptoComexOBRepositorio;

public class ServicioConceptoComexOB extends ServicioOB{

	private ConceptoComexOBRepositorio repo;

	public ServicioConceptoComexOB(ContextoOB contexto) {
		super(contexto);
		repo = new ConceptoComexOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
			
	public Futuro<List<ConceptoComexOB>> findByCategoria(CategoriaComexOB categoria){
		return futuro(() -> repo.findByField("categoria", categoria));
	}
	
	public Futuro<ConceptoComexOB> findById(Integer id) {
		return futuro(() -> repo.find(id));
	}
}
