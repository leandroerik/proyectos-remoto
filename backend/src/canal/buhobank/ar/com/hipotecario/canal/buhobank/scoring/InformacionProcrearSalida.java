package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "informacionProcrearSalida")
public class InformacionProcrearSalida {

	@XmlElement(required = true)
	public IngresosProcrearSalida ingresosProcrearSalida;
	@XmlElement(required = true)
	public String controlMuestral;
	@XmlElement(required = true)
	public String zonaOriginacion;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosProcrearSalida")
	public static class IngresosProcrearSalida {

		@XmlElement(required = true)
		public String categoriaMonotributoTit;
		@XmlElement(required = true)
		public String categoriaMonotributoCoTit;
		@XmlElement(required = true)
		public BigInteger ingresoSOLValidado;
		@XmlElement(required = true)
		public String vigenciaValidacion;
		public BigDecimal ingrCoTitularRD;
		public BigDecimal ingrCoTitularJUB;
		public BigDecimal ingrCoTitularUVHI;
		public BigDecimal ingrCoTitularAF;
		public BigDecimal ingrTitularRD;
		public BigDecimal ingrTitularJUB;
		public BigDecimal ingrTitularUVHI;
		public BigDecimal ingrTitularAF;
	}
}
