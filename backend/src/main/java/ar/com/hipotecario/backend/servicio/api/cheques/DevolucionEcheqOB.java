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

public class DevolucionEcheqOB extends ApiObjetos<DevolucionEcheqOB.result> {

    public result result;
    public class result extends ApiObjeto {
        public respuesta respuesta;
    }
    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }

    public static DevolucionEcheqOB solicitarDevolucion(ContextoOB contexto,String idCheque, String emisorDocumento,String motivoDevolucion,String mailBeneficiario){
        ApiRequest request = new ApiRequest("Solicitud devolucion cheque","cheques","POST","/v1/cheque/devolucion/solicitar",contexto);
        request.body("cheque_id",idCheque);
        request.body("emisor_documento_tipo","CUIT");
        request.body("emisor_documento",emisorDocumento);
        Optional.ofNullable(mailBeneficiario).ifPresentOrElse(email->{
            request.query("enviarMail",true);
            request.body("beneficiario_email",mailBeneficiario);
        },()->{
            request.query("enviarMail",false);
        });
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);
        request.body("devolucion_motivo",motivoDevolucion);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DevolucionEcheqOB.class);
    }
    public static DevolucionEcheqOB aceptarDevolucion(ContextoOB contexto,String idCheque, String beneficiarioDocumento, String beneficiarioDocumentoTipo){
        ApiRequest request = new ApiRequest("Solicitud devolucion cheque","cheques","POST","/v1/cheque/devolucion/aceptar",contexto);
        request.body("cheque_id",idCheque);
        request.body("beneficiario_documento_tipo",beneficiarioDocumentoTipo);
        request.body("beneficiario_documento",beneficiarioDocumento);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);


        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DevolucionEcheqOB.class);
    }

    public static DevolucionEcheqOB rechazarDevolucion(ContextoOB contexto,String idCheque, String beneficiarioDocumento, String beneficiarioDocumentoTipo){
        ApiRequest request = new ApiRequest("Solicitud devolucion cheque","cheques","POST","/v1/cheque/devolucion/rechazar",contexto);
        request.body("cheque_id",idCheque);
        request.body("beneficiario_documento_tipo",beneficiarioDocumentoTipo);
        request.body("beneficiario_documento",beneficiarioDocumento);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);


        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DevolucionEcheqOB.class);
    }

    public static DevolucionEcheqOB anularDevolucion(ContextoOB contexto,String idCheque, String emisorDocumento,String motivoDevolucion){
        ApiRequest request = new ApiRequest("Solicitud devolucion cheque","cheques","POST","/v1/cheque/devolucion/anular",contexto);
        request.body("cheque_id",idCheque);
        request.body("emisor_documento_tipo","CUIT");
        request.body("emisor_documento",emisorDocumento);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes",listaFirmantes);
        request.body("devolucion_motivo",motivoDevolucion);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DevolucionEcheqOB.class);
    }
}
