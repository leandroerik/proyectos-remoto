package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;

public class CronOBRechazarPagosVepSinFirma extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

	private static final String CRON = CronOBRechazarPagosVepSinFirma.class.getSimpleName().toUpperCase();

	public void run() {
		boolean ejecutar = true;

		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		EstadoPagoOB estadoEnBandeja = new ServicioEstadoPagoOB(contexto).find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		List<PagosVepOB> pagosARechazar = new ServicioPagosVepOB(contexto).buscarPorEstado(estadoEnBandeja).get();

		if (pagosARechazar != null && !pagosARechazar.isEmpty()) {
			for (PagosVepOB vep : pagosARechazar) {
				if (vep.fechaVencimiento.isBefore(LocalDate.now())) {
					OBFirmas.rechazarPagoVepSinFirma(contexto, vep);
				}
			}
		}
		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);
	}
}
