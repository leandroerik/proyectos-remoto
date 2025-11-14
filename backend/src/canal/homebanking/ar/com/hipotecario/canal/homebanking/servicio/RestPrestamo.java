package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;

public class RestPrestamo {

	/* ========== SERVICIOS ========== */
	public static ApiResponse ultimaLiquidacion(ContextoHB contexto, String numeroPrestamo, String cuota) {
		ApiRequest request = Api.request("UltimaLiquidacionPrestamo", "prestamos", "POST", "/v1/vencimientos/{plantilla}/prestamos", contexto);
		request.path("plantilla", "AvisoVencimiento");
		request.query("numerooperacion", numeroPrestamo);
		request.query("cuota", cuota);
		return Api.response(request, contexto.idCobis(), cuota);
	}

	public static ApiResponse ultimaLiquidacionNsp(ContextoHB contexto, String numeroPrestamo) {
		ApiRequest request = Api.request("UltimaLiquidacionPrestamoNsp", "prestamos", "GET", "/v1/prestamos/{numero}/resumennsp", contexto);
		request.path("numero", numeroPrestamo);
		request.query("TipoPagoPrestamo", "OTROS");
		return Api.response(request, contexto.idCobis(), numeroPrestamo);
	}

	public static ApiResponse detalle(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("Prestamo", "prestamos", "GET", "/v1/prestamos/{id}", contexto);
		request.path("id", numero);
		request.query("detalle", "true");
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static void eliminarCacheDetalle(ContextoHB contexto, String numero) {
		Api.eliminarCache(contexto, "Prestamo", numero);
	}

	public static List<ApiResponse> cuotas(ContextoHB contexto, String numero, Integer cantidadCuotas) {
		Integer elementosPorPagina = 10;
		List<ApiResponse> lista = new ArrayList<>();

		ExecutorService executorService = Concurrencia.executorService(cantidadCuotas / elementosPorPagina);
		for (Integer i = 0; i < cantidadCuotas; i += elementosPorPagina) {
			final Integer x = i;
			executorService.submit(() -> {
				ApiRequest request = Api.request("CuotasPrestamos", "prestamos", "GET", "/v1/prestamos/{id}/cuotas", contexto);
				request.path("id", numero);
				request.query("cuota", x.toString());
				request.permitirSinLogin = true;
				request.cacheSesion = true;
				ApiResponse response = Api.response(request, numero, x);
				lista.add(response);
			});
		}
		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
		return lista;
	}

	public static void eliminarCacheCuotas(ContextoHB contexto, String numero, Integer cantidadCuotas) {
		Integer elementosPorPagina = 10;
		for (Integer i = 0; i < cantidadCuotas; i += elementosPorPagina) {
			Api.eliminarCache(contexto, "CuotasPrestamos", numero, i);
		}
	}

	public static ApiResponse simluarCancelacionTotal(ContextoHB contexto, String numero) {
		Boolean cancelacionAnticipada = contexto.parametros.bool("cancelacionAnticipada", false);
		BigDecimal montoAnticipo = contexto.parametros.bigDecimal("montoAnticipo", "0");

		ApiRequest request = Api.request("SimularCancelacionTotalPrestamo", "prestamos", "POST", "/v1/prestamos/negociacion", contexto);
		request.body("num_operacion", numero);
		request.body("reduccion", "T");
		if(!cancelacionAnticipada)
			request.body("tipo_cancelacion", "CANCTOTAL");
		else
		{
			request.body("tipo_cancelacion", "ANTCAPITAL");
			request.body("monto", contexto.sesion.cotizacionAnticipoPrestamo != null && contexto.sesion.cotizacionAnticipoPrestamo.intValue() > 0 ? montoAnticipo.divide(contexto.sesion.cotizacionAnticipoPrestamo, 2, RoundingMode.CEILING) : montoAnticipo);
		}
		return Api.response(request, numero);
	}

	public static ApiResponse eliminarNegociacionCancelacionTotal(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("EliminarNegociacionCancelacionTotalPrestamo", "prestamos", "DELETE", "/v1/prestamos/{id}/negociacion", contexto);
		request.path("id", numero);
		return Api.response(request, numero);
	}

	public static ApiResponse movimientos(ContextoHB contexto, String cuenta, String fecha, String productoCobis, String secuencial) {
		ApiRequest request = Api.request("Movimientos", "prestamos", "GET", "/v1/prestamos/{numCuenta}/movimientos", contexto);
		request.path("numCuenta", cuenta);
		request.query("fechaMovimiento", fecha);
		request.query("numCuenta ", cuenta);
		request.query("productoCobis", productoCobis);
		request.query("secuencial", secuencial);
		return Api.response(request);
	}

	public static ApiResponse detalleAnticipo(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("API-Prestamos_ConsultaAnticipo", "prestamos", "GET", "/v1/prestamos/consultaAnticipo", contexto);
		request.query("num_operacion", numero);
		return Api.response(request, numero);
	}

	public static ApiResponse hipotecas(ContextoHB contexto, String cuil) {
		ApiRequest request = Api.request("API-Prestamos_ConsultaHipotecas", "prestamos", "GET", "/v1/hipoteca/{cuil}", contexto);
		request.path("cuil", cuil);
		return Api.response(request, cuil);
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
		mapa.put("PROCONHOG2", "Hipotecario");
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
		mapa.put("PROLOCOHOG2", "Hipotecario");
		mapa.put("PROCAMPHOG", "Hipotecario");
		mapa.put("PROCCONHOG", "Hipotecario");
		mapa.put("RHPLUSSINF", "Personal");
		mapa.put("RHPLUSSINV", "Personal");
		mapa.put("TASADIPSC", "Personal");
		mapa.put("PPCUOTIFSG", "Cuotificación");
		mapa.put("PROCOMPHOG", "Complementario");
		mapa.put("PPADELANTO", "Adelanto");
		mapa.put("PERSPSG", "Personal");
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
		mapa.put("NORMAL", "Normal");
		mapa.put("NO VIGENTE", "No vigente");
		mapa.put("CANCELADA", "Cancelada");
		mapa.put("VENCIDO", "Vencida");
		String valor = mapa.get(codigo);
		return valor != null ? valor : "";
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

	public static Objeto detalleFormaPago(ContextoHB contexto, String numOperacion, String formaPago) {
		// Obtiene las formas de cobro disponibles por la operacion (numeroProducto)
		ApiRequest request = Api.request("formacobro", "prestamos", "GET", "/v1/prestamos/{numOperacion}/formacobro", contexto);
		request.path("numOperacion", numOperacion);
		ApiResponse response = Api.response(request);
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

}
