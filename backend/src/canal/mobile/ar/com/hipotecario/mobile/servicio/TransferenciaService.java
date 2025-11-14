package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.negocio.Cuenta;

public class TransferenciaService {

	/* ========== SERVICIOS ========== */
	public static Map<String, String> conceptos() {
		Map<String, String> mapa = new LinkedHashMap<>();
		mapa.put("ALQ", "Alquileres");
		mapa.put("EXP", "Expensas");
		mapa.put("OIN", "Operaciones inmobiliarias");
		mapa.put("PRE", "Préstamos");
		mapa.put("HON", "Honorarios");
		mapa.put("BRH", "Bienes registrables habitualistas");
		mapa.put("SON", "Suscripción Obligaciones Negociables");
		mapa.put("CUO", "Cuotas");
		mapa.put("FAC", "Facturas");
		mapa.put("OIH", "Operaciones inmobiliarias habitualista");
		mapa.put("SEG", "Seguros");
		mapa.put("HAB", "Haberes");
		mapa.put("BRN", "Bienes registrables no habitualistas");
		mapa.put("APC", "Aportes de capital");
		mapa.put("VAR", "Varios");
		return mapa;
	}	
	
	public static Map<String, String> conceptosTransferencias() {
		Map<String, String> mapa = new LinkedHashMap<>();
		mapa.put("ALQ", "Alquileres");
		mapa.put("EXP", "Expensas");
		mapa.put("OIN", "Operaciones inmobiliarias");
		mapa.put("PRE", "Préstamos");
		mapa.put("HON", "Honorarios");
		mapa.put("BRH", "Bienes registrables habitualistas");
		mapa.put("SON", "Suscripción Obligaciones Negociables");
		mapa.put("CUO", "Cuotas");
		mapa.put("FAC", "Facturas");
		mapa.put("OIH", "Operaciones inmobiliarias habitualista");
		mapa.put("SEG", "Seguros");
		mapa.put("HAB", "Haberes");
		mapa.put("BRN", "Bienes registrables no habitualistas");
		mapa.put("APC", "Aportes de capital");
		mapa.put("VAR", "Varios");
		return mapa;
	}
	
	public static Map<String, String> conceptosPagoSueldos() {
		Map<String, String> mapa = new LinkedHashMap<>();
		mapa.put("HAB", "Haberes");
		mapa.put("SERVDOM", "Servicio doméstico");
		return mapa;
	}

	public static Map<String, String> conceptosDebin() {
		Map<String, String> mapa = new LinkedHashMap<>();
		mapa.put("ALQ", "Alquileres");
		mapa.put("EXP", "Expensas");
		mapa.put("OIN", "Operaciones inmobiliarias");
		mapa.put("PRE", "Préstamos");
		mapa.put("HON", "Honorarios");
		mapa.put("BRH", "Bienes registrables habitualistas");
		mapa.put("SON", "Suscripción Obligaciones Negociables");
		mapa.put("CUO", "Cuotas");
		mapa.put("FAC", "Facturas");
		mapa.put("OIH", "Operaciones inmobiliarias habitualista");
		mapa.put("SEG", "Seguros");
		mapa.put("BRN", "Bienes registrables no habitualistas");
		mapa.put("APC", "Aportes de capital");
		mapa.put("VAR", "Varios");
		mapa.put("PLF", "Plazo Fijo");
		return mapa;
	}

	public static ApiResponseMB transferenciaCuentaPropia(ContextoMB contexto, String idCuentaOrigen, String idCuentaDestino, BigDecimal monto, String concepto, Boolean aceptaDDJJ) {
		Cuenta cuentaOrigen = contexto.cuenta(idCuentaOrigen);
		Cuenta cuentaDestino = contexto.cuenta(idCuentaDestino);

		ApiRequestMB request = ApiMB.request("TransferenciaCuentaPropia", "cuentas", "POST", "/v1/cuentas/{idcuenta}/transferencias", contexto);
		request.path("idcuenta", cuentaOrigen.numero());
		request.query("cuentapropia", "true");
		request.query("inmediata", "false");
		request.query("aceptaDDJJ", aceptaDDJJ.toString());

		request.body("cuentaOrigen", cuentaOrigen.numero());
		request.body("importe", monto);
		request.body("reverso", false);
		request.body("cuentaDestino", cuentaDestino.numero());
		request.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
		request.body("tipoCuentaDestino", cuentaDestino.idTipo());
		request.body("idMoneda", cuentaOrigen.idMoneda());
		request.body("idMonedaDestino", cuentaDestino.idMoneda());
		request.body("modoSimulacion", false);
		request.body("descripcionConcepto", concepto);
		request.body("idCliente", contexto.idCobis());

		return ApiMB.response(request, new Date().getTime());
	}

	public void getTransferByIdCobis(String idCobis) {
//		SqlRequest request = Sql.request("SelectAuditorTransferencia", "hbs");
//		String query = "SELECT * FROM [hbs].[dbo].[auditor_transferencia] WHERE cobis = ?";

	}

//	private AuditorTransferencia assemblerTransferencia(Objeto objeto) {
//		Integer id = (Integer) objeto.get("id");
//		String idcobis = (String) objeto.get("idCobis");
//		LocalDateTime momento = ((Timestamp) objeto.get("momento")).toLocalDateTime();
//		String idProceso = (String) objeto.get("idProceso");
//		String ip = (String) objeto.get("ip");
//		String canal = (String) objeto.get("canal");
//		String codigoError = (String) objeto.get("codigoError");
//		String descripcionError = (String) objeto.get("descripcionError");
//		String tipo = (String) objeto.get("tipo");
//		String cuentaOrigen = (String) objeto.get("cuentaOrigen");
//		String cuentaDestino = (String) objeto.get("cuentaDestino");
//		String importe = (String) objeto.get("importe");
//		String moneda = (String) objeto.get("moneda");
//		String concepto = (String) objeto.get("concepto");
//
//		return new AuditorTransferencia();
//	}
}
