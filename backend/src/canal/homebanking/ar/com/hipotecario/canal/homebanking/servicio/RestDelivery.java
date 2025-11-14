package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestDelivery {
	public static ApiResponse getPieza(ContextoHB contexto, String idTarjeta) {
		ApiRequest request = Api.request("GetPieza", "delivery", "GET", "/api/v1/delivery/piezas/{nroTarjeta}", contexto);
		request.path("nroTarjeta", idTarjeta);
		request.header("user", ConfigHB.string("api_delivery_user"));
		request.header("password", ConfigHB.string("api_delivery_pwd"));
		request.header("handle", "clientesCRM");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), idTarjeta);
	}

	public static ApiResponse getDireccionReenvio(ContextoHB contexto, String numPieza) {
		ApiRequest request = Api.request("GetPieza", "delivery", "GET", "/api/v1/delivery/reenvios/direccionReenvio/{numPieza}", contexto);
		request.path("numPieza", numPieza);
		request.header("user", ConfigHB.string("api_delivery_user"));
		request.header("password", ConfigHB.string("api_delivery_pwd"));
		request.header("handle", "clientesCRM");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numPieza);
	}

	// Métodos replicados de MB

	public static ApiResponse deliveryPiezas(ContextoHB contexto, String idTarjeta) {
		ApiRequest request = Api.request("TarjetasDistribucionGet", "delivery", "GET", "/api/v1/delivery/piezas/{nroTarjeta}", contexto);
		request.path("nroTarjeta", idTarjeta);
		request.header("user", ConfigHB.string("api_delivery_user"));
		request.header("password", ConfigHB.string("api_delivery_pwd"));
		request.header("handle", " ");

		request.cacheSesion = false;
		return Api.response(request);
	}

	public static ApiResponse deliveryClientes(ContextoHB contexto, String dni) {
		ApiRequest request = Api.request("TarjetasDistribucionGet", "delivery", "GET", "/api/V1/delivery/clientes/filtro/filtro", contexto);
		request.query("filtro", "dni=" + dni + ",IncluirAdicionales=SI,Entidad=100,CanalInvocador=MOBILBANKING");
		request.header("user", ConfigHB.string("api_delivery_user"));
		request.header("password", ConfigHB.string("api_delivery_pwd"));
		request.header("handle", " ");

		request.cacheSesion = false;
		return Api.response(request);
	}

}
