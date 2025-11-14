package ar.com.hipotecario.mobile.negocio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public class Email {

	/* ========== ATRIBUTOS ========== */
//	private Contexto contexto;
	private Objeto email;

	/* ========== CONSTRUCTOR ========== */
	public Email(ContextoMB contexto, String tipo) {
//		this.contexto = contexto;
		if (contexto.persona().cuit() != null && !contexto.persona().cuit().trim().isEmpty()) {
			this.email = RestPersona.mapaEmails(contexto, contexto.persona().cuit()).get(tipo);
			if (this.email == null) {
				this.email = new Objeto();
			}
		}

	}

	/* ========== ATRIBUTOS ========== */
	public String direccion() {
		return email.string("direccion");
	}

//	{
//		"id": 6001681,
//		"idTipoMail": "EMP",
//		"direccion": "jorgelinalourdes26@gmail.com",
//		"idCore": 2,
//		"prioridad": 3,
//		"esDeclarado": false,
//		"canalModificacion": "BATCH",
//		"fechaCreacion": "2018-03-09T17:31:09",
//		"usuarioModificacion": "BATCH",
//		"fechaModificacion": "2018-03-09T17:31:09",
//		"etag": 1031681690
//	}
}
