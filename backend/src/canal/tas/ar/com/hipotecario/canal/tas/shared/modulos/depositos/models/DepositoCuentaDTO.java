package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;

public class DepositoCuentaDTO extends SqlObjeto {

	public static class DepositosCuentaDTO extends SqlObjetos<DepositoCuentaDTO> {
	}

	// falta producto
	public String NumeroCuentaCOBIS;
	public String TipoCuenta;
	public Integer TasOriginaria;
	public Integer KioscoId;
	public Integer DepositoIdDB;
	public Integer DepositoId;
	public Fecha FechaDeposito;
	public String NumeroTicket;
	public Integer NumeroSobre;
	public String EstadoDeposito; // ok
	public Fecha FechaEstadoDeposito; // ok
	public String NumeroOperacionCanal; // ok
	public String NumeroOperacio; // ok
	public Integer Sucursal; // ok
	public Integer SucursalId; // ok
	public String TipoDocumento; // ok
	public String NumeroDocumento; // ok
	public String TipoDestino; // ok
	public String DestinoDescripcion; // OK
	public String Moneda; // ok
	public String NumeroCuentaDestino;
	public String TipoCuentaDestino;
	public String CodigoCliente;
	public String IdProductoCuenta;
	public String TipoCliente;
	public Object Valores;
	public Integer CantidadRetenidos;
	public Boolean EsInteligente;
	public String DepositoInteligente; // ok
	public String Precinto; // ok
	public Integer Lote; // ok
	public String ProcessId; // ok
	public String CodigoBanca; // ok
	public String DescripcionBanca; // ok
	public BigDecimal ImporteTotalEfectivo; // ok
	public String CodigoMoneda; // ok

	public Integer getDepositoId() {
		return DepositoId;
	}

	public void setDepositoId(Integer depositoId) {
		DepositoId = depositoId;
	}

	public Fecha getFechaDeposito() {
		return FechaDeposito;
	}

	public void setFechaDeposito(Fecha fechaDeposito) {
		FechaDeposito = fechaDeposito;
	}

	public String getEstadoDeposito() {
		return EstadoDeposito;
	}

	public void setEstadoDeposito(String estadoDeposito) {
		EstadoDeposito = estadoDeposito;
	}

	public Fecha getFechaEstadoDeposito() {
		return FechaEstadoDeposito;
	}

	public void setFechaEstadoDeposito(Fecha fechaEstadoDeposito) {
		FechaEstadoDeposito = fechaEstadoDeposito;
	}

	public String getNumeroOperacio() {
		return NumeroOperacio;
	}

	public void setNumeroOperacio(String numeroOperacio) {
		NumeroOperacio = numeroOperacio;
	}

	public Integer getSucursalId() {
		return SucursalId;
	}

	public void setSucursalId(Integer sucursalId) {
		SucursalId = sucursalId;
	}

	public Integer getCantidadRetenidos() {
		return CantidadRetenidos;
	}

	public void setCantidadRetenidos(Integer cantidadRetenidos) {
		CantidadRetenidos = cantidadRetenidos;
	}

	public String getDepositoInteligente() {
		return DepositoInteligente;
	}

	public void setDepositoInteligente(String depositoInteligente) {
		DepositoInteligente = depositoInteligente;
	}

	public BigDecimal getImporteTotalEfectivo() {
		return ImporteTotalEfectivo;
	}

	public void setImporteTotalEfectivo(BigDecimal importeTotalEfectivo) {
		ImporteTotalEfectivo = importeTotalEfectivo;
	}

	public String getCodigoMoneda() {
		return CodigoMoneda;
	}

	public void setCodigoMoneda(String codigoMoneda) {
		CodigoMoneda = codigoMoneda;
	}

	public String getNumeroCuentaCOBIS() {
		return NumeroCuentaCOBIS;
	}

	public void setNumeroCuentaCOBIS(String numeroCuentaCOBIS) {
		NumeroCuentaCOBIS = numeroCuentaCOBIS;
	}

	public String getTipoCuenta() {
		return TipoCuenta;
	}

	public void setTipoCuenta(String tipoCuenta) {
		TipoCuenta = tipoCuenta;
	}

	public Integer getTasOriginaria() {
		return TasOriginaria;
	}

	public void setTasOriginaria(Integer tasOriginaria) {
		TasOriginaria = tasOriginaria;
	}

	public Integer getDepositoIdDB() {
		return DepositoIdDB;
	}

	public void setDepositoIdDB(Integer depositoIdDB) {
		DepositoIdDB = depositoIdDB;
	}

	public String getNumeroTicket() {
		return NumeroTicket;
	}

	public void setNumeroTicket(String numeroTicket) {
		NumeroTicket = numeroTicket;
	}

	public Integer getNumeroSobre() {
		return NumeroSobre;
	}

	public void setNumeroSobre(Integer numeroSobre) {
		NumeroSobre = numeroSobre;
	}

	public String getNumeroOperacionCanal() {
		return NumeroOperacionCanal;
	}

	public void setNumeroOperacionCanal(String numeroOperacionCanal) {
		NumeroOperacionCanal = numeroOperacionCanal;
	}

	public Integer getSucursal() {
		return Sucursal;
	}

	public void setSucursal(Integer sucursal) {
		Sucursal = sucursal;
	}

	public String getTipoDocumento() {
		return TipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		TipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumento() {
		return NumeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		NumeroDocumento = numeroDocumento;
	}

	public String getTipoDestino() {
		return TipoDestino;
	}

	public void setTipoDestino(String tipoDestino) {
		TipoDestino = tipoDestino;
	}

	public String getDestinoDescripcion() {
		return DestinoDescripcion;
	}

	public void setDestinoDescripcion(String destinoDescripcion) {
		DestinoDescripcion = destinoDescripcion;
	}

	public String getMoneda() {
		return Moneda;
	}

	public void setMoneda(String moneda) {
		Moneda = moneda;
	}

	public String getNumeroCuentaDestino() {
		return NumeroCuentaDestino;
	}

	public void setNumeroCuentaDestino(String numeroCuentaDestino) {
		NumeroCuentaDestino = numeroCuentaDestino;
	}

	public String getTipoCuentaDestino() {
		return TipoCuentaDestino;
	}

	public void setTipoCuentaDestino(String tipoCuentaDestino) {
		TipoCuentaDestino = tipoCuentaDestino;
	}

	public String getCodigoCliente() {
		return CodigoCliente;
	}

	public void setCodigoCliente(String codigoCliente) {
		CodigoCliente = codigoCliente;
	}

	public String getIdProductoCuenta() {
		return IdProductoCuenta;
	}

	public void setIdProductoCuenta(String idProductoCuenta) {
		IdProductoCuenta = idProductoCuenta;
	}

	public String getTipoCliente() {
		return TipoCliente;
	}

	public void setTipoCliente(String tipoCliente) {
		TipoCliente = tipoCliente;
	}

	public Object getValores() {
		return Valores;
	}

	public void setValores(Object valores) {
		Valores = valores;
	}

	public Boolean getEsInteligente() {
		return EsInteligente;
	}

	public void setEsInteligente(Boolean esInteligente) {
		EsInteligente = esInteligente;
	}

	public String getPrecinto() {
		return Precinto;
	}

	public void setPrecinto(String precinto) {
		Precinto = precinto;
	}

	public Integer getLote() {
		return Lote;
	}

	public void setLote(Integer lote) {
		Lote = lote;
	}

	public String getProcessId() {
		return ProcessId;
	}

	public void setProcessId(String processId) {
		ProcessId = processId;
	}

	public String getCodigoBanca() {
		return CodigoBanca;
	}

	public void setCodigoBanca(String codigoBanca) {
		CodigoBanca = codigoBanca;
	}

	public String getDescripcionBanca() {
		return DescripcionBanca;
	}

	public void setDescripcionBanca(String descripcionBanca) {
		DescripcionBanca = descripcionBanca;
	}

	public Integer getKioscoId() {
		return KioscoId;
	}

	public void setKioscoId(Integer kioscoId) {
		KioscoId = kioscoId;
	}

}
