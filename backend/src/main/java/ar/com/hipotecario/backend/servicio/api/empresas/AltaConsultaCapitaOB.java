package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class AltaConsultaCapitaOB extends ApiObjeto {
	public String activiConstruc;
	public String cbuCajaAho;
	public String cbuFcl;
	public String cta;
	public String ctaFcl;
	public String descripcion;
	public Integer ente;
	public String error;
	public String estado;
	public Integer iericCliente;
	public Integer lote;
	public String nombreCliente;
	public String tipoError;
	public String codigo;
	public String mensajeAlUsuario;

	/* ========== SERVICIOS ========== */
	// API-Empresas_AltaDeCapita

	public static AltaConsultaCapitaOB get(Contexto contexto, AltaConsultaCapitaRequest requestAlta) {
		ApiRequest request = new ApiRequest("API-Empresas_AltaDeCapita", "empresas", "POST", "/v1/empresas/capita", contexto);
		request.body(requestAlta.objeto());

		ApiResponse response = request.ejecutar();
		return response.crear(AltaConsultaCapitaOB.class);
	}
}
