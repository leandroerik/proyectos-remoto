package ar.com.hipotecario.backend.servicio.api.cheques;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB.cheques;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class RescatarEcheqOB extends ApiObjetos<RescatarEcheqOB.result> {
        public result result;
    class result extends ApiObjeto {
        private List<Respuesta> respuesta;
    }

    class Respuesta extends ApiObjeto {
        private String cheque_id;
        private RespuestaEcheqBody respuesta;
    }

    class RespuestaEcheqBody extends ApiObjeto {
        private String codigo;
        private String descripcion;
    }

    private static class AutorizacionParametro extends ApiObjeto {
        Integer modulo;
        Integer transaccion;
        String usuario;
    }
    
    private static class ClienteRescate extends ApiObjeto {
        String cuenta;
        String numeroDocumento;
        Integer tipoDocumento;
    }
    
    private static class Autorizante extends ApiObjeto {
        String autorizanteNombre;
        String numeroDocumento;
        Integer tipoDocumento;
    }
    
    private static class ParametrosRescate extends ApiObjeto {
        List<Autorizante> autorizantes;
        String echeqId;
    }

    public static RescatarEcheqOB rescatarEcheq(ContextoOB contexto, cheques cheque, String cuentaDeposito, String cbuDeposito) {
        ApiRequest request = new ApiRequest("Rescate eCheq", "cheques", "POST", "/v1/factoring/transacciones/rescate/echeq", contexto);
        
        AutorizacionParametro autorizacionParametro = new AutorizacionParametro();
        autorizacionParametro.modulo = 802;
        autorizacionParametro.transaccion = 997;
        autorizacionParametro.usuario = "OFB";
        
        ClienteRescate cliente = new ClienteRescate();
        cliente.tipoDocumento = 11;
        cliente.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        cliente.cuenta = cuentaDeposito;
        
        Autorizante autorizante = new Autorizante();
        autorizante.autorizanteNombre = contexto.sesion().usuarioOB.nombre;
        autorizante.numeroDocumento = contexto.sesion().usuarioOB.cuil.toString();
        autorizante.tipoDocumento = 8;
        List<Autorizante> autorizantes = new ArrayList<Autorizante>();
        autorizantes.add(autorizante);
        
        ParametrosRescate parametros = new ParametrosRescate();
        parametros.autorizantes = autorizantes;
        parametros.echeqId = cheque.cheque_id;

        request.body("autorizacionParametro", autorizacionParametro);
        request.body("cliente",cliente);
        
        request.body("parametros", parametros);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(RescatarEcheqOB.class);
    }


}
