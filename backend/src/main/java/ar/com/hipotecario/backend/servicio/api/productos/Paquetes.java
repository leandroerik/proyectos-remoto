package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.Paquetes.Paquete;

public class Paquetes extends ApiObjetos<Paquete> {

	/* ========== ATRIBUTOS ========== */
	public static class Paquete extends ApiObjeto {
		public String codigoPaquete;
		public String descPaquete;
		public String descTipoTitularidad;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public Boolean muestraPaquete;
		public String numero;
		public String tipo;
		public String tipoTitularidad;

		public static Map<String, String> mapaDescripciones(Contexto contexto) {
			Map<String, String> mapa = new LinkedHashMap<>();
			mapa.put("23", "Paquete Empleados BH");
			mapa.put("24", "Paquete BH Fácil");
			mapa.put("25", "Paquete BH Fácil");
			mapa.put("26", "Paquete BH Click");
			mapa.put("28", "Paquete BH Click");
			mapa.put("34", "Fácil Pack Sueldo");
			mapa.put("35", "Búho Pack Sueldo");
			mapa.put("36", "Gold Pack Sueldo");
			mapa.put("37", "Platinum Pack Sueldo");
			mapa.put("38", "Black Pack Sueldo");
			mapa.put("39", "Fácil Pack");
			mapa.put("40", "Búho Pack");
			mapa.put("41", "Gold Pack");
			mapa.put("42", "Platinum Pack");
			mapa.put("43", "Black Pack");
			mapa.put("44", "Pack");
			mapa.put("45", "Pack");
			mapa.put("46", "Pack");
			mapa.put("47", "Emprendedor Pack");
			mapa.put("48", "BH Pack");
			mapa.put("53", "Emprendedor Black Pack");
			mapa.put("68", "Búho Pack Libertad");
			mapa.put("69", "Gold Pack Libertad");
			mapa.put("70", "Platinum Pack Libertad");
			mapa.put("71", "Black Pack Libertad");

			// Para el negocio Buho inicia no se considera venta paquetizada
			mapa.put("0", "Búho Inicia");
			mapa.put("2", "Búho Inicia Libertad");
			return mapa;
		}
	}

	public static List<Paquete> paquetes(Contexto contexto) {
		List<Paquete> paquetes = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (Paquete item : productos.paquetes) {
			paquetes.add(item);
		}
		return paquetes;
	}
}
