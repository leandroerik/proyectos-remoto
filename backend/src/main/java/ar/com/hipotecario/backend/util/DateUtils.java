package ar.com.hipotecario.backend.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	// Declarar el logger estático
	private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

	public static final String FORMATO_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String FORMATO_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String FORMATO_YYMMDDHHMMSS = "yyMMddHHmmss";
	public static final String FORMATO_YYYY_MM_DD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMATO_YYYY_MM_DD_T_HHMMSS_SSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String FORMATO_YYYY_MM_DD_T_HHMMSS_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String FORMATO_YYYY_MM_DDTHHMMSS_SSS = "yyyy-MM-ddTHH:mm:ss.SSS";
	public static final String FORMATO_YYYYMMDD_T_HHMMSS_SSS = "yyyy/MM/dd'T'HH:mm:ss.SSS"; // *
	public static final String FORMATO_YYYYMMDD_HHMMSS_SSS = "yyyy/MM/dd HH:mm:ss.SSS"; // *
	public static final String FORMATO_YYYY_MM_DD_T_HHMMSS = "yyyy-MM-dd'T'HH:mm:ss"; // *
	public static final String HHMMSS = "HH:mm:ss";
	public static final String HHMMSS_SOLO_NUM = "HHmmss";
	public static final String HHMM = "HH:mm";
	public static final String FORMATO_YYYYMMDD = "yyyyMMdd";
	public static final String FORMATO_YYMMDD = "yyMMdd";
	public static final String FORMATO_YYYYMM = "yyyyMM";
	public static final String FORMATO_DDMMYYYY = "dd/MM/yyyy";
	public static final String FORMATO_EEE_MMM_DD = "EEE MMM dd HH:mm:ss z yyyy";
	public static final String DEFAULT_DATE = "1900-01-01 00:00:00.0";
	public static final String FORMATO_HISTORICO_LOG = ".%d{yyyy-MM-dd-HH}.zip";
	public static final String FORMATO_NO_SEPARATOR_DDMMYYYY = "ddMMyyyy";
	public static final String FORMATO_YYYYMMDDHHMM = "yyyyMMddHHmm";
	public static final String FORMATO_DDMMYY = "ddMMyy";
	public static final Date FECHA_DEFAULT = new GregorianCalendar(1900, Calendar.JANUARY, 1, 0, 0, 0).getTime();

	private DateUtils() {
	}

	// Return true if strDate1 with format 'pattern' is minor date than strDate2
	// with format 'pattern'
	public static Boolean isMinorDate(String strDate1, String strDate2, String pattern) throws ParseException {
		SimpleDateFormat formatDate = new SimpleDateFormat(pattern);
		Date date1 = formatDate.parse(strDate1);
		Date date2 = formatDate.parse(strDate2);
		return date1.compareTo(date2) <= 0;
	}

	public static Date esFormatoValido(String format, String time, Locale locale) {

		Date fecha = null;

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);

		try {

			fecha = sdf.parse(time);

		} catch (ParseException e) {
			log.error(e.getMessage());
		}

		return fecha;

	}

	public static Date getDateWithFormat(String format, String time, Locale locale) {

		Date fecha = null;

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);

		try {

			fecha = sdf.parse(time);

		} catch (ParseException e) {
			log.error(e.getMessage());
		}

		return fecha;

	}

	public static String getDateFormatWithLocale(String format, Date fecha, Locale locale) {

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(fecha);

	}

	public static String getFormatoFecha(String format, Date fecha) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(fecha);

	}

	public static boolean esFechaValidaPS(String fecha) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date fechaFormateada = null;

		try {

			fechaFormateada = sdf.parse(fecha);

		} catch (ParseException e) {
			log.error(e.getMessage());
			return false;
		}

		if (!sdf.format(fechaFormateada).equals(fecha)) {
			return false;
		}

		return true;
	}

	public static boolean esFechaValidaAC(String fecha) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Date fechaFormateada = null;

		try {

			fechaFormateada = sdf.parse(fecha);

		} catch (ParseException e) {
			log.error(e.getMessage());
			return false;
		}

		if (!sdf.format(fechaFormateada).equals(fecha)) {
			return false;
		}

		return true;
	}

	public static boolean esHorarioValidoPS(String horario) {

		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		Date horarioFormateado = null;

		try {

			horarioFormateado = sdf.parse(horario);

		} catch (ParseException e) {
			log.error(e.getMessage());
			return false;
		}

		if (!sdf.format(horarioFormateado).equals(horario)) {
			return false;
		}

		return true;
	}

	public static String getDiaAnteriorFormat() {

		Calendar cl = Calendar.getInstance();

		cl.setTime(new Date());
		cl.add(Calendar.DATE, -1);
		Date diaAnterior = cl.getTime();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		return sdf.format(diaAnterior);
	}

	public static Date getDateAnterior(int dias) {

		Calendar cl = Calendar.getInstance();

		cl.setTime(new Date());
		cl.add(Calendar.DATE, -dias);
		Date diaAnterior = cl.getTime();

		return diaAnterior;
	}

	public static Date getDiaAnterior() {

		Calendar cl = Calendar.getInstance();

		cl.setTime(new Date());
		cl.add(Calendar.DATE, -1);

		cl.set(Calendar.HOUR_OF_DAY, 0);
		cl.set(Calendar.MINUTE, 0);
		cl.set(Calendar.SECOND, 0);
		cl.set(Calendar.MILLISECOND, 0);

		Date diaAnterior = cl.getTime();

		return diaAnterior;
	}

	public static Date getFechaInicioDia(Date fecha) {

		if (fecha == null)
			return null;

		Calendar fechaInicial = Calendar.getInstance();
		fechaInicial.setTime(fecha);

		fechaInicial.set(Calendar.HOUR_OF_DAY, 0);
		fechaInicial.set(Calendar.MINUTE, 0);
		fechaInicial.set(Calendar.SECOND, 0);
		fechaInicial.set(Calendar.MILLISECOND, 0);

		return fechaInicial.getTime();

	}

	public static Date getFechaFinDia(Date fecha) {

		if (fecha == null)
			return null;

		Calendar fechaFin = Calendar.getInstance();
		fechaFin.setTime(fecha);

		fechaFin.set(Calendar.HOUR_OF_DAY, 23);
		fechaFin.set(Calendar.MINUTE, 59);
		fechaFin.set(Calendar.SECOND, 59);
		fechaFin.set(Calendar.MILLISECOND, 0);

		return fechaFin.getTime();

	}

	public static long getDiferenciaFechas(Date fecha1, Date fecha2) {
		long diff = (Math.max(fecha1.getTime(), fecha2.getTime()) - Math.min(fecha2.getTime(), fecha1.getTime())) / (1000 * 60 * 60 * 24);
		return diff;
	}

	public static Date restarMeses(Date fecha, int cantMesRest) {
		Calendar c = Calendar.getInstance();
		c.setTime(fecha);
		c.add(Calendar.MONTH, -cantMesRest);
		return c.getTime();
	}

	public static Date sumarMeses(Date fecha, int cantMes) {
		return restarMeses(fecha, -cantMes);
	}

	public static Date restarDias(Date fecha, int cantDias) {
		Calendar c = Calendar.getInstance();
		c.setTime(fecha);
		c.add(Calendar.DATE, -cantDias);
		return c.getTime();
	}

	public static Date sumarDias(Date fecha, Integer cantDias) {
		return restarDias(fecha, -cantDias);
	}

	public static long diferenciaEnMeses(Date fecha1, Date fecha2) {

		Calendar dateOne = Calendar.getInstance();
		dateOne.setTime(fecha1);

		Calendar dateSecond = Calendar.getInstance();
		dateSecond.setTime(fecha2);

		// convert date value into millisecond
		long t1 = dateOne.getTimeInMillis();
		long t2 = dateSecond.getTimeInMillis();

		// Calculate difference in millisecond
		long diffInMilliSecond = t2 - t1;

		// Convert milliseconds into minutes
		long minutes = (diffInMilliSecond / (1000 * 60));

		// Convert minutes value into hours
		long hours = minutes / 60;

		// convert hours into days
		long day = hours / 24;

		// convert days into months
		long month = day / 30;

		return Math.abs(month);
	}

	public static boolean isValidYYYYMMDD(String text) {

		if (text == null || !text.matches("\\d{4}-[01]\\d-[0-3]\\d")) {
			return false;
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		df.setLenient(false);

		try {

			df.parse(text);

			return true;

		} catch (ParseException ex) {
			return false;
		}
	}

	public static String dateToFormat(Date fecha) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		cal.add(Calendar.DATE, 0);
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

		String formateado = formato.format(cal.getTime());

		return formateado;
	}

	public static boolean isSameDay(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}

	public static boolean esFechaMenorAlDia(Date fecha) {

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		Date today = new Date();

		Date todayWithZeroTime;
		try {
			todayWithZeroTime = formatter.parse(formatter.format(today));
		} catch (ParseException e) {
			return true;
		}

		if (fecha.before(todayWithZeroTime)) {
			return true;
		}

		return false;
	}

	public static boolean esFechaMayorAlDia(Date fecha) {

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		Date today = new Date();

		Date todayWithZeroTime;
		try {
			todayWithZeroTime = formatter.parse(formatter.format(today));
		} catch (ParseException e) {
			return true;
		}

		if (fecha.after(todayWithZeroTime)) {
			return true;
		}

		return false;
	}

	public static boolean esFecha1MayorAFecha2(Date fecha1, Date fecha2) {

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		Date fecha1WithZeroTime;
		Date fecha2WithZeroTime;
		try {
			fecha1WithZeroTime = formatter.parse(formatter.format(fecha1));
			fecha2WithZeroTime = formatter.parse(formatter.format(fecha2));
		} catch (ParseException e) {
			return true;
		}

		if (fecha1WithZeroTime.after(fecha2WithZeroTime)) {
			return true;
		}

		return false;
	}

	public static boolean esFecha1IgualAFecha2(Date fecha1, Date fecha2) {

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		Date fecha1WithZeroTime;
		Date fecha2WithZeroTime;
		try {
			fecha1WithZeroTime = formatter.parse(formatter.format(fecha1));
			fecha2WithZeroTime = formatter.parse(formatter.format(fecha2));
		} catch (ParseException e) {
			return true;
		}

		if (fecha1WithZeroTime.equals(fecha2WithZeroTime)) {
			return true;
		}

		return false;
	}

	public static Integer parsearUnixDateTime(Date fecha) {
		long timeInMilliseconds = fecha.getTime();
		long timeInSeconds = timeInMilliseconds / 1000; // Convertir a segundos
		return Long.valueOf(timeInSeconds).intValue();
	}

}
