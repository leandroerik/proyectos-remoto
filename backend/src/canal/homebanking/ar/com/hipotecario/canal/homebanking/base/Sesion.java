package ar.com.hipotecario.canal.homebanking.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;

import ar.gabrielsuarez.glib.G;

public abstract class Sesion implements Serializable {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public String idSesion;
	public String idCobis;
	public String ip;

	/* ========== ATRIBUTOS PRIVADOS ========== */
	private Integer hashCode;

	/* ========== METODOS ========== */
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
