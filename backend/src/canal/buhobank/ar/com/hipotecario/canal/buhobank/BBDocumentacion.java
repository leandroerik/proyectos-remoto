package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.digitalizacion.ApiDigitalizacion;
import ar.com.hipotecario.backend.servicio.api.digitalizacion.EnvioDocumentos;
import ar.com.hipotecario.backend.servicio.api.digitalizacion.EnvioDocumentos.NuevoEnvioDocumento;
import ar.com.hipotecario.backend.servicio.api.digitalizacion.EnvioDocumentos.Propiedades;
import ar.com.hipotecario.backend.servicio.api.formulario.ApiFormulario;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;

public class BBDocumentacion extends Modulo {

	public static Objeto guardarArchivos(ContextoBB contexto) {

		String tipoArchivo = contexto.parametros.string("typeFile");
		String archivo = contexto.parametros.string("file");
		String cuil = contexto.parametros.string("cuil");

		if (!empty(tipoArchivo)) {
			if (!tipoArchivo.equals("jpg") && !tipoArchivo.equals("pdf") && !tipoArchivo.equals("png")) {
				LogBB.error(contexto, ErroresBB.ARCHIVO_INVALIDO);
				return respuesta(ErroresBB.ARCHIVO_INVALIDO);
			}
		} else {
			return respuesta(ErroresBB.TIPO_ARCHIVO_VACIO);
		}

		if (empty(archivo)) {
			LogBB.error(contexto, ErroresBB.TIPO_ARCHIVO_VACIO);
			return respuesta(ErroresBB.TIPO_ARCHIVO_VACIO);
		}

		if (empty(cuil)) {
			LogBB.error(contexto, ErroresBB.CUIL_VACIO);
			return respuesta(ErroresBB.CUIL_VACIO);
		}

		NuevoEnvioDocumento nuevoDocumento = BBDocumentacion.nuevo(contexto, tipoArchivo, archivo, cuil, GeneralBB.CLASE_DOCUMENTAL_INGRESOS_H);
		EnvioDocumentos documento = ApiDigitalizacion.guardarDocumentacion(contexto, nuevoDocumento).tryGet();

		if (empty(documento)) {
			LogBB.error(contexto, ErroresBB.ARCHIVO_NO_GUARDADO);
			return respuesta(ErroresBB.ARCHIVO_NO_GUARDADO);
		}

		LogBB.evento(contexto, "ETAPA_ENVIAR_ARCHIVO", documento.idDocumento);

		Objeto respuesta = respuesta();
		return respuesta;
	}

	public static NuevoEnvioDocumento nuevo(ContextoBB contexto, String tipoArchivo, String Archivo, String cuil, String claseDocumental) {
		SesionBB sesion = contexto.sesion();
		NuevoEnvioDocumento nuevoDocumento = new NuevoEnvioDocumento();

		if (empty(sesion.apellido)) {
			Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
			if (!empty(persona)) {
				sesion.apellido = persona.apellidos;
				sesion.nombre = persona.nombres;
			}

		}

		nuevoDocumento.bytesDocumento = Archivo;
		nuevoDocumento.claseDocumental = claseDocumental;

		Propiedades propiedades = new Propiedades();
		propiedades.DocumentTitle = claseDocumental + "-" + cuil;
		propiedades.CUIL = cuil;
		propiedades.ApellidoyNombre = sesion.apellido + " " + sesion.nombre;
		propiedades.DNI = sesion.numeroDocumento;
		propiedades.OrigenDelAlta = GeneralBB.ORIGEN_ALTA;
		propiedades.TipoPersona = GeneralBB.TIPO_PERSONA;
		propiedades.ExtArchivo = tipoArchivo;
		propiedades.NroSolicitud = sesion.idSolicitud;
		propiedades.mimetype = GeneralBB.MIMETYPE + tipoArchivo;

		nuevoDocumento.propiedades = propiedades;

		return nuevoDocumento;

	}

	public static void guardarFormulario(ContextoBB contexto) {
		try {
			String cuil = contexto.sesion().cuil;
			Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);

			LogsBuhoBank logsFormulario = SqlBuhoBank.captarAbandono(contexto, cuil, "BB_GUARDAR_FORMULARIO_OK", fechaDesde).tryGet();
			boolean tieneFormularioGuardado = logsFormulario != null && !logsFormulario.isEmpty();
			if(tieneFormularioGuardado){
				LogBB.evento(contexto, "FORMULARIO_YA_GUARDADO");
				return;
			}

			String f2900 = ApiFormulario.get(contexto, contexto.sesion().idSolicitud, GeneralBB.GRUPO_CODIGO).tryGet();
			if (empty(f2900)) {
				LogBB.evento(contexto, "GUARDAR_FORMULARIO_ERROR", "sin recuperar formulario 2900");
				return;
			}

			NuevoEnvioDocumento nuevoDocumento = BBDocumentacion.nuevo(contexto, "pdf", f2900, cuil, GeneralBB.CLASE_DOCUMENTAL_LAVADO_DINERO);
			EnvioDocumentos envioformulario = ApiDigitalizacion.guardarDocumentacion(contexto, nuevoDocumento).tryGet();
			if (empty(envioformulario)) {
				LogBB.evento(contexto, "GUARDAR_FORMULARIO_ERROR", "error envio formulario 2900");
				return;
			}

			LogBB.evento(contexto, "GUARDAR_FORMULARIO_OK");
		}
		catch (Exception e) {
			LogBB.error(contexto, "GUARDAR_FORMULARIO_ERROR", "Error al guardar formulario");
		}
	}

	public static void guardarLegajoImagenesVU(ContextoBB contexto) {
		try {
			String cuil = contexto.sesion().cuil;
			Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);

			LogsBuhoBank logsDocument = SqlBuhoBank.captarAbandono(contexto, cuil, "BB_GUARDAR_LEGAJO_DOCUMENT_OK", fechaDesde).tryGet();
			boolean tieneDocumentGuardado = logsDocument != null && !logsDocument.isEmpty();

			LogsBuhoBank logsSelfie = SqlBuhoBank.captarAbandono(contexto, cuil, "BB_GUARDAR_LEGAJO_SELFIE_OK", fechaDesde).tryGet();
			boolean tieneSelfieGuardado = logsSelfie != null && !logsSelfie.isEmpty();

			if (tieneDocumentGuardado && tieneSelfieGuardado) {
				LogBB.evento(contexto, "LEGAJO_YA_GUARDADO");
				return;
			}

			Objeto datosVU = BBPersona.obtenerDatosVUByCuil(contexto, cuil, 1);
			if (datosVU == null) {
				LogBB.evento(contexto, "GUARDAR_LEGAJO_ERROR", "datos vu nulo");
				return;
			}

			if(!tieneDocumentGuardado){
				String legajoDocument = unirImagenesDucumentos(datosVU.string("legajo.documentFront"), datosVU.string("legajo.documentBack"));
				if (empty(legajoDocument)) {
					LogBB.evento(contexto, "GUARDAR_LEGAJO_DOCUMENT_ERROR", "generar legajo document nulo");
					return;
				}

				NuevoEnvioDocumento nuevoDocumento = BBDocumentacion.nuevo(contexto, "png", legajoDocument, cuil, GeneralBB.CLASE_DOCUMENTAL_DNI);
				EnvioDocumentos envioLegajoDocument = ApiDigitalizacion.guardarDocumentacion(contexto, nuevoDocumento).tryGet();
				if(envioLegajoDocument != null){
					LogBB.evento(contexto, "GUARDAR_LEGAJO_DOCUMENT_OK", envioLegajoDocument.idDocumento);
				} else {
					LogBB.evento(contexto, "GUARDAR_LEGAJO_DOCUMENT_ERROR");
				}
			}

			if(tieneSelfieGuardado){
				String legajoSelfie = datosVU.string("legajo.selfie");
				if (empty(legajoSelfie)) {
					LogBB.evento(contexto, "GUARDAR_LEGAJO_SELFIE_ERROR", "generar legajo selfie nulo");
					return;
				}

				NuevoEnvioDocumento nuevoDocumento = BBDocumentacion.nuevo(contexto, "png", legajoSelfie, cuil, GeneralBB.CLASE_DOCUMENTAL_FE_VIDA);
				EnvioDocumentos envioLegajoSelfie = ApiDigitalizacion.guardarDocumentacion(contexto, nuevoDocumento).tryGet();
				if(envioLegajoSelfie != null){
					LogBB.evento(contexto, "GUARDAR_LEGAJO_SELFIE_OK", envioLegajoSelfie.idDocumento);
				} else {
					LogBB.evento(contexto, "GUARDAR_LEGAJO_SELFIE_ERROR");
				}
			}

		} catch (Exception e) {
			LogBB.error(contexto, "GUARDAR_LEGAJO_ERROR", "Error al guardar legajo de imagenes VU");
		}
	}

	static String unirImagenesDucumentos(String documentFront, String documentBack) {

		if (!empty(documentFront) && !empty(documentBack)) {

			String document = Util.mergeImagesBase64(documentFront, documentBack);
			if (empty(document)) {
				return documentFront;
			}

			return document;
		} else if (!empty(documentFront) && empty(documentBack)) {
			return documentFront;
		} else if (empty(documentFront) && !empty(documentBack)) {
			return documentBack;
		}

		return null;
	}

}
