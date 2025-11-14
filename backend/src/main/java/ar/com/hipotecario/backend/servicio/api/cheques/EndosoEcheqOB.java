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
import java.util.Optional;

public class EndosoEcheqOB extends ApiObjetos<EndosoEcheqOB.result> {

    public result result;
    public class result extends ApiObjeto {
        public respuesta respuesta;
    }
    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }

    public static EndosoEcheqOB endosarEcheq(ContextoOB contexto, String idCheque, String tipoDocumentoEmisor, String documentoEmisor, String tipoDocumentoBeneficiario, String documentoBeneficiario, String tipoEndoso,String emailBeneficiario){
        ApiRequest request = new ApiRequest("Endoso eCheq","cheques","POST","/v1/cheque/activo/endosar",contexto);
        Optional.ofNullable(emailBeneficiario).ifPresentOrElse(email->{
            request.query("enviarMail",true);
            request.body("nuevo_beneficiario_email",email);
        },()->{
            request.query("enviarMail",false);
        });
        request.body("cheque_id",idCheque);
        request.body("beneficiario_documento_tipo",tipoDocumentoEmisor);
        request.body("beneficiario_documento",documentoEmisor);
        request.body("nuevo_beneficiario_documento_tipo",tipoDocumentoBeneficiario);
        request.body("nuevo_beneficiario_documento",documentoBeneficiario);
        request.body("tipo_endoso",tipoEndoso);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(EndosoEcheqOB.class);


    }
}
