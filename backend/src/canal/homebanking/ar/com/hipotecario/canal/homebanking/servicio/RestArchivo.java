package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;

public class RestArchivo {

	/* ========== SERVICIOS ========== */
	public static ApiResponse digitalizacionGetDocumentos(ContextoHB contexto) {
		ApiRequest request = Api.request("ListaDocumentosDigitalizados", "digitalizacion", "GET", "/v1/documentos", contexto);
		request.query("cuil", contexto.persona().cuit());
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse digitalizacionGetArchivo(ContextoHB contexto, String idDocumento) {
		ApiRequest request = Api.request("DocumentoDigitalizado", "digitalizacion", "GET", "/v1/documentos/{idDocumento}", contexto);
		request.path("idDocumento", idDocumento);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse subirDni(ContextoHB contexto, String nombreArchivo, byte[] bytes, String idSolicitud) {
		Persona persona = contexto.persona();

		Objeto propiedades = new Objeto();
		propiedades.set("DocumentTitle", "DNI " + persona.cuit() + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
		propiedades.set("CUIL", persona.cuit());
		propiedades.set("ApellidoyNombre", persona.nombreCompleto());
		propiedades.set("NroTramiteWKF", idSolicitud);
		propiedades.set("DNI", persona.numeroDocumento());
		propiedades.set("FechaAltaOrig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("FechaDig", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		propiedades.set("TipoPersona", "F");
		propiedades.set("mimetype", Texto.mimeType(nombreArchivo));
		propiedades.set("ExtArchivo", Texto.extension(nombreArchivo));
		propiedades.set("OrigenDelAlta", "HB");
		propiedades.set("IdArchivo", UUID.randomUUID().toString());

		ApiRequest request = Api.request("SubirArchivo", "digitalizacion", "POST", "/v1/documentos/{idTributario}", contexto);
		request.path("idTributario", persona.cuit());
		request.body("bytesDocumento", Texto.toBase64(bytes));
		request.body("claseDocumental", "IdentidadH");
		request.body("propiedades", propiedades);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse subirDocumentacion(ContextoHB contexto, String nombreDocumento, String claseDocumental, String documento) throws Exception {
		Persona persona = contexto.persona();
		String nroSolicitud = contexto.parametros.string("solicitud", "");
		Objeto propiedades = propiedades(contexto, claseDocumental, nombreDocumento);

		if (!nroSolicitud.isEmpty()) {
			propiedades.set("NroSolicitud", nroSolicitud);
		}

		ApiRequest request = Api.request("SubirArchivo", "digitalizacion", "POST", "/v1/documentos/{idTributario}", contexto);
		request.path("idTributario", persona.cuit());
		request.body("bytesDocumento", documento);
		request.body("claseDocumental", claseDocumental);
		request.body("propiedades", propiedades);
		if (claseDocumental.equals("FormOrigTyC")) {
			request.permitirSinLogin = true;
		}
		return Api.response(request, contexto.idCobis());
	}

	/*
	 * Para la persona que haya diseñado API Digitalización: es buena práctica
	 * diseñar un sistema para que IGNORE los inputs que no le conciernen :) de esa
	 * forma, personas como yo no tienen que hacer lo siguiente:
	 */
	private static Objeto propiedades(ContextoHB contexto, String claseDocumental, String nombreDocumento) throws Exception {
		Objeto propiedades = new Objeto();

		switch (claseDocumental) {
		case "DNI":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("CUIL", contexto.persona().cuit());
			propiedades.set("DNI", contexto.persona().numeroDocumento());
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("OrigenDelAlta", "HB");
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
			propiedades.set("OrigenDelAlta", "HB");
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
			propiedades.set("OrigenDelAlta", "HB");
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
			propiedades.set("OrigenDelAlta", "HB");
//			propiedades.set("IdArchivo", UUID.randomUUID().toString());
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			propiedades.set("NumeroCliente", contexto.idCobis());
			propiedades.set("TipoPersona", "F");
			break;

		case "FormOrigTyC":
			propiedades.set("DocumentTitle", contexto.persona().cuit() + "-" + claseDocumental);
			propiedades.set("ApellidoyNombre", contexto.persona().nombreCompleto());
			propiedades.set("CUIL", contexto.persona().cuit());
			// propiedades.set("FechaAltaElemento", new SimpleDateFormat("dd/MM/yyyy
			// HH:MM").format(new Date()));
			// propiedades.set("IdElemento", Util.secureRandom.nextInt());
			propiedades.set("Producto", "PS_Jubilados");
			propiedades.set("Canal", "HB");
			propiedades.set("NroTramiteWKF", "");
			propiedades.set("TipoPersona", "F");
			propiedades.set("ExtArchivo", Texto.extension(nombreDocumento));
			propiedades.set("mimetype", Texto.mimeType(nombreDocumento));
			break;

		default:
			throw new Exception("Clase documental no reconocida en RestArchivo.");
		}

		return propiedades;
	}
}
