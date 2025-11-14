package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasComitentesV4.CuentaComitenteV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CuentasComitentesV4 extends ApiObjetos<CuentaComitenteV4> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteV4 extends ApiObjeto {
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
			return "Cuenta Comitente";
		}

		public String titularidad() {
			return Texto.primeraMayuscula(descTitularidad);
		}
	}

	public static List<CuentaComitenteV4> cuentasComitentes(Contexto contexto) {
		List<CuentaComitenteV4> cuentasComitente = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CuentaComitenteV4 item : productos.cuentasComitentes) {
			if ("UNI".equals(item.tipo)) {
				if (!"C".equals(item.estado)) {
					cuentasComitente.add(item);
				}
			}
		}
		return cuentasComitente;
	}
}

