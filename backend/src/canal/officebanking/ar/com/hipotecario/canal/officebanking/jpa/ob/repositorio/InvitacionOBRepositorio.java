package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class InvitacionOBRepositorio extends RepositorioGenericoImpl<InvitacionOB> {

	public InvitacionOB enviada(Long numeroDocumento, EmpresaOB empresa) {

		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionOB.class);
			Root<InvitacionOB> itemRoot = criteriaQuery.from(InvitacionOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("numeroDocumento"), numeroDocumento);
			Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("empresa"), empresa);

			Predicate predicateEstadoEnviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.ENVIADA);
			Predicate predicateEstadoReenviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.REENVIADA);
			Predicate predicateEstado = criteriaBuilder.or(predicateEstadoEnviada, predicateEstadoReenviada);

			predicate = criteriaBuilder.and(predicate, predicateEmp, predicateEstado);
			criteriaQuery.where(predicate);

			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}

	}

	public InvitacionOB findByToken(Long numeroDocumento, String token) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionOB.class);
			Root<InvitacionOB> itemRoot = criteriaQuery.from(InvitacionOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("numeroDocumento"), numeroDocumento);
			Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("token"), token);
			predicate = criteriaBuilder.and(predicate, predicateEmp);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}
	}

}