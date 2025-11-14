package ar.com.hipotecario.mobile.api.dto.modo;

import java.time.LocalDateTime;

public class AuditorTransferencia {

	private Integer id;

	private String idCobis;

	private LocalDateTime momento;

	private String idProceso;

	private String ip;

	private String canal;

	private String codigoError;

	private String descripcionError;

	private String tipo;

	private String cuentaOrigen;

	private String cuentaDestino;

	private String importe;

	private String moneda;

	private String concepto;

	private String cuentaPropia;

	private String servicioDomestico;

	private String especial;

	private String tarjetaDebito;

	private String transaccion;

	public AuditorTransferencia() {

	}

	public AuditorTransferencia(Integer id, String idCobis, LocalDateTime momento, String idProceso, String ip, String canal, String codigoError, String descripcionError, String tipo, String cuentaOrigen, String cuentaDestino, String importe, String moneda, String concepto, String cuentaPropia, String servicioDomestico, String especial, String tarjetaDebito, String transaccion) {
		super();
		this.id = id;
		this.idCobis = idCobis;
		this.momento = momento;
		this.idProceso = idProceso;
		this.ip = ip;
		this.canal = canal;
		this.codigoError = codigoError;
		this.descripcionError = descripcionError;
		this.tipo = tipo;
		this.cuentaOrigen = cuentaOrigen;
		this.cuentaDestino = cuentaDestino;
		this.importe = importe;
		this.moneda = moneda;
		this.concepto = concepto;
		this.cuentaPropia = cuentaPropia;
		this.servicioDomestico = servicioDomestico;
		this.especial = especial;
		this.tarjetaDebito = tarjetaDebito;
		this.transaccion = transaccion;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdCobis() {
		return idCobis;
	}

	public void setIdCobis(String idCobis) {
		this.idCobis = idCobis;
	}

	public LocalDateTime getMomento() {
		return momento;
	}

	public void setMomento(LocalDateTime momento) {
		this.momento = momento;
	}

	public String getIdProceso() {
		return idProceso;
	}

	public void setIdProceso(String idProceso) {
		this.idProceso = idProceso;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getCodigoError() {
		return codigoError;
	}

	public void setCodigoError(String codigoError) {
		this.codigoError = codigoError;
	}

	public String getDescripcionError() {
		return descripcionError;
	}

	public void setDescripcionError(String descripcionError) {
		this.descripcionError = descripcionError;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCuentaOrigen() {
		return cuentaOrigen;
	}

	public void setCuentaOrigen(String cuentaOrigen) {
		this.cuentaOrigen = cuentaOrigen;
	}

	public String getCuentaDestino() {
		return cuentaDestino;
	}

	public void setCuentaDestino(String cuentaDestino) {
		this.cuentaDestino = cuentaDestino;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getCuentaPropia() {
		return cuentaPropia;
	}

	public void setCuentaPropia(String cuentaPropia) {
		this.cuentaPropia = cuentaPropia;
	}

	public String getServicioDomestico() {
		return servicioDomestico;
	}

	public void setServicioDomestico(String servicioDomestico) {
		this.servicioDomestico = servicioDomestico;
	}

	public String getEspecial() {
		return especial;
	}

	public void setEspecial(String especial) {
		this.especial = especial;
	}

	public String getTarjetaDebito() {
		return tarjetaDebito;
	}

	public void setTarjetaDebito(String tarjetaDebito) {
		this.tarjetaDebito = tarjetaDebito;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

}
