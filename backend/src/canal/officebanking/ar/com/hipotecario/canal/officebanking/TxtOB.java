package ar.com.hipotecario.canal.officebanking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import com.github.jknack.handlebars.internal.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Objeto;

public abstract class TxtOB {

	public static byte[] txtDescargaMovimientos(Map<String, Object> parametros) {
		ByteArrayOutputStream fileOut = new ByteArrayOutputStream();

		Objeto movimientos = (Objeto) parametros.get("MOVIMIENTOS");
		List<Objeto> lstMovimientos = movimientos.objetos();

		try {

			if (lstMovimientos != null && !lstMovimientos.isEmpty()) {
				String lineaRegMov = new String();
				String separador = "|";
				PrintStream formatStream = new PrintStream(fileOut);

				for (Objeto movimiento : lstMovimientos) {
					String debito = "";
					String credito = "";
					movimiento.set("cuitOriginante", "");
					movimiento.set("razonSocial", "");
					if (movimiento.get("importe").toString().contains("-")) {
						debito = movimiento.get("importe").toString().replace("-", "");
					} else {
						credito = movimiento.get("importe").toString();
						movimiento = extraerDatosOriginante(movimiento);
					}

					lineaRegMov = StringUtils.leftPad(((movimiento.get("fecha") != null) ? movimiento.get("fecha").toString() : ""), 8, " ") + separador + StringUtils.rightPad(((movimiento.get("descripcion") != null) ? movimiento.get("descripcion").toString() : ""), 100, " ") + separador + StringUtils.leftPad(((movimiento.get("sucursal") != null) ? movimiento.get("sucursal").toString() : ""), 3, "0") + separador + StringUtils.rightPad(((movimiento.get("referencia") != null) ? movimiento.get("referencia").toString() : ""), 30, " ") + separador + StringUtils.leftPad(((debito != null) ? debito.replace(".", "") : ""), 30, "0") + separador + StringUtils.leftPad(((credito != null) ? credito.replace(".", "") : ""), 30, "0") + separador
							+ StringUtils.leftPad(((movimiento.get("saldo") != null) ? movimiento.get("saldo").toString().replace(".", "") : ""), 30, "0") + separador + StringUtils.leftPad(((movimiento.get("cuitOriginante") != null) ? movimiento.get("cuitOriginante").toString() : ""), 11, " ") + separador + StringUtils.rightPad(((movimiento.get("razonSocial") != null) ? movimiento.get("razonSocial").toString() : ""), 80, " ");

					formatStream.write(lineaRegMov.getBytes());
					formatStream.println();

				}
				formatStream.close();
				fileOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileOut.toByteArray();

	}

	private static Objeto extraerDatosOriginante(Objeto movimiento) {
		try {
			String[] subCampos = StringUtils.split(movimiento.get("descCausa").toString(), "-");
			if (subCampos.length > 2 && !isInvalidCuit(subCampos[subCampos.length - 2].replace(" ", ""))) {

				String cuitOriginante = (subCampos[subCampos.length - 2].replace(" ", ""));
				if (subCampos[subCampos.length - 1].length() > 1 && (subCampos[subCampos.length - 1]).startsWith(" ")) {
					subCampos[subCampos.length - 1] = subCampos[subCampos.length - 1].substring(1, subCampos[subCampos.length - 1].length());
				}
				String razonSocial = (subCampos[subCampos.length - 1]);

				movimiento.set("cuitOriginante", cuitOriginante);
				movimiento.set("razonSocial", razonSocial);

				return movimiento;

			}
		} catch (Exception e) {

		}
		return movimiento;
	}

	public static Boolean isInvalidCuit(String value) {
		int[] mult = new int[] { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
		String cuit = value.toString();

		if (cuit.length() != 11)
			return true;
		int total = 0;
		for (int i = 0; i < mult.length; i++) {
			total += Integer.valueOf(Character.toString(cuit.charAt(i))) * mult[i];
		}

		int resto = total % 11;
		int digito = resto == 0 ? 0 : resto == 1 ? 9 : 11 - resto;

		return digito != Integer.valueOf(Character.toString(cuit.charAt(10)));
	}

}
