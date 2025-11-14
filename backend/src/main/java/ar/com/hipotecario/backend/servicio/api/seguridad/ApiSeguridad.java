package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.servicio.api.seguridad.VU.RequestIniciarVU;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.ResponseMigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.RequestMigracionUsuario;

public class ApiSeguridad extends Api {

    /* ========== HOMEBANKING ========== */
    public static Futuro<Boolean> loginHB(Contexto contexto, String usuario, String claveUsuario, String claveNumerica) {
        return futuro(() -> LoginHB.post(contexto, usuario, claveUsuario, claveNumerica));
    }

    /* ========== OFFICE BANKING ========== */
    public static Futuro<LoginOB> loginOB(Contexto contexto, String numeroDocumento, String usuario, String clave) {
        return futuro(() -> LoginOB.post(contexto, numeroDocumento, usuario, clave));
    }

    public static Futuro<LoginOBAnterior> loginOBAnterior(Contexto contexto, Long cuit, String usuario, String clave) {
        return futuro(() -> LoginOBAnterior.post(contexto, cuit, usuario, clave));
    }

    public static Futuro<CambioClaveOB> cambiarClaveOB(Contexto contexto, String nro_documento, String usuario, String clave, String claveAnterior) {
        return futuro(() -> CambioClaveOB.post(contexto, nro_documento, usuario, clave, claveAnterior));
    }

    public static Futuro<RecuperoClaveOB> recuperarClaveOB(Contexto contexto, String nro_documento, String usuario, String clave) {
        return futuro(() -> RecuperoClaveOB.post(contexto, nro_documento, usuario, clave));
    }

    public static Futuro<MigrarUsuarioOB> migrarUsuarioOB(Contexto contexto, String cuitAnterior, String usuarioAnterior, String numeroDocumento, String usuario, String clave, String cuits, String email) {
        return futuro(() -> MigrarUsuarioOB.post(contexto, cuitAnterior, usuarioAnterior, numeroDocumento, usuario, clave, cuits, email));
    }

    public static Futuro<NuevoOperadorOB> altaOperadorOB(Contexto contexto, String numeroDocumentoInicial, String numeroDocumento, Long cuit, String cuentas, String menues) {
        return futuro(() -> NuevoOperadorOB.post(contexto, numeroDocumentoInicial, numeroDocumento, cuit, cuentas, menues));
    }

    public static Futuro<LoginGire> loginGire(Contexto contexto, String cuit, String nombre, String officeBankingId) {
        return futuro(() -> LoginGire.post(contexto, cuit, nombre, officeBankingId));
    }

    public static Futuro<UsuarioGire> usuarioGire(Contexto contexto, Objeto body) {
        return futuro(() -> UsuarioGire.post(contexto, body));
    }


    /* ========== OTP ========== */
    public static Futuro<OTP> generarOTP(Contexto contexto, String idCliente) {
        return futuro(() -> OTP.generar(contexto, idCliente));
    }

    public static Futuro<ApiObjeto> validarOTP(Contexto contexto, String idCliente, String clave, String stateId, String cookie) {
        return futuro(() -> OTP.validar(contexto, idCliente, clave, stateId, cookie));
    }

    public static Futuro<Boolean> validarOtp(Contexto contexto, String idCliente, String clave, String stateId, String cookie) {
        return futuro(() -> OTP.validarOtp(contexto, idCliente, clave, stateId, cookie));
    }

    /* ========== TARJETA DE COORDENADAS ========== */
    public static Futuro<TarjetasCoordenadas> tco(Contexto contexto, String grupo, String idCliente) {
        return futuro(() -> TarjetasCoordenadas.get(contexto, grupo, idCliente));
    }

    public static Futuro<DesafiosTCO> desafiosTCO(Contexto contexto, String idCliente, String grupo, Integer cantidad) {
        return futuro(() -> DesafiosTCO.get(contexto, idCliente, grupo, cantidad));
    }

    public static Futuro<ApiObjeto> validarTCO(Contexto contexto, String idCliente, String grupo, String... respuestas) {
        return futuro(() -> TarjetasCoordenadas.post(contexto, idCliente, grupo, respuestas));
    }

    public static Futuro<ApiObjeto> desbloquearTCO(Contexto contexto, String grupo, String idCliente) {
        return futuro(() -> TarjetasCoordenadas.path(contexto, grupo, idCliente));
    }

    public static Futuro<ApiObjeto> bajaTCO(Contexto contexto, String grupo, String idCliente, String texto, String numerodeserie) {
        return futuro(() -> TarjetasCoordenadas.patch(contexto, grupo, idCliente, texto, numerodeserie));
    }

    /* ========== SOFT TOKEN ========== */
    public static Futuro<SoftToken> softToken(Contexto contexto, String idCliente) {
        return futuro(() -> SoftToken.get(contexto, idCliente));
    }

    public static Futuro<SoftToken> softToken(Contexto contexto, String idCliente, String stateId, String cookie, String otp) {
        return futuro(() -> SoftToken.post(contexto, idCliente, stateId, cookie, otp));
    }

    public static Futuro<TokenISVA> token(Contexto contexto) {
        return futuro(() -> TokenISVA.post(contexto));
    }

    public static Futuro<ApiObjeto> bajaSoftToken(Contexto contexto, String idCliente, String accesToken) {
        return futuro(() -> SoftToken.delete(contexto, idCliente, accesToken));
    }

    public static Futuro<ConsultaQRSoftToken> consultaQR(Contexto contexto, String idCliente, String accesToken) {
        return futuro(() -> ConsultaQRSoftToken.qr(contexto, idCliente, accesToken));
    }

    /* ========== USUARIOS ========== */
    public static Futuro<UsuarioISVA> usuarioISVA(Contexto contexto, String idCliente) {
        return futuro(() -> UsuarioISVA.get(contexto, idCliente));
    }

    public static Futuro<UsuarioISVA> usuarioISVA(Contexto contexto, String idCliente, String grupo) {
        return futuro(() -> UsuarioISVA.get(contexto, idCliente, grupo));
    }

    public static Futuro<UsuarioISVA> postUsuarioISVA(Contexto contexto, String idCliente, String nombreCompleto) {
        return futuro(() -> UsuarioISVA.post(contexto, idCliente, nombreCompleto));
    }

    public static Futuro<ApiObjeto> crearClave(Contexto contexto, String idCobis, String clave, String tipo) {
        return futuro(() -> UsuarioISVA.crearClave(contexto, idCobis, clave, tipo));
    }

    public static Futuro<ApiObjeto> cambiarClave(Contexto contexto, String idCobis, String clave, String tipo) {
        return futuro(() -> UsuarioISVA.cambiarClave(contexto, idCobis, clave, tipo));
    }

    /* ========== VU ========== */
    public static Futuro<VU> iniciarVU(Contexto contexto, RequestIniciarVU request) {
        return futuro(() -> VU.iniciar(contexto, request));
    }

    public static Futuro<VU> frenteVU(Contexto contexto, String cuil, String idOperacion, String foto, Boolean analizarAnomalias, Boolean analizarOcr) {
        return futuro(() -> VU.frente(contexto, cuil, idOperacion, foto, analizarAnomalias, analizarOcr));
    }

    /* ========== Transmit ========== */
    public static Futuro<ResponseMigracionUsuario> gestionarMigracion(Contexto contexto, RequestMigracionUsuario request) {
        return futuro(() -> MigracionUsuario.gestionarMigracion(contexto, request));
    }
}