package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;

public class CrearComercio extends ApiObjeto {
	private String id;
	private String cuit;
	private String segmento;


	/* ========== SERVICIOS ========== */
	// API-Empresas_Crear_Comercio

	public static CrearComercio get(Contexto contexto, CrearComercioRequest requestCrearComercio) {
		ApiRequest request = new ApiRequest("API-Empresas_Crear_Comercio", "empresas", "POST", "/v1/comercios", contexto);
		request.body(requestCrearComercio.objeto());

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CrearComercio.class);
	}
}
