package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Persona;

public class RestArchivo {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB digitalizacionGetDocumentos(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("ListaDocumentosDigitalizados", "digitalizacion", "GET", "/v1/documentos", contexto);
		request.query("cuil", contexto.persona().cuit());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB digitalizacionGetArchivo(ContextoMB contexto, String idDocumento) {
		ApiRequestMB request = ApiMB.request("DocumentoDigitalizado", "digitalizacion", "GET", "/v1/documentos/{idDocumento}", contexto);
		request.path("idDocumento", idDocumento);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB subirDni(ContextoMB contexto, String nombreArchivo, byte[] bytes) {
		Persona persona = contexto.persona();

		Objeto propiedades = new Objeto();
		propiedades.set("DocumentTitle", "DNI " + persona.cuit() + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
		propiedades.set("CUIL", persona.cuit());
		propiedades.set("ApellidoyNombre", persona.nombreCompleto());
		propiedades.set("NroTramiteWKF", "");
		propiedades.set("DNI", persona.numeroDocumento());
		propiedades.set("FechaAltaOrig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("FechaDig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("TipoPersona", "F");
		propiedades.set("mimetype", Texto.mimeType(nombreArchivo));
		propiedades.set("ExtArchivo", Texto.extension(nombreArchivo));
		propiedades.set("OrigenDelAlta", "HB");
		propiedades.set("IdArchivo", UUID.randomUUID().toString());

		ApiRequestMB request = ApiMB.request("SubirArchivo", "digitalizacion", "POST", "/v1/documentos/{idTributario}", contexto);
		request.path("idTributario", persona.cuit());
		request.body("bytesDocumento", Texto.toBase64(bytes));
		request.body("claseDocumental", "IdentidadH");
		request.body("propiedades", propiedades);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB subirPresupuesto(ContextoMB contexto, String nombreArchivo, byte[] bytes) {
		Persona persona = contexto.persona();

		Objeto propiedades = new Objeto();
		propiedades.set("DocumentTitle", "PRESUPUESTO " + persona.cuit() + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
		propiedades.set("CUIL", persona.cuit());
		propiedades.set("ApellidoyNombre", persona.nombreCompleto());
		propiedades.set("NroTramiteWKF", "");
		propiedades.set("DNI", persona.numeroDocumento());
		propiedades.set("FechaAltaOrig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("FechaDig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("TipoPersona", "F");
		propiedades.set("mimetype", Texto.mimeType(nombreArchivo));
		propiedades.set("ExtArchivo", Texto.extension(nombreArchivo));
		propiedades.set("OrigenDelAlta", "HB");
		propiedades.set("IdArchivo", UUID.randomUUID().toString());

		ApiRequestMB request = ApiMB.request("SubirArchivo", "digitalizacion", "POST", "/v1/documentos/{idTributario}", contexto);
		request.path("idTributario", persona.cuit());
		request.body("bytesDocumento", Texto.toBase64(bytes));
		request.body("claseDocumental", "PresupuestoObra");
		request.body("propiedades", propiedades);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB subirDocumentacion(ContextoMB contexto, String nombreDocumento, String claseDocumental, String documento) throws Exception {
		Persona persona = contexto.persona();
		String nroSolicitud = contexto.parametros.string("solicitud", "");
		Objeto propiedades = propiedades(contexto, claseDocumental, nombreDocumento);

		if (!nroSolicitud.isEmpty()) {
			propiedades.set("NroSolicitud", nroSolicitud);
		}

		ApiRequestMB request = ApiMB.request("SubirArchivo", "digitalizacion", "POST", "/v1/documentos/{idTributario}", contexto);
		request.path("idTributario", persona.cuit());
		request.body("bytesDocumento", documento);
		request.body("claseDocumental", claseDocumental);
		request.body("propiedades", propiedades);
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		return response;
	}

	private static Objeto propiedades(ContextoMB contexto, String claseDocumental, String nombreDocumento) throws Exception {
		Objeto propiedades = new Objeto();

		switch (claseDocumental) {
		case "DNI":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("DNI", contexto.persona().numeroDocumento());
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("OrigenDelAlta", "MB");
			propiedades.set("IdArchivo", UUID.randomUUID().toString());
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("NumeroCliente", contexto.idCobis());
			propiedades.set("IdElemento", Util.secureRandom.nextInt());
			propiedades.set("TipoPersona", "F");
			break;

		/*
		 * "Pero C06470," te escucho decir,
		 * "estas clases comparten algunas propiedades!" Claro que si. Hasta que haya
		 * que agregar una clase que no comparta propiedad X con el resto, y entonces
		 * hay que meterla de nuevo en cada caso.
		 */

		case "PrevencionLavadoH":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("FechaAltaElemento", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
			propiedades.set("TipoPersona", "F");
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("DNI", contexto.persona().numeroDocumento());
			propiedades.set("OrigenDelAlta", "MB");
			break;

		case "IngresosH":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("FechaAltaElemento", new SimpleDateFormat("dd/MM/yyyy HH:MM").format(new Date()));
			propiedades.set("IdElemento", Util.secureRandom.nextInt());
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("TipoPersona", "F");
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			propiedades.set("NumeroCliente", contexto.idCobis());
			propiedades.set("OrigenDelAlta", "MB");
			break;

		case "DocumentacionRespaldatoriaPrevencionH":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("FechaAltaElemento", new SimpleDateFormat("dd/MM/yyyy HH:MM").format(new Date()));
			propiedades.set("IdElemento", Util.secureRandom.nextInt());
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("TipoPersona", "F");
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			break;

		// C05302: Al mandar el campo IdArchivo se rompe el servicio, menos mal que
		// estan estos case
		case "ComprobanteDomicilio":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("DNI", contexto.persona().numeroDocumento());
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("OrigenDelAlta", "MB");
//			propiedades.set("IdArchivo", UUID.randomUUID().toString());
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			propiedades.set("NumeroCliente", contexto.idCobis());
			propiedades.set("TipoPersona", "F");
			break;
		default:
			throw new Exception("Clase documental no reconocida en RestArchivo.");
		}

		return propiedades;
	}
}
