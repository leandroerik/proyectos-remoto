package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "datosInternos")
public class DatosInternos {

	@XmlElement(required = true)
	public String rutaTramite;
	@XmlElement(required = true)
	public String canalContacto;
	@XmlElement(required = true)
	public String observacionesGO;
	@XmlElement(required = true)
	public String observacionesRI;
	@XmlElement(required = true)
	public String flagErroresServicios;
	@XmlElement(required = true)
	public String logServicios;
	@XmlElement(required = true)
	public BigInteger errorPorExcepcion;
	@XmlElement(required = true)
	public String mensajeErrorPorExcepcion;
	@XmlElement(required = true)
	public String rutaTramiteCodigo;
	@XmlElement(required = true)
	public String canalContactoCodigo;
	@XmlElement(required = true)
	public String version;
	@XmlElement(required = true)
	public String estadoFinal;
	@XmlElement(required = true)
	public String trace;
	@XmlElement(required = true)
	public BigInteger aplicaTest;
	@XmlElement(required = true)
	public String motivoChampionChallengerAplicado;
	@XmlElement(required = true)
	public String motivoChampionChallengerPosibles;
	public Boolean planAmigo;
	@XmlElement(required = true)
	public String derivarA;
	public Integer nroPantalla;
	public Boolean ventaMktDir;
}
