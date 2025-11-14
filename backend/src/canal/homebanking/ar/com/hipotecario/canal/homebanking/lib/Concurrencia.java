package ar.com.hipotecario.canal.homebanking.lib;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;

public abstract class Concurrencia {

	public static ExecutorService executorService(Object... parametros) {
		Integer cantidadHilos = 0;
		for (Object parametro : parametros) {
			if (parametro instanceof Boolean) {
				cantidadHilos += (Boolean) parametro ? 1 : 0;
			}
			if (parametro instanceof Integer) {
				cantidadHilos += (Integer) parametro;
			}
			if (parametro instanceof Collection<?>) {
				cantidadHilos += ((Collection<?>) parametro).size();
			}
		}
		Integer maximaCantidadHilos = ConfigHB.integer("servidor_maxima_cantidad_hilos_por_proceso", 50);
		cantidadHilos = cantidadHilos != 0 ? cantidadHilos : 1;
		cantidadHilos = cantidadHilos < maximaCantidadHilos ? cantidadHilos : maximaCantidadHilos;
		return Executors.newFixedThreadPool(cantidadHilos);
	}

	public static Boolean esperar(ExecutorService executorService, Respuesta respuesta) {
		return esperar(executorService, respuesta, 30);
	}

	public static Boolean esperar(ExecutorService executorService, Respuesta respuesta, Integer timeout) {
		try {
			executorService.shutdown();
			if (!executorService.awaitTermination(ConfigHB.integer("servidor_timeout_threads", timeout), TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
			return true;
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			if (respuesta != null) {
				respuesta.setEstadoExistenErrores();
			}
			return false;
		}
	}
}
