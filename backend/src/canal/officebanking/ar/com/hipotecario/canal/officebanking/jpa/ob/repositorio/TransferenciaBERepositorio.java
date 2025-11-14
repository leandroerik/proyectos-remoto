package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.BE.BETransferencia;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;

public class TransferenciaBERepositorio extends RepositorioGenericoImpl<BETransferencia> {

    public List<BETransferencia> existsTransferenciaByCuitEmpresaAndCbuCredito(BigDecimal cuitEmpresa, String cbuCredito) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<BETransferencia> query = em.createNamedQuery("BETransferencia.findByCuitEmpresaAndCbuCredito", BETransferencia.class);
        query.setParameter("cuitEmpresa", cuitEmpresa);
        query.setParameter("cbuCredito", cbuCredito);
        return query.getResultList();
    }
}
