package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.CuentaOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CuentaOperadorOBRepositorio;

public class ServicioCuentaOperadorOB extends ServicioOB {

	private CuentaOperadorOBRepositorio repo;

	public ServicioCuentaOperadorOB(ContextoOB contexto) {
		super(contexto);
		repo = new CuentaOperadorOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<CuentaOperadorOB>> permisos() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<List<CuentaOperadorOB>> findByEmpresaUsuario(EmpresaUsuarioOB empresaUsuarioOB) {
		return futuro(() -> repo.findByField("empresaUsuario", empresaUsuarioOB));
	}

	public Futuro<Integer> eliminarCuenta(Integer idCuentaOperador) {
		return futuro(() -> repo.eliminarCuenta(idCuentaOperador));
	}
}