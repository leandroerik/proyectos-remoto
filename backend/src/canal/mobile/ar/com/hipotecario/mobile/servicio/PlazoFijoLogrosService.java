package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class PlazoFijoLogrosService {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB cabecera(ContextoMB contexto, String idPlanAhorro) {
		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("CabeceraPlazoFijoLogro", "plazosfijos", "GET", "/v1/planAhorro/cabecera", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("CabeceraPlazoFijoLogro", "plazosfijos_windows", "GET", "/v1/planAhorro/cabecera", contexto);
		}
		request.query("operacion", "Q");
		request.query("opcion", "2");
		request.query("codCliente", contexto.idCobis());
		request.cacheSesion = true;
		request.cache204 = true;
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plan_logro_cabecera_secuencial")) {
			request.query("idPlanAhorro", idPlanAhorro);
			return ApiMB.response(request, contexto.idCobis(), idPlanAhorro);
		} else {
			return ApiMB.response(request, contexto.idCobis());
		}
	}

	public static void eliminarCacheCabecera(ContextoMB contexto) {
		ApiMB.eliminarCache(contexto, "CabeceraPlazoFijoLogro", contexto.idCobis());
	}
}
