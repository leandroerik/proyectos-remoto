package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.FirmasProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.FirmasProductosOBRepositorio;

import java.math.BigDecimal;
import java.util.List;


public class ServicioFirmasProductosOB extends ServicioOB {

    private FirmasProductosOBRepositorio repo;

    public ServicioFirmasProductosOB(ContextoOB contexto) {
        super(contexto);
        repo = new FirmasProductosOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<FirmasProductoOB>> buscarOperacionesSegunMonto(Integer id) {
        return futuro(() -> repo.buscarOperacionesSegunMonto(id));
    }

    public Futuro<FirmasProductoOB> crear(Integer id, int codProd, BigDecimal monto) {
        FirmasProductoOB firma = new FirmasProductoOB();
        firma.id = id;
        firma.codProducto = codProd;
        firma.monto = monto;
        return futuro(() -> repo.create(firma));
    }
}
