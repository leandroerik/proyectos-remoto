package ar.com.hipotecario.canal.homebanking.base;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * https://www.sauronsoftware.it/projects/cron4j/manual.php
 */
public class CronLocal {

	public static abstract class CronJobLocal implements Runnable {
	}

	public static void registrar(String cron, CronJobLocal cronJob) {
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(cron, runnable(cronJob));
		scheduler.start();
	}

	private static Runnable runnable(CronJobLocal runnable) {
		return new Runnable() {
			public void run() {
				runnable.run();
			}
		};
	}
}
