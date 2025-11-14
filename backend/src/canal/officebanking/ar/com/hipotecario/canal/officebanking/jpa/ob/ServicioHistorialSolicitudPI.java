package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoSolicitudFCIOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.HistorialSolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.SolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialSolicitudPIOBRepositorio;

public class ServicioHistorialSolicitudPI extends ServicioOB {

	private HistorialSolicitudPIOBRepositorio repo;

	public ServicioHistorialSolicitudPI(ContextoOB contexto) {
		super(contexto);
		repo = new HistorialSolicitudPIOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<HistorialSolicitudPerfilInversorOB> crear(ContextoOB contexto, SolicitudPerfilInversorOB perfil, AccionesOB accion, EmpresaUsuarioOB empresaUsuario) {
		HistorialSolicitudPerfilInversorOB historial = new HistorialSolicitudPerfilInversorOB();
		ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.perfilInversor = perfil;
		if (perfil.estado.id.equals(EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()))
			historial.estadoInicial = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
		else
			historial.estadoInicial = perfil.estado;
		historial.estadoFinal = perfil.estado;

		return futuro(() -> repo.create(historial));
	}

	public Futuro<HistorialSolicitudPerfilInversorOB> cambiaEstado(SolicitudPerfilInversorOB perfil, AccionesOB accion, EmpresaUsuarioOB empresaUsuario, EstadoSolicitudInversionOB estadoInicial, EstadoSolicitudInversionOB estadoFinal) {
		HistorialSolicitudPerfilInversorOB historial = new HistorialSolicitudPerfilInversorOB();
		historial.accion = accion;
		historial.empresaUsuario = empresaUsuario;
		historial.perfilInversor = perfil;
		historial.estadoInicial = estadoInicial;
		historial.estadoFinal = estadoFinal;

		return futuro(() -> repo.create(historial));
	}
}