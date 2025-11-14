package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ModoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ModoOBRepositorio;

public class ServicioModoOB extends ServicioOB {

	private ModoOBRepositorio repo;

	public ServicioModoOB(ContextoOB contexto) {
		super(contexto);
		repo = new ModoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

//	public Futuro<List<EmpresaOB>> empresas() {
//		return futuro(() -> repo.findAll());
//	}

	public Futuro<ModoOB> findByEmpCodigo(int empCodigo) {
		return futuro(() -> repo.findByEmpCodigo(empCodigo));
	}

	public Futuro<ModoOB> create(ModoOB modoOB) {
		return futuro(() -> repo.create(modoOB));
	}
	
	public Futuro<ModoOB> actualizarModo(ModoOB modoOB) {
		return futuro(() -> repo.update(modoOB));
	}

}