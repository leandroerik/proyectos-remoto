package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;

public class CronOBRechazarEcheqDescuentoSinFirma extends CronJob {
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBRechazarEcheqDescuentoSinFirma.class.getSimpleName().toUpperCase();
    //@Override
    public void run() {
        boolean ejecutar = contexto.config.bool("ob_cron_rechazar_echeq_descuento_sin_firma_ejecutar",true);
        try {
	        if (!ejecutar) {
	            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
	            return;
	        }
	        LogOB.evento(contexto, "INICIO CRON RECHAZO-BF", LocalDateTime.now().toString(), CRON);
	        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
	        EstadoEcheqOB estadoEnBandeja = servicioEstadoEcheqOB.find(1).get();
	        ServicioEcheqDescuentoOB servicioEcheqDescuentoOB = new ServicioEcheqDescuentoOB(contexto);
	        List<EcheqDescuentoOB> echeqARechazar = servicioEcheqDescuentoOB.buscarPorEstado(estadoEnBandeja).get();

	        if (!echeqARechazar.isEmpty()){
	            echeqARechazar.stream().forEach(cheque->{
	                OBFirmas.rechazarEcheqDescuentoSinFirma(contexto,cheque);	
	            });
	        }       
        } catch (Exception e) {
            LogOB.evento(contexto, "CATCH CRON RECHAZO-BF", new Objeto().set("Exception", e.getMessage()).toString(), CRON);
        }
        LogOB.evento(contexto, "FIN CRON RECHAZO-BF", LocalDateTime.now().toString(), CRON);
    }
}
