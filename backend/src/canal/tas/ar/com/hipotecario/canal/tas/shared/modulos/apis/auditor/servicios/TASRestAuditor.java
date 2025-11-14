package ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.servicios;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.modelos.negocio.TASReporte;

public class TASRestAuditor {

    public static Objeto generarReporte(ContextoTAS contexto, TASReporte reporte) {
        try {
            ApiRequest request = new ApiRequest(reporte.getServicio(), "auditor", "POST", "/v1/reportes", contexto);
            request.body("canal", reporte.getCanal());
            request.body("subCanal", reporte.getSubCanal());
            request.body("usuario", reporte.getUsuario());
            request.body("idProceso", reporte.getIdProceso());
            request.body("sesion", reporte.getSesion());
            request.body("servicio", reporte.getServicio());
            request.body("resultado", reporte.getResultado());
            request.body("duracion", reporte.getDuracion());

            request.body("mensajes.entrada", reporte.getMensajeEntrada().toJson());
            request.body("mensajes.salida", reporte.getMensajeSalida().toJson());
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            return  new Objeto().set("estado", "OK");
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
}
