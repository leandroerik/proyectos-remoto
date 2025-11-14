package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaCuentasCreadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class NominaCuentasCreadasOBRepositorio extends RepositorioGenericoImpl<NominaCuentasCreadasOB> {

	public List<NominaCuentasCreadasOB> buscarPorIdOperacion(PagoDeHaberesOB nomina) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<NominaCuentasCreadasOB> typedQuery = em.createNamedQuery("NominaCuentasCreadasOB.buscarPorIdOperacion", NominaCuentasCreadasOB.class);
			typedQuery.setParameter("nomina", nomina);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
