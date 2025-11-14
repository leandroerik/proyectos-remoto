package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.HistorialCobranzaIntegral;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialCobranzaIntegralOBRepositorio;

public class ServicioHistorialCobranzaIntegralOB extends ServicioOB {
    private HistorialCobranzaIntegralOBRepositorio repo;
    private HistorialCobranzaIntegral historial = new HistorialCobranzaIntegral();

    public ServicioHistorialCobranzaIntegralOB(ContextoOB contexto) {
        super(contexto);
        repo = new HistorialCobranzaIntegralOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialCobranzaIntegral> crear(CobranzaIntegralOB cobranza, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.cobranzaIntegral = cobranza;
        historial.cuentaOrigen = cobranza.cuentaOrigen;
        historial.estadoInicial = cobranza.estado;
        historial.estadoFinal = cobranza.estado;
        historial.moneda = cobranza.moneda;
        historial.monto = cobranza.monto;
        historial.tipoProducto = cobranza.tipoProducto;

        return futuro(() -> repo.create(historial));
    }

    public Futuro<HistorialCobranzaIntegral> cambiaEstado(CobranzaIntegralOB cobranza, AccionesOB accion, EmpresaUsuarioOB empresa, EstadosCobranzaIntegralOB estadoInicial, EstadosCobranzaIntegralOB estadoFinal) {
        HistorialCobranzaIntegral historial = new HistorialCobranzaIntegral();
        historial.accion = accion;
        historial.empresaUsuario = empresa;
        historial.cobranzaIntegral = cobranza;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.cuentaOrigen = cobranza.cuentaOrigen;
        historial.moneda = cobranza.moneda;
        historial.monto = cobranza.monto;

        return futuro(() -> repo.create(historial));
    }
}