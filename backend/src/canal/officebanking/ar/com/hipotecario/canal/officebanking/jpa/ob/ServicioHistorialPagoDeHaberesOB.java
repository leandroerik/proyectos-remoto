package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.HistorialPagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialPagoDeHaberesOBRepositorio;

public class ServicioHistorialPagoDeHaberesOB extends ServicioOB {
	private HistorialPagoDeHaberesOBRepositorio repo;
	private HistorialPagoDeHaberesOB historial = new HistorialPagoDeHaberesOB();

	public ServicioHistorialPagoDeHaberesOB(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialPagoDeHaberesOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialPagoDeHaberesOB> crear(PagoDeHaberesOB pago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.pagoDeHaberes = pago;
		historial.cuentaOrigen = pago.cuentaOrigen;
		historial.estadoInicial = pago.estado;
		historial.estadoFinal = pago.estado;
		historial.moneda = pago.moneda;
		historial.monto = pago.monto;
		historial.tipoProducto = pago.tipoProducto;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialPagoDeHaberesOB> cambiaEstado(PagoDeHaberesOB pago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoPagosHaberesOB estadoInicial, EstadoPagosHaberesOB estadoFinal) {
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.pagoDeHaberes = pago;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;
		historial.cuentaOrigen = pago.cuentaOrigen;
		historial.moneda = pago.moneda;
		historial.monto = pago.monto;
		historial.tipoProducto = pago.tipoProducto;

		return futuro(() -> repo.create(historial));
	}
}
