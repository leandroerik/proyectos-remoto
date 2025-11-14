package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestDelivery {

	public static ApiResponseMB deliveryPiezas(ContextoMB contexto, String idTarjeta) {
		ApiRequestMB request = ApiMB.request("TarjetasDistribucionGet", "delivery", "GET", "/DeliveryAPI/api/v1/delivery/piezas/{nroTarjeta}", contexto);
		request.path("nroTarjeta", idTarjeta);
		request.header("user", ConfigMB.string("delivery_user"));
		request.header("password", ConfigMB.string("delivery_pwd"));
		request.header("handle", " ");

		request.cacheSesion = false;
		return ApiMB.response(request);
	}

	public static ApiResponseMB deliveryClientes(ContextoMB contexto, String dni) {
		ApiRequestMB request = ApiMB.request("TarjetasDistribucionGet", "delivery", "GET", "/DeliveryAPI/api/V1/delivery/clientes/filtro/filtro", contexto);
		request.query("filtro", "dni=" + dni + ",IncluirAdicionales=SI,Entidad=100,CanalInvocador=MOBILBANKING");
		request.header("user", ConfigMB.string("delivery_user"));
		request.header("password", ConfigMB.string("delivery_pwd"));
		request.header("handle", " ");

		request.cacheSesion = false;
		return ApiMB.response(request);
	}

}
