package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBLogin;

public class TestBiometria {

	public static void main(String[] args) {
		try {
			loginBio();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loginBio() {
		try {

			String idCobis = "6689032";
			String documento = "34541472";
			String metodo = "rostro";
			String dispositivo = "uuid906e3684-09e2-45a6-baa5-78a32100406d";
			String push_token = "1bb6d516e387ec5aefa6ba1ddf22d2013ab434aa1a893e1727e6b6e1aac6317a";

			ContextoMB contexto = new ContextoMB(idCobis, "1234567890", "127.0.0.1");
			contexto.parametros.set("documento", documento);
			contexto.parametros.set("metodo", metodo);
			contexto.parametros.set("dispositivo", dispositivo);
			contexto.parametros.set("push_token", push_token);

			RespuestaMB r = MBLogin.biometria(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
