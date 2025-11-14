package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Censo extends ApiObjeto {

	public static String GET_CENSO = "Censo";

	/* ========== ATRIBUTOS ========== */
	public Boolean censado;
	public String wociStatus;

	/* ========== SERVICIOS ========== */
	// API-Personas_ConsultaCensoNacionalEconomico
	public static Censo get(Contexto contexto, String cuil, String idcobis, String sexo, String tipoDocumento) {
		ApiRequest request = new ApiRequest(GET_CENSO, ApiPersonas.API, "GET", "/censo", contexto);
		request.query("cuil", cuil);
		request.query("idcobis", idcobis);
		request.query("sexo", sexo);
		request.query("tipoDocumento", tipoDocumento);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Censo.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Censo datos = get(contexto, "23551000095", "133366", "M", "01");
		imprimirResultado(contexto, datos);
	}
}
