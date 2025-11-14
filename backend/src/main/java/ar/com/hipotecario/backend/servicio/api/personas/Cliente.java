package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.NuevoDomicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.NuevoTelefono;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;

public class Cliente extends Persona {

	public static String GET_CLIENTE = "Cliente";
	public static String POST_CLIENTE = "CrearCliente";

	/* ========== SERVICIOS ========== */
	// API-Personas_ConsultarPersonaIDCore
	public static Cliente get(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest(GET_CLIENTE, ApiPersonas.API, "GET", "/clientes/{idCliente}", contexto);
		request.path("idCliente", idCobis);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no fue encontrada en BUPT"), request, response);
		ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);
		return response.crear(Cliente.class, response.objetos(0));
	}

	// API-Personas_AltaCliente
	public static Cliente post(Contexto contexto, String cuit) {
		ApiRequest request = new ApiRequest(POST_CLIENTE, ApiPersonas.API, "POST", "/clientes", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.body("cuit", cuit);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, "Persona", cuit);
		Api.eliminarCache(contexto, "Cliente", response.string("idCliente"));
		ApiException.throwIf("FUERA_HORARIO", response.contains("FAULTCODE:40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cliente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "get";

		if (test.equals("get")) {
			Contexto contexto = contexto("HB", "homologacion");
			Cliente datos = get(contexto, "133366");
			imprimirResultado(contexto, datos);
		}

		if (test.equals("post")) {
			String cuit = "20336539014";
			Contexto contexto = contexto("HB", "homologacion");

			Persona persona = ApiPersonas.persona(contexto, cuit).tryGet();
			if (persona == null) {
				persona = ApiPersonas.crearPersona(contexto, cuit).get();
			}
			if (persona.faltanDatosCriticos()) {
				persona.idNacionalidad = "80";
				persona.idSexo = "M";
				persona.idTipoDocumento = "01";
				persona.nombres = "GABRIEL ERNESTO";
				persona.numeroDocumento = cuit.substring(2, 10);
				persona.idTipoIDTributario = "08";
				persona.idIva = persona.idTipoIDTributario.equals("08") ? "CONF" : "MONO";
				persona.apellidos = "GONZALEZ";
				persona.fechaNacimiento = Fecha.hoy().restarAños(21);
				persona = ApiPersonas.actualizarPersona(contexto, persona).get();
			}

			Domicilios domicilios = ApiPersonas.domicilios(contexto, cuit).get();
			NuevoDomicilio domicilio = new NuevoDomicilio();
			domicilio.calle = "YRIGOYEN";
			domicilio.idCiudad = "195";
			domicilio.idProvincia = "1";
			domicilio.idCodigoPostal = "1406";
			domicilios.crearActualizar(contexto, cuit, domicilio, Domicilio.LEGAL);
			domicilios.crearActualizar(contexto, cuit, domicilio, Domicilio.POSTAL);

			Telefonos telefonos = ApiPersonas.telefonos(contexto, cuit).get();
			NuevoTelefono telefono = new NuevoTelefono();
			telefono.codigoPais = "054";
			telefono.codigoArea = "011";
			telefono.prefijo = "15";
			telefono.caracteristica = "3645";
			telefono.numero = "5850";
			telefonos.crearActualizar(contexto, cuit, telefono, Telefono.CELULAR);

			Emails emails = ApiPersonas.emails(contexto, cuit).get();
			emails.crearActualizar(contexto, cuit, "prueba@hipotecario.com.ar", Email.PERSONAL);

			Cliente datos = post(contexto, cuit);
			imprimirResultado(contexto, datos);
		}
	}
}
