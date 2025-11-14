package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBPersona;
import ar.com.hipotecario.mobile.api.MBSalesforce;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class RestPersona {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB personas(ContextoMB contexto, String documento, String idTipoDocumento) {
		ApiRequestMB request = ApiMB.request("Persona", "personas", "GET", "/personas", contexto);
		request.query("nroDocumento", documento);
		if (idTipoDocumento != null) {
			request.query("idTipoDocumento", idTipoDocumento);
		}
		request.query("consultaCuil", "false");
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return ApiMB.response(request, documento);
	}

	public static ApiResponseMB clientes(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
		request.path("idCliente", contexto.idCobis());
		request.permitirSinLogin = true;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	// emm-20190614--> necesito una función que consulta un cliente de forma
	// específica
	public static ApiResponseMB consultarClienteEspecifico(ContextoMB contexto, String idCobis) {
		ApiRequestMB request = ApiMB.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
		request.path("idCliente", idCobis);
		request.cacheSesion = true;
		return ApiMB.response(request, idCobis);
	}

	public static ApiResponseMB consultarPersonaEspecifica(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("PersonaTercero", "personas", "GET", "/personas/{cuit}", contexto);
		request.path("cuit", cuit);
		request.cacheSesion = true;
		request.permitirSinLogin = true;
		ApiResponseMB response = ApiMB.response(request, cuit);
		return response;

//		ApiResponse response = null;
//		for (String tipoIdTributaria : Objeto.listOf("08", "09", "11", "12")) {
//			ApiRequest request = Api.request("PersonaTercero", "personas", "GET", "/personas", contexto);
//			request.query("numeroIdTributaria", cuit);
//			request.query("tipoIdTributaria", tipoIdTributaria);
//			request.query("consultaCuil", "false");
//			request.cacheSesion = true;
//			response = Api.response(request, cuit, tipoIdTributaria);
//			if (!response.hayError()) {
//				return response;
//			}
//		}
//		return response;
	}

	public static ApiResponseMB consultarActividades(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("Actividades", "personas", "GET", "/personas/{id}/actividades", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.cacheSesion = true;
		return ApiMB.response(request);
	}

	public static ApiResponseMB actualizarActividad(ContextoMB contexto, Integer idActividad, String idSituacionLaboral, String idProfesion, String idRamo, String idCargo, BigDecimal ingresoNeto, Boolean esPrincipal) {
		ApiRequestMB request = null;
		if (idActividad == null) { // si viene nulo doy de alta la actividad sino la actualizo
			request = ApiMB.request("ActividadesAlta", "personas", "POST", "/personas/{id}/actividades", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
			request.path("id", contexto.persona().cuit());
		} else {
			request = ApiMB.request("ActividadesModificacion", "personas", "PATCH", "/actividades/{id}", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
			request.path("id", idActividad.toString());
			request.body("id", idActividad);
		}

		request.body("idSituacionLaboral", idSituacionLaboral);
		if (!"".equals(idProfesion))
			request.body("idProfesion", idProfesion);
		if (!"".equals(idRamo))
			request.body("idRamo", idRamo);
		if (!"".equals(idCargo))
			request.body("idCargo", idCargo);
		if (ingresoNeto != null)
			request.body("ingresoNeto", ingresoNeto);
		if (esPrincipal != null)
			request.body("esPrincipal", esPrincipal);

		return ApiMB.response(request);
	}

	public static ApiResponseMB actividades(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("Actividad", "personas", "GET", "/personas/{id}/actividades", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB emails(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("Email", "personas", "GET", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB emails(ContextoMB contexto, String cuit, Boolean cached) {
		ApiRequestMB request = ApiMB.request("Email", "personas", "GET", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = cached;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB telefonos(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("Telefono", "personas", "GET", "/personas/{id}/telefonos", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB telefonos(ContextoMB contexto, String cuit, Boolean cached) {
		ApiRequestMB request = ApiMB.request("Telefono", "personas", "GET", "/personas/{id}/telefonos", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = cached;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB domicilios(ContextoMB contexto, String cuit) {
		ApiRequestMB request = ApiMB.request("Domicilio", "personas", "GET", "/personas/{id}/domicilios", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB actualizarEmail(ContextoMB contexto, String cuit, String email) {
		ApiMB.eliminarCache(contexto, "Email", contexto.idCobis());
		ApiRequestMB request = ApiMB.request("ActualizarEmail", "personas", "POST", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
		request.path("id", cuit);
		request.body("canalModificacion", "MB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
		request.body("idTipoMail", "EMP");
		request.body("direccion", email);
		request.permitirSinLogin = true;
		// TODO: guardar cambios de datos del usuario
		// return Api.response(request, contexto.idCobis());
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError() && cuit.equals(contexto.persona().cuit())) {
			contexto.insertarContador("CAMBIO_MAIL");
		}
		return response;

	}

	public static String compararCelularActualizado(ContextoMB contexto, String celularCodigoArea, String celularCaracteristica, String celularNumero) {
		if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
			return "";
		}

		Objeto celularAnterior = RestPersona.celular(contexto, contexto.persona().cuit());
		if (celularAnterior == null) {
			return "celular";
		}
		;

		if (!celularAnterior.string("codigoArea").equals(celularCodigoArea) || !celularAnterior.string("caracteristica").equals(celularCaracteristica) || !celularAnterior.string("numero").equals(celularNumero)) {
			return "celular";
		}

		return "";
	}

	public static String compararDomicilioActualizado(ContextoMB contexto, String calle, String altura, String piso, String departamento, String idProvincia, String idLocalidad, String codigoPostal) {

		if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
			return "";
		}

		Objeto domicilioAnterior = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
		String modificacionAux = "";
		if (domicilioAnterior == null) {
			modificacionAux = "calle, altura, piso, departamento, provincia, localidad, código postal";
		} else {
			modificacionAux += (!calle.equals(domicilioAnterior.string("calle")) ? "calle, " : "");
			modificacionAux += (!altura.equals(domicilioAnterior.string("numero")) ? "altura, " : "");
			modificacionAux += (!piso.equals(domicilioAnterior.string("piso")) ? "piso, " : "");
			modificacionAux += (!departamento.equals(domicilioAnterior.string("departamento")) ? "departamento, " : "");
			modificacionAux += (!idProvincia.equals(domicilioAnterior.integer("idProvincia", 1).toString()) ? "provincia, " : "");
			modificacionAux += (!idLocalidad.equals(domicilioAnterior.integer("idCiudad", 146).toString()) ? "localidad, " : "");
			modificacionAux += (!codigoPostal.equals(domicilioAnterior.string("idCodigoPostal")) ? "código postal, " : "");
			if (!modificacionAux.equals("")) {
				modificacionAux = modificacionAux.substring(0, modificacionAux.length() - 2);
			}
		}
		return modificacionAux;
	}

	public static void enviarMailActivacionOtp(ContextoMB contexto) {
		try {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
				Objeto parametros = new Objeto();
				parametros.set("Subject", "Se realizó la activación de OTP");
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
				Date hoy = new Date();
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
				parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
				parametros.set("CANAL", "Banca Móvil");
				parametros.set("TITULAR_CANAL", contexto.persona().apellido());

				RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_activacion_otp"), parametros);
			}
		} catch (Exception e) {
		}
	}

	public static void enviarMailActualizacionDatosPersonales(ContextoMB contexto, String modificacion) {
		if ("".equals(modificacion)) {
			return;
		}
		try {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
				Objeto parametros = new Objeto();
				parametros.set("Subject", "Se modificaron datos personales");
				parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
				Date hoy = new Date();
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
				parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
				parametros.set("MODULO", modificacion);
				parametros.set("CANAL", "Banca Móvil");

				RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_edicion_datos_personales"), parametros);
			}
		} catch (Exception e) {
		}
	}

	public static String compararMailActualizado(ContextoMB contexto, String email) {
		if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
			return "";
		}

		String mailAnterior = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
		if (mailAnterior == null) {
			return "email";
		}
		;

		return (mailAnterior.equals(email) ? "" : "email");
	}

	public static ApiResponseMB actualizarCelular(ContextoMB contexto, String cuit, String codigoArea, String caracteristica, String numero) {
		Objeto celular = celular(contexto, cuit);
		ApiMB.eliminarCache(contexto, "Telefono", contexto.idCobis());

		// TODO: guardar cambios de datos del usuario
		ApiRequestMB request = null;

		if(codigoArea.length() + caracteristica.length() != 7){
			String caracteristicaAux = caracteristica;
			caracteristica = caracteristicaAux.substring(0, 7 - codigoArea.length());
			numero = caracteristicaAux.substring(7 - codigoArea.length()) + numero;
		}

		if (celular == null) {
			// TODO: guardar cambios de datos del usuario
			// ApiRequest request = Api.request("CrearCelular", "personas", "POST",
			// "/personas/{id}/telefonos", contexto);
			request = ApiMB.request("CrearCelular", "personas", "POST", "/personas/{id}/telefonos", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", cuit);
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("codigoPais", "54");
			request.body("prefijo", "15");
			// TODO: guardar cambios de datos del usuario
			// return Api.response(request, contexto.idCobis());
		} else {
			// TODO: guardar cambios de datos del usuario
			// ApiRequest request = Api.request("ActualizarCelular", "personas", "PATCH",
			// "/telefonos/{id}", contexto);
			request = ApiMB.request("ActualizarCelular", "personas", "PATCH", "/telefonos/{id}", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", celular.string("id"));
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("codigoPais", "54");
			request.body("prefijo", "15");
			// TODO: guardar cambios de datos del usuario
			// return Api.response(request, contexto.idCobis());
		}
		request.permitirSinLogin = true;
		// TODO: guardar cambios de datos del usuario
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError() && contexto.persona().cuit().equals(cuit)) {
			contexto.insertarContador("CAMBIO_TELEFONO");
		}
		return response;

	}

	public static ApiResponseMB actualizarDomicilio(ContextoMB contexto, String cuit, Objeto datos, String tipoDomicilio) {

		if (tipoDomicilio == null || "".equals(tipoDomicilio)) {
			tipoDomicilio = "DP";
		}

		// Objeto domicilio = domicilioPostal(contexto, cuit);
		Objeto domicilio = domicilioPorTipo(contexto, cuit, tipoDomicilio);
		ApiMB.eliminarCache(contexto, "Domicilio", contexto.idCobis());

		if (domicilio == null) {
			ApiRequestMB request = ApiMB.request("CrearDomicilio", "personas", "POST", "/personas/{id}/domicilios", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", cuit);
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoDomicilio", tipoDomicilio);
			request.body("calle", datos.string("calle"));
			request.body("numero", datos.integer("numero"));
			request.body("piso", datos.string("piso"));
			request.body("departamento", datos.string("departamento"));
			request.body("calleEntre1", datos.string("calleEntre1"));
			request.body("calleEntre2", datos.string("calleEntre2"));
			request.body("idPais", 80);
			request.body("idProvincia", datos.integer("idProvincia"));
			request.body("idCiudad", datos.integer("idCiudad"));
			request.body("idCodigoPostal", datos.string("idCodigoPostal"));
			return ApiMB.response(request, contexto.idCobis());
		} else {
			ApiRequestMB request = ApiMB.request("ActualizarDomicilio", "personas", "PATCH", "/domicilios/{id}", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", domicilio.string("id"));
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoDomicilio", tipoDomicilio);
			request.body("calle", datos.string("calle"));
			request.body("numero", datos.integer("numero"));
			request.body("piso", datos.string("piso"));
			request.body("departamento", datos.string("departamento"));
			request.body("calleEntre1", datos.string("calleEntre1"));
			request.body("calleEntre2", datos.string("calleEntre2"));
			request.body("idPais", 80);
			request.body("idProvincia", datos.integer("idProvincia"));
			request.body("idCiudad", datos.integer("idCiudad"));
			request.body("idCodigoPostal", datos.string("idCodigoPostal"));
			return ApiMB.response(request, contexto.idCobis());
		}
	}

	public static ApiResponseMB perfilInversor(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PerfilInversor", "personas", "GET", "/perfilInversor", contexto);
		request.query("clientes", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB actualizaPerfilInversor(ContextoMB contexto, String perfilInversor, String operacion) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 365); // emm: por ahora lo hardcodeo, pero tiene que ser variable. Tendríamos
													// que
													// agregar una variable de entorno.
		SimpleDateFormat fechaHasta = new SimpleDateFormat("yyyy-MM-dd");
		fechaHasta.setTimeZone(calendar.getTimeZone());

		ApiRequestMB request = ApiMB.request("AltaPerfilInversorPropioRiesgo", "personas", "POST", "/administrarPerfil", contexto);
		request.body("ente", contexto.idCobis());
		request.body("estado", "A");
		request.body("fechaAMPerfil", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.body("fechaFin", fechaHasta.format(calendar.getTime()));
		request.body("fuenteOrigen", "W");
		request.body("operacion", operacion);
		request.body("perfilInversor", perfilInversor);

		return ApiMB.response(request);
	}

	public static ApiResponseMB segmentacion(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("SegmentacionCliente", "personas", "GET", "/segmentacionCliente", contexto);
		request.query("cuit", contexto.persona().cuit());
		return ApiMB.response(request);
	}

	public static ApiResponseMB convenios(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("ConveniosCliente", "recaudaciones", "GET", "/v1/convenios", contexto);
		request.query("cuit", contexto.persona().cuit());
		request.query("operacion", "S");
		return ApiMB.response(request);
	}

	/* ========== PERSONA ========== */

	public static List<Objeto> personas(ContextoMB contexto, String documento, String idTipoDocumento, String idSexo) {
		ApiResponseMB response = personas(contexto, documento, idTipoDocumento);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			return null;
		}
		List<Objeto> lista = new ArrayList<>();
		if (!response.hayError()) {
			for (Objeto persona : response.objetos()) {
				if (!persona.string("idCliente").isEmpty()) {
					if (idTipoDocumento == null || idTipoDocumento.equals(persona.string("tipoDocumento"))) {
						if (idSexo == null || idSexo.equals(persona.string("sexo"))) {
							lista.add(persona);
						}
					}
				}
			}
		}
		return lista;
	}

	public static List<Objeto> personasParaCuil(ContextoMB contexto, String documento, String idTipoDocumento, String idSexo) {
		ApiResponseMB response = personas(contexto, documento, idTipoDocumento);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			return null;
		}
		List<Objeto> lista = new ArrayList<>();
		if (!response.hayError()) {
			for (Objeto persona : response.objetos()) {
				if (!persona.string("idCliente").isEmpty()) {
					// if (idTipoDocumento == null ||
					// idTipoDocumento.equals(persona.string("tipoDocumento"))) {
					if (idSexo == null || idSexo.equals(persona.string("sexo"))) {
						lista.add(persona);
					}
					// }
				}
			}
		}
		return lista;
	}

	public static List<String> listaIdCobisParaCuil(ContextoMB contexto, String documento, String idTipoDocumento, String idSexo) {
		List<String> listaIdCobis = new ArrayList<>();
		List<Objeto> personas = personasParaCuil(contexto, documento, idTipoDocumento, idSexo);
		if (personas == null) {
			return null;
		}
		for (Objeto persona : personas) {
			listaIdCobis.add(persona.string("idCliente"));
		}
		return listaIdCobis;
	}

	public static List<String> listaIdCobis(ContextoMB contexto, String documento, String idTipoDocumento, String idSexo) {
		List<String> listaIdCobis = new ArrayList<>();
		List<Objeto> personas = personas(contexto, documento, idTipoDocumento, idSexo);
		if (personas == null) {
			return null;
		}
		for (Objeto persona : personas) {
			listaIdCobis.add(persona.string("idCliente"));
		}
		return listaIdCobis;
	}

	public static String cuitConyuge(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponseMB response = ApiMB.response(request);
		for (Objeto item : response.objetos()) {
			if (item.integer("idTipoRelacion", 0).equals(2)) {
				if (item.string("fechaFinRelacion", null) == null) {
					return item.string("cuitPersonaRelacionada");
				}
			}
		}
		return null;
	}

	public static List<String> cuitsRelacionados(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponseMB response = ApiMB.response(request);

		List<String> lista = new ArrayList<>();
		for (Objeto item : response.objetos()) {
			String cuit = item.string("cuitPersonaRelacionada");
			lista.add(cuit);
		}
		return lista;
	}

	/* ========== EMAIL ========== */
	public static Map<String, Objeto> mapaEmails(ContextoMB contexto, String cuit) {
		Boolean bool = contexto.parametros.bool("tomarDatosCache");
		ApiResponseMB response;
		if (bool == null || bool) {
			response = emails(contexto, cuit);
		} else {
			response = emails(contexto, cuit, false);
		}
//		ApiResponse response = emails(contexto, cuit);
		Map<String, Objeto> emails = new HashMap<>();
		Map<String, Date> fechasModificacion = new HashMap<>();
		for (Objeto item : response.objetos()) {
			String tipo = item.string("idTipoMail");
			Date fechaModificacion = fechasModificacion.get(tipo);
			Date fechaModificacionActual = item.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", item.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss"));
			if (fechaModificacion == null || fechaModificacion.getTime() < fechaModificacionActual.getTime()) {
				if (!Objeto.setOf("BATCH", "EMERIX").contains(item.string("canalModificacion"))) {
					emails.put(tipo, item);
					fechasModificacion.put(tipo, fechaModificacionActual);
				}
			}
		}
		return emails;
	}

	public static Objeto email(ContextoMB contexto, String cuit) {
		if (cuit != null && !cuit.trim().isEmpty()) {
			return mapaEmails(contexto, cuit).get("EMP");
		}
		return null;
	}

	public static String direccionEmail(ContextoMB contexto, String cuit) {
		String direccionEmail = null;
		Objeto email = null;
		if (cuit != null && !cuit.trim().isEmpty()) {
			email = email(contexto, cuit);
		}
		if (email != null) {
			contexto.sesion().setModificacionMail(email.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", email.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion().setModificacionMailCanal(email.string("canalModificacion"));
			direccionEmail = email.string("direccion");
		}
		return direccionEmail;
	}

	public static Integer idEmail(ContextoMB contexto, String cuit) {
		Integer id = 0;
		Objeto email = email(contexto, cuit);
		if (email != null) {
			contexto.sesion().setModificacionMail(email.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", email.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion().setModificacionMailCanal(email.string("canalModificacion"));
			id = email.integer("idCore");
		}
		return id;
	}

	/* ========== ACTIVIDADES LABORALES ========== */
	public static Map<String, Objeto> mapaActividades(ContextoMB contexto, String cuit) {
		ApiResponseMB response = actividades(contexto, cuit);
		Map<String, Objeto> actividades = new HashMap<>();
		Map<String, Date> fechasModificacion = new HashMap<>();
		for (Objeto item : response.objetos()) {
			if (item.string("fechaEgresoActividad").isEmpty()) {
				String tipo = item.string("idSituacionLaboral");
				Date fechaModificacion = fechasModificacion.get(tipo);
				Date fechaModificacionActual = item.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", item.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss"));
				if (fechaModificacion == null || fechaModificacion.getTime() < fechaModificacionActual.getTime()) {
					actividades.put(tipo, item);
					fechasModificacion.put(tipo, fechaModificacionActual);
				}
			}
		}
		return actividades;
	}

	/* ========== TELEFONO ========== */
	public static Map<String, Objeto> mapaTelefonos(ContextoMB contexto, String cuit) {
		ApiResponseMB response;
		Boolean bool = contexto.parametros.bool("tomarDatosCache");
		if (bool == null || bool) {
			response = telefonos(contexto, cuit);
		} else {
			response = telefonos(contexto, cuit, false);
		}
//		ApiResponse response = telefonos(contexto, cuit);
		Map<String, Objeto> telefonos = new HashMap<>();
		Map<String, Date> fechasModificacion = new HashMap<>();
		for (Objeto item : response.objetos()) {
			String tipo = item.string("idTipoTelefono");
			Date fechaModificacion = fechasModificacion.get(tipo);
			Date fechaModificacionActual = item.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", item.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss"));
			if (fechaModificacion == null || fechaModificacion.getTime() < fechaModificacionActual.getTime()) {
				if (!Objeto.setOf("BATCH", "EMERIX").contains(item.string("canalModificacion"))) {
					telefonos.put(tipo, item);
					fechasModificacion.put(tipo, fechaModificacionActual);
				} else {
					if ("E".equalsIgnoreCase(tipo)) {
						telefonos.put("ERROR", item);
					}
				}
			}
		}
		return telefonos;
	}

	public static Objeto celular(ContextoMB contexto, String cuit) {
		return mapaTelefonos(contexto, cuit).get("E");
	}

	public static String numeroCelular(ContextoMB contexto, String cuit) {
		String numeroCelular = null;
		Map<String, Objeto> telefonos = mapaTelefonos(contexto, cuit);
		Objeto celular = telefonos.get("E");
		if (celular != null) {
			contexto.sesion().setModificacionCelular(celular.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", celular.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion().setModificacionCelularCanal(celular.string("canalModificacion"));
			numeroCelular = celular.string("codigoArea") + celular.string("caracteristica") + celular.string("numero");
			if (celular.string("codigoArea").equals("054") && !celular.string("codigoPais").equals("054")) {
				numeroCelular = celular.string("codigoPais") + celular.string("caracteristica") + celular.string("numero");
			}
			while (numeroCelular.startsWith("0")) {
				numeroCelular = numeroCelular.substring(1);
			}
		} else {
			return mapearError(contexto, cuit, telefonos.get("ERROR"));
		}
		return numeroCelular;
	}

	private static String mapearError(ContextoMB contexto, String cuit, Objeto telefonosConError) {
		if (telefonosConError != null) {
			return "ERROR_TELEFONO_NO_VALIDO";
		}
		return "USUARIO_NO_POSEE_CELULAR";
	}

	/* ========== DOMICILIO ========== */
	public static Map<String, Objeto> mapaDomicilios(ContextoMB contexto, String cuit) {
		ApiResponseMB response = domicilios(contexto, cuit);
		Map<String, Objeto> domicilios = new HashMap<>();
		Map<String, Date> fechasModificacion = new HashMap<>();
		for (Objeto item : response.objetos()) {
			String tipo = item.string("idTipoDomicilio");
			Date fechaModificacion = fechasModificacion.get(tipo);
			Date fechaModificacionActual = item.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", item.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss"));
			if (fechaModificacion == null || fechaModificacion.getTime() < fechaModificacionActual.getTime()) {
				domicilios.put(tipo, item);
				fechasModificacion.put(tipo, fechaModificacionActual);
			}
		}
		return domicilios;
	}

	public static Objeto domicilioPostal(ContextoMB contexto, String cuit) {
		return mapaDomicilios(contexto, cuit).get("DP");
	}

	public static Objeto domicilioLaboral(ContextoMB contexto, String cuit) {
		return mapaDomicilios(contexto, cuit).get("LA");
	}

	public static Objeto domicilioLegal(ContextoMB contexto, String cuit) {
		return mapaDomicilios(contexto, cuit).get("LE");
	}

	public static Objeto domicilioPorTipo(ContextoMB contexto, String cuit, String tipoDomicilio) {
		return mapaDomicilios(contexto, cuit).get(tipoDomicilio);
	}

	/* ========== CONFIGURACION ========== */
	public static Integer sugerirOtpSegundoFactor(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectSugerirOtpSegundoFactor", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[validadores_cobis] WHERE id_cobis = ?";
		sqlRequest.add(idCobis);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return valor;
	}

	public static List<String> funcionalidadesSegundoFactor() {
		List<String> lista = new ArrayList<>();

		SqlRequestMB sqlRequest = SqlMB.request("SelectFuncionalidadesSegundoFactor", "hbs");
		sqlRequest.sql = "SELECT DISTINCT funcionalidad FROM [Hbs].[dbo].[validadores_funcionalidad] ORDER BY funcionalidad";
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return null;
		}
		for (Objeto registro : sqlResponse.registros) {
			String funcionalidad = registro.string("funcionalidad");
			lista.add(funcionalidad);
		}

		return lista;
	}

	public static List<String> validadoresSegundoFactor(ContextoMB contexto, String funcionalidad, String idCobis) {
		contexto.parametros.set("funcionalidad", funcionalidad);
		RespuestaMB respuesta = MBPersona.validadoresSegundoFactor(contexto);
		List<String> lista = respuesta.objeto("validadoresUsuario").toList().stream().map(object -> Objects.toString(object, null)).collect(Collectors.toList());
		return lista;

	}

	// TODO: implementar donde corresponda
	public static Boolean preguntasPersonalesCargadas(ContextoMB contexto) {
		ApiResponseMB response = RestSeguridad.consultaPreguntasPorCliente(contexto, 1);
		if (response.hayError()) {
			if (response.string("mensajeAlUsuario").contains("the challenge size cannot be larger than the number of questions stored for the user")) {
				return false;
			}
			if (response.string("mensajeAlUsuario").contains("authentication attempts exceeded")) {
				return false;
			}
			return false;
		}
		return true;
	}

	public static SqlResponseMB permitirSegundoFactorOtp(String idCobis, Boolean acepto) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdatePermitirSmsEmailSegundoFactor", "hbs");
		sqlRequest.sql = "UPDATE [Hbs].[dbo].[validadores_cobis] ";
		sqlRequest.sql += "SET [fecha_modificacion] = ?, [acepto] = ? ";
		sqlRequest.sql += "WHERE [id_cobis] = ? ";
		sqlRequest.add(new Date());
		sqlRequest.add(acepto ? "1" : "0");
		sqlRequest.add(idCobis);

		sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
		sqlRequest.sql += "INSERT INTO [Hbs].[dbo].[validadores_cobis] ([id_cobis], [fecha_modificacion], [acepto]) ";
		sqlRequest.sql += "VALUES (?, ?, ?) ";
		sqlRequest.add(idCobis);
		sqlRequest.add(new Date());
		sqlRequest.add(acepto ? "1" : "0");

		return SqlMB.response(sqlRequest);
	}

	public static ApiResponseMB consumosSugeridos(ContextoMB contexto, String idCobis) {
		ApiRequestMB request = ApiMB.request("consumosSugeridos", "clientes", "GET", "/v1/cliente/consumosSugeridos/{id}", contexto);
		request.path("id", idCobis);
		request.cacheSesion = true;
		return ApiMB.response(request, idCobis);
	}

	public static ApiResponseMB consultaCensoNacional(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PersonaCensoNacional", "personas", "GET", "/censo", contexto);
		request.query("cuil", contexto.persona().cuit());
		request.query("idcobis", contexto.idCobis());
		request.query("sexo", contexto.persona().idSexo());
		request.query("tipoDocumento", contexto.persona().tipoTributario());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static Boolean otpSegundoFactor(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectSugerirOtpSegundoFactor", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[validadores_cobis] WHERE id_cobis = ?";
		sqlRequest.add(idCobis);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return (valor != null && valor == 1);
	}

	public static void enviarMailActualizacionDatosPersonales(ContextoMB contexto, String modificacion, String mailAnterior, String celularAnterior) {
		if ("".equals(modificacion)) {
			return;
		}
		try {
			boolean isSalesforce = 	MBSalesforce.prendidoSalesforce(contexto.idCobis());
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
				if (modificacion.contains("email") && mailAnterior != null && !"".equals(mailAnterior)) {
					Objeto parametros = new Objeto();
					parametros.set("Subject", "Se modificaron datos personales");
					parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
					Date hoy = new Date();
					parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
					parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
					parametros.set("MODULO", modificacion);
					parametros.set("CANAL", "Banca Móvil");

					RestNotificaciones.envioMailOtroDestino(contexto, ConfigMB.string("doppler_edicion_datos_personales"), parametros, mailAnterior);
				}

				Objeto parametros = new Objeto();
				parametros.set("Subject", "Se modificaron datos personales");
				parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
				Date hoy = new Date();
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
				parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
				parametros.set("MODULO", modificacion);
				parametros.set("CANAL", "Banca Móvil");

				if (modificacion.contains("email") && mailAnterior != null && !"".equals(mailAnterior)) {
					if (!isSalesforce) {
						RestNotificaciones.envioMailOtroDestino(contexto, ConfigMB.string("doppler_edicion_datos_personales"), parametros, mailAnterior);
					} else {
						parametros.set("EMAIL_DESTINO", mailAnterior);
						String salesforce_edicion_datos_personales = ConfigMB.string("salesforce_edicion_datos_personales");
						parametros.set("IDCOBIS", contexto.idCobis());
		                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
						new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_edicion_datos_personales, parametros));
					}
				}

				if (!isSalesforce) {
					RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_edicion_datos_personales"), parametros);
				} else {
					String emailDestino = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
					parametros.set("EMAIL_DESTINO", emailDestino);
					String salesforce_edicion_datos_personales = ConfigMB.string("salesforce_edicion_datos_personales");
					parametros.set("IDCOBIS", contexto.idCobis());
	                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
					new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_edicion_datos_personales, parametros));
				}

				
				if (celularAnterior != null && !"".equals(celularAnterior) && modificacion.contains("celular")) {
					RestNotificaciones.sendSms(contexto, celularAnterior, ConfigMB.string("mensaje_sms_cambio_dato_personal", "Modificaste tus datos personales en Banca Movíl de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."), "");
				}
				String celular = contexto.persona().celular();
				if (celular != null && !"".equals(celular)) {
					RestNotificaciones.sendSms(contexto, celular, ConfigMB.string("mensaje_sms_cambio_dato_personal", "Modificaste tus datos personales en Banca Movíl de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."), "");
				}
			}
		} catch (Exception e) {
		}
	}

	public static String actualizaDatosObligatorios(ContextoMB contexto) {
		String idNivelEstudios = contexto.parametros.string("nivelEstudios");
		String idSituacionVivienda = contexto.parametros.string("idSituacionVivienda");
		String idEstadoCivil = contexto.parametros.string("idEstadoCivil");

		Boolean esSujetoObligado = contexto.parametros.bool("sujetoObligado", null);
		Boolean esExpuestoPoliticamente = contexto.parametros.bool("expuestoPoliticamente", null);
		Boolean estadounidenseOResidenciaFiscalOtroPais = contexto.parametros.bool("estadounidenseOresidenciaFiscalOtroPais", null);

		if (!Objeto.allEmpty(idNivelEstudios, idSituacionVivienda, idEstadoCivil, esSujetoObligado, esExpuestoPoliticamente, estadounidenseOResidenciaFiscalOtroPais)) {

			ApiRequestMB request = ApiMB.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
			request.path("id", contexto.persona().cuit());

			if (!"".equals(idNivelEstudios))
				request.body("idNivelEstudios", idNivelEstudios);
			if (!"".equals(idSituacionVivienda))
				request.body("idSituacionVivienda", idSituacionVivienda);
			if (!"".equals(idEstadoCivil))
				request.body("idEstadoCivil", idEstadoCivil);

			if (esSujetoObligado != null) {
				request.body("esSO", esSujetoObligado);
			}
			if (esExpuestoPoliticamente != null)
				request.body("esPEP", esExpuestoPoliticamente);

			if (estadounidenseOResidenciaFiscalOtroPais != null && !estadounidenseOResidenciaFiscalOtroPais) {
				// no modifico estos datos si viene true estadounidenseOResidenciaFiscalOtroPais
				// (ya estoy tirando un error antes)
				request.body("indicioFatca", false);
				request.body("idResidencia", "L");
			}

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
			if (response.hayError()) {
				if (response.string("mensajeAlUsuario").contains("Producto bancario deshabilitado")) {
					return "ERROR_CORRIENDO_BATCH";
				}
				return "ERROR";
			}
			ApiMB.eliminarCache(contexto, "personas", contexto.idCobis());
			return "";
		}

		return "";
	}

	public static Boolean existeMuestreo(String tipoMuestra, String valor, String subid) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectMuestreo", "hbs");
		sqlRequest.sql = "SELECT 1 FROM [Homebanking].[dbo].[muestreo] WHERE m_tipoMuestra = ? AND m_valor = ? AND m_subid = ? ";
		sqlRequest.add(tipoMuestra);
		sqlRequest.add(valor);
		sqlRequest.add(subid);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

		return (sqlResponse.registros != null && sqlResponse.registros.size() > 0);

	}

	public static Objeto buscarBaseNegativa(Boolean buscarBaseNegativa, ContextoMB contexto) {
		Objeto baseNegativa = new Objeto();
		baseNegativa.set("COBUVACVS", false);

		if (buscarBaseNegativa) {
			try {
				ApiRequestMB requestBaseNegativa = ApiMB.request("BasesNegativas", "personas", "GET", "/basesNegativas/personas", contexto);
				requestBaseNegativa.query("idSolicitante", contexto.idCobis());
				requestBaseNegativa.query("tipoDocumento", contexto.persona().idTipoDocumentoString());
				requestBaseNegativa.query("nroDocumento", contexto.persona().numeroDocumento());
				requestBaseNegativa.query("sexo", contexto.persona().idSexo());
				requestBaseNegativa.query("idTributario", contexto.persona().tipoTributario());
				requestBaseNegativa.query("nroTributario", contexto.persona().cuit());
				ApiResponseMB responseBaseNegativa = ApiMB.response(requestBaseNegativa, contexto.idCobis());
				if (!responseBaseNegativa.hayError()) {
					for (Objeto item : responseBaseNegativa.objetos()) {
						if (item.string("referencia").equals("COBUVACVS")) {
							baseNegativa.set("COBUVACVS", true);
						}
					}
				}
			} catch (Exception e) {
			}

		}
		return baseNegativa;
	}

//	public static Date obtenerFechaClaveBuho(String idCobis) {
//		SqlRequestMB sqlRequest = SqlMB.request("SelectFechaClaveBuho", "clientes_operadores");
//		sqlRequest.sql = "select top 1 bfc_fecha from [clientes-operadores].[dbo].[ApiSeg_BuhoFacil_Cliente] where [cli_id] = ? order by bfc_fecha desc";
//		sqlRequest.parametros.add(idCobis);
//		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
//
//		if (!sqlResponse.hayError)
//			if (sqlResponse.registros.size() != 0) {
//				return sqlResponse.registros.get(0).date("bfc_fecha");
//			}
//
//		return null;
//	}

	public static Boolean direccionEmailLegal(ContextoMB contexto, String cuit) {
		Objeto email = email(contexto, cuit);
		if (email != null) {
			return email.bool("direccionLegal");
		}
		return false;
	}

	// opcion. valores aceptados: solodirecciones | solomails | solotelefonos | todo
	public static ApiResponseMB getDataValid(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("GetDataValid", "personas", "GET", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
		request.path("id", contexto.persona().cuit());
		request.query("opcion", "todo");
		request.cacheSesion = true;

		return ApiMB.response(request, contexto.idCobis());
	}

	public static void eliminarCacheDatavalid(ContextoMB contexto) {
		try {
			ApiMB.eliminarCache(contexto, "GetDataValid", contexto.idCobis());
		} catch (Exception e) {
		}
	}

	public static ApiResponseMB postDataValid(ContextoMB contexto, Integer secDir, Integer secMail, Integer secTel) {
		ApiRequestMB request = ApiMB.request("PostDataValid", "personas", "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
		request.path("id", contexto.persona().cuit());

		if (secDir != null && secDir != 0){
			request.body("secDir", secDir);
		}

		if (secMail != null && secMail != 0) {
			request.body("secMail", secMail);
			request.body("tipoMail", "EMP");
		}

		if (secTel != null && secTel != 0){
			request.body("secTel", secTel);
		}

		request.permitirSinLogin = true;
		eliminarCacheDatavalid(contexto);

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB postDataValidOtp(ContextoMB contexto, Integer secDir, Integer secMail, Integer secTel) {
		ApiRequestMB request = ApiMB.request("PostDataValid", "personas", "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
		request.path("id", contexto.persona().cuit());

		if (secDir != null && secDir != 0){
			request.body("secDir", secDir);
		}

		if (secMail != null && secMail != 0) {
			request.body("secMail", secMail);
			request.body("tipoMail", "EMP");
			request.body("otpMail", "S");
		}

		if (secTel != null && secTel != 0){
			request.body("secTel", secTel);
			request.body("otpTelefono", "S");
		}

		request.permitirSinLogin = true;
		eliminarCacheDatavalid(contexto);

		return ApiMB.response(request, contexto.idCobis());
	}

	public static RespuestaMB getRedesSociales(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectRedesSociales", "homebanking");
		sqlRequest.sql = "SELECT TOP 1 * FROM [homebanking].[dbo].[redes_sociales] WHERE cobis = ? ORDER BY momento DESC";
		sqlRequest.add(contexto.idCobis());
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		RespuestaMB res = new RespuestaMB();
		res.set("twitter", "");
		res.set("instagram", "");
		if (sqlResponse.hayError) {
			res.setEstado("ERROR");
			return res;
		}
		for (Objeto item : sqlResponse.registros) {
			res.set("twitter", item.get("twitter", ""));
			res.set("instagram", item.get("instagram", ""));
		}
		return res;
	}

	/*
	 * Se crea método para obtener el conyuge obviando la fecha fin relación, esto
	 * sólo se usará para aumento de límite por casos de datos con inconsistencia
	 * del estado civil
	 */
	public static String cuitConyugeSinFechaFinRelacion(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponseMB response = ApiMB.response(request);
		Objeto relacion = null;
		for (Objeto item : response.objetos()) {
			if (item.integer("idTipoRelacion", 0).equals(2)) {
				if (relacion == null) {
					relacion = item;
				} else {
					if (item.date("fechaModificacion", "yyyy-MM-dd").compareTo(relacion.date("fechaModificacion", "yyyy-MM-dd")) >= 0) {
						relacion = item;
					}
				}
			}
		}
		return relacion != null ? relacion.string("cuitPersonaRelacionada") : null;
	}

	public static ApiResponseMB consultarPerfilInversor(ContextoMB contexto, String idCobis) {
		ApiRequestMB request = ApiMB.request("PerfilInversor", "personas", "GET", "/perfilInversor", contexto);
		request.query("clientes", idCobis);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB generarRelacionPersona(ContextoMB contexto, String tipoRelacion, String cuitRelacion, String cobisRelacion) {
		ApiRequestMB request = ApiMB.request("PersonasRelacionadas", "personas", "POST", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.body.set("idTipoRelacion", tipoRelacion);
		request.body.set("idPersonaRelacionada", cobisRelacion);
		request.body.set("cuitPersonaRelacionada", cuitRelacion);
		ApiResponseMB response = ApiMB.response(request);
		return response;
	}

	public static Objeto getTipoRelacionPersona(ContextoMB contexto, String cuitPersonaRelacionada) {
		Objeto relacion = new Objeto();

		try {
			ApiRequestMB requestPersonasRelacionadas = ApiMB.request("PersonasRelacionadas", "personas", "GET", "/personas/{id}/relaciones", contexto);
			requestPersonasRelacionadas.path("id", contexto.persona().cuit());
			requestPersonasRelacionadas.cacheSesion = true;
			ApiResponseMB responsePersonasRelacionadas = ApiMB.response(requestPersonasRelacionadas, contexto.idCobis());

			if (!responsePersonasRelacionadas.hayError()) {
				for (Objeto item : responsePersonasRelacionadas.objetos()) {
					if (item.string("cuitPersonaRelacionada").equals(cuitPersonaRelacionada) && item.date("fechaFinRelacion", "yyyy-MM-dd'T'HH:mm:ss") == null) {
						relacion.set("id", item.string("id"));
						relacion.set("idTipoRelacion", item.string("idTipoRelacion"));
					}
				}
			}

		} catch (Exception e) {
		}
		return relacion;
	}

	public static ApiResponseMB actualizarRelacionPersona(ContextoMB contexto, String id, String tipoRelacion, String cuitRelacion, String cobisRelacion, String fechaInicioRelacion, String fechaFinRelacion, String fechaModificacion) {
		ApiRequestMB request = ApiMB.request("ActualizarRelacion", "personas", "PATCH", "/relaciones/{id}", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", id);
		request.body("idTipoRelacion", tipoRelacion);
		request.body("cuitPersonaRelacionada", cuitRelacion);
		if (fechaInicioRelacion != null) {
			request.body("fechaInicioRelacion", fechaInicioRelacion);
		}
		if (fechaFinRelacion != null) {
			request.body("fechaFinRelacion", fechaFinRelacion);
		}
		if (fechaModificacion != null) {
			request.body("fechaModificacion", fechaModificacion);
		}
		ApiResponseMB response = ApiMB.response(request);
		return response;
	}

	public static ApiResponseMB consultarPersonaCuitPadron(ContextoMB contexto, String dni) {
		try {
			ApiRequestMB request = ApiMB.request("ObtenerCuilPersonaPorDocumento", "personas", "GET", "/nrodoc", contexto);
			request.query("nrodoc", dni);
			ApiResponseMB response = ApiMB.response(request);
			return response;
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB crearDomicilioProspecto(ContextoMB contexto, String cuitProspecto, Objeto datos, String tipoDomicilio) {
		ApiRequestMB request = ApiMB.request("CrearDomicilio", "personas", "POST", "/personas/{id}/domicilios", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", cuitProspecto);
		request.body("canalModificacion", "MB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario"));
		request.body("idTipoDomicilio", tipoDomicilio);
		request.body("calle", datos.string("calle"));
		request.body("numero", datos.integer("numero"));
		request.body("piso", datos.string("piso"));
		request.body("departamento", datos.string("departamento"));
		request.body("calleEntre1", datos.string("calleEntre1"));
		request.body("calleEntre2", datos.string("calleEntre2"));
		request.body("idPais", 80);
		request.body("idProvincia", datos.integer("idProvincia"));
		request.body("idCiudad", datos.integer("idCiudad"));
		request.body("idCodigoPostal", datos.string("idCodigoPostal"));
		return ApiMB.response(request, cuitProspecto);
	}

	public static ApiResponseMB actualizarPersona(ContextoMB contexto, Objeto cambios, String cuit) {
		ApiRequestMB request = ApiMB.request("PersonaActualizar", "personas", "PATCH", "/personas/{cuit}", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("cuit", cuit);
		request.body(cambios);
		ApiResponseMB response = ApiMB.response(request, cuit);
		return response;
	}

	public static boolean tieneBaseNegativa(ContextoMB contexto) {
		try {
			ApiRequestMB requestBaseNegativa = ApiMB.request("BasesNegativas", "personas", "GET", "/basesNegativas/personas", contexto);
			requestBaseNegativa.query("idSolicitante", contexto.idCobis());
			requestBaseNegativa.query("tipoDocumento", contexto.persona().idTipoDocumentoString());
			requestBaseNegativa.query("nroDocumento", contexto.persona().numeroDocumento());
			requestBaseNegativa.query("sexo", contexto.persona().idSexo());
			requestBaseNegativa.query("idTributario", contexto.persona().tipoTributario());
			requestBaseNegativa.query("nroTributario", contexto.persona().cuit());
			requestBaseNegativa.cacheSesion = true;

			ApiResponseMB responseBaseNegativa = ApiMB.response(requestBaseNegativa, contexto.idCobis());
			if (!responseBaseNegativa.hayError()) {
				for (Objeto item : responseBaseNegativa.objetos()) {
					if (item.string("referencia").equals("UPCLDLEGA") || item.string("referencia").equals("UPCLDNOCI")) {
						return true;
					}
				}
			}
		} catch (Exception e) {}

		return false;
	}

	public static ApiResponseMB crearPersona(ContextoMB contexto, String cuil) {
		ApiRequestMB request = ApiMB.request("CrearPersona", "personas", "POST", "/personas", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.body("cuit", cuil);
		return ApiMB.response(request, cuil);
	}

	public static String buscarCuil(ContextoMB contexto, String numeroDocumento, String genero) {
		ApiRequestMB request = ApiMB.request("ConsultaPorDni", "personas", "GET", "/cuils", contexto);
		request.query("dni", numeroDocumento);

		ApiResponseMB response = ApiMB.response(request, numeroDocumento);
		if(response.hayError()){
			return null;
		}

		if(response.objetos().size() == 1){
			return response.objetos().get(0).string("cuil");
		}

		if(response.objetos().size() > 1){
			for (Objeto item : response.objetos()) {
				String cuil = item.string("cuil");
				ApiResponseMB personaTercero = consultarPersonaEspecifica(contexto, cuil);
				if(personaTercero.hayError()){
					continue;
				}

				if(genero.equals(personaTercero.string("idSexo"))){
					return cuil;
				}
			}
		}

		return null;
	}

}
