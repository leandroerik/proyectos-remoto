package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.homebanking.*;
import ar.com.hipotecario.backend.servicio.sql.homebanking.LogsMonitor.LogMonitor;
import ar.com.hipotecario.backend.servicio.sql.homebanking.MuestreoHB.Muestreo;
import ar.com.hipotecario.backend.servicio.sql.homebanking.SessionHB.Session;
import ar.com.hipotecario.backend.servicio.sql.homebanking.UltimasConexionesHB.UltimaConexionHB;
import ar.com.hipotecario.backend.servicio.sql.homebanking.AccesoBiometriaHB.AccesoBiometria;

public class SqlHomeBanking extends Sql {

    /* ========== SERVICIOS ========== */
    public static Futuro<Boolean> registrarLogMonitor(Contexto contexto, LogMonitor logMonitor) {
        return futuro(() -> LogsMonitor.post(contexto, logMonitor));
    }

    public static Futuro<UltimaConexionHB> ultimaConexion(Contexto contexto, String idCobis) {
        return futuro(() -> UltimasConexionesHB.select(contexto, idCobis));
    }

    public static Futuro<Boolean> actualizaUltimaConexion(Contexto contexto, String idCobis) {
        return futuro(() -> UltimasConexionesHB.update(contexto, idCobis));
    }

    public static Futuro<Muestreo> bloqueadoFraude(Contexto contexto, String idCobis) {
        return futuro(() -> MuestreoHB.select(contexto, idCobis, "deshabilitar.acceso.fraude", "true"));
    }

    public static Futuro<Boolean> bloquearPorFraude(Contexto contexto, String idCobis) {
        return futuro(() -> MuestreoHB.insert(contexto, idCobis, "deshabilitar.acceso.fraude", "true"));
    }

    public static Futuro<Session> sesionExistente(Contexto contexto, String idCobis, String fingerprint) {
        return futuro(() -> SessionHB.select(contexto, idCobis, fingerprint));
    }

    public static Futuro<Boolean> registrarSesion(Contexto contexto, String idCobis, String fingerprint) {
        return futuro(() -> SessionHB.update(contexto, idCobis, fingerprint));
    }

    public static Futuro<Boolean> eliminarSesion(Contexto contexto, String idCobis) {
        return futuro(() -> SessionHB.delete(contexto, idCobis));
    }

    public static Futuro<Boolean> registrarLogLogin(Contexto contexto, String idCobis) {
        return futuro(() -> LogsLoginHB.insert(contexto, idCobis));
    }

    public static Futuro<Boolean> registrarLogOtp(Contexto contexto, String idCobis, String celular, String email, Boolean riesgoNet, Boolean link, String estado) {
        return futuro(() -> LogsEnvioOtpHB.insert(contexto, idCobis, celular, email, riesgoNet, link, estado));
    }

    /* ========== ACCESOS BIOMETRIA ========== */
    public static Futuro<AccesoBiometria> obtenerAccesosBiometria(Contexto contexto, String idCobis, String dispositivo) {
        return futuro(() -> AccesoBiometriaHB.obtenerAccesosBiometria(contexto, idCobis, dispositivo));
    }

    public static Futuro<Boolean> borrarAccesosBiometria(Contexto contexto, String idCobis) {
        return futuro(() -> AccesoBiometriaHB.borrarAccesosBiometria(contexto, idCobis));
    }
}
