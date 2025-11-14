package ar.com.hipotecario.backend.util;

import java.util.Arrays;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;

public class CuitUtil extends Base {

	public static String MASCULINO = "M";
	public static String FEMENINO = "F";
	public static String EMPRESA = "E";

	/* ========== METODOS ========== */
	public static String generarCuitAzar(String... tiposPersona) {
		String cuit = null;
		while (cuit == null) {
			String tipoPersona = Arrays.asList(tiposPersona).get(random(0, tiposPersona.length));
			String dni = random(10000000, 50000000).toString();
			cuit = generarCuit(tipoPersona, dni);
		}
		return cuit;
	}

	public static String generarCuit(String tipoPersona, String dni) {
		String cuit = "";
		cuit += MASCULINO.equals(tipoPersona) ? "20" : "";
		cuit += FEMENINO.equals(tipoPersona) ? "27" : "";
		cuit += EMPRESA.equals(tipoPersona) ? "30" : "";
		cuit += dni;
		if (digitoVerificador(cuit) == null) {
			cuit = "";
			cuit += MASCULINO.equals(tipoPersona) ? "23" : "";
			cuit += FEMENINO.equals(tipoPersona) ? "23" : "";
			cuit += EMPRESA.equals(tipoPersona) ? "33" : "";
			cuit += dni;
		}
		if (digitoVerificador(cuit) == null) {
			cuit = "";
			cuit += MASCULINO.equals(tipoPersona) ? "24" : "";
			cuit += FEMENINO.equals(tipoPersona) ? "24" : "";
			cuit += EMPRESA.equals(tipoPersona) ? "34" : "";
			cuit += dni;
		}
		cuit = digitoVerificador(cuit) != null ? cuit + digitoVerificador(cuit) : null;
		return cuit;
	}

	public static String digitoVerificador(String cuit) {
		if (cuit != null && cuit.length() >= 10 && cuit.matches("[0-9]+")) {
			Integer digitoVerificadorCuit = 0;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(0))) * 5;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(1))) * 4;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(2))) * 3;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(3))) * 2;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(4))) * 7;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(5))) * 6;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(6))) * 5;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(7))) * 4;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(8))) * 3;
			digitoVerificadorCuit += Integer.parseInt(String.valueOf(cuit.charAt(9))) * 2;
			digitoVerificadorCuit = digitoVerificadorCuit % 11;
			digitoVerificadorCuit = 11 - digitoVerificadorCuit;
			digitoVerificadorCuit = digitoVerificadorCuit == 11 ? 0 : digitoVerificadorCuit;
			digitoVerificadorCuit = digitoVerificadorCuit == 10 ? 1 : digitoVerificadorCuit;
			digitoVerificadorCuit = digitoVerificadorCuit == 0 ? null : digitoVerificadorCuit;
			return digitoVerificadorCuit != null ? digitoVerificadorCuit.toString() : null;
		}
		return null;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Api.habilitarLog = false;
		Contexto contexto = new Contexto("HB", "homologacion", "133366");
		for (Integer i = 0; i < 100; ++i) {
			String cuit = generarCuitAzar(CuitUtil.FEMENINO);
			if (ApiPersonas.persona(contexto, cuit).tryGet() == null) {
				System.out.println(cuit);
			}
		}
	}
}
