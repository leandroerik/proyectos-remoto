package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoCobranzaIntegralOBRepositorio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoDebinLoteOBRepositorio;

import java.util.List;

public class ServicioEstadosDebinPorLote extends ServicioOB{
    private EstadoDebinLoteOBRepositorio repo;
    public  ServicioEstadosDebinPorLote(ContextoOB contexto){
        super(contexto);
        repo = new EstadoDebinLoteOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }
    public Futuro<EstadosDebinLoteOB> find(Integer codigo){return futuro(()->repo.find(codigo));}
    public Futuro<List<EstadosDebinLoteOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
