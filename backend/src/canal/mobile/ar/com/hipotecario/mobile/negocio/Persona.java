package ar.com.hipotecario.mobile.negocio;

import java.util.Date;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.servicio.RestNotificaciones;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public class Persona {

	/* ========== ATRIBUTOS ========== */
	private ContextoMB contexto;
	private Objeto persona;

	/* ========== CONSTRUCTOR ========== */
	public Persona(ContextoMB contexto, Objeto persona) {
		this.contexto = contexto;
		this.persona = persona;
	}

	/* ========== ATRIBUTOS ========== */
	public String id() {
		return persona.string("id");
	}

	public String nombre() {
		return nombres().split(" ")[0];
	}

	public String string(String clave, String valorPorDefecto) {
		return persona.string(clave, valorPorDefecto);
	}

	public String nombres() {
		return Texto.primerasMayuscula(persona.string("nombres"));
	}

	public String apellido() {
		return apellidos().split(" ")[0];
	}

	public String apellidos() {
		return Texto.primerasMayuscula(persona.string("apellidos"));
	}

	public String nombreCompleto() {
		return apellidos() + " " + nombres();
	}

	public String cuit() {
		return persona.string("cuit");
	}

	public String cuitFormateado() {
		String cuit = cuit();
		String cuitformateado = "";
		cuitformateado += cuit.substring(0, 2) + "-";
		cuitformateado += cuit.substring(2, cuit.length() - 1) + "-";
		cuitformateado += cuit.substring(cuit.length() - 1);
		return cuitformateado;
	}

	public String sucursal() {
		return persona.string("idSucursalAsignada");
	}

	public Boolean esMenor() {
		return persona.bool("cajaAhorroMenores");
	}

	public String actividadAFIP() {
		return persona.string("actividadAFIP");
	}

	public Boolean esEmpleado() {
		return Objeto.setOf("EM", "FU").contains(persona.string("idTipoCliente"));
	}

	public Boolean esPackSueldo() {
		return Objeto.setOf("PS").contains(persona.string("idTipoCliente"));
	}

	public Boolean esPersonaFisica() {
		return persona.bool("esPersonaFisica");
	}

	public Boolean esPersonaJuridica() {
		return persona.bool("esPersonaJuridica");
	}

	public Boolean tieneCuit() {
		return "11".equals(persona.string("idTipoIDTributario").trim());
	}

	public String email() {
		return RestPersona.direccionEmail(contexto, cuit());
	}

	public String celular() {
		return RestPersona.numeroCelular(contexto, cuit());
	}

	public boolean alertaIngresos() {
		return RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_ACC");
	}

	public boolean alertaCambioClave() {
		return RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_CCL");
	}

	public boolean alertaDesbloqueoClave() {
		return RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_DBC");
	}

	// TODO: refactor

	public Integer idTipoDocumento() {
		return persona.integer("idTipoDocumento");
	}

	public String idTipoDocumentoString() {
		return persona.string("idTipoDocumento");
	}

	public String tipoDocumento() {
		Integer idTipoDocumento = persona.integer("idTipoDocumento");
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

	public String numeroDocumento() {
		return persona.string("numeroDocumento");
	}

	public String idSexo() {
		return persona.string("idSexo");
	}

	public Date fechaNacimiento() {
		return persona.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String fechaNacimiento(String formato) {
		return persona.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss", formato);
	}

	public Date fechaResidencia() {
		// TODO: tener en cuenta extranjeros
		return persona.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String fechaResidencia(String formato) {
		// TODO: tener en cuenta extranjeros
		return persona.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss", formato);
	}

	public Integer idPaisNacimiento() {
		return persona.integer("idPaisNacimiento");
	}

	public Integer idNacionalidad() {
		return persona.integer("idNacionalidad");
	}
	public String nombreConyuge() {
		return persona.string("nombreConyuge");
	}

	public String ciudadNacimiento() {
		return persona.string("ciudadNacimiento");
	}

	public String tipoTributario() {
		return persona.string("idTipoIDTributario");
	}

	public String idEstadoCivil() {
		return persona.string("idEstadoCivil");
	}

	public String idSituacionLaboral() {
		return persona.string("idSituacionLaboral");
	}

	public Boolean esPersonaExpuestaPoliticamente() {
		return persona.bool("esPEP");
	}

	public Boolean esSujetoObligado() {
		return persona.bool("esSO");
	}

	public Integer idPaisResidencia() {
		return 80;
	}

	public Integer edad() {
		return persona.integer("edad");
	}

	public boolean tieneMora() {
		ApiRequestMB requestMora = ApiMB.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
		requestMora.query("idClienteCobis", contexto.idCobis());
		ApiResponseMB responseMora = ApiMB.response(requestMora, contexto.idCobis());
		if (!responseMora.hayError()) {
			for (Objeto item : responseMora.objetos()) {
				if (item.string("pro_cod").equals("203")) {
					return true;
				}
			}
		} else {
			throw new RuntimeException();
		}
		return false;

	}

//	[{
//		"id": 406742,
//		"idTipoDocumento": "01",
//		"numeroDocumento": "8099264",
//		"idVersionDocumento": "A",
//		"idSexo": "M",
//		"idTipoIDTributario": "08", | cuil: "08" | cuit: "11"
//		"cuit": 20080992646,
//		"idCliente": 21428,
//		"apellidos": "TATO",
//		"nombres": "JUAN FELIPE",
//		"esPersonaFisica": true,
//		"esPersonaJuridica": false,
//		"esReferido": null,
//		"fechaActualizacionEstadoCivil": "2018-05-30T15:45:39",
//		"idSubtipoEstadoCivil": "",
//		"idObraSocial": "",
//		"idSituacionVivienda": "01",
//		"fechaNacimiento": "1987-05-03T02:00:00",
//		"idEstadoCivil": "S",
//		"esUnidoDeHecho": false,
//		"cantidadNupcias": "",
//		"cantidadHijos": 1,
//		"idNivelEstudios": "4",
//		"fechaInicioResidenciaVivienda": 2000,
//		"montoAlquiler": null,
//		"idPaisNacimiento": 80,
//		"idPaisResidencia": "",
//		"fechaInicioResidenciaPais": null,
//		"ciudadNacimiento": "SANTIAGO DEL ESTERO",
//		"apellidosPadre": "sd",
//		"nombresPadre": "fghfhg",
//		"apellidosMadre": "dfsd",
//		"nombresMadre": "sd",
//		"idGanancias": "NORE",
//		"idImpuestoDebitosCreditos": "",
//		"idImpuestoSellos": "",
//		"idAlcanceDebitosCreditos": "A",
//		"esPEP": false,
//		"cargoPEP": null,
//		"otroCargoPEP": null,
//		"fechaDeclaracionPEP": "2018-08-09T00:00:00",
//		"fechaVencimientoPEP": null,
//		"informadoPadronPEP": false,
//		"esSO": false,
//		"idTipoSO": "",
//		"adoptoDisposicionesSO": null,
//		"presentoConstanciaSO": null,
//		"fechaPresentacionDDJJSO": "2018-08-17T00:00:00",
//		"fechaIncripcionSO": null,
//		"perfilPatrimonial": 76000.0000,
//		"fechaPerfilCliente": "2014-09-19T00:00:00",
//		"idNacionalidad": 52,
//		"idSegundaNacionalidad": null,
//		"idTerceraNacionalidad": null,
//		"idPaisResidencia1": null,
//		"fechaInicialResidencia1": null,
//		"fechaFinalResidencia1": null,
//		"numeroContribuyente1": null,
//		"idPaisResidencia2": null,
//		"fechaInicialResidencia2": null,
//		"fechaFinalResidencia2": null,
//		"numeroContribuyente2": null,
//		"idCategoriaFatca": 2,
//		"presentoFormularioFatca": null,
//		"fechaVencimientoFormularioFatca": null,
//		"razonSocial": "ASD",
//		"actividadAFIP": "0",
//		"fechaConstitucionPersonaJuridica": null,
//		"cantidadEmpleados": 0,
//		"calificacionCrediticia": null,
//		"representanteLegal": null,
//		"cantidadApoderados": null,
//		"fechaVencimientoMandatos": null,
//		"grupoEconomico": null,
//		"cantidadChequeras": 0,
//		"idSituacionImpositiva": "CONF",
//		"categoriaCliente": "C",
//		"tipoSocietario": null,
//		"canalModificacion": "CRM",
//		"fechaCreacion": null,
//		"usuarioModificacion": "B05306",
//		"fechaModificacion": "2018-08-17T11:49:59",
//		"cuentaUsuarioFacebook": null,
//		"cuentaUsuarioTwitter": null,
//		"fechaNacimientoHijoMayor": "2016-07-04T00:00:00",
//		"idMarcaRodado": null,
//		"idModeloRodado": null,
//		"anioRodado": null,
//		"valorLealtadCliente": "A1",
//		"esPerflInversor": true,
//		"idSucursalAsignada": "37",
//		"idConvenio": null,
//		"tipoConvenio": null,
//		"idTipoCliente": "PA",
//		"idTipoBanca": "01",
//		"valorRentabilidadCa": null,
//		"valorRentabilidadCc": null,
//		"valorRentabilidadEmerix": null,
//		"valorRentabilidadGenesys": null,
//		"valorRentabilidadIvr": null,
//		"valorRentabilidadMesaCambio": null,
//		"valorRentabilidadPf": null,
//		"valorRentabilidadPh": null,
//		"valorRentabilidadPp": null,
//		"valorRentabilidadTc": null,
//		"numeroIdentificacionFatca": "",
//		"fechaFirmaFormularioFatca": null,
//		"tipoFormularioFatca": null,
//		"idSituacionLaboral": "1",
//		"indicioFatca": false,
//		"eTagPreguntasCSC": -1,
//		"categoriaMonotributo": "02",
//		"fechaRecategorizacionMonotributo": null,
//		"fechaPerfilPatrimonial": "2020-01-16T00:00:00",
//		"importePerfilPatrimonial": 550000.00,
//		"etag": 1757147314,
//		"etagActividades": null,
//		"etagCliente": -1,
//		"etagDomicilios": 879190794,
//		"etagGastosPatrimoniales": -1,
//		"etagMails": 949314499,
//		"etagPerfilesPatrimoniales": 33703982,
//		"etagPrestamosPatrimoniales": null,
//		"etagReferencias": null,
//		"etagRelaciones": 858236127,
//		"etagTarjetasPatrimoniales": null,
//		"etagTelefonos": null,
//		"idIva": "CONF",
//		"cajaAhorroMenores": false,
//		"edad": 31
//	}]

}
