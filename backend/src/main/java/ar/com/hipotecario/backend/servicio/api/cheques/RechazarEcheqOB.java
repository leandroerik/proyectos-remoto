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

public class RechazarEcheqOB extends ApiObjetos<RechazarEcheqOB.result> {

        public result result;
        public class result extends ApiObjeto {
            public respuesta respuesta;
            public String cod_visualizacion;
        }
        public class respuesta extends ApiObjeto {
            public String codigo;
            public String descripcion;
        }

        public static RechazarEcheqOB post(ContextoOB contexto, String idCheque, String beneficiarioDocumento, String beneficiarioDocumentoTipo,String motivoRepudio){
            ApiRequest request = new ApiRequest("Repudio cheque","cheques","POST","/v1/cheque/emitido/repudio",contexto);
            request.body("cheque_id",idCheque);
            request.body("beneficiario_documento_tipo",beneficiarioDocumentoTipo);
            request.body("beneficiario_documento",beneficiarioDocumento);
            request.body("cheque_motivo_repudio",motivoRepudio);
            firmante firmante = new firmante();
            firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
            firmante.documento_tipo = "CUIL";
            List<firmante> listaFirmantes = new ArrayList<>();
            listaFirmantes.add(firmante);
            request.body("firmantes",listaFirmantes);
            request.cache = false;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200), request, response);
            return  response.crear(RechazarEcheqOB.class);
        }
}
