package ar.com.hipotecario.canal.officebanking.util;

public class StringUtil {

    public static String eliminarCerosIzquierda(String texto){
        if (texto==null||texto.isBlank())
        {
            return texto;
        }
        int idx = 0;
        while (idx< texto.length()&&texto.charAt(idx)=='0'){
            idx++;
        }
        if (idx==texto.length()){
            return texto;
        }
        return texto.substring(idx);
    }

    public static String eliminarEspaciosDerecha(String input) {
        if (input.trim().isEmpty()) {
            return "";
        }
        int endIndex = input.length() - 1;
        while (endIndex >= 0 && input.charAt(endIndex) == ' ') {
            endIndex--;
        }
        return input.substring(0, endIndex + 1);
    }

    public static String agregarCerosAIzquierda(String input, int x) {
        if (input.length() >= x) {
            return input;
        }

        int cantidadCeros = x - input.length();

        StringBuilder ceros = new StringBuilder();
        for (int i = 0; i < cantidadCeros; i++) {
            ceros.append("0");
        }

        return ceros.toString() + input;
    }
    public static String padLeftWithZeros(String input, int length) {
        if (input.length() >= length) {
            return input;
        }
        int zerosToAdd = length - input.length();
        StringBuilder paddedString = new StringBuilder();
        for (int i = 0; i < zerosToAdd; i++) {
            paddedString.append('0');
        }
        paddedString.append(input);

        return paddedString.toString();
    }

    public static String reemplazarTXT(String input){
        return input.replace("TXT","txt");
    }
    public static String agregarCerosIzquierda(int numero) {
        String numeroStr = String.valueOf(numero);
        int longitudActual = numeroStr.length();
        int cerosFaltantes = 7 - longitudActual;

        if (cerosFaltantes > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cerosFaltantes; i++) {
                sb.append('0');
            }
            sb.append(numeroStr);
            return sb.toString();
        } else {
            return numeroStr;
        }
    }

    public static String reemplazarCaracteresCodificacion(String texto){
        return texto.replace("Ã¡", "á")
                .replace("Ã©", "é")
                .replace("Ã­", "í")
                .replace("Ã³", "ó")
                .replace("Ãº", "ú")
                .replace("Ã±", "ñ")
                .replace("Ã", "Á")
                .replace("Ã‰", "É")
                .replace("Ã", "Í")
                .replace("Ã“", "Ó")
                .replace("Ãš", "Ú")
                .replace("Ã‘", "Ñ")
                .replace("â€œ", "“")
                .replace("â€", "”")
                .replace("â€˜", "‘")
                .replace("â€™", "’")
                .replace("â€“", "–")
                .replace("â€”", "—")
                .replace("Â", "");
    }
}
