package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBCuenta;
import ar.com.hipotecario.mobile.api.MBOmnicanalidad;
import ar.com.hipotecario.mobile.api.MBTarjetas;

public class TestTdVirutal {
	public static void main(String[] args) {
		try {
			// consolidadaTarjetasFullTest();
			// crearTDVirtual();
			// convertirVirutalToFisica();
			validarCuentaAsociadaTDVirtual();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void consolidadaTarjetasFullTest() {
		try {
			// String idCobis = "3636393";
			String idCobis = "6212286";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			RespuestaMB r = MBTarjetas.consolidadaTarjetasFull(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void crearTDVirtual() {
		try {
			String idCobis = "1430015";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idMoneda", "80");
			RespuestaMB solicitud = MBOmnicanalidad.crearSolicitudCajaAhorro(contexto);
			System.out.println(solicitud);
			String idSol = (String) solicitud.get("idSolicitud");

			contexto.parametros.set("idSolicitud", idSol);
			contexto.parametros.set("idTarjetaDebito", 0);
			contexto.parametros.set("virtual", true);
			RespuestaMB actualizar = MBOmnicanalidad.actualizarSolicitudCajaAhorro(contexto);
			System.out.println(actualizar);

			RespuestaMB finalizar = MBOmnicanalidad.finalizarSolicitudVenta(contexto);
			System.out.println(finalizar);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void convertirVirutalToFisica() {
		try {
			String idCobis = "831986";
			String idTarjetaDebito = "4998590027970504";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idTarjeta", idTarjetaDebito);
			RespuestaMB r = MBTarjetas.convertirTarjetaDebitoVirtualToFisica(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void validarCuentaAsociadaTDVirtual() {
		try {
			String idCobis = "133366";
			String numeroCuenta = "404500308269517";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("numero", numeroCuenta);
			RespuestaMB r = MBCuenta.validarCuentaAsociadaVirtual(contexto);
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
