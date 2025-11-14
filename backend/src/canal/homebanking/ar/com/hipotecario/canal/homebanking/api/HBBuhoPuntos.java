package ar.com.hipotecario.canal.homebanking.api;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Http;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HBBuhoPuntos {

	public static OkHttpClient http = ConfigHB.bool("enabled_http_proxy", false) ? Http.okHttpClientProxy() : Http.okHttpClient();

	public static Respuesta consolidada(ContextoHB contexto) {

		//Puntos mockeados para desa y homo
		if (ConfigHB.esHomologacion() || ConfigHB.esDesarrollo()) {
			
			return getConsolidadaMock(contexto);

		}
		ApiRequest request = Api.request("BuhoPuntos", "tarjetascredito", "GET", "/v1/{idcliente}/puntos", contexto);
		request.path("idcliente", contexto.idCobis());

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}
		if (response.codigo == 204) {
			return Respuesta.estado("SIN_CUENTA").set("url", ConfigHB.string("buhopuntos_url"));
		}
		Respuesta respuesta = new Respuesta();
		respuesta.set("url", ConfigHB.string("buhopuntos_url"));
		respuesta.set("puntos", response.integer("puntosDisponibles"));
		respuesta.set("puntosFormateados", Formateador.importe(response.bigDecimal("puntosDisponibles")).replace(",00", ""));
		if (response.integer("puntosAvencer") != null) {
			respuesta.set("puntosAVencer", response.integer("puntosAvencer"));
			respuesta.set("puntosAVencerFormateados", Formateador.importe(response.bigDecimal("puntosAvencer")).replace(",00", ""));
			try {
				String dia = "01";
				String mes = response.string("fechaVtoPuntos").substring(4, 6);
				String anio = response.string("fechaVtoPuntos").substring(0, 4);
				Date fecha = new SimpleDateFormat("ddMMyyyy").parse(dia + mes + anio);
				fecha = Fecha.sumarMeses(fecha, 1);
				fecha = Fecha.restarDias(fecha, 1L);
				respuesta.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yy").format(fecha));
			} catch (Exception e) {
				respuesta.set("fechaVencimiento", "***");
			}

			respuesta.set("tienePuntosAVencer", true);
		} else {
			respuesta.set("puntosAVencer", 0);
			respuesta.set("puntosAVencerFormateados", "0");
			respuesta.set("fechaVencimiento", "***");
			respuesta.set("tienePuntosAVencer", false);
		}
		respuesta.set("url", ConfigHB.string("buhopuntos_url"));

		return respuesta;
	}

	public static Respuesta token(ContextoHB contexto) {

		// Consulta Puntos
		ApiRequest request = Api.request("BuhoPuntos", "tarjetascredito", "GET", "/v1/{idcliente}/puntos", contexto);
		request.path("idcliente", contexto.idCobis());

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}
		if (response.codigo == 204) {
			return Respuesta.estado("SIN_CUENTA").set("url", ConfigHB.string("buhopuntos_url"));
		}

		// Token
		String claveHmac = ConfigHB.string("buhopuntos_claveHMAC");
		Boolean datosExtras = ConfigHB.bool("buhopuntos_datosextras", false);

		String datos = contexto.idCobis();
		datos += "|" + contexto.persona().nombreEscapado();
		datos += "|" + contexto.persona().apellidosEscapado();
		datos += "|" + response.string("puntosDisponibles");
		if (datosExtras) {
			ApiResponse segmento = RestPersona.segmentacion(contexto);
			ApiResponse convenio = RestPersona.convenios(contexto);
			datos += "|" + (!segmento.hayError() && segmento.objetos().size() > 0 ? segmento.objetos().get(0).string("idPaquete", "-") : "-");
			datos += "|" + (!segmento.hayError() && segmento.objetos().size() > 0 ? segmento.objetos().get(0).string("idSegmentoRenta", "-") : "-");
			datos += "|" + (!segmento.hayError() && segmento.objetos().size() > 0 ? segmento.objetos().get(0).string("idSegmentoComercial", "-") : "-");
			datos += "|" + (!convenio.hayError() ? convenio.string("codigoConvenio", "-") : "-");
			datos += "|" + (contexto.persona().email() != null ? contexto.persona().email() : "-");
		}
		datos = datos.replace("ñ", "n").replace("Ñ", "N");
		datos = datos.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
		datos = datos.replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U");
		datos += "|" + hmacDigest(datos, claveHmac, "HmacSHA256");
		String base64 = Base64.getEncoder().encodeToString(datos.getBytes());

		// Invocacion
		String url = ConfigHB.string("buhopuntos_url") + "/i";

		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		FormBody.Builder formBodyBuilder = new FormBody.Builder();
		formBodyBuilder.add("d", base64);
		FormBody formBody = formBodyBuilder.build();

		Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(url);
		for (String clave : headers.keySet()) {
			requestBuilder.addHeader(clave, headers.get(clave));
		}
		requestBuilder.post(formBody);
		Request requestPyP = requestBuilder.build();

		String token = "";
		try (Response responsePyP = http.newCall(requestPyP).execute()) {
			token = responsePyP.body().string();
			if (!token.startsWith("http")) {
				return Respuesta.error();
			}
		} catch (Exception e) {
			return Respuesta.error();
		}

		return Respuesta.exito().set("url", token);
	}

	/* ========== UTIL ========== */
	public static String hmacDigest(String msg, String keyString, String algo) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);

			byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		return digest;
	}
	
	//======================== Inicio Mock para pruebas DESA/HOMO ========================
	private static SqlResponse consultaPuntosMock(ContextoHB contexto) {
		SqlResponse sqlResponse;
		SqlRequest sqlRequest = Sql.request("SelectPuntos", "mobile");
		String select = "SELECT id,"
				+ "    url,"
				+ "    puntos,"
				+ "    puntosFormateados,"
				+ "    puntosAVencer,"
				+ "    puntosAVencerFormateados,"
				+ "    FORMAT(fechaVencimiento, 'dd/MM/yyyy') AS fechaVencimiento,"
				+ "    tienePuntosAVencer,"
				+ "    tieneCuenta,"
				+ "    id_cobis ";
		sqlRequest.sql = select;
		sqlRequest.sql += " FROM Mobile.dbo.buhopuntos_mock ";
		sqlRequest.sql += "WHERE id_cobis = ? ";

		sqlRequest.add(Integer.valueOf(contexto.idCobis()));

		sqlResponse = Sql.response(sqlRequest);

		return sqlResponse;
	}
	
	private static Respuesta setConsolidadaMock(Objeto item) {
		Respuesta propuesta = new Respuesta();
		propuesta.set("id", item.integer("id"));
		propuesta.set("puntos", item.integer("puntos"));
		propuesta.set("puntosFormateados", item.string("puntosFormateados"));
		propuesta.set("puntosAVencer", item.integer("puntosAVencer"));
		propuesta.set("puntosAVencerFormateados", item.string("puntosAVencerFormateados"));
		propuesta.set("fechaVencimiento", item.string("fechaVencimiento"));
		propuesta.set("tienePuntosAVencer", item.bool("tienePuntosAVencer"));
		propuesta.set("tieneCuenta", item.bool("tieneCuenta"));

		return propuesta;
	}
	
	public static void updatePuntosMock(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("UpdatePuntos", "mobile");
		
		sqlRequest.sql = "UPDATE bpm SET  bpm.puntos = bpm.puntos - " + contexto.parametros.integer("puntosacanjear") + ",";
		sqlRequest.sql += " bpm.puntosFormateados = CAST(bpm.puntos - " + contexto.parametros.integer("puntosacanjear") + " AS VARCHAR) ";
		sqlRequest.sql += " FROM Mobile.dbo.buhopuntos_mock bpm ";
		sqlRequest.sql += "WHERE id_cobis = ? ";

		sqlRequest.add(Integer.valueOf(contexto.idCobis()));

		Sql.response(sqlRequest);
	}
	
	private static Respuesta getConsolidadaMock(ContextoHB contexto) {
		
		SqlResponse sql = consultaPuntosMock(contexto);
		Respuesta respuesta = setConsolidadaMock(sql.registros.get(0));
		
		return respuesta;
	}
	
	//======================== Fin Mock para pruebas DESA/HOMO ========================
}
