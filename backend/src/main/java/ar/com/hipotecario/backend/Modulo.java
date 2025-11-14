package ar.com.hipotecario.backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;

public class Modulo extends Base {

	private static Logger log = LoggerFactory.getLogger(Modulo.class);

	/* ========== RESPUESTA ========== */
	protected static Objeto respuesta() {
		return respuesta("0", null, null);
	}

	protected static Objeto respuesta(String estado) {
		Objeto objeto = new Objeto();
		objeto.set("estado", estado);
		return objeto;
	}

	protected static Objeto respuesta(String clave, Object valor) {
		return respuesta("0", clave, valor);
	}

	protected static Objeto respuesta(String estado, String clave, Object valor) {
		Objeto objeto = new Objeto();
		objeto.set("estado", estado);
		objeto.set(clave, valor);
		return objeto;
	}

	public static String importe(BigDecimal valor) {
		String numero = "";
		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat("###,##0.00", decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	public static String ultimos4digitos(String numero) {
		String ultimos4digitos = "";
		if (numero != null && numero.length() >= 4) {
			ultimos4digitos = numero.substring(numero.length() - 4, numero.length());
		}
		return ultimos4digitos;
	}

	public static String moneda(String codigoMoneda) {
		String simboloMoneda = "";
		simboloMoneda = "80".equals(codigoMoneda) ? "Pesos" : simboloMoneda;
		simboloMoneda = "2".equals(codigoMoneda) ? "Dólares" : simboloMoneda;
		simboloMoneda = "98".equals(codigoMoneda) ? "Euros" : simboloMoneda;
		simboloMoneda = "88".equals(codigoMoneda) ? "UVAs" : simboloMoneda;
		return simboloMoneda;
	}

	public static String simboloMoneda(String codigoMoneda) {
		String simboloMoneda = "";
		simboloMoneda = "80".equals(codigoMoneda) ? "$" : simboloMoneda;
		simboloMoneda = "2".equals(codigoMoneda) ? "USD" : simboloMoneda;
		simboloMoneda = "98".equals(codigoMoneda) ? "Eur" : simboloMoneda;
		simboloMoneda = "88".equals(codigoMoneda) ? "UVAs" : simboloMoneda;
		return simboloMoneda;
	}

	public static String simboloMonedaActual(String codigoMoneda) {
		String simboloMoneda = "";
		simboloMoneda = "80".equals(codigoMoneda) ? "$" : simboloMoneda;
		simboloMoneda = "2".equals(codigoMoneda) ? "U$S" : simboloMoneda;
		simboloMoneda = "98".equals(codigoMoneda) ? "Eur" : simboloMoneda;
		simboloMoneda = "88".equals(codigoMoneda) ? "UVAs" : simboloMoneda;
		return simboloMoneda;
	}

	protected static Redireccion redireccion(String url) {
		return new Redireccion(url);
	}

	/* ========== LOG ========== */
	protected static class Log {
		private static Gson gson = new Gson();

		public String canal;
		public String cuit;
		public String idCobis;
		public String tipo;
		public String texto;
		public String stackTrace;

		public String toString() {
			return gson.toJson(this);
		}
	}

	protected static void logProceso(Contexto contexto, String texto) {
		if (contexto.config != null && contexto.config.bool("kibana")) {
			Log log = new Log();
			log.canal = contexto.canal();
			log.cuit = "";
			log.idCobis = "";
			log.tipo = "info";
			log.texto = texto;
			Modulo.log.info(log.toString());
		} else {
			Modulo.log.info(Fecha.ahora().string("[HH:mm:ss] ") + texto);
		}
	}

	protected static void log(Contexto contexto, String texto) {
		if (contexto.config != null && contexto.config.bool("kibana")) {
			Log log = new Log();
			log.canal = contexto.canal();
			log.cuit = contexto.sesion() != null ? contexto.sesion().cuil : "";
			log.idCobis = contexto.sesion() != null ? contexto.sesion().idCobis : "";
			log.tipo = "info";
			log.texto = texto;
			Modulo.log.info(log.toString());
		} else {
			Modulo.log.info(Fecha.ahora().string("[HH:mm:ss] ") + texto);
		}
	}

	protected static void log(Contexto contexto, Exception e) {
		if (contexto.config != null && contexto.config.bool("kibana")) {
			Log log = new Log();
			log.canal = contexto.canal();
			log.cuit = contexto.sesion() != null ? contexto.sesion().cuil : "";
			log.idCobis = contexto.sesion() != null ? contexto.sesion().idCobis : "";
			log.tipo = "error";
			log.stackTrace = Texto.stackTrace(e);
			Modulo.log.error(log.toString());
		} else {
			Modulo.log.error(Fecha.ahora().string("[HH:mm:ss] ") + Texto.stackTrace(e));
		}
	}

	public static Boolean parametriaDeshabilitada(String valor) {
		return empty(valor) || valor.equals("0") || valor.equals("false");
	}

	public static Boolean isTimeOut(String message) {
		if (empty(message))
			return false;

		return message.toLowerCase().contains("timeout")
				|| message.toLowerCase().contains("timed out")
				|| message.toLowerCase().contains("imprevisto")
				|| message.toLowerCase().contains("error accediendo al servicio")
				|| message.toLowerCase().contains("el servidor está actuando de proxy o gateway");
	}

	public static Boolean isInformado(String message) {
		if (empty(message))
			return false;

		return message.contains("lista de informados");
	}
}
