package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Prestamo;

public class RestPrestamo {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB ultimaLiquidacion(ContextoMB contexto, String numeroPrestamo, String cuota) {
		ApiRequestMB request = ApiMB.request("UltimaLiquidacionPrestamo", "prestamos", "POST", "/v1/vencimientos/{plantilla}/prestamos", contexto);
		request.path("plantilla", "AvisoVencimiento");
		request.query("numerooperacion", numeroPrestamo);
		request.query("cuota", cuota);
		return ApiMB.response(request, contexto.idCobis(), cuota);
	}

	public static ApiResponseMB ultimaLiquidacionNsp(ContextoMB contexto, String numeroPrestamo) {
		ApiRequestMB request = ApiMB.request("UltimaLiquidacionPrestamoNsp", "prestamos", "GET", "/v1/prestamos/{numero}/resumennsp", contexto);
		request.path("numero", numeroPrestamo);
		request.query("TipoPagoPrestamo", "OTROS");
		return ApiMB.response(request, contexto.idCobis(), numeroPrestamo);
	}

	public static RespuestaMB tienePrestamosNsp(ContextoMB contexto, RespuestaMB respuesta) {

		if (contexto.tienePrestamosNsp()) {
			ApiRequestMB request = ApiMB.request("PrestamosNSP", "prestamos", "GET", "/v1/prestamos", contexto);
			request.query("buscansp", "true");
			request.query("estado", "true");
			request.query("idcliente", contexto.idCobis());

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
			if (response.hayError()) {
				return RespuestaMB.error();
			}

			for (Objeto item : response.objetos()) {
				if (item.string("tipoProducto").equals("NSP")) {
					Objeto nsp = new Objeto();
					nsp.set("id", item.string("idProducto"));
					nsp.set("numero", item.string("numeroProducto"));
					nsp.set("idEstado", item.string("estado"));
					nsp.set("estado", item.string("descEstado"));
					nsp.set("fechaAlta", item.date("fechaAlta", "yyyy-MM-dd", "dd/MM/yyyy"));
					nsp.set("idTitularidad", item.string("tipoTitularidad"));
					nsp.set("titularidad", item.string("descTipoTitularidad"));
					respuesta.add("nsp", nsp);
				}
			}
		}
		return respuesta;
	}

	public static ApiResponseMB detalle(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("Prestamo", "prestamos", "GET", "/v1/prestamos/{id}", contexto);
		request.path("id", numero);
		request.query("detalle", "true");
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static void eliminarCacheDetalle(ContextoMB contexto, String numero) {
		ApiMB.eliminarCache(contexto, "Prestamo", numero);
	}

	public static List<ApiResponseMB> cuotas(ContextoMB contexto, String numero, Integer cantidadCuotas) {
		Integer elementosPorPagina = 10;
		List<ApiResponseMB> lista = new ArrayList<>();

		ExecutorService executorService = Concurrencia.executorService(cantidadCuotas / elementosPorPagina);
		for (Integer i = 0; i < cantidadCuotas; i += elementosPorPagina) {
			final Integer x = i;
			executorService.submit(() -> {
				ApiRequestMB request = ApiMB.request("CuotasPrestamos", "prestamos", "GET", "/v1/prestamos/{id}/cuotas", contexto);
				request.path("id", numero);
				request.query("cuota", x.toString());
				request.cacheSesion = true;
				ApiResponseMB response = ApiMB.response(request, numero, x);
				lista.add(response);
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
		return lista;
	}

	public static void eliminarCacheCuotas(ContextoMB contexto, String numero, Integer cantidadCuotas) {
		Integer elementosPorPagina = 10;
		for (Integer i = 0; i < cantidadCuotas; i += elementosPorPagina) {
			ApiMB.eliminarCache(contexto, "CuotasPrestamos", numero, i);
		}
	}

	public static ApiResponseMB simluarCancelacionTotal(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("SimularCancelacionTotalPrestamo", "prestamos", "POST", "/v1/prestamos/negociacion", contexto);
		request.body("num_operacion", numero);
		request.body("reduccion", "T");
		request.body("tipo_cancelacion", "CANCTOTAL");
		return ApiMB.response(request, numero);
	}

	public static ApiResponseMB eliminarNegociacionCancelacionTotal(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("EliminarNegociacionCancelacionTotalPrestamo", "prestamos", "DELETE", "/v1/prestamos/{id}/negociacion", contexto);
		request.path("id", numero);
		return ApiMB.response(request, numero);
	}

	public static ApiResponseMB movimientos(ContextoMB contexto, String cuenta, String fecha, String productoCobis, String secuencial) {
		ApiRequestMB request = ApiMB.request("Movimientos", "prestamos", "GET", "/v1/prestamos/{numCuenta}/movimientos", contexto);
		request.path("numCuenta", cuenta);
		request.query("fechaMovimiento", fecha);
		request.query("numCuenta ", cuenta);
		request.query("productoCobis", productoCobis);
		request.query("secuencial", secuencial);
		return ApiMB.response(request);
	}

	/* ========== MAPAS ========== */
	public static String tipo(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("CALLANSES$", "Hipotecario");
		mapa.put("HADNUANSES", "Hipotecario");
		mapa.put("HADUSANSES", "Hipotecario");
		mapa.put("HAMTEANSES", "Hipotecario");
		mapa.put("HCHAADNU", "Hipotecario");
		mapa.put("HCHAADNURP", "Hipotecario");
		mapa.put("HCHAADUS", "Hipotecario");
		mapa.put("HCHAADUSRP", "Hipotecario");
		mapa.put("HCHAAMTE", "Hipotecario");
		mapa.put("HCHACONS", "Hipotecario");
		mapa.put("HCHACONSRP", "Hipotecario");
		mapa.put("HCHAESCAYT", "Hipotecario");
		mapa.put("HCHAESCCON", "Hipotecario");
		mapa.put("HCONSANSES", "Hipotecario");
		mapa.put("HESCAYT", "Hipotecario");
		mapa.put("HESCCONS", "Hipotecario");
		mapa.put("HICONSEMVA", "Hipotecario");
		mapa.put("HICONSUVI", "Hipotecario UVA");
		mapa.put("HIPLOCFI", "Hipotecario");
		mapa.put("HIPLOCVA", "Hipotecario");
		mapa.put("HIPO$", "Hipotecario");
		mapa.put("HIPO$EMP", "Hipotecario");
		mapa.put("HIPO$EMPVA", "Hipotecario");
		mapa.put("HIPO$VAR", "Hipotecario");
		mapa.put("HIPOBH0609", "Hipotecario");
		mapa.put("HIPOCASAPR", "Hipotecario");
		mapa.put("HIPOCOMB", "Hipotecario");
		mapa.put("HIPOCONEMP", "Hipotecario");
		mapa.put("HIPOCONS$", "Hipotecario");
		mapa.put("HIPOCONSUE", "Hipotecario");
		mapa.put("HIPOCONSVA", "Hipotecario");
		mapa.put("HIPOHPLUSF", "Hipotecario");
		mapa.put("HIPOHPLUSV", "Hipotecario");
		mapa.put("HIPOINM", "Hipotecario");
		mapa.put("HIPOINMEMP", "Hipotecario");
		mapa.put("HIPOMIG01", "Hipotecario");
		mapa.put("HIPOMIGCER", "Hipotecario");
		mapa.put("HIPOMIGCOM", "Hipotecario");
		mapa.put("HIPOMIGEMP", "Hipotecario");
		mapa.put("HIPOMIGFA", "Hipotecario");
		mapa.put("HIPOMIGMB", "Hipotecario");
		mapa.put("HIPOMIGME", "Hipotecario");
		mapa.put("HIPOMIGVJA", "Hipotecario");
		mapa.put("HIPOPROC1", "C. Propia y A. Joven");
		mapa.put("HIPOREGU", "Hipotecario");
		mapa.put("HIPOSINCEN", "Hipotecario");
		mapa.put("HIPOSTEPUP", "Hipotecario");
		mapa.put("HIPOSUELDO", "Hipotecario");
		mapa.put("HIPOSVIDA", "Hipotecario");
		mapa.put("HIPOTERR$", "Hipotecario");
		mapa.put("HIPOU$S", "Hipotecario");
		mapa.put("HIPOUVI", "Hipotecario UVA");
		mapa.put("HIPYMEALF", "Hipotecario");
		mapa.put("HIPYMEALV", "Hipotecario");
		mapa.put("PERSP", "Personal");
		mapa.put("PERSPADEBA", "Personal");
		mapa.put("PERSPAMPL", "Personal");
		mapa.put("PERSPCC", "Personal");
		mapa.put("PERSPEMP", "Personal");
		mapa.put("PERSPFGS", "Personal");
		mapa.put("PERSPFGS2", "Personal");
		mapa.put("PERSPFGS2A", "Personal");
		mapa.put("PERSPREGU", "Personal");
		mapa.put("PERSPSMF", "Personal");
		mapa.put("PERSPUVA", "Personal");
		mapa.put("PERSPVAR", "Personal");
		mapa.put("PMOGARPRE", "Personal");
		mapa.put("PMOGTIAHIP", "Personal");
		mapa.put("PMOSIND", "Personal");
		mapa.put("PMOSINDOL", "Personal");
		mapa.put("PMOSPYMES", "Personal");
		mapa.put("PMOTASADIP", "Personal");
		mapa.put("PMOTASADIR", "Personal");
		mapa.put("PMOU$SPROP", "Personal");
		mapa.put("PMTASADIP2", "Personal");
		mapa.put("PPANSESTUR", "Hipotecario");
		mapa.put("PPCHACONV", "Personal");
		mapa.put("PPCHAPREND", "Personal");
		mapa.put("PPEMPFGS", "Personal");
		mapa.put("PPEMPFGS2", "Personal");
		mapa.put("PPEMPFGS2A", "Personal");
		mapa.put("PPPROACUVA", "Micro Crédito");
		mapa.put("PPPROCREA1", "Personal");
		mapa.put("PPPROCREA2", "Personal");
		mapa.put("PPPROCREA3", "Personal");
		mapa.put("PPPROINFRA", "Procrear Gas");
		mapa.put("PPPROMATE2", "Procrear Materiales");
		mapa.put("PPPROMATER", "Procrear Materiales");
		mapa.put("PPROMATER", "Procrear Gas");
		mapa.put("PPROSARIO", "Hipotecario");
		mapa.put("PPTDCOMER", "Personal");
		mapa.put("PRECODEMP", "Personal");
		mapa.put("PRECODEU", "Personal");
		mapa.put("PRECODEUFH", "Personal");
		mapa.put("PRECONSFIN", "Personal");
		mapa.put("PREFI", "Personal");
		mapa.put("PRENDAU$S", "Prendario");
		mapa.put("PRENDAUTOP", "Prendario");
		mapa.put("PRENDAUTOV", "Prendario");
		mapa.put("PRESCHA", "Personal");
		mapa.put("PRESHML", "Personal");
		mapa.put("PRESLARIO", "Personal");
		mapa.put("PRESMENDO", "Personal");
		mapa.put("PRESMENFGS", "Personal");
		mapa.put("PRESRETAIL", "Personal");
		mapa.put("PREVENTA1", "PREVENTA 1");
		mapa.put("PREVENTA2", "PREVENTA 2");
		mapa.put("PREVENTA3", "PREVENTA 3");
		mapa.put("PREVENTA4", "PREVENTA 4");
		mapa.put("PREVENTA5", "PREVENTA 5");
		mapa.put("PROADQNU1", "Hipotecario");
		mapa.put("PROADQNU2", "Hipotecario");
		mapa.put("PROADQNU3", "Hipotecario");
		mapa.put("PROADQNU4", "Hipotecario");
		mapa.put("PROADQNU5", "Hipotecario");
		mapa.put("PROADQROS1", "Hipotecario");
		mapa.put("PROADQROS2", "Hipotecario");
		mapa.put("PROC3D1", "Hipotecario");
		mapa.put("PROC3D2", "Hipotecario");
		mapa.put("PROC3D3", "Hipotecario");
		mapa.put("PROC3D4", "Hipotecario");
		mapa.put("PROC3D5", "Hipotecario");
		mapa.put("PROCAYUDA1", "Personal");
		mapa.put("PROCAYUDA2", "Personal");
		mapa.put("PROCAYUDA3", "Personal");
		mapa.put("PROCAYUDA4", "Personal");
		mapa.put("PROCAYUDA5", "Personal");
		mapa.put("PROCINUND", "Personal");
		mapa.put("PROCINUND2", "Personal");
		mapa.put("PROCLOTSE1", "Hipotecario");
		mapa.put("PROCLOTSE2", "Hipotecario");
		mapa.put("PROCLOTSE3", "Hipotecario");
		mapa.put("PROCLOTSE4", "Hipotecario");
		mapa.put("PROCLOTSE5", "Hipotecario");
		mapa.put("PROCOMB1", "Hipotecario");
		mapa.put("PROCOMB2", "Hipotecario");
		mapa.put("PROCOMB3", "Hipotecario");
		mapa.put("PROCOMB4", "Hipotecario");
		mapa.put("PROCOMB5", "Hipotecario");
		mapa.put("PROCOMPUVI", "P. Compl UVA");
		mapa.put("PROCOMUVI2", "P. Compl UVA");
		mapa.put("PROCONROS1", "Hipotecario");
		mapa.put("PROCONROS2", "Hipotecario");
		mapa.put("PROCREACE1", "Hipotecario");
		mapa.put("PROCREACE2", "Hipotecario");
		mapa.put("PROCREACE3", "Hipotecario");
		mapa.put("PROCREACE4", "Hipotecario");
		mapa.put("PROCREACE5", "Hipotecario");
		mapa.put("PROCREAR1", "Hipotecario");
		mapa.put("PROCREAR2", "Hipotecario");
		mapa.put("PROCREAR3", "Hipotecario");
		mapa.put("PROCREAR4", "Hipotecario");
		mapa.put("PROCREAR5", "Hipotecario");
		mapa.put("PROCREAYT1", "Hipotecario");
		mapa.put("PROCREAYT2", "Hipotecario");
		mapa.put("PROCREAYT3", "Hipotecario");
		mapa.put("PROCREAYT4", "Hipotecario");
		mapa.put("PROCREFAC1", "Crédito Refacción");
		mapa.put("PROCREFAC2", "Crédito Refacción");
		mapa.put("PROEMPHOG1", "Hipotecario");
		mapa.put("PROEMPHOG2", "Hipotecario");
		mapa.put("PROEMPHOG3", "Hipotecario");
		mapa.put("PROEMPREN1", "Hipotecario");
		mapa.put("PROEMPREN2", "Hipotecario");
		mapa.put("PROEMPREN3", "Hipotecario");
		mapa.put("PROEMPREN4", "Hipotecario");
		mapa.put("PROEMPREN5", "Hipotecario");
		mapa.put("PROEMPUVA1", "Hipotecario UVA");
		mapa.put("PROEMPUVA2", "Hipotecario UVA");
		mapa.put("PROLOCOHOG", "Hipotecario");
		mapa.put("RHPLUSSINF", "Personal");
		mapa.put("RHPLUSSINV", "Personal");
		mapa.put("TASADIPSC", "Personal");
		mapa.put("PPADELANTO", "Adelanto");
		String valor = mapa.get(codigo);
		return valor != null ? valor : "";
	}

	// TODO: REVISAR: NDMNCA, EFMN
	public static String formaPago(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("1", "Chequera");
		mapa.put("2", "Por Ventanilla");
		mapa.put("3", "Por Tarjeta");
		mapa.put("5", "Débito en cuenta");
		mapa.put("8", "Otros");
		mapa.put("7", "Sueldos Externos");
		mapa.put("EFMN", "Efectivo");
		mapa.put("NDMNCA", "Se debitará de tu");
		mapa.put("NDMNCC", "Se debitará de tu");
		mapa.put("EFMNC", "Efectivo");
		String valor = mapa.get(codigo);
		return valor != null ? valor : "";
	}

	public static String titularidad(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("A", "Cotitular");
		mapa.put("C", "Codeudor");
		mapa.put("D", "Deudor Principal");
		mapa.put("E", "Empleador");
		mapa.put("F", "Firmante");
		mapa.put("R", "Representante Legal");
		mapa.put("T", "Titular");
		mapa.put("U", "Autorizado ATM");
		String valor = mapa.get(codigo);
		return valor != null ? valor : "";
	}

	public static String estado(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("V", "Vigente");
		mapa.put("NORMAL", "a pagar");
		mapa.put("NO VIGENTE", "No vigente");
		mapa.put("CANCELADA", "pagada");
		mapa.put("VENCIDO", "impaga");
		// TODO problema con estados agrego estas opciones
		mapa.put("CANCELADO", "Cancelada");
		mapa.put("C", "Cancelada");
		String valor = mapa.get(codigo);
		return valor != null ? valor : "";
	}

	public static Objeto detalleFormaPago(ContextoMB contexto, String numOperacion, String formaPago) {
		// Obtiene las formas de cobro disponibles por la operacion (numeroProducto)
		ApiRequestMB request = ApiMB.request("formacobro", "prestamos", "GET", "/v1/prestamos/{numOperacion}/formacobro", contexto);
		request.path("numOperacion", numOperacion);
		ApiResponseMB response = ApiMB.response(request);
		Objeto detalleFormaPago = new Objeto();

		if (formaPago.equals("EFMN")) {
			for (Objeto fp : response.objetos()) {
				if (fp.get("formaCobro").equals(formaPago) || fp.get("formaCobro").equals("EFMNC")) {
					detalleFormaPago.add(fp);
					break;
				}
			}
		} else {
			for (Objeto fp : response.objetos()) {
				if (fp.get("formaCobro").equals(formaPago)) {
					detalleFormaPago.add(fp);
					break;
				}
			}
		}

		return detalleFormaPago;
	}

	public static ApiResponseMB pagarPrestamoElectronico(ContextoMB contexto, Prestamo prestamo, Cuenta cuenta, BigDecimal importe) {

		ApiRequestMB request = ApiMB.request("PagarPrestamo", "orquestados", "POST", "/v1/prestamos/{id}/electronicos", contexto);
		request.path("id", prestamo.numero());
		request.body("cuenta", cuenta.numero());
		request.body("idMoneda", prestamo.idMoneda());
		request.body("importe", importe);
		request.body("tipoCuenta", cuenta.idTipo());
		request.body("tipoPrestamo", prestamo.idTipo());
		request.body("reverso", "false");
		request.body("idProducto", prestamo.id());

		ApiResponseMB response = ApiMB.response(request);
		return response;
	}

	public static ApiResponseMB pagarTotalPrestamo(ContextoMB contexto, Prestamo prestamo, Cuenta cuenta) {
		ApiRequestMB request = ApiMB.request("PrecancelacionTotalPrestamo", "prestamos", "POST", "/v1/prestamos/aplicacion", contexto);
		request.body("cuenta_cobro", cuenta.numero());
		request.body("forma_cobro", cuenta.esCajaAhorro() ? "NDMNCA" : cuenta.esCuentaCorriente() ? "NDMNCC" : null);
		request.body("num_operacion", prestamo.numero());
		request.body("tipo_cancelacion", "CANCTOTAL");

		ApiResponseMB response = null;
		try {
			response = ApiMB.response(request);
		} catch (RuntimeException e) {
			RestPrestamo.eliminarNegociacionCancelacionTotal(contexto, prestamo.numero());
			throw e;
		} finally {
			try {
				ProductosService.eliminarCacheProductos(contexto);
				prestamo.eliminarCacheDetalle();
			} catch (Exception e) {
			}
		}
		return response;
	}

//	public static String estado(String codigo) {
//		Map<String, String> mapa = new HashMap<>();
//		mapa.put("BCA", "Baja Cartera Administrada");
//		mapa.put("BPV", "Baja Preventiva");
//		mapa.put("C", "Cancelado");
//		mapa.put("CIERRE MOR", "Cierre por Mora");
//		mapa.put("CIERREMORA", "Cierre por Mora");
//		mapa.put("CME", "Convenio de Mora Extinto");
//		mapa.put("CP", "Cancelacion Pendiente");
//		mapa.put("CPV", "Preventa Cancelada");
//		mapa.put("D", "Desembolso");
//		mapa.put("E", "Extinto");
//		mapa.put("G", "Periodo de Gracia");
//		mapa.put("I", "Ingresado Minorista");
//		mapa.put("J", "En Juicio");
//		mapa.put("JS", "Juicio en Suspenso");
//		mapa.put("L", "Liberado");
//		mapa.put("MAD", "Modificacion Administrativa");
//		mapa.put("MT", "Rematado");
//		mapa.put("NE", "No Emite");
//		mapa.put("NORMAL", "Normal");
//		mapa.put("PL", "Pendiente de Liquidacion");
//		mapa.put("PV", "Preventa");
//		mapa.put("R", "Reembolso");
//		mapa.put("S", "Solicitado");
//		mapa.put("SPV", "Solicitado Preventa");
//		mapa.put("ST", "Siniestro en Trámite");
//		mapa.put("SUSPENDIDO", "Suspendido");
//		mapa.put("TRD", "Prestamo Transferido de Delegacion");
//		mapa.put("V", "Vigente");
//		mapa.put("z", "Aprobado");
//		mapa.put("Z", "Rechazado");
//		String valor = mapa.get(codigo);
//		return valor != null ? valor : "";
//	}
}
