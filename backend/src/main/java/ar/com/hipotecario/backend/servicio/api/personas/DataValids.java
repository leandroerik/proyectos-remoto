package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.DataValids.DataValid;

public class DataValids extends ApiObjetos<DataValid> {

	/* ========== ATRIBUTOS ========== */
	public static class DataValid extends ApiObjeto {
		public Integer idCliente;
		public String entidad;
		public String usrValid;
		public Boolean validado;
		public Fecha fhVto;

		public Integer deIdCore;
		public String deTipoMail;
		public String deDireccion;

		public Integer diIdCore;
		public String diIdTipoDomicilio;
		public String diIdCodigoPostal;
		public Integer diProvincia;
		public Integer diNumero;
		public String diCalle;
		public String diPiso;
		public String diDepartamento;

		public Integer teIdCore;
		public String teIdTipoTelefono;
		public Integer teDireccionIdCore;
		public String teCodigoPais;
		public String teCodigoArea;
		public String teValor;
		public String optMail;
		public String otpTelefono;
	}

	/* ========== CLASES ========== */

	/* ========== SERVICIOS ========== */
	public static DataValids get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest("datavalid", ApiPersonas.API, "GET", "/personas/{id}/datavalid", contexto);
		request.path("id", cuit);
		request.query("opcion", "todo");
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(DataValids.class);
	}

	public static DataValids post(Contexto contexto, String cuit, String secDir, String secTel, String secMail, String tipoMail) {
		ApiRequest request = new ApiRequest("datavalid", ApiPersonas.API, "POST", "/personas/{id}/datavalid", contexto);
		request.path("id", cuit);
		request.body("secDir", secDir != null ? Integer.parseInt(secDir) : null);
		request.body("secTel", secTel != null ? Integer.parseInt(secTel) : null);
		request.body("secMail", secMail != null ? Integer.parseInt(secMail) : null);
		request.body("tipoMail", tipoMail);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(DataValids.class);
	}

	public static Boolean postOtpTel(Contexto contexto, String xusuario, String cuit, int secTel, int secDir) {
		ApiRequest request = new ApiRequest("datavalid", ApiPersonas.API, "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", xusuario);
		request.path("id", cuit);
		request.body("secTel", secTel);
		request.body("secDir", secDir);
		request.body("otpTelefono", "S");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.http(200);
	}

	public static Boolean postOtpEmail(Contexto contexto, String xusuario, String cuit, int secMail, String tipoMail) {
		ApiRequest request = new ApiRequest("datavalid", ApiPersonas.API, "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", xusuario);
		request.path("id", cuit);
		request.body("secMail", secMail);
		request.body("tipoMail", tipoMail);
		request.body("otpMail", "S");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.http(200);
	}

	/* ========== TEST ========== */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Contexto contexto = new Contexto("HB", "homologacion", "133366");

	}
}
