package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.servicios;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.dto.TASPersonaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.*;

import java.util.ArrayList;
import java.util.List;

public class TASRestPersona {

    public static Objeto getPersona(Contexto contexto, String idCliente){
        try {
            TASCliente cliente = getDatosCliente(contexto, idCliente);  // OK

            Futuro<List<TASDomicilio>> futuroDomicilios = new Futuro<>(() -> getDomiciliosPersona(contexto, cliente.getCuit().toString()));
            Futuro<List<TASTelefono>> futuroTelefonos = new Futuro<>(() -> getTelefonosPersona(contexto, cliente.getCuit().toString()));
            Futuro<List<TASMail>> futuroEmails = new Futuro<>(() -> getMailsPersona(contexto, cliente.getCuit().toString()));
            Futuro<List<TASEnte>> futuroEntes = new Futuro<>(() -> getEntesPersona(contexto, cliente.getNumeroDocumento()));
            Futuro<List<TASDocumento>> futuroDocumentos = new Futuro<>(() -> getDocumentosPersona(contexto, cliente.getNumeroDocumento()));

            TASPersonaDTO persona = new TASPersonaDTO();
            persona.set("cliente", cliente);
            persona.set("domicilios", futuroDomicilios.get());
            persona.set("telefonos", futuroTelefonos.get());
            persona.set("emails", futuroEmails.get());
            persona.set("entes", futuroEntes.get());
            persona.set("documentos", futuroDocumentos.get());
            persona.set("censado", true);
            return persona;
        }catch (Exception e) {
            return new Objeto().set("ERROR", e);
        }
    }

    public static List<TASClientePersona> getClientePersona(ContextoTAS contexto, String nroDoc){
        ApiRequest request = new ApiRequest("Personas", "personas", "GET", "/personas?consultaCuil=false&nroDocumento={nroDocumento}", contexto);
        request.path("nroDocumento", nroDoc);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("COBIS_CAIDO", response.contains("Server fuera de linea"), request, response);
        ApiException.throwIf("USUARIO_INVALIDO", response.contains("no fue encontrada en bup o core"), request, response);
        ApiException.throwIf(!response.http(200), request, response);
        List<TASClientePersona> clientesPersona = new ArrayList<>();

        for(Objeto obj : response.objetos()){
            TASClientePersona clientePersona = obj.toClass(TASClientePersona.class);
            clientesPersona.add(clientePersona);
        }
        return clientesPersona;
    }

    public static TASCliente getDatosCliente(Contexto contexto, String idCliente){
        ApiRequest request = new ApiRequest("Cliente", "personas","GET","/clientes/{idCliente}", contexto);
        request.path("idCliente", idCliente);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no fue encontrada en BUP"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);
        return response.crear(TASCliente.class, response.objetos(0));
    }

    public static List<TASDomicilio> getDomiciliosPersona(Contexto contexto, String cuil){
        List<TASDomicilio> domicilios =  new ArrayList<>();
        ApiRequest request = new ApiRequest("Domicilios", "personas", "GET", "/personas/{cuil}/domicilios", contexto);
        request.path("cuil", cuil );
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);

        for(Objeto obj : response.objetos()){
            TASDomicilio dom = obj.toClass(TASDomicilio.class);
            domicilios.add(dom);
        }
        return domicilios;
    }

    public static List<TASTelefono> getTelefonosPersona(Contexto contexto, String cuil){
        List<TASTelefono> telefonos = new ArrayList<>();
        ApiRequest request = new ApiRequest("Telefonos", "personas", "GET", "/personas/{cuil}/telefonos", contexto);
        request.path("cuil", cuil);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);

        for(Objeto obj : response.objetos()){
            TASTelefono tel = obj.toClass(TASTelefono.class);
            telefonos.add(tel);
        }
        return telefonos;
    }

    public static List<TASMail> getMailsPersona(Contexto contexto, String cuil){
        List<TASMail> emails = new ArrayList<>();
        ApiRequest request = new ApiRequest("Emails", "personas", "GET", "/personas/{cuil}/mails", contexto);
        request.path("cuil", cuil);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        if (response.isEmpty()) return null;
        for(Objeto obj : response.objetos()){
            TASMail email = obj.toClass(TASMail.class);
            emails.add(email);
        }
        return emails;
    }

    public static List<TASEnte> getEntesPersona(Contexto contexto, String nroDoc){
        List<TASEnte> entes = new ArrayList<>();
        ApiRequest request = new ApiRequest("DatosBasicosPersona", "personas","GET", "/personas", contexto);
        request.query("nroDocumento", nroDoc);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        for(Objeto obj : response.objetos()){
            TASEnte ente = obj.toClass(TASEnte.class);
            entes.add(ente);
        }
        return entes;
    }

    public static List<TASDocumento> getDocumentosPersona(Contexto contexto, String nroDoc){
        List <TASDocumento> documentos = new ArrayList<>();
        ApiRequest request = new ApiRequest("nroDocumento", "personas", "GET", "/nrodoc", contexto);
        request.query("nrodoc", nroDoc);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        for(Objeto obj : response.objetos()){
            TASDocumento documento = obj.toClass(TASDocumento.class);
            documentos.add(documento);
        }
        return documentos;
    }

    /*
     * metodo hardcodeado en la rta
     * ERROR 500 EN EL RESPONSE DE LA API
     * */
    public static Boolean getCensadoPersona(Contexto contexto, TASCliente cliente, String tipoDocumento){
        Boolean censado = true;
        ApiRequest request = new ApiRequest("Censo", "personas", "GET", "/censo", contexto);
        request.query("cuil", cliente.getCuit());
        request.query("idcobis", cliente.getIdCliente());
        request.query("sexo", cliente.getIdSexo());
        request.query("tipoDocumento", tipoDocumento);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        for(Objeto obj : response.objetos()){
            censado = obj.bool("censado", true);
        }
        return censado;
    }

}

