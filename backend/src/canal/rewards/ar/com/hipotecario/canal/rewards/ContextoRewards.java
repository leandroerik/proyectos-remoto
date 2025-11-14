package ar.com.hipotecario.canal.rewards;

import ar.com.hipotecario.backend.Contexto;
import spark.Request;
import spark.Response;

public class ContextoRewards extends Contexto {

	/* ========== ATRIBUTOS ========== */
	private SesionRewards sesion;

	/* ========== CONSTRUCTORES ========== */
	public ContextoRewards(Request request, Response response, String canal, String ambiente) {
		super(request, response, canal, ambiente);
	}

	public SesionRewards sesion() {
		if (sesion == null) {
			sesion = super.sesion(SesionRewards.class);
		}
		return sesion;
	}

	public void eliminarSesion() {
		SesionRewards sesionActual = sesion();
		if (sesionActual != null) {
			sesionActual.usr = null;
			sesionActual.rol = null;
			sesionActual.idCobis = null;
		}

	}
}