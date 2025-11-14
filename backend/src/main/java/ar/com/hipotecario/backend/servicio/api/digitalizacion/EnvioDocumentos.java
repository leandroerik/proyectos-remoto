package ar.com.hipotecario.backend.servicio.api.digitalizacion;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

public class EnvioDocumentos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String idDocumento;
	public String bytesDocumento;
	public String claseDocumental;
	public Propiedades propiedades;

	public static class Propiedades extends ApiObjeto {
		public String DocumentTitle;
		public String CUIL;
		public String ApellidoyNombre;
		public String NroSolicitud;
		public String DNI;
		public String OrigenDelAlta;
		public String TipoPersona;
		public String ExtArchivo;
		public String mimetype;
	}

	public static class NuevoEnvioDocumento {
		public String idDocumento;
		public String bytesDocumento;
		public String claseDocumental;
		public Propiedades propiedades;
	}

	/* ========== SERVICIOS ========== */
	public static EnvioDocumentos post(Contexto contexto, NuevoEnvioDocumento nuevoDocumento) {

		ApiRequest request = new ApiRequest("EnvioDocumento", "digitalizacion", "POST", "/v1/documentos/{cuil}", contexto);
		request.path("cuil", nuevoDocumento.propiedades.CUIL);
		request.body("bytesDocumento", nuevoDocumento.bytesDocumento);
		request.body("claseDocumental", nuevoDocumento.claseDocumental);
		request.body("propiedades", nuevoDocumento.propiedades);
		
		if(GeneralBB.CLASE_DOCUMENTAL_FE_VIDA.equals(nuevoDocumento.claseDocumental)){
			Objeto propiedades = new Objeto();
			propiedades.set("DocumentTitle", nuevoDocumento.propiedades.DocumentTitle);
			propiedades.set("CUIL", nuevoDocumento.propiedades.CUIL);
			propiedades.set("ApellidoyNombre", nuevoDocumento.propiedades.ApellidoyNombre);
			propiedades.set("NroTramiteWKF", nuevoDocumento.propiedades.NroSolicitud);
			propiedades.set("DNI", nuevoDocumento.propiedades.DNI);
			propiedades.set("OrigenDelAlta", nuevoDocumento.propiedades.OrigenDelAlta);
			propiedades.set("TipoPersona", nuevoDocumento.propiedades.TipoPersona);
			propiedades.set("ExtArchivo", nuevoDocumento.propiedades.ExtArchivo);
			propiedades.set("mimetype", nuevoDocumento.propiedades.mimetype);
			request.body("propiedades", propiedades);
		}
		
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioDocumentos.class, response);
	}

	public static EnvioDocumentos get(Contexto contexto, String idDocumento) {

		ApiRequest request = new ApiRequest("ConsultarDocumento", "digitalizacion", "GET", "/v1/documentos/{{idDocumento}}", contexto);
		request.path("idDocumento", idDocumento);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioDocumentos.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "desarrollo");
		String test = "post Fe de Vida";

		if (test.equals("post")) {
			NuevoEnvioDocumento nuevoEnvioDocumento = new NuevoEnvioDocumento();

			nuevoEnvioDocumento.bytesDocumento = "PruebaPrevencionLavadoDinero";
			nuevoEnvioDocumento.claseDocumental = "PrevencionLavadoH";

			Propiedades propiedades = new Propiedades();
			propiedades.DocumentTitle = "PrevencionLavadoH-27295620833";
			propiedades.CUIL = "27295620833";
			propiedades.ApellidoyNombre = "PRUCCA CINTIA ALEJANDRA";
			propiedades.DNI = "29562083";
			propiedades.OrigenDelAlta = "BUHOBANK";
			propiedades.NroSolicitud = "10203630";
			propiedades.TipoPersona = "F";
			propiedades.ExtArchivo = "pdf";
			propiedades.mimetype = "application/pdf";

			nuevoEnvioDocumento.propiedades = propiedades;

			EnvioDocumentos datos = post(contexto, nuevoEnvioDocumento);
			imprimirResultado(contexto, datos);
		} else if (test.equals("get")) {
			String idDocumento = "383B8CC7-821C-C788-8E24-89C082300000";

			EnvioDocumentos datos = get(contexto, idDocumento);
			imprimirResultado(contexto, datos);
		} else if (test.equals("post Fe de Vida")) {
			NuevoEnvioDocumento nuevoEnvioDocumento = new NuevoEnvioDocumento();

			nuevoEnvioDocumento.bytesDocumento = "PruebaFeDeVida1245Prueba";
			nuevoEnvioDocumento.claseDocumental = "FeDeVida";

			Propiedades propiedades = new Propiedades();
			propiedades.DocumentTitle = "FeDeVida-27295620833";
			propiedades.CUIL = "27295620833";
			propiedades.ApellidoyNombre = "PRUCCA CINTIA ALEJANDRA";
			propiedades.DNI = "29562083";
			propiedades.OrigenDelAlta = "BUHOBANK";
			propiedades.NroSolicitud = "10203630";
			propiedades.TipoPersona = "F";
			propiedades.ExtArchivo = "png";
			propiedades.mimetype = "application/png";

			nuevoEnvioDocumento.propiedades = propiedades;

			post(contexto, nuevoEnvioDocumento);
		}
	}
}
