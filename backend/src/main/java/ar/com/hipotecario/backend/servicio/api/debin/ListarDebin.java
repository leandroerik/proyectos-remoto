package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.util.List;

public class ListarDebin extends ApiObjeto {

    public class ListarDebinResponse extends ApiObjeto {
        public List<Debin> debins;
        public Listado listado;
    }

    public static ListarDebinResponse post(Contexto contexto, String idTributarioComprador, String banco, String fechaDesde, String fechaHasta, String idTributarioVendedor) {
        ApiRequest request = new ApiRequest("ListarDebin", "debin", "POST", "/v1/debin/listas", contexto);
        request.body("listado.tamano", 300);
        request.body("listado.pagina", 0);

        request.body("debin.creacion.fechaDesde", fechaDesde);
        request.body("debin.creacion.fechaHasta", fechaHasta);

        if (idTributarioComprador != null) {
            request.body("comprador.cliente.idTributario", idTributarioComprador);
            request.body("comprador.cliente.cuenta.banco", banco);
        }
        if (idTributarioVendedor != null) {
            request.body("vendedor.cliente.idTributario", idTributarioVendedor);
            request.body("vendedor.cliente.cuenta.banco", banco);
        }


        ApiResponse response = request.ejecutar();

        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(ListarDebinResponse.class);
    }

    public static void main(String[] args) {
        Contexto contexto = contexto("HB", "homologacion");
        ListarDebinResponse datos = post(contexto, null, "044", "2024-03-30T00:00:00.000Z", "2024-05-01T00:00:00.000Z", "20160004887");
        imprimirResultado(contexto, datos);
    }
}
