package ar.com.hipotecario.canal.officebanking.jpa;

import java.util.List;

public interface RepositorioGenerico<T> {

	T create(T t);

	void delete(T t);

	T find(T t);

	T update(T t);

	Iterable<T> findAll();

	T findByFieldUnique(String field, Object value);

	List<T> findByField(String field, Object value);

}