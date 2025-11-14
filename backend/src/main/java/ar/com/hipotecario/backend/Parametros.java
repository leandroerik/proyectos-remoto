package ar.com.hipotecario.backend;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.exception.ParametrosIncorrectosException;

public class Parametros extends Objeto {

	private Map<String, Archivo> archivos = new LinkedHashMap<>();

	/* ========== CONSTRUCTORES ========== */
	public static Parametros fromJson(String json) {
		Parametros parametros = new Parametros();
		try {
			parametros.loadJson(json);
		} catch (Exception e) {
		}
		return parametros;
	}

	public void setArchivo(String clave, Archivo archivo) {
		archivos.put(clave, archivo);
	}

	/* ========== METODOS ========== */
	public Map<String, Archivo> archivos() {
		return archivos;
	}

	public Archivo archivo(String clave) {
		return archivos.get(clave);
	}

	public Archivo imagen(String clave, Integer calidad) {
		return archivos.get(clave).comprimirImagen(calidad);
	}

	public String string(String clave) {
		String valor = super.string(clave, null);
		ParametrosIncorrectosException.throwIf(valor == null, clave, valor);
		return valor;
	}

	public Boolean bool(String clave) {
		Boolean valor = super.bool(clave, null);
		ParametrosIncorrectosException.throwIf(valor == null, clave, valor);
		return valor;
	}

	public Integer integer(String clave) {
		Integer valor = super.integer(clave, null);
		ParametrosIncorrectosException.throwIf(valor == null, clave, valor);
		return valor;
	}

	public Long longer(String clave) {
		Long valor = super.longer(clave, null);
		ParametrosIncorrectosException.throwIf(valor == null, clave, valor);
		return valor;
	}

	public BigDecimal bigDecimal(String clave) {
		BigDecimal valor = super.bigDecimal(clave, null);
		ParametrosIncorrectosException.throwIf(valor == null, clave, valor);
		return valor;
	}

	public Fecha fecha(String clave, String formato) {
		Fecha valor = super.fecha(clave, formato);
		ParametrosIncorrectosException.throwIf(valor.isNull(), clave, valor);
		return valor;
	}

	public String type(String clave, String... valores) {
		String valor = super.string(clave);
		ParametrosIncorrectosException.throwIf(!Util.set(valores).contains(valor), clave, valor);
		return valor;
	}
}
