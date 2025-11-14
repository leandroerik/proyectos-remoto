package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AltaCuentaEcheqOB extends ApiObjeto {
    public result result;
    public class respuesta extends ApiObjeto{
        public String codigo;
        public String descripcion;
    }
    public class result extends ApiObjeto{
        public respuesta respuesta;
    }

    public static AltaCuentaEcheqOB post(ContextoOB contexto, String sucursalCodigo,String sucursalNombre,String sucursalDomicilio,String sucursalCP,String sucursalProvincia,String emisorCuit,String emisorRazonSocial,String emisorCBU,String emisorCuenta,String emisorMoneda,String emisorDomicilio,String emisorCP){
        ApiRequest request = new ApiRequest("API-Cheques_AltaCuenta_Echeq","cheques","POST","/v1/cuenta",contexto);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");
        request.body("sucursal_codigo",sucursalCodigo);
        request.body("sucursal_nombre",sucursalNombre);
        request.body("sucursal_domicilio",sucursalDomicilio);
        request.body("sucursal_cp",sucursalCP);
        request.body("sucursal_provincia",sucursalProvincia);
        request.body("emisor_cuit",emisorCuit);
        request.body("emisor_razon_social",emisorRazonSocial);
        request.body("emisor_cbu",emisorCBU);
        request.body("emisor_cuenta",emisorCuenta);
        request.body("emisor_cuenta_fecha_alta", LocalDateTime.now().format(formatter));
        request.body("emisor_moneda",emisorMoneda);
        request.body("emisor_domicilio",emisorDomicilio);
        request.body("emisor_cp",emisorCP);
        request.body("emisor_subcuenta","");
        request.body("emisor_emails", new String[] {});
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(AltaCuentaEcheqOB.class);
    }
}
