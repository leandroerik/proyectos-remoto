package ar.com.hipotecario.backend.servicio.api.notificaciones;

import java.util.ArrayList;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.empresas.SubConveniosOB;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BeneficiarioOB;

//http://api-notificaciones-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiNotificaciones extends Api {

	/* ========== Notificaciones Controller ========== */

	// TODO: GET /v1/notificaciones

	// TODO: @Deprecated POST /v1/notificaciones/{id}/alertas

	// POST /v1/notificaciones/sms
	public static Futuro<EnvioSMS> envioOtpSms(Contexto contexto, String telefono, String codigo) {
		return futuro(() -> EnvioSMS.postOTP(contexto, telefono, codigo));
	}
	
	public static Futuro<EnvioSMS> envioOtpSms(Contexto contexto, String telefono, String codigo, String mensaje) {
		return futuro(() -> EnvioSMS.postOTPv1(contexto, telefono, codigo, mensaje));
	}

	public static Futuro<EnvioSMS> envioSms(Contexto contexto, String telefono, String codigo) {
		return futuro(() -> EnvioSMS.post(contexto, telefono, codigo));
	}

	// TODO: POST /v2/notificaciones/{id}/alertas

	/* ========== Correo Electronico Controller ========== */

	// POST /v1/correoelectronico
	public static Futuro<EnvioEmail> envioOtpEmailHB(Contexto contexto, String email, String clave) {
		return futuro(() -> EnvioEmail.postEnvioOtpHB(contexto, email, clave));
	}

	public static Futuro<EnvioEmail> envioOtpEmailOB(Contexto contexto, String email, String nombre, String apellido, String clave) {
		return futuro(() -> EnvioEmail.postEnvioOtpOB(contexto, email, nombre, apellido, clave));
	}

	public static Futuro<EnvioEmail> envioOtpEmailBB(Contexto contexto, String email, String otp, String nombre) {
		return futuro(() -> EnvioEmail.postEnvioOtpBB(contexto, email, otp, nombre));
	}

	public static Futuro<EnvioEmail> postEnvioBB(Contexto contexto, String asunto, String plantilla, String email, String url) {
		return futuro(() -> EnvioEmail.postEnvioBB(contexto, asunto, plantilla, email, url));
	}
	
	public static Futuro<EnvioEmail> postEnvioBBV2(Contexto contexto, String asunto, String plantilla, String para, String nombre, String linkPromociones, String contenido) {
		return futuro(() -> EnvioEmail.postEnvioBBV2(contexto, asunto, plantilla, para, nombre, linkPromociones, contenido));
	}

	public static Futuro<EnvioEmail> envioEmailBB(Contexto contexto, String plantilla, String asunto, String para, String urlStore) {
		return futuro(() -> EnvioEmail.postEnvioMailBB(contexto, plantilla, asunto, para, urlStore));
	}

	public static Futuro<EnvioEmail> recuperoUsuarioOB(Contexto contexto, String para, String nombre, String apellido, String usuario) {
		return futuro(() -> EnvioEmail.postRecuperoUsuarioOB(contexto, para, nombre, apellido, usuario));
	}

	public static Futuro<EnvioEmail> envioInvitacionOB(Contexto contexto, String para, String nombre, String apellido, String empresa, String token) {
		return futuro(() -> EnvioEmail.postEnvioInvitacionOB(contexto, para, nombre, apellido, empresa, token));
	}
	public static Futuro<EnvioEmail> envioInvitacionNuevoUserAdministradorOB(Contexto contexto, String para, String nombre, String apellido, EmpresaOB empresa) {
		return futuro(() -> EnvioEmail.postEnvioInvitacionNuevoUserAdministradorOB(contexto, para, nombre, apellido, empresa));
	}
	public static Futuro<EnvioEmail> envioBienvenidaVinculacionEmpresaAdministradorOB(Contexto contexto, String para, String nombre, String apellido, EmpresaOB empresa) {
		return futuro(() -> EnvioEmail.postEnvioVinculacionEmpresaAdministradorOB(contexto, para, nombre, apellido, empresa));
	}
	
	public static Futuro<EnvioEmail> envioAltaNominaOB(Contexto contexto, String para, String nombre, String apellido, String empresa) {
		return futuro(() -> EnvioEmail.postAltaNominaOB(contexto, para, nombre, apellido, empresa));
	}

	public static Futuro<EnvioEmail> envioDatosCuentaOB(Contexto contexto, String email, String cbu, String alias, String cuenta, String titular, String cuit, String tipoCuenta) {
		return futuro(() -> EnvioEmail.postEnvioDatosDeCuentaOB(contexto, email, cbu, alias, cuenta, titular, cuit, tipoCuenta));
	}

	public static Futuro<EnvioEmail> envioAvisoCambioClave(Contexto contexto, String para, String nombre, String apellido) {
		return futuro(() -> EnvioEmail.postAvisoCambioClaveOB(contexto, para, nombre, apellido));
	}

	public static Futuro<EnvioEmail> envioAvisoActivacionSoftToken(Contexto contexto, String para, String nombre, String apellido) {
		return futuro(() -> EnvioEmail.postAvisoActivacionSoftToken(contexto, para, nombre, apellido));
	}

	public static Futuro<EnvioEmail> envioAvisoModificaDatosPersonales(Contexto contexto, String para, String nombre, String apellido) {
		return futuro(() -> EnvioEmail.postAvisoModificaDatosPersonales(contexto, para, nombre, apellido));
	}

	public static Futuro<EnvioEmail> envioBienvenidaOB(Contexto contexto, String para, String nombre, String apellido) {
		return futuro(() -> EnvioEmail.postEnvioBienvenidaOB(contexto, para, nombre, apellido));
	}

	public static Futuro<EnvioEmail> envioAvisoTRNEnviadaOB(Contexto contexto, String para, String nombre, String monto, String numeroOperacion, String cuentaOrigen, String cuentaDestino) {
		LogOB.evento(null, "envioAvisoTRNEnviadaOB", "Envio mail trn: "+ numeroOperacion);
		return futuro(() -> EnvioEmail.postTRNEnviadaOB(contexto, para, nombre, monto, numeroOperacion, cuentaOrigen, cuentaDestino));
	}

	public static Futuro<EnvioEmail> envioAvisoBenfiario(Contexto contexto, InfoCuentaDTO infoCuenta, String banco, String fechaCreacion,String para, String usuario) {
		return futuro(() -> EnvioEmail.postEnvioAvisoBeneficiario(contexto, infoCuenta, banco, fechaCreacion, para, usuario));
	}

	public static Futuro<EnvioEmail> envioAvisoSinPlantilla(Contexto contexto, String para, String cc, String nombre, String asunto, String mensaje, String html) {
		return futuro(() -> EnvioEmail.postAvisoSinPlantilla(contexto, para, cc, nombre, asunto, mensaje, html));
	}

	public static Futuro<EnvioEmail> envioAvisoTRNRecibidaOB(Contexto contexto, String para, String nombre, String monto, String numeroOperacion, String cuentaOrigen, String cuentaDestino) {
		return futuro(() -> EnvioEmail.postTRNRecibidaOB(contexto, para, nombre, monto, numeroOperacion, cuentaOrigen, cuentaDestino));
	}

	public static Futuro<EnvioEmail> envioAvisoOperacionError(Contexto contexto, String para, String tipoOperacion, String idOperacion, String fecha) {
		return futuro(() -> EnvioEmail.postOperacionError(contexto, para, tipoOperacion, idOperacion, fecha));
	}

	public static Futuro<EnvioEmail> envioSolicitudPermisosOB(Contexto contexto, String para, String nombreOperadorInicial, String nombreSolicitante, ArrayList<String> funcionalidadesSolicitadas, String empresa) {
		return futuro(() -> EnvioEmail.postSolicitarPermisosOB(contexto, para, nombreOperadorInicial, nombreSolicitante, funcionalidadesSolicitadas, empresa));
	}
	public static Futuro<EnvioEmail> EnvioCheques(Contexto contexto, String para, EcheqOB cheque) {
		return futuro(() -> EnvioEmail.postEnvioCheque(contexto, para, cheque));
	}
	public static Futuro<EnvioEmail> postEnvioMailInhabilitadoSeguridad(Contexto contexto, String para, UsuarioOB usuario) {
		return futuro(() -> EnvioEmail.postEnvioMailInhabilitadoSeguridad(contexto, para,usuario));
	}
	public static Futuro<EnvioEmail> postEnvioMailInhabilitado(Contexto contexto, UsuarioOB usuario) {
		return futuro(() -> EnvioEmail.postEnvioMailInhabilitado(contexto, usuario));
	}
	// TODO: POST /v1/exchange
	// TODO: Separar las notificaciones de los canales

}