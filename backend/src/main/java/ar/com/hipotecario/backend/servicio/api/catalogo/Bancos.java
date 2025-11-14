package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos.Banco;

public class Bancos extends ApiObjetos<Banco> {

	/* ========== ATRIBUTOS ========== */
	public static class Banco extends ApiObjeto {
		public String codigo;
		public String Descripcion;
	}

	// API-Catalogo_ConsultaCodigosBancos
	public static Bancos get(Contexto contexto) {
		ApiRequest request = new ApiRequest("Bancos", "catalogo", "GET", "/v1/codigosBancos", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Bancos.class);
	}

	public static Banco get(Contexto contexto, String codigoBanco) {
		ApiRequest request = new ApiRequest("Bancos", "catalogo", "GET", "/v1/codigosBancos", contexto);
		request.query("codigoBanco", codigoBanco);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Bancos.class).get(0);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Banco datos = get(contexto, "440");
		imprimirResultado(contexto, datos);
	}
}
