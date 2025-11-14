package ar.com.hipotecario.mobile.api;

import java.util.Set;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class MBSalesforce {

	public static RespuestaMB registrarEventoSalesforce(ContextoMB contexto) {
		
		
		boolean isSalesforce = MBSalesforce.prendidoSalesforce(contexto.idCobis());
		String eventDefinitionKey = contexto.parametros.string("EventDefinitionKey");
		Set<String> eventoCarteraGeneral = Objeto.setOf(ConfigMB.string("salesforce_keys_cartera_general").split("_"));

		if (!isSalesforce && !eventoCarteraGeneral.contains(eventDefinitionKey)) 
			return new RespuestaMB();
		
		Objeto data = contexto.parametros.objeto("Data");
		return registrarEventoSalesforce(contexto, eventDefinitionKey, data);
	}

	public static RespuestaMB registrarEventoSalesforce(ContextoMB contexto, String eventDefinitionKey, Objeto data) {
		if (contexto.idCobis() == null) {
			return RespuestaMB.estado("SIN_PSEUDO_SESION");
		}

		for(String clave : data.toMap().keySet()) {
			Object valor = data.get(clave);
			if ("IDCOBIS".equals(valor)) {
				data.set(clave, contexto.idCobis());
			}
			if ("ISMOBILE".equals(valor)) {
				data.set(clave, true);
			}
		}
		
		if(!data.existe("IDCOBIS"))
			data.set("IDCOBIS", contexto.idCobis());
		
		ApiRequestMB request = ApiMB.request("RegistrarEventoSalesforce", "notificaciones", "POST", "/v1/marketing/evento", contexto);
		request.body("ContactKey", contexto.idCobis());
		request.body("EventDefinitionKey", eventDefinitionKey);
		request.body("Data", data);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("idProceso", request.idProceso());
		respuesta.set("idEvento", response.string("eventInstanceId"));
		respuesta.set("data", data);
		return respuesta;
	}
	
	public static boolean prendidoSalesforce(String idCobis) {
		return MBAplicacion.funcionalidadPrendida(idCobis, "prendido_salesforce", "prendido_salesforce_cobis");
	}
	
	public static boolean prendidoSalesforceAmbienteBajoConFF(ContextoMB contexto) {
		return contexto.esProduccion() ? contexto.esProduccion() : prendidoSalesforce(contexto.idCobis());
	}
}
