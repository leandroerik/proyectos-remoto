package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.ErroresMigracionTransmitEnum;
import ar.com.hipotecario.backend.servicio.api.transmit.EstadoMigradoEnum;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaRequest;
import ar.com.hipotecario.backend.servicio.api.transmit.OpcionMigradoEnum;
import ar.com.hipotecario.backend.util.Errores;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.BankProcess;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.lib.Util;

import java.util.Date;

public class TransmitMB extends Transmit {

    public static final String REASON_TRANSFERENCIA = "transferencia";
    public static final String REASON_VENTA_DOLARES = "ventaDolares";
    public static final String REASON_DEBIN = "debin";
    public static final String REASON_RESCATE = "rescate";
    public static final String REASON_VENTA_BONOS = "bonos";
    public static final String REASON_VENTA_ACCIONES = "acciones";
    public static final String CANAL = "MB";

    public static boolean isChallengeOtp(ContextoMB contexto, String funcionalidad) {
        return contexto.sesion().isChallengeOtp(funcionalidad);
    }

    public static RespuestaMB recomendacionTransmit(ContextoMB contexto, BankProcess bankProcess, String funcionalidad) {
        try {
            loguearInfo("recomendacionTransmit", "Inicio");

            AuditLogReport auditLogReport = new AuditLogReport(
                    "MB",
                    "MOBILE",
                    contexto.idSesion() == null ? "" : contexto.idSesion(),
                    Util.idProceso(),
                    "",
                    ConfigMB.string("api_url_auditor", ""),
                    contexto.ip() == null ? "" : contexto.ip());

            RecommendationDTO recommendationDTO = recomendacion(contexto, bankProcess, auditLogReport).tryGet();

            if (recommendationDTO != null) {
                loguearInfo("recomendacionTransmit", recommendationDTO.getRecommendationType() + "- " + recommendationDTO.getRiskScore());
                loguearInfo("recomendacionTransmit", "Fin");
                switch (recommendationDTO.getRecommendationType()) {
                    case Transmit.DENY:
                        contexto.limpiarSegundoFactor();
                        // return RespuestaMB.estado(Transmit.getErrorDeny());
                        contexto.sesion().setChallengeOtp(true);
                        contexto.sesion().setFuncionalidadChallengeOtp(funcionalidad);
                        return RespuestaMB.estado(Transmit.getErrorChallenge());
                    case Transmit.CHALLENGE:
                        contexto.limpiarSegundoFactor();
                        if (funcionalidad.equals("registrar-dispositivo") || funcionalidad.equals("alta-soft-token"))
                            return RespuestaMB.estado(Transmit.getErrorDeny());
                        contexto.sesion().setChallengeOtp(true);
                        contexto.sesion().setFuncionalidadChallengeOtp(funcionalidad);
                        return RespuestaMB.estado(Transmit.getErrorChallenge());
                    default:
                        return RespuestaMB.exito();
                }
            }
            loguearError("recomendacionTransmit", "recomendacion NULL");
            return RespuestaMB.exito();
        } catch (Exception e) {
            loguearError("recomendacionTransmit", "error al tener la recomendacion");
            return RespuestaMB.exito();
        }
    }

    public static boolean validarCsmTransaccion(ContextoMB contexto, JourneyTransmitEnum journeyTransmitEnum) {
        UsuarioLibreriaRequest usuarioLibreriaRequest = new UsuarioLibreriaRequest(
                "",
                "",
                contexto.idCobis(),
                "",
                "",
                "MB",
                "MOBILE",
                contexto.persona().cuit(),
                contexto.ip(),
                journeyTransmitEnum,
                contexto.sesion().id(),
                contexto.idCobis(),
                contexto.parametros.string("csmId"),
                contexto.parametros.string("checksum"),
                false
        );
        return validarCsm(contexto, usuarioLibreriaRequest);
    }

    private static MigracionUsuario.ResponseMigracionUsuario obtenerEstadoMigracion(ContextoMB contexto, int numeroDocumento) {
        MigracionUsuario.ResponseMigracionUsuario response = ApiSeguridad.gestionarMigracion(contexto, new MigracionUsuario.RequestMigracionUsuario(numeroDocumento, 0, OpcionMigradoEnum.CONSULTAR, contexto.idCobis(), "", "")).tryGet();
        if (Objeto.empty(response) || (!Objeto.empty(response) && response.codRet == -1))
            return null;
        return response;
    }

    public static RespuestaMB usuarioParaMigrar(ContextoMB contexto, String documento) {
        MigracionUsuario.ResponseMigracionUsuario estadoMigradoResponse = obtenerEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(documento));
        if (estadoMigradoResponse == null || !EstadoMigradoEnum.codigo(estadoMigradoResponse.migrado).esParaMigrar()){
            new Futuro<>(() -> actualizarErrorMigracion(contexto, contexto.parametros.integer("documento"), ErroresMigracionTransmitEnum.ERROR_LIBRERIA_OBTENER_ESTADO_MIGRACION));
            return RespuestaMB.estado(Errores.ERROR_EN_BUSCAR_ESTADO_MIGRACION);}
        return null;
    }

    public static boolean actualizarErrorMigracion(ContextoMB contexto, int numeroDocumento, ErroresMigracionTransmitEnum errorMigracionTransmitEnum) {
        MigracionUsuario.ResponseMigracionUsuario response =
        ApiSeguridad.gestionarMigracion(contexto,
                new MigracionUsuario.RequestMigracionUsuario(
                        numeroDocumento,
                        Integer.parseInt(EstadoMigradoEnum.MIGRAR.getCodigo()),
                        OpcionMigradoEnum.INSERTAR_ERROR,
                        contexto.idCobis(),
                        errorMigracionTransmitEnum.name(),
                        ar.com.hipotecario.backend.base.Util.date(new Date(), "yyyy-MM-dd"))
                ).tryGet();

        return !Objeto.empty(response) && (Objeto.empty(response) || response.codRet != -1);
    }
}
