package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondoAceptadoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class FondoAceptadoOBRepositorio extends RepositorioGenericoImpl<FondoAceptadoOB> {

	public FondoAceptadoOB buscarPorFondoYEmpresa(Integer id, Integer version, Integer empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<FondoAceptadoOB> typedQuery = em.createNamedQuery("FondoAceptadoOB.buscarPorFondoYEmpresa", FondoAceptadoOB.class);
			typedQuery.setParameter("id", id);
			typedQuery.setParameter("version", version);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			em.close();
		}
	}

}