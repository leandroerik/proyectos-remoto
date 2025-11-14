package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.HistorialPagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialPagoVepsOBRepositorio;

public class ServicioHistorialPagoVepsOB extends ServicioOB {

	private HistorialPagoVepsOBRepositorio repo;

	public ServicioHistorialPagoVepsOB(ContextoOB contexo) {
		super(contexo);
		repo = new HistorialPagoVepsOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialPagosVepOB> crear(PagosVepOB pagoVeps, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		HistorialPagosVepOB historial = new HistorialPagosVepOB();
		historial.accion = accion;
		historial.pagosVep = pagoVeps;
		historial.empresaUsuario = empresaUsuario;
		historial.estadoInicial = pagoVeps.estado;
		historial.estadoFinal = pagoVeps.estado;
		historial.moneda = pagoVeps.moneda;
		historial.monto = pagoVeps.monto;
		historial.cuentaOrigen = pagoVeps.cuentaOrigen;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialPagosVepOB> cambiaEstado(PagosVepOB pagoVeps, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoPagoOB estadoInicial, EstadoPagoOB estadoFinal) {
		HistorialPagosVepOB historial = new HistorialPagosVepOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.pagosVep = pagoVeps;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;
		historial.cuentaOrigen = pagoVeps.cuentaOrigen;
		historial.moneda = pagoVeps.moneda;
		historial.monto = pagoVeps.monto;

		return futuro(() -> repo.create(historial));
	}
}
