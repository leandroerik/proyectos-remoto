package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.ErroresArchivosOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ErrorArchivoOBRepositorio extends RepositorioGenericoImpl<ErroresArchivosOB> {
	public List<ErroresArchivosOB> buscarPorIdOperacion(BandejaOB operacion) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ErroresArchivosOB> typedQuery = em.createNamedQuery("ErroresArchivosOB.buscarPorIdOperacion", ErroresArchivosOB.class);
			typedQuery.setParameter("operacion", operacion);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
