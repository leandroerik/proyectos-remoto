package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.HistorialEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialEcheqOBRepositorio;

public class ServicioHistorialPagoTarjetaOB extends ServicioOB {
    private HistorialEcheqOB historial = new HistorialEcheqOB();
    private HistorialEcheqOBRepositorio repo;

    public ServicioHistorialPagoTarjetaOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialEcheqOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialEcheqOB> crear(EcheqOB echeq, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.echeq = echeq;
        historial.cuentaOrigen = echeq.cuentaOrigen;
        historial.estadoInicial = echeq.estado;
        historial.estadoFinal = echeq.estado;
        historial.moneda = echeq.moneda;
        historial.tipoProducto = echeq.tipoProductoFirma.descripcion;

        return futuro(() -> repo.create(historial));
    }

    public Futuro<HistorialEcheqOB> cambiaEstado(EcheqOB echeq, AccionesOB accionFirmar, EmpresaUsuarioOB empresaUsuario, EstadoEcheqOB estadoInicial, EstadoEcheqOB estadoFinal) {
        HistorialEcheqOB historial = new HistorialEcheqOB();
        historial.accion = accionFirmar;
        historial.empresaUsuario = empresaUsuario;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = echeq.cuentaOrigen;
        historial.moneda = echeq.moneda;
        historial.tipoProducto = echeq.tipoProductoFirma.descripcion;
        historial.echeq = echeq;

        return futuro(() -> repo.create(historial));
    }
}
