package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestSucursalVirtual {

    public static ApiResponseMB prestamoPersonalGET(ContextoMB contexto, String nroSolicitud, String prestamoPersonalID) {
        ApiRequestMB request = ApiMB.request("prestamoPersonalGET", "ventas_windows", "GET", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{numeroPrestamo}", contexto);
        request.path("numeroSolicitud", nroSolicitud);
        request.path("numeroPrestamo", prestamoPersonalID);
        return ApiMB.response(request, contexto.idCobis());
    }


    public static ApiResponseMB finalizarGestionCrm(ContextoMB contexto, String cobisTitular, String cuil, String aceptacionDigital, String idSolicitud) {
        ApiRequestMB apiRequestCRM = ApiMB.request("consultaaceptaciondigital", "CRM_Windows", "POST", "/consultaaceptaciondigital/post", contexto);
        apiRequestCRM.body("CUIL", cuil);
        apiRequestCRM.body("IdCobis", cobisTitular);
        apiRequestCRM.body("AceptacionDigital", aceptacionDigital);
        apiRequestCRM.body("IdSolicitud", idSolicitud);
        apiRequestCRM.body("stateCode", "0"); //El stateCode es un campo conjunto de opciones y se completa con numeros: 0 = Abierta | 1 = Ganada | 2 = Perdida
        return ApiMB.response(apiRequestCRM, contexto.idCobis());
    }

    public static ApiResponseMB getTerminosYCondiciones(String nroSolicitud, boolean todosAceptaron, ContextoMB contexto){
        ApiRequestMB request = ApiMB.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
        request.query("solicitudid",nroSolicitud );
        if (todosAceptaron){
            request.query("grupocodigo", "GRUPO3");
        }
        else{
            request.query("grupocodigo", "ACEPDIGV2");
        }
        //CARAT-VISUALIZACION
        //ACEPDIG
        request.query("canal", contexto.canal);
        request.header("x-cuil", contexto.persona().cuit());

        return ApiMB.response(request,nroSolicitud, contexto.idCobis());
    }
}
