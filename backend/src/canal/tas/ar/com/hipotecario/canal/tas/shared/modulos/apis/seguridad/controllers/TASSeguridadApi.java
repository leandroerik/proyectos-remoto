package ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.servicios.TASRestSeguridad;

public class TASSeguridadApi {

    // TODO PROBAR EN OTRO AMBIENTE.. Y MEJORAR EL CONTROLADOR PARA Q REALIZE UN
    // SOLO LLAMADO
    public static Objeto validacionISVA(ContextoTAS contexto) {
        return null;
    }

    public static Objeto getUsuarioIDG(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            if (idCliente.isEmpty())
                RespuestaTAS.sinParametros(contexto, "Uno o mas parametros incompletos");
            Objeto rta = TASRestSeguridad.getUsuarioIDG(contexto, idCliente);
            return rta;
        } catch (Exception e){
            return RespuestaTAS.error(contexto, "[TASSeguridadApi] - getUsuarioIDG()", e);
        }
    }

    public static Objeto getClaveCanal(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            String clave = contexto.parametros.string("clave");
            if (idCliente.isEmpty() || clave.isEmpty())
                RespuestaTAS.sinParametros(contexto, "Uno o mas parametros incompletos");
            Objeto rta = TASRestSeguridad.getClaveCanal(contexto, idCliente, clave);
            return rta;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASSeguridadApi - getCLaveCanal()", e);
        }
    }

    public static Objeto getClave(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            String clave = contexto.parametros.string("clave");
            if (idCliente.isEmpty() || clave.isEmpty())
                RespuestaTAS.sinParametros(contexto, "Uno o mas parametros incompletos");
            Objeto rta = TASRestSeguridad.getClave(contexto, idCliente, clave);
            return rta;
        } catch (Exception e){
            return RespuestaTAS.error(contexto, "TASSeguridadApi - getClave()", e);
        }
    }
}
