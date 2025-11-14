package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class TarjetaVirtualOBRepositorio extends RepositorioGenericoImpl<TarjetaVirtualOB> {

	public List<TarjetaVirtualOB> buscarPorEmpresa(EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<TarjetaVirtualOB> typedQuery = em.createNamedQuery("TarjetaVirtualOB.findByEmpresa", TarjetaVirtualOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

    public List<TarjetaVirtualOB> buscarPorEmpresaAndId(EmpresaOB empresa, Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<TarjetaVirtualOB> typedQuery = em.createNamedQuery("TarjetaVirtualOB.findByEmpresaAndId", TarjetaVirtualOB.class);
            typedQuery.setParameter("emp_codigo", empresa);
            typedQuery.setParameter("id", id);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }


    public String getIndex() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em.createNamedQuery("TarjetaVirtualOB.lastIndex");
            query.setMaxResults(1);
            
            String result = null;
            
            try {
                result = (String) query.getSingleResult();
            } catch (NoResultException e) {
            	tx.commit();
            	return null;
            }            
            
            tx.commit();
            return result;
        } finally {
            em.close();
        }
    }
}
