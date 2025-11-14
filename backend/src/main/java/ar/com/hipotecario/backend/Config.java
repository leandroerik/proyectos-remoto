package ar.com.hipotecario.backend;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Properties;
import ar.com.hipotecario.backend.cron.CronConfig;
import ar.com.hipotecario.canal.officebanking.LogOB;

public class Config {

	public static final String AMBIENTE_POR_DEFECTO = "desarrollo";

	/* ========== ATRIBUTOS ========== */
	private Map<String, String> properties;

	/* ========== CONSTRUCTORES ========== */
	public Config() {
		this.properties = esOpenShift() ? variablesEntorno() : properties(AMBIENTE_POR_DEFECTO);
		this.properties.putAll(cronConfig());
	}

	public Config(String ambiente) {
		this.properties = esOpenShift() ? variablesEntorno()
				: properties(ambiente != null && !ambiente.isEmpty() ? ambiente : AMBIENTE_POR_DEFECTO);
		this.properties.putAll(cronConfig());
	}

	/* ========== CONFIG ========== */
	private Map<String, String> variablesEntorno() {
		return new HashMap<>(System.getenv());
	}

	private static Map<String, String> properties(String ambiente) {
		try {
			return Properties.toMap(String.format("ambiente.%s.properties", ambiente));
		} catch (Exception e) {
			throw new RuntimeException(String.format("No existe ambiente: %s", ambiente));
		}
	}

	private Map<String, String> cronConfig() {
		return esOpenShift() ? CronConfig.config : new HashMap<>();
	}

	/* ========== METODOS ESTATICOS ========== */
	public static Boolean esOpenShift() {
		return "true".equals(System.getenv("openshift"));
	}

	public static Boolean esARO() {
		return "true".equals(System.getenv("ARO"));
	}

	public static Integer puerto() {
		return new Config().integer("puerto", 8080);
	}

	public static String ambiente() {
		return new Config().string("ambiente", AMBIENTE_POR_DEFECTO);
	}

	public static Map<String, String> properties() {
		return new Config().properties;
	}

	public static String basedatos() {
		return new Config().string("basedatos", null);
	}

	public static String aes_key() {
		return new Config().string("aes_key", AMBIENTE_POR_DEFECTO);
	}

	/* ========== AMBIENTE ========== */
	public Boolean esDesarrollo() {
		return "desarrollo".equals(ambiente());
	}

	public Boolean esIntegracion() {
		return "integracion".equals(ambiente());
	}

	public Boolean esHomologacion() {
		return "homologacion".equals(ambiente());
	}

	public Boolean esProduccion() {
		return "produccion".equals(ambiente());
	}

	/* ========== GET ========== */
	public String string(String clave) {
		return string(clave, "");
	}

	public String string(String clave, String valorPorDefecto) {
		String valor = properties.get(clave);
		valor = valor != null ? valor : valorPorDefecto;
		valor = desencriptar(valor);
		valor = valor != null && valor.equalsIgnoreCase("null") ? null : valor;
		return valor;
	}

	public Integer integer(String clave) {
		return integer(clave, null);
	}

	public Integer integer(String clave, Integer valorPorDefecto) {
		String string = string(clave, null);
		Integer valor = string != null ? Integer.valueOf(string) : valorPorDefecto;
		return valor;
	}

	public Long longer(String clave) {
		return longer(clave, null);
	}

	public Long longer(String clave, Long valorPorDefecto) {
		String string = string(clave, null);
		Long valor = string != null ? Long.valueOf(string) : valorPorDefecto;
		return valor;
	}

	public BigDecimal bigDecimal(String clave) {
		return bigDecimal(clave, null);
	}

	public BigDecimal bigDecimal(String clave, String valorPorDefecto) {
		String string = string(clave, null);
		BigDecimal valor = string != null ? new BigDecimal(string)
				: valorPorDefecto != null ? new BigDecimal(valorPorDefecto) : null;
		return valor;
	}

	public Boolean bool(String clave) {
		return bool(clave, false);
	}

	public Boolean bool(String clave, Boolean valorPorDefecto) {
		String string = string(clave, null);
		Boolean valor = string != null ? Boolean.valueOf(string) : valorPorDefecto;
		return valor;
	}

	/* ========== GRUPO ========== */
	public Set<String> grupo(String prefijo) {
		Set<String> grupo = new HashSet<>();
		for (String clave : properties.keySet()) {
			if (clave.startsWith(prefijo + "_")) {
				grupo.add(clave);
			}
		}
		return grupo;
	}

	/* ========== ENCRIPTACION ========== */
	public static String encriptar(String texto) {
		if (texto != null) {
			texto = "ENC(" + Encriptador.encriptarPBEMD5DES("SIN_PASSWORD", texto) + ")";
		}
		return texto;
	}

	public static String desencriptar(String texto) {
		if (texto != null) {
			Integer posicionInicial = texto.indexOf("ENC(");
			Integer posicionFinal = texto.indexOf(")", posicionInicial + 4);
			while (posicionInicial >= 0 && posicionFinal >= 0) {
				String inicio = texto.substring(0, posicionInicial);
				String medio = texto.substring(posicionInicial + 4, posicionFinal);
				String fin = texto.substring(posicionFinal + 1);
				try {
					texto = inicio + Encriptador.desencriptarPBEMD5DES("SIN_PASSWORD", medio) + fin;
				} catch (Exception e) {
					texto = inicio + Encriptador.desencriptarPBEMD5DES("W4UHujtA84", medio) + fin;
				}
				posicionInicial = texto.indexOf("ENC(");
				posicionFinal = texto.indexOf(")", posicionInicial + 4);
			}
		}
		if (texto != null) {
			Integer posicionInicial = texto.indexOf("AES(");
			Integer posicionFinal = texto.indexOf(")", posicionInicial + 4);
			while (posicionInicial >= 0 && posicionFinal >= 0) {
				String inicio = texto.substring(0, posicionInicial);
				String medio = texto.substring(posicionInicial + 4, posicionFinal);
				String fin = texto.substring(posicionFinal + 1);
				texto = inicio + Encriptador.desencriptarAES256CBC(aes_key(), medio) + fin;
				posicionInicial = texto.indexOf("AES(");
				posicionFinal = texto.indexOf(")", posicionInicial + 4);
			}
		}
		return texto;
	}
	/*public static String desencriptarOB(String texto) {
		try{
		String incio=null;
		if(	texto != null){
			incio  = texto.length() >= 4 ? texto.substring(0, 4) : "";
			if (incio.equals("AES(") || incio.equals("ENC(")){
				int posicionInicial = texto.indexOf(incio);
				int posicionFinal = texto.indexOf(")", posicionInicial + 4);
				while (posicionInicial >= 0 && posicionFinal >= 0) {
					String inicio = texto.substring(0, posicionInicial);
					String medio = texto.substring(posicionInicial + 4, posicionFinal);
					String fin = texto.substring(posicionFinal + 1);
					texto = inicio + Encriptador.desencriptarAES256CBC(aes_key(), medio) + fin;
					posicionInicial = texto.indexOf(incio);
					posicionFinal = texto.indexOf(")", posicionInicial + 4);
				}
			}
		}
		return texto;
		}catch (Exception ex){
			return null;
		}
	}*/
	public static String desencriptarOB(String texto) {
		try{
			if (texto == null) {
				return null;
			}

			String inicioMarcador = texto.length() >= 4 ? texto.substring(0, 4) : "";
			if (!inicioMarcador.equals("AES(") && !inicioMarcador.equals("ENC(")) {
				return texto;
			}

			int maxIteraciones = 10; 
			int iteraciones = 0;

			int posicionInicial = texto.indexOf(inicioMarcador);
			int posicionFinal = texto.indexOf(")", posicionInicial + 4);
	    
			while (posicionInicial >= 0 && posicionFinal > posicionInicial && iteraciones < maxIteraciones) {
				try {
					String inicio = texto.substring(0, posicionInicial);
					String medio = texto.substring(posicionInicial + 4, posicionFinal);
					String fin = texto.substring(posicionFinal + 1);

					String desencriptado = Encriptador.desencriptarAES256CBC(aes_key(), medio);
					texto = inicio + desencriptado + fin;
					
					posicionInicial = texto.indexOf(inicioMarcador);
					posicionFinal = texto.indexOf(")", posicionInicial + 4);
				} catch (Exception e) {
					texto = texto.substring(0, posicionInicial) + "[ERROR_DESCIFRANDO]" + texto.substring(posicionFinal + 1);
					break;
				}

				iteraciones++;
			}
			return texto;
		}catch (Exception ex){
			System.out.println("ERROR_desencriptarOB" + ex.getMessage());
			return null;
		}
	}

	public static String encriptarAES(String texto) {
		return Encriptador.encriptarAES("ENCRIPTAR_AES_KEY", texto);
	}

	public static String desencriptarAES(String texto) {
		return Encriptador.desencriptarAES("ENCRIPTAR_AES_KEY", texto);
	}
}