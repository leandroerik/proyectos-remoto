package ar.com.hipotecario.canal.officebanking.enums.pagosMasivos;

public enum EnumRetencionTipo {
    RETEN_IMP_VALOR_AGREGADO(1, "Reten.Imp.Valor Agregado"),
    RETENC_IMP_GCIAS(2, "Retenc.Imp.a las Gcias"),
    RET_INGR_BTOS_CDAD_BS_AS(3, "Ret.Ingr.Btos.Cdad.Bs.As"),
    RET_INGR_BTOS_PROV_BS_AS(4, "Ret.Ingr.Btos.Prov.Bs.As"),
    RET_INGR_BTOS_CATAMARCA(5, "Ret.Ingr.Btos.Catamarca"),
    RET_INGR_BTOS_CHACO(6, "Ret.Ingr.Btos.Chacho"),
    RET_INGR_BTOS_CHUBUT(7, "Ret.Ingr.Btos.Chubut"),
    RET_INGR_BTOS_CORDOBA(8, "Ret.Ingr.Btos.Cordoba"),
    RET_INGR_BTOS_CORRIENTES(9, "Ret.Ingr.Btos.Corrientes"),
    RET_INGR_BTOS_ENTRE_RIOS(10, "Ret.Ingr.Btos.Entre Rios"),
    RET_INGR_BTOS_FORMOSA(11, "Ret.Ingr.Btos.Formosa"),
    RET_INGR_BTOS_JUJUY(12, "Ret.Ingr.Btos.Jujuy"),
    RET_INGR_BTOS_LA_PAMPA(13, "Ret.Ingr.Btos.La Pampa"),
    RET_INGR_BTOS_LA_RIOJA(14, "Ret.Ingr.Btos.La Rioja"),
    RET_INGR_BTOS_MENDOZA(15, "Ret.Ingr.Btos.Mendoza"),
    RET_INGR_BTOS_MISIONES(16, "Ret.Ingr.Btos.Misiones"),
    RET_INGR_BTOS_NEUQUEN(17, "Ret.Ingr.Btos.Neuquen"),
    RET_INGR_BTOS_RIO_NEGRO(18, "Ret.Ingr.Btos.Rio Negro"),
    RET_INGR_BTOS_SALTA(19, "Ret.Ingr.Btos.Salta"),
    RET_INGR_BTOS_SAN_JUAN(20, "Ret.Ingr.Btos.San Juan"),
    RET_INGR_BTOS_SAN_LUIS(21, "Ret.Ingr.Btos.San Luis"),
    RET_INGR_BTOS_SANTA_CRUZ(22, "Ret.Ingr.Btos.Santa Cruz"),
    RET_INGR_BTOS_SANTA_FE(23, "Ret.Ingr.Btos.Santa Fe"),
    RET_II_BB_SANT_ESTERO(24, "Ret.II.BB.Sant.del Estero"),
    RET_INGR_BTOS_TUCUMAN(25, "Ret.Ingr.Btos.Tucuman"),
    RET_II_BB_TIERRA_DEL_FUEGO(26, "Ret.IIBB.Tierra del Fuego"),
    RET_TASA_SEG_HIG_M_ZARATE(27, "Ret.Tasa Seg.Hig.M.Zárate"),
    RET_SEG_HIG_L_ZAMORA(28, "Ret. Seg Hig L de Zamora"),
    RET_CONTRATISTAS(29, "Retenciones contratistas"),
    RET_RG_1556_SERV_LIMPIEZA(30, "Ret. Rg1556 Serv Limpieza"),
    RET_RG_1769_SERV_SEGURIDAD(31, "Ret Rg1769 Serv Seguridad"),
    RET_RG_3164_SERV_SEG_LIMP(32, "Ret Rg3164 Serv Seg/Limp"),
    SS_PERSONAL_EVENTUAL_RG_3983(33, "SS Personal Eventual - RG 3983"),
    SS_PERSONAL_EVENTUAL_RG_3983_OS(34, "SS Personal Eventual - RG 3983 - O.Soc."),
    SUSS_SEG_SOCIAL_RG_1784(35, "SUSS Seguridad Social - RG 1784"),
    GANANCIAS_BENEF_EXTERIOR(36, "Ganancias - Beneficiarios del Exterior"),
    GANANCIAS_RG_830_00_RESP_INSC(37, "Ganancias RG 830/00 - Resp. Inscripto"),
    GANANCIAS_RG_1575_03_RESP_INSC_FC_M(38, "Ganancias RG 1575/03 - Resp. Insc. Fc M"),
    GANANCIAS_RG_830_00_NO_RESPONSABLES(39, "Ganancias RG 830/00 - No Responsables"),
    GANANCIAS_RG_2616_REG_SIMPLIFICADO(40, "Ganancias RG 2616 Reg Simplificado"),
    GANANCIAS_BENEF_EXTERIOR_TRANSFERENC(41, "Ganancias - Benef Exterior - TRANSFERENC"),
    IIBB_USHUAIA_ADIC_FONDO_SERV_SOC(42, "IIBB - Ushuaia - Adic. Fondo Serv. Soc."),
    IBRUTOS_SAN_JUAN_ADIC_LOTE_HOGAR(43, "I.Brutos-San Juan- Adicional Lote Hogar"),
    IBRUTOS_MENDOZA_PROFESIONALES(44, "Ingresos Brutos-Mendoza- Profesionales"),
    IBRUTOS_MENDOZA_PROV_BS_SERV(45, "Ingresos Brutos-Mendoza- Prov. Bs.y Serv"),
    IIBB_USHUAIA_ADIC_FONDO_FIN_SIST_PREV(46, "IIBB-Ushuaia-Adic. Fondo Fin. Sist. Prev"),
    IBRUTOS_USHUAIA(47, "Ingresos Brutos - Ushuaia"),
    IBRUTOS_LA_RIOJA_P_LIBERALES(48, "Ingresos Brutos - La Rioja - P.Liberales"),
    SUJETO_NO_CATEGORIZADO_RET_10_5(49, "Sujeto No Categorizado - Ret 10.5%"),
    RET_PACIS_RENTAS_MUNICIPALES_TUCUMAN(50, "Ret-P.A.C.I.S-Rentas Municipales Tucumán"),
    REG_RETENCION_IVA_RG_2854_10(51, "Régimen de retencion IVA RG 2854/10."),
    RG_RETENCION_IVA_RG_2854_10_PRO_CRE_AR(52, "Rg retencion IVA RG 2854/10. Pro.Cre.Ar"),
    IVA_RG_1575_03_FC_M_100(53, "IVA - RG 1575/03 - Fc M 100%"),
    RETENCION_IVA_SUSTITUTIVA(54, "Retencion IVA Sustitutiva"),
    IVA_RG_2616_REG_SIMPLIFICADO(55, "IVA - RG 2616 Reg Simplificado"),
    SISTEMATICO_SEG_SOCIAL(100, "Sistematico Seg. Social"),
    TASA_COMERCIO_COMODORO_RIV(101, "Tasa Comercio ComodoroRiv"),
    OTROS(997, "Otros"),
    ORDEN_DE_PAGO(998, "Orden de Pago"),
    COMP_IMPRESO_ADHEREN(999, "Comp. impreso por adheren");

    private final int codigo;
    private final String nombre;

    EnumRetencionTipo(int codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public static EnumRetencionTipo fromCodigo(int codigo) {
        for (EnumRetencionTipo tipo : EnumRetencionTipo.values()) {
            if (tipo.getCodigo() == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de retención no válido: " + codigo);
    }
}

