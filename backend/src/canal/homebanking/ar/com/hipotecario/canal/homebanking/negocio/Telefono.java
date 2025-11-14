package ar.com.hipotecario.canal.homebanking.negocio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class Telefono {

	/* ========== ATRIBUTOS ========== */
//	private Contexto contexto;
	private Objeto telefono;

	/* ========== CONSTRUCTOR ========== */
	public Telefono(ContextoHB contexto, String tipo) {
//		this.contexto = contexto;
		this.telefono = RestPersona.mapaTelefonos(contexto, contexto.persona().cuit()).get(tipo);
		if (this.telefono == null) {
			this.telefono = new Objeto();
		}
	}

	/* ========== ATRIBUTOS ========== */

	public String string(String clave, String valorPorDefecto) {
		return telefono.string(clave, valorPorDefecto);
	}

	public String codigoArea() {
		return telefono.string("codigoArea");
	}

	public String caracteristica() {
		return telefono.string("caracteristica");
	}

	public String finalNumero() {
		return telefono.string("numero");
	}

//	{
//		"id": 4348975,
//		"idTipoTelefono": "P",
//		"codigoPais": "054",
//		"codigoArea": "03465",
//		"prefijo": "",
//		"caracteristica": "42",
//		"numero": "4370",
//		"interno": "",
//		"idTelefonoPertenencia": "",
//		"idCore": 1,
//		"prioridad": null,
//		"esListaNegra": null,
//		"numeroNormalizado": "03465424370",
//		"canalModificacion": "CORE",
//		"fechaCreacion": "2008-10-09T02:04:43",
//		"usuarioModificacion": "b02867",
//		"fechaModificacion": "2009-05-07T08:07:11",
//		"esParaRecibirSMS": null,
//		"idDireccion": null,
//		"etag": 212044842
//	}, {
//		"id": 4348976,
//		"idTipoTelefono": "E",
//		"codigoPais": "054",
//		"codigoArea": "03471",
//		"prefijo": "15",
//		"caracteristica": "55",
//		"numero": "9508",
//		"interno": "",
//		"idTelefonoPertenencia": "",
//		"idCore": 4,
//		"prioridad": null,
//		"esListaNegra": null,
//		"numeroNormalizado": "0347115559508",
//		"canalModificacion": "CORE",
//		"fechaCreacion": "2016-07-13T13:26:50",
//		"usuarioModificacion": "hobdes",
//		"fechaModificacion": "2019-05-08T12:50:13",
//		"esParaRecibirSMS": null,
//		"idDireccion": 1,
//		"etag": 719624543
//	}
}
