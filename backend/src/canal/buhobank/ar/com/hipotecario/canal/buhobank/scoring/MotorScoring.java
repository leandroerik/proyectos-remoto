package ar.com.hipotecario.canal.buhobank.scoring;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.util.HTMLEncoder;

public class MotorScoring extends ApiObjeto {

	public static RespuestaMotor ejecutarServicioBlaze(Contexto contexto, SolicitudMotor entrada) {
		RespuestaMotor respuestaMotor = null;
		Config config = contexto.config;

		String motorUri = config.string("backend_scoring");
		String blazeAction = config.string("backend_scoring_servicio_blaze");

		try {
			entrada.header.startTime = Fecha.ahora().string("yyyy-MM-dd'T'HH:mm:ss", null);
			entrada.header.requestSource = config.string("backend_scoring_source");
			entrada.header.component = config.string("backend_scoring_componente");
			entrada.header.action = "InvocarMotor";
			entrada.header.dominio = config.string("backend_scoring_dominio");
			entrada.header.user = config.string("backend_scoring_user");
			entrada.header.password = config.string("backend_scoring_pass");
			entrada.header.perfil = config.string("backend_scoring_perfil");
			entrada.header.userCOBIS = config.string("backend_scoring_usuario_cobis");

			String xmlEntrada = JAXBContextUtils.jaxbObjectToXML(entrada);
			xmlEntrada = HTMLEncoder.decodeHTML(xmlEntrada);

			String response = SOAPConnectionService.callSoapWebService(motorUri, blazeAction, xmlEntrada);
			if (Util.empty(response)) {
				System.out.println("Response motor nula");
				return null;
			}

			respuestaMotor = JAXBContextUtils.jaxbXMLToObject(response, RespuestaMotor.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return respuestaMotor;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		SolicitudMotor solicitud = new SolicitudMotor();
		System.out.println("ejecutarServicioBlaze");
		ejecutarServicioBlaze(contexto, solicitud);
	}
}
