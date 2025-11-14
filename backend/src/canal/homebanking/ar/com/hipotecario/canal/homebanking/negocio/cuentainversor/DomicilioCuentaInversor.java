package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor;

public class DomicilioCuentaInversor {

	private String alturaCalle;
	private String calle;
	private String cp;
	private String localidad;
	private String pais;
	private String provincia;
	private String piso;
	private String departamento;

	public DomicilioCuentaInversor() {
	}

	public DomicilioCuentaInversor(String alturaCalle, String calle, String cp, String localidad, String pais, String provincia, String piso, String departamento) {
		this.alturaCalle = alturaCalle;
		this.calle = calle;
		this.cp = cp;
		this.localidad = localidad;
		this.pais = pais;
		this.provincia = provincia;
		this.piso = piso;
		this.departamento = departamento;
	}

	public String getAlturaCalle() {
		return alturaCalle;
	}

	public void setAlturaCalle(String alturaCalle) {
		this.alturaCalle = alturaCalle;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		this.cp = cp;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getPiso() {
		return piso;
	}

	public void setPiso(String piso) {
		this.piso = piso;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
}
