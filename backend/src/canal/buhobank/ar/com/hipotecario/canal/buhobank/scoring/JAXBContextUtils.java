package ar.com.hipotecario.canal.buhobank.scoring;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Document;

public class JAXBContextUtils {

	private final static Map<Class<?>, JAXBContext> contextStore = new ConcurrentHashMap<Class<?>, JAXBContext>();

	public static JAXBContext getContext(Class<?> c) throws JAXBException {
		JAXBContext context = contextStore.get(c);
		if (context == null) {
			context = JAXBContext.newInstance(c);
			contextStore.put(c, context);
		}

		return context;
	}

	public static String jaxbObjectToXML(SolicitudMotor solicitud) {
		String xmlString = "";

		try {
			JAXBContext jaxbContext = getContext(SolicitudMotor.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(solicitud, sw);

			xmlString = sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return xmlString;
	}

	@SuppressWarnings("unchecked")
	public static <T> T jaxbXMLToObject(String xml, Class<T> clase) {
		T obj = null;

		try {
			JAXBContext jaxbContext = getContext(clase);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			obj = (T) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T> T jaxbXMLToObject(Document doc, Class<T> clase) {
		T obj = null;

		try {
			JAXBContext jaxbContext = getContext(clase);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			obj = (T) jaxbUnmarshaller.unmarshal(doc);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return obj;
	}
}