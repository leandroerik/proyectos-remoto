package ar.com.hipotecario.backend.conector.api;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;

public class ApiResponse extends Objeto {

	/* ========== ATRIBUTOS ========== */
	public ApiRequest request;
	public Integer codigoHttp;
	public Map<String, String> headers;
	public String body;

	/* ========== CONSTRUCTORES ========== */
	public ApiResponse(ApiRequest request, HttpResponse httpResponse) {
		this.request = request;
		this.codigoHttp = httpResponse.code;
		this.headers = httpResponse.headers;
		this.body = httpResponse.body;
		this.loadJson(httpResponse.body);
	}

	/* ========== METODOS ========== */
	public Boolean http(Integer... codigos) {
		return new HashSet<Integer>(Arrays.asList(codigos)).contains(codigoHttp);
	}

	public Boolean hayError() {
		return codigoHttp < 200 || codigoHttp > 300;
	}

	public Boolean codigo(String codigo) {
		return string("codigo").equals(codigo);
	}

	public Boolean contains(String texto) {
		return toString().contains(texto);
	}

	public Boolean equals(String clave, String valor) {
		return string(clave).equals(valor);
	}

	/* ========== CREAR ========== */
	public <T extends ApiObjeto> T crear(Class<T> clase) {
		return crear(clase, this);
	}

	@SuppressWarnings("unchecked")
	public <T extends ApiObjeto> T crear(Class<T> clase, Objeto datos) {
		try {
			T objetoApi = null;
			if (datos.isMap()) {
				try {
					objetoApi = datos.toClass(clase);
				} catch (Exception e) {
					objetoApi = clase.getDeclaredConstructor().newInstance();
				}
			} else {
				objetoApi = clase.getDeclaredConstructor().newInstance();
				Class<T> subclase = (Class<T>) ((ParameterizedType) clase.getGenericSuperclass()).getActualTypeArguments()[0];
				for (Objeto item : objetos()) {
					ApiObjetos<T> objetosApi = (ApiObjetos<T>) objetoApi;
					objetosApi.add(item.toClass(subclase));
				}
			}
			objetoApi.codigoHttp = codigoHttp;
			objetoApi.idProceso = request.idProceso();
			return objetoApi;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends ApiObjeto> T crear(Class<T> clase, List<Objeto> datos) {
		try {
			T objetoApi = clase.getDeclaredConstructor().newInstance();
			Class<T> subclase = (Class<T>) ((ParameterizedType) clase.getGenericSuperclass()).getActualTypeArguments()[0];
			for (Objeto item : datos) {
				ApiObjetos<T> objetosApi = (ApiObjetos<T>) objetoApi;
				objetosApi.add(item.toClass(subclase));
			}
			objetoApi.codigoHttp = codigoHttp;
			objetoApi.idProceso = request.idProceso();
			return objetoApi;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends ApiObjeto> T crear(Class<T> clase, List<Objeto> datos, Function<Objeto, Boolean> condicion) {
		try {
			T objetoApi = clase.getDeclaredConstructor().newInstance();
			Class<T> subclase = (Class<T>) ((ParameterizedType) clase.getGenericSuperclass()).getActualTypeArguments()[0];
			for (Objeto item : datos) {
				if (condicion.apply(item)) {
					ApiObjetos<T> objetosApi = (ApiObjetos<T>) objetoApi;
					objetosApi.add(item.toClass(subclase));
				}
			}
			objetoApi.codigoHttp = codigoHttp;
			objetoApi.idProceso = request.idProceso();
			return objetoApi;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
