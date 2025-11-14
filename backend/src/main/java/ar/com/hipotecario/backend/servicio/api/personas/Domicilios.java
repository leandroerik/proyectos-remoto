package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;

public class Domicilios extends ApiObjetos<Domicilio> {

	public static String GET_DOMICILIOS = "Domicilios";
	public static String POST_DOMICILIO = "CrearDomicilio";
	public static String PATCH_DOMICILIO = "ActualizarDomicilio";

	/* ========== ATRIBUTOS ========== */
	public static class Domicilio extends ApiObjeto {
		// cobis: SELECT * FROM cobis.dbo.cl_catalogo WHERE tabla IN (SELECT codigo FROM
		// cobis.dbo.cl_tabla WHERE tabla = 'cl_tdireccion')
		public static String COMERCIAL = "CO";
		public static String POSTAL = "DP";
		public static String LABORAL = "LA";
		public static String LEGAL = "LE";

		public String id;
		public String idTipoDomicilio;
		public String calle;
		public String numero;
		public String piso;
		public String departamento;
		public String calleEntre1;
		public String calleEntre2;
		public String idCodigoPostal;
		public String codigoPostalAmpliado;
		public String idCiudad;
		public String idProvincia;
		public String idPais;
		public String ubicacion;
		public String idCore;
		public String barrio;
		public String latitud;
		public String longitud;
		public String canalModificacion;
		public Fecha fechaCreacion;
		public String usuarioModificacion;
		public Fecha fechaModificacion;
		public String ciudad;
		public String pais;
		public String provincia;
		public String partido;
		public String etag;

		public Fecha fecha() {
			return Fecha.maxima(fechaCreacion, fechaModificacion);
		}

		public Domicilio actualizar(NuevoDomicilio domicilio) {
			this.calle = domicilio.calle;
			this.numero = domicilio.numero;
			this.piso = domicilio.piso;
			this.departamento = domicilio.departamento;
			this.calleEntre1 = domicilio.calleEntre1;
			this.calleEntre2 = domicilio.calleEntre2;
			this.idCodigoPostal = domicilio.idCodigoPostal;
			this.codigoPostalAmpliado = domicilio.codigoPostalAmpliado;
			this.idCiudad = domicilio.idCiudad;
			this.idProvincia = domicilio.idProvincia;
			this.idPais = domicilio.idPais;
			return this;
		}
		
		public int getCodigoProvincia() {
		    switch (idProvincia.trim()) {
		        case "1":
		            return CodigoProvincia.CIUDAD_DE_BUENOS_AIRES.getCodigoProvincia();
		        case "2":
		            return CodigoProvincia.PROVINCIA_DE_BUENOS_AIRES.getCodigoProvincia();
		        case "3":
		            return CodigoProvincia.CATAMARCA.getCodigoProvincia();
		        case "4":
		            return CodigoProvincia.CORDOBA.getCodigoProvincia();
		        case "5":
		            return CodigoProvincia.CORRIENTES.getCodigoProvincia();
		        case "6":
		            return CodigoProvincia.CHACO.getCodigoProvincia();
		        case "7":
		            return CodigoProvincia.CHUBUT.getCodigoProvincia();
		        case "8":
		            return CodigoProvincia.FORMOSA.getCodigoProvincia();
		        case "9":
		            return CodigoProvincia.ENTRE_RIOS.getCodigoProvincia();
		        case "10":
		            return CodigoProvincia.JUJUY.getCodigoProvincia();
		        case "11":
		            return CodigoProvincia.LA_PAMPA.getCodigoProvincia();
		        case "12":
		            return CodigoProvincia.LA_RIOJA.getCodigoProvincia();
		        case "13":
		            return CodigoProvincia.MENDOZA.getCodigoProvincia();
		        case "14":
		            return CodigoProvincia.MISIONES.getCodigoProvincia();
		        case "15":
		            return CodigoProvincia.NEUQUEN.getCodigoProvincia();
		        case "16":
		            return CodigoProvincia.RIO_NEGRO.getCodigoProvincia();
		        case "17":
		            return CodigoProvincia.SALTA.getCodigoProvincia();
		        case "18":
		            return CodigoProvincia.SAN_JUAN.getCodigoProvincia();
		        case "19":
		            return CodigoProvincia.SAN_LUIS.getCodigoProvincia();
		        case "20":
		            return CodigoProvincia.SANTA_CRUZ.getCodigoProvincia();
		        case "21":
		            return CodigoProvincia.SANTA_FE.getCodigoProvincia();
		        case "22":
		            return CodigoProvincia.SANTIAGO_DEL_ESTERO.getCodigoProvincia();
		        case "23":
		            return CodigoProvincia.TIERRA_DEL_FUEGO.getCodigoProvincia();
		        case "24":
		            return CodigoProvincia.TUCUMAN.getCodigoProvincia();
		        default:
		            throw new IllegalArgumentException("Código de provincia no válido: " + provincia);
		    }
		}
	}

	/* ========== CLASES ========== */
	public static class NuevoDomicilio {
		public String calle;
		public String numero;
		public String piso;
		public String departamento;
		public String calleEntre1;
		public String calleEntre2;
		public String idCodigoPostal;
		public String codigoPostalAmpliado;
		public String idCiudad;
		public String idProvincia;
		public String idPais;
	}

	/* ========== SERVICIOS ========== */
	// API-Personas_ConsultarDomiciliosDePersona
	public static Domicilios get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest(GET_DOMICILIOS, ApiPersonas.API, "GET", "/personas/{id}/domicilios", contexto);
		request.path("id", cuit);
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(Domicilios.class);
	}

	// API-Personas_AltaDomicilioAPersona
	public static Domicilio post(Contexto contexto, String cuit, NuevoDomicilio domicilio, String tipo) {
		ApiRequest request = new ApiRequest(POST_DOMICILIO, ApiPersonas.API, "POST", "/personas/{id}/domicilios", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.body("idTipoDomicilio", tipo);
		request.body("calle", domicilio.calle);
		request.body("numero", domicilio.numero);
		request.body("piso", domicilio.piso);
		request.body("departamento", domicilio.departamento);
		request.body("calleEntre1", domicilio.calleEntre1);
		request.body("calleEntre2", domicilio.calleEntre2);
		request.body("idCodigoPostal", domicilio.idCodigoPostal);
		request.body("codigoPostalAmpliado", domicilio.codigoPostalAmpliado);
		request.body("idCiudad", domicilio.idCiudad);
		request.body("idProvincia", domicilio.idProvincia);
		request.body("idPais", domicilio.idPais);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_DOMICILIOS, cuit);
		ApiException.throwIf("EXISTE_TIPO_DOMICILIO", response.contains("Ya existe un domicilio"), request, response);
		ApiException.throwIf(!response.http(201), request, response);
		return response.crear(Domicilio.class);
	}

	// API-Personas_ModificarParcialmenteDomicilio
	public static Domicilio patch(Contexto contexto, String cuit, Domicilio domicilio) {
		ApiRequest request = new ApiRequest(PATCH_DOMICILIO, ApiPersonas.API, "PATCH", "/domicilios/{id}", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", domicilio.id);
		request.body("calle", domicilio.calle);
		request.body("numero", domicilio.numero);
		request.body("piso", domicilio.piso);
		request.body("departamento", domicilio.departamento);
		request.body("calleEntre1", domicilio.calleEntre1);
		request.body("calleEntre2", domicilio.calleEntre2);
		request.body("idCodigoPostal", domicilio.idCodigoPostal);
		request.body("codigoPostalAmpliado", domicilio.codigoPostalAmpliado);
		request.body("idCiudad", domicilio.idCiudad);
		request.body("idProvincia", domicilio.idProvincia);
		request.body("idPais", domicilio.idPais);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_DOMICILIOS, cuit);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Domicilio.class);
	}

	/* ========== METODOS ========== */
	public Domicilio crearActualizar(Contexto contexto, String cuit, NuevoDomicilio nuevoDomicilio, String tipo) {
		Domicilio domicilio = buscar(tipo);
		if (domicilio == null) {
			domicilio = ApiPersonas.crearDomicilio(contexto, cuit, nuevoDomicilio, tipo).get();
		} else {
			domicilio = ApiPersonas.actualizarDomicilio(contexto, cuit, domicilio.actualizar(nuevoDomicilio)).get();
		}
		return domicilio;
	}

	public Domicilio crearActualizarTry(Contexto contexto, String cuit, NuevoDomicilio nuevoDomicilio, String tipo) {

		Domicilio domicilio = buscar(tipo);
		if (domicilio == null) {
			domicilio = ApiPersonas.crearDomicilio(contexto, cuit, nuevoDomicilio, tipo).get();
		} else {
			domicilio = ApiPersonas.actualizarDomicilio(contexto, cuit, domicilio.actualizar(nuevoDomicilio)).get();
		}
		return domicilio;
	}

	private Domicilio buscar(String tipo) {
		Domicilio dato = null;
		for (Domicilio domicilio : this) {
			if (domicilio.idTipoDomicilio.equals(tipo)) {
				dato = (dato == null || domicilio.fecha().esPosterior(dato.fecha())) ? domicilio : dato;
			}
		}
		return dato;
	}

	public Domicilio comercial() {
		return buscar(Domicilio.COMERCIAL);
	}

	public Domicilio postal() {
		return buscar(Domicilio.POSTAL);
	}

	public Domicilio laboral() {
		return buscar(Domicilio.LABORAL);
	}

	public Domicilio legal() {
		return buscar(Domicilio.LEGAL);
	}

	
	public enum CodigoProvincia {
		CIUDAD_DE_BUENOS_AIRES(901),
		PROVINCIA_DE_BUENOS_AIRES(902),
		CATAMARCA(903),
		CORDOBA(904),
		CORRIENTES(905),
		CHACO(906),
		CHUBUT(907),
		ENTRE_RIOS(908),
		FORMOSA(909),
		JUJUY(910),
		LA_PAMPA(911),
		LA_RIOJA(912),
		MENDOZA(913),
		MISIONES(914),
		NEUQUEN(915),
		RIO_NEGRO(916),
		SALTA(917),
		SAN_JUAN(918),
		SAN_LUIS(919),
		SANTA_CRUZ(920),
		SANTA_FE(921),
		SANTIAGO_DEL_ESTERO(922),
		TIERRA_DEL_FUEGO(923),
		TUCUMAN(924);
		
		private final int codigoProvincia;

		CodigoProvincia(int codigo){
			this.codigoProvincia=codigo;
		}
		int getCodigoProvincia(){
			return codigoProvincia;
		}
	}
	
	/* ========== TEST ========== */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Contexto contexto = new Contexto("HB", "desarrollo", "133366");

		String test = "post";
		if ("get".equals(test)) {
			Domicilios datos = get(contexto, "20320732892", true);
			Domicilio comercial = datos.comercial();
			Domicilio legal = datos.legal();
			Domicilio postal = datos.postal();
			Domicilio laboral = datos.laboral();
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos.get(0));
		}
		if ("post".equals(test)) {
			NuevoDomicilio domicilio = new NuevoDomicilio();
			domicilio.calle = "GRAL PAZ";
			domicilio.numero = "1346";
			domicilio.piso = "";
			domicilio.departamento = "";
			domicilio.calleEntre1 = "";
			domicilio.calleEntre2 = "";
			domicilio.idCodigoPostal = "1722";
			domicilio.idCiudad = "7323";
			domicilio.idProvincia = "2";
			Domicilio datos = post(contexto, "20275551083", domicilio, Domicilio.POSTAL);
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos);
		}
		if ("patch".equals(test)) {
			String cuit = "20275551083";
			NuevoDomicilio domicilio = new NuevoDomicilio();

			domicilio.calle = "GRAL PAZ";
			domicilio.numero = "1346";
			domicilio.piso = "";
			domicilio.departamento = "";
			domicilio.calleEntre1 = "";
			domicilio.calleEntre2 = "";
			domicilio.idCodigoPostal = "1722";
			domicilio.idCiudad = "7323";
			domicilio.idProvincia = "2";

			Domicilios domicilios = get(contexto, cuit, true);
			Domicilio postal = domicilios.postal();
			Domicilio datos = patch(contexto, cuit, postal.actualizar(domicilio));
			imprimirResultado(contexto, datos);
		}
	}
}
