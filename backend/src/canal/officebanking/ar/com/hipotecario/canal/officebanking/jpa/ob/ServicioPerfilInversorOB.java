package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoSolicitudFCIOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMonedasOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.SolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PerfilInversorOBRepositorio;

public class ServicioPerfilInversorOB extends ServicioOB {

	private static PerfilInversorOBRepositorio repo;

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
	private static ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
	private static ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
	private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
	private static EstadoBandejaOB estadoPendienteFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();

	public ServicioPerfilInversorOB(ContextoOB contexto) {
		super(contexto);
		repo = new PerfilInversorOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<SolicitudPerfilInversorOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<SolicitudPerfilInversorOB> update(SolicitudPerfilInversorOB perfil) {
		perfil.ultimaModificacion = LocalDateTime.now();
		return futuro(() -> repo.update(perfil));
	}

	public Futuro<SolicitudPerfilInversorOB> crear(ContextoOB contexto, SolicitudPerfilInversorOB perfil) {
		SesionOB sesion = contexto.sesion();

		perfil.cuentaOrigen = "no_aplica";
		perfil.monto = BigDecimal.valueOf(0);
		perfil.ultimaModificacion = LocalDateTime.now();
		perfil.fechaUltActulizacion = LocalDateTime.now();
		perfil.empCodigo = sesion.empresaOB;
		perfil.empresa = sesion.empresaOB;

		if (perfil.estado.id.equals(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()))
			perfil.estadoBandeja = estadoPendienteFirma;
		else if (perfil.estado.id.equals(EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()))
			perfil.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
		perfil.tipoProductoFirma = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get();
		perfil.moneda = servicioMoneda.find(EnumMonedasOB.PESOS.getMoneda()).get();
		perfil.usuario = sesion.usuarioOB;

		return futuro(() -> repo.create(perfil));
	}

	public Futuro<List<SolicitudPerfilInversorOB>> buscarPorEmpresaYEstado(EmpresaOB empresa) {
		EstadoSolicitudInversionOB estadoPendiente = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();

		return futuro(() -> repo.buscarPorEmpresaYEstado(empresa, estadoPendiente));
	}
}
