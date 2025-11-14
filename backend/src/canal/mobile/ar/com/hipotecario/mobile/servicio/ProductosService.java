package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class ProductosService {

	public static ApiResponseMB productos(ContextoMB contexto) {
		return productos(contexto, true);
	}

	public static ApiResponseMB productos(ContextoMB contexto, Boolean permitirSinSesion) {
		// emm-20190618-desde-->hago un arreglo para que si viene nulo el cobis, que no
		// llame a la posicion consolidada.
		if (contexto.idCobis() == null) {
			return new ApiResponseMB(null, 501, "Cobis nulo");
		}
		// emm-20190618-hasta

		String version = ConfigMB.string("mb_version_posicionconsolidada", "/v3");
		ApiRequestMB request = ApiMB.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cancelados", "false");
		request.query("firmaconjunta", "false");
		request.query("firmantes", "false");
		request.query("adicionales", "true");
		request.query("tipoestado", "vigente");
		request.permitirSinLogin = permitirSinSesion;
		request.cacheSesion = true;
		request.cache204 = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB productosConCancelados(ContextoMB contexto) {
		// Solo lo uso para un plazos fijos cancelados
		if (contexto.idCobis() == null) {
			return new ApiResponseMB(null, 501, "Cobis nulo");
		}

		String version = ConfigMB.string("mb_version_posicionconsolidada", "/v3");
		ApiRequestMB request = ApiMB.request("ProductosConCancelados", "productos", "GET", version + "/posicionconsolidada", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cancelados", "true");
		request.query("firmaconjunta", "false");
		request.query("firmantes", "false");
		request.query("adicionales", "true");
		request.query("tipoestado", "vigente");
		request.permitirSinLogin = false;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static void eliminarCacheProductos(ContextoMB contexto) {
		ApiMB.eliminarCache(contexto, "Productos", contexto.idCobis());
	}

	public static ApiResponseMB getCampania(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("CampaniasVigentesWOCI", "productos", "GET", "/v1/campania", contexto);
		request.query("idtributaria", contexto.persona().cuit());
		request.permitirSinLogin = false;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB getAcreditaciones(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("Acreditaciones", "productos", "GET", "/v1/clientes/{id}/acreditaciones", contexto);
		request.path("id", contexto.idCobis());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB integrantesProducto(ContextoMB contexto, String cuenta) {

		ApiRequestMB request = ApiMB.request("IntegrantesProducto", "productos", "GET", "/v1/{cuenta}/integrantes", contexto);
		request.path("cuenta", cuenta);
		request.permitirSinLogin = false;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB relacionClienteProducto(ContextoMB contexto, Objeto relacion) {
		ApiRequestMB request = ApiMB.request("relaciones", "productos", "POST", "/v1/relaciones", contexto);
		request.body(relacion);
		return ApiMB.response(request, contexto.idCobis());
	}
}
