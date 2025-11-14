package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.HistorialDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialDebinOBRepositorio;

public class ServicioHistorialDebinOB extends ServicioOB {

    private HistorialDebinOBRepositorio repo;

    public ServicioHistorialDebinOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialDebinOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialDebinOB> cambiaEstado(DebinOB debin, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoDebinEnviadasOB estadoInicialEnviada, EstadoDebinEnviadasOB estadoFinalEnviada, EstadoDebinRecibidasOB estadoInicialRecibida, EstadoDebinRecibidasOB estadoFinalRecibida) {
        HistorialDebinOB historial = new HistorialDebinOB();
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.debin = debin;
        historial.cuentaDestino = debin.cuentaVendedor;
        historial.cuentaOrigen = debin.cuentaOrigen;
        historial.estadoInicialEnviada = estadoInicialEnviada;
        historial.estadoFinalEnviada = estadoFinalEnviada;
        historial.estadoInicialRecibida = estadoInicialRecibida;
        historial.estadoFinalRecibida = estadoFinalRecibida;
        historial.moneda = debin.moneda;
        historial.monto = debin.monto;
        return futuro(() -> repo.create(historial));
    }
}
