package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestMora {

	public static ApiResponseMB getProductosEnMora(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
		request.query("idClienteCobis", contexto.idCobis());
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarProductosMoraCache(ContextoMB contexto) {
		ApiRequestMB requestMora = ApiMB.request("MorasCache", "moras", "GET", "/v1/productosEnMora", contexto);
		requestMora.query("idClienteCobis", contexto.idCobis());
		requestMora.cacheSesion = true;
		return ApiMB.response(requestMora, contexto.idCobis());
	}

	public static ApiResponseMB getProductosEnMoraDetallesCache(ContextoMB contexto, String ctaId) {
		ApiRequestMB request = ApiMB.request("MorasDetalleCache", "moras", "GET", "/v1/productosEnMoraDetalles", contexto);
		request.query("cta_id", ctaId);
		request.cacheSesion = true;
		return ApiMB.response(request, ctaId);
	}

	public static ApiResponseMB getProductosEnMoraDetalles(ContextoMB contexto, String ctaId) {
		ApiRequestMB request = ApiMB.request("MorasDetalle", "moras", "GET", "/v1/productosEnMoraDetalles", contexto);
		request.query("cta_id", ctaId);
		request.permitirSinLogin = true;
		return ApiMB.response(request, ctaId);
	}

	public static ApiResponseMB getProductosEnMoraDetallesNoCache(ContextoMB contexto, String ctaId) {
		ApiRequestMB request = ApiMB.request("MorasDetalleNoCache", "moras", "GET", "/v1/productosEnMoraDetalles", contexto);
		request.query("cta_id", ctaId);
		request.permitirSinLogin = true;
		return ApiMB.response(request, ctaId);
	}

	public static ApiResponseMB autogestionMora(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("MorasAutogestion", "moras", "POST", "/v1/autogestionMora", contexto);

		String montoPago = contexto.parametros.string("montoPago");

		try {
			if (!montoPago.isEmpty() && montoPago.contains(",")) {
				montoPago = montoPago.replace(".", "");
				montoPago = montoPago.replace(",", ".");
			}
		} catch (Exception e) {
		}

		request.body("canalPago", "PFAC");
		request.body("loginUsuario", "USU_APP");
		request.body("gestionTac", "AUTD");
		request.body("gestionTrp", contexto.parametros.string("tipo"));
		request.body("cliente", contexto.idCobis());
		request.body("fechaPago", contexto.parametros.string("fechaPago"));
		request.body("moneda", contexto.parametros.string("moneda"));
		request.body("montoPago", montoPago);
		request.body("nroOperacion", contexto.parametros.string("nroOperacion"));
		request.body("telefono", contexto.parametros.string("telefono"));
		request.body("areaTelefono", contexto.parametros.string("areaTelefono"));
		request.body("tipoTelefono", contexto.parametros.string("tipoTelefono"));
		request.body("horarioContacto", contexto.parametros.string("horarioContacto"));
		return ApiMB.response(request);
	}

	public static ApiResponseMB getMoraCliente(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("MorasCliente", "moras", "GET", "/v1/cliente/{idClienteCobis}/moras", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB getMoraCasos(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("MorasCasosCliente", "moras", "GET", "/v1/cliente/{idClienteCobis}/casos", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB tieneVentaCarteraPre2010(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("MorasVentaCartera2010", "moras", "GET", "/v1/cliente/{idClienteCobis}", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

}
