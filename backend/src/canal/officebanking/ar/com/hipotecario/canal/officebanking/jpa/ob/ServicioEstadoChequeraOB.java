package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoChequeraOBRepositorio;

public class ServicioEstadoChequeraOB extends ServicioOB {
    private EstadoChequeraOBRepositorio repo;

    public ServicioEstadoChequeraOB(ContextoOB contexto) {
        super(contexto);
        repo = new EstadoChequeraOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EstadoChequeraOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }
}
