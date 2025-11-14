package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.ChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.HistorialChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialChequeraOBRepositorio;


public class ServicioHistorialChequeraOB extends ServicioOB {
    private HistorialChequeraOBRepositorio repo;
    private HistorialChequeraOB historial = new HistorialChequeraOB();

    public ServicioHistorialChequeraOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialChequeraOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialChequeraOB> crear(ChequeraOB chequera, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.chequera = chequera;
        historial.cuentaOrigen = chequera.cuentaOrigen;
        historial.estadoInicial = chequera.estado;
        historial.estadoFinal = chequera.estado;
        historial.moneda = chequera.moneda;
        historial.tipoProducto = chequera.tipoProductoFirma.descripcion;

        return futuro(() -> repo.create(historial));
    }

    public Futuro<HistorialChequeraOB> cambiaEstado(ChequeraOB chequera, AccionesOB accionFirmar, EmpresaUsuarioOB empresaUsuario, EstadoChequeraOB estadoInicial, EstadoChequeraOB estadoFinal) {
        HistorialChequeraOB historial = new HistorialChequeraOB();
        historial.accion = accionFirmar;
        historial.empresaUsuario = empresaUsuario;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = chequera.cuentaOrigen;
        historial.moneda = chequera.moneda;
        historial.tipoProducto = chequera.tipoProductoFirma.descripcion;
        historial.chequera = chequera;

        return futuro(() -> repo.create(historial));
    }
}
