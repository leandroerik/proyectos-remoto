package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBCatalogo;
import ar.com.hipotecario.mobile.lib.Objeto;

public class CatalogoTest {

	public static void main(String[] args) {
		try {
			// sucursalesProvincia();
			// sucursalesTipoTurno();
			// motivos();
			formaPagoTC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void sucursalesProvincia() {
		String idCobis = "3636393";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idProvincia", 1);
		RespuestaMB respuesta = new RespuestaMB();
		respuesta = MBCatalogo.sucursalesPorProvincia(contexto);
		System.out.println("**************************");
		System.out.println(respuesta);
	}

	protected static void sucursalesTipoTurno() {
		String idCobis = "3636393";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idProvincia", 4);
		contexto.parametros.set("idTurno", 36);
		RespuestaMB respuesta = new RespuestaMB();
		respuesta = MBCatalogo.sucursalesPorTipoTurno(contexto);
		System.out.println("**************************");
		System.out.println(respuesta);
	}

	protected static void motivos() {
		String idCobis = "3636393";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB respuesta = new RespuestaMB();
		respuesta = MBCatalogo.motivos(contexto);
		System.out.println("**************************");
		System.out.println(respuesta);
	}

	private static void formaPagoTC() {
		String idCobis = "3636393";
		String idFormaPago = "04";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB respuesta = new RespuestaMB();
		respuesta = MBCatalogo.formaPagoTC(contexto);
		// System.out.println("**************************");
		// System.out.println(respuesta.get("formasPago"));
		Objeto formaPago = new Objeto();
		for (Objeto obj : respuesta.objetos("formasPago")) {
			if (idFormaPago.equals(obj.get("pagoFormaCodi").toString().trim())) {
				formaPago = obj;
				break;
			}
		}
		System.out.println(formaPago);

	}

}
