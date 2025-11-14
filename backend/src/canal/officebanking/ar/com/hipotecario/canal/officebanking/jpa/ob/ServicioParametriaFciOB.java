package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.ParametriaFciOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ParametriaFCIRepositorio;

public class ServicioParametriaFciOB extends ServicioOB {

	private ParametriaFCIRepositorio repo;

	public ServicioParametriaFciOB(ContextoOB contexto) {
		super(contexto);
		repo = new ParametriaFCIRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<ParametriaFciOB> buscarPorFondoId(Integer id) {
		return futuro(() -> repo.buscarPorFondo(id, "Clase B", "Suscripcion"));
	}
	
	public Futuro<ParametriaFciOB> buscarPorFondoIdYTipo(Integer id, String tipo) {
		return futuro(() -> repo.buscarPorFondo(id, tipo, "Suscripcion"));
	}

	public Futuro<ParametriaFciOB> buscarPorFondoIdTipoYOperacion(Integer id, String tipo, String operacion) {
		return futuro(() -> repo.buscarPorFondoYOperacion(id, tipo, operacion));
	}


}