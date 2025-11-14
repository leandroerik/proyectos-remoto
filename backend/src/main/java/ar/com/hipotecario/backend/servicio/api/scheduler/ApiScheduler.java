package ar.com.hipotecario.backend.servicio.api.scheduler;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.scheduler.HorariosBancarios.HorarioBancario;

public class ApiScheduler extends Api {

	/* ========== CONSTANTES ========== */
	public static String API = "scheduler";

	/* ========== Horario Bancario Rest Controller ========== */

	// GET /v1/horariobancario
	public static Futuro<HorariosBancarios> posicionConsolidada(Contexto contexto) {
		return futuro(() -> HorariosBancarios.get(contexto));
	}

	// GET /v1/horariobancario/identificador/{id}
	public static Futuro<HorarioBancario> horario(Contexto contexto, String identificador) {
		return futuro(() -> HorariosBancarios.get(contexto, identificador));
	}
}
