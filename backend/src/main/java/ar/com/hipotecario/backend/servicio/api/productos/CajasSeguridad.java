package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CajasSeguridad.CajaSeguridad;

public class CajasSeguridad extends ApiObjetos<CajaSeguridad> {

	/* ========== ATRIBUTOS ========== */
	public static class CajaSeguridad extends ApiObjeto {
		public Boolean muestraPaquete;
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public String estado;
		public Fecha fechaAlta;
		public String idDomicilio;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String tipoOperacion;
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;
		public String estadoCajaSeguridad;
		public Fecha fechaVencimiento;

		public String descripcion() {
			return "Caja de Seguridad";
		}

		public String titularidad() {
			String titularidad = null;
			titularidad = "T".equals(tipoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(tipoTitularidad) ? "Autorizado" : titularidad;
			return titularidad != null ? titularidad : Texto.primeraMayuscula(descTipoTitularidad);
		}

		public String estado() {
			String estado = "";
			estado = "V".equals(estadoCajaSeguridad) ? "Vigente" : estado;
			estado = "C".equals(estadoCajaSeguridad) ? "Cancelada" : estado;
			estado = "L".equals(estadoCajaSeguridad) ? "Legales" : estado;
			estado = "B".equals(estadoCajaSeguridad) ? "Bloqueada" : estado;
			return estado;
		}

		public String sucursal() {
			return Texto.primeraMayuscula(descSucursal);
		}
	}

	public static List<CajaSeguridad> cajasSeguridad(Contexto contexto) {
		List<CajaSeguridad> cajasSeguridad = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CajaSeguridad item : productos.cajasSeguridad) {
			if ("CSG".equals(item.tipoProducto)) {
				cajasSeguridad.add(item);
			}
		}
		return cajasSeguridad;
	}
}
