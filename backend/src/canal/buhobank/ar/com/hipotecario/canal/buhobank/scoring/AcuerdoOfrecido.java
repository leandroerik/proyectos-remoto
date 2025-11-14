package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "acuerdoOfrecido")
public class AcuerdoOfrecido {

	@XmlElement(required = true)
	public BigInteger nro;
	@XmlElement(required = true)
	public String subProducto;
	@XmlElement(required = true)
	public String negocio;
	@XmlElement(name = "dummy_01", required = true)
	public String dummy01;
	public BigDecimal monto;
	public BigDecimal montoMax;
	public Integer plazo;
}
