package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class RubrosDebitosAutomaticos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer cantidadPaginas;
	public Integer numeroPagina;
	public Integer numeroRegistros;
	public List<Result> result;

	public static class Result {
		public String codigoRubro;
		public String descripcionRubro;
	}

	/* ============== SERVICIOS ============ */
	// API-Catalogo_ConsultaDebitoAutomatico
	static RubrosDebitosAutomaticos get(Contexto contexto, Integer numeroPagina, Integer cantidadRegistros) {
		ApiRequest request = new ApiRequest("RubrosDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/{typeQuery}", contexto);
		request.path("typeQuery", "RUBROS");
		request.query("cantidadRegistros", cantidadRegistros);
		request.query("numeroPagina", numeroPagina);
		request.query("codigoRubro", null);
		request.query("cuit", null);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.equals("codigo", "40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(RubrosDebitosAutomaticos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		RubrosDebitosAutomaticos datos = get(contexto, 1, 30);
		imprimirResultado(contexto, datos);
	}
}
