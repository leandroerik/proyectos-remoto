package ar.com.hipotecario.canal.tas;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

public class SesionTAS extends Sesion {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public String idSesionTAS;
	public TASClientePersona clienteTAS;
	public String idTas;

	public SesionTAS(){}

	public SesionTAS(String idSesionTAS, TASClientePersona clienteTAS, String idTas) {
		this.idSesionTAS = idSesionTAS;
		this.clienteTAS = clienteTAS;
		this.idTas = idTas;
	}

	/* ========== PERSISTENCIA ========== */
	public void save() {
		contexto.saveSesion(this);
	}

	public String getIdSesionTAS() {
		return idSesionTAS;
	}

	public void setIdSesionTAS(String idSesionTAS) {
		this.idSesionTAS = idSesionTAS;
	}

	public TASClientePersona getClienteTAS() {
		return clienteTAS;
	}

	public void setClienteTAS(TASClientePersona clienteTAS) {
		this.clienteTAS = clienteTAS;
	}

	public String getIdTas() {
		return idTas;
	}

	public void setIdTas(String idTas) {
		this.idTas = idTas;
	}
}


