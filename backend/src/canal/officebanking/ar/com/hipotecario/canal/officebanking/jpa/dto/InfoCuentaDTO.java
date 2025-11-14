package ar.com.hipotecario.canal.officebanking.jpa.dto;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;

public class InfoCuentaDTO {

	public CuentaCoelsa cuenta;
	public List<MonedaOB> monedas = new ArrayList<MonedaOB>();
	public String numero;


	public CuentaCoelsa  linktocoelsa(CuentaLink cuentaLink){
		if(this.cuenta==null){
			this.cuenta = new CuentaCoelsa();
		}
		this.cuenta.cbu=cuentaLink.cbu;
		this.cuenta.ctaActiva=true;
		this.cuenta.nombreTitular=cuentaLink.titulares.get(0).denominacion;
		this.cuenta.nuevoAlias="";
		this.cuenta.cuit=cuentaLink.titulares.get(0).idTributario;
		this.cuenta.nroBco=cuentaLink.cbu.substring(0,3);
		this.cuenta.tipoCuenta=cuentaLink.tipoProducto;
		return  cuenta;
	}

}