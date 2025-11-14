package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class DebinOBRepositorio extends RepositorioGenericoImpl<DebinOB> {
    public DebinOB findByIdDebin(String idDebin) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<DebinOB> typedQuery = em.createNamedQuery("DebinOB.findByIdDebin", DebinOB.class);
            typedQuery.setParameter("idDebin", idDebin);
            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }
}
