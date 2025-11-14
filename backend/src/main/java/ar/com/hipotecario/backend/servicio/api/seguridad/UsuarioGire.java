package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class UsuarioGire extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String mensaje;
	/* ========== SERVICIOS ========== */	
	public static UsuarioGire post(Contexto contexto, Objeto body) {
		ApiRequest request = new ApiRequest("SeguridadGireUsuarioPost", "seguridad", "POST", "/v1/usuariogire", contexto);
		request.body(body);
		ApiResponse response = request.ejecutar();
		return response.crear(UsuarioGire.class, response);
	}
}