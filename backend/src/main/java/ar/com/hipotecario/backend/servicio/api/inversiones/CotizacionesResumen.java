package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CotizacionesResumen.CotizacionResumen;

public class CotizacionesResumen extends ApiObjetos<CotizacionResumen> {

	/* ========== ATRIBUTOS ========== */
	public static class CotizacionResumen extends ApiObjeto {
		public String codError;
		public String descError;
		public String descripcion;
		public String simbolo;
		public String tipo;
		public Fecha fechaAnual;
		public Fecha fechaMensual;
		public BigDecimal valorAnual;
		public BigDecimal valorMensual;
		public Integer id;
		public Integer idVencimiento;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CotizacionesResumen
	public static CotizacionesResumen get(Contexto contexto, String idEspecie, String idIndice, String idPanel, String idVencimiento) {
		ApiRequest request = new ApiRequest("cotizacionesResumen", "inversiones", "GET", "/v1/cotizacionesresumen", contexto);
		request.query("idEspecie", idEspecie);
		request.query("idIndice", idIndice);
		request.query("idPanel", idPanel);
		request.query("idVencimiento", idVencimiento);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CotizacionesResumen.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CotizacionesResumen datos = get(contexto, "", "6513", "7", "1");
		imprimirResultado(contexto, datos);
	}
}
