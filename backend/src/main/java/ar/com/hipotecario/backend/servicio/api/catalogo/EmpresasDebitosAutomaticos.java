package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class EmpresasDebitosAutomaticos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer cantidadPaginas;
	public Integer numeroPagina;
	public Integer numeroRegistros;
	public List<Result> result;

	public static class Result {
		public String codigoRubro;
		public String descripcionRubro;
		public String cuit;
		public String nombreEmpresa;
		public String nombreFantasia;
		public String estado;
	}

	/* ============== SERVICIOS ============ */
	// API-Catalogo_ConsultaDebitoAutomatico
	static EmpresasDebitosAutomaticos get(Contexto contexto, String codigoRubro, Integer numeroPagina, Integer cantidadRegistros) {
		ApiRequest request = new ApiRequest("EmpresasDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/{typeQuery}", contexto);
		request.path("typeQuery", "EMPRESAS");
		request.query("cantidadRegistros", cantidadRegistros);
		request.query("numeroPagina", numeroPagina);
		request.query("codigoRubro", codigoRubro);
		request.query("cuit", null);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.equals("codigo", "40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EmpresasDebitosAutomaticos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		EmpresasDebitosAutomaticos datos = get(contexto, "30", 1, 10);
		imprimirResultado(contexto, datos);
	}
}
