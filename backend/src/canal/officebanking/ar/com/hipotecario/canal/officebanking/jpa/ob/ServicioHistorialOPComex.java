package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.HistorialOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialOPComexOBRepositorio;

public class ServicioHistorialOPComex extends ServicioOB{
	
	private HistorialOPComexOBRepositorio repo;
    private HistorialOPComexOB historial = new HistorialOPComexOB();
	public ServicioHistorialOPComex(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialOPComexOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<HistorialOPComexOB> cambiaEstado(OrdenPagoComexOB ordenPago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoOPComexOB estadoInicial, EstadoOPComexOB estadoFinal, Character rectificacion) {
		HistorialOPComexOB historial = new HistorialOPComexOB();
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.ordenPago = ordenPago;
        historial.nroCuentaCreditoPesos = ordenPago.nroCuentaCreditoPesos;
        historial.nroCuentaCredMonedaExt = ordenPago.nroCuentaCredMonedaExt;
        historial.montoMonedaExt = ordenPago.montoMonedaExt;
        historial.simboloMonedaExt = ordenPago.simboloMonedaExt;
        historial.rectificacion = rectificacion == null ? ordenPago.rectificacion : rectificacion;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.moneda = ordenPago.moneda;
        historial.monto = ordenPago.monto;
        historial.fechaCreacion = LocalDateTime.now();
        return futuro(() -> repo.create(historial));
    }

    public Futuro<HistorialOPComexOB> crear(OrdenPagoComexOB ordenPago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoOPComexOB estadoInicial, EstadoOPComexOB estadoFinal) {
        historial.accion = accion;
        historial.empresaUsuario = empresaUsuario;
        historial.ordenPago = ordenPago;
        historial.nroCuentaCreditoPesos = ordenPago.nroCuentaCreditoPesos;
        historial.nroCuentaCredMonedaExt = ordenPago.nroCuentaCredMonedaExt;
        historial.montoMonedaExt = ordenPago.montoMonedaExt;
        historial.simboloMonedaExt = ordenPago.simboloMonedaExt;
        historial.rectificacion = ordenPago.rectificacion;
        historial.estadoInicial = estadoInicial;
        historial.estadoFinal = estadoFinal;
        historial.moneda = ordenPago.moneda;
        historial.monto = ordenPago.monto;
        return futuro(() -> repo.create(historial));
    }
}
