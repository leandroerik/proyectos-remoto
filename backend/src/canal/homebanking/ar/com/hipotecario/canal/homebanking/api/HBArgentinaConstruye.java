package ar.com.hipotecario.canal.homebanking.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Base64;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.DataFile;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Archivo;
import ar.com.hipotecario.canal.homebanking.lib.Imagen;
import ar.com.hipotecario.canal.homebanking.servicio.RestArchivo;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudPrestamo;

public class HBArgentinaConstruye {

	/* ========== SERVICIOS ========== */
	public static Object terminosCondiciones(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String nemonico = contexto.parametros.string("nemonico");

		if (Objeto.anyEmpty(idSolicitud, nemonico)) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.parametrosIncorrectos();
		}

		ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupocodigo", nemonico.replace("GREFACTYC", "GREFA2021"));
		request.query("canal", "HB");

		if (nemonico.equals("PPPROMATE2") || nemonico.equals("GREFACTYC")) {
			request.header("x-cuil", contexto.persona().cuit());
			try {
				request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
			} catch (Exception e) {
				request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
			}
			request.header("x-tipoPersona", "F");
			request.header("x-dni", contexto.persona().numeroDocumento());
			request.header("x-producto", nemonico.replace("GREFACTYC", "GREFA2021"));
		}

		ApiResponse response = Api.response(request, idSolicitud, nemonico, contexto.idCobis());
		if (response.hayError()) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.error();
		}

		String base64 = response.string("Data");
		byte[] archivo = Base64.getDecoder().decode(base64);
		if (base64.isEmpty() && !ConfigHB.esOpenShift() && !ConfigHB.esProduccion()) {
			archivo = ejemplo();
		}
		contexto.responseHeader("estado", "0");
		contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");

		Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
		if (solicitud.contienePrestamo()) {
			SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
			if (prestamo.esProcrearRefaccion()) {
				String path = HBProcrearRefaccion.rutaTemporal(contexto, idSolicitud);
				path = path + "1100" + ".pdf";
				Archivo.escribirBinario(path, archivo);
			}
		}

		return archivo;
	}

	public static Respuesta subirDocumentacion(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud", "");
		DataFile dniFrente = contexto.archivos.get("dniFrente");
		DataFile dniDorso = contexto.archivos.get("dniDorso");

		if (Objeto.anyEmpty(dniFrente, dniDorso)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (dniFrente != null) {
			byte[] archivo = dniFrente.bytes;
			byte[] archivoComprimido = Imagen.comprimir(archivo, dniFrente.name, 0.80f);
			ApiResponse response = RestArchivo.subirDni(contexto, dniFrente.name, archivoComprimido, idSolicitud);
			if (response.hayError()) {
				return Respuesta.error();
			}
		}

		if (dniDorso != null) {
			byte[] archivo = dniDorso.bytes;
			byte[] archivoComprimido = Imagen.comprimir(archivo, dniDorso.name, 0.80f);
			ApiResponse response = RestArchivo.subirDni(contexto, dniDorso.name, archivoComprimido, idSolicitud);
			if (response.hayError()) {
				return Respuesta.error();
			}
		}

		return Respuesta.exito();
	}

	/* ========== AUXILIAR ========== */
	public static ApiResponse ofertaArgentinaConstruye(ContextoHB contexto) {
		Integer plazoParametro = contexto.parametros.integer("plazo");

		if (plazoParametro == null) {
			plazoParametro = 60;
		}

		ApiRequest request = Api.request("OfertaArgentinaConstruye", "prestamos", "GET", "/v1/prestamos/{id}/beneficiario", contexto);
		request.path("id", contexto.idCobis());
		request.query("Tipo", "C");
		request.cacheSesion = true;
//		request.dummy = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
//		if(response.esLista()) {
//			response = new ApiResponse(request, response.codigo, response.objetos().get(0).toJson());
//		}

		ApiResponse responseAux = new ApiResponse();
		for (Objeto item : response.objetos()) {
			if (item.string("estado").equals("SO")) {
				String nemonicoActual = item.string("nemonico");
				if ("PPPROMATE2".equals(nemonicoActual)) {
					if (plazoParametro != null && !item.integer("plazo").equals(plazoParametro)) {
						continue;
					}
					responseAux = new ApiResponse(request, response.codigo, item.toJson());
				}
			}
		}
		return responseAux;
	}

	public static Boolean esBatch() {
		Boolean esBatch = false;

		SqlRequest sqlRequest = Sql.request("BatchArgentinaConstruye", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[parametros] WHERE nombre_parametro = 'argentina_construye_batch'";

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.registros.isEmpty()) {
			setBatch(false);
		} else {
			for (Objeto registros : sqlResponse.registros) {
				esBatch = Objeto.setOf("1", "true").contains(registros.string("valor"));
			}
		}

		return esBatch;
	}

	public static Boolean setBatch(Boolean valor) {
		SqlRequest sqlRequest = Sql.request("BatchArgentinaConstruye", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[parametros] WHERE nombre_parametro = 'argentina_construye_batch'";
		SqlResponse sqlResponse = Sql.response(sqlRequest);

		if (sqlResponse.registros.isEmpty()) {
			sqlRequest = Sql.request("CreateBatchArgentinaConstruye", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[parametros] (nombre_parametro, valor, comentario, valor_long) VALUES (?, ?, ?, ?)";
			sqlRequest.parametros.add("argentina_construye_batch");
			sqlRequest.parametros.add(valor.toString());
			sqlRequest.parametros.add("");
			sqlRequest.parametros.add(null);
			sqlResponse = Sql.response(sqlRequest);
		} else {
			sqlRequest = Sql.request("UpdateBatchArgentinaConstruye", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[parametros] SET valor = ?";
			sqlRequest.parametros.add(valor);
			sqlResponse = Sql.response(sqlRequest);
		}
		return sqlResponse.hayError;
	}

	/* ========== DUMMY ========== */
	public static byte[] ejemplo() {
		ByteArrayOutputStream buffer = null;
		try {
			InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/ejemplo.pdf");
			buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		byte[] archivo = buffer.toByteArray();
		return archivo;
	}
}
