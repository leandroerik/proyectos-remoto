package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.PagoTarjetaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class PagoTarjetaOBRepositorio extends RepositorioGenericoImpl<PagoTarjetaOB> {
    @Override
    public List<PagoTarjetaOB> findByField(String field, Object value) {
        return super.findByField(field, value);
    }
    
    public List<PagoTarjetaOB> buscarPorEstado(EstadoEcheqOB estado) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<PagoTarjetaOB> typedQuery = em.createNamedQuery("PagoTarjetaOB.buscarPorEstado", PagoTarjetaOB.class);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
}

