package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.math.BigDecimal;

public class CotizacionesOb extends ApiObjetos<CotizacionesOb.CotizacionOb> {

	/* ========== ATRIBUTOS ========== */
	public static class CotizacionOb extends ApiObjeto {
		public BigDecimal compra;
		public BigDecimal venta;
		public Boolean cont;
		public String desde;
		public String estado;
		public String expresion;
		public Fecha fecha;
		public Integer hasta;
		public String hora;
		public String id;
		public String mercado;
		public String idMoneda;
		public String usuario;
		public String var;

	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Cotizaciones
	public static CotizacionesOb get(Contexto contexto, String id, String mercado) {
		ApiRequest request = new ApiRequest("Cotizaciones", "inversiones", "GET", "/v2/cotizaciones", contexto);
		request.query("idcliente", id);
		request.query("mercado", mercado);
		request.header("x-canal", "HB_BE");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CotizacionesOb.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CotizacionesOb datos = get(contexto, "0", "6");
		imprimirResultado(contexto, datos);
	}

}
