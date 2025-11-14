package ar.com.hipotecario.backend.base;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** @author Gabriel Suarez */
public class Fecha implements Serializable {

	private static final long serialVersionUID = 1L;
	/* ========== ATRIBUTOS ========== */
	private Date date;

	/* ========== CONSTRUCTORES ========== */
	private Fecha() {
	}

	public Fecha(Date date) {
		this.date = date;
	}

	public Fecha(Long milisegundos) {
		if (milisegundos != null) {
			this.date = new Date(milisegundos);
		}
	}

	public Fecha(String fecha, String formato) {
		try {
			this.date = new SimpleDateFormat(formato).parse(fecha);
		} catch (ParseException e) {
			this.date = null;
		}
	}

	/* ========== CONSTRUCTORES ESTATICOS ========== */
	public static Fecha ahora() {
		return new Fecha(new Date());
	}

	public static Fecha hoy() {
		return ahora().removerHora();
	}

	public static Fecha ayer() {
		return hoy().restarDias(1);
	}

	public static Fecha mañana() {
		return hoy().sumarDias(1);
	}

	public static Fecha nunca() {
		return new Fecha();
	}

	public static Fecha maxima(Fecha... fechas) {
		Fecha mayor = Fecha.nunca();
		for (Fecha fecha : fechas) {
			if (mayor.isNull()) {
				mayor = fecha;
			} else if (fecha.esPosterior(mayor)) {
				mayor = fecha;
			}
		}
		return mayor;
	}

	/* ========== METODOS (Fecha) ========== */
	public Fecha removerHora() {
		if (date != null) {
			String formato = "yyyy-MM-dd";
			String fecha = new SimpleDateFormat(formato).format(date);
			return new Fecha(fecha, formato);
		}
		return Fecha.nunca();
	}

	public Fecha sumarMilisegundos(Integer cantidad) {
		if (date != null) {
			return new Fecha(date.getTime() + cantidad * 1L);
		}
		return Fecha.nunca();
	}

	public Fecha sumarSegundos(Integer cantidad) {
		if (date != null) {
			return new Fecha(date.getTime() + cantidad * 1000L);
		}
		return Fecha.nunca();
	}

	public Fecha sumarMinutos(Integer cantidad) {
		if (date != null) {
			return new Fecha(date.getTime() + cantidad * 60 * 1000L);
		}
		return Fecha.nunca();
	}

	public Fecha sumarHoras(Integer cantidad) {
		if (date != null) {
			return new Fecha(date.getTime() + cantidad * 60 * 60 * 1000L);
		}
		return Fecha.nunca();
	}

	public Fecha sumarDias(Integer cantidad) {
		if (date != null) {
			return new Fecha(date.getTime() + cantidad * 24 * 60 * 60 * 1000L);
		}
		return Fecha.nunca();
	}

	public Fecha sumarMeses(Integer cantidad) {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.MONTH, cantidad);
			return new Fecha(calendar.getTime());
		}
		return Fecha.nunca();
	}

	public Fecha sumarAños(Integer cantidad) {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, cantidad);
			return new Fecha(calendar.getTime());
		}
		return Fecha.nunca();
	}

	public Fecha restarMilisegundos(Integer cantidad) {
		return sumarMilisegundos(cantidad * -1);
	}

	public Fecha restarSegundos(Integer cantidad) {
		return sumarSegundos(cantidad * -1);
	}

	public Fecha restarMinutos(Integer cantidad) {
		return sumarMinutos(cantidad * -1);
	}

	public Fecha restarHoras(Integer cantidad) {
		return sumarHoras(cantidad * -1);
	}

	public Fecha restarDias(Integer cantidad) {
		return sumarDias(cantidad * -1);
	}

	public Fecha restarMeses(Integer cantidad) {
		return sumarMeses(cantidad * -1);
	}

	public Fecha restarAños(Integer cantidad) {
		return sumarAños(cantidad * -1);
	}

	/* ========== METODOS (Integer) ========== */
	public Integer diasFaltantes(Fecha fecha) {
		if (date != null && fecha.date != null) {
			Long valor = (fecha.date.getTime() - date.getTime()) / (24 * 60 * 60 * 1000L);
			return valor.intValue();
		}
		return null;
	}

	public Integer diasFaltantes(String fecha, String formato) {
		if (date != null) {
			return diasFaltantes(new Fecha(fecha, formato));
		}
		return null;
	}

	public Integer diasTranscurridos(Fecha fecha) {
		Integer diasFaltantes = diasFaltantes(fecha);
		return diasFaltantes != null ? diasFaltantes * -1 : null;
	}

	public Integer diasTranscurridos(String fecha, String formato) {
		Integer diasFaltantes = diasFaltantes(fecha, formato);
		return diasFaltantes != null ? diasFaltantes * -1 : null;
	}

	public Integer horasFaltantes(Fecha fecha) {
		if (date != null && fecha.date != null) {
			return Long.valueOf((fecha.date.getTime() - date.getTime()) / (60 * 60 * 1000L)).intValue();
		}
		return null;
	}

	public Integer segundosFaltantes(Fecha fecha) {
		if (date != null && fecha.date != null) {
			return Long.valueOf((fecha.date.getTime() - date.getTime()) / 1000L).intValue();
		}
		return null;
	}

	public Integer horasFaltantes(String fecha, String formato) {
		if (date != null) {
			return horasFaltantes(new Fecha(fecha, formato));
		}
		return null;
	}

	public Integer horasTranscurridas(Fecha fecha) {
		Integer horasFaltantes = horasFaltantes(fecha);
		return horasFaltantes != null ? horasFaltantes * -1 : null;
	}

	public Integer horasTranscurridas(String fecha, String formato) {
		Integer horasFaltantes = horasFaltantes(fecha, formato);
		return horasFaltantes != null ? horasFaltantes * -1 : null;
	}

	public Integer segundosTranscurridos(Fecha fecha) {
		Integer segundosFaltantes = segundosFaltantes(fecha);
		return segundosFaltantes != null ? segundosFaltantes * -1 : null;
	}

	public Integer segundo() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("ss").format(date));
		}
		return null;
	}

	public Integer minuto() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("mm").format(date));
		}
		return null;
	}

	public Integer hora() {
		if (date != null) {
			return Integer.valueOf(new SimpleDateFormat("HH").format(date));
		}
		return null;
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
		return esAnterior(Fecha.ahora());
	}

	public Boolean esFuturo() {
		return esPosterior(Fecha.ahora());
	}

	public Boolean esAnterior(Fecha fecha) {
		if (date != null && fecha.date != null) {
			return date.getTime() < fecha.date.getTime();
		}
		return false;
	}

	public Boolean esPosterior(Fecha fecha) {
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

	public Boolean antesDeLas(Integer hora, Integer minuto, Integer segundo) {
		if (!hora.equals(this.hora()))
			return this.hora() < hora;

		if (!minuto.equals(this.minuto()))
			return this.minuto() < minuto;

		return this.segundo() < segundo;
	}

	public Boolean despuesDeLas(Integer hora, Integer minuto, Integer segundo) {
		if (!hora.equals(this.hora()))
			return this.hora() > hora;

		if (!minuto.equals(this.minuto()))
			return this.minuto() > minuto;

		return this.segundo() > segundo;
	}

	public Boolean enIntervalo(Fecha fechaDesde, Fecha fechaHasta) {
		Boolean dentroIntervalo = this.despuesDeLas(fechaDesde.hora(), fechaDesde.minuto(), fechaDesde.segundo());
		dentroIntervalo &= this.antesDeLas(fechaHasta.hora(), fechaHasta.minuto(), fechaHasta.segundo());
		return dentroIntervalo;
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
		if (object instanceof Fecha) {
			Fecha fecha = (Fecha) object;
			String actual = date != null ? this.toString() : "";
			String esperado = fecha.date != null ? fecha.toString() : "";
			return actual.equals(esperado);
		}
		return false;
	}

	public int hashCode() {
		return date != null ? date.hashCode() : 0;
	}

	public static String horaSegundoActual() {
		return Instant.now().atZone(ZoneId.systemDefault()).toString().substring(11, 19);
	}
}
