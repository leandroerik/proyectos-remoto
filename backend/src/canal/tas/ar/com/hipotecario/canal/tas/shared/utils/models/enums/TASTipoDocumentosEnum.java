package ar.com.hipotecario.canal.tas.shared.utils.models.enums;

import java.util.Arrays;

public enum TASTipoDocumentosEnum {

    DNI ("01"),
    LIBRETA_DE_ENROLAMIENTO ("02"),
    LIBRETA_CIVICA ("03"),
    CUIL ("08"),
    CUIT ("11"),
    CI_POLICIA_FEDERAL_ARGENTINA ("101"),
    CI_POLICIA_PROVINCIA_BUENOS_AIRES ("102"),
    CI_POLICIA_PROVINCIA_CATAMARCA ("103"),
    CI_POLICIA_PROVINCIA_CORDOBA ("104"),
    CI_POLICIA_PROVINCIA_CORRIENTES ("105"),
    CI_POLICIA_PROVINCIA_CHACO ("106"),
    CI_POLICIA_PROVINCIA_CHUBUT ("107"),
    CI_POLICIA_PROVINCIA_ENTRE_RIOS ("108"),
    CI_POLICIA_PROVINCIA_FORMOSA ("109"),
    CI_POLICIA_PROVINCIA_JUJUY ("110"),
    CI_POLICIA_PROVINCIA_LA_PAMPA ("111"),
    CI_POLICIA_PROVINCIA_RIOJA ("112"),
    CI_POLICIA_PROVINCIA_MENDOZA ("113"),
    CI_POLICIA_PROVINCIA_MISIONES ("114"),
    CI_POLICIA_PROVINCIA_NEUQUEN ("115"),
    CI_POLICIA_PROVINCIA_RIO_NEGRO ("116"),
    CI_POLICIA_PROVINCIA_SALTA ("117"),
    CI_POLICIA_PROVINCIA_SAN_JUAN ("118"),
    CI_POLICIA_PROVINCIA_SAN_LUIS ("119"),
    CI_POLICIA_PROVINCIA_SANTA_CRUZ ("120"),
    CI_POLICIA_PROVINCIA_SANTA_FE ("121"),
    CI_POLICIA_PROVINCIA_SANTIAGO_DEL_ESTERO ("122"),
    CI_POLICIA_PROVINCIA_TUCUMAN ("123"),
    PASAPORTE_EXTRANJERO ("125"),
    PASAPORTE_ARGENTINO ("126"),
    DOC_EXTRANJEROS_RESIDENTES_EN_ARGENTINA ("134"),
    DOCUMENTO_PARA_EXTRANJEROS ("135"),
    DNI_PARA_MENORES ("136"),
    CI_POLICIA_PROVINCIA_TIERRA_DEL_FUEGO ("140"),
    PRESOS_NRO_PRONTUARIO ("998"),
    PERSONAL_MIGRACION ("999"),
    DOCUMENTO_UNICO ("141"),
    DOCUMENTO_IPV ("142"),
    COMPROBANTE_CUIT ("143"),
    PYMES ("144"),
    RESOLUCION_MINISTERIO ("145"),
    VUELCO_CEDULA_IDENTIDAD ("146"),
    VUELCO_DOCUMENTO_UNICO ("147"),
    VUELCO_LIBRETA_CIVICA ("148"),
    VUELCO_LIBRETA_ENROLAMIENTO ("149"),
    VUELCO_LIBRETA_SIN_DATOS ("150"),
    VUELCO_NSP2 ("151"),
    VUELCO ("152"),
    DOCUMENTO_ARGENTINO_RESIDENTES_EN_EL_EXTERIOR ("153"),
    PASAPORTE_ARGENTINO_RESIDENTES_EN_EL_EXTERIOR ("154"),
    ;
    private String  desc = null;
    TASTipoDocumentosEnum(String tipoDocumento) {
        this.desc = tipoDocumento;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getTipoDocumento(String tipoDocumento){
        String tipoDoc = String.valueOf(Arrays.stream(TASTipoDocumentosEnum.values()).filter(tipo -> tipo.desc.equals((tipoDocumento))).findFirst());
        return tipoDoc.replace("Optional[", "").replace("]", "");
    }


}
