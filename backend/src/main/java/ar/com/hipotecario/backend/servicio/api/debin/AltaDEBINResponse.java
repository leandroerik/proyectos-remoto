package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.math.BigDecimal;

public class AltaDEBINResponse extends ApiObjeto {
        public Debin debin;


    public static AltaDEBINResponse post(Contexto contexto, String cbuComprador, String idTributario, String concepto, String banco, String idSucursal, String descSucursal, BigDecimal importe, Moneda moneda, Boolean recurrencia, String tiempoExpiracion, String cbuVendedor, String idTributarioVendedor) {
        ApiRequest request = new ApiRequest("AltaDebin", "debin", "POST", "/v1/debin", contexto);
        request.body("comprador.cliente.cuenta.cbu", cbuComprador);
        request.body("comprador.cliente.idTributario", idTributario);
        request.body("concepto", concepto);
        request.body("importe", importe);
        request.body("moneda", moneda);
        request.body("recurrencia", recurrencia);
        request.body("tiempoExpiracion", tiempoExpiracion);
        request.body("vendedor.cliente.cuenta.cbu", cbuVendedor);
        request.body("vendedor.cliente.cuenta.banco", banco);
        request.body("vendedor.cliente.cuenta.sucursal.id", idSucursal);
        request.body("vendedor.cliente.cuenta.sucursal.descripcion", descSucursal);
        request.body("vendedor.cliente.idTributario", idTributarioVendedor);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 201), request, response);
        ApiException.throwIf("NO_ADHERIDO_COMO_VENDEDOR", response.http(99), request, response);
        return response.crear(AltaDEBINResponse.class);
    }

    public static void main(String[] args) {
        Contexto contexto = contexto("HB", "homologacion");
        Moneda moneda = new Moneda("Pesos", "80", "$");
        AltaDEBINResponse datos = post(contexto, "0440007330000003713144", "30707634401", "VAR", "044","0018","0", BigDecimal.valueOf(10), moneda, false, "1440", "27236442514", "30539222259");
        imprimirResultado(contexto, datos);
    }
}


