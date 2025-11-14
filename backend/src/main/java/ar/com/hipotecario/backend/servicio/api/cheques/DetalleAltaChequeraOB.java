package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class DetalleAltaChequeraOB extends ApiObjetos<DetalleAltaChequeraOB.DetalleAltaChequera> {

    DetalleAltaChequera detalle;
    private static final String COD_BH = "044";
    private static final int COD_PESOS = 80;
    public static class DetalleAltaChequera extends ApiObjeto{
        public int NRO_CHEQUERA;
    }

    public static DetalleAltaChequera post(ContextoOB contexto, String cuentaBanco, String tipoChequera){
        ApiRequest request = new ApiRequest("Alta de chequera","cheques","POST","/v1/chequera",contexto);
        request.body("cod_banco",COD_BH);
        request.body("cta_banco",cuentaBanco);
        request.body("moneda",COD_PESOS);
        request.body("oficina",0);
        request.body("serie","L");
        request.body("tipo_chequera",tipoChequera);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DetalleAltaChequera.class);
    }
}
