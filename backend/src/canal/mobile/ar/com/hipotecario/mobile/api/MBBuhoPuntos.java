package ar.com.hipotecario.mobile.api;

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

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Http;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MBBuhoPuntos {

	public static OkHttpClient http = Http.okHttpClient();

	public static RespuestaMB consolidada(ContextoMB contexto) {

		//Puntos mockeados para desa y homo
		if (ConfigMB.esHomologacion() || ConfigMB.esDesarrollo()) {
			
			return getConsolidadaMock(contexto);
			
		}
		ApiRequestMB request = ApiMB.request("BuhoPuntos", "tarjetascredito", "GET", "/v1/{idcliente}/puntos", contexto);
		request.path("idcliente", contexto.idCobis());

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		if (response.codigo == 204) {
			return RespuestaMB.estado("SIN_CUENTA").set("url", ConfigMB.string("buhopuntos_url"));
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("url", ConfigMB.string("buhopuntos_url"));
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
		respuesta.set("url", ConfigMB.string("buhopuntos_url"));

		return respuesta;
	}

	public static RespuestaMB token(ContextoMB contexto) {

		// Consulta Puntos
		ApiRequestMB request = ApiMB.request("BuhoPuntos", "tarjetascredito", "GET", "/v1/{idcliente}/puntos", contexto);
		request.path("idcliente", contexto.idCobis());

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		if (response.codigo == 204) {
			return RespuestaMB.estado("SIN_CUENTA").set("url", ConfigMB.string("buhopuntos_url"));
		}

		// Token
		String claveHmac = ConfigMB.string("buhopuntos_claveHMAC");
		Boolean datosExtras = ConfigMB.bool("buhopuntos_datosextras", false);

		String datos = contexto.idCobis();
		datos += "|" + contexto.persona().nombres();
		datos += "|" + contexto.persona().apellidos();
		datos += "|" + response.string("puntosDisponibles");
		if (datosExtras) {
			ApiResponseMB segmento = RestPersona.segmentacion(contexto);
			ApiResponseMB convenio = RestPersona.convenios(contexto);
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
		String url = ConfigMB.string("buhopuntos_url") + "/i";

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
				return RespuestaMB.error();
			}
		} catch (Exception e) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito().set("url", token);
	}

	public static RespuestaMB canjesBuhoPuntos(ContextoMB contexto) {
		ApiRequestMB requestHistoricoCanjes = ApiMB.request("BuhoPuntosCanjes", "prisma", "GET", "/v1/reclamosContainer", contexto);
		requestHistoricoCanjes.query("fechaAperturaDesde", contexto.parametros.date("fechaDesde", "d/M/yyyy", "M/d/yyyy", null));
		requestHistoricoCanjes.query("fechaAperturaHasta", contexto.parametros.date("fechaHasta", "d/M/yyyy", "M/d/yyyy", null));
		requestHistoricoCanjes.query("idCobis", contexto.idCobis());

		if (ConfigMB.esProduccion()) {
			return RespuestaMB.estado("FALTA_CONFIGURAR_RECLAMO_CANJES_BP");
		}

		if ("".equals(ConfigMB.string("reclamo_historico_canjes_bp"))) {
			return RespuestaMB.estado("FALTA_CONFIGURAR_RECLAMO_CANJES_BP");
		} else {
			requestHistoricoCanjes.query("idTema", ConfigMB.string("reclamo_historico_canjes_bp").split("_")[0]);
		}

		ApiResponseMB responseHistoricoCanjes = ApiMB.response(requestHistoricoCanjes, contexto.idCobis());
		if (responseHistoricoCanjes.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();

		Objeto canjes = new Objeto();

		for (Objeto item : responseHistoricoCanjes.objetos()) {
			Objeto canje = new Objeto();
			String idPremio = item.objetos("items").get(0).string("idPremio");
			// if (!idPremio.isEmpty()) {

			canje.set("idReclamo", item.string("idReclamo"));
			canje.set("idPremio", idPremio);
			canje.set("puntos", item.string("puntos"));
			canje.set("articulo", item.string("articulo"));
			canje.set("fechaRecepcion", item.objetos("items").get(0).date("fechaRecepcion", "yyyy-MM-dd", "dd/MM/yyyy"));

			canjes.add(canje);
			// }

			respuesta.set("historico", canjes.ordenar("orden"));

		}

		return respuesta;

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
	
	private static SqlResponseMB consultaPuntosMock(ContextoMB contexto) {
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("SelectPuntos", "mobile");
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

		sqlResponse = SqlMB.response(sqlRequest);
		
		return sqlResponse;
	}
	
	private static RespuestaMB setConsolidadaMock(Objeto item) {
		RespuestaMB propuesta = new RespuestaMB();
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
	
	public static void updatePuntosMock(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("UpdatePuntos", "mobile");

		sqlRequest.sql = "UPDATE bpm SET  bpm.puntos = bpm.puntos - " + contexto.parametros.integer("puntosacanjear") + ",";
		sqlRequest.sql += " bpm.puntosFormateados = CAST(bpm.puntos - " + contexto.parametros.integer("puntosacanjear") + " AS VARCHAR) ";
		sqlRequest.sql += " FROM Mobile.dbo.buhopuntos_mock bpm ";
		sqlRequest.sql += "WHERE id_cobis = ? ";

		sqlRequest.add(Integer.valueOf(contexto.idCobis()));
		
		SqlMB.response(sqlRequest);
	}
	
	private static RespuestaMB getConsolidadaMock(ContextoMB contexto) {
		SqlResponseMB sql = consultaPuntosMock(contexto);
		RespuestaMB respuesta = setConsolidadaMock(sql.registros.get(0));
		
		return respuesta;
	}
	
	//======================== Fin Mock para pruebas DESA/HOMO ========================

}
