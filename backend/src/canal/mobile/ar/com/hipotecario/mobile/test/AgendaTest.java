package ar.com.hipotecario.mobile.test;

import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBAgenda;
import ar.com.hipotecario.mobile.lib.Objeto;

public class AgendaTest {

	public static void main(String[] args) {
		try {
			tiposTurno();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void tiposTurno() {
		String idCobis = "3636393";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("cuil", contexto.persona().cuit());
		RespuestaMB respuesta = new RespuestaMB();
		respuesta = MBAgenda.tipoTurnosHabilitados(contexto);
		List<Objeto> objList = (List<Objeto>) respuesta.get("datos");
		for (Objeto obj : objList) {
			System.out.println(obj.get("descTipoTurno"));
		}
		System.out.println("**************************");
		System.out.println(respuesta);
		// System.out.println("estado: " + respuesta.get("estado"));
		// System.out.println("datos: " + respuesta.get("datos"));
	}

}
