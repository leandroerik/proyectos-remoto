package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class DocumentacionService {

	private static final String documento_requerido_canal_relacion_dependencia = "2230";
	private static final String documento_requerido_canal_monotributista = "2264_2261";
	private static final String documento_requerido_canal_jubilado = "2281";

	public static List<Objeto> documentacionXSolicitud(ContextoHB contexto, String idSolicitud) {
		List<Objeto> documentos = new ArrayList<Objeto>();

		ApiRequest request = Api.request("DocumentacionRequeridaBPM", "ventas_windows", "GET", "/solicitudes/{numeroSolicitud}/documentacion", contexto);
		request.path("numeroSolicitud", idSolicitud);
		request.query("solo-obligatorios", "true");
		request.query("consultar-contenedor", "true");
		request.cacheSesion = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			return documentos;
		}

		documentos = response.objetos("Datos").get(0).existe("Documentacion") ? response.objetos("Datos").get(0).objetos("Documentacion") : new ArrayList<Objeto>();

		return documentos;
	}

	public static Objeto docRequeridoXCanalAmarillo(ContextoHB contexto, String idSolicitud) {
		List<Objeto> datos = documentacionXSolicitud(contexto, idSolicitud);
		String titulo = "";

		Objeto salida = new Objeto();
		for (Objeto item : datos) {
			if (Objeto.setOf(documento_requerido_canal_relacion_dependencia.split("_")).contains(item.string("Id"))) { // relacion de dependencia
				titulo = "Los últimos 3 recibos de sueldo.";
			}
			if (Objeto.setOf(documento_requerido_canal_monotributista.split("_")).contains(item.string("Id"))) { // monotributista
				titulo = "Los últimos 3 comprobantes de pago.";
			}
			if (Objeto.setOf(documento_requerido_canal_jubilado.split("_")).contains(item.string("Id"))) { // jubilado
				titulo = "Los últimos 3 recibos de haberes.";
			}
			if (!Objeto.empty(titulo)) {
				salida = new Objeto().set("claseDocumental", item.string("ClaseDocumental")).set("titulo", titulo);
				break;
			}

		}
		return salida;
	}

}
