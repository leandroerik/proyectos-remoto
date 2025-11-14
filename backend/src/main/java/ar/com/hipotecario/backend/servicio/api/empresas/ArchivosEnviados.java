package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
public class ArchivosEnviados extends ApiObjeto {

    public ArchivosEnvia2 archivosEnviados;

    public static class ArchivosEnvia2 extends ApiObjeto{
        public List<ArchivoEnviado> archivoEnviado;
    }
    public static class ArchivoEnviado extends ApiObjeto {
        public int IDLote;
        public String tipoArch;
        public String nroConc;
        public String nroAdh;
        public String nomAdh;
        public int nroConv;
        public int nroSubConv;
        public String descNroSubConv;
        public int estado;
        public String descEstado;
        public String archVacio;
        public String fechaGen;
        public String horaGeneracion;
        public String idArchRecibido;
        public String idArchEnviado;
        public String archDebeEnviarse;
    }

public  static ArchivosEnviados get(ContextoOB contextoOB, int nroAdh, int nroConv, int nroSubConv,String fechaD,String fechaH,String tipoArch){
    ApiRequest apiRequest = new ApiRequest("Api-Empresas_archivosRecibidosOP", "empresas", "GET", "/v1/sat/archivosEnviadosPagoProveedores", contextoOB);


    SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String dateString = timestamp.format(new Date());
    apiRequest.query("cantRegMostrar", 500);
    apiRequest.query("codigoEntidad", 044);
    apiRequest.query("fecGeneracionD", fechaD);
    apiRequest.query("fecGeneracionH", fechaH);
    apiRequest.query("fechaHoraConsulta", dateString);  // Si se codifica la URL, los espacios se convierten en %20
    apiRequest.query("frontEnd", "2");
    apiRequest.query("frontEndID", "OB");
    apiRequest.query("id", contextoOB.sesion().usuarioOB.idCobis+"-"+contextoOB.sesion().usuarioOB.apellido);
    apiRequest.query("idUsrLog", contextoOB.sesion().usuarioOB.idCobis);
    apiRequest.query("maxReg", 500);
    apiRequest.query("nroAdh", nroAdh);
    apiRequest.query("nroConsulta", 0);
    apiRequest.query("nroConv", nroConv);
    apiRequest.query("nroPagina", 1);
    apiRequest.query("nroSubConv", nroSubConv);
    apiRequest.query("tipoArch", tipoArch);
    apiRequest.query("tipoID", "1");
    ApiResponse apiResponse = apiRequest.ejecutar();
    ArchivosEnviados ArchivosEnviados = apiResponse.crear(ArchivosEnviados.class);
    ApiException.throwIf(!apiResponse.http(200, 204, 404), apiRequest, apiResponse);

    return ArchivosEnviados;
}





    
}
