package ar.com.hipotecario.canal.homebanking.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.RestSeguridad;
import ar.com.hipotecario.canal.homebanking.servicio.SqlClaveRedLinkUso;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.RippleGimpyRenderer;

public class HBSeguridad {

    private static final String NEMONICO_MODAL_CLAVE_BF = "MODAL_CLAVE";
    private static final String FORMATO_FECHA_ISO_8106 = "yyyy-MM-dd HH:mm:ss";

    public static Respuesta canalesOTP(ContextoHB contexto) {
        String funcionalidad = contexto.parametros.string("funcionalidad");
        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        Respuesta respuesta = new Respuesta();

        List<String> validadores = null;

        if (funcionalidad.equals("adhesion-canal")) {
            ApiResponse usuarioIDG = RestSeguridad.usuario(contexto);
            if (usuarioIDG.hayError()) {
                if (usuarioIDG.string("mensajeAlDesarrollador").contains("Connection reset no se encuentran ocurrencias configuradas, en el Sistema de control de errores")) {
                    return Respuesta.estado("COBIS_CAIDO");
                }
                return Respuesta.error();
            }
            if (usuarioIDG.bool("tieneClaveDefault", false) && usuarioIDG.bool("tieneClaveNumerica", false)) {
                funcionalidad = "cambio-clave-canal";
            }
        }

        validadores = RestPersona.validadoresSegundoFactor(contexto, funcionalidad, contexto.idCobis());

        if (validadores.contains("email") || "".equals(funcionalidad)) {
            String email = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
            if (email != null) {
                String emailOfuscado = email;
                try {
                    emailOfuscado = email.substring(0, 1) + "*****" + email.substring(email.indexOf("@") - 1);
                } catch (Exception e) {
                }
                Objeto item = new Objeto();
                item.set("id", "EMAIL_EMP");
                item.set("descripcion", emailOfuscado);
                item.set("tipo", "email");
                respuesta.add("canales", item);
            }
        }

        if (validadores.contains("sms") || "".equals(funcionalidad)) {
            String celular = RestPersona.numeroCelular(contexto, contexto.persona().cuit());
            if (celular != null) {
                String celularOfuscado = celular;
                try {
                    celularOfuscado = celular.substring(0, 1) + "*****" + celular.substring(celular.length() - 4);
                } catch (Exception e) {
                }
                Objeto item = new Objeto();
                item.set("id", "SMS_E");
                item.set("descripcion", celularOfuscado);
                item.set("tipo", "celular");
                respuesta.add("canales", item);
            }
        }

        if (validadores.contains("red-link") || "".equals(funcionalidad)) {
            Objeto item = new Objeto();
            item.set("id", "LINK");
            item.set("descripcion", "Clave Red Link");
            item.set("tipo", "LINK");
            respuesta.add("canales", item);
        }

        if (validadores.contains("soft-token")) {
            Objeto item = new Objeto();

            item.set("id", "SOFT_TOKEN");
            item.set("descripcion", "Soft Token");
            item.set("tipo", "SOFT_TOKEN");

            respuesta.add("canales", item);
        }
        respuesta.set("esMonoProductoTC", contexto.esMonoProductoTC());
        return respuesta;
    }

    public static Respuesta pedirOTP(ContextoHB contexto) {
        String idCanal = contexto.parametros.string("idCanal");
        String forzarEmail = contexto.parametros.string("email", null);
        String forzarTelefono = contexto.parametros.string("telefono", null);

        if (Objeto.anyEmpty(idCanal)) {
            return Respuesta.parametrosIncorrectos();
        }

        if ("DATAVALID_OTP".equals(idCanal)) {
            if (Objeto.anyEmpty(forzarEmail) && Objeto.anyEmpty(forzarTelefono)) {
                return Respuesta.parametrosIncorrectos();
            } else if (!Objeto.anyEmpty(forzarEmail) && !Objeto.anyEmpty(forzarTelefono)) {
                return Respuesta.parametrosIncorrectos();
            }
        } else {
            forzarEmail = null;
            forzarTelefono = null;
        }

        contexto.sesion.validadorPedido = (null);

        String idEmail = idCanal.startsWith("EMAIL_") ? idCanal.substring("EMAIL_".length()) : null;
        String idTelefono = idCanal.startsWith("SMS_") ? idCanal.substring("SMS_".length()) : null;

        // emm-20190807-desde --> Agrego esta lógica para que expire el otp a los cinco
        // minutos y que no puedan llamar varias veces al otp por front
        Date fechaExpiracionOtp = contexto.sesion.expiracionOtp;
        if (fechaExpiracionOtp != null && new Date().getTime() < fechaExpiracionOtp.getTime()) {
            return Respuesta.estado("OTP_SIN_EXPIRAR");
        }
        // emm-20190807-hasta

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        ApiResponse response = null;
        if (!ConfigHB.bool("deshabilitar_isva", false) && !ConfigHB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
            // Verificar que exista usuario en IDG
            RestSeguridad.usuario(contexto, true);

            // SeguridadGetOtp
            ApiRequest request = Api.request("SeguridadGetOtp", "seguridad", "GET", "/v1/clave/otp", contexto);
            request.query("grupo", "ClientesBH");
            request.query("idcliente", idCobis);
            request.permitirSinLogin = true;

            response = Api.response(request);
            if (response.hayError()) {
                return Respuesta.error();
            }
        } else {
            String otp = Util.randomHmacSHA1(100000, 999999).toString();

            Objeto item = new Objeto();
            item.set("clave", otp);
            item.set("stateId", "random");

            response = new ApiResponse();
            response.add("otp", item);
            response.headers.put("set-cookie", "random");

            contexto.sesion.otp = (otp + ":" + new Date().getTime());
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

        contexto.insertarLogEnvioOtp(contexto, telefono, email, null, null, "P");

        contexto.sesion.stateIdOtp = (response.objetos("otp").get(0).string("stateId"));
        contexto.sesion.cookieOtp = (response.headers.get("set-cookie"));

        if (email != null) {
            contexto.sesion.setEmailOtpDatavalid(email, idCanal);
            if (idCanal.startsWith("EMAIL_") && forzarEmail == null) {
                contexto.sesion.validadorPedido = ("email");
            }

            ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", email);
            requestMail.body("plantilla", ConfigHB.string("doppler_otp"));
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Banco Hipotecario - Código de verificación");
            parametros.set("TOKEN", otp);
            requestMail.permitirSinLogin = true;

            try {
                ApiResponse responseMail = Api.response(requestMail);

                if (responseMail.hayError()) {
                    return Respuesta.error();
                }
            } catch (Exception exception) {
                if (exception.toString().contains("SocketTimeoutException")) {
                    contexto.insertarLogEnvioOtp(contexto, null, email, null, null, "T");
                }
                return Respuesta.error();
            }
        } else if (telefono != null) {
            contexto.sesion.setTelefonoOtpDatavalid(telefono, idCanal);
            if (idCanal.startsWith("SMS_") && forzarTelefono == null) {
                contexto.sesion.validadorPedido = ("sms");
            }

            telefono = telefono.startsWith("0") ? telefono.substring(1) : telefono;
            String texto = "Nunca compartas esta clave. Es confidencial y de uso personal exclusivo. Tu codigo de seguridad Banco Hipotecario es " + otp;
            contexto.parametros.set("mensaje", texto);
            contexto.parametros.set("codigo", otp);
            contexto.parametros.set("telefono", telefono);
            Respuesta respuestaSendSms = new Respuesta();
            respuestaSendSms = HBNotificaciones.sendSmsOTP(contexto);
            if (respuestaSendSms.hayError()) {
                return Respuesta.error();
            }

        } else {
            return Respuesta.error();
        }

        // emm-20190807-desde --> Agrego esta lógica para que expire el otp al
        // minuto y que no puedan llamar varias veces al otp por front
        Calendar calendario = Calendar.getInstance();
        int minutos = 1;
        calendario.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE) + minutos);
        contexto.sesion.expiracionOtp = (calendario.getTime());
        // emm-20190807-hasta

        if (!ConfigHB.esProduccion()) {
            return Respuesta.exito("otp", otp);
        }
        return Respuesta.exito();
    }

    public static Respuesta validarOTP(ContextoHB contexto) {
        String otp = contexto.parametros.string("otp");

        if (Objeto.anyEmpty(otp)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (Objeto.anyEmpty(contexto.sesion.validadorPedido)) {
            return Respuesta.error();
        }

        contexto.sesion.validadorUsado = (null);

        Date fechaExpiracionOtp = contexto.sesion.expiracionOtp;
        if (fechaExpiracionOtp != null && new Date().getTime() > fechaExpiracionOtp.getTime()) {
            return Respuesta.estado("OTP_EXPIRADO");
        }
        contexto.limpiarSegundoFactorPreCondicionLink(false);

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        if (!ConfigHB.esProduccion() && ("0".equals(otp) || "000000".equals(otp) || "111111".equals(otp) || "999999".equals(otp))) {
            contexto.sesion.otp = (null);
            String validadorPedido = contexto.sesion.validadorPedido;

            if (contexto.sesion.tieneOtpDatavalid(validadorPedido)) {
                contexto.sesion.validarOtpDatavalid(validadorPedido);
            } else {
                contexto.sesion.validadorUsado = validadorPedido;
                contexto.sesion.validaSegundoFactorOtp = (true);
                validarDatavalidOtp(contexto, validadorPedido);
            }

            contexto.sesion.expiracionOtp = (null);
            contexto.sesion.save();
            return Respuesta.exito();
        }

        if (!ConfigHB.bool("deshabilitar_isva", false) && !ConfigHB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
        } else {
            String otpSesionFull = contexto.sesion.otp;
            String otpSesion = otpSesionFull != null ? otpSesionFull.split(":")[0] : null;
            String timestamp = otpSesionFull != null ? otpSesionFull.split(":")[1] : null;
            Long duracionOTP = ConfigHB.longer("duracionOTP", 2 * 60 * 1000L);

            if (otpSesion == null || otpSesion.isEmpty()) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return Respuesta.estado("OTP_NO_GENERADO");
            }

            if (timestamp == null || new Date().getTime() > Long.valueOf(timestamp) + duracionOTP) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return Respuesta.estado("OTP_EXPIRADO");
            }

            if (!otpSesion.equals(otp)) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return Respuesta.estado("OTP_INVALIDO");
            }

            contexto.sesion.otp = (null);
            String validadorPedido = contexto.sesion.validadorPedido;

            if (contexto.sesion.tieneOtpDatavalid(validadorPedido)) {
                contexto.sesion.validarOtpDatavalid(validadorPedido);
            } else {
                contexto.sesion.validadorUsado = validadorPedido;
                contexto.sesion.validaSegundoFactorOtp = (true);
                validarDatavalidOtp(contexto, validadorPedido);
            }

            contexto.sesion.expiracionOtp = (null);
            contexto.sesion.save();
            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");
            return Respuesta.exito();
        }

        ApiRequest request = Api.request("ValidarOTP", "seguridad", "GET", "/v1/clave", contexto);
        request.query("grupo", "ClientesBH");
        request.query("idcliente", idCobis);
        request.query("clave", otp);
        request.query("nombreClave", "OTP");
        try {
            request.query("stateId", URLEncoder.encode(contexto.sesion.stateIdOtp, "UTF-8"));
        } catch (Exception e) {
            request.query("stateId", contexto.sesion.stateIdOtp);
        }
        request.header("cookie", contexto.sesion.cookieOtp);

        request.permitirSinLogin = true;

        ApiResponse response = Api.response(request);
        if (response.hayError()) {
            if (response.string("detalle").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return Respuesta.estado("OTP_INVALIDO");
            }
            if (response.string("mensajeAlUsuario").contains("Invalid response to a challenge")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                return Respuesta.estado("OTP_INVALIDO");
            }
            if ("La clave ingresada es invalida, intente de nuevo".equals(response.string("mensajeError"))) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion.stateIdOtp = (response.string("stateId"));
                return Respuesta.estado("OTP_INVALIDO");
            }
            if (response.string("mensajeError").contains("Authentication service received an invalid state ID")) {
                contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "R");
                contexto.sesion.stateIdOtp = (response.string("stateId"));
                return Respuesta.estado("OTP_INVALIDO_VOLVER_EMPEZAR");
            }
            contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "E");
            return Respuesta.error();
        }

        contexto.sesion.otp = (null);
        String validadorPedido = contexto.sesion.validadorPedido;
        contexto.sesion.setChallengeOtp(false);

        if (contexto.sesion.tieneOtpDatavalid(validadorPedido)) {
            contexto.sesion.validarOtpDatavalid(validadorPedido);
        } else {
            contexto.sesion.validadorUsado = validadorPedido;
            contexto.sesion.validaSegundoFactorOtp = (true);
        }

        contexto.sesion.expiracionOtp = (null);
        contexto.insertarLogEnvioOtp(contexto, null, null, null, null, "A");

        return Respuesta.exito();
    }

    private static void validarDatavalidOtp(ContextoHB contexto, String validadorPedido) {
        if (!HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp")) return;

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

    public static Respuesta cantidadTarjetasCreditoActivas(ContextoHB contexto) {
        List<TarjetaDebito> tarjetasDebitoActivas = contexto.tarjetasDebitoRedLinkActivo();
        return Respuesta.exito("cantidadTarjetasDebito", tarjetasDebitoActivas.size());
    }

    public static Respuesta precondicionValidarClaveLink(ContextoHB contexto) {
        String clave = contexto.parametros.string("clave");
        String ultimosDigitos = contexto.parametros.string("ultimosDigitos", null);
        String regex = "^([0-9]{4})$";// regex para 4 digitos
        if (Objeto.anyEmpty(clave)) {
            return Respuesta.parametrosIncorrectos();
        }
        contexto.limpiarSegundoFactorPreCondicionLink();

        String idCobis = contexto.idCobis();
        if (idCobis == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        Pattern pattern = Pattern.compile(regex);
        if (ultimosDigitos != null && !(pattern.matcher(ultimosDigitos)).matches()) {
            return Respuesta.parametrosIncorrectos();
        }
        return null;
    }

    public static Respuesta validarClaveLink(ContextoHB contexto) {
        return validarClave(contexto);
    }

    public static Respuesta validarClaveLinkTransmit(ContextoHB contexto) {
        return Respuesta.exito("validacion", Transmit.generarRespuestaLink(ConfigHB.string("clave_secreta_transmit", ""), !validarClave(contexto).hayError()));
    }

    private static Respuesta validarClave(ContextoHB contexto) {
        String clave = contexto.parametros.string("clave");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String ultimosDigitos = contexto.parametros.string("ultimosDigitos", null);
        Respuesta precondition = precondicionValidarClaveLink(contexto);
        if (precondition != null)
            return precondition;

        TarjetaDebito tarjetaDebito = null;
        if (idTarjetaDebito != null && !idTarjetaDebito.isEmpty()) {
            tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        } else if (ultimosDigitos != null) {
            tarjetaDebito = contexto.tarjetasDebitoRedLinkActivo().stream().filter(td -> td.numero().endsWith(ultimosDigitos)).findFirst().orElse(null);
        } else
            tarjetaDebito = contexto.tarjetasDebitoRedLinkActivo().stream().findFirst().orElse(null);

        if (tarjetaDebito == null)
            return Respuesta.estado("NO_EXISTE_TARJETA_DEBITO");

        String claveLinkHasheada = Encriptador.sha256(clave);

        SqlResponse sqlResponse = SqlClaveRedLinkUso.consultarUltimoUso(contexto.idCobis(), tarjetaDebito.numero(), claveLinkHasheada);

        if (sqlResponse.hayError || (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()))
            return Respuesta.error();

        if (!ConfigHB.esProduccion() && "999999".equals(clave)) {
            contexto.sesion.validaSegundoFactorClaveLink = (true);
            if (StringUtils.isNotBlank(ConfigHB.string("hb_prendido_link_uso_cobis", "")) && Arrays.asList(ConfigHB.string("hb_prendido_link_uso_cobis", "").split("_")).stream().filter(c -> c.equals(contexto.idCobis())).count() == 1)
                SqlClaveRedLinkUso.insertarClave(contexto.idCobis(), tarjetaDebito.numero(), claveLinkHasheada, "HB", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));
            return Respuesta.exito();
        }

        // LinkPostVerificacion
        ApiRequest request = Api.request("LinkPostVerificacion", "link", "POST", "/v1/verificacion", contexto);
        request.body("cardId", tarjetaDebito.numero());
        request.body("pin", clave);
        request.permitirSinLogin = true;

        ApiResponse response = Api.response(request);
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

            return Respuesta.estado(estado);
        }

        // Verificar que exista usuario en IDG
        if (!ConfigHB.bool("deshabilitar_isva", false) && !ConfigHB.string("deshabilitar_isva_cobis", "").contains(contexto.idCobis())) {
            RestSeguridad.usuario(contexto, true);
        }

        contexto.sesion.validaSegundoFactorClaveLink = (true);
        contexto.insertarLogEnvioOtp(contexto, null, null, null, true, "A");

        SqlClaveRedLinkUso.insertarClave(contexto.idCobis(), tarjetaDebito.numero(), claveLinkHasheada, "HB", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));

        return Respuesta.exito();
    }

    public static byte[] pedirCaptcha(ContextoHB contexto) {
        Captcha.Builder captchaBuilder = new Captcha.Builder(140, 50);
        captchaBuilder.addText();
        captchaBuilder.addBackground(new GradiatedBackgroundProducer());
        captchaBuilder.addBorder();
        captchaBuilder.addNoise();
        captchaBuilder.gimp(new RippleGimpyRenderer());

        Captcha captcha = captchaBuilder.build();
        contexto.sesion.captcha = (captcha.getAnswer());

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

        contexto.responseHeader("Content-Type", "image/jpeg");
        return imagen;
    }

    public static Respuesta validarCaptcha(ContextoHB contexto) {
        String texto = contexto.parametros.string("texto");

        if (!ConfigHB.esProduccion() && texto.equals("00000")) {
            contexto.sesion.captcha = ("");
            contexto.sesion.validaCaptcha = (true);
            return Respuesta.exito();
        }
        if (texto.equals(contexto.sesion.captcha)) {
            contexto.sesion.captcha = ("");
            contexto.sesion.validaCaptcha = (true);
            return Respuesta.exito();
        }
        return Respuesta.estado("CAPTCHA_INVALIDO");
    }

    public static Respuesta historialActividades(ContextoHB contexto) {
        String tipoActividad = contexto.parametros.string("tipoActividad");
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);

        if (Objeto.anyEmpty(tipoActividad, fechaDesde, fechaHasta)) {
            return Respuesta.parametrosIncorrectos();
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

        ApiResponse response = RestSeguridad.historialActividades(contexto, tipoActividad);
        if (response.hayError()) {
            return Respuesta.error();
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

        return Respuesta.exito("actividades", actividades);
    }

    public static Respuesta usuarioGirePost(ContextoHB contexto) {

        String gireAdhesionPostAction = ConfigHB.string("gire_adhesion_post_action", "");

        if ("".equals(gireAdhesionPostAction)) {
            return Respuesta.estado("CONFIGURAR_POST_ADHESION");
        }

        SqlResponse responseSql = RestSeguridad.usuarioGireSql(contexto);
        if (responseSql.hayError) {
            return Respuesta.estado("ERROR_BASE_DE_DATOS_PRE_POST");
        }
        boolean usuarioNuevo = responseSql.registros.isEmpty();

        if (contexto.cuentas().isEmpty()) {
            return Respuesta.estado("SIN_CUENTAS");
        }
        ApiResponse responseGirePost;
        try {
            responseGirePost = RestSeguridad.usuarioGirePost(contexto);
            if (responseGirePost.hayError()) {
                return Respuesta.error();
            }
        } catch (Exception e) {
            return Respuesta.estado("ERROR_SUCURSAL".equals(e.getMessage()) ? "ERROR_SUCURSAL" : "ERROR");
        }

        if (usuarioNuevo) {
            SqlResponse responseInsertSql = RestSeguridad.insertarGireUsuarioSql(contexto);
            if (responseInsertSql.hayError) {
                return Respuesta.estado("ERROR_BASE_DE_DATOS_POS_POST");
            }
        } else {
            SqlResponse responseUpdateSql = RestSeguridad.updateGireUsuarioSql(contexto);
            if (responseUpdateSql.hayError) {
                return Respuesta.estado("ERROR_BASE_DE_DATOS_POS_POST");
            }
        }

        SqlResponse responseUpdateCuentasGire = RestSeguridad.updateCuentasGireUsuarioSql(contexto);
        if (responseUpdateCuentasGire.hayError) {
            return Respuesta.estado("ERROR_BASE_DE_DATOS_POS_POST");
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("mensaje", responseGirePost.string("mensaje"));
        respuesta.set("action", gireAdhesionPostAction);

        return respuesta;
    }

    public static Object getClienteExterior(ContextoHB contexto) {
        String idCobis = contexto.parametros.string("idCobis", contexto.idCobis());

        contexto.sesion.setChallengeOtp(false);

        if (Objeto.anyEmpty(idCobis)) {
            return Respuesta.parametrosIncorrectos();
        }

        Boolean permitirEmailRecuperoClave = RestPersona.existeMuestreo("habilitar.mail.login", "true", idCobis);
        Boolean permitirEmailTransferencias = RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", idCobis);

        Respuesta respuesta = new Respuesta();
        respuesta.set("permitirEmailRecuperoClave", permitirEmailRecuperoClave);
        respuesta.set("permitirEmailTransferencias", permitirEmailTransferencias);

        return respuesta;
    }

    public static Object putClienteExterior(ContextoHB contexto) {
        String idCobis = contexto.parametros.string("idCobis", contexto.idCobis());
        String permitirEmailRecuperoClave = contexto.parametros.string("permitirEmailRecuperoClave");
        String permitirEmailTransferencias = contexto.parametros.string("permitirEmailTransferencias");

        if (Objeto.anyEmpty(idCobis, permitirEmailRecuperoClave, permitirEmailTransferencias)) {
            return Respuesta.parametrosIncorrectos();
        }

        try {
            SqlRequest sqlRequest = Sql.request("DeleteMuestreo", "hbs");
            sqlRequest.sql = "DELETE FROM [Homebanking].[dbo].[muestreo] WHERE m_tipoMuestra = ? AND m_subid = ?";
            sqlRequest.add("habilitar.mail.login");
            sqlRequest.add(idCobis);
            Sql.response(sqlRequest);
        } catch (Exception e) {
        }

        try {
            SqlRequest sqlRequest = Sql.request("DeleteMuestreo", "hbs");
            sqlRequest.sql = "DELETE FROM [Homebanking].[dbo].[muestreo] WHERE m_tipoMuestra = ? AND m_subid = ?";
            sqlRequest.add("habilitar.mail.transferencias");
            sqlRequest.add(idCobis);
            Sql.response(sqlRequest);
        } catch (Exception e) {
        }

        try {
            SqlRequest sqlRequest = Sql.request("InsertMuestreo", "hbs");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[muestreo] (m_tipoMuestra, m_valor, m_subid) VALUES (?, ?, ?)";
            sqlRequest.add("habilitar.mail.login");
            sqlRequest.add(permitirEmailRecuperoClave);
            sqlRequest.add(idCobis);
            Sql.response(sqlRequest);
        } catch (Exception e) {
        }

        try {
            SqlRequest sqlRequest = Sql.request("InsertMuestreo", "hbs");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[muestreo] (m_tipoMuestra, m_valor, m_subid) VALUES (?, ?, ?)";
            sqlRequest.add("habilitar.mail.transferencias");
            sqlRequest.add(permitirEmailTransferencias);
            sqlRequest.add(idCobis);
            Sql.response(sqlRequest);
        } catch (Exception e) {
        }

        return getClienteExterior(contexto);
    }

    public static Respuesta fechaVencimientoClaveBuhoFacil(ContextoHB contexto) {
        try {

            // deshabilito esta funcion ya que consume la base de datos directamente
            // esto deberia consumirse a traves de api seguridad
            if (ConfigHB.bool("deshabilitar_alerta_vencimiento_buho_facil", true)) {
                return Respuesta.exito();
            }

            if (!HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "verificacion_vencimiento_clave")) {
                return Respuesta.exito();
            }

            SqlResponse response = Util.getContador(contexto, NEMONICO_MODAL_CLAVE_BF);

            if (!response.hayError && !response.registros.isEmpty()) {
                for (Objeto registro : response.registros) {
                    if (Util.diferenciaDias(registro.date("momento")) < 350) {
                        return Respuesta.exito();
                    }
                }
            }

            Respuesta respuesta = new Respuesta();
//			SqlResponse sqlResponse = SqlClientesOperadores.fechaVencimientoClaveBuhoFacil(contexto);
//			long diff = Util.diferenciaDias(sqlResponse.registros.get(0).date("fechaExpiracion"));
//			if (diff > 7) {
//				return Respuesta.exito();
//			}

//			respuesta.set("diasParaVencimiento", diff);
//			respuesta.setEstado("VENCE_PRONTO");
//			Util.contador(contexto, NEMONICO_MODAL_CLAVE_BF);
            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

}
