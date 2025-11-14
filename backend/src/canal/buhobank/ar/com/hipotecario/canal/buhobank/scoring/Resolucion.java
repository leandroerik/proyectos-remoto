package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "resolucion")
public class Resolucion {

	@XmlElement(required = true)
	public String resolucion;
	@XmlElement(required = true)
	public String explicacion;
	@XmlElement(required = true)
	public String detalleAprobacion;
	@XmlElement(required = true)
	public String modoAprobacion;
	@XmlElement(required = true)
	public String fechaVigencia;
	@XmlElement(required = true)
	public String esquemaEvaluacion;
	@XmlElement(required = true)
	public BigInteger tipoEvaluacion;
	public Boolean flagExcepcion;
	@XmlElement(required = true)
	public String motivosExcepcion;
	@XmlElement(required = true)
	public String observacionesCanal;
	@XmlElement(required = true)
	public String documentacion;
	@XmlElement(required = true)
	public String resolucionCodigo;
	@XmlElement(required = true)
	public String resolucionDescripcion;
	@XmlElement(required = true)
	public String modoAprobacionCodigo;
	@XmlElement(required = true)
	public String modoAprobacionDescripcion;
	@XmlElement(required = true)
	public String esquemaEvaluacionCodigo;
	@XmlElement(required = true)
	public String esquemaEvaluacionDescripcion;
	@XmlElement(required = true)
	public String codigoExplicacion;
	@XmlElement(name = "dummy_01", required = true)
	public String dummy01;
	@XmlElement(name = "dummy_02", required = true)
	public String dummy02;
	@XmlElement(name = "dummy_03", required = true)
	public String dummy03;
	@XmlElement(required = true)
	public String codEsquemaEvaluacionCore;
	@XmlElement(required = true)
	public String codResolucionCore;
	@XmlElement(required = true)
	public String codigoEstado;
}
