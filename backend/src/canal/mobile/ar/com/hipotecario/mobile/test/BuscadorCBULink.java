package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;

public abstract class BuscadorCBULink {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(1);
		for (String dato : lista()) {
			executorService.submit(() -> {
				try {
					String cbu = dato.trim();
					ContextoMB contexto = new ContextoMB("135706", "1", "127.0.0.1");
					String idMoneda = "2";

					ApiRequestMB request = ApiMB.request("CuentaLink", "cuentas", "GET", "/v1/cuentas", contexto);
					request.query("idcliente", contexto.idCobis());
					request.query("cbu", cbu);
					request.query("numerotarjeta", "4998590015391523");
					request.query("idmoneda", idMoneda);
					request.query("consultaalias", "false");
					request.dummy = true;

//					ApiResponse response = Api.response(request, cbu, idMoneda);

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String contenido = Archivo.leer("C:\\Users\\C05302\\Desktop\\cbu_coelsa.txt");
		List<String> lista = Arrays.asList(contenido.split("\\n"));
		return lista;
	}
}
