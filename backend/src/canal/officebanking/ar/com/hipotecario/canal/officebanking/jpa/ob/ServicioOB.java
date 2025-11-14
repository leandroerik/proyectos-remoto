package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import jakarta.persistence.EntityManagerFactory;

public class ServicioOB extends Base {

	private static final String BASE_OFFICE_BANKING = "hb_be";

	private EntityManagerFactory emf = null;

	public ServicioOB(ContextoOB contexto) {
		this.emf = contexto.entityMangerFactory(BASE_OFFICE_BANKING);
	}

	public EntityManagerFactory getEntityManager() {
		return emf;
	}

	public void setEntityManager(EntityManagerFactory entityManager) {
		this.emf = entityManager;
	}

}