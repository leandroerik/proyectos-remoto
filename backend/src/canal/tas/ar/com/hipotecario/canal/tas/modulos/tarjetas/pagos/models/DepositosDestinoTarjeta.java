package ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models;

public class DepositosDestinoTarjeta {
    public Integer KioscoId;
    public Long DepositoId;
    public String CodigoCliente;
    public String TipoCliente;
    public String NumeroTarjeta;
    public String TipoTarjeta;
    public String NumeroCuentaTarjeta;
    public String TipoTitularidad;


    public DepositosDestinoTarjeta(Integer kioscoId, Long depositoId, String codigoCliente, String tipoCliente,
            String numeroTarjeta,
            String tipoTarjeta, String numeroCuentaTarjeta, String tipoTitularidad) {
        KioscoId = kioscoId;
        DepositoId = depositoId;
        CodigoCliente = codigoCliente;
        TipoCliente = tipoCliente;
        NumeroTarjeta = numeroTarjeta;
        TipoTarjeta = tipoTarjeta;
        NumeroCuentaTarjeta = numeroCuentaTarjeta;
        TipoTitularidad = tipoTitularidad;
    }

    public Long getDepositoId() {
        return DepositoId;
    }

    public void setDepositoId(Long depositoId) {
        DepositoId = depositoId;
    }

    public String getCodigoCliente() {
        return CodigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        CodigoCliente = codigoCliente;
    }

	public Integer getKioscoId() {
		return KioscoId;
	}

	public void setKioscoId(Integer kioscoId) {
		KioscoId = kioscoId;
	}

	public String getTipoCliente() {
		return TipoCliente;
	}

	public void setTipoCliente(String tipoCliente) {
		TipoCliente = tipoCliente;
	}

	public String getNumeroTarjeta() {
		return NumeroTarjeta;
	}

	public void setNumeroTarjeta(String numeroTarjeta) {
		NumeroTarjeta = numeroTarjeta;
	}

	public String getTipoTarjeta() {
		return TipoTarjeta;
	}

	public void setTipoTarjeta(String tipoTarjeta) {
		TipoTarjeta = tipoTarjeta;
	}

	public String getNumeroCuentaTarjeta() {
		return NumeroCuentaTarjeta;
	}

	public void setNumeroCuentaTarjeta(String numeroCuentaTarjeta) {
		NumeroCuentaTarjeta = numeroCuentaTarjeta;
	}

	public String getTipoTitularidad() {
		return TipoTitularidad;
	}

	public void setTipoTitularidad(String tipoTitularidad) {
		TipoTitularidad = tipoTitularidad;
	}

    

}
