package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos.Producto;
import com.github.jknack.handlebars.Handlebars.Utils;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlCampaniasWF;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.DatosTarjetaCredito;
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales.SucursalOnboardingEsales;
import ar.com.hipotecario.backend.util.Validadores;
import ar.com.hipotecario.backend.servicio.api.ventas.MotorScoringSimulacion.TarjetaOfrecida;

@SuppressWarnings("serial")
public class SesionBB extends Sesion {

	/* ========== ATRIBUTOS GENERALES =========== */
	public String token;
	public String operationVU;
	public String ofertaElegida;
	public String subProducto;
	public String estado;
	public Integer reintentos;
	public String aceptartyc;
	public String tdFisica;
	public String codeVU;
	public Boolean finalizarEnEjecucion = false;

	/* ========== ATRIBUTOS VENTAS =========== */
	public String idSolicitud;
	public String idTarjeta;
	public String letraTC;
	public BigDecimal limite;
	public Integer ingresoNeto;

	/* ========== ATRIBUTOS MOTOR =========== */
	public String resolucionMotorDeScoring;
	public String modoAprobacion;
	public String codigoPaqueteMotor;
	public TarjetaOfertaBB tarjetaOferta;

	/* ========== ATRIBUTOS OTP =========== */
	public Integer intentosOtp;
	public Fecha fechaEnvioOtpMail = Fecha.nunca();
	public Fecha fechaEnvioOtpSms = Fecha.nunca();
	public String mail;
	public String codArea;
	public String celular;
	public String claveOtp;
	public String stateId;
	public String cookie;

	/* ========== ATRIBUTOS BASICOS =========== */
	public String nombre;
	public String apellido;
	public String genero;
	public Fecha fechaNacimiento;
	public String nacionalidad;
	public String idNacionalidad;
	public String paisNacimiento;
	public String idPaisNacimiento;
	public String idTipoIDTributario;
	public String ejemplar;

	/* ========== ATRIBUTOS DE ENTREGA =========== */
	public DomicilioBB domicilioLegal = new DomicilioBB();
	public DomicilioBB domicilioPostal = new DomicilioBB();
	public String formaEntrega;
	public Integer idSucursal;

	/* ========== ATRIBUTOS ADICIONALES =========== */
	public String idEstadoCivil;
	public String idSituacionLaboral;
	public String idCantidadNupcias;
	public String tipoSitLaboral;
	public String idSubtipoEstadoCivil;
	public String SubtipoEstadoCivilDescr;
	public ConyugeBB conyuge = new ConyugeBB();
	public Boolean telefonoOtpValidado;
	public Boolean emailOtpValidado;
	public String tokenFirebase;
	public String plataforma;
	public Boolean motorIndicador;
	public String bbInversorAceptada;
	public String estadoCajaUsd;
	public String usuarioVU;
	public String idDispositivo;
	public String sucursalOnboarding;
	public BigDecimal latitud;
	public BigDecimal longitud;
	public Boolean esExpuestaPolitica;
	public Boolean esSujetoObligado;
	public Boolean esFatcaOcde;
	public Boolean lavadoDinero;
	public String versionPlataforma;
	public Boolean tdVirtual;
	public Boolean tcVirtual;
	public String cuilReferido;
	public String adjustAdid;
	public String adjustGpsAdid;

	/* ========== VALIDACIONES =========== */
	public Boolean valDatosPersonales = false;
	public Boolean checkTdFisica = false;
	public Boolean checkTdFisicaCgu = false;
	public Boolean checkCuentaSueldo = false;
	private boolean guardarCobisTemprano = false;
	private boolean ventaTemprana = false;

	/* ========== ATRIBUTOS DEL FLUJO =========== */
	private String atFlujoName = "";
	private String atParamUrlVU = "";
	private String atParamKeyPublicaVU = "";
	private String atParamKeyPrivadaVU = "";
	private Boolean atParamHorarioBatch = false;
	private Boolean atParamCuentaSueldo = false;
	private Boolean atParamEnvioAndriani = false;
	private Boolean atParamPrevencionStandalone = false;
	private Boolean atParamOtpV2 = false;
	private Boolean atParamTcOnline = false;

	public String getCobis(ContextoBB contexto) {
		try{
			if(cobisPositivo()) return idCobis;
			Persona persona = ApiPersonas.persona(contexto, cuil).tryGet();
			if(persona != null && !persona.idCliente.startsWith("-")){
				idCobis = persona.idCliente;
				saveSesion();
				return persona.idCliente;
			}
		}
		catch(Exception e){}
		return "-1";
	}

	/* ========== CLASES =========== */
	public static class TarjetaOfertaBB extends Base {
		public Integer marca;
		public String distribucionDesc;
		public Integer producto;
		public Integer grupo;
		public String afinidadDesc;
		public Integer modLiq;

		public TarjetaOfertaBB clonar() {
			return Util.clonar(this, TarjetaOfertaBB.class);
		}

		public static TarjetaOfertaBB crear(Objeto tarjetaOferta) {
			return Util.fromJson(tarjetaOferta.toJson(), TarjetaOfertaBB.class);
		}
	}

	public static class ConyugeBB extends Base {
		public String nombres;
		public String apellido;
		public String genero;
		public String numeroDocumento;
		public String cuil;
		public Fecha fechaNacimiento;
		public String nacionalidad;
		public String paisResidencia;
		public String idNacionalidad;
		public String idPaisResidencia;

		public ConyugeBB clonar() {
			return Util.clonar(this, ConyugeBB.class);
		}

		public String tipoDocumento() {
			if (Util.empty(numeroDocumento))
				return "";
			if (Validadores.esExtranjero(numeroDocumento))
				return GeneralBB.TIPO_DOC_EXTRANJERO;
			return GeneralBB.TIPO_DOC_NACIONAL;
		}

		public String cuil() {
			if (Util.empty(numeroDocumento) || Util.empty(nombres) || Util.empty(apellido))
				return "";
			String nombreCompleto = apellido + " " + nombres;
			Cuils cuils = ApiPersonas.cuils(null, numeroDocumento, nombreCompleto).tryGet();
			if (cuils == null || cuils.isEmpty())
				return "";
			if (cuils.get(0) == null)
				return null;
			return cuils.get(0).cuil;
		}

		public static ConyugeBB crear(Objeto conyuge) {
			return Util.fromJson(conyuge.toJson(), ConyugeBB.class);
		}
	}

	public static class DomicilioBB extends Base {
		public String calle;
		public String numeroCalle;
		public String piso;
		public String dpto;
		public String cp;
		public String ciudad;
		public String localidad;
		public String provincia;
		public String pais;
		public String idCiudad;
		public String idProvincia;
		public String idPais;

		public DomicilioBB clonar() {
			return Util.clonar(this, DomicilioBB.class);
		}

		public static DomicilioBB crear(Objeto domicilio) {
			DomicilioBB dom = Util.fromJson(domicilio.toJson(), DomicilioBB.class);
			if (dom == null)
				return null;
			dom.ciudad = Validadores.filtroUpper(dom.ciudad);
			dom.localidad = Validadores.filtroUpper(dom.localidad);
			dom.provincia = Validadores.filtroUpper(dom.provincia);
			dom.pais = Validadores.filtroUpper(dom.pais);

			return dom;
		}

		public static CiudadWF ciudadPorCP(Contexto contexto, String cp) {
			if (empty(cp))
				return null;
			CiudadesWF ciudad = SqlCampaniasWF.ciudades(contexto, cp).tryGet();
			if (ciudad == null || ciudad.isEmpty())
				return null;
			if (ciudad.get(0) == null)
				return null;
			return ciudad.get(0);
		}

		public static CiudadWF ciudadPorId(Contexto contexto, String id) {
			if (empty(id))
				return null;
			CiudadWF ciudad = SqlCampaniasWF.ciudad(contexto, id).tryGet();
			if (empty(ciudad))
				return null;
			return ciudad;
		}
	}

	/* ========== INICIALIZACION =========== */
	public void crearSesion() {
		atFlujoName = "";
		Sesion sesion = contexto.sesion(Sesion.class);
		sesion.cuil = this.cuil;
		sesion.idCobis = this.idCobis;
		sesion.numeroDocumento = this.numeroDocumento;
		sesion.usuario = this.usuario;
		sesion.sucursal = this.sucursal;
		sesion.fechaLogin = this.fechaLogin;
		sesion.fechaUltimaActividad = this.fechaLogin;
		sesion.save();
		this.fechaUltimaActividad = Fecha.ahora();
		this.save();
	}

	/* ========== MANEJO DE SESION =========== */
	public Boolean usuarioLogueado() {
		return cuil != null;
	}
	
	public void limpiarSesion() {
		if (usuarioLogueado()) {
			atFlujoName = "";
			delete();
		}
	}

	public void actualizarFechaUltimaActividad() {
		fechaUltimaActividad = Fecha.ahora();
		this.saveSesion();
	}

	public void actualizarEstado(String estadoNuevo) {
		
		if (EstadosBB.FINALIZAR_OK.equals(estado)) {
			return;
		}
		
		estado = estadoNuevo;
		this.saveSesion();
	}

	public void actualizarEstadoError(String estadoNuevo) {
		estado = "ERROR_" + estadoNuevo;
		reintentos = reintentos + 1;
		this.saveSesion();
	}

	public Boolean expirada() {
		Fecha fechaExpiracion = fechaUltimaActividad.sumarHoras(GeneralBB.SESION_EXPIRACION);
		return fechaExpiracion.esAnterior(Fecha.ahora());
	}

	public Boolean maximoReintentos() {
		if (reintentos == null)
			return false;
		return reintentos >= GeneralBB.MAXIMO_REINTENTOS;
	}

	@Override
	public void save() {
		fechaUltimaActividad = Fecha.ahora();
		super.save();
		if (!Util.empty(token)) {
			Boolean fueGuardada = SqlEsales.guardarSesion(contexto, this).get();
			if (fueGuardada == null || !fueGuardada) {
				System.out.println("SESION_NO_GUARDADA");
			}

			Boolean fueGuardadaBB2 = SqlEsales.guardarSesionBB2(contexto, this).get();
			if (fueGuardadaBB2 == null || !fueGuardadaBB2) {
				System.out.println("SESION_BB2_NO_GUARDADA " + "cuil : " + this.cuil + ", token_sesion: " + this.token);
			}
		}
	}

	public void saveSesion() {
		fechaUltimaActividad = Fecha.ahora();
		super.save();
		if (!Util.empty(token)) {
			Boolean fueGuardada = SqlEsales.guardarSesion(contexto, this).get();
			if (fueGuardada == null || !fueGuardada) {
				System.out.println("SESION_NO_GUARDADA");
			}
		}
	}

	public void saveSesionbb2() {
		super.save();
		if (!Util.empty(token)) {
			Boolean fueGuardadaBB2 = SqlEsales.guardarSesionBB2(contexto, this).get();
			if (fueGuardadaBB2 == null || !fueGuardadaBB2) {
				System.out.println("SESION_BB2_NO_GUARDADA " + "cuil : " + this.cuil + ", token_sesion: " + this.token);
			}
		}
	}
	
	public void saveCache() {
		super.save();
	}

	/* ========== TIPO FLUJO =========== */

	public Boolean buhoInversorAceptada() {
		return GeneralBB.CUENTA_INVERSOR_ACEPTADA.equals(bbInversorAceptada);
	}

	public Boolean esDeLaOferta(Integer ofertaNumero) {
		if (Util.empty(ofertaElegida))
			return false;
		return ofertaElegida.equals(ofertaNumero.toString());
	}

	public Boolean esStandalone() {
		return esDeLaOferta(GeneralBB.BUHO_INICIA) || esDeLaOferta(GeneralBB.BUHO_CGU) || esDeLaOferta(GeneralBB.BUHO_INICIA_HML) || esDeLaOferta(GeneralBB.CS_FACIL_PACK) || esDeLaOferta(GeneralBB.BUHO_INICIA_INVERSOR);
	}

	public Boolean sucursalVacio(Integer pos) {

		if (Util.empty(sucursalOnboarding) || !sucursalOnboarding.contains("|") || sucursalOnboarding.split("\\|").length < pos) {
			return true;
		}

		String valor = sucursalOnboarding.split("\\|")[pos - 1];
		return Utils.isEmpty(valor);
	}

	public String getFlujo() {
		int pos = 1;
		return sucursalVacio(pos) ? GeneralBB.FLUJO_DEFAULT : sucursalOnboarding.split("\\|")[pos - 1];
	}

	public String getSucursal() {
		int pos = 2;
		return sucursalVacio(pos) ? "" : sucursalOnboarding.split("\\|")[pos - 1];
	}

	public String getCanal() {
		int pos = 3;
		return sucursalVacio(pos) ? GeneralBB.CANAL_DEFAULT : sucursalOnboarding.split("\\|")[pos - 1];
	}

	public Boolean esFlujoTcv() {
		return GeneralBB.FLUJO_TCV.equals(getFlujo());
	}

	public Boolean esFlujoInversiones() {
		return GeneralBB.FLUJO_INVERSIONES.equals(getFlujo());
	}

	public boolean tieneCAPesos(ContextoBB contexto){
		if(!cobisPositivo()) return false;

		Productos productos = ApiProductos.productosVigentes(contexto, idCobis).tryGet();
		if(productos != null) {
			for (Producto producto : productos) {
				if (producto.cajaPesosActiva()) {
					return true;
				}
			}
		}

		return false;
	}
	public String descripcionOferta(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		BBPaquetesBuhobank paquetes = SqlBuhoBank.obtenerPaquetes(contexto, sesion.getFlujo()).tryGet();
		BBPaqueteBuhobank paquete = BBPaquetesBuhobank.buscarPaquete(paquetes, sesion.letraTC, sesion.numeroPaquete());

		String ofertaElegida = paquete != null ? paquete.nombre : "OFERTA";

		if (buhoInversorAceptada()) {
			ofertaElegida += "_INVERSOR";
		}

		if (esTdVirtual()) {
			ofertaElegida += "_TD_VIRTUAL";
		}

		return ofertaElegida;
	}

	public String obtenerNombreOferta() {

		String ofertaElegida = "";

		if (esDeLaOferta(GeneralBB.BUHO_INICIA)) {
			ofertaElegida = "BUHO_INICIA";
		} else if (esDeLaOferta(GeneralBB.BUHO_CGU)) {

			if (esTdVirtual()) {
				ofertaElegida = "CGU_VIRTUAL";
			} else {
				ofertaElegida = "CGU";

			}
		} else if (esDeLaOferta(GeneralBB.BUHO_INICIA_INVERSOR)) {
			ofertaElegida = "BUHO_INICIA_INVERSOR";
		} else if (esDeLaOferta(GeneralBB.CS_FACIL_SUELDO)) {
			ofertaElegida = "CS_FACIL_SUELDO";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_BUHO_PACK)) {
			ofertaElegida = "INTERNACIONAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_GOLD)) {
			ofertaElegida = "GOLD";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_PLATINUM)) {
			ofertaElegida = "PLATINUM";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_BLACK)) {
			ofertaElegida = "BLACK";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_BUHO_PACK_VIRTUAL)) {
			ofertaElegida = "INTERNACIONAL_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_GOLD_VIRTUAL)) {
			ofertaElegida = "GOLD_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_PLATINUM_VIRTUAL)) {
			ofertaElegida = "PLATINUM_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_BLACK_VIRTUAL)) {
			ofertaElegida = "BLACK_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR_BLACK)) {
			ofertaElegida = "BLACK_EMPRENDEDOR";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR) && GeneralBB.LETRA_PAQUETE_BUHO_PACK.equals(letraTC)) {
			ofertaElegida = "INTERNACIONAL_EMPRENDEDOR";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR) && GeneralBB.LETRA_PAQUETE_GOLD.equals(letraTC)) {
			ofertaElegida = "GOLD_EMPRENDEDOR";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR) && GeneralBB.LETRA_PAQUETE_PLATINUM.equals(letraTC)) {
			ofertaElegida = "PLATINUM_EMPRENDEDOR";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR_BLACK_VIRTUAL)) {
			ofertaElegida = "BLACK_EMPRENDEDOR_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR_VIRTUAL) && GeneralBB.LETRA_PAQUETE_BUHO_PACK.equals(letraTC)) {
			ofertaElegida = "INTERNACIONAL_EMPRENDEDOR_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR_VIRTUAL) && GeneralBB.LETRA_PAQUETE_GOLD.equals(letraTC)) {
			ofertaElegida = "GOLD_EMPRENDEDOR_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.PAQUETE_EMPRENDEDOR_VIRTUAL) && GeneralBB.LETRA_PAQUETE_PLATINUM.equals(letraTC)) {
			ofertaElegida = "PLATINUM_EMPRENDEDOR_VIRTUAL";
		} else if (esDeLaOferta(GeneralBB.BUHO_INICIA_HML)) {
			ofertaElegida = "BUHO_INICIA_LIBERTAD";
		} else if (esDeLaOferta(GeneralBB.CS_PAQUETE_BUHO_PACK)) {
			ofertaElegida = "CS_INTERNACIONAL";
		} else if (esDeLaOferta(GeneralBB.CS_PAQUETE_GOLD)) {
			ofertaElegida = "CS_PAQUETE_GOLD";
		} else if (esDeLaOferta(GeneralBB.CS_PAQUETE_PLATINUM)) {
			ofertaElegida = "CS_PAQUETE_PLATINUM";
		} else if (esDeLaOferta(GeneralBB.CS_PAQUETE_BLACK)) {
			ofertaElegida = "CS_PAQUETE_BLACK";
		}

		if (contexto.esProduccion()) {

			if (esDeLaOferta(GeneralBB.PROD_NRO_PAQUETE_HML_INTERNACIONAL)) {
				ofertaElegida = "INTERNACIONAL_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.PROD_NRO_PAQUETE_HML_GOLD)) {
				ofertaElegida = "GOLD_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.PROD_NRO_PAQUETE_HML_PLATINUM)) {
				ofertaElegida = "PLATINUM_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.PROD_NRO_PAQUETE_HML_SIGNATURE)) {
				ofertaElegida = "BLACK_LIBERTAD";
			}
		} else {
			if (esDeLaOferta(GeneralBB.HOMO_NRO_PAQUETE_HML_INTERNACIONAL)) {
				ofertaElegida = "INTERNACIONAL_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.HOMO_NRO_PAQUETE_HML_GOLD)) {
				ofertaElegida = "GOLD_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.HOMO_NRO_PAQUETE_HML_PLATINUM)) {
				ofertaElegida = "PLATINUM_LIBERTAD";
			} else if (esDeLaOferta(GeneralBB.HOMO_NRO_PAQUETE_HML_SIGNATURE)) {
				ofertaElegida = "BLACK_LIBERTAD";
			}
		}

		return ofertaElegida;
	}

	public Boolean esCGU() {
		return esDeLaOferta(GeneralBB.BUHO_CGU);
	}

	public Boolean esCuentaSueldo() {
		return esDeLaOferta(GeneralBB.CS_FACIL_SUELDO) || esDeLaOferta(GeneralBB.CS_PAQUETE_BUHO_PACK) || esDeLaOferta(GeneralBB.CS_PAQUETE_GOLD) || esDeLaOferta(GeneralBB.CS_PAQUETE_PLATINUM) || esDeLaOferta(GeneralBB.CS_PAQUETE_BLACK);
	}

	public String tipoOferta() {
		if (Util.empty(ofertaElegida))
			return null;

		String letra = "0";
		if (!esStandalone() && !Util.empty(letraTC)) {
			letra = letraTC;
		}

		String subProd = !Util.empty(subProducto) ? subProducto : GeneralBB.SUB_BUHO_PUNTOS;
		tdFisica = Util.empty(tdFisica) ? GeneralBB.VISUALIZA_N : tdFisica;

		return String.format("%s:%s:%s:%s:%s", ofertaElegida, letra, obtenerNombreOferta(), subProd, tdFisica);
	}

	/* ========== METODOS =========== */
	public String getPrimerNombre() {
		return ucFirst(nombre);
	}
	
	public static String ucFirst(String str) {
		try {
			str = str.split(" ")[0];
		    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
		}
		catch(Exception e) {
			return str;
		}
	}

	public String uuid() {
		return java.util.UUID.randomUUID().toString();
	}

	public Integer tiempoReenvioOtp() {
		Integer reintentosCalc = reintentos;
		if (reintentos == null)
			reintentosCalc = 0;
		return (GeneralBB.OTP_EXPIRACION + 2 * reintentosCalc) * 60 * 1000;
	}

	public Boolean validoOtpMail() {
		return !fechaEnvioOtpMail.isNull() && !fechaEnvioOtpMail.sumarMinutos(GeneralBB.OTP_EXPIRACION).esAnterior(Fecha.ahora());
	}

	public Boolean validoOtpSms() {
		return !fechaEnvioOtpSms.isNull() && !fechaEnvioOtpSms.sumarMinutos(GeneralBB.OTP_EXPIRACION).esAnterior(Fecha.ahora());
	}

	public String embozado() {
		if (Util.empty(nombre) || Util.empty(apellido))
			return "";

		int longitud = apellido.length() + nombre.length() + 1;
		if (longitud <= 19) {
			return apellido.toUpperCase() + "/" + nombre.toUpperCase();
		}

		if (apellido.contains(" ")) {
			apellido = apellido.split("\\s+")[0];
		}

		if (apellido.length() > 12) {
			apellido = apellido.substring(0, 12);
		}

		String embozado = apellido.toUpperCase() + "/" + nombre.toUpperCase();

		return embozado.length() > 19 ? embozado.substring(0, 19) : embozado;
	}

	public String codAreaCelular() {
		if (Util.empty(codArea))
			return "";
		return Telefono.obtenerCodigoArea(codArea);
	}

	public String caracteristicaCelular() {
		if (Util.empty(codArea) || Util.empty(celular))
			return "";
		return Telefono.obtenerCaracteristica(codArea, celular);
	}

	public String numeroCelular() {
		if (Util.empty(codArea) || Util.empty(celular))
			return "";
		return Telefono.obtenerNumero(codArea, celular);
	}

	public String telefono() {
		if (Util.empty(codArea) || Util.empty(celular))
			return "";
		String codigoArea = codAreaCelular();
		String caracteristica = caracteristicaCelular();
		String numero = numeroCelular();

		String telefono = codigoArea + caracteristica + numero;
		while (telefono.startsWith("0")) {
			telefono = telefono.substring(1);
		}
		return telefono;
	}

	public Boolean cobisPositivo() {
		if (Util.empty(idCobis))
			return false;
		return !idCobis.startsWith("-");
	}

	public String dni() {
		if (Util.empty(cuil) || cuil.length() < 10)
			return "";
		return cuil.substring(2, 10);
	}

	public String tipoDocumento() {
		String nroDocumento = dni();
		if (Util.empty(nroDocumento))
			return "";
		if (Validadores.esExtranjero(nroDocumento))
			return GeneralBB.TIPO_DOC_EXTRANJERO;
		return GeneralBB.TIPO_DOC_NACIONAL;
	}

	public Boolean esExtranjero() {
		return GeneralBB.TIPO_DOC_EXTRANJERO.equals(tipoDocumento());
	}

	public Boolean esNacionalidadInvalida(String idNacionalidad) {
		boolean resultado = false;
		resultado |= GeneralBB.TIPO_DOC_EXTRANJERO.equals(tipoDocumento()) && GeneralBB.DEFAULT_NACIONALIDAD.equals(idNacionalidad);
		resultado |= !GeneralBB.TIPO_DOC_EXTRANJERO.equals(tipoDocumento()) && !GeneralBB.DEFAULT_NACIONALIDAD.equals(idNacionalidad);
		return resultado;
	}

	public Boolean casada() {
		if (Util.empty(idEstadoCivil))
			return false;
		return GeneralBB.CASADA.equals(idEstadoCivil.toUpperCase());
	}

	public Boolean emailValido() {
		if (Util.empty(mail))
			return false;
		return Validadores.esMailValido(mail);
	}

	public Boolean resolucionAprobada() {
		if (Util.empty(resolucionMotorDeScoring)){
			return false;
		}

		return resolucionMotorDeScoring.equals(Resolucion.APROBAR_VERDE);
	}

	public Boolean aSucursal() {
		if (Util.empty(formaEntrega))
			return false;
		return formaEntrega.equals(GeneralBB.ENTREGA_SUCURSAL);
	}
	public Boolean aSucursalAndreani() {
		if (Util.empty(formaEntrega))
			return false;
		return formaEntrega.equals(GeneralBB.ENTREGA_DOM_ANDREANI);
	}
	public TarjetaOfertaBB crearTarjetaOferta(TarjetaOfrecida tarjetaOfrecida) {
		if (Util.empty(tarjetaOfrecida))
			return null;

		TarjetaOfertaBB tarjetaOf = new TarjetaOfertaBB();
		tarjetaOf.marca = GeneralBB.MARCA_TC_DUENIOS;
		tarjetaOf.distribucionDesc = tarjetaOfrecida.CodigoDistribucion;
		tarjetaOf.producto = Util.integer(tarjetaOfrecida.Producto);
		tarjetaOf.grupo = Util.integer(tarjetaOfrecida.Cartera);
		tarjetaOf.afinidadDesc = String.valueOf(tarjetaOfrecida.GrupoAfinidad);
		tarjetaOf.modLiq = Util.integer(tarjetaOfrecida.ModeloLiquidacion);

		return tarjetaOf;
	}

	public DatosTarjetaCredito obtenerDatosTC() {
		if (Util.empty(tarjetaOferta))
			return null;

		Integer marca = tarjetaOferta.marca;
		String distribucionDesc = tarjetaOferta.distribucionDesc;
		Integer producto = tarjetaOferta.producto;
		Integer grupo = tarjetaOferta.grupo;
		String afinidadDesc = tarjetaOferta.afinidadDesc;
		Integer modLiq = tarjetaOferta.modLiq;

		return DatosTarjetaCredito.crear(contexto, marca, distribucionDesc, producto, grupo, afinidadDesc, modLiq);
	}

	public String cantidadNupcias() {
		if (Util.empty(idCantidadNupcias))
			return "";
		if (GeneralBB.NUPCIAS_PRIMERAS.equals(idCantidadNupcias))
			return "PRIMERAS";
		if (GeneralBB.NUPCIAS_SEGUNDAS.equals(idCantidadNupcias))
			return "SEGUNDAS";
		if (GeneralBB.NUPCIAS_TERCERAS.equals(idCantidadNupcias))
			return "TERCERAS";
		if (GeneralBB.NUPCIAS_CUARTAS.equals(idCantidadNupcias))
			return "CUARTAS";

		return "";
	}

	public String subtipoEstadoCivil() {
		if (Util.empty(idSubtipoEstadoCivil))
			return "";
		if (GeneralBB.UNION_CIVIL_COMUNIDAD.equals(idSubtipoEstadoCivil))
			return "COMUNIDAD";
		if (GeneralBB.UNION_CIVIL_SEP_BIENES.equals(idSubtipoEstadoCivil))
			return "SEPARACION DE BIENES";

		return "";
	}

	public Boolean estadoVUOK() {
		return EstadosBB.VU_PERSON_OK.equals(estado) || EstadosBB.VU_TOTAL_OK.equals(estado);
	}

	public Boolean esErrorFueraDeServicio(ContextoBB contexto) {
		boolean esFueraServicio =  EstadosBB.ERROR_FUERA_DE_SERVICIO.equals(estado);
		if(esFueraServicio){
			LogBB.eventoHomo(contexto, "ERROR_FUERA_DE_SERVICIO");
		}

		return esFueraServicio;
	}

	public Boolean esBatch() {
		return !EstadosBB.FINALIZAR_OK.equals(estado);
	}

	public Objeto getSesion() {
		Objeto sesion = new Objeto();
		sesion.set("numeroDocumento", numeroDocumento);
		sesion.set("nombre", nombre);
		sesion.set("apellido", apellido);
		sesion.set("genero", genero);
		sesion.set("paisNacimiento", paisNacimiento);
		sesion.set("nacionalidad", nacionalidad);
		sesion.set("fechaNacimiento", fechaNacimiento != null ? fechaNacimiento.string("dd/MM/yyyy") : null);
		sesion.set("mail", mail);
		sesion.set("codArea", codArea);
		sesion.set("celular", celular);

		sesion.set("domicilioLegal.calle", domicilioLegal.calle);
		sesion.set("domicilioLegal.numeroCalle", domicilioLegal.numeroCalle);
		sesion.set("domicilioLegal.piso", domicilioLegal.piso);
		sesion.set("domicilioLegal.dpto", domicilioLegal.dpto);
		sesion.set("domicilioLegal.cp", domicilioLegal.cp);
		sesion.set("domicilioLegal.ciudad", domicilioLegal.ciudad);
		sesion.set("domicilioLegal.localidad", domicilioLegal.localidad);
		sesion.set("domicilioLegal.provincia", domicilioLegal.provincia);
		sesion.set("domicilioLegal.pais", domicilioLegal.pais);
		
		sesion.set("domicilioPostal.calle", domicilioPostal.calle);
		sesion.set("domicilioPostal.numeroCalle", domicilioPostal.numeroCalle);
		sesion.set("domicilioPostal.piso", domicilioPostal.piso);
		sesion.set("domicilioPostal.dpto", domicilioPostal.dpto);
		sesion.set("domicilioPostal.cp", domicilioPostal.cp);
		sesion.set("domicilioPostal.ciudad", domicilioPostal.ciudad);
		sesion.set("domicilioPostal.localidad", domicilioPostal.localidad);
		sesion.set("domicilioPostal.provincia", domicilioPostal.provincia);
		sesion.set("domicilioPostal.pais", domicilioPostal.pais);

		return sesion;
	}

	public boolean esRelacionDependencia() {
		return "1".equals(idSituacionLaboral);
	}

	String plataforma() {
		return !Utils.isEmpty(plataforma) ? plataforma.toUpperCase() : GeneralBB.PLATAFORMA_ANDROID;
	}

	public boolean esAndroid() {
		return GeneralBB.PLATAFORMA_ANDROID.equals(plataforma());
	}

	public boolean esTdVirtual() {
		return !Utils.isEmpty(tdVirtual) && tdVirtual;
	}

	public boolean esSoloTdVirtual() {
		return esTdVirtual() && esStandalone();
	}

	public boolean esTcVirtual() {
		return !Utils.isEmpty(tcVirtual) && tcVirtual;
	}

	public boolean esTdFisica() {
		return GeneralBB.VISUALIZA_S.equals(tdFisica);
	}

	public boolean tieneDomVirtual() {

		if (esTdFisica()) {
			return false;
		}

		if (esTdVirtual() && esStandalone()) {
			return true;
		}

		return esTcVirtual() && esTdVirtual();
	}

	public String inicializarFlujo(ContextoBB contexto, String url) {
		SucursalOnboardingEsales sucursal = SqlEsales.obtenerSucursalesByQr(contexto, url).tryGet();
		if (sucursal != null && sucursal.habilitado) {
			return sucursal.flujo;
		}

		return GeneralBB.FLUJO_DEFAULT;
	}

	private void setSucursalOnboarding(String flujo) {
		sucursalOnboarding = (Utils.isEmpty(flujo) ? GeneralBB.FLUJO_DEFAULT : flujo) + "|";
	}

	public void setFlujo(ContextoBB contexto, String flujo, String plataformaStr) {
		setSucursalOnboarding(flujo);
		plataforma = plataformaStr;
		validarAtributos(contexto);
	}

	public Integer numeroPaquete() {

		if (Util.empty(ofertaElegida)) {
			return null;
		}

		try {
			return Integer.parseInt(ofertaElegida);
		} catch (Exception e) {
			return null;
		}
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {

	}

	public void setAtributos(ContextoBB contexto) {
		String flujoBB = getFlujo();
		String plataformaBB = plataforma();
		atFlujoName = flujoBB;

		BBParametriasBuhobank parametrias = SqlBuhoBank.obtenerParametrias(contexto, flujoBB).tryGet();
		atParamUrlVU = BBParametriasBuhobank.obtenerValor(parametrias, BBParametriasBuhobank.URL_BACKOFFICE_VU, plataformaBB);
		atParamKeyPublicaVU = BBParametriasBuhobank.obtenerValor(parametrias, BBParametriasBuhobank.KEY_BACKOFFICE_VU, plataformaBB);
		atParamKeyPrivadaVU = BBParametriasBuhobank.obtenerValor(parametrias, BBParametriasBuhobank.KEY_PRIVADA_BACKOFFICE_VU, plataformaBB);
		atParamHorarioBatch = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.HORARIO_BATCH, plataformaBB);
		atParamOtpV2 = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.OTP_V2, plataformaBB);
		atParamCuentaSueldo = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.CUENTA_SUELDO, plataformaBB);
		atParamEnvioAndriani = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.ENVIO_ANDRIANI, plataformaBB);
		atParamPrevencionStandalone = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.PREVENCION_STANDALONE, plataformaBB);
		atParamTcOnline = BBParametriasBuhobank.estaPrendido(parametrias, BBParametriasBuhobank.TC_ONLINE, plataformaBB);

		if(Utils.isEmpty(atParamUrlVU) || Utils.isEmpty(atParamKeyPublicaVU)){
			BBParametriasBuhobank parametriasDefault = SqlBuhoBank.obtenerParametrias(contexto, GeneralBB.FLUJO_ONBOARDING).tryGet();
			atParamUrlVU = BBParametriasBuhobank.obtenerValor(parametriasDefault, BBParametriasBuhobank.URL_BACKOFFICE_VU, plataformaBB);
			atParamKeyPublicaVU = BBParametriasBuhobank.obtenerValor(parametriasDefault, BBParametriasBuhobank.KEY_BACKOFFICE_VU, plataformaBB);
		}
	}

	private void validarAtributos(ContextoBB contexto) {
		if (!getFlujo().equals(atFlujoName)) {
			setAtributos(contexto);
		}
	}

	public String getUrlVU(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamUrlVU;
	}

	public String getParamKeyPublicaVU(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamKeyPublicaVU;
	}

	public String getParamKeyPrivadaVU(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamKeyPrivadaVU;
	}

	public boolean getParamHorarioBatch(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamHorarioBatch;
	}

	public boolean getParamCuentaSueldo(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamCuentaSueldo;
	}
	
	public boolean getParamOtpV2(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamOtpV2;
	}

	public boolean getParamEnvioAndriani(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamEnvioAndriani;
	}

	public boolean getParamPrevencionStandalone(ContextoBB contexto) {
		validarAtributos(contexto);
		return atParamPrevencionStandalone;
	}

	public boolean getParamTcOnline(ContextoBB contexto) {
		validarAtributos(contexto);
		return esFlujoTcv() && atParamTcOnline;
	}

	public boolean getCheckTdFisica() {
		return !Utils.isEmpty(checkTdFisica) && checkTdFisica;
	}
	
	public boolean getCheckTdFisicaCgu() {
		return !Utils.isEmpty(checkTdFisicaCgu) && checkTdFisicaCgu;
	}

	public boolean getCheckCuentaSueldo() {
		return !Utils.isEmpty(checkCuentaSueldo) && checkCuentaSueldo;
	}

	public boolean setCheckCuentaSueldo(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		checkCuentaSueldo = false;
		if (!sesion.getParamCuentaSueldo(contexto)) {
			return false;
		}

		try {

			String tipoOferta = sesion.tipoOferta();
			if (tipoOferta == null || !tipoOferta.contains("EMPRENDEDOR")) {

				int ofertaElegida = Integer.parseInt(sesion.ofertaElegida);
				BBPaqueteBuhobank paqCSueldo = SqlBuhoBank.obtenerCuentaSueldo(contexto, ofertaElegida).tryGet();
				checkCuentaSueldo = !Utils.isEmpty(paqCSueldo);
			}
		} catch (Exception e) {
			return false;
		}

		return checkCuentaSueldo;
	}
	
	public String usuarioOtp() {
		return !"-1".equals(idCobis) ? idCobis : cuil;
	}

	public String getLogoPrincipal(String flujo) {
		if("FLUJO_INVERSIONES".equals(flujo)) {
			return GeneralBB.LOGO_FLUJO_INVERSIONES;
		}

		if("FLUJO_LIBERTAD".equals(flujo)) {
			return GeneralBB.LOGO_FLUJO_LIBERTAD;
		}

		return GeneralBB.LOGO_FLUJO_ONBOARDING;
	}

	public String getLogoSecundario(String flujo) {
		if("FLUJO_INVERSIONES".equals(flujo)) {
			return GeneralBB.LOGO_SEC_FLUJO_INVERSIONES;
		}

		if("FLUJO_LIBERTAD".equals(flujo)) {
			return GeneralBB.LOGO_SEC_FLUJO_LIBERTAD;
		}

		return GeneralBB.LOGO_SEC_FLUJO_ONBOARDING;
	}

	public String getColorPrincipal(String flujo) {
		if("FLUJO_INVERSIONES".equals(flujo)) {
			return GeneralBB.COLOR_FLUJO_INVERSIONES;
		}

		if("FLUJO_LIBERTAD".equals(flujo)) {
			return GeneralBB.COLOR_FLUJO_LIBERTAD;
		}

		return GeneralBB.COLOR_FLUJO_ONBOARDING;
	}

	public String getColorSecundario(String flujo) {
		if("FLUJO_INVERSIONES".equals(flujo)) {
			return GeneralBB.COLOR_SEC_FLUJO_INVERSIONES;
		}

		if("FLUJO_LIBERTAD".equals(flujo)) {
			return GeneralBB.COLOR_SEC_FLUJO_LIBERTAD;
		}

		return GeneralBB.COLOR_SEC_FLUJO_ONBOARDING;
	}

	public boolean tieneOtpValidado() {
		return emailOtpValidado && telefonoOtpValidado;
	}

	public boolean esV3() {
		return GeneralBB.NUMERO_VERSION_PLATAFORMA_0_0_3.equals(versionPlataforma);
	}

	@Override
	public String toString() {
		return "SesionBB{" +
				"token='" + token + '\'' +
				", operationVU='" + operationVU + '\'' +
				", ofertaElegida='" + ofertaElegida + '\'' +
				", subProducto='" + subProducto + '\'' +
				", estado='" + estado + '\'' +
				", reintentos=" + reintentos +
				", aceptartyc='" + aceptartyc + '\'' +
				", tdFisica='" + tdFisica + '\'' +
				", codeVU='" + codeVU + '\'' +
				", finalizarEnEjecucion=" + finalizarEnEjecucion +
				", idSolicitud='" + idSolicitud + '\'' +
				", idTarjeta='" + idTarjeta + '\'' +
				", letraTC='" + letraTC + '\'' +
				", limite=" + limite +
				", ingresoNeto=" + ingresoNeto +
				", resolucionMotorDeScoring='" + resolucionMotorDeScoring + '\'' +
				", modoAprobacion='" + modoAprobacion + '\'' +
				", codigoPaqueteMotor='" + codigoPaqueteMotor + '\'' +
				", tarjetaOferta=" + tarjetaOferta +
				", intentosOtp=" + intentosOtp +
				", fechaEnvioOtpMail=" + fechaEnvioOtpMail +
				", fechaEnvioOtpSms=" + fechaEnvioOtpSms +
				", mail='" + mail + '\'' +
				", codArea='" + codArea + '\'' +
				", celular='" + celular + '\'' +
				", claveOtp='" + claveOtp + '\'' +
				", stateId='" + stateId + '\'' +
				", cookie='" + cookie + '\'' +
				", nombre='" + nombre + '\'' +
				", apellido='" + apellido + '\'' +
				", genero='" + genero + '\'' +
				", fechaNacimiento=" + fechaNacimiento +
				", nacionalidad='" + nacionalidad + '\'' +
				", idNacionalidad='" + idNacionalidad + '\'' +
				", paisNacimiento='" + paisNacimiento + '\'' +
				", idPaisNacimiento='" + idPaisNacimiento + '\'' +
				", idTipoIDTributario='" + idTipoIDTributario + '\'' +
				", ejemplar='" + ejemplar + '\'' +
				", domicilioLegal=" + domicilioLegal +
				", domicilioPostal=" + domicilioPostal +
				", formaEntrega='" + formaEntrega + '\'' +
				", idSucursal=" + idSucursal +
				", idEstadoCivil='" + idEstadoCivil + '\'' +
				", idSituacionLaboral='" + idSituacionLaboral + '\'' +
				", idCantidadNupcias='" + idCantidadNupcias + '\'' +
				", tipoSitLaboral='" + tipoSitLaboral + '\'' +
				", idSubtipoEstadoCivil='" + idSubtipoEstadoCivil + '\'' +
				", SubtipoEstadoCivilDescr='" + SubtipoEstadoCivilDescr + '\'' +
				", conyuge=" + conyuge +
				", telefonoOtpValidado=" + telefonoOtpValidado +
				", emailOtpValidado=" + emailOtpValidado +
				", tokenFirebase='" + tokenFirebase + '\'' +
				", plataforma='" + plataforma + '\'' +
				", motorIndicador=" + motorIndicador +
				", bbInversorAceptada='" + bbInversorAceptada + '\'' +
				", estadoCajaUsd='" + estadoCajaUsd + '\'' +
				", usuarioVU='" + usuarioVU + '\'' +
				", idDispositivo='" + idDispositivo + '\'' +
				", sucursalOnboarding='" + sucursalOnboarding + '\'' +
				", latitud=" + latitud +
				", longitud=" + longitud +
				", esExpuestaPolitica=" + esExpuestaPolitica +
				", esSujetoObligado=" + esSujetoObligado +
				", esFatcaOcde=" + esFatcaOcde +
				", lavadoDinero=" + lavadoDinero +
				", versionPlataforma='" + versionPlataforma + '\'' +
				", tdVirtual=" + tdVirtual +
				", tcVirtual=" + tcVirtual +
				", cuilReferido='" + cuilReferido + '\'' +
				", adjustAdid='" + adjustAdid + '\'' +
				", adjustGpsAdid='" + adjustGpsAdid + '\'' +
				", valDatosPersonales=" + valDatosPersonales +
				", checkTdFisica=" + checkTdFisica +
				", checkTdFisicaCgu=" + checkTdFisicaCgu +
				", checkCuentaSueldo=" + checkCuentaSueldo +
				", atFlujoName='" + atFlujoName + '\'' +
				", atParamUrlVU='" + atParamUrlVU + '\'' +
				", atParamKeyPublicaVU='" + atParamKeyPublicaVU + '\'' +
				", atParamKeyPrivadaVU='" + atParamKeyPrivadaVU + '\'' +
				", atParamHorarioBatch=" + atParamHorarioBatch +
				", atParamCuentaSueldo=" + atParamCuentaSueldo +
				", atParamEnvioAndriani=" + atParamEnvioAndriani +
				", atParamPrevencionStandalone=" + atParamPrevencionStandalone +
				", atParamOtpV2=" + atParamOtpV2 +
				'}';
	}

	public boolean solicitoImpresion() {
		return esFlujoTcv() && esTdFisica();
	}

	public boolean enviarOfertaPaquete() {
		return esFlujoTcv() && resolucionAprobada() && esStandalone();
	}

	public boolean getGuardarCobisTemprano() {
		return guardarCobisTemprano;
	}

	public void setGuardarCobisTemprano(boolean valor) {
		this.guardarCobisTemprano = valor;
		this.saveCache();
	}

	public boolean getVentaTemprana() {
		return ventaTemprana;
	}

	public void ventaTempranaOK() {
		this.ventaTemprana = true;
		this.saveCache();
	}

	public void limpiezaCache() {
		this.ventaTemprana = false;
		this.guardarCobisTemprano = false;
		this.finalizarEnEjecucion = false;
		this.saveCache();
	}
}
