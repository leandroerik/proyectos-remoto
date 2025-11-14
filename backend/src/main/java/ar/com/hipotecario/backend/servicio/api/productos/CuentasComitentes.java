package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasComitentes.CuentaComitente;

public class CuentasComitentes extends ApiObjetos<CuentaComitente> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitente extends ApiObjeto {
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
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;

		public String descripcion() {
			return "Cuenta Comitente";
		}

		public String titularidad() {
			return Texto.primeraMayuscula(descTipoTitularidad);
		}
	}

	public static List<CuentaComitente> cuentasComitentes(Contexto contexto) {
		List<CuentaComitente> cuentasComitente = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CuentaComitente item : productos.cuentasComitentes) {
			if ("UNI".equals(item.tipoProducto)) {
				if (!"C".equals(item.estado)) {
					cuentasComitente.add(item);
				}
			}
		}
		return cuentasComitente;
	}
}
