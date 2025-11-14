package ar.com.hipotecario.canal.homebanking.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.RestDelivery;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;

public class HBDelivery {
	public static Respuesta crearCasoReimpresionTC(ContextoHB contexto) {
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");

		Respuesta respuesta = new Respuesta();
		ApiResponse pieza = RestDelivery.getPieza(contexto, numeroTarjeta);

		if (pieza.hayError()) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", convertApiDeliveryErrorMessage(pieza.string("Mensaje")));
		} else {
			ApiResponse response = RestPostventa.crearCasoReimpresionTC(contexto, "143", numeroTarjeta, pieza.string("IdPieza"));
			if (response.hayError()) {
				return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", response.get("Errores"));
			}
			respuesta.set("numeroCaso", Util.getNumeroCaso(response));
		}
		return respuesta;
	}

	public static Respuesta crearCasoRescateTcEnSucursal(ContextoHB contexto) {
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		String idSucursal = contexto.parametros.string("idSucursal");

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numeroTarjeta);
		if (tarjetaCredito == null) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", "Numero de tarjeta no encontrado en el Contexto");
		}

		ApiResponse pieza = RestDelivery.getPieza(contexto, numeroTarjeta);
		if (pieza.hayError()) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", convertApiDeliveryErrorMessage(pieza.string("Mensaje")));
		}
		Respuesta respuesta = new Respuesta();
		ApiResponse response = RestPostventa.crearCasoRescateTcEnSucursal(contexto, tarjetaCredito, idSucursal, pieza.string("IdPieza"));
		if (response.hayError()) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", response.get("Errores"));
		}
		respuesta.set("numeroCasoCreado", Util.getNumeroCaso(response));
		return respuesta;
	}

	public static Respuesta operacionTarjetaInhibida(ContextoHB contexto) {
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_gestion_plasticos")) {
			Respuesta respuesta = new Respuesta();
			String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
			respuesta.set("mostrarModal", validarEstadoTipificacion(contexto, tipificaciones()) ? true : validarEstadoCuentaTarjeta(contexto, numeroTarjeta));
			return respuesta;
		} else {
			return Respuesta.error();
		}
	}

	public static Respuesta obtenerTarjetaEstado(ContextoHB contexto) {
		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");

		Respuesta response = new Respuesta();
		ApiResponse pieza = RestDelivery.getPieza(contexto, numeroTarjeta);

		if (pieza.hayError()) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", convertApiDeliveryErrorMessage(pieza.string("Mensaje")));
		} else {
			Objeto estadoTarjeta = SqlHomebanking.findEstadoTarjetaCRM(pieza.integer("Estado"));
			if (estadoTarjeta == null) {
				return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", "Estado de tarjeta no encontrado en parametría");
			}
			response.set("gestionEnvio", splitEstado(estadoTarjeta.string("gestion_envio")));
			response.set("recepcionTarjeta", splitEstado(estadoTarjeta.string("recepcion_tarjeta")));
			response.set("numeroEnmascarado", numeroEnmascaradoTarjeta(contexto, tipoTarjeta, numeroTarjeta));
		}
		return response;
	}

	public static Respuesta obtenerDireccionReenvio(ContextoHB contexto) {

		String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
		Respuesta response = new Respuesta();

		ApiResponse pieza = RestDelivery.getPieza(contexto, numeroTarjeta);
		if (pieza.hayError()) {
			return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", convertApiDeliveryErrorMessage(pieza.string("Mensaje")));
		} else {
			ApiResponse direccionReenvio = RestDelivery.getDireccionReenvio(contexto, pieza.string("IdPieza"));
			if (direccionReenvio == null) {
				return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje", convertApiDeliveryErrorMessage(pieza.string("Mensaje")));
			}
			response.set("direccionReenvio", direccionReenvio.string("DomicilioDeliv"));
		}
		return response;
	}

	private static List<String> splitEstado(String estado) {
		List<String> estados = new ArrayList<>();
		if (!StringUtils.isEmpty(estado)) {
			estados = Arrays.asList(estado.split(","));
		}
		return estados;
	}

	private static String numeroEnmascaradoTarjeta(ContextoHB contexto, String tipoTarjeta, String numeroTarjeta) {
		if (tipoTarjeta.equals("TC")) {
			return contexto.tarjetaCredito(numeroTarjeta).numeroEnmascarado();
		} else if (tipoTarjeta.equals("TD")) {
			return contexto.tarjetaDebito(numeroTarjeta).numeroEnmascarado();
		}
		return null;
	}

	private static Boolean validarEstadoTipificacion(ContextoHB contexto, Map<String, String> tipificaciones) {
		ApiResponse responseObtenerCaso = RestPostventa.obtenerCaso(contexto, contexto.persona().cuit());
		Boolean validacionCaso = false;
		if (!responseObtenerCaso.objetos().isEmpty()) {
			for (Objeto item : responseObtenerCaso.objetos("Datos")) {
				String estadoResponse = item.string("Titulo");
				if ("Activo".equals(item.string("Estado")) && tipificaciones.containsValue(estadoResponse)) {
					validacionCaso = true;
					break;
				}
			}
		}

		return validacionCaso;
	}

	private static Boolean validarEstadoCuentaTarjeta(ContextoHB contexto, String numeroTarjeta) {
		ApiResponse responseConsultaTarjetaCredito = TarjetaCreditoService.consultaTarjetaCredito(contexto, numeroTarjeta);
		Boolean validacionEstado = false;
		if (!responseConsultaTarjetaCredito.objetos().isEmpty()) {
			String estadoCuenta = responseConsultaTarjetaCredito.objetos().get(0).string("cuentaEstado");
			String estadoTarjeta = responseConsultaTarjetaCredito.objetos().get(0).string("tarjetaEstado");

			if (!(("10".equals(estadoCuenta)) && Arrays.asList(new String[] { "20", "22" }).contains(estadoTarjeta))) {
				validacionEstado = true;
			}
		}
		return validacionEstado;
	}

	// todo: Pasarlo a una DB
	private static Map<String, String> tipificaciones() {
		Map<String, String> tipificaciones = new HashMap<>();
		tipificaciones.put("15", "REENVIO DE TC");
		tipificaciones.put("143", "REIMPRESION DE PLASTICO TC");
		tipificaciones.put("12", "RESCATE DE TARJETA DE CREDITO");
		tipificaciones.put("Por definir", "EMISION TD"); // todo: Por definir.
		tipificaciones.put("7", "REIMPRESION DE PLASTICO TD");
		tipificaciones.put("16", "REENVIO DE TD");
		tipificaciones.put("13", "RESCATE DE TARJETA DE DEBITO");
		return tipificaciones;
	}

	private static String convertApiDeliveryErrorMessage(String originalMessage) {
		String convertedMessage;
		if (originalMessage.contains("Bad user")) {
			return "El user/password de api-delivery no son válidos";
		}
		switch (originalMessage) {
		case "DeliveryApiNotFoundException":
			convertedMessage = "Número de tarjeta/pieza no encontrado";
			break;
		case "AuthenticationException":
			convertedMessage = "El user/password de api-delivery estan vacios";
			break;
		default:
			convertedMessage = originalMessage;
		}
		return convertedMessage;
	}
}