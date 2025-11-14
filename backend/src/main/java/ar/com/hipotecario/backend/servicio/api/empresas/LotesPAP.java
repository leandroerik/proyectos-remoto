package ar.com.hipotecario.backend.servicio.api.empresas;


import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LotesPAP extends ApiObjeto {
    public ArchivosRecibidos archivosRecibidos;

    public static class ArchivosRecibidos extends ApiObjeto {
        public List<ArchivoRecibido> archivoRecibido;
    }

    public RespuestaPaginado respuestaPaginado;


    public static class ArchivoRecibido extends ApiObjeto {
        public int servicio;
        public String descServicio;
        public String nroAdh;
        public String nomAdh;
        public int nroConv;
        public int nroSubConv;
        public String descNroSubConv;
        public String fecEjec;
        public int idLote;
        public String idArch;
        public String nombOrigArch;
        public String tipoArch;
        public String descTipoArch;
        public String procLotCompleto;
        public int estado;
        public String descEstado;
        public String impTotal;
        public String regTotal;
        public String tipoOper;
        public String descTipoOper;
        public String fecIngSist;
        public String horaIngSist;
        public String fecVal;
        public String horaVal;
        public String fecProc;
        public String horaProc;
        public String reqAprob;
        public Object fecAprob;
        public Object horaAprob;
        public String usrAprob;
        public Object fecRechazo;
        public Object horaRechazo;
        public String usrRechazo;
    }


    public class RespuestaPaginado {
        public int cantRegMostrar;
        public int totalReg;
        public int nroConsulta;
        public String masReg;
        public int nroPagina;
        public int maxReg;
    }

    public static LotesPAP get(ContextoOB contextoOB, int nroAdh, int nroConv, int nroSubConv) {
        ApiRequest apiRequest = new ApiRequest("Api-Empresas_archivosRecibidosOP", "empresas", "GET", "/v1/sat/archivosRecibidosOP", contextoOB);

        apiRequest.query("cantRegMostrar", 100);
        apiRequest.query("codigoEntidad", 044);
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());
        apiRequest.query("fechaHoraConsulta", dateString);
        apiRequest.query("frontEnd", 2);
        apiRequest.query("frontEndID", "OB");
        apiRequest.query("id", contextoOB.sesion().idCobis);
        apiRequest.query("idUsrLog", contextoOB.sesion().idCobis);
        apiRequest.query("maxReg", 100);
        apiRequest.query("nroConsulta", 0);
        apiRequest.query("nroPagina", 1);
        apiRequest.query("tipoID", 1);
        apiRequest.query("nroAdh", nroAdh);
        apiRequest.query("nroConv", nroConv);
        apiRequest.query("nroSubConv", nroSubConv);
        ApiResponse apiResponse = apiRequest.ejecutar();
        LotesPAP lotesPAP = apiResponse.crear(LotesPAP.class);
        ApiException.throwIf(!apiResponse.http(200), apiRequest, apiResponse);

        return lotesPAP;
    }


}
