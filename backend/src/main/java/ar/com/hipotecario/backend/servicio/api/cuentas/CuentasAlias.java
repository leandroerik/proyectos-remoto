package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasAlias.CuentaAlia;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.ModuloOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioMonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CuentasAlias extends ApiObjetos<CuentaAlia> {

    /* ========== ATRIBUTOS ========== */
    public static class CuentaAlia extends ApiObjeto {
        public String aliasValorOriginal;

        public String getAliasValorOriginal(){
            return aliasValorOriginal;
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

    /* ========== SERVICIOS ========== */
    // API-Cuentas_ConsultaCuentaIdcliente
    static Objeto update(Contexto contexto, String cbu, String alias, String nuevoAlias, String cuit, Boolean tieneAlias){
        try{
            ApiRequest request;
            if (tieneAlias) {
                request = new ApiRequest("CuentasPatchAlias", "cuentas", "PATCH", "/v1/alias", contexto);
                request.body("cuit", cuit);
                request.body("nro_bco", "044");
                request.body("nro_cbu", cbu);
                request.body("reasigna", null);
                request.body("valor", nuevoAlias);
                request.body("valor_original", alias);
            } else {
                request = new ApiRequest("CuentasPutAlias", "cuentas", "PUT", "/v1/alias", contexto);
                request.body("cuit", cuit);
                request.body("nro_bco", "044");
                request.body("nro_cbu", cbu);
                request.body("reasigna", null);
                request.body("valor", nuevoAlias);
                request.body("valor_original", alias);
            }
            request.cache = false;
            ApiResponse response = request.ejecutar();
            Objeto data = new Objeto();
            if(!response.http(200, 204)){
                ErrorApiResponse errorResponse = response.crear(ErrorApiResponse.class);
                data.set("codigo",errorResponse.getCodigo());
                data.set("mensaje",errorResponse.getMensajeAlUsuario());
                data.set("error",true);
                return data;
            }
            CuentaAlia cuenta = response.crear(CuentaAlia.class);
            data.set("alias",cuenta.getAliasValorOriginal());
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

    public static Objeto infoAlias(Contexto contexto, String cbuAlias, String cuitCuenta) {
        try{
            String cuit = cuitCuenta;
            String cbu = ModuloOB.cbuValido(cbuAlias) ? cbuAlias : null;
            String alias = ModuloOB.aliasValido(cbuAlias) ? cbuAlias : null;

            ApiRequest request = new ApiRequest("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
            request.query("cbu", cbu);
            request.query("alias", alias);
            request.query("consultaalias", true);
            request.query("acuerdo", false);
            request.header("x-usuarioIP", contexto.ip());
            request.cache = false;

            ApiResponse response = request.ejecutar();

            Objeto data = new Objeto();
            if(!response.http(200, 204)){
                List<String> mensajesDeErrores = List.of(
                        "CBU NO EXISTE",
                        "CBU INCORRECTO",
                        "ALIAS NO EXISTE",
                        "NO TIENE ALIAS ASIGNADO",
                        "ALIAS MAL FORMULADO",
                        "EL ALIAS SE ENCUENTRA ELIMINADO",
                        "CBU ENCONTRADO CON ALIAS ASIGNADO, PERO LA CUENTA NO ESTÁ ACTIVA");
                ErrorApiResponse errorResponse = response.crear(ErrorApiResponse.class);
                Boolean validarAlias = false;
                for (String mensaje : mensajesDeErrores) {
                    if (errorResponse.getMensajeAlUsuario() != null && errorResponse.getMensajeAlUsuario().contains(mensaje)) {
                        validarAlias = true;
                    }
                }

                if(validarAlias){
                    data.set("alias",null);
                    data.set("error",false);
                }else {
                    data.set("codigo",errorResponse.getCodigo());
                    data.set("mensaje",errorResponse.getMensajeAlUsuario());
                    data.set("error",true);
                }
                return data;
            }else{
                CuentaCoelsa datosAlias = response.crear(CuentaCoelsa.class);

                data.set("alias",datosAlias.nuevoAlias);
                data.set("error",false);

                return data;
            }
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
