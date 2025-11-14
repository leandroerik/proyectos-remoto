package ar.com.hipotecario.backend.servicio.api.productos;

import java.util.Arrays;
import java.util.Optional;

public enum EnumProductos {

        ADMINISTRACION(1, "ADMINISTRACION", "ADM", "V"),
        MASTER_INFORMATION_SUBSYSTEM(2, "MASTER INFORMATION SUBSYSTEM", "MIS", "V"),
        CUENTA_CORRIENTE(3, "CUENTA CORRIENTE", "CTE", "V"),
        CUENTA_DE_AHORROS(4, "CUENTA DE AHORROS", "AHO", "V"),
        FIRMAS(5, "FIRMAS", "FIR", "V"),
        CONTABILIDAD(6, "CONTABILIDAD", "CON", "V"),
        CARTERA(7, "CARTERA", "CCA", "V"),
        BATCH(8, "BATCH", "BAT", "V"),
        COMERCIO_EXTERIOR(9, "COMERCIO EXTERIOR", "CEX", "V"),
        REMESAS_Y_CAMARA(10, "REMESAS Y CAMARA", "REM", "V"),
        MESA_DE_CAMBIOS(12, "MESA DE CAMBIOS", "MCA", "V"),
        ATX_BRANCH(13, "ATX - BRANCH", "ATX", "V"),
        DEPOSITOS_A_PLAZO_FIJO(14, "DEPOSITOS A PLAZO FIJO", "PFI", "V"),
        ATM_ADMINISTRACION_TARJETAS_DE_DEBITO(16, "ATM - ADMINISTRACION TARJETAS DE DEBITO", "ATM", "V"),
        PERSONALIZACION(17, "PERSONALIZACION", "PER", "V"),
        BANCA_VIRTUAL(18, "BANCA VIRTUAL", "BVI", "V"),
        GARANTIA(19, "GARANTIA", "GAR", "V"),
        CREDITO(21, "CREDITO", "CRE", "V"),
        TRAMITES(23, "TRAMITES", "TRA", "V"),
        RIESGO(24, "RIESGO", "RIE", "V"),
        CONCENTRADOR(25, "CONCENTRADOR", "CTR", "V"),
        BRANCH(26, "BRANCH", "BRA", "V"),
        TARJETAS_DE_CREDITO(28, "TARJETAS DE CREDITO", "ATC", "V"),
        BCRA_DGI(29, "BCRA-DGI", "BCR", "V"),
        RECAUDACIONES(30, "RECAUDACIONES", "REC", "V"),
        DATANET(31, "DATANET", "DNT", "V"),
        JUBILACIONES(32, "JUBILACIONES", "JUB", "V"),
        PAQUETE_DE_PRODUCTOS(80, "PAQUETE DE PRODUCTOS", "PAQ", "V"),
        CAJA_DE_SEGURIDAD(82, "CAJA DE SEGURIDAD", "CSG", "V"),
        EMPRENDIMIENTOS(83, "EMPRENDIMIENTOS", "EMP", "V"),
        SEGUROS(84, "SEGUROS", "SEG", "V"),
        CLIENTES_IPV(85, "CLIENTES IPV", "IPV", "V"),
        ADMINISTRADOR_DE_PARAMETRIZACION(151, "ADMINISTRADOR DE PARAMETRIZACION", "ADP", "V"),
        PRESTAMOS_PRENDARIOS_NSP(200, "PRESTAMOS PRENDARIOS NSP", "PPN", "V"),
        PRESTAMOS_PERSONALES_NSP(201, "PRESTAMOS PERSONALES NSP", "NSP", "V"),
        UNITRADE(202, "UNITRADE", "UNI", "V"),
        SMARTOPEN(203, "SMARTOPEN", "SMA", "V"),
        FACTORING(204, "FACTORING", "FAC", "V"),
        PRESTAMOS_HIPOTECARIOS_NSP(205, "PRESTAMOS HIPOTECARIOS NSP", "PHN", "V"),
        EMERIX(206, "EMERIX", "EME", "V"),
        INLENDER_PARA_COMPRA_DE_CARTERA(207, "INLENDER PARA COMPRA DE CARTERA", "LEN", "V"),
        FONDOS_COMUNES_DE_INVERSION(208, "FONDOS COMUNES DE INVERSION", "RJA", "V"),
        SERVICIO_CONSULTIVO_COBIS(209, "SERVICIO CONSULTIVO COBIS", "SRV", "V"),
        SISTEMA_DE_LEASING(210, "Sistema de Leasing", "LSG", "V"),
        CUSTODIA_GLOBAL(211, "CUSTODIA GLOBAL", "CGL", "V"),
        SISTEMA_SAP_DEUDORES(212, "SISTEMA SAP DEUDORES", "SAP", "V"),
        DUENOS_EXPRESS(213, "DUENOS EXPRESS", "DEX", "V"),
        SAT_PAGO_A_PROVEEDORES(214, "SAT - PAGO A PROVEEDORES", "SAT", "V"),
        LINK(215, "LINK", "LNK", "V"),
        MEP_MEDIO_ELECTRONICO_DE_PAGOS(216, "MEP MEDIO ELECTRONICO DE PAGOS", "MEP", "V"),
        CEDIP(217, "CEDIP", "CDP", "V");

        private final int codigo;
        private final String nombre;
        private final String abreviatura;
        private final String estado;

        // Constructor privado
        private EnumProductos(int codigo, String nombre, String abreviatura, String estado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.abreviatura = abreviatura;
            this.estado = estado;
        }

    // Getters para acceder a los atributos
        public int getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getAbreviatura() {
            return abreviatura;
        }

        public String getEstado() {
            return estado;
        }

    public static Optional<EnumProductos> buscarPorCodigo(int codigo) {
        return Arrays.stream(EnumProductos.values())
                .filter(tipo -> tipo.getCodigo() == codigo)
                .findFirst();
    }

}
