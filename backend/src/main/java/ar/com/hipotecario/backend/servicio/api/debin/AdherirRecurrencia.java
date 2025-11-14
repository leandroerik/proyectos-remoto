package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;

public class AdherirRecurrencia extends ApiObjeto {
    public static class Response {
        public String codigo;
        public String descripcion;
        public String fechaNegocio;
    }

    public int id;
    public Response respuesta;


    public static AdherirRecurrencia post(ContextoOB contexto, DebinProgramadoOB debin, boolean aceptarORechar){
        ApiRequest request = new ApiRequest("AceptarRecurrencia", "debin", "POST", "/v1/compradores/confirmarrecurrencia", contexto);

        request.body("vendedor.cliente.idTributario",debin.vendedorCuit );
        request.body("comprador.cliente.idTributario",debin.compradorCuit );
        request.body("comprador.cliente.cuenta.cbu", debin.compradorCbu);
        request.body("comprador.cliente.cuenta.banco", debin.compradorCbu.substring(0,3));
        if(debin.moneda.id == 80){
            request.body("debin.moneda.id", "032");
            request.body("debin.moneda.descripcion", "Pesos");
            request.body("debin.moneda.signo", "$");
        }
        if(debin.moneda.id == 2){
            request.body("debin.moneda.id", "840");
            request.body("debin.moneda.descripcion", "Dolares");
            request.body("debin.moneda.signo", "U$S");
        }
        request.body("debin.limite_cuotas", debin.debinLimiteCuotas);
        request.body("debin.importe", debin.monto);
        request.body("debin.detalle", debin.debinDetalle);
        request.body("debin.referencia", debin.debinReferencia);
        request.body("debin.concepto", debin.debinConcepto.substring(0,3));
        request.body("debin.prestacion", debin.debinReferencia);
        request.body("autorizacion", aceptarORechar);
        request.body("id", debin.idDebin);
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(AdherirRecurrencia.class);
        }

}
