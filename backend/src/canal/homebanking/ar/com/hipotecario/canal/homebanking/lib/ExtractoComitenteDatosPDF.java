package ar.com.hipotecario.canal.homebanking.lib;

public class ExtractoComitenteDatosPDF {

	private String documento;
	private String cuil;
	private String domicilio;
	private String codPostal;
	private String telefonos;
	private String localidad;

//Boleto Compra-Venta
	private String fechaOrden;
	private String horaOrden;
	private String fechaHora;
	private String fechaConcertacion;
	private String sucursal;
	private String contraparte;
	private String numerosOrden;
	private String tipoBoleto;
	private String tipoAccion;
	private String nroBoleto;
	private String nroMinuta;
	private String fechaVencimiento;
	private String cantidadValorNominal;
	private String cantidadValorResidual;
	private String especie;
	private String mercado;
	private String precio;
	private String derechos;
	private String comision;
	private String totalML;
	private String totalME;
	private String totalLetrasML;
	private String totalLetrasME;
	private String ivaRI;
	private String ivaRNI;
	private String aranceles;
	private String cuentaLiquidacionML;
	private String cuentaLiquidacionME;
	private String plazo;
	private String refMercado;
	private String cuentaNro;
	private String nombreRazon;
	private String bruto;
	private String fechaPago;
	private String moneda;
	// LICU
	private String amortizacion;
	private String renta;

	private String cantidadResidualActual;
	private String amortizacionMonto;
	private String rentaMonto;
	private String totalMonto;
	private String totalLetra;
	private String cuentaLiquidacion;
	private String dividendoEnAcciones;
	private String dividendoEnEfectivo;

	public String getFechaOrden() {
		return ((fechaOrden == null) ? "" : fechaOrden);
	}

	public void setFechaOrden(String fechaOrden) {
		this.fechaOrden = fechaOrden;
	}

	public String getHoraOrden() {
		return ((horaOrden == null) ? "" : horaOrden);
	}

	public void setHoraOrden(String horaOrden) {
		this.horaOrden = horaOrden;
	}

	public String getSucursal() {
		return ((sucursal == null) ? "" : sucursal);
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public String getContraparte() {
		return ((contraparte == null) ? "" : contraparte);
	}

	public void setContraparte(String contraparte) {
		this.contraparte = contraparte;
	}

	public String getNumerosOrden() {
		return ((numerosOrden == null) ? "" : numerosOrden);
	}

	public void setNumerosOrden(String numerosOrden) {
		this.numerosOrden = numerosOrden;
	}

	public String getTipoBoleto() {
		return ((tipoBoleto == null) ? "" : tipoBoleto);
	}

	public void setTipoBoleto(String tipoBoleto) {
		this.tipoBoleto = tipoBoleto;
	}

	public String getTipoAccion() {
		return ((tipoAccion == null) ? "" : tipoAccion);
	}

	public void setTipoAccion(String tipoAccion) {
		this.tipoAccion = tipoAccion;
	}

	public String getNroBoleto() {
		return ((nroBoleto == null) ? "" : nroBoleto);
	}

	public void setNroBoleto(String nroBoleto) {
		this.nroBoleto = nroBoleto;
	}

	public String getNroMinuta() {
		return ((nroMinuta == null) ? "" : nroMinuta);
	}

	public void setNroMinuta(String nroMinuta) {
		this.nroMinuta = nroMinuta;
	}

	public String getCantidadValorResidual() {
		return ((cantidadValorResidual == null) ? "" : cantidadValorResidual);
	}

	public void setCantidadValorResidual(String cantidadValorResidual) {
		this.cantidadValorResidual = cantidadValorResidual;
	}

	public String getCantidadValorNominal() {
		return ((cantidadValorNominal == null) ? "" : cantidadValorNominal);
	}

	public void setCantidadValorNominal(String cantidadValorNominal) {
		this.cantidadValorNominal = cantidadValorNominal;
	}

	public String getEspecie() {
		return ((especie == null) ? "" : especie);
	}

	public void setEspecie(String especie) {
		this.especie = especie;
	}

	public String getMercado() {
		return ((mercado == null) ? "" : mercado);
	}

	public void setMercado(String mercado) {
		this.mercado = mercado;
	}

	public String getPrecio() {
		return ((precio == null) ? "" : precio);
	}

	public void setPrecio(String precio) {
		this.precio = precio;
	}

	public String getDerechos() {
		return ((derechos == null) ? "" : derechos);
	}

	public void setDerechos(String derechos) {
		this.derechos = derechos;
	}

	public String getComision() {
		return ((comision == null) ? "" : comision);
	}

	public void setComision(String comision) {
		this.comision = comision;
	}

	public String getTotalML() {
		return ((totalML == null) ? "" : totalML);
	}

	public void setTotalML(String totalML) {
		this.totalML = totalML;
	}

	public String getTotalME() {
		return ((totalME == null) ? "" : totalME);
	}

	public void setTotalME(String totalME) {
		this.totalME = totalME;
	}

	public String getTotalLetrasML() {
		return ((totalLetrasML == null) ? "" : totalLetrasML);
	}

	public void setTotalLetrasML(String totalLetrasML) {
		this.totalLetrasML = totalLetrasML;
	}

	public String getTotalLetrasME() {
		return ((totalLetrasME == null) ? "" : totalLetrasME);
	}

	public void setTotalLetrasME(String totalLetrasME) {
		this.totalLetrasME = totalLetrasME;
	}

	public String getIvaRI() {
		return ((ivaRI == null) ? "" : ivaRI);
	}

	public void setIvaRI(String ivaRI) {
		this.ivaRI = ivaRI;
	}

	public String getIvaRNI() {
		return ((ivaRNI == null) ? "" : ivaRNI);
	}

	public void setIvaRNI(String ivaRNI) {
		this.ivaRNI = ivaRNI;
	}

	public String getAranceles() {
		return ((aranceles == null) ? "" : aranceles);
	}

	public void setAranceles(String aranceles) {
		this.aranceles = aranceles;
	}

	public String getCuentaLiquidacionML() {
		return ((cuentaLiquidacionML == null) ? "" : cuentaLiquidacionML);
	}

	public void setCuentaLiquidacionML(String cuentaLiquidacionML) {
		this.cuentaLiquidacionML = cuentaLiquidacionML;
	}

	public String getCuentaLiquidacionME() {
		return ((cuentaLiquidacionME == null) ? "" : cuentaLiquidacionME);
	}

	public void setCuentaLiquidacionME(String cuentaLiquidacionME) {
		this.cuentaLiquidacionME = cuentaLiquidacionME;
	}

	public String getPlazo() {
		return ((plazo == null) ? "" : plazo);
	}

	public void setPlazo(String plazo) {
		this.plazo = plazo;
	}

	public String getRefMercado() {
		return ((refMercado == null) ? "" : refMercado);
	}

	public void setRefMercado(String refMercado) {
		this.refMercado = refMercado;
	}

	public String getCuentaNro() {
		return ((cuentaNro == null) ? "" : cuentaNro);
	}

	public void setCuentaNro(String cuentaNro) {
		this.cuentaNro = cuentaNro;
	}

	public String getAmortizacion() {
		return ((amortizacion == null) ? "" : amortizacion);
	}

	public void setAmortizacion(String amortizacion) {
		this.amortizacion = amortizacion;
	}

	public String getRenta() {
		return ((renta == null) ? "" : renta);
	}

	public void setRenta(String renta) {
		this.renta = renta;
	}

	public String getCantidadResidualActual() {
		return ((cantidadResidualActual == null) ? "" : cantidadResidualActual);
	}

	public void setCantidadResidualActual(String cantidadResidualActual) {
		this.cantidadResidualActual = cantidadResidualActual;
	}

	public String getAmortizacionMonto() {
		return ((amortizacionMonto == null) ? "" : amortizacionMonto);
	}

	public void setAmortizacionMonto(String amortizacionMonto) {
		this.amortizacionMonto = amortizacionMonto;
	}

	public String getRentaMonto() {
		return ((rentaMonto == null) ? "" : rentaMonto);
	}

	public void setRentaMonto(String rentaMonto) {
		this.rentaMonto = rentaMonto;
	}

	public String getTotalLetra() {
		return ((totalLetra == null) ? "" : totalLetra);
	}

	public void setTotalLetra(String totalLetra) {
		this.totalLetra = totalLetra;
	}

	public String getCuentaLiquidacion() {
		return ((cuentaLiquidacion == null) ? "" : cuentaLiquidacion);
	}

	public void setCuentaLiquidacion(String cuentaLiquidacion) {
		this.cuentaLiquidacion = cuentaLiquidacion;
	}

	public String getTotalMonto() {
		return ((totalMonto == null) ? "" : totalMonto);
	}

	public void setTotalMonto(String totalMonto) {
		this.totalMonto = totalMonto;
	}

	public String getFechaHora() {
		return ((fechaHora == null) ? "" : fechaHora);
	}

	public void setFechaHora(String fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getNombreRazon() {
		return ((nombreRazon == null) ? "" : nombreRazon);
	}

	public void setNombreRazon(String nombreRazon) {
		this.nombreRazon = nombreRazon;
	}

	public String getFechaConcertacion() {
		return ((fechaConcertacion == null) ? "" : fechaConcertacion);
	}

	public void setFechaConcertacion(String fechaConcertacion) {
		this.fechaConcertacion = fechaConcertacion;
	}

	public String getDocumento() {
		return ((documento == null) ? "" : documento);
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getCuil() {
		return ((cuil == null) ? "" : cuil);
	}

	public void setCuil(String cuil) {
		this.cuil = cuil;
	}

	public String getDomicilio() {
		return ((domicilio == null) ? "" : domicilio);
	}

	public void setDomicilio(String domicilio) {
		this.domicilio = domicilio;
	}

	public String getCodPostal() {
		return ((codPostal == null) ? "" : codPostal);
	}

	public void setCodPostal(String codPostal) {
		this.codPostal = codPostal;
	}

	public String getTelefonos() {
		return ((telefonos == null) ? "" : telefonos);
	}

	public void setTelefonos(String telefonos) {
		this.telefonos = telefonos;
	}

	public String getLocalidad() {
		return ((localidad == null) ? "" : localidad);
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getFechaVencimiento() {
		return ((fechaVencimiento == null) ? "" : fechaVencimiento);
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getBruto() {
		return ((bruto == null) ? "" : bruto);
	}

	public void setBruto(String bruto) {
		this.bruto = bruto;
	}

	public String getFechaPago() {
		return ((fechaPago == null) ? "" : fechaPago);
	}

	public void setFechaPago(String fechaPago) {
		this.fechaPago = fechaPago;
	}

	public String getMoneda() {
		return ((moneda == null) ? "" : moneda);
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	/**
	 * @return the dividendoEnAcciones
	 */
	public String getDividendoEnAcciones() {
		return ((dividendoEnAcciones == null) ? "" : dividendoEnAcciones);
	}

	/**
	 * @param dividendoEnAcciones the dividendoEnAcciones to set
	 */
	public void setDividendoEnAcciones(String dividendoEnAcciones) {
		this.dividendoEnAcciones = dividendoEnAcciones;
	}

	public String getDividendoEnEfectivo() {
		return ((dividendoEnEfectivo == null) ? "" : dividendoEnEfectivo);
	}

	public void setDividendoEnEfectivo(String dividendoEnEfectivo) {
		this.dividendoEnEfectivo = dividendoEnEfectivo;
	}
}
