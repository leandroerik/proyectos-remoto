package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.EstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoPagoTarjetaOBRepositorio;

public class ServicioEstadoPagoTarjetaOB extends ServicioOB {
    private EstadoPagoTarjetaOBRepositorio repo;

    public ServicioEstadoPagoTarjetaOB(ContextoOB contexto) {
        super(contexto);
        repo = new EstadoPagoTarjetaOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EstadoPagoTarjetaOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }
    
    public Futuro<List<EstadoPagoTarjetaOB>> findAll() {
		return futuro(() -> repo.findAll());
	}
}
