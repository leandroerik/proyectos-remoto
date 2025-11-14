package ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TasPagoTarjetaEfectivo extends ApiObjeto {

	private String cuentaTarjeta;
	private Fecha fecha;
	private String lote;
	private String idMoneda;
	private BigDecimal importe;
	private String oficina;
	private String codigoPrecinto;
	private String tas;
	private String tipoProducto;
	private String tipoTarjeta;


	public TasPagoTarjetaEfectivo(
			String cuentaTarjeta,
			Fecha fecha,
			String lote,
			String idMoneda,
			BigDecimal importe,
			String oficina,
			String codigoPrecinto,
			String tas,
			String tipoProducto, 
			String tipoTarjeta) {
		this.cuentaTarjeta = cuentaTarjeta;
		this.fecha = fecha;
		this.lote = lote;
		this.idMoneda = idMoneda;
		this.importe = importe;
		this.oficina = oficina;
		this.codigoPrecinto = codigoPrecinto;
		this.tas = tas;
		this.tipoProducto = tipoProducto;
		this.tipoTarjeta  = tipoTarjeta;
	}
	
	
	
	
	public String getCuentaTarjeta() {
		return cuentaTarjeta;
	}




	public void setCuentaTarjeta(String cuentaTarjeta) {
		this.cuentaTarjeta = cuentaTarjeta;
	}




	public Fecha getFecha() {
		return fecha;
	}




	public void setFecha(Fecha fecha) {
		this.fecha = fecha;
	}




	public String getLote() {
		return lote;
	}




	public void setLote(String lote) {
		this.lote = lote;
	}




	public String getIdMoneda() {
		return idMoneda;
	}




	public void setIdMoneda(String idMoneda) {
		this.idMoneda = idMoneda;
	}




	public BigDecimal getImporte() {
		return importe;
	}




	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}




	public String getOficina() {
		return oficina;
	}




	public void setOficina(String oficina) {
		this.oficina = oficina;
	}




	public String getCodigoPrecinto() {
		return codigoPrecinto;
	}




	public void setCodigoPrecinto(String codigoPrecinto) {
		this.codigoPrecinto = codigoPrecinto;
	}




	public String getTas() {
		return tas;
	}




	public void setTas(String tas) {
		this.tas = tas;
	}




	public String getTipoProducto() {
		return tipoProducto;
	}




	public void setTipoProducto(String tipoProducto) {
		this.tipoProducto = tipoProducto;
	}




	public String getTipoTarjeta() {
		return tipoTarjeta;
	}




	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}




	public static Objeto toObjeto(TasPagoTarjetaEfectivo deposito) {
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
		result = prime * result + ((cuentaTarjeta == null) ? 0 : cuentaTarjeta.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((lote == null) ? 0 : lote.hashCode());
		result = prime * result + ((idMoneda == null) ? 0 : idMoneda.hashCode());
		result = prime * result + ((importe == null) ? 0 : importe.hashCode());
		result = prime * result + ((oficina == null) ? 0 : oficina.hashCode());
		result = prime * result + ((codigoPrecinto == null) ? 0 : codigoPrecinto.hashCode());
		result = prime * result + ((tas == null) ? 0 : tas.hashCode());
		result = prime * result + ((tipoProducto == null) ? 0 : tipoProducto.hashCode());
		result = prime * result + ((tipoTarjeta == null) ? 0 : tipoTarjeta.hashCode());


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
		TasPagoTarjetaEfectivo other = (TasPagoTarjetaEfectivo) obj;
		if (cuentaTarjeta == null) {
			if (other.cuentaTarjeta != null)
				return false;
		} else if (!cuentaTarjeta.equals(other.cuentaTarjeta))
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
		if (idMoneda == null) {
			if (other.idMoneda != null)
				return false;
		} else if (!idMoneda.equals(other.idMoneda))
			return false;
		if (importe == null) {
			if (other.importe != null)
				return false;
		} else if (!importe.equals(other.importe))
			return false;
		if (oficina == null) {
			if (other.oficina != null)
				return false;
		} else if (!oficina.equals(other.oficina))
			return false;
		if (codigoPrecinto == null) {
			if (other.codigoPrecinto != null)
				return false;
		} else if (!codigoPrecinto.equals(other.codigoPrecinto))
			return false;
		if (tipoProducto == null) {
			if (other.tipoProducto != null)
				return false;
		} else if (!tipoProducto.equals(other.tipoProducto))
			return false;
		if (tas == null) {
			if (other.tas != null)
				return false;
		} else if (!tas.equals(other.tas))
			return false;
		if (tipoTarjeta == null) {
			if (other.tipoTarjeta != null)
				return false;
		} else if (!tipoTarjeta.equals(other.tipoTarjeta))
			return false;
		return true;
		
	}

	@Override
	public String toString() {
		return "TasDepositoEfectivo [cuentaTarjeta=" + cuentaTarjeta + ", fecha=" + fecha + ", lote=" + lote
				+ ", idMoneda=" + idMoneda + ", importe=" + importe + ", oficina=" + oficina + ", codigoPrecinto=" + codigoPrecinto
				+ ", tipoProducto="
				+ tipoProducto + ", tas=" + tas + ", tipoTarjeta=" + tipoTarjeta + "]";
	}
}
