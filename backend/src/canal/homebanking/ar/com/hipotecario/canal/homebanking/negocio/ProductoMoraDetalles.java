package ar.com.hipotecario.canal.homebanking.negocio;

import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class ProductoMoraDetalles {
	private Objeto producto;

	public ProductoMoraDetalles(Objeto productosEnMora) {
		this.producto = productosEnMora;
	}

	public String inicioMora() {
		return producto.string("Inicio Mora");
	}

	public String deudaVencida() {
		return producto.string("Deuda Vencida");
	}

	public Integer diasEnMora() {
		return producto.integer("DiasMora");
	}

	public String ctaId() {
		return producto.string("cta_id");
	}

	public String promesaVigente() {
		return producto.string("PromesaVigente");
	}

	public String yaPague() {
		return producto.string("IndicaYPAG");
	}

	public String deudaAVencer() {
		return producto.string("deudaAVencer");
	}

	public String montoMinPromesa() {
		return producto.string("montoMinPromesa");
	}
}
