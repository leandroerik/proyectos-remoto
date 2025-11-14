package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class DebitosCreditosOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public BigDecimal comision;
	public String estado;
	public String fecha;
	public String id;
	public String idTransaccion;
	public BigDecimal importe;
	public BigDecimal impuestos;
	public String recibo;

	/* ========== SERVICIOS ========== */
	public static DebitosCreditosOB postDebito(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta) {
		ApiRequest request = new ApiRequest("Debitos", "cuentas", "POST", "/v1/cuentas/{idcuenta}/debitos", contexto);
		request.path("idcuenta", numeroCuenta);
		request.body("tasaIvaAdicional", 0);
		request.body("tasaIvaBasico", 0);
		request.body("tasaIvaExencion", 0);
		request.body("tasaIvaPercepcion", 0);
		request.body("tasaIvaReduccion", 0);
		request.body("ivaBasico", 0);
		request.body("ivaPercepcion", 0);
		request.body("ivaAdicional", 0);
		request.body("codigoOficina", 0);

		request.body("codigoConcepto", codigoConcepto);
		request.body("cuenta", numeroCuenta);
		request.body("idMoneda", moneda);
		request.body("importe", importe);
		request.body("importeBase", importe);
		request.body("usuario", usuario);
		request.body("fecha", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toString());
		request.body("tipoProducto", tipoCuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DebitosCreditosOB.class);
	}

	public static DebitosCreditosOB patchDebito(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta, String idTransaccion) {
		ApiRequest request = new ApiRequest("ReversaDebitos", "cuentas", "PATCH", "/v1/cuentas/{idcuenta}/debitos/{idtransaccion}", contexto);
		request.path("idcuenta", numeroCuenta);
		request.path("idtransaccion", idTransaccion);
		request.body("tasaIvaAdicional", 0);
		request.body("tasaIvaBasico", 0);
		request.body("tasaIvaExencion", 0);
		request.body("tasaIvaPercepcion", 0);
		request.body("tasaIvaReduccion", 0);
		request.body("ivaBasico", 0);
		request.body("ivaPercepcion", 0);
		request.body("ivaAdicional", 0);
		request.body("codigoOficina", 0);
		request.body("codRespuesta", 0);

		request.body("codigoConcepto", codigoConcepto);
		request.body("cuenta", numeroCuenta);
		request.body("idMoneda", moneda);
		request.body("importe", importe);
		request.body("importeBase", importe);
		request.body("usuario", usuario);
		request.body("fecha", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toString());
		request.body("tipoProducto", tipoCuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DebitosCreditosOB.class, response);
	}

	public static DebitosCreditosOB postCredito(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta) {
		ApiRequest request = new ApiRequest("Creditos", "cuentas", "POST", "/v1/cuentas/{idcuenta}/creditos", contexto);
		request.path("idcuenta", numeroCuenta);
		request.body("tasaIvaAdicional", 0);
		request.body("tasaIvaBasico", 0);
		request.body("tasaIvaExencion", 0);
		request.body("tasaIvaPercepcion", 0);
		request.body("tasaIvaReduccion", 0);
		request.body("ivaBasico", 0);
		request.body("ivaPercepcion", 0);
		request.body("ivaAdicional", 0);
		request.body("codigoOficina", 0);

		request.body("codigoConcepto", codigoConcepto);
		request.body("cuenta", numeroCuenta);
		request.body("idMoneda", moneda);
		request.body("importe", importe);
		request.body("importeBase", importe);
		request.body("usuario", usuario);
		request.body("fecha", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toString());
		request.body("tipoProducto", tipoCuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DebitosCreditosOB.class);
	}

	public static DebitosCreditosOB patchCredito(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta, String idTransaccion) {
		ApiRequest request = new ApiRequest("ReversaCreditos", "cuentas", "PATCH", "/v1/cuentas/{idcuenta}/creditos/{idtransaccion}", contexto);
		request.path("idcuenta", numeroCuenta);
		request.path("idtransaccion", idTransaccion);
		request.body("tasaIvaAdicional", 0);
		request.body("tasaIvaBasico", 0);
		request.body("tasaIvaExencion", 0);
		request.body("tasaIvaPercepcion", 0);
		request.body("tasaIvaReduccion", 0);
		request.body("ivaBasico", 0);
		request.body("ivaPercepcion", 0);
		request.body("ivaAdicional", 0);
		request.body("codigoOficina", 0);
		request.body("codRespuesta", 0);

		request.body("codigoConcepto", codigoConcepto);
		request.body("cuenta", numeroCuenta);
		request.body("idMoneda", moneda);
		request.body("importe", importe);
		request.body("importeBase", importe);
		request.body("usuario", usuario);
		request.body("fecha", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toString());
		request.body("tipoProducto", tipoCuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DebitosCreditosOB.class, response);
	}
}