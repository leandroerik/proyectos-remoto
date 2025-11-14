package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "informacionProcrear")
public class InformacionProcrear {

	@XmlElement(required = true)
	public String segmentoDeclarado;
	@XmlElement(required = true)
	public String segmentoValidado;
	@XmlElement(required = true)
	public String codEstadoCivilBH;
	@XmlElement(required = true)
	public String nroDocumento;
	@XmlElement(required = true)
	public String coNroDocumento;
	@XmlElement(required = true)
	public BigInteger cantMenorACargo;
	@XmlElement(required = true)
	public BigInteger cantMayorACargo;
	@XmlElement(required = true)
	public BigInteger cantDiscapACargo;
	@XmlElement(required = true)
	public BigInteger cantPersHabitan;
	@XmlElement(required = true)
	public IngresosProcrear ingresosTitular;
	@XmlElement(required = true)
	public IngresosProcrear ingresosCoTitular;
	@XmlElement(required = true)
	public String destinoFondos;
	@XmlElement(required = true)
	public String fechaSorteo;
	@XmlElement(required = true)
	public BigInteger plazoPresentacion;
	@XmlElement(required = true)
	public InformacionPreventa informacionPreventa;
	@XmlElement(required = true)
	public String codigoEmprendimiento;
	public Boolean veteranoGuerra;
	public BigDecimal valorInmueble;
	public BigDecimal montoRefuerzo;

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class IngresosProcrear {

		@XmlElement(required = true)
		public IngresosRD ingresosRD;
		@XmlElement(required = true)
		public IngresosAU ingresosAU;
		@XmlElement(required = true)
		public IngresosMON ingresosMON;
		@XmlElement(required = true)
		public IngresosDOM ingresosDOM;
		@XmlElement(required = true)
		public IngresosES ingresosES;
		@XmlElement(required = true)
		public IngresosAF ingresosAF;
		@XmlElement(required = true)
		public IngresosPRE ingresosPRE;
		@XmlElement(required = true)
		public IngresosBPNT ingresosBPNT;
		@XmlElement(required = true)
		public IngresosUVHI ingresosUVHI;

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosRD")
	public static class IngresosRD {

		@XmlElement(required = true)
		public String periodoRD1;
		@XmlElement(required = true)
		public String cuitRD;
		public BigDecimal ingresosRD1;
		public BigDecimal ingresosRD2;
		public BigDecimal ingresosRD3;
		public BigDecimal ingresosRD4;
		public BigDecimal ingresosRD5;
		public BigDecimal ingresosRD6;
		public BigDecimal ingresosRD7;
		public BigDecimal ingresosRD8;
		public BigDecimal ingresosRD9;
		public BigDecimal ingresosRD10;
		public BigDecimal ingresosRD11;
		public BigDecimal ingresosRD12;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosAU")
	public static class IngresosAU {

		@XmlElement(required = true)
		public String marcaAU;
		@XmlElement(required = true)
		public String categoriaAU;
		@XmlElement(required = true)
		public String fechaAltaAU;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosMON")
	public static class IngresosMON {

		@XmlElement(required = true)
		public String categoriaMON;
		@XmlElement(required = true)
		public String fechaAltaMON;
		@XmlElement(required = true)
		public String periodoMON1;
		public BigDecimal importeTransMON1;
		public BigDecimal importeTransMON2;
		public BigDecimal importeTransMON3;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosDOM")
	public static class IngresosDOM {

		@XmlElement(required = true)
		public String periodoDOM;
		public BigDecimal haberDOM;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosES")
	public static class IngresosES {

		@XmlElement(required = true)
		public String marcaES;
		@XmlElement(required = true)
		public String categoriaES;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosAF")
	public static class IngresosAF {

		@XmlElement(required = true)
		public String periodoAF1;
		public BigDecimal importeAF1;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosPRE")
	public static class IngresosPRE {

		@XmlElement(required = true)
		public String periodoPRE1;
		public BigDecimal haberJubPRE1;
		public BigDecimal haberPenPRE1;
		public BigDecimal haberJubPRE2;
		public BigDecimal haberPenPRE2;
		public BigDecimal haberJubPRE3;
		public BigDecimal haberPenPRE3;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosBPNT")
	public static class IngresosBPNT {

		@XmlElement(required = true)
		public String periodoBPNT1;
		@XmlElement(required = true)
		public String cuitBPNT1;
		public BigDecimal ingresosBPNT1;
		public BigDecimal ingresosBPNT2;
		public BigDecimal ingresosBPNT3;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosUVHI")
	public static class IngresosUVHI {

		@XmlElement(required = true)
		public String periodoUVHI1;
		public BigDecimal importeUVHI1;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "informacionPreventa")
	public static class InformacionPreventa {

		@XmlElement(required = true)
		public BigInteger plazoPh;
		@XmlElement(required = true)
		public String esquemaEvaluacionPh;
		public BigDecimal montoPh;
		public BigDecimal valorInmuebleInicial;
		public BigDecimal cuotaPh;
		public BigDecimal tasaPh;
		public BigDecimal ingresoComputadoSolicitud;
	}
}
