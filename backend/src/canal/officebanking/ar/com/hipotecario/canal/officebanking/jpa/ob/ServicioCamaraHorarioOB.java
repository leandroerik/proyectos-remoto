package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.HorarioCamaraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HorarioCamaraOBRepositorio;

public class ServicioCamaraHorarioOB extends ServicioOB {

	private HorarioCamaraOBRepositorio repo;

	public ServicioCamaraHorarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new HorarioCamaraOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<HorarioCamaraOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<HorarioCamaraOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}