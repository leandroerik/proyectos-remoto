package ar.com.hipotecario.canal.homebanking.api;

import org.owasp.encoder.Encode;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.servicio.SqlConsentimiento;

public class HBConsentimiento {
	
	public static Respuesta obtenerToken(ContextoHB contexto) {
		
		String bcraId = contexto.parametros.string("bcraId");
		String state = contexto.parametros.string("state");
		String code = contexto.parametros.string("code");
		String errorDescripcion = contexto.parametros.string("error_description");
		String error = contexto.parametros.string("error");
		// Verificar peligros potenciales del state 
		String encodedState = Encode.forJava(state);
		
		Respuesta respuesta = new Respuesta();
		
		SqlConsentimiento sqlConsentimiento = new SqlConsentimiento();
		SqlResponse response = sqlConsentimiento.consultarPorState(contexto, encodedState);
		if (response.hayError || response.registros.isEmpty()) {
			return Respuesta.estado("STATE_INEXISTENTE");
		}
		
		String cuil = (String) response.registros.get(0).get("user_identifier");
		
		ApiRequest request = Api.request("Canales","canales", "POST", "/obtener-token", contexto);

		request.body("state", encodedState);
		request.body("user_identifier", cuil);
		request.body("code", code);
		request.body("bcraId", bcraId);
		request.body("error_description", errorDescripcion);
		request.body("error", error);
		request.permitirSinLogin = true;

		ApiResponse apiResponse = Api.response(request);
		if (apiResponse.hayError() || apiResponse.get("estado").equals("ERROR")) {
			return respuesta.setEstado("ERROR");
		}else {
			return respuesta;
		}
	}

}