package ar.com.hipotecario.backend.servicio.api.cajasseguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.cajasseguridad.CajasSeguridad.EstadoCajaSeguridad;

// http://api-cajasseguridad-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiCajasSeguridad {

	/* ========== Operatoria de cajas de seguridad ========== */

	// GET /v2/cajasseguridad
	public static Futuro<CajasSeguridad> cajasSeguridad(Contexto contexto, String idCliente) {
		return Util.futuro(() -> CajasSeguridad.get(contexto, idCliente));
	}

	public static Futuro<CajasSeguridad> cajasSeguridad(Contexto contexto, String idCliente, EstadoCajaSeguridad tipoEstado) {
		return Util.futuro(() -> CajasSeguridad.get(contexto, idCliente, tipoEstado));
	}

	// GET /v2/cajasseguridad/{idcajaseguridad}
	public static Futuro<DetalleCajaSeguridad> detalleCajaSeguridad(Contexto contexto, String idCajasSeguridad) {
		return Util.futuro(() -> DetalleCajaSeguridad.get(contexto, idCajasSeguridad));
	}
}
