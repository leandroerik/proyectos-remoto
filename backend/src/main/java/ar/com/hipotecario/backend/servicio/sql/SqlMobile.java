package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.mobile.RegistroDispositivoMobile;
import ar.com.hipotecario.backend.servicio.sql.mobile.RegistroDispositivoMobile.RegistroDispositivo;
import ar.com.hipotecario.backend.servicio.sql.mobile.SoftTokenMobile;
import ar.com.hipotecario.backend.servicio.sql.mobile.SoftTokenMobile.SoftToken;

public class SqlMobile extends Sql {

    /* ========== REGISTRO DISPOSITIVO ========== */
    public static Futuro<Boolean> deshabilitarRegistroDispositivo(Contexto contexto, String idDispositivo, String id) {
        return futuro(() -> RegistroDispositivoMobile.deshabilitarRegistroDispositivo(contexto, idDispositivo, id));
    }

    public static Futuro<RegistroDispositivo> obtenerUltimoRegistroPorCobis(Contexto contexto, String idCobis) {
        return futuro(() -> RegistroDispositivoMobile.obtenerUltimoRegistroPorCobis(contexto, idCobis));
    }

    /* ========== SOFT TOKEN ========== */
    public static Futuro<Boolean> deshabilitarSoftToken(Contexto contexto, String id) {
        return futuro(() -> SoftTokenMobile.deshabilitarSoftToken(contexto, id));
    }

    public static Futuro<SoftToken> obtenerUltimaAltaSoftTokenActiva(Contexto contexto, String idCobis) {
        return futuro(() -> SoftTokenMobile.obtenerUltimaAltaSoftTokenActiva(contexto, idCobis));
    }

    /* ========== SERVICIOS ========== */
    public static Futuro<Boolean> guardarRegistroDispositivo(Contexto contexto, String cobis, String idDispositivo, String alias) {
        return futuro(() -> RegistroDispositivoMobile.insertRegistroDispositivo(contexto, cobis, idDispositivo, alias));
    }
}
