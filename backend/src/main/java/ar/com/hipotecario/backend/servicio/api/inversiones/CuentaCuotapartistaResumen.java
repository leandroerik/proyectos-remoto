// src/main/java/ar/com/hipotecario/backend/servicio/api/inversiones/CuentaCuotapartistaResumen.java
package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import java.util.Base64;

public class CuentaCuotapartistaResumen extends ApiObjeto {
    public String Response;
    public boolean Success;

    public static CuentaCuotapartistaResumen post(Contexto contexto, CuentaCuotapartistaResumenRequest requestObj) {
        ApiRequest request = new ApiRequest("CuentaCuotapartistaResumen", "inversiones", "POST", "/v1/cuenta/resumen", contexto);
        request.body("codCuotapartista", requestObj.getCodCuotapartista());
        request.body("codFondo", requestObj.getCodFondo());
        request.body("fechaDesde", requestObj.getFechaDesde());
        request.body("fechaHasta", requestObj.getFechaHasta());
        request.body("soloCtasAct", requestObj.isSoloCtasAct());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);

        System.out.println(requestObj.getCodCuotapartista());
        System.out.println(requestObj.getCodFondo());
        System.out.println(requestObj.getFechaDesde());
        System.out.println(requestObj.getFechaHasta());
        System.out.println(requestObj.isSoloCtasAct());

        return response.crear(CuentaCuotapartistaResumen.class);
    }
}