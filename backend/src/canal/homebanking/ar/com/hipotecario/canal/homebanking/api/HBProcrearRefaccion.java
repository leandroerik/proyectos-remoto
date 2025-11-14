package ar.com.hipotecario.canal.homebanking.api;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.DataFile;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.lib.Archivo;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Imagen;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudPrestamo;

public class HBProcrearRefaccion {

	public static Respuesta modal(ContextoHB contexto) {
		/*
		 * LocalTime localTime = LocalTime.now();
		 * 
		 * 
		 * Boolean enHorario = (localTime.isAfter(LocalTime.parse("06:00:00")) &&
		 * localTime.isBefore(LocalTime.parse("21:00:00"))); if (!enHorario &&
		 * Config.esOpenShift()) { return Respuesta.exito("mostrarModal", false); }
		 */
		if (contexto.sesion.ofertaPrestamoMostrada) {
			return Respuesta.exito("mostrarModal", false);
		}

		ApiResponse response = ofertasProcrear(contexto);
		if (response.hayError()) {
			return Respuesta.exito("mostrarModal", false);
		}

		LocalTime localTime = LocalTime.now();
		Boolean enHorario = (localTime.isAfter(LocalTime.parse("06:00:00")) && localTime.isBefore(LocalTime.parse("21:00:00")));

		Boolean mostrarModal = false;
		Respuesta respuesta = new Respuesta();
		respuesta.set("mostrarModal", false);

		boolean tenerEnCuentaElHorario = false;
		for (Objeto item : response.objetos()) {
			System.out.println(item);
			if (item.string("estado").equals("SO")) {
				String nemonico = item.string("nemonico");
				contexto.sesion.nuevoNemonico = (item.string("nemonico"));
				// if (!"PROREFHOG2".equals(nemonico)) {
				if ("PPPROMATE2".equals(nemonico) || "PROREFHOG1".equals(nemonico) || "PROREFHOG2".equals(nemonico)) {
					Integer cantidadVecesMostradas = cantidadVecesMostradas(contexto, nemonico);
					if (cantidadVecesMostradas <= 5 && !contexto.tienePrestamosProcrear(nemonico)) {
						mostrarModal = true;
						contexto.sesion.ofertaPrestamoMostrada = (true);
						incrementarCantidadVecesMostradas(contexto, nemonico);
						respuesta.add("nemonicos", nemonico);
						// este if si bien no hace falta, lo dejo.
						// son los procrear que están con fuera de horario.
						// podría tranquilamente dejarlo con: tenerEnCuentaElHorario = true;
						// sin el if, pero prefiero que quede así, ya que en un futuro es probable que
						// el proximo procrear no texnga el fuera de horario
						if ("PPPROMATE2".equals(nemonico) || "PROREFHOG1".equals(nemonico) || "PROREFHOG2".equals(nemonico)) {
							tenerEnCuentaElHorario = true;
						}
					}
				}
			}
		}

		if (!tenerEnCuentaElHorario) {
			enHorario = true;
		} // la validación solo la hago para los procrear que tenemos en HB por las dudas.
			// sino va a la consolidada de préstamos.
		respuesta.set("enHorario", enHorario);
		respuesta.set("mostrarModal", mostrarModal);

		return respuesta;
	}

	public static Respuesta documentacion(ContextoHB contexto) {
//		Boolean usarMock = true;
//		if (usarMock && !Config.esOpenShift()) {
//			Respuesta respuesta = new Respuesta();
//			respuesta.add("documentos", new Objeto().set("id", "1100").set("descripcion", "SOLICITUD (y Anexos)"));
//			respuesta.add("documentos", new Objeto().set("id", "2201").set("descripcion", "DOCUMENTO de IDENTIDAD"));
//			respuesta.add("documentos", new Objeto().set("id", "4600").set("descripcion", "Registro de firmas"));
//			respuesta.add("documentos", new Objeto().set("id", "3500").set("descripcion", "PAGARE"));
//			respuesta.add("documentos", new Objeto().set("id", "2210").set("descripcion", "COMPROBANTE de DOMICILIO"));
//			respuesta.add("documentos", new Objeto().set("id", "9903").set("descripcion", "Otra Documentación"));
//			return respuesta;
//		}

		String idSolicitud = contexto.parametros.string("idSolicitud");
		Boolean esResubida = contexto.parametros.bool("esResubida", false);
		Boolean usarCache = contexto.parametros.bool("usarCache", false); // en este caso prefiero que se limpie a menos que lo pida de front

		if (!usarCache) {
			Api.eliminarCache(contexto, "DocumentacionRequeridaResubidaBPM", contexto.idCobis());
			Api.eliminarCache(contexto, "DocumentacionRequeridaBPM", contexto.idCobis());
		}

		if (Objeto.anyEmpty(idSolicitud)) {
			return Respuesta.parametrosIncorrectos();
		}
		if (esResubida) {
			contexto.sesion.documentacionResubirBpm = (null);
			/*
			 * Boolean usarMock = true; if (usarMock && !Config.esOpenShift()) { Respuesta
			 * respuesta = new Respuesta(); //respuesta.add("documentos", new
			 * Objeto().set("id", "1100").set("descripcion", "SOLICITUD (y Anexos)"));
			 * respuesta.add("documentos", new Objeto().set("id", "2201").set("descripcion",
			 * "DOCUMENTO de IDENTIDAD")); respuesta.add("documentos", new
			 * Objeto().set("id", "4600").set("descripcion", "Registro de firmas"));
			 * //respuesta.add("documentos", new Objeto().set("id",
			 * "3500").set("descripcion", "PAGARE")); respuesta.add("documentos", new
			 * Objeto().set("id", "2210").set("descripcion", "COMPROBANTE de DOMICILIO"));
			 * //respuesta.add("documentos", new Objeto().set("id",
			 * "9903").set("descripcion", "Otra Documentación")); return respuesta; }
			 */
			ApiRequest request = Api.request("DocumentacionRequeridaResubidaBPM", "procesos", "GET", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/reclamos/documentacion/documentos", contexto);
			request.path("idSolicitud", idSolicitud);

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError() || !response.objetos("Errores").isEmpty()) {
				return Respuesta.error();
			}

			Respuesta respuesta = new Respuesta();
			Objeto resubida = new Objeto();
			for (Objeto item : response.objetos("documentos")) {
				Objeto objeto = new Objeto();
				// objeto.set("id", item.string("idDocumento"));
				objeto.set("descripcion", item.string("descripcion"));
				objeto.set("numeroTributarioIntegrante", ""); // este dato me hace falta
				Integer version = item.integer("version", 0);
				version = version + 1;

				objeto.set("id", item.string("NumeroTributarioIntegrnate", contexto.persona().cuit()) + "_" + item.string("idDocumento") + "-v" + version.toString());
				String id = item.string("idDocumento");
				String path = HBProcrearRefaccion.rutaTemporal(contexto, idSolicitud);

				path = path + id + "-v" + version.toString() + ".pdf";
				Boolean existeArchivo = Archivo.existe(path);

				if (Objeto.setOf("1100").contains(item.string("Id")) || Objeto.setOf("4600", "3500", "9903").contains(id)) {
					if (!existeArchivo) {
						byte[] pdf = Archivo.pdf();
						Archivo.escribirBinario(path, pdf);
					}
				} else {
					respuesta.add("documentos", objeto);
				}

				resubida.set("idSolicitud", idSolicitud);
				Objeto elements = new Objeto();
				elements.set("descripcion", item.string("descripcion"));
				elements.set("idDocumento", item.string("idDocumento"));
				elements.set("integranteCuil", item.string("NumeroTributarioIntegrnate", contexto.persona().cuit()));
				elements.set("version", version);
				resubida.add("documentos", new Objeto().set("elements", elements));
				contexto.sesion.documentacionResubirBpm = (resubida.toJson());
			}

			return respuesta;
		} else {
			Solicitud solicitud = Solicitud.solicitudProcrearRefaccion(contexto, contexto.persona().cuit());
			if (solicitud != null) {
				String resoluctionMotor = solicitud.ResolucionCodigo;
				String derivarA = solicitud.DerivarA;
				if ("AV".equals(resoluctionMotor) && "N".equals(derivarA)) {
					return Respuesta.exito();
				}
			}

			ApiRequest request = Api.request("DocumentacionRequeridaBPM", "ventas_windows", "GET", "/solicitudes/{numeroSolicitud}/documentacion", contexto);
			request.path("numeroSolicitud", idSolicitud);
			request.query("solo-obligatorios", "true");
			request.query("consultar-contenedor", "false");
			request.cacheSesion = true;

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError() || !response.objetos("Errores").isEmpty()) {
				return Respuesta.error();
			}

			List<String> listaIds = new ArrayList<>();

			Respuesta respuesta = new Respuesta();
			for (Objeto datos : response.objetos("Datos")) {
				for (Objeto item : datos.objetos("Documentacion")) {
					Objeto objeto = new Objeto();
					objeto.set("id", item.string("Id"));
					objeto.set("descripcion", item.string("Descripcion"));
					objeto.set("numeroTributarioIntegrante", "");
					if (!"".equals(item.string("NumeroTributarioIntegrnate"))) {
						/*
						 * if
						 * (item.string("NumeroTributarioIntegrnate").equals(contexto.persona().cuit()))
						 * { objeto.set("id", item.string("NumeroTributarioIntegrnate") + "_" +
						 * item.string("Id")); objeto.set("numeroTributarioIntegrante",
						 * item.string("NumeroTributarioIntegrnate")); } else { objeto.set("id",
						 * item.string("NumeroTributarioIntegrnate") + "_" + item.string("Id") + "-v2");
						 * objeto.set("numeroTributarioIntegrante",
						 * item.string("NumeroTributarioIntegrnate")); }
						 */
						Integer cantidadMismoId = 0;
						for (String itemLista : listaIds) {
							if (itemLista.equals(item.string("Id"))) {
								cantidadMismoId++;
							}
						}
						String version = "";
						if (cantidadMismoId > 0) {
							cantidadMismoId++;
							version = "-v" + (cantidadMismoId).toString();
						}

						objeto.set("id", item.string("NumeroTributarioIntegrnate") + "_" + item.string("Id") + version);
						objeto.set("numeroTributarioIntegrante", item.string("NumeroTributarioIntegrnate"));
					}

					String id = item.string("Id");
					String path = HBProcrearRefaccion.rutaTemporal(contexto, idSolicitud);
					path = path + id + ".pdf";
					Boolean existeArchivo = Archivo.existe(path);

					listaIds.add(id);

					if (Objeto.setOf("1100", "4600", "3500", "9903").contains(id)) {
						if (!existeArchivo) {
							byte[] pdf = Archivo.pdf();
							Archivo.escribirBinario(path, pdf);
						}
					} else {
						respuesta.add("documentos", objeto);
					}
				}
			}
			return respuesta;
		}

	}

	public static Respuesta subirDocumentacion(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String idDocumentacion = contexto.parametros.string("idDocumentacion");
		Set<String> archivo1 = contexto.archivos.keySet();

		if (Objeto.anyEmpty(idSolicitud, idDocumentacion, archivo1)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (idDocumentacion != null) {
			String path = HBProcrearRefaccion.rutaTemporal(contexto, idSolicitud);

			List<byte[]> archivos = new ArrayList<>();
			for (Integer i = 1; i <= 48; ++i) {
				DataFile archivo = contexto.archivos.get("archivo" + i);
				byte[] binario = archivo != null ? archivo.bytes : null;
				if (binario != null) {
					String nombreArchivo = archivo.name;
					if (nombreArchivo.toLowerCase().endsWith(".pdf")) {
						archivos.add(binario);
					} else {
						byte[] binarioComprimido = Imagen.comprimir(binario, nombreArchivo, ConfigHB.bigDecimal("caldidad_imagen_bpm", "0.60").floatValue());
						archivos.add(binarioComprimido);
					}
				}
			}
			byte[] pdf = Archivo.pdf(archivos);
			Archivo.escribirBinario(path + idDocumentacion + ".pdf", pdf);
		}

		return Respuesta.exito();
	}

	public static Respuesta finalizarResubirSubirDocumentacion(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");

		ApiRequest request = Api.request("PostDocumentacionRequeridaResubidaBPM", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/notificacion/documentacion/digital", contexto);
		request.path("idSolicitud", idSolicitud);
		if (contexto.sesion.documentacionResubirBpm != null) {
			request.body(Objeto.fromJson(contexto.sesion.documentacionResubirBpm));
		} else {
			request.body(new Objeto());
		}

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			return Respuesta.error();
		}

		String rutaOrigen = rutaTemporal(contexto, idSolicitud).replace("/1/documentacionAdjunta/", "").replace("/1/documentacionAdjunta", "");
		String rutaDestino = rutaFinal(contexto, idSolicitud).replace("/1/documentacionAdjunta/", "").replace("/1/documentacionAdjunta", "");
		File origen = new File(rutaOrigen);
		if (origen.isDirectory()) {
			File destino = new File(rutaDestino);
			origen.renameTo(destino);
		}

		return Respuesta.exito();
	}

	public static Respuesta segurosVida(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		respuesta.add("segurosVida", new Objeto().set("id", "40").set("descripcion", "BHN VIDA SA"));
		respuesta.add("segurosVida", new Objeto().set("id", "4786211").set("descripcion", "CARUSO CIA ARG DE SEGUROS SA"));
		return respuesta;
	}

	public static Respuesta postConsulta(ContextoHB contexto) {
		ApiRequest request = Api.request("EstadoSolicitudBPM", "procesos", "GET", "/procesos-de-negocio/v1/solicitud/estado", contexto);
		request.query("pNroDoc", contexto.persona().numeroDocumento());

		// emm: cambio la cantidad de días para consulta. Lo hago por variable de
		// entorno.
		// no puedo sacar este parámetro porque por default es 30.
		// request.query("pDias", "90");
		request.query("pDias", ConfigHB.string("dias_consulta_estado_solicitud_bpm", "90"));

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			return Respuesta.error();
		}

//		Boolean usarMock = true;
//		if (usarMock && !Config.esOpenShift()) {
//			Respuesta respuesta = new Respuesta();
//			Objeto oferta = new Objeto();
//			oferta.set("idSolicitud", "10184305");
//			oferta.set("idPrestamo", "252534");
//			oferta.set("estado", "NUEVA");
//			oferta.set("monto", new BigDecimal("90000"));
//			oferta.set("valorOferta", new BigDecimal("100000"));
//			oferta.set("plazo", 60);
//			oferta.set("tasa", new BigDecimal("101"));
//			oferta.set("cft", new BigDecimal("220.45"));
//			oferta.set("esOfertaMejorableComprobandoIngresos", true);
//			
//			//nuevos campos
//			oferta.set("tipo", "Refacción");
//			oferta.set("moneda", "Pesos");
//			oferta.set("simboloMoneda", "$");
//			oferta.set("montoFormateado", Formateador.importe(new BigDecimal("100000")));
//			oferta.set("plazoFormateado", "60 meses");
//			oferta.set("tnaFormateada", "101,00%");
//			oferta.set("cftFormateado", "30,30%");
//			oferta.set("cuota", new BigDecimal("1588.00"));
//			oferta.set("cuotaFormateada", "1.588,00");
//			oferta.set("formaPago", "Débito automatico");
//			oferta.set("destinoManoObra", new BigDecimal("50000"));
//			oferta.set("destinoMateriales", new BigDecimal("50000"));
//			oferta.set("destinoManoObraFormateado", Formateador.importe(new BigDecimal("50000")));
//			oferta.set("destinoMaterialesFormateado", Formateador.importe(new BigDecimal("50000")));
//		
//			Objeto originacion = new Objeto();  
//			originacion.set("generaCuenta", true);
//			originacion.set("generaTarjetaDebito", true);
//			originacion.set("debeSubirDocumentacion", false); //es el que usas en argentina construye cuando el tipo es nuevo login
//			
//			respuesta.set("oferta", oferta);
//			respuesta.set("originacion", originacion);
//			
//			return respuesta;
//		}

		String descripcion = response.string("estado");

		Boolean documentacionAdicional = Objeto.setOf("PENDIENTE DE DOCUMENTACION ADICIONAL", "RECLAMO DE DOCUMENTACION").contains(descripcion);
		if (documentacionAdicional) {
			Respuesta respuesta = new Respuesta();
			Objeto oferta = new Objeto();
			oferta.set("idSolicitud", response.string("idSolicitud"));
			respuesta.set("oferta", oferta);
			return respuesta;
		}

		Boolean confirmarOperaciones = Objeto.setOf("PENDIENTE CONFIRMACION OPERACIONES").contains(descripcion);
		if (!confirmarOperaciones) {
			return Respuesta.estado("SIN_SOLICITUD");
		}

		String idSolicitud = response.string("idSolicitud");
		if (idSolicitud.isEmpty()) {
			return Respuesta.estado("SIN_SOLICITUD");
		}
		Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
		SolicitudPrestamo prestamo = solicitud.prestamo(contexto);

		Respuesta respuesta = new Respuesta();
		Objeto oferta = new Objeto();
		oferta.set("idSolicitud", solicitud.IdSolicitud);
		oferta.set("idPrestamo", solicitud.idPrestamo());
		oferta.set("monto", prestamo.MontoAprobado);
//		oferta.set("valorOferta", valorOferta);
		oferta.set("plazo", prestamo.Plazo);
		oferta.set("tasa", prestamo.Tasa);
		oferta.set("cft", prestamo.CFT);
//		oferta.set("esOfertaMejorableComprobandoIngresos", resolucionMotor.EsOfertaMejorableComprobandoIngresos);

		oferta.set("tipo", "Refacción");
		oferta.set("moneda", "Pesos");
		oferta.set("simboloMoneda", "$");
		oferta.set("montoFormateado", Formateador.importe(prestamo.MontoAprobado));
		oferta.set("plazoFormateado", prestamo.Plazo + " meses");
		oferta.set("tnaFormateada", Formateador.importe(prestamo.Tasa));
		oferta.set("cftFormateado", Formateador.importe(prestamo.CFT));
		oferta.set("cuota", prestamo.importeCuota);
		oferta.set("cuotaFormateada", Formateador.importe(prestamo.importeCuota));
		oferta.set("formaPago", "Débito automatico");
		oferta.set("destinoManoObra", prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.UP));
		oferta.set("destinoMateriales", prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.DOWN));
		oferta.set("destinoManoObraFormateado", Formateador.importe(prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.UP)));
		oferta.set("destinoMaterialesFormateado", Formateador.importe(prestamo.MontoAprobado.divide(new BigDecimal("2"), RoundingMode.DOWN)));
		respuesta.set("oferta", oferta);

		/*
		 * // respuesta.add("nemonicos", "GRUPO3"); respuesta.add("nemonicos",
		 * "GREFACTYC");
		 */
		if ("AV".equals(solicitud.ResolucionCodigo)) {
			respuesta.add("nemonicos", "GREFACTYC");
		} else {
			respuesta.add("nemonicos", "GRUPO3");
		}
		if (solicitud.contieneCajaAhorroPesos()) {
			respuesta.add("nemonicos", "CASOLIC");
		}

		return respuesta;
	}

	public static Respuesta postFinalizar(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
//		Solicitud solicitud = Solicitud.solicitudProcrearRefaccion(contexto, contexto.persona().cuit());

		ApiRequest request = Api.request("PostFinalizarBPM", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/notificacion/accion", contexto);
		request.path("idSolicitud", idSolicitud);
		request.body("idSolicitud", Integer.valueOf(idSolicitud));
		request.body("idAccion", 1);

		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	/* ========== AUXILIAR ========== */
	public static ApiResponse ofertasProcrear(ContextoHB contexto) {
		ApiRequest request = Api.request("OfertasProcrear", "prestamos", "GET", "/v1/prestamos/{id}/beneficiario", contexto);
		request.path("id", contexto.idCobis());
		request.query("Tipo", "C");
		request.cacheSesion = true;
		request.permitirSinLogin = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
		return response;
	}

	public static BigDecimal valorOferta(ContextoHB contexto, String nemonico) {
		ApiResponse response = ofertasProcrear(contexto);
		if (response.hayError()) {
			return null;
		}

		for (Objeto item : response.objetos()) {
			if (item.string("estado").equals("SO")) {
				String nemonicoActual = item.string("nemonico");
				/*
				 * if (nemonico.equals(nemonicoActual)) { return item.bigDecimal("valorOferta");
				 * }
				 */
				if ("PROREFHOG1".equals(nemonicoActual) || "PROREFHOG2".equals(nemonicoActual)) {
					return item.bigDecimal("valorOferta");
				}
			}
		}

		// DUMMY
//		if (!Config.esOpenShift()) {
//			return new BigDecimal("100000");
//		}

		return null;
	}

	public static Integer cantidadVecesMostradas(ContextoHB contexto, String nemonico) {
		try {
			SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ?";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			Integer cantidad = Sql.response(sqlRequest).registros.size();
			return cantidad;
		} catch (Exception e) {
		}
		return 0;
	}

	public static void incrementarCantidadVecesMostradas(ContextoHB contexto, String nemonico) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertContador", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static String rutaTemporal(ContextoHB contexto, String idSolicitud) {
		String path = ConfigHB.string("path_documentacion_bpm").replace("SUCURSAL", "temp_SUCURSAL").replace("{NUMERO_SOLICITUD}", idSolicitud);
		new File(path).mkdirs();
		return path;
	}

	public static String rutaFinal(ContextoHB contexto, String idSolicitud) {
		String path = ConfigHB.string("path_documentacion_bpm").replace("{NUMERO_SOLICITUD}", idSolicitud);
		return path;
	}
}
