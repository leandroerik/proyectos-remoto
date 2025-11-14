package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "integrantes")
public class Integrantes {

	@XmlElement(required = true)
	public List<Integrante> integrante;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "integrante")
	public static class Integrante {

		@XmlElement(required = true)
		public BigInteger nroIntegrante;
		@XmlElement(required = true)
		public String nroIdTributaria;
		@XmlElement(required = true)
		public Ocupaciones ocupaciones;
		@XmlElement(required = true)
		public NosisVariables nosisVariables;
		@XmlElement(required = true)
		public NosisInforme nosisInforme;
		@XmlElement(required = true)
		public String gradoBancarizacion;
		@XmlElement(required = true)
		public ScoreInterno scoreInternoApplication;
		@XmlElement(required = true)
		public ScoreInterno scoreInternoBehavior;
		public BigDecimal probDefault;
		@XmlElement(required = true)
		public CampanaVigente campañaVigente;
		@XmlElement(required = true)
		public TramitesVigentes tramitesVigentes;
		@XmlElement(required = true)
		public DatosPosicionConsolidada datosPosicionConsolidada;
		@XmlElement(required = true)
		public VerazVariables verazVariables;
		@XmlElement(required = true)
		public BureauVeraz bureauVeraz;
		@XmlElement(required = true)
		public String tipoRolIntegrante;
		@XmlElement(required = true)
		public String explicacion;
		@XmlElement(required = true)
		public String logStatus;
		@XmlElement(name = "flag_BureauVeraz")
		public Boolean flagBureauVeraz;
		@XmlElement(required = true)
		public SvmDomicilio domicilioParticular;
		@XmlElement(required = true)
		public SvmTelemail telemailParticular;
		@XmlElement(required = true)
		public SvmDomicilio domicilioLaboral;
		@XmlElement(required = true)
		public SvmTelemail telemailLaboral;
		@XmlElement(required = true)
		public Documentaciones documentaciones;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		@XmlElement(required = true)
		public RiesgoNetVariables riesgoNetVariables;
		@XmlElement(required = true)
		public RiesgoNet riesgoNet;
		@XmlElement(required = true)
		public VerazBehavior verazBehavior;
		@XmlElement(required = true)
		public BigInteger validacionIdentidadSoloMotor;
		@XmlElement(required = true)
		public BigInteger validacionIdentidad;
		@XmlElement(required = true)
		public String segmentoValidacion;
		@XmlElement(required = true)
		public String tipoCuentaPlanSueldo;
		@XmlElement(required = true)
		public String grupoControlValidacionNse;
		@XmlElement(required = true)
		public String grupoControlValidacionNseRandom1;
		@XmlElement(required = true)
		public String codTipoIngresoCore;
		@XmlElement(required = true)
		public String codSubTipoIngresoCore;
		@XmlElement(required = true)
		public IngresosInferidos ingresosInferidos;
		public Boolean utilizaCuotaPhone;
		public Boolean flagGrisSF;
		public Boolean indOK;
		public BigDecimal ingresoComputado;
		public BigDecimal pesoComportamiento;
		public Boolean flagConsultarVeraz;
		public Boolean solicitarIngresosPreaprobado;
		public Boolean flagGrisBNP;
		public Boolean flagNegroBNP;
		public Boolean flagCliente;
		public Boolean flagGrisBH;
		public Boolean flagGrisBCRA;
		public Boolean flagTramiteVigente;
		public Boolean flagNegroBND;
		public Boolean flagGrisBND;
		public Boolean flagGrisBNE;
		public Boolean flagNegroBNE;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ocupaciones")
	public static class Ocupaciones {

		@XmlElement(required = true)
		public List<Ocupacion> ocupacion;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "ocupacion")
		public static class Ocupacion {

			@XmlElement(required = true)
			public String tipoEmpresa;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			@XmlElement(name = "dummy_02", required = true)
			public String dummy02;
			@XmlElement(name = "dummy_03", required = true)
			public String dummy03;
			@XmlElement(required = true)
			public String vigenciaAcreditaciones;
			@XmlElement(required = true)
			public String codTipoIngresoCore;
			@XmlElement(required = true)
			public String codSubTipoIngresoCore;
			public Integer nroOcupacion;
			public BigDecimal ingresoComputado;
			public BigDecimal ingresoPorAcreditaciones;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "nosisVariables")
	public static class NosisVariables {

		@XmlElement(required = true)
		public String resolucionNosis;
		@XmlElement(required = true)
		public String nosisInfoHtml;
		@XmlElement(required = true)
		public BigInteger tipoCDA;
		@XmlElement(required = true)
		public BigInteger cantConsultas;
		@XmlElement(required = true)
		public BigInteger scoreRiesgo;
		@XmlElement(required = true)
		public String scoreNSE;
		@XmlElement(required = true)
		public BigInteger percentil;
		@XmlElement(required = true)
		public String fechaInforme;
		@XmlElement(required = true)
		public String fuenteInforme;
		@XmlElement(required = true)
		public String fechaVigencia;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		public Boolean errorServicio;
		public Boolean bancarizado;
		public BigDecimal cuotaNoRegistrada;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "nosisInforme")
	public static class NosisInforme {

		@XmlElement(required = true)
		public NosisDeudas nosisDeudas;
		public List<NosisItem> nosisItem;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "nosisItem")
		public static class NosisItem {

			@XmlElement(required = true)
			public String clave;
			@XmlElement(required = true)
			public String descripcion;
			@XmlElement(required = true)
			public String detalle;
			@XmlElement(required = true)
			public String nro;
			@XmlElement(required = true)
			public String valor;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "nosisDeudas")
	public static class NosisDeudas {

		@XmlElement(required = true)
		public List<NosisDeuda> nosisDeuda;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "nosisDeuda")
		public static class NosisDeuda {

			@XmlElement(required = true)
			public String codigoEntidad;
			@XmlElement(required = true)
			public String periodo;
			@XmlElement(required = true)
			public String procJudicial;
			@XmlElement(required = true)
			public String situacion;
			@XmlElement(required = true)
			public NosisLineas nosisLineas;
			public Boolean flagEntidadInformaBureau;
			public BigDecimal monto;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "nosisLineas")
	public static class NosisLineas {

		@XmlElement(required = true)
		public List<NosisLinea> nosisLinea;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "nosisLinea")
		public static class NosisLinea {

			@XmlElement(required = true)
			public String codigoLinea;
			public BigDecimal total;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "scoreInterno")
	public static class ScoreInterno {

		@XmlElement(required = true)
		public String codigoRazon;
		@XmlElement(required = true)
		public String variables;
		@XmlElement(required = true)
		public String valores;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(required = true)
		public BigInteger tipoScoreAplicado;
		public BigDecimal resultado;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "campanaVigente")
	public static class CampanaVigente {

		@XmlElement(required = true)
		public BigInteger idLote;
		@XmlElement(required = true)
		public String descCampanaAmpliado;
		@XmlElement(required = true)
		public String descCampania;
		@XmlElement(required = true)
		public String grupoAfinidad;
		@XmlElement(required = true)
		public String grupoRiesgo;
		@XmlElement(required = true)
		public String flagCobranding;
		@XmlElement(required = true)
		public String flagVentaTcHabilitada;
		@XmlElement(required = true)
		public String tipoCampanaVigente;
		@XmlElement(required = true)
		public BigInteger plazoPrestamoPersonal;
		@XmlElement(required = true)
		public BigInteger plazoPrestamoPersonalSolo;
		@XmlElement(required = true)
		public String fechaInforme;
		@XmlElement(required = true)
		public String fechaVigencia;
		@XmlElement(required = true)
		public String fechaValidezPrescreening;
		@XmlElement(required = true)
		public String nseNosis;
		@XmlElement(required = true)
		public String fuenteInferencia;
		@XmlElement(required = true)
		public String sucursalAsociada;
		@XmlElement(required = true)
		public String codigoPostal;
		@XmlElement(required = true)
		public String fechaNacimiento;
		@XmlElement(name = "ADN", required = true)
		public String adn;
		@XmlElement(required = true)
		public String esquemaInferencias;
		@XmlElement(required = true)
		public BigInteger plazoMaximo;
		@XmlElement(required = true)
		public String segmentoProcrear;
		@XmlElement(required = true)
		public String zonaProcrear;
		@XmlElement(required = true)
		public String destinoFondosProcrear;
		@XmlElement(required = true)
		public String PQ_CodPaquete;
		public Boolean productoCuentaCorriente;
		public BigDecimal lineaCuentaCorriente;
		public BigDecimal lineaTarjetaCredito;
		public BigDecimal lineaTarjetaCreditoOriginal;
		public BigDecimal lineaRotativa;
		public BigDecimal montoPrestamoPersonal;
		public BigDecimal cuotaPrestamoPersonal;
		public BigDecimal valorInmuebleProcrear;
		public BigDecimal valorLoteProcrear;
		public BigDecimal cuotaPreventa;
		public BigDecimal montoPreventa;
		public BigDecimal refuerzoPreventa;
		public BigDecimal endeudamiento;
		public BigDecimal compromiso;
		public BigDecimal tasaAplicada;
		public BigDecimal ingresos;
		public BigDecimal cuotaAplicada;
		public BigDecimal montoPrestamoPersonalSolo;
		public BigDecimal cuotaPrestamoPersonalSolo;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "tramitesVigentes")
	public static class TramitesVigentes {

		@XmlElement(required = true)
		public List<TramiteVigente> tramiteVigente;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "tramiteVigente")
		public static class TramiteVigente {

			@XmlElement(required = true)
			public String idTramite;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			@XmlElement(name = "dummy_02", required = true)
			public String dummy02;
			@XmlElement(name = "dummy_03", required = true)
			public String dummy03;
			public BigDecimal sumaAcuerdos;
			public BigDecimal sumaLimitesCompra;
			public Prestamos prestamos;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "prestamos")
	public static class Prestamos {

		@XmlElement(required = true)
		public List<PrestamoTramiteVigente> prestamo;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "prestamoTramiteVigente")
		public static class PrestamoTramiteVigente {

			@XmlElement(required = true)
			public String tipo;
			@XmlElement(required = true)
			public BigInteger plazo;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			public BigDecimal monto;
			public BigDecimal tasa;
			public BigDecimal cuota;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "datosPosicionConsolidada")
	public static class DatosPosicionConsolidada {

		@XmlElement(required = true)
		public BigInteger antiguedadClienteProdRiesgo;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		public BigDecimal sumaAcuerdos;
		public BigDecimal sumaLimitesCompra;
		public BigDecimal sumaDeudaSinGarantia;
		public BigDecimal sumaCuotasPrestamos;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "verazVariables")
	public static class VerazVariables {

		@XmlElement(required = true)
		public BigInteger scoreVeraz;
		@XmlElement(required = true)
		public String tipoScoreVeraz;
		@XmlElement(name = "maxAcuerdoEntidadesTipo_1")
		public BigDecimal maxAcuerdoEntidadesTipo1;
		@XmlElement(name = "maxAcuerdoEntidadesTipo_2")
		public BigDecimal maxAcuerdoEntidadesTipo2;
		@XmlElement(name = "maxLimiteCompraEntidadesTipo_1")
		public BigDecimal maxLimiteCompraEntidadesTipo1;
		@XmlElement(name = "maxLimiteCompraEntidadesTipo_2")
		public BigDecimal maxLimiteCompraEntidadesTipo2;
		@XmlElement(required = true)
		public String verazRiscHtml;
		@XmlElement(required = true)
		public String sectorVeraz;
		@XmlElement(required = true)
		public BigInteger cantConsultas;
		@XmlElement(required = true)
		public String fechaVigencia;
		@XmlElement(required = true)
		public String fechaInforme;
		@XmlElement(required = true)
		public String fuenteInforme;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		public BigDecimal sumaDeudaSinPrestamosSinBH;
		public BigDecimal sumaCuotasPrestamosSinBH;
		public Boolean errorServicio;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "bureauVeraz")
	public static class BureauVeraz {

		@XmlElement(required = true)
		public SvmValidacion validacion;
		@XmlElement(required = true)
		public BigInteger edad;
		@XmlElement(required = true)
		public SvmBcra bcra;
		@XmlElement(required = true)
		public SvmCheques cheques;
		@XmlElement(required = true)
		public SvmConsultas consultas;
		@XmlElement(required = true)
		public SvmObservaciones observaciones;
		@XmlElement(required = true)
		public SvmBureau bureau;
		@XmlElement(required = true)
		public SvmVariablesBH variablesBH;
		@XmlElement(name = "score_veraz", required = true)
		public BigInteger scoreVeraz;
		@XmlElement(name = "score_info", required = true)
		public String scoreInfo;
		@XmlElement(name = "deuda_ini")
		public BigDecimal deudaIni;
		@XmlElement(name = "v_informe", required = true)
		public String vInforme;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmValidacion")
	public static class SvmValidacion {

		@XmlElement(name = "val_copia", required = true)
		public String valCopia;
		@XmlElement(name = "val_doc_status", required = true)
		public BigInteger valDocStatus;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmBcra")
	public static class SvmBcra {

		@XmlElement(name = "bcr_comportamiento", required = true)
		public String bcrComportamiento;
		@XmlElement(name = "bcr_informado")
		public Boolean bcrInformado;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmCheques")
	public static class SvmCheques {

		@XmlElement(name = "che_12_sf_cant", required = true)
		public BigInteger che12SfCant;
		@XmlElement(name = "che_12_sf_cant_pag", required = true)
		public BigInteger che12SfCantPag;
		@XmlElement(name = "che_24_sf_cant", required = true)
		public BigInteger che24SfCant;
		@XmlElement(name = "che_24_sf_cant_pag", required = true)
		public BigInteger che24SfCantPag;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmConsultas")
	public static class SvmConsultas {

		@XmlElement(name = "con_comer_2", required = true)
		public BigInteger conComer2;
		@XmlElement(name = "con_comer_12", required = true)
		public BigInteger conComer12;
		@XmlElement(name = "con_finan_2", required = true)
		public BigInteger conFinan2;
		@XmlElement(name = "con_finan_6", required = true)
		public BigInteger conFinan6;
		@XmlElement(name = "con_finan_12", required = true)
		public BigInteger conFinan12;
		@XmlElement(name = "con_otros_2", required = true)
		public BigInteger conOtros2;
		@XmlElement(name = "con_otros_12", required = true)
		public BigInteger conOtros12;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmObservaciones")
	public static class SvmObservaciones {

		@XmlElement(name = "obs_grupo_10", required = true)
		public BigInteger obsGrupo10;
		@XmlElement(name = "obs_grupo_20", required = true)
		public BigInteger obsGrupo20;
		@XmlElement(name = "obs_grupo_35", required = true)
		public BigInteger obsGrupo35;
		@XmlElement(name = "obs_grupo_45", required = true)
		public BigInteger obsGrupo45;
		@XmlElement(name = "obs_grupo_50", required = true)
		public BigInteger obsGrupo50;
		@XmlElement(name = "obs_grupo_55", required = true)
		public BigInteger obsGrupo55;
		@XmlElement(name = "obs_grupo_60", required = true)
		public BigInteger obsGrupo60;
		@XmlElement(name = "obs_grupo_70", required = true)
		public BigInteger obsGrupo70;
		@XmlElement(name = "obs_mora_60_regularizadas", required = true)
		public BigInteger obsMora60Regularizadas;
		@XmlElement(name = "obs_mora_60_no_regularizadas", required = true)
		public BigInteger obsMora60NoRegularizadas;
		@XmlElement(name = "obs_informado")
		public Boolean obsInformado;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmBureau")
	public static class SvmBureau {

		@XmlElement(name = "vcb_activas", required = true)
		public BigInteger vcbActivas;
		@XmlElement(name = "vcb_alta", required = true)
		public String vcbAlta;
		@XmlElement(name = "vcb_comportamiento", required = true)
		public String vcbComportamiento;
		@XmlElement(name = "vcb_deuda_vencida")
		public BigDecimal vcbDeudaVencida;
		@XmlElement(name = "vcb_informado")
		public Boolean vcbInformado;
		@XmlElement(name = "vcb_lineas", required = true)
		public BigInteger vcbLineas;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmVariablesBH")
	public static class SvmVariablesBH {

		@XmlElement(name = "v_max_lin_cc")
		public BigDecimal vMaxLinCc;
		@XmlElement(name = "v_max_lin_cc_adherente", required = true)
		public String vMaxLinCcAdherente;
		@XmlElement(name = "v_max_lin_tc")
		public BigDecimal vMaxLinTc;
		@XmlElement(name = "v_max_lin_tc_adherente", required = true)
		public String vMaxLinTcAdherente;
		@XmlElement(name = "flag_pr_refinanciacion")
		public Boolean flagPrRefinanciacion;
		@XmlElement(name = "v_cuo_sf")
		public BigDecimal vCuoSf;
		@XmlElement(name = "v_cuo_sf_2")
		public BigDecimal vCuoSf2;
		public BigDecimal endeudamiento;
		public BigDecimal compromiso;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmDomicilio")
	public static class SvmDomicilio {

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
		public BigInteger provincia;
		@XmlElement(required = true)
		public BigInteger pais;
		@XmlElement(required = true)
		public String localidadDescriptivo;
		@XmlElement(required = true)
		public String provinciaDescriptivo;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "svmTelemail")
	public static class SvmTelemail {

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
	@XmlRootElement(name = "documentaciones")
	public static class Documentaciones {

		@XmlElement(required = true)
		public List<Documentacion> documentacion;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "riesgoNetVariables")
	public static class RiesgoNetVariables {

		@XmlElement(required = true)
		public String resolucionRiesgoNet;
		@XmlElement(required = true)
		public String tipoConexion;
		@XmlElement(required = true)
		public String informeOnLine;
		@XmlElement(required = true)
		public BigInteger cantConsultas;
		@XmlElement(required = true)
		public String georefZona;
		@XmlElement(required = true)
		public String dniConyuge;
		@XmlElement(required = true)
		public String fraudes;
		@XmlElement(required = true)
		public BigInteger flagsGrisRN;
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
		@XmlElement(required = true)
		public String fechaVigencia;
		@XmlElement(required = true)
		public String fechaInforme;
		@XmlElement(required = true)
		public String fuenteInforme;
		public boolean errorServicio;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "verazBehavior")
	public static class VerazBehavior {

		@XmlElement(required = true)
		public BigInteger idSecuencia;
		@XmlElement(required = true)
		public String perReferencia;
		@XmlElement(required = true)
		public String documento;
		@XmlElement(required = true)
		public String tipoDeDocumento;
		@XmlElement(required = true)
		public String apellidoNombres;
		@XmlElement(required = true)
		public String sexo;
		@XmlElement(required = true)
		public BigInteger scoreVeraz;
		@XmlElement(required = true)
		public BigInteger riesgo6;
		@XmlElement(required = true)
		public BigInteger actividadTc;
		@XmlElement(required = true)
		public BigInteger actOpenTc;
		@XmlElement(required = true)
		public BigInteger actOpenSg;
		@XmlElement(required = true)
		public BigInteger antiguedadTc;
		@XmlElement(required = true)
		public BigInteger cobCant;
		@XmlElement(required = true)
		public BigInteger cobStatus;
		@XmlElement(required = true)
		public BigInteger hit;
		@XmlElement(required = true)
		public String periodoProceso;
		@XmlElement(required = true)
		public String fechaUltimaRecepcion;
		public BigDecimal usoTcPromedio3;
		public BigDecimal usoTcCantA;
		public BigDecimal usoTcLimiteMax;
		public BigDecimal usoCcLimiteMax;
		public BigDecimal usoSgExigible;
		public BigDecimal usoGpExigible;
		public BigDecimal usoGhExigible;
		public BigDecimal cobVencido;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "ingresosInferidos")
	public static class IngresosInferidos {

		@XmlElement(required = true)
		public List<IngresoInferido> ingresoInferido;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "ingresoInferido")
		public static class IngresoInferido {

			@XmlElement(required = true)
			public String codTipoIngresoCore;
			@XmlElement(required = true)
			public String codSubTipoIngresoCore;
			@XmlElement(required = true)
			public String codFuenteInferenciaCore;
			public BigDecimal ingresoComputado;
		}
	}
}
