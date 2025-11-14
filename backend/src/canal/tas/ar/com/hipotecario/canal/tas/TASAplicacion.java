package ar.com.hipotecario.canal.tas;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.tas.servicios.TASRestApiTas;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.servicios.TASSqlKiosco;
import java.util.Map;

public class TASAplicacion {

	public static Object health(ContextoTAS contexto) {
		Objeto response = new Objeto();
		response.set("status_BACUNI", contexto.config.integer("tas_estado_os") != 1 ? false : true );
		response.set("status_SQL", TASSqlKiosco.datosBD(contexto));
		response.set("status_MW", statusAPI(contexto));
		return response;
	}

	private static boolean statusAPI(ContextoTAS contexto){
			Objeto response = TASRestApiTas.healthApiTas(contexto);
			return response.string("status").contains("UP");
	}

	public static Objeto flagBacuni(ContextoTAS contexto){
		try {
			String ipKiosco = contexto.parametros.string("direccionIp");
			if (ipKiosco.isEmpty()) return new Objeto().set("bacuni", false);
			Objeto kiosco = TASSqlKiosco.obtenerKioscoByIp(contexto, ipKiosco);
			if(kiosco.objetos().size() > 1)	return new Objeto().set("bacuni", false);
			int estadoBacuni = kiosco.objetos().get(0).integer("bac_uni", 0);
			return new Objeto().set("bacuni", estadoBacuni);
		}catch (Exception e){
			return new Objeto().set("bacuni", false);
		}
	}

	public static Objeto dataEnContexto(ContextoTAS contexto) {
		try {
			Objeto response = new Objeto();
			Map<Integer, TASKiosco> kioscos = contexto.getKioscos();
			Objeto kioscosSesion = new Objeto();
			if (!kioscos.isEmpty()) {
				for (TASKiosco kiosco : kioscos.values()) {
					kioscosSesion.add(TASKiosco.armarModeloObjeto(kiosco));
				}
				response.set("kioscos_sesion", kioscosSesion);
			} else {
				response.set("kioscos_sesion","no hay datos");
			}
			if (contexto.sesion().clienteTAS != null) {
				response.set("Cliente", contexto.sesion().clienteTAS.getIdCliente());
				response.set("subcanal_sesion", contexto.subCanal());
				response.set("fecha_login", contexto.sesion().fechaLogin);
			}
			return response;
		} catch (Exception e){
			return RespuestaTAS.error(contexto, "TASAplicacion - dataEnContexto()", e);
		}
	}
}
