package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

import java.math.BigDecimal;

public class ResponseInsertValores {
	public boolean hasErrors;
	public BigDecimal importeTotal;

	public ResponseInsertValores(boolean hasErrors, BigDecimal importeTotal) {
		this.hasErrors = hasErrors;
		this.importeTotal = importeTotal;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public BigDecimal getImporteTotal() {
		return importeTotal;
	}

	public void setImporteTotal(BigDecimal importeTotal) {
		this.importeTotal = importeTotal;
	}

}
