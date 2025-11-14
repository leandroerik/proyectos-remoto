package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasDebitoV4.TarjetaDebitoV4;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.TarjetasCredito;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// COBIS: SELECT TOP 100 * FROM [cob_atm].[dbo].[tm_tarjeta]
public class TarjetasDebitoV4 extends ApiObjetos<TarjetaDebitoV4> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaDebitoV4 extends ApiObjeto {
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
			titularidad = "P".equals(codigoTitularidad) ? "Principal" : titularidad;
			titularidad = "T".equals(codigoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(codigoTitularidad) ? "Adicional" : titularidad;
			return titularidad;
		}
	}

	public static List<TarjetaDebitoV4> tarjetasDebito(Contexto contexto) {
		List<TarjetaDebitoV4> tarjetasDebito = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (TarjetaDebitoV4 item : productos.tarjetasDebito) {
			if (!"C".equals(item.estado)) {
				tarjetasDebito.add(item);
			}
		}
		return tarjetasDebito;
	}

	public static List<Objeto> tarjetasDebitoById(ContextoHB contexto) {
		ar.com.hipotecario.backend.conector.api.ApiRequest request = new ApiRequest("ConsultaConsolidadaTarjetaDeDebito", "tarjetasdebito", "GET", "/v2/tarjetasdebito", contexto);

		request.query("idcliente", contexto.idCobis());
		request.query("tipoestado", "vigente");
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		if (response.http(204)) {
			throw new RuntimeException("SIN_TARJETAS");
		}

		try {
			List<Objeto> tarjetas = response.objetos();
			return tarjetas;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error deserializando la respuesta a una lista de Tarjetas", e);
		}
	}
}