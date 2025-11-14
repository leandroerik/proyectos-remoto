package ar.com.hipotecario.mobile.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBPago;
import ar.com.hipotecario.mobile.lib.Concurrencia;

public abstract class EstresLink {

	public static void main(String[] args) throws InterruptedException {
		ContextoMB contexto2 = new ContextoMB("6227014", "1", "127.0.0.1");
		contexto2.parametros.set("buscarPagables", true);
		contexto2.parametros.set("dummy", true);
		MBPago.consolidadaPagos(contexto2);

		ExecutorService executorService = Concurrencia.executorService(100);
		for (Integer i = 0; i < 2000; ++i) {
			for (String idCobis : lista()) {
				executorService.submit(() -> {
					try {
						ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
						contexto.parametros.set("buscarPagables", true);
						contexto.parametros.set("dummy", true);
						MBPago.consolidadaPagos(contexto);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		List<String> lista = new ArrayList<>();
		lista.add("6227014");
//		lista.add("6327249");
		return lista;
	}
}
