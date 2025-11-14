package ar.com.hipotecario.canal.homebanking.api;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.helper.IpListaBlancaHelper;
import ar.com.hipotecario.canal.homebanking.servicio.GoogleReCaptchaService;

public class HBGoogleCaptcha {

    private static final String FUNCIONALIDAD_APAGADA = "ERROR_FUNCIONALIDAD_APAGADA";
    private static final String VE_PRENDIDO_RECAPTCHA = "prendido_recaptcha";
    private static final String VE_PUBLIC_KEY_RECAPTCHA = "recaptcha_key_public";
    private static final String VE_ACTION_LOGIN_RECAPTCHA = "recaptcha_action_login";

    public static Respuesta verificarReCaptchaLogin(ContextoHB contexto) {
        String token = contexto.parametros.string("token", null);
        String documento = contexto.parametros.string("documento", null);

        if (Objeto.anyEmpty(token, documento))
            return Respuesta.parametrosIncorrectos();

        if (!HBAplicacion.funcionalidadPrendida(VE_PRENDIDO_RECAPTCHA))
            return Respuesta.estado(FUNCIONALIDAD_APAGADA);

        if (verificarRangoIpEstaListaBlanca(contexto)) {
            registrarListadoBlanco(contexto, documento);
            return Respuesta.exito();
        }

        return GoogleReCaptchaService.verificarRecaptchaLogin(documento, token, contexto.request.userAgent(), contexto.ip());
    }

    public static Respuesta config() {
        return Respuesta.exito("config", new Objeto()
                .set("reCaptchaKeyPublic", ConfigHB.string(VE_PUBLIC_KEY_RECAPTCHA, ""))
                .set("prendidoReCaptcha", HBAplicacion.funcionalidadPrendida(VE_PRENDIDO_RECAPTCHA))
                .set("reCaptchaActionLogin", ConfigHB.string(VE_ACTION_LOGIN_RECAPTCHA, "")));
    }

    private static boolean verificarRangoIpEstaListaBlanca(ContextoHB contexto) {
        return new IpListaBlancaHelper().estaIpEnRango(contexto.ip());
    }

    private static void registrarListadoBlanco(ContextoHB contexto, String documento) {
        GoogleReCaptchaService.registrarIpListaBlancaRecaptcha(documento, contexto.ip());
    }
}
