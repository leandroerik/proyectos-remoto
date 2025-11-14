package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.RestMora;

public class HBMora {

	public static final String[] TIPO_PROMESA = { "VPAG", "YPAG", "SCON" };

	public static final String FORMATO_FECHA = "yyyy-MM-dd";

	public static final String[] MONEDAS = { "$", "U$S", "UVAs" };

	public static final String[] TIPO_TELEFONO = { "E", "L", "P", "CL" };

	protected static final String[] CANAL_PAGO = { "CAJA", "PFAC", "RAPI", "PNET", "CEXP", "MP", "TRAN", "HB", "TRAA" };

	public static Respuesta generarPromesaPago(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		SimpleDateFormat formatoFecha = new SimpleDateFormat(FORMATO_FECHA);

		String tipo = contexto.parametros.string("tipo");
		String fechaPago = contexto.parametros.string("fechaPago");
		String moneda = contexto.parametros.string("moneda");
		String montoPago = contexto.parametros.string("montoPago");
		String nroOperacion = contexto.parametros.string("nroOperacion");

		// Requeridos para caso SCON
		String areaTelefono = contexto.parametros.string("areaTelefono");
		String horarioContacto = contexto.parametros.string("horarioContacto");
		String telefono = contexto.parametros.string("telefono");
		String tipoTelefono = contexto.parametros.string("tipoTelefono");

		if (tipo.isEmpty()) {
			return Respuesta.estado("TIPO_PROMESA_REQUERIDO");
		}
		if (Arrays.stream(TIPO_PROMESA).noneMatch(t -> t.equals(tipo))) {
			return Respuesta.estado("TIPO_PROMESA_INVALIDO");
		}

		if ("SCON".equals(tipo)) {
			if (!esAreaTelfValido(areaTelefono)) {
				return Respuesta.estado("NUMERO_AREA_TLF_INVALIDO");
			}
			if (horarioContacto.isEmpty()) {
				return Respuesta.estado("HORARIO_REQUERIDO");
			}
			if (telefono.isEmpty()) {
				return Respuesta.estado("NUMERO_TELEFONO_REQUERIDO");
			}
			if (tipoTelefono.isEmpty()) {
				return Respuesta.estado("TIPO_TELEFONO_REQUERIDO");
			}
			if (Arrays.stream(TIPO_TELEFONO).noneMatch(t -> t.equals(tipoTelefono))) {
				return Respuesta.estado("TIPO_TELEFONO_INVALIDO");
			}
		}

		if ("VPAG".equals(tipo) || "YPAG".equals(tipo)) {
			if (fechaPago.isEmpty()) {
				return Respuesta.estado("FECHA_PAGO_REQUERIDO");
			}
			if (moneda.isEmpty()) {
				return Respuesta.estado("MONEDA_REQUERIDO");
			}
			if (Arrays.stream(MONEDAS).noneMatch(m -> m.equals(moneda.trim()))) {
				return Respuesta.estado("MONEDA_INVALIDA");
			}
			if (montoPago.isEmpty()) {
				return Respuesta.estado("MONTO_REQUERIDO");
			}
			if (nroOperacion.isEmpty()) {
				return Respuesta.estado("NUMERO_OPERACION_REQUERIDO");
			}

			try {
				Date fecha = formatoFecha.parse(fechaPago);
				Date fechaActual = Util.dateStringToDate(new Date(), FORMATO_FECHA);
				Calendar fechaMaxima = Calendar.getInstance();
				fechaMaxima.setTime(Util.dateStringToDate(new Date(), FORMATO_FECHA));
				fechaMaxima.add(Calendar.DAY_OF_MONTH, 5);
				if (!"YPAG".equals(tipo)) {
					if (fecha.before(fechaActual) || fecha.after(fechaMaxima.getTime())) {
						return Respuesta.estado("FECHA_PAGO_INVALIDA");
					}
				}
			} catch (ParseException e) {
				return Respuesta.estado("FORMATO_FECHA_INVALIDO");
			}
		}

		ApiResponse generarPromesa = RestMora.autogestionMora(contexto);
		if (generarPromesa.hayError()) {
			return Respuesta.error();
		}
		return respuesta.setEstado("OK");
	}

	private static boolean esAreaTelfValido(String input) {
		if (input.length() <= 5) {
			try {
				Integer.parseInt(input);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	// todo validar horas horario VCON
	protected static boolean esHoraValida(String input) {
		String timePattern = "^(?:[01]\\d|2[0-3]):[0-5]\\d$";
		Pattern pattern = Pattern.compile(timePattern);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	public static Respuesta productosEnMora(ContextoHB contexto) {
		try {

			Respuesta respuesta = new Respuesta();
			TarjetaCredito tc = contexto.tarjetaCreditoTitular();
			BigDecimal importePesos = new BigDecimal(0);
			BigDecimal importeUvas = new BigDecimal(0);

			if (tc != null) {
				contexto.parametros.set("idTarjetaCredito", tc.numero());
				Respuesta respuestaTC = HBCuenta.estadosCuenta(contexto);
				if (respuestaTC.get("datos.estadoMora") != null) {
					respuesta.set("TarjetaCredito", respuestaTC.get("datos"));
					importePesos = importePesos.add(respuestaTC.bigDecimal("datos.montoMaxPromesa"));
				}
			}

			Respuesta respuestaPrestamos = HBPrestamo.consolidada(contexto);
			Objeto prestamos = new Objeto();

			if (!respuestaPrestamos.objetos("hipotecarios").isEmpty()) {
				for (Objeto pp : respuestaPrestamos.objetos("hipotecarios")) {
					if (pp.get("estadoMora") != null) {
						prestamos.add(pp);
						if (pp.get("idMoneda").equals("80")) {
							importePesos = importePesos.add(pp.bigDecimal("montoMaxPromesa"));
						} else {
							importeUvas = importeUvas.add(pp.bigDecimal("montoMaxPromesa"));
						}
					}
				}
			}

			if (!respuestaPrestamos.objetos("personales").isEmpty()) {
				for (Objeto pp : respuestaPrestamos.objetos("personales")) {
					if (pp.get("estadoMora") != null) {
						prestamos.add(pp);
						if (pp.get("idMoneda").equals("80")) {
							importePesos = importePesos.add(pp.bigDecimal("montoMaxPromesa"));
						} else {
							importeUvas = importeUvas.add(pp.bigDecimal("montoMaxPromesa"));
						}
					}
				}
			}

			if (!prestamos.objetos().isEmpty()) {
				respuesta.set("prestamos", prestamos);
			}

			respuesta.set("importeTotalPesos", importePesos);
			respuesta.set("importeTotalPesosFormateado", Formateador.importe(importePesos));
			respuesta.set("importeTotalUvas", importeUvas);
			respuesta.set("importeTotalUvasFormateado", Formateador.importe(importeUvas));
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

}