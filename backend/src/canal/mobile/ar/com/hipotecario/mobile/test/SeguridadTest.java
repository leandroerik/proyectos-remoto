package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBLogin;
import ar.com.hipotecario.mobile.api.MBSeguridad;

public class SeguridadTest {

	public static void main(String[] args) {
		try {
			canalesOtp();
			// pedirOTP();
			// pedirPreguntas();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void canalesOtp() {
		try {
			String idCobis = "4900353";
//			String cbu = "400000016248447";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("funcionalidad", "datos-personales");
			// contexto.parametros.set("cbu", cbu);
			RespuestaMB r = MBSeguridad.canalesOTPorUsuario(contexto);

			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void pedirOTP() {
		try {
			String idCobis = "4900353";
			String motivo = "es una prueba";
			String tipoMotivo = "transferencia";

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idCanal", "SMS_E");
			contexto.parametros.set("tipoMotivo", tipoMotivo);
			contexto.parametros.set("motivo", motivo);

			RespuestaMB r = MBSeguridad.pedirOTP(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void pedirPreguntas() {
		String idCobis = "8051418";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB p = MBLogin.preguntasRiesgoNet(contexto);
		System.out.println(p);
	}

}
