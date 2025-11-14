package ar.com.hipotecario.backend.base;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.underscore.lodash.U;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Objeto {

	/* ========== ATRIBUTOS ========== */
	private Map<String, Object> mapa = new LinkedHashMap<>();
	private List<Object> lista;

	/* ========== CONSTRUCTORES ========== */
	public Objeto() {
	}

	/* ========== CONSTRUCTORES ESTATICOS ========== */
	public static Objeto fromJson(String json) {
		Objeto objeto = new Objeto();
		objeto.loadJson(json);
		return objeto;
	}

	public static Objeto fromMap(Map<String, Object> mapa) {
		Objeto objeto = new Objeto();
		objeto.loadMap(mapa);
		return objeto;
	}

	public static Objeto fromRawMap(Map<String, Object> mapa) {
		Objeto objeto = new Objeto();
		objeto.mapa = mapa;
		return objeto;
	}

	public static Objeto fromList(List<Object> lista) {
		Objeto objeto = new Objeto();
		objeto.loadList(lista);
		return objeto;
	}

	public static Objeto fromResultSet(ResultSet rs) throws SQLException {
		Objeto objeto = new Objeto();
		objeto.loadResultSet(rs);
		return objeto;
	}

	/* ========== LOAD ========== */
	@SuppressWarnings("unchecked")
	public void loadJson(String json) {
		if (json != null && !json.isEmpty()) {
			Object object = U.fromJson(json);
			if (object instanceof Map) {
				loadMap((Map<String, Object>) object);
			}
			if (object instanceof List) {
				loadList((List<Object>) object);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void loadMap(Map<String, Object> mapa) {
		setMapa();
		if (mapa != null) {
			for (String key : mapa.keySet()) {
				Object value = mapa.get(key);
				value = value instanceof Map ? Objeto.fromMap((Map<String, Object>) value) : value;
				value = value instanceof List ? Objeto.fromList((List<Object>) value) : value;
				this.mapa.put(key, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void loadList(List<Object> lista) {
		setLista();
		if (lista != null) {
			for (Object value : lista) {
				value = value instanceof Map ? Objeto.fromMap((Map<String, Object>) value) : value;
				value = value instanceof List ? Objeto.fromList((List<Object>) value) : value;
				this.lista.add(value);
			}
		}
	}

	public void loadResultSet(ResultSet rs) throws SQLException {
		setLista();
		if (rs != null) {
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				Objeto objeto = new Objeto();
				for (Integer i = 1; i <= rsmd.getColumnCount(); ++i) {
					String clave = rsmd.getColumnName(i);
					Object valor = rs.getObject(i);
					objeto.set(clave, valor);
				}
				add(objeto);
			}
		}
	}

	/* ========== TIPOS ========== */
	private void setMapa() {
		if (this.mapa == null) {
			this.mapa = new LinkedHashMap<>();
			this.lista = null;
		}
	}

	private void setLista() {
		if (this.lista == null) {
			this.mapa = null;
			this.lista = new ArrayList<>();
		}
	}

	/* ========== SERIALIZACION ========== */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		for (String key : mapa.keySet()) {
			Object value = mapa.get(key);
			if (value instanceof Objeto) {
				Objeto objeto = (Objeto) value;
				value = objeto.lista == null ? objeto.toMap() : value;
				value = objeto.lista != null ? objeto.toList() : value;
			}
			map.put(key, value);
		}
		return map;
	}

	public List<Object> toList() {
		List<Object> list = new ArrayList<>();
		for (Object value : lista) {
			if (value instanceof Objeto) {
				Objeto objeto = (Objeto) value;
				value = objeto.lista == null ? objeto.toMap() : value;
				value = objeto.lista != null ? objeto.toList() : value;
			}
			list.add(value);
		}
		return list;
	}

	public String toJson() {
		String json = null;
		json = json == null && lista == null && !mapa.isEmpty() ? U.toJson(toMap()) : json;
		json = json == null && lista != null ? U.toJson(toList()) : json;
		json = json == null ? "{}" : json;
		return json;
	}

	public String toSimpleJson() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = null;
		json = json == null && lista == null && !mapa.isEmpty() ? gson.toJson(toMap()) : json;
		json = json == null && lista != null ? gson.toJson(toList()) : json;
		json = json == null ? "{}" : json;
		return json;
	}

	public <T> T toClass(Class<T> clase) {
		T objeto = Util.fromJson(toJson(), clase);
		return objeto;
	}

	/* ========== ATTRIBUTOS ========== */
	public Set<String> keys() {
		return mapa != null ? mapa.keySet() : new HashSet<>();
	}

	/* ========== GET ========== */
	public Object get(String clave) {
		Objeto destino = this;
		if (clave != null && !clave.isEmpty()) {
			String[] partes = clave.split("\\.");
			for (Integer i = 0; i < partes.length; ++i) {
				String subClave = partes[i];
				if (i + 1 < partes.length) {
					Objeto objeto = new Objeto();
					if (!subClave.matches("[0-9]+")) {
						Object object = destino.mapa.get(subClave);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						} else {
							return mapa.get(subClave);
						}
					} else {
						destino.lista = destino.lista == null ? new ArrayList<>() : destino.lista;
						Integer posicion = Integer.valueOf(subClave);
						while (destino.lista.size() <= posicion) {
							destino.lista.add(new Objeto());
						}
						Object object = destino.lista.get(posicion);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						}
					}
					destino = objeto;
				} else {
					return destino.mapa.get(subClave);
				}
			}
		}
		return destino;
	}

	public Object get(String clave, Object valorPorDefecto) {
		Object object = get(clave);
		return object != null ? object : valorPorDefecto;
	}

	public String string(String clave) {
		return string(clave, "");
	}

	public String string(String clave, String valorPorDefecto) {
		Object object = get(clave, null);
		return object != null ? object.toString() : valorPorDefecto;
	}

	public Boolean bool(String clave) {
		return bool(clave, false);
	}

	public Boolean bool(String clave, Boolean valorPorDefecto) {
		String string = string(clave, null);
		try {
			return string != null ? Boolean.valueOf(string) : valorPorDefecto;
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public Integer integer(String clave) {
		return integer(clave, null);
	}

	public Integer integer(String clave, Integer valorPorDefecto) {
		String string = string(clave, null);
		try {
			return string != null ? Integer.valueOf(string) : valorPorDefecto;
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
			return string != null ? Long.valueOf(string) : valorPorDefecto;
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public BigDecimal bigDecimal(String clave) {
		return bigDecimal(clave, null);
	}

	public BigDecimal bigDecimal(String clave, String valorPorDefecto) {
		try {
			String string = string(clave, null);
			try {
				return string != null ? new BigDecimal(string) : new BigDecimal(valorPorDefecto.toString());
			} catch (Exception e) {
				return new BigDecimal(valorPorDefecto.toString());
			}
		} catch (Exception e) {
			return null;
		}
	}

	public Fecha fecha(String clave, String formato) {
		return fecha(clave, formato, Fecha.nunca());
	}

	public Fecha fecha(String clave, String formato, Fecha valorPorDefecto) {
		String string = string(clave, null);
		try {
			return string != null ? new Fecha(string, formato) : valorPorDefecto;
		} catch (Exception e) {
			return valorPorDefecto;
		}
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

	@SuppressWarnings("unchecked")
	public List<Objeto> objetos() {
		List<Objeto> objetos = new ArrayList<>();
		if (lista != null) {
			for (Object object : lista) {
				if (object instanceof Map) {
					Objeto objeto = Objeto.fromMap((Map<String, Object>) object);
					objetos.add(objeto);
				}
				if (object instanceof Objeto) {
					objetos.add((Objeto) object);
				}
			}
		}
		return objetos;
	}

	public Objeto objetos(Integer i) {
		try {
			return objetos().get(i);
		} catch (Exception e) {
			return new Objeto();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Objeto> objetos(String clave) {
		List<Objeto> objetos = new ArrayList<>();
		Object list = get(clave);
		if (list instanceof Objeto) {
			list = ((Objeto) list).toList();
		}
		if (list instanceof List) {
			for (Object object : (List<Object>) list) {
				if (object instanceof Map) {
					Objeto objeto = Objeto.fromMap((Map<String, Object>) object);
					objetos.add(objeto);
				}
			}
		}
		return objetos;
	}

	public <T> List<T> objetos(String clave, Class<T> clase) {
		List<Objeto> objetos = objetos(clave);
		List<T> lista = new ArrayList<>();
		for (Objeto objeto : objetos) {
			T item = U.fromJson(objeto.toJson());
			lista.add(item);
		}
		return lista;
	}

	@SuppressWarnings("unchecked")
	public List<Object> objects(String clave) {
		List<Object> objetos = new ArrayList<>();
		Object list = get(clave);
		if (list instanceof Objeto) {
			list = ((Objeto) list).toList();
		}
		if (list instanceof List) {
			for (Object object : (List<Object>) list) {
				objetos.add(object);
			}
		}
		return objetos;
	}

	/* ========== SET ========== */
	public Objeto set(String clave) {
		Objeto objeto = new Objeto();
		set(clave, objeto);
		return objeto;
	}

	public Objeto setIf(Boolean condicion, String clave, Object valor) {
		if (condicion) {
			return set(clave, valor);
		}
		return this;
	}

	public Objeto setRaw(String clave, Object valor) {
		Objeto destino = this;
		if (clave != null && !clave.isEmpty()) {
			if (valor instanceof Fecha) {
				destino.mapa.put(clave, ((Fecha) valor).string("yyyy-MM-dd HH:mm:ss"));
			} else {
				destino.mapa.put(clave, valor);
			}
		}
		return destino;
	}

	public Objeto set(String clave, Object valor) {
		Objeto destino = this;
		if (clave != null && !clave.isEmpty()) {
			String[] partes = clave.split("\\.");
			for (Integer i = 0; i < partes.length; ++i) {
				String subClave = partes[i];
				if (i + 1 < partes.length) {
					Objeto objeto = new Objeto();
					if (!subClave.matches("[0-9]+")) {
						Object object = destino.mapa.get(subClave);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						} else {
							destino.mapa.put(subClave, objeto);
						}
					} else {
						destino.lista = destino.lista == null ? new ArrayList<>() : destino.lista;
						Integer posicion = Integer.valueOf(subClave);
						while (destino.lista.size() <= posicion) {
							destino.lista.add(new Objeto());
						}
						Object object = destino.lista.get(posicion);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						}
					}
					destino = objeto;
				} else {
					if (valor instanceof Fecha) {
						destino.mapa.put(subClave, ((Fecha) valor).string("yyyy-MM-dd HH:mm:ss"));
					} else {
						destino.mapa.put(subClave, valor);
					}
				}
			}
		}
		return destino;
	}

	/* ========== SET-LIST ========== */
	public Objeto setList() {
		lista = lista == null ? new ArrayList<>() : lista;
		return this;
	}

	public Objeto setList(String clave) {
		Objeto objeto = new Objeto();
		objeto.setList();
		return set(clave, objeto);
	}

	/* ========== DEL ========== */
	public Objeto del(String clave) {
		Objeto destino = this;
		if (clave != null && !clave.isEmpty()) {
			String[] partes = clave.split("\\.");
			for (Integer i = 0; i < partes.length; ++i) {
				String subClave = partes[i];
				if (i + 1 < partes.length) {
					Objeto objeto = new Objeto();
					if (!subClave.matches("[0-9]+")) {
						Object object = destino.mapa.get(subClave);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						} else {
							destino.mapa.put(subClave, objeto);
						}
					} else {
						destino.lista = destino.lista == null ? new ArrayList<>() : destino.lista;
						Integer posicion = Integer.valueOf(subClave);
						while (destino.lista.size() <= posicion) {
							destino.lista.add(new Objeto());
						}
						Object object = destino.lista.get(posicion);
						if (object != null && object instanceof Objeto) {
							objeto = (Objeto) object;
						}
					}
					destino = objeto;
				} else {
					destino.mapa.remove(subClave);
				}
			}
		}
		return this;
	}

	public Objeto del(String... claves) {
		for (String clave : claves) {
			del(clave);
		}
		return this;
	}

	/* ========== ADD ========== */
	public Objeto add() {
		Objeto objeto = new Objeto();
		add(objeto);
		return objeto;
	}

	public Objeto add(String clave) {
		Objeto objeto = new Objeto();
		add(clave, objeto);
		return objeto;
	}

	public Objeto add(Object valor) {
		setList().lista.add(valor);
		return this;
	}

	public Objeto addValue(String valor) {
		return add((Object) valor);
	}

	public Objeto addValue(String clave, String valor) {
		Objeto objeto = objeto(clave, null);
		objeto = objeto == null ? set(clave) : objeto;
		objeto.add((Object) valor);
		return this;
	}

	public Objeto add(String clave, Object valor) {
		Objeto objeto = objeto(clave, null);
		objeto = objeto == null ? set(clave) : objeto;
		objeto.add(valor);
		return this;
	}

	public Objeto addIf(Boolean condicion, String clave) {
		if (condicion) {
			return add(clave);
		}
		return new Objeto();
	}

	public Objeto addIf(Boolean condicion, String clave, Object valor) {
		if (condicion) {
			return add(clave, valor);
		}
		return new Objeto();
	}

	/* ========== UTIL ========== */
	public Boolean isEmpty() {
		Boolean isEmpty = true;
		if (mapa != null && !mapa.isEmpty()) {
			isEmpty = false;
		}
		if (lista != null && !lista.isEmpty()) {
			isEmpty = false;
		}
		return isEmpty;
	}

	public Boolean isMap() {
		return !isList();
	}

	public Boolean isList() {
		return lista != null;
	}

	public Boolean existe(String clave) {
		return get(clave) != null;
	}

	/* ========== ORDENAR ========== */
	// TODO: Ver Gabriel
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

	public Objeto ordenarDouble(Boolean reverse, String... campos) {
		if (this.mapa == null && this.lista == null) {
			return this;
		}

		Map<Double, Objeto> mapa;
		if (reverse == true)
			mapa = new TreeMap<>(Collections.reverseOrder());
		else
			mapa = new TreeMap<>();

		for (Objeto item : objetos()) {
			Double clave = 0.0;
			for (String campo : campos) {
				String df = item.string(campo);
				clave += Double.parseDouble(df);
			}
			mapa.put(clave, item);
		}
		lista = null;
		for (Double clave : mapa.keySet()) {
			add(mapa.get(clave));
		}
		return this;
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


	/* ========== TOSTRING ========== */
	public String toString() {
		return toJson();
	}
}
