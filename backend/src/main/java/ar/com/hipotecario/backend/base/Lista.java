package ar.com.hipotecario.backend.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** @author Gabriel Suarez */
public class Lista<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	/* ========== STREAM ========== */
	public Stream<T> filter(Predicate<? super T> predicate) {
		return stream().filter(predicate);
	}

	public T first(Predicate<? super T> predicate) {
		Optional<T> value = stream().filter(predicate).findFirst();
		return value.isPresent() ? value.get() : null;
	}

	public T first(Predicate<? super T> predicate, Comparator<? super T> comparator) {
		Optional<T> value = stream().filter(predicate).sorted(comparator).findFirst();
		return value.isPresent() ? value.get() : null;
	}
}
