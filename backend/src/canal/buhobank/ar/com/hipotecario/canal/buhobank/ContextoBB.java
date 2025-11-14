package ar.com.hipotecario.canal.buhobank;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Encriptador;
import spark.Request;
import spark.Response;

public class ContextoBB extends Contexto {

    /* ========== ATRIBUTOS ========== */
    private SesionBB sesion;

    /* ========== CONSTRUCTORES ========== */
    public ContextoBB(String canal, String ambiente, String idCobis) {
        super(canal, ambiente, idCobis);
    }

    public ContextoBB(Request request, Response response, String canal, String ambiente) {
        super(request, response, canal, ambiente);
    }

    /* ========== METODOS ========== */
    public SesionBB sesion() {
        if (sesion == null) {
            sesion = super.sesion(SesionBB.class);
        }
        return sesion;
    }

    public Boolean prendidoOfertaMotor() {
        return GeneralBB.PRENDIDO_OFERTA_MOTOR;
    }

    public Boolean prendidoExpiracionSesion() {
        return GeneralBB.PRENDIDO_EXPIRACION_SESION;
    }

    public Boolean prendidoMaximoReintentos() {
        return GeneralBB.PRENDIDO_MAXIMO_REINTENTOS_POR_SESION;
    }

    public Boolean prendidoValidarFallecimiento() {
        return GeneralBB.PRENDIDO_VALIDAR_FALLECIMIENTO;
    }

    public Boolean prendidoLogsBase() {
        return GeneralBB.PRENDIDO_LOGS_BASE;
    }

    public String canalVenta1() {
        return super.canalVenta();
    }

    public String canalVenta2() {
        return "2211";
    }

    public String canalVenta3() {
        return "2230";
    }

    public String canalVenta4() {
        return "25001";
    }

    public String canalOriginacion1() {
        if (esProduccion() || esHomologacion()) {
            return "40";
        }

        return "42";
    }

    public String canalOriginacion2() {
        if (esProduccion() || esHomologacion()) {
            return "1063";
        }

        return "847";
    }

    public String canalOriginacion3() {
        if (esProduccion()) {
            return "ESAPRD";
        } else if (esHomologacion()) {
            return "ESACCS";
        }

        return "ESADES";
    }

    public String variablePorAmbiente(String variable) {

        if (esProduccion()) {
            return "PROD_" + variable;
        }

        return "HOMO_" + variable;
    }

    public String encriptar(String valor) {

        String clave = "Gc0JwpQH6FZjLg45A+0eAg==";
        return valor == null ? null : Encriptador.encriptarBase64(clave, valor);
    }

    public String getApiKeyBackOfficeVU(String key) {

        if (!Utils.isEmpty(key))
            return encriptar(key);

        String apiKey = config.string("buhobank_backoffice_apikey", null);

        return encriptar(apiKey);
    }

    @Override
    public String idSesionTransmit() {
        return sesion().cuil;
    }

}
