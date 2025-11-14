package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.CategoriaComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CategoriaComexOBRepositorio;
import ar.com.hipotecario.backend.base.Futuro;
import java.util.List;

public class ServicioCategoriaComexOB extends ServicioOB{

	private CategoriaComexOBRepositorio repo;
	
	public ServicioCategoriaComexOB(ContextoOB contexto) {
		super(contexto);
		repo = new CategoriaComexOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<CategoriaComexOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
	
	public Futuro<List<CategoriaComexOB>> findAll(){
		return futuro(() -> repo.findAll());
	}
}
