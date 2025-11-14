package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBTarjetas;

public class TarjetaCreditoTest {

	public static void main(String[] args) {
		try {
			// cambioFormaPagoTC();
			// solicitudEnCursoTC();
			solicitudBajaTC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void cambioFormaPagoTC() {
		String idCobis = "133366";
		String idFormaPago = "1";
		String idCuenta = "404500010244503";

		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idFormaPago", idFormaPago);
		contexto.parametros.set("idCuenta", idCuenta);
		RespuestaMB r = MBTarjetas.cambiarFormaPagoTarjetaCredito(contexto);
		System.out.println(r);
	}

	protected static void solicitudEnCursoTC() {
		String idCobis = "133366";
		String numeroTarjeta = "4304960039740881";

		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("numeroTarjeta", numeroTarjeta);
		RespuestaMB r = MBTarjetas.validarEstadosBajaTarjetaCredito(contexto);
		System.out.println(r);
	}

	protected static void solicitudBajaTC() {
		String idCobis = "133366";
		String numeroTarjeta = "4304960039740881";

		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("numeroTarjeta", numeroTarjeta);
		RespuestaMB r = MBTarjetas.crearCasoBajaTarjetaCredito(contexto);
		System.out.println(r);
	}

}
