package ar.com.hipotecario.backend.servicio.api.clientes;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.util.MethodEnum;

public class Cliente extends ApiObjeto {
    public static String GET_FECHA_PROX_PAGO = "JubiladoProximaFechaPago";
    public static String GET_ESTADO_BENEF = "JubiladoEstadoBeneficiario";
    public static String URL_FECHA_PROX_PAGO = "/v1/jubilados/{cuil}/fechaProxPago";
    public static String URL_ESTADO_BENEF = "/v1/jubilados/{cuil}/estadoBeneficio";

    public static class JubiladoPago extends ApiObjeto {
        private String fechaProximoPago;

        public String getFechaProximoPago() {
            return fechaProximoPago;
        }

        public void setFechaProxPago(String fechaProximoPago) {
            this.fechaProximoPago = fechaProximoPago;
        }

        public JubiladoPago() {
        }

        public JubiladoPago(String fechaProximoPago) {
            this.fechaProximoPago = fechaProximoPago;
        }
    }

    public static class JubiladoEstadoBeneficiario extends ApiObjeto {
        private Boolean inhabilitado;

        public Boolean getInhabilitado() {
            return inhabilitado;
        }

        public void setInhabilitado(Boolean inhabilitado) {
            this.inhabilitado = inhabilitado;
        }

        public JubiladoEstadoBeneficiario() {
        }

        public JubiladoEstadoBeneficiario(Boolean inhabilitado) {
            this.inhabilitado = inhabilitado;
        }
    }

    public static JubiladoPago getJubiladoFechaProxPago(Contexto contexto, String cuil) {
        ApiRequest request = new ApiRequest(GET_FECHA_PROX_PAGO, ApiClientes.API, MethodEnum.GET.name(), URL_FECHA_PROX_PAGO.replace("{cuil}", cuil), contexto);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        return response.crear(JubiladoPago.class);
    }

    public static JubiladoEstadoBeneficiario getJubiladoEstadoBeneficiario(Contexto contexto, String cuil) {
        ApiRequest request = new ApiRequest(GET_ESTADO_BENEF, ApiClientes.API, MethodEnum.GET.name(), URL_ESTADO_BENEF.replace("{cuil}", cuil), contexto);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        return response.crear(JubiladoEstadoBeneficiario.class);
    }

}
