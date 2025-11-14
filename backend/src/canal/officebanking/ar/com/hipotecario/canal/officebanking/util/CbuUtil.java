package ar.com.hipotecario.canal.officebanking.util;

public class CbuUtil {

    public static Boolean isValidCBU(String cbu) {
        if (cbu == null)
            return false;

        int[] cbuDigits = new int[22];
        char[] code = cbu.toCharArray();

        for (int i = 0; i < code.length; i++) {
            cbuDigits[i] = code[i] - '0';
        }

        int verifier1 = v1(cbuDigits, 0, 6);
        int verifier2 = v1(cbuDigits, 8, 20);

        return (verifier1 == cbuDigits[7] && verifier2 == cbuDigits[21]);
    }

    public static int v1(final int[] code, int pos1, int pos2) {

        final int[] M = {9, 7, 1, 3}; // multiplier

        int sum = 0;
        for (int i = pos2, j = -1; i >= pos1; i--, j--) {
            if (j == -1)
                j = 3;
            sum += code[i] * M[j];
        }
        return v2(sum);
    }

    public static int v2(int v1) {
        int mod = v1 % 10;
        return (mod == 0) ? 0 : (10 - mod);
    }

    public static String convertirPaddingLeft(String value) {
        //Completa con 0 a la izquierda, el nro de cuenta
        String strValue = "0";

        while (value.length() != 22) {
            value = strValue.concat(value);
        }

        return value;
    }

    public static String deletePaddingLeft(String value) {
        //Elimina con 0 a la izquierda, el nro de cuenta
        String strValue = null;

        for (int i = 0; i < value.length(); i++) {
            if (String.valueOf(value.charAt(i)).equals("0")) {
                strValue = value.substring(i + 1);
            } else {
                return strValue;
            }
        }

        return strValue;
    }
}
