package ar.com.hipotecario.mobile.api;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Certificado;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class MBSitioExterno {

//	public static RespuestaMB tokenViejoHB(ContextoMB contexto) {
//		if (contexto.idCobis() == null) {
//			return RespuestaMB.estado("SIN_SESION");
//		}
//
//		if (!contexto.sesion().idCobis().equals(contexto.sesion().idCobisReal())) {
//			return RespuestaMB.estado("SIN_SESION");
//		}
//
//		SqlRequestMB sqlRequest = SqlMB.request("SelectUsuario", "clientes_operadores");
//		sqlRequest.sql = "SELECT * FROM [clientes-operadores].[dbo].[op_user] WHERE [clientId] = ?";
//		sqlRequest.parametros.add(contexto.idCobis());
//
//		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
//		if (sqlResponse.hayError) {
//			return RespuestaMB.error();
//		}
//
//		String usuario = "";
//		String tipoAdhesion = "C";
//		for (Objeto item : sqlResponse.registros) {
//			usuario = item.string("id");
//			tipoAdhesion = item.string("accessType");
//		}
//
//		Objeto objeto = new Objeto();
//		objeto.set("idCobis", contexto.idCobis());
//		objeto.set("usuario", !usuario.isEmpty() ? usuario : "GENERICO");
//		objeto.set("tipoAdhesion", tipoAdhesion);
//		objeto.set("expiracion", new Date().getTime() + 60 * 1000);
//
//		String json = objeto.toJson();
//
//		String clave = ConfigMB.string("configuracion_clave_salto_hb", null);
//		String iv = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
//		String jsonEncriptado = Encriptador.encriptarAES("MD5", clave, iv, json);
//		String token = iv + ":" + jsonEncriptado;
//
//		RespuestaMB respuesta = new RespuestaMB();
//		respuesta.set("url", ConfigMB.string("configuracion_url_salto_hb"));
//		respuesta.set("token", token);
//		return respuesta;
//	}

	public static RespuestaMB vfhome(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("SeguridadGetVfNet", "seguridad", "GET", "/v1/tokenvfnet", contexto);
		request.query("idCliente", contexto.idCobis());

		ApiResponseMB response = ApiMB.response(request);
		if (response.string("errorType").equals("TOKEN_USR_OPEN_SESSION")) {
			return RespuestaMB.estado("SESION_DUPLICADA");
		}
		if (response.hayError() || response.string("token").isEmpty()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("url", ConfigMB.string("vfhome_url"));
		respuesta.set("token", response.string("token"));
		return respuesta;
	}

	public static RespuestaMB todopago(ContextoMB contexto) {
		if (contexto.tarjetaDebitoHabilitadaLink() == null) {
			return RespuestaMB.estado("SIN_TARJETA_DEBITO_HABILITADA");
		}

		String mensaje = "";
		mensaje += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "\n";
		mensaje += "<Billetera xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "\n";
		mensaje += "  <datosUsuario>" + "\n";
		mensaje += "    <banco>044</banco>" + "\n";
		mensaje += "    <tipoDocumento>" + contexto.persona().tipoDocumento() + "</tipoDocumento>" + "\n";
		mensaje += "    <nroDocumento>" + contexto.persona().numeroDocumento() + "</nroDocumento>" + "\n";
		mensaje += "    <genero>" + contexto.persona().idSexo() + "</genero>" + "\n";
		mensaje += "    <factorValidacion>S</factorValidacion>" + "\n";
		mensaje += "  </datosUsuario>" + "\n";
		mensaje += "  <datosFiliatorios>" + "\n";
		mensaje += "    <nombre>" + contexto.persona().nombres().toUpperCase() + "</nombre>" + "\n";
		mensaje += "    <apellido>" + contexto.persona().apellidos().toUpperCase() + "</apellido>" + "\n";
		mensaje += "    <fechaNacimiento>" + contexto.persona().fechaNacimiento("yyyy-MM-dd") + "</fechaNacimiento>" + "\n";
		mensaje += "  </datosFiliatorios>" + "\n";
		mensaje += "  <cuentas>" + "\n";
		for (Cuenta cuenta : contexto.cuentas()) {
			mensaje += "    " + "<cuenta>" + "\n";
			mensaje += "      " + (cuenta.esCajaAhorro() ? "<tipoCuenta>CAJA_AHORRO</tipoCuenta>" : "<tipoCuenta>CTA_CTE</tipoCuenta>") + "\n";
			mensaje += "      " + ("80".equals(cuenta.idMoneda()) ? "<monedaCuenta>032</monedaCuenta>" : "<monedaCuenta>840</monedaCuenta>") + "\n";
			mensaje += "      " + "<numeroCuenta>" + cuenta.numero() + "</numeroCuenta>" + "\n";
			mensaje += "      " + "<cbu>" + cuenta.cbu() + "</cbu>" + "\n";
			mensaje += "      " + "<cuit>" + contexto.persona().cuit() + "</cuit>" + "\n";
			mensaje += "    " + "</cuenta>" + "\n";
		}
		mensaje += "  </cuentas>" + "\n";
		mensaje += "  <mediosDePago>" + "\n";
		for (TarjetaDebito td : contexto.tarjetasDebito()) {
			mensaje += "    " + "<medioDePago>" + "\n";
			mensaje += "      " + "<codigoMedioDePago>43</codigoMedioDePago>" + "\n";
			mensaje += "      " + "<numeroTarjeta>" + td.numero() + "</numeroTarjeta>" + "\n";
			mensaje += "      " + "<fechaVto>" + td.fechaVencimiento("MMyyyy", "") + "</fechaVto>" + "\n";
			mensaje += "    " + "</medioDePago>" + "\n";
		}
		for (TarjetaCredito tc : contexto.tarjetasCredito()) {
			mensaje += "    " + "<medioDePago>" + "\n";
			mensaje += "      " + "<codigoMedioDePago>42</codigoMedioDePago>" + "\n";
			mensaje += "      " + "<numeroTarjeta>" + tc.numero() + "</numeroTarjeta>" + "\n";
			mensaje += "      " + "<fechaVto>" + tc.fechaVencimiento("MMyyyy") + "</fechaVto>" + "\n";
			mensaje += "    " + "</medioDePago>" + "\n";
		}
		mensaje += "  </mediosDePago>" + "\n";
		mensaje += "</Billetera>" + "\n";

		String token = pkcs7(mensaje, Certificado.privateKey("todopago"), Certificado.x509("todopago"));

		RespuestaMB respuesta = new RespuestaMB();
		String url = ConfigMB.string("jks_todopago_url");
		respuesta.set("url", url);
		respuesta.set("version", "2");
		respuesta.set("token", token);

		return respuesta;
	}

	public static RespuestaMB permitirSaltoVisaHome(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("permitirSalto", true);

		if (contexto.tarjetaCreditoTitular() == null) {
			respuesta.set("permitirSalto", false);
			respuesta.set("motivo", "SIN_TARJETA_CREDITO_TITULAR");
		}

		if (contexto.tarjetaDebitoPorDefecto() == null) {
			respuesta.set("permitirSalto", false);
			respuesta.set("motivo", "SIN_TARJETA_DEBITO_HABILITADA");
		}

		return respuesta;
	}

	public static RespuestaMB permitirSaltoTodoPago(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("permitirSalto", true);

		if (contexto.tarjetaDebitoPorDefecto() == null) {
			respuesta.set("permitirSalto", false);
			respuesta.set("motivo", "SIN_TARJETA_DEBITO_HABILITADA");
		}

		return respuesta;
	}

	/* ========== UTIL ========== */
	private static String pkcs7(String mensaje, PrivateKey privateKey, X509Certificate x509certificate) {
		try {
			byte[] signedMessage = null;
			List<X509Certificate> certList = new ArrayList<X509Certificate>();
			CMSTypedData cmsData = new CMSProcessableByteArray(mensaje.getBytes());
			certList.add(x509certificate);
			JcaCertStore certs = new JcaCertStore(certList);

			CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
			ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
			DigestCalculatorProvider dcp = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
			SignerInfoGenerator sig = new JcaSignerInfoGeneratorBuilder(dcp).build(contentSigner, x509certificate);
			cmsGenerator.addSignerInfoGenerator(sig);
			cmsGenerator.addCertificates(certs);

			CMSSignedData cms = cmsGenerator.generate(cmsData, true);
			signedMessage = cms.getEncoded();
			String base64signedData = Base64.getEncoder().encodeToString(signedMessage);
			return base64signedData;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
