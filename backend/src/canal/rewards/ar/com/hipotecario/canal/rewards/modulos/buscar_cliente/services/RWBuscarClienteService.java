package ar.com.hipotecario.canal.rewards.modulos.buscar_cliente.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiPersona;
import ar.com.hipotecario.canal.rewards.middleware.models.dto.RWPersonaDTO;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWCliente;

import java.util.ArrayList;
import java.util.List;

public class RWBuscarClienteService {
    public static Objeto buscarCliente(ContextoRewards contexto, String cobis, String documento) {

        try {
            String tipo = !cobis.isEmpty() ? "cobis" : "documento";
            String dato = !cobis.isEmpty() ? cobis : documento;

            RWPersonaDTO persona = new RWPersonaDTO();
            List<RWCliente> clientes = new ArrayList<>();

            if (tipo.equals("documento")) {
                String tipoDocu = String.valueOf(dato.length() == 11);
                clientes = RWApiPersona.getDatosClienteByNroDoc(contexto, dato, tipoDocu);
            } else {
                clientes = RWApiPersona.getDatosClienteByIdCobis(contexto, dato); // OK
            }
            persona.set("status", 0);
            persona.set("cliente", clientes);
            return persona;

        } catch (ApiException e) {
            Objeto response = new Objeto();

            if (e.response.codigoHttp.equals(204)) {
                return RespuestaRW.error(contexto, "SIN_DATOS");
            } else {
                response.set("error",
                        RespuestaRW.error(contexto, e.getMessage(), e, "Api Exception: ERROR_BUSCAR_CLIENTE"));
            }

            return response;
        } catch (Exception e) {
            Objeto response = new Objeto();
            response.set("error", RespuestaRW.error(contexto,
                    "RWLoginService.login - Exception", e, "Api Exception: ERROR_BUSCAR_CLIENTE"));

            return response;
        }
    }
}
