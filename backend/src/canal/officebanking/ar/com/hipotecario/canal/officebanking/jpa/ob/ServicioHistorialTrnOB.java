package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.HistorialTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialTrnOBRepositorio;

public class ServicioHistorialTrnOB extends ServicioOB {

	private HistorialTrnOBRepositorio repo;

	public ServicioHistorialTrnOB(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialTrnOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialTrnOB> crear(TransferenciaOB transferencia, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		HistorialTrnOB historial = new HistorialTrnOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.transferencia = transferencia;
		historial.cuentaDestino = transferencia.credito.nroCuenta;
		historial.cuentaOrigen = transferencia.debito.nroCuenta;
		historial.estadoInicial = transferencia.estado;
		historial.estadoFinal = transferencia.estado;
		historial.moneda = transferencia.moneda;
		historial.monto = transferencia.monto;
		historial.tipoCuentaOrigen = transferencia.debito.tipoCuenta;
		historial.tipoCuentaDestino = transferencia.credito.tipoCuenta;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialTrnOB> cambiaEstado(TransferenciaOB transferencia, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoTRNOB estadoInicial, EstadoTRNOB estadoFinal) {
		HistorialTrnOB historial = new HistorialTrnOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.transferencia = transferencia;
		historial.cuentaDestino = transferencia.credito.nroCuenta;
		historial.cuentaOrigen = transferencia.debito.nroCuenta;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;
		historial.moneda = transferencia.moneda;
		historial.monto = transferencia.monto;
		historial.tipoCuentaOrigen = transferencia.debito.tipoCuenta;
		historial.tipoCuentaDestino = transferencia.credito.tipoCuenta;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<List<HistorialTrnOB>> buscar(TransferenciaOB transferencia, EmpresaUsuarioOB empresaUsuario) {
		return futuro(() -> repo.buscar(transferencia, empresaUsuario));
	}

	public Futuro<List<HistorialTrnOB>> buscar(TransferenciaOB transferencia) {
		return futuro(() -> repo.buscar(transferencia));
	}

}