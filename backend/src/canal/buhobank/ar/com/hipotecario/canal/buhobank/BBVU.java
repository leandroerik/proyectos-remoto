package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.util.Validadores;

public class BBVU extends Modulo {

	public String idOperacionVU;
	public String estadoVU;
	public BigDecimal fiabilidad;
	public BigDecimal fiabilidadDocumento;
	public BigDecimal fiabilidadTotal;
	public BigDecimal mrz;
	public BigDecimal barcode;
	public BigDecimal ocr;
	public String percentageNames;
	public String percentageBirthdate;
	public String percentageDocumentNumber;
	public String percentageLastNames;
	public String percentageGender;
	public Fecha expiryDate;
	public Boolean identico;

	/* ========== METODOS =========== */
	public static void guardarAnomaliasVU(Contexto contexto, SesionBB sesion, Objeto anomaliesObj, String idOperacionVU, Fecha fechaExpira, Boolean identical) {
		BBVU infoVU = new BBVU();
		// si anomalías no está vacío continuar
		if (!Validadores.anomaliesValidoVU(anomaliesObj)) {
			return;
		}

		String validacionDniJson = anomaliesObj.string("textValidationsDocumentData");
		Objeto validacionDni = Objeto.fromJson(validacionDniJson);

		BigDecimal ocr = validacionDni.bigDecimal("BarcodeOcr");
		BigDecimal mrz = validacionDni.bigDecimal("BarcodeMrz");
		String porcentajeNombres = "";
		String porcentajeCumpleaños = "";
		String porcentajeDni = "";
		String porcentajeApellido = "";
		String porcentajeGenero = "";

		if (!anomaliesObj.string("textValidationsBarcodeOcr").isEmpty()) {
			String validacionBarcodeOcrJson = anomaliesObj.string("textValidationsBarcodeOcr");
			Objeto validacionBarcodeOcr = Objeto.fromJson(validacionBarcodeOcrJson);

			porcentajeNombres = validacionBarcodeOcr.string("Percentage_Names");
			porcentajeCumpleaños = validacionBarcodeOcr.string("Percentage_Birthdate");
			porcentajeDni = validacionBarcodeOcr.string("Percentage_DocumentNumber");
			porcentajeApellido = validacionBarcodeOcr.string("Percentage_LastNames");
			porcentajeGenero = validacionBarcodeOcr.string("Percentage_Gender");

		} else if (!anomaliesObj.string("textValidationsBarcodeMrz").isEmpty()) {
			String validacionBarcodeMrzJson = anomaliesObj.string("textValidationsBarcodeMrz");
			Objeto validacionBarcodeMrz = Objeto.fromJson(validacionBarcodeMrzJson);

			porcentajeNombres = validacionBarcodeMrz.string("Percentage_Names");
			porcentajeCumpleaños = validacionBarcodeMrz.string("Percentage_Birthdate");
			porcentajeDni = validacionBarcodeMrz.string("Percentage_DocumentNumber");
			porcentajeApellido = validacionBarcodeMrz.string("Percentage_LastNames");
			porcentajeGenero = validacionBarcodeMrz.string("Percentage_Gender");
		}

		String fiabilidadDniJson = anomaliesObj.string("textValidationsDocument");
		Objeto fiabilidadDniObj = Objeto.fromJson(fiabilidadDniJson);

		infoVU.idOperacionVU = idOperacionVU;
		infoVU.fiabilidadDocumento = fiabilidadDniObj.bigDecimal("Document");
		infoVU.mrz = mrz;
		infoVU.ocr = ocr;

		if (!Util.empty(mrz) && !Util.empty(ocr)) {
			infoVU.barcode = mrz.add(ocr);
		}

		infoVU.expiryDate = fechaExpira;
		infoVU.percentageNames = porcentajeNombres;
		infoVU.percentageBirthdate = porcentajeCumpleaños;
		infoVU.percentageDocumentNumber = porcentajeDni;
		infoVU.percentageLastNames = porcentajeApellido;
		infoVU.percentageGender = porcentajeGenero;
		infoVU.identico = identical;

		SqlEsales.crearSesionOB(contexto, sesion, infoVU).get();

		return;
	}

}
