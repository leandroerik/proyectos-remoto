package ar.com.hipotecario.mobile.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** @author Gabriel Suarez */
public class Momento {

	/* ========== ATRIBUTOS ========== */
	private Date date;

	/* ========== CONSTRUCTORES ========== */
	private Momento() {
	}

	public Momento(Date date) {
		this.date = date;
	}

	public Momento(Long milisegundos) {
		if (milisegundos != null) {
			this.date = new Date(milisegundos);
		}
	}

	public Momento(String fecha, String formato) {
		try {
			this.date = new SimpleDateFormat(formato).parse(fecha);
		} catch (ParseException e) {
			this.date = null;
		}
	}

	/* ========== CONSTRUCTORES ESTATICOS ========== */
	public static Momento ahora() {
		return new Momento(new Date());
	}

	public static Momento hoy() {
		return ahora().removerHora();
	}

	public static Momento ayer() {
		return hoy().restarDias(1);
	}

	public static Momento mañana() {
		return hoy().sumarDias(1);
	}

	public static Momento nunca() {
		return new Momento();
	}

	public static Momento maxima(Momento... fechas) {
		Momento mayor = Momento.nunca();
		for (Momento fecha : fechas) {
			if (mayor.isNull()) {
				mayor = fecha;
			} else if (fecha.esPosterior(mayor)) {
				mayor = fecha;
			}
		}
		return mayor;
	}

	/* ========== METODOS (Fecha) ========== */
	public Momento removerHora() {
		if (date != null) {
			String formato = "yyyy-MM-dd";
			String fecha = new SimpleDateFormat(formato).format(date);
			return new Momento(fecha, formato);
		}
		return Momento.nunca();
	}

	public Momento sumarMilisegundos(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 1L);
		}
		return Momento.nunca();
	}

	public Momento sumarSegundos(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 1000L);
		}
		return Momento.nunca();
	}

	public Momento sumarMinutos(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 60 * 1000L);
		}
		return Momento.nunca();
	}

	public Momento sumarHoras(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 60 * 60 * 1000L);
		}
		return Momento.nunca();
	}

	public Momento sumarDias(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 24 * 60 * 60 * 1000L);
		}
		return Momento.nunca();
	}

	public Momento sumarMeses(Integer cantidad) {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.MONTH, cantidad);
			return new Momento(calendar.getTime());
		}
		return Momento.nunca();
	}

	public Momento sumarAños(Integer cantidad) {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, cantidad);
			return new Momento(calendar.getTime());
		}
		return Momento.nunca();
	}

	public Momento restarMilisegundos(Integer cantidad) {
		return sumarMilisegundos(cantidad * -1);
	}

	public Momento restarSegundos(Integer cantidad) {
		return sumarSegundos(cantidad * -1);
	}

	public Momento restarMinutos(Integer cantidad) {
		return sumarMinutos(cantidad * -1);
	}

	public Momento restarHoras(Integer cantidad) {
		return sumarHoras(cantidad * -1);
	}

	public Momento restarDias(Integer cantidad) {
		return sumarDias(cantidad * -1);
	}

	public Momento restarMeses(Integer cantidad) {
		return sumarMeses(cantidad * -1);
	}

	public Momento restarAños(Integer cantidad) {
		return sumarAños(cantidad * -1);
	}

	/* ========== METODOS (Integer) ========== */
	public Integer diasFaltantes(Momento fecha) {
		if (date != null && fecha.date != null) {
			return Long.valueOf((fecha.date.getTime() - date.getTime()) / (24 * 60 * 60 * 1000L)).intValue();
		}
		return null;
	}

	public Integer diasFaltantes(String fecha, String formato) {
		if (date != null) {
			return diasFaltantes(new Momento(fecha, formato));
		}
		return null;
	}

	public Integer diasTranscurridos(Momento fecha) {
		Integer diasFaltantes = diasFaltantes(fecha);
		return diasFaltantes != null ? diasFaltantes * -1 : null;
	}

	public Integer diasTranscurridos(String fecha, String formato) {
		Integer diasFaltantes = diasFaltantes(fecha, formato);
		return diasFaltantes != null ? diasFaltantes * -1 : null;
	}

	public Integer horasFaltantes(Momento fecha) {
		if (date != null && fecha.date != null) {
			return Long.valueOf((fecha.date.getTime() - date.getTime()) / (60 * 60 * 1000L)).intValue();
		}
		return null;
	}

	public Integer horasFaltantes(String fecha, String formato) {
		if (date != null) {
			return horasFaltantes(new Momento(fecha, formato));
		}
		return null;
	}

	public Integer horasTranscurridas(Momento fecha) {
		Integer horasFaltantes = horasFaltantes(fecha);
		return horasFaltantes != null ? horasFaltantes * -1 : null;
	}

	public Integer horasTranscurridas(String fecha, String formato) {
		Integer horasFaltantes = horasFaltantes(fecha, formato);
		return horasFaltantes != null ? horasFaltantes * -1 : null;
	}

	public Integer dia() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("dd").format(date));
		}
		return null;
	}

	public Integer mes() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("MM").format(date));
		}
		return null;
	}

	public Integer año() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("yyyy").format(date));
		}
		return null;
	}

	public Integer edad() {
		if (date != null) {
			LocalDate fechaNacimiento = new java.sql.Date(date.getTime()).toLocalDate();
			LocalDate hoy = new java.sql.Date(new Date().getTime()).toLocalDate();
			return Period.between(fechaNacimiento, hoy).getYears();
		}
		return null;
	}

	public Date fechaDate() {
		return date;
	}

	/* ========== METODOS (Boolean) ========== */
	public Boolean isNull() {
		return date == null;
	}

	public Boolean esPasado() {
		return esAnterior(Momento.ahora());
	}

	public Boolean esFuturo() {
		return esPosterior(Momento.ahora());
	}

	public Boolean esAnterior(Momento fecha) {
		if (date != null && fecha.date != null) {
			return date.getTime() < fecha.date.getTime();
		}
		return false;
	}

	public Boolean esPosterior(Momento fecha) {
		if (date != null && fecha.date != null) {
			return date.getTime() > fecha.date.getTime();
		}
		return false;
	}

	public Boolean esSabado() {
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return Integer.valueOf(calendar.get(Calendar.DAY_OF_WEEK)).equals(Calendar.SATURDAY);
		}
		return false;
	}

	public Boolean esDomingo() {
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return Integer.valueOf(calendar.get(Calendar.DAY_OF_WEEK)).equals(Calendar.SUNDAY);
		}
		return false;
	}

	public Boolean esFinDeSemana() {
		return esSabado() || esDomingo();
	}

	public Date FechaDate() { // TODO: preguntar a gabi si puedo hacer esta función, a veces necesito el date
		return date;
	}

	/* ========== SQL ========== */
	public java.sql.Date sql() {
		return new java.sql.Date(date.getTime());
	}

	public java.sql.Timestamp timestamp() {
		return new java.sql.Timestamp(date.getTime());
	}

	/* ========== TOSTRING ========== */
	public String string(String formato) {
		return string(formato, null);
	}

	public String string(String formato, String valorPorDefecto) {
		return date != null ? new SimpleDateFormat(formato).format(date) : valorPorDefecto;
	}

	public String toString() {
		return date != null ? string("yyyy-MM-dd HH:mm:ss").replace(" 00:00:00", "") : null;
	}

	/* ========== EQUALS | HASHCODE ========== */
	public boolean equals(Object object) {
		if (object instanceof Momento) {
			Momento fecha = (Momento) object;
			String actual = date != null ? this.toString() : "";
			String esperado = fecha.date != null ? fecha.toString() : "";
			return actual.equals(esperado);
		}
		return false;
	}

	public int hashCode() {
		return date != null ? date.hashCode() : 0;
	}
}
