package ar.com.hipotecario.mobile.api;

import java.util.Set;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.DocumentacionService;
import ar.com.hipotecario.mobile.servicio.RestArchivo;

public class MBDocumentacion {

	public static RespuestaMB documentacionXSolicitud(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		RespuestaMB respuesta = new RespuestaMB();

		if (Objeto.empty(idSolicitud)) {
			RespuestaMB.parametrosIncorrectos();
		}

		Objeto documentacionRequerida = DocumentacionService.docRequeridoXCanalAmarillo(contexto, idSolicitud);
		if (Objeto.empty(documentacionRequerida)) {
			return RespuestaMB.estado("ERROR_SIN_DOCUMENTOS_REQUERIDOS");
		}

		return respuesta.set("documentacion", documentacionRequerida);
	}

	// esta version no realiza updates de la persona, se encarga unicamente de subir
	// los arhivos al contenedor
	public static RespuestaMB guardarDocumentacionV2(ContextoMB contexto) {
		Objeto datos = contexto.parametros;

		if (datos == null) {
			return RespuestaMB.parametrosIncorrectos();
		}

		try {
			if (datos.existe("documentos") && datos.objeto("documentos") != null) {
				for (Objeto doc : datos.objetos("documentos")) {
					String claseDocumental = doc.string("claseDocumental", null);

					if (claseDocumental == null || claseDocumental.isEmpty()) {
						return RespuestaMB.estado("SIN_CLASE_DOCUMENTAL");
					}

					boolean subeArchivo = !doc.string("archivo").equals("");

					if (subeArchivo) {
						String nombre = doc.string("nombre", null);

						if (nombre == null || nombre.isEmpty()) {
							return RespuestaMB.estado("SIN_NOMBRE_ARCHIVO");
						}

						nombre = setExtensionNombreArchivo(nombre, doc.string("extension"));
						ApiResponseMB res = RestArchivo.subirDocumentacion(contexto, nombre, claseDocumental, doc.string("archivo"));

						if (res.hayError()) {
							return RespuestaMB.estado("ERROR_SUBIENDO_ARCHIVO");
						}
					} else {
						return RespuestaMB.estado("ERROR_SIN_ARCHIVO");
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return RespuestaMB.error();
		}
		contexto.sesion().setAdjuntaDocumentacion(true);
		return RespuestaMB.exito();
	}

	private static String setExtensionNombreArchivo(String nombreArchivo, String extension) {
		Set<String> formatos = Objeto.setOf(".pdf", ".png", ".jpg", ".peg", ".jpe", ".jpg");
		if (!formatos.contains(nombreArchivo)) {
			String[] ext = extension.split("/");
			extension = ext.length > 1 ? ext[1] : ext[0];
			extension = extension.replace(".", "");
			nombreArchivo = nombreArchivo + "." + extension;
		}
		return nombreArchivo;
	}

}
