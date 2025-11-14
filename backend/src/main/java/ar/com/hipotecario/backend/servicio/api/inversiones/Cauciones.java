package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Cauciones extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public static class CaucionesClass {
		public Fecha fecha;
		public String especie;
		public String tipoLiquidacion;
		public Integer plazo;
		public String plazoLiquidacion;
		public Integer montoContado;
		public Integer montoFuturo;
		public BigDecimal tasaPromedio;
	}

	public List<CaucionesClass> cauciones;
	public String mensajeNro;

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CaucionesDelay
	public static Cauciones get(Contexto contexto, Integer nroMsj) {
		ApiRequest request = new ApiRequest("InversionesCaucionesDelay", "inversiones", "GET", "/v1/caucionesdelay", contexto);
		request.query("nroMsg", nroMsj);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cauciones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cauciones datos = get(contexto, 4373);
		imprimirResultado(contexto, datos);
	}

}
