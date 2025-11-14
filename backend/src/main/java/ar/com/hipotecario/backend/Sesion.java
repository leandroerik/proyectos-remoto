package ar.com.hipotecario.backend;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;

import ar.com.hipotecario.backend.base.Fecha;
import ar.gabrielsuarez.glib.G;

public class Sesion implements Serializable {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public transient Contexto contexto;
	public String cuil;
	public String idCobis;
	public String numeroDocumento;
	public String usuario;
	public String sucursal;
	public Fecha fechaLogin = Fecha.nunca();
	public Fecha fechaUltimaActividad = Fecha.ahora();
	public String idPrisma;

	/* ========== ATRIBUTOS HB ========== */
	public String idSesion;
	public String ip;

	/* ========== ATRIBUTOS PRIVADOS ========== */
	private Integer hashCode;

	/* ========== CONSTRUCTORES ========== */
	public Sesion() {
	}

	/* ========== PERSISTENCIA ========== */
	public void save() {
		contexto.saveSesion(this);
	}

	public void delete() {
		contexto.deleteSesion(this);
	}

	/* ========== METODOS ========== */
	public void actualizarFechaUltimaActividad() {
		fechaUltimaActividad = Fecha.ahora();
		save();
	}

	public void calcularHashCode() {
		hashCode = hashCode(this);
	}

	public Boolean sesionModificada() {
		return !hashCode(this).equals(hashCode);
	}

	// hashcode
	public static Integer hashCode(Object object) {
		try {
			if (object != null) {
				Field[] fields = fields(object.getClass());
				Object[] values = new Object[fields.length];
				for (int i = 0; i < fields.length; ++i) {
					values[i] = fields[i].get(object);
				}
				return Objects.hash(values);
			}
			return 0;
		} catch (Exception e) {
			throw G.runtimeException(e);
		}
	}

	public static Field[] fields(Class<?> type) {
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
		}
		return fields;
	}
}
