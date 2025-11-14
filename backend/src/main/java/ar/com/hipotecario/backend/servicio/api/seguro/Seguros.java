package ar.com.hipotecario.backend.servicio.api.seguro;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.SegurosV4;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


public class Seguros extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String sessionId;

	public static class Seguro extends ApiObjeto {
		public String producto;
		public String ramo;
		public String numeroPoliza;
		public Fecha fechaDesde;
		public Fecha fechaHasta;
		public String montoPrima;
		public String medioPago;
		public String origen;
		public String numeroCuenta;
		public Fecha fechaVenta;
	}

	/* ========== SERVICIOS ========== */
	public static String get(Contexto contexto) {
		ApiRequest request = new ApiRequest("SalesForce", ApiSeguro.API, "GET", "/v1/token-salesforce", contexto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		Objeto json = response.objeto("result");
		String sessionId = json.string("sessionId");
		return sessionId;
	}

	public static Seguro getOferta(Contexto contexto, String sessionId) {
		ApiRequest request = new ApiRequest("ApiSeguro", ApiSeguro.API, "GET", "/v1/ofertas/{sessionId}/", contexto);
		request.path("sessionId", sessionId);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		response.objeto("productos");
		return null;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "getOferta";

		if (test.equals("getOferta")) {
			Seguro datos = getOferta(contexto, "algo");
			System.out.println(datos);
		} else {
			String datos = get(contexto);
			System.out.println(datos);
		}

	}

}
