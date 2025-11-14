package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class OrdenesByIdOrden extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<OrdenesOrdenadas> operacionesOrdenOrdenadas;
	public Paginacion paginacion;

	public static class OrdenesOrdenadas {
		public Integer numero;
		public Integer ordenSecuencial;
		public BigDecimal cantidadNominal;
		public BigDecimal monto;
		public BigDecimal montoNeto;
		public BigDecimal comisiones;
		public BigDecimal precio;
		public Fecha fecha;
		public Fecha fechaLiquidacion;
		public String tipo;
		public String moneda;
		public String estado;
		public String usoDeCuenta;
		public String tipoEspecie;
		public String descEspecie;
		public String numeroDeCuenta;
		public String ccDescripcion;
		public String ccTipoRelacion;
		public String ccSituacion;
	}

	public static class Paginacion {
		public String totalRegistrosPaginacion;
		public String ultimoSecuencialPaginacion;
		public Boolean existenMasRegistrosPaginacion;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaComposicionOrden
	public static OrdenesByIdOrden get(Contexto contexto, String idOrden, String desdePagina, String idCobis, String registrosxPagina) {
		ApiRequest request = new ApiRequest("OrdenesByIdorden", "inversiones", "GET", "/v1/ordenes/{idorden}", contexto);
		request.path("idorden", idOrden);
		request.query("desdepagina", desdePagina);
		request.query("idcobis", idCobis);
		request.query("registrosxpagina", registrosxPagina);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(OrdenesByIdOrden.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		OrdenesByIdOrden datos = get(contexto, "OTV4312296700257A2KSF", "1", "4373070", "100");
		imprimirResultado(contexto, datos);
	}
}
