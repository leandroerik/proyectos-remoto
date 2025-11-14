package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadosLiquidacionFCI {

	SOLICITADO("No Requiere Autorización"), 
	LIQUIDADA("Realizado"), 
	ESTADO_FINAL("Liquidada"), 
	PENDIENTE_LIQUIDACION("Pendiente de liquidación"),
	NO_REQUIERE_AUTORIZACION("No Requiere Autorización"), 
	AUTORIZADO("Autorizado"),
    PENDIENTE_DE_AUTORIZACION("Pendiente de Autorización");

	    private final String respuesta;

	    EnumEstadosLiquidacionFCI(String respuesta) {
	        this.respuesta = respuesta;
	    }

	    public String getRespuesta() {
	        return respuesta;
	    }

}
