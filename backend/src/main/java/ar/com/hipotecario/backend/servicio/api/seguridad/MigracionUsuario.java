package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.transmit.OpcionMigradoEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MigracionUsuario extends ApiObjeto {

    /* ========== ATRIBUTOS ========== */
    private static Logger log = LoggerFactory.getLogger(MigracionUsuario.class);

    /* ========== REQUEST ========== */
    public static class RequestMigracionUsuario extends ApiObjeto {

        public RequestMigracionUsuario() {
        }

        public RequestMigracionUsuario(int dni, int nuevoValor, OpcionMigradoEnum opcion, String idCobis, String descripcionError, String fechaError) {
            this.dni = dni;
            this.nuevoValor = nuevoValor;
            this.opcion = opcion;
            this.idCobis = idCobis;
            this.fechaError = fechaError;
            this.descripcionError = descripcionError;
        }

        public int dni;
        public String idCobis;
        public int nuevoValor;
        public OpcionMigradoEnum opcion;
        public String descripcionError;
        public String fechaError;
    }

    /* ========== RESPONSE ========== */
    public static class ResponseMigracionUsuario extends ApiObjeto {
        public int codRet;
        public String mensajeRet;
        public String migrado;
        public String ultimaFechaModificacionClave;
        public String ultimaFechaModificacionUsuario;
        public String mensajeError;
        public String fechaError;

        public ResponseMigracionUsuario() {
        }

        public ResponseMigracionUsuario(int codRet, String mensajeRet, String migrado, String ultimaFechaModificacionClave, String ultimaFechaModificacionUsuario, String mensajeError, String fechaError) {
            this.codRet = codRet;
            this.mensajeRet = mensajeRet;
            this.migrado = migrado;
            this.ultimaFechaModificacionClave = ultimaFechaModificacionClave;
            this.ultimaFechaModificacionUsuario = ultimaFechaModificacionUsuario;
            this.mensajeError = mensajeError;
            this.fechaError = fechaError;
        }

    }

    public static ResponseMigracionUsuario gestionarMigracion(Contexto contexto, RequestMigracionUsuario body) {
        ApiRequest request = new ApiRequest("MigracionMinorista", "seguridad", "POST", "/v1/migracionbm", contexto);

        request.body("dni", body.dni);
        request.body("nuevoValor", body.nuevoValor);
        request.body("opcion", body.opcion.getCodigo());
        request.body("idCobis", body.idCobis);
        request.body("fechaError", body.fechaError);
        request.body("mensajeError", body.descripcionError);

        request.cache = false;

        log.info("TRANSMIT - gestionarMigracion - request: {}", request);
        log.info("TRANSMIT - gestionarMigracion - request.url(): {}", request.url());
        ApiResponse response = request.ejecutar();
        log.info("TRANSMIT - gestionarMigracion - response: {}", response);
        ApiException.throwIf(!response.http(200, 204), request, response);
        log.info("TRANSMIT - gestionarMigracion - response.crear: {}", response.crear(ResponseMigracionUsuario.class));
        return response.crear(ResponseMigracionUsuario.class);
    }

}
