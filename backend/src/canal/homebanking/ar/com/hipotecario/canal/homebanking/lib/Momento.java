package ar.com.hipotecario.canal.homebanking.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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

	public static Momento nunca() {
		return new Momento();
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

	public Momento sumarDias(Integer cantidad) {
		if (date != null) {
			return new Momento(date.getTime() + cantidad * 24 * 60 * 60 * 1000L);
		}
		return Momento.nunca();
	}

	public Momento restarDias(Integer cantidad) {
		return sumarDias(cantidad * -1);
	}

	/* ========== METODOS (Boolean) ========== */
	public Boolean esAnterior(Momento fecha) {
		if (date != null && fecha.date != null) {
			return date.getTime() < fecha.date.getTime();
		}
		return false;
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

	public static Boolean enHorario(boolean tenerEnCuentaElHorario) {
		if (tenerEnCuentaElHorario) {
			LocalTime localTime = LocalTime.now();
			return (localTime.isAfter(LocalTime.parse("06:00:00")) && localTime.isBefore(LocalTime.parse("21:00:00")));
		}
		return true;
	}

	/**
	 * Método para obtener la diferencia en días de una fecha con respecto a la
	 * fecha actual
	 * 
	 * @param fecha La fecha a comparar
	 * @return diferencia en días
	 */
	public static Integer diferenciaEntreFechas(String fecha) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate fechaFormateada = LocalDate.parse(fecha, formatter);
		Integer dias = Math.toIntExact(ChronoUnit.DAYS.between(convertirDateALocalDate(new Date()), fechaFormateada));
		return dias;
	}

	/**
	 * Método para convertir una fecha Date a LocalDate
	 * 
	 * @param fecha La fecha a convertir
	 * @return fecha LocalDate
	 */
	public static LocalDate convertirDateALocalDate(Date fecha) {
		Instant instant = fecha.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate;
	}
}
