package ar.com.hipotecario.canal.homebanking.api;

import java.net.URLEncoder;
import java.util.Base64;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;

public class HBAcumar {

	public static byte[] terminosCondiciones(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String nemonico = contexto.parametros.string("nemonico");

		if (idSolicitud.isEmpty() || nemonico.isEmpty()) {
			throw new RuntimeException();
		}

		ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupocodigo", nemonico);
		request.query("canal", "HB");

		if (nemonico.equalsIgnoreCase("PPADELANTO")) {
			request.header("x-cuil", contexto.persona().cuit());
			try {
				request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
			} catch (Exception e) {
				request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
			}
			request.header("x-dni", contexto.persona().numeroDocumento());
			request.header("x-producto", "AdelantoBH");
		}

		ApiResponse response = Api.response(request, idSolicitud, nemonico, contexto.idCobis());
		if (response.hayError()) {
			throw new RuntimeException();
		}

		String base64 = response.string("Data");
		byte[] archivo = Base64.getDecoder().decode(base64);
		try {
			archivo = Base64.getDecoder().decode(new String(archivo));
		} catch (Exception e) {
		}
		contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");
		return archivo;
	}

	public static Respuesta terminosCondicionesString(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String nemonico = contexto.parametros.string("nemonico");

		if (idSolicitud.isEmpty() || nemonico.isEmpty()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "terminosCondicionesHtml", null, "PARAMETROS_INCORRECTOS");
			return Respuesta.parametrosIncorrectos();
		}

		ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupocodigo", nemonico);
		request.query("canal", "MB");
		ApiResponse response = Api.response(request, idSolicitud, nemonico, contexto.idCobis());
		if (response.hayError()) {
			throw new RuntimeException();
		}

		String base64 = response.string("Data");
		contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");
		Respuesta respuesta = new Respuesta();
		respuesta.set("Data", base64);

		return respuesta;
	}
}
