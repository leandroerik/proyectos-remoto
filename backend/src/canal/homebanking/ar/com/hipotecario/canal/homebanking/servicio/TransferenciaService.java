package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

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
		mapa.put("HAB", "Haberes");
		mapa.put("BRN", "Bienes registrables no habitualistas");
		mapa.put("APC", "Aportes de capital");
		mapa.put("VAR", "Varios");
		mapa.put("PLF", "Plazo Fijo");
		return mapa;
	}
}
