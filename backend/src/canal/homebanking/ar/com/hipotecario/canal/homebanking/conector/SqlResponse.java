package ar.com.hipotecario.canal.homebanking.conector;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class SqlResponse {

	/* ========== ATRIBUTOS ========== */
	public Boolean hayError = false;
	public List<Objeto> registros = new ArrayList<>();
	public Exception exception = null;
}
