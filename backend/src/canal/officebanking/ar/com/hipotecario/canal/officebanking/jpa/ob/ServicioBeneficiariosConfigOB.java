package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoBeneficiariosConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoBeneficiariosConfigOBRepositorio;

import java.util.List;

public class ServicioBeneficiariosConfigOB extends ServicioOB{
    private PagoBeneficiariosConfigOBRepositorio repo;
    public ServicioBeneficiariosConfigOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoBeneficiariosConfigOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<PagoBeneficiariosConfigOB>> findAll(){return futuro(()->repo.findAll());}
}
