package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.HistorialPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialPagoAProveedoresOBRepositorio;

public class ServicioHistorialPagoAProveedoresOB extends ServicioOB {
    private HistorialPagoAProveedoresOBRepositorio repo;
    private HistorialPagoAProveedoresOB historial = new HistorialPagoAProveedoresOB();

    public ServicioHistorialPagoAProveedoresOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialPagoAProveedoresOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialPagoAProveedoresOB> crear(PagoAProveedoresOB pago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.pap = pago;
        historial.cuentaOrigen = pago.cuentaOrigen;
        historial.estadoInicial = pago.estado;
        historial.estadoFinal = pago.estado;
        historial.moneda = pago.moneda;
        historial.monto = pago.monto;
        historial.tipoProducto = pago.tipoProducto;

        return futuro(() -> repo.create(historial));
    }
    
    public Futuro<HistorialPagoAProveedoresOB> cambiaEstado(PagoAProveedoresOB pagoVeps, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadosPagosAProveedoresOB estadoInicial, EstadosPagosAProveedoresOB estadoFinal) {
        HistorialPagoAProveedoresOB historial = new HistorialPagoAProveedoresOB();
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.pap = pagoVeps;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = pagoVeps.cuentaOrigen;
        historial.moneda = pagoVeps.moneda;
        historial.monto = pagoVeps.monto;

        return futuro(() -> repo.create(historial));
    }

}
