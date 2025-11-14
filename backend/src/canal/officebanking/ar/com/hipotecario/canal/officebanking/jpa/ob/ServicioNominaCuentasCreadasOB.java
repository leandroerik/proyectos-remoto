package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaCuentasCreadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.NominaCuentasCreadasOBRepositorio;

public class ServicioNominaCuentasCreadasOB extends ServicioOB {

	private NominaCuentasCreadasOBRepositorio repo;

	public ServicioNominaCuentasCreadasOB(ContextoOB contexto) {
		super(contexto);
		repo = new NominaCuentasCreadasOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<NominaCuentasCreadasOB> crear(NominaCuentasCreadasOB cuentaCreada) {
		return futuro(() -> repo.create(cuentaCreada));
	}

	public Futuro<List<NominaCuentasCreadasOB>> buscarPorIdOperacion(PagoDeHaberesOB nomina) {
		return futuro(() -> repo.buscarPorIdOperacion(nomina));
	}

}
