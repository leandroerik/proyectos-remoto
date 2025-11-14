package ar.com.hipotecario.backend.conector.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ApiObjetos<T> extends ApiObjeto implements Iterable<T> {

	/* ========== ATRIBUTOS ========== */
	private List<T> lista = new ArrayList<>();

	/* ========== ITERABLE ========== */
	public Iterator<T> iterator() {
		return lista.iterator();
	}

	/* ========== METODOS ========== */
	public T get(int index) {
		try {
			return lista.get(index);
		} catch (Exception e) {
			return null;
		}
	}

	public List<T> list() {
		return lista;
	}

	public Boolean isEmpty() {
		return lista.isEmpty();
	}

	public Integer size() {
		return lista.size();
	}

	public void add(T item) {
		lista.add(item);
	}

	public void remove(Integer index) {
		lista.remove(lista.get(index));
	}

	public void addAll(Collection<T> item) {
		lista.addAll(item);
	}

	/* ========== STREAM ========== */
	public Stream<T> stream() {
		return lista.stream();
	}

	public Stream<T> filter(Predicate<? super T> predicate) {
		return lista.stream().filter(predicate);
	}

	public <X extends ApiObjetos<T>> X filter(Class<X> clase, Predicate<? super T> predicate) {
		return filter(predicate, clase);
	}

	public <X extends ApiObjetos<T>> X filter(Predicate<? super T> predicate, Class<X> clase) {
		try {
			Stream<T> items = lista.stream().filter(predicate);
			X item = clase.getDeclaredConstructor().newInstance();
			Iterator<T> i = items.iterator();
			while (i.hasNext()) {
				item.add(i.next());
			}
			return item;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public T first(Predicate<? super T> predicate) {
		Optional<T> value = lista.stream().filter(predicate).findFirst();
		return value.isPresent() ? value.get() : null;
	}

	public T first(Predicate<? super T> predicate, Comparator<? super T> comparator) {
		Optional<T> value = lista.stream().filter(predicate).sorted(comparator).findFirst();
		return value.isPresent() ? value.get() : null;
	}

	/* ========== TOSTRING ========== */
	public String toString() {
		return gson.toJson(lista);
	}
}
