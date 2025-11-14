package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASDatosBasicosCuenta extends ApiObjeto{

  private String nroCuenta;

  private String nroProducto;

  private String tipoCuenta;

  private String tipoTitular;

  private String idCategoria;

  

  public TASDatosBasicosCuenta() {
  }

  public String getNroCuenta() {
    return nroCuenta;
  }

  public void setNroCuenta(String nroCuenta) {
    this.nroCuenta = nroCuenta;
  }

  public String getNroProducto() {
    return nroProducto;
  }

  public void setNroProducto(String nroProducto) {
    this.nroProducto = nroProducto;
  }

  public String getTipoCuenta() {
    return tipoCuenta;
  }

  public void setTipoCuenta(String tipoCuenta) {
    this.tipoCuenta = tipoCuenta;
  }

  public String getTipoTitular() {
    return tipoTitular;
  }

  public void setTipoTitular(String tipoTitular) {
    this.tipoTitular = tipoTitular;
  }

  public String getIdCategoria() {
    return idCategoria;
  }

  public void setIdCategoria(String idCategoria) {
    this.idCategoria = idCategoria;
  }

  
}