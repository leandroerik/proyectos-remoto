package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBTransferencias;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import com.google.protobuf.StringValue;

public class CronOBProgramadasTRN extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBProgramadasTRN.class.getSimpleName().toUpperCase();

	public void run() {

		boolean ejecutar = true;
		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		boolean camaraAbierta = Boolean.valueOf(contexto.get("ob_camara_interbanking"));

		LogOB.evento(contexto, "CAMARA_ABIERTA", String.valueOf(camaraAbierta), CRON);

		if (camaraAbierta) {
			ServicioEstadoTRNOB servicioEstadoTransferenciasOB = new ServicioEstadoTRNOB(contexto);
			EstadoTRNOB estadoProgramada = servicioEstadoTransferenciasOB.find(EnumEstadoTRNOB.PROGRAMADA.getCodigo()).get();

			ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
			List<TransferenciaOB> transferencias = servicioTransferenciaOB.buscarPorEstadoYFechaDeAplicacion(estadoProgramada, LocalDate.now()).tryGet();
			LogOB.evento(contexto, "CronOBProgramadasTRN", String.valueOf(transferencias.size()));
			if (transferencias != null && transferencias.size() > 0) {
				OBTransferencias.registrarEnCore(contexto, transferencias);
			}
		}

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

	}
}