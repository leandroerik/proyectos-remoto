package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBAplicacion;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.CuentaInversor;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.AltaCuentaComitenteRequestUnificado;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentafondos.AltaCuentaFondosRequestUnificado;

public class RestInversiones {

	public static ApiResponse altaCuentaInversor(ContextoHB contexto, CuentaInversor cuentaInversor) {
		ApiRequest request = Api.request("altaCuentas", "inversiones", "POST", "/v1/cuenta", contexto);
		Objeto body = new Objeto();
		body.add(cuentaInversor);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}
	public static ApiResponse altaCuentaComitente(ContextoHB contexto, AltaCuentaComitenteRequestUnificado objeto) {
		ApiRequest request = Api.request("altacuentacomitente", "altacuentas", "POST", "/v1/paquetecuentacomitente", contexto);
		Objeto body = new Objeto();
		body.add(objeto);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse altaCuentasFondos(ContextoHB contexto, AltaCuentaFondosRequestUnificado cuenta) {
		ApiRequest request = Api.request("altacuentafondos", "altacuentas", "POST", "/v1/paquetecuentacuotapartista", contexto);
		Objeto body = new Objeto();
		body.add(cuenta);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultaEstadoInversorByma(ContextoHB contexto, List<String> taskIds) {
		ApiRequest request = Api.request("altacuentafondos", "altacuentas", "POST", "/v1/estadoinversorcajavalores", contexto);
		Objeto body = new Objeto();
		taskIds.stream().forEach(t -> body.add(t));
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}
	
	public static ApiResponse consultaEstadoCuentaByma(ContextoHB contexto, String taskId) {
		ApiRequest request = Api.request("altacuentafondos", "altacuentas", "GET", "/v1/estadocuentacajavalores/{taskId}", contexto);
		request.path("taskId", taskId);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse getInversorCNVByCuit(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("cuentasUnitrade", "inversiones", "GET", "/v2/buscar-inversor/{cuil}", contexto);
		request.path("cuil", cuit);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse cuentasUnitrade(ContextoHB contexto) {
		ApiRequest request = Api.request("cuentasUnitrade", "inversiones", "GET", "/v1/cuentascomitentes/cliente/{id}", contexto);
		request.path("id", contexto.idCobis());
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarSubCuenta(ContextoHB contexto, String nroCuenta) {
		ApiRequest request = Api.request("consultarSubcuentas", "inversiones", "POST", "/v1/ConsultarSubCuenta", contexto);
		Objeto body = new Objeto();
		body.set("nroSubCuenta", nroCuenta);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse selectPersonaByDoc(ContextoHB contexto, String numDocumento, String idTipoDoc) {
		ApiRequest request = Api.request("selectByDocPersona", "inversiones", "POST", "/v1/SelectByDocPersona", contexto);
		Objeto body = new Objeto();
		body.set("numDocIdentidad", numDocumento);
		body.set("codTpDocIdentidad", idTipoDoc);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse seguimientoOperaciones(ContextoHB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequest request = Api.request("SeguimientoOperacionesTitulosValores", "inversiones", "GET", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("cuentacomitente", numeroCuentaComitente);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(fechaDesde));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(fechaHasta));
		request.query("desdepagina", "1");
		request.query("registrosxpagina", "1000");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponse seguimientoLicitaciones(ContextoHB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequest request = Api.request("SeguimientoLicitacionesTitulosValores", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/posturas", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(fechaDesde));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(fechaHasta));
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponse movimientos(ContextoHB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequest request = Api.request("MovimientosTitulosValores", "inversiones", "GET", "/v1/{cuenta}/extractoComitente", contexto);
		request.path("cuenta", numeroCuentaComitente);
		request.query("idCobis", contexto.idCobis());
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(fechaDesde));
		request.query("fechaFin", new SimpleDateFormat("dd/MM/yyyy").format(fechaHasta));
		request.query("numeroSecuencial", "1");
		request.query("cantidadRegistro", "1000");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponse movimientosV2(ContextoHB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequest request = Api.request("MovimientosTitulosValores", "inversiones", "GET", "/v2/{cuenta}/extractocomitente", contexto);
		request.path("cuenta", numeroCuentaComitente);
		request.query("idCobis", contexto.idCobis());
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(fechaDesde));
		request.query("fechaFin", new SimpleDateFormat("dd/MM/yyyy").format(fechaHasta));
		request.query("numeroSecuencial", "1");
		request.query("cantidadRegistro", "1000");
		request.query("agrupado", "1");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numeroCuentaComitente);
	}




	public static class ItemCacheTitulosValores {
		public String idTipoActivo;
		public String fecha;
		public Date expiracion;
		public ApiResponse response;

		public Boolean expirado() {
			return expiracion.before(new Date());
		}

		public String clave() {
			return idTipoActivo + "_" + fecha;
		}

		public static String clave(String idTipoActivo, String fecha) {
			return idTipoActivo + "_" + fecha;
		}
	}

	public static class CacheTitulosValores {
		public static Map<String, ItemCacheTitulosValores> mapa = new ConcurrentHashMap<>();

		public static ItemCacheTitulosValores get(String clave) {
			ItemCacheTitulosValores item = mapa.get(clave);
			return item != null && !item.expirado() ? item : null;
		}

		public static void set(String idTipoActivo, String fecha, ApiResponse response) {
			ItemCacheTitulosValores item = new ItemCacheTitulosValores();
			item.idTipoActivo = idTipoActivo;
			item.fecha = fecha;
			item.expiracion = new Date(System.currentTimeMillis() + (ConfigHB.integer("hb_titulos_valores_cache_minutos", 120) * 60 * 1000));
			item.response = response;
			mapa.put(item.clave(), item);
		}
	}

	public static ApiResponse titulosValores(ContextoHB contexto, String fecha) {
		return titulosValores(contexto, null, fecha);
	}

	public static ApiResponse titulosValores(ContextoHB contexto, String idTipoActivo, String fecha) {
		if (fecha.isEmpty()) {
			fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}

		ItemCacheTitulosValores item = CacheTitulosValores.get(ItemCacheTitulosValores.clave(idTipoActivo, fecha));
		if (item != null) {
			return item.response;
		}
		
		ApiRequest request = Api.request("TitulosValores", "inversiones", "GET", "/v1/titulos/producto", contexto);
		request.query("fecha", fecha);
		request.query("secuencial", "1");
		request.query("cantregistros", "1000");
		request.query("clasificacion", idTipoActivo);
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, contexto.idCobis(), idTipoActivo);
		if (!response.hayError()) {
			CacheTitulosValores.set(idTipoActivo, fecha, response);
		}
		return response;
	}

	public static ApiResponse tenenciaTitulosValores(ContextoHB contexto, String numeroCuentaComitente) {
		ApiRequest request = Api.request("TenenciaTitulosValores", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/especies", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		request.query("idcliente", contexto.idCobis());
		return Api.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponse obtenerPosicionesNegociables(ContextoHB contexto, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequest request = Api.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		return Api.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponse obtenerPosicionesNegociablesCache(ContextoHB contexto, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequest request = Api.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponse obtenerPosicionesNegociables(ContextoHB contexto, String cuentaComitente, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequest request = Api.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
		request.path("id", contexto.idCobis());
		request.path("cuentaComitente", cuentaComitente);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.query("secuencial", secuencial.toString());
		return Api.response(request, contexto.idCobis(), secuencial);
	}

	// RestInversiones (HB)
	public static ApiResponse obtenerPosicionesNegociablesV2(ContextoHB contexto, String cuentaComitente, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequest request = Api.request("TenenciaPosicionNegociable", "inversiones", "GET",
				"/v2/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
		request.path("id", contexto.idCobis());
		request.path("cuentaComitente", cuentaComitente);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.query("secuencial", secuencial.toString());
		return Api.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponse precioTituloValor(ContextoHB contexto, String codigo, String moneda) {
		ApiRequest request = Api.request("PrecioTituloValor", "inversiones", "GET", "/v1/titulos/precio", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("tipoproducto", codigo);
		request.query("descmoneda", moneda);
		return Api.response(request, contexto.idCobis());
	}

	public static Objeto tituloValor(ContextoHB contexto, String codigo, String fecha) {
		ApiResponse titulosValores = titulosValores(contexto, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			if (objeto.string("codigo").equals(codigo)) {
				return objeto;
			}
		}
		return new Objeto();
	}

	public static List<String> cuentasLiquidacionTitulo(ContextoHB contexto, String numeroCuentaComitente, String idMoneda) {
		ApiRequest request = Api.request("CuentasLiquidacionTitulo", "inversiones", "GET", "/v1/cuentaLiquidacion", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("cuentacomitente", numeroCuentaComitente);
		request.query("tipocuenta", "TITULO");
		request.query("moneda", idMoneda.equals("80") ? "PESOS" : idMoneda.equals("2") ? "USD" : null);
		request.query("desdesecuencialpaginacion", "1");
		request.query("cantidadregistrospaginacion", "300");

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return null;
		}

		List<String> cuentasLiquidacion = new ArrayList<>();
		for (Objeto item : response.objetos("cuentasLiquidacionOrdenadas.CuentaLiquidacionOrdenada")) {
			cuentasLiquidacion.add(item.string("Numero"));
		}
		return cuentasLiquidacion;
	}

	public static List<String> cuentasLiquidacionMonetaria(ContextoHB contexto, String numeroCuentaComitente) {
		List<String> cuentasLiquidacion = new ArrayList<>();
		for (String idMoneda : Objeto.listOf("80", "2")) {
			ApiRequest request = Api.request("CuentasLiquidacionTitulo", "inversiones", "GET", "/v1/cuentaLiquidacion", contexto);
			request.query("idcobis", contexto.idCobis());
			request.query("cuentacomitente", numeroCuentaComitente);
			request.query("tipocuenta", "MONETARIA");
			request.query("moneda", idMoneda.equals("80") ? "PESOS" : idMoneda.equals("2") ? "USD" : null);
			request.query("desdesecuencialpaginacion", "1");
			request.query("cantidadregistrospaginacion", "300");

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError() && !response.string("masInformacion").equals("2070")) {
				return null;
			}

			for (Objeto item : response.objetos("cuentasLiquidacionOrdenadas.CuentaLiquidacionOrdenada")) {
				cuentasLiquidacion.add(item.string("Numero"));
			}
		}
		return cuentasLiquidacion;
	}

	public static ApiResponse indicesBursatiles(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesIndicesBursatiles", "inversiones", "GET", "/v1/indicesbursatiles", contexto);
		request.query("sectorial", "false");
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse indicesSectoriales(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesIndicesSectoriales", "inversiones", "GET", "/v1/indicesbursatiles", contexto);
		request.query("sectorial", "true");
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse indicesBursatilesDelay(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesIndicesBursatilesDelay", "inversiones", "GET", "/v1/indicesbursatilesdelay", contexto);
		request.query("cierreAnterior", "false");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse indicesRealTime(ContextoHB contexto, String codigo, String idPanel, String idVencimiento) {
		ApiRequest request = Api.request("InversionesIndicesRealTime", "inversiones", "GET", "/v1/intradiarias", contexto);
		if (!"".equals(codigo)) {
			request.query("codigo", codigo);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		if (!"".equals(idVencimiento)) {
			request.query("idVencimiento", idVencimiento);
		}
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis(), codigo, idPanel, idVencimiento);
	}

	public static ApiResponse indicesRealTimeV2(ContextoHB contexto, String codigo, String idPanel, String idVencimiento) {
		ApiRequest request = Api.request("InversionesIndicesRealTimeV2", "inversiones", "GET", "/v1/intradiariaslist", contexto);
		if (!"".equals(codigo)) {
			request.query("codigo", codigo);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		if (!"".equals(idVencimiento)) {
			request.query("idVencimiento", idVencimiento);
		}
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis(), codigo, idPanel, idVencimiento);
	}

	public static ApiResponse intradiariasOferta(ContextoHB contexto, String idIntradiaria, String idPanel) {
		ApiRequest request = Api.request("InversionesIntradiariasOferta", "inversiones", "GET", "/v1/intradiariasoferta", contexto);
		if (!"".equals(idIntradiaria)) {
			request.query("idIntradiaria", idIntradiaria);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse intradiariasProfundidad(ContextoHB contexto, String idIntradiaria, String idPanel) {
		ApiRequest request = Api.request("InversionesIntradiariasProfundidad", "inversiones", "GET", "/v1/intradiariasprofundidad", contexto);
		if (!"".equals(idIntradiaria)) {
			request.query("idIntradiaria", idIntradiaria);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		request.permitirSinLogin = !ConfigHB.esProduccion();
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse indicesSectorialesDelay(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesIndicesSectorialesDelay", "inversiones", "GET", "/v1/indicessectorialesdelay", contexto);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse panelesEspecies(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesPanelesEspecies", "inversiones", "GET", "/v1/panelesespecies", contexto);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse especiesPorPanel(ContextoHB contexto, String idPanel) {
		ApiRequest request = Api.request("InversionesEspeciesPorPanel", "inversiones", "GET", "/v1/especies", contexto);
		request.query("idPanel", idPanel);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse panelesCotizacionesDelay(ContextoHB contexto, String idPanel) {
		ApiRequest request = Api.request("InversionesPanelesCotizacionesDelay", "inversiones", "GET", "/v1/panelcotizacionesdelay", contexto);
		request.query("idPanel", idPanel);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse caucionesDelay(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesCaucionesDelay", "inversiones", "GET", "/v1/caucionesdelay", contexto);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse vencimientosEspecies(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesVencimientosEspecies", "inversiones", "GET", "/v1/vencimientosespecies", contexto);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse cuotapartista(ContextoHB contexto, String email, String tipoDoc, String idUsuario, Boolean mostrarCuentasAnuladas, String nroDoc) {
		Objeto body = new Objeto();
		body.set("nombre", "SelectByDoc");
		body.set("pCuotapartista").set("email", email).set("idTpDocIdentidad", tipoDoc).set("idUsuario", idUsuario).set("mostrarCuentasAnuladas", mostrarCuentasAnuladas).set("numDocIdentidad", nroDoc);
		ApiRequest request = Api.request("Cuotapartista", "inversiones", "POST", "/v1/cuotapartista", contexto);
		request.body(body);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse posicionCuotapartista(ContextoHB contexto, String fecha, String idCuotapartista, Integer nroCuotapartista) {
		Objeto body = new Objeto();
		body.set("nombre", "PosicionCuotapartista");
		body.set("pPosicionCuotapartista").set("fecha", fecha).set("idCuotapartista", idCuotapartista).set("numeroCuotapartista", nroCuotapartista);
		ApiRequest request = Api.request("PosicionCuotapartista", "inversiones", "POST", "/v1/posicionCuotapartista", contexto);
		request.cacheSesion = ConfigHB.bool("hb_cache_esco_pPosicionCuotapartista", true);
		request.body(body);
		return Api.response(request, contexto.idCobis(), nroCuotapartista, fecha);
	}

	public static ApiResponse solicitudes(ContextoHB contexto, String fechaDesde, String fechaHasta, String iDAgColocador, String iDCuotapartista, String iDFondo, String iDTpValorCp, String iDUsuario, Integer nroCuotapartista) {

		if (ConfigHB.esHomologacion() && HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "504_test")) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.codigo = 504;
			return apiResponse;
		}

		Objeto body = new Objeto();
		body.set("nombre", "GetAllSolicitudes");
		body.set("pSolicitudSuscripcion").set("FechaDesde", fechaDesde).set("FechaHasta", fechaHasta).set("iDAgColocador", iDAgColocador).set("IDCuotapartista", iDCuotapartista).set("IDFondo", iDFondo).set("IDTpValorCp", iDTpValorCp).set("IDUsuario", iDUsuario).set("NumeroCuotapartista", nroCuotapartista);
		ApiRequest request = Api.request("Solicitudes", "inversiones", "POST", "/v1/solicitudes", contexto);
		request.cacheRequest = true;
		request.body(body);
		return Api.response(request, contexto.idCobis(), fechaDesde, fechaHasta, iDAgColocador, iDCuotapartista, iDFondo, iDTpValorCp, iDUsuario, nroCuotapartista);
	}

	public static ApiResponse solicitudespruebaServiceLayer(ContextoHB contexto, String fechaDesde, String fechaHasta, String iDAgColocador, String iDCuotapartista, String iDFondo, String iDTpValorCp, String iDUsuario, Integer nroCuotapartista) {
		Objeto body = new Objeto();
		body.set("nombre", "GetAllSolicitudes");
		body.set("pSolicitudSuscripcion").set("FechaDesde", fechaDesde).set("FechaHasta", fechaHasta).set("iDAgColocador", iDAgColocador).set("IDCuotapartista", iDCuotapartista).set("IDFondo", iDFondo).set("IDTpValorCp", iDTpValorCp).set("IDUsuario", iDUsuario).set("NumeroCuotapartista", nroCuotapartista);
		ApiRequest request = Api.request("Solicitudes", "inversiones", "POST", "/v1/solicitudes", contexto);
		request.body(body);
		return Api.response(request, contexto);
	}

	public static ApiResponse fondos(ContextoHB contexto, int idcuotapartista, int idtipoValorCuotaParte, Integer numeroDeFondo, String tipoSolicitud) {
		Objeto body = new Objeto();
		body.set("pFondosParaOperar").set("idcuotapartista", idcuotapartista).set("idtipoValorCuotaParte", idtipoValorCuotaParte).set("numeroDeFondo", numeroDeFondo).set("tipoSolicitud", tipoSolicitud);
		ApiRequest request = Api.request("Fondos", "inversiones", "POST", "/v1/fondos", contexto);
		request.body(body);
		request.cacheSesion = ConfigHB.bool("hb_cache_esco_pFondosParaOperar", true);
		request.cacheRequest = true;
		return Api.response(request, contexto.idCobis(), idcuotapartista, tipoSolicitud);
	}

	public static ApiResponse variacionFondos(ContextoHB contexto, String fecha) {
		ApiRequest request = Api.request("variacionFondos", "servicio-fondos", "GET", "/detalle-fondos", contexto);
		request.query("fecha", fecha);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), fecha);
	}

	public static ApiResponse rescate(ContextoHB contexto, Integer cantCuotapartes, String ctaBancaria, String cuotapartista, String esTotal, String fechaAcreditacion, String fechaConcertacion, String formaCobro, String IDSolicitud, String importe, Objeto inversionFondo, String moneda, String porcGastos, String porcGtoBancario, String tpOrigenSol) {
		Objeto body = crearBodyRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion,
				fechaConcertacion, formaCobro, IDSolicitud, importe, inversionFondo, moneda, porcGastos,
				porcGtoBancario, tpOrigenSol);
		ApiRequest request = Api.request("Rescate", "inversiones", "POST", "/v1/rescateSL", contexto);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static Objeto crearBodyRequestRescate(Integer cantCuotapartes, String ctaBancaria, String cuotapartista,
			String esTotal, String fechaAcreditacion, String fechaConcertacion, String formaCobro, String IDSolicitud,
			String importe, Objeto inversionFondo, String moneda, String porcGastos, String porcGtoBancario,
			String tpOrigenSol) {
		Objeto body = new Objeto();
		body.set("pSolicitudRescate").set("CantCuotapartes", cantCuotapartes).set("CtaBancaria", ctaBancaria).set("Cuotapartista", cuotapartista).set("EsTotal", esTotal).set("FechaAcreditacion", fechaAcreditacion).set("FechaConcertacion", fechaConcertacion).set("FormaCobro", formaCobro).set("IDSolicitud", IDSolicitud).set("Importe", importe).set("InversionFondo", inversionFondo).set("Moneda", moneda).set("PorcGastos", porcGastos).set("PorcGtoBancario", porcGtoBancario).set("TpOrigenSol", tpOrigenSol);
		return body;
	}

	public static ApiResponse suscripcion(ContextoHB contexto, Objeto cuentaBancaria, String cuotapartista,
			String fechaAcreditacion, String fechaConcertacion, Objeto formasPagoCuentaBancaria, String IDSolicitud,
			Objeto inversionFondo, String moneda, String tpOrigenSol) {
		Objeto body = crearBodyRequestSuscripcion(cuentaBancaria, cuotapartista, fechaAcreditacion, fechaConcertacion,
				formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, tpOrigenSol);
		ApiRequest request = Api.request("Suscripcion", "inversiones", "POST", "/v1/suscripcionSL", contexto);
		request.body(body);
		return Api.response(request, contexto.idCobis());
	}

	public static Objeto crearBodyRequestSuscripcion(Objeto cuentaBancaria, String cuotapartista,
			String fechaAcreditacion, String fechaConcertacion, Objeto formasPagoCuentaBancaria, String IDSolicitud,
			Objeto inversionFondo, String moneda, String tpOrigenSol) {
		Objeto body = new Objeto();
		body.set("SuscripcionSL").set("AceptacionDocumentacionWEB", null).set("CuentaBancaria", cuentaBancaria)
				.set("Cuotapartista", cuotapartista).set("FechaAcreditacion", fechaAcreditacion)
				.set("FechaConcertacion", fechaConcertacion).set("FormasPagoCuentaBancaria", formasPagoCuentaBancaria)
				.set("IDSolicitud", IDSolicitud).set("InversionFondo", inversionFondo).set("Moneda", moneda)
				.set("TpOrigenSol", tpOrigenSol);
		return body;
	}

	public static ApiResponse rescateAgenda(ContextoHB contexto, LocalDateTime fechaHoraEjecucion, Objeto body) {
		Objeto requestOperacion = new Objeto()					
				.set("tipoRequest", "json")
				.set("tecnologia", "api-rest")
				.set("enviaEmail", "false")
				.set("metodo", "POST")
				.set("url", ConfigHB.string("hb_api_url_inversiones_agenda"))
				.set("recurso", "/v1/rescateSL")				
				.set("servicio", "servicios")
				.set("numeroMaxIntento", "0")
				.set("identificadorHorarioEjecucion", "GENERAL")
				.set("identificadorPeriodicidad", "UNICA")
				.set("fechaHoraEjecuciones", Objeto.listOf(fechaHoraEjecucion));
		
		requestOperacion.set("body", body);
		
		ApiRequest request = Api.request("Rescate", "inversiones", "POST", "/v2/rescateAgenda", contexto);
		request.body(requestOperacion);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse suscripcionAgenda(ContextoHB contexto, LocalDateTime fechaHoraEjecucion, Objeto body) {
		Objeto requestOperacion = new Objeto()					
				.set("tipoRequest", "json")
				.set("tecnologia", "api-rest")
				.set("enviaEmail", "false")
				.set("metodo", "POST")
				.set("url", ConfigHB.string("hb_api_url_inversiones_agenda"))
				.set("recurso", "/v1/suscripcionSL")				
				.set("servicio", "servicios")
				.set("numeroMaxIntento", "0")
				.set("identificadorHorarioEjecucion", "GENERAL")
				.set("identificadorPeriodicidad", "UNICA")
				.set("fechaHoraEjecuciones", Objeto.listOf(fechaHoraEjecucion));
		
		requestOperacion.set("body", body);
		
		ApiRequest request = Api.request("Suscripcion", "inversiones", "POST", "/v2/suscripcionAgenda", contexto);
		request.body(requestOperacion);
		return Api.response(request, contexto.idCobis());
	}
	
	public static ApiResponse tenenciaPosicionesNegociables(ContextoHB contexto, String fecha, Integer secuencial) {
		ApiRequest request = Api.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", "15");
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		return Api.response(request, contexto.idCobis(), secuencial);
	}

	public static Map<String, Objeto> obtenerProductosOperablesMap(ContextoHB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponse titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("codigo"), objeto);
		}
		return productosOperables;
	}

	public static List<Objeto> obtenerProductosOperables(ContextoHB contexto, String fecha) {
		List<Objeto> productosOperables = new ArrayList<>();
		ApiResponse titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.add(objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByProducto(ContextoHB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponse titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("producto"), objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByCodigo(ContextoHB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponse titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("codigo"), objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByProductoPesos(ContextoHB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponse titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			if (objeto.string("descMoneda").equalsIgnoreCase("PESOS")) {
				productosOperables.put(objeto.string("producto"), objeto);
			}
		}
		return productosOperables;
	}

}
