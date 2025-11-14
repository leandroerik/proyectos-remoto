package ar.com.hipotecario.canal.homebanking.negocio;

public class EstadoDocumentacion {
	private String nombreCompleto;
	private int estado;
	private String idCobis;
	private long cuil;
	private TipoNotificacion tipoNotificacion;

	public EstadoDocumentacion(String nombreCompleto, int estado, String idCobis, long cuil, TipoNotificacion tipoNotificacion) {
		super();
		this.nombreCompleto = nombreCompleto;
		this.estado = estado;
		this.idCobis = idCobis;
		this.cuil = cuil;
		this.tipoNotificacion = tipoNotificacion;
	}

	public EstadoDocumentacion(String nombreCompleto, int estado, long cuil, TipoNotificacion tipoNotificacion) {
		super();
		this.nombreCompleto = nombreCompleto;
		this.estado = estado;
		this.idCobis = "";
		this.cuil = cuil;
		this.tipoNotificacion = tipoNotificacion;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public int getEstado() {
		return estado;
	}

	public void setEstado(int estado) {
		this.estado = estado;
	}

	public long getCuil() {
		return cuil;
	}

	public void setCuil(long cuil) {
		this.cuil = cuil;
	}

	public TipoNotificacion getTipoNotificacion() {
		return tipoNotificacion;
	}

	public void setTipoNotificacion(TipoNotificacion tipoNotificacion) {
		this.tipoNotificacion = tipoNotificacion;
	}

	public String getIdCobis() {
		return idCobis;
	}

	public void setIdCobis(String idCobis) {
		this.idCobis = idCobis;
	}
}
