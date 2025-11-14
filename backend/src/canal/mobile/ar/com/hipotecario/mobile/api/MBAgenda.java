package ar.com.hipotecario.mobile.api;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestAgenda;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;

public class MBAgenda {

	private static String flagTurnosOnline = "prendido_turnos_online_api";

	public static RespuestaMB tipoTurnosHabilitados(ContextoMB contexto) {
		try {
			String cuil = contexto.persona().cuit();

			if (cuil == null || cuil.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			ApiResponseMB response = RestAgenda.habilitado(contexto, cuil);
			if (response.hayError()) {
				return RespuestaMB.error();
			}

			List<Objeto> objList = new ArrayList<Objeto>();

			if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendio_filtro_tipo_turno")) {
				for (Objeto tipos : response.objetos()) {
					Objeto item = new Objeto();
					item.set("codTipoTurno", tipos.string("codTipoTurno"));
					item.set("descTipoTurno", tipos.string("descTipoTurno"));
					objList.add(item);
				}
			} else {
				for (Objeto tipos : response.objetos()) {
					Objeto item = new Objeto();
					if (tipos.bool("visible")) {
						item.set("codTipoTurno", tipos.string("codTipoTurno"));
						item.set("descTipoTurno", tipos.string("descTipoTurno"));
						item.set("enSucursalesEspecificas", tipos.bool("enSucursalesEspecificas"));
						objList.add(item);
					}
				}
			}

			RespuestaMB respuesta = new RespuestaMB();
			return respuesta.set("datos", objList);
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB postAgenda(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB response = RestAgenda.crearTurno(contexto);
		if (response.hayError()) {
			RespuestaMB error = RespuestaMB.error();
			error.set("message", response.string("mensajeAlUsuario", "Ha ocurrido un Error"));
			error.set("codigo", response.string("codigo"));
			return error;
		}
		respuesta.set("codigo", response.get("codigo"));
		respuesta.set("id", response.get("id"));
		return respuesta;
	}

	public static RespuestaMB getAgenda(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTurnosOnline)) {
			try {
				ApiResponseMB response = RestAgenda.obtenerTurnos(contexto);
				if (response.codigo == 204) {
					respuesta.set("code", response.codigo);
					respuesta.set("message", "No hay turnos disponibles para esta sucursal, por favor seleccioná otra.");
				} else if (response.hayError()) {
					respuesta = obtenerRespuestaError(response);
					respuesta.set("success", false);
				} else {
					List<Objeto> agendas = new ArrayList<>();
					List<Objeto> disponibilidades = new ArrayList<>();
					Objeto agenda = null;
					boolean iniciarDeNuevo = true;
					String ultimaFecha = response.objetos().size() > 0 ? response.objetos().get(0).string("fecha") : "";
					for (Objeto element : response.objetos()) {
						if (iniciarDeNuevo) {
							if (agenda != null) {
								agendas.add(agenda);
							}
							agenda = new Objeto();
							disponibilidades = new ArrayList<>();
							agenda.set("sucursal", element.get("sucursal"));
							agenda.set("fecha", element.get("fecha"));
							agenda.set("disponibilidad", disponibilidades);
						}
						Objeto disponibilidad = new Objeto();
						disponibilidad.set("id", element.get("id"));
						disponibilidad.set("hora", element.get("hora"));
						disponibilidad.set("cantidad", element.get("cantidad"));
						disponibilidad.set("capacidad", element.get("capacidad"));
						disponibilidades.add(disponibilidad);
						iniciarDeNuevo = !(element.string("fecha").equals(ultimaFecha));
						ultimaFecha = element.string("fecha");
					}
					respuesta.set("turno", agendas);
					respuesta.set("success", true);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			respuesta.set("error", "Funcionalidad no habilitada");
		}
		return respuesta;
	}

	public static RespuestaMB getSucursales(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTurnosOnline)) {
			try {
				String codProvincia = contexto.parametros.string("cod_provincia");
				List<Objeto> sucursales = new ArrayList<>();
				for (Objeto sucursal : RestCatalogo.sucursalesPorProvincia(contexto, codProvincia).objetos()) {
					Objeto mapeoSucursal = new Objeto();
					mapeoSucursal.set("cod_sucursal", sucursal.get("CodSucursal"));
					mapeoSucursal.set("cod_provincia", sucursal.get("codprovincia"));
					mapeoSucursal.set("cod_tipo_sucursal", sucursal.get("codTipoSucursal"));
					mapeoSucursal.set("cod_zona_cotizacion", 0);
					mapeoSucursal.set("desc_sucursal", sucursal.get("DesSucursal"));
					mapeoSucursal.set("domicilio", sucursal.get("Domicilio"));
					mapeoSucursal.set("horario_atencion", sucursal.get("HorarioAtencion"));
					mapeoSucursal.set("latitud", sucursal.get("Latitud"));
					mapeoSucursal.set("longitud", sucursal.get("Longitud"));
					mapeoSucursal.set("telefono", "0810-222-7777");
					mapeoSucursal.set("cajero", "Sucursal con cajero");
					sucursales.add(mapeoSucursal);
				}
				if (sucursales.isEmpty()) {
					respuesta.set("message", "No hay sucursales para la provincia seleccionada.");
				} else {
					respuesta.set("sucursales", sucursales);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			respuesta.set("error", "Funcionalidad no habilitada");
		}
		return respuesta;
	}

	private static RespuestaMB obtenerRespuestaError(ApiResponseMB response) {
		RespuestaMB respuesta = new RespuestaMB();
		for (String clave : response.claves()) {
			respuesta.set(clave, response.get(clave));
		}
		return respuesta;
	}
}
