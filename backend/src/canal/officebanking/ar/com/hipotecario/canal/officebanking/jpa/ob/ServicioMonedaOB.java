package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.MonedaOBRepositorio;

public class ServicioMonedaOB extends ServicioOB {

	private MonedaOBRepositorio repo;

	public ServicioMonedaOB(ContextoOB contexto) {
		super(contexto);
		repo = new MonedaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<MonedaOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<MonedaOB> findBySimbol(String simbolo) {
		return futuro(() -> repo.findByFieldUnique("simbolo", simbolo));
	}

	public Futuro<List<MonedaOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

}