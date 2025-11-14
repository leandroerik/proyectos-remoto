package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class TipoProductoFirmaOBRepositorio extends RepositorioGenericoImpl<TipoProductoFirmaOB> {

	public List<TipoProductoFirmaOB> findByActivo() {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<TipoProductoFirmaOB> typedQuery = em.createNamedQuery("TipoProductoFirmaOB.listarPorActivo", TipoProductoFirmaOB.class);

			List<TipoProductoFirmaOB> resultList = typedQuery.getResultList();
			return resultList;
		} finally {
			em.close();
		}
	}

	public TipoProductoFirmaOB findByCodigo(Integer codigo) {
		EntityManager em = this.emf.createEntityManager();

		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<TipoProductoFirmaOB> criteriaQuery = criteriaBuilder.createQuery(TipoProductoFirmaOB.class);
			Root<TipoProductoFirmaOB> itemRoot = criteriaQuery.from(TipoProductoFirmaOB.class);

			Predicate predicate = criteriaBuilder.equal(itemRoot.get("codProdFirma"), codigo);
			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}
	}
}