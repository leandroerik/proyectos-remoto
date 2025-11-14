package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.SesionTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.servicios.TASRestPersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.*;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASRestCuenta;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlUsuariosAdministradores;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASTipoDocumentoDescCortaEnum;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASTipoDocumentosEnum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TASApiPersona {

    public static Objeto getPersona(ContextoTAS contexto) {

        String idCliente = contexto.parametros.string("idCliente");
        if (idCliente == null || idCliente.isEmpty())
            return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");

        Objeto rta = TASRestPersona.getPersona(contexto, idCliente);
        if(!rta.string("ERROR").isEmpty()) return RespuestaTAS.error(contexto, "TASApiPersona - getPersona()", (Exception) rta.get("ERROR"));

        return rta;
    }

    public static Objeto getClientePersona(ContextoTAS contexto) {
        try {
            String nroDoc = contexto.parametros.string("nroDoc");
            String idTas = contexto.parametros.string("tasId");
            if (nroDoc.isEmpty() || idTas.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "documento o id TAS nulo");

            SesionTAS sesion = new SesionTAS();
            sesion.idTas = idTas;
            TASClientePersona cliente = new TASClientePersona();
            sesion.setClienteTAS(cliente);
            sesion.clienteTAS.numeroDocumento = nroDoc;
            contexto.saveSesion(sesion);
            
            List<TASClientePersona> responseList = TASRestPersona.getClientePersona(contexto, nroDoc);
// 135.. 136.. 141.. 153..08..11..154
            String tipoDocumentoDesc = TASTipoDocumentosEnum.getTipoDocumento(responseList.get(0).tipoDocumento);
            if(!tipoDocumentoDesc.equals("DNI")){
                tipoDocumentoDesc = TASTipoDocumentoDescCortaEnum.getDescripcionCorta(responseList.get(0).tipoDocumento);
            }
            contexto.parametros.set("idCliente", responseList.get(0).getIdCliente());
            TASCliente clientes = TASRestPersona.getDatosCliente(contexto, responseList.get(0).getIdCliente());

            Objeto adm = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto, nroDoc, tipoDocumentoDesc);
            if (responseList.size() <= 1)
                return armarRespInicio(responseList.get(0), clientes, adm);

            if (responseList.size() == 2)
                return verificarLista(nroDoc, responseList);

            return responseList.stream()
                    .collect(Collectors.groupingBy(TASClientePersona::getTipoDocumento))
                    .values()
                    .stream()
                    .anyMatch(personas -> personas.size() > 1)
                            ? RespuestaTAS.docDuplicado("TIP", "Tipo documento duplicado")
                            : RespuestaTAS.docDuplicado("DUP", "ID Sexo duplicado");
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,
                    "[TASRestPersona] - getClientePersona()",
                    e);
        }
    }

    /**
     * Recorre una lista de clientes y valida que exista un cliente determinado
     * 
     * @param nroDoc
     * @param listaClientes
     * @return Respuesta de tipo documento duplicado, un cliente de la lista
     */
    private static Objeto verificarLista(String nroDoc, List<TASClientePersona> listaClientes) {
        TASClientePersona clienteA = listaClientes.get(0);
        TASClientePersona clienteB = listaClientes.get(1);
        if (!clienteA.getTipoDocumento().equals(clienteB.getTipoDocumento())
                && !clienteA.getSexo().equals(clienteB.getSexo()))
            return RespuestaTAS.docDuplicado("DUP", "requiere TipoDoc");
        if (clienteA.getTipoDocumento().equals(clienteB.getTipoDocumento()))
            return RespuestaTAS.docDuplicado("TIP", "requiere TipoDoc");
        if (clienteA.getSexo().equals(clienteB.getSexo()))
            return RespuestaTAS.docDuplicado("DUP", "requiere IdSexo");
        if (clienteA.getNumeroDocumento().equals(clienteB.getNumeroDocumento()))
            return clienteA.objeto();
        return listaClientes.stream().filter(cliente -> cliente.getNumeroDocumento().equals(nroDoc)).findFirst()
                .orElse(null).objeto();
    }

    // tiene q recibir si hay uno o mas clientes.. validar el tipo de documento y el
    // genero.. devolver un solo cliente
    public static Objeto validarPersona(ContextoTAS contexto) {
        try {
            String tipoDoc = contexto.parametros.string("tipoDoc", "");
            String idSexo = contexto.parametros.string("idSexo", "");
            String nroDoc = contexto.parametros.string("nroDoc");
            if (nroDoc.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "NroDocumento no ingresado");
            if (tipoDoc.isEmpty() && idSexo.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "TipoDocumento e IdGenero no ingresados");
            List<TASClientePersona> clientes = TASRestPersona.getClientePersona(contexto, nroDoc);
            if (clientes == null)
                return RespuestaTAS.sinResultados(contexto, "Persona no encontrada, para ese Nro Documento");
            // valido si vinieron mas de un cliente con los datos ingresados y obtengo el
            // cliente solicitado
            TASClientePersona clienteNoValidado = new TASClientePersona();
            clienteNoValidado.setNombre("TIP");
            TASClientePersona clienteValidado = clientes
                    .size() <= 1
                            ? clientes.get(0)
                            : !tipoDoc.isEmpty()
                                    ? clientes.stream()
                                            .collect(Collectors.groupingBy(TASClientePersona::getTipoDocumento))
                                            .values()
                                            .stream()
                                            .anyMatch(personas -> personas.size() > 1)
                                                    ? null
                                                    : clientes.stream()
                                                            .filter(cliente -> cliente.getTipoDocumento()
                                                                    .equals(tipoDoc))
                                                            .findFirst().orElse(null)
                                    : clientes.stream().collect(Collectors.groupingBy(TASClientePersona::getSexo))
                                            .values()
                                            .stream()
                                            .anyMatch(personas -> personas.size() > 1)
                                                    ? clienteNoValidado
                                                    : clientes.stream()
                                                            .filter(cliente -> cliente.getSexo().equals(idSexo))
                                                            .findFirst().orElse(null);
            if (clienteValidado == null)
                return RespuestaTAS.docDuplicado("DUP", "id sexo requerido");
            if (clienteValidado.getNombre().equals("TIP"))
                return RespuestaTAS.docDuplicado("TIP", "tipo documento necesario");

            // obtengo datos para armar la respuesta final
            String tipoDocumentoDesc = TASTipoDocumentosEnum.getTipoDocumento(clienteValidado.tipoDocumento);
            if(!tipoDocumentoDesc.equals("DNI")){
                tipoDocumentoDesc = TASTipoDocumentoDescCortaEnum.getDescripcionCorta(clienteValidado.tipoDocumento);
            }
            contexto.parametros.set("idCliente", clienteValidado.getIdCliente());
            TASCliente datosCliente = TASRestPersona.getDatosCliente(contexto, clienteValidado.getIdCliente());
            Objeto adm = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto, nroDoc, tipoDocumentoDesc);

            return armarRespInicio(clienteValidado, datosCliente, adm);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASApiPersonas- validarPersona()", e);
        }
    }

    public static Objeto armarRespInicio(TASClientePersona clientePersona, TASCliente cliente, Objeto usuarioAdmin) {
        String tipoDocumentoDesc = TASTipoDocumentosEnum.getTipoDocumento(clientePersona.tipoDocumento);
        if(!tipoDocumentoDesc.equals("DNI")){
            tipoDocumentoDesc = TASTipoDocumentoDescCortaEnum.getDescripcionCorta(clientePersona.tipoDocumento);
        }
        clientePersona.setIdTipoDocumento(clientePersona.getTipoDocumento());
        clientePersona.setTipoDocumento(tipoDocumentoDesc);

        Objeto response = new Objeto();

        response.set("esMenor", cliente.getCajaAhorroMenores());
        response.set("cuil", cliente.getCuit());
        response.set("idtipoIdtributario", cliente.getIdTipoIDTributario());
        response.set("nombreCompleto", clientePersona.getNombre() + " " + clientePersona.getApellido());
        response.set("flagCumpleanios", esCumpleanios(clientePersona.getFechaNacimiento()));
        response.set("flagAdministrador", esAdmin(usuarioAdmin));
        response.set("cliente", clientePersona.objeto());
        return response;
    }

    private static boolean esAdmin(Objeto usuarioAdmin) {
        return !usuarioAdmin.isEmpty() &&
                !usuarioAdmin.toString().contains("SIN_RESULTADOS") &&
                usuarioAdmin.objetos().get(0).get("NumeroDocumento") != null;
    }

    public static String esCumpleanios(String fechaNacimiento) {
        Date fechaHoy = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Boolean compare;
        try {
            String fechaActual = df.format(fechaHoy);
            compare = fechaActual.equals(fechaNacimiento);
        } catch (Exception e) {
            return "ERROR";
        }
        return compare.toString();
    }

    public static Objeto getMailPrioritario(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            List<TASMail> mailResponse = TASRestPersona.getMailsPersona(contexto, clienteSesion.getNumeroIdentificacionTributaria());
            if(mailResponse == null) return RespuestaTAS.sinResultados(contexto,"Sin mails registrados");
            String mail = mailResponse.size() > 1 ? obtenerMailPrioritario(mailResponse) : mailResponse.get(0).getDireccion();
            return new Objeto().set("mail", mail);
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASApiPersona - getMailPrioritario()", e);
        }
    }
    private static String obtenerMailPrioritario(List<TASMail> listaMails) {
        String mailPrioritario = "error";  // Valor por defecto si no se encuentra ningún correo
        Integer prioridadActual = null;  // Para almacenar la menor prioridad encontrada
        for (TASMail correo : listaMails) {
            int prioridadCorreo = correo.getPrioridad() != null ? correo.getPrioridad() : 0;
            // Si prioridadActual es null o prioridadCorreo es menor que la actual, actualizamos
            if (prioridadActual == null || prioridadCorreo < prioridadActual) {
                prioridadActual = prioridadCorreo;
                mailPrioritario = correo.getDireccion();
            }
        }
        return mailPrioritario;  // Devuelve el correo con la mayor prioridad (menor valor de prioridad)
    }
    
    public static Objeto getPersonaDocumento(ContextoTAS contexto) {
        try {
            String doc = contexto.parametros.string("doc");
            if (doc.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Documento sin especificar");
            List<TASDocumento> docPersona = TASRestPersona.getDocumentosPersona(contexto, doc);
           boolean cuilExiste  = docPersona!=null && docPersona.size()>0;
           return new Objeto().set("cuilExiste", cuilExiste);
        } catch (Exception e) {
            return new Objeto().set("cuilExiste", false);
        }
    }

    public static Objeto getConsultarCenso(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            //? se hardcodea la respuesta porq la api devuelve 500
            /*
            TASCliente cliente = new TASCliente();
            cliente.setCuit(Long.valueOf(clienteSesion.getNumeroIdentificacionTributaria()));
            cliente.setIdCliente(Integer.valueOf(idCliente));
            cliente.setIdSexo(clienteSesion.getSexo());
            cliente.setIdTipoDocumento(clienteSesion.getTipoDocumento());
            boolean censo = TASRestPersona.getCensadoPersona(contexto, cliente, idCliente);
            */
            boolean censo = true;
            return new Objeto().set("censado", censo);
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASApiPersona - getConsultarCenso()", e);
        }
    }    

    public static Objeto getConsultarDatosPersonales(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            
            TASCliente datosCliente = TASRestPersona.getDatosCliente(contexto, idCliente);
            if(datosCliente == null) return RespuestaTAS.sinResultados(contexto, "Cliente no encontrado");

            List<TASMail> mailResponse = TASRestPersona.getMailsPersona(contexto, clienteSesion.getNumeroIdentificacionTributaria());
            //? ver q hacemos si viene null
            if(mailResponse == null) return RespuestaTAS.sinResultados(contexto,"Sin mails registrados");
            String mail = mailResponse.size() > 1 ? obtenerMailPrioritario(mailResponse) : mailResponse.get(0).getDireccion();
                        
            List<TASDomicilio> domicilios = TASRestPersona.getDomiciliosPersona(contexto, clienteSesion.getNumeroIdentificacionTributaria());
            //? ver q hacemos si viene null
            if(domicilios == null) return RespuestaTAS.sinResultados(contexto,"Sin domicilios registrados");
            TASDomicilio domicilioPostalFiltrado = filtrarDomiciliosPostales(domicilios);
            
            Objeto datosPersonales = armarObjetoDatosPersonales(mail, datosCliente);
            Objeto datosDomicilio = armarObjetoDomicilio(domicilioPostalFiltrado);            
            Objeto regimenInformativoCliente = armarObjetoRegimenInformativo(contexto, datosCliente);

            Objeto response = new Objeto();
            response.set("datosPersonales", datosPersonales);
            response.set("domicilioPostal", datosDomicilio);
            response.set("regimenInformativoCliente", regimenInformativoCliente);
            
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASApiPersona - getConsultarDatosPersonales()", e);
        }
    }

    private static TASDomicilio filtrarDomiciliosPostales(List<TASDomicilio> domicilios) {
        TASDomicilio domicilioFiltrado = null;
        for (TASDomicilio domicilio : domicilios) {
            if (domicilio.getIdTipoDomicilio().equals("DP")) {
                if (domicilioFiltrado == null || domicilio.getFechaModificacion().compareTo(domicilioFiltrado.getFechaModificacion()) > 0) {
                    domicilioFiltrado = domicilio;
                }
            }        
        }
        return domicilioFiltrado;
    }

    private static Objeto armarObjetoDatosPersonales(String mail, TASCliente datosCliente){
        Objeto datosPersonales = new Objeto();
        datosPersonales.set("correoElectronico", mail);
        datosPersonales.set("situacionLaboral", datosCliente.getIdSituacionLaboral() != null ? datosCliente.getIdSituacionLaboral() : "");
	    datosPersonales.set("codSituacionLaboral", datosCliente.getIdSituacionLaboral() != null ? datosCliente.getIdSituacionLaboral() : "");
	    datosPersonales.set("pep", datosCliente.getEsPEP());
	    datosPersonales.set("sujetoObligado", datosCliente.getEsSO());
	    datosPersonales.set("nroIdTrib", datosCliente.getCuit() != null ? datosCliente.getCuit() : "");
        

        return datosPersonales;
    }

    private static Objeto armarObjetoDomicilio(TASDomicilio domicilio){
        Objeto objDomicilio = new Objeto();
        objDomicilio.set("secuencialDomicilio", domicilio.getIdCore());
        objDomicilio.set("calle", domicilio.getCalle());
        objDomicilio.set("altura", domicilio.getNumero());
        objDomicilio.set("piso", domicilio.getPiso());
        objDomicilio.set("departamento", domicilio.getDepartamento());
        objDomicilio.set("CodPostal", domicilio.getIdCodigoPostal());
        objDomicilio.set("provincia", domicilio.getIdProvincia());
        objDomicilio.set("descProvincia", domicilio.getProvincia());
        return objDomicilio;
    }

    private static Objeto armarObjetoRegimenInformativo(ContextoTAS contexto, TASCliente datosCliente){
        Objeto regimenInformativoCliente = new Objeto();
        regimenInformativoCliente.set("paisResTrib1", datosCliente.getIdPaisResidencia1() != null ? datosCliente.getIdPaisResidencia1() : "");
        regimenInformativoCliente.set("codResTrib1", datosCliente.getIdPaisResidencia1() != null ? datosCliente.getIdPaisResidencia1() : "");
        regimenInformativoCliente.set("paisResTrib2", datosCliente.getIdPaisResidencia2() != null ? datosCliente.getIdPaisResidencia2() : "");
        regimenInformativoCliente.set("codResTrib2", datosCliente.getIdPaisResidencia2() != null ? datosCliente.getIdPaisResidencia2() : "");
        regimenInformativoCliente.set("nroContPais1", datosCliente.getNumeroContribuyente1() != null ? datosCliente.getNumeroContribuyente1() : "");
        regimenInformativoCliente.set("nroContPais2", datosCliente.getNumeroContribuyente2() != null ? datosCliente.getNumeroContribuyente2() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        regimenInformativoCliente.set("fechaDesdePais1", datosCliente.getFechaInicialResidencia1() != null ? sdf.format(datosCliente.getFechaInicialResidencia1()) : "");
        regimenInformativoCliente.set("fechaDesdePais2", datosCliente.getFechaInicialResidencia2() != null ? sdf.format(datosCliente.getFechaInicialResidencia2()) : "");
        regimenInformativoCliente.set("fechaHastaPais1", datosCliente.getFechaFinalResidencia1() != null ? sdf.format(datosCliente.getFechaFinalResidencia1()) : "");
        regimenInformativoCliente.set("fechaHastaPais2", datosCliente.getFechaFinalResidencia2() != null ? sdf.format(datosCliente.getFechaFinalResidencia2()) : "");
        regimenInformativoCliente.set("categoriaFATCA", datosCliente.getIdCategoriaFatca() != null ? datosCliente.getIdCategoriaFatca() : "");
        regimenInformativoCliente.set("subategoriaFATCA", "");       
        return regimenInformativoCliente;
    }

}
