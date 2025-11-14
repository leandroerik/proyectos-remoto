package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.EnumEstadoPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ServicioOrdenPagoFechaEjecucionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.OrdenPagoFechaEjecucionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;

public class CronOBRechazarPagosMasivosSinFirma extends CronJob {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	
	private static final String CRON = CronOBRechazarPagosMasivosSinFirma.class.getSimpleName().toUpperCase();
	
	public void run() {
        boolean ejecutar = true;

        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }

        LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

        EstadosPagosAProveedoresOB estadoEnBandeja = new ServicioEstadosPagoAProveedoresOB(contexto).find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get();
        ServicioOrdenPagoFechaEjecucionOB servicioOPFechaEjecucion = new ServicioOrdenPagoFechaEjecucionOB(contexto);
        List<PagoAProveedoresOB> pagosARechazar = new ServicioPagoAProveedoresOB(contexto).buscarPorEstado(estadoEnBandeja).get();

        if (pagosARechazar != null && !pagosARechazar.isEmpty()) {
            for (PagoAProveedoresOB pap : pagosARechazar) {
            	OrdenPagoFechaEjecucionOB od = servicioOPFechaEjecucion.findById(contexto, pap.id).get();
            	if(od.fechaEjecucion.isBefore(LocalDate.now())) {
            		OBFirmas.rechazarPapSinFirma(contexto, pap);
            	}
            }
        }
        LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);
    }
}
