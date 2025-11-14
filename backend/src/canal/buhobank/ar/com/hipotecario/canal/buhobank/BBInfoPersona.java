package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.viviendas.ConsultaPersona;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper.PersonaRenaper;

public class BBInfoPersona extends Modulo {

	public static String FORMATO_FECHA_OCR = "yyyy-MM-dd";

	public String idTramite;
	public String ejemplar;
	public Fecha emision;
	public Fecha vencimiento;
	public String apellido;
	public String nombre;
	public Fecha fechaNacimiento;
	public String cuil;
	public String calle;
	public String numero;
	public String piso;
	public String departamento;
	public String codigoPostal;
	public String barrio;
	public String monoblock;
	public String ciudad;
	public String municipio;
	public String provincia;
	public String pais;
	public String idDispositivo;
	public String estado;
	public Boolean renaperVU;

	/* ========== METODOS =========== */

	public static void informarDatos(ContextoBB contexto, SesionBB sesion, Objeto extra, ConsultaPersona persona) {
		BBInfoPersona infoPersona = new BBInfoPersona();

		if (!empty(persona)) {

			Objeto fechas = new Objeto();
			fechas.set("emision", persona.emision);
			fechas.set("vencimiento", persona.vencimiento);

			if (persona.id_tramite_principal.length() == 9) {
				infoPersona.idTramite = "00" + persona.id_tramite_principal;
			} else {
				infoPersona.idTramite = persona.id_tramite_principal;
			}
			infoPersona.emision = fechas.fecha("emision", FORMATO_FECHA_OCR);
			infoPersona.vencimiento = fechas.fecha("vencimiento", FORMATO_FECHA_OCR);
			infoPersona.barrio = persona.barrio;
			infoPersona.monoblock = persona.monoblock;

		}

		infoPersona.idDispositivo = sesion.idDispositivo;
		infoPersona.ejemplar = sesion.ejemplar;
		infoPersona.apellido = sesion.apellido;
		infoPersona.nombre = sesion.nombre;
		infoPersona.fechaNacimiento = sesion.fechaNacimiento;
		infoPersona.cuil = sesion.cuil;
		infoPersona.calle = sesion.domicilioLegal.calle;
		infoPersona.numero = sesion.domicilioLegal.numeroCalle;
		infoPersona.piso = sesion.domicilioLegal.piso;
		infoPersona.departamento = sesion.domicilioLegal.dpto;
		infoPersona.codigoPostal = sesion.domicilioLegal.cp;
		infoPersona.ciudad = sesion.domicilioLegal.ciudad;
		infoPersona.municipio = sesion.domicilioLegal.localidad;
		infoPersona.provincia = sesion.domicilioLegal.provincia;
		infoPersona.pais = sesion.domicilioLegal.pais;
		infoPersona.idDispositivo = sesion.idDispositivo;

		if (!empty(extra)) {
			String additionalJson = extra.string("additional");
			Objeto additional = Objeto.fromJson(additionalJson);
			if (!empty(additional)) {
				infoPersona.idTramite = additional.string("TramitNumber");
				infoPersona.vencimiento = additional.fecha("ExpiryDate", FORMATO_FECHA_OCR);
				infoPersona.emision = additional.fecha("IssueDate", FORMATO_FECHA_OCR);

			}
			String mrzJson = extra.string("mrz");
			Objeto mrz = Objeto.fromJson(mrzJson);
			if (!empty(mrz) || empty(infoPersona.fechaNacimiento)) {
				infoPersona.fechaNacimiento = mrz.fecha("BirthDate", FORMATO_FECHA_OCR);
				infoPersona.vencimiento = mrz.fecha("ExpiryDate", FORMATO_FECHA_OCR);
			}
		} else {
			infoPersona.renaperVU = false;
		}

		PersonaRenaper personaInformacion = null;
		try {
			personaInformacion = SqlEsales.get(contexto, infoPersona.cuil).get();
		} catch (Exception e) {
			System.out.println("PRIMER INGRESO");
		}

		if (empty(personaInformacion))
			SqlEsales.crear(contexto, infoPersona).get();

	}

}
