package ar.com.hipotecario.backend.servicio.api.cajasseguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cajasseguridad.CajasSeguridad.CajaSeguridad;

public class CajasSeguridad extends ApiObjetos<CajaSeguridad> {

	public static enum EstadoCajaSeguridad {
		TODOS, VIGENTE, CANCELADO
	}

	/* ========== ATRIBUTOS ========== */
	public static class CajaSeguridad extends ApiObjeto {
		public String estado;
		public Fecha fechaVencimiento;
		public Boolean muestraPaquete;
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public Fecha fechaAlta;
		public String idDomicilio;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String tipoOperacion;
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;
		public String estadoCajaSeguridad;
	}

	/* ========== SERVICIOS ========== */
	// API-CajasSeguridad_ConsultaCajaSeguridadDeCliente
	static CajasSeguridad get(Contexto contexto, String idCliente, Boolean... cache) {
		return get(contexto, idCliente, EstadoCajaSeguridad.VIGENTE, cache);
	}

	static CajasSeguridad get(Contexto contexto, String idCliente, EstadoCajaSeguridad tipoEstado, Boolean... cache) {
		ApiRequest request = new ApiRequest("CajaSeguridad", "cajasseguridad", "GET", "/v2/cajasseguridad", contexto);
		request.query("idcliente", idCliente);
		request.query("tipoestado", tipoEstado.toString().toLowerCase());
		request.cache = Util.lastNonNull(false, cache);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CajasSeguridad.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CajasSeguridad datos = get(contexto, "2094835");
		imprimirResultado(contexto, datos);
	}
}
