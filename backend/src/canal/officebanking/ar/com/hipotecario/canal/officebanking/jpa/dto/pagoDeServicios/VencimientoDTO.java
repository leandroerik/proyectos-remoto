package ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios;

import java.math.BigDecimal;

public class VencimientoDTO {
	private String id;
	private String fecha;
	private BigDecimal importe;
	private ConceptoDTO concepto;

	public VencimientoDTO(String id, String fecha, BigDecimal importe, ConceptoDTO concepto) {
		this.id = id;
		this.fecha = fecha;
		this.importe = importe;
		this.concepto = concepto;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public ConceptoDTO getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoDTO concepto) {
		this.concepto = concepto;
	}
}
