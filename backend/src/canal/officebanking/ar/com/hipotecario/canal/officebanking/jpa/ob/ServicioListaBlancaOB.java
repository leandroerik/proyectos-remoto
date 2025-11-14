package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ListaBlancaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ListaBlancaOBRepositorio;

public class ServicioListaBlancaOB extends ServicioOB {

	private ListaBlancaOBRepositorio repo;

	public ServicioListaBlancaOB(ContextoOB contexto) {
		super(contexto);
		repo = new ListaBlancaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<ListaBlancaOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<ListaBlancaOB> findByCuil(Long cuil) {
		return futuro(() -> repo.findByFieldUnique("cuil", cuil));
	}

}