package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Resumen extends ApiObjeto{
	
	
	public String file;
	
	public static Resumen getResumen(ContextoOB contexto, String numeroCuenta, String keyvalue) {
		ApiRequest request = new ApiRequest("ResumenTarjeta", "tarjetascredito", "GET", "/v1/tarjetascredito/ultimaliquidacion", contexto);
		request.query("cuenta", numeroCuenta);
		request.query("keyvalue", keyvalue);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		Resumen resumenTarjeta = response.crear(Resumen.class);
		try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        Map<String, String> responseBodyMap = objectMapper.readValue(response.body, Map.class);
	        String base64Pdf = responseBodyMap.get("renglon");

	        if (isBase64(base64Pdf)) {
	            resumenTarjeta.file = base64Pdf;
	        } else {
	            throw new IllegalArgumentException("El cuerpo de la respuesta no contiene una cadena Base64 válida");
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Error al procesar la respuesta", e);
	    }

	    return resumenTarjeta;
		
	}
	
	private static boolean isBase64(String str) {
	    String base64Pattern = "^[A-Za-z0-9+/=]+$";
	    return str != null && Pattern.matches(base64Pattern, str);
	}
	
}
