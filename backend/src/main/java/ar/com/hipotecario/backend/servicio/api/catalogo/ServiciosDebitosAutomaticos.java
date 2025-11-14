package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ServiciosDebitosAutomaticos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer cantidadPaginas;
	public Integer numeroPagina;
	public Integer numeroRegistros;
	public List<Result> result;

	public static class Result {
		public String codigoServicio;
		public String descripcionServicio;
		public String leyenda;
		public Integer longitudClave;
	}

	/* ============== SERVICIOS ============ */
	// API-Catalogo_ConsultaDebitoAutomatico
	static ServiciosDebitosAutomaticos get(Contexto contexto, String codigoRubro, String cuit, Integer numeroPagina, Integer cantidadRegistros) {
		ApiRequest request = new ApiRequest("ServiciosDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/{typeQuery}", contexto);
		request.path("typeQuery", "SERVICIOS");
		request.query("cantidadRegistros", cantidadRegistros);
		request.query("numeroPagina", numeroPagina);
		request.query("codigoRubro", codigoRubro);
		request.query("cuit", cuit);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.equals("codigo", "40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ServiciosDebitosAutomaticos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		ServiciosDebitosAutomaticos datos = get(contexto, "30", "30707606610", 1, 30);
		imprimirResultado(contexto, datos);
	}
}
