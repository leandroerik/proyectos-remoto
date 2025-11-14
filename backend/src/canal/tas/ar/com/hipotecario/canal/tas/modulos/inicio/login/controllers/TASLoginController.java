package ar.com.hipotecario.canal.tas.modulos.inicio.login.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.libreriasecurity.application.service.ChannelTassUseCaseService;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.*;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.SesionTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.TASIngresoUsuario;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlSesionCliente;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.servicios.TASRestPersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.servicios.TASRestSeguridad;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.controllers.TASKioscoController;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.modelos.TASUsuariosAdministradores;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.TasMinoRequestBuilder;
import ar.com.hipotecario.canal.tas.shared.utils.models.TasMinoRequestParams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class TASLoginController {
    public static Objeto ingresar(ContextoTAS contexto) {
        try {
            String idTas = contexto.parametros.string("tasId");
            String nroDoc = contexto.parametros.string("nroDoc");
            if (idTas.isEmpty() || nroDoc.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");

            // obtengo los datos del Cliente y valido en caso de que sea mas de uno
            List<TASClientePersona> clientes = TASRestPersona.getClientePersona(contexto, nroDoc);
            if (clientes == null)
                return RespuestaTAS.sinResultados(contexto,
                        "TASLoginController - ingresar(): No se encontro cliente con ese nro de documento");

            // todo probar aca con mas de un cliente..
            TASClientePersona cliente =
                    clientes.size() > 1 ? TASIngresoUsuario.validarCliente(contexto, clientes)
                            : clientes.get(0);
            if (cliente == null)
                return RespuestaTAS.parametrosIncorrectos(contexto,
                        "TASLoginController - ingresar(): No se pudo validar Cliente, verifique los datos");

            SesionTAS sesion = new SesionTAS(contexto.idSesion(), cliente, idTas);
            sesion.idCobis = cliente.getIdCliente();
            contexto.setSesion(sesion);

            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if (kiosco == null) {
                Objeto kioscoIngresado = TASKioscoController.getKiosco(contexto);
                kiosco = TASKiosco.fillDatos(kioscoIngresado);
            }
            // obtengo datos si es usuario Administrador
            // todo ver como funca aca..
            TASUsuariosAdministradores usuarioAdmin = TASUsuariosAdministradores.fillDatos(
                    TASUsuariosAdminController.buscarUsuarioAdminByDocSucursal(contexto, kiosco));

            // TODO: Probemos si este cambio que meti funciona y ayuda a limpiar la logica
            // de este metodo.
            Objeto existeSesion = TASIngresoUsuario.sesionesActivasEncontradas(
                    usuarioAdmin.isFlagAdministrador(), contexto, nroDoc);
            if (existeSesion != null) {
                return existeSesion;
            }
            // TODO: VER QUE NO DEVUELVA UN OBJETO VACIO...
            // metodo que realiza la validacion del usuario con la API Seguridad
            Objeto rta = TASIngresoUsuario.ingresoUsuario(contexto, cliente, kiosco); // Dentro de
                                                                                      // este metodo
                                                                                      // se llama a
                                                                                      // la libreria

            // si el login no fue true devuelvo la respuesta de la validacion de usuario
            if (!rta.bool("login"))
                return rta;

            Objeto sesionCliente = TASSqlSesionCliente.inicioSesion(contexto, nroDoc, idTas);
            if (!sesionCliente.bool("update_sesion"))
                return RespuestaTAS.error(contexto,
                        "la validacion de cliente fue correcta, pero no se pude realizar el cambio en BBDD");

            // si el update o insert fue true seteo hora de inicio de sesion en contexto
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            String date = df.format(sesionCliente.get("hora_inicio"));
            contexto.sesion().fechaLogin = (new Fecha(date, "yyyy/MM/dd HH:mm:ss.SSS"));
            contexto.saveSesion(sesion);

            LogTAS.evento(contexto, "LOGIN", rta);
            return rta;

        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASLoginController - ingresar()", e);
        }
    }

    public static Objeto salir(ContextoTAS contexto) {
        try {
            String nroDoc = contexto.parametros.string("nroDoc");
            String idTas = contexto.parametros.string("tasId");
            if (nroDoc.isEmpty() || idTas.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            boolean rta = TASSqlSesionCliente.cierreSesion(contexto, nroDoc, idTas);
            if (!rta)
                return RespuestaTAS.sinResultados(contexto, "No se pudo finalizar sesion");
            // si el update o insert fue true elimino la sesion del contexto y registro el evento
            contexto.deleteSesion();
            LogTAS.evento(contexto, "LOGOUT", new Objeto().set("cierre_sesion", true));
            return new Objeto().set("cierre_sesion", true);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "error en TASLoginController - salir()", e);
        }
    }

    public static Objeto putCambiarClave(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            String claveActual = contexto.parametros.string("claveActual");
            String claveNueva = contexto.parametros.string("claveNueva");
            Objeto parametros = new Objeto().set("clave", claveNueva);

            if (claveActual.isEmpty() || claveNueva.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            LogTAS.evento(contexto, "INICIO_CAMBIAR_CLAVE",
                    new Objeto().set("idCliente", idCliente));
            LogTAS.evento(contexto, "VALIDAR_CLAVE", new Objeto().set("idCliente", idCliente));

            TASRestSeguridad.getClaveCanal(contexto, idCliente, claveActual);
            LogTAS.evento(contexto, "CAMBIAR_CLAVE_REST_SEGURIDAD",
                    new Objeto().set("idCliente", idCliente));
            Objeto response = TASRestSeguridad.putClave(contexto, idCliente, parametros);
            LogTAS.evento(contexto, "FIN_CAMBIAR_CLAVE_REST_SEGURIDAD",
                    new Objeto().set("response", "true"));
            //flag prendido transmit
            String flag_transmit = contexto.config.string("tas_ts_flag");
            if(flag_transmit.equals("true")) {
                LogTAS.evento(contexto,"Modulo Transmit ON");
                // TODO: NELSON Aca es donde viene el pinchado para el cambio de clave con TS.
                LogTAS.evento(contexto, "CAMBIAR_CLAVE_MOSAIC", new Objeto().set("response", "true"));
                ChannelTassUseCaseService channelTassUseCaseService = new ChannelTassUseCaseService();
                TasMinoRequestParams params = new TasMinoRequestParams(
                        clienteSesion.getSexo().concat(clienteSesion.getNumeroDocumento()), claveNueva,
                        contexto.config.string("tas_journey_asignar_buho_0141"));
                TasMinoResquest tasMinoResquest = TasMinoRequestBuilder.build(params, contexto);
                Optional<GenericResponse<?>> clienteValidado =
                        channelTassUseCaseService.assignBuhoFacilPassword(tasMinoResquest);

                // respuesta
                if (clienteValidado.get().isCodeErrorNotEqualCero()) {
                    String msg = "TASLoginController - putCambiarClave(): "
                            + clienteValidado.get().getErrorMessage();
                    return RespuestaTAS.error(contexto, msg);
                }
                LogTAS.evento(contexto, "FIN_CAMBIAR_MOSAIC", new Objeto().set("response", "true"));

            }
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASModificarClave - putCambiarClave()",
                    (Exception) e);
        }
    }

}
