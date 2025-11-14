package ar.com.hipotecario.backend.servicio.api.seguridad;

import java.util.Base64;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class VU extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String codigo;
	public String mensaje;
	public String idOperacion;

	/* ========== REQUEST ========== */
	public static class RequestIniciarVU extends ApiObjeto {
		public String cuil;
		public String fabricanteDispositivo;
		public String hashDispositivo;
		public String nombreDispositivo;
		public Boolean rooteado;
		public String sistemaOperativo;
		public String versionAplicacion;
		public String versionSistemaOperativo;
	}

	public static class RequestCodigoBarras extends ApiObjeto {
		public String cuil;
		public String idOperacion;
		public String nombres;
		public String apellidos;
		public String numeroDocumento;
		public Fecha fechaNacimiento;
		public String genero;
		public String calle;
		public String ciudad;
		public String codigoPostal;
	}

	/* ========== SERVICIOS ========== */
	// API-Seguridad_IniciarOperacionIdentidad
	public static VU iniciar(Contexto contexto, RequestIniciarVU requestIniciarVU) {
		ApiRequest request = new ApiRequest("VuIniciar", "seguridad", "GET", "/v1/identidad/iniciar", contexto);
		request.header("x-usuario", requestIniciarVU.cuil);
		request.header("x-cuil", requestIniciarVU.cuil);
		request.header("x-vu", "true");
		request.query("fabricanteDispositivo", requestIniciarVU.fabricanteDispositivo);
		request.query("hashDispositivo", requestIniciarVU.hashDispositivo);
		request.query("nombreDispositivo", requestIniciarVU.nombreDispositivo);
		request.query("rooteado", requestIniciarVU.rooteado);
		request.query("sistemaOperativo", requestIniciarVU.sistemaOperativo);
		request.query("versionAplicacion", requestIniciarVU.versionAplicacion);
		request.query("versionSistemaOperativo", requestIniciarVU.versionSistemaOperativo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	// API-Seguridad_AgregarFrenteIdentidad
	public static VU frente(Contexto contexto, String cuil, String idOperacion, String foto, Boolean analizarAnomalias, Boolean analizarOcr) {
		ApiRequest request = new ApiRequest("VuFrente", "seguridad", "POST", "/v1/identidad/{idOperacion}/frente", contexto);
		request.header("x-usuario", cuil);
		request.header("x-cuil", cuil);
		request.header("x-vu", "true");
		request.path("idOperacion", idOperacion);
		request.body("analizarAnomalias", analizarAnomalias);
		request.body("analizarOcr", analizarOcr);
		request.body("foto", foto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	// API-Seguridad_AgregarCodigoBarrasIdentidad
	public static VU codigoBarras(Contexto contexto, RequestCodigoBarras requestCodigoBarras) {
		ApiRequest request = new ApiRequest("VuCodigoBarras", "seguridad", "POST", "/v1/identidad/{idOperacion}/codigobarras", contexto);
		request.header("x-usuario", requestCodigoBarras.cuil);
		request.header("x-cuil", requestCodigoBarras.cuil);
		request.header("x-vu", "true");
		request.path("idOperacion", requestCodigoBarras.idOperacion);
		request.body("documento.nombres", requestCodigoBarras.nombres);
		request.body("documento.apellidos", requestCodigoBarras.apellidos);
		request.body("documento.numero", requestCodigoBarras.numeroDocumento);
		request.body("documento.fechaNacimiento", requestCodigoBarras.fechaNacimiento.string("dd-MM-yyyy"));
		request.body("documento.genero", requestCodigoBarras.genero);
		request.body("direccion.calle", requestCodigoBarras.calle);
		request.body("direccion.ciudad", requestCodigoBarras.ciudad);
		request.body("direccion.codigoPostal", requestCodigoBarras.codigoPostal);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	// API-Seguridad_AgregarReversoIdentidad
	public static VU reverso(Contexto contexto, String cuil, String idOperacion, String foto, Boolean analizarAnomalias, Boolean analizarOcr) {
		ApiRequest request = new ApiRequest("VuReverso", "seguridad", "POST", "/v1/identidad/{idOperacion}/reverso", contexto);
		request.header("x-usuario", cuil);
		request.header("x-cuil", cuil);
		request.header("x-vu", "true");
		request.path("idOperacion", idOperacion);
		request.body("analizarAnomalias", analizarAnomalias);
		request.body("analizarOcr", analizarOcr);
		request.body("foto", foto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	// API-Seguridad_AgregarPruebaDeVidaDocumentoIdentidad
	public static VU pruebaVida(Contexto contexto, String idOperacion, String cuil, byte[] sn, byte[] sce, byte[] ss) {
		String base64sn = Base64.getEncoder().encodeToString(sn);
		String base64sce = Base64.getEncoder().encodeToString(sce);
		String base64ss = Base64.getEncoder().encodeToString(ss);

		Objeto fotos = new Objeto();
		fotos.add().set("foto", base64sn).set("tipoImagen", "SN");
		fotos.add().set("foto", base64sce).set("tipoImagen", "SCE");
		fotos.add().set("foto", base64ss).set("tipoImagen", "SS");

		ApiRequest request = new ApiRequest("VuPruebaVida", "seguridad", "POST", "/v1/identidad/{idOperacion}/pruebadevida", contexto);
		request.header("x-usuario", cuil);
		request.header("x-cuil", cuil);
		request.header("x-vu", "true");
		request.path("idOperacion", idOperacion);
		request.body("fotos", fotos);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	public static VU finalizar(Contexto contexto, String idOperacion, String cuil) {
		ApiRequest request = new ApiRequest("VuFinalizar", "seguridad", "GET", "/v1/identidad/{idOperacion}/finalizar", contexto);
		request.header("x-usuario", cuil);
		request.header("x-cuil", cuil);
		request.header("x-vu", "true");
		request.path("idOperacion", idOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VU.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("BB", "homologacion");
		String cuil = "27330207871";

		RequestIniciarVU requestIniciarVU = new RequestIniciarVU();
		requestIniciarVU.cuil = cuil;
		requestIniciarVU.fabricanteDispositivo = "Samsung";
		requestIniciarVU.hashDispositivo = "hash";
		requestIniciarVU.nombreDispositivo = "Samsung";
		requestIniciarVU.rooteado = false;
		requestIniciarVU.sistemaOperativo = "Android";
		requestIniciarVU.versionAplicacion = "1.0.0";
		requestIniciarVU.versionSistemaOperativo = "5.0";
		VU iniciar = iniciar(contexto, requestIniciarVU);
		imprimirResultado(contexto, iniciar);

		String frenteBase64 = new Archivo("C:/VU/1-frente.jpg").comprimirImagen(30).base64();
		VU frente = frente(contexto, cuil, iniciar.idOperacion, frenteBase64, true, true);
		imprimirResultado(contexto, frente);

		RequestCodigoBarras requestCodigoBarras = new RequestCodigoBarras();
		requestCodigoBarras.cuil = cuil;
		requestCodigoBarras.idOperacion = iniciar.idOperacion;
		requestCodigoBarras.nombres = "JULIETA";
		requestCodigoBarras.apellidos = "SOSA";
		requestCodigoBarras.numeroDocumento = "33020787";
		requestCodigoBarras.fechaNacimiento = new Fecha("05-06-1987", "dd-MM-yyyy");
		requestCodigoBarras.genero = "F";
		requestCodigoBarras.calle = "Moreno";
		requestCodigoBarras.ciudad = "CRUZ DEL EJE";
		requestCodigoBarras.codigoPostal = "5280";
		VU codigoBarras = codigoBarras(contexto, requestCodigoBarras);
		imprimirResultado(contexto, codigoBarras);

		String reversoBase64 = new Archivo("C:/VU/2-reverso.jpg").comprimirImagen(30).base64();
		VU reverso = reverso(contexto, cuil, iniciar.idOperacion, reversoBase64, true, true);
		imprimirResultado(contexto, reverso);
	}
}
