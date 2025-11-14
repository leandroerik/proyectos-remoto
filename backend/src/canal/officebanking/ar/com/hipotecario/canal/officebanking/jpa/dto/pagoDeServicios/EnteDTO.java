package ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios;

public class EnteDTO {
	private String codigo;
	private String descripcion;
	private RubroDTO rubro;

	public EnteDTO(String codigo, String descripcion, RubroDTO rubro) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.rubro = rubro;
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

	public RubroDTO getRubro() {
		return rubro;
	}

	public void setRubro(RubroDTO rubro) {
		this.rubro = rubro;
	}
}
