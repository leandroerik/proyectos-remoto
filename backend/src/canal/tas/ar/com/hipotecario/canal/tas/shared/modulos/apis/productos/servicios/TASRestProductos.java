package ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TASRestProductos {

  public static Objeto getTitularesProducto(ContextoTAS contexto, String idProducto, String tipoProducto){
        ApiRequest request = new ApiRequest("ConsultaProductoClientes", "productos", "GET", "/v1/productos/{idProducto}/clientes", contexto);
        request.path("idProducto", idProducto);
        request.query("tipo", tipoProducto);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202), request, response);
        return response;
  }
  
}
