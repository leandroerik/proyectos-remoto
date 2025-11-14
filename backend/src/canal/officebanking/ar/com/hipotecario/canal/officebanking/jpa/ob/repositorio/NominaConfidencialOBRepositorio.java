package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaConfidencialOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class NominaConfidencialOBRepositorio extends RepositorioGenericoImpl<NominaConfidencialOB> {
    public NominaConfidencialOB findByUsuarioEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NominaConfidencialOB> typedQuery = em.createNamedQuery("NominaConfidencial", NominaConfidencialOB.class);
            typedQuery.setParameter("usuario", usuario);
            typedQuery.setParameter("emp_codigo", empresa);
            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<NominaConfidencialOB> findByEmpresa(EmpresaOB empresa) {
            EntityManager em = emf.createEntityManager();
            try {
                TypedQuery<NominaConfidencialOB> typedQuery = em.createNamedQuery("NominaConfidencial.findByEmpresa", NominaConfidencialOB.class);
                typedQuery.setParameter("emp_codigo", empresa);
                return typedQuery.getResultList();
            } finally {
                em.close();
            }
        }

    public int eliminarUsuario(Integer emp_codigo) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em.createNamedQuery("NominaConfidencial.delete", NominaConfidencialOB.class);
            query.setParameter("emp_codigo", emp_codigo);
            int result = query.executeUpdate();
            tx.commit();
            return result;
        } finally {
            em.close();
        }
    }


}
