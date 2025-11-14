package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ClaveUsuarioOBRepositorio;

public class ServicioClaveUsuarioOB extends ServicioOB {

	private ClaveUsuarioOBRepositorio repo;

	public ServicioClaveUsuarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new ClaveUsuarioOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<ClaveUsuarioOB> create(ClaveUsuarioOB usuarioOB) {
		return futuro(() -> repo.create(usuarioOB));
	}
		
	public Futuro<List<ClaveUsuarioOB>> findByUsuario(UsuarioOB usuario) {
		return futuro(() -> repo.findByUsuario(usuario));
	}
}