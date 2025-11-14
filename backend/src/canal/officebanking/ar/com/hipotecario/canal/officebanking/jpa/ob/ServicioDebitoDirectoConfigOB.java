package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebitoDirectoConfigObRepositorio;

import java.util.List;

public class ServicioDebitoDirectoConfigOB extends ServicioOB {
    private DebitoDirectoConfigObRepositorio repo;

    public ServicioDebitoDirectoConfigOB(ContextoOB contexto) {
        super(contexto);
        repo = new DebitoDirectoConfigObRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<DebitoDirectoConfigOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
