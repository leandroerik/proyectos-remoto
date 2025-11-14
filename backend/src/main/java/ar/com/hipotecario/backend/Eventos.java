package ar.com.hipotecario.backend;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Eventos {

	private static Map<String, List<String>> map = new ConcurrentHashMap<>();

	public static void limpiar(String idCobis) {
		if (idCobis != null) {
			map.put(idCobis, new CopyOnWriteArrayList<String>());
		}
	}

	public static void registrar(String idCobis, String evento) {
		if (idCobis != null) {
			List<String> eventos = map.get(idCobis);
			if (eventos != null) {
				eventos.add(evento);
			}
		}
	}

	public static List<String> consultar(String idCobis) {
		List<String> eventos = map.get(idCobis);
		return eventos != null ? eventos : new CopyOnWriteArrayList<String>();
	}
}