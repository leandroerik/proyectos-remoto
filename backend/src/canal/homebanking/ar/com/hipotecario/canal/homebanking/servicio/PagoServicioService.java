package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBAplicacion;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class PagoServicioService {

	public static Map<TarjetaDebito, ApiResponse> pendientes(ContextoHB contexto) {
		Map<TarjetaDebito, ApiResponse> mapa = new LinkedHashMap<>();
		ExecutorService executorService = Concurrencia.executorService(contexto.tarjetasDebito());
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			executorService.submit(() -> {
				mapa.put(tarjetaDebito, pendientes(contexto, tarjetaDebito.numero()));
			});
		}
		Concurrencia.esperar(executorService, null);
		return mapa;
	}

	public static ApiResponse pendientes(ContextoHB contexto, String numeroTarjeta) {
		ApiRequest request = Api.request("LinkGetPagosPendientes", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos/pendientes", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroTarjeta);
	}

	public static void eliminarCachePendientes(ContextoHB contexto) {
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			Api.eliminarCache(contexto, "LinkGetPagosPendientes", contexto.idCobis(), tarjetaDebito.numero());
		}
	}

	public static Map<TarjetaDebito, ApiResponse> linkGetAdhesiones(ContextoHB contexto) {
		Map<TarjetaDebito, ApiResponse> mapa = new LinkedHashMap<>();
		ExecutorService executorService = Concurrencia.executorService(contexto.tarjetasDebito());
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			executorService.submit(() -> {
				mapa.put(tarjetaDebito, linkGetAdhesiones(contexto, tarjetaDebito.numero()));
			});
		}
		Concurrencia.esperar(executorService, null);
		return mapa;
	}

	public static ApiResponse linkPostAdhesion(ContextoHB contexto, String numeroTarjetaDebito, String codigoEnte, Boolean esBaseDeuda, String codigoLink) {
		if (!esBaseDeuda) {
			ApiRequest request = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
			request.path("numeroTarjeta", numeroTarjetaDebito);
			request.body("codigoEnte", codigoEnte);
			request.body("isBase", esBaseDeuda);
			request.body("usuarioLP", codigoLink);
			ApiResponse response = Api.response(request, contexto.idCobis(), numeroTarjetaDebito);
			return response;
		} else {
			if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_link_cpe", "prendido_link_cpe_cobis")) {
				ApiRequest request = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
				request.path("numeroTarjeta", numeroTarjetaDebito);
				request.body("codigoEnte", codigoEnte);
				request.body("isBase", esBaseDeuda);
				request.body("usuarioLP", codigoLink);
				request.query("validarcpe", "true");
				request.body("idEntidadEmisora", "0044");
				request.body("tipoTerminal", "74");
				request.body("cpe", codigoLink);
				ApiResponse response = Api.response(request, contexto.idCobis(), numeroTarjetaDebito, "cpe");
				return response;
			} else {
				ApiRequest request = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
				request.path("numeroTarjeta", numeroTarjetaDebito);
				request.body("codigoEnte", codigoEnte);
				request.body("isBase", esBaseDeuda);
				request.body("usuarioLP", codigoLink);
				ApiResponse response = Api.response(request, contexto.idCobis(), numeroTarjetaDebito);
				return response;
			}
		}
	}

//	public static ApiResponse linkPostAdhesion(Contexto contexto, String numeroTarjetaDebito, String codigoEnte, Boolean esBaseDeuda, String codigoLink) {
//		ApiRequest request = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
//		request.path("numeroTarjeta", numeroTarjetaDebito);
//		request.body("codigoEnte", codigoEnte);
//		request.body("isBase", esBaseDeuda);
//		request.body("usuarioLP", codigoLink);
//
//		ApiResponse response = Api.response(request, contexto.idCobis(), numeroTarjetaDebito);
//
//		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_link_cpe", "prendido_link_cpe_cobis") && esBaseDeuda) {
//			if (response.hayError() && "82".equals(response.string("codigo"))) {
//				ApiRequest requestReintento = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
//				requestReintento.path("numeroTarjeta", numeroTarjetaDebito);
//				requestReintento.body("codigoEnte", codigoEnte);
//				requestReintento.body("isBase", esBaseDeuda);
//				requestReintento.body("usuarioLP", codigoLink);
//				requestReintento.query("validarcpe", "true");
//				requestReintento.body("idEntidadEmisora", "0044");
//				requestReintento.body("tipoTerminal", "74");
//				requestReintento.body("cpe", codigoLink);
//				response = Api.response(requestReintento, contexto.idCobis(), numeroTarjetaDebito, "cpe");
//
//				if (response.hayError() && "151".equals(response.string("codigo"))) {
//					if (codigoLink != null && codigoLink.length() > 3 && codigoLink.startsWith(codigoEnte)) {
//						ApiRequest requestReintentoDelReintento = Api.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
//						requestReintentoDelReintento.path("numeroTarjeta", numeroTarjetaDebito);
//						requestReintentoDelReintento.body("codigoEnte", codigoEnte);
//						requestReintentoDelReintento.body("isBase", esBaseDeuda);
//						requestReintentoDelReintento.body("usuarioLP", codigoLink.substring(3));
//						response = Api.response(requestReintentoDelReintento, contexto.idCobis(), numeroTarjetaDebito, "cpe");
//					}
//				}
//			}
//		}
//
//		return response;
//	}

	public static ApiResponse linkGetAdhesiones(ContextoHB contexto, String numeroTarjetaDebito) {
		ApiRequest request = Api.request("LinkGetAdhesiones", "link", "GET", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroTarjetaDebito);
	}

	public static void eliminarCacheLinkGetAdhesiones(ContextoHB contexto) {
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			Api.eliminarCache(contexto, "LinkGetAdhesiones", contexto.idCobis(), tarjetaDebito.numero());
		}
	}

	public static ApiResponse linkGetRubros(ContextoHB contexto) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
		if (tarjetaDebito != null) {
			return linkGetRubros(contexto, tarjetaDebito.numero());
		}
		return null;
	}

	public static ApiResponse linkGetRubros(ContextoHB contexto, String numeroTarjetaDebito) {
		ApiRequest request = Api.request("LinkGetRubros", "link", "GET", "/v1/servicios/{numeroTarjeta}/rubros", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroTarjetaDebito);
	}
}
