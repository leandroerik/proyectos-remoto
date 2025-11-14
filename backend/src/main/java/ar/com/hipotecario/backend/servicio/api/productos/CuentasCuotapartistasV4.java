package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasCuotapartistasV4.CuentaCuotapartistaV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CuentasCuotapartistasV4 extends ApiObjetos<CuentaCuotapartistaV4> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaCuotapartistaV4 extends ApiObjeto {
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
	}

	public static List<CuentaCuotapartistaV4> cuentasCuotapartistas(Contexto contexto) {
		List<CuentaCuotapartistaV4> cuentasCuotapartistas = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CuentaCuotapartistaV4 item : productos.cuentasCuotapartistas) {
			cuentasCuotapartistas.add(item);
		}
		return cuentasCuotapartistas;
	}
}

