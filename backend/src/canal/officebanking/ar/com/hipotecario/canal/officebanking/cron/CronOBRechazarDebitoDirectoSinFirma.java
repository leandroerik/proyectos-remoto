package ar.com.hipotecario.canal.officebanking.cron;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ServicioOrdenPagoFechaEjecucionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.OrdenPagoFechaEjecucionOB;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CronOBRechazarDebitoDirectoSinFirma extends CronJob {

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBRechazarDebitoDirectoSinFirma.class.getSimpleName().toUpperCase();

    public void run() {
        boolean ejecutar = true;

        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }
        LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);
        EstadosDebitoDirectoOB estadoEnBandeja = new ServicioEstadosDebitoDirectoOB(contexto).find(EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()).get();
        List<DebitoDirectoOB> debitosARechazar = new ServicioDebitoDirectoOB(contexto).buscarPorEstado(estadoEnBandeja).get();

        if (debitosARechazar != null && !debitosARechazar.isEmpty())
        {
            for (DebitoDirectoOB debito : debitosARechazar){
                    OBFirmas.rechazarDDSinFirma(contexto,debito);
            }
        }
        LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

    }
}
