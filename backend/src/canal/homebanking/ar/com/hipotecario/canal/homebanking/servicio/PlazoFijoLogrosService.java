package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class PlazoFijoLogrosService {

	/* ========== SERVICIOS ========== */
	public static ApiResponse cabecera(ContextoHB contexto, String idPlanAhorro) {
		ApiRequest request = null;
		request = Api.request("CabeceraPlazoFijoLogro", "plazosfijos", "GET", "/v1/planAhorro/cabecera", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.query("operacion", "Q");
		request.query("opcion", "2");
		request.query("codCliente", contexto.idCobis());
		request.cacheSesion = true;
		request.cache204 = true;
		request.query("idPlanAhorro", idPlanAhorro);
		return Api.response(request, contexto.idCobis(), idPlanAhorro);
	}
}
