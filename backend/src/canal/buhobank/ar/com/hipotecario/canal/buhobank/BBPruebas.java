package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.viviendas.ConsultaPersona;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.canal.buhobank.SesionBB.DomicilioBB;

public class BBPruebas extends Modulo {

	public static Objeto simularVU(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;

		if (contexto.esProduccion()) {
			Boolean existe = controlarCuit(contexto, cuil);

			if (!existe) {
				return respuesta("CUIL_NO_PERMITIDO");
			}
		}

		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
		if (persona == null) {
			LogBB.evento(contexto, "No existe en BUP", cuil);
		}

		Boolean guardar = guardarSesionPersona(contexto, persona, sesion, cuil);
		if (!guardar) {
			LogBB.evento(contexto, "No se pudo guardar la sesion", cuil);
		}

		//Boolean anomalias = guardarAnomalias(contexto, sesion);
		//if (!anomalias) {
		//	LogBB.evento(contexto, "No se pudo guardar anomalias", cuil);
		//}

		// PersonaRenaper
		ConsultaPersona perRenaper = new ConsultaPersona();

		perRenaper.emision = "2016-03-07";
		perRenaper.vencimiento = "2032-03-07";
		perRenaper.id_tramite_principal = "000000001";
		perRenaper.barrio = "Bº SANTA ANA";

		BBInfoPersona.informarDatos(contexto, sesion, null, perRenaper);

		Objeto respuesta = respuesta();

		respuesta.set("sesion.nombre", sesion.nombre);
		respuesta.set("sesion.apellido", sesion.apellido);

		sesion.actualizarEstado(EstadosBB.VU_TOTAL_OK);
		LogBB.evento(contexto, EstadosBB.VU_TOTAL_OK, respuesta);

		BBPersona.validarDatosPersonales(contexto);

		return respuesta;

	}

	public static Boolean guardarSesionPersona(ContextoBB contexto, Persona persona, SesionBB sesion, String cuil) {
		try {
			sesion.genero = persona.idSexo;
			sesion.cuil = cuil;
			sesion.nombre = persona.nombres;
			sesion.apellido = persona.apellidos;
			sesion.fechaNacimiento = empty(persona.fechaNacimiento) ? Fecha.hoy().restarAños(40) : persona.fechaNacimiento;
			sesion.paisNacimiento = "ARGENTINA";
			sesion.nacionalidad = "ARGENTINA";
			sesion.idCobis = persona.idCliente;
			sesion.idPaisNacimiento = "80";
			sesion.idNacionalidad = "80";
			sesion.numeroDocumento = persona.numeroDocumento;
			sesion.idTipoIDTributario = persona.idTipoIDTributario;
			sesion.ejemplar = "A";

			DomicilioBB domicilioLegal = new DomicilioBB();

			domicilioLegal.calle = "SAAVEDRA";
			domicilioLegal.numeroCalle = "975";
			domicilioLegal.piso = "2";
			domicilioLegal.dpto = "4";
			if (contexto.esHomologacion()) {
				domicilioLegal.cp = "8000";
				domicilioLegal.ciudad = "BAHIA BLANCA";
				domicilioLegal.localidad = "BAHIA BLANCA";
				domicilioLegal.provincia = "BUENOS AIRES";
				domicilioLegal.idCiudad = "7001";
				domicilioLegal.idProvincia = "2";
			} else if (contexto.esDesarrollo()) {
				domicilioLegal.cp = "6300";
				domicilioLegal.ciudad = "SANTA ROSA";
				domicilioLegal.localidad = "SANTA ROSA";
				domicilioLegal.provincia = "LA PAMPA";
				domicilioLegal.idCiudad = "19636";
				domicilioLegal.idProvincia = "11";
			}
			domicilioLegal.pais = "ARGENTINA";
			domicilioLegal.idPais = "80";
			sesion.domicilioLegal = domicilioLegal;
			sesion.domicilioPostal = sesion.domicilioLegal;

			sesion.saveSesion();

			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public static Boolean guardarAnomalias(ContextoBB contexto, SesionBB sesion) {
		try {
			BBVU infoVU = new BBVU();

			infoVU.idOperacionVU = "0089";
			infoVU.estadoVU = "900 END_OK";
			infoVU.fiabilidadDocumento = new BigDecimal("0.46633929");
			infoVU.mrz = new BigDecimal("0.234375");
			infoVU.ocr = new BigDecimal("0.234375");
			infoVU.expiryDate = Fecha.ahora().sumarAños(10);
			infoVU.percentageNames = "0.3";
			infoVU.percentageBirthdate = null;
			infoVU.percentageDocumentNumber = "0.4663392984867096";
			infoVU.percentageLastNames = "0.8411344289779663";
			infoVU.percentageGender = "0.8778074979782104";
			infoVU.identico = true;
			infoVU.barcode = null;

			SqlEsales.crearSesionOB(contexto, sesion, infoVU).get();

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Boolean controlarCuit(ContextoBB contexto, String cuil) {
		ArrayList<String> listaCuil = new ArrayList<>();

		listaCuil.add("27116871349");
		listaCuil.add("27185165065");
		listaCuil.add("20301852003"); // Android
		listaCuil.add("20415602449"); // IOs
		listaCuil.add("27278110821"); // Qa
		listaCuil.add("20277007771");
		listaCuil.add("20288008001");
		listaCuil.add("20299000096");
		listaCuil.add("20303033301");
		listaCuil.add("20310103137");
		listaCuil.add("20320032009");
		listaCuil.add("20330003007");
		listaCuil.add("20350035053");
		listaCuil.add("20100500737");
		listaCuil.add("23100500744");
		listaCuil.add("20100500753");
		listaCuil.add("20100500761");
		listaCuil.add("27100500774");
		listaCuil.add("20100500796");
		listaCuil.add("23100500809");
		listaCuil.add("20100500818");
		listaCuil.add("27100500820");
		listaCuil.add("20100500834");

		int indice = listaCuil.indexOf(cuil);
		if (indice != -1) {
			return true;
		} else {
			return false;
		}
	}

	static boolean tieneSesionByEstado(ContextoBB contexto, String cuil, String estado, Fecha fechaDesde) {

		SesionesEsales sesionesFinalizadas = SqlEsales.obtenerSesionByEstado(contexto, cuil, estado, fechaDesde).tryGet();
		if (sesionesFinalizadas != null && sesionesFinalizadas.size() > 0) {
			return true;
		}

		return false;
	}

	public static Object obtenerEstadosSesiones(ContextoBB contexto) {

		Objeto cuils = contexto.parametros.objeto("cuils");
		Boolean finalizar = contexto.parametros.bool("finalizar", false);

		List<Object> cuilsAux = cuils.toList();

		List<String> cuilsSesionFinalizarOK = new ArrayList<String>();
		List<String> cuilsLogFinalizarOK = new ArrayList<String>();
		List<String> cuilsUnionFinalizarOK = new ArrayList<String>();
		List<String> cuilsIntercepcionFinalizarOK = new ArrayList<String>();

		List<String> cuilsBatchCorriendo = new ArrayList<String>();
		List<String> cuilsOtroEstado = new ArrayList<String>();

		for (Object cuilAux : cuilsAux) {

			String cuil = (String) cuilAux;
			Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_LIMPIAR_USUARIO_QA);

			LogsBuhoBank logFinalizado = SqlBuhoBank.captarAbandono(contexto, cuil, EstadosBB.BB_FINALIZAR_OK, fechaDesde).tryGet();

			Boolean finalizadoSesion = tieneSesionByEstado(contexto, cuil, EstadosBB.FINALIZAR_OK, fechaDesde);
			Boolean finalizadoLog = logFinalizado != null && logFinalizado.size() > 0;

			if (finalizadoSesion || finalizadoLog) {

				cuilsUnionFinalizarOK.add(cuil);

				if (finalizadoSesion && finalizadoLog) {
					cuilsIntercepcionFinalizarOK.add(cuil);
				} else if (finalizadoSesion && !finalizadoLog) {
					cuilsSesionFinalizarOK.add(cuil);
				} else if (!finalizadoSesion && finalizadoLog) {
					cuilsLogFinalizarOK.add(cuil);
				}

			} else if (tieneSesionByEstado(contexto, cuil, EstadosBB.BATCH_CORRIENDO, fechaDesde)) {
				cuilsBatchCorriendo.add(cuil);
			} else {
				cuilsOtroEstado.add(cuil);
			}
		}

		if (finalizar) {
			Objeto respuesta = new Objeto();
			respuesta.set("total_union_finalizar_ok", cuilsUnionFinalizarOK.size());
			respuesta.set("total_intercepcion_finalizar_ok", cuilsIntercepcionFinalizarOK.size());
			respuesta.set("total_sesion_finalizar_ok", cuilsSesionFinalizarOK.size());
			respuesta.set("total_log_finalizar_ok", cuilsLogFinalizarOK.size());

			respuesta.set("union_finalizar_ok", cuilsUnionFinalizarOK);
			respuesta.set("intercepcion_finalizar_ok", cuilsIntercepcionFinalizarOK);
			respuesta.set("sesion_finalizar_ok", cuilsSesionFinalizarOK);
			respuesta.set("log_finalizar_ok", cuilsLogFinalizarOK);

			return respuesta;
		}

		Objeto respuesta = new Objeto();
		respuesta.set("total_batch_corriendo", cuilsBatchCorriendo.size());
		respuesta.set("total_finalizar_ok", cuilsUnionFinalizarOK.size());
		respuesta.set("total_otro_estado", cuilsOtroEstado.size());

		respuesta.set("otro_estado", cuilsOtroEstado);
		respuesta.set("batch_corriendo", cuilsBatchCorriendo);
		respuesta.set("finalizar_ok", cuilsUnionFinalizarOK);

		return respuesta;
	}

    public static Objeto testFinalizarTcv(ContextoBB contexto) {
		if(contexto.esProduccion()){
			return respuesta("ERROR");
		}

		Objeto crearSesionV2 = BBSeguridad.crearSesionV2(contexto);
		if (!BBValidacion.estadoOk(crearSesionV2)) {
			return crearSesionV2.set("ERROR_CREAR_SESION");
		}

		Objeto guardarContacto = BBValidacion.guardarContacto(contexto);
		if (!BBValidacion.estadoOk(guardarContacto)) {
			return guardarContacto.set("ERROR_GUARDAR_CONTACTO");
		}

		contexto.sesion().emailOtpValidado = true;
		contexto.sesion().telefonoOtpValidado = true;

		Objeto ofertasV2 = BBPaquetes.ofertasV2(contexto);
		if (!BBValidacion.estadoOk(ofertasV2)) {
			return ofertasV2.set("ERROR_OBTENER_OFERTA");
		}

		String nroProducto = contexto.parametros.string("nroProducto", ofertasV2.string("nroProducto"));
		contexto.parametros.set("nroProducto", nroProducto);

		Objeto aceptarOfertaMotor = BBPaquetes.aceptarOfertaMotor(contexto);
		if (!BBValidacion.estadoOk(aceptarOfertaMotor)) {
			return aceptarOfertaMotor.set("ERROR_ACEPTAR_OFERTA");
		}

		Objeto guardarRespuestaCompletaVU = BBPersona.guardarRespuestaCompletaVU(contexto);
		if (!BBValidacion.estadoOk(guardarRespuestaCompletaVU)) {
			return guardarRespuestaCompletaVU.set("ERROR_GUARDAR_VU");
		}

		Objeto ofertaSesion = BBPaquetes.ofertaSesion(contexto);
		if (!BBValidacion.estadoOk(ofertaSesion)) {
			return ofertaSesion.set("ERROR_OBTENER_OFERTA_SESION");
		}

		Objeto elegirFisica = BBPaquetes.elegirFisica(contexto);
		if (!BBValidacion.estadoOk(elegirFisica)) {
			return elegirFisica.set("ERROR_ELEGIR_FISICA");
		}

		Object finalizarRes = BBAlta.finalizar(contexto, false, false, false, false);
		if (empty(finalizarRes) || !finalizarRes.toString().equals(respuesta().toString())) {
			return respuesta("ERROR_FINALIZAR")
					.set("idSolicitud", contexto.sesion().idSolicitud);
		}

		return respuesta("FINALIZO_OK");
    }
}
