package ar.com.hipotecario.backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;

public class Validadores {
	/* ========== METODOS ========== */
	public static Boolean esMailValido(String mail) {
		return mail.contains("@");
	}

	public static Boolean esNumerico(String numero) {
		return Pattern.matches("[0-9]+", numero);
	}

	public static Boolean esExtranjero(String nroDocumento) {
		try {
			Integer dni = Integer.parseInt(nroDocumento);
			if (dni >= 60000000)
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static Boolean ocrValidoVU(Objeto ocr) {
		if (ocr == null || ocr.objeto("extra", null) == null) {
			return false;
		}

		Objeto extra = ocr.objeto("extra");
		return !Util.empty(extra.string("additional"));
	}

	public static Boolean mrzValidoVU(Objeto ocr) {
		if (!ocrValidoVU(ocr)) {
			return false;
		}

		Objeto extra = ocr.objeto("extra");
		return !Util.empty(extra.string("mrz"));
	}

	public static Boolean barcodeValidoVU(Objeto barcode) {
		if (barcode == null) {
			return false;
		}

		Boolean esValido = barcode.objeto("data", null) != null;
		esValido &= barcode.bool("contains");

		return esValido;
	}

	public static Boolean anomaliesValidoVU(Objeto anomalies) {
		if (anomalies == null) {
			return false;
		}

		return !Util.empty(anomalies);
	}

	public static Boolean informationValidoVU(Objeto information) {
		if (information == null) {
			return false;
		}

		return !Util.empty(information.string("person"));
	}

	public static Boolean mismaPersona(Contexto contexto, String cuil, String numeroDocumento) {
		Sesion sesion = contexto.sesion();

		if (Util.empty(sesion.cuil) && Util.empty(sesion.numeroDocumento)) {
			return false;
		}

		if (Util.empty(cuil) && Util.empty(numeroDocumento)) {
			return false;
		}

		Boolean esIgual = true;

		if (!Util.empty(numeroDocumento)) {
			esIgual &= numeroDocumento.equals(sesion.numeroDocumento);
		}

		if (!Util.empty(cuil)) {
			String numDoc = cuil.length() >= 10 ? cuil.substring(2, 10) : cuil;
			if (sesion.numeroDocumento.length() == 7) {
				numDoc = numDoc.charAt(0) == '0' ? numDoc.substring(1) : numDoc;
			}
			esIgual &= numDoc.equals(sesion.numeroDocumento);
		}

		if (!Util.empty(sesion.cuil)) {
			String numDoc = sesion.cuil.length() >= 10 ? sesion.cuil.substring(2, 10) : sesion.cuil;
			if (numeroDocumento.length() == 7) {
				numDoc = numDoc.charAt(0) == '0' ? numDoc.substring(1) : numDoc;
			}
			esIgual &= numDoc.equals(numeroDocumento);
		}

		return esIgual;
	}

	private static String convertSpecialChars(String str) {
		String tab00c0 = "aaaaaaaceeeeiiii" + "DNOOOOO\u00d7\u00d8UUUUYI\u00df" + "aaaaaaaceeeeiiii" + "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey" + "aaaaaaccccccccdd" + "ddeeeeeeeeeegggg" + "gggghhhhiiiiiiii" + "iijjjjkkklllllll" + "lllnnnnnnnnnoooo" + "oooorrrrrrssssss" + "ssttttttuuuuuuuu" + "uuuuwwyyyzzzzzzf" + "bbbbbboccddddoee" + "effgyhltikklawnn" + "ooooopprsseltttt" + "uuuuyyzz3ee3255t" + "plll!dddjjjnnnaa" + "iioouuuuuuuuuuea" + "aaaaaggggkkoooo3" + "3jdddgghpnnaaaao" + "oaaaaeeeeiiiiooo orrrruuuusstt33h" + "hnd88zzaaeeooooo" + "oooyybnbjbpacclt" + "sz??buaeejjqrrryy";

		String strMapped = str.replace("-", "").replaceAll("[^a-zA-ZÀ-ÿ\u00f1\u00d1\\s]", " ").replace("ñ", "\001").replace("Ñ", "\002");

		String strNormalizado = Normalizer.normalize(strMapped, Normalizer.Form.NFKD);
		StringBuilder strBuilder = new StringBuilder();
		for (int idx = 0; idx < strNormalizado.length(); ++idx) {
			char ch = strNormalizado.toLowerCase().charAt(idx);

			if (ch >= '\u00c0' && ch <= '\u024f') {
				ch = tab00c0.charAt((int) ch - '\u00c0');
			}
			strBuilder.append(ch);
		}

		return strBuilder.toString().replaceAll("[^\\p{ASCII}]", "").replace("\001", "ñ").replace("\002", "Ñ");
	}

	public static String filtro(String str) {
		if (Util.empty(str))
			return "";

		String strConvert = convertSpecialChars(str);
		if (Util.empty(strConvert))
			return "";

		return strConvert.trim().replaceAll("\\s+", " ");
	}

	public static String filtroUpper(String str) {
		if (Util.empty(str))
			return "";

		String strFiltrado = filtro(str);
		if (Util.empty(strFiltrado))
			return "";

		return strFiltrado.toUpperCase();
	}

	public static <T extends Object> T replace(T obj, T def) {
		return !Util.empty(obj) ? obj : def;
	}

	public static String nullString(String str, Boolean esStrNumerico) {
		if (Util.empty(str))
			return esStrNumerico ? null : null;
		if (str.equals("null")) {
			return null;
		} else {
			return str;
		}
	}

	public static String nullString(String str) {
		return nullString(str, false);
	}

	public static String nullString(Integer number) {
		String str = String.valueOf(number);
		str = "null".equals(str.toLowerCase()) ? "" : str;
		return nullString(str, true);
	}

	/* ========== METODOS ========== */
//	public static void main(String[] args) {
//		System.out.println(nullString(""));
//		System.out.println(nullString("") instanceof String);
//
//		ContextoBB contexto = new ContextoBB(GeneralBB.CANAL_CODIGO, Config.ambiente(), "1");
//		SesionBB sesion = contexto.sesion();
//		for (int i = 0; i < 7; i++) {
//			Boolean igual1 = false;
//			Boolean igual2 = false;
//			Boolean igual3 = false;
//			Boolean igual4 = false;
//
//			if (i == 1) {
//				sesion.cuil = "20420009975";
//				sesion.numeroDocumento = "";
//			}
//
//			if (i == 2) {
//				sesion.cuil = "";
//				sesion.numeroDocumento = "42000997";
//			}
//
//			if (i == 3) {
//				sesion.cuil = "20420009975";
//				sesion.numeroDocumento = "42000997";
//			}
//
//			if (i == 4) {
//				sesion.cuil = "20420009985";
//				sesion.numeroDocumento = "";
//
//				igual1 = true;
//				igual2 = true;
//				igual3 = true;
//			}
//
//			if (i == 5) {
//				sesion.cuil = "";
//				sesion.numeroDocumento = "42000998";
//
//				igual1 = true;
//				igual2 = true;
//				igual3 = true;
//			}
//
//			if (i == 6) {
//				sesion.cuil = "20420009985";
//				sesion.numeroDocumento = "42000998";
//
//				igual1 = true;
//				igual2 = true;
//				igual3 = true;
//			}
//
//			String cuil1 = "20420009985";
//			String dni1 = "42000998";
//
//			String cuil2 = "";
//			String dni2 = "42000998";
//
//			String cuil3 = "20420009985";
//			String dni3 = "";
//
//			String cuil4 = "";
//			String dni4 = "";
//
//			String formatStr = "SES_CUIL: %11s | SES_DNI: %8s || CUIL: %11s | DNI: %8s | PASS: %5b | IGUAL: %5b";
//			Boolean calc1 = mismaPersona(contexto, cuil1, dni1);
//			Boolean calc2 = mismaPersona(contexto, cuil2, dni2);
//			Boolean calc3 = mismaPersona(contexto, cuil3, dni3);
//			Boolean calc4 = mismaPersona(contexto, cuil4, dni4);
//			Boolean pass1 = calc1 == igual1;
//			Boolean pass2 = calc2 == igual2;
//			Boolean pass3 = calc3 == igual3;
//			Boolean pass4 = calc4 == igual4;
//
//			System.out.println(String.format(formatStr, sesion.cuil, sesion.numeroDocumento, cuil1, dni1, pass1, calc1));
//			System.out.println(String.format(formatStr, sesion.cuil, sesion.numeroDocumento, cuil2, dni2, pass2, calc2));
//			System.out.println(String.format(formatStr, sesion.cuil, sesion.numeroDocumento, cuil3, dni3, pass3, calc3));
//			System.out.println(String.format(formatStr, sesion.cuil, sesion.numeroDocumento, cuil4, dni4, pass4, calc4));
//		}
//	}
}
