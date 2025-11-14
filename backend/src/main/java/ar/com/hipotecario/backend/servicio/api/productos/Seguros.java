package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.Seguros.Seguro;

public class Seguros extends ApiObjetos<Seguro> {

	/* ========== ATRIBUTOS ========== */
	public static class Seguro extends ApiObjeto {
		public String producto;
		public String ramo;
		public String numeroPoliza;
		public Fecha fechaDesde;
		public Fecha fechaHasta;
		public String montoPrima;
		public String medioPago;
		public String origen;
		public String numeroCuenta;
	}
}
