package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.homebanking.base.Objeto;

import java.util.List;

public class EndpointVendedores extends ApiObjeto {

    /* ========== ATRIBUTOS ========== */

    public String idTributario;
    public String nombreCompleto;
    public Contacto contacto;
    public List<Cuenta> cuentas;

    public static class RespuestaOk extends ApiObjeto {
        public Boolean ok;
    }

    public static EndpointVendedores get(Contexto contexto, String idTributario) {
        ApiRequest request = new ApiRequest("DebinVendedor", "debin", "GET", "/v1/vendedores/{idTributario}", contexto);
        request.path("idTributario", idTributario);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(EndpointVendedores.class, response.objeto("cliente"));
    }

    public static RespuestaOk post(Contexto contexto, String correo, String cbu, String idSucursal, String descSucursal, String cuit) {
        Objeto contacto = new Objeto();
        contacto.set("correoElectronico", correo);

        Objeto cuenta = new Objeto();
        cuenta.set("cbu", cbu);
        cuenta.set("sucursal").set("id", idSucursal).set("descripcion", descSucursal);

        Objeto cliente = new Objeto();
        cliente.set("idTributario", cuit);
        cliente.set("contacto", contacto);
        cliente.set("cuenta", cuenta);

        ApiRequest request = new ApiRequest("ActivarCuentaDebin", "debin", "POST", "/v1/vendedores", contexto);
        request.body("cliente", cliente);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        ApiException.throwIf("MAL_FORMULADO_VENDEDOR", response.http(404), request, response);
        return response.crear(RespuestaOk.class);
    }

    public static RespuestaOk delete(Contexto contexto, String idTributario, String cbu) {
        ApiRequest request = new ApiRequest("DesactivarCuentaDebin", "debin", "DELETE", "/v1/vendedores", contexto);
        request.query("idTributario", idTributario);
        request.query("cbu", cbu);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("VENDEDOR INEXISTENTE", response.contains("Vendedor inexistente"), request, response);
        ApiException.throwIf("CBU_INCORRECTO", response.http(1405), request, response);
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(RespuestaOk.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("HB", "homologacion");
        EndpointVendedores datos = get(contexto, "30707634401");
        //RespuestaOk datos = post(contexto, "nmtabucchi@hipotecario.com.ar", "0440000430000010856041", "0", "BUENOS AIRES", "30539222259");
        //RespuestaOk datos = delete(contexto, "30539222259", "0440000430000010856041");
        imprimirResultado(contexto, datos);
    }
}
