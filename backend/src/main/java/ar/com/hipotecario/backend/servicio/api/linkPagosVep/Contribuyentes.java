package ar.com.hipotecario.backend.servicio.api.linkPagosVep;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Contribuyentes extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer paginaSiguiente;
	public Integer cantidad;
	public List<Contribuyente> contribuyentes;

	public static class Contribuyente extends ApiObjeto {
		public String referencia;
		public Tributario contribuyente;
	}

	public static class Tributario extends ApiObjeto {
		public String idTributario;
	}

	/* ========== SERVICIOS ========== */
	public static Contribuyentes get(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String maxCantidad, String numeroTarjeta, String pagina) {
		ApiRequest request = new ApiRequest("LinkGetContribuyentes", "veps", "GET", "/v1/contribuyentes/{idTributarioCliente}", contexto);
		request.path("idTributarioCliente", idTributarioCliente);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("idTributarioEmpresa", idTributarioEmpresa);
		request.query("maxCantidad", maxCantidad);
		request.query("pagina", pagina);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		return response.crear(Contribuyentes.class);

	}

	public static Boolean post(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String idTributarioContribuyente, String referencia) {
		ApiRequest request = new ApiRequest("LinkPostContribuyentes", "veps", "POST", "/v1/contribuyentes", contexto);
		request.body("cliente", new Objeto().set("idTributario", idTributarioCliente));
		request.body("empresa", new Objeto().set("idTributario", idTributarioEmpresa));
		request.body("tarjetaDebito", new Objeto().set("numero", numeroTarjeta));
		request.body("contribuyente", new Objeto().set("idTributario", idTributarioContribuyente));
		request.body("referencia", referencia);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("EL_CONTRIBUYENTE_YA_EXISTE", response.contains("El contribuyente ya existe"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return true;
	}

	public static Boolean delete(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String idTributarioContribuyente) {
		ApiRequest request = new ApiRequest("LinkPostContribuyentes", "veps", "DELETE", "/v1/contribuyentes", contexto);
		request.query("idTributarioCliente", idTributarioCliente);
		request.query("idTributarioContribuyente", idTributarioContribuyente);
		request.query("idTributarioEmpresa", idTributarioEmpresa);
		request.query("numeroTarjeta", numeroTarjeta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("CONTRIBUYENTE_NOT_EXISTS", response.contains("El contribuyente no existe"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return true;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "get";
		if ("get".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			Contribuyentes datos = get(contexto, "20000000087", "30000000015", "10", "5046200441112559", "1");
			imprimirResultado(contexto, datos);
		}
		if ("post".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			post(contexto, "20000000087", "30000000015", "5046200441112559", "20000000087", "1");
		}
		if ("delete".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			delete(contexto, "20000000087", "30000000015", "5046200441112559", "20000000087");
		}
	}
}
