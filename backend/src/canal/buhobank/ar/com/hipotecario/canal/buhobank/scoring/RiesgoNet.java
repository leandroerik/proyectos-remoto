package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "riesgoNet")
public class RiesgoNet {

	@XmlElement(required = true)
	public BigInteger resultado;
	@XmlElement(required = true)
	public RiesgoNetIntegrante integrante;
	@XmlElement(required = true)
	public RiesgoNetRelacionados relacionados;
	@XmlElement(required = true)
	public RiesgoNetConsultas consultas;
	@XmlElement(required = true)
	public RiesgoNetDatosAfip datosAfip;
	@XmlElement(required = true)
	public RiesgoNetEmpleos empleos;
	@XmlElement(required = true)
	public RiesgoNetServicioMapas servicioMapas;
	@XmlElement(required = true)
	public RiesgoNetCheques cheques;
	@XmlElement(required = true)
	public RiesgoNetJuicios juicios;
	@XmlElement(required = true)
	public RiesgoNetBcra bcra;
	@XmlElement(required = true)
	public RiesgoNetOtros otros;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetIntegrante")
	public static class RiesgoNetIntegrante {

		@XmlElement(required = true)
		public String validacionIdentidad;
		@XmlElement(required = true)
		public RiesgoNetIdentidad identidad;
		@XmlElement(required = true)
		public RiesgoNetDomicilio direccion;
		@XmlElement(required = true)
		public RiesgoNetServicioMapas servicioMapas;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetIdentidad")
	public static class RiesgoNetIdentidad {

		@XmlElement(required = true)
		public String dni;
		@XmlElement(required = true)
		public String versionDNI;
		@XmlElement(required = true)
		public String cuit;
		@XmlElement(required = true)
		public String apellido;
		@XmlElement(required = true)
		public String nombres;
		@XmlElement(required = true)
		public String sexo;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetDomicilio")
	public static class RiesgoNetDomicilio {

		@XmlElement(required = true)
		public String calle;
		@XmlElement(required = true)
		public String altura;
		@XmlElement(required = true)
		public String piso;
		@XmlElement(required = true)
		public String depto;
		@XmlElement(required = true)
		public String localidad;
		@XmlElement(required = true)
		public String provincia;
		@XmlElement(required = true)
		public String codigoPostal;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetServicioMapas")
	public static class RiesgoNetServicioMapas {

		@XmlElement(required = true)
		public String georefZona;
		@XmlElement(required = true)
		public String georefXCoord;
		@XmlElement(required = true)
		public String georefYCoord;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetRelacionados")
	public static class RiesgoNetRelacionados {

		@XmlElement(required = true)
		public BigInteger cantidad;
		@XmlElement(required = true)
		public String dniConyuge;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetConsultas")
	public static class RiesgoNetConsultas {

		@XmlElement(name = "cantRetailUlt1meses", required = true)
		public BigInteger cantRetailUlt1Meses;
		@XmlElement(name = "cantSistFinUlt1meses", required = true)
		public BigInteger cantSistFinUlt1Meses;
		@XmlElement(name = "cantOtrosUlt1meses", required = true)
		public BigInteger cantOtrosUlt1Meses;
		@XmlElement(name = "cantRetailUlt3meses", required = true)
		public BigInteger cantRetailUlt3Meses;
		@XmlElement(name = "cantSistFinUlt3meses", required = true)
		public BigInteger cantSistFinUlt3Meses;
		@XmlElement(name = "cantOtrosUlt3meses", required = true)
		public BigInteger cantOtrosUlt3Meses;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetDatosAfip")
	public static class RiesgoNetDatosAfip {

		@XmlElement(required = true)
		public String categoriaMonotributo;
		@XmlElement(required = true)
		public String fechaInicioAct;
		@XmlElement(required = true)
		public String inscripcionAutonomos;
		@XmlElement(required = true)
		public String categoriaFiscal;
		@XmlElement(required = true)
		public String inscripcionIva;
		@XmlElement(required = true)
		public String inscripcionGcias;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetEmpleos")
	public static class RiesgoNetEmpleos {

		@XmlElement(required = true)
		public String cuitEmpleador;
		@XmlElement(required = true)
		public BigInteger peorSitBcraEmpleadorUlt12Meses;
		@XmlElement(name = "ScoreIngresoPresunto", required = true)
		public BigInteger scoreIngresoPresunto;
		@XmlElement(required = true)
		public BigInteger factor;
		@XmlElement(required = true)
		public String fechaIngreso;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetCheques")
	public static class RiesgoNetCheques {

		@XmlElement(name = "cantRechSinFondoUlt12meses", required = true)
		public BigInteger cantRechSinFondoUlt12Meses;
		@XmlElement(name = "cantRechSinFondoUlt24meses", required = true)
		public BigInteger cantRechSinFondoUlt24Meses;
		@XmlElement(name = "cantRechSinFondoUlt36meses", required = true)
		public BigInteger cantRechSinFondoUlt36Meses;
		@XmlElement(name = "cantRechSinFondoUlt48meses", required = true)
		public BigInteger cantRechSinFondoUlt48Meses;
		@XmlElement(name = "cantRechSinFondoUlt60meses", required = true)
		public BigInteger cantRechSinFondoUlt60Meses;
		@XmlElement(name = "cantRechSinFondoRecupUlt12meses", required = true)
		public BigInteger cantRechSinFondoRecupUlt12Meses;
		@XmlElement(name = "cantRechSinFondoRecupUlt24meses", required = true)
		public BigInteger cantRechSinFondoRecupUlt24Meses;
		@XmlElement(name = "cantRechSinFondoRecupUlt36meses", required = true)
		public BigInteger cantRechSinFondoRecupUlt36Meses;
		@XmlElement(name = "cantRechSinFondoRecupUlt48meses", required = true)
		public BigInteger cantRechSinFondoRecupUlt48Meses;
		@XmlElement(name = "cantRechSinFondoRecupUlt60meses", required = true)
		public BigInteger cantRechSinFondoRecupUlt60Meses;
		@XmlElement(name = "cantRechSinFondo3rosUlt12meses", required = true)
		public BigInteger cantRechSinFondo3RosUlt12Meses;
		@XmlElement(name = "cantRechSinFondo3rosUlt24meses", required = true)
		public BigInteger cantRechSinFondo3RosUlt24Meses;
		@XmlElement(name = "cantRechSinFondo3rosUlt36meses", required = true)
		public BigInteger cantRechSinFondo3RosUlt36Meses;
		@XmlElement(name = "cantRechSinFondo3rosUlt48meses", required = true)
		public BigInteger cantRechSinFondo3RosUlt48Meses;
		@XmlElement(name = "cantRechSinFondo3rosUlt60meses", required = true)
		public BigInteger cantRechSinFondo3RosUlt60Meses;
		@XmlElement(name = "cantRechSinFondo3rosRecupUlt12meses", required = true)
		public BigInteger cantRechSinFondo3RosRecupUlt12Meses;
		@XmlElement(name = "cantRechSinFondo3rosRecupUlt24meses", required = true)
		public BigInteger cantRechSinFondo3RosRecupUlt24Meses;
		@XmlElement(name = "cantRechSinFondo3rosRecupUlt36meses", required = true)
		public BigInteger cantRechSinFondo3RosRecupUlt36Meses;
		@XmlElement(name = "cantRechSinFondo3rosRecupUlt48meses", required = true)
		public BigInteger cantRechSinFondo3RosRecupUlt48Meses;
		@XmlElement(name = "cantRechSinFondo3rosRecupUlt60meses", required = true)
		public BigInteger cantRechSinFondo3RosRecupUlt60Meses;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetJuicios")
	public static class RiesgoNetJuicios {

		@XmlElement(required = true)
		public BigInteger concursos;
		@XmlElement(required = true)
		public BigInteger quiebras;
		@XmlElement(required = true)
		public BigInteger ordinarios;
		@XmlElement(required = true)
		public BigInteger ejecutivos;
		@XmlElement(required = true)
		public String fechaUltimoJuicio;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetBcra")
	public static class RiesgoNetBcra {

		@XmlElement(name = "peorSitUlt4meses", required = true)
		public BigInteger peorSitUlt4Meses;
		@XmlElement(name = "peorSitUlt12meses", required = true)
		public BigInteger peorSitUlt12Meses;
		@XmlElement(name = "peorSitUlt18meses", required = true)
		public BigInteger peorSitUlt18Meses;
		@XmlElement(name = "peorSitUlt24meses", required = true)
		public BigInteger peorSitUlt24Meses;
		@XmlElement(name = "peorSitUlt36meses", required = true)
		public BigInteger peorSitUlt36Meses;
		@XmlElement(name = "peorSitUlt48meses", required = true)
		public BigInteger peorSitUlt48Meses;
		@XmlElement(name = "peorSitUlt60meses", required = true)
		public BigInteger peorSitUlt60Meses;
		@XmlElement(required = true)
		public BigInteger situacionActual;
		@XmlElement(required = true)
		public String fechaInforme;
		@XmlElement(required = true)
		public BigInteger cantLineasActivas;
		@XmlElement(required = true)
		public String deudaEntidadesLiquidada;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetOtros")
	public static class RiesgoNetOtros {

		@XmlElement(required = true)
		public String fraudes;
		@XmlElement(required = true)
		public String verificaciones;
		@XmlElement(name = "cantVecesMoraUlt6meses", required = true)
		public BigInteger cantVecesMoraUlt6Meses;
		@XmlElement(name = "cantVecesMoraCancelUlt6meses", required = true)
		public BigInteger cantVecesMoraCancelUlt6Meses;
		@XmlElement(name = "cantVecesMoraUlt12meses", required = true)
		public BigInteger cantVecesMoraUlt12Meses;
		@XmlElement(name = "cantVecesMoraCancelUlt12meses", required = true)
		public BigInteger cantVecesMoraCancelUlt12Meses;
		@XmlElement(name = "cantVecesMoraUlt24meses", required = true)
		public BigInteger cantVecesMoraUlt24Meses;
		@XmlElement(name = "cantVecesMoraCancelUlt24meses", required = true)
		public BigInteger cantVecesMoraCancelUlt24Meses;
		public List<RiesgoNetEntidad> entidades;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetEntidad")
	public static class RiesgoNetEntidad {

		@XmlElement(required = true)
		public String entidad;
		@XmlElement(required = true)
		public BigInteger diasMora;
		public BigDecimal montoDeuda;
		@XmlElement(required = true)
		public String fechaInforme;
	}
}
