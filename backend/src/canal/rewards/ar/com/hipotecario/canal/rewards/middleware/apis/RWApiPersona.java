package ar.com.hipotecario.canal.rewards.middleware.apis;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioNovedad;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWCliente;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWClienteDNI;
import com.azure.json.implementation.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RWApiPersona {
    public static List<RWCliente>  getDatosClienteByIdCobis(Contexto contexto, String idCliente) {

        List<RWCliente> clientes = new ArrayList<>();

        ApiRequest request = new ApiRequest("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
        request.path("idCliente", idCliente);
        request.cache = false;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no fue encontrada en BUP"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        RWCliente cliente = response.crear(RWCliente.class, response.objetos(0));
        clientes.add(cliente);
        return clientes;
    }

    public static RWCliente getDatosClienteByNroCuit(Contexto contexto, String nrodoc) {

        ApiRequest request = new ApiRequest("Cliente", "personas", "GET",
                "/personas/{nroDoc}", contexto);
        request.path("nroDoc", nrodoc);
        request.cache = false;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE",
                response.contains("no fue encontrada en BUP") || response.contains("no fue encontrada en bup o core"),
                request, response);
        ApiException.throwIf(!response.http(200) || response.body.isEmpty(), request, response);

        return response.crear(RWCliente.class, response);
    }

    public static List<RWCliente> getDatosClienteByNroDoc(Contexto contexto, String nrodoc, String consultaCuil) {
        List<RWCliente> clientes = new ArrayList<>();

        if(consultaCuil == "true"){
            RWCliente cliente = getDatosClienteByNroCuit(contexto, nrodoc);

            if(cliente != null){
                clientes.add(cliente);
            }
            return clientes;
        }
        else{
            ApiRequest request = new ApiRequest("Cliente", "personas", "GET",
                    "/personas", contexto);
            request.query("nroDocumento", nrodoc);
            request.cache = false;
            ApiResponse response = request.ejecutar();

            ApiException.throwIf("CLIENTE_NO_EXISTE",
                    response.contains("no fue encontrada en BUP") || response.contains("no fue encontrada en bup o core"),
                    request, response);
            ApiException.throwIf(!response.http(200) || response.body.isEmpty(), request, response);

            for (Objeto obj : response.objetos()) {
                RWClienteDNI clienteDni = response.crear(RWClienteDNI.class, obj);
                RWCliente clienteResp = getDatosClienteByNroCuit(contexto, clienteDni.getNumeroIdentificacionTributaria());
                clientes.add(clienteResp);
            }

            return clientes;
        }
    }
}