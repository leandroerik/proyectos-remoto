package ar.com.hipotecario.backend.servicio.api.notificaciones;

import java.util.ArrayList;
import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BeneficiarioOB;
import org.apache.commons.lang3.StringUtils;

public class EnvioEmail extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String guid;

	/* ========== SERVICIOS ========== */
	// API-Notificaciones_EnvioCorreoElectronicoExterno
	private static EnvioEmail post(Contexto contexto, String de, String para, String plantilla, Objeto parametros) {
		ApiRequest request = new ApiRequest("EnvioEmail", "notificaciones", "POST", "/v1/correoelectronico", contexto);
		String xCobis = extraerXCobis(contexto);
		if (StringUtils.isNotBlank(xCobis)) {
			request.header("x-cobis", xCobis);
		}
		request.body("ip", contexto.ip());
		request.body("de", de);
		request.body("para", para);
		request.body("plantilla", plantilla);
		request.body("parametros", parametros);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioEmail.class, response);
	}

	private static EnvioEmail postSinPlantilla(Contexto contexto, String de, String para, String cc, String asunto, Objeto parametros, String html) {
		ApiRequest request = new ApiRequest("EnvioEmail", "notificaciones", "POST", "/v1/correoelectronico", contexto);
		request.body("token", "");
		request.body("de", de);
		request.body("para", para);
		request.body("asunto", asunto);
		request.body("plantilla", null);
		request.body("html", html);
		request.body("parametros", parametros);
		request.body("async", true);
		request.body("copiaCarbon", cc);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioEmail.class, response);
	}

	public static EnvioEmail postRecuperoUsuarioOB(Contexto contexto, String para, String nombre, String apellido, String usuario) {
		String plantilla = "ob_recupero_usuario";
		String asunto = "Office Banking - Recupero de usuario";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("USUARIO", Config.desencriptarAES(usuario));
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioOtpOB(Contexto contexto, String para, String nombre, String apellido, String clave) {
		String plantilla = "ob_envio_otp";
		String asunto = "Office Banking - Envio token";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("TOKEN", clave);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioOtpHB(Contexto contexto, String para, String clave) {
		String plantilla = "hb_envio_otp";
		String asunto = "Banco Hipotecario - Código de verificación";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("TOKEN", clave);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioOtpBB(Contexto contexto, String para, String otp, String nombre) {
		String plantilla = "bb_envio_otp";
		String asunto = "BuhoBank - Verificá tu email";
		Objeto parametros = new Objeto();
		parametros.set("ASUNTO", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("CODIGO", otp);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioBB(Contexto contexto, String asunto, String plantilla, String para, String url) {

		Objeto parametros = new Objeto();
		parametros.set("ASUNTO", asunto);
		if (url != null) {
			parametros.set("URL", url);
		}
		return post(contexto, de(contexto), para, plantilla, parametros);
	}
	
	public static EnvioEmail postEnvioBBV2(Contexto contexto, String asunto, String plantilla, String para, String nombre, String linkPromociones, String contenido) {

		Objeto parametros = new Objeto();
		parametros.set("ASUNTO", asunto);
		parametros.set("NOMBRE_CLIENTE", nombre);
		parametros.set("LINK_PROMOCIONES", linkPromociones);
		parametros.set("ITEMS_PACK", contenido);
		return post(contexto, de(contexto), para, plantilla, parametros);
	}

	public static EnvioEmail postEnvioMailBB(Contexto contexto, String plantilla, String asunto, String para, String urlStore) {
		Objeto parametros = new Objeto();
		String template = plantilla(contexto, plantilla);

		if (template == null) {
			return null;
		}

		parametros.set("ASUNTO", asunto(contexto, plantilla, asunto));
		parametros.set("URL_STORE", urlStore);
		return post(contexto, de(contexto), para, template, parametros);
	}

	public static EnvioEmail postEnvioInvitacionOB(Contexto contexto, String para, String nombre, String apellido, String empresa, String token) {
		String plantilla = "ob_envio_invitacion";
		String asunto = "Office Banking - Invitación";

		String url = format(contexto.config.string("ob_url_frontend") + "/AltaOperador_1/operadorToken=" + token);

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre + " " + apellido);
		parametros.set("EMPRESA", empresa);
		parametros.set("URL", url);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}
	public static EnvioEmail postEnvioInvitacionNuevoUserAdministradorOB(Contexto contexto, String para, String nombre, String apellido, EmpresaOB empresa) {
		String plantilla = "ob_invitacion_admin";
		String asunto = "Office Banking - Invitación ";

		String url = format(contexto.config.string("ob_url_frontend") + "/AltaOperadorAdmin_1/");

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre + " " + apellido ); 
		parametros.set("EMPRESA", empresa.razonSocial);
		parametros.set("URL", url);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}
	public static EnvioEmail postEnvioVinculacionEmpresaAdministradorOB(Contexto contexto, String para, String nombre, String apellido, EmpresaOB empresa) {
		String plantilla = "ob_bienvenida_admin";//OB-CRM-Bienvenida";
		String asunto = "Office Banking - Bienvenido/a ";

		String url = format(contexto.config.string("ob_url_frontend") + "/login");

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre + " " + apellido); 
		parametros.set("EMPRESA", empresa.razonSocial);
		parametros.set("URL", url);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	
	public static EnvioEmail postAltaNominaOB(Contexto contexto, String para, String nombre, String apellido, String empresa) {
		String plantilla = "ob_nomina";
		String asunto = "Office Banking - Altas de Cuentas";
		
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("EMPRESA", empresa);
		//parametros.set("URL", url);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioDatosDeCuentaOB(Contexto contexto, String email, String cbu, String alias, String cuenta, String titular, String cuit, String tipoCuenta) {
		String plantilla = "ob_datos_cuenta";
		String asunto = "Office Banking - Información de cuenta";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		String valorDeAlias = alias.isEmpty() ? "-" : alias;
		String valorCuit = cuit.isEmpty() ? "-" : cuit;
		parametros.set("CBU", cbu);
		parametros.set("ALIAS", valorDeAlias);
		parametros.set("CUENTA", cuenta);
		parametros.set("TITULAR", titular);
		parametros.set("CUIL", valorCuit);
		parametros.set("TIPO_CUENTA", tipoCuenta);
		return post(contexto, de(contexto), email, plantilla(contexto, plantilla), parametros);
	}
	
	
	
	public static EnvioEmail postAvisoCambioClaveOB(Contexto contexto, String para, String nombre, String apellido) {
		String plantilla = "ob_cambio_de_clave";
		String asunto = "Office Banking - Aviso: Cambio de Clave";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("FECHA", Fecha.hoy().string("dd/MM/yyyy"));
		parametros.set("HORA", Fecha.ahora().string("HH:mm"));
		parametros.set("CANAL", "Office Banking");
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postAvisoActivacionSoftToken(Contexto contexto, String para, String nombre, String apellido) {
		String plantilla = "ob_activacion_softtoken";
		String asunto = "Office Banking - Aviso: Activación de SoftToken";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("FECHA", Fecha.hoy().string("dd/MM/yyyy"));
		parametros.set("HORA", Fecha.ahora().string("HH:mm"));
		parametros.set("CANAL", "Office Banking");
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postAvisoModificaDatosPersonales(Contexto contexto, String para, String nombre, String apellido) {
		String plantilla = "ob_modifica_datos_personales";
		String asunto = "Office Banking - Aviso: Modifcación de Datos Personales";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		parametros.set("FECHA", Fecha.hoy().string("dd/MM/yyyy"));
		parametros.set("HORA", Fecha.ahora().string("HH:mm"));
		parametros.set("CANAL", "Office Banking");
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioBienvenidaOB(Contexto contexto, String para, String nombre, String apellido) {
		String plantilla = "ob_bienvenida";
		String asunto = "Office Banking - ¡Te damos la Bienvenida!";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("APELLIDO", apellido);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postTRNEnviadaOB(Contexto contexto, String para, String nombre, String monto, String operacion, String cuentaOrigen, String cuentaDestino) {
		String plantilla = "ob_trn_envio";
		String asunto = "Office Banking - Envio de transferencia";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("MONTO", monto);
		parametros.set("CUENTA_ORIGEN", cuentaOrigen);
		parametros.set("CUENTA_DESTINO", cuentaDestino);
		parametros.set("NRO_OPERACION", operacion);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}
	//Contexto contexto, String para, String nombre, String mensaje

	public static EnvioEmail postEnvioAvisoBeneficiario(Contexto contexto, InfoCuentaDTO infoCuenta, String banco, String fechaCreacion, String destinatario, String usuario) {
		String plantilla = "OB-Nuevo-Beneficiario";
		String asunto = "Office Banking - Nuevo Beneficiario";
		String para = destinatario;
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE_APELLIDO", usuario);
		parametros.set("FECHA", fechaCreacion);
		parametros.set("HORA", fechaCreacion);
		parametros.set("NOMBRE_BENEFICIARIO", infoCuenta.cuenta.nombreTitular);
		parametros.set("APELLIDO_BENEFICIARIO", "");
		parametros.set("CUIT", infoCuenta.cuenta.cuit);
		parametros.set("CBU", infoCuenta.cuenta.cbu);
		parametros.set("ALIAS", infoCuenta.cuenta.nuevoAlias!=null?infoCuenta.cuenta.nuevoAlias:"");
		parametros.set("BANCO", banco!=null?banco:infoCuenta.cuenta.nroBco);
		return post(contexto, de(contexto), para , plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postAvisoSinPlantilla(Contexto contexto, String para, String cc, String nombre, String asunto, String mensaje, String hmlMensaje) {
		String plantilla = null;

		Objeto parametros = new Objeto();
		parametros.set("additionalProp1", mensaje);
		parametros.set("mensaje", mensaje);
		parametros.set("copiaCarbon", cc);
		String html=hmlMensaje;
		return postSinPlantilla(contexto, de(contexto), para, cc, asunto, parametros, html);
	}

	public static EnvioEmail postTRNRecibidaOB(Contexto contexto, String para, String nombre, String monto, String operacion, String cuentaOrigen, String cuentaDestino) {
		String plantilla = "ob_trn_destino";
		String asunto = "Office Banking - Recibiste una transferencia";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE", nombre);
		parametros.set("MONTO", monto);
		parametros.set("CUENTA_ORIGEN", cuentaOrigen);
		parametros.set("CUENTA_DESTINO", cuentaDestino);
		parametros.set("NRO_OPERACION", operacion);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postOperacionError(Contexto contexto, String para, String tipoOperacion, String idOperacion, String fecha) {
		String plantilla = "ob_operacion_error";
		String asunto = "Office Banking - Error en operación";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("TIPO_OPERACION", tipoOperacion);
		parametros.set("ID_OPERACION", idOperacion);
		parametros.set("FECHA", fecha);
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postSolicitarPermisosOB(Contexto contexto, String para, String nombreOperadorInicial, String nombreSolicitante, ArrayList<String> funcionalidadesSolicitadas, String empresa) {
		String plantilla = "ob_solicitar_permisos";
		String asunto = "Office Banking - Solicitud de permisos";

		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE_OPERADOR_INICIAL", nombreOperadorInicial);
		parametros.set("NOMBRE_SOLICITANTE", nombreSolicitante);
		
		for (int i=0; i< funcionalidadesSolicitadas.size();i++) {
			parametros.set("POINT_"+i, "• ");
			parametros.set("FUNCIONALIDADES_SOLICITADAS_"+i, funcionalidadesSolicitadas.get(i));
		}
				
		parametros.set("EMPRESA", empresa);

		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}

	public static EnvioEmail postEnvioCheque(Contexto contexto, String para, EcheqOB cheque) {
		String plantilla = "f39d068c-e36c-4c56-a093-3eeaca356701";
		String asunto = "Office Banking - Nuevo Cheque";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE_APELLIDO", cheque.razonSocialBeneficiario);
		parametros.set("EMPRESA", cheque.empresa.razonSocial);
		parametros.set("CUIT", cheque.empresa.cuit);
		//datos del cheque
		parametros.set("ID", cheque.idCheque);
		parametros.set("NUMERO", cheque.numeroCheque);
		parametros.set("IMPORTE", cheque.monto);
		parametros.set("FECHA", cheque.fechaPago.toString());
		parametros.set("MOTIVO", cheque.motivoPago);

		return post(contexto, de(contexto), para, plantilla , parametros);
	}

	public static EnvioEmail postEnvioMailInhabilitadoSeguridad(Contexto contexto, String para, UsuarioOB usuario) {
		String plantilla = "ob_bloqueo_cuenta_seguridad";
		String asunto = "Solicitud de bloqueo de acceso a Office Banking";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("NOMBRE_USUARIO", usuario.nombre + " "+ usuario.apellido );
		parametros.set("CUIT_USUARIO", usuario.cuil);
		parametros.set("FECHA", Fecha.ahora().dia()+"/"+Fecha.ahora().mes() +"/"+Fecha.ahora().año());
		parametros.set("HORA",Fecha.ahora().hora() + ":" + Fecha.ahora().minuto());
		return post(contexto, de(contexto), para, plantilla(contexto, plantilla), parametros);
	}
	public static EnvioEmail postEnvioMailInhabilitado(Contexto contexto, UsuarioOB usuario) {
		String plantilla = "ob_bloqueo_cuenta";
		String asunto = "Solicitaste el bloqueo de tu cuenta en Office Banking";
		Objeto parametros = new Objeto();
		parametros.set("Subject", asunto(contexto, plantilla, asunto));
		parametros.set("FECHA", Fecha.ahora().dia()+"/"+Fecha.ahora().mes() +"/"+Fecha.ahora().año());
		parametros.set("HORA",Fecha.ahora().hora() + ":" + Fecha.ahora().minuto());
		return post(contexto, de(contexto), usuario.email, plantilla(contexto, plantilla), parametros);
	}

	/* ========== METODOS PRIVADOS ========== */
	private static String de(Contexto contexto) {
		return contexto.config.string("backend_doppler_de", "aviso@mail-hipotecario.com.ar");
	}

	private static String asunto(Contexto contexto, String template, String valorPorDefecto) {
		return contexto.config.string("backend_doppler_asunto_" + template, valorPorDefecto);
	}

	public static String plantilla(Contexto contexto, String template) {
		return contexto.config.string("backend_doppler_plantilla_" + template, null);
	}

	/* ================ utils ================= */
	private static String extraerXCobis(Contexto ctx) {
		if (ctx instanceof ContextoOB ob) {
			var sesion = ob.sesion();
			if (sesion != null && sesion.empresaOB != null) {
				return sesion.empresaOB.idCobis;
			}
		}
		return null;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("OB", "desarrollo");
		String test = "postErrorTRN";

		if (test.equals("postErrorTRN")) {
			EnvioEmail datos = postOperacionError(contexto, "nmtabucchi@hipotecario.com.ar", "Transferencia", "10005", "12/4/2023");
			imprimirResultado(contexto, datos);
		}

		/*
		 * if (test.equals("postRecuperoUsuarioOB")) { EnvioEmail datos =
		 * postRecuperoUsuarioOB(contexto, "gsuarez@hipotecario.com.ar", "Emiliano",
		 * "Kalujerovich", "kalujeroviche"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postEnvioOtpOB")) { EnvioEmail datos =
		 * postEnvioOtpOB(contexto, "gsuarez@hipotecario.com.ar", "Emiliano",
		 * "Kalujerovich", "12323"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postEnvioOtpBB")) { EnvioEmail datos =
		 * postEnvioOtpBB(contexto, "menavarro@hipotecario.com.ar", "12345", "MARTIN");
		 * imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postAvisoCambioClaveOB")) { EnvioEmail datos =
		 * postAvisoCambioClaveOB(contexto, "maristiqui@hipotecario.com.ar", "Emiliano",
		 * "Kalujerovich"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postAvisoActivacionSoftToken")) { EnvioEmail datos =
		 * postAvisoActivacionSoftToken(contexto, "maristiqui@hipotecario.com.ar",
		 * "Emiliano", "Kalujerovich"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postAvisoModificaDatosPersonales")) { EnvioEmail datos =
		 * postAvisoModificaDatosPersonales(contexto, "maristiqui@hipotecario.com.ar",
		 * "Emiliano", "Kalujerovich"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postBienvenidaOB")) { EnvioEmail datos =
		 * postEnvioBienvenidaOB(contexto, "maristiqui@hipotecario.com.ar", "Emiliano",
		 * "Kalujerovich"); imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postTRNEnviadaOB")) { EnvioEmail datos =
		 * postTRNEnviadaOB(contexto, "mceschi@hipotecario.com.ar", "Marcelo Ceschi",
		 * "1500", "123456", "2001212121233", "3001212121233");
		 * imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postTRNRecibidaOB")) { EnvioEmail datos =
		 * postTRNRecibidaOB(contexto, "mceschi@hipotecario.com.ar", "Marcelo Ceschi",
		 * "1500", "123456", "2001212121233", "3001212121233");
		 * imprimirResultado(contexto, datos); }
		 *
		 * if (test.equals("postSolicitarPermisosOB")) { EnvioEmail datos =
		 * postSolicitarPermisosOB(contexto, "rncastanervivas@hipotecario.com.ar",
		 * "Marcelo Ceschi", "Rocio Castañer", "Pago de impuesto", "Cachirulo");
		 * imprimirResultado(contexto, datos); }
		 */

	}
}