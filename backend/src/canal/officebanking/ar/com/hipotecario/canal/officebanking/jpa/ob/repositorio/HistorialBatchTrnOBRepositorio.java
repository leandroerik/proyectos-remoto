package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.HistorialBatchTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class HistorialBatchTrnOBRepositorio extends RepositorioGenericoImpl<HistorialBatchTrnOB> {

	public List<HistorialBatchTrnOB> buscar(TransferenciaOB transferencia) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<HistorialBatchTrnOB> typedQuery = em.createNamedQuery("HistorialBatchTrnOB.buscarPorTrn", HistorialBatchTrnOB.class);
			typedQuery.setParameter("idTransferencia", transferencia.id);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

}
