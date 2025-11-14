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

public class DepositarEcheqOB extends ApiObjetos<DepositarEcheqOB.result> {
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

    private static class deposito extends ApiObjeto {
        String cheque_id;
        String beneficiario_documento_tipo;
        String  beneficiario_documento;
        String beneficiario_cbu;
    }

    public static DepositarEcheqOB depositarEcheq(ContextoOB contexto, int oficinaDep, String ctaDepositada, int producto, String nroChequeBanco, String idEcheq, String codigoVisualizacion, String codBanco, String codPostal, String ctaGirada, double importe, String tipoEcheq, String fechaPago,String cmc7, String modo, String motivoPago, String concepto, String caracter, int moneda, String fechaPresentacion, String cbu) {
        ApiRequest request = new ApiRequest("Depósito eCheq", "cheques", "POST", "/v1/cheque/activo/depositar", contexto);

        deposito deposito = new deposito();
        deposito.beneficiario_documento_tipo = "cuit";
        deposito.beneficiario_documento = contexto.sesion().empresaOB.cuit.toString();
        deposito.cheque_id = idEcheq;
        deposito.beneficiario_cbu = cbu;
        List<deposito> depositos = new ArrayList<>();
        depositos.add(deposito);
        request.body("depositos",depositos);

        request.body("oficina_dep", oficinaDep);
        request.body("cta_depositada", ctaDepositada);
        request.body("producto", producto);
        request.body("nro_cheque_banco", nroChequeBanco);
        request.body("id_echeq", idEcheq);
        request.body("codigo_visualizacion", codigoVisualizacion);
        request.body("codbanco", codBanco);
        request.body("cod_postal", codPostal);
        request.body("cta_girada", ctaGirada);
        request.body("importe", importe);
        request.body("tipo_echeq", tipoEcheq);
        request.body("fecha_pago", fechaPago);
        request.body("num_boleta", 0);
        request.body("cmc7", cmc7);
        request.body("modo", modo);
        request.body("motivoPago", motivoPago);
        request.body("concepto", concepto);
        request.body("caracter", caracter);
        request.body("moneda", moneda);
        request.body("fecha_presentacion", fechaPresentacion);
        request.body("usuario", contexto.sesion().usuarioOB.idCobis);

        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DepositarEcheqOB.class);
    }


}
