package ar.com.hipotecario.canal.rewards;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.canal.tas.SesionTAS;

public class SesionRewards extends Sesion {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public String usr;
	public String rol;

	/* ========== PERSISTENCIA ========== */
	public void save() {
		contexto.saveSesion(this);
	}

}
