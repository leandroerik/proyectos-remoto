package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class CuentaLiquidaciones extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public CuentaLiquidacionOrdenadas cuentasLiquidacionOrdenadas;
	public String codigoError;
	public Paginacion paginacion;
	public String descripcionError;

	public static class CuentaLiquidacionOrdenada {
		public String Numero;
		public String TipoCuenta;
		public String OrdenSecuencial;
		public String Entidad;
	}

	public static class CuentaLiquidacionOrdenadas {
		public List<CuentaLiquidacionOrdenada> CuentaLiquidacionOrdenada;
	}

	public static class Paginacion {
		public String TotalRegistrosPaginacion;
		public String UltimoSecuencialPaginacion;
		public String ExistenMasRegistrosPaginacion;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CuentasLiquidacion
	public static CuentaLiquidaciones get(Contexto contexto, String idcobis, String cuentacomitente, String cantidadregistrospaginacion, Integer desdesecuencialpaginacion) {
		return get(contexto, idcobis, cuentacomitente, cantidadregistrospaginacion, desdesecuencialpaginacion, null, null);
	}

	public static CuentaLiquidaciones get(Contexto contexto, String idcobis, String cuentacomitente, String cantidadregistrospaginacion, Integer desdesecuencialpaginacion, String moneda) {
		return get(contexto, idcobis, cuentacomitente, cantidadregistrospaginacion, desdesecuencialpaginacion, moneda, null);
	}

	public static CuentaLiquidaciones get(Contexto contexto, String idcobis, String cuentacomitente, String cantidadregistrospaginacion, Integer desdesecuencialpaginacion, String moneda, String tipocuenta) {
		ApiRequest request = new ApiRequest("CuentaLiquidacion", "inversiones", "GET", "/v1/cuentaLiquidacion", contexto);
		request.query("idcobis", idcobis);
		request.query("cuentacomitente", cuentacomitente);
		request.query("cantidadregistrospaginacion", cantidadregistrospaginacion);
		request.query("desdesecuencialpaginacion", desdesecuencialpaginacion.intValue() > 0 ? desdesecuencialpaginacion : 1);
		request.query("moneda", moneda != null ? moneda : "");
		request.query("tipocuenta", tipocuenta != null ? tipocuenta : "");
//		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaLiquidaciones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaLiquidaciones datos = get(contexto, "4373070", "000108703", "100", 0);
		imprimirResultado(contexto, datos);
	}
}
