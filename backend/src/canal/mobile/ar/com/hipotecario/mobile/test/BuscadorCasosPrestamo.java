package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBPrestamo;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Objeto;

public abstract class BuscadorCasosPrestamo {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(50);
		for (String cobis : lista()) {
			executorService.submit(() -> {
				try {
					String idCobis = cobis.trim();
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
					RespuestaMB consolidada = MBPrestamo.consolidada(contexto);
					for (Objeto item : consolidada.objetos("hipotecarios")) {
						String id = item.string("id");
						Boolean pagable = item.bool("pagable");
						if (pagable && contexto.cuentaPorDefecto() != null) {
							contexto.parametros.set("idPrestamo", id);
							contexto.parametros.set("idCuenta", contexto.cuentaPorDefecto().id());
							contexto.parametros.set("importe", 10);
							MBPrestamo.pagar(contexto);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String cobis = Archivo.leer("C:\\Users\\C05302\\Desktop\\cobis.txt");
		List<String> lista = Arrays.asList(cobis.replace("\r", "").split("\\n"));
		return lista;
	}
}
