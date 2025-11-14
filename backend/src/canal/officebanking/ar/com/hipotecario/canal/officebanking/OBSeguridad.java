
package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Captcha;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesOBV2;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioSMS;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet.Opcion;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet.Pregunta;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.ConsultaQRSoftToken;
import ar.com.hipotecario.backend.servicio.api.seguridad.LoginGire;
import ar.com.hipotecario.backend.servicio.api.seguridad.OTP;
import ar.com.hipotecario.backend.servicio.api.seguridad.SoftToken;
import ar.com.hipotecario.backend.servicio.api.seguridad.TokenISVA;
import ar.com.hipotecario.backend.servicio.api.seguridad.UsuarioGire;
import ar.com.hipotecario.backend.servicio.api.seguridad.UsuarioISVA;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.hb_be.TokensOB;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.domain.enums.BankProcessChangeDataType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.BankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.ChangeDataBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.TransactionBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.generics.TransactionBankProcess;
import ar.com.hipotecario.canal.officebanking.SesionOB.TokenCorreo;
import ar.com.hipotecario.canal.officebanking.SesionOB.TokenSMS;
import ar.com.hipotecario.canal.officebanking.enums.EnumMigracionTransmit;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioInvitacionAdministradorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionAdministradorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.transmit.TransmitOB;

public class OBSeguridad extends ModuloOB {

	public static Object saveLog(ContextoOB contexto) {
		try {
			String evento = contexto.parametros.string("evento");
			String datos = contexto.parametros.string("datos");
	    
			LogOB.evento(contexto, evento, datos);
		
			return respuesta("0");
		}catch(Exception ex) {
			LogOB.evento(contexto,"saveLog", ex.getMessage());
			return respuesta("ERROR_SAVE_LOG");
		}
	}
		
	public static Object extraerContenido(ContextoOB contexto) {
		try {
			String valor = contexto.parametros.string("valor");
			String token = JwtOB.generarToken(Config.desencriptarOB(valor));
			return respuesta("token", token);
		}catch(Exception ex) {
			LogOB.evento(contexto,"extraerContenido", ex.getMessage());
			return respuesta("ERROR_EXTRAER_CONTENIDO");
		}
	}
	
	public static Object captcha(ContextoOB contexto) {
		Captcha captcha = Captcha.generar(4, 100, 50);
		SesionOB sesion = contexto.sesion();
		sesion.captcha.texto = captcha.texto;
		sesion.save();
		if (!contexto.esProduccion()) {
			contexto.responseHeader("captcha", captcha.texto);
		}
		return captcha.archivo();
	}

	public static Object validarCaptcha(ContextoOB contexto) {
		String captcha = contexto.parametros.string("captcha");

		SesionOB sesion = contexto.sesion();
		Boolean validarCaptcha = contexto.esProduccion() || !captcha.equals("0");
		if (validarCaptcha) {
			if (sesion.captcha.texto == null) {
				return respuesta("CAPTCHA_NO_GENERADO");
			} else if (!captcha.equals(sesion.captcha.texto)) {
				return respuesta("CAPTCHA_INVALIDO");
			}
		}
		sesion.captcha.fechaValidacion = Fecha.ahora();
		sesion.captcha.texto = null;
		sesion.save();
		LogOB.evento(contexto, "CAPTCHA_VALIDO");
		return respuesta();
	}

	public static Object validarCorreoYCelular(ContextoOB contexto) {
		String email = contexto.parametros.string("email", null);
		String telefonoMovil = contexto.parametros.string("telefonoMovil", null);
 
		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
 
		if (email != null && !empty(servicioUsuario.findByEmail(email).tryGet())) {
			return respuesta("EMAIL_REGISTRADO");
		}
 
		if (telefonoMovil != null && !(servicioUsuario.findByCelular(telefonoMovil).tryGet().isEmpty())) {
			return respuesta("CELULAR_REGISTRADO");
		}
 
		return respuesta("DATOS_CORRECTOS");
	}
	
	public static Object validadores(ContextoOB contexto) {
		Objeto validadores = obtenerValidadores(contexto,false);

		if (validadores.isEmpty()) {
			return respuesta("SIN_VALIDADORES");
		}

		return respuesta("validadores", validadores);
	}
	public static Object V1validadores(ContextoOB contexto) {
		Objeto validadores = obtenerValidadores(contexto,true);

		if (validadores.isEmpty()) {
			return respuesta("SIN_VALIDADORES");
		}

		return respuesta("validadores", validadores);
	}

	public static Object validarFactores(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();

		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (empty(sesion.idCobis)) {
				return respuesta("SIN_IDCOBIS");
			}

			List<String> lstValidadores = obtenerValidadores(contexto,false).objetos().stream().map(v -> v.get("id").toString()).collect(Collectors.toList());
			if (sesion.usuarioOB.telefonoMovil != null && !lstValidadores.contains("SMS_TOKEN") && lstValidadores.contains("SOFT_TOKEN")) {
				return respuesta("ACTUALIZAR_CELULAR");
			}

			if (sesion.usuarioOB.telefonoMovil == null && !lstValidadores.contains("SMS_TOKEN") && lstValidadores.contains("SOFT_TOKEN")) {
				return respuesta("REGISTRAR_CELULAR");
			}

			if (!lstValidadores.contains("SMS_TOKEN") && !lstValidadores.contains("SOFT_TOKEN")) {
				return respuesta("SIN_FACTORES");
			}

			if (lstValidadores.contains("SMS_TOKEN") && !lstValidadores.contains("SOFT_TOKEN")) {
				return respuesta("ACTIVAR_SOFT_TOKEN");
			}
		}else {
			if (sesion.hasSoftToken == false && sesion.usuarioOB.softTokenActivo == false) {
				return respuesta("ACTIVAR_SOFT_TOKEN");
			}
			if (sesion.hasSoftToken == false && sesion.usuarioOB.softTokenActivo == true) {
				return respuesta("REACTIVAR_SOFT_TOKEN");
			}
		}

		return respuesta("0");
	}

	private static Objeto obtenerValidadores(ContextoOB contexto,Boolean emailReques) {

		SesionOB sesion = contexto.sesion();

		UsuarioOB usuarioOB = sesion.usuarioOB;
		//el email es solo para tu cuenta esta en reisgo. avisar si en el canal lo usan para otra cosa.
		String email="";
      /*if(emailReques){
		  email = usuarioOB.emailValidado.equals(true) ? usuarioOB.email : null;
	  }*/
		String celular = usuarioOB.validoOTP.equals(true) ? usuarioOB.telefonoMovil : null;
		String idCobis = usuarioOB.idCobis;

		UsuarioISVA usuarioISVA = null;
		if (!empty(idCobis)) {
			Futuro<UsuarioISVA> futuroUsuarioISVA = ApiSeguridad.usuarioISVA(contexto, idCobis);
			usuarioISVA = futuroUsuarioISVA.tryGet();
		}

		// TODO: analizar de parametrizar los factores en la base
		Objeto validadores = new Objeto();
		if (!empty(usuarioISVA) && usuarioISVA.tieneSoftToken && sesion.usuarioOB.softTokenActivo) {
			Objeto validador = new Objeto();
			validador.set("id", "SOFT_TOKEN");
			validador.set("descripcion", "Soft Token");
			validadores.add(validador);
		}
		if (celularValido(celular)) {
			Objeto validador = new Objeto();
			validador.set("id", "SMS_TOKEN");
			validador.set("descripcion", "SMS al " + celularEnmascarado(celular));
			validadores.add(validador);
		}

		if (emailValido(email)&&emailReques) { Objeto validador = new Objeto();
			validador.set("id", "EMAIL_TOKEN"); validador.set("descripcion", "Email a " +
					emailEnmascarado(email)); validadores.add(validador); }


		sesion.save();

		return validadores;
	}

	public static Object enviarToken(ContextoOB contexto) {
		String validador = contexto.parametros.type("validador", "SOFT_TOKEN", "EMAIL_TOKEN", "SMS_TOKEN");
		Integer idOperacion = null;
		String moneda = null;
		BigDecimal monto = null;
		String cbu = null;
		String mensaje = null;
		
		if(contexto.parametros.existe("idOperacion")) {
			idOperacion = contexto.parametros.integer("idOperacion");
		}
		if(contexto.parametros.existe("moneda")) {
			moneda =  contexto.parametros.string("moneda").replace("80", "$").replace("2", "U$D");
		}
		if(contexto.parametros.existe("monto")) {
			monto = contexto.parametros.bigDecimal("monto");
		}
		if(contexto.parametros.existe("cbu")) {
			cbu = contexto.parametros.string("cbu");
			if(cbu != "false") {
				cbu = cbu.substring(cbu.length() - 4);
			}else {
				cbu = null;
			}
		}
		
		SesionOB sesion = contexto.sesion();

		List<String> lstValidadores = obtenerValidadores(contexto,true).objetos().stream().map(v -> v.get("id").toString()).collect(Collectors.toList());
		if (!lstValidadores.contains(validador)) {
			return respuesta("VALIDADOR_INVALIDO");
		}

		UsuarioOB usuarioOB = sesion.usuarioOB;
		Futuro<OTP> futuroOTP = ApiSeguridad.generarOTP(contexto, usuarioOB.idCobis);
		OTP otp = futuroOTP.tryGet();

		String idCobis = usuarioOB.idCobis;
		String nombre = usuarioOB.nombre;
		String apellido = usuarioOB.apellido;
		String email = usuarioOB.email;
		String celular = usuarioOB.telefonoMovil;

		if (set("SOFT_TOKEN").contains(validador)) {
			SoftToken softToken = ApiSeguridad.softToken(contexto, idCobis).get();
			sesion.token.codigo = "***";
			sesion.token.estado = softToken.stateId;
			sesion.token.cookie = softToken.cookie;
			sesion.save();
			LogOB.evento(contexto, "SOFT_TOKEN");
			return respuesta();
		}

		if (set("SMS_TOKEN", "EMAIL_TOKEN").contains(validador)) {
			sesion.token.codigo = otp != null ? otp.clave : random(100000, 999999).toString();
			sesion.token.estado = otp != null ? otp.stateId : null;
			sesion.token.cookie = otp != null ? otp.cookie : null;
			sesion.save();

			if (validador.equals("SMS_TOKEN")) {
				String telefono = celular;
				String codigo = sesion.token.codigo;
				LogOB.evento(contexto, "SMS_TOKEN", new Objeto().set("telefono", telefono));

				if(idOperacion!= null && idOperacion.intValue() == EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()) {
					if(cbu != null) {
						mensaje="Nunca te pediremos este dato por teléfono u otro medio. No lo compartas. Tu código SMS es " + codigo + " para transferir a CBU/CVU *****" + cbu + " por " + moneda + "" + monto;
					}else {
						mensaje="Nunca te pediremos este dato por teléfono u otro medio. No lo compartas. Tu código SMS es " + codigo + " para transferir por " + moneda + "" + monto;
					}
					ApiNotificaciones.envioOtpSms(contexto, telefono, codigo, mensaje);
				}else {
					ApiNotificaciones.envioOtpSms(contexto, telefono, codigo);
				}								
			}

			if (validador.equals("EMAIL_TOKEN")) {
				String codigo = sesion.token.codigo;
				LogOB.evento(contexto, "EMAIL_TOKEN", new Objeto().set("email", email));
				ApiNotificaciones.envioOtpEmailOB(contexto, email, nombre, apellido, codigo).get();
			}
		}

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			Objeto datos = new Objeto();
			if(mensaje == null) {
				datos.set("token", sesion.token.codigo);	
			}else {
				datos.set("token", mensaje);
			}
						
			respuesta.set("datos", datos);
		}
		return respuesta;
	}

	public static Object validarToken(ContextoOB contexto) {
		String validador = contexto.parametros.type("validador", "SOFT_TOKEN", "EMAIL_TOKEN", "SMS_TOKEN");
		String token = contexto.parametros.string("token");

		SesionOB sesion = contexto.sesion();

		Boolean validarToken = contexto.esProduccion() || !token.equals("0");
		if (validarToken && sesion.token.codigo == null) {
			return respuesta("TOKEN_NO_GENERADO");
		}

		if (validarToken && set("SOFT_TOKEN").contains(validador)) {
			if (validarToken && sesion.token.estado == null) {
				return respuesta("TOKEN_NO_GENERADO");
			} else if (validarToken) {
				SoftToken validar = ApiSeguridad.softToken(contexto, sesion.usuarioOB.idCobis, sesion.token.estado, sesion.token.cookie, token).tryGet();
				if (!empty(validar.state)) {
					sesion.token.estado = validar.state;
					sesion.save();
					return respuesta("TOKEN_INVALIDO");
				}

			}
			sesion.token.fechaValidacion = Fecha.ahora();
			sesion.token.cookie = null;
			sesion.save();

			return respuesta();
		}

		if (validarToken && set("SMS_TOKEN").contains(validador)) {
			if (sesion.token.cookie != null) {
				ApiSeguridad.validarOTP(contexto, sesion.usuarioOB.idCobis, token, sesion.token.estado, sesion.token.cookie).get();
			} else if (!sesion.token.codigo.equals(token)) {
				return respuesta("TOKEN_INVALIDO");
			}
		}

		if (validarToken && set("EMAIL_TOKEN").contains(validador)) {
			if (sesion.token.cookie != null) {
				ApiSeguridad.validarOTP(contexto, sesion.usuarioOB.idCobis, token, sesion.token.estado, sesion.token.cookie).get();
			} else if (!sesion.token.codigo.equals(token)) {
				return respuesta("TOKEN_INVALIDO");
			}
		}

		LogOB.evento(contexto, "TOKEN_VALIDO");
		sesion.token = new SesionOB.Token();
		sesion.token.fechaValidacion = Fecha.ahora();

		boolean pagoMultiple = false;
		if (contexto.parametros.existe("multiple")) {
			pagoMultiple = contexto.parametros.bool("multiple");
		}

		if (pagoMultiple) {
			String uuid = UUID.randomUUID().toString();
			Long cuit = sesion.empresaOB.cuit;
			String cuil = sesion.usuarioOB.cuil.toString();
			String usu_codigo = sesion.usuarioOB.codigo.toString();
			Fecha fechaExpiracion = Fecha.ahora().sumarMinutos(5);
			TokensOB.crear(contexto, uuid, cuit.toString(), cuil, "ob-pago-multiple", fechaExpiracion, usu_codigo);
			sesion.uuid = uuid;
		}

		sesion.save();

		return respuesta();
	}

	public static Object recuperarUsuario(ContextoOB contexto) {
		String usuario = contexto.parametros.string("usuario");

		SesionOB sesion = contexto.sesion();

		if (!usuarioValido(usuario)) {
			return respuesta("USUARIO_INVALIDO");
		}

		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		 
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (usuarioOB.estado.codigo.equals(0)) {
			usuarioOB.estado.codigo = 1;			
		}
		
		usuarioOB.login = Config.encriptarAES(usuario);
		servicioUsuario.update(usuarioOB);
		
		contexto.deleteSesion();
		return respuesta();
	}

	public static Object recuperarClave(ContextoOB contexto) {
		String clave = contexto.parametros.string("clave");

		SesionOB sesion = contexto.sesion();

		if (!claveValida(clave)) {
			return respuesta("CLAVE_INVALIDA");
		}

		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		 
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().usuarioOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"recuperarClave".equals(contexto.sesion().challenge)){
			if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
				LogOB.evento(contexto, "recuperarClave", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				contexto.sesion().challenge = "recuperarClave";
				contexto.sesion().save();
				return respuesta("CHALLENGE");
			}
			if (recommendationDTO.getRecommendationType().equals("DENY")) {
				LogOB.evento(contexto, "recuperarClave", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				return respuesta("DENY");
			}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}
		*/
		ApiSeguridad.recuperarClaveOB(contexto, sesion.usuarioOB.numeroDocumento.toString(), sesion.usuarioOB.login, clave).get();
		LogOB.evento(contexto, "RECUPERO_CLAVE");

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (usuarioOB.estado.codigo.equals(0)) {
			usuarioOB.estado.codigo = 1;
			servicioUsuario.update(usuarioOB);
		}

		EnvioEmail envioMail = ApiNotificaciones.envioAvisoCambioClave(contexto, sesion.usuarioOB.email, sesion.usuarioOB.nombre, sesion.usuarioOB.apellido).tryGet();
		if (envioMail == null) {
			LogOB.error(contexto, "Error enviando Aviso Cambio Clave: " + sesion.usuarioOB.email + "|" + sesion.usuarioOB.nombre + "|" + sesion.usuarioOB.apellido + "|");
		}

		contexto.deleteSesion();
		return respuesta();
	}

	public static Object recuperarUsuarioClave(ContextoOB contexto) {
		String usuario = contexto.parametros.string("usuario");
		String clave = contexto.parametros.string("clave");

		SesionOB sesion = contexto.sesion();

		if (!usuarioValido(usuario)) {
			return respuesta("USUARIO_INVALIDO");
		}
		if (!claveValida(clave)) {
			return respuesta("CLAVE_INVALIDA");
		}

		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		 
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (usuarioOB.estado.codigo.equals(0)) {
			usuarioOB.estado.codigo = 1;			
		}
		
		usuarioOB.login = Config.encriptarAES(usuario);
		ServicioClaveUsuarioOB servicioClaveUsuario = new ServicioClaveUsuarioOB(contexto);
		ClaveUsuarioOB nuevaClave = new ClaveUsuarioOB();
		nuevaClave.clave = Encriptador.sha512(clave);
		nuevaClave.usuario = usuarioOB;
		nuevaClave.fechaCreacion = LocalDateTime.now();
		servicioClaveUsuario.create(nuevaClave);
		
		servicioUsuario.update(usuarioOB);
		
		contexto.deleteSesion();
		return respuesta();
	}
	
	public static Object consultarPreguntas(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		if (SqlHB_BE.consultarUsuarioBloqueado(contexto, cuil).get()) {
			return respuesta("USUARIO_BLOQUEADO_POR_NUMERO_DE_INTENTOS");
		} else {

			Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, cuil);
			Futuro<Domicilios> futuroDomicilios = ApiPersonas.domicilios(contexto, cuil);

			Persona persona = futuroPersona.get();
			Domicilios domicilios = futuroDomicilios.get();

			Domicilio domicilioPostal = domicilios.postal();
			if (domicilioPostal == null) {
				return respuesta("SIN_DOMICILIO_POSTAL");
			}

			PreguntasRiesgoNet.Request request = new PreguntasRiesgoNet.Request();
			request.dni = persona.numeroDocumento;
			request.genero = persona.idSexo;
			request.cuit = persona.cuit;
			request.apellido = persona.apellidos;
			request.nombre = persona.nombres;
			request.fechaNacimiento = persona.fechaNacimiento;
			request.provincia = domicilioPostal.provincia;
			request.localidad = domicilioPostal.ciudad;
			request.calle = domicilioPostal.calle;
			request.altura = domicilioPostal.numero;

			PreguntasRiesgoNet preguntasRiesgoNet = ApiPersonas.preguntasRiesgoNet(contexto, request).get();
			sesion.preguntasRiesgoNet = preguntasRiesgoNet;
			sesion.preguntasRiesgoNet.valido = false;
			sesion.save();

			Objeto respuesta = respuesta();
			for (Pregunta pregunta : preguntasRiesgoNet) {
				Objeto item = new Objeto();
				item.set("id", pregunta.id);
				item.set("enunciado", pregunta.enunciado);
				for (Opcion opcion : pregunta.opciones) {
					Objeto subitem = new Objeto();
					subitem.set("id", opcion.id);
					subitem.set("texto", opcion.texto);
					if (!contexto.esProduccion()) {
						subitem.set("correcta", opcion.correcta);
					}
					item.add("opciones", subitem);
				}
				respuesta.add("preguntas", item);
			}
			return respuesta;
		}
	}

	public static Object responderPreguntas(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		PreguntasRiesgoNet preguntasRiesgoNet = sesion.preguntasRiesgoNet;
		List<Objeto> respuestas = contexto.parametros.objetos("respuestas");

		if (SqlHB_BE.consultarUsuarioBloqueado(contexto, sesion.cuil).get()) {
			return respuesta("USUARIO_BLOQUEADO_POR_NUMERO_DE_INTENTOS");
		} else {
			Integer cantidadRespuestasCorrectasRiesgonet = contexto.config.integer("cantidad_respuestas_correctas_riesgonet");
			// Revisar respuestas
			int totalRespuestasCorrectas = 0;
			for (Pregunta pregunta : preguntasRiesgoNet) {
				for (Objeto respuesta : respuestas) {
					if (pregunta.id.equals(respuesta.get("idPregunta").toString())) {
						if (preguntasRiesgoNet.esCorrecta(pregunta.id, respuesta.get("idRespuesta").toString())) {
							totalRespuestasCorrectas++;
						}
					}
				}
			}
			// Guardar en base de datos el intento
			int exitoso = 0;
			if (totalRespuestasCorrectas >= cantidadRespuestasCorrectasRiesgonet) {
				exitoso = 1;
			}
			SqlHB_BE.registrarIntentoRiesgonet(contexto, sesion.empresaOB.cuit, sesion.cuil, exitoso).get();
			if (exitoso == 1) {
				sesion.preguntasRiesgoNet.valido = true;
				sesion.save();
				return respuesta();
			} else {
				return respuesta("VALIDACION_FALLIDA");
			}
		}
	}

	public static Object altaSoftToken(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String idCobis = sesion.usuarioOB.idCobis;

		if (empty(idCobis)) {
			return respuesta("SIN_IDCOBIS");
		}

		if (sesion.token.fechaValidacion.isNull()) {
			LogOB.evento(contexto, "firmar", "VALIDACION TOKEN PARA ALTA ST");
			return respuesta("FACTOR_NO_VALIDADO");
		}

		UsuarioISVA usuarioISVA = ApiSeguridad.usuarioISVA(contexto, idCobis).get();
		if (empty(usuarioISVA.idISVA)) {
			usuarioISVA = ApiSeguridad.postUsuarioISVA(contexto, idCobis, "").tryGet();
		}

		if (!empty(usuarioISVA.idISVA)) {
			TokenISVA token = ApiSeguridad.token(contexto).get();
			ConsultaQRSoftToken result = ApiSeguridad.consultaQR(contexto, idCobis, token.access_token).get();
			return new Archivo("qr.png", result.qr);
		}

		return respuesta("IDCOBIS_INVALIDO");
	}

	public static Object bajaSoftToken(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String idCobis = sesion.usuarioOB.idCobis;

		TokenISVA token = ApiSeguridad.token(contexto).get();
		ApiSeguridad.bajaSoftToken(contexto, idCobis, token.access_token).get();

		UsuarioOB usuarioOB = sesion.usuarioOB;
		usuarioOB.softTokenActivo = false;
		ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
		servicio.update(usuarioOB);

		sesion.usuarioOB = usuarioOB;
		sesion.save();

		LogOB.evento(contexto, "BAJA_SOFT_TOKEN");
		return respuesta();
	}

	public static Object enviarActualizarSms(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		UsuarioOB usuarioOB = sesion.usuarioOB;
		Futuro<OTP> futuroOTP = ApiSeguridad.generarOTP(contexto, usuarioOB.idCobis);
		OTP otp = futuroOTP.tryGet();

		String telefono = contexto.parametros.string("telefono");
		String codigo = otp != null ? otp.clave : random(100000, 999999).toString();

		LogOB.evento(contexto, "ENVIO_SMS_ACTUALIZAR", new Objeto().set("telefono", telefono));

		EnvioSMS respuesta = ApiNotificaciones.envioOtpSms(contexto, telefono, codigo).tryGet();
		if (contexto.esProduccion() && (empty(respuesta) || respuesta.codigoHttp() != 200)) {
			return respuesta("ERROR_ENVIO_SMS");
		}

		sesion.tokenActualizarSMS = new TokenSMS();
		sesion.tokenActualizarSMS.codigo = codigo;
		sesion.tokenActualizarSMS.telefono = telefono;
		sesion.save();

		return respuesta("0");
	}

	public static Object validarActualizarSms(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String token = contexto.parametros.string("token");
		Long numeroDocumento = sesion.usuarioOB.numeroDocumento;

		Boolean validarClave = contexto.esProduccion() || !token.equals("0");

		if (validarClave && (empty(sesion.tokenActualizarSMS) || !sesion.tokenActualizarSMS.codigo.equals(token))) {
			return respuesta("TOKEN_INVALIDO");
		}

		LogOB.evento(contexto, "VALIDAR_SMS_ACTUALIZAR", new Objeto().set("token", token));

		sesion.tokenActualizarSMS.fechaValidacion = Fecha.ahora();
		sesion.save();

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = servicioUsuario.findByNumeroDocumento(Long.valueOf(numeroDocumento)).tryGet();
		usuarioOB.validoOTP = true;

		ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
		servicio.update(usuarioOB);

		sesion.usuarioOB = usuarioOB;
		sesion.save();

		LogOB.evento(contexto, "ACTUALIZAR_SMS");
		return respuesta("0");
	}

	public static Object validarTokenActualizarSms(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String token = contexto.parametros.string("token");

		Boolean validarClave = contexto.esProduccion() || !token.equals("0");

		if (validarClave && (empty(sesion.tokenActualizarSMS) || !sesion.tokenActualizarSMS.codigo.equals(token))) {
			return respuesta("TOKEN_INVALIDO");
		}

		LogOB.evento(contexto, "VALIDAR_SMS_ACTUALIZAR", new Objeto().set("token", token));

		sesion.tokenActualizarSMS.fechaValidacion = Fecha.ahora();
		sesion.save();

		LogOB.evento(contexto, "ACTUALIZAR_SMS");
		return respuesta("0");
	}

	public static Object enviarAltaSoftToken(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String idCobis = sesion.usuarioOB.idCobis;


		UsuarioISVA usuarioISVA = null;
		if (!empty(idCobis)) {
			Futuro<UsuarioISVA> futuroUsuarioISVA = ApiSeguridad.usuarioISVA(contexto, idCobis);
			usuarioISVA = futuroUsuarioISVA.get();
		}

		if (!empty(usuarioISVA) && usuarioISVA.tieneSoftToken) {
			SoftToken softToken = ApiSeguridad.softToken(contexto, idCobis).tryGet();

			sesion.tokenActivarSoftToken.estado = softToken.stateId;
			sesion.tokenActivarSoftToken.cookie = softToken.cookie;
			sesion.save();
		} else {
			return respuesta("FACTOR_INVALIDO");
		}

		LogOB.evento(contexto, "ENVIO_ALTA_SOFT_TOKEN");
		return respuesta();
	}

	public static Object validarAltaSoftToken(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		String token = contexto.parametros.string("token");
		/*
		ChangeDataBEBankProcess data = new ChangeDataBEBankProcess(contexto.sesion().empresaOB.idCobis,contexto.sesion().sessionId, BankProcessChangeDataType.ACCOUNT_AUTH_CHANGE);
		RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, data);
		if(!"validarAltaSoftToken".equals(contexto.sesion().challenge)){
			if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
				LogOB.evento(contexto, "validarAltaSoftToken", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				contexto.sesion().challenge = "validarAltaSoftToken";
				contexto.sesion().save();
				return respuesta("CHALLENGE");
			}
			if (recommendationDTO.getRecommendationType().equals("DENY")) {
				LogOB.evento(contexto, "validarAltaSoftToken", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
				return respuesta("DENY");
			}
		}else {
			contexto.sesion().challenge = null;
			contexto.sesion().save();
		}*/

		if (sesion.tokenActivarSoftToken.estado == null) {
			return respuesta("TOKEN_NO_GENERADO");
		}

		Boolean validarToken = contexto.esProduccion() || !token.equals("0");
		if (validarToken) {
			SoftToken validar = ApiSeguridad.softToken(contexto, sesion.usuarioOB.idCobis, sesion.tokenActivarSoftToken.estado, sesion.tokenActivarSoftToken.cookie, token).tryGet();
			if (!empty(validar.state)) {
				sesion.tokenActivarSoftToken.estado = validar.state;
				sesion.save();
				return respuesta("TOKEN_INVALIDO");
			}
		}

		sesion.tokenActivarSoftToken.estado = null;
		sesion.tokenActivarSoftToken.cookie = null;
		sesion.save();

		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (!usuarioOB.softTokenActivo) {
			usuarioOB.softTokenActivo = true;
			ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);
			servicio.update(usuarioOB);

			sesion.usuarioOB = usuarioOB;
			sesion.save();
		}

		EnvioEmail envioMail = ApiNotificaciones.envioAvisoActivacionSoftToken(contexto, sesion.usuarioOB.email, sesion.usuarioOB.nombre, sesion.usuarioOB.apellido).tryGet();
		if (envioMail == null) {
			LogOB.error(contexto, "Error enviando Aviso Activación de SoftToken: " + sesion.usuarioOB.email + "|" + sesion.usuarioOB.nombre + "|" + sesion.usuarioOB.apellido + "|");
		}

		return respuesta();
	}

	public static Object enviarTokenCorreo(ContextoOB contexto) {
		String email = contexto.parametros.string("email");

		SesionOB sesion = contexto.sesion();
		sesion.tokenCorreo = null;

		if (!emailValido(email)) {
			return respuesta("EMAIL_INVALIDO");
		}

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = servicioUsuario.findByEmail(email).tryGet();
		if (!empty(usuarioOB)) {
			return respuesta("EMAIL_REGISTRADO");
		}

		sesion.tokenCorreo = new TokenCorreo();
		sesion.tokenCorreo.codigo = random(100000, 999999).toString();
		sesion.tokenCorreo.correo = email;
		sesion.save();

		LogOB.evento(contexto, "ENVIAR_TOKEN_CORREO", new Objeto().set("email", email));
		ApiNotificaciones.envioOtpEmailOB(contexto, email, "", "", sesion.tokenCorreo.codigo).get();

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			Objeto datos = new Objeto();
			datos.set("token", sesion.tokenCorreo.codigo);
			respuesta.set("datos", datos);
		}
		return respuesta;
	}

	public static Object validarTokenCorreo(ContextoOB contexto) {
		String token = contexto.parametros.string("token");
		SesionOB sesion = contexto.sesion();

		Boolean validarToken = contexto.esProduccion() || !token.equals("0");
		if (validarToken && sesion.tokenCorreo.codigo == null) {
			return respuesta("TOKEN_NO_GENERADO");
		}

		if (validarToken && !sesion.tokenCorreo.codigo.equals(token)) {
			sesion.tokenCorreo.codigo = null;
			return respuesta("TOKEN_INVALIDO");
		}

		LogOB.evento(contexto, "VALIDAR_TOKEN_CORREO", new Objeto().set("email", token));

		sesion.tokenCorreo.fechaValidacion = Fecha.ahora();
		sesion.tokenCorreo.codigo = null;
		sesion.save();

		return respuesta("0");
	}

	public static Object enviarTokenInvitacionAdministrador(ContextoOB contexto) {
		Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
		ServicioInvitacionAdministradorOB servicioInvitaAdministradorOB = new ServicioInvitacionAdministradorOB(contexto);
		List<InvitacionAdministradorOB> listInvitacionAdministrador = servicioInvitaAdministradorOB.findByDNIEstado(contexto, numeroDocumento).tryGet();
		InvitacionAdministradorOB invitacionAdministrador = null;
		
		if(listInvitacionAdministrador!=null && listInvitacionAdministrador.size()>0 ) {
			invitacionAdministrador  = listInvitacionAdministrador.get(0);
		}
		String email = invitacionAdministrador.usu_correo;
		String nombre = invitacionAdministrador.usu_nombre;
		String apellido = invitacionAdministrador.usu_apellido;
		
		
		SesionOB sesion = contexto.sesion();
		sesion.tokenInvitacionCorreo = null;

		if (!emailValido(email)) {
			return respuesta("EMAIL_INVALIDO");
		}

		sesion.tokenInvitacionCorreo = new TokenCorreo();
		sesion.tokenInvitacionCorreo.codigo = random(100000, 999999).toString();
		sesion.tokenInvitacionCorreo.correo = email;
		sesion.save();

		LogOB.evento(contexto, "ENVIAR_TOKEN_INVITACION", new Objeto().set("email", email));
		ApiNotificaciones.envioOtpEmailOB(contexto, email, nombre, apellido, sesion.tokenInvitacionCorreo.codigo).get();

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			Objeto datos = new Objeto();
			datos.set("token", sesion.tokenInvitacionCorreo.codigo);
			respuesta.set("datos", datos);
		}
		return respuesta;

	}

	public static Object enviarTokenInvitacion(ContextoOB contexto) {
		String email = contexto.parametros.string("email");

		SesionOB sesion = contexto.sesion();
		sesion.tokenInvitacionCorreo = null;

		if (!emailValido(email)) {
			return respuesta("EMAIL_INVALIDO");
		}

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = servicioUsuario.findByEmail(email).tryGet();
		if (!empty(usuarioOB)) {
			LogOB.evento(contexto, "ENVIAR_TOKEN_INVITACION", new Objeto().set("email", email).set("Error", "EMAIL_REGISTRADO"));
			return respuesta("EMAIL_REGISTRADO");
		}

		sesion.tokenInvitacionCorreo = new TokenCorreo();
		sesion.tokenInvitacionCorreo.codigo = random(100000, 999999).toString();
		sesion.tokenInvitacionCorreo.correo = email;
		sesion.save();

		LogOB.evento(contexto, "ENVIAR_TOKEN_INVITACION", new Objeto().set("email", email));
		ApiNotificaciones.envioOtpEmailOB(contexto, email, "", "", sesion.tokenInvitacionCorreo.codigo).get();

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			Objeto datos = new Objeto();
			datos.set("token", sesion.tokenInvitacionCorreo.codigo);
			respuesta.set("datos", datos);
		}
		return respuesta;
	}

	public static Object validarTokenInvitacion(ContextoOB contexto) {
		String token = contexto.parametros.string("token");
		SesionOB sesion = contexto.sesion();

		Boolean validarToken = contexto.esProduccion() || !token.equals("0");
		if (validarToken && sesion.tokenInvitacionCorreo.codigo == null) {
			return respuesta("TOKEN_NO_GENERADO");
		}

		if (validarToken && !sesion.tokenInvitacionCorreo.codigo.equals(token)) {
			sesion.tokenInvitacionCorreo.codigo = null;
			return respuesta("TOKEN_INVALIDO");
		}

		LogOB.evento(contexto, "VALIDAR_TOKEN_INVITACION", new Objeto().set("email", token));

		sesion.tokenInvitacionCorreo.fechaValidacion = Fecha.ahora();
		sesion.tokenInvitacionCorreo.codigo = null;
		sesion.save();

		return respuesta("0");
	}

	
	public static Object enviarSmsInvitacionAdministrador(ContextoOB contexto) {
		Long numeroDocumento = contexto.parametros.longer("numeroDocumento");

		ServicioInvitacionAdministradorOB servicioInvitacionesAdmin = new ServicioInvitacionAdministradorOB(contexto);
		List<InvitacionAdministradorOB> listInvitacionAdmin = servicioInvitacionesAdmin.findByDNIEstado(contexto, numeroDocumento).tryGet();
		
		if (listInvitacionAdmin!=null && listInvitacionAdmin.size()==0) {
			return respuesta("INVITACION_ADM_INVALIDA");
		}
		
		InvitacionAdministradorOB invitacionPrincipal = listInvitacionAdmin.get(0);
	
		if(empty(invitacionPrincipal)) {
			LogOB.evento_sinSesion(contexto, "INVITACION", new Objeto().set("numeroDocumento", numeroDocumento), numeroDocumento.toString());
		}
		
		String telefono = invitacionPrincipal.usu_telefono_movil;
		String codigo = random(100000, 999999).toString();
		SesionOB sesion = contexto.sesion();
		
		LogOB.evento_sinSesion(contexto, "ENVIO_SMS_INVITACION", new Objeto().set("telefono", telefono).set("empresa", invitacionPrincipal.empresa.cuit), invitacionPrincipal.usu_cuil.toString());
		
		EnvioSMS respuesta = ApiNotificaciones.envioOtpSms(contexto, telefono, codigo).tryGet();
		if (contexto.esProduccion() && (empty(respuesta) || respuesta.codigoHttp() != 200)) {
			LogOB.evento_sinSesion(contexto, "ERROR_ENVIO_SMS", new Objeto().set("telefono", telefono).set("empresa", invitacionPrincipal.empresa.cuit), invitacionPrincipal.usu_cuil.toString());
			return respuesta("ERROR_ENVIO_SMS");
		}

		sesion.tokenInvitacionSMS = new TokenSMS();
		sesion.tokenInvitacionSMS.codigo = codigo;
		sesion.tokenInvitacionSMS.telefono = telefono;
		sesion.save();

		return respuesta("0");
	}

	public static Object enviarSmsInvitacion(ContextoOB contexto) {
		String telefono = contexto.parametros.string("telefono");
		String codigo = random(100000, 999999).toString();
		SesionOB sesion = contexto.sesion();

		LogOB.evento(contexto, "ENVIO_SMS_INVITACION", new Objeto().set("telefono", telefono));

		EnvioSMS respuesta = ApiNotificaciones.envioOtpSms(contexto, telefono, codigo).tryGet();
		if (contexto.esProduccion() && (empty(respuesta) || respuesta.codigoHttp() != 200)) {
			return respuesta("ERROR_ENVIO_SMS");
		}

		sesion.tokenInvitacionSMS = new TokenSMS();
		sesion.tokenInvitacionSMS.codigo = codigo;
		sesion.tokenInvitacionSMS.telefono = telefono;
		sesion.save();

		return respuesta("0");
	}

	public static Object validarSmsInvitacion(ContextoOB contexto) {
		String token = contexto.parametros.string("token");
		SesionOB sesion = contexto.sesion();

		Boolean validarClave = contexto.esProduccion() || !token.equals("0");

		if (validarClave && (empty(sesion.tokenInvitacionSMS) || !sesion.tokenInvitacionSMS.codigo.equals(token))) {
			return respuesta("TOKEN_INVALIDO");
		}

		LogOB.evento(contexto, "VALIDAR_SMS_INVITACION", new Objeto().set("token", token));

		sesion.tokenInvitacionSMS.fechaValidacion = Fecha.ahora();
		sesion.save();

		return respuesta("0");
	}

	public static Objeto usuarioGirePost(ContextoOB contexto) {
		final ServicioParametroOB servicioParametro = new ServicioParametroOB(contexto);
		String urlGire = null;
		UsuarioGire usuarioGire = null;
		try {
			urlGire = servicioParametro.find("url.gireSoluciones").get().valor;
		}catch(Exception e) {
			return respuesta("ERROR_OBTENER_URL_GIRE");
		}
		SesionOB sesion = contexto.sesion();
		try {
			Objeto body = new Objeto();
			Objeto empresa = new Objeto(); //
			empresa.set("limiteCheques", "0");//
			empresa.set("razonSocial", sesion.empresaOB.razonSocial);
			empresa.set("cuit", sesion.empresaOB.cuitFormateado());
			Objeto tipoProcesos = new Objeto();
			tipoProcesos.add(new Objeto().set("tipo","1"));
			tipoProcesos.add(new Objeto().set("tipo","2"));
			String officeBankingId = contexto.sesion().usuarioOB.idCobis != null?contexto.sesion().usuarioOB.idCobis:String.valueOf(contexto.sesion().usuarioOB.cuil);
			// obtiene las cuentas habilitadas, las recorre y completa del cuerpo lo faltante
			//Object cuentas = OBCuentas.cuentasHabilitadas(contexto, Moneda.PESOS,null);
			Objeto cuentasResponse = (Objeto) OBCuentas.cuentas(contexto);
			Objeto datos = (Objeto) cuentasResponse.get("datos");
			Objeto objetoCuentas = (Objeto) datos.get("cuentas");
			Objeto cuentaUsuarios = new Objeto();
			if (objetoCuentas.isList()) {
				List<Objeto> cuentas = objetoCuentas.objetos();
				if(!cuentas.isEmpty()) {
					Integer contador = 0;
					for (Objeto cuenta : cuentas) {
						//Objeto tipoCuenta =	(Objeto) OBCuentas.validar_CuentaUnipersonal(contexto,String.valueOf(sesion.usuarioOB.cuil) );
						//if(tipoCuenta.get("estado") == "EMPRESA") {
						if((cuenta.get("tipoProducto").equals("CTE") || cuenta.get("tipoProducto").equals("AHO")) && cuenta.get("moneda").equals("80")) {
							String sucursal = (String) cuenta.get("sucursal");
							String nroProductoAux = (String) cuenta.get("numeroProducto");
							String nroProducto = cuenta.get("tipoProducto").equals("CTE")?"3".concat(nroProductoAux):"4".concat(nroProductoAux);
							// cuentaUsuario

							Objeto cuentaUsuario = new Objeto();
							cuentaUsuario.set("numero", nroProducto);
							cuentaUsuario.set("officeBankingId", officeBankingId);
							cuentaUsuarios.add(cuentaUsuario);

							// empresa
							//contador ++;
							Objeto limiteCuenta = new Objeto();

							limiteCuenta.set("cuentaBase", "0");
							limiteCuenta.set("cuentaNumero", nroProducto);
							limiteCuenta.set("importeMaximo", "0");
							// limiteCuenta.set("lid", contador);
							limiteCuenta.set("limiteCheques", "0");
							limiteCuenta.set("limiteDias", "0");
							limiteCuenta.set("procTipo", "1");
							limiteCuenta.set("sucursalCuenta", String.format("%04d",Integer.valueOf(sucursal)));

							boolean sucursalAgregada = false;
							for (Objeto localizacionAux : empresa.objetos("localizaciones")) {
								if (sucursal.equals(localizacionAux.string("sucradicacion"))) {
									limiteCuenta.set("lid", localizacionAux.string("lid"));
									sucursalAgregada = true;
								}
							}

							empresa.add("limiteCuentas", limiteCuenta);
							empresa.set("tiposProcesos", tipoProcesos);
							if (!sucursalAgregada) {
								contador++;
								limiteCuenta.set("lid", contador);
								Objeto localizacion = new Objeto();
								localizacion.set("importeMaximo", "0");
								localizacion.set("lid", contador.toString());
								localizacion.set("limiteDias", "0");
								String descripcionSucursal = "";
								try {
									SucursalesOBV2 responseSucursalesOBV2 = ApiCatalogo.sucursalesOBV2(contexto,sucursal).get();
									if(responseSucursalesOBV2 != null) {
										descripcionSucursal = responseSucursalesOBV2.desSucursal;
									}
								}catch (Exception e) {
									// continua con el flujo
								}
								localizacion.set("nombre", descripcionSucursal);
								localizacion.set("sucradicacion", sucursal);
								empresa.add("localizaciones", localizacion);
							}
							// cuentaEmpresa
							Objeto cuentaEmpresa = new Objeto();
							cuentaEmpresa.set("numero", nroProducto);
							empresa.add("cuentas", cuentaEmpresa);
							empresa.set("horarioCorte", "17:00:00");//
							empresa.set("responsabilidadEndoso", "N");
						}
					}
				}
				body.set("cuentaUsuarios", cuentaUsuarios);
				//localizaciones
				Objeto localizacionesUsuarios = new Objeto();
				localizacionesUsuarios.add(new Objeto().set("lid", "1").set("officeBankingId", officeBankingId));//
				body.set("localizacionesUsuarios", localizacionesUsuarios);//

				//empresa
				body.set("empresa", empresa);

				//usuario
				Objeto usuario = new Objeto();
				usuario.set("perfilrd", "A");//
				usuario.set("nombre", contexto.sesion().usuarioOB.nombreCompleto());//
				usuario.set("officeBankingId", officeBankingId); ///?????
				usuario.set("rolMobile", "S");
				Objeto usuarios = new Objeto();
				usuarios.add(usuario);
				body.set("usuarios", usuarios);

				usuarioGire = ApiSeguridad.usuarioGire(contexto,body).get();
				if(usuarioGire!=null) {
					contexto.parametros.set("adheridoGire", true);
					Objeto respuesta = (Objeto) OBUsuarios.updateAdheridoGire(contexto);
					if ("ERROR_MODIFICAR_ADHERIDO_GIRE".equals(respuesta.get("estado"))) {
						return respuesta;
					}
				}else {
					return new Objeto().set("ERROR_USUARIO_GIRE");
				}
			} else {
				// en caso de error devuelve el error en la obtencion de las cuentas
				return (Objeto)cuentasResponse;
			}
		} catch (Exception e) {
			return new Objeto().set("ERROR");
		}


//		SqlResponse responseUpdateCuentasGire = RestSeguridad.updateCuentasGireUsuarioSql(contexto);
//		if (responseUpdateCuentasGire.hayError) {
//			return Respuesta.estado("ERROR_BASE_DE_DATOS_POS_POST");
//		}

//		Respuesta respuesta = new Respuesta();
//		respuesta.set("mensaje", responseGirePost.string("mensaje"));
//		respuesta.set("action", gireAdhesionPostAction);
		Objeto respuesta = new Objeto();
		respuesta.set("estado", "OK");
		respuesta.set("mensaje", usuarioGire.mensaje);
		respuesta.set("urlGire",urlGire);
		return respuesta;
	}
	
	public static Objeto loginGire(ContextoOB contexto) {
		final ServicioParametroOB servicioParametro = new ServicioParametroOB(contexto);
	    String urlGire = null;
		try {
        	urlGire = servicioParametro.find("url.gireSoluciones").get().valor;
        }catch(Exception e) {
        	return respuesta("ERROR_OBTENER_URL_GIRE");
        }
		SesionOB sesionOB = contexto.sesion();
		// en el office viejo se utiliza como officeBankingId el id con el que ingresan (el usu_login). 
		//En el caso del Ob nuevo, el usu_login parece ser un hash y es por esto que uso el id de cobis
		LoginGire loginGire = ApiSeguridad.loginGire(contexto,String.valueOf(sesionOB.empresaOB.cuit), sesionOB.empresaOB.razonSocial, sesionOB.usuarioOB.idCobis).get();
		Objeto respuesta = new Objeto();
		respuesta.set("estado", "OK");
		respuesta.set("mensaje", loginGire.mensaje);
		respuesta.set("urlGire",urlGire);	
		return respuesta;
	}
}