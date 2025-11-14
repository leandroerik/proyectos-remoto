package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;

public class BajaRecurrencia extends ApiObjeto {

    public Integer id;

    public Response respuesta;
    public static class Response {
        public String codigo;
        public String descripcion;
        public String fechaNegocio;

    }



    public static BajaRecurrencia post(ContextoOB contexto, String id, DebinProgramadoOB objDebinProgramado){
        ApiRequest request = new ApiRequest("BajaRecurrencia", "debin", "DELETE", "/v1/compradores/recurrencias", contexto);
        request.body("activo", false);
        request.body("idRecurrencia", id);

        request.body("vendedor.cliente.idTributario", objDebinProgramado.cuitCreacion);
        request.body("vendedor.cliente.cuenta.banco","");
        request.body("vendedor.cliente.cuenta.cbu", "");

        request.body("comprador.cliente.idTributario", objDebinProgramado.compradorCuit);
        request.body("comprador.cliente.cuenta.banco","044");
        request.body("comprador.cliente.cuenta.cbu",objDebinProgramado.compradorCbu);

        request.body("debin.moneda.id",objDebinProgramado.debinMoneda.equals("032")?"80":"2");
        request.body("debin.moneda.descripcion", "Pesos");
        request.body("debin.moneda.signo", "$");
        request.body("debin.detalle",objDebinProgramado.debinDetalle);
        request.body("debin.prestacion",objDebinProgramado.debinPrestacion );
        request.body("debin.referencia",objDebinProgramado.debinReferencia );
        request.body("debin.concepto", objDebinProgramado.debinConcepto );

        request.body("detalle","");
        request.body("concepto", "");
        request.body("periodo", "");
        request.body("cantidad", "");
        request.body("tipo_adhesion", 0);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(BajaRecurrencia.class);

    }
}
