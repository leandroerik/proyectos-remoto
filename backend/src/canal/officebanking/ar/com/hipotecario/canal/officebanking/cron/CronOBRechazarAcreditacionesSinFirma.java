package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;

public class CronOBRechazarAcreditacionesSinFirma extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBRechazarAcreditacionesSinFirma.class.getSimpleName().toUpperCase();

	public void run() {
		boolean ejecutar = true;

		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		List<PagoDeHaberesOB> acreditacionesARechazar = new ServicioPagoHaberesOB(contexto).buscarAcreditacionesSinFirmaAFechaArchivo().get();

		if (acreditacionesARechazar != null && !acreditacionesARechazar.isEmpty()) {
			OBFirmas.rechazarAcreditacionesSinFirma(contexto, acreditacionesARechazar);
		}

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);
	}
}
