package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.PaquetesV4.PaqueteV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaquetesV4 extends ApiObjetos<PaqueteV4> {

	/* ========== ATRIBUTOS ========== */
	public static class PaqueteV4 extends ApiObjeto {
		public String codigoProducto;
		public String codigoTitularidad;
		public String descripcionPaquete;
		public String descTitularidad;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public Boolean muestraPaquete;
		public String numeroProducto;
		public String tipo;
		public String cuentaAsociada;
		public String codMoneda;
		public String descProducto;
		public BigDecimal detProducto;
		public BigDecimal importe;
		public Fecha pfFechaVencimiento;

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

	public static List<PaqueteV4> paquetes(Contexto contexto) {
		List<PaqueteV4> paquetes = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (PaqueteV4 item : productos.paquetes) {
			paquetes.add(item);
		}
		return paquetes;
	}
}