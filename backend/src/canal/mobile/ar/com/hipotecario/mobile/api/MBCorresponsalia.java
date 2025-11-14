package ar.com.hipotecario.mobile.api;

import static ar.com.hipotecario.mobile.lib.Objeto.empty;
import static ar.com.hipotecario.mobile.servicio.RestPersona.celular;

import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class MBCorresponsalia {
	/*
	 * cobis cuenta documento 857847 200000010557572 24994815 2543459
	 * 200000013187088 29985527 2724903 200100013322705 28147992 36481
	 * 200400012871766 28414895 230462 200700010599685 10448361 367196
	 * 200700012788296 1558838 4639382 200700013885576 29598359 Prueba RiesgoNet
	 * 858731 200700013375178 6876770 29985527 4954796 200800014283181 24676769 --
	 * 537819 200800014370214 25621197 5087703 200800013924504 25621197 354395
	 * 200800013683179 31210605 3199075 201400014449520 31712502 4568356
	 * 201400014015624 27450603
	 */

	public static RespuestaMB esJubilado(ContextoMB contexto) {
		String idCobis = "";
		try {
			idCobis = contexto.parametros.string("idCobis", contexto.idCobis());
		} catch (Exception e) {
			idCobis = contexto.parametros.string("idCobis");
		}

		String version = ConfigMB.string("mb_version_posicionconsolidada", "/v3");
		ApiRequestMB request = ApiMB.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto);
		request.query("idcliente", idCobis);
		request.query("tipoestado", "vigente");
		request.query("cancelados", "False");
		request.query("firmaconjunta", "False");
		request.query("firmantes", "False");
		request.query("adicionales", "True");
		request.query("tipoestado", "vigente");
		request.permitirSinLogin = true;
		ApiResponseMB response = ApiMB.response(request, idCobis);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		for (Objeto cuentas : response.objetos("cuentas")) {
			if (cuentas.string("categoria").equals("SS")) {
				ApiRequestMB requestSeguridadGetUsuario = ApiMB.request("Seguridad", "seguridad", "GET", "/v1/usuario", contexto);
				requestSeguridadGetUsuario.query("grupo", "ClientesBH");
				requestSeguridadGetUsuario.query("idcliente", idCobis);
				requestSeguridadGetUsuario.permitirSinLogin = true;

				ApiResponseMB responseSeguridadGetUsuario = ApiMB.response(requestSeguridadGetUsuario, idCobis);
				if (responseSeguridadGetUsuario.hayError() && responseSeguridadGetUsuario.codigo != 404) {
					return RespuestaMB.error();
				}

				if (!responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
					return RespuestaMB.exito("esJubilado", true);
				}
			}
		}

		return RespuestaMB.exito("esJubilado", false);
	}

	public static RespuestaMB getEtapa(ContextoMB contexto) {
		String dni = contexto.parametros.string("dni");
		String dispositivo = contexto.parametros.string("dispositivo");

		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("prueba", "mobile");
		sqlRequest.sql = "SELECT TOP 1 * FROM [Mobile].[dbo].[corresponsalia_jubilados] where dni = ? order by etapa desc";
		sqlRequest.add(dni);

		sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}

		if (sqlResponse.registros.isEmpty()) {
			SqlRequestMB sqlRequestInsert = SqlMB.request("prueba", "mobile");
			// dni, etapa, fecha(YYYYMMDD) , hora , dispositivo
			sqlRequestInsert.sql = "  INSERT INTO [Mobile].[dbo].[corresponsalia_jubilados] (Dni, Etapa, Fecha,Hora,Dispositivo) VALUES " + " (?, 1, ?, ?, ?);";

			sqlRequestInsert.add(dni);
			sqlRequestInsert.add(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			sqlRequestInsert.add(new SimpleDateFormat("hh:mm:ss").format(new Date()));
			sqlRequestInsert.add(dispositivo);
			sqlResponse = SqlMB.response(sqlRequestInsert);
			if (sqlResponse.hayError) {
				return RespuestaMB.error();
			}

			sqlRequest = SqlMB.request("prueba", "mobile");
			sqlRequest.sql = "SELECT TOP 1 * FROM [Mobile].[dbo].[corresponsalia_jubilados] where dni = ? order by etapa desc";
			sqlRequest.add(dni);

			sqlResponse = SqlMB.response(sqlRequest);
			if (sqlResponse.hayError) {
				return RespuestaMB.error();
			}
		}
		int etapa = sqlResponse.registros.get(0).integer("Etapa");

		sqlRequest = SqlMB.request("prueba", "mobile");
		sqlRequest.sql = "SELECT [DesEtapa] " + "  FROM [Mobile].[dbo].[corresponsalia_etapas] where etapa = ?";
		sqlRequest.add(etapa);

		sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito("etapa", sqlResponse.registros.get(0).string("DesEtapa"));
	}

	public static RespuestaMB avanzarEtapa(ContextoMB contexto) {
		String dni = contexto.parametros.string("dni");
		String dispositivo = contexto.parametros.string("dispositivo");

		SqlResponseMB sqlResponse;

		SqlRequestMB sqlRequest = SqlMB.request("prueba", "mobile");
		sqlRequest.sql = "SELECT TOP 1 * FROM [Mobile].[dbo].[corresponsalia_jubilados] where dni = ? order by etapa desc";
		sqlRequest.add(dni);

		sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}

		int etapa = sqlResponse.registros.get(0).integer("Etapa");

		SqlRequestMB sqlRequestInsert = SqlMB.request("prueba", "mobile");
		// dni, etapa, fecha(YYYYMMDD) , hora , dispositivo
		sqlRequestInsert.sql = "  INSERT INTO [Mobile].[dbo].[corresponsalia_jubilados] (Dni, Etapa, Fecha,Hora,Dispositivo) VALUES " + " (?, ?, ?, ?, ?);";

		sqlRequestInsert.add(dni);
		sqlRequestInsert.add(etapa + 1);
		sqlRequestInsert.add(new SimpleDateFormat("yyyyMMdd").format(new Date()));
		sqlRequestInsert.add(new SimpleDateFormat("hh:mm:ss").format(new Date()));
		sqlRequestInsert.add(dispositivo);

		sqlResponse = SqlMB.response(sqlRequestInsert);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB mismoDNI(ContextoMB contexto) {
		String scanner = contexto.parametros.string("scanner");
		String scannerDni = buscarDniByScanner(scanner);

		if (contexto.sesion().numeroDocumento.equals(scannerDni)) {
			return RespuestaMB.exito("mismoDni", true);
		} else {
			return RespuestaMB.exito("mismoDni", false);
		}
	}

	private static String buscarDniByScanner(String scanner) {
		String[] parts = scanner.split("@");
		for (int i = 0; i < parts.length; i++) {
			String dato = empty(parts[i]) ? null : parts[i].trim();
			if (esDni(dato)) {
				return dato;
			}
		}
		return null;
	}

	public static Boolean esDni(String dni) {
		return !empty(dni) && (dni.length() == 7 || dni.length() == 8) && dni.matches(".*[0-9].*");
	}

	public static Object guardarDatos(ContextoMB contexto) {
		String dni = contexto.parametros.string("dni");
		String email = contexto.parametros.string("email");
		Integer celular = contexto.parametros.integer("celular");
		boolean tyc = contexto.parametros.bool("tyc");

		// ApiPersonas Email y Celu

		SqlRequestMB sqlRequestInsert = SqlMB.request("prueba", "Mobile");
		// dni, etapa, fecha(YYYYMMDD) , hora , dispositivo
		sqlRequestInsert.sql = "  INSERT INTO [Mobile].[dbo].[corresponsalia_jubilados_datos] (Dni,Email,Celular,TyC) VALUES (?, ?, ?, ?);";

		sqlRequestInsert.add(dni);
		sqlRequestInsert.add(email);
		sqlRequestInsert.add(celular);
		sqlRequestInsert.add(tyc);

		SqlResponseMB sqlResponse = SqlMB.response(sqlRequestInsert);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

	public static ApiResponseMB actualizarCelular(ContextoMB contexto, String cuit, String codigoArea, String numero) {
		Objeto celular = celular(contexto, cuit);
		ApiMB.eliminarCache(contexto, "Telefono", contexto.idCobis());

		String caracteristica = Telefono.obtenerCaracteristica(codigoArea, numero);
		numero = Telefono.obtenerNumero(codigoArea, numero);

		ApiRequestMB request = null;
		if (celular == null) {
			request = ApiMB.request("CrearCelular", "personas", "POST", "/personas/{id}/telefonos", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", cuit);
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", "0" + codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("codigoPais", "054");
			request.body("prefijo", "15");
			request.permitirSinLogin = true;
		} else {
			request = ApiMB.request("ActualizarCelular", "personas", "PATCH", "/telefonos/{id}", contexto);
			request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
			request.path("id", celular.string("id"));
			request.body("canalModificacion", "MB");
			request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
			request.body("idTipoTelefono", "E");
			request.body("codigoArea", "0" + codigoArea);
			request.body("caracteristica", caracteristica);
			request.body("numero", numero);
			request.body("codigoPais", "54");
			request.body("prefijo", "15");
			request.permitirSinLogin = true;
		}
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError() && cuit.equals(contexto.persona().cuit())) {
			contexto.insertarContador("CAMBIO_TELEFONO");
		}

		return response;

	}

	public static ApiResponseMB actualizarEmail(ContextoMB contexto, String cuit, String email) {
		ApiMB.eliminarCache(contexto, "Email", contexto.idCobis());
		ApiRequestMB request = ApiMB.request("ActualizarEmail", "personas", "POST", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario_mb"));
		request.path("id", cuit);
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario_mb"));
		request.body("idTipoMail", "EMP");
		request.body("direccion", email);
		request.permitirSinLogin = true;

		// TODO: guardar cambios de datos del usuario
		// return Api.response(request, contexto.idCobis());
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError() && contexto.persona().cuit().equals(cuit)) {
			contexto.insertarContador("CAMBIO_MAIL");
		}
		return response;

	}

	public static RespuestaMB obtenerId(ContextoMB contexto) {
		String dni = contexto.parametros.string("dni");
		ApiRequestMB request = ApiMB.request("BuscarIdCliente", "personas", "GET", "/personas", contexto);
		request.query("nroDocumento", dni);
		request.permitirSinLogin = true;
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito("idCobis", response.objetos().get(0).string("idCliente"));
	}

	public static RespuestaMB actualizarTelefono(ContextoMB contexto) {
		String cuit = contexto.persona().cuit();
		String codArea = contexto.parametros.string("codigoArea");
		String numero = contexto.parametros.string("numero");

		ApiResponseMB response = actualizarCelular(contexto, cuit, codArea, numero);

		if (response.hayError()) {
			return RespuestaMB.error();
		}
		return RespuestaMB.exito();
	}
}
