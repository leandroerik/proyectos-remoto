package ar.com.hipotecario.canal.buhobank.scoring;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solicitudEtapas")
public class SolicitudEtapas {

	@XmlElement(required = true)
	public List<SolicitudEtapa> solicitudEtapa;

	public static class SolicitudEtapa {

		@XmlElement(required = true)
		public String motivosExcepcionEtapa;
		public Integer nroEtapa;
		public Boolean flagExcepcionEtapa;
	}
}
