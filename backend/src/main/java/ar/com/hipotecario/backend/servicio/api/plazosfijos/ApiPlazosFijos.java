package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.CedipNuevo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.ResponsePostTransmision;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;

//http://api-plazosfijos-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiPlazosFijos extends Api {

	// no figura en swagger como patch
	public static Futuro<CancelacionAnticipada.SolicitudCancelacionAnticipadaEstado> patch(Contexto contexto, String nroOperacion) {
		return futuro(() -> CancelacionAnticipada.patch(contexto, nroOperacion));
	}

	// no figura en swagger como post
	public static Futuro<CancelacionAnticipada.SolicitudCancelacionAnticipadaEstado> post(Contexto contexto, String nroOperacion) {
		return futuro(() -> CancelacionAnticipada.post(contexto, nroOperacion));
	}

	/* ========== Plazo Fijo Cancelacion Anticipada Controller ========== */

	// GET /v1/plazosfijos/{nroOperacion}/cancelacionanticipada
	public static Futuro<CancelacionAnticipada> get(Contexto contexto, String nroOperacion) {
		return futuro(() -> CancelacionAnticipada.get(contexto, nroOperacion));
	}

	// GET /v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud
	public static Futuro<CancelacionAnticipada.SolicitudPrecancelar> getSolicitud(Contexto contexto, String nroOperacion) {
		return futuro(() -> CancelacionAnticipada.getSolicitud(contexto, nroOperacion));
	}

	// GET /v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud/estado
	public static Futuro<CancelacionAnticipada.SolicitudCancelacionAnticipadaEstado> getEstado(Contexto contexto, String nroOperacion) {
		return futuro(() -> CancelacionAnticipada.getEstado(contexto, nroOperacion));
	}

	// TODO: POST /v1/plazosfijos/cancelacionanticipada/solicitud

	// TODO: PATCH /v1/plazosfijos/cancelacionanticipada/solicitud

	/* ========== Pf Ganador Procrear Joven Controller ========== */

	// TODO: GET /v1/solicitudPrecancelar

	// TODO: PATCH /v1/solicitudPrecancelar/{idCobis}

	// TODO: PATCH /v1/solicitudPrecancelar/reversa/{idCobis}

	/* ========== Plazo Fijo Controller ========== */

	// TODO: GET /v1/{idCobis}

	// TODO: GET /v1/{idCobis}/bajaPlazoFijoAhorro

	// TODO: GET /v1/{idCobis}/forzadoPlazoFijo

	// TODO: GET /v1/{idCobis}/historicos

	// TODO: GET /v1/{idCobis}/modificacionPlazoFijoAhorro

	// GET /v1/planAhorro/cabecera
	public static Futuro<PlazosFijosLogros> getCabecera(Contexto contexto, String idCobis, String idPlanAhorro) {
		return futuro(() -> PlazosFijosLogros.getCabecera(contexto, idCobis, idPlanAhorro));
	}

	// GET /v1/planAhorro/detalle
	public static Futuro<PlazosFijosLogros.DetallePlazoFijoLogros> getDetalle(Contexto contexto, String idCobis, String planContratado, String secuencial) {
		return futuro(() -> PlazosFijosLogros.getDetalle(contexto, idCobis, planContratado, secuencial));
	}

	// TODO: POST /v1/plazoFijosMETA

	// TODO: GET /v1/plazosfijos

	// TODO: GET /v1/plazosfijos/{nropf}

	// TODO: PATCH /v1/plazosfijos/{nropf}

	// GET /v1/tasas
	public static Futuro<Tasas> getTasas(Contexto contexto, String idCliente, String secuencial, String canal) {
		return futuro(() -> Tasas.getTasas(contexto, idCliente, secuencial, canal));
	}

	// GET /v1/tasasPreferenciales
	public static Futuro<Tasas.TasasPreferenciales> getTasasPreferenciales(Contexto contexto, String idCliente) {
		return futuro(() -> Tasas.getTasasPreferenciales(contexto, idCliente));
	}

	// TODO: GET /v2/plazosfijos

	// TODO: GET /v2/tasas

	/* ========== Plazo Fijo Resumen Electronico Controller ========== */

	// TODO: GET /v1/cotizacion

	// TODO: GET /v1/detalle

	// TODO: GET /v1/operaciones

	// TODO: GET /v1/titular

	public static Futuro<TitularPlazoFijo> getTitularPf(Contexto contexto, String fechaDesde, String fechaHasta,String numeroCuenta) {
		return futuro(() -> TitularPlazoFijo.get(contexto, fechaDesde, fechaHasta, numeroCuenta));
	}
	/* ========== Plazo Fijo UVA Controller ========== */

	// TODO: GET /v1/plazoFijoUVA

	// TODO: GET /v1/plazoFijoUVA/{idCobis}/ganador

	// TODO: PATCH /v1/plazoFijoUVA/actualizacion/

	/* ========== Plazo Fijo Web Controller ========== */

	// POST /v1/plazosfijosweb
	public static Futuro<PlazoFijoWeb> crearPlazoFijoWeb(Contexto contexto, Persona persona, CuentaLink cuentaLink, BigDecimal monto, Integer plazo, String tipoOperacion) {
		return futuro(() -> PlazoFijoWeb.post(contexto, persona, cuentaLink, monto, plazo, tipoOperacion));
	}

	// TODO: PATCH /v1/plazosfijosweb

	// POST /v1/plazosfijosweb/simulacion
	public static Futuro<SimulacionPlazoFijoWeb> get(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion, String numeroSucursal) {
		return futuro(() -> SimulacionPlazoFijoWeb.post(contexto, monto, plazo, tipoOperacion, numeroSucursal));
	}

	public static Futuro<SimulacionPlazoFijoWeb> simulacionPlazoFijoWeb(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion, String numeroSucursal) {
		return futuro(() -> SimulacionPlazoFijoWeb.post(contexto, monto, plazo, tipoOperacion, numeroSucursal));
	}

	public static Futuro<SimulacionPlazoFijoWeb> post(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion) {
		return futuro(() -> SimulacionPlazoFijoWeb.post(contexto, monto, plazo, tipoOperacion));
	}

	public static Futuro<SimulacionPlazoFijoWeb> simulacionPlazoFijoWeb(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion) {
		return futuro(() -> SimulacionPlazoFijoWeb.post(contexto, monto, plazo, tipoOperacion));
	}
	
	////NUEVO PF
	
	public static Futuro<PlazoFijoList> getPlazoFijoList(Contexto contexto, String idcliente) {
		return futuro(() -> PlazoFijoList.get(contexto, idcliente));
	}
	
	public static Futuro<PlazoFijoDetalle> getPlazoFijoDetalle(Contexto contexto, String numeroBanco) {
		return futuro(() -> PlazoFijoDetalle.get(contexto, numeroBanco));
	}
	
	public static Futuro<PlazoFijo> nuevoPlazoFijo(Contexto contexto, PlazoFijoOB plazoFijo) {
		return futuro(() -> PlazosFijos.post(contexto, plazoFijo));
	}
	
	/* ========== CEDIPS Web Controller ========== */
	// GET /v1/cedips/{cuit}
	public static Futuro<Cedips> getCedips(Contexto contexto, String cuit) {
		return futuro(() -> Cedips.get(contexto, cuit));
	}
	// GET /v1/cedips/{cuit} + filtro (estado = ACTIVO-PENDIENTE)
	public static Futuro<Cedips> getCedipsRecibidos(Contexto contexto, String cuit) {
		return futuro(() -> Cedips.getRecibidos(contexto, cuit));
	}
	// GET /v1/cedips/{cedipId}/{cuit}/{fraccion}
	public static Futuro<Cedip> getDetalleCedip(Contexto contexto, String cedipId, String cuit, Integer fraccion) {
		return futuro(() -> Cedips.getDetalleCedip(contexto, cedipId, cuit, fraccion));
	}
	
	public static Futuro<List<SimulacionCedip>> simulacionCedip(Contexto contexto, BigDecimal monto, Integer plazo, String cuenta, String tipoOperacion, Integer idCliente, String capInteres, String moneda) {
		return futuro(() -> SimulacionCedip.get(contexto, monto, plazo, tipoOperacion, cuenta, idCliente, capInteres,moneda));
	}
	
	// TODO: POST /v1/plazoFijos
	public static Futuro<CedipNuevo> nuevoCedip(Contexto contexto, CedipOB cedip) {
		return futuro(() -> Cedips.post(contexto, cedip));
	}
	
	public static Futuro<ResponsePostTransmision> transmitirCedip(Contexto contexto, CedipAccionesOB cedipA) {
		return futuro(() -> Cedips.postTransmitir(contexto, cedipA));
	}
	
	public static Futuro<ResponsePostTransmision> aceptarCedip(Contexto contexto, Parametros parametros) {
		return futuro(() -> Cedips.postAdmitir(contexto, parametros));
	}
	
	public static Futuro<ResponsePostTransmision> rechazarCedip(Contexto contexto, Parametros parametros) {
		return futuro(() -> Cedips.postRepudiar(contexto, parametros));
	}
	
	public static Futuro<ResponsePostTransmision> anularTransmisionCedip(Contexto contexto, CedipAccionesOB cedipA) {
		return futuro(() -> Cedips.postAnularTransmision(contexto, cedipA));
	}
	
	public static Futuro<ResponsePostTransmision> modificarAcreditacionCbuCedip(Contexto contexto, CedipAccionesOB cedipA) {
		return futuro(() -> Cedips.putModificarAcreditacionCbu(contexto, cedipA));
	}
	
	public static Futuro<ResponsePostTransmision> depositarCedip(Contexto contexto, CedipAccionesOB cedipA) {
		return futuro(() -> Cedips.postDepositarCedip(contexto, cedipA));
	}

}
