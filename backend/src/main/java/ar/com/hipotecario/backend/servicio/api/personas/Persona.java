package ar.com.hipotecario.backend.servicio.api.personas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Persona extends ApiObjeto {

	public static String GET_PERSONA = "Personas";
	public static String POST_PERSONA = "CrearPersona";
	public static String PATCH_PERSONA = "ActualizarPersona";
	public static String GET_DATOS_BASICOS = "DatosBasicosPersona";

	/* ========== ATRIBUTOS ========== */
	public String id;
	public String idTipoDocumento;
	public String numeroDocumento;
	public String idVersionDocumento;
	public String idSexo;
	public String idTipoIDTributario;
	public String cuit;
	public String idCliente;
	public String apellidos;
	public String nombres;
	public Boolean esPersonaFisica;
	public Boolean esPersonaJuridica;
	public Boolean esReferido;
	public Fecha fechaActualizacionEstadoCivil;
	public String idSubtipoEstadoCivil;
	public String idObraSocial;
	public String idSituacionVivienda;
	public Fecha fechaNacimiento;
	public String idEstadoCivil;
	public Boolean esUnidoDeHecho;
	public String cantidadNupcias;
	public Integer cantidadHijos;
	public String idNivelEstudios;
	public Integer fechaInicioResidenciaVivienda;
	public BigDecimal montoAlquiler;
	public String idPaisNacimiento;
	public String idPaisResidencia;
	public Fecha fechaInicioResidenciaPais;
	public String ciudadNacimiento;
	public String apellidosPadre;
	public String nombresPadre;
	public String apellidosMadre;
	public String nombresMadre;
	public String idGanancias;
	public String idImpuestoDebitosCreditos;
	public String idImpuestoSellos;
	public String idAlcanceDebitosCreditos;
	public Boolean esPEP;
	public String cargoPEP;
	public String otroCargoPEP;
	public Fecha fechaDeclaracionPEP;
	public Fecha fechaVencimientoPEP;
	public Boolean informadoPadronPEP;
	public Boolean esSO;
	public String idTipoSO;
	public Boolean adoptoDisposicionesSO;
	public Boolean presentoConstanciaSO;
	public Fecha fechaPresentacionDDJJSO;
	public Fecha fechaIncripcionSO;
	public BigDecimal perfilPatrimonial;
	public Fecha fechaPerfilCliente;
	public String idNacionalidad;
	public String idSegundaNacionalidad;
	public String idTerceraNacionalidad;
	public String idPaisResidencia1;
	public Fecha fechaInicialResidencia1;
	public Fecha fechaFinalResidencia1;
	public String numeroContribuyente1;
	public String idPaisResidencia2;
	public Fecha fechaInicialResidencia2;
	public Fecha fechaFinalResidencia2;
	public String numeroContribuyente2;
	public String idCategoriaFatca;
	public Boolean presentoFormularioFatca;
	public Fecha fechaVencimientoFormularioFatca;
	public String razonSocial;
	public String actividadAFIP;
	public Fecha fechaConstitucionPersonaJuridica;
	public Integer cantidadEmpleados;
	public String calificacionCrediticia;
	public String representanteLegal;
	public Integer cantidadApoderados;
	public Fecha fechaVencimientoMandatos;
	public String grupoEconomico;
	public Integer cantidadChequeras;
	public String idSituacionImpositiva;
	public String categoriaCliente;
	public String tipoSocietario;
	public String canalModificacion;
	public Fecha fechaCreacion;
	public String usuarioModificacion;
	public Fecha fechaModificacion;
	public String cuentaUsuarioFacebook;
	public String cuentaUsuarioTwitter;
	public Fecha fechaNacimientoHijoMayor;
	public String idMarcaRodado;
	public String idModeloRodado;
	public Integer anioRodado;
	public String valorLealtadCliente;
	public Boolean esPerflInversor;
	public String idSucursalAsignada;
	public String idConvenio;
	public String tipoConvenio;
	public String idTipoCliente;
	public String idTipoBanca;
	public BigDecimal valorRentabilidadCa;
	public BigDecimal valorRentabilidadCc;
	public BigDecimal valorRentabilidadEmerix;
	public BigDecimal valorRentabilidadGenesys;
	public BigDecimal valorRentabilidadIvr;
	public BigDecimal valorRentabilidadMesaCambio;
	public BigDecimal valorRentabilidadPf;
	public BigDecimal valorRentabilidadPh;
	public BigDecimal valorRentabilidadPp;
	public BigDecimal valorRentabilidadTc;
	public String numeroIdentificacionFatca;
	public Fecha fechaFirmaFormularioFatca;
	public String tipoFormularioFatca;
	public String idSituacionLaboral;
	public Boolean indicioFatca;
	public String eTagPreguntasCSC;
	public String categoriaMonotributo;
	public Fecha fechaRecategorizacionMonotributo;
	public Fecha fechaPerfilPatrimonial;
	public BigDecimal importePerfilPatrimonial;
	public String tipoCompania;
	public String apellidoConyuge;
	public String nombreConyuge;
	public String idSector;
	public String idOficial;
	public String idResidencia;
	public String etag;
	public String etagActividades;
	public String etagCliente;
	public String etagDomicilios;
	public String etagGastosPatrimoniales;
	public String etagMails;
	public String etagPerfilesPatrimoniales;
	public String etagPrestamosPatrimoniales;
	public String etagReferencias;
	public String etagRelaciones;
	public String etagTarjetasPatrimoniales;
	public String etagTelefonos;
	public String idIva;
	public Boolean cajaAhorroMenores;
	public Integer edad;

	public static class DatosBasicosPersonas extends ApiObjetos<DatosBasicosPersona> {
	}

	public static class DatosBasicosPersona extends ApiObjeto {
		public String idCliente;
		public String tipoDocumento;
		public String numeroDocumento;
		public String numeroIdentificacionTributaria;
		public String apellido;
		public String nombre;
		public String sexo;
		public Fecha fechaNacimiento;
		public String tipoPersona;
	}

	/* ========== METODOS ========== */
	public Boolean esCliente() {
		try {
			return Long.valueOf(idCliente) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean esEmpleado() {
		return "EM".equals(idTipoCliente) || "FU".equals(idTipoCliente);
	}

	public Boolean faltanDatosCriticos() {
		Boolean faltanDatosCriticos = false;
		faltanDatosCriticos |= empty(idNacionalidad);
		faltanDatosCriticos |= empty(idSexo);
		faltanDatosCriticos |= empty(idTipoDocumento);
		faltanDatosCriticos |= empty(nombres);
		faltanDatosCriticos |= empty(numeroDocumento);
		faltanDatosCriticos |= empty(idTipoIDTributario);
		faltanDatosCriticos |= empty(idIva);
		faltanDatosCriticos |= empty(apellidos);
		faltanDatosCriticos |= empty(fechaNacimiento);
		return faltanDatosCriticos;
	}
	
	public String nombreCompleto() {
		return this.apellidos + " " + this.nombres;
	}
	
	public String nombre() {
		return nombres.split(" ")[0];
	}
	
	public String apellido() {
		return apellidos.split(" ")[0];
	}
	
	public Boolean esPersonaJuridica() {
		return this.esPersonaJuridica;
	}

	public String tipoDocumento() {
		Integer idTipoDocumento =  Integer.parseInt(this.idTipoDocumento);
		String tipoDocumento = "DNI";
		switch (idTipoDocumento) {
		case 1:
			tipoDocumento = "DNI";
			break;
		case 134:
			tipoDocumento = "DNI";
			break;
		case 2:
			tipoDocumento = "LE";
			break;
		case 3:
			tipoDocumento = "LC";
			break;
		case 101:
		case 102:
		case 103:
		case 104:
		case 105:
		case 106:
		case 107:
		case 108:
		case 109:
		case 110:
		case 111:
		case 112:
		case 113:
		case 114:
		case 115:
		case 116:
		case 117:
		case 118:
		case 119:
		case 120:
		case 121:
		case 123:
		case 140:
		case 122:
			tipoDocumento = "CI";
			break;
		case 125:
			;
		case 126:
			tipoDocumento = "PAS";
			break;
		}
		return tipoDocumento;
	}

	/* ========== SERVICIOS ========== */
	public static Persona get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest(GET_PERSONA, ApiPersonas.API, "GET", "/personas/{id}", contexto);
		request.path("id", cuit);
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("PERSONA_NO_ENCONTRADA", response.contains("no fue encontrada en bup o core"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Persona.class);
	}

	public static Persona get(Contexto contexto, String idTipoDocumento, String documento, Boolean consultaCuil, String genero) {
		ApiRequest request = new ApiRequest(GET_PERSONA, ApiPersonas.API, "GET", "/personas", contexto);
		request.query("nroDocumento", documento);
		request.query("consultaCuil", consultaCuil.toString());
		if (idTipoDocumento != null) {
			request.query("idTipoDocumento", idTipoDocumento);
		}
		// request.query("userAgent", contexto.usuarioCanal()); //?????
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("COBIS_CAIDO", response.contains("Server fuera de linea"), request, response);
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("no fue encontrada en bup o core"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		ApiException.throwIf("MULTIPLES_PERSONAS_ENCONTRADAS", response.objetos().size() > 1 && genero == null, request, response);

		Persona aux = new Persona();

		if (genero != null) {
			List<Objeto> lista = new ArrayList<>();
			for (Objeto item : response.objetos()) {
				if (item.string("sexo").equals(genero)) {
					lista.add(item);
				}
			}
			ApiException.throwIf("MULTIPLES_PERSONAS_ENCONTRADAS", lista.size() > 1, request, response);
			ApiException.throwIf("USUARIO_INVALIDO", lista.isEmpty(), request, response);
			return response.crear(Persona.class, lista.get(0)); // TODO: arreglar esto
		}

		aux.id = response.objetos().get(0).string("idCliente");

		return aux;
	}

	// API-Personas_ConsultarPersona
	public static DatosBasicosPersonas getDatosBasicos(Contexto contexto, String documento) {
		ApiRequest request = new ApiRequest(GET_DATOS_BASICOS, ApiPersonas.API, "GET", "/personas", contexto);
		request.query("nroDocumento", documento);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DatosBasicosPersonas.class);
	}

	// API-Personas_AltaPersona
	public static Persona post(Contexto contexto, String cuit) {
		ApiRequest request = new ApiRequest(POST_PERSONA, ApiPersonas.API, "POST", "/personas", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.body("cuit", cuit);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_PERSONA, cuit);
		Api.eliminarCache(contexto, Cliente.GET_CLIENTE, response.string("idCliente"));
		ApiException.throwIf("PERSONA_EXISTENTE", response.contains("ya existe"), request, response);
		ApiException.throwIf(!response.http(201), request, response);
		return response.crear(Persona.class);
	}

	// API-Personas_ModificarParcialmentePersona
	// No estan todos los campos mapeados
	public static Persona patch(Contexto contexto, Persona persona) {
		ApiRequest request = new ApiRequest(PATCH_PERSONA, ApiPersonas.API, "PATCH", "/personas/{id}", contexto);
		request.path("id", persona.cuit);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.bodyIfNotNull("nombres", persona.nombres);
		request.bodyIfNotNull("apellidos", persona.apellidos);
		request.bodyIfNotNull("idSexo", persona.idSexo);
		request.bodyIfNotNull("idTipoDocumento", persona.idTipoDocumento);
		request.bodyIfNotNull("numeroDocumento", persona.numeroDocumento);
		request.bodyIfNotNull("idVersionDocumento", persona.idVersionDocumento);
		request.bodyIfNotNull("fechaNacimiento", persona.fechaNacimiento.string("yyyy-MM-dd'T'HH:mm:ss"));
		request.bodyIfNotNull("idPaisNacimiento", persona.idPaisNacimiento);
		request.bodyIfNotNull("idNacionalidad", persona.idNacionalidad);
		request.bodyIfNotNull("ciudadNacimiento", persona.ciudadNacimiento);
		request.bodyIfNotNull("idSituacionLaboral", persona.idSituacionLaboral);
		request.bodyIfNotNull("idSituacionImpositiva", persona.idSituacionImpositiva);
		request.bodyIfNotNull("idIva", persona.idIva);
		request.bodyIfNotNull("idGanancias", persona.idGanancias);
		request.bodyIfNotNull("idEstadoCivil", persona.idEstadoCivil);
		request.bodyIfNotNull("cantidadNupcias", persona.cantidadNupcias);
		request.bodyIfNotNull("idSubtipoEstadoCivil", persona.idSubtipoEstadoCivil);
		request.bodyIfNotNull("idNivelEstudios", persona.idNivelEstudios);
		request.body("esPersonaFisica", persona.esPersonaFisica);
		request.body("esPersonaJuridica", persona.esPersonaJuridica);
		request.body("esPEP", persona.esPEP);
		if(persona.fechaDeclaracionPEP != null){
			request.body("fechaDeclaracionPEP", persona.fechaDeclaracionPEP.string("yyyy-MM-dd'T'HH:mm:ss"));
		}
		request.body("esSO", persona.esSO);
		request.bodyIfNotNull("idTipoIDTributario", persona.idTipoIDTributario);
		request.bodyIfNotNull("idSucursalAsignada", persona.idSucursalAsignada);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_PERSONA, persona.cuit);
		Api.eliminarCache(contexto, Cliente.GET_CLIENTE, response.string("idCliente"));
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Persona.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");

		String test = "gets";
		if (test.equals("get")) {
			Persona datos = get(contexto, "27272283686", true);
			imprimirResultado(contexto, datos);
		}
		if (test.equals("gets")) {
			DatosBasicosPersonas datos = getDatosBasicos(contexto, "12692830");
			imprimirResultado(contexto, datos);
		}

		if (test.equals("post")) {
			Persona datos = post(contexto, "23532777232");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("patch")) {
			Persona datos = get(contexto, "27259301039", true);
			datos.idSucursalAsignada = "46";
			datos = patch(contexto, datos);
			imprimirResultado(contexto, datos);
		}
	}
}
