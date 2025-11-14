package ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios;

import java.math.BigDecimal;

public class ConceptoDTO {
	private String codigo;
	private String descripcion;
	private Boolean isIngresoReferencia;
	private Boolean isLongitudReferencia;
	private BigDecimal longitudMinimaTextoReferencia;
	private BigDecimal longitudMaximaTextoReferencia;
	private Boolean ingresoImportes;

	public ConceptoDTO(String codigo, String descripcion, Boolean isIngresoReferencia, Boolean isLongitudReferencia, BigDecimal longitudMinimaTextoReferencia, BigDecimal longitudMaximaTextoReferencia, Boolean ingresoImportes) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.isIngresoReferencia = isIngresoReferencia;
		this.isLongitudReferencia = isLongitudReferencia;
		this.longitudMinimaTextoReferencia = longitudMinimaTextoReferencia;
		this.longitudMaximaTextoReferencia = longitudMaximaTextoReferencia;
		this.ingresoImportes = ingresoImportes;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean getIngresoReferencia() {
		return isIngresoReferencia;
	}

	public void setIngresoReferencia(Boolean ingresoReferencia) {
		isIngresoReferencia = ingresoReferencia;
	}

	public Boolean getLongitudReferencia() {
		return isLongitudReferencia;
	}

	public void setLongitudReferencia(Boolean longitudReferencia) {
		isLongitudReferencia = longitudReferencia;
	}

	public BigDecimal getLongitudMinimaTextoReferencia() {
		return longitudMinimaTextoReferencia;
	}

	public void setLongitudMinimaTextoReferencia(BigDecimal longitudMinimaTextoReferencia) {
		this.longitudMinimaTextoReferencia = longitudMinimaTextoReferencia;
	}

	public BigDecimal getLongitudMaximaTextoReferencia() {
		return longitudMaximaTextoReferencia;
	}

	public void setLongitudMaximaTextoReferencia(BigDecimal longitudMaximaTextoReferencia) {
		this.longitudMaximaTextoReferencia = longitudMaximaTextoReferencia;
	}

	public Boolean getIngresoImportes() {
		return ingresoImportes;
	}

	public void setIngresoImportes(Boolean ingresoImportes) {
		this.ingresoImportes = ingresoImportes;
	}
}
