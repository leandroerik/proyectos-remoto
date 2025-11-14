package ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios;

public class PagoUnicoDTO {
	private String codigoAdhesion;
	private String codigoLink;
	private EnteDTO ente;
	private ConceptoDTO concepto;
	private VencimientoDTO vencimiento;

	public PagoUnicoDTO() {
	}

	public PagoUnicoDTO(String codigoAdhesion, String codigoLink, EnteDTO ente, ConceptoDTO concepto, VencimientoDTO vencimiento) {
		this.codigoAdhesion = codigoAdhesion;
		this.codigoLink = codigoLink;
		this.ente = ente;
		this.concepto = concepto;
		this.vencimiento = vencimiento;
	}

	public String getCodigoAdhesion() {
		return codigoAdhesion;
	}

	public void setCodigoAdhesion(String codigoAdhesion) {
		this.codigoAdhesion = codigoAdhesion;
	}

	public String getCodigoLink() {
		return codigoLink;
	}

	public void setCodigoLink(String codigoLink) {
		this.codigoLink = codigoLink;
	}

	public EnteDTO getEnte() {
		return ente;
	}

	public void setEnte(EnteDTO ente) {
		this.ente = ente;
	}

	public ConceptoDTO getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoDTO concepto) {
		this.concepto = concepto;
	}

	public VencimientoDTO getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(VencimientoDTO vencimiento) {
		this.vencimiento = vencimiento;
	}
}
