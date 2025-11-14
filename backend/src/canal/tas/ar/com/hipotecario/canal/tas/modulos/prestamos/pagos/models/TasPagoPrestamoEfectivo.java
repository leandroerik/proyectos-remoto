package ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TasPagoPrestamoEfectivo extends ApiObjeto {

	private String nroPrestamo;
	private Fecha fecha;
	private String hora;
	private String lote;
	private BigDecimal importe;
	private String sucursal;
	private String precinto;
	private String tas;
	


	public TasPagoPrestamoEfectivo(
			String nroPrestamo,
			Fecha fecha,
			String hora,
			String lote,
			BigDecimal importe,
			String sucursal,
			String precinto,
			String tas) {
		this.nroPrestamo = nroPrestamo;
		this.fecha = fecha;
		this.hora = hora;
		this.lote = lote;
		this.importe = importe;
		this.sucursal = sucursal;
		this.precinto = precinto;
		this.tas = tas;
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




	




	public BigDecimal getImporte() {
		return importe;
	}




	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}




	



	public String getTas() {
		return tas;
	}




	public void setTas(String tas) {
		this.tas = tas;
	}








	




	public String getNroPrestamo() {
		return nroPrestamo;
	}








	public void setNroPrestamo(String nroPrestamo) {
		this.nroPrestamo = nroPrestamo;
	}








	public String getHora() {
		return hora;
	}








	public void setHora(String hora) {
		this.hora = hora;
	}








	public String getSucursal() {
		return sucursal;
	}








	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}








	public String getPrecinto() {
		return precinto;
	}








	public void setPrecinto(String precinto) {
		this.precinto = precinto;
	}








	public static Objeto toObjeto(TasPagoPrestamoEfectivo deposito) {
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
		result = prime * result + ((nroPrestamo == null) ? 0 : nroPrestamo.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((hora == null) ? 0 : hora.hashCode());
		result = prime * result + ((lote == null) ? 0 : lote.hashCode());
		result = prime * result + ((importe == null) ? 0 : importe.hashCode());
		result = prime * result + ((sucursal == null) ? 0 : sucursal.hashCode());
		result = prime * result + ((precinto == null) ? 0 : precinto.hashCode());
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
		TasPagoPrestamoEfectivo other = (TasPagoPrestamoEfectivo) obj;
		if (nroPrestamo == null) {
			if (other.nroPrestamo != null)
				return false;
		} else if (!nroPrestamo.equals(other.nroPrestamo))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (hora == null) {
			if (other.hora != null)
				return false;
		} else if (!hora.equals(other.hora))
			return false;
		if (lote == null) {
			if (other.lote != null)
				return false;
		} else if (!lote.equals(other.lote))
			return false;
		if (importe == null) {
			if (other.importe != null)
				return false;
		} else if (!importe.equals(other.importe))
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		if (precinto == null) {
			if (other.precinto != null)
				return false;
		} else if (!precinto.equals(other.precinto))
			return false;
		return true;
		
	}

	@Override
	public String toString() {
		return "TasDepositoEfectivo [nroPrestamo=" + nroPrestamo + ", fecha=" + fecha + ", hora=" + hora +", lote=" + lote
				 + ", importe=" + importe + ", sucursal=" + sucursal + ", precinto=" + precinto
				 + "]";
	}
}
