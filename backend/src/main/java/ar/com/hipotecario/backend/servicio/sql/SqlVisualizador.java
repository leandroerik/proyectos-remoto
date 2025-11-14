package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.visualizador.LogsMW;
import ar.com.hipotecario.backend.servicio.sql.visualizador.LogsMW.LogMW;

public class SqlVisualizador extends Sql {

	/* ========== SERVICIOS ========== */
	public static Futuro<LogMW> logMW(Contexto contexto, String idProceso) {
		return futuro(() -> LogsMW.get(contexto, idProceso));
	}
}
