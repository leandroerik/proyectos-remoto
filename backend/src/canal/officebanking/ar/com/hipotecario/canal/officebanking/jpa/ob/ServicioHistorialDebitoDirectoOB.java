package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.HistorialDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialDebitoDirectoOBRepositorio;

public class ServicioHistorialDebitoDirectoOB extends ServicioOB {
    private HistorialDebitoDirectoOBRepositorio repo;
    private HistorialDebitoDirectoOB historial = new HistorialDebitoDirectoOB();

    public ServicioHistorialDebitoDirectoOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialDebitoDirectoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialDebitoDirectoOB> crear(DebitoDirectoOB debito, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.debitoDirecto = debito;
        historial.cuentaOrigen = debito.cuentaOrigen;
        historial.estadoInicial = debito.estado;
        historial.estadoFinal = debito.estado;
        historial.moneda = debito.moneda;
        historial.monto = debito.monto;
        historial.tipoProducto = debito.tipoProducto;

        return futuro(() -> repo.create(historial));
    }

    public Futuro<HistorialDebitoDirectoOB> cambiaEstado(DebitoDirectoOB debito, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadosDebitoDirectoOB estadoInicial, EstadosDebitoDirectoOB estadoFinal) {
        HistorialDebitoDirectoOB historial = new HistorialDebitoDirectoOB();
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.debitoDirecto = debito;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = debito.cuentaOrigen;
        historial.moneda = debito.moneda;
        historial.monto = debito.monto;

        return futuro(() -> repo.create(historial));
    }

}
