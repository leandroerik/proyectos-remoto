package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.logssistemas.LogsApiVenta;
import ar.com.hipotecario.backend.servicio.sql.logssistemas.LogsApiVenta.LogApiVenta;

public class SqlLogsSistemas extends Sql {

	/* ========== SERVICIOS ========== */
	public static Futuro<LogApiVenta> logApiVenta(Contexto contexto, String idProceso) {
		return futuro(() -> LogsApiVenta.get(contexto, idProceso));
	}
}
