package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.math.BigDecimal;

public class Autorizar extends ApiObjeto {
    public Debin debin;

    public static Autorizar post(Contexto contexto, Debin debin, String operacion) {
        ApiRequest request = new ApiRequest("AceptarDebin", "debin", "POST", "/v1/debin/autorizar", contexto);

        request.body("debin.id", debin.id);
        request.body("debin.importe", debin.importe);

        String codigoEstado = null;
        if (operacion.equals("ACEPTAR")) codigoEstado = "00";
        else if (operacion.equals("RECHAZAR")) codigoEstado = "10";
        request.body("debin.estado.codigo", codigoEstado);

        request.body("debin.comprador.cliente.idTributario", debin.comprador.cliente.idTributario);
        request.body("debin.comprador.cliente.nombreCompleto", debin.comprador.cliente.nombreCompleto);
        request.body("debin.comprador.cliente.cuenta.cbu", debin.comprador.cliente.cuenta.cbu);
        request.body("debin.comprador.cliente.cuenta.numero", debin.comprador.cliente.cuenta.numero);
        request.body("debin.comprador.cliente.cuenta.tipo", debin.comprador.cliente.cuenta.tipo);
        request.body("debin.comprador.cliente.cuenta.moneda.id", debin.comprador.cliente.cuenta.moneda.id);
        request.body("debin.comprador.cliente.cuenta.moneda.descripcion", debin.comprador.cliente.cuenta.moneda.descripcion);
        request.body("debin.comprador.cliente.cuenta.moneda.signo", debin.comprador.cliente.cuenta.moneda.signo);
        request.body("debin.comprador.cliente.cuenta.sucursal.id", debin.comprador.cliente.cuenta.sucursal.id);
        request.body("debin.comprador.cliente.cuenta.sucursal.descripcion", debin.comprador.cliente.cuenta.sucursal.descripcion);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("ESTADO_NO_MODIFICAR", response.http(87), request, response);
        ApiException.throwIf("SECUENCIAL_COELSA_EN_USO", response.http(358487), request, response);
        ApiException.throwIf("RECHAZO_CLIENTE", response.contains("RECHAZO CLIENTE"), request, response);
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Autorizar.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("HB", "homologacion");
        BigDecimal importe = new BigDecimal(333.33);

        Estado estado = new Estado("00");

        Moneda moneda = new Moneda("80", "Pesos", "$");
        Sucursal sucursal = new Sucursal("0", "0");
        Cuenta cuenta = new Cuenta("0440000440000343674278", "400000034367427", "AHO", moneda, sucursal);
        Cliente cliente = new Cliente("20335028989", "MCGOOKEM SOL", cuenta);
        Comprador comprador = new Comprador(cliente);

        Debin debin = new Debin("4XJ8G7V95REDGOMNEMPYR0", importe, estado, comprador);

        Autorizar datos = post(contexto, debin, "ACEPTAR");
        imprimirResultado(contexto, datos);
    }
}
