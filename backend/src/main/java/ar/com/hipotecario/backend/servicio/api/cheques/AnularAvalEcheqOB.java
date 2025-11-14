package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.utils.firmante;

import java.util.ArrayList;
import java.util.List;

public class AnularAvalEcheqOB extends ApiObjetos<AnularAvalEcheqOB.result> {
    public result result;
    public class result extends ApiObjeto {
        public respuesta respuesta;
    }
    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }

    public static AnularAvalEcheqOB post(ContextoOB contexto, String idCheque, String avalistaDocumento, String tenedorDocumento){
        ApiRequest request = new ApiRequest("Anulacion cesion cheque","cheques","POST","/v1/cheque/aval/anulacion",contexto);
        request.body("cheque_id",idCheque);
        request.body("aval_documento",avalistaDocumento);
        request.body("aval_documento_tipo","cuit");
        request.body("tenedor_documento",tenedorDocumento);
        request.body("tenedor_documento_tipo","cuit");
        request.body("motivo_anulacion","anulacion aval");
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(AnularAvalEcheqOB.class);

    }
}
