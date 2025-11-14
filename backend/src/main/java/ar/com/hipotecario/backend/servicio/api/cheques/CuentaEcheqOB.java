package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class CuentaEcheqOB extends ApiObjeto {
    public result result;
    public class Respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }
    public class Cuenta extends ApiObjeto{
        public String emisor_cbu;
        public String emisor_cuenta;
        public String cuenta_estado;
        public String emisor_moneda;
    }
    public class result extends ApiObjeto{
        public List<Cuenta> cuentas;
        public Respuesta respuesta;
        public int total_cuentas;
    }

    public static CuentaEcheqOB get(ContextoOB contexto, String cuit){
        ApiRequest request = new ApiRequest("API-Cheques_ConsultaCuenta_Echeq","cheques","GET","/v1/cuenta",contexto);
        request.query("$select","cuentas.emisor_cbu,cuentas.emisor_cuenta,cuentas.cuenta_estado,cuentas.emisor_moneda");
        request.query("$filter",String.format("cuentas.emisor_cuit eq __%s__",cuit));
        request.cache = false;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(CuentaEcheqOB.class);
    }
}
