package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ECheqOBRepositorio extends RepositorioGenericoImpl<EcheqOB> {
    @Override
    public List<EcheqOB> findByField(String field, Object value) {
        return super.findByField(field, value);
    }

    public List<EcheqOB> buscarPorEstado(EstadoEcheqOB estado) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<EcheqOB> typedQuery = em.createNamedQuery("EcheqOB.buscarPorEstado", EcheqOB.class);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public List<EcheqOB> findChequeByEmisorAndIdAndEstado(String emisorDocumento,List<Integer> estados){
        EntityManager em = emf.createEntityManager();
        try{
            TypedQuery<EcheqOB> typedQuery = em.createNamedQuery("EcheqOB.findChequeByEmisorAndIdAndEstado", EcheqOB.class);
            typedQuery.setParameter("emisorDocumento",emisorDocumento);
            typedQuery.setParameter("estados",estados);
            return typedQuery.getResultList();
        }finally {
            em.close();
        }
    }
    public List<EcheqOB> findByFieldIn(List<String> valores) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM EcheqOB e WHERE e.id IN (SELECT MAX(e2.id) FROM EcheqOB e2 WHERE e2.idCheque IN :valores GROUP BY e2.idCheque)";
            TypedQuery<EcheqOB> query = em.createQuery(jpql, EcheqOB.class);
            query.setParameter("valores", valores);
            return query.getResultList();
        }finally {
            em.close();
        }

    }
}

