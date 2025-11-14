package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBArchivo;

public class ArchivoTest {

	public static void main(String[] args) {
		try {
			getArchivo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getArchivo() {
		try {
			String idCobis = "133366";
			String idDocumento = "0A4130D8-AB4D-CEDE-889F-773EB8F00000";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idDoc", idDocumento);
			RespuestaMB r = MBArchivo.descargaAdjunto(contexto);
			System.out.println("########################");
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
