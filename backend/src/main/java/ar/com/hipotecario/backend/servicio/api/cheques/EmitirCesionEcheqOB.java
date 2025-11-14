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

public class EmitirCesionEcheqOB extends ApiObjetos<EmitirCesionEcheqOB.result> {
    public result result;
    public class result extends ApiObjeto {
        public respuesta respuesta;
        public cesion cesion;
    }
    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }
    public class cesion {
        public String cesion_id;
        public String cheque_id;
        public String cheque_numero;
        public String monto;
        public String fecha_emision_cesion;
        public String fecha_ult_modificacion_cesion;
        public String cesionario_documento_tipo;
        public String cesionario_documento;
        public String cesionario_nombre;
        public String cesionario_domicilio;
        public String estado_cesion;
    }

    public static EmitirCesionEcheqOB post(ContextoOB contexto, String idCheque, String cedenteDocumento, String cesionarioDocumento,String cesionarioDocumentoTipo,String cesionarioDomicilio,String cesionarioNombre){
        ApiRequest request = new ApiRequest("Emitir cesion cheque","cheques","POST","/v1/cheque/cesion/emitir",contexto);
        request.body("cheque_id",idCheque);
        request.body("cedente_documento",cedenteDocumento);
        request.body("cedente_documento_tipo","cuit");
        request.body("cesionario_documento",cesionarioDocumento);
        request.body("cesionario_documento_tipo",cesionarioDocumentoTipo);
        request.body("cesionario_domicilio",cesionarioDomicilio);
        request.body("cesionario_nombre",cesionarioNombre);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return  response.crear(EmitirCesionEcheqOB.class);

    }


}
