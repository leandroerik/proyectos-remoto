package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoDebinRecibidasOBRepositorio;

public class ServicioEstadoDebinRecibidasOB extends ServicioOB {
	
	private EstadoDebinRecibidasOBRepositorio repo;

	public ServicioEstadoDebinRecibidasOB(ContextoOB contexto) {
		super(contexto);
		repo = new EstadoDebinRecibidasOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<EstadoDebinRecibidasOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}
