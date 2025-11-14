package ar.com.hipotecario.canal.tas.modulos.cajasseguridad.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cajasseguridad.utils.UtilesCajasSeguridad;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

public class TASCajasSeguridadController {

    public static Objeto getCajasSeguridad(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto cajasList = contexto.parametros.objeto("cajasSeguridad", new Objeto());
            if(cajasList.toList().size() < 1) return RespuestaTAS.sinParametros(contexto, "sin cajas para consultar");
            Objeto responseCajas = UtilesCajasSeguridad.getCajasSeguridad(contexto, cajasList);
            return responseCajas;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "", e);
        }
    }
}
