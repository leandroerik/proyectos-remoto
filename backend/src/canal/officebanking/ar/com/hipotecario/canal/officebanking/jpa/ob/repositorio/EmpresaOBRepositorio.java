package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EmpresaOBRepositorio extends RepositorioGenericoImpl<EmpresaOB> {

	public EmpresaOB findByCuit(Long cuit, String idCobis) {
		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<EmpresaOB> criteriaQuery = criteriaBuilder.createQuery(EmpresaOB.class);
			Root<EmpresaOB> itemRoot = criteriaQuery.from(EmpresaOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("cuit"), cuit);
			if (idCobis != null) {
				Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("idCobis"), idCobis);
				predicate = criteriaBuilder.and(predicate, predicateEmp);
			}
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} catch (NoResultException e) {
			return null; // Devuelve null si no encuentra un resultado
		} finally {
			em.close();
		}
	}

	public EmpresaOB findByCuit(Long cuit) {
		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<EmpresaOB> criteriaQuery = criteriaBuilder.createQuery(EmpresaOB.class);
			Root<EmpresaOB> itemRoot = criteriaQuery.from(EmpresaOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("cuit"), cuit);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} catch (NoResultException e) {
			return null; // Devuelve null si no encuentra un resultado
		} finally {
			em.close();
		}
	}

}