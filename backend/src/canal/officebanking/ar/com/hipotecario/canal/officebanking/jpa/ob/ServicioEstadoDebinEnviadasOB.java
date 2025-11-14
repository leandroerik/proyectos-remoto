package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoDebinEnviadasOBRepositorio;

public class ServicioEstadoDebinEnviadasOB extends ServicioOB {

	private EstadoDebinEnviadasOBRepositorio repo;

	public ServicioEstadoDebinEnviadasOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoDebinEnviadasOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<EstadoDebinEnviadasOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}
