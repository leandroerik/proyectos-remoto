package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.conector.sql.SqlObjeto;

public class DepositoValores extends SqlObjeto {
	public Integer KioscoId;
	public Integer DepositoId;
	public Integer Orden;
	public String TipoValor;
	public BigDecimal Importe;
	public Long Cantidad;

	public Integer getKioscoId() {
		return KioscoId;
	}

	public void setKioscoId(Integer kioscoId) {
		KioscoId = kioscoId;
	}

	public Integer getDepositoId() {
		return DepositoId;
	}

	public void setDepositoId(Integer depositoId) {
		DepositoId = depositoId;
	}

	public Integer getOrden() {
		return Orden;
	}

	public void setOrden(Integer orden) {
		Orden = orden;
	}

	public String getTipoValor() {
		return TipoValor;
	}

	public void setTipoValor(String tipoValor) {
		TipoValor = tipoValor;
	}

	public BigDecimal getImporte() {
		return Importe;
	}

	public void setImporte(BigDecimal importe) {
		Importe = importe;
	}

	public Long getCantidad() {
		return Cantidad;
	}

	public void setCantidad(Long cantidad) {
		Cantidad = cantidad;
	}

}
