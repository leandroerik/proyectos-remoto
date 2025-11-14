package ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.libreriasecurity.application.service.ChannelTassUseCaseService;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.*;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlSesionCliente;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlUsuariosAdministradores;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.servicios.TASRestSeguridad;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.TasMinoRequestBuilder;
import ar.com.hipotecario.canal.tas.shared.utils.models.TasMinoRequestParams;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TASIngresoUsuario {

    public static Objeto ingresoUsuario(ContextoTAS contexto, TASClientePersona cliente,
            TASKiosco kiosco) {
        try {
            // obtengo parametros de contexto
            String nroDoc = contexto.sesion().getClienteTAS().getSexo()
                    .concat(contexto.parametros.string("nroDoc"));
            String clave = contexto.parametros.string("clave");
            Integer doc = Integer.parseInt(contexto.parametros.string("nroDoc"));

            // valido los que vienen por defecto
            if (nroDoc.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "nro documento nulo");
            if (clave.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "clave nulo");

            //flag prendido transmit
            String flag_transmit = contexto.config.string("tas_ts_flag");
            if(flag_transmit.equals("true")) {
            	if (estaMigrado(cliente, doc, contexto)) {
                LogTAS.evento(contexto, "Modulo Transmit ON");
                // TODO: NELSON Aca es donde vieneel pinchado para el login con TS.
                ChannelTassUseCaseService channelTassUseCaseService = new ChannelTassUseCaseService();
                TasMinoRequestParams params = new TasMinoRequestParams(nroDoc, clave,
                        contexto.config.string("tas_journey_auth_buho_0141"));
                TasMinoResquest tasMinoResquest = TasMinoRequestBuilder.build(params, contexto);

                Optional<GenericResponse<?>> clienteValidado =
                        channelTassUseCaseService.authBuhoFacilPassword(tasMinoResquest);

	                // respuesta
	                if (clienteValidado.get().isCodeErrorNotEqualCero()) {
	                    String msg = "TASIngresousuario - ingresoUsuario(): "
	                            + clienteValidado.get().getErrorMessage();
	                    return RespuestaTAS.errorLoginTS(contexto, msg);
	                }
            	} else {
            		 // valido usuario
                    Objeto clienteValidado = validarUsuario(contexto, cliente, clave);

                    if (!clienteValidado.isEmpty() && clienteValidado.toString().contains("ERROR"))
                        return RespuestaTAS.error(contexto, "TASIngresousuario - ingresoUsuario()", (Exception) clienteValidado.get("ERROR"));
            	}
              }else {
                // valido usuario
                Objeto clienteValidado = validarUsuario(contexto, cliente, clave);

                if (!clienteValidado.isEmpty() && clienteValidado.toString().contains("ERROR"))
                    return RespuestaTAS.error(contexto, "TASIngresousuario - ingresoUsuario()", (Exception) clienteValidado.get("ERROR"));
            }
            Objeto rta = new Objeto();
            rta.set("clienteID", cliente.getIdCliente());
            rta.set("terminalID", kiosco.getKioscoId());
            rta.set("login", true);
            return rta;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASIngresoUsuario - ingresoUsuario()", e);
        }
    }

    private static boolean estaMigrado(TASClientePersona cliente, Integer nroDoc, ContextoTAS contexto) {
    	  try {
              String idCliente = cliente.getIdCliente();
              Objeto estadoMigracionResponse = TASRestSeguridad.postMigracionBM(contexto, idCliente, nroDoc);
              String migrado = estadoMigracionResponse.string("migrado");
              return migrado.equals("1");
          } catch (Exception e) {
        	  RespuestaTAS.error(contexto, "TASIngresoUsuario - ingresoUsuario()", e);
              return false;
          }
	}

	public static Objeto validarUsuario(ContextoTAS contexto, TASClientePersona cliente,
            String clave) {
        try {
            LogTAS.evento(contexto, "INICIO_VALIDAR_USUARIO",
                    new Objeto().set("idCliente", cliente.getIdCliente()));
            String idCliente = cliente.getIdCliente();
            return TASRestSeguridad.getClaveCanal(contexto, idCliente, clave);
        } catch (Exception e) {
            return new Objeto().set("ERROR", e);
        }
        // si hay mas de un cliente con el mismo dni

    }

    // tiene q recibir si hay uno o mas clientes.. validar el tipo de documento y el
    // genero.. devolver un solo cliente
    public static TASClientePersona validarCliente(ContextoTAS contexto,
            List<TASClientePersona> clientes) {
        String tipoDoc = contexto.parametros.string("tipoDoc", "");
        String idSexo = contexto.parametros.string("idSexo", "");
        String nroDoc = contexto.parametros.string("nroDoc");
        if (!tipoDoc.isEmpty() && !idSexo.isEmpty()) {
            return clientes.stream()
                    .filter(cliente -> cliente.getNumeroDocumento().equals(nroDoc)
                            && cliente.getTipoDocumento().equals(tipoDoc)
                            && cliente.getSexo().equals(idSexo))
                    .findFirst().orElse(null);
        }
        if (clientes.size() == 2)
            return verificarLista(nroDoc, clientes, tipoDoc, idSexo);
        return !tipoDoc.isEmpty() ? validarClientePorDoc(clientes, nroDoc, tipoDoc)
                : validarClientePorGenero(clientes, nroDoc, idSexo);
    }

    private static TASClientePersona verificarLista(String nroDoc,
            List<TASClientePersona> listaClientes, String tipoDoc, String idSexo) {
        TASClientePersona clienteA = listaClientes.get(0);
        TASClientePersona clienteB = listaClientes.get(1);
        /*
         * if (!clienteA.getTipoDocumento().equals(clienteB.getTipoDocumento()) &&
         * !clienteA.getSexo().equals(clienteB.getSexo())) return null; if
         * (clienteA.getTipoDocumento().equals(clienteB.getTipoDocumento())) return null; if
         * (clienteA.getSexo().equals(clienteB.getSexo())) return null; if
         * (clienteA.getNumeroDocumento().equals(clienteB.getNumeroDocumento())) return clienteA;
         */
        if (!tipoDoc.isEmpty()) {
            return clienteA.getTipoDocumento().equals(tipoDoc) ? clienteA
                    : clienteB.getTipoDocumento().equals(tipoDoc) ? clienteB : null;
        } else if (!idSexo.isEmpty()) {
            return clienteA.getSexo().equals(idSexo) ? clienteA
                    : clienteB.getSexo().equals(idSexo) ? clienteB : null;
        }
        return listaClientes.stream().filter(cliente -> cliente.getNumeroDocumento().equals(nroDoc))
                .findFirst().orElse(null);
    }

    private static TASClientePersona validarClientePorDoc(List<TASClientePersona> clientes,
            String nroDoc, String tipoDoc) {
        TASClientePersona clienteValidado = clientes.stream()
                .collect(Collectors.groupingBy(TASClientePersona::getTipoDocumento)).values()
                .stream().anyMatch(personas -> personas.size() > 1)
                        ? null
                        : clientes.stream()
                                .filter(cliente -> cliente.getNumeroDocumento().equals(nroDoc)
                                        && cliente.getTipoDocumento().equals(tipoDoc))
                                .findFirst().orElse(null);
        return clienteValidado;
    }

    private static TASClientePersona validarClientePorGenero(List<TASClientePersona> clientes,
            String nroDoc, String idSexo) {
        TASClientePersona clienteValidado = clientes.stream()
                .collect(Collectors.groupingBy(TASClientePersona::getSexo)).values().stream()
                .anyMatch(personas -> personas.size() > 1)
                        ? null
                        : clientes.stream()
                                .filter(cliente -> cliente.getNumeroDocumento().equals(nroDoc)
                                        && cliente.getSexo().equals(idSexo))
                                .findFirst().orElse(null);
        return clienteValidado;
    }

    // Este metodo es nuevo, tendriamos que probarlo y afinarlo.
    public static Objeto sesionesActivasEncontradas(boolean isAdmin, ContextoTAS contexto,
            String nroDoc) {
        try {
            Objeto ultimaSesion = TASSqlSesionCliente.getUltimaSesion(contexto, nroDoc);
            if (ultimaSesion.string("estado").equals("ERROR"))
                throw new Exception((Exception) ultimaSesion.get("error"));
            if (ultimaSesion.string("estado").equals("PRIMER_SESION"))
                return null;

            String idTasUltSesion = ultimaSesion.objeto("estado").objetos(0).string("idKiosco");

            boolean existeSesion =
                    TASSqlSesionCliente.existeSesion(contexto, nroDoc, idTasUltSesion);

            if (!existeSesion)
                return null;

            if (isAdmin) {
                boolean existeSesionAdmin =
                        TASSqlUsuariosAdministradores.existeSesionAdministador(contexto, nroDoc);
                if (existeSesionAdmin)
                    return RespuestaTAS.sesionActiva(contexto,
                            "Existe una sesion de Administrador activa");
                return null;
            }
            return RespuestaTAS.sesionActiva(contexto, "Existe una sesion de Cliente activa");
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,
                    "TASLoginController - Metodo sesionesActivasEncontradas(): " + e.getMessage());
        }
    }
}
