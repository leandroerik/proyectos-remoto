package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.CatalogoActividades.Actividad;;

public class CatalogoActividades extends ApiObjetos<Actividad> {

	/* ========== ATRIBUTOS ========== */
	public static class Actividad extends ApiObjeto {
		public String codigoActividad;
		public String descripcionActividad;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaCodigosActividades
	static CatalogoActividades get(Contexto contexto) {
		ApiRequest request = new ApiRequest("Actividades", "catalogo", "GET", "/v1/codigos/actividades", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CatalogoActividades.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CatalogoActividades datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
