package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.HistorialFCIOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialFCIOBRepositorio;

public class ServicioHistorialFCI extends ServicioOB {

	private HistorialFCIOBRepositorio repo;

	public ServicioHistorialFCI(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialFCIOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialFCIOB> crear(FondosComunesOB fci, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		HistorialFCIOB historial = new HistorialFCIOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.fci = fci;
		historial.estadoInicial = fci.estado;
		historial.estadoFinal = fci.estado;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialFCIOB> cambiaEstado(FondosComunesOB fci, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoSolicitudInversionOB estadoInicial, EstadoSolicitudInversionOB estadoFinal) {
		HistorialFCIOB historial = new HistorialFCIOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.fci = fci;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;

		return futuro(() -> repo.create(historial));
	}
}