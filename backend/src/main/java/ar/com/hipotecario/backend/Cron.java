package ar.com.hipotecario.backend;

import java.util.UUID;

import ar.com.hipotecario.backend.base.Util;
import it.sauronsoftware.cron4j.Scheduler;

/**
 * https://www.sauronsoftware.it/projects/cron4j/manual.php
 */
public class Cron {

	public static abstract class CronJob implements Runnable {
	}

	public static void registrar(String cron, CronJob cronJob) {
		Scheduler scheduler = new Scheduler();
		scheduler.schedule(cron, runnable(cronJob));
		scheduler.start();
	}

	private static Runnable runnable(CronJob runnable) {
		return new Runnable() {
			public void run() {
				if (podPrincipal(runnable)) {
					runnable.run();
				}
			}
		};
	}

	private static Boolean podPrincipal(CronJob runnable) {
		Contexto contexto = new Contexto();
		if (contexto.usarRedis()) {
			Integer segundosEsperaOtrosPods = 3;
			Integer segundosExpiracionRedis = 30;

			String nombre = runnable.getClass().getCanonicalName();
			String uuid = UUID.randomUUID().toString();
			Integer random = Util.random(1, 100);
			String clave = nombre + "_" + random;
			contexto.set(clave, uuid, segundosExpiracionRedis);

			try {
				Thread.sleep(segundosEsperaOtrosPods * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String uuidPodPrincipal = null;
			for (Integer i = 1; i < 100; ++i) {
				String subclave = nombre + "_" + i;
				uuidPodPrincipal = contexto.get(subclave);
				if (uuidPodPrincipal != null) {
					break;
				}
			}

			Boolean podPrincipal = uuid.equals(uuidPodPrincipal);
			return podPrincipal;
		}
		return true;
	}
}
