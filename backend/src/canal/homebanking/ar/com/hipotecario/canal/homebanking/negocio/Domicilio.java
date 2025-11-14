package ar.com.hipotecario.canal.homebanking.negocio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class Domicilio {

	/* ========== ATRIBUTOS ========== */
	private ContextoHB contexto;
	private Objeto domicilio;

	/* ========== CONSTRUCTOR ========== */
	public Domicilio(ContextoHB contexto, String tipo) {
		this.contexto = contexto;
		this.domicilio = RestPersona.mapaDomicilios(contexto, contexto.persona().cuit()).get(tipo);
		if (this.domicilio == null) {
			this.domicilio = new Objeto();
		}
	}

	/* ========== ATRIBUTOS ========== */
	public String string(String clave, String valorPorDefecto) {
		Object valor = domicilio.get(clave);
		return valor != null | valor != "" ? valor.toString() : valorPorDefecto;
	}

	public String calle() {
		return domicilio.string("calle");
	}

	public String altura() {
		return domicilio.string("numero");
	}

	public String piso() {
		return domicilio.string("piso");
	}

	public String dpto() {
		return domicilio.string("departamento");
	}

	public String codigoPostal() {
		return domicilio.string("idCodigoPostal");
	}

	public String idLocalidad() {
		return domicilio.string("idCiudad");
	}

	public String localidad() {
		return RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia"), domicilio.integer("idCiudad"));
//		return domicilio.string("ciudad");
	}

	public String idProvincia() {
		return domicilio.string("idProvincia");
	}

	public String provincia() {
		return RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia"));
//		return domicilio.string("provincia");
	}

//	{
//		"id": 7328190,
//		"idTipoDomicilio": "DP",
//		"calle": "H YRIGOYEN",
//		"numero": 1784,
//		"piso": null,
//		"departamento": null,
//		"calleEntre1": "SAN LORENZO",
//		"calleEntre2": "MORENO",
//		"idCodigoPostal": "2630",
//		"codigoPostalAmpliado": "",
//		"idCiudad": 18876,
//		"idProvincia": 21,
//		"idPais": 80,
//		"ubicacion": null,
//		"idCore": 2,
//		"barrio": null,
//		"latitud": null,
//		"longitud": null,
//		"canalModificacion": "BATCH",
//		"fechaCreacion": null,
//		"usuarioModificacion": "BATCH",
//		"fechaModificacion": "2019-05-22T15:02:27",
//		"ciudad": null,
//		"pais": "ARGENTINA",
//		"provincia": null,
//		"partido": null,
//		"etag": -1162482040
//	}
}
