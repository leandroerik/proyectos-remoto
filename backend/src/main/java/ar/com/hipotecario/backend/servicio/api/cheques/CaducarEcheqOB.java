package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.ArrayList;
import java.util.List;

public class CaducarEcheqOB extends ApiObjetos<CaducarEcheqOB.result> {
    public result result;

    public class ChequeResponse {
        private result result;

    }

    class result extends ApiObjeto  {
        private List<Respuesta> respuesta;


    }

    class respuesta extends ApiObjeto {
        private String cheque_id;
        private RespuestaEcheqBody respuesta;

    }

    class RespuestaEcheqBody extends ApiObjeto {
        private String codigo;
        private String descripcion;
    }

    public static class cheque_id extends ApiObjeto{
        String cheque_id;
    }

    public static CaducarEcheqOB post(ContextoOB contexto,String idCheque){
        ApiRequest request = new ApiRequest("Caducar cheque","cheques","POST","/v1/cheque/activo/chequeCaducado",contexto);
        cheque_id chq = new cheque_id();
        chq.cheque_id = idCheque;
        List<cheque_id> cheques = new ArrayList<>();
        cheques.add(chq);
        request.body("cheques",cheques);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return  response.crear(CaducarEcheqOB.class);
    }
}
