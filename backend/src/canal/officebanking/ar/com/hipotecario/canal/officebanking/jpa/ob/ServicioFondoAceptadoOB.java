package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondoAceptadoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.FondoAceptadoOBRepositorio;

public class ServicioFondoAceptadoOB extends ServicioOB {

	private FondoAceptadoOBRepositorio repo;

	public ServicioFondoAceptadoOB(ContextoOB contexto) {
		super(contexto);
		repo = new FondoAceptadoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<FondoAceptadoOB> buscarPorFondoYEmpresa(Integer id, Integer version, EmpresaOB empresa) {
		return futuro(() -> repo.buscarPorFondoYEmpresa(id, version, empresa.emp_codigo));
	}

	public Futuro<FondoAceptadoOB> save(FondoAceptadoOB fondo) {
		return futuro(() -> repo.create(fondo));
	}

}