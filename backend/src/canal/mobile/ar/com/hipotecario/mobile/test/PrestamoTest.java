package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBPrestamo;

public class PrestamoTest {

	public static void main(String[] args) {
		try {
			detallePrestamoTest();
			// cambioFormaPago();
			// formasPago();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void detallePrestamoTest() {
		try {
			// homo: cobis: 1036007 idPrestamo: 65451997
			String idCobis = "6391862";
			String idPrestamo = "74367762";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idPrestamo", idPrestamo);

			RespuestaMB r = MBPrestamo.detalle(contexto);
			// Respuesta r = ApiPrestamo.consolidada(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private static void cambioFormaPago() {
//		// HOMO
//		String idCobis = "886336";
//		// String idPrestamo = "70914150";
//		String formaPago = "DEBIT";
//		String idCuenta = "403700021339286";
//		String numeroProducto = "0370646730";
//		//
//
//		Contexto contexto = new Contexto(idCobis, "1", "127.0.0.1");
//		// contexto.parametros.set("idPrestamo", idPrestamo);
//		contexto.parametros.set("formaPago", formaPago);
//		contexto.parametros.set("idCuenta", idCuenta);
//		contexto.parametros.set("numeroProducto", numeroProducto);
//		Respuesta r = ApiPrestamo.cambiarFormaPago(contexto);
//		System.out.println(r);
//	}

	protected static void formasPago() {
		String idCobis = "1710089";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB r = MBPrestamo.formasDePago(contexto);
		System.out.println(r);
	}

}
