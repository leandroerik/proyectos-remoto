package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class MBProcrearRefaccion {

	public static ApiResponseMB ofertasProcrear(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("OfertasProcrear", "prestamos", "GET", "/v1/prestamos/{id}/beneficiario", contexto);
		request.path("id", contexto.idCobis());
		request.query("Tipo", "C");
		request.cacheSesion = true;
		request.permitirSinLogin = true;

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		return response;
	}

	public static RespuestaMB postFinalizar(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
//		Solicitud solicitud = Solicitud.solicitudProcrearRefaccion(contexto, contexto.persona().cuit());

		ApiRequestMB request = ApiMB.request("PostFinalizarBPM", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/notificacion/accion", contexto);
		request.path("idSolicitud", idSolicitud);
		request.body("idSolicitud", Integer.valueOf(idSolicitud));
		request.body("idAccion", 1);

		ApiResponseMB response = ApiMB.response(request);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

}
