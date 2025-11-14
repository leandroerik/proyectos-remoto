package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.salma.ModificarLimiteMinimoSalma;
import ar.com.hipotecario.backend.servicio.sql.salma.ModificarLimiteMinimoSalma.ModificaLimiteMinimoSalma;

public class SqlSalma extends Sql {

	public static String SQL = "salma";

	/* ========== SERVICIOS ========== */
	public static Futuro<Boolean> modificar(Contexto contexto, String cuil, String limiteMinimo) {
		return futuro(() -> ModificarLimiteMinimoSalma.modificar(contexto, cuil, limiteMinimo));
	}

	public static Futuro<ModificaLimiteMinimoSalma> obtenerLimiteMinimo(Contexto contexto, String cuil) {
		return futuro(() -> ModificarLimiteMinimoSalma.obtener(contexto, cuil));
	}
}
