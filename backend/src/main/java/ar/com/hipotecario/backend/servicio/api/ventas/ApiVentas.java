package ar.com.hipotecario.backend.servicio.api.ventas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.Integrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.NuevoIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion.NuevaResolucion;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud.NuevaSolicitud;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudCajaAhorro.NuevaSolicitudCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaDebito.NuevaSolicitudTarjetaDebito;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor;

public class ApiVentas extends Api {

	/* ========== CONSTANTES ========== */
	public static String API = "ventas";
	public static String DATOS = "Datos";
	public static String ERRORES = "Errores";
	public static String X_HANDLE = "X-Handle";

	/* ========== SERVICIOS ========== */

	// Solicitudes
	public static Futuro<Solicitudes> solicitudes(Contexto contexto, String cuit) {
		return futuro(() -> Solicitudes.get(contexto, cuit));
	}

	// Solicitud
	public static Futuro<Solicitud> solicitud(Contexto contexto, String idSolicitud) {
		return futuro(() -> Solicitud.get(contexto, idSolicitud));
	}

	public static Futuro<Solicitud> crearSolicitud(Contexto contexto, NuevaSolicitud nuevaSolicitud) {
		return futuro(() -> Solicitud.post(contexto, nuevaSolicitud));
	}

	public static Futuro<Solicitud> actualizarSolicitud(Contexto contexto, Solicitud solicitud) {
		return futuro(() -> Solicitud.put(contexto, solicitud));
	}

	public static Futuro<Solicitud> finalizarSolicitud(Contexto contexto, String idSolicitud) {
		return futuro(() -> Solicitud.getEstado(contexto, idSolicitud, "finalizar"));
	}

	// Integrantes
	public static Futuro<Integrantes> integrantes(Contexto contexto, String idSolicitud) {
		return futuro(() -> Integrantes.get(contexto, idSolicitud));
	}

	public static Futuro<Integrante> crearIntegrante(Contexto contexto, String idSolicitud, NuevoIntegrante nuevoIntegrante) {
		return futuro(() -> Integrantes.post(contexto, idSolicitud, nuevoIntegrante));
	}

	// SolicitudPaquete
	public static Futuro<SolicitudPaquete> solicitudPaquete(Contexto contexto, String numeroSolicitud, String idPaquete) {
		return futuro(() -> SolicitudPaquete.get(contexto, numeroSolicitud, idPaquete));
	}

	public static Futuro<SolicitudPaquete> crearPaquete(Contexto contexto, String numeroSolicitud, SolicitudPaquete nuevoPaquete) {
		return futuro(() -> SolicitudPaquete.post(contexto, numeroSolicitud, nuevoPaquete));
	}

	public static Futuro<SolicitudPaquete> deletePaquete(Contexto contexto, String numeroSolicitud, String idPaquete) {
		return futuro(() -> SolicitudPaquete.delete(contexto, numeroSolicitud, idPaquete));
	}

	// Resoluciones
	public static Futuro<Resolucion> resolucionesGet(Contexto contexto, String numeroSolicitud) {
		return futuro(() -> Resolucion.get(contexto, numeroSolicitud));
	}

	public static Futuro<Resolucion> resolucionesPut(Contexto contexto, String numeroSolicitud, NuevaResolucion nuevaResolucion) {
		return futuro(() -> Resolucion.put(contexto, numeroSolicitud, nuevaResolucion));
	}

	// Tarjeta credito
	public static Futuro<SolicitudTarjetaCredito> tarjetaCredito(Contexto contexto, String numeroSolicitud, String idTarjeta) {
		return futuro(() -> SolicitudTarjetaCredito.get(contexto, numeroSolicitud, idTarjeta));
	}

	// Tarjeta débito
	// -GET
	public static Futuro<SolicitudTarjetaDebito> tarjetaDebito(Contexto contexto, String numeroSolicitud, String idProducto) {
		return futuro(() -> SolicitudTarjetaDebito.get(contexto, numeroSolicitud, idProducto));
	}

	// -POST
	public static Futuro<SolicitudTarjetaDebito> solicitudTarjetaDebito(Contexto contexto, String idSolicitud, NuevaSolicitudTarjetaDebito tarjetaDebito) {
		return futuro(() -> SolicitudTarjetaDebito.post(contexto, idSolicitud, tarjetaDebito));
	}

	public static Futuro<SolicitudTarjetaDebito> solicitudTarjetaDebitoStand(Contexto contexto, String idSolicitud, NuevaSolicitudTarjetaDebito tarjetaDebito) {
		return futuro(() -> SolicitudTarjetaDebito.postStand(contexto, idSolicitud, tarjetaDebito));
	}

	public static Futuro<SolicitudTarjetaDebito> solicitudTarjetaDebitoStandV2(Contexto contexto, String idSolicitud, SolicitudTarjetaDebito tarjetaDebito) {
		return futuro(() -> SolicitudTarjetaDebito.postStandV2(contexto, idSolicitud, tarjetaDebito));
	}

	// PUT
	public static Futuro<SolicitudTarjetaDebito> actualizarTarjetaDebito(Contexto contexto, String idSolicitud, String idProducto, SolicitudTarjetaDebito tarjetaDebito) {
		return futuro(() -> SolicitudTarjetaDebito.put(contexto, idSolicitud, idProducto, tarjetaDebito));
	}

	// Caja Ahorro

	// GET
	public static Futuro<SolicitudCajaAhorro> cajaAhorro(Contexto contexto, String idSolicitud, String idProducto) {
		return futuro(() -> SolicitudCajaAhorro.get(contexto, idSolicitud, idProducto));
	}

	// -POST
	public static Futuro<SolicitudCajaAhorro> solicitudCajaAhorro(Contexto contexto, String idSolicitud, NuevaSolicitudCajaAhorro solicitud) {
		return futuro(() -> SolicitudCajaAhorro.post(contexto, idSolicitud, solicitud));
	}

	public static Futuro<SolicitudCajaAhorro> solicitudCajaAhorroStand(Contexto contexto, String idSolicitud, NuevaSolicitudCajaAhorro solicitud) {
		return futuro(() -> SolicitudCajaAhorro.postStand(contexto, idSolicitud, solicitud));
	}

	public static Futuro<SolicitudCajaAhorro> solicitudCajaAhorroStandV2(Contexto contexto, String idSolicitud, SolicitudCajaAhorro solicitud) {
		return futuro(() -> SolicitudCajaAhorro.postStandV2(contexto, idSolicitud, solicitud));
	}

	// PUT
	public static Futuro<SolicitudCajaAhorro> actualizarCajaAhorro(Contexto contexto, String idSolicitud, String idProducto, SolicitudCajaAhorro solicitud) {
		return futuro(() -> SolicitudCajaAhorro.put(contexto, idSolicitud, idProducto, solicitud));
	}

	// Indicador
	public static Futuro<Indicador> sujetoPasibleCredito(Contexto contexto, String numeroDocumento, String idTributario, String sexo, String tipoDocumento, String tipoTributario) {
		return futuro(() -> Indicador.get(contexto, numeroDocumento, idTributario, sexo, tipoDocumento, tipoTributario));
	}

	/* ========== METODOS RESPONSE ========== */
	public static Boolean errorVentas(ApiResponse response) {
		return !response.http(200) || !response.objetos(ERRORES).isEmpty() || response.objetos("Datos").size() != 1;
	}

	public static Boolean errorInversiones(ApiResponse response) {
		return response.codigoHttp < 200 || response.codigoHttp > 300;
	}
	
	// oferta motor scoring
	public static Futuro<MotorScoringSimulacion> getOfertaMotorScoring(Contexto contexto, String numeroSolicitud, SolicitudMotor datos, boolean esTcv) {
		return futuro(() -> MotorScoringSimulacion.get(contexto, numeroSolicitud, datos, esTcv));
	}
}
