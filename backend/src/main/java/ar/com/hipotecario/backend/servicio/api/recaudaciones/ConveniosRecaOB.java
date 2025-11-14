package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosRecaOB.*;

public class ConveniosRecaOB extends ApiObjetos<ConvenioReca> {
	public List<Encabezado> lista1;    
	public List<ConvenioReca> lista2;

	/* ========== ATRIBUTOS ========== */
	
	public static class Encabezado extends ApiObjeto {
		public String totalRegistrosGcr;
	}
	public static class ConvenioReca extends ApiObjeto {
		public String grupoRecaudacion;
		public String aperturaConvenio;
		public String descGrupoRecaudacion;
		public String servRecaudacion;
		public String descServRecaudacion;
		public Integer convenio;
		public String descConvenio;
        public Integer moneda;
	    public String tipoConvenio;
	    public Integer codEsquemaComis;
	    public String descEsquemaComis;
	    public String cobroComis;
	    public String cuit;
	    public String numeroCuenta;
	    public String grupoConvenio;
	}

	/* ========== SERVICIOS ========== */

	public static ConveniosRecaOB get(Contexto contexto, Integer idCobis) {
		ApiRequest request = new ApiRequest("LinkGetConvenios", "recaudaciones", "GET", "/v1/convenioRecaudaciones", contexto);
		request.header("Content-Type","application/json");
		request.query("idEnteCobis", idCobis);
		request.query("secuencial", "100");
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		return response.crear(ConveniosRecaOB.class);
	}


	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "get";
		if ("get".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			ConveniosRecaOB datos = get(contexto, 4832646);
			imprimirResultado(contexto, datos);
		}
	}
}
