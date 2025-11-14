package ar.com.hipotecario.canal.tas.shared.utils.models.enums;

import java.util.Arrays;

public enum TASTipoDocumentoDescCortaEnum {
    DNI ("01"),
    LE ("02"),
    LC ("03"),
    CI ("101"),
    CIB ("102"),
    CIK ("103"),
    CIX ("104"),
    CIW ("105"),
    CIH ("106"),
    CIU ("107"),
    CIE ("108"),
    CIP ("109"),
    CIY ("110"),
    CIL ("111"),
    CIF ("112"),
    CIM ("113"),
    CIN ("114"),
    CIQ ("115"),
    CIR ("116"),
    CIA ("117"),
    CIJ ("118"),
    CID ("119"),
    CIZ ("120"),
    CIS ("121"),
    CIG ("122"),
    CIT ("123"),
    PS ("125"),
    PAS ("126"),
    DPE ("134"),
    DOCUMENTO_EXTRANJERO ("135"),//TODO HARDCODE DPE
    DNI_MENORES ("136"),//TODO HARDCODE DNI
    CIV ("140"),
    PNP ("998"),
    PPM ("999"),
    DOCUMENTO_UNICO ("141"),//HARDCODE DNI
    IPV ("142"),
    CCU ("143"),
    PYM ("144"),
    RM ("145"),
    VCI ("146"),
    VDU ("147"),
    VLC ("148"),
    VLE ("149"),
    VSD ("150"),
    VU2 ("151"),
    VUE ("152"),
    DOCUMENTO_ARG_RESIDENTES_EXTERIOR ("153"),// TODO HARDCODE 153
    PASAPORTE_ARGENTINO_RESIDENTES ("154"),// TODO HARDCODE PAS
    CUIL ("08"), //TODO HARDCODE 08
    CUIT ("11"),;//TODO HARDCODE 11
    private String descCorta;

    TASTipoDocumentoDescCortaEnum(String descCorta) {
        this.descCorta = descCorta;
    }

    public String getDescCorta() {
        return descCorta;
    }

    public static String getDescripcionCorta(String tipoDocumento){
        String tipo = "";
        switch (tipoDocumento){
            case "135":
                tipo = "DPE";
                return tipo;                
            case "136", "141":
                tipo = "DNI";
                return tipo;
            case "153":
                tipo = "153";
                return tipo; 
            case "154":
                tipo = "PAS";
                return tipo; 
            case "08":
                tipo = "08";
                return tipo; 
            case "11":
                tipo = "11";
                return tipo;
            default:
                break;
        }
        String descripcionCorta = String.valueOf(Arrays.stream(TASTipoDocumentoDescCortaEnum.values()).filter(tipe -> tipe.descCorta.equals((tipoDocumento))).findFirst());
        descripcionCorta = descripcionCorta.replace("Optional[", "").replace("]", "");
        return descripcionCorta;

    }

    public void setDescCorta(String descCorta) {
        this.descCorta = descCorta;
    }
}
