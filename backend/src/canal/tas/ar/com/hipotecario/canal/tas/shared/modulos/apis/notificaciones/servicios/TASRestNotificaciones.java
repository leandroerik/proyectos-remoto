package ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.modelos.TASNotificacion;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.modelos.TASNotificacionResponse;

import java.util.ArrayList;
import java.util.List;

public class TASRestNotificaciones {
    public static List<TASNotificacion> getNotificaciones(ContextoTAS contexto, String idCliente, boolean priorizada){
           ApiRequest request = new ApiRequest("Notificaciones", "notificaciones", "GET", "/v1/notificaciones?idcliente={idCliente}&priorizada={priorizada}", contexto);
           request.path("idCliente", idCliente);
           request.path("priorizada", String.valueOf(priorizada));
           request.cache = true;
           ApiResponse response = null;
           response = request.ejecutar();
           if (response.http(204)) return null;
           ApiException.throwIf(!response.http(200, 202), request, response);
           List<TASNotificacion> notificaciones = new ArrayList<>();
           for (Objeto obj : response.objetos()){
               TASNotificacion notificacion = obj.toClass(TASNotificacion.class);
               notificaciones.add(notificacion);
           }
           return notificaciones;
    }

    public static boolean postNotificaciones(ContextoTAS contexto, String idCliente, TASNotificacionResponse notificacionResponse){
        ApiRequest request = new ApiRequest("Notificaciones", "notificaciones", "POST", "/v2/notificaciones/{idCliente}/alertas", contexto);
        request.path("idCliente", idCliente);
        request.body(notificacionResponse.objeto());
        request.cache = true;
        ApiResponse response = null;
        response = request.ejecutar();
        /**
         * !!!se comenta el 204 para DESA y se agrega como response true, PROBAR EN OTROS AMBIENTES SU COMPORTAMIENTO
         */        
        //if(response.http(204 )) return false;
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return true;
    }
    public static Objeto postEnviarMail(ContextoTAS contexto, Objeto datos){
        ApiRequest request = new ApiRequest("EnvioCorreoElectronicoExterno", "notificaciones", "POST", "/v1/correoelectronico", contexto);
        Objeto adjuntos = new Objeto();
        Objeto parametros = new Objeto();
        adjuntos.set("contenidoBase64", datos.string("base64"));
        adjuntos.set("nombreArchivo", datos.string("nombreArchivo"));
        ArrayList<Objeto> objArray = new ArrayList<>();
        objArray.add(adjuntos);
        parametros.set("subject", datos.string("subject"));
        parametros.set("body", datos.string("body"));
        request.body("adjuntos", objArray);
        request.body("asunto", datos.string("asunto"));
        request.body("de", datos.string("de"));
        request.body("nombreDe", datos.string("nombreDe"));
        request.body("para", datos.string("para"));
        request.body("parametros", parametros.toMap());
        request.body("plantilla", datos.string("plantilla"));
        request.body("async", datos.string("async", null));
        request.body("copiaCarbon", datos.string("copiaCarbon", null));
        request.body("copiaOculta", datos.string("copiaOculta", null));
        request.body("html", datos.string("html", null));
        request.body("seguimiento", datos.string("seguimiento", null));
        request.body("texto", datos.string("texto", null));
        request.body("token", datos.string("token", null));
        request.cache = true;

        LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_RESUMEN");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_RESUMEN");
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }

}
