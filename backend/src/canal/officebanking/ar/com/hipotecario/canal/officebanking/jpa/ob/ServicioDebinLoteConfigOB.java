package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.DebinLote.DebinLoteConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebinLoteConfigOBRepositorio;

import java.util.List;

public class ServicioDebinLoteConfigOB extends ServicioOB{
    private DebinLoteConfigOBRepositorio repo;

    public ServicioDebinLoteConfigOB(ContextoOB contexto){
        super(contexto);
        repo = new DebinLoteConfigOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<DebinLoteConfigOB>> findAll(){return futuro(()->repo.findAll());}
}
