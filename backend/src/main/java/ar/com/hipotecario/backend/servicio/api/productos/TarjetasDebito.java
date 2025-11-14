package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasDebito.TarjetaDebito;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


// COBIS: SELECT TOP 100 * FROM [cob_atm].[dbo].[tm_tarjeta]
public class TarjetasDebito extends ApiObjetos<TarjetaDebito> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaDebito extends ApiObjeto {
		public Boolean muestraPaquete;
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public String estado;
		public Fecha fechaAlta;
		public String tipoTitularidad;
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;
		public Fecha fechaVencimiento;
		public Boolean activacionTemprana;
		public String idPaquete;
		public Boolean virtual;

		public String descripcion() {
			return "Tarjeta de Débito";
		}

		public String ultimos4digitos() {
			return Modulo.ultimos4digitos(numeroProducto);
		}

		public String titularidad() {
			String titularidad = "";
			titularidad = "P".equals(tipoTitularidad) ? "Principal" : titularidad;
			titularidad = "T".equals(tipoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(tipoTitularidad) ? "Adicional" : titularidad;
			return titularidad;
		}
	}

	public static List<TarjetaDebito> tarjetasDebito(Contexto contexto) {
		List<TarjetaDebito> tarjetasDebito = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (TarjetaDebito item : productos.tarjetasDebito) {
			if (!"C".equals(item.estado)) {
				tarjetasDebito.add(item);
			}
		}
		return tarjetasDebito;
	}

}
