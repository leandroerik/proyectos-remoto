package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Ordenes extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<OrdenesOrdenadas> ordenesOrdenadas;
	public Paginacion paginacion;

	public static class OrdenesOrdenadas {
		public Integer numero;
		public Integer ordenSecuencial;
		public BigDecimal precioLimite;
		public BigDecimal cantidadNominal;
		public BigDecimal monto;
		public Fecha fecha;
		public String tipo;
		public String moneda;
		public String estado;
		public String cuentaLiquidacionMonetaria;
		public String cuentaLiquidacionTitulos;
		public String orderId;
		public String vigencia;
		public String plazo;
		public String usoDeCuenta;
		public String tipoEspecie;
		public String descEspecie;
		public String numeroDeCuenta;
		public String ccDescripcion;
		public String ccTipoRelacion;
		public String ccSituacion;
		public String ccCodigoTipoRelacion;
	}

	public static class Paginacion {
		public String totalRegistrosPaginacion;
		public String ultimoSecuencialPaginacion;
		public Boolean existenMasRegistrosPaginacion;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaOrden
	public static Ordenes get(Contexto contexto, String cuentaComitente, String desdePagina, String idCobis, String registrosxPagina, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("Ordenes", "inversiones", "GET", "/v1/ordenes", contexto);
		request.query("cuentacomitente", cuentaComitente);
		request.query("desdepagina", desdePagina);
		request.query("idcobis", idCobis);
		request.query("registrosxpagina", registrosxPagina);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Ordenes.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		Fecha fechaDesde = new Fecha("2019-01-16", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2019-06-16", "yyyy-MM-dd");
		Ordenes datos = get(contexto, "2-000108703", "1", "4373070", "100", fechaDesde, fechaHasta);
		imprimirResultado(contexto, datos);
	}
}
