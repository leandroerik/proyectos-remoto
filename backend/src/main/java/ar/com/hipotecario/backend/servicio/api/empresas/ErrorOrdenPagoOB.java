package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ErrorOrdenPagoOB extends ApiObjetos<ErrorOrdenPagoOB.ErroresOP> {
    public ErroresOP erroresOP;
    public static class ErroresOP extends ApiObjeto{
        public List<ErrorOrdenPagoOB.ErrorOP> errorOP;
    }
    public static class ErrorOP extends ApiObjeto{

        public int nroOrden;
        public int subNroOrden;
        public String nroSecError;
        public int tabla;
        public int codigo;
        public int subCodigo;
        public String descError;
        public String errorExterno;
        public String fecVal;
        public String horaVal;
        public String usrVal;
        public String rechazaOP;
    }
    public static ErrorOrdenPagoOB get(ContextoOB contexto, int nroOrden, int nroSubOrden) {
        ApiRequest request = new ApiRequest("API-Empresas_ErroresOrdenesPago", "empresas", "GET", "/v1/sat/erroresOrdenesPago", contexto);
        request.header("Content-Type", "application/json");
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());
        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID", "OB");
        request.query("id", "0");
        request.query("idUsrLog", "0");
        request.query("nroOrden",nroOrden);
        request.query("subNroOrden",nroSubOrden);
        request.query("tipoID", "1");

        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(ErrorOrdenPagoOB.class);

    }
}
