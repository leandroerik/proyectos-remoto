package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.servicio.RestMora;

public class MBMora {
	public static final String[] TIPO_PROMESA = { "VPAG", "YPAG", "SCON" };
	public static final String FORMATO_FECHA = "yyyy-MM-dd";
	public static final String[] MONEDAS = { "$", "U$S", "UVAs" };
	public static final String[] TIPO_TELEFONO = { "E", "L", "P", "CL" };

	public static RespuestaMB generarPromesaPago(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
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
			return RespuestaMB.estado("TIPO_PROMESA_REQUERIDO");
		}
		if (Arrays.stream(TIPO_PROMESA).noneMatch(t -> t.equals(tipo))) {
			return RespuestaMB.estado("TIPO_PROMESA_INVALIDO");
		}

		if ("SCON".equals(tipo)) {
			if (!esAreaTelfValido(areaTelefono)) {
				return RespuestaMB.estado("NUMERO_AREA_TLF_INVALIDO");
			}
			if (horarioContacto.isEmpty()) {
				return RespuestaMB.estado("HORARIO_REQUERIDO");
			}
			if (telefono.isEmpty()) {
				return RespuestaMB.estado("NUMERO_TELEFONO_REQUERIDO");
			}
			if (tipoTelefono.isEmpty()) {
				return RespuestaMB.estado("TIPO_TELEFONO_REQUERIDO");
			}
			if (Arrays.stream(TIPO_TELEFONO).noneMatch(t -> t.equals(tipoTelefono))) {
				return RespuestaMB.estado("TIPO_TELEFONO_INVALIDO");
			}
		}

		if ("VPAG".equals(tipo) || "YPAG".equals(tipo)) {
			if (fechaPago.isEmpty()) {
				return RespuestaMB.estado("FECHA_PAGO_REQUERIDO");
			}
			if (moneda.isEmpty()) {
				return RespuestaMB.estado("MONEDA_REQUERIDO");
			}
			if (Arrays.stream(MONEDAS).noneMatch(m -> m.equals(moneda))) {
				return RespuestaMB.estado("MONEDA_INVALIDA");
			}
			if (montoPago.isEmpty()) {
				return RespuestaMB.estado("MONTO_REQUERIDO");
			}
			if (nroOperacion.isEmpty()) {
				return RespuestaMB.estado("NUMERO_OPERACION_REQUERIDO");
			}

			try {
				Date fecha = formatoFecha.parse(fechaPago);
				Date fechaActual = Util.dateStringToDate(new Date(), FORMATO_FECHA);
				Calendar fechaMaxima = Calendar.getInstance();
				fechaMaxima.setTime(Util.dateStringToDate(new Date(), FORMATO_FECHA));
				fechaMaxima.add(Calendar.DAY_OF_MONTH, 5);

				if (!"YPAG".equals(tipo)) {
					if (fecha.before(fechaActual) || fecha.after(fechaMaxima.getTime())) {
						return RespuestaMB.estado("FECHA_PAGO_INVALIDA");
					}
				}
			} catch (ParseException e) {
				return RespuestaMB.estado("FORMATO_FECHA_INVALIDO");
			}
		}

		ApiResponseMB generarPromesa = RestMora.autogestionMora(contexto);
		if (generarPromesa.hayError()) {
			return RespuestaMB.error();
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

	public static RespuestaMB productosEnMora(ContextoMB contexto) {
		try {

			RespuestaMB respuesta = new RespuestaMB();
			TarjetaCredito tc = contexto.tarjetaCreditoTitular();
			BigDecimal importePesos = new BigDecimal(0);
			BigDecimal importeUvas = new BigDecimal(0);

			if (tc != null) {
				contexto.parametros.set("idTarjetaCredito", tc.numero());
				RespuestaMB respuestaTC = MBCuenta.estadosCuenta(contexto);
				if (respuestaTC.get("datos.estadoMora") != null) {
					respuesta.set("TarjetaCredito", respuestaTC.get("datos"));
					importePesos = importePesos.add(respuestaTC.bigDecimal("datos.montoMaxPromesa"));
				}
			}

			RespuestaMB respuestaPrestamos = MBPrestamo.consolidadaLite(contexto);
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
			return RespuestaMB.error();
		}

	}

	public static RespuestaMB generarMultiPromesaPago(ContextoMB contexto) {
		try {
			RespuestaMB respuesta = new RespuestaMB();
			Objeto promesas = contexto.parametros.objeto("promesas");
			List<Objeto> resultados = new ArrayList<>();
			for (Objeto promesa : promesas.objetos()) {
				contexto.parametros.set("tipo", promesa.get("tipo"));
				contexto.parametros.set("fechaPago", promesa.get("fechaPago"));
				contexto.parametros.set("moneda", promesa.get("moneda"));
				contexto.parametros.set("montoPago", promesa.get("montoPago"));
				contexto.parametros.set("nroOperacion", promesa.get("nroOperacion"));
				RespuestaMB respuestaProm = generarPromesaPago(contexto);
				resultados.add(new Objeto().set("estado", respuestaProm.get("estado")).set("promesa", promesa));
				contexto.parametros.set("tipo", "").set("fechaPago", "").set("moneda", "").set("montoPago", "").set("nroOperacion", "");
			}
			respuesta.set("promesas", resultados);
			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}
}
