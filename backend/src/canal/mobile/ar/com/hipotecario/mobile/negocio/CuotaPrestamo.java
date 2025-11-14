package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;

import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPrestamo;

public class CuotaPrestamo {

	/* ========== ATRIBUTOS ========== */
	private Objeto cuota;

	/* ========== CONSTRUCTORES ========== */
	public CuotaPrestamo(Objeto cuota) {
		this.cuota = cuota;
	}

	/* ========== METODOS ========== */
	public String id() {
		return cuota.string("numero");
	}

	public String numero() {
		return cuota.string("numero");
	}

	public BigDecimal saldoPrestamo() {
		return cuota.bigDecimal("saldoCapital");
	}

	public String saldoPrestamoFormateado() {
		return Formateador.importe(saldoPrestamo());
	}

	public String fechaVencimiento(String formato) {
		return cuota.date("fechaVencimiento", "yyyy-MM-dd", formato);
	}

	public BigDecimal importeCuota() {
		return cuota.bigDecimal("total");
	}

	public String importeCuotaFormateado() {
		return Formateador.importe(importeCuota());
	}

	public String importePagarFormateado() {
		return cuota.string(""); // TODO
	}

	public String idEstado() {
		return cuota.string("estado");
	}

	public String estado() {
		return RestPrestamo.estado(idEstado());
	}

	public String cuotaPuraFormateada() {
		return Formateador.importe(cuota.bigDecimal("capital"));
	}

	public String interesFormateado() {
		return Formateador.importe(cuota.bigDecimal("intereses"));
	}

	public String otrosRubrosFormateado() {
		return Formateador.importe(cuota.bigDecimal("otrosRubros"));
	}

	public String impuestosFormateado() {
		return Formateador.importe(cuota.bigDecimal("impuestos"));
	}

//	/* ========== OBJETO ========== */
//	{
//		"numero": "1",
//		"fechaVencimiento": "2014-11-10",
//		"saldoCapital": 202801.25,
//		"capital": 411.59,
//		"ajusteCapital": 0,
//		"otrosCapitales": 0,
//		"intereses": 507,
//		"otrosIntereses": 0,
//		"monto": 918.59,
//		"otrosRubros": 4.59,
//		"impuestos": 0.96,
//		"total": 924.14,
//		"montoBeneficios": 0,
//		"exigible": 924.14,
//		"gracia": 0,
//		"capitalizado": 0,
//		"extraordinario": 0,
//		"ajusteCapitalExtraordinario": 0,
//		"estado": "CANCELADA",
//		"cuotaNSP": "",
//		"ivaPercepcion": 0,
//		"quitaCapital": 0,
//		"cobrado": 924.14
//	}
}
