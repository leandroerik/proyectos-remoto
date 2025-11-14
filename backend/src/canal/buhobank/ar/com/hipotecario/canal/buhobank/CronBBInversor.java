package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;

public class CronBBInversor extends CronJob {
	private static ContextoBB contexto = new ContextoBB(GeneralBB.CANAL_CODIGO, Config.ambiente(), "1");

	public void run() {
		BBInversor.ejecutarBBInversor(contexto);
	}
}
