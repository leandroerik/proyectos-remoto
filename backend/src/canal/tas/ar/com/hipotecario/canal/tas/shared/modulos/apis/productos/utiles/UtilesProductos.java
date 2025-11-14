package ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.utiles;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.servicios.TASRestProductos;

public class UtilesProductos {

  public static Objeto getTitularesProducto(ContextoTAS contexto, String IdProducto, String tipoProducto){
    try {
      Objeto response = new Objeto();
      Objeto titulares = TASRestProductos.getTitularesProducto(contexto, IdProducto, tipoProducto);
      return response.set("estado", titulares);
    } catch (Exception e) {
      Objeto error = new Objeto();
      error.set("estado", "ERROR");
      error.set("error", e);
      return error;
    }
  }
  
}
