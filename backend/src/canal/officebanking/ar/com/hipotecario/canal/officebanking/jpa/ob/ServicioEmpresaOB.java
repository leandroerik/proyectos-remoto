package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EmpresaOBRepositorio;

public class ServicioEmpresaOB extends ServicioOB {

	private EmpresaOBRepositorio repo;

	public ServicioEmpresaOB(ContextoOB contexto) {
		super(contexto);
		repo = new EmpresaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<EmpresaOB>> empresas() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<EmpresaOB> findByCuit(Long cuit, String idCobis) {
		return futuro(() -> repo.findByCuit(cuit, idCobis));
	}
	public Futuro<EmpresaOB> findByCuit(Long cuit) {
		return futuro(() -> repo.findByCuit(cuit));
	}

	public Futuro<EmpresaOB> create(EmpresaOB empresa) {
		return futuro(() -> repo.create(empresa));
	}
	
	public Futuro<EmpresaOB> actualizarEmpresa(EmpresaOB empresaOB) {
		return futuro(() -> repo.update(empresaOB));
	}

}