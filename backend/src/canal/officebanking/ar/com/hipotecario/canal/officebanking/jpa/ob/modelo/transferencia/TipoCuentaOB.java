package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum TipoCuentaOB {
	NA("N/A"), CC("Cuenta Corriente"), CA("Caja de Ahorro");

	public String descripcionLarga;

	private TipoCuentaOB(String descripcion) {
		this.descripcionLarga = descripcion;
	}

	public String getDescripcionLarga() {
		return descripcionLarga;
	}

	public void setDescripcionLarga(String descripcionLarga) {
		this.descripcionLarga = descripcionLarga;
	}

	public enum TipoCuentaATE {
		CUENTA_CORRIENTE_PESOS("01", "Cuenta Corriente Pesos", "CC", "80"), CUENTA_AHORRO_PESOS("02", "Cuenta de Ahorro Pesos", "CA", "80"), CUENTA_CORRIENTE_DOLARES("13", "Cuenta Corriente Dolares", "CC", "2"), CUENTA_AHORRO_DOLARES("15", "Cuenta de Ahorro Dolares", "CA", "2"), CUENTA_CORRIENTE_EURO("21", "Cuenta Corriente Euro", "CC", "98"), CUENTA_AHORRO_EURO("22", "Cuenta de Ahorro Euro", "CA", "98");

		private static final Map<String, TipoCuentaATE> lookup = new ConcurrentHashMap<String, TipoCuentaATE>();
		private String value;
		private String name;
		private String tipo;
		private String moneda;

		private TipoCuentaATE(String value, String name, String tipo, String moneda) {
			this.value = value;
			this.name = name;
			this.tipo = tipo;
			this.moneda = moneda;
		}

		static {
			for (TipoCuentaATE s : TipoCuentaATE.values()) {
				String value = s.getValue();
				lookup.put(value, s);
			}
		}

		public static TipoCuentaATE get(String value) {
			return lookup.get(value);
		}

		public static String getTipo(String moneda, String tipoCuenta) {
			for (TipoCuentaATE s : TipoCuentaATE.values()) {
				if (s.tipo.equals(tipoCuenta) && s.moneda.equals(moneda)) {
					return s.getValue();
				}
			}
			return null;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

	}

}