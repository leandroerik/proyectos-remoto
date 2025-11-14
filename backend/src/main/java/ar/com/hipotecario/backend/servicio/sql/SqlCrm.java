package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.crm.ActividadesCrm;

public class SqlCrm extends Sql {

	public static String SQL = "crm";

	/* ========== SERVICIOS ========== */

	public static Futuro<Boolean> fueLlamadoCrmPaquetes(Contexto contexto, String cuil, Fecha fechaDesde) {
		return futuro(() -> ActividadesCrm.fueLlamadoCrmPaquetes(contexto, cuil, fechaDesde));
	}

	public static Futuro<Boolean> fueLlamadoCrmPP(Contexto contexto, String cuil, Fecha fechaDesde) {
		return futuro(() -> ActividadesCrm.fueLlamadoCrmPP(contexto, cuil, fechaDesde));
	}

}
