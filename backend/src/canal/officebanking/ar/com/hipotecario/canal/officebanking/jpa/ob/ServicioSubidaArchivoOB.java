package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.SubidaArchivoOBRepositorio;

public class ServicioSubidaArchivoOB extends ServicioOB {

	private SubidaArchivoOBRepositorio repo;

	public ServicioSubidaArchivoOB(ContextoOB contexto) {
		super(contexto);
		repo = new SubidaArchivoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<PagoDeHaberesOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<PagoDeHaberesOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}
}
