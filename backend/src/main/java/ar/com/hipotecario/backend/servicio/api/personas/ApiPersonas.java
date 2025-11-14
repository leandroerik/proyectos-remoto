package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.inversiones.PerfilInversor;
import ar.com.hipotecario.backend.servicio.api.inversiones.PerfilInversor.EnumOperacionPI;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades.Actividad;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades.NuevaActividad;
import ar.com.hipotecario.backend.servicio.api.personas.DocumentoCuestionados.DocumentoCuestionado;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.NuevoDomicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.NuevaRelacion;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.Relacion;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.NuevoTelefono;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;

//http://api-personas-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiPersonas extends Api {

	/* ========== CONSTANTES ========== */
	public static String API = "personas";
	public static String X_USUARIO = "x-usuario";

	/* ========== SERVICIOS ========== */

	/*
	 * ========== Sav Api Controller
	 * 
	 * // TODO: POST /ventas
	 * 
	 * /* ========== Informados Api Controller ==========
	 */

	/* ========== Segmentacion Cliente Api Controller ========== */

	// TODO: GET /segmentacionCliente

	// TODO: GET /segmentacionCliente/BeneficiosCantClientes

	// TODO: GET /segmentacionVLC

	/* ========== Actividades Api Controller ========== */

	// TODO: GET /actividades
	public static Futuro<Actividades> actividades(Contexto contexto, String cuit) {
		return futuro(() -> Actividades.get(contexto, cuit, true));
	}

	public static Futuro<Actividades> actividades(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Actividades.get(contexto, cuit, cache));
	}

	// TODO: POST @Deprecated /actividades/{id}
	public static Futuro<Actividad> crearActividad(Contexto contexto, String cuit, NuevaActividad actividad) {
		return futuro(() -> Actividades.post(contexto, cuit, actividad));
	}

	// TODO: GET /actividades/{id}

	// TODO: PUT /actividades/{id}

	// TODO: DELETE /actividades/{id}

	// PATCH /actividades/{id}
	public static Futuro<Actividad> actualizarActividad(Contexto contexto, String cuit, Actividad actividad) {
		return futuro(() -> Actividades.patch(contexto, cuit, actividad));
	}

	/* ========== Bases Negativas Api Controller ========== */

	// TODO: GET /basesNegativas/personas

	/* ========== Campania Api Controller ========== */

	/* ========== Censo Nacional Economico Controller ========== */

	// GET /censo
	public static Futuro<Censo> censo(Contexto contexto, String cuil, String idcobis, String sexo, String tipoDocumento) {
		return futuro(() -> Censo.get(contexto, cuil, idcobis, sexo, tipoDocumento));
	}

	/* ========== Cliente Api Controller ========== */

	// POST /clientes
	public static Futuro<Cliente> crearCliente(Contexto contexto, String cuit) {
		return futuro(() -> Cliente.post(contexto, cuit));
	}

	// GET /clientes/{idCliente}
	public static Futuro<Cliente> cliente(Contexto contexto, String idCobis) {
		return futuro(() -> Cliente.get(contexto, idCobis));
	}

	public static Futuro<Documentos> buscarPorDocumento(Contexto contexto, String numDoc) {
		return futuro(() -> Documentos.get(contexto, numDoc));
	}
	// TODO: GET /clientes/{idCliente}/beneficiariosTransferencias

	// TODO: POST /clientes/{idCliente}/beneficiariosTransferencias

	// TODO: DELETE /clientes/{idCliente}/beneficiariosTransferencias

	// TODO: PATCH /clientes/{idCliente}/beneficiariosTransferencias

	// TODO: GET /clientes/{idCliente}/identificacionPositiva

	// TODO: GET /clientes/{idCliente}/lineaCredito

	// TODO: GET /clientes/{idCliente}/vip

	/* ========== Cuil Controller ========== */

	// GET /cuils
	public static Futuro<Cuils> cuils(Contexto contexto, String numeroDocumento, String nombreCompleto) {
		return futuro(() -> Cuils.get(contexto, numeroDocumento, nombreCompleto));
	}

	public static Futuro<Cuils> cuils(Contexto contexto, String numeroDocumento) {
		return futuro(() -> Cuils.get(contexto, numeroDocumento));
	}
	
	public static Futuro<Cuils> cuilsNroDoc(Contexto contexto, String numeroDocumento) {
		return futuro(() -> Cuils.getNroDoc(contexto, numeroDocumento));
	}

	// TODO: GET /nrodoc

	/* ========== Documentos Controller ========== */

	// TODO: GET /documentoscuestionados

	// TODO: POST /documentoscuestionadosporlote

	/* ========== Domicilios Api Controller ========== */

	// TODO: GET /domicilios

	// TODO: POST /domicilios

	// TODO: GET /domicilios/{id}

	// TODO: PUT /domicilios/{id}

	// TODO: DELETE /domicilios/{id}

	// PATCH /domicilios/{id}
	public static Futuro<Domicilio> actualizarDomicilio(Contexto contexto, String cuit, Domicilio domicilio) {
		return futuro(() -> Domicilios.patch(contexto, cuit, domicilio));
	}

	/* ========== Firma Digital Api Controller ========== */

	// TODO: GET /firmaDigital/{id}/digitalizada

	// TODO: GET /firmaDigital/{id}/productosVinculados

	// TODO: GET /firmaDigital/{id}/uso

	/* ========== Gastos Patrimoniales Api Controller ========== */

	// TODO: GET /gastosPatrimoniales/{id}

	// TODO: POST /gastosPatrimoniales/{id}

	// TODO: PUT /gastosPatrimoniales/{id}

	// TODO: DELETE /gastosPatrimoniales/{id}

	// TODO: PATCH /gastosPatrimoniales/{id}

	/* ========== Mails Api Controller ========== */

	// TODO: GET /mails

	// TODO: GET /mails/{id}

	// TODO: POST /mails/{id}

	// TODO: PUT /mails/{id}

	// TODO: DELETE /mails/{id}

	// PATCH /mails/{id}
	public static Futuro<Email> actualizarEmail(Contexto contexto, String cuit, Email email) {
		return futuro(() -> Emails.patch(contexto, cuit, email));
	}

	/* ========== Microcreditos Controller ========== */

	// TODO: GET /deudores/{cuil}

	// TODO: POST /referencia

	/* ========== Perfil Inversor Controller ========== */

	// POST /administrarPerfil
	public static Futuro<PerfilInversor> administrarPerfil(Contexto contexto, String idCliente, EnumOperacionPI operacion, Integer perfilInversor) {
		return futuro(() -> PerfilInversor.post(contexto, idCliente, operacion, perfilInversor));
	}

	// GET /perfilInversor
	public static Futuro<PerfilInversor> perfilInversor(Contexto contexto, String idCliente) {
		return futuro(() -> PerfilInversor.get(contexto, idCliente));
	}

	/* ========== Perfiles Patrimoniales Api Controller ========== */

	// TODO: GET /perfilesPatrimoniales/{id}

	// TODO: POST /perfilesPatrimoniales/{id}

	// TODO: PUT /perfilesPatrimoniales/{id}

	// TODO: DELETE /perfilesPatrimoniales/{id}

	// TODO: PATCH /perfilesPatrimoniales/{id}

	/* ========== Personas Api Controller ========== */

	// GET /personas
	public static Futuro<Persona> persona(Contexto contexto, String idTipoDocumento, String documento, Boolean consultaCuil, String genero) {
		return futuro(() -> Persona.get(contexto, idTipoDocumento, documento, consultaCuil, genero));
	}

	public static Futuro<DatosBasicosPersonas> datosBasicos(Contexto contexto, String documento) {
		return futuro(() -> Persona.getDatosBasicos(contexto, documento));
	}

	// POST /personas
	public static Futuro<Persona> crearPersona(Contexto contexto, String cuit) {
		return futuro(() -> Persona.post(contexto, cuit));
	}

	// TODO: GET /personas/{id}
	public static Futuro<Persona> persona(Contexto contexto, String cuit) {
		return futuro(() -> Persona.get(contexto, cuit, true));
	}

	public static Futuro<Persona> persona(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Persona.get(contexto, cuit, cache));
	}

	// TODO: POST /personas/{id}

	// TODO: DELETE /personas/{id}

	// PATCH /personas/{id}
	public static Futuro<Persona> actualizarPersona(Contexto contexto, Persona persona) {
		return futuro(() -> Persona.patch(contexto, persona));
	}

	// GET /personas/{id}/domicilios
	public static Futuro<Domicilios> domicilios(Contexto contexto, String cuit) {
		return futuro(() -> Domicilios.get(contexto, cuit, true));
	}

	public static Futuro<Domicilios> domicilios(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Domicilios.get(contexto, cuit, cache));
	}

	// POST /personas/{id}/domicilios
	public static Futuro<Domicilio> crearDomicilio(Contexto contexto, String cuit, NuevoDomicilio domicilio, String tipo) {
		return futuro(() -> Domicilios.post(contexto, cuit, domicilio, tipo));
	}

	// TODO: GET /personas/{id}/gastosPatrimoniales

	// TODO: POST /personas/{id}/gastosPatrimoniales

	// GET /personas/{id}/mails
	public static Futuro<Emails> emails(Contexto contexto, String cuit) {
		return futuro(() -> Emails.get(contexto, cuit, true));
	}

	public static Futuro<Emails> emails(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Emails.get(contexto, cuit, cache));
	}

	// POST /personas/{id}/mails
	public static Futuro<Email> crearEmail(Contexto contexto, String cuit, String email, String tipo) {
		return futuro(() -> Emails.post(contexto, cuit, email, tipo));
	}

	// TODO: GET /personas/{id}/perfilesPatrimoniales

	// TODO: POST /personas/{id}/perfilesPatrimoniales

	// TODO: GET /personas/{id}/preguntas

	// TODO: POST /personas/{id}/preguntas

	// TODO: GET /personas/{id}/prestamosPatrimoniales

	// TODO: POST /personas/{id}/prestamosPatrimoniales

	// TODO: GET /personas/{id}/referencias

	// TODO: POST /personas/{id}/referencias

	// GET /personas/{id}/relaciones
	public static Futuro<Relaciones> relaciones(Contexto contexto, String cuit) {
		return futuro(() -> Relaciones.get(contexto, cuit, true));
	}

	public static Futuro<Relaciones> relaciones(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Relaciones.get(contexto, cuit, cache));
	}

	// POST /personas/{id}/relaciones
	public static Futuro<Relacion> crearRelacion(Contexto contexto, String cuit, NuevaRelacion relacion, String tipo) {
		return futuro(() -> Relaciones.post(contexto, cuit, relacion, tipo));
	}

	public static Futuro<Relacion> crearRelacion(Contexto contexto, String cuit, String cuitRelacion, String tipo) {
		return futuro(() -> Relaciones.post(contexto, cuit, cuitRelacion, tipo));
	}

	// TODO: GET /personas/{id}/tarjetasPatrimoniales

	// TODO: POST /personas/{id}/tarjetasPatrimoniales

	// GET /personas/{id}/telefonos
	public static Futuro<Telefonos> telefonos(Contexto contexto, String cuit) {
		return futuro(() -> Telefonos.get(contexto, cuit, true));
	}

	public static Futuro<Telefonos> telefonos(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> Telefonos.get(contexto, cuit, cache));
	}

	// POST /personas/{id}/telefonos
	public static Futuro<Telefono> crearTelefono(Contexto contexto, String cuit, NuevoTelefono telefono, String tipo) {
		return futuro(() -> Telefonos.post(contexto, cuit, telefono, tipo));
	}

	/* ========== Preguntas Api Controller ========== */

	// TODO: GET /preguntas/{id}

	// TODO: POST /preguntas/{id}

	// TODO: PUT /preguntas/{id}

	// TODO: DELETE /preguntas/{id}

	// TODO: PATCH /preguntas/{id}

	/* ========== Prestamos Patrimoniales Api Controller ========== */

	// TODO: GET /prestamosPatrimoniales/{id}

	// TODO: POST /prestamosPatrimoniales/{id}

	// TODO: PUT /prestamosPatrimoniales/{id}

	// TODO: DELETE /prestamosPatrimoniales/{id}

	// TODO: PATCH /prestamosPatrimoniales/{id}

	/* ========== Procrear Gas Api Controller ========== */

	// TODO: PATCH /procrearGas/interesadoPG

	// TODO: GET /procrearGas/solicitudes/{id}

	// TODO: PATCH /procrearGas/solicitudPG

	// TODO: GET /procrearGas/tipoInteresado/{id}

	/* ========== Referencias Api Controller ========== */

	// TODO: GET /referencias

	// TODO: GET /referencias/{id}

	// TODO: POST /referencias/{id}

	// TODO: PUT /referencias/{id}

	// TODO: DELETE /referencias/{id}

	// TODO: PATCH /referencias/{id}

	/* ========== Relaciones Api Controller ========== */

	// TODO: GET /relaciones/{id}

	// TODO: POST /relaciones/{id}

	// TODO: PUT /relaciones/{id}

	// TODO: DELETE /relaciones/{id}

	// PATCH /relaciones/{id}
	public static Futuro<Relacion> actualizarRelacion(Contexto contexto, String cuit, Relacion relacion) {
		return futuro(() -> Relaciones.patch(contexto, cuit, relacion));
	}

	/* ========== Rnv Consulta Respuesta Controller ========== */

	// GET /rnvconsulta
	public static Futuro<PreguntasRiesgoNet> preguntasRiesgoNet(Contexto contexto, PreguntasRiesgoNet.Request request) {
		return futuro(() -> PreguntasRiesgoNet.get(contexto, request));
	}

	// TODO: POST /rnvrespuesta

	/* ========== Tarjetas Patrimoniales Api Controller ========== */

	// TODO: GET /tarjetasPatrimoniales/{id}

	// TODO: POST /tarjetasPatrimoniales/{id}

	// TODO: PUT /tarjetasPatrimoniales/{id}

	// TODO: DELETE /tarjetasPatrimoniales/{id}

	// TODO: PATCH /tarjetasPatrimoniales/{id}

	/* ========== Telefonos Api Controller ========== */

	// TODO: GET /telefonos

	// TODO: GET /telefonos/{id}

	// TODO: POST /telefonos/{id}

	// TODO: PUT /telefonos/{id}

	// TODO: DELETE /telefonos/{id}

	// PATCH /telefonos/{id}
	public static Futuro<Telefono> actualizarTelefono(Contexto contexto, String cuit, Telefono telefono) {
		return futuro(() -> Telefonos.patch(contexto, cuit, telefono));
	}

	public static Futuro<Telefono> borrarTelefono(Contexto contexto, String cuit, Telefono telefono) {
		return futuro(() -> Telefonos.delete(contexto, cuit, telefono));
	}

	/* ========== CampaniasVigentesWOCI ========== */

	// TODO: GET /campaniaVigente

	/* ========== ConsultaSegmentacionVLC ========== */

	// TODO: GET /segmentacionVLC

	// datavalid

	public static Futuro<DataValids> obtenerDataValid(Contexto contexto, String cuit, Boolean cache) {
		return futuro(() -> DataValids.get(contexto, cuit, cache));
	}

	public static Futuro<Boolean> datavalidOtpTel(Contexto contexto, String xusuario, String cuit, int secTel, int secDir) {
		return futuro(() -> DataValids.postOtpTel(contexto, xusuario, cuit, secTel, secDir));
	}

	public static Futuro<Boolean> datavalidOtpEmail(Contexto contexto, String xusuario, String cuit, int secMail, String tipoMail) {
		return futuro(() -> DataValids.postOtpEmail(contexto, xusuario, cuit, secMail, tipoMail));
	}

	public static Futuro<DocumentoCuestionado> consultaDocCuestionado(Contexto contexto, String nroDoc, String sexo) {
		return futuro(() -> DocumentoCuestionados.get(contexto, nroDoc, sexo));
	}

}
