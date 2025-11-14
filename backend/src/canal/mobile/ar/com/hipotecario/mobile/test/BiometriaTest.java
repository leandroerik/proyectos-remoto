package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBBiometria;

public class BiometriaTest {

	public static void main(String[] args) {
		registroBiometria();
	}

	private static void registroBiometria() {
		try {
			String idCobis = "5322689";
			ContextoMB contexto = new ContextoMB(idCobis, "1", " 0:0:0:0:0:0:0:1");

			contexto.parametros.set("device_name", "Maria iPhone");
			contexto.parametros.set("device_type", "iPhone1");
			contexto.parametros.set("fingerprint_support", true);

			contexto.parametros.set("os_version", "10.0.2");
			contexto.parametros.set("push_token", "OjUEA1NfqUTlz06JPwPP");
			contexto.parametros.set("tenant_id", "");
			contexto.parametros.set("notificar", true);
			MBBiometria apiBiometria = new MBBiometria();
			RespuestaMB r = apiBiometria.accessTokensAutenticador(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
