package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosConsulta.PrestamoConsulta;

public class PrestamosConsulta extends ApiObjetos<PrestamoConsulta> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoConsulta extends ApiObjeto {

	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_Consulta
	public static Boolean validTipoCancelacion(String tipoCancelacion) {
		return tipoCancelacion.equals("ANTCAPITAL") || tipoCancelacion.equals("CANCTOTAL") ? true : false;
	}

	public static PrestamosConsulta get(Contexto contexto, String numOperacion, Fecha fecha, String tipoCancelacion) {
		ApiRequest request = new ApiRequest("PrestamosConsulta", "prestamos", "GET", "/v1/prestamos/consulta", contexto);
		request.query("fecha", fecha.string("MM/dd/yyyy"));
		request.query("num_operacion", numOperacion);

		if (validTipoCancelacion(tipoCancelacion))
			request.query("tipo_cancelacion", tipoCancelacion);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosConsulta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		// SIN DATOS PARA PROBAR
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fecha = new Fecha("01/21/2021", "MM/dd/yyyy");
		PrestamosConsulta datos = get(contexto, "0370081148", fecha, "ANTCAPITAL");
		imprimirResultado(contexto, datos);
	}
}
