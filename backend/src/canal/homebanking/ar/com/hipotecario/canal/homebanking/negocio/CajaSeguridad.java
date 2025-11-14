package ar.com.hipotecario.canal.homebanking.negocio;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Texto;

public class CajaSeguridad {

	/* ========== ATRIBUTOS ========== */
	private Objeto consolidada;

	/* ========== CONSTRUCTOR ========== */
	public CajaSeguridad(Objeto consolidada) {
		this.consolidada = consolidada;
	}

	/* ========== GET ========== */
	public String id() {
		return consolidada.string("idProducto");
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public String numeroEnmascarado() {
		String numeroFormateado = "";
		String numero = consolidada.string("numeroProducto");
		if (numero != null && numero.length() == 15) {
			numeroFormateado += "XXX-";
			numeroFormateado += numero.substring(11, 15);
		} else if (numero != null && numero.length() > 4) {
			numeroFormateado += "XXX-";
			numeroFormateado += numero.substring(numero.length() - 4);
		}
		return numeroFormateado;
	}

	public String producto() {
		return "Caja de Seguridad";
	}

	public String titularidad() {
		String titularidad = null;
		titularidad = "T".equals(consolidada.string("tipoTitularidad")) ? "Titular" : titularidad;
		titularidad = "A".equals(consolidada.string("tipoTitularidad")) ? "Autorizado" : titularidad;
		return titularidad != null ? titularidad : Texto.primeraMayuscula(consolidada.string("descTipoTitularidad"));
	}

	public String idEstado() {
		return consolidada.string("estadoCajaSeguridad");
	}

	public String estado() {
		String estado = "";
		estado = "V".equals(consolidada.string("estadoCajaSeguridad")) ? "Vigente" : estado;
		estado = "C".equals(consolidada.string("estadoCajaSeguridad")) ? "Cancelada" : estado;
		estado = "L".equals(consolidada.string("estadoCajaSeguridad")) ? "Legales" : estado;
		estado = "B".equals(consolidada.string("estadoCajaSeguridad")) ? "Bloqueada" : estado;
		return estado;
	}

	public String fechaAlta(String formato) {
		return consolidada.date("fechaAlta", "yyyy-MM-dd", formato);
	}

	public String fechaVencimiento(String formato) {
		return consolidada.date("fechaVencimiento", "yyyy-MM-dd", formato);
	}

	public String sucursal() {
		return Texto.primeraMayuscula(consolidada.string("descSucursal"));
	}

//	{
//	    "muestraPaquete" : false,
//	    "tipoProducto" : "CSG",
//	    "numeroProducto" : "2542",
//	    "idProducto" : "55705291",
//	    "sucursal" : 18,
//	    "descSucursal" : "PARANA",
//	    "descEstado" : "VIGENTE",
//	    "estado" : "V",
//	    "fechaAlta" : "2009-06-30",
//	    "idDomicilio" : 1,
//	    "tipoTitularidad" : "T",
//	    "descTipoTitularidad" : "TITULAR",
//	    "tipoOperacion" : "",
//	    "adicionales" : false,
//	    "moneda" : "80",
//	    "descMoneda" : "PESOS",
//	    "estadoCajaSeguridad" : "V",
//	    "fechaVencimiento" : "2019-06-21"
//	  }
}
