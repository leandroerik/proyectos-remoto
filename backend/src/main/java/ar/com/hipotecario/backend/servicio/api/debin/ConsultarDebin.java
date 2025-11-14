package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ConsultarDebin extends ApiObjeto {

    public class ConsultaDebinResponse extends ApiObjeto {
        public Comprador comprador;
        public Detalle detalle;
        public Estado estado;
        public String fechaNegocio;
        public String garantiaOK;
        public String id;
        public String loteId;
        public String preautorizado;
        public String tipo;
        public String tipoTransaccion;
        public Vendedor vendedor;
    }

    public static ConsultaDebinResponse get(Contexto contexto, String id) {
        ApiRequest request = new ApiRequest("ConsultarDebin", "debin", "GET", "/v1/debin/{id}", contexto);
        request.path("id", id);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(ConsultaDebinResponse.class);
    }

    public static void main(String[] args) {
        Contexto contexto = contexto("OB", "homologacion");
        ConsultaDebinResponse datos = get(contexto, "V8D0Q619LKYRPP3N7JZ5RG");
        imprimirResultado(contexto, datos);
    }
}
