package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.ComprobantePAPOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ComprobantesPAPOBRepositorio extends RepositorioGenericoImpl<ComprobantePAPOB> {

    public List<ComprobantePAPOB> listar(ContextoOB contexto, int nroAdh, int nroConv, int nroSubConv, LocalDateTime fechaD, LocalDateTime fechaH){
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ComprobantePAPOB> typedQuery = em.createNamedQuery("comprobantes", ComprobantePAPOB.class);
            typedQuery.setParameter("convenio", nroConv);
            typedQuery.setParameter("subconv", nroSubConv);
            typedQuery.setParameter("fechaD",  fechaD );
            typedQuery.setParameter("fechaH", fechaH);
            typedQuery.setParameter("codigo", contexto.sesion().empresaOB);
            return  typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
}
