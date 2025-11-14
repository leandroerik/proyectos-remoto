package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class BackofficevuBB extends ApiObjeto {

	private static String urlBase;
	private static String privateKey;
	private static String username;
	private static Integer operationId;
	private static String operationGuid;

	private static Objeto postRequestVU(ContextoBB contexto, String servicio, Boolean anonimizar) {

		try {

			if(!anonimizar){
				servicio = "onboarding" + servicio;
			}
			HttpRequest request = new HttpRequest("POST", urlBase + servicio);
			request.header("x-access-apikey-private", privateKey);
			request.body("userName", username);
			request.body("operationId", operationId);
			request.body("operationGuid", operationGuid);
			if(!anonimizar){
				request.body("includeIDPhotos", true);
				request.body("includeSelfies", true);
				request.body("document", true);
				request.body("selfies", true);
			}

			HttpResponse response = request.run();
			return response.jsonBody();
		} catch (Exception e) {
			LogBB.evento(contexto, GeneralBB.ERROR_GET_INFORMATION_VU, operationId + "|" + operationGuid + "|" + "servicio");
			return null;
		}

	}

	public static Objeto obtenerDatosVU(ContextoBB contexto, String urlBase, String privateKey, String cuil, Integer operationId, String operationGuid) {

		BackofficevuBB.urlBase = urlBase;
		BackofficevuBB.privateKey = privateKey;
		BackofficevuBB.username = cuil;
		BackofficevuBB.operationId = operationId;
		BackofficevuBB.operationGuid = operationGuid;

		Objeto resStatus = postRequestVU(contexto, "/statusOperation", false);

		try {
			if (resStatus != null && resStatus.integer("operationStatusId") == 1) {
				postRequestVU(contexto, "/cancelOperation", false);
			}
		} catch (Exception e) {

		}

		return postRequestVU(contexto, "/getOperationInformation", false);
	}

	public static Objeto anonimizarDatosVU(ContextoBB contexto, String urlBase, String privateKey, String cuil, Integer operationId, String operationGuid) {

		BackofficevuBB.urlBase = urlBase;
		BackofficevuBB.privateKey = privateKey;
		BackofficevuBB.username = cuil;
		BackofficevuBB.operationId = operationId;
		BackofficevuBB.operationGuid = operationGuid;

		try {

			Objeto resStatus;
			resStatus = postRequestVU(contexto, "configuration/usermanagement/anonymizeOperationById", true);
			return resStatus;

		} catch (Exception e) {

		}
		return null;
	}

	private static Objeto postRequestAnonimizarVU(ContextoBB contexto, String servicio) {

		try {
			HttpRequest request = new HttpRequest("POST", urlBase + servicio);
			request.header("x-access-apikey-private", privateKey);
			request.body("userName", username);
			request.body("operationId", operationId);
			request.body("operationGuid", operationGuid);

			HttpResponse response = request.run();
			return response.jsonBody();
		} catch (Exception e) {
			LogBB.evento(contexto, GeneralBB.ERROR_GET_INFORMATION_VU, operationId + "|" + operationGuid + "|" + "servicio");
			return null;
		}
	}

}

