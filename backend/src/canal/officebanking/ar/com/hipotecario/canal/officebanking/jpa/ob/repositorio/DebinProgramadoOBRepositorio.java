package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class DebinProgramadoOBRepositorio extends RepositorioGenericoImpl<DebinProgramadoOB> {
    public DebinProgramadoOB findByIdDebin(String idDebin) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<DebinProgramadoOB> typedQuery = em.createNamedQuery("DebinProgramadoOB.findByIdDebin", DebinProgramadoOB.class);
            typedQuery.setParameter("idDebin", idDebin);
            typedQuery.setMaxResults(1);
            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }

}
