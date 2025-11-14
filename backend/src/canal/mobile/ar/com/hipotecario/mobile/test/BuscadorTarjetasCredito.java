package ar.com.hipotecario.mobile.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBTarjetas;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Concurrencia;

public abstract class BuscadorTarjetasCredito {

	public static void main(String[] args) throws InterruptedException {
//		Api.habilitarLog = false;
		ExecutorService executorService = Concurrencia.executorService(15);
		for (String idCobis : lista()) {
			executorService.submit(() -> {
				try {
					ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
					contexto.parametros.set("dummy", true);
//					contexto.parametros.set("idTarjetaCredito", contexto.tarjetaCreditoTitular());
//					ApiTarjetas.consolidadaTarjetas(contexto);
//					ApiTarjetas.consultaAdicionalesPropias(contexto);
					MBTarjetas.resumenCuenta(contexto);
					MBTarjetas.autorizaciones(contexto);
//					ApiTarjetas.cuotasPendientes(contexto);
//					ApiTarjetas.movimientos(contexto);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static List<String> lista() {
		String cobis = Archivo.leer("C:\\Users\\C05302\\Desktop\\cobis_tc.txt");
		List<String> lista = Arrays.asList(cobis.replace("\r", "").split("\\n"));
		return lista;
	}
}
