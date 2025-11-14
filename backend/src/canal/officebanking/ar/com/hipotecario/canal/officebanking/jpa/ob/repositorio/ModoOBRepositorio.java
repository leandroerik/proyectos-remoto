package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ModoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ModoOBRepositorio extends RepositorioGenericoImpl<ModoOB> {

	public ModoOB findByEmpCodigo(int empCodigo) {
		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<ModoOB> criteriaQuery = criteriaBuilder.createQuery(ModoOB.class);
			Root<ModoOB> itemRoot = criteriaQuery.from(ModoOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("empCodigo"), empCodigo);
			
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} catch (NoResultException e) {
			return null; // Devuelve null si no encuentra un resultado
		} finally {
			em.close();
		}
	}

}