package ar.com.hipotecario.backend.servicio.api.mobile;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SoftToken extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String estado;
	public String algoritmo;
	public String digitos;
	public String periodo;
	public String claveSecreta;
	public String urlClaveSecreta;
	public String username;
	public String cuil;
	public String id_dispositivo;

	/* ========== SERVICIOS ========== */
	// POST /api/soft-token-alta

	public static SoftToken post(Contexto contexto, Boolean onboarding, String idCobis, String idDispositivo) {
		ApiRequest request = new ApiRequest("AltaSoftToken", "mobile", "POST", "/api/soft-token-alta", contexto);
		request.header("User-Agent", "Android");
		request.body("onboarding", onboarding);
		request.body("idCobisOnboarding", idCobis);
		request.body("idDispositivoOnboarding", idDispositivo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SoftToken.class, response);
	}

	// GET /api/soft-token-generar-idcliente
	public static SoftToken get(Contexto contexto) {
		ApiRequest request = new ApiRequest("GenerarId", "mobile", "GET", "/api/soft-token-generar-idcliente", contexto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SoftToken.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		String test = "post";
		if (test.equals("post")) {
			Contexto contexto = contexto("BB", "homologacion");
			SoftToken datos = post(contexto, true, "121212", "6318754");
			imprimirResultado(contexto, datos);
		}
	}

}
