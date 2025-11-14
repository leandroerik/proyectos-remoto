package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CajasSeguridadV4.CajaSeguridadV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CajasSeguridadV4 extends ApiObjetos<CajaSeguridadV4> {

	/* ========== ATRIBUTOS ========== */
	public static class CajaSeguridadV4 extends ApiObjeto {
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

		public String descripcion() {
			return "Caja de Seguridad";
		}

		public String titularidad() {
			String titularidad = null;
			titularidad = "T".equals(codigoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(codigoTitularidad) ? "Autorizado" : titularidad;
			return titularidad != null ? titularidad : Texto.primeraMayuscula(descTitularidad);
		}

		public String estado() {
			String estadoFinalizado = "";
			estadoFinalizado = "V".equals(estado) ? "Vigente" : estado;
			estadoFinalizado = "C".equals(estado) ? "Cancelada" : estado;
			estadoFinalizado = "L".equals(estado) ? "Legales" : estado;
			estadoFinalizado = "B".equals(estado) ? "Bloqueada" : estado;
			return estadoFinalizado;
		}

//		public String sucursal() {
//			return Texto.primeraMayuscula(descSucursal);
//		}
	}

	public static List<CajaSeguridadV4> cajasSeguridad(Contexto contexto) {
		List<CajaSeguridadV4> cajasSeguridad = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CajaSeguridadV4 item : productos.cajasSeguridad) {
			if ("CSG".equals(item.tipo)) {
				cajasSeguridad.add(item);
			}
		}
		return cajasSeguridad;
	}
}

