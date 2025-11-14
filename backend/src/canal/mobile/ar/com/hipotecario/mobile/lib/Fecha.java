package ar.com.hipotecario.mobile.lib;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class Fecha {

	public static String mes(String numeroMes) {
		String mes = "";
		mes = "01".equals(numeroMes) ? "Enero" : mes;
		mes = "02".equals(numeroMes) ? "Febrero" : mes;
		mes = "03".equals(numeroMes) ? "Marzo" : mes;
		mes = "04".equals(numeroMes) ? "Abril" : mes;
		mes = "05".equals(numeroMes) ? "Mayo" : mes;
		mes = "06".equals(numeroMes) ? "Junio" : mes;
		mes = "07".equals(numeroMes) ? "Julio" : mes;
		mes = "08".equals(numeroMes) ? "Agosto" : mes;
		mes = "09".equals(numeroMes) ? "Septiembre" : mes;
		mes = "10".equals(numeroMes) ? "Octubre" : mes;
		mes = "11".equals(numeroMes) ? "Noviembre" : mes;
		mes = "12".equals(numeroMes) ? "Diciembre" : mes;
		return mes;
	}

	public static Boolean esPasado(Date fecha) {
		return new Date().getTime() - fecha.getTime() > 0;
	}

	public static Boolean esFuturo(Date fecha) {
		try {
			return new Date().getTime() - fecha.getTime() < 0;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer cantidadDias(Date fechaInicio, Date fechaFin) {
//		try { //emm: Se le trunca la hora a la fecha de inicio
//			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//			fechaInicio = formatter.parse(formatter.format(fechaInicio));
//		} catch (ParseException e) {
//		}
		return fechaInicio == null || fechaFin == null ? null : (int) ((fechaFin.getTime() - fechaInicio.getTime()) / (1000 * 60 * 60 * 24));
	}

	public static Integer porcentajeTranscurrido(Date fechaInicio, Date fechaFin) {
		Integer porcentaje = 100 * cantidadDias(fechaInicio, new Date()) / Fecha.cantidadDias(fechaInicio, fechaFin);
		porcentaje = porcentaje < 0 ? 0 : porcentaje;
		porcentaje = porcentaje > 100 ? 100 : porcentaje;
		return porcentaje;
	}

	public static Integer porcentajeTranscurrido(Long cantidadDias, Date fechaFin) {
		try {
			Date fechaInicio = restarDias(fechaFin, cantidadDias);
			return porcentajeTranscurrido(fechaInicio, fechaFin);
		} catch (Exception e) {
			return 0;
		}
	}

	public static Date sumarDias(Date fechaInicio, Long cantidadDias) {
		Long tiempo = fechaInicio.getTime() + (cantidadDias * 24L * 60L * 60L * 1000L);
		Date fechaFutura = new Date();
		fechaFutura.setTime(tiempo);
		return fechaFutura;
	}

	public static Date sumarMeses(Date fechaInicio, Integer cantidadMeses) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(fechaInicio);
		calendar.add(Calendar.MONTH, cantidadMeses);
		Date fecha = new Date(calendar.getTimeInMillis());
		return fecha;
	}

	public static Date restarDias(Date fechaFin, Long cantidadDias) {
		Long tiempo = fechaFin.getTime() - (cantidadDias * 24L * 60L * 60L * 1000L);
		Date fechaFutura = new Date();
		fechaFutura.setTime(tiempo);
		return fechaFutura;
	}

	public static String restarDias(Date fechaFin, Long cantidadDias, String formato) {
		return new SimpleDateFormat(formato).format(restarDias(fechaFin, cantidadDias));
	}

	public static String sumarDias(Date fechaInicio, Long cantidadDias, String formato) {
		return new SimpleDateFormat(formato).format(sumarDias(fechaInicio, cantidadDias));
	}

	public static Date sumarSegundos(Date fechaInicio, Long cantidadSegundos) {
		Long tiempo = fechaInicio.getTime() + (cantidadSegundos * 1000L);
		Date fechaFutura = new Date();
		fechaFutura.setTime(tiempo);
		return fechaFutura;
	}

	public static String formato(String clave, String formatoOrigen, String formatoDestino) {
		Date date;
		try {
			date = new SimpleDateFormat(formatoOrigen).parse(clave);
			if (date != null) {
				return new SimpleDateFormat(formatoDestino).format(date);
			}
		} catch (Exception e1) {
			return clave;
		}
		return clave;
	}

	public static boolean fechaEnRangoFecha(String fecha, String fechaDesde, String fechaHasta, String formato) {
		boolean esValido = false;
		try {
			Date fechaTest = new SimpleDateFormat(formato).parse(fecha);
			Date desde = new SimpleDateFormat(formato).parse(fechaDesde);
			Date hasta = new SimpleDateFormat(formato).parse(fechaHasta);
			if (fechaTest != null && desde != null && hasta != null) {
				esValido = (fechaTest.equals(desde) || fechaTest.after(desde)) && (fechaTest.equals(hasta) || fechaTest.before(hasta));
			}
		} catch (ParseException e) {
			return esValido;
		}

		return esValido;
	}

	public static List<Objeto> ordenarPorFechaDesc(Objeto lista, String campo, String formato) {
		List<Objeto> listaObjetos = lista.objetos();
		ordenarPorFechaDesc(listaObjetos, campo, formato);
		return listaObjetos;
	}

	public static List<Objeto> ordenarPorFechaAsc(Objeto lista, String campo, String formato) {
		List<Objeto> listaObjetos = lista.objetos();
		ordenarPorFechaAsc(listaObjetos, campo, formato);
		return listaObjetos;
	}

	public static void ordenarPorFechaDesc(List<Objeto> lista, String campo, String formato) {
		Collections.sort(lista, Collections.reverseOrder(new Comparator<Objeto>() {
			public int compare(Objeto o1, Objeto o2) {
				Date a = o1.date(campo, formato);
				Date b = o2.date(campo, formato);
				return a.compareTo(b);
			}
		}));
	}

	public static void ordenarPorFechaAsc(List<Objeto> lista, String campo, String formato) {
		Collections.sort(lista, new Comparator<Objeto>() {
			public int compare(Objeto o1, Objeto o2) {
				Date a = o1.date(campo, formato);
				Date b = o2.date(campo, formato);
				return a.compareTo(b);
			}
		});
	}

	public static String diaES(DayOfWeek fechaHoy) {
		switch (fechaHoy) {
		case MONDAY:
			return "LUNES";
		case TUESDAY:
			return "MARTES";
		case WEDNESDAY:
			return "MIERCOLES";
		case THURSDAY:
			return "JUEVES";
		case FRIDAY:
			return "VIERNES";
		case SATURDAY:
			return "SABADO";
		case SUNDAY:
			return "DOMINGO";
		default:
			return "";
		}
	}

	public static String diaCapitalize(DayOfWeek fechaHoy) {
		return StringUtils.capitalize(StringUtils.lowerCase(diaES(fechaHoy)));
	}

	public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static Date stringToDate(String date, String formato) {
		DateFormat format = new SimpleDateFormat(formato);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String fechaCompletaString() {
		return Integer.toString(LocalDateTime.now().getYear()) + Integer.toString(LocalDateTime.now().getMonthValue()) + Integer.toString(LocalDateTime.now().getDayOfMonth()) + Integer.toString(LocalDateTime.now().getHour()) + Integer.toString(LocalDateTime.now().getMinute());
	}

	public static String fechaYHoraActual() {
		// AAAA-MM-DDTHH:MM:SS en tiempo local
		return Instant.now().atZone(ZoneId.systemDefault()).toString().substring(0, 19);
	}

	public static String horaActual() {
		// HH:MM en tiempo local
		return Instant.now().atZone(ZoneId.systemDefault()).toString().substring(11, 16);
	}

	public static String horaSegundoActual() {
		return Instant.now().atZone(ZoneId.systemDefault()).toString().substring(11, 19);
	}

	public static String fechaActual() {
		return fechaYHoraActual().split("T")[0];
	}

	public static boolean esFechaActual(Date fecha) {
		try {
			LocalDate fechaConvertida = fecha.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate();
			return LocalDate.now().isEqual(fechaConvertida);
		} catch (Exception e) {
			return false;
		}
	}

}
