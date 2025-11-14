package ar.com.hipotecario.canal.homebanking.lib;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import ar.com.hipotecario.mobile.ConfigMB;
import org.apache.commons.lang3.StringUtils;

public abstract class Formateador {
	private static final String FORMATO_PRECIO = ConfigMB.string("formato_precio");

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

	public static String importe(BigDecimal valor) {
		return importe(valor, 2);
	}
	
	public static String importe(BigDecimal valor, int decimales) {
		String numero = "";
		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			String formatoDecimales = (decimales==0)?"":"."+"0".repeat(decimales);
			DecimalFormat decimalFormat = new DecimalFormat("###,##0"+formatoDecimales, decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	public static String importeSinRedondeo(BigDecimal valor) {
		String numero = "";
		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat("###,##0.00", decimalFormatSymbols);
			// decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	public static String entero(Long valorLong) {
		String numero = "";
		if (valorLong != null) {
			BigDecimal valor = new BigDecimal(valorLong);
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat("###,##0", decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	// emm-20190520
	public static String importeCantDecimales(BigDecimal valor, int cantDecimales) {
		String numero = "";
		String pattern = "###,##0.";
		for (int x = 0; x < cantDecimales; x++) {
			pattern += "0";
		}

		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	public static String importeConVE(BigDecimal valor) {
		String numero = "";
		String pattern = FORMATO_PRECIO;

		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			numero = decimalFormat.format(valor);
		}
		return numero;
	}

	public static String importe(BigDecimal valor, String valorPorDefecto) {
		String importe = importe(valor);
		if (importe == null || importe.isEmpty()) {
			return valorPorDefecto;
		}
		return importe;
	}

	public static String ultimos4digitos(String numero) {
		String ultimos4digitos = "";
		if (numero != null && numero.length() >= 4) {
			ultimos4digitos = numero.substring(numero.length() - 4, numero.length());
		}
		return ultimos4digitos;
	}

	public static String cbu(String numero) {
		String cbuFormateado = "";
		if (numero != null && numero.length() == 22) {
			cbuFormateado = numero.substring(0, 8) + "-" + numero.substring(8);
		}
		return cbuFormateado;
	}

	public static String tipoCuenta(String tipoCuenta) {
		String descripcion = "";
		descripcion = "AHO".equals(tipoCuenta) ? "CA" : descripcion;
		descripcion = "CTE".equals(tipoCuenta) ? "CC" : descripcion;
		return descripcion;
	}

	public static String cuit(String cuit) {
		String descripcion = "";
		if (cuit.length() == 11) {
			descripcion = cuit.substring(0, 2) + "-" + cuit.substring(2, 10) + "-" + cuit.substring(10, 11);
		}
		return descripcion;
	}

	public static Double importe(String monto) {
		Double montoFormat = 0.00;
		if (StringUtils.isNotEmpty(monto)) {
			monto = monto.replace(".", "");
			monto = monto.replace(",", ".");
			montoFormat = Double.parseDouble(monto);
		}
		return montoFormat;
	}

	public static String eliminarTextoEnDescripcion(String descripcion, String texto) {
		String descripcionModificada = StringUtils.substringAfter(descripcion, texto);
		return descripcionModificada;
	}

	public static BigDecimal importeNegativo(BigDecimal importe) {
		return importe.negate();
	}

	public static String importeTlf(BigDecimal valor, Integer cantidadMaximaCaracteres) {
		String numero = "";
		if (valor != null) {
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			DecimalFormat decimalFormat = new DecimalFormat("###,##0.00", decimalFormatSymbols);
			decimalFormat.setRoundingMode(RoundingMode.DOWN);
			decimalFormat.setMaximumFractionDigits(2);
			decimalFormat.setMinimumFractionDigits(2);
			numero = decimalFormat.format(valor).replace(".", "").replace(",", "");
			int cantidadRellenarIzquierda = cantidadMaximaCaracteres - numero.length();
			for (int i = 0; i < cantidadRellenarIzquierda; i++) {
				numero = "0".concat(numero);
			}
		}
		return numero;
	}
}
