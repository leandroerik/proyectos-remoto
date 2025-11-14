package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestSucursalVirtual {



    public static ApiResponse prestamoPersonalGET(ContextoHB contexto, String nroSolicitud, String prestamoPersonalID) {
        ApiRequest request = Api.request("prestamoPersonalGET", "ventas_windows", "GET", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{numeroPrestamo}", contexto);
        request.path("numeroSolicitud", nroSolicitud);
        request.path("numeroPrestamo", prestamoPersonalID);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse finalizarGestionCrm(ContextoHB contexto, String cobisTitular, String cuil, String aceptacionDigital, String idSolicitud) {
        ApiRequest apiRequestCRM = Api.request("consultaaceptaciondigital", "CRM_Windows", "POST", "/consultaaceptaciondigital/post", contexto);
        apiRequestCRM.body("CUIL", cuil);
        apiRequestCRM.body("IdCobis", cobisTitular);
        apiRequestCRM.body("AceptacionDigital", aceptacionDigital); // 0 = FAVORABLE || 1 = NO FAVORABLE
        apiRequestCRM.body("IdSolicitud", idSolicitud);
        apiRequestCRM.body("stateCode", "0"); //El stateCode es un campo conjunto de opciones y se completa con numeros: 0 = Abierta | 1 = Ganada | 2 = Perdida
        return Api.response(apiRequestCRM, contexto.idCobis());
    }

    public static ApiResponse getTerminosYCondiciones(String nroSolicitud, boolean todosAceptaron, ContextoHB contexto){
        ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
        request.query("solicitudid",nroSolicitud );
        if (todosAceptaron){
            request.query("grupocodigo", "GRUPO3");
        }
        else{
            request.query("grupocodigo", "ACEPDIGV2");
        }
        //CARAT-VISUALIZACION Dami muller
        //ACEPDIG
        request.query("canal", contexto.canal);
        request.header("x-cuil", contexto.persona().cuit());

        ApiResponse response = Api.response(request,nroSolicitud, contexto.idCobis());
        return response;
    }
}
