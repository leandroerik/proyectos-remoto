package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestVenta;

public abstract class BuscadorPrestamosVerdes {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(10);
		for (String idCobis : listaCobis()) {
			executorService.submit(() -> {
				try {
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");

					/*---------------*/

					boolean encontro = buscarCaso(contexto);
					if (encontro) {
						String linea = "encontro caso verde prestamo" + idCobis;
						System.out.println(linea);
//						System.exit(0);
					}

					/*---------------*/

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static boolean buscarCaso(ContextoMB contexto) {
		ApiResponseMB response = RestVenta.consultarSolicitudes(contexto);
		for (Objeto item : response.objetos("Datos")) {
			if (!item.string("Estado").equals("D")) {
				String id = item.string("IdSolicitud");
				RestVenta.desistirSolicitud(contexto, id);
			}
		}

		String idSolicitud = "";
		ApiResponseMB solicitud = RestVenta.generarSolicitud(contexto);
		if (solicitud.hayError() || !solicitud.objetos("Errores").isEmpty()) {
			return false;
		}
		idSolicitud = solicitud.objetos("Datos").get(0).string("IdSolicitud");

		// INTEGRANTE
		ApiResponseMB integrante = RestVenta.generarIntegrante(contexto, idSolicitud);
		if (integrante.hayError() || !integrante.objetos("Errores").isEmpty()) {
			if (!"".equals(idSolicitud))
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			return false;
		}

		// INTEGRANTE
		ApiResponseMB prestamo = RestVenta.agregarPrestamoPersonal(contexto, idSolicitud);
		if (prestamo.hayError() || !prestamo.objetos("Errores").isEmpty()) {
			if (!"".equals(idSolicitud))
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			return false;
		}

		// EVALUAR SOLICITUD
		ApiResponseMB evaluacionSolicitud = RestVenta.evaluarSolicitud(contexto, idSolicitud);
		if (evaluacionSolicitud.hayError() || !evaluacionSolicitud.objetos("Errores").isEmpty()) {
			if (!"".equals(idSolicitud))
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			return false;
		}
		if (evaluacionSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("CT")) {
			if (!"".equals(idSolicitud))
				RestVenta.desistirSolicitud(contexto, idSolicitud);

//			if (evaluacionSolicitud.objetos("Datos").get(0).string("Explicacion") != null 
//					&& evaluacionSolicitud.objetos("Datos").get(0).string("Explicacion").contains("Versión de DNI")) {
//				ApiRequest request = Api.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
//				request.header("x-usuario", Config.string("configuracion_usuario"));
//				// request.path("id", contexto.idCobis());
//				request.path("id", contexto.persona().cuit());
//				request.body("idVersionDocumento", "A");

//				ApiResponse response = Api.response(request, contexto.idCobis());
//				if (response.hayError()) {
//					return false; //si dio error el cambio de versión de documento retorno.
//				}
//				return buscarCaso(contexto); //Vuelvo a llamarlo ya que tenía el error de que le faltaba la versión de dni.
//			}

			return false;
		}
		if (evaluacionSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("RE")) {
			if (!"".equals(idSolicitud))
				RestVenta.desistirSolicitud(contexto, idSolicitud);
			return false;
		}
		return true;
	}

	public static List<String> listaCobis() {
		String tarjetas = Archivo.leer("D:\\Users\\C05302\\Desktop\\cobis.txt");
		List<String> lista = Arrays.asList(tarjetas.split("\\n"));
		return lista;
	}
}
