package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.seguridad.TarjetasCoordenadas.TCO;

public class TarjetasCoordenadas extends ApiObjetos<TCO> {

	/* ========== ATRIBUTOS ========== */
	public static class TCO extends ApiObjeto {
		public String numeroDeSerie;
		public String state;
		public String fechaCreacion;
		public String usuario;
		public String grupo;
	}

	public TCO buscar(String estado) {
		TCO dato = null;
		for (TCO tco : this) {
			if (tco.state != null && tco.state.equals(estado)) {
				dato = tco;
			}
		}
		return dato;
	}

	public TCO current() {
		return buscar("CURRENT");
	}

	/* ========== SERVICIOS ========== */
	// API-Seguridad_ConsultaTarjetaCoordenadas
	public static TarjetasCoordenadas get(Contexto contexto, String grupo, String idCliente) {
		ApiRequest request = new ApiRequest("TarjetasCoordenadas", "seguridad", "GET", "/v1/tarjetascoordenadas", contexto);
		request.query("grupo", grupo);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		return response.crear(TarjetasCoordenadas.class, response);
	}

	public static ApiObjeto post(Contexto contexto, String idCliente, String grupo, String... respuestas) {
		Objeto datos = new Objeto();
		for (Integer i = 0; i < respuestas.length; ++i) {
			datos.add().set("orden", i + 1).set("respuesta", respuestas[i]);
		}

		ApiRequest request = new ApiRequest("ValidacionTCO", "seguridad", "POST", "/v1/tarjetascoordenadas/respuesta", contexto);
		request.body("cantidad", respuestas.length);
		request.body("grupo", grupo);
		request.body("respuestas", datos);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("DESAFIO_NO_ENCONTRADO", response.contains("USER_NO_CHALLENGE"), request, response);
		ApiException.throwIf("COORDENADAS_INVALIDAS", response.contains("INVALID_RESPONSE"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("USER_LOCKED"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ApiObjeto.class, response);
	}

	public static ApiObjeto patch(Contexto contexto, String grupo, String idCliente, String texto, String numerodeserie) {
		ApiRequest request = new ApiRequest("BajaTCO", "seguridad", "PATCH", "/v1/tarjetascoordenadas/{numerodeserie}/usuario", contexto);
		request.query("grupo", grupo);
		request.query("idcliente", idCliente);
		request.body("texto", texto);
		request.path("numerodeserie", numerodeserie);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("TCO_INEXISTENTE", response.contains("CARD_NOT_EXIST"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(ApiObjeto.class, response);
	}

	public static ApiObjeto path(Contexto contexto, String grupo, String idCliente) {
		ApiRequest request = new ApiRequest("DesbloqueoTCO", "seguridad", "PATCH", "/v1/tarjetascoordenadas", contexto);
		request.query("grupo", grupo);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_SIN_TCO", response.contains("USER_NOT_EXISTT"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(ApiObjeto.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		String test = "get";
		if (test.equals("get")) {
			Contexto contexto = contexto("OB", "desarrollo");
			TarjetasCoordenadas datos = get(contexto, "ClientesBH", "5161669");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("post")) {
			Contexto contexto = contexto("OB", "desarrollo");
			ApiObjeto datos = post(contexto, "4594725", "ClientesBH", "90", "10");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("patch")) {
			Contexto contexto = contexto("OB", "desarrollo");
			ApiObjeto datos = patch(contexto, "ClientesBH", "133366", "Baja de TCO", "741");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("path")) {
			Contexto contexto = contexto("OB", "desarrollo");
			ApiObjeto datos = path(contexto, "ClientesBH", "4594725");
			imprimirResultado(contexto, datos);
		}
	}
}
