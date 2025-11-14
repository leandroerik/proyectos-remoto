package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionAdministradorOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class InvitacionAdministradorOBRepositorio extends RepositorioGenericoImpl<InvitacionAdministradorOB> {

	public List<InvitacionAdministradorOB> enviada(Long numeroDocumento, EmpresaOB empresa) {

		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
			Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("usu_nro_documento"), numeroDocumento);
			Predicate predicateEmp = criteriaBuilder.equal(itemRoot.get("empresa"), empresa);

			Predicate predicateEstadoEnviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.ENVIADA);
			Predicate predicateEstadoReenviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.REENVIADA);
			Predicate predicateEstado = criteriaBuilder.or(predicateEstadoEnviada, predicateEstadoReenviada);

			predicate = criteriaBuilder.and(predicate, predicateEmp, predicateEstado);
			criteriaQuery.where(predicate);

			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}

	}

	public InvitacionAdministradorOB findByDNI(Long numeroDocumento) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
			Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("usu_nro_documento"), numeroDocumento);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}
	}
	
	public List<InvitacionAdministradorOB> findByDNIEstado(Long numeroDocumento) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
			Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("usu_nro_documento"), numeroDocumento);
			Predicate predicateEstados = criteriaBuilder.or(
					criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.ENVIADA),
					criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.REENVIADA),
					criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.TRANSMIT)
					);
			predicate = criteriaBuilder.and(predicate, predicateEstados);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<InvitacionAdministradorOB> findByCuilEstado(Long cuil) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
			Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

			Predicate predicatecuil = criteriaBuilder.equal(itemRoot.get("usu_cuil"), cuil);
			Predicate predicateEstadoEnviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.ENVIADA);
			Predicate predicateEstadoReenviada = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.REENVIADA);
			Predicate predicateEstado = criteriaBuilder.or(predicateEstadoEnviada, predicateEstadoReenviada);
			Predicate predicate = criteriaBuilder.and(predicatecuil, predicateEstado);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<InvitacionAdministradorOB> findInvitacionesPorVincularDNI(Long numeroDocumento) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
			Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("usu_nro_documento"), numeroDocumento);
			Predicate predicateEstadoVincularPend = criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.VINCULACION_PEND);
			predicate = criteriaBuilder.and(predicate, predicateEstadoVincularPend );
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}
		public List<InvitacionAdministradorOB> findInvitacionDNIPendiente(Long numeroDocumento) {
			EntityManager em = this.emf.createEntityManager();
			try {
				CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
				CriteriaQuery<InvitacionAdministradorOB> criteriaQuery = criteriaBuilder.createQuery(InvitacionAdministradorOB.class);
				Root<InvitacionAdministradorOB> itemRoot = criteriaQuery.from(InvitacionAdministradorOB.class);

				Predicate predicate = criteriaBuilder.equal(itemRoot.get("usu_nro_documento"), numeroDocumento);
				Predicate predicateEstadoEnviada = criteriaBuilder.or(
						criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.ENVIADA),
						criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.REENVIADA),
						criteriaBuilder.equal(itemRoot.get("estado"), EnumEstadoInvitacionOB.TRANSMIT)
						);
				predicate = criteriaBuilder.and(predicate, predicateEstadoEnviada);
				criteriaQuery.where(predicate);
				return em.createQuery(criteriaQuery).getResultList();
			} finally {
				em.close();
			}
	}

}