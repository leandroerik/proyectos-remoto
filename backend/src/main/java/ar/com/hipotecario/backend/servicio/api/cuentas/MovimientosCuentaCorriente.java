package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCuentaCorriente.MovimientoCuentaCorriente;

public class MovimientosCuentaCorriente extends ApiObjetos<MovimientoCuentaCorriente> {

	public Integer cantPaginas;
	public Integer numPagina;

	/* ========== ATRIBUTOS ========== */
	public static class MovimientoCuentaCorriente extends ApiObjeto {
		public String descripcionMovimiento;
		public String oficina;
		public String causa;
		public String descCausa;
		public String referencia;
		public String secuencial;
		public String codigoAlterno;
		public String canal;
		public String prodCP;
		public String cuentaCP;
		public String cuitCP;
		public String codBancoCP;
		public String nombreCP;
		public String sucursalCP;
		public String codComercio;
		public String codRubro;
		public String categoria;
		public String subCategoria;
		public String tipoMsg;
		public BigDecimal valor;
		public Fecha fecha;
		public Fecha hora;
		public BigDecimal saldo;
	}

	/* ========== SERVICIOS ========== */
	public static Boolean validPendiente(Integer pendiente) {
		return pendiente >= 0 && pendiente < 3 ? true : false;
	}

	public static Boolean validDiaHabil(Character diaHabil) {
		return diaHabil.equals('S') || diaHabil.equals('N') ? true : false;
	}

	public static Boolean validOrden(Character diaHabil) {
		return diaHabil.equals('A') || diaHabil.equals('D') ? true : false;
	}

	public static Boolean validTipoMovimiento(Character tipoMovimiento) {
		if (tipoMovimiento.equals('D') || tipoMovimiento.equals('C') || tipoMovimiento.equals('T'))
			return true;
		return false;
	}

	public static MovimientosCuentaCorriente get(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, String concepto) {
		return get(contexto, idCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validactaempleado, 'N', concepto);
	};

	// API-Cuentas_ConsultaMovimientosCuentaCorriente
	static MovimientosCuentaCorriente get(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, Character diaHabil, String concepto) {
		ApiRequest request = new ApiRequest("CuentasGetMovimientos", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}/movimientos", contexto);

		if (!validPendiente(pendientes) || !validTipoMovimiento(tipoMovimiento) || !validOrden(orden))
			throw new ApiException(request, null);

		request.path("idcuenta", idCuenta);
		if (validDiaHabil(diaHabil))
			request.query("soloDiaHabil", diaHabil);
		request.query("pendientes", pendientes);
		request.query("orden", orden);
		request.query("tipomovimiento", tipoMovimiento);
		request.query("validactaempleado", validactaempleado);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
//		request.query("numeropagina", Integer.parseInt(numeroPagina));
//		if (concepto != null)
//			request.query("concepto", concepto);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		MovimientosCuentaCorriente movimientos = response.crear(MovimientosCuentaCorriente.class);
		if (movimientos != null && !movimientos.isEmpty()) {
			movimientos.cantPaginas = response.objetos().get(0).integer("cantPaginas");
			movimientos.numPagina = response.objetos().get(0).integer("numPagina");
			movimientos.remove(0);
		}
		return movimientos;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2021-10-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2021-10-31", "yyyy-MM-dd");
		MovimientosCuentaCorriente datos = get(contexto, "300000000000332", fechaDesde, fechaHasta, "0", 'A', 0, 'T', true, null);
		imprimirResultado(contexto, datos);
	}
}
