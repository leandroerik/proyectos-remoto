package ar.com.hipotecario.backend.base;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.canal.homebanking.CanalHomeBanking;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.mobile.CanalMobile;
import ar.com.hipotecario.mobile.ContextoMB;

//import ar.com.hipotecario.canal.homebanking.CanalHomeBanking;
//import ar.com.hipotecario.canal.homebanking.ContextoHB;

public class Futuro<T> {

	/* ========== ATRIBUTOS ESTATICOS ========== */
	private static ExecutorService executor = Executors.newCachedThreadPool();
	public static final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

	/* ========== ATRIBUTOS ========== */
	private Future<T> future;

	/* ========== CONSTRUCTORES ========== */
	public Futuro(Callable<T> funcion) {
		Integer random10digitos = Util.random(1000, 9999);
		threadLocal.set(random10digitos);
		ContextoHB contextoHB = CanalHomeBanking.threadLocal.get();
		ContextoMB contextoMB = CanalMobile.threadLocal.get();
		Callable<T> wrappedFunction = () -> {
			CanalHomeBanking.threadLocal.set(contextoHB);
			CanalMobile.threadLocal.set(contextoMB);
			try {
				return funcion.call();
			} finally {
				CanalHomeBanking.threadLocal.remove();
				CanalMobile.threadLocal.remove();
			}
		};
		this.future = executor.submit(wrappedFunction);
	}

	public Futuro(Boolean condicion, Callable<T> funcion) {
		this.future = condicion ? executor.submit(funcion) : executor.submit(() -> null);
	}

	/* ========== GET ========== */
	public T get() {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			if (t != null && t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			if (t != null) {
				throw new RuntimeException(t);
			}
			throw new RuntimeException(e);
		}
	}

	/* ========== TRY-GET ========== */
	public T tryGet() {
		try {
			return get();
		} catch (Exception e) {
			return null;
		}
	}

	public T tryGet(T defaultValue) {
		try {
			T valor = get();
			if (valor != null) {
				return valor;
			}
			return defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
