package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestMora {
	public static ApiResponse getProductosEnMora(ContextoHB contexto) {
		ApiRequest request = Api.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
		request.query("idClienteCobis", contexto.idCobis());
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse getMoraCliente(ContextoHB contexto) {
		ApiRequest request = Api.request("MorasCliente", "moras", "GET", "/v1/cliente/{idClienteCobis}/moras", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse getMoraCasos(ContextoHB contexto) {
		ApiRequest request = Api.request("MorasCasosCliente", "moras", "GET", "/v1/cliente/{idClienteCobis}/casos", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse tieneVentaCarteraPre2010(ContextoHB contexto) {
		ApiRequest request = Api.request("MorasVentaCartera2010", "moras", "GET", "/v1/cliente/{idClienteCobis}", contexto);
		request.path("idClienteCobis", contexto.idCobis());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse getProductosEnMoraDetalles(ContextoHB contexto, String ctaId) {
		ApiRequest request = Api.request("MorasDetalle", "moras", "GET", "/v1/productosEnMoraDetalles", contexto);
		request.query("cta_id", ctaId);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, ctaId);
	}

	public static ApiResponse consultarProductosMoraCache(ContextoHB contexto) {
		ApiRequest requestMora = Api.request("MorasCache", "moras", "GET", "/v1/productosEnMora", contexto);
		requestMora.query("idClienteCobis", contexto.idCobis());
		requestMora.cacheSesion = true;
		return Api.response(requestMora, contexto.idCobis());
	}

	public static ApiResponse getProductosEnMoraDetallesCache(ContextoHB contexto, String ctaId) {
		ApiRequest request = Api.request("MorasDetalleCache", "moras", "GET", "/v1/productosEnMoraDetalles", contexto);
		request.query("cta_id", ctaId);
		request.cacheSesion = true;
		return Api.response(request, ctaId);
	}

	public static ApiResponse autogestionMora(ContextoHB contexto) {
		ApiRequest request = Api.request("MorasAutogestion", "moras", "POST", "/v1/autogestionMora", contexto);

		String montoPago = contexto.parametros.string("montoPago");
		try {
			if (!montoPago.isEmpty() && montoPago.contains(",")) {
				montoPago = montoPago.replace(".", "");
				montoPago = montoPago.replace(",", ".");
			}
		} catch (Exception e) {
		}

		request.body("canalPago", "PFAC");
		request.body("loginUsuario", "USU_HB");
		request.body("gestionTac", "AUTD");
		request.body("gestionTrp", contexto.parametros.string("tipo"));
		request.body("cliente", contexto.idCobis());
		request.body("fechaPago", contexto.parametros.string("fechaPago"));
		request.body("moneda", contexto.parametros.string("moneda").trim());
		if (!"SCON".equals(contexto.parametros.string("tipo"))) {
			request.body("montoPago", montoPago);
		}
		request.body("nroOperacion", contexto.parametros.string("nroOperacion"));
		request.body("telefono", contexto.parametros.string("telefono"));
		request.body("areaTelefono", contexto.parametros.string("areaTelefono"));
		request.body("tipoTelefono", contexto.parametros.string("tipoTelefono"));
		request.body("horarioContacto", contexto.parametros.string("horarioContacto"));
		return Api.response(request);
	}

}
