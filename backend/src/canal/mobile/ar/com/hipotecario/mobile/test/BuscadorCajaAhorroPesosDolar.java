package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.negocio.Cuenta;

public abstract class BuscadorCajaAhorroPesosDolar {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Concurrencia.executorService(5);
		for (String cobis : lista()) {
			executorService.submit(() -> {
				try {
					String idCobis = cobis;
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
//					contexto.parametros.set("dummy", true);
					boolean encontroPesos = false;
					boolean encontroDolares = false;

					List<Cuenta> cuentas = contexto.cuentas();
					for (Cuenta cuenta : cuentas) {
						if (cuenta.esPesos()) {
							encontroPesos = true;
						}
						if (cuenta.esDolares()) {
							encontroDolares = true;
						}
					}

					if (encontroPesos && encontroDolares) {
						String linea = "encontro caso " + idCobis;
						System.out.println(linea);

					}
					// contexto.parametros.set("codigoPaquete", "40");
					// ApiOriginacion.crearPaquete(contexto);
//
//					List<String> lineas = new ArrayList<>();
//					lineas.add(idCobis + " => " + idSolicitud);
//					Files.write(Paths.get("D:\\Users\\C05302\\Desktop\\salida.txt"), lineas, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);

				} catch (Exception e) {
					// e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String cobis = Archivo.leer("C:\\prueba\\cobis_extranjeros_desa.txt");
		List<String> lista = Arrays.asList(cobis.replace("\r", "").split("\\n"));
		return lista;
	}
}
