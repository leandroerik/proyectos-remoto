package ar.com.hipotecario.canal.tas.shared.utils.models.enums;

public enum TASCodigoMonedaEnum {
    PESOS ("$"),
    DOLARES ("USD");
   

    private String codigoMoneda;

    TASCodigoMonedaEnum(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

   
    
    public String getCodigoMoneda() {
		return codigoMoneda;
	}



	public void setCodigoMoneda(String codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
	}


}
