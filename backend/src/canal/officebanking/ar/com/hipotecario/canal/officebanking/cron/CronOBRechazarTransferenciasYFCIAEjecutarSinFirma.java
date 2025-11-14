package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.OBInversiones;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioFCIOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;

public class CronOBRechazarTransferenciasYFCIAEjecutarSinFirma extends CronJob {
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBRechazarTransferenciasYFCIAEjecutarSinFirma.class.getSimpleName().toUpperCase();

	public void run() {
		boolean ejecutar = true;

		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
		List<TransferenciaOB> transferenciasARechazar = servicioTransferenciaOB.buscarSinFirmaPorVencer(contexto).get();

		if (transferenciasARechazar != null && !transferenciasARechazar.isEmpty()) {
			OBFirmas.rechazarTransferenciasSinFirma(contexto, transferenciasARechazar);
		}
		LogOB.evento(contexto, "FIN BARRIDO TRANSFERENCIAS SIN FIRMAR", LocalDateTime.now().toString(), CRON);

		ServicioFCIOB servicioFCIOB = new ServicioFCIOB(contexto);
		List<FondosComunesOB> fcisARechazar = servicioFCIOB.buscarSinFirmaCompletaPorVencer(contexto).get();

		if (fcisARechazar != null && !fcisARechazar.isEmpty()) {
			OBInversiones.rechazarSinFirmaCompleta(contexto, fcisARechazar);
		}
		LogOB.evento(contexto, "FIN BARRIDO FCIS SIN FIRMAR", LocalDateTime.now().toString(), CRON);

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

	}
}
