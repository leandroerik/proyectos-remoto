package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.servicio.CuentasService;

public abstract class BuscadorDebin {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(20);
		for (String cobis : lista()) {
			executorService.submit(() -> {
				try {
					String idCobis = cobis.trim();
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
					contexto.parametros.set("dummy", true);
					for (Cuenta cuenta : contexto.cuentas()) {
						CuentasService.cuentaCoelsa(contexto, cuenta.cbu());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String tarjetas = Archivo.leer("C:\\Users\\C05302\\Desktop\\cobis.txt");
		List<String> lista = Arrays.asList(tarjetas.split("\\n"));
		return lista;
	}
}
