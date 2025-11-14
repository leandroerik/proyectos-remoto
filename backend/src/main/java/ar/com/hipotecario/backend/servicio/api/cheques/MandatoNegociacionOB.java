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

public class MandatoNegociacionOB extends ApiObjetos<MandatoNegociacionOB.result> {
    public result result;


    class RespuestaEcheqBody extends ApiObjeto {
        private String codigo;
        private String descripcion;
    }
    class Respuesta extends ApiObjeto {
        private RespuestaEcheqBody respuesta;
    }
    class result extends ApiObjeto {
        private List<Respuesta> respuesta;
    }
    public static MandatoNegociacionOB mandatoNegociacion(ContextoOB contexto, String idCheque, String documentoMandatario, String domicilioMandante) {
        ApiRequest request = new ApiRequest("Mandato negociacion eCheq eCheq", "cheques", "POST", "/v1/cheque/mandatonegociacion", contexto);
        request.body("cheque_id",idCheque);
        request.body("documento_mandante",contexto.sesion().empresaOB.cuit.toString());
        request.body("documento_mandatario",documentoMandatario);
        request.body("domicilio_mandante",domicilioMandante);

        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(MandatoNegociacionOB.class);
    }

    public static MandatoNegociacionOB admitirMandatoNegociacion(ContextoOB contexto, String idCheque, String cuit) {
        ApiRequest request = new ApiRequest("Admitir Mandato Negociacion","cheques","POST","/v1/cheque/mandatonegociacion/admitir",contexto);
        request.body("cheque_id",idCheque);
        request.body("documento_mandatario",cuit);

        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(MandatoNegociacionOB.class);
    }

    public static MandatoNegociacionOB repudiarMandatoNegociacion(ContextoOB contexto, String idCheque, String motivoRepudio) {
        ApiRequest request = new ApiRequest("Repudiar Mandato Negociacion","cheques","POST","/v1/cheque/mandatonegociacion/repudiar",contexto);
        request.body("cheque_id",idCheque);
        request.body("documento_mandatario",contexto.sesion().empresaOB.cuit.toString());
        request.body("motivo_repudio",motivoRepudio);


        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(MandatoNegociacionOB.class);
    }

    public static MandatoNegociacionOB anularMandatoNegociacion(ContextoOB contexto, String chequeId) {
        ApiRequest request = new ApiRequest("Anular Mandato Negociacion","cheques","POST","/v1/cheque/mandatonegociacion/anular",contexto);
        request.body("cheque_id",chequeId);
        request.body("documento_mandante",contexto.sesion().empresaOB.cuit.toString());
        request.body("motivo_anulacion"," ");


        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(MandatoNegociacionOB.class);
    }

    public static MandatoNegociacionOB revocarMandatoNegociacion(ContextoOB contexto, String idCheque, String cuit, String motivoRevocatoria) {
        ApiRequest request = new ApiRequest("Revocatoria de mandato para negociacion","cheques","POST","/v1/cheque/mandatonegociacion/revocar",contexto);
        request.body("cheque_id",idCheque);
        request.body("documento_mandante",cuit);
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);
        request.body("motivo_revocatoria",motivoRevocatoria);

        request.cache = false;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(MandatoNegociacionOB.class);

    }
}
