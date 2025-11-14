package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDate;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EjecucionBatchOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class EjecucionBatchOBRepositorio extends RepositorioGenericoImpl<EjecucionBatchOB> {

	public EjecucionBatchOB findByNombreyFecha(String nombreCron, LocalDate fecha) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EjecucionBatchOB> typedQuery = em.createNamedQuery("EjecucionBatchOB.buscaPorCron", EjecucionBatchOB.class);
			typedQuery.setParameter("cron", nombreCron);
			typedQuery.setParameter("fecha_ejecucion", fecha);

			List<EjecucionBatchOB> results = null;
			results = typedQuery.getResultList();
			return results.isEmpty() ? null : results.get(0);
		} finally {
			em.close();
		}
	}
}