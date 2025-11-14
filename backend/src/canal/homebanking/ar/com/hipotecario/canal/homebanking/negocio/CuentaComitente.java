package ar.com.hipotecario.canal.homebanking.negocio;

import java.util.Date;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Texto;

public class CuentaComitente {

	/* ========== ATRIBUTOS ========== */
	private Objeto consolidada;

	/* ========== CONSTRUCTOR ========== */
	public CuentaComitente(Objeto cuentaComitente) {
		this.consolidada = cuentaComitente;
	}

	/* ========== GET ========== */
	public String id() {
		return consolidada.string("idProducto");
	}

	public String producto() {
		return "Cuenta Comitente";
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public Date fechaAlta() {
		return consolidada.date("fechaAlta", "yyyy-MM-dd");
	}

	public String titularidad() {
		return Texto.primeraMayuscula(consolidada.string("descTipoTitularidad"));
	}

//	{
//	    "tipoProducto" : "UNI",
//	    "numeroProducto" : "2-000109682",
//	    "idProducto" : "58031047",
//	    "sucursal" : 29,
//	    "descSucursal" : "SAN JUAN",
//	    "descEstado" : "VIGENTE",
//	    "estado" : "V",
//	    "fechaAlta" : "2013-02-26",
//	    "idDomicilio" : 2,
//	    "tipoTitularidad" : "T",
//	    "descTipoTitularidad" : "TITULAR",
//	    "adicionales" : false,
//	    "moneda" : 80,
//	    "descMoneda" : "PESOS"
//	  }
}
