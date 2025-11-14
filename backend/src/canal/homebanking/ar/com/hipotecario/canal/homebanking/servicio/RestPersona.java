package ar.com.hipotecario.canal.homebanking.servicio;

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
import ar.com.hipotecario.backend.servicio.api.transmit.PersonaCliente;
import ar.com.hipotecario.backend.util.MapperUtil;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.api.HBPersona;
import ar.com.hipotecario.canal.homebanking.api.HBSalesforce;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class RestPersona {

	/* ========== SERVICIOS ========== */
	public static ApiResponse personas(ContextoHB contexto, String documento) {
		ApiRequest request = Api.request("Persona", "personas", "GET", "/personas", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.query("nroDocumento", documento);
		request.query("consultaCuil", "false");
		request.query("userAgent", contexto.request.userAgent());
		request.permitirSinLogin = true;
		request.cacheSesion = true;
//		if (Config.esDesarrollo()) {
//			request.cacheBaseDatosNoProductiva = true;
//		}
		return Api.response(request, documento);
	}

	public static ApiResponse clientes(ContextoHB contexto) {
		ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
		request.path("idCliente", contexto.idCobis());
		request.query("userAgent", contexto.request.userAgent());
		request.permitirSinLogin = true;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
//		if (Config.esDesarrollo()) {
//			request.cacheBaseDatosNoProductiva = true;
//		}
		return Api.response(request, contexto.idCobis());
	}

	// emm-20190614--> necesito una función que consulta un cliente de forma
	// específica
	public static ApiResponse consultarClienteLogsCambios(ContextoHB contexto, String idCobis) {
		ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("idCliente", idCobis);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, idCobis);
	}

	public static ApiResponse consultarClienteEspecifico(ContextoHB contexto, String idCobis) {
		ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
		request.path("idCliente", idCobis);
		request.cacheSesion = true;
		return Api.response(request, idCobis);
	}

	public static ApiResponse consultarPersonaEspecifica(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("PersonaTercero", "personas", "GET", "/personas/{cuit}", contexto);
		request.path("cuit", cuit);
		request.cacheSesion = true;

		ApiResponse response = Api.response(request, cuit);
		return response;
	}

	public static ApiResponse consultarActividades(ContextoHB contexto) {
		ApiRequest request = Api.request("Actividades", "personas", "GET", "/personas/{id}/actividades", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		return Api.response(request);
	}

	public static ApiResponse actualizarActividad(ContextoHB contexto, Integer idActividad, String idSituacionLaboral, String idProfesion, String idRamo, String idCargo, BigDecimal ingresoNeto, Boolean esPrincipal) {
		ApiRequest request = null;
		// if (idActividad == null)
		if (idActividad == null) { // si viene nulo doy de alta la actividad sino la actualizo
			request = Api.request("ActividadesAlta", "personas", "POST", "/personas/{id}/actividades", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", contexto.persona().cuit());
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
		} else {
			request = Api.request("ActividadesModificacion", "personas", "PATCH", "/actividades/{id}", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", idActividad.toString());
			request.body("id", idActividad);
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
		}

		return Api.response(request);
	}

	public static ApiResponse emails(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("Email", "personas", "GET", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse telefonos(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("Telefono", "personas", "GET", "/personas/{id}/telefonos", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse domicilios(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("Domicilio", "personas", "GET", "/personas/{id}/domicilios", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse actualizarEmail(ContextoHB contexto, String cuit, String email) {
		Api.eliminarCache(contexto, "Email", contexto.idCobis());
		ApiRequest request = Api.request("ActualizarEmail", "personas", "POST", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
		request.body("idTipoMail", "EMP");
		request.body("direccion", email);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (!response.hayError() && cuit.equals(contexto.persona().cuit())) {
			contexto.insertarContador("CAMBIO_MAIL");
		}
		return response;
	}

	public static ApiResponse actualizarEmail(ContextoHB contexto, String cuit, String email, Boolean checkDirLegal) {
		Api.eliminarCache(contexto, "Email", contexto.idCobis());
		ApiRequest request = Api.request("ActualizarEmail", "personas", "POST", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuit);
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
		request.body("idTipoMail", "EMP");
		request.body("direccion", email);
		request.body("direccionLegal", checkDirLegal);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (!response.hayError() && contexto.persona().cuit().equals(cuit)) {
			contexto.insertarContador("CAMBIO_MAIL");
		}
		return response;
	}

	public static ApiResponse actualizarCelular(ContextoHB contexto, String cuit, String codigoArea, String caracteristica, String numero) {
		Objeto celular = celular(contexto, cuit);
		Api.eliminarCache(contexto, "Telefono", contexto.idCobis());
		ApiRequest request = null;

		if(codigoArea.length() + caracteristica.length() != 7){
			String caracteristicaAux = caracteristica;
			caracteristica = caracteristicaAux.substring(0, 7 - codigoArea.length());
			numero = caracteristicaAux.substring(7 - codigoArea.length()) + numero;
		}

		if (celular == null) {
			request = Api.request("CrearCelular", "personas", "POST", "/personas/{id}/telefonos", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", cuit);
			request.body("canalModificacion", "HB");
			request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("prefijo", "15");
			request.body("codigoPais", "54");
		} else {
			request = Api.request("ActualizarCelular", "personas", "PATCH", "/telefonos/{id}", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", celular.string("id"));
			request.body("canalModificacion", "HB");
			request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("prefijo", "15");
			request.body("codigoPais", "54");
		}
		ApiResponse response = Api.response(request, contexto.idCobis());
		if (!response.hayError() && contexto.persona().cuit().equals(cuit)) {
			contexto.insertarContador("CAMBIO_TELEFONO");
		}
		return response;
	}

	public static ApiResponse actualizarDomicilio(ContextoHB contexto, String cuit, Objeto datos, String tipoDomicilio) {

		if (tipoDomicilio == null || "".equals(tipoDomicilio)) {
			tipoDomicilio = "DP";
		}

		// Objeto domicilio = domicilioPostal(contexto, cuit);
		Objeto domicilio = domicilioPorTipo(contexto, cuit, tipoDomicilio);
		Api.eliminarCache(contexto, "Domicilio", contexto.idCobis());

		if (domicilio == null) {
			ApiRequest request = Api.request("CrearDomicilio", "personas", "POST", "/personas/{id}/domicilios", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", cuit);
			request.body("canalModificacion", "HB");
			request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
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
			return Api.response(request, contexto.idCobis());
		} else {
			ApiRequest request = Api.request("ActualizarDomicilio", "personas", "PATCH", "/domicilios/{id}", contexto);
			request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
			request.path("id", domicilio.string("id"));
			request.body("canalModificacion", "HB");
			request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
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
			return Api.response(request, contexto.idCobis());
		}
	}

	public static ApiResponse perfilInversor(ContextoHB contexto) {
		ApiRequest request = Api.request("PerfilInversor", "personas", "GET", "/perfilInversor", contexto);
		request.query("clientes", contexto.idCobis());
		request.cacheSesion = true;
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarPerfilInversor(ContextoHB contexto, String idCobis) {
		ApiRequest request = Api.request("PerfilInversor", "personas", "GET", "/perfilInversor", contexto);
		request.query("clientes", idCobis);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarPerfilInversorCuentaCotitular(ContextoHB contexto, String idCobis) {
		ApiRequest request = Api.request("PerfilInversor", "personas", "GET", "/perfilInversor", contexto);
		request.query("clientes", idCobis);
		request.cacheSesion = false;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse actualizaPerfilInversor(ContextoHB contexto, String perfilInversor, String operacion) {

		Api.eliminarCache(contexto, "PerfilInversor", contexto.idCobis());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 365); // emm: por ahora lo hardcodeo, pero tiene que ser variable. Tendríamos que
		// agregar una variable de entorno.
		SimpleDateFormat fechaHasta = new SimpleDateFormat("yyyy-MM-dd");
		fechaHasta.setTimeZone(calendar.getTimeZone());

		ApiRequest request = Api.request("AltaPerfilInversorPropioRiesgo", "personas", "POST", "/administrarPerfil", contexto);
		request.body("ente", contexto.idCobis());
		request.body("estado", "A");
		request.body("fechaAMPerfil", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.body("fechaFin", fechaHasta.format(calendar.getTime()));
		request.body("fuenteOrigen", "W");
		request.body("operacion", operacion);
		request.body("perfilInversor", perfilInversor);

		return Api.response(request);
	}

	public static ApiResponse segmentacion(ContextoHB contexto) {
		ApiRequest request = Api.request("SegmentacionCliente", "personas", "GET", "/segmentacionCliente", contexto);
		request.query("cuit", contexto.persona().cuit());
		return Api.response(request);
	}

	public static ApiResponse convenios(ContextoHB contexto) {
		ApiRequest request = Api.request("ConveniosCliente", "recaudaciones", "GET", "/v1/convenios", contexto);
		request.query("cuit", contexto.persona().cuit());
		request.query("operacion", "S");
		return Api.response(request);
	}

	/* ========== PERSONA ========== */
	public static List<Objeto> personas(ContextoHB contexto, String documento, String idTipoDocumento, String idSexo) {
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = personas(contexto, documento);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			if (response.string("mensajeAlUsuario").contains("NO EXISTE CLIENTE")) {
				contexto.sesion.clienteInexistente = (true);
			}
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

	public static List<String> listaIdCobis(ContextoHB contexto, String documento, String idTipoDocumento, String idSexo) {
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

	public static String cuitConyuge(ContextoHB contexto) {

		ApiRequest request = Api.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponse response = Api.response(request);
		for (Objeto item : response.objetos()) {
			if (item.integer("idTipoRelacion", 0).equals(2)) {
				if (item.string("fechaFinRelacion", null) == null) {
					return item.string("cuitPersonaRelacionada");
				}
			}
		}
		return null;
	}

	public static String cuitConyugeComplementario(ContextoHB contexto) {

		ApiRequest request = Api.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponse response = Api.response(request);
		for (Objeto item : response.objetos()) {
			if (item.integer("idTipoRelacion", 0).equals(2) || item.integer("idTipoRelacion", 0).equals(17) || item.integer("idTipoRelacion", 0).equals(19)) {
				if (item.string("fechaFinRelacion", null) == null) {
					return item.string("cuitPersonaRelacionada");
				}
			}
		}
		return null;
	}

	/*
	 * Se crea método para obtener el conyuge obviando la fecha fin relación, esto
	 * sólo se usará para aumento de límite por casos de datos con inconsistencia
	 * del estado civil
	 */
	public static String cuitConyugeSinFechaFinRelacion(ContextoHB contexto) {
		ApiRequest request = Api.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		ApiResponse response = Api.response(request);
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

	/* ========== EMAIL ========== */
	public static Map<String, Objeto> mapaEmails(ContextoHB contexto, String cuit) {
		ApiResponse response = emails(contexto, cuit);
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

	public static Objeto email(ContextoHB contexto, String cuit) {
		return mapaEmails(contexto, cuit).get("EMP");
	}

	public static String direccionEmail(ContextoHB contexto, String cuit) {
		String direccionEmail = null;
		Objeto email = email(contexto, cuit);
		if (email != null) {
			contexto.sesion.modificacionMail = (email.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", email.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion.modificacionMailCanal = (email.string("canalModificacion"));
			direccionEmail = email.string("direccion");
		}
		return direccionEmail;
	}

	public static Integer idEmail(ContextoHB contexto, String cuit) {
		Integer id = 0;
		Objeto email = email(contexto, cuit);
		if (email != null) {
			contexto.sesion.modificacionMail = (email.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", email.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion.modificacionMailCanal = (email.string("canalModificacion"));
			id = email.integer("idCore");
		}
		return id;
	}

	/* ========== TELEFONO ========== */
	public static Map<String, Objeto> mapaTelefonos(ContextoHB contexto, String cuit) {
		ApiResponse response = telefonos(contexto, cuit);
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
				}
			}
		}
		return telefonos;
	}

	public static Objeto celular(ContextoHB contexto, String cuit) {
		return mapaTelefonos(contexto, cuit).get("E");
	}

	public static String numeroCelular(ContextoHB contexto, String cuit) {
		String numeroCelular = null;
		Objeto celular = celular(contexto, cuit);

		if (celular != null) {
			contexto.sesion.modificacionCelular = (celular.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss", celular.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss")));
			contexto.sesion.modificacionCelularCanal = (celular.string("canalModificacion"));
			numeroCelular = celular.string("codigoArea") + celular.string("caracteristica") + celular.string("numero");
			if (celular.string("codigoArea").equals("054") && !celular.string("codigoPais").equals("054")) {
				numeroCelular = celular.string("codigoPais") + celular.string("caracteristica") + celular.string("numero");
			}
			while (numeroCelular.startsWith("0")) {
				numeroCelular = numeroCelular.substring(1);
			}
		}
		return numeroCelular;
	}

	/* ========== DOMICILIO ========== */
	public static Map<String, Objeto> mapaDomicilios(ContextoHB contexto, String cuit) {
		ApiResponse response = domicilios(contexto, cuit);
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

	public static Objeto domicilioPostal(ContextoHB contexto, String cuit) {
		return mapaDomicilios(contexto, cuit).get("DP");
	}

	public static Objeto domicilioLegal(ContextoHB contexto, String cuit) {
		return mapaDomicilios(contexto, cuit).get("LE");
	}

	public static Objeto domicilioPorTipo(ContextoHB contexto, String cuit, String tipoDomicilio) {
		return mapaDomicilios(contexto, cuit).get(tipoDomicilio);
	}

	/* ========== CONFIGURACION ========== */
	public static Integer sugerirOtpSegundoFactor(String idCobis) {
		SqlRequest sqlRequest = Sql.request("SelectSugerirOtpSegundoFactor", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[validadores_cobis] WHERE id_cobis = ?";
		sqlRequest.add(idCobis);
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return valor;
	}

	public static Boolean otpSegundoFactor(String idCobis) {
		SqlRequest sqlRequest = Sql.request("SelectSugerirOtpSegundoFactor", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[validadores_cobis] WHERE id_cobis = ?";
		sqlRequest.add(idCobis);
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return (valor != null && valor == 1);
	}

	public static Boolean existeMuestreo(String tipoMuestra, String valor, String subid) {
		SqlRequest sqlRequest = Sql.request("SelectMuestreo", "hbs");
		sqlRequest.sql = "SELECT 1 FROM [Homebanking].[dbo].[muestreo] WHERE m_tipoMuestra = ? AND m_valor = ? AND m_subid = ? ";
		sqlRequest.add(tipoMuestra);
		sqlRequest.add(valor);
		sqlRequest.add(subid);
		SqlResponse sqlResponse = Sql.response(sqlRequest);

		return (sqlResponse.registros != null && sqlResponse.registros.size() > 0);

	}

	public static List<String> validadoresSegundoFactor(ContextoHB contexto, String funcionalidad, String idCobis) {
		contexto.parametros.set("funcionalidad", funcionalidad);
		Respuesta respuesta = HBPersona.validadoresSegundoFactor(contexto, contexto.sesion.cbuDestinoValidacionSegundoFactor);
		return respuesta.objeto("validadoresUsuario").toList().stream().map(object -> Objects.toString(object, null)).collect(Collectors.toList());
	}

	public static Boolean enviarMailActualizacionDatosPersonales(ContextoHB contexto, String modificacion, String mailAnterior, String celularAnterior) {
		if ("".equals(modificacion)) {
			return false;
		}
		try {
			if (true) {
				Objeto parametros = new Objeto();
				parametros.set("Subject", "Se modificaron datos personales");
				parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
				Date hoy = new Date();
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
				parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
				parametros.set("MODULO", modificacion);
				parametros.set("CANAL", "Home Banking");
				if (modificacion.contains("email") && mailAnterior != null && !"".equals(mailAnterior)) {

					if (!HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
						RestNotificaciones.envioMailOtroDestino(contexto, ConfigHB.string("doppler_edicion_datos_personales"), parametros, mailAnterior);
					} else {
						String salesforce_edicion_datos_personales = ConfigHB.string("salesforce_edicion_datos_personales");
						parametros.set("NOMBRE", contexto.persona().nombre());
						parametros.set("APELLIDO", contexto.persona().apellido());
						parametros.set("EMAIL_DESTINO", mailAnterior);
						parametros.set("IDCOBIS", contexto.idCobis());
						new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_edicion_datos_personales, parametros));
					}

				}

				if (!HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
					RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_edicion_datos_personales"), parametros);
				} else {
					String salesforce_edicion_datos_personales = ConfigHB.string("salesforce_edicion_datos_personales");
					String emailDestino = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
					parametros.set("NOMBRE", contexto.persona().nombre());
					parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("EMAIL_DESTINO", emailDestino);
					parametros.set("IDCOBIS", contexto.idCobis());
					new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_edicion_datos_personales, parametros));
				}
				if (celularAnterior != null && !"".equals(celularAnterior) && modificacion.contains("celular")) {
					RestNotificaciones.sendSms(contexto, celularAnterior, ConfigHB.string("mensaje_sms_cambio_dato_personal", "Modificaste tus datos personales en Home Banking de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."), "");
				}
				String celular = contexto.persona().celular();
				if (celular != null && !"".equals(celular)) {
					RestNotificaciones.sendSms(contexto, celular, ConfigHB.string("mensaje_sms_cambio_dato_personal", "Modificaste tus datos personales en Home Banking de Banco Hipotecario. Si desconoces haber hecho el cambio comunicate al 08102227777."), "");
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static String compararMailActualizado(ContextoHB contexto, String email) {
		String mailAnterior = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
		if (mailAnterior == null) {
			return "email";
		}

		return (mailAnterior.equals(email) ? "" : "email");
	}

	public static String compararCelularActualizado(ContextoHB contexto, String celularCodigoArea, String celularCaracteristica, String celularNumero) {
		Objeto celularAnterior = RestPersona.celular(contexto, contexto.persona().cuit());
		if (celularAnterior == null) {
			return "celular";
		}

		if (!celularAnterior.string("codigoArea").equals(celularCodigoArea) || !celularAnterior.string("caracteristica").equals(celularCaracteristica) || !celularAnterior.string("numero").equals(celularNumero)) {
			return "celular";
		}

		return "";
	}

	public static String compararDomicilioActualizado(ContextoHB contexto, String calle, String altura, String piso, String departamento, String idProvincia, String idLocalidad, String codigoPostal) {
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

	// TODO: implementar donde corresponda
	public static Boolean preguntasPersonalesCargadas(ContextoHB contexto) {
		ApiResponse response = RestSeguridad.consultaPreguntasPorCliente(contexto, 1);
		return !response.hayError();
	}

	public static SqlResponse permitirSegundoFactorOtp(String idCobis, Boolean acepto) {

		if (otpSegundoFactor(idCobis)) {
			return new SqlResponse();
		}

		SqlRequest sqlRequest = Sql.request("InsertOrUpdatePermitirSmsEmailSegundoFactor", "hbs");
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

		return Sql.response(sqlRequest);
	}

	public static ApiResponse actualizarPersona(ContextoHB contexto, Objeto cambios) {
		String cuit = contexto.persona().cuit();
		ApiRequest request = Api.request("PersonaActualizar", "personas", "PATCH", "/personas/{cuit}", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("cuit", cuit);
		request.body(cambios);

		ApiResponse response = Api.response(request, cuit);
		return response;
	}

	public static ApiResponse actualizarPersona(ContextoHB contexto, Objeto cambios, String cuit) {
		ApiRequest request = Api.request("PersonaActualizar", "personas", "PATCH", "/personas/{cuit}", contexto);
		request.path("cuit", cuit);
		request.body(cambios);
		ApiResponse response = Api.response(request, cuit);
		return response;
	}

	public static Boolean direccionEmailLegal(ContextoHB contexto, String cuit) {
		Objeto email = email(contexto, cuit);
		if (email != null) {
			return email.bool("direccionLegal");
		}
		return false;
	}

	// opcion. valores aceptados: solodirecciones | solomails | solotelefonos | todo
	public static ApiResponse getDataValid(ContextoHB contexto) {
		ApiRequest request = Api.request("GetDataValid", "personas", "GET", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.query("opcion", "todo");
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static void eliminarCacheDatavalid(ContextoHB contexto) {
		try {
			Api.eliminarCache(contexto, "GetDataValid", contexto.idCobis());
		} catch (Exception e) {
		}
	}

	public static ApiResponse postDataValid(ContextoHB contexto, Integer secDir, Integer secMail, Integer secTel) {
		ApiRequest request = Api.request("PostDataValid", "personas", "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
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

		eliminarCacheDatavalid(contexto);

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse postDataValidOtp(ContextoHB contexto, Integer secDir, Integer secMail, Integer secTel) {
		ApiRequest request = Api.request("PostDataValid", "personas", "POST", "/personas/{id}/datavalid", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario_mb"));
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

		return Api.response(request, contexto.idCobis());
	}

	public static Boolean insertRedesSociales(ContextoHB contexto) {
		String twitter = contexto.parametros.string("twitter", "");
		String instagram = contexto.parametros.string("instagram", "");
		SqlRequest sqlRequest = Sql.request("insertRedesSociales", "homebanking");
		sqlRequest.sql = "INSERT into [Homebanking].[dbo].[redes_sociales] (cobis, twitter, instagram, momento) VALUES (?, ?, ?, GETDATE() )";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(twitter);
		sqlRequest.add(instagram);
		Sql.response(sqlRequest);
		return true;
	}

	public static Respuesta getRedesSociales(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("SelectRedesSociales", "homebanking");
		sqlRequest.sql = "SELECT TOP 1 * FROM [homebanking].[dbo].[redes_sociales] WHERE cobis = ? ORDER BY momento DESC";
		sqlRequest.add(contexto.idCobis());
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		Respuesta res = new Respuesta();
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

	public static ApiResponse generarRelacionPersona(ContextoHB contexto, String tipoRelacion, String cuitRelacion, String cobisRelacion) {
		ApiRequest request = Api.request("PersonasRelacionadas", "personas", "POST", "/personas/{cuit}/relaciones", contexto);
		request.path("cuit", contexto.persona().cuit());
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.body.set("idTipoRelacion", tipoRelacion);
		request.body.set("idPersonaRelacionada", cobisRelacion);
		request.body.set("cuitPersonaRelacionada", cuitRelacion);
		ApiResponse response = Api.response(request);
		return response;
	}

	public static Objeto getTipoRelacionPersona(ContextoHB contexto, String cuitPersonaRelacionada) {
		Objeto relacion = new Objeto();
		try {
			ApiRequest requestPersonasRelacionadas = Api.request("PersonasRelacionadas", "personas", "GET", "/personas/{id}/relaciones", contexto);
			requestPersonasRelacionadas.path("id", contexto.persona().cuit());
			requestPersonasRelacionadas.cacheSesion = true;
			ApiResponse responsePersonasRelacionadas = Api.response(requestPersonasRelacionadas);

			if (!responsePersonasRelacionadas.hayError()) {
				for (Objeto item : responsePersonasRelacionadas.objetos()) {
					if (item.string("cuitPersonaRelacionada").equals(cuitPersonaRelacionada) && item.date("fechaFinRelacion", "yyyy-MM-dd'T'HH:mm:ss") == null) {
						relacion.set("id", item.string("id"));
						relacion.set("idTipoRelacion", item.string("idTipoRelacion"));
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return relacion;
	}

	public static ApiResponse actualizarRelacionPersona(ContextoHB contexto, String id, String tipoRelacion, String cuitRelacion, String cobisRelacion, String fechaInicioRelacion, String fechaFinRelacion, String fechaModificacion) {
		ApiRequest request = Api.request("ActualizarRelacion", "personas", "PATCH", "/relaciones/{id}", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
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
		ApiResponse response = Api.response(request);
		return response;
	}

	public static ApiResponse consultarPersonaCuitPadron(ContextoHB contexto, String dni) {
		try {
			ApiRequest request = Api.request("ObtenerCuilPersonaPorDocumento", "personas", "GET", "/nrodoc", contexto);
			request.query("nrodoc", dni);
			ApiResponse response = Api.response(request);
			return response;
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse crearDomicilioProspecto(ContextoHB contexto, String cuitProspecto, Objeto datos, String tipoDomicilio) {
		ApiRequest request = Api.request("CrearDomicilio", "personas", "POST", "/personas/{id}/domicilios", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.path("id", cuitProspecto);
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigHB.string("configuracion_usuario"));
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
		return Api.response(request, cuitProspecto);
	}

	public static ApiResponse crearPersona(ContextoHB contexto, String cuil) {
		ApiRequest request = Api.request("CrearPersona", "personas", "POST", "/personas", contexto);
		request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
		request.body("cuit", cuil);
		return Api.response(request, cuil);
	}

	public static String buscarCuil(ContextoHB contexto, String numeroDocumento, String genero) {
		ApiRequest request = Api.request("ConsultaPorDni", "personas", "GET", "/cuils", contexto);
		request.query("dni", numeroDocumento);

		ApiResponse response = Api.response(request, numeroDocumento);
		if(response.hayError()){
			return null;
		}

		if(response.objetos().size() == 1){
			return response.objetos().get(0).string("cuil");
		}

		if(response.objetos().size() > 1){
			for (Objeto item : response.objetos()) {
				String cuil = item.string("cuil");
				ApiResponse personaTercero = consultarPersonaEspecifica(contexto, cuil);
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
