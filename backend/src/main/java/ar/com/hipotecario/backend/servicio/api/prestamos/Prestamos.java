package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;
import java.util.Date;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.Prestamos.Prestamo;

public class Prestamos extends ApiObjetos<Prestamo> {

	/* ========== ATRIBUTOS ========== */
	public class PrestamosPorId extends ApiObjeto {
		public String nroCuenta;
		public String tipoPrestamo;
		public String tipoOperacion;
		public String moneda;
		public Date fechaProxVencimiento;
		public Date fechaVencimientoCuotaActual;
		public String plazoRestante;
		public Boolean pagable;
		public Integer cuotaActual;
		public BigDecimal montoAprobado;
		public BigDecimal saldoRestante;
		public BigDecimal montoUltimaCuota;
		public String plazoOriginal;
		public Double tasaInteres;
		public String sistemaAmortizacion;
		public String tipoTasa;
		public BigDecimal montoACancelar;
		public String estado;
	}

	public static class Prestamo extends ApiObjeto {
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String descSucursal;
		public String estado;
		public String descEstado;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String moneda;
		public String descMoneda;
		public String codProducto;
		public String hipotecarioNSP;
		public String formaPago;
		public String esPrecodeu;
		public String esProcrear;
		public String categoria;
		public String idDomicilio;
		public String sucursal;
		public Boolean muestraPaquete;
		public Boolean adicionales;
		public Fecha fechaAlta;
		public Fecha fechaProximoVenc;
		public BigDecimal montoAprobado;
		public BigDecimal cantCuotasMora;
		public BigDecimal montoCuotaActual;
		public Integer plazoOriginal;
	}

	public class CreditosTasaCero extends ApiObjeto {
		public String cuilSolicitante;
		public String cuitEntidad;
		public String email;
		public String fechaSolicitud;
		public String importePeriodo1;
		public String importePeriodo2;
		public String importePeriodo3;
		public String mensaje;
		public String monto;
		public String nroTarjeta;
		public String tipoCredito;
	}

	/* ========== ATRIBUTOS NOVEDADES ========== */
	public static class Novedades extends ApiObjetos<ItemNovedades> {
	}

	public static class ItemNovedades extends ApiObjeto {
		public String numero;
		public String codigo;
		public String descripcion;
		public String referencia;
		public String usuarioAlta;
		public String usuarioUltimaModificacion;
		public String restricciones;
		public Fecha fechaInicial;
		public Fecha fechaAlta;
		public Fecha fechaUltimaModificacion;
	}

	/* ========== ATRIBUTOS RESUMEN NSP ========== */
	public static class ResumenNsp extends ApiObjeto {
		public String file;
	}

	/* ========== ATRIBUTOS IMPUESTOS SELLOS ========== */
	public static class ImpuestosSellos extends ApiObjeto {
		public BigDecimal monto;
	}

	/* ========== ATRIBUTOS FORMAS COBROS ========== */
	public static class FormasCobros extends ApiObjetos<FormaCobro> {
	}

	public static class FormaCobro extends ApiObjeto {
		public String formaCobro;
		public String descripcion;
		public Integer producto;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_PosicionConsolidadaPrestamos
	public static Prestamos get(Contexto contexto, String idCliente) {
		return get(contexto, idCliente, true, false);
	}

	public static Prestamos get(Contexto contexto, String idCliente, Boolean estado, Boolean buscansp) {
		ApiRequest request = new ApiRequest("Prestamos", "prestamos", "GET", "/v2/prestamos", contexto);
		request.query("idcliente", idCliente);
		request.query("estado", estado);
		request.query("buscansp", buscansp);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Prestamos.class);
	}
	public static Prestamos getPorEstado(Contexto contexto, String idCliente, String tipoEstado, Boolean buscansp) {
		ApiRequest request = new ApiRequest("Prestamos", "prestamos", "GET", "/v2/prestamos", contexto);
		request.query("idcliente", idCliente);
		request.query("estado", tipoEstado);
		request.query("buscansp", buscansp);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Prestamos.class);
	}



	// API-Prestamos_ConsultaPrestamo
	public static PrestamosPorId get(Contexto contexto, String id, Boolean detalle) {
		ApiRequest request = new ApiRequest("PrestamoPorId", "prestamos", "GET", "/v1/prestamos/{id}", contexto);
		request.path("id", id);
		request.query("detalle", true);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosPorId.class);
	}

	// API-Prestamos_ConsultaSolicitudCreditoTasaCero
	public static CreditosTasaCero getCreditosTasaCero(Contexto contexto, String cuil) {
		ApiRequest request = new ApiRequest("BeneficiarioPrestamoTasaCero", "prestamos", "GET", "/v1/prestamos/creditos/{cuil}", contexto);
		request.path("cuil", cuil);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CreditosTasaCero.class);
	}

	// API-Prestamos_ConsultaNovedadesPrestamo
	public static Novedades getNovedades(Contexto contexto, String id) {
		ApiRequest request = new ApiRequest("PrestamosNovedades", "prestamos", "GET", "/v1/prestamos/{id}/novedades", contexto);
		request.path("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Novedades.class);
	}

	// API-Prestamos_PDFResumenPrestamo
	public static Boolean validTipoPagoPrestamo(String tipoPagoPrestamo) {
		return tipoPagoPrestamo.equals("CHEQUERA") || tipoPagoPrestamo.equals("VENTANILLA") || tipoPagoPrestamo.equals("TARJETA") || tipoPagoPrestamo.equals("DEBITO_EN_CUENTA") || tipoPagoPrestamo.equals("SUELDOS_EXTERNOS") || tipoPagoPrestamo.equals("OTROS") ? true : false;
	}

	public static ResumenNsp getResumenNsp(Contexto contexto, String id, String tipoPagoPrestamo) {
		ApiRequest request = new ApiRequest("PrestamosResumenNsp", "prestamos", "GET", "/v1/prestamos/{id}/resumennsp", contexto);
		request.path("id", id);
		if (validTipoPagoPrestamo(tipoPagoPrestamo))
			request.query("TipoPagoPrestamo", tipoPagoPrestamo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ResumenNsp.class);
	}

	// API-Prestamos_CalculoImpuestoSellos
	public static ImpuestosSellos getImpuestosSellos(Contexto contexto, String idcliente, String iddestinoprestamo, String iddestinovivienda, String idmoneda, String montoaprobado, String plazo, String sucursal, String tipoPrestamo) {
		ApiRequest request = new ApiRequest("CalculoImpuestoSellos", "prestamos", "GET", "/v1/prestamos/impuestosellos/{idcliente}", contexto);
		request.path("idcliente", idcliente);
		request.query("iddestinoprestamo", iddestinoprestamo);
		request.query("iddestinovivienda", iddestinovivienda);
		request.query("idmoneda", idmoneda);
		request.query("montoaprobado", montoaprobado);
		request.query("plazo", plazo);
		request.query("sucursal", sucursal);
		request.query("tipoPrestamo", tipoPrestamo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ImpuestosSellos.class);
	}

	// API-Prestamos_ListaFormaCobroPrestamo
	public static FormasCobros getFormasCobro(Contexto contexto, String numOperacion) {
		ApiRequest request = new ApiRequest("PrestamoListaFormaCobro", "prestamos", "GET", "/v1/prestamos/{numOperacion}/formacobro", contexto);
		request.path("numOperacion", numOperacion);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(FormasCobros.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String metodo = "getFormasCobro";

		if (metodo.equals("prestamoById")) {
			PrestamosPorId datos = get(contexto, "0280334001", false);
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("prestamo")) {
			Prestamos datos = get(contexto, "2523908");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("creditoTasaCero")) {
			CreditosTasaCero datos = getCreditosTasaCero(contexto, "20224386738");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getNovedades")) {
			Novedades datos = getNovedades(contexto, "0001669293");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getResumenNsp")) {
			ResumenNsp datos = getResumenNsp(contexto, "MN1422-000-00044-00000-028606", "OTROS");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getImpuestosSellos")) {
			ImpuestosSellos datos = getImpuestosSellos(contexto, "6461133", "010", "0", "80", "1434306.00", "960", "0", "PROEMPREN4");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getFormasCobro")) {
			FormasCobros datos = getFormasCobro(contexto, "0370081148");
			imprimirResultado(contexto, datos);
		}
	}
}
