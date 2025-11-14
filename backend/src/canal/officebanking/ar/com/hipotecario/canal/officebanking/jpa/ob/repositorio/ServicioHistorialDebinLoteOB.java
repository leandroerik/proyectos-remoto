package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.HistorialCobranzaIntegral;

public class ServicioHistorialDebinLoteOB extends ServicioOB {
    private HistorialDebinLoteOBRepositorio repo;
    private HistorialDebinLote historial = new HistorialDebinLote();
    public ServicioHistorialDebinLoteOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialDebinLoteOBRepositorio();
        repo.setEntityManager(this.getEntityManager());

    }
    public Futuro<HistorialDebinLote> crear(DebinLoteOB debinLote, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.debinLote = debinLote;
        historial.cuentaOrigen = debinLote.cuentaOrigen;
        historial.estadoInicial = debinLote.estado;
        historial.estadoFinal = debinLote.estado;
        historial.moneda = debinLote.moneda;
        historial.monto = debinLote.monto;
        historial.tipoProducto = debinLote.tipoProducto;

        return futuro(() -> repo.create(historial));
    }
    public Futuro<HistorialDebinLote> cambiaEstado(DebinLoteOB debinLote, AccionesOB accion, EmpresaUsuarioOB empresa, EstadosDebinLoteOB estadoInicial, EstadosDebinLoteOB estadoFinal) {
        HistorialDebinLote historial = new HistorialDebinLote();
        historial.accion = accion;
        historial.empresaUsuario = empresa;
        historial.debinLote = debinLote;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = debinLote.cuentaOrigen;
        historial.moneda = debinLote.moneda;
        historial.monto = debinLote.monto;

        return futuro(() -> repo.create(historial));
    }
}
