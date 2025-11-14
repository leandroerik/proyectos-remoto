package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TasDepositoEfectivo extends ApiObjeto {

	private String cuenta;
	private Fecha fecha;
	private Integer lote;
	private String moneda;
	private BigDecimal monto;
	private Integer oficina;
	private String precinto;
	private String producto;
	private String tas;

	public TasDepositoEfectivo(
			String cuenta,
			Fecha fecha,
			Integer lote,
			String moneda,
			BigDecimal monto,
			Integer oficina,
			String precinto,
			String producto,
			String tas) {
		this.cuenta = cuenta;
		this.fecha = fecha;
		this.lote = lote;
		this.moneda = moneda;
		this.monto = monto;
		this.oficina = oficina;
		this.precinto = precinto;
		this.producto = producto;
		this.tas = tas;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public void setFecha(Fecha fecha) {
		this.fecha = fecha;
	}

	public void setLote(Integer lote) {
		this.lote = lote;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public void setOficina(Integer oficina) {
		this.oficina = oficina;
	}

	public void setPrecinto(String precinto) {
		this.precinto = precinto;
	}

	public void setProducto(String producto) {
		this.producto = producto;
	}

	public void setTas(String tas) {
		this.tas = tas;
	}

	public Fecha getFecha() {
		return fecha;
	}

	public Integer getLote() {
		return lote;
	}

	public String getMoneda() {
		return moneda;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public Integer getOficina() {
		return oficina;
	}

	public String getPrecinto() {
		return precinto;
	}

	public String getProducto() {
		return producto;
	}

	public String getTas() {
		return tas;
	}

	public static Objeto toObjeto(TasDepositoEfectivo deposito) {
		Objeto response = new Objeto();
		if (deposito != null) {
			response.add(deposito.objeto());
		}
		return response;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cuenta == null) ? 0 : cuenta.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((lote == null) ? 0 : lote.hashCode());
		result = prime * result + ((moneda == null) ? 0 : moneda.hashCode());
		result = prime * result + ((monto == null) ? 0 : monto.hashCode());
		result = prime * result + ((oficina == null) ? 0 : oficina.hashCode());
		result = prime * result + ((precinto == null) ? 0 : precinto.hashCode());
		result = prime * result + ((producto == null) ? 0 : producto.hashCode());
		result = prime * result + ((tas == null) ? 0 : tas.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TasDepositoEfectivo other = (TasDepositoEfectivo) obj;
		if (cuenta == null) {
			if (other.cuenta != null)
				return false;
		} else if (!cuenta.equals(other.cuenta))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (lote == null) {
			if (other.lote != null)
				return false;
		} else if (!lote.equals(other.lote))
			return false;
		if (moneda == null) {
			if (other.moneda != null)
				return false;
		} else if (!moneda.equals(other.moneda))
			return false;
		if (monto == null) {
			if (other.monto != null)
				return false;
		} else if (!monto.equals(other.monto))
			return false;
		if (oficina == null) {
			if (other.oficina != null)
				return false;
		} else if (!oficina.equals(other.oficina))
			return false;
		if (precinto == null) {
			if (other.precinto != null)
				return false;
		} else if (!precinto.equals(other.precinto))
			return false;
		if (producto == null) {
			if (other.producto != null)
				return false;
		} else if (!producto.equals(other.producto))
			return false;
		if (tas == null) {
			if (other.tas != null)
				return false;
		} else if (!tas.equals(other.tas))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TasDepositoEfectivo [cuenta=" + cuenta + ", fecha=" + fecha + ", lote=" + lote
				+ ", moneda=" + moneda + ", monto=" + monto + ", oficina=" + oficina + ", precinto=" + precinto
				+ ", producto="
				+ producto + ", tas=" + tas + "]";
	}
}
