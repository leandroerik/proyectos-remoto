package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.FirmasProductoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;


import java.util.List;

public class FirmasProductosOBRepositorio extends RepositorioGenericoImpl<FirmasProductoOB> {

    public List<FirmasProductoOB> buscarOperacionesSegunMonto(Integer id) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<FirmasProductoOB> typedQuery = em.createNamedQuery("FirmasProductoOB.buscarOperacionesSegunMonto", FirmasProductoOB.class);
            typedQuery.setParameter("id", id);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }


}
