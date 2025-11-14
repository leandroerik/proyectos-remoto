package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualAutorizanteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ControlDualAutorizanteOBRepositorio;

public class ServicioControlDualAutorizanteOB extends ServicioOB {

	private ControlDualAutorizanteOBRepositorio repo;
	
	public ServicioControlDualAutorizanteOB(ContextoOB contexto) {
		super(contexto);
		repo = new ControlDualAutorizanteOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<List<ControlDualAutorizanteOB>> findAutorizantesActivos(EmpresaOB empresa) {
		return futuro(() -> repo.findAutorizantesActivos(empresa));
	}
	
	public Futuro<ControlDualAutorizanteOB> findAutorizantePorEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
		return futuro(() -> repo.findAutorizantePorEmpresa(usuario, empresa));
	}
}
