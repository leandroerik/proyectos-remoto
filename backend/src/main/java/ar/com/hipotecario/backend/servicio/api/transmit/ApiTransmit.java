package ar.com.hipotecario.backend.servicio.api.transmit;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.exception.TransmitException;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaRequest;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaResponse;

public class ApiTransmit extends Api {

    /* ========== HB ========== */

    public static Futuro<UsuarioLibreriaResponse> migrarUsuarioHB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        return futuro(() -> LibreriaFraudes.migrarUsuarioHB(contexto, usuarioLibreriaRequest));
    }

    public static Futuro<UsuarioLibreriaResponse> validarCsmHB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        return futuro(() -> LibreriaFraudes.validarCsmHB(contexto, usuarioLibreriaRequest));
    }

    /* ========== MB ========== */

    public static Futuro<UsuarioLibreriaResponse> migrarUsuarioMB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        return futuro(() -> LibreriaFraudes.migrarUsuarioMB(contexto, usuarioLibreriaRequest));
    }

    public static Futuro<UsuarioLibreriaResponse> validarCsmMB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        return futuro(() -> LibreriaFraudes.validarCsmMB(contexto, usuarioLibreriaRequest));
    }

}
