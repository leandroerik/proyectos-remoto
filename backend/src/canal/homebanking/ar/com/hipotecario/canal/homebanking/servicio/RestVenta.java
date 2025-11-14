package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.util.Date;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class RestVenta {

	/* ========== GENERAL ========== */
	public static ApiResponse consultarSolicitudes(ContextoHB contexto) {

		String idSolicitud = contexto.parametros.string("idSolicitud");

		ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.query("cuil", contexto.persona().cuit());
		request.query("fechadesde", Fecha.restarDias(new Date(), 5L, "yyyyMMdd"));
		request.path("SolicitudId", idSolicitud);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarSolicitudes(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.query("cuil", contexto.persona().cuit());
		request.query("fechadesde", Fecha.restarDias(new Date(), 5L, "yyyyMMdd"));
		request.path("SolicitudId", idSolicitud);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse generarSolicitud(ContextoHB contexto) {
		ApiRequest request = Api.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
		request.headers.put("X-Handle", "0");
		request.body("TipoOperacion", "03");
		request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
		request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
		request.body("CanalVenta1", ConfigHB.string("api_venta_canalVenta1"));
		request.body("CanalVenta2", ConfigHB.string("api_venta_canalVenta2"));
		request.body("CanalVenta3", ConfigHB.string("api_venta_canalVenta3"));
		request.body("CanalVenta4", ConfigHB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse generarIntegrante(ContextoHB contexto, String idSolicitud) {
		return RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
	}

	public static ApiResponse evaluarSolicitud(ContextoHB contexto, String idSolicitud) {
		RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);

		ApiRequest request = Api.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("TipoOperacion", "03");
		request.body("IdSolicitud", idSolicitud);

		// Unicamente para cuotificacion
		boolean esCuotif = contexto.parametros.bool("esCuotificacion", false);
		if (esCuotif) {
			// request.body("FlagSolicitaAprobacionEstandard", true);
		}

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse finalizarSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.query("estado", "finalizar");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse desistirSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasDesistirSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.query("estado", "desistir");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse agregarSeguroDesempleo(ContextoHB contexto, String idSolicitud, String idProducto) {
		// TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		// integrante.set("idCobis", new Long(contexto.idCobis()));
		// integrante.set("numeroTarjetaDebito", tarjetaDebito != null ?
		// tarjetaDebito.numero() : null);
		integrante.set("rol", "T");

		ApiRequest request = Api.request("VentasGenerarSeguroDesempleo", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/seguroDesempleo", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.add("Integrantes", integrante);

		request.body("TipoOperacion", "02");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse modificarSeguroDesempleo(ContextoHB contexto, String idSolicitud, String idSeguro, String idPrestamo) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
		integrante.set("rol", "T");

		ApiRequest request = Api.request("VentasGenerarSeguroDesempleo", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/seguroDesempleo/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idSeguro);
		request.add("Integrantes", integrante);

		ApiResponse responsePersona = RestPersona.consultarClienteEspecifico(contexto, contexto.idCobis());
		// El Producto Depende de la actividad laboral del cliente, si es relación de
		// dependencia fijo enviar "03278002" sino enviar "18178002"
		if ("1".equals(responsePersona.objetos().get(0).string("idSituacionLaboral"))) {
			request.body("Producto", "03278002");
		} else {
			request.body("Producto", "18178002");
		}
		request.body("DomicilioEnvio").set("Tipo", "DP");
		request.body("DomicilioLaboral").set("Tipo", "LA");
		Objeto ddjj = new Objeto();
		ddjj.set("PreguntaNoAsegurable1", false);
		ddjj.set("PreguntaNoAsegurable2", false);
		ddjj.set("PreguntaNoAsegurable3", false);
		ddjj.set("PreguntaNoAsegurable4", false);
		request.body("Ddjj", ddjj);
		Objeto seguroMedioPago = new Objeto();
		seguroMedioPago.set("TipoMedioPago", "BH");
		Objeto pagoBh = new Objeto();
		pagoBh.set("EsProductoTramite", true);
		pagoBh.set("TipoProducto", "CCA");
		pagoBh.set("NumeroProducto", idPrestamo);
		seguroMedioPago.set("PagoBH", pagoBh);
		request.body("SeguroMedioPago", seguroMedioPago);
		request.body("TipoOperacion", "03");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse eliminarSeguroDesempleo(ContextoHB contexto, String idSolicitud, String idProducto) {
		ApiRequest request = Api.request("VentasEliminarSeguroDesempleo", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/seguroDesempleo/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idProducto);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarMontoSeguroDesempleo(ContextoHB contexto, String idSolicitud, String idProducto) {
		ApiRequest request = Api.request("VentasMontoSeguroDesempleo", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/parametria/31?campo=producto", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		return Api.response(request, contexto.idCobis());
	}

	/* ========== PRODUCTOS ========== */
	public static ApiResponse generarCajaAhorroPesos(ContextoHB contexto, String idSolicitud) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
		integrante.set("rol", "T");

		ApiRequest request = Api.request("VentasGenerarCajaAhorroPesos", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Categoria", "MOV");
		request.body("ProductoBancario", "3");
		request.body("DomicilioResumen").set("tipo", "DP");
		request.body("Oficial", "0");
		request.body("CobroPrimerMantenimiento", false);
		request.body("Moneda", "80");
		request.body("Origen", "10");
		request.body("UsoFirma", "U");
		request.body("Ciclo", "6");
		request.body("ResumenMagnetico", true);
		request.body("TransfiereAcredHab", false);
		request.add("Integrantes", integrante);
		request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
		request.body("IdSolicitud", idSolicitud);
		request.body("TipoOperacion", "03");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse eliminarCajaAhorroPesos(ContextoHB contexto, String idSolicitud, String idProducto) {
		ApiRequest request = Api.request("VentasEliminarCajaAhorroPesos", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/cajaAhorro/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idProducto);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse generarCuentaCorriente(ContextoHB contexto, String idSolicitud, String categoria, String numeroCA) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
		integrante.set("rol", "T");

		ApiRequest request = Api.request("VentasGenerarCuentaCorriente", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cuentaCorriente", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Categoria", categoria);
		request.body("ProductoBancario", "1");
		request.body("IdProductoFrontEnd", "7");
		request.body("CajaAhorroExistente", numeroCA);
		request.body("DomicilioResumen").set("tipo", "DP");
		request.body("Oficial", "0");
		request.body("CobroPrimerMantenimiento", false);
		request.body("Moneda", "80");
		request.body("Origen", "10");
		request.body("UsoFirma", "U");
		request.body("Ciclo", "6");
		request.body("ResumenMagnetico", true);
		request.body("TransfiereAcredHab", false);
		request.add("Integrantes", integrante);
		request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
		request.body("IdSolicitud", idSolicitud);
		request.body("TipoOperacion", "03");
		request.body("EmpresaAseguradora", "40");
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse generarTarjetaDebito(ContextoHB contexto, String idSolicitud, String... numerosCuentasAsociadas) {
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("rol", "T");

		ApiRequest request = Api.request("VentasWindowsPostTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);

		request.body("Tipo", "NC");

		request.body("Domicilio").set("Tipo", "DP");
		request.body("Grupo", "3");
		request.body("TipoCuentaComision", "4");
		request.body("NumeroCtaComision", "0");
		request.add("Integrantes", integrante);
		request.body("IdSolicitud", idSolicitud);
		request.body("TipoOperacion", "03");

		for (String numeroCuentaAsociada : numerosCuentasAsociadas) {
			Objeto item = new Objeto();
			item.set("producto", numeroCuentaAsociada.startsWith("3") ? "3" : "4");
			item.set("cuenta", numeroCuentaAsociada);
			item.set("moneda", numeroCuentaAsociada.startsWith("2") ? "2" : "80");
			item.set("principal", true);
			request.add("TarjetaDebitoCuentasOperativas", item);

			request.body("NumeroCtaComision", numeroCuentaAsociada); // emm: agrego esto que faltaba por si trae cuenta
		}
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse eliminarTarjetaDebito(ContextoHB contexto, String idSolicitud, String idProducto) {
		ApiRequest request = Api.request("VentasEliminarTarjetaDebito", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/tarjetaDebito/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idProducto);
		return Api.response(request, contexto.idCobis());
	}

	/* ========== PRESTAMOS ========== */
	public static ApiResponse consultarSolicitudPrestamoPersonal(ContextoHB contexto, String idSolicitud, String idProducto) {
		ApiRequest request = Api.request("VentasConsultarSolicitudPrestamoPersonal", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idProducto);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse agregarPrestamoPersonal(ContextoHB contexto, String idSolicitud, Boolean esAdelanto) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
		integrante.set("rol", "D");
		ApiRequest request = Api.request("VentasAgregarPrestamoPersonal", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Amortizacion", "01");
		request.body("TipoTasa", "01");
		if (esAdelanto) {
			request.body("Subproducto", ConfigHB.esDesarrollo() ? "46" : "33");
		}
		request.body("DestinoBien", ConfigHB.esDesarrollo() ? "143" : "20");
		request.body("DescripcionDestinoFondos", "Libre destino");
		request.add("Integrantes", integrante);
		request.body("Moneda", "80");

		if (contexto.parametros.bigDecimal("montoCuotificacion") != null) {

			BigDecimal montoCuotificacion = contexto.parametros.bigDecimal("montoCuotificacion");
			// Integer plazoSolicitado = contexto.parametros.integer("plazoSolicitado");
			// request.body("PlazoSolicitado", 12);
			request.body("MontoSolicitado", montoCuotificacion);
			request.body("SubProducto", ConfigHB.esDesarrollo() ? "44" : "31");

		}

		request.body("TipoOperacion", "02"); // Se manda 02 para que no haga todas las validaciones
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse modificarSolicitudPrestamoPersonal(ContextoHB contexto, String idSolicitud, String idProducto, BigDecimal montoSolicitado, Integer plazoSolicitado, Integer diaCobro, String idCuenta, String tipoOperacion // 02: no tiene en cuenta varios errores (ejemplo: que no este la cuenta),
	) {
		Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
		integrante.set("rol", "D");

		ApiRequest request = Api.request("VentasModificarPrestamoPersonal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("ProductoId", idProducto);
		request.body("Amortizacion", "01");
		request.body("TipoTasa", "01");
		request.body("DestinoBien", ConfigHB.esDesarrollo() ? "143" : "20");
		request.body("DescripcionDestinoFondos", "Libre destino");
		request.body("MontoSolicitado", montoSolicitado);
		request.body("PlazoSolicitado", esAdelanto ? 1 : plazoSolicitado);
		if (esAdelanto) {
			request.body("Subproducto", ConfigHB.esDesarrollo() ? "46" : "33");
		}
		request.body("mercado", "01");
		request.body("FormaCobroTipo", "NDMNCA");
		request.body("FormaCobroCuenta", idCuenta);
		Objeto domicilio = new Objeto();
		domicilio.set("Tipo", "DP");
		request.body("Domicilio", domicilio);
		request.body("FechaCobroFija", true);
		request.body("DiaCobro", diaCobro); // pasar por parametro
		request.body("EmpresaAseguradora", "40");

		// DESEMBOLSOS
		Objeto desembolsos = new Objeto();
		desembolsos.set("NroDesembolso", 1);
		desembolsos.set("Capital", montoSolicitado);
		Objeto formasDesembolso = new Objeto();
		formasDesembolso.set("NroDesembolso", 1);
		formasDesembolso.set("Forma", "NCMNCA");
		formasDesembolso.set("Referencia", idCuenta);
		String nombreCompleto = contexto.persona().nombreCompleto();
		formasDesembolso.set("Beneficiario", nombreCompleto.substring(0, nombreCompleto.length() < 40 ? nombreCompleto.length() - 1 : 39));
		formasDesembolso.set("Valor", montoSolicitado); // pasar por parametro
		desembolsos.add("FormasDesembolso", formasDesembolso);
		request.body("Desembolsos", desembolsos);

		// MAIL AVISOS
		Objeto mailAvisos = new Objeto();
		mailAvisos.set("Tipo", "EMP");
		request.body("MailAvisos", mailAvisos);

		// INTEGRANTES
		request.add("Integrantes", integrante);

		request.body("TipoOperacion", tipoOperacion);

		boolean esCuotif = contexto.parametros.bool("esCuotificacion", false);
		if (esCuotif) {

			request.body("SubProducto", ConfigHB.esDesarrollo() ? "44" : "31");

		}

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarSolicitudes(ContextoHB contexto, Long cantidadDias) {
		ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
		request.query("cuil", contexto.persona().cuit());
		String fechaDesde = Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd");
		request.query("fechadesde", fechaDesde);
		return Api.response(request, contexto.idCobis());
	}

	/* ========== PRESTAMOS ========== */
	public static ApiResponse consultarSolicitudAdelanto(ContextoHB contexto, String idSolicitud, String cuentaCorrienteId) {
		ApiRequest request = Api.request("VentasConsultarSolicitudAdelantoBH", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/cuentaCorriente/{cuentaCorrienteId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.path("cuentaCorrienteId", cuentaCorrienteId);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarSolicitudProducto(ContextoHB contexto, String idSolicitud, String tipoProducto, String idProducto) {
		ApiRequest request = Api.request("VentasConsultarSolicitudProducto", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/{tipoProducto}/{idProducto}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("tipoProducto", tipoProducto);
		request.path("idProducto", idProducto);
		return Api.response(request, contexto.idCobis());
	}
}
