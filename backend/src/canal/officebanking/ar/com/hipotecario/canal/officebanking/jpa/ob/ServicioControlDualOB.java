package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ControlDualOBRepositorio;

public class ServicioControlDualOB extends ServicioOB {
	
	private ControlDualOBRepositorio repo;
	
	public ServicioControlDualOB(ContextoOB contexto) {
		super(contexto);
		repo = new ControlDualOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
		
	public  Futuro<ControlDualOB> findByEmpresa(EmpresaOB empresaOB) {
		return futuro(() -> repo.findByFieldUnique("empresa", empresaOB));
	}

}
