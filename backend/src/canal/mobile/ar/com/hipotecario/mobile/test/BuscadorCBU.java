package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.servicio.CuentasService;

public abstract class BuscadorCBU {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(15);
		for (String dato : lista()) {
			executorService.submit(() -> {
				try {
					String cbu = dato.trim();
					ContextoMB contexto = new ContextoMB("135706", "1", "127.0.0.1");
					CuentasService.cuentaCoelsa(contexto, cbu);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String contenido = Archivo.leer("C:\\Users\\C05302\\Desktop\\cbu_otros_bancos.txt");
		List<String> lista = Arrays.asList(contenido.split("\\n"));
		return lista;
	}
}
