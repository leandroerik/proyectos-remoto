package ar.com.hipotecario.mobile.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.servicio.*;

public class MBProcesos {

	static final String CLAVE_DATOS = "Datos";
	static final String ESTADO_O = "O";
	public static final String[] CODIGOS_PRODUCTO_DESISTIR = { "17" };
	static final String VALOR_ERRORES = "Errores";

	public static RespuestaMB estadoSolicitudes(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {
			ApiResponseMB response = RestProcesos.getEstadoSolicitudes(contexto);
			Integer diasNotififacion = ConfigMB.integer("cantidadDiasNotificacion", 45);

			if (response.hayError()) {
				return RespuestaMB.error();
			}

			// no tiene solicitudes
			if (response.codigo == 204) {
				return RespuestaMB.exito();
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -diasNotififacion);
			Date dateDDiasAtras = cal.getTime();

			List<Objeto> solicitudes = new ArrayList<Objeto>();
			for (Objeto solicitud : response.objetos("solicitudes")) {

				try {
					// Fix para evitar notificaciones viejas
					if (dateDDiasAtras.after(solicitud.date("fechaDeAlta", "yyyy-MM-dd"))) {
						continue;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

				Objeto sol = new Objeto();
				sol.set("estado", solicitud.get("estado"));
				sol.set("estadoDescipcion", descripcionEstado(solicitud.string("estado")));
				sol.set("idSolicitud", solicitud.get("idSolicitud"));
				sol.set("fechaDeAlta", solicitud.get("fechaDeAlta"));

				// para chequar q haya ingresado al proceso y no sea un desistido por no
				// finalizar desde el canal
				if ("D".equalsIgnoreCase(solicitud.string("estado"))) {
					ApiResponseMB responseDetalleProceso = RestProcesos.detalleProcesoSolicitud(contexto, solicitud.string("idSolicitud"));
					if (responseDetalleProceso.hayError() || responseDetalleProceso.codigo == 204) {
						continue;
					}
				}

				// para saber de que producto se trata cada solicitud
				ApiResponseMB consultaSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, solicitud.string("idSolicitud"));
				if (!consultaSolicitud.hayError()) {
					try {
						String dateDesc = Fecha.formato(solicitud.get("fechaDeFin") != null ? solicitud.string("fechaDeFin") : solicitud.string("fechaDeAlta"), "yyyy-MM-dd", "dd/MM/yyyy");
						sol.set("dateDesc", dateDesc);
						sol.set("producto", consultaSolicitud.objetos("Datos").get(0).objetos("Productos").get(0).string("Producto"));

						// TODO si esta reclamando documentacion recuperar que detalle de lo que se
						// reclama al usuario
						if ("RECLAMODOC".equalsIgnoreCase(solicitud.string("estado"))) {
							ApiResponseMB responseReclamoDoc = RestOriginacion.consultarReclamosDocumentacion(contexto, solicitud.string("idSolicitud"));
							if (!responseReclamoDoc.hayError()) {
								List<Objeto> reclamos = new ArrayList<Objeto>();
								for (Objeto detalleReclamo : responseReclamoDoc.objetos("detalles")) {
									Objeto reclamo = new Objeto();

									// TODO llamar a dictamenes
									ApiResponseMB dictamen = RestDictamentes.dictamenRiesgo(contexto, solicitud.string("idSolicitud"));

									if (dictamen.hayError() || dictamen.codigo == 204) {
										reclamo.set("descipcionMotivo", detalleReclamo.objetos("subMotivoDevolucion").get(0).get("descripcion"));
									} else {
										reclamo.set("descipcionMotivo", dictamen.string("observacionDevolucion"));
									}

									reclamo.set("claseDocumental", detalleReclamo.objetos("claseDocumental").get(0).get("id"));
									reclamo.set("fechaVencimiento", responseReclamoDoc.string("fechaVencimiento"));
									reclamo.set("idSolicitud", solicitud.string("idSolicitud"));
									reclamos.add(reclamo);
								}
								sol.set("reclamos", reclamos);
							}
						}

						solicitudes.add(sol);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			respuesta.set("solicitudes", solicitudes);
		} catch (Exception e) {
			return RespuestaMB.error();
		}
		return respuesta;
	}

	private static String descripcionEstado(String estado) {
		String descripcion = "";
		try {
			switch (estado) {
			case "F":
				descripcion = "FINALIZADA";
				break;
			case "RECLAMODOC":
				descripcion = "RECLAMO DE DOCUMENTACION";
				break;
			case "R":
				descripcion = "RECHAZADA";
				break;
			case "D":
				descripcion = "DESISTIDA";
				break;
			default:
				break;
			}
		} catch (Exception e) {
			//
		}
		return descripcion;
	}

	public static RespuestaMB desistirSolicitudes(ContextoMB contexto) {
		List<Objeto> solicitudesDemitirLimitesTc = consultarSolicitudesDesistir(contexto);
		solicitudesDemitirLimitesTc.stream().map(objeto -> (String) objeto.get("idSolicitud")).forEach(idSolicitud -> {
			ApiResponseMB response = RestAumentoLimiteTC.desistirSolicitud(contexto, idSolicitud);
			if (response.hayError()) {
				Solicitud.logApiVentas(contexto, idSolicitud, "desistirSolicitud", response);
			}
		});
		return RespuestaMB.exito();
	}

	public static List<Objeto> consultarSolicitudesDesistir(ContextoMB contexto) {
		Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigMB.longer("solicitud_dias_vigente", 30L));
		List<Objeto> solicitudes = new ArrayList<>();

		try {
			ApiResponseMB solicitudesCliente = RestAumentoLimiteTC.consultarSolicitudes(contexto, cantidadDias);
			if (hayErrores(solicitudesCliente)) {
				return solicitudes;
			}
			for (Objeto datos : solicitudesCliente.objetos(CLAVE_DATOS)) {
				if (ESTADO_O.equals(datos.string("Estado"))) {
					for (Objeto producto : datos.objetos("Productos")) {
						if (Arrays.asList(CODIGOS_PRODUCTO_DESISTIR).contains(producto.string("tipoProducto"))) {
							Objeto item = new Objeto();
							item.set("fechaAlta", datos.string("FechaAlta"));
							item.set("producto", producto.string("Producto"));
							item.set("id", producto.string("Id"));
							item.set("idSolicitud", datos.string("IdSolicitud"));
							item.set("estado", datos.string("Estado"));
							solicitudes.add(item);
						}
					}
				}
			}
		} catch (Exception e) {
			return solicitudes;
		}

		return solicitudes;
	}

	private static boolean hayErrores(ApiResponseMB error) {
		return error.hayError() || !error.objetos(VALOR_ERRORES).isEmpty();
	}

}
