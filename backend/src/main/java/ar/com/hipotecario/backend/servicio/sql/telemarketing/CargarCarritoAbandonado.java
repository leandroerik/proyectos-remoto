package ar.com.hipotecario.backend.servicio.sql.telemarketing;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank.LogBuhoBank;

@SuppressWarnings("serial")
public class CargarCarritoAbandonado extends SqlObjetos<LogBuhoBank> {

	/* ========== ATRIBUTOS ========== */
	public static class ItemCarritoAbandonado extends SqlObjeto {
		public String CUIL;
		public String TelCelular;
		public String DNI;
		public String NombreApellido;
		public String ListaDiscador;
	}

	/* ========== SERVICIO ========== */
	public static Boolean post(Contexto contexto, ItemCarritoAbandonado item) {
		String sql = "EXEC [dbo].[BuhoBank_CargarLlamada] ";
		sql += "?, ?, null, ?, ?, ?";

		return Sql.update(contexto, "telemarketing", sql, item.CUIL, item.TelCelular, item.DNI, item.NombreApellido, item.ListaDiscador) > 0;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "desarrollo");
		ItemCarritoAbandonado item = new ItemCarritoAbandonado();
		item.CUIL = "";
		Boolean exito = post(contexto, item);
		imprimirResultado(contexto, exito);
	}
}
