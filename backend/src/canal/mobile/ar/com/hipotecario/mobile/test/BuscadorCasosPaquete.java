package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBOriginacion;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;

public abstract class BuscadorCasosPaquete {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(5);
		for (String cobis : lista()) {
			executorService.submit(() -> {
				try {
					String idCobis = cobis;
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
//					contexto.parametros.set("dummy", true);

					contexto.parametros.set("codigoPaquete", "40");
					MBOriginacion.crearPaquete(contexto);
//
//					List<String> lineas = new ArrayList<>();
//					lineas.add(idCobis + " => " + idSolicitud);
//					Files.write(Paths.get("D:\\Users\\C05302\\Desktop\\salida.txt"), lineas, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String cobis = Archivo.leer("D:\\Users\\C05302\\Desktop\\cobis_sin_paquetes.txt");
		List<String> lista = Arrays.asList(cobis.replace("\r", "").split("\\n"));
		return lista;
	}
}
