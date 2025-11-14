package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBTarjetas;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public abstract class BuscadorTCAdicional {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(5);
		for (String cobis : lista()) {
			executorService.submit(() -> {
				try {
					String idCobis = cobis;
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
					List<String> lista = RestPersona.cuitsRelacionados(contexto);
					for (String cuit : lista) {
						contexto.parametros.set("cuit", cuit);
						contexto.parametros.set("porcentaje", "15");
						MBTarjetas.crearSolicitudTarjetaCreditoAdicional(contexto);
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
