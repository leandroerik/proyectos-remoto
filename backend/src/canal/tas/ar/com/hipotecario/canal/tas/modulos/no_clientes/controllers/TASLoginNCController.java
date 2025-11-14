package ar.com.hipotecario.canal.tas.modulos.no_clientes.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.SesionTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.TASIngresoUsuario;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlSesionCliente;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASCliente;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.servicios.TASRestPersona;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TASLoginNCController {

    public static Objeto loginNoCliente(ContextoTAS contexto){
        try {
            String idTas = contexto.parametros.string("tasId");
            String idCliente = contexto.parametros.string("idCliente");

            if (idTas.isEmpty() || idCliente.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");

            // obtengo los datos del Cliente y valido en caso de que sea mas de uno
            TASCliente clienteApi = TASRestPersona.getDatosCliente(contexto, idCliente);
            if (clienteApi == null)
                return RespuestaTAS.sinResultados(contexto,
                        "TASLoginController - ingresar(): No se encontro cliente con ese id");
            TASClientePersona cliente = refactorClienteApi(clienteApi);

            SesionTAS sesion = new SesionTAS(contexto.idSesion(), cliente, idTas);
            sesion.idCobis = cliente.getIdCliente();
            contexto.setSesion(sesion);

            // si el update o insert fue true seteo hora de inicio de sesion en contexto
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            String date = df.format(new Date());
            contexto.sesion().fechaLogin = (new Fecha(date, "yyyy/MM/dd HH:mm:ss.SSS"));
            contexto.saveSesion(sesion);
            Objeto sesionNoCliente = new Objeto();
            sesionNoCliente.set("idCliente", cliente.idCliente);
            return sesionNoCliente;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASLoginNCController - loginNoCliente()", e);
        }
    }

    private static TASClientePersona refactorClienteApi(TASCliente clienteApi){
        TASClientePersona clientePersona = new TASClientePersona();
        clientePersona.setIdCliente(clienteApi.getIdCliente().toString());
        clientePersona.setIdTipoDocumento(clienteApi.getIdTipoDocumento());
        clientePersona.setNumeroDocumento(clienteApi.getNumeroDocumento());
        clientePersona.setNumeroIdentificacionTributaria(clienteApi.getCuit() != null ? clienteApi.getCuit().toString() : "");
        clientePersona.setApellido(clienteApi.getApellidos());
        clientePersona.setNombre(clienteApi.getNombres());
        clientePersona.setSexo(clienteApi.getIdSexo());
        clientePersona.setFechaNacimiento(clienteApi.getFechaNacimiento());
        clientePersona.setTipoPersona(clienteApi.getIdTipoCliente());
        return clientePersona;
    }

    public static Objeto logoutNoCliente(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            String idTas = contexto.parametros.string("tasId");
            if (idCliente.isEmpty() || idTas.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            SesionTAS sesion = contexto.sesion();
            sesion.delete();
            LogTAS.evento(contexto, "LOGOUT", new Objeto().set("cierre_sesion", true));
            return new Objeto().set("cierre_sesion", true);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "error en TASLoginController - salir()", e);
        }
    }
}
