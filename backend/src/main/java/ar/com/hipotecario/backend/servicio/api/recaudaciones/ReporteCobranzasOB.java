package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ReporteCobranzasOB extends ApiObjeto {

    public Number montoTotal;
    public String paginado;

    public ArrayList<ReporteCobranza> reportes;

    public static class ReporteCobranza extends ApiObjeto {

        public String fechaDeposito;
        public String cuenta;
        public Integer moneda;
        public String fechaPago;
        public String formaPago;
        public String tipoCheque;
        public BigDecimal importe;
        public String nuemeroCheque;
        public String cuit;
        public String estadoCheque;
        public String sucursal;
        public String cuitCliente;
        public String fechaEmision;
        public String secuencial;
        public String grupo;
        public String servicio;
        public String ticket;
        public String numeroDepositante;
        public String nombreDepositante;
        public String numeroComprobante;
        public String vigencia;
        public String convenio;
    }

    public static ReporteCobranzasOB get(ContextoOB contexto, String estadoCheque, String fechaFin, String fechaInicio, String idConvenio, Integer tipoPago,int secuencial,Integer idProceso) {
        ApiRequest request = new ApiRequest("API-Recaudaciones-Reporte", "recaudaciones", "GET", "/v1/reporte", contexto);

        request.header("Content-Type", "application/json");
        if (estadoCheque != null) request.query("estadoCheque", estadoCheque);
        request.query("fechaFin", fechaFin);
        request.query("fechaInicio", fechaInicio);
        request.query("idConvenio", idConvenio);
        request.query("secuencial", secuencial);
        if (tipoPago != null) request.query("tipoPago", tipoPago);
        request.body("idProceso", idProceso);
        request.header("x-idProceso", idProceso.toString());

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204, 404), request, response);

        return response.crear(ReporteCobranzasOB.class);

    }
}
