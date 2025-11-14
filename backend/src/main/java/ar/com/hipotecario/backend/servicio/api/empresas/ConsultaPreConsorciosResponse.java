package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.link.TarjetaVirtual;

public class ConsultaPreConsorciosResponse extends ApiObjetos<ConsultaPreConsorciosResponse.ConsultaPreConsorcio> {

    /* ========== ATRIBUTOS ========== */
    public static class ConsultaPreConsorcio extends ApiObjeto {
        public String cuenta;
        public String cuit;
        public String estado;
        public String fechaCarga;
        public String idCobis;
        public String nombreCuenta;
        public String preConsorcio;
        public String razonSocial;
    }

    /* =============== SERVICIOS ================ */

    public static ConsultaPreConsorciosResponse get(Contexto contexto, String cuenta, String cuit, String idCobis) {
        ApiRequest request = new ApiRequest("API-Empresas_ConsultaPreConsorcios", "empresas", "GET", "/v1/preConsorcios", contexto);
        request.query("cuenta", cuenta);
        request.query("cuit", cuit);
        request.query("idCobis", idCobis);

        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(ConsultaPreConsorciosResponse.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("OB", "desarrollo");
        ConsultaPreConsorciosResponse response = get(contexto, null, null, "8746677");
        imprimirResultado(contexto, response);
    }
}
