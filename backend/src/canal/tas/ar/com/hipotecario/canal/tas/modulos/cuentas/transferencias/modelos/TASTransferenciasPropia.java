package ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.modelos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASTransferenciasPropia extends ApiObjeto{

  private String cuentaDestino;
	private String cuentaOrigen;
	private String idCliente;
	private Integer idMoneda;
	private Integer idMonedaDestino;
	private Integer idMonedaOrigen;
	private BigDecimal importe;
	private Boolean modoSimulacion;
	private Boolean reverso;
	private String tipoCuentaDestino;
	private String tipoCuentaOrigen;
	private Integer transaccion;
	private Boolean mismoTitular;
	private Boolean ddjjCompraventa;
	private BigDecimal cotizacion;
	private BigDecimal importePesos;
	private BigDecimal importeDivisa;
	private BigDecimal tasa;
	private Boolean efectivo;
	private String identificacionPersona;
	private BigDecimal montoEnDivisa;
	private Integer paisDocumento;
  private String concepto;
  private String idCuenta;

  private BigDecimal importeDestino;
  private boolean cuentaPropia;
  private boolean inmediata;
  private boolean especial;
  private Objeto error;
  
  public TASTransferenciasPropia() {
  }

  public String getCuentaDestino() {
    return cuentaDestino;
  }
  public void setCuentaDestino(String cuentaDestino) {
    this.cuentaDestino = cuentaDestino;
  }
  public String getCuentaOrigen() {
    return cuentaOrigen;
  }
  public void setCuentaOrigen(String cuentaOrigen) {
    this.cuentaOrigen = cuentaOrigen;
  }
  public String getIdCliente() {
    return idCliente;
  }
  public void setIdCliente(String idCliente) {
    this.idCliente = idCliente;
  }
  public Integer getIdMoneda() {
    return idMoneda;
  }
  public void setIdMoneda(Integer idMoneda) {
    this.idMoneda = idMoneda;
  }
  public Integer getIdMonedaDestino() {
    return idMonedaDestino;
  }
  public void setIdMonedaDestino(Integer idMonedaDestino) {
    this.idMonedaDestino = idMonedaDestino;
  }
  public Integer getIdMonedaOrigen() {
    return idMonedaOrigen;
  }
  public void setIdMonedaOrigen(Integer idMonedaOrigen) {
    this.idMonedaOrigen = idMonedaOrigen;
  }
  public BigDecimal getImporte() {
    return importe;
  }
  public void setImporte(BigDecimal importe) {
    this.importe = importe;
  }
  public Boolean getModoSimulacion() {
    return modoSimulacion;
  }
  public void setModoSimulacion(Boolean modoSimulacion) {
    this.modoSimulacion = modoSimulacion;
  }
  public Boolean getReverso() {
    return reverso;
  }
  public void setReverso(Boolean reverso) {
    this.reverso = reverso;
  }
  public String getTipoCuentaDestino() {
    return tipoCuentaDestino;
  }
  public void setTipoCuentaDestino(String tipoCuentaDestino) {
    this.tipoCuentaDestino = tipoCuentaDestino;
  }
  public String getTipoCuentaOrigen() {
    return tipoCuentaOrigen;
  }
  public void setTipoCuentaOrigen(String tipoCuentaOrigen) {
    this.tipoCuentaOrigen = tipoCuentaOrigen;
  }
  public Integer getTransaccion() {
    return transaccion;
  }
  public void setTransaccion(Integer transaccion) {
    this.transaccion = transaccion;
  }
  public Boolean getMismoTitular() {
    return mismoTitular;
  }
  public void setMismoTitular(Boolean mismoTitular) {
    this.mismoTitular = mismoTitular;
  }
  public Boolean getDdjjCompraventa() {
    return ddjjCompraventa;
  }
  public void setDdjjCompraventa(Boolean ddjjCompraventa) {
    this.ddjjCompraventa = ddjjCompraventa;
  }
  public BigDecimal getCotizacion() {
    return cotizacion;
  }
  public void setCotizacion(BigDecimal cotizacion) {
    this.cotizacion = cotizacion;
  }
  public BigDecimal getImportePesos() {
    return importePesos;
  }
  public void setImportePesos(BigDecimal importePesos) {
    this.importePesos = importePesos;
  }
  public BigDecimal getImporteDivisa() {
    return importeDivisa;
  }
  public void setImporteDivisa(BigDecimal importeDivisa) {
    this.importeDivisa = importeDivisa;
  }
  public BigDecimal getTasa() {
    return tasa;
  }
  public void setTasa(BigDecimal tasa) {
    this.tasa = tasa;
  }
  public Boolean getEfectivo() {
    return efectivo;
  }
  public void setEfectivo(Boolean efectivo) {
    this.efectivo = efectivo;
  }
  public String getIdentificacionPersona() {
    return identificacionPersona;
  }
  public void setIdentificacionPersona(String identificacionPersona) {
    this.identificacionPersona = identificacionPersona;
  }
  public BigDecimal getMontoEnDivisa() {
    return montoEnDivisa;
  }
  public void setMontoEnDivisa(BigDecimal montoEnDivisa) {
    this.montoEnDivisa = montoEnDivisa;
  }
  public Integer getPaisDocumento() {
    return paisDocumento;
  }
  public void setPaisDocumento(Integer paisDocumento) {
    this.paisDocumento = paisDocumento;
  }

  public String getIdCuenta() {
    return idCuenta;
  }

  public void setIdCuenta(String idCuenta) {
    this.idCuenta = idCuenta;
  }

  public boolean isCuentaPropia() {
    return cuentaPropia;
  }

  public void setCuentaPropia(boolean cuentaPropia) {
    this.cuentaPropia = cuentaPropia;
  }

  public boolean isInmediata() {
    return inmediata;
  }

  public void setInmediata(boolean inmediata) {
    this.inmediata = inmediata;
  }

  public boolean isEspecial() {
    return especial;
  }

  public void setEspecial(boolean especial) {
    this.especial = especial;
  }

  public Objeto getError() {
    return error;
  }

  public void setError(Objeto error) {
    this.error = error;
  }

  public String getConcepto() {
    return concepto;
  }

  public void setConcepto(String concepto) {
    this.concepto = concepto;
  }

  public BigDecimal getImporteDestino() {
    return importeDestino;
  }

  public void setImporteDestino(BigDecimal importeDestino) {
    this.importeDestino = importeDestino;
  }
}
