package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.clientesoperadores.OpUsers;
import ar.com.hipotecario.backend.servicio.sql.clientesoperadores.OpUsers.OpUser;

public class SqlClientesOperadores extends Sql {

	/* ========== SERVICIOS ========== */
	public static Futuro<OpUser> opUser(Contexto contexto, String idCobis) {
		return futuro(() -> OpUsers.select(contexto, idCobis));
	}
}
