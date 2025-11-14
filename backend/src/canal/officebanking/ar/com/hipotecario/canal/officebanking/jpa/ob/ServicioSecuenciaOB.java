package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.SecuenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.SecuenciaOBRepositorio;

public class ServicioSecuenciaOB extends ServicioOB {

	private SecuenciaOBRepositorio repo;

	public ServicioSecuenciaOB(ContextoOB contexto) {
		super(contexto);
		repo = new SecuenciaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<SecuenciaOB> create(SecuenciaOB secuencia) {
		return futuro(() -> repo.create(secuencia));
	}

}