package ar.com.hipotecario.mobile.servicio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class PagoServicioService {

	public static Map<TarjetaDebito, ApiResponseMB> pendientes(ContextoMB contexto) {
		Map<TarjetaDebito, ApiResponseMB> mapa = new LinkedHashMap<>();
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

	public static ApiResponseMB pendientes(ContextoMB contexto, String numeroTarjeta) {
		ApiRequestMB request = ApiMB.request("LinkGetPagosPendientes", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos/pendientes", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroTarjeta);
	}

	public static void eliminarCachePendientes(ContextoMB contexto) {
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			ApiMB.eliminarCache(contexto, "LinkGetPagosPendientes", contexto.idCobis(), tarjetaDebito.numero());
		}
	}

	public static Map<TarjetaDebito, ApiResponseMB> linkGetAdhesiones(ContextoMB contexto) {
		Map<TarjetaDebito, ApiResponseMB> mapa = new LinkedHashMap<>();
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

	public static ApiResponseMB linkPostAdhesion(ContextoMB contexto, String numeroTarjetaDebito, String codigoEnte, Boolean esBaseDeuda, String codigoLink) {
		if (!esBaseDeuda) {
			ApiRequestMB request = ApiMB.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
			request.path("numeroTarjeta", numeroTarjetaDebito);
			request.body("codigoEnte", codigoEnte);
			request.body("isBase", esBaseDeuda);
			request.body("usuarioLP", codigoLink);
			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), numeroTarjetaDebito);
			return response;
		} else {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_link_cpe", "prendido_link_cpe_cobis")) {
				ApiRequestMB request = ApiMB.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
				request.path("numeroTarjeta", numeroTarjetaDebito);
				request.body("codigoEnte", codigoEnte);
				request.body("isBase", esBaseDeuda);
				request.body("usuarioLP", codigoLink);
				request.query("validarcpe", "true");
				request.body("idEntidadEmisora", "0044");
				request.body("tipoTerminal", "74");
				request.body("cpe", codigoLink);
				ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), numeroTarjetaDebito, "cpe");
				return response;
			} else {
				ApiRequestMB request = ApiMB.request("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
				request.path("numeroTarjeta", numeroTarjetaDebito);
				request.body("codigoEnte", codigoEnte);
				request.body("isBase", esBaseDeuda);
				request.body("usuarioLP", codigoLink);
				ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), numeroTarjetaDebito);
				return response;
			}
		}
	}

	public static ApiResponseMB linkGetAdhesiones(ContextoMB contexto, String numeroTarjetaDebito) {
		ApiRequestMB request = ApiMB.request("LinkGetAdhesiones", "link", "GET", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroTarjetaDebito);
	}

	public static void eliminarCacheLinkGetAdhesiones(ContextoMB contexto) {
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			ApiMB.eliminarCache(contexto, "LinkGetAdhesiones", contexto.idCobis(), tarjetaDebito.numero());
		}
	}

	public static ApiResponseMB linkGetRubros(ContextoMB contexto) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
		if (tarjetaDebito != null) {
			return linkGetRubros(contexto, tarjetaDebito.numero());
		}
		return null;
	}

	public static ApiResponseMB linkGetRubros(ContextoMB contexto, String numeroTarjetaDebito) {
		ApiRequestMB request = ApiMB.request("LinkGetRubros", "link", "GET", "/v1/servicios/{numeroTarjeta}/rubros", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroTarjetaDebito);
	}
}
