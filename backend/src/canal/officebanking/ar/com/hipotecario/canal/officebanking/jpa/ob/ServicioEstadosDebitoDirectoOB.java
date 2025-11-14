package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoDebitoDirectoOBRepositorio;

import java.util.List;

public class ServicioEstadosDebitoDirectoOB extends ServicioOB {
    private EstadoDebitoDirectoOBRepositorio repo;

    public ServicioEstadosDebitoDirectoOB(ContextoOB contexto) {
        super(contexto);
        repo = new EstadoDebitoDirectoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EstadosDebitoDirectoOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }

    public Futuro<List<EstadosDebitoDirectoOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
