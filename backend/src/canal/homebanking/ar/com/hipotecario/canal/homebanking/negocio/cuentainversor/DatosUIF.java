package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor;

public class DatosUIF {

	private String monedaImporteEstimado;

	public String getMonedaImporteEstimado() {
		return monedaImporteEstimado != null ? monedaImporteEstimado : "";
	}

	public void setMonedaImporteEstimado(String monedaImporteEstimado) {
		this.monedaImporteEstimado = monedaImporteEstimado;
	}

	public DatosUIF(String monedaImporteEstimado) {
		this.monedaImporteEstimado = monedaImporteEstimado;
	}

	public DatosUIF() {
	}
}
