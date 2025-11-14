package ar.com.hipotecario.canal.buhobank.teradata;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.teradata.DatosActividad.DatoActividad;

public class SqlTeradata extends Sql {

	/* ========== SERVICIOS ========== */
	public static String SQL = "teradata";

	/* ========== SERVICIOS ========== */

	public static Futuro<DatoActividad> get(ContextoBB contexto, String cuit) {
		return futuro(() -> DatosActividad.get(contexto, cuit));
	}

}
