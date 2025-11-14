package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

public class ResponseSpAltaDepositos {
	private int sqlType;
	private Object value;

	public ResponseSpAltaDepositos(int sqlType) {
		this.sqlType = sqlType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
