package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron;

public class CronPrismaNotificacionesNFC extends Cron.CronJob {
  private static final ContextoBB contexto = new ContextoBB(GeneralBB.CANAL_CODIGO, Config.ambiente(), "1");

  public void run() {
    BBAlta.configurarPrismaNotificacionesNFC(contexto);
  }
}
