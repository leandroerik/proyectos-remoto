package ar.com.hipotecario.canal.homebanking.api;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
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

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Certificado;

public class HBSitioExterno {

	public static Respuesta vfhome(ContextoHB contexto) {
		ApiRequest request = Api.request("SeguridadGetVfNet", "seguridad", "GET", "/v1/tokenvfnet", contexto);
		request.query("idCliente", contexto.idCobis());

		ApiResponse response = Api.response(request);
		if (response.string("errorType").equals("TOKEN_USR_OPEN_SESSION")) {
			return Respuesta.estado("SESION_DUPLICADA");
		}
		if (response.hayError() || response.string("token").isEmpty()) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("url", ConfigHB.string("vfhome_url"));
		respuesta.set("token", response.string("token"));
		return respuesta;
	}

	public static Respuesta testVisahome(ContextoHB contexto) {
		String visaHome = "";
		visaHome += "Direccion-Cliente: " + "***" + "\n";
		visaHome += "expiración: " + "***" + "\n";
		visaHome += "Tipo-Documento: " + "***" + "\n";
		visaHome += "Documento: " + "***" + "\n";
		visaHome += "Numero-Cuenta: " + "***" + "\n";
		visaHome += "Tarjeta: " + "***" + "\n";
		visaHome += "Codigo-Banco: " + "***" + "\n";
		visaHome += "Menu-Permisos: " + "***";

		String tokenVisaHome = pkcs7(visaHome, Certificado.privateKey("visahome"), Certificado.x509("visahome"));

		Respuesta respuesta = new Respuesta();
		respuesta.set("token-visahome", tokenVisaHome);

		return respuesta;
	}

	public static Respuesta testTodoPago(ContextoHB contexto) {
		String todoPago = "";
		todoPago += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "\n";
		todoPago += "<Billetera xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "\n";
		todoPago += "  <datosUsuario>" + "\n";
		todoPago += "    <banco>044</banco>" + "\n";
		todoPago += "    <tipoDocumento>" + "***" + "</tipoDocumento>" + "\n";
		todoPago += "    <nroDocumento>" + "***" + "</nroDocumento>" + "\n";
		todoPago += "    <genero>" + "***" + "</genero>" + "\n";
		todoPago += "    <factorValidacion>S</factorValidacion>" + "\n";
		todoPago += "  </datosUsuario>" + "\n";
		todoPago += "  <datosFiliatorios>" + "\n";
		todoPago += "    <nombre>" + "***" + "</nombre>" + "\n";
		todoPago += "    <apellido>" + "***" + "</apellido>" + "\n";
		todoPago += "    <fechaNacimiento>" + "***" + "</fechaNacimiento>" + "\n";
		todoPago += "  </datosFiliatorios>" + "\n";
		todoPago += "  <cuentas>" + "\n";
		todoPago += "  </cuentas>" + "\n";
		todoPago += "  <mediosDePago>" + "\n";
		todoPago += "  </mediosDePago>" + "\n";
		todoPago += "</Billetera>" + "\n";

		String tokenTodoPago = pkcs7(todoPago, Certificado.privateKey("todopago"), Certificado.x509("todopago"));

		Respuesta respuesta = new Respuesta();
		respuesta.set("token-todopago", tokenTodoPago);

		return respuesta;
	}

	public static Respuesta visahome(ContextoHB contexto) {
		if (contexto.tarjetaCreditoTitular() == null) {
			return Respuesta.estado("SIN_TARJETA_CREDITO_TITULAR");
		}

		if (contexto.tarjetaDebitoHabilitadaLink() == null) {
			return Respuesta.estado("SIN_TARJETA_DEBITO_HABILITADA");
		}

		Calendar calendario = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		calendario.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE) + 5);
		String expiracion = sdf.format(calendario.getTime());

		String mensaje = "";
		mensaje += "Direccion-Cliente: " + contexto.ip() + "\n";
		mensaje += "expiración: " + expiracion + "\n";
		mensaje += "Tipo-Documento: " + contexto.persona().tipoDocumento() + "\n";
		mensaje += "Documento: " + contexto.persona().numeroDocumento() + "\n";
		mensaje += "Numero-Cuenta: " + contexto.tarjetaCreditoTitular().cuenta() + "\n";
		mensaje += "Tarjeta: " + contexto.tarjetaCreditoTitular().numero() + "\n";
		mensaje += "Codigo-Banco: " + "044" + "\n";
		mensaje += "Menu-Permisos: " + "FULL";

		Respuesta respuesta = new Respuesta();

		String token = pkcs7(mensaje, Certificado.privateKey("visahome"), Certificado.x509("visahome"));

		String url = ConfigHB.string("jks_visahome_url");
		respuesta.set("url", url);
		respuesta.set("token", token);

		return respuesta;
	}

	public static Respuesta visahometest(ContextoHB contexto) {
		String mensaje = "";
		mensaje += "Direccion-Cliente: " + "0" + "\n";
		mensaje += "expiración: " + "0" + "\n";
		mensaje += "Tipo-Documento: " + "0" + "\n";
		mensaje += "Documento: " + "0" + "\n";
		mensaje += "Numero-Cuenta: " + "0" + "\n";
		mensaje += "Tarjeta: " + "0" + "\n";
		mensaje += "Codigo-Banco: " + "044" + "\n";
		mensaje += "Menu-Permisos: " + "FULL";

		Respuesta respuesta = new Respuesta();

		String token = pkcs7(mensaje, Certificado.privateKey("visahome"), Certificado.x509("visahome"));

		String url = ConfigHB.string("jks_visahome_url");
		respuesta.set("url", url);
		respuesta.set("token", token);

		return respuesta;
	}

	public static Respuesta permitirSaltoVisaHome(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
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
