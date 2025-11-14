package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ReferenciaPagoServicioOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ReferenciaPagoDeServiciosOBRepositorio extends RepositorioGenericoImpl<ReferenciaPagoServicioOB> {
    public ReferenciaPagoServicioOB findByFields(EmpresaOB empresa, String ente, String codigoLink) {
        EntityManager em = this.emf.createEntityManager();
        try {
            String jpql = "SELECT r FROM ReferenciaPagoServicioOB r WHERE r.empresa = :empresa AND r.ente = :ente AND r.codigoLink = :codigoLink";
            TypedQuery<ReferenciaPagoServicioOB> query = em.createQuery(jpql, ReferenciaPagoServicioOB.class);
            query.setParameter("empresa", empresa);
            query.setParameter("ente", ente);
            query.setParameter("codigoLink", codigoLink);

            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; 
        } finally {
            em.close();
        }
    }
}
