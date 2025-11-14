package ar.com.hipotecario.backend.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** @author Gabriel Suarez */
public class Futuros<K, V> {

	/* ========== ATRIBUTOS ========== */
	private Map<K, Futuro<V>> futuros = new LinkedHashMap<>();

	/* ========== CONSTRUCTOR ========== */
	public Futuros(Iterable<K> collection, Function<K, V> funcion) {
		for (K item : collection) {
			futuros.put(item, new Futuro<>(() -> funcion.apply(item)));
		}
	}

	/* ========== GET ========== */
	public V get(K clave) {
		Futuro<V> futuro = futuros.get(clave);
		if (futuro != null) {
			return futuro.get();
		}
		return null;
	}

	/* ========== TRY-GET ========== */
	public V tryGet(K clave) {
		Futuro<V> futuro = futuros.get(clave);
		if (futuro != null) {
			return futuro.tryGet();
		}
		return null;
	}
}
