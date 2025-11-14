package ar.com.hipotecario.canal.tas.shared.utils.models.enums;

public enum TASEstadoDepositoEnum {
    ABORTADO ("P"),
    ERROR ("C"),
    ERROR_CIM ("L"),
    ERROR_FUNCIONAL ("F"),
    ERROR_RETENIDO ("B"),
    OK ("R"),
    REVERSAR ("A"),
    REVERSA_FALLIDA ("X"),;

    private String estadoError;

    TASEstadoDepositoEnum(String estadoError) {
        this.estadoError = estadoError;
    }

    public String getEstadoError() {
        return estadoError;
    }

    public void setEstadoError(String estadoError) {
        this.estadoError = estadoError;
    }
    
    public static String fromEstadoError(String estadoError) {
        for (TASEstadoDepositoEnum estado : TASEstadoDepositoEnum.values()) {
            if (estado.getEstadoError().equals(estadoError)) {
                return estado.getEstadoError();
            }
        }
        return null;
    }
}
