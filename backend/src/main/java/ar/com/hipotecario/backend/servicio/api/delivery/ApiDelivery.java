package ar.com.hipotecario.backend.servicio.api.delivery;


import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class ApiDelivery {
	
	public static Objeto getPieza(Contexto contexto, String idTarjeta,String canal) {
		ApiRequest request = new ApiRequest("GetPieza", "delivery", "GET", "/api/v1/delivery/piezas/{nroTarjeta}", contexto);
		request.path("nroTarjeta", idTarjeta);
		request.header("user", contexto.config.string(canal+"_api_delivery_user"));
		request.header("password",contexto.config.string(canal+"_api_delivery_pwd"));
		request.header("handle", " ");
		request.path("nroTarjeta", idTarjeta);

		request.cache = false;
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response;
	}

	public static ApiResponse getDireccionReenvio(Contexto contexto, String numPieza, String canal) {
		ApiRequest request = new ApiRequest("GetPieza", "delivery", "GET", "/api/v1/delivery/reenvios/direccionReenvio/{numPieza}", contexto);
		request.path("numPieza", numPieza);
		request.header("user", contexto.config.string(canal+"_api_delivery_user"));
		request.header("password",contexto.config.string(canal+"_api_delivery_pwd"));
		request.header("handle", "clientesCRM");
		request.path("numPieza", numPieza);
		request.cache = true;
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response;
	}

	// Métodos replicados de MB

	public static ApiResponse deliveryPiezas(ContextoOB contexto, String idTarjeta, String canal) {
		ApiRequest request = new ApiRequest("TarjetasDistribucionGet", "delivery", "GET", "/api/v1/delivery/piezas/{nroTarjeta}", contexto);
		request.path("nroTarjeta", idTarjeta);
		request.header("user", contexto.config.string(canal+"_api_delivery_user"));
		request.header("password",contexto.config.string(canal+"_api_delivery_pwd"));
		request.header("handle", " ");

		request.cache = false;
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response;
	}

	public static ApiResponse deliveryClientes(Contexto contexto, String dni, String canal) {
		ApiRequest request = new ApiRequest("TarjetasDistribucionGet", "delivery2", "GET", "/api/V1/delivery/clientes/filtro/filtro", contexto);
		String canalInvocador = "MOBILBANKING";
		if (canal != null && canal.equals("ob")) {
		    canalInvocador = "OFFICEBANKING";
		}
		request.query("filtro", "dni=" + dni + ",IncluirAdicionales=SI,Entidad=100,CanalInvocador="+canalInvocador);
		request.header("user", contexto.config.string(canal+"_api_delivery_user"));
		request.header("password",contexto.config.string(canal+"_api_delivery_pwd"));
		request.header("handle", " ");

		request.cache = false;
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response;
	}

}
