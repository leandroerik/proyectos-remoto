package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.buhobank.UsuariosPushBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.PadronAfipEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.PadronAfipEsales.PadronAfip;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales.AlertaPushUsuarioEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales.AlertaPushEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.BBAdjustsEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.CodigosAreasBb;
import ar.com.hipotecario.backend.servicio.sql.esales.ContenidosBBEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ParametriasEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ParametriasEsales.PoliticasValidator;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper.PersonaRenaper;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales.ProspectEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.RemarketingsWhatsappEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesOBEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy.SesionStandBy;
import ar.com.hipotecario.backend.servicio.sql.esales.SolicitudesProductosEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SolicitudesProductosEsales.SolicitudProductosHB;
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales.SucursalOnboardingEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.TarjetasEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.TarjetasEsales.TarjetaEsales;
import ar.com.hipotecario.canal.buhobank.BBInfoPersona;
import ar.com.hipotecario.canal.buhobank.BBInversor.DescripcionDomicilio;
import ar.com.hipotecario.canal.buhobank.BBVU;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.SesionBB;

public class SqlEsales extends Sql {

	/* ========== SERVICIOS ========== */
	public static String SQL = "esales";

	/* ========== SERVICIOS ========== */
	// Sesiones

	public static Futuro<SesionEsales> sesionEsales(Contexto contexto, String token) {
		return futuro(() -> SesionesEsales.getPorToken(contexto, token));
	}

	public static Futuro<Boolean> tieneMailOtpValidado(Contexto contexto, String cuil, String mail) {
		return futuro(() -> SesionesEsales.mailValidado(contexto, cuil, mail));
	}
	
	public static Futuro<Boolean> existeMailFinalizado(Contexto contexto, String cuil, String mail) {
		return futuro(() -> SesionesEsales.existeMailFinalizado(contexto, cuil, mail));
	}
	
	public static Futuro<Boolean> existeTelefonoFinalizado(Contexto contexto, String cuil, String codigoArea, String caracteristica, String numero) {
		return futuro(() -> SesionesEsales.existeTelefonoFinalizado(contexto, cuil, codigoArea, caracteristica, numero));
	}

	public static Futuro<Boolean> tieneTelefonoOtpValidado(Contexto contexto, String cuil, String codigoArea, String caracteristica, String numero) {
		return futuro(() -> SesionesEsales.telefonoValidado(contexto, cuil, codigoArea, caracteristica, numero));
	}

	public static Futuro<Boolean> actualizarEstadoByToken(Contexto contexto, String tokenSesion, String estado) {
		return futuro(() -> SesionesEsales.actualizarEstadoByToken(contexto, tokenSesion, estado));
	}

	public static Futuro<SesionBB> retomarSesionAbandono(ContextoBB contexto, SesionEsales sesionEsales, SesionEsalesBB2 sesionEsalesBB2) {
		return futuro(() -> SesionesEsales.retomarSesionAbandono(contexto, sesionEsales, sesionEsalesBB2));
	}

	public static Futuro<SesionBB> retomarSesion(ContextoBB contexto, SesionEsales sesionEsales) {
		return futuro(() -> SesionesEsales.retomarSesion(contexto, sesionEsales));
	}

	public static Futuro<SesionesEsales> obtenerSesion(Contexto contexto, String cuit, Fecha fecha) {
		return futuro(() -> SesionesEsales.obtenerSesion(contexto, cuit, fecha));
	}

	public static Futuro<SesionesEsales> obtenerSesionByEstado(Contexto contexto, String cuit, String estado, Fecha fecha) {
		return futuro(() -> SesionesEsales.obtenerSesionByEstado(contexto, cuit, estado, fecha));
	}

	public static Futuro<SesionesEsales> sesionesPendientes(Contexto contexto, String estado, Fecha fechaDesde) {
		return futuro(() -> SesionesEsales.getPendientes(contexto, estado, fechaDesde));
	}

	public static Futuro<SesionesEsales> sesionPendiente(Contexto contexto, String cuil, String estado, Fecha fechaDesde) {
		return futuro(() -> SesionesEsales.getPendiente(contexto, cuil, estado, fechaDesde));
	}

	public static Futuro<Boolean> guardarSesion(Contexto contexto, SesionBB sesion) {
		return futuro(() -> SesionesEsales.guardarSesion(contexto, sesion));
	}

	public static Futuro<SesionEsalesBB2> sesionEsalesBB2(Contexto contexto, String id) {
		return futuro(() -> SesionesEsalesBB2.getId(contexto, id));
	}

	public static Futuro<SesionesEsalesBB2> sesionEsalesBB2ByCuil(Contexto contexto, String cuil, Fecha fechaDesde) {
		return futuro(() -> SesionesEsalesBB2.getCuil(contexto, cuil, fechaDesde));
	}

	public static Futuro<Boolean> guardarSesionBB2(Contexto contexto, SesionBB sesion) {
		return futuro(() -> SesionesEsalesBB2.guardarSesionBB2(contexto, sesion));
	}

	public static Futuro<Boolean> crearSesionOB(Contexto contexto, SesionBB sesion, BBVU infoVU) {
		return futuro(() -> SesionesOBEsales.crearSesionOB(contexto, sesion, infoVU));
	}

	public static Futuro<SesionesEsales> obtenerSesionesEstado(Contexto contexto, String estado, Fecha fechaDesde) {
		return futuro(() -> SesionesEsales.getPorEstado(contexto, estado, fechaDesde));
	}

	// Tarjetas
	public static Futuro<TarjetaEsales> tarjeta(Contexto contexto, Integer id) {
		return futuro(() -> TarjetasEsales.get(contexto, id));
	}

	public static Futuro<TarjetaEsales> tarjetaPorInicial(Contexto contexto, String inicial) {
		return futuro(() -> TarjetasEsales.getPorInicial(contexto, inicial));
	}

	// Parametrias
	public static Futuro<PoliticasValidator> politicas(Contexto contexto) {
		return futuro(() -> ParametriasEsales.politicas(contexto));
	}

	public static Futuro<Integer> resultadosScoring(Contexto contexto, String cuil, Fecha fechaInicio, String tipo) {
		return futuro(() -> ParametriasEsales.resultadosScoring(contexto, cuil, fechaInicio, tipo));
	}

	public static Futuro<ProspectsEsales> obtenerProspects(Contexto contexto, Fecha fechaDesde) {
		return futuro(() -> ProspectsEsales.obtenerProspects(contexto, fechaDesde));
	}

	public static Futuro<Boolean> actualizarEstadoProspect(Contexto contexto, String cuil, String listaDiscador) {
		return futuro(() -> ProspectsEsales.actualizarEstadoProspect(contexto, cuil, listaDiscador));
	}

	public static Futuro<Boolean> actualizarFechaUltimoEnvio(Contexto contexto, ProspectEsales prospect) {
		return futuro(() -> ProspectsEsales.actualizarFechaUltimoEnvio(contexto, prospect));
	}

	public static Futuro<Boolean> guardarProspects(Contexto contexto, String listaDiscador, Fecha fechaDesde) {
		return futuro(() -> ProspectsEsales.guardarProspects(contexto, listaDiscador, fechaDesde));
	}

	// Contenido onboarding
	public static Futuro<ContenidosBBEsales> getPromosLanding(Contexto contexto) {
		return futuro(() -> ContenidosBBEsales.getPromosLanding(contexto));
	}

	public static Futuro<ContenidosBBEsales> getContenidoOnboarding(Contexto contexto) {
		return futuro(() -> ContenidosBBEsales.get(contexto));
	}

	public static Futuro<ContenidosBBEsales> obtenerContenidoByTipo(Contexto contexto, String tipo) {
		return futuro(() -> ContenidosBBEsales.getByTipo(contexto, tipo));
	}

	// CodigoArea
	public static Futuro<CodigosAreasBb> getCodigosAreasPorProvincias(Contexto contexto, String provincia) {
		return futuro(() -> CodigosAreasBb.getPorProvincia(contexto, provincia));
	}

	public static Futuro<CodigosAreasBb> getCodigosAreas(Contexto contexto) {
		return futuro(() -> CodigosAreasBb.get(contexto));
	}

	// Notificaciones push
	public static Futuro<AlertaPushUsuarioEsales> obtenerUltimoToken(Contexto contexto, String cuil) {
		return futuro(() -> AlertaPushUsuariosEsales.obtenerUltimoToken(contexto, cuil));
	}

	public static Futuro<AlertaPushUsuarioEsales> obtenerUltimoMail(Contexto contexto, String cuil) {
		return futuro(() -> AlertaPushUsuariosEsales.obtenerUltimoMail(contexto, cuil));
	}

	public static Futuro<AlertasPushEsales> obtenerAlertasPush(Contexto contexto) {
		return futuro(() -> AlertasPushEsales.obtenerAlertaPush(contexto));
	}

	public static Futuro<AlertaPushUsuariosEsales> alertasPushPendientes(Contexto contexto) {
		return futuro(() -> AlertaPushUsuariosEsales.pendientesPush(contexto));
	}
	
	public static Futuro<AlertaPushUsuariosEsales> alertasMailPendientes(Contexto contexto) {
		return futuro(() -> AlertaPushUsuariosEsales.pendientesMail(contexto));
	}

	public static Futuro<Boolean> guardarNuevaAlertaPush(Contexto contexto, String codigoAlerta, String tokenFirebase, String mail, String cuil, Fecha fechaUltimoAbandono, String estado, String estadoMail, String plataforma) {
		return futuro(() -> AlertasPushEsales.guardarNuevaAlertaPush(contexto, codigoAlerta, tokenFirebase, mail, cuil, fechaUltimoAbandono, estado, estadoMail, plataforma));
	}

	public static Futuro<Boolean> actualizarEstadoAlerta(Contexto contexto, String id, String estado, String tipoEstado) {
		return futuro(() -> AlertaPushUsuariosEsales.actualizarEstadoAlerta(contexto, id, estado, tipoEstado));
	}

	public static Futuro<AlertaPushUsuarioEsales> obtenerAlertaPushUsuarioByToken(Contexto contexto, String codigoAlerta, String tokenFirebase) {
		return futuro(() -> AlertaPushUsuariosEsales.existeAlertaPushUsuarioByToken(contexto, codigoAlerta, tokenFirebase));
	}

	public static Futuro<AlertaPushUsuarioEsales> obtenerAlertaPushUsuarioByMail(Contexto contexto, String codigoAlerta, String mail) {
		return futuro(() -> AlertaPushUsuariosEsales.existeAlertaPushUsuarioByMail(contexto, codigoAlerta, mail));
	}

	public static Futuro<CodigosAreasBb> getCodigosAreasPorCodigo(Contexto contexto, String codigo) {
		return futuro(() -> CodigosAreasBb.getPorCodigo(contexto, codigo));
	}

	public static Futuro<UsuariosPushBuhobank> obtenerAbandonos(Contexto contexto, AlertaPushEsales notificacion) {
		return futuro(() -> UsuariosPushBuhobank.obtenerAbandonosSesion(contexto, notificacion));
	}

	// telemarketing indicador
	public static Futuro<SesionesEsales> getSesionesSinOferta(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> SesionesEsales.getSesionesSinOferta(contexto, fechaDesde, fechaHasta));
	}

	public static Futuro<Boolean> actualizarMotorIndicador(Contexto contexto, String sesionId, int valor) {
		return futuro(() -> SesionesEsalesBB2.actualizarMotorIndicador(contexto, sesionId, valor));
	}

	public static Futuro<Boolean> guardarProspect(Contexto contexto, SesionEsales sesion, String listaDiscador) {
		return futuro(() -> ProspectsEsales.guardarProspect(contexto, sesion, listaDiscador));
	}

	public static Futuro<ProspectsEsales> getProspectByCuil(Contexto contexto, String cuil, String listaDiscador) {
		return futuro(() -> ProspectsEsales.getProspectByCuil(contexto, cuil, listaDiscador));
	}

	// parametrias onboarding

	public static Futuro<Boolean> actualizarEstadoBBInversor(Contexto contexto, String id, String estado) {
		return futuro(() -> SesionesEsalesBB2.actualizarEstadoBBInversor(contexto, id, estado));
	}

	// buho inversor

	public static Futuro<SolicitudesProductosEsales> obtenerSolicitudes(Contexto contexto) {
		return futuro(() -> SolicitudesProductosEsales.obtenerSolicitudes(contexto));
	}

	public static Futuro<Boolean> crearSolicitudBuhoInversor(Contexto contexto, String idCobis, SesionEsales sesion, String tipoProducto, String numeroCuentaUsd, String cbuUsd, String numeroCuenta, String idSucursal, String cbu, String presentaDoc, DescripcionDomicilio domicilio) {
		return futuro(() -> SolicitudesProductosEsales.crearSolicitudProducto(contexto, idCobis, sesion, tipoProducto, numeroCuentaUsd, cbuUsd, numeroCuenta, idSucursal, cbu, presentaDoc, domicilio));
	}

	public static Futuro<SesionesEsales> sesionesBBInversorAceptada(Contexto contexto, Fecha fechaDesde) {
		return futuro(() -> SesionesEsales.sesionesBBInversorPaquetes(contexto, fechaDesde));
	}

	// Información RENAPER / API-Viviendas
	public static Futuro<Boolean> crear(Contexto contexto, BBInfoPersona infoPersona) {
		return futuro(() -> PersonasRenaper.crear(contexto, infoPersona));
	}

	public static Futuro<PersonaRenaper> get(Contexto contexto, String cuil) {
		return futuro(() -> PersonasRenaper.get(contexto, cuil));
	}

	public static Futuro<Boolean> update(Contexto contexto, PersonaRenaper infoPersona) {
		return futuro(() -> PersonasRenaper.update(contexto, infoPersona));
	}

	// proceso stand by
	public static Futuro<SesionesStandBy> sesionesByEstado(Contexto contexto, String estado, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> SesionesStandBy.sesionesByEstado(contexto, estado, fechaDesde, fechaHasta));
	}

	public static Futuro<SesionStandBy> sesionStandBy(Contexto contexto, String cuil) {
		return futuro(() -> SesionesStandBy.sesionByCuil(contexto, cuil));
	}

	public static Futuro<Boolean> crearSesionStandBy(Contexto contexto, String cuil) {
		return futuro(() -> SesionesStandBy.crear(contexto, cuil));
	}

	public static Futuro<Boolean> actualizarSesionStandBy(Contexto contexto, SesionStandBy sesionStandBy) {
		return futuro(() -> SesionesStandBy.put(contexto, sesionStandBy));
	}

	public static Futuro<Boolean> borrarSesionStandBy(Contexto contexto, String cuil) {
		return futuro(() -> SesionesStandBy.borrarSesion(contexto, cuil));
	}

	// sucursales onboarding
	public static Futuro<SucursalesOnboardingEsales> obtenerSucursalesOnboarding(Contexto contexto) {
		return futuro(() -> SucursalesOnboardingEsales.get(contexto));
	}

	public static Futuro<SucursalOnboardingEsales> obtenerSucursalesByQr(Contexto contexto, String urlQr) {
		return futuro(() -> SucursalesOnboardingEsales.getByQr(contexto, urlQr));
	}

	public static Futuro<Boolean> actualizarSucursalesOnboarding(Contexto contexto, SucursalOnboardingEsales sucursal) {
		return futuro(() -> SucursalesOnboardingEsales.put(contexto, sucursal));
	}

	public static Futuro<Boolean> crearSucursalesOnboarding(Contexto contexto, SucursalOnboardingEsales sucursal) {
		return futuro(() -> SucursalesOnboardingEsales.post(contexto, sucursal));
	}

	// chatbot
	public static Futuro<RemarketingsWhatsappEsales> obtenerRemarketingsWhatsapp(Contexto contexto) {
		return futuro(() -> RemarketingsWhatsappEsales.get(contexto));
	}

	public static Futuro<Boolean> ejecutarHistoricoRemarketingsWhatsapp(Contexto contexto, Fecha fechaDesde, String estados, String eventos) {
		return futuro(() -> RemarketingsWhatsappEsales.ejecutarHistorico(contexto, fechaDesde, estados, eventos));
	}

	// Adjust - Webhook

	public static Futuro<BBAdjustsEsales> registrarEventoAdjust(Contexto contexto, String adid, String claves, String valores) {
		return futuro(() -> BBAdjustsEsales.post(contexto, adid, claves, valores));
	}

	public static Futuro<PadronAfip> getPadronAfip(Contexto contexto, String cuit) {
		return futuro(() -> PadronAfipEsales.get(contexto, cuit));
	}
}
