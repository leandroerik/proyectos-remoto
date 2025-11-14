package ar.com.hipotecario.canal.tas.modulos.cajasseguridad.utils;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cajasseguridad.modelos.TASCajaSeguridad;
import ar.com.hipotecario.canal.tas.modulos.cajasseguridad.servicios.TASRestCajasSeguridad;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.utils.UtilesTarjetas;

import java.util.ArrayList;
import java.util.List;

public class UtilesCajasSeguridad {

    public static Objeto getCajasSeguridad(ContextoTAS contexto, Objeto idCajas){
        List<TASCajaSeguridad> cajasList = new ArrayList<>();
        for(Object caja : idCajas.toList()){
            TASCajaSeguridad cajaDetalle = new TASCajaSeguridad();
            Objeto responseDetalleCaja = TASRestCajasSeguridad.getCajasSeguridad(contexto, caja.toString());
            if(responseDetalleCaja.string("estado").equals("ERROR")) {
                cajaDetalle.setError(responseDetalleCaja);
            }
            if(responseDetalleCaja.string("estado").equals("SIN_RESULTADOS")) cajaDetalle.setError(new Objeto().set("detalleCaja", "SIN_RESULTADO"));
            if(responseDetalleCaja.string("estado").equals("OK")) cajaDetalle = responseDetalleCaja.objeto("detalleCS").objetos().get(0).toClass(TASCajaSeguridad.class);
            cajaDetalle.setIdCaja(caja.toString());
            cajasList.add(cajaDetalle);
        }
        return armarResponseCS(contexto, cajasList);
    }
    public static Objeto armarResponseCS(ContextoTAS contexto, List<TASCajaSeguridad> cajasList){
        Objeto response = new Objeto();
        boolean hayResultados = false;
        boolean todosErrores = false;
        int conteoErrores = 0;
        for(TASCajaSeguridad caja : cajasList){
            if(caja.getError() != null){
                response.add(armarResponseEstadoErrorCS(caja));
                conteoErrores++;
            }else{
                hayResultados = true;
                response.add(caja.objeto());
            }
        }
        if(cajasList.size() == conteoErrores) todosErrores = true;
        return hayResultados && !todosErrores ? response : RespuestaTAS.sinResultados(contexto, "La consulta arrojó error o sin resultados.");

    }

    private static Objeto armarResponseEstadoErrorCS(TASCajaSeguridad caja){
        Objeto response = new Objeto();
        Objeto error = caja.getError();
        Exception e = (Exception) error.get("error");
        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;
            response.set("idCaja", caja.getIdCaja());
            response.set("estado", "ERROR");
            response.set("codigo", apiException.response.codigoHttp);
            response.set("mensajeAlUsuario", apiException.response.string("mensajeAlUsuario"));
            return response;
        }
        response.set("estado", "ERROR");
        response.set("mensaje", e.getMessage());
        return response;
    }

}
