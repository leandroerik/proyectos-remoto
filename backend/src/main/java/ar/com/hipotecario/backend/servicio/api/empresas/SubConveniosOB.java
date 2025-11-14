package ar.com.hipotecario.backend.servicio.api.empresas;

import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;


import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import ar.com.hipotecario.canal.officebanking.ContextoOB;

import ar.com.hipotecario.canal.officebanking.SesionOB;
import com.google.gson.annotations.SerializedName;

public class SubConveniosOB extends ApiObjeto {


    /* ========== ATRIBUTOS ========== */
    @SerializedName("subconvenios")
    public Subconvenios subconvenios;

    @SerializedName("respuestaPaginado")
    public RespuestaPaginado respuestaPaginado;

    public static class Subconvenios {
        @SerializedName("subconvenio")
        public ArrayList<SubConvenio> subconvenio;
    }
    public static class SubConvenio {
        public int tipDoc;
        public String descTipDoc;
        public long nroDoc;
        public String nroCliente;
        public int nroAdh;
        public int nroConv;
        public int nroSubConv;
        public String descSubConv;
        public String fecVig;
        public String fecVto;
        public int monOperatoria;
        public String descMonOperatoria;
        public int tipoCuentaPagos;
        public String descTipoCtaPagos;
        public String cbuCuentaPagos;
        public String strCtaFormatoPagos;
        public int tipoCuentaComisiones;
        public String descTipoCtaCom;
        public String cbuCuentaComisiones;
        public String strCtaFormatoComisiones;
        public int tipoCuentaOperativa;
        public String descTipoCtaOper;
        public String cbuCuentaOperativa;
        public String strCtaFormatoOperativa;
        public int utilCuentas;
        public String descUtilCuentas;
        public String reqEnvArchSinInfo;
        public String email;
        public String reqChekSum;
        public int nroChekSum;
        public int envioCompxmail;
        public String descEnvioCompxmail;
        public int codPaqComis;
        public String descCodPaqComis;
        public String fecProxFacturacion;
        public String fecUltiFacturacion;
        public int maxAnticLotPag;
        public int forArch;
        public String descForArch;
        public int estado;
        public String desEstado;
        public String habilitado;
        public String fecBajaLog;
        public String fecAlta;
        public String usrAlta;
        public String fecAutorizCreacion;
        public String usrAutorizCreacion;
        public String fecUltcambio;
        public String usrUltCambio;
        public String fecAutorizUltCambio;
        public String usrAutorizUltCambio;
        public Transferencia transferencia;
        public ArrayList<Cheque> cheques;
    }

    public static class Cheque {
        public int tipoOperHab;
        public String descTipoOperHab;
        public int catMedioEjec;
        public String descCatMedioEjec;
        public int plzMaxDifCPD;
        public String reqSalvadoCheqVencido;
        public String reqEntregaCheqVencido;
        public int tolCheqFechaPrevia;
        public int esqFirmas;
        public String descEsqFirmas;
        public String entregaCheqCodSeg;
        public String aceptaNegCheques;
        public String consMontoParaNeg;
        public Double montoMinNeg;
        public Double montoMaxNeg;
        public String consPlazoParaNeg;
        public int plazoMinNeg;
        public int plazoMaxNeg;
        public String negConRecurso;
        public String reqCheqCruzado;
        public String reqCheqNoALaOrden;
        public String fecAlta;
        public String fecUltAct;
    }

    public static class Transferencia {
        public int tipoOperHab;
        public String descTipoOperHab;
        public int catMedioEjec;
        public String descCatMedioEjec;
        public int transElecionCanal;
        public String descTransElecionCanal;
        public String fecAlta;
        public String fecUltAct;
    }
    public static class RespuestaPaginado {
        public int cantRegMostrar;
        public int totalReg;
        public int nroConsulta;
        public String masReg;
        public int nroPagina;
        public int maxReg;
    }


    /* ========== SERVICIOS ========== */
    // API-Empresas_ConsultaDeGruposEconómicosDeUnaEmpresa
    public static SubConveniosOB get(ContextoOB contexto) throws UnsupportedEncodingException {

        ApiRequest request = new ApiRequest("API-Empresas_ConsultaSubconvenios", "empresas", "GET", "/v1/sat/subconvenios", contexto);
        request.header("Content-Type","application/json");
        SesionOB sesion = contexto.sesion();
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());

        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID","OB");
        request.query("id", contexto.sesion().empresaOB.idCobis);
        request.query("idUsrLog", contexto.sesion().empresaOB.idCobis);
        request.query("nroCliente", contexto.sesion().empresaOB.idCobis);
        request.query("tipoID", "1");
        request.query("nroConsulta", "1");
        request.query("nroPagina", "1");
        request.query("cantRegMostrar", "100");
        request.query("maxReg", "100");
        request.query("nroDoc",String.valueOf(sesion.empresaOB.cuit));

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204, 404), request, response);
        return response.crear(SubConveniosOB.class);
    }


}