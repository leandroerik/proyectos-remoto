package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoAProveedoresConfigObRepositorio;

import java.util.List;

public class ServicioPagoAProveedoresConfigOB extends ServicioOB{
    private PagoAProveedoresConfigObRepositorio repo;
    public ServicioPagoAProveedoresConfigOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoAProveedoresConfigObRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<PagoAProveedoresConfigOB>> findAll(){return futuro(()->repo.findAll());}
}
