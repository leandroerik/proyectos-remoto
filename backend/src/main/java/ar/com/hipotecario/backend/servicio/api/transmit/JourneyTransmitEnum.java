package ar.com.hipotecario.backend.servicio.api.transmit;

public enum JourneyTransmitEnum {

    /* HB */
    HB_ENROLAR_USUARIO("942a40b1-5b6d-425b-a1ae-f11d535b3e9e"),
    HB_RECUPERAR_CONTRASENA("BH_MIN_WEB_Recuperar_Contraseña_0113_V5"),
    HB_TRANSACCIONES("84a01889-2621-4fb4-b6e2-2645b97368e5"),
    HB_MODIFICACION_DATOS("b3ae9c40-ec6e-41ac-9f8f-ac050afa01c8"),
    HB_INICIO_SESION("BH_MAY_verifica_csm"),
    HB_MI_CUENTA_ESTA_EN_RIESGO("2111_BH_MIN_mi_cuenta_en_riesgo_2111_V02"),
    /* MB */
    MB_ENROLAR_SUBJ_DISPOSITIVO_CONFIANZA("BH_MIN_SUBJ_enroll_disp_confianza_0122_V4"),
    MB_ENROLAR_USUARIO("942a40b1-5b6d-425b-a1ae-f11d535b3e9e"),
    MB_ENROLAR_APP_DISPOSITIVO_CONFIANZA("BH_MIN_APP_enroll_disp_confianza_0122_V5"),
    MB_REGISTRO_SOFT_TOKEN("BH_MIN_APP_Registro_SofToken_0123_V3"),
    MB_ENROLAR_BIOMETRIA("0124_BH_MIN_APP_enroll_de_Biometria_0124_V2"),
    MB_INICIO_SESION("BH_MAY_verifica_csm"),
    MB_TRANSACCIONES("f398692b-e74f-40fa-b3c8-b814076aaa7a"),
    MB_MODIFICACION_DATOS("b3ae9c40-ec6e-41ac-9f8f-ac050afa01c8"),
    /* MODO */
    MODO_ENROLAR_APP("a9f4ffbb-7eb6-4afb-9f1e-93851cf34030"),
    MODO_ENROLAR_DESDE_APP("d732df4c-2edd-466f-b105-75839efba757"),
    MODO_INGRESA_DINERO("6d5c631e-6aa5-4152-99a7-ffdbef88d91a"),;

    public String descripcion;

    JourneyTransmitEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public static JourneyTransmitEnum codigo(String descripcion) {
        for (JourneyTransmitEnum e : values())
            if (e.getDescripcion().equals(descripcion))
                return e;
        throw new IllegalArgumentException(String.valueOf(descripcion));
    }

    public String getDescripcion() {
        return descripcion;
    }
}
