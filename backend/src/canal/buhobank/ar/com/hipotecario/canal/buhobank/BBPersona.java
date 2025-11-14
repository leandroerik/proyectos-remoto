package ar.com.hipotecario.canal.buhobank;

import java.util.concurrent.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.TarjetasDebitoAsociadasCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.productos.PosicionConsolidada;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasDebito;
import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.UnauthorizedException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades.Actividad;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades.NuevaActividad;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.DocumentoCuestionados.DocumentoCuestionado;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.NuevoDomicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Emails;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.NuevaRelacion;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.Relacion;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.NuevoTelefono;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos.Producto;
import ar.com.hipotecario.backend.servicio.api.viviendas.ApiViviendas;
import ar.com.hipotecario.backend.servicio.api.viviendas.ConsultaPersona;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.esales.BBPersonasAlta;
import ar.com.hipotecario.backend.servicio.sql.esales.BBPersonasAlta.BBPersonaAlta;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper.PersonaRenaper;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy.SesionStandBy;
import ar.com.hipotecario.backend.util.Validadores;
import ar.com.hipotecario.canal.buhobank.SesionBB.ConyugeBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.DomicilioBB;
import ar.com.hipotecario.canal.buhobank.teradata.SqlTeradata;

public class BBPersona extends Modulo {

	public static String FORMATO_FECHA = "dd/MM/yyyy";
	public static String FORMATO_FECHA_OCR = "yyyy-MM-dd";

	public static Boolean esClienteBool(ContextoBB contexto, String cuil) {
		if (empty(cuil)) {
			return false;
		}

		contexto.parametros.set("cuil", cuil);
		Objeto clienteResp = esCliente(contexto);

        return !clienteResp.bool("continua");
	}

	public static String getCuil(ContextoBB contexto, SesionBB sesion) {
		String numeroDocumento = sesion.numeroDocumento;
		String sexo = sesion.genero;

		contexto.parametros.set("numeroDocumento", numeroDocumento);
		contexto.parametros.set("sexo", sexo);
		Objeto cuilResp = obtenerCuil(contexto);
		if (!cuilResp.string("estado").equals("OK")) {
			return null;
		}

		return cuilResp.string("cuil");
	}

	/**
	 * Este método define si el cuit ingresado existe como cliente Si existe como
	 * cliente -> rechaza Si existe como prospecto o exCliente y sus productos no
	 * están activos -> continua Si no existe en BUP -> continúa
	 * 
	 * @param contexto
	 */
	
	public static Objeto esClienteBH(ContextoBB contexto) {
		UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
		return esCliente(contexto);
	}
	
	public static Objeto esCliente(ContextoBB contexto) {
		String cuil = contexto.parametros.string("cuil");

		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
		if (persona == null) {
			return respuesta("continua", true);
		}

		if(persona.esCliente()){
			return respuesta("continua", false);
		}

		if(!Utils.isEmpty(persona.edad) && persona.edad < 18) {
			return respuesta("continua", false);
		}

		DocumentoCuestionado resDocCuestinado = ApiPersonas.consultaDocCuestionado(contexto, cuil.substring(2, 10), persona.idSexo).tryGet();
		boolean esDocCuestionado = resDocCuestinado != null && "S".equals(resDocCuestinado.cuestionado);

		return respuesta("continua", !esDocCuestionado);
	}

	public static Objeto guardarRespuestaVU(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String idOperacionVU = contexto.parametros.string("idOperacion");
		String personJson = contexto.parametros.string("person");

		Objeto personVU = Objeto.fromJson(personJson.replaceAll("null", "")).objeto("person");
		if (personVU == null || personVU.isEmpty()) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.PERSON_VU_VACIA);
			return respuesta(ErroresBB.PERSON_VU_VACIA);
		}

		Objeto dataVU = personVU.objeto("data");
		if (dataVU == null || dataVU.isEmpty()) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DATA_VU_VACIA);
			return respuesta(ErroresBB.DATA_VU_VACIA);
		}

		Boolean esDocValido = dataVU.bool("valid");
		if (!esDocValido) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DOCUMENTO_INVALIDO);
			return respuesta(ErroresBB.DOCUMENTO_INVALIDO);
		}

		String mensajeFallecimiento = personVU.string("messageOfDeath");
		if (contexto.prendidoValidarFallecimiento() && !empty(mensajeFallecimiento) && !"Sin Aviso de Fallecimiento".equals(mensajeFallecimiento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.PERSONA_FALLECIDA);
			return respuesta(ErroresBB.PERSONA_FALLECIDA);
		}

		String nombre = personVU.string("names");
		String apellido = personVU.string("lastNames");
		String genero = dataVU.string("gender");
		String nacionalidad = personVU.string("nationality");
		String paisNacimiento = dataVU.string("countryBirth");
		String numeroDocumento = personVU.string("number");
		String cuil = buscarCuil(contexto, true);
		String ejemplar = dataVU.string("copy");

		Fecha fechaVencimiento = dataVU.fecha("expirationDate", FORMATO_FECHA);
		if(!empty(fechaVencimiento)){
			Boolean estaVencido = !Fecha.ahora().esAnterior(fechaVencimiento);
			if(Boolean.TRUE.equals(estaVencido)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
				LogBB.evento(contexto, GeneralBB.ERROR_FECHA_VENCIMIENTO);
				return respuesta(GeneralBB.ERROR_FECHA_VENCIMIENTO);
			}
		}

		Fecha fechaNacimiento = personVU.fecha("birthDate", FORMATO_FECHA);
		if(!empty(fechaNacimiento)){
			Boolean esMenorA18 = fechaNacimiento.esPosterior(Fecha.ahora().restarAños(18));
			if (Boolean.TRUE.equals(esMenorA18)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
				LogBB.evento(contexto, ErroresBB.MENOR_DE_EDAD);
				return respuesta(ErroresBB.MENOR_DE_EDAD);
			}
		}

		Boolean datosValidos = !empty(cuil) && !empty(numeroDocumento);
		if (!datosValidos) {
			return respuesta();
		}

		if (!empty(numeroDocumento)) {
			if (numeroDocumento.length() == 7) {
				numeroDocumento = "0" + numeroDocumento;
			}
			sesion.numeroDocumento = numeroDocumento;
			sesion.genero = Validadores.filtroUpper(genero);
		}

		if(!sesion.esFlujoTcv()){
			if (!sesion.cuil.equals(cuil)) {
				LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuil);
				sesion.cuil = cuil;
			}

			if (!Validadores.mismaPersona(contexto, cuil, numeroDocumento)) {
				String cuilRes = getCuil(contexto, sesion);
				LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuilRes);
				sesion.cuil = cuilRes;
				cuil = cuilRes;
			}

			if (esClienteBool(contexto, sesion.cuil)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
				LogBB.evento(contexto, ErroresBB.ERROR_YA_CLIENTE);
				return respuesta(ErroresBB.DOCUMENTO_INVALIDO);
			}
		}

		if (!Validadores.mismaPersona(contexto, cuil, numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DOCUMENTO_DIFERENTE);
			return respuesta(ErroresBB.DOCUMENTO_DIFERENTE);
		}

		String calle = dataVU.string("streetAddress");
		String numeroCalle = quitarPrimerosCeros(dataVU.string("numberStreet"));
		Boolean prueba = numeroCalle.matches("[0-9]*");
		if (!prueba || empty(numeroCalle)) {
			numeroCalle = null;
		}
		String piso = quitarPrimerosCeros(dataVU.string("floor"));
		String dpto = quitarPrimerosCeros(dataVU.string("department"));
		if (piso.equals("P.BA")) {
			piso = "";
			dpto = "PB";
		}
		piso = piso.length() > 2 ? "" : piso;
		dpto = dpto.length() > 3 ? "" : dpto;
		String cp = quitarPrimerosCeros(dataVU.string("zipCode"));
		cp = cp.length() > 4 ? "" : cp;
		String ciudad = dataVU.string("city", "");
		ciudad = ciudad.length() > 40 ? ciudad.substring(0, 40) : ciudad;
		String localidad = dataVU.string("municipality");
		String provincia = dataVU.string("province");
		String pais = dataVU.string("country");

		String paisNacimientoFiltered = filtroUpperPais(paisNacimiento);
		String nacionalidadFiltered = filtroUpperPais(nacionalidad);

		// Guardar datos de la persona devueltos por VU
		sesion.nombre = Validadores.filtroUpper(nombre);
		sesion.apellido = Validadores.filtroUpper(apellido);
		sesion.genero = Validadores.filtroUpper(genero);
		sesion.fechaNacimiento = fechaNacimiento;
		sesion.paisNacimiento = paisNacimientoFiltered;
		sesion.nacionalidad = nacionalidadFiltered;

		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		String idPaisNacimiento = GeneralBB.DEFAULT_PAIS_NACIMIENTO;
		String idNacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
		if (paises != null && !paises.isEmpty()) {
			Pais paisNacimientoObj = paises.buscarPais(paisNacimientoFiltered);
			Pais paisNacionalidadObj = paises.buscarPais(nacionalidadFiltered);

			if (paisNacimientoObj != null) {
				idPaisNacimiento = String.valueOf(paisNacimientoObj.id);
			}

			if (paisNacionalidadObj != null) {
				idNacionalidad = String.valueOf(paisNacionalidadObj.id);
			}
		}
		sesion.idPaisNacimiento = idPaisNacimiento;
		sesion.idNacionalidad = idNacionalidad;
		sesion.numeroDocumento = numeroDocumento;
		sesion.idTipoIDTributario = "08";
		sesion.cuil = cuil;
		sesion.ejemplar = Validadores.filtroUpper(ejemplar);
		sesion.domicilioLegal.calle = calle;
		sesion.domicilioLegal.numeroCalle = numeroCalle;
		sesion.domicilioLegal.piso = piso;
		sesion.domicilioLegal.dpto = dpto;
		sesion.domicilioLegal.cp = cp;
		sesion.domicilioLegal.ciudad = Validadores.filtroUpper(ciudad);
		sesion.domicilioLegal.localidad = Validadores.filtroUpper(localidad);
		sesion.domicilioLegal.provincia = Validadores.filtroUpper(provincia);
		sesion.domicilioLegal.pais = Validadores.filtroUpper(pais);

		CiudadWF ciudadPorCp = DomicilioBB.ciudadPorCP(contexto, cp);
		if (ciudadPorCp != null) {
			sesion.domicilioLegal.idCiudad = ciudadPorCp.CIU_Id;
			sesion.domicilioLegal.idProvincia = ciudadPorCp.CIU_PRV_Id;
			sesion.domicilioLegal.idPais = ciudadPorCp.CIU_PAI_Id;
		}

		sesion.domicilioPostal = sesion.domicilioLegal;
		sesion.estado = EstadosBB.VU_PERSON_OK;
		sesion.save();

		Boolean datosVaciosVU = datosPersonalesVacios(contexto);
		if (datosVaciosVU) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
			LogBB.error(contexto, ErroresBB.DATOS_VACIOS_VU);
			return respuesta(ErroresBB.DATOS_VACIOS_VU);
		}

		Objeto respuesta = respuesta();
		respuesta.set("idOperacionVU", idOperacionVU);
		respuesta.set("sesion.nombre", sesion.nombre);
		respuesta.set("sesion.apellido", sesion.apellido);
		respuesta.set("sesion.genero", sesion.genero);
		respuesta.set("sesion.fechaNacimiento", sesion.fechaNacimiento);
		respuesta.set("sesion.paisNacimiento", sesion.paisNacimiento);
		respuesta.set("sesion.nacionalidad", sesion.nacionalidad);
		respuesta.set("sesion.idPaisNacimiento", sesion.idPaisNacimiento);
		respuesta.set("sesion.idNacionalidad", sesion.idNacionalidad);
		respuesta.set("sesion.domicilioLegal", sesion.domicilioLegal);
		respuesta.set("sesion.domicilioPostal", sesion.domicilioPostal);
		respuesta.set("sesion.numeroDocumento", sesion.numeroDocumento);
		respuesta.set("sesion.idTipoIDTributario", sesion.idTipoIDTributario);
		respuesta.set("sesion.cuil", sesion.cuil);
		respuesta.set("sesion.ejemplar", sesion.ejemplar);
		respuesta.set("sesion.usuarioVU", sesion.usuarioVU);

		LogBB.evento(contexto, EstadosBB.VU_PERSON_OK, respuesta);

		if(sesion.esFlujoTcv()){
			return respuesta();
		}

		return respuesta;
	}

	private static String obtenerPaisValido(String pais) {

		if (empty(pais)) {
			return "";
		}

		if (pais.equals("ESTADOS_UNIDOS")) {
			return "EE.UU.";
		}

		if (pais.equals("ESTADOUNIDENSE")) {
			return "AMERICANO";
		}

		if (pais.equals("BRITISH_INDIAN_OCEAN_TERRITORY")) {
			return "REINO UNIDO";
		}

		if (pais.equals("BRITANICA")) {
			return "INGLES";
		}

		if (pais.equals("RUSA")) {
			return "SOVIETICO";
		}

		return pais;
	}

	private static String filtroUpperPais(String pais) {

		pais = obtenerPaisValido(pais);

		if (pais.equals("EE.UU.")) {
			return pais;
		}

		return Validadores.filtroUpper(pais);
	}

	public static String quitarPrimerosCeros(String cadena) {
		if (empty(cadena) || cadena.equals("S/N") || cadena.equals("null")) {
			return "";
		}

		return cadena.replaceFirst("^0+","");
	}

	public static Objeto guardarRespuestaCompletaVU(ContextoBB contexto) {
		LogBB.evento(contexto, "REQUEST_GUARDAR_VU", contexto.parametros);

		SesionBB sesion = contexto.sesion();
		String idOperacionVU = contexto.parametros.string("idOperacion", "0");
		String guidOperacionVU = contexto.parametros.string("guidOperacion", null);
		BigDecimal confidence = contexto.parametros.bigDecimal("confidence");
		BigDecimal confidenceTotal = contexto.parametros.bigDecimal("confidenceTotal");
		Objeto ocrObj = contexto.parametros.objeto("ocr", null);
		Objeto barcodeObj = contexto.parametros.objeto("barcode", null);
		Objeto informationObj = contexto.parametros.objeto("information", null);
		BigDecimal confidenceDocument = contexto.parametros.bigDecimal("confidenceDocument");
		Boolean identical = contexto.parametros.bool("identical");

		if (!empty(guidOperacionVU) && !empty(idOperacionVU)) {
			sesion.operationVU = idOperacionVU + "/" + guidOperacionVU;
		}

		if (!identical) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.PERSONA_DIFERENTE);
			return respuesta(ErroresBB.DOCUMENTO_DIFERENTE);
		}

		if (empty(confidence) || empty(confidenceTotal) || empty(confidenceDocument)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DOCUMENTO_INVALIDO);
			return respuesta(ErroresBB.DOCUMENTO_INVALIDO);
		}

		if (Validadores.informationValidoVU(informationObj)) {
			contexto.parametros.set("idOperacion", idOperacionVU);
			String personJson = informationObj.string("person");
			contexto.parametros.set("person", personJson);
			Objeto guardarVUResp = guardarRespuestaVU(contexto);

			if (guardarVUResp.get("estado") != "0" && guardarVUResp.get("estado") != ErroresBB.PERSON_VU_VACIA && guardarVUResp.get("estado") != ErroresBB.DATA_VU_VACIA) {
				return guardarVUResp;
			}

			if (!datosPersonalesVacios(contexto)) {
				boolean tieneResidenciaTemp = false;
				if (Validadores.ocrValidoVU(ocrObj)) {
					Objeto extra = ocrObj.objeto("extra");
					String additionalJson = extra.string("additional");
					Objeto additional = Objeto.fromJson(additionalJson);
					tieneResidenciaTemp = tieneResidenciaTemporal(additional);

					guardarPersonaRenaper(contexto, sesion, extra, null);
				}
				
				if(tieneResidenciaTemp) {
					SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesion.cuil).tryGet();
					if (sesionStandBy == null) {
						SqlEsales.crearSesionStandBy(contexto, sesion.cuil);
						LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CREADA, sesion.token);
					}
				}
				else {
					if (actualizarEstadoSesionStandBy(contexto, SesionesStandBy.FLUJO_VU_OK)) {
						LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_VU_OK, sesion.token);
					}
				}

				return guardarVUResp;
			}
		}

		if (!Validadores.ocrValidoVU(ocrObj)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.OCR_VU_VACIO);
			return respuesta(ErroresBB.OCR_VU_VACIO);
		}

		Objeto extra = ocrObj.objeto("extra");
		String additionalJson = extra.string("additional");
		Objeto additional = Objeto.fromJson(additionalJson);

		String nombre = ocrObj.string("names");
		String apellido = ocrObj.string("lastNames");
		String genero = ocrObj.string("gender");
		String nacionalidad = Validadores.replace(additional.string("Nationality"), GeneralBB.DEFAULT_NACIONALIDAD_DESC);
		Fecha fechaNacimiento = ocrObj.fecha("birthdate", FORMATO_FECHA_OCR);
		String additionalPais = additional.string("BirthPlace", GeneralBB.DEFAULT_PAIS_NACIMIENTO_DESC);
		String[] additionalPaisParts = additionalPais.split("-");
		String paisNacimiento = additionalPaisParts.length > 1 ? additionalPaisParts[0].trim() : GeneralBB.DEFAULT_PAIS_NACIMIENTO_DESC;
		String numeroDocumento = ocrObj.string("number");
		String cuil = buscarCuil(contexto, Validadores.informationValidoVU(informationObj));
		String ejemplar = GeneralBB.DEFAULT_EJEMPLAR;
		Fecha fechaVencimiento = null;

		if (Validadores.mrzValidoVU(ocrObj)) {
			String mrzJson = extra.string("mrz");
			Objeto mrz = Objeto.fromJson(mrzJson);

			genero = Validadores.replace(mrz.string("Gender"), genero);
			fechaNacimiento = Validadores.replace(mrz.fecha("BirthDate", FORMATO_FECHA_OCR), fechaNacimiento);
			fechaVencimiento = mrz.fecha("ExpiryDate", FORMATO_FECHA_OCR);
			numeroDocumento = Validadores.replace(mrz.string("DocumentNumber"), numeroDocumento);
		}

		if (Validadores.barcodeValidoVU(barcodeObj)) {
			String dataJson = barcodeObj.string("data");
			Objeto dataBarcode = Objeto.fromJson(dataJson);

			nombre = Validadores.replace(dataBarcode.string("names"), nombre);
			apellido = Validadores.replace(dataBarcode.string("lastNames"), apellido);
			genero = Validadores.replace(dataBarcode.string("gender"), genero);
			fechaNacimiento = Validadores.replace(dataBarcode.fecha("birthDate", FORMATO_FECHA), fechaNacimiento);
			fechaVencimiento = Validadores.replace(dataBarcode.fecha("ExpiryDate", FORMATO_FECHA), fechaVencimiento);
			numeroDocumento = Validadores.replace(dataBarcode.string("number"), numeroDocumento);
			ejemplar = Validadores.replace(dataBarcode.string("copy"), ejemplar);
		}

		if(!empty(fechaVencimiento)){
			Boolean estaVencido = !Fecha.ahora().esAnterior(fechaVencimiento);
			if(Boolean.TRUE.equals(estaVencido)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
				LogBB.evento(contexto, GeneralBB.ERROR_FECHA_VENCIMIENTO);
				return respuesta(GeneralBB.ERROR_FECHA_VENCIMIENTO);
			}
		}

		if(!empty(fechaNacimiento)){
			Boolean esMenorA18 = fechaNacimiento.esPosterior(Fecha.ahora().restarAños(18));
			if (Boolean.TRUE.equals(esMenorA18)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
				LogBB.evento(contexto, ErroresBB.MENOR_DE_EDAD);
				return respuesta(ErroresBB.MENOR_DE_EDAD);
			}
		}

		Boolean datosValidos = !empty(cuil) && !empty(numeroDocumento);
		if (!datosValidos) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DOCUMENTO_INVALIDO);
			return respuesta(ErroresBB.DOCUMENTO_INVALIDO);
		}

		if (!empty(numeroDocumento)) {
			if (numeroDocumento.length() == 7) {
				numeroDocumento = "0" + numeroDocumento;
			}
			sesion.numeroDocumento = numeroDocumento;
			sesion.genero = Validadores.filtroUpper(genero);
		}

		if(!sesion.esFlujoTcv()){
			if (!sesion.cuil.equals(cuil)) {
				LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuil);
				sesion.cuil = cuil;
			}

			if (!Validadores.mismaPersona(contexto, cuil, numeroDocumento)) {
				String cuilRes = getCuil(contexto, sesion);
				LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuilRes);
				sesion.cuil = cuilRes;
				cuil = cuilRes;
			}

			if (esClienteBool(contexto, sesion.cuil)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
				LogBB.evento(contexto, ErroresBB.ERROR_YA_CLIENTE);
				return respuesta(ErroresBB.DOCUMENTO_INVALIDO);
			}
		}

		if (!Validadores.mismaPersona(contexto, cuil, numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.error(contexto, ErroresBB.DOCUMENTO_DIFERENTE);
			return respuesta(ErroresBB.DOCUMENTO_DIFERENTE);
		}

		String paisNacimientoFiltered = filtroUpperPais(paisNacimiento);
		String nacionalidadFiltered = filtroUpperPais(nacionalidad);

		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		String idPaisNacimiento = GeneralBB.DEFAULT_PAIS_NACIMIENTO;
		String idNacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
		if (paises != null && !paises.isEmpty()) {
			Pais paisNacimientoObj = paises.buscarPais(paisNacimientoFiltered);
			Pais paisNacionalidadObj = paises.buscarPais(nacionalidadFiltered);

			if (paisNacimientoObj != null) {
				idPaisNacimiento = String.valueOf(paisNacimientoObj.id);
			}

			if (paisNacionalidadObj != null) {
				idNacionalidad = String.valueOf(paisNacionalidadObj.id);
			}
		}

		sesion.nombre = Validadores.filtroUpper(nombre);
		sesion.apellido = Validadores.filtroUpper(apellido);
		sesion.genero = Validadores.filtroUpper(genero);
		sesion.fechaNacimiento = fechaNacimiento;
		sesion.idNacionalidad = idNacionalidad;
		sesion.nacionalidad = nacionalidadFiltered;

		if (idPaisNacimiento.equals(GeneralBB.DEFAULT_NACIONALIDAD) && !idNacionalidad.equals(GeneralBB.DEFAULT_NACIONALIDAD) && sesion.esExtranjero()) {
			sesion.idPaisNacimiento = idNacionalidad;
			sesion.paisNacimiento = paises.buscarPaisById(idNacionalidad).descripcion;
		} else {
			sesion.idPaisNacimiento = idPaisNacimiento;
			sesion.paisNacimiento = paisNacimientoFiltered;
		}
		sesion.numeroDocumento = numeroDocumento;
		sesion.idTipoIDTributario = "08";
		sesion.ejemplar = ejemplar;
		sesion.estado = EstadosBB.VU_TOTAL_OK;
		sesion.save();

		Boolean datosVaciosVU = datosPersonalesVacios(contexto);
		if (datosVaciosVU) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
			LogBB.error(contexto, ErroresBB.DATOS_VACIOS_VU);
			return respuesta(ErroresBB.DATOS_VACIOS_VU);
		}

		guardarPersonaRenaper(contexto, sesion, extra, null);

		if (actualizarEstadoSesionStandBy(contexto, SesionesStandBy.FLUJO_VU_OK)) {
			LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_VU_OK, sesion.token);
		}

		Objeto respuesta = respuesta();
		respuesta.set("idOperacionVU", idOperacionVU);
		respuesta.set("sesion.nombre", sesion.nombre);
		respuesta.set("sesion.apellido", sesion.apellido);
		respuesta.set("sesion.genero", sesion.genero);
		respuesta.set("sesion.fechaNacimiento", sesion.fechaNacimiento);
		respuesta.set("sesion.paisNacimiento", sesion.paisNacimiento);
		respuesta.set("sesion.nacionalidad", sesion.nacionalidad);
		respuesta.set("sesion.idPaisNacimiento", sesion.idPaisNacimiento);
		respuesta.set("sesion.idNacionalidad", sesion.idNacionalidad);
		respuesta.set("sesion.domicilioLegal", sesion.domicilioLegal);
		respuesta.set("sesion.domicilioPostal", sesion.domicilioPostal);
		respuesta.set("sesion.numeroDocumento", sesion.numeroDocumento);
		respuesta.set("sesion.idTipoIDTributario", sesion.idTipoIDTributario);
		respuesta.set("sesion.cuil", sesion.cuil);
		respuesta.set("sesion.ejemplar", sesion.ejemplar);
		respuesta.set("sesion.usuarioVU", sesion.usuarioVU);

		LogBB.evento(contexto, EstadosBB.VU_TOTAL_OK, respuesta);

		if(sesion.esFlujoTcv()){
			return respuesta();
		}

		return respuesta;
	}
	
	private static boolean tieneResidenciaTemporal(Objeto datos) {
		try {
			
			if(datos.string("Nationality").equals("ARGENTINA")) {
				return false;
			}
			
			Fecha expiryDate = datos.fecha("ExpiryDate", FORMATO_FECHA_OCR);
			Fecha issueDate = datos.fecha("IssueDate", FORMATO_FECHA_OCR);
			Integer diasEntreFechas = issueDate.diasFaltantes(expiryDate);
			return diasEntreFechas <= 365 * 2;
		}
		catch(Exception e) {
			return false;
		}
	}

	private static Boolean actualizarEstadoSesionStandBy(ContextoBB contexto, String estado) {

		SesionBB sesion = contexto.sesion();

		SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesion.cuil).tryGet();
		if (sesionStandBy != null) {

			sesionStandBy.estado = estado;
			return SqlEsales.actualizarSesionStandBy(contexto, sesionStandBy).tryGet();
		}

		return false;
	}

	public static void guardarPersonaRenaper(ContextoBB contexto, SesionBB sesion, Objeto extra, ConsultaPersona persona) {
		try {
			BBInfoPersona.informarDatos(contexto, sesion, extra, persona);
		} catch (Exception e) {
			System.out.println("Fallo al insertar PersonaRenaper, pero continúa el flujo");
		}
	}

	public static String buscarCuil(ContextoBB contexto, Boolean tieneInformation) {

		String cuil = "";
		String numeroDocumento = "";

		if (tieneInformation) {
			String personJson = contexto.parametros.string("person");
			Objeto personVU = Objeto.fromJson(personJson.replaceAll("null", "")).objeto("person");
			numeroDocumento = personVU.string("number");
			Objeto dataVU = personVU.objeto("data");
			cuil = dataVU.string("cuil");
			cuil = cuil.replace(".","");
			if (!empty(cuil) && !cuil.equals("0")) {
				return cuil;
			}
		}

		Objeto ocrObj = contexto.parametros.objeto("ocr");

		if (Validadores.ocrValidoVU(ocrObj)) {
			if (empty(numeroDocumento)) {
				numeroDocumento = ocrObj.string("number");
			}

			Objeto extra = ocrObj.objeto("extra");
			String additionalJson = extra.string("additional");
			Objeto additional = Objeto.fromJson(additionalJson);
			cuil = additional.string("CUIL").replace("-", "");
			cuil = cuil.replace(".","");
			if (!empty(cuil)) {
				return cuil;
			}
		}

		SesionBB sesion = contexto.sesion();
		return sesion.cuil;
	}

	public static Objeto guardarAdicionales(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String idEstadoCivil = contexto.parametros.string("idEstadoCivil", "S");
		String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
		String tipoSituacionLaboral = contexto.parametros.string("tipoSituacionLaboral", null);

		if (GeneralBB.CASADA.equals(idEstadoCivil)) {
			sesion.idCantidadNupcias = contexto.parametros.string("idCantidadNupcias", GeneralBB.NUPCIAS_PRIMERAS);
			sesion.idSubtipoEstadoCivil = GeneralBB.SUB_ESTADO_CIVIL_ID;
			sesion.SubtipoEstadoCivilDescr = GeneralBB.SUB_ESTADO_CIVIL_DESCRIPCION;
		}

		if (!empty(tipoSituacionLaboral)) {
			if (tipoSituacionLaboral.equals("M")) {
				sesion.tipoSitLaboral = GeneralBB.TIPO_SIT_LABORAL_M;
			} else {
				sesion.tipoSitLaboral = GeneralBB.TIPO_SIT_LABORAL_A;
			}
		}

		sesion.idEstadoCivil = idEstadoCivil;
		sesion.idSituacionLaboral = idSituacionLaboral;
		sesion.estado = EstadosBB.GUARDAR_ADICIONALES_OK;

		if (sesion.esRelacionDependencia()) {
			sesion.setCheckCuentaSueldo(contexto);
		}

		sesion.save();

		Objeto respuesta = respuesta();
		respuesta.set("idEstadoCivil", idEstadoCivil);
		respuesta.set("idSituacionLaboral", idSituacionLaboral);
		respuesta.set("check_cuenta_sueldo", sesion.getCheckCuentaSueldo());

		LogBB.evento(contexto, EstadosBB.GUARDAR_ADICIONALES_OK, respuesta);

		if(sesion.esFlujoTcv()){
			return respuesta();
		}

		return respuesta;
	}

	@SuppressWarnings("deprecation")
	public static Fecha convertirFecha(String fechaStr) {

		String[] fechaNacimientoArray = fechaStr.split("/");
		String anioNacimiento = fechaNacimientoArray[2];

		if (anioNacimiento.length() == 2) {
			Integer anioNacimientoInt = Integer.parseInt(anioNacimiento);
			Date fechaActual = new Date();
			String anioActual = String.valueOf(fechaActual.getYear()).substring(1, 3);
			Integer anioActualInt = Integer.parseInt(anioActual);

			if (anioNacimientoInt <= anioActualInt) {
				anioNacimiento = "20" + anioNacimiento;
			} else {
				anioNacimiento = "19" + anioNacimiento;
			}
			fechaStr = fechaNacimientoArray[0] + "/" + fechaNacimientoArray[1] + "/" + anioNacimiento;
		}

		return new Fecha(fechaStr, FORMATO_FECHA);
	}

	public static Objeto guardarAdicionalConyuge(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String nombres = contexto.parametros.string("nombres");
		String apellido = contexto.parametros.string("apellido");
		String genero = contexto.parametros.string("genero", "").trim();

		if (!esSexo(genero)) {
			genero = "M".equals(sesion.genero) ? "F" : "M";
		}

		contexto.parametros.set("sexo", genero);
		String numeroDocumento = contexto.parametros.string("numeroDocumento");
		String nacionalidad = contexto.parametros.string("nacionalidad");
		String paisResidencia = contexto.parametros.string("paisResidencia");
		Fecha fechaNacimiento = convertirFecha(contexto.parametros.string("fechaNacimiento"));

		if (sesion.numeroDocumento.equals(numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.CONYUGE_INVALIDO);
			LogBB.error(contexto, ErroresBB.CONYUGE_INVALIDO);
			return respuesta(ErroresBB.CONYUGE_INVALIDO);
		}

		if (empty(nacionalidad)) {
			nacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
		}

		if (empty(paisResidencia)) {
			paisResidencia = GeneralBB.DEFAULT_PAIS_NACIMIENTO;
		}

		Paises paises = ApiCatalogo.paises(contexto).tryGet();

		if (paises != null && !paises.isEmpty()) {

			Pais paisNacionalidad = nacionalidad.matches(".*[0-9].*") ? paises.buscarPaisById(nacionalidad) : paises.buscarPais(nacionalidad);
			if (paisNacionalidad != null) {
				sesion.conyuge.nacionalidad = paisNacionalidad.nacionalidad;
				sesion.conyuge.idNacionalidad = String.valueOf(paisNacionalidad.id);
			}

			Pais paisRecidencia = paisResidencia.matches(".*[0-9].*") ? paises.buscarPaisById(paisResidencia) : paises.buscarPais(paisResidencia);
			if (paisRecidencia != null) {
				sesion.conyuge.paisResidencia = paisRecidencia.descripcion;
				sesion.conyuge.idPaisResidencia = String.valueOf(paisRecidencia.id);
			}
		}

		if (GeneralBB.DEFAULT_NACIONALIDAD_DESC.equals(sesion.conyuge.nacionalidad) && Validadores.esExtranjero(numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.CONYUGE_INVALIDO_POR_NACIONALIDAD);
			LogBB.error(contexto, ErroresBB.CONYUGE_INVALIDO_POR_NACIONALIDAD);
			return respuesta(ErroresBB.CONYUGE_INVALIDO);
		} else if (!Validadores.esExtranjero(numeroDocumento) && !GeneralBB.DEFAULT_NACIONALIDAD_DESC.equals(sesion.conyuge.nacionalidad)) {
			sesion.conyuge.nacionalidad = GeneralBB.DEFAULT_NACIONALIDAD_DESC;
			sesion.conyuge.idNacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
		}

		sesion.conyuge.nombres = Validadores.filtroUpper(nombres);
		sesion.conyuge.apellido = Validadores.filtroUpper(apellido);
		sesion.conyuge.genero = genero;
		sesion.conyuge.numeroDocumento = numeroDocumento;
		sesion.conyuge.fechaNacimiento = fechaNacimiento;
		Objeto cuil = obtenerCuil(contexto);
		sesion.conyuge.cuil = cuil.string("cuil");
		sesion.estado = EstadosBB.GUARDAR_CONYUGE_OK;
		sesion.save();

		Objeto respuesta = respuesta();
		respuesta.set("conyuge", sesion.conyuge);

		LogBB.evento(contexto, EstadosBB.GUARDAR_CONYUGE_OK, respuesta);
		return respuesta;
	}

	public static Objeto guardarPersona(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;

		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();

		if (persona == null) {
			persona = ApiPersonas.crearPersona(contexto, cuil).tryGet();
		}

		if (persona == null) {
			LogBB.error(contexto, ErroresBB.PERSONA_NO_GUARDADA);
			return respuesta(ErroresBB.PERSONA_NO_GUARDADA);
		}

		// Sucursal asignada
		String idSucursalAsignada = GeneralBB.DEFAULT_SUCURSAL_ASIGNADA;
		if (!empty(sesion.idSucursal)) {
			idSucursalAsignada = String.valueOf(sesion.idSucursal);
		}
		persona.idSucursalAsignada = idSucursalAsignada;

		// Datos basicos
		persona.nombres = sesion.nombre;
		persona.apellidos = sesion.apellido;
		persona.idSexo = sesion.genero;

		// Documento
		persona.idTipoDocumento = sesion.tipoDocumento();
		persona.numeroDocumento = sesion.dni();
		persona.idVersionDocumento = sesion.ejemplar;
		persona.fechaNacimiento = sesion.fechaNacimiento;

		if (GeneralBB.TIPO_DOC_EXTRANJERO.equals(persona.idTipoDocumento) && GeneralBB.DEFAULT_NACIONALIDAD.equals(sesion.idNacionalidad)) {
			sesion.idNacionalidad = sesion.idPaisNacimiento;
		}

		sesion.idCobis = persona.idCliente;
		sesion.saveSesion();

		persona.idPaisNacimiento = sesion.idPaisNacimiento;
		persona.idNacionalidad = sesion.idNacionalidad;

		if (sesion.domicilioLegal == null) {
			LogBB.error(contexto, ErroresBB.DATOS_INCOMPLETOS);
			return respuesta(ErroresBB.DATOS_INCOMPLETOS);
		}
		persona.ciudadNacimiento = sesion.domicilioLegal.ciudad;

		// Laboral
		persona.idSituacionLaboral = sesion.idSituacionLaboral;

		if (GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(persona.idSituacionLaboral)) {

			persona.idTipoIDTributario = GeneralBB.CUIT;

			if (!empty(sesion.tipoSitLaboral) && GeneralBB.TIPO_SIT_LABORAL_A.equals(sesion.tipoSitLaboral)) {
				persona.idSituacionImpositiva = GeneralBB.SITUACION_IMP_AUTONOMOS;
				persona.idIva = GeneralBB.IVA_AUTONOMOS;
				persona.idGanancias = GeneralBB.DGI_AUTONOMOS;
			} else {
				persona.idSituacionImpositiva = GeneralBB.CUIT_SITUACION_IMPOSITIVA;
				persona.idIva = GeneralBB.CUIT_IVA;
				persona.idGanancias = GeneralBB.CUIT_GANANCIAS;
			}

		} else {
			persona.idTipoIDTributario = GeneralBB.CUIL;
			persona.idSituacionImpositiva = GeneralBB.CUIL_SITUACION_IMPOSITIVA;
			persona.idIva = GeneralBB.CUIL_IVA;
			persona.idGanancias = GeneralBB.CUIL_GANANCIAS;
		}

		// Perfil patrimonial
		// TODO-BB: Revisar como obtener perfil patrimonial por NOSIS
		/*
		 * if( clientePersonaAGuardar.getPerfilPatrimonial() == null ||
		 * clientePersonaAGuardar.getImportePerfilPatrimonial() == null ){
		 * 
		 * //SALARIO MINIMO VITAL Y MOVIL AL 04/12/2019
		 * //https://www.argentina.gob.ar/noticias/nuevo-aumento-del-salario-minimo-
		 * vital-y-movil-y-de-la-prestacion-por-desempleo Integer
		 * salarioMininoVitalMovil = 16875 * 2;
		 * 
		 * clientePersonaAGuardar.setPerfilPatrimonial(salarioMininoVitalMovil);
		 * clientePersonaAGuardar.setImportePerfilPatrimonial(salarioMininoVitalMovil);
		 * }
		 */

		// Conyuge
		persona.idEstadoCivil = sesion.idEstadoCivil;
		if (sesion.casada()) {
			persona.cantidadNupcias = sesion.idCantidadNupcias;
			persona.idSubtipoEstadoCivil = sesion.idSubtipoEstadoCivil;

			ConyugeBB conyuge = sesion.conyuge;
			if (!empty(conyuge)) {
				persona.nombreConyuge = conyuge.nombres;
				persona.apellidoConyuge = conyuge.apellido;
			}
		} else {
			persona.cantidadNupcias = "";
			persona.idSubtipoEstadoCivil = "";
		}

		// Condiciones
		persona.esPersonaFisica = true;
		persona.esPersonaJuridica = false;
		persona.esPEP = false;
		persona.fechaDeclaracionPEP = Fecha.hoy();
		persona.esSO = false;

		if (!empty(persona.idVersionDocumento)) {
			persona.idVersionDocumento = sesion.ejemplar;
		}

		if (empty(persona.idTipoIDTributario)) {
			persona.idTipoIDTributario = GeneralBB.CUIL;
		}

		if (persona.idEstadoCivil != null && GeneralBB.SOLTERA.equals(persona.idEstadoCivil.toUpperCase())) {
			persona.idSubtipoEstadoCivil = GeneralBB.SUBESTADO_CIVIL_SOLTERO;
		}

		try {
			persona = ApiPersonas.actualizarPersona(contexto, persona).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.PERSONA_NO_GUARDADA, e.response.body.toString());
			return respuesta(ErroresBB.PERSONA_NO_GUARDADA);
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.PERSONA_NO_GUARDADA, e.getMessage());
			return respuesta(ErroresBB.PERSONA_NO_GUARDADA);
		}

		Objeto relacion = terminarRelaciones(contexto);
		if (!BBValidacion.estadoOk(relacion)) {
			return respuesta(ErroresBB.ERROR_RELACIONES);
		}

		return respuesta("persona", persona);
	}

	public static Objeto terminarRelaciones(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;

		Relaciones relaciones = ApiPersonas.relaciones(contexto, cuil, false).tryGet();

		Relacion relacion = relaciones.conyugue();
		if (relacion != null && relacion.fechaFinRelacion == null) {
			relacion.fechaFinRelacion = Fecha.ahora();

			try {
				relacion = ApiPersonas.actualizarRelacion(contexto, cuil, relacion).tryGet();
			} catch (ApiException e) {
				LogBB.error(contexto, ErroresBB.RELACION_NO_TERMINADA, e.response.body.toString());
				return respuesta(ErroresBB.RELACION_NO_TERMINADA);
			} catch (Exception e) {
				LogBB.error(contexto, ErroresBB.RELACION_NO_TERMINADA, e.getMessage());
				return null;
			}
		}

		Objeto respuesta = respuesta();
		respuesta.set("relacion", relacion);

		return respuesta;
	}

	public static Objeto guardarConyuge(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		ConyugeBB conyuge = sesion.conyuge;

		if (empty(conyuge)) {
			LogBB.error(contexto, ErroresBB.CONYUGE_VACIO);
			return respuesta(ErroresBB.CONYUGE_VACIO);
		}

		String cuil = conyuge.cuil;

		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();

		if (persona == null) {
			persona = ApiPersonas.crearPersona(contexto, cuil).tryGet();
		}

		if (persona == null) {
			LogBB.error(contexto, ErroresBB.PERSONA_CONYUGE_NO_GUARDADA);
			return respuesta(ErroresBB.PERSONA_CONYUGE_NO_GUARDADA);
		}

		persona.nombres = conyuge.nombres;
		persona.apellidos = conyuge.apellido;
		persona.nombreConyuge = sesion.nombre;
		persona.apellidoConyuge = sesion.apellido;
		persona.idSexo = conyuge.genero;
		persona.idTipoDocumento = conyuge.tipoDocumento();
		persona.numeroDocumento = conyuge.numeroDocumento;
		persona.fechaNacimiento = conyuge.fechaNacimiento;
		persona.idNacionalidad = conyuge.idNacionalidad;
		persona.idPaisResidencia = conyuge.idPaisResidencia;
		persona.idPaisNacimiento = conyuge.idNacionalidad;
		persona.idEstadoCivil = GeneralBB.CASADA;
		persona.idSubtipoEstadoCivil = GeneralBB.UNION_CIVIL_COMUNIDAD;

		if (empty(persona.idVersionDocumento) || persona.idVersionDocumento.matches("[0-9]*")) {
			persona.idVersionDocumento = GeneralBB.DEFAULT_EJEMPLAR;
		}

		if (empty(persona.idTipoIDTributario)) {
			persona.idTipoIDTributario = GeneralBB.CUIL;
		}

		if (empty(persona.idIva) || empty(persona.idSituacionImpositiva)) {
			if (persona.idTipoIDTributario.equals(GeneralBB.CUIL)) {
				persona.idIva = GeneralBB.CUIL_IVA;
				persona.idGanancias = GeneralBB.CUIL_GANANCIAS;
			} else if (persona.idTipoIDTributario.equals(GeneralBB.CUIT)) {
				persona.idIva = GeneralBB.CUIT_IVA;
				persona.idGanancias = GeneralBB.CUIT_GANANCIAS;
				persona.categoriaMonotributo = GeneralBB.CATEGORIA_MONO;
			}
		}

		try {
			persona = ApiPersonas.actualizarPersona(contexto, persona).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.PERSONA_CONYUGE_NO_GUARDADA, e.response.body.toString());
			return respuesta(ErroresBB.PERSONA_CONYUGE_NO_GUARDADA);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.PERSONA_CONYUGE_NO_GUARDADA, e.getMessage());
			return null;
		}

		controlarActividadesConyuge(contexto);
		return respuesta("conyuge", persona);
	}

	public static Objeto guardarRelacionConyuge(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		ConyugeBB conyuge = sesion.conyuge;

		if (empty(conyuge)) {
			LogBB.error(contexto, ErroresBB.CONYUGE_VACIO);
			return respuesta(ErroresBB.CONYUGE_VACIO);
		}

		String cuit = sesion.cuil;
		String cuitRelacion = conyuge.cuil;

		Relaciones relaciones = ApiPersonas.relaciones(contexto, cuit, false).tryGet();
		if (relaciones == null) {
			LogBB.error(contexto, ErroresBB.RELACIONES_NO_ENCONTRADAS);
			return respuesta(ErroresBB.RELACIONES_NO_ENCONTRADAS);
		}

		NuevaRelacion nuevaRelacion = new NuevaRelacion();
		nuevaRelacion.cuitPersonaRelacionada = cuitRelacion;

		Relacion relacion = relaciones.crearActualizarTry(contexto, cuit, nuevaRelacion, Relacion.CONYUGUE);
		if (empty(relacion)) {
			relacion = ApiPersonas.crearRelacion(contexto, cuit, cuitRelacion, Relacion.CONYUGUE).tryGet();
		}

		return respuesta("relacionConyuge", relacion);
	}

	public static Objeto guardarDomicilio(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		DomicilioBB domicilioLegal = sesion.domicilioLegal;
		DomicilioBB domicilioPostal = sesion.domicilioPostal;

		Domicilios domicilios = ApiPersonas.domicilios(contexto, cuil, false).tryGet();
		if (domicilios == null) {
			domicilios = new Domicilios();
		}

		NuevoDomicilio nuevoDomicilioLegal = new NuevoDomicilio();

		nuevoDomicilioLegal.calle = domicilioLegal.calle;
		nuevoDomicilioLegal.idCiudad = domicilioLegal.idCiudad;
		nuevoDomicilioLegal.idProvincia = domicilioLegal.idProvincia;
		nuevoDomicilioLegal.idCodigoPostal = domicilioLegal.cp;
		nuevoDomicilioLegal.numero = domicilioLegal.numeroCalle;
		nuevoDomicilioLegal.piso = domicilioLegal.piso;
		nuevoDomicilioLegal.departamento = domicilioLegal.dpto;
		nuevoDomicilioLegal.idPais = GeneralBB.DEFAULT_PAIS_NACIMIENTO;

		try {
			domicilios.crearActualizarTry(contexto, cuil, nuevoDomicilioLegal, Domicilio.LEGAL);
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.DOM_LEGAL_NO_GUARDADO, e.response.body.toString());
			return respuesta(ErroresBB.DOM_LEGAL_NO_GUARDADO);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.DOM_LEGAL_NO_GUARDADO, e.getMessage());
			return null;
		}

		NuevoDomicilio nuevoDomicilioPostal = new NuevoDomicilio();

		nuevoDomicilioPostal.calle = domicilioPostal.calle;
		nuevoDomicilioPostal.idCiudad = domicilioPostal.idCiudad;
		nuevoDomicilioPostal.idProvincia = domicilioPostal.idProvincia;
		nuevoDomicilioPostal.idCodigoPostal = domicilioPostal.cp;
		nuevoDomicilioPostal.numero = domicilioPostal.numeroCalle;
		nuevoDomicilioPostal.piso = domicilioPostal.piso;
		nuevoDomicilioPostal.departamento = domicilioPostal.dpto;
		nuevoDomicilioPostal.idPais = GeneralBB.DEFAULT_PAIS_NACIMIENTO;

		try {
			domicilios.crearActualizarTry(contexto, cuil, nuevoDomicilioPostal, Domicilio.POSTAL);
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.DOM_POSTAL_NO_GUARDADO, e.response.body.toString());
			return respuesta(ErroresBB.DOM_POSTAL_NO_GUARDADO);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.DOM_POSTAL_NO_GUARDADO, e.getMessage());
			return null;
		}

		Objeto respuesta = respuesta();
		respuesta.set("domicilioPostal", domicilioPostal);
		respuesta.set("domicilioLegal", domicilioLegal);

		return respuesta;
	}

	public static void guardarDomicilioPostalTemprano(ContextoBB contexto) {
		try {
			if(!contexto.sesion().getGuardarCobisTemprano()
					|| !contexto.sesion().esFlujoTcv()
					|| !BBPoliticas.estaEnHorario(contexto, 30)){
				return;
			}

			LogBB.eventoHomo(contexto,"inicio - guardar domicilio postal temprano");

			SesionBB sesion = contexto.sesion();
			String cuil = sesion.cuil;
			DomicilioBB domicilioPostal = sesion.domicilioPostal;

			Domicilios domicilios = ApiPersonas.domicilios(contexto, cuil, false).tryGet();
			if (domicilios == null) {
				domicilios = new Domicilios();
			}

			NuevoDomicilio nuevoDomicilioPostal = new NuevoDomicilio();
			nuevoDomicilioPostal.calle = domicilioPostal.calle;
			nuevoDomicilioPostal.idCiudad = domicilioPostal.idCiudad;
			nuevoDomicilioPostal.idProvincia = domicilioPostal.idProvincia;
			nuevoDomicilioPostal.idCodigoPostal = domicilioPostal.cp;
			nuevoDomicilioPostal.numero = domicilioPostal.numeroCalle;
			nuevoDomicilioPostal.piso = domicilioPostal.piso;
			nuevoDomicilioPostal.departamento = domicilioPostal.dpto;
			nuevoDomicilioPostal.idPais = GeneralBB.DEFAULT_PAIS_NACIMIENTO;

			domicilios.crearActualizarTry(contexto, cuil, nuevoDomicilioPostal, Domicilio.POSTAL);
			LogBB.eventoHomo(contexto,"finalizado - guardar domicilio postal temprano");

		} catch (ApiException e) {
			contexto.sesion().setGuardarCobisTemprano(false);
			LogBB.eventoHomo(contexto,"error - guardar domicilio postal temprano");
		}
	}

	public static Objeto guardarMail(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String nuevoEmail = sesion.mail;

		if (!sesion.emailValido()) {
			LogBB.error(contexto, ErroresBB.EMAIL_INVALIDO);
			return respuesta(ErroresBB.EMAIL_INVALIDO);
		}

		Emails emails = ApiPersonas.emails(contexto, cuil, false).tryGet();
		if (emails == null) {
			emails = new Emails();
		}

		try {
			emails.crearActualizarTry(contexto, cuil, nuevoEmail, Email.PERSONAL);
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.EMAIL_NO_GUARDADO, e.response.body.toString());
			return respuesta(ErroresBB.EMAIL_NO_GUARDADO);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.EMAIL_NO_GUARDADO, e.getMessage());
			return null;
		}

		return respuesta("email", nuevoEmail);
	}

	public static Objeto guardarTelefono(ContextoBB contexto) {
		Objeto domicilios = guardarDomicilio(contexto);
		if (!BBValidacion.estadoOk(domicilios)) {
			return respuesta(ErroresBB.ERROR_DOMICILIO);
		}

		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String codArea = sesion.codArea;
		String celular = sesion.celular;

		Telefonos telefonos = ApiPersonas.telefonos(contexto, cuil, false).tryGet();
		if (telefonos == null) {
			telefonos = new Telefonos();
		}

		String codigoPais = Telefono.obtenerCodigoPais();
		String codigoArea = Telefono.obtenerCodigoArea(codArea);
		String prefijo = Telefono.obtenerPrefijo();
		String caracteristica = Telefono.obtenerCaracteristica(codArea, celular);
		String numero = Telefono.obtenerNumero(codArea, celular);

		NuevoTelefono nuevoTelefono = new NuevoTelefono();

		nuevoTelefono.codigoPais = codigoPais;
		nuevoTelefono.codigoArea = codigoArea;
		nuevoTelefono.prefijo = prefijo;
		nuevoTelefono.caracteristica = caracteristica;
		nuevoTelefono.numero = numero;

		try {
			telefonos.crearActualizarTry(contexto, cuil, nuevoTelefono, Telefono.CELULAR);
		} catch (ApiException e) {
			
			boolean hayError = true;
			if(e.response.body.toString().contains("EL DDN INGRESADO ES INEXISTENTE")) {
				codArea += "0";
				nuevoTelefono.codigoArea = Telefono.obtenerCodigoArea(codArea);
				nuevoTelefono.caracteristica = Telefono.obtenerCaracteristica(codArea, celular);
				nuevoTelefono.numero = Telefono.obtenerNumero(codArea, celular);
				try {
					telefonos.crearActualizarTry(contexto, cuil, nuevoTelefono, Telefono.CELULAR);						
					sesion.codArea = codArea;
					sesion.saveSesion();
					hayError = false;
				}catch(Exception exc) {}
			}
			else if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}
			
			if(hayError) {
				LogBB.error(contexto, ErroresBB.TELEFONO_CELULAR_NO_GUARDADO, e.response.body.toString());
				return respuesta(ErroresBB.TELEFONO_CELULAR_NO_GUARDADO);				
			}

		} catch (Exception e) {
			
			boolean hayError = true;
			if(e.getMessage().contains("EL DDN INGRESADO ES INEXISTENTE")) {
				codArea += "0";
				nuevoTelefono.codigoArea = Telefono.obtenerCodigoArea(codArea);
				nuevoTelefono.caracteristica = Telefono.obtenerCaracteristica(codArea, celular);
				nuevoTelefono.numero = Telefono.obtenerNumero(codArea, celular);
				try {
					telefonos.crearActualizarTry(contexto, cuil, nuevoTelefono, Telefono.CELULAR);						
					sesion.codArea = codArea;
					sesion.saveSesion();
					hayError = false;
				}catch(Exception exc) {}
			}
			else if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			if(hayError) {
				LogBB.error(contexto, ErroresBB.TELEFONO_CELULAR_NO_GUARDADO, e.getMessage());
				return respuesta(ErroresBB.TELEFONO_CELULAR_NO_GUARDADO);				
			}
		}
		
		Objeto respuesta = respuesta();
		respuesta.set("telefono.codigoPais", codigoPais);
		respuesta.set("telefono.codigoArea", codigoArea);
		respuesta.set("telefono.prefijo", prefijo);
		respuesta.set("telefono.caracteristica", caracteristica);
		respuesta.set("telefono.numero", numero);

		return respuesta;
	}

	public static void controlarActividadesTitular(ContextoBB contexto) {
		try{
			controlarActividadPrincipal(contexto, contexto.sesion().cuil);
			controlarFechasActividades(contexto, contexto.sesion().cuil);
		}catch(Exception e){
			LogBB.evento(contexto, "ERROR_ACTIVIDAD", "error controlar actividades titular");
		}
	}

	private static void controlarFechasActividades(ContextoBB contexto, String cuil) {

		Actividades actividades = ApiPersonas.actividades(contexto, cuil, false).tryGet();
		if (actividades == null || actividades.size() == 0) {
			return;
		}

		for (Actividad actividad : actividades) {
			controlarFechasActividad(contexto, cuil, actividad);
		}
	}

	private static void controlarActividadPrincipal(ContextoBB contexto, String cuil) {

		Actividades actividades = ApiPersonas.actividades(contexto, cuil, false).tryGet();
		if (actividades == null || actividades.size() == 0) {
			return;
		}

		Actividades actividadesPrincipales = new Actividades();
		Actividad ultimaActividadPrincipal = null;

		for (Actividad actividad : actividades) {

			if (empty(actividad.esPrincipal) || !actividad.esPrincipal) {
				continue;
			}

			if (empty(actividad.fechaInicioActividad)) {
				actividadesPrincipales.add(actividad);
				continue;
			}

			if (ultimaActividadPrincipal == null) {
				ultimaActividadPrincipal = actividad;
			}

			if (ultimaActividadPrincipal.fechaInicioActividad.esAnterior(actividad.fechaInicioActividad)) {
				ultimaActividadPrincipal = actividad;
			}

			actividadesPrincipales.add(actividad);
		}

		if (actividadesPrincipales.size() > 1) {

			if (ultimaActividadPrincipal == null) {
				ultimaActividadPrincipal = actividadesPrincipales.get(0);
			}

			for (Actividad actividad : actividadesPrincipales) {

				if (!actividad.id.equals(ultimaActividadPrincipal.id)) {
					actividad.fechaEgresoActividad = Fecha.ahora();
					actividad.esPrincipal = false;
					ApiPersonas.actualizarActividad(contexto, cuil, actividad).tryGet();
				}
			}
		}

		if (ultimaActividadPrincipal != null) {

			if (!GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(ultimaActividadPrincipal.idSituacionLaboral) && !GeneralBB.SITUACION_LABORAL_REL_DEPENDENCIA.equals(ultimaActividadPrincipal.idSituacionLaboral)) {
				return;
			}

			Boolean modificarActividad = false;

			if (empty(ultimaActividadPrincipal.ingresoNeto)) {
				ultimaActividadPrincipal.ingresoNeto = GeneralBB.INGRESO_NETO.multiply(new BigDecimal("12"));
				modificarActividad = true;
			}

			if (GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(ultimaActividadPrincipal.idSituacionLaboral) && empty(ultimaActividadPrincipal.resultadoDDJJGanancias)) {
				ultimaActividadPrincipal.resultadoDDJJGanancias = ultimaActividadPrincipal.ingresoNeto.multiply(new BigDecimal("2"));
				modificarActividad = true;
			}

			if (GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(ultimaActividadPrincipal.idSituacionLaboral) && empty(ultimaActividadPrincipal.ingresoAnualDDJJIIBB)) {
				ultimaActividadPrincipal.ingresoAnualDDJJIIBB = ultimaActividadPrincipal.resultadoDDJJGanancias.multiply(new BigDecimal("12"));
				modificarActividad = true;
			}

			if (modificarActividad) {
				ApiPersonas.actualizarActividad(contexto, cuil, ultimaActividadPrincipal).tryGet();
			}
		}
	}

	private static Boolean controlarFechasActividad(ContextoBB contexto, String cuil, Actividad actividad) {

		Boolean modificarActividad = false;

		if (empty(actividad.idSituacionLaboral)) {
			actividad.esPrincipal = false;
			modificarActividad = true;
		}

		if (empty(actividad.fechaInicioActividad) && empty(actividad.fechaEgresoActividad)) {
			actividad.fechaInicioActividad = Fecha.ahora().restarAños(1);
			modificarActividad = true;
		}

		if (empty(actividad.fechaInicioActividad) && !empty(actividad.fechaEgresoActividad)) {
			actividad.fechaInicioActividad = actividad.fechaEgresoActividad.restarAños(1);
			modificarActividad = true;
		}

		if (!empty(actividad.fechaInicioActividad) && !empty(actividad.fechaEgresoActividad)) {

			if (!actividad.fechaInicioActividad.esAnterior(actividad.fechaEgresoActividad)) {
				actividad.fechaInicioActividad = actividad.fechaEgresoActividad.restarAños(1);
				modificarActividad = true;
			}
		}

		if (modificarActividad) {
			Actividad actividadActualizada = ApiPersonas.actualizarActividad(contexto, cuil, actividad).tryGet();
			if (actividadActualizada == null) {
				return false;
			}
		}

		return true;
	}

	public static void guardarCobisTemprano(ContextoBB contexto) {
		try{
			if(contexto.sesion().getGuardarCobisTemprano()
					|| !contexto.sesion().esFlujoTcv()
					|| !BBPoliticas.estaEnHorario(contexto, 30)){
				return;
			}

			LogBB.eventoHomo(contexto,"inicio - guardar cobis temprano");

			Objeto persona = guardarPersona(contexto);
			if (!BBValidacion.estadoOk(persona)) {
				throw new Exception("Error");
			}

			Objeto mail = guardarMail(contexto);
			Objeto telefono = guardarTelefono(contexto);
			Objeto actividad = guardarActividades(contexto);

			if (!BBValidacion.estadoOk(mail)
					|| !BBValidacion.estadoOk(telefono)
					|| !BBValidacion.estadoOk(actividad)) {
				throw new Exception("Error");
			}

			contexto.sesion().setGuardarCobisTemprano(true);
			LogBB.eventoHomo(contexto, "finalizado - guardar cobis temprano");
		}
		catch (Exception e){
			LogBB.eventoHomo(contexto,"error - guardar cobis temprano");
		}
	}

	public static void guardarCobisTempranoFuture(ContextoBB contexto) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(() -> {
			BBPersona.guardarCobisTemprano(contexto);
		});

		try {
			future.get(15, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			LogBB.eventoHomo(contexto, "GUARDAR_COBIS_TIMEOUT");
			future.cancel(true);
		} catch (Exception e) { }
		finally {
			executor.shutdownNow();
		}
	}

	public static Objeto guardarCobis(ContextoBB contexto) {
		LogBB.eventoHomo(contexto, "inicio guardado en cobis");

		Objeto persona = guardarPersona(contexto);
		if (!BBValidacion.estadoOk(persona)){
			if(ErroresBB.ERROR_RELACIONES.equals(persona.string("estado"))){
				return respuesta(ErroresBB.ERROR_RELACIONES);
			}
			else {
				return respuesta(ErroresBB.ERROR_PERSONA);
			}
		}

		Objeto mail = guardarMail(contexto);
		if (!BBValidacion.estadoOk(mail)){
			return respuesta(ErroresBB.ERROR_MAIL);
		}

		Objeto telefono = guardarTelefono(contexto);
		if (!BBValidacion.estadoOk(telefono)){
			if(ErroresBB.ERROR_DOMICILIO.equals(telefono.string("estado"))){
				return respuesta(ErroresBB.ERROR_DOMICILIO);
			}
			else {
				return respuesta(ErroresBB.ERROR_TELEFONO);
			}
		}

		Objeto actividad = guardarActividadesFinal(contexto);
		if (!BBValidacion.estadoOk(actividad)){
			return respuesta(ErroresBB.ERROR_ACTIVIDADES);
		}

		if(contexto.sesion().casada()){
			Objeto personaConyuge = guardarConyuge(contexto);
			if (!BBValidacion.estadoOk(personaConyuge)){
				return respuesta(ErroresBB.ERROR_CONYUGE);
			}

			Objeto relacionConyuge = guardarRelacionConyuge(contexto);
			if (!BBValidacion.estadoOk(relacionConyuge)) {
				return respuesta(ErroresBB.ERROR_RELACION_CONYUGE);
			}

			Objeto domicilioConyuge = guardarDomicilioConyuge(contexto);
			if (!BBValidacion.estadoOk(domicilioConyuge)) {
				return respuesta(ErroresBB.ERROR_DOMICILIO_CONYUGE);
			}
		}

		return respuesta();
	}

	private static Objeto guardarDomicilioConyuge(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		ConyugeBB conyuge = sesion.conyuge;
		if (empty(conyuge)) {
			LogBB.error(contexto, ErroresBB.CONYUGE_VACIO);
			return respuesta(ErroresBB.CONYUGE_VACIO);
		}

		String cuil = conyuge.cuil;
		DomicilioBB domicilioLegal = sesion.domicilioLegal;
		DomicilioBB domicilioPostal = sesion.domicilioPostal;

		Domicilios domicilios = ApiPersonas.domicilios(contexto, cuil, false).tryGet();
		if (domicilios == null) {
			domicilios = new Domicilios();
		}

		NuevoDomicilio nuevoDomicilioLegal = new NuevoDomicilio();

		nuevoDomicilioLegal.calle = domicilioLegal.calle;
		nuevoDomicilioLegal.idCiudad = domicilioLegal.idCiudad;
		nuevoDomicilioLegal.idProvincia = domicilioLegal.idProvincia;
		nuevoDomicilioLegal.idCodigoPostal = domicilioLegal.cp;
		nuevoDomicilioLegal.numero = domicilioLegal.numeroCalle;
		nuevoDomicilioLegal.piso = domicilioLegal.piso;
		nuevoDomicilioLegal.departamento = domicilioLegal.dpto;
		nuevoDomicilioLegal.idPais = GeneralBB.DEFAULT_PAIS_NACIMIENTO;

		try {
			domicilios.crearActualizarTry(contexto, cuil, nuevoDomicilioLegal, Domicilio.LEGAL);
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.DOM_LEGAL_NO_GUARDADO, e.response.body.toString());
			return respuesta(ErroresBB.DOM_LEGAL_NO_GUARDADO);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.DOM_LEGAL_NO_GUARDADO, e.getMessage());
			return null;
		}

		NuevoDomicilio nuevoDomicilioPostal = new NuevoDomicilio();

		nuevoDomicilioPostal.calle = domicilioPostal.calle;
		nuevoDomicilioPostal.idCiudad = domicilioPostal.idCiudad;
		nuevoDomicilioPostal.idProvincia = domicilioPostal.idProvincia;
		nuevoDomicilioPostal.idCodigoPostal = domicilioPostal.cp;
		nuevoDomicilioPostal.numero = domicilioPostal.numeroCalle;
		nuevoDomicilioPostal.piso = domicilioPostal.piso;
		nuevoDomicilioPostal.departamento = domicilioPostal.dpto;
		nuevoDomicilioPostal.idPais = GeneralBB.DEFAULT_PAIS_NACIMIENTO;

		try {
			domicilios.crearActualizarTry(contexto, cuil, nuevoDomicilioPostal, Domicilio.POSTAL);
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.DOM_POSTAL_NO_GUARDADO, e.response.body.toString());
			return respuesta(ErroresBB.DOM_POSTAL_NO_GUARDADO);
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.DOM_POSTAL_NO_GUARDADO, e.getMessage());
			return null;
		}

		Objeto respuesta = respuesta();
		respuesta.set("domicilioPostal", domicilioPostal);
		respuesta.set("domicilioLegal", domicilioLegal);

		return respuesta;
	}

	private static void controlarActividadesConyuge(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		ConyugeBB conyuge = sesion.conyuge;
		if (empty(conyuge)) {
			return;
		}

		String cuil = conyuge.cuil;

		controlarSituacionLaboral(contexto, cuil);
		controlarActividadPrincipal(contexto, cuil);
		controlarFechasActividades(contexto, cuil);
	}

	private static void controlarSituacionLaboral(ContextoBB contexto, String cuil) {

		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
		if (persona == null) {
			return;
		}

		if (!empty(persona.idSituacionLaboral)) {
			return;
		}

		if (GeneralBB.SITUACION_LABORAL_REL_DEPENDENCIA.equals(persona.idSituacionLaboral) || GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(persona.idSituacionLaboral)) {
			return;
		}

		Actividades actividades = ApiPersonas.actividades(contexto, cuil, false).tryGet();
		if (actividades == null) {
			return;
		}

		for (Actividad actividad : actividades) {

			if (actividad.esPrincipal) {
				actividad.fechaEgresoActividad = Fecha.ahora();
				actividad.esPrincipal = false;
				ApiPersonas.actualizarActividad(contexto, cuil, actividad).tryGet();
			}
		}
	}

	private static Objeto guardarActividadesFinal(ContextoBB contexto) {
		Objeto actividadRes = guardarActividades(contexto);
		if (actividadRes == null || !actividadRes.string("estado").equals("0")) {
			return respuesta(ErroresBB.ERROR_ACTIVIDADES);
		}

		controlarActividadesTitular(contexto);
		return respuesta();
	}

	private static Objeto guardarActividades(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		if (!GeneralBB.SITUACION_LABORAL_IND_O_MONO.equals(sesion.idSituacionLaboral)) {
			return respuesta();
		}

		Actividades actividades = ApiPersonas.actividades(contexto, sesion.cuil, false).tryGet();
		if (Actividades.esActivo(actividades)) {
			return respuesta();
		}

		NuevaActividad nuevaActividad = new NuevaActividad();
		nuevaActividad.idSituacionLaboral = sesion.idSituacionLaboral;
		nuevaActividad.cuitEmpleador = sesion.cuil;
		nuevaActividad.ingresoNeto = GeneralBB.INGRESO_NETO.multiply(new BigDecimal("12"));
		nuevaActividad.resultadoDDJJGanancias = nuevaActividad.ingresoNeto.multiply(new BigDecimal("2"));
		nuevaActividad.ingresoAnualDDJJIIBB = nuevaActividad.resultadoDDJJGanancias.multiply(new BigDecimal("12"));
		nuevaActividad.esPrincipal = true;
		nuevaActividad.fechaInicioActividad = Fecha.hoy().restarAños(1);

		if (!empty(sesion.tipoSitLaboral)) {

			if (GeneralBB.TIPO_SIT_LABORAL_M.equals(sesion.tipoSitLaboral)) {
				nuevaActividad.categoriaMonotributo = GeneralBB.CATEGORIA_MONO;
			} else {
				nuevaActividad.razonSocialEmpleador = sesion.apellido + " " + sesion.nombre;
			}

			try {
				ApiPersonas.crearActividad(contexto, sesion.cuil, nuevaActividad).get();
			} catch (ApiException e) {
				LogBB.error(contexto, ErroresBB.ACTIVIDAD_NO_CREADA, e.response.body.toString());
				return respuesta(ErroresBB.ACTIVIDAD_NO_CREADA);
			} catch (Exception e) {
				LogBB.error(contexto, ErroresBB.ACTIVIDAD_NO_CREADA, e.getMessage());
				return null;
			}

		} else {

			nuevaActividad.categoriaMonotributo = GeneralBB.CATEGORIA_MONO;

			try {
				ApiPersonas.crearActividad(contexto, sesion.cuil, nuevaActividad).get();
			} catch (ApiException e) {
				LogBB.error(contexto, ErroresBB.ACTIVIDAD_NO_CREADA, e.response.body.toString());
				return respuesta(ErroresBB.ACTIVIDAD_NO_CREADA);
			} catch (Exception e) {
				LogBB.error(contexto, ErroresBB.ACTIVIDAD_NO_CREADA, e.getMessage());
				return null;
			}
		}

		return respuesta();
	}

	public static Objeto noConfirmarDatos(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		sesion.estado = EstadosBB.NO_CONFIRMA_DATOS;
		sesion.saveSesion();

		LogBB.evento(contexto, EstadosBB.NO_CONFIRMA_DATOS);
		return respuesta();
	}

	public static Boolean datosPersonalesVacios(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		String nombre = sesion.nombre;
		String apellido = sesion.apellido;
		String genero = sesion.genero;
		String nacionalidad = sesion.nacionalidad;
		Fecha fechaNacimiento = sesion.fechaNacimiento;
		String paisNacimiento = sesion.paisNacimiento;
		String numeroDocumento = sesion.numeroDocumento;
		String cuil = sesion.cuil;
		String ejemplar = sesion.ejemplar;

		Boolean datosVacios = false;
		datosVacios |= empty(nombre);
		datosVacios |= empty(apellido);
		datosVacios |= empty(genero);
		datosVacios |= empty(nacionalidad);
		datosVacios |= empty(fechaNacimiento);
		datosVacios |= empty(paisNacimiento);
		datosVacios |= empty(numeroDocumento);
		datosVacios |= empty(cuil);
		datosVacios |= empty(ejemplar);

		return datosVacios;
	}

	public static Objeto validarDatosPersonales(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		Fecha fechaNacimiento = sesion.fechaNacimiento;
		if (empty(fechaNacimiento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
			LogBB.error(contexto, ErroresBB.FECHA_NACIMIENTO_VACIA);
			return respuesta(ErroresBB.FECHA_NACIMIENTO_VACIA);
		}
		String fechaNacimientoStr = fechaNacimiento.string(FORMATO_FECHA);

		Boolean esMenorA18 = fechaNacimiento.esPosterior(Fecha.ahora().restarAños(18));
		if (esMenorA18) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
			LogBB.evento(contexto, ErroresBB.MENOR_DE_EDAD, fechaNacimientoStr);
			return respuesta(ErroresBB.MENOR_DE_EDAD);
		}

		Boolean datosVaciosVU = datosPersonalesVacios(contexto);
		if (datosVaciosVU) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_DATOS_PERSONALES);
			LogBB.error(contexto, ErroresBB.DATOS_VACIOS_VU);
			return respuesta(ErroresBB.DATOS_VACIOS_VU);
		}

		sesion.valDatosPersonales = true;
		sesion.actualizarEstado(EstadosBB.VALIDAR_DATOS_PERSONALES_OK);

		LogBB.evento(contexto, EstadosBB.VALIDAR_DATOS_PERSONALES_OK, contexto.sesion().token);
		return respuesta();
	}

	public static Objeto obtenerCuil(ContextoBB contexto) {
		String numeroDocumento = contexto.parametros.string("numeroDocumento");
		String sexo = contexto.parametros.string("sexo", "");
		Objeto respuesta = new Objeto();

		if (numeroDocumento.length() == 7) {
			numeroDocumento = "0" + numeroDocumento;
		}

		if (numeroDocumento.length() != 8) {
			respuesta.set("estado", "DOCUMENTO_INVALIDO");
			return respuesta;
		}

		Cuils resCuils = ApiPersonas.cuils(contexto, numeroDocumento).tryGet();

		if (resCuils == null && empty(sexo)) {
			respuesta.set("estado", "SEXO_VACIO");
			return respuesta;
		}

		if (resCuils == null && !sexo.equals("M") && !sexo.equals("F")) {
			respuesta.set("estado", "SEXO_INVALIDO");
			return respuesta;
		}

		if (resCuils == null) {
			respuesta.set("estado", "OK");
			respuesta.set("cuil", calcularCuil(numeroDocumento, sexo));
			return respuesta;
		}

		if (resCuils.size() == 1) {
			respuesta.set("estado", "OK");
			respuesta.set("cuil", resCuils.get(0).getCuil());
			return respuesta;
		}

		if (resCuils.size() != 1 && empty(sexo)) {
			respuesta.set("estado", "SEXO_VACIO");
			return respuesta;
		}

		if (!sexo.equals("M") && !sexo.equals("F")) {
			respuesta.set("estado", "SEXO_INVALIDO");
			return respuesta;
		}

		if (resCuils.isEmpty()) {
			String cuilGenerado = calcularCuil(numeroDocumento, sexo);
			LogBB.evento(contexto, "CUIL_GENERADO", contexto.parametros.toString(), cuilGenerado);

			respuesta.set("estado", "OK");
			respuesta.set("cuil", cuilGenerado);
			return respuesta;
		}

		String cuilPrimero = null;
		String cuilNoCliente = null;
		Integer cantSexo = 0;

		for (Integer i = 0; i < resCuils.size(); i++) {
			String cuil = resCuils.get(i).getCuil();
			Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
			if (persona != null) {
				if (sexo.equals(persona.idSexo)) {

					cantSexo++;
					if (empty(cuilPrimero)) {
						cuilPrimero = cuil;
					}

					contexto.parametros.set("cuil", cuil);
					Objeto clienteResp = esCliente(contexto);
					if (clienteResp.bool("continua")) {
						cuilNoCliente = cuil;
					}
				}
			}
		}

		if (cantSexo > 1) {
			LogBB.evento(contexto, "DNI_SEXO_REPETIDO", contexto.parametros.toJson(), !empty(cuilNoCliente) ? cuilNoCliente : cuilPrimero);
		}

		if (!empty(cuilPrimero)) {

			respuesta.set("estado", "OK");
			respuesta.set("cuil", !empty(cuilNoCliente) ? cuilNoCliente : cuilPrimero);
			return respuesta;
		}

		String cuilGenerado = calcularCuil(numeroDocumento, sexo);

		LogBB.evento(contexto, "CUIL_GENERADO", contexto.parametros.toJson(), cuilGenerado);
		respuesta.set("estado", "OK");
		respuesta.set("cuil", cuilGenerado);
		return respuesta;
	}

	public static String calcularCuil(String nroDocumento, String sexo) {

		String AB = sexo.equals("M") ? "20" : "27";
		List<Integer> listaAux = obtenerListaAux(AB, nroDocumento);
		List<Integer> multiplicadores = obtenerMultiplicadores();

		Integer sumatoria = 0;

		for (Integer i = 0; i < listaAux.size(); i++) {
			sumatoria += listaAux.get(i) * multiplicadores.get(i);
		}

		Integer resto = sumatoria % 11;
		String Z = String.valueOf(resto);

		if (resto == 0) {
			return AB + nroDocumento + Z;
		}

		if (resto == 1) {
			Z = sexo.equals("M") ? "9" : "4";
			return "23" + nroDocumento + Z;
		}

		Z = String.valueOf(11 - resto);

		return AB + nroDocumento + Z;
	}

	public static List<Integer> obtenerMultiplicadores() {

		List<Integer> multiplicadores = new ArrayList<Integer>();
		multiplicadores.add(5);
		multiplicadores.add(4);
		multiplicadores.add(3);
		multiplicadores.add(2);
		multiplicadores.add(7);
		multiplicadores.add(6);
		multiplicadores.add(5);
		multiplicadores.add(4);
		multiplicadores.add(3);
		multiplicadores.add(2);

		return multiplicadores;
	}

	public static List<Integer> obtenerListaAux(String AB, String nroDocumento) {

		String ABDocumento = AB + nroDocumento;
		String[] listaABDocumento = ABDocumento.split("");

		List<Integer> listaAux = new ArrayList<Integer>();

		for (Integer i = 0; i < listaABDocumento.length; i++) {
			listaAux.add(Integer.parseInt(listaABDocumento[i]));
		}

		return listaAux;
	}

	public static Objeto obtenerGuardarRespuestaCompletaVU(ContextoBB contexto) {

		if (contexto.esProduccion()) {
			return null;
		}

		String cuil = contexto.parametros.string("cuil");
		Boolean optInformation = contexto.parametros.bool("information", true);
		Boolean optOcr = contexto.parametros.bool("ocr", true);
		Boolean identical = contexto.parametros.bool("identical", true);
		Boolean optInformationCuil = contexto.parametros.bool("informationCuil", true);
		String nationality = contexto.parametros.string("nationality", "ARGENTINA");
		String birthPlace = contexto.parametros.string("birthPlace", "BUENOS AIRES");
		String birthdate = contexto.parametros.string("birthdate", "1995-10-30");

		Objeto addressDefault = new Objeto();
		addressDefault.set("zipCode", "1407");
		addressDefault.set("country", "ARGENTINA");
		addressDefault.set("numberStreet", "975");
		addressDefault.set("city", "BAHIA BLANCA");
		addressDefault.set("municipality", "BAHIA BLANCA");
		addressDefault.set("province", "BUENOS AIRES");
		addressDefault.set("streetAddress", "SAAVEDRA");
		addressDefault.set("floor", "2");
		addressDefault.set("department", "A");
		Objeto address = contexto.parametros.objeto("address", addressDefault);
		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();

		Objeto ocr = new Objeto();

		if (optOcr) {

			ocr.set("lastNames", persona.apellidos);
			ocr.set("gender", persona.idSexo);
			ocr.set("birthdate", birthdate);
			ocr.set("names", persona.nombres);
			ocr.set("number", persona.numeroDocumento);

			Objeto extra = new Objeto();

			String additional = "";

			additional += "{\"ExpiryDate\":\"2032-03-07\",\"BirthPlace\":\"";
			additional += birthPlace;
			additional += "\",\"Address\":\"";
			additional += address.get("streetAddress") + " - " + address.get("city") + " - " + address.get("municipality") + " - " + address.get("province");
			additional += "\",\"DETECTED_COUNTRY\":\"argentina\",\"OfIdent\":\"8028\",\"IssueDate\":\"2017-03-07\",\"CUIL\":\"";
			additional += cuil.substring(0, 2) + "-" + (persona.numeroDocumento.length() == 7 ? '0' + persona.numeroDocumento : persona.numeroDocumento) + "-" + cuil.substring(10);
			additional += "\",\"TramitNumber\":\"00482648695\",\"Nationality\":\"";
			additional += nationality;
			additional += "\"}";

			extra.set("additional", additional);

			String mrz = "";
			mrz += "{\"ExpiryDate\":\"2032-03-07\",\"Gender\":\"";
			mrz += persona.idSexo;
			mrz += "\",\"DocumentNumber\":\"";
			mrz += persona.numeroDocumento;
			mrz += "\",\"FullName\":\"";
			mrz += persona.apellidos + " " + persona.nombres;
			mrz += "\",\"BirthDate\":\"";
			mrz += birthdate;
			mrz += "\"}";

			extra.set("mrz", mrz);
			ocr.set("extra", extra);

			Objeto anomalies = new Objeto();
			anomalies.set("textValidationsDocumentData", "{\"BarcodeOcr\":\"0.24375\",\"BarcodeMrz\":\"0.234375\",\"DataGovernmentBarcode\":\"0.4875\"}");
			anomalies.set("areaValidationsDocumentAnomalies", null);
			anomalies.set("textValidationsTotal", "{\"Document\":\"0.46387056291103357\",\"Biometry\":\"0.45500001311302185\"}");
			anomalies.set("areaValidations", "{\"Front_Shield\":\"0.8306459784507751\",\"Reverse_Country\":\"0.8523945808410645\"}");
			anomalies.set("textValidationsBarcodeOcr", null);
			anomalies.set("textValidationsDataGovernmentBarcode", null);
			anomalies.set("textValidationsBarcodeMrz", null);
			anomalies.set("textValidationsDocument", null);
			ocr.set("anomalies", anomalies);
		}

		Objeto information = new Objeto();

		if (optInformation) {

			String person = "";
			person += "{\"person\":{\"number\":\"";
			person += persona.numeroDocumento;
			person += "\",\"names\":\"";
			person += persona.nombres;
			person += "\",\"nationality\":\"";
			person += nationality;
			person += "\",\"data\":{\"zipCode\":\"";
			person += address.get("zipCode");
			person += "\",\"country\":\"";
			person += address.get("country");
			person += "\",\"gender\":\"";
			person += persona.idSexo;
			person += "\",\"numberStreet\":\"";
			person += address.get("numberStreet");
			person += "\",\"city\":\"";
			person += address.get("city");
			person += "\",\"municipality\":\"";
			person += address.get("municipality");
			person += "\",\"creationDate\":\"07/03/2017\",\"valid\":\"true\",\"messageOfDeath\":\"Sin Aviso de Fallecimiento\",\"province\":\"";
			person += address.get("province");
			person += "\",\"streetAddress\":\"";
			person += address.get("streetAddress");
			person += "\",\"countryBirth\":\"";
			person += nationality;
			person += "\",\"copy\":\"B\",\"cuil\":\"";
			person += optInformationCuil ? cuil : "";
			person += "\",\"floor\":\"";
			person += address.get("floor");
			person += "\",\"department\":\"";
			person += address.get("department");
			person += "\",\"expirationDate\":\"07/03/2032\"},\"lastNames\":\"";
			person += persona.apellidos;
			person += "\",\"birthDate\":\"";

			String birthdateDay = birthdate.substring(8);
			String birthdateMonth = birthdate.substring(5, 7);
			String birthdateYear = birthdate.substring(0, 4);
			person += birthdateDay + "/" + birthdateMonth + "/" + birthdateYear;
			person += "\"}}";

			information.set("person", person);
		}

		Objeto respuesta = respuesta();
		respuesta.set("idOperacion", "4470");
		respuesta.set("confidence", 0.91);
		respuesta.set("confidenceTotal", 0.91887057);
		respuesta.set("ocr", optOcr ? ocr : "");
		respuesta.set("barcode", null);
		respuesta.set("confidenceDocument", 0.9277411);
		respuesta.set("information", optInformation ? information : "");
		respuesta.set("identical", identical);

		return respuesta;
	}

	public static Objeto obtenerDatosCobis(ContextoBB contexto) {
		return respuesta();
	}

	public static Objeto validarPersona(ContextoBB contexto) {
		LogBB.evento(contexto, "REQUEST_VALIDAR_PERSONA", contexto.parametros);

		SesionBB sesion = contexto.sesion();
		String numeroDocumento = sesion.numeroDocumento;
		String numeroTramite = contexto.parametros.string("numeroTramite", null);
		String sexo = contexto.parametros.string("sexo", sesion.genero);
		String scanner = contexto.parametros.string("scanner", null);
		String ejemplarScanner = null;

		if (!empty(scanner)) {
			numeroTramite = buscarNroTramitebyScanner(scanner);
			sexo = buscarSexoByScanner(scanner);
			numeroDocumento = buscarDniByScanner(scanner, sexo);
			ejemplarScanner = buscarEjemplarByScanner(scanner, "@");
		}

		if (!esNroTramite(numeroTramite) || !esSexo(sexo) || !esDni(numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
			LogBB.evento(contexto, ErroresBB.DATOS_INCORRECTOS);
			return respuesta(ErroresBB.DATOS_INCORRECTOS);
		}

		ConsultaPersona persona = ApiViviendas.validaciones(contexto, numeroTramite, numeroDocumento, sexo).tryGet();
		LogBB.evento(contexto, "SERVICIO_VIVIENDAS", persona);

		if (empty(persona) || empty(persona.codigo) || persona.codigo.equals(ErroresBB.COD_PERSONA_NO_ENCONTRADA)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
			LogBB.evento(contexto, ErroresBB.PERSONA_NO_ENCONTRADA, persona);
			return respuesta(ErroresBB.PERSONA_NO_ENCONTRADA);
		}

		if (persona.codigo.equals(ErroresBB.COD_DATOS_INCORRECTOS)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
			LogBB.evento(contexto, ErroresBB.DATOS_INCORRECTOS);
			return respuesta(ErroresBB.DATOS_INCORRECTOS);
		}

		if (!persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_OK) && !persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_FIRMADA)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
			LogBB.evento(contexto, ErroresBB.ERROR_INTERNO, persona);
			return respuesta(ErroresBB.ERROR_INTERNO);
		}

		if (!empty(numeroDocumento)) {
			if (numeroDocumento.length() == 7) {
				numeroDocumento = "0" + numeroDocumento;
			}
			sesion.numeroDocumento = numeroDocumento;
			sesion.genero = Validadores.filtroUpper(sexo);
		}

		String cuil = persona.cuil.trim();

		if(!sesion.esFlujoTcv()){
			if (!empty(cuil) && cuil.length() == 11) {

				if (!cuil.equals(sesion.cuil)) {
					LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuil);
					sesion.cuil = cuil;
				}

			} else if (!sesion.cuil.contains(numeroDocumento)) {
				sesion.numeroDocumento = numeroDocumento;
				sesion.genero = sexo;

				String cuilRes = getCuil(contexto, sesion);
				LogsBuhoBank.actualizarCuilSesion(contexto, sesion, cuilRes);
				sesion.cuil = cuilRes;
			}

			if (esClienteBool(contexto, sesion.cuil)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
				LogBB.evento(contexto, ErroresBB.ERROR_YA_CLIENTE);
				return respuesta(ErroresBB.ERROR_INTERNO);
			}
		}

		if (!Validadores.mismaPersona(contexto, sesion.cuil, numeroDocumento)) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VU);
			LogBB.evento(contexto, ErroresBB.DOCUMENTO_DIFERENTE);
			return respuesta(ErroresBB.DOCUMENTO_DIFERENTE);
		}

		if (!empty(ejemplarScanner) && !empty(persona.ejemplar) && !ejemplarScanner.equals(persona.ejemplar)) {
			LogBB.evento(contexto, GeneralBB.ERROR_EJEMPLAR_DIFERENTE, "ejemplar_renaper: " + persona.ejemplar + " | ejemplar_onboarding: " + ejemplarScanner);
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
			return respuesta(GeneralBB.ERROR_EJEMPLAR_DIFERENTE);
		}

		if (!empty(persona.vencimiento)) {

			Fecha fechaVencimiento = new Fecha(persona.vencimiento, GeneralBB.FORMATO_FECHA);

			if (!empty(fechaVencimiento) && !Fecha.ahora().esAnterior(fechaVencimiento)) {

				LogBB.evento(contexto, GeneralBB.ERROR_FECHA_VENCIMIENTO, persona.vencimiento);

				if (!contexto.esProduccion()) {
					sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
					return respuesta(GeneralBB.ERROR_FECHA_VENCIMIENTO);
				}
			}
		}

		if (!tieneSelfies(contexto, sesion.cuil)) {
			LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_SIN_SELFIES, sesion.token);
			return respuesta(ErroresBB.SERVICIO_DESHABILITADO);
		}

		sesion.nombre = Validadores.filtroUpper(persona.nombres);
		sesion.apellido = Validadores.filtroUpper(persona.apellido);
		sesion.genero = Validadores.filtroUpper(sexo);
		sesion.numeroDocumento = numeroDocumento;

		Objeto personaObj = new Objeto();
		personaObj.set("fechaNacimiento", persona.fecha_nacimiento);

		String formatoFecha = FORMATO_FECHA;
		if (persona.fecha_nacimiento != null && persona.fecha_nacimiento.toString().contains("-")) {
			formatoFecha = FORMATO_FECHA_OCR;
		}

		sesion.fechaNacimiento = personaObj.fecha("fechaNacimiento", formatoFecha);
		sesion.ejemplar = Validadores.filtroUpper(persona.ejemplar);

		sesion.domicilioLegal.localidad = Validadores.filtroUpper(persona.municipio);
		sesion.domicilioLegal.calle = persona.calle;

		String numeroCalle = quitarPrimerosCeros(persona.numero);
		sesion.domicilioLegal.numeroCalle = esNumero(numeroCalle) ? numeroCalle : null;
		String piso = quitarPrimerosCeros(persona.departamento);
		String dpto = quitarPrimerosCeros(persona.piso);
		if (piso.equals("P.BA")) {
			piso = "";
			dpto = "PB";
		}
		piso = piso.length() > 2 ? "" : piso;
		dpto = dpto.length() > 3 ? "" : dpto;
		sesion.domicilioLegal.piso = quitarPrimerosCeros(piso);
		sesion.domicilioLegal.dpto = quitarPrimerosCeros(dpto);

		String cp = quitarPrimerosCeros(persona.codigo_postal);
		cp = cp.length() > 4 ? "" : cp;
		sesion.domicilioLegal.cp = cp;

		String ciudad = persona.ciudad;
		ciudad = ciudad != null && ciudad.length() > 40 ? ciudad.substring(0, 40) : ciudad;
		sesion.domicilioLegal.ciudad = Validadores.filtroUpper(ciudad);
		
		sesion.domicilioLegal.provincia = Validadores.filtroUpper(persona.provincia);
		sesion.domicilioLegal.pais = Validadores.filtroUpper(persona.pais);

		CiudadWF ciudadPorCp = DomicilioBB.ciudadPorCP(contexto, cp);
		if (ciudadPorCp != null) {
			sesion.domicilioLegal.idCiudad = ciudadPorCp.CIU_Id;
			sesion.domicilioLegal.idProvincia = ciudadPorCp.CIU_PRV_Id;
			sesion.domicilioLegal.idPais = ciudadPorCp.CIU_PAI_Id;
		}

		sesion.idTipoIDTributario = GeneralBB.CUIL;
		sesion.domicilioPostal = sesion.domicilioLegal;
		sesion.estado = EstadosBB.VALIDAR_PERSONA_OK;
		sesion.save();

		Fecha fechaNacimiento = sesion.fechaNacimiento;
		if (!empty(fechaNacimiento)) {

			Boolean esMenorA18 = fechaNacimiento.esPosterior(Fecha.ahora().restarAños(18));
			if (esMenorA18) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_PERSONA);
				LogBB.evento(contexto, ErroresBB.MENOR_DE_EDAD, fechaNacimiento.string(FORMATO_FECHA));
				return respuesta(ErroresBB.MENOR_DE_EDAD);
			}
		}

		guardarNacionalidadCobis(contexto);

		guardarPersonaRenaper(contexto, sesion, null, persona);

		SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesion.cuil).tryGet();
		if (sesionStandBy == null) {
			SqlEsales.crearSesionStandBy(contexto, sesion.cuil);
			LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CREADA, sesion.token);
		}

		Objeto respuesta = respuesta();
		respuesta.set("sesion.cuil", sesion.cuil);
		respuesta.set("sesion.numeroDocumento", sesion.numeroDocumento);
		respuesta.set("sesion.nombre", sesion.nombre);
		respuesta.set("sesion.apellido", sesion.apellido);
		respuesta.set("sesion.genero", sesion.genero);
		respuesta.set("sesion.fechaNacimiento", sesion.fechaNacimiento);
		respuesta.set("sesion.paisNacimiento", sesion.paisNacimiento);
		respuesta.set("sesion.nacionalidad", sesion.nacionalidad);
		respuesta.set("sesion.idPaisNacimiento", sesion.idPaisNacimiento);
		respuesta.set("sesion.idNacionalidad", sesion.idNacionalidad);
		respuesta.set("sesion.idTipoIDTributario", sesion.idTipoIDTributario);
		respuesta.set("sesion.ejemplar", sesion.ejemplar);
		respuesta.set("sesion.domicilioLegal", sesion.domicilioLegal);
		respuesta.set("sesion.domicilioPostal", sesion.domicilioPostal);

		LogBB.evento(contexto, EstadosBB.VALIDAR_PERSONA_OK, respuesta);
		return respuesta;
	}

	private static void guardarNacionalidadCobis(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		Persona personaBH = ApiPersonas.persona(contexto, sesion.cuil, false).tryGet();
		if (empty(personaBH)) {

			if (!sesion.esExtranjero()) {
				sesion.idNacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
				sesion.nacionalidad = GeneralBB.DEFAULT_NACIONALIDAD_DESC;
				sesion.idPaisNacimiento = GeneralBB.DEFAULT_PAIS_NACIMIENTO;
				sesion.paisNacimiento = GeneralBB.DEFAULT_PAIS_NACIMIENTO_DESC;
				sesion.save();
			}
		} else {

			if (!empty(personaBH.idCliente)) {
				sesion.idCobis = personaBH.idCliente;
				sesion.save();
			}

			if (!empty(personaBH.idNacionalidad) && !empty(personaBH.idPaisNacimiento)) {

				if (sesion.esNacionalidadInvalida(personaBH.idNacionalidad)) {
					return;
				}

				Paises paises = ApiCatalogo.paises(contexto).tryGet();

				sesion.idNacionalidad = personaBH.idNacionalidad;
				Pais paisNacionalidad = paises.buscarPaisById(personaBH.idNacionalidad);
				sesion.nacionalidad = paisNacionalidad == null ? null : paisNacionalidad.descripcion;

				sesion.idPaisNacimiento = personaBH.idPaisNacimiento;
				Pais paisNacimiento = paises.buscarPaisById(personaBH.idPaisNacimiento);
				sesion.paisNacimiento = paisNacimiento == null ? null : paisNacimiento.descripcion;

				sesion.save();
			}
		}
	}

	static boolean tieneSelfies(ContextoBB contexto, String cuil) {
		SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, cuil).tryGet();
		if (sesionStandBy != null) {
			return true;
		}

		Objeto datosVU = obtenerDatosVUByCuil(contexto, cuil, 1);
		return datosVU != null && datosVU.bool("doc");
	}

	private static String buscarSexoByScanner(String scanner) {

		String[] parts = scanner.split("@");

		for (int i = 0; i < parts.length; i++) {
			String dato = empty(parts[i]) ? null : parts[i].trim();
			if (esSexo(dato)) {
				return dato;
			}
		}

		return null;
	}

	private static String buscarNroTramitebyScanner(String scanner) {

		String[] parts = scanner.split("@");

		for (int i = 0; i < parts.length; i++) {
			String dato = empty(parts[i]) ? null : parts[i].trim();
			if (esNroTramite(dato)) {
				return dato;
			}
		}

		return null;
	}

	private static String buscarDniByScanner(String scanner, String sexo) {

		String[] parts = scanner.split("@");

		for (int i = 0; i < parts.length; i++) {
			String dato = empty(parts[i]) ? null : parts[i].trim();
			if (!empty(dato) && dato.length() == 8 && dato.substring(0, 1).equals(sexo)) {
				dato = "0" + dato.substring(1, dato.length());
			}

			if (esDni(dato)) {
				return dato;
			}
		}

		return null;
	}

	static String buscarEjemplarByScanner(String scanner, String separador) {

		if (empty(scanner) || empty(separador)) {
			return null;
		}

		String[] parts = scanner.split(separador);

		Integer countSexo = 0;
		String segundoSexo = null;

		for (int i = 0; i < parts.length; i++) {
			String dato = empty(parts[i]) ? null : parts[i].trim();
			if (esEjemplar(dato)) {
				return dato;
			}

			if (esSexo(dato)) {
				countSexo++;
			}

			if (empty(segundoSexo) && countSexo == 2) {
				segundoSexo = dato;
			}
		}

		return segundoSexo;
	}

	public static Objeto guardarNacionalidad(ContextoBB contexto) {
		String idNacionalidad = contexto.parametros.string("idNacionalidad", null);
		String idPaisNacimiento = contexto.parametros.string("idPaisNacimiento", null);

		SesionBB sesion = contexto.sesion();
		if (!sesion.esExtranjero()) {
			idNacionalidad = GeneralBB.DEFAULT_NACIONALIDAD;
		} else {

			if (GeneralBB.DEFAULT_NACIONALIDAD.equals(idNacionalidad)) {
				idNacionalidad = idPaisNacimiento;
			}
		}

		if (!empty(idNacionalidad) && !empty(idPaisNacimiento)) {

			if (sesion.esNacionalidadInvalida(idNacionalidad)) {
				sesion.actualizarEstadoError(ErroresBB.ETAPA_GUARDAR_NACIONALIDAD);
				LogBB.error(contexto, ErroresBB.DATOS_INVALIDOS);
				return respuesta(ErroresBB.DATOS_INVALIDOS);
			}

			Paises paises = ApiCatalogo.paises(contexto).tryGet();

			sesion.idNacionalidad = idNacionalidad;
			Pais paisNacionalidad = paises.buscarPaisById(idNacionalidad);
			sesion.nacionalidad = paisNacionalidad.descripcion;

			sesion.idPaisNacimiento = idPaisNacimiento;
			Pais paisNacimiento = paises.buscarPaisById(idPaisNacimiento);
			sesion.paisNacimiento = paisNacimiento.descripcion;
			sesion.estado = EstadosBB.GUARDAR_NACIONALIDAD_OK;
			sesion.saveSesion();

			Objeto respuesta = respuesta();
			respuesta.set("sesion.idNacionalidad", sesion.idNacionalidad);
			respuesta.set("sesion.nacionalidad", sesion.nacionalidad);
			respuesta.set("sesion.idPaisNacimiento", sesion.idPaisNacimiento);
			respuesta.set("sesion.paisNacimiento", sesion.paisNacimiento);

			LogBB.evento(contexto, EstadosBB.GUARDAR_NACIONALIDAD_OK, respuesta);
			return respuesta;
		}

		sesion.actualizarEstadoError(ErroresBB.ETAPA_GUARDAR_NACIONALIDAD);
		LogBB.error(contexto, ErroresBB.DATOS_INCOMPLETOS);
		return respuesta(ErroresBB.DATOS_INCOMPLETOS);
	}

	public static Boolean esNroTramite(String numeroTramite) {
		return !empty(numeroTramite) && numeroTramite.length() == 11 && numeroTramite.substring(0, 2).equals("00");
	}

	public static Boolean esFecha(String dato) {
		return !empty(dato) && dato.length() == 10 && dato.contains("/");
	}

	public static Boolean esSexo(String sexo) {
		return !empty(sexo) && (sexo.equals("M") || sexo.equals("F") || sexo.equals("X"));
	}

	public static Boolean esDni(String dni) {
		return !empty(dni) && (dni.length() == 7 || dni.length() == 8) && esNumero(dni);
	}

	private static boolean esEjemplar(String ejemplar) {
		return !empty(ejemplar) && ejemplar.length() == 1 && !esSexo(ejemplar) && !esNumero(ejemplar);
	}

	private static boolean esNumero(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static Object obtenerRenaperFinalizados(ContextoBB contexto) {

		String cuil = contexto.parametros.string("cuil", null);

		if (cuil == null) {
			return null;
		}

		PersonaRenaper personaRenaper = SqlEsales.get(contexto, cuil).tryGet();

		if (personaRenaper == null || !EstadosBB.FINALIZAR_OK.equals(personaRenaper.estado)) {
			return null;
		}

		Objeto respuesta = respuesta();

		respuesta.set("idDispositivo", personaRenaper.id_dispositivo);

		respuesta.set("idTramite", personaRenaper.id_tramite);
		respuesta.set("ejemplar", personaRenaper.ejemplar);
		respuesta.set("vencimiento", personaRenaper.vencimiento);
		respuesta.set("fechaEmision", personaRenaper.fecha_emision);
		respuesta.set("fechaNacimiento", personaRenaper.fecha_nacimiento);

		respuesta.set("cuil", personaRenaper.cuil);
		respuesta.set("apellido", personaRenaper.apellido);
		respuesta.set("nombre", personaRenaper.nombre);

		respuesta.set("pais", personaRenaper.pais);
		respuesta.set("provincia", personaRenaper.provincia);
		respuesta.set("ciudad", personaRenaper.ciudad);
		respuesta.set("municipio", personaRenaper.municipio);
		respuesta.set("barrio", personaRenaper.barrio);
		respuesta.set("monoblock", personaRenaper.monoblock);

		respuesta.set("codigoPostal", personaRenaper.codigo_postal);
		respuesta.set("calle", personaRenaper.calle);
		respuesta.set("numero", personaRenaper.numero);
		respuesta.set("piso", personaRenaper.piso);
		respuesta.set("departamento", personaRenaper.departamento);

		return respuesta;
	}

	public static Object obtenerRenaperQr(ContextoBB contexto) {

		String scanner = contexto.parametros.string("scanner", null);

		String numeroDocumento = contexto.parametros.string("numeroDocumento", null);
		String numeroTramite = contexto.parametros.string("numeroTramite", null);
		String sexo = contexto.parametros.string("sexo", null);

		if (!empty(scanner)) {
			numeroTramite = buscarNroTramitebyScanner(scanner);
			sexo = buscarSexoByScanner(scanner);
			numeroDocumento = buscarDniByScanner(scanner, sexo);
		}

		if (!esNroTramite(numeroTramite) || !esSexo(sexo) || !esDni(numeroDocumento)) {
			return respuesta(ErroresBB.DATOS_INCORRECTOS);
		}

		ConsultaPersona persona = ApiViviendas.validaciones(contexto, numeroTramite, numeroDocumento, sexo).tryGet();
		LogBB.evento(contexto, "SERVICIO_VIVIENDAS", persona);

		if (empty(persona) || persona.codigo.equals(ErroresBB.COD_PERSONA_NO_ENCONTRADA)) {
			return respuesta(ErroresBB.PERSONA_NO_ENCONTRADA);
		}

		if (persona.codigo.equals(ErroresBB.COD_DATOS_INCORRECTOS)) {
			return respuesta(ErroresBB.DATOS_INCORRECTOS);
		}

		if (!persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_OK) && !persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_FIRMADA)) {
			return respuesta(ErroresBB.ERROR_INTERNO);
		}

		Objeto respuesta = respuesta();
		respuesta.set("datos", persona);

		return respuesta;
	}

	public static Object guardarLocalizacion(ContextoBB contexto) {

		String latitud = contexto.parametros.string("latitud", null);
		String longitud = contexto.parametros.string("longitud", null);

		SesionBB sesion = contexto.sesion();

		sesion.latitud = BBSeguridad.stringToBigDecimal(latitud);
		sesion.longitud = BBSeguridad.stringToBigDecimal(longitud);

		sesion.saveSesionbb2();

		Objeto respuesta = respuesta();
		respuesta.set("latitud", latitud);
		respuesta.set("longitud", longitud);

		return respuesta;
	}

	public static Object actualizarSesion(ContextoBB contexto) {

		LogBB.evento(contexto, "REQUEST_PUT_SESION", contexto.parametros);

		String operationId = contexto.parametros.string("operation_id", null);
		String operationGuid = contexto.parametros.string("operation_guid", null);

		String latitud = contexto.parametros.string("latitud", null);
		String longitud = contexto.parametros.string("longitud", null);

		Boolean esExpuestaPolitica = contexto.parametros.bool("es_expuesta_politica", null);
		Boolean esSujetoObligado = contexto.parametros.bool("es_sujeto_obligado", null);
		Boolean esFatcaOcde = contexto.parametros.bool("es_fatca_ocde", null);
		Boolean lavadoDinero = contexto.parametros.bool("lavado_dinero", null);

		String estado = contexto.parametros.string("estado", null);
		String urlQr = contexto.parametros.string("url_qr", null);
		String codeVU = contexto.parametros.string("code_vu", null);

		SesionBB sesion = contexto.sesion();

		if (operationId != null && operationGuid != null) {
			sesion.operationVU = operationId + "/" + operationGuid;
		}

		if (latitud != null) {
			sesion.latitud = BBSeguridad.stringToBigDecimal(latitud);
		}

		if (longitud != null) {
			sesion.longitud = BBSeguridad.stringToBigDecimal(longitud);
		}

		if (esExpuestaPolitica != null) {
			sesion.esExpuestaPolitica = esExpuestaPolitica;
		}

		if (esSujetoObligado != null) {
			sesion.esSujetoObligado = esSujetoObligado;
		}

		if (esFatcaOcde != null) {
			sesion.esFatcaOcde = esFatcaOcde;
		}

		if (lavadoDinero != null) {
			sesion.lavadoDinero = lavadoDinero;
		}

		if (estado != null) {
			sesion.estado = estado;
		}

		if (urlQr != null) {
			sesion.sucursalOnboarding = sesion.inicializarFlujo(contexto, urlQr) + "|" + urlQr;
		}
		
		if(codeVU != null) {
			sesion.codeVU = codeVU;
		}
		
		sesion.save();

		return respuesta("sesion", sesion.getSesion());
	}

	public static Objeto obtenerDatosVu(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		String cuil = contexto.parametros.string("cuil", null);
		Integer idOperation = contexto.parametros.integer("id_operation", null);
		String guidOperation = contexto.parametros.string("guid_operation", null);

		if (empty(cuil)) {
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		if (!empty(idOperation) && !empty(guidOperation)) {
			String urlBase = sesion.getUrlVU(contexto);
			String privateKey = sesion.getParamKeyPrivadaVU(contexto);
			if (empty(urlBase) || empty(privateKey)) {
				return null;
			}

			return BackofficevuBB.obtenerDatosVU(contexto, urlBase, privateKey, cuil, idOperation, guidOperation);
		}

		Objeto datosVU = obtenerDatosVUByCuil(contexto, cuil, 3);
		if (datosVU == null) {
			return respuesta("ERROR");
		}

		return datosVU;
	}

	static Objeto obtenerDatosVUByCuil(ContextoBB contexto, String cuil, Integer maxImagenes) {

		if (empty(cuil)) {
			return null;
		}

		Fecha fechaDesde = Fecha.ahora().restarDias(10);
		SesionesEsalesBB2 sesionesEsalesBB2 = SqlEsales.sesionEsalesBB2ByCuil(contexto, cuil, fechaDesde).tryGet();
		if (sesionesEsalesBB2 == null || sesionesEsalesBB2.size() == 0) {
			return null;
		}

		SesionBB sesion = contexto.sesion();
		String urlBase = sesion.getUrlVU(contexto);
		String privateKey = sesion.getParamKeyPrivadaVU(contexto);
		if (empty(urlBase) || empty(privateKey)) {
			return null;
		}

		Objeto ocr = new Objeto();
		String ocrRaw = null;
		Objeto barcode = new Objeto();
		Objeto mrz = new Objeto();
		List<String> documentFronts = new ArrayList<String>();
		List<String> documentBacks = new ArrayList<String>();
		List<Objeto> selfiesAux = new ArrayList<Objeto>();
		List<String> operationVUAux = new ArrayList<String>();

		for (SesionEsalesBB2 sesionEsalesBB2 : sesionesEsalesBB2) {

			if (documentFronts.size() >= maxImagenes && documentBacks.size() >= maxImagenes && selfiesAux.size() >= maxImagenes) {
				if (!ocr.isEmpty()) {
					break;
				}
			}

			String operationVU = sesionEsalesBB2.operation_vu;
			if (!validarOperationVU(operationVU) || operationVUAux.contains(operationVU)) {
				continue;
			}

			operationVUAux.add(operationVU);

			Integer operationId = getInt(operationVU.split("/")[0]);
			String operationGuid = operationVU.split("/")[1];

			Objeto objeto = BackofficevuBB.obtenerDatosVU(contexto, urlBase, privateKey, sesionEsalesBB2.usuario_vu, operationId, operationGuid);
			if (objeto == null) {
				continue;
			}

			if (objeto.objeto("ocr", null) == null) {
				continue;
			}

			String documentFront = objeto.string("document.front", null);
			if (!empty(documentFront) && documentFronts.size() <= maxImagenes) {
				documentFronts.add(documentFront);
			}

			String documentBack = objeto.string("document.back", null);
			if (!empty(documentBack) && documentBacks.size() <= maxImagenes) {
				documentBacks.add(documentBack);
			}

			List<Objeto> selfies = objeto.objetos("selfies");
			if (selfies != null && selfies.size() > 0 && selfiesAux.size() <= maxImagenes) {
				selfiesAux.addAll(selfies);
			}

			Objeto ocrRes = objeto.objeto("ocr", null);
			if (ocr.isEmpty() && !empty(ocrRes)) {
				ocr = ocrRes;
			}

			String ocrRawRes = objeto.string("ocrRaw", null);
			if (empty(ocrRaw) && !empty(ocrRawRes)) {
				ocrRaw = ocrRawRes;
			}

			Objeto barcodeRes = objeto.objeto("barcode", null);
			if (barcode.isEmpty() && !empty(barcodeRes)) {
				barcode = barcodeRes;
			}

			Objeto mrzRes = objeto.objeto("mrz", null);
			if (mrz.isEmpty() && !empty(mrzRes)) {
				mrz = mrzRes;
			}
		}

		String documentFrontLegajo = null;
		String documentBackLegajo = null;
		String selfieLegajo = null;

		if (documentFronts != null && documentFronts.size() > 0) {
			documentFrontLegajo = documentFronts.get(0);
		}

		if (documentBacks != null && documentBacks.size() > 0) {
			documentBackLegajo = documentBacks.get(0);
		}

		if (selfiesAux != null && selfiesAux.size() > 0) {
			selfieLegajo = selfiesAux.get(0).string("image");
		}

		Objeto respuesta = respuesta();

		respuesta.set("ocr", ocr);
		respuesta.set("ocrRaw", ocrRaw);
		respuesta.set("barcode", barcode);
		respuesta.set("mrz", mrz);
		respuesta.set("document.front", documentFronts);
		respuesta.set("document.back", documentBacks);
		respuesta.set("selfies", selfiesAux);
		respuesta.set("legajo.document", Util.mergeImagesBase64(documentFrontLegajo, documentBackLegajo));
		respuesta.set("legajo.documentFront", documentFrontLegajo);
		respuesta.set("legajo.documentBack", documentBackLegajo);
		respuesta.set("legajo.selfie", selfieLegajo);
		respuesta.set("doc", documentFrontLegajo != null && documentBackLegajo != null && selfieLegajo != null);

		return respuesta;
	}

	private static int getInt(String str) {

		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return 0;
		}
	}

	private static boolean validarOperationVU(String operationVU) {

		if (empty(operationVU) || !operationVU.contains("/")) {
			return false;
		}

		String[] operationVUList = operationVU.split("/");
		if (operationVUList.length != 2) {
			return false;
		}

		String operationId = operationVUList[0];
		String operationGuid = operationVUList[1];
		if (empty(operationId) || empty(operationGuid)) {
			return false;
		}

		return true;
	}

	public static Objeto obtenerDatosAfip(ContextoBB ctx) {
		return respuesta().set("datos", SqlTeradata.get(ctx, ctx.parametros.string("cuil")).tryGet());
	}

	public static Objeto personaRenaper(ContextoBB contexto) {
		UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));

		String scanner = contexto.parametros.string("scanner");

		if (dniVencido(scanner)){
			return respuesta("DNI_VENCIDO");
		}

		String numeroTramite = buscarNroTramitebyScanner(scanner);
		String sexo = buscarSexoByScanner(scanner);

		String numeroDocumento = buscarDniByScanner(scanner, sexo);
		if(!empty(numeroDocumento) && numeroDocumento.length() == 7){
			numeroDocumento = "0" + numeroDocumento;
		}

		if(!esDni(numeroDocumento) || !esNroTramite(numeroTramite) || !esSexo(sexo)){
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		ConsultaPersona persona = ApiViviendas.validaciones(contexto, numeroTramite, numeroDocumento, sexo).tryGet();
		if (empty(persona) || (!persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_OK)
				&& !persona.codigo.equals(ErroresBB.COD_VALIDAR_PERSONA_FIRMADA))) {
			return respuesta("PERSONA_NO_ENCONTRADA");
		}

		String ejemplar = buscarEjemplarByScanner(scanner, "@");
		if (!persona.esUltimoEjemplar(ejemplar)){
			return respuesta("ERROR_EJEMPLAR");
		}

		if (!persona.edadValido()){
			return respuesta("ERROR_MENOR_EDAD");
		}

		if (persona.estaFallecido()){
			return respuesta("ERROR_FALLECIDO");
		}

		if(contexto.esHomologacion() && "27299526378".equals(persona.getCuil())){
			return respuesta("ERROR_FALLECIDO");
		}

		if(persona.esCuilValido() && esClienteBool(contexto, persona.getCuil())){
			return respuesta("ES_CLIENTE");
		}

		Objeto respuesta = respuesta();
		respuesta.set("cuil", persona.getCuil());
		respuesta.set("sexo", sexo);
		respuesta.set("nombres", persona.getNombres());
		respuesta.set("apellidos", persona.getApellido());
		respuesta.set("fechaNacimiento", persona.getFechaNacimiento());
		return respuesta;
	}

	private static boolean dniVencido(String scanner){
		try{
			Fecha fechaEmision = new Fecha(buscarEmisionScanner(scanner), "dd/MM/yyyy");
			Date hoy = new Date();
			return hoy.after(fechaEmision.sumarAños(15).FechaDate());
		}
		catch(Exception e){}
		return false;
	}

	private static String buscarEmisionScanner(String scanner) {
		String emision = null;
		String[] parts = scanner.split("@");
		for (String part : parts) {
			String dato = empty(part) ? null : part.trim();
			if (esFecha(dato)) {
				if (emision == null) {
					emision = dato;
				} else {
					Date emisionDate = new Fecha(emision, "dd/MM/yyyy").FechaDate();
					Date datoDate = new Fecha(dato, "dd/MM/yyyy").FechaDate();
					if (emisionDate.before(datoDate)) {
						emision = dato;
					}
				}
			}
		}

		return emision;
	}

	public static Objeto guardarFatca(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		sesion.esExpuestaPolitica = contexto.parametros.bool("esPep", false);
		sesion.esSujetoObligado = contexto.parametros.bool("esSujetoObligado", false);
		sesion.esFatcaOcde = contexto.parametros.bool("esFatca", false);
		sesion.saveSesionbb2();

		LogBB.evento(contexto, "GUARDAR_FATCA", contexto.parametros);
		return respuesta();
	}
}
