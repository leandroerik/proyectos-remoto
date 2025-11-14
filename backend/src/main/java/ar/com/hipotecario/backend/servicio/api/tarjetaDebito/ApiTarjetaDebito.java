package ar.com.hipotecario.backend.servicio.api.tarjetaDebito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class ApiTarjetaDebito extends Api {

	/* ========== SERVICIOS ========== */

	// API-TarjetasDebito_ConsultaTarjetaDebitoDetalle
	public static Futuro<TarjetasDebitos> tarjeta(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> TarjetasDebitos.get(contexto, numeroTarjeta));
	}

	// API-TarjetasDebito_ConsultaCuentasTarjetaDebito
	public static Futuro<Cuentas> cuentas(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> Cuentas.getCuentas(contexto, numeroTarjeta));
	}
	
	// API-TarjetasDebito_TarjetaDebitoActivar
	public static Futuro<TarjetasDebitos> habilitarTarjetaDebito(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> TarjetasDebitos.habilitarTarjetaDebito(contexto, numeroTarjeta));
	}
	
	// API-TarjetasDebito_AltaSolicitudAmpliacionLimiteTarjetaDebito
	public static Futuro<ApiResponse> modificarLimiteTarjetaDebito(Contexto contexto, String idCliente, String numeroTarjeta, String limiteRetiro) {
		return futuro(() -> TarjetasDebitos.modificarLimiteTarjetaDebito(contexto, idCliente, numeroTarjeta, limiteRetiro));
	}

	// API-TarjetasDebito_ConsultaTitularidadTD
	public static Futuro<ApiResponse> obtenerTitularidadTd(Contexto contexto, String numeroTarjeta, String extendido) {
		return futuro(() -> TarjetasDebitos.obtenerTitularidadTd(contexto, numeroTarjeta, extendido));
	}

	// API-TarjetasDebito_BlanqueoPinLink
	public static Futuro<ApiResponse> tarjetaDebitoBlanquearPin(Contexto contexto, String numeroTarjeta) {
		return futuro(() -> TarjetasDebitos.tarjetaDebitoBlanquearPin(contexto, numeroTarjeta));
	}
	
	// API-TarjetasDebito_ConsultaEstadoTarjetaDebitoLink
	public static Futuro<ApiResponse> consultaEstadoTarjetaDebitoLink(Contexto contexto,String digitoVerificador,String numeroMiembro,String numeroVersion, String numeroTarjeta) {
		return futuro(() -> TarjetasDebitos.consultaEstadoTarjetaDebitoLink(contexto, digitoVerificador, numeroMiembro, numeroVersion, numeroTarjeta));
	}
	

}
