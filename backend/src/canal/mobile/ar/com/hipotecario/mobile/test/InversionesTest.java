package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBInversion;

public class InversionesTest {

	public static void main(String[] args) {
		try {
			fondos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fondos() {
		String cobis = "4373070";
		Integer idcuotapartista = 8473;
		Integer idtipoValorCuotaParte = 0;
		String tipoSolicitud = "SU";
		String fciUpdated = "ok";
		try {
			ContextoMB contexto = new ContextoMB(cobis, "1", "127.0.0.1");
			contexto.parametros.set("idcuotapartista", idcuotapartista);
			contexto.parametros.set("idtipoValorCuotaParte", idtipoValorCuotaParte);
			contexto.parametros.set("tipoSolicitud", tipoSolicitud);
			contexto.parametros.set("fciUpdated", fciUpdated);

			RespuestaMB respuesta = MBInversion.fondos(contexto);
			System.out.println(respuesta);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
