package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosOB.Convenio;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class ConveniosOB extends ApiObjetos<Convenio> {

	/* ========== ATRIBUTOS ========== */
	public static class Convenio extends ApiObjeto {

		public String cuit;
		public String prioridad;
		public String marcaPagoParcial;
		public String segmento;
		public Integer codigoConvenio;
		public String cuenta;
		public String descripcion;
		public String fechaUltimaAcreditacion;
		public String tipoDeConvenio;
		public String tipoEmpresa;
		public String clienteVinculado;
		public Integer cantDias;

	}

	/* ========== SERVICIOS ========== */

	public static ConveniosOB get(Contexto contexto, String cuit, String operacion) {
		ApiRequest request = new ApiRequest("LinkGetConvenios", "recaudaciones", "GET", "/v1/convenios", contexto);
		request.query("cuit", cuit);
		request.query("operacion", operacion);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		return response.crear(ConveniosOB.class);
	}

	public static ConveniosOB getConvenio(Contexto contexto, Integer idCobis) {
		ApiRequest request = new ApiRequest("ConsultaConvenioRecaudaciones", "recaudaciones", "GET", "/v1/convenios/{convenio}", contexto);
		request.path("convenio", String.valueOf(idCobis));
		request.query("secuencial", 100);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		return response.crear(ConveniosOB.class);
	}

	public static Object relacionarEcheqConConvenio(ContextoOB contexto, String idCheque, String convenio, String tipo, String cuit, String nombre) {
		ApiRequest request = new ApiRequest("V1ConveniosNominaEcheqsPost", "recaudaciones", "POST", "/v1/convenios/echeqs/depositocustodia", contexto);
		request.body("idEcheq", idCheque);
		request.body("codigoConvenio", convenio);
		request.body("formaPago", tipo);
		request.body("cuitCliente", cuit);
		request.body("nombreCliente", nombre);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ConveniosOB.class);
	}
	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "get";
		if ("get".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			ConveniosOB datos = get(contexto, "30558525025", "S");
			imprimirResultado(contexto, datos);
		}
	}
}
