package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasCuotapartistas.CuentaCuotapartista;

public class CuentasCuotapartistas extends ApiObjetos<CuentaCuotapartista> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaCuotapartista extends ApiObjeto {
		public String descPaquete;
		public String descTipoTitularidad;
		public String estado;
		public Fecha fechaAlta;
		public Boolean muestraPaquete;
		public String numero;
		public String tipo;
		public String tipoTitularidad;
	}

	public static List<CuentaCuotapartista> cuentasCuotapartistas(Contexto contexto) {
		List<CuentaCuotapartista> cuentasCuotapartistas = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CuentaCuotapartista item : productos.cuentasCuotapartistas) {
			cuentasCuotapartistas.add(item);
		}
		return cuentasCuotapartistas;
	}
}
