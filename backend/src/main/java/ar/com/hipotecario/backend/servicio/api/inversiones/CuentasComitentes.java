package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentasComitentes.CuentaComitente;

public class CuentasComitentes extends ApiObjetos<CuentaComitente> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitente extends ApiObjeto {
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String estado;
		public String descEstado;
		public Fecha fechaAlta;
		public Fecha fechaBaja;
		public String idDomicilio;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public Boolean adicionales;
		public String moneda;
		public String monedaDesc;
	}

	/* ========== SERVICIOS ========== */
	public static CuentasComitentes get(Contexto contexto, String idCliente) {
		return get(contexto, idCliente, null, true);
	}

	public static CuentasComitentes get(Contexto contexto, String idCliente, String tipoEstado) {
		return get(contexto, idCliente, tipoEstado, false);
	}

	// API-Inversiones_CuentasComitentesDetalle
	public static CuentasComitentes get(Contexto contexto, String idCliente, String tipoEstado, Boolean firmantes) {
		ApiRequest request = new ApiRequest("CuentasPorComitente", "inversiones", "GET", "/v2/cuentascomitentes", contexto);
		request.query("idcliente", idCliente);
		request.query("tipoestado", tipoEstado);
		request.query("firmantes", firmantes.toString());
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasComitentes.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentasComitentes datos = get(contexto, "4373070");
		imprimirResultado(contexto, datos);
	}
}
