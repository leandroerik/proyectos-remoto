package ar.com.hipotecario.mobile.api.dto.modo;

import java.time.LocalDateTime;

public class Contador {

	private Integer id;

	private String idCobis;

	private String tipo;

	private LocalDateTime momento;

	public Contador() {

	}

	public Contador(Integer id, String idCobis, String tipo, LocalDateTime momento) {
		super();
		this.id = id;
		this.idCobis = idCobis;
		this.tipo = tipo;
		this.momento = momento;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdCobis() {
		return idCobis;
	}

	public void setIdCobis(String idCobis) {
		this.idCobis = idCobis;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public LocalDateTime getMomento() {
		return momento;
	}

	public void setMomento(LocalDateTime momento) {
		this.momento = momento;
	}

}
