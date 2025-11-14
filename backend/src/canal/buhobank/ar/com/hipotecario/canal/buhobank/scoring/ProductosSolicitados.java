package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "productosSolicitados")
public class ProductosSolicitados {

	public CuentasSolicitadas cuentasSolicitadas;
	public AcuerdosSolicitados acuerdosSolicitados;
	public TarjetasSolicitadas tarjetasSolicitadas;
	public PrestamosSolicitados prestamosSolicitados;
	public ModificacionesSolicitadas modificacionesSolicitadas;
	public InclusionesSolicitadas inclusionesSolicitadas;
	@XmlElement(required = true)
	public BigInteger tipoPaquete;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "cuentasSolicitadas")
	public static class CuentasSolicitadas {

		@XmlElement(required = true)
		public List<CuentaSolicitada> cuentaSolicitada;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "cuentaSolicitada")
		public static class CuentaSolicitada {

			@XmlElement(required = true)
			public RolesSolicitantes rolesSolicitantes;
			@XmlElement(required = true)
			public BigInteger tipoCuenta;
			@XmlElement(required = true)
			public BigInteger subTipoCuenta;
			@XmlElement(required = true)
			public BigInteger nro;
			public BigDecimal montoAlta;
			public Boolean flagPaquete;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "rolesSolicitantes")
	public static class RolesSolicitantes {

		@XmlElement(required = true)
		public List<RolSolicitante> rolSolicitante;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "rolSolicitante")
		public static class RolSolicitante {

			public Integer nroSolicitante;
			@XmlElement(required = true)
			public String rolSolicitante;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "acuerdosSolicitados")
	public static class AcuerdosSolicitados {

		@XmlElement(required = true)
		public List<AcuerdoSolicitado> acuerdoSolicitado;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "acuerdoSolicitado")
		public static class AcuerdoSolicitado {

			@XmlElement(required = true)
			public String nroCuenta;
			@XmlElement(required = true)
			public BigInteger nro;
			public BigDecimal montoAlta;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "tarjetasSolicitadas")
	public static class TarjetasSolicitadas {

		@XmlElement(required = true)
		public List<TarjetaSolicitada> tarjetaSolicitada;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "tarjetaSolicitada")
		public static class TarjetaSolicitada {

			@XmlElement(required = true)
			public RolesSolicitantes rolesSolicitantes;
			@XmlElement(required = true)
			public BigInteger tipoTarjeta;
			@XmlElement(required = true)
			public BigInteger subtipoTarjeta;
			@XmlElement(required = true)
			public String tipoNegocio;
			@XmlElement(required = true)
			public String destinoFondos;
			@XmlElement(required = true)
			public String tipoBien;
			@XmlElement(required = true)
			public BigInteger nro;
			public Boolean solicitaPrimeraCompra;
			@XmlElement(required = true)
			public String caracteristicasEspeciales;
			@XmlElement(required = true)
			public String seguroAplicado;
			@XmlElement(required = true)
			public String zonaDistribucionCP;
			public BigDecimal montoSolicitado;
			public BigDecimal montoAlta;
			public Boolean flagPaquete;
			public Boolean esVirtual;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "prestamosSolicitados")
	public static class PrestamosSolicitados {

		@XmlElement(required = true)
		public List<PrestamoSolicitado> prestamoSolicitado;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "prestamoSolicitado")
		public static class PrestamoSolicitado {

			@XmlElement(required = true)
			public RolesSolicitantes rolesSolicitantes;
			@XmlElement(required = true)
			public String tipoPrestamo;
			@XmlElement(required = true)
			public String subTipoPrestamo;
			@XmlElement(required = true)
			public String tipoTasa;
			@XmlElement(required = true)
			public String sistemaAmortizacion;
			@XmlElement(required = true)
			public String destinoFondos;
			@XmlElement(required = true)
			public String tipoBien;
			@XmlElement(required = true)
			public String detalleBien;
			@XmlElement(required = true)
			public String destinoBien;
			@XmlElement(required = true)
			public String plazoSolicitado;
			@XmlElement(required = true)
			public String formaCobro;
			@XmlElement(required = true)
			public String tipoCuentaCobro;
			@XmlElement(required = true)
			public String nroCuentaCobro;
			@XmlElement(required = true)
			public String entidadCobro;
			@XmlElement(required = true)
			public BigInteger nro;
			@XmlElement(required = true)
			public String fechaEmisionCertificado;
			public BigDecimal montoAfectacionCertificado;
			public Boolean conSeguroVida;
			public Boolean conSeguroIncendio;
			public BigDecimal valorBien;
			public BigDecimal valorTerreno;
			public BigDecimal valorObra;
			public BigDecimal montoSolicitado;
			public BigDecimal cuotaSolicitada;
			public Integer antiguedadBien;
			public BigDecimal montoAlta;
			public Integer plazoAlta;
			public Boolean flagPaquete;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "modificacionesSolicitadas")
	public static class ModificacionesSolicitadas {

		@XmlElement(required = true)
		public List<ModificacionSolicitada> modificacionSolicitada;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "modificacionSolicitada")
		public static class ModificacionSolicitada {

			@XmlElement(required = true)
			public String tipoModificacion;
			@XmlElement(required = true)
			public String nroProducto;
			public BigDecimal montoAlta;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "inclusionesSolicitadas")
	public static class InclusionesSolicitadas {

		@XmlElement(required = true)
		public List<InclusionSolicitada> inclusionSolicitada;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "inclusionSolicitada")
		public static class InclusionSolicitada {

			@XmlElement(required = true)
			public RolesSolicitantes rolesSolicitantes;
			@XmlElement(required = true)
			public String tipoInclusion;
			@XmlElement(required = true)
			public String nroProducto;
		}
	}

}
