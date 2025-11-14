package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;

public class NumeroEnLetras {
    private static final String[] UNIDADES = {
            "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"
    };

    private static final String[] DIEZ_A_DIECINUEVE = {
            "diez", "once", "doce", "trece", "catorce", "quince",
            "dieciséis", "diecisiete", "dieciocho", "diecinueve"
    };

    private static final String[] DECENAS = {
            "", "", "veinte", "treinta", "cuarenta", "cincuenta",
            "sesenta", "setenta", "ochenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos",
            "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    public static String convertir(BigDecimal numero) {
        long parteEntera = numero.longValue();
        int parteDecimal = numero.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String letras = convertirNumeroCompleto(parteEntera);
        String resultado = letras + " con " + String.format("%02d", parteDecimal) + "/100 CENTAVOS";

        return resultado.toUpperCase();
    }

    private static String convertirNumeroCompleto(long numero) {
        if (numero == 0) return "cero";

        StringBuilder resultado = new StringBuilder();

        if (numero >= 1_000_000_000) {
            resultado.append(convertirNumeroCompleto(numero / 1_000_000_000)).append(" mil millones ");
            numero %= 1_000_000_000;
        }

        if (numero >= 1_000_000) {
            long millones = numero / 1_000_000;
            if (millones == 1) {
                resultado.append("un millón ");
            } else {
                resultado.append(convertirNumeroCompleto(millones)).append(" millones ");
            }
            numero %= 1_000_000;
        }

        if (numero >= 1000) {
            if (numero / 1000 == 1) {
                resultado.append("mil ");
            } else {
                resultado.append(convertirNumeroCompleto(numero / 1000)).append(" mil ");
            }
            numero %= 1000;
        }

        if (numero >= 100) {
            if (numero == 100) {
                resultado.append("cien ");
            } else {
                resultado.append(CENTENAS[(int)(numero / 100)]).append(" ");
            }
            numero %= 100;
        }

        if (numero >= 20) {
            resultado.append(DECENAS[(int)(numero / 10)]);
            if (numero % 10 != 0) {
                resultado.append(" y ").append(UNIDADES[(int)(numero % 10)]);
            }
        } else if (numero >= 10) {
            resultado.append(DIEZ_A_DIECINUEVE[(int)(numero - 10)]);
        } else if (numero > 0) {
            resultado.append(UNIDADES[(int) numero]);
        }

        return resultado.toString().trim();
    }
}
