package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.BE.BETransferencia;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TransferenciaBERepositorio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TransferenciaOBRepositorio;

import java.math.BigDecimal;
import java.util.List;

public class ServicioTransferenciaBE extends ServicioOB{
    TransferenciaBERepositorio repo;
    public ServicioTransferenciaBE(ContextoOB contexto) {
        super(contexto);
        repo = new TransferenciaBERepositorio();
        repo.setEntityManager(this.getEntityManager());
        
    }

    public Futuro<List<BETransferencia>> existsTransferenciaByCuitEmpresaAndCbuCredito(BigDecimal empresa, String cbu){
        return futuro(()->repo.existsTransferenciaByCuitEmpresaAndCbuCredito(empresa,cbu));
    }

}
