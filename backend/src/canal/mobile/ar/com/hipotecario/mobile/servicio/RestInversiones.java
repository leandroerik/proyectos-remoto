package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.cuentainversor.CuentaInversor;
import ar.com.hipotecario.mobile.negocio.cuentainversor.CuotasBancarias;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.AltaCuentaComitenteRequestUnificado;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentafondos.AltaCuentaFondosRequestUnificado;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class RestInversiones {

	public static ApiResponseMB altaCuentasFondos(ContextoMB contexto, AltaCuentaFondosRequestUnificado cuenta) throws JsonProcessingException {
		ApiRequestMB request = ApiMB.request("altacuentafondos", "altacuentas", "POST", "/v1/paquetecuentacuotapartista", contexto);
		ObjectMapper objectMapper = new ObjectMapper();
		Objeto body = new Objeto();
		String jsonString = cuenta.toJson();
		Object aux = objectMapper.readValue(jsonString, Object.class);
		body.add(aux);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB altaCuentaComitente(ContextoMB contexto, AltaCuentaComitenteRequestUnificado objeto) throws JsonProcessingException {
		ApiRequestMB request = ApiMB.request("altacuentacomitente", "altacuentas", "POST", "/v1/paquetecuentacomitente", contexto);
		ObjectMapper objectMapper = new ObjectMapper();
		Objeto body = new Objeto();
		String jsonString = objeto.toJson();
		Object aux = objectMapper.readValue(jsonString, Object.class);
		body.add(aux);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}//Permitir

	public static ApiResponseMB consultaEstadoInversorByma(ContextoMB contexto, List<String> taskIds) {
		ApiRequestMB request = ApiMB.request("altacuentafondos", "altacuentas", "POST", "/v1/estadoinversorcajavalores",
				contexto);
		Objeto body = new Objeto();
		taskIds.stream().forEach(t -> body.add(t));
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultaEstadoCuentaByma(ContextoMB contexto, String taskId) {
		ApiRequestMB request = ApiMB.request("altacuentafondos", "altacuentas", "GET",
				"/v1/estadocuentacajavalores/{taskId}", contexto);
		request.path("taskId", taskId);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB getInversorCNVByCuit(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("cuentasUnitrade", "altacuentas", "GET", "/v2/buscar-inversor/{cuil}", contexto);
		request.path("cuil", cuit);
		return ApiMB.response(request, contexto.idCobis());
	} //Todo: Usar este servicio para quitar carga al servicio orquestador

	public static ApiResponseMB altaCuentaInversor(ContextoMB contexto, CuentaInversor cuentaInversor) {
		ApiRequestMB request = ApiMB.request("altaCuentas", "inversiones", "POST", "/v1/cuenta", contexto);
		Objeto body = new Objeto();
		Objeto cuenta = new Objeto();
		cuenta.set("idPersonaFondo", cuentaInversor.getIdPersonaFondo());
		cuenta.set("cobis",	cuentaInversor.getCobis());
		cuenta.set("sucursal", cuentaInversor.getSucursal());
		cuenta.set("razonSocial", cuentaInversor.getRazonSocial());
		cuenta.set("tipoIdTributario", cuentaInversor.getTipoIdTributario());
		cuenta.set("cuit", cuentaInversor.getCuit());
		cuenta.set("tipoSujeto",cuentaInversor.getTipoSujeto());
		cuenta.set("calificacion", cuentaInversor.getCalificacion());
		cuenta.set("actividad",	cuentaInversor.getActividad());
		cuenta.set("situacionGanancia",	cuentaInversor.getSituacionGanancia());
		cuenta.set("condicionIva", cuentaInversor.getCondicionIva());
		cuenta.set("direccion", cuentaInversor.getDireccion());
		cuenta.set("telefonoVFNet", cuentaInversor.getTelefonoVFNet());
		cuenta.set("telefonoUnitrade", cuentaInversor.getTelefonoUnitrade());
		cuenta.set("email", cuentaInversor.getEmail());
		cuenta.set("nombre", cuentaInversor.getNombre());
		cuenta.set("apellido", cuentaInversor.getApellido());
		cuenta.set("tipoPersona", cuentaInversor.getTipoPersona());
		cuenta.set("idPersona", cuentaInversor.getIdPersona());

		Objeto domicilio = new Objeto();
		domicilio.set("alturaCalle", cuentaInversor.getDomicilio().getAlturaCalle());
		domicilio.set("calle",	cuentaInversor.getDomicilio().getCalle());
		domicilio.set("cp", cuentaInversor.getDomicilio().getCp());
		domicilio.set("localidad", cuentaInversor.getDomicilio().getLocalidad());
		domicilio.set("pais", cuentaInversor.getDomicilio().getPais());
		domicilio.set("provincia", cuentaInversor.getDomicilio().getProvincia());
		domicilio.set("piso",cuentaInversor.getDomicilio().getPiso());
		domicilio.set("departamento", cuentaInversor.getDomicilio().getDepartamento());
		cuenta.set("domicilio", domicilio);

		cuenta.set("fechaNacimiento", cuentaInversor.getFechaNacimiento());
		if(cuentaInversor.getFechaIngreso() == null){
			cuenta.setNull("fechaIngreso");
		}else{
			cuenta.set("fechaIngreso", cuentaInversor.getFechaIngreso());
		}
		cuenta.set("paisNacimiento", cuentaInversor.getPaisNacimiento());
		cuenta.set("paisNacionalidad", cuentaInversor.getPaisNacionalidad());

		Objeto cuentasLiquidacion = new Objeto();
		Objeto peso = new Objeto();
		peso.set("tipoCuenta",	cuentaInversor.getCuentasLiquidacion().getPeso().getTipoCuenta());
		peso.set("moneda",	cuentaInversor.getCuentasLiquidacion().getPeso().getMoneda());
		peso.set("numero", cuentaInversor.getCuentasLiquidacion().getPeso().getNumero());
		peso.set("sucursal", cuentaInversor.getCuentasLiquidacion().getPeso().getSucursal());
		cuentasLiquidacion.set("peso", peso);

		Objeto dolares = new Objeto();
		if (cuentaInversor.getCuentasLiquidacion().getDolares() != null) {
			dolares.set("tipoCuenta", cuentaInversor.getCuentasLiquidacion().getDolares().getTipoCuenta());
			dolares.set("moneda", cuentaInversor.getCuentasLiquidacion().getDolares().getMoneda());
			dolares.set("numero", cuentaInversor.getCuentasLiquidacion().getDolares().getNumero());
			dolares.set("sucursal", cuentaInversor.getCuentasLiquidacion().getDolares().getSucursal());
			cuentasLiquidacion.set("dolares", dolares);
		}
		cuenta.set("cuentasLiquidacion", cuentasLiquidacion);


		Objeto origen = new Objeto();
		origen.set("agenteColocador",	cuentaInversor.getOrigen().getAgenteColocador());
		origen.set("sucursal", cuentaInversor.getOrigen().getSucursal());
		cuenta.set("origen", origen);

		Objeto radicacion = new Objeto();
		radicacion.set("agenteColocador", cuentaInversor.getRadicacion().getAgenteColocador());
		radicacion.set("sucursal", cuentaInversor.getRadicacion().getSucursal());
		radicacion.set("canalVivienda", cuentaInversor.getRadicacion().getCanalVivienda());
		radicacion.set("oficinaCuenta", cuentaInversor.getRadicacion().getOficinaCuenta());
		cuenta.set("radicacion", radicacion);

		Objeto cuotasBancarias = new Objeto();
		for (CuotasBancarias cb : cuentaInversor.getCuotasBancarias()) {
			Objeto unaCuotaBancaria = new Objeto();
			unaCuotaBancaria.set("alias", cb.getAlias());
			Objeto banco = new Objeto();
			banco.set("id", cb.getBanco().getId());
			unaCuotaBancaria.set("banco",banco);
			unaCuotaBancaria.set("cbu", cb.getCbu());
			unaCuotaBancaria.set("cuitTitular", cb.getCuitTitular());
			unaCuotaBancaria.set("descripcion", cb.getDescripcion());
			unaCuotaBancaria.set("fechaApertura", cb.getFechaApertura());
			unaCuotaBancaria.set("iban", cb.getIban());
			unaCuotaBancaria.set("idCuentaBancaria", cb.getIdCuentaBancaria());
			unaCuotaBancaria.set("idCuentaBancariaSec", cb.getIdCuentaBancariaSec());
			Objeto moneda = new Objeto();
			moneda.set("id", cb.getMoneda().getId());
			unaCuotaBancaria.set("moneda",moneda);
			unaCuotaBancaria.set("numeroCuenta", cb.getNumeroCuenta());
			unaCuotaBancaria.set("numeroSucursal", cb.getNumeroSucursal());
			unaCuotaBancaria.set("requiereFirmaConjunta", cb.getRequiereFirmaConjunta());
			unaCuotaBancaria.set("swift", cb.getSwift());
			Objeto tipoCuentaBancaria = new Objeto();
			tipoCuentaBancaria.set("id", cb.getTipoCuentaBancaria().getId());
			unaCuotaBancaria.set("tipoCuentaBancaria",tipoCuentaBancaria);
			cuotasBancarias.add(unaCuotaBancaria);
		}

		cuenta.set("cuotasBancarias", cuotasBancarias);

		Objeto datosUIF = new Objeto();
		datosUIF.set("monedaImporteEstimado", cuentaInversor.getDatosUIF().getMonedaImporteEstimado());

		cuenta.set("datosUIF", datosUIF);
		cuenta.set("esFisico", cuentaInversor.getEsFisico());
		cuenta.set("esmasculino", cuentaInversor.getEsmasculino());
		cuenta.set("esPEP", cuentaInversor.getEsPEP());
		cuenta.set("imprimeResumenCuenta", cuentaInversor.getImprimeResumenCuenta());
		cuenta.set("poseeInstrPagoPerm", cuentaInversor.getPoseeInstrPagoPerm());
		cuenta.set("requiereFirmaConjunta", cuentaInversor.getRequiereFirmaConjunta());
		cuenta.set("tipoDoc", cuentaInversor.getTipoDoc());
		cuenta.set("numDoc", cuentaInversor.getNumDoc());
		cuenta.set("cuotaPartista",	cuentaInversor.getCuotaPartista());
		cuenta.set("actividadPrincipal", cuentaInversor.getActividadPrincipal());
		cuenta.set("categoria", cuentaInversor.getCategoria());
		cuenta.set("numCuotapartista", cuentaInversor.getNumCuotapartista());
		cuenta.set("segmentoInversion",	cuentaInversor.getSegmentoInversion());
		cuenta.set("tipoCuotapartista",	cuentaInversor.getTipoCuotapartista());
		cuenta.set("tipoInversor", cuentaInversor.getTipoInversor());
		cuenta.set("tipoPerfilRiesgo", cuentaInversor.getTipoPerfilRiesgo());
		cuenta.set("representantes", cuentaInversor.getRepresentantes());
		cuenta.set("tarjetasCredito", cuentaInversor.getTarjetasCredito());
		cuenta.set("idPersonaCondomino", cuentaInversor.getIdPersonaCondomino());
		cuenta.set("perfil", cuentaInversor.getPerfil());
		cuenta.set("patrimonio", cuentaInversor.getPatrimonio());
		if(cuentaInversor.getIdBanco() == null){
			cuenta.setNull("idBanco");
		}else{
			cuenta.set("idBanco", cuentaInversor.getIdBanco());
		}

		cuenta.set("promedioMensual", cuentaInversor.getPromedioMensual());
		cuenta.set("tipoContribuyente", cuentaInversor.getTipoContribuyente());
		cuenta.set("tipoEstadoCivil", cuentaInversor.getTipoEstadoCivil());
		cuenta.set("identTributario", cuentaInversor.getIdentTributario());
		cuenta.set("numIdentificador", cuentaInversor.getNumIdentificador());
		cuenta.set("lugarRegistracion", cuentaInversor.getLugarRegistracion());
		cuenta.set("provExpedicionCI", cuentaInversor.getProvExpedicionCI());
		cuenta.set("residenteExterior", cuentaInversor.getResidenteExterior());
		cuenta.set("identificacionFiscal", cuentaInversor.getIdentificacionFiscal());
		cuenta.set("paisResidenciaFiscal", cuentaInversor.getPaisResidenciaFiscal());

		body.add(cuenta);
		request.body(body);

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB selectPersonaByDoc(ContextoMB contexto, String numDocumento, String idTipoDoc) {
		ApiRequestMB request = ApiMB.request("selectByDocPersona", "inversiones", "POST", "/v1/SelectByDocPersona", contexto);
		Objeto body = new Objeto();
		body.set("numDocIdentidad", numDocumento);
		body.set("codTpDocIdentidad", idTipoDoc);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB seguimientoOperaciones(ContextoMB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequestMB request = ApiMB.request("SeguimientoOperacionesTitulosValores", "inversiones", "GET", "/v1/ordenes", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("cuentacomitente", numeroCuentaComitente);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(fechaDesde));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(fechaHasta));
		request.query("desdepagina", "1");
		request.query("registrosxpagina", "1000");
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponseMB seguimientoLicitaciones(ContextoMB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequestMB request = ApiMB.request("SeguimientoLicitacionesTitulosValores", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/posturas", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(fechaDesde));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(fechaHasta));
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroCuentaComitente);
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

	public static ApiResponseMB suscripcionAgenda(ContextoMB contexto, String fechaHoraEjecucion, Objeto body) {
		Objeto requestOperacion = new Objeto()
				.set("tipoRequest", "json")
				.set("tecnologia", "api-rest")
				.set("enviaEmail", "false")
				.set("metodo", "POST")
				.set("url", ConfigMB.string("mb_api_url_inversiones_agenda"))
				.set("recurso", "/v1/suscripcionSL")
				.set("servicio", "servicios")
				.set("numeroMaxIntento", "0")
				.set("identificadorHorarioEjecucion", "GENERAL")
				.set("identificadorPeriodicidad", "UNICA")
				.set("fechaHoraEjecuciones", Objeto.listOf(fechaHoraEjecucion));

		requestOperacion.set("body", body);

		ApiRequestMB request = ApiMB.request("Suscripcion", "inversiones", "POST", "/v2/suscripcionAgenda", contexto);
		request.body(requestOperacion);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB movimientos(ContextoMB contexto, String numeroCuentaComitente, Date fechaDesde, Date fechaHasta) {
		ApiRequestMB request = ApiMB.request("MovimientosTitulosValores", "inversiones", "GET", "/v1/{cuenta}/extractoComitente", contexto);
		request.path("cuenta", numeroCuentaComitente);
		request.query("idCobis", contexto.idCobis());
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(fechaDesde));
		request.query("fechaFin", new SimpleDateFormat("dd/MM/yyyy").format(fechaHasta));
		request.query("numeroSecuencial", "1");
		request.query("cantidadRegistro", "1000");
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponseMB titulosValores(ContextoMB contexto) {
		return titulosValores(contexto, null);
	}

	public static ApiResponseMB titulosValores(ContextoMB contexto, String idTipoActivo) {
		ApiRequestMB request = ApiMB.request("TitulosValores", "inversiones", "GET", "/v1/titulos/producto", contexto);
		request.query("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("secuencial", "1");
		request.query("cantregistros", "1000");
		request.query("clasificacion", idTipoActivo);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), idTipoActivo);
	}

	public static ApiResponseMB titulosValores(ContextoMB contexto, String idTipoActivo, String fecha) {
		if (fecha.isEmpty()) {
			fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		ApiRequestMB request = ApiMB.request("TitulosValores", "inversiones", "GET", "/v1/titulos/producto", contexto);
		request.query("fecha", fecha);
		request.query("secuencial", "1");
		request.query("cantregistros", "1000");
		request.query("clasificacion", idTipoActivo);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), idTipoActivo);
	}

	public static ApiResponseMB tenenciaTitulosValores(ContextoMB contexto, String numeroCuentaComitente) {
		ApiRequestMB request = ApiMB.request("TenenciaTitulosValores", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/especies", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		request.query("idcliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis(), numeroCuentaComitente);
	}

	public static ApiResponseMB obtenerPosicionesNegociables(ContextoMB contexto, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		return ApiMB.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponseMB obtenerPosicionesNegociables(ContextoMB contexto, String cuentaComitente, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
		request.path("id", contexto.idCobis());
		request.path("cuentaComitente", cuentaComitente);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.query("secuencial", secuencial.toString());
		return ApiMB.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponseMB obtenerPosicionesNegociablesV2(ContextoMB contexto, String cuentaComitente, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v2/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
		request.path("id", contexto.idCobis());
		request.path("cuentaComitente", cuentaComitente);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.query("secuencial", secuencial.toString());
		return ApiMB.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponseMB precioTituloValor(ContextoMB contexto, String codigo, String moneda) {
		ApiRequestMB request = ApiMB.request("PrecioTituloValor", "inversiones", "GET", "/v1/titulos/precio", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("tipoproducto", codigo);
		request.query("descmoneda", moneda);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static Objeto tituloValor(ContextoMB contexto, String codigo) {
		ApiResponseMB titulosValores = titulosValores(contexto);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			if (objeto.string("codigo").equals(codigo)) {
				return objeto;
			}
		}
		return new Objeto();
	}

	public static List<String> cuentasLiquidacionTitulo(ContextoMB contexto, String numeroCuentaComitente, String idMoneda) {
		ApiRequestMB request = ApiMB.request("CuentasLiquidacionTitulo", "inversiones", "GET", "/v1/cuentaLiquidacion", contexto);
		request.query("idcobis", contexto.idCobis());
		request.query("cuentacomitente", numeroCuentaComitente);
		request.query("tipocuenta", "TITULO");
		request.query("moneda", idMoneda.equals("80") ? "PESOS" : idMoneda.equals("2") ? "USD" : null);
		request.query("desdesecuencialpaginacion", "1");
		request.query("cantidadregistrospaginacion", "300");

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return null;
		}

		List<String> cuentasLiquidacion = new ArrayList<>();
		for (Objeto item : response.objetos("cuentasLiquidacionOrdenadas.CuentaLiquidacionOrdenada")) {
			cuentasLiquidacion.add(item.string("Numero"));
		}
		return cuentasLiquidacion;
	}

	public static List<String> cuentasLiquidacionMonetaria(ContextoMB contexto, String numeroCuentaComitente) {
		List<String> cuentasLiquidacion = new ArrayList<>();
		for (String idMoneda : Objeto.listOf("80", "2")) {
			ApiRequestMB request = ApiMB.request("CuentasLiquidacionTitulo", "inversiones", "GET", "/v1/cuentaLiquidacion", contexto);
			request.query("idcobis", contexto.idCobis());
			request.query("cuentacomitente", numeroCuentaComitente);
			request.query("tipocuenta", "MONETARIA");
			request.query("moneda", idMoneda.equals("80") ? "PESOS" : idMoneda.equals("2") ? "USD" : null);
			request.query("desdesecuencialpaginacion", "1");
			request.query("cantidadregistrospaginacion", "300");

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
			if (response.hayError() && !response.string("masInformacion").equals("2070")) {
				return null;
			}

			for (Objeto item : response.objetos("cuentasLiquidacionOrdenadas.CuentaLiquidacionOrdenada")) {
				cuentasLiquidacion.add(item.string("Numero"));
			}
		}
		return cuentasLiquidacion;
	}

	public static ApiResponseMB indicesBursatiles(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesBursatiles", "inversiones", "GET", "/v1/indicesbursatiles", contexto);
		request.query("sectorial", "false");
		request.permitirSinLogin = !ConfigMB.esProduccion();
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB indicesSectoriales(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesSectoriales", "inversiones", "GET", "/v1/indicesbursatiles", contexto);
		request.query("sectorial", "true");
		request.permitirSinLogin = !ConfigMB.esProduccion();
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB indicesBursatilesDelay(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesBursatilesDelay", "inversiones", "GET", "/v1/indicesbursatilesdelay", contexto);
		request.query("cierreAnterior", "false");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB indicesRealTime(ContextoMB contexto, String codigo, String idPanel, String idVencimiento) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesRealTime", "inversiones", "GET", "/v1/intradiarias", contexto);
		if (!"".equals(codigo)) {
			request.query("codigo", codigo);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		if (!"".equals(idVencimiento)) {
			request.query("idVencimiento", idVencimiento);
		}
		request.permitirSinLogin = !ConfigMB.esProduccion();
		return ApiMB.response(request, contexto.idCobis(), codigo, idPanel, idVencimiento);
	}

	public static ApiResponseMB intradiariasOferta(ContextoMB contexto, String idIntradiaria, String idPanel) {
		ApiRequestMB request = ApiMB.request("InversionesIntradiariasOferta", "inversiones", "GET", "/v1/intradiariasoferta", contexto);
		if (!"".equals(idIntradiaria)) {
			request.query("idIntradiaria", idIntradiaria);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		request.permitirSinLogin = !ConfigMB.esProduccion();
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB intradiariasProfundidad(ContextoMB contexto, String idIntradiaria, String idPanel) {
		ApiRequestMB request = ApiMB.request("InversionesIntradiariasProfundidad", "inversiones", "GET", "/v1/intradiariasprofundidad", contexto);
		if (!"".equals(idIntradiaria)) {
			request.query("idIntradiaria", idIntradiaria);
		}
		if (!"".equals(idPanel)) {
			request.query("idPanel", idPanel);
		}
		request.permitirSinLogin = !ConfigMB.esProduccion();
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB indicesSectorialesDelay(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesSectorialesDelay", "inversiones", "GET", "/v1/indicessectorialesdelay", contexto);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB panelesEspecies(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesPanelesEspecies", "inversiones", "GET", "/v1/panelesespecies", contexto);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB especiesPorPanel(ContextoMB contexto, String idPanel) {
		ApiRequestMB request = ApiMB.request("InversionesEspeciesPorPanel", "inversiones", "GET", "/v1/especies", contexto);
		request.query("idPanel", idPanel);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB panelesCotizacionesDelay(ContextoMB contexto, String idPanel) {
		ApiRequestMB request = ApiMB.request("InversionesPanelesCotizacionesDelay", "inversiones", "GET", "/v1/panelcotizacionesdelay", contexto);
		request.query("idPanel", idPanel);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB caucionesDelay(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesCaucionesDelay", "inversiones", "GET", "/v1/caucionesdelay", contexto);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB vencimientosEspecies(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesVencimientosEspecies", "inversiones", "GET", "/v1/vencimientosespecies", contexto);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB tenenciaHistorico(ContextoMB contexto, String fechaDesde, String fechaHasta, String codigo, String plazo) {
		ApiRequestMB request = ApiMB.request("InversionesCotizacionesHistoricas", "inversiones", "GET", "/v1/cotizacioneshistoricas", contexto);
		request.query("fechaDesde", fechaDesde);
		request.query("fechaHasta", fechaHasta);
		request.query("simbolo", codigo);
		request.query("idVencimiento", plazo);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB cuotapartista(ContextoMB contexto, String email, String tipoDoc, String idUsuario, Boolean mostrarCuentasAnuladas, String nroDoc) {
		Objeto body = new Objeto();
		body.set("nombre", "SelectByDoc");
		body.set("pCuotapartista").set("email", email).set("idTpDocIdentidad", tipoDoc).set("idUsuario", idUsuario).set("mostrarCuentasAnuladas", mostrarCuentasAnuladas).set("numDocIdentidad", nroDoc);
		ApiRequestMB request = ApiMB.request("Cuotapartista", "inversiones", "POST", "/v1/cuotapartista", contexto);
		request.body(body);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB formulario(ContextoMB contexto, String idAgColocador, String idFondo, String idSucursal, String idTpFormulario, String pCodLiquidacion, String pNumSolicitud) {
		Objeto body = new Objeto();
		body.set("nombre", "GetFormulario");
		body.set("pSharedReportsFormularioModelRequest").set("esConsultaNG", false).set("idAgColocador", idAgColocador).set("idFondo", idFondo).set("idSucursal", idSucursal).set("idTpFormulario", idTpFormulario).set("pCodLiquidacion", pCodLiquidacion).set("pNumSolicitud", pNumSolicitud);
		ApiRequestMB request = ApiMB.request("Formulario", "inversiones", "POST", "/v1/formulario", contexto);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB liquidaciones(ContextoMB contexto, String fechaDesde, String fechaHasta, String iDCuotapartista, String iDFondo, String iDTipoValorCuotaParte, Integer nroCuotapartista) {
		Objeto body = new Objeto();
		body.set("nombre", "Liquidaciones");
		body.set("pLiquidaciones").set("FechaDesde", fechaDesde).set("FechaHasta", fechaHasta).set("IDCuotapartista", iDCuotapartista).set("IDFondo", iDFondo).set("IDTipoValorCuotaParte", iDTipoValorCuotaParte).set("NumeroCuotapartista", nroCuotapartista);
		ApiRequestMB request = ApiMB.request("Liquidaciones", "inversiones", "POST", "/v1/liquidaciones", contexto);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB posicionCuotapartista(ContextoMB contexto, String fecha, String idCuotapartista, Integer nroCuotapartista) {
		Objeto body = new Objeto();
		body.set("nombre", "PosicionCuotapartista");
		body.set("pPosicionCuotapartista").set("fecha", fecha).set("idCuotapartista", idCuotapartista).set("numeroCuotapartista", nroCuotapartista);
		ApiRequestMB request = ApiMB.request("PosicionCuotapartista", "inversiones", "POST", "/v1/posicionCuotapartista", contexto);
		request.body(body);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), fecha, idCuotapartista, nroCuotapartista);
	}

	public static ApiResponseMB solicitudes(ContextoMB contexto, String fechaDesde, String fechaHasta, String iDAgColocador, String iDCuotapartista, String iDFondo, String iDTpValorCp, String iDUsuario, Integer nroCuotapartista) {

		if (ConfigMB.esHomologacion() && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "504_test")) {
			ApiResponseMB apiResponse = new ApiResponseMB();
			apiResponse.codigo = 504;
			return apiResponse;
		}

		Objeto body = new Objeto();
		body.set("nombre", "GetAllSolicitudes");
		body.set("pSolicitudSuscripcion").set("FechaDesde", fechaDesde).set("FechaHasta", fechaHasta).set("iDAgColocador", iDAgColocador).set("IDCuotapartista", iDCuotapartista).set("IDFondo", iDFondo).set("IDTpValorCp", iDTpValorCp).set("IDUsuario", iDUsuario).set("NumeroCuotapartista", nroCuotapartista);
		ApiRequestMB request = ApiMB.request("Solicitudes", "inversiones", "POST", "/v1/solicitudes", contexto);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB fondos(ContextoMB contexto, int idcuotapartista, int idtipoValorCuotaParte, Integer numeroDeFondo, String tipoSolicitud) {
		Objeto body = new Objeto();
		body.set("pFondosParaOperar").set("idcuotapartista", idcuotapartista).set("idtipoValorCuotaParte", idtipoValorCuotaParte).set("numeroDeFondo", numeroDeFondo).set("tipoSolicitud", tipoSolicitud);
		ApiRequestMB request = ApiMB.request("Fondos", "inversiones", "POST", "/v1/fondos", contexto);
		request.body(body);
		request.cacheSesion = ConfigMB.bool("mb_cache_esco_pFondosParaOperar", true);
		return ApiMB.response(request, contexto.idCobis(), idcuotapartista, tipoSolicitud);
	}

	public static ApiResponseMB variacionFondos(ContextoMB contexto, String fecha) {
		ApiRequestMB request = ApiMB.request("variacionFondos", "servicio-fondos", "GET", "/detalle-fondos", contexto);
		request.query("fecha", fecha);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), fecha);
	}

	public static ApiResponseMB rescate(ContextoMB contexto, Integer cantCuotapartes, String ctaBancaria, String cuotapartista, String esTotal, String fechaAcreditacion, String fechaConcertacion, String formaCobro, String IDSolicitud, String importe, Objeto inversionFondo, String moneda, String porcGastos, String porcGtoBancario, String tpOrigenSol) {
		Objeto body = crearBodyRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion,
				fechaConcertacion, formaCobro, IDSolicitud, importe, inversionFondo, moneda, porcGastos,
				porcGtoBancario, tpOrigenSol);
		ApiRequestMB request = ApiMB.request("Rescate", "inversiones", "POST", "/v1/rescateSL", contexto);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static Objeto crearBodyRequestRescate(Integer cantCuotapartes, String ctaBancaria, String cuotapartista,
												 String esTotal, String fechaAcreditacion, String fechaConcertacion, String formaCobro, String IDSolicitud,
												 String importe, Objeto inversionFondo, String moneda, String porcGastos, String porcGtoBancario,
												 String tpOrigenSol) {
		Objeto body = new Objeto();
		body.set("pSolicitudRescate").set("CantCuotapartes", cantCuotapartes).set("CtaBancaria", ctaBancaria).set("Cuotapartista", cuotapartista).set("EsTotal", esTotal).set("FechaAcreditacion", fechaAcreditacion).set("FechaConcertacion", fechaConcertacion).set("FormaCobro", formaCobro).set("IDSolicitud", IDSolicitud).set("Importe", importe).set("InversionFondo", inversionFondo).set("Moneda", moneda).set("PorcGastos", porcGastos).set("PorcGtoBancario", porcGtoBancario).set("TpOrigenSol", tpOrigenSol);
		return body;
	}

	public static ApiResponseMB rescateAgenda(ContextoMB contexto, String fechaHoraEjecucion, Objeto body) {
		Objeto requestOperacion = new Objeto()
				.set("tipoRequest", "json")
				.set("tecnologia", "api-rest")
				.set("enviaEmail", "false")
				.set("metodo", "POST")
				.set("url", ConfigMB.string("mb_api_url_inversiones_agenda"))
				.set("recurso", "/v1/rescateSL")
				.set("servicio", "servicios")
				.set("numeroMaxIntento", "0")
				.set("identificadorHorarioEjecucion", "GENERAL")
				.set("identificadorPeriodicidad", "UNICA")
				.set("fechaHoraEjecuciones", Objeto.listOf(fechaHoraEjecucion));

		requestOperacion.set("body", body);

		ApiRequestMB request = ApiMB.request("Rescate", "inversiones", "POST", "/v2/rescateAgenda", contexto);
		request.body(requestOperacion);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB suscripcion(ContextoMB contexto, Objeto cuentaBancaria, String cuotapartista, String fechaAcreditacion, String fechaConcertacion, Objeto formasPagoCuentaBancaria, String IDSolicitud, Objeto inversionFondo, String moneda, String tpOrigenSol) {
		Objeto body = new Objeto();
		body.set("SuscripcionSL").set("AceptacionDocumentacionWEB", null).set("CuentaBancaria", cuentaBancaria).set("Cuotapartista", cuotapartista).set("FechaAcreditacion", fechaAcreditacion).set("FechaConcertacion", fechaConcertacion).set("FormasPagoCuentaBancaria", formasPagoCuentaBancaria).set("IDSolicitud", IDSolicitud).set("InversionFondo", inversionFondo).set("Moneda", moneda).set("TpOrigenSol", tpOrigenSol);
		ApiRequestMB request = ApiMB.request("Suscripcion", "inversiones", "POST", "/v1/suscripcionSL", contexto);
		request.body(body);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB tenenciaPosicionesNegociables(ContextoMB contexto, String fecha, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", "15");
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		return ApiMB.response(request, contexto.idCobis(), secuencial);
	}

	public static Map<String, Objeto> obtenerProductosOperablesMap(ContextoMB contexto) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("codigo"), objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByProducto(ContextoMB contexto) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("producto"), objeto);
		}
		return productosOperables;
	}

	public static List<Objeto> obtenerProductosOperables(ContextoMB contexto) {
		List<Objeto> productosOperables = new ArrayList<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.add(objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByProductoFecha(ContextoMB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("producto"), objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByCodigo(ContextoMB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			productosOperables.put(objeto.string("codigo"), objeto);
		}
		return productosOperables;
	}

	public static Map<String, Objeto> obtenerProductosOperablesMapByProductoPesos(ContextoMB contexto, String fecha) {
		Map<String, Objeto> productosOperables = new HashMap<>();
		ApiResponseMB titulosValores = titulosValores(contexto, null, fecha);
		for (Objeto objeto : titulosValores.objetos("productosOperablesOrdenados")) {
			if (objeto.string("descMoneda").equalsIgnoreCase("PESOS")) {
				productosOperables.put(objeto.string("producto"), objeto);
			}
		}
		return productosOperables;
	}

	public static ApiResponseMB obtenerPosicionesNegociablesCache(ContextoMB contexto, String cantidadRegistros, String fecha, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("TenenciaPosicionNegociable", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.query("cantregistros", cantidadRegistros);
		request.query("fecha", fecha);
		request.path("id", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), secuencial);
	}

	public static ApiResponseMB indicesRealTimeV2(ContextoMB contexto, String codigo, String idPanel, String idVencimiento) {
		ApiRequestMB request = ApiMB.request("InversionesIndicesRealTimeV2", "inversiones", "GET", "/v1/intradiariaslist", contexto);
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
		return ApiMB.response(request, contexto.idCobis(), codigo, idPanel, idVencimiento);
	}
}



