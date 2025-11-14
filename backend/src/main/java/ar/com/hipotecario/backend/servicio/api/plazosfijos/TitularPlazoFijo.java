package ar.com.hipotecario.backend.servicio.api.plazosfijos;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.TitularPlazoFijo.TitularPF;

import java.util.Date;

public class TitularPlazoFijo extends ApiObjetos<TitularPF> {
    /* ========== ATRIBUTOS ========== */
    public static class TitularPF extends ApiObjeto {

        public String nombreCompleto;
        public String mODescripcion;
        public String direccion;
        public String oficina;
        public String prodBancDescripcion;
        public String codigoPostal;
        public String provincia;
        public String sucursalCuenta;
        public String direccionSucursalCuenta;
        public String ciudadSucursalCuenta;
        public String provinciaSucursalCuenta;
        public String cantidadDeTitulares;
        public String tipoDeDocumento;
        public String numeroDeDocumento;
        public static class RespuestaOk extends ApiObjeto {
            public Boolean ok;
        }
    }


    /* ========== SERVICIOS ========== */
    public static TitularPlazoFijo get(Contexto contexto, String fechaDesde, String fechaHasta,String numeroBanco) {
        ApiRequest request = new ApiRequest("API-PlazoFijo_ConsultaPlazosFijos", "plazosfijos", "GET", "/v1/titular", contexto);

        Parametros parametros = contexto.parametros;

        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.query("numero", numeroBanco);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        return response.crear(TitularPlazoFijo.class);
    }
}
