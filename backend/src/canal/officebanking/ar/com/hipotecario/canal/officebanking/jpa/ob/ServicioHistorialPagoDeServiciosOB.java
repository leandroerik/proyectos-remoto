package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.HistorialPagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialPagoDeServiciosOBRepositorio;

public class ServicioHistorialPagoDeServiciosOB extends ServicioOB {

	private HistorialPagoDeServiciosOBRepositorio repo;

	public ServicioHistorialPagoDeServiciosOB(ContextoOB contexo) {
		super(contexo);
		repo = new HistorialPagoDeServiciosOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialPagoDeServiciosOB> crear(PagoDeServiciosOB pagoDeServicios, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		HistorialPagoDeServiciosOB historial = new HistorialPagoDeServiciosOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.pagoDeServicios = pagoDeServicios;
		historial.cuentaOrigen = pagoDeServicios.cuentaOrigen;
		historial.estadoInicial = pagoDeServicios.estado;
		historial.estadoFinal = pagoDeServicios.estado;
		historial.moneda = pagoDeServicios.moneda;
		historial.monto = pagoDeServicios.monto;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialPagoDeServiciosOB> cambiaEstado(PagoDeServiciosOB pago, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoPagoOB estadoInicial, EstadoPagoOB estadoFinal) {
		HistorialPagoDeServiciosOB historial = new HistorialPagoDeServiciosOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.pagoDeServicios = pago;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;
		historial.cuentaOrigen = pago.cuentaOrigen;
		historial.moneda = pago.moneda;
		historial.monto = pago.monto;

		return futuro(() -> repo.create(historial));
	}
}
