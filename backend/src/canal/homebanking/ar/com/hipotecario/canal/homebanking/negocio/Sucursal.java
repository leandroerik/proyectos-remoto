package ar.com.hipotecario.canal.homebanking.negocio;

import java.util.HashMap;
import java.util.Map;

public class Sucursal {
    public static String email(String id) {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("52", "SUC_52@hipotecario.com.ar");
        mapa.put("47", "SUC_47@hipotecario.com.ar");
        mapa.put("1", "SUC_01@hipotecario.com.ar");
        mapa.put("0", "SUC_00@hipotecario.com.ar");
        mapa.put("3", "SUC_03@hipotecario.com.ar");
        mapa.put("57", "SUC_57@hipotecario.com.ar");
        mapa.put("4", "SUC_04@hipotecario.com.ar");
        mapa.put("6", "SUC_06@hipotecario.com.ar");
        mapa.put("7", "SUC_07@hipotecario.com.ar");
        mapa.put("8", "SUC_08@hipotecario.com.ar");
        mapa.put("38", "SUC_38@hipotecario.com.ar");
        mapa.put("11", "SUC_11@hipotecario.com.ar");
        mapa.put("12", "SUC_12@hipotecario.com.ar");
        mapa.put("13", "SUC_13@hipotecario.com.ar");
        mapa.put("14", "SUC_14@hipotecario.com.ar");
        mapa.put("65", "SUC_65@hipotecario.com.ar");
        mapa.put("62", "SUC_62@hipotecario.com.ar");
        mapa.put("15", "SUC_15@hipotecario.com.ar");
        mapa.put("16", "SUC_16@hipotecario.com.ar");
        mapa.put("54", "SUC_54@hipotecario.com.ar");
        mapa.put("17", "SUC_17@hipotecario.com.ar");
        mapa.put("18", "SUC_18@hipotecario.com.ar");
        mapa.put("83", "SUC_83@hipotecario.com.ar");
        mapa.put("22", "SUC_22@hipotecario.com.ar");
        mapa.put("53", "SUC_53@hipotecario.com.ar");
        mapa.put("23", "SUC_23@hipotecario.com.ar");
        mapa.put("81", "SUC_81@hipotecario.com.ar");
        mapa.put("24", "SUC_24@hipotecario.com.ar");
        mapa.put("25", "SUC_25@hipotecario.com.ar");
        mapa.put("26", "SUC_26@hipotecario.com.ar");
        mapa.put("45", "SUC_45@hipotecario.com.ar");
        mapa.put("27", "SUC_27@hipotecario.com.ar");
        mapa.put("28", "SUC_28@hipotecario.com.ar");
        mapa.put("43", "SUC_43@hipotecario.com.ar");
        mapa.put("29", "SUC_29@hipotecario.com.ar");
        mapa.put("49", "SUC_49@hipotecario.com.ar");
        mapa.put("30", "SUC_30@hipotecario.com.ar");
        mapa.put("61", "SUC_61@hipotecario.com.ar");
        mapa.put("71", "SUC_71@hipotecario.com.ar");
        mapa.put("77", "SUC_77@hipotecario.com.ar");
        mapa.put("32", "SUC_32@hipotecario.com.ar");
        mapa.put("33", "SUC_33@hipotecario.com.ar");
        mapa.put("34", "SUC_34@hipotecario.com.ar");
        mapa.put("35", "SUC_35@hipotecario.com.ar");
        mapa.put("63", "SUC_63@hipotecario.com.ar");
        mapa.put("36", "SUC_36@hipotecario.com.ar");
        mapa.put("37", "SUC_37@hipotecario.com.ar");
        mapa.put("46", "SUC_46@hipotecario.com.ar");
        mapa.put("51", "SUC_51@hipotecario.com.ar");
        mapa.put("40", "SUC_40@hipotecario.com.ar");
        mapa.put("56", "SUC_56@hipotecario.com.ar");
        mapa.put("69", "SUC_69@hipotecario.com.ar");
        mapa.put("76", "SUC_76@hipotecario.com.ar");
        String valor = mapa.get(id);
        return valor != null ? valor : "";
    }
}
