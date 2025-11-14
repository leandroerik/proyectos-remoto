package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TitulosProducto extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<ProductosOperablesOrdenados> productosOperablesOrdenados;
	public Paginacion paginacion;

	public static class ProductosOperablesOrdenados {
		public Integer ordenSecuencial;
		public String codigo;
		public String descripcion;
		public String clasificacion;
		public String descMoneda;
		public Integer montoMinimo;
		public Integer montoMaximo;
		public String perfilProducto;
		public String producto;
		public Integer fraccionMinima;
		public String laminaMinima;
		public String fraccionMinimaPrecio;
		public Integer precioPorCada;
	}

	public static class Paginacion {
		public String totalRegistrosPaginacion;
		public String ultimoSecuencialPaginacion;
		public Boolean existenMasRegistrosPaginacion;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaProductoOperable
	public static TitulosProducto get(Contexto contexto, String secuencial, String cantRegistros, Fecha fecha) {
		return get(contexto, secuencial, cantRegistros, fecha, null);
	}

	public static TitulosProducto get(Contexto contexto, String secuencial, String cantRegistros, Fecha fecha, String clasificacion) {
		ApiRequest request = new ApiRequest("TitulosValores", "inversiones", "GET", "/v1/titulos/producto", contexto);
		request.query("fecha", fecha.string("yyyy-MM-dd"));
		if (clasificacion != null)
			request.query("clasificacion", clasificacion);
		request.query("cantregistros", cantRegistros);
		request.query("secuencial", secuencial);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TitulosProducto.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fecha = new Fecha("2021-01-01", "yyyy-MM-dd");
		TitulosProducto datos = get(contexto, "1", "300", fecha);
		imprimirResultado(contexto, datos);
	}
}
