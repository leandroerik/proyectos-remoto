package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "documentacion")
public class Documentacion {

	@XmlElement(required = true)
	public BigInteger codigo;
}
