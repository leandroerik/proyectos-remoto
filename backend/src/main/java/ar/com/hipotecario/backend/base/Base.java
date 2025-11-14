package ar.com.hipotecario.backend.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** @author Gabriel Suarez */
public class Base {

	/* ========== RANDOM ========== */
	protected static Integer random(Integer minimo, Integer maximo) {
		return Util.random(minimo, maximo);
	}

	@SafeVarargs
	protected static <T> T random(T... valores) {
		return Util.random(valores);
	}

	/* ========== STRING ========== */
	protected static String format(String texto, Object... parametros) {
		return Util.format(texto, parametros);
	}

	protected static StringBuilder append(StringBuilder stringBuilder, String texto, Object... parametros) {
		return Util.append(stringBuilder, texto, parametros);
	}

	/* ========== FUTUROS ========== */
	protected static <T> Futuro<T> futuro(Callable<T> funcion) {
		return Util.futuro(funcion);
	}

	protected static <K, V> Futuros<K, V> futuros(Collection<K> collection, Function<K, V> funcion) {
		return Util.futuros(collection, funcion);
	}

	/* ========== COLLECTIONS ========== */
	@SuppressWarnings("unchecked")
	protected static <T extends Object> List<T> list(T... valores) {
		return Util.list(valores);
	}

	@SuppressWarnings("unchecked")
	protected static <T extends Object> Set<T> set(T... valores) {
		return Util.set(valores);
	}

	@SuppressWarnings("unchecked")
	public static <T> T firstNonNull(T... objetos) {
		return Util.firstNonNull(objetos);
	}

	@SuppressWarnings("unchecked")
	public static <T> T firstNonEmpty(T... objetos) {
		return Util.firstNonEmpty(objetos);
	}

	public static <T> Boolean addIf(Boolean condicion, Collection<T> collection, T item) {
		if (condicion) {
			collection.add(item);
			return true;
		}
		return false;
	}

	/* ========== EMPTY ========== */
	protected static Boolean empty(Object... objetos) {
		return Util.empty(objetos);
	}

	protected static Boolean anyEmpty(Object... objetos) {
		return Util.anyEmpty(objetos);
	}

	protected static Boolean allEmpty(Object... objetos) {
		return Util.allEmpty(objetos);
	}

	/* ========== JSON ========== */
	protected Gson gson() {
		return Util.gson(true);
	}

	protected static GsonBuilder gsonBuilder() {
		return Util.gsonBuilder();
	}

	protected String toJson() {
		return Util.toJson(this);
	}

	protected static <T> T fromJson(String json, Class<T> clase) {
		return Util.fromJson(json, clase);
	}

	/* ========== TOSTRING ========== */
	public String toString() {
		return Util.toJson(this);
	}

	/* ========== EXCEPTION ========== */
	protected static Throwable getCause(Throwable t) {
		return Util.getCause(t);
	}

	protected static StackTraceElement stackTraceElement(Throwable t) {
		return Util.stackTraceElement(t);
	}

	/* ========== POOL ========== */
	protected static Boolean esperar(ExecutorService executorService) {
		return esperar(executorService, 60);
	}

	protected static Boolean esperar(ExecutorService executorService, Integer timeout) {
		try {
			executorService.shutdown();
			if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
			return true;
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			return false;
		}
	}

	/* ========== UTIL ========== */
	protected static Integer min(Integer... numeros) {
		Integer max = null;
		for (Integer numero : numeros) {
			if (max == null || numero < max) {
				max = numero;
			}
		}
		return max;
	}

	protected static Integer max(Integer... numeros) {
		Integer max = null;
		for (Integer numero : numeros) {
			if (max == null || numero > max) {
				max = numero;
			}
		}
		return max;
	}
}
