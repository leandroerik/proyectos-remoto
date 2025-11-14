package ar.com.hipotecario.mobile.negocio;

import ar.com.hipotecario.mobile.lib.Objeto;

public class ProductoMora {

	private Objeto producto;

	public ProductoMora(Objeto productosEnMora) {
		this.producto = productosEnMora;
	}

	public String ctaId() {
		return producto.string("cta_id");
	}

	public String tipoMora() {
		return producto.string("Tipo Mora");
	}

	public String prodCod() {
		return producto.string("pro_cod");
	}

	public String numeroProducto() {
		return producto.string("NumeroProducto");
	}

	public Integer diasMora() {
		return producto.integer("diasMora");
	}
}
