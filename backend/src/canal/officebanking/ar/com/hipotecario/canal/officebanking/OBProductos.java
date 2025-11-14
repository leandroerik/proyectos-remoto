package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.PosicionConsolidada;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;

public class OBProductos extends ModuloOB {
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioParametroOB servicioParametro = new ServicioParametroOB(contexto);
    
    public static Object productos(ContextoOB contexto) {
    	String idCobis = contexto.sesion().idCobis;
    	PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, idCobis, true, false, false, "todos").get();
    	Objeto respuesta = respuesta("datos", productos);
    	return respuesta;
    	
    }
}
