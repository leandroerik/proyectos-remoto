package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales.ProspectEsales;
import ar.com.hipotecario.backend.servicio.sql.telemarketing.CarritoAbandonadosTelemarketing;
import ar.com.hipotecario.backend.servicio.sql.telemarketing.CarritoAbandonadosTelemarketing.CarritoAbandonadoTelemarketing;

public class SqlCarritoAbandonado extends Sql {

	/* ========== SERVICIOS ========== */
	public static String SQL = "telemarketing";

	/* ========== SERVICIOS ========== */
	public static Futuro<CarritoAbandonadosTelemarketing> obtenerLlamadosCarritoAbandonado(Contexto contexto) {
		return futuro(() -> CarritoAbandonadosTelemarketing.getLlamados(contexto));
	}

	public static Futuro<Boolean> enviarAGenesis(Contexto contexto, ProspectEsales prospect) {
		return futuro(() -> CarritoAbandonadosTelemarketing.cargarCarritoAbandonado(contexto, prospect));
	}

	public static Futuro<Boolean> refresh(Contexto contexto) {
		return futuro(() -> CarritoAbandonadosTelemarketing.refresh(contexto));
	}

}
