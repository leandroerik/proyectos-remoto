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

public class AvalEcheqOB extends ApiObjetos<AvalEcheqOB.result> {
    public result result;

    class Respuesta extends ApiObjeto {
         String cheque_id;
         RespuestaEcheqBody respuesta;
    }
    class RespuestaEcheqBody extends ApiObjeto {
         String codigo;
         String descripcion;
    }
     class result extends ApiObjeto {
         List<Respuesta> respuesta;
    }
    private static class aval extends ApiObjeto{
        String aval_caracter;
        String aval_documento;
        String aval_documento_tipo;
        String aval_domicilio;
        String aval_importe_avalado;
        String aval_nombre;
        String aval_sujeto_avalado;
    }

    public static AvalEcheqOB avalarEcheq(ContextoOB contexto, String avalDocumento, String avalDomicilio, String avalNombre, ListadoChequesOB.cheques cheque){
        ApiRequest request = new ApiRequest("Aval eCheq","cheques","POST","/v1/cheque/aval",contexto);

        aval aval = new aval();
        aval.aval_caracter=" ";
        aval.aval_documento = avalDocumento;
        aval.aval_documento_tipo = "cuit";
        aval.aval_domicilio = avalDomicilio;
        aval.aval_importe_avalado = String.valueOf(cheque.monto);
        aval.aval_nombre = avalNombre;
        aval.aval_sujeto_avalado = avalNombre;

        request.body("aval",aval);
        request.body("cheque_id",cheque.cheque_id);
        request.body("tenedor_documento",contexto.sesion().empresaOB.cuit.toString());
        request.body("tenedor_documento_tipo","cuit");
        firmante firmante = new firmante();
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        firmante.documento_tipo = "CUIL";
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        request.body("firmantes", listaFirmantes);

        request.cache = false;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(AvalEcheqOB.class);


    }
}
