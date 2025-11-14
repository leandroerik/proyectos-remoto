package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBTransferencias;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEjecucionBatchOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EjecucionBatchOB;

public class CronOBAperturaYCierreCamara extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBAperturaYCierreCamara.class.getSimpleName().toUpperCase();
	private static final String CRON_LECTURA_NOVEDADES = CronOBLeeNovedadesInterbanking.class.getSimpleName().toUpperCase();

	public void run() {

		// ob_cron_paertura_camara_ejecutar --> Variable Openshift para saber si se
		// ejecuta o no el CRON.
		boolean ejecutar = true;
		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", new Objeto().set("hora", LocalDate.now().toString()).set("cron", "AperturaYCierre").toString(), CRON);

		boolean camaraAbiertaConfig = Boolean.valueOf(contexto.config.bool("ob_camara_interbanking"));
		Objeto horario = (Objeto) OBTransferencias.horarioCamara(contexto);
		boolean camaraAbierta = (boolean) horario.get("datos.camaraAbierta");

		LogOB.evento(contexto, "VALIDA APERTURA CAMARA", new Objeto().set("camaraAbiertaConfig", String.valueOf(camaraAbiertaConfig)).set("camaraAbierta", String.valueOf(camaraAbierta)).toString(), CRON);

		ServicioEjecucionBatchOB servicioEjecucion = new ServicioEjecucionBatchOB(contexto);
		EjecucionBatchOB ejecucion = servicioEjecucion.buscarPorCron(CRON_LECTURA_NOVEDADES, LocalDate.now()).tryGet();

		if (ejecucion == null) {
			ejecucion = new EjecucionBatchOB();
			ejecucion.cron = CRON_LECTURA_NOVEDADES;
			ejecucion.fechaEjecucion = LocalDate.now();
			ejecucion.ultimaNovedad = 0;
			servicioEjecucion.create(ejecucion);
		}

		// PARA ABRIR LA CAMARA A LA FUERZA
		if (camaraAbiertaConfig) {
			contexto.set("ob_camara_interbanking", "true");
			LogOB.evento(contexto, "CAMARA_ABIERTA_POR_CONFIG", String.valueOf(camaraAbierta), CRON);
			return;
		}

		// CIRCUITO NORMAL
		if (camaraAbierta) {
			contexto.set("ob_camara_interbanking", "true");
			LogOB.evento(contexto, "CAMARA_ABIERTA", LocalDateTime.now().toString(), CRON);
		} else {
			contexto.set("ob_camara_interbanking", "false");
			LogOB.evento(contexto, "CAMARA_CERRADA", LocalDateTime.now().toString(), CRON);
		}

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

	}
}