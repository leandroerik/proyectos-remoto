package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

// http://api-empresas-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiEmpresas {

    /* ========== Sat Controller ========== */

    // TODO: GET /v1/sat/archivosEnviadosPagoProveedores
    public static Futuro<ArchivosEnviados> archivosEnviadosPagoProveedores(ContextoOB contextoOB, int nroAdh, int nroConv, int nroSubConv,String fechaD,String fechaH,String tipoArch) {
        return Util.futuro(() -> ArchivosEnviados.get(contextoOB,  nroAdh,  nroConv,  nroSubConv, fechaD, fechaH, tipoArch));
    }

    // GET /v1/sat/archivosRecibidosOP
    public static Futuro<LotesPAP> archivosRecibidosOP(ContextoOB contextoOB, int nroAdh, int nroConv, int nroSubConv) {
        return Util.futuro(() -> LotesPAP.get(contextoOB, nroAdh, nroConv, nroSubConv));
    }

    // TODO: GET /v1/sat/autorizadosAlRetiro

    // TODO: GET /v1/sat/beneficiariosPP
    public static Futuro<BeneficiariosOB> beneficiarios(ContextoOB contexto, Integer nroConv, Integer nroSubConv) {
        return Util.futuro(() -> BeneficiariosOB.get(contexto, nroConv, nroSubConv));
    }

    // TODO: GET /v1/sat/descargaArchivosEnviadosPagoProveedores

    public static Futuro<ArchivoEnviado> descargaArchivosEnviadosPagoProveedores(ContextoOB contextoOB,  int idLote) {
        return Util.futuro(() -> ArchivoEnviado.get(contextoOB,  idLote));
    }


    // TODO: GET /v1/sat/erroresArchivosRecibidosOP


    public static Futuro<ErrorOrdenPagoOB> errorOrdenPago(ContextoOB contexto, int nroOrden,int nroSubOrden){
        return Util.futuro(()->ErrorOrdenPagoOB.get(contexto,nroOrden,nroSubOrden));
    }

    // GET /v1/sat/ordenesdePagos
    public static Futuro<OrdenPagoOB> ordenesPagos(ContextoOB contexto, Integer nroConv, Integer nroSubConv, String nroAdh, Integer codEstado, Fecha fechaDesde, Fecha fechaHasta,int tipoOper,String numeroPagina,int cantRegistros) {
        return Util.futuro(() -> OrdenPagoOB.get(contexto, nroConv, nroSubConv, nroAdh, codEstado, fechaDesde, fechaHasta,tipoOper,numeroPagina,cantRegistros));
    }

    public static Futuro<OrdenPagoOB> ordenesPagos(ContextoOB contexto, Integer nroConv, Integer nroSubConv, String nroAdh, String codEstado, Fecha fechaDesde, Fecha fechaHasta, String nroOrden,int tipoOper) {
        return Util.futuro(() -> OrdenPagoOB.get(contexto, nroConv, nroSubConv, nroAdh, codEstado, fechaDesde, fechaHasta, nroOrden,tipoOper));
    }

    // GET /v1/sat/subconvenios
    public static Futuro<SubConveniosOB> subconvenios(ContextoOB contexto) {
        return Util.futuro(() -> SubConveniosOB.get(contexto));
    }

    // TODO: GET /v1/sat/tipoOperatoriaYMedioEjecucion

    // /v1/sat/validaIntegridadArchivo
    public static ValidaintegridadArchivoOB validaIntegridadArchivo(ContextoOB contexto,String nombreArchivo,String convenio,String subconvenio) {
        return ValidaintegridadArchivoOB.get(contexto, nombreArchivo, convenio,subconvenio);
        
    }


    /* ========== Cuentas Controller ========== */

    // TODO: GET /v1/cuentas/deudas

    /* ========== Deuda Controller ========== */

    // TODO: @deprecated GET
    // /v1/factoring/{idCliente}/consultacomposiciondeudafactoring

    // TODO: GET /v2/factoring/{idCliente}/composiciondeuda

    /* ========== Empresa Controller ========== */

    // GET /v1/empresas/{emp_cuit}/{usu_cuit}
    public static Futuro<EmpresaOB> empresaOB(Contexto contexto, String cuit, String cuil) {
        return Util.futuro(() -> EmpresaOB.get(contexto, cuit, cuil));
    }

    // GET /v1/empresas/consultaPreConsorcios
    public static Futuro<ConsultaPreConsorciosResponse> consultaPreConsorcios(Contexto contexto, String cuenta, String cuit, String idCobis) {
        return Util.futuro(() -> ConsultaPreConsorciosResponse.get(contexto, cuenta, cuit, idCobis));
    }

    // TODO: GET /v1/empresas/{id}

    // TODO: GET /v1/empresas/{id}/acuerdos

    // TODO: @deprecated GET /v1/empresas/{id}/calificacion-crediticia

    // TODO: POST /v1/empresas/{id}/calificacioncrediticia

    // TODO: POST /v1/empresas/{id}/calificacionriesgo

    // GET /v1/empresas/{id}/grupos
    public static Futuro<GrupoEconomicoOB> grupoEconomico(Contexto contexto, String idEmpresa) {
        return Util.futuro(() -> GrupoEconomicoOB.get(contexto, idEmpresa));
    }

    // GET /v1/empresas/{id}/posicionconsolidada
    public static Futuro<PosicionConsolidadaOB> posicionConsolidada(Contexto contexto, String idEmpresa) {
        return Util.futuro(() -> PosicionConsolidadaOB.get(contexto, idEmpresa));
    }

    // GET /esquemas/{cedruc}/completafirma
    public static Futuro<CompletaFirmaOB> completaFirma(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB) {
        return Util.futuro(() -> CompletaFirmaOB.get(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB, null));
    }

    public static Futuro<CompletaFirmaOB> completaFirma(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB, String moneda) {
        return Util.futuro(() -> CompletaFirmaOB.get(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB, moneda));
    }

    // POST /v1/empresas/capita
    public static Futuro<AltaConsultaCapitaOB> altaConsultaNomina(Contexto contexto, AltaConsultaCapitaRequest request) {
        return Util.futuro(() -> AltaConsultaCapitaOB.get(contexto, request));
    }
    
    //POST /v1/comercios Crear comercio
    public static Futuro<CrearComercio> crearComercio(Contexto contexto, CrearComercioRequest request) {
        return Util.futuro(() -> CrearComercio.get(contexto, request));
    }

    public static Futuro<StopPaymentPapOB> stopPayment(ContextoOB contexto, int nroOrden, int subNroOrden) {
        return Util.futuro(()->StopPaymentPapOB.post(contexto,nroOrden,subNroOrden));
    }

    // TODO: GET /v1/empresas/deudas

    // TODO: GET /v1/empresas/token

    // TODO: GET /v2/empresas/{id}/calificacionescrediticias

    /* ========== Transaccional Controller ========== */

    // TODO: POST /v1/creditos

    // TODO: POST /v1/debitos


}
