package ar.com.hipotecario.canal.homebanking.base;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.gabrielsuarez.glib.G;

public class Objeto implements Serializable {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public Map<String, Object> mapa;
	public List<Object> lista;

	/* ========== UTIL ========== */
	public Objeto absorber(Objeto objeto) {
		this.mapa = objeto.mapa;
		this.lista = objeto.lista;
		return this;
	}

	public Boolean existe(String clave) {
		return get(clave) != null;
	}

	public Boolean esLista() {
		return lista != null;
	}

	public Set<String> claves() {
		if (mapa != null) {
			return mapa.keySet();
		}
		return new HashSet<>();
	}

	/* ========== GET ========== */
	public Object get(String clave) {
		if (mapa == null) {
			return null;
		} else if (mapa.containsKey(clave)) {
			return mapa.get(clave);
		} else {
			Objeto actual = this;
			String[] partes = clave.split("\\.");
			for (int i = 0; i < partes.length - 1; ++i) {
				actual = actual.objeto(partes[i]);
			}
			return actual.mapa != null ? actual.mapa.get(partes[partes.length - 1]) : null;
		}
	}

	public Object get(String clave, Object valorPorDefecto) {
		Object object = get(clave);
		return object != null ? object : valorPorDefecto;
	}

	public Objeto objeto(String clave) {
		return objeto(clave, new Objeto());
	}

	public Objeto objeto(String clave, Objeto valorPorDefecto) {
		Object object = get(clave);
		if (object != null && object instanceof Objeto) {
			return (Objeto) object;
		}
		return valorPorDefecto;
	}

	public List<Objeto> objetos() {
		List<Objeto> respuesta = new ArrayList<>();
		if (mapa != null) {
			respuesta.add(fromMap(mapa));
		}
		if (lista != null) {
			for (Object object : lista) {
				if (object instanceof Objeto) {
					respuesta.add((Objeto) object);
				}
			}
		}
		return respuesta;
	}

	public List<Objeto> objetos(String clave) {
		return objeto(clave).objetos();
	}

	public String string(String clave) {
		return desencriptarBH(string(clave, ""));
	}

	public String string(String clave, String valorPorDefecto) {
		Object object = get(clave);
		String valor = object != null ? object.toString() : valorPorDefecto;
		return desencriptarBH(valor);
	}

	public String stringNotEmpty(String clave, String valorPorDefecto) {
		Object object = get(clave);
		String valor = object != null && !object.toString().isEmpty()  ? object.toString() : valorPorDefecto;
		return desencriptarBH(valor);
	}

	public String desencriptarBH(String valor){
		return valor != null && valor.startsWith(Encriptador.ENC_BH) ? Encriptador.desencriptarPBEBH(valor) : valor;
	}

	public Integer integer(String clave) {
		return integer(clave, null);
	}

	public Integer integer(String clave, Integer valorPorDefecto) {
		String string = string(clave, null);
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public Long longer(String clave) {
		return longer(clave, null);
	}

	public Long longer(String clave, Long valorPorDefecto) {
		String string = string(clave, null);
		try {
			return Long.parseLong(string);
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public Boolean bool(String clave) {
		return bool(clave, false);
	}

	public Boolean bool(String clave, Boolean valorPorDefecto) {
		String string = string(clave, null);
		try {
			if (string != null) {
				return Boolean.parseBoolean(string);
			} else {
				return valorPorDefecto;
			}
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public Date date(String clave) {
		return date(clave, null, (Date) null);
	}

	public Date date(String clave, String formato) {
		return date(clave, formato, (Date) null);
	}

	public Date date(String clave, String formato, Date valorPorDefecto) {
		Object object = get(clave);
		if (object == null) {
			return valorPorDefecto;
		}
		if (object instanceof Date) {
			return (Date) object;
		}
		try {
			return new SimpleDateFormat(formato).parse(object.toString());
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public String date(String clave, String formatoOrigen, String formatoDestino) {
		return date(clave, formatoOrigen, formatoDestino, "");
	}

	public String date(String clave, String formatoOrigen, String formatoDestino, String valorPorDefecto) {
		Date date = date(clave, formatoOrigen, (Date) null);
		if (date != null) {
			try {
				return new SimpleDateFormat(formatoDestino).format(date);
			} catch (Exception e) {
				return valorPorDefecto;
			}
		}
		return valorPorDefecto;
	}

	public BigDecimal bigDecimal(String clave) {
		return bigDecimal(clave, null, null, (BigDecimal) null);
	}

	public BigDecimal bigDecimal(String clave, String valorPorDefecto) {
		BigDecimal bigDecimal = bigDecimal(clave, null, null, (BigDecimal) null);
		if (bigDecimal == null) {
			bigDecimal = new BigDecimal(valorPorDefecto);
		}
		return bigDecimal;
	}

	public BigDecimal bigDecimal(String clave, Character separadorDecimal, Character separadorMiles, BigDecimal valorPorDefecto) {
		String string = string(clave, null);
		if (string != null && !string.isEmpty()) {
			string = separadorMiles != null ? string.replace(separadorMiles.toString(), "") : string;
			string = separadorDecimal != null ? string.replace(separadorDecimal.toString(), ".") : string;
			return new BigDecimal(string);
		}
		return valorPorDefecto;
	}

	/* ========== SET ========== */
	public Objeto set(String clave) {
		Objeto objeto = new Objeto();
		set(clave, objeto);
		return objeto;
	}

	public Objeto set(String clave, Object valor) {
		mapa = mapa == null ? new LinkedHashMap<>() : mapa;
		if (valor != null) {
			mapa.put(clave, valor);
		} else {
			mapa.remove(clave);
		}
		return this;
	}

	public Objeto setNull(String clave) {
		mapa.put(clave, null);
		return this;
	}

	/* ========== ADD ========== */
	public Objeto add(Object valor) {
		return add(valor, true);
	}

	public Objeto add(Object valor, Boolean condicion) {
		lista = lista == null ? new ArrayList<>() : lista;
		if (condicion) {
			lista.add(valor);
		}
		return this;
	}

	public Objeto add(String clave, Object valor) {
		return add(clave, valor, true);
	}

	public Objeto add(String clave, Object valor, Boolean condicion) {
		Objeto objeto = objeto(clave, null);
		if (objeto == null) {
			set(clave).add(valor, condicion);
		} else {
			objeto.add(valor, condicion);
		}
		return this;
	}

	/* ========== ORDENAR ========== */
	public Objeto ordenar(String... campos) {
		if (this.mapa == null && this.lista == null) {
			return this;
		}

		if (this.mapa != null && this.lista == null) {
			Map<String, Object> mapaA = new LinkedHashMap<>();
			for (String campo : campos) {
				if (mapa.containsKey(campo)) {
					mapaA.put(campo, mapa.get(campo));
				}
			}
			Map<String, Object> mapaB = new TreeMap<>();
			for (String clave : mapa.keySet()) {
				if (!mapaA.containsKey(clave)) {
					mapaB.put(clave, mapa.get(clave));
				}
			}
			mapa = new LinkedHashMap<>();
			mapa.putAll(mapaA);
			mapa.putAll(mapaB);
			return this;
		}

		Map<String, Objeto> mapa = new TreeMap<>();
		for (Objeto item : objetos()) {
			String clave = "";
			for (String campo : campos) {
				if (campo.startsWith("_bigdecimal_")) {
					BigDecimal original = item.bigDecimal(campo.substring("_bigdecimal_".length()));
					original = original != null ? original : new BigDecimal("0");
					BigDecimal diferencia = new BigDecimal("10000000000").subtract(original);
					clave += diferencia.toString() + "_";
				} else {
					clave += item.string(campo) + "_";
				}
			}
			clave += UUID.randomUUID();
			mapa.put(clave, item);
		}
		lista = null;
		for (String clave : mapa.keySet()) {
			add(mapa.get(clave));
		}
		return this;
	}

	/* ========== SERIALIZACION ========== */
	public Map<String, Object> toMap() {
		Map<String, Object> resultado = new LinkedHashMap<>();
		if (mapa != null) {
			for (String clave : mapa.keySet()) {
				Object valor = mapa.get(clave);
				if (valor instanceof Objeto) {
					Objeto objeto = (Objeto) valor;
					if (objeto.lista != null) {
						resultado.put(clave, objeto.toList());
					} else if (objeto.mapa != null) {
						resultado.put(clave, objeto.toMap());
					}
				} else {
					resultado.put(clave, valor);
				}
			}
		}
		return resultado;
	}

	@SuppressWarnings("unchecked")
	public static Objeto fromMap(Map<String, Object> mapa) {
		Objeto resultado = new Objeto();
		if (mapa != null) {
			for (String clave : mapa.keySet()) {
				Object valor = mapa.get(clave);
				if (valor != null) {
					if (valor instanceof Map) {
						resultado.set(clave, fromMap((Map<String, Object>) valor));
					} else if (valor instanceof List) {
						resultado.set(clave, fromList((List<Object>) valor));
					} else {
						resultado.set(clave, valor);
					}
				}
			}
		}
		return resultado;
	}

	public List<Object> toList() {
		if (this.lista == null) {
			return new ArrayList<>();
		}
		List<Object> lista = new ArrayList<>();
		for (Object object : this.lista) {
			if (object instanceof Objeto) {
				Objeto objeto = (Objeto) object;
				if (objeto.lista != null) {
					lista.add(objeto.toList());
				} else if (objeto.mapa != null) {
					lista.add(objeto.toMap());
				}
			} else {
				lista.add(object);
			}
		}
		return lista;
	}

	public List<Object> toList(String atributo) {
		return this.objeto(atributo).toList();
	}

	@SuppressWarnings("unchecked")
	public static Objeto fromList(List<Object> lista) {
		Objeto objeto = new Objeto();
		if (lista != null) {
			for (Object valor : lista) {
				if (valor instanceof Map) {
					objeto.add(fromMap((Map<String, Object>) valor));
				} else if (valor instanceof List) {
					objeto.add(fromList((List<Object>) valor));
				} else {
					objeto.add(valor);
				}
			}
		}
		return objeto;
	}

	public String toJson() {
		if (lista != null) {
			return G.toJson(toList());
		} else if (mapa != null) {
			return G.toJson(toMap());
		} else {
			return "{}";
		}
	}

	@SuppressWarnings("unchecked")
	public static Objeto fromJson(String json) {
		Objeto objeto = new Objeto();
		if (json != null && !json.isEmpty()) {
			Object object = G.fromJson(json);
			if (object instanceof Map) {
				objeto = Objeto.fromMap((Map<String, Object>) object);
			} else if (object instanceof List) {
				objeto = Objeto.fromList((List<Object>) object);
			} else if(object instanceof Boolean){
				objeto.set("Valor", object);
			}
			else {
				throw new RuntimeException();
			}
		}
		return objeto;
	}

//	public String toXml(String elementoRaiz) {
//		Map<String, Object> mapa = toMap();
//		mapa.put("#omit-xml-declaration", true);
//		String xml = Xml.toXml(mapa, XmlStringBuilder.Step.FOUR_SPACES);
//		xml = "<" + elementoRaiz + ">" + xml.substring(6, xml.length() - 7) + "</" + elementoRaiz + ">";
//		return xml;
//	}

	public static Objeto fromXml(String xml) {
		Object data = G.fromXml(xml);
		String json = G.toJson(data);
		return fromJson(json);
	}

	/* ========== TOSTRING ========== */
	public String toString() {
		return toJson();
	}

	/* ========== METODOS ESTATICOS ========== */
	@SafeVarargs
	public static <T extends Object> List<T> listOf(T... valores) {
		List<T> list = new ArrayList<>();
		for (int i = 0; i < valores.length; ++i) {
			list.add(valores[i]);
		}
		return list;
	}

	@SafeVarargs
	public static <T extends Object> Set<T> setOf(T... valores) {
		Set<T> set = new HashSet<>();
		for (int i = 0; i < valores.length; ++i) {
			set.add(valores[i]);
		}
		return set;
	}

	public static Boolean empty(Object... objetos) {
		Boolean resultado = false;
		for (Object objeto : objetos) {
			resultado |= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty());
		}
		return resultado;
	}

	public static Boolean anyEmpty(Object... objetos) {
		Boolean resultado = false;
		for (Object objeto : objetos) {
			resultado |= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty());
		}
		return resultado;
	}

	public static Boolean allEmpty(Object... objetos) {
		Boolean resultado = true;
		for (Object objeto : objetos) {
			resultado &= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty());
		}
		return resultado;
	}
}
