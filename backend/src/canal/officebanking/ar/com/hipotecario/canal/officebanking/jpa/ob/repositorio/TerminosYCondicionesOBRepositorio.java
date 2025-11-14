package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TerminosYCondicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EjecucionBatchOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class TerminosYCondicionesOBRepositorio extends RepositorioGenericoImpl<TerminosYCondicionesOB> {
        public Boolean buscarPorEmpresaYCuenta(EmpresaOB empresa, String numeroCuenta) {
            EntityManager em = emf.createEntityManager();
            try {
                TypedQuery<TerminosYCondicionesOB> typedQuery = em.createNamedQuery("TerminosYCondicionesOB.buscarPorEmpresaYCuenta", TerminosYCondicionesOB.class);
                typedQuery.setParameter("idEmpresa", empresa);
                typedQuery.setParameter("numeroCuenta", numeroCuenta);

                List<TerminosYCondicionesOB> results = null;
                results = typedQuery.getResultList();
                return results.isEmpty() ? false : true;
            } finally {
                em.close();
            }
        }
    }