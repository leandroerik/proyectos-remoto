package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class CuentasQR extends ApiObjetos<CuentasQR.CuentaQR> {
    public static class CuentaQR extends ApiObjeto {
        private String base64;
        private String id;
        private String urlQR;

        public String getBase64(){
            return base64;
        }

        public String getId(){
            return id;
        }

        public String getUrlQR(){
            return urlQR;
        }
    }

    public static class ErrorApiResponse extends ApiObjeto{
        private String codigo;
        private String mensajeAlUsuario;

        public String getMensajeAlUsuario() {
            return mensajeAlUsuario;
        }

        public String getCodigo(){
            return codigo;
        }
    }

    public static Objeto obtenerQRCuenta(Contexto contexto, String cbu, String cuit, String tipoCuenta){
        try {
            ApiRequest request = new ApiRequest("ApiEmpresas", "empresas", "GET", "/v1/comercios/{cuit}/qr/{cbu}", contexto);
            request.path("cuit", cuit);
            request.path("cbu", cbu);
            request.cache = false;

            ApiResponse response = request.ejecutar();
            Objeto data = new Objeto();
            if(!response.http(200, 204)){
                if(response.http(404)){
                    ApiRequest requestGenerarQR = new ApiRequest("ApiEmpresas", "empresas", "POST", "/v1/comercios/{cuit}/qr", contexto);
                    requestGenerarQR.path("cuit", cuit);
                    requestGenerarQR.body("cbu", cbu);
                    requestGenerarQR.body("cuit", cuit);
                    requestGenerarQR.body("tipoCuenta","CTE");
                    requestGenerarQR.cache = false;

                    ApiResponse responseGenerarQR = requestGenerarQR.ejecutar();
                    if(!responseGenerarQR.http(200, 204)){
                        CuentasQR.ErrorApiResponse errorResponseGenerarQR = response.crear(CuentasQR.ErrorApiResponse.class);
                        data.set("codigo",errorResponseGenerarQR.getCodigo());
                        data.set("mensaje",errorResponseGenerarQR.getMensajeAlUsuario());
                        data.set("error",true);
                        return data;
                    }
                    CuentasQR.CuentaQR datosQR = responseGenerarQR.crear(CuentasQR.CuentaQR.class);
                    data.set("base64",datosQR.getBase64());
                    data.set("error",false);
                    return data;
                }else {
                    CuentasQR.ErrorApiResponse errorResponse = response.crear(CuentasQR.ErrorApiResponse.class);
                    data.set("codigo",errorResponse.getCodigo());
                    data.set("mensaje",errorResponse.getMensajeAlUsuario());
                    data.set("error",true);
                    return data;
                }
            }

            CuentasQR.CuentaQR datosQR = response.crear(CuentasQR.CuentaQR.class);
            data.set("base64",datosQR.getBase64());
            data.set("error",false);
            return data;

        }catch (ApiException e){
            Objeto data = new Objeto();
            data.set("mensaje", "Ocurrió un error interno de la API");
            data.set("codigo","500");
            data.set("error", true);
            return data;
        }
    }
    public static void main(String[] args) {}
}
