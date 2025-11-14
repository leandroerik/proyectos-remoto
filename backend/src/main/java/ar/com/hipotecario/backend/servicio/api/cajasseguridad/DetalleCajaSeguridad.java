package ar.com.hipotecario.backend.servicio.api.cajasseguridad;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class DetalleCajaSeguridad extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String estado;
	public String descripcionProducto;
	public String tipoCuenta;
	public Fecha fechaVencimiento;
	public Boolean renueva;
	public String numeroProducto;
	public String descEstado;
	public BigDecimal valorContrato;
	public Integer periodicidadCobro;

	/* ========== SERVICIOS ========== */
	// API-CajasSeguridad_ConsultaCajaSeguridad
	static DetalleCajaSeguridad get(Contexto contexto, String idCajasSeguridad, Boolean... cache) {
		ApiRequest request = new ApiRequest("DetalleCajaSeguridad", "cajasseguridad", "GET", "/v2/cajasseguridad/{idcajasseguridad}", contexto);
		request.path("idcajasseguridad", idCajasSeguridad);
		request.cache = Util.lastNonNull(false, cache);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DetalleCajaSeguridad.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DetalleCajaSeguridad detalle = get(contexto, "2810");
		imprimirResultado(contexto, detalle);
	}
}
