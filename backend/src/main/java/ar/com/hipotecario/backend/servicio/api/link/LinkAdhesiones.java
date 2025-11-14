package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.link.LinkAdhesiones.ServicioAdhesion;

public class LinkAdhesiones extends ApiObjetos<ServicioAdhesion> {

	/* ========== ATRIBUTOS ========== */
	public static class Rubro {
		public String codigo;
		public String descripcion;
		public String descripcionAbreviada;
	}

	public static class Ente {
		public String codigo;
		public String descripcion;
		public Boolean isBaseDeuda;
		public Boolean isMultipleConcepto;
		public Boolean isIngresoReferencia;
		public Boolean isIngresoImporte;
		public Boolean isHabilitado;
		public Rubro rubro;
	}

	public static class ServicioAdhesion extends ApiObjeto {
		public String codigoPagoElectronico;
		public String codigoAdhesion;
		public Ente ente;
	}

	public static class RespuestaOk extends ApiObjeto {
		public Boolean ok;
	}

	/* =============== SERVICIOS ================ */
	// API-Link_ConsultaAdhesiones-PagosServicios
	public static LinkAdhesiones get(Contexto contexto, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("LinkGetAdhesiones", "link", "GET", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
		request.path("numeroTarjeta", numeroTarjeta);

		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(LinkAdhesiones.class);
	}

	// API-Link_EliminarAdhesion-PagosServicios
	public static RespuestaOk delete(Contexto contexto, String numeroTarjeta, String codigoAdhesion) {
		ApiRequest request = new ApiRequest("LinkDeleteAdhesiones", "link", "DELETE", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.query("codigoAdhesion", codigoAdhesion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(RespuestaOk.class);
	}

	// API-Link_CrearAdhesion-PagosServicios
	public static RespuestaOk post(Contexto contexto, String numeroTarjeta, String codigoEnte, Boolean esBaseDeuda, String usuarioLP) {
		ApiRequest request = new ApiRequest("LinkPostAdhesiones", "link", "POST", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.body("codigoEnte", codigoEnte);
		request.body("isBase", esBaseDeuda);
		request.body("usuarioLP", usuarioLP);
		request.body("producto", "30");
		request.body("idEntidadEmisora", "0044");
		request.body("tipoTerminal", "D5");
		request.body("cpe", usuarioLP);
		request.query("validarcpe", esBaseDeuda);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(RespuestaOk.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String method = "delete";

		if (method.equals("get")) {
			LinkAdhesiones datos = get(contexto, "4998590239039700");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("delete")) {
			RespuestaOk datos = delete(contexto, "4998590239039700", "02");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("post")) {
			RespuestaOk datos = post(contexto, "4998590238774703", "918", false, "918");
			imprimirResultado(contexto, datos);
		}
	}

}
