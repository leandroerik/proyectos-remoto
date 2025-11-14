package ar.com.hipotecario.canal.officebanking.cron;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosCobranzaIntegral;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosCobranzaIntegralOB;

import java.time.LocalDateTime;
import java.util.List;

public class CronOBRechazarCobranzaIntegralSinFirma extends Cron.CronJob {
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBRechazarCobranzaIntegralSinFirma.class.getSimpleName().toUpperCase();

    public void run() {
        boolean ejecutar = true;

        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }
        LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);
        EstadosCobranzaIntegralOB estadoEnBandeja = new ServicioEstadosCobranzaIntegral(contexto).find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        List<CobranzaIntegralOB> cobranzasARechazar = new ServicioCobranzaIntegralOB(contexto).buscarPorEstado(estadoEnBandeja).get();

        if (cobranzasARechazar != null && !cobranzasARechazar.isEmpty())
        {
            for (CobranzaIntegralOB cobranza : cobranzasARechazar){
                OBFirmas.rechazarCobranzaIntegralSinFirma(contexto,cobranza);
            }
        }
        LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);

    }
}
