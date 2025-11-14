package ar.com.hipotecario.canal.homebanking;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.canal.homebanking.lib.Redis;

public class SesionHB extends Sesion implements Serializable {

	public static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public String captcha = "";
	public String cbuDestinoValidacionSegundoFactor;
	public String cookieOtp;
	public String cuentaPrestamoAprobado;
	public String documentacionResubirBpm;
	public String idCobisReal;
	public String modificacionCelularCanal;
	public String modificacionMailCanal;
	public String nuevoNemonico;
	public String otp;
	public String stateIdOtp;
	public String validadorPedido;
	public String validadorUsado;
	public Integer plazoPrestamoAprobado;
	public BigDecimal montoMaximoPrestamo;
	public BigDecimal montoPrestamoAprobado;
	public Date expiracionOtp;
	private String emailOtpDatavalid;
	private String telefonoOtpDatavalid;

	public Date fechaHoraUltimaConexion;
	public Date modificacionCelular;
	public Date modificacionMail;
	public Boolean aceptaTyC = false;
	public Boolean cambioDetectadoPP = true;
	public Boolean cambioDetectadoPPChequeado = false;
	public Boolean clienteInexistente = false;
	public Boolean cobisCaido = false;
	public Boolean ofertaPp = false;
	public Boolean ofertaPpCuotificacionMostrada = false;
	public Boolean ofertaPrestamoMostrada = false;
	public Boolean usuarioLogueado = false;
	public Boolean validaCaptcha = false;
	public Boolean validaDatosPersonalesOriginacion = false;
	public Boolean validaRiesgoNet = false;
	public Boolean validaSegundoFactorClaveLink = false;
	public Boolean validaSegundoFactorOtp = false;
	public Boolean validaSegundoFactorPreguntasPersonales = false;
	public Boolean validaSegundoFactorSoftToken = false;
	public Boolean validaSegundoFactorTarjetaCoordenadas = false;
	public Boolean dataValid = false;
	private Boolean adjuntaDocumentacion = false;
	public Map<Integer, Integer> respuestasRiesgoNet = new HashMap<>();
	public Map<String, Map<String, String>> comprobantes = new ConcurrentHashMap<>();
	public Map<String, String> cache = new ConcurrentHashMap<>();
	public Map<String, Integer> cacheHttp = new ConcurrentHashMap<>();
	public String canal = null;
	public Boolean esEmpleado = false;
	public String ultimaCuentaConsultadaMovimientos = null;
	public BigDecimal cotizacionAnticipoPrestamo;
	private Boolean odeCreada = false;

	/* ========== TRANSMIT ========== */
	private boolean challengeOtp = false;
	private String funcionalidadChallengeOtp = null;

	public String getFuncionalidadChallengeOtp() {
		return funcionalidadChallengeOtp;
	}

	public void setFuncionalidadChallengeOtp(String funcionalidadChallengeOtp) {
		this.funcionalidadChallengeOtp = funcionalidadChallengeOtp;
	}

	public boolean isChallengeOtp() {
		return challengeOtp;
	}

	public void setChallengeOtp(boolean challengeOtp) {
		this.challengeOtp = challengeOtp;
	}

	public boolean isChallengeOtp(String funcionalidad) {
		return this.isChallengeOtp() && this.getFuncionalidadChallengeOtp().equals(funcionalidad);
	}

	/* ========== CONSTRUCTOR ========== */
	public SesionHB(String idSesion) {
		this.idSesion = idSesion;
	}

	/* ========== SAVE ========== */
	public void save() {
		Redis.set(idSesion, this);
	}

	/* ========== METODOS ========== */
	public void limpiarSegundoFactor(boolean precondicionValidarClaveLink) {
		this.validaSegundoFactorOtp = false;
		this.validaSegundoFactorClaveLink = false;
		this.validaSegundoFactorTarjetaCoordenadas = false;
		this.validaSegundoFactorPreguntasPersonales = false;
		this.validaSegundoFactorSoftToken = false;
		if(!precondicionValidarClaveLink)
		{
			this.setChallengeOtp(false);
			this.setFuncionalidadChallengeOtp(null);
		}
	}

	public void limpiarSegundoFactor(boolean precondicionValidarClaveLink, boolean limpiarSoftoken) {
		this.validaSegundoFactorOtp = false;
		this.validaSegundoFactorClaveLink = false;
		this.validaSegundoFactorTarjetaCoordenadas = false;
		this.validaSegundoFactorPreguntasPersonales = false;
		if(limpiarSoftoken){
			this.validaSegundoFactorSoftToken = false;
		}
		if(!precondicionValidarClaveLink)
		{
			this.setChallengeOtp(false);
			this.setFuncionalidadChallengeOtp(null);
		}
	}

	public Boolean adjuntaDocumentacion() {
		return adjuntaDocumentacion;
	}

	public void setAdjuntaDocumentacion(Boolean adjuntaDocumentacion) {
		this.adjuntaDocumentacion = adjuntaDocumentacion;
		Redis.set(idSesion, this);
	}

	public BigDecimal montoMaximoPrestamo() {
		return montoMaximoPrestamo;
	}

	public void limpiarOtpDatavalid() {
		this.emailOtpDatavalid = null;
		this.telefonoOtpDatavalid = null;
		Redis.set(idSesion, this);
	}

	public void setEmailOtpDatavalid(String emailOtp, String idCanal) {
		if("DATAVALID_OTP".equals(idCanal)){
			this.emailOtpDatavalid = emailOtp;
			this.validadorPedido = "email_datavalid";
		}
		else{
			this.emailOtpDatavalid = null;
		}

		Redis.set(idSesion, this);
	}

	public void setTelefonoOtpDatavalid(String telefonoOtp, String idCanal) {
		if("DATAVALID_OTP".equals(idCanal)){
			this.telefonoOtpDatavalid = telefonoOtp != null && telefonoOtp.startsWith("0") ? telefonoOtp.substring(1) : telefonoOtp;
			this.validadorPedido = "sms_datavalid";
		}
		else{
			this.telefonoOtpDatavalid = null;
		}
		Redis.set(idSesion, this);
	}

	public void validarOtpDatavalid(String tipoOtp) {
		String tipoOtpAux = getTipoAux(tipoOtp);
		String validado = "VALIDADO_";

		if("email_datavalid".equals(tipoOtpAux) && this.emailOtpDatavalid != null){
			String email = this.emailOtpDatavalid.startsWith(validado) ? this.emailOtpDatavalid.substring(validado.length()) : this.emailOtpDatavalid;
			this.emailOtpDatavalid = validado + email;
			Redis.set(idSesion, this);
		}
		else if("sms_datavalid".equals(tipoOtpAux) && this.telefonoOtpDatavalid != null){
			String telefono = this.telefonoOtpDatavalid.startsWith(validado) ? this.telefonoOtpDatavalid.substring(validado.length()) : this.telefonoOtpDatavalid;
			this.telefonoOtpDatavalid = validado + telefono;
			Redis.set(idSesion, this);
		}
	}

	public boolean tieneOtpDatavalid(String tipoOtp){
		String tipoOtpAux = getTipoAux(tipoOtp);
		return ("email_datavalid".equals(tipoOtpAux) && this.emailOtpDatavalid != null)
				|| ("sms_datavalid".equals(tipoOtpAux) && this.telefonoOtpDatavalid != null);
	}

	public boolean tieneOtpDatavalidValido(String tipoOtp, String contacto) {
		String tipoOtpAux = getTipoAux(tipoOtp);
		String validado = "VALIDADO_";
		String contactoValidado = "email_datavalid".equals(tipoOtpAux) ? this.emailOtpDatavalid : this.telefonoOtpDatavalid;
		return this.tieneOtpDatavalid(tipoOtpAux)
				&& contactoValidado != null
				&& contactoValidado.contains(validado + contacto);
	}

	private String getTipoAux(String tipo){
		if(tipo == null) return "";
		return tipo + (tipo.contains("_datavalid") ? "" : "_datavalid");
	}

	public Boolean aceptaTyC() {
		return aceptaTyC;
	}

	public void setAceptaTyC(Boolean aceptaTyC) {
		this.aceptaTyC = aceptaTyC;
		Redis.set(idSesion, this);
	}

	public Boolean isOdeCreada() {
        return odeCreada;
    }

    public void setOdeCreada(Boolean odeCreada) {
        this.odeCreada = odeCreada;
		Redis.set(idSesion, this);
    }

}
