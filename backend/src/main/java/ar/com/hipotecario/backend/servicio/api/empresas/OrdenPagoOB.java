package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class OrdenPagoOB extends ApiObjeto {

    /* ========== ATRIBUTOS ========== */
    @SerializedName("ordenesDePago")
    public OrdenesPagos ordenesPagos;

    public static class OrdenesPagos {
        @SerializedName("ordenDePago")
        public ArrayList<OrdenPago> subconvenio;
    }

    public static class OrdenPago implements Comparable<OrdenPagoOB.OrdenPago> {
        public String admiteRevStop;
        public String admiteStop;
        public String altFormEnt;
        public String altPedAvisBen;
        public String cbuCtaADebitar;
        public ArrayList<Cheque> cheque;
        public int codEstOrden;
        public int codEstadoAnt;
        public String compAdj;
        public String compIng;
        public String cuitEmp;
        public String descAltFormEnt;
        public String descAltPedAvisBen;
        public String descCatMedioEjec;
        public String descCodEstOrden;
        public String descCodEstadoAnt;
        public String descMedioEjec;
        public String descMonCtaADebitar;
        public String descMonPago;
        public String descNroAdh;
        public String descNroSubConv;
        public String descRechBcoDest;
        public String descSucDest;
        public String descTipDocBeneficiario;
        public String descTipoCtaADebitar;
        public String descTipoOper;
        public String emailBeneficiario;
        public String faltaFondos;
        public String fecCambioEst;
        public String fecEjec;
        public String fecEjecOrden;
        public String fecGestFin;
        public String fecRechBcoDest;
        public String formatoCtaADebitar;
        public String horaEjec;
        public String horaGestFin;
        public int idLotcatMedioEjece;
        public int idLote;
        public String impOrden;
        public String indicBeneficiario;
        public String insCentralizadora;
        public String medioEjec;
        public int monCtaADebitar;
        public int monPago;
        public String motivoPago;
        public String msjBeneficiario;
        public String nomBeneficiario;
        public String nomConc;
        public int nroAdh;
        public String nroBeneficiarioSist;
        public String nroConc;
        public int nroConv;
        public String nroCuilCuitBeneficiario;
        public String nroInstPago;
        public int nroOrden;
        public int nroSubConv;
        public String ordenDebitada;
        public String rechBcoDest;
        public String refCliente;
        public String simboloMonCtaADebitar;
        public String simboloMonPago;
        public String stopAplicado;
        public int subNroOrden;
        public String sucDest;
        public int tipDocBeneficiario;
        public String tipoCtaADebitar;
        public String tipoCuentaDebito;
        public int tipoOper;
        public Transferencia transferencia;
        private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        @Override
        public int compareTo(OrdenPago other) {
            try {
                LocalDateTime thisDateTime = parseDateTime(this);
                LocalDateTime otherDateTime = parseDateTime(other);
                return otherDateTime.compareTo(thisDateTime); // Orden descendente
            } catch (Exception e) {
                System.out.println(e);
                return -1;
            }
        }
        private LocalDateTime parseDateTime(OrdenPago orden) throws Exception {
            String fechaValida = obtenerFechaValida(orden);
            String horaValida = obtenerHoraValida(orden);
            if (horaValida != null) {
                return LocalDateTime.parse(fechaValida + " " + horaValida, dateTimeFormatter);
            } else {
                return LocalDate.parse(fechaValida, dateFormatter).atStartOfDay();
            }
        }
        private String obtenerHoraValida(OrdenPago orden) throws Exception {
            if (orden.horaEjec != null&&!orden.horaEjec.isBlank()) {
                return orden.horaEjec;
            } else if (orden.horaGestFin != null&&!orden.horaGestFin.isBlank()) {
                return orden.horaGestFin;
            } else {
           return null;
            }
        }
        private String obtenerFechaValida(OrdenPago orden) throws Exception {
            if (orden.fecEjec!=null&&!orden.fecEjec.isBlank()){
                return orden.fecEjec;
            } else if (orden.fecEjecOrden!=null&&!orden.fecEjecOrden.isBlank()){
                return orden.fecEjecOrden;
            } else if (orden.fecGestFin!=null&&!orden.fecGestFin.isBlank()){
                return orden.fecGestFin;
            } else {
                throw new Exception("No hay fechas");
            }
        }
    }

    public static class Cheque {
        public String aceptaNegCheques;
        public int altFmaEntChq;
        public int catMedioEjec;
        public String chqNegoc;
        public int codEstado;
        public int codEstadoAnt;
        public int codSeguridad;
        public String consMontoParaNeg;
        public String consPlazoParaNeg;
        public String cruzarCheque;
        public String denomSucDest;
        public String denomSucImp;
        public String denomSucOrig;
        public String descAltFmaEntChq;
        public String descCatMedioEjec;
        public String descCodEstado;
        public String descCodEstadoAnt;
        public String descEsqFirmas;
        public String descTipoOperHab;
        public String descUbicFisica;
        public String descUbicFisicaAntChq;
        public String entregaCheqCodSeg;
        public int esqFirmas;
        public String fecAlta;
        public String fecDispChq;
        public String fecEmiSolic;
        public String fecEntrega;
        public String fecEnvDestruccion;
        public String fecImpOReImpChq;
        public String fecImpOReImpChqAnt;
        public String fecPagoRechazo;
        public String fecRecepSuc;
        public String fecUltAct;
        public String fecUltAviso;
        public String fecVencimientoVig;
        public String fechaVencimiento;
        public String imprimCruzado;
        public String imprimeValor;
        public String incFirma;
        public String leyenda;
        public int montoMaxNeg;
        public int montoMinNeg;
        public String negConRecurso;
        public String nomApellido;
        public String nomBeneficiario;
        public int nroCheque;
        public int plazoFinanc;
        public int plazoMaxNeg;
        public int plazoMinNeg;
        public int plzMaxDifCPD;
        public String reqCheqCruzado;
        public String reqCheqNoALaOrden;
        public String reqEntregaCheqVencido;
        public String reqRecibo;
        public String reqSalvadoCheqVencido;
        public String retiraBeneficiario;
        public String serCheque;
        public String sucDest;
        public String sucDestOrig;
        public String sucImp;
        public int tasaDesc;
        public String tipoCheque;
        public String tipoNroDoc;
        public int tipoOperHab;
        public int tolCheqFechaPrevia;
        public int ubicFisica;
        public int ubicFisicaAntChq;
    }

    public static class Transferencia {
        public int catMedioEjec;
        public String cbuCtaAcred;
        public String desNroSuc;
        public String descBco;
        public String descCatMedioEjec;
        public String descMonCtaAcred;
        public String descTipoCta;
        public String descTipoOperHab;
        public String descTransElecionCanal;
        public String fecAlta;
        public String fecUltAct;
        public String formatoCtaAcred;
        public int monCtaAcred;
        public String nomCuentaAcred;
        public int nroBco;
        public int nroSuc;
        public String simboloMonCtaAcred;
        public int tipoCtaAcred;
        public String tipoCuentaCredito;
        public int tipoOperHab;
        public int transElecionCanal;
    }

    /* ========== SERVICIOS ========== */
    // API-Empresas_ConsultaOrdenesPago
    public static OrdenPagoOB get(ContextoOB contexto, Integer nroConv, Integer nroSubConv, String nroAdh, Integer codEstado, Fecha fechaDesde, Fecha fechaHasta,int tipoOper,String numeroPagina,int cantRegistros) throws UnsupportedEncodingException {

        ApiRequest request = new ApiRequest("API-Empresas_ConsultaOrdenesPago", "empresas", "GET", "/v1/sat/ordenesdePagos", contexto);
        request.header("Content-Type", "application/json");
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());

        LocalDate localFechaDesde = null;
        LocalDate localFechaHasta = null;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = LocalDate.parse(fechaDesde.toString(), formato);
            localFechaHasta = LocalDate.parse(fechaHasta.toString(), formato);
        }

        request.query("cantRegMostrar", cantRegistros);
        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID", "OB");
        request.query("id", contexto.sesion().empresaOB.idCobis + "-" + Config.desencriptarAES(contexto.sesion().usuarioOB.login));
        request.query("idUsrLog", contexto.sesion().empresaOB.idCobis);
        request.query("maxReg", cantRegistros);
        request.query("nroPagina", numeroPagina);
        request.query("tipoID", "1");
        request.query("nroConv", nroConv);
//        request.query("nroConsulta", nroConsulta);
        request.query("nroSubConv", nroSubConv);
        request.query("nroAdh", nroAdh);
        request.query("tipoOper", "1");
        request.query("codEstOrden", codEstado);
        request.query("fecEjecOrdenD", localFechaDesde.toString());
        request.query("fecEjecOrdenH", localFechaHasta.toString());
        if (tipoOper!=0) request.query("tipoOper",tipoOper);

        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(OrdenPagoOB.class);
    }
    public static OrdenPagoOB get(ContextoOB contexto, Integer nroConv, Integer nroSubConv, String nroAdh, String codEstado, Fecha fechaDesde, Fecha fechaHasta,String nroOrden, int tipoOper) throws UnsupportedEncodingException {

        ApiRequest request = new ApiRequest("API-Empresas_ConsultaOrdenesPago", "empresas", "GET", "/v1/sat/ordenesdePagos", contexto);
        request.header("Content-Type", "application/json");
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());

        LocalDate localFechaDesde = null;
        LocalDate localFechaHasta = null;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = LocalDate.parse(fechaDesde.toString(), formato);
            localFechaHasta = LocalDate.parse(fechaHasta.toString(), formato);
        }

        request.query("cantRegMostrar", "500");
        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID", "OB");
        request.query("id", contexto.sesion().empresaOB.idCobis + "-" + Config.desencriptarAES(contexto.sesion().usuarioOB.login));
        request.query("idUsrLog", contexto.sesion().empresaOB.idCobis);
        request.query("maxReg", "500");
        request.query("nroPagina", "1");
        request.query("tipoID", "1");
        request.query("nroConv", nroConv);
        request.query("nroSubConv", nroSubConv);
        request.query("nroAdh", nroAdh);
        request.query("tipoOper", tipoOper);
        request.query("codEstOrden", codEstado);
        request.query("fecEjecOrdenD", localFechaDesde.toString());
        request.query("fecEjecOrdenH", localFechaHasta.toString());
        request.query("nroOrden", nroOrden);

        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(OrdenPagoOB.class);
    }
}
