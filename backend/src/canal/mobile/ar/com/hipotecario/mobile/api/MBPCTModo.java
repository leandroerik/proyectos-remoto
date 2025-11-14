package ar.com.hipotecario.mobile.api;

import java.util.Date;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.EnumCodigoProvincias;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.servicio.RestPersona;

/*
 *
 * Invocacion a API Empresa para alta de comercios y QR's en cuenta
 * para Aceptacion de PCT por medio de MODO
 * 
 * METODOS EXPUESTOS DE GET Y POST MODO
 *   1- CREAR COMERCIO
 * 	 2- CONSULTAR COMERCIO
 * 	 3- CREAR QR
 * 	 4- CONSULTAR QR
 * 
 * METDOOS SQL:
 *   1- VERIFICAR SI TIENE CUENTAS / CONSULTAR TYC
 *   2- INSERT EN TABLA DE PCT
 *   3- UPDATE TYC
 *   4- UPDATE TIENE MERCHANT
 *  
 * METODOS EMBEBIDOS:
 *   1- UNA RUTA QUE RESPONDE SI TIENE O NO TIENE TYC
 *   2- RUTA QUE GUARDA QUE ACEPTO TYC
 *   3- RUTA QUE CREA MERCHANT Y QR
 * 
 */

public class MBPCTModo {

	// GET's
	public static RespuestaMB getComercio(ContextoMB contexto) {
		// Sesion sesion = contexto.sesion();
		String cuit = contexto.persona().cuit();

		ApiRequestMB request = ApiMB.request("V1ComerciosGetByCuit", "pct_modo", "GET", "/v1/comercios/{cuit}", contexto);
		request.path("cuit", cuit);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!"OK".equals(checkResponse(response)))
			return RespuestaMB.estado(checkResponse(response));

		return RespuestaMB.exito("Comercios", response);
	}

	public static RespuestaMB getQrCuenta(ContextoMB contexto) {
		String razonSocial = contexto.persona().nombreCompleto();
		String cuit = contexto.persona().cuit();
		String cbu = contexto.parametros.string("cbu", "");

		if (Objeto.anyEmpty(cbu))
			return RespuestaMB.parametrosIncorrectos();

		ApiRequestMB request = ApiMB.request("V1ComerciosQrGetByCuitAndCbu", "pct_modo", "GET", "/v1/comercios/{cuit}/qr/{cbu}", contexto);

		request.path("cuit", cuit);
		request.path("cbu", cbu);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		response.set("razonSocial", razonSocial);
		if (!"OK".equals(checkResponse(response)))
			return RespuestaMB.estado(checkResponse(response));
		return RespuestaMB.exito("Comercios", response);
	}

	// POST's
	public static RespuestaMB crearComercioCuenta(ContextoMB contexto) {
		Persona persona = contexto.persona();
		String actividadAFIP = persona.actividadAFIP() == null || persona.actividadAFIP().isEmpty() || persona.actividadAFIP().equals("0") ? "000012" : persona.actividadAFIP().toString();
		if (actividadAFIP.length() < 6) {
			while (actividadAFIP.length() < 6) {
				actividadAFIP = "0" + actividadAFIP;
			}

			// actividadAFIP = "0" + actividadAFIP;
		}
		String cuit = persona.cuit();
		String email = persona.email();
		Objeto domicilio = RestPersona.domicilioLegal(contexto, cuit);

		if (Objeto.anyEmpty(domicilio))
			return RespuestaMB.parametrosIncorrectos();

		Integer idProvincia = EnumCodigoProvincias.getCodigoProvincia(domicilio.integer("idProvincia"));
		Objeto domicilioModificado = new Objeto();
		domicilioModificado.set("codigoPostal", domicilio.string("idCodigoPostal"));
		domicilioModificado.set("codigoProvincia", idProvincia);
		domicilioModificado.set("localidad", domicilio.string("ciudad"));

		if (Objeto.anyEmpty(domicilioModificado))
			return RespuestaMB.parametrosIncorrectos();

		boolean esExceptuadoIVA = false;
		boolean esPersonaJuridica = persona.esPersonaJuridica();
		String razonSocial = persona.nombreCompleto();
		String nombreFantasia = razonSocial;
		String segmento = "SMALL";
		String sexo = persona.idSexo().isEmpty() ? "X" : persona.idSexo();

		ApiRequestMB request = ApiMB.request("V1ComerciosPost", "pct_modo", "POST", "/v1/comercios", contexto);

		request.body("codigoActividadAFIP", actividadAFIP);
		request.body("cuit", cuit);
		request.body("email", email);
		request.body("domicilio", domicilioModificado);
		request.body("esExceptuadoIVA", esExceptuadoIVA);
		request.body("esPersonaJuridica", esPersonaJuridica);
		request.body("nombreFantasia", nombreFantasia);
		request.body("razonSocial", razonSocial);
		request.body("segmento", segmento);
		if (!esPersonaJuridica)
			request.body("sexo", sexo);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

		if (!"OK".equals(checkResponse(response)))
			return RespuestaMB.estado(checkResponse(response));

		RespuestaMB resp = RespuestaMB.exito("Comercios", response);
		// TODO: verificar el exito de la respuesta para modificar la tabla
		if (!resp.string("Comercios").isEmpty() || resp.get("estado") == "0") {
			RespuestaMB sql = modificarRegistroMerchant(contexto.idCobis());
			return sql.get("estado") != "merchant_agregado" ? resp.set("msg", "Error al modificar aceptacion_pct") : resp;
		}
		return resp;
	}

	public static RespuestaMB crearQrCuenta(ContextoMB contexto) {
		Persona persona = contexto.persona();
		String razonSocial = persona.nombreCompleto();
		String cuit = persona.cuit();
		String cbu = contexto.parametros.string("cbu", "");
		String tipoCuenta = contexto.parametros.string("tipoCuenta", "CURRENT");

		if (Objeto.anyEmpty(cbu))
			return RespuestaMB.parametrosIncorrectos();

		ApiRequestMB request = ApiMB.request("V1ComerciosQrPostByCuit", "pct_modo", "POST", "/v1/comercios/{cuit}/qr", contexto);

		request.path("cuit", cuit);
		request.body.set("cuit", cuit);
		request.body.set("cbu", cbu);
		request.body.set("tipoCuenta", tipoCuenta);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		response.set("razonSocial", razonSocial);
		if (!"OK".equals(checkResponse(response))) {
			if (response.toString().contains("Account already exists")) {
				response.set("mensajeAlDesarrollador", "qr_existente");
			} else {
				return RespuestaMB.estado(checkResponse(response));
			}
		}
		return RespuestaMB.exito("QR", response);
	}

	// Metodo que unifica la creacion del comercio y el QR
	public static RespuestaMB crearComercioAndQR(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		Objeto respComercio = new Objeto();
		if (!getTieneMerchant(contexto)) {
			respComercio = crearComercioCuenta(contexto);
			if (!respComercio.get("estado").equals("0")) {
				respuesta.setEstado("ERROR");
				respuesta.set("mensajeAlDesarrollador", "Error al crear el Merchant");
				return respuesta;
			}
		}
		/*
		 * TODO: VERIFICAR LA RESPUESTA QUE VIENE DE crearQRCuenta() en caso virgen SI
		 * EL CAMPO DEL respQR ES "Comercios" o "QR"
		 */
		RespuestaMB respQR = crearQrCuenta(contexto);
		Objeto qr = respQR.objeto("QR");
		if (qr.existe("mensajeAlDesarrollador") && qr.string("mensajeAlDesarrollador").contains("qr_existente")) {
			respQR = getQrCuenta(contexto);
		}
		respuesta.absorber(respQR.string("Comercios").isEmpty() || !respQR.existe("Comercios") ? respQR.set("msg", "Error al crear el QR") : respQR);
		return respuesta;
	}

	// SQL's
	/*
	 * metodo que consulte si existe un idCobis para ese usuario obtiene si se
	 * acepto o no TyC crea el registro en base
	 **/
	public static RespuestaMB getRegistroTyC(ContextoMB contexto) {
		RespuestaMB resp = new RespuestaMB();
		if (contexto.idCobis().isEmpty() || contexto.idCobis() == null) {
			return RespuestaMB.estado("ERROR").set("msg", "usuario_sin_cobis");
		} else {
			SqlRequestMB sqlRequest = SqlMB.request("GetRegistroTyC", "mobile");
			sqlRequest.sql += "SELECT * FROM [Mobile].[dbo].[aceptacion_pct] WHERE idCobis = ? ";
			sqlRequest.add(contexto.idCobis());
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			// SQL Error
			if (sqlResponse.hayError)
				return resp.set("msg", "error_al_consultar_aceptacion_pct");
			// Sin Registros, hago insert
			if (sqlResponse.registros.isEmpty() || sqlResponse == null)
				return agregarRegistroTyC(contexto);
			// Devolvemos proxima accion para el FRONT:
			// Crear el merchant
			// Aceptar TyC
			Objeto registro = sqlResponse.registros.get(0);
			return registro.get("acepta_tyc") != null && registro.get("acepta_tyc").equals(true) ? resp.set("msg", "crear_merchant") : resp.set("msg", "falta_aceptar_terminos_y_condiciones");
		}
	}

	/*
	 * metodo booleano para saber si tiene creado merchant
	 */
	public static Boolean getTieneMerchant(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("GetTieneMerchant", "mobile");
		sqlRequest.sql += "SELECT * FROM [Mobile].[dbo].[aceptacion_pct] WHERE idCobis = ? AND tiene_merchant = 1";
		sqlRequest.add(contexto.idCobis());
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		// SQL Error
		if (sqlResponse.hayError)
			return false;
		return sqlResponse.registros.isEmpty() ? false : true;
	}

	/*
	 * metodo para crear un registro cuando el usuario accede a cobrar con QR
	 */
	public static RespuestaMB agregarRegistroTyC(ContextoMB contexto) {
		String idCobis = contexto.idCobis();
		SqlRequestMB sqlRequest = SqlMB.request("InsertRegistroTyC", "mobile");
		sqlRequest.sql += "INSERT INTO [Mobile].[dbo].[aceptacion_pct] ([idCobis], [fecha_creacion])";
		sqlRequest.sql += "VALUES (?, ?)";
		sqlRequest.add(idCobis);
		sqlRequest.add(new Date());
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		RespuestaMB resp = new RespuestaMB();
		// SQL Error
		if (sqlResponse.hayError)
			return resp.set("msg", "Error al insertar aceptacion_pct");
		// Response para el FRONT
		return resp.set("msg:", "falta_aceptar_terminos_y_condiciones");

	}

	/*
	 * metodo update para cuando se aceptan TyC
	 */
	public static RespuestaMB modificarRegistroTyC(ContextoMB contexto) {

		SqlRequestMB sqlRequest = SqlMB.request("UpdateRegistroTyC", "mobile");
		sqlRequest.sql = "UPDATE [Mobile].[dbo].[aceptacion_pct] ";
		sqlRequest.sql += "SET [fecha_aceptacion] = ?, [acepta_tyc] = ? ";
		sqlRequest.sql += "WHERE [idCobis] = ? ";
		sqlRequest.add(new Date());
		sqlRequest.add("1");
		sqlRequest.add(contexto.idCobis());
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		RespuestaMB resp = new RespuestaMB();

		// SQL Error
		if (sqlResponse.hayError)
			return resp.set("msg", "Error al modificar aceptacion_pct");
		// Response para el FRONT
		return resp.set("msg", "OK");
	}

	/*
	 * metdo update para cuando se crea el merchant
	 */
	public static RespuestaMB modificarRegistroMerchant(String idCobis) {

		SqlRequestMB sqlRequest = SqlMB.request("UpdateRegistroMerchant", "mobile");
		sqlRequest.sql = "UPDATE [Mobile].[dbo].[aceptacion_pct] ";
		sqlRequest.sql += "SET [tiene_merchant] = ? ";
		sqlRequest.sql += "WHERE [idCobis] = ? ";
		sqlRequest.add("1");
		sqlRequest.add(idCobis);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		RespuestaMB resp = new RespuestaMB();
		// SQL Error
		if (sqlResponse.hayError)
			return resp.set("msg", "Error al modificar aceptacion_pct");
		return resp.setEstado("merchant_agregado");
	}

	// UTIL's
	public static String validarString(String cadena, String valorPorDefecto) {
		return cadena != null ? cadena : valorPorDefecto;
	}

	public static String checkResponse(ApiResponseMB response) {
		if (response.hayError() && "UNAUTHORIZED".equals(response.string("codigo"))) {
			return "UNAUTHORIZED";
		} else if (response.hayError()) {
			return validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR");
		}
		return "OK";
	}

}
