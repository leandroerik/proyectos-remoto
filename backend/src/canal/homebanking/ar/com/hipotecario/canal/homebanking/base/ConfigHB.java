package ar.com.hipotecario.canal.homebanking.base;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import ar.com.hipotecario.canal.homebanking.lib.Encriptador;

public class ConfigHB {

	private static final String AMBIENTE = "desarrollo"; // desarrollo, integracion, homologacion, produccion=======

	/* ========== ATRIBUTOS ========== */
	private static Map<String, String> codCausalesConComprobantes = new HashMap<>();
	public static String ambiente = ambiente(AMBIENTE);
	public static Map<String, String> mapa;

	static {
		mapa = mapa();
		try {
			cargarMapaCausales("transferencias", get("causales_de_transferencias"));
			cargarMapaCausales("serviciosPagados", get("causales_de_pago_de_servicios"));
			cargarMapaCausales("pagoPrestamos", get("causales_de_pago_pp"));
			cargarMapaCausales("pagoTarjetasCredito", get("causales_de_pago_tc"));
			cargarMapaCausales("vepsPagados", get("causales_de_pago_vep"));
			cargarMapaCausales("debitosAutomaticos", get("causales_de_pago_debitos_automaticos"));
		} catch (Exception e) {
		}
	}

	/* ========== INICIALIZACION ========== */
	private static Map<String, String> mapa() {
		Map<String, String> mapa = properties("ambiente." + ambiente + ".properties");
		for (String clave : claves()) {
			String valor = esOpenShift(mapa) ? System.getenv(clave) : mapa.get(clave);
			mapa.put(clave, valor);
		}
		return mapa;
	}

	private static Set<String> claves() {
		Set<String> claves = new LinkedHashSet<>();
		for (String ambiente : Objeto.listOf("desarrollo", "integracion", "homologacion", "produccion")) {
			Map<String, String> mapa = properties("ambiente." + ambiente + ".properties");
			claves.addAll(mapa.keySet());
		}
		return claves;
	}

	private static String get(String clave) {
		if (clave != null && !clave.startsWith("hb_") && !clave.startsWith("backend_")) {
			clave = "hb_" + clave;
		}
		String valor = mapa.get(clave);
		if (valor == null) {
			valor = System.getenv(clave);
			if (valor != null) {
				mapa.put(clave, valor);
			}
		}
		if (valor != null && valor.startsWith("ENC(") && valor.endsWith(")")) {
			valor = Encriptador.desencriptarPBE(valor);
		}
		return valor;
	}

	public static Map<String, String> properties(String ruta) {
		try {
			Properties properties = new Properties();
			try (InputStream is = ConfigHB.class.getResourceAsStream("/" + ruta)) {
				properties.load(is);
			}
			Map<String, String> mapa = new LinkedHashMap<>();
			for (Object clave : properties.keySet()) {
				mapa.put(clave.toString(), properties.getProperty(clave.toString()).trim());
			}
			return mapa;
		} catch (Exception e) {
			return new LinkedHashMap<>();
		}
	}

	public static Map<String, String> getCausales() {
		return codCausalesConComprobantes;
	}

	/* ========== UTIL ========== */
	public static String ambiente(String ambientePorDefecto) {
		String ambiente = System.getenv("ambiente");
		return ambiente != null ? ambiente : ambientePorDefecto;
	}

	public static Boolean esOpenShift() {
		return esOpenShift(mapa);
	}

	private static Boolean esOpenShift(Map<String, String> mapa) {
		Boolean esOpenShift = false;
		esOpenShift |= "true".equals(mapa.get("openshift"));
		esOpenShift |= "true".equals(System.getenv("openshift"));
		return esOpenShift;
	}

	public static Boolean esDesarrollo() {
		Boolean esProduccion = "desarrollo".equals(ambiente);
		esProduccion |= ambiente == null;
		esProduccion |= ambiente.isEmpty();
		return esProduccion;
	}

	public static Boolean esHomologacion() {
		Boolean esProduccion = "homologacion".equals(ambiente);
		esProduccion |= ambiente == null;
		esProduccion |= ambiente.isEmpty();
		return esProduccion;
	}

	public static Boolean esProduccion() {
		Boolean esProduccion = "produccion".equals(ambiente);
		esProduccion |= ambiente == null;
		esProduccion |= ambiente.isEmpty();
		return esProduccion;
	}

	public static Boolean esARO() {
		return "true".equals(System.getenv("ARO"));
	}

	/* ========== METODOS PUBLICOS ========== */
	public static String string(String clave) {
		return string(clave, null);
	}

	public static String string(String clave, String valorPorDefecto) {
		return get(clave) != null ? get(clave) : valorPorDefecto;
	}

	public static Integer integer(String clave) {
		return integer(clave, null);
	}

	public static Integer integer(String clave, Integer valorPorDefecto) {
		return get(clave) != null ? Integer.valueOf(get(clave)) : valorPorDefecto;
	}

	public static Long longer(String clave, Long valorPorDefecto) {
		return get(clave) != null ? Long.valueOf(get(clave)) : valorPorDefecto;
	}

	public static BigDecimal bigDecimal(String clave) {
		return bigDecimal(clave, (BigDecimal) null);
	}

	public static BigDecimal bigDecimal(String clave, BigDecimal valorPorDefecto) {
		return get(clave) != null ? new BigDecimal(get(clave)) : valorPorDefecto;
	}

	public static BigDecimal bigDecimal(String clave, String valorPorDefecto) {
		return get(clave) != null ? new BigDecimal(get(clave)) : new BigDecimal(valorPorDefecto);
	}

	public static Boolean bool(String clave) {
		return bool(clave, false);
	}

	public static Boolean bool(String clave, Boolean valorPorDefecto) {
		return get(clave) != null ? Boolean.valueOf(get(clave)) : valorPorDefecto;
	}

	private static void cargarMapaCausales(String causal, String codigosCausal) {
		for (String codigoCausal : codigosCausal.split("_")) {
			codCausalesConComprobantes.put(codigoCausal, causal);
		}
	}

	/* ========== TEST ========== */
	public static Map<String, String> variables(String ambiente) {
		Map<String, String> mapa = null;
		if ("true".equals(System.getenv("openshift"))) {
			mapa = System.getenv();
		} else {
			mapa = properties("ambiente." + ambiente + ".properties");
		}
		return new TreeMap<>(mapa);
	}
}
