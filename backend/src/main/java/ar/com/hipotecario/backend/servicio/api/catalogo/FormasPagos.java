package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.FormasPagos.FormaPago;

public class FormasPagos extends ApiObjetos<FormaPago> {

	/* =========== ATRIBUTOS =========== */
	public static class FormaPago extends ApiObjeto {
		public String marcaCodi;
		public String pagoFormaCodi;
		public String pagoFormaDescrip;
		public String cuentaTipo;
		public String cuentaTipoDescrip;
		public String cuentaMoneda;
	}

	/* ============== SERVICIOS ============ */
	static FormasPagos get(Contexto contexto) {
		return get(contexto, null);
	}

	// API-Catalogo_ConsultaFormaPagoTarjeta
	static FormasPagos get(Contexto contexto, String marca) {
		ApiRequest request = new ApiRequest("FormaPago", "catalogo", "GET", "/v1/formapago", contexto);
		request.query("marca", marca);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(FormasPagos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		FormasPagos datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
