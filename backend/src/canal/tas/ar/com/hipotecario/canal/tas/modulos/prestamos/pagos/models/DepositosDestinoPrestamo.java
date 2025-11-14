package ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models;

public class DepositosDestinoPrestamo {
    public Integer KioscoId;
    public Long DepositoId;
    public String CodigoCliente;
    public String TipoCliente;
    public String NumeroPrestamo;
    public String TipoPrestamo;
    public String IdPrestamo;


    public DepositosDestinoPrestamo(Integer kioscoId, Long depositoId, String codigoCliente, String tipoCliente,
            String numeroPrestamo,
            String tipoPrestamo, String idPrestamo) {
        KioscoId = kioscoId;
        DepositoId = depositoId;
        CodigoCliente = codigoCliente;
        TipoCliente = tipoCliente;
        NumeroPrestamo = numeroPrestamo;
        TipoPrestamo = tipoPrestamo;
        IdPrestamo = idPrestamo;
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

	public String getNumeroPrestamo() {
		return NumeroPrestamo;
	}

	public void setNumeroPrestamo(String numeroPrestamo) {
		NumeroPrestamo = numeroPrestamo;
	}

	public String getTipoPrestamo() {
		return TipoPrestamo;
	}

	public void setTipoPrestamo(String tipoPrestamo) {
		TipoPrestamo = tipoPrestamo;
	}

	public String getIdPrestamo() {
		return IdPrestamo;
	}

	public void setIdPrestamo(String idPrestamo) {
		IdPrestamo = idPrestamo;
	}

	

    

}
