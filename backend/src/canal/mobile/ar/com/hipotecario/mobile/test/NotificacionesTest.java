package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBNotificaciones;
import ar.com.hipotecario.mobile.api.MBSeguridad;

public class NotificacionesTest {

	public static void main(String[] args) {
		try {
			// sendSmsOTPTest();
			// sendSmsSeguridadTest();
			notificacionesDescarga();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void sendSmsOTPTest() {
		try {
			String otp = "12345678";
			String mensaje = "Nunca compartas esta clave. Es confidencial y de uso personal exclusivo. Tu codigo de seguridad Banco Hipotecario es " + otp;
			String motivo = "Para transferir a CBU ***XXX por 0.000.001,00 pesos";
			String tipoMotivo = "transferencia";

			ContextoMB contexto = new ContextoMB("123456", "1", "127.0.0.1");
			contexto.parametros.set("telefono", "1164318977");
			contexto.parametros.set("codigo", otp);
			contexto.parametros.set("mensaje", mensaje);
			contexto.parametros.set("tipoMotivo", tipoMotivo);
			contexto.parametros.set("motivo", motivo);

			RespuestaMB r = MBNotificaciones.sendSmsOTP(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static void sendSmsSeguridadTest() {
		try {
			String otp = "123456";
			String mensaje = "test: ";
			String motivo = "es una prueba";
			String tipoMotivo = "transferencia";

			ContextoMB contexto = new ContextoMB("123456", "1", "127.0.0.1");
			contexto.parametros.set("idCanal", "SMS_E");
			contexto.parametros.set("codigo", otp);
			contexto.parametros.set("mensaje", mensaje);
			contexto.parametros.set("tipoMotivo", tipoMotivo);
			contexto.parametros.set("motivo", motivo);

			RespuestaMB r = MBSeguridad.pedirOTP(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void notificacionesDescarga() {
		try {
			String idCobis = "133366";// "133366";//"6991029";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			RespuestaMB r = MBNotificaciones.notificationesDescarga(contexto);
			System.out.println("########################");
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
