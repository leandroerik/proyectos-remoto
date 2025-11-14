package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CobranzaIntegralConfigObRepositorio;

import java.util.List;

public class ServicioCobranzaIntegralConfigOB extends ServicioOB {
    private CobranzaIntegralConfigObRepositorio repo;

    public ServicioCobranzaIntegralConfigOB(ContextoOB contexto) {
        super(contexto);
        repo = new CobranzaIntegralConfigObRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<CobranzaIntegralConfigOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
