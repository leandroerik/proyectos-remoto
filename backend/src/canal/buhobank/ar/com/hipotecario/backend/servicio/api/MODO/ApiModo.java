package ar.com.hipotecario.backend.servicio.api.MODO;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.CuentaModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.TarjetaModo;

import java.util.List;

public class ApiModo extends Api {
	public static String API = "Modo";

	public static Futuro<Boolean> get(Contexto contexto, String idCobis, String phoneNumber) {
		return futuro(() -> Usuarios.checkUser(contexto, idCobis, phoneNumber));
	}

	public static Futuro<Usuarios> post(Contexto contexto, String idCobis, String numeroDocumento, String nombres, String apellidos, String genero, String telefono, String email) {
		return futuro(() -> Usuarios.post(contexto, idCobis, numeroDocumento, nombres, apellidos, genero, telefono, email));
	}

	public static Futuro<Usuarios> postSimplificado(Contexto contexto, String idCobis, String numeroDocumento, String nombres, String apellidos, String genero, String telefono, String email) {
		return futuro(() -> Usuarios.postSimplificado(contexto, idCobis, numeroDocumento, nombres, apellidos, genero, telefono, email));
	}

	public static Futuro<Usuarios> confirmOtp(Contexto contexto, String idCobis, String numeroDocumento, String telefono, String id, String clave) {
		return futuro(() -> Usuarios.confirmOtp(contexto, idCobis, numeroDocumento, telefono, id, clave));
	}

	public static void insertToken(Contexto contexto, String idCobis, String accessToken, String refreshToken, String expiresIn, String scope, String phoneNumber) {
		futuro(() -> Usuarios.insertToken(contexto, idCobis, accessToken, refreshToken, expiresIn, scope, phoneNumber));
	}

	public static Futuro<Boolean> postCuentas(Contexto contexto, String idCobis, String numeroDocumento, String accessToken, List<CuentaModo> cuentas) {
		return futuro(() -> Usuarios.postCuentas(contexto, idCobis, numeroDocumento, accessToken, cuentas));
	}

	public static Futuro<Boolean> postTarjeta(Contexto contexto, String idCobis, String numeroDocumento, String accessToken, TarjetaModo tarjeta) {
		return futuro(() -> Usuarios.postTarjeta(contexto, idCobis, numeroDocumento, accessToken, tarjeta));
	}
}
