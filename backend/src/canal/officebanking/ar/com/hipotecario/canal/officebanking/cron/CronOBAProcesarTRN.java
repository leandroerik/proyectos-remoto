package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBTransferencias;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.PaginaTransferenciaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;

public class CronOBAProcesarTRN extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBAProcesarTRN.class.getSimpleName().toUpperCase();

	public void run() {

		boolean ejecutar = true;
		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		boolean camaraAbierta = Boolean.valueOf(contexto.get("ob_camara_interbanking"));
		int trnPorEjecucion = contexto.config.integer("ob_cron_trn_por_ejecucion", 500);

		LogOB.evento(contexto, "CronOBAProcesarTRN", "CAMARA_ABIERTA: "+String.valueOf(camaraAbierta), CRON);

		if (camaraAbierta) {
			EstadoTRNOB estadoAProcesar = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.A_PROCESAR.getCodigo()).get();
			ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
			PaginaTransferenciaDTO paginadoTRN = servicioTransferenciaOB.find(null, 1, trnPorEjecucion, null, estadoAProcesar.id, Fecha.ahora(), Fecha.ahora()).tryGet();
			LogOB.evento(contexto, "CronOBAProcesarTRN", String.valueOf(paginadoTRN.cantidad));
			if (paginadoTRN != null && paginadoTRN.cantidad > 0) {
				OBTransferencias.registrarEnCore(contexto, paginadoTRN.transferencias);
			}
		}

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

	}
}