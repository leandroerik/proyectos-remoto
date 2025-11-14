package ar.com.hipotecario.mobile.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.imageio.ImageIO;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Encriptador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.AuditorLogService;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.RestSeguridad;
import ar.com.hipotecario.mobile.servicio.SqlClaveRedLinkUso;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.RippleGimpyRenderer;

public class MBSeguridad {

    private static final String FORMATO_FECHA_ISO_8106 = "yyyy-MM-dd HH:mm:ss";
    private static final String MENSAJE_INSERTAR_INTENTO_EMAIL = "OTP incorrecto";

    public static RespuestaMB canalesOTP(ContextoMB contexto) {
        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        RespuestaMB respuesta = new RespuestaMB();

        String email = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
        String celular = RestPersona.numeroCelular(contexto, contexto.persona().cuit());

        if (email != null) {
            Objeto item = new Objeto();
            item.set("id", "EMAIL_EMP");
            item.set("descripcion", email);
            item.set("tipo", "email");
            respuesta.add("canales", item);
        }

        if (celular != null) {
            Objeto item = new Objeto();
            item.set("id", "SMS_E");
            item.set("descripcion", celular);
            item.set("tipo", "celular");
            respuesta.add("canales", item);
        }

        return respuesta;
    }

    public static RespuestaMB pedirOTP(ContextoMB contexto) {
        String idCanal = contexto.parametros.string("idCanal");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        String forzarEmail = contexto.parametros.string("email", null);
        String forzarTelefono = contexto.parametros.string("telefono", null);

        if (Objeto.anyEmpty(idCanal)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if ("DATAVALID_OTP".equals(idCanal)) {
            if (Objeto.anyEmpty(forzarEmail) && Objeto.anyEmpty(forzarTelefono)) {
                return RespuestaMB.parametrosIncorrectos();
            } else if (!Objeto.anyEmpty(forzarEmail) && !Objeto.anyEmpty(forzarTelefono)) {
                return RespuestaMB.parametrosIncorrectos();
            }
        } else {
            forzarEmail = null;
            forzarTelefono = null;
        }

        String idEmail = idCanal.startsWith("EMAIL_") ? idCanal.substring("EMAIL_".length()) : null;
        String idTelefono = idCanal.startsWith("SMS_") ? idCanal.substring("SMS_".length()) : null;

        // emm-20190807-desde --> Agrego esta lógica para que expire el otp a los cinco
        // minutos y que no puedan llamar varias veces al otp por front
        Date fechaExpiracionOtp = contexto.sesion().expiracionOtp();
        if (fechaExpiracionOtp != null && new Date().getTime() < fechaExpiracionOtp.getTime()) {
            return RespuestaMB.estado("OTP_SIN_EXPIRAR");
        }
        // emm-20190807-hasta

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        contexto.sesion().setValidadorPedido(null);

        ApiResponseMB response = null;
        if (!ConfigMB.bool("deshabilitar_isva", false) && !ConfigMB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {

            // Verificar que exista usuario en IDG
            RestSeguridad.usuario(contexto, true);

            // SeguridadGetOtp
            ApiRequestMB request = ApiMB.request("SeguridadGetOtp", "seguridad", "GET", "/v1/clave/otp", contexto);
            request.header("x-fingerprint", fingerprint);
            request.query("grupo", "ClientesBH");
            request.query("idcliente", idCobis);
            request.permitirSinLogin = true;

            response = ApiMB.response(request);
            if (response.hayError()) {
                return RespuestaMB.error();
            }
        } else {
            String otp = Util.randomHmacSHA1(100000, 999999).toString();

            Objeto item = new Objeto();
            item.set("clave", otp);
            item.set("stateId", "random");

            response = new ApiResponseMB();
            response.add("otp", item);
            response.headers.put("set-cookie", "random");

            contexto.sesion().setOTP(otp + ":" + new Date().getTime());
        }

        String email = forzarEmail;
        if (idEmail != null && email == null) {
            email = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
        }

        String telefono = forzarTelefono;
        if (idTelefono != null && telefono == null) {
            telefono = RestPersona.numeroCelular(contexto, contexto.persona().cuit());
        }

        String otp = response.objetos("otp").get(0).string("clave");
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_otp_state_id")) {
            contexto.sesion().setStateIdOtp(response.objetos("otp").get(0).string("stateId"));
            contexto.sesion().setCookieOtp(response.headers.get("set-cookie"));
        }

        if (email != null) { // NotificacionesPostCorreoElectronico
            contexto.sesion().setEmailOtpDatavalid(email, idCanal);
            if (idCanal.startsWith("EMAIL_") && forzarEmail == null) {
                contexto.sesion().setValidadorPedido("email");
            }

            ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", email);
            requestMail.body("plantilla", ConfigMB.string("doppler_otp"));
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Banco Hipotecario - Código de verificación");
            parametros.set("TOKEN", otp);
            requestMail.permitirSinLogin = true;

            ApiResponseMB responseMail = ApiMB.response(requestMail);
            if (responseMail.hayError()) {
                return RespuestaMB.error();
            }
        } else if (telefono != null) { // Notificacion por Sms
            contexto.sesion().setTelefonoOtpDatavalid(telefono, idCanal);
            if (idCanal.startsWith("SMS_") && forzarTelefono == null) {
                contexto.sesion().setValidadorPedido("sms");
            }

            telefono = telefono.startsWith("0") ? telefono.substring(1) : telefono;
            String texto = "Nunca compartas esta clave. Es confidencial y de uso personal exclusivo. Tu codigo de seguridad Banco Hipotecario es " + otp;
            contexto.parametros.set("mensaje", texto);
            contexto.parametros.set("codigo", otp);
            contexto.parametros.set("telefono", telefono);
            RespuestaMB respuestaSendSms = new RespuestaMB();
            respuestaSendSms = MBNotificaciones.sendSmsOTP(contexto);
            if (!respuestaSendSms.get("estado").equals("0")) {
                return respuestaSendSms;
            }
        } else {
            return RespuestaMB.error();
        }

        // emm-20190807-desde --> Agrego esta lógica para que expire el otp a los cinco
        // minutos y que no puedan llamar varias veces al otp por front
        Calendar calendario = Calendar.getInstance();
        int minutos = 1;
        calendario.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE) + minutos);
        contexto.sesion().setExpiracionOtp(calendario.getTime());
        // emm-20190807-hasta
        contexto.insertarLogEnvioOtp(contexto, telefono, email, null, null, "P");

        if (!ConfigMB.esProduccion()) {
            return RespuestaMB.exito("otp", otp);
        }
        return RespuestaMB.exito();
    }

    public static RespuestaMB validarOTP(ContextoMB contexto) {
        String otp = contexto.parametros.string("otp");
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (Objeto.anyEmpty(otp)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (Objeto.anyEmpty(contexto.sesion().validadorPedido())) {
            return RespuestaMB.error();
        }

        contexto.sesion().setValidadorUsado(null);

        // emm-20190807-desde --> Agrego esta lógica para que expire el otp a los cinco
        // minutos y que no puedan llamar varias veces al otp por front
        Date fechaExpiracionOtp = contexto.sesion().expiracionOtp();
        if (fechaExpiracionOtp != null && new Date().getTime() > fechaExpiracionOtp.getTime()) {
            return RespuestaMB.estado("OTP_EXPIRADO");
        }
        // emm-20190807-hasta

        contexto.limpiarSegundoFactor();

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (!ConfigMB.esProduccion() && ("0".equals(otp) || "000000".equals(otp) || "111111".equals(otp) || "999999".equals(otp))) {
            String validadorPedido = contexto.sesion().validadorPedido();
            if (contexto.sesion().tieneOtpDatavalid(validadorPedido)) {
                contexto.sesion().setValidarOtpDatavalid(validadorPedido);
            } else {
                contexto.sesion().setValidarOtp(validadorPedido);
                validarDatavalidOtp(contexto, validadorPedido);
            }

            return RespuestaMB.exito();
        }

        if (!ConfigMB.bool("deshabilitar_isva", false) && !ConfigMB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
        } else {
            String otpSesionFull = contexto.sesion().getOTP();
            String otpSesion = otpSesionFull != null ? otpSesionFull.split(":")[0] : null;
            String timestamp = otpSesionFull != null ? otpSesionFull.split(":")[1] : null;
            Long duracionOTP = ConfigMB.longer("duracionOTP", 2 * 60 * 1000L);

            if (otpSesion == null || otpSesion.isEmpty()) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return RespuestaMB.estado("OTP_NO_GENERADO");
            }

            if (timestamp == null || new Date().getTime() > Long.valueOf(timestamp) + duracionOTP) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return RespuestaMB.estado("OTP_EXPIRADO");
            }

            if (!otpSesion.equals(otp)) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return RespuestaMB.estado("OTP_INVALIDO");
            }

            String validadorPedido = contexto.sesion().validadorPedido();
            if (contexto.sesion().tieneOtpDatavalid(validadorPedido)) {
                contexto.sesion().setValidarOtpDatavalid(validadorPedido);
            } else {
                contexto.sesion().setValidarOtp(validadorPedido);
                validarDatavalidOtp(contexto, validadorPedido);
            }

            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");
            return RespuestaMB.exito();
        }

        // SeguridadGetClaveOtp
        ApiRequestMB request = ApiMB.request("ValidarOTP", "seguridad", "GET", "/v1/clave", contexto);
        request.header("x-fingerprint", fingerprint);
        request.query("grupo", "ClientesBH");
        request.query("idcliente", idCobis);
        request.query("clave", otp);
        request.query("nombreClave", "OTP");

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_otp_state_id")) {
            try {
                request.query("stateId", URLEncoder.encode(contexto.sesion().stateIdOtp(), "UTF-8"));
            } catch (Exception e) {
                request.query("stateId", contexto.sesion().stateIdOtp());
            }
            request.header("cookie", contexto.sesion().cookieOtp());
        }

        request.permitirSinLogin = true;

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {
            if (response.string("detalle").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return RespuestaMB.estado("OTP_INVALIDO");
            }
            if (response.string("mensajeAlUsuario").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return RespuestaMB.estado("OTP_INVALIDO");
            }
            if ("La clave ingresada es invalida, intente de nuevo".equals(response.string("mensajeError"))) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion().setStateIdOtp(response.string("stateId"));
                return RespuestaMB.estado("OTP_INVALIDO");
            }
            if (response.string("mensajeError").contains("Authentication service received an invalid state ID")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion().setStateIdOtp(response.string("stateId"));
                return RespuestaMB.estado("OTP_INVALIDO_VOLVER_EMPEZAR");
            }
            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "E");
            return RespuestaMB.error();
        }

        String validadorPedido = contexto.sesion().validadorPedido();
        if (contexto.sesion().tieneOtpDatavalid(validadorPedido)) {
            contexto.sesion().setValidarOtpDatavalid(validadorPedido);
        } else {
            contexto.sesion().setValidarOtp(validadorPedido);
            validarDatavalidOtp(contexto, validadorPedido);
        }

        contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");
        return RespuestaMB.exito();
    }

    private static void validarDatavalidOtp(ContextoMB contexto, String validadorPedido) {
        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp_registro")) return;

        try {
            if ("sms".equals(validadorPedido)) {
                Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
                Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
                new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, secTelDom, 0, secTel));
            }

            if ("email".equals(validadorPedido)) {
                Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
                new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, 0, secMail, 0));
            }
        } catch (Exception e) {
        }
    }

    // TODO eliminar TCO
    public static RespuestaMB pedirTarjetaCoordenadas(ContextoMB contexto) {
        return RespuestaMB.estado("SIN_TARJETA_COORDENADAS");
    }

    // TODO eliminar TCO
    public static RespuestaMB validarTarjetaCoordenadas(ContextoMB contexto) {
        return RespuestaMB.error();
    }

    public static RespuestaMB validarClaveLink(ContextoMB contexto) {
        return validarClave(contexto);
    }

    public static RespuestaMB validarClaveLinkTransmit(ContextoMB contexto) {
        return RespuestaMB.exito("validacion", Transmit.generarRespuestaLink(ConfigMB.string("clave_secreta_transmit", ""), !validarClave(contexto).hayError()));
    }

    private static RespuestaMB validarClave(ContextoMB contexto) {
        String clave = contexto.parametros.string("clave");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);

        if (Objeto.anyEmpty(clave))
            return RespuestaMB.parametrosIncorrectos();

        contexto.limpiarSegundoFactor();

        String idCobis = contexto.idCobis();
        if (idCobis == null)
            return RespuestaMB.estado("SIN_PSEUDO_SESION");

        TarjetaDebito tarjetaDebito = idTarjetaDebito != null ? contexto.tarjetaDebito(idTarjetaDebito) : contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null)
            return RespuestaMB.estado("NO_EXISTE_TARJETA_DEBITO");

        String claveLinkHasheada = Encriptador.sha256(clave);

        SqlResponseMB sqlResponse = SqlClaveRedLinkUso.consultarUltimoUso(contexto.idCobis(), tarjetaDebito.numero(), claveLinkHasheada);

        if (sqlResponse.hayError || (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()))
            return RespuestaMB.error();

        if (!ConfigMB.esProduccion() && "999999".equals(clave)) {
            RestPersona.permitirSegundoFactorOtp(contexto.idCobis(), true);
            contexto.sesion().setValidaSegundoFactorClaveLink(true);
            if (StringUtils.isNotBlank(ConfigMB.string("mb_prendido_link_uso_cobis", "")) && Arrays.asList(ConfigMB.string("mb_prendido_link_uso_cobis", "").split("_")).stream().filter(c -> c.equals(contexto.idCobis())).count() == 1)
                SqlClaveRedLinkUso.insertarClave(idCobis, tarjetaDebito.numero(), claveLinkHasheada, "MB", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));
            return RespuestaMB.exito();
        }

        // LinkPostVerificacion
        ApiResponseMB response = new RestSeguridad().verificarClaveRedLink(tarjetaDebito.numero(), clave, contexto);
        if (response.hayError() || !"true".equals(response.string("adherido"))) {
            String estado = "ERROR";
            // TODO: Mapear errores
            estado = "48".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "50".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "52".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "53".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "54".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "70".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "73".equals(response.string("codigo")) ? "ERROR" : estado;
            estado = "74".equals(response.string("codigo")) ? "ERROR" : estado;
            contexto.insertarLogEnvioOtp(contexto, null, null, null, true, "E");

            return RespuestaMB.estado(estado);
        }

        // Verificar que exista usuario en IDG
        if (!ConfigMB.bool("deshabilitar_isva", false) && !ConfigMB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
            RestSeguridad.usuario(contexto, true);
        }

        contexto.sesion().setValidaSegundoFactorClaveLink(true);
        contexto.insertarLogEnvioOtp(contexto, null, null, null, true, "A");

        SqlClaveRedLinkUso.insertarClave(idCobis, tarjetaDebito.numero(), claveLinkHasheada, "MB", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));

        return RespuestaMB.exito();
    }

    public static byte[] pedirCaptcha(ContextoMB contexto) {
        Captcha.Builder captchaBuilder = new Captcha.Builder(140, 50);
        captchaBuilder.addText();
        captchaBuilder.addBackground(new GradiatedBackgroundProducer());
        captchaBuilder.addBorder();
        captchaBuilder.addNoise();
        captchaBuilder.gimp(new RippleGimpyRenderer());

        Captcha captcha = captchaBuilder.build();
        contexto.sesion().setCaptcha(captcha.getAnswer());

        byte[] imagen;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(captcha.getImage(), "jpg", baos);
            baos.flush();
            imagen = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            return null;
        }

        contexto.setHeader("Content-Type", "image/jpeg");
        return imagen;
    }

    public static RespuestaMB validarCaptcha(ContextoMB contexto) {
        String texto = contexto.parametros.string("texto");

        if (texto.equals(contexto.sesion().captcha())) {
            contexto.sesion().setCaptcha("");
            contexto.sesion().setValidaCaptcha(true);
            return RespuestaMB.exito();
        }

        return RespuestaMB.estado("CAPTCHA_INVALIDO");
    }

    public static RespuestaMB solicitudTCO(ContextoMB contexto) {
        String calle = contexto.parametros.string("calle");
        String altura = contexto.parametros.string("altura");
        String codigoPostal = contexto.parametros.string("codigoPostal");
        String piso = contexto.parametros.string("piso");
        String departamento = contexto.parametros.string("departamento");
        String localidad = contexto.parametros.string("localidad");
        String entreCalles = contexto.parametros.string("entreCalles");
        String provincia = contexto.parametros.string("provincia");
        String telefono = contexto.parametros.string("telefono");

        Date hoy = new Date();

        String idTipoDocumento = contexto.persona().idTipoDocumento().toString();
        if (idTipoDocumento.length() == 1) {
            idTipoDocumento = "0" + idTipoDocumento;
        }

        SqlResponseMB sqlResponse = insertSolicitudTCO(new SimpleDateFormat("yyyyMMdd").format(hoy), new SimpleDateFormat("HHmmss").format(hoy), contexto.persona().apellidos() + ", " + contexto.persona().nombre(), idTipoDocumento, contexto.persona().numeroDocumento(), contexto.persona().cuit(), contexto.idCobis(), calle, entreCalles, altura, piso, departamento, codigoPostal, localidad, provincia, telefono);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB consultaPreguntasPorDefecto(ContextoMB contexto) {

        // boolean tienePreguntas = false;
        RespuestaMB respuesta = new RespuestaMB();

        ApiResponseMB responseDefecto = RestSeguridad.consultaPreguntasPorDefecto(contexto, 10);
        if (responseDefecto.hayError()) {
            return RespuestaMB.error();
        }

        for (Objeto item : responseDefecto.objetos()) {
            Objeto pregunta = new Objeto();
            pregunta.set("orden", item.string("orden"));
            pregunta.set("texto", item.string("texto"));
            respuesta.add("preguntas", pregunta);
        }
        return respuesta;
    }

    public static RespuestaMB consultaPreguntasDesafio(ContextoMB contexto) {
        Integer cantidad = contexto.parametros.integer("cantidad");

        RespuestaMB respuesta = new RespuestaMB();

        ApiResponseMB response = RestSeguridad.consultaPreguntasPorCliente(contexto, cantidad);
        if (response.hayError()) {
            if (response.string("mensajeAlUsuario").contains("the challenge size cannot be larger than the number of questions stored for the user")) {
                return RespuestaMB.estado("NO_TIENE_PREGUNTAS");
            }
            if (response.string("mensajeAlUsuario").contains("authentication attempts exceeded")) {
                return RespuestaMB.estado("INTENTOS_TERMINADOS");
            }
            return RespuestaMB.error();
        }
        for (Objeto item : response.objetos()) {
            Objeto pregunta = new Objeto();
            pregunta.set("orden", item.string("orden"));
            pregunta.set("texto", item.string("texto"));
            respuesta.add("preguntas", pregunta);
        }
        return respuesta;
    }

    public static RespuestaMB enrolarPreguntasDesafio(ContextoMB contexto) {
        Integer orden1 = contexto.parametros.integer("orden1");
        String pregunta1 = contexto.parametros.string("pregunta1");
        String respuesta1 = contexto.parametros.string("respuesta1");
        Integer orden2 = contexto.parametros.integer("orden2");
        String pregunta2 = contexto.parametros.string("pregunta2");
        String respuesta2 = contexto.parametros.string("respuesta2");
        Integer orden3 = contexto.parametros.integer("orden3");
        String pregunta3 = contexto.parametros.string("pregunta3");
        String respuesta3 = contexto.parametros.string("respuesta3");
        Integer orden4 = contexto.parametros.integer("orden4");
        String pregunta4 = contexto.parametros.string("pregunta4");
        String respuesta4 = contexto.parametros.string("respuesta4");
        Integer orden5 = contexto.parametros.integer("orden5");
        String pregunta5 = contexto.parametros.string("pregunta5");
        String respuesta5 = contexto.parametros.string("respuesta5");
        RespuestaMB respuesta = new RespuestaMB();

        if (Objeto.anyEmpty(orden1, pregunta1, respuesta1, orden2, pregunta2, respuesta2, orden3, pregunta3, respuesta3, orden4, pregunta4, respuesta4, orden5, pregunta5, respuesta5)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // emm-esto es para operar preguntas seguridad-->comentar si no tiene que pasar
        // a prod
        if (!contexto.validaSegundoFactor("operar-preguntas-seguridad")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        pregunta1 = pregunta1.trim();
        pregunta2 = pregunta2.trim();
        pregunta3 = pregunta3.trim();
        pregunta4 = pregunta4.trim();
        pregunta5 = pregunta5.trim();
        respuesta1 = respuesta1.trim();
        respuesta2 = respuesta2.trim();
        respuesta3 = respuesta3.trim();
        respuesta4 = respuesta4.trim();
        respuesta5 = respuesta5.trim();

        if (pregunta1.equals(pregunta2) || pregunta1.equals(pregunta3) || pregunta1.equals(pregunta4) || pregunta1.equals(pregunta5)) {
            return RespuestaMB.estado("PREGUNTAS_REPETIDAS");
        }
        if (pregunta2.equals(pregunta3) || pregunta2.equals(pregunta4) || pregunta2.equals(pregunta5)) {
            return RespuestaMB.estado("PREGUNTAS_REPETIDAS");
        }
        if (pregunta3.equals(pregunta4) || pregunta3.equals(pregunta5)) {
            return RespuestaMB.estado("PREGUNTAS_REPETIDAS");
        }
        if (pregunta4.equals(pregunta5)) {
            return RespuestaMB.estado("PREGUNTAS_REPETIDAS");
        }

        if (respuesta1.equals(respuesta2) || pregunta1.equals(respuesta3) || respuesta1.equals(respuesta4) || pregunta1.equals(respuesta5)) {
            return RespuestaMB.estado("RESPUESTAS_REPETIDAS");
        }
        if (respuesta2.equals(respuesta3) || pregunta2.equals(respuesta4) || pregunta2.equals(respuesta5)) {
            return RespuestaMB.estado("RESPUESTAS_REPETIDAS");
        }
        if (respuesta3.equals(respuesta4) || pregunta3.equals(respuesta5)) {
            return RespuestaMB.estado("RESPUESTAS_REPETIDAS");
        }
        if (respuesta4.equals(pregunta5)) {
            return RespuestaMB.estado("RESPUESTAS_REPETIDAS");
        }

        ApiResponseMB responseBlanqueo = RestSeguridad.blanquearPreguntasDesafio(contexto);
        if (responseBlanqueo.hayError()) {
            if (!"USER_NOT_EXIST".equals(responseBlanqueo.string("codigo"))) {
                return RespuestaMB.error();
            }
        }

        Objeto preguntas = new Objeto();
        Objeto item1 = new Objeto();
        Objeto item2 = new Objeto();
        Objeto item3 = new Objeto();
        Objeto item4 = new Objeto();
        Objeto item5 = new Objeto();
        item1.set("orden", orden1);
        item1.set("pregunta", pregunta1);
        item1.set("respuesta", respuesta1);
        item2.set("orden", orden2);
        item2.set("pregunta", pregunta2);
        item2.set("respuesta", respuesta2);
        item3.set("orden", orden3);
        item3.set("pregunta", pregunta3);
        item3.set("respuesta", respuesta3);
        item4.set("orden", orden4);
        item4.set("pregunta", pregunta4);
        item4.set("respuesta", respuesta4);
        item5.set("orden", orden5);
        item5.set("pregunta", pregunta5);
        item5.set("respuesta", respuesta5);

        preguntas.add(item1);
        preguntas.add(item2);
        preguntas.add(item3);
        preguntas.add(item4);
        preguntas.add(item5);
        ApiRequestMB request = ApiMB.request("CargarPreguntasDesafio", "seguridad", "POST", "/v1/preguntas/{idcliente}", contexto);
        request.path("idcliente", contexto.idCobis());
        request.body("grupo", "ClientesBH");
        request.body("idcliente", contexto.idCobis());
        request.body("preguntas", preguntas);
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        ApiMB.eliminarCache(contexto, "UsuarioIDG", contexto.idCobis());
        contexto.limpiarSegundoFactor();
        return respuesta;
    }

    // TODO eliminar TCO
    public static RespuestaMB validarTarjetaCoordenadasPreproducidas(ContextoMB contexto) {
        return RespuestaMB.error();
    }

    // TODO eliminar TCO
    public static RespuestaMB tieneTCO(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        boolean tieneTarjeta = false;
        respuesta.set("tieneTarjeta", tieneTarjeta);

        return respuesta;
    }

    public static void otpLog(ContextoMB contexto, String funcionalidad, String mensaje, HashMap<String, Boolean> habilitados, String respuesta) {
        Objeto detalle = new Objeto();
        detalle.set("peticion", "api/canales-otp-usuario");
        detalle.set("Funcionalidad", funcionalidad);
        if (habilitados != null) {
            detalle.set("otpSegundoFactor", habilitados.get("otpSegundoFactor"));
            detalle.set("smsHabilitado", habilitados.get("smsHabilitado"));
            detalle.set("emailHabilitado", habilitados.get("emailHabilitado"));
            detalle.set("tcoHabilitado", habilitados.get("tcoHabilitado"));
            detalle.set("linkHabilitado", habilitados.get("linkHabilitado"));
        }
        AuditorLogService.otpLogVisualizador(contexto, "Api-seguridad_canalesOTPorUsuario", detalle, respuesta);
    }

    public static RespuestaMB canalesOTPorUsuario(ContextoMB contexto) {
        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        RespuestaMB respuestaOTP = null;
        respuestaOTP = validaCanalesActivosV2(contexto);

        if (respuestaOTP.hayError()) {
            otpLog(contexto, contexto.parametros.string("funcionalidad"), "ERROR_VALIDADORES_USUARIO", null, "");
            return RespuestaMB.estado("ERROR_VALIDADORES_USUARIO");
        }
        return respuestaOTP;
    }

    public static RespuestaMB validarPreguntasSeguridad(ContextoMB contexto) {
        String operacion = contexto.parametros.string("operacion"); // activacion/desbloqueo/baja
        Integer orden1 = contexto.parametros.integer("orden1");
        String respuesta1 = contexto.parametros.string("respuesta1");
        Integer orden2 = contexto.parametros.integer("orden2");
        String respuesta2 = contexto.parametros.string("respuesta2");

        if (Objeto.anyEmpty(operacion, orden1, respuesta1)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        int cantidadPreguntas = 0;
        if ("activacion".equals(operacion)) {
            cantidadPreguntas = 1;
        }
        if ("desbloqueo".equals(operacion) || "baja".equals(operacion)) {
            cantidadPreguntas = 2;
        }

        if (cantidadPreguntas == 0) {
            return RespuestaMB.error();
        }

        contexto.limpiarSegundoFactor();

        Objeto respuestas = new Objeto();
        Objeto item1 = new Objeto();
        item1.set("orden", orden1);
        item1.set("respuesta", respuesta1);
        respuestas.add(item1);
        if (cantidadPreguntas == 2) {
            Objeto item2 = new Objeto();
            item2.set("orden", orden2);
            item2.set("respuesta", respuesta2);
            respuestas.add(item2);
        }

        ApiResponseMB responseRespuesta = RestSeguridad.respuestaDesafio(contexto, cantidadPreguntas, respuestas);
        if (responseRespuesta.hayError()) {
            if ("INVALID_RESPONSE".equals(responseRespuesta.string("codigo"))) {
                return RespuestaMB.estado("RESPUESTA_INVALIDA");
            }
            if ("AUTH_FAILED_USER_LOCKED".equals(responseRespuesta.string("codigo"))) {
                return RespuestaMB.estado("USUARIO_BLOQUEADO");
            }
            return RespuestaMB.error();
        }

        contexto.sesion().setValidaSegundoFactorPreguntasPersonales(true);

        return RespuestaMB.exito();

    }

    public static RespuestaMB desbloquearTCO(ContextoMB contexto) {

        if (!contexto.validaSegundoFactor("operar-tco")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        ApiResponseMB responseDesbloqueo = RestSeguridad.desbloquearTCO(contexto);
        if (responseDesbloqueo.hayError()) {
            return RespuestaMB.error();
        }
        contexto.limpiarSegundoFactor();
        ApiMB.eliminarCache(contexto, "UsuarioIDG", contexto.idCobis()); // elimino la cache para que reconsulte en la
        // consolidada de tco el estado
        ApiMB.eliminarCache(contexto, "SeguridadGetTarjetaCoordenadas", contexto.idCobis());
        return RespuestaMB.exito();
    }

    // TODO eliminar TCO
    public static RespuestaMB bajaTCO(ContextoMB contexto) {
        return RespuestaMB.error();
    }

    // TODO eliminar TCO
    public static RespuestaMB pedirActivacionTCO(ContextoMB contexto) {
        return RespuestaMB.error();
    }

    public static RespuestaMB historialActividades(ContextoMB contexto) {
        String tipoActividad = contexto.parametros.string("tipoActividad");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);

        if (Objeto.anyEmpty(tipoActividad, fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        fechaHasta = fechaHasta + "T23:59:59";

        switch (tipoActividad) {
            case "Todos":
                tipoActividad = "Todos";
                break;
            case "ingresos":
                tipoActividad = "Accesos";
                break;
            case "adhesion":
                tipoActividad = "Adhesion";
                break;
            case "cambio-clave":
                tipoActividad = "Cambio Clave";
                break;
            case "bloqueo-clave":
                tipoActividad = "Bloqueo Clave";
                break;
            case "desbloqueo-clave":
                tipoActividad = "Desbloqueo Clave";
                break;
            case "vencimiento-clave":
                tipoActividad = "Vencimiento de Clave";
                break;
            case "preguntas-personales":
                tipoActividad = "PreguntasPersonales";
                break;
        }

        ApiResponseMB response = RestSeguridad.historialActividades(contexto, tipoActividad);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto actividades = new Objeto();
        for (Objeto item : response.objetos()) {
            String fecha = item.date("fechaHora", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy hh:mm");
            if (item.string("fechaHora").compareTo(fechaDesde) >= 0 && item.string("fechaHora").compareTo(fechaHasta) <= 0) {
                Objeto actividad = new Objeto();
                actividad.set("canal", item.string("canal"));
                actividad.set("idOperacion", item.string("idOperacion"));
                actividad.set("fecha", fecha);
                actividad.set("actividad", item.string("descripcion"));
                actividades.add(actividad);
            }
        }

        return RespuestaMB.exito("actividades", actividades);
    }

    public static RespuestaMB consultaEstadoTCO(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB usuarioIDG = RestSeguridad.usuarioCache(contexto);

        if (usuarioIDG.hayError()) {
            return RespuestaMB.error();
        }

        respuesta.set("estadoTCO", "");
        respuesta.set("tieneTCO", usuarioIDG.bool("tieneTarjetaCoordenadas"));
        respuesta.set("tienePreguntas", usuarioIDG.bool("tienePreguntas"));
        for (Objeto item : usuarioIDG.objetos("estadosTarjetaCoordenadas")) {
            if ("CURRENT".equals(item.string("state"))) {
                respuesta.set("estadoTCO", item.string("state"));
                break;
            }
        }
        Integer cantIntentosTarjetaCoordenada = usuarioIDG.integer("cantidadIntentos.tarjetaCoordenadas");
        Integer cantIntentosPreguntasTarjetaCoordenada = usuarioIDG.integer("cantidadIntentos.preguntas");
        if (cantIntentosTarjetaCoordenada == null) {
            cantIntentosTarjetaCoordenada = 5;
        }
        if (cantIntentosPreguntasTarjetaCoordenada == null) {
            cantIntentosPreguntasTarjetaCoordenada = 5;
        }

        respuesta.set("tcoBloqueada", cantIntentosTarjetaCoordenada == 0);
        respuesta.set("tcoPreguntasBloqueadas", cantIntentosPreguntasTarjetaCoordenada == 0);

        return respuesta;
    }

    public static SqlResponseMB insertSolicitudTCO(String stco_fecha, String stco_hora, String stco_nombre, String stco_tipo_doc, String stco_nro_doc, String stco_cuit, String stco_idCobis, String stco_calle, String stco_entreCalles, String stco_numero, String stco_piso, String stco_departamento, String stco_codigoPostal, String stco_localidad, String stco_provincia, String stco_telefono) {
        SqlRequestMB sqlRequest = SqlMB.request("InsertSolicitudTCO", "homebanking");

        sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[solicitudTCO] ( ";
        sqlRequest.sql += "[stco_fecha],";
        sqlRequest.sql += "[stco_hora],";
        sqlRequest.sql += "[stco_nombre],";
        sqlRequest.sql += "[stco_tipo_doc],";
        sqlRequest.sql += "[stco_nro_doc],";
        sqlRequest.sql += "[stco_cuit],";
        sqlRequest.sql += "[stco_idCobis],";
        sqlRequest.sql += "[stco_calle],";
        sqlRequest.sql += "[stco_entreCalles],";
        sqlRequest.sql += "[stco_numero],";
        sqlRequest.sql += "[stco_piso],";
        sqlRequest.sql += "[stco_departamento],";
        sqlRequest.sql += "[stco_codigoPostal],";
        sqlRequest.sql += "[stco_localidad],";
        sqlRequest.sql += "[stco_provincia],";
        sqlRequest.sql += "[stco_telefono],";
        sqlRequest.sql += "[stco_recibirInfo],";
        sqlRequest.sql += "[stco_procesado]";
        sqlRequest.sql += ")";

        sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        sqlRequest.add(stco_fecha);
        sqlRequest.add(stco_hora);
        sqlRequest.add(stco_nombre);
        sqlRequest.add(stco_tipo_doc);
        sqlRequest.add(stco_nro_doc);
        sqlRequest.add(stco_cuit);
        sqlRequest.add(stco_idCobis);
        sqlRequest.add(stco_calle);
        sqlRequest.add(stco_entreCalles);
        sqlRequest.add(stco_numero);
        sqlRequest.add(stco_piso);
        sqlRequest.add(stco_departamento);
        sqlRequest.add(stco_codigoPostal);
        sqlRequest.add(stco_localidad);
        sqlRequest.add(stco_provincia);
        sqlRequest.add(stco_telefono);
        sqlRequest.add(0);
        sqlRequest.add(0);

        return SqlMB.response(sqlRequest);
    }

    public static RespuestaMB accessTokenGire(ContextoMB contexto) {

        // Se dio de alta en la DB
        SqlResponseMB responseSql = RestSeguridad.usuarioGireSql(contexto);
        if (responseSql.hayError) {
            return RespuestaMB.estado("ERROR_BASE_DE_DATOS_PRE_POST");
        }
        if (responseSql.registros.isEmpty()) {
            return RespuestaMB.error();
        }

        // Buscamos la informacion en GIRE
        String usuario = contexto.persona().cuit();

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB accessToken = RestSeguridad.accessTokenGire(contexto, usuario);

        if (accessToken.hayError()) {
            return RespuestaMB.error();
        }

        respuesta.set("aToken", accessToken.string("tokenLogin"));

        return respuesta;
    }

    public static RespuestaMB esGireUsuario(ContextoMB contexto) {

        SqlResponseMB responseSql = RestSeguridad.usuarioGireSql(contexto);
        if (responseSql.hayError) {
            return RespuestaMB.estado("ERROR_BASE_DE_DATOS_PRE_POST");
        }
        if (responseSql.registros.isEmpty()) {
            return RespuestaMB.error();
        } else {
            return RespuestaMB.exito();
        }

    }

    protected static RespuestaMB validaCanalesActivos(ContextoMB contexto) {
        String funcionalidad = contexto.parametros.string("funcionalidad");
        RespuestaMB respuesta = new RespuestaMB();
        String email = null;
        String celular = null;
        Boolean tieneTCO = false;
        Boolean tieneTD = false;

        RespuestaMB validadoresActivos = MBPersona.validadoresSegundoFactor(contexto);
        if (validadoresActivos.hayError()) {
            return validadoresActivos;
        }

        for (Objeto registro : validadoresActivos.objetos()) {
            String validador = registro.string("validadoresUsuario");
            if (!validador.isEmpty() && validador.contains("sms")) {
                celular = RestPersona.numeroCelular(contexto, contexto.persona().cuit());
            }
            if (!validador.isEmpty() && validador.contains("email")) {
                email = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
            }
            if (!validador.isEmpty() && validador.contains("tco")) {
                tieneTCO = true;
            }
            if (!validador.isEmpty() && validador.contains("red-link")) {
                tieneTD = true;
            }
        }

        if (email != null && !email.isEmpty()) {
            Objeto item = new Objeto();
            item.set("id", "EMAIL_EMP");
            item.set("descripcion", email);
            item.set("tipo", "email");
            respuesta.add("canales", item);
        }

        if (celular != null && !celular.isEmpty()) {
            Objeto item = new Objeto();
            item.set("id", "SMS_E");
            item.set("descripcion", celular);
            item.set("tipo", "celular");
            respuesta.add("canales", item);
        }

        if (tieneTD && contexto.tarjetaDebitoPorDefecto() != null && !funcionalidad.equalsIgnoreCase("transferencia")) {
            Objeto item = new Objeto();
            item.set("id", "LINK_E");
            item.set("descripcion", "Clave Red Link");
            item.set("tipo", "Red Link");
            respuesta.add("canales", item);
        }

        if (tieneTCO) {
            Objeto tco = new Objeto();
            tco.set("id", "TCO_E");
            tco.set("descripcion", "Tarjeta de Coordenadas");
            tco.set("tipo", "TCO");
            respuesta.add("canales", tco);
        }

        return respuesta;
    }

    private static RespuestaMB validaCanalesActivosV2(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        String email = null;
        String celular = null;
        Boolean tieneTCO = false;
        Boolean tieneBuhoFacil = false;
        Boolean tieneTD = false;
        Boolean tieneRiesgoNet = false;

        RespuestaMB validadoresActivos = MBPersona.validadoresSegundoFactor(contexto);
        if (validadoresActivos.hayError()) {
            return validadoresActivos;
        }

        for (Objeto registro : validadoresActivos.objetos()) {
            String validador = registro.string("validadoresUsuario");
            if (!validador.isEmpty() && validador.contains("sms")) {
                celular = RestPersona.numeroCelular(contexto, contexto.persona().cuit());
            }
            if (!validador.isEmpty() && validador.contains("email")) {
                email = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
            }
            if (!validador.isEmpty() && validador.contains("tco")) {
                tieneTCO = true;
            }
            if (!validador.isEmpty() && validador.contains("buhoFacil")) {
                tieneBuhoFacil = true;
            }
            if (!validador.isEmpty() && validador.contains("red-link")) {
                tieneTD = true;
            }
            if (validador.contains("riesgo-net")) {
                tieneRiesgoNet = true;
            }
        }

        if (tieneBuhoFacil && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_dobleFactor_buhofacil")) {
            Objeto buho = new Objeto();
            buho.set("id", "BUHOFACIL_E");
            buho.set("descripcion", "Clave Búho Fácil");
            buho.set("tipo", "Búho Fácil");
            respuesta.add("canales", buho);
        }

        if (email != null && !email.isEmpty()) {
            Objeto item = new Objeto();
            item.set("id", "EMAIL_EMP");
            item.set("descripcion", email);
            item.set("tipo", "email");
            respuesta.add("canales", item);
        }

        if (celular != null && !celular.isEmpty() && StringUtils.containsAny(celular, "ERROR")) {
            respuesta.set("error", celular);
        } else if (celular != null && !celular.isEmpty()) {
            Objeto item = new Objeto();
            item.set("id", "SMS_E");
            item.set("descripcion", celular);
            item.set("tipo", "celular");
            respuesta.add("canales", item);
        }

        if (tieneTD) {
            Objeto item = new Objeto();
            item.set("id", "LINK_E");
            item.set("descripcion", "Clave Red Link");
            item.set("tipo", "Red Link");
            respuesta.add("canales", item);
        }

        if (tieneTCO) {
            Objeto tco = new Objeto();
            tco.set("id", "TCO_E");
            tco.set("descripcion", "Tarjeta de Coordenadas");
            tco.set("tipo", "TCO");
            respuesta.add("canales", tco);
        }

        if (tieneRiesgoNet) {
            Objeto rn = new Objeto();
            rn.set("id", "PI_E");
            rn.set("descripcion", "Información personal");
            rn.set("tipo", "PersonalInformation");
            respuesta.add("canales", rn);
        }

        MBSeguridad.otpLog(contexto, contexto.parametros.string("funcionalidad"), "", null, respuesta.toString());
        return respuesta;
    }

    public static Object getClienteExterior(ContextoMB contexto) {
        String idCobis = contexto.parametros.string("idCobis", contexto.idCobis());

        if (Objeto.anyEmpty(idCobis)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Boolean permitirEmailRecuperoClave = RestPersona.existeMuestreo("habilitar.mail.login", "true", idCobis);
        Boolean permitirEmailTransferencias = RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", idCobis);

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("permitirEmailRecuperoClave", permitirEmailRecuperoClave);
        respuesta.set("permitirEmailTransferencias", permitirEmailTransferencias);

        return respuesta;
    }

    public static RespuestaMB validarOTPSoftToken(ContextoMB contexto) {
        String otp = contexto.parametros.string("otp");

        if (Objeto.anyEmpty(otp))
            return RespuestaMB.parametrosIncorrectos();

        contexto.sesion().setValidadorUsado(null);
        Date fechaExpiracionOtp = contexto.sesion().expiracionOtp();
        if (!Objeto.empty(fechaExpiracionOtp) && new Date().getTime() > fechaExpiracionOtp.getTime())
            return RespuestaMB.estado("OTP_EXPIRADO");

        contexto.limpiarSegundoFactor();

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (!ConfigMB.esProduccion() && ("0".equals(otp) || "000000".equals(otp) || "111111".equals(otp) || "999999".equals(otp))) {
            contexto.sesion().setValidaSegundoFactorOtp(true);
            contexto.sesion().setExpiracionOtp(null);
            contexto.sesion().setValidadorUsado(contexto.sesion().validadorPedido());
            return RespuestaMB.exito();
        }

        if (ConfigMB.bool("deshabilitar_isva", false) && ConfigMB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
            RespuestaMB respuesta;
            String otpSesionFull = contexto.sesion().getOTP();
            String otpSesion = otpSesionFull != null ? otpSesionFull.split(":")[0] : null;
            String timestamp = otpSesionFull != null ? otpSesionFull.split(":")[1] : null;

            if (otpSesion == null || otpSesion.isEmpty()) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_NO_GENERADO");
            }

            if (timestamp == null || new Date().getTime() > Long.valueOf(timestamp) + ConfigMB.longer("duracionOTP", 2 * 60 * 1000L)) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_EXPIRADO");
            }

            if (!otpSesion.equals(otp)) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_INVALIDO");
            }

            contexto.sesion().setOTP(null);

            contexto.sesion().setValidaSegundoFactorOtp(true);
            contexto.sesion().setExpiracionOtp(null);
            contexto.sesion().setValidadorUsado(contexto.sesion().validadorPedido());
            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");
            return RespuestaMB.exito();
        }

        ApiRequestMB request = ApiMB.request("ValidarOTP", "seguridad", "GET", "/v1/clave", contexto);
        request.header("x-fingerprint", UUID.randomUUID().toString());
        request.query("grupo", "ClientesBH");
        request.query("idcliente", contexto.idCobis());
        request.query("clave", otp);
        request.query("nombreClave", "OTP");

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_otp_state_id")) {
            try {
                request.query("stateId", URLEncoder.encode(contexto.sesion().stateIdOtp(), "UTF-8"));
            } catch (Exception e) {
                request.query("stateId", contexto.sesion().stateIdOtp());
            }
            request.header("cookie", contexto.sesion().cookieOtp());
        }

        request.permitirSinLogin = true;

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {
            RespuestaMB respuesta;
            if (response.string("detalle").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_INVALIDO");
            }

            if (response.string("mensajeAlUsuario").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_INVALIDO");
            }

            if ("La clave ingresada es invalida, intente de nuevo".equals(response.string("mensajeError"))) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion().setStateIdOtp(response.string("stateId"));
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_INVALIDO");
            }

            if (response.string("mensajeError").contains("Authentication service received an invalid state ID")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion().setStateIdOtp(response.string("stateId"));
                respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                if (!Objeto.empty(respuesta))
                    return respuesta;
                return RespuestaMB.estado("OTP_INVALIDO_VOLVER_EMPEZAR");
            }

            respuesta = new MBSoftToken().intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
            if (!Objeto.empty(respuesta))
                return respuesta;
            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "E");
            return RespuestaMB.error();
        }

        contexto.sesion().setValidaSegundoFactorOtp(true);
        contexto.sesion().setExpiracionOtp(null);
        contexto.sesion().setValidadorUsado(contexto.sesion().validadorPedido());

        contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");

        return RespuestaMB.exito();
    }

    public static RespuestaMB limpiarValidadoresDrs(ContextoMB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();
        contexto.sesion().limpiarChallengeDrs();
        return RespuestaMB.exito();
    }
}