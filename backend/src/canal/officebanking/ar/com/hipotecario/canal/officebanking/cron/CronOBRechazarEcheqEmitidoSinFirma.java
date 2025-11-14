package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;

public class CronOBRechazarEcheqEmitidoSinFirma extends Cron.CronJob{
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBRechazarEcheqEmitidoSinFirma.class.getSimpleName().toUpperCase();
    @Override
    public void run() {
        boolean ejecutar = true;
        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }
        LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
        EstadoEcheqOB estadoEnBandeja = servicioEstadoEcheqOB.find(1).get();
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        List<EcheqOB> echeqARechazar = servicioEcheqOB.buscarPorEstado(estadoEnBandeja).get();

        if (!echeqARechazar.isEmpty()){
            echeqARechazar.stream().filter(cheque->cheque.accion.equals(EnumAccionesEcheqOB.EMISION)).forEach(cheque->{
                OBFirmas.rechazarEcheqEmisionSinFirma(contexto,cheque);

            });
        }
        LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);
    }
}
