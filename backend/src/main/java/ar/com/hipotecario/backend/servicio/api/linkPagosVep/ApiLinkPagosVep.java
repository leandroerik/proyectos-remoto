package ar.com.hipotecario.backend.servicio.api.linkPagosVep;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;

//http://api-linkpagosvep-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiLinkPagosVep extends Api {

	// TODO: No me figura en este swagger
	// "LinkPostPagoVeps", "veps", "POST", "/v1/veps"
	public static Futuro<VepsPagados.Vep> pagarVep(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String numeroVep, String idTributarioContribuyente, BigDecimal importe, String token, Cuenta cuenta) {
		return futuro(() -> VepsPendientes.post(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, numeroVep, idTributarioContribuyente, importe, token, cuenta));
	}

	/* ========== Contribuyente Controller ========== */

	// POST /v1/contribuyentes
	public static Futuro<Boolean> agregarContribuyente(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String idTributarioContribuyente, String referencia) {
		return futuro(() -> Contribuyentes.post(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, idTributarioContribuyente, referencia));
	}

	// DELETE /v1/contribuyentes
	public static Futuro<Boolean> eliminarContribuyente(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String idTributarioContribuyente) {
		return futuro(() -> Contribuyentes.delete(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, idTributarioContribuyente));
	}

	// GET /v1/contribuyentes/{idTributarioCliente}
	public static Futuro<Contribuyentes> contribuyentes(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String maxCantidad, String numeroTarjeta, String pagina) {
		return futuro(() -> Contribuyentes.get(contexto, idTributarioCliente, idTributarioEmpresa, maxCantidad, numeroTarjeta, pagina));
	}

	/* ========== Pago Veps Controller ========== */

	// TODO: GET /v1/pagoVeps

	// TODO: POST /v1/pagoVeps

	/* ========== Token Controller ========== */

	// POST /v1/tokenAFIP
	public static Futuro<TokenAfip> tokenAfip(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta) {
		return futuro(() -> TokenAfip.post(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta));
	}

	/* ========== Vep Controller ========== */

	// DELETE /v1/veps
	public static Futuro<ApiObjeto> eliminarVep(Contexto contexto, String idTributarioCliente, String numeroTarjeta, String numeroVep) {
		return futuro(() -> VepsPendientes.delete(contexto, idTributarioCliente, numeroTarjeta, numeroVep));
	}

	// GET /v1/veps/{idTributarioCliente}/pagados
	public static Futuro<VepsPagados> vepsPagados(Contexto contexto, Fecha fechaDesde, String idTributarioCliente, String idTributarioContribuyente, String idTributarioOriginante, String idTributarioEmpresa, String maxCantidad, String numeroTarjeta, String numeroVep, String tipoConsultaLink) {
		return futuro(() -> VepsPagados.get(contexto, fechaDesde, idTributarioCliente, idTributarioContribuyente, idTributarioOriginante, idTributarioEmpresa, maxCantidad, numeroTarjeta, numeroVep, tipoConsultaLink));
	}

	public static Futuro<VepsPagados> vepsPagados(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		return futuro(() -> VepsPagados.get(contexto, fechaDesde, fechaHasta, idTributarioCliente, idTributarioContribuyente, idTributarioEmpresa, idTributarioOriginante, maxCantidad, numeroTarjeta, numeroVep, pagina, tipoConsultaLink));
	}

	// GET /v1/veps/{idTributarioCliente}/pendientes
	public static Futuro<VepsPendientes> vepPendientes(Contexto contexto, String idTributarioCliente, String idTributarioContribuyente, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String tipoConsultaLink) {
		return futuro(() -> VepsPendientes.get(contexto, idTributarioCliente, idTributarioContribuyente, idTributarioOriginante, maxCantidad, numeroTarjeta, tipoConsultaLink));
	}

	public static Futuro<VepsPendientes> vepPendientes(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		return futuro(() -> VepsPendientes.get(contexto, fechaDesde, fechaHasta, idTributarioCliente, idTributarioContribuyente, idTributarioEmpresa, idTributarioOriginante, maxCantidad, numeroTarjeta, numeroVep, pagina, tipoConsultaLink));
	}

	public static Futuro<VepsPendientes> vepPendientesNuevo(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		return futuro(() -> VepsPendientes.getNuevo(contexto, fechaDesde, fechaHasta, idTributarioCliente, idTributarioContribuyente, idTributarioEmpresa, idTributarioOriginante, maxCantidad, numeroTarjeta, numeroVep, pagina, tipoConsultaLink));
	}

}
