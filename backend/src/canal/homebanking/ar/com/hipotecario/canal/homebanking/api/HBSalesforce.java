package ar.com.hipotecario.canal.homebanking.api;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

import java.util.Set;

public class HBSalesforce {
	

	public static Respuesta registrarEventoSalesforce(ContextoHB contexto) {
		boolean isSalesforce = HBSalesforce.prendidoSalesforce(contexto.idCobis());
		String eventDefinitionKey = contexto.parametros.string("EventDefinitionKey");
		Set<String> eventoCarteraGeneral = Objeto.setOf(ConfigHB.string("salesforce_keys_cartera_general").split("_"));

		if (!isSalesforce && !eventoCarteraGeneral.contains(eventDefinitionKey)) {
			return new Respuesta();
		}
		
		if(eventoCarteraGeneral.contains(eventDefinitionKey) && !prendidoSalesforceAmbienteBajoConFF(contexto))
			return new Respuesta();
		
		Objeto data = contexto.parametros.objeto("Data");
		return registrarEventoSalesforce(contexto, eventDefinitionKey, data);
	}

	public static Respuesta registrarEventoSalesforce(ContextoHB contexto, String eventDefinitionKey, Objeto data) {
		if (contexto.idCobis() == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		for(String clave : data.toMap().keySet()) {
			Object valor = data.get(clave);
			if ("IDCOBIS".equals(valor)) {
				data.set(clave, contexto.idCobis());
			}
			if ("ISMOBILE".equals(valor)) {
				data.set(clave, contexto.esMobile());
			}
		}
		data.set("IDCOBIS", contexto.idCobis());

		ApiRequest request = Api.request("RegistrarEventoSalesforce", "notificaciones", "POST", "/v1/marketing/evento", contexto);
		request.body("ContactKey", contexto.idCobis());
		request.body("EventDefinitionKey", eventDefinitionKey);
		request.body("Data", data);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("idProceso", request.idProceso());
		respuesta.set("idEvento", response.string("eventInstanceId"));
		respuesta.set("data", data);
		return respuesta;
	}
	
	public static boolean prendidoSalesforce(String idCobis) {
		return HBAplicacion.funcionalidadPrendida(idCobis, "prendido_salesforce", "prendido_salesforce_cobis");
	}
	
	public static boolean prendidoSalesforceAmbienteBajoConFF(ContextoHB contexto) {
		return contexto.esProduccion() ? contexto.esProduccion() : prendidoSalesforce(contexto.idCobis());
		
	}
}

