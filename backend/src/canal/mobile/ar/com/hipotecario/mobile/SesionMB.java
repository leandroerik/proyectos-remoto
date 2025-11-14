package ar.com.hipotecario.mobile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.mobile.lib.Redis;

@SuppressWarnings("serial")
public class SesionMB extends Sesion implements Serializable {

    /* ========== SESION ========== */
    private String id;

    /* ========== USUARIO ========== */
    private Boolean usuarioLogueado = false;
    private Date fechaHoraUltimaConexion = null;
    private String idCobisReal = null;

    private Date modificacionMail;
    private Date modificacionCelular;
    private String modificacionMailCanal;
    private String modificacionCelularCanal;

    private Boolean dataValid = false;
    /* ========== CAPTCHA ========== */
    private String captcha = "";
    private Boolean validaCaptcha = false;

    /* ========== SEGUNDO FACTOR ========== */
    private Boolean validaSegundoFactorOtp = false;
    public Boolean cobisCaido = false;
    private Boolean validaSegundoFactorBiometria = false;
    private Boolean validaSegundoFactorBuhoFacil = false;
    private Boolean validaSegundoFactorClaveLink = false;
    private Boolean validaSegundoFactorTarjetaCoordenadas = false;
    private Boolean validaSegundoFactorPreguntasPersonales = false;
    private Boolean validaSegundoFactorSoftToken = false;
    private String validadorPedido = null;
    private String validadorUsado = null;

    private Date expiracionOtp;
    private String emailOtpDatavalid;
    private String telefonoOtpDatavalid;
    private String stateIdOtp;
    private String cookieOtp;
    private Integer cantidadIntentosValidacionLink;

    private Boolean validaDatosPersonalesOriginacion = false; // variable que necesito para asegurarme de que van
    // por backend a la hora de validar los datos personales
    // (por si modifican el front)
    private String otp;

    /* ========== VARIABLE PARA PRESTAMOS ========== */
    private BigDecimal montoMaximoPrestamo;
    private BigDecimal montoPrestamoAprobado;
    private Integer plazoPrestamoAprobado;
    private String cuentaPrestamoAprobado;
    private Boolean ofertaPp = false;
    private Boolean aceptaTyC = false;
    private Boolean adjuntaDocumentacion = false;

    /* ========== VARIABLE PARA SCAN DE DNI ========== */

    private Integer cuentaIntentosScanDni;

    private Integer cuentaIntentosPersonaNoEncontrada;

    /* ========== RIESGONET ========== */
    private Map<Integer, Integer> respuestasRiesgoNet = null;
    private Boolean validaRiesgoNet = false;

    /* ========== CACHE ========== */
    public Map<String, String> cache = new ConcurrentHashMap<>();
    private Map<String, Integer> cacheHttp = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> comprobantes = new ConcurrentHashMap<>();

    /* ========== OTROS ========== */
    private String ultimaCuentaConsultadaMovimientos = null;

    /* ========== TRANSMIT ========== */
    private String migraPorBiometria;
    private boolean challengeOtp = false;
    private boolean cambioClaveTransmit;
    private boolean cambioUsuarioTransmit;

    private String funcionalidadChallengeOtp = "";

    /* ========== CONSTRUCTOR ========== */
    public SesionMB(String id) {
        this.id = id;
    }

    /* ========== GET ========== */
    public String id() {
        return id;
    }

    public String idCobis() {
        return idCobis;
    }

    public String idCobisReal() {
        return idCobisReal;
    }

    public Boolean usuarioLogueado() {
        return usuarioLogueado;
    }

    public Date fechaHoraUltimaConexion() {
        return fechaHoraUltimaConexion;
    }

    public String captcha() {
        return captcha;
    }

    public Boolean validaCaptcha() {
        return validaCaptcha;
    }

    public Boolean validaSegundoFactorOtp() {
        return validaSegundoFactorOtp;
    }

    public Boolean cobisCaido() {
        return cobisCaido;
    }

    public Boolean validaSegundoFactorClaveLink() {
        return validaSegundoFactorClaveLink;
    }

    public Boolean validaSegundoFactorTarjetaCoordenadas() {
        return validaSegundoFactorTarjetaCoordenadas;
    }

    public Boolean validaSegundoFactorPreguntasPersonales() {
        return validaSegundoFactorPreguntasPersonales;
    }

    public Boolean validaSegundoFactorSoftToken() {
        return this.validaSegundoFactorSoftToken;
    }

    public Boolean validaDatosPersonalesOriginacion() {
        return validaDatosPersonalesOriginacion;
    }

    public Date expiracionOtp() {
        return expiracionOtp;
    }

    public Integer cantidadIntentosValidacionLink() {
        return cantidadIntentosValidacionLink;
    }

    public Integer getCuentaIntentosScanDni() {
        return cuentaIntentosScanDni;
    }

    public Integer getCuentaIntentosPersonaNoEncontrada() {
        return cuentaIntentosPersonaNoEncontrada;
    }

    public String cache(String clave) {
        return cache.get(clave);
    }

    public Integer cacheHttp(String clave) {
        return cacheHttp.get(clave);
    }

    public Map<String, String> comprobante(String clave) {
        return comprobantes.get(clave);
    }

    public BigDecimal montoMaximoPrestamo() {
        return montoMaximoPrestamo;
    }

    public BigDecimal montoPrestamoAprobado() {
        return montoPrestamoAprobado;
    }

    public Integer plazoPrestamoAprobado() {
        return plazoPrestamoAprobado;
    }

    public String cuentaPrestamoAprobado() {
        return cuentaPrestamoAprobado;
    }

    public String stateIdOtp() {
        return stateIdOtp;
    }

    public String cookieOtp() {
        return cookieOtp;
    }

    public Map<Integer, Integer> respuestasRiesgoNet() {
        return respuestasRiesgoNet;
    }

    public Boolean validaRiesgoNet() {
        return validaRiesgoNet;
    }

    public String validadorPedido() {
        return validadorPedido;
    }

    public String validadorUsado() {
        return validadorUsado;
    }

    public Boolean ofertaPpMostrada() {
        return ofertaPp;
    }

    public String getOTP() {
        return otp;
    }

    public Boolean getDataValid() {
        return dataValid;
    }

    public String getUltimaCuentaConsultadaMovimientos() {
        return ultimaCuentaConsultadaMovimientos;
    }

    public Boolean ofertaPpCuotificacionMostrada = false;

    public String getMigraPorBiometria() {
        return migraPorBiometria;
    }

    public String getFuncionalidadChallengeOtp() {
        return funcionalidadChallengeOtp;
    }

    public void setFuncionalidadChallengeOtp(String funcionalidadChallengeOtp) {
        this.funcionalidadChallengeOtp = funcionalidadChallengeOtp;
        Redis.set(id, this);
    }

    public boolean isChallengeOtp() {
        return challengeOtp;
    }

    public void setChallengeOtp(boolean challengeOtp) {
        this.challengeOtp = challengeOtp;
        Redis.set(id, this);
    }

    public boolean isCambioClaveTransmit() {
        return cambioClaveTransmit;
    }

    public void setCambioClaveTransmit(boolean cambioClaveTransmit) {
        this.cambioClaveTransmit = cambioClaveTransmit;
        Redis.set(id, this);
    }

    public boolean isCambioUsuarioTransmit() {
        return cambioUsuarioTransmit;
    }

    public void setCambioUsuarioTransmit(boolean cambioUsuarioTransmit) {
        this.cambioUsuarioTransmit = cambioUsuarioTransmit;
        Redis.set(id, this);
    }

    /* ========== SET ========== */
    public void setIdCobis(String idCobis) {
        this.idCobis = idCobis;
        Redis.set(id, this);
    }

    public void setIdCobisReal(String idCobisReal) {
        this.idCobisReal = idCobisReal;
        Redis.set(id, this);
    }

    public void setUsuarioLogueado(Boolean usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        Redis.set(id, this);
    }

    public void setFechaHoraUltimaConexion(Date fechaHoraUltimaConexion) {
        this.fechaHoraUltimaConexion = fechaHoraUltimaConexion;
        Redis.set(id, this);
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
        Redis.set(id, this);
    }

    public void setValidaCaptcha(Boolean validaCaptcha) {
        this.validaCaptcha = validaCaptcha;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorOtp(Boolean validaSegundoFactorOtp) {
        this.validaSegundoFactorOtp = validaSegundoFactorOtp;
        Redis.set(id, this);
    }

    public void setCobisCaido(Boolean cobisCaido) {
        this.cobisCaido = cobisCaido;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorClaveLink(Boolean validaSegundoFactorClaveLink) {
        this.validaSegundoFactorClaveLink = validaSegundoFactorClaveLink;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorTarjetaCoordenadas(Boolean validaSegundoFactorTarjetaCoordenadas) {
        this.validaSegundoFactorTarjetaCoordenadas = validaSegundoFactorTarjetaCoordenadas;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorPreguntasPersonales(Boolean validaSegundoFactorPreguntasPersonales) {
        this.validaSegundoFactorPreguntasPersonales = validaSegundoFactorPreguntasPersonales;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorSoftToken(Boolean validaSegundoFactorSoftToken) {
        this.validaSegundoFactorSoftToken = validaSegundoFactorSoftToken;
        Redis.set(id, this);
    }

    public void setValidaDatosPersonalesOriginacion(Boolean validarDatosPersonalesOriginacion) {
        this.validaDatosPersonalesOriginacion = validarDatosPersonalesOriginacion;
        Redis.set(id, this);
    }

    public void setExpiracionOtp(Date expiracionOtp) {
        this.expiracionOtp = expiracionOtp;
        Redis.set(id, this);
    }

    public void limpiarOtpDatavalid() {
        this.emailOtpDatavalid = null;
        this.telefonoOtpDatavalid = null;
        Redis.set(id, this);
    }

    public void setEmailOtpDatavalid(String emailOtp, String idCanal) {
        if ("DATAVALID_OTP".equals(idCanal)) {
            this.emailOtpDatavalid = emailOtp;
            this.validadorPedido = "email_datavalid";
        } else {
            this.emailOtpDatavalid = null;
        }

        Redis.set(id, this);
    }

    public String getEmailOtpDatavalid() {
        return this.emailOtpDatavalid;
    }

    public void setTelefonoOtpDatavalid(String telefonoOtp, String idCanal) {
        if ("DATAVALID_OTP".equals(idCanal)) {
            this.telefonoOtpDatavalid = telefonoOtp != null && telefonoOtp.startsWith("0") ? telefonoOtp.substring(1) : telefonoOtp;
            this.validadorPedido = "sms_datavalid";
        } else {
            this.telefonoOtpDatavalid = null;
        }
        Redis.set(id, this);
    }

    public String getTelefonoOtpDatavalid() {
        return this.telefonoOtpDatavalid != null && telefonoOtpDatavalid.startsWith("0") ? telefonoOtpDatavalid.substring(1) : telefonoOtpDatavalid;
    }

    public void setValidarOtpDatavalid(String tipoOtp) {
        String tipoOtpAux = getTipoAux(tipoOtp);
        String validado = "VALIDADO_";

        if ("email_datavalid".equals(tipoOtpAux) && this.emailOtpDatavalid != null) {
            String email = this.emailOtpDatavalid.startsWith(validado) ? this.emailOtpDatavalid.substring(validado.length()) : this.emailOtpDatavalid;
            this.emailOtpDatavalid = validado + email;
            this.otp = null;
            this.expiracionOtp = null;
            Redis.set(id, this);
        } else if ("sms_datavalid".equals(tipoOtpAux) && this.telefonoOtpDatavalid != null) {
            String telefono = this.telefonoOtpDatavalid.startsWith(validado) ? this.telefonoOtpDatavalid.substring(validado.length()) : this.telefonoOtpDatavalid;
            this.telefonoOtpDatavalid = validado + telefono;
            this.otp = null;
            this.expiracionOtp = null;
            Redis.set(id, this);
        }
    }

    public boolean tieneOtpDatavalid(String tipoOtp) {
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

    private String getTipoAux(String tipo) {
        if (tipo == null) return "";
        return tipo + (tipo.contains("_datavalid") ? "" : "_datavalid");
    }

    public void setCantidadIntentosValidacionLink(Integer cantidadIntentosValidacionLink) {
        this.cantidadIntentosValidacionLink = cantidadIntentosValidacionLink;
        Redis.set(id, this);
    }

    public void setCuentaIntentosScanDni(Integer cuentaIntentosScanDni) {
        this.cuentaIntentosScanDni = cuentaIntentosScanDni;
        Redis.set(id, this);
    }

    public void setCuentaIntentosPersonaNoEncontrada(Integer cuentaIntentosPersonaNoEncontrada) {
        this.cuentaIntentosPersonaNoEncontrada = cuentaIntentosPersonaNoEncontrada;
        Redis.set(id, this);
    }

    public void setMigraPorBiometria(String migraPorBiometria) {
        this.migraPorBiometria = migraPorBiometria;
    }

    public void limpiarSegundoFactor(Boolean limpiarChallenge) {
        this.limpiarSegundoFactorCompleto();
        if (!limpiarChallenge) {
            this.setChallengeOtp(false);
            this.setFuncionalidadChallengeOtp("");
        }
        Redis.set(id, this);
    }

    public void limpiarSegundoFactor() {
        this.limpiarSegundoFactorCompleto();
        this.setChallengeOtp(false);
        this.setFuncionalidadChallengeOtp("");
        Redis.set(id, this);
    }

    public void limpiarChallengeDrs() {
        this.setChallengeOtp(false);
        this.setFuncionalidadChallengeOtp("");
        Redis.set(id, this);
    }

    private void limpiarSegundoFactorCompleto() {
        this.validaSegundoFactorBiometria = false;
        this.validaSegundoFactorBuhoFacil = false;
        this.validaSegundoFactorOtp = false;
        this.validaSegundoFactorClaveLink = false;
        this.validaSegundoFactorTarjetaCoordenadas = false;
        this.validaSegundoFactorPreguntasPersonales = false;
        this.validaSegundoFactorSoftToken = false;
    }

    public void setCache(String clave, String valor) {
        cache.put(clave, valor);
        Redis.set(id, this);
    }

    public void setCacheHttp(String clave, String valor, Integer codigoHttp) {
        cache.put(clave, valor);
        cacheHttp.put(clave, codigoHttp);
        Redis.set(id, this);
    }

    public void delCache(String clave) {
        cache.remove(clave);
        cacheHttp.remove(clave);
        Redis.set(id, this);
    }

    public void setComprobante(String clave, Map<String, String> valor) {
        comprobantes.put(clave, valor);
        Redis.set(id, this);
    }

    public void delComprobante(String clave) {
        comprobantes.remove(clave);
        Redis.set(id, this);
    }

    public void setMontoMaximoPrestamo(BigDecimal montoMaximoPrestamo) {
        this.montoMaximoPrestamo = montoMaximoPrestamo;
        Redis.set(id, this);
    }

    public void setMontoPrestamoAprobado(BigDecimal montoPrestamoAprobado) {
        this.montoPrestamoAprobado = montoPrestamoAprobado;
        Redis.set(id, this);
    }

    public void setPlazoPrestamoAprobado(Integer plazoPrestamoAprobado) {
        this.plazoPrestamoAprobado = plazoPrestamoAprobado;
        Redis.set(id, this);
    }

    public void setCuentaPrestamoAprobado(String cuentaPrestamoAprobado) {
        this.cuentaPrestamoAprobado = cuentaPrestamoAprobado;
        Redis.set(id, this);
    }

    public void setStateIdOtp(String stateIdOtp) {
        this.stateIdOtp = stateIdOtp;
        Redis.set(id, this);
    }

    public void setCookieOtp(String cookieOtp) {
        this.cookieOtp = cookieOtp;
        Redis.set(id, this);
    }

    public void clearRespuestasRiesgoNet() {
        this.respuestasRiesgoNet = new HashMap<>();
        Redis.set(id, this);
    }

    public void setRespuestaRiesgoNet(Integer clave, Integer valor) {
        this.respuestasRiesgoNet.put(clave, valor);
        Redis.set(id, this);
    }

    public void setValidaRiesgoNet(Boolean valor) {
        this.validaRiesgoNet = valor;
        Redis.set(id, this);
    }

    public Boolean validaSegundoFactorBiometria() {
        return validaSegundoFactorBiometria;
    }

    public Boolean validaSegundoFactorBuhoFacil() {
        return validaSegundoFactorBuhoFacil;
    }

    public void setValidaSegundoFactorBiometria(Boolean validaSegundoFactorBiometria) {
        this.validaSegundoFactorBiometria = validaSegundoFactorBiometria;
        Redis.set(id, this);
    }

    public void setValidaSegundoFactorBuhoFacil(Boolean validaSegundoFactorBuhoFacil) {
        this.validaSegundoFactorBuhoFacil = validaSegundoFactorBuhoFacil;
        Redis.set(id, this);
    }

    public void setValidadorPedido(String validadorPedido) {
        this.validadorPedido = validadorPedido;
        Redis.set(id, this);
    }

    public void setValidadorUsado(String validadorUsado) {
        this.validadorUsado = validadorUsado;
        Redis.set(id, this);
    }

    public void setModificacionMail(Date modificacionMail) {
        this.modificacionMail = modificacionMail;
        Redis.set(id, this);
    }

    public void setModificacionCelular(Date modificacionCelular) {
        this.modificacionCelular = modificacionCelular;
        Redis.set(id, this);
    }

    public void setModificacionMailCanal(String modificacionMailCanal) {
        this.modificacionMailCanal = modificacionMailCanal;
        Redis.set(id, this);
    }

    public void setModificacionCelularCanal(String modificacionCelularCanal) {
        this.modificacionCelularCanal = modificacionCelularCanal;
        Redis.set(id, this);
    }

    public void setOfertaPpMostrada(Boolean ofertaPp) {
        this.ofertaPp = ofertaPp;
        Redis.set(id, this);
    }

    public Boolean aceptaTyC() {
        return aceptaTyC;
    }

    public void setAceptaTyC(Boolean aceptaTyC) {
        this.aceptaTyC = aceptaTyC;
        Redis.set(id, this);
    }

    public Boolean adjuntaDocumentacion() {
        return adjuntaDocumentacion;
    }

    public void setAdjuntaDocumentacion(Boolean adjuntaDocumentacion) {
        this.adjuntaDocumentacion = adjuntaDocumentacion;
        Redis.set(id, this);
    }

    public Date modificacionMail() {
        return modificacionMail;
    }

    public Date modificacionCelular() {
        return modificacionCelular;
    }

    public String modificacionMailCanal() {
        return modificacionMailCanal;
    }

    public String modificacionCelularCanal() {
        return modificacionCelularCanal;
    }

    public void setOTP(String otp) {
        this.otp = otp;
        Redis.set(id, this);
    }

    public void setDataValid(Boolean dataValid) {
        this.dataValid = dataValid;
        Redis.set(id, this);
    }

    public void setUltimaCuentaConsultadaMovimientos(String ultimaCuentaConsultadaMovimientos) {
        this.ultimaCuentaConsultadaMovimientos = ultimaCuentaConsultadaMovimientos;
        Redis.set(id, this);
    }

    public boolean isChallengeOtp(String funcionalidad) {
        return this.isChallengeOtp() && this.getFuncionalidadChallengeOtp().equals(funcionalidad);
    }

    public void limpiarMigracion() {
        this.cambioClaveTransmit = false;
        this.cambioUsuarioTransmit = false;
    }

    public void setValidarOtp(String validadorUsado) {
        this.validadorUsado = validadorUsado;
        this.validaSegundoFactorOtp = true;
        this.otp = null;
        this.expiracionOtp = null;
        Redis.set(id, this);
    }
}

