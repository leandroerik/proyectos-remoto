package ar.com.hipotecario.canal.tas.shared.modulos.apis.formularios.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestFormularios {

    public static Objeto getFormularioPDF(ContextoTAS contexto, Objeto params) {
      try{
            ApiRequest request = new ApiRequest("FormularioImpresion", "formularios","GET","/api/FormularioImpresion", contexto);
            request.query("solicitudid", params.string("solicitudId"));
            request.query("grupocodigo", params.string("grupoCodigo"));
            request.query("canal", params.string("canal"));
            request.cache = true;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200), request, response);
            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return respuesta;
      }catch(Exception e){
          Objeto error = new Objeto();
          LogTAS.error(contexto, e);
          error.set("estado", "ERROR");
          error.set("error", e);
          return error;
      }        
    }
  
}
