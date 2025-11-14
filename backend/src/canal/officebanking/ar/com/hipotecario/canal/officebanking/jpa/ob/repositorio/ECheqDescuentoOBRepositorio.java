package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ECheqDescuentoOBRepositorio extends RepositorioGenericoImpl<EcheqDescuentoOB> {
    @Override
    public List<EcheqDescuentoOB> findByField(String field, Object value) {
        return super.findByField(field, value);
    }
    
    public List<EcheqDescuentoOB> buscarPorEstado(EstadoEcheqOB estado) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<EcheqDescuentoOB> typedQuery = em.createNamedQuery("EcheqDescuentoOB.buscarPorEstado", EcheqDescuentoOB.class);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
}

