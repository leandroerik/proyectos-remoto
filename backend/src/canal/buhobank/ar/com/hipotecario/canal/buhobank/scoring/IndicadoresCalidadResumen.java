package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "indicadoresCalidadResumen")
public class IndicadoresCalidadResumen {

	@XmlElement(required = true)
	public BigInteger cantDictamenesRiesgo;
	@XmlElement(required = true)
	public BigInteger cantDictamenesControlMuestral;
	@XmlElement(required = true)
	public BigInteger cantExcepciones;
	@XmlElement(required = true)
	public String nivelEvaluado;
	public BigDecimal porcDevolucionesRiesgo;
	public BigDecimal porcObservadosControlMuestral;
}
