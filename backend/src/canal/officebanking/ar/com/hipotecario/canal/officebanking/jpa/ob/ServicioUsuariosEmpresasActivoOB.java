package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuariosEmpresasActivoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.UsuariosEmpresasActivoOBRepositorio;

public class ServicioUsuariosEmpresasActivoOB extends ServicioOB {

	private UsuariosEmpresasActivoOBRepositorio repo;
	
	public ServicioUsuariosEmpresasActivoOB(ContextoOB contexto) {
		super(contexto);
		repo = new UsuariosEmpresasActivoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<UsuariosEmpresasActivoOB> create(UsuariosEmpresasActivoOB empresaUsuarioActivo) {
		return futuro(() -> repo.create(empresaUsuarioActivo));
	}
	
	public Futuro<UsuariosEmpresasActivoOB> update(UsuariosEmpresasActivoOB empresaUsuarioActivo){
		return futuro(() -> repo.update(empresaUsuarioActivo));
	}
	
	public Futuro<UsuariosEmpresasActivoOB> findByUsuarioEmpresaActivo(UsuarioOB usuario, EmpresaOB empresa) {
		return futuro(() -> repo.findByUsuarioEmpresaActivo(usuario, empresa));
	}
}
