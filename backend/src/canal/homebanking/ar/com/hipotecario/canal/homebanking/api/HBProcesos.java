package ar.com.hipotecario.canal.homebanking.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;

public class HBProcesos {

	static final String CLAVE_DATOS = "Datos";
	static final String ESTADO_O = "O";
	public static final String[] CODIGOS_PRODUCTO_DESISTIR = { "17" }; // TODO: agregar Pedido Adicional TC
	static final String VALOR_ERRORES = "Errores";

	public static Respuesta estadoSolicitudes(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		try {
			ApiResponse response = RestProcesos.getEstadoSolicitudes(contexto);

			if (response.hayError()) {
				return Respuesta.error();
			}

			if (response.codigo == 204) {
				return Respuesta.exito();
			}
			Integer cantidadDiasNotificacion = ConfigHB.integer("cantidadDiasNotificacion", 45);
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -cantidadDiasNotificacion);
			Date dateDDiasAtras = cal.getTime();

			List<Objeto> solicitudes = new ArrayList<Objeto>();
			for (Objeto solicitud : response.objetos("solicitudes")) {

				try {
					if (dateDDiasAtras.after(solicitud.date("fechaDeAlta", "yyyy-MM-dd"))) {
						continue;
					}
				} catch (Exception e) {
				}

				Objeto sol = new Objeto();
				sol.set("estado", solicitud.get("estado"));
				sol.set("estadoDescipcion", descripcionEstado(solicitud.string("estado")));
				sol.set("idSolicitud", solicitud.get("idSolicitud"));
				sol.set("fechaDeAlta", solicitud.get("fechaDeAlta"));

				if ("D".equalsIgnoreCase(solicitud.string("estado"))) {
					ApiResponse responseDetalleProceso = RestProcesos.detalleProcesoSolicitud(contexto, solicitud.string("idSolicitud"));
					if (responseDetalleProceso.hayError() || responseDetalleProceso.codigo == 204) {
						continue;
					}
				}

				ApiResponse consultaSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, solicitud.string("idSolicitud"));
				if (!consultaSolicitud.hayError()) {
					try {
						String dateDesc = Fecha.formato(solicitud.get("fechaDeFin") != null ? solicitud.string("fechaDeFin") : solicitud.string("fechaDeAlta"), "yyyy-MM-dd", "dd/MM/yyyy");
						sol.set("dateDesc", dateDesc);
						sol.set("producto", consultaSolicitud.objetos("Datos").get(0).objetos("Productos").get(0).string("Producto"));

						if ("RECLAMODOC".equalsIgnoreCase(solicitud.string("estado"))) {
							ApiResponse responseReclamoDoc = RestOriginacion.consultarReclamosDocumentacion(contexto, solicitud.string("idSolicitud"));
							if (!responseReclamoDoc.hayError()) {
								List<Objeto> reclamos = new ArrayList<Objeto>();
								for (Objeto detalleReclamo : responseReclamoDoc.objetos("detalles")) {
									Objeto reclamo = new Objeto();
									ApiResponse dictamen = RestDictamentes.dictamenRiesgo(contexto, solicitud.string("idSolicitud"));

									if (dictamen.hayError() || dictamen.codigo == 204) {
										reclamo.set("descripcionMotivo", detalleReclamo.objetos("subMotivoDevolucion").get(0).get("descripcion"));
									} else {
										reclamo.set("descripcionMotivo", dictamen.string("observacionDevolucion"));
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
					}
				}
			}

			for (Objeto item : solicitudes) {
				respuesta.add("solicitudes", item);
			}
			// respuesta.set("solicitudes", solicitudes);
		} catch (Exception e) {
			return Respuesta.error();
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
			case "D":
				descripcion = "DESISTIDA";
				break;
			case "R":
				descripcion = "RECHAZADA";
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
		return descripcion;
	}

	public static Respuesta desistirSolicitudes(ContextoHB contexto) {
		List<Objeto> solicitudesDemitirLimitesTc = consultarSolicitudesDesistir(contexto);
		solicitudesDemitirLimitesTc.stream().map(objeto -> (String) objeto.get("idSolicitud")).forEach(idSolicitud -> {
			ApiResponse response = RestVenta.desistirSolicitud(contexto, idSolicitud);
			if (response.hayError()) {
				Solicitud.logApiVentas(contexto, idSolicitud, "desistirSolicitud", response);
			}
		});
		return Respuesta.exito();
	}

	public static List<Objeto> consultarSolicitudesDesistir(ContextoHB contexto) {
		Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigHB.longer("solicitud_dias_vigente", 30L));
		List<Objeto> solicitudes = new ArrayList<>();

		try {
			ApiResponse solicitudesCliente = RestVenta.consultarSolicitudes(contexto, cantidadDias);
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

	private static boolean hayErrores(ApiResponse error) {
		return error.hayError() || !error.objetos(VALOR_ERRORES).isEmpty();
	}

}
