package ar.com.hipotecario.canal.officebanking.jpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class RepositorioGenericoImpl<T> implements RepositorioGenerico<T> {

	protected EntityManagerFactory emf;
	private Class<T> type;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RepositorioGenericoImpl() {
		Type t = getClass().getGenericSuperclass();
		ParameterizedType pt = (ParameterizedType) t;
		type = (Class) pt.getActualTypeArguments()[0];
	}

	public EntityManagerFactory getEntityManager() {
		return emf;
	}

	@PersistenceContext
	public void setEntityManager(EntityManagerFactory entityManager) {
		this.emf = entityManager;
	}

	public T create(final T t) {
		EntityManager em = this.emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(t);
			em.getTransaction().commit();
			return t;
		} finally {
			em.close();
		}
	}

	public void delete(final Object objeto) {
		EntityManager em = this.emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.remove(em.merge(objeto));
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public T find(final Object id) {
		EntityManager em = this.emf.createEntityManager();
		try {
			return (T) em.find(type, id);
		} finally {
			em.close();
		}
	}

	public T update(final T t) {
		EntityManager em = this.emf.createEntityManager();
		try {
			em.getTransaction().begin();
			T result = em.merge(t);
			em.getTransaction().commit();
			return result;
		} finally {
			em.close();
		}
	}

	public List<T> saveAll(final List<T> list) {
		EntityManager em = this.emf.createEntityManager();
		try {
			em.getTransaction().begin();
			for (T obj : list) {
				em.merge(obj);
			}
			em.getTransaction().commit();
			return list;
		} catch (Exception e) {
			em.getTransaction().rollback();
			return null;
		} finally {
			em.close();
		}
	}

	public List<T> findAll() {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = cb.createQuery(type);
			criteriaQuery.select(criteriaQuery.from(type));
			TypedQuery<T> query = em.createQuery(criteriaQuery);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public T findByFieldUnique(String field, Object value) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaQuery<T> criteriaQuery = findByField(field, value, em);
			return em.createQuery(criteriaQuery).getSingleResult();
		} finally {
			em.close();
		}
	}

	public List<T> findByField(String field, Object value) {
		EntityManager em = this.emf.createEntityManager();
		try {
			CriteriaQuery<T> criteriaQuery = findByField(field, value, em);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}

	private CriteriaQuery<T> findByField(String field, Object value, EntityManager em) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
		Root<T> itemRoot = criteriaQuery.from(type);
		Predicate predicate = criteriaBuilder.equal(itemRoot.get(field), value);
		criteriaQuery.where(predicate);
		return criteriaQuery;
	}

}