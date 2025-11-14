package ar.com.hipotecario.backend.conector.sql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;

@SuppressWarnings("serial")
public class SqlObjetos<T> extends ArrayList<T> {

	/* ========== METODOS ESTATICOS ========== */
	protected static Object[] parametros(Object... parametros) {
		return parametros;
	}

	/* ========== METODOS ========== */
	public T first() {
		return !isEmpty() ? get(0) : null;
	}

	/* ========== TOSTRING ========== */
	public String toString() {
		return Util.toJson(this);
	}

	/* ========== MAP ========== */
	public static <S extends SqlObjetos<T>, T extends SqlObjeto> S map(Objeto registros, Class<S> claseObjetos,
			Class<T> claseObjeto) {
		try {
			S clase = claseObjetos.getDeclaredConstructor().newInstance();
			Field[] fields = claseObjeto.getDeclaredFields();
			Constructor<T> constructor = claseObjeto.getDeclaredConstructor();
			for (Objeto registro : registros.objetos()) {
				T item = constructor.newInstance();
				for (Field field : fields) {
					Class<?> type = field.getType();
					String name = field.getName();
					if (type == String.class) {
						field.set(item, registro.string(name));
					} else if (type == Integer.class) {
						field.set(item, registro.integer(name));
					} else if (type == Long.class) {
						field.set(item, registro.longer(name));
					} else if (type == BigDecimal.class) {
						field.set(item, registro.bigDecimal(name));
					} else if (type == Fecha.class) {
						field.set(item, new Fecha(registro.longer(name)));
					} else {
						field.set(item, registro.get(name));
					}
				}
				clase.add(item);
			}
			return clase;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <S extends SqlObjetos<T>, T extends SqlObjeto> S mapTas(Objeto registros, Class<S> claseObjetos,
			Class<T> claseObjeto) {
		try {
			S clase = claseObjetos.getDeclaredConstructor().newInstance();
			Field[] fields = claseObjeto.getDeclaredFields();
			Constructor<T> constructor = claseObjeto.getDeclaredConstructor();
			for (Objeto registro : registros.objetos()) {
				T item = constructor.newInstance();
				for (Field field : fields) {
					Class<?> type = field.getType();
					String name = field.getName();
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					field.setAccessible(true);
					if (type == String.class) {
						field.set(item, registro.string(name));
					} else if (type == Integer.class) {
						field.set(item, registro.integer(name));
					} else if (type == Long.class) {
						field.set(item, registro.longer(name));
					} else if (type == BigDecimal.class) {
						field.set(item, registro.bigDecimal(name));
					} else if (type == Fecha.class) {
						field.set(item, new Fecha(registro.longer(name)));
					} else {
						field.set(item, registro.get(name));
					}
				}
				clase.add(item);
			}
			return clase;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* ========== TEST ========== */
	protected static Contexto contexto(String canal, String ambiente) {
		return new Contexto(canal, ambiente, "1");
	}

	protected static Contexto contexto(String canal, String ambiente, String idCobis) {
		return new Contexto(canal, ambiente, idCobis);
	}

	protected static void imprimirResultado(Contexto contexto, SqlObjeto objeto) {
		Long momentoFin = new Date().getTime();
		System.out.println(objeto);
		System.out.println();
		System.out.println("TIEMPO: " + (momentoFin - contexto.momentoCreacion) + " ms");
		System.out.println();
		contexto.momentoCreacion = new Date().getTime();
	}

	protected static void imprimirResultado(Contexto contexto, SqlObjetos<?> objeto) {
		Long momentoFin = new Date().getTime();
		System.out.println(objeto);
		System.out.println();
		System.out.println("TIEMPO: " + (momentoFin - contexto.momentoCreacion) + " ms");
		System.out.println();
		contexto.momentoCreacion = new Date().getTime();
	}

	protected static void imprimirResultado(Contexto contexto, Boolean exito) {
		Long momentoFin = new Date().getTime();
		System.out.println("EXITO: " + exito);
		System.out.println();
		System.out.println("TIEMPO: " + (momentoFin - contexto.momentoCreacion) + " ms");
		System.out.println();
		contexto.momentoCreacion = new Date().getTime();
	}
}
