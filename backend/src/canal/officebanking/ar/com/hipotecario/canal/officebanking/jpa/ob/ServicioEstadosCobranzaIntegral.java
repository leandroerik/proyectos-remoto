package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoCobranzaIntegralOBRepositorio;

import java.util.List;

public class ServicioEstadosCobranzaIntegral extends ServicioOB{
    private EstadoCobranzaIntegralOBRepositorio repo;
    public  ServicioEstadosCobranzaIntegral(ContextoOB contexto){
        super(contexto);
        repo = new EstadoCobranzaIntegralOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }
    public Futuro<EstadosCobranzaIntegralOB> find(Integer codigo){return futuro(()->repo.find(codigo));}
    public Futuro<List<EstadosCobranzaIntegralOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
