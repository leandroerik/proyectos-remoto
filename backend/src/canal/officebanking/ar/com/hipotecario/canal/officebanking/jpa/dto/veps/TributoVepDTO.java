package ar.com.hipotecario.canal.officebanking.jpa.dto.veps;

public class TributoVepDTO {
	private String idTributarioOriginante;
	private String idTributarioContribuyente;
	private String numeroVep;
	private String tipoConsultaLink;

	public TributoVepDTO() {
	}

	public TributoVepDTO(String idTributarioOriginante, String idTributarioContribuyente, String numeroVep, String tipoConsultaLink) {
		this.idTributarioOriginante = idTributarioOriginante;
		this.idTributarioContribuyente = idTributarioContribuyente;
		this.numeroVep = numeroVep;
		this.tipoConsultaLink = tipoConsultaLink;
	}

	public String getIdTributarioOriginante() {
		return idTributarioOriginante;
	}

	public void setIdTributarioOriginante(String idTributarioOriginante) {
		this.idTributarioOriginante = idTributarioOriginante;
	}

	public String getIdTributarioContribuyente() {
		return idTributarioContribuyente;
	}

	public void setIdTributarioContribuyente(String idTributarioContribuyente) {
		this.idTributarioContribuyente = idTributarioContribuyente;
	}

	public String getNumeroVep() {
		return numeroVep;
	}

	public void setNumeroVep(String numeroVep) {
		this.numeroVep = numeroVep;
	}

	public String getTipoConsultaLink() {
		return tipoConsultaLink;
	}
}
