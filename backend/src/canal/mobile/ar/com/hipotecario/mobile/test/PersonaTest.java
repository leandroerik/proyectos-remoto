package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;

public class PersonaTest {

	public static void main(String[] args) {
		try {
			logCambioDato();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void logCambioDato() {
		try {
			String idCobis = "4516926";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.insertarContador("CAMBIO_MAIL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
