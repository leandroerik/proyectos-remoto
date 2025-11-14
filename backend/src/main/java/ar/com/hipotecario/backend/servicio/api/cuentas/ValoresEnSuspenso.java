package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ValoresEnSuspenso extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public static class ValoresSuspenso {
		public String descripcion;
		public String servicio;
		public String secuencial;
		public BigDecimal valor;
		public String fecha;
	}

	/* =============== ATRIBUTOS =============== */
	public BigDecimal totalValoresSuspenso;
	public Integer cantidadvaloresSuspenso;
	public List<ValoresSuspenso> valoresSuspenso;

	/* ========== SERVICIOS ========== */
	static Boolean validOperacion(String operacion) {
		return operacion.equals("C") || operacion.equals("P") ? true : false;
	}

	// API-Cuenta_ConsultaValoresSuspenso
	static ValoresEnSuspenso get(Contexto contexto, String id, String operacion, String secuencial) {
		ApiRequest request = new ApiRequest("CuentasGetValorSuspenso", "cuentas", "GET", "/v1/cuentas/{id}/valoresensuspenso", contexto);

		if (!validOperacion(operacion)) {
			throw new ApiException(request, null);
		}

		if (secuencial != null) {
			request.query("secuencial", secuencial);
		}

		request.path("id", id);
		request.query("operacion", operacion);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ValoresEnSuspenso.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		ValoresEnSuspenso datos = get(contexto, "200000010089451", "P", "0");
		imprimirResultado(contexto, datos);
	}
}
