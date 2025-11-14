package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.DeudasUnitrade.DeudaUnitrade;

public class DeudasUnitrade extends ApiObjetos<DeudaUnitrade> {

	/* ========== ATRIBUTOS ========== */
	public static class DeudaUnitrade extends ApiObjeto {
		public Boolean tipoProducto;
		public String numeroMinuta;
		public Boolean idMoneda;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaComposicionDeudaUnitrade
	public static DeudasUnitrade get(Contexto contexto, String cliente, Boolean moneda, Boolean numProducto, Boolean tipoProducto) {
		ApiRequest request = new ApiRequest("DeudasUnitrade", "inversiones", "GET", "/v1/deudas/{cliente}/unitrade", contexto);
		request.path("cliente", cliente);
		request.query("moneda", moneda);
		request.query("numproducto", numProducto);
		request.query("tipoproducto", tipoProducto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(DeudasUnitrade.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DeudasUnitrade datos = get(contexto, "4373070", false, false, false);
		imprimirResultado(contexto, datos);
	}
}
