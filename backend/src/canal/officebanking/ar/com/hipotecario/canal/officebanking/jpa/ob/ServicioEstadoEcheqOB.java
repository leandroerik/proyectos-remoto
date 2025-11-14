package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoEcheqOBRepositorio;

public class ServicioEstadoEcheqOB extends ServicioOB {
    private EstadoEcheqOBRepositorio repo;

    public ServicioEstadoEcheqOB(ContextoOB contexto) {
        super(contexto);
        repo = new EstadoEcheqOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EstadoEcheqOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }
}
