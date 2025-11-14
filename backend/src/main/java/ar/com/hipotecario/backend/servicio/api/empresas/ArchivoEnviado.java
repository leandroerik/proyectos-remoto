package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchivoEnviado extends ApiObjeto {


    public Archivo archivoEnviado;

    public static  class Archivo  extends ApiObjeto {
        public String IDArchEnviado;
        public String archivo;
    }


    public static ArchivoEnviado get(ContextoOB contexto, int idLote){
            ApiRequest request = new ApiRequest("API-Empresas_descargaArchivosEnviadosPagoProveedores", "empresas", "GET", "/v1/sat/descargaArchivosEnviadosPagoProveedores", contexto);
            request.header("Content-Type", "application/json");
            SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateString = timestamp.format(new Date());
        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID", "OB");
        request.query("id", contexto.sesion().usuarioOB.idCobis+"-"+contexto.sesion().usuarioOB.apellido);
        request.query("IdLote", idLote);
        request.query("idUsrLog", contexto.sesion().usuarioOB.idCobis);
        request.query("tipoID", "1");
        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(ArchivoEnviado.class);


    }




}
