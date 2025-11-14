package ar.com.hipotecario.canal.buhobank.scoring;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;

public class SOAPConnectionService {

	private static Logger log = LoggerFactory.getLogger(SOAPConnectionService.class);

	public static String PREFIX = "soap";

	public static void createSoapEnvelope(SOAPMessage message, String xmlEntrada) throws Exception {
		SOAPPart part = message.getSOAPPart();

		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();
		SOAPHeader header = envelope.getHeader();

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		envelope.addNamespaceDeclaration(PREFIX, "http://schemas.xmlsoap.org/soap/envelope/");
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
		envelope.setPrefix(PREFIX);
		header.setPrefix(PREFIX);
		body.setPrefix(PREFIX);

		SOAPElement soapEjecutar = body.addNamespaceDeclaration("xmlns", "http://BH/webservices/").addChildElement("EjecutarServicioBlaze");

		SOAPElement soapXmlEntrada = soapEjecutar.addChildElement("xmlEntrada");
		soapXmlEntrada.addTextNode(xmlEntrada);
	}

	public static SOAPMessage createSOAPRequest(String action, String xmlEntrada) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();

		createSoapEnvelope(message, xmlEntrada);

		MimeHeaders headers = message.getMimeHeaders();
		headers.addHeader("SOAPAction", action);

		message.saveChanges();
		System.out.println("Message SOAP:");
		message.writeTo(System.out);
		System.out.println();

		return message;
	}

	public static String callSoapWebService(String url, String action, String xmlEntrada) {
		String result = "";

		try {
			SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = connectionFactory.createConnection();

			SOAPMessage response = connection.call(createSOAPRequest(action, xmlEntrada), url);
			System.out.println("Response SOAP:");
			response.writeTo(System.out);
			System.out.println();

			SOAPBody responseBody = response.getSOAPBody();

			NodeList resultList = responseBody.getElementsByTagName("EjecutarServicioBlazeResult");
			if (resultList != null && resultList.item(0) != null) {
				result = resultList.item(0).getTextContent();
			}

			connection.close();
		} catch (Exception e) {
			SOAPConnectionService.log.error(Fecha.ahora().string("[HH:mm:ss] ") + Texto.stackTrace(e));
		}

		return result;
	}
}
