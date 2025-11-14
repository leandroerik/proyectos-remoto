package ar.com.hipotecario.mobile.negocio;

public class CuentaAsociada {

	private String numero;

	private String codigo;

	private String tipo;

	private Boolean principal;

	private Boolean principalExt;

	public CuentaAsociada() {

	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Boolean getPrincipalExt() {
		return principalExt;
	}

	public void setPrincipalExt(Boolean principalExt) {
		this.principalExt = principalExt;
	}

}
