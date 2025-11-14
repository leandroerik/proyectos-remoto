package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TitulosPrecio extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String descMoneda;
	public BigDecimal precioReferencia;
	public String tipoProducto;
	public Fecha fechaPrecio;

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaPrecioReferencia
	public static TitulosPrecio get(Contexto contexto, Fecha fecha, String descmoneda, String tipoProducto) {
		return get(contexto, fecha, descmoneda, tipoProducto, null);
	}

	public static TitulosPrecio get(Contexto contexto, Fecha fecha, String descmoneda, String tipoProducto, String idCobis) {
		ApiRequest request = new ApiRequest("PrecioTituloValor", "inversiones", "GET", "/v1/titulos/precio", contexto);
		request.query("fecha", fecha.string("yyyy-MM-dd"));
		request.query("descmoneda", descmoneda);
		request.query("tipoproducto", tipoProducto);
		if (idCobis != null)
			request.query("idcobis", idCobis);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TitulosPrecio.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fecha = new Fecha("2021-01-01", "yyyy-MM-dd");
		TitulosPrecio datos = get(contexto, fecha, "PESOS", "AY24", "4373070");
		imprimirResultado(contexto, datos);
	}

}
