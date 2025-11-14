package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PermisoOperadorOBRepositorio;

public class ServicioPermisoOperadorOB extends ServicioOB {

	private PermisoOperadorOBRepositorio repo;

	public ServicioPermisoOperadorOB(ContextoOB contexto) {
		super(contexto);
		repo = new PermisoOperadorOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<PermisoOperadorOB>> permisos() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<PermisoOperadorOB> insert(PermisoOperadorOB permisoOperador) {
		return futuro(() -> repo.create(permisoOperador));
	}

	public Futuro<List<PermisoOperadorOB>> findByEmpresaUsuario(EmpresaUsuarioOB empresaUsuarioOB) {
		return futuro(() -> repo.findByField("empresaUsuario", empresaUsuarioOB));
	}

	public Futuro<PermisoOperadorOB> update(PermisoOperadorOB permisoOperador) {
		return futuro(() -> repo.update(permisoOperador));
	}

	public Futuro<PermisoOperadorOB> buscarPermiso(EmpresaUsuarioOB empresaUsuarioOB, PermisoOB permisoOperador) {
		return futuro(() -> repo.buscarPermiso(empresaUsuarioOB, permisoOperador));
	}

	public Futuro<Integer> eliminarPermiso(Integer idPermisoOperador) {
		return futuro(() -> repo.eliminarPermiso(idPermisoOperador));
	}

}