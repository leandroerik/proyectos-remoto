package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumSucursalesOB {
    YERBA_BUENA(76, "4107"),
    VILLA_URQUIZA(69, "1431"),
    VILLA_MARIA(56, "5900"),
    VIEDMA(40, "8500"),
    VENADO_TUERTO(51, "2600"),
    USHUAIA(46, "9410"),
    TUCUMAN(37, "4000"),
    TRIBUNALES(59, "1003"),
    TRELEW(36, "9100"),
    TORRE_SAN_MARTIN(58, "1038"),
    TIGRE(63, "1648"),
    TANDIL(35, "7000"),
    SANTIAGO_DEL_ESTERO(34, "4200"),
    SANTA_ROSA(33, "6300"),
    SANTA_FE(32, "3000"),
    SAN_RAFAEL(31, "5500"),
    SAN_NICOLAS(77, "2900"),
    SAN_MIGUEL(71, "1663"),
    SAN_MARTIN(61, "1650"),
    SAN_LUIS(30, "5700"),
    SAN_JUSTO(49, "1754"),
    SAN_JUAN(29, "5400"),
    SAN_ISIDRO(55, "1648"),
    SAN_FRANCISCO(43, "2400"),
    SALTA(28, "4400"),
    ROSARIO(27, "2000"),
    RIO_GALLEGOS(45, "9400"),
    RIO_CUARTO(26, "5800"),
    RESISTENCIA(25, "3500"),
    RECONQUISTA(24, "3560"),
    RAMOS_MEJIA(81, "1754"),
    RAFAELA(23, "2300"),
    QUILMES(53, "1878"),
    POSADAS(22, "3300"),
    PILAR(83, "1629"),
    PERGAMINO(21, "2900"),
    PARANA(18, "3100"),
    NEUQUEN(17, "8300"),
    MORON(54, "1708"),
    MORENO(82, "1708"),
    MONTE_GRANDE(73, "1832"),
    MENDOZA(16, "5500"),
    MAR_DEL_PLATA(15, "7600"),
    LUJAN(62, "6700"),
    LOMAS_DE_ZAMORA(65, "1832"),
    LANUS(60, "1824"),
    LA_RIOJA(14, "5300"),
    LA_PLATA(13, "1900"),
    JUNIN(12, "6000"),
    JUJUY(11, "4600"),
    GENERAL_PICO(44, "6360"),
    FORMOSA(38, "3600"),
    FLORES(70, "1045"),
    CORRIENTES(8, "3400"),
    CORDOBA(7, "5000"),
    CONCORDIA(6, "3200"),
    COMODORO_RIVADAVIA(4, "9000"),
    CERRO_DE_LAS_ROSAS(57, "5009"),
    CATAMARCA(3, "4700"),
    CASA_CENTRAL(99, "1003"),
    CALETA_OLIVIA(75, "9011"),
    BUENOS_AIRES(0, "1003"),
    BELGRANO(64, "1431"),
    BAHIA_BLANCA(1, "8000"),
    AVELLANEDA(47, "1870"),
    ALMAGRO(52, "1045");

    private final int cobSucSucursal;
    private final String codigoPostal;

    EnumSucursalesOB(int cobSucSucursal, String codigoPostal) {
        this.cobSucSucursal = cobSucSucursal;
        this.codigoPostal = codigoPostal;
    }

    public int getCobSucSucursal() {
        return cobSucSucursal;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public static String getCodigoPostalBySucursal(int cobSucSucursal) {
        for (EnumSucursalesOB enumSucursal : EnumSucursalesOB.values()) {
            if (enumSucursal.getCobSucSucursal() == cobSucSucursal) {
                return enumSucursal.getCodigoPostal();
            }
        }
        throw new IllegalArgumentException("Sucursal no encontrada: " + cobSucSucursal);
    }
}
