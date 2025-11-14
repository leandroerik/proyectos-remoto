package ar.com.hipotecario.canal.rewards.modulos.rechazos.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.canal.rewards.ContextoRewards;

import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.core.ValidacionPermisos;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWRechazo;
import ar.com.hipotecario.canal.rewards.modulos.posicion_consolidada.services.PosicionConsolidadaService;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.SolicitudRechazos;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.services.RWRechazosService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RWRechazosController {

    public static Objeto getRechazosEd(ContextoRewards contexto) {
        return RWRechazosService.getRechazosEd(contexto);
    }

    public static Objeto getRechazosAr(ContextoRewards contexto) {
        return RWRechazosService.getRechazosAr(contexto);
    }

    public static Objeto getDocumentosRewards(ContextoRewards contexto) {
        return RWRechazosService.getDocumentosRewards(contexto);
    }

    public static Objeto getActividades(ContextoRewards contexto) {
        return RWRechazosService.getActividades(contexto);
    }

    public static Objeto getEstadosCiviles(ContextoRewards contexto) {
        return RWRechazosService.getEstadosCiviles(contexto);
    }

    public static Objeto getSexos(ContextoRewards contexto) {
        return RWRechazosService.getSexos(contexto);
    }

    public static Objeto getRechazosClientes(ContextoRewards contexto) {

        String id = contexto.parametros.string("idCobis", "0"); // requerido si viene vacio que sea 0
        String programa = contexto.parametros.string("programa", ""); // requerido
        String entidad = contexto.parametros.string("entidad", ""); // requerido
        String novedad = contexto.parametros.string("novedad", ""); // requerido
        String estado = contexto.parametros.string("estado", ""); // Si esta vacio debo mandar Ingresado(Rechazao)?

        String fechaDesde = contexto.parametros.string("fechaDesde", ""); // solo requerido si ultimoAnio = false
        String fechaHasta = contexto.parametros.string("fechaHasta", ""); // solo requerido si ultimoAnio = false
        String tipo = contexto.sesion().rol.contains("AUTORIZADOR") ? "S" : "U";
        String idRechazo = contexto.parametros.string("motivoRechazo", "");
        String transaccion = contexto.parametros.string("transaccion", "");

        if (programa.isEmpty() || entidad.isEmpty() || fechaDesde.isEmpty() || fechaHasta.isEmpty()){
            return RespuestaRW.sinParametros(contexto, "Bad request. faltan parametros obligatorios: program, enridad, fechaDesed o  fechaHasta");
        }

        SolicitudRechazos reqRechazos = new SolicitudRechazos(
                programa,entidad,novedad,estado,fechaDesde,fechaHasta,tipo, idRechazo,transaccion
        );

        Objeto response = RWRechazosService
                .getRechazosClientes(contexto, id, reqRechazos);

        return response;
    }

    static public Objeto postRechazos(ContextoRewards contexto)
    {
        try{
            String rolUsuario = contexto.sesion().rol.contains("AUTORIZADOR") ? "S" : "U";
            String nivel = rolUsuario;

            List<Objeto> rechazosModificacionesObj = contexto.parametros.objetos("rechazos");
            List<RWRechazo> rechazosModificaciones = new ArrayList<>();
            String accion = contexto.parametros.string("accion");

            if (rechazosModificacionesObj == null || rechazosModificacionesObj.size() == 0) {
                throw new IllegalArgumentException("No se encontraron objetos 'Rechazos' en los parámetros.");
            }

            //valido que la accion corresponda al rol del usuario
            switch (accion)
            {
                case "C":
                        if (!ValidacionPermisos.validarPermisos(contexto, ValidacionPermisos.Permisos.CORREGIR_RECHAZO)) {
                            Objeto respuesta = new Objeto();
                            respuesta.set("status", 401);
                            respuesta.set("message", "Sin permisos para realizar la operacion");
                            respuesta.set("error", RespuestaRW.error(contexto,
                                    "RWRechazosController.postRechazos - Error",
                                    new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_CORREGIR_REHAZO"));
                            return respuesta;
                        }
                    break;
                case "A":
                        if (!ValidacionPermisos.validarPermisos(contexto, ValidacionPermisos.Permisos.APROBAR_RECHAZO)) {
                            Objeto respuesta = new Objeto();
                            respuesta.set("status", 401);
                            respuesta.set("message", "Sin permisos para realizar la operacion");
                            respuesta.set("error", RespuestaRW.error(contexto,
                                    "RWRechazosController.postRechazos - Error",
                                    new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_APROBAR_REHAZO"));
                            return respuesta;
                        }
                    break;
                case "R":
                    if (!ValidacionPermisos.validarPermisos(contexto, ValidacionPermisos.Permisos.DENEGAR_RECHAZO)) {
                        Objeto respuesta = new Objeto();
                        respuesta.set("status", 401);
                        respuesta.set("message", "Sin permisos para realizar la operacion");
                        respuesta.set("error", RespuestaRW.error(contexto,
                                "RWRechazosController.postRechazos - Error",
                                new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_DENEGAR_REHAZO"));
                        return respuesta;
                    }
                    break;
            }


            for (Objeto obj : rechazosModificacionesObj) {
                RWRechazo rechazo = new RWRechazo(obj);
                rechazo.setNivel(nivel);

                //logica del nivel segun rewards viejo.
                /*if(rolUsuario == "U" && Objects.equals(rechazo.getPrograma(), "AR") && !Objects.equals(rechazo.getEstado(), "U")
                        && !Objects.equals(rechazo.getEstado(), "G") && !Objects.equals(rechazo.getEstado(), "C")) {
                    //rechazo.setNivel("G");
                }

                if(rolUsuario == "U" && rechazo.getPrograma() == "ED" && rechazo.getEstado() != "A" &&  rechazo.getEstado() != "C"){
                    rechazo.setNivel("G");
                }*/

                if(rolUsuario.equals("S") && accion.equals("R")){
                    rechazo.setNivel("D");
                }

                rechazosModificaciones.add(rechazo);
            }


            return RWRechazosService.postRechazos(contexto, rechazosModificaciones);
        }
        catch(Exception e){
            Objeto response = new Objeto();
            response.set("error",e.getMessage());
            return response;
        }
    }


}
