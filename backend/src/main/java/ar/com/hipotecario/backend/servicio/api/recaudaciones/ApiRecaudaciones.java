package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.RelacionEcheqConvenioOB.RelacionEcheqConvenio;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

//http://api-recaudaciones-microservicios-homo.appd.bh.com.ar/swagger-ui.html#
public class ApiRecaudaciones extends Api {

    /* ========== Recaudaciones Controller ========== */

    // GET /v1/convenios
    public static Futuro<ConveniosOB> convenios(Contexto contexto, String cuit, String operacion) {
        return futuro(() -> ConveniosOB.get(contexto, cuit, operacion));
    }

    // GET /v1/convenios/{idCobis}
    public static Futuro<ConveniosOB> convenio(Contexto contexto, Integer convenio) {
        return futuro(() -> ConveniosOB.getConvenio(contexto, convenio));
    }

    // GET /v1/convenioRecaudaciones
    public static Futuro<ConveniosRecaOB> convenioRecaudaciones(Contexto contexto, Integer idCobis) {
        return futuro(() -> ConveniosRecaOB.get(contexto, idCobis));
    }

    // GET /v1/plansueldo/lotes
    public static Futuro<LotesOB> consultaLotes(Contexto contexto, String canal, String convenio, String fechadesde, String fechahasta) {
        return futuro(() -> LotesOB.get(contexto, canal, convenio, fechadesde, fechahasta));
    }

    // GET /v1/plansueldo/detalle
    public static Futuro<DetalleLotesOB> consultaDetalleLotes(ContextoOB contexto, String numeroLote, String convenio) {
        return futuro(() -> DetalleLotesOB.get(contexto, numeroLote, convenio));
    }
    public static Futuro<DetalleLotesOB> consultaDetalleLotes(ContextoOB contexto, String numeroLote, String convenio,String secuencial,Integer idProceso) {
        return futuro(() -> DetalleLotesOB.get(contexto, numeroLote, convenio,secuencial,idProceso));
    }

    // PATCH /vi/convenios/{convenio}/recaudacion/habilitacion
    public static Futuro<DetalleConveniosHabilitacionOB.DetalleConveniosHabilitacion> habilitarConveniosRecaudaciones(ContextoOB contexto, int convenio, String echeq, String transf, String debin, String tipoConsulta) {
        return futuro(() -> DetalleConveniosHabilitacionOB.patch(contexto, convenio, echeq, transf, debin, tipoConsulta));
    }

    public static Futuro<DetalleEstadoHabilitacionConveniosOB> detalleEstadoHabilitacionConvenios(ContextoOB contexto, int convenio, String medioRecaudacion) {
        return futuro(() -> DetalleEstadoHabilitacionConveniosOB.get(contexto, convenio, medioRecaudacion));
    }
    
    public static Futuro<DetalleEstadoHabilitacionConveniosOB> detalleEstadoHabilitacionTodosConvenios(ContextoOB contexto, String medioRecaudacion) {
        return futuro(() -> DetalleEstadoHabilitacionConveniosOB.getTodosConvenios(contexto, medioRecaudacion));
    }

    public static Futuro<ReporteCobranzasOB> repCobranzas(ContextoOB contexto, String estadoCheque, String fechaFin, String FechaInicio, String idConvenio, Integer tipoPago,int secuencial,Integer idProceso) {
        return Util.futuro(() -> ReporteCobranzasOB.get(contexto, estadoCheque, fechaFin, FechaInicio, idConvenio, tipoPago,secuencial,idProceso));
    }

    // GET /v1/convenio/{convenio}/rutas
    public static Futuro<RutasOB> consultaRutas(ContextoOB contexto, String convenio, String
            codigoGrupoRecaudacion, String servicioGrupoRecaudacion) {
        return futuro(() -> RutasOB.get(contexto, convenio, codigoGrupoRecaudacion, servicioGrupoRecaudacion));
    }
    
    public static Futuro<ConveniosSugeridosEcheqOB> consultaConveniosSugeridos(ContextoOB contexto, String convenio, String tipo, String numeroCheque, String estado, String razonSocial, String fechaDesde, String fechaHasta, String pagina, String limite) {
        return futuro(() -> ConveniosSugeridosEcheqOB.get(contexto, convenio, tipo, numeroCheque, estado, razonSocial, fechaDesde, fechaHasta, pagina, limite));
    }
    
    public static Futuro<Object> relacionarEcheqConConvenio(ContextoOB contexto, String idCheque, String convenio, String tipo, String cuit, String nombre) {
        return futuro(() -> ConveniosOB.relacionarEcheqConConvenio(contexto, idCheque,  convenio,  tipo,  cuit,  nombre));
    }
    
    // POST /v1/convenios/echeqs/depositocustodia
    public static Futuro<RelacionEcheqConvenioOB> relacionarEcheqConvenio(ContextoOB contexto, String idEcheq, int codConv, String formaPago, Long cuitCliente, String nomLar) {
        return futuro(() -> RelacionEcheqConvenioOB.post( contexto, codConv, idEcheq, formaPago, cuitCliente, nomLar));
    }

}


