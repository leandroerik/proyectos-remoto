package ar.com.hipotecario.backend.servicio.api.link;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.link.Pagos.Pago;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;

//http://api-link-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiLink extends Api {

	/* ========== Punto Efectivo Controller ========== */

	// TODO: GET /v1/puntoefectivo/{numeroTarjeta}/datosodes

	// GET /v1/puntoefectivo/{numeroTarjeta}/documentos
	public static Futuro<OrdenesExtraccion.Documentos> ordenesExtraccionDocumentos(Contexto contexto, String numeroTarjetaDebito, String tipoDocumento) {
		return futuro(() -> OrdenesExtraccion.getDocumentos(contexto, numeroTarjetaDebito, tipoDocumento));
	}

	// POST /v1/puntoefectivo/{numeroTarjeta}/ode
	public static Futuro<OrdenesExtraccion.RespuestaOk> postOrdenesExtraccion(Contexto contexto, String numeroTarjeta, BigDecimal importe, String numeroDocumento, String nombreCompleto, Cuenta cuenta) {
		return futuro(() -> OrdenesExtraccion.post(contexto, numeroTarjeta, importe, numeroDocumento, nombreCompleto, cuenta));
	}

	// DELETE /v1/puntoefectivo/{numeroTarjeta}/ode
	public static Futuro<OrdenesExtraccion.RespuestaOk> deleteOrdenesExtraccion(Contexto contexto, String numeroTarjetaDebito, String idODE) {
		return futuro(() -> OrdenesExtraccion.delete(contexto, numeroTarjetaDebito, idODE));
	}

	// GET /v1/puntoefectivo/{numeroTarjeta}/odes
	public static Futuro<OrdenesExtraccion> ordenesExtraccion(Contexto contexto, String numeroTarjetaDebito, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> OrdenesExtraccion.get(contexto, numeroTarjetaDebito, fechaDesde, fechaHasta));
	}

	// GET /v1/puntoefectivo/{numeroTarjeta}/referencias
	public static Futuro<OrdenesExtraccion.Referencias> ordenesExtraccionReferencias(Contexto contexto, String numeroTarjetaDebito) {
		return futuro(() -> OrdenesExtraccion.getReferencias(contexto, numeroTarjetaDebito));
	}

	// GET /v1/puntoefectivo/{numeroTarjeta}/tiposdocumentos
	public static Futuro<OrdenesExtraccion.TiposDocumentos> ordenesExtraccionTiposDocumentos(Contexto contexto, String numeroTarjetaDebito) {
		return futuro(() -> OrdenesExtraccion.getTiposDocumentos(contexto, numeroTarjetaDebito));
	}

	/* ========== Adhesion Servicio Controller ========== */

	// GET /v1/servicios/{numeroTarjeta}/adhesiones
	public static Futuro<LinkAdhesiones> getAdhesiones(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> LinkAdhesiones.get(contexto, numeroTarjeta));
	}

	// POST /v1/servicios/{numeroTarjeta}/adhesiones
	public static Futuro<LinkAdhesiones.RespuestaOk> crearAdhesiones(Contexto contexto, String numeroTarjeta, String codigoEnte, Boolean esBaseDeuda, String usuarioLP) {
		return futuro(() -> LinkAdhesiones.post(contexto, numeroTarjeta, codigoEnte, esBaseDeuda, usuarioLP));
	}

	// DELETE /v1/servicios/{numeroTarjeta}/adhesiones
	public static Futuro<LinkAdhesiones.RespuestaOk> eliminarAdhesiones(Contexto contexto, String numeroTarjeta, String codigoAdhesion) {
		return futuro(() -> LinkAdhesiones.delete(contexto, numeroTarjeta, codigoAdhesion));
	}

	/* ========== Pago Servicio Controller ========== */

	// GET /v1/servicios/{numeroTarjeta}/pagos
	public static Futuro<Pago> pagos(Contexto contexto, String numeroTarjeta, String usuarioLP, String codigoEnte, String fechaDesde, String fechaHasta) {
		return futuro(() -> Pagos.get(contexto, numeroTarjeta, usuarioLP, codigoEnte, fechaDesde, fechaHasta));
	}

	// POST /v1/servicios/{numeroTarjeta}/pagos
	public static Futuro<Pagos.ResponsePost> crearPago(Contexto contexto, String numeroTarjeta, String idMoneda, String numeroCuenta, String tipoCuenta, String codigoConcepto, String codigoEnte, String identificadorPago, String referencia, String usuarioLP, String importe, String idBase) {
		return futuro(() -> Pagos.post(contexto, numeroTarjeta, idMoneda, numeroCuenta, tipoCuenta, codigoConcepto, codigoEnte, identificadorPago, referencia, usuarioLP, importe, idBase));
	}

	// GET /v1/servicios/{numeroTarjeta}/pagos/pendientes
	public static Futuro<Pagos> pagosPendientes(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> Pagos.getPendientes(contexto, numeroTarjeta));
	}
	// GET /v2/servicios/{numeroTarjeta}/pagos/pendientes
	public static Futuro<PagosOB> pagosPendientesOB(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> PagosOB.getPendientes(contexto, numeroTarjeta));
	}

	// GET /v1/servicios/pagos/status
	public static Futuro<Pago> pagosStatus(Contexto contexto, String identificadorPago, String timeStampTransaccion) {
		return futuro(() -> Pagos.getStatus(contexto, identificadorPago, timeStampTransaccion));
	}


	/* ========== Link Controller ========== */

	// POST /v1/servicios/tarjetaVirtual
	public static Futuro<TarjetaVirtual> tarjetaVirtual(Contexto contexto, String idEmpresa) {
		return futuro(() -> TarjetaVirtual.post(contexto, idEmpresa));
	}

	// GET /v1/servicios/{numeroTarjeta}/entes/{idRubro}
	public static Futuro<Entes> entes(Contexto contexto, String numeroTarjeta, String idRubro) {
		return futuro(() -> Entes.get(contexto, numeroTarjeta, idRubro));
	}

	// GET /v1/servicios/{numeroTarjeta}/rubros
	public static Futuro<Rubros> rubros(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> Rubros.get(contexto, numeroTarjeta));
	}

	/* ========== Verificar Link Controller ========== */

	// POST /v1/verificacion
	public static Futuro<Verificaciones> verificaciones(Contexto contexto, String cardId, String pin) {
		return futuro(() -> Verificaciones.post(contexto, cardId, pin));
	}

	// TODO: POST /v2/verificacion

	/* ========== Token Afip Controller ========== */

	// POST /v1/tokenAFIP
	public static Futuro<TokenAFIP> tokenAFIP(Contexto contexto, String cliente, String empresa, String numeroTarjetaDebito) {
		return futuro(() -> TokenAFIP.post(contexto, cliente, empresa, numeroTarjetaDebito));
	}

	/* ========== Vep Controller ========== */

	// TODO: GET /v1/{idTributarioCliente}/pendientes

}
