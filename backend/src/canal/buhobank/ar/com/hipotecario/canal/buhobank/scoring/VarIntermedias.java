package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "varIntermedias")
public class VarIntermedias {

	@XmlElement(required = true)
	public String variablesIngreso;
	@XmlElement(required = true)
	public String valoresIngreso;
	@XmlElement(required = true)
	public String segmento;
	@XmlElement(required = true)
	public String grupoRiesgo;
	@XmlElement(required = true)
	public String resultadoAlgoritmoAprobacion;
	@XmlElement(required = true)
	public String resultadoAlgoritmoControlMuestral;
	@XmlElement(required = true)
	public String resultadoAlgoritmoVerificacion;
	@XmlElement(required = true)
	public String flagAlgoritmoControlMuestral;
	@XmlElement(required = true)
	public String flagAlgoritmoVerificacion;
	@XmlElement(name = "dummy_01", required = true)
	public String dummy01;
	@XmlElement(name = "dummy_02", required = true)
	public String dummy02;
	@XmlElement(name = "dummy_03", required = true)
	public String dummy03;
	@XmlElement(name = "dummy_04", required = true)
	public String dummy04;
	@XmlElement(name = "dummy_05", required = true)
	public String dummy05;
	@XmlElement(required = true)
	public String codTipoIngresoCore;
	@XmlElement(required = true)
	public String codSubTipoIngresoCore;
	@XmlElement(required = true)
	public String codFuenteInferenciaCore;
	public Boolean flagCondicionesEspeciales;
	public BigDecimal ranking;
	public BigDecimal sumaAcuerdosBH;
	public BigDecimal sumaLimitesCompraBH;
	public BigDecimal sumaDeudasPrestamosSinGtiaBH;
	public BigDecimal endeudamiento;
	public BigDecimal compromiso;
	public Boolean flagGrisModificDatos;
	public BigDecimal probDefault;
	public Integer casoVentaCruzada;
	public BigDecimal ingresoComputado;
	public BigDecimal ingresoComputadoIncrementado;
}