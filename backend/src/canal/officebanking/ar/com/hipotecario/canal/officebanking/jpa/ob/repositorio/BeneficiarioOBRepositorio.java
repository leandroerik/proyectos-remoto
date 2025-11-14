package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoBeneficiarioOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class BeneficiarioOBRepositorio extends RepositorioGenericoImpl<BeneficiarioOB> {

	public BeneficiarioOB cbu(EmpresaOB empresa, String cbu) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<BeneficiarioOB> criteriaQuery = criteriaBuilder.createQuery(BeneficiarioOB.class);
			Root<BeneficiarioOB> itemRoot = criteriaQuery.from(BeneficiarioOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("empresa"), empresa);
			Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("cbu"), cbu);
			predicate = criteriaBuilder.and(predicate, predicateEmp);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}
	}

	public List<BeneficiarioOB> beneficiario(EmpresaOB empresa, TipoBeneficiarioOB tipo) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<BeneficiarioOB> criteriaQuery = criteriaBuilder.createQuery(BeneficiarioOB.class);
			Root<BeneficiarioOB> itemRoot = criteriaQuery.from(BeneficiarioOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("empresa"), empresa);
			Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("tipoBeneficiario"), tipo);
			predicate = criteriaBuilder.and(predicate, predicateEmp);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}

}