
package ar.com.hipotecario.canal.officebanking;

import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils.Cuil;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet.Opcion;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet.Pregunta;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.LoginOBAnterior;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigrarUsuarioOB;
import ar.com.hipotecario.backend.servicio.api.seguridad.OTP;
import ar.com.hipotecario.backend.servicio.api.seguridad.SoftToken;
import ar.com.hipotecario.backend.servicio.api.seguridad.UsuarioISVA;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosEmpresasOBAnterior;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior.UsuarioOBAnterior;
import ar.com.hipotecario.canal.officebanking.SesionOBAnterior.TokenCorreo;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioListaBlancaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ListaBlancaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

public class OBSeguridadAnterior extends ModuloOB {

	public static Object login(ContextoOB contexto) {
		String usuario = contexto.parametros.string("usuario");
		Long cuit = contexto.parametros.longer("cuit");
		String clave = contexto.parametros.string("clave");

		SesionOBAnterior sesion = contexto.sesionOBAnterior();
		LoginOBAnterior loginOBAnterior = ApiSeguridad.loginOBAnterior(contexto, cuit, usuario, clave).get();

		UsuarioOBAnterior usuarioOBAnterior = SqlHB_BE.usuarioEmpresaOBAnteriorPorUsuarioYCuit(contexto, usuario, cuit).get();
		ServicioListaBlancaOB servicioUsuarioCuil = new ServicioListaBlancaOB(contexto);

		ListaBlancaOB listaBlanca = servicioUsuarioCuil.findByCuil(Long.valueOf(usuarioOBAnterior.usu_cuil)).tryGet();

		if (empty(listaBlanca)) {
			return respuesta("NO_ES_POSIBLE_MIGRARSE");
		}

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = servicioUsuario.findByCuil(Long.valueOf(usuarioOBAnterior.usu_cuil)).tryGet();
		if (!empty(usuarioOB)) {
			return respuesta("USUARIO_MIGRADO");
		}

		sesion.cuitEmpresa = loginOBAnterior.emp_cuit;
		sesion.usuario = loginOBAnterior.usu_login;
		sesion.cuil = usuarioOBAnterior.usu_cuil;

		sesion.crearSesion();
		LogOB.evento(contexto, "LOGIN_ANTERIOR");

		UsuariosEmpresasOBAnterior usuarioEmpresasOB = SqlHB_BE.usuarioEmpresasOBAnteriorPorCuil(contexto, usuarioOBAnterior.usu_cuil).get();
		List<Objeto> empresasActivas = usuarioEmpresasOB.buscarObjetosPorEstado("1");
		List<Objeto> empresasInactivas = usuarioEmpresasOB.buscarObjetosPorEstado("0");

		Objeto datos = new Objeto();
		datos.set("cuil", usuarioOBAnterior.usu_cuil);
		datos.set("total", usuarioEmpresasOB.size());

		Objeto activos = new Objeto();
		activos.set("total", empresasActivas.size());
		activos.set("empresas", empresasActivas);

		datos.set("activos", activos);

		Objeto bloqueados = new Objeto();
		bloqueados.set("total", empresasInactivas.size());
		bloqueados.set("empresas", empresasInactivas);

		datos.set("bloqueados", bloqueados);

		return respuesta("datos", datos);
	}

	public static Object validadores(ContextoOB contexto) {
		SesionOBAnterior sesion = contexto.sesionOBAnterior();
		sesion.validadores.clear();

		Futuro<UsuarioOBAnterior> futuroUsuarioOBAnterior = SqlHB_BE.usuarioOBAnteriorPorCuil(contexto, sesion.cuil);
		UsuarioOBAnterior usuario = futuroUsuarioOBAnterior.get();
//		String email = usuario.usu_email;
		String celular = usuario.valido_otp.equals("1") ? usuario.usu_telefono_movil : null;
		sesion.idCobis = usuario.usu_idCobis;

		UsuarioISVA usuarioISVA = null;
		if (!empty(sesion.idCobis)) {
			Futuro<UsuarioISVA> futuroUsuarioISVA = ApiSeguridad.usuarioISVA(contexto, sesion.idCobis);
			usuarioISVA = futuroUsuarioISVA.get();
		}

		Objeto validadores = new Objeto();
		if (!empty(usuarioISVA) && usuarioISVA.tieneSoftToken) {
			SoftToken softToken = ApiSeguridad.softToken(contexto, sesion.idCobis).tryGet();
			sesion.token.estado = softToken.stateId;
			sesion.token.cookie = softToken.cookie;
			sesion.save();
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

		/*
		 * Boolean emailValidado = usuario.usu_email_validado.equals("1"); if
		 * (emailValido(email) && (emailValidado || validadores.objetos().size() == 0))
		 * { Objeto validador = new Objeto(); validador.set("id", "EMAIL_TOKEN");
		 * validador.set("descripcion", "Email a " + emailEnmascarado(email));
		 * validadores.add(validador); }
		 */

		if (validadores.isEmpty()) {
			return respuesta("SIN_VALIDADORES");
		}

		List<String> lstValidadores = validadores.objetos().stream().map(v -> v.get("id").toString()).collect(Collectors.toList());
		sesion.validadores.addAll(lstValidadores);
		sesion.save();

		return respuesta("validadores", validadores);
	}

	public static Object enviarToken(ContextoOB contexto) {
		String validador = contexto.parametros.type("validador", "SOFT_TOKEN", "EMAIL_TOKEN", "SMS_TOKEN");
		SesionOBAnterior sesion = contexto.sesionOBAnterior();

		if (!sesion.validadores.contains(validador)) {
			return respuesta("VALIDADOR_INVALIDO");
		}

		Futuro<UsuarioOBAnterior> futuroUsuarioOBAnterior = SqlHB_BE.usuarioOBAnteriorPorCuil(contexto, sesion.cuil);
		Futuro<OTP> futuroOTP = ApiSeguridad.generarOTP(contexto, sesion.idCobis);
		UsuarioOBAnterior usuarioOBAnterior = futuroUsuarioOBAnterior.get();
		OTP otp = futuroOTP.tryGet();

		String idCobis = usuarioOBAnterior.usu_idCobis;
		String nombre = usuarioOBAnterior.usu_nombre;
		String apellido = usuarioOBAnterior.usu_apellido;
		String email = usuarioOBAnterior.usu_email;
		String celular = usuarioOBAnterior.usu_telefono_movil;

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
				ApiNotificaciones.envioOtpSms(contexto, telefono, codigo);
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
			datos.set("token", sesion.token.codigo);
			respuesta.set("datos", datos);
		}
		return respuesta;
	}

	public static Object validarToken(ContextoOB contexto) {
		String validador = contexto.parametros.type("validador", "SOFT_TOKEN", "EMAIL_TOKEN", "SMS_TOKEN");
		String token = contexto.parametros.string("token");
		SesionOBAnterior sesion = contexto.sesionOBAnterior();

		Boolean validarToken = contexto.esProduccion() || !token.equals("0");
		if (validarToken && sesion.token.codigo == null) {
			return respuesta("TOKEN_NO_GENERADO");
		}

		if (validarToken && set("SOFT_TOKEN").contains(validador)) {
			if (validarToken && sesion.token.estado == null) {
				LogOB.evento(contexto, "OBSeguridad_validarToken", "TOKEN_NO_GENERADO");
				return respuesta("TOKEN_NO_GENERADO");
			} else if (validarToken) {
				SoftToken validar = ApiSeguridad.softToken(contexto, sesion.idCobis, sesion.token.estado, sesion.token.cookie, token).tryGet();

				LogOB.evento(contexto, "OBSeguridad_validarToken", new Objeto().set("validar", validar));

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
				ApiSeguridad.validarOTP(contexto, sesion.idCobis, token, sesion.token.estado, sesion.token.cookie).get();
			} else if (!sesion.token.codigo.equals(token)) {
				return respuesta("TOKEN_INVALIDO");
			}
		}

		if (validarToken && set("EMAIL_TOKEN").contains(validador)) {
			if (sesion.token.cookie != null) {
				ApiSeguridad.validarOTP(contexto, sesion.idCobis, token, sesion.token.estado, sesion.token.cookie).get();
			} else if (!sesion.token.codigo.equals(token)) {
				LogOB.evento(contexto, "OBSeguridad_validarToken", "TOKEN_NO_GENERADO");

				return respuesta("TOKEN_INVALIDO");
			}
		}

		LogOB.evento(contexto, "TOKEN_VALIDO");
		sesion.token = new SesionOBAnterior.Token();
		sesion.token.fechaValidacion = Fecha.ahora();
		sesion.save();

		return respuesta();
	}

	public static Object enviarTokenCorreo(ContextoOB contexto) {
		String email = contexto.parametros.string("email");

		SesionOBAnterior sesion = contexto.sesionOBAnterior();
		sesion.tokenCorreo = null;

		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		UsuarioOB usuarioOB = servicioUsuario.findByEmail(email).tryGet();
		if (!empty(usuarioOB)) {
			return respuesta("EMAIL_REGISTRADO");
		}

		sesion.tokenCorreo = new TokenCorreo();
		sesion.tokenCorreo.codigo = random(100000, 999999).toString();
		sesion.tokenCorreo.correo = email;
		sesion.save();

		LogOB.evento(contexto, "ENVIAR_CORREO", new Objeto().set("email", email));
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
		SesionOBAnterior sesion = contexto.sesionOBAnterior();

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

	public static Object validarDNI(ContextoOB contexto) {
		String dni = contexto.parametros.string("dni");

		SesionOBAnterior sesion = contexto.sesionOBAnterior();
		sesion.numeroDocumento = null;

		Cuils cuils = ApiPersonas.cuils(contexto, dni).get();
		List<String> listaCuils = cuils.stream().map(Cuil::getCuil).collect(Collectors.toList());
		if (!listaCuils.contains(sesion.cuil)) {
			sesion.save();
			return respuesta("DNI_INVALIDO");
		}

		sesion.numeroDocumento = dni;
		sesion.save();

		LogOB.evento(contexto, "VALIDAR_DNI", new Objeto().set("dni", dni));

		return respuesta("0");
	}

	public static Object migrarUsuario(ContextoOB contexto) {
		String usuario = contexto.parametros.string("usuario");
		String clave = contexto.parametros.string("clave");
		String cuits = contexto.parametros.string("cuits", null);

		SesionOBAnterior sesion = contexto.sesionOBAnterior();
		String dni = sesion.numeroDocumento;
		String correo = sesion.tokenCorreo.correo;

		if (!usuarioClaveValidos(usuario, clave)) {
			return respuesta("DATOS_INVALIDOS");
		}

		if (empty(dni)) {
			return respuesta("DNI_NO_VALIDADO");
		}

		if (sesion.tokenCorreo.fechaValidacion.isNull()) {
			return respuesta("TOKEN_CORREO_NO_VALIDADO");
		}

		if (sesion.token.fechaValidacion.isNull()) {
			return respuesta("FACTOR_NO_VALIDADO");
		}
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.tokenCorreo.fechaValidacion = Fecha.nunca();
		sesion.numeroDocumento = null;
		sesion.save();

		MigrarUsuarioOB result = ApiSeguridad.migrarUsuarioOB(contexto, sesion.cuitEmpresa, sesion.usuario, dni, usuario, clave, cuits, correo).get();

		LogOB.evento(contexto, "MIGRAR_USUARIO");

		if (result.codigoHttp() == 200) {
			Futuro<UsuarioOBAnterior> futuroUsuarioOBAnterior = SqlHB_BE.usuarioOBAnteriorPorCuityCuil(contexto, sesion.cuil, Long.valueOf(sesion.cuitEmpresa));
			UsuarioOBAnterior usuarioOBAnterior = futuroUsuarioOBAnterior.get();
			String nombre = "";
			String apellido = " ";
			nombre = usuarioOBAnterior.usu_nombre;
			apellido = usuarioOBAnterior.usu_apellido;

			try {
				EnvioEmail envioMail = ApiNotificaciones.envioBienvenidaOB(contexto, correo, nombre, apellido).tryGet();
				if (envioMail == null) {
					LogOB.error(contexto, "Error enviando Bienvenida OB: " + correo);
				}

			} catch (Exception ex) {
			}
		}

		return respuesta();
	}

	public static Object consultarPreguntas(ContextoOB contexto) {
		SesionOBAnterior sesion = contexto.sesionOBAnterior();

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
		SesionOBAnterior sesion = contexto.sesionOBAnterior();
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
			SqlHB_BE.registrarIntentoRiesgonet(contexto, Long.valueOf(sesion.cuitEmpresa), sesion.cuil, exitoso).get();
			if (exitoso == 1) {
				sesion.preguntasRiesgoNet.valido = true;
				sesion.save();
				return respuesta();
			} else {
				return respuesta("VALIDACION_FALLIDA");
			}
		}
	}
}