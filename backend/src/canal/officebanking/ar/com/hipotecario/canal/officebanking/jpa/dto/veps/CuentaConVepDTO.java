package ar.com.hipotecario.canal.officebanking.jpa.dto.veps;

import java.util.List;

public class CuentaConVepDTO {
	private String numeroCuenta;
	private String tipoConsultaLink;
	private List<TributoVepDTO> listaVep;

	public CuentaConVepDTO() {
	}

	public CuentaConVepDTO(String numeroCuenta, List<TributoVepDTO> listaVep, String tipoConsultaLink) {
		this.tipoConsultaLink = tipoConsultaLink;
		this.numeroCuenta = numeroCuenta;
		this.listaVep = listaVep;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public List<TributoVepDTO> getListaVep() {
		return listaVep;
	}

	public void setListaVep(List<TributoVepDTO> listaVep) {
		this.listaVep = listaVep;
	}

	public String getTipoConsultaLink() {
		return tipoConsultaLink;
	}
}
