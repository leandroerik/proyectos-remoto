package ar.com.hipotecario.canal.homebanking.servicio;


import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class ProductosService {

	public static ApiResponse productos(ContextoHB contexto) {
		return productos(contexto, true);
	}

	public static ApiResponse productos(ContextoHB contexto, Boolean permitirSinSesion) {
		// emm-20190618-desde-->hago un arreglo para que si viene nulo el cobis, que no
		// llame a la posicion consolidada.
		if (contexto.idCobis() == null) {
			return new ApiResponse(null, 501, "Cobis nulo");
		}
		// emm-20190618-hasta

		String version = ConfigHB.string("hb_version_posicionconsolidada", "/v3");
		ApiRequest request = Api.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cancelados", "false");
		request.query("firmaconjunta", "false");
		request.query("firmantes", "false");
		request.query("adicionales", "true");
		request.query("tipoestado", "vigente");
		request.permitirSinLogin = permitirSinSesion;
		request.cacheSesion = true;
		request.cache204 = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse productosConCancelados(ContextoHB contexto) {
		// Solo lo uso para un plazos fijos cancelados
		if (contexto.idCobis() == null) {
			return new ApiResponse(null, 501, "Cobis nulo");
		}

		String version = ConfigHB.string("hb_version_posicionconsolidada", "/v3");
		ApiRequest request = Api.request("ProductosConCancelados", "productos", "GET", version + "/posicionconsolidada", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cancelados", "true");
		request.query("firmaconjunta", "false");
		request.query("firmantes", "false");
		request.query("adicionales", "true");
		request.query("tipoestado", "todos");
		request.permitirSinLogin = false;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse integrantesProducto(ContextoHB contexto, String cuenta) {

		ApiRequest request = Api.request("IntegrantesProducto", "productos", "GET", "/v1/{cuenta}/integrantes", contexto);
		request.path("cuenta", cuenta);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse integrantesProductoCuentaCotitular(ContextoHB contexto, String cuenta) {

		ApiRequest request = Api.request("IntegrantesProducto", "productos", "GET", "/v1/{cuenta}/integrantes", contexto);
		request.path("cuenta", cuenta);
		request.permitirSinLogin = true;
		request.cacheSesion = false;
		return Api.response(request, contexto.idCobis());
	}

	public static void eliminarCacheProductos(ContextoHB contexto) {
		try {
			Api.eliminarCache(contexto, "Productos", contexto.idCobis());
			Api.eliminarCache(contexto, "CuentasBloqueos", contexto.idCobis()); // emm: como no viene en la consolidada (la realidad es que debería venir no me
																				// queda alternativa que limpiarla acá)
		} catch (Exception e) {
		}
	}

	public static ApiResponse getCampania(ContextoHB contexto) {
		ApiRequest request = Api.request("CampaniasVigentesWOCI", "productos", "GET", "/v1/campania", contexto);
		request.query("idtributaria", contexto.persona().cuit());
		request.permitirSinLogin = false;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse getAcreditaciones(ContextoHB contexto) {
		ApiRequest request = Api.request("Acreditaciones", "productos", "GET", "/v1/clientes/{id}/acreditaciones", contexto);
		request.path("id", contexto.idCobis());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse relacionClienteProducto(ContextoHB contexto, Objeto relacion) {
		ApiRequest request = Api.request("relaciones", "productos", "POST", "/v1/relaciones", contexto);
		request.body(relacion);
		return Api.response(request, contexto.idCobis());
	}
}
