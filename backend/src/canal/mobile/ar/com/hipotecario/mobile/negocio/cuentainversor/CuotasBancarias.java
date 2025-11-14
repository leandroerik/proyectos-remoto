package ar.com.hipotecario.mobile.negocio.cuentainversor;

public class CuotasBancarias {

	private String alias;
	private Banco banco;
	private String cbu;
	private String cuitTitular;
	private String descripcion;
	private String fechaApertura;
	private String iban;
	private String idCuentaBancaria;
	private String idCuentaBancariaSec;
	private Moneda moneda;
	private String numeroCuenta;
	private String numeroSucursal;
	private String requiereFirmaConjunta;
	private String swift;
	private TipoCuentaBancaria tipoCuentaBancaria;

	public CuotasBancarias() {
	}

	public CuotasBancarias(String alias, Banco banco, String cbu, String cuitTitular, String descripcion, String fechaApertura, String iban, String idCuentaBancaria, String idCuentaBancariaSec, Moneda moneda, String numeroCuenta, String numeroSucursal, String requiereFirmaConjunta, String swift, TipoCuentaBancaria tipoCuentaBancaria) {
		this.alias = alias;
		this.banco = banco;
		this.cbu = cbu;
		this.cuitTitular = cuitTitular;
		this.descripcion = descripcion;
		this.fechaApertura = fechaApertura;
		this.iban = iban;
		this.idCuentaBancaria = idCuentaBancaria;
		this.idCuentaBancariaSec = idCuentaBancariaSec;
		this.moneda = moneda;
		this.numeroCuenta = numeroCuenta;
		this.numeroSucursal = numeroSucursal;
		this.requiereFirmaConjunta = requiereFirmaConjunta;
		this.swift = swift;
		this.tipoCuentaBancaria = tipoCuentaBancaria;
	}

	public String getAlias() {
		return alias != null ? alias : "";
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public String getCbu() {
		return cbu != null ? cbu : "";
	}

	public void setCbu(String cbu) {
		this.cbu = cbu;
	}

	public String getCuitTitular() {
		return cuitTitular != null ? cuitTitular : "";
	}

	public void setCuitTitular(String cuitTitular) {
		this.cuitTitular = cuitTitular;
	}

	public String getDescripcion() {
		return descripcion != null ? descripcion : "";
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFechaApertura() {
		return fechaApertura;
	}

	public void setFechaApertura(String fechaApertura) {
		this.fechaApertura = fechaApertura;
	}

	public String getIban() {
		return iban != null ? iban : "";
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public String getIdCuentaBancaria() {
		return idCuentaBancaria != null ? idCuentaBancaria : "";
	}

	public void setIdCuentaBancaria(String idCuentaBancaria) {
		this.idCuentaBancaria = idCuentaBancaria;
	}

	public String getIdCuentaBancariaSec() {
		return idCuentaBancariaSec != null ? idCuentaBancariaSec : "";
	}

	public void setIdCuentaBancariaSec(String idCuentaBancariaSec) {
		this.idCuentaBancariaSec = idCuentaBancariaSec;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public String getNumeroCuenta() {
		return numeroCuenta != null ? numeroCuenta : "";
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public String getNumeroSucursal() {
		return numeroSucursal != null ? numeroSucursal : "";
	}

	public void setNumeroSucursal(String numeroSucursal) {
		this.numeroSucursal = numeroSucursal;
	}

	public String getRequiereFirmaConjunta() {
		return requiereFirmaConjunta != null ? requiereFirmaConjunta : "";
	}

	public void setRequiereFirmaConjunta(String requiereFirmaConjunta) {
		this.requiereFirmaConjunta = requiereFirmaConjunta;
	}

	public String getSwift() {
		return swift != null ? swift : "";
	}

	public void setSwift(String swift) {
		this.swift = swift;
	}

	public TipoCuentaBancaria getTipoCuentaBancaria() {
		return tipoCuentaBancaria;
	}

	public void setTipoCuentaBancaria(TipoCuentaBancaria tipoCuentaBancaria) {
		this.tipoCuentaBancaria = tipoCuentaBancaria;
	}
}
