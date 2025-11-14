package ar.com.hipotecario.canal.homebanking.api;

import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.servicio.BuhopuntosProductoService;

public class HBBuhopuntosProducto {

	public static Respuesta validarCashBack(ContextoHB contexto) {

		Boolean prendidoCashback = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cashback",
				"prendido_cashback_cobis");

		return Respuesta.exito("prendidoCashBack", prendidoCashback);
	}

	public static Respuesta consultaCashBack(ContextoHB contexto) {
		Objeto cashback = BuhopuntosProductoService.consultaCashBack(contexto);
		Boolean tieneTcPermitida = BuhopuntosProductoService.tieneTcPermitida(contexto);
		Boolean tieneCAPermitida = BuhopuntosProductoService.tieneCAPermitida(contexto);
		Respuesta respuesta = new Respuesta();
		respuesta.set("tc_permitida", tieneTcPermitida);
		respuesta.set("ca_permitida", tieneCAPermitida);
		respuesta.set("cashback", cashback);
		respuesta.set("estado", "0");
		return respuesta;
	}

	public static Respuesta canjearCashback(ContextoHB contexto) {

		Integer idOperacion = BuhopuntosProductoService.canjearCashback(contexto);
		if (idOperacion == -1)
			return Respuesta.error();

		Respuesta respuesta = new Respuesta();

		respuesta.set("id_operacion", idOperacion);
		return respuesta;

	}

	public static Respuesta aceptarPropuesta(ContextoHB contexto) {

		Integer respuesta = BuhopuntosProductoService.aceptarPropuesta(contexto);
		if (respuesta != 0)
			return Respuesta.error();

		return Respuesta.exito();
	}

	public static Respuesta getPropuestas(ContextoHB contexto) {
		Objeto propuestas = BuhopuntosProductoService.getPropuestas(contexto);
		if (propuestas.objetos().isEmpty()) {
			Objeto listaVacia = new Objeto();
			return Respuesta.exito("propuestas", listaVacia.objetos());
		}
		return Respuesta.exito("propuestas", propuestas);
	}

	public static Respuesta consultarHistorialPropuestas(ContextoHB contexto) {

		List<Objeto> historial = BuhopuntosProductoService.consultarHistorialPropuestas(contexto);

		Objeto listaVacia = new Objeto();
		return Respuesta.exito("propuestas", historial != null && historial.size() > 0 ? historial : listaVacia);
	}

	public static byte[] comprobantePropuesta(ContextoHB contexto) {

		return BuhopuntosProductoService.comprobantePropuesta(contexto);
	}

	public static byte[] comprobanteCashback(ContextoHB contexto) {

		return BuhopuntosProductoService.comprobanteCashback(contexto);
	}

}
