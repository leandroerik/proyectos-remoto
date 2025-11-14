package ar.com.hipotecario.backend.servicio.api.formulario;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class Formularios extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String data;

	/* ========== SERVICIOS ========== */
	public static String get(Contexto contexto, String idSolicitud, String grupoCodigo) {
		ApiRequest request = new ApiRequest("Formulario", ApiFormulario.API, "GET", "/Api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupoCodigo", grupoCodigo);
		request.query("canal", contexto.canal);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		return response.string("Data");
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "desarrollo");
		String datos = get(contexto, "10198226", "GPEPFATCA");
		System.out.println(datos);
	}

}
