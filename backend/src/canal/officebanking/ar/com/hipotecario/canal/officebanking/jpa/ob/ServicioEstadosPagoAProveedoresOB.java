package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EstadoPagoAProveedoresOBRepositorio;

import java.util.List;

public class ServicioEstadosPagoAProveedoresOB extends ServicioOB {
    private EstadoPagoAProveedoresOBRepositorio repo;

    public ServicioEstadosPagoAProveedoresOB(ContextoOB contexto) {
        super(contexto);
        repo = new EstadoPagoAProveedoresOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EstadosPagosAProveedoresOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }
    public Futuro<List<EstadosPagosAProveedoresOB>> findAll() {
        return futuro(() -> repo.findAll());
    }
}
