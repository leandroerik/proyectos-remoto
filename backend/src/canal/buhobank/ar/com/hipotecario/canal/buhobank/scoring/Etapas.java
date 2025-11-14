package ar.com.hipotecario.canal.buhobank.scoring;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "etapas")
public class Etapas {

	@XmlElement(required = true)
	public List<Etapa> etapa;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "etapa")
	public static class Etapa {

		@XmlElement(required = true)
		public String motivosRechazoEtapa;
		@XmlElement(required = true)
		public String motivosExcepcionEtapa;
		@XmlElement(required = true)
		public String fechaEjecucionEtapa;
		@XmlElement(required = true)
		public String motivosControlEtapa;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		public Integer nroEtapa;
		public Integer contadorEjecucionEtapa;
		public Boolean resultadoChampionEtapa;
		public Boolean flagControlEtapa;
		public Boolean flagExcepcionEtapa;
		public Boolean flagRechazoEtapa;
	}
}
