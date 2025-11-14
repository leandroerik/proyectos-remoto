package ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestSeguridad {

    public static Objeto getUsuarioIDG(ContextoTAS contexto, String idCliente) {
            ApiRequest request = new ApiRequest("UsuarioISVA", "seguridad", "GET", "/v1/usuario?idcliente={idcliente}",
                    contexto);
            request.path("idcliente", idCliente);
            request.query("grupo", "ClientesBH");

            ApiResponse response = request.ejecutar();
            ApiException.throwIf("USUARIO_NO_ENCONTRADO",
                    response.http(204) || response.http(404) || "USER_NOT_EXIST".equals(response.string("codigo")),
                    request, response);
            ApiException.throwIf(!response.http(200, 202), request, response);
            return response;
    }

    public static Objeto getClave(ContextoTAS contexto, String idCliente, String clave) {
            ApiRequest requestClave = new ApiRequest("ValidarClaveIDG", "seguridad", "GET", "/v1/clave", contexto);
            requestClave.query("grupo", "ClientesBH");
            requestClave.query("idcliente", idCliente);
            requestClave.query("clave", clave);
            requestClave.query("nombreClave", "numerica");

            ApiResponse responseClave = requestClave.ejecutar();
            ApiException.throwIf("USUARIO_INVALIDO", responseClave.contains("The password authentication failed"),
                    requestClave, responseClave);
            ApiException.throwIf("USUARIO_BLOQUEADO", responseClave.contains("is now locked out"), requestClave,
                    responseClave);
            ApiException.throwIf("USUARIO_BLOQUEADO",
                    responseClave.contains("Maximum authentication attempts exceeded"), requestClave, responseClave);
            ApiException.throwIf("CLAVE_EXPIRADA", responseClave.contains("password has expired"), requestClave,
                    responseClave);
            ApiException.throwIf(!responseClave.http(200) && !responseClave.http(202), requestClave, responseClave);
            Objeto respuesta = new Objeto().add(responseClave.body);
            return respuesta;
    }

    public static Objeto getClaveCanal(ContextoTAS contexto, String idCliente, String clave) {
            ApiRequest request = new ApiRequest("ValidarClaveCanalIDG", "seguridad", "GET",
                    "/v1/clave/canal?grupo=Clientes&idcliente={idCliente}&clave={clave}&nombreClave=numerica",
                    contexto);
            request.path("idCliente", idCliente);
            request.path("clave", clave);
            //request.cache = true;
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_VALIDAR_CLAVE_CANAL");
            ApiException.throwIf(!response.http(200, 202), request, response);
            Objeto respuesta = new Objeto().add(response.body);
            return respuesta;
    }
    
    public static Objeto putClave(ContextoTAS contexto, String idCliente, Objeto parametros) {
        ApiRequest request = new ApiRequest("ModificarClaveIDG", "seguridad", "PUT",
                "/v1/clave",
                contexto);
        request.body("idUsuario", idCliente);
        request.body("grupo", "ClientesBH");
        request.body("nombreClave", "numerica");
        request.body("parametros", parametros);

        request.cache = true;
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_MODIFICAR_CLAVE");
        ApiException.throwIf(!response.http(200, 202), request, response);
        Objeto respuesta = new Objeto().add(response.body);
        return respuesta;
}
    
    public static Objeto postMigracionBM(ContextoTAS contexto, String idCliente, Integer doc) {
        ApiRequest request = new ApiRequest("MigracionBM", "seguridad", "POST",
                "/v1/migracionbm",
                contexto);
        request.body("dni", doc);
        request.body("idCobis", idCliente);
        request.body("opcion", 1);
        request.body("nuevoValor", 0);

        //request.cache = true;
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_MIGRACION_BM");
        ApiException.throwIf(!response.http(200, 202), request, response);
        return response;
}
}
