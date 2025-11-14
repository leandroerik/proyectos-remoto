package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solicitantes")
public class Solicitantes {

	@XmlElement(required = true)
	public List<Solicitante> solicitante;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "solicitante")
	public static class Solicitante {

		public Integer nroSolicitante;
		@XmlElement(required = true)
		public Identidad identidad;
		@XmlElement(required = true)
		public String relacionTitular;
		@XmlElement(required = true)
		public DatosPersonales datosPersonales;
		@XmlElement(required = true)
		public Domicilio domicilioParticular;
		@XmlElement(required = true)
		public Telemail telemailParticular;
		@XmlElement(required = true)
		public DatosIngresosSolicitante ingresos;
		@XmlElement(required = true)
		public Domicilio domicilioLaboral;
		@XmlElement(required = true)
		public Telemail telemailLaboral;
		@XmlElement(required = true)
		public EmpleoAnterior empleoAnterior;
		@XmlElement(required = true)
		public CondicionImpositiva condicionImpositiva;
		@XmlElement(required = true)
		public DatosPatrimonialesFinancieros datosPatrimonialesFinancieros;
		@XmlElement(required = true)
		public String resultadoDDJJSalud;
		@XmlElement(required = true)
		public String resultadoPsicometrico;
		public BuhoBank buhoBank;
		public Referencias referencias;
		public Actividades actividades;
		public Boolean esSujetoObligado;
		public Boolean solicitaValidarIdentidad;
		public String esPlanSueldo;
		public Boolean solicitaEvaluarMercadoAbierto;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "identidad")
	public static class Identidad {

		@XmlElement(required = true)
		public String tipoDocumento;
		@XmlElement(required = true)
		public String nroDocumento;
		@XmlElement(required = true)
		public String versionDNI;
		@XmlElement(required = true)
		public String sexo;
		@XmlElement(required = true)
		public String tipoIdTributaria;
		@XmlElement(required = true)
		public String nroIdTributaria;
		public Long idCliente;
		@XmlElement(required = true)
		public String apellido;
		@XmlElement(required = true)
		public String nombres;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosPersonales")
	public static class DatosPersonales {

		@XmlElement(required = true)
		public BigInteger nacionalidad;
		@XmlElement(required = true)
		public String fechaNacimiento;
		@XmlElement(required = true)
		public String estadoCivil;
		@XmlElement(required = true)
		public String nupcias;
		@XmlElement(required = true)
		public BigInteger nroHijos;
		@XmlElement(required = true)
		public String nivelEstudios;
		@XmlElement(required = true)
		public String tituloAlcanzado;
		@XmlElement(required = true)
		public String obraSocial;
		@XmlElement(required = true)
		public String situacionVivienda;
		@XmlElement(required = true)
		public BigInteger resideDesdeVivienda;
		@XmlElement(required = true)
		public BigInteger paisNacimiento;
		@XmlElement(required = true)
		public String paisResidencia;
		@XmlElement(required = true)
		public BigInteger resideDesde;
		@XmlElement(required = true)
		public String ciudadNacimiento;
		@XmlElement(required = true)
		public String provinciaPertenencia;
		@XmlElement(required = true)
		public String apellidoNombreMadre;
		@XmlElement(required = true)
		public String apellidoNombrePadre;
		@XmlElement(required = true)
		public String tipoPersona;
		@XmlElement(required = true)
		public String subEstadoCivil;
		public BigDecimal montoAlquiler;
		public Boolean unidoHecho;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "domicilio")
	public static class Domicilio {

		@XmlElement(required = true)
		public String tipoDomicilio;
		@XmlElement(required = true)
		public String calle;
		@XmlElement(required = true)
		public BigInteger numero;
		@XmlElement(required = true)
		public String piso;
		@XmlElement(required = true)
		public String depto;
		@XmlElement(required = true)
		public String entreCalle;
		@XmlElement(required = true)
		public String yCalle;
		@XmlElement(required = true)
		public String codigoPostal;
		@XmlElement(required = true)
		public BigInteger localidad;
		@XmlElement(required = true)
		public String localidadDescriptivo;
		@XmlElement(required = true)
		public BigInteger provincia;
		@XmlElement(required = true)
		public String provinciaDescriptivo;
		@XmlElement(required = true)
		public BigInteger pais;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "telemail")
	public static class Telemail {

		@XmlElement(required = true)
		public String telLineaDDI;
		@XmlElement(required = true)
		public String telLineaDDN;
		@XmlElement(required = true)
		public String telLineaCaract;
		@XmlElement(required = true)
		public String telLineaNro;
		@XmlElement(required = true)
		public String telPertenencia;
		@XmlElement(required = true)
		public String telCelularPrefijo;
		@XmlElement(required = true)
		public String telCelularCaract;
		@XmlElement(required = true)
		public String telCelularNro;
		@XmlElement(required = true)
		public String email;
		public Boolean tieneMail;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "referencias")
	public static class Referencias {

		@XmlElement(required = true)
		public List<Referencia> referencia;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "referencia")
		public static class Referencia {

			@XmlElement(required = true)
			public String apellidoYNombres;
			@XmlElement(required = true)
			public Telemail telemail;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "actividades")
	public static class Actividades {

		@XmlElement(required = true)
		public List<Actividad> actividad;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "actividad")
		public static class Actividad {

			@XmlElement(required = true)
			public String situacionLaboral;
			@XmlElement(required = true)
			public String ramo;
			@XmlElement(required = true)
			public String cargo;
			@XmlElement(required = true)
			public String profesion;
			@XmlElement(required = true)
			public String fechaInicioActividad;
			@XmlElement(required = true)
			public String razonSocialEmpleador;
			@XmlElement(required = true)
			public String cuitEmpleador;
			@XmlElement(required = true)
			public String relacionCorporatConBH;
			@XmlElement(required = true)
			public DatosIngresosActividad ingresos;
			@XmlElement(required = true)
			public DatosConvenioPlanSueldo convenioPlanSueldo;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosIngresosActividad")
	public static class DatosIngresosActividad {

		public BigDecimal ingresoNeto;
		public BigDecimal conceptosVariables;
		public BigDecimal anticiposPercibidos;
		public BigDecimal cuotasDescontadas;
		@XmlElement(required = true)
		public String periodicidadRecibo;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosConvenioPlanSueldo")
	public static class DatosConvenioPlanSueldo {

		@XmlElement(required = true)
		public String tipoConvenio;
		@XmlElement(required = true)
		public String tipoEmpresa;
		@XmlElement(required = true)
		public String segmentoCliente;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosIngresosSolicitante")
	public static class DatosIngresosSolicitante {

		public double ingresosDdjjGanancias;
		@XmlElement(required = true)
		public String fechaDdjjGanancias;
		@XmlElement(required = true)
		public String categoriaMonotributo;
		@XmlElement(required = true)
		public String fechaCategorizacion;
		public BigDecimal ingresosDdjjIngresosBrutos;
		@XmlElement(required = true)
		public String fechaInicioActividad;
		public BigDecimal iva;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "empleoAnterior")
	public static class EmpleoAnterior {

		@XmlElement(required = true)
		public String razonSocial;
		@XmlElement(required = true)
		public String telLineaDDI;
		@XmlElement(required = true)
		public String telLineaDDN;
		@XmlElement(required = true)
		public String telLineaCaract;
		@XmlElement(required = true)
		public String telLineaNro;
		@XmlElement(required = true)
		public String fechaIngreso;
		@XmlElement(required = true)
		public String fechaCese;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "condicionImpositiva")
	public static class CondicionImpositiva {

		@XmlElement(required = true)
		public String impValorAgregado;
		@XmlElement(required = true)
		public String impIngresosBrutos;
		@XmlElement(required = true)
		public String impGanancias;
		@XmlElement(required = true)
		public String impDebitosCreditos;
		@XmlElement(required = true)
		public String impSellos;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosPatrimonialesFinancieros")
	public static class DatosPatrimonialesFinancieros {

		@XmlElement(required = true)
		public Rodados rodados;
		@XmlElement(required = true)
		public Inmuebles inmuebles;
		@XmlElement(required = true)
		public PrestamosSF prestamosSF;
		@XmlElement(required = true)
		public TarjetasSF tarjetasSF;
		@XmlElement(required = true)
		public CuentasSF cuentasSF;
		@XmlElement(required = true)
		public Gastos gastos;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "rodados")
	public static class Rodados {

		@XmlElement(required = true)
		public List<Rodado> rodado;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "rodado")
		public static class Rodado {

			@XmlElement(required = true)
			public String marca;
			@XmlElement(required = true)
			public String modelo;
			@XmlElement(required = true)
			public BigInteger ano;
			@XmlElement(required = true)
			public String patente;
			public BigDecimal valorEstimado;
			public Boolean prenda;
			public BigDecimal porcentajeCondominio;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "inmuebles")
	public static class Inmuebles {

		@XmlElement(required = true)
		public List<Inmueble> inmueble;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "inmueble")
		public static class Inmueble {

			@XmlElement(required = true)
			public String tipo;
			@XmlElement(required = true)
			public String calle;
			@XmlElement(required = true)
			public BigInteger numero;
			@XmlElement(required = true)
			public String piso;
			@XmlElement(required = true)
			public String depto;
			@XmlElement(required = true)
			public String codigoPostal;
			public BigDecimal valorEstimado;
			public Boolean hipoteca;
			public BigDecimal porcentajeCondominio;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "prestamosSF")
	public static class PrestamosSF {

		@XmlElement(required = true)
		public List<PrestamoSF> prestamoSF;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "prestamoSF")
		public static class PrestamoSF {

			@XmlElement(required = true)
			public String tipoPrestamoSF;
			@XmlElement(required = true)
			public String entidad;
			@XmlElement(required = true)
			public String detalleEntidad;
			public BigDecimal deuda;
			@XmlElement(required = true)
			public BigInteger plazoRestante;
			public BigDecimal valorCuota;
			public Boolean cancela;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "tarjetasSF")
	public static class TarjetasSF {

		@XmlElement(required = true)
		public List<TarjetaSF> tarjetaSF;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "tarjetaSF")
		public static class TarjetaSF {

			@XmlElement(required = true)
			public String marcaTarjetaSF;
			@XmlElement(required = true)
			public String entidad;
			@XmlElement(required = true)
			public String detalleEntidad;
			public BigDecimal limiteCompra;
			public BigDecimal deuda;
			public BigDecimal promPagos;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "cuentasSF")
	public static class CuentasSF {

		@XmlElement(required = true)
		public List<CuentaSF> cuentaSF;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "cuentaSF")
		public static class CuentaSF {

			@XmlElement(required = true)
			public String entidad;
			@XmlElement(required = true)
			public String detalleEntidad;
			public BigDecimal montoAcuerdo;
			public BigDecimal deuda;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "gastos")
	public static class Gastos {

		public BigDecimal expensas;
		public BigDecimal colegios;
		public BigDecimal medicinaPrepaga;
		public BigDecimal segurosOpcionales;
		public BigDecimal otrosGastosMensuales;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "buhobank")
	public static class BuhoBank {

		public BigDecimal ingresoNeto;
		@XmlElement(required = true)
		public String situacionLaboral;
	}

}
